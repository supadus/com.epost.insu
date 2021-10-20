package com.epost.insu.fragment

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.text.*
import android.text.InputFilter.LengthFilter
import android.text.style.ForegroundColorSpan
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.View.OnTouchListener
import android.view.ViewGroup
import android.view.accessibility.AccessibilityEvent
import android.widget.*
import androidx.appcompat.app.AlertDialog
import com.epost.insu.EnvConfig
import com.epost.insu.R
import com.epost.insu.activity.auth.IUPC90M00_P
import com.epost.insu.common.CommonFunction
import com.epost.insu.common.LogPrinter
import com.epost.insu.common.Regex
import com.epost.insu.control.CustomCheckView
import com.epost.insu.control.SwitchingControl
import com.epost.insu.service.AES256Util
import java.io.UnsupportedEncodingException
import java.util.*
import java.util.regex.Pattern

/**
 * @copyright : (주)프로니스정보기술 | http://www.phronis.co.kr
 *
 * @project   : 모바일슈랑스 구축
 * @pakage    : com.epost.insu.activity
 * @fileName  : IUPC90M00_F.java
 *
 * @Title     : 공통 > 휴대폰 본인인증 >> 01. 휴대폰인증번호 요청 (화면 ID : iupc90m00_f)
 * @author    : 양지훈
 * @version   : 1.0
 *
 * @note      : - 휴대폰 인증번호 요청 화면<br></br>
 * ======================================================================
 * 수정 내역
 * 0.0.0    YJH_20181109    최초등록, 휴대폰인증 청구
 * 0.0.0    NJM_20190402    휴대폰번호 단말정보값으로 고정하도록 변경(추후 변경가능하도록 기존 소스 유지)
 * 0.0.0    NJM_20190508    실명번호 입력에서 생년월일(8자) 및 성별 입력으로 변경, 키패드보안관련 주석처리
 * 0.0.0    NJM_20200122    공통 인증유형/청구유형 추가에 따른 로직수정
 * 1.6.2    NJM_20210802    [2021년 대우 취약점] 2차본 : 부적절한 예외 처리
 * =======================================================================
 */
class IUPC90M00_F : Fragment_Default(), OnTouchListener {
    private val subUrl_pdf_1: String = "/prod/p_yak04.pdf" // 본인확인 서비스 이용약관
    private val subUrl_pdf_2: String = "/prod/p_yak02.pdf" // 개인정보 이용 및 제공동의
    private val subUrl_pdf_3: String = "/prod/p_yak03.pdf" // 고유식별정보 처리동의
    private val subUrl_pdf_4: String = "/prod/p_yak04.pdf" // 통신사 본인확인서비스 이용동의

    private var mActivity: IUPC90M00_P? = null

    private var edtName: EditText? = null // 이름
    private var edtBirth: EditText? = null // 생년월일(개발에서만 수정가능 운영에서는 읽기전용)
    private var edtMobile: EditText? = null // 휴대폰번호(개발에서만 수정가능 운영에서는 읽기전용)
    private var textTelecom: TextView? = null
    private var textLabelSex: TextView? = null

    private var switchingSex: SwitchingControl? = null // 성별
    private var checkViewAll: CustomCheckView? = null
    private var arrCheckView: Array<CustomCheckView?>? = null // 동의 체크View Array
    private var onClickSeeDoc: View.OnClickListener? = null // 전문보기 click 리스너

    // 2019-04-02 휴대폰번호 AES 암호화용 추가
    private val SYMMETRIC_KEY_SIZE: Int = 32
    private val symmetricKey: String = "422d3951ef007cc32e2f714ccb6ff8dc"
    private var aes256Util: AES256Util? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mActivity = activity as IUPC90M00_P?
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setInit()
    }

    /**
     * 초기 세팅 함수
     */
    private fun setInit() {
        arrCheckView = arrayOfNulls(4)
        onClickSeeDoc = View.OnClickListener { view ->
            val value: Int = view.tag as Int
            var subUrl = ""
            when (value) {
                0 -> subUrl = subUrl_pdf_1
                1 -> subUrl = subUrl_pdf_2
                2 -> subUrl = subUrl_pdf_3
                3 -> subUrl = subUrl_pdf_4
            }
            if (!TextUtils.isEmpty(subUrl)) {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(EnvConfig.host_url + subUrl))
                startActivity(intent)
            }
        }
    }

    @SuppressLint("InflateParams")
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.iupc90m00_f, null)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        setUIControl()

        // 데이터 복구
        restoreData(savedInstanceState)
    }

    /**
     * UI 생성 및 세팅 함수
     */
    private fun setUIControl() {
        // 사용자 정보 입력 UI
        setUIOfUserInfoInput()

        // 휴대폰 본인인증 약관동의 UI
        setUIOfAgree()

        // 휴대폰인증
        val btnAuth: Button = getView()!!.findViewById(R.id.btnFill)
        btnAuth.text = resources.getString(R.string.btn_mobile_auth)
        btnAuth.setOnClickListener {
            if (checkUserInput()) {
                mActivity!!.httpReq_mobile_auth1()
            }
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        saveData(outState)
    }

    /**
     * 데이터 백업
     * @param p_bundle Bundle
     * @since 2019-05-08 : @deprecated 실명번호 -> 생년월일/성별로 인한 주석처리
     */
    private fun saveData(p_bundle: Bundle) {
        p_bundle.putString("name", edtName!!.text.toString().trim())
        //        p_bundle.putString("rrno1"          , editResident_1.getText().toString().trim());
        p_bundle.putString("telecomName", textTelecom!!.text.toString().trim())
        p_bundle.putString("telecomCode", textTelecom!!.tag as String?)
        p_bundle.putString("birthDay", edtBirth!!.text.toString().trim())
        p_bundle.putInt("checkState", switchingSex!!.CF_getCheckState())
        p_bundle.putBoolean("flagCheckAll", checkViewAll!!.CF_isChecked())
        p_bundle.putBoolean("flagCheck_1", arrCheckView?.get(0)!!.CF_isChecked())
        p_bundle.putBoolean("flagCheck_2", arrCheckView?.get(1)!!.CF_isChecked())
        p_bundle.putBoolean("flagCheck_3", arrCheckView?.get(2)!!.CF_isChecked())
        p_bundle.putBoolean("flagCheck_4", arrCheckView?.get(3)!!.CF_isChecked())
    }

    /**
     * 데이터 복구
     * @param p_bundle Bundle
     * @since 2019-05-08 : @deprecated 실명번호 -> 생년월일/성별로 인한 주석처리
     */
    private fun restoreData(p_bundle: Bundle?) {
        if (p_bundle != null) {
            // 이름 복원
            edtName!!.setText(p_bundle.getString("name"))

            // 주민번호 앞자리 복원
//            editResident_1.setText(p_bundle.getString("rnno"));

            // 통신사 복원
            textTelecom!!.text = p_bundle.getString("telecomName")
            textTelecom!!.tag = p_bundle.getString("telecomCode")
            if (TextUtils.isEmpty(textTelecom!!.text.toString())) {
                textTelecom!!.contentDescription = "선택창, " + resources.getString(R.string.hint_telecom) + ", 선택하시려면 두번 누르세요."
            } else {
                textTelecom!!.contentDescription = "선택창, " + textTelecom!!.text + " 선택됨, 선택하시려면 두번 누르세요."
            }

            // 생년월일 복원
            edtBirth!!.setText(p_bundle.getString("birthDay"))
            if (TextUtils.isEmpty(edtBirth!!.text.toString())) {
                edtBirth!!.contentDescription = "선택창, " + resources.getString(R.string.hint_birth) + ", 선택하시려면 두번 누르세요."
            } else {
                edtBirth!!.contentDescription = "선택창, " + edtBirth!!.text + " 선택됨, 선택하시려면 두번 누르세요."
            }

            // 성별 복원
            switchingSex!!.CF_setCheck(p_bundle.getInt("checkState"))
            if (switchingSex!!.CF_getCheckState() > 0) {
                textLabelSex!!.contentDescription = resources.getString(R.string.desc_sex_man)
            } else if (switchingSex!!.CF_getCheckState() == 0) {
                textLabelSex!!.contentDescription = resources.getString(R.string.desc_sex_woman)
            } else {
                textLabelSex!!.contentDescription = resources.getString(R.string.desc_sex_no)
            }

            // 체크 상태 복원
            val tmp_flagCheckAll: Boolean = p_bundle.getBoolean("flagCheckAll")
            val tmp_flagCheck_1: Boolean = p_bundle.getBoolean("flagCheck_1")
            val tmp_flagCheck_2: Boolean = p_bundle.getBoolean("flagCheck_2")
            val tmp_flagCheck_3: Boolean = p_bundle.getBoolean("flagCheck_3")
            val tmp_flagCheck_4: Boolean = p_bundle.getBoolean("flagCheck_4")
            if (tmp_flagCheckAll) {
                checkViewAll!!.CF_setCheck(tmp_flagCheckAll, false)
            }
            if (tmp_flagCheck_1) {
                arrCheckView?.get(0)!!.CF_setCheck(tmp_flagCheck_1, false)
            }
            if (tmp_flagCheck_2) {
                arrCheckView?.get(1)!!.CF_setCheck(tmp_flagCheck_2, false)
            }
            if (tmp_flagCheck_3) {
                arrCheckView?.get(2)!!.CF_setCheck(tmp_flagCheck_3, false)
            }
            if (tmp_flagCheck_4) {
                arrCheckView?.get(3)!!.CF_setCheck(tmp_flagCheck_4, false)
            }
        }
    }

    /**
     * 사용자 정보 입력 UI 세팅 함수<br></br>
     * 이름 / 휴대폰번호 / 통신사 / 생년월일 /성별
     */
    private fun setUIOfUserInfoInput() {
        LogPrinter.CF_debug("!----------------------------------------------------------")
        LogPrinter.CF_debug("!-- IUPC90M00_F.setUIOfUserInfoInput()")
        LogPrinter.CF_debug("!----------------------------------------------------------")
        val tmp_linName: LinearLayout = view!!.findViewById(R.id.labelEditName) // 이름
        val tmp_linTelecom: LinearLayout = view!!.findViewById(R.id.labelTextTelecom) // 통신사
        val tmp_linBirth: LinearLayout = view!!.findViewById(R.id.labelTextBirth) // 생년월일
        val tmp_linMobile: LinearLayout = view!!.findViewById(R.id.labelTextMobile) // 휴대전화

        // -- 라벨 세팅
        val tmp_labelName: TextView = tmp_linName.findViewById(R.id.label)
        val tmp_labelTelecom: TextView = tmp_linTelecom.findViewById(R.id.label)
        val tmp_labelBirth: TextView = tmp_linBirth.findViewById(R.id.label)
        val tmp_labelMobile: TextView = tmp_linMobile.findViewById(R.id.label)

        // -- 텍스트 세팅
        tmp_labelName.text = resources.getString(R.string.label_name)
        tmp_labelTelecom.text = resources.getString(R.string.label_telecom)
        tmp_labelBirth.text = resources.getString(R.string.label_birth)
        tmp_labelMobile.text = getResources().getString(R.string.label_mobile)

        // -- 이름
        edtName = tmp_linName.findViewById(R.id.edit)
        // 저장된 이름이 있으면 세팅(최종 청구시 이름 값이 있음)
        if ("" != mActivity!!.CF_getName()) {
            edtName?.setText(mActivity!!.CF_getName()) // 이름 셋팅
            edtName?.isFocusableInTouchMode = false // 읽기전용으로 변경
        }

        // -- 개발환경시 수정 가능하도록 설정
        if (("devel" == EnvConfig.operation)) {
            edtName?.isFocusableInTouchMode = true // 수정가능
        }

        // deprecated 2019-04-02 휴대폰번호 단말정보값으로 고정으로 인한 사용중지
        /*
        else {
            //edtName.setText(getUser_name_fromSqlite());
            edtName.setOnTouchListener(this);
            edtName.setOnEditorActionListener(new TextView.OnEditorActionListener() {
                @Override
                public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {

                    if(actionId == EditorInfo.IME_ACTION_DONE){
                        showTransKeyPad();
                    }
                    return false;
                }
            });
        }
        */
        //textName = (TextView)tmp_linName.findViewById(R.id.text);
        //textName.setText(getUser_name_fromSqlite());


        // 통신사 세팅
        textTelecom = tmp_linTelecom.findViewById(R.id.text)
        textTelecom?.isClickable = true
        textTelecom?.setHint(R.string.hint_telecom)
        textTelecom?.contentDescription = "선택창, " + resources.getString(R.string.hint_telecom) + ", 선택하시려면 두번 누르세요."
        textTelecom?.tag = ""
        textTelecom?.setBackgroundResource(R.drawable.btn_empty_selector)
        textTelecom?.setOnClickListener { showDlgOfTelecomList() }

        // -- 생년월일 세팅
        edtBirth = tmp_linBirth.findViewById(R.id.edit)
        edtBirth?.filters = arrayOf<InputFilter>(LengthFilter(8)) // 글자수 제한 (8자)
        edtBirth?.inputType = InputType.TYPE_CLASS_NUMBER // 숫자 타입
        edtBirth?.setHint(R.string.hint_birth_input)

        // -- 성별 셋팅
        textLabelSex = view!!.findViewById(R.id.labelTextSex)
        textLabelSex?.contentDescription = resources.getString(R.string.desc_sex_no)
        switchingSex = view!!.findViewById(R.id.switchingSex)
        switchingSex?.CF_setText(getResources().getString(R.string.btn_man), getResources().getString(R.string.btn_woman))
        switchingSex?.CF_setUniformWidth()
        switchingSex?.CE_setOnChangedCheckedStateEventListener { p_flagCheck ->
            if (p_flagCheck) {
                textLabelSex?.contentDescription = resources.getString(R.string.desc_sex_man)
            } else {
                textLabelSex?.contentDescription = resources.getString(R.string.desc_sex_woman)
            }
        }

        // -- 휴대폰번호 세팅
        // 2019-04-02 휴대폰 번호 : 현재 단말 휴대폰번호로 셋팅
        var phoneNo: String? = mActivity!!.CF_getPhoneNumber()
        val regEx = "(\\d{3})(\\d{3,4})(\\d{4})"
        if (phoneNo != null && Pattern.matches(regEx, phoneNo)) {
            phoneNo = phoneNo.replace(regEx.toRegex(), "$1-$2-$3")
        }

        // 휴대폰 번호 확인 불가에 따른 에러 다이얼로그
        if (phoneNo == null || ("" == phoneNo)) {
            mActivity!!.CF_cancelRequest()
        }
        edtMobile = tmp_linMobile.findViewById(R.id.edit)
        edtMobile?.setText(phoneNo)
        edtMobile?.isFocusableInTouchMode = false // 읽기전용으로 변경

        // -- 개발환경시 수정 가능하도록 설정
        if (("devel" == EnvConfig.operation)) {
            edtMobile?.isFocusableInTouchMode = true // 수정가능
        }

        //txtMobile = getView().findViewById(R.id.txtMobile);
        //txtMobile.setText(phoneNo);


        // 2019-04-02 키패드 관련 주석처리
        /*
        edtMobile = (EditText)getView().findViewById(R.id.relKeyInput).findViewById(R.id.editText);
        edtMobile.setOnTouchListener(this);

        relMobile = (RelativeLayout)getView().findViewById(R.id.relKeyInput_sub);
        relMobile.setMotionEventSplittingEnabled(false);
        relMobile.setContentDescription("입력창, 휴대폰번호 "+getResources().getString(R.string.hint_mobile_2)+", 편집하려면 두 번 누르세요.");

        ViewGroup tmp_keyLayout = (ViewGroup)getView().findViewById(R.id.relKeyInput).findViewById(R.id.keylayout);
        tmp_keyLayout.setOnTouchListener(this);

        ViewGroup tmp_keyScroll = (ViewGroup)getView().findViewById(R.id.relKeyInput).findViewById(R.id.keyscroll);
        tmp_keyScroll.setOnTouchListener(this);
        */
    }

    /**
     * 약관 동의 UI 세팅 함수
     */
    private fun setUIOfAgree() {
        val linAgree1: LinearLayout = view!!.findViewById(R.id.linAgree_1)
        val linAgree2: LinearLayout = view!!.findViewById(R.id.linAgree_2)
        val linAgree3: LinearLayout = view!!.findViewById(R.id.linAgree_3)
        val linAgree4: LinearLayout = view!!.findViewById(R.id.linAgree_4)

        // -- 버튼 세팅
        val btn1: LinearLayout = linAgree1.findViewById(R.id.btn_line)
        val btn2: LinearLayout = linAgree2.findViewById(R.id.btn_line)
        val btn3: LinearLayout = linAgree3.findViewById(R.id.btn_line)
        val btn4: LinearLayout = linAgree4.findViewById(R.id.btn_line)
        btn1.tag = 0
        btn2.tag = 1
        btn3.tag = 2
        btn4.tag = 3
        btn1.contentDescription = resources.getString(R.string.desc_label_mobile_agree_1)
        btn2.contentDescription = resources.getString(R.string.desc_label_mobile_agree_2)
        btn3.contentDescription = resources.getString(R.string.desc_label_mobile_agree_3)
        btn4.contentDescription = resources.getString(R.string.desc_label_mobile_agree_4)
        btn1.setOnClickListener(onClickSeeDoc)
        btn2.setOnClickListener(onClickSeeDoc)
        btn3.setOnClickListener(onClickSeeDoc)
        btn4.setOnClickListener(onClickSeeDoc)

        // -- Text 세팅
        val txtAgree1: TextView = linAgree1.findViewById(R.id.text)
        val txtAgree2: TextView = linAgree2.findViewById(R.id.text)
        val txtAgree3: TextView = linAgree3.findViewById(R.id.text)
        val txtAgree4: TextView = linAgree4.findViewById(R.id.text)
        val spannable1: Spannable = SpannableString(resources.getString(R.string.label_mobile_agree_1))
        spannable1.setSpan(ForegroundColorSpan(Color.parseColor("#ff7c29c7")), spannable1.length - 4, spannable1.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        txtAgree1.text = spannable1
        val spannable2: Spannable = SpannableString(resources.getString(R.string.label_mobile_agree_2))
        spannable2.setSpan(ForegroundColorSpan(Color.parseColor("#ff7c29c7")), spannable2.length - 4, spannable2.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        txtAgree2.text = spannable2
        val spannable3: Spannable = SpannableString(resources.getString(R.string.label_mobile_agree_3))
        spannable3.setSpan(ForegroundColorSpan(Color.parseColor("#ff7c29c7")), spannable3.length - 4, spannable3.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        txtAgree3.text = spannable3
        val spannable4: Spannable = SpannableString(resources.getString(R.string.label_mobile_agree_4))
        spannable4.setSpan(ForegroundColorSpan(Color.parseColor("#ff7c29c7")), spannable4.length - 4, spannable4.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        txtAgree4.text = spannable4

        // -- 체크 View 세팅
        checkViewAll = view!!.findViewById(R.id.checkAll)
        checkViewAll?.CF_setContentsDesc("약관에 전체동의 체크해제 버튼", "약관에 전체동의 체크 버튼")
        checkViewAll?.CE_setOnChangedCheckedStateEventListener { p_flagIsCheck ->
            setAllCheck(p_flagIsCheck)
            if (p_flagIsCheck) {
                // 스크롤 이동
                scrollBottom()
            }
        }
        arrCheckView!![0] = linAgree1.findViewById(R.id.checkAgree)
        arrCheckView?.get(0)?.CF_setContentsDesc("본인확인 서비스 이용약관 동의 체크해제 버튼", "본인확인 서비스 이용약관 동의 체크 버튼")
        arrCheckView!![1] = linAgree2.findViewById(R.id.checkAgree)
        arrCheckView?.get(1)?.CF_setContentsDesc("개인정보 이용 및 제공 동의 체크해제 버튼", "개인정보 이용 및 제공 동의 체크 버튼")
        arrCheckView!![2] = linAgree3.findViewById(R.id.checkAgree)
        arrCheckView?.get(2)?.CF_setContentsDesc("고유식별정보 처리 동의 체크해제 버튼", "고유식별정보 처리 동의 체크 버튼")
        arrCheckView!![3] = linAgree4.findViewById(R.id.checkAgree)
        arrCheckView?.get(3)?.CF_setContentsDesc("통신사 본인확인서비스 이용동의 체크해제 버튼", "통신사 본인확인서비스 이용동의 체크 버튼")
        for (i in arrCheckView!!.indices) {
            arrCheckView?.get(i)?.CE_setOnChangedCheckedStateEventListener { p_flagCheck -> setAllCheckViewState(p_flagCheck) }
        }
    }

    /**
     * 전체 CheckView 체크상태 세팅 함수<br></br>
     * 마지막 항목의 check 상태를 param으로 전달받아 모든 항목의 check 상태를 검사하여 그 결과를 토대로 전체 CheckView의 체크 상태를 결정한다.
     * @param p_lastChangedCheckState   boolean
     */
    private fun setAllCheckViewState(p_lastChangedCheckState: Boolean) {
        var flagAllSame = true
        for (i in arrCheckView!!.indices) {
            if (arrCheckView?.get(i)!!.CF_isChecked() != p_lastChangedCheckState) {
                flagAllSame = false
                break
            }
        }
        if (flagAllSame) {
            if (checkViewAll!!.CF_isChecked() != p_lastChangedCheckState) {
                checkViewAll!!.CF_setCheck(p_lastChangedCheckState, true, false)
                if (p_lastChangedCheckState) {
                    scrollBottom()
                }
            }
        } else {
            if (checkViewAll!!.CF_isChecked()) {
                checkViewAll!!.CF_setCheck(false, true, false)
            }
        }
    }

    /**
     * 전체 CheckView를 제외한 모든 항목CheckView 체크 상태 변경 함수
     */
    private fun setAllCheck(p_flagCheck: Boolean) {
        for (i in arrCheckView!!.indices) {
            if (arrCheckView?.get(i)!!.CF_isChecked() != p_flagCheck) {
                arrCheckView?.get(i)!!.CF_setCheck(p_flagCheck, true, false)
            }
        }
    }

    /**
     * 스크롤 Bottom 이동 함수
     */
    private fun scrollBottom() {
        val tmp_scroll: ScrollView = view!!.findViewById(R.id.scrollView)
        tmp_scroll.scrollTo(0, tmp_scroll.findViewById<View>(R.id.linContents).height)

        // -----------------------------------------------------------------------------------------
        // 시각 접근성 On 상태시에만 다음 버튼에 Accessible 포커스 이동
        // -----------------------------------------------------------------------------------------
        if (CommonFunction.CF_checkAccessibilityTurnOn(Objects.requireNonNull(activity))) {
            Handler().postDelayed(object : Runnable {
                override fun run() {
                    val btnAuth: Button = view!!.findViewById(R.id.btnFill)
                    btnAuth.sendAccessibilityEvent(AccessibilityEvent.TYPE_VIEW_FOCUSED)
                }
            }, 150)
        }
    }

    /**
     * 사용자 입력 값 검사 함수
     * @return      boolean
     */
    private fun checkUserInput(): Boolean {
        var flagOk = true
        if (TextUtils.isEmpty(edtName!!.text.toString().trim())) {
            flagOk = false
            showCustomDialog(resources.getString(R.string.dlg_no_input_name), (edtName)!!)
        } else if (TextUtils.isEmpty(textTelecom!!.text.toString().trim())) {
            flagOk = false
            showCustomDialog(resources.getString(R.string.dlg_empty_telecom), (textTelecom)!!)
        } else if (TextUtils.isEmpty(edtBirth!!.text.toString().trim())) {
            flagOk = false
            showCustomDialog(resources.getString(R.string.dlg_empty_birth), (edtBirth)!!)
        } else if (!Regex.CF_Macher8Birth(edtBirth!!.text.toString().trim())) {
            flagOk = false
            showCustomDialog(resources.getString(R.string.dlg_regex_birth), (edtBirth)!!)
        } else if (switchingSex!!.CF_getCheckState() < 0) {
            flagOk = false
            showCustomDialog(resources.getString(R.string.dlg_no_select_sex), (textLabelSex)!!)
        } else if (!arrCheckView?.get(0)!!.CF_isChecked()) {
            flagOk = false
            showCustomDialog(resources.getString(R.string.dlg_agree_mobile_auth_1), (arrCheckView?.get(0))!!)
        } else if (!arrCheckView?.get(1)!!.CF_isChecked()) {
            flagOk = false
            showCustomDialog(resources.getString(R.string.dlg_agree_mobile_auth_2), (arrCheckView?.get(1))!!)
        } else if (!arrCheckView?.get(2)!!.CF_isChecked()) {
            flagOk = false
            showCustomDialog(resources.getString(R.string.dlg_agree_mobile_auth_3), (arrCheckView?.get(2))!!)
        } else if (!arrCheckView?.get(3)!!.CF_isChecked()) {
            flagOk = false
            showCustomDialog(resources.getString(R.string.dlg_agree_mobile_auth_4), (arrCheckView?.get(3))!!)
        }
        return flagOk
    }

    /**
     * 이름 반환
     * @return      String
     */
    fun CF_getName(): String {
        return edtName!!.text.toString().trim()
    }
    //    /**
    //     * 휴대폰번호(하이픈 제거)
    //     * @deprecated 2019-04-02 휴대폰번호 단말정보값으로 고정으로 인한 사용중지
    //     * @return
    //     */
    //    public String CF_getEncodedMobile(){
    //        return mTransKeyCtrl.getCipherDataEx();
    //    }
    /**
     * 휴대폰번호(하이픈 제거)
     * 2019-04-02 휴대폰번호 임시 평문 값
     * @return encodedMobile String
     */
    fun CF_getEncodedMobile2(): String? {
        // -- 암호화
        try {
            if (symmetricKey.length != SYMMETRIC_KEY_SIZE) {
                return ""
            }
            aes256Util = AES256Util(symmetricKey)
        } catch (e: UnsupportedEncodingException) {
            e.message
        }
        var encodedMobile: String? = null
        try {
            val originMobile: String = edtMobile!!.text.toString().trim().replace("[^0-9]".toRegex(), "")
            encodedMobile = aes256Util!!.aesEncode(originMobile)
        } catch (e: NullPointerException) {
            e.message
        } catch (e: Exception) {
            e.message
        }
        return encodedMobile
    }

    /**
     * 통신사 코드
     * @return  String
     */
    fun CF_getMBTCode(): String {
        return textTelecom!!.tag as String
    }

    /**
     * 생년월일 반환
     * @return  String
     */
    fun CF_getBirth(): String {
        return edtBirth!!.text.toString().replace(".", "")
    }

    /**
     * 성별 반환
     * @return  String
     */
    fun CF_getSex(): String {
        return if (switchingSex!!.CF_getCheckState() > 0) "1" else "0"
    }
    // #############################################################################################
    //  보안키패드
    // #############################################################################################
    //    /**
    //     * 키패드 초기 세팅 함수
    //     * @deprecated 2019-04-02 휴대폰번호 단말정보값으로 고정으로 인한 사용중지
    //     */
    //
    //    private void initTransKeyPad() {
    //        Intent tmp_intent = getIntentParam( TransKeyActivity.mTK_TYPE_KEYPAD_NUMBER,
    //                                            TransKeyActivity.mTK_TYPE_TEXT_IMAGE,
    //                                            getResources().getString(R.string.hint_mobile_2),
    //                                            maxLengthOfMobile,
    //                                            "",
    //                                            5,
    //                                            60);
    //
    //
    //        if (tmp_intent != null) {
    //            mTransKeyCtrl.init(tmp_intent,
    //                    (FrameLayout) getView().findViewById(R.id.keypadContainer),
    //                    (EditText) (getView().findViewById(R.id.relKeyInput)).findViewById(R.id.editText),
    //                    (HorizontalScrollView) (getView().findViewById(R.id.relKeyInput)).findViewById(R.id.keyscroll),
    //                    (LinearLayout) (getView().findViewById(R.id.relKeyInput)).findViewById(R.id.keylayout),
    //                    (ImageButton) (getView().findViewById(R.id.relKeyInput)).findViewById(R.id.clearall),
    //                    (RelativeLayout) getView().findViewById(R.id.keypadBallon),
    //                    null);
    //            //mTransKeyCtrl.setReArrangeKeapad(true);
    //            mTransKeyCtrl.setTransKeyListener(this);
    //            mTransKeyCtrl.setTransKeyListenerEx(this);
    //            mTransKeyCtrl.setTransKeyListenerCallback(this);
    //        }
    //    }
    //    /**
    //     * @deprecated 2019-05-08 실명번호 -> 생년월일/성별로 인한 사용중지
    //     */
    //
    //    private void initTransKeyPad2() {
    //        Intent tmp_intent = getIntentParam(
    //                TransKeyActivity.mTK_TYPE_KEYPAD_NUMBER,
    //                //TransKeyActivity.mTK_TYPE_TEXT_IMAGE,
    //                TransKeyActivity.mTK_TYPE_TEXT_PASSWORD_EX,
    //                "",
    //                maxLengthOfresident_2,
    //                "",
    //                5,
    //                60);
    //
    //      //  tmp_intent.putExtra(TransKeyActivity.mTK_PARAM_HIDE_TIMER_DELAY,1);
    //        if (tmp_intent != null) {
    //            mTransKeyCtrlRes.init(tmp_intent,
    //                    (FrameLayout) getView().findViewById(R.id.keypadContainer),
    //                    (EditText) (getView().findViewById(R.id.resident_relInput)).findViewById(R.id.editText2),
    //                    (HorizontalScrollView) (getView().findViewById(R.id.resident_relInput)).findViewById(R.id.keyscroll2),
    //                    (LinearLayout) (getView().findViewById(R.id.resident_relInput)).findViewById(R.id.keylayout2),
    //                    (ImageButton) (getView().findViewById(R.id.resident_relInput)).findViewById(R.id.clearall2),
    //                    (RelativeLayout) getView().findViewById(R.id.keypadBallon),
    //                    null);
    //            //mTransKeyCtrlRes.setReArrangeKeapad(true);
    //            mTransKeyCtrlRes.setTransKeyListener(this);
    //            mTransKeyCtrlRes.setTransKeyListenerEx(this);
    //            mTransKeyCtrlRes.setTransKeyListenerCallback(this);
    //        }
    //    }
    //    /**
    //     * 보안키패드 설정 Intent 반환 함수
    //     *
    //     * @param keyPadType       키패드 타입
    //     * @param textType         보안 텍스트 타입
    //     * @param hint             입력 힌트
    //     * @param maxLength        입력 최대길이
    //     * @param maxLengthMessage 최대길이 도달시 보일 메시지
    //     * @param line3Padding     라인패딩
    //     * @param reduceRate       크기 조절 배율
    //     * @deprecated  2019-05-08 실명번호 -> 생년월일/성별로 인한 사용중지
    //     * @return
    //     */
    //
    //    private Intent getIntentParam(int keyPadType, int textType, String hint,
    //                                  int maxLength, String maxLengthMessage, int line3Padding, int reduceRate) {
    //        Intent newIntent = new Intent(this.getActivity().getApplicationContext(),
    //                TransKeyActivity.class);
    //
    //        //public static final int mTK_TYPE_KEYPAD_NUMBER				//숫자전용
    //        //public static final int mTK_TYPE_KEYPAD_QWERTY_LOWER		    //소문자 쿼티
    //        //public static final int mTK_TYPE_KEYPAD_QWERTY_UPPER		    //대문자 쿼티
    //        //public static final int mTK_TYPE_KEYPAD_ABCD_LOWER			//소문자 순열자판
    //        //public static final int mTK_TYPE_KEYPAD_ABCD_UPPER			//대문자 순열자판
    //        //public static final int mTK_TYPE_KEYPAD_SYMBOL				//심벌자판
    //
    //        newIntent.putExtra(TransKeyActivity.mTK_PARAM_KEYPAD_TYPE, keyPadType);
    //
    //        //키보드가 입력되는 형태
    //        //TransKeyActivity.mTK_TYPE_TEXT_IMAGE - 보안 텍스트 입력
    //        //TransKeyActivity.mTK_TYPE_TEXT_PASSWORD - 패스워드 입력
    //        //TransKeyActivity.mTK_TYPE_TEXT_PASSWORD_EX - 마지막 글자 보여주는 패스워드 입력
    //        newIntent.putExtra(TransKeyActivity.mTK_PARAM_INPUT_TYPE, textType);
    //
    //        //키패드입력화면의 입력 라벨
    //        //newIntent.putExtra(TransKeyActivity.mTK_PARAM_NAME_LABEL, label);
    //        newIntent.putExtra(TransKeyActivity.mTK_PARAM_DISABLE_SPACE, false);
    //
    //        //newIntent.putExtra(TransKeyActivity.mTK_PARAM_HIDE_TIMER_DELAY, 5);
    //
    //        //최대 입력값 설정
    //        newIntent.putExtra(TransKeyActivity.mTK_PARAM_INPUT_MAXLENGTH, maxLength);
    //        newIntent.putExtra(TransKeyActivity.mTK_PARAM_USE_ATM_MODE, false);
    //        //인터페이스 - maxLength시에 메시지 박스 보여주기. 기본은 메시지 안나옴.
    //        //newIntent.putExtra(TransKeyActivity.mTK_PARAM_MAX_LENGTH_MESSAGE, maxLengthMessage);
    //
    //        // --> SERVER 연동
    //        newIntent.putExtra(TransKeyActivity.mTK_PARAM_CRYPT_TYPE, TransKeyActivity.mTK_TYPE_CRYPT_SERVER);
    //        newIntent.putExtra(TransKeyActivity.mTK_PARAM_SECURE_KEY, EnvConfig.mTransServerKey);
    //
    //        // --> Local
    //        //newIntent.putExtra(TransKeyActivity.mTK_PARAM_CRYPT_TYPE, TransKeyActivity.mTK_TYPE_CRYPT_LOCAL);
    //
    //        //해당 Hint 메시지를 보여준다.
    //        newIntent.putExtra(TransKeyActivity.mTK_PARAM_SET_HINT, hint);
    //
    //        //Hint 테스트 사이즈를 설정한다.(단위 dip, 0이면 디폴트 크기로 보여준다.)
    //        newIntent.putExtra(TransKeyActivity.mTK_PARAM_SET_HINT_TEXT_SIZE, 0);
    //
    //        //커서를 보여준다.
    //        newIntent.putExtra(TransKeyActivity.mTK_PARAM_SHOW_CURSOR, true);
    //        newIntent.putExtra(TransKeyActivity.mTK_PARAM_USE_CUSTOM_CURSOR, true);
    //
    //        //에디트 박스안의 글자 크기를 조절한다.
    //        newIntent.putExtra(TransKeyActivity.mTK_PARAM_EDIT_CHAR_REDUCE_RATE, reduceRate);
    //
    //        //심볼 변환 버튼을 비활성화 시킨다.
    //        newIntent.putExtra(TransKeyActivity.mTK_PARAM_DISABLE_SYMBOL, false);
    //        newIntent.putExtra(TransKeyActivity.mTK_PARAM_PREVENT_CAPTURE, true);
    //
    //        //심볼 변환 버튼을 비활성화 시킬 경우 팝업 메시지를 설정한다.
    //        //newIntent.putExtra(TransKeyActivity.mTK_PARAM_DISABLE_SYMBOL_MESSAGE, "심볼키는 사용할 수 없습니다.");
    //
    //        //////////////////////////////////////////////////////////////////////////////
    //        //인터페이스 - line3 padding 값 설정 가능 인자. 기본은 0 값이면 아무 설정 안했을 시 원래 transkey에서 제공하던 값을 제공한다..
    //        //newIntent.putExtra(TransKeyActivity.mTK_PARAM_KEYPAD_MARGIN, line3Padding);
    //        //newIntent.putExtra(TransKeyActivity.mTK_PARAM_KEYPAD_LEFT_RIGHT_MARGIN, 0);
    //        newIntent.putExtra(TransKeyActivity.mTK_PARAM_KEYPAD_HIGHEST_TOP_MARGIN, 2);
    //
    //        newIntent.putExtra(TransKeyActivity.mTK_PARAM_USE_TALKBACK, true);
    //        newIntent.putExtra(TransKeyActivity.mTK_PARAM_SUPPORT_ACCESSIBILITY_SPEAK_PASSWORD, true);
    //
    //        newIntent.putExtra(TransKeyActivity.mTK_PARAM_USE_SHIFT_OPTION, true);
    //        newIntent.putExtra(TransKeyActivity.mTK_PARAM_USE_CLEAR_BUTTON, false);
    //        newIntent.putExtra(TransKeyActivity.mTK_PARAM_USE_KEYPAD_ANIMATION, true);
    //
    //        newIntent.putExtra(TransKeyActivity.mTK_PARAM_SAMEKEY_ENCRYPT_ENABLE, true);
    //        newIntent.putExtra(TransKeyActivity.mTK_PARAM_LANGUAGE, TransKeyActivity.mTK_Language_Korean);
    //
    //        return newIntent;
    //    }
    //    /**
    //     * 키패드 Show 함수
    //     * @since 2019-04-02
    //     * @deprecated 휴대폰번호 단말정보값으로 고정으로 인한 사용중지
    //     */
    //    private void showTransKeyPad() {
    //
    //        finishTranskeypad(false);
    //
    //        long tmp_flagWaitTimeMilli = 0L;
    //
    //        if(edtName.hasFocus()){
    //            CommonFunction.CF_closeVirtualKeyboard(getActivity(), edtName.getWindowToken());
    //            edtName.clearFocus();
    //            edtName.setCursorVisible(false);
    //            tmp_flagWaitTimeMilli = 120L;
    //        }
    //
    //        // @since 2019-05-08 : @deprecated 실명번호 -> 생년월일/성별로 인한 사용중지
    //        if(editResident_1.hasFocus()){
    //            CommonFunction.CF_closeVirtualKeyboard(getActivity(), editResident_1.getWindowToken());
    //            editResident_1.clearFocus();
    //            editResident_1.setCursorVisible(false);
    //            tmp_flagWaitTimeMilli = 120L;
    //        }
    //        new Handler().postDelayed(new Runnable() {
    //            @Override
    //            public void run() {
    //                relMobile.requestFocus();
    //                mTransKeyCtrl.showKeypad(TransKeyActivity.mTK_TYPE_KEYPAD_NUMBER);
    //
    //                if(CommonFunction.CF_checkAccessibilityTurnOn(IUPC90M00_F.this.getActivity())) {
    //                    relMobile.sendAccessibilityEvent(AccessibilityEvent.TYPE_VIEW_FOCUSED);
    //                    relMobile.announceForAccessibility("보안키패드 표시됨");
    //                }
    //            }
    //        },tmp_flagWaitTimeMilli);
    //    }
    //    /**
    //     * 키패드 Show 함수
    //     * @since 2019-05-08
    //     * @deprecated 2019-05-08 실명번호 -> 생년월일/성별로 인한 사용중지
    //     */
    //    private void showTransKeyPad2() {
    //
    //        finishTranskeypad(false);
    //
    //        long tmp_flagWaitTimeMilli = 0L;
    //
    //        if (edtName.hasFocus()) {
    //            CommonFunction.CF_closeVirtualKeyboard(getActivity(), edtName.getWindowToken());
    //            edtName.clearFocus();
    //            edtName.setCursorVisible(false);
    //            tmp_flagWaitTimeMilli = 120L;
    //        }
    //
    //        if (editResident_1.hasFocus()) {
    //            CommonFunction.CF_closeVirtualKeyboard(getActivity(), editResident_1.getWindowToken());
    //            editResident_1.clearFocus();
    //            editResident_1.setCursorVisible(false);
    //            tmp_flagWaitTimeMilli = 120L;
    //        }
    //
    //        new Handler().postDelayed(new Runnable() {
    //            @Override
    //            public void run() {
    //                relResident_2.requestFocus();
    //                mTransKeyCtrlRes.showKeypad(TransKeyActivity.mTK_TYPE_KEYPAD_NUMBER);
    //
    //                if (CommonFunction.CF_checkAccessibilityTurnOn(IUPC90M00_F.this.getActivity())) {
    //                    relResident_2.sendAccessibilityEvent(AccessibilityEvent.TYPE_VIEW_FOCUSED);
    //                    relResident_2.announceForAccessibility("보안키패드 표시됨");
    //                }
    //            }
    //        }, tmp_flagWaitTimeMilli);
    //    }
    //    /**
    //     * 보안 키패드 show/hide 유무 반환
    //     * @return
    //     * @deprecated 2019-05-08 실명번호 -> 생년월일/성별로 인한 사용중지
    //     */
    //
    //    public boolean CF_isShownTransKeyPad(){
    //        boolean bRet = false;
    //        if(mTransKeyCtrl.isShown() || mTransKeyCtrlRes.isShown()){
    //            bRet = true;
    //        }
    //        //return mTransKeyCtrl.isShown();
    //        return bRet;
    //    }
    //    /**
    //     * 보안키패드 close
    //     * 왜 두번 호출하나?
    //     * @deprecated 2019-05-08 실명번호 -> 생년월일/성별로 인한 사용중지
    //     */
    //
    //    public void CF_closeTransKeyPad(){
    //        finishTranskeypad(false);
    //        finishTranskeypad(false);
    //    }
    //    /**
    //     * 보안키패드 숨김
    //     * @since 2019-04-02 : 휴대폰번호 단말정보값으로 고정으로 인한 사용중지
    //     * @since 2019-05-08 : 실명번호 -> 생년월일/성별로 인한 사용중지
    //     * @deprecated 2019-05-08 실명번호 -> 생년월일/성별로 인한 사용중지
    //     */
    //    private void finishTranskeypad(boolean p_flagFinishOption){
    //
    //        // @since 2019-04-02 : @deprecated 휴대폰번호 단말정보값으로 고정으로 인한 사용중지
    //        if(mTransKeyCtrl.isShown()){
    //            mTransKeyCtrl.finishTransKey(p_flagFinishOption);
    //            // -------------------------------------------------------------------------------------
    //            //  접근성 announce
    //            // -------------------------------------------------------------------------------------
    //            if(CommonFunction.CF_checkAccessibilityTurnOn(getActivity())){
    //                relMobile.announceForAccessibility("보안키패드 숨김.");
    //            }
    //        }
    //
    //        // @since 2019-05-08 : @deprecated 실명번호 -> 생년월일/성별로 인한 사용중지
    //        if(mTransKeyCtrlRes.isShown()) {
    //            mTransKeyCtrlRes.finishTransKey(p_flagFinishOption);
    //            // -------------------------------------------------------------------------------------
    //            //  접근성 announce
    //            // -------------------------------------------------------------------------------------
    //            if (CommonFunction.CF_checkAccessibilityTurnOn(getActivity())) {
    //                relResident_2.announceForAccessibility("보안키패드 숨김.");
    //            }
    //        }
    //    }

    /**
     * 터치이벤트 핸들러
     * @param view         View
     * @param event     MotionEvent
     * @return          boolean
     * @since 2019-05-08 키보드보안관련 주석처리
     */
    override fun onTouch(view: View, event: MotionEvent): Boolean {
        if (event.action == MotionEvent.ACTION_DOWN) {

            // 이름
            if (view.id == R.id.edit) {
//                finishTranskeypad(false);
                edtName!!.isCursorVisible = true
                return false

                // 성별
            } else if (view.id == R.id.switchingSex) {
//                finishTranskeypad(false);
                //switchingSex
                return false

                // 전체선택
            } else if (view.id == R.id.checkAll) {
//                finishTranskeypad(false);
                //checkViewAll
                return false
            }
        }
        return false
    }
    //    /**
    //     *
    //     * @param intent
    //     * @since 2019-05-08
    //     * @deprecated 실명번호 -> 생년월일/성별로 인한 주석처리
    //     */
    //
    //    @Override
    //    public void cancel(Intent intent) {
    //    }
    //    /**
    //     *
    //     * @param intent
    //     * @since 2019-05-08
    //     * @deprecated 실명번호 -> 생년월일/성별로 인한 주석처리
    //     */
    //    @Override
    //    public void done(Intent intent) {
    //        // -----------------------------------------------------------------------------------------
    //        //  접근성 포커스 이동
    //        // -----------------------------------------------------------------------------------------
    //        if(CommonFunction.CF_checkAccessibilityTurnOn(IUPC90M00_F.this.getActivity())){
    //            clearAllFocus();
    //            LinearLayout tmp_linTelecom = (LinearLayout)getView().findViewById(R.id.labelTextTelecom);         // 통신사
    //            TextView tmp_labelTelecom = (TextView)tmp_linTelecom.findViewById(R.id.label);
    //            tmp_labelTelecom.setFocusableInTouchMode(true);
    //            tmp_labelTelecom.requestFocus();
    //            tmp_labelTelecom.setFocusableInTouchMode(false);
    //        }
    //    }
    //    /**
    //     *
    //     * @since 2019-05-08
    //     * @deprecated 실명번호 -> 생년월일/성별로 인한 주석처리
    //     */
    //    @Override
    //    public void input(int i) {
    //        // since 2019-04-02 : @deprecated 휴대폰번호 단말정보값으로 고정으로 인한 사용중지
    //        if(mTransKeyCtrl.isShown() && mTransKeyCtrl.getInputLength() >= maxLengthOfMobile) {
    //            finishTranskeypad(true);
    //        }
    //
    //        // since 2019-05-08 : @deprecated 실명번호 -> 생년월일/성별로 인한 사용중지
    //        if(mTransKeyCtrlRes.isShown() && mTransKeyCtrlRes.getInputLength() >= maxLengthOfresident_2) {
    //            finishTranskeypad(true);
    //        }
    //    }
    //    /**
    //     * @since 2019-05-08
    //     * @deprecated 실명번호 -> 생년월일/성별로 인한 주석처리
    //     */
    //    @Override
    //    public void minTextSizeCallback() {
    //    }
    //    /**
    //     * @since 2019-05-08
    //     * @deprecated 실명번호 -> 생년월일/성별로 인한 주석처리
    //     */
    //    @Override
    //    public void maxTextSizeCallback() {
    //    }
    // #############################################################################################
    //  Dioalog
    // #############################################################################################
    /**
     * 통신사 선택 다이얼로그 show 함수
     */
    private fun showDlgOfTelecomList() {
        val builder: AlertDialog.Builder = AlertDialog.Builder((Objects.requireNonNull(getActivity()))!!)
        val arrTelecoms: Array<String> = resources.getStringArray(R.array.telecom)
        val arrTelecomCodes: Array<String> = resources.getStringArray(R.array.telectom_code)
        builder.setItems(arrTelecoms) { dialog, which ->
            textTelecom!!.text = arrTelecoms[which]
            textTelecom!!.tag = arrTelecomCodes[which]
            textTelecom!!.contentDescription = "선택창, " + textTelecom!!.text + " 선택됨, 선택하시려면 두번 누르세요."
        }
        if (CommonFunction.CF_checkAccessibilityTurnOn(activity)) {
            builder.setOnDismissListener {
                clearAllFocus()
                textTelecom!!.isFocusableInTouchMode = true
                textTelecom!!.requestFocus()
                textTelecom!!.isFocusableInTouchMode = false
            }
        }
        builder.show()
    }

    //    /**
    //     * 생년월일 선택 다이얼로그 show 함수<br/>
    //     * 선택한 날짜가 있을 경우 DatePickerDialog에 선택한 날짜를 세팅하고 없으면 오늘 날짜를 세팅한다.
    //     * @since 2019-05-08 : 다이어로그 스타일 추가
    //     * @since 2019-05-08 : openYearView() 함수 호출안함
    //     * deprecated 2019-05-08 생년월일 직접입력으로 사용안함
    //     */
    //    private void showDlgOfDatePicker(){
    //
    //        Calendar tmp_calendar = Calendar.getInstance();
    //
    //        int tmp_year = tmp_calendar.get(Calendar.YEAR);
    //        int tmp_month = tmp_calendar.get(Calendar.MONTH);
    //        int tmp_dayOfMonth = tmp_calendar.get(Calendar.DAY_OF_MONTH);
    //
    //        if(TextUtils.isEmpty(edtBirth.getText().toString()) == false){
    //            String[] tmp_birth = edtBirth.getText().toString().split("\\.");
    //            if(tmp_birth.length == 3) {
    //                tmp_year = Integer.parseInt(tmp_birth[0]);
    //                tmp_month = Integer.parseInt(tmp_birth[1]) - 1;
    //                tmp_dayOfMonth = Integer.parseInt(tmp_birth[2]);
    //            }
    //        }
    //
    //        // @since 2019-05-08 : 다이어로그 스타일 추가
    //        DatePickerDialog tmp_dlg = new DatePickerDialog(
    //                                            getActivity(),
    //                                            android.app.AlertDialog.THEME_HOLO_LIGHT,
    //                                            //android.R.style.Theme_Material_Light_Dialog_Alert,
    //                                            new DatePickerDialog.OnDateSetListener() {
    //                                                    @Override
    //                                                    public void onDateSet(DatePicker view, final int year, final int month, final int dayOfMonth) {
    //                                                        String tmp_dateTime = ""+year+"."+String.format(Locale.getDefault(),"%02d",month+1)+"."+String.format(Locale.getDefault(),"%02d", dayOfMonth);
    //                                                        edtBirth.setText(tmp_dateTime);
    //                                                        edtBirth.setContentDescription("선택창, "+tmp_dateTime+" 선택됨, 선택하시려면 두번 누르세요.");
    //                                                    }
    //                                                },
    //                                            tmp_year,
    //                                            tmp_month,
    //                                            tmp_dayOfMonth);
    //
    //        if(CommonFunction.CF_checkAccessibilityTurnOn(getActivity())) {
    //            tmp_dlg.setOnDismissListener(new DialogInterface.OnDismissListener() {
    //                @Override
    //                public void onDismiss(DialogInterface dialog) {
    //                    clearAllFocus();
    //                    edtBirth.setFocusableInTouchMode(true);
    //                    edtBirth.requestFocus();
    //                    edtBirth.setFocusableInTouchMode(false);
    //                }
    //            });
    //        }
    //        tmp_dlg.show();
    //
    //        // 머터리얼 스타일 DatePicker는 사용자 UX 경험이 없어 연도 변경에 어려움이 있다.
    //        // 머터리얼 스타일 DatePicker는 바로 년도 선택이 나오게 만든다.
    //        // openYearView(tmp_dlg.getDatePicker());
    //    }
    //    /**
    //     * 날짜선택 DatePicker의 년도 선택 View Show 함수<br/>
    //     * 내부 함수를 찾아 강제로 변경하는 것으로 문제가 발생할 수 있다.
    //     * @param p_datePicker DatePicker
    //     * deprecated 2019-05-08 HOLO 스타일 변경으로인한 사용안함
    //     */
    //    private void openYearView(DatePicker p_datePicker){
    //
    //        try {
    //            Field tmp_delegateField = p_datePicker.getClass().getDeclaredField("mDelegate");
    //            tmp_delegateField.setAccessible(true);
    //            Object delegate = tmp_delegateField.get(p_datePicker);
    //            Method tmp_currentViewMethod = delegate.getClass().getDeclaredMethod("setCurrentView", int.class);
    //            tmp_currentViewMethod.setAccessible(true);
    //            tmp_currentViewMethod.invoke(delegate,1);
    //        } catch (NoSuchFieldException e) {
    //            LogPrinter.CF_line();
    //            LogPrinter.CF_debug(getResources().getString(R.string.log_no_such_field));
    //        } catch (NoSuchMethodException e) {
    //            LogPrinter.CF_line();
    //            LogPrinter.CF_debug(getResources().getString(R.string.log_no_such_func));
    //        } catch (IllegalAccessException e) {
    //            LogPrinter.CF_line();
    //            LogPrinter.CF_debug(getResources().getString(R.string.log_no_permission));
    //        } catch (InvocationTargetException e) {
    //            LogPrinter.CF_line();
    //            LogPrinter.CF_debug(getResources().getString(R.string.log_invoke_target));
    //        }
    //    }
}