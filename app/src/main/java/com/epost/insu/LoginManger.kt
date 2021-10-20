package com.epost.insu

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Handler
import android.widget.Toast
import com.epost.insu.activity.IUCOA0M00
import com.epost.insu.common.CustomSQLiteFunction
import com.epost.insu.common.LogPrinter
import com.epost.insu.module.PSMobileModule
import com.epost.insu.push.InterfaceCustom
import m.client.push.library.PushManager
import m.client.push.library.common.PushConstants
import org.json.JSONException
import org.json.JSONObject

object LoginManger {


    /**
     * 데이터 로그아웃 처리
     * @param p_context     Context
     */
    fun CF_logOut(p_context: Context?) {
        LogPrinter.CF_debug("!-----------------------------------------------------------")
        LogPrinter.CF_debug("!-- CustomApplication.CF_logOut()")
        LogPrinter.CF_debug("!-----------------------------------------------------------")
        SharedPreferencesFunc.setLoginInfo(
            p_context,
            false,
            EnvConfig.AuthDvsn.UNCERTI,
            "",
            ""
        ) // 고객정보 초기화
        SharedPreferencesFunc.setAppInBackgroundTime(p_context, 0L)

        // -- 서류첨부 솔루션 헬퍼 : 임시파일(이미지) 삭제
        val psMobileModule = PSMobileModule(p_context)
        psMobileModule.clearFiles()
    }

    /**
     * 로그인 처리
     */
    fun CF_setLogin(applicationContext: Context, isLogin: Boolean, authDvsn: EnvConfig.AuthDvsn, csno: String, name:String, tempKey:String?) {
        LogPrinter.CF_debug("!-----------------------------------------------------------")
        LogPrinter.CF_debug("!-- Activity_Auth.CF_setLogin() --로그인 처리")
        LogPrinter.CF_debug("!-----------------------------------------------------------")
        LogPrinter.CF_debug("!---- isLogin  : $isLogin")
        LogPrinter.CF_debug("!---- authDvsn : $authDvsn")
        LogPrinter.CF_debug("!---- csno     : $csno")
        LogPrinter.CF_debug("!---- name     : $name")
        LogPrinter.CF_debug("!---- tempKey  : $tempKey")

        // -- [shared] 로그인정보 저장
        SharedPreferencesFunc.setLoginInfo(applicationContext, isLogin, authDvsn, csno, name)
        if ("" != tempKey && tempKey != null) {
            SharedPreferencesFunc.setWebTempKey(applicationContext, tempKey) // Web tempKey 저장
        }


        // -------------------------------------------------------------------------------------
        // -- 기존 저장된 csno와 로그인된 csno가 다를경우 flag 초기화 및 인증여부 등록
        // -------------------------------------------------------------------------------------
        val savedCsno = CustomSQLiteFunction.getLastLoginCsno(applicationContext)
        if(savedCsno != null && "" != savedCsno) {
            LogPrinter.CF_debug("!---- savedCsno : $savedCsno")
            if(csno != savedCsno) SharedPreferencesFunc.initFlagRegAuthDvsn(applicationContext)
        }

        SharedPreferencesFunc.setFlagRegAuthDvsn(applicationContext, true, authDvsn) // -- 인증 등록 여부 (개선버전)
        // -- [단말DB] 최근 로그인 정보 업데이트
        CustomSQLiteFunction.setLastLoginInfo(applicationContext, name, csno, authDvsn)
        // -- [유라클] 푸쉬 서비스&사용자 등록
        CF_pushRegisterServiceAndUser(applicationContext,csno, name)

        // (로그인처리) 로그인일 경우만 업데이트(임시로그인 제외)
        if(isLogin) {
            Toast.makeText(applicationContext, applicationContext.resources.getString(R.string.dlg_login_success), Toast.LENGTH_LONG).show()
        }

        // TODO : 테스트 로그
        for(authDvsnLog in EnvConfig.AuthDvsn.values() ) {
            SharedPreferencesFunc.getFlagRegAuthDvsn(applicationContext, authDvsnLog)
        }
    }


    /**
     * <유라클> 인터페이스 생성
     * push 서비스 등록 및 user 등록 ,해제, 그룹등록 등등 푸시 등록 처리 상태를 전달 받기 위한 callback 인터페이스로
     * 상태에 따라 다음 동작을 처리하면 됨
     * 예제: 현재 예로 loginActivity 화면에서 로그인 버튼을 클릭 시 서비스 등록 및 user 등록을 진행하고 정상 등록이 완료되면
     * loginActivity 화면에서 로그인 id,pw 입력화면을 숨기고 psuh 메시지 보내기 버튼이 존재하는
     * 화면으로 display 하는 형태로 만든 예제임으로 해당 화면에서 콜백을 받고 다음 단계를 진행하게 된 것임
     */
    private fun setInterfaceInit() {
        LogPrinter.CF_debug("!----------------------------------------------------------")
        LogPrinter.CF_debug("!-- Activity_Auth.setInterfaceInit() --[유라클] 인터페이스 생성")
        LogPrinter.CF_debug("!----------------------------------------------------------")

        // 인터페이스 생성 및 푸시 콜백 리스터 등록
        val interfaceCustom: InterfaceCustom = InterfaceCustom.getInstance()
        interfaceCustom.setPushReceiverCBListner { resultCd, resultMsg ->
            LogPrinter.CF_debug("!----------------------------------------------------------")
            LogPrinter.CF_debug("!-- Activity_Auth.setInterfaceInit().onPushReceiverState()")
            LogPrinter.CF_debug("!----------------------------------------------------------")
            LogPrinter.CF_debug("!---- resultCd  : $resultCd")
            LogPrinter.CF_debug("!---- resultMsg : $resultMsg")
            when (resultCd) {
                InterfaceCustom.SUCCESS_RESULT_CODE -> {
                }
                else -> {
                }
            }
        }
    }

    // 자체 DB에서 업데이트 했으나, 유라클 PUSH 적용 후 사용안함
    //    /**
    //     * [모슈DB] 푸쉬아이디 업데이트 요청
    //     * @param p_csno        String
    //     */
    //    protected void CF_requestUpdatePushId(String p_csno){
    //        LogPrinter.CF_debug("!----------------------------------------------------------");
    //        LogPrinter.CF_debug("!-- Activity_Auth.requestUpdatePushId() --푸쉬아이디 업데이트 요청");
    //        LogPrinter.CF_debug("!----------------------------------------------------------");
    //
    //        String tmp_token = FirebaseInstanceId.getInstance().getToken();
    //        if(!TextUtils.isEmpty(tmp_token)) {
    //            Uri.Builder tmp_builder = new Uri.Builder();
    //            tmp_builder.appendQueryParameter("csno"         , p_csno);
    //            tmp_builder.appendQueryParameter("tempKey"      , SharedPreferencesFunc.getWebTempKey(getApplicationContext()));
    //            tmp_builder.appendQueryParameter("mobileType"   , "A");
    //            tmp_builder.appendQueryParameter("mobilePushId" , tmp_token);
    //            tmp_builder.appendQueryParameter("uuid"         , CommonFunction.CF_getUUID(getApplicationContext()));
    //
    //            // 로그
    //            LogPrinter.CF_debug("!---- csno         : " + p_csno);
    //            LogPrinter.CF_debug("!---- tempKey      : " + SharedPreferencesFunc.getWebTempKey(getApplicationContext()));
    //            LogPrinter.CF_debug("!---- mobileType   : " + "A");
    //            LogPrinter.CF_debug("!---- mobilePushId : " + tmp_token);
    //            LogPrinter.CF_debug("!---- uuid         : " + CommonFunction.CF_getUUID(getApplicationContext()));
    //
    //            HttpConnections.sendPostData(EnvConfig.host_url + EnvConfig.URL_PUSH_UPDATE,
    //                    tmp_builder.build().getEncodedQuery(),
    //                    handler,
    //                    HANDLERJOB_UPDATE_PUSH,
    //                    HANDLERJOB_ERROR_UPDATE_PUSH);
    //        }
    //    }
    //    /**
    //     * 푸시 정보 업데이트 요청 결과 처리 함수
    //     * @param p_jsonObject      JSONObject
    //     * @throws JSONException    JSONException
    //     */
    //    protected void CF_setResultOfUpdatePush(JSONObject p_jsonObject) throws JSONException {
    //        LogPrinter.CF_debug("!----------------------------------------------------------");
    //        LogPrinter.CF_debug("!-- Activity_Auth.CF_setResultOfUpdatePush() --푸시 정보 업데이트 요청 결과 처리");
    //        LogPrinter.CF_debug("!----------------------------------------------------------");
    //        /*
    //            예시(정상)  : {"errCode":"","errMsg":""}
    //         */
    //
    //        final String jsonKey_errorCode = "errCode";
    //        final String jsonKey_errorMsg  = "errMsg";
    //
    //        // --<1> (전문성공) : 에러코드 키가 있을때
    //        if(p_jsonObject.has(jsonKey_errorCode)){
    //            //String tmp_errCode = p_jsonObject.getString(jsonKey_errorCode);
    //
    //            // --<2> (최종성공)
    //            if(p_jsonObject.has(jsonKey_errorMsg)) {
    //                //String tmp_errorMsg = p_jsonObject.getString(jsonKey_errorMsg);
    //
    //                // --<3> 에러메시지가 공백이 아닐경우 메시지 추가
    //                if (!p_jsonObject.getString(jsonKey_errorMsg).equals("")) {
    //                    String tmp_dlgMessage = getResources().getString(R.string.dlg_login_cert) + "\n\n" + p_jsonObject.getString(jsonKey_errorMsg);
    //                    CustomDialog tmp_dlg = new CustomDialog(this);
    //                    tmp_dlg.show();
    //                    tmp_dlg.CF_setBackKeyMode(CustomDialog.BackMode.NOTUSE);
    //                    tmp_dlg.CF_setTextContent(tmp_dlgMessage);
    //                    tmp_dlg.CF_setSingleButtonText(getResources().getString(R.string.btn_ok));
    //                    tmp_dlg.setOnDismissListener(new DialogInterface.OnDismissListener() {
    //                        @Override
    //                        public void onDismiss(DialogInterface dialog) {
    //                            setResult(RESULT_OK);
    //                            finish();
    //                        }
    //                    });
    //                } else {
    //                    setResult(RESULT_OK);
    //                    finish();
    //                }
    //            }
    //        }
    //        // --<1> (전문에러) 에러코드 키가 없을때 : 인증 엑티비티 바로 종료
    //        else {
    //            setResult(RESULT_OK);
    //            finish();
    //        }
    //    }



    /**
     * <유라클> push 서비스&사용자 등록 처리
     * @param p_csno  고객번호
     * @param p_name  고객명
     */
    private fun CF_pushRegisterServiceAndUser(context: Context,p_csno: String?, p_name: String?) {
        LogPrinter.CF_debug("!----------------------------------------------------------")
        LogPrinter.CF_debug("!-- Activity_Auth.CF_pushRegisterServiceAndUser() --유라클 푸시 서비스&사용자 등록 처리")
        LogPrinter.CF_debug("!----------------------------------------------------------")

        // -- PUSH 인터페이스 생성
        setInterfaceInit()

        // 필수 데이터 구성 - Client ID 와 Client Name 으로 User 등록
        val params = JSONObject()
        try {
            params.put(PushConstants.KEY_CUID, p_csno)
            params.put(PushConstants.KEY_CNAME, p_name)
            params.put("CUSTOM_RECEIVER_SERVER_URL", EnvConfig.pushServerUrl)
            params.put("CUSTOM_UPNS_SERVER_URL"    , EnvConfig.pushServerUrl)
        } catch (e: JSONException) {
            e.message
        }

        // 서비스 등록 및 사용자 등록을 함께 진행할 시 이용하는 API
        // 별도의 서비스 등록이 필요 없고 사용자 등록 시에 해당 API를 통해 서비스 등록_사용자 등록을 진행
        // http://docs.morpheus.co.kr/client/push/gcm.html#service 참고.
        Handler().post { PushManager.getInstance().registerServiceAndUser(context, params) }
    }

    //    /**
    //     * [유라클] push 서비스&사용자 해지 처리
    //     * @param csno  고객번호
    //     * @param name  고객명
    //     */
    //    protected void CF_pushUnregisterPushService(String csno, String name) {
    //        if(PushUtils.checkNetwork(IUCOA0M00.this)){
    //            new Handler().post(new Runnable() {
    //
    //                @Override
    //                public void run() {
    //                    // TODO Auto-generated method stub
    //                    // 푸시 서비스 등록 해제 - 서비스 및 사용자가 해제
    //                    PushManager.getInstance().unregisterPushService(getApplicationContext());
    //                }
    //            });
    //        }
    //        else{
    //            Toast.makeText(IUCOA0M00.this, "[MainActivity] network is not connected.", Toast.LENGTH_SHORT).show();
    //        }
    //    }

}