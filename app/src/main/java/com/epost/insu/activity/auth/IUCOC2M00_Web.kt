package com.epost.insu.activity.auth

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.view.*
import android.view.View.OnTouchListener
import android.view.accessibility.AccessibilityEvent
import android.view.inputmethod.EditorInfo
import android.widget.*
import android.widget.TextView.OnEditorActionListener
import com.epost.insu.*
import com.epost.insu.activity.Activity_Default
import com.epost.insu.common.CommonFunction
import com.epost.insu.common.LogPrinter
import com.epost.insu.control.CustomCheckView
import com.epost.insu.dialog.CustomDialog
import com.epost.insu.network.HttpConnections
import com.epost.insu.network.WeakReferenceHandler
import com.epost.insu.network.WeakReferenceHandler.ObjectHandlerMessage
import com.initech.cryptox.util.Base64Util
import com.softsecurity.transkey.*
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException

/**
 * 공통 > 공동인증서 로그인 > 스마트보험 서비스 이용신청 (등록된 공동인증서가 아닌 경우 실행됨) (웹요청)
 * @since     :
 * @version   : 1.1
 * @author    : LSH
 * @see
 * <pre>
 * 스마트보험 서비스 이용신청 화면
 * 공동인증 로그인(IUCOC0M00_Web) 시 등록된 공동인증서가 아닌 경우 보인다
 * ======================================================================
 * 0.0.0    LSH_20170908    최초 등록
 * 0.0.0    NJM_20190220    폐기된 인증서 알림메시지 표기(임시)
 * 1.5.6    NJM_20210527    [인증오류코드 수정] ERRIUC0 -> ERRIUCO
 * 1.6.2    NJM_20210802    [2021년 대우 취약점] 2차본 : 부적절한 예외 처리
 * 1.6.3    NJM_20210826    [캡쳐방지 예외] 캡쳐방지 예외추가 및 로그표기일때만 예외처리
 * 1.6.3    NJM_20210826    url 공통화
 * =======================================================================
 * copyright : (주)프로니스정보기술 | http://www.phronis.co.kr
 * </pre>
 */
class IUCOC2M00_Web : Activity_Default(), ITransKeyActionListener, ITransKeyActionListenerEx, ITransKeyCallbackListener, OnTouchListener, ObjectHandlerMessage {
    //private val subUrl_web: String = "/CO/IUCOG0M02.do" // 개인정보 이용안내 url

    private val HANDLERJOB_SUBMIT: Int = 0
    private val HANDLERJOB_ERROR_SUBMIT: Int = 1

    private val maxLengthOfName: Int = 20
    private val maxLengthOfResident1: Int = 6
    private val maxLengthOfResident2: Int = 7

    private lateinit var relResident2: RelativeLayout
    private lateinit var editName: EditText
    private lateinit var editResident1: EditText
    private lateinit var checkView: CustomCheckView
    private var mTransKeyCtrl: TransKeyCtrl? = null

    private var sign: ByteArray? = null // 인증서 서명 값
    private var vvid: String? = null// 인증서 검증 값
    private var tempKey: String? = null // Web 로그인을 위한 값(Web에서 전달)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Intent 데이터 세팅
        setIntentData()

        // 보안키패드 초기화
        initTransKeyPad()
    }

    override fun onTouch(v: View, event: MotionEvent): Boolean {
        if (event.action == MotionEvent.ACTION_DOWN) {
            if (v.id == R.id.edit) {                                 // 이름 입력 EditText
                finishTranskeypad(false)
                editName.isCursorVisible = true
                return false
            }
            if (v.id == R.id.edtResident_1) {                      // 주민번호 앞자리 입력 EditText
                finishTranskeypad(false)
                editResident1.isCursorVisible = true
                return false
            } else if ((v.id == R.id.editText) || (v.id == R.id.keyscroll) || (v.id == R.id.keylayout)) {
                // 주민번호 뒷자리 입력관련 View
                showTransKeyPad()
                return true
            }
        }
        return false
    }

    override fun onBackPressed() {

        // -----------------------------------------------------------------------------------------
        //  보안 키패드 shown() 상태 : 키패드 hide
        // -----------------------------------------------------------------------------------------
        if (mTransKeyCtrl!!.isShown) {
            finishTranskeypad(false)
        } else {
            val customDialog = CustomDialog(this)
            customDialog.show()
            customDialog.CF_setTextContent(resources.getString(R.string.dlg_cancel_req_smart_service))
            customDialog.CF_setDoubleButtonText(resources.getString(R.string.btn_no), resources.getString(R.string.btn_yes))
            customDialog.setOnDismissListener { dialog ->
                if (!(dialog as CustomDialog).CF_getCanceled()) {
                    IntentManager.moveTaskToBack(this@IUCOC2M00_Web,true)
                    finish()
                }
            }
        }
    }

    override fun setInit() {
        setContentView(R.layout.iucoc2m00)
        handler = WeakReferenceHandler(this)
        try {
            mTransKeyCtrl = TransKeyCtrl(this)
        } catch (e: NullPointerException) {
            e.message
        } catch (e: Exception) {
            LogPrinter.CF_line()
            LogPrinter.CF_debug(resources.getString(R.string.log_fail_create_trankey))
        }
    }

    /**
     * Intent Data 세팅 함수
     */
    private fun setIntentData() {
        if (intent != null) {
            if (intent.hasExtra("sign")) {
                sign = intent.extras!!.getByteArray("sign")
            }
            if (intent.hasExtra("vvid")) {
                vvid = intent.extras!!.getString("vvid")
            }
            if (intent.hasExtra("tempKey")) {
                tempKey = intent.extras!!.getString("tempKey")
            }
        }
    }

    override fun setUIControl() {
        // 타이틀바 세팅
        setTitleBarUI()
        val linName: LinearLayout = findViewById(R.id.labelEditName)
        linName.isMotionEventSplittingEnabled = false

        // 라벨 세팅
        val txtName: TextView = linName.findViewById(R.id.label)
        txtName.text = resources.getString(R.string.label_name)

        // 이름 Edit세팅
        editName = linName.findViewById(R.id.edit)
        editName.contentDescription = resources.getString(R.string.desc_edt_name)
        editName.filters = CommonFunction.CF_getInputLengthFilter(maxLengthOfName)
        editName.hint = resources.getString(R.string.hint_name_2)
        editName.setOnTouchListener(this)
        editName.setOnEditorActionListener(object : OnEditorActionListener {
            override fun onEditorAction(v: TextView, actionId: Int, event: KeyEvent): Boolean {
                if (actionId == EditorInfo.IME_ACTION_NEXT) {
                    editName.isCursorVisible = false
                    editResident1.isCursorVisible = true
                    if (CommonFunction.CF_checkAccessibilityTurnOn(this@IUCOC2M00_Web)) {
                        editResident1.sendAccessibilityEvent(AccessibilityEvent.TYPE_VIEW_FOCUSED)
                    }
                }
                return false
            }
        })

        // 주민번호 앞자리
        editResident1 = findViewById(R.id.edtResident_1)
        editResident1.filters = CommonFunction.CF_getInputLengthFilter(maxLengthOfResident1)
        editResident1.contentDescription = resources.getString(R.string.desc_edt_resident_first)
        editResident1.nextFocusRightId = R.id.editText
        editResident1.setOnEditorActionListener(object : OnEditorActionListener {
            override fun onEditorAction(v: TextView, actionId: Int, event: KeyEvent): Boolean {
                if (actionId == EditorInfo.IME_ACTION_NEXT) {
                    showTransKeyPad()
                }
                return false
            }
        })
        editResident1.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                if (s.length == maxLengthOfResident1) {
                    showTransKeyPad()
                }
            }

            override fun afterTextChanged(s: Editable) {}
        })
        editResident1.setOnTouchListener(this)

        // 주민번호 뒷자리
        val editResident2: EditText = findViewById<View>(R.id.resident_relInput).findViewById<View>(R.id.editText) as EditText
        editResident2.setOnTouchListener(this)
        editResident2.contentDescription = resources.getString(R.string.desc_edt_resident_last)
        editResident2.importantForAccessibility = View.IMPORTANT_FOR_ACCESSIBILITY_NO
        relResident2 = findViewById(R.id.resident_relInput_sub)
        relResident2.isMotionEventSplittingEnabled = false
        relResident2.contentDescription = "입력창, " + resources.getString(R.string.desc_edt_resident_last) + ", 편집하려면 두 번 누르세요."
        val tmp_keyLayout: ViewGroup = findViewById<View>(R.id.resident_relInput).findViewById(R.id.keylayout)
        tmp_keyLayout.setOnTouchListener(this)
        val tmp_keyScroll: ViewGroup = findViewById<View>(R.id.resident_relInput).findViewById(R.id.keyscroll)
        tmp_keyScroll.setOnTouchListener(this)

        // 개인정보 이용안내 WebView
        //WebView tmp_WebAgree_1 = (WebView)findViewById(R.id.webView);
        //tmp_WebAgree_1.loadUrl(EnvConfig.host_url+subUrl_web);

        // 이용동의 체크View
        checkView = findViewById(R.id.checkAgree)
        checkView.CF_setBgOff(R.drawable.oval_check_none_2)

        // 확인 버튼
        val btnReg: Button = findViewById(R.id.btnFill)
        btnReg.text = resources.getString(R.string.btn_ok)
        btnReg.setOnClickListener {
            if (checkUserInput()) {
                requestSubmit()
            }
        }
    }

    /**
     * 타이틀바 UI 세팅 함수
     */
    private fun setTitleBarUI() {

        // 타이틀 세팅
        val title: TextView = findViewById(R.id.title_bar_textTitle)
        title.text = resources.getString(R.string.title_agree_s_service)

        // left 버튼 세팅
        val btnLeft: ImageButton = findViewById(R.id.title_bar_imgBtnLeft)
        btnLeft.visibility = View.VISIBLE
        btnLeft.setOnClickListener {
            val customDialog = CustomDialog(this@IUCOC2M00_Web)
            customDialog.show()
            customDialog.CF_setTextContent(resources.getString(R.string.dlg_cancel_req_smart_service))
            customDialog.CF_setDoubleButtonText(resources.getString(R.string.btn_no), resources.getString(R.string.btn_yes))
            customDialog.setOnDismissListener { dialog ->
                if (!(dialog as CustomDialog).CF_getCanceled()) {
                    IntentManager.moveTaskToBack(this@IUCOC2M00_Web,true)
                    finish()
                }
            }
        }
    }

    /**
     * 키패드 초기 세팅 함수
     */
    private fun initTransKeyPad() {
        val intent: Intent = getIntentParam(TransKeyActivity.mTK_TYPE_KEYPAD_NUMBER,
            TransKeyActivity.mTK_TYPE_TEXT_PASSWORD_EX,
            "",
            7,
            "",
            5,
            50)
        mTransKeyCtrl!!.init(intent,
            findViewById<View>(R.id.keypadContainer) as FrameLayout?,
            (findViewById<View>(R.id.resident_relInput)).findViewById<View>(R.id.editText) as EditText?,
            (findViewById<View>(R.id.resident_relInput)).findViewById<View>(R.id.keyscroll) as HorizontalScrollView?,
            (findViewById<View>(R.id.resident_relInput)).findViewById<View>(R.id.keylayout) as LinearLayout?,
            (findViewById<View>(R.id.resident_relInput)).findViewById<View>(R.id.clearall) as ImageButton?,
            findViewById<View>(R.id.keypadBallon) as RelativeLayout?,
            null)
        //mTransKeyCtrl.setReArrangeKeapad(true);
        mTransKeyCtrl!!.setTransKeyListener(this)
        mTransKeyCtrl!!.setTransKeyListenerEx(this)
        mTransKeyCtrl!!.setTransKeyListenerCallback(this)

        // -- 캡쳐 방지 적용 (DRM적용: addFlags / 미적용: clearFlags)
        if (EnvConfig.mFlagShowLog) {
            window?.clearFlags(WindowManager.LayoutParams.FLAG_SECURE)
        } else {
            window?.addFlags(WindowManager.LayoutParams.FLAG_SECURE)
        }
    }

    /**
     * 보안키패드 설정 Intent 반환 함수
     *
     * @param keyPadType       키패드 타입
     * @param textType         보안 텍스트 타입
     * @param hint             입력 힌트
     * @param maxLength        입력 최대길이
     * @param maxLengthMessage 최대길이 도달시 보일 메시지
     * @param line3Padding     라인패딩
     * @param reduceRate       크기 조절 배율
     * @return  Intent
     */
    private fun getIntentParam(keyPadType: Int, textType: Int, hint: String, maxLength: Int, maxLengthMessage: String, line3Padding: Int, reduceRate: Int): Intent {
        val intent = Intent(applicationContext, TransKeyActivity::class.java)

        //val int mTK_TYPE_KEYPAD_NUMBER				//숫자전용
        //val int mTK_TYPE_KEYPAD_QWERTY_LOWER		//소문자 쿼티
        //val int mTK_TYPE_KEYPAD_QWERTY_UPPER		//대문자 쿼티
        //val int mTK_TYPE_KEYPAD_ABCD_LOWER			//소문자 순열자판
        //val int mTK_TYPE_KEYPAD_ABCD_UPPER			//대문자 순열자판
        //val int mTK_TYPE_KEYPAD_SYMBOL				//심벌자판
        intent.putExtra(TransKeyActivity.mTK_PARAM_KEYPAD_TYPE, keyPadType)

        //키보드가 입력되는 형태
        //TransKeyActivity.mTK_TYPE_TEXT_IMAGE - 보안 텍스트 입력
        //TransKeyActivity.mTK_TYPE_TEXT_PASSWORD - 패스워드 입력
        //TransKeyActivity.mTK_TYPE_TEXT_PASSWORD_EX - 마지막 글자 보여주는 패스워드 입력
        intent.putExtra(TransKeyActivity.mTK_PARAM_INPUT_TYPE, textType)

        //키패드입력화면의 입력 라벨
        //newIntent.putExtra(TransKeyActivity.mTK_PARAM_NAME_LABEL, label);
        intent.putExtra(TransKeyActivity.mTK_PARAM_DISABLE_SPACE, false)
        intent.putExtra(TransKeyActivity.mTK_PARAM_HIDE_TIMER_DELAY, 5)

        //최대 입력값 설정
        intent.putExtra(TransKeyActivity.mTK_PARAM_INPUT_MAXLENGTH, maxLength)
        intent.putExtra(TransKeyActivity.mTK_PARAM_USE_ATM_MODE, false)
        //인터페이스 - maxLength시에 메시지 박스 보여주기. 기본은 메시지 안나옴.
        //newIntent.putExtra(TransKeyActivity.mTK_PARAM_MAX_LENGTH_MESSAGE, maxLengthMessage);

        // --> SERVER 연동
        intent.putExtra(TransKeyActivity.mTK_PARAM_CRYPT_TYPE, TransKeyActivity.mTK_TYPE_CRYPT_SERVER)
        intent.putExtra(TransKeyActivity.mTK_PARAM_SECURE_KEY, EnvConfig.mTransServerKey)

        // --> Local
        //newIntent.putExtra(TransKeyActivity.mTK_PARAM_CRYPT_TYPE, TransKeyActivity.mTK_TYPE_CRYPT_LOCAL);


        //해당 Hint 메시지를 보여준다.
        intent.putExtra(TransKeyActivity.mTK_PARAM_SET_HINT, hint)

        //Hint 테스트 사이즈를 설정한다.(단위 dip, 0이면 디폴트 크기로 보여준다.)
        intent.putExtra(TransKeyActivity.mTK_PARAM_SET_HINT_TEXT_SIZE, 0)

        //커서를 보여준다.
        intent.putExtra(TransKeyActivity.mTK_PARAM_SHOW_CURSOR, true)
        intent.putExtra(TransKeyActivity.mTK_PARAM_USE_CUSTOM_CURSOR, true)

        //에디트 박스안의 글자 크기를 조절한다.
        intent.putExtra(TransKeyActivity.mTK_PARAM_EDIT_CHAR_REDUCE_RATE, reduceRate)

        //심볼 변환 버튼을 비활성화 시킨다.
        intent.putExtra(TransKeyActivity.mTK_PARAM_DISABLE_SYMBOL, false)
        intent.putExtra(TransKeyActivity.mTK_PARAM_PREVENT_CAPTURE, true)

        //심볼 변환 버튼을 비활성화 시킬 경우 팝업 메시지를 설정한다.
        //newIntent.putExtra(TransKeyActivity.mTK_PARAM_DISABLE_SYMBOL_MESSAGE, "심볼키는 사용할 수 없습니다.");

        //////////////////////////////////////////////////////////////////////////////
        //인터페이스 - line3 padding 값 설정 가능 인자. 기본은 0 값이면 아무 설정 안했을 시 원래 transkey에서 제공하던 값을 제공한다..
        //newIntent.putExtra(TransKeyActivity.mTK_PARAM_KEYPAD_MARGIN, line3Padding);
        //newIntent.putExtra(TransKeyActivity.mTK_PARAM_KEYPAD_LEFT_RIGHT_MARGIN, 0);
        intent.putExtra(TransKeyActivity.mTK_PARAM_KEYPAD_HIGHEST_TOP_MARGIN, 2)
        intent.putExtra(TransKeyActivity.mTK_PARAM_USE_TALKBACK, true)
        intent.putExtra(TransKeyActivity.mTK_PARAM_SUPPORT_ACCESSIBILITY_SPEAK_PASSWORD, true)
        intent.putExtra(TransKeyActivity.mTK_PARAM_USE_SHIFT_OPTION, true)
        intent.putExtra(TransKeyActivity.mTK_PARAM_USE_CLEAR_BUTTON, false)
        intent.putExtra(TransKeyActivity.mTK_PARAM_USE_KEYPAD_ANIMATION, true)
        intent.putExtra(TransKeyActivity.mTK_PARAM_SAMEKEY_ENCRYPT_ENABLE, true)

//		newIntent.putExtra(TransKeyActivity.mTK_PARAM_LANGUAGE, TransKeyActivity.mTK_Language_English);
        val language: Int = TransKeyActivity.mTK_Language_Korean
        intent.putExtra(TransKeyActivity.mTK_PARAM_LANGUAGE, language)
        return intent
    }

    /**
     * 보안키패드 숨김
     */
    private fun finishTranskeypad(p_flagFinishOption: Boolean) {
        if (mTransKeyCtrl!!.isShown) {
            mTransKeyCtrl!!.finishTransKey(p_flagFinishOption)
            if (CommonFunction.CF_checkAccessibilityTurnOn(this)) {
                relResident2.announceForAccessibility("보안키패드 숨김.")
            }
        }
    }

    /**
     * 사용자 입력값 검사 함수
     * @return  boolean
     */
    private fun checkUserInput(): Boolean {
        var flagOk = true
        if (editName.text.toString().trim().isEmpty()) {
            flagOk = false
            showCustomDialog(resources.getString(R.string.dlg_no_input_name), editName)
        } else if (editResident1.text.toString().length < maxLengthOfResident1 || mTransKeyCtrl!!.inputLength < maxLengthOfResident2) {
            flagOk = false
            showCustomDialog(resources.getString(R.string.dlg_no_input_resident), editResident1)
        } else if (!checkView.CF_isChecked()) {
            flagOk = false
            showCustomDialog(resources.getString(R.string.dlg_need_agree_smart_service), checkView)
        } else if (sign == null || TextUtils.isEmpty(vvid)) {
            flagOk = false
            CommonFunction.CF_showCustomAlertDilaog(this@IUCOC2M00_Web, resources.getString(R.string.dlg_fail_req_smartservice), resources.getString(R.string.btn_ok))
        }
        return flagOk
    }

    /**
     * 키패드 Show 함수
     */
    private fun showTransKeyPad() {
        var flagWaitTimeMilli = 0L

        // -----------------------------------------------------------------------------------------
        //  EditText에 포커스가 있는 경우 보안키패드 팝업 delay
        // -----------------------------------------------------------------------------------------
        if (editName.hasFocus()) {
            CommonFunction.CF_closeVirtualKeyboard(this, editName.windowToken)
            editName.clearFocus()
            editName.isCursorVisible = false
            flagWaitTimeMilli = 120L
        }
        if (editResident1.hasFocus()) {
            CommonFunction.CF_closeVirtualKeyboard(this, editResident1.windowToken)
            editResident1.clearFocus()
            editResident1.isCursorVisible = false
            flagWaitTimeMilli = 120L
        }
        Handler().postDelayed(object : Runnable {
            override fun run() {
                relResident2.requestFocus()
                mTransKeyCtrl!!.showKeypad(TransKeyActivity.mTK_TYPE_KEYPAD_NUMBER)
                if (CommonFunction.CF_checkAccessibilityTurnOn(this@IUCOC2M00_Web)) {
                    relResident2.sendAccessibilityEvent(AccessibilityEvent.TYPE_VIEW_FOCUSED)
                    relResident2.announceForAccessibility("보안키패드 표시됨")
                }
            }
        }, flagWaitTimeMilli)
    }

    /**
     * (IUCOC3M00) 공동인증 로그인 완료 Activity 호출 함수
     */
    private fun startIUCOC3M00() {
        val intent = Intent(this, IUCOC3M00::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
        startActivity(intent)
    }

    // #############################################################################################
    //  보안키패드 이벤트
    // #############################################################################################
    override fun cancel(intent: Intent) {}
    override fun done(p_intent: Intent) {
        if (CommonFunction.CF_checkAccessibilityTurnOn(this@IUCOC2M00_Web)) {
            clearAllFocus()
            val txtLabelAgree: TextView = findViewById(R.id.textLabelSmartService)
            txtLabelAgree.isFocusableInTouchMode = true
            txtLabelAgree.requestFocus()
            txtLabelAgree.isFocusableInTouchMode = false
        }
    }

    override fun input(i: Int) {
        if (mTransKeyCtrl!!.inputLength >= maxLengthOfResident2) {
            finishTranskeypad(true)
        }
    }

    override fun minTextSizeCallback() {
        // nothing
    }

    override fun maxTextSizeCallback() {
        // nothing
    }

    // #############################################################################################
    //  Http 관련
    // #############################################################################################
    override fun handleMessage(p_message: Message) {
        if (!isDestroyed) {
            // 프로그레스 다이얼로그 dismiss
            CF_dismissProgressDialog()
            when (p_message.what) {
                HANDLERJOB_SUBMIT -> try {
                    setResultOfSubmit(JSONObject(p_message.obj as String?))
                } catch (e: JSONException) {
                    LogPrinter.CF_line()
                    LogPrinter.CF_debug(resources.getString(R.string.log_json_exception))
                }
                HANDLERJOB_ERROR_SUBMIT -> CommonFunction.CF_showCustomAlertDilaog(this, p_message.obj as String?, resources.getString(R.string.btn_ok))
                else -> {
                }
            }
        }
    }

    /**
     * 스마트보험 서비스 이용신청
     */
    private fun requestSubmit() {
        // 프로그레스 다이얼로그 show
        CF_showProgressDialog()
        val builder: Uri.Builder = Uri.Builder()
        try {
            builder.appendQueryParameter("signedData", String(Base64Util.encode(sign)))
        } catch (e: IOException) {
            LogPrinter.CF_line()
            LogPrinter.CF_debug(resources.getString(R.string.log_fail_encoding_base64))
        }
        builder.appendQueryParameter("rv", vvid)
        builder.appendQueryParameter("name", editName.text.toString().trim())
        builder.appendQueryParameter("tempKey", tempKey)
        builder.appendQueryParameter("rn_1", editResident1.text.toString().trim())
        builder.appendQueryParameter("encoded_rn_2", mTransKeyCtrl!!.cipherDataEx)
        HttpConnections.sendPostData(
            EnvConfig.host_url + EnvConfig.URL_CERT_REG_WEB_REQ,
            builder.build().encodedQuery,
            handler,
            HANDLERJOB_SUBMIT,
            HANDLERJOB_ERROR_SUBMIT)
    }

    /**
     * 스마트보험 서비스 이용 신청 결과 처리 함수
     * @param p_jsonObject  JSONObject
     */
    @Throws(JSONException::class)
    private fun setResultOfSubmit(p_jsonObject: JSONObject) {
        val jsonKey_errorCode = "errCode"
        val jsonKey_data = "data"
        val jsonKey_flagCert = "flagCert" // 공동인증유효여부
        val jsonKey_flagCust = "flagCust" // 고객여부(true csno있는상태)
        val tmp_errorCode: String

        if (p_jsonObject.has(jsonKey_errorCode)) {
            tmp_errorCode = p_jsonObject.getString(jsonKey_errorCode)
            if (!TextUtils.isEmpty(tmp_errorCode)) {
                // ---------------------------------------------------------------------------------
                // ERRIUCOC2M00001 : 요청데이터 누락
                // ERRIUCOC2M00002 : 인증서 파일 처리 오류
                // ERRIUCOC2M00003 : 인증시스템 장애
                // ERRIUCOC2M00004 : 인증서 검증 모듈 오류
                // ERRIUCOC2M00005 : 인증서DN추출 실패(미사용)
                // ERRIUCOC2M00006 : 스마트보험 서비스 고객으로 등록 실패
                // ERRIUCOC2M00007 : 전자금융고객여부 조회 실패(미사용)
                // ERRIUCOC2M00008 : 청약고객여부 조회 실패(미사용)
                // ERRIUCOC2M00009 : 개인식별번호 조회 실패(미사용)
                // ERRIUCOC2M00010 : 인증서 검증 실패(미사용)
                // ERRIUCOC2M00011 : 입력된 실명번호가 고객정보와 다름(미사용)
                // ERRIUCOC2M00091 : 폐기된 인증서 {ER91}
                // ---------------------------------------------------------------------------------
                if ((tmp_errorCode == "ERRIUCOC2M00006")) {
                    val customDialog = CustomDialog(this)
                    customDialog.show()
                    customDialog.CF_setBackKeyMode(CustomDialog.BackMode.NOTUSE)
                    customDialog.CF_setTextContent(resources.getString(R.string.dlg_error_erriucoc2m00006))
                    customDialog.CF_setSingleButtonText(resources.getString(R.string.btn_ok))
                    customDialog.setOnDismissListener { finish() }
                }
                else {
                    var errMsg: String? = "[" + tmp_errorCode + "] " + resources.getString(R.string.dlg_error_erriucoc2m00001)
                    if (("ERRIUCOC2M00091" == tmp_errorCode)) {
                        errMsg = resources.getString(R.string.dlg_error_cert_91)
                    }
                    CommonFunction.CF_showCustomAlertDilaog(this, errMsg, resources.getString(R.string.btn_ok))
                }
            } else if (p_jsonObject.has(jsonKey_data)) {
                val tmp_jsonData: JSONObject = p_jsonObject.getJSONObject(jsonKey_data)
                val tmp_flagCert: Boolean = tmp_jsonData.getBoolean(jsonKey_flagCert) // 공동인증발급기관에서의 전자서명값 체크 여부(if false : 유효하지 않은 공동인증서)
                val tmp_flagCust: Boolean = tmp_jsonData.getBoolean(jsonKey_flagCust) // 모바일슈랑스 고객 DB에 공동인증 정보 저장 유무
                if (!tmp_flagCust) {
                    // 우체국 고객이 아니기에 서비스 이용 신청 불가
                    val tmp_dlg: CustomDialog = CustomDialog(this)
                    tmp_dlg.show()
                    tmp_dlg.CF_setBackKeyMode(CustomDialog.BackMode.NOTUSE)
                    tmp_dlg.CF_setTextContent(resources.getString(R.string.dlg_no_member))
                    tmp_dlg.CF_setSingleButtonText(resources.getString(R.string.btn_ok))
                    tmp_dlg.setOnDismissListener {
                        setResult(RESULT_CANCELED)
                        finish()
                    }
                } else if (!tmp_flagCert) {
                    CommonFunction.CF_showCustomAlertDilaog(this, resources.getString(R.string.dlg_error_erriucoc1m00002), resources.getString(R.string.btn_ok))
                } else {
                    startIUCOC3M00() // 공동인증 로그인 완료 화면
                    finish()
                }
            } else {
                CommonFunction.CF_showCustomAlertDilaog(this, resources.getString(R.string.dlg_error_server_2), resources.getString(R.string.btn_ok))
            }
        } else {
            CommonFunction.CF_showCustomAlertDilaog(this, resources.getString(R.string.dlg_error_server_1), resources.getString(R.string.btn_ok))
        }
    }
}