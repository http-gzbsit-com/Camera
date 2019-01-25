package com.baoshen.cameralib;

import android.content.Context;
import android.support.annotation.Keep;

import com.baoshen.cameralib.enums.ParameterKey;
import com.baoshen.cameralib.serialization.AbsParameterSerialization;
import com.baoshen.cameralib.serialization.ParameterSerializationFactory;
import com.baoshen.common.file.DelaySaveQuene;
import com.baoshen.common.file.FileEx;
import com.baoshen.common.file.FileUtils;
import com.baoshen.common.Log;
import com.baoshen.common.file.IDelaySave;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Map;
/**
 * 参数本地缓存策略:从服务器获得参数，如果获取不到，则自动生成。获取到服务器参数后，覆盖现有的参数
 * Created by Shute on 2018/10/27.
 */
@Keep
public class ParameterCacheManager{
    static {
        delaySave = new DelaySaveParameter();
    }

    public static final String FILE_PATH = "CameraParameter.cfg";
    //服务器参数备份(是否有必要将服务器获得的参数配置做额外备份？)
    private static final String BACKUP = "CameraParameter.bak";
    private static DelaySaveParameter delaySave;

    public static void save(final Context context,final Map<ParameterKey,Object> map) {
        delaySave.set(context,map);
        DelaySaveQuene.add(delaySave);
    }

    public static Map<ParameterKey,Object> load(Context context) {
        File dir = FileUtils.getAppDirectory(context, FileUtils.AppFilesDir);
        File file = new File(dir, FILE_PATH);
        if (file.exists()) {
            FileInputStream stream = null;
            try {
                stream = new FileInputStream(file);
                AbsParameterSerialization serialization = ParameterSerializationFactory.create();
                Map<ParameterKey, Object> map = serialization.deserialize(stream);
                return map;
            } catch (IOException ex) {
                Log.e(ex);
            } finally {
                if (stream != null) {
                    try {
                        stream.close();
                    } catch (IOException ex) {
                        Log.e(ex);
                    }
                }
            }
        }
        return null;
    }

    private static class DelaySaveParameter implements IDelaySave{
        public DelaySaveParameter(){

        }
        private Context mContext;
        private Map<ParameterKey,Object> mMap;
        @Override
        public void delaySave() {
            if (mMap != null && mMap.size() > 0 && mContext != null) {
                AbsParameterSerialization serialization = ParameterSerializationFactory.create();
                byte[] buff = serialization.serialize(mMap);
                if (buff == null || buff.length == 0) {
                    return;
                }
                File dir = FileUtils.getAppDirectory(mContext, FileUtils.AppFilesDir);
                File file = new File(dir, FILE_PATH);
                FileEx.write(buff, file);
                mMap = null;
                mContext = null;
            }
        }
        public void set(Context context,Map<ParameterKey,Object>map){
            mMap = map;
            mContext = context;
        }
    }
}
