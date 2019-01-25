package com.baoshen.cameralib.api1;

import com.baoshen.cameralib.AbsCameraImage;
import com.baoshen.cameralib.AbsCameraReader;
import com.baoshen.cameralib.enums.ImageFormats;

public class CameraReaderApi1 extends AbsCameraReader {

    public CameraReaderApi1(int imageWidth, int imageHeight, ImageFormats format, int maxImageCount,int cameraOrientation,boolean isFront){
        super(imageWidth,imageHeight,format,maxImageCount,cameraOrientation,isFront);
    }

    public AbsCameraImage find(byte[] buff) {
        AbsCameraImage image = null;
        for (int i = 0; i < mArray.length; i++) {
            AbsCameraImage item = mArray[i];
            if (item != null && item.getBuffer() == buff) {
                image = item;
                break;
            }
        }
        return image;
    }

    @Override
    protected AbsCameraImage create(){
        return new CameraImageApi1(mImageWidth,mImageHeight,mImageFormat,mCameraOrientation,mIsFront);
    }

}
