package com.baoshen.cameralib;

import android.support.annotation.Keep;

import com.baoshen.cameralib.enums.CameraState;
import com.baoshen.cameralib.enums.ParameterKey;

@Keep
public interface ICameraListener {
    void onPreviewFrame(AbsCamera camera,AbsCameraReader reader);

    void onStateChanged(AbsCamera camera, CameraState oldState, CameraState newState);

    void onParameterChanged(AbsCamera camera, ParameterKey key,Object value);
}
