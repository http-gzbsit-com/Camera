package com.baoshen.cameralib.api2;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.nfc.Tag;

import com.baoshen.cameralib.AbsCameraImage;
import com.baoshen.cameralib.CameraImageDecoder;
import com.baoshen.cameralib.CameraUtils;
import com.baoshen.cameralib.enums.ImageFormats;
import com.baoshen.common.Log;
import com.baoshen.common.graphics.Size;

import org.jetbrains.annotations.NotNull;

import java.io.ByteArrayOutputStream;

public class CameraImageApi2 extends AbsCameraImage {

    public CameraImageApi2(int imageWidth, int imageHeight, @NotNull ImageFormats imageFormat, int cameraRotation, boolean isFront) {
        super(imageWidth, imageHeight, imageFormat, cameraRotation, isFront);
    }

    private static final String KEY_YUV = "CameraImageApi2_YUV";
    private static final String KEY_BGR = "CameraImageApi2_BGR";

    /**
     * @param crop              要裁减的区域(预览数据内的区域，而非UI区域)
     * @param screenOrientation 图片角度，一般保持跟屏幕一致，可选值：0、90、180、270，如果不需要特殊处理，传入-1
     * @author Shute
     * @time 2018/10/24 14:52
     */
    @Override
    public Bitmap getRgba(Rect crop, int screenOrientation) {
        Bitmap bitmap = BitmapFactory.decodeByteArray(mBuff, 0, getLength());
        Bitmap cropBitmap;
        if (crop != null) {
            cropBitmap = Bitmap.createBitmap(bitmap, crop.left, crop.top, crop.width(), crop.height());
            bitmap.recycle();
        } else {
            cropBitmap = bitmap;
        }
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        cropBitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        Bitmap outputBitmap;
        int displayOrientation = CameraUtils.getCameraDisplayOrientation(mCameraRotation, screenOrientation, mIsFront);
        if (displayOrientation != 0) {
            outputBitmap = CameraUtils.rotate(cropBitmap, mCameraRotation, screenOrientation, mIsFront);
            if (cropBitmap != cropBitmap) {
                cropBitmap.recycle();
            }
        } else {
            outputBitmap = cropBitmap;
        }
        return outputBitmap;
    }

    /**
     * @param crop              要裁减的区域(预览数据内的区域，而非UI区域)
     * @param outputSize        用于输出byte[]代表的图片的大小
     * @param screenOrientation 图片角度，一般保持跟屏幕一致，可选值：0、90、180、270，如果不需要特殊处理，传入-1
     * @author Shute
     * @time 2018/10/24 14:52
     */
    @Override
    public byte[] getBgr(Rect crop, Size outputSize, int screenOrientation) {
        //todo 这里的crop为要裁减的预览数据的区域，即知道imageSize，区别只在于外部需要识别相机orientation
        Bitmap bitmap = BitmapFactory.decodeByteArray(mBuff, 0, getLength());
        Bitmap cropBitmap;
        if (crop == null) {
            cropBitmap = bitmap;
        } else {
            cropBitmap = Bitmap.createBitmap(bitmap, crop.left, crop.top, crop.width(), crop.height());
            bitmap.recycle();
        }
        Bitmap outputBitmap;
        int displayOrientation = CameraUtils.getCameraDisplayOrientation(mCameraRotation, screenOrientation, mIsFront);
        if (displayOrientation != 0) {
            outputBitmap = CameraUtils.rotate(cropBitmap, mCameraRotation, screenOrientation, mIsFront);
            if (outputBitmap != cropBitmap) {
                cropBitmap.recycle();
            }
        } else {
            outputBitmap = cropBitmap;
        }
        //输出裁减过、旋转后的尺寸
        outputSize.setWidth(outputBitmap.getWidth());
        outputSize.setHeight(outputBitmap.getHeight());
        int[] argb = CameraImageDecoder.bitmap2Argb(outputBitmap, outputSize.getWidth(), outputSize.getHeight());
        byte[] brg = CameraImageDecoder.argb2bgr(argb, outputSize.getWidth(), outputSize.getHeight());
        outputBitmap.recycle();
        return brg;
    }

    public byte[] toYuv420(Size outputSize, int screenOrientation) {
        Bitmap bitmap = BitmapFactory.decodeByteArray(mBuff, 0, getLength());
        int[] pixels = CameraImageDecoder.bitmap2Argb(bitmap, mImageWidth, mImageHeight);

        byte[] yuv420 = CameraImageDecoder.rgb2YCbCr420(pixels, mImageWidth, mImageHeight);
        int displayOrientation = CameraUtils.getCameraDisplayOrientation(mCameraRotation, screenOrientation, mIsFront);
        byte[] data;
        if (displayOrientation != 0) {
            data = CameraImageDecoder.rotatedYuv420(yuv420, getImageWidth(), getImageHeight(), displayOrientation);
        } else {
            data = yuv420;
        }
        if (displayOrientation % 180 == 90) {
            outputSize.setHeight(getImageWidth());
            outputSize.setWidth(getImageHeight());
        } else {
            outputSize.setWidth(getImageWidth());
            outputSize.setHeight(getImageHeight());
        }
        bitmap.recycle();
        return data;
    }

    @Override
    public void close() {
        super.close();
        mBuff = null;
    }
}
