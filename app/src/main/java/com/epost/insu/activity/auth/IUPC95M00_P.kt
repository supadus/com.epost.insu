package com.epost.insu.activity.auth

import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.os.Message
import android.telephony.TelephonyManager
import android.view.View
import android.widget.ImageButton
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.epost.insu.EnvConfig
import com.epost.insu.EnvConfig.AuthMode
import com.epost.insu.EnvConstant
import com.epost.insu.R
import com.epost.insu.adapter.CustomPagerAdapter
import com.epost.insu.common.CommonFunction
import com.epost.insu.common.CustomViewPager
import com.epost.insu.common.LogPrinter
import com.epost.insu.dialog.CustomDialog
import com.epost.insu.fragment.IUPC95M00_F
import com.epost.insu.network.HttpConnections
import com.epost.insu.network.WeakReferenceHandler
import com.epost.insu.network.WeakReferenceHandler.ObjectHandlerMessage
import org.json.JSONException
import org.json.JSONObject

/**
 * 인증 > 카카오페이인증 로그인 > 정보입력(최초등록)
 * @since     :
 * @version   : 1.0
 * @author    : YJH
 * @see
 * <pre>
 *  최초 카카오인증 등록시만 사용함
 *  [IUPC95M90] 에서 최종 검증 처리 후 로그인/전자서명 처리
 * ======================================================================
 * 1.4.4    YJH_20201210    최초등록, 카카오페이인증 로그인
 * 1.5.2    NJM_20210317    카카오페이 인증 로그인시 에러 해결 (구글콘솔에러 : com.epost.insu.activity.auth.IUPC95M00_P.CF_requestAuth --java.lang.NullPointerException)
 * 1.5.2    NJM_20210322    [subUrl 공통파일로 변경]
 * 1.5.4    NJM_20210429    [카카오페이앱 인증 S320] 앱to앱 인증 추가
 * 1.6.2    NJM_20210802    [2021년 대우 취약점] 2차본 : 부적절한 예외 처리
 * =======================================================================
 * copyright : (주)프로니스정보기술 | http://www.phronis.co.kr
 * </pre>
 */
class IUPC95M00_P : Activity_Auth(), ObjectHandlerMessage {
    private val HANDLERJOB_REQUEST: Int = 0 // 인증 요청 성공
    private val HANDLERJOB_ERROR_REQUEST: Int = 1 // 인증 요청 실패

    private var fragmentReqKakaopay: IUPC95M00_F? = null // 카카오페이인증 요청 Fragment
    private lateinit var pager: CustomViewPager
    private var mAuthName: String? = null // 인증요청자 이름

    override fun onActivityResult(p_requestCode: Int, p_resultCode: Int, p_data: Intent?) {
        super.onActivityResult(p_requestCode, p_resultCode, p_data)
        LogPrinter.CF_debug("!-----------------------------------------------------------")
        LogPrinter.CF_debug("!-- IUPC95M00_P.onActivityResult()")
        LogPrinter.CF_debug("!-----------------------------------------------------------")

        // --<> 카카오앱 인증 완료
        if (p_requestCode == EnvConfig.REQUESTCODE_ACTIVITY_IUPC95M90 && p_resultCode == RESULT_OK) {
            setResult(RESULT_OK)
            finish()
        } else {
            setResult(RESULT_CANCELED)
            finish()
        }
    }

    override fun onBackPressed() {
        if (pager.currentItem == 1) {
            val tmp_dlg = CustomDialog(this)
            tmp_dlg.show()
            tmp_dlg.CF_setTextContent(getResources().getString(R.string.dlg_cancel_kakaopay_auth))
            tmp_dlg.CF_setDoubleButtonText(getResources().getString(R.string.btn_no), getResources().getString(R.string.btn_yes))
            tmp_dlg.setOnDismissListener { dialog ->
                if (!(dialog as CustomDialog).CF_getCanceled()) {
                    //fragmentInputCode.CF_stopTimer();   삭제
                    pager.currentItem = 0
                }
            }
        } else {
            if (fragmentReqKakaopay != null && fragmentReqKakaopay!!.isAdded) {
                val customDialog = CustomDialog(this)
                customDialog.show()
                customDialog.CF_setTextContent(getResources().getString(R.string.dlg_cancel_kakaopay_auth))
                customDialog.CF_setDoubleButtonText(getResources().getString(R.string.btn_no), getResources().getString(R.string.btn_yes))
                customDialog.setOnDismissListener { dialog ->
                    if (!(dialog as CustomDialog).CF_getCanceled()) {
                        setResult(RESULT_CANCELED)
                        finish()
                    }
                }
            }
        }
    }

    override fun setInit() {
        LogPrinter.CF_debug("!-----------------------------------------------------------")
        LogPrinter.CF_debug("!-- IUPC95M00_P.setInit()")
        LogPrinter.CF_debug("!-----------------------------------------------------------")

        setContentView(R.layout.iupc95m00_p)
        handler = WeakReferenceHandler(this)
    }

    override fun setUIControl() {
        // -- 타이틀바 세팅
        setTitleBarUI()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        LogPrinter.CF_debug("!-----------------------------------------------------------")
        LogPrinter.CF_debug("!-- IUPC95M00_P.onCreate()")
        LogPrinter.CF_debug("!-----------------------------------------------------------")

        if (!checkKakaotalk()) {
            LogPrinter.CF_line()
            LogPrinter.CF_debug("카카오톡이 설치되지 않았습니다.")
        } else {
            LogPrinter.CF_line()
            LogPrinter.CF_debug("카카오톡이 설치되어 있습니다.")
        }

        // -- Fragment 세팅
        setFragments(savedInstanceState)
        val adapter = CustomPagerAdapter(supportFragmentManager, arrayOf(fragmentReqKakaopay))
        pager = findViewById(R.id.viewPager)
        pager.CF_setPagingEnabled(false)
        pager.adapter = adapter
        pager.importantForAccessibility = View.IMPORTANT_FOR_ACCESSIBILITY_NO
    }

    override fun onResume() {
        super.onResume()
        LogPrinter.CF_debug("!-----------------------------------------------------------")
        LogPrinter.CF_debug("!-- IUPC95M00_P.onResume()")
        LogPrinter.CF_debug("!-----------------------------------------------------------")
    }

    override fun onPause() {
        super.onPause()
        LogPrinter.CF_debug("!-----------------------------------------------------------")
        LogPrinter.CF_debug("!-- IUPC95M00_P.onPause()")
        LogPrinter.CF_debug("!-----------------------------------------------------------")
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        LogPrinter.CF_debug("!-----------------------------------------------------------")
        LogPrinter.CF_debug("!-- IUPC95M00_P.onSaveInstanceState()")
        LogPrinter.CF_debug("!-----------------------------------------------------------")
        if (fragmentReqKakaopay != null && fragmentReqKakaopay!!.isAdded) {
            supportFragmentManager.putFragment(outState, IUPC95M00_F::class.java.name, fragmentReqKakaopay!!)
        }
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        LogPrinter.CF_debug("!-----------------------------------------------------------")
        LogPrinter.CF_debug("!-- IUPC95M00_P.onRestoreInstanceState()")
        LogPrinter.CF_debug("!-----------------------------------------------------------")
    }

    /**
     * Fragment 세팅
     */
    private fun setFragments(p_bundle: Bundle?) {
        if (p_bundle != null) {
            fragmentReqKakaopay = supportFragmentManager.getFragment(p_bundle, IUPC95M00_F::class.java.name) as IUPC95M00_F?
        } else {
            fragmentReqKakaopay = Fragment.instantiate(this, IUPC95M00_F::class.java.name) as IUPC95M00_F?
        }
    }

    /**
     * 타이틀바 레이아웃 세팅
     */
    private fun setTitleBarUI() {
        // 타이틀 세팅
        val txtTitle: TextView = findViewById(R.id.title_bar_textTitle)
        txtTitle.text = resources.getString(R.string.title_kakaopay_auth)

        // left 버튼 세팅
        val btnLeft: ImageButton = findViewById(R.id.title_bar_imgBtnLeft)
        btnLeft.visibility = View.VISIBLE
        btnLeft.setOnClickListener {
            if (pager.currentItem == 1) {
                showCustomDialog(resources.getString(R.string.dlg_cancel_kakaopay_auth),
                        resources.getString(R.string.btn_no),
                        resources.getString(R.string.btn_yes),
                        btnLeft
                ) { dialog ->
                    if (!(dialog as CustomDialog).CF_getCanceled()) {
                        //fragmentInputCode.CF_stopTimer();
                        pager.currentItem = 0
                    } else if (CommonFunction.CF_checkAccessibilityTurnOn(applicationContext)) {
                        clearAllFocus()
                        btnLeft.requestFocus()
                    }
                    btnLeft.isFocusableInTouchMode = false
                }
            } else {
                showCustomDialog(resources.getString(R.string.dlg_cancel_kakaopay_auth),
                        resources.getString(R.string.btn_no),
                        resources.getString(R.string.btn_yes),
                        btnLeft
                ) { dialog ->
                    if (!(dialog as CustomDialog).CF_getCanceled()) {
                        setResult(RESULT_CANCELED)
                        finish()
                    } else if (CommonFunction.CF_checkAccessibilityTurnOn(applicationContext)) {
                        clearAllFocus()
                        btnLeft.requestFocus()
                    }
                    btnLeft.isFocusableInTouchMode = false
                }
            }
        }
    }

    /**
     * 카카오톡 설치 여부 확인
     * @return boolean
     */
    private fun checkKakaotalk(): Boolean {
        var bKakaotalkInstalled = true
        try {
            packageManager.getPackageInfo("com.kakao.talk", PackageManager.GET_ACTIVITIES)
        } catch (e: PackageManager.NameNotFoundException) {
            e.message
            bKakaotalkInstalled = false
        }
        return bKakaotalkInstalled
    }

    /**
     * 단말 정보 조회(휴대폰 번호)
     * @since  2019-04-02
     * @return phonNo - 휴대폰번호)
     */
    @SuppressLint("MissingPermission")
    fun CF_getPhoneNumber(): String? {
        val tm: TelephonyManager = getSystemService(TELEPHONY_SERVICE) as TelephonyManager
        //String telPhonNo2 = tm.getSimOperator();    // 망사업자
        var phoneNo: String? = tm.getLine1Number() // 휴대폰번호
        if (phoneNo != null && phoneNo.startsWith("+82")) {
            phoneNo = phoneNo.replace("+82", "0")
        }
        return phoneNo
    }

    /**
     * 휴대폰 본인인증청구 취소
     * - 휴대폰번호 확인 불가
     */
    fun CF_cancelRequest() {
        val customDialog = CustomDialog(this)
        customDialog.show()
        customDialog.CF_setTextContent(getResources().getString(R.string.dlg_cancel_kakaopay_request))
        customDialog.CF_setBackKeyMode(CustomDialog.BackMode.CANCELED)
        customDialog.CF_setSingleButtonText(getResources().getString(R.string.btn_ok))
        customDialog.setOnDismissListener { //fragmentInputCode.CF_stopTimer();
            //pager.setCurrentItem(0);
            clearAllFocus()
            setResult(RESULT_CANCELED)
            finish()
        }
    }

    // #############################################################################################
    // Http 관련 함수
    // #############################################################################################
    override fun handleMessage(p_message: Message) {
        LogPrinter.CF_debug("!-----------------------------------------------------------")
        LogPrinter.CF_debug("!-- IUPC95M00_P.handleMessage()")
        LogPrinter.CF_debug("!-----------------------------------------------------------")
        if (!isDestroyed) {
            CF_dismissProgressDialog()
            when (p_message.what) {
                HANDLERJOB_REQUEST -> try {
                    httpRes_kakaoAuth1(JSONObject(p_message.obj as String?))
                } catch (e: JSONException) {
                    LogPrinter.CF_line()
                    LogPrinter.CF_debug(resources.getString(R.string.log_json_exception))
                } catch (e :Exception) {
                    LogPrinter.CF_line()
                }

                HANDLERJOB_ERROR_REQUEST -> CommonFunction.CF_showCustomAlertDilaog(this, p_message.obj as String?, getResources().getString(R.string.btn_ok))
                else -> {
                }
            }
        }
    }

    /**
     * 카카오페이인증 인증요청 처리 함수
     */
    fun httpReq_kakaoAuth1() {
        // -- 프로그레스 다이얼로그 show
        CF_showProgressDialog()
        val builder: Uri.Builder = Uri.Builder()
        builder.appendQueryParameter("name", fragmentReqKakaopay!!.CF_getName()) // 이름
        builder.appendQueryParameter("encoded_mobile", fragmentReqKakaopay!!.CF_getEncodedMobile()) // 암호화됨 휴대폰 번호
        builder.appendQueryParameter("birthday", fragmentReqKakaopay!!.CF_getBirth()) // 생년월일(8자리)
        builder.appendQueryParameter("sex", fragmentReqKakaopay!!.CF_getSex()) // 성별 (남1,여0)
        builder.appendQueryParameter("service_code", "S320") // S320:카카오앱실행

        // 인증 요청정보 설정 : 인증 완료 시 정보 전달
        mAuthName = fragmentReqKakaopay!!.CF_getName()
        HttpConnections.sendPostData(
                EnvConfig.host_url + EnvConfig.URL_KAKAO_REG_AUTH1,
                builder.build().encodedQuery,
                handler,
                HANDLERJOB_REQUEST,
                HANDLERJOB_ERROR_REQUEST)
    }

    /**
     * 카카오페이인증 인증요청 결과 처리 함수
     * @param p_jsonObject  JSONObject
     */
    @Throws(JSONException::class)
    private fun httpRes_kakaoAuth1(p_jsonObject: JSONObject) {
        val jsonKey_errorCode = "errCode"
        val jsonKey_debugMsg  = "debugMsg"
        val jsonKey_flagOk    = "flagOk"
        val jsonKey_csno      = "s_csno" // 고객번호
        val jsonKey_result    = "result" // 인증요청 결과
        val jsonKey_txid      = "tx_id" // 카카오페이 거래ID

        if (p_jsonObject.has(jsonKey_errorCode)) {
            val tmp_errorCode: String = p_jsonObject.getString(jsonKey_errorCode)
            val tmp_debugMsg: String = p_jsonObject.getString(jsonKey_debugMsg)

            if (("" == tmp_errorCode)) {
                if (p_jsonObject.has(jsonKey_flagOk)) {
                    val tmp_flagOk: Boolean = p_jsonObject.getBoolean(jsonKey_flagOk)
                    // -- (전문성공)
                    if (tmp_flagOk) {
                        val sAuthCsno: String = p_jsonObject.getString(jsonKey_csno) // 인증요청 고객번호
                        val sResult: String = p_jsonObject.getString(jsonKey_result) // 인증요청 결과
                        val sTxid: String = p_jsonObject.getString(jsonKey_txid) // 인증요청 tx_id
                        //final Button btnAuth = findViewById(R.id.btnFill);

                        // -- (최종 정상)
                        if (("Y" == sResult)) {
                            // -- (S310) 항상 카카오앱 실행으로 사용안함
//                            if(false) {
//                                showCustomDialog(getResources().getString(R.string.dlg_verify_kakaopay_auth),
//                                        getResources().getString(R.string.btn_cancel),
//                                        getResources().getString(R.string.btn_kakaopay_verify),
//                                        btnAuth,
//                                        new DialogInterface.OnDismissListener() {
//                                            @Override
//                                            public void onDismiss(DialogInterface dialog) {
//                                                if (!((CustomDialog) dialog).CF_getCanceled()) {
//                                                    // -- 인증 확인 재요청
//                                                    //httpReq_kakaoAuth2();
//                                                } else if (CommonFunction.CF_checkAccessibilityTurnOn(getApplicationContext())) {
//                                                    // -- 취소
//                                                    clearAllFocus();
//                                                    btnAuth.requestFocus();
//                                                }
//                                                //textBtnCancel.setFocusableInTouchMode(false);
//                                            }
//                                        }
//                                );
//                            }
                            // -- (S320) 카카오페이 앱 실행
                            startIUPC95M90(sAuthCsno, sTxid)
                        } else {  // 중계서버 result = false 실패
                            // 에러코드 없지만 실패
                            val errMsg: String = getResources().getString(R.string.dlg_error_kakao_common) + "[" + tmp_errorCode + "] " + tmp_debugMsg
                            CommonFunction.CF_showCustomAlertDilaog(this, errMsg, getResources().getString(R.string.btn_ok))
                        }
                    } else {    // flagOK = false 실패
                        val errMsg: String = getResources().getString(R.string.dlg_error_kakao_common) + "[" + tmp_errorCode + "] " + tmp_debugMsg
                        CommonFunction.CF_showCustomAlertDilaog(this, errMsg, getResources().getString(R.string.btn_ok))
                    }
                } else { // 서버응답 flagOK 누락
                    CommonFunction.CF_showCustomAlertDilaog(this, getResources().getString(R.string.dlg_error_server_2), getResources().getString(R.string.btn_ok))
                }
            } else { // errCode 존재
                val errMsg: String = getResources().getString(R.string.dlg_error_kakao_common) + "[" + tmp_errorCode + "] " + tmp_debugMsg
                CommonFunction.CF_showCustomAlertDilaog(this, errMsg, getResources().getString(R.string.btn_ok))
            }
        } else { // 서버응답 errCode 누락
            CommonFunction.CF_showCustomAlertDilaog(this, getResources().getString(R.string.dlg_error_server_1), getResources().getString(R.string.btn_ok))
        }
    }
    //    /**
    //     * 카카오페이인증 검증요청 처리 함수
    //     */
    //    public void httpReq_kakaoAuth2(){
    //        // -- 프로그레스 다이얼로그 show
    //        CF_showProgressDialog();
    //
    //        Uri.Builder tmp_builder = new Uri.Builder();
    //        tmp_builder.appendQueryParameter("tx_id"     , sTxid);
    //        tmp_builder.appendQueryParameter("sCsno"     , sCsno);
    //
    //        HttpConnections.sendPostData(
    //                EnvConfig.host_url + EnvConfig.URL_KAKAO_REG_AUTH2,
    //                tmp_builder.build().getEncodedQuery(),
    //                handler,
    //                HANDLERJOB_VERIFY,
    //                HANDLERJOB_ERROR_VERIFY);
    //    }
    //    /**
    //     * 카카오페이인증 검증요청 결과 처리 함수
    //     * @param p_jsonObject      JSONObject
    //     */
    //    private void httpRes_kakaoAuth2(JSONObject p_jsonObject) throws JSONException {
    //        final String jsonKey_errorCode = "errCode";
    //        final String jsonKey_debugMsg  = "debugMsg";
    //        final String jsonKey_csno      = "csno";
    //        final String jsonKey_flagOk    = "flagOk";
    //        final String jsonKey_tempKey   = "tempKey";
    //
    //        if(p_jsonObject.has(jsonKey_errorCode)){
    //            String tmp_errorCode = p_jsonObject.getString(jsonKey_errorCode);
    //            String tmp_debugMsg = p_jsonObject.getString(jsonKey_debugMsg);
    //
    //            if("".equals(tmp_errorCode)){
    //                if(p_jsonObject.has(jsonKey_flagOk)){
    //                    final boolean tmp_flagOk = p_jsonObject.getBoolean(jsonKey_flagOk);
    //
    //                    // --<> (최종성공)
    //                    if(tmp_flagOk){
    //                        final String tmp_tempKey = p_jsonObject.getString(jsonKey_tempKey);
    //                        final String tmp_csno    = p_jsonObject.getString(jsonKey_csno);
    //
    //                        // -- 인증정보 저장(Prefernces)
    //                        SharedPreferencesFunc.setFlagLogin(getApplicationContext()      , true);                 // 로그인 처리
    //                        SharedPreferencesFunc.setLoginAuthDvsn(getApplicationContext()  , EnvConfig.AuthDvsn.KAKAOPAY);     // 인증방법 저장
    //                        SharedPreferencesFunc.setLoginCsno(getApplicationContext()      , tmp_csno);                        // 고객번호 저장
    //                        SharedPreferencesFunc.setLoginName(getApplicationContext()      , sAuthName);                       // 고객명 저장
    //
    //                        SharedPreferencesFunc.setLoginBirthday(getApplicationContext()  , sAuthBirthDate);                  // 생년월일 저장
    //                        SharedPreferencesFunc.setLoginSex(getApplicationContext()       , sAuthSex);                        // 성별 저장
    //                        SharedPreferencesFunc.setLoginMobile(getApplicationContext()    , sAuthEncodedMobileNo);            // 암호화된 휴대폰번호 저장
    //
    //                        // -- Web 요청 TempKey 저장 : 메인에서 Web 호출 시 넘겨달라함.
    //                        if(p_jsonObject.has(jsonKey_tempKey)){
    //                            SharedPreferencesFunc.setWebTempKey(getApplicationContext(), tmp_tempKey);
    //                        }
    //
    //                        // -- [단말DB] 사용자 정보 업데이트
    //                        CustomSQLiteFunction.setUser_Info(getApplicationContext(), sAuthName, tmp_csno, Integer.toString(EnvConfig.AuthDvsn.KAKAOPAY.ordinal()));
    //
    //                        // -- [유라클] 푸쉬 서비스&사용자 등록
    //                        CF_pushRegisterServiceAndUser(tmp_csno, sAuthName);
    //
    //                        // -- [서버DB] 푸쉬정보 업데이트
    //                        //CF_requestUpdatePushId(tmp_csno);
    //
    //                        showCustomDialog(getResources().getString(R.string.dlg_auth_sms), RESULT_OK);
    //                    }
    //                    // flagOK = false 실패
    //                    else{
    //                        String errMsg = getResources().getString(R.string.dlg_error_kakao_common) + "["+tmp_errorCode+"] "+tmp_debugMsg;
    //                        CommonFunction.CF_showCustomAlertDilaog(this, errMsg, getResources().getString(R.string.btn_ok));
    //                    }
    //                }
    //                else { // 서버응답 flagOK 누락
    //                    CommonFunction.CF_showCustomAlertDilaog(this, getResources().getString(R.string.dlg_error_server_2), getResources().getString(R.string.btn_ok));
    //                }
    //            }
    //            else if("E201".equals(tmp_errorCode)) {
    //                // 서명 대기중
    //                final Button btnAuth = findViewById(R.id.btnFill);
    //                showCustomDialog(getResources().getString(R.string.dlg_err_kakaopay_E201),
    //                        getResources().getString(R.string.btn_cancel),
    //                        getResources().getString(R.string.btn_kakaopay_verify),
    //                        btnAuth,
    //                        new DialogInterface.OnDismissListener() {
    //                            @Override
    //                            public void onDismiss(DialogInterface dialog) {
    //                                if(!((CustomDialog) dialog).CF_getCanceled()){
    //                                    httpReq_kakaoAuth2();
    //                                }
    //                                else if(CommonFunction.CF_checkAccessibilityTurnOn(getApplicationContext())){
    //                                    clearAllFocus();
    //                                    btnAuth.requestFocus();
    //                                }
    //                            }
    //                        }
    //                );
    //            } else { // errCode 존재
    //                String errMsg = getResources().getString(R.string.dlg_error_kakao_common) + "["+tmp_errorCode+"] "+tmp_debugMsg;
    //                CommonFunction.CF_showCustomAlertDilaog(this, errMsg, getResources().getString(R.string.btn_ok));
    //            }
    //        } else {   // 서버응답 errCode 누락
    //            CommonFunction.CF_showCustomAlertDilaog(this, getResources().getString(R.string.dlg_error_server_1), getResources().getString(R.string.btn_ok));
    //        }
    //    }
    /**
     * 카카오앱(카카오페이인증) 실행
     * @param p_tx_id   카카오 거래ID
     */
    private fun startIUPC95M90(p_authCsno: String, p_tx_id: String) {
        val intent = Intent(this, IUPC95M90::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
        intent.putExtra(EnvConstant.KEY_INTENT_AUTH_MODE, AuthMode.LOGIN_APP) // 인증모드
        intent.putExtra(EnvConstant.KEY_INTENT_AUTH_KAKAO_TX_ID, p_tx_id) // 카카오톡 거래번호
        intent.putExtra(EnvConstant.KEY_INTENT_AUTH_CSNO, p_authCsno) // 인증자 CSNO
        intent.putExtra(EnvConstant.KEY_INTENT_AUTH_NAME, mAuthName) // 인증자 이름
        startActivityForResult(intent, EnvConfig.REQUESTCODE_ACTIVITY_IUPC95M90)
    }
}