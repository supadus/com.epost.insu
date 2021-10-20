package com.epost.insu.activity.auth

import android.annotation.SuppressLint
import android.content.DialogInterface
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
import com.epost.insu.EnvConfig.AuthDvsn
import com.epost.insu.EnvConstant
import com.epost.insu.R
import com.epost.insu.adapter.CustomPagerAdapter
import com.epost.insu.common.CommonFunction
import com.epost.insu.common.CustomViewPager
import com.epost.insu.common.LogPrinter
import com.epost.insu.dialog.CustomDialog
import com.epost.insu.fragment.IUPC90M00_F
import com.epost.insu.fragment.IUPC91M00_F
import com.epost.insu.network.HttpConnections
import com.epost.insu.network.WeakReferenceHandler
import com.epost.insu.network.WeakReferenceHandler.ObjectHandlerMessage
import org.json.JSONException
import org.json.JSONObject

/**
 * 인증 > 휴대폰인증 1회로그인/본인확인
 * @since     :
 * @version   : 1.7
 * @author    : LSH
 * @see
 * <pre>
 *  - 1회 로그인 : 청구시 휴대폰인증으로 로그인할때 로그인 처리는 안되나 임시로 고객번호/이름을 저장한다
 *  - 본인확인   : 휴대폰인증 청구 마지막단계에서 휴대폰 본인인증을 한번 더 호출한다.
 * ======================================================================
 * 0.0.0    LSH_20170818    최초 등록
 * 0.0.0    YJH_20181109    휴대폰인증청구 신규
 * 0.0.0    NJM_20190402    휴대폰번호 단말정보값으로 고정하도록 변경(추후 변경가능하도록 기존 소스 유지) : 요청URL 변경하고, 휴대폰번호 자체암호화(AES256)함
 * 0.0.0    NJM_20190405    주민번호 뒷자리 마스킹 처리
 * 0.0.0    NJM_20190508    실명번호 입력에서 생년월일(8자) 및 성별 입력으로 변경
 * 0.0.0    NJM_20200122    공통 인증유형/청구유형 추가에 따른 로직수정
 * 1.5.5    NJM_20210520    [subUrl 공통파일로 변경]
 * 1.5.8    NJM_20210630    [로그인처리 공통화] CF_setLogin() 공통 로그인 처리 함수
 * =======================================================================
 * copyright : (주)프로니스정보기술 | http://www.phronis.co.kr
 * </pre>
 */
class IUPC90M00_P : Activity_Auth(), ObjectHandlerMessage {

    private val HANDLERJOB_MOBILE_AUTH1: Int = 0 // SMS 인증번호 요청 성공
    private val HANDLERJOB_MOBILE_AUTH1_ERR: Int = 1
    private val HANDLERJOB_MOBILE_AUTH2: Int = 2 // SMS 인증번호 확인 성공
    private val HANDLERJOB_MOBILE_AUTH2_ERR: Int = 3

    private val _ERR_001: String = "ERRIUPC90M00001" // 요청데이터 누락
    private val _ERR_002: String = "ERRIUPC90M00002" // 인증번호 요청 실패
    private val _ERR_003: String = "ERRIUPC90M00003" // 인증번호 검증 실패
    private val _ERR_004: String = "ERRIUPC90M00004" // 실명번호 오류
    private val _ERR_005: String = "ERRIUPC90M00005" // 고객정보조회 실패
    private val _ERR_006: String = "ERRIUPC90M00006" // 본인확인 실패

    private var fragmentReqSms: IUPC90M00_F? = null // 휴대폰인증번호 요청 Fragment
    private var fragmentInputCode: IUPC91M00_F? = null // 휴대폰인증번호 확인 Fragment

    private lateinit var pager: CustomViewPager
    private var sCsno: String = "" // 고객번호
    private var sName: String? = null // 고객명
    private var sMmfmCode: String? = null // 회원사 코드 (휴대폰인증요청시 생성)
    private var sPrauTxSqnu: String? = null // 본인인증거래 일련번호 (휴대폰인증요청시 생성)

    private var authMode: AuthMode? = null // 요청모드("LOGIN_APP", "SIGN_APP",..)

    override fun setInit() {
        setContentView(R.layout.iupc90m00_p)
        sCsno = ""
        sName = ""
        sMmfmCode = ""
        sPrauTxSqnu = ""
        handler = WeakReferenceHandler(this)
    }

    override fun setUIControl() {
        // -- 타이틀바 세팅
        setTitleBarUI()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // -- restore 데이터
        if (savedInstanceState != null) {
            sCsno = savedInstanceState.getString("sCsno").toString()
            sName = savedInstanceState.getString("sName")
            sMmfmCode = savedInstanceState.getString("sMmfmCode")
            sPrauTxSqnu = savedInstanceState.getString("sPrauTxSqnu")
        }

        // -- Intent 데이터 세팅 함수
        setIntentData()
        // -- Fragment 세팅
        setFragments(savedInstanceState)
        val adapter = CustomPagerAdapter(getSupportFragmentManager(), arrayOf<Fragment?>(fragmentReqSms, fragmentInputCode))
        pager = findViewById(R.id.viewPager)
        pager.CF_setPagingEnabled(false)
        pager.setAdapter(adapter)
        pager.setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_NO)
    }

    override fun onBackPressed() {
        if (pager!!.getCurrentItem() == 1) {
            val tmp_dlg = CustomDialog(this)
            tmp_dlg.show()
            tmp_dlg.CF_setTextContent(getResources().getString(R.string.dlg_cancel_mobile_auth))
            tmp_dlg.CF_setDoubleButtonText(getResources().getString(R.string.btn_no), getResources().getString(R.string.btn_yes))
            tmp_dlg.setOnDismissListener(object : DialogInterface.OnDismissListener {
                override fun onDismiss(dialog: DialogInterface) {
                    if (!(dialog as CustomDialog).CF_getCanceled()) {
                        fragmentInputCode!!.CF_stopTimer()
                        pager!!.setCurrentItem(0)
                    }
                }
            })
        } else {
            if (fragmentReqSms != null && fragmentReqSms!!.isAdded()) {
                val tmp_dlg = CustomDialog(this)
                tmp_dlg.show()
                tmp_dlg.CF_setTextContent(getResources().getString(R.string.dlg_cancel_mobile_auth))
                tmp_dlg.CF_setDoubleButtonText(getResources().getString(R.string.btn_no), getResources().getString(R.string.btn_yes))
                tmp_dlg.setOnDismissListener(object : DialogInterface.OnDismissListener {
                    override fun onDismiss(dialog: DialogInterface) {
                        if (!(dialog as CustomDialog).CF_getCanceled()) {
                            setResult(RESULT_CANCELED)
                            finish()
                        }
                    }
                })
            }
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString("sCsno", sCsno)
        outState.putString("sName", sName)
        outState.putString("sMmfmCode", sMmfmCode)
        outState.putString("sPrauTxSqnu", sPrauTxSqnu)
        supportFragmentManager.putFragment(outState, IUPC90M00_F::class.java.name, (fragmentReqSms)!!)
        supportFragmentManager.putFragment(outState, IUPC91M00_F::class.java.name, (fragmentInputCode)!!)
    }

    /**
     * Intent 데이터 세팅 함수
     */
    private fun setIntentData() {
        if (intent != null) {
            // -- 이름 : 청구 최종 인증시에는 이름을 받음
            if (intent.hasExtra(EnvConstant.KEY_INTENT_AUTH_NAME)) {
                sName = intent.extras!!.getString(EnvConstant.KEY_INTENT_AUTH_NAME)
            }
            // -- 인증모드(SIGN_APP, LOGIN_APP..)
            if (intent.hasExtra(EnvConstant.KEY_INTENT_AUTH_MODE)) {
                authMode = intent.getSerializableExtra(EnvConstant.KEY_INTENT_AUTH_MODE) as AuthMode?
            }
        }
    }

    /**
     * Fragment 세팅
     */
    private fun setFragments(p_bundle: Bundle?) {
        if (p_bundle != null) {
            fragmentReqSms = supportFragmentManager.getFragment(p_bundle, IUPC90M00_F::class.java.name) as IUPC90M00_F?
            fragmentInputCode = supportFragmentManager.getFragment(p_bundle, IUPC91M00_F::class.java.name) as IUPC91M00_F?
        } else {
            fragmentReqSms = Fragment.instantiate(this, IUPC90M00_F::class.java.getName()) as IUPC90M00_F?
            fragmentInputCode = Fragment.instantiate(this, IUPC91M00_F::class.java.getName()) as IUPC91M00_F?
        }
    }

    /**
     * 타이틀바 레이아웃 세팅
     */
    private fun setTitleBarUI() {
        // 타이틀 세팅
        val tmp_title: TextView = findViewById(R.id.title_bar_textTitle)
        tmp_title.text = getResources().getString(R.string.title_mobile_auth)

        // left 버튼 세팅
        val tmp_btnLeft: ImageButton = findViewById(R.id.title_bar_imgBtnLeft)
        tmp_btnLeft.visibility = View.VISIBLE
        tmp_btnLeft.setOnClickListener(object : View.OnClickListener {
            override fun onClick(v: View) {
                if (pager.currentItem == 1) {
                    showCustomDialog(getResources().getString(R.string.dlg_cancel_mobile_auth),
                            getResources().getString(R.string.btn_no),
                            getResources().getString(R.string.btn_yes),
                            tmp_btnLeft,
                            object : DialogInterface.OnDismissListener {
                                override fun onDismiss(dialog: DialogInterface) {
                                    if (!(dialog as CustomDialog).CF_getCanceled()) {
                                        fragmentInputCode!!.CF_stopTimer()
                                        pager!!.currentItem = 0
                                    } else if (CommonFunction.CF_checkAccessibilityTurnOn(getApplicationContext())) {
                                        clearAllFocus()
                                        tmp_btnLeft.requestFocus()
                                    }
                                    tmp_btnLeft.isFocusableInTouchMode = false
                                }
                            })
                } else {
                    showCustomDialog(getResources().getString(R.string.dlg_cancel_mobile_auth),
                            getResources().getString(R.string.btn_no),
                            getResources().getString(R.string.btn_yes),
                            tmp_btnLeft,
                            object : DialogInterface.OnDismissListener {
                                override fun onDismiss(dialog: DialogInterface) {
                                    if (!(dialog as CustomDialog).CF_getCanceled()) {
                                        setResult(RESULT_CANCELED)
                                        finish()
                                    } else if (CommonFunction.CF_checkAccessibilityTurnOn(getApplicationContext())) {
                                        clearAllFocus()
                                        tmp_btnLeft.requestFocus()
                                    }
                                    tmp_btnLeft.isFocusableInTouchMode = false
                                }
                            })
                }
            }
        })
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
        customDialog.CF_setTextContent(getResources().getString(R.string.dlg_cancel_request))
        customDialog.CF_setBackKeyMode(CustomDialog.BackMode.CANCELED)
        customDialog.CF_setSingleButtonText(getResources().getString(R.string.btn_ok))
        customDialog.setOnDismissListener(object : DialogInterface.OnDismissListener {
            override fun onDismiss(dialog: DialogInterface) {
                //fragmentInputCode.CF_stopTimer();
                //pager.setCurrentItem(0);
                clearAllFocus()
                setResult(RESULT_CANCELED)
                finish()
            }
        })
    }

    /**
     * 이름 반환
     * 이름 세팅 : 청구 최종 본인인증시 계좌 예금주명으로 세팅
     * @return  String
     */
    fun CF_getName(): String? {
        return sName
    }

    // #############################################################################################
    // Http 관련 함수
    // #############################################################################################
    override fun handleMessage(p_message: Message) {
        if (!isDestroyed) {
            CF_dismissProgressDialog()
            when (p_message.what) {
                HANDLERJOB_MOBILE_AUTH1 -> try {
                    httpRes_mobile_auth1(JSONObject(p_message.obj as String?))
                } catch (e: JSONException) {
                    LogPrinter.CF_line()
                    LogPrinter.CF_debug(getResources().getString(R.string.log_json_exception))
                }
                HANDLERJOB_MOBILE_AUTH2 -> try {
                    httpRes_mobileAuth2(JSONObject(p_message.obj as String?))
                } catch (e: JSONException) {
                    LogPrinter.CF_line()
                    LogPrinter.CF_debug(getResources().getString(R.string.log_json_exception))
                }
                HANDLERJOB_MOBILE_AUTH1_ERR, HANDLERJOB_MOBILE_AUTH2_ERR -> CommonFunction.CF_showCustomAlertDilaog(this, p_message.obj as String?, getResources().getString(R.string.btn_ok))
                else -> {
                }
            }
        }
    }

    /**
     * 휴대폰 인증 요청 함수
     * @since 20190508 실명인증 -> 생년월일 성별값으로 변경됨
     */
    fun httpReq_mobile_auth1() {
        // -- 인증번호 요청 수신 데이터 초기화
        sMmfmCode = ""
        sPrauTxSqnu = ""

        // -- 프로그레스 다이얼로그 show
        CF_showProgressDialog()
        val builder: Uri.Builder = Uri.Builder()
        builder.appendQueryParameter("name"          , fragmentReqSms!!.CF_getName()) // 이름
        builder.appendQueryParameter("mbtcCode"      , fragmentReqSms!!.CF_getMBTCode()) // 통신사 코드
        builder.appendQueryParameter("encoded_mobile", fragmentReqSms!!.CF_getEncodedMobile2()) // 암호화됨 휴대폰 번호 //2019-04-02 변경
        builder.appendQueryParameter("birthday"      , fragmentReqSms!!.CF_getBirth()) //생년월일 (8자리)
        builder.appendQueryParameter("sex"           , fragmentReqSms!!.CF_getSex()) //성별 (남1,여0)
        HttpConnections.sendPostData(
                EnvConfig.host_url + EnvConfig.URL_MOBILE_AUTH1,
                builder.build().encodedQuery,
                handler,
                HANDLERJOB_MOBILE_AUTH1,
                HANDLERJOB_MOBILE_AUTH1_ERR)
    }

    /**
     * 휴대폰 인증번호 요청 결과 처리 함수
     * @param p_jsonObject  JSONObject
     */
    @Throws(JSONException::class)
    private fun httpRes_mobile_auth1(p_jsonObject: JSONObject) {
        val jsonKey_errorCode = "errCode"
        val jsonKey_flagOk = "flagOk"
        //val jsonKey_data = "data"
        val jsonKey_csno = "s_csno" // 고객번호
        val jsonKey_sMmfmCode = "sMmfmCode" // 회원사 코드
        val jsonKey_sPrauTxSqnu = "sPrauTxSqnu" // 본인인증거래 일련번호
        val tmp_errorCode: String

        if (p_jsonObject.has(jsonKey_errorCode)) {
            tmp_errorCode = p_jsonObject.getString(jsonKey_errorCode)

            //TODO 에러메시지 정비
            if ((_ERR_001 == tmp_errorCode)) {
                showCustomDialogAlert(getResources().getString(R.string.dlg_error_erriupc90m00001))
            } else if ((_ERR_002 == tmp_errorCode)) {
                showCustomDialogAlert(getResources().getString(R.string.dlg_error_erriupc90m00002))
            } else if ((_ERR_004 == tmp_errorCode)) {
                showCustomDialogAlert(getResources().getString(R.string.dlg_error_erriupc90m00001))
            } else if ((_ERR_005 == tmp_errorCode)) {
                showCustomDialogAlert(getResources().getString(R.string.dlg_error_erriupc90m00001))
            } else if ((_ERR_006 == tmp_errorCode)) {
                showCustomDialogAlert(getResources().getString(R.string.dlg_error_erriupc90m00006))
            } else if (p_jsonObject.has(jsonKey_flagOk)) {
                val tmp_flagOk: Boolean = p_jsonObject.getBoolean(jsonKey_flagOk)
                if (tmp_flagOk) {
                    sCsno = p_jsonObject.getString(jsonKey_csno) // 고객번호
                    sMmfmCode = p_jsonObject.getString(jsonKey_sMmfmCode) // 회원사 코드
                    sPrauTxSqnu = p_jsonObject.getString(jsonKey_sPrauTxSqnu) // 본인인증거래 일련번호

                    // -- 타이머 실행
                    fragmentInputCode!!.CF_startTimer()
                    pager.currentItem = 1
                    showCustomDialog(getResources().getString(R.string.dlg_send_sms_code), fragmentInputCode!!.CF_getInputBox()!!)
                }
            } else {
                showCustomDialogAlert(getResources().getString(R.string.dlg_error_server_2))
            }
        } else {
            showCustomDialogAlert(getResources().getString(R.string.dlg_error_server_1))
        }
    }

    /**
     * 휴대폰 인증번호 확인 함수
     */
    fun httpReq_mobileAuth2(p_smsCode: String?) {
        // -- 프로그레스 다이얼로그 show
        CF_showProgressDialog()
        val builder: Uri.Builder = Uri.Builder()
        builder.appendQueryParameter("authCode"   , p_smsCode)
        builder.appendQueryParameter("sCsno"      , sCsno)
        builder.appendQueryParameter("sMmfmCode"  , sMmfmCode)
        builder.appendQueryParameter("sPrauTxSqnu", sPrauTxSqnu)
        HttpConnections.sendPostData(
                EnvConfig.host_url + EnvConfig.URL_MOBILE_AUTH2,
                builder.build().encodedQuery,
                handler,
                HANDLERJOB_MOBILE_AUTH2,
                HANDLERJOB_MOBILE_AUTH2_ERR)
    }

    /**
     * 휴대폰 인증번호 확인 결과 처리 함수
     * @param p_jsonObject      JSONObject
     */
    @Throws(JSONException::class)
    private fun httpRes_mobileAuth2(p_jsonObject: JSONObject) {
        val jsonKey_errorCode = "errCode"
        val jsonKey_flagOk = "flagOk"

        val tmp_errorCode: String

        if (p_jsonObject.has(jsonKey_errorCode)) {
            tmp_errorCode = p_jsonObject.getString(jsonKey_errorCode)
            if ((_ERR_001 == tmp_errorCode)) {
                CommonFunction.CF_showCustomAlertDilaog(this, getResources().getString(R.string.dlg_error_erriupc71m00001), getResources().getString(R.string.btn_ok))
            } else if ((_ERR_003 == tmp_errorCode)) {
                CommonFunction.CF_showCustomAlertDilaog(this, getResources().getString(R.string.dlg_error_erriupc71m00001), getResources().getString(R.string.btn_ok))
            } else if (p_jsonObject.has(jsonKey_flagOk)) {
                val tmp_flagOk: Boolean = p_jsonObject.getBoolean(jsonKey_flagOk)

                // -- (최종성공)
                if (tmp_flagOk) {
                    // -- 타이머 정지
                    if (fragmentInputCode != null && fragmentInputCode!!.isAdded()) {
                        fragmentInputCode!!.CF_stopTimer()
                    }

                    // -----------------------------------------------------------------------------
                    // -- 최종 로그인 처리
                    // -----------------------------------------------------------------------------
                    if(AuthMode.isLogin(authMode)) {
                        CF_setLogin(false, AuthDvsn.MOBILE, sCsno, fragmentReqSms!!.CF_getName(), "" )
                    }

                    // -- 다이얼로그 팝업
                    //showCustomDialog(getResources().getString(R.string.dlg_auth_sms), RESULT_OK)
                    CommonFunction.CF_showCustomAlertDilaog(this, resources.getString(R.string.dlg_auth_sms)) {
                        setResult(RESULT_OK)
                        finish()
                    }
                }
                else {
                    showCustomDialog(getResources().getString(R.string.dlg_different_sms), fragmentInputCode!!.CF_getInputBox()!!)
                }
            } else {
                showCustomDialogAlert(getResources().getString(R.string.dlg_error_server_2))
            }
        } else {
            showCustomDialogAlert(getResources().getString(R.string.dlg_error_server_1))
        }
    }
}