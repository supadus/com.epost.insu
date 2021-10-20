package com.epost.insu.activity.auth

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Message
import android.text.TextUtils
import com.epost.insu.CustomApplication
import com.epost.insu.EnvConfig
import com.epost.insu.EnvConfig.AuthMode
import com.epost.insu.EnvConstant
import com.epost.insu.R
import com.epost.insu.activity.Activity_Default
import com.epost.insu.common.LogPrinter
import com.epost.insu.fido.Fido2Constant
import com.epost.insu.module.FingerModule
import com.epost.insu.network.HttpConnections
import com.epost.insu.network.WeakReferenceHandler
import com.epost.insu.network.WeakReferenceHandler.ObjectHandlerMessage
import kr.or.kftc.fido.api.KFTCBioFidoManager
import kr.or.kftc.fido.api.OnCompleteListener
import org.json.JSONException
import org.json.JSONObject
import java.security.InvalidParameterException

/**
 * 인증 > 간편인증(핀/지문/패턴) > 등록 (화면없음)
 * @since     : project 48:1.4.5
 * @version   : 1.1
 * @author    : NJM
 * <pre>
 * ======================================================================
 * 1.4.5    NJM_20201229    최초 등록
 * 1.5.2    NJM_20210322    [subUrl 공통파일로 변경]
 * 1.5.2    NJM_20210322    [FIDO인증 로직 변경] kftcBioFidoManager 직접 생성
 * 1.5.3    NJM_20210330    [FIDO호출 로직 변경] 에러발생으로인한 변경
 * 1.5.9    NJM_20210701    [PIN등록 오류 수정] 최초설치시 PIN등록시 csno 누락 오류 수정
 * 1.6.2    NJM_20210802    [2021년 대우 취약점] 2차본 : 부적절한 예외 처리
 * =======================================================================
 * copyright : (주)프로니스정보기술 | http://www.phronis.co.kr
 * </pre>
 */
class IUFC10M00 : Activity_Default(), OnCompleteListener, ObjectHandlerMessage {
    private val HANDLERJOB_REG_FIDO_1: Int = 1 // FIDO 등록 선조회 성공
    private val HANDLERJOB_ERROR_REG_FIDO_1: Int = 2 // FIDO 등록 선조회 실패
    private val HANDLERJOB_REG_FIDO_2: Int = 3 // FIDO 등록 본거래 성공
    private val HANDLERJOB_ERROR_REG_FIDO_2: Int = 4 // FIDO 등록 본거래 실패

    private var authTechCode: String = ""

    private var phoneInfoBundle: Bundle? = null // 디바이스 정보
    private var tmp_tlgr_chas_no: String? = null // 전문추적번호 결과값
    private var tmp_responseMsg: String? = "" // FIDO 등록요청메시지
    private var tmp_publicKey: String? = "" // 일회용공개키
    private var mCsno: String = ""

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        LogPrinter.CF_debug("!----------------------------------------------------------")
        LogPrinter.CF_debug("!-- IUFC10M00.onActivityResult() --간편인증 등록처리(화면없음)")
        LogPrinter.CF_debug("!----------------------------------------------------------")

        // -- 간편인증 Activity 종료
        if (resultCode == RESULT_OK) {
            when (requestCode) {
                EnvConfig.REQUESTCODE_ACTIVITY_IUFC00M00 -> httpReq_RegFido1()
            }
        } else {
            setResult(RESULT_CANCELED)
            finish()
        }
    }

    override fun setInit() {
        LogPrinter.CF_debug("!-----------------------------------------------------------")
        LogPrinter.CF_debug("!-- IUFC10M00.setInit() --간편인증 등록처리(화면없음)")
        LogPrinter.CF_debug("!-----------------------------------------------------------")
        phoneInfoBundle = (applicationContext as CustomApplication).bundleBioInfo

        handler = WeakReferenceHandler(this)
        if (intent.hasExtra(EnvConstant.KEY_INTENT_AUTH_TECH_CODE)) {
            authTechCode = intent.extras!!.getString(EnvConstant.KEY_INTENT_AUTH_TECH_CODE)!!
        }
        if (intent.hasExtra(EnvConstant.KEY_INTENT_AUTH_CSNO)) {
            mCsno = intent.extras!!.getString(EnvConstant.KEY_INTENT_AUTH_CSNO)!!
        }
    }

    override fun setUIControl() {
        LogPrinter.CF_debug("!-----------------------------------------------------------")
        LogPrinter.CF_debug("!-- IUFC10M00.setUIControl() --간편인증 등록처리(화면없음)")
        LogPrinter.CF_debug("!-----------------------------------------------------------")
        CF_showProgressDialog()
    }

    override fun onCreate(p_bundle: Bundle?) {
        super.onCreate(p_bundle)
        LogPrinter.CF_debug("!-----------------------------------------------------------")
        LogPrinter.CF_debug("!-- IUFC10M00.onCreate() --간편인증 등록처리(화면없음)")
        LogPrinter.CF_debug("!-----------------------------------------------------------")

        // --<> (등록하는 인증이 PIN이 아니면 PIN인증)
        if (authTechCode != Fido2Constant.AUTH_TECH_PIN) {
            startIUFC00M00_Auth(Fido2Constant.AUTH_TECH_PIN)
        } else {
            // -- FIDO 선거래 요청
            httpReq_RegFido1()
        }
    }

    override fun onPause() {
        super.onPause()
        LogPrinter.CF_debug("!-----------------------------------------------------------")
        LogPrinter.CF_debug("!-- IUFC10M00.onPause() --간편인증 등록처리(화면없음)")
        LogPrinter.CF_debug("!-----------------------------------------------------------")

        overridePendingTransition(0, 0) // end anition 해지
    }

    override fun onComplete(resCode: String, resData: Bundle) {
        LogPrinter.CF_debug("!----------------------------------------------------------")
        LogPrinter.CF_debug("!-- IUFC10M00.onComplete() --FIDO API 콜백")
        LogPrinter.CF_debug("!----------------------------------------------------------")
        LogPrinter.CF_debug("!---- resCode : $resCode / resData : $resData")

        if ((Fido2Constant.FIDO_CODE_SUCCESS == resCode)) {
            // --<> 지문(1단계)
            if (resData.getString(Fido2Constant.KEY_FIDO) != null) {
                LogPrinter.CF_debug("!---- FIDO API 등록 요청 성공(reqBio) : (지문 1단계)")
                tmp_responseMsg = resData.getString(Fido2Constant.KEY_FIDO) // FIDO 등록요청메시지
                tmp_publicKey = resData.getString(Fido2Constant.KEY_PUB_KEY) // 일회용공개키
                LogPrinter.CF_debug("!----tmp_publicKey   : $tmp_publicKey")
                LogPrinter.CF_debug("!----tmp_responseMsg : $tmp_responseMsg")

                // -- 전자서명(인증) 및 본거래
                httpReq_RegFido2()
                //                startIUFC00M00_Auth(Fido2Constant.AUTH_TECH_PIN);
            } else {
                LogPrinter.CF_debug("!-- FIDO API 등록 요청 성공(지문(2단계), 핀, 패턴)")

                // -- (지문)
                if ((authTechCode == Fido2Constant.AUTH_TECH_FINGER)) {
                   regAuthFinish() // 최종완료
                }
                else {
                    httpReq_RegFido2()
                }
                //                // -- (핀) : 본거래 HTTP 요청
//                else if(authTechCode.equals(Fido2Constant.AUTH_TECH_PIN)) {
//                    httpReq_RegFido2();
//                }
//                // -- (패턴) : 전자서명(인증) 및 본거래
//                else {
//                    startIUFC00M00_Auth(Fido2Constant.AUTH_TECH_PIN);
//                }
            }
        } else {
            LogPrinter.CF_debug("!-- FIDO API 등록 요청 실패")
            CF_finishCancelMsg(Fido2Constant.getErrMsgForCode(resCode))
        }
    }

    // -- FIDO API 호출
    private fun reqBioReg(pBundle: Bundle) {
        LogPrinter.CF_debug("!----------------------------------------------------------")
        LogPrinter.CF_debug("!-- IUFC10M00.reqBioReg() --FIDO API 호출")
        LogPrinter.CF_debug("!----------------------------------------------------------")
        LogPrinter.CF_debug("!---- pBundle : $pBundle")
        try {
            val kftcBioFidoManager = KFTCBioFidoManager(applicationContext)
            kftcBioFidoManager.reqBioReg(pBundle, this)
        } catch (e: NullPointerException) {
            e.message
            CF_finishCancelMsg(resources.getString(R.string.dlg_error_server_5))
        } catch (e: InvalidParameterException) {
            e.message
            CF_finishCancelMsg(resources.getString(R.string.dlg_error_server_5))
        } catch (e: Exception) {
            e.message
            CF_finishCancelMsg(resources.getString(R.string.dlg_error_server_5))
        }
    }

    // -- 지문일 경우 추가 등록정보 저장
    private fun reqSaveRegInfo(pEncNidcn: String) {
        LogPrinter.CF_debug("!----------------------------------------------------------")
        LogPrinter.CF_debug("!-- IUFC10M00.reqSaveRegInfo() --FIDO API 호출")
        LogPrinter.CF_debug("!----------------------------------------------------------")

        val bundleIn = Bundle()
        bundleIn.putString(Fido2Constant.KEY_ENC_NIDCN, pEncNidcn)
        val kftcBioFidoManager = KFTCBioFidoManager(applicationContext)
        kftcBioFidoManager.reqSaveRegInfo(bundleIn, this)
    }

    // #############################################################################################
    //  Http 관련
    // #############################################################################################
    override fun handleMessage(p_message: Message) {
        LogPrinter.CF_debug("!----------------------------------------------------------")
        LogPrinter.CF_debug("!-- IUFC10M00.handleMessage()")
        LogPrinter.CF_debug("!----------------------------------------------------------")
        if (!isDestroyed) {
            when (p_message.what) {
                HANDLERJOB_REG_FIDO_1 -> try {
                    httpRes_RegFido1(JSONObject(p_message.obj as String?))
                } catch (e: JSONException) {
                    LogPrinter.CF_debug(getResources().getString(R.string.log_json_exception))
                    CF_finishCancelMsg(getResources().getString(R.string.dlg_error_server_5))
                }
                HANDLERJOB_REG_FIDO_2 -> try {
                    httpRes_RegFido2(JSONObject(p_message.obj as String?))
                } catch (e: JSONException) {
                    LogPrinter.CF_debug(getResources().getString(R.string.log_json_exception))
                    CF_finishCancelMsg(getResources().getString(R.string.dlg_error_server_5))
                }
                HANDLERJOB_ERROR_REG_FIDO_1, HANDLERJOB_ERROR_REG_FIDO_2 -> CF_finishCancelMsg(getResources().getString(R.string.dlg_error_server_5))
                else -> {
                }
            }
        }
    }

    /**
     * FIDO 서버연동 등록 선거래 요청
     */
    private fun httpReq_RegFido1() {
        LogPrinter.CF_debug("!----------------------------------------------------------")
        LogPrinter.CF_debug("!-- IUFC10M00.httpReq_RegFido1() --FIDO 서버연동 등록 선거래 HTTP 요청")
        LogPrinter.CF_debug("!----------------------------------------------------------")

        val builder: Uri.Builder = Uri.Builder()
        builder.appendQueryParameter("auth_tech"  , authTechCode) // 100:지문, 116:핀, 121:패턴
        builder.appendQueryParameter("device_id"  , phoneInfoBundle!!.getString(Fido2Constant.KEY_DEVICE_ID))
        builder.appendQueryParameter("appVersion" , phoneInfoBundle!!.getString(Fido2Constant.KEY_VERSION))
        builder.appendQueryParameter("osType"     , Fido2Constant.OS_TYPE) // (고정값) 안드로이드
        builder.appendQueryParameter("serviceCode", Fido2Constant.SVC_CODE) // (고정값) 서비스 코드
        builder.appendQueryParameter("csno"       , mCsno) // 고객번호

        LogPrinter.CF_debug("!---- builder : $builder")
        HttpConnections.sendPostData(
                EnvConfig.host_url + EnvConfig.URL_FIDO_REG1,
                builder.build().encodedQuery,
                handler,
                HANDLERJOB_REG_FIDO_1,
                HANDLERJOB_ERROR_REG_FIDO_1)
    }

    /**
     * FIDO 서버연동 등록 선거래 요청 응답 처리
     * @param p_jsonObject  JSONObject
     */
    @Throws(JSONException::class)
    private fun httpRes_RegFido1(p_jsonObject: JSONObject) {
        LogPrinter.CF_debug("!----------------------------------------------------------")
        LogPrinter.CF_debug("!-- IUFC10M00.httpRes_RegFido1() --FIDO 서버연동 등록 선거래 HTTP 응답")
        LogPrinter.CF_debug("!----------------------------------------------------------")
        LogPrinter.CF_debug("!---- p_jsonObject : $p_jsonObject")

        val jsonKey_errorCode = "errCode"
        val jsonKey_data = "data"

        // 핀/패턴(691310 : 공동FIDO2.0등록)
        val jsonKey_s_tlgr_chas_no = "s_tlgr_chas_no" // 전문 추적번호
        val jsonKey_s_fido_tlgr_no = "s_fido_tlgr_no" // FIDO 추적번호

        // 지문(699962 : 공동FIDO인증정보생성)
        val jsonKey_s_rgtn_rqut_msg = "s_rgtn_rqut_msg" // TODO 없는데?
        val jsonKey_debugMsg = "debugMsg"
        val tmp_errorCode: String
        var tmp_errorMsg: String? = ""

        // --<1>
        if (p_jsonObject.has(jsonKey_errorCode)) {
            tmp_errorCode = p_jsonObject.getString(jsonKey_errorCode) // IUCOA0M00 에서는 반환하는 errorCode 없음. errorCode empty 검사 제외

            // --<2> (최종 에러)
            if ("" != tmp_errorCode) {
                tmp_errorMsg = p_jsonObject.getString(jsonKey_debugMsg)
            } else {
                // --<3>
                if (p_jsonObject.has(jsonKey_data)) {
                    val tmp_jsonData: JSONObject = p_jsonObject.getJSONObject(jsonKey_data)
                    tmp_tlgr_chas_no = tmp_jsonData.getString(jsonKey_s_tlgr_chas_no) // 전문 추적번호
                    val tmp_bundleApi = Bundle()
                    tmp_bundleApi.putString(Fido2Constant.KEY_AUTH_TECH_CODE, authTechCode) // 인증기술코드
                    tmp_bundleApi.putInt(Fido2Constant.KEY_CODE, Fido2Constant.FIDO_CODE_REG_1_IN) // 단계 코드 (필수는 아니네..?)

                    // --<> (지문)
                    if ((authTechCode == Fido2Constant.AUTH_TECH_FINGER)) {
                        val fingerModule = FingerModule(applicationContext)
                        // --<4> (단말기에 지문 있음)
                        if (fingerModule.CF_hasFIngerprint()) {
                            tmp_bundleApi.putString(Fido2Constant.KEY_FIDO, tmp_jsonData.getString(jsonKey_s_rgtn_rqut_msg))
                            tmp_bundleApi.putString(Fido2Constant.KEY_TLS_CERT, Fido2Constant.TLS_CERTIFICATE)
                            reqBioReg(tmp_bundleApi) // FIDO API 실행
                        } else {
                            tmp_errorMsg = getResources().getString(R.string.dlg_not_reg_finger)
                        }
                    } else {
                        tmp_bundleApi.putString(Fido2Constant.KEY_TRID, tmp_jsonData.getString(jsonKey_s_fido_tlgr_no)) // FIDO 추적번호
                        reqBioReg(tmp_bundleApi) // FIDO API 실행
                    }
                } else {
                    tmp_errorMsg = getResources().getString(R.string.dlg_error_server_2)
                }
            }
        } else {
            tmp_errorMsg = getResources().getString(R.string.dlg_error_server_1)
        }

        // -- 에러가 있는 경우 에러 메시지 팝업
        if (!TextUtils.isEmpty(tmp_errorMsg)) {
            CF_finishCancelMsg(tmp_errorMsg)
        }
    }

    /**
     * FIDO 서버연동 등록 본거래 HTTP 요청
     */
    private fun httpReq_RegFido2() {
        LogPrinter.CF_debug("!----------------------------------------------------------")
        LogPrinter.CF_debug("!-- IUFC10M00.httpReq_RegFido2() --FIDO 서버연동 등록 본거래 HTTP 요청")
        LogPrinter.CF_debug("!----------------------------------------------------------")

        val builder: Uri.Builder = Uri.Builder()
        builder.appendQueryParameter("csno"        , mCsno)
        builder.appendQueryParameter("auth_tech"   , authTechCode) // 100:지문, 116:핀, 121:패턴
        builder.appendQueryParameter("device_id"   , phoneInfoBundle!!.getString(Fido2Constant.KEY_DEVICE_ID))
        builder.appendQueryParameter("appVersion"  , phoneInfoBundle!!.getString(Fido2Constant.KEY_VERSION))
        builder.appendQueryParameter("osType"      , Fido2Constant.OS_TYPE) // (고정값) 로그 기록 위해 추가
        builder.appendQueryParameter("serviceCode" , Fido2Constant.SVC_CODE) // (고정값) 서비스코드 고정
        builder.appendQueryParameter("tlgr_chas_no", tmp_tlgr_chas_no) // 전문추적번호

        // 지문만(그 외 "")
        builder.appendQueryParameter("tgtn_rqut_msg_len", "" + tmp_responseMsg!!.length)
        builder.appendQueryParameter("tgtn_rqut_msg"    , tmp_responseMsg)
        builder.appendQueryParameter("dipo_oppb_key_len", "" + tmp_publicKey!!.length)
        builder.appendQueryParameter("dipo_oppb_key"    , tmp_publicKey)

        LogPrinter.CF_debug("!-- tmp_builder : $builder")
        HttpConnections.sendPostData(
                EnvConfig.host_url + EnvConfig.URL_FIDO_REG2,
                builder.build().encodedQuery,
                handler,
                HANDLERJOB_REG_FIDO_2,
                HANDLERJOB_ERROR_REG_FIDO_2)
    }

    /**
     * FIDO 서버연동 등록 본거래 HTTP 응답
     * @param p_jsonObject  JSONObject
     */
    @Throws(JSONException::class)
    private fun httpRes_RegFido2(p_jsonObject: JSONObject) {
        LogPrinter.CF_debug("!----------------------------------------------------------")
        LogPrinter.CF_debug("!-- IUFC10M00.httpRes_RegFido2() --FIDO 서버연동 등록 본거래 HTTP 응답")
        LogPrinter.CF_debug("!----------------------------------------------------------")
        LogPrinter.CF_debug("!---- p_jsonObject : $p_jsonObject")

        val jsonKey_errorCode = "errCode"
        val jsonKey_data = "data"
        val jsonKey_s_not_rcgn_cpat_no = "s_not_rcgn_cpat_no"
        val jsonKey_debugMsg = "debugMsg"
        var tmp_errorCode: String? = ""
        var tmp_errorMsg: String? = ""
        if (p_jsonObject.has(jsonKey_errorCode)) {
            tmp_errorCode = p_jsonObject.getString(jsonKey_errorCode)

            // -- (전문 에러)
            if (!("" == p_jsonObject.getString(jsonKey_errorCode))) {
                tmp_errorMsg = p_jsonObject.getString(jsonKey_debugMsg)
            } else {
                // --<> (DATA 있음)
                if (p_jsonObject.has(jsonKey_data)) {
                    val tmp_jsonData: JSONObject = p_jsonObject.getJSONObject(jsonKey_data)

                    // --<> (최종성공)
                    if ((tmp_jsonData.getString("errCode") == "000000")) {
                        // -- (지문일 경우 추가) : 등록 정보 저장
                        if ((authTechCode == Fido2Constant.AUTH_TECH_FINGER)) {
                            val tmp_encNidcn: String = tmp_jsonData.getString(jsonKey_s_not_rcgn_cpat_no)
                            reqSaveRegInfo(tmp_encNidcn) // -- 본거래 추가요청(지문만)
                        }
                        else {
                            regAuthFinish();
                        }
                    } else {
                        tmp_errorMsg = getResources().getString(R.string.dlg_error_reg_finger_nidcn)
                    }
                } else {
                    tmp_errorMsg = getResources().getString(R.string.dlg_error_server_2)
                }
            }
        } else {
            tmp_errorMsg = getResources().getString(R.string.dlg_error_server_1)
        }

        // 에러가 있는 경우 에러 메시지 팝업
        if (!TextUtils.isEmpty(tmp_errorMsg)) {
            CF_finishCancelMsg(tmp_errorMsg)
        }
    }

    /**
     * 등록 완료 처리
     */
    private fun regAuthFinish() {
        setResult(RESULT_OK)
        finish()
    }

    /**
     * 간편인증 Activity 호출 함수<br></br>
     */
    private fun startIUFC00M00_Auth(authTechCode: String) {
        LogPrinter.CF_debug("!----------------------------------------------------------")
        LogPrinter.CF_debug("!-- IUFC10M00.startIUCOE0M00()")
        LogPrinter.CF_debug("!----------------------------------------------------------")

        val intent = Intent(this, IUFC00M00::class.java)
        intent.putExtra(EnvConstant.KEY_INTENT_AUTH_MODE, AuthMode.SIGN_APP)
        intent.putExtra(EnvConstant.KEY_INTENT_AUTH_TECH_CODE, authTechCode)

        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
        startActivityForResult(intent, EnvConfig.REQUESTCODE_ACTIVITY_IUFC00M00)
    }
}