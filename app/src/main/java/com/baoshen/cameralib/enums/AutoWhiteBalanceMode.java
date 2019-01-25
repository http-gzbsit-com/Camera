package com.baoshen.cameralib.enums;

import android.hardware.camera2.CaptureRequest;

/**
 * Created by Shute on 2018/9/25.
 */
public enum AutoWhiteBalanceMode implements IParameterStringValue {
    Unknown(-1,"unknown"),

    //关闭自动白平衡。用户控制颜色矩阵。
    CONTROL_AWB_MODE_OFF(CaptureRequest.CONTROL_AWB_MODE_OFF,"null"),//api1没有这种

    //使能自动白平衡；3A(AF AE AWB)控制颜色转换，可能会使用比简单矩阵更复杂的转换。
    CONTROL_AWB_MODE_AUTO(CaptureRequest.CONTROL_AWB_MODE_AUTO,"auto"),

    //用于室内白炽灯的白平衡设置，色温大概2700K。
    CONTROL_AWB_MODE_INCANDESCENT(CaptureRequest.CONTROL_AWB_MODE_INCANDESCENT,"incandescent"),

    //用于荧光灯的白平衡设置，色温大概5000K。
    CONTROL_AWB_MODE_FLUORESCENT(CaptureRequest.CONTROL_AWB_MODE_FLUORESCENT,"fluorescent"),

    //用于荧光灯的白平衡设置，色温大概3000K。
    CONTROL_AWB_MODE_WARM_FLUORESCENT(CaptureRequest.CONTROL_AWB_MODE_WARM_FLUORESCENT,"warm-fluorescent"),

    //用于晴天的白平衡设置，色温大概5500K。
    CONTROL_AWB_MODE_DAYLIGHT(CaptureRequest.CONTROL_AWB_MODE_DAYLIGHT,"daylight"),

    //用于阴天的白平衡设置，色温大概6500K。
    CONTROL_AWB_MODE_CLOUDY_DAYLIGHT(CaptureRequest.CONTROL_AWB_MODE_CLOUDY_DAYLIGHT,"cloudy-daylight"),

    //用于日出/日落的白平衡设置，色温大概15000K。
    CONTROL_AWB_MODE_TWILIGHT(CaptureRequest.CONTROL_AWB_MODE_TWILIGHT,"twilight"),

    //用于阴影处的白平衡设置，色温大概7500K。
    CONTROL_AWB_MODE_SHADE(CaptureRequest.CONTROL_AWB_MODE_SHADE,"shade");


    private AutoWhiteBalanceMode(int code,String value) {
        this.code = code;
        this.value = value;
    }

    private int code;//用于api2
    private String value;//用于api1

    public int getCode() {
        return code;
    }
    public String getValue(){return value;}

    public static AutoWhiteBalanceMode get(int code) {
        for (AutoWhiteBalanceMode item : AutoWhiteBalanceMode.values()) {
            if (item.code == code) {
                return item;
            }
        }
        return AutoWhiteBalanceMode.Unknown;
    }

    public static AutoWhiteBalanceMode get(String value) {
        for (AutoWhiteBalanceMode item : AutoWhiteBalanceMode.values()) {
            if (item.value.equals(value)) {
                return item;
            }
        }
        return AutoWhiteBalanceMode.Unknown;
    }
}
