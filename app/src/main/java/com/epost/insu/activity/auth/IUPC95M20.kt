package com.epost.insu.activity.auth

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Message
import android.text.TextUtils
import com.epost.insu.EnvConfig
import com.epost.insu.EnvConfig.AuthMode
import com.epost.insu.EnvConstant
import com.epost.insu.R
import com.epost.insu.common.CommonFunction
import com.epost.insu.common.LogPrinter
import com.epost.insu.network.HttpConnections
import com.epost.insu.network.WeakReferenceHandler
import com.epost.insu.network.WeakReferenceHandler.ObjectHandlerMessage
import org.json.JSONException
import org.json.JSONObject

/**
 * 인증 > 카카오페이인증 로그인/전자서명(등록 이후/전자서명)(화면 없음)
 * @since     :
 * @version   : 1.1
 * @author    : YJH
 * @see
 * <pre>
 *  일회성 투명 Activity로 다이얼로그 같은 팝업 후 Activity 종료 필수
 *  최초 등록 이후 또는 전자서명시 사용함 (정보입력 화면 없이 바로 호출함)
 *  [IUPC95M90] 에서 최종 검증 처리 후 로그인/전자서명 처리
 * ======================================================================
 * 1.4.4    YJH_20201211    최초 등록
 * 1.5.2    NJM_20210322    [subUrl 공통파일로 변경]
 * 1.5.4    NJM_20210429    [카카오페이앱 인증 S320] 앱to앱 인증 추가
 * 1.5.4    NJM_20210506    [IUFC34M00 임시고객번호 삭제]
 * =======================================================================
 * copyright : (주)프로니스정보기술 | http://www.phronis.co.kr
 * </pre>
 */
class IUPC95M20 : Activity_Auth(), ObjectHandlerMessage {
    private val HANDLERJOB_KAKAO_AUTH_1: Int = 0
    private val HANDLERJOB_ERROR_KAKAO_AUTH_1: Int = 1

    private var mAuthMode: AuthMode? = null // 요청모드("LOGIN_APP", "SIGN_APP", ..)
    private var mAuthCsno: String? = null // 인증요청자 csno
    private var mAuthName: String? = null // 인증요청자 이름

    override fun onActivityResult(p_requestCode: Int, p_resultCode: Int, p_data: Intent?) {
        super.onActivityResult(p_requestCode, p_resultCode, p_data)

        LogPrinter.CF_debug("!-----------------------------------------------------------")
        LogPrinter.CF_debug("!-- IUPC95M20.onActivityResult()")
        LogPrinter.CF_debug("!-----------------------------------------------------------")
        LogPrinter.CF_debug("!-- requestCode : [$p_requestCode] / resultCode : [$p_resultCode]")

        // --<> 카카오앱(로그인/전자서명) 검증 완료
        if (p_requestCode == EnvConfig.REQUESTCODE_ACTIVITY_IUPC95M90 && p_resultCode == RESULT_OK) {
            setResult(RESULT_OK)
            finish()
        } else {
            setResult(RESULT_CANCELED)
            finish()
        }
    }

    override fun onBackPressed() {
        LogPrinter.CF_debug("!-----------------------------------------------------------")
        LogPrinter.CF_debug("!-- IUPC95M20.onBackPressed() --카카오인증")
        LogPrinter.CF_debug("!-----------------------------------------------------------")
        setResult(RESULT_CANCELED)
        finish()
    }

    override fun setInit() {
        LogPrinter.CF_debug("!-----------------------------------------------------------")
        LogPrinter.CF_debug("!-- IUPC95M20.setInit() --카카오인증")
        LogPrinter.CF_debug("!-----------------------------------------------------------")
        handler = WeakReferenceHandler(this)
        if (intent.hasExtra(EnvConstant.KEY_INTENT_AUTH_MODE)) {
            mAuthMode = intent.getSerializableExtra(EnvConstant.KEY_INTENT_AUTH_MODE) as AuthMode?
        }
        if (getIntent().hasExtra(EnvConstant.KEY_INTENT_AUTH_CSNO)) {    // 고객번호
            mAuthCsno = intent.extras!!.getString(EnvConstant.KEY_INTENT_AUTH_CSNO)
        }
        if (getIntent().hasExtra(EnvConstant.KEY_INTENT_AUTH_NAME)) {    // 고객명
            mAuthName = intent.extras!!.getString(EnvConstant.KEY_INTENT_AUTH_NAME)
        }
        if (("" == mAuthCsno) || ("" == mAuthName)) {
            setResult(RESULT_CANCELED)
            finish()
        }

//        setContentView(R.layout.iufc00m00)
    }

    override fun setUIControl() {
        LogPrinter.CF_debug("!-----------------------------------------------------------")
        LogPrinter.CF_debug("!-- IUPC95M20.setUIControl() --카카오인증")
        LogPrinter.CF_debug("!-----------------------------------------------------------")
        CF_showProgressDialog()
    }

    override fun onCreate(p_bundle: Bundle?) {
        super.onCreate(p_bundle)
        LogPrinter.CF_debug("!-----------------------------------------------------------")
        LogPrinter.CF_debug("!-- IUPC95M20.onCreate() --카카오인증")
        LogPrinter.CF_debug("!-----------------------------------------------------------")

        //-- 카카오인증 실행
        httpReq_kakaoAuth1()
    }

    override fun onPause() {
        super.onPause()
        LogPrinter.CF_debug("!-----------------------------------------------------------")
        LogPrinter.CF_debug("!-- IUPC95M20.onPause() --카카오인증")
        LogPrinter.CF_debug("!-----------------------------------------------------------")
        overridePendingTransition(0, 0) // end anition 해지
    }

    override fun onResume() {
        super.onResume()
        LogPrinter.CF_debug("!-----------------------------------------------------------")
        LogPrinter.CF_debug("!-- IUPC95M20.onResume() --카카오인증")
        LogPrinter.CF_debug("!-----------------------------------------------------------")
    }

    // #############################################################################################
    //  Http 관련
    // #############################################################################################
    override fun handleMessage(p_message: Message) {
        LogPrinter.CF_debug("!-----------------------------------------------------------")
        LogPrinter.CF_debug("!-- IUPC95M20.handleMessage() --카카오인증")
        LogPrinter.CF_debug("!-----------------------------------------------------------")
        if (!isDestroyed()) {
            when (p_message.what) {
                HANDLERJOB_KAKAO_AUTH_1 -> try {
                    httpRes_kakaoAuth1(JSONObject(p_message.obj as String?))
                } catch (e: JSONException) {
                    LogPrinter.CF_line()
                    LogPrinter.CF_debug(getResources().getString(R.string.log_json_exception))
                }
                HANDLERJOB_ERROR_KAKAO_AUTH_1 -> CommonFunction.CF_showCustomAlertDilaog(this, p_message.obj as String?, getResources().getString(R.string.btn_ok))
                else -> {
                }
            }
        }
    }

    /**
     * 카카오인증 요청
     */
    private fun httpReq_kakaoAuth1() {
        LogPrinter.CF_debug("!-----------------------------------------------------------")
        LogPrinter.CF_debug("!-- IUPC95M20.httpReq_kakaoAuth1() --카카오인증1 요청")
        LogPrinter.CF_debug("!-----------------------------------------------------------")
        val tmp_builder: Uri.Builder = Uri.Builder()
        tmp_builder.appendQueryParameter("csno", mAuthCsno)
        tmp_builder.appendQueryParameter("service_code", "S320") // (S320:앱요청 고정)
        HttpConnections.sendPostData(
                EnvConfig.host_url + EnvConfig.URL_KAKAO_SIGN_AUTH1,
                tmp_builder.build().getEncodedQuery(),
                handler,
                HANDLERJOB_KAKAO_AUTH_1,
                HANDLERJOB_ERROR_KAKAO_AUTH_1)
    }

    /**
     * 카카오인증 요청 응답
     * @param p_jsonObject      JSONObject
     */
    @Throws(JSONException::class)
    private fun httpRes_kakaoAuth1(p_jsonObject: JSONObject) {
        LogPrinter.CF_debug("!-----------------------------------------------------------")
        LogPrinter.CF_debug("!-- IUPC95M20.httpRes_kakaoAuth1() --카카오인증1 응답")
        LogPrinter.CF_debug("!-----------------------------------------------------------")
        LogPrinter.CF_debug("!---- p_jsonObject : $p_jsonObject")
        val jsonKey_errorCode = "errCode"
        val jsonKey_debugMsg = "debugMsg"
        val tmp_errorCode: String
        var tmp_errorMsg = ""

        // --<1> (통신 정상)
        if (p_jsonObject.has(jsonKey_errorCode)) {
            tmp_errorCode = p_jsonObject.getString(jsonKey_errorCode)

            // --<2> (전문 에러)
            if (!TextUtils.isEmpty(tmp_errorCode)) {
                tmp_errorMsg = p_jsonObject.getString(jsonKey_debugMsg)
            }
            // --<2> (전문 성공)
            if (p_jsonObject.has("flagOk")) {
                // --<3> (최종 성공)
                if (p_jsonObject.getBoolean("flagOk")) {
                    val res_result: String = p_jsonObject.getString("result") // 발송여부(Y,N)
                    val res_tx_id: String = p_jsonObject.getString("tx_id") // 인증확인시 재전송 값
                    if (("Y" == res_result)) {
                        startIUPC95M90(res_tx_id)
                    } else {
                        // 에러코드 없지만 실패
                        val errMsg: String = getResources().getString(R.string.dlg_error_kakao_common) + "[" + tmp_errorCode + "] " + tmp_errorMsg
                        showCustomDialog(errMsg)
                    }
                } else {
                    val errMsg: String = getResources().getString(R.string.dlg_error_kakao_common) + "[" + tmp_errorCode + "] " + tmp_errorMsg
                    showCustomDialog(errMsg)
                }
            } else {
                showCustomDialog(getResources().getString(R.string.dlg_error_server_2))
            }
        } else {
            showCustomDialog(getResources().getString(R.string.dlg_error_server_1))
        }

        // -- 에러가 있는 경우 에러 메시지 팝업
        if (!TextUtils.isEmpty(tmp_errorMsg)) {
            showCustomDialog(tmp_errorMsg)
        }
    }

    /**
     * 카카오앱(카카오페이인증) 실행
     * @param p_tx_id   카카오 거래ID
     */
    private fun startIUPC95M90(p_tx_id: String) {
        val intent = Intent(this, IUPC95M90::class.java)
        intent.putExtra(EnvConstant.KEY_INTENT_AUTH_KAKAO_TX_ID, p_tx_id) // 카카오페이 거래ID
        intent.putExtra(EnvConstant.KEY_INTENT_AUTH_MODE, mAuthMode) // 인증모드
        intent.putExtra(EnvConstant.KEY_INTENT_AUTH_CSNO, mAuthCsno) // 인증자 CSNO
        intent.putExtra(EnvConstant.KEY_INTENT_AUTH_NAME, mAuthName) // 인증자 이름
        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
        startActivityForResult(intent, EnvConfig.REQUESTCODE_ACTIVITY_IUPC95M90)
    }
}