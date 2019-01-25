package com.baoshen.cameralib.serialization;

import android.support.annotation.Keep;

import com.baoshen.cameralib.ValueUtils;
import com.baoshen.cameralib.enums.AutoExposureMode;
import com.baoshen.cameralib.enums.AutoFocusMode;
import com.baoshen.cameralib.enums.FlashMode;
import com.baoshen.cameralib.enums.ImageFormats;
import com.baoshen.cameralib.enums.ParameterKey;
import com.baoshen.common.Log;
import com.baoshen.common.graphics.Size;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

/**
 * Created by Shute on 2018/10/26.
 */
@Keep
public abstract class AbsParameterSerialization {
    private static final String TAG = AbsParameterSerialization.class.getSimpleName();

    public abstract Map<ParameterKey, Object> deserialize(InputStream inputStream) throws IOException;

    public abstract Map<ParameterKey, Object> deserialize(byte[] data);

    public abstract void serialize(InputStream inputStream, Map<ParameterKey, Object> map);

    public abstract byte[] serialize(Map<ParameterKey, Object> map);

    protected Object stringToObject(ParameterKey key, String text) {
        try {
            switch (key) {
                case PreviewOrientation:
                case Zoom:
                case Version:
                case ApiLevel:
                    return Integer.valueOf(ValueUtils.toInt(text));
                case AutoExposureMode:{
                    return AutoExposureMode.get(ValueUtils.toInt(text));
                }
                case FlashMode:{
                    return FlashMode.get(text);
                }
                case PreviewFormat:
                    return ImageFormats.get(ValueUtils.toInt(text));
                case AutoFocusMode:
                    return AutoFocusMode.get(text);
                case PreviewSize:
                    return Size.valueOf(text);
                default: {
                    Log.w(TAG, "Unknown ParameterKey:" + key.toString(), Log.LEVEL_ERROR);
                    break;
                }
            }
        } catch (Exception ex) {
            Log.e(ex);
        }
        return null;
    }
}
