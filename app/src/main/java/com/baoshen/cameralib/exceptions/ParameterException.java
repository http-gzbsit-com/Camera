package com.baoshen.cameralib.exceptions;

import android.support.annotation.Keep;

import com.baoshen.cameralib.enums.ParameterKey;

/**
 * Created by Shute on 2018/9/26.
 */
@Keep
public class ParameterException extends Exception {
    ParameterKey key;
    public ParameterException(ParameterKey key,String message){
        super(message);

    }

    public ParameterKey getKey() {
        return key;
    }
}
