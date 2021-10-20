package com.epost.insu.fragment

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.telephony.PhoneNumberFormattingTextWatcher
import android.telephony.PhoneNumberUtils
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.view.*
import android.view.View.OnTouchListener
import android.view.accessibility.AccessibilityEvent
import android.view.inputmethod.EditorInfo
import android.widget.*
import android.widget.TextView.OnEditorActionListener
import com.epost.insu.EnvConfig
import com.epost.insu.R
import com.epost.insu.SharedPreferencesFunc
import com.epost.insu.activity.IUCOF0M00
import com.epost.insu.common.*
import com.epost.insu.control.ButtonGroupView
import com.epost.insu.control.SwitchingControl
import com.epost.insu.data.Data_IUII90M03_F
import com.epost.insu.dialog.CustomDialog
import com.epost.insu.event.OnChangedCheckedStateEventListener
import com.epost.insu.event.OnSelectedChangeEventListener
import com.epost.insu.network.HttpConnections
import com.epost.insu.network.WeakReferenceHandler
import com.epost.insu.network.WeakReferenceHandler.ObjectHandlerMessage
import com.softsecurity.transkey.*
import org.json.JSONException
import org.json.JSONObject
import java.util.*

/**
 * 보험금청구 > 자녀청구 > 3단계. 보험청구서작성(개인정보)
 * @since     :
 * @version   : 1.3
 * @author    : YJH
 * @see
 * <pre>
 * ======================================================================
 * 0.0.0    YJH_20181109    최초 등록
 * 0.0.0    YJH_20181109    계=피=수 조건 변경 -> (수익자=피보험자)
 * 0.0.0    NJM_20191002    (자녀)지급청구 마지막 단계 하얀화면 문제 해결 (3단계에서 고객휴대폰 번호 소실되는 문제 수정)
 * 0.0.0    NJM_20191007    최근 청구 계좌 & 우체국 계좌 조회 후 화면에 초기세팅
 * 0.0.0    NJM_20200123    자녀보험금청구에 휴대폰인증 추가 (고객정보조회하는부분에서 서버작업필요함)
 * 0.0.0    NJM_20200213    필수사항 라벨, 선택사항 라벨, 직장 하시는일 레이아웃 gone / 키패드레이아웃 관련 수정
 * 1.5.3    NJM_20210421    [subUrl 공통파일로 변경]
 * 1.5.3    NJM_20210422    [자녀청구 예금주소실 수정]
 * 1.6.2    NJM_20210802    [2021년 대우 취약점] 2차본 : 부적절한 예외 처리
 * 1.6.3    NJM_20210826    [캡쳐방지 예외] 캡쳐방지 예외추가 및 로그표기일때만 예외처리
 * =======================================================================
 * copyright : (주)프로니스정보기술 | http://www.phronis.co.kr
 * </pre>
 */
class IUII90M03_F : IUII90M00_FD(), ITransKeyActionListener, ITransKeyActionListenerEx, ITransKeyCallbackListener, OnTouchListener, ObjectHandlerMessage {
    private val HANDLERJOB_GET_USERINFO        = 0
    private val HANDLERJOB_ERROR_GET_USERINFO  = 1
    private val HANDLERJOB_GET_CHILDINFO       = 2
    private val HANDLERJOB_ERROR_GET_CHILDINFO = 3

    private val REQUESTCODE_SEARCH_ADDR = 1

    private val length_company = 40
    private val length_job = 100
    private val length_mobile = 13
    private val maxLengthOfName = 20
    private val maxLengthOfResident1 = 6
    private val maxLengthOfResident2 = 7

    private var editMobile: EditText? = null
    private var editCompany: EditText? = null
    private var editJob: EditText? = null
    private var textName: TextView? = null
    private var textResidentNum: TextView? = null
    private var textSMS: TextView? = null
    private var textAddress: TextView? = null
    private var editName: EditText? = null
    private var edtResident1: EditText? = null
    private var edtResident2: EditText? = null // 자녀이름, 주민번호
    private var editBeneficiaryName: EditText? = null // 수익자명
    private var editAnotherParentname: EditText? = null // 청구자외의 다른 친권자 이름
    private var switchingNoti: SwitchingControl? = null
    private var btnGroupRelation: ButtonGroupView? = null // 계약관계유형 (1:계약자, 2:입원(장해)수익자)
    private var btnGroupParentType: ButtonGroupView? = null // 자녀와의 관계 (1:부, 2:모)
    private var btnGroupParentAgree: ButtonGroupView? = null // 친권자동의 (1:예, 2:아니오)
    private var btnGroupParentAloneReason: ButtonGroupView? = null // 친권단독행사 사유 (1:이혼, 2:사별, 3:기타)
    private var relResident2: RelativeLayout? = null
    private var parentRelativeGroupView: LinearLayout? = null
    private var parentAloneReasonGroupView: LinearLayout? = null
    private var relChildInfoView: LinearLayout? = null
    private var beneficiaryNameView: LinearLayout? = null
    private var anotherParentNameView: LinearLayout? = null
    private var requireGroupView: LinearLayout? = null

    private var s_mobl_no: String? = null
    private var s_psno: String? = null
    private var s_addr1: String? = null
    private var s_addr2: String? = null
    private var moblWatcher: TextWatcher? = null

    private var mTransKeyCtrl: TransKeyCtrl? = null
    private var data: Data_IUII90M03_F? = null
    private var handler: WeakReferenceHandler? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setInit()
    }

    @SuppressLint("InflateParams")
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.iuii90m03_f, null)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        if (savedInstanceState != null) {
            if (savedInstanceState.containsKey("data")) {
                data = savedInstanceState.getParcelable("data")
            }
        }
        setUIControl() // UI 생성 및 세팅
        restoreData() // 데이터 복구
        initTransKeyPad() // 보안키패드 초기화
    }

    override fun onActivityResult(p_requestCode: Int, p_resultCode: Int, p_data: Intent?) {
        super.onActivityResult(p_requestCode, p_resultCode, p_data)

        // ---------------------------------------------------------------------------------
        //  -- 주소검색 및 상세주소 입력 완료 시
        // ---------------------------------------------------------------------------------
        if (p_requestCode == REQUESTCODE_SEARCH_ADDR && p_resultCode == Activity.RESULT_OK) {
            if (p_data != null) {
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

                // -- 접근성 ON : 입력하신 주소를 적용합니다.
                if (CommonFunction.CF_checkAccessibilityTurnOn(activity)) {
                    showCustomDialog("입력하신 주소를 적용합니다.", textAddress!!)
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
        LogPrinter.CF_debug("!----------------------------------------------------------")
        LogPrinter.CF_debug("!-- IUII90M03_F.setInit()")
        LogPrinter.CF_debug("!----------------------------------------------------------")

        moblWatcher = PhoneNumberFormattingTextWatcher()
        data = Data_IUII90M03_F()
        handler = WeakReferenceHandler(this)
        try {
            mTransKeyCtrl = TransKeyCtrl(activity)
        } catch (e: NullPointerException) {
            LogPrinter.CF_debug(resources.getString(R.string.log_fail_create_trankey))
            e.message
        } catch (e: Exception) {
            LogPrinter.CF_debug(resources.getString(R.string.log_fail_create_trankey))
            e.message
        }
        LogPrinter.CF_debug("!---- (3단계) 인증구분 : " + mActivity?.CF_getAuthDvsn())
    }

    /**
     * UI 생성 및 세팅 함수
     */
    private fun setUIControl() {
        // -----------------------------------------------------------------------------------------
        // -- 청구자(부모)
        // -----------------------------------------------------------------------------------------
        textName = view!!.findViewById(R.id.textName) // 이름
        textResidentNum = view!!.findViewById(R.id.textResidentNumber) // 주민등록번호

        // -- 계약관계가 입원(장애)수익자일 때 : 필수사항 필드
        // 휴대폰번호
        editMobile = view!!.findViewById(R.id.edtMobile)
        editMobile?.filters = CommonFunction.CF_getInputLengthFilter(length_mobile)
        editMobile?.addTextChangedListener(moblWatcher)

        // 휴대폰번호 수정 이미지 버튼
        val btnModMobile = view!!.findViewById<ImageButton>(R.id.btnMod)
        btnModMobile.setOnClickListener { resetMobile() }

        // 알림서비스 신청여부 버튼 UI 세팅
        setNotifyServiceAgreeButtonUI()
        textAddress = view!!.findViewById(R.id.textAddress)

        // 주소검색 버튼
        val btnSearchAddr = view!!.findViewById<LinearLayout>(R.id.linBtnSearchAddr)
        btnSearchAddr.setOnClickListener {
            if (CommonFunction.CF_checkAccessibilityTurnOn(activity)) {
                val customDialog = CustomDialog(activity!!)
                customDialog.show()
                customDialog.CF_setBackKeyMode(CustomDialog.BackMode.CANCELED)
                customDialog.CF_setTextContent(resources.getString(R.string.dlg_accessible_move_iucof0m00))
                customDialog.CF_setDoubleButtonText(resources.getString(R.string.btn_cancel), resources.getString(R.string.btn_ok))
                customDialog.setOnDismissListener { dialog ->
                    if (!(dialog as CustomDialog).CF_getCanceled()) {
                        startSearchAddressActivity()
                    } else {
                        Handler().postDelayed({ btnSearchAddr.sendAccessibilityEvent(AccessibilityEvent.TYPE_VIEW_FOCUSED) }, 500)
                    }
                }
            } else {
                startSearchAddressActivity()
            }
        }

        // -----------------------------------------------------------------------------------------
        // -- 친권자동의
        // -----------------------------------------------------------------------------------------


        // -----------------------------------------------------------------------------------------
        // -- 피보험자
        // -----------------------------------------------------------------------------------------
        // -- 피보험자 선택사항
        // 직장명
        editCompany = view!!.findViewById(R.id.edtCompany)
        editCompany?.filters = CommonFunction.CF_getInputLengthFilter(length_company)

        // 하시는일(직업)
        editJob = view!!.findViewById(R.id.edtJob)
        editJob?.filters = CommonFunction.CF_getInputLengthFilter(length_job)


        // -----------------------------------------------------------------------------------------
        // -- 기타 View
        // -----------------------------------------------------------------------------------------
        scrollView = view!!.findViewById(R.id.scrollView)

        // 다음 버튼
        btnNext = view!!.findViewById(R.id.btnFill)
        btnNext?.text = resources.getString(R.string.btn_next_2)
        btnNext?.setOnClickListener { //  -- 키패드 가리기
            if (activity!!.currentFocus != null) CommonFunction.CF_closeVirtualKeyboard(activity, activity!!.currentFocus!!.windowToken)
            if (checkUserInput()) {
                httpReq_childInfo()

                /*
                    // EditText 포커스 영향으로 스크롤 변경되는 것을 막기위함으로 강제 포커스 세팅
                    textName.requestFocus();
                    textName.setFocusableInTouchMode(true);
                    editMobile.setFocusableInTouchMode(true);
                    editCompany.setFocusableInTouchMode(true);
                    editJob.setFocusableInTouchMode(true);
                    editName.setFocusableInTouchMode(true);

                    if (CommonFunction.CF_checkAccessibilityTurnOn(getActivity())) {
                        mActivity.CF_requestFocusIndicator();
                    }
                    */
            }
        }

        // -- 자녀보험금청구 관련 UI
        setUIChild()
    }

    /**
     * 자녀보험금청구 관련 UI 세팅
     */
    private fun setUIChild() {
        // -- 피보험자(자녀)
        relChildInfoView = view?.findViewById(R.id.relChildInfo)

        // -- 계약관계
        val beneficiryName = view!!.findViewById<LinearLayout>(R.id.labelEditBeneficiaryName)
        beneficiryName.isMotionEventSplittingEnabled = false

        // 라벨 세팅
        val txtBeneficiaryName = beneficiryName.findViewById<TextView>(R.id.label)
        txtBeneficiaryName.text = resources.getString(R.string.label_beneficiary_name)

        // -- 수익자명 Edit세팅
        editBeneficiaryName = beneficiryName.findViewById(R.id.edit)
        editBeneficiaryName?.contentDescription = resources.getString(R.string.desc_edt_name)
        editBeneficiaryName?.filters = CommonFunction.CF_getInputLengthFilter(maxLengthOfName)
        editBeneficiaryName?.hint = resources.getString(R.string.hint_name_2)
        editBeneficiaryName?.setOnTouchListener(this)
        editBeneficiaryName?.setOnEditorActionListener(OnEditorActionListener { v, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_NEXT) {
                editBeneficiaryName?.isCursorVisible = false
                /*
                    editResident_1.setCursorVisible(true);
                    if(CommonFunction.CF_checkAccessibilityTurnOn(IUII90M03_F.this.getActivity())){
                        editResident_1.sendAccessibilityEvent(AccessibilityEvent.TYPE_VIEW_FOCUSED);
                    }
                    */
            }
            false
        })

        // 수익자명 숨기기
        beneficiaryNameView = view!!.findViewById(R.id.beneficiaryName)
        beneficiaryNameView?.visibility = View.GONE
        val linName = view!!.findViewById<LinearLayout>(R.id.labelEditName)
        linName.isMotionEventSplittingEnabled = false

        // 라벨 세팅
        val txtName = linName.findViewById<TextView>(R.id.label)
        txtName.text = resources.getString(R.string.label_name)

        // 자녀이름 Edit세팅
        editName = linName.findViewById(R.id.edit)
        editName?.contentDescription = resources.getString(R.string.desc_edt_name)
        editName?.filters = CommonFunction.CF_getInputLengthFilter(maxLengthOfName)
        editName?.hint = resources.getString(R.string.hint_name_2)
        editName?.setOnTouchListener(this)
        editName?.setOnEditorActionListener(OnEditorActionListener { v, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_NEXT) {
                editName?.isCursorVisible = false
                edtResident1!!.isCursorVisible = true
                if (CommonFunction.CF_checkAccessibilityTurnOn(this@IUII90M03_F.activity)) {
                    edtResident1!!.sendAccessibilityEvent(AccessibilityEvent.TYPE_VIEW_FOCUSED)
                }
            }
            false
        })

        // -- 자녀주민번호 앞자리
        edtResident1 = view!!.findViewById(R.id.edtResident_1)
        edtResident1?.filters = CommonFunction.CF_getInputLengthFilter(maxLengthOfResident1)
        edtResident1?.contentDescription = resources.getString(R.string.desc_edt_resident_first)
        edtResident1?.nextFocusRightId = R.id.editText
        edtResident1?.setOnEditorActionListener(OnEditorActionListener { v, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_NEXT) {
                showTransKeyPad()
            }
            false
        })
        edtResident1?.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                if (s.length == maxLengthOfResident1) {
                    showTransKeyPad()
                }
            }

            override fun afterTextChanged(s: Editable) {}
        })
        edtResident1?.setOnTouchListener(this)

        // -- 주민번호 뒷자리
        edtResident2 = view!!.findViewById<View>(R.id.resident_relInput).findViewById(R.id.editText)
        edtResident2?.setOnTouchListener(this)
        edtResident2?.contentDescription = resources.getString(R.string.desc_edt_resident_last)
        edtResident2?.importantForAccessibility = View.IMPORTANT_FOR_ACCESSIBILITY_NO
        relResident2 = view!!.findViewById(R.id.resident_relInput_sub)
        relResident2?.isMotionEventSplittingEnabled = false
        relResident2?.contentDescription = "입력창, " + resources.getString(R.string.desc_edt_resident_last) + ", 편집하려면 두 번 누르세요."
        val keyLayout = view!!.findViewById<View>(R.id.resident_relInput).findViewById<ViewGroup>(R.id.keylayout)
        keyLayout.setOnTouchListener(this)
        val keyScroll = view!!.findViewById<View>(R.id.resident_relInput).findViewById<ViewGroup>(R.id.keyscroll)
        keyScroll.setOnTouchListener(this)

        // 필수사항 그룹 숨기기
        requireGroupView = view!!.findViewById(R.id.linRequire)
        requireGroupView?.visibility = View.GONE

        // 계약관계유형 선택
        val tmp_arrWeight = floatArrayOf(1.0f, 1.0f)
        btnGroupRelation = view!!.findViewById(R.id.btnGroupRelation)
        btnGroupRelation?.CF_setButtons(EnvConfig.reqChildRelationName, booleanArrayOf(true, false))

        // 계약관계유형을 '계약자'로 선택시, '수익자명' 입력창 보이기
        btnGroupRelation?.CE_setOnSelectedChangeEventListener(OnSelectedChangeEventListener { p_index ->
            if (p_index == 0) {
                editBeneficiaryName?.setText("")
                beneficiaryNameView?.visibility = View.VISIBLE

                //editMobile.removeTextChangedListener(new PhoneNumberFormattingTextWatcher());
                editMobile!!.removeTextChangedListener(moblWatcher)
                requireGroupView?.visibility = View.GONE
            } else {
                beneficiaryNameView?.visibility = View.GONE

                // 휴대폰 번호 셋팅
                editMobile = view!!.findViewById(R.id.edtMobile)
                editMobile?.filters = CommonFunction.CF_getInputLengthFilter(length_mobile)
                //editMobile.addTextChangedListener(new PhoneNumberFormattingTextWatcher());
                editMobile?.addTextChangedListener(moblWatcher)
                editMobile?.setText(PhoneNumberUtils.formatNumber(s_mobl_no)) // 휴대폰번호
                setAddressText(s_psno, s_addr1, s_addr2) // 주소
                requireGroupView?.visibility = View.VISIBLE
            }
        })

        // 자녀와의 관계유형 선택
        btnGroupParentType = view!!.findViewById(R.id.btnGroupParentType)
        btnGroupParentType?.CF_setButtons(EnvConfig.reqChildParentTypeName, booleanArrayOf(true, false))

        // 친권자동의 선택
        btnGroupParentAgree = view!!.findViewById(R.id.btnGroupParentAgree)
        btnGroupParentAgree?.CF_setButtons(EnvConfig.reqChildParentAgreeName, booleanArrayOf(true, false))

        // 친권자외의 다른 부모 이름
        val anotherParentName = view!!.findViewById<LinearLayout>(R.id.labelEditAnotherParentName)
        anotherParentName.isMotionEventSplittingEnabled = false

        // 라벨 세팅
        val txtAnotherParentName = anotherParentName.findViewById<TextView>(R.id.label)
        txtAnotherParentName.text = resources.getString(R.string.label_another_parent_name)

        // 친권자외의 다른 부모 이름 Edit세팅
        editAnotherParentname = anotherParentName.findViewById(R.id.edit)
        editAnotherParentname?.contentDescription = resources.getString(R.string.desc_edt_another_parent_name)
        editAnotherParentname?.filters = CommonFunction.CF_getInputLengthFilter(maxLengthOfName)
        editAnotherParentname?.hint = resources.getString(R.string.hint_another_parent_name)
        editAnotherParentname?.setOnTouchListener(this)
        editAnotherParentname?.setOnEditorActionListener(OnEditorActionListener { v, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_NEXT) {
                editAnotherParentname?.isCursorVisible = false
            }
            false
        })

        // 청구자 외의 다른 친권자명 숨기기
        anotherParentNameView = view!!.findViewById(R.id.anotherParentName)
        anotherParentNameView?.visibility = View.GONE

        // 친권단독행사 사유 선택
        parentAloneReasonGroupView = view!!.findViewById(R.id.parentAloneReasonGroup)
        parentAloneReasonGroupView?.visibility = View.GONE
        parentRelativeGroupView = view!!.findViewById(R.id.parentRelativeGroup)
        btnGroupParentAloneReason = view!!.findViewById(R.id.btnGroupParentAloneReason)
        btnGroupParentAloneReason?.CF_setButtons(EnvConfig.reqChildParentAloneReasonName, booleanArrayOf(false, false, false))
        btnGroupParentAgree?.CE_setOnSelectedChangeEventListener(OnSelectedChangeEventListener { p_index ->
            if (p_index == 0) {
                parentAloneReasonGroupView?.visibility = View.GONE
                anotherParentNameView?.visibility = View.VISIBLE
            } else {
                parentAloneReasonGroupView?.visibility = View.VISIBLE
                anotherParentNameView?.visibility = View.GONE
                btnGroupParentAloneReason?.CF_setButtons(EnvConfig.reqChildParentAloneReasonName, booleanArrayOf(false, false, false))
            }
        })
    }

    /**
     * 데이터 복구
     */
    private fun restoreData() {
        textName!!.text = data!!.CF_getName()
        textResidentNum!!.text = data!!.CF_getBirth()
        editMobile!!.setText(data!!.CF_getMobile())
        switchingNoti!!.CF_setCheck(data!!.CF_getAgreeAlarm())
        if (switchingNoti!!.CF_getCheckState() > 0) {
            textSMS!!.contentDescription = resources.getString(R.string.desc_sms_agree_yes)
        } else {
            textSMS!!.contentDescription = resources.getString(R.string.desc_sms_agree_no)
        }
        editCompany!!.setText(data!!.CF_getCompany())
        editJob!!.setText(data!!.CF_getJob())
        setAddressText(data!!.CF_getZipNo(), data!!.CF_getAddress(), data!!.CF_getAddressDetail())
        btnGroupRelation!!.CF_setCheck(data!!.CF_getRelationType())
        btnGroupParentType!!.CF_setCheck(data!!.CF_getParentType())
        btnGroupParentAgree!!.CF_setCheck(data!!.CF_getAgreeType())
        btnGroupParentAloneReason!!.CF_setCheck(data!!.CF_getAgreeTypeReason())
        editAnotherParentname!!.setText(data!!.CF_getAnotherParentName())
        editBeneficiaryName!!.setText(data!!.CF_getBeneficiaryName())
        editName!!.setText(data!!.CF_getChildName())
        edtResident1!!.setText(data!!.CF_getChildRrno1())
        edtResident2!!.setText(data!!.CF_getChildRrno2_enc())

        //textNameChild.setText(data.CF_getNameChild());
        //textResidentNumberChild.setText(data.CF_getBirthChild());

        // 사용자 정보 요청을 한번이라도 한 경우 자동 포커스 영향으로 스크롤 이동 되는 것을 막기 위해
        // 처리한 포커스 값 변경
        if (!TextUtils.isEmpty(textName!!.text.toString().trim())) {
            textName!!.requestFocus()
            textName!!.isFocusableInTouchMode = true
            editMobile!!.isFocusableInTouchMode = true
            editCompany!!.isFocusableInTouchMode = true
            editJob!!.isFocusableInTouchMode = true
            editName!!.isFocusableInTouchMode = true
            editBeneficiaryName!!.isFocusableInTouchMode = true
            editAnotherParentname!!.isFocusableInTouchMode = true
        }
    }

    /**
     * 데이터 저장
     */
    private fun saveData() {
        data!!.CF_setName(textName!!.text.toString().trim())
        data!!.CF_setBirth(textResidentNum!!.text.toString().trim())
        data!!.CF_setMobile(editMobile!!.text.toString().trim())
        data!!.CF_setAgreeAlarm(switchingNoti!!.CF_getCheckState())
        data!!.CF_setCompany(editCompany!!.text.toString().trim())
        data!!.CF_setJob(editJob!!.text.toString().trim())
        data!!.CF_setRelationType(btnGroupRelation!!.CF_getCheckdButtonFirstIndex()) // 청구자의 계약관계    (0:계약자, 1:입원(장해)수익자)
        data!!.CF_setParentType(btnGroupParentType!!.CF_getCheckdButtonFirstIndex()) // 청구자의 자녀와의관계  (0:부, 1:모)
        data!!.CF_setAgreeType(btnGroupParentAgree!!.CF_getCheckdButtonFirstIndex()) // 공동친권여부 (0:공동, 1:단독)
        data!!.CF_setAgreeTypeReason(btnGroupParentAloneReason!!.CF_getCheckdButtonFirstIndex()) // 단독친권사유 (0:사망, 1:이혼, 2:기타)
        data!!.CF_setAnotherParentName(editAnotherParentname!!.text.toString().trim()) // 다른 친권자명
        data!!.CF_setBeneficiaryName(editBeneficiaryName!!.text.toString().trim()) // 수익자명 저장
        data!!.CF_setChildName(editName!!.text.toString().trim()) // 자녀명 저장
        data!!.CF_setChildRrno1(edtResident1!!.text.toString().trim()) // 자녀실명번호 앞자리
        data!!.CF_setChildRrno2_enc(mTransKeyCtrl!!.cipherDataEx) // 자녀실명번호 뒷자리
    }

    /**
     * 알림서비스 신청여부 UI 세팅 함수
     */
    private fun setNotifyServiceAgreeButtonUI() {
        textSMS = view!!.findViewById(R.id.textLabelSMS)
        switchingNoti = view!!.findViewById(R.id.switchingNoti)
        // -----------------------------------------------------------------------------------------
        //  기본 체크 상태는 restoreData() 에서 세팅 되며, Data_IUII12M00_F의
        // -----------------------------------------------------------------------------------------
//        switchingNoti.CF_setCheck(1);
        switchingNoti?.CE_setOnChangedCheckedStateEventListener(OnChangedCheckedStateEventListener { p_flagCheck ->
            if (p_flagCheck) {
                textSMS?.contentDescription = resources.getString(R.string.desc_sms_agree_yes)
            } else {
                textSMS?.contentDescription = resources.getString(R.string.desc_sms_agree_no)
            }
        })
    }

    /**
     * 휴대폰번호 초기화 후 입력 키패드 Show
     */
    private fun resetMobile() {
        editMobile!!.setText("")
        CommonFunction.CF_showVirtualKeyboard(activity, editMobile)
    }

    /**
     * 주소검색 Activity 호출 함수
     */
    private fun startSearchAddressActivity() {
        val intent = Intent(activity, IUCOF0M00::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
        startActivityForResult(intent, REQUESTCODE_SEARCH_ADDR)
    }

    /**
     * 주소 세팅 함수
     * @param p_postNo      String
     * @param p_addr        String
     * @param p_addrDetail  String
     */
    private fun setAddressText(p_postNo: String?, p_addr: String?, p_addrDetail: String?) {
        if (!TextUtils.isEmpty(p_postNo) && !TextUtils.isEmpty(p_addr)) {
            var addrFull = "[$p_postNo]\r\n$p_addr"
            if (!TextUtils.isEmpty(p_addrDetail)) {
                addrFull += """
                    $p_addrDetail
                    """.trimIndent()
            }
            textAddress!!.text = addrFull
            data!!.CF_setZipNo(p_postNo)
            data!!.CF_setAddress(p_addr)
            data!!.CF_setAddressDetail(p_addrDetail)
        }
    }

    /**
     * 사용자 입력값 검사 함수<br></br>
     * 필수 값만 검사한다.(휴대폰번호, 주소)
     * @return  boolean
     */
    private fun checkUserInput(): Boolean {
        var flagOk = true

        // --<> 계약관계유형
        if (!btnGroupRelation!!.CF_isChecked()) {
            flagOk = false
            mActivity?.CF_setVisibleStepIndicator(true)
            showCustomDialog(resources.getString(R.string.dlg_select_relation), parentRelativeGroupView!!)
        }
        // --<> 계약자 선택시, 수익자명 입력 체크
        else if (btnGroupRelation!!.CF_getCheckdButtonFirstIndex() == 0&& TextUtils.isEmpty(editBeneficiaryName!!.text.toString().trim())) {
            flagOk = false
            mActivity?.CF_setVisibleStepIndicator(true)
            showCustomDialogAndFocus(resources.getString(R.string.dlg_beneficaiary_name), editBeneficiaryName)
        }
        // --<> 계약자 선택시, 계약자와 수익자명이 같은지 비교
        else if (btnGroupRelation!!.CF_getCheckdButtonFirstIndex() == 0 && textName!!.text.toString().trim() == editBeneficiaryName!!.text.toString().trim()) {
            flagOk = false
            mActivity?.CF_setVisibleStepIndicator(true)
            showCustomDialogAndFocus(resources.getString(R.string.dlg_beneficaiary_name_same), editBeneficiaryName)
        }
        // --<> 자녀와의관계
        else if (!btnGroupParentType!!.CF_isChecked()) {
            flagOk = false
            mActivity?.CF_setVisibleStepIndicator(true)
            showCustomDialog(resources.getString(R.string.dlg_select_parent_type), parentRelativeGroupView!!)
        }
        // --<> 휴대폰번호 empty
        else if (btnGroupRelation!!.CF_getCheckdButtonFirstIndex() == 1 && TextUtils.isEmpty(editMobile!!.text.toString().trim())) {
            flagOk = false
            showCustomDialogAndFocus(resources.getString(R.string.dlg_empty_mobile), editMobile)
        }
        // --<> 휴대폰번호 정규식 에러
        else if (btnGroupRelation!!.CF_getCheckdButtonFirstIndex() == 1 && !Regex.CF_MacherMobileNoHypen(editMobile!!.text.toString().trim().replace("-", ""))) {
            flagOk = false
            showCustomDialogAndFocus(resources.getString(R.string.dlg_regex_mobile), editMobile)
        }
        // --<> 주소 empty
        else if (btnGroupRelation!!.CF_getCheckdButtonFirstIndex() == 1 && TextUtils.isEmpty(textAddress!!.text.toString().trim())) {
            flagOk = false
            CommonFunction.CF_showCustomAlertDilaog(activity, resources.getString(R.string.dlg_empty_address), resources.getString(R.string.btn_ok))
        }
        // --<> 공동친권여부
        else if (!btnGroupParentAgree!!.CF_isChecked()) {
            flagOk = false
            mActivity?.CF_setVisibleStepIndicator(true)
            showCustomDialog(resources.getString(R.string.dlg_select_parent_agree), view!!.findViewById(R.id.textParentAgree))
        }
        // --<> 다른 친권자 이름 입력 체크
        else if (btnGroupParentAgree!!.CF_getCheckdButtonFirstIndex() == 0 && TextUtils.isEmpty(editAnotherParentname!!.text.toString().trim())) {

            flagOk = false
            mActivity?.CF_setVisibleStepIndicator(true)
            showCustomDialog(resources.getString(R.string.dlg_another_parent_name), editAnotherParentname!!)
        }
        // --<> 다른 친권자 이름이 청구자와 같은지 체크
        else if (btnGroupParentAgree!!.CF_getCheckdButtonFirstIndex() == 0 && textName!!.text.toString().trim() == editAnotherParentname!!.text.toString().trim()) {
            flagOk = false
            mActivity?.CF_setVisibleStepIndicator(true)
            showCustomDialog(resources.getString(R.string.dlg_another_parent_name_same), editAnotherParentname!!)
        }
        // --<> 단독친권행사 사유
        else if (btnGroupParentAgree!!.CF_getCheckdButtonFirstIndex() == 1 && !btnGroupParentAloneReason!!.CF_isChecked()) {
            flagOk = false
            mActivity?.CF_setVisibleStepIndicator(true)
            showCustomDialog(resources.getString(R.string.dlg_select_parent_alone_reason), view!!.findViewById(R.id.textParentAloneReason))
        }
        // --<> 자녀이름입력 체크
        else if (editName!!.text.toString().trim().isEmpty()) {
            flagOk = false
            showCustomDialogAndFocus(resources.getString(R.string.dlg_child_name), editName)
        }
        else if (edtResident1!!.text.toString().length < maxLengthOfResident1 || mTransKeyCtrl!!.inputLength < maxLengthOfResident2) {
            flagOk = false
            showCustomDialogAndFocus(resources.getString(R.string.dlg_no_input_resident), edtResident1)
        }
//        else if(!CommonFunction.CF_isValidDate(editResident_1.getText().toString().trim())){
//            tmp_flagOk = false;
//            showCustomDialogAndFocus(getResources().getString(R.string.dlg_not_valid_resident1), editResident_1);
//        }
        return flagOk
    }

    /**
     * IUII10M03_F 데이터 반환
     * @return  Data_IUII90M03_F
     */
    fun CF_getData(): Data_IUII90M03_F? {
        return data
    }
    // #############################################################################################
    //  Dialog 관련
    // #############################################################################################
    /**
     * 다이얼로그 팝업 후 EditText 포커스 이동
     * @param p_message     String
     * @param p_editText    final EditText
     */
    private fun showCustomDialogAndFocus(p_message: String, p_editText: EditText?) {
        val customDialog = CustomDialog(activity!!)
        customDialog.show()
        customDialog.CF_setTextContent(p_message)
        customDialog.CF_setSingleButtonText(resources.getString(R.string.btn_ok))
        customDialog.setOnDismissListener {
            if (p_editText != null) {
                CommonFunction.CF_showVirtualKeyboard(activity, p_editText)
            }
        }
    }
    // #############################################################################################
    //  보안키패드
    // #############################################################################################
    /**
     * 키패드 초기 세팅 함수
     */
    private fun initTransKeyPad() {
        val intent = getIntentParam(TransKeyActivity.mTK_TYPE_KEYPAD_NUMBER,
            TransKeyActivity.mTK_TYPE_TEXT_PASSWORD_EX,
            resources.getString(R.string.hint_resident_last_number),
            maxLengthOfResident2,
            "",
            5,
            60)
        mTransKeyCtrl!!.init(intent,
            view!!.findViewById<View>(R.id.keypadContainer) as FrameLayout,
            view!!.findViewById<View>(R.id.resident_relInput).findViewById<View>(R.id.editText) as EditText,
            view!!.findViewById<View>(R.id.resident_relInput).findViewById<View>(R.id.keyscroll) as HorizontalScrollView,
            view!!.findViewById<View>(R.id.resident_relInput).findViewById<View>(R.id.keylayout) as LinearLayout,
            view!!.findViewById<View>(R.id.resident_relInput).findViewById<View>(R.id.clearall) as ImageButton,
            view!!.findViewById<View>(R.id.keypadBallon) as RelativeLayout,
            null)
        //mTransKeyCtrl.setReArrangeKeapad(true);
        mTransKeyCtrl!!.setTransKeyListener(this)
        mTransKeyCtrl!!.setTransKeyListenerEx(this)
        mTransKeyCtrl!!.setTransKeyListenerCallback(this)

        // -- 캡쳐 방지 적용 (DRM적용: addFlags / 미적용: clearFlags)
        if (EnvConfig.mFlagShowLog) {
            this.activity?.window?.clearFlags(WindowManager.LayoutParams.FLAG_SECURE)
        } else {
            this.activity?.window?.addFlags(WindowManager.LayoutParams.FLAG_SECURE)
        }
        LogPrinter.CF_debug("!---- 보안키패드 초기화 완료")
    }

    /**
     * 보안키패드 설정 Intent 반환 함수
     *
     * @param keyPadType       키패드 타입
     * @param textType         보안 텍스트 타입
     * @param hint             입력 힌트
     * @param maxLength        입력 최대길이
     * @param maxLengthMessage 최대길이 도달시 보일 메시지
     * @param line3Padding     라인패딩
     * @param reduceRate       크기 조절 배율
     * @return Intent
     */
    private fun getIntentParam(keyPadType: Int, textType: Int, hint: String, maxLength: Int, maxLengthMessage: String, line3Padding: Int, reduceRate: Int): Intent {
        val newIntent = Intent(this.activity!!.applicationContext, TransKeyActivity::class.java)

        //public static final int mTK_TYPE_KEYPAD_NUMBER				//숫자전용
        //public static final int mTK_TYPE_KEYPAD_QWERTY_LOWER		    //소문자 쿼티
        //public static final int mTK_TYPE_KEYPAD_QWERTY_UPPER		    //대문자 쿼티
        //public static final int mTK_TYPE_KEYPAD_ABCD_LOWER			//소문자 순열자판
        //public static final int mTK_TYPE_KEYPAD_ABCD_UPPER			//대문자 순열자판
        //public static final int mTK_TYPE_KEYPAD_SYMBOL				//심벌자판
        newIntent.putExtra(TransKeyActivity.mTK_PARAM_KEYPAD_TYPE, keyPadType)

        //키보드가 입력되는 형태
        //TransKeyActivity.mTK_TYPE_TEXT_IMAGE - 보안 텍스트 입력
        //TransKeyActivity.mTK_TYPE_TEXT_PASSWORD - 패스워드 입력
        //TransKeyActivity.mTK_TYPE_TEXT_PASSWORD_EX - 마지막 글자 보여주는 패스워드 입력
        newIntent.putExtra(TransKeyActivity.mTK_PARAM_INPUT_TYPE, textType)

        //키패드입력화면의 입력 라벨
        //newIntent.putExtra(TransKeyActivity.mTK_PARAM_NAME_LABEL, label);
        newIntent.putExtra(TransKeyActivity.mTK_PARAM_DISABLE_SPACE, false)

        //newIntent.putExtra(TransKeyActivity.mTK_PARAM_HIDE_TIMER_DELAY, 5);

        //최대 입력값 설정
        newIntent.putExtra(TransKeyActivity.mTK_PARAM_INPUT_MAXLENGTH, maxLength)
        newIntent.putExtra(TransKeyActivity.mTK_PARAM_USE_ATM_MODE, false)
        //인터페이스 - maxLength시에 메시지 박스 보여주기. 기본은 메시지 안나옴.
        //newIntent.putExtra(TransKeyActivity.mTK_PARAM_MAX_LENGTH_MESSAGE, maxLengthMessage);

        // --> SERVER 연동
        newIntent.putExtra(TransKeyActivity.mTK_PARAM_CRYPT_TYPE, TransKeyActivity.mTK_TYPE_CRYPT_SERVER)
        newIntent.putExtra(TransKeyActivity.mTK_PARAM_SECURE_KEY, EnvConfig.mTransServerKey)

        // --> Local
        //newIntent.putExtra(TransKeyActivity.mTK_PARAM_CRYPT_TYPE, TransKeyActivity.mTK_TYPE_CRYPT_LOCAL);


        // 해당 Hint 메시지를 보여준다.
        newIntent.putExtra(TransKeyActivity.mTK_PARAM_SET_HINT, hint)

        // Hint 테스트 사이즈를 설정한다.(단위 dip, 0이면 디폴트 크기로 보여준다.)
        newIntent.putExtra(TransKeyActivity.mTK_PARAM_SET_HINT_TEXT_SIZE, 0)

        // 커서를 보여준다.
        newIntent.putExtra(TransKeyActivity.mTK_PARAM_SHOW_CURSOR, true)
        newIntent.putExtra(TransKeyActivity.mTK_PARAM_USE_CUSTOM_CURSOR, true)

        // 에디트 박스안의 글자 크기를 조절한다.
        newIntent.putExtra(TransKeyActivity.mTK_PARAM_EDIT_CHAR_REDUCE_RATE, reduceRate)

        // 심볼 변환 버튼을 비활성화 시킨다.
        newIntent.putExtra(TransKeyActivity.mTK_PARAM_DISABLE_SYMBOL, false)
        newIntent.putExtra(TransKeyActivity.mTK_PARAM_PREVENT_CAPTURE, true)

        //심볼 변환 버튼을 비활성화 시킬 경우 팝업 메시지를 설정한다.
        //newIntent.putExtra(TransKeyActivity.mTK_PARAM_DISABLE_SYMBOL_MESSAGE, "심볼키는 사용할 수 없습니다.");

        //////////////////////////////////////////////////////////////////////////////
        //인터페이스 - line3 padding 값 설정 가능 인자. 기본은 0 값이면 아무 설정 안했을 시 원래 transkey에서 제공하던 값을 제공한다..
        //newIntent.putExtra(TransKeyActivity.mTK_PARAM_KEYPAD_MARGIN, line3Padding);
        //newIntent.putExtra(TransKeyActivity.mTK_PARAM_KEYPAD_LEFT_RIGHT_MARGIN, 0);
        newIntent.putExtra(TransKeyActivity.mTK_PARAM_KEYPAD_HIGHEST_TOP_MARGIN, 2)
        newIntent.putExtra(TransKeyActivity.mTK_PARAM_USE_TALKBACK, true)
        newIntent.putExtra(TransKeyActivity.mTK_PARAM_SUPPORT_ACCESSIBILITY_SPEAK_PASSWORD, true)
        newIntent.putExtra(TransKeyActivity.mTK_PARAM_USE_SHIFT_OPTION, true)
        newIntent.putExtra(TransKeyActivity.mTK_PARAM_USE_CLEAR_BUTTON, false)
        newIntent.putExtra(TransKeyActivity.mTK_PARAM_USE_KEYPAD_ANIMATION, true)
        newIntent.putExtra(TransKeyActivity.mTK_PARAM_SAMEKEY_ENCRYPT_ENABLE, true)
        newIntent.putExtra(TransKeyActivity.mTK_PARAM_LANGUAGE, TransKeyActivity.mTK_Language_Korean)
        return newIntent
    }

    /**
     * 키패드 Show 함수
     */
    private fun showTransKeyPad() {
        var flagWaitTimeMilli = 0L
        if (editMobile!!.hasFocus()) {
            CommonFunction.CF_closeVirtualKeyboard(activity, editMobile!!.windowToken)
            editMobile!!.clearFocus()
            editMobile!!.isCursorVisible = false
            flagWaitTimeMilli = 120L
        }
        if (editCompany!!.hasFocus()) {
            CommonFunction.CF_closeVirtualKeyboard(activity, editCompany!!.windowToken)
            editCompany!!.clearFocus()
            editCompany!!.isCursorVisible = false
            flagWaitTimeMilli = 120L
        }
        if (editJob!!.hasFocus()) {
            CommonFunction.CF_closeVirtualKeyboard(activity, editJob!!.windowToken)
            editJob!!.clearFocus()
            editJob!!.isCursorVisible = false
            flagWaitTimeMilli = 120L
        }
        if (editName!!.hasFocus()) {
            CommonFunction.CF_closeVirtualKeyboard(activity, editName!!.windowToken)
            editName!!.clearFocus()
            editName!!.isCursorVisible = false
            flagWaitTimeMilli = 120L
        }
        if (edtResident1!!.hasFocus()) {
            CommonFunction.CF_closeVirtualKeyboard(activity, edtResident1!!.windowToken)
            edtResident1!!.clearFocus()
            flagWaitTimeMilli = 120L
        }
        if (editBeneficiaryName!!.hasFocus()) {
            CommonFunction.CF_closeVirtualKeyboard(activity, editBeneficiaryName!!.windowToken)
            editBeneficiaryName!!.clearFocus()
            editBeneficiaryName!!.isCursorVisible = false
            flagWaitTimeMilli = 120L
        }
        if (editAnotherParentname!!.hasFocus()) {
            CommonFunction.CF_closeVirtualKeyboard(activity, editAnotherParentname!!.windowToken)
            editAnotherParentname!!.clearFocus()
            editAnotherParentname!!.isCursorVisible = false
            flagWaitTimeMilli = 120L
        }
        Handler().postDelayed({
            relResident2!!.requestFocus()
            if (mTransKeyCtrl == null) {
                LogPrinter.CF_debug("!---- mTransKeyCtrl 이 널입니다.")
            } else {
                LogPrinter.CF_debug("!---- mTransKeyCtrl 이 널이 아닙니다.")
            }
            LogPrinter.CF_debug("!---- mTK_TYPE_KEYPAD_NUMBER : " + TransKeyActivity.mTK_TYPE_KEYPAD_NUMBER)
            try {
                mTransKeyCtrl!!.showKeypad(TransKeyActivity.mTK_TYPE_KEYPAD_NUMBER)
                scrollView?.fullScroll(ScrollView.FOCUS_DOWN) // 스크롤 하단이동
            } catch (e: NullPointerException) {
                e.message
            } catch (e: Exception) {
                e.message
            }
            if (CommonFunction.CF_checkAccessibilityTurnOn(this@IUII90M03_F.activity)) {
                relResident2!!.sendAccessibilityEvent(AccessibilityEvent.TYPE_VIEW_FOCUSED)
                relResident2!!.announceForAccessibility("보안키패드 표시됨")
            }
        }, flagWaitTimeMilli)
    }

    /**
     * 보안 키패드 show/hide 유무 반환
     * @return  boolean
     */
    fun CF_isShownTransKeyPad(): Boolean {
        return mTransKeyCtrl!!.isShown
    }

    /**
     * 보안키패드 close
     */
    fun CF_closeTransKeyPad() {
        finishTranskeypad(false)
    }

    /**
     * 보안키패드 숨김
     */
    private fun finishTranskeypad(p_flagFinishOption: Boolean) {
        if (mTransKeyCtrl!!.isShown) {
            mTransKeyCtrl!!.finishTransKey(p_flagFinishOption)

            // -------------------------------------------------------------------------------------
            //  접근성 announce
            // -------------------------------------------------------------------------------------
            if (CommonFunction.CF_checkAccessibilityTurnOn(activity)) {
                relResident2!!.announceForAccessibility("보안키패드 숨김.")
            }
        }
    }

    // #############################################################################################
    //  이벤트 리스너
    // #############################################################################################
    override fun onTouch(v: View, event: MotionEvent): Boolean {
        if (event.action == MotionEvent.ACTION_DOWN) {

            // -- 연락처 입력 EditText
            if (v.id == R.id.edtMobile) {
                finishTranskeypad(false)
                editMobile!!.isCursorVisible = true
                return false
            } else if (v.id == R.id.edtCompany) {
                finishTranskeypad(false)
                editCompany!!.isCursorVisible = true
                return false
            } else if (v.id == R.id.edtJob) {
                finishTranskeypad(false)
                editJob!!.isCursorVisible = true
                return false
            } else if (v.id == R.id.edit) {
                finishTranskeypad(false)
                editName!!.isCursorVisible = true
                return false
            } else if (v.id == R.id.edtResident_1) {
                finishTranskeypad(false)
                edtResident1!!.isCursorVisible = true
                scrollView?.fullScroll(ScrollView.FOCUS_DOWN) // 스크롤 하단이동
                return false
            } else if (v.id == R.id.editText || v.id == R.id.keyscroll || v.id == R.id.keylayout) {
                showTransKeyPad() // 보안키패드 활성화
                return true
            }
        }
        return false
    }

    override fun cancel(intent: Intent) {}

    override fun done(intent: Intent) {
        // -----------------------------------------------------------------------------------------
        //  접근성 포커스 이동
        // -----------------------------------------------------------------------------------------
        if (CommonFunction.CF_checkAccessibilityTurnOn(this@IUII90M03_F.activity)) {
            clearAllFocus()
            val linCompany = view!!.findViewById<LinearLayout>(R.id.labelTextCompany)
            val edtCompany = linCompany.findViewById<EditText>(R.id.edtCompany)
            edtCompany.isFocusableInTouchMode = true
            edtCompany.requestFocus()
            edtCompany.isFocusableInTouchMode = false
        }
    }

    override fun input(i: Int) {
        if (mTransKeyCtrl!!.inputLength >= maxLengthOfResident2) {
            finishTranskeypad(true)
        }
    }

    override fun minTextSizeCallback() {}
    override fun maxTextSizeCallback() {}

    // #############################################################################################
    //  Http 관련
    // #############################################################################################
    override fun handleMessage(p_message: Message) {
        if (mActivity != null && !mActivity!!.isDestroyed) {
            mActivity?.CF_dismissProgressDialog()
            when (p_message.what) {
                HANDLERJOB_GET_USERINFO -> try {
                    httpRes_userInfo(JSONObject(p_message.obj as String))
                } catch (e: JSONException) {
                    LogPrinter.CF_debug(resources.getString(R.string.log_json_exception))
                    mActivity?.CF_OnError(resources.getString(R.string.dlg_error_server))
                }
                HANDLERJOB_GET_CHILDINFO -> try {
                    httpRes_childInfo(JSONObject(p_message.obj as String))
                } catch (e: JSONException) {
                    LogPrinter.CF_debug(resources.getString(R.string.log_json_exception))
                    mActivity?.CF_OnError(resources.getString(R.string.dlg_error_server))
                }
                HANDLERJOB_ERROR_GET_USERINFO, HANDLERJOB_ERROR_GET_CHILDINFO -> mActivity?.CF_OnError(p_message.obj as String)
                else -> {
                }
            }
        }
    }

    /**
     * 청구자 기본정보 요청 함수
     */
    fun httpReq_userInfo() {
        LogPrinter.CF_debug("!----------------------------------------------------------")
        LogPrinter.CF_debug("!-- IUII90M03_F.httpReq_userInfo()")
        LogPrinter.CF_debug("!----------------------------------------------------------")

        // --<1> (고객정보요청) 이름 정보가 없는 경우에만 요청
        if (TextUtils.isEmpty(textName!!.text.toString().trim())) {
            mActivity?.CF_showProgressDialog()
            val builder = Uri.Builder()
            builder.appendQueryParameter("csno", mActivity?.CF_getData()!!.CF_getS_rctr_csno())
            builder.appendQueryParameter("tempKey", SharedPreferencesFunc.getWebTempKey(Objects.requireNonNull(activity)))
            HttpConnections.sendPostData(
                EnvConfig.host_url + EnvConfig.URL_CLAIM_CHILD_CERT_USER_INFO_INQ,
                builder.build().encodedQuery,
                handler,
                HANDLERJOB_GET_USERINFO,
                HANDLERJOB_ERROR_GET_USERINFO)
        }
    }

    /**
     * (Https result)청구자 기본정보 요청결과 처리 함수
     * @param p_jsonObject  JSONObject
     */
    @Throws(JSONException::class)
    private fun httpRes_userInfo(p_jsonObject: JSONObject) {
        LogPrinter.CF_debug("!----------------------------------------------------------")
        LogPrinter.CF_debug("!-- IUII90M03_F.httpRes_userInfo()")
        LogPrinter.CF_debug("!----------------------------------------------------------")

        // 프로그레스 다이얼로그 dismiss
        val jsonKey_errorCode = "errCode"
        val jsonKey_data = "data"
        val tmp_errorCode: String
        if (p_jsonObject.has(jsonKey_errorCode)) {
            tmp_errorCode = p_jsonObject.getString(jsonKey_errorCode)

            // --<2> (조회 실패) 고객정보가 없는 경우
            if (tmp_errorCode == "ERRIUII12M00001") {
                mActivity?.CF_OnError(resources.getString(R.string.dlg_error_erriuii12m00001))
            } else if (p_jsonObject.has(jsonKey_data)) {
                val tmp_jsonData = p_jsonObject.getJSONObject(jsonKey_data)

                // -- 청구자 데이터 세팅
                setUserInfo(tmp_jsonData)

                // -- EditText 포커스 영향으로 스크롤 변경되는 것을 막기위함으로 강제 포커스 세팅
                textName!!.requestFocus()
                textName!!.isFocusableInTouchMode = true
                editMobile!!.isFocusableInTouchMode = true
                editCompany!!.isFocusableInTouchMode = true
                editJob!!.isFocusableInTouchMode = true
                editName!!.isFocusableInTouchMode = true

                // -- 시각접근성 처리
                if (CommonFunction.CF_checkAccessibilityTurnOn(Objects.requireNonNull(activity))) {
                    mActivity?.CF_requestFocusIndicator()
                }
            } else {
                mActivity?.CF_OnError(resources.getString(R.string.dlg_error_server_2))
            }
        } else {
            mActivity?.CF_OnError(resources.getString(R.string.dlg_error_server_1))
        }
    }

    /**
     * (CF_requestUserInfo() 결과처리) Https 통신을 통해 JsonData를 수신하여 사용자(청구자) 정보 세팅
     * <pre>
     * **Histroy:**
     * 2020-02-13    노지민     : 직장명, 하시는일 주석처리
    </pre> *
     * @param p_jsonObject      JSONObject
     */
    @SuppressLint("SetTextI18n")
    @Throws(JSONException::class)
    private fun setUserInfo(p_jsonObject: JSONObject) {
        LogPrinter.CF_debug("!----------------------------------------------------------")
        LogPrinter.CF_debug("!-- IUII90M03_F.setUserInfo()")
        LogPrinter.CF_debug("!----------------------------------------------------------")

        val jsonKey_s_rnno = "s_rnno" // 주민등록변환 --> 생년월일 앞 6자리
        //final String jsonKey_s_csno           = "s_csno";                     // 고객번호
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
        //        final String jsonKey_s_ofce_nm          = "s_ofce_nm";                  // 직장명
//        final String jsonKey_s_ocpt_nm          = "s_ocpt_nm";                  // 하시는 일

        // 청구자 최근청구계좌
        val jsonKey_s_fnis_code = "s_fnis_code" // 최근청구계좌 은행코드
        val jsonKey_s_fnis_nm = "s_fnis_nm" // 최근청구계좌 은행명
        val jsonKey_s_acno = "s_acno" // 최근청구계좌 계좌번호
        val jsonKey_s_dpow_nm = "s_dpow_nm" // 최근청구계좌 예금주

        // 청구자 보유 우체국 계좌 리스트
        val jsonKey_accList = "accList" // 고객 우체국 계좌 리스트
        //------------------------------------------------------------------------------------------

        // 최근청구계좌 : JsonObject 파싱 후 Activity 변수에서 SET
        if (p_jsonObject.has(jsonKey_s_fnis_code)) {
            LogPrinter.CF_debug("!---- 최근청구계좌(은행코드)::::" + p_jsonObject.getString(jsonKey_s_fnis_code))
            mActivity?.CF_setsLastFnisCode(p_jsonObject.getString(jsonKey_s_fnis_code))
        }
        if (p_jsonObject.has(jsonKey_s_fnis_nm)) {
            LogPrinter.CF_debug("!---- 최근청구계좌(은행명)::::::" + p_jsonObject.getString(jsonKey_s_fnis_nm))
            mActivity?.CF_setsLastFnisNm(p_jsonObject.getString(jsonKey_s_fnis_nm))
        }
        if (p_jsonObject.has(jsonKey_s_acno)) {
            LogPrinter.CF_debug("!---- 최근청구계좌(계좌번호)::::" + p_jsonObject.getString(jsonKey_s_acno))
            mActivity?.CF_setsLastAcno(p_jsonObject.getString(jsonKey_s_acno))
        }
        if (p_jsonObject.has(jsonKey_s_dpow_nm)) {
            LogPrinter.CF_debug("!---- 최근청구계좌(예금주)::::::" + p_jsonObject.getString(jsonKey_s_dpow_nm))
            mActivity?.CF_setsLastDpowNm(p_jsonObject.getString(jsonKey_s_dpow_nm))
        }

        // -- 청구자 보유 우체국 계좌 리스트 : JsonArray 파싱 후 Activity 변수에서 SET
        if (p_jsonObject.has(jsonKey_accList)) {
            val tmp_jsonArray_accList = p_jsonObject.getJSONArray(jsonKey_accList)
            if (tmp_jsonArray_accList.length() > 0) {
                LogPrinter.CF_debug("!--<> 우체국계좌리스트 존재함")
                val tmp_accList = arrayOfNulls<String>(tmp_jsonArray_accList.length())
                for (i in 0 until tmp_jsonArray_accList.length()) {
                    tmp_accList[i] = tmp_jsonArray_accList.getJSONObject(i).getString("s_acno")
                    LogPrinter.CF_debug("!---- [" + i + "] 우체국 계좌번호: " + tmp_accList[i])
                }
                LogPrinter.CF_debug("!---- 계좌리스트 변수 길이 : " + tmp_accList.size)
                mActivity?.CF_setAccList(tmp_accList)
            }
        }

        // 청구자 기본정보
        val tmp_birth = p_jsonObject.getString(jsonKey_s_rnno)
        val tmp_name = p_jsonObject.getString(jsonKey_s_cust_krnm)
        val tmp_mobile = p_jsonObject.getString(jsonKey_s_mobl_no_area_dvsn) + p_jsonObject.getString(jsonKey_s_mobl_no_brn_code) + p_jsonObject.getString(jsonKey_s_mobl_no_sqnu)
        //        String tmp_company          = p_jsonObject.getString(jsonKey_s_ofce_nm);
        val tmp_psno_1 = p_jsonObject.getString(jsonKey_s_psno1)
        val tmp_addr_1 = p_jsonObject.getString(jsonKey_s_bass_addr_nm1)
        val tmp_addr_detail_1 = p_jsonObject.getString(jsonKey_s_dtls_addr_nm1)
        val tmp_psno_2 = p_jsonObject.getString(jsonKey_s_psno2)
        val tmp_addr_2 = p_jsonObject.getString(jsonKey_s_bass_addr_nm2)
        val tmp_addr_detail_2 = p_jsonObject.getString(jsonKey_s_dtls_addr_nm2)

        // 하시는일
//        String tmp_job              = "";
//        if(p_jsonObject.has(jsonKey_s_ocpt_nm)){
//            tmp_job = p_jsonObject.getString(jsonKey_s_ocpt_nm);
//        }
        textName!!.text = tmp_name // 이름
        editMobile!!.setText(tmp_mobile) // 휴대폰번호
        s_mobl_no = tmp_mobile

        // -- 직장명/하시는일              // 2020-02-13 사용안함
        // editCompany.setText(tmp_company);   // 직장명
        // editJob.setText(tmp_job);           // 하시는일(직업)
        editCompany!!.setText("")
        editJob!!.setText("")

        // -- PhoneNumber 포맷팅 적용이 안되는 경우가 있기에 확인 후 hypen 추가
        var tmp_edtMobile = editMobile!!.text.toString().trim()

        // 01X-XXX(X)-XXXX 포맷팅이 아니면서 && hypen 제거한 전화번호 형식이 맞지 않는 경우
        if (!Regex.CF_MacherMobile(tmp_edtMobile) && Regex.CF_MacherMobileNoHypen(tmp_edtMobile.replace("-", ""))) {
            tmp_edtMobile = tmp_edtMobile.replace("-", "")
            editMobile!!.setText(tmp_edtMobile.substring(0, 3) + "-" +
                    tmp_edtMobile.substring(3, tmp_edtMobile.length - 4) + "-" +
                    tmp_edtMobile.substring(tmp_edtMobile.length - 4))
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
            s_psno = tmp_psno_1
            s_addr1 = tmp_addr_1
            s_addr2 = tmp_addr_detail_1
        } else if (!TextUtils.isEmpty(tmp_psno_2) && !TextUtils.isEmpty(tmp_addr_2)) {
            setAddressText(tmp_psno_2, tmp_addr_2, tmp_addr_detail_2)
            s_psno = tmp_psno_2
            s_addr1 = tmp_addr_2
            s_addr2 = tmp_addr_detail_2
        } else {
            textAddress!!.text = ""
            s_psno = ""
            s_addr1 = ""
            s_addr2 = ""
        }
    }

    /**
     * 자녀(피보험자) 고객번호 요청 함수 TODO : 미오픈
     */
    fun httpReq_childInfo() {
        mActivity?.CF_showProgressDialog()
        val builder = Uri.Builder()
        builder.appendQueryParameter("s_csno", mActivity?.CF_getData()!!.CF_getS_rctr_csno()) // 청구자고객번호
        builder.appendQueryParameter("s_name", textName!!.text.toString().trim()) // 청구자명
        if (btnGroupRelation!!.CF_getCheckdButtonFirstIndex() == 0) {
            builder.appendQueryParameter("s_bnfc_nm", editBeneficiaryName!!.text.toString().trim()) // 수익자명 (입력)
        } else {
            builder.appendQueryParameter("s_bnfc_nm", textName!!.text.toString().trim()) // 수익자명 (=청구자명)
        }
        builder.appendQueryParameter("reltDvsn", EnvConfig.reqChildRelationCode[btnGroupRelation!!.CF_getCheckdButtonFirstIndex()]) // 청구자의 계약관계
        builder.appendQueryParameter("childName", editName!!.text.toString().trim()) // 자녀명
        builder.appendQueryParameter("childRrno1", edtResident1!!.text.toString().trim()) // 자녀실명번호 앞6자리
        builder.appendQueryParameter("childRrno2_enc", mTransKeyCtrl!!.cipherDataEx) // 자녀실명번호 뒤7자리 암호화

        //TODO 휴대폰인증시 고객정보 조회하는 부분에서 서버쪽 수정 필요함
        // 2020-01-23    노지민     : 자녀보험금청구에 휴대폰인증 추가 (고객정보조회하는부분에서 서버작업필요함)
        HttpConnections.sendPostData(
            EnvConfig.host_url + EnvConfig.URL_CLAIM_CHILD_MOBILE_USER_INFO_INQ,
            builder.build().encodedQuery,
            handler,
            HANDLERJOB_GET_CHILDINFO,
            HANDLERJOB_ERROR_GET_CHILDINFO)
    }

    /**
     * 자녀 기본정보 요청결과 처리 함수
     * @param p_jsonObject  JSONObject
     */
    @Throws(JSONException::class)
    private fun httpRes_childInfo(p_jsonObject: JSONObject) {
        val jsonKey_errorCode = "errCode"
        val jsonKey_data = "data"
        val jsonKey_s_cttr_isrt_smns_yn = "s_cttr_isrt_smns_yn" // 피보험자,수익자 일치 여부 "Y" or "N"
        val tmp_errorCode: String
        if (p_jsonObject.has(jsonKey_errorCode)) {
            tmp_errorCode = p_jsonObject.getString(jsonKey_errorCode)
            // ERRIUII12M020001 : 요청데이터누락
            // ERRIUII12M020002 : 시스템장애(프로그램오류 등)
            // ERRIUII12M020003 : 654458 전문 오류
            // ERRIUII12M020004 : 654534 전문 오류
            // ERRIUII12M020005 : 654458 고객명 상이
            // ERRIUII12M020006 : 654534 고객명 상이
            if (tmp_errorCode == "ERRIUII12M02001") {            // 요청데이터 누락
                showCustomDialog(resources.getString(R.string.dlg_error_erriuii12m02001), relChildInfoView!!)
            } else if (tmp_errorCode == "ERRIUII12M02002") {        // 잘못된 실명번호
                showCustomDialog(resources.getString(R.string.dlg_error_erriuii12m02001), relChildInfoView!!)
            } else if (tmp_errorCode == "ERRIUII12M02003") {        // 미성년자녀가 아닙니다.
                showCustomDialog(resources.getString(R.string.dlg_error_erriuii12m02002), relChildInfoView!!)
            } else if (p_jsonObject.has(jsonKey_data)) {
                // -- 자녀 데이터 세팅
                val tmp_jsonData = p_jsonObject.getJSONObject(jsonKey_data)

                // ---------------------------------------------------------------------------------
                // -- 고객의 보험 계약 정보 확인 : 수익자이면서 피보험자인 경우만 청구 가능
                // ---------------------------------------------------------------------------------
                if (tmp_jsonData.has(jsonKey_s_cttr_isrt_smns_yn)) {
                    val tmp_strAvailable = tmp_jsonData.getString(jsonKey_s_cttr_isrt_smns_yn)

                    // --<> "E" : 전문 에러 : 고객정보조회 실패
                    if (tmp_strAvailable.toLowerCase(Locale.getDefault()) == "e") {
                        showCustomDialog(resources.getString(R.string.dlg_error_erriuii12m02001), relChildInfoView!!)
                    } else if (tmp_strAvailable.toLowerCase(Locale.getDefault()) == "s") {
                        showCustomDialog(resources.getString(R.string.dlg_error_erriuii12m00002), relChildInfoView!!)
                    } else if (tmp_strAvailable.toLowerCase(Locale.getDefault()) == "y") {
                        val jsonKey_p_csno = "bnfc_csno" // 수익자고객번호
                        val jsonKey_s_name = "s_name" // 자녀명
                        val jsonKey_s_csno = "s_csno" // 자녀고객번호
                        val bnfc_csno = tmp_jsonData.getString(jsonKey_p_csno)
                        val s_name = tmp_jsonData.getString(jsonKey_s_name)
                        val s_csno = tmp_jsonData.getString(jsonKey_s_csno)
                        if ("" == bnfc_csno || "" == s_csno || "" == s_name) {
                            showCustomDialog(resources.getString(R.string.dlg_error_erriuii12m02001), relChildInfoView!!)
                        } else {
                            LogPrinter.CF_debug("!---- 수익자 고객번호 :$bnfc_csno")
                            LogPrinter.CF_debug("!---- 자녀 고객번호   :$s_csno")
                            LogPrinter.CF_debug("!---- 자녀 이름       :$s_name")

                            // -- 청구 데이터 세팅
                            setChildInfo(bnfc_csno, s_csno, s_name)
                            btnNext?.isEnabled = false

                            //LogPrinter.CF_debug("3단계 -> 4단계 부담보 : " + data.CF_getSmbrPsblYn());
                            //tmp_data.CF_setB_smbr_psbl("Y".equals(data.CF_getSmbrPsblYn()) ? true : false);      // 부담보내역 추가
                            //EventBus.getDefault().post(tmp_data);

                            // -----------------------------------------------------------------------------
                            //  다음페이지 이동 시간 Delay
                            // -----------------------------------------------------------------------------
                            Handler().postDelayed({
                                mActivity?.CF_showNextPage()
                                mActivity?.CF_setVisibleStepIndicator(true)
                            }, 120L)
                        }
                    } else {
                        showCustomDialog(resources.getString(R.string.dlg_error_erriuii12m02003), relChildInfoView!!)
                    }
                } else {
                    // 운영서버 미적용으로 임시 팝업
                    mActivity?.CF_OnError(resources.getString(R.string.dlg_error_erriuii12m00003))
                }
            } else {
                mActivity?.CF_OnError(resources.getString(R.string.dlg_error_server_2))
            }
        } else {
            mActivity?.CF_OnError(resources.getString(R.string.dlg_error_server_1))
        }
    }

    /**
     * 청구데이터(청구자 및 자녀) 세팅
     * @param p_csno    String
     * @param s_csno    String
     * @param s_name    String
     * NJM_20191002 휴대폰번호 자르는 방식 변경
     */
    private fun setChildInfo(p_csno: String, s_csno: String, s_name: String) {
        LogPrinter.CF_debug("!----------------------------------------------------------")
        LogPrinter.CF_debug("!-- IUII90M03_F.setChildInfo()")
        LogPrinter.CF_debug("!----------------------------------------------------------")
        saveData()
        val tmp_activityData = mActivity?.CF_getData()

        // -- 휴대폰번호 : 휴대폰번호 자릿수로 자르기
        val tmp_mobile = editMobile!!.text.toString().trim().replace("-", "")
        LogPrinter.CF_debug("!---- tmp_mobile(dash제거) :$tmp_mobile")
        tmp_activityData!!.CF_setS_mobl_no_1(tmp_mobile.substring(0, 3))
        tmp_activityData.CF_setS_mobl_no_2(tmp_mobile.substring(3, tmp_mobile.length - 4))
        tmp_activityData.CF_setS_mobl_no_3(tmp_mobile.substring(tmp_mobile.length - 4))
        LogPrinter.CF_debug("!---- 저장된 휴대폰번호" + tmp_activityData.CF_getS_mobl_no_1() + "/" + tmp_activityData.CF_getS_mobl_no_2() + "/" + tmp_activityData.CF_getS_mobl_no_3())
        tmp_activityData.CF_setS_infm_serv_rqst_ym(if (switchingNoti!!.CF_getCheckState() > 0) "Y" else "N")
        tmp_activityData.CF_setS_cfce_nm(editCompany!!.text.toString().trim())
        tmp_activityData.CF_setS_ocpn_nm(editJob!!.text.toString().trim())
        tmp_activityData.CF_setS_psno(data!!.CF_getZipNo())
        tmp_activityData.CF_setS_bass_addr_nm(data!!.CF_getAddress())
        tmp_activityData.CF_setS_dtls_addr_nm(data!!.CF_getAddressDetail())

        // 자녀보험금청구
        tmp_activityData.CF_setS_rctr_csno(mActivity?.CF_getData()!!.CF_getS_rctr_csno()) // 청구자의 고객번호
        tmp_activityData.CF_setS_rctr_relt_dvsn(EnvConfig.reqChildRelationCode[data!!.CF_getRelationType()]) // 청구자의 계약관계 (30:계약자, 42:입원(장해)수익자) !!
        tmp_activityData.CF_setS_rctr_type(EnvConfig.reqChildParentTypeCode[data!!.CF_getParentType()]) // 청구자의 부모유형 !!

        // --<> (청구자 == 수익자) 청구자가 수익자인경우
        if (data!!.CF_getRelationType() == 1) {
            tmp_activityData.CF_setS_bnfc_nm(data!!.CF_getName()) // [수익자] <-- 청구자명
            tmp_activityData.CF_setS_bnfc_csno(mActivity?.CF_getData()!!.CF_getS_rctr_csno()) // 청구자고객번호를 수익자 고객번호로
        }
        else { // --<> (청구자 != 수익자) 청구자가 계약자인경우
            tmp_activityData.CF_setS_bnfc_nm(data!!.CF_getBeneficiaryName()) // [수익자] <-- 입력받은 수익자명
            tmp_activityData.CF_setS_bnfc_csno(p_csno) // 자녀정보조회시 받은 수익자 고객번호
        }

        tmp_activityData.CF_setS_pipa_nm_1(data!!.CF_getName()) // 친권자1
        if (data!!.CF_getAgreeType() == 0) {      // 공동친권일경우
            tmp_activityData.CF_setS_rel_agree_type("1") // 친권자동의유형 (1:공동친권)
            tmp_activityData.CF_setS_pipa_nm_2(data!!.CF_getAnotherParentName()) // 친권자2
        } else {
            tmp_activityData.CF_setS_rel_agree_type(EnvConfig.reqChildParentAloneReasonCode[data!!.CF_getAgreeTypeReason()]) // 친권자동의유형(2:이혼, 3:사별, 4:기타)
        }
        tmp_activityData.CF_setS_acdp_csno(s_csno) // 사고자(피보험자) 고객번호
        tmp_activityData.CF_setS_acdp_nm(s_name) // 피보험자명
        tmp_activityData.CF_setS_acdp_rrno1(data!!.CF_getChildRrno1()) // 피보험자 실명번호 앞자리
        tmp_activityData.CF_setS_acdp_rrno2_enc(data!!.CF_getChildRrno2_enc()) // 피보험자 실명번호 뒷자리 암호화
    }
}