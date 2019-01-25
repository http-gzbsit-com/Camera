package com.baoshen.cameralib.api1;

import android.graphics.Bitmap;
import android.graphics.ImageFormat;
import android.graphics.Rect;
import android.graphics.YuvImage;

import com.baoshen.cameralib.AbsCameraImage;
import com.baoshen.cameralib.CameraImageDecoder;
import com.baoshen.cameralib.CameraUtils;
import com.baoshen.cameralib.enums.ImageFormats;
import com.baoshen.common.graphics.Size;

public class CameraImageApi1 extends AbsCameraImage {

    public CameraImageApi1(int imageWidth,int imageHeight,ImageFormats imageFormat,int cameraRotation,boolean isFront) {
        super(imageWidth, imageHeight, imageFormat,cameraRotation,isFront);
        //todo 这里要判断下图片格式
        if (imageFormat == ImageFormats.NV21) {
            int yuv_bufferSize = imageWidth * imageHeight * ImageFormat.getBitsPerPixel(imageFormat.getCode()) / 8;
            mBuff = new byte[yuv_bufferSize];//
            mLength = yuv_bufferSize;
        } else {
            throw new UnsupportedOperationException();
        }
    }

    @Deprecated
    @Override
    public void setLength(int imageLength) {
        //do nothing
    }

    /**
     * @param crop 要裁减的区域(预览数据内的区域，而非UI区域)
     * @param screenOrientation 图片角度，一般保持跟屏幕一致，可选值：0、90、180、270，如果不需要特殊处理，传入-1
     * @author Shute
     * @time 2018/10/24 14:52
     */
    @Override
    public Bitmap getRgba(Rect crop,int screenOrientation) {
        Bitmap cropBitmap = CameraImageDecoder.toRgba(mBuff, mImageWidth, mImageHeight, crop);
        Bitmap outputBitmap;
        int displayOrientation = CameraUtils.getCameraDisplayOrientation(mCameraRotation, screenOrientation, mIsFront);
        if (displayOrientation != 0) {
            outputBitmap = CameraUtils.rotate(cropBitmap, mCameraRotation, screenOrientation, mIsFront);
            if(outputBitmap!=cropBitmap) {
                cropBitmap.recycle();
            }
        }
        else{
            outputBitmap = cropBitmap;
        }
        return outputBitmap;
    }

    /**
     * @param outputSize 用于输出byte[]代表的图片的大小
     * @param crop 要裁减的区域(预览数据内的区域，而非UI区域)
     * @param screenOrientation 图片角度，一般保持跟屏幕一致，可选值：0、90、180、270，如果不需要特殊处理，传入-1
     * @author Shute
     * @time 2018/10/24 14:52
     */
    @Override
    public byte[] getBgr(Rect crop, Size outputSize, int screenOrientation) {
        //todo 这里的crop为要裁减的预览数据的区域，即知道imageSize，区别只在于外部需要识别相机orientation
        byte[] buff = CameraImageDecoder.yuv420toBgr(mBuff, this.getImageWidth(), this.getImageHeight(), crop, outputSize);
        byte[] rotatedBuff;
        int displayOrientation = CameraUtils.getCameraDisplayOrientation(mCameraRotation, screenOrientation, mIsFront);
        if (displayOrientation != 0) {
            rotatedBuff = CameraImageDecoder.rotatedArgb(buff, outputSize.getWidth(),outputSize.getHeight(),displayOrientation,3);
            if (displayOrientation % 180 == 90) {
                int width = outputSize.getWidth();
                outputSize.setWidth(outputSize.getHeight());
                outputSize.setHeight(width);
            }
        } else {
            rotatedBuff = buff;
        }
        return rotatedBuff;
    }

    public byte[] toYuv420(Size outputSize,int screenOrientation) {
        int displayOrientation = CameraUtils.getCameraDisplayOrientation(mCameraRotation, screenOrientation, mIsFront);
        byte[] rotatedYuv420;
        //需要旋转
        if (displayOrientation != 0) {
            rotatedYuv420 = CameraImageDecoder.rotatedYuv420(mBuff, getImageWidth(), getImageHeight(), displayOrientation);
        } else {
            rotatedYuv420 = mBuff;
        }
        if (displayOrientation % 180 == 90) {
            outputSize.setHeight(getImageWidth());
            outputSize.setWidth(getImageHeight());
        } else {
            outputSize.setWidth(getImageWidth());
            outputSize.setHeight(getImageHeight());
        }
        return rotatedYuv420;
    }
}
