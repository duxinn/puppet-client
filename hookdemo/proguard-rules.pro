# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Uncomment this to preserve the line number information for
# debugging stack traces.
#-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile


-keep class com.google.android.material.** {*;}
-keep class androidx.** {*;}
-keep public class * extends androidx.**
-keep interface androidx.** {*;}
-dontwarn com.google.android.material.**
-dontnote com.google.android.material.**
-dontwarn androidx.**

-dontwarn com.android.*
-dontwarn com.google.*
-dontwarn android.app.*

-keep public class com.mango.wechattool.R$*{
	public static final int *;
	public static int *;
}

-keepclassmembers class * {
   public <init>(org.json.JSONObject);
}

-keepclasseswithmembers class * {
  public <init>(android.content.Context);
}

# 注解
-keepattributes *Annotation*
# 异常
-keepattributes Exceptions
# 泛型
-keepattributes Signature
# 反射
-keepattributes EnclosingMethod

# 保留行号
-keepattributes SourceFile,LineNumberTable
-keep class * implements android.os.Parcelable {public static final android.os.Parcelable$Creator *;}

# OkHttp3
-dontwarn okhttp3.logging.**
-keep class okhttp3.internal.**{*;}
-dontwarn okio.**

# Retrofit
-dontwarn retrofit2.**
-keep class retrofit2.** { *; }
-keepattributes Signature
-keepattributes Exceptions

#RxJava/RxAndroid
-dontwarn sun.misc.**
-keepclassmembers class rx.internal.util.unsafe.*ArrayQueue*Field* {
   long producerIndex;
   long consumerIndex;
}

## glide[version 3.7.0]
-keep public class * implements com.bumptech.glide.module.GlideModule
-keep public enum com.bumptech.glide.load.resource.bitmap.ImageHeaderParser$** {  **[] $VALUES;  public *;}
## glide

## gson
-keep public class com.google.gson.**
-keep public class com.google.gson.** {public private protected *;}

## webview
-keepattributes *JavascriptInterface*
-dontwarn android.webkit.WebView

## model
-keepclasseswithmembers public class com.mango.wechattool.bean.**{*;}

-keepclasseswithmembers public class com.mango.wechattool.business.WechatEntrance {
    public static void entrance();
}