package com.epost.insu.fragment

import android.annotation.SuppressLint
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.os.Message
import android.text.Spannable
import android.text.SpannableString
import android.text.TextUtils
import android.text.style.ForegroundColorSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import com.epost.insu.EnvConfig
import com.epost.insu.R
import com.epost.insu.activity.IUII60M00
import com.epost.insu.common.FormatUtils
import com.epost.insu.common.LogPrinter
import com.epost.insu.common.WebBrowserHelper
import com.epost.insu.data.Data_IUII10M00
import com.epost.insu.data.Data_IUII10M09_F
import com.epost.insu.dialog.CustomDialog
import com.epost.insu.network.HttpConnections
import com.epost.insu.network.WeakReferenceHandler
import com.epost.insu.network.WeakReferenceHandler.ObjectHandlerMessage
import org.json.JSONException
import org.json.JSONObject

/**
 * 보험금청구 > 본인청구 > 8단계. 보험금청구신청완료
 * @since     :
 * @version   : 1.4
 * @author    : LSH
 * <pre>
 * - 화면 진입 시 보험금청구에 필요한 데이터를 서버에 전송한다.<br></br>
 * - 보험금청구 실패시 보험금청구 오류 화면 show<br></br>
 * - 보험금청구 성공시 청구내용 show
 * ======================================================================
 * 0.0.0    LSH_20170907    최초 등록
 * 0.0.0    NJM_20190529    청구이벤트 관련 웹뷰 or 브라우저 show
 * 0.0.0    NJM_20191224    보험금청구 완료단계에 사고일자, 진단명 추가
 * 0.0.0    NJM_20200122    공통 인증유형/청구유형 추가에 따른 로직수정
 * 1.5.2    NJM_20210322    [subUrl 공통파일로 변경]
 * =======================================================================
 * copyright : (주)프로니스정보기술 | http://www.phronis.co.kr
 * </pre>
 */
class IUII10M09_F : IUII10M00_FD(), ObjectHandlerMessage {
    private var linResultOk: LinearLayout? = null
    private var linResultFail: LinearLayout? = null
    private var textCompleteMsg: TextView? = null // 청구완료 메시지

    private var textNum: TextView? = null
    private var textReqDate: TextView? = null
    private var textCenter: TextView? = null
    private var textPersonInCharge: TextView? = null
    private var textCenterPhone: TextView? = null
    private var textReqName: TextView? = null
    private var textReqCategory: TextView? = null
    private var textReqReason: TextView? = null
    private var textReqType: TextView? = null
    private var textDueDate: TextView? = null
    private var textBankInfo: TextView? = null
    private var textReqReasonDate: TextView? = null
    private var textReqPlace: TextView? = null
    private var textReqCntt: TextView? = null // 사고일자, 사고장소, 사고내용 //2019-12-17 추가
    private var textReqDignNm: TextView? = null // 진단명    //2019-12-17 추가
    private var tmp_viewBtnGuide: View? = null
    private var mActivityData: Data_IUII10M00? = Data_IUII10M00()
    private var data: Data_IUII10M09_F? = null
    private var currentPageIndex: Int = 0

    // 이벤트웹뷰
    private var handler: WeakReferenceHandler? = null

    // 이벤트 유무 HTTP
    private val HANDLERJOB_GET_INFO: Int = 0 // HTTP 요청 성공 코드
    private val HANDLERJOB_ERROR_GET_INFO: Int = 1 // HTTP 요청 에러 코드

    /**
     * @param p_message Message
     */
    override fun handleMessage(p_message: Message) {
        LogPrinter.CF_debug("!----------------------------------------------------------")
        LogPrinter.CF_debug("!-- IUII10M09_F.handleMessage()")
        LogPrinter.CF_debug("!----------------------------------------------------------")
        mActivity!!.CF_dismissProgressDialog()
        when (p_message.what) {
            HANDLERJOB_GET_INFO -> {
                LogPrinter.CF_debug("!---- 이벤트 요청 성공")
                try {
                    setResultOfEventInfo(JSONObject(p_message.obj as String?))
                } catch (e: JSONException) {
                    LogPrinter.CF_line()
                    LogPrinter.CF_debug(resources.getString(R.string.log_json_exception))
                }
            }
            HANDLERJOB_ERROR_GET_INFO -> LogPrinter.CF_debug("!---- 이벤트 요청 실패")
        }
    }

    override fun onSaveInstanceState(p_bundle: Bundle) {
        super.onSaveInstanceState(p_bundle)

        // -- 데이터 백업
        saveData()
        p_bundle.putParcelable("data", data)
        p_bundle.putInt("currentPageIndex", currentPageIndex)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // -- 초기화
        setInit()
    }

    @SuppressLint("InflateParams")
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.iuii10m09_f, null)
    }

    override fun onActivityCreated(p_bundle: Bundle?) {
        super.onActivityCreated(p_bundle)
        if (p_bundle != null) {
            if (p_bundle.containsKey("data")) {
                data = p_bundle.getParcelable("data")
            }
            if (p_bundle.containsKey("currentPageIndex")) {
                currentPageIndex = p_bundle.getInt("currentPageIndex")
            }
        }

        // -- UI 생성 및 세팅 함수
        setUIControl()

        // -- 데이터 복구
        restoreData()
    }

    /**
     * 초기 세팅 함수
     */
    private fun setInit() {
        currentPageIndex = -1
        data = Data_IUII10M09_F()
        handler = WeakReferenceHandler(this)
        LogPrinter.CF_debug("!---- (8단계) 인증구분 : " + mActivity!!.CF_getAuthDvsn())
    }

    /**
     * UI 생성 및 세팅 함수
     */
    private fun setUIControl() {
        linResultOk = view!!.findViewById(R.id.linResultOk)
        linResultFail = view!!.findViewById(R.id.linResultFail)
        val tmp_linNumber: LinearLayout = view!!.findViewById(R.id.labelTextNumber) // 접수번호
        val tmp_linDate: LinearLayout = view!!.findViewById(R.id.labelTextDate) // 접수일자
        val tmp_linCenter: LinearLayout = view!!.findViewById(R.id.labelTextCenter) // 접수센터
        val tmp_linCenterPhone: LinearLayout = view!!.findViewById(R.id.labelTextCenterPhone) // 접수센터 전화번호
        val tmp_linPersonInCharge: LinearLayout = view!!.findViewById(R.id.labelTextPersonInCharge) // 접수자
        val tmp_linReqName: LinearLayout = view!!.findViewById(R.id.labelTextReqName) // 청구자
        val tmp_linReqCategory: LinearLayout = view!!.findViewById(R.id.labelTextReqCategory) // 청구유형
        val tmp_linReqReason: LinearLayout = view!!.findViewById(R.id.labelTextReqReason) // 사고발생원인
        val tmp_linReqType: LinearLayout = view!!.findViewById(R.id.labelTextReqType) // 청구사유
        val tmp_linDueDate: LinearLayout = view!!.findViewById(R.id.labelTextDueDate) // 보험처리예정일
        val tmp_linBankInfo: LinearLayout = view!!.findViewById(R.id.labelTextBankInfo) // 송금요청계좌정보
        val tmp_linReasonDate: LinearLayout = view!!.findViewById(R.id.labelTextReqReasonDate) // 사고일자             // 2019-12-24 추가
        val tmp_linReqPlace: LinearLayout = view!!.findViewById(R.id.labelTextReqPlace) // 사고장소             // 2019-12-17 추가
        val tmp_linReqCntt: LinearLayout = view!!.findViewById(R.id.labelTextReqCntt) // 사고내용             // 2019-12-17 추가
        val tmp_linDignNm: LinearLayout = view!!.findViewById(R.id.labelTextReqDignNm) // 진단명               // 2019-12-24 추가

        // -- 라벨 세팅
        val tmp_labelNumber: TextView = tmp_linNumber.findViewById(R.id.label)
        val tmp_labelDate: TextView = tmp_linDate.findViewById(R.id.label)
        val tmp_labelCenter: TextView = tmp_linCenter.findViewById(R.id.label)
        val tmp_labelCenterPhone: TextView = tmp_linCenterPhone.findViewById(R.id.label)
        val tmp_labelPersonInCharge: TextView = tmp_linPersonInCharge.findViewById(R.id.label)
        val tmp_labelReqName: TextView = tmp_linReqName.findViewById(R.id.label)
        val tmp_labelReqCategory: TextView = tmp_linReqCategory.findViewById(R.id.label)
        val tmp_labelReqReason: TextView = tmp_linReqReason.findViewById(R.id.label)
        val tmp_labelReqType: TextView = tmp_linReqType.findViewById(R.id.label)
        val tmp_labelDueDate: TextView = tmp_linDueDate.findViewById(R.id.label)
        val tmp_labelBankInfo: TextView = tmp_linBankInfo.findViewById(R.id.label)
        val tmp_labelReasonDate: TextView = tmp_linReasonDate.findViewById(R.id.label) // 사고일자     //2019-12-24 추가
        val tmp_labelReqPlace: TextView = tmp_linReqPlace.findViewById(R.id.label) // 사고장소     //2019-12-24 추가
        val tmp_labelReqCntt: TextView = tmp_linReqCntt.findViewById(R.id.label) // 사고내용     //2019-12-24 추가
        val tmp_labelDignNm: TextView = tmp_linDignNm.findViewById(R.id.label) // 진단명       //2019-12-24 추가
        tmp_labelNumber.text = resources.getString(R.string.label_request_number)
        tmp_labelDate.text = resources.getString(R.string.label_request_date)
        tmp_labelCenter.text = resources.getString(R.string.label_request_center)
        tmp_labelCenterPhone.text = resources.getString(R.string.label_request_center_phone)
        tmp_labelPersonInCharge.text = resources.getString(R.string.label_person_in_charge_s)
        tmp_labelReqName.text = resources.getString(R.string.label_request_name)
        tmp_labelReqCategory.text = resources.getString(R.string.label_req_category)
        tmp_labelReqReason.text = resources.getString(R.string.label_req_reason)
        tmp_labelReqType.text = resources.getString(R.string.label_req_type)
        tmp_labelDueDate.text = resources.getString(R.string.label_pay_due_date)
        tmp_labelBankInfo.text = resources.getString(R.string.label_req_bank_account)
        tmp_labelReasonDate.text = resources.getString(R.string.label_request_reason_date) //2019-12-24 추가
        tmp_labelReqPlace.text = resources.getString(R.string.label_request_place) //2019-12-24 추가
        tmp_labelReqCntt.text = resources.getString(R.string.label_request_cntt) //2019-12-24 추가
        tmp_labelDignNm.text = resources.getString(R.string.label_request_dign_Nm) //2019-12-24 추가

        // -- TextView member 세팅
        textCompleteMsg = view!!.findViewById(R.id.textReqComplete) // 청구완료 메시지
        textNum = tmp_linNumber.findViewById(R.id.text) // 접수번호
        textReqDate = tmp_linDate.findViewById(R.id.text) // 접수일자
        textCenter = tmp_linCenter.findViewById(R.id.text) // 접수센터
        textCenterPhone = tmp_linCenterPhone.findViewById(R.id.text) // 접수센터 전화번호
        textPersonInCharge = tmp_linPersonInCharge.findViewById(R.id.text) // 접수자
        textReqName = tmp_linReqName.findViewById(R.id.text) // 청구자
        textReqCategory = tmp_linReqCategory.findViewById(R.id.text) // 청구유형
        textReqReason = tmp_linReqReason.findViewById(R.id.text) // 사고발생원인
        textReqType = tmp_linReqType.findViewById(R.id.text) // 청구사유
        textDueDate = tmp_linDueDate.findViewById(R.id.text) // 보험처리예정일
        textBankInfo = tmp_linBankInfo.findViewById(R.id.text) // 송금요청계좌정보
        textReqReasonDate = tmp_linReasonDate.findViewById(R.id.text) // 사고일자            // 2019-12-24 추가
        textReqPlace = tmp_linReqPlace.findViewById(R.id.text) // 사고장소            // 2019-12-24 추가
        textReqCntt = tmp_linReqCntt.findViewById(R.id.text) // 사고내용            // 2019-12-24 추가
        textReqDignNm = tmp_linDignNm.findViewById(R.id.text) // 진단명              // 2019-12-24 추가
        tmp_viewBtnGuide = view!!.findViewById(R.id.btnGuideProcess)
        if (tmp_viewBtnGuide != null) {
            val tmp_btnGuide: Button? = tmp_viewBtnGuide!!.findViewById(R.id.btnFill)
            if (tmp_btnGuide != null) {
                tmp_btnGuide.text = resources.getString(R.string.btn_guide_req)
                tmp_btnGuide.setOnClickListener(object : View.OnClickListener {
                    override fun onClick(v: View) {
                        startIUII60M00Activity()
                    }
                })
            }
        }
        val tmp_viewBtnRetry: View? = view!!.findViewById(R.id.btnRetry)
        if (tmp_viewBtnRetry != null) {
            val tmp_btnRetry: Button? = tmp_viewBtnRetry.findViewById(R.id.btnFill)
            if (tmp_btnRetry != null) {
                tmp_btnRetry.text = resources.getString(R.string.btn_retry)
                tmp_btnRetry.setOnClickListener(object : View.OnClickListener {
                    override fun onClick(v: View) {
                        CF_showVisibliteyPage(-1)
                        mActivity!!.CF_requestSubmitPay()
                    }
                })
            }
        }
        CF_showVisibliteyPage(currentPageIndex)
    }

    /**
     * 데이터 백업
     */
    private fun saveData() {
        data!!.CF_setNumber(textNum!!.text.toString().trim())
        data!!.CF_setDate(textReqDate!!.text.toString().trim())
        data!!.CF_setCenter(textCenter!!.text.toString().trim())
        data!!.CF_setCenterPhone(textCenterPhone!!.text.toString().trim())
        data!!.CF_setPersonInCharge(textPersonInCharge!!.text.toString().trim())
        data!!.CF_setReqCategory(textReqCategory!!.text.toString().trim())
        data!!.CF_setReqReason(textReqReason!!.text.toString().trim())
        data!!.CF_setReqType(textReqType!!.text.toString().trim())
        data!!.CF_setDueDate(textDueDate!!.text.toString().trim())
        data!!.CF_setBankInfo(textBankInfo!!.text.toString().trim())
        data!!.CF_setReqName(textReqName!!.text.toString().trim())
        data!!.CF_setReqReasonDate(textReqReasonDate!!.text.toString().trim()) // 사고일자            //2019-12-24 추가
        data!!.CF_setReqPlace(textReqPlace!!.text.toString().trim()) // 사고장소            //2019-12-24 추가
        data!!.CF_setReqCntt(textReqCntt!!.text.toString().trim()) // 사고내용            //2019-12-24 추가
        data!!.CF_setReqDignNm(textReqDignNm!!.text.toString().trim()) // 진단명              //2019-12-24 추가
    }

    /**
     * 데이터 복구
     */
    private fun restoreData() {

        // -- 상단 청구완료 메시지
        val tmp_userName: String = data!!.CF_getReqName()
        val tmp_fullText: String = tmp_userName + " " + resources.getString(R.string.label_req_complete)
        val tmp_spannable: Spannable = SpannableString(tmp_fullText)
        tmp_spannable.setSpan(ForegroundColorSpan(Color.parseColor("#ff7c29c7")), 0, tmp_userName.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        textCompleteMsg!!.text = tmp_spannable
        textNum!!.text = data!!.CF_getNumber()
        textReqDate!!.text = data!!.CF_getDate()
        textCenter!!.text = data!!.CF_getCenter()
        textCenterPhone!!.text = data!!.CF_getCenterPhone()
        textPersonInCharge!!.text = data!!.CF_getPersonInCharge()
        textReqCategory!!.text = data!!.CF_getReqCategory()
        textReqReason!!.text = data!!.CF_getReqReason()
        textReqType!!.text = data!!.CF_getReqType()
        textDueDate!!.text = data!!.CF_getDueDate()
        textBankInfo!!.text = data!!.CF_getBankInfo()
        textReqName!!.text = data!!.CF_getReqName()
        textReqReasonDate!!.text = data!!.CF_getReqReasonDate() // 사고일자            //2019-12-24 추가
        textReqPlace!!.text = data!!.CF_getReqPlace() // 사고장소            //2019-12-24 추가
        textReqCntt!!.text = data!!.CF_getReqCntt() // 사고내용            //2019-12-24 추가
        textReqDignNm!!.text = data!!.CF_getReqDignNm() // 진단명              //2019-12-24 추가
    }
    // #############################################################################################
    //  Activity 함수
    // #############################################################################################
    /**
     * (IUII60M00) 보험금청구절차 Activity 호출 함수
     */
    private fun startIUII60M00Activity() {
        val tmp_intent: Intent = Intent(activity, IUII60M00::class.java)
        tmp_intent.putExtra("flagUseCloseBtn", true)
        tmp_intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
        activity!!.startActivity(tmp_intent)
        mActivity!!.finish()
    }
    // #############################################################################################
    //   함수
    // #############################################################################################
    /**
     * 해당 페이지 Show 함수
     * @param p_pageIndex int
     */
    fun CF_showVisibliteyPage(p_pageIndex: Int) {
        currentPageIndex = p_pageIndex

        // 지급청구 성공시
        if (p_pageIndex == 0) {
            linResultOk!!.visibility = View.VISIBLE
            linResultFail!!.visibility = View.GONE

            // 이벤트가 있을 경우 이벤트 웹뷰 or 브라우저 show
            CF_requestEventChk()

            // 지급청구 실패시
        } else if (p_pageIndex == 1) {
            linResultOk!!.visibility = View.GONE
            linResultFail!!.visibility = View.VISIBLE

            // 그외..
        } else {
            linResultOk!!.visibility = View.GONE
            linResultFail!!.visibility = View.GONE
        }
    }

    /**
     * 보험금청구 접수 요청 결과 set 함수
     *
     * @param p_requ_recp_id        접수 아이디
     * @param p_recp_cent_nm        접수 센터명
     * @param p_recp_cent_tlno      접수 센터 전화번호
     * @param p_recp_cent_chps_nm   담당자 이름
     * @param p_requ_proc_schd_date 처리 예정일
     */
    fun CF_setResultOk(p_requ_recp_id: String?,
                       p_recp_cent_nm: String?,
                       p_recp_cent_tlno: String?,
                       p_recp_cent_chps_nm: String?,
                       p_requ_proc_schd_date: String
    ) {
        var p_requ_proc_schd_date: String = p_requ_proc_schd_date
        LogPrinter.CF_debug("!----------------------------------------------------------")
        LogPrinter.CF_debug("!-- IUII10M09_F.CF_setResultOk()")
        LogPrinter.CF_debug("!----------------------------------------------------------")

        // Activity Data
        mActivityData = mActivity!!.CF_getData()
        val tmp_userName: String = mActivityData!!.CF_getLoginUserName() // 청구자명

        // -----------------------------------------------------------------------------------------
        // -- 청구(전문수신) Data
        // -----------------------------------------------------------------------------------------
        // -- 상단 청구완료 메시지
        val tmp_fullText: String = tmp_userName + " " + resources.getString(R.string.label_req_complete)
        val tmp_spannable: Spannable = SpannableString(tmp_fullText)
        tmp_spannable.setSpan(ForegroundColorSpan(Color.parseColor("#ff7c29c7")), 0, tmp_userName.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        textCompleteMsg!!.text = tmp_spannable
        textNum!!.text = p_requ_recp_id
        textCenter!!.text = p_recp_cent_nm
        textCenterPhone!!.text = p_recp_cent_tlno
        textPersonInCharge!!.text = p_recp_cent_chps_nm

        // -- 보험처리 예정일 : 날짜형식 변환 (예 20191010 -> 2019.10.10)
        if (p_requ_proc_schd_date.trim().length == 8) {
            p_requ_proc_schd_date = FormatUtils.CF_convertDate(p_requ_proc_schd_date)
        }
        textDueDate!!.text = p_requ_proc_schd_date
        textReqDate!!.text = FormatUtils.CF_getTodayStr() // 접수일자 : 현재일
        textReqName!!.text = tmp_userName // 청구자명
        textReqCategory!!.text = mActivityData!!.CF_getStr_s_insu_requ_type_code() // 청구유형
        textReqReason!!.text = mActivityData!!.CF_getStr_s_requ_gent_caus_code() // 발생원인
        textReqType!!.text = mActivityData!!.CF_getStr_s_insu_requ_resn_code() // 청구사유
        textBankInfo!!.text = "은행명 : " + mActivityData!!.CF_getS_fnis_nm() + "\r\n" + "계좌번호 : " + mActivityData!!.CF_getDecode_s_acno() + "\r\n예금주 : " + tmp_userName

        // -- 사고일자 //2019-12-24 추가
        var tmp_reqReasonDate: String = mActivityData!!.CF_getS_acdt_date()
        var tmp_reqReasonTime: String = mActivityData!!.CF_getS_acdt_time()

        // -1- (정상)
        if (tmp_reqReasonDate.trim().length == 8) {
            //날짜형식 변환 (예 20191010 -> 2019.10.10 18:00)
            tmp_reqReasonTime = tmp_reqReasonTime.substring(0, 2) + ":" + tmp_reqReasonTime.substring(2, 4)
            tmp_reqReasonDate = FormatUtils.CF_convertDate(tmp_reqReasonDate) + " " + tmp_reqReasonTime
        } else if (TextUtils.isEmpty(tmp_reqReasonDate)) {
            tmp_reqReasonDate = "-"
        }
        textReqReasonDate!!.text = tmp_reqReasonDate

        // -- 사고장소
        textReqPlace!!.text = if (!TextUtils.isEmpty(mActivityData!!.CF_getS_acdt_pace())) mActivityData!!.CF_getS_acdt_pace() else "-" // 사고장소

        // -- 사고내용
        textReqCntt!!.text = if (!TextUtils.isEmpty(mActivityData!!.CF_getS_acdt_cntt())) mActivityData!!.CF_getS_acdt_cntt() else "-" // 사고내용

        // -- 진단명  //2019-12-24 추가
        textReqDignNm!!.text = if (!TextUtils.isEmpty(mActivityData!!.CF_getS_dign_nm())) mActivityData!!.CF_getS_dign_nm() else "-"
    }

    /**
     * @since 2019-05-29 청구 이벤트 팝업 & 웹뷰 호출
     */
    private fun CF_eventShow(url: String) {
        // 이벤트 다이얼로그 show
        showCustomDialog(
            resources.getString(R.string.dlg_benefit_choice_event), resources.getString(R.string.btn_no), resources.getString(R.string.btn_yes), (tmp_viewBtnGuide)!!, object : DialogInterface.OnDismissListener {
                override fun onDismiss(dialog: DialogInterface) {
                    if (!(dialog as CustomDialog).CF_getCanceled()) {
                        // 토스트 & 웹뷰(or 브라우저) 실행
                        Toast.makeText(mActivity, "이벤트페이지로 이동합니다.", Toast.LENGTH_SHORT).show()
                        WebBrowserHelper.callWebBrowser(context, url)
                        //WebBrowserHelper.startWebViewActivity(getContext(),0,url,"청구이벤트");        // 웹뷰로 띄울때 사용
                    }
                }
            }
        )
    }
    // #############################################################################################
    //  Http 관련
    // #############################################################################################
    /**
     * 지급청구 이벤트 유무 체크
     */
    fun CF_requestEventChk() {
        LogPrinter.CF_debug("!----------------------------------------------------------")
        LogPrinter.CF_debug("!-- IUII10M09_F.CF_requestEventChk()")
        LogPrinter.CF_debug("!----------------------------------------------------------")

        // this.CF_showProgressDialog();    //켜지마
        //((Activity_Default)getActivity()).CF_showProgressDialog();
        val tmp_builder: Uri.Builder = Uri.Builder()
        HttpConnections.sendPostData(
            EnvConfig.host_url + EnvConfig.URL_EVENT_INQ,
            tmp_builder.build().encodedQuery,
            handler,
            HANDLERJOB_GET_INFO,
            HANDLERJOB_ERROR_GET_INFO)
    }

    /**
     * 이벤트요청 결과 처리 함수
     * @param p_jsonObject  JSONObject
     */
    @Throws(JSONException::class)
    private fun setResultOfEventInfo(p_jsonObject: JSONObject) {
        LogPrinter.CF_debug("!----------------------------------------------------------")
        LogPrinter.CF_debug("!-- IUII10M09_F.setResultOfEventInfo()")
        LogPrinter.CF_debug("!----------------------------------------------------------")
        val jsonKey_data: String = "data"
        val jsonKey_cfgEvent: String = "cfg_event" // 다음페이지 존재 여부
        val jsonKey_cfgEventUrl: String = "cfg_event_url" // 이벤트URL


        // -1- (data있음) : 있을 때
        if (p_jsonObject.has(jsonKey_data)) {
            val tmp_jsonData: JSONObject = p_jsonObject.getJSONObject(jsonKey_data)
            // -2- (리스트 있음) : arrPerson 배열이 있을 경우
            if (tmp_jsonData.has(jsonKey_cfgEvent)) {

                // -3- Event 유무 체크 : 이벤트 있음
                val tmp_cfgEvent: String = tmp_jsonData.getString(jsonKey_cfgEvent)
                if (("Y" == tmp_cfgEvent)) {
                    LogPrinter.CF_debug("!---- 이벤트 있음")

                    // -4- Event URL GET
                    if (tmp_jsonData.has(jsonKey_cfgEventUrl)) {
                        val tmp_cfgEventUrl: String = tmp_jsonData.getString(jsonKey_cfgEventUrl)
                        LogPrinter.CF_debug("!---- Event URL GET : " + tmp_cfgEventUrl)

                        // -- 이벤트 실행 알림 & show
                        CF_eventShow(tmp_cfgEventUrl)
                    }

                    // -3- Event 유무 체크 : 이벤트 없음
                } else {
                    LogPrinter.CF_debug("!---- 이벤트 없음")
                }

                // -2- (리스트 없음) : arrPerson 배열이 없을 경우
            } else {
                LogPrinter.CF_debug("!---- arrPerson 배열 없음")
                // showDlgOfHttpError(getResources().getString(R.string.dlg_error_server_2), true);
            }

            // -1- data : 없을 때
        } else {
            LogPrinter.CF_debug("!---- data 없음")
            //showDlgOfHttpError(getResources().getString(R.string.dlg_error_server_1), true);
        }
    }
}