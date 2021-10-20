package com.epost.insu.activity.auth

import android.os.Handler
import android.widget.Toast
import com.epost.insu.EnvConfig
import com.epost.insu.EnvConfig.AuthDvsn
import com.epost.insu.LoginManger
import com.epost.insu.R
import com.epost.insu.SharedPreferencesFunc
import com.epost.insu.activity.Activity_Default
import com.epost.insu.common.CustomSQLiteFunction
import com.epost.insu.common.LogPrinter
import com.epost.insu.push.InterfaceCustom
import m.client.push.library.PushManager
import m.client.push.library.common.PushConstants
import org.json.JSONException
import org.json.JSONObject

/**
 * 인증 Activity
 * @since     : project 40:1.3.7
 * @version   : 1.3
 * @author    : NJM
 * @see
 * <pre>
 *  인증관련 Activity는 [Activity_Auth]를 상속받는다.
 * ======================================================================
 * 1.3.7    NJM_20200311    최초 등록
 * 1.5.2    NJM_20210322    [subUrl 공통파일로 변경]
 * 1.5.4    NJM_20210504    [간편인증 전자서명 추가] 웹요청 간편인증 추가, 청구시 간편인증 전자서명 추가
 * 1.5.6    NJM_20210601    [push업데이트 삭제] 모슈DB push id 업데이트 호출 삭제(유라클 사용함)
 * 1.5.8    NJM_20210630    [로그인처리 공통화] CF_setLogin() 공통 로그인 처리 함수
 * 1.6.2    NJM_20210729    [간편인증 플래그 반영] 간편인증 로그인시 flag값 참조 변경 (서버저장 -> 단말저장)
 * =======================================================================
 * copyright : (주)프로니스정보기술 | http://www.phronis.co.kr
 * </pre>
 */
abstract class Activity_Auth : Activity_Default() {




    /**
     * 로그인 처리
     */
    fun CF_setLogin(isLogin: Boolean, authDvsn: AuthDvsn, csno: String, name:String, tempKey:String?) {
                LoginManger.CF_setLogin(this@Activity_Auth,isLogin,authDvsn,csno,name,tempKey)
    }
}