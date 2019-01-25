package com.baoshen.cameralib.enums;

import android.graphics.ImageFormat;

/**
 * Created by Shute on 2018/10/11.
 */
public enum ImageFormats implements IParameterIntValue {
    UNKNOWN(ImageFormat.UNKNOWN),
    DEPTH16(ImageFormat.DEPTH16),
    DEPTH_POINT_CLOUD(ImageFormat.DEPTH_POINT_CLOUD),
    FLEX_RGB_888(ImageFormat.FLEX_RGB_888),
    FLEX_RGBA_8888(ImageFormat.FLEX_RGBA_8888),
    JPEG(ImageFormat.JPEG),
    NV16(ImageFormat.NV16),
    NV21(ImageFormat.NV21),
    PRIVATE(ImageFormat.PRIVATE),
    RAW10(ImageFormat.RAW10),
    RAW12(ImageFormat.RAW12),
    RAW_PRIVATE(ImageFormat.RAW_PRIVATE),
    RAW_SENSOR(ImageFormat.RAW_SENSOR),
    RGB_565(ImageFormat.RGB_565),
    YUV_420_888(ImageFormat.YUV_420_888),
    YUV_422_888(ImageFormat.YUV_422_888),
    YUV_444_888(ImageFormat.YUV_444_888),
    YUY2(ImageFormat.YUY2),
    YV12(ImageFormat.YV12);

    private ImageFormats(int code) {
        this.code = code;
    }

    private int code;

    public int getCode() {
        return code;
    }

    public static ImageFormats get(int code) {
        for (ImageFormats item : ImageFormats.values()) {
            if (item.code == code) {
                return item;
            }
        }
        return ImageFormats.UNKNOWN;
    }
}
