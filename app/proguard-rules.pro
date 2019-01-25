
#-keepclassmembers class * implements java.io.Serializable {
#}
-keepattributes *Annotation*
-keep class * extends java.lang.annotation.Annotation { *; }
-keep interface * extends java.lang.annotation.Annotation { *; }

-keep class com.baoshen.cameralib.enums.** { *; } #枚举不混淆
-keep class com.baoshen.cameralib.AbsCamera {*;} #对外接口不混淆


-keep class com.baoshen.cameralib.pb.** { *; } #pb不混淆

-keep class com.baoshen.cameralib.AutoFitTextureView #自定义控件不混淆

#手动启用support keep注解
-dontskipnonpubliclibraryclassmembers
-printconfiguration
-keep,allowobfuscation @interface android.support.annotation.Keep
-keep @android.support.annotation.Keep class *
-keepclassmembers class * {
    @android.support.annotation.Keep *;
}



