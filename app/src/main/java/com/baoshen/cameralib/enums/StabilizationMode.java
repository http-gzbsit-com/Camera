package com.baoshen.cameralib.enums;

import android.hardware.camera2.CaptureRequest;

/**
 * Created by Shute on 2018/10/8.
 */
public enum StabilizationMode implements IParameterIntValue {
    Unknown(-1),
    LENS_OPTICAL_STABILIZATION_MODE_OFF(CaptureRequest.LENS_OPTICAL_STABILIZATION_MODE_OFF),
    LENS_OPTICAL_STABILIZATION_MODE_ON(CaptureRequest.LENS_OPTICAL_STABILIZATION_MODE_ON);

    private StabilizationMode(int code){
        this.code = code;
    }
    private int code;

    public int getCode() {
        return code;
    }

    public static StabilizationMode get(int code) {
        for (StabilizationMode item : StabilizationMode.values()) {
            if (item.code == code) {
                return item;
            }
        }
        return StabilizationMode.Unknown;
    }
}
