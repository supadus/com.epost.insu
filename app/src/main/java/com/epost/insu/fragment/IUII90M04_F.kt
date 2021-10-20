package com.epost.insu.fragment

import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.os.Handler
import android.text.TextUtils
import android.view.*
import android.view.accessibility.AccessibilityEvent
import android.view.inputmethod.EditorInfo
import android.widget.*
import com.epost.insu.EnvConfig
import com.epost.insu.R
import com.epost.insu.SharedPreferencesFunc
import com.epost.insu.common.*
import com.epost.insu.control.ButtonGroupView
import com.epost.insu.data.Data_IUII10M00
import com.epost.insu.data.Data_IUII10M04_F
import com.epost.insu.dialog.CustomDialog
import com.epost.insu.event.OnSelectedChangeEventListener
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import java.util.*

/**
 * 보험금청구 > 자녀청구 > 4단계. 보험청구서작성(청구내용1)
 * @since     :
 * @version   : 1.4
 * @author    : YJH
 * @see
 * <pre>
 * ======================================================================
 * 0.0.0    YJH_20181109    최초 등록
 * 0.0.0    NJM_20191208    청구시 정액+실손 추가
 * 0.0.0    NJM_20200207    문자셋 오류 검사 기능 추가 (사고내용,사고경위,진단명)
 * 1.5.6    NJM_20210528    [청구서서식변경] 청구사유변경(깁스삭제,기타 추가 및 선택 금지)
 * 1.6.2    NJM_20210802    [2021년 대우 취약점] 2차본 : 부적절한 예외 처리
 * =======================================================================
 * copyright : (주)프로니스정보기술 | http://www.phronis.co.kr
 * </pre>
 */
class IUII90M04_F : IUII90M00_FD() {
    private val maxLengthDiseaseName = 40 // 진단명 max length
    private val maxLengthDiseaseCode = 5 // 진단코드 max length
    private val maxLengthAccidentPlace = 100 // 사고장소 max length
    private val maxLengthAccidentNote = 100 // 사고경위 max length
    private var btnGroupCategory: ButtonGroupView? = null
    private var btnGroupReason: ButtonGroupView? = null
    private var btnGroupType_1: ButtonGroupView? = null
    private var linAccidentGroupView: LinearLayout? = null

    // 질병 관련
    private var edtDiseaseName: EditText? = null //, edtDiseaseCode;

    // 사고 관련
    private var textDate: TextView? = null
    private var edtAccidentPlace: EditText? = null
    private var edtAccidentNote: EditText? = null
    private var data: Data_IUII10M04_F? = null
    private var datePickerDialog: DatePickerDialog? = null
    private var timePickerDialog: TimePickerDialog? = null
    private var btnSmbrPsbl: Button? = null // 부담보내역조회 버튼
    private var linSmbrPsblview: LinearLayout? = null // 부담보내역 안내
    private var bSmbrPsbl = false // 부담보내역 존재 여부

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        data = Data_IUII10M04_F()
        try {
            // 부담보내역존재여부 전달을 위한 EventBus
            EventBus.getDefault().register(this)
        } catch (e: NullPointerException) {
            e.message
        } catch (e: Exception) {
            e.message
        }
        LogPrinter.CF_debug("!---- (4단계) 인증구분 : " + mActivity?.CF_getAuthDvsn())
    }

    @SuppressLint("InflateParams")
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.iuii10m04_f, null)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        if (savedInstanceState != null) {
            if (savedInstanceState.containsKey("data")) {
                data = savedInstanceState.getParcelable("data")
            }
        }
        setUIControl()

        // 데이터 복구
        restoreData()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        saveData()
        outState.putParcelable("data", data)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        if (datePickerDialog != null && datePickerDialog!!.isShowing) {
            datePickerDialog!!.dismiss()
        }
        if (timePickerDialog != null && timePickerDialog!!.isShowing) {
            timePickerDialog!!.dismiss()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        LogPrinter.CF_debug("!----------------------------------------------------------")
        LogPrinter.CF_debug("!-- IUII90M04_F.onDestroy()")
        LogPrinter.CF_debug("!----------------------------------------------------------")
        EventBus.getDefault().unregister(this)
    }

    /**
     * 부담보내역 전달을 위한 EventBus method
     * @param data  Data_IUII10M00
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    fun transferData(data: Data_IUII10M00) {
        bSmbrPsbl = data.CF_getB_smbr_psbl()
        LogPrinter.CF_debug("4단계 부담보내역 : $bSmbrPsbl")
        if (bSmbrPsbl) {
            linSmbrPsblview!!.visibility = View.VISIBLE
            btnSmbrPsbl = view!!.findViewById(R.id.btnFillRed)
            btnSmbrPsbl?.text = resources.getString(R.string.btn_view_smbr)
            btnSmbrPsbl?.setOnClickListener(View.OnClickListener {
                val tmp_pageNumber = "26" // 부담보내역조회
                val tmp_csNo = mActivity?.CF_getData()!!.CF_getS_acdp_csno()
                val tmp_tempKey = SharedPreferencesFunc.getWebTempKey(mActivity?.applicationContext)
                val tmp_loginType = SharedPreferencesFunc.getLoginAuthDvsn(mActivity?.applicationContext)
                val tmp_url = EnvConfig.host_url + EnvConfig.URL_WEB_LINK + "?menuNo=&page=" + tmp_pageNumber + "&csno=" + tmp_csNo + "&loginType=" + tmp_loginType + "&tempKey=" + tmp_tempKey
                WebBrowserHelper.startWebViewActivity(mActivity?.applicationContext, 0, false, tmp_url, resources.getString(R.string.title_smbr_psbl))
            })
        } else {
            linSmbrPsblview!!.visibility = View.GONE
        }
    }

    /**
     * UI 생성 및 세팅 함수
     */
    private fun setUIControl() {
        // -- 버튼 UI
        setUIOfSmbrPsbl() // 부담보내역조회 UI
        setUIOfCategory() // 청구유형 UI
        setUIOfReason() // 발생원인 UI
        setUIOfType() // 청구사유 UI
        setUIOfDisease() // 진단명/진별코드 입력 UI
        setUIOfAccident() // 사고관련 UI 세팅
        scrollView = view!!.findViewById(R.id.scrollView)
        btnNext = view!!.findViewById(R.id.btnFill)
        btnNext?.text = resources.getString(R.string.btn_next_2)
        btnNext?.setOnClickListener {
            // ---------------------------------------------------------------------------------
            //  키패드 가리기
            // ---------------------------------------------------------------------------------
            if (activity!!.currentFocus != null) {
                CommonFunction.CF_closeVirtualKeyboard(activity, activity!!.currentFocus!!.windowToken)
            }
            if (checkUserInput()) {
                // -----------------------------------------------------------------------------
                //  Activity에 데이터 세팅 : submit에 사용하기 위한 데이터 세팅
                // -----------------------------------------------------------------------------
                val tmp_data = mActivity?.CF_getData()
                tmp_data!!.CF_setS_insu_requ_type_code(EnvConfig.reqCategoryCode[btnGroupCategory!!.CF_getCheckdButtonFirstIndex()])
                tmp_data.CF_setStr_s_insu_requ_type_code(EnvConfig.reqCategoryName[btnGroupCategory!!.CF_getCheckdButtonFirstIndex()])
                tmp_data.CF_setS_requ_gent_caus_code(EnvConfig.reqReasonCode[btnGroupReason!!.CF_getCheckdButtonFirstIndex()])
                tmp_data.CF_setStr_s_requ_gent_caus_code(EnvConfig.reqReasonName[btnGroupReason!!.CF_getCheckdButtonFirstIndex()])
                tmp_data.CF_setS_insu_requ_resn_code(strCodeReason)
                tmp_data.CF_setStr_s_insu_requ_resn_code(strNameReason)
                if ("1" == tmp_data.CF_getS_requ_gent_caus_code()) {
                    tmp_data.CF_setS_acdt_date(data!!.CF_getAccidentYear() + data!!.CF_getAccidentMonth() + data!!.CF_getAccidentDayOfMonth())
                    tmp_data.CF_setS_acdt_time(data!!.CF_getAccidentHour() + data!!.CF_getAccidentMinute() + "00")
                    tmp_data.CF_setS_acdt_pace(data!!.CF_getAccidentPlace())
                    tmp_data.CF_setS_acdt_cntt(data!!.CF_getAccidentNote())
                }
                tmp_data.CF_setS_acdt_pace(edtAccidentPlace!!.text.toString().trim())
                tmp_data.CF_setS_acdt_cntt(edtAccidentNote!!.text.toString().trim())
                tmp_data.CF_setS_dign_nm(edtDiseaseName!!.text.toString().trim())
                ////tmp_data.CF_setS_sick_code_no(edtDiseaseCode.getText().toString().trim());
                btnNext?.isEnabled = false

                // -----------------------------------------------------------------------------
                //  다음페이지 이동 시간 Delay
                // -----------------------------------------------------------------------------
                Handler().postDelayed({
                    mActivity?.CF_showNextPage()
                    mActivity?.CF_setVisibleStepIndicator(true)
                }, 120L)
            }
        }
    }

    /**
     * 데이터 복구 함수
     */
    private fun restoreData() {
        btnGroupCategory!!.CF_setCheck(data!!.CF_getCategory())
        btnGroupReason!!.CF_setCheck(data!!.CF_getReason())
        btnGroupType_1!!.CF_setCheck(data!!.CF_getArrType_1())
        edtDiseaseName!!.setText(data!!.CF_getDiseaseName())
        ////edtDiseaseCode.setText(data.CF_getDiseaseCode());
        if (!TextUtils.isEmpty(data!!.CF_getAccidentYear())) {
            setTextAccidentDateTime(data!!.CF_getAccidentYear(), data!!.CF_getAccidentMonth(), data!!.CF_getAccidentDayOfMonth(), data!!.CF_getAccidentHour(), data!!.CF_getAccidentMinute())
        }
        edtAccidentPlace!!.setText(data!!.CF_getAccidentPlace())
        edtAccidentNote!!.setText(data!!.CF_getAccidentNote())
        if (btnGroupReason!!.CF_getCheckdButtonFirstIndex() == 0) {
            linAccidentGroupView!!.visibility = View.VISIBLE
            ////edtDiseaseCode.setImeOptions(EditorInfo.IME_ACTION_NEXT);
        }
        LogPrinter.CF_debug("!---- 청구사유 선택값 : " + btnGroupCategory!!.CF_getCheckdButtonFirstIndex())
        // 청구유형에 따른 청구사유 제한
        // 실손의 경우, '입원','통원'만 선택
        /*
        if( btnGroupCategory.CF_getCheckdButtonFirstIndex() != 0){
            ArrayList<TextView> tmp_textBtnArr = btnGroupType_1.getArrTextView();

            String[] tmp_arrText = EnvConfig.reqTypeName;
            for(int i = 0 ; i < tmp_arrText.length; i++){
                if(!"입원".equals(tmp_arrText[i]) && !"통원".equals(tmp_arrText[i])){
                    tmp_textBtnArr.get(i).setEnabled(false);
                }
            }
        } else{
            ArrayList<TextView> tmp_textBtnArr = btnGroupType_1.getArrTextView();

            String[] tmp_arrText = EnvConfig.reqTypeName;
            for(int i = 0 ; i < tmp_arrText.length; i++){
                //if(!"입원".equals(tmp_arrText[i]) && !"통원".equals(tmp_arrText[i])){
                tmp_textBtnArr.get(i).setEnabled(true);
                //}
            }
        }

        ///양지훈_수정
        ///if( btnGroupCategory.CF_isChecked()
        */
    }

    /**
     * 데이터 저장
     */
    private fun saveData() {
        data!!.CF_setCategory(btnGroupCategory!!.CF_getCheckdButtonFirstIndex())
        data!!.CF_setReason(btnGroupReason!!.CF_getCheckdButtonFirstIndex())
        data!!.CF_setArrType_1(btnGroupType_1!!.CF_getCheckdArray())
        data!!.CF_setDiseaseName(edtDiseaseName!!.text.toString().trim())
        ////data.CF_setDiseaseCode(edtDiseaseCode.getText().toString().trim());
        data!!.CF_setAccidentPlace(edtAccidentPlace!!.text.toString().trim())
        data!!.CF_setAccidentNote(edtAccidentNote!!.text.toString().trim())
    }

    /**
     * 부담보내역 조회 UI 세팅 함수
     */
    private fun setUIOfSmbrPsbl() {
        linSmbrPsblview = view!!.findViewById(R.id.linSmbrPsbl)
        linSmbrPsblview?.visibility = View.GONE
    }

    /**
     * 청구유형 선택 UI 세팅 함수
     */
    private fun setUIOfCategory() {
        /*
        float[] tmp_arrWeight ={1.0f,1.0f};
        btnGroupCategory = (ButtonGroupView)getView().findViewById(R.id.btnGroupCategory);
        btnGroupCategory.CF_setButtons(EnvConfig.reqCategoryName, tmp_arrWeight, 2.0f);
        */

        // 청구유형버튼
        btnGroupCategory = view!!.findViewById(R.id.btnGroupCategory)
        btnGroupCategory?.CF_setButtons(EnvConfig.reqCategoryName, booleanArrayOf(true, true, false))
    }

    /**
     * 발생원인 UI 세팅 함수
     */
    private fun setUIOfReason() {
        val tmp_arrWeight = floatArrayOf(2.0f, 1.0f, 1.0f)

        /*
        btnGroupReason = (ButtonGroupView)getView().findViewById(R.id.btnGroupReason);
        btnGroupReason.CF_setButtons(EnvConfig.reqReasonName, tmp_arrWeight,4.0f);
        */
        btnGroupReason = view?.findViewById(R.id.btnGroupReason)
        btnGroupReason?.CF_setButtons(EnvConfig.reqReasonName, booleanArrayOf(true, false, false))
        btnGroupReason?.CE_setOnSelectedChangeEventListener(OnSelectedChangeEventListener { p_index ->
            if (p_index == 0) {
                linAccidentGroupView!!.visibility = View.VISIBLE
                ////edtDiseaseCode.setImeOptions(EditorInfo.IME_ACTION_NEXT);
            } else {
                linAccidentGroupView!!.visibility = View.GONE
                ////edtDiseaseCode.setImeOptions(EditorInfo.IME_ACTION_DONE);
                textDate!!.text = ""
                edtAccidentPlace!!.setText("")
                edtAccidentNote!!.setText("")
            }
        })
    }

    /**
     * 청구사유 UI 세팅 함수
     */
    private fun setUIOfType() {
        val splitCount = 4
        val arrText = EnvConfig.reqTypeName
        val arrText1 = arrayOfNulls<String>(splitCount)
        val arrText2 = arrayOfNulls<String>(splitCount)
        val arrText3 = arrayOfNulls<String>(splitCount)
        for (i in arrText.indices) {
            if (i / splitCount == 0) {
                arrText1[i % splitCount] = arrText[i]
            } else if (i / splitCount == 1) {
                arrText2[i % splitCount] = arrText[i]
            } else if (i / splitCount == 2) {
                arrText3[i % splitCount] = arrText[i]
            }
        }
        val tmp_arrWeight = floatArrayOf(1.0f, 1.0f, 1.0f, 1.0f)

        /*
        btnGroupType_1 = (ButtonGroupView)getView().findViewById(R.id.btnGroupType_1);
        btnGroupType_1.CF_setButtons(tmp_arrText_1,tmp_arrWeight, 4.0f);
        */
        val arrBtnGroup: Array<Array<String?>?> = arrayOfNulls(3)
        arrBtnGroup[0] = arrText1
        arrBtnGroup[1] = arrText2
        arrBtnGroup[2] = arrText3
        btnGroupType_1 = view?.findViewById(R.id.btnGroupType_1)
        //btnGroupType_1.CF_setButtons(tmp_arrText_1,new boolean[]{true,true,false,false});
        btnGroupType_1?.CF_setMode(ButtonGroupView.SelectMode.MultipleSelect)
        btnGroupType_1?.CF_setButtons(arrBtnGroup, booleanArrayOf(true, true, false, false))

        // NJM_20210528 TODO : "기타" 사유 클릭 금지 요청으로 인한 임시 처리
        btnGroupType_1?.arrTextView?.get(8)?.isClickable = false
        /*
        btnGroupType_2 = (ButtonGroupView)getView().findViewById(R.id.btnGroupType_2);
        btnGroupType_2.CF_setButtons(tmp_arrText_2,tmp_arrWeight, 4.0f);

        btnGroupType_3 = (ButtonGroupView)getView().findViewById(R.id.btnGroupType_3);
        btnGroupType_3.CF_setButtons(tmp_arrText_3,tmp_arrWeight, 4.0f);
        */
    }

    /**
     * 진단명/질병코드 입력 UI
     */
    private fun setUIOfDisease() {
        val linDiseaseName = view?.findViewById<LinearLayout>(R.id.labelEditDiseaseName) // 진단명
        ////LinearLayout tmp_linDiseaseCode = (LinearLayout)getView().findViewById(R.id.labelEditDiseaseCode);     // 진단코드

        // 라벨 세팅
        val labelName = linDiseaseName?.findViewById<TextView>(R.id.label)
        ////TextView tmp_labelCode = (TextView)tmp_linDiseaseCode.findViewById(R.id.label);

        // 라벨 LayoutParam 설정
        val lpLabel = labelName?.layoutParams as LinearLayout.LayoutParams
        lpLabel.width = CommonFunction.CF_convertDipToPixel(Objects.requireNonNull(activity)!!.applicationContext, 75f)
        labelName.layoutParams = lpLabel
        ////tmp_labelCode.setLayoutParams(tmp_lpLabel);

        // 라벨 Pading 설정
        labelName.setPadding(CommonFunction.CF_convertDipToPixel(activity!!.applicationContext, 10f), labelName.paddingTop, labelName.paddingRight, labelName.paddingBottom)
        ////tmp_labelCode.setPadding(CommonFunction.CF_convertDipToPixel(getActivity().getApplicationContext(),10), tmp_labelCode.getPaddingTop(), tmp_labelCode.getPaddingRight(), tmp_labelCode.getPaddingBottom());

        // 라벨 Text 설정
        labelName.text = resources.getString(R.string.label_disease_name_n)
        ////tmp_labelCode.setText(getResources().getString(R.string.label_disease_code_n));

        // 질병명 입력 EditText 설정
        edtDiseaseName = linDiseaseName.findViewById(R.id.edit)
        edtDiseaseName?.filters = CommonFunction.CF_getInputLengthFilter(maxLengthDiseaseName)
        edtDiseaseName?.imeOptions = EditorInfo.IME_ACTION_NEXT
        edtDiseaseName?.setHint(R.string.hint_disease_name)


        // 질병코드 입력 EditText 설정
        /*
        edtDiseaseCode = (EditText)tmp_linDiseaseCode.findViewById(R.id.edit);
        edtDiseaseCode.setInputType(InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
        edtDiseaseCode.setFilters(CommonFunction.CF_getInputLengthFilter(maxLengthDiseaseCode));
        edtDiseaseCode.setImeOptions(EditorInfo.IME_ACTION_DONE);
        edtDiseaseCode.setHint(R.string.hint_disease_code);
        edtDiseaseCode.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                // 키패드 hide
                if(s.length() == maxLengthDiseaseCode){
                    CommonFunction.CF_closeVirtualKeyboard(getActivity(), edtDiseaseCode.getWindowToken());
                }
            }
        });
        */
    }

    /**
     * 사고관련 UI 세팅 함수<br></br>
     * 사고일시 / 사고장소 / 사고경위
     */
    private fun setUIOfAccident() {
        linAccidentGroupView = view?.findViewById(R.id.linAccidentGroup)
        val tmp_linDate = view!!.findViewById<LinearLayout>(R.id.labelTextAccidentDate) // 사고일시
        val tmp_linPlace = view!!.findViewById<LinearLayout>(R.id.labelEditAccidentPlace) // 사고장소

        // 라벨 세팅
        val labelDate = tmp_linDate.findViewById<TextView>(R.id.label)
        val labelPlace = tmp_linPlace.findViewById<TextView>(R.id.label)
        val lpLabel = labelDate.layoutParams as LinearLayout.LayoutParams
        lpLabel.width = CommonFunction.CF_convertDipToPixel(Objects.requireNonNull(activity)!!.applicationContext, 75f)
        labelDate.layoutParams = lpLabel
        labelPlace.layoutParams = lpLabel
        labelDate.setPadding(CommonFunction.CF_convertDipToPixel(activity!!.applicationContext, 10f), labelDate.paddingTop, labelDate.paddingRight, labelDate.paddingBottom)
        labelPlace.setPadding(CommonFunction.CF_convertDipToPixel(activity!!.applicationContext, 10f), labelPlace.paddingTop, labelPlace.paddingRight, labelPlace.paddingBottom)
        labelDate.text = resources.getString(R.string.label_accident_date_n)
        labelPlace.text = resources.getString(R.string.label_accident_place_n)

        // 사고일시 click 세팅
        textDate = tmp_linDate.findViewById(R.id.text)
        textDate?.isClickable = true
        textDate?.setHint(R.string.hint_accident_date)
        textDate?.setBackgroundResource(R.drawable.btn_bank_name_selector)
        textDate?.setOnClickListener(View.OnClickListener {
            val calendar = Calendar.getInstance()

            // 시간 선택 다이얼로그
            timePickerDialog = TimePickerDialog(activity, { view, hourOfDay, minute ->
                val year = "" + textDate?.getTag(R.string.tag_year)
                val month = String.format(Locale.getDefault(), "%02d", textDate?.getTag(R.string.tag_month) as Int + 1)
                val dayOfMonth = String.format(Locale.getDefault(), "%02d", textDate?.getTag(R.string.tag_day_of_month))
                val hour = String.format(Locale.getDefault(), "%02d", hourOfDay)
                val minute = String.format(Locale.getDefault(), "%02d", minute)
                data!!.CF_setAccidentYear(year)
                data!!.CF_setAccidentMonth(month)
                data!!.CF_setAccidentDayOfMonth(dayOfMonth)
                data!!.CF_setAccidentHour(hour)
                data!!.CF_setAccidentMinute(minute)

                // TextView 사고일시 TEXT  세팅
                setTextAccidentDateTime(year, month, dayOfMonth, hour, minute)

                // 사고장소 입력 EditText 포커싱및 키패드 보이기
                if (edtAccidentPlace!!.text.isEmpty()) {
                    edtAccidentPlace!!.postDelayed({ CommonFunction.CF_showVirtualKeyboard(activity, edtAccidentPlace) }, 120)
                }
            }, calendar[Calendar.HOUR_OF_DAY], calendar[Calendar.MINUTE], false)

            // 날짜 선택 다이얼로그
            datePickerDialog = DatePickerDialog(activity!!, { view, year, month, dayOfMonth ->
                textDate?.setTag(R.string.tag_year, year)
                textDate?.setTag(R.string.tag_month, month)
                textDate?.setTag(R.string.tag_day_of_month, dayOfMonth)
                timePickerDialog!!.show()
            }, calendar[Calendar.YEAR], calendar[Calendar.MONTH], calendar[Calendar.DATE])
            datePickerDialog!!.datePicker.maxDate = System.currentTimeMillis() // 미래날짜 제한 NJM_20201104
            datePickerDialog!!.show()
        })

        // edittext 세팅
        edtAccidentPlace = tmp_linPlace.findViewById(R.id.edit)
        edtAccidentPlace?.filters = CommonFunction.CF_getInputLengthFilter(maxLengthAccidentPlace)
        edtAccidentPlace?.setHint(R.string.hint_accident_place)
        edtAccidentNote = view!!.findViewById(R.id.edtAccidentNote)
        edtAccidentNote?.filters = CommonFunction.CF_getInputLengthFilter(maxLengthAccidentNote)
    }

    /**
     * 사고일시 Text 문자열 세팅 함수
     * @param p_year        String
     * @param p_month       String
     * @param p_DayOfMonth  String
     * @param p_hour        String
     * @param p_minute      String
     */
    private fun setTextAccidentDateTime(p_year: String, p_month: String, p_DayOfMonth: String, p_hour: String, p_minute: String) {
        textDate!!.text = "$p_year.$p_month.$p_DayOfMonth $p_hour:$p_minute"
    }// 청구사유 row 1

    /**
     * 선택한 청구사유 코드를 문자열로 반환한다.
     * @return      String
     */
    private val strCodeReason: String
        get() {
            val arrText = ArrayList<String?>()
            val arrGroup_1 = btnGroupType_1!!.CF_getCheckdArray()

            // 청구사유 row 1
            for (i in arrGroup_1.indices) {
                if (arrGroup_1[i]) {
                    arrText.add(EnvConfig.reqTypeCode[i])
                }
            }
            return TextUtils.join(",", arrText)
        }// 청구사유 row 1

    /**
     * 선택한 청구사유 TEXT를 반환한다.
     * @return      String
     */
    private val strNameReason: String
        get() {
            val tmp_arrText = ArrayList<String?>()
            val tmp_arrGroup_1 = btnGroupType_1!!.CF_getCheckdArray()

            // 청구사유 row 1
            for (i in tmp_arrGroup_1.indices) {
                if (tmp_arrGroup_1[i]) {
                    tmp_arrText.add(EnvConfig.reqTypeName[i])
                }
            }
            return TextUtils.join(",", tmp_arrText)
        }

    /**
     * 다이얼로그 팝업 후 접근성 포커스 이동
     * @param p_message     String
     * @param p_view        View
     */
    private fun showCustomDlgAndAccessibilityFocus(p_message: String, p_view: View) {
        // -----------------------------------------------------------------------------------------
        //  StepIndicator hide 상태에서 AccessibilityEvent.TYPE_VIEW_FOCUSED 가 EditText 대상으로
        //  제대로 동작하지 않음.
        // -----------------------------------------------------------------------------------------
        mActivity!!.CF_setVisibleStepIndicator(true)
        val customDialog = CustomDialog(activity!!)
        customDialog.show()
        customDialog.CF_setBackKeyMode(CustomDialog.BackMode.NOTUSE)
        customDialog.CF_setTextContent(p_message)
        customDialog.CF_setSingleButtonText(resources.getString(R.string.btn_ok))
        customDialog.setOnDismissListener { Handler().postDelayed({ p_view.sendAccessibilityEvent(AccessibilityEvent.TYPE_VIEW_FOCUSED) }, 500) }
    }

    /**
     * 사용자 입력 값 검사 함수
     * @return      boolean
     */
    private fun checkUserInput(): Boolean {
        var tmp_flagOk = true

        // -- 진단명
        val temp_diseaseName = edtDiseaseName!!.text.toString().trim()
        val temp_euckr_diseaseName = FormatUtils.CF_checkUtf8toEucKr(temp_diseaseName)
        val tmp_flagOnAccibility = CommonFunction.CF_checkAccessibilityTurnOn(activity)
        if (!btnGroupCategory!!.CF_isChecked()) {        // 청구유형 검사
            tmp_flagOk = false
            mActivity!!.CF_setVisibleStepIndicator(true)
            showCustomDialog(resources.getString(R.string.dlg_select_category), view!!.findViewById(R.id.textCategory))
        } else if (!btnGroupReason!!.CF_isChecked()) {         // 발생원인 검사
            tmp_flagOk = false
            mActivity!!.CF_setVisibleStepIndicator(true)
            showCustomDialog(resources.getString(R.string.dlg_select_reason), view!!.findViewById(R.id.textReason))
        } else if (!btnGroupType_1!!.CF_isChecked()) {          // 청구사유 검사
            tmp_flagOk = false
            mActivity!!.CF_setVisibleStepIndicator(true)
            showCustomDialog(resources.getString(R.string.dlg_select_type), view!!.findViewById(R.id.textType))
        } else if (TextUtils.isEmpty(edtDiseaseName!!.text.toString().trim())) {
            tmp_flagOk = false
            mActivity!!.CF_setVisibleStepIndicator(true)
            showCustomDialog(resources.getString(R.string.dlg_empty_disease_name), edtDiseaseName!!)
        } else if ("" != temp_euckr_diseaseName) {
            showCustomDialog(resources.getString(R.string.dlg_error_charset) + "(" + temp_euckr_diseaseName + ")", edtDiseaseName!!)
            tmp_flagOk = false
        } else if (btnGroupReason!!.CF_getCheckdButtonFirstIndex() == 0) {
            val temp_textDate = textDate!!.text.toString().trim() // 사고일시
            val temp_accidentPlace = edtAccidentPlace!!.text.toString().trim() // 사고장소
            val temp_euckr_accidentPlace = FormatUtils.CF_checkUtf8toEucKr(temp_accidentPlace)
            val temp_accidentNote = edtAccidentNote!!.text.toString().trim() // 사고경위
            val temp_euckr_accidentNote = FormatUtils.CF_checkUtf8toEucKr(temp_accidentNote)

            // 사고일시 null 검사
            if (TextUtils.isEmpty(temp_textDate)) {
                tmp_flagOk = false
                mActivity!!.CF_setVisibleStepIndicator(true)
                showCustomDialog(resources.getString(R.string.dlg_empty_accident_date), textDate!!)
            } else if (TextUtils.isEmpty(temp_accidentPlace)) {
                tmp_flagOk = false
                mActivity!!.CF_setVisibleStepIndicator(true)
                showCustomDialog(resources.getString(R.string.dlg_empty_accident_place), edtAccidentPlace!!)
            } else if ("" != temp_euckr_accidentPlace) {
                showCustomDialog(resources.getString(R.string.dlg_error_charset) + "(" + temp_euckr_accidentPlace + ")", edtAccidentPlace!!)
                tmp_flagOk = false
            } else if (TextUtils.isEmpty(temp_accidentNote)) {
                tmp_flagOk = false
                mActivity!!.CF_setVisibleStepIndicator(true)
                showCustomDialog(resources.getString(R.string.dlg_empty_accident_note), edtAccidentNote!!)
            } else if ("" != temp_euckr_accidentNote) {
                showCustomDialog(resources.getString(R.string.dlg_error_charset) + "(" + temp_euckr_accidentNote + ")", edtAccidentNote!!)
                tmp_flagOk = false
            }
        } else if (btnGroupCategory!!.CF_getCheckdButtonFirstIndex() == 1) {
            val tmp_checkedArray = btnGroupType_1!!.CF_getCheckdArray()
            for (i in tmp_checkedArray.indices) {
                if (tmp_checkedArray[i]) {
                    val tmp_text = EnvConfig.reqTypeName[i]
                    if ("입원" != tmp_text && "통원" != tmp_text) {
                        val customDialog = CustomDialog(activity!!)
                        customDialog.show()
                        customDialog.CF_setTextContent(resources.getString(R.string.dlg_wrong_request_reason))
                        customDialog.CF_setBackKeyMode(CustomDialog.BackMode.CANCELED)
                        customDialog.CF_setSingleButtonText(resources.getString(R.string.btn_ok))
                        tmp_flagOk = false
                        break
                    }
                }
            }
        }
        return tmp_flagOk
    }
}