package com.epost.insu.fragment

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Message
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import com.epost.insu.EnvConfig
import com.epost.insu.R
import com.epost.insu.SharedPreferencesFunc
import com.epost.insu.activity.IUII10M00_S
import com.epost.insu.activity.IUII50M00_P
import com.epost.insu.common.CommonFunction
import com.epost.insu.common.FormatUtils
import com.epost.insu.common.LogPrinter
import com.epost.insu.common.WebFileDownloadHelper
import com.epost.insu.dialog.CustomDialog
import com.epost.insu.network.HttpConnections
import com.epost.insu.network.WeakReferenceHandler
import com.epost.insu.network.WeakReferenceHandler.ObjectHandlerMessage
import org.json.JSONException
import org.json.JSONObject
import java.util.*

/**
 * 보험금청구 > 보험금청구 > 보험금청구조회 상세
 * @since     :
 * @version   : 1.9
 * @author    : LSH
 * <pre>
 * // TODO : 화면 새로 그릴때 지금청구 버튼이 복구 안되는 문제 있음 onSaveInstanceState 담지 않아서 발생함 -> 추후 수정 필요함
 * ======================================================================
 * LSH_20171116    최초 등록
 * NJM_20191224    지급청구내역 상세조회에 사고일자, 진단명 추가
 * NJM_20200106    청구서류 항목 추가, 청구취소 버튼 추가
 * NJM_20200304    보험금청구조회에서 이미지 조회 및 접수취소 기능 추가
 * NJM_20200318    지급청구조회에서 접수취소 후 다른상세화면에서 기존 (동적생성된)버튼리스트 나오는문제 수정
 * NJM_20200408    pdf 다운로드 리시버 개선 - 종료시 리시버 해제 fileDownloadUnRegisterReceiver()
 * NJM_20200413    접수 상태 외 청구취소 버튼 안보이게 변경(심사완료후 취소되는 문제 해결)
 * NJM_20200616    보안서류요청이 y일때, 상태값 상관 없이 항상 보완요청 버튼 보임 -> "접수" 상태일때만 보완 요청 가능하도록 수정
 * : 접수상태값 추가 (11,12)
 * NJM_20200721    서류보완버튼 표기 단계 수정 접수단계(0) -> 0~5단계
 * 1.5.2    NJM_20210322    [subUrl 공통파일로 변경]
 * =======================================================================
 * copyright : (주)프로니스정보기술 | http://www.phronis.co.kr
</pre> *
 */
class IUII51M00_F : Fragment_Default(), ObjectHandlerMessage {
    private val HANDLERJOB_DETAIL = 0 // 보험금청구조회 상세내용 요청 성공 what
    private val HANDLERJOB_ERROR_DETAIL = 1 // 보험금청구조회 상세내용 요청 실패 what
    private val HANDLERJOB_REQ_CANCEL = 2 // 보험금청구 취소 성공 what
    private val HANDLERJOB_REQ_ERROR_CANCEL = 3 // 보험금청구 취소 실패 what
    private var mActivity: IUII50M00_P? = null
    private var handler: WeakReferenceHandler? = null
    private var btnModDoc: Button? = null // 청구서류 보완신청 버튼
    private var btnCancelReq: Button? = null // 청구취소 버튼
    private var linContents: LinearLayout? = null // 0. 전체 뷰
    private var mLinReqDocRoot: LinearLayout? = null // 2. 청구서류 뷰

    // -- 접수신청정보 Text
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
    private var textReqCntt: TextView? = null // 사고일자, 사고장소, 사고내용
    private var textReqDignNm: TextView? = null // 진단명

    // -- 심사진행결과 Text
    private var textState: TextView? = null
    private var textDoDate: TextView? = null
    private var textFailReason: TextView? = null
    private var textMoney: TextView? = null
    private var textLabelFailReason: TextView? = null
    private var linFailReason: LinearLayout? = null
    private var linMoney: LinearLayout? = null // 반려 사유 , 보험금지급액 viewGroup

    override fun handleMessage(p_message: Message) {
        if (mActivity != null && !mActivity!!.isDestroyed) {
            mActivity!!.CF_dismissProgressDialog()
            when (p_message.what) {
                HANDLERJOB_DETAIL -> try {
                    setResultOfDetail(JSONObject(p_message.obj as String))
                } catch (e: JSONException) {
                    LogPrinter.CF_line()
                    LogPrinter.CF_debug(resources.getString(R.string.log_json_exception))
                }
                HANDLERJOB_ERROR_DETAIL -> {
                    mActivity!!.CF_setCurrentPage(0)
                    CommonFunction.CF_showCustomAlertDilaog(activity, p_message.obj as String, resources.getString(R.string.btn_ok))
                }
                HANDLERJOB_REQ_CANCEL -> try {
                    setResultOfCancel(JSONObject(p_message.obj as String))
                } catch (e: JSONException) {
                    LogPrinter.CF_line()
                    LogPrinter.CF_debug(resources.getString(R.string.log_json_exception))
                }
                HANDLERJOB_REQ_ERROR_CANCEL -> CommonFunction.CF_showCustomAlertDilaog(activity, p_message.obj as String, resources.getString(R.string.btn_ok))
                else -> {
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // -- 초기 세팅
        setInit()
    }

    @SuppressLint("InflateParams")
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.iuii51m00_f, null)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        // -- UI 생성 및 세팅
        setUIControl()

        // -- 데이터 복구
        restoreData(savedInstanceState)
    }

    override fun onResume() {
        super.onResume()
    }

    override fun onStop() {
        super.onStop()
    }

    override fun onDestroyView() {
        super.onDestroyView()

        // -- 다운로드 리시버 해제
        WebFileDownloadHelper.fileDownloadUnRegisterReceiver(this.context)
    }

    /**
     *
     * @param outState      Bundle
     * @since 2019-12-17    사고일자,사고장소,사고내용,진단명 추가
     */
    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putBoolean("flagVisibleContents", linContents!!.visibility == View.VISIBLE)
        outState.putBoolean("flagVisibleMoney", linMoney!!.visibility == View.VISIBLE)
        outState.putBoolean("flagVisibleFail", linFailReason!!.visibility == View.VISIBLE)
        outState.putString("textNum", textNum!!.text.toString())
        outState.putString("textReqDate", textReqDate!!.text.toString())
        outState.putString("textCenter", textCenter!!.text.toString())
        outState.putString("textPersonInCharge", textPersonInCharge!!.text.toString())
        outState.putString("textCenterPhone", textCenterPhone!!.text.toString())
        outState.putString("textReqName", textReqName!!.text.toString())
        outState.putString("textReqCategory", textReqCategory!!.text.toString())
        outState.putString("textReqReason", textReqReason!!.text.toString())
        outState.putString("textReqType", textReqType!!.text.toString())
        outState.putString("textDueDate", textDueDate!!.text.toString())
        outState.putString("textBankInfo", textBankInfo!!.text.toString())
        outState.putString("textState", textState!!.text.toString())
        outState.putString("textDoDate", textDoDate!!.text.toString())
        outState.putString("textFailReason", textFailReason!!.text.toString())
        outState.putString("textLabelFailReason", textLabelFailReason!!.text.toString())
        outState.putString("textMoney", textMoney!!.text.toString())
        outState.putString("textReqReasonDate", textReqReasonDate!!.text.toString()) // 사고일자
        outState.putString("textReqPlace", textReqPlace!!.text.toString()) // 사고장소
        outState.putString("textReqCntt", textReqCntt!!.text.toString()) // 사고내용
        outState.putString("textReqDignNm", textReqDignNm!!.text.toString()) // 진단명
        outState.putBoolean("flagVisibleModBtn", btnModDoc!!.visibility == View.VISIBLE)
    }

    /**
     * onSave 에서 저장한 데이터 복구
     * @param p_bundle      Bundle
     * @since  2019-12-17    사고일자,사고장소,사고내용,진단명 추가
     */
    private fun restoreData(p_bundle: Bundle?) {
        if (p_bundle != null) {
            if (p_bundle.containsKey("flagVisibleContents")) {
                val tmp_flagVisibleContents = p_bundle.getBoolean("flagVisibleContents")
                if (tmp_flagVisibleContents) {
                    linContents!!.visibility = View.VISIBLE
                } else {
                    linContents!!.visibility = View.GONE
                }
            }
            if (p_bundle.containsKey("flagVisibleMoney")) {
                val tmp_flagVisibleMoney = p_bundle.getBoolean("flagVisibleMoney")
                if (tmp_flagVisibleMoney) {
                    linMoney!!.visibility = View.VISIBLE
                } else {
                    linMoney!!.visibility = View.GONE
                }
            }
            if (p_bundle.containsKey("flagVisibleFail")) {
                val tmp_flagVisibleFail = p_bundle.getBoolean("flagVisibleFail")
                if (tmp_flagVisibleFail) {
                    linFailReason!!.visibility = View.VISIBLE
                } else {
                    linFailReason!!.visibility = View.GONE
                }
            }
            if (p_bundle.containsKey("textNum")) {
                textNum!!.text = p_bundle.getString("textNum")
            }
            if (p_bundle.containsKey("textReqDate")) {
                textReqDate!!.text = p_bundle.getString("textReqDate")
            }
            if (p_bundle.containsKey("textCenter")) {
                textCenter!!.text = p_bundle.getString("textCenter")
            }
            if (p_bundle.containsKey("textPersonInCharge")) {
                textPersonInCharge!!.text = p_bundle.getString("textPersonInCharge")
            }
            if (p_bundle.containsKey("textCenterPhone")) {
                textCenterPhone!!.text = p_bundle.getString("textCenterPhone")
            }
            if (p_bundle.containsKey("textReqName")) {
                textReqName!!.text = p_bundle.getString("textReqName")
            }
            if (p_bundle.containsKey("textReqCategory")) {
                textReqCategory!!.text = p_bundle.getString("textReqCategory")
            }
            if (p_bundle.containsKey("textReqReason")) {
                textReqReason!!.text = p_bundle.getString("textReqReason")
            }
            if (p_bundle.containsKey("textReqType")) {
                textReqType!!.text = p_bundle.getString("textReqType")
            }
            if (p_bundle.containsKey("textDueDate")) {
                textDueDate!!.text = p_bundle.getString("textDueDate")
            }
            if (p_bundle.containsKey("textBankInfo")) {
                textBankInfo!!.text = p_bundle.getString("textBankInfo")
            }
            if (p_bundle.containsKey("textState")) {
                textState!!.text = p_bundle.getString("textState")
            }
            if (p_bundle.containsKey("textDoDate")) {
                textDoDate!!.text = p_bundle.getString("textDoDate")
            }
            if (p_bundle.containsKey("textFailReason")) {
                textFailReason!!.text = p_bundle.getString("textFailReason")
            }
            if (p_bundle.containsKey("textLabelFailReason")) {
                textLabelFailReason!!.text = p_bundle.getString("textLabelFailReason")
            }
            if (p_bundle.containsKey("textMoney")) {
                textMoney!!.text = p_bundle.getString("textMoney")
            }
            if (p_bundle.containsKey("textReqReasonDate")) {                                          // 사고일자
                textReqReasonDate!!.text = p_bundle.getString("textReqReasonDate")
            }
            if (p_bundle.containsKey("textReqPlace")) {                                               // 사고장소
                textReqPlace!!.text = p_bundle.getString("textReqPlace")
            }
            if (p_bundle.containsKey("textReqCntt")) {                                                // 사고내용
                textReqCntt!!.text = p_bundle.getString("textReqCntt")
            }
            if (p_bundle.containsKey("textReqDignNm")) {                                              // 진단명
                textReqDignNm!!.text = p_bundle.getString("textReqDignNm")
            }
            if (p_bundle.containsKey("flagVisibleModBtn")) {
                val tmp_flagVisible = p_bundle.getBoolean("flagVisibleModBtn")
                if (tmp_flagVisible) {
                    btnModDoc!!.visibility = View.VISIBLE
                } else {
                    btnModDoc!!.visibility = View.GONE
                }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        // --<> (보완처리완료) 청구서류 보완신청 완료
        if (requestCode == EnvConfig.REQUESTCODE_ACTIVITY_IUII10M00_S && resultCode == Activity.RESULT_OK) {

            // -- 서류보완 요청내용 레이아웃 Visiblity
            linMoney!!.visibility = View.VISIBLE // 보험금지금액
            linFailReason!!.visibility = View.GONE // 반려사유
            btnModDoc!!.visibility = View.GONE // 청구서류 보완신청 버튼

            // -- 구비서류 보완 여부 값 업데이트
            mActivity!!.CF_updateFlagNeedModValue(textNum!!.text.toString())
        }
    }

    override fun onDestroy() {
        super.onDestroy()
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mActivity = activity as IUII50M00_P?
    }

    /**
     * 초기 세팅 함수
     */
    private fun setInit() {
        handler = WeakReferenceHandler(this)
    }

    /**
     * UI 생성 및 세팅 함수
     * @since 2019-01-08    청구취소버튼 추가
     */
    private fun setUIControl() {
        linContents = Objects.requireNonNull(view)?.findViewById(R.id.linContents) // 전체뷰
        mLinReqDocRoot = view!!.findViewById(R.id.linReqDocRoot) // 청구서류뷰

        // -- 접수신청정보 UI 세팅
        setUIRequestInfo()

        // -- 심사진행결과 UI 세팅
        setUIResultInfo()

        // -- 청구취소 버튼
        btnCancelReq = view!!.findViewById(R.id.btnFillRed)
        btnCancelReq?.setText(resources.getString(R.string.btn_cancel_req))
        btnCancelReq?.setVisibility(View.GONE)
        btnCancelReq?.setOnClickListener(View.OnClickListener {
            mActivity!!.showCustomDialog(
                    resources.getString(R.string.dlg_req_cancel_choice), resources.getString(R.string.btn_no), resources.getString(R.string.btn_yes), btnCancelReq!!, DialogInterface.OnDismissListener { dialog ->
                if (!(dialog as CustomDialog).CF_getCanceled()) {
                    // -- 취소요청처리
                    requestCancel()
                }
            }
            )
        })

        // -- 청구서류 보완신청 버튼
        btnModDoc = view!!.findViewById(R.id.btnFill)
        btnModDoc?.setText(resources.getString(R.string.btn_mod_req_doc))
        btnModDoc?.setOnClickListener(View.OnClickListener { // -- 구비서류 보완 화면 이동
            startIUII10M00_S()
        })
    }

    /**
     * 접수신청정보 UI 세팅 함수
     * @since 2019-12-17    사고일자,사고장소,사고내용,진단명 추가
     */
    private fun setUIRequestInfo() {
        val tmp_linNum = Objects.requireNonNull(view)?.findViewById<LinearLayout>(R.id.labelTextNumber)
        val tmp_linReqDate = view!!.findViewById<LinearLayout>(R.id.labelTextReqDate)
        val tmp_linCenter = view!!.findViewById<LinearLayout>(R.id.labelTextCenter)
        val tmp_linPersonInCharge = view!!.findViewById<LinearLayout>(R.id.labelTextPersonInCharge)
        val tmp_linCenterPhone = view!!.findViewById<LinearLayout>(R.id.labelTextCenterPhone)
        val tmp_linReqName = view!!.findViewById<LinearLayout>(R.id.labelTextReqName)
        val tmp_linReqCategory = view!!.findViewById<LinearLayout>(R.id.labelTextReqCategory)
        val tmp_linReqReason = view!!.findViewById<LinearLayout>(R.id.labelTextReqReason)
        val tmp_linReqType = view!!.findViewById<LinearLayout>(R.id.labelTextReqType)
        val tmp_linDueDate = view!!.findViewById<LinearLayout>(R.id.labelTextDueDate)
        val tmp_linBankInfo = view!!.findViewById<LinearLayout>(R.id.labelTextBankInfo)
        val tmp_linReasonDate = view!!.findViewById<LinearLayout>(R.id.labelTextReqReasonDate) // 사고일자
        val tmp_linReqPlace = view!!.findViewById<LinearLayout>(R.id.labelTextReqPlace) // 사고장소
        val tmp_linReqCntt = view!!.findViewById<LinearLayout>(R.id.labelTextReqCntt) // 사고내용
        val tmp_linDignNm = view!!.findViewById<LinearLayout>(R.id.labelTextReqDignNm) // 진단명
        val tmp_labelNum = tmp_linNum?.findViewById<TextView>(R.id.label)
        val tmp_labelReqDate = tmp_linReqDate.findViewById<TextView>(R.id.label)
        val tmp_labelCenter = tmp_linCenter.findViewById<TextView>(R.id.label)
        val tmp_labelPersonInCharge = tmp_linPersonInCharge.findViewById<TextView>(R.id.label)
        val tmp_labelCenterPhone = tmp_linCenterPhone.findViewById<TextView>(R.id.label)
        val tmp_labelReqName = tmp_linReqName.findViewById<TextView>(R.id.label)
        val tmp_labelReqCategory = tmp_linReqCategory.findViewById<TextView>(R.id.label)
        val tmp_labelReqReason = tmp_linReqReason.findViewById<TextView>(R.id.label)
        val tmp_labelReqType = tmp_linReqType.findViewById<TextView>(R.id.label)
        val tmp_labelDueDate = tmp_linDueDate.findViewById<TextView>(R.id.label)
        val tmp_labelReqBank = tmp_linBankInfo.findViewById<TextView>(R.id.label)
        val tmp_labelReasonDate = tmp_linReasonDate.findViewById<TextView>(R.id.label) // 사고일자
        val tmp_labelReqPlace = tmp_linReqPlace.findViewById<TextView>(R.id.label) // 사고장소
        val tmp_labelReqCntt = tmp_linReqCntt.findViewById<TextView>(R.id.label) // 사고일자
        val tmp_labelDignNm = tmp_linDignNm.findViewById<TextView>(R.id.label) // 진단명
        tmp_labelNum?.text = resources.getString(R.string.label_request_number)
        tmp_labelReqDate.text = resources.getString(R.string.label_request_date)
        tmp_labelCenter.text = resources.getString(R.string.label_request_center)
        tmp_labelPersonInCharge.text = resources.getString(R.string.label_person_in_charge_s)
        tmp_labelCenterPhone.text = resources.getString(R.string.label_request_center_phone)
        tmp_labelReqName.text = resources.getString(R.string.label_request_name)
        tmp_labelReqCategory.text = resources.getString(R.string.label_req_category)
        tmp_labelReqReason.text = resources.getString(R.string.label_req_reason)
        tmp_labelReqType.text = resources.getString(R.string.label_req_type)
        tmp_labelDueDate.text = resources.getString(R.string.label_pay_due_date)
        tmp_labelReqBank.text = resources.getString(R.string.label_req_bank_account)
        tmp_labelReasonDate.text = resources.getString(R.string.label_request_reason_date) // 사고일자
        tmp_labelReqPlace.text = resources.getString(R.string.label_request_place) // 사고장소
        tmp_labelReqCntt.text = resources.getString(R.string.label_request_cntt) // 사고내용
        tmp_labelDignNm.text = resources.getString(R.string.label_request_dign_Nm) // 진단명
        textNum = tmp_linNum?.findViewById(R.id.text)
        textReqDate = tmp_linReqDate.findViewById(R.id.text)
        textCenter = tmp_linCenter.findViewById(R.id.text)
        textPersonInCharge = tmp_linPersonInCharge.findViewById(R.id.text)
        textCenterPhone = tmp_linCenterPhone.findViewById(R.id.text)
        textReqName = tmp_linReqName.findViewById(R.id.text)
        textReqCategory = tmp_linReqCategory.findViewById(R.id.text)
        textReqReason = tmp_linReqReason.findViewById(R.id.text)
        textReqType = tmp_linReqType.findViewById(R.id.text)
        textDueDate = tmp_linDueDate.findViewById(R.id.text)
        textBankInfo = tmp_linBankInfo.findViewById(R.id.text)
        textReqReasonDate = tmp_linReasonDate.findViewById(R.id.text) // 사고일자
        textReqPlace = tmp_linReqPlace.findViewById(R.id.text) // 사고장소
        textReqCntt = tmp_linReqCntt.findViewById(R.id.text) // 사고내용
        textReqDignNm = tmp_linDignNm.findViewById(R.id.text) // 진단명
    }

    /**
     * 심사진행결과 UI 세팅 함수
     */
    private fun setUIResultInfo() {
        val tmp_linState = Objects.requireNonNull(view)?.findViewById<LinearLayout>(R.id.labelTextState)
        val tmp_linDoDate = view!!.findViewById<LinearLayout>(R.id.labelTextDoDate)
        linFailReason = view!!.findViewById(R.id.labelTextFailResult)
        linMoney = view!!.findViewById(R.id.labelTextMoney)
        val tmp_labelState = tmp_linState?.findViewById<TextView>(R.id.label)
        val tmp_labelDoDate = tmp_linDoDate.findViewById<TextView>(R.id.label)
        textLabelFailReason = linFailReason?.findViewById(R.id.label)
        val tmp_labelPayment = linMoney?.findViewById<TextView>(R.id.label)
        tmp_labelState?.text = resources.getString(R.string.label_state)
        tmp_labelDoDate?.text = resources.getString(R.string.label_handle_date)
        textLabelFailReason?.setText(resources.getString(R.string.label_fail_reason_))
        tmp_labelPayment?.text = resources.getString(R.string.label_insure_payments)
        textState = tmp_linState?.findViewById(R.id.text)
        textDoDate = tmp_linDoDate.findViewById(R.id.text)
        textFailReason = linFailReason?.findViewById(R.id.text)
        textMoney = linMoney?.findViewById(R.id.text)
    }

    /**
     * 데이터 클리어
     * <pre>
     * - 2019-12-17    textReqReasonDate, textReqDignNm 추가
     * - 2020-01-06    linReqDocRoot.removeAllViews() 추가
    </pre> *
     */
    fun CF_clearData() {
        textNum!!.text = "" // 접수번호
        textReqDate!!.text = "" // 접수일자
        textCenter!!.text = "" // 접수센터
        textPersonInCharge!!.text = "" // 접수자
        textCenterPhone!!.text = "" // 접수센터 전화번호
        textReqName!!.text = "" // 청구자
        textReqCategory!!.text = "" // 청구유형 (정액, 실손, 정액+실손)
        textReqReason!!.text = "" // 발생원인 (질병..)
        textReqType!!.text = "" // 청구사유
        textDueDate!!.text = "" // 보험처리 예정일
        textBankInfo!!.text = "" // 송금요청 계좌번호
        textState!!.text = "" // 상태
        textDoDate!!.text = "" // 처리일자
        textMoney!!.text = "" // 서류보완 요청내용
        textReqReasonDate!!.text = "" // 사고일자
        textReqDignNm!!.text = "" // 진단명
        linFailReason!!.visibility = View.GONE
        linMoney!!.visibility = View.GONE
        mLinReqDocRoot!!.removeAllViews() // [청구서류] 하위뷰 삭제
    }

    /**
     * 스크롤 Top 포지션 이동 함수
     */
    fun CF_scrollTop() {
        val tmp_rootview = Objects.requireNonNull(view)?.findViewById<ScrollView>(R.id.rootView)
        tmp_rootview?.scrollTo(0, 0)
    }

    /**
     * 청구유형 코드 값을 이름으로 변환
     * @param p_category    String
     * @return  String
     */
    private fun convertCategoryCodeAll(p_category: String): String {
        val tmp_arrCategoryType = p_category.split(",").toTypedArray()
        val tmp_arrCategoryName = ArrayList<String?>()
        for (s in tmp_arrCategoryType) {
            tmp_arrCategoryName.add(convertCategoryCodeName(s))
        }
        return TextUtils.join("/", tmp_arrCategoryName)
    }

    /**
     * 청구사유 코드 값을 이름으로 변환
     * @param p_reason  String
     * @return  String
     */
    private fun convertTypeCodeAll(p_reason: String): String {
        val tmp_arrReasonCode = p_reason.split(",").toTypedArray()
        val tmp_arrReasonName = ArrayList<String?>()
        for (s in tmp_arrReasonCode) {
            tmp_arrReasonName.add(convertTypeCodeName(s))
        }
        return TextUtils.join("/", tmp_arrReasonName)
    }

    /**
     * 발생원인 코드 값을 이름으로 변환
     * @param p_type    String
     * @return  String
     */
    private fun convertReasonCodeAll(p_type: String): String {
        val tmp_arrTypeCode = p_type.split(",").toTypedArray()
        val tmp_arrTypeName = ArrayList<String?>()
        for (s in tmp_arrTypeCode) {
            tmp_arrTypeName.add(convertReasonCodeName(s))
        }
        return TextUtils.join("/", tmp_arrTypeName)
    }

    /**
     * 청구유형 코드 값을 문자로 반환 한다.
     * @param p_categoryCode    String
     * @return  String
     */
    private fun convertCategoryCodeName(p_categoryCode: String): String {
        val tmp_index = Arrays.asList(*EnvConfig.reqCategoryCode).indexOf(p_categoryCode)
        return if (tmp_index >= 0 && tmp_index < EnvConfig.reqCategoryName.size) {
            EnvConfig.reqCategoryName[tmp_index]
        } else ""
    }

    /**
     * 청구사유 코드 값을 문자로 반환한다.
     * @param p_typeCode    String
     * @return              String
     */
    private fun convertTypeCodeName(p_typeCode: String): String {
        val tmp_index = Arrays.asList(*EnvConfig.reqTypeCode).indexOf(p_typeCode)
        return if (tmp_index >= 0 && tmp_index < EnvConfig.reqTypeName.size) {
            EnvConfig.reqTypeName[tmp_index]
        } else ""
    }

    /**
     * 발생원인 코드 값을 문자로 반환한다.
     * @param p_reasonCode  String
     * @return              String
     */
    private fun convertReasonCodeName(p_reasonCode: String): String {
        val tmp_index = Arrays.asList(*EnvConfig.reqReasonCode).indexOf(p_reasonCode)
        return if (tmp_index >= 0 && tmp_index < EnvConfig.reqReasonName.size) {
            EnvConfig.reqReasonName[tmp_index]
        } else ""
    }

    /**
     * 보험금청구 상세내역 데이터 세팅 함수
     * @param p_jsonObject      JSONObject
     * @throws JSONException    JSONException
     * @since 2019-12-17    사고일자,사고시간,사고장소,사고내용,진단명,질병코드번호 추가
     * @since 2020-01-06    청구서류(스캔횟수,스캔리스트) 추가
     * @since 2020-01-06    청구접수ID 추가
     */
    @Throws(JSONException::class)
    private fun setData(p_jsonObject: JSONObject) {
        LogPrinter.CF_debug("!----------------------------------------------------------")
        LogPrinter.CF_debug("!-- IUII51M00_F.setData()")
        LogPrinter.CF_debug("!----------------------------------------------------------")
        linContents!!.visibility = View.VISIBLE
        val jsonKey_s_recp_date = "s_recp_date" // 접수일자
        val jsonKey_s_recp_cent_nm = "s_recp_cent_nm" // 접수센터 이름
        val jsonKey_s_recp_cent_tlno = "s_recp_cent_tlno" // 접수센터 전화번호
        val jsonKey_s_recp_cent_chps_nm = "s_recp_cent_chps_nm" // 접수센터 담당자명
        val jsonKey_s_pay_clam_nm = "s_pay_clam_nm" // 청구자(고객명)
        val jsonKey_s_insu_requ_type_code = "s_insu_requ_type_code" // 청구유형코드
        val jsonKey_s_requ_gent_caus_code = "s_requ_gent_caus_code" // 발생원인코드
        val jsonKey_s_insu_requ_resn_code = "s_insu_requ_resn_code" // 청구사유코드 여러개
        val jsonKey_s_requ_proc_schd_date = "s_requ_proc_schd_date" // 보험처리 예정일
        val jsonKey_s_fnis_nm = "s_fnis_nm" // 금융기관명
        val jsonKey_s_acno = "s_acno" // 계좌번호
        val jsonKey_s_dpow_nm = "s_dpow_nm" // 예금주
        val jsonKey_s_proc_stat = "s_proc_stat" // 상태코드 (또는 상태명)
        val jsonKey_s_proc_date = "s_proc_date" // 처리일자
        val jsonKey_s_chps_cpst_opin = "s_chps_cpst_opin" // 보완사유
        val jsonKey_s_insm_pyam = "s_insm_pyam" // 보험금지급액
        val jsonKey_s_requ_doc_yn = "s_requ_doc_yn" // 구비서류보완 필요 여부 if Y 서류보완 필요
        val jsonKey_s_acdt_date = "s_acdt_date" // 사고일자
        val jsonKey_s_acdt_time = "s_acdt_time" // 사고시간
        val jsonKey_s_acdt_pace = "s_acdt_pace" // 사고장소
        val jsonKey_s_acdt_cntt = "s_acdt_cntt" // 사고내용
        val jsonKey_s_dign_nm = "s_dign_nm" // 진단명
        //final String jsonKey_s_sick_code_no         = "s_sick_code_no";         // 질병코드번호         // 현재화면에서는 사용하지 않아서 주석처리함
        val jsonKey_s_requ_recp_id = "s_requ_recp_id" // 청구접수ID
        val jsonKey_s_scan_cnt = "s_scan_cnt" // 스캔횟수
        val jsonKey_s_scan_list = "s_scan_list" // 스캔리스트
        textReqDate!!.text = FormatUtils.CF_convertDate(p_jsonObject.getString(jsonKey_s_recp_date))
        textCenter!!.text = p_jsonObject.getString(jsonKey_s_recp_cent_nm)
        textPersonInCharge!!.text = p_jsonObject.getString(jsonKey_s_recp_cent_chps_nm)
        textCenterPhone!!.text = p_jsonObject.getString(jsonKey_s_recp_cent_tlno)
        textReqName!!.text = p_jsonObject.getString(jsonKey_s_pay_clam_nm)
        textReqCategory!!.text = convertCategoryCodeAll(p_jsonObject.getString(jsonKey_s_insu_requ_type_code))
        textReqReason!!.text = convertReasonCodeAll(p_jsonObject.getString(jsonKey_s_requ_gent_caus_code))
        textReqType!!.text = convertTypeCodeAll(p_jsonObject.getString(jsonKey_s_insu_requ_resn_code))
        textDueDate!!.text = FormatUtils.CF_convertDate(p_jsonObject.getString(jsonKey_s_requ_proc_schd_date))
        textDoDate!!.text = FormatUtils.CF_convertDate(p_jsonObject.getString(jsonKey_s_proc_date))

        // 사고일자 + 사고시간
        var tmp_reqReasonDate = p_jsonObject.getString(jsonKey_s_acdt_date) // 사고일자
        var tmp_reqReasonTime = p_jsonObject.getString(jsonKey_s_acdt_time) // 사고시간
        tmp_reqReasonDate = if (!TextUtils.isEmpty(tmp_reqReasonDate)) FormatUtils.CF_convertDate(tmp_reqReasonDate) else "-"
        tmp_reqReasonTime = if (!TextUtils.isEmpty(tmp_reqReasonTime)) tmp_reqReasonTime.substring(0, 2) + ":" + tmp_reqReasonTime.substring(2, 4) else ""
        tmp_reqReasonDate = "$tmp_reqReasonDate $tmp_reqReasonTime"
        textReqReasonDate!!.text = tmp_reqReasonDate

        // 사고장소
        val tmp_reqPlace = p_jsonObject.getString(jsonKey_s_acdt_pace)
        textReqPlace!!.text = if (!TextUtils.isEmpty(tmp_reqPlace)) tmp_reqPlace else "-"

        // 사고내용
        val tmp_reqCntt = p_jsonObject.getString(jsonKey_s_acdt_cntt)
        textReqCntt!!.text = if (!TextUtils.isEmpty(tmp_reqCntt)) tmp_reqCntt else "-"

        // 진단명
        val tmp_dign_nm = p_jsonObject.getString(jsonKey_s_dign_nm)
        textReqDignNm!!.text = if (!TextUtils.isEmpty(tmp_dign_nm)) tmp_dign_nm else "-"

        // (은행명 + 계좌 + 예금주)
        val tmp_bankName = p_jsonObject.getString(jsonKey_s_fnis_nm)
        val tmp_bankAccount = p_jsonObject.getString(jsonKey_s_acno)
        val tmp_bankOwner = p_jsonObject.getString(jsonKey_s_dpow_nm)
        textBankInfo!!.text = """
             $tmp_bankName
             $tmp_bankAccount
             $tmp_bankOwner
             """.trimIndent()

        // 청구심사상태
        val tmp_state = p_jsonObject.getString(jsonKey_s_proc_stat)
        textState!!.text = getStatusStr(tmp_state)
        val tmp_indexState = Arrays.asList(*EnvConfig.reqStateCode).indexOf(tmp_state)

        // 보험금지급액
        val tmp_money = p_jsonObject.getString(jsonKey_s_insm_pyam)
        textMoney!!.text = if (!TextUtils.isEmpty(tmp_money.trim { it <= ' ' })) tmp_money else "-"

        //
        var tmp_modBtnStatus = ""
        if (p_jsonObject.has(jsonKey_s_requ_doc_yn)) {
            tmp_modBtnStatus = p_jsonObject.getString(jsonKey_s_requ_doc_yn)
        }

        // -----------------------------------------------------------------------------------------
        // 뷰 컨트롤 (visible/gone)
        // -----------------------------------------------------------------------------------------
        btnModDoc!!.visibility = View.GONE // 보완신청버튼
        btnCancelReq!!.visibility = View.GONE // 청구취소버튼
        linFailReason!!.visibility = View.GONE // 반려사유
        linMoney!!.visibility = View.GONE // 보험금지급액

        // -------------------------------------------
        // -- 청구상태에 따른 처리
        // -------------------------------------------
        // --<1> (정상) 청구심사상태(0) - "접수"
        if (tmp_indexState == 0) {
            btnCancelReq!!.visibility = View.VISIBLE
        } else if (tmp_indexState >= 1 && tmp_indexState <= 6) {
            linMoney!!.visibility = View.VISIBLE // 보험금지급액
        } else {
            linFailReason!!.visibility = View.VISIBLE
            textLabelFailReason!!.text = resources.getString(R.string.label_reason)
            textFailReason!!.text = if (!TextUtils.isEmpty(p_jsonObject.getString(jsonKey_s_chps_cpst_opin))) p_jsonObject.getString(jsonKey_s_chps_cpst_opin) else "-"
        }

        // -------------------------------------------
        // -- (구비서류보완 필요) 0~5단계 & 서류보완Y
        // -------------------------------------------
        if (tmp_indexState >= 0 && tmp_indexState <= 5 && tmp_modBtnStatus.toLowerCase() == "y") {
            // 반려사유
            linFailReason!!.visibility = View.VISIBLE
            textLabelFailReason!!.text = resources.getString(R.string.label_fail_reason_)
            textFailReason!!.text = p_jsonObject.getString(jsonKey_s_chps_cpst_opin).replace("\\n", "\n----------------------------------------\n")
            btnModDoc!!.visibility = View.VISIBLE // 서류보완버튼
            linMoney!!.visibility = View.GONE // 보험금지급액
        }

        //------------------------------------------------------------------------------------------
        // -- 청구서류
        //------------------------------------------------------------------------------------------
        // -- 이전에 동적으로 생성된 버튼 삭제
        mLinReqDocRoot!!.removeAllViews()
        val s_requ_recp_id = p_jsonObject.getString(jsonKey_s_requ_recp_id) // 청구접수ID
        val s_scan_cnt = p_jsonObject.getString(jsonKey_s_scan_cnt) // 스캔횟수
        LogPrinter.CF_debug("!---- s_requ_recp_id(청구접수ID):$s_requ_recp_id")
        LogPrinter.CF_debug("!---- s_scan_cnt(스캔횟수):::::::$s_scan_cnt")
        val tmp_scanList_jArray = p_jsonObject.getJSONArray(jsonKey_s_scan_list)
        /*
         * s_scan_cnt          // 스캔 횟수
         * s_scan_list         // 스캔 리스트
         * s_sqnu              // 일련번호 (ex: 0001)
         * s_affr_type_dvsn    // 업무유형 구분 (C1:신규접수, C2:서류보완)
         * s_scan_doc_code     // 스캔문서코드 (300:신규접수, 399:서류보완)
         * s_scan_nm           // 스캔문서명 (지급청구서, 첨부서류)
         * s_scan_date         // 스캔일자 (ex: 20191223)
         * s_affr_type_nm      // 엄무유형명 (지급-신규, 지급-보완)
         */if (tmp_scanList_jArray != null) {
            for (i in 0 until tmp_scanList_jArray.length()) {
                val tmp_jsonObject = tmp_scanList_jArray.getJSONObject(i)
                val s_sqnu = tmp_jsonObject.getString("s_sqnu")
                val s_affr_type_dvsn = tmp_jsonObject.getString("s_affr_type_dvsn")
                val s_scan_doc_code = tmp_jsonObject.getString("s_scan_doc_code")
                val s_scan_doc_nm = tmp_jsonObject.getString("s_scan_doc_nm")
                val s_scan_date = tmp_jsonObject.getString("s_scan_date")
                val s_affr_type_nm = tmp_jsonObject.getString("s_affr_type_nm")
                LogPrinter.CF_debug("!---- " + (i + 1) + "번째============================")
                LogPrinter.CF_debug("!---- s_sqnu:::::::::::$s_sqnu")
                LogPrinter.CF_debug("!---- s_affr_type_dvsn:$s_affr_type_dvsn")
                LogPrinter.CF_debug("!---- s_scan_doc_code::$s_scan_doc_code")
                LogPrinter.CF_debug("!---- s_scan_nm::::::::$s_scan_doc_nm")
                LogPrinter.CF_debug("!---- s_scan_date::::::$s_scan_date")
                LogPrinter.CF_debug("!---- s_affr_type_nm:::$s_affr_type_nm")

                // -- 청구서류 뷰 생성
                val tmpInflater = Objects.requireNonNull(this.context)?.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

                // -- (라벨/텍스트/버튼) 필드 생성
                val tmpLinLabelTextButton = tmpInflater.inflate(R.layout.layout_label_text_button, mLinReqDocRoot, false) as LinearLayout
                val tmpLable = tmpLinLabelTextButton.findViewById<TextView>(R.id.label)
                val tmpText = tmpLinLabelTextButton.findViewById<TextView>(R.id.text)
                val tmpButton = tmpLinLabelTextButton.findViewById<TextView>(R.id.button)
                tmpLable.text = s_scan_doc_nm
                tmpText.text = FormatUtils.CF_convertDate(s_scan_date)
                tmpButton.tag = i
                tmpButton.setText(R.string.btn_show)
                tmpButton.setOnClickListener { /*
                            requ_recp_id    // 청구접수ID
                            affr_type       // 업무유형 구분(C1:신규접수, C2:서류보완)
                            doc_code        // 스캔문서코드(300:신규접수, 399:서류보완)
                            doc_sqnu        // 일련번호(ex: 0001)
                        */
                    requestDoc(s_requ_recp_id, s_affr_type_dvsn, s_scan_doc_code, s_sqnu, s_scan_doc_nm)
                }

                // 루트뷰에 attach
                mLinReqDocRoot!!.addView(tmpLinLabelTextButton)
                // 라인생성
                tmpInflater.inflate(R.layout.line_view_h, mLinReqDocRoot, true)
            }
        }
    }

    /**
     * 심사진형결과 상태값 문자열 반환 함수
     * @param p_statusCode  String
     * @return  String
     */
    private fun getStatusStr(p_statusCode: String): String {
        val tmp_index = Arrays.asList(*EnvConfig.reqStateCode).indexOf(p_statusCode)
        return if (tmp_index >= 0 && tmp_index < EnvConfig.reqStateName.size) {
            EnvConfig.reqStateName[tmp_index]
        } else ""
    }
    // #############################################################################################
    //  Activity 호출
    // #############################################################################################
    /**
     * (IUII10M00_S) 구비서류 보완 Activity 호출 함수
     */
    private fun startIUII10M00_S() {
        val tmp_intent = Intent(activity, IUII10M00_S::class.java)
        tmp_intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
        tmp_intent.putExtra("s_requ_recp_id", textNum!!.text.toString().trim { it <= ' ' })

        // --<> 휴대폰본인인증
        if (mActivity!!.CF_getAuthDvsn() == EnvConfig.AuthDvsn.MOBILE) {
            tmp_intent.putExtra("s_csno", mActivity!!.CF_getCsno())
            tmp_intent.putExtra("bSelfReqInquery", true)
        }
        startActivityForResult(tmp_intent, EnvConfig.REQUESTCODE_ACTIVITY_IUII10M00_S)
    }
    // #############################################################################################
    //  Http 관련
    // #############################################################################################
    /**
     * (HTTP요청) 보험금청구조회 상세 정보 요청 함수
     */
    fun CF_requestDetail(p_s_requ_recp_id: String) {
        linContents!!.visibility = View.GONE
        textNum!!.text = p_s_requ_recp_id // -- 접수번호 세팅
        mActivity!!.CF_showProgressDialog() // -- 프로그레스 다이얼로그 show
        val reqUrl: String
        val tmp_builder = Uri.Builder()
        tmp_builder.appendQueryParameter("s_requ_recp_id", p_s_requ_recp_id) // 접수번호

        // --<1> (휴대폰인증)
        if (mActivity!!.CF_getAuthDvsn() == EnvConfig.AuthDvsn.MOBILE) {
            LogPrinter.CF_debug("!--<1> 휴대폰인증 청구내역조회:$p_s_requ_recp_id")
            reqUrl = EnvConfig.host_url + EnvConfig.URL_CLAIM_MOBILE_DETAIL_INQ
            tmp_builder.appendQueryParameter("s_acdp_csno", mActivity!!.CF_getCsno())
        } else {
            LogPrinter.CF_debug("!--<1> (기타인증) 청구내역조회:$p_s_requ_recp_id")
            reqUrl = EnvConfig.host_url + EnvConfig.URL_CLAIM_CERT_DETAIL_INQ
            tmp_builder.appendQueryParameter("tempKey", SharedPreferencesFunc.getWebTempKey(Objects.requireNonNull(activity)))
        }
        HttpConnections.sendPostData(
                reqUrl,
                tmp_builder.build().encodedQuery,
                handler,
                HANDLERJOB_DETAIL,
                HANDLERJOB_ERROR_DETAIL)
    }

    /**
     * (HTTP결과) 보험금청구조회 상세 정보 요청 결과 처리 함수
     * @param p_jsonObject      JSONObject
     * @throws JSONException    JSONException
     */
    @Throws(JSONException::class)
    private fun setResultOfDetail(p_jsonObject: JSONObject) {
        // 프로그레스바 dismiss
        mActivity!!.CF_dismissProgressDialog()
        val jsonKey_errorCode = "errCode"
        val jsonKey_data = "data"
        val tmp_errorCode: String

        // --<1> (정상) : 에러코드 있음
        if (p_jsonObject.has(jsonKey_errorCode)) {
            // IUCOA0M00 에서는 반환하는 errorCode 없음. errorCode empty 검사 제외
            tmp_errorCode = p_jsonObject.getString(jsonKey_errorCode)
            // --<2> (에러)
            if (tmp_errorCode == "ERRIUII51M00001") {
                mActivity!!.CF_setCurrentPage(0)
                CommonFunction.CF_showCustomAlertDilaog(activity, resources.getString(R.string.dlg_error_erriuii40m02001), resources.getString(R.string.btn_ok))
            } else if (p_jsonObject.has(jsonKey_data)) {
                // -- 데이터 세팅 및 화면 Draw
                setData(p_jsonObject.getJSONObject(jsonKey_data))
            } else {
                mActivity!!.CF_setCurrentPage(0)
                CommonFunction.CF_showCustomAlertDilaog(activity, resources.getString(R.string.dlg_error_server_2), resources.getString(R.string.btn_ok))
            }
        } else {
            mActivity!!.CF_setCurrentPage(0)
            CommonFunction.CF_showCustomAlertDilaog(activity, resources.getString(R.string.dlg_error_server_1), resources.getString(R.string.btn_ok))
        }
    }

    /**
     * (HTTP요청) 청구취소 요청 함수
     */
    fun requestCancel() {
        // -- 프로그레스 다이얼로그 show
        mActivity!!.CF_showProgressDialog()
        val reqUrl: String
        val tmp_builder = Uri.Builder()
        tmp_builder.appendQueryParameter("s_requ_recp_id", textNum!!.text.toString()) // 접수번호

        // --<1> (휴대폰인증)
        if (mActivity!!.CF_getAuthDvsn() == EnvConfig.AuthDvsn.MOBILE) {
            LogPrinter.CF_debug("!--<1> 휴대폰인증 청구내역조회")
            reqUrl = EnvConfig.host_url + EnvConfig.URL_CLAIM_CANCLE_MOBILE_REQ
            tmp_builder.appendQueryParameter("s_acdp_csno", mActivity!!.CF_getCsno())
        } else {
            LogPrinter.CF_debug("!--<1> 로그인 청구내역조회")
            reqUrl = EnvConfig.host_url + EnvConfig.URL_CLAIM_CANCLE_CERT_REQ
            tmp_builder.appendQueryParameter("tempKey", SharedPreferencesFunc.getWebTempKey(Objects.requireNonNull(activity)))
        }
        LogPrinter.CF_debug("!---- s_requ_recp_id(접수번호) :" + textNum!!.text.toString())
        LogPrinter.CF_debug("!---- s_acdp_csno(고객번호)    :" + mActivity!!.CF_getCsno())
        LogPrinter.CF_debug("!---- tempKey                 :" + SharedPreferencesFunc.getWebTempKey(Objects.requireNonNull(activity)))
        HttpConnections.sendPostData(
                reqUrl,
                tmp_builder.build().encodedQuery,
                handler,
                HANDLERJOB_REQ_CANCEL,
                HANDLERJOB_REQ_ERROR_CANCEL)
    }

    /**
     * (HTTP결과) 청구취소 요청 결과 처리 함수
     * @param p_jsonObject      JSONObject
     * @throws JSONException    JSONException
     */
    @Throws(JSONException::class)
    private fun setResultOfCancel(p_jsonObject: JSONObject) {
        LogPrinter.CF_debug("!----------------------------------------------------------")
        LogPrinter.CF_debug("!-- IUII51M00_F.setResultOfCancel() --(HTTP결과) 청구취소 요청 결과 처리")
        LogPrinter.CF_debug("!----------------------------------------------------------")

        // -- 프로그레스바 dismiss
        mActivity!!.CF_dismissProgressDialog()
        val jsonKey_errorCode = "errCode"
        val tmp_errorCode: String

        // --<1> 정상 : 에러코드 있음
        if (p_jsonObject.has(jsonKey_errorCode)) {
            tmp_errorCode = p_jsonObject.getString(jsonKey_errorCode)
            LogPrinter.CF_debug("!---- 에러코드(공백일때 성공):$tmp_errorCode")
            when (tmp_errorCode) {
                "" -> {
                    CommonFunction.CF_showCustomAlertDilaog(activity, resources.getString(R.string.dlg_okiuii52_cancel), resources.getString(R.string.btn_ok))
                    // -- 목록 재요청
                    mActivity?.fragmentList?.httpReq_claimList()
                    mActivity!!.CF_setCurrentPage(0)
                }
                "ERRIUII52M00001" -> CommonFunction.CF_showCustomAlertDilaog(activity, resources.getString(R.string.dlg_erriuii52m00001), resources.getString(R.string.btn_ok))
                "ERRIUII52M00002" -> CommonFunction.CF_showCustomAlertDilaog(activity, resources.getString(R.string.dlg_erriuii52m00002), resources.getString(R.string.btn_ok))
                "ERRIUII52M00003" -> CommonFunction.CF_showCustomAlertDilaog(activity, resources.getString(R.string.dlg_erriuii52m00003), resources.getString(R.string.btn_ok))
                else -> CommonFunction.CF_showCustomAlertDilaog(activity, resources.getString(R.string.dlg_error_server_2), resources.getString(R.string.btn_ok))
            }
        } else {
            mActivity!!.CF_setCurrentPage(0)
            CommonFunction.CF_showCustomAlertDilaog(activity, resources.getString(R.string.dlg_error_server_3), resources.getString(R.string.btn_ok))
        }
    }

    /**
     * 보험금청구 첨부서류 이미지 조회
     * @param requ_recp_id  String  청구접수ID
     * @param affr_type     String  업무유형 구분 (C1:신규접수, C2:서류보완)
     * @param doc_code      String  스캔문서코드 (300:신규접수, 399:서류보완)
     * @param doc_sqnu      String  일련번호 (ex: 0001)
     */
    fun requestDoc(requ_recp_id: String, affr_type: String, doc_code: String, doc_sqnu: String, fileTitle: String?) {
        LogPrinter.CF_debug("!----------------------------------------------------------")
        LogPrinter.CF_debug("!-- IUII51M00_F.CF_requestDoc()")
        LogPrinter.CF_debug("!----------------------------------------------------------")
        var reqUrl = EnvConfig.host_url + EnvConfig.URL_CLAIM_IMAGE_INQ
        reqUrl += "?s_acdp_csno=" + mActivity!!.CF_getCsno()
        reqUrl += "&s_requ_recp_id=$requ_recp_id"
        reqUrl += "&s_affr_type=$affr_type"
        reqUrl += "&s_doc_code=$doc_code"
        reqUrl += "&s_doc_sqnu=$doc_sqnu"
        val fileName = requ_recp_id + "_" + doc_code + "_" + doc_sqnu + ".pdf"
        WebFileDownloadHelper.webFileDownloadManager(this.context, reqUrl, "", fileName, "", false, 0)
        LogPrinter.CF_debug("!---- IUII51M00_F URL::::::: $reqUrl")
        LogPrinter.CF_debug("!---- IUII51M00_F File Name: $fileName")
    }
}