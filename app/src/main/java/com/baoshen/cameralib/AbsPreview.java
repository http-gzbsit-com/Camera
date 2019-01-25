package com.baoshen.cameralib;

import android.support.annotation.Keep;
import android.view.TextureView;
import com.baoshen.common.graphics.Size;

/**
 * Created by Shute on 2018/9/26.
 */
@Keep
public abstract class AbsPreview implements IRelease{

    public AbsPreview(){

    }

    protected OnPreviewFrameHandler mOnPreviewFrameHandler;
    protected boolean mIsRunning;
    protected TextureView mTextureView;
    protected Size mSurfaceSize;

    public abstract boolean start();

    public abstract boolean stop();

    public void setOnPreviewFrame(OnPreviewFrameHandler onPreviewFrameHandler){
        this.mOnPreviewFrameHandler = onPreviewFrameHandler;
    }
    public void setView(TextureView view,Size surfaceSize){
        //todo 这里应该是有类型要求的
        this.mTextureView = view;
        this.mSurfaceSize = surfaceSize;
    }

    public interface OnPreviewFrameHandler {
        void reader(AbsPreviewFrame frame);
    }
    public boolean isRunning(){
        return mIsRunning;
    }
}
