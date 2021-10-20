package com.epost.insu.common;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.view.View;

/**
 * @copyright : (주)프로니스정보기술 | http://www.phronis.co.kr
 *
 * @project   : 모바일슈랑스 구축
 * @pakage   : com.epost.insu.common
 * @fileName  : DeprecatedFunc.java
 *
 * @Title     : Deprecated 선언된 함수 세팅 모음
 * @author    : 이수행
 * @created   : 2017-10-26
 * @version   : 1.0
 *
 * @note      : <u>Deprecated 선언된 함수 세팅 모음</u><br/>
 *               이전 sdk 버전과 변경 이후 sdk 버전 모두 처리할 수 있도록 안드로이드 버전 체크 후 해당하는 함수를 적용 시킨다.<br/>
 *               deprecate 선언이 되었다해서 해당 함수를 사용할 수 없는 것은 아니나 안전성 보장이 되지 않기에 되도록 변경된 함수를 사용한다.<br/>
 * ======================================================================
 * 수정 내역
 * NO      날짜          작업자       내용
 * 01      2017-10-26    이수행       최초 등록
 * =======================================================================
 */
@SuppressWarnings("deprecation")
@SuppressLint("NewApi")
public class DeprecatedFunc {

    /**
     * View.setBackgroundDrawable
     * @param p_view
     * @param p_drawable
     */
    public static void CF_setBackgroundDrawable(View p_view, Drawable p_drawable){
        int sdk = android.os.Build.VERSION.SDK_INT;
        if(sdk >= android.os.Build.VERSION_CODES.JELLY_BEAN){
            p_view.setBackground(p_drawable);
        }else{
            p_view.setBackgroundDrawable(p_drawable);
        }
    }

    /**
     * 리소스 Drawable 반환
     * @param p_context
     * @param p_id
     * @return
     */
    public static Drawable CF_getDrawable(Context p_context, int p_id){
        int sdk = android.os.Build.VERSION.SDK_INT;
        if(sdk >= Build.VERSION_CODES.LOLLIPOP){
            return p_context.getResources().getDrawable(p_id, p_context.getTheme());
        }else{
            return p_context.getResources().getDrawable(p_id);
        }
    }

    /**
     * get resource colorStateList
     * @param p_context
     * @param p_id
     * @param p_theme
     * @return
     */
    public static ColorStateList CF_getColorStateList(Context p_context, int p_id, Resources.Theme p_theme){

        int sdk = Build.VERSION.SDK_INT;

        if(sdk >= Build.VERSION_CODES.M){
            return p_context.getResources().getColorStateList(p_id, p_theme);
        }
        return p_context.getResources().getColorStateList(p_id);
    }

    /**
     * get resource color
     * @param p_context
     * @param p_id
     * @param p_theme
     * @return
     */
    public static int CF_getColor(Context p_context, int p_id, Resources.Theme p_theme){

        int sdk = Build.VERSION.SDK_INT;

        if(sdk >= Build.VERSION_CODES.M){
            return p_context.getResources().getColor(p_id, p_theme);
        }
        return p_context.getResources().getColor(p_id);
    }
}
