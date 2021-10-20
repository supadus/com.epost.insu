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
import android.widget.*
import androidx.core.widget.NestedScrollView
import com.epost.insu.EnvConfig
import com.epost.insu.R
import com.epost.insu.SharedPreferencesFunc
import com.epost.insu.activity.IUCOF0M00
import com.epost.insu.common.CommonFunction
import com.epost.insu.common.CustomSQLiteHelper
import com.epost.insu.common.LogPrinter
import com.epost.insu.common.Regex
import com.epost.insu.control.SwitchingControl
import com.epost.insu.data.Data_IUII10M03_F
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
 * @pakage   : com.epost.insu.fragment
 * @fileName  : IUBC12M00_F.java
 *
 * @Title     : 스마트보험금청구 > 보험청구서작성(개인정보) (화면 ID : IUBC12M00)
 * @author    : 이경민
 * @created   : 2018-08-21
 * @version   : 1.0
 *
 * @note      : 스마트보험금청구 > 보험청구서작성(개인정보) (화면 ID : IUBC12M00) <br></br>
 * 보험금지급청구 3단계 ::: 개인정보 확인 및 입력
 * ======================================================================
 * 수정 내역
 * NO      날짜          작업자       내용
 * 01      2018-08-21    이경민       최초 등록
 * 02      2018-11-09    양지훈       계=피=수 조건 변경 (수익자=피보험자)
 * 03      2020-02-13    노지민       필수사항 라벨, 선택사항 라벨, 직장 하시는일 레이아웃 gone
 * =======================================================================
 */
class IUBC12M00_F() : IUBC10M00_FD(), ObjectHandlerMessage {
    private val subUrl_getUserInfo = "/II/IUII12M00.do"
    private val HANDLERJOB_GET_USERINFO = 0
    private val HANDLERJOB_ERROR_GET_USERINFO = 1
    private val REQUESTCODE_SEARCH_ADDR = 1
    private val length_company = 40
    private val length_job = 100
    private val length_mobile = 13
    private var edtMobile: EditText? = null
    private var edtCompany: EditText? = null
    private var edtJob: EditText? = null
    private var textName: TextView? = null
    private var textResidentNum: TextView? = null
    private var textSMS: TextView? = null
    private var textAddress: TextView? = null
    private var switchingNoti: SwitchingControl? = null
    private var data: Data_IUII10M03_F? = null
    private var handler // 핸들러
            : WeakReferenceHandler? = null

    override fun handleMessage(p_message: Message) {
        if (mActivity != null && !mActivity!!.isDestroyed) {
            mActivity!!.CF_dismissProgressDialog()
            when (p_message.what) {
                HANDLERJOB_GET_USERINFO -> try {
                    setResultOfUserInfo(JSONObject(p_message.obj as String))
                } catch (e: JSONException) {
                    LogPrinter.CF_debug(resources.getString(R.string.log_json_exception))
                    mActivity!!.CF_OnError(resources.getString(R.string.dlg_error_server))
                }
                HANDLERJOB_ERROR_GET_USERINFO -> mActivity!!.CF_OnError(p_message.obj as String)
                else -> {
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setInit()
    }

    @SuppressLint("InflateParams")
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.iubc12m00_f, null)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        if (savedInstanceState != null) {
            if (savedInstanceState.containsKey("data")) {
                data = savedInstanceState.getParcelable("data")
            }
        }

        // UI 생성 및 세팅
        setUIControl()

        // 데이터 복구
        restoreData()
    }

    override fun onActivityResult(p_requestCode: Int, p_resultCode: Int, p_data: Intent?) {
        super.onActivityResult(p_requestCode, p_resultCode, p_data)
        if (p_requestCode == REQUESTCODE_SEARCH_ADDR && p_resultCode == Activity.RESULT_OK) {
            if (p_data != null) {
                // ---------------------------------------------------------------------------------
                //  주소검색 및 상세주소 입력 완료 시
                // ---------------------------------------------------------------------------------
                var tmp_zipNo: String? = ""
                var tmp_townName: String? = ""
                var tmp_addrRoad: String? = ""
                var tmp_addrDetail: String? = ""
                if (p_data.hasExtra("zipNo")) {
                    tmp_zipNo = p_data.extras!!.getString("zipNo")
                }
                if (p_data.hasExtra("townName")) {
                    tmp_townName = p_data.extras!!.getString("townName")
                }
                if (p_data.hasExtra("addrRoad")) {
                    tmp_addrRoad = p_data.extras!!.getString("addrRoad")
                }
                if (p_data.hasExtra("addrDetail")) {
                    tmp_addrDetail = p_data.extras!!.getString("addrDetail")
                }
                setAddressText(tmp_zipNo, tmp_addrRoad, tmp_addrDetail)

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
        saveData()
        outState.putParcelable("data", data)
    }

    /**
     * 초기 세팅 함수
     */
    private fun setInit() {
        data = Data_IUII10M03_F()
        handler = WeakReferenceHandler(this)
    }

    /**
     * UI 생성 및 세팅 함수
     */
    private fun setUIControl() {
        textName = view!!.findViewById<View>(R.id.textName) as TextView
        textResidentNum = view!!.findViewById<View>(R.id.textResidentNumber) as TextView
        edtMobile = view!!.findViewById<View>(R.id.edtMobile) as EditText
        edtMobile!!.filters = CommonFunction.CF_getInputLengthFilter((length_mobile))
        edtMobile!!.addTextChangedListener(PhoneNumberFormattingTextWatcher())
        edtCompany = view!!.findViewById<View>(R.id.edtCompany) as EditText
        edtCompany!!.filters = CommonFunction.CF_getInputLengthFilter((length_company))
        edtJob = view!!.findViewById<View>(R.id.edtJob) as EditText
        edtJob!!.filters = CommonFunction.CF_getInputLengthFilter((length_job))
        val tmp_btnModMobile = view!!.findViewById<View>(R.id.btnMod) as ImageButton
        tmp_btnModMobile.setOnClickListener(View.OnClickListener { resetMobile() })
        textAddress = view!!.findViewById<View>(R.id.textAddress) as TextView

        // 주소검색 버튼
        val tmp_btnSearchAddr = view!!.findViewById<View>(R.id.linBtnSearchAddr) as LinearLayout
        tmp_btnSearchAddr.setOnClickListener(object : View.OnClickListener {
            override fun onClick(v: View) {
                if (CommonFunction.CF_checkAccessibilityTurnOn(activity)) {
                    val tmp_dlg = CustomDialog((activity)!!)
                    tmp_dlg.show()
                    tmp_dlg.CF_setBackKeyMode(CustomDialog.BackMode.CANCELED)
                    tmp_dlg.CF_setTextContent(resources.getString(R.string.dlg_accessible_move_iucof0m00))
                    tmp_dlg.CF_setDoubleButtonText(resources.getString(R.string.btn_cancel), resources.getString(R.string.btn_ok))
                    tmp_dlg.setOnDismissListener(object : DialogInterface.OnDismissListener {
                        override fun onDismiss(dialog: DialogInterface) {
                            if ((dialog as CustomDialog).CF_getCanceled() == false) {
                                startSearchAddressActivity()
                            } else {
                                Handler().postDelayed(object : Runnable {
                                    override fun run() {
                                        tmp_btnSearchAddr.sendAccessibilityEvent(AccessibilityEvent.TYPE_VIEW_FOCUSED)
                                    }
                                }, 500)
                            }
                        }
                    })
                } else {
                    startSearchAddressActivity()
                }
            }
        })

        // 알림서비스 신청여부 버튼 UI 세팅
        setNotifyServiceAgreeButtonUI()
        scrollView = view!!.findViewById<View>(R.id.scrollView) as NestedScrollView

        // 다음 버튼
        btnNext = view!!.findViewById<View>(R.id.btnFill) as Button
        btnNext!!.text = resources.getString(R.string.btn_next_2)
        btnNext!!.setOnClickListener(object : View.OnClickListener {
            override fun onClick(v: View) {
                // ---------------------------------------------------------------------------------
                //  키패드 가리기
                // ---------------------------------------------------------------------------------
                if (activity!!.currentFocus != null) CommonFunction.CF_closeVirtualKeyboard(activity, activity!!.currentFocus!!.windowToken)
                if (checkUserInput()) {
                    val tmp_data = mActivity!!.CF_getData()
                    val tmp_mobile = edtMobile!!.text.toString().trim { it <= ' ' }.split("-").toTypedArray()
                    if (tmp_mobile.size == 3) {
                        tmp_data!!.CF_setS_mobl_no_1(tmp_mobile[0])
                        tmp_data.CF_setS_mobl_no_2(tmp_mobile[1])
                        tmp_data.CF_setS_mobl_no_3(tmp_mobile[2])
                    }
                    tmp_data!!.CF_setS_infm_serv_rqst_ym(if (switchingNoti!!.CF_getCheckState() > 0) "Y" else "N")
                    tmp_data.CF_setS_cfce_nm(edtCompany!!.text.toString().trim { it <= ' ' })
                    tmp_data.CF_setS_ocpn_nm(edtJob!!.text.toString().trim { it <= ' ' })
                    tmp_data.CF_setS_psno(data!!.CF_getZipNo())
                    tmp_data.CF_setS_bass_addr_nm(data!!.CF_getAddress())
                    tmp_data.CF_setS_dtls_addr_nm(data!!.CF_getAddressDetail())
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
        })
    }

    /**
     * 데이터 복구
     */
    private fun restoreData() {
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
        if (TextUtils.isEmpty(textName!!.text.toString().trim { it <= ' ' }) == false) {
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
        data!!.CF_setName(textName!!.text.toString().trim { it <= ' ' })
        data!!.CF_setBirth(textResidentNum!!.text.toString().trim { it <= ' ' })
        data!!.CF_setMobile(edtMobile!!.text.toString().trim { it <= ' ' })
        data!!.CF_setAgreeAlarm(switchingNoti!!.CF_getCheckState())
        data!!.CF_setCompany(edtCompany!!.text.toString().trim { it <= ' ' })
        data!!.CF_setJob(edtJob!!.text.toString().trim { it <= ' ' })
    }

    /**
     * 알림서비스 신청여부 UI 세팅 함수
     */
    private fun setNotifyServiceAgreeButtonUI() {
        textSMS = view!!.findViewById<View>(R.id.textLabelSMS) as TextView
        switchingNoti = view!!.findViewById<View>(R.id.switchingNoti) as SwitchingControl
        // -----------------------------------------------------------------------------------------
        //  기본 체크 상태는 restoreData() 에서 세팅 되며, Data_IUII12M00_F의
        // -----------------------------------------------------------------------------------------
//        switchingNoti.CF_setCheck(1);
        switchingNoti!!.CE_setOnChangedCheckedStateEventListener(object : OnChangedCheckedStateEventListener {
            override fun onCheck(p_flagCheck: Boolean) {
                if (p_flagCheck) {
                    textSMS!!.contentDescription = resources.getString(R.string.desc_sms_agree_yes)
                } else {
                    textSMS!!.contentDescription = resources.getString(R.string.desc_sms_agree_no)
                }
            }
        })
    }

    /**
     * 휴대폰번호 초기화 후 입력 키패드 Show
     */
    private fun resetMobile() {
        edtMobile!!.setText("")
        CommonFunction.CF_showVirtualKeyboard(activity, edtMobile)
    }

    /**
     * 주소검색 Activity 호출 함수
     */
    private fun startSearchAddressActivity() {
        val tmp_intent = Intent(activity, IUCOF0M00::class.java)
        tmp_intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
        startActivityForResult(tmp_intent, REQUESTCODE_SEARCH_ADDR)
    }

    /**
     * DB에 저장되어 있는 고객식별코드 csno 반환 함수
     * @return
     */
    private val user_csno_fromSqlite: String
        private get() {
            var tmp_csno = ""
            val tmp_helper = CustomSQLiteHelper(activity!!.applicationContext)
            val tmp_sqlite = tmp_helper.readableDatabase
            tmp_csno = tmp_helper.CF_Selectcsno(tmp_sqlite)
            tmp_sqlite.close()
            tmp_helper.close()
            return tmp_csno
        }

    /**
     * 사용자 정보 세팅 함수
     */
    @SuppressLint("SetTextI18n")
    @Throws(JSONException::class)
    private fun setUserInfo(p_jsonObject: JSONObject) {
        val jsonKey_s_rnno = "s_rnno" // 주민등록변환 --> 생년월일 앞 6자리
        val jsonKey_s_csno = "s_csno" // 고객번호
        val jsonKey_s_cust_krnm = "s_cust_krnm" // 고객한글명
        val jsonKey_s_mobl_no_area_dvsn = "s_mobl_no_area_dvsn" // 휴대폰번호 1
        val jsonKey_s_mobl_no_brn_code = "s_mobl_no_brn_code" // 휴대폰번호 2
        val jsonKey_s_mobl_no_sqnu = "s_mobl_no_sqnu" // 휴대폰번호 3
        val jsonKey_s_psno1 = "s_psno1" // 집 우편번호
        val jsonKey_s_bass_addr_nm1 = "s_bass_addr_nm1" // 집 주소
        val jsonKey_s_dtls_addr_nm1 = "s_dtls_addr_nm1" // 집 주소 상세
        val jsonKey_s_psno2 = "s_psno2" // 직장 우편번호
        val jsonKey_s_bass_addr_nm2 = "s_bass_addr_nm2" // 직장 주소
        val jsonKey_s_dtls_addr_nm2 = "s_dtls_addr_nm2" // 직장 주소 상세
        val jsonKey_s_ofce_nm = "s_ofce_nm" // 직장명
        val jsonKey_s_ocpt_nm = "s_ocpt_nm" // 하시는 일
        val tmp_birth = p_jsonObject.getString(jsonKey_s_rnno)
        val tmp_name = p_jsonObject.getString(jsonKey_s_cust_krnm)
        val tmp_mobile = p_jsonObject.getString(jsonKey_s_mobl_no_area_dvsn) + p_jsonObject.getString(jsonKey_s_mobl_no_brn_code) + p_jsonObject.getString(jsonKey_s_mobl_no_sqnu)
        val tmp_company = p_jsonObject.getString(jsonKey_s_ofce_nm)
        val tmp_psno_1 = p_jsonObject.getString(jsonKey_s_psno1)
        val tmp_addr_1 = p_jsonObject.getString(jsonKey_s_bass_addr_nm1)
        val tmp_addr_detail_1 = p_jsonObject.getString(jsonKey_s_dtls_addr_nm1)
        val tmp_psno_2 = p_jsonObject.getString(jsonKey_s_psno2)
        val tmp_addr_2 = p_jsonObject.getString(jsonKey_s_bass_addr_nm2)
        val tmp_addr_detail_2 = p_jsonObject.getString(jsonKey_s_dtls_addr_nm2)
        var tmp_job: String? = ""
        if (p_jsonObject.has(jsonKey_s_ocpt_nm)) {
            tmp_job = p_jsonObject.getString(jsonKey_s_ocpt_nm)
        }
        textName!!.text = tmp_name // 고객명
        edtMobile!!.setText(tmp_mobile) // 휴대폰번호

        // -- 직장명/하시는일               // 2020-02-13 사용안함
        // edtCompany.setText(tmp_company);    // 직장명
        // edtJob.setText(tmp_job);
        edtCompany!!.setText("")
        edtJob!!.setText("")

        // PhoneNumber 포맷팅 적용이 안되는 경우가 있기에 확인 후  hypen 추가
        var tmp_edtMobile = edtMobile!!.text.toString().trim { it <= ' ' }
        if ((Regex.CF_MacherMobile(tmp_edtMobile) == false
                        && Regex.CF_MacherMobileNoHypen(tmp_edtMobile.replace("-", "")))) {
            tmp_edtMobile = tmp_edtMobile.replace("-", "")
            edtMobile!!.setText(tmp_edtMobile.substring(0, 3) + "-" + tmp_edtMobile.substring(3, tmp_edtMobile.length - 4) + "-" + tmp_edtMobile.substring(tmp_edtMobile.length - 4, tmp_edtMobile.length))
        }

        // 생년월일
        if (TextUtils.isEmpty(tmp_birth) == false) {
            textResidentNum!!.text = "$tmp_birth - *******"
        } else {
            textResidentNum!!.text = "****** - *******"
        }

        // 주소
        // 집주소를 우선 시 하며 집주소가 empty 인 경우 직장 주소를 세팅 한다.
        if (TextUtils.isEmpty(tmp_psno_1) == false && TextUtils.isEmpty(tmp_addr_1) == false) {
            setAddressText(tmp_psno_1, tmp_addr_1, tmp_addr_detail_1)
        } else if (TextUtils.isEmpty(tmp_psno_2) == false && TextUtils.isEmpty(tmp_addr_2) == false) {
            setAddressText(tmp_psno_2, tmp_addr_2, tmp_addr_detail_2)
        } else {
            textAddress!!.text = ""
        }
    }

    /**
     * 주소 세팅 함수
     * @param p_postNo
     * @param p_addr
     * @param p_addrDetail
     */
    private fun setAddressText(p_postNo: String?, p_addr: String?, p_addrDetail: String?) {
        if (TextUtils.isEmpty(p_postNo) == false && TextUtils.isEmpty(p_addr) == false) {
            var tmp_addrFull: String = "[$p_postNo]\r\n$p_addr"
            if (TextUtils.isEmpty(p_addrDetail) == false) {
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
     * @return
     */
    private fun checkUserInput(): Boolean {
        var tmp_flagOk = true
        if (TextUtils.isEmpty(edtMobile!!.text.toString().trim { it <= ' ' })) {           // 휴대폰번호 empty
            tmp_flagOk = false
            showCustomDialogAndFocus(resources.getString(R.string.dlg_empty_mobile), edtMobile)
        } else if (Regex.CF_MacherMobileNoHypen(edtMobile!!.text.toString().trim { it <= ' ' }.replace("-", "")) == false) {           // 휴대폰번호 정규식 에러
            // 휴대폰번호 검사
            tmp_flagOk = false
            showCustomDialogAndFocus(resources.getString(R.string.dlg_regex_mobile), edtMobile)
        } else if (TextUtils.isEmpty(textAddress!!.text.toString().trim { it <= ' ' })) {            // 주소 empty
            tmp_flagOk = false
            CommonFunction.CF_showCustomAlertDilaog(activity, resources.getString(R.string.dlg_empty_address), resources.getString(R.string.btn_ok))
        }
        return tmp_flagOk
    }

    /**
     * IUII10M03_F 데이터 반환
     * @return
     */
    fun CF_getData(): Data_IUII10M03_F? {
        return data
    }
    // #############################################################################################
    //  Dialog 관련
    // #############################################################################################
    /**
     * 다이얼로그 팝업 후 EditText 포커스 이동
     * @param p_message
     * @param p_editText
     */
    private fun showCustomDialogAndFocus(p_message: String, p_editText: EditText?) {
        val tmp_dlg = CustomDialog((activity)!!)
        tmp_dlg.show()
        tmp_dlg.CF_setTextContent(p_message)
        tmp_dlg.CF_setSingleButtonText(resources.getString(R.string.btn_ok))
        tmp_dlg.setOnDismissListener(object : DialogInterface.OnDismissListener {
            override fun onDismiss(dialog: DialogInterface) {
                if (p_editText != null) {
                    CommonFunction.CF_showVirtualKeyboard(activity, p_editText)
                }
            }
        })
    }
    // #############################################################################################
    //  Http 관련
    // #############################################################################################
    /**
     * (Https)사용자 기본정보 요청 함수<br></br>
     */
    fun CF_requestUserInfo() {
        if (TextUtils.isEmpty(textName!!.text.toString().trim { it <= ' ' })) {
            // 이름 정보가 없는 경우에만 요청
            mActivity!!.CF_showProgressDialog()
            val tmp_builder = Uri.Builder()
            tmp_builder.appendQueryParameter("csno", user_csno_fromSqlite)
            tmp_builder.appendQueryParameter("tempKey", SharedPreferencesFunc.getWebTempKey(activity))
            tmp_builder.appendQueryParameter("chkSmart", "Y") // 스마트보험금청구 만원의행복, 단체보장보험 제외
            HttpConnections.sendPostData(
                    EnvConfig.host_url + subUrl_getUserInfo,
                    tmp_builder.build().encodedQuery,
                    handler,
                    HANDLERJOB_GET_USERINFO,
                    HANDLERJOB_ERROR_GET_USERINFO)
        }
    }

    /**
     * (Https result)사용자 기본정보 요청결과 처리 함수
     * @param p_jsonObject
     */
    @Throws(JSONException::class)
    private fun setResultOfUserInfo(p_jsonObject: JSONObject) {

        // 프로그레스 다이얼로그 dismiss
        val jsonKey_errorCode = "errCode"
        val jsonKey_data = "data"
        val jsonKey_s_cttr_isrt_smns_yn = "s_cttr_isrt_smns_yn" // 피보험자,수익자 일치 여부 "Y" or "N"
        var tmp_errorCode = ""
        if (p_jsonObject.has(jsonKey_errorCode)) {
            tmp_errorCode = p_jsonObject.getString(jsonKey_errorCode)
            if ((tmp_errorCode == "ERRIUII12M00001")) {        // 고객정보가 없는 경우
                mActivity!!.CF_OnError(resources.getString(R.string.dlg_error_erriuii12m00001))
            } else if (p_jsonObject.has(jsonKey_data)) {
                val tmp_jsonData = p_jsonObject.getJSONObject(jsonKey_data)

                // ---------------------------------------------------------------------------------
                // 고객의 보험 계약 정보 확인 : 수익자이면서 피보험자인 경우만 청구 가능
                // ---------------------------------------------------------------------------------
                if (tmp_jsonData.has(jsonKey_s_cttr_isrt_smns_yn)) {
                    val tmp_strAvailable = tmp_jsonData.getString(jsonKey_s_cttr_isrt_smns_yn)
                    if ((tmp_strAvailable.toLowerCase(Locale.getDefault()) == "e")) {
                        // "E" : 전문 에러
                        mActivity!!.CF_OnError(resources.getString(R.string.dlg_error_erriuii12m00003))
                    } else if ((tmp_strAvailable.toLowerCase(Locale.getDefault()) == "s")) {
                        // "S" : 보장성보험 미보유 : 상품코드(4,5,6)으로 시작
                        mActivity!!.CF_OnError("청구 가능한 보장성 보험이 없습니다.\n가입하신 보험내역을 확인해 주십시오.")
                    } else if ((tmp_strAvailable.toLowerCase(Locale.getDefault()) == "y")) {
                        // 사용자 데이터 세팅
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
                    } else {
                        // -------------------------------------------------------------------------
                        //  피보험자 수익자 확인 필요 Dialog 팝업
                        // -------------------------------------------------------------------------
                        val tmp_dlg = CustomDialog((activity)!!)
                        tmp_dlg.show()
                        tmp_dlg.CF_setBackKeyMode(CustomDialog.BackMode.NOTUSE)
                        tmp_dlg.CF_setTextContent(resources.getString(R.string.dlg_not_available_req))
                        tmp_dlg.CF_setSingleButtonText(resources.getString(R.string.btn_ok))
                        tmp_dlg.setOnDismissListener(object : DialogInterface.OnDismissListener {
                            override fun onDismiss(dialog: DialogInterface) {
                                if ((dialog as CustomDialog).CF_getCanceled() == false) {
                                    mActivity!!.setResult(Activity.RESULT_CANCELED)
                                    mActivity!!.finish()
                                }
                            }
                        })
                    }
                } else {
                    // 운영서버 미적용으로 임시 팝업
                    mActivity!!.CF_OnError("고객님의 보험계약 정보를 확인할 수 없습니다. 잠시 후에 다시 시도해주세요.")
                }
            } else {
                mActivity!!.CF_OnError(resources.getString(R.string.dlg_error_server_2))
            }
        } else {
            mActivity!!.CF_OnError(resources.getString(R.string.dlg_error_server_1))
        }
    }
}