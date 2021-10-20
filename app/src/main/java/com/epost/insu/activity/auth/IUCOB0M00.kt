package com.epost.insu.activity.auth

import android.annotation.SuppressLint
import android.app.KeyguardManager
import android.content.Intent
import android.content.res.Resources
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Message
import android.view.View
import android.widget.*

import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.epost.insu.EnvConfig
import com.epost.insu.EnvConfig.ReqDvsn
import com.epost.insu.EnvConstant
import com.epost.insu.R
import com.epost.insu.SharedPreferencesFunc
import com.epost.insu.common.CommonFunction
import com.epost.insu.common.CustomSQLiteFunction
import com.epost.insu.common.LogPrinter
import com.epost.insu.common.UiUtil
import com.epost.insu.control.OnOffControl
import com.epost.insu.event.OnChangedCheckedStateEventListener
import com.epost.insu.module.FingerModule
import com.epost.insu.network.HttpConnections
import com.epost.insu.network.WeakReferenceHandler
import com.epost.insu.network.WeakReferenceHandler.ObjectHandlerMessage
import org.json.JSONException
import org.json.JSONObject
import java.lang.ClassCastException
import java.lang.NullPointerException

/**
 * 공통 > 로그인방법선택 > 로그인방법선택
 * @since     :
 * @version   : 2.2
 * @author    : LSH
 * @see
 * <pre>
 *  로그인 방법 선택 화면(공동인증, 지문인증, PIN인증 택 1)
 *  로그인(지문,공동인증 등) 후 항상 현재 Activity로 돌아와서 이후 로직 처리해야함
 * ======================================================================
 * 0.0.0    LSH_20170818    최초 등록
 * 0.0.0    YJH_20181109    휴대폰인증청구 신규
 * 1.3.5    NJM_20200122    공통 인증유형/청구유형 추가에 따른 로직수정
 * 1.3.5    NJM_20200123    자녀보험금청구에 휴대폰인증 추가 (주석해제 필요)
 * 1.3.5    NJM_20200130    지급청구 다이얼로그 로직 변경
 * 1.4.4    YJH_20201207    카카오페이인증 추가
 * 1.5.2    NJM_20210319    [임시개발 로그인 기능 추가]
 * 1.5.3    NJM_20210408    임시개발 로그인 파라미터 수정
 * 1.5.4    NJM_20210429    [카카오페이앱 인증 S320] 인증정보 저장방식 일부 변경
 * 1.5.4    NJM_20210506    [IUFC34M00 임시고객번호 삭제]
 * 1.5.8    NJM_20210630    [금융인증서 도입]
 * 1.5.9    NJM_20210701    [PIN등록 오류 수정] 최초설치시 PIN등록시 csno 누락 오류 수정
 * 1.6.2    NJM_20210729    [자동로그인 신규] 1차본 (기능 반영만)
 * =======================================================================
 * copyright : (주)프로니스정보기술 | http://www.phronis.co.kr
 * </pre>
 */
class IUCOB0M00 : Activity_Auth(), View.OnClickListener, ObjectHandlerMessage {
    private val HANDLERJOB_AUTH_REQ = 0
    private val HANDLERJOB_ERROR_AUTH_REQ = 1

    private var viewNeedAccessibleFocus : View? = null // onPostResume에서 접근성 포커싱이 필요한 View
    private var mReqDvsn : ReqDvsn? = null // 요청구분(본인청구,자녀청구 등)
    private var onOffControl: OnOffControl? = null // 푸시알림 수신 여부 On/Off 컨트롤

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        LogPrinter.CF_debug("!----------------------------------------------------------")
        LogPrinter.CF_debug("!-- IUCOB0M00.onActivityResult()")
        LogPrinter.CF_debug("!----------------------------------------------------------")
        LogPrinter.CF_debug("!---- requestCode : [$requestCode] / resultCode : [$resultCode]")

        if (resultCode == RESULT_OK) {
            SharedPreferencesFunc.setLastLoginMethod(this@IUCOB0M00,clickLoginId)
            LogPrinter.CF_debug("!---- login debug requestCode: $requestCode")
            //로그인성공
            LocalBroadcastManager.getInstance(this).sendBroadcast(Intent().setAction(EnvConfig.BROADCAST_APPLOGINSUCCESS))
            when (requestCode) {
                EnvConfig.REQUESTCODE_ACTIVITY_IUPC90M00 -> {
                    setResult(RESULT_OK)
                            finish()
                }
                EnvConfig.REQUESTCODE_ACTIVITY_IUFC00M00,
                EnvConfig.REQUESTCODE_ACTIVITY_IUPC95M00,
                EnvConfig.REQUESTCODE_ACTIVITY_IUPC10M00,
                EnvConfig.REQUESTCODE_ACTIVITY_IUPC20M00,
                EnvConfig.REQUESTCODE_ACTIVITY_IUCOC0M00 -> {
                    onOffControl?.let {
                        SharedPreferencesFunc.setFlagAutoLogin(applicationContext, it.CF_getFlagIsOn())
                    }
                    setResult(RESULT_OK)
                    finish()
                }
                else -> {
                    LogPrinter.CF_debug("!---- p_requestCode값 없음 : $requestCode")
                }
            }
        } else {
            LogPrinter.CF_debug("!---- p_resultCode실패 : $requestCode")
        }
    }







    var clickLoginId = -1
    @SuppressLint("NonConstantResourceId")
    override fun onClick(p_view: View) {
        when (p_view.id) {
            R.id.btnLoginMobile -> startIUPC90M00_P()
            R.id.btnLoginPin -> startIUFC00M00_fido(EnvConfig.AuthDvsn.PIN, p_view)
            R.id.btnLoginFinger -> startIUFC00M00_fido(EnvConfig.AuthDvsn.PINGER, p_view)
            R.id.btnLoginPattern -> startIUFC00M00_fido(EnvConfig.AuthDvsn.PATTERN, p_view)
            R.id.btnLoginKakaopay -> startIUPC95M00_kakao()
            R.id.btnLoginCertificate -> startIUCOC0M00()
            R.id.btnLoginFinCert -> startIUPC10M00()
            R.id.btnLoginPassCert -> startIUPC20M00()
            R.id.activity_login_btnManageS -> startIUPC30M00()
            else -> {
            }
        }
        clickLoginId = p_view.id
    }



    override fun setInit() {
        setContentView(R.layout.iucob0m00)

        // -- 초기화
        mReqDvsn = null
        this.handler = WeakReferenceHandler(this)
        handler = WeakReferenceHandler(this)
    }

    override fun setUIControl() {
        // -- 타이틀바 세팅
        setTitleBarUI()
        // -- 버튼 세팅
        setBtnUI()

        // -- 로그유무에 따른 개발 임시로그인 온/오프
        if (EnvConfig.mFlagShowLog) {
            val devLogin = findViewById<View>(R.id.lin_dev)
            devLogin.visibility = View.VISIBLE
            setTempPwLayoutUI()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        LogPrinter.CF_debug("!----------------------------------------------------------")
        LogPrinter.CF_debug("!-- IUCOB0M00.onCreate()")
        LogPrinter.CF_debug("!----------------------------------------------------------")

        // -- Intent 데이터 세팅 함수
        setIntentData()

        var runAutoLogin=false
        // TODO : 자동로그인 테스트 : API 23이상 && 잠금화면ON
        LogPrinter.CF_info("---- 단말기 API 버전 : " + Build.VERSION.SDK_INT)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && false ) { // TODO 자동로그인 오픈시 false 삭제
            // -- 잠금상태 확인
            val keyguardManager = getSystemService(KEYGUARD_SERVICE) as KeyguardManager
            LogPrinter.CF_info("---- 화면잠금 상태 : " + keyguardManager.isDeviceSecure)


           // -- 자동로그인 UI : 잠금화면 설정되어있을경우에만 보여진다.
           if (keyguardManager.isDeviceSecure) {
                // -- 자동로그인 UI : 잠금화면 설정되어있을경우에만 보여진다.
                setAutoLoginUI()
                val isAutoLogin = SharedPreferencesFunc.getFlagAutoLogin(applicationContext)
                LogPrinter.CF_info("---- 자동로그인 여부 : $isAutoLogin")
                if(isAutoLogin) {
                    runAutoLogin=true
                    httpReq_tempAuth(CustomSQLiteFunction.getLastLoginCsno(applicationContext));
                }

            } else {
                // 화면 잠금 해제 상태
            }
        }

        //마지막 로그인 정보 불러옴
        if(!runAutoLogin) {
            val lastLoginButtonId = SharedPreferencesFunc.getLastLoginMethod(this@IUCOB0M00)
            if (lastLoginButtonId != -1) {
                try {
                    val button = findViewById<LinearLayout>(lastLoginButtonId)
                    button.callOnClick()
                } catch (e: ClassCastException) {
                    LogPrinter.CF_info("LastLoginMethod ClassCastException")
                } catch (e: NullPointerException) {
                    LogPrinter.CF_info("LastLoginMethod NullPointerException")
                }
            }
        }



    }

    override fun onResume() {
        super.onResume()

        // TODO : 자녀보험금 휴대폰인증 추가시 주석해제
        // NJM_20200123 자녀보험금청구에 휴대폰인증 추가 ( 주석해제 필요)
        // 휴대폰인증 로그인 버튼 VIew 활성화
        if (mReqDvsn == ReqDvsn.SELF /*|| mReqDvsn == EnvConfig.ReqDvsn.CHILDE*/ || mReqDvsn == ReqDvsn.INQUERY) {
            showPhoneAuthBtnView()
        }
    }

    override fun onPostResume() {
        super.onPostResume()
        if (viewNeedAccessibleFocus != null) {
            clearAllFocus()
            viewNeedAccessibleFocus!!.isFocusableInTouchMode = true
            viewNeedAccessibleFocus!!.requestFocus()
            viewNeedAccessibleFocus!!.isFocusableInTouchMode = false
            viewNeedAccessibleFocus = null
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putSerializable("mReqDvsn", mReqDvsn)
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        if (savedInstanceState.containsKey("mReqDvsn")) {
            mReqDvsn = savedInstanceState.getSerializable("mReqDvsn") as ReqDvsn?
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        LogPrinter.CF_debug("!----------------------------------------------------------")
        LogPrinter.CF_debug("!-- IUCOB0M00.onDestroy()")
        LogPrinter.CF_debug("!----------------------------------------------------------")

        // -----------------------------------------------------------------------------------------
        //  Handler 메시지 및 Callback 삭제
        // -----------------------------------------------------------------------------------------
        if (handler != null) {
            LogPrinter.CF_debug("!---- handler 삭제")
            handler?.removeCallbacksAndMessages(null)
            handler = null
        }
    }

    /**
     * Intent 데이터 세팅 함수
     */
    private fun setIntentData() {
        if (intent != null) {
            // 청구구분
            if (intent.hasExtra("reqDvsn")) {
                mReqDvsn = intent.getSerializableExtra("reqDvsn") as ReqDvsn
            }
        }
    }

    /**
     * 타이틀바 UI 세팅 함수
     */
    private fun setTitleBarUI() {
        // 타이틀 세팅
        val txtTitle = findViewById<TextView>(R.id.title_bar_textTitle)
        txtTitle.text = resources.getString(R.string.title_login)


        val title_main = findViewById<TextView>(R.id.title_main)
        UiUtil.setTitleColor(title_main,getString(R.string.label_iucob0m00_title),6,11)
        // left 버튼 세팅
        val btnLeft = findViewById<ImageButton>(R.id.title_bar_imgBtnLeft)
        btnLeft.visibility = View.VISIBLE
        btnLeft.setOnClickListener { finish() }
    }

    /**
     * 로그인 버튼 UI 세팅 함수
     */
    private fun setBtnUI() {
        // -- 공동인증 로그인 버튼 VIew
        val btnLoginCert = findViewById<View>(R.id.btnLoginCertificate)
        (btnLoginCert.findViewById<View>(R.id.imageView) as ImageView).setImageResource(R.drawable.ic_certificate_for_login)
        (btnLoginCert.findViewById<View>(R.id.textView) as TextView).text = resources.getString(R.string.btn_login_certificate)
        btnLoginCert.findViewById<View>(R.id.textView).contentDescription = resources.getString(R.string.btn_login_certificate) + " 버튼"

        // -- 금융인증 로그인 버튼 VIew
        val btnLoginFinCert = findViewById<View>(R.id.btnLoginFinCert)
        (btnLoginFinCert.findViewById<View>(R.id.imageView) as ImageView).setImageResource(R.drawable.ic_fincertificate_for_login)
        (btnLoginFinCert.findViewById<View>(R.id.textView) as TextView).text = resources.getString(R.string.btn_login_fincert)
        btnLoginFinCert.findViewById<View>(R.id.textView).contentDescription = resources.getString(R.string.btn_login_fincert) + " 버튼"

        // -- PASS인증 로그인 버튼 VIew
        val btnLoginPassCert = findViewById<View>(R.id.btnLoginPassCert)
        (btnLoginPassCert.findViewById<View>(R.id.imageView) as ImageView).setImageResource(R.drawable.ic_pass_cert_for_login)
        (btnLoginPassCert.findViewById<View>(R.id.textView) as TextView).text = resources.getString(R.string.btn_login_passcert)
        btnLoginPassCert.findViewById<View>(R.id.textView).contentDescription = resources.getString(R.string.btn_login_passcert) + " 버튼"

        // -- 지문인증 로그인 버튼 View
        val btnLoginFinger = findViewById<View>(R.id.btnLoginFinger)
        (btnLoginFinger.findViewById<View>(R.id.imageView) as ImageView).setImageResource(R.drawable.ic_finger)
        (btnLoginFinger.findViewById<View>(R.id.textView) as TextView).text = resources.getString(R.string.btn_login_finger)
        btnLoginFinger.findViewById<View>(R.id.textView).contentDescription = resources.getString(R.string.btn_login_finger) + " 버튼"

        // 해당 단말기의 지문인증 하드웨어 지원 여부 확인
        // 지문인식 관련 모듈
        val fingerModule = FingerModule(this)
        if (!fingerModule.CF_hasFingerSensor()) {
            btnLoginFinger.isEnabled = false
            val tmp_textSub = btnLoginFinger.findViewById<TextView>(R.id.textViewSub)
            tmp_textSub.text = resources.getString(R.string.label_no_sensor_finger)
            tmp_textSub.visibility = View.VISIBLE
            tmp_textSub.isSelected = true
        }

        // -- 패턴인증 로그인 버튼 View
        val btnLoginPattern = findViewById<View>(R.id.btnLoginPattern)
        (btnLoginPattern.findViewById<View>(R.id.imageView) as ImageView).setImageResource(R.drawable.ic_pattern)
        (btnLoginPattern.findViewById<View>(R.id.textView) as TextView).text = resources.getString(R.string.btn_login_pattern)
        btnLoginPattern.findViewById<View>(R.id.textView).contentDescription = resources.getString(R.string.btn_login_pattern) + " 버튼"
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            btnLoginPattern.isEnabled = false
            val tmp_textSub = btnLoginPattern.findViewById<TextView>(R.id.textViewSub)
            tmp_textSub.text = resources.getString(R.string.label_no_sensor_pattern)
            tmp_textSub.visibility = View.VISIBLE
            tmp_textSub.isSelected = true
        }

        // -- 핀 번호 로그인 버튼 View
        val btnLoginPIN = findViewById<View>(R.id.btnLoginPin)
        (btnLoginPIN.findViewById<View>(R.id.imageView) as ImageView).setImageResource(R.drawable.ic_pin)
        (btnLoginPIN.findViewById<View>(R.id.textView) as TextView).text = resources.getString(R.string.btn_login_pin)
        btnLoginPIN.findViewById<View>(R.id.textView).contentDescription = resources.getString(R.string.btn_login_pin) + " 버튼"
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            btnLoginPIN.isEnabled = false
            val tmp_textSub = btnLoginPIN.findViewById<TextView>(R.id.textViewSub)
            tmp_textSub.text = resources.getString(R.string.label_no_sensor_pin)
            tmp_textSub.visibility = View.VISIBLE
            tmp_textSub.isSelected = true
        }

        // --휴대폰인증 UI 세팅
        val btnLoginMobile = findViewById<View>(R.id.btnLoginMobile)
        (btnLoginMobile.findViewById<View>(R.id.imageView) as ImageView).setImageResource(R.drawable.ic_mobile_auth)
        (btnLoginMobile.findViewById<View>(R.id.textView) as TextView).text = resources.getString(R.string.btn_login_mobile)
        btnLoginMobile.findViewById<View>(R.id.textView).contentDescription = resources.getString(R.string.btn_login_mobile) + " 버튼"
        val tmp_textSub = btnLoginMobile.findViewById<TextView>(R.id.textViewSub)
        tmp_textSub.text = resources.getString(R.string.label_guide_mobile_auth_req)
        tmp_textSub.visibility = View.VISIBLE
        tmp_textSub.isSelected = true
        val textView = btnLoginMobile.findViewById<TextView>(R.id.textView)
        textView.setTextColor(Color.RED)
        btnLoginMobile.isEnabled = false




        // -- 카카오페이인증 UI 세팅
        val btnLoginKakao = findViewById<View>(R.id.btnLoginKakaopay)
        (btnLoginKakao.findViewById<View>(R.id.imageView) as ImageView).setImageResource(R.drawable.ic_kakaopay_auth_login)
        (btnLoginKakao.findViewById<View>(R.id.textView) as TextView).text = resources.getString(R.string.btn_login_kakaopay)
        btnLoginKakao.findViewById<View>(R.id.textView).contentDescription = resources.getString(R.string.btn_login_kakaopay) + " 버튼"


        // -- 간편인증관리 버튼
        val btnManageSimple = findViewById<Button>(R.id.activity_login_btnManageS)

        // -- 리스너
        btnLoginCert.setOnClickListener(this)
        btnLoginFinCert.setOnClickListener(this)
        btnLoginPassCert.setOnClickListener(this)
        btnLoginFinger.setOnClickListener(this)
        btnLoginPIN.setOnClickListener(this)
        btnLoginPattern.setOnClickListener(this)
        btnLoginMobile.setOnClickListener(this)
        btnLoginKakao.setOnClickListener(this)
        btnManageSimple.setOnClickListener(this)
    }


    /**
     * 자동로그인 UI 세팅 함수
     */
    private fun setAutoLoginUI() {
        findViewById<RelativeLayout>(R.id.linAutoLogin).visibility = View.VISIBLE // 자동로그인 layout 표기
        onOffControl = findViewById(R.id.onOffAutoLogin)
        onOffControl?.CF_setOnOffNoAnim(SharedPreferencesFunc.getFlagAutoLogin(applicationContext)) // 초기세팅
        onOffControl?.CE_setONChangedCheckedStateEventListener(OnChangedCheckedStateEventListener { p_flagCheck ->
            if(p_flagCheck) {
                CommonFunction.CF_showCustomAlertDilaog(this, "해당기기에 잠금상태가 적용되어 있을 경우에만 사용가능합니다 \n\n 셋팅값 : $p_flagCheck")
            }
        })
    }

    /**
     * 휴대폰본인인증 레이아웃 show
     */
    private fun showPhoneAuthBtnView() {
        val btnLoginMobile = findViewById<View>(R.id.btnLoginMobile)
        val textView = btnLoginMobile.findViewById<TextView>(R.id.textView)
        btnLoginMobile.isEnabled=true
        textView.setTextColor(Color.parseColor("#ff222222"))
    }

    /**
     * 임시 개발 로그인 레이아웃 세팅
     */
    private fun setTempPwLayoutUI() {
        LogPrinter.CF_debug("!----------------------------------------------------------")
        LogPrinter.CF_debug("!-- IUCOB0M00.setTempPwLayoutUI()")
        LogPrinter.CF_debug("!----------------------------------------------------------")

        // 확인 버튼 이벤트 연결 : 패스워드 일치시 로그인 화면 이동
        val btnOk = findViewById<Button>(R.id.btnOkPw)
        btnOk.setOnClickListener {
            val tmpEdit = findViewById<EditText>(R.id.edtPw)
            val tmpCsno = tmpEdit.text.toString().trim()
            LogPrinter.CF_debug("!-- tmp_csno : $tmpCsno")
            httpReq_tempAuth(tmpCsno)
        }
    }



    // #############################################################################################
    //  Http 관련
    // #############################################################################################
    override fun handleMessage(p_message: Message) {
        LogPrinter.CF_debug("!----------------------------------------------------------")
        LogPrinter.CF_debug("!-- IUCOC0M00.handleMessage()")
        LogPrinter.CF_debug("!----------------------------------------------------------")

        if (!isDestroyed) {
            when (p_message.what) {
                HANDLERJOB_AUTH_REQ -> try {
                    httpRes_tempCsnoAuth(JSONObject(p_message.obj as String))
                } catch (e: JSONException) {
                    LogPrinter.CF_line()
                    LogPrinter.CF_debug(resources.getString(R.string.log_json_exception))
                }
                HANDLERJOB_ERROR_AUTH_REQ -> {
                    CF_dismissProgressDialog()
                    CommonFunction.CF_showCustomAlertDilaog(this, p_message.obj as String, resources.getString(R.string.btn_ok))
                }
                else -> {
                }
            }
        }
    }

    /**
     * 개발 임시 고객전환 인증 요청
     */
    private fun httpReq_tempAuth(p_csno: String) {
        LogPrinter.CF_debug("!----------------------------------------------------------")
        LogPrinter.CF_debug("!-- IUCOB0M00.httpReq_tempAuth()")
        LogPrinter.CF_debug("!----------------------------------------------------------")

        // 프로그레스 다이얼로그 Show
        CF_showProgressDialog()
        val builder = Uri.Builder()
        builder.appendQueryParameter("csno", p_csno)
        builder.appendQueryParameter("userid", p_csno)
        HttpConnections.sendPostData(
                EnvConfig.host_url + EnvConfig.URL_TEMP_AUTH,
                builder.build().encodedQuery,
                handler,
                HANDLERJOB_AUTH_REQ,
                HANDLERJOB_ERROR_AUTH_REQ)
    }

    /**
     * 개발 임시 고객전환 인증 요청
     * @param p_jsonObject  JSONObject
     */
    @Throws(JSONException::class)
    private fun httpRes_tempCsnoAuth(p_jsonObject: JSONObject) {
        LogPrinter.CF_debug("!----------------------------------------------------------")
        LogPrinter.CF_debug("!-- IUCOB0M00.httpRes_tempAuth()")
        LogPrinter.CF_debug("!----------------------------------------------------------")
        LogPrinter.CF_debug("!---- p_jsonObject : $p_jsonObject")

        /*
        성공 케이스 p_jsonObject :
        {"data":{"flagCert":true, "flagCust":true,"csno":"114041783", "name":"나봄미", ,"flagInsuCust":true, "flagRegCert":true, "flagRegFido":false, "flagRegPin":false}
        ,"errCode":"", "debugMsg":"Success", "tempKey":"ba8HXH1kK2tkJLcjUbQM54YzxMHwvgi1QAFexxCuE"}

        실패 케이스 p_jsonObject :
        {"data":{"flagCert":false,"flagCust":false,"csno":"8855","name":"","flagInsuCust":true,"flagRegCert":true,"flagRegFido":false,"flagRegPin":false}
        ,"errCode":"","debugMsg":"고객정보 조회 실패 (TEL.654458_UEC654458UI0)"}

        실패 케이스 p_jsonObject :
        {"data":{"flagCert":false,"flagCust":false,"csno":"","name":"","flagInsuCust":true,"flagRegCert":false,"flagRegFido":false,"flagRegPin":false},
        "errCode":"ERRIUCOC4M00001","debugMsg":"요청데이터가 누락되었습니다."}
        */
        val jsonKey_errorCode = "errCode"
        val jsonKey_debugMsg = "debugMsg"
        val jsonKey_tempKey = "tempKey"
        val jsonKey_data = "data"
        val jsonKey_csno = "csno" // 고객번호
        val jsonKey_name = "name" // 고객명
        //final String jsonKey_flagInsuCust = "flagInsuCust";       // 보험청약고객여부
        val tmp_errorCode: String
        val tmp_debugMsg: String

        // --<1> (통신 성공)
        if (p_jsonObject.has(jsonKey_errorCode)) {
            tmp_errorCode = p_jsonObject.getString(jsonKey_errorCode)
            tmp_debugMsg = p_jsonObject.getString(jsonKey_debugMsg)
            // --<2> (전문 에러)
            if ("Success" != tmp_debugMsg) {
                val errMsg = "[" + tmp_errorCode + "] " + p_jsonObject.getString(jsonKey_debugMsg)
                CommonFunction.CF_showCustomAlertDilaog(this, errMsg, resources.getString(R.string.btn_ok))
            } else if (p_jsonObject.has(jsonKey_data)) {
                val tmp_jsonData = p_jsonObject.getJSONObject(jsonKey_data)
                val tmp_csno = tmp_jsonData.getString(jsonKey_csno) // 고객 csno
                val tmp_name = tmp_jsonData.getString(jsonKey_name) // 고객명


                // -- 인증정보 저장
                SharedPreferencesFunc.setLoginInfo(applicationContext, true, EnvConfig.AuthDvsn.CERT, tmp_csno, tmp_name)
                // -- Web 요청 TempKey 저장 : 메인에서 Web 호출 시 넘겨달라함.
                if (p_jsonObject.has(jsonKey_tempKey)) {
                    SharedPreferencesFunc.setWebTempKey(applicationContext, p_jsonObject.getString(jsonKey_tempKey))
                }
                // -- [단말DB] 사용자 정보 업데이트
                CustomSQLiteFunction.setLastLoginInfo(applicationContext, tmp_name, tmp_csno, EnvConfig.AuthDvsn.CERT)
                CommonFunction.CF_showCustomAlertDilaog(this, "[$tmp_csno / $tmp_name] 로그인 성공") {
                    setResult(RESULT_OK)
                    finish()
                }






                //로그인성공
                LocalBroadcastManager.getInstance(this).sendBroadcast(Intent().setAction(EnvConfig.BROADCAST_APPLOGINSUCCESS))



            } else {
                CommonFunction.CF_showCustomAlertDilaog(this, resources.getString(R.string.dlg_error_server_2), resources.getString(R.string.btn_ok))
            }
        } else {
            CommonFunction.CF_showCustomAlertDilaog(this, resources.getString(R.string.dlg_error_server_1), resources.getString(R.string.btn_ok))
        }
        CF_dismissProgressDialog()
    }
    // #############################################################################################
    //  Activity 호출
    // #############################################################################################
    /**
     * 간편인증 Activity 호출 함수<br></br>
     */
    private fun startIUFC00M00_fido(authDvsn: EnvConfig.AuthDvsn, p_view: View) {
        LogPrinter.CF_debug("!----------------------------------------------------------")
        LogPrinter.CF_debug("!-- IUCOB0M00.startIUCOE0M00() --간편인증 Activity 호출")
        LogPrinter.CF_debug("!----------------------------------------------------------")

        //if (CustomSQLiteFunction.hasUserCsno(applicationContext)) {
        // --<> (최초 여부) 기존에 해당 인증으로 로그인 했을 경우




        if(SharedPreferencesFunc.getFlagRegAuthDvsn(applicationContext, authDvsn)) {
            val intent = Intent(this, IUFC00M00::class.java)
            intent.putExtra(EnvConstant.KEY_INTENT_AUTH_MODE, EnvConfig.AuthMode.LOGIN_APP)
            intent.putExtra(EnvConstant.KEY_INTENT_AUTH_TECH_CODE, EnvConfig.AuthDvsn.getFidoByAuthDvsn(authDvsn))
            intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
            startActivityForResult(intent, EnvConfig.REQUESTCODE_ACTIVITY_IUFC00M00)
        }
        else {
            var msg = resources.getString(R.string.dlg_empty_csno)
            when (authDvsn) {
                EnvConfig.AuthDvsn.PIN -> {
                    msg = resources.getString(R.string.dlg_not_reg_pin)
                }
                EnvConfig.AuthDvsn.PATTERN -> {
                    msg = resources.getString(R.string.dlg_not_reg_pattern)
                }
                EnvConfig.AuthDvsn.PINGER -> {
                    msg = resources.getString(R.string.dlg_not_reg_finger2)
                }
            }
            showCustomDialog(msg, p_view)
        }
    }

    /**
     * 휴대폰 본인인증 Activity 호출 함수
     */
    private fun startIUPC90M00_P() {
        LogPrinter.CF_debug("!----------------------------------------------------------")
        LogPrinter.CF_debug("!-- IUCOB0M00.startIUPC90M00_P() --휴대폰 본인인증 Activity 호출")
        LogPrinter.CF_debug("!----------------------------------------------------------")
        LogPrinter.CF_debug("!---- 청구구분(mReqDvsn) : $mReqDvsn")

        val intent = Intent(this, IUPC90M00_P::class.java)
        intent.putExtra(EnvConstant.KEY_INTENT_AUTH_MODE, EnvConfig.AuthMode.LOGIN_APP)

        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
        startActivityForResult(intent, EnvConfig.REQUESTCODE_ACTIVITY_IUPC90M00)
    }

    /**
     * 카카오페이인증 Activity 호출 함수
     */
    private fun startIUPC95M00_kakao() {
        LogPrinter.CF_debug("!----------------------------------------------------------")
        LogPrinter.CF_debug("!-- IUCOB0M00.startIUPC95M00() --카카오페이인증 Activity 호출")
        LogPrinter.CF_debug("!----------------------------------------------------------")
        val intent: Intent

        // --<> (카카오페이인증 등록됨) 입력창 없이 인증 시작


        if (SharedPreferencesFunc.getFlagRegAuthDvsn(applicationContext, EnvConfig.AuthDvsn.KAKAOPAY)) {
            intent = Intent(this, IUPC95M20::class.java)
            intent.putExtra(EnvConstant.KEY_INTENT_AUTH_CSNO, CustomSQLiteFunction.getLastLoginCsno(this))
            intent.putExtra(EnvConstant.KEY_INTENT_AUTH_NAME, CustomSQLiteFunction.getLastLoginName(this))
        }
        else {
            intent = Intent(this, IUPC95M00_P::class.java)
        }


        intent.putExtra(EnvConstant.KEY_INTENT_AUTH_MODE, EnvConfig.AuthMode.LOGIN_APP)
        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
        startActivityForResult(intent, EnvConfig.REQUESTCODE_ACTIVITY_IUPC95M00)
    }

    /**
     * 인증센터 Activity 호출 함수
     */
    private fun startIUPC30M00() {
        LogPrinter.CF_debug("!----------------------------------------------------------")
        LogPrinter.CF_debug("!-- IUCOB0M00.startIUPC30M00() --인증센터 Activity 호출")
        LogPrinter.CF_debug("!----------------------------------------------------------")
        val intent = Intent(this, IUFC30M00::class.java)

        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
        startActivity(intent)
    }

    /**
     * 공동인증 로그인 Activity 호출 함수
     */
    private fun startIUCOC0M00() {
        LogPrinter.CF_debug("!----------------------------------------------------------")
        LogPrinter.CF_debug("!-- IUCOB0M00.startIUCOC0M00() --공동인증 로그인 Activity 호출")
        LogPrinter.CF_debug("!----------------------------------------------------------")
        val intent = Intent(this, IUCOC0M00::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
        startActivityForResult(intent, EnvConfig.REQUESTCODE_ACTIVITY_IUCOC0M00)
    }

    /**
     * 금융인증 로그인 Activity 호출 함수
     */
    private fun startIUPC10M00() {
        LogPrinter.CF_debug("!----------------------------------------------------------")
        LogPrinter.CF_debug("!-- IUCOB0M00.startIUCOC0M00() --금융인증 로그인 Activity 호출")
        LogPrinter.CF_debug("!----------------------------------------------------------")
        val intent = Intent(this, IUPC10M00::class.java)
        intent.putExtra(EnvConstant.KEY_INTENT_AUTH_MODE, EnvConfig.AuthMode.LOGIN_APP)
        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
        startActivityForResult(intent, EnvConfig.REQUESTCODE_ACTIVITY_IUPC10M00)
    }

    /**
     * PASS인증 로그인 Activity 호출 함수
     */
    private fun startIUPC20M00() {
        LogPrinter.CF_debug("!----------------------------------------------------------")
        LogPrinter.CF_debug("!-- IUCOB0M00.startIUPC20M00() --PASS인증 로그인 Activity 호출")
        LogPrinter.CF_debug("!----------------------------------------------------------")

        val intent = Intent(this, IUPC20M00::class.java)
        intent.putExtra(EnvConstant.KEY_INTENT_AUTH_MODE, EnvConfig.AuthMode.LOGIN_APP)

        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
        startActivityForResult(intent, EnvConfig.REQUESTCODE_ACTIVITY_IUPC20M00)
    }

}