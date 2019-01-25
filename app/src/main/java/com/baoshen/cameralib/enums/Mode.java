package com.baoshen.cameralib.enums;

import android.hardware.camera2.CameraCharacteristics;

public enum Mode implements IParameterIntValue {

    UNKNOWN(-1),
    CONTROL_MODE_OFF(CameraCharacteristics.CONTROL_MODE_OFF),
    CONTROL_MODE_AUTO(CameraCharacteristics.CONTROL_MODE_AUTO),
    CONTROL_MODE_USE_SCENE_MODE(CameraCharacteristics.CONTROL_MODE_USE_SCENE_MODE),
    CONTROL_MODE_OFF_KEEP_STATE(CameraCharacteristics.CONTROL_MODE_OFF_KEEP_STATE);

    private Mode(int code){
        this.code = code;
    }
    private int code;

    public int getCode() {
        return code;
    }

    public static Mode get(int code) {
        for (Mode item : Mode.values()) {
            if (item.code == code) {
                return item;
            }
        }
        return Mode.UNKNOWN;
    }
}
