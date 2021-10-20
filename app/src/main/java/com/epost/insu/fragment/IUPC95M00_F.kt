package com.epost.insu.fragment

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.text.InputFilter
import android.text.InputFilter.LengthFilter
import android.text.InputType
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.View.OnTouchListener
import android.view.ViewGroup
import android.view.accessibility.AccessibilityEvent
import android.widget.*
import com.epost.insu.EnvConfig
import com.epost.insu.R
import com.epost.insu.activity.auth.IUPC95M00_P
import com.epost.insu.common.CommonFunction
import com.epost.insu.common.LogPrinter
import com.epost.insu.common.Regex
import com.epost.insu.control.CustomCheckView
import com.epost.insu.control.SwitchingControl
import com.epost.insu.event.OnChangedCheckedStateEventListener
import com.epost.insu.service.AES256Util
import java.io.UnsupportedEncodingException
import java.security.InvalidAlgorithmParameterException
import java.security.InvalidKeyException
import java.security.NoSuchAlgorithmException
import java.util.*
import java.util.regex.Pattern
import javax.crypto.BadPaddingException
import javax.crypto.IllegalBlockSizeException
import javax.crypto.NoSuchPaddingException

/**
 * 공통 > 카카오페이인증 로그인 >> 카카오페이인증 정보 입력 및 요청
 * @since     :
 * @version   : 1.0
 * @author    : YJH
 * <pre>
 * ======================================================================
 * 1.4.4    YJH_20201210    최초등록, 카카오페이인증 로그인
 * 1.5.2    NJM_20210317    카카오페이 인증 로그인시 에러 해결
 * (구글콘솔에러 : com.epost.insu.activity.auth.IUPC95M00_P.CF_requestAuth --java.lang.NullPointerException)
 * =======================================================================
 * copyright : (주)프로니스정보기술 | http://www.phronis.co.kr
</pre> *
 */
class IUPC95M00_F constructor() : Fragment_Default(), OnTouchListener {
    private var mActivity: IUPC95M00_P? = null
    private var edtName: EditText? = null // 이름

    private var edtBirth: EditText? = null // 생년월일

    private var txtMobile: TextView? = null
    private var textLabelSex: TextView? = null
    private var switchingSex: SwitchingControl? = null // 성별

    private var checkViewAll: CustomCheckView? = null
    private var aes256Util: AES256Util? = null
    private var btnKakaopayAuth: Button? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setInit()
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mActivity = getActivity() as IUPC95M00_P?
    }

    @SuppressLint("InflateParams")
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.iupc95m00_f, null)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        setUIControl()
        restoreData(savedInstanceState)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        saveData(outState)
    }

    /**
     * 초기 세팅 함수
     */
    private fun setInit() {}

    /**
     * UI 생성 및 세팅 함수
     */
    private fun setUIControl() {
        // -- 사용자 정보 입력 UI
        setUIOfUserInfoInput()
        // -- 휴대폰 본인인증 약관동의 UI
        setUIOfAgree()

        // -- 카카오페이인증
        btnKakaopayAuth = getView()!!.findViewById(R.id.btnFill)
        btnKakaopayAuth?.setText(getResources().getString(R.string.btn_kakaopay_auth))
        btnKakaopayAuth?.setOnClickListener(object : View.OnClickListener {
            override fun onClick(v: View) {
                if (checkUserInput()) {
                    try {
                        mActivity!!.httpReq_kakaoAuth1()
                    } catch (e: Exception) {
                        e.message
                        CommonFunction.CF_showCustomDialogException(mActivity,  /*e.getMessage()*/"")
                    }
                }
            }
        })
    }

    /**
     * 데이터 백업
     * @param p_bundle Bundle
     * @since 2019-05-08 : @deprecated 실명번호 -> 생년월일/성별로 인한 주석처리
     */
    private fun saveData(p_bundle: Bundle) {
        p_bundle.putString("name", edtName!!.getText().toString().trim({ it <= ' ' }))
        p_bundle.putString("birthDay", edtBirth!!.getText().toString().trim({ it <= ' ' }))
        p_bundle.putInt("checkState", switchingSex!!.CF_getCheckState())
        p_bundle.putBoolean("flagCheckAll", checkViewAll!!.CF_isChecked())
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

            // 생년월일 복원
            edtBirth!!.setText(p_bundle.getString("birthDay"))
            if (TextUtils.isEmpty(edtBirth!!.getText().toString())) {
                edtBirth!!.setContentDescription("선택창, " + getResources().getString(R.string.hint_birth) + ", 선택하시려면 두번 누르세요.")
            } else {
                edtBirth!!.setContentDescription("선택창, " + edtBirth!!.getText() + " 선택됨, 선택하시려면 두번 누르세요.")
            }

            // 성별 복원
            switchingSex!!.CF_setCheck(p_bundle.getInt("checkState"))
            if (switchingSex!!.CF_getCheckState() > 0) {
                textLabelSex!!.setContentDescription(getResources().getString(R.string.desc_sex_man))
            } else if (switchingSex!!.CF_getCheckState() == 0) {
                textLabelSex!!.setContentDescription(getResources().getString(R.string.desc_sex_woman))
            } else {
                textLabelSex!!.setContentDescription(getResources().getString(R.string.desc_sex_no))
            }

            // 체크 상태 복원
            val tmp_flagCheckAll: Boolean = p_bundle.getBoolean("flagCheckAll")
            if (tmp_flagCheckAll) {
                checkViewAll!!.CF_setCheck(tmp_flagCheckAll, false)
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
        val tmp_linName: LinearLayout = getView()!!.findViewById(R.id.labelEditName) // 이름
        val tmp_linBirth: LinearLayout = getView()!!.findViewById(R.id.labelTextBirth) // 생년월일

        // -- 라벨 세팅
        val tmp_labelName: TextView = tmp_linName.findViewById(R.id.label)
        val tmp_labelBirth: TextView = tmp_linBirth.findViewById(R.id.label)

        // -- 텍스트 세팅
        tmp_labelName.setText(getResources().getString(R.string.label_name))
        tmp_labelBirth.setText(getResources().getString(R.string.label_birth))

        // -- 이름
        edtName = tmp_linName.findViewById(R.id.edit)

        // -- 생년월일 세팅
        edtBirth = tmp_linBirth.findViewById(R.id.edit)
        edtBirth?.setFilters(arrayOf<InputFilter>(LengthFilter(8))) // 글자수 제한 (8자)
        edtBirth?.setInputType(InputType.TYPE_CLASS_NUMBER) // 숫자 타입
        edtBirth?.setHint(R.string.hint_birth_input)

        // -- 성별 셋팅
        textLabelSex = getView()!!.findViewById(R.id.labelTextSex)
        textLabelSex?.setContentDescription(getResources().getString(R.string.desc_sex_no))
        switchingSex = getView()!!.findViewById(R.id.switchingSex)
        switchingSex?.CF_setText(getResources().getString(R.string.btn_man), getResources().getString(R.string.btn_woman))
        switchingSex?.CF_setUniformWidth()
        switchingSex?.CE_setOnChangedCheckedStateEventListener(object : OnChangedCheckedStateEventListener {
            override fun onCheck(p_flagCheck: Boolean) {
                if (p_flagCheck) {
                    textLabelSex?.setContentDescription(getResources().getString(R.string.desc_sex_man))
                } else {
                    textLabelSex?.setContentDescription(getResources().getString(R.string.desc_sex_woman))
                }
                // finishTranskeypad(false);
            }
        })

        // -- 휴대폰번호 세팅
        // 2019-04-02 휴대폰 번호 : 현재 단말 휴대폰번호로 셋팅
        var phoneNo: String? = mActivity!!.CF_getPhoneNumber()
        val regEx: String = "(\\d{3})(\\d{3,4})(\\d{4})"
        if (phoneNo != null && Pattern.matches(regEx, phoneNo)) {
            phoneNo = phoneNo.replace(regEx.toRegex(), "$1-$2-$3")
        }

        // 휴대폰 번호 확인 불가에 따른 에러 다이얼로그
        if (phoneNo == null || ("" == phoneNo)) {
            mActivity!!.CF_cancelRequest()
        }
        txtMobile = getView()!!.findViewById(R.id.txtMobile)
        txtMobile?.setText(phoneNo)
    }

    /**
     * 약관 동의 UI 세팅 함수
     */
    private fun setUIOfAgree() {
        // -- 체크 View 세팅
        checkViewAll = getView()!!.findViewById(R.id.checkAll)
        checkViewAll?.CF_setContentsDesc("약관에 전체동의 체크해제 버튼", "약관에 전체동의 체크 버튼")
        checkViewAll?.CE_setOnChangedCheckedStateEventListener(object : OnChangedCheckedStateEventListener {
            override fun onCheck(p_flagIsCheck: Boolean) {
                if (p_flagIsCheck) {
                    // 스크롤 이동
                    scrollBottom()
                }
            }
        })
    }

    /**
     * 스크롤 Bottom 이동 함수
     */
    private fun scrollBottom() {
        val tmp_scroll: ScrollView = getView()!!.findViewById(R.id.scrollView)
        tmp_scroll.scrollTo(0, tmp_scroll.findViewById<View>(R.id.linContents).getHeight())

        // -----------------------------------------------------------------------------------------
        // 시각 접근성 On 상태시에만 다음 버튼에 Accessible 포커스 이동
        // -----------------------------------------------------------------------------------------
        if (CommonFunction.CF_checkAccessibilityTurnOn(Objects.requireNonNull(getActivity()))) {
            Handler().postDelayed(object : Runnable {
                override fun run() {
                    val tmp_btnAuth: Button = getView()!!.findViewById(R.id.btnFill)
                    tmp_btnAuth.sendAccessibilityEvent(AccessibilityEvent.TYPE_VIEW_FOCUSED)
                }
            }, 150)
        }
    }

    /**
     * 사용자 입력 값 검사 함수
     * @return      boolean
     */
    private fun checkUserInput(): Boolean {
        var tmp_flagOk: Boolean = true
        if (TextUtils.isEmpty(edtName!!.getText().toString().trim({ it <= ' ' }))) {
            tmp_flagOk = false
            showCustomDialog(getResources().getString(R.string.dlg_no_input_name), (edtName)!!)
        } else if (TextUtils.isEmpty(edtBirth!!.getText().toString().trim({ it <= ' ' }))) {
            tmp_flagOk = false
            showCustomDialog(getResources().getString(R.string.dlg_empty_birth), (edtBirth)!!)
        } else if (!Regex.CF_Macher8Birth(edtBirth!!.getText().toString().trim({ it <= ' ' }))) {
            tmp_flagOk = false
            showCustomDialog(getResources().getString(R.string.dlg_regex_birth), (edtBirth)!!)
        } else if (switchingSex!!.CF_getCheckState() < 0) {
            tmp_flagOk = false
            showCustomDialog(getResources().getString(R.string.dlg_no_select_sex), (textLabelSex)!!)
        } else if (!checkViewAll!!.CF_isChecked()) {
            tmp_flagOk = false
            showCustomDialog(getResources().getString(R.string.dlg_no_check_agree), (checkViewAll)!!)
        }
        return tmp_flagOk
    }

    /**
     * 이름 반환
     * @return      String
     */
    fun CF_getName(): String {
        return edtName!!.getText().toString().trim({ it <= ' ' })
    }

    /**
     * 생년월일 반환
     * @return  String
     */
    fun CF_getBirth(): String {
        return edtBirth!!.getText().toString().replace(".", "")
    }

    /**
     * 성별 반환
     * @return  String
     */
    fun CF_getSex(): String {
        return if (switchingSex!!.CF_getCheckState() > 0) "1" else "0"
    }

    /**
     * 휴대폰번호(하이픈 제거)
     * 2019-04-02 휴대폰번호 임시 평문 값
     * @return encodedMobile String
     */
    fun CF_getEncodedMobile(): String? {
        // -- 암호화
        try {
            if (EnvConfig.SYMMETRIC_KEY.length != EnvConfig.SYMMETRIC_KEY_SIZE) {
                return ""
            }
            aes256Util = AES256Util(EnvConfig.SYMMETRIC_KEY)
        } catch (e: UnsupportedEncodingException) {
            e.message
        }
        var encodedMobile: String? = null
        try {
            val originMobile: String = txtMobile!!.text.toString().trim().replace("[^0-9]".toRegex(), "")
            encodedMobile = aes256Util!!.aesEncode(originMobile)
        } catch (e: NoSuchAlgorithmException) {
            e.message
        } catch (e: NoSuchPaddingException) {
            e.message
        } catch (e: InvalidKeyException) {
            e.message
        } catch (e: InvalidAlgorithmParameterException) {
            e.message
        } catch (e: IllegalBlockSizeException) {
            e.message
        } catch (e: BadPaddingException) {
            e.message
        } catch (e: UnsupportedEncodingException) {
            e.message
        }
        return encodedMobile
    }

    /**
     * 터치이벤트 핸들러
     * @param v         View
     * @param event     MotionEvent
     * @return          boolean
     * @since 2019-05-08 키보드보안관련 주석처리
     */
    override fun onTouch(v: View, event: MotionEvent): Boolean {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            // 이름
            if (v.getId() == R.id.edit) {
                edtName!!.setCursorVisible(true)
                return false
            } else if (v.getId() == R.id.switchingSex) {
                return false
            } else if (v.getId() == R.id.checkAll) {
                return false
            }
        }
        return false
    }
}