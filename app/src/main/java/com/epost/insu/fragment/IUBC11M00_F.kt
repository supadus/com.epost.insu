package com.epost.insu.fragment

import android.annotation.SuppressLint
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.accessibility.AccessibilityEvent
import android.webkit.WebView
import android.widget.Button
import android.widget.TextView
import androidx.core.widget.NestedScrollView
import com.epost.insu.R
import com.epost.insu.common.CommonFunction
import com.epost.insu.control.CustomCheckView
import com.epost.insu.data.Data_IUII10M02_F
import com.epost.insu.dialog.CustomDialog

/**
 * @copyright : 우정사업정보센터
 *
 * @project   : 모바일슈랑스 구축
 * @pakage   : com.epost.insu.fragment
 * @fileName  : IUBC11M00_F.java
 *
 * @Title     : 스마트보험금청구 > 개인정보처리동의 (화면 ID : IUBC11M00)
 * @author    : 이경민
 * @created   : 2018-06-09
 * @version   : 1.0
 *
 * @note      : <u>스마트보험금청구 > 개인정보처리동의 (화면 ID : IUBC11M00)</u><br></br>
 * 보험금지급청구 2단계 :: 개인정보처리동의 화면<br></br>
 * 요청에 의하여 Scroll 안에 WebView를 넣어 Scroll 이동에 제한이 있음
 * 접근성 이유로 WebView importantForAccessibility 옵션 값 조절이 필요함. (현재 화면에 보이는 경우 yes, 보이지 않는 경우 no)
 * : webView가 화면에 보이지 않아도 음성지원에 읽히는 문제가 있음.
 * ======================================================================
 * 수정 내역
 * NO      날짜          작업자       내용
 * 01      2018-06-09    이경민       최초 등록
 * =======================================================================
 */
class IUBC11M00_F : IUBC10M00_FD() {
    private val html_path_1 = "file:///android_asset/req_agree_html/agree_1.html"
    private val html_path_2 = "file:///android_asset/req_agree_html/agree_2.html"
    private val html_path_3 = "file:///android_asset/req_agree_html/agree_3.html"
    private val html_path_4 = "file:///android_asset/req_agree_html/agree_4.html"
    private var checkViewAll: CustomCheckView? = null // 이용약관 전체 동의 check view
    private var arrCheckView: Array<CustomCheckView?>? = null // 약관 동의 check view array
    private var data: Data_IUII10M02_F? = null // IUII10M02_F 데이터 클래스

    // 약관 webView
    private var webView_1: WebView? = null
    private var webView_2: WebView? = null
    private var webView_3: WebView? = null
    private var webView_4: WebView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 초기세팅
        setInit()
    }

    @SuppressLint("InflateParams")
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.iubc11m00_f, null)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        if (savedInstanceState != null) {
            if (savedInstanceState.containsKey("data")) {
                data = savedInstanceState.getParcelable("data")
            }
        }

        // UI 생성및 세팅
        setUIControl()

        // UI 상태 복구
        restoreUIState()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)

        // UI 상태 저장
        saveUIState()
        outState.putParcelable("data", data)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        webView_1!!.removeAllViews()
        webView_2!!.removeAllViews()
        webView_3!!.removeAllViews()
        webView_4!!.removeAllViews()
    }

    /**
     * 초기 세팅 함수
     */
    private fun setInit() {
        arrCheckView = arrayOfNulls(4)
        data = Data_IUII10M02_F()
    }

    /**
     * UI 생성 및 세팅 함수
     */
    private fun setUIControl() {

        // 동의 라벨 Text 세팅
        setLabelText()

        // 약관 HTML 세팅
        setAgreeHtml()

        // 체크View 세팅
        setCheckViewUI()
        scrollView = view!!.findViewById<View>(R.id.scrollView) as NestedScrollView
        btnNext = view!!.findViewById<View>(R.id.btnFill) as Button
        btnNext!!.text = resources.getString(R.string.btn_next_2)
        btnNext!!.setOnClickListener {
            if (checkUserInput()) {
                btnNext!!.isEnabled = false
                CF_setWebViewAccessbileImportant(false)
                mActivity!!.CF_showNextPage()
            } else {
                if (CommonFunction.CF_checkAccessibilityTurnOn(activity)) {
                    mActivity!!.CF_setVisibleStepIndicator(true)
                    val tmp_dlg = CustomDialog(activity!!)
                    tmp_dlg.show()
                    tmp_dlg.CF_setBackKeyMode(CustomDialog.BackMode.NOTUSE)
                    tmp_dlg.CF_setTextContent(resources.getString(R.string.dlg_check_all_agree))
                    tmp_dlg.CF_setSingleButtonText(resources.getString(R.string.btn_ok))
                    tmp_dlg.setOnDismissListener { Handler().postDelayed({ checkViewAll!!.sendAccessibilityEvent(AccessibilityEvent.TYPE_VIEW_FOCUSED) }, 500) }
                } else {
                    CommonFunction.CF_showCustomAlertDilaog(activity, resources.getString(R.string.dlg_check_all_agree), resources.getString(R.string.btn_ok))
                }
            }
        }
    }

    /**
     * 라벨 Text 세팅 함수
     */
    private fun setLabelText() {
        val tmp_textLabel_1 = view!!.findViewById<View>(R.id.textAgreeLabel_1) as TextView
        val tmp_textLabel_2 = view!!.findViewById<View>(R.id.textAgreeLabel_2) as TextView
        val tmp_textLabel_3 = view!!.findViewById<View>(R.id.textAgreeLabel_3) as TextView
        val tmp_textLabel_4 = view!!.findViewById<View>(R.id.textAgreeLabel_4) as TextView
        val tmp_spannable_1: Spannable = SpannableString(resources.getString(R.string.label_agree_privacy_1))
        tmp_spannable_1.setSpan(ForegroundColorSpan(Color.parseColor("#ff7c29c7")), tmp_spannable_1.length - 4, tmp_spannable_1.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        tmp_textLabel_1.text = tmp_spannable_1
        val tmp_spannable_2: Spannable = SpannableString(resources.getString(R.string.label_agree_privacy_2))
        tmp_spannable_2.setSpan(ForegroundColorSpan(Color.parseColor("#ff7c29c7")), tmp_spannable_2.length - 4, tmp_spannable_2.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        tmp_textLabel_2.text = tmp_spannable_2
        val tmp_spannable_3: Spannable = SpannableString(resources.getString(R.string.label_agree_privacy_3))
        tmp_spannable_3.setSpan(ForegroundColorSpan(Color.parseColor("#ff7c29c7")), tmp_spannable_3.length - 4, tmp_spannable_3.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        tmp_textLabel_3.text = tmp_spannable_3
        val tmp_spannable_4: Spannable = SpannableString(resources.getString(R.string.label_agree_privacy_4))
        tmp_spannable_4.setSpan(ForegroundColorSpan(Color.parseColor("#ff7c29c7")), tmp_spannable_4.length - 4, tmp_spannable_4.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        tmp_textLabel_4.text = tmp_spannable_4
    }

    /**
     * 약관 HTML Text 세팅 함수
     */
    private fun setAgreeHtml() {
        webView_1 = view!!.findViewById<View>(R.id.webViewAgree_1) as WebView
        webView_2 = view!!.findViewById<View>(R.id.webViewAgree_2) as WebView
        webView_3 = view!!.findViewById<View>(R.id.webViewAgree_3) as WebView
        webView_4 = view!!.findViewById<View>(R.id.webViewAgree_4) as WebView
        webView_1!!.loadUrl(html_path_1)
        webView_2!!.loadUrl(html_path_2)
        webView_3!!.loadUrl(html_path_3)
        webView_4!!.loadUrl(html_path_4)
    }

    /**
     * UI 상태 저장
     */
    private fun saveUIState() {
        data!!.CF_setFlagCheckAll(checkViewAll!!.CF_isChecked())
        data!!.CF_setFlagCheck_1(arrCheckView?.get(0)?.CF_isChecked())
        data!!.CF_setFlagCheck_1(arrCheckView?.get(1)?.CF_isChecked())
        data!!.CF_setFlagCheck_1(arrCheckView?.get(2)?.CF_isChecked())
        data!!.CF_setFlagCheck_1(arrCheckView?.get(3)?.CF_isChecked())
    }

    /**
     * UI 상태 복구
     */
    private fun restoreUIState() {
        checkViewAll!!.CF_setCheck(data!!.CF_getFlagCheckAll(), false, false)
        arrCheckView?.get(0)?.CF_setCheck(data!!.CF_getFlagCheck_1(), false, false)
        arrCheckView?.get(1)?.CF_setCheck(data!!.CF_getFlagCheck_2(), false, false)
        arrCheckView?.get(2)?.CF_setCheck(data!!.CF_getFlagCheck_3(), false, false)
        arrCheckView?.get(3)?.CF_setCheck(data!!.CF_getFlagCheck_4(), false, false)
    }

    /**
     * 체크박스 UI 세팅 함수
     */
    private fun setCheckViewUI() {
        checkViewAll = view!!.findViewById<View>(R.id.checkAll) as CustomCheckView
        checkViewAll!!.CF_setContentsDesc("약관에 전체동의 체크해제 버튼", "약관에 전체동의 체크 버튼")
        arrCheckView?.set(0, view!!.findViewById<View>(R.id.checkAgree_1) as CustomCheckView)
        arrCheckView?.get(0)?.CF_setContentsDesc("개인(신용)정보의 수집이용에 관항 사항 동의 체크해제 버튼", "개인(신용)정보의 수집이용에 관한 사항 동의 체크 버튼")
        arrCheckView!![1] = view!!.findViewById<View>(R.id.checkAgree_2) as CustomCheckView
        arrCheckView?.get(1)?.CF_setContentsDesc("개인(신용)정보의 조회에 관한 사항 동의 체크해제 버튼", "개인(신용)정보의 조회에 관한 사항 동의 체크 버튼")
        arrCheckView!![2] = view!!.findViewById<View>(R.id.checkAgree_3) as CustomCheckView
        arrCheckView?.get(2)?.CF_setContentsDesc("개인(신용)정보의 제공에 관한 사항 동의 체크해제 버튼", "개인(신용)정보의 제공에 관한 사항 동의 체크 버튼")
        arrCheckView!![3] = view!!.findViewById<View>(R.id.checkAgree_4) as CustomCheckView
        arrCheckView?.get(3)?.CF_setContentsDesc("민감정보 및 고유식별정보의 처리에 관한 사항 동의 체크해제 버튼", "민감정보 및 고유식별정보의 처리에 관한 사항 동의 체크 버튼")
        for (i in arrCheckView!!.indices) {
            arrCheckView!![i]!!.CE_setOnChangedCheckedStateEventListener { p_flagIsCheck -> setAllCheckViewState(p_flagIsCheck) }
        }
        checkViewAll!!.CE_setOnChangedCheckedStateEventListener { p_flagIsCheck ->
            setAllCheck(p_flagIsCheck)
            if (p_flagIsCheck) {
                mActivity!!.CF_setVisibleStepIndicator(false)

                // 스크롤 이동
                scrollBottom()
            }
        }
    }

    /**
     * 전체 CheckView 체크상태 세팅 함수<br></br>
     * 마지막 항목의 check 상태를 param으로 전달받아 모든 항목의 check 상태를 검사하여 그 결과를 토대로 전체 CheckView의 체크 상태를 결정한다.
     * @param p_lastChangedCheckState
     */
    private fun setAllCheckViewState(p_lastChangedCheckState: Boolean) {
        var tmp_flagAllSame = true
        for (i in this.arrCheckView!!.indices) {
            if (arrCheckView?.get(i)!!.CF_isChecked() != p_lastChangedCheckState) {
                tmp_flagAllSame = false
                break
            }
        }
        if (tmp_flagAllSame) {
            if (checkViewAll!!.CF_isChecked() != p_lastChangedCheckState) {
                checkViewAll!!.CF_setCheck(p_lastChangedCheckState, true, false)
                if (p_lastChangedCheckState == true) {
                    mActivity!!.CF_setVisibleStepIndicator(false)
                    scrollBottom()
                }
            }
        } else {
            if (checkViewAll!!.CF_isChecked() != false) {
                checkViewAll!!.CF_setCheck(false, true, false)
            }
        }
    }

    /**
     * 전체 CheckView를 제외한 모든 항목CheckView 체크 상태 변경 함수
     */
    private fun setAllCheck(p_flagCheck: Boolean) {
        for (i in arrCheckView!!.indices) {
            if (arrCheckView!![i]!!.CF_isChecked() != p_flagCheck) {
                arrCheckView!![i]!!.CF_setCheck(p_flagCheck, true, false)
            }
        }
    }

    /**
     * 스크롤 Bottom 이동 함수
     */
    private fun scrollBottom() {
        val tmp_scroll = view!!.findViewById<View>(R.id.scrollView) as NestedScrollView
        tmp_scroll.scrollTo(0, tmp_scroll.findViewById<View>(R.id.linContents).height)

        // -----------------------------------------------------------------------------------------
        // 시각 접근성 On 상태시에만 다음 버튼에 Accessible 포커스 이동
        // -----------------------------------------------------------------------------------------
        if (CommonFunction.CF_checkAccessibilityTurnOn(activity)) {
            Handler().postDelayed({ btnNext!!.sendAccessibilityEvent(AccessibilityEvent.TYPE_VIEW_FOCUSED) }, 150)
        }
    }

    /**
     * 사용자 입력값 검사 함수<br></br>
     * 이용동의 체크상태 확인
     * @return
     */
    private fun checkUserInput(): Boolean {

        // 전체 이용동의 체크 상태만 검사하여 그 결과값 반환
        return checkViewAll!!.CF_isChecked()
    }

    /**
     * WebView importantForAccessibility 옵션 설정
     * @param p_flagImportant if true : yes , false : no
     */
    fun CF_setWebViewAccessbileImportant(p_flagImportant: Boolean) {
        if (p_flagImportant) {
            webView_1!!.importantForAccessibility = View.IMPORTANT_FOR_ACCESSIBILITY_YES
            webView_2!!.importantForAccessibility = View.IMPORTANT_FOR_ACCESSIBILITY_YES
            webView_3!!.importantForAccessibility = View.IMPORTANT_FOR_ACCESSIBILITY_YES
            webView_4!!.importantForAccessibility = View.IMPORTANT_FOR_ACCESSIBILITY_YES
        } else {
            webView_1!!.importantForAccessibility = View.IMPORTANT_FOR_ACCESSIBILITY_NO
            webView_2!!.importantForAccessibility = View.IMPORTANT_FOR_ACCESSIBILITY_NO
            webView_3!!.importantForAccessibility = View.IMPORTANT_FOR_ACCESSIBILITY_NO
            webView_4!!.importantForAccessibility = View.IMPORTANT_FOR_ACCESSIBILITY_NO
        }
    }
}