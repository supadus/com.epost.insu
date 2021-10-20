package com.epost.insu.common;

import android.app.Activity;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;

import com.epost.insu.CustomApplication;
import com.epost.insu.R;
import com.epost.insu.activity.IUCOA0M00;

/**
 * @copyright : (주)프로니스정보기술 | http://www.phronis.co.kr
 *
 * @project   : 모바일슈랑스 구축
 * @pakage   : com.epost.insu.common
 * @fileName  : BackDoubleClickCloseHandler.java
 *
 * @Title     : 하드웨어 백키 입력 핸들러
 * @author    : 이수행
 * @created   : 2017-06-29
 * @version   : 1.0
 *
 * @note      : <u>하드웨어 백키 두번 입력으로 Activity 종료 시키기 위한 클래스</u><br/>
 * ======================================================================
 * 수정 내역
 * NO      날짜          작업자       내용
 * 01      2017-06-29    이수행       최초 등록
 * =======================================================================
 */
public class BackDoubleClickCloseHandler {

    private final int default_waitTime = 2000;

    /** 백 키 입력 시간(밀리초) */
    private long backKeypressedTime = 0;

    /** 백 키 한번 입력시 보이는 토스트 */
    private Toast toast;

    /** 메인 Activity */
    private IUCOA0M00 activity;

    private String msg = "";
    private long waitTime;

    /**
     * 생성자
     * @param context
     */
    public BackDoubleClickCloseHandler(IUCOA0M00 context){
        this.activity = context;
        this.msg = activity.getResources().getString(R.string.toast_finish);
        this.waitTime = default_waitTime;
    }
    public BackDoubleClickCloseHandler(IUCOA0M00 context, String p_msg){
        this.activity = context;
        this.msg = p_msg;
        this.waitTime = default_waitTime;
    }
    public BackDoubleClickCloseHandler(IUCOA0M00 context, String p_msg, long p_waitTime){
        this.activity = context;
        this.msg = p_msg;
        this.waitTime = p_waitTime;
    }

    @SuppressWarnings("unused")
    public void CF_setMessage(String p_msg){
        this.msg = p_msg;
    }

    @SuppressWarnings("unused")
    public void CF_setWaitTime(long p_waitTime){
        this.waitTime = p_waitTime;
    }
    /**
     * 백키 입력시 호출되는 함수<br/>
     * 한번 입력 시 토스트를 보이고 두번 입력 시 Activity를 종료한다.
     */
    public void CF_onBackPressed( Activity activity,boolean p_flagLogout){
        if(System.currentTimeMillis() > backKeypressedTime +waitTime){
            backKeypressedTime = System.currentTimeMillis();
            showGuide();
        }else if(System.currentTimeMillis() <= backKeypressedTime + waitTime){

            if(p_flagLogout){
                CustomApplication.CF_logOut(activity);
            }

            toast.cancel();
            ActivityCompat.finishAffinity(activity);
            System.exit(0);
        }
    }

    /**
     * 안내 메시지 호출 함수
     */
    private void showGuide(){
        toast = Toast.makeText(activity, this.msg, Toast.LENGTH_SHORT);
        toast.show();
    }
}
