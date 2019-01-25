package com.baoshen.cameralib.api2;

import com.baoshen.cameralib.AbsCameraImage;
import com.baoshen.cameralib.AbsCameraReader;
import com.baoshen.cameralib.enums.ImageFormats;

public class CameraReaderApi2 extends AbsCameraReader {
    public CameraReaderApi2(int imageWidth, int imageHeight, ImageFormats format, int maxImageCount,int cameraOrientation,boolean isFront){
        super(imageWidth,imageHeight,format,maxImageCount,cameraOrientation,isFront);
    }

    @Override
    protected AbsCameraImage create(){
        return new CameraImageApi2(mImageWidth,mImageHeight,mImageFormat,mCameraOrientation,mIsFront);
    }
}
