package com.baoshen.cameralib.enums;

import android.hardware.camera2.CaptureRequest;

/**
 * Created by Shute on 2018/10/9.
 */
public enum NoiseReductionMode implements IParameterIntValue {
    Unknown(-1),
    NOISE_REDUCTION_MODE_OFF(CaptureRequest.NOISE_REDUCTION_MODE_OFF),
     NOISE_REDUCTION_MODE_FAST(CaptureRequest.NOISE_REDUCTION_MODE_FAST),
     NOISE_REDUCTION_MODE_HIGH_QUALITY(CaptureRequest.NOISE_REDUCTION_MODE_HIGH_QUALITY),
     NOISE_REDUCTION_MODE_MINIMAL(CaptureRequest.NOISE_REDUCTION_MODE_MINIMAL),
     NOISE_REDUCTION_MODE_ZERO_SHUTTER_LAG(CaptureRequest.NOISE_REDUCTION_MODE_ZERO_SHUTTER_LAG);

    private NoiseReductionMode(int code) {
        this.code = code;
    }

    private int code;

    public int getCode() {
        return code;
    }

    public static NoiseReductionMode get(int code) {
        for (NoiseReductionMode item : NoiseReductionMode.values()) {
            if (item.code == code) {
                return item;
            }
        }
        return NoiseReductionMode.Unknown;
    }
}
