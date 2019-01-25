package com.baoshen.cameralib.enums;

import android.hardware.camera2.CaptureRequest;

/**
 * Created by Shute on 2018/9/25.
 */
public enum AutoWhiteBalanceState implements IParameterIntValue {
    Unknown(-1),

    //切换模式后AWB的初始状态。当设备刚打开时，AWB必须处于这个状态。
    CONTROL_AWB_STATE_INACTIVE(CaptureRequest.CONTROL_AWB_STATE_INACTIVE),

    //AWB没有收敛到目标值，在改变颜色调整参数。
    CONTROL_AWB_STATE_SEARCHING(CaptureRequest.CONTROL_AWB_STATE_SEARCHING),

    //AWB为当前场景已经找到了理想的颜色调整值，这些参数不再改变。HAL层会自动离开该状态去寻找更好的解决方案。
    CONTROL_AWB_STATE_CONVERGED(CaptureRequest.CONTROL_AWB_STATE_CONVERGED),

    //使用AWB_LOCK锁住了AWB。颜色调整值不再改变。
    CONTROL_AWB_STATE_LOCKED(CaptureRequest.CONTROL_AWB_STATE_LOCKED);


    private AutoWhiteBalanceState(int code) {
        this.code = code;
    }

    private int code;

    public int getCode() {
        return code;
    }

    public static AutoWhiteBalanceState get(int code) {
        for (AutoWhiteBalanceState item : AutoWhiteBalanceState.values()) {
            if (item.code == code) {
                return item;
            }
        }
        return AutoWhiteBalanceState.Unknown;
    }
}
