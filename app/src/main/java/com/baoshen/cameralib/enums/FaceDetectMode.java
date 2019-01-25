package com.baoshen.cameralib.enums;

import android.hardware.camera2.CaptureRequest;

/**
 * Created by Shute on 2018/9/25.
 */
public enum FaceDetectMode implements IParameterIntValue {
    Unknown(-1),
    STATISTICS_FACE_DETECT_MODE_OFF(CaptureRequest.STATISTICS_FACE_DETECT_MODE_OFF),
    STATISTICS_FACE_DETECT_MODE_SIMPLE(CaptureRequest.STATISTICS_FACE_DETECT_MODE_SIMPLE),
    STATISTICS_FACE_DETECT_MODE_FULL(CaptureRequest.STATISTICS_FACE_DETECT_MODE_FULL);

    private FaceDetectMode(int code) {
        this.code = code;
    }

    private int code;

    public int getCode() {
        return code;
    }

    public static FaceDetectMode get(int code) {
        for (FaceDetectMode item : FaceDetectMode.values()) {
            if (item.code == code) {
                return item;
            }
        }
        return FaceDetectMode.Unknown;
    }
}
