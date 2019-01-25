package com.baoshen.cameralib.enums;

import android.hardware.camera2.CaptureRequest;

import com.baoshen.common.Log;

/**
 * Created by Shute on 2018/9/23.
 */
public enum AutoFocusMode implements IParameterStringValue {
    Unknown(-1,"infinity"),
    //关闭自动聚焦
    CONTROL_AF_MODE_OFF(CaptureRequest.CONTROL_AF_MODE_OFF,"manual"),

    //自动聚焦
    CONTROL_AF_MODE_AUTO(CaptureRequest.CONTROL_AF_MODE_AUTO,"auto"),

    //微距自动聚焦
    CONTROL_AF_MODE_MACRO(CaptureRequest.CONTROL_AF_MODE_MACRO,"macro"),

    //似平滑的持续聚焦，用于视频录制。触发则立即在当前位置锁住焦点。取消而继续持续聚焦。
    CONTROL_AF_MODE_CONTINUOUS_VIDEO(CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_VIDEO,"continuous-video"),

    //快速持续聚焦，用于静态图片的ZSL捕获。一旦达到扫描目标，触发则立即锁住焦点。取消而继续持续聚焦。
    CONTROL_AF_MODE_CONTINUOUS_PICTURE(CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE,"continuous-picture"),

    //高级的景深聚焦。没有自动聚焦的浏览，触发和取消没有意义。通过HAL层控制图像的聚集。
    CONTROL_AF_MODE_EDOF(CaptureRequest.CONTROL_AF_MODE_EDOF,"edof"),

    //Focus固定的，不可调节的
    CONTROL_AF_MODE_FIXED(100,"fixed");

    private AutoFocusMode(int code,String value) {
        this.code = code;
        this.value = value;
    }
    private int code;//用于api2
    private String value;//用于api1

    public int getCode() {
        return code;
    }
    public String getValue(){return value;}

    public static AutoFocusMode get(int code) {
        for (AutoFocusMode item : AutoFocusMode.values()) {
            if (item.code == code) {
                return item;
            }
        }
        return AutoFocusMode.Unknown;
    }

    public static AutoFocusMode get(String value) {
        for (AutoFocusMode item : AutoFocusMode.values()) {
            if (item.value.equals(value)) {
                return item;
            }
        }
        Log.e("AutoFocusMode","Unknown AutoFocusMode:"+value);
        return AutoFocusMode.Unknown;
    }
}
