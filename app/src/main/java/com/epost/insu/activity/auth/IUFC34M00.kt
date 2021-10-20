package com.epost.insu.activity.auth

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.view.MotionEvent
import android.view.View
import android.view.View.OnTouchListener
import android.view.ViewGroup
import android.view.WindowManager
import android.view.accessibility.AccessibilityEvent
import android.view.inputmethod.EditorInfo
import android.widget.*
import android.widget.TextView.OnEditorActionListener
import com.epost.insu.*
import com.epost.insu.EnvConfig.AuthMode
import com.epost.insu.common.CommonFunction
import com.epost.insu.common.LogPrinter
import com.epost.insu.control.CustomCheckView
import com.epost.insu.control.SwitchingControl
import com.epost.insu.dialog.CustomDialog
import com.epost.insu.event.OnChangedCheckedStateEventListener
import com.epost.insu.fido.Fido2Constant
import com.epost.insu.network.HttpConnections
import com.epost.insu.network.WeakReferenceHandler
import com.epost.insu.network.WeakReferenceHandler.ObjectHandlerMessage
import com.softsecurity.transkey.*
import org.json.JSONException
import org.json.JSONObject

/**
 * 인증센터 > 개인인증번호 (재)등록
 * @since     : project 48:1.4.5
 * @version   : 1.1
 * @author    : NJM
 * @see
 * <pre>
 * ======================================================================
 * 1.4.5    NJM_20201229    최초 등록
 * 1.5.2    NJM_20210317    onActivityResult에서 실패 메시지 팝업 처리
 * 1.5.2    NJM_20210322    [subUrl 공통파일로 변경]
 * 1.5.3    NJM_20210330    [FIDO호출 로직 변경] 에러발생으로인한 변경
 * 1.5.4    NJM_20210506    [IUFC34M00 임시고객번호 삭제]
 * 1.5.9    NJM_20210701    [PIN등록 오류 수정] 최초설치시 PIN등록시 csno 누락 오류 수정
 * 1.6.1    NJM_20210705    [간편인증 등록 tempkey] 간편인증 등록시 로그인 처리 후 tempkey 누락 -> 로그인 처리 안함
 * 1.6.2    NJM_20210729    [간편인증 플래그 반영] 간편인증 로그인시 flag값 참조 변경 (서버저장 -> 단말저장)
 * 1.6.2    NJM_20210802    [2021년 대우 취약점] 2차본 : 부적절한 예외 처리
 * 1.6.3    NJM_20210826    [캡쳐방지 예외] 캡쳐방지 예외추가 및 로그표기일때만 예외처리
 * =======================================================================
 * copyright : (주)프로니스정보기술 | http://www.phronis.co.kr
 * </pre>
 */
class IUFC34M00 : Activity_Auth(), ITransKeyActionListener, ITransKeyActionListenerEx, ITransKeyCallbackListener, OnTouchListener, ObjectHandlerMessage {
    private val HANDLERJOB_SUBMIT = 0
    private val HANDLERJOB_ERROR_SUBMIT = 1
    private val maxLengthOfName = 20
    private val maxLengthOfResident_1 = 6
    private val maxLengthOfresident_2 = 7

    private lateinit var relResident_2: RelativeLayout
    private lateinit var editName: EditText
    private lateinit var editResident_1: EditText
    private lateinit var editResident_2: EditText
    private lateinit var checkView: CustomCheckView
    private lateinit var switchAuthSign: SwitchingControl

    private var mTransKeyCtrl: TransKeyCtrl? = null

    private var mResCsno = ""
    private var mResName = ""

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        LogPrinter.CF_debug("!----------------------------------------------------------")
        LogPrinter.CF_debug("!-- IUFC34M00.onActivityResult()")
        LogPrinter.CF_debug("!----------------------------------------------------------")
        LogPrinter.CF_debug("!-- requestCode : $requestCode")
        LogPrinter.CF_debug("!-- resultCode  : $resultCode")
        LogPrinter.CF_debug("!-- data        : " + CommonFunction.CF_intentToString(data))

        if (resultCode == RESULT_OK) {
            when (requestCode) {
                EnvConfig.REQUESTCODE_ACTIVITY_IUPC90M00 ->
                    // --<> (공동인증서 선택)
                    if (switchAuthSign.CF_getCheckState() == 1) {
                        startIUPC80M00_cert()
                    }
                    else { // --<> (카카오페인증 선택)
                        startIUCOK0M00_kakao()
                    }
                EnvConfig.REQUESTCODE_ACTIVITY_IUPC80M00,
                EnvConfig.REQUESTCODE_ACTIVITY_IUCOK0M00 -> startIUFC10M00_fidoReg()

                // -- 개인인증번호(PIN) 등록완료
                EnvConfig.REQUESTCODE_ACTIVITY_IUFC10M00 -> {
                    // -- 최근 로그인정보 변경(로그인 처리 없이 csno 단말기에 저장)
                    //CustomSQLiteFunction.setLastLoginInfo(applicationContext, mResName, mResCsno, EnvConfig.AuthDvsn.PIN)
                    CF_setLogin(false, EnvConfig.AuthDvsn.PIN, mResCsno, mResName, "")
                    startIUPC39M00()
                }

                // -- 간편인증 완료화면 종료
                EnvConfig.REQUESTCODE_ACTIVITY_IUPC39M00 -> {
                    setResult(RESULT_OK)
                    finish()
                }
            }
        } else {
            var rtnMsg: String? = ""
            if (data != null && data.hasExtra(EnvConstant.KEY_INTENT_RTN_MSG)) {
                rtnMsg = data.extras!!.getString(EnvConstant.KEY_INTENT_RTN_MSG)
            }
            setResult(RESULT_CANCELED)
            if ("" != rtnMsg) {
                CommonFunction.CF_showCustomDialogFinishActivity(this@IUFC34M00, rtnMsg)
            } else {
                finish()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        LogPrinter.CF_debug("!----------------------------------------------------------")
        LogPrinter.CF_debug("!-- IUFC34M00.onCreate() --")
        LogPrinter.CF_debug("!----------------------------------------------------------")

        // -- 보안키패드 초기화
        initTransKeyPad()
    }

    override fun onTouch(v: View, event: MotionEvent): Boolean {
        if (event.action == MotionEvent.ACTION_DOWN) {
            // 이름 입력 EditText
            if (v.id == R.id.edit) {
                finishTranskeypad(false)
                editName.isCursorVisible = true
                return false
            } else if (v.id == R.id.edtResident_1) {
                finishTranskeypad(false)
                editResident_1.isCursorVisible = true
                return false
            } else if (v.id == R.id.editText || v.id == R.id.keyscroll || v.id == R.id.keylayout) {
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
            customDialog.CF_setTextContent(resources.getString(R.string.dlg_cancel_req_bio_service))
            customDialog.CF_setDoubleButtonText(resources.getString(R.string.btn_no), resources.getString(R.string.btn_yes))
            customDialog.setOnDismissListener { dialog ->
                if (!(dialog as CustomDialog).CF_getCanceled()) {
                    finish()
                }
            }
        }
    }

    override fun setInit() {
        LogPrinter.CF_debug("!----------------------------------------------------------")
        LogPrinter.CF_debug("!-- IUFC34M00.setInit() --")
        LogPrinter.CF_debug("!----------------------------------------------------------")

        setContentView(R.layout.iufc34m00)
        handler = WeakReferenceHandler(this)
        try {
            mTransKeyCtrl = TransKeyCtrl(this)
        } catch (e: NullPointerException) {
            e.message
        } catch (e: Exception) {
            e.message
            LogPrinter.CF_debug(resources.getString(R.string.log_fail_create_trankey))
        }
    }

    override fun setUIControl() {
        LogPrinter.CF_debug("!----------------------------------------------------------")
        LogPrinter.CF_debug("!-- IUFC34M00.setUIControl() --")
        LogPrinter.CF_debug("!----------------------------------------------------------")

        // 타이틀바 세팅
        setTitleBarUI()
        val tmp_linName = findViewById<LinearLayout>(R.id.labelEditName)
        tmp_linName.isMotionEventSplittingEnabled = false

        // 라벨 세팅
        val tmp_textName = tmp_linName.findViewById<TextView>(R.id.label)
        tmp_textName.text = resources.getString(R.string.label_name)

        // 이름 Edit세팅
        editName = tmp_linName.findViewById(R.id.edit)
        editName.contentDescription = resources.getString(R.string.desc_edt_name)
        editName.filters = CommonFunction.CF_getInputLengthFilter(maxLengthOfName)
        editName.hint = resources.getString(R.string.hint_name_2)
        editName.setOnTouchListener(this)
        editName.setOnEditorActionListener(OnEditorActionListener { v, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_NEXT) {
                editName.isCursorVisible = false
                editResident_1.isCursorVisible = true
                if (CommonFunction.CF_checkAccessibilityTurnOn(this@IUFC34M00)) {
                    editResident_1.sendAccessibilityEvent(AccessibilityEvent.TYPE_VIEW_FOCUSED)
                }
            }
            false
        })

        // 주민번호 앞자리
        editResident_1 = findViewById(R.id.edtResident_1)
        editResident_1.filters = CommonFunction.CF_getInputLengthFilter(maxLengthOfResident_1)
        editResident_1.contentDescription = resources.getString(R.string.desc_edt_resident_first)
        editResident_1.nextFocusRightId = R.id.editText
        editResident_1.setOnEditorActionListener(OnEditorActionListener { v, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_NEXT) {
                showTransKeyPad()
            }
            false
        })
        editResident_1.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                if (s.length == maxLengthOfResident_1) {
                    showTransKeyPad()
                }
            }

            override fun afterTextChanged(s: Editable) {}
        })
        editResident_1.setOnTouchListener(this)

        // 주민번호 뒷자리
        val linResident2 = findViewById<View>(R.id.resident_relInput)
        editResident_2 = linResident2.findViewById(R.id.editText)
        editResident_2.contentDescription = resources.getString(R.string.desc_edt_resident_last)
        editResident_2.importantForAccessibility = View.IMPORTANT_FOR_ACCESSIBILITY_NO
        relResident_2 = findViewById(R.id.resident_relInput_sub)
        relResident_2.isMotionEventSplittingEnabled = false
        relResident_2.contentDescription = "입력창, " + resources.getString(R.string.desc_edt_resident_last) + ", 편집하려면 두 번 누르세요."
        editResident_2.setOnTouchListener(this)
        val tmp_keyLayout = linResident2.findViewById<ViewGroup>(R.id.keylayout)
        tmp_keyLayout.setOnTouchListener(this)
        val tmp_keyScroll = linResident2.findViewById<ViewGroup>(R.id.keyscroll)
        tmp_keyScroll.setOnTouchListener(this)

        // 이용동의 체크View
        checkView = findViewById(R.id.checkAgree)
        checkView.CF_setBgOff(R.drawable.oval_check_none_2)

        // -- 전자서명
        switchAuthSign = findViewById(R.id.switchingInsureCar)
        switchAuthSign.CF_getBtnLeft().contentDescription = "공동인증서"
        switchAuthSign.CF_getBtnRight().contentDescription = "카카오페이인증"
        switchAuthSign.CF_setText(" 공동인증서 ", "카카오페이")
        switchAuthSign.CF_setUniformWidth()
        switchAuthSign.CE_setOnChangedCheckedStateEventListener(OnChangedCheckedStateEventListener { })

        // 확인 버튼
        val btnOk = findViewById<Button>(R.id.btnFill)
        btnOk.text = resources.getString(R.string.btn_ok_bio)
        btnOk.setOnClickListener {
            if (checkUserInput()) {
                httpReq_getUserInfo()
            }
        }
    }

    /**
     * 타이틀바 UI 세팅 함수
     */
    private fun setTitleBarUI() {
        LogPrinter.CF_debug("!----------------------------------------------------------")
        LogPrinter.CF_debug("!-- IUFC34M00.setTitleBarUI() --")
        LogPrinter.CF_debug("!----------------------------------------------------------")

        // 타이틀 세팅
        val txtTitle = findViewById<TextView>(R.id.title_bar_textTitle)
        txtTitle.text = resources.getString(R.string.title_agree_bio_service)

        // left 버튼 세팅
        val btnLeft = findViewById<ImageButton>(R.id.title_bar_imgBtnLeft)
        btnLeft.visibility = View.VISIBLE
        btnLeft.setOnClickListener {
            val customDialog = CustomDialog(this@IUFC34M00)
            customDialog.show()
            customDialog.CF_setTextContent(resources.getString(R.string.dlg_cancel_req_bio_service))
            customDialog.CF_setDoubleButtonText(resources.getString(R.string.btn_no), resources.getString(R.string.btn_yes))
            customDialog.setOnDismissListener { dialog ->
                if (!(dialog as CustomDialog).CF_getCanceled()) {
                    finish()
                }
            }
        }
    }

    /**
     * 키패드 초기 세팅 함수
     */
    private fun initTransKeyPad() {
        val intent = getIntentParam(TransKeyActivity.mTK_TYPE_KEYPAD_NUMBER,
            TransKeyActivity.mTK_TYPE_TEXT_PASSWORD_EX,
            "",
            7,
            "",
            5,
            50)
        if (intent != null) {
            mTransKeyCtrl!!.init(intent,
                findViewById<View>(R.id.keypadContainer) as FrameLayout,
                findViewById<View>(R.id.resident_relInput).findViewById<View>(R.id.editText) as EditText,
                findViewById<View>(R.id.resident_relInput).findViewById<View>(R.id.keyscroll) as HorizontalScrollView,
                findViewById<View>(R.id.resident_relInput).findViewById<View>(R.id.keylayout) as LinearLayout,
                findViewById<View>(R.id.resident_relInput).findViewById<View>(R.id.clearall) as ImageButton,
                findViewById<View>(R.id.keypadBallon) as RelativeLayout,
                null)
            //mTransKeyCtrl.setReArrangeKeapad(true);
            mTransKeyCtrl!!.setTransKeyListener(this)
            mTransKeyCtrl!!.setTransKeyListenerEx(this)
            mTransKeyCtrl!!.setTransKeyListenerCallback(this)
        }

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
     * @return      Intent
     */
    private fun getIntentParam(keyPadType: Int, textType: Int, hint: String, maxLength: Int, maxLengthMessage: String, line3Padding: Int, reduceRate: Int): Intent {
        val newIntent = Intent(this.applicationContext, TransKeyActivity::class.java)

        //val int mTK_TYPE_KEYPAD_NUMBER				//숫자전용
        //val int mTK_TYPE_KEYPAD_QWERTY_LOWER		    //소문자 쿼티
        //val int mTK_TYPE_KEYPAD_QWERTY_UPPER		    //대문자 쿼티
        //val int mTK_TYPE_KEYPAD_ABCD_LOWER			//소문자 순열자판
        //val int mTK_TYPE_KEYPAD_ABCD_UPPER			//대문자 순열자판
        //val int mTK_TYPE_KEYPAD_SYMBOL				//심벌자판
        newIntent.putExtra(TransKeyActivity.mTK_PARAM_KEYPAD_TYPE, keyPadType)

        //키보드가 입력되는 형태
        //TransKeyActivity.mTK_TYPE_TEXT_IMAGE - 보안 텍스트 입력
        //TransKeyActivity.mTK_TYPE_TEXT_PASSWORD - 패스워드 입력
        //TransKeyActivity.mTK_TYPE_TEXT_PASSWORD_EX - 마지막 글자 보여주는 패스워드 입력
        newIntent.putExtra(TransKeyActivity.mTK_PARAM_INPUT_TYPE, textType)

        //키패드입력화면의 입력 라벨
        //newIntent.putExtra(TransKeyActivity.mTK_PARAM_NAME_LABEL, label);
        newIntent.putExtra(TransKeyActivity.mTK_PARAM_DISABLE_SPACE, false)
        newIntent.putExtra(TransKeyActivity.mTK_PARAM_HIDE_TIMER_DELAY, 5)

        //최대 입력값 설정
        newIntent.putExtra(TransKeyActivity.mTK_PARAM_INPUT_MAXLENGTH, maxLength)
        newIntent.putExtra(TransKeyActivity.mTK_PARAM_USE_ATM_MODE, false)
        //인터페이스 - maxLength시에 메시지 박스 보여주기. 기본은 메시지 안나옴.
        //newIntent.putExtra(TransKeyActivity.mTK_PARAM_MAX_LENGTH_MESSAGE, maxLengthMessage);

        // --> SERVER 연동
        newIntent.putExtra(TransKeyActivity.mTK_PARAM_CRYPT_TYPE, TransKeyActivity.mTK_TYPE_CRYPT_SERVER)
        newIntent.putExtra(TransKeyActivity.mTK_PARAM_SECURE_KEY, EnvConfig.mTransServerKey)

        // --> Local
        //newIntent.putExtra(TransKeyActivity.mTK_PARAM_CRYPT_TYPE, TransKeyActivity.mTK_TYPE_CRYPT_LOCAL);

        //해당 Hint 메시지를 보여준다.
        newIntent.putExtra(TransKeyActivity.mTK_PARAM_SET_HINT, hint)

        //Hint 테스트 사이즈를 설정한다.(단위 dip, 0이면 디폴트 크기로 보여준다.)
        newIntent.putExtra(TransKeyActivity.mTK_PARAM_SET_HINT_TEXT_SIZE, 0)

        //커서를 보여준다.
        newIntent.putExtra(TransKeyActivity.mTK_PARAM_SHOW_CURSOR, true)
        newIntent.putExtra(TransKeyActivity.mTK_PARAM_USE_CUSTOM_CURSOR, true)

        //에디트 박스안의 글자 크기를 조절한다.
        newIntent.putExtra(TransKeyActivity.mTK_PARAM_EDIT_CHAR_REDUCE_RATE, reduceRate)

        //심볼 변환 버튼을 비활성화 시킨다.
        newIntent.putExtra(TransKeyActivity.mTK_PARAM_DISABLE_SYMBOL, false)
        newIntent.putExtra(TransKeyActivity.mTK_PARAM_PREVENT_CAPTURE, true)

        //심볼 변환 버튼을 비활성화 시킬 경우 팝업 메시지를 설정한다.
        //newIntent.putExtra(TransKeyActivity.mTK_PARAM_DISABLE_SYMBOL_MESSAGE, "심볼키는 사용할 수 없습니다.");

        //////////////////////////////////////////////////////////////////////////////
        //인터페이스 - line3 padding 값 설정 가능 인자. 기본은 0 값이면 아무 설정 안했을 시 원래 transkey에서 제공하던 값을 제공한다..
        //newIntent.putExtra(TransKeyActivity.mTK_PARAM_KEYPAD_MARGIN, line3Padding);
        //newIntent.putExtra(TransKeyActivity.mTK_PARAM_KEYPAD_LEFT_RIGHT_MARGIN, 0);
        newIntent.putExtra(TransKeyActivity.mTK_PARAM_KEYPAD_HIGHEST_TOP_MARGIN, 2)
        newIntent.putExtra(TransKeyActivity.mTK_PARAM_USE_TALKBACK, true)
        newIntent.putExtra(TransKeyActivity.mTK_PARAM_SUPPORT_ACCESSIBILITY_SPEAK_PASSWORD, true)
        newIntent.putExtra(TransKeyActivity.mTK_PARAM_USE_SHIFT_OPTION, true)
        newIntent.putExtra(TransKeyActivity.mTK_PARAM_USE_CLEAR_BUTTON, false)
        newIntent.putExtra(TransKeyActivity.mTK_PARAM_USE_KEYPAD_ANIMATION, true)
        newIntent.putExtra(TransKeyActivity.mTK_PARAM_SAMEKEY_ENCRYPT_ENABLE, true)

//		newIntent.putExtra(TransKeyActivity.mTK_PARAM_LANGUAGE, TransKeyActivity.mTK_Language_English);
        val language = TransKeyActivity.mTK_Language_Korean
        newIntent.putExtra(TransKeyActivity.mTK_PARAM_LANGUAGE, language)
        return newIntent
    }

    /**
     * 보안키패드 숨김
     */
    private fun finishTranskeypad(p_flagFinishOption: Boolean) {
        if (mTransKeyCtrl!!.isShown) {
            mTransKeyCtrl!!.finishTransKey(p_flagFinishOption)
            if (CommonFunction.CF_checkAccessibilityTurnOn(this)) {
                relResident_2.announceForAccessibility("보안키패드 숨김.")
            }
        }
    }

    /**
     * 사용자 입력값 검사 함수
     * @return      boolean
     */
    private fun checkUserInput(): Boolean {
        var flagOk = true
        if (editName.text.toString().trim().isEmpty()) {
            flagOk = false
            showCustomDialog(resources.getString(R.string.dlg_no_input_name), editName)
        }
        else if (editResident_1.text.toString().length < maxLengthOfResident_1 || mTransKeyCtrl!!.inputLength < maxLengthOfresident_2) {
            flagOk = false
            showCustomDialog(resources.getString(R.string.dlg_no_input_resident), editResident_1)
        }
        else if (!checkView.CF_isChecked()) {
            flagOk = false
            showCustomDialog(resources.getString(R.string.dlg_need_agree_smart_service), checkView)
        }
        else if (switchAuthSign.CF_getCheckState() < 0) {
            flagOk = false
            CommonFunction.CF_showCustomAlertDilaog(this, "전자서명을 선택해주십시오.", resources.getString(R.string.btn_ok))
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
        if (editResident_1.hasFocus()) {
            CommonFunction.CF_closeVirtualKeyboard(this, editResident_1.windowToken)
            editResident_1.clearFocus()
            editResident_1.isCursorVisible = false
            flagWaitTimeMilli = 120L
        }
        Handler().postDelayed({
            relResident_2.requestFocus()
            mTransKeyCtrl!!.showKeypad(TransKeyActivity.mTK_TYPE_KEYPAD_NUMBER)
            if (CommonFunction.CF_checkAccessibilityTurnOn(this@IUFC34M00)) {
                relResident_2.sendAccessibilityEvent(AccessibilityEvent.TYPE_VIEW_FOCUSED)
                relResident_2.announceForAccessibility("보안키패드 표시됨")
            }
        }, flagWaitTimeMilli)
    }

    // #############################################################################################
    //  보안키패드 이벤트
    // #############################################################################################
    override fun cancel(intent: Intent) {}
    override fun done(p_intent: Intent) {
        // -- 접근성 On : 다음 Text 이용동의 라벨로 접근성 포커스 이동
        if (CommonFunction.CF_checkAccessibilityTurnOn(this@IUFC34M00)) {
            clearAllFocus()
            val txtLabelAgree = findViewById<TextView>(R.id.textLabelSmartService)
            txtLabelAgree.isFocusableInTouchMode = true
            txtLabelAgree.requestFocus()
            txtLabelAgree.isFocusableInTouchMode = false
        }
    }

    override fun input(i: Int) {
        if (mTransKeyCtrl!!.inputLength >= maxLengthOfresident_2) {
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
        LogPrinter.CF_debug("!----------------------------------------------------------")
        LogPrinter.CF_debug("!-- IUFC34M00.handleMessage() --")
        LogPrinter.CF_debug("!----------------------------------------------------------")
        if (!isDestroyed) {
            // 프로그레스 다이얼로그 dismiss
            CF_dismissProgressDialog()
            when (p_message.what) {
                HANDLERJOB_SUBMIT -> try {
                    httpRes_getUserInfo(JSONObject(p_message.obj as String))
                } catch (e: JSONException) {
                    LogPrinter.CF_line()
                    LogPrinter.CF_debug(resources.getString(R.string.log_json_exception))
                }
                HANDLERJOB_ERROR_SUBMIT -> CommonFunction.CF_showCustomAlertDilaog(this, p_message.obj as String, resources.getString(R.string.btn_ok))
                else -> {
                }
            }
        }
    }

    /**
     * 고객정보조회 요청
     */
    private fun httpReq_getUserInfo() {
        LogPrinter.CF_debug("!-----------------------------------------------------------")
        LogPrinter.CF_debug("!-- IUFC34M00.httpReq_getUserInfo() --고객정보조회 요청")
        LogPrinter.CF_debug("!-----------------------------------------------------------")
        CF_showProgressDialog()

        val bundle = (applicationContext as CustomApplication).bundleBioInfo
        if (bundle != null) {
            val builder = Uri.Builder()
            builder.appendQueryParameter("name"        , editName.text.toString().trim()) // 이름
            builder.appendQueryParameter("rn_1"        , editResident_1.text.toString().trim()) // 주민등록번호 앞자리
            builder.appendQueryParameter("encoded_rn_2", mTransKeyCtrl!!.cipherDataEx) // 주민등록번호 뒷자리
            builder.appendQueryParameter("device_id"   , bundle.getString(Fido2Constant.KEY_DEVICE_ID))
            builder.appendQueryParameter("serviceCode" , Fido2Constant.SVC_CODE) // 서비스코드(005고정)
            HttpConnections.sendPostData(
                EnvConfig.host_url + EnvConfig.URL_CUSTOMER_INFO,
                builder.build().encodedQuery,
                handler,
                HANDLERJOB_SUBMIT,
                HANDLERJOB_ERROR_SUBMIT)
        } else {
            CF_dismissProgressDialog()
        }
    }

    /**
     * 고객정보조회 응답
     * @param p_jsonObject      JSONObject
     */
    @Throws(JSONException::class)
    private fun httpRes_getUserInfo(p_jsonObject: JSONObject) {
        LogPrinter.CF_debug("!-----------------------------------------------------------")
        LogPrinter.CF_debug("!-- IUFC34M00.httpRes_getUserInfo() --고객정보조회 응답")
        LogPrinter.CF_debug("!-----------------------------------------------------------")
        LogPrinter.CF_debug("!---- p_jsonObject : $p_jsonObject")
        /*
            정상 - p_jsonObject : {"data":{"s_csno":"147647863","s_name":"노지민","s_rgtn_yn":"Y"},"errCode":"","debugMsg":""}
         */
        val jsonKey_errorCode = "errCode"
        val jsonKey_debugMsg  = "debugMsg"
        val jsonKey_data      = "data"
        val jsonKey_s_csno    = "s_csno" // 고객번호
        val jsonKey_s_name    = "s_name" // 고객명
        //val jsonKey_s_rgtn_yn = "s_rgtn_yn" // 핀번호 기등록여부
        val tmp_errorCode: String

        if (p_jsonObject.has(jsonKey_errorCode)) {
            tmp_errorCode = p_jsonObject.getString(jsonKey_errorCode)

            // -- (전문에러)
            if (!TextUtils.isEmpty(tmp_errorCode)) {
                showCustomDialog(p_jsonObject.getString(jsonKey_debugMsg), RESULT_CANCELED)
            }
            else if (p_jsonObject.has(jsonKey_data)) {
                val tmp_jsonData = p_jsonObject.getJSONObject(jsonKey_data)
                mResCsno = tmp_jsonData.getString(jsonKey_s_csno) // 고객번호
                mResName = tmp_jsonData.getString(jsonKey_s_name) // 고객명

                val dialog = CustomDialog(this)
                dialog.show()
                dialog.CF_setBackKeyMode(CustomDialog.BackMode.NOTUSE)
                dialog.CF_setTextContent("우체국 예금/보험 가입 고객입니다. 휴대폰 본인인증으로 이동합니다.")
                dialog.CF_setSingleButtonText(resources.getString(R.string.btn_ok))
                dialog.setOnDismissListener { // -- 휴대폰 본인인증
                    startIUPC90M00_P()
                }
            } else {
                showCustomDialog(resources.getString(R.string.dlg_error_server_2), RESULT_CANCELED)
            }
        } else {
            showCustomDialog(resources.getString(R.string.dlg_error_server_1), RESULT_CANCELED)
        }
    }

    /**
     * 휴대폰 본인인증 Activity 호출 함수
     */
    private fun startIUPC90M00_P() {
        LogPrinter.CF_debug("!----------------------------------------------------------")
        LogPrinter.CF_debug("!-- IUFC34M00.startIUPC90M00_P() --휴대폰 본인인증 Activity 호출")
        LogPrinter.CF_debug("!----------------------------------------------------------")

        val intent = Intent(this, IUPC90M00_P::class.java)
        intent.putExtra(EnvConstant.KEY_INTENT_AUTH_MODE, AuthMode.SIGN_APP)
        intent.putExtra(EnvConstant.KEY_INTENT_AUTH_NAME, mResName) // 휴대폰인증시 이름

        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
        startActivityForResult(intent, EnvConfig.REQUESTCODE_ACTIVITY_IUPC90M00)
    }

    /**
     * 공동인증 전자서명 Activity 호출 함수
     */
    private fun startIUPC80M00_cert() {
        LogPrinter.CF_debug("!----------------------------------------------------------")
        LogPrinter.CF_debug("!-- IUFC34M00.startIUPC80M00() --공동인증 전자서명 Activity 호출")
        LogPrinter.CF_debug("!----------------------------------------------------------")

        val intent = Intent(this, IUPC80M00::class.java)
        intent.putExtra(EnvConstant.KEY_INTENT_AUTH_CSNO, mResCsno)

        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
        startActivityForResult(intent, EnvConfig.REQUESTCODE_ACTIVITY_IUPC80M00)
    }

    /**
     * 카카오페이 전자서명 Activity 호출 함수
     */
    fun startIUCOK0M00_kakao() {
        LogPrinter.CF_debug("!----------------------------------------------------------")
        LogPrinter.CF_debug("!-- IUFC34M00.IUPC95M20() --카카오페이인증 Activity 호출")
        LogPrinter.CF_debug("!----------------------------------------------------------")

        val intent = Intent(this, IUPC95M20::class.java)
        intent.putExtra(EnvConstant.KEY_INTENT_AUTH_MODE, AuthMode.SIGN_APP)
        intent.putExtra(EnvConstant.KEY_INTENT_AUTH_CSNO, mResCsno)
        intent.putExtra(EnvConstant.KEY_INTENT_AUTH_NAME, mResName)

        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
        startActivityForResult(intent, EnvConfig.REQUESTCODE_ACTIVITY_IUCOK0M00)
    }

    /**
     * 간편인증 등록요청 Activity 호출 함수
     */
    private fun startIUFC10M00_fidoReg() {
        LogPrinter.CF_debug("!----------------------------------------------------------")
        LogPrinter.CF_debug("!-- IUFC34M00.startIUFC10M00_fidoReg() --간편인증 등록요청 Activity 호출")
        LogPrinter.CF_debug("!----------------------------------------------------------")

        val intent = Intent(this, IUFC10M00::class.java)
        intent.putExtra(EnvConstant.KEY_INTENT_AUTH_TECH_CODE, Fido2Constant.AUTH_TECH_PIN) // 인증기술코드
        intent.putExtra(EnvConstant.KEY_INTENT_AUTH_CSNO, mResCsno) // 고객번호

        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
        startActivityForResult(intent, EnvConfig.REQUESTCODE_ACTIVITY_IUFC10M00)
    }

    /**
     * 간편인증 등록/해지 완료 Activity 호출 함수
     */
    private fun startIUPC39M00() {
        LogPrinter.CF_debug("!----------------------------------------------------------")
        LogPrinter.CF_debug("!-- IUFC34M00.startIUPC39M00() --간편인증 등록/해지 완료 Activity 호출")
        LogPrinter.CF_debug("!----------------------------------------------------------")

        val intent = Intent(this, IUFC39M00::class.java)
        intent.putExtra(EnvConstant.KEY_INTENT_AUTH_TECH_CODE, Fido2Constant.AUTH_TECH_PIN) // 인증기술코드
        intent.putExtra("authResultDvsn", 0) // 요청구분(0:등록/변경, 1:해지)

        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
        startActivityForResult(intent, EnvConfig.REQUESTCODE_ACTIVITY_IUPC39M00)
    }
}