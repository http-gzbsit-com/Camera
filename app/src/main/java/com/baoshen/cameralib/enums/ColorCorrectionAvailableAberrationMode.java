package com.baoshen.cameralib.enums;

import android.hardware.camera2.CaptureRequest;

/**
 * Created by Shute on 2018/9/25.
 */
public enum ColorCorrectionAvailableAberrationMode implements IParameterIntValue {
    Unknown(-1),
    COLOR_CORRECTION_ABERRATION_MODE_OFF(CaptureRequest.COLOR_CORRECTION_ABERRATION_MODE_OFF),
    COLOR_CORRECTION_ABERRATION_MODE_FAST(CaptureRequest.COLOR_CORRECTION_ABERRATION_MODE_FAST),
    COLOR_CORRECTION_ABERRATION_MODE_HIGH_QUALITY(CaptureRequest.COLOR_CORRECTION_ABERRATION_MODE_HIGH_QUALITY);

    private ColorCorrectionAvailableAberrationMode(int code) {
        this.code = code;
    }

    private int code;

    public int getCode() {
        return code;
    }

    public static ColorCorrectionAvailableAberrationMode get(int code) {
        for (ColorCorrectionAvailableAberrationMode item : ColorCorrectionAvailableAberrationMode.values()) {
            if (item.code == code) {
                return item;
            }
        }
        return ColorCorrectionAvailableAberrationMode.Unknown;
    }
}
