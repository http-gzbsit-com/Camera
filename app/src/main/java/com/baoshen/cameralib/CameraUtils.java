package com.baoshen.cameralib;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.hardware.Camera;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraManager;
import android.os.Build;

import com.baoshen.cameralib.api1.CameraApi1;
import com.baoshen.cameralib.api2.CameraApi2;
import com.baoshen.common.Log;
import com.baoshen.common.graphics.Size;

import android.support.annotation.Keep;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Shute on 2018/9/21.
 */
@Keep
public class CameraUtils {
    public static boolean isSupperAdvanceCamera(Context context,boolean isFront) {
//        if(BuildConfig.DEBUG){
//            return false;
//        }
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            //低版本的，不支持Camera API 2
            return false;
        } else {
            float hardwareLevel = getCameraApi2Level(context, isFront);
//            CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL_EXTERNAL 外部摄像头，不确定要不要排除掉
            return hardwareLevel != CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL_LEGACY;

        }
    }

    //是否能支持最新的CameraAPI
    public static boolean isSupperAdvanceCamera(Context context,String cameraId) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            //低版本的，不支持Camera API 2
            return false;
        } else {
            float hardwareLevel = getCameraApi2Level(context, cameraId);
//            CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL_EXTERNAL 外部摄像头，不确定要不要排除掉
            return hardwareLevel != CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL_LEGACY;

        }
    }

    public static int getCameraApi2Level(Context context, String cameraId){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            CameraManager manager = getManager(context);
            try {
                CameraCharacteristics characteristics = manager.getCameraCharacteristics(cameraId);
                int hardwareLevel = characteristics.get(CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL);
                return hardwareLevel;
            }
            catch (CameraAccessException ex){
                Log.e(ex);
            }
        }
        return -1;
    }
    public static int getCameraApi2Level(Context context, boolean isFront) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            CameraManager manager = getManager(context);
            try {
                String[] ids = manager.getCameraIdList();
                if (ids != null && ids.length > 0) {
                    for (String cameraId : ids) {
                        CameraCharacteristics characteristics = manager.getCameraCharacteristics(cameraId);
                        Integer facing = characteristics.get(CameraCharacteristics.LENS_FACING);
                        if (facing != null) {
                            if (isFront) {
                                if (facing == CameraCharacteristics.LENS_FACING_FRONT) {
                                    return characteristics.get(CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL);
                                }
                            } else {
                                if (facing == CameraCharacteristics.LENS_FACING_BACK) {
                                    return characteristics.get(CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL);
                                }
                            }
                        }
                    }
                }
            } catch (CameraAccessException ex) {
                Log.e(ex);
            }
        }
        return -1;
    }

    public static synchronized AbsCamera createCamera(int level,boolean isFront,@NotNull Activity activity){
        if(level==1){
            return new CameraApi1(isFront,activity);
        }
        else{
            return new CameraApi2(isFront,activity);
        }
    }

    private static synchronized CameraManager getManager(Context context) {
        return (CameraManager) context.getSystemService(Context.CAMERA_SERVICE);
    }

    public static int findCameraId(boolean front) {
        int cameraCount;
        try {
            Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
            cameraCount = Camera.getNumberOfCameras();
            for (int camIdx = 0; camIdx < cameraCount; camIdx++) {
                Camera.getCameraInfo(camIdx, cameraInfo);
                int facing = front ? 1 : 0;
                if (cameraInfo.facing == facing) {
                    return camIdx;
                }
            }
        } catch (Exception ex) {
            Log.e(ex);
        }
        return -1;
    }

    public static Size getPreviewSize(List<Size> list, Size displaySize, int minSize,boolean biggerIsBetter) {
        //width :the short side;
        //height :the long side;
        int displayWidth = Math.min(displaySize.getWidth(), displaySize.getHeight());
        int displayHeight = Math.max(displaySize.getWidth(), displaySize.getHeight());
        float aspect = displayHeight/(float)displayWidth;
        int fixWidth = Math.min(displayWidth, minSize);
        int bestWidth = Math.max(displayWidth, minSize);
        int bestHeight = (int)Math.ceil(bestWidth*aspect);
        List<Integer> weights = new ArrayList<>(list.size());
        for (Size item : list) {
            int width = Math.min(item.getWidth(), item.getHeight());
            int height = Math.max(item.getWidth(), item.getHeight());
            int weight = 0;
            float sizeRate;
            if(biggerIsBetter){
                sizeRate = height / (float) bestHeight;
            }
            else {
                if (height > bestHeight) sizeRate = bestHeight / (float) height;
                else sizeRate = height / (float) bestHeight;
            }
            if (width >= bestWidth) {
                weight = 1000000;
            } else if (width >= fixWidth) {
                weight = 10000;
            } else {
                weight = 100;
            }
            float aspectRate;
            float sizeAspect = height/(float)width;
            if(sizeAspect>aspect){
                aspectRate = 1;
            }
            else{
                aspectRate = sizeAspect/aspect;
            }
            weight = (int) (weight * sizeRate * aspectRate);
            weights.add(weight);
        }
        int index = 0;
        int maxWeight = weights.get(0);
        for (int i = 1; i < weights.size(); i++) {
            int weight = weights.get(i);
            if (weight > maxWeight) {
                maxWeight = weight;
                index = i;
            }
        }
        return list.get(index);
    }

    public synchronized static void nv21Rotate(byte[] nv21_data,byte[] rotated ,int width, int height, int angel) {
        assert nv21_data!=null && rotated!=null && rotated.length == nv21_data.length;
        if (angel == 90) {
            nv21Rotate90(nv21_data, rotated, height,width);
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

    /**
     * @param imageSize 相机预览采用的尺寸
     * @param surfaceSize 预览所占用屏幕的尺寸
     * @param cameraOrientation 相机的方向(通常后置似乎90，前置是270)
     * @param screenOrientation 布局方向
     * @param frame 界面区域大小(如果不需要裁减,则frame的大小等于surfaceSize)
     * @return 返回相机预览裁减出来的对应的区域
     * @author Shute
     * @time 2018/10/24 14:10
     */
    public static Rect crop(Size imageSize,Size surfaceSize,int cameraOrientation,int screenOrientation,Rect frame,boolean isFront){
        int rotatedImageWidth,rotatedImageHeight;
        if(cameraOrientation%180 != screenOrientation%180){
            rotatedImageWidth = imageSize.getHeight();
            rotatedImageHeight = imageSize.getWidth();
        }
        else{
            rotatedImageWidth = imageSize.getWidth();
            rotatedImageHeight = imageSize.getHeight();
        }
        float widthScale = rotatedImageWidth/(float)surfaceSize.getWidth();
        float heightScale = rotatedImageHeight/(float)surfaceSize.getHeight();
        //先采用屏幕的方向来计算
        int left = Math.round(frame.left* widthScale);
        int right = Math.round(frame.right*widthScale);
        int top = Math.round(frame.top* heightScale);
        int bottom = Math.round(frame.bottom* heightScale);
        //屏幕方向跟相机方向的相差角度
//        int orientation = (cameraOrientation - screenOrientation +360)%360;
        int orientation = getCameraDisplayOrientation(cameraOrientation,screenOrientation,isFront);
        Rect rect;
        //然后还原相机的方向
//        switch (orientation) {
//            case 90:
//                rect = new Rect(top, imageSize.getHeight() - right, bottom, imageSize.getHeight() - left);
//                break;
//            case 180:
//                rect = new Rect(imageSize.getWidth() - right, imageSize.getHeight() - bottom, imageSize.getWidth() - left, imageSize.getHeight() - top);
//                break;
//            case 270:
//                rect = new Rect(imageSize.getWidth() - bottom,left, imageSize.getWidth() - top,right);
//                break;
//            default://0
//                rect = new Rect(left, top, right, bottom);
//                break;
//        }
        //todo 跟上边的注释代码做下比较，也许是上方的正确
        switch (orientation) {
            case 90:
                rect = new Rect(top, rotatedImageWidth - right, bottom, rotatedImageWidth - left);
                break;
            case 180:
                rect = new Rect(rotatedImageWidth - right, rotatedImageHeight - bottom, rotatedImageWidth - left, rotatedImageHeight - top);
                break;
            case 270:
                rect = new Rect(rotatedImageHeight - bottom,left, rotatedImageHeight - top,right);
                break;
            default://0
                rect = new Rect(left, top, right, bottom);
                break;
        }
        return rect;
    }

    //摄像头跟屏幕保持一致，需要用的角度
    public static int getCameraDisplayOrientation(int cameraOrientation, int screenOrientation, boolean isFront) {
        switch (screenOrientation) {
            case 0:
                screenOrientation = 90;
                break;
            case 90:
                screenOrientation = 0;
                break;
            case 180:
                screenOrientation = 270;
                break;
            case 270:
                screenOrientation = 180;
                break;
        }
        int orientation;
        if (isFront) {
            orientation = (cameraOrientation + screenOrientation + 90) % 360;
        } else {
            // back-facing
            orientation = (cameraOrientation + screenOrientation + 270) % 360;
        }
        //这是另外一个算法，但缺乏手机测试
//        int orientation = (360 + cameraOrientation - screenOrientation) % 360;
//        if (isFront) {
//            orientation = (orientation + 180) % 360;
//        }
        return orientation;
    }

    //旋转相机拍摄的图片
    public static Bitmap rotate(Bitmap source,int cameraOrientation,int screenOrientation,boolean isFront) {
        int displayOrientation = CameraUtils.getCameraDisplayOrientation(cameraOrientation, screenOrientation, isFront);
        Bitmap outputBitmap = source;
        //需要旋转
        if (displayOrientation != 0) {
            long time1=System.currentTimeMillis();
            Matrix matrix = new Matrix();
            // 缩放原图
            matrix.postScale(1f, 1f);
            // 向左旋转45度，参数为正则向右旋转
            matrix.postRotate(displayOrientation);
            outputBitmap = Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(), matrix, true);
            source.recycle();
            Log.i("rotate bitmap","Time:"+(System.currentTimeMillis()-time1));
        }
        return outputBitmap;
    }
}
