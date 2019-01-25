package com.baoshen.cameralib.enums;

import android.hardware.camera2.CaptureRequest;

import com.baoshen.common.Log;

/**
 * Created by Shute on 2018/9/25.
 */
public enum SceneMode implements IParameterStringValue {
    Unknown(-1, "unknown"),
    CONTROL_SCENE_MODE_DISABLED(CaptureRequest.CONTROL_SCENE_MODE_DISABLED, "auto"),
    CONTROL_SCENE_MODE_FACE_PRIORITY(CaptureRequest.CONTROL_SCENE_MODE_FACE_PRIORITY, "CONTROL_SCENE_MODE_FACE_PRIORITY"),//API 1不支持的

    //运动物体
    CONTROL_SCENE_MODE_ACTION(CaptureRequest.CONTROL_SCENE_MODE_ACTION, "action"),
    //肖像(拍人)
    CONTROL_SCENE_MODE_PORTRAIT(CaptureRequest.CONTROL_SCENE_MODE_PORTRAIT, "portrait"),
    //远景
    CONTROL_SCENE_MODE_LANDSCAPE(CaptureRequest.CONTROL_SCENE_MODE_LANDSCAPE, "landscape"),
    //夜景
    CONTROL_SCENE_MODE_NIGHT(CaptureRequest.CONTROL_SCENE_MODE_NIGHT, "night"),
    //夜间人物
    CONTROL_SCENE_MODE_NIGHT_PORTRAIT(CaptureRequest.CONTROL_SCENE_MODE_NIGHT_PORTRAIT, "night-portrait"),
    //剧场照片(具体意义暂不明确)Take photos in a theater. Flash light is off.
    CONTROL_SCENE_MODE_THEATRE(CaptureRequest.CONTROL_SCENE_MODE_THEATRE, "theatre"),
    //在沙滩拍照
    CONTROL_SCENE_MODE_BEACH(CaptureRequest.CONTROL_SCENE_MODE_BEACH, "beach"),
    //在雪地拍照
    CONTROL_SCENE_MODE_SNOW(CaptureRequest.CONTROL_SCENE_MODE_SNOW, "snow"),
    //日落
    CONTROL_SCENE_MODE_SUNSET(CaptureRequest.CONTROL_SCENE_MODE_SUNSET, "sunset"),
    //避免模糊(防抖)
    CONTROL_SCENE_MODE_STEADYPHOTO(CaptureRequest.CONTROL_SCENE_MODE_STEADYPHOTO, "steadyphoto"),
    // For shooting firework displays.
    CONTROL_SCENE_MODE_FIREWORKS(CaptureRequest.CONTROL_SCENE_MODE_FIREWORKS, "fireworks"),
    //拍摄高速物体(在API 1中等同于CONTROL_SCENE_MODE_ACTION)
    CONTROL_SCENE_MODE_SPORTS(CaptureRequest.CONTROL_SCENE_MODE_SPORTS, "sports"),
    //室内低光环境
    CONTROL_SCENE_MODE_PARTY(CaptureRequest.CONTROL_SCENE_MODE_PARTY, "party"),
    //烛光、暖光环境
    CONTROL_SCENE_MODE_CANDLELIGHT(CaptureRequest.CONTROL_SCENE_MODE_CANDLELIGHT, "candlelight"),
    //条码扫描
    CONTROL_SCENE_MODE_BARCODE(CaptureRequest.CONTROL_SCENE_MODE_BARCODE, "barcode"),
    CONTROL_SCENE_MODE_HIGH_SPEED_VIDEO(CaptureRequest.CONTROL_SCENE_MODE_HIGH_SPEED_VIDEO, "CONTROL_SCENE_MODE_HIGH_SPEED_VIDEO"),//API 1不支持的
    //HDR功能
    CONTROL_SCENE_MODE_HDR(CaptureRequest.CONTROL_SCENE_MODE_HDR, "hdr"),

    //以下是API 1 特有
    //似乎是背光
    CONTROL_SCENE_MODE_BACKLIGHT(100, "backlight"),
    CONTROL_SCENE_MODE_FLOWERS(101, "flowers"),
    CONTROL_SCENE_MODE_AR(102, "AR"),
    CONTROL_SCENE_MODE_HWAUTO(103, "hwauto"),
    CONTROL_SCENE_MODE_ASD(104, "asd");

    private SceneMode(int code, String value) {
        this.code = code;
        this.value = value;
    }

    private int code;//用于api2
    private String value;//用于api1

    public int getCode() {
        return code;
    }
    public String getValue(){return value;}

    public static SceneMode get(int code) {
        for (SceneMode item : SceneMode.values()) {
            if (item.code == code) {
                return item;
            }
        }
        return SceneMode.Unknown;
    }


    public static SceneMode get(String value) {
        for (SceneMode item : SceneMode.values()) {
            if (item.value.equals(value)) {
                return item;
            }
        }
        Log.e("SceneMode","Unknown SceneMode:"+value);
        return SceneMode.Unknown;
    }
}
