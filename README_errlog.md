# com.epost.insu
안드로이드 구글코솔 에러 처리


## java.lang.IllegalStateException / com.epost.insu.activity.Activity_Default.onCreate / 61.apk / 1,081건 / 518명
java.lang.RuntimeException:
  at android.app.ActivityThread.performLaunchActivity (ActivityThread.java:2858)
  at android.app.ActivityThread.handleLaunchActivity (ActivityThread.java:2933)
  at android.app.ActivityThread.-wrap11 (Unknown Source)
  at android.app.ActivityThread$H.handleMessage (ActivityThread.java:1612)
  at android.os.Handler.dispatchMessage (Handler.java:105)
  at android.os.Looper.loop (Looper.java:164)
  at android.app.ActivityThread.main (ActivityThread.java:6710)
  at java.lang.reflect.Method.invoke (Native Method)
  at com.android.internal.os.Zygote$MethodAndArgsCaller.run (Zygote.java:240)
  at com.android.internal.os.ZygoteInit.main (ZygoteInit.java:770)
Caused by: java.lang.IllegalStateException:
  at android.app.Activity.onCreate (Activity.java:986)
  at androidx.core.app.ComponentActivity.onCreate (ComponentActivity.java:85)
  at androidx.activity.ComponentActivity.onCreate (ComponentActivity.java:149)
  at androidx.fragment.app.FragmentActivity.onCreate (FragmentActivity.java:313)
  at androidx.appcompat.app.AppCompatActivity.onCreate (AppCompatActivity.java:115)
  at com.epost.insu.activity.Activity_Default.onCreate (Activity_Default.kt:58)

  -- 투명액티비티에서 발생함
  1. at com.epost.insu.activity.auth.IUPC10M00.onCreate (IUPC10M00.kt:72)
  2. at com.epost.insu.activity.auth.IUPC95M20.onCreate (IUPC95M20.kt:101)
  3. at com.epost.insu.activity.auth.IUPC95M90.onCreate (IUPC95M90.kt:135)
  4. at com.epost.insu.activity.auth.IUFC10M00.onCreate (IUFC10M00.kt:96)

  at android.app.Activity.performCreate (Activity.java:6980)
  at android.app.Instrumentation.callActivityOnCreate (Instrumentation.java:1214)
  at android.app.ActivityThread.performLaunchActivity (ActivityThread.java:2811)
  at android.app.ActivityThread.handleLaunchActivity (ActivityThread.java:2933)
  at android.app.ActivityThread.-wrap11 (Unknown Source)
  at android.app.ActivityThread$H.handleMessage (ActivityThread.java:1612)
  at android.os.Handler.dispatchMessage (Handler.java:105)
  at android.os.Looper.loop (Looper.java:164)
  at android.app.ActivityThread.main (ActivityThread.java:6710)
  at java.lang.reflect.Method.invoke (Native Method)
  at com.android.internal.os.Zygote$MethodAndArgsCaller.run (Zygote.java:240)
  at com.android.internal.os.ZygoteInit.main (ZygoteInit.java:770)
    -- 안드로이드 sdk26(8.0) 버전의 투명액티비티에서만 발생함
    -- 투명액티비티에 대한 sdk26 처리 문제로 이전/이후 버전은 영향이 없음
 * 1.6.1    NJM_20210708    [투명액티비티 오류] SDK26(8.0)에서 AndroidManifest.xml 투명액티비티 screenOrientation 문제 수정 portrait -> unspecified


## java.lang.NullPointerException  / com.epost.insu.g.d.a / 52.apk / 14,474 / 3,289
java.lang.RuntimeException:
  at android.app.ActivityThread.handleMakeApplication (ActivityThread.java:7506)
  at android.app.ActivityThread.handleBindApplication (ActivityThread.java:7440)
  at android.app.ActivityThread.access$1500 (ActivityThread.java:301)
  at android.app.ActivityThread$H.handleMessage (ActivityThread.java:2148)
  at android.os.Handler.dispatchMessage (Handler.java:106)
  at android.os.Looper.loop (Looper.java:246)
  at android.app.ActivityThread.main (ActivityThread.java:8506)
  at java.lang.reflect.Method.invoke (Native Method)
  at com.android.internal.os.RuntimeInit$MethodAndArgsCaller.run (RuntimeInit.java:602)
  at com.android.internal.os.ZygoteInit.main (ZygoteInit.java:1130)
Caused by: java.lang.NullPointerException:
  at com.epost.insu.g.d.a (Fido2RegistableAuthTech.java:1)
  at com.epost.insu.CustomApplication.onCreate (CustomApplication.java:12)
  at android.app.Instrumentation.callApplicationOnCreate (Instrumentation.java:1192)
  at android.app.ActivityThread.handleMakeApplication (ActivityThread.java:7501)
 * 1.5.3    NJM_20210405    [FIDO호출 로직 변경] 에러발생으로인한 변경