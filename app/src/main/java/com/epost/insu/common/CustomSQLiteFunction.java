package com.epost.insu.common;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.text.TextUtils;

import com.epost.insu.EnvConfig;

/**
 * SQLite Function 클래스
 * @since     :
 * @version   : 1.0
 * @author    : NJM
 * <pre>
 * ======================================================================
 * 1.0  NJM_20200121    최초 등록
 * =======================================================================
 * copyright : (주)프로니스정보기술 | http://www.phronis.co.kr
 * </pre>
 */
@SuppressWarnings("FieldCanBeLocal")
public class CustomSQLiteFunction{
    /**
     * 단말기 DB에 저장되어 있는 csno 정보를 가져와
     * 고객 식별번호(csno) 보유 여부 검사
     * @return      고객번호저장 유무
     */
    public static boolean hasUserCsno(Context context){
        LogPrinter.CF_debug("!----------------------------------------------------------");
        LogPrinter.CF_debug("!-- CustomSQLiteFunction.hasUserCsno()");
        LogPrinter.CF_debug("!----------------------------------------------------------");

        boolean tmp_flagHas = false;
        if(!TextUtils.isEmpty(getLastLoginCsno(context))){
            tmp_flagHas = true;
        }

        return tmp_flagHas;
    }

    /**
     * 최근(마지막) 로그인 정보 (이름, 고객번호, 로그인유형)
     * @param lastLoginName        String
     * @param lastLoginCsno        String
     * @param lastLoginAuthDvsn    String {@link com.epost.insu.EnvConfig.AuthDvsn} 로그인유형
     */
    public static void setLastLoginInfo(Context context, String lastLoginName, String lastLoginCsno, EnvConfig.AuthDvsn lastLoginAuthDvsn){
        LogPrinter.CF_debug("!----------------------------------------------------------");
        LogPrinter.CF_debug("!-- CustomSQLiteFunction.updateUserInfo()");
        LogPrinter.CF_debug("!----------------------------------------------------------");
        LogPrinter.CF_debug("!---- lastLoginName     : " + lastLoginName);
        LogPrinter.CF_debug("!---- lastLoginCsno     : " + lastLoginCsno);
        LogPrinter.CF_debug("!---- lastLoginAuthDvsn : " + lastLoginAuthDvsn);

        CustomSQLiteHelper tmp_helper = new CustomSQLiteHelper(context);
        SQLiteDatabase tmp_sqlite = tmp_helper.getWritableDatabase();
        tmp_helper.CF_UpdateUserInfo(tmp_sqlite, lastLoginName, lastLoginCsno, EnvConfig.AuthDvsn.getAuthDvsnNum(lastLoginAuthDvsn));
        tmp_sqlite.close();
        tmp_helper.close();
    }

    /**
     * 최근(마지막) 로그인 csno 반환 함수
     * @return  String      고객번호
     */
    public static String getLastLoginCsno(Context context){
        LogPrinter.CF_debug("!----------------------------------------------------------");
        LogPrinter.CF_debug("!-- CustomSQLiteFunction.getLastLoginCsno()");
        LogPrinter.CF_debug("!----------------------------------------------------------");

        CustomSQLiteHelper helper = new CustomSQLiteHelper(context);
        SQLiteDatabase database = helper.getReadableDatabase();
        String csno = helper.CF_Selectcsno(database);

        database.close();
        helper.close();

        return csno;
    }

    /**
     * 최근(마지막) 로그인 이름 반환
     * @return      고객명
     */
    public static String getLastLoginName(Context context){
        LogPrinter.CF_debug("!----------------------------------------------------------");
        LogPrinter.CF_debug("!-- CustomSQLiteFunction.getLastLoginName()");
        LogPrinter.CF_debug("!----------------------------------------------------------");

        CustomSQLiteHelper helper = new CustomSQLiteHelper(context);
        SQLiteDatabase database   = helper.getReadableDatabase();
        String name = helper.CF_SelectUserName(database);

        database.close();
        helper.close();

        return name;
    }

    /**
     * 최근(마지막) 로그인 타입 반환
     * @return      String
     */
    public static String getLastLoginAuthDvsnNum(Context context){
        LogPrinter.CF_debug("!----------------------------------------------------------");
        LogPrinter.CF_debug("!-- CustomSQLiteFunction.getLastLoginAuthDvsnNum()");
        LogPrinter.CF_debug("!----------------------------------------------------------");

        CustomSQLiteHelper helper = new CustomSQLiteHelper(context);
        SQLiteDatabase database   = helper.getReadableDatabase();
        String tmp_loginType = helper.CF_SelectLoginType(database);

        helper.close();
        database.close();

        return tmp_loginType;
    }
}
