package com.epost.insu.fido;

import android.app.Activity;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.os.Message;
import android.text.TextUtils;

import com.epost.insu.CustomApplication;
import com.epost.insu.EnvConfig;
import com.epost.insu.R;
import com.epost.insu.SharedPreferencesFunc;
import com.epost.insu.common.CustomSQLiteFunction;
import com.epost.insu.common.LogPrinter;
import com.epost.insu.network.HttpConnections;
import com.epost.insu.network.WeakReferenceHandler;

import org.json.JSONException;
import org.json.JSONObject;

import java.security.InvalidParameterException;

import kr.or.kftc.fido.api.KFTCBioFidoManager;
import kr.or.kftc.fido.api.OnCompleteListener;

/**
 * FIDO 해지 class
 * @since     : project 48:1.4.5
 * @version   : 1.1
 * @author    : NJM
 * <pre>
 * ======================================================================
 * 1.4.5    NJM_20201229    최초 등록
 * 1.5.2    NJM_20210322    [subUrl 공통파일로 변경]
 * 1.5.2    NJM_20210322    [FIDO인증 로직 변경] kftcBioFidoManager 직접 생성
 * 1.5.3    NJM_20210330    [FIDO호출 로직 변경] 에러발생으로인한 변경
 * 1.6.2    NJM_20210802    [2021년 대우 취약점] 2차본 : 부적절한 예외 처리
 * =======================================================================
 * copyright : (주)프로니스정보기술 | http://www.phronis.co.kr
 * </pre>
 */
public class Fido2Cancelation implements OnCompleteListener,  WeakReferenceHandler.ObjectHandlerMessage {
    private final int HANDLERJOB_CANCEL_1       = 0;        // FIDO 해지 선거래
    private final int HANDLERJOB_ERROR_CANCEL_1 = 1;
    private final int HANDLERJOB_CANCEL_2       = 2;        // FIDO 해지 본거래(결과통보)
    private final int HANDLERJOB_ERROR_CANCEL_2 = 3;

    protected WeakReferenceHandler handler;                 // 핸들러

    private final Context context;
    private final Fido2Callback callback;                   // 화면
    private final Bundle phoneInfoBundle;
    private final String authTechCode;
    private final Activity activity;

    public Fido2Cancelation(Context pContext, Activity pActivity, String pAuthTechCode, Fido2Callback callback) {
        this.phoneInfoBundle = ((CustomApplication)pContext).getBundleBioInfo();
        this.context         = pContext;
        this.callback        = callback;
        this.authTechCode    = pAuthTechCode;
        this.activity        = pActivity;
        this.handler         = new WeakReferenceHandler(this);
    }

    // -- 실행
    public void process() {
        httpReq_cancel1();
    }

    // -- FIDO 인앱 API 호출
    private void reqBioDeReg(Bundle pBundle) {
        LogPrinter.CF_debug("!----------------------------------------------------------");
        LogPrinter.CF_debug("!-- Fido2Cancelation.reqBioDeReg() --FIDO 인앱 API 호출(해지)");
        LogPrinter.CF_debug("!----------------------------------------------------------");
        LogPrinter.CF_debug("!---- pBundle : " + pBundle.toString() );

        try {
            KFTCBioFidoManager kftcBioFidoManager = new KFTCBioFidoManager(context);
            kftcBioFidoManager.reqBioDeReg(pBundle, this);
        } catch (InvalidParameterException e) {
            callback.onFailure(Fido2Constant.CALLBACK_FAIL, Fido2Constant.getErrMsgForCode("-5000"));
        }
    }

    @Override
    public void onComplete(String resCode, Bundle resData) {
        LogPrinter.CF_debug("!----------------------------------------------------------");
        LogPrinter.CF_debug("!-- Fido2Cancelation.onComplete()");
        LogPrinter.CF_debug("!----------------------------------------------------------");
        LogPrinter.CF_debug("!---- resCode : " + resCode + " / resData : " + resData.toString() );
        /*
             핀인증 해지 성공
             resCode : 0000 / resData : Bundle[{DATA_KEY_CODE=2122, DATA_KEY_TRID=16091630710079830794, DATA_KEY_AUTH_TECH_CODE=116, DATA_KEY_VERSION=0107}]
         */
        if(Fido2Constant.FIDO_CODE_SUCCESS.equals(resCode)) {
                LogPrinter.CF_debug("!----------------------------------------------------------");
                LogPrinter.CF_debug("!-- Fido2Cancelation.onReceiveMessage() --FIDO API 해지 요청 성공");
                LogPrinter.CF_debug("!----------------------------------------------------------");
                /*
                resCode : 0000
                resData : Bundle[{  DATA_KEY_PUBKEY=30820122300D06092A864886F70D01010105000382010F003082010A0282010100D86E56CF56D71C864E310C988D2046CAC99C3AB958A75F941C5C6CBE8695C184EEE62FB4822B8B1524DB18AB89611550EC68BF5A769C67DE254F9BF23E86CC7141FD6FA486D72244C2A7B6979EEB98FEB1F47C4B7ED8A7277C2841605C2A41105805A6D1A4601506585C55F861CF45721AD0B9CE26FF1B2C285C2B10C474226F70BCF400787010581E8744258442342EA21869E2D2E5EBC3326F128984616830D4514556BBF781E652413C3F0720AD539A59CD96EB2D126B14055AC1521256905A927783840266BCACB007622C22E576E1D1B8D8E4EDBEC0D18DF15871A83E0903C05CCBBF84725ACDCC3481AC1A7F78EF3F377C5FF79AB8069F0D57F38E079D0203010001,
                                    DATA_KEY_CODE=104,
                                    DATA_KEY_FIDO={"uafResponse":"[{\"assertions\":[{\"assertion\":\"AT7qAwM-yAALLgkAMDAxMiMwMDIwDi4HAAEAAQYAAQEKLiAALn9Z2PZFQRPN3vNpGBdzhxiOcMRJHSVjlxswmrgYk5oJLiAA4OsRxBvQTcK_y_7vb79DUuVUkhyqBKnPRSW_lQl1DYQNLggAAAAAAAEAAAAMLlgAMFYwEAYHKoZIzj0CAQYFK4EEAAoDQgAEvzqd62S3ZWRb3t3sWf2YDQkdC5RqnWeQmf-FPG6jzNCJTFPuyllVDjv24wtildXLgvXp9rz-1MXFmd8UeFru-gc-GgMGLkgAMEYCIQDzr3yb7x8sAFcFOPjT8StsJlCYvgmtMdFe_Bvg6rwvTgIhAKbWJU02Jt5Zy6RHLNLZsEvJ_-TfKLTTNRqsc8xzImMDBS7KAjCCAsYwggGuoAMCAQICCQDmIwl-MIFjTTANBgkqhkiG9w0BAQUFADBnMQswCQYDVQQGEwJLUjEdMBsGA1UEChMUUmFvblNlY3VyZSBDby4sIEx0ZC4xGjAYBgNVBAsTEVF1YWxpdHkgQXNzdXJhbmNlMR0wGwYDVQQDExRSYW9uU2VjdXJlIENvLiwgTHRkLjAeFw0xNjA1MDMwNzE5NTFaFw00NjA0MjYwNzE5NTFaMH8xCzAJBgNVBAYTAktSMQ4wDAYDVQQIEwVTZW91bDETMBEGA1UEChMKUmFvbnNlY3VyZTESMBAGA1UECxMJUm5kMyBUZWFtMRIwEAYDVQQDFAkwMDEyIzAwMDYxIzAhBgkqhkiG9w0BCQEWFHN3bGVlQHJhb25zZWN1cmUuY29tMFYwEAYHKoZIzj0CAQYFK4EEAAoDQgAEndbefuQTurDH5DmyAQ-MlXJ3IdFNA0l3TykpEAGvNPH4hVQMqNX6H-T2ZCXUMLinNZy0wJcmkIgBRtcAzC2S_aMrMCkwEgYDVR0TAQH_BAgwBgEB_wIBATATBgNVHSUEDDAKBggrBgEFBQcDAjANBgkqhkiG9w0BAQUFAAOCAQEAhVnYyMwFtOcp4Z507bvoRBAgX8ou6dlMB6OuB69v5d6aT8tHGqVs_bCOkmgnXkJw8Hen25OheVH0yWU_pD05WesWsNAqiQKErs46VGrx1uF-XwxkLBAC8Ma13J9sm98qONcSD-fUAGT93iRKgm6H-GQGWbozkVgbxz_7M4INsmx90J-PSedyQYRPH27vahSm96CfV-Esc5vaGHczNMo4nqzMa0VnJBN-yOlMy-d8ECM7FIBPTDL2kdBi_ehbBDcksbJW-agrVV8kj8Vv7qNITYl416BdDWcwIhSfMikl8XOTc1K9WNQ4XyvwyZlf3hd3wMQKa6wwpz5e4eOWY8KtoA\",\"assertionScheme\":\"UAFV1TLV\"}],\"fcParams\":\"eyJhcHBJRCI6ImFuZHJvaWQ6YXBrLWtleS1oYXNoOkNBMEV5eHZ5QzZUKzdGWll2VVUxamxCRmExVSIsImNoYWxsZW5nZSI6IlpYMHdKM0VZRFVWR3VMMEZ6OXdOYUJkb1FPN0JkUnRjV2dUWGxqaDVOZEkiLCJjaGFubmVsQmluZGluZyI6eyJzZXJ2ZXJFbmRQb2ludCI6IlgtenJadl9JYnpqWlVuaHNiV2xzZWNMYndqbmRUcEcwWnluWE9pZjdWLWsiLCJ0bHNTZXJ2ZXJDZXJ0aWZpY2F0ZSI6Ik1BIn0sImZhY2V0SUQiOiJhbmRyb2lkOmFway1rZXktaGFzaDpDQTBFeXh2eUM2VCs3RlpZdlVVMWpsQkZhMVUifQ\",\"header\":{\"appID\":\"android:apk-key-hash:CA0EyxvyC6T+7FZYvUU1jlBFa1U\",\"op\":\"Reg\",\"serverData\":\"oQamuH6Zghx4MJ4x5BmYSjJopqbseY1tyhIWcZ5H_h0\",\"upv\":{\"major\":1,\"minor\":0}}}]"},
                                    DATA_KEY_TLS_CERT=MI,
                                    DATA_KEY_AUTH_TECH_CODE=100
                                }]
                 */

                // --<> (지문) 최종완료
                if(authTechCode.equals(Fido2Constant.AUTH_TECH_FINGER)) {
                    callback.onReceiveMessage(resCode, resData);
                }
                // -- (핀/패턴) 해지 결과 HTTP 전송 (지문 FIDO 1.0만 제외)
                else {
                    httpReq_cancel2_complete();
                }
        }
        else {
            callback.onFailure(resCode, Fido2Constant.getErrMsgForCode(resCode));
        }
    }


    // #############################################################################################
    //  Http 관련
    // #############################################################################################
    @Override
    public void handleMessage(Message p_message) {
        if(!activity.isDestroyed()) {
            switch (p_message.what) {
                // ---------------------------------------------------------------------------------
                // -- FIDO 해지 선거래
                // ---------------------------------------------------------------------------------
                case HANDLERJOB_CANCEL_1 :
                    try {
                        httpRes_cancel1(new JSONObject((String)p_message.obj));
                    } catch (JSONException e) {
                        LogPrinter.CF_debug(context.getResources().getString(R.string.log_json_exception));
                        callback.onFailure(Fido2Constant.CALLBACK_FAIL, context.getResources().getString(R.string.dlg_error_server_5));
                    }
                    break;
                case HANDLERJOB_ERROR_CANCEL_1 :
                    callback.onFailure(Fido2Constant.CALLBACK_FAIL, (String) p_message.obj);
                    break;
                // ---------------------------------------------------------------------------------
                // -- FIDO 해지 본거래
                // ---------------------------------------------------------------------------------
                case HANDLERJOB_CANCEL_2 :
                    try {
                        httpRes_cancel2_complete(new JSONObject((String)p_message.obj));
                    } catch (JSONException e) {
                        LogPrinter.CF_debug(context.getResources().getString(R.string.log_json_exception));
                        callback.onFailure(Fido2Constant.CALLBACK_FAIL, context.getResources().getString(R.string.dlg_error_server_5));
                    }
                    break;
                case HANDLERJOB_ERROR_CANCEL_2 :
                    callback.onFailure(Fido2Constant.CALLBACK_FAIL, (String) p_message.obj);
                    break;
                default:
                    break;
            }
        }
    }

    /**
     * FIDO 해지 요청
     */
    private void httpReq_cancel1() {
        LogPrinter.CF_debug("!----------------------------------------------------------");
        LogPrinter.CF_debug("!-- Fido2Cancelation.httpReq_cancel1() --FIDO 서버연동 해지 선조회 HTTP 요청");
        LogPrinter.CF_debug("!----------------------------------------------------------");

        Uri.Builder tmp_builder = new Uri.Builder();
        tmp_builder.appendQueryParameter("csno"         , CustomSQLiteFunction.getLastLoginCsno(context));
        tmp_builder.appendQueryParameter("tempKey"      , SharedPreferencesFunc.getWebTempKey(context));
        tmp_builder.appendQueryParameter("device_id"    , phoneInfoBundle.getString(Fido2Constant.KEY_DEVICE_ID));
        tmp_builder.appendQueryParameter("serviceCode"  , Fido2Constant.SVC_CODE);                  // 신규
        tmp_builder.appendQueryParameter("auth_tech"    , authTechCode);                            // 100:지문, 116:핀, 121:패턴

        HttpConnections.sendPostData(
                EnvConfig.host_url + EnvConfig.URL_FIDO_CANCEL1,
                tmp_builder.build().getEncodedQuery(),
                handler,
                HANDLERJOB_CANCEL_1,
                HANDLERJOB_ERROR_CANCEL_1);
    }

    /**
     * 지문인증 해지요청 처리 결과
     * @param p_jsonObject  JSONObject
     */
    private void httpRes_cancel1(JSONObject p_jsonObject) throws JSONException {
        LogPrinter.CF_debug("!----------------------------------------------------------");
        LogPrinter.CF_debug("!-- Fido2Cancelation.httpRes_cancel1() --FIDO 서버연동 해지 선거래 HTTP 응답");
        LogPrinter.CF_debug("!----------------------------------------------------------");
        LogPrinter.CF_debug("!---- p_jsonObject : " + p_jsonObject.toString());

        final String jsonKey_errorCode       = "errCode";
        final String jsonKey_data            = "data";
        final String jsonKey_s_tmnt_rqut_msg = "s_tmnt_rqut_msg";
        final String jsonKey_debugMsg        = "debugMsg";

        // 핀/패턴만
        final String jsonKey_s_enc_dvsn     = "s_enc_dvsn";
        final String jsonKey_s_psnid_enc    = "s_psnid_enc";
        final String jsonKey_s_tlgr_chas_no = "s_tlgr_chas_no";
        final String jsonKey_s_fido_tlgr_no = "s_fido_tlgr_no";

        String tmp_errorCode = "";
        String tmp_errorMsg = "";

        // --<> (에러코드키 포함)
        if(p_jsonObject.has(jsonKey_errorCode)) {
            tmp_errorCode = p_jsonObject.getString(jsonKey_errorCode);

            // --<> (최종 에러)
            if(!"".equals(tmp_errorCode)) {
                tmp_errorMsg = p_jsonObject.getString(jsonKey_debugMsg);
            }
            // --<> (최종 성공)
            else {
                if(p_jsonObject.has(jsonKey_data)){
                    JSONObject tmp_jsonData = p_jsonObject.getJSONObject(jsonKey_data);
                    try {
                        // -- (핀/패턴)
                        if(!authTechCode.equals(Fido2Constant.AUTH_TECH_FINGER)) {
                            phoneInfoBundle.putString("enc_dvsn"    , tmp_jsonData.getString(jsonKey_s_enc_dvsn));
                            phoneInfoBundle.putString("psnid_enc"   , tmp_jsonData.getString(jsonKey_s_psnid_enc));
                            phoneInfoBundle.putString("tlgr_chas_no", tmp_jsonData.getString(jsonKey_s_tlgr_chas_no));
                        }

                        Bundle tmp_bundle = new Bundle();
                        tmp_bundle.putString(Fido2Constant.KEY_AUTH_TECH_CODE, authTechCode);
                        tmp_bundle.putInt(Fido2Constant.KEY_CODE             , Fido2Constant.FIDO_CODE_CANCEL_IN);

                        // 지문
                        if(authTechCode.equals(Fido2Constant.AUTH_TECH_FINGER)) {
                            tmp_bundle.putString(Fido2Constant.KEY_FIDO          , tmp_jsonData.getString(jsonKey_s_tmnt_rqut_msg));
                            tmp_bundle.putString(Fido2Constant.KEY_TLS_CERT      , Fido2Constant.TLS_CERTIFICATE);
                            tmp_bundle.putString(Fido2Constant.KEY_SITE_CODE     , Fido2Constant.SITE_CODE);
                            tmp_bundle.putString(Fido2Constant.KEY_SVC_CODE      , Fido2Constant.SVC_CODE);
                        }
                        // 핀/패턴
                        else {
                            tmp_bundle.putString(Fido2Constant.KEY_TRID          , tmp_jsonData.getString(jsonKey_s_fido_tlgr_no));   // FIDO 추적번호
                        }

                        // -- 해지 실행 메소드
                        reqBioDeReg(tmp_bundle);

                    } catch (NullPointerException e) {
                        LogPrinter.CF_debug(context.getResources().getString(R.string.log_fail_send_fido_message));
                        callback.onFailure(Fido2Constant.CALLBACK_FAIL, context.getResources().getString(R.string.dlg_error_server_5));
                    } catch (Exception e) {
                        LogPrinter.CF_debug(context.getResources().getString(R.string.log_fail_send_fido_message));
                        callback.onFailure(Fido2Constant.CALLBACK_FAIL, context.getResources().getString(R.string.dlg_error_server_5));
                    }
                }
                else{
                    tmp_errorMsg = context.getResources().getString(R.string.dlg_error_server_2);
                }
            }
        }
        // --<> (에러코드키 미포함)
        else{
            tmp_errorMsg = context.getResources().getString(R.string.dlg_error_server_1);
        }

        // -- 에러가 있는 경우 에러 메시지 팝업
        if(!TextUtils.isEmpty(tmp_errorMsg)) {
            callback.onFailure(Fido2Constant.CALLBACK_FAIL    , tmp_errorMsg);
        }
    }

    /**
     * 해지 완료 정보 전달 (얼굴,핀,패턴만 사용)
     */
    private void httpReq_cancel2_complete(){
        LogPrinter.CF_debug("!----------------------------------------------------------");
        LogPrinter.CF_debug("!-- Fido2Cancelation.httpReq_cancel2_complete() --FIDO 서버연동 해지 본거래 HTTP 요청(핀/패턴)");
        LogPrinter.CF_debug("!----------------------------------------------------------");

        Uri.Builder tmp_builder = new Uri.Builder();
        tmp_builder.appendQueryParameter("csno"         , CustomSQLiteFunction.getLastLoginCsno(context));
        tmp_builder.appendQueryParameter("tempKey"      , SharedPreferencesFunc.getWebTempKey(context));
        tmp_builder.appendQueryParameter("serviceCode"  , Fido2Constant.SVC_CODE);                  // 신규
        tmp_builder.appendQueryParameter("auth_tech"    , authTechCode);                            // 100:지문, 116:핀, 121:패턴
        tmp_builder.appendQueryParameter("device_id"    , phoneInfoBundle.getString(Fido2Constant.KEY_DEVICE_ID));

        tmp_builder.appendQueryParameter("enc_dvsn"     , phoneInfoBundle.getString("enc_dvsn"));
        tmp_builder.appendQueryParameter("psnid_enc"    , phoneInfoBundle.getString("psnid_enc"));
        tmp_builder.appendQueryParameter("tlgr_chas_no" , phoneInfoBundle.getString("tlgr_chas_no"));

        HttpConnections.sendPostData(
                EnvConfig.host_url + EnvConfig.URL_FIDO_CANCEL2,
                tmp_builder.build().getEncodedQuery(),
                handler,
                HANDLERJOB_CANCEL_2,
                HANDLERJOB_ERROR_CANCEL_2);
    }

    /**
     * 지문인증 해지요청 처리 결과
     * @param p_jsonObject  JSONObject
     */
    private void httpRes_cancel2_complete(JSONObject p_jsonObject) throws JSONException {
        LogPrinter.CF_debug("!----------------------------------------------------------");
        LogPrinter.CF_debug("!-- Fido2Cancelation.setResultOfCancel_2() --FIDO 서버연동 해지 본거래 HTTP 응답(핀/패턴)");
        LogPrinter.CF_debug("!----------------------------------------------------------");
        LogPrinter.CF_debug("!---- p_jsonObject : " + p_jsonObject.toString());

        final String jsonKey_errorCode       = "errCode";
        final String jsonKey_data            = "data";
        final String jsonKey_s_tmnt_rqut_msg = "s_tmnt_rqut_msg";
        final String jsonKey_debugMsg        = "debugMsg";

        String tmp_errorCode = "";
        String tmp_errorMsg = "";

        // --<> (에러코드키 포함)
        if(p_jsonObject.has(jsonKey_errorCode)) {
            tmp_errorCode = p_jsonObject.getString(jsonKey_errorCode);

            // -- (최종 에러)
            if(!"".equals(p_jsonObject.getString(jsonKey_errorCode))) {
                tmp_errorMsg = p_jsonObject.getString(jsonKey_debugMsg);
            }
            // -- (최종 성공)
            else {
                if(p_jsonObject.has(jsonKey_data)){
                    JSONObject tmp_jsonData = p_jsonObject.getJSONObject(jsonKey_data);
                    callback.onReceiveMessage(Fido2Constant.FIDO_CODE_SUCCESS, phoneInfoBundle);
                }
                else{
                    tmp_errorMsg = context.getResources().getString(R.string.dlg_error_server_2);
                }
            }
        }
        // --<> (에러코드키 미포함)
        else{
            tmp_errorMsg = context.getResources().getString(R.string.dlg_error_server_1);
        }

        // -- 에러가 있는 경우 에러 메시지 팝업
        if(!TextUtils.isEmpty(tmp_errorMsg)) {
            callback.onFailure(Fido2Constant.CALLBACK_FAIL, tmp_errorMsg);
        }
    }
}
