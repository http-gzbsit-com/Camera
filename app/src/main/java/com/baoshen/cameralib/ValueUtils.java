package com.baoshen.cameralib;

import android.support.annotation.Keep;

/**
 * Created by Shute on 2018/9/30.
 */
@Keep
public class ValueUtils {
    public static <T>  int toInt(T value) {
        if (value instanceof Integer) {
            return (Integer) value;
        } else if (value instanceof Float) {
            return Math.round(((Float) value).floatValue());
        } else if (value instanceof Long) {
            return (int) ((Long) value).longValue();
        } else if (value instanceof Double) {
            return (int) Math.round(((Double) value).doubleValue());
        } else if (value instanceof String) {
            return Integer.valueOf((String) value);
        } else if (value != null) {
            return Integer.valueOf(value.toString());
        }
        else{
            throw new IllegalArgumentException();
        }
    }
    public static <T>  long toLong(T value) {
        if (value instanceof Integer) {
            return (Integer) value;
        } else if (value instanceof Float) {
            return Math.round(((Float) value).floatValue());
        } else if (value instanceof Long) {
            return ((Long) value).longValue();
        } else if (value instanceof Double) {
            return  Math.round(((Double) value).doubleValue());
        } else if (value instanceof String) {
            return Long.valueOf((String) value);
        } else if (value != null) {
            return Long.valueOf(value.toString());
        }
        else{
            throw new IllegalArgumentException();
        }
    }

    public static <T>  float toFloat(T value) {
        if (value instanceof Integer) {
            return (float) ((Integer) value).intValue();
        } else if (value instanceof Float) {
            return ((Float) value).floatValue();
        } else if (value instanceof Long) {
            return (float) ((Long) value).longValue();
        } else if (value instanceof Double) {
            return (float) (((Double) value).doubleValue());
        } else if (value instanceof String) {
            return Float.valueOf((String) value);
        } else if (value != null) {
            return Float.valueOf(value.toString());
        }
        else{
            throw new IllegalArgumentException();
        }
    }

    public static int clamp(int value,int min,int max){
        if(value<min) return min;
        if(value>max) return max;
        return value;
    }
}
