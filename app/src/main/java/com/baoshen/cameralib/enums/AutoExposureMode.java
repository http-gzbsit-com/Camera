package com.baoshen.cameralib.enums;

import android.hardware.camera2.CaptureRequest;

/**
 * Created by Shute on 2018/9/23.
 */
public enum AutoExposureMode implements IParameterIntValue {
    Unknown(-1),

    //关闭自动曝光；用户控制曝光，增益，帧周期和闪光灯
    CONTROL_AE_MODE_OFF(CaptureRequest.CONTROL_AE_MODE_OFF),

    //标准的自动聚焦，闪光灯关闭。用户设置闪光灯启动或者手电筒模式
    CONTROL_AE_MODE_ON(CaptureRequest.CONTROL_AE_MODE_ON),

    //标准自动曝光，开启闪光灯。HAL层精确控制捕获前和捕获静态图片时闪光。用户可控制闪光灯关闭。
    CONTROL_AE_MODE_ON_AUTO_FLASH(CaptureRequest.CONTROL_AE_MODE_ON_AUTO_FLASH),

    //标准自动曝光，拍照时闪光灯一直开启。HAL层精确控制捕获前闪光。用户可控制闪光灯关闭。
    CONTROL_AE_MODE_ON_ALWAYS_FLASH(CaptureRequest.CONTROL_AE_MODE_ON_ALWAYS_FLASH),

    //标准自动曝光。HAL层精确控制预闪和捕获静态图片时闪光。在前面捕获序列的最后一帧启动一次闪光灯，以减少后面图片中的红眼现象。用户可控制闪光灯关闭。
    CONTROL_AE_MODE_ON_AUTO_FLASH_REDEYE(CaptureRequest.CONTROL_AE_MODE_ON_AUTO_FLASH_REDEYE),

    //描述当前AE算法状态的动态元数据，HAL层在结果的元数据中报告该信息。
    CONTROL_AE_MODE_ON_EXTERNAL_FLASH(CaptureRequest.CONTROL_AE_MODE_ON_EXTERNAL_FLASH);

    private AutoExposureMode(int code) {
        this.code = code;
    }

    private int code;

    public int getCode() {
        return code;
    }

    public static AutoExposureMode get(int code) {
        for (AutoExposureMode item : AutoExposureMode.values()) {
            if (item.code == code) {
                return item;
            }
        }
        return AutoExposureMode.Unknown;
    }
}
