package com.baoshen.cameralib.enums;

import android.hardware.camera2.CaptureRequest;

/**
 * Created by Shute on 2018/9/25.
 */
public enum FlashMode implements IParameterStringValue {
    Unknown(-1,"unknown"),
    //关
    FLASH_MODE_OFF(CaptureRequest.FLASH_MODE_OFF,"off"),
    //闪一下
    FLASH_MODE_SINGLE(CaptureRequest.FLASH_MODE_SINGLE,""),
    //开
    FLASH_MODE_TORCH(CaptureRequest.FLASH_MODE_TORCH, "torch"),

    //API 1的
    FLASH_MODE_AUTO(100,"auto"),
    FLASH_MODE_ON(101,"on"),
    FLASH_MODE_RED_EYE(101,"red-eye");

    private FlashMode(int code,String value) {
        this.code = code;
        this.value = value;
    }
    private int code;//用于api2
    private String value;//用于api1

    public int getCode() {
        return code;
    }
    public String getValue(){return value;}

    public static FlashMode get(int code) {
        for (FlashMode item : FlashMode.values()) {
            if (item.code == code) {
                return item;
            }
        }
        return FlashMode.Unknown;
    }

    public static FlashMode get(String value) {
        for (FlashMode item : FlashMode.values()) {
            if (item.value.equals(value)) {
                return item;
            }
        }
        return FlashMode.Unknown;
    }
}
