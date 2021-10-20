# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in C:\Users\Administrator\AppData\Local\Android\Sdk/tools/proguard/proguard-android.txt
# You can edit the include path and order by changing the proguardFiles
# directive in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# Add any project specific keep options here:

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Uncomment this to preserve the line number information for
# debugging stack traces.
-keepattributes SourceFile,LineNumberTable #소스파일, 라인 정보 유지

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile


-keep public class **
-keep public class * { public protected *; }
-keep class * { public *; }

-keepclasseswithmembernames class * {
    native <methods>;
}

-keepclasseswithmembernames class * {
    public <init>(android.content.Context, android.util.AttributeSet);
}

-keepclasseswithmembernames class * {
    public <init>(android.content.Context, android.util.AttributeSet, int);
}

-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

-keep class * implements android.os.Parcelable {
  public static final android.os.Parcelable$Creator *;
}

###################
# 이너 클래스 유지 - 안할시 goClass 튕김
###################
-keepattributes InnerClasses

############
# 경고 무시 설정 #
############
#-dontwarn com.sds.**
#-dontwarn com.itholic.**
#-dontwarn com.rimesoft.**
#-dontwarn kr.co.sdk.vguard2.**
#-dontwarn com.kakao.**
#-dontwarn net.ib.asp.**
#-dontwarn org.jboss.netty.**
-dontwarn org.apache.http.**
-dontwarn com.extrus.**

###################
# 외부 라이브러리 난독화 제외 #
###################
#-keep class com.sds.**{*;}
#-keep class com.itholic.**{*;}
#-keep class com.rimesoft.**{*;}
#-keep class kr.co.sdk.vguard2.**{*;}
#-keep class com.kakao.**{*;}
#-keep class net.ib.asp.**{*;}
#-keep class org.jboss.netty.**{*;}
-keep class org.apache.http.** {*;}
-keep class com.extrus.** {*;}
-keep class com.secureland.** {*;}
-keep class com.dreamsecurity.** {*;}
-keep class com.sun.org.apache.** {*;}         # AES256Util
-keep class okhttp3.Interceptor.** {*;}
-keep class com.makeramen.** {*;}
-keep class com.google.** {*;}
-keep class org.greenrobot.** {*;}

##일루텍 촬영솔루션
-keep class org.opencv.** {*;}
-keep class com.bumptech.glide.** {*;}
-keep class com.epost.insu.psmobile.** {*;}
-keep class shilla.sowon.** {*;}

##kftc 서브라이브러리--------------------------
-keep class kr.or.kftc.** {*;}
-keep class org.kftc.** {*;}

-dontwarn com.samsung.**
-keep class com.samsung.** {*;}
-keep class com.samsung.android.** {*;}
-keep interface com.samsung.** {*;}

-dontwarn com.sec.**
-keep class com.sec.**{*;}
-keep interface com.sec.**{*;}

-dontwarn entri.fido.**
-keep class entri.fido.**{*;}
-keep interface entri.fido.**{*;}

#KFTC_SSC_KEYPAD
-dontwarn kr.or.kftc.**
-keep class kr.or.kftc.**{*;}
-keep interface kr.or.kftc.**{*;}

#spongycastle
-dontwarn org.spongycastle.**
-keep  class org.spongycastle.** {*;}
-keep interface org.spongycastle.** {*;}

#raonsecure
-dontwarn com.raonsecure.**
-keep class com.raonsecure.** {*;}
-keep interface com.raonsecure.** {*;}

#raon
-dontwarn com.raon.**
-keep class com.raon.**{*;}
-keep interface com.raon.**{*;}

#Core
-dontwarn com.android.telephony.**
-dontwarn bitdefender.antimalware.**
-dontwarn r.r.r.**
-dontwarn raonsecure.mvaccine.crypto.**
-dontwarn secureland.smartmedic.**
-dontwarn TouchEn.mVaccine.b2b2c.**
-keep class com.android.telephony.**{*;}
-keep class bitdefender.antimalware.**{*;}
-keep class r.r.r.**{*;}
-keep class raonsecure.mvaccine.crypto.**{*;}
-keep class secureland.smartmedic.**{*;}
-keep class TouchEn.mVaccine.b2b2c.**{*;}
-keep interface com.android.telephony.**{*;}
-keep interface bitdefender.antimalware.**{*;}
-keep interface r.r.r.**{*;}
-keep interface raonsecure.mvaccine.crypto.**{*;}
-keep interface secureland.smartmedic.**{*;}
-keep interface TouchEn.mVaccine.b2b2c.**{*;}
##kftc 서브라이브러리--------------------------

#To remove debug logs:
#-assumenosideeffects class android.util.Log {
#    public static *** v(…);
#    public static *** d(…);
#    public static *** i(…);
#    public static *** w(…);
#    public static *** e(…);
#}

-ignorewarnings