package com.baoshen.cameralib;

import android.graphics.Point;
import android.os.SystemClock;
import android.support.annotation.Keep;

import com.baoshen.cameralib.enums.CameraState;

import org.jetbrains.annotations.NotNull;

@Keep
public class FocusThread extends Thread{
    public FocusThread(@NotNull AbsCamera camera, long interval){
        mCamera = camera;
        this.setmInterval(interval);
    }
    public FocusThread(AbsCamera camera){
        this(camera,3000);
    }
    Point position;
    private long mInterval;
    AbsCamera mCamera;

    public void setFocusPosition(Point position){
        this.position = position;
    }

    public Point getFocusPosition(){
        return position;
    }

    public long getmInterval() {
        return mInterval;
    }

    public void setmInterval(long mInterval) {
        assert mInterval>0 :"参数取值范围不对";
        this.mInterval = mInterval;
    }
    @Override
    public void run(){
        while(!isInterrupted()){
            focus();
            SystemClock.sleep(mInterval);
        }
    }

    @Override
    public void interrupt(){
        mCamera = null;
        super.interrupt();
    }

    private void focus() {
        if (mCamera != null && CameraState.Running == mCamera.getState() && position != null) {
            mCamera.focus(position, false);
        }
    }
}
