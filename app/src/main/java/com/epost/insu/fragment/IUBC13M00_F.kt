package com.epost.insu.fragment

import android.annotation.SuppressLint
import android.app.Activity
import android.app.DatePickerDialog
import android.app.DatePickerDialog.OnDateSetListener
import android.app.TimePickerDialog
import android.app.TimePickerDialog.OnTimeSetListener
import android.content.DialogInterface
import android.content.Intent
import android.database.sqlite.SQLiteDatabase
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.text.Editable
import android.text.InputType
import android.text.TextUtils
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.accessibility.AccessibilityEvent
import android.view.inputmethod.EditorInfo
import android.widget.*
import androidx.core.widget.NestedScrollView
import com.epost.insu.EnvConfig
import com.epost.insu.EnvConstant
import com.epost.insu.R
import com.epost.insu.SharedPreferencesFunc
import com.epost.insu.activity.IUII31M00
import com.epost.insu.activity.auth.IUPC80M00
import com.epost.insu.common.CommonFunction
import com.epost.insu.common.CustomSQLiteFunction
import com.epost.insu.common.CustomSQLiteHelper
import com.epost.insu.common.LogPrinter
import com.epost.insu.control.ButtonGroupView
import com.epost.insu.control.SwitchingControl
import com.epost.insu.data.Data_IUII10M00
import com.epost.insu.data.Data_IUII10M04_F
import com.epost.insu.data.Data_IUII10M05_F
import com.epost.insu.data.Data_IUII10M07_F
import com.epost.insu.dialog.CustomDialog
import com.epost.insu.event.OnChangedCheckedStateEventListener
import com.epost.insu.network.HttpConnections
import com.epost.insu.network.WeakReferenceHandler
import com.epost.insu.network.WeakReferenceHandler.ObjectHandlerMessage
import org.json.JSONException
import org.json.JSONObject
import java.util.*

/**
 * @copyright : 우정사업정보센터
 *
 * @project   : 모바일슈랑스 구축
 * @pakage    : com.epost.insu.fragment
 * @fileName  : IUBC13M00_F.java
 *
 * @Title     : 스마트보험금청구 > 보험청구내용작성 (화면 ID : IUBC13M00)
 * @author    : 이경민
 * @created   : 2018-06-17
 * @version   : 1.0
 *
 * @note      : <u>스마트보험금청구 > 보험청구내용작성 (화면 ID : IUBC13M00)</u><br></br>
 * 보험금지급청구 4단계 :: 보험청구내용작성 화면<br></br>
 * ======================================================================
 * 수정 내역
 * NO      날짜          작업자       내용
 * 01      2018-06-17    이경민       최초 등록
 * 02      2019-09-23    이경민       서울의료원 추가
 * 03      2019-11-11    이경민       서울의료원 추가수정
 * 1.5.4    NJM_20210506    [IUFC34M00 임시고객번호 삭제]
 * =======================================================================
 */
class IUBC13M00_F constructor() : IUBC10M00_FD(), ObjectHandlerMessage {
    private val maxLengthDiseaseName: Int = 40 // 진단명 max length
    private val maxLengthAccidentPlace: Int = 100 // 사고장소 max length
    private val maxLengthAccidentNote: Int = 100 // 사고경위 max length
    private var btnGroupCategory: ButtonGroupView? = null
    private var btnGroupReason: ButtonGroupView? = null
    private var btnGroupType_1: ButtonGroupView? = null
    private val subUrl_checkRealInsured: String = "/II/IUII15M00.do" // 실손보험 보유여부
    private val HANDLERJOB_CHECK_REALINSURED: Int = 4 // 실손보험 보유여부 핸들러
    private val HANDLERJOB_ERROR_CHECK_REALINSURED: Int = 5 // 실손보험 보유여부 핸들러
    private var linAccidentGroupView: LinearLayout? = null

    // 질병 관련
    private var edtDiseaseName //, edtDiseaseCode;
            : EditText? = null

    // 사고 관련
    private var textDate: TextView? = null
    private var edtAccidentPlace: EditText? = null
    private var edtAccidentNote: EditText? = null
    private var data: Data_IUII10M04_F? = null
    private var datePickerDialog: DatePickerDialog? = null
    private var timePickerDialog: TimePickerDialog? = null

    /* 고객정보 설정 */
    private var handler // 핸들러
            : WeakReferenceHandler? = null

    // 실손보험가입여부 관련
    private var isRealInsuJoined: Boolean = true
    private var isCalledCheckRealInsured: Boolean = false
    private val maxCountOtherInsure: Int = 4 // 타보험사 최대 수
    private var data2: Data_IUII10M05_F? = null
    private var textLabelSilson: TextView? = null
    private var switchJoinReal: SwitchingControl? = null
    private var linRealInsure: LinearLayout? = null
    private var linRealInsureMore: LinearLayout? = null
    private var edtRealInsure: EditText? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setInit()

        // 계좌정보 관련
        setInitAccountInfo()

        // 실손보험 청구여부 확인
        setInitRealInsuJoined()
    }

    @SuppressLint("InflateParams")
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.iubc13m00_f, null)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        if (savedInstanceState != null) {
            if (savedInstanceState.containsKey("data")) {
                data = savedInstanceState.getParcelable("data")
            }
            // 실손가입여부 관련
            if (savedInstanceState.containsKey("data2")) {
                data2 = savedInstanceState.getParcelable("data2")
            }
        }
        setUIControl()

        // 데이터 복구
        restoreData()

        // 계좌정보 관련
        setUIControlAccountInfo()
        restoreDataAccountInfo()
        if (savedInstanceState != null) {
            if (savedInstanceState.containsKey("dataAccountInfo")) {
                dataAccountInfo = savedInstanceState.getParcelable("dataAccountInfo")
            }
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        saveData()
        outState.putParcelable("data", data)
        outState.putParcelable("data2", data2)

        // 계좌정보 관련
        saveDataAccountInfo()
        outState.putParcelable("dataAccountInfo", dataAccountInfo)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        if (datePickerDialog != null && datePickerDialog!!.isShowing()) {
            datePickerDialog!!.dismiss()
        }
        if (timePickerDialog != null && timePickerDialog!!.isShowing()) {
            timePickerDialog!!.dismiss()
        }

        // 데이터 백업
        //saveData();
    }

    /**
     * 초기 세팅 함수
     */
    private fun setInit() {
        data = Data_IUII10M04_F()
        handler = WeakReferenceHandler(this)

        // 실손가입여부 관련
        data2 = Data_IUII10M05_F()

        // 초기 버튼 데이터 설정
        data!!.CF_setCategory(0)
        data!!.CF_setReason(0)
        val tmpArr: ArrayList<Boolean> = ArrayList()
        tmpArr.add(true)
        tmpArr.add(false)
        data!!.CF_setArrType_1(tmpArr)
    }

    /**
     * UI 생성 및 세팅 함수
     */
    private fun setUIControl() {

        // 청구유형 UI
        setUIOfCategory()

        // 발생원인 UI
        setUIOfReason()

        // 청구사유 UI
        setUIOfType()

        // 진단명 / 진별코드 입력 UI
        setUIOfDisease()

        // 사고관련 UI 세팅
        //setUIOfAccident();
        scrollView = getView()!!.findViewById<View>(R.id.scrollView) as NestedScrollView
        btnNext = getView()!!.findViewById<View>(R.id.btnFill) as Button
        btnNext!!.setText(getResources().getString(R.string.btn_next_2))
        btnNext!!.setOnClickListener(object : View.OnClickListener {
            override fun onClick(v: View) {

                // ---------------------------------------------------------------------------------
                //  키패드 가리기
                // ---------------------------------------------------------------------------------
                if (getActivity()!!.getCurrentFocus() != null) CommonFunction.CF_closeVirtualKeyboard(getActivity(), getActivity()!!.getCurrentFocus()!!.getWindowToken())
                if (checkUserInput()) {
                    // -----------------------------------------------------------------------------
                    //  Activity에 데이터 세팅 : submit에 사용하기 위한 데이터 세팅
                    // -----------------------------------------------------------------------------
                    val tmp_data: Data_IUII10M00? = mActivity!!.CF_getData()

                    /*
                    if(isRealInsuJoined == true) {   // 실손포함
                        tmp_data.CF_setS_insu_requ_type_code(EnvConfig.smartReqRealCategoryCode[btnGroupCategory.CF_getCheckdButtonFirstIndex()]);
                        tmp_data.CF_setStr_s_insu_requ_type_code(EnvConfig.smartReqRealCategoryName[btnGroupCategory.CF_getCheckdButtonFirstIndex()]);
                    } else {                        // 정액만
                        tmp_data.CF_setS_insu_requ_type_code(EnvConfig.smartReqCategoryCode[btnGroupCategory.CF_getCheckdButtonFirstIndex()]);
                        tmp_data.CF_setStr_s_insu_requ_type_code(EnvConfig.smartReqCategoryName[btnGroupCategory.CF_getCheckdButtonFirstIndex()]);
                    }
                    */tmp_data!!.CF_setS_insu_requ_type_code(EnvConfig.smartReqRealCategoryCode.get(1)) // 실손
                    tmp_data.CF_setStr_s_insu_requ_type_code(EnvConfig.smartReqRealCategoryName.get(1)) // 실손
                    tmp_data.CF_setS_requ_gent_caus_code(EnvConfig.smartReqReasonCode.get(0)) // 질병
                    tmp_data.CF_setStr_s_requ_gent_caus_code(EnvConfig.smartReqReasonName.get(0)) // 질병
                    tmp_data.CF_setS_insu_requ_resn_code(EnvConfig.smartReqTypeCode.get(0)) // 통원
                    tmp_data.CF_setStr_s_insu_requ_resn_code(EnvConfig.smartReqTypeName.get(0)) // 통원

                    // 스마트보험금청구 예외 - 상해인 경우 추가입력
                    /*
                    if(tmp_data.CF_getS_requ_gent_caus_code() == "1") {
                        tmp_data.CF_setS_acdt_date(data.CF_getAccidentYear() + data.CF_getAccidentMonth() + data.CF_getAccidentDayOfMonth());
                        tmp_data.CF_setS_acdt_time(data.CF_getAccidentHour() + data.CF_getAccidentMinute() + "00");
                        tmp_data.CF_setS_acdt_pace(data.CF_getAccidentPlace());
                        tmp_data.CF_setS_acdt_cntt(data.CF_getAccidentNote());
                    }
                    tmp_data.CF_setS_acdt_pace(edtAccidentPlace.getText().toString().trim());
                    tmp_data.CF_setS_acdt_cntt(edtAccidentNote.getText().toString().trim());
                    */tmp_data.CF_setS_dign_nm(edtDiseaseName!!.getText().toString().trim({ it <= ' ' })) // 진단명
                    ////tmp_data.CF_setS_sick_code_no(edtDiseaseCode.getText().toString().trim());

                    // 실손가입여부 관련, 나머지 데이터는 해당없음
                    tmp_data.CF_setS_car_insu_yn("N")
                    tmp_data.CF_setS_inds_dstr_insu_yn("N")
                    tmp_data.CF_setS_polc_decl_yn("N")
                    tmp_data.CF_setS_other_insu_comp_entr_yn("N")
                    tmp_data.CF_setS_rllo_entr_yn(if (switchJoinReal!!.CF_getCheckState() > 0) "Y" else "N")
                    tmp_data.CF_setS_rllo_insu_comp_nm(strRealInsureCompanyName)
                    btnNext!!.setEnabled(false)

                    // 계좌정보 관련
                    if (checkUserInputAccountInfo()) {
                        requestCehckBankAccount()
                    } else {
                        btnNext!!.setEnabled(true)
                    }

                    /* 스마트보험금청구 예외
                    // -----------------------------------------------------------------------------
                    //  다음페이지 이동 시간 Delay
                    // -----------------------------------------------------------------------------
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            mActivity.CF_showNextPage();
                            mActivity.CF_setVisibleStepIndicator(true);
                        }
                    }, 120L);
                    */
                }
            }
        })

        // 실손보험 가입내용
        setUIOfRealInsureJoin()
    }

    /**
     * 실손보험 가입 여부 UI 세팅 함수
     */
    private fun setUIOfRealInsureJoin() {
        linRealInsure = getView()!!.findViewById<View>(R.id.linAddRealInsure) as LinearLayout?
        linRealInsureMore = getView()!!.findViewById<View>(R.id.linAddRealInsureMore) as LinearLayout?
        val tmp_linInputOtherInsure: View = getView()!!.findViewById(R.id.linRealInsureInput)
        edtRealInsure = tmp_linInputOtherInsure.findViewById<View>(R.id.editText) as EditText?
        edtRealInsure!!.setPrivateImeOptions("defaultInputmode=korean")
        textLabelSilson = getView()!!.findViewById<View>(R.id.textLabelRealInsure) as TextView?
        switchJoinReal = getView()!!.findViewById<View>(R.id.switchingRealInsure) as SwitchingControl?
        switchJoinReal!!.CE_setOnChangedCheckedStateEventListener(object : OnChangedCheckedStateEventListener {
            override fun onCheck(p_flagCheck: Boolean) {

                // 실손가입여부 라벨 description 설정
                setRealInsureDesc(p_flagCheck)
                if (p_flagCheck) {
                    linRealInsure!!.setVisibility(View.VISIBLE)

                    // -----------------------------------------------------------------------------
                    //  접근성 On : 입력창 이동 확인 다이얼로그 show
                    // -----------------------------------------------------------------------------
                    if (CommonFunction.CF_checkAccessibilityTurnOn(getActivity())) {
                        showCustomDialog(getResources().getString(R.string.dlg_accessible_open_insure_inputbox), (edtRealInsure)!!)
                    } else {
                        CommonFunction.CF_showVirtualKeyboard(getActivity(), edtRealInsure)
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
                    if (getActivity()!!.hasWindowFocus()) {
                        if (getActivity()!!.getWindow().getCurrentFocus() != null) {
                            CommonFunction.CF_closeVirtualKeyboard(getActivity(), getActivity()!!.getCurrentFocus()!!.getWindowToken())
                        }
                    }
                    edtRealInsure!!.setText("")
                    linRealInsure!!.setVisibility(View.GONE)
                    linRealInsureMore!!.removeAllViews()

                    // -----------------------------------------------------------------------------
                    //  접근성 On : d실손가입여부 라벨로 포커스 이동
                    // -----------------------------------------------------------------------------
                    if (CommonFunction.CF_checkAccessibilityTurnOn(getActivity())) {
                        clearAllFocus()
                        textLabelSilson!!.setFocusableInTouchMode(true)
                        textLabelSilson!!.requestFocus()
                        textLabelSilson!!.setFocusableInTouchMode(false)
                    }
                }
            }
        })

        // 실손보험사 추가 버튼
        val tmp_btn: Button = getView()!!.findViewById<View>(R.id.btnAddCompany_2) as Button
        tmp_btn.setOnClickListener(object : View.OnClickListener {
            override fun onClick(v: View) {
                if (linRealInsureMore!!.getChildCount() < maxCountOtherInsure) {
                    createInputCompany(linRealInsureMore, "", object : View.OnClickListener {
                        override fun onClick(v: View) {
                            linRealInsureMore!!.removeView(v.getParent() as View?)

                            // -------------------------------------------------------------------------
                            //  접근성 On : 가입여부 라벨로 포커스 이동
                            // -------------------------------------------------------------------------
                            if (CommonFunction.CF_checkAccessibilityTurnOn(getActivity())) {
                                clearAllFocus()
                                textLabelSilson!!.setFocusableInTouchMode(true)
                                textLabelSilson!!.requestFocus()
                                textLabelSilson!!.setFocusableInTouchMode(false)
                            }
                        }
                    })

                    // ---------------------------------------------------------------------------------
                    //  접근성 On : 보험사명 입력 UI로 이동
                    // ---------------------------------------------------------------------------------
                    if (CommonFunction.CF_checkAccessibilityTurnOn(getActivity())) {
                        val tmp_lin: LinearLayout = linRealInsureMore!!.getChildAt(linRealInsureMore!!.getChildCount() - 1) as LinearLayout
                        showCustomDialog(getResources().getString(R.string.dlg_accessible_open_insure_inputbox), tmp_lin.findViewById(R.id.editText))
                    }
                }
            }
        })
    }

    /**
     * 데이터 복구 함수
     */
    private fun restoreData() {
        btnGroupCategory!!.CF_setCheck(data!!.CF_getCategory())
        btnGroupReason!!.CF_setCheck(data!!.CF_getReason())
        btnGroupType_1!!.CF_setCheck(data!!.CF_getArrType_1())
        edtDiseaseName!!.setText(data!!.CF_getDiseaseName())

        // 실손보험가입여부 복구
        val tmp_arrRealInsureMore: ArrayList<String> = data2!!.CF_getArrRealInsureMore()
        if (switchJoinReal!!.CF_getCheckState() > 0) {
            linRealInsure!!.setVisibility(View.VISIBLE)
            for (i in tmp_arrRealInsureMore.indices) {
                createInputCompany(linRealInsureMore, tmp_arrRealInsureMore.get(i), object : View.OnClickListener {
                    override fun onClick(v: View) {
                        linRealInsureMore!!.removeView(v.getParent() as View?)

                        // -------------------------------------------------------------------------
                        //  접근성 On : 실손가입여부 라벨로 포커스 이동
                        // -------------------------------------------------------------------------
                        if (CommonFunction.CF_checkAccessibilityTurnOn(getActivity())) {
                            clearAllFocus()
                            textLabelSilson!!.setFocusableInTouchMode(true)
                            textLabelSilson!!.requestFocus()
                            textLabelSilson!!.setFocusableInTouchMode(false)
                        }
                    }
                })
            }
        }
        switchJoinReal!!.CF_setCheck(data2!!.CF_getCheckStateJoinReal())
        setRealInsureDesc(data2!!.CF_getCheckStateJoinReal() > 0)
    }

    /**
     * 데이터 저장
     */
    private fun saveData() {
        data!!.CF_setCategory(btnGroupCategory!!.CF_getCheckdButtonFirstIndex())
        data!!.CF_setReason(btnGroupReason!!.CF_getCheckdButtonFirstIndex())
        data!!.CF_setArrType_1(btnGroupType_1!!.CF_getCheckdArray())
        data!!.CF_setDiseaseName(edtDiseaseName!!.getText().toString().trim({ it <= ' ' }))

        // 실손가입여부 관련
        data2!!.CF_setCheckStateJoinReal(switchJoinReal!!.CF_getCheckState())
    }

    /**
     * 청구유형 선택 UI 세팅 함수
     */
    private fun setUIOfCategory() {
        btnGroupCategory = getView()!!.findViewById<View>(R.id.btnGroupCategory) as ButtonGroupView?
        /*
        if(isRealInsuJoined == true){   // 실손포함
            btnGroupCategory.CF_setButtons(EnvConfig.smartReqRealCategoryName, new boolean[]{true,true,false});
        } else {                        // 정액만
            btnGroupCategory.CF_setButtons(EnvConfig.smartReqCategoryName, new boolean[]{true,false});
        }
        */btnGroupCategory!!.CF_setButtons(arrayOf(EnvConfig.smartReqRealCategoryName.get(1), ""), booleanArrayOf(true, false))
        btnGroupCategory!!.CF_setCheck(data!!.CF_getCategory())

        // 버튼 선택 비활성화
        btnGroupCategory!!.getArrTextView().get(0).setEnabled(false)
    }

    /**
     * 발생원인 UI 세팅 함수
     */
    private fun setUIOfReason() {
        btnGroupReason = getView()!!.findViewById<View>(R.id.btnGroupReason) as ButtonGroupView?
        btnGroupReason!!.CF_setButtons(arrayOf(EnvConfig.smartReqReasonName.get(0), ""), booleanArrayOf(true, false))
        btnGroupReason!!.CF_setCheck(data!!.CF_getReason())

        // 버튼 선택 비활성화
        btnGroupReason!!.getArrTextView().get(0).setEnabled(false)
    }

    /**
     * 청구사유 UI 세팅 함수
     */
    private fun setUIOfType() {
        val tmp_arrBtnGroup: Array<Array<String>?> = arrayOfNulls(1)
        val tmp_btnGroup: Array<String> = arrayOf(EnvConfig.smartReqTypeName.get(0), "")
        tmp_arrBtnGroup[0] = tmp_btnGroup
        btnGroupType_1 = getView()!!.findViewById<View>(R.id.btnGroupType_1) as ButtonGroupView?
        btnGroupType_1!!.CF_setMode(ButtonGroupView.SelectMode.MultipleSelect)
        btnGroupType_1!!.CF_setButtons(tmp_arrBtnGroup, booleanArrayOf(true, false))

        // 통원 데이터 설정
        btnGroupType_1!!.CF_setCheck(data!!.CF_getArrType_1()) // 통원

        // 버튼 선택 비활성화
        btnGroupType_1!!.getArrTextView().get(0).setEnabled(false)
    }

    /**
     * 진단명/질병코드 입력 UI
     */
    private fun setUIOfDisease() {
        val tmp_linDiseaseName: LinearLayout = getView()!!.findViewById<View>(R.id.labelEditDiseaseName) as LinearLayout // 진단명

        // 라벨 세팅
        val tmp_labelName: TextView = tmp_linDiseaseName.findViewById<View>(R.id.label) as TextView

        // 라벨 LayoutParam 설정
        val tmp_lpLabel: LinearLayout.LayoutParams = tmp_labelName.getLayoutParams() as LinearLayout.LayoutParams
        tmp_lpLabel.width = CommonFunction.CF_convertDipToPixel(getActivity()!!.getApplicationContext(), 75f)
        tmp_labelName.setLayoutParams(tmp_lpLabel)

        // 라벨 Pading 설정
        tmp_labelName.setPadding(CommonFunction.CF_convertDipToPixel(getActivity()!!.getApplicationContext(), 10f), tmp_labelName.getPaddingTop(), tmp_labelName.getPaddingRight(), tmp_labelName.getPaddingBottom())

        // 라벨 Text 설정
        tmp_labelName.setText(getResources().getString(R.string.label_disease_name_n))

        // 질병명 입력 EditText 설정
        edtDiseaseName = tmp_linDiseaseName.findViewById<View>(R.id.edit) as EditText?
        edtDiseaseName!!.setFilters(CommonFunction.CF_getInputLengthFilter(maxLengthDiseaseName))
        edtDiseaseName!!.setImeOptions(EditorInfo.IME_ACTION_NEXT)
        edtDiseaseName!!.setHint(R.string.hint_disease_name)
    }

    /**
     * 사고관련 UI 세팅 함수<br></br>
     * 사고일시 / 사고장소 / 사고경위
     */
    private fun setUIOfAccident() {
        linAccidentGroupView = getView()!!.findViewById<View>(R.id.linAccidentGroup) as LinearLayout?
        val tmp_linDate: LinearLayout = getView()!!.findViewById<View>(R.id.labelTextAccidentDate) as LinearLayout // 사고일시
        val tmp_linPlace: LinearLayout = getView()!!.findViewById<View>(R.id.labelEditAccidentPlace) as LinearLayout // 사고장소

        // 라벨 세팅
        val tmp_labelDate: TextView = tmp_linDate.findViewById<View>(R.id.label) as TextView
        val tmp_labelPlace: TextView = tmp_linPlace.findViewById<View>(R.id.label) as TextView
        val tmp_lpLabel: LinearLayout.LayoutParams = tmp_labelDate.getLayoutParams() as LinearLayout.LayoutParams
        tmp_lpLabel.width = CommonFunction.CF_convertDipToPixel(getActivity()!!.getApplicationContext(), 75f)
        tmp_labelDate.setLayoutParams(tmp_lpLabel)
        tmp_labelPlace.setLayoutParams(tmp_lpLabel)
        tmp_labelDate.setPadding(CommonFunction.CF_convertDipToPixel(getActivity()!!.getApplicationContext(), 10f), tmp_labelDate.getPaddingTop(), tmp_labelDate.getPaddingRight(), tmp_labelDate.getPaddingBottom())
        tmp_labelPlace.setPadding(CommonFunction.CF_convertDipToPixel(getActivity()!!.getApplicationContext(), 10f), tmp_labelPlace.getPaddingTop(), tmp_labelPlace.getPaddingRight(), tmp_labelPlace.getPaddingBottom())
        tmp_labelDate.setText(getResources().getString(R.string.label_accident_date_n))
        tmp_labelPlace.setText(getResources().getString(R.string.label_accident_place_n))

        // 사고일시 click 세팅
        textDate = tmp_linDate.findViewById<View>(R.id.text) as TextView?
        textDate!!.setClickable(true)
        textDate!!.setHint(R.string.hint_accident_date)
        textDate!!.setBackgroundResource(R.drawable.btn_bank_name_selector)
        textDate!!.setOnClickListener(object : View.OnClickListener {
            override fun onClick(v: View) {
                val tmp_calendar: Calendar = Calendar.getInstance()

                // 시간 선택 다이얼로그
                timePickerDialog = TimePickerDialog(getActivity(), object : OnTimeSetListener {
                    override fun onTimeSet(view: TimePicker, hourOfDay: Int, minute: Int) {
                        val tmp_year: String = "" + textDate!!.getTag(R.string.tag_year)
                        val tmp_month: String = String.format(Locale.getDefault(), "%02d", textDate!!.getTag(R.string.tag_month) as Int + 1)
                        val tmp_dayOfMonth: String = String.format(Locale.getDefault(), "%02d", textDate!!.getTag(R.string.tag_day_of_month) as Int?)
                        val tmp_hour: String = String.format(Locale.getDefault(), "%02d", hourOfDay)
                        val tmp_minute: String = String.format(Locale.getDefault(), "%02d", minute)
                        data!!.CF_setAccidentYear(tmp_year)
                        data!!.CF_setAccidentMonth(tmp_month)
                        data!!.CF_setAccidentDayOfMonth(tmp_dayOfMonth)
                        data!!.CF_setAccidentHour(tmp_hour)
                        data!!.CF_setAccidentMinute(tmp_minute)

                        // TextView 사고일시 TEXT  세팅
                        setTextAccidentDateTime(tmp_year, tmp_month, tmp_dayOfMonth, tmp_hour, tmp_minute)

                        // 사고장소 입력 EditText 포커싱및 키패드 보이기
                        if (edtAccidentPlace!!.getText().length == 0) {
                            edtAccidentPlace!!.postDelayed(object : Runnable {
                                override fun run() {
                                    CommonFunction.CF_showVirtualKeyboard(getActivity(), edtAccidentPlace)
                                }
                            }, 120)
                        }
                    }
                }, tmp_calendar.get(Calendar.HOUR_OF_DAY), tmp_calendar.get(Calendar.MINUTE), false)

                // 날짜 선택 다이얼로그
                datePickerDialog = DatePickerDialog((getActivity())!!, object : OnDateSetListener {
                    override fun onDateSet(view: DatePicker, year: Int, month: Int, dayOfMonth: Int) {
                        textDate!!.setTag(R.string.tag_year, year)
                        textDate!!.setTag(R.string.tag_month, month)
                        textDate!!.setTag(R.string.tag_day_of_month, dayOfMonth)
                        timePickerDialog!!.show()
                    }
                }, tmp_calendar.get(Calendar.YEAR), tmp_calendar.get(Calendar.MONTH), tmp_calendar.get(Calendar.DATE))
                datePickerDialog!!.show()
            }
        })

        // edittext 세팅
        edtAccidentPlace = tmp_linPlace.findViewById<View>(R.id.edit) as EditText?
        edtAccidentPlace!!.setFilters(CommonFunction.CF_getInputLengthFilter(maxLengthAccidentPlace))
        edtAccidentPlace!!.setHint(R.string.hint_accident_place)
        edtAccidentNote = getView()!!.findViewById<View>(R.id.edtAccidentNote) as EditText?
        edtAccidentNote!!.setFilters(CommonFunction.CF_getInputLengthFilter(maxLengthAccidentNote))
    }

    /**
     * 사고일시 Text 문자열 세팅 함수
     * @param p_year
     * @param p_month
     * @param p_DayOfMonth
     * @param p_hour
     * @param p_minute
     */
    private fun setTextAccidentDateTime(p_year: String, p_month: String, p_DayOfMonth: String, p_hour: String, p_minute: String) {
        textDate!!.setText("" + p_year + "." + p_month + "." + p_DayOfMonth + " " + p_hour + ":" + p_minute)
    }
    // 실손가입여부 관련
    /**
     * 실손가입여부 라벨 description 설정
     * @param p_flagYes
     */
    private fun setRealInsureDesc(p_flagYes: Boolean) {
        if (p_flagYes) {
            textLabelSilson!!.setContentDescription(getResources().getString(R.string.desc_silson_yes))
        } else {
            textLabelSilson!!.setContentDescription(getResources().getString(R.string.desc_silson_no))
        }
    }

    /**
     * 보험사명 입력 UI 생성 및 세팅
     * @param p_parentView
     * @param p_strText
     * @param p_onClickRemove
     * @return
     */
    private fun createInputCompany(p_parentView: LinearLayout?, p_strText: String, p_onClickRemove: View.OnClickListener) {
        val tmp_linItem: LinearLayout = View.inflate(getActivity(), R.layout.c_input_other_company, null) as LinearLayout
        tmp_linItem.setPadding(0, CommonFunction.CF_convertDipToPixel(getActivity()!!.getApplicationContext(), 5f), 0, 0)
        val tmp_edt: EditText = tmp_linItem.findViewById<View>(R.id.editText) as EditText
        tmp_edt.setText(p_strText)
        tmp_edt.setPrivateImeOptions("defaultInputmode=korean")
        val tmp_btnRemove: ImageButton = tmp_linItem.findViewById<View>(R.id.btnDel) as ImageButton
        tmp_btnRemove.setOnClickListener(p_onClickRemove)
        tmp_btnRemove.setVisibility(View.VISIBLE)
        p_parentView!!.addView(tmp_linItem)
    }

    /**
     * 타보험사 보험사명을 문자열로 반환 한다.<br></br>
     * 입력값이 empty 인 경우 제외 시킨다.
     * @return
     */
    private val strRealInsureCompanyName: String
        private get() {
            val tmp_arrText: ArrayList<String?> = ArrayList()
            if (TextUtils.isEmpty(edtRealInsure!!.getText().toString().trim({ it <= ' ' })) == false) {
                tmp_arrText.add(edtRealInsure!!.getText().toString().trim({ it <= ' ' }))
            }
            val tmp_arrMore: ArrayList<String> = realInsureCompanyNameMoreList
            for (i in tmp_arrMore.indices) {
                val tmp_arrInput: String = tmp_arrMore.get(i)
                if (TextUtils.isEmpty(tmp_arrInput) == false) {
                    tmp_arrText.add(tmp_arrInput)
                }
            }
            return TextUtils.join(",", tmp_arrText)
        }

    /**
     * 실손보험사 추가 보험사명 목록 반환 함수
     * @return
     */
    private val realInsureCompanyNameMoreList: ArrayList<String>
        private get() {
            val tmp_arrStr: ArrayList<String> = ArrayList()
            for (i in 0 until linRealInsureMore!!.getChildCount()) {
                val tmp_linItem: LinearLayout = linRealInsureMore!!.getChildAt(i) as LinearLayout
                val tmp_edt: EditText = tmp_linItem.findViewById<View>(R.id.editText) as EditText
                tmp_arrStr.add(tmp_edt.getText().toString().trim({ it <= ' ' }))
            }
            return tmp_arrStr
        }

    /**
     * 기타 내용 입력 포커스 이동 제한<br></br>
     * 특정행위 뒤 접근성 포커스 강제 이동을 막기 위함.<br></br>
     * 약간의 시간 딜레이 뒤에 원복한다.
     */
    private fun interceptEditTextOtherFocus() {
        if (CommonFunction.CF_checkAccessibilityTurnOn(getActivity())) {
            val tmp_lin: LinearLayout = getView()!!.findViewById<View>(R.id.linOtherInput) as LinearLayout
            tmp_lin.setDescendantFocusability(ViewGroup.FOCUS_BLOCK_DESCENDANTS)
            Handler().postDelayed(object : Runnable {
                override fun run() {
                    tmp_lin.setDescendantFocusability(ViewGroup.FOCUS_BEFORE_DESCENDANTS)
                }
            }, 500)
        }
    }// 청구사유 row 1

    /**
     * 선택한 청구사유 코드를 문자열로 반환한다.
     * @return
     */
    private val strCodeReason: String
        private get() {
            val tmp_arrText: ArrayList<String?> = ArrayList()
            val tmp_arrGroup_1: ArrayList<Boolean> = btnGroupType_1!!.CF_getCheckdArray()

            // 청구사유 row 1
            for (i in tmp_arrGroup_1.indices) {
                if (tmp_arrGroup_1.get(i)) {
                    tmp_arrText.add(EnvConfig.smartReqTypeCode.get(i))
                }
            }
            return TextUtils.join(",", tmp_arrText)
        }// 청구사유 row 1

    /**
     * 선택한 청구사유 TEXT를 반환한다.
     * @return
     */
    private val strNameReason: String
        private get() {
            val tmp_arrText: ArrayList<String?> = ArrayList()
            val tmp_arrGroup_1: ArrayList<Boolean> = btnGroupType_1!!.CF_getCheckdArray()

            // 청구사유 row 1
            for (i in tmp_arrGroup_1.indices) {
                if (tmp_arrGroup_1.get(i)) {
                    tmp_arrText.add(EnvConfig.smartReqTypeName.get(i))
                }
            }
            return TextUtils.join(",", tmp_arrText)
        }

    /**
     * 다이얼로그 팝업 후 접근성 포커스 이동
     * @param p_message
     * @param p_view
     */
    private fun showCustomDlgAndAccessibilityFocus(p_message: String, p_view: View?) {

        // -----------------------------------------------------------------------------------------
        //  StepIndicator hide 상태에서 AccessibilityEvent.TYPE_VIEW_FOCUSED 가 EditText 대상으로
        //  제대로 동작하지 않음.
        // -----------------------------------------------------------------------------------------
        mActivity!!.CF_setVisibleStepIndicator(true)
        val tmp_dlg: CustomDialog = CustomDialog((getActivity())!!)
        tmp_dlg.show()
        tmp_dlg.CF_setBackKeyMode(CustomDialog.BackMode.NOTUSE)
        tmp_dlg.CF_setTextContent(p_message)
        tmp_dlg.CF_setSingleButtonText(getResources().getString(R.string.btn_ok))
        tmp_dlg.setOnDismissListener(object : DialogInterface.OnDismissListener {
            override fun onDismiss(dialog: DialogInterface) {
                Handler().postDelayed(object : Runnable {
                    override fun run() {
                        p_view!!.sendAccessibilityEvent(AccessibilityEvent.TYPE_VIEW_FOCUSED)
                    }
                }, 500)
            }
        })
    }

    /**
     * 사용자 입력 값 검사 함수
     * @return
     */
    private fun checkUserInput(): Boolean {
        var tmp_flagOk: Boolean = true
        val tmp_flagOnAccibility: Boolean = CommonFunction.CF_checkAccessibilityTurnOn(getActivity())
        if (btnGroupCategory!!.CF_isChecked() == false) {        // 청구유형 검사
            tmp_flagOk = false
            mActivity!!.CF_setVisibleStepIndicator(true)
            showCustomDialog(getResources().getString(R.string.dlg_select_category), getView()!!.findViewById(R.id.textCategory))
        } else if (TextUtils.isEmpty(edtDiseaseName!!.getText().toString().trim({ it <= ' ' }))) {
            tmp_flagOk = false
            mActivity!!.CF_setVisibleStepIndicator(true)
            showCustomDialog(getResources().getString(R.string.dlg_empty_disease_name), (edtDiseaseName)!!)
        } else if (switchJoinReal!!.CF_getCheckState() > 0 && TextUtils.isEmpty(strRealInsureCompanyName)) {     // 실손보험 'Y' && 보험사명 미입력
            tmp_flagOk = false
            CommonFunction.CF_showCustomAlertDilaog(getActivity(), getResources().getString(R.string.dlg_empty_insure_company), getResources().getString(R.string.btn_ok))
        }
        return tmp_flagOk
    }

    // 계좌정보 설정
    override fun handleMessage(p_message: Message) {
        if (mActivity != null && mActivity!!.isDestroyed() == false) {
            mActivity!!.CF_dismissProgressDialog()
            when (p_message.what) {
                HANDLERJOB_CHECK -> {
                    if (btnNext != null) {
                        btnNext!!.setEnabled(true)
                    }
                    try {
                        setResultOfCheckBankAccount(JSONObject(p_message.obj as String?))
                    } catch (e: JSONException) {
                        LogPrinter.CF_line()
                        LogPrinter.CF_debug(getResources().getString(R.string.log_json_exception))
                    }
                }
                HANDLERJOB_ERROR_CHECK -> {
                    if (btnNext != null) {
                        btnNext!!.setEnabled(true)
                    }
                    mActivity!!.CF_dismissProgressDialog()
                    CommonFunction.CF_showCustomAlertDilaog(getActivity(), p_message.obj as String?, getResources().getString(R.string.btn_ok))
                }
                HANDLERJOB_CHECK_REALINSURED -> try {
                    setResultOfCheckRealInsured(JSONObject(p_message.obj as String?))
                } catch (e: JSONException) {
                    LogPrinter.CF_line()
                    LogPrinter.CF_debug(getResources().getString(R.string.log_json_exception))
                }
                HANDLERJOB_ERROR_CHECK_REALINSURED -> {
                    mActivity!!.CF_dismissProgressDialog()
                    CommonFunction.CF_showCustomAlertDilaog(getActivity(), p_message.obj as String?, getResources().getString(R.string.btn_ok))
                }
                else -> {
                }
            }
        }
    }

    // 계좌정보 관련
    private val subUrl_bankInfo: String = "/II/IUII30M00.do" // 고객이 입력한계좌 정합성 체크 url
    private val maxlengthBankAccount: Int = 14
    private val maxLengthName: Int = 20
    private val HANDLERJOB_CHECK: Int = 2 // 고객이 입력한계좌 정합성 체크
    private val HANDLERJOB_ERROR_CHECK: Int = 3 // 고객이 입력한계좌 정합성 체크 에러
    private var textBankName: TextView? = null
    private var textBankOwner: TextView? = null
    private var edtBankAccount: EditText? = null
    private var dataAccountInfo: Data_IUII10M07_F? = null
    override fun onActivityResult(p_requestCode: Int, p_resultCode: Int, p_data: Intent?) {
        super.onActivityResult(p_requestCode, p_resultCode, p_data)
        if (p_requestCode == EnvConfig.REQUESTCODE_ACTIVITY_CHOICE_BANK && p_resultCode == Activity.RESULT_OK) {
            if ((p_data != null) && p_data.hasExtra("code") && p_data.hasExtra("name")) {
                val tmp_bankCode: String? = p_data.getExtras()!!.getString("code")
                val tmp_bankName: String? = p_data.getExtras()!!.getString("name")

                // 고객이 선택한 금융기관 이름 & 코드 세팅
                textBankName!!.setText(tmp_bankName)
                textBankName!!.setTag(tmp_bankCode)
                textBankName!!.setContentDescription("선택창, " + textBankName!!.getText() + "선택됨, 선택하시려면 두번 누르세요.")


                // ---------------------------------------------------------------------------------
                //  계좌번호 입력 EditText 포커스 이동
                //  접근성 On : 접근성 포커스 요청
                // ---------------------------------------------------------------------------------
                Handler().postDelayed(object : Runnable {
                    override fun run() {
                        CommonFunction.CF_showVirtualKeyboard(getActivity(), edtBankAccount)
                        if (CommonFunction.CF_checkAccessibilityTurnOn(getActivity())) {
                            edtBankAccount!!.sendAccessibilityEvent(AccessibilityEvent.TYPE_VIEW_FOCUSED)
                        }
                    }
                }, 250)
            }
        } else if (p_requestCode == EnvConfig.REQUESTCODE_ACTIVITY_IUPC80M00 && p_resultCode == Activity.RESULT_OK) {
            if ((p_data != null) && p_data.hasExtra("plainText") && p_data.hasExtra("certificateIndex")) {
                mActivity!!.CF_setCertificateData(p_data.getExtras()!!.getString("plainText"), p_data.getExtras()!!.getInt("certificateIndex"))
            }
            mActivity!!.CF_showNextPage()
        }
    }

    /**
     * 초기 세팅 함수
     */
    private fun setInitAccountInfo() {
        dataAccountInfo = Data_IUII10M07_F()
    }

    /**
     * 초기 세팅 함수(실손보험 가입여부)
     */
    private fun setInitRealInsuJoined() {
        requestCheckRealInsured()
    }

    /**
     * UI 생성 및 세팅 함수
     */
    private fun setUIControlAccountInfo() {
        val tmp_linBankName: LinearLayout = getView()!!.findViewById<View>(R.id.labelTextBank) as LinearLayout // 금융기관
        val tmp_linBankOwner: LinearLayout = getView()!!.findViewById<View>(R.id.labelTextName) as LinearLayout // 예금주
        val tmp_linBankNumber: LinearLayout = getView()!!.findViewById<View>(R.id.labelEditBankNumber) as LinearLayout // 계좌번호

        // 라벨 세팅
        val tmp_labelBankName: TextView = tmp_linBankName.findViewById<View>(R.id.label) as TextView
        val tmp_labelName: TextView = tmp_linBankOwner.findViewById<View>(R.id.label) as TextView
        val tmp_labelBankNumber: TextView = tmp_linBankNumber.findViewById<View>(R.id.label) as TextView
        tmp_labelBankName.setText(getResources().getString(R.string.label_bank_name_n))
        tmp_labelName.setText(getResources().getString(R.string.label_bank_owner))
        tmp_labelBankNumber.setText(getResources().getString(R.string.label_bank_account_n))

        // 금융기관 click 세팅
        textBankName = tmp_linBankName.findViewById<View>(R.id.text) as TextView?
        textBankName!!.setClickable(true)
        textBankName!!.setHint(getResources().getString(R.string.hint_bank_company))
        textBankName!!.setContentDescription("선택창, " + getResources().getString(R.string.hint_bank_company) + ", 선택하시려면 두번 누르세요.")
        textBankName!!.setTag("")
        textBankName!!.setBackgroundResource(R.drawable.btn_bank_name_selector)
        textBankName!!.setOnClickListener(object : View.OnClickListener {
            override fun onClick(v: View) {

                // ---------------------------------------------------------------------------------
                //  Accessible On 상태 : 팝업 안내 후 이동
                // ---------------------------------------------------------------------------------
                if (CommonFunction.CF_checkAccessibilityTurnOn(getActivity())) {
                    val tmp_dlg: CustomDialog = CustomDialog((getActivity())!!)
                    tmp_dlg.show()
                    tmp_dlg.CF_setBackKeyMode(CustomDialog.BackMode.CANCELED)
                    tmp_dlg.CF_setTextContent(getResources().getString(R.string.dlg_accessible_move_iuii31m00))
                    tmp_dlg.CF_setDoubleButtonText(getResources().getString(R.string.btn_cancel), getResources().getString(R.string.btn_ok))
                    tmp_dlg.setOnDismissListener(object : DialogInterface.OnDismissListener {
                        override fun onDismiss(dialog: DialogInterface) {
                            if ((dialog as CustomDialog).CF_getCanceled() == false) {
                                startBankListActivity()
                            } else {
                                Handler().postDelayed(object : Runnable {
                                    override fun run() {
                                        textBankName!!.sendAccessibilityEvent(AccessibilityEvent.TYPE_VIEW_FOCUSED)
                                    }
                                }, 500)
                            }
                        }
                    })
                } else {
                    startBankListActivity()
                }
            }
        })

        // 예금주
        textBankOwner = tmp_linBankOwner.findViewById<View>(R.id.text) as TextView?
        textBankOwner!!.setText(user_name_fromSqlite)
        edtBankAccount = tmp_linBankNumber.findViewById<View>(R.id.edit) as EditText?
        edtBankAccount!!.setFilters(CommonFunction.CF_getInputLengthFilter(maxlengthBankAccount))
        edtBankAccount!!.setHint(getResources().getString(R.string.hint_bank_account))
        edtBankAccount!!.setInputType(InputType.TYPE_CLASS_NUMBER)
        edtBankAccount!!.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                if (edtBankAccount!!.getText().length == maxlengthBankAccount) {
                    CommonFunction.CF_closeVirtualKeyboard(getActivity(), edtBankAccount!!.getWindowToken())
                }
            }

            override fun afterTextChanged(s: Editable) {}
        })
    }

    /**
     * 데이터 저장
     */
    private fun saveDataAccountInfo() {

        // 계좌번호 , 예금주 백업 (금융기관 정보는 금융기관 선택시 저장함)
        dataAccountInfo!!.setBankCode(textBankName!!.getTag() as String?)
        dataAccountInfo!!.setBankName(textBankName!!.getText().toString().trim({ it <= ' ' }))
        //data.CF_setBankAccount(edtBankAccount.getText().toString().trim());
    }

    /**
     * 데이터 복구
     */
    private fun restoreDataAccountInfo() {
        textBankName!!.setText(dataAccountInfo!!.getBankName())
        textBankName!!.setTag(dataAccountInfo!!.getBankCode())
        if (TextUtils.isEmpty(textBankName!!.getText().toString())) {
            textBankName!!.setContentDescription("선택창, " + getResources().getString(R.string.hint_bank_company) + ", 선택하시려면 두번 누르세요.")
        } else {
            textBankName!!.setContentDescription("선택창, " + textBankName!!.getText() + "선택됨, 선택하시려면 두번 누르세요.")
        }
        //textBankOwner.setText(data.CF_getBankOwner());
    }

    /**
     * 사용자 입력 값 검사 함수<br></br>
     * 계좌정보확인 전 호출되는 함수<br></br>
     * @return
     */
    private fun checkUserInputAccountInfo(): Boolean {
        var tmp_flagOk: Boolean = true
        val tmp_flagOnAccibility: Boolean = CommonFunction.CF_checkAccessibilityTurnOn(getActivity())
        if (textBankName!!.getText().toString().trim({ it <= ' ' }).length == 0) {
            tmp_flagOk = false
            if (tmp_flagOnAccibility) {
                showCustomDlgAndAccessibilityFocus(getResources().getString(R.string.dlg_no_select_bank), textBankName)
            } else {
                CommonFunction.CF_showCustomAlertDilaog(getActivity(), getResources().getString(R.string.dlg_no_select_bank), getResources().getString(R.string.btn_ok))
            }
        } else if (edtBankAccount!!.getText().toString().length == 0) {
            tmp_flagOk = false
            if (tmp_flagOnAccibility) {
                val tmp_edtAccount: EditText = getView()!!.findViewById<View>(R.id.relKeyInput).findViewById<View>(R.id.editText) as EditText
                showCustomDlgAndAccessibilityFocus(getResources().getString(R.string.dlg_no_input_bank_account), tmp_edtAccount)
            } else {
                CommonFunction.CF_showCustomAlertDilaog(getActivity(), getResources().getString(R.string.dlg_no_input_bank_account), getResources().getString(R.string.btn_ok))
            }
        }
        return tmp_flagOk
    }
    // #############################################################################################
    //  Sqlite
    // #############################################################################################
    /**
     * 단말기 DB에 저장되어 있는 사용자 이름 반환 함수
     * @return
     */
    private val user_name_fromSqlite: String
        private get() {
            var tmp_name: String = ""
            val tmp_helper: CustomSQLiteHelper = CustomSQLiteHelper(getActivity()!!.getApplicationContext())
            val tmp_sqlite: SQLiteDatabase = tmp_helper.getReadableDatabase()
            tmp_name = tmp_helper.CF_SelectUserName(tmp_sqlite)
            tmp_sqlite.close()
            tmp_helper.close()
            return tmp_name
        }
    // #############################################################################################
    //  Activity 호출
    // #############################################################################################
    /**
     * IUII31M00 금융기관 선택 Activity 호출 함수
     */
    private fun startBankListActivity() {
        val tmp_intent: Intent = Intent(getActivity(), IUII31M00::class.java)
        tmp_intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
        startActivityForResult(tmp_intent, EnvConfig.REQUESTCODE_ACTIVITY_CHOICE_BANK)
    }

    /**
     * 공동인증 본인인증 Activity 호출 함수
     */
    private fun startIUPC80M00() {
        val tmp_intent: Intent = Intent(getActivity(), IUPC80M00::class.java)
        tmp_intent.putExtra(EnvConstant.KEY_INTENT_AUTH_CSNO, CustomSQLiteFunction.getLastLoginCsno(mActivity))
        tmp_intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
        startActivityForResult(tmp_intent, EnvConfig.REQUESTCODE_ACTIVITY_IUPC80M00)
    }
    // #############################################################################################
    //  Http 관련
    // #############################################################################################
    /**
     * 계좌정보 확인
     */
    private fun requestCehckBankAccount() {

        // 현재 포커싱 중인 VIew의 키패드 닫기기
        if (getActivity()!!.hasWindowFocus()) {
            if (getActivity()!!.getWindow().getCurrentFocus() != null) {
                CommonFunction.CF_closeVirtualKeyboard(getActivity(), getActivity()!!.getWindow().getCurrentFocus()!!.getWindowToken())
            }
        }
        mActivity!!.CF_showProgressDialog()
        val tmp_builder: Uri.Builder = Uri.Builder()
        tmp_builder.appendQueryParameter("mode", "check")
        tmp_builder.appendQueryParameter("bankCode", textBankName!!.getTag() as String?)
        tmp_builder.appendQueryParameter("account", edtBankAccount!!.getText().toString().trim({ it <= ' ' }))
        tmp_builder.appendQueryParameter("owner", textBankOwner!!.getText().toString().trim({ it <= ' ' }))
        HttpConnections.sendPostData(
                EnvConfig.host_url + subUrl_bankInfo,
                tmp_builder.build().getEncodedQuery(),
                handler,
                HANDLERJOB_CHECK,
                HANDLERJOB_ERROR_CHECK)
    }

    /**
     * 계좌정보확인 요청 결과 처리 함수
     * @param p_jsonObject
     */
    @Throws(JSONException::class)
    private fun setResultOfCheckBankAccount(p_jsonObject: JSONObject) {
        mActivity!!.CF_dismissProgressDialog()
        val jsonKey_errorCode: String = "errCode"
        val jsonKey_flagOk: String = "flagOk"
        var tmp_errorCode: String = ""
        if (p_jsonObject.has(jsonKey_errorCode)) {
            tmp_errorCode = p_jsonObject.getString(jsonKey_errorCode)
            if ((tmp_errorCode == "ERRIUII30M00001")) {             // 전문 오류
                CommonFunction.CF_showCustomAlertDilaog(getActivity(), getResources().getString(R.string.dlg_error_erriuii30m00001), getResources().getString(R.string.btn_ok))
            } else if ((tmp_errorCode == "ERRIUII30M00002")) {       // 예금주 다름
                CommonFunction.CF_showCustomAlertDilaog(getActivity(), getResources().getString(R.string.dlg_error_erriuii30m00002), getResources().getString(R.string.btn_ok))
            } else if ((tmp_errorCode == "ERRIUII30M00003")) {       // 상대은행 처리 지연(전문 에러 EEA683)
                CommonFunction.CF_showCustomAlertDilaog(getActivity(), getResources().getString(R.string.dlg_error_erriuii30m00003), getResources().getString(R.string.btn_ok))
            } else if (TextUtils.isEmpty(tmp_errorCode)) {

                // 에러코드 empty 이면 성공
                val tmp_data: Data_IUII10M00? = mActivity!!.CF_getData()
                tmp_data!!.CF_setS_fnis_nm(textBankName!!.getText().toString().trim({ it <= ' ' }))
                tmp_data.CF_setS_fnis_code(textBankName!!.getTag() as String?)
                tmp_data.CF_setS_acno(edtBankAccount!!.getText().toString().trim({ it <= ' ' }))
                tmp_data.CF_setDecode_s_acno(edtBankAccount!!.getText().toString().trim({ it <= ' ' }))
                tmp_data.CF_setS_dpow_nm(textBankOwner!!.getText().toString().trim({ it <= ' ' }))

                // ---------------------------------------------------------------------------------
                //  계좌인증 완료 Dialog 팝업 후 -> 본인인증(IUPC80M00) 호출
                // ---------------------------------------------------------------------------------
                val tmp_dlg: CustomDialog = CustomDialog((getActivity())!!)
                tmp_dlg.show()
                tmp_dlg.CF_setTextContent(getResources().getString(R.string.dlg_ok_bankcode))
                tmp_dlg.CF_setSingleButtonText(getResources().getString(R.string.btn_ok))
                tmp_dlg.setOnDismissListener(object : DialogInterface.OnDismissListener {
                    override fun onDismiss(dialog: DialogInterface) {
                        startIUPC80M00()
                    }
                })
            }
        } else {
            CommonFunction.CF_showCustomAlertDilaog(getActivity(), getResources().getString(R.string.dlg_error_server_1), getResources().getString(R.string.btn_ok))
        }
    }

    /**
     * 실손보험 가입여부 확인
     */
    fun requestCheckRealInsured() {
        if (isCalledCheckRealInsured == true) {
            return
        }
        if (getActivity()!!.hasWindowFocus()) {
            if (getActivity()!!.getWindow().getCurrentFocus() != null) {
                CommonFunction.CF_closeVirtualKeyboard(getActivity(), getActivity()!!.getWindow().getCurrentFocus()!!.getWindowToken())
            }
        }
        mActivity!!.CF_showProgressDialog()
        val tmp_builder: Uri.Builder = Uri.Builder()
        tmp_builder.appendQueryParameter("csno", user_csno_fromSqlite)
        tmp_builder.appendQueryParameter("tempKey", SharedPreferencesFunc.getWebTempKey(getActivity()))
        HttpConnections.sendPostData(
                EnvConfig.host_url + subUrl_checkRealInsured,
                tmp_builder.build().getEncodedQuery(),
                handler,
                HANDLERJOB_CHECK_REALINSURED,
                HANDLERJOB_ERROR_CHECK_REALINSURED)
    }

    /**
     * 실손보험 보유여부 결과 처리 함수
     * @param p_jsonObject
     */
    @Throws(JSONException::class)
    private fun setResultOfCheckRealInsured(p_jsonObject: JSONObject) {
        mActivity!!.CF_dismissProgressDialog()
        val jsonKey_errorCode: String = "errCode"
        val jsonKey_data: String = "data"
        val jsonKey_s_actl_insu_yn: String = "s_actl_insu_yn"
        var tmp_errorCode: String = ""
        var tmp_s_actl_insu_yn: String = ""
        if (p_jsonObject.has(jsonKey_errorCode)) {
            tmp_errorCode = p_jsonObject.getString(jsonKey_errorCode)
            if ((tmp_errorCode == "ERRIUII30M00001")) {             // 전문 오류
                CommonFunction.CF_showCustomAlertDilaog(getActivity(), getResources().getString(R.string.dlg_error_erriuii30m00001), getResources().getString(R.string.btn_ok))
            } else if ((tmp_errorCode == "ERRIUII30M00002")) {       // 예금주 다름
                CommonFunction.CF_showCustomAlertDilaog(getActivity(), getResources().getString(R.string.dlg_error_erriuii30m00002), getResources().getString(R.string.btn_ok))
            } else if ((tmp_errorCode == "ERRIUII30M00003")) {       // 상대은행 처리 지연(전문 에러 EEA683)
                CommonFunction.CF_showCustomAlertDilaog(getActivity(), getResources().getString(R.string.dlg_error_erriuii30m00003), getResources().getString(R.string.btn_ok))
            } else if (TextUtils.isEmpty(tmp_errorCode)) {
                if (p_jsonObject.has(jsonKey_data)) {
                    val tmp_data: JSONObject = p_jsonObject.getJSONObject(jsonKey_data)
                    if (tmp_data.has(jsonKey_s_actl_insu_yn)) {
                        tmp_s_actl_insu_yn = tmp_data.getString(jsonKey_s_actl_insu_yn)
                        if ((tmp_s_actl_insu_yn == "Y")) {
                            isRealInsuJoined = true
                            isCalledCheckRealInsured = true
                        } else if ((tmp_s_actl_insu_yn == "N")) {
                            isRealInsuJoined = false
                            isCalledCheckRealInsured = true
                        } else if ((tmp_s_actl_insu_yn == "E")) {
                            isCalledCheckRealInsured = true
                            CommonFunction.CF_showCustomAlertDilaog(getActivity(), getResources().getString(R.string.dlg_error_server_3), getResources().getString(R.string.btn_ok))
                        } else {
                            CommonFunction.CF_showCustomAlertDilaog(getActivity(), getResources().getString(R.string.dlg_error_server_4), getResources().getString(R.string.btn_ok))
                        }
                    } else {
                        CommonFunction.CF_showCustomAlertDilaog(getActivity(), getResources().getString(R.string.dlg_error_server_4), getResources().getString(R.string.btn_ok))
                    }
                } else {
                    CommonFunction.CF_showCustomAlertDilaog(getActivity(), getResources().getString(R.string.dlg_error_server_4), getResources().getString(R.string.btn_ok))
                }
            } else {
                CommonFunction.CF_showCustomAlertDilaog(getActivity(), getResources().getString(R.string.dlg_error_server), getResources().getString(R.string.btn_ok))
            }
        } else {
            CommonFunction.CF_showCustomAlertDilaog(getActivity(), getResources().getString(R.string.dlg_error_server_1), getResources().getString(R.string.btn_ok))
        }
    }

    /**
     * DB에 저장되어 있는 고객식별코드 csno 반환 함수
     * @return
     */
    private val user_csno_fromSqlite: String
        private get() {
            var tmp_csno: String = ""
            val tmp_helper: CustomSQLiteHelper = CustomSQLiteHelper(getActivity()!!.getApplicationContext())
            val tmp_sqlite: SQLiteDatabase = tmp_helper.getReadableDatabase()
            tmp_csno = tmp_helper.CF_Selectcsno(tmp_sqlite)
            tmp_sqlite.close()
            tmp_helper.close()
            return tmp_csno
        }
}