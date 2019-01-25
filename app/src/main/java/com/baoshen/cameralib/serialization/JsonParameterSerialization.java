package com.baoshen.cameralib.serialization;

import com.baoshen.cameralib.enums.IParameterIntValue;
import com.baoshen.cameralib.enums.IParameterStringValue;
import com.baoshen.cameralib.enums.ParameterKey;
import com.baoshen.common.Log;
import com.baoshen.common.io.StreamUtils;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.jetbrains.annotations.NotNull;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Type;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Created by Shute on 2018/10/26.
 */
public class JsonParameterSerialization extends AbsParameterSerialization {

    @Override
    public Map<ParameterKey, Object> deserialize(@NotNull InputStream inputStream) {
        try {

            byte[] buff = StreamUtils.readAll(inputStream);
            String json = new String(buff, Charset.forName("UTF-8"));
            Gson gson = new Gson();
            Type type = new TypeToken<Map<String, String>>() {
            }.getType();
            Map<String, String> strMap = gson.fromJson(json, type);
            Map<ParameterKey, Object> map = new HashMap<>();
            for (Map.Entry<String, String> entry : strMap.entrySet()) {
                ParameterKey key = ParameterKey.get(entry.getKey());
                Object value = stringToObject(key, entry.getValue());
                if (value != null) {
                    map.put(key, value);
                }
            }
            return map;
        } catch (IOException ex) {
            Log.e(ex);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public Map<ParameterKey, Object> deserialize(@NotNull byte[] data) {
        String json = new String(data,Charset.forName("UTF-8"));
        Gson gson = new Gson();
        Type type = new TypeToken<Map<String, String>>() {
        }.getType();
        Map<String, String> strMap = gson.fromJson(json, type);
        Map<ParameterKey, Object> map = new HashMap<>();
        for (Map.Entry<String, String> entry : strMap.entrySet()) {
            ParameterKey key = ParameterKey.get(entry.getKey());
            Object value = stringToObject(key, entry.getValue());
            if (value != null) {
                map.put(key, value);
            }
        }
        return map;
    }

    @Override
    public void serialize(@NotNull InputStream inputStream,@NotNull Map<ParameterKey, Object> map){
        throw new UnsupportedOperationException();
    }

    @Override
    public byte[] serialize(@NotNull Map<ParameterKey, Object> map) {
        final String jsonHead = "{";
        StringBuilder sb = new StringBuilder(1024);
        sb.append(jsonHead);
        Set<Map.Entry<ParameterKey, Object>> set = map.entrySet();
        for (Map.Entry<ParameterKey, Object> item : set) {
            Object itemValue = item.getValue();
            if (itemValue == null) continue;
            String itemValueStr;
            if(itemValue instanceof IParameterStringValue){
                itemValueStr = ((IParameterStringValue) itemValue).getValue();
            }
            else if (itemValue instanceof IParameterIntValue) {
                itemValueStr = String.valueOf(((IParameterIntValue) itemValue).getCode());
            } else {
                itemValueStr = itemValue.toString();
            }
            if (sb.length()>jsonHead.length()) {
                sb.append(",");
            }
            sb.append(String.format("\"%s\":\"%s\"", item.getKey(), itemValueStr));
        }
        sb.append('}');
        return sb.toString().getBytes();
    }

}
