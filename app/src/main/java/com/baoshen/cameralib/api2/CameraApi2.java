package com.baoshen.cameralib.api2;

import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Matrix;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CameraMetadata;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.CaptureResult;
import android.hardware.camera2.TotalCaptureResult;
import android.hardware.camera2.params.MeteringRectangle;
import android.media.Image;
import android.media.ImageReader;
import android.os.Handler;
import android.os.HandlerThread;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.Range;
import android.view.Surface;
import android.view.TextureView;

import com.baoshen.cameralib.AbsCamera;
import com.baoshen.cameralib.AbsCameraImage;
import com.baoshen.cameralib.AbsCameraParameter;
import com.baoshen.cameralib.AutoFitTextureView;
import com.baoshen.cameralib.CameraUtils;
import com.baoshen.cameralib.ICameraListener;
import com.baoshen.cameralib.ValueUtils;
import com.baoshen.cameralib.enums.AutoExposureMode;
import com.baoshen.cameralib.enums.AutoFocusCaptureState;
import com.baoshen.cameralib.enums.AutoFocusMode;
import com.baoshen.cameralib.enums.AutoFocusState;
import com.baoshen.cameralib.enums.CameraState;
import com.baoshen.cameralib.enums.ParameterKey;
import com.baoshen.common.DisplayUtils;
import com.baoshen.common.Log;
import com.baoshen.common.graphics.Size;

import org.jetbrains.annotations.NotNull;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

/**
 * Created by Shute on 2018/9/26.
 */
public class CameraApi2 extends AbsCamera {
    public CameraApi2(boolean isFront, @NotNull Activity context) {
        super(isFront, context);
    }

    public CameraApi2(@NotNull String cameraId, @NotNull Activity context) {
        super(cameraId, context);
    }

    public static final String TAG = CameraApi2.class.getSimpleName();

    protected Semaphore mCameraOpenCloseLock = new Semaphore(1);//信号量

    protected CameraDevice mCameraDevice;
    protected CameraManager mManager;
    protected CameraDevice.StateCallback mDeviceStateCallback;

    /**
     * This a callback object for the {@link ImageReader}. "onImageAvailable" will be called when a
     * still image is ready to be saved.
     */
    private final ImageReader.OnImageAvailableListener mOnImageAvailableListener = new ImageReader.OnImageAvailableListener() {
        @Override
        public void onImageAvailable(ImageReader reader) {
            long time1 = System.currentTimeMillis();
            Image image = reader.acquireNextImage();//这里不调用reader.acquireNextImage，会卡住
            AbsCameraImage cameraImage = mCameraReader.acquireImageForWrite();
            if (CameraState.Running != getState() || mListener == null || cameraImage == null) {
                if (image != null) {
                    image.close();
                }
                if (cameraImage != null) {
                    cameraImage.setState(AbsCameraImage.ImageCacheState.Written);
                    cameraImage.release();
                }
                return;
            }
            //todo 如果这样的话，要考虑性能优化了
            ByteBuffer buffer = image.getPlanes()[0].getBuffer();
            int imageLength = buffer.remaining();
            Log.v(TAG, "mOnImageAvailableListener Jpeg size:" + imageLength, Log.LEVEL_LOW);
            cameraImage.setLength(imageLength);//设置使用长度
            byte[] data = cameraImage.getBuffer();
            buffer.get(data, 0, imageLength);
            cameraImage.setState(AbsCameraImage.ImageCacheState.Written);
            cameraImage.release();
            image.close();//用完关闭
            mListener.onPreviewFrame(CameraApi2.this, mCameraReader);
            Log.v(TAG, "mOnImageAvailableListener Time:" + (System.currentTimeMillis() - time1), Log.LEVEL_LOW);
        }
    };

    /**
     * A {@link CameraCaptureSession.CaptureCallback} that handles events related to JPEG capture.
     */
    private CameraCaptureSession.CaptureCallback mCaptureCallback = new CameraCaptureSession.CaptureCallback() {
        private void process(CaptureResult result) {
            Log.i(TAG, "Capture process");
            Integer afState = result.get(CaptureResult.CONTROL_AF_STATE);
            if (null == afState) {
                return;
            }

            AutoFocusState autoFocusState = AutoFocusState.get(afState.intValue());
            Log.i(TAG, "focus state: " + autoFocusState);
            if (AutoFocusState.CONTROL_AF_STATE_FOCUSED_LOCKED == autoFocusState ||
                    AutoFocusState.CONTROL_AF_STATE_NOT_FOCUSED_LOCKED == autoFocusState) {
                mPreviewRequestBuilder.set(CaptureRequest.CONTROL_AF_TRIGGER, CameraMetadata.CONTROL_AF_TRIGGER_CANCEL);
                if (mIsAutoFocus) {
                    AutoExposureMode aeMode = mParameters.get(ParameterKey.AutoExposureMode);
                    AutoFocusMode afMode = mParameters.get(ParameterKey.AutoFocusMode);
                    if (afMode != null) {
                        mPreviewRequestBuilder.set(CaptureRequest.CONTROL_AF_MODE, afMode.getCode());
                    }
                    if (aeMode != null) {
                        mPreviewRequestBuilder.set(CaptureRequest.CONTROL_AE_MODE, aeMode.getCode());
                    }
                }
                mAutoFocusCaptureState = AutoFocusCaptureState.Complete;
                mPreviewRequest = mPreviewRequestBuilder.build();
                Log.i(TAG, "manual focus finish");
                try {
                    mCaptureSession.setRepeatingRequest(mPreviewRequest, null, mBackgroundHandler);
                } catch (CameraAccessException e) {
                    Log.e(TAG, "setRepeatingRequest failed, errMsg: " + e.getMessage());
                }
            }
        }

        @Override
        public void onCaptureProgressed(@NonNull CameraCaptureSession session,
                                        @NonNull CaptureRequest request,
                                        @NonNull CaptureResult partialResult) {
            Log.i(TAG, "Capture onCaptureProgressed");
            process(partialResult);
        }

        @Override
        public void onCaptureCompleted(@NonNull CameraCaptureSession session,
                                       @NonNull CaptureRequest request,
                                       @NonNull TotalCaptureResult result) {
            Log.i(TAG, "Capture onCaptureCompleted");
            process(result);
        }
    };

    CameraParameterApi2 mParameters;
    @NotNull
    private CameraCaptureSession mCaptureSession;
    @NotNull
    private CaptureRequest mPreviewRequest;
    @NotNull
    private ImageReader mImageReader;//可以用acquireLatestImage获取预览图片，达到最大后，不能再次使用
    private CaptureRequest.Builder mPreviewRequestBuilder;
    private HandlerThread mBackgroundThread;
    private CameraCharacteristics mCameraCharacteristics;
    /**
     * A {@link Handler} for running tasks in the background.
     */
    private Handler mBackgroundHandler = null;
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
            configureTransform(width, height);
        }

        @Override
        public boolean onSurfaceTextureDestroyed(SurfaceTexture texture) {
            return true;
        }

        @Override
        public void onSurfaceTextureUpdated(SurfaceTexture texture) {
            //内容更新，应该怎么设计处理，是否将整个SurfaceTexture往外发？外部根据需要去处理，这里是否会导致大量内容问题？
            //lockCanvas拿到null，估计得用GL才可以处理
        }
    };
    CameraParameterApi2.ICameraParameterSyncApi2 mParameterSync = new CameraParameterApi2.ICameraParameterSyncApi2() {

        @Override
        public CaptureRequest.Builder getBuilder() {
            if (CameraState.Running == getState() || CameraState.Open == getState()) {
                return mPreviewRequestBuilder;
            }
            return null;
        }

        @Override
        @NotNull
        public CameraCharacteristics getCameraCharacteristics() {
            return mCameraCharacteristics;
        }

        @Override
        public boolean beginSync(Map<ParameterKey, Object> map) {
            return true;
        }

        @Override
        public boolean endSync(Map<ParameterKey, Object> map) {
            boolean isOk = false;
            if (CameraState.Running == getState() || CameraState.Open == getState()) {
                try {
                    mCameraOpenCloseLock.acquire();
                    if (null == mCameraDevice) {
                        mCameraOpenCloseLock.release();
                        android.util.Log.e(TAG, "createCameraPreviewSession: camera isn't opened");
                        return isOk;
                    }
//                    if (null != mCaptureSession) {
//                        mCameraOpenCloseLock.release();
//                        android.util.Log.e(TAG, "createCameraPreviewSession: mCaptureSession is already started");
//                        return isOk;
//                    }
                    mPreviewRequest = mPreviewRequestBuilder.build();
                    mCaptureSession.setRepeatingRequest(mPreviewRequest, null, mBackgroundHandler);
                    isOk = true;

                    ICameraListener listener = mListener;
                    if (mListener != null) {
                        Set<Map.Entry<ParameterKey, Object>> set = map.entrySet();
                        for (Map.Entry<ParameterKey, Object> item : set) {
                            listener.onParameterChanged(CameraApi2.this, item.getKey(), item.getValue());
                        }
                    }
                } catch (InterruptedException ex) {
                    Log.e(ex);
                } catch (CameraAccessException ex) {
                    Log.e(ex);
                } finally {
                    mCameraOpenCloseLock.release();
                }
            }
            return isOk;
        }
    };

    //初始化等，初始化后，才可以获得设备参数
    @Override
    public boolean init(@NotNull AutoFitTextureView textureView, @NotNull ICameraListener listener) {
        try {
            if (!mCameraInitCloseLock.tryAcquire(2500, TimeUnit.MILLISECONDS)) {
                Log.e("CameraState", "Time out waiting to lock camera Initing.");
                return false;
                //throw new RuntimeException("Time out waiting to lock camera Initing.");
            }
        } catch (InterruptedException e) {
            Log.e(e);
        }
        super.init(textureView, listener);
        if (CameraState.Release == getState()) {
            Log.e("CameraState", "camera had released");
            return false;
        }
        //assert CameraState.Release != getState() : "camera had released";
        if (CameraState.Init == getState() || CameraState.WaitingOpen == getState() || CameraState.Open == getState() || CameraState.Running == getState()) {
            return true;
        }
        mTextureView = textureView;

        TextureView.SurfaceTextureListener oldTextureListener = mTextureView.getSurfaceTextureListener();
        assert oldTextureListener == null : "目前不兼容外部设置TextureView.setSurfaceTextureListener";
        mTextureView.setSurfaceTextureListener(mSurfaceTextureListener);
        mParameters = new CameraParameterApi2(mContext, this);
        mCameraCharacteristics = null;
        CameraManager manager = (CameraManager) mContext.getSystemService(Context.CAMERA_SERVICE);
        try {
            if (TextUtils.isEmpty(mCameraId)) {
                String[] idList = manager.getCameraIdList();
                if (idList != null && idList.length > 0) {
                    boolean isFind = false;
                    for (String id : idList) {
                        CameraCharacteristics info = manager.getCameraCharacteristics(id);
                        Integer facing = info.get(CameraCharacteristics.LENS_FACING);
                        if (facing != null) {
                            if (mIsFront) {
                                if (facing == CameraCharacteristics.LENS_FACING_FRONT) {
                                    isFind = true;
                                }
                            } else {
                                if (facing == CameraCharacteristics.LENS_FACING_BACK) {
                                    isFind = true;
                                }
                            }
                        }
                        if (isFind) {
                            this.mCameraId = id;
                            mCameraCharacteristics = info;
                            break;
                        }
                    }
                }
            } else {
                mCameraCharacteristics = manager.getCameraCharacteristics(mCameraId);
                Integer facing = mCameraCharacteristics.get(CameraCharacteristics.LENS_FACING);
                if (facing != null) {
                    mIsFront = facing == CameraCharacteristics.LENS_FACING_FRONT;
                }
            }
        } catch (CameraAccessException ex) {
            return false;
        }
        boolean isOk = mParameters.init(mParameterSync);
        if (isOk) {

            mManager = (CameraManager) mContext.getSystemService(mContext.CAMERA_SERVICE);
            mDeviceStateCallback = new CameraDevice.StateCallback() {
                @Override
                public void onOpened(CameraDevice cameraDevice) {
                    // This method is called when the camera is opened.
                    mCameraOpenCloseLock.release();
                    mCameraDevice = cameraDevice;
                    setState(CameraState.Open);
                    createCameraPreviewSession();
                    Log.i(TAG, "Camera State : onOpened");
                }

                @Override
                public void onDisconnected(CameraDevice cameraDevice) {
                    close();
                    Log.i(TAG, "Camera State : onDisconnected");
                }

                public void onClosed(@NonNull CameraDevice camera) {
                    Log.i(TAG, "Camera State : onClosed");
                }

                @Override
                public void onError(CameraDevice cameraDevice, int i) {
                    mCameraOpenCloseLock.release();
                    cameraDevice.close();
                    Log.i(TAG, "Camera State : onError");
                    mCameraDevice = null;
                    if (CameraState.WaitingOpen == getState() || CameraState.Open == getState() || CameraState.Running == getState()) {
                        setState(CameraState.Close);
                    }
                }
            };
            setState(CameraState.Init);
        }
        return isOk;
    }

    @Override
    public AbsCameraParameter getParameters() {
        assert CameraState.None != getState() && CameraState.Close != getState() && CameraState.Release != getState() : "请先初始化设备";
        return mParameters;
    }

    @Override
    public int getApiLevel() {
        return 2;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean open() {
        if (CameraState.Open == getState() || CameraState.Running == getState()) {
            return true;
        }
        try {
            if (!mCameraOpenCloseLock.tryAcquire(2500, TimeUnit.MILLISECONDS)) {
                Log.e(TAG, "Time out waiting to lock camera opening.");
                Log.v("test", "Time out waiting to lock camera opening.");
                return false;
                //throw new RuntimeException("Time out waiting to lock camera opening.");
            }
            Log.i(TAG, "openCamera");
            //assert CameraState.Init == getState(): "相机状态不对，错误的调用";
            if (CameraState.Init == getState() || CameraState.WaitingOpen == getState()) {
                startBackgroundThread();
                mPreviewSize = mParameters.get(ParameterKey.PreviewSize);
                if (mPreviewSize == null) {
                    Log.e(TAG, "需要设置预览尺寸");
                    Log.v("test", "需要设置预览尺寸");
                    return false;
                }
                Integer screenOrientation = mParameters.get(ParameterKey.PreviewOrientation);
                if (screenOrientation == null) {
                    Log.e(TAG, "必须设置屏幕方向");
                    Log.v("test", "必须设置屏幕方向");
                    return false;
                }

                //todo 如果后续需要支持TextureView绘图，则要调整下方逻辑，直接用setSurfaceTextureListener
                if (!mTextureView.isAvailable()) {
                    setState(CameraState.WaitingOpen);
                    return true;
                }
                int width = mTextureView.getWidth();
                int height = mTextureView.getHeight();
                setUpCameraOutputs(width, height);
                configureTransform(width, height);
                boolean isOk = false;

                mManager.openCamera(mCameraId, mDeviceStateCallback, null);
                Log.i(TAG, "Camera State : " + getState());
                isOk = true;

                if (!isOk) {
                    setState(CameraState.Init);
                }
                return isOk;
            } else {
                Log.e(TAG, "相机状态不对，错误的调用");
                Log.v("test", "相机状态不对，错误的调用");
                return false;
            }
        } catch (CameraAccessException e) {
            Log.e(e);
            return false;
        } catch (InterruptedException e) {
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
        Log.i(TAG, "closeCamera");
        mAutoFocusCaptureState = null;
        if (CameraState.None == getState() || CameraState.Close == getState() || CameraState.Release == getState()) {
            return true;
        }
        boolean isOk;
        try {
            mCameraOpenCloseLock.acquire();
            mIsAutoFocus = false;
            if (null != mCaptureSession) {
                mCaptureSession.close();
                mCaptureSession = null;
            }
            if (null != mCameraDevice) {
                mCameraDevice.close();
                mCameraDevice = null;
            }
            if (null != mImageReader) {
                mImageReader.close();
                mImageReader = null;
            }
            if (null != mCameraReader) {
                mCameraReader.close();
                mCameraReader = null;
            }
            mPreviewRequestBuilder = null;
            mPreviewRequest = null;
            setState(CameraState.Close);
            mParameters.close();

            isOk = true;
        } catch (InterruptedException e) {
            //todo 如果失败了,外部应该再一段时间后，再次关闭摄像头
            throw new RuntimeException("Interrupted while trying to lock camera closing.", e);
        } finally {

            mCameraOpenCloseLock.release();
        }
        if (mBackgroundThread != null)
            stopBackgroundThread();
        mCameraInitCloseLock.release();
        Log.d("CameraState", "mCameraInitCloseLock.release();");
        return isOk;
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
            return false;
        }

        Range<Integer> afRegionRange = mParameters.getSupportedRange(ParameterKey.AutoFocusRegion);
        Range<Integer> aeRegionRange = mParameters.getSupportedRange(ParameterKey.AutoExposureRegion);
        boolean isSupportAfRegion = afRegionRange != null && afRegionRange.getUpper().intValue() > 0;
        boolean isSupportAeRegion = aeRegionRange != null && aeRegionRange.getUpper().intValue() > 0;
        if (!isSupportAeRegion && !isSupportAfRegion) {
            Log.e(TAG, "无法聚焦，原因:聚焦区域数为0");
        }

        //先尝试拿到裁剪区域
        Rect activeArrayRect = mCameraCharacteristics.get(CameraCharacteristics.SENSOR_INFO_ACTIVE_ARRAY_SIZE);
        if (null == activeArrayRect) {
            Log.e(TAG, "无法聚焦，原因:SENSOR_INFO_ACTIVE_ARRAY_SIZE为空");
        }
        mAutoFocusCaptureState = AutoFocusCaptureState.Running;
        Log.i(TAG, "相机感光(成像区域):" + activeArrayRect);
        Integer zoom = mParameters.get(ParameterKey.Zoom);
        Rect previewRegion = getFixedCropRegion(mCameraCharacteristics, mPreviewSize, zoom == null ? 100 : zoom.intValue());

        double percentX = ((double) position.x) / mTextureView.getWidth();
        double percentY = ((double) position.y) / mTextureView.getHeight();
        int cameraOrientation = getCameraOrientation();
        int screenOrientation = DisplayUtils.getScreenOrientation((Activity) mContext);
        int orientation = CameraUtils.getCameraDisplayOrientation(cameraOrientation, screenOrientation, isFront());
        double rotatedPercentX, rotatedPercentY;
        switch (orientation) {
            case 90:
                rotatedPercentX = percentY;
                rotatedPercentY = 1.0 - percentX;
                break;
            case 180:
                rotatedPercentX = 1.0 - percentX;
                rotatedPercentY = 1.0 - percentY;
                break;
            case 270:
                rotatedPercentX = 1.0 - percentY;
                rotatedPercentY = percentX;
                break;
            default://0
                rotatedPercentX = percentX;
                rotatedPercentY = percentY;
                break;
        }
        int centerX = (int) Math.floor(previewRegion.width() * rotatedPercentX);
        int centerY = (int) Math.floor(previewRegion.height() * rotatedPercentY);

        int regionWidth = previewRegion.width() / 10;
        int regionHeight = previewRegion.height() / 10;
        int left = previewRegion.left + centerX - regionWidth / 2;
        int right = left + regionWidth;
        int top = previewRegion.top + centerY - regionHeight / 2;
        int bottom = top + regionHeight;
        left = ValueUtils.clamp(left, previewRegion.left, previewRegion.right - regionWidth);
        right = ValueUtils.clamp(right, previewRegion.left + regionWidth, previewRegion.right);
        top = ValueUtils.clamp(top, previewRegion.top, previewRegion.bottom - regionHeight);
        bottom = ValueUtils.clamp(bottom, previewRegion.top + regionHeight, previewRegion.bottom);
        Rect rect = new Rect(left, top, right, bottom);

        //（聚焦区域是基于SENSOR_INFO_ACTIVE_ARRAY_SIZE坐标系的）
        //当只设置一个测光、聚焦区域时，权重设置为非0即可(0将被忽略)
        //裁减区域外的聚焦区域将被忽略
        MeteringRectangle[] areas = mPreviewRequestBuilder.get(CaptureRequest.CONTROL_AF_REGIONS);
        if (isSupportAfRegion) {
            mPreviewRequestBuilder.set(CaptureRequest.CONTROL_AF_REGIONS, new MeteringRectangle[]{new MeteringRectangle(rect, 1000)});
        }
        if (isSupportAeRegion) {
            mPreviewRequestBuilder.set(CaptureRequest.CONTROL_AE_REGIONS, new MeteringRectangle[]{new MeteringRectangle(rect, 1000)});
        }
        mPreviewRequestBuilder.set(CaptureRequest.CONTROL_AF_MODE, CaptureRequest.CONTROL_AF_MODE_AUTO);
        mPreviewRequestBuilder.set(CaptureRequest.CONTROL_AF_TRIGGER, CameraMetadata.CONTROL_AF_TRIGGER_START);
        mPreviewRequestBuilder.set(CaptureRequest.CONTROL_AE_PRECAPTURE_TRIGGER, CameraMetadata.CONTROL_AE_PRECAPTURE_TRIGGER_START);

        mPreviewRequest = mPreviewRequestBuilder.build();
        try {
            mCaptureSession.setRepeatingRequest(mPreviewRequest, mCaptureCallback, mBackgroundHandler);
            return true;
        } catch (CameraAccessException e) {
            Log.e(TAG, "setRepeatingRequest failed, " + e.getMessage());
            return false;
        }
    }

    private void createCameraPreviewSession() {
        try {
            SurfaceTexture texture = mTextureView.getSurfaceTexture();

//                texture.setOnFrameAvailableListener(new SurfaceTexture.OnFrameAvailableListener() {
//                    @Override
//                    public void onFrameAvailable(SurfaceTexture surfaceTexture) {
//                        Log.i(TAG,"frame view");
//                    }
//                });
            assert texture != null : "texture不应该为空";

            // We configure the size of default buffer to be the size of camera preview we want.
            //实际测试中，如果传入非预览尺寸列表提供的规格，内部会自动适配其他尺寸
            texture.setDefaultBufferSize(mPreviewSize.getWidth(), mPreviewSize.getHeight());
            mPreviewFormat = mParameters.get(ParameterKey.PreviewFormat);
            assert mPreviewFormat != null : "必须设置预览格式";
            //实际可用图片数为n+1,也就是3张。
            mImageReader = ImageReader.newInstance(mPreviewSize.getWidth(), mPreviewSize.getHeight(), mPreviewFormat.getCode(), /*maxImages*/2);
            mCameraReader = new CameraReaderApi2(mPreviewSize.getWidth(), mPreviewSize.getHeight(), mPreviewFormat,
                    2, mParameters.getCameraOrientation(), mIsFront);
            mImageReader.setOnImageAvailableListener(mOnImageAvailableListener, mBackgroundHandler);
            AutoFocusMode focusMode = mParameters.get(ParameterKey.AutoFocusMode);
            mIsAutoFocus = focusMode == AutoFocusMode.CONTROL_AF_MODE_CONTINUOUS_PICTURE
                    || focusMode == AutoFocusMode.CONTROL_AF_MODE_CONTINUOUS_VIDEO;

            // This is the output Surface we need to start preview.
            Surface surface = new Surface(texture);

            // We set up a CaptureRequest.Builder with the output Surface.
            mPreviewRequestBuilder
                    = mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
            mPreviewRequestBuilder.addTarget(surface);//预览的
            mPreviewRequestBuilder.addTarget(mImageReader.getSurface());//预览数据回调的

            // Here, we create a CameraCaptureSession for camera preview.
            mCameraDevice.createCaptureSession(Arrays.asList(surface, mImageReader.getSurface()),
                    new CameraCaptureSession.StateCallback() {
                        @Override
                        public void onConfigured(@NonNull CameraCaptureSession cameraCaptureSession) {
                            // The camera is already closed
                            if (null == mCameraDevice) {
                                return;
                            }

                            // When the session is ready, we start displaying the preview.
                            mCaptureSession = cameraCaptureSession;
                            // Auto focus should be continuous for camera preview.

                            //修改摄像机参数时，会启动预览
                            mParameters.open();
                            mParameters.syncAll();
                            setState(CameraState.Running);
                        }

                        @Override
                        public void onConfigureFailed(@NonNull CameraCaptureSession cameraCaptureSession) {
                            Log.e(TAG, "configure failed");
                        }
                    }, null
            );
        } catch (CameraAccessException e) {
            Log.e(e);
        }
    }

    /**
     * Configures the necessary {@link android.graphics.Matrix} transformation to `mTextureView`.
     * This method should be called after the camera preview size is determined in
     * setUpCameraOutputs and also the size of `mTextureView` is fixed.
     *
     * @param viewWidth  The width of `mTextureView`
     * @param viewHeight The height of `mTextureView`
     */
    private void configureTransform(int viewWidth, int viewHeight) {
        if (mParameters == null || null == mTextureView || null == mPreviewSize) {
            return;
        }
        int rotation = DisplayUtils.getScreenOrientation(mContext);
        Matrix matrix = new Matrix();
        RectF viewRect = new RectF(0, 0, viewWidth, viewHeight);
        float centerX = viewRect.centerX();
        float centerY = viewRect.centerY();
        //todo 这种方式，后续要确认是否合理,这里会影响到后续的截图处理吧
        if (90 == rotation || 270 == rotation) {
            RectF bufferRect = new RectF(0, 0, mPreviewSize.getHeight(), mPreviewSize.getWidth());
            bufferRect.offset(centerX - bufferRect.centerX(), centerY - bufferRect.centerY());
            matrix.setRectToRect(viewRect, bufferRect, Matrix.ScaleToFit.FILL);
            float scale = Math.max(
                    (float) viewHeight / mPreviewSize.getHeight(),
                    (float) viewWidth / mPreviewSize.getWidth());
            matrix.postScale(scale, scale, centerX, centerY);
            matrix.postRotate((rotation - 180), centerX, centerY);
        } else if (180 == rotation) {
            matrix.postRotate(180, centerX, centerY);
        }
        mTextureView.setTransform(matrix);
    }

    /**
     * Sets up member variables related to camera.
     *
     * @param width  The width of available size for camera preview
     * @param height The height of available size for camera preview
     */
    @SuppressWarnings("SuspiciousNameCombination")
    private void setUpCameraOutputs(int width, int height) {
        if (mParameters == null) {
            return;
        }
        Activity activity = mContext;

        // We fit the aspect ratio of TextureView to the size of preview we picked.
        int orientation = activity.getResources().getConfiguration().orientation;
        if (orientation == Configuration.ORIENTATION_LANDSCAPE) {

            mTextureView.setAspectRatio(mPreviewSize.getWidth(), mPreviewSize.getHeight());
        } else {
            mTextureView.setAspectRatio(mPreviewSize.getHeight(), mPreviewSize.getWidth());
        }
    }

    /**
     * Starts a background thread and its {@link Handler}.
     */
    private void startBackgroundThread() {
        mBackgroundThread = new HandlerThread("CameraBackground");
        mBackgroundThread.start();
        mBackgroundHandler = new Handler(mBackgroundThread.getLooper());
    }

    /**
     * Stops the background thread and its {@link Handler}.
     */
    private void stopBackgroundThread() {
        mBackgroundThread.quitSafely();
        try {
            mBackgroundThread.join();
            mBackgroundThread = null;
            mBackgroundHandler = null;
        } catch (InterruptedException e) {
            Log.e(e);
        }
    }

    //根据缩放计算出最佳裁减区域
    Rect getFixedCropRegion(CameraCharacteristics characteristics,Size previewSize,int zoom) {
        Rect activeRegion = characteristics.get(CameraCharacteristics.SENSOR_INFO_ACTIVE_ARRAY_SIZE);
        int cameraOrientation = getCameraOrientation();
        int screenOrientation = DisplayUtils.getScreenOrientation((Activity) mContext);
        int orientation = CameraUtils.getCameraDisplayOrientation(cameraOrientation, screenOrientation, isFront());
        Size outSize = new Size(activeRegion.width(), activeRegion.height());
        double zoomRate = ((double) zoom) / 100f;
        Size virtualSize;
        Size rotatedPreviewSize;
        if (orientation % 180 == 90) {
            virtualSize = new Size(activeRegion.height(), activeRegion.width());
            rotatedPreviewSize = new Size(previewSize.getHeight(),previewSize.getWidth());
        } else {
            virtualSize = outSize;
            rotatedPreviewSize = previewSize;
        }

        float maxZoom = characteristics.get(CameraCharacteristics.SCALER_AVAILABLE_MAX_DIGITAL_ZOOM);
        int minWidth = (int) Math.floor(virtualSize.getWidth() / maxZoom);
        int minHeight = (int) Math.floor(virtualSize.getHeight() / maxZoom);

        double cropWidth = virtualSize.getWidth() / zoomRate;
        double cropHeight = virtualSize.getHeight() / zoomRate;

        double activeRatio = cropWidth / cropHeight;
        double previewRatio = ((double) rotatedPreviewSize.getWidth()) / rotatedPreviewSize.getHeight();
        int fixWidth, fixHeight;
        if (activeRatio - previewRatio >= 0.000001) {
            //水平方向上，左边会被裁减掉
            fixHeight = (int) Math.floor(cropHeight);
            fixWidth = (int) Math.floor(cropHeight * previewRatio);
        } else {
            fixWidth = (int) Math.floor(cropWidth);
            fixHeight = (int) Math.floor(cropWidth / previewRatio);
        }
        if (fixWidth < minWidth) {
            fixWidth = minWidth;
        }
        if (fixHeight < minHeight) {
            fixHeight = minHeight;
        }
        int left = (virtualSize.getWidth() - fixWidth) / 2;
        int top = (virtualSize.getHeight()- fixHeight) / 2;
        Rect region;
        if (orientation % 180 == 90) {
            region = new Rect(top, left, top + fixHeight, left + fixWidth);
        }
        else{
            region = new Rect(left, top, left + fixWidth, top + fixHeight);
        }
        return region;
    }

    @Override
    protected String getLogTagName() {
        return TAG;
    }
}
