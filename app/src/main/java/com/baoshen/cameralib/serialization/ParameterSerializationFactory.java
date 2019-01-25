package com.baoshen.cameralib.serialization;

import android.support.annotation.Keep;

@Keep
public class ParameterSerializationFactory {
    //暂时先用JSON，PB的等实现再使用
    public static AbsParameterSerialization create(){
        return new ProtocolBufferParameterSerialization();
    }
}
