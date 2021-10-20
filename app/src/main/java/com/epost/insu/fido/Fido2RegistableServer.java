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
import com.epost.insu.common.LogPrinter;
import com.epost.insu.network.HttpConnections;
import com.epost.insu.network.WeakReferenceHandler;

import org.json.JSONException;
import org.json.JSONObject;
/**
 * 서버DB에서 FIDO등록정보 조회 class
 * @since     : project 48:1.4.5
 * @version   : 1.1
 * @author    : NJM
 * @deprecated  * 1.6.2    NJM_20210729    [간편인증 플래그 반영] 간편인증 로그인시 저장된 flag값으로
 * <pre>
 * ======================================================================
 * 1.4.5    NJM_20201229    최초 등록
 * 1.5.2    NJM_20210322    [FIDO인증 로직 변경]
 * 1.5.3    NJM_20210330    [FIDO호출 로직 변경] 에러발생으로인한 변경
 * =======================================================================
 * copyright : (주)프로니스정보기술 | http://www.phronis.co.kr
 * </pre>
 */
public class Fido2RegistableServer implements WeakReferenceHandler.ObjectHandlerMessage {
    private final String subUrl_getFidoRegInfo      = "/CO/IUCOD2M51.do";   // 공동 FIDO 등록정보 조회

    private final int HANDLERJOB_GET_INFO           = 0;    // FIDO 등록정보 조회
    private final int HANDLERJOB_ERROR_GET_INFO     = 1;

    private final Fido2Callback callback;                   // 화면
    private final Bundle phoneInfoBundle;
    private final Context context;
    private Activity activity;
    protected WeakReferenceHandler handler;                 // 핸들러

    public Fido2RegistableServer(Context context, Activity activity, Fido2Callback pCallback) {
        this.context  = context;
        this.activity = activity;
        this.callback = pCallback;

        this.phoneInfoBundle =  ((CustomApplication)context).getBundleBioInfo();;

        handler = new WeakReferenceHandler(this);
    }

    /**
     * 등록여부 조회
     */
    public void process() {
        LogPrinter.CF_debug("!----------------------------------------------------------");
        LogPrinter.CF_debug("!-- Fido2RegistableServer.getInfo() --DB FIDO등록정보 조회 시작");
        LogPrinter.CF_debug("!----------------------------------------------------------");

        // -- 선거래 요청
        httpReq_getFidoRegInfo();
    }

    // #############################################################################################
    //  Http 관련
    // #############################################################################################
    @Override
    public void handleMessage(Message p_message) {
        LogPrinter.CF_debug("!----------------------------------------------------------");
        LogPrinter.CF_debug("!-- Fido2RegistableServer.handleMessage() --HTTP 응답");
        LogPrinter.CF_debug("!----------------------------------------------------------");
        if(!activity.isDestroyed()) {
            switch (p_message.what) {
                //----------------------------------------------------------------------------------
                // -- FIDO 등록 정보 조회
                //----------------------------------------------------------------------------------
                case HANDLERJOB_GET_INFO:
                    try {
                        httpRes_getFidoRegInfo(new JSONObject((String) p_message.obj));
                    } catch (JSONException e) {
                        LogPrinter.CF_debug(p_message.obj.toString());
                        callback.onFailure(Fido2Constant.CALLBACK_FAIL, context.getResources().getString(R.string.dlg_error_server_5));
                    }
                    break;
                case HANDLERJOB_ERROR_GET_INFO:
                    callback.onFailure(Fido2Constant.CALLBACK_FAIL, (String) p_message.obj);
                    break;

                default:
                    break;
            }
        }
    }

    /**
     * FIDO등록정보 HTTP 요청
     */
    private void httpReq_getFidoRegInfo() {
        LogPrinter.CF_debug("!----------------------------------------------------------");
        LogPrinter.CF_debug("!-- Fido2RegistableServer.httpReq_getFidoRegInfo() --FIDO등록정보 HTTP 요청");
        LogPrinter.CF_debug("!----------------------------------------------------------");

        Uri.Builder tmp_builder = new Uri.Builder();
        tmp_builder.appendQueryParameter("serviceCode"  , Fido2Constant.SVC_CODE);
        tmp_builder.appendQueryParameter("device_id"    , phoneInfoBundle.getString(Fido2Constant.KEY_DEVICE_ID));
        tmp_builder.appendQueryParameter("osType"       , Fido2Constant.OS_TYPE);

        HttpConnections.sendPostData(
                EnvConfig.host_url+subUrl_getFidoRegInfo,
                tmp_builder.build().getEncodedQuery(),
                handler,
                HANDLERJOB_GET_INFO,
                HANDLERJOB_ERROR_GET_INFO);
    }

    /**
     * FIDO등록정보 HTTP 응답
     * @param p_jsonObject      JSONObject
     */
    private void httpRes_getFidoRegInfo(JSONObject p_jsonObject) throws JSONException {
        LogPrinter.CF_debug("!----------------------------------------------------------");
        LogPrinter.CF_debug("!-- Fido2RegistableServer.httpRes_getFidoRegInfo() --FIDO등록정보 HTTP 응답");
        LogPrinter.CF_debug("!----------------------------------------------------------");
        LogPrinter.CF_debug("!---- p_jsonObject : " + p_jsonObject.toString());

        final String jsonKey_errorCode        = "errCode";
        final String jsonKey_data             = "data";
        final String jsonKey_debugMsg         = "debugMsg";

        final String jsonKey_finger     = Fido2Constant.AUTH_TECH_FINGER;      // 100:지문
        final String jsonKey_fin        = Fido2Constant.AUTH_TECH_PIN;         // 116:핀
        final String jsonKey_pattern    = Fido2Constant.AUTH_TECH_PATTERN;     // 121:패턴

        String tmp_errorCode;
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

                    if(tmp_jsonData.has(jsonKey_fin)) {
                        //SharedPreferencesFunc.setFlag_RegFido(context      , true, Fido2Constant.AUTH_TECH_PIN);
                        SharedPreferencesFunc.setDttm_RgtnFido(context     , tmp_jsonData.getJSONObject(jsonKey_fin).getString("rgtnDttm"), Fido2Constant.AUTH_TECH_PIN);
                        SharedPreferencesFunc.setDttm_lastAuthFido(context , tmp_jsonData.getJSONObject(jsonKey_fin).getString("lastAutnDttm"), Fido2Constant.AUTH_TECH_PIN);
                    } else {
                        //SharedPreferencesFunc.setFlag_RegFido(context      , false, Fido2Constant.AUTH_TECH_PIN);
                    }
                    if(tmp_jsonData.has(jsonKey_finger)) {
                        //SharedPreferencesFunc.setFlag_RegFido(context      , true, Fido2Constant.AUTH_TECH_FINGER);
                        SharedPreferencesFunc.setDttm_RgtnFido(context     , tmp_jsonData.getJSONObject(jsonKey_finger).getString("rgtnDttm"), Fido2Constant.AUTH_TECH_FINGER);
                        SharedPreferencesFunc.setDttm_lastAuthFido(context , tmp_jsonData.getJSONObject(jsonKey_finger).getString("lastAutnDttm"), Fido2Constant.AUTH_TECH_FINGER);
                    } else {
                        // SharedPreferencesFunc.setFlag_RegFido(context      , false, Fido2Constant.AUTH_TECH_FINGER);
                    }
                    if(tmp_jsonData.has(jsonKey_pattern)) {
                        //SharedPreferencesFunc.setFlag_RegFido(context      , true, Fido2Constant.AUTH_TECH_PATTERN);
                        SharedPreferencesFunc.setDttm_RgtnFido(context     , tmp_jsonData.getJSONObject(jsonKey_pattern).getString("rgtnDttm"), Fido2Constant.AUTH_TECH_PATTERN);
                        SharedPreferencesFunc.setDttm_lastAuthFido(context , tmp_jsonData.getJSONObject(jsonKey_pattern).getString("lastAutnDttm"), Fido2Constant.AUTH_TECH_PATTERN);
                    } else {
                        //  SharedPreferencesFunc.setFlag_RegFido(context      , false, Fido2Constant.AUTH_TECH_PATTERN);
                    }

                    Bundle tmpBundle = new Bundle();
                    callback.onReceiveMessage(Fido2Constant.FIDO_CODE_SUCCESS, tmpBundle);
                }
                else{
                    tmp_errorMsg = context.getResources().getString(R.string.dlg_error_server_2);
                }
            }
        }
        else{
            tmp_errorMsg = context.getResources().getString(R.string.dlg_error_server_1);
        }

        // -- 에러가 있는 경우 에러 메시지 팝업
        if(!TextUtils.isEmpty(tmp_errorMsg)){
            callback.onFailure(Fido2Constant.CALLBACK_FAIL, tmp_errorMsg);
        }
    }
}
