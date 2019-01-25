package com.baoshen.cameralib;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.support.annotation.Keep;

import com.baoshen.cameralib.enums.ParameterKey;
import com.baoshen.cameralib.serialization.AbsParameterSerialization;
import com.baoshen.cameralib.serialization.ParameterSerializationFactory;
import com.baoshen.common.file.FileEx;
import com.baoshen.common.file.FileUtils;
import com.baoshen.common.Log;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.Map;

@Keep
public class CameraImageSaver {
    private static final String TAG = CameraImageSaver.class.getSimpleName();

    public static String savePreview(Context context, AbsCamera camera, AbsCameraImage image, Rect rect,int screenOrientation){
        Bitmap bitmap = image.getRgba(rect,screenOrientation);
        byte[] jpegBuff = null;
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
            jpegBuff = baos.toByteArray();
            baos.close();
        } catch (IOException ex) {
            Log.e(ex);
        }
        bitmap.recycle();
        if (jpegBuff == null) return null;

        File rootFile = FileUtils.getAppDirectory(context,null);
        // 创建并保存图片文件
        long time = System.currentTimeMillis();
        Date date = new Date(time);
        SimpleDateFormat dataFormat = new SimpleDateFormat("yyyy-MM-dd");
        File dirFile = new File(rootFile,"/images/" + dataFormat.format(date) + "/");
        if (!dirFile.exists()) {
            dirFile.mkdirs();
        }
        SimpleDateFormat timeFormat = new SimpleDateFormat("HH-mm-ss_SSS");
        FileOutputStream fos = null;
        String filePath = null;

        File imgFile = new File(dirFile,timeFormat.format(date) + ".jpg");
        if(FileEx.write(jpegBuff, imgFile)){
            filePath = imgFile.getAbsolutePath();
            Map<ParameterKey,Object> map = camera.getParameters().getAll();
            AbsParameterSerialization serialization = ParameterSerializationFactory.create();
            byte[] parameterBuff = serialization.serialize(map);
            File cfgFile = new File(dirFile,timeFormat.format(date) + ".txt");
            FileEx.write(parameterBuff,cfgFile);
        }
        Log.v(TAG, "save image:" + filePath);
        return filePath;
    }
}
