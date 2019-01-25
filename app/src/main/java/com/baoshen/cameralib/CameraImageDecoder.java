package com.baoshen.cameralib;

import android.graphics.Bitmap;
import android.graphics.Rect;
import android.support.annotation.Keep;

import com.baoshen.common.cache.BytesCache;
import com.baoshen.common.cache.CacheManager;
import com.baoshen.common.cache.IntsCache;
import com.baoshen.common.graphics.Size;

@Keep
public class CameraImageDecoder {

    private static final String CACHE_KEY_NV21_ROTATED = "NV21_ROTATED";
    private static final String CACHE_KEY_SMALL_BGR = "SMALL_BGR";
    private static final String CACHE_KEY_SMALL_ARGB = "SMALL_ARGB";
    private static final String CACHE_KEY_SMALL_RGBA = "SMALL_RGBA";
    private static final String CACHE_KEY_LARGE_YUV = "LARGE_YUV";
    private static final String CACHE_KEY_LARGE_BGR = "LARGE_BGR";
    private static final String CACHE_KEY_LARGE_ARGB = "LARGE_ARGB";
    private static final String CACHE_KEY_CHANNELS = "CHANNELS";

    public static Bitmap toRgba(byte[] yuv420,int imageWidth,int imageHeight, Rect crop) {
        //坑爹的旋转(YUV的数据排布有些特殊，需要对其旋转)
        Rect decodeBound = crop;
        if (decodeBound == null) {
            decodeBound = new Rect(0, 0, imageWidth, imageHeight);
        }
        int smallWidth = decodeBound.width();
        int smallHeight = decodeBound.height();
        int smallLength = smallWidth * smallHeight;
        int[] rgbaImage = getIntsCache(CACHE_KEY_SMALL_RGBA, smallLength);
        int[] rgba = rgbaImage;
        int height = imageHeight;
        int width = imageWidth;
        int frameSize = height * width;
        for (int i = decodeBound.top<0?0:decodeBound.top; i < decodeBound.bottom; i++) {
            for (int j = decodeBound.left<0?0:decodeBound.left; j < decodeBound.right; j++) {
                int y = (0xff & ((int) yuv420[i * width + j]));
                int u = (0xff & ((int) yuv420[frameSize + (i >> 1) * width + (j & ~1) + 0]));
                int v = (0xff & ((int) yuv420[frameSize + (i >> 1) * width + (j & ~1) + 1]));
                y = y < 16 ? 16 : y;

                int r = Math.round(1.164f * (y - 16) + 1.596f * (v - 128));
                int g = Math.round(1.164f * (y - 16) - 0.813f * (v - 128) - 0.391f * (u - 128));
                int b = Math.round(1.164f * (y - 16) + 2.018f * (u - 128));

                r = r < 0 ? 0 : (r > 255 ? 255 : r);
                g = g < 0 ? 0 : (g > 255 ? 255 : g);
                b = b < 0 ? 0 : (b > 255 ? 255 : b);

                rgba[(i - decodeBound.top) * smallWidth + (j - decodeBound.left)] = 0xff000000 + (b << 16) + (g << 8) + r;
            }
        }

        Bitmap bmp = Bitmap.createBitmap(smallWidth, smallHeight, Bitmap.Config.ARGB_8888);
        bmp.setPixels(rgba, 0, smallWidth, 0, 0, smallWidth, smallHeight);
        return bmp;
    }

    public static byte[] yuv420toBgr(byte[] yuv420, int imageWidth, int imageHeight, Rect crop, Size imageSize) {
        assert imageSize != null;
        if (null == crop) {
            crop = new Rect(0, 0, imageWidth, imageHeight);
        }
        Rect decodeBound = crop;
        int smallWidth = decodeBound.width();
        int smallHeight = decodeBound.height();
        int smallLength = smallWidth * smallHeight * 3;//3通道
        byte[] bgr = getBytesCache(CACHE_KEY_SMALL_BGR, smallLength);
        int height = imageHeight;
        int width = imageWidth;
        int frameSize = height * width;
        for (int i = decodeBound.top<0?0:decodeBound.top; i < decodeBound.bottom; i++)
            for (int j = decodeBound.left<0?0:decodeBound.left; j < decodeBound.right; j++) {
                int y = (0xff & ((int) yuv420[i * width + j]));
                int u = (0xff & ((int) yuv420[frameSize + (i >> 1) * width + (j & ~1) + 0]));
                int v = (0xff & ((int) yuv420[frameSize + (i >> 1) * width + (j & ~1) + 1]));
                y = y < 16 ? 16 : y;

                int r = Math.round(1.164f * (y - 16) + 1.596f * (v - 128));
                int g = Math.round(1.164f * (y - 16) - 0.813f * (v - 128) - 0.391f * (u - 128));
                int b = Math.round(1.164f * (y - 16) + 2.018f * (u - 128));

                r = r < 0 ? 0 : (r > 255 ? 255 : r);
                g = g < 0 ? 0 : (g > 255 ? 255 : g);
                b = b < 0 ? 0 : (b > 255 ? 255 : b);

                int index = ((i - decodeBound.top) * smallWidth + (j - decodeBound.left)) * 3;
                bgr[index] = (byte) (b & 0xff);
                bgr[index + 1] = (byte) (g & 0xff);
                bgr[index + 2] = (byte) (r & 0xff);
            }
        imageSize.setWidth(smallWidth);
        imageSize.setHeight(smallHeight);
        return bgr;
    }

    public static int[] bitmap2Argb(Bitmap bitmap,int width,int height){
        int bgrLength = width*height;
        int[] pixels = getIntsCache(CACHE_KEY_SMALL_ARGB,bgrLength);
        bitmap.getPixels(pixels, 0, width, 0, 0, width, height);
//        bitmap.recycle();
        return pixels;
    }

    public static byte[] rgb2YCbCr420(int[] bgr, int width, int height) {
        int len = width * height;
        int y, u, v;
        byte[] yuv =getBytesCache(CACHE_KEY_LARGE_YUV,width*height*3/2);
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                // 屏蔽ARGB的透明度值
                int rgb = bgr[i * width + j] & 0x00FFFFFF;
                // 像素的颜色顺序为bgr，移位运算。
//                int r = rgb & 0xFF;
//                int g = (rgb >> 8) & 0xFF;
//                int b = (rgb >> 16) & 0xFF;
                int r = (rgb>>16) & 0xFF;
                int g = (rgb >> 8) & 0xFF;
                int b = rgb & 0xFF;
                // 套用公式
                y = ((66 * r + 129 * g + 25 * b + 128) >> 8) + 16;
                u = ((-38 * r - 74 * g + 112 * b + 128) >> 8) + 128;
                v = ((112 * r - 94 * g - 18 * b + 128) >> 8) + 128;
                // rgb2yuv
                // y = (int) (0.299 * r + 0.587 * g + 0.114 * b);
                // u = (int) (-0.147 * r - 0.289 * g + 0.437 * b);
                // v = (int) (0.615 * r - 0.515 * g - 0.1 * b);
                // RGB转换YCbCr
                // y = (int) (0.299 * r + 0.587 * g + 0.114 * b);
                // u = (int) (-0.1687 * r - 0.3313 * g + 0.5 * b + 128);
                // if (u > 255)
                // u = 255;
                // v = (int) (0.5 * r - 0.4187 * g - 0.0813 * b + 128);
                // if (v > 255)
                // v = 255;
                // 调整
                y = y < 16 ? 16 : (y > 255 ? 255 : y);
                u = u < 0 ? 0 : (u > 255 ? 255 : u);
                v = v < 0 ? 0 : (v > 255 ? 255 : v);
                // 赋值
                yuv[i * width + j] = (byte) y;
                yuv[len + (i >> 1) * width + (j & ~1) + 0] = (byte) u;
                yuv[len + +(i >> 1) * width + (j & ~1) + 1] = (byte) v;
            }
        }
        return yuv;
    }
    public static byte[] argb2bgr(int[] argb ,int width,int height) {
        int bgrLength = width*height*3;
        byte[] bgr = getBytesCache(CACHE_KEY_SMALL_BGR,bgrLength);
        // Copy pixels into place
        for (int i = 0; i < argb.length; i++) {
            int pixel = argb[i];
            bgr[i * 3] = (byte)(pixel & 0xFF);//b
            bgr[i * 3 + 1] = (byte)((pixel >> 8) & 0xFF);//g
            bgr[i * 3 + 2] = (byte)((pixel >> 16) & 0xFF);//r
        }

        return bgr;
    }

    //对预览数据进行旋转
    public synchronized static byte[] rotatedYuv420(byte[] yuv420, int imageWidth, int imageHeight, int rotation){
        assert yuv420!=null:"buffer不应该为空";
        byte[] restoredYuv = yuv420;
        if (rotation > 0) {
            //要多部手机，测试下，这里的宽高，用不用兑换
            int buffLength = imageWidth * imageHeight * 3 / 2;
            byte[] nv21_rotated = getBytesCache(CACHE_KEY_NV21_ROTATED,buffLength);
            nv21Rotate(yuv420,nv21_rotated,imageWidth,imageHeight,rotation);
            restoredYuv = nv21_rotated;
        }
        return restoredYuv;
    }
    public synchronized static byte[] rotatedArgb(byte[] src, int width, int height, int rotation,int channels){
        assert src!=null:"buffer不应该为空";
        byte[] dst = src;
        if (rotation > 0) {
            //要多部手机，测试下，这里的宽高，用不用兑换
            int buffLength = width * height * channels;
            dst = getBytesCache(CACHE_KEY_CHANNELS,buffLength);
            if (rotation == 90) {
                argbRotate90(src, dst, width,height,channels);
            } else if (rotation == 180) {
                argbRotate180(src, dst, width, height,channels);
            } else if (rotation == 270) {
                argbRotate270(src, dst, width,height,channels);
            } else {
                //do nothing
            }
        }
        return dst;
    }
    private static byte[] getBytesCache(String key,int length){
        BytesCache cache = CacheManager.getBytesCache();
        byte[] buff = cache.get(key);
        if(buff==null || buff.length!=length){
            buff = new byte[length];
            cache.put(key,buff);
        }
        return buff;
    }
    private static int[] getIntsCache(String key,int length){
        IntsCache cache = CacheManager.getIntsCache();
        int[] buff = cache.get(key);
        if(buff==null || buff.length!=length){
            buff = new int[length];
            cache.put(key,buff);
        }
        return buff;
    }

    public synchronized static void nv21Rotate(byte[] nv21_data,byte[] rotated ,int width, int height, int angel) {
        assert nv21_data!=null && rotated!=null && rotated.length == nv21_data.length;
        if (angel == 90) {
            nv21Rotate90(nv21_data, rotated, width,height);
        } else if (angel == 180) {
            nv21Rotate180(nv21_data, rotated, width, height);
        } else if (angel == 270) {
            nv21Rotate270(nv21_data, rotated, height,width);
        } else {
            //do nothing
        }
    }

    private static byte[] nv21Rotate270(byte[] nv21_data,byte[] nv21_rotated, int width, int height) {
        int y_size = width * height;
        int buffser_size = y_size * 3 / 2;
        int i = 0;

        // Rotate the Y luma
        for (int x = width - 1; x >= 0; x--) {
            int offset = 0;
            for (int y = 0; y < height; y++) {
                nv21_rotated[i] = nv21_data[offset + x];
                i++;
                offset += width;
            }
        }
        // Rotate the U and V color components
        i = y_size;
        for (int x = width - 1; x > 0; x = x - 2) {
            int offset = y_size;
            for (int y = 0; y < height / 2; y++) {
                nv21_rotated[i] = nv21_data[offset + (x - 1)];
                i++;
                nv21_rotated[i] = nv21_data[offset + x];
                i++;
                offset += width;
            }
        }
        return nv21_rotated;
    }

    private static byte[] nv21Rotate180(byte[] nv21_data,byte[] nv21_rotated, int width, int height) {
        int y_size = width * height;
        int buffser_size = y_size * 3 / 2;
        int i = 0;
        int count = 0;
        for (i = y_size - 1; i >= 0; i--) {
            nv21_rotated[count] = nv21_data[i];
            count++;
        }

        for (i = buffser_size - 1; i >= y_size; i -= 2) {
            nv21_rotated[count++] = nv21_data[i - 1];
            nv21_rotated[count++] = nv21_data[i];
        }
        return nv21_rotated;
    }

    private static byte[] nv21Rotate90(byte[] nv21_data,byte[] nv21_rotated, int width, int height) {
        int y_size = width * height;
        int buffser_size = y_size * 3 / 2;
        // Rotate the Y luma
        int i = 0;
        int startPos = (height - 1) * width;
        for (int x = 0; x < width; x++) {
            int offset = startPos;
            for (int y = height - 1; y >= 0; y--) {
                nv21_rotated[i] = nv21_data[offset + x];
                i++;
                offset -= width;
            }
        }
        // Rotate the U and V color components
        i = buffser_size - 1;
        for (int x = width - 1; x > 0; x = x - 2) {
            int offset = y_size;
            for (int y = 0; y < height / 2; y++) {
                nv21_rotated[i] = nv21_data[offset + x];
                i--;
                nv21_rotated[i] = nv21_data[offset + (x - 1)];
                i--;
                offset += width;
            }
        }
        return nv21_rotated;
    }

    //适合三通道、4通道
    private static void argbRotate90(byte[] src_data,byte[] dst_rotated, int width, int height,int channels) {
        int srcIndex = 0;
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int y1 = x;
                int x1 = height - y - 1;
                int dstIndex = ((y1 * height) + x1)*channels;
                dst_rotated[dstIndex++] = src_data[srcIndex++];
                dst_rotated[dstIndex++] = src_data[srcIndex++];
                dst_rotated[dstIndex++] = src_data[srcIndex++];
                if(channels==4){
                    dst_rotated[dstIndex++] = src_data[srcIndex++];
                }
            }
        }
    }
    //适合三通道、4通道
    private static void argbRotate180(byte[] src_data,byte[] dst_rotated, int width, int height,int channels) {
        int srcIndex = 0;
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int y1 = width - x - 1;
                int x1 = height - y - 1;
                int dstIndex = ((y1 * height) + x1)*channels;
                dst_rotated[dstIndex++] = src_data[srcIndex++];
                dst_rotated[dstIndex++] = src_data[srcIndex++];
                dst_rotated[dstIndex++] = src_data[srcIndex++];
                if(channels==4){
                    dst_rotated[dstIndex++] = src_data[srcIndex++];
                }
            }
        }
    }
    //适合三通道、4通道
    private static void argbRotate270(byte[] src_data,byte[] dst_rotated, int width, int height,int channels) {
        int srcIndex = 0;
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int x1 = y;
                int y1 = width - x - 1;
                int dstIndex = ((y1 * height) + x1)*channels;
                dst_rotated[dstIndex++] = src_data[srcIndex++];
                dst_rotated[dstIndex++] = src_data[srcIndex++];
                dst_rotated[dstIndex++] = src_data[srcIndex++];
                if(channels==4){
                    dst_rotated[dstIndex++] = src_data[srcIndex++];
                }
            }
        }
    }

}
