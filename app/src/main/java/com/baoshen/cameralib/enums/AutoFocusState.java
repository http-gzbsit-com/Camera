package com.baoshen.cameralib.enums;

import android.hardware.camera2.CaptureRequest;

/**
 * Created by Shute on 2018/9/23.
 */
public enum AutoFocusState implements IParameterIntValue {
    Unknown(-1),

    //不做聚焦，或者算法被重置。镜头不移动。这个状态总是用于MODE_OFF或者MODE_EDOF。当设备刚被打开时，必须处于这个状态。
    CONTROL_AF_STATE_INACTIVE(CaptureRequest.CONTROL_AF_STATE_INACTIVE),

    //一个持续聚焦的算法正在做扫描。镜头正在移动中。
    CONTROL_AF_STATE_PASSIVE_SCAN(CaptureRequest.CONTROL_AF_STATE_PASSIVE_SCAN),

    //一个持续聚焦的算法认为已经聚焦成功。镜头不在移动。HAL层会自动地离开这个状态。
    CONTROL_AF_STATE_PASSIVE_FOCUSED(CaptureRequest.CONTROL_AF_STATE_PASSIVE_FOCUSED),

    //一个持续聚焦的算法认为聚焦失败。镜头不在移动。HAL层会自动地离开这个状态。
    CONTROL_AF_STATE_PASSIVE_UNFOCUSED(CaptureRequest.CONTROL_AF_STATE_PASSIVE_UNFOCUSED),

    //用户触发的扫描正在进行中。
    CONTROL_AF_STATE_ACTIVE_SCAN(CaptureRequest.CONTROL_AF_STATE_ACTIVE_SCAN),

    //AF算法认为聚焦结束。镜头不再移动。
    CONTROL_AF_STATE_FOCUSED_LOCKED(CaptureRequest.CONTROL_AF_STATE_FOCUSED_LOCKED),

    //AF算法没能完成聚焦。镜头不再移动。
    CONTROL_AF_STATE_NOT_FOCUSED_LOCKED(CaptureRequest.CONTROL_AF_STATE_NOT_FOCUSED_LOCKED);

    private AutoFocusState(int code) {
        this.code = code;
    }
    private int code;

    public int getCode() {
        return code;
    }

    public static AutoFocusState get(int code) {
        for (AutoFocusState item : AutoFocusState.values()) {
            if (item.code == code) {
                return item;
            }
        }
        return AutoFocusState.Unknown;
    }
}
