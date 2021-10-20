package com.epost.insu.common;

import android.util.Log;

import com.epost.insu.EnvConfig;

/**
 * @copyright : (주)프로니스정보기술 | http://www.phronis.co.kr
 *
 * @project   : 모바일슈랑스 구축
 * @pakage   : com.epost.insu.common
 * @fileName  : LogPrinter.java
 *
 * @Title     : 로그 프린터
 * @author    : 이수행
 * @created   : 2017-10-26
 * @version   : 1.0
 *
 * @note      : <u>로그 프린터</u><br/>
 *               {@link #CF_info(String)} info 타입 로그<br/>
 *               {@link #CF_debug(String)} debug 타입 로그<br/>
 *               {@link #CF_error(String)} error 타입 로그<br/>
 *               {@link #CF_warnning(String)} warnning 타입 로그<br/>
 *               {@link #CF_line()} debug 라인 출력<br/>
 *               {@link #CF_setFlagShowLog(boolean)} true 세팅 시 로그 출력<br/>
 * ======================================================================
 * 수정 내역
 * NO      날짜          작업자       내용
 * 01      2017-10-26    이수행       최초 등록
 * =======================================================================
 */
public class LogPrinter {

    private static boolean mFlagShowLog = EnvConfig.mFlagShowLog;
    private static String tag = "com.epost.insu";

    public static void CF_info(String p_message){
        if(mFlagShowLog)
            Log.i(tag,p_message);
    }

    public static void CF_info(String p_tag, String p_message){
        if(mFlagShowLog)
            Log.i(p_tag,p_message);
    }

    public static void CF_warnning(String p_message){
        if(mFlagShowLog)
            Log.w(tag,p_message);
    }

    public static void CF_warnning(String p_tag, String p_message){
        if(mFlagShowLog)
            Log.w(p_tag,p_message);
    }

    public static void CF_debug(String p_message){
        if(mFlagShowLog)
            Log.d(tag,p_message);
    }

    public static void CF_debug(String p_tag, String p_message){
        if(mFlagShowLog)
            Log.d(p_tag,p_message);
    }

    public static void CF_error(String p_message){
        if(mFlagShowLog)
            Log.e(tag,p_message);
    }

    public static void CF_error(String p_tag, String p_message){
        if(mFlagShowLog)
            Log.e(p_tag,p_message);
    }

    public static void CF_line(){
        if(mFlagShowLog)
            Log.d(tag, "============================================================");
    }

    public static void CF_setFlagShowLog(boolean p_flagShowLog){
        mFlagShowLog = p_flagShowLog;
    }
}
