package com.baoshen.cameralib.serialization;

import com.baoshen.cameralib.enums.IParameterIntValue;
import com.baoshen.cameralib.enums.IParameterStringValue;
import com.baoshen.cameralib.enums.ParameterKey;
import com.baoshen.cameralib.pb.PbCameraParameterOuterClass;
import com.baoshen.cameralib.pb.PbKeyValueOuterClass;
import com.baoshen.common.Log;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by Shute on 2018/10/26.
 */
public class ProtocolBufferParameterSerialization extends AbsParameterSerialization {

    @Override
    public Map<ParameterKey, Object> deserialize(InputStream inputStream) {

        try {
            PbCameraParameterOuterClass.PbCameraParameter pbResult = PbCameraParameterOuterClass.PbCameraParameter.parseFrom(inputStream);

            if (pbResult.getIsSuccess()) {
                List<PbKeyValueOuterClass.PbKeyValue> pbKeyValues = pbResult.getParametersList();
                Map<ParameterKey, Object> map = new HashMap<>();
                for (int i = 0; i < pbKeyValues.size(); i++) {
                    PbKeyValueOuterClass.PbKeyValue pbKeyValue = pbKeyValues.get(i);
                    ParameterKey key = ParameterKey.get(pbKeyValue.getKey());
                    Object value = stringToObject(key, pbKeyValue.getValue());
                    if (value != null) {
                        map.put(key, value);
                    }
                }
                return map;
            }
        } catch (IOException e) {
            Log.e(e);
        }
        return null;
    }

    @Override
    public Map<ParameterKey, Object> deserialize(byte[] data) {
        try {
            PbCameraParameterOuterClass.PbCameraParameter pbResult = PbCameraParameterOuterClass.PbCameraParameter.parseFrom(data);

            if (pbResult.getIsSuccess()) {
                List<PbKeyValueOuterClass.PbKeyValue> pbKeyValues = pbResult.getParametersList();
                Map<ParameterKey, Object> map = new HashMap<>();
                for (int i = 0; i < pbKeyValues.size(); i++) {
                    PbKeyValueOuterClass.PbKeyValue pbKeyValue = pbKeyValues.get(i);
                    ParameterKey key = ParameterKey.get(pbKeyValue.getKey());
                    Object value = stringToObject(key, pbKeyValue.getValue());
                    if (value != null) {
                        map.put(key, value);
                    }
                }
                return map;
            }
        } catch (IOException e) {
            Log.e(e);
        }
        return null;
    }

    @Override
    public void serialize(InputStream inputStream, Map<ParameterKey, Object> map) {
        throw new UnsupportedOperationException();
    }

    @Override
    public byte[] serialize(Map<ParameterKey, Object> map) {
        try {
            PbCameraParameterOuterClass.PbCameraParameter.Builder builder = PbCameraParameterOuterClass.PbCameraParameter.newBuilder();
            Set<Map.Entry<ParameterKey, Object>> set = map.entrySet();
            for (Map.Entry<ParameterKey, Object> item : set) {
                Object itemValue = item.getValue();
                if (itemValue == null) continue;
                String itemValueStr;
                if (itemValue instanceof IParameterStringValue) {
                    itemValueStr = ((IParameterStringValue) itemValue).getValue();
                } else if (itemValue instanceof IParameterIntValue) {
                    itemValueStr = String.valueOf(((IParameterIntValue) itemValue).getCode());
                } else {
                    itemValueStr = itemValue.toString();
                }
                PbKeyValueOuterClass.PbKeyValue.Builder value = PbKeyValueOuterClass.PbKeyValue.newBuilder();
                value.setKey(item.getKey().toString());
                value.setValue(itemValueStr);
                builder.addParameters(value);
            }
            builder.setIsSuccess(true);
            PbCameraParameterOuterClass.PbCameraParameter pbCameraParameter = builder.build();
            ByteArrayOutputStream outStream = new ByteArrayOutputStream();
            pbCameraParameter.writeTo(outStream);
            return outStream.toByteArray();
        } catch (IOException e) {
            Log.e(e);
        }
        return null;
    }
}