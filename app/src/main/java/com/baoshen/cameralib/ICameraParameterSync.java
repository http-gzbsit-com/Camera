package com.baoshen.cameralib;

import com.baoshen.cameralib.enums.ParameterKey;

import java.util.Map;

public interface ICameraParameterSync {
    boolean beginSync(Map<ParameterKey, Object> map);
    boolean endSync(Map<ParameterKey, Object> map);
}
