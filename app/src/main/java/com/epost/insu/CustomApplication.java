package com.epost.insu;

import android.app.Activity;
import android.app.Application;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.webkit.WebView;
import android.widget.Toast;

import com.epost.insu.activity.IUCOA0M00;
import com.epost.insu.common.CommonFunction;
import com.epost.insu.common.LogPrinter;
import com.epost.insu.dialog.CustomDialog;
import com.epost.insu.fido.Fido2Callback;
import com.epost.insu.fido.Fido2Constant;
import com.epost.insu.fido.Fido2RegistableAuthTech;
import com.epost.insu.module.PSMobileModule;

import kr.or.kftc.fido.api.KFTCBioFidoManager;
import kr.or.kftc.fido.api.OnCompleteListener;

/**
 * 커스텀 Application
 * @since     :
 * @version   : 1.3
 * @author    : LSH
 * <pre>
 *      Time over 로그아웃 기능
 *      App가 백그라운드로 진입 하거나 스크린 OFF 상태에서 20분 경과 시 App 로그아웃
 *      로그아웃 시에 현재 위치가 메인화면(IUCOA0M00)이 아닌 경우에는 App 재시작
 *      ==> 현재 위치가 Web요청 Activity인 경우 로그아웃 처리 하지 않음.
 * ======================================================================
 * 0.0.0    LSM_20171101    최초 등록
 * 0.0.0    LSM_20171205    타임오버 로그아웃 정책 변경
 * 1.4.5    NJM_20201229    FIDO2.0 초기화 호출 추가
 * 1.5.2    NJM_20210318    [액티비티 재실행시 사진첨부오류(~준비되지 않은) 수정] psMobileModule 호출 추가
 * 1.5.2    NJM_20210322    [FIDO인증 로직 변경] Fido2RegistableAuthTech() 메인엑티비티에서 호출
 * 1.5.3    NJM_20210405    [FIDO호출 로직 변경] 에러발생으로인한 변경
 * 1.6.2    NJM_20210802    [2021년 대우 취약점] 2차본 : 부적절한 예외 처리
 * 1.6.3    NJM_20211006    [2021년 대우 취약점] 3차본 : 부적절한 예외 처리
 * =======================================================================
 * copyright : (주)프로니스정보기술 | http://www.phronis.co.kr
 * </pre>
 */
@SuppressWarnings("FieldCanBeLocal")
public class CustomApplication extends Application implements Application.ActivityLifecycleCallbacks, OnCompleteListener {
    // FIDO
    private KFTCBioFidoManager kftcBioFidoManager;
    private static Bundle bundleBioInfo;

    /**
     * App kill 모드 enum<br/>
     * Activity onStart 시에 해당 값 확인 후 처리된다.
     */
    public enum KillAppMode{
        noKill,     // not kill app
        allKill,    // 모든 Activity finish
        goMain      // 메인 Activity(IUCOA0M00)을 제외한 모든 Activity finish
    }

    private static KillAppMode killAppMode;                   // App Kill 모드

    @Override
    public void onCreate() {
        super.onCreate();
        LogPrinter.CF_debug("!-----------------------------------------------------------");
        LogPrinter.CF_debug("!-- CustomApplication.onCreate()");
        LogPrinter.CF_debug("!-----------------------------------------------------------");

        Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
            @Override
            public void uncaughtException(Thread paramThread, Throwable paramThrowable) {
              
            }
        });

        // -- Activity Life cycle callback 세팅
        registerActivityLifecycleCallbacks(this);

        // -- Screen Off 리시버 세팅
        IntentFilter tmp_filter = new IntentFilter(Intent.ACTION_SCREEN_OFF);
        registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Long tmp_inBackTimeMilli = SharedPreferencesFunc.getAppInBackgroundTime(getApplicationContext());
                if(tmp_inBackTimeMilli == 0L){
                    saveAppInBackgroundTime(System.currentTimeMillis());
                }
            }
        }, tmp_filter);

        CF_setKillApp(KillAppMode.noKill);       // flagKillApp 초기값 : false

        // -- 최초 실행시 로그아웃 처리
        CF_logOut(this);

        // -----------------------------------------------------------------------------------------
        // -- FIDO
        // -----------------------------------------------------------------------------------------
        if(getKftcBioFidoManager() != null) {
            // -- FIDO 정보조회
            getBundleBioInfo();
        }
    }

    @Override
    public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
        LogPrinter.CF_debug("!-----------------------------------------------------------");
        LogPrinter.CF_debug("!-- CustomApplication.onActivityCreated()");
        LogPrinter.CF_debug("!-----------------------------------------------------------");
    }

    @Override
    public void onActivityStarted( final Activity p_activity) {
        LogPrinter.CF_debug("!-----------------------------------------------------------");
        LogPrinter.CF_debug("!-- CustomApplication.onActivityStarted()");
        LogPrinter.CF_debug("!-----------------------------------------------------------");

//        // --<> bundleBioInfo가 null 일 경우 activity 재시작해야 함
//        if(getBundleBioInfo() == null) {
//            CustomDialog tmp_dlg = new CustomDialog(p_activity);
//            tmp_dlg.show();
//            tmp_dlg.CF_setBackKeyMode(CustomDialog.BackMode.NOTUSE);
//            tmp_dlg.CF_setTextContent("초기화 오류로 인하여 앱을 종료 후 재시작해야합니다.");
//            tmp_dlg.CF_setSingleButtonText(getResources().getString(R.string.btn_ok));
//            tmp_dlg.setOnDismissListener(new DialogInterface.OnDismissListener() {
//                @Override
//                public void onDismiss(DialogInterface dialog) {
//                    CommonFunction.CF_restartApp(p_activity);
//                }
//            });
//        }

        //  ----------------------------------------------------------------------------------------
        //  onActivityStarted 에서 로그아웃 처리를 하는 이유
        //  : 메인화면(IUCOA0M00)에서 onResume 시 로그인 상태값에 따라 우측 상단 아이콘 변경함.
        //  : Acitivty의 onResume보다 로그아웃 처리 선행해야함.
        // -----------------------------------------------------------------------------------------
        if(SharedPreferencesFunc.getFlagLogin(getApplicationContext())){
            Long tmp_stopTime = SharedPreferencesFunc.getAppInBackgroundTime(getApplicationContext());

            // ---------------------------------------------------------------------------------
            //  로그인 유지 시간 초과
            //      if, 현재 IUCOA0M00 Activity 인 경우 로그아웃 처리
            //      else, 로그아웃 처리 및 다이얼로그 팝업
            // ---------------------------------------------------------------------------------
            if (tmp_stopTime > 0L && (System.currentTimeMillis() - tmp_stopTime) > EnvConfig.logoutTime) {
                // ---------------------------------------------------------------------------------
                //  현재 화면이 메인 화면인 경우 : 로그아웃 처리만 한다.
                // ---------------------------------------------------------------------------------
                if(p_activity.getClass().getName().equals(IUCOA0M00.class.getName())){
                    /*
                    CF_logOut(this);

                    if(!p_activity.isFinishing()) {
                        WebView webView = ((IUCOA0M00) p_activity).getMWebview();
                        if (webView != null) {
                            webView.loadUrl(EnvConfig.host_url + "/CO/misLogoutAction.do");
                        }
                    }*/

                    CF_logOut(this);

                    CustomDialog tmp_dlg = new CustomDialog(p_activity);
                    tmp_dlg.show();
                    tmp_dlg.CF_setBackKeyMode(CustomDialog.BackMode.NOTUSE);
                    tmp_dlg.CF_setTextContent(getResources().getString(R.string.dlg_time_out));
                    tmp_dlg.CF_setSingleButtonText(getResources().getString(R.string.btn_ok));
                    tmp_dlg.setOnDismissListener(new DialogInterface.OnDismissListener() {
                        @Override
                        public void onDismiss(DialogInterface dialog) {
                            CommonFunction.CF_restartApp(p_activity);
                        }
                    });

                }
                // ---------------------------------------------------------------------------------
                //  현재 화면이 메인 화면이 아닌 경우 : 로그아웃 처리 및 다이얼로그 알림
                //      => 웹 요청이 들어온 경우  Activity_WebStart에서 SharedPreferencesFunc.setAppInBackgroundTime(this, 0L); 선행처리됨.
                //         따라서 IUCOA0M00(메인 Activity)가 아니며 웹 요청 Activity가 아닌 Activity들에 한하여 아래 과정 진행됨.
                // ---------------------------------------------------------------------------------
                else{
                    CF_logOut(this);

                    CustomDialog tmp_dlg = new CustomDialog(p_activity);
                    tmp_dlg.show();
                    tmp_dlg.CF_setBackKeyMode(CustomDialog.BackMode.NOTUSE);
                    tmp_dlg.CF_setTextContent(getResources().getString(R.string.dlg_time_out));
                    tmp_dlg.CF_setSingleButtonText(getResources().getString(R.string.btn_ok));
                    tmp_dlg.setOnDismissListener(new DialogInterface.OnDismissListener() {
                        @Override
                        public void onDismiss(DialogInterface dialog) {
                            CommonFunction.CF_restartApp(p_activity);
                        }
                    });
                }
            }
            SharedPreferencesFunc.setAppInBackgroundTime(this, 0L);
        }
    }

    @Override
    public void onActivityResumed(Activity activity) {
        LogPrinter.CF_debug("!-----------------------------------------------------------");
        LogPrinter.CF_debug("!-- CustomApplication.onActivityResumed()");
        LogPrinter.CF_debug("!-----------------------------------------------------------");
    }

    @Override
    public void onActivityPaused(Activity activity) {
        LogPrinter.CF_debug("!-----------------------------------------------------------");
        LogPrinter.CF_debug("!-- CustomApplication.onActivityPaused()");
        LogPrinter.CF_debug("!-----------------------------------------------------------");
    }

    @Override
    public void onActivityStopped(Activity activity) {
        LogPrinter.CF_debug("!-----------------------------------------------------------");
        LogPrinter.CF_debug("!-- CustomApplication.onActivityStopped()");
        LogPrinter.CF_debug("!-----------------------------------------------------------");
    }

    @Override
    public void onActivitySaveInstanceState(Activity activity, Bundle outState) {
        LogPrinter.CF_debug("!-----------------------------------------------------------");
        LogPrinter.CF_debug("!-- CustomApplication.onActivitySaveInstanceState()");
        LogPrinter.CF_debug("!-----------------------------------------------------------");
    }

    @Override
    public void onActivityDestroyed(Activity activity) {
        LogPrinter.CF_debug("!-----------------------------------------------------------");
        LogPrinter.CF_debug("!-- CustomApplication.onActivityDestroyed()");
        LogPrinter.CF_debug("!-----------------------------------------------------------");
    }

    @Override
    public void onTrimMemory(int level) {
        super.onTrimMemory(level);
        LogPrinter.CF_debug("!-----------------------------------------------------------");
        LogPrinter.CF_debug("!-- CustomApplication.onTrimMemory()");
        LogPrinter.CF_debug("!-----------------------------------------------------------");

        // -----------------------------------------------------------------------------------------
        //  App Background 진입 체크
        // -----------------------------------------------------------------------------------------
        if(level == TRIM_MEMORY_UI_HIDDEN){
            saveAppInBackgroundTime(System.currentTimeMillis());
        }
    }

    /**
     * App 백그라운드 진입 시간 기록<br/>
     * 로그인 상태에서만 기록 한다.
     */
    private void saveAppInBackgroundTime(Long p_timeMilli){
        if(SharedPreferencesFunc.getFlagLogin(getApplicationContext())){
            SharedPreferencesFunc.setAppInBackgroundTime(getApplicationContext(), p_timeMilli);
        }
    }

    /**
     * App Kill 모드 값 세팅
     * @param p_mode    KillAppMode
     */
    public static void CF_setKillApp(KillAppMode p_mode){
        killAppMode = p_mode;
    }

    /**
     * App Kill 모드 값 반환
     * @return      KillAppMode
     */
    public static KillAppMode CF_getKillApp(){
        return killAppMode;
    }

    /**
     * 데이터 로그아웃 처리
     * @param p_context     Context
     */
    public static void CF_logOut(Context p_context){
        LogPrinter.CF_debug("!-----------------------------------------------------------");
        LogPrinter.CF_debug("!-- CustomApplication.CF_logOut()");
        LogPrinter.CF_debug("!-----------------------------------------------------------");

        SharedPreferencesFunc.setLoginInfo(p_context, false, EnvConfig.AuthDvsn.UNCERTI, "", ""); // 고객정보 초기화
        SharedPreferencesFunc.setAppInBackgroundTime(p_context, 0L);

        // -- 서류첨부 솔루션 헬퍼 : 임시파일(이미지) 삭제
        PSMobileModule psMobileModule = new PSMobileModule(p_context);
        psMobileModule.clearFiles();
    }

    @Override
    public void onComplete(String resCode, Bundle resData) {
        LogPrinter.CF_debug("!-----------------------------------------------------------");
        LogPrinter.CF_debug("!-- CustomApplication.onComplete()");
        LogPrinter.CF_debug("!-----------------------------------------------------------");

        // -- (API 실패)
        if(!Fido2Constant.FIDO_CODE_SUCCESS.equals(resCode)) {
            Toast.makeText(this, "간편인증 초기화 실패 [" + resCode + "]", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * FIDIO 매니저 반환
     * @return  KFTCBioFidoManager
     */
    public KFTCBioFidoManager getKftcBioFidoManager() {
        LogPrinter.CF_debug("!-----------------------------------------------------------");
        LogPrinter.CF_debug("!-- CustomApplication.getKftcBioFidoManager()");
        LogPrinter.CF_debug("!-----------------------------------------------------------");
        try {
            if(kftcBioFidoManager == null) {
                LogPrinter.CF_debug("!---- kftcBioFidoManager == null");
                kftcBioFidoManager = new KFTCBioFidoManager(this);
            }
        } catch (NullPointerException e) {
            LogPrinter.CF_debug("!---- kftcBioFidoManager 생성에러(NPE)");
            e.getMessage();
        } catch (Exception e) {
            LogPrinter.CF_debug("!---- kftcBioFidoManager 생성에러");
            e.getMessage();
        }
        return kftcBioFidoManager;
    }

    /**
     * Fido2 getPhoneInfo 반환
     * @return tmp_bundle
     */
    public Bundle getBundleBioInfo() {
        LogPrinter.CF_debug("!------------------------------------------------------------");
        LogPrinter.CF_debug("!-- CustomApplication.getBundleBioInfo()");
        LogPrinter.CF_debug("!------------------------------------------------------------");

        try {
            if(bundleBioInfo == null) {
                LogPrinter.CF_debug("!-- bundleBioInfo == null");
                if(getKftcBioFidoManager() != null) {
                    // -- FIDO 정보조회
                    new Fido2RegistableAuthTech(this, new Fido2Callback(){
                        @Override
                        public void onReceiveMessage(String code, Bundle bundle) {
                            LogPrinter.CF_debug("!----[getPhoneInfo][성공] (code) : " + code + " / (bundle) : " + bundle.toString());
                            setBundleBioInfo(bundle);
                        }

                        @Override
                        public void onFailure(String code, String msg) {
                            LogPrinter.CF_debug("!----[getPhoneInfo][에러] (code) : " + code + " / (bundle) : " + msg);
                        }
                    }).process();
                }
            }
        } catch (NullPointerException e) {
            LogPrinter.CF_debug("NullPointerException" + e.getMessage());
        } catch (Exception e) {
            LogPrinter.CF_debug("Exception" + e.getMessage());
        }
        return bundleBioInfo;
    }

    /**
     * Fido2 getPhoneInfo 저장
     */
    public void setBundleBioInfo(Bundle p_bundle) {
        LogPrinter.CF_debug("!------------------------------------------------------------");
        LogPrinter.CF_debug("!-- CustomApplication.setBundleBioInfo()");
        LogPrinter.CF_debug("!------------------------------------------------------------");
        bundleBioInfo = p_bundle;
    }
}
