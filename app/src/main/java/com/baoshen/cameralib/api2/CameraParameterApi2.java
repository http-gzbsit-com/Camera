package com.baoshen.cameralib.api2;

import android.content.Context;
import android.graphics.ImageFormat;
import android.graphics.Rect;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.CaptureResult;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.util.Range;
import android.util.SparseIntArray;

import com.baoshen.cameralib.AbsCamera;
import com.baoshen.cameralib.AbsCameraParameter;
import com.baoshen.cameralib.BuildConfig;
import com.baoshen.cameralib.ICameraParameterSync;
import com.baoshen.cameralib.ValueUtils;
import com.baoshen.cameralib.enums.AutoExposureMode;
import com.baoshen.cameralib.enums.AutoFocusMode;
import com.baoshen.cameralib.enums.AutoWhiteBalanceMode;
import com.baoshen.cameralib.enums.ColorCorrectionAvailableAberrationMode;
import com.baoshen.cameralib.enums.ColorEffect;
import com.baoshen.cameralib.enums.FaceDetectMode;
import com.baoshen.cameralib.enums.FlashMode;
import com.baoshen.cameralib.enums.ImageFormats;
import com.baoshen.cameralib.enums.Mode;
import com.baoshen.cameralib.enums.NoiseReductionMode;
import com.baoshen.cameralib.enums.ParameterKey;
import com.baoshen.cameralib.enums.ParameterValueSupportedMode;
import com.baoshen.cameralib.enums.SceneMode;
import com.baoshen.cameralib.enums.VideoStabilizationMode;
import com.baoshen.common.Log;
import com.baoshen.common.graphics.Size;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by Shute on 2018/9/22.
 */
public class CameraParameterApi2 extends AbsCameraParameter {
    CameraParameterApi2(@NotNull Context context, @NotNull AbsCamera camera){
        super(context,camera);
    }
    private static final String TAG = CameraParameterApi2.class.getSimpleName();
    private static final SparseIntArray ORIENTATIONS = new SparseIntArray();
    private CameraCharacteristics mCharacteristics;//摄像头信息
    private ICameraParameterSyncApi2 mParameterSync;

    @Override
    public boolean init(@NotNull ICameraParameterSync sync){
        //todo 注意项：如果调用了Open，但是没执行到打开完成的回调，就要关闭摄像头，这种情况要特别处理一下
        assert mParameterSync instanceof CameraParameterApi2 : "Please use CameraParameterApi2";
        ICameraParameterSyncApi2 sync2 = (ICameraParameterSyncApi2)sync;
        mCameraId = mCamera.getCameraId();
        this.mParameterSync = sync2;
        mCharacteristics = mParameterSync.getCameraCharacteristics();
        mIsFront=super.mCamera.isFront();
        if(BuildConfig.DEBUG){
            for(CaptureRequest.Key key: mCharacteristics.getAvailableCaptureRequestKeys()){
                Log.i("Camera2 Available Capture Request Keys",key.toString(),Log.LEVEL_LOW);
            }
            for(CaptureResult.Key key: mCharacteristics.getAvailableCaptureResultKeys()){
                Log.i("Camera2 Available Capture Result Keys",key.toString(),Log.LEVEL_LOW);
            }
        }
        return true;
    }

    @Override
    public <T> List<T> getSupportedList(ParameterKey key) {
        assert ParameterValueSupportedMode.List != key.getParameterValueSupportedMode() :
                "getSupportedList require ParameterValueSupportedMode.List";

        CameraCharacteristics characteristics = mCharacteristics;
        if (mCharacteristics == null) {
            return null;
        }
        switch (key) {
            case Aperture: {
                float[] modes = characteristics.get(CameraCharacteristics.LENS_INFO_AVAILABLE_APERTURES);
                if (modes != null && modes.length > 0) {
                    List<Float> list = new ArrayList<>(modes.length);
                    for (float item : modes) {
                        list.add(item);
                    }
                    return (List<T>) list;
                }
                break;
            }
            case AutoExposureMode: {
                int[] modes = characteristics.get(CameraCharacteristics.CONTROL_AE_AVAILABLE_MODES);
                if (modes != null) {
                    List<AutoExposureMode> list = new ArrayList<>(modes.length);
                    for (int mode : modes) {
                        list.add(AutoExposureMode.get(mode));
                    }
                    return (List<T>) list;
                }
                break;
            }
            case AutoFocusMode: {
                int[] modes = characteristics.get(CameraCharacteristics.CONTROL_AF_AVAILABLE_MODES);
                if (modes != null) {
                    List<AutoFocusMode> list = new ArrayList<>(modes.length);
                    for (int mode : modes) {
                        list.add(AutoFocusMode.get(mode));
                    }
                    return (List<T>) list;
                }
                break;
            }
            case AutoWhiteBalanceMode: {
                int[] modes = characteristics.get(CameraCharacteristics.CONTROL_AWB_AVAILABLE_MODES);
                if (modes != null) {
                    List<AutoWhiteBalanceMode> list = new ArrayList<>(modes.length);
                    for (int mode : modes) {
                        list.add(AutoWhiteBalanceMode.get(mode));
                    }
                    return (List<T>) list;
                }
                break;
            }
            case ColorCorrectionAvailableAberrationMode: {
                int[] modes = characteristics.get(CameraCharacteristics.COLOR_CORRECTION_AVAILABLE_ABERRATION_MODES);
                if (modes != null && modes.length > 0) {
                    List<ColorCorrectionAvailableAberrationMode> list = new ArrayList<>(modes.length);
                    for (int mode : modes) {
                        list.add(ColorCorrectionAvailableAberrationMode.get(mode));
                    }
                    return (List<T>) list;
                }
                break;
            }
            case ColorEffect: {
                int[] modes = characteristics.get(CameraCharacteristics.CONTROL_AVAILABLE_EFFECTS);
                if (modes != null && modes.length > 0) {
                    List<ColorEffect> list = new ArrayList<>(modes.length);
                    for (int item : modes) {
                        list.add(ColorEffect.get(item));
                    }
                    return (List<T>) list;
                }
                break;
            }
            case FaceDetection: {
                int[] modes = characteristics.get(CameraCharacteristics.STATISTICS_INFO_AVAILABLE_FACE_DETECT_MODES);

                if (modes != null && modes.length > 0) {
                    List<FaceDetectMode> list = new ArrayList<>(modes.length);
                    for (int mode : modes) {
                        list.add(FaceDetectMode.get(mode));
                    }
                    return (List<T>) list;
                }
                break;
            }
            case FlashMode: {
                FlashMode[] modes = new FlashMode[] {
                        FlashMode.FLASH_MODE_OFF, FlashMode.FLASH_MODE_SINGLE, FlashMode.FLASH_MODE_TORCH
                } ;
                if (modes != null && modes.length > 0) {
                    List<FlashMode> list = new ArrayList<>(modes.length);
                    for (FlashMode item : modes) {
                        list.add(item);
                    }
                    return (List<T>) list;
                }
                break;
            }
            case FocusDistance: {
                float[] distances = characteristics.get(CameraCharacteristics.LENS_INFO_AVAILABLE_FOCAL_LENGTHS);
                if (distances != null && distances.length > 0) {
                    List<Float> list = new ArrayList<>(distances.length);
                    for (float item : distances) {
                        list.add(item);
                    }
                    return (List<T>) list;
                }
                break;
            }
            case Mode: {
                int[] modes = characteristics.get(CameraCharacteristics.CONTROL_AVAILABLE_MODES);
                if (modes != null && modes.length > 0) {
                    List<Mode> list = new ArrayList<>(modes.length);
                    for (int item : modes) {
                        list.add(Mode.get(item));
                    }
                    return (List<T>) list;
                }
                break;
            }
            case NoiseReductionMode: {
                int[] modes = characteristics.get(CameraCharacteristics.NOISE_REDUCTION_AVAILABLE_NOISE_REDUCTION_MODES);
                if (modes != null) {
                    List<NoiseReductionMode> list = new ArrayList<>(modes.length);
                    for (int mode : modes) {
                        list.add(NoiseReductionMode.get(mode));
                    }
                    return (List<T>) list;
                }
                break;
            }
            case PictureFormat: {
                //todo 搞清楚能支持哪些格式
                List<ImageFormats> formats = new ArrayList<>();
                formats.add(ImageFormats.JPEG);
                return (List<T>) formats;
            }
            case PictureSize: {
                android.util.Size[] sizes = getPictureSize();
                return (List<T>) toSizeList(sizes);
            }
            case PreviewFormat:{
                //todo 搞清楚能支持哪些格式
                List<ImageFormats> formats = new ArrayList<>();
                formats.add(ImageFormats.JPEG);
                return (List<T>) formats;
            }
            case PreviewSize: {
                android.util.Size[] sizes = getPreviewSize();
                return (List<T>) toSizeList(sizes);
            }
            case SceneMode: {
                int[] modes = characteristics.get(CameraCharacteristics.CONTROL_AVAILABLE_SCENE_MODES);
                if (modes != null && modes.length > 0) {
                    List<SceneMode> list = new ArrayList<>(modes.length);
                    for (int mode : modes) {
                        list.add(SceneMode.get(mode));
                    }
                    return (List<T>) list;
                }
                break;
            }
            case StabilizationMode: {
                int[] modes = characteristics.get(CameraCharacteristics.LENS_INFO_AVAILABLE_OPTICAL_STABILIZATION);
                if (modes != null && modes.length > 0) {
                    List<VideoStabilizationMode> list = new ArrayList<>(modes.length);
                    for (int mode : modes) {
                        list.add(VideoStabilizationMode.get(mode));
                    }
                    return (List<T>) list;
                }
                break;
            }
            case VideoStabilizationMode: {
                int[] modes = characteristics.get(CameraCharacteristics.CONTROL_AVAILABLE_VIDEO_STABILIZATION_MODES);
                if (modes != null && modes.length > 0) {
                    List<VideoStabilizationMode> list = new ArrayList<>(modes.length);
                    for (int mode : modes) {
                        list.add(VideoStabilizationMode.get(mode));
                    }
                    return (List<T>) list;
                }
                break;
            }
            default:
                throw new UnsupportedOperationException();
        }
        return null;
    }

    @Override
    public <T extends Comparable<? super T>> Range<T> getSupportedRange(ParameterKey key) {
        assert ParameterValueSupportedMode.Range != key.getParameterValueSupportedMode() :
                "getSupportedRange require ParameterValueSupportedMode.Range";

        if (mCharacteristics == null) {
            return null;
        }
        switch (key) {
            case AutoExposureRegion:{
                Integer count =mCharacteristics.get(CameraCharacteristics.CONTROL_MAX_REGIONS_AE);
                if(count>0){
                    return (Range<T>)new Range<Integer>(0,count);
                }
                break;
            }
            case AutoFocusRegion:{
                Integer count =mCharacteristics.get(CameraCharacteristics.CONTROL_MAX_REGIONS_AF);
                if(count>0){
                    return (Range<T>)new Range<Integer>(0,count);
                }
                break;
            }
            case AutoWhiteBalanceRegion:{
                Integer count =mCharacteristics.get(CameraCharacteristics.CONTROL_MAX_REGIONS_AWB);
                if(count>0){
                    return (Range<T>)new Range<Integer>(0,count);
                }
                break;
            }
            case ExposureCompensation: {
                Range<Integer> range = mCharacteristics.get(CameraCharacteristics.CONTROL_AE_COMPENSATION_RANGE);
                return (Range<T>) range;
//                break;
            }
            case ExposureTime: {
                Range<Long> range = mCharacteristics.get(CameraCharacteristics.SENSOR_INFO_EXPOSURE_TIME_RANGE);
                //单位是纳秒，转换为毫秒
                if (range != null) {
                    range = new Range(range.getLower() / 1000000, range.getUpper() / 1000000);
                }
                return (Range<T>) range;
//                break;
            }
            case FrameDuration: {
                //单位是什么?
                Long duration = mCharacteristics.get(CameraCharacteristics.SENSOR_INFO_MAX_FRAME_DURATION);
                if (duration != null && duration.longValue() >= 0L) {
                    //默认单位是ns，转换为毫秒
                    //最小值是100us(微妙，不足1毫秒)
                    Range<Long> range = new Range<>(0L, duration.longValue() / 1000000);
                    return (Range<T>) range;
                }
                break;
            }
            case FocusDistance: {
                Float minDistance = mCharacteristics.get(CameraCharacteristics.LENS_INFO_MINIMUM_FOCUS_DISTANCE);
                if (minDistance != null && Float.compare(minDistance.floatValue(), 0f) != 0) {
                    Range<Float> range = new Range<>(0f, minDistance);
                    return (Range<T>) range;
                }
                break;
            }
            case Sensitivity: {
                Range<Integer> range = mCharacteristics.get(CameraCharacteristics.SENSOR_INFO_SENSITIVITY_RANGE);
                if (range != null) {
                    return (Range<T>) range;
                }
                break;
            }
            case Zoom: {
                Float maxZoom = mCharacteristics.get(CameraCharacteristics.SCALER_AVAILABLE_MAX_DIGITAL_ZOOM);
                if (maxZoom != null) {
                    Range<Integer> range = new Range<>(100, (int) (maxZoom * 100));
                    return (Range<T>) range;
                }
                break;
            }
            default:
                throw new UnsupportedOperationException();
        }
        return null;
    }

    @Override
    public void release(){
        mParameterSync = null;
    }

    @Override
    protected boolean isSupportedProtected(ParameterKey key) {
        CameraCharacteristics characteristics = mCharacteristics;
        if (mCharacteristics == null) {
            return false;
        }
        switch (key) {
            case Aperture: {
                float[] modes = characteristics.get(CameraCharacteristics.LENS_INFO_AVAILABLE_APERTURES);
                if (modes != null && modes.length > 0) {
                    return true;
                }
                break;
            }
            case AutoFocusMode: {
                int[] afModes = characteristics.get(CameraCharacteristics.CONTROL_AF_AVAILABLE_MODES);
                if (afModes != null && afModes.length > 0) {
                    int offCode = AutoFocusMode.CONTROL_AF_MODE_OFF.getCode();
                    for (int mode : afModes) {
                        if (mode != offCode) {
                            return true;
                        }
                    }
                }
                break;
            }
            case AutoExposureLock:{
                Boolean isSupported = characteristics.get(CameraCharacteristics.CONTROL_AE_LOCK_AVAILABLE);
                return isSupported!=null && isSupported.booleanValue();
            }
            case AutoExposureMode: {
                int[] aeModes = characteristics.get(CameraCharacteristics.CONTROL_AE_AVAILABLE_MODES);
                if (aeModes != null && aeModes.length > 0) {
                    int offCode = AutoExposureMode.CONTROL_AE_MODE_OFF.getCode();
                    for (int mode : aeModes) {
                        if (mode != offCode) {
                            return true;
                        }
                    }
                }
                break;
            }
            case AutoExposureRegion:{
                Integer count =characteristics.get(CameraCharacteristics.CONTROL_MAX_REGIONS_AE);
                return count!=null && count.intValue() >0;
//                break;
            }
            case AutoFocusTrigger:{
                return true;
            }
            case AutoFocusRegion: {
                Integer count = characteristics.get(CameraCharacteristics.CONTROL_MAX_REGIONS_AF);
                return count!=null && count.intValue() >0;
//                break;
            }
            case AutoWhiteBalanceLock:{
                Boolean isSupported = characteristics.get(CameraCharacteristics.CONTROL_AWB_LOCK_AVAILABLE);
                return isSupported!=null && isSupported.booleanValue();
            }
            case AutoWhiteBalanceMode: {
                int[] awbModes = characteristics.get(CameraCharacteristics.CONTROL_AWB_AVAILABLE_MODES);
                if (awbModes != null && awbModes.length > 0) {
                    int offCode = AutoWhiteBalanceMode.CONTROL_AWB_MODE_OFF.getCode();
                    for (int mode : awbModes) {
                        if (mode != offCode) {
                            return true;
                        }
                    }
                }
                break;
            }
            case AutoWhiteBalanceRegion:{
                Integer count =characteristics.get(CameraCharacteristics.CONTROL_MAX_REGIONS_AWB);
                return count!=null && count.intValue() >0;
//                break;
            }
            case ColorCorrectionAvailableAberrationMode: {
                int[] modes = characteristics.get(CameraCharacteristics.COLOR_CORRECTION_AVAILABLE_ABERRATION_MODES);
                if (modes != null && modes.length > 0) {
                    return true;
                }
                break;
            }
            case ColorEffect: {
                return true;
            }
            case ExposureCompensation: {
                Range<Integer> range = characteristics.get(CameraCharacteristics.CONTROL_AE_COMPENSATION_RANGE);
                if (range != null) {
                    return true;
                }
                break;
            }
            case ExposureTime: {
                Range<Long> range = characteristics.get(CameraCharacteristics.SENSOR_INFO_EXPOSURE_TIME_RANGE);
                if (range != null) {
                    return true;
                }
                break;
            }
            case FaceDetection: {
                int[] faceModes = characteristics.get(CameraCharacteristics.STATISTICS_INFO_AVAILABLE_FACE_DETECT_MODES);
                if (faceModes != null && faceModes.length > 0) {
                    return true;
                }
                break;
            }
            case FlashMode: {
                Boolean available = characteristics.get(CameraCharacteristics.FLASH_INFO_AVAILABLE);
                if (available != null) {
                    return available.booleanValue();
                }
                break;
            }
            case FocusAreas:{
                return true;
            }
            case FocusDistance: {
//                    Float minDistance = characteristics.get(CameraCharacteristics.LENS_INFO_MINIMUM_FOCUS_DISTANCE);
//                    if (minDistance != null && Float.compare(minDistance.floatValue(), 0f) != 0) {
//                        return true;
//                    }
                float[] distances = characteristics.get(CameraCharacteristics.LENS_INFO_AVAILABLE_FOCAL_LENGTHS);
                if (distances != null && distances.length > 1) {
                    return true;
                }
                break;
            }
            case FrameDuration: {
                Long duration = characteristics.get(CameraCharacteristics.SENSOR_INFO_MAX_FRAME_DURATION);
                if (duration != null) {
                    return true;
                }
                break;
            }
            case Mode:{
                int[] modes = characteristics.get(CameraCharacteristics.CONTROL_AVAILABLE_MODES);
                return modes!=null && modes.length>1;
//                break;
            }
            case NoiseReductionMode: {
                int[] awbModes = characteristics.get(CameraCharacteristics.NOISE_REDUCTION_AVAILABLE_NOISE_REDUCTION_MODES);
                if (awbModes != null && awbModes.length > 0) {
                    return true;
                }
                break;
            }
            case PreviewFormat:{
                return true;
            }
            case PictureSize: {
                android.util.Size[] sizes = getPictureSize();
                if (sizes != null && sizes.length > 0) {
                    return true;
                }
                break;
            }
            case PictureFormat:{
                return true;
            }
            case PreviewSize: {
                android.util.Size[] sizes = getPreviewSize();
                if (sizes != null && sizes.length > 0) {
                    return true;
                }
                break;
            }
            case SceneMode: {
                int[] modes = characteristics.get(CameraCharacteristics.CONTROL_AVAILABLE_SCENE_MODES);
                if (modes != null && modes.length > 0) {
                    return true;
                }
                break;
            }
            case Sensitivity: {
                Range<Integer> range = characteristics.get(CameraCharacteristics.SENSOR_INFO_SENSITIVITY_RANGE);
                if (range != null) {
                    return true;
                }
                break;
            }
            case StabilizationMode: {
                int[] modes = characteristics.get(CameraCharacteristics.LENS_INFO_AVAILABLE_OPTICAL_STABILIZATION);
                if (modes != null && modes.length > 0) {
                    return true;
                }
                break;
            }
            case VideoStabilizationMode: {
                int[] modes = characteristics.get(CameraCharacteristics.CONTROL_AVAILABLE_VIDEO_STABILIZATION_MODES);
                if (modes != null && modes.length > 0) {
                    return true;
                }
                break;
            }
            case Zoom: {
                return true;
            }
            case PreviewBound:{
                return true;
            }
            default:
                throw new UnsupportedOperationException("不支持的操作:"+key.toString());
        }
//        CameraCharacteristics.SENSOR_MAX_ANALOG_SENSITIVITY;
//        CameraCharacteristics.CONTROL_POST_RAW_SENSITIVITY_BOOST_RANGE;

        return false;
    }

    @Override
    protected boolean syncAll(){
        assert isOpen;
        assert mParameterSync !=null : "相机参数同步回调，不可为null";
        if(mMap.size()==0){
            return true;
        }
        //todo 同步部分参数时，要特别处理，比如手动聚焦。这部分参数，很可能会有冲突的情况发生
        CaptureRequest.Builder builder = mParameterSync.getBuilder();
        mParameterSync.beginSync(mMap);
        Set<Map.Entry<ParameterKey, Object>> set = mMap.entrySet();
        for (Map.Entry<ParameterKey, Object> item : set) {
            processContent(item.getKey(),item.getValue(),builder);
        }
        boolean isOk = mParameterSync.endSync(mMap);
        return isOk;
    }

    @Override
    protected boolean syncSingle(ParameterKey key,Object value) {
        assert isOpen;
        assert mParameterSync != null : "相机参数同步回调，不可为null";
        if (mMap.size() == 0) {
            return true;
        }
        //todo 同步部分参数时，要特别处理，比如手动聚焦。这部分参数，很可能会有冲突的情况发生
        CaptureRequest.Builder builder = mParameterSync.getBuilder();
        Map<ParameterKey, Object> map = new HashMap<>();
        map.put(key, value);
        mParameterSync.beginSync(map);
        processContent(key, value, builder);
        boolean isOk = mParameterSync.endSync(map);
        return isOk;
    }

    //处理要同步的内容
    private boolean processContent(ParameterKey key, Object value, CaptureRequest.Builder builder){
        boolean isOk=false;
        Log.i("Set Camera Parmater ",String.format("%s: %s",key.toString() ,String.valueOf(value)), Log.LEVEL_HIGH);
        switch (key){
            case Aperture:{
                float aperture = ValueUtils.toFloat(value);
                builder.set(CaptureRequest.LENS_APERTURE,aperture);
                isOk=true;
                break;
            }
            case ApiLevel: {
                isOk = getApiLevel() == ValueUtils.toInt(value);
                break;
            }
            case AutoExposureMode:{
                builder.set(CaptureRequest.CONTROL_AE_MODE, ((AutoExposureMode) value).getCode());
                isOk=true;
                break;
            }
            case AutoFocusMode:{
                builder.set(CaptureRequest.CONTROL_AF_MODE, ((AutoFocusMode) value).getCode());
                isOk=true;
                break;
            }
            case AutoWhiteBalanceMode:{
                builder.set(CaptureRequest.CONTROL_AWB_MODE, ((AutoWhiteBalanceMode) value).getCode());
                isOk=true;
                break;
            }
            case ColorCorrectionAvailableAberrationMode:{
                builder.set(CaptureRequest.COLOR_CORRECTION_ABERRATION_MODE,((ColorCorrectionAvailableAberrationMode)value).getCode());
                isOk=true;
                break;
            }
            case ColorEffect:{
                builder.set(CaptureRequest.CONTROL_EFFECT_MODE,((ColorEffect)value).getCode());
                isOk=true;
                break;
            }
            case ExposureCompensation:{
                int compensation = ValueUtils.toInt(value);
                builder.set(CaptureRequest.CONTROL_AE_EXPOSURE_COMPENSATION,compensation);
                isOk=true;
                break;
            }
            case ExposureTime:{
                long time = ValueUtils.toLong(value);
                builder.set(CaptureRequest.SENSOR_EXPOSURE_TIME,time);
                isOk=true;
                break;
            }
            case FaceDetection:{
                builder.set(CaptureRequest.STATISTICS_FACE_DETECT_MODE,((FaceDetectMode)value).getCode());
                isOk=true;
                break;
            }
            case FlashMode: {
                builder.set(CaptureRequest.FLASH_MODE, ((FlashMode) value).getCode());
                isOk = true;
                break;
            }
            case FocusAreas:{
                //设置聚焦区域，是否需要关闭自动聚焦，或者自动聚焦中，有特殊的方式可以指定聚焦区域
                isOk=true;
                break;
            }
            case FocusDistance:{
                float distance = ValueUtils.toFloat(value);
                builder.set(CaptureRequest.LENS_FOCAL_LENGTH,distance);
                isOk = true;
                break;
            }
            case FrameDuration:{
                long duration = ValueUtils.toLong(value);
                builder.set(CaptureRequest.SENSOR_FRAME_DURATION,duration);
                isOk=true;
                break;
            }
            case Mode:{
                builder.set(CaptureRequest.CONTROL_MODE,((Mode)value).getCode());
                isOk=true;
                break;
            }
            case NoiseReductionMode:{
                builder.set(CaptureRequest.NOISE_REDUCTION_MODE,((NoiseReductionMode)value).getCode());
                isOk=true;
                break;
            }
            case PictureSize:{
                //目前不处理
                isOk=true;
                break;
            }
            case PictureOrientatoin:{
                int cameraOrientation = mCharacteristics.get(CameraCharacteristics.SENSOR_ORIENTATION);
                int screenOrientation = ValueUtils.toInt(value);
                int orientation;
                switch (screenOrientation){
                    case 0:
                        screenOrientation=90;
                        break;
                    case 90:
                        screenOrientation=0;
                        break;
                    case 180:
                        screenOrientation = 270;
                        break;
                    case 270:
                        screenOrientation=180;
                        break;
                }
                if (mIsFront) {
                    orientation =  (screenOrientation +cameraOrientation + 90) % 360;
                } else {
                    // back-facing
                    orientation = (screenOrientation + cameraOrientation + 270) % 360;
                }
                builder.set(CaptureRequest.JPEG_ORIENTATION,orientation);
                isOk=true;
                break;
            }
            case PreviewBound:{
                isOk=true;
                break;
            }
            case PreviewFormat:{
                isOk=true;
                break;
            }
            case PreviewOrientation:{
                isOk = true;
                break;
            }
            case PreviewSize:{
                //预览尺寸无法通过修改摄像机参数完成，在打开相机时处理，如果临时需要修改预览尺寸，需要重新打开相机
                isOk=true;
                break;
            }
            case SceneMode:{
                builder.set(CaptureRequest.CONTROL_SCENE_MODE,((SceneMode)value).getCode());
                isOk=true;
                break;
            }
            case Sensitivity:{
                int sensitivity = ValueUtils.toInt(value);
                builder.set(CaptureRequest.SENSOR_SENSITIVITY,sensitivity);
                isOk=true;
                break;
            }
            case Version:{
                isOk=true;
                break;
            }
            case VideoStabilizationMode:{
                builder.set(CaptureRequest.CONTROL_VIDEO_STABILIZATION_MODE,((VideoStabilizationMode)value).getCode());
                isOk=true;
                break;
            }
            case Zoom: {
                int zoom =ValueUtils.toInt(value);
                Range<Integer> range = getSupportedRange(ParameterKey.Zoom);
                if (zoom < range.getLower()) {
                    zoom = range.getLower().intValue();
                } else if (zoom > range.getUpper()) {
                    zoom = range.getUpper().intValue();
                }

                int cropType = mCharacteristics.get(CameraCharacteristics.SCALER_CROPPING_TYPE).intValue();
                //todo 这里的处理方式，需要优化
                //只能在中心位置
                if(cropType == CameraCharacteristics.SCALER_CROPPING_TYPE_CENTER_ONLY){

                }
                else{

                }
                Rect activeRegion = mCharacteristics.get(CameraCharacteristics.SENSOR_INFO_ACTIVE_ARRAY_SIZE);
                Log.i(TAG,"SENSOR_INFO_ACTIVE_ARRAY_SIZE:"+activeRegion.toString());
                Size previewSize = get(ParameterKey.PreviewSize);
                if(previewSize==null){
                    throw  new IllegalArgumentException("PreviewSize is necessary");
                }
                Rect region = getCropRegion(mCharacteristics,previewSize,zoom);
                /*
                * SCALER_CROP_REGION：是基于SENSOR_INFO_ACTIVE_ARRAY_SIZE坐标体系(左上角为0,0),部分格式不支持裁减，比如RAW16
                * 宽高不能小于activeArraySize / SCALER_AVAILABLE_MAX_DIGITAL_ZOOM ;
                * 输出流的宽高比跟裁减宽高比一致的话，可以被直接使用，否则需要进一步裁减
                 */
                builder.set(CaptureRequest.SCALER_CROP_REGION, region);
                Log.i(TAG,"zoom crop regoin:"+region.toString());
                isOk=true;
                break;
            }
        }
        return isOk;
    }

    @Override
    protected <T> boolean checkType(ParameterKey key, T value){
        return super.checkType(key,value);
    }

    @Override
    public int getCameraOrientation() {
        if (mCharacteristics!=null) {
            int sensorOrientation = mCharacteristics.get(CameraCharacteristics.SENSOR_ORIENTATION);
            return sensorOrientation;
        }
        return -1;
    }

    @Override
    protected Integer getVersion(){
        return new Integer(1);
    }
    @Override
    protected Integer getApiLevel(){
        return new Integer(2);
    }

    private android.util.Size[] getPreviewSize() {
        if (mCharacteristics != null) {
            StreamConfigurationMap map = mCharacteristics.get(
                    CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
            if (map != null) {
                android.util.Size[] sizeList = map.getOutputSizes(SurfaceTexture.class);
//                android.util.Size[] sizeList1 = map.getOutputSizes(ImageReader.class);
//                android.util.Size[] sizeList2 = map.getOutputSizes(ImageFormat.JPEG);
//                android.util.Size[] sizeList3 = map.getOutputSizes(ImageFormat.YUV_420_888);
                return sizeList;
            }
        }
        return null;
    }
    private android.util.Size[] getPictureSize() {
        if (mCharacteristics != null) {
            StreamConfigurationMap map = mCharacteristics.get(
                    CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
            if (map != null) {
                android.util.Size[] sizeList = map.getOutputSizes(ImageFormat.JPEG);
//                android.util.Size[] sizeList = map.getOutputSizes(SurfaceTexture.class);
//                android.util.Size[] sizeList = map.getOutputSizes(ImageReader.class);
//                android.util.Size[] sizeList = map.getOutputSizes(ImageFormat.YUV_420_888);
                return sizeList;
            }
        }
        return null;
    }
    private List<Size> toSizeList(android.util.Size[] sizes){
        if(sizes==null) return null;
        List<Size> list = new ArrayList<>(sizes.length);
        for(android.util.Size item:sizes){
            list.add(new Size(item.getWidth(),item.getHeight()));
        }
        return list;
    }

    Rect getCropRegion(CameraCharacteristics characteristics,Size previewSize,int zoom) {
        //注1：适配裁减宽高比后，发现依旧会被二次裁减。最终决定保持宽高比跟SENSOR_INFO_ACTIVE_ARRAY_SIZE一致
        //注2：完美裁减宽高比算法，在CameraApi2.getFixedCropRegion中实现了
        Rect activeRegion = characteristics.get(CameraCharacteristics.SENSOR_INFO_ACTIVE_ARRAY_SIZE);
        float maxZoom = characteristics.get(CameraCharacteristics.SCALER_AVAILABLE_MAX_DIGITAL_ZOOM);
        int minWidth = (int) Math.floor(activeRegion.width() / maxZoom);
        int minHeight = (int) Math.floor(activeRegion.height() / maxZoom);
        double zoomRate = ((double) zoom) / 100f;
        int width = (int)Math.floor(activeRegion.width() /zoomRate);
        int height = (int)Math.floor(activeRegion.height() /zoomRate);
        if(width<minWidth){
            width = minWidth;
        }
        if(height<minHeight){
            height = minHeight;
        }
        int left= (activeRegion.width()-width)/2;
        int top = (activeRegion.height()-height)/2;
        Rect region = new Rect(left,top,left+width,top+height);
        return region;
    }
    interface ICameraParameterSyncApi2 extends ICameraParameterSync {
        CaptureRequest.Builder getBuilder();

        @NotNull
        CameraCharacteristics getCameraCharacteristics();
    }
}
