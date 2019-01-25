package com.baoshen.cameralib;

import android.support.annotation.CallSuper;
import android.support.annotation.Keep;

import com.baoshen.cameralib.enums.ImageFormats;
import com.baoshen.common.Log;

import org.jetbrains.annotations.NotNull;

@Keep
public abstract class AbsCameraReader {
    //实际Images数量为n+1
    public AbsCameraReader(int imageWidth, int imageHeight, @NotNull ImageFormats format, int maxImageCount,int cameraOrientation,boolean isFront){
        assert maxImageCount>0:"最大可用数必须大于0";
        mImageWidth = imageWidth;
        mImageHeight = imageHeight;
        mImageFormat = format;
        mMaxCount = maxImageCount;
        mCameraOrientation = cameraOrientation;
        mArray= new AbsCameraImage[maxImageCount+1];
        mIsFront = isFront;
    }
    protected int mImageWidth;
    protected int mImageHeight;
    protected ImageFormats mImageFormat;
    protected int mMaxCount;
    protected int mCameraOrientation;
    protected AbsCameraImage[] mArray;
    protected boolean mIsFront;

    @CallSuper
    public AbsCameraImage acquireImageForWrite() {
        AbsCameraImage image = null;
        //先拿空闲的
        for (int i = 0; i < mArray.length; i++) {
            AbsCameraImage item = mArray[i];
            if (item == null) {
                image= create();
                mArray[i]  = image;
                break;
            }
            else{
                if(item.isAvailable() && AbsCameraImage.ImageCacheState.None == item.getState()){
                    image = item;
                    break;
                }
            }
        }
        //再拿正在写的
        if(image==null){
            for (int i = 0; i < mArray.length; i++) {
                AbsCameraImage item = mArray[i];
                if (item != null) {
                    if(item != null && item.isAvailable() && AbsCameraImage.ImageCacheState.Writing == item.getState()){
                        image = item;
                        break;
                    }
                }
            }
        }
        //最后用已经写完成的
        if(image==null){
            for (int i = 0; i < mArray.length; i++) {
                AbsCameraImage item = mArray[i];
                if (item != null) {
                    if(item != null && item.isAvailable() && AbsCameraImage.ImageCacheState.Written == item.getState()){
                        image = item;
                        break;
                    }
                }
            }
        }

        if(image == null) {
            //这种情况，不应该出现的
            if(BuildConfig.DEBUG) {
                //这种情况，不应该出现的
                StringBuffer sb = new StringBuffer();
                for (int i = 0; i < mArray.length; i++) {
                    AbsCameraImage item = mArray[i];
                    if (item != null) {
                        sb.append(String.format("[%d] ", i));
                        sb.append(String.format("Available:%s", String.valueOf(item.isAvailable())));
                        sb.append(String.format(",State:%s",String.valueOf(item.getState())));
                    }
                }
                Log.e(this.getClass().getSimpleName(), "无可用缓存(acquireImageForWrite),"+sb.toString());
            }
            else {
                Log.e(this.getClass().getSimpleName(), "无可用缓存(acquireImageForWrite)");
            }
            return null;
        }
        image.acquire();
        image.setState(AbsCameraImage.ImageCacheState.Writing);
        return image;
    }

    public AbsCameraImage acquireImageForRead() {
        //todo 这里是否会导致拿不到最新的，而导致延时
        AbsCameraImage image = null;
        for (int i = 0; i < mArray.length; i++) {
            AbsCameraImage item = mArray[i];
            if (item != null) {
                if (item.isAvailable() && AbsCameraImage.ImageCacheState.Written == item.getState()) {
                    image = item;
                    break;
                }
            }
        }
        if (image == null) {
            if(BuildConfig.DEBUG) {
                StringBuffer sb = new StringBuffer();
                for (int i = 0; i < mArray.length; i++) {
                    AbsCameraImage item = mArray[i];
                    if (item != null) {
                        sb.append(String.format("[%d] ", i));
                        sb.append(String.format("Available:%s", String.valueOf(item.isAvailable())));
                        sb.append(String.format(",State:%s",String.valueOf(item.getState())));
                    }
                }
                Log.w(this.getClass().getSimpleName(), "无可用缓存(acquireImageForRead),"+sb.toString());
            }
            else {
                Log.w(this.getClass().getSimpleName(), "无可用缓存(acquireImageForRead)");
            }
            return null;
        }
        image.acquire();
        image.setState(AbsCameraImage.ImageCacheState.Reading);
        return image;
    }

    public ImageFormats getImageFormat(){
        return mImageFormat;
    }

    public void close() {
        for (int i = 0; i < mArray.length; i++) {
            AbsCameraImage item = mArray[i];
            if (item != null) {
                item.close();
            }
        }
    }

    public int getCameraOrientation() {
        return mCameraOrientation;
    }

    protected abstract AbsCameraImage create();
}
