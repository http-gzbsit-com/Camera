package com.baoshen.cameralib.enums;

/**
 * Created by Shute on 2018/9/28.
 */
public enum CameraWorkingState {

    /**
     * Camera state: Showing camera preview.
     */
    STATE_PREVIEW(0),

    /**
     * Camera state: Waiting for the focus to be locked.
     */
    STATE_WAITING_LOCK(1),

    /**
     * Camera state: Waiting for the exposure to be precapture state.
     */
    STATE_WAITING_PRECAPTURE(2),

    /**
     * Camera state: Waiting for the exposure state to be something other than precapture.
     */
    STATE_WAITING_NON_PRECAPTURE(3),

    /**
     * Camera state: Picture was taken.
     */
    STATE_PICTURE_TAKEN(4);

    private CameraWorkingState(int code) {
        this.code = code;
    }

    private int code;

    public int getCode() {
        return code;
    }

    public static CameraWorkingState get(int code) {
        for (CameraWorkingState item : CameraWorkingState.values()) {
            if (item.code == code) {
                return item;
            }
        }
        throw new IllegalArgumentException();
    }
}
