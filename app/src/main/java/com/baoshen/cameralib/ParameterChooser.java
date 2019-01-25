package com.baoshen.cameralib;

import android.app.Activity;
import android.support.annotation.Keep;
import android.util.Range;

import com.baoshen.cameralib.enums.AutoExposureMode;
import com.baoshen.cameralib.enums.AutoFocusMode;
import com.baoshen.cameralib.enums.CameraState;
import com.baoshen.cameralib.enums.ImageFormats;
import com.baoshen.cameralib.enums.NoiseReductionMode;
import com.baoshen.cameralib.enums.ParameterKey;
import com.baoshen.common.DisplayUtils;

import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
/**
 * Created by Shute on 2018/10/25.
 */
@Keep
public class ParameterChooser {
    public Map<ParameterKey, Object> getBestParameters(@NotNull AbsCamera camera, @NotNull Activity context) {
        assert CameraState.None != camera.getState() &&
                CameraState.Close != camera.getState() &&
                CameraState.Release != camera.getState():
                "Camera is't initialized";

        //todo 这里是否要加入PreviewSize的处理？
        AbsCameraParameter parameters = camera.getParameters();
        Map<ParameterKey, Object> map = new HashMap<>();
        putMapSafely(map, ParameterKey.AutoFocusMode, getBestAutoFocus(parameters));
        putMapSafely(map, ParameterKey.AutoExposureMode, getBestAutoExposureMode(parameters));
        putMapSafely(map,ParameterKey.PreviewFormat,getBestPreviewFormat(camera));
        putMapSafely(map,ParameterKey.PreviewOrientation,getBestPreviewOrientation(context));
        putMapSafely(map,ParameterKey.Zoom,getBestZoom(parameters));

        return map;
    }

    static final AutoFocusMode[] AutoFocusModeCandidates = new AutoFocusMode[]{
            AutoFocusMode.CONTROL_AF_MODE_CONTINUOUS_PICTURE,
            AutoFocusMode.CONTROL_AF_MODE_CONTINUOUS_VIDEO
    };
    static final AutoExposureMode[] AutoExposureModeCandidates = new AutoExposureMode[]{
            AutoExposureMode.CONTROL_AE_MODE_ON
    };
    static final NoiseReductionMode[] NoiseReductionModeCandidates = new NoiseReductionMode[]{
            NoiseReductionMode.NOISE_REDUCTION_MODE_HIGH_QUALITY
    };

    Object getBestAutoFocus(AbsCameraParameter parameters) {
        ParameterKey key = ParameterKey.AutoFocusMode;
        Object value = null;
        if (parameters.isSupported(key)) {
            List<AutoFocusMode> modes = parameters.getSupportedList(key);
            value = getBestOne(modes, AutoFocusModeCandidates);
        }
        return value;
    }

    protected Object getBestAutoExposureMode(AbsCameraParameter parameters) {
        ParameterKey key = ParameterKey.AutoExposureMode;
        Object value = null;
        if (parameters.isSupported(key)) {
            List<AutoExposureMode> modes = parameters.getSupportedList(key);
            value = getBestOne(modes, AutoExposureModeCandidates);
        }
        return value;
    }
    protected Object getBest(AbsCameraParameter parameters){
        ParameterKey key = ParameterKey.NoiseReductionMode;
        Object value = null;
        if (parameters.isSupported(key)) {
            List<NoiseReductionMode> modes = parameters.getSupportedList(key);
            value = getBestOne(modes, NoiseReductionModeCandidates);
        }
        return value;
    }
    protected Object getBestPreviewFormat(AbsCamera camera) {
        if (camera.getApiLevel() == 1) {
            return ImageFormats.NV21;
        } else {
            return ImageFormats.JPEG;
        }
    }
    protected Object getBestPreviewOrientation(Activity context) {
        int screenOrientation = DisplayUtils.getScreenOrientation(context);
        return new Integer(screenOrientation);
    }
    protected Object getBestZoom(AbsCameraParameter parameters) {
        if (parameters.isSupported(ParameterKey.Zoom)) {
            Range<Integer> zoomRang = parameters.getSupportedRange(ParameterKey.Zoom);
            if (zoomRang.getUpper() > 500) {
                return 300;
            } else if (zoomRang.getUpper() >= 200) {
                return 200;
            } else {
                return zoomRang.getUpper();
            }
        }
        return 100;
    }

    /**
     * 从列表中，选出最佳的一个
     *
     * @param list       列表
     * @param candidates 候选列表，在前面的是最优先的
     * @author Shute
     * @time 2018/10/25 16:47
     */
    protected <T> Object getBestOne(List<T> list, T[] candidates) {
        assert candidates != null && candidates.length > 0 : "candidates can't be null";
        if (list == null || list.size() == 0) return null;
        for (T candidate : candidates) {
            if (list.contains(candidate)) {
                return candidate;
            }
        }
        return null;
    }

    protected <K, V> boolean putMapSafely(Map<K, V> map, K key, V value) {
        if (key == null) return false;
        if (value == null) return false;
        if (map == null) return false;
        map.put(key, value);
        return true;
    }
}
