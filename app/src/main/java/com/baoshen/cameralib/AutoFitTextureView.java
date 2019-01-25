package com.baoshen.cameralib;

import android.app.Activity;
import android.content.Context;
import android.graphics.Matrix;
import android.os.Looper;
import android.support.annotation.Keep;
import android.util.AttributeSet;
import android.view.TextureView;

@Keep
public class AutoFitTextureView extends TextureView {

    private int mRatioWidth = 0;
    private int mRatioHeight = 0;
    private Context mContext;

    public AutoFitTextureView(Context context) {
        this(context, null);
        mContext=context;
    }

    public AutoFitTextureView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
        mContext=context;
    }

    public AutoFitTextureView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        mContext=context;
    }

    /**
     * Sets the aspect ratio for this view. The size of the view will be measured based on the ratio
     * calculated from the parameters. Note that the actual sizes of parameters don't matter, that
     * is, calling setAspectRatio(2, 3) and setAspectRatio(4, 6) make the same result.
     *
     * @param width  Relative horizontal size
     * @param height Relative vertical size
     */
    public void setAspectRatio(int width, int height) {
        if (width < 0 || height < 0) {
            throw new IllegalArgumentException("Size cannot be negative.");
        }
        mRatioWidth = width;
        mRatioHeight = height;
        if (Looper.getMainLooper() == Looper.myLooper()) {
            requestLayout();
        } else {
            Runnable runnable = new Runnable() {
                @Override
                public void run() {
                    try {
                        requestLayout();
                    } catch (Exception ex) {

                    }
                }
            };
            ((Activity) mContext).runOnUiThread(runnable);
        }
    }

    @Override
    public void setTransform(Matrix matrix) {
    if (Looper.getMainLooper() == Looper.myLooper()) {
        super.setTransform(matrix);
    } else {
        final Matrix finalMatrix = matrix;
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                try {
                    AutoFitTextureView.super.setTransform(finalMatrix);
                } catch (Exception ex) {

                }
            }
        };
        ((Activity) mContext).runOnUiThread(runnable);
    }
}

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int width = MeasureSpec.getSize(widthMeasureSpec);
        int height = MeasureSpec.getSize(heightMeasureSpec);
        if (0 == mRatioWidth || 0 == mRatioHeight) {
            setMeasuredDimension(width, height);
        } else {
            if (width < height * mRatioWidth / mRatioHeight) {
                setMeasuredDimension(width, width * mRatioHeight / mRatioWidth);
            } else {
                setMeasuredDimension(height * mRatioWidth / mRatioHeight, height);
            }
        }
    }

}
