package com.baoshen.cameralib.api1;

import android.content.Context;
import android.hardware.Camera;
import android.hardware.camera2.CaptureRequest;
import android.util.Range;

import com.baoshen.cameralib.AbsCamera;
import com.baoshen.cameralib.AbsCameraParameter;
import com.baoshen.cameralib.CameraUtils;
import com.baoshen.cameralib.ICameraParameterSync;
import com.baoshen.cameralib.ValueUtils;
import com.baoshen.cameralib.api2.CameraApi2;
import com.baoshen.cameralib.enums.AutoFocusMode;
import com.baoshen.cameralib.enums.AutoWhiteBalanceMode;
import com.baoshen.cameralib.enums.ColorEffect;
import com.baoshen.cameralib.enums.FlashMode;
import com.baoshen.cameralib.enums.ImageFormats;
import com.baoshen.cameralib.enums.ParameterKey;
import com.baoshen.cameralib.enums.ParameterValueSupportedMode;
import com.baoshen.cameralib.enums.SceneMode;
import com.baoshen.cameralib.enums.VideoStabilizationMode;
import com.baoshen.common.Log;
import com.baoshen.common.graphics.Size;

import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by Shute on 2018/9/22.
 */
public class CameraParameterApi1 extends AbsCameraParameter {
    CameraParameterApi1(@NotNull Context context, @NotNull AbsCamera camera){
        super(context,camera);
        assert camera instanceof CameraApi1 : "camera must be CameraApi1";
    }
    private static final String TAG = CameraParameterApi1.class.getSimpleName();
    Camera.Parameters mParametersGetter;
    int mIntCameraId = -1;
    Camera mDevice;
    ICameraParameterSync mParamerSync;

    @Override
    public boolean init(@NotNull ICameraParameterSync sync){
        mParamerSync = sync;
        CameraApi1 cameraApi1 = (CameraApi1) this.mCamera;
        mDevice = cameraApi1.getCamera();
        mCameraId = mCamera.getCameraId();
        mIntCameraId = Integer.parseInt(mCameraId);
        mParametersGetter = mDevice.getParameters();
        Camera.CameraInfo info = new android.hardware.Camera.CameraInfo();
        Camera.getCameraInfo(mIntCameraId, info);
        mIsFront = info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT;
        return true;
    }

    @Override
    public <T> List<T> getSupportedList(ParameterKey key) {
        assert ParameterValueSupportedMode.List != key.getParameterValueSupportedMode() :
                "getSupportedList require ParameterValueSupportedMode.List";
        Camera.Parameters parameters = mParametersGetter;
        if (mCameraId == null || mParametersGetter==null) {
            return null;
        }
        switch (key) {
            case Aperture: {
                break;
            }
            case AutoExposureMode: {
                return null;
            }
            case AutoFocusMode: {
                List<String> modes = parameters.getSupportedFocusModes();
                if(modes!=null && modes.size()>0){
                    List<AutoFocusMode> list =  new ArrayList<>(modes.size());
                    for (String item :modes){
                        list.add(AutoFocusMode.get(item));
                    }
                    return (List<T>)list;
                }
                break;
            }
            case AutoWhiteBalanceMode: {
                List<String> modes = parameters.getSupportedWhiteBalance();
                if(modes!=null) {
                    List<AutoWhiteBalanceMode> list= new ArrayList<>();
                    for (String item :modes){
                        list.add(AutoWhiteBalanceMode.get(item));
                    }
                    return (List<T>) list;
                }
                break;
            }
            case ColorEffect: {
                List<String> modes = parameters.getSupportedColorEffects();
                if (modes != null && modes.size() > 0) {
                    List<ColorEffect> list = new ArrayList<>(modes.size());
                    for (String item : modes) {
                        list.add(ColorEffect.get(item));
                    }
                    return (List<T>) list;
                }
                break;
            }
            case FaceDetection: {
                int count = parameters.getMaxNumDetectedFaces();
                if (count>0) {
                    List<Integer> modes = new ArrayList<>();
                    for (int i = 1; i <= count; i++) {
                        modes.add(i);
                    }
                    return (List<T>) modes;
                }
                break;
            }
            case FlashMode: {
                List<String> modes = parameters.getSupportedFlashModes();
                if (modes != null && modes.size() > 0) {
                    List<FlashMode> list = new ArrayList<>(modes.size());
                    for (String item:modes){
                        list.add(FlashMode.get(item));
                    }
                    return (List<T>) list;
                }
                break;
            }
            case FocusDistance:{
                break;
            }
            case NoiseReductionMode:{
                return null;
            }
            case PictureFormat:{
                List<Integer> formats = parameters.getSupportedPictureFormats();
                if(formats!=null && formats.size()>0){
                    List<ImageFormats> list =new ArrayList<>(formats.size());
                    for(Integer item:formats){
                        list.add(ImageFormats.get(item));
                    }
                    return (List<T>)list;
                }
                break;
            }
            case PictureSize: {
                android.util.Size[] sizes = getPictureSize();
                return (List<T>) toSizeList(sizes);
            }
            case PreviewFormat:{
                List<Integer> formats = parameters.getSupportedPreviewFormats();
                if(formats!=null && formats.size()>0){
                    List<ImageFormats> list =new ArrayList<>(formats.size());
                    for(Integer item:formats){
                        list.add(ImageFormats.get(item));
                    }
                    return (List<T>)list;
                }
                break;
            }
            case PreviewSize: {
                android.util.Size[] sizes = getPreviewSize();
                return (List<T>) toSizeList(sizes);
            }
            case SceneMode:{
                List<String> modes = parameters.getSupportedSceneModes();
                if(modes!=null){
                    List<SceneMode> list =new ArrayList<>(modes.size());
                    for (String item :modes){
                        list.add(SceneMode.get(item));
                    }
                    return (List<T>)list;
                }
                break;
            }
            case VideoStabilizationMode:{
                if(parameters.getVideoStabilization()){
                    List<VideoStabilizationMode>list = new ArrayList<>();
                    list.add(VideoStabilizationMode.CONTROL_VIDEO_STABILIZATION_MODE_OFF);
                    list.add(VideoStabilizationMode.CONTROL_VIDEO_STABILIZATION_MODE_ON);
                    return (List<T>) list;
                }
                break;
            }
            default:
                throw new UnsupportedOperationException("不支持的参数类型:"+key.toString());
        }
        return null;
    }

    @Override
    public <T extends Comparable<? super T>> Range<T> getSupportedRange(ParameterKey key) {
        assert ParameterValueSupportedMode.Range != key.getParameterValueSupportedMode() :
                "getSupportedRange require ParameterValueSupportedMode.Range";

        Camera.Parameters parameters = mParametersGetter;
        if (mParametersGetter == null) {
            return null;
        }
        switch (key) {
            case AutoFocusRegion:{
                int max = parameters.getMaxNumFocusAreas();
                if(max>0){
                    return (Range<T>)new Range<Integer>(0,max);
                }
            }
            case AutoExposureRegion:{
                return getSupportedRange(ParameterKey.AutoMeteringRegion);
            }
            case AutoMeteringRegion:{
                int max = parameters.getMaxNumMeteringAreas();
                if(max>0){
                    return (Range<T>)new Range<Integer>(0,max);
                }
            }
            case ExposureCompensation:{
                int min = parameters.getMinExposureCompensation();
                int max = parameters.getMaxExposureCompensation();
                if(max>0 && max>min){
                    return (Range<T>)new Range<Integer>(min,max);
                }
                break;
            }
            case ExposureTime:{
                break;
            }
            case FocusDistance:{
                float[] distances = new float[3];
                mParametersGetter.getFocusDistances(distances);
                float near = distances[0];
                float far = distances[2];
                Range<Float> range = new Range<>(near,far);
                if(far>near){
                    return (Range<T>)range;
                }
                break;
            }
            case FrameDuration: {
                int[] fps = new int[2];
                parameters.getPreviewFpsRange(fps);
                int min = fps[Camera.Parameters.PREVIEW_FPS_MIN_INDEX];
                int max = fps[Camera.Parameters.PREVIEW_FPS_MAX_INDEX];
                if(max>min) {
                    return (Range<T>) new Range<Integer>(min, max);
                }
                break;
            }
            case Sensitivity:{
                break;
            }
            case Zoom: {
                if (parameters.isZoomSupported()) {
                    String maxZoomStr = parameters.get("max-zoom");
                    String motZoomValuesStr = parameters.get("mot-zoom-values");
                    if ((maxZoomStr != null) || (motZoomValuesStr != null)) {
                        List<Integer> zoomList = parameters.getZoomRatios();
                        if (zoomList != null && zoomList.size() > 1) {
                            int max = Integer.MIN_VALUE, min = Integer.MAX_VALUE;
                            for (int item : zoomList) {
                                if (item > max) {
                                    max = item;
                                }
                                if (item < min) {
                                    min = item;
                                }
                            }
                            Range<Integer> range = new Range<>(min, max);
                            return (Range<T>) range;
                        }
                    }
                }
                break;
            }
            default:
                throw new UnsupportedOperationException("不支持的参数类型:"+key.toString());
        }
        return null;
    }

    @Override
    public void release(){
        mCamera = null;
        mDevice = null;
        mParametersGetter = null;
        mSupportedMap.clear();
        mMap.clear();
    }
    @Override
    public int getCameraOrientation() {
//        Integer orientationInteger = get(ParameterKey.PreviewOrientation);
//        int orientation = 0;
//        if(orientationInteger!=null){
//            orientation = orientationInteger.intValue();
//        }
//        Camera.CameraInfo info = new android.hardware.Camera.CameraInfo();
//        Camera.getCameraInfo(mIntCameraId, info);
//        /*
//         For example, suppose a device has a naturally tall screen. The
//         back-facing camera sensor is mounted in landscape. You are looking at
//         the screen. If the top side of the camera sensor is aligned with the
//         right edge of the screen in natural orientation, the value should be
//         90. If the top side of a front-facing camera sensor is aligned with
//         the right of the screen, the value should be 270.
//         */
//        int sensorOrientation = info.orientation;
//
//                getOrientation(orientation,)
        //实际测试中，跟传感器似乎没关系，背后摄像头，固定式90°；前置摄像头是270°(跟本侯摄像头相差180°)
        if (mIntCameraId != -1) {
            try {
                Camera.CameraInfo info = new android.hardware.Camera.CameraInfo();
                Camera.getCameraInfo(mIntCameraId, info);

                int orientation = info.orientation;
                return orientation;
            } catch (Exception ex) {
                Log.e(ex);
            }
        }
        return -1;
    }

    @Override
    protected boolean isSupportedProtected(ParameterKey key) {
        Camera.Parameters parameters = mParametersGetter;
        if(mParametersGetter==null) return false;
        switch (key) {
            case AutoFocusMode: {
                List<String> modes = parameters.getSupportedFocusModes();;
                if(modes!=null && modes.size()>1){
                    return true;
                }
                break;
            }
            case AutoFocusRegion:{
                int count = parameters.getMaxNumFocusAreas();
                return count>0;
            }
            case AutoExposureRegion:{
                return isSupportedProtected(ParameterKey.AutoMeteringRegion);
            }
            case AutoMeteringRegion:{
                int max = parameters.getMaxNumMeteringAreas();
                return max>0;
            }
            case AutoWhiteBalanceMode: {
                List<String> modes = parameters.getSupportedWhiteBalance();
                if (modes!=null && modes.size()>0) {
                    return true;
                }
                break;
            }
            case ColorEffect:{
                List<String> list = parameters.getSupportedColorEffects();
                if(list!=null && list.size()>0){
                    return true;
                }
                break;
            }
            case ExposureCompensation:{
                int max = parameters.getMaxExposureCompensation();
                int min = parameters.getMinExposureCompensation();
                if(max>0 && max>min){
                    return true;
                }
                break;
            }
            case FaceDetection: {
                int count = parameters.getMaxNumDetectedFaces();
                if (count>0) {
                    return true;
                }
                break;
            }
            case FlashMode:{
                List<String> modes = parameters.getSupportedFlashModes();
                if(modes!=null && modes.size()>0){
                    return true;
                }
                break;
            }
            case FrameDuration:{
                int[] fps = new int[2];
                int min = fps[Camera.Parameters.PREVIEW_FPS_MIN_INDEX];
                int max = fps[Camera.Parameters.PREVIEW_FPS_MAX_INDEX];
                if(max>min) {
                    return true;
                }
                break;
            }
            case FocusDistance: {
                float[] distances = new float[3];
                parameters.getFocusDistances(distances);
                float near = distances[0];
                float far = distances[2];
                if(far>near){
                    return true;
                }
                break;
            }
            case PreviewFormat:{
                return true;
            }
            case PreviewSize: {
                android.util.Size[] sizes = getPreviewSize();
                if (sizes != null && sizes.length > 0) {
                    return true;
                }
                break;
            }
            case PictureFormat:{
                return true;
            }
            case PictureSize: {
                android.util.Size[] sizes = getPictureSize();
                if( sizes != null && sizes.length > 0){
                    return true;
                }
                break;
            }
            case SceneMode:{
                List<String> modes= parameters.getSupportedSceneModes();
                if(modes !=null && modes.size()>0){
                    return true;
                }
                break;
            }
            case VideoStabilizationMode:{
                if(mParametersGetter.isVideoStabilizationSupported()){
                    return true;
                }
                break;
            }
            case Zoom: {
                if(parameters.isZoomSupported()){
                    return true;
                }
                break;
            }
            case Aperture:
            case AutoExposureMode:
            case ColorCorrectionAvailableAberrationMode:
            case ExposureTime:
            case FocusAreas:
            case Mode:
            case NoiseReductionMode:
            case PreviewBound:
            case Sensitivity:
            case StabilizationMode:{
                break;
            }
            default:
                throw new UnsupportedOperationException("不支持的参数类型:"+key.toString());
        }

        return false;
    }

    @Override
    protected boolean syncAll() {
        if (mMap.size() == 0) {
            return true;
        }
        if(mCamera==null){
            return false;
        }
        //todo 同步部分参数时，要特别处理，比如手动聚焦。这部分参数，很可能会有冲突的情况发生
        Set<Map.Entry<ParameterKey, Object>> set = mMap.entrySet();
        boolean isOk = true;
        mParamerSync.beginSync(mMap);
        Camera.Parameters parameters = mDevice.getParameters();
        for (Map.Entry<ParameterKey, Object> item : set) {
            if (!processContent(item.getKey(), item.getValue(),parameters)) {
                isOk = false;
            }
        }
        mParamerSync.endSync(mMap);
        mDevice.setParameters(parameters);
        return isOk;
    }
    @Override
    protected boolean syncSingle(ParameterKey key,Object value) {
        if (mDevice == null) {
            return false;
        }
        if(!isOpen){
            throw new UnsupportedOperationException("修改单个属性，需要在相机开启之后");
        }
        Map<ParameterKey,Object> map = new HashMap<>();
        map.put(key,value);
        mParamerSync.beginSync(map);
        Camera.Parameters parameters = mDevice.getParameters();
        boolean isOk = processContent(key, value, parameters);
        mDevice.setParameters(parameters);
        mParamerSync.endSync(map);
        return isOk;
    }

    //处理要同步的内容
    private boolean processContent(ParameterKey key, Object value,Camera.Parameters parameters){
        boolean isOk=false;
        if(parameters==null){
            return isOk;
        }
        Log.i("Set Camera Parmater ",String.format("%s: %s",key.toString() ,String.valueOf(value)), Log.LEVEL_HIGH);
        switch (key){
            case Aperture:{
                break;
            }
            case ApiLevel: {
                isOk = getApiLevel() == ValueUtils.toInt(value);
                break;
            }
            case AutoFocusMode:{
                parameters.setFocusMode(((AutoFocusMode)value).getValue());
                isOk = true;
                break;
            }
            case AutoWhiteBalanceMode:{
                parameters.setWhiteBalance(((AutoWhiteBalanceMode)value).getValue());
                isOk=true;
                break;
            }
            case ColorEffect:{
                parameters.setColorEffect(((ColorEffect)value).getValue());
                isOk=true;
                break;
            }
            case ExposureCompensation:{
                int compensation = ValueUtils.toInt(value);
                parameters.setExposureCompensation(compensation);
                if(parameters.isAutoExposureLockSupported()){
                    parameters.setAutoExposureLock(true);
                }
                isOk=true;
                break;
            }
            case FaceDetection:{
                break;
            }
            case FrameDuration: {
                Range<Integer> range = getSupportedRange(ParameterKey.FrameDuration);
                //todo 这里的逻辑，要配合着改动
                parameters.setPreviewFpsRange(range.getLower(), ValueUtils.toInt(value));
                isOk = true;
                break;
            }
            case FlashMode: {
                parameters.setFlashMode(((FlashMode) value).getValue());
                isOk = true;
                break;
            }
            case FocusAreas:{
                //设置聚焦区域，是否需要关闭自动聚焦，或者自动聚焦中，有特殊的方式可以指定聚焦区域
                break;
            }
            case NoiseReductionMode:{
                break;
            }
            case PictureSize:{
                Size pictureSize = (Size)value;
                parameters.setPictureSize(pictureSize.getWidth(),pictureSize.getHeight());
                isOk=true;
                break;
            }
            case PreviewBound:{
                //todo 预览范围，作为特殊处理
                break;
            }
            case PreviewFormat:{
                ImageFormats format = (ImageFormats) value;
                parameters.setPreviewFormat(format.getCode());
                isOk=true;
                break;
            }
            case PreviewOrientation:{
                if(mDevice!=null) {
                    //todo mParametersGetter.setRotation()具体作用是什么（猜测是影响拍照保存的jpeg照片）
                    int cameraOrientation = getCameraOrientation();
                    int screenOrientation = ValueUtils.toInt(value);
                    int orientation = CameraUtils.getCameraDisplayOrientation(cameraOrientation,screenOrientation,mIsFront);
                    Log.i(TAG,"Preview Display Orientation:"+orientation);
                    mDevice.setDisplayOrientation(orientation);
                    isOk = true;
                }
                break;
            }
            case PreviewSize:{
                Size previewSize = (Size)value;
                parameters.setPreviewSize(previewSize.getWidth(),previewSize.getHeight());
                isOk=true;
                break;
            }
            case SceneMode:{
                parameters.setSceneMode(((SceneMode)value).getValue());
                isOk=true;
                break;
            }
            case Sensitivity:{
                break;
            }
            case Version:{
                break;
            }
            case VideoStabilizationMode:{
                parameters.setVideoStabilization((VideoStabilizationMode)value == VideoStabilizationMode.CONTROL_VIDEO_STABILIZATION_MODE_ON);
                isOk=true;
                break;
            }
            case Zoom: {
//                int zoom =ValueUtils.toInt(value);
//                Range<Integer> range = getSupportedRange(ParameterKey.Zoom);
//                if (zoom < range.getLower()) {
//                    zoom = range.getLower().intValue();
//                } else if (zoom > range.getUpper()) {
//                    zoom = range.getUpper().intValue();
//                }
//                parameters.setZoom(zoom);
//                isOk = true;

                //另外一种方式，是用set("zoom",index)
                int zoom =ValueUtils.toInt(value);
                List<Integer> ratios = parameters.getZoomRatios();//按1%分割的，从小排到大的
                if(ratios!=null && ratios.size()>0){
                    int index = 0;
                    for(int i=0;i<ratios.size();i++){
                        if(ratios.get(i)<=zoom){
                            index = i;
                        }
                    }
                    parameters.set("zoom",index);
                  isOk = true;
                }

                break;
            }
        }
        return isOk;
    }

    @Override
    protected Integer getVersion(){
        return new Integer(1);
    }
    @Override
    protected Integer getApiLevel(){
        return new Integer(1);
    }
    private android.util.Size[] getPreviewSize() {
        Camera.Parameters parameters = mParametersGetter;
        if (parameters!= null) {
            List<Camera.Size> list = parameters.getSupportedPreviewSizes();
            if(list!=null && list.size()>0){
                android.util.Size[] sizes = new android.util.Size[list.size()];
                for(int i=0;i<list.size();i++){
                    Camera.Size item = list.get(i);
                    sizes[i] = new android.util.Size(item.width,item.height);
                }
                return sizes;
            }
        }
        return null;
    }
    private android.util.Size[] getPictureSize() {
        Camera.Parameters parameters = mParametersGetter;
        if (parameters!= null) {
            List<Camera.Size> list = parameters.getSupportedPictureSizes();
            if(list!=null && list.size()>0){
                android.util.Size[] sizes = new android.util.Size[list.size()];
                for(int i=0;i<list.size();i++){
                    Camera.Size item = list.get(i);
                    sizes[i] = new android.util.Size(item.width,item.height);
                }
                return sizes;
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
}
