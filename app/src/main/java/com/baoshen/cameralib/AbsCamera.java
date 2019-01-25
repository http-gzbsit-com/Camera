package com.baoshen.cameralib;

import android.app.Activity;
import android.content.Context;
import android.graphics.Point;
import android.graphics.Rect;
import android.support.annotation.CallSuper;
import android.support.annotation.Keep;
import android.view.TextureView;
import android.view.View;

import com.baoshen.cameralib.enums.AutoFocusCaptureState;
import com.baoshen.cameralib.enums.AutoFocusMode;
import com.baoshen.cameralib.enums.AutoFocusState;
import com.baoshen.cameralib.enums.CameraState;
import com.baoshen.cameralib.enums.ImageFormats;
import com.baoshen.cameralib.enums.ParameterKey;
import com.baoshen.common.Log;
import com.baoshen.common.graphics.Size;

import org.jetbrains.annotations.NotNull;
import org.w3c.dom.Text;

import java.util.List;
import java.util.concurrent.Semaphore;

/**
 * Created by Shute on 2018/9/26.
 */
@Keep
public abstract class AbsCamera implements IRelease{
    public AbsCamera(){}

    public AbsCamera(boolean isFront , @NotNull Activity context){
        this();
        this.mIsFront = isFront;
        this.mCameraId = null;
        this.mContext = context;
    }
    public AbsCamera(@NotNull String cameraId, @NotNull Activity context) {
        this.mCameraId = cameraId;
        this.mContext = context;
    }
    static protected Semaphore mCameraInitCloseLock = new Semaphore(1);//防止AbsCamera实例调用init多次
    protected String mCameraId;
    protected Activity mContext;
    private CameraState mState = CameraState.None;//这个字段保持私有
    protected boolean mIsFront;
    protected ICameraListener mListener;
    protected Size mPreviewSize;//相机预览尺寸
    protected ImageFormats mPreviewFormat;//预览格式
    protected boolean mIsAutoFocus;//是否开启了启动聚焦(手动聚焦时，需要临时关闭)
    protected AutoFocusCaptureState mAutoFocusCaptureState = AutoFocusCaptureState.None;
    protected AbsCameraReader mCameraReader;
    @NotNull
    protected AutoFitTextureView mTextureView;

    public abstract AbsCameraParameter getParameters() ;

    public CameraState getState(){
        return mState;
    }
    public abstract  int getApiLevel();
    public String getCameraId(){return mCameraId;}
    /**
     * 打开摄像头
     * 注：打开摄像头之前，应该先init
     */
    public abstract boolean open();
    public abstract boolean close();

    protected boolean isAutoFocus(){
        return mIsAutoFocus;
    }

    /**
     * 聚焦
     * @param  position 聚焦的区域
     * @param  manual 是否人工点击的
     * @return
     */
    @CallSuper
    public boolean focus(@NotNull Point position, boolean manual) {
        if(CameraState.Running != mState){
            return false;
        }
        if(!mTextureView.isAvailable()){
            Log.e(getLogTagName(),"无法聚焦，原因:TextureView未呈现出来");
            return false;
        }
//        if(mAutoFocusCaptureState == AutoFocusCaptureState.Running) {
//            Log.i(getLogTagName(), "无法聚焦，原因:上次聚焦未完成");
//            return false;
//        }

//        AbsCameraParameter parameters =  getParameters();
//        if(parameters==null) {
//            return false;
//        }
//        if(!parameters.isSupported(ParameterKey.AutoFocusMode)){
//            return false;
//        }
//        List<AutoFocusMode> afModes = parameters.getSupportedList(ParameterKey.AutoFocusMode);
//        if(afModes ==null || !afModes.contains(AutoFocusMode.CONTROL_AF_MODE_AUTO)){
//            return false;
//        }
        if(manual){
            Log.i(getLogTagName(),"manual focus:"+position.toString());
        }
        return true;
    }

    @CallSuper
    public boolean init(@NotNull AutoFitTextureView textureView, @NotNull ICameraListener listener) {
        mTextureView = textureView;
        mListener = listener;
        return true;
    }

    protected abstract String getLogTagName();

    public int getCameraOrientation() {
        AbsCameraParameter parameter = getParameters();
        int orientation = -1;
        if (parameter != null) {
            orientation = parameter.getCameraOrientation();
        }
        if (orientation == -1) {
            orientation = mIsFront ? 270 : 90;
        }
        return orientation;
    }

    //相机是否横着摆放的
    public boolean isLandscape(){
        int orientation = getCameraOrientation();
        return orientation == 90 || orientation == 270;
    }

    public boolean isFront(){
        return mIsFront;
    }

    @CallSuper
    protected void setState(CameraState newState) {
        if (newState != mState) {
            CameraState oldState = mState;
            Log.i("Set Camera State", String.format(" %s -> %s ", oldState, newState), Log.LEVEL_HIGH);
            mState = newState;
            if (mListener != null) {
                mListener.onStateChanged(this, oldState, newState);
            }
        } else {
            Log.i("Set Camera State", "(No change)", Log.LEVEL_HIGH);
        }
    }
}
