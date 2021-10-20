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
import android.widget.TextView
import androidx.core.widget.NestedScrollView
import com.epost.insu.EnvConfig
import com.epost.insu.R
import com.epost.insu.common.*
import com.epost.insu.control.CustomCheckView
import com.epost.insu.data.Data_IUII10M02_F
import com.epost.insu.dialog.CustomDialog
import java.util.*

/**
 * 보험금청구 > 본인청구 > 2단계. 개인정보처리동의
 * @since     :
 * @version   : 1.3
 * @author    : LSH
 * @see
 * <pre>
 * - 요청에 의하여 Scroll 안에 WebView를 넣어 Scroll 이동에 제한이 있음
 * - 접근성 이유로 WebView importantForAccessibility 옵션 값 조절이 필요함. (현재 화면에 보이는 경우 yes, 보이지 않는 경우 no)
 * : webView가 화면에 보이지 않아도 음성지원에 읽히는 문제가 있음.
 * ======================================================================
 * 0.0.0    LSH_20170707    최초 등록
 * 0.0.0    NJM_20191210    앱접근성 - 올체크 후 다음버튼 초점이동 (스크롤) 해지
 * 0.0.0    NJM_20200122    공통 인증유형/청구유형 추가에 따른 로직수정
 * 1.5.6    NJM_20210528    [청구서서식변경] 개인정보처리동의 내용 변경
 * =======================================================================
 * copyright : (주)프로니스정보기술 | http://www.phronis.co.kr
 * </pre>
 */
class IUII10M02_F : IUII10M00_FD() {
    private var checkViewAll: CustomCheckView? = null // 이용약관 전체 동의 check view
    private var arrCheckView: Array<CustomCheckView?>? = null // 약관 동의 check view array

    // 약관 webView
    private var webView1: WebView? = null
    private var webview2: WebView? = null
    private var webview3: WebView? = null

    private var data: Data_IUII10M02_F? = null // IUII10M02_F 데이터 클래스

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // -- 초기세팅
        arrCheckView = arrayOfNulls(3)
        data = Data_IUII10M02_F()
        LogPrinter.CF_debug("!---- (2단계) 인증구분 : " + mActivity!!.CF_getAuthDvsn())
    }

    @SuppressLint("InflateParams")
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.iuii10m02_f, null)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        if (savedInstanceState != null) {
            if (savedInstanceState.containsKey("data")) {
                data = savedInstanceState.getParcelable("data")
            }
        }

        // -- UI 생성및 세팅
        setUIControl()

        // -- UI 상태 복구
        restoreUIState()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)

        // -- UI 상태 저장
        saveUIState()
        outState.putParcelable("data", data)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        webView1!!.removeAllViews()
        webview2!!.removeAllViews()
        webview3!!.removeAllViews()
        //webView_4.removeAllViews();
    }

    /**
     * UI 생성 및 세팅 함수
     */
    private fun setUIControl() {
        setLabelText() // 동의 라벨 Text 세팅
        setAgreeHtml() // 약관 HTML 세팅
        setCheckViewUI() // 체크View 세팅
        scrollView = view?.findViewById(R.id.scrollView)
        btnNext = view?.findViewById(R.id.btnFill)
        btnNext?.text = resources.getString(R.string.btn_next_2)
        btnNext?.setOnClickListener {
            if (checkUserInput()) {
                btnNext?.isEnabled = false
                CF_setWebViewAccessbileImportant(false)
                mActivity!!.CF_showNextPage()
            } else {
                if (CommonFunction.CF_checkAccessibilityTurnOn(Objects.requireNonNull(activity))) {
                    mActivity!!.CF_setVisibleStepIndicator(true)
                    val customDialog = CustomDialog((activity)!!)
                    customDialog.show()
                    customDialog.CF_setBackKeyMode(CustomDialog.BackMode.NOTUSE)
                    customDialog.CF_setTextContent(resources.getString(R.string.dlg_check_all_agree))
                    customDialog.CF_setSingleButtonText(resources.getString(R.string.btn_ok))
                    customDialog.setOnDismissListener {
                        Handler().postDelayed(object : Runnable {
                            override fun run() {
                                checkViewAll!!.sendAccessibilityEvent(AccessibilityEvent.TYPE_VIEW_FOCUSED)
                            }
                        }, 500)
                    }
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
        val txtAgreeLabel1: TextView = view!!.findViewById(R.id.textAgreeLabel_1)
        val txtAgreeLabel2: TextView = view!!.findViewById(R.id.textAgreeLabel_2)
        val txtAgreeLabel3: TextView = view!!.findViewById(R.id.textAgreeLabel_3)

        val spannable1: Spannable = SpannableString(resources.getString(R.string.label_agree_privacy_1))
        spannable1.setSpan(ForegroundColorSpan(Color.parseColor("#ff7c29c7")), spannable1.length - 4, spannable1.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        txtAgreeLabel1.text = spannable1
        val spannable2: Spannable = SpannableString(resources.getString(R.string.label_agree_privacy_2))
        spannable2.setSpan(ForegroundColorSpan(Color.parseColor("#ff7c29c7")), spannable2.length - 4, spannable2.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        txtAgreeLabel2.text = spannable2
        val spannable3: Spannable = SpannableString(resources.getString(R.string.label_agree_privacy_3))
        spannable3.setSpan(ForegroundColorSpan(Color.parseColor("#ff7c29c7")), spannable3.length - 4, spannable3.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        txtAgreeLabel3.text = spannable3
    }

    /**
     * 약관 HTML Text 세팅 함수
     */
    private fun setAgreeHtml() {
        webView1 = view!!.findViewById(R.id.webViewAgree_1)
        webview2 = view!!.findViewById(R.id.webViewAgree_2)
        webview3 = view!!.findViewById(R.id.webViewAgree_3)
        webView1?.loadUrl(EnvConfig.URL_FILE_AGREE1)
        webview2?.loadUrl(EnvConfig.URL_FILE_AGREE2)
        webview3?.loadUrl(EnvConfig.URL_FILE_AGREE3)
    }

    /**
     * UI 상태 저장
     */
    private fun saveUIState() {
        data!!.CF_setFlagCheckAll(checkViewAll!!.CF_isChecked())
        data!!.CF_setFlagCheck_1(arrCheckView?.get(0)!!.CF_isChecked())
        data!!.CF_setFlagCheck_2(arrCheckView?.get(1)!!.CF_isChecked())
        data!!.CF_setFlagCheck_3(arrCheckView?.get(2)!!.CF_isChecked())
    }

    /**
     * UI 상태 복구
     */
    private fun restoreUIState() {
        checkViewAll!!.CF_setCheck(data!!.CF_getFlagCheckAll(), false, false)
        arrCheckView?.get(0)!!.CF_setCheck(data!!.CF_getFlagCheck_1(), false, false)
        arrCheckView?.get(1)!!.CF_setCheck(data!!.CF_getFlagCheck_2(), false, false)
        arrCheckView?.get(2)!!.CF_setCheck(data!!.CF_getFlagCheck_3(), false, false)
    }

    /**
     * 체크박스 UI 세팅 함수
     */
    private fun setCheckViewUI() {
        checkViewAll = view?.findViewById(R.id.checkAll)
        checkViewAll?.CF_setContentsDesc("약관에 전체동의 체크해제 버튼", "약관에 전체동의 체크 버튼")
        arrCheckView!![0] = view!!.findViewById(R.id.checkAgree_1)
        arrCheckView?.get(0)?.CF_setContentsDesc("개인(신용)정보의 수집이용에 관항 사항 동의 체크해제 버튼", "개인(신용)정보의 수집이용에 관한 사항 동의 체크 버튼")
        arrCheckView!![1] = view!!.findViewById(R.id.checkAgree_2)
        arrCheckView?.get(1)?.CF_setContentsDesc("개인(신용)정보의 조회에 관한 사항 동의 체크해제 버튼", "개인(신용)정보의 조회에 관한 사항 동의 체크 버튼")
        arrCheckView!![2] = view!!.findViewById(R.id.checkAgree_3)
        arrCheckView?.get(2)?.CF_setContentsDesc("개인(신용)정보의 제공에 관한 사항 동의 체크해제 버튼", "개인(신용)정보의 제공에 관한 사항 동의 체크 버튼")

        for (customCheckView: CustomCheckView? in arrCheckView!!) {
            customCheckView!!.CE_setOnChangedCheckedStateEventListener { p_flagIsCheck -> setAllCheckViewState(p_flagIsCheck) }
        }

        // 전체동의 체크박스
        checkViewAll?.CE_setOnChangedCheckedStateEventListener { p_flagIsCheck ->
            // 전체체크 처리
            setAllCheck(p_flagIsCheck)
            if (p_flagIsCheck) {
                mActivity!!.CF_setVisibleStepIndicator(false)

                // -----------------------------------------------------------------------------
                // (시각접근성 On상태) : 스크롤이동 방지   // 2019-12-10
                // -----------------------------------------------------------------------------
                if (CommonFunction.CF_checkAccessibilityTurnOn(Objects.requireNonNull(activity))) {
                    // 스크롤이동 없음
                } else {
                    // 스크롤이동
                    scrollBottom()
                }
            }
        }
    }

    /**
     * 전체 CheckView 체크상태 세팅 함수<br></br>
     * 마지막 항목의 check 상태를 param으로 전달받아 모든 항목의 check 상태를 검사하여 그 결과를 토대로 전체 CheckView의 체크 상태를 결정한다.
     * @param p_lastChangedCheckState       boolean
     */
    private fun setAllCheckViewState(p_lastChangedCheckState: Boolean) {
        var flagAllSame = true
        for (customCheckView: CustomCheckView? in arrCheckView!!) {
            if (customCheckView!!.CF_isChecked() != p_lastChangedCheckState) {
                flagAllSame = false
                break
            }
        }
        if (flagAllSame) {
            if (checkViewAll!!.CF_isChecked() != p_lastChangedCheckState) {
                checkViewAll!!.CF_setCheck(p_lastChangedCheckState, true, false)
                if (p_lastChangedCheckState) {
                    mActivity!!.CF_setVisibleStepIndicator(false)
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
        for (customCheckView: CustomCheckView? in arrCheckView!!) {
            if (customCheckView!!.CF_isChecked() != p_flagCheck) {
                customCheckView.CF_setCheck(p_flagCheck, true, false)
            }
        }
    }

    /**
     * 스크롤 Bottom 이동 함수
     */
    private fun scrollBottom() {
        val scroll: NestedScrollView? = view?.findViewById(R.id.scrollView)
        scroll?.scrollTo(0, scroll.findViewById<View>(R.id.linContents).height)

        // -----------------------------------------------------------------------------------------
        // 시각 접근성 On 상태시에만 다음 버튼에 Accessible 포커스 이동
        // -----------------------------------------------------------------------------------------
        if (CommonFunction.CF_checkAccessibilityTurnOn(Objects.requireNonNull(activity))) {
            Handler().postDelayed(object : Runnable {
                override fun run() {
                    btnNext!!.sendAccessibilityEvent(AccessibilityEvent.TYPE_VIEW_FOCUSED)
                }
            }, 150)
        }
    }

    /**
     * 사용자 입력값 검사 함수<br></br>
     * 이용동의 체크상태 확인
     * @return  boolean
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
            webView1!!.importantForAccessibility = View.IMPORTANT_FOR_ACCESSIBILITY_YES
            webview2!!.importantForAccessibility = View.IMPORTANT_FOR_ACCESSIBILITY_YES
            webview3!!.importantForAccessibility = View.IMPORTANT_FOR_ACCESSIBILITY_YES
        } else {
            webView1!!.importantForAccessibility = View.IMPORTANT_FOR_ACCESSIBILITY_NO
            webview2!!.importantForAccessibility = View.IMPORTANT_FOR_ACCESSIBILITY_NO
            webview3!!.importantForAccessibility = View.IMPORTANT_FOR_ACCESSIBILITY_NO
        }
    }
}