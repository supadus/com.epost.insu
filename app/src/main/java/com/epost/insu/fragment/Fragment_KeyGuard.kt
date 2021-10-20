package com.epost.insu.fragment

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.text.TextUtils
import android.util.AttributeSet
import android.view.*
import android.view.View.OnTouchListener
import android.widget.*
import com.epost.insu.EnvConfig
import com.epost.insu.R
import com.epost.insu.activity.auth.IUPC80M00
import com.epost.insu.common.*
import com.epost.insu.dialog.CustomDialog
import com.epost.insu.event.OnFragmentKeyGuardEventListener
import com.softsecurity.transkey.*

/**
 * 보안 키패드 입력 Fragment
 * @since     :
 * @version   : 1.2
 * @author    : LSH
 * @see
 * <pre>
 * 주민등록번호 뒷자리, 인증서 비밀번호를 입력받는다.<br></br>
 * 공통 사용 <br></br>
 * - [IUCOC0M00] 공동인증 로그인<br></br>
 * - [IUCOC0M00_Web] 공동인증 로그인(웹 요청)<br></br>
 * - [IUPC80M00] 본인인증 <br></br>
 * - [IUPC80M10_Web] 본인인증<br></br>
 * ======================================================================
 * 0.0.0    LSH_20171124    최초 등록
 * 1.6.2    NJM_20210802    [2021년 대우 취약점] 2차본 : 부적절한 예외 처리
 * 1.6.3    NJM_20210826    [캡쳐방지 예외] 캡쳐방지 예외추가 및 로그표기일때만 예외처리
 * 1.6.3    NJM_20211007    [API30 대응] targetApi 29 -> 30 변경에 따른 코틀린 오류 수정
 * =======================================================================
 * copyright : (주)프로니스정보기술 | http://www.phronis.co.kr
 * </pre>
 */
class Fragment_KeyGuard : Fragment_Default(), ITransKeyActionListener, ITransKeyActionListenerEx, ITransKeyCallbackListener, OnTouchListener {
    // Fragment_KeyGuard 이벤트 리스너
    private var listener: OnFragmentKeyGuardEventListener? = null

    private var relInput: RelativeLayout? = null // 보안키패드 입력 레이아웃
    private var mTransKeyCtrl: TransKeyCtrl? = null // 보안키패드 컨트롤
    private var isViewCtrlKeypad = false
    private var keyMode = KeyMode.text

    /**
     * 보안키패드 모드 enum
     */
    enum class KeyMode {
        text,  // 영문문자열
        number // 숫자
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setInit()
    }

    @SuppressLint("InflateParams")
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_keyguard, null)
    }

    override fun onInflate(context: Context, attrs: AttributeSet, savedInstanceState: Bundle?) {
        super.onInflate(context, attrs, savedInstanceState)
        val a = context.obtainStyledAttributes(attrs, R.styleable.fragmentKeyGuard)
        val tmp_keyMode = a.getInt(R.styleable.fragmentKeyGuard_keyMode, 0)
        if (tmp_keyMode < KeyMode.values().size) {
            keyMode = KeyMode.values()[tmp_keyMode]
        }
        a.recycle()
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        setUIControl()
        initTransKeyPad()
        CF_showTransKeyPad()
    }

    override fun onDestroy() {
        super.onDestroy()
        mTransKeyCtrl!!.finishTransKey(false)
        mTransKeyCtrl = null
    }

    override fun onTouch(v: View, event: MotionEvent): Boolean {
        if (event.action == MotionEvent.ACTION_DOWN) {
            if (v.id == R.id.editText || v.id == R.id.keyscroll || v.id == R.id.keylayout) {
                CF_showTransKeyPad()
                return true
            }
        }
        return false
    }

    /**
     * 초기 세팅
     */
    private fun setInit() {
        try {
            mTransKeyCtrl = TransKeyCtrl(activity)
        } catch (e: NullPointerException) {
            LogPrinter.CF_line()
            LogPrinter.CF_debug(resources.getString(R.string.log_fail_create_trankey))
            e.message
        } catch (e: Exception) {
            LogPrinter.CF_line()
            LogPrinter.CF_debug(resources.getString(R.string.log_fail_create_trankey))
            e.message
        }
    }

    /**
     * UI 생성 및 세팅 함수
     */
    private fun setUIControl() {
        val txtLabel = view!!.findViewById<View>(R.id.fragment_keyguard_textLabel) as TextView
        if (keyMode == KeyMode.text) {
            txtLabel.text = resources.getString(R.string.guide_input_pw)
        } else if (keyMode == KeyMode.number) {
            txtLabel.text = resources.getString(R.string.guide_input_pin)
        }
        val editText = view!!.findViewById<View>(R.id.fragment_keyGuard_relInputSub).findViewById<View>(R.id.editText) as EditText
        editText.setOnTouchListener(this)
        editText.importantForAccessibility = View.IMPORTANT_FOR_ACCESSIBILITY_NO
        relInput = view!!.findViewById<View>(R.id.fragment_keyGuard_relInputSub) as RelativeLayout
        relInput!!.contentDescription = "입력창, " + resources.getString(R.string.desc_edt_password) + ", 편집하려면 두 번 누르세요."
        val keyLayout = view!!.findViewById<View>(R.id.fragment_keyGuard_relInputSub).findViewById<View>(R.id.keylayout) as ViewGroup
        keyLayout.setOnTouchListener(this)
        val keyScroll = view!!.findViewById<View>(R.id.fragment_keyGuard_relInputSub).findViewById<View>(R.id.keyscroll) as ViewGroup
        keyScroll.setOnTouchListener(this)
        val btnOk = view!!.findViewById<View>(R.id.btnFill) as Button
        btnOk.text = resources.getString(R.string.btn_ok)
        btnOk.setOnClickListener { mTransKeyCtrl!!.done() }
    }

    /**
     * 키패드 초기 세팅 함수
     */
    private fun initTransKeyPad() {
        var intent: Intent? = null
        if (keyMode == KeyMode.text) {
            intent = getIntentParam(TransKeyActivity.mTK_TYPE_KEYPAD_QWERTY_LOWER,
                TransKeyActivity.mTK_TYPE_TEXT_PASSWORD_EX,
                resources.getString(R.string.hint_pw),
                50,  // 25, // 인증서암호최대길이
                "",
                5,
                40)
        } else if (keyMode == KeyMode.number) {
            intent = getIntentParam(TransKeyActivity.mTK_TYPE_KEYPAD_NUMBER,
                TransKeyActivity.mTK_TYPE_TEXT_PASSWORD_EX,
                resources.getString(R.string.hint_pin),
                6,
                "",
                5,
                40)
        }
        if (intent != null) {
            mTransKeyCtrl!!.init(intent,
                view!!.findViewById<View>(R.id.keypadContainer) as FrameLayout,
                view!!.findViewById<View>(R.id.fragment_keyGuard_relInputSub).findViewById<View>(R.id.editText) as EditText,
                view!!.findViewById<View>(R.id.fragment_keyGuard_relInputSub).findViewById<View>(R.id.keyscroll) as HorizontalScrollView,
                view!!.findViewById<View>(R.id.fragment_keyGuard_relInputSub).findViewById<View>(R.id.keylayout) as LinearLayout,
                view!!.findViewById<View>(R.id.fragment_keyGuard_relInputSub).findViewById<View>(R.id.clearall) as ImageButton,
                view!!.findViewById<View>(R.id.keypadBallon) as RelativeLayout,
                null)
            mTransKeyCtrl!!.setReArrangeKeapad(true)
            mTransKeyCtrl!!.setTransKeyListener(this)
            mTransKeyCtrl!!.setTransKeyListenerEx(this)
            mTransKeyCtrl!!.setTransKeyListenerCallback(this)
        }

        // -- 캡쳐 방지 적용 (DRM적용: addFlags / 미적용: clearFlags)
        if (EnvConfig.mFlagShowLog) {
            this.activity?.window?.clearFlags(WindowManager.LayoutParams.FLAG_SECURE)
        } else {
            this.activity?.window?.addFlags(WindowManager.LayoutParams.FLAG_SECURE)
        }
    }

    /**
     * 입력창에 Accessible Focus 세팅
     */
    fun CF_setAccessibleFocusInputBox() {
        Handler().postDelayed({
            clearAllFocus()
            relInput!!.isFocusableInTouchMode = true
            relInput!!.requestFocus()
            relInput!!.isFocusableInTouchMode = false
        }, 500)
    }

    /**
     * 키패드 show(QWERTY_LOWER 자판)<br></br>
     */
    fun CF_showTransKeyPad() {
        if (keyMode == KeyMode.text) {
            showTransKeyPad(TransKeyActivity.mTK_TYPE_KEYPAD_QWERTY_LOWER)
        } else if (keyMode == KeyMode.number) {
            showTransKeyPad(TransKeyActivity.mTK_TYPE_KEYPAD_NUMBER)
        }
    }

    /**
     * 키패드 Show 함수
     * @param p_keyPadType
     */
    private fun showTransKeyPad(p_keyPadType: Int) {
        relInput!!.requestFocus()
        mTransKeyCtrl!!.showKeypad(p_keyPadType)
        isViewCtrlKeypad = true
    }

    private fun getIntentParam(keyPadType: Int, textType: Int, hint: String, maxLength: Int, maxLengthMessage: String, line3Padding: Int, reduceRate: Int): Intent {
        val newIntent = Intent(this.activity!!.applicationContext,
            TransKeyActivity::class.java)

        //public static final int mTK_TYPE_KEYPAD_NUMBER				//숫자전용
        //public static final int mTK_TYPE_KEYPAD_QWERTY_LOWER		    //소문자 쿼티
        //public static final int mTK_TYPE_KEYPAD_QWERTY_UPPER		    //대문자 쿼티
        //public static final int mTK_TYPE_KEYPAD_ABCD_LOWER			//소문자 순열자판
        //public static final int mTK_TYPE_KEYPAD_ABCD_UPPER			//대문자 순열자판
        //public static final int mTK_TYPE_KEYPAD_SYMBOL				//심벌자판
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

        //--> SERVER 연동테스트
        newIntent.putExtra(TransKeyActivity.mTK_PARAM_CRYPT_TYPE, TransKeyActivity.mTK_TYPE_CRYPT_SERVER)
        newIntent.putExtra(TransKeyActivity.mTK_PARAM_SECURE_KEY, EnvConfig.mTransServerKey)

        //<-- SERVER 연동테스트

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
        newIntent.putExtra(TransKeyActivity.mTK_PARAM_USE_KEYPAD_ANIMATION, false)
        newIntent.putExtra(TransKeyActivity.mTK_PARAM_SAMEKEY_ENCRYPT_ENABLE, true)

        //newIntent.putExtra(TransKeyActivity.mTK_PARAM_LANGUAGE, TransKeyActivity.mTK_Language_English);
        val language = TransKeyActivity.mTK_Language_Korean
        newIntent.putExtra(TransKeyActivity.mTK_PARAM_LANGUAGE, language)
        return newIntent
    }

    /**
     * 이벤트 세팅 함수
     * @param p_listener
     */
    fun CE_setOnFragmentKeyGuardEventListener(p_listener: OnFragmentKeyGuardEventListener?) {
        listener = p_listener
    }

    /**
     * 보안 키패드 Show / Hide 상태를 반환한다.<br></br>
     * @return if true, is shown
     */
    fun CF_getFlagIsShwonKeypad(): Boolean {
        return isViewCtrlKeypad
    }

    // #############################################################################################
    //  보안키패드 Func Override
    // #############################################################################################
    override fun cancel(intent: Intent) {
        isViewCtrlKeypad = false
        if (listener != null) {
            listener!!.onCancel()
        }
        mTransKeyCtrl!!.ClearAllData()
    }

    override fun done(p_intent: Intent) {
        isViewCtrlKeypad = false
        if (keyMode == KeyMode.text && mTransKeyCtrl!!.inputLength < 6) {
            // ---------------------------------------------------------------------------------
            //  최소 글자 수 미만 입력 : 다이얼로그 팝업 후 보안 키패드 띄움.
            // ---------------------------------------------------------------------------------
            val customDialog = CustomDialog(this@Fragment_KeyGuard.activity!!)
            customDialog.show()
            customDialog.CF_setBackKeyMode(CustomDialog.BackMode.NOTUSE)
            customDialog.CF_setTextContent(resources.getString(R.string.dlg_input_cert_pw_6))
            customDialog.CF_setSingleButtonText(resources.getString(R.string.btn_ok))
            customDialog.setOnDismissListener {
                CF_showTransKeyPad()
                if (CommonFunction.CF_checkAccessibilityTurnOn(activity)) {
                    CF_setAccessibleFocusInputBox()
                }
            }
        } else {
            if (p_intent != null) {
                val cipher = p_intent.getStringExtra(TransKeyActivity.mTK_PARAM_CIPHER_DATA)
                val secureKey = p_intent.getByteArrayExtra(TransKeyActivity.mTK_PARAM_SECURE_KEY)
                val iRealDataLength = p_intent.getIntExtra(TransKeyActivity.mTK_PARAM_DATA_LENGTH, 0)
                if (iRealDataLength == 0) return
                if (!TextUtils.isEmpty(cipher) && secureKey!!.isNotEmpty()) {

                    // 비대칭키를 사용할 경우 데이터 포맷
                    val encryptedData = p_intent.getStringExtra(TransKeyActivity.mTK_PARAM_RSA_DATA)
                    var plainData: StringBuffer? = null
                    val tkc = TransKeyCipher("SEED")
                    tkc.secureKey = secureKey
                    val pbPlainData = ByteArray(iRealDataLength)
                    if (tkc.getDecryptCipherData(cipher, pbPlainData)) {
                        plainData = StringBuffer(String(pbPlainData))
                        for (i in pbPlainData.indices) pbPlainData[i] = 0x01
                    } else {
                        // 복호화 실패
                        plainData = StringBuffer("plainData decode fail...")
                    }
                    if (listener != null) {
                        listener!!.onDone(plainData.toString())
                    }
                }
            }
            mTransKeyCtrl!!.ClearAllData()
        }
    }

    override fun input(i: Int) {}
    override fun minTextSizeCallback() {}
    override fun maxTextSizeCallback() {}
}