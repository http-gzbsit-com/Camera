package com.baoshen.cameralib.enums;

import android.hardware.camera2.CaptureRequest;

/**
 * Created by Shute on 2018/9/25.
 */
public enum VideoStabilizationMode implements IParameterIntValue {
    Unknown(-1),
    CONTROL_VIDEO_STABILIZATION_MODE_OFF(CaptureRequest.CONTROL_VIDEO_STABILIZATION_MODE_OFF),
    CONTROL_VIDEO_STABILIZATION_MODE_ON(CaptureRequest.CONTROL_VIDEO_STABILIZATION_MODE_ON);

    private VideoStabilizationMode(int code){
        this.code = code;
    }
    private int code;

    public int getCode() {
        return code;
    }

    public static VideoStabilizationMode get(int code) {
        for (VideoStabilizationMode item : VideoStabilizationMode.values()) {
            if (item.code == code) {
                return item;
            }
        }
        return VideoStabilizationMode.Unknown;
    }
}
