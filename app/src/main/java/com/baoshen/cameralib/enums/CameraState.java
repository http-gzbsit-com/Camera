package com.baoshen.cameralib.enums;

/**
 * Created by Shute on 2018/9/22.
 */
public enum CameraState {
    None(-1),
    Init(0),
    //由于部分条件未满足，不能直接Open，处于等待状态
    WaitingOpen(1),
    //正在打开
    Open(2),
    //打开完成，正在运行
    Running(3),
    Close(4),
    Release(100);

    private CameraState(int code) {
        this.code = code;
    }

    private int code;

    public int getCode() {
        return code;
    }
}
