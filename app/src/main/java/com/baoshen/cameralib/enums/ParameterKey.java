package com.baoshen.cameralib.enums;

import android.graphics.Rect;

import com.baoshen.common.graphics.Size;

/**
 * Created by Shute on 2018/9/21.
 */
public enum ParameterKey {

    Unknown("Unknown"),

    //特别定义的，表示参数版本
    Version("Version",Integer.class,ParameterValueSupportedMode.None),

    //API 1或者 API2
    ApiLevel("ApiLevel",Integer.class,ParameterValueSupportedMode.None),

    /**
     * 非常重要，整体控制3A(AE,AF,AWB)。设置为OFF时，将需要手动控制3A
     * 设置为AUTO时，将影响到个别android.control.*算法，比如afMode。
     * 设置为USE_SCENE_MODE时，大部分android.control.*算法将无效
     * 设置为OFF_KEEP_STATE时，通常等同于OFF
     */
    Mode("Mode", com.baoshen.cameralib.enums.Mode.class,ParameterValueSupportedMode.List),

    //减少由于JPEG压缩导致的颜色渐变的效果
    AntiBanding("Anti-Banding"),

    //孔径大小
    Aperture("Aperture",Float.class,ParameterValueSupportedMode.List),

    //白平衡锁
    AutoExposureLock("AutoExposureLock"),

    //自动曝光
    AutoExposureMode("AutoExposureMode",com.baoshen.cameralib.enums.AutoExposureMode.class,ParameterValueSupportedMode.List),

    //曝光区域数量
    AutoExposureRegion("AutoExposureRegion",Integer.class,ParameterValueSupportedMode.Range),

    //手动聚焦
    AutoFocusTrigger("AutoFocusTrigger"),

    //自动聚焦
    AutoFocusMode("AutoFocusMode",com.baoshen.cameralib.enums.AutoFocusMode.class,ParameterValueSupportedMode.List),

    //聚焦区域数量
    AutoFocusRegion("AutoFocusRegion",Integer.class,ParameterValueSupportedMode.Range),

    //测光区域(API 1 特有，是否跟曝光区域有关的)
    AutoMeteringRegion("AutoMeteringRegion",Integer.class,ParameterValueSupportedMode.Range),

    //白平衡锁
    AutoWhiteBalanceLock("AutoWhiteBalanceLock"),

    //自动白平衡
    AutoWhiteBalanceMode("AutoWhiteBalanceMode",com.baoshen.cameralib.enums.AutoWhiteBalanceMode.class,ParameterValueSupportedMode.List),

    //自动白平衡区域数量
    AutoWhiteBalanceRegion("AutoWhiteBalanceRegion",Integer.class,ParameterValueSupportedMode.Range),

//    //白色平衡状态
//    AutoWhiteBalanceState("AutoWhiteBalanceState"),

    //色差
    ColorCorrectionAvailableAberrationMode("ColorCorrectionAvailableAberrationMode",
            com.baoshen.cameralib.enums.ColorCorrectionAvailableAberrationMode.class,ParameterValueSupportedMode.List),

    //对被捕获的图像应用一种颜色效果，如黑色和白色，深褐色
    ColorEffect("ColorEffect",com.baoshen.cameralib.enums.ColorEffect.class,ParameterValueSupportedMode.List),

    //曝光补偿
    ExposureCompensation("ExposureCompensation",Integer.class,ParameterValueSupportedMode.Range),

    //停止或启动自动曝光调整
    ExposureLock("ExposureLock"),

    //曝光时间
    ExposureTime("ExposureTime",Long.class,ParameterValueSupportedMode.Range),

    //在图片中识别人脸并将其用于焦点、计量和白平衡
    FaceDetection("FaceDetection",Integer.class,ParameterValueSupportedMode.Range),

    //闪光灯：打开，关闭，或使用自动设置
    FlashMode("FlashMode",com.baoshen.cameralib.enums.FlashMode.class,ParameterValueSupportedMode.List),

    //在图像中设置一个或多个区域以供焦点使用
    FocusAreas("FocusAreas",Integer.class,ParameterValueSupportedMode.Range),

    //报告在相机和看起来焦点的物体之间的距离
    FocusDistance("FocusDistance",Float.class,ParameterValueSupportedMode.Range),

    //帧间隔
    FrameDuration("FrameDuration",Long.class,ParameterValueSupportedMode.Range),

    //包含或省略带有图像的地理位置数据
    GpsData("GpsData"),

    //设置JPEG图像的压缩级别，这会增加或减少图像输出文件的质量和大小
    JpegQuality("JpegQuality",Integer.class,ParameterValueSupportedMode.Range),

    //在图像中指定一个或多个区域来计算白平衡
    MeteringAreas("MeteringAreas",Integer.class,ParameterValueSupportedMode.Range),


    //在设备上支持多个摄像头，包括前置和后置摄像头
    MultipleCameras("MultipleCameras",Integer.class,ParameterValueSupportedMode.Range),
    //噪音消除模式
    NoiseReductionMode("NoiseReductionMode",com.baoshen.cameralib.enums.NoiseReductionMode.class,ParameterValueSupportedMode.List),

    //拍照的旋转角度
    PhotoOrientatoin("PhotoOrientatoin",Integer.class,ParameterValueSupportedMode.List),

    PictureFormat("PictureFormat",ImageFormats.class,ParameterValueSupportedMode.List),

    //拍照的旋转方向
    PictureOrientatoin("PictureOrientatoin",Integer.class,ParameterValueSupportedMode.Range),

    //拍照尺寸
    PictureSize("PictureSize",Size.class,ParameterValueSupportedMode.List),

    //预览的区域(在整个预览中，扣出一个孔，用来预览，解码)
    PreviewBound("PreviewBound",Rect.class,null),

    PreviewFormat("PreviewFormat",ImageFormats.class,ParameterValueSupportedMode.List),

    //预览的旋转角度(屏幕方向，取值方位：0、90、180、270 。0°是正常竖屏，90°是设备向左倾倒；270°是向右倾倒)
    PreviewOrientation("PreviewOrientation",Integer.class,ParameterValueSupportedMode.List),

    //预览尺寸
    PreviewSize("PreviewSize",Size.class,ParameterValueSupportedMode.List),

    //场景模式：为特定类型的摄影场景应用一种预设模式，如夜、沙滩、雪或烛光场景
    SceneMode("SceneMode",com.baoshen.cameralib.enums.SceneMode.class,ParameterValueSupportedMode.List),

    //ISO 感光度
    Sensitivity("Sensitivity",Integer.class,ParameterValueSupportedMode.Range),

    //光学防抖 OIS
    StabilizationMode("StabilizationMode",com.baoshen.cameralib.enums.StabilizationMode.class,ParameterValueSupportedMode.List),

    //记录帧延迟的记录帧，以记录一个延时视频
    TimeLapseVideo("TimeLapseVideo"),

    //拍摄视频时拍张照片（帧抓取）
    VideoSnapshot("VideoSnapshot"),

    //视频防抖
    VideoStabilizationMode("VideoStabilizationMode",
            com.baoshen.cameralib.enums.VideoStabilizationMode.class,ParameterValueSupportedMode.List),

    //停止或启动自动白平衡调整
    WhiteBalanceLock("WhiteBalanceLock"),

    //缩放率:(100开始，上限不确定。100表示1倍)
    Zoom("Zoom",Integer.class,ParameterValueSupportedMode.Range);

    private String name;
    private Class valueType;
    private ParameterValueSupportedMode parameterValueSupportedMode;

    private ParameterKey(String name,Class valueType,ParameterValueSupportedMode valueMode){
        this.name = name;
        this.valueType = valueType;
        this.parameterValueSupportedMode = valueMode;
    }
    private ParameterKey(String name){
        this.name = name;
        this.valueType = null;
        this.parameterValueSupportedMode = null;
    }
    public String getName(){
        return name;
    }

    public Class getValueType(){return valueType;}

    public ParameterValueSupportedMode getParameterValueSupportedMode() {
        return parameterValueSupportedMode;
    }

    @Override
    public String toString(){
        return name;
    }

    public static ParameterKey get(String name) {
        for (ParameterKey item : ParameterKey.values()) {
            if (item.name.equals(name)) {
                return item;
            }
        }
        return ParameterKey.Unknown;
    }
}
