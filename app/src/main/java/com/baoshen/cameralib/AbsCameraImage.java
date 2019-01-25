package com.baoshen.cameralib;

import android.graphics.Bitmap;
import android.graphics.Rect;
import android.support.annotation.CallSuper;
import android.support.annotation.Keep;

import com.baoshen.cameralib.enums.ImageFormats;
import com.baoshen.common.Log;
import com.baoshen.common.graphics.Size;

import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

@Keep
public abstract class AbsCameraImage {
    public AbsCameraImage(int imageWidth, int imageHeight, ImageFormats imageFormat, int cameraRotation, boolean isFront) {
        mImageWidth = imageWidth;
        mImageHeight = imageHeight;
        mImageFormat = imageFormat;
        mSemaphore = new Semaphore(1);
        mCameraRotation = cameraRotation;
        mIsFront = isFront;
    }

    protected static final long SemaphoreTimeout = 3000;//3秒
    protected int mImageWidth;
    protected int mImageHeight;
    protected ImageFormats mImageFormat;
    protected Semaphore mSemaphore;
    protected int mCameraRotation;
    protected byte[] mBuff;
    protected int mLength;//实际使用长度
    protected boolean mIsFront;
    protected ImageCacheState mState = ImageCacheState.None;

    public boolean isAvailable() {
        return mSemaphore.availablePermits() > 0;
    }

    public int getCameraRotation() {
        return mCameraRotation;
    }

    public int getImageWidth() {
        return mImageWidth;
    }

    public int getImageHeight() {
        return mImageHeight;
    }

    @CallSuper
    //捕获(锁住)图片对象
    public void acquire() {
        try {
            mSemaphore.tryAcquire(SemaphoreTimeout, TimeUnit.MILLISECONDS);
        } catch (InterruptedException ex) {
            Log.e(ex);
        }
    }

    @CallSuper
    //释放(解锁)图片对象
    public void release() {
        mSemaphore.release();
    }

    @CallSuper
    public void close() {
        mSemaphore.release();
        mState = ImageCacheState.None;
    }

    public byte[] getBuffer() {
        return mBuff;
    }

    /**
     * @param crop 要裁减的区域(预览数据内的区域，而非UI区域)
     * @param screenOrientation 图片角度，一般保持跟屏幕一致，可选值：0、90、180、270，如果不需要特殊处理，传入-1
     * @author Shute
     * @time 2018/10/24 14:52
     */
    public abstract Bitmap getRgba(Rect crop, int screenOrientation);

    /**
     * @param crop 要裁减的区域(预览数据内的区域，而非UI区域)
     * @param screenOrientation 图片角度，一般保持跟屏幕一致，可选值：0、90、180、270，如果不需要特殊处理，传入-1
     * @author Shute
     * @time 2018/10/24 14:52
     */
    public int[] getRgbaPixels(Rect crop,int screenOrientation,Size outputSize) {
        Bitmap outputBitmap = getRgba(crop,screenOrientation);
        int[] pixels = CameraImageDecoder.bitmap2Argb(outputBitmap, outputBitmap.getWidth(), outputBitmap.getHeight());
        if(outputSize!=null){
            outputSize.setHeight(outputBitmap.getHeight());
            outputSize.setWidth(outputBitmap.getWidth());
        }
        outputBitmap.recycle();
        return pixels;
    }

    /**
     * @param outputSize        用于输出byte[]代表的图片的大小
     * @param crop              要裁减的区域(预览数据内的区域，而非UI区域)
     * @param screenOrientation 图片角度，一般保持跟屏幕一致，可选值：0、90、180、270，如果不需要特殊处理，传入-1
     * @author Shute
     * @time 2018/10/24 14:52
     */
    public abstract byte[] getBgr(Rect crop, Size outputSize, int screenOrientation);

    public int getLength() {
        return mLength;
    }

    public void setLength(int length) {
        mLength = length;
        if (mBuff == null || mBuff.length < length) {
            mBuff = new byte[mLength / 10 * 12];//放大到1.2倍
        }
    }

    public ImageCacheState getState(){
        return mState;
    }
    public void setState(ImageCacheState newState){
        mState = newState;
    }
    public enum ImageCacheState{
        //未使用
        None,
        //正在写
        Writing,
        //写完了
        Written,
        //正在读
        Reading,
    }
}
