package com.epost.insu.fragment

import android.annotation.SuppressLint
import android.app.Activity
import android.content.DialogInterface
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.telephony.PhoneNumberFormattingTextWatcher
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.accessibility.AccessibilityEvent
import android.widget.EditText
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import com.epost.insu.EnvConfig
import com.epost.insu.EnvConfig.AuthDvsn
import com.epost.insu.R
import com.epost.insu.SharedPreferencesFunc
import com.epost.insu.activity.IUCOF0M00
import com.epost.insu.common.CommonFunction
import com.epost.insu.common.LogPrinter
import com.epost.insu.common.Regex
import com.epost.insu.control.SwitchingControl
import com.epost.insu.data.Data_IUII10M00
import com.epost.insu.data.Data_IUII10M03_F
import com.epost.insu.dialog.CustomDialog
import com.epost.insu.event.OnChangedCheckedStateEventListener
import com.epost.insu.network.HttpConnections
import com.epost.insu.network.WeakReferenceHandler
import com.epost.insu.network.WeakReferenceHandler.ObjectHandlerMessage
import org.greenrobot.eventbus.EventBus
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.util.*

/**
 * 보험금청구 > 본인청구 > 3단계. 보험청구서작성(개인정보)
 * @since     :
 * @version   : 2.0
 * @author    : NJM
 * <pre>
 * ======================================================================
 * 0.0.0    LSH_20170908    최초 등록
 * 0.0.0    LSH_20171201    계약자, 피보험자, 수익자 일치 여부 확인
 * 0.0.0    YJH_20181109    계=피=수 조건 변경 (수익자=피보험자)
 * 0.0.0    NJM_20190919    최근 청구 계좌 & 우체국 계좌 조회 후 화면에 초기세팅
 * 0.0.0    NJM_20191002    (자녀)지급청구 마지막 단계 하얀화면 문제 해결 (3단계에서 고객휴대폰 번호 소실되는 문제 수정)
 * 0.0.0    NJM_20191113    3단계 보험금청구서 작성 - 휴대폰번호 없을시 팝업 2번 뜨는 문제 수정
 * 0.0.0    NJM_20191210    앱접근성 - 버튼 description 설정
 * 0.0.0    NJM_20200122    공통 인증유형/청구유형 추가에 따른 로직수정
 * 0.0.0    NJM_20200213    필수사항 라벨, 선택사항 라벨, 직장 하시는일 레이아웃 gone
 * 0.0.0    YJH_20201210    고객 부담보내역 조회 추가
 * 0.0.0    NJM_20210127    소스정렬
 * 1.5.3    NJM_20210421    [subUrl 공통파일로 변경]
 * =======================================================================
 * copyright : (주)프로니스정보기술 | http://www.phronis.co.kr
 * </pre>
 */
class IUII10M03_F : IUII10M00_FD(), ObjectHandlerMessage {
    private val HANDLERJOB_GET_USERINFO: Int = 0
    private val HANDLERJOB_ERROR_GET_USERINFO: Int = 1
    private val REQUESTCODE_SEARCH_ADDR: Int = 1
    private val length_company: Int = 40
    private val length_job: Int = 100
    private val length_mobile: Int = 13
    private var edtMobile: EditText? = null
    private var edtCompany: EditText? = null
    private var edtJob: EditText? = null
    private var textName: TextView? = null
    private var textResidentNum: TextView? = null
    private var textSMS: TextView? = null
    private var textAddress: TextView? = null
    private var switchingNoti: SwitchingControl? = null
    private var data: Data_IUII10M03_F? = null
    private var handler: WeakReferenceHandler? = null // 핸들러

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        LogPrinter.CF_debug("!----------------------------------------------------------")
        LogPrinter.CF_debug("!-- IUII10M03_F.onCreate()")
        LogPrinter.CF_debug("!----------------------------------------------------------")

        // -- 초기세팅
        data = Data_IUII10M03_F()
        handler = WeakReferenceHandler(this)
        LogPrinter.CF_debug("!---- (3단계) 인증구분 : " + mActivity!!.CF_getAuthDvsn())
    }

    @SuppressLint("InflateParams")
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.iuii10m03_f, null)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        LogPrinter.CF_debug("!----------------------------------------------------------")
        LogPrinter.CF_debug("!-- IUII10M03_F.onActivityCreated()")
        LogPrinter.CF_debug("!----------------------------------------------------------")
        if (savedInstanceState != null) {
            if (savedInstanceState.containsKey("data")) {
                data = savedInstanceState.getParcelable("data")
            }
        }

        // -- UI 생성 및 세팅
        setUIControl()

        // -- 데이터 복구
        restoreData()
    }

    override fun onActivityResult(p_requestCode: Int, p_resultCode: Int, p_data: Intent?) {
        super.onActivityResult(p_requestCode, p_resultCode, p_data)
        LogPrinter.CF_debug("!----------------------------------------------------------")
        LogPrinter.CF_debug("!-- IUII10M03_F.onActivityResult()")
        LogPrinter.CF_debug("!----------------------------------------------------------")
        if (p_requestCode == REQUESTCODE_SEARCH_ADDR && p_resultCode == Activity.RESULT_OK) {
            if (p_data != null) {
                // ---------------------------------------------------------------------------------
                //  주소검색 및 상세주소 입력 완료 시
                // ---------------------------------------------------------------------------------
                var zipNo: String? = ""
                var townName: String? = ""
                var addrRoad: String? = ""
                var addrDetail: String? = ""
                if (p_data.hasExtra("zipNo")) {
                    zipNo = p_data.extras!!.getString("zipNo")
                }
                if (p_data.hasExtra("townName")) {
                    townName = p_data.extras!!.getString("townName")
                }
                if (p_data.hasExtra("addrRoad")) {
                    addrRoad = p_data.extras!!.getString("addrRoad")
                }
                if (p_data.hasExtra("addrDetail")) {
                    addrDetail = p_data.extras!!.getString("addrDetail")
                }
                setAddressText(zipNo, addrRoad, addrDetail)

                // ---------------------------------------------------------------------------------
                //  접근성 ON : 입력하신 주소를 적용합니다.
                // ---------------------------------------------------------------------------------
                if (CommonFunction.CF_checkAccessibilityTurnOn(activity)) {
                    showCustomDialog("입력하신 주소를 적용합니다.", (textAddress)!!)
                }
            }
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        LogPrinter.CF_debug("!----------------------------------------------------------")
        LogPrinter.CF_debug("!-- IUII10M03_F.onSaveInstanceState()")
        LogPrinter.CF_debug("!----------------------------------------------------------")
        saveData()
        outState.putParcelable("data", data)
    }

    /**
     * UI 생성 및 세팅 함수
     */
    private fun setUIControl() {
        LogPrinter.CF_debug("!----------------------------------------------------------")
        LogPrinter.CF_debug("!-- IUII10M03_F.setUIControl()")
        LogPrinter.CF_debug("!----------------------------------------------------------")

        textName = view!!.findViewById(R.id.textName)
        textResidentNum = view!!.findViewById(R.id.textResidentNumber)
        textAddress = view!!.findViewById(R.id.textAddress)

        // -- 휴대폰 번호
        edtMobile = view!!.findViewById(R.id.edtMobile)
        edtMobile?.filters = CommonFunction.CF_getInputLengthFilter((length_mobile))
        edtMobile?.addTextChangedListener(PhoneNumberFormattingTextWatcher())

        // -- 직장명
        edtCompany = view!!.findViewById(R.id.edtCompany)
        edtCompany?.filters = CommonFunction.CF_getInputLengthFilter((length_company))

        // -- 하시는일
        edtJob = view!!.findViewById(R.id.edtJob)
        edtJob?.filters = CommonFunction.CF_getInputLengthFilter((length_job))

        // -- 휴대폰 번호 변경 버튼
        val btnModMobile: ImageButton = view!!.findViewById(R.id.btnMod)
        btnModMobile.setOnClickListener { resetMobile() }

        // -- 주소검색 버튼
        val btnSearchAddr: LinearLayout = view!!.findViewById(R.id.linBtnSearchAddr)
        btnSearchAddr.setOnClickListener {
            if (CommonFunction.CF_checkAccessibilityTurnOn(activity)) {
                val customDialog = CustomDialog((activity)!!)
                customDialog.show()
                customDialog.CF_setBackKeyMode(CustomDialog.BackMode.CANCELED)
                customDialog.CF_setTextContent(resources.getString(R.string.dlg_accessible_move_iucof0m00))
                customDialog.CF_setDoubleButtonText(resources.getString(R.string.btn_cancel), resources.getString(R.string.btn_ok))
                customDialog.setOnDismissListener { dialog ->
                    if (!(dialog as CustomDialog).CF_getCanceled()) {
                        startSearchAddressActivity()
                    } else {
                        Handler().postDelayed(object : Runnable {
                            override fun run() {
                                btnSearchAddr.sendAccessibilityEvent(AccessibilityEvent.TYPE_VIEW_FOCUSED)
                            }
                        }, 500)
                    }
                }
            } else {
                startSearchAddressActivity()
            }
        }

        // -- 알림서비스 신청여부 버튼 UI 세팅
        setNotifyServiceAgreeButtonUI()
        scrollView = view!!.findViewById(R.id.scrollView)

        // -- 다음 버튼
        btnNext = view!!.findViewById(R.id.btnFill)
        btnNext?.text = resources.getString(R.string.btn_next_2)
        btnNext?.setOnClickListener(object : View.OnClickListener {
            override fun onClick(v: View) {
                LogPrinter.CF_debug("!----------------------------------------------------------")
                LogPrinter.CF_debug("!-- IUII10M03_F.btnNext.setOnClickListener()")
                LogPrinter.CF_debug("!----------------------------------------------------------")

                // ---------------------------------------------------------------------------------
                //  키패드 가리기
                // ---------------------------------------------------------------------------------
                if (activity!!.currentFocus != null) CommonFunction.CF_closeVirtualKeyboard(activity, activity!!.currentFocus!!.windowToken)
                if (checkUserInput()) {

                    // -- 입력 데이터 저장
                    val tmp_data: Data_IUII10M00? = mActivity!!.CF_getData()

                    // ---- 휴대폰번호 : 휴대폰번호 자릿수로 자르기
                    val tmp_mobile: String = edtMobile?.text.toString().trim().replace("-", "")
                    LogPrinter.CF_debug("!---- tmp_mobile(dash제거) :$tmp_mobile")
                    tmp_data!!.CF_setS_mobl_no_1(tmp_mobile.substring(0, 3))
                    tmp_data.CF_setS_mobl_no_2(tmp_mobile.substring(3, tmp_mobile.length - 4))
                    tmp_data.CF_setS_mobl_no_3(tmp_mobile.substring(tmp_mobile.length - 4))
                    LogPrinter.CF_debug("!---- 저장된 휴대폰번호" + tmp_data.CF_getS_mobl_no_1() + "/" + tmp_data.CF_getS_mobl_no_2() + "/" + tmp_data.CF_getS_mobl_no_3())

                    tmp_data.CF_setS_infm_serv_rqst_ym(if (switchingNoti!!.CF_getCheckState() > 0) "Y" else "N")
                    tmp_data.CF_setS_cfce_nm(edtCompany?.text.toString().trim())
                    tmp_data.CF_setS_ocpn_nm(edtJob?.text.toString().trim())
                    tmp_data.CF_setS_psno(data!!.CF_getZipNo())
                    tmp_data.CF_setS_bass_addr_nm(data!!.CF_getAddress())
                    tmp_data.CF_setS_dtls_addr_nm(data!!.CF_getAddressDetail())
                    LogPrinter.CF_debug("3단계 -> 4단계 부담보 : " + data!!.CF_getSmbrPsblYn())

                    tmp_data.CF_setB_smbr_psbl(("Y" == data!!.CF_getSmbrPsblYn())) // 부담보내역 추가
                    EventBus.getDefault().post(tmp_data)
                    btnNext?.isEnabled = false
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
        })
    }

    /**
     * 데이터 복구
     */
    private fun restoreData() {
        LogPrinter.CF_debug("!----------------------------------------------------------")
        LogPrinter.CF_debug("!-- IUII10M03_F.restoreData()")
        LogPrinter.CF_debug("!----------------------------------------------------------")

        textName!!.text = data!!.CF_getName()
        textResidentNum!!.text = data!!.CF_getBirth()
        edtMobile!!.setText(data!!.CF_getMobile())
        switchingNoti!!.CF_setCheck(data!!.CF_getAgreeAlarm())
        if (switchingNoti!!.CF_getCheckState() > 0) {
            textSMS!!.contentDescription = resources.getString(R.string.desc_sms_agree_yes)
        } else {
            textSMS!!.contentDescription = resources.getString(R.string.desc_sms_agree_no)
        }
        edtCompany!!.setText(data!!.CF_getCompany())
        edtJob!!.setText(data!!.CF_getJob())
        setAddressText(data!!.CF_getZipNo(), data!!.CF_getAddress(), data!!.CF_getAddressDetail())

        // 사용자 정보 요청을 한번이라도 한 경우 자동 포커스 영향으로 스크롤 이동 되는 것을 막기 위해
        // 처리한 포커스 값 변경
        if (!TextUtils.isEmpty(textName!!.text.toString().trim())) {
            textName!!.requestFocus()
            textName!!.isFocusableInTouchMode = true
            edtMobile!!.isFocusableInTouchMode = true
            edtCompany!!.isFocusableInTouchMode = true
            edtJob!!.isFocusableInTouchMode = true
        }
    }

    /**
     * 데이터 저장
     */
    private fun saveData() {
        LogPrinter.CF_debug("!----------------------------------------------------------")
        LogPrinter.CF_debug("!-- IUII10M03_F.saveData()")
        LogPrinter.CF_debug("!----------------------------------------------------------")

        data!!.CF_setName(textName!!.text.toString().trim())
        data!!.CF_setBirth(textResidentNum!!.text.toString().trim())
        data!!.CF_setMobile(edtMobile!!.text.toString().trim())
        data!!.CF_setAgreeAlarm(switchingNoti!!.CF_getCheckState())
        data!!.CF_setCompany(edtCompany!!.text.toString().trim())
        data!!.CF_setJob(edtJob!!.text.toString().trim())
    }

    /**
     * 알림서비스 신청여부 UI 세팅 함수
     */
    private fun setNotifyServiceAgreeButtonUI() {
        LogPrinter.CF_debug("!----------------------------------------------------------")
        LogPrinter.CF_debug("!-- IUII10M03_F.setNotifyServiceAgreeButtonUI()")
        LogPrinter.CF_debug("!----------------------------------------------------------")

        textSMS = view!!.findViewById(R.id.textLabelSMS)
        switchingNoti = view!!.findViewById(R.id.switchingNoti)
        switchingNoti?.CF_getBtnLeft()?.contentDescription = resources.getString(R.string.desc_sms_agree_yes_btn)
        switchingNoti?.CF_getBtnRight()?.contentDescription = resources.getString(R.string.desc_sms_agree_no_btn)
        // -----------------------------------------------------------------------------------------
        //  기본 체크 상태는 restoreData() 에서 세팅 되며, Data_IUII12M00_F의
        // -----------------------------------------------------------------------------------------
//        switchingNoti.CF_setCheck(1);
        switchingNoti?.CE_setOnChangedCheckedStateEventListener { p_flagCheck ->
            if (p_flagCheck) {
                textSMS?.contentDescription = resources.getString(R.string.desc_sms_agree_yes)
            } else {
                textSMS?.contentDescription = resources.getString(R.string.desc_sms_agree_no)
            }
        }
    }

    /**
     * 휴대폰번호 초기화 후 입력 키패드 Show
     */
    private fun resetMobile() {
        LogPrinter.CF_debug("!----------------------------------------------------------")
        LogPrinter.CF_debug("!-- IUII10M03_F.resetMobile()")
        LogPrinter.CF_debug("!----------------------------------------------------------")

        edtMobile!!.setText("")
        CommonFunction.CF_showVirtualKeyboard(activity, edtMobile)
    }

    /**
     * 주소검색 Activity 호출 함수
     */
    private fun startSearchAddressActivity() {
        LogPrinter.CF_debug("!----------------------------------------------------------")
        LogPrinter.CF_debug("!-- IUII10M03_F.startSearchAddressActivity()")
        LogPrinter.CF_debug("!----------------------------------------------------------")

        val intent = Intent(activity, IUCOF0M00::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
        startActivityForResult(intent, REQUESTCODE_SEARCH_ADDR)
    }

    /**
     * (CF_requestUserInfo() 결과처리) Https 통신을 통해 JsonData를 수신하여 사용자(청구자) 정보 세팅
     * <pre>
     * NJM_20200213 직장명, 하시는일 주석처리
    </pre> *
     * @param p_jsonObject      JSONObject
     */
    @SuppressLint("SetTextI18n")
    @Throws(JSONException::class)
    private fun setUserInfo(p_jsonObject: JSONObject) {
        LogPrinter.CF_debug("!----------------------------------------------------------")
        LogPrinter.CF_debug("!-- IUII10M03_F.setUserInfo()")
        LogPrinter.CF_debug("!----------------------------------------------------------")

        // -- 사용자정보
        val jsonKey_s_rnno: String = "s_rnno" // 주민등록변환 --> 생년월일 앞 6자리
        //        final String jsonKey_s_csno              = "s_csno";                    // 고객번호
        val jsonKey_s_cust_krnm: String = "s_cust_krnm" // 고객한글명
        val jsonKey_s_mobl_no_area_dvsn: String = "s_mobl_no_area_dvsn" // 휴대폰번호 1
        val jsonKey_s_mobl_no_brn_code: String = "s_mobl_no_brn_code" // 휴대폰번호 2
        val jsonKey_s_mobl_no_sqnu: String = "s_mobl_no_sqnu" // 휴대폰번호 3
        val jsonKey_s_psno1: String = "s_psno1" // 집 우편번호
        val jsonKey_s_bass_addr_nm1: String = "s_bass_addr_nm1" // 집 주소
        val jsonKey_s_dtls_addr_nm1: String = "s_dtls_addr_nm1" // 집 주소 상세
        val jsonKey_s_psno2: String = "s_psno2" // 직장 우편번호
        val jsonKey_s_bass_addr_nm2: String = "s_bass_addr_nm2" // 직장 주소
        val jsonKey_s_dtls_addr_nm2: String = "s_dtls_addr_nm2" // 직장 주소 상세
        //        final String jsonKey_s_ofce_nm           = "s_ofce_nm";                 // 직장명
//        final String jsonKey_s_ocpt_nm           = "s_ocpt_nm";                 // 하시는 일

        // 최근청구계좌
        val jsonKey_s_fnis_code: String = "s_fnis_code" // 최근청구계좌 은행코드
        val jsonKey_s_fnis_nm: String = "s_fnis_nm" // 최근청구계좌 은행명
        val jsonKey_s_acno: String = "s_acno" // 최근청구계좌 계좌번호
        val jsonKey_s_dpow_nm: String = "s_dpow_nm" // 최근청구계좌 예금주

        // 고객 보유 우체국 계좌 리스트
        val jsonKey_accList: String = "accList" // 고객 우체국 계좌 리스트


        // -- 최근청구계좌 : JsonObject 파싱 후 Activity 변수에서 SET
        if (p_jsonObject.has(jsonKey_s_fnis_code)) {
            mActivity!!.CF_setsLastFnisCode(p_jsonObject.getString(jsonKey_s_fnis_code))
        }
        if (p_jsonObject.has(jsonKey_s_fnis_nm)) {
            mActivity!!.CF_setsLastFnisNm(p_jsonObject.getString(jsonKey_s_fnis_nm))
        }
        if (p_jsonObject.has(jsonKey_s_acno)) {
            mActivity!!.CF_setsLastAcno(p_jsonObject.getString(jsonKey_s_acno))
        }
        if (p_jsonObject.has(jsonKey_s_dpow_nm)) {
            mActivity!!.CF_setsLastDpowNm(p_jsonObject.getString(jsonKey_s_dpow_nm))
        }

        // --<1> 고객 보유 우체국 계좌 리스트 : JsonArray 파싱 후 Activity 변수에서 SET
        if (p_jsonObject.has(jsonKey_accList)) {
            val tmp_jsonArray_accList: JSONArray = p_jsonObject.getJSONArray(jsonKey_accList)

            // --<2> (우체국계좌리스트 존재함)
            if (tmp_jsonArray_accList.length() > 0) {
                LogPrinter.CF_debug("!--<2> 우체국계좌리스트 있음")
                val tmp_accList: Array<String?> = arrayOfNulls(tmp_jsonArray_accList.length())
                for (i in 0 until tmp_jsonArray_accList.length()) {
                    tmp_accList[i] = tmp_jsonArray_accList.getJSONObject(i).getString("s_acno")
                    LogPrinter.CF_debug("!---- 우체국 계좌번호::::" + tmp_accList.get(i))
                }
                LogPrinter.CF_debug("!---- 계좌리스트 변수 길이::::" + tmp_accList.size)
                mActivity!!.CF_setAccList(tmp_accList)
            } else {
                LogPrinter.CF_debug("!--<2> 우체국계좌리스트 없음")
            }
        }

        // 사용자정보
        val tmp_birth: String = p_jsonObject.getString(jsonKey_s_rnno)
        val tmp_name: String = p_jsonObject.getString(jsonKey_s_cust_krnm)
        val tmp_mobile: String = p_jsonObject.getString(jsonKey_s_mobl_no_area_dvsn) + p_jsonObject.getString(jsonKey_s_mobl_no_brn_code) + p_jsonObject.getString(jsonKey_s_mobl_no_sqnu)
        //        String tmp_company       = p_jsonObject.getString(jsonKey_s_ofce_nm);
        val tmp_psno_1: String = p_jsonObject.getString(jsonKey_s_psno1)
        val tmp_addr_1: String = p_jsonObject.getString(jsonKey_s_bass_addr_nm1)
        val tmp_addr_detail_1: String = p_jsonObject.getString(jsonKey_s_dtls_addr_nm1)
        val tmp_psno_2: String = p_jsonObject.getString(jsonKey_s_psno2)
        val tmp_addr_2: String = p_jsonObject.getString(jsonKey_s_bass_addr_nm2)
        val tmp_addr_detail_2: String = p_jsonObject.getString(jsonKey_s_dtls_addr_nm2)
        //        String tmp_job           = "";

        // 하시는 일
//        if(p_jsonObject.has(jsonKey_s_ocpt_nm)){
//            tmp_job = p_jsonObject.getString(jsonKey_s_ocpt_nm);
//        }

        // -- 로그인시 이름과 사용자정보조회에서 나온 이름이 매칭 되는지 확인
        if ((tmp_name == mActivity!!.CF_getData()!!.CF_getLoginUserName())) {
            textName!!.setText(tmp_name)
        }
        else {
            // TODO : 매칭이 안될때(else) 에러 처리 필요함
            textName!!.text = mActivity!!.CF_getData()!!.CF_getLoginUserName()
            LogPrinter.CF_error("!---- 이름매칭 안됨::::전문정보:{} / 저장된이름:{}" + tmp_name, mActivity!!.CF_getData()!!.CF_getLoginUserName())
        }
        edtMobile!!.setText(tmp_mobile) // 휴대폰번호

        // -- 직장명/하시는일
//        edtCompany.setText(tmp_company);    // 직장명
//        edtJob.setText(tmp_job);            // 하시는일
        edtCompany!!.setText("")
        edtJob!!.setText("")

        // -- PhoneNumber 포맷팅 적용이 안되는 경우가 있기에 확인 후 hypen 추가
        var tmp_edtMobile: String = edtMobile!!.text.toString().trim()

        // 01X-XXX(X)-XXXX 포맷팅이 아니면서 && hypen 제거한 전화번호 형식이 맞지 않는 경우
        if (!Regex.CF_MacherMobile(tmp_edtMobile) && Regex.CF_MacherMobileNoHypen(tmp_edtMobile.replace("-", ""))) {
            tmp_edtMobile = tmp_edtMobile.replace("-", "")
            edtMobile!!.setText((tmp_edtMobile.substring(0, 3) + "-" +
                    tmp_edtMobile.substring(3, tmp_edtMobile.length - 4) + "-" +
                    tmp_edtMobile.substring(tmp_edtMobile.length - 4)))
        }

        // -- 생년월일
        if (!TextUtils.isEmpty(tmp_birth)) {
            textResidentNum!!.text = "$tmp_birth - *******"
        } else {
            textResidentNum!!.text = "****** - *******"
        }

        // -- 주소 : 집주소를 우선 시 하며 집주소가 empty 인 경우 직장 주소를 세팅 한다.
        if (!TextUtils.isEmpty(tmp_psno_1) && !TextUtils.isEmpty(tmp_addr_1)) {
            setAddressText(tmp_psno_1, tmp_addr_1, tmp_addr_detail_1)
        } else if (!TextUtils.isEmpty(tmp_psno_2) && !TextUtils.isEmpty(tmp_addr_2)) {
            setAddressText(tmp_psno_2, tmp_addr_2, tmp_addr_detail_2)
        } else {
            textAddress!!.text = ""
        }
    }

    /**
     * 주소 세팅 함수
     * @param p_postNo      String
     * @param p_addr        String
     * @param p_addrDetail  String
     */
    private fun setAddressText(p_postNo: String?, p_addr: String?, p_addrDetail: String?) {
        LogPrinter.CF_debug("!----------------------------------------------------------")
        LogPrinter.CF_debug("!-- IUII10M03_F.setAddressText()")
        LogPrinter.CF_debug("!----------------------------------------------------------")
        if (!TextUtils.isEmpty(p_postNo) && !TextUtils.isEmpty(p_addr)) {
            var tmp_addrFull: String? = "[" + p_postNo + "]\r\n" + p_addr
            if (!TextUtils.isEmpty(p_addrDetail)) {
                tmp_addrFull += "\r\n" + p_addrDetail
            }
            textAddress!!.text = tmp_addrFull
            data!!.CF_setZipNo(p_postNo)
            data!!.CF_setAddress(p_addr)
            data!!.CF_setAddressDetail(p_addrDetail)
        }
    }

    /**
     * 사용자 입력값 검사 함수<br></br>
     * 필수 값만 검사한다.(휴대폰번호, 주소)
     * @return      boolean
     */
    private fun checkUserInput(): Boolean {
        LogPrinter.CF_debug("!----------------------------------------------------------")
        LogPrinter.CF_debug("!-- IUII10M03_F.checkUserInput()")
        LogPrinter.CF_debug("!----------------------------------------------------------")
        var tmp_flagOk = true

        // --<> 휴대폰번호 empty
        if (TextUtils.isEmpty(edtMobile!!.text.toString().trim())) {
            tmp_flagOk = false
            showCustomDialogAndFocus(resources.getString(R.string.dlg_empty_mobile), edtMobile)
        }
        // --<> 휴대폰번호 정규식 에러
        else if (!Regex.CF_MacherMobileNoHypen(edtMobile!!.text.toString().trim().replace("-", ""))) {
            // 휴대폰번호 검사
            tmp_flagOk = false
            showCustomDialogAndFocus(resources.getString(R.string.dlg_regex_mobile), edtMobile)
        }
        // --<> 주소 empty
        else if (TextUtils.isEmpty(textAddress!!.text.toString().trim())) {
            tmp_flagOk = false
            CommonFunction.CF_showCustomAlertDilaog(activity, resources.getString(R.string.dlg_empty_address), getResources().getString(R.string.btn_ok))
        }
        return tmp_flagOk
    }

    /**
     * IUII10M03_F.데이터 반환
     * @return  Data_IUII10M03_F
     */
    fun CF_getData(): Data_IUII10M03_F? {
        LogPrinter.CF_debug("!----------------------------------------------------------")
        LogPrinter.CF_debug("!-- IUII10M03_F.CF_getData()")
        LogPrinter.CF_debug("!----------------------------------------------------------")
        return data
    }
    // #############################################################################################
    //  Dialog 관련
    // #############################################################################################
    /**
     * 다이얼로그 팝업 후 EditText 포커스 이동
     * @param p_message     String
     * @param p_editText    EditText
     */
    private fun showCustomDialogAndFocus(p_message: String, p_editText: EditText?) {
        LogPrinter.CF_debug("!----------------------------------------------------------")
        LogPrinter.CF_debug("!-- IUII10M03_F.showCustomDialogAndFocus()")
        LogPrinter.CF_debug("!----------------------------------------------------------")

        val customDialog = CustomDialog((activity)!!)
        customDialog.show()
        customDialog.CF_setTextContent(p_message)
        customDialog.CF_setSingleButtonText(getResources().getString(R.string.btn_ok))
        customDialog.setOnDismissListener {
            if (p_editText != null) {
                CommonFunction.CF_showVirtualKeyboard(activity, p_editText)
            }
        }
    }

    // #############################################################################################
    //  HTTP 호출
    // #############################################################################################
    override fun handleMessage(p_message: Message) {
        LogPrinter.CF_debug("!----------------------------------------------------------")
        LogPrinter.CF_debug("!-- IUII10M03_F.handleMessage()")
        LogPrinter.CF_debug("!----------------------------------------------------------")

        if (mActivity != null && !mActivity!!.isDestroyed) {
            mActivity!!.CF_dismissProgressDialog()
            when (p_message.what) {
                HANDLERJOB_GET_USERINFO -> try {
                    httpRes_serInfo(JSONObject(p_message.obj as String?))
                } catch (e: JSONException) {
                    LogPrinter.CF_debug(getResources().getString(R.string.log_json_exception))
                    mActivity!!.CF_OnError(getResources().getString(R.string.dlg_error_server))
                }
                HANDLERJOB_ERROR_GET_USERINFO -> mActivity!!.CF_OnError(p_message.obj as String?)
                else -> {
                }
            }
        }
    }

    /**
     * 사용자 기본정보 요청 함수
     */
    fun httpReq_serInfo() {
        LogPrinter.CF_debug("!----------------------------------------------------------")
        LogPrinter.CF_debug("!-- IUII10M03_F.httpReq_serInfo()")
        LogPrinter.CF_debug("!----------------------------------------------------------")

        // --<1> (최초요청) 이름 정보가 없는 경우에만 요청
        if (TextUtils.isEmpty(textName!!.text.toString().trim())) {
            mActivity!!.CF_showProgressDialog()
            var sUrl: String? = ""
            val builder: Uri.Builder = Uri.Builder()
            builder.appendQueryParameter("csno", SharedPreferencesFunc.getLoginCsno(requireActivity()))

            // --<2> (휴대폰인증 청구)
            if (mActivity!!.CF_getAuthDvsn() == AuthDvsn.MOBILE) {
                sUrl = EnvConfig.host_url + EnvConfig.URL_CLAIM_SELF_MOBILE_USER_INFO_INQ
            } else {
                sUrl = EnvConfig.host_url + EnvConfig.URL_CLAIM_SELF_CERT_USER_INFO_INQ
                builder.appendQueryParameter("tempKey", SharedPreferencesFunc.getWebTempKey(activity))
            }
            HttpConnections.sendPostData(
                sUrl,
                builder.build().encodedQuery,
                handler,
                HANDLERJOB_GET_USERINFO,
                HANDLERJOB_ERROR_GET_USERINFO)
        }
    }

    /**
     * 사용자 기본정보 요청 결과 처리 함수
     * @param p_jsonObject  JSONObject
     */
    @Throws(JSONException::class)
    private fun httpRes_serInfo(p_jsonObject: JSONObject) {
        LogPrinter.CF_debug("!----------------------------------------------------------")
        LogPrinter.CF_debug("!-- IUII10M03_F.httpRes_serInfo()")
        LogPrinter.CF_debug("!----------------------------------------------------------")

        // 프로그레스 다이얼로그 dismiss
        val jsonKey_errorCode = "errCode"
        val jsonKey_data = "data"
        val jsonKey_s_cttr_isrt_smns_yn = "s_cttr_isrt_smns_yn" // 피보험자,수익자 일치 여부 "Y" or "N"
        val tmp_errorCode: String

        // --<1>
        if (p_jsonObject.has(jsonKey_errorCode)) {
            tmp_errorCode = p_jsonObject.getString(jsonKey_errorCode)

            // --<2> 에러코드 수신
            if ((tmp_errorCode == "ERRIUII12M00001") || (tmp_errorCode == "ERRIUII12M00002") || (tmp_errorCode == "ERRIUII12M01001") || (tmp_errorCode == "ERRIUII12M01002")) { // 고객정보가 없는 경우
                mActivity!!.CF_OnError(resources.getString(R.string.dlg_error_erriuii12m00001))
            }
            // --<2> jsonKey_data 성공
            else if (p_jsonObject.has(jsonKey_data)) {
                val tmp_jsonData: JSONObject = p_jsonObject.getJSONObject(jsonKey_data)
                // ---------------------------------------------------------------------------------
                // 고객의 보험 계약 정보 확인 : 수익자이면서 피보험자인 경우만 청구 가능
                // ---------------------------------------------------------------------------------
                // --<3>
                if (tmp_jsonData.has(jsonKey_s_cttr_isrt_smns_yn)) {
                    val tmp_strAvailable: String = tmp_jsonData.getString(jsonKey_s_cttr_isrt_smns_yn)
                    when (tmp_strAvailable.toLowerCase(Locale.getDefault())) {
                        "e" -> mActivity!!.CF_OnError(resources.getString(R.string.dlg_error_erriuii12m00003))
                        "s" -> mActivity!!.CF_OnError("청구 가능한 보장성 보험이 없습니다.\n가입하신 보험내역을 확인해 주십시오.")
                        "y" -> {
                            val jsonKey_s_smbr_yn: String = "s_smbr_yn"
                            // --<5> 부담보존재 여부
                            if (tmp_jsonData.has(jsonKey_s_smbr_yn)) {
                                val tmp_smbr_yn: String = tmp_jsonData.getString(jsonKey_s_smbr_yn)
                                // (존재) IUII10M04_F 에서 안내 및 웹뷰(부담보리스트) 연계
                                if (("Y" == tmp_smbr_yn)) {
                                    data!!.CF_setSmbrPsblYn("Y")
                                } else {
                                    data!!.CF_setSmbrPsblYn("N")
                                }
                            }

                            // -- 사용자 데이터 세팅
                            setUserInfo(tmp_jsonData)

                            // EditText 포커스 영향으로 스크롤 변경되는 것을 막기위함으로 강제 포커스 세팅
                            textName!!.requestFocus()
                            textName!!.isFocusableInTouchMode = true
                            edtMobile!!.isFocusableInTouchMode = true
                            edtCompany!!.isFocusableInTouchMode = true
                            edtJob!!.isFocusableInTouchMode = true
                            if (CommonFunction.CF_checkAccessibilityTurnOn(activity)) {
                                mActivity!!.CF_requestFocusIndicator()
                            }
                        }
                        else -> {
                            val customDialog = CustomDialog((activity)!!)
                            customDialog.show()
                            customDialog.CF_setBackKeyMode(CustomDialog.BackMode.NOTUSE)
                            customDialog.CF_setTextContent(resources.getString(R.string.dlg_not_available_req))
                            customDialog.CF_setSingleButtonText(resources.getString(R.string.btn_ok))
                            customDialog.setOnDismissListener { dialog ->
                                if (!(dialog as CustomDialog).CF_getCanceled()) {
                                    mActivity!!.setResult(Activity.RESULT_CANCELED)
                                    mActivity!!.finish()
                                }
                            }
                        }
                    }
                } else {
                    // 운영서버 미적용으로 임시 팝업
                    mActivity!!.CF_OnError(resources.getString(R.string.dlg_error_erriuii12m00003))
                }
            } else {
                mActivity!!.CF_OnError(resources.getString(R.string.dlg_error_server_2))
            }
        } else {
            mActivity!!.CF_OnError(resources.getString(R.string.dlg_error_server_1))
        }
    }
}