package com.epost.insu.fragment

import android.annotation.SuppressLint
import android.os.Bundle
import android.os.Handler
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import com.epost.insu.R
import com.epost.insu.common.CommonFunction
import com.epost.insu.common.FormatUtils
import com.epost.insu.common.LogPrinter
import com.epost.insu.control.SwitchingControl
import com.epost.insu.data.Data_IUII10M05_F
import com.epost.insu.event.OnChangedCheckedStateEventListener
import java.util.*

/**
 * 보험금청구 > 자녀청구 > 5단계. 보험청구서작성(청구내용2)
 * @since     :
 * @version   : 1.1
 * @author    : YJH
 * @see
 * <pre>
 * 보험금청구서 작성 2단계 내용으로 사고처리여부, 타보험사 가입내용을 입력한다
 * ======================================================================
 * 0.0.0    YJH_20181109    최초 등록
 * 0.0.0    NJM_20200207    문자셋 오류 검사 기능 추가 (기타)
 * =======================================================================
 * copyright : (주)프로니스정보기술 | http://www.phronis.co.kr
 * </pre>
 */
class IUII90M05_F : IUII90M00_FD() {
    private val maxInsureName = 40 // 보험사명 맥스 length
    private val maxCountOtherInsure = 4 // 타보험사 최대 수
    private val maxLengthCarInsure = 40 // 자동차보험 보험사명 맥스 length

    private var linInputCarInsure: LinearLayout? = null
    private var textLabelCar: TextView? = null
    private var textLabelIndustry: TextView? = null
    private var textLabelPolice: TextView? = null
    private var textLabelJoin: TextView? = null
    private var textLabelSilson: TextView? = null
    private var switchCarInsure: SwitchingControl? = null
    private var switchIndustryInsure: SwitchingControl? = null
    private var switchCallPolice: SwitchingControl? = null
    private var switchJoinOther: SwitchingControl? = null
    private var linOtherInsure: LinearLayout? = null
    private var linOtherInsureMore: LinearLayout? = null
    private var switchJoinReal: SwitchingControl? = null
    private var linRealInsure: LinearLayout? = null
    private var linRealInsureMore: LinearLayout? = null
    private var edtCarInsure: EditText? = null
    private var edtOther: EditText? = null
    private var edtOtherInsure: EditText? = null
    private var edtRealInsure: EditText? = null
    private var data: Data_IUII10M05_F? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setInit()
    }

    @SuppressLint("InflateParams")
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.iuii10m05_f, null)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        if (savedInstanceState != null) {
            if (savedInstanceState.containsKey("data")) {
                data = savedInstanceState.getParcelable("data")
            }
        }

        // UI 생성 및 세팅함수
        setUIControl()

        // 데이터 복구
        restoreData()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        saveData()
        outState.putParcelable("data", data)
    }

    /**
     * 초기 세팅 함수
     */
    private fun setInit() {
        LogPrinter.CF_debug("!----------------------------------------------------------")
        LogPrinter.CF_debug("!-- IUII90M05_F.setInit()")
        LogPrinter.CF_debug("!----------------------------------------------------------")
        data = Data_IUII10M05_F()
        LogPrinter.CF_debug("!---- (5단계) 인증구분 : " + mActivity!!.CF_getAuthDvsn())
    }

    /**
     * UI 생성 및 세팅 함수
     */
    private fun setUIControl() {

        // 사고처리여부 UI 세팅 함수
        setUIOfFlagDo()

        // 타보험가 가입내용
        setUIOfOtherInsureJoin()

        // 실손보험 가입내용
        setUIOfRealInsureJoin()
        scrollView = view!!.findViewById(R.id.scrollView)

        // 다음 버튼
        btnNext = view!!.findViewById(R.id.btnFill)
        btnNext?.text = resources.getString(R.string.btn_next_2)
        btnNext?.setOnClickListener(object : View.OnClickListener {
            override fun onClick(v: View) {

                // ---------------------------------------------------------------------------------
                //  키패드 가리기
                // ---------------------------------------------------------------------------------
                if (activity!!.currentFocus != null) CommonFunction.CF_closeVirtualKeyboard(activity, activity!!.currentFocus!!.windowToken)
                if (checkUserInput()) {
                    val tmp_data = mActivity?.CF_getData()
                    tmp_data!!.CF_setS_car_insu_yn(if (switchCarInsure!!.CF_getCheckState() > 0) "Y" else "N")
                    tmp_data.CF_setS_insu_comp_nm(edtCarInsure!!.text.toString().trim())
                    tmp_data.CF_setS_inds_dstr_insu_yn(if (switchIndustryInsure!!.CF_getCheckState() > 0) "Y" else "N")
                    tmp_data.CF_setS_polc_decl_yn(if (switchCallPolice!!.CF_getCheckState() > 0) "Y" else "N")
                    tmp_data.CF_setS_etc_desc(edtOther!!.text.toString().trim())
                    tmp_data.CF_setS_other_insu_comp_entr_yn(if (switchJoinOther!!.CF_getCheckState() > 0) "Y" else "N")
                    tmp_data.CF_setS_other_insu_comp_nm(strOtherInsureCompanyName)
                    tmp_data.CF_setS_rllo_entr_yn(if (switchJoinReal!!.CF_getCheckState() > 0) "Y" else "N")
                    tmp_data.CF_setS_rllo_insu_comp_nm(strRealInsureCompanyName)
                    btnNext!!.isEnabled = false

                    // -----------------------------------------------------------------------------
                    //  다음페이지 이동 시간 Delay
                    // -----------------------------------------------------------------------------
                    Handler().postDelayed(Runnable {
                        mActivity?.CF_showNextPage()
                        mActivity?.CF_setVisibleStepIndicator(true)
                    }, 120L)
                }
            }
        })
    }

    /**
     * 데이터 복구 함수
     */
    private fun restoreData() {
        switchCarInsure!!.CF_setCheck(data!!.CF_getCheckStateCar())
        setCarInsureDesc(data!!.CF_getCheckStateCar() > 0)
        switchIndustryInsure!!.CF_setCheck(data!!.CF_getCheckStateIndustry())
        setIndustryInsureDesc(data!!.CF_getCheckStateIndustry() > 0)
        switchCallPolice!!.CF_setCheck(data!!.CF_getCheckStatePolice())
        setPoliceDesc(data!!.CF_getCheckStatePolice() > 0)
        switchJoinOther!!.CF_setCheck(data!!.CF_getCheckStateJoinOther())
        setOtherInsureJoinDesc(data!!.CF_getCheckStateJoinOther() > 0)
        switchJoinReal!!.CF_setCheck(data!!.CF_getCheckStateJoinReal())
        setRealInsureDesc(data!!.CF_getCheckStateJoinReal() > 0)
        edtCarInsure!!.setText(data!!.CF_getStrCarInsure())
        edtOther!!.setText(data!!.CF_getNote())
        edtOtherInsure!!.setText(data!!.CF_getStrOtherInsure())
        edtRealInsure!!.setText(data!!.CF_getStrRealInsure())
        val arrOtherInsureMore = data!!.CF_getArrOtherInsureMore()
        val arrRealInsureMore = data!!.CF_getArrRealInsureMore()

        // 자동차보험에 예 체크한 경우 보험사명 입력 UI를 보인다.
        if (switchCarInsure!!.CF_getCheckState() > 0) {
            linInputCarInsure!!.visibility = View.VISIBLE
        }
        if (switchJoinOther!!.CF_getCheckState() > 0) {
            linOtherInsure!!.visibility = View.VISIBLE
            for (i in arrOtherInsureMore.indices) {
                createInputCompany(linOtherInsureMore, arrOtherInsureMore[i]) { v ->
                    linOtherInsureMore!!.removeView(v.parent as View)

                    // -------------------------------------------------------------------------
                    //  접근성 On : 가입여부 라벨로 포커스 이동
                    // -------------------------------------------------------------------------
                    if (CommonFunction.CF_checkAccessibilityTurnOn(activity)) {
                        clearAllFocus()
                        textLabelJoin!!.isFocusableInTouchMode = true
                        textLabelJoin!!.requestFocus()
                        textLabelJoin!!.isFocusableInTouchMode = false
                    }
                }
            }
        }
        if (switchJoinReal!!.CF_getCheckState() > 0) {
            linRealInsure!!.visibility = View.VISIBLE
            for (i in arrRealInsureMore.indices) {
                createInputCompany(linRealInsureMore, arrRealInsureMore[i]) { v ->
                    linRealInsureMore!!.removeView(v.parent as View)

                    // -------------------------------------------------------------------------
                    //  접근성 On : 실손가입여부 라벨로 포커스 이동
                    // -------------------------------------------------------------------------
                    if (CommonFunction.CF_checkAccessibilityTurnOn(activity)) {
                        clearAllFocus()
                        textLabelSilson!!.isFocusableInTouchMode = true
                        textLabelSilson!!.requestFocus()
                        textLabelSilson!!.isFocusableInTouchMode = false
                    }
                }
            }
        }
    }

    /**
     * 데이터 저장
     */
    private fun saveData() {
        data!!.CF_setCheckStateCar(switchCarInsure!!.CF_getCheckState())
        data!!.CF_setCheckStateIndustry(switchIndustryInsure!!.CF_getCheckState())
        data!!.CF_setCheckStatePolice(switchCallPolice!!.CF_getCheckState())
        data!!.CF_setCheckStateJoinOther(switchJoinOther!!.CF_getCheckState())
        data!!.CF_setCheckStateJoinReal(switchJoinReal!!.CF_getCheckState())
        data!!.CF_setStrCarInsure(edtCarInsure!!.text.toString())
        data!!.CF_setNote(edtOther!!.text.toString())
        data!!.CF_setStrOtherInsure(edtOtherInsure!!.text.toString())
        data!!.CF_setStrRealInsure(edtRealInsure!!.text.toString())
        data!!.CF_setArrOtherInsureMore(otherInsureCompanyNameMoreList)
        data!!.CF_setArrRealInsureMore(realInsureCompanyNameMoreList)
    }

    /**
     * 사고처리여부 UI 세팅 함수
     */
    private fun setUIOfFlagDo() {
        textLabelCar = view!!.findViewById(R.id.textLabelInsureCar)
        switchCarInsure = view!!.findViewById(R.id.switchingInsureCar)
        switchCarInsure?.CE_setOnChangedCheckedStateEventListener(object : OnChangedCheckedStateEventListener {
            override fun onCheck(p_flagCheck: Boolean) {

                // 자동차보험 라벨 description 설정
                setCarInsureDesc(p_flagCheck)
                if (p_flagCheck) {
                    linInputCarInsure!!.visibility = View.VISIBLE

                    // -----------------------------------------------------------------------------
                    //  접근성 Off 상태에서만 키패드 자동 show
                    // -----------------------------------------------------------------------------
                    if (CommonFunction.CF_checkAccessibilityTurnOn(activity)) {
                        showCustomDialog(resources.getString(R.string.dlg_accessible_open_insure_inputbox), (edtCarInsure)!!)
                    } else {
                        CommonFunction.CF_showVirtualKeyboard(activity, edtCarInsure)
                    }
                } else {

                    // -----------------------------------------------------------------------------
                    //  접근성 Accessiblity 포커스 이동을 막기 위해 EditText의 parent Viewgroup 에
                    //  descendantFocus block 처리
                    // -----------------------------------------------------------------------------
                    interceptEditTextOtherFocus()

                    // -----------------------------------------------------------------------------
                    //  키패드 내리기
                    // -----------------------------------------------------------------------------
                    if (activity!!.hasWindowFocus()) {
                        if (activity!!.window.currentFocus != null) {
                            CommonFunction.CF_closeVirtualKeyboard(activity, activity!!.currentFocus!!.windowToken)
                        }
                    }
                    linInputCarInsure!!.visibility = View.GONE
                    edtCarInsure!!.setText("")
                    if (CommonFunction.CF_checkAccessibilityTurnOn(activity)) {
                        clearAllFocus()
                        textLabelCar?.isFocusableInTouchMode = true
                        textLabelCar?.requestFocus()
                        textLabelCar?.isFocusableInTouchMode = false
                    }
                }
            }
        })

        // 자동차보험 입력 레이아웃
        linInputCarInsure = view!!.findViewById(R.id.linInputCarInsure)
        edtCarInsure = view!!.findViewById(R.id.edtCarInsure)
        edtCarInsure?.filters = CommonFunction.CF_getInputLengthFilter(maxLengthCarInsure)

        // 산업재해보험 레이아웃
        textLabelIndustry = view!!.findViewById(R.id.textLabelIndustry)
        switchIndustryInsure = view!!.findViewById(R.id.switchingInsureIndustry)
        switchIndustryInsure?.CE_setOnChangedCheckedStateEventListener(object : OnChangedCheckedStateEventListener {
            override fun onCheck(p_flagCheck: Boolean) {
                setIndustryInsureDesc(p_flagCheck)
            }
        })

        // 경찰신고 레이아웃
        textLabelPolice = view!!.findViewById(R.id.textLabelPolice)
        switchCallPolice = view!!.findViewById(R.id.switchingPolice)
        switchCallPolice?.CE_setOnChangedCheckedStateEventListener(object : OnChangedCheckedStateEventListener {
            override fun onCheck(p_flagCheck: Boolean) {
                setPoliceDesc(p_flagCheck)
            }
        })

        // 기타입력 EditText
        edtOther = view!!.findViewById(R.id.edtOther)
    }

    /**
     * 타보험사 가입내용 UI 세팅 함수
     */
    private fun setUIOfOtherInsureJoin() {
        linOtherInsure = view!!.findViewById(R.id.linAddOtherInsure)
        linOtherInsureMore = view!!.findViewById(R.id.linAddOtherInsureMore)
        val linInputOtherInsure = view!!.findViewById<View>(R.id.linOtherInsureInput)
        edtOtherInsure = linInputOtherInsure.findViewById(R.id.editText)
        edtOtherInsure?.privateImeOptions = "defaultInputmode=korean"
        textLabelJoin = view!!.findViewById(R.id.textLabelJoin)
        switchJoinOther = view!!.findViewById(R.id.switchingJoin)
        switchJoinOther?.CE_setOnChangedCheckedStateEventListener(object : OnChangedCheckedStateEventListener {
            override fun onCheck(p_flagCheck: Boolean) {
                // -- 타보험사 가입여부 라벨 description 설정
                setOtherInsureJoinDesc(p_flagCheck)
                if (p_flagCheck) {
                    linOtherInsure?.visibility = View.VISIBLE

                    // -----------------------------------------------------------------------------
                    //  접근성 On : 입력창 이동 확인 다이얼로그 show
                    // -----------------------------------------------------------------------------
                    if (CommonFunction.CF_checkAccessibilityTurnOn(activity)) {
                        showCustomDialog(resources.getString(R.string.dlg_accessible_open_insure_inputbox), edtOtherInsure!!)
                    } else {
                        CommonFunction.CF_showVirtualKeyboard(activity, edtOtherInsure)
                    }
                } else {

                    // -----------------------------------------------------------------------------
                    //  접근성 Accessiblity 포커스 이동을 막기 위해 EditText의 parent Viewgroup 에
                    //  descendantFocus block 처리
                    // -----------------------------------------------------------------------------
                    interceptEditTextOtherFocus()

                    // -----------------------------------------------------------------------------
                    //  키패드 내리기
                    // -----------------------------------------------------------------------------
                    if (activity!!.hasWindowFocus()) {
                        if (activity!!.window.currentFocus != null) {
                            CommonFunction.CF_closeVirtualKeyboard(activity, activity!!.currentFocus!!.windowToken)
                        }
                    }
                    edtOtherInsure?.setText("")
                    linOtherInsure?.visibility = View.GONE
                    linOtherInsureMore?.removeAllViews()

                    // -----------------------------------------------------------------------------
                    //  접근성 On : 타보험사 가입여부 라벨로 포커스 이동
                    // -----------------------------------------------------------------------------
                    if (CommonFunction.CF_checkAccessibilityTurnOn(activity)) {
                        clearAllFocus()
                        textLabelJoin?.isFocusableInTouchMode = true
                        textLabelJoin?.requestFocus()
                        textLabelJoin?.isFocusableInTouchMode = false
                    }
                }
            }
        })
        val tmp_btn = view!!.findViewById<Button>(R.id.btnAddCompany_1)
        tmp_btn.setOnClickListener {
            if (linOtherInsureMore?.childCount!! < maxCountOtherInsure) {
                createInputCompany(linOtherInsureMore, "") { v ->
                    linOtherInsureMore?.removeView(v.parent as View)

                    // -------------------------------------------------------------------------
                    //  접근성 On : 가입여부 라벨로 포커스 이동
                    // -------------------------------------------------------------------------
                    if (CommonFunction.CF_checkAccessibilityTurnOn(activity)) {
                        clearAllFocus()
                        textLabelJoin?.isFocusableInTouchMode = true
                        textLabelJoin?.requestFocus()
                        textLabelJoin?.isFocusableInTouchMode = false
                    }
                }

                // ---------------------------------------------------------------------------------
                //  접근성 On : 보험사명 입력 UI로 이동
                // ---------------------------------------------------------------------------------
                if (CommonFunction.CF_checkAccessibilityTurnOn(activity)) {
                    val lin = linOtherInsureMore?.getChildAt(linOtherInsureMore?.childCount!! - 1) as LinearLayout
                    showCustomDialog(resources.getString(R.string.dlg_accessible_open_insure_inputbox), lin.findViewById(R.id.editText))
                }
            }
        }
    }

    /**
     * 실손보험 가입 여부 UI 세팅 함수
     */
    private fun setUIOfRealInsureJoin() {
        linRealInsure = view!!.findViewById(R.id.linAddRealInsure)
        linRealInsureMore = view!!.findViewById(R.id.linAddRealInsureMore)
        val tmp_linInputOtherInsure = view!!.findViewById<View>(R.id.linRealInsureInput)
        edtRealInsure = tmp_linInputOtherInsure.findViewById(R.id.editText)
        edtRealInsure?.privateImeOptions = "defaultInputmode=korean"
        textLabelSilson = view!!.findViewById(R.id.textLabelRealInsure)
        switchJoinReal = view!!.findViewById(R.id.switchingRealInsure)
        switchJoinReal?.CE_setOnChangedCheckedStateEventListener(object : OnChangedCheckedStateEventListener {
            override fun onCheck(p_flagCheck: Boolean) {

                // 실손가입여부 라벨 description 설정
                setRealInsureDesc(p_flagCheck)
                if (p_flagCheck) {
                    linRealInsure?.visibility = View.VISIBLE

                    // -----------------------------------------------------------------------------
                    //  접근성 On : 입력창 이동 확인 다이얼로그 show
                    // -----------------------------------------------------------------------------
                    if (CommonFunction.CF_checkAccessibilityTurnOn(activity)) {
                        showCustomDialog(resources.getString(R.string.dlg_accessible_open_insure_inputbox), edtRealInsure!!)
                    } else {
                        CommonFunction.CF_showVirtualKeyboard(activity, edtRealInsure)
                    }
                } else {
                    // -----------------------------------------------------------------------------
                    //  접근성 Accessiblity 포커스 이동을 막기 위해 EditText의 parent Viewgroup 에
                    //  descendantFocus block 처리
                    // -----------------------------------------------------------------------------
                    interceptEditTextOtherFocus()

                    // -----------------------------------------------------------------------------
                    //  키패드 내리기
                    // -----------------------------------------------------------------------------
                    if (activity!!.hasWindowFocus()) {
                        if (activity!!.window.currentFocus != null) {
                            CommonFunction.CF_closeVirtualKeyboard(activity, activity!!.currentFocus!!.windowToken)
                        }
                    }
                    edtRealInsure?.setText("")
                    linRealInsure?.visibility = View.GONE
                    linRealInsureMore?.removeAllViews()

                    // -----------------------------------------------------------------------------
                    //  접근성 On : 실손가입여부 라벨로 포커스 이동
                    // -----------------------------------------------------------------------------
                    if (CommonFunction.CF_checkAccessibilityTurnOn(activity)) {
                        clearAllFocus()
                        textLabelSilson?.isFocusableInTouchMode = true
                        textLabelSilson?.requestFocus()
                        textLabelSilson?.isFocusableInTouchMode = false
                    }
                }
            }
        })

        // 실손보험사 추가 버튼
        val tmp_btn = view!!.findViewById<Button>(R.id.btnAddCompany_2)
        tmp_btn.setOnClickListener {
            if (linRealInsureMore?.childCount!! < maxCountOtherInsure) {
                createInputCompany(linRealInsureMore, "") { v ->
                    linRealInsureMore?.removeView(v.parent as View)

                    // -------------------------------------------------------------------------
                    //  접근성 On : 가입여부 라벨로 포커스 이동
                    // -------------------------------------------------------------------------
                    if (CommonFunction.CF_checkAccessibilityTurnOn(activity)) {
                        clearAllFocus()
                        textLabelSilson?.isFocusableInTouchMode = true
                        textLabelSilson?.requestFocus()
                        textLabelSilson?.isFocusableInTouchMode = false
                    }
                }

                // ---------------------------------------------------------------------------------
                //  접근성 On : 보험사명 입력 UI로 이동
                // ---------------------------------------------------------------------------------
                if (CommonFunction.CF_checkAccessibilityTurnOn(activity)) {
                    val lin = linRealInsureMore?.getChildAt(linRealInsureMore?.childCount!! - 1) as LinearLayout
                    showCustomDialog(resources.getString(R.string.dlg_accessible_open_insure_inputbox), lin.findViewById(R.id.editText))
                }
            }
        }
    }

    /**
     * 자동차 보험 라벨 description 설정
     * @param p_flagYes     boolean
     */
    private fun setCarInsureDesc(p_flagYes: Boolean) {
        if (p_flagYes) {
            textLabelCar!!.contentDescription = resources.getString(R.string.desc_car_insure_yes)
        } else {
            textLabelCar!!.contentDescription = resources.getString(R.string.desc_car_insure_no)
        }
    }

    /**
     * 산업재해보험 라벨 description 설정
     * @param p_flagYes     boolean
     */
    private fun setIndustryInsureDesc(p_flagYes: Boolean) {
        if (p_flagYes) {
            textLabelIndustry!!.contentDescription = resources.getString(R.string.desc_industry_insure_yes)
        } else {
            textLabelIndustry!!.contentDescription = resources.getString(R.string.desc_industry_insure_no)
        }
    }

    /**
     * 경찰서신고 라벨 description 설정
     * @param p_flagYes     boolean
     */
    private fun setPoliceDesc(p_flagYes: Boolean) {
        if (p_flagYes) {
            textLabelPolice!!.contentDescription = resources.getString(R.string.desc_police_yes)
        } else {
            textLabelPolice!!.contentDescription = resources.getString(R.string.desc_police_no)
        }
    }

    /**
     * 타보험사 가입여부 라벨 description 설정
     * @param p_flagYes     boolean
     */
    private fun setOtherInsureJoinDesc(p_flagYes: Boolean) {
        if (p_flagYes) {
            textLabelJoin!!.contentDescription = resources.getString(R.string.desc_other_join_yes)
        } else {
            textLabelJoin!!.contentDescription = resources.getString(R.string.desc_other_join_no)
        }
    }

    /**
     * 실손가입여부 라벨 description 설정
     * @param p_flagYes     boolean
     */
    private fun setRealInsureDesc(p_flagYes: Boolean) {
        if (p_flagYes) {
            textLabelSilson!!.contentDescription = resources.getString(R.string.desc_silson_yes)
        } else {
            textLabelSilson!!.contentDescription = resources.getString(R.string.desc_silson_no)
        }
    }

    /**
     * 기타 내용 입력 포커스 이동 제한<br></br>
     * 특정행위 뒤 접근성 포커스 강제 이동을 막기 위함.<br></br>
     * 약간의 시간 딜레이 뒤에 원복한다.
     */
    private fun interceptEditTextOtherFocus() {
        if (CommonFunction.CF_checkAccessibilityTurnOn(activity)) {
            val lin = view!!.findViewById<LinearLayout>(R.id.linOtherInput)
            lin.descendantFocusability = ViewGroup.FOCUS_BLOCK_DESCENDANTS
            Handler().postDelayed({ lin.descendantFocusability = ViewGroup.FOCUS_BEFORE_DESCENDANTS }, 500)
        }
    }

    /**
     * 보험사명 입력 UI 생성 및 세팅
     * @param p_parentView      LinearLayout
     * @param p_strText         String
     * @param p_onClickRemove   View.OnClickListener
     */
    private fun createInputCompany(p_parentView: LinearLayout?, p_strText: String, p_onClickRemove: View.OnClickListener) {
        val linItem = View.inflate(this.activity, R.layout.c_input_other_company, null) as LinearLayout
        linItem.setPadding(0, CommonFunction.CF_convertDipToPixel(activity!!.applicationContext, 5f), 0, 0)
        val edt = linItem.findViewById<EditText>(R.id.editText)
        edt.setText(p_strText)
        edt.privateImeOptions = "defaultInputmode=korean"
        val btnRemove = linItem.findViewById<ImageButton>(R.id.btnDel)
        btnRemove.setOnClickListener(p_onClickRemove)
        btnRemove.visibility = View.VISIBLE
        p_parentView!!.addView(linItem)
    }

    /**
     * 타보험사 보험사명을 문자열로 반환 한다.<br></br>
     * 입력값이 empty 인 경우 제외 시킨다.
     * @return      String
     */
    private val strOtherInsureCompanyName: String
        get() {
            val arrText = ArrayList<String?>()
            if (!TextUtils.isEmpty(edtOtherInsure!!.text.toString().trim())) {
                arrText.add(edtOtherInsure!!.text.toString().trim())
            }
            val arrMore = otherInsureCompanyNameMoreList
            for (i in arrMore.indices) {
                val arrInput = arrMore[i]
                if (!TextUtils.isEmpty(arrInput)) {
                    arrText.add(arrInput)
                }
            }
            return TextUtils.join(",", arrText)
        }

    /**
     * 타보험사 추가 보험사명 목록 반환 함수
     * @return      ArrayList<String>
    </String> */
    private val otherInsureCompanyNameMoreList: ArrayList<String>
        get() {
            val arrStr = ArrayList<String>()
            for (i in 0 until linOtherInsureMore!!.childCount) {
                val linItem = linOtherInsureMore!!.getChildAt(i) as LinearLayout
                val edt = linItem.findViewById<EditText>(R.id.editText)
                arrStr.add(edt.text.toString().trim())
            }
            return arrStr
        }

    /**
     * 타보험사 보험사명을 문자열로 반환 한다.<br></br>
     * 입력값이 empty 인 경우 제외 시킨다.
     * @return      String
     */
    private val strRealInsureCompanyName: String
        get() {
            val arrText = ArrayList<String?>()
            if (!TextUtils.isEmpty(edtRealInsure!!.text.toString().trim())) {
                arrText.add(edtRealInsure!!.text.toString().trim())
            }
            val arrMore = realInsureCompanyNameMoreList
            for (i in arrMore.indices) {
                val arrInput = arrMore[i]
                if (!TextUtils.isEmpty(arrInput)) {
                    arrText.add(arrInput)
                }
            }
            return TextUtils.join(",", arrText)
        }

    /**
     * 실손보험사 추가 보험사명 목록 반환 함수
     * @return      ArrayList<String>
    </String> */
    private val realInsureCompanyNameMoreList: ArrayList<String>
        get() {
            val tmp_arrStr = ArrayList<String>()
            for (i in 0 until linRealInsureMore!!.childCount) {
                val tmp_linItem = linRealInsureMore!!.getChildAt(i) as LinearLayout
                val tmp_edt = tmp_linItem.findViewById<EditText>(R.id.editText)
                tmp_arrStr.add(tmp_edt.text.toString().trim())
            }
            return tmp_arrStr
        }

    /**
     * 사용자 입력값 검사 함수
     * @return      boolean
     */
    private fun checkUserInput(): Boolean {
        var tmp_flagOk = true

        // -- 기타
        val temp_edtOther = edtOther!!.text.toString().trim()
        val temp_euckr_edtOther = FormatUtils.CF_checkUtf8toEucKr(temp_edtOther)
        if (switchCarInsure!!.CF_getCheckState() > 0 && TextUtils.isEmpty(edtCarInsure!!.text.toString().trim())) {  // 자동차보험 'Y' && 보험사명 미입력
            tmp_flagOk = false
            CommonFunction.CF_showCustomAlertDilaog(activity, resources.getString(R.string.dlg_empty_insure_company), resources.getString(R.string.btn_ok))
        } else if (switchJoinOther!!.CF_getCheckState() > 0 && TextUtils.isEmpty(strOtherInsureCompanyName)) {   // 타보험사 'Y' && 보험사명 미입력
            tmp_flagOk = false
            CommonFunction.CF_showCustomAlertDilaog(activity, resources.getString(R.string.dlg_empty_insure_company), resources.getString(R.string.btn_ok))
        } else if (switchJoinReal!!.CF_getCheckState() > 0 && TextUtils.isEmpty(strRealInsureCompanyName)) {     // 실손보험 'Y' && 보험사명 미입력
            tmp_flagOk = false
            CommonFunction.CF_showCustomAlertDilaog(activity, resources.getString(R.string.dlg_empty_insure_company), resources.getString(R.string.btn_ok))
        } else if (temp_edtOther.length > 100) {
            tmp_flagOk = false
            CommonFunction.CF_showCustomAlertDilaog(activity, resources.getString(R.string.dlg_max_insure_other), resources.getString(R.string.btn_ok))
        } else if ("" != temp_euckr_edtOther) {
            showCustomDialog(resources.getString(R.string.dlg_error_charset) + "(" + temp_euckr_edtOther + ")", (edtOther)!!)
            tmp_flagOk = false
        }
        return tmp_flagOk
    }
}