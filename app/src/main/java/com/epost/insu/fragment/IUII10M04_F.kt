package com.epost.insu.fragment

import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.app.DatePickerDialog.OnDateSetListener
import android.app.TimePickerDialog
import android.app.TimePickerDialog.OnTimeSetListener
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.*
import com.epost.insu.EnvConfig
import com.epost.insu.EnvConfig.AuthDvsn
import com.epost.insu.R
import com.epost.insu.SharedPreferencesFunc
import com.epost.insu.common.*
import com.epost.insu.control.ButtonGroupView
import com.epost.insu.data.Data_IUII10M00
import com.epost.insu.data.Data_IUII10M04_F
import com.epost.insu.dialog.CustomDialog
import com.epost.insu.event.OnSelectedChangeEventListener
import com.epost.insu.network.HttpConnections
import com.epost.insu.network.WeakReferenceHandler
import com.epost.insu.network.WeakReferenceHandler.ObjectHandlerMessage
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.json.JSONException
import org.json.JSONObject
import java.util.*

/**
 * 보험금청구 > 본인청구 > 4단계. 보험청구서작성(청구내용1)
 * @since     :
 * @version   : 1.7
 * @author    : LSH
 * @see
 * <pre>
 * ======================================================================
 * 0.0.0    LSH_20170811    최초 등록
 * 0.0.0    LSH_20171127    청구유형 정액/실손/동시 => 정액/실손
 * 0.0.0    LSH_20180618    청구유형에 따른 청구사유 제한 (실손 : 입원,통원만 가능)
 * 0.0.0    NJM_20190118    청구시 실손 사전 체크 기능 추가 (전문호출시간이 길어서 미오픈)
 * 0.0.0    NJM_20191208    청구시 정액+실손 추가
 * 0.0.0    NJM_20200207    문자셋 오류 검사 기능 추가 (사고내용,사고경위,진단명)
 * 0.0.0    YJH_20201210    고객 부담보내역 조회 추가
 * 1.5.6    NJM_20210528    [청구서서식변경] 청구사유변경(깁스삭제,기타 추가 및 선택 금지)
 * 1.6.2    NJM_20210802    [2021년 대우 취약점] 2차본 : 부적절한 예외 처리
 * =======================================================================
 * copyright : (주)프로니스정보기술 | http://www.phronis.co.kr
 * </pre>
 */
class IUII10M04_F : IUII10M00_FD(), ObjectHandlerMessage {
    private val maxLengthDiseaseName: Int = 40 // 진단명 max length
    private val maxLengthDiseaseCode: Int = 5 // 진단코드 max length
    private val maxLengthAccidentPlace: Int = 100 // 사고장소 max length
    private val maxLengthAccidentNote: Int = 100 // 사고경위 max length
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

    // 2019-01-18 실손 사전 체크 관련 변수
    private val subUrl_getUserInfo: String = "/II/IUII15M00.do"
    private var httpHandler: WeakReferenceHandler? = null // http 리턴 핸들러
    private val HANDLERJOB_GET_SILSON: Int = 0 // http 성공 Flag
    private val HANDLERJOB_ERROR_GET_SILSON: Int = 1 // http 실패 Flag
    private var btnSmbrPsbl: Button? = null // 부담보내역조회 버튼

    private var linSmbrPsblview: LinearLayout? = null // 부담보내역 안내
    private var bSmbrPsbl: Boolean = false // 부담보내역 존재 여부

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        LogPrinter.CF_debug("!----------------------------------------------------------")
        LogPrinter.CF_debug("!-- IUII10M04_F.onSaveInstanceState()")
        LogPrinter.CF_debug("!----------------------------------------------------------")
        saveData()
        outState.putParcelable("data", data)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        LogPrinter.CF_debug("!----------------------------------------------------------")
        LogPrinter.CF_debug("!-- IUII10M04_F.onCreate()")
        LogPrinter.CF_debug("!----------------------------------------------------------")
        data = Data_IUII10M04_F()
        httpHandler = WeakReferenceHandler(this)
        try {
            // 부담보내역존재여부 수신을 위한 EventBus
            EventBus.getDefault().register(this)
        } catch (e: NullPointerException) {
            e.message
        } catch (e: Exception) {
            e.message
        }
        LogPrinter.CF_debug("!---- (4단계) 인증구분 : " + mActivity!!.CF_getAuthDvsn())
    }

    @SuppressLint("InflateParams")
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        LogPrinter.CF_debug("!----------------------------------------------------------")
        LogPrinter.CF_debug("!-- IUII10M04_F.onCreateView()")
        LogPrinter.CF_debug("!----------------------------------------------------------")
        return inflater.inflate(R.layout.iuii10m04_f, null)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        LogPrinter.CF_debug("!----------------------------------------------------------")
        LogPrinter.CF_debug("!-- IUII10M04_F.onActivityCreated()")
        LogPrinter.CF_debug("!----------------------------------------------------------")
        if (savedInstanceState != null) {
            if (savedInstanceState.containsKey("data")) {
                data = savedInstanceState.getParcelable("data")
            }
        }
        setUIControl()
        restoreData() // 데이터 복구
    }

    override fun onDestroyView() {
        super.onDestroyView()
        LogPrinter.CF_debug("!----------------------------------------------------------")
        LogPrinter.CF_debug("!-- IUII10M04_F.onDestroyView()")
        LogPrinter.CF_debug("!----------------------------------------------------------")
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
        LogPrinter.CF_debug("!-- IUII10M04_F.onDestroy()")
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
        LogPrinter.CF_debug("4단계 부담보내역 : " + bSmbrPsbl)
        if (bSmbrPsbl) {
            linSmbrPsblview!!.visibility = View.VISIBLE
            btnSmbrPsbl = view?.findViewById(R.id.btnFillRed)
            btnSmbrPsbl?.text = resources.getString(R.string.btn_view_smbr)
            btnSmbrPsbl?.setOnClickListener {
                val tmp_pageNumber: String = "26" // 부담보내역조회
                val tmp_csNo: String = mActivity!!.CF_getData()!!.CF_getS_acdp_csno()
                val tmp_tempKey: String = SharedPreferencesFunc.getWebTempKey(mActivity!!.applicationContext)
                val tmp_loginType: AuthDvsn = SharedPreferencesFunc.getLoginAuthDvsn(mActivity!!.applicationContext)
                val tmp_url: String = EnvConfig.host_url + EnvConfig.URL_WEB_LINK + "?menuNo=&page=" + tmp_pageNumber + "&csno=" + tmp_csNo + "&loginType=" + tmp_loginType + "&tempKey=" + tmp_tempKey
                WebBrowserHelper.startWebViewActivity(mActivity!!.applicationContext, 0, false, tmp_url, resources.getString(R.string.title_smbr_psbl))
            }
        } else {
            linSmbrPsblview!!.visibility = View.GONE
        }
    }

    /**
     * UI 생성 및 세팅 함수
     */
    private fun setUIControl() {
        setUIOfSmbrPsbl() // 부담보내역조회 UI
        setUIOfCategory() // 청구유형 UI
        setUIOfReason() // 발생원인 UI
        setUIOfType() // 청구사유 UI
        setUIOfDisease() // 진단명/질병코드 입력 UI
        setUIOfAccident() // 사고관련 UI 세팅
        scrollView = view?.findViewById(R.id.scrollView)
        btnNext = view!!.findViewById(R.id.btnFill)
        btnNext?.text = resources.getString(R.string.btn_next_2)
        btnNext?.setOnClickListener(object : View.OnClickListener {
            override fun onClick(v: View) {

                //TODO : 2019-01-18 실손사전조회 (전문속도 향상 후 오픈)
                val ctgy: Int = 0 // 임시로 해제함 (사용시 삭제 후 아래 변수 사용)
                // int ctgy = btnGroupCategory.CF_getCheckdButtonFirstIndex();

                // 실손청구(ctgy:1)
                if (ctgy == 1) {
                    // 실손 청구일 경우 청구 가능상품 보유했는지 사전조회
                    btnNext?.isEnabled = false
                    httpReq_canSilson()

                    // 정액청구(ctgy:0)
                } else {
                    next()
                }
            }
        })
    }

    /**
     * 다음페이지 이동
     */
    private operator fun next() {
        // ---------------------------------------------------------------------------------
        //  키패드 가리기
        // ---------------------------------------------------------------------------------
        if (activity!!.currentFocus != null) CommonFunction.CF_closeVirtualKeyboard(activity, activity!!.currentFocus!!.windowToken)

        if (checkUserInput()) {
            // -----------------------------------------------------------------------------
            //  Activity에 데이터 세팅 : submit에 사용하기 위한 데이터 세팅
            // -----------------------------------------------------------------------------
            val dataA: Data_IUII10M00? = mActivity!!.CF_getData()

            val categoryIdx = btnGroupCategory!!.CF_getCheckdButtonFirstIndex()
            val reasonIdx   = btnGroupReason!!.CF_getCheckdButtonFirstIndex()

            // 취약점) 정수 연산 전에 안전한 범위 내의 값인지 검사한다.
            require(!(categoryIdx < 0 || categoryIdx > 100)) { "out of bound" }
            require(!(reasonIdx < 0   || reasonIdx > 100)) { "out of bound" }

            dataA!!.CF_setS_insu_requ_type_code(EnvConfig.reqCategoryCode[categoryIdx])
            dataA.CF_setStr_s_insu_requ_type_code(EnvConfig.reqCategoryName[categoryIdx])
            dataA.CF_setS_requ_gent_caus_code(EnvConfig.reqReasonCode[reasonIdx])
            dataA.CF_setStr_s_requ_gent_caus_code(EnvConfig.reqReasonName[reasonIdx])
            dataA.CF_setS_insu_requ_resn_code(strCodeReason)
            dataA.CF_setStr_s_insu_requ_resn_code(strNameReason)

            if (("1" == dataA.CF_getS_requ_gent_caus_code())) {
                dataA.CF_setS_acdt_date(data!!.CF_getAccidentYear() + data!!.CF_getAccidentMonth() + data!!.CF_getAccidentDayOfMonth())
                dataA.CF_setS_acdt_time(data!!.CF_getAccidentHour() + data!!.CF_getAccidentMinute() + "00")
                dataA.CF_setS_acdt_pace(data!!.CF_getAccidentPlace())
                dataA.CF_setS_acdt_cntt(data!!.CF_getAccidentNote())
            }
            dataA.CF_setS_acdt_pace(edtAccidentPlace!!.text.toString().trim())
            dataA.CF_setS_acdt_cntt(edtAccidentNote!!.text.toString().trim())
            dataA.CF_setS_dign_nm(edtDiseaseName!!.text.toString().trim())
            ////tmp_data.CF_setS_sick_code_no(edtDiseaseCode.getText().toString().trim());
            btnNext!!.isEnabled = false

            // -----------------------------------------------------------------------------
            //  다음페이지 이동 시간 Delay
            // -----------------------------------------------------------------------------
            Handler().postDelayed(object : Runnable {
                override fun run() {
                    mActivity!!.CF_showNextPage()
                    mActivity!!.CF_setVisibleStepIndicator(true)
                }
            }, 120L)
        }
    }

    /**
     * 데이터 복구 함수
     */
    private fun restoreData() {
        LogPrinter.CF_debug("!----------------------------------------------------------")
        LogPrinter.CF_debug("!-- IUII10M04_F.restoreData()")
        LogPrinter.CF_debug("!----------------------------------------------------------")
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
        linSmbrPsblview = view?.findViewById(R.id.linSmbrPsbl)
        linSmbrPsblview?.visibility = View.GONE
    }

    /**
     * 청구유형 선택 UI 세팅 함수
     */
    private fun setUIOfCategory() {
        // -- 청구유형버튼
        btnGroupCategory = view?.findViewById(R.id.btnGroupCategory)
        btnGroupCategory?.CF_setButtons(EnvConfig.reqCategoryName, booleanArrayOf(true, true, false))
    }

    /**
     * 발생원인 UI 세팅 함수
     */
    private fun setUIOfReason() {
        btnGroupReason = view?.findViewById(R.id.btnGroupReason)
        btnGroupReason?.CF_setButtons(EnvConfig.reqReasonName, booleanArrayOf(true, false, false))
        btnGroupReason?.CE_setOnSelectedChangeEventListener(object : OnSelectedChangeEventListener {
            override fun onSelected(p_index: Int) {
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
            }
        })
    }

    /**
     * 청구사유 UI 세팅 함수
     */
    private fun setUIOfType() {
        val splitCount: Int = 4
        val tmp_arrText: Array<String> = EnvConfig.reqTypeName
        val tmp_arrText_1: Array<String?> = arrayOfNulls(splitCount)
        val tmp_arrText_2: Array<String?> = arrayOfNulls(splitCount)
        val tmp_arrText_3: Array<String?> = arrayOfNulls(splitCount)
        for (i in tmp_arrText.indices) {
            if (i / splitCount == 0) {
                tmp_arrText_1[i % splitCount] = tmp_arrText.get(i)
            } else if (i / splitCount == 1) {
                tmp_arrText_2[i % splitCount] = tmp_arrText.get(i)
            } else if (i / splitCount == 2) {
                tmp_arrText_3[i % splitCount] = tmp_arrText.get(i)
            }
        }
        val tmp_arrBtnGroup: Array<Array<String?>?> = arrayOfNulls(3)
        tmp_arrBtnGroup[0] = tmp_arrText_1
        tmp_arrBtnGroup[1] = tmp_arrText_2
        tmp_arrBtnGroup[2] = tmp_arrText_3
        btnGroupType_1 = view?.findViewById(R.id.btnGroupType_1)
        btnGroupType_1?.CF_setMode(ButtonGroupView.SelectMode.MultipleSelect)
        btnGroupType_1?.CF_setButtons(tmp_arrBtnGroup, booleanArrayOf(true, true, false, false))

        // TODO : "기타" 사유 클릭 금지 요청으로 인한 임시 처리
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
        val tmp_linDiseaseName: LinearLayout? = view?.findViewById(R.id.labelEditDiseaseName) // 진단명
        //LinearLayout tmp_linDiseaseCode = (LinearLayout)getView().findViewById(R.id.labelEditDiseaseCode);     // 진단코드

        // 라벨 세팅
        val tmp_labelName: TextView? = tmp_linDiseaseName?.findViewById(R.id.label)
        //TextView tmp_labelCode = (TextView)tmp_linDiseaseCode.findViewById(R.id.label);

        // 라벨 LayoutParam 설정
        val tmp_lpLabel: LinearLayout.LayoutParams = tmp_labelName?.layoutParams as LinearLayout.LayoutParams
        tmp_lpLabel.width = CommonFunction.CF_convertDipToPixel(Objects.requireNonNull(activity)!!.applicationContext, 75f)
        tmp_labelName.layoutParams = tmp_lpLabel
        //tmp_labelCode.setLayoutParams(tmp_lpLabel);

        // 라벨 Pading 설정
        tmp_labelName.setPadding(CommonFunction.CF_convertDipToPixel(activity!!.applicationContext, 10f), tmp_labelName.paddingTop, tmp_labelName.paddingRight, tmp_labelName.paddingBottom)
        //tmp_labelCode.setPadding(CommonFunction.CF_convertDipToPixel(getActivity().getApplicationContext(),10), tmp_labelCode.getPaddingTop(), tmp_labelCode.getPaddingRight(), tmp_labelCode.getPaddingBottom());

        // 라벨 Text 설정
        tmp_labelName.text = resources.getString(R.string.label_disease_name_n)
        //tmp_labelCode.setText(getResources().getString(R.string.label_disease_code_n));

        // 질병명 입력 EditText 설정
        edtDiseaseName = tmp_linDiseaseName.findViewById(R.id.edit)
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
        val tmp_linDate: LinearLayout = view!!.findViewById(R.id.labelTextAccidentDate) // 사고일시
        val tmp_linPlace: LinearLayout = view!!.findViewById(R.id.labelEditAccidentPlace) // 사고장소

        // 라벨 세팅
        val tmp_labelDate: TextView = tmp_linDate.findViewById(R.id.label)
        val tmp_labelPlace: TextView = tmp_linPlace.findViewById(R.id.label)
        val tmp_lpLabel: LinearLayout.LayoutParams = tmp_labelDate.layoutParams as LinearLayout.LayoutParams
        tmp_lpLabel.width = CommonFunction.CF_convertDipToPixel(Objects.requireNonNull(activity)!!.applicationContext, 75f)
        tmp_labelDate.layoutParams = tmp_lpLabel
        tmp_labelPlace.layoutParams = tmp_lpLabel
        tmp_labelDate.setPadding(CommonFunction.CF_convertDipToPixel(activity!!.applicationContext, 10f), tmp_labelDate.paddingTop, tmp_labelDate.paddingRight, tmp_labelDate.paddingBottom)
        tmp_labelPlace.setPadding(CommonFunction.CF_convertDipToPixel(activity!!.applicationContext, 10f), tmp_labelPlace.paddingTop, tmp_labelPlace.paddingRight, tmp_labelPlace.paddingBottom)
        tmp_labelDate.text = resources.getString(R.string.label_accident_date_n)
        tmp_labelPlace.text = resources.getString(R.string.label_accident_place_n)

        // -- 사고일시 click 세팅
        textDate = tmp_linDate.findViewById(R.id.text)
        textDate?.isClickable = true
        textDate?.setHint(R.string.hint_accident_date)
        textDate?.setBackgroundResource(R.drawable.btn_bank_name_selector)
        textDate?.setOnClickListener(object : View.OnClickListener {
            override fun onClick(v: View) {
                val tmp_calendar: Calendar = Calendar.getInstance()

                // 시간 선택 다이얼로그
                timePickerDialog = TimePickerDialog(activity, object : OnTimeSetListener {
                    override fun onTimeSet(view: TimePicker, hourOfDay: Int, minute: Int) {
                        val tmp_year: String = "" + textDate?.getTag(R.string.tag_year)
                        val tmp_month: String = String.format(Locale.getDefault(), "%02d", textDate?.getTag(R.string.tag_month) as Int + 1)
                        val tmp_dayOfMonth: String = String.format(Locale.getDefault(), "%02d", textDate?.getTag(R.string.tag_day_of_month))
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
                        if (edtAccidentPlace!!.text.length == 0) {
                            edtAccidentPlace!!.postDelayed(object : Runnable {
                                override fun run() {
                                    CommonFunction.CF_showVirtualKeyboard(activity, edtAccidentPlace)
                                }
                            }, 120)
                        }
                    }
                }, tmp_calendar.get(Calendar.HOUR_OF_DAY), tmp_calendar.get(Calendar.MINUTE), false)

                // -- 날짜 선택 다이얼로그
                datePickerDialog = DatePickerDialog((activity)!!, object : OnDateSetListener {
                    override fun onDateSet(view: DatePicker, year: Int, month: Int, dayOfMonth: Int) {
                        textDate?.setTag(R.string.tag_year, year)
                        textDate?.setTag(R.string.tag_month, month)
                        textDate?.setTag(R.string.tag_day_of_month, dayOfMonth)
                        timePickerDialog!!.show()
                    }
                }, tmp_calendar.get(Calendar.YEAR), tmp_calendar.get(Calendar.MONTH), tmp_calendar.get(Calendar.DATE))
                datePickerDialog!!.datePicker.maxDate = System.currentTimeMillis() // 미래날짜 제한 NJM_20201104
                datePickerDialog!!.show()
            }
        })

        // -- edittext 세팅
        edtAccidentPlace = tmp_linPlace.findViewById(R.id.edit)
        edtAccidentPlace?.filters = CommonFunction.CF_getInputLengthFilter(maxLengthAccidentPlace)
        edtAccidentPlace?.setHint(R.string.hint_accident_place)
        edtAccidentNote = view?.findViewById(R.id.edtAccidentNote)
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
        textDate!!.text = "" + p_year + "." + p_month + "." + p_DayOfMonth + " " + p_hour + ":" + p_minute
    }// 청구사유 row 1

    /**
     * 선택한 청구사유 코드를 문자열로 반환한다.
     * @return      String
     */
    private val strCodeReason: String
        private get() {
            val tmp_arrText: ArrayList<String?> = ArrayList()
            val tmp_arrGroup_1: ArrayList<Boolean> = btnGroupType_1!!.CF_getCheckdArray()

            // 청구사유 row 1
            for (i in tmp_arrGroup_1.indices) {
                if (tmp_arrGroup_1.get(i)) {
                    tmp_arrText.add(EnvConfig.reqTypeCode.get(i))
                }
            }
            return TextUtils.join(",", tmp_arrText)
        }// 청구사유 row 1

    /**
     * 선택한 청구사유 TEXT를 반환한다.
     * @return      String
     */
    private val strNameReason: String
        private get() {
            val tmp_arrText: ArrayList<String?> = ArrayList()
            val tmp_arrGroup_1: ArrayList<Boolean> = btnGroupType_1!!.CF_getCheckdArray()

            // 청구사유 row 1
            for (i in tmp_arrGroup_1.indices) {
                if (tmp_arrGroup_1.get(i)) {
                    tmp_arrText.add(EnvConfig.reqTypeName.get(i))
                }
            }
            return TextUtils.join(",", tmp_arrText)
        }

    /**
     * 사용자 입력 값 검사 함수
     * @return      boolean
     */
    private fun checkUserInput(): Boolean {
        var tmp_flagOk: Boolean = true

        // -- 진단명
        val temp_diseaseName: String = edtDiseaseName!!.text.toString().trim()
        val temp_euckr_diseaseName: String = FormatUtils.CF_checkUtf8toEucKr(temp_diseaseName)
        val tmp_flagOnAccibility: Boolean = CommonFunction.CF_checkAccessibilityTurnOn(Objects.requireNonNull(activity))
        if (!btnGroupCategory!!.CF_isChecked()) { // 청구유형 검사
            tmp_flagOk = false
            mActivity!!.CF_setVisibleStepIndicator(true)
            showCustomDialog(resources.getString(R.string.dlg_select_category), view!!.findViewById(R.id.textCategory))
        } else if (!btnGroupReason!!.CF_isChecked()) { // 발생원인 검사
            tmp_flagOk = false
            mActivity!!.CF_setVisibleStepIndicator(true)
            showCustomDialog(resources.getString(R.string.dlg_select_reason), view!!.findViewById(R.id.textReason))
        } else if (!btnGroupType_1!!.CF_isChecked()) { // 청구사유 검사
            tmp_flagOk = false
            mActivity!!.CF_setVisibleStepIndicator(true)
            showCustomDialog(resources.getString(R.string.dlg_select_type), view!!.findViewById(R.id.textType))
        } else if (TextUtils.isEmpty(temp_diseaseName)) {
            tmp_flagOk = false
            mActivity!!.CF_setVisibleStepIndicator(true)
            showCustomDialog(resources.getString(R.string.dlg_empty_disease_name), (edtDiseaseName)!!)
        } else if (!("" == temp_euckr_diseaseName)) {
            showCustomDialog(resources.getString(R.string.dlg_error_charset) + "(" + temp_euckr_diseaseName + ")", (edtDiseaseName)!!)
            tmp_flagOk = false
        } else if (btnGroupReason!!.CF_getCheckdButtonFirstIndex() == 0) {
            val temp_textDate: String = textDate!!.text.toString().trim() // 사고일시
            val temp_accidentPlace: String = edtAccidentPlace!!.text.toString().trim() // 사고장소
            val temp_euckr_accidentPlace: String = FormatUtils.CF_checkUtf8toEucKr(temp_accidentPlace)
            val temp_accidentNote: String = edtAccidentNote!!.text.toString().trim() // 사고경위
            val temp_euckr_accidentNote: String = FormatUtils.CF_checkUtf8toEucKr(temp_accidentNote)

            // --<2> 사고일시 null 검사
            if (TextUtils.isEmpty(temp_textDate)) {
                tmp_flagOk = false
                mActivity!!.CF_setVisibleStepIndicator(true)
                showCustomDialog(resources.getString(R.string.dlg_empty_accident_date), (textDate)!!)
            } else if (TextUtils.isEmpty(temp_accidentPlace)) {
                tmp_flagOk = false
                mActivity!!.CF_setVisibleStepIndicator(true)
                showCustomDialog(resources.getString(R.string.dlg_empty_accident_place), (edtAccidentPlace)!!)
            } else if (!("" == temp_euckr_accidentPlace)) {
                showCustomDialog(resources.getString(R.string.dlg_error_charset) + "(" + temp_euckr_accidentPlace + ")", (edtAccidentPlace)!!)
                tmp_flagOk = false
            } else if (TextUtils.isEmpty(temp_accidentNote)) {
                tmp_flagOk = false
                mActivity!!.CF_setVisibleStepIndicator(true)
                showCustomDialog(resources.getString(R.string.dlg_empty_accident_note), (edtAccidentNote)!!)
            } else if (!("" == temp_euckr_accidentNote)) {
                showCustomDialog(resources.getString(R.string.dlg_error_charset) + "(" + temp_euckr_accidentNote + ")", (edtAccidentNote)!!)
                tmp_flagOk = false
            }
        } else if (btnGroupCategory!!.CF_getCheckdButtonFirstIndex() == 1) {
            val tmp_checkedArray: ArrayList<Boolean> = btnGroupType_1!!.CF_getCheckdArray()
            for (i in tmp_checkedArray.indices) {
                if (tmp_checkedArray.get(i)) {
                    val tmp_text: String = EnvConfig.reqTypeName.get(i)
                    if (!("입원" == tmp_text) && !("통원" == tmp_text)) {
                        val tmp_dlg: CustomDialog = CustomDialog((activity)!!)
                        tmp_dlg.show()
                        tmp_dlg.CF_setTextContent(resources.getString(R.string.dlg_wrong_request_reason))
                        tmp_dlg.CF_setBackKeyMode(CustomDialog.BackMode.CANCELED)
                        tmp_dlg.CF_setSingleButtonText(resources.getString(R.string.btn_ok))
                        tmp_flagOk = false
                        break
                    }
                }
            }
        }
        return tmp_flagOk
    }

    // #############################################################################################
    //  Http 관련
    // #############################################################################################
    override fun handleMessage(p_message: Message) {
        if (mActivity != null && !mActivity!!.isDestroyed) {
            mActivity!!.CF_dismissProgressDialog()
            when (p_message.what) {
                HANDLERJOB_GET_SILSON -> try {
                    httpRes_canSilson(JSONObject(p_message.obj as String?))
                } catch (e: JSONException) {
                    LogPrinter.CF_debug(resources.getString(R.string.log_json_exception))
                    mActivity!!.CF_OnError(resources.getString(R.string.dlg_error_server))
                }
                HANDLERJOB_ERROR_GET_SILSON -> mActivity!!.CF_OnError(p_message.obj as String?)
                else -> {
                }
            }
        }
    }

    /**
     * 2019-01-18 (Https)실손 사전체크 통신<br></br>
     */
    fun httpReq_canSilson() {
        LogPrinter.CF_debug("!----------------------------------------------------------")
        LogPrinter.CF_debug("!-- IUII10M04_F.httpReq_canSilson()")
        LogPrinter.CF_debug("!----------------------------------------------------------")
        mActivity!!.CF_showProgressDialog()
        val sUrl: String = EnvConfig.host_url + subUrl_getUserInfo
        val tmp_builder: Uri.Builder = Uri.Builder()
        tmp_builder.appendQueryParameter("csno", mActivity!!.CF_getData()!!.CF_getS_acdp_csno())

        // --<1> (휴대폰인증 외)
        if (mActivity!!.CF_getAuthDvsn() != AuthDvsn.MOBILE) {
            tmp_builder.appendQueryParameter("tempKey", SharedPreferencesFunc.getWebTempKey(Objects.requireNonNull(activity)))
            LogPrinter.CF_debug("!---- 기타인증(공동,지문,핀) tempKey::::" + SharedPreferencesFunc.getWebTempKey(activity))
        }
        HttpConnections.sendPostData(
            sUrl, tmp_builder.build().encodedQuery, httpHandler, HANDLERJOB_GET_SILSON, HANDLERJOB_ERROR_GET_SILSON
        )
    }

    /**
     * 2019-01-18 (Https result) 실손정보 요청결과 처리 함수
     * @param p_jsonObject      JSONObject
     */
    @Throws(JSONException::class)
    private fun httpRes_canSilson(p_jsonObject: JSONObject) {
        LogPrinter.CF_debug("!----------------------------------------------------------")
        LogPrinter.CF_debug("!-- IUII10M04_F.httpRes_canSilson()")
        LogPrinter.CF_debug("!----------------------------------------------------------")
        val jsonKey_errorCode: String = "errCode"
        val jsonKey_data: String = "data"
        val jsonKey_s_actl_insu_yn: String = "s_actl_insu_yn" // 조회 결과
        val tmp_errorCode: String
        if (p_jsonObject.has(jsonKey_errorCode)) { // --<1> errCorde(결과코드) 키가 있을때,
            tmp_errorCode = p_jsonObject.getString(jsonKey_errorCode)

            // --<2> 입력값에서 고객정보가 없었을 경우, (비정상 호출시)
            if ((tmp_errorCode == "ERRIUII15M00001")) {
                mActivity!!.CF_OnError(resources.getString(R.string.dlg_error_erriuii12m00001))
            } else if (p_jsonObject.has(jsonKey_data)) { // --<2> Return값에 data key가 있을 때 (정상 리턴시)
                val tmp_jsonData: JSONObject = p_jsonObject.getJSONObject(jsonKey_data)

                // ---------------------------------------------------------------------------------
                // 고객의 보험 계약 정보 확인 : 수익자이면서 피보험자인 경우만 청구 가능
                // ---------------------------------------------------------------------------------
                // --<3> Return값에 s_actl_insu_yn key가 있을 때 (정상 리턴시 - 결과코드)
                if (tmp_jsonData.has(jsonKey_s_actl_insu_yn)) {
                    val tmp_strAvailable: String = tmp_jsonData.getString(jsonKey_s_actl_insu_yn)
                    when (tmp_strAvailable.toLowerCase(Locale.getDefault())) {
                        "s" -> {
                            mActivity!!.showCustomDialogAlert(resources.getString(R.string.dlg_error_erriuii12m00004))
                            btnNext!!.isEnabled = true
                        }
                        "y" -> {
                            if (CommonFunction.CF_checkAccessibilityTurnOn(activity)) {
                                mActivity!!.CF_requestFocusIndicator()
                            }
                            next() // 다음단계 진행
                        }
                        "e" -> {
                            mActivity!!.showCustomDialogAlert(resources.getString(R.string.dlg_error_erriuii12m00003))
                            btnNext!!.isEnabled = true
                        }
                        else -> {
                            mActivity!!.showCustomDialogAlert(resources.getString(R.string.dlg_error_erriuii12m00003))
                            btnNext!!.isEnabled = true
                        }
                    }
                } else { // --<3> (결과에러) Return값에 s_actl_insu_yn key가 없을 때 (비정상 리턴시 - 결과코드)
                    // 운영서버 미적용으로 임시 팝업
                    mActivity!!.CF_OnError(resources.getString(R.string.dlg_error_erriuii12m00003))
                }
            } else { // --<2> (전문에러) Return값에 data key가 없을 때, (비정상 리턴시)
                mActivity!!.CF_OnError(resources.getString(R.string.dlg_error_server_2))
            }
        } else { // --<1> (통신실패) errCorde(결과코드) 키가 없을때,
            mActivity!!.CF_OnError(resources.getString(R.string.dlg_error_server_1))
        }
    }
}