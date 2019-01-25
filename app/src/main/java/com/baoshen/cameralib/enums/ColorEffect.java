package com.baoshen.cameralib.enums;

import android.hardware.camera2.CaptureRequest;

import com.baoshen.common.Log;

/**
 * Created by Shute on 2018/10/10.
 */
public enum  ColorEffect implements IParameterStringValue {
    Unknown(-1,"unknown"),
    CONTROL_EFFECT_MODE_OFF(CaptureRequest.CONTROL_EFFECT_MODE_OFF,"none"),

    //纯色(只有一种颜色,通常是有灰度的)
     CONTROL_EFFECT_MODE_MONO(CaptureRequest.CONTROL_EFFECT_MODE_MONO,"mono"),

     //反色
     CONTROL_EFFECT_MODE_NEGATIVE(CaptureRequest.CONTROL_EFFECT_MODE_NEGATIVE,"negative"),

      //整体、部分颠倒的(是反色的意思吗？)
     CONTROL_EFFECT_MODE_SOLARIZE(CaptureRequest.CONTROL_EFFECT_MODE_SOLARIZE,"solarize"),

     //处理为暖的灰色、红色、棕色
     CONTROL_EFFECT_MODE_SEPIA(CaptureRequest.CONTROL_EFFECT_MODE_SEPIA,"sepia"),

    //不同的音域，而不是连续的音阶
     CONTROL_EFFECT_MODE_POSTERIZE(CaptureRequest.CONTROL_EFFECT_MODE_POSTERIZE,"posterize"),

    //白色，但有黑色跟灰色的细节
     CONTROL_EFFECT_MODE_WHITEBOARD(CaptureRequest.CONTROL_EFFECT_MODE_WHITEBOARD, "whiteboard"),

    //黑色，但有白色跟灰色的细节
     CONTROL_EFFECT_MODE_BLACKBOARD(CaptureRequest.CONTROL_EFFECT_MODE_BLACKBOARD,"blackboard"),

    //有附加的蓝色
     CONTROL_EFFECT_MODE_AQUA(CaptureRequest.CONTROL_EFFECT_MODE_AQUA,"aqua"),

    //以下是API 1 特有
    CONTROL_EFFECT_MODE_SKETCH(100,"sketch"),
    CONTROL_EFFECT_MODE_NEON(101,"neon");

    private ColorEffect(int code,String value) {
        this.code = code;
        this.value = value;
    }
    private int code;//用于api2
    private String value;//用于api1

    public int getCode() {
        return code;
    }
    public String getValue(){return value;}

    public static ColorEffect get(int code) {
        for (ColorEffect item : ColorEffect.values()) {
            if (item.code == code) {
                return item;
            }
        }
        return ColorEffect.Unknown;
    }

    public static ColorEffect get(String value) {
        for (ColorEffect item : ColorEffect.values()) {
            if (item.value.equals(value)) {
                return item;
            }
        }
        Log.e("ColorEffect","Unknown ColorEffect:"+value);
        return ColorEffect.Unknown;
    }
}
