package com.epost.insu;

import android.content.Context;
import android.content.SharedPreferences;

import com.epost.insu.common.LogPrinter;

/**
 * SharedPreferences 처리 함수 모음
 * @since     :
 * @version   : 1.2
 * @author    : NJM
 * <pre>
 * ======================================================================
 *          LSH_20170816    최초 등록
 *          LSH_20171124    commit() -> apply()
 *          LKM_20180814    스마트보험금청구 구현 추가
 * 1.5.4    NJM_20210429    [카카오페이앱 인증 S320] 앱to앱 인증 추가
 * 1.5.8    NJM_20210630    [금융인증서 도입] flagReg_ 인증방법 flag 저장
 * 1.5.9    NJM_20210702    [금융인증서 uuid 변경] UUID 생성 로직 변경
 * 1.6.2    NJM_20210729    [간편인증 플래그 반영] 간편인증 로그인시 저장된 flag값으로
 * 1.6.2    NJM_20210729    [자동로그인 신규] 1차본 (기능 반영만)
 * 1.6.3    NJM_20211006    [2021년 대우 취약점] 3차본 : 부적절한 예외 처리
 * =======================================================================
 * copyright : (주)프로니스정보기술 | http://www.phronis.co.kr
 * </pre>
 */
public class SharedPreferencesFunc {
    // -- 공통
    private final static String dt_notiOneDayClose = "dt_notiOneDayClose";
    //private final static String webLaunchJsonText  = "webLaunchJsonText"; // Web to App 실행정보 JSON 문자열

    // -- 로그인

    private final static String lastLoginMethod      = "lastLoginMethod";        // 마지막 로그인 방법 android id

    private final static String flagLogin      = "flagLogin";       // 로그인 유무 (true: 로그인, false: 로그아웃)
    private final static String flagAutoLogin  = "flagAutoLogin";   // 자동 로그인 유무 (true: 자동로그인)
    private final static String loginCsno      = "loinCsno";        // 로그인시 임시 고객 csno
    private final static String loginName      = "loinName";        // 로그인시 임시 고객명
    private final static String loginAuthDvsn  = "loinAuthDvsn";    // 로그인 인증유형(공동인증서, 카카오, 핀, 등) Envconfig.AuthDvsn
//    private final static String loginSex     = "loginSex";        // 로그인시 성별
//    private final static String loginBirthday= "loginBirthday";   // 로그인시 생년월일
//    private final static String loginMobile  = "loginMobile";     // 로그인시 휴대폰번호(암호화) : 카카오페이인증

    private final static String webTempKey     = "tempKey";         // 로그인세션용 임시키
    private final static String reqLoginTime   = "reqLoginTime";    // Web to App 인증 요청 시간

    private final static String flagReg_       = "flagReg_";        // 인증방법 등록 여부 ex) flagReg_KAKAO

    private final static String UUID           = "UUID";            // UUID

    // -- 로그인-FIDO
    private final static String flag_RegPin          = "flag_RegPin";           // FIDO 등록 여부(핀)
    private final static String flag_RegFinger       = "flag_RegFinger";
    private final static String flag_RegPattern      = "flag_RegPattern";

    private final static String dttm_RgtnPin         = "dttm_RgtnPin";          // FIDO 최근수정일(없을경우 최근등록일)
    private final static String dttm_RgtnFinger      = "dttm_RgtnFinger";
    private final static String dttm_RgtnPattern     = "dttm_Rgtnattern";

    private final static String dttm_lastAuthPin     = "dttm_lastAuthPin";      // FIDO 최근로그인일시
    private final static String dttm_lastAuthFinger  = "dttm_lastAuthFinger";
    private final static String dttm_lastAuthPattern = "dttm_lastAuthPattern";

    // -- 권한
    private final static String flagReqReadStorage          = "flagReqReadStorage";             // READ_EXTERNAL_STORAGE 퍼미션 요청 유무로 단 한번이라도 사용자 동의를 요구했다면 true
    private final static String flagReqDefaultPermissions   = "flagReqDefaultPermissions";      // 필수 퍼미션 요청 유무로 단 한번이라도 사용자 동의를 요구했다면 true
    private final static String flagReqCameraPermission     = "flagReqCameraPermission";        // 카메라 퍼미션 요청 유무로 단 한번이라도 사용자 동의를 요구했다면 true

    private final static String flagShowNotification        = "flagShowNotification";
    private final static String appInBackgroundTime         = "appInBackgroundTime";            // App Background 진입 시간
    private final static String smartReqAuthTime            = "smartReqAuthTime";               // 스마트보험금청구 로그인 시간
    private final static String smartReqHospitalCode        = "smartReqHospitalCode";           // 스마트보험금청구 병원정보
//    private final static String blockchainAuthKey           = "blockchainAuthKey";              // 블록체인 간편인증 키

//    /**
//     * Web to App실행 json 문자열 세팅
//     * @param p_context     Context
//     * @param p_jsonText    String
//     */
//    public static void setWebLaunchJsonText(Context p_context, String p_jsonText){
//        SharedPreferences tmp_pref = p_context.getSharedPreferences(EnvConfig.SHAREDPREFERENCES_NAME, Context.MODE_PRIVATE);
//        SharedPreferences.Editor tmp_editor = tmp_pref.edit();
//        tmp_editor.putString(webLaunchJsonText, p_jsonText);
//        tmp_editor.apply();
//    }
//    /**
//     * Web to App실행 json 문자열 반환
//     * @param p_context     Context
//     * @return              String
//     */
//    public static String getWebLaunchJsonText(Context p_context){
//        SharedPreferences tmp_pref = p_context.getSharedPreferences(EnvConfig.SHAREDPREFERENCES_NAME, Context.MODE_PRIVATE);
//        return tmp_pref.getString(webLaunchJsonText, "");
//    }

    /**
     * Web to App 인증 요청 시간 저장
     * @param p_context     Context
     * @param p_timeMilli   long
     */
    public static void setReqLoginTime(Context p_context, long p_timeMilli){
        SharedPreferences tmp_pref = p_context.getSharedPreferences(EnvConfig.SHAREDPREFERENCES_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor tmp_editor = tmp_pref.edit();
        tmp_editor.putLong(reqLoginTime, p_timeMilli);
        tmp_editor.apply();
    }
    /**
     * Web to App 인증 요청 시간 반환
     * @param p_context     Context
     * @return              Long
     */
    public static Long getReqLoginTime(Context p_context){
        SharedPreferences tmp_pref = p_context.getSharedPreferences(EnvConfig.SHAREDPREFERENCES_NAME, Context.MODE_PRIVATE);
        return tmp_pref.getLong(reqLoginTime, System.currentTimeMillis());
    }

    /**
     * App Background 진입 시간 저장
     * @param p_context     Context
     * @param p_timeMilli   Long
     */
    public static void setAppInBackgroundTime(Context p_context, long p_timeMilli){
        SharedPreferences tmp_pref = p_context.getSharedPreferences(EnvConfig.SHAREDPREFERENCES_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor tmp_editor = tmp_pref.edit();
        tmp_editor.putLong(appInBackgroundTime, p_timeMilli);
        tmp_editor.apply();
    }
    /**
     * App Background 진입 시간 반환
     * @param p_context     Context
     * @return              Long
     */
    public static Long getAppInBackgroundTime(Context p_context){
        SharedPreferences tmp_pref = p_context.getSharedPreferences(EnvConfig.SHAREDPREFERENCES_NAME, Context.MODE_PRIVATE);
        return tmp_pref.getLong(appInBackgroundTime, 0L);
    }

    /**
     * 푸시 알림 메시지 show 유무 값 세팅
     * @param p_context     Context
     * @param p_flagShow    boolean
     */
    public static void setFlagShowNotification(Context p_context, boolean p_flagShow){
        SharedPreferences tmp_pref = p_context.getSharedPreferences(EnvConfig.SHAREDPREFERENCES_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor tmp_editor = tmp_pref.edit();
        tmp_editor.putBoolean(flagShowNotification, p_flagShow);
        tmp_editor.apply();
    }
    /**
     * 푸시 알림 메시지 show 유무 값 반환
     * @param p_context     Context
     * @return              boolean
     */
    public static boolean getFlagShowNotification(Context p_context){
        SharedPreferences tmp_pref = p_context.getSharedPreferences(EnvConfig.SHAREDPREFERENCES_NAME, Context.MODE_PRIVATE);
        return tmp_pref.getBoolean(flagShowNotification, false);
    }

    /**
     * 하단팝업공지 1일간보지않기 버튼 클릭 날짜 세팅 함수
     * @param p_context     Context
     * @param p_dt          String  버튼클릭일
     */
    public static void setDt_notiOneDayClose(Context p_context, String p_dt){
        SharedPreferences tmp_pref = p_context.getSharedPreferences(EnvConfig.SHAREDPREFERENCES_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor tmp_editor = tmp_pref.edit();
        tmp_editor.putString(dt_notiOneDayClose, p_dt);
        tmp_editor.apply();
    }
    /**
     * 하단팝업공지 1일간보지않기 버튼 클릭 날짜 반환 함수
     * @param p_context     Context
     * @return              String  버튼클릭일
     */
    public static String getDt_notiOneDayClose(Context p_context) {
        SharedPreferences tmp_pref = p_context.getSharedPreferences(EnvConfig.SHAREDPREFERENCES_NAME, Context.MODE_PRIVATE);
        return tmp_pref.getString(dt_notiOneDayClose, "");
    }

    /**
     * Web에서 사용하는 tempKey 저장(로그인 시 저장됨)
     * @param p_context     Context
     * @param p_tempKey     String
     */
    public static void setWebTempKey(Context p_context, String p_tempKey){
        SharedPreferences tmp_pref = p_context.getSharedPreferences(EnvConfig.SHAREDPREFERENCES_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor tmp_editor = tmp_pref.edit();
        tmp_editor.putString(webTempKey, p_tempKey);
        tmp_editor.apply();
    }
    /**
     * Web에서 사용하는 tempKey 반환
     * @param p_context     Context
     * @return              String
     */
    public static String getWebTempKey(Context p_context){
        SharedPreferences tmp_pref = p_context.getSharedPreferences(EnvConfig.SHAREDPREFERENCES_NAME, Context.MODE_PRIVATE);
        return tmp_pref.getString(webTempKey, "");
    }

    /**
     * UUID 저장
     * @param p_context     Context
     * @param uuid          String
     */
    public static void setUuid(Context p_context, String uuid){
        SharedPreferences tmp_pref = p_context.getSharedPreferences(EnvConfig.SHAREDPREFERENCES_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor tmp_editor = tmp_pref.edit();
        tmp_editor.putString(UUID, uuid);
        tmp_editor.apply();
    }
    /**
     * 저장된 UUID 반환
     * @param p_context     Context
     * @return              String
     */
    public static String getUuid(Context p_context){
        SharedPreferences tmp_pref = p_context.getSharedPreferences(EnvConfig.SHAREDPREFERENCES_NAME, Context.MODE_PRIVATE);
        return tmp_pref.getString(UUID, "");
    }


//    /**
//     * logOut
//     * @param p_context     Context
//     * @param isLogin       boolean              로그인처리 유무
//     * @param p_authDvsn    EnvConfig.AuthDvsn  로그인 인증 유형
//     * @param p_loginCsno   String              고객번호
//     * @param p_loginName   String              고객명
//     */
//    public static void logOut(Context p_context) {
//        LogPrinter.CF_debug("!-----------------------------------------------------------");
//        LogPrinter.CF_debug("!-- SharedPreferencesFunc.setLoginInfo()");
//        LogPrinter.CF_debug("!-----------------------------------------------------------");
//
//        try {
//            SharedPreferences tmp_pref = p_context.getSharedPreferences(EnvConfig.SHAREDPREFERENCES_NAME, Context.MODE_PRIVATE);
//            SharedPreferences.Editor tmp_editor = tmp_pref.edit();
//            tmp_editor.putBoolean(flagLogin   , false); // 로그인 여부
//            tmp_editor.putString(loginAuthDvsn, EnvConfig.AuthDvsn.UNCERTI.toString()); // 로그인 인증 유형
//            tmp_editor.apply();
//        }
//        catch (Exception e) {
//            LogPrinter.CF_debug("!---- setLoginInfo() 에러:" +  e.getMessage());
//        }
//    }

    /**
     * Login 기본정보 저장
     * @param p_context     Context
     * @param isLogin       boolean              로그인처리 유무
     * @param p_authDvsn    EnvConfig.AuthDvsn  로그인 인증 유형
     * @param p_loginCsno   String              고객번호
     * @param p_loginName   String              고객명
     */
    public static void setLoginInfo(Context p_context, boolean isLogin, EnvConfig.AuthDvsn p_authDvsn, String p_loginCsno, String p_loginName) {
        LogPrinter.CF_debug("!-----------------------------------------------------------");
        LogPrinter.CF_debug("!-- SharedPreferencesFunc.setLoginInfo()");
        LogPrinter.CF_debug("!-----------------------------------------------------------");
        try {
            SharedPreferences tmp_pref = p_context.getSharedPreferences(EnvConfig.SHAREDPREFERENCES_NAME, Context.MODE_PRIVATE);
            SharedPreferences.Editor tmp_editor = tmp_pref.edit();
            tmp_editor.putBoolean(flagLogin, isLogin); // 로그인 여부
            tmp_editor.putString(loginAuthDvsn, p_authDvsn.toString()); // 로그인 인증 유형
            tmp_editor.putString(loginCsno, p_loginCsno); // 로그인 고객번호
            tmp_editor.putString(loginName, p_loginName); // 로그인 고객이름
            tmp_editor.apply();

            LogPrinter.CF_debug("!---- 저장된 로그인 정보 : " + p_authDvsn.toString() + " / " + p_loginCsno + " / " + p_loginName);
        } catch (NullPointerException e) {
            LogPrinter.CF_debug("!---- NullPointerException :" + e.getMessage());
        } catch (Exception e) {
            LogPrinter.CF_debug("!---- Exception :" + e.getMessage());
        }
    }





    public static int  getLastLoginMethod(Context p_context)
    {

        LogPrinter.CF_debug("!-----------------------------------------------------------");
        LogPrinter.CF_debug("!-- SharedPreferencesFunc.getLastLoginMethod()");
        LogPrinter.CF_debug("!-----------------------------------------------------------");

        int result = -1;
        try {

            SharedPreferences tmp_pref = p_context.getSharedPreferences(EnvConfig.SHAREDPREFERENCES_NAME_SETTING, Context.MODE_PRIVATE);
            result=tmp_pref.getInt(lastLoginMethod,-1);
            //LogPrinter.CF_debug("!---- 저장된 로그인 정보 : " + p_authDvsn.toString() + " / " + p_loginCsno + " / "+ p_loginName);

        }
        catch (Exception e) {
            LogPrinter.CF_debug("!---- getLastLoginMethod() 에러:" +  e.getMessage());
        }

        return result;
    }

    public static void setLastLoginMethod(Context p_context,Integer loginMethod)
    {

        LogPrinter.CF_debug("!-----------------------------------------------------------");
        LogPrinter.CF_debug("!-- SharedPreferencesFunc.setLastLoginMethod()");
        LogPrinter.CF_debug("!-----------------------------------------------------------");

        try {

            SharedPreferences tmp_pref = p_context.getSharedPreferences(EnvConfig.SHAREDPREFERENCES_NAME_SETTING, Context.MODE_PRIVATE);
            SharedPreferences.Editor tmp_editor = tmp_pref.edit();
            tmp_editor.putInt(lastLoginMethod    , loginMethod); // 로그인 고객번호
            tmp_editor.apply();
            //LogPrinter.CF_debug("!---- 저장된 로그인 정보 : " + p_authDvsn.toString() + " / " + p_loginCsno + " / "+ p_loginName);

        }
        catch (Exception e) {
            LogPrinter.CF_debug("!---- setLastLoginMethod() 에러:" +  e.getMessage());
        }
    }
//    /**
//     * Login 유무 값 세팅
//     * @param p_context     p_context
//     * @param p_flagLogin   boolean
//     */
//    public static void setFlagLogin(Context p_context, boolean p_flagLogin){
//        LogPrinter.CF_debug("!-----------------------------------------------------------");
//        LogPrinter.CF_debug("!-- SharedPreferencesFunc.setFlagLogin()");
//        LogPrinter.CF_debug("!-----------------------------------------------------------");
//
//        SharedPreferences tmp_pref = p_context.getSharedPreferences(EnvConfig.SHAREDPREFERENCES_NAME, Context.MODE_PRIVATE);
//        SharedPreferences.Editor tmp_editor = tmp_pref.edit();
//        tmp_editor.putBoolean(flagLogin, p_flagLogin);
//        tmp_editor.apply();
//
//        LogPrinter.CF_debug("!---- 저장된 로그인유무(flagLogin) ::::" + getFlagLogin(p_context));
//    }

    /**
     * Login 유무 값 반환
     * @param p_context     Context
     * @return              boolean
     */
    public static boolean getFlagLogin(Context p_context){
        SharedPreferences tmp_pref = p_context.getSharedPreferences(EnvConfig.SHAREDPREFERENCES_NAME, Context.MODE_PRIVATE);
        return tmp_pref.getBoolean(flagLogin, false);
    }


    /**
     * 자동로그인 유무 저장
     * @param p_context     Context
     * @param isAutoLogin     boolean     자동로그인(true:자동로그인)
     */
    public static void setFlagAutoLogin(Context p_context, boolean isAutoLogin) {
        LogPrinter.CF_debug("!-----------------------------------------------------------");
        LogPrinter.CF_debug("!-- SharedPreferencesFunc.setFlagAutoLogin()");
        LogPrinter.CF_debug("!-----------------------------------------------------------");
        try {
            SharedPreferences tmp_pref = p_context.getSharedPreferences(EnvConfig.SHAREDPREFERENCES_NAME, Context.MODE_PRIVATE);
            SharedPreferences.Editor tmp_editor = tmp_pref.edit();
            tmp_editor.putBoolean(flagAutoLogin , isAutoLogin); // 자동 로그인 여부
            tmp_editor.apply();
            LogPrinter.CF_debug("!---- 자동 로그인 정보 : " + isAutoLogin);

        } catch (NullPointerException e) {
            LogPrinter.CF_debug("!---- NullPointerException : " +  e.getMessage());
        } catch (Exception e) {
            LogPrinter.CF_debug("!---- Exception :" +  e.getMessage());
        }
    }
    /**
     * 자동로그인 유무 반환
     * @param p_context     Context
     * @return              boolean
     */
    public static boolean getFlagAutoLogin(Context p_context){
        LogPrinter.CF_debug("!-----------------------------------------------------------");
        LogPrinter.CF_debug("!-- SharedPreferencesFunc.getFlagAutoLogin()");
        LogPrinter.CF_debug("!-----------------------------------------------------------");
        SharedPreferences tmp_pref = p_context.getSharedPreferences(EnvConfig.SHAREDPREFERENCES_NAME, Context.MODE_PRIVATE);
        return tmp_pref.getBoolean(flagAutoLogin, false);
    }


// 사용할때 OPEN
//    /**
//     * Login csno 값 세팅
//     * @param p_context     Context
//     * @param p_loginCsno   String
//     *
//     * @since 1.3.6
//     */
//    public static void setLoginCsno(Context p_context, String p_loginCsno){
//        LogPrinter.CF_debug("!----------------------------------------------------------");
//        LogPrinter.CF_debug("!-- SharedPreferencesFunc.setLoginCsno()");
//        LogPrinter.CF_debug("!----------------------------------------------------------");
//
//        SharedPreferences tmp_pref = p_context.getSharedPreferences(EnvConfig.SHAREDPREFERENCES_NAME, Context.MODE_PRIVATE);
//        SharedPreferences.Editor tmp_editor = tmp_pref.edit();
//        tmp_editor.putString(loginCsno, p_loginCsno);
//        tmp_editor.apply();
//
//        LogPrinter.CF_debug("!---- 저장된 고객번호(loginCsno) ::::" + getLoginCsno(p_context));
//    }
    /**
     * Login csno 값 반환
     * @param p_context Context
     * @return          String
     *
     * @since 1.3.6
     */
    public static String getLoginCsno(Context p_context){
        SharedPreferences tmp_pref = p_context.getSharedPreferences(EnvConfig.SHAREDPREFERENCES_NAME, Context.MODE_PRIVATE);
        return tmp_pref.getString(loginCsno, "");
    }

// 사용할때 OPEN
//    /**
//     * Login 사용자명 값 세팅
//     * @param p_context     Context
//     * @param p_loginName   String
//     *
//     * @since 1.3.6
//     */
//    public static void setLoginName(Context p_context, String p_loginName){
//        LogPrinter.CF_debug("!----------------------------------------------------------");
//        LogPrinter.CF_debug("!-- SharedPreferencesFunc.setLoginCsno()");
//        LogPrinter.CF_debug("!----------------------------------------------------------");
//
//        SharedPreferences tmp_pref = p_context.getSharedPreferences(EnvConfig.SHAREDPREFERENCES_NAME, Context.MODE_PRIVATE);
//        SharedPreferences.Editor tmp_editor = tmp_pref.edit();
//        tmp_editor.putString(loginName, p_loginName);
//        tmp_editor.apply();
//
//        LogPrinter.CF_debug("!---- 저장된 고객명(p_loginName) ::::" + getLoginName(p_context));
//    }
    /**
     * Login 사용자명 값 반환
     * @param p_context Context
     * @return          String
     *
     * @since 1.3.6
     */
    public static String getLoginName(Context p_context){
        SharedPreferences tmp_pref = p_context.getSharedPreferences(EnvConfig.SHAREDPREFERENCES_NAME, Context.MODE_PRIVATE);
        return tmp_pref.getString(loginName, "");
    }

// 사용할때 OPEN
//    /**
//     * 로그인 인증 유형 값 세팅
//     * @param p_context     Context
//     * @param p_authDvsn    {@link EnvConfig.AuthDvsn}
//     *
//     * @since 1.3.6
//     */
//    public static void setLoginAuthDvsn(Context p_context, EnvConfig.AuthDvsn p_authDvsn){
//        LogPrinter.CF_debug("!----------------------------------------------------------");
//        LogPrinter.CF_debug("!-- SharedPreferencesFunc.setLoginAuthDvsn()");
//        LogPrinter.CF_debug("!----------------------------------------------------------");
//
//        SharedPreferences tmp_pref = p_context.getSharedPreferences(EnvConfig.SHAREDPREFERENCES_NAME, Context.MODE_PRIVATE);
//        SharedPreferences.Editor tmp_editor = tmp_pref.edit();
//        tmp_editor.putString(loginAuthDvsn, p_authDvsn.toString());
//        tmp_editor.apply();
//
//        LogPrinter.CF_debug("!---- 저장된 로그인유형(loginAuthDvsn) :" + getLoginAuthDvsn(p_context));
//    }
    /**
     * 로그인 인증 유형 값 반환
     * @param p_context Context
     * @return          String
     * @since 1.3.6
     */
    public static EnvConfig.AuthDvsn getLoginAuthDvsn(Context p_context){
        SharedPreferences tmp_pref = p_context.getSharedPreferences(EnvConfig.SHAREDPREFERENCES_NAME, Context.MODE_PRIVATE);
        return EnvConfig.AuthDvsn.valueOf(tmp_pref.getString(loginAuthDvsn, EnvConfig.AuthDvsn.UNCERTI.toString())) ;
    }

// 사용할때 OPEN
//    /**
//     * Login시 성별 세팅
//     * @param p_context Context
//     * @param sloginSex String
//     */
//    public static void setLoginSex(Context p_context, String sloginSex){
//        LogPrinter.CF_debug("!----------------------------------------------------------");
//        LogPrinter.CF_debug("!-- SharedPreferencesFunc.setLoginSex()");
//        LogPrinter.CF_debug("!----------------------------------------------------------");
//
//        SharedPreferences tmp_pref = p_context.getSharedPreferences(EnvConfig.SHAREDPREFERENCES_NAME, Context.MODE_PRIVATE);
//        SharedPreferences.Editor tmp_editor = tmp_pref.edit();
//        tmp_editor.putString(loginSex, sloginSex);
//        tmp_editor.apply();
//    }
//    /**
//     * Login시 성별 반환 : 카카오페이인증
//     * @param p_context Context
//     * @return          String
//     */
//    public static String getLoginSex(Context p_context){
//        SharedPreferences tmp_pref = p_context.getSharedPreferences(EnvConfig.SHAREDPREFERENCES_NAME, Context.MODE_PRIVATE);
//        return tmp_pref.getString(loginSex, "");
//    }

// 사용할때 OPEN
//    /**
//     * Login시 생년월일 세팅 : 카카오페이인증
//     * @param p_context         Context
//     * @param sLoginBirthday    String
//     */
//    public static void setLoginBirthday(Context p_context, String sLoginBirthday){
//        LogPrinter.CF_debug("!----------------------------------------------------------");
//        LogPrinter.CF_debug("!-- SharedPreferencesFunc.setLoginSex()");
//        LogPrinter.CF_debug("!----------------------------------------------------------");
//
//        SharedPreferences tmp_pref = p_context.getSharedPreferences(EnvConfig.SHAREDPREFERENCES_NAME, Context.MODE_PRIVATE);
//        SharedPreferences.Editor tmp_editor = tmp_pref.edit();
//        tmp_editor.putString(loginBirthday, sLoginBirthday);
//        tmp_editor.apply();
//    }
//    /**
//     * Login시 생년월일 반환 : 카카오페이인증
//     * @param p_context Context
//     * @return          String
//     */
//    public static String getLoginBirthday(Context p_context){
//        SharedPreferences tmp_pref = p_context.getSharedPreferences(EnvConfig.SHAREDPREFERENCES_NAME, Context.MODE_PRIVATE);
//        return tmp_pref.getString(loginBirthday, "");
//    }

// 사용할때 OPEN
//    /**
//     * Login시 휴대폰번호(암호화) 세팅 : 카카오페이인증
//     * @param p_context     Context
//     * @param sLoginMobile  String
//     */
//    public static void setLoginMobile(Context p_context, String sLoginMobile) {
//        SharedPreferences tmp_pref = p_context.getSharedPreferences(EnvConfig.SHAREDPREFERENCES_NAME, Context.MODE_PRIVATE);
//        SharedPreferences.Editor tmp_editor = tmp_pref.edit();
//        tmp_editor.putString(loginMobile, sLoginMobile);
//        tmp_editor.apply();
//    }
//    /**
//     * Login시 휴대폰번호(암호화) 반환 : 카카오페이인증
//     * @param p_context Context
//     * @return          String
//     */
//    public static String getLoginMobile(Context p_context){
//        SharedPreferences tmp_pref = p_context.getSharedPreferences(EnvConfig.SHAREDPREFERENCES_NAME, Context.MODE_PRIVATE);
//        return tmp_pref.getString(loginMobile, "");
//    }

    /**
     * READ_EXTERNAL_STORAGE 퍼미션 요청유무 값 세팅
     * @param p_context     Context
     * @param p_flagReq     boolean
     */
    public static void setReqPermissionReadStorage(Context p_context, boolean p_flagReq){
        SharedPreferences tmp_pref = p_context.getSharedPreferences(EnvConfig.SHAREDPREFERENCES_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor tmp_editor = tmp_pref.edit();
        tmp_editor.putBoolean(flagReqReadStorage, p_flagReq);
        tmp_editor.apply();
    }
//    /**
//     * READ_EXTERNAL_STORAGE 퍼미션 요청유무 값 반환
//     * @param p_context     Context
//     * @return              boolean
//     */
//    public static boolean getReqPermissionReadStorage(Context p_context){
//        SharedPreferences tmp_pref = p_context.getSharedPreferences(EnvConfig.SHAREDPREFERENCES_NAME, Context.MODE_PRIVATE);
//        return tmp_pref.getBoolean(flagReqReadStorage, false);
//    }

    /**
     * 필수 퍼미션 요청유무 값 세팅
     * @param p_context     Context
     * @param p_flagReq     boolean
     */
    public static void setReqPermissionDefault(Context p_context, boolean p_flagReq){
        SharedPreferences tmp_pref = p_context.getSharedPreferences(EnvConfig.SHAREDPREFERENCES_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor tmp_editor = tmp_pref.edit();
        tmp_editor.putBoolean(flagReqDefaultPermissions, p_flagReq);
        tmp_editor.apply();
    }
    /**
     * 필수 퍼미션 요청유무 값 반환
     * @param p_context     Context
     * @return              boolean
     */
    public static boolean getReqPermissionDefault(Context p_context){
        SharedPreferences tmp_pref = p_context.getSharedPreferences(EnvConfig.SHAREDPREFERENCES_NAME, Context.MODE_PRIVATE);
        return tmp_pref.getBoolean(flagReqDefaultPermissions, false);
    }

    /**
     * 카메라 퍼미션 요청유무 값 세팅
     * @param p_context     Context
     * @param p_flagReq     boolean
     */
    public static void setReqPermissionCamera(Context p_context, boolean p_flagReq){
        SharedPreferences tmp_pref = p_context.getSharedPreferences(EnvConfig.SHAREDPREFERENCES_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor tmp_editor = tmp_pref.edit();
        tmp_editor.putBoolean(flagReqCameraPermission, p_flagReq);
        tmp_editor.apply();
    }
    /**
     * 카메라 퍼미션 요청유무 값 반환
     * @param p_context     Context
     * @return              boolean
     */
    public static boolean getReqPermissionCamera(Context p_context){
        SharedPreferences tmp_pref = p_context.getSharedPreferences(EnvConfig.SHAREDPREFERENCES_NAME, Context.MODE_PRIVATE);
        return tmp_pref.getBoolean(flagReqCameraPermission, false);
    }

    /**
     * 고객의 인증방법 등록 여부 세팅 함수
     *  - 로그인시 등록 처리
     * @param p_context     Context
     * @param p_flagReg     boolean             등록여부
     * @param authDvsn      EnvConfig.AuthDvsn  인증방법
     */
    public static void setFlagRegAuthDvsn(Context p_context, boolean p_flagReg, EnvConfig.AuthDvsn authDvsn){
        LogPrinter.CF_debug("!-----------------------------------------------------------");
        LogPrinter.CF_debug("!-- SharedPreferencesFunc.setFlagRegAuthDvsn()");
        LogPrinter.CF_debug("!-----------------------------------------------------------");
        LogPrinter.CF_debug("!---- p_flagReg : " + p_flagReg + " / authDvsn : " + authDvsn.toString());

        SharedPreferences preferences = p_context.getSharedPreferences(EnvConfig.SHAREDPREFERENCES_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean(flagReg_ + authDvsn.toString(), p_flagReg);
        editor.apply();
    }

    /**
     * 고객의 인증방법 등록 여부 반환 함수
     * - 로그인시 등록 처리
     * @param p_context     Context
     * @return              boolean
     */
    public static boolean getFlagRegAuthDvsn(Context p_context, EnvConfig.AuthDvsn authDvsn){
        LogPrinter.CF_debug("!-----------------------------------------------------------");
        LogPrinter.CF_debug("!-- SharedPreferencesFunc.getFlagRegAuthDvsn()");
        LogPrinter.CF_debug("!-----------------------------------------------------------");

        SharedPreferences preferences = p_context.getSharedPreferences(EnvConfig.SHAREDPREFERENCES_NAME, Context.MODE_PRIVATE);
        boolean rtnFlag = preferences.getBoolean(flagReg_ + authDvsn.toString(), false);
        LogPrinter.CF_debug("!---- " + authDvsn.toString() + " : " + rtnFlag);
        return rtnFlag;
    }

    /**
     * 고객의 인증방법 등록 정보 초기화(삭제) 함수
     * - 로그인시 등록 처리
     * @param p_context     Context
     */
    public static void initFlagRegAuthDvsn(Context p_context){
        LogPrinter.CF_debug("!-----------------------------------------------------------");
        LogPrinter.CF_debug("!-- SharedPreferencesFunc.initFlagRegAuth()");
        LogPrinter.CF_debug("!-----------------------------------------------------------");

        SharedPreferences preferences = p_context.getSharedPreferences(EnvConfig.SHAREDPREFERENCES_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        for(EnvConfig.AuthDvsn key : EnvConfig.AuthDvsn.values()) {
            LogPrinter.CF_debug("!---- authDvsn : " + key.toString());
            editor.remove(flagReg_ + key.toString());
        }
        editor.apply();
    }
    // ---------------------------------------------------------------------------------------------
    // FIDO
    // ---------------------------------------------------------------------------------------------
//    /**
//     * 고객의 FIDO 등록 여부 세팅 함수
//     * @param p_context     Context
//     * @param p_flagReg     boolean
//     */
//    public static void setFlag_RegFido(Context p_context, boolean p_flagReg, String flag){
//        SharedPreferences tmp_pref = p_context.getSharedPreferences(EnvConfig.SHAREDPREFERENCES_NAME, Context.MODE_PRIVATE);
//        SharedPreferences.Editor tmp_editor = tmp_pref.edit();
//
//        // 지문
//        if("100".equals(flag)) {
//            tmp_editor.putBoolean(flag_RegFinger, p_flagReg);
//        }
//        // 핀인증
//        else if("116".equals(flag)) {
//            tmp_editor.putBoolean(flag_RegPin, p_flagReg);
//        }
//        // 패턴인증
//        else if("121".equals(flag)) {
//            tmp_editor.putBoolean(flag_RegPattern, p_flagReg);
//        }
//        else {
//           return;
//        }
//        tmp_editor.apply();
//    }

    /**
     * 고객의 FIDO 최근수정일(없을경우 최근등록일) 세팅 함수
     * @param p_context Context
     * @param p_ddtm    String
     * @param flag      String
     */
    public static void setDttm_RgtnFido(Context p_context, String p_ddtm, String flag){
        SharedPreferences tmp_pref = p_context.getSharedPreferences(EnvConfig.SHAREDPREFERENCES_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor tmp_editor = tmp_pref.edit();

        // (FIDO 코드)
        if("100".equals(flag)) { // 지문
            tmp_editor.putString(dttm_RgtnFinger, p_ddtm);
        }
        else if("116".equals(flag)) { // 핀인증
            tmp_editor.putString(dttm_RgtnPin, p_ddtm);
        }
        else if("121".equals(flag)) { // 패턴인증
            tmp_editor.putString(dttm_RgtnPattern, p_ddtm);
        }
        else {
            return;
        }
        tmp_editor.apply();
    }
//    /**
//     * 고객의 FIDO 최근수정일(없을경우 최근등록일) 반환 함수
//     * @param p_context     Context
//     * @param flag          String
//     */
//    public static String getDttm_RgtnFido(Context p_context, String flag){
//        SharedPreferences tmp_pref = p_context.getSharedPreferences(EnvConfig.SHAREDPREFERENCES_NAME, Context.MODE_PRIVATE);
//        // 지문
//        if("100".equals(flag)) {
//            return tmp_pref.getString(dttm_RgtnFinger, "");
//        }
//        // 핀인증
//        else if("116".equals(flag)) {
//            return tmp_pref.getString(dttm_RgtnPin, "");
//        }
//        // 패턴인증
//        else if("121".equals(flag)) {
//            return tmp_pref.getString(dttm_RgtnPattern, "");
//        }
//        else {
//            return "";
//        }
//    }

    /**
     * 고객의 마지막 FIDO 로그인 일시 세팅 함수
     * @param p_context     Context
     * @param p_ddtm        String
     */
    public static void setDttm_lastAuthFido(Context p_context, String p_ddtm, String flag){
        SharedPreferences tmp_pref = p_context.getSharedPreferences(EnvConfig.SHAREDPREFERENCES_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor tmp_editor = tmp_pref.edit();

        // (FIDO코드)
        if("100".equals(flag)) { // 지문
            tmp_editor.putString(dttm_lastAuthFinger, p_ddtm);
        }
        else if("116".equals(flag)) { // 핀인증
            tmp_editor.putString(dttm_lastAuthPin, p_ddtm);
        }
        else if("121".equals(flag)) { // 패턴인증
            tmp_editor.putString(dttm_lastAuthPattern, p_ddtm);
        }
        else {
            return;
        }
        tmp_editor.apply();
    }
//    /**
//     * 고객의 마지막 FIDO 로그인 일시 반환 함수
//     * @param p_context     Context
//     * @param flag          String
//     */
//    public static String getDttm_lastAuthFido(Context p_context, String flag){
//        SharedPreferences tmp_pref = p_context.getSharedPreferences(EnvConfig.SHAREDPREFERENCES_NAME, Context.MODE_PRIVATE);
//        // 지문
//        if("100".equals(flag)) {
//            return tmp_pref.getString(dttm_lastAuthFinger, "");
//        }
//        // 핀인증
//        else if("116".equals(flag)) {
//            return tmp_pref.getString(dttm_lastAuthPin, "");
//        }
//        // 패턴인증
//        else if("121".equals(flag)) {
//            return tmp_pref.getString(dttm_lastAuthPattern, "");
//        }
//        else {
//            return "";
//        }
//    }


    // ---------------------------------------------------------------------------------------------
    // 스마트청구(블록체인)
    // ---------------------------------------------------------------------------------------------
    /**
     * Web to App 스마트청구 인증 요청 시간 저장
     * @param p_context     Context
     * @param p_authTime    String
     */
    public static void setSmartReqAuthTime(Context p_context, String p_authTime){
        SharedPreferences tmp_pref = p_context.getSharedPreferences(EnvConfig.SHAREDPREFERENCES_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor tmp_editor = tmp_pref.edit();
        tmp_editor.putString(smartReqAuthTime, p_authTime);
        tmp_editor.apply();
    }
    /**
     * Web to App 스마트청구 인증 요청 시간 반환
     * @param p_context     Context
     * @return              String
     */
    public static String getSmartReqAuthTime(Context p_context){
        SharedPreferences tmp_pref = p_context.getSharedPreferences(EnvConfig.SHAREDPREFERENCES_NAME, Context.MODE_PRIVATE);
        return tmp_pref.getString(smartReqAuthTime, "");
    }

    /**
     * Web to App 스마트청구 병원정보 저장
     * @param p_context         Context
     * @param p_hospitalCode    String
     */
    public static void setSmartReqHospitalCode(Context p_context, String p_hospitalCode){
        SharedPreferences tmp_pref = p_context.getSharedPreferences(EnvConfig.SHAREDPREFERENCES_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor tmp_editor = tmp_pref.edit();
        tmp_editor.putString(smartReqHospitalCode, p_hospitalCode);
        tmp_editor.apply();
    }

    /**
     * Web to App 스마트청구 병원정보 반환
     * @param p_context     Context
     * @return              String
     */
    public static String getSmartReqHospitalCode(Context p_context){
        SharedPreferences tmp_pref = p_context.getSharedPreferences(EnvConfig.SHAREDPREFERENCES_NAME, Context.MODE_PRIVATE);
        return tmp_pref.getString(smartReqHospitalCode, "");
    }

}
