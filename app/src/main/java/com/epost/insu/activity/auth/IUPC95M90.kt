package com.epost.insu.activity.auth

import android.content.DialogInterface
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Message
import android.text.TextUtils
import android.widget.Button
import com.epost.insu.EnvConfig
import com.epost.insu.EnvConfig.AuthDvsn
import com.epost.insu.EnvConfig.AuthMode
import com.epost.insu.EnvConstant
import com.epost.insu.R
import com.epost.insu.common.CommonFunction
import com.epost.insu.common.LogPrinter
import com.epost.insu.dialog.CustomDialog
import com.epost.insu.network.HttpConnections
import com.epost.insu.network.WeakReferenceHandler
import com.epost.insu.network.WeakReferenceHandler.ObjectHandlerMessage
import org.json.JSONException
import org.json.JSONObject

/**
 * 인증 > 카카오페이인증 로그인/전자서명 > 최종 검증(화면 없음)
 * @since     :
 * @version   : 1.5.4
 * @author    : NJM
 * @see
 * <pre>
 *  일회성 투명 Activity로 다이얼로그 같은 팝업 후 Activity 종료 필수
 *  카카오앱(외부앱)에서 인증 후 검증 처리 및 완료 처리 화면
 * ======================================================================
 * 1.5.4    NJM_20210429    [카카오페이앱 인증 S320] 앱to앱 인증 추가
 * 1.5.8    NJM_20210630    [로그인처리 공통화] CF_setLogin() 공통 로그인 처리 함수
 * =======================================================================
 * copyright : (주)프로니스정보기술 | http://www.phronis.co.kr
 * </pre>
 */
class IUPC95M90 : Activity_Auth(), ObjectHandlerMessage {
    private val HANDLERJOB_KAKAO_AUTH_2: Int = 2
    private val HANDLERJOB_ERROR_KAKAO_AUTH_2: Int = 3

    private var mAuthMode: AuthMode? = null // 요청모드("LOGIN", "SIGN")
    private var mTxId: String? = ""
    private var mSig: String? = ""
    private val mFlag: String = ""
    private var mUserCsno: String = ""
    private var mUserName: String = ""
    private var mTempKey: String = ""

    override fun onBackPressed() {
        LogPrinter.CF_debug("!-----------------------------------------------------------")
        LogPrinter.CF_debug("!-- IUPC95M90.onBackPressed() --카카오인증")
        LogPrinter.CF_debug("!-----------------------------------------------------------")
        setResult(RESULT_CANCELED)
        finish()
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        LogPrinter.CF_debug("!-----------------------------------------------------------")
        LogPrinter.CF_debug("!-- IUPC95M90.onNewIntent() --카카오인증")
        LogPrinter.CF_debug("!-----------------------------------------------------------")

        LogPrinter.CF_debug("!-- intent : " + intent.toString())

        // -- (카카오앱인증 완료) 검증 실행
        val uri: Uri? = intent.getData()
        if (uri != null) {
            LogPrinter.CF_debug("!-- getIntent().getData() : " + uri.toString())
            val tmp_txId: String? = uri.getQueryParameter("tx_id") // 거래ID (비교용)
            val isOk: String? = uri.getQueryParameter("flag") // 성공여부(1:성공, 0:실패)
            mSig = uri.getQueryParameter("sig") // 전자서명키
            val rtnCode: String? = uri.getQueryParameter("code") // 카카오페이인증 오류코드
            // -- (요청성공)
            if (("success" == isOk)) {
                if ((mTxId == tmp_txId)) {
                    // -- 검증실행
                    httpReq_kakao_auth_2()
                } else {
                    CommonFunction.CF_showCustomDialogFinishActivity(this, "중복요청입니다. 다시 실행해주세요")
                }
            } else {
                CommonFunction.CF_showCustomDialogFinishActivity(this, getErrMsgForCode(rtnCode))
            }
        }
    }

    override fun setInit() {
        LogPrinter.CF_debug("!-----------------------------------------------------------")
        LogPrinter.CF_debug("!-- IUPC95M90.setInit() --카카오인증")
        LogPrinter.CF_debug("!-----------------------------------------------------------")
        handler = WeakReferenceHandler(this)
        if (intent != null) {
            if (intent.hasExtra(EnvConstant.KEY_INTENT_AUTH_MODE)) {
                mAuthMode = intent.extras!!.getSerializable(EnvConstant.KEY_INTENT_AUTH_MODE) as AuthMode?
            }
            if (intent.hasExtra(EnvConstant.KEY_INTENT_AUTH_KAKAO_TX_ID)) {
                mTxId = intent.extras!!.getString(EnvConstant.KEY_INTENT_AUTH_KAKAO_TX_ID)
            }
            if (intent.hasExtra(EnvConstant.KEY_INTENT_AUTH_CSNO)) {
                mUserCsno = intent.extras!!.getString(EnvConstant.KEY_INTENT_AUTH_CSNO)!!
            }
            if (intent.hasExtra(EnvConstant.KEY_INTENT_AUTH_NAME)) {
                mUserName = intent.extras!!.getString(EnvConstant.KEY_INTENT_AUTH_NAME)!!
            }

            // (최초실행) 카카오앱 호출
            if (!("" == mTxId)) {
                startKakaoApp()
            } else {
                LogPrinter.CF_debug("!-- 카카오톡 tx_id 없음")
            }
        }
        LogPrinter.CF_debug("!-- mUserCsno  : $mUserCsno")
        LogPrinter.CF_debug("!-- mUserName  : $mUserName")
        LogPrinter.CF_debug("!-- mAuthMode  : $mAuthMode")
        LogPrinter.CF_debug("!-- mTxId      : $mTxId")

        if (("" == mUserCsno) || ("" == mUserName)) {
            setResult(RESULT_CANCELED)
            finish()
        }
    }

    override fun setUIControl() {
        LogPrinter.CF_debug("!-----------------------------------------------------------")
        LogPrinter.CF_debug("!-- IUPC95M90.setUIControl() --카카오인증")
        LogPrinter.CF_debug("!-----------------------------------------------------------")
        CF_showProgressDialog()
    }

    override fun onCreate(p_bundle: Bundle?) {
        super.onCreate(p_bundle)
        LogPrinter.CF_debug("!-----------------------------------------------------------")
        LogPrinter.CF_debug("!-- IUPC95M90.onCreate() --카카오인증")
        LogPrinter.CF_debug("!-----------------------------------------------------------")
    }

    override fun onResume() {
        super.onResume()
        LogPrinter.CF_debug("!-----------------------------------------------------------")
        LogPrinter.CF_debug("!-- IUPC95M90.onResume() --카카오인증")
        LogPrinter.CF_debug("!-----------------------------------------------------------")
    }

    override fun onPause() {
        super.onPause()
        LogPrinter.CF_debug("!-----------------------------------------------------------")
        LogPrinter.CF_debug("!-- IUPC95M90.onPause() --카카오인증")
        LogPrinter.CF_debug("!-----------------------------------------------------------")
        overridePendingTransition(0, 0) // end anition 해지
    }

    // #############################################################################################
    //  Http 관련
    // #############################################################################################
    override fun handleMessage(p_message: Message) {
        LogPrinter.CF_debug("!-----------------------------------------------------------")
        LogPrinter.CF_debug("!-- IUPC95M90.handleMessage() --카카오인증")
        LogPrinter.CF_debug("!-----------------------------------------------------------")

        if (!isDestroyed()) {
            when (p_message.what) {
                HANDLERJOB_KAKAO_AUTH_2 -> try {
                    httpRes_kakao_auth_2(JSONObject(p_message.obj as String?))
                } catch (e: JSONException) {
                    LogPrinter.CF_line()
                    LogPrinter.CF_debug(getResources().getString(R.string.log_json_exception))
                }
                HANDLERJOB_ERROR_KAKAO_AUTH_2 -> CommonFunction.CF_showCustomAlertDilaog(this, p_message.obj as String?, getResources().getString(R.string.btn_ok))
                else -> {
                }
            }
        }
    }

    /**
     * 카카오 인증 확인
     */
    private fun httpReq_kakao_auth_2() {
        LogPrinter.CF_debug("!-----------------------------------------------------------")
        LogPrinter.CF_debug("!-- IUPC95M90.httpReq_kakao_auth_2() --카카오인증2 검증 요청")
        LogPrinter.CF_debug("!-----------------------------------------------------------")

        LogPrinter.CF_debug("!---- csno(sCsno) : $mUserCsno")
        LogPrinter.CF_debug("!---- tx_id       : $mTxId")
        LogPrinter.CF_debug("!---- flag        : $mFlag")
        LogPrinter.CF_debug("!---- sig         : $mSig")
        val tmp_builder: Uri.Builder = Uri.Builder()

        tmp_builder.appendQueryParameter("csno", mUserCsno)
        tmp_builder.appendQueryParameter("sCsno", mUserCsno)
        tmp_builder.appendQueryParameter("tx_id", mTxId)
        var tmp_url: String? = EnvConfig.host_url + EnvConfig.URL_KAKAO_REG_AUTH2
        // -- 전자서명시 검증 URL
        if (mAuthMode == AuthMode.SIGN_APP || mAuthMode == AuthMode.SIGN_WEB) {
            tmp_url = EnvConfig.host_url + EnvConfig.URL_KAKAO_SIGN_AUTH2
        }
        HttpConnections.sendPostData(
                tmp_url,
                tmp_builder.build().getEncodedQuery(),
                handler,
                HANDLERJOB_KAKAO_AUTH_2,
                HANDLERJOB_ERROR_KAKAO_AUTH_2)
    }

    /**
     * 카카오 인증 확인 응답
     * @param p_jsonObject      JSONObject
     */
    @Throws(JSONException::class)
    private fun httpRes_kakao_auth_2(p_jsonObject: JSONObject) {
        LogPrinter.CF_debug("!-----------------------------------------------------------")
        LogPrinter.CF_debug("!-- IUPC95M90.httpRes_kakao_auth_2() --카카오인증2 검증 확인")
        LogPrinter.CF_debug("!-----------------------------------------------------------")
        LogPrinter.CF_debug("!---- p_jsonObject : " + p_jsonObject.toString())

        val jsonKey_errorCode = "errCode"
        val jsonKey_debugMsg = "debugMsg"
        val jsonKey_flagOk = "flagOk"
        val jsonKey_tempKey = "tempKey"
        val tmp_errorCode: String
        var tmp_errorMsg: String? = ""
        if (p_jsonObject.has(jsonKey_errorCode)) {
            tmp_errorCode = p_jsonObject.getString(jsonKey_errorCode)

            // -- (전문에러)
            if (!TextUtils.isEmpty(tmp_errorCode)) {
                if (("E201" == tmp_errorCode)) {
                    // 서명 대기중
                    val btnAuth: Button = findViewById(R.id.btnFill)
                    showCustomDialog(getResources().getString(R.string.dlg_err_kakaopay_E201),
                            getResources().getString(R.string.btn_cancel),
                            getResources().getString(R.string.btn_kakaopay_verify),
                            btnAuth,
                            object : DialogInterface.OnDismissListener {
                                override fun onDismiss(dialog: DialogInterface) {
                                    if (!(dialog as CustomDialog).CF_getCanceled()) {
                                        httpReq_kakao_auth_2()
                                    } else {
                                        setResult(RESULT_CANCELED)
                                        finish()
                                    }
                                }
                            }
                    )
                } else {
                    tmp_errorMsg = p_jsonObject.getString(jsonKey_debugMsg)
                }
            }
            else if (p_jsonObject.has(jsonKey_flagOk)) {
                val res_flagOk: Boolean = p_jsonObject.getBoolean(jsonKey_flagOk)

                // -- (최종성공)
                if (res_flagOk) {
                    // --<> (로그인 요청시)
                    if (mAuthMode == AuthMode.LOGIN_APP || mAuthMode == AuthMode.LOGIN_WEB) {
                        // -- 로그인 처리
                        CF_setLogin(true, AuthDvsn.KAKAOPAY, mUserCsno, mUserName, p_jsonObject.optString(jsonKey_tempKey) )

                        // --<> (앱요청)
                        if (mAuthMode == AuthMode.LOGIN_APP) {
                            setResult(RESULT_OK)
                            finish()
                        } else {
                            startActivityComplete()
                        }
                    } else {
                        setResult(RESULT_OK)
                        finish()
                    }
                }
            } else {
                tmp_errorMsg = getResources().getString(R.string.dlg_error_server_2)
            }
        } else {
            tmp_errorMsg =


                getResources().getString(R.string.dlg_error_server_1)
        }

        // -- 에러가 있는 경우 에러 메시지 팝업
        if (!TextUtils.isEmpty(tmp_errorMsg)) {
            showCustomDialog(tmp_errorMsg)
        }
    }

    private fun getErrMsgForCode(code: String?): String {
        var msg = "인증에 실패하였습니다. 잠시 후에 다시 시도해주세요. \n"
        var msgAdd = "[$code]"
        when (code) {
            "1000" -> msgAdd = "[약관에 동의가 필요합니다.]"
            "1001", "1002", "1003", "1004", "4004", "4005", "4008", "5000" -> {
                msg = "카카오페이인증을 취소하였습니다."
                msgAdd = ""
            }
            "2000" -> msgAdd = "[사용자 상태 체크 실패]"
            "2001" -> msgAdd = "[CI중복 상태]"
            "3000" -> msgAdd = "[인증서 발급 요청 실패]"
            "3001" -> msgAdd = "[사용기관 등록 실패]"
            "4000" -> msgAdd = "[서명 요청 실패]"
            "4001" -> msgAdd = "[서명 요청 유효성 검증 실패]"
            "4002" -> msgAdd = "[인증서 상태가 유효하지 않음]"
            "4003" -> msgAdd = "[서명 데이터 요청 실패]"
            "4006" -> msgAdd = "[인증 비밀번호를 5회 잘못 입력함]"
            "4007" -> msgAdd = "[개인키 로드 실패]"
            "4009" -> msgAdd = "[서명 확인 실패]"
        }
        return msg + msgAdd
    }

    /**
     * 카카오페이인증앱 호출
     */
    fun startKakaoApp() {
        val success = "epostinsu://kakaopay_auth?flag=success"
        val fail    = "epostinsu://kakaopay_auth?flag=fail"
        val uri: String = "kakaotalk://kakaopay/cert/sign?tx_id=" + mTxId + "&success=" + success + "&fail=" + fail
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(uri))
        startActivity(intent)
    }

    /**
     * 인증 완료 Activity 호출 함수
     */
    private fun startActivityComplete() {
        val tmp_intent = Intent(this, IUFC00M09::class.java)
        tmp_intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
        tmp_intent.putExtra("authDvsn", AuthDvsn.KAKAOPAY)
        startActivity(tmp_intent)
    }
}