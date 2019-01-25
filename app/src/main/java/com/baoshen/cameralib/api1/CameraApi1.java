package com.baoshen.cameralib.api1;

import android.app.Activity;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.text.TextUtils;
import android.view.TextureView;

import com.baoshen.cameralib.AbsCamera;
import com.baoshen.cameralib.AbsCameraImage;
import com.baoshen.cameralib.AbsCameraParameter;
import com.baoshen.cameralib.AutoFitTextureView;
import com.baoshen.cameralib.ICameraListener;
import com.baoshen.cameralib.ICameraParameterSync;
import com.baoshen.cameralib.enums.AutoFocusCaptureState;
import com.baoshen.cameralib.enums.AutoFocusMode;
import com.baoshen.cameralib.enums.CameraState;
import com.baoshen.cameralib.enums.ImageFormats;
import com.baoshen.cameralib.enums.ParameterKey;
import com.baoshen.common.Log;
import com.baoshen.common.graphics.Size;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

/**
 * Created by Shute on 2018/9/26.
 */
public class CameraApi1 extends AbsCamera {
    public CameraApi1(boolean isFront, @NotNull Activity context) {
        super(isFront, context);
    }

    public CameraApi1(@NotNull String cameraId, @NotNull Activity context) {
        super(cameraId, context);
    }

    public static final String TAG = CameraApi1.class.getSimpleName();

    CameraParameterApi1 mParameters;
    Camera mCamera;
    protected ICameraParameterSync mParameterSync = new ICameraParameterSync() {
        @Override
        public boolean beginSync(Map<ParameterKey, Object> map) {
            return true;
        }

        @Override
        public boolean endSync(Map<ParameterKey, Object> map) {
            ICameraListener listener = mListener;
            if (mListener != null) {
                Set<Map.Entry<ParameterKey, Object>> set = map.entrySet();
                for (Map.Entry<ParameterKey, Object> item : set) {
                    listener.onParameterChanged(CameraApi1.this, item.getKey(), item.getValue());
                }
            }
            return true;
        }
    };
    protected Semaphore mCameraOpenCloseLock = new Semaphore(1);//信号量，防止在相机打开的过程中关闭相机

    private final TextureView.SurfaceTextureListener mSurfaceTextureListener
            = new TextureView.SurfaceTextureListener() {
        @Override
        public void onSurfaceTextureAvailable(SurfaceTexture texture, int width, int height) {
            if (CameraState.WaitingOpen == getState()) {
                open();
            }

        }

        @Override
        public void onSurfaceTextureSizeChanged(SurfaceTexture texture, int width, int height) {

        }

        @Override
        public boolean onSurfaceTextureDestroyed(SurfaceTexture texture) {
            return true;
        }

        @Override
        public void onSurfaceTextureUpdated(SurfaceTexture texture) {
            //内容更新，应该怎么设计处理，是否将整个SurfaceTexture往外发？外部根据需要去处理，这里是否会导致大量内容问题？
            //lockCanvas拿到null，估计得用GL才可以处理
//            if(!mTextureView.isAvailable()){
//                return;
//            }
//            if(CameraState.Running != mState){
//                return;
//            }
//            Canvas canvas = mTextureView.lockCanvas();
//            if(canvas!=null) {
//                Log.i(TAG,"canvas existed");
//                mTextureView.unlockCanvasAndPost(canvas);
//            }
        }
    };

    private Camera.PreviewCallback mPreviewCallback = new Camera.PreviewCallback() {
        public synchronized void onPreviewFrame(byte[] data, Camera camera) {
            if (data == null) {
                //release();
                return;
            }
            if (mPreviewSize == null) {
                return;
            }
            AbsCameraImage newImage = mCameraReader.acquireImageForWrite();
            byte[] buff;
            if (newImage != null) {
                buff = newImage.getBuffer();
                AbsCameraImage oldImage = ((CameraReaderApi1) mCameraReader).find(data);
                //取消锁定
                //注：先申请新的，再处理旧的，不重复用同一个
                if (oldImage != null) {
                    oldImage.setState(AbsCameraImage.ImageCacheState.Written);
                    oldImage.release();
                } else {
                    Log.e(TAG, "找不到预览的图片缓存");
                }
                ICameraListener listener = mListener;
                if (listener != null) {
                    listener.onPreviewFrame(CameraApi1.this, mCameraReader);
                }
            } else {
                buff = data;
            }
            //这行代码要保持在最后
            mCamera.addCallbackBuffer(buff);
        }
    };

    public Camera getCamera() {
        return mCamera;
    }

    //初始化等，初始化后，才可以获得设备参数
    @Override
    public boolean init(@NotNull AutoFitTextureView textureView, @NotNull ICameraListener listener) {
        try {
            if (!mCameraInitCloseLock.tryAcquire(2500, TimeUnit.MILLISECONDS)) {
                Log.e("CameraState", "Time out waiting to lock camera Initing.");
                Log.v("test", "init Time out waiting to lock camera Initing.", Log.LEVEL_HIGH);
                return false;
                //throw new RuntimeException("Time out waiting to lock camera Initing.");
            }
        } catch (InterruptedException e) {
            Log.e(e);
        }
        super.init(textureView, listener);
        if (CameraState.Release == getState()) {
            Log.e("CameraState", "camera had released");
            Log.v("test", "init camera had released", Log.LEVEL_HIGH);
            return false;
        }
        //assert CameraState.Release != getState() : "camera had released";
        if (CameraState.Init == getState() || CameraState.WaitingOpen == getState() || CameraState.Open == getState() || CameraState.Running == getState()) {
            Log.v("test", "init 已经初始化", Log.LEVEL_HIGH);
            return true;
        }

        //当有一个初始化摄像头的请求
        mTextureView = textureView;
        TextureView.SurfaceTextureListener oldTextureListener = mTextureView.getSurfaceTextureListener();
        assert oldTextureListener == null : "目前不兼容外部设置TextureView.setSurfaceTextureListener";
        mTextureView.setSurfaceTextureListener(mSurfaceTextureListener);
        Log.v("test", "init 已经初始化一半", Log.LEVEL_HIGH);
        mParameters = new CameraParameterApi1(mContext, this);
        if (TextUtils.isEmpty(mCameraId)) {
            int cameraCount = 0;
            Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
            cameraCount = Camera.getNumberOfCameras();
            for (int camIdx = 0; camIdx < cameraCount; camIdx++) {
                Camera.getCameraInfo(camIdx, cameraInfo);
                int facing = mIsFront ? 1 : 0;
                if (cameraInfo.facing == facing) {
                    mCameraId = String.valueOf(camIdx);
                    break;
                }
            }
        } else {
            Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
            int intCamId = Integer.parseInt(mCameraId);
            Camera.getCameraInfo(intCamId, cameraInfo);
            mIsFront = cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_FRONT;
        }
        boolean isOk = false;
        if (!TextUtils.isEmpty(mCameraId)) {
            try {
                mCamera = Camera.open(Integer.parseInt(mCameraId));
                isOk = mParameters.init(mParameterSync);
                if (isOk) {
                    setState(CameraState.Init);
                }
                isOk = true;
            } catch (Exception ex) {
                Log.e(ex);
            }
        }
        Log.v("test", "init 初始化成功", Log.LEVEL_HIGH);
        return isOk;
    }

    @Override
    public AbsCameraParameter getParameters() {
        assert CameraState.None != getState() && CameraState.Close != getState() && CameraState.Release != getState() : "请先初始化设备";
        return mParameters;
    }

    @Override
    public int getApiLevel() {
        return 1;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean open() {
        try {
            if (!mCameraOpenCloseLock.tryAcquire(2500, TimeUnit.MILLISECONDS)) {
                Log.e("CameraState", "Time out waiting to lock camera opening.");
                Log.v("test", "open Time out waiting to lock camera opening.", Log.LEVEL_HIGH);
                return false;
                //throw new RuntimeException("Time out waiting to lock camera opening.");
            }
            if (CameraState.Open == getState() || CameraState.Running == getState()) {
                Log.v("test", "open 相机已经打开", Log.LEVEL_HIGH);
                return true;
            }
            Log.i(TAG, "openCamera");
            if (CameraState.Init == getState() || CameraState.WaitingOpen == getState()) {
                if (!mTextureView.isAvailable()) {
                    setState(CameraState.WaitingOpen);
                    return true;
                }
                boolean isOk = false;

                setState(CameraState.Open);

                mCamera.setPreviewTexture(null);
                mCamera.setPreviewTexture(mTextureView.getSurfaceTexture());
                mParameters.open();
                mParameters.syncAll();
                mPreviewFormat = mParameters.get(ParameterKey.PreviewFormat);
                assert mPreviewFormat != null : "需要设置预览格式";
                mPreviewSize = mParameters.get(ParameterKey.PreviewSize);

                if (mPreviewSize == null) {
                    Log.e("CameraState", "需要设置预览尺寸");
                    Log.v("test", "open 需要设置预览尺寸.", Log.LEVEL_HIGH);
                    return false;
                }
                //assert mPreviewSize != null : "需要设置预览尺寸";

                mCamera.setPreviewCallbackWithBuffer(mPreviewCallback);//设置摄像头预览帧回调
                mCameraReader = new CameraReaderApi1(mPreviewSize.getWidth(), mPreviewSize.getHeight(), mPreviewFormat,
                        2, mParameters.getCameraOrientation(), mIsFront);
                AbsCameraImage image = mCameraReader.acquireImageForWrite();
                mCamera.addCallbackBuffer(image.getBuffer());
                mCamera.startPreview();
                isOk = true;

                setState(isOk ? CameraState.Running : CameraState.Init);
                Log.v("test", "open 相机打开完成", Log.LEVEL_HIGH);
                return isOk;
            } else {
                Log.e("CameraState", "相机状态不对，错误的调用");
                Log.v("test", "open 相机状态不对，错误的调用", Log.LEVEL_HIGH);
                return false;
            }
        } catch (InterruptedException e) {
            Log.e(e);
            return false;
        } catch (IOException e) {
            Log.e(e);
            return false;
        } catch (IllegalArgumentException e) {
            Log.e(e);
            return false;
        } catch (SecurityException e) {
            Log.e(e);
            //相机权限权限
            throw e;
        } finally {
            mCameraOpenCloseLock.release();

        }
    }


    @Override
    public boolean close() throws RuntimeException {
        try {
            Log.i(TAG, "closeCamera");
            mAutoFocusCaptureState = null;
            if (CameraState.None == getState() || CameraState.Close == getState() || CameraState.Release == getState()) {
                Log.v("test", "close 已经关闭", Log.LEVEL_HIGH);
                return true;
            }
            boolean isOk;

            mCameraOpenCloseLock.acquire();
            mIsAutoFocus = false;
            if (mCamera != null) {
                mCamera.setPreviewTexture(null);
                mCamera.setPreviewCallback(null);
                mCamera.stopPreview();
                mCamera.release();
                mCamera = null;
            }

            setState(CameraState.Close);
            if (mParameters != null) {
                mParameters.close();
            }
            if (mCameraReader != null) {
                mCameraReader.close();
            }
            isOk = true;
            Log.v("test", "close 关闭完成", Log.LEVEL_HIGH);
            return isOk;
        } catch (Exception e) {
            //todo 如果失败了,外部应该再一段时间后，再次关闭摄像头
            throw new RuntimeException("Interrupted while trying to lock camera closing.", e);
        } finally {
            mCameraOpenCloseLock.release();
            mCameraInitCloseLock.release();
        }

    }

    public void release() {
        close();
        if (mParameters != null) {
            mParameters.release();
        }
        mListener = null;
        setState(CameraState.Release);
    }

    @Override
    public boolean focus(@NotNull Point position, boolean manual) {
        if (!super.focus(position, manual)) {
            Log.v("test", "focus CameraState.Running != mState或者 无法聚焦，原因:TextureView未呈现出来", Log.LEVEL_HIGH);
            return false;
        }
        Rect focusRegion = getFocusArea(position);
        if (focusRegion == null) {
            Log.v("test", "focus 聚焦的区域为空", Log.LEVEL_HIGH);

            return false;
        }
        try {
            Camera cam = mCamera;
            if (cam == null) {
                Log.v("test", "focus 相机为空", Log.LEVEL_HIGH);

                return false;
            }
            boolean isFocusAreaSupported = mParameters.isSupportedProtected(ParameterKey.AutoFocusRegion);
            boolean isMeteringAreaSupported = mParameters.isSupportedProtected(ParameterKey.AutoMeteringRegion);

            //两种方式都不支持
            if (!isFocusAreaSupported && !isMeteringAreaSupported) {
                Log.e(TAG, "无法聚焦，原因：可用聚焦区域、测光区域数为0");
                Log.v("test", "focus 无法聚焦，原因：可用聚焦区域、测光区域数为0", Log.LEVEL_HIGH);

                return false;
            }
            mAutoFocusCaptureState = AutoFocusCaptureState.Running;

            //需要先取消之前的聚焦
            cam.cancelAutoFocus();
            Camera.Area focusArea = new Camera.Area(focusRegion, 1000);
            Camera.Parameters parameters = cam.getParameters();
            if (isFocusAreaSupported) {
                parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
                List<Camera.Area> focusAreas = new ArrayList<Camera.Area>();
                focusAreas.add(focusArea);
                parameters.setFocusAreas(focusAreas);
            }

            //暂时不确定测光区域是否有效果
            if (isMeteringAreaSupported) {
                List<Camera.Area> meteringAreas = new ArrayList<Camera.Area>();
                meteringAreas.add(focusArea);
                parameters.setMeteringAreas(meteringAreas);
            }
            cam.setParameters(parameters);
            cam.autoFocus(new Camera.AutoFocusCallback() {
                @Override
                public void onAutoFocus(boolean success, Camera camera) {
                    if (success) {
                        mAutoFocusCaptureState = AutoFocusCaptureState.Complete;
                        camera.cancelAutoFocus();//只有加上了这一句，才会自动对焦。
                        Log.v(TAG, "Camera  focus success.");
                        try {
                            if (CameraState.Running == getState()) {
                                AbsCameraParameter parameter = mParameters;
                                AutoFocusMode afMode = parameter.get(ParameterKey.AutoFocusMode);
                                if (afMode != null) {
                                    mParameters.putAndSync(ParameterKey.AutoFocusMode, afMode);
                                }
                            }
                        } catch (Exception ex) {

                        }
                    }
                }
            });
        } catch (Exception ex) {
            Log.e(ex);
        }
        Log.v("test", "focus 聚焦完成", Log.LEVEL_HIGH);
        return true;
    }

    Rect getFocusArea(Point position) {
        Rect focusArea = null;
        if (mTextureView == null || !mTextureView.isAvailable()) {
            return focusArea;
        }
        int uiWidth = mTextureView.getWidth();
        int uiHeight = mTextureView.getHeight();
        int radius = uiWidth/20;
        Rect uiRect = new Rect(position.x-radius,position.y-radius,position.x+radius,position.y+radius);
        if (uiWidth == 0 || uiHeight == 0) {
            return focusArea;
        }
        Rect bound = new Rect(-1000, -1000, 1000, 1000);//相机设计是这么一块区域
        focusArea = genFocusArea(uiRect, bound, uiWidth, uiHeight);
        Log.i(TAG, "Focus Area:" + focusArea.toString());
        return focusArea;
    }

    @Override
    protected String getLogTagName() {
        return TAG;
    }

    /**
     * params request 要聚焦的区域(UI区域)
     * params photoRegion 成像区域
     * params textureWidth TextureView的宽度
     */
    private Rect genFocusArea(Rect request,Rect photoRegion, int textureWidth,int textureHeight) {
        Rect focusArea = null;
        if (textureWidth == 0 || textureHeight == 0) {
            return focusArea;
        }

        float scaleX = photoRegion.width() / (float) textureWidth;
        float scaleY = photoRegion.height() / (float) textureHeight;
        int focusWidth,focusHeight;

        //todo 是否有明确的方式，获得可用的最大聚焦区域,具体比例为1/(multiple*multiple)
        final int multiple = 10;
        if(request.width() * multiple >textureWidth){
            focusWidth = Math.round(textureWidth * scaleX /multiple);
        }
        else{
            focusWidth = Math.round(request.width() * scaleX);
        }
        if(request.height() * multiple >textureHeight){
            focusHeight = Math.round(textureHeight*scaleY /multiple);
        }
        else{
            focusHeight = Math.round(request.height() * scaleY);
        }
        int centerX = Math.round(scaleX * request.centerX());
        int centerY = Math.round(scaleY * request.centerY());
        int focusLeft = centerX + focusWidth / 2 + photoRegion.left;
        int focusTop = centerY + focusHeight / 2 + photoRegion.top;

        //特殊边界
        if(focusLeft >photoRegion.right){
            focusLeft = photoRegion.right - focusWidth;
        }
        if(focusTop >photoRegion.bottom){
            focusTop = photoRegion.bottom - focusHeight;
        }

        if (focusLeft < photoRegion.left) {
            focusLeft = photoRegion.left;
        }
        if (focusTop < photoRegion.top) {
            focusTop = photoRegion.top;
        }
        if (focusLeft + focusWidth > photoRegion.right) {
            focusWidth = photoRegion.right - focusLeft;
        }
        if (focusTop + focusHeight > photoRegion.bottom) {
            focusHeight = photoRegion.bottom - focusTop;
        }
        Rect area = new Rect(focusLeft, focusTop, focusLeft + focusWidth, focusTop + focusHeight);
        Log.i(getLogTagName(),"对焦区域:" + area.toString());
        return area;
    }
}
