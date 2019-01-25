package com.baoshen.cameralib;

import android.content.Context;
import android.os.Build;
import android.support.annotation.CallSuper;
import android.support.annotation.Keep;
import android.util.Range;

import com.baoshen.cameralib.enums.CameraState;
import com.baoshen.cameralib.enums.ParameterKey;

import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by Shute on 2018/9/22.
 */
@Keep
public abstract class AbsCameraParameter implements IRelease {
    private static final String TAG = AbsCameraParameter.class.getSimpleName();
    protected static final String UNMATCHED_TYPE_ERROR_FORMAT = "参数%s,数据类型不符合";

    private AbsCameraParameter() {
        this.mSupportedMap = new HashMap<>();
        this.mMap = new HashMap<>();
    }

    protected AbsCameraParameter(@NotNull Context context,@NotNull AbsCamera camera) {
        this();
        this.mCamera = camera;
        this.mContext = context;
        this.mModel = Build.MODEL;//手机型号
        this.mManufacturer = Build.MANUFACTURER;//制造商
    }

    protected Context mContext;
    protected boolean mIsFront;
    protected String mCameraId;
    protected Map<ParameterKey, Boolean> mSupportedMap;
    protected Map<ParameterKey, Object> mMap;
    protected String mModel;//手机型号，特殊手机，可能需要专门处理
    protected String mManufacturer;
    protected boolean isOpen;
    protected AbsCamera mCamera;

    /**
     * 设置参数（异步的）
     *
     * @param map 要设置的属性
     */
    @CallSuper
    public void put(@NotNull Map<ParameterKey, Object> map) {
        if(map.size() == 0){
            return;
        }
        assert null != mCamera && CameraState.Running != mCamera.getState() && CameraState.Close !=mCamera.getState()
                && CameraState.Release !=mCamera.getState() :"please use this method at init.";
        Set<Map.Entry<ParameterKey, Object>> set = map.entrySet();
        for (Map.Entry<ParameterKey, Object> item : set) {
            changeProtected(item.getKey(), item.getValue());
        }
    }

    public void putAndSync(ParameterKey key,Object value ) {
        if (!isOpen) {
            throw new RuntimeException("Don't use this method until camera opened");
        }
        assert ParameterKey.PreviewSize != key : "Don't set " + key + "by 'putAndSync',use 'put' instead";
        changeProtected(key, value);
        syncSingle(key, value);
    }

    //判断参数
    public boolean isSupported(ParameterKey key) {
        boolean isSupported;
        if (!mSupportedMap.containsKey(key)) {
            isSupported = isSupportedProtected(key);
            mSupportedMap.put(key, new Boolean(isSupported));
        } else {
            isSupported = mSupportedMap.get(key);
        }
        return isSupported;
    }

    public <T> T get(ParameterKey key) {
        if(ParameterKey.Version == key){
            return (T)getVersion();
        }
        else if(ParameterKey.ApiLevel == key){
            return (T)getApiLevel();
        }
        if (!mMap.containsKey(key)) {
            return null;
        }
        return (T) mMap.get(key);
    }
    public Map<ParameterKey,Object> getAll() {
        Map<ParameterKey, Object> map = new HashMap<>(mMap);
        map.put(ParameterKey.Version, getVersion());
        map.put(ParameterKey.ApiLevel, getApiLevel());
        return map;
    }

    public abstract <T> List<T> getSupportedList(ParameterKey key);

    public abstract <T extends Comparable<? super T>> Range<T> getSupportedRange(ParameterKey key);

    public String getId() {
        return mCameraId;
    }

    public boolean isIsFront() {
        return mIsFront;
    }

    public void release() {
        close();
    }

    public boolean open() {
        isOpen = true;
        return isOpen;
    }

    @CallSuper
    public boolean close() {
        isOpen = false;
        if(mMap!=null && mMap.size()>0){
            mMap.clear();
        }
        if(mSupportedMap!=null && mMap.size()>0){
            mSupportedMap.clear();
        }
        return !isOpen;
    }

    public abstract boolean init(@NotNull ICameraParameterSync sync);

    //将参数同步到摄像机
    protected abstract boolean syncAll();
    protected abstract boolean syncSingle(ParameterKey key,Object value);

    protected abstract boolean isSupportedProtected(ParameterKey key);

    protected <T> boolean checkType(ParameterKey key, T value) {
        assert value !=null : "value can't be null";
        Class requireType = key.getValueType();
        Class valueType = value.getClass();
        return  requireType.equals(valueType);
    }

    //设置属性，但不直接更改
    protected <T> void changeProtected(ParameterKey key,@NotNull T value) {
        //设置焦距时，注意CameraCharacteristics.LENS_INFO_FOCUS_DISTANCE_CALIBRATION为LENS_INFO_FOCUS_DISTANCE_CALIBRATION_UNCALIBRATED表示直接数值，
        //跟另外2种有标准单位的不同，所以设计时，要统一下单位问题

        //todo 注意下曝光时间，单位应该是纳秒，1000000ns=1ms
        //todo 注意FrameDuration单位是纳秒
        if(!checkType(key,value)){
            throw new IllegalArgumentException(String.format(UNMATCHED_TYPE_ERROR_FORMAT,key.toString()));
        }
        mMap.put(key, value);
    }
    public abstract int getCameraOrientation();

    protected abstract Integer getVersion();
    protected abstract Integer getApiLevel();
}
