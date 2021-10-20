package com.epost.insu.fragment

import android.annotation.SuppressLint
import android.app.Activity
import android.content.DialogInterface
import android.content.Intent
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
import android.widget.*
import android.widget.AdapterView.OnItemSelectedListener
import com.epost.insu.EnvConfig
import com.epost.insu.EnvConfig.AuthDvsn
import com.epost.insu.EnvConstant
import com.epost.insu.R
import com.epost.insu.activity.IUII31M00
import com.epost.insu.activity.IUII33M00_P
import com.epost.insu.activity.auth.*
import com.epost.insu.common.CommonFunction
import com.epost.insu.common.CustomSQLiteFunction
import com.epost.insu.common.LogPrinter
import com.epost.insu.data.Data_IUII10M00
import com.epost.insu.data.Data_IUII10M07_F
import com.epost.insu.dialog.CustomDialog
import com.epost.insu.fido.Fido2Constant
import com.epost.insu.network.HttpConnections
import com.epost.insu.network.WeakReferenceHandler
import com.epost.insu.network.WeakReferenceHandler.ObjectHandlerMessage
import org.json.JSONException
import org.json.JSONObject
import java.util.*

/**
 * 보험금청구 > 본인청구 > 7단계. 계좌정보확인 및 추천국/추천인 선택
 * @since     :
 * @version   : 1.6
 * @author    : LSH
 * @see
 * <pre>
 * - 보험금지급 받을 계좌정보를 입력받는다.
 * - 추천국/추천인 정보를 입력(선택) 받는다.
 * ======================================================================
 * 0.0.0    LSM_20170831    최초 등록
 * 0.0.0    LSM_20171127    계좌번호입력 보안키패드=>기본키패드
 * 0.0.0    NJM_20190529    추천국/추천인 선택 추가
 * 0.0.0    NJM_20190919    최근 청구 계좌 & 우체국 계좌 조회 후 화면에 초기세팅
 * 0.0.0    NJM_20191011    우체국 계좌 선택시 포커스온(키패드팝업) 제거
 * 0.0.0    NJM_20200122    공통 인증유형/청구유형 추가에 따른 로직수정
 * 1.5.3    NJM_20210421    [subUrl 공통파일로 변경]
 * 1.5.4    NJM_20210504    [간편인증 전자서명 추가] 간편인증 전자서명 로직 추가
 * 1.5.4    NJM_20210506    [IUFC34M00 임시고객번호 삭제]
 * 1.5.8    NJM_20210630    [금융인증서 도입] 전자서명 추가
 * 1.6.1    NJM_20210728    [PASS인증서 도입]
 * 1.6.2    NJM_20210802    [2021년 대우 취약점] 2차본 : 부적절한 예외 처리
 * =======================================================================
 * copyright : (주)프로니스정보기술 | http://www.phronis.co.kr
 * </pre>
 */
class IUII10M07_F : IUII10M00_FD(), ObjectHandlerMessage {
    private val HANDLERJOB_CHECK: Int = 0 // 고객이 입력한계좌 정합성 체크
    private val HANDLERJOB_ERROR_CHECK: Int = 1 // 고객이 입력한계좌 정합성 체크 에러
    private val maxlengthBankAccount: Int = 14
    private var aData: Data_IUII10M00? = null // Activity Data

    private var data: Data_IUII10M07_F? = null
    private var handler: WeakReferenceHandler? = null // 핸들러

    private var textBankOwner: TextView? = null // 예금주
    private var textBankName: TextView? = null // 은행명

    // 계좌번호 (단일)
    var lin_BankAccount: LinearLayout? = null
    private var lbl_BankAccount: TextView? = null // 계좌번호 타이틀
    private var edt_BankAccount: EditText? = null // 계좌번호 입력란

    // 계좌번호 (우체국 리스트)
    var lin_BankAccountList: LinearLayout? = null
    private var spn_BankAccount: Spinner? = null // 계좌리스트(우체국)

    private var accountVisibilityDivision: String = "one" // 계좌필드 종류지정("one":단일, "list":스피너)
    private var authBankAccount: String = "" // 인증받을(인증된) 계좌번호

    // 추천국/추천인
    private var textRecommDepart: TextView? = null
    private var textRecommPerson: TextView? = null
    private var srchDepartName: String? = null
    private var srchPersonName: String? = null // 추천국/추천인 이름 검색어 임시저장

    /**
     * NJM_20191011 우체국 계좌 선택시 포커스온(키패드팝업) 제거
     */
    override fun onActivityResult(p_requestCode: Int, p_resultCode: Int, p_data: Intent?) {
        super.onActivityResult(p_requestCode, p_resultCode, p_data)
        LogPrinter.CF_debug("!-----------------------------------------------------------")
        LogPrinter.CF_debug("!-- IUII10M07_F.onActivityResult()")
        LogPrinter.CF_debug("!-----------------------------------------------------------")

        //------------------------------------------------------------------------------------------
        // -- 금융기관 선택 Activity 완료
        //------------------------------------------------------------------------------------------
        if (p_requestCode == EnvConfig.REQUESTCODE_ACTIVITY_CHOICE_BANK && p_resultCode == Activity.RESULT_OK) {
            LogPrinter.CF_debug("!--<1> 금융기관 선택 Activity 완료")

            if ((p_data != null) && p_data.hasExtra("code") && p_data.hasExtra("name")) {
                LogPrinter.CF_debug("!--<2> data수신 정상")

                val tmp_bankCode: String? = p_data.extras?.getString("code")
                val tmp_bankName: String? = p_data.extras?.getString("name")

                // -- 고객이 선택한 금융기관 이름 & 코드 세팅
                // 우체국 선택 & 우체국계좌 보유 : 스피너로 구성
                if (("071" == tmp_bankCode) && data!!.bankPostAccountArray.isNotEmpty()) {
                    setAccountVisibility("list")
                } else {
                    setAccountVisibility("one")

                    //  -- 접근성 On : 접근성 포커스 요청 (계좌번호 입력 EditText 포커스 이동)
                    Handler().postDelayed(object : Runnable {
                        override fun run() {
                            CommonFunction.CF_showVirtualKeyboard(activity, edt_BankAccount)
                            if (CommonFunction.CF_checkAccessibilityTurnOn(Objects.requireNonNull(activity))) {
                                edt_BankAccount!!.sendAccessibilityEvent(AccessibilityEvent.TYPE_VIEW_FOCUSED)
                            }
                        }
                    }, 250)
                }
                textBankName?.text = tmp_bankName
                textBankName?.tag = tmp_bankCode
                textBankName?.contentDescription = "선택창, " + textBankName!!.getText() + "선택됨, 선택하시려면 두번 누르세요."
            }
        }
        else if (p_requestCode == EnvConfig.REQUESTCODE_ACTIVITY_IUII33M00 && p_resultCode == Activity.RESULT_OK) {
            LogPrinter.CF_debug("!--<1> 추천국/추천인 선택 Activity 완료")
            if (p_data != null) {
                // -- Intent 데이터 GET
                val tmp_departCode: String? = p_data.extras?.getString("departCode") // 추천국 코드
                val tmp_departName: String? = p_data.extras!!.getString("departName") // 추천국 이름
                val tmp_personRemnNo: String? = p_data.extras!!.getString("personRemnNo") // 추천인 번호
                val tmp_personName: String? = p_data.extras!!.getString("personName") // 추천인 이름
                srchDepartName = p_data.extras!!.getString("srchDepartName") // 추천국 이름 검색어
                srchPersonName = p_data.extras!!.getString("srchPersonName") // 추천인 이름 검색어

                // -- 데이터 세팅
                // ---- 고객이 선택한 추천국 이름 & 코드 세팅
                textRecommDepart?.text = tmp_departName
                textRecommDepart?.contentDescription = "선택창, " + textRecommDepart!!.text + "선택됨, 선택하시려면 두번 누르세요."
                textRecommDepart?.tag = tmp_departCode

                // ---- 고객이 선택한 추천인 이름 & 코드 세팅
                textRecommPerson?.text = tmp_personName
                textRecommPerson?.contentDescription = "선택창, " + textRecommPerson!!.text + "선택됨, 선택하시려면 두번 누르세요."
                textRecommPerson?.tag = tmp_personRemnNo

                LogPrinter.CF_debug("!---- tmp_departName   : $tmp_departName")
                LogPrinter.CF_debug("!---- tmp_departCode   : $tmp_departCode")
                LogPrinter.CF_debug("!---- tmp_personName   : $tmp_personName")
                LogPrinter.CF_debug("!---- tmp_personRemnNo : $tmp_personRemnNo")
            }
        }
        else if (p_requestCode == EnvConfig.REQUESTCODE_ACTIVITY_IUPC80M00 && p_resultCode == Activity.RESULT_OK) {
            mActivity!!.CF_showNextPage()
        }
        else if (p_requestCode == EnvConfig.REQUESTCODE_ACTIVITY_IUPC90M00 && p_resultCode == Activity.RESULT_OK) {
            mActivity!!.CF_showNextPage()
        }
        else if (p_requestCode == EnvConfig.REQUESTCODE_ACTIVITY_IUCOK0M00 && p_resultCode == Activity.RESULT_OK) {
            mActivity!!.CF_showNextPage()
        }
        else if (p_requestCode == EnvConfig.REQUESTCODE_ACTIVITY_IUFC00M00 && p_resultCode == Activity.RESULT_OK) {
            mActivity!!.CF_showNextPage()
        }
        else if (p_requestCode == EnvConfig.REQUESTCODE_ACTIVITY_IUPC10M00 && p_resultCode == Activity.RESULT_OK) {
            mActivity!!.CF_showNextPage()
        }
        else if (p_requestCode == EnvConfig.REQUESTCODE_ACTIVITY_IUPC20M00 && p_resultCode == Activity.RESULT_OK) {
            mActivity!!.CF_showNextPage()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        LogPrinter.CF_debug("!-----------------------------------------------------------")
        LogPrinter.CF_debug("!-- IUII10M07_F.onCreate()")
        LogPrinter.CF_debug("!-----------------------------------------------------------")

        data = Data_IUII10M07_F()
        handler = WeakReferenceHandler(this)
    }

    @SuppressLint("InflateParams")
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.iuii10m07_f, null)
    }

    /**
     * 화면 복구(전환이나 폰트변경) 일때 Activity data 복구보다 Fragment data 복구가 먼저 발생하여
     * Activity Data는 복구전이라(null상태는 아님) 사용하지 못함.
     * - 최초는 Activity Data 사용하여 Fragment Data에 저장하고
     * - 이후에는 p_bundle(Fragment Data) 사용
     * @param p_bundle  Bundle
     */
    override fun onActivityCreated(p_bundle: Bundle?) {
        super.onActivityCreated(p_bundle)
        LogPrinter.CF_debug("!-----------------------------------------------------------")
        LogPrinter.CF_debug("!-- IUII10M07_F.onActivityCreated()")
        LogPrinter.CF_debug("!-----------------------------------------------------------")
        LogPrinter.CF_debug("!---- (7단계) 인증구분 :" + mActivity!!.CF_getAuthDvsn())

        aData = mActivity!!.CF_getData()

        // -----------------------------------------------------------------------------------------
        // -- 초기 필드 셋팅
        // 화면 복구(전환이나 폰트변경) 일때  Activity data 복구보다 Fragment data 복구가 먼저 발생하여
        // Activity data는 null이라 사용하지 못함 최초 이후에는 p_bundle 사용하여 복구하여야함
        // -----------------------------------------------------------------------------------------
        if (p_bundle != null && p_bundle.containsKey("data")) {
            LogPrinter.CF_debug("!--<> p_bundle data 있음 (data 덮어씀)")
            data = p_bundle.getParcelable("data")
        }
        else {
            LogPrinter.CF_debug("!--<> p_bundle data NULL (Activity Data Set)")
            data?.bankOwner = aData!!.CF_getLoginUserName()
            data?.bankName = mActivity!!.CF_getsLastFnisNm().trim()
            data?.bankCode = mActivity!!.CF_getsLastFnisCode()
            data?.bankPostAccountArray = mActivity!!.CF_getAccList()

            // --<> (우체국 & 우체국계좌보유) : 스피너로 구성
            if (("071" == mActivity!!.CF_getsLastFnisCode()) && data!!.bankPostAccountArray.isNotEmpty()) {
                data?.bankPostAccount = mActivity!!.CF_getsLastAcno()
                data?.accountVisibilityDivision = "list"
            } else {
                data?.bankAccount = mActivity!!.CF_getsLastAcno()
                data?.accountVisibilityDivision = "one"
            }
        }
        data!!.logPrint()

        // -- UI 생성 및 세팅 함수
        setUIControl()

        // -- 데이터 복구
        restoreData()
    }

    /**
     * UI 생성 및 세팅 함수
     * 2019-04-18 추천국/추천인 추가
     */
    private fun setUIControl() {
        LogPrinter.CF_debug("!-----------------------------------------------------------")
        LogPrinter.CF_debug("!-- IUII10M07_F.setUIControl()")
        LogPrinter.CF_debug("!-----------------------------------------------------------")

        // -----------------------------------------------------------------------------------------
        // -- view 및 라벨 세팅
        // -----------------------------------------------------------------------------------------
        // -- 금융기관(은행명)
        val tmp_linBankName: LinearLayout? = view?.findViewById(R.id.labelTextBank)
        val tmp_labelBankName: TextView? = tmp_linBankName?.findViewById(R.id.label)
        textBankName = tmp_linBankName?.findViewById(R.id.text)
        tmp_labelBankName?.text = resources.getString(R.string.label_bank_name_n)

        // -- 계좌번호 (단일)
        lin_BankAccount = view?.findViewById(R.id.labelEditBankAccount)
        lbl_BankAccount = lin_BankAccount?.findViewById(R.id.label) // 계좌번호 타이틀
        edt_BankAccount = lin_BankAccount?.findViewById(R.id.edit) // 계좌번호 입력란
        lbl_BankAccount?.text = resources.getString(R.string.label_bank_account_n)

        // -- 계좌번호(우체국 계좌 리스트)
        lin_BankAccountList = view?.findViewById(R.id.labelSpinnerBankAccountList)
        lbl_BankAccount = lin_BankAccountList!!.findViewById(R.id.label)
        spn_BankAccount = lin_BankAccountList!!.findViewById(R.id.spinner)
        lbl_BankAccount?.text = resources.getString(R.string.label_bank_account_n)

        // -- 추천국/추천인
        val tmp_linRecommendDepartment: LinearLayout? = view?.findViewById(R.id.labelTextRecommendDepartment) // 추천국
        val tmp_linRecommendPerson: LinearLayout? = view?.findViewById(R.id.labelTextRecommendPerson) // 추천인
        val tmp_labelRecommendDepartment: TextView? = tmp_linRecommendDepartment?.findViewById(R.id.label)
        val tmp_labelRecommendPerson: TextView? = tmp_linRecommendPerson?.findViewById(R.id.label)
        tmp_labelRecommendDepartment?.text = resources.getString(R.string.label_recomm_depart_name)
        tmp_labelRecommendPerson?.text = resources.getString(R.string.label_recomm_person_name)

        // -----------------------------------------------------------------------------------------
        // -- view 및 이벤트 세팅
        // -----------------------------------------------------------------------------------------
        // -- 금융기관(은행명)
        textBankName?.isClickable = true
        textBankName?.hint = resources.getString(R.string.hint_bank_company)
        textBankName?.contentDescription = "선택창, " + resources.getString(R.string.hint_bank_company) + ", 선택하시려면 두번 누르세요."
        textBankName?.tag = ""
        textBankName?.setBackgroundResource(R.drawable.btn_bank_name_selector)
        textBankName?.setOnClickListener { // -- Accessible On 상태 : 팝업 안내 후 이동
            if (CommonFunction.CF_checkAccessibilityTurnOn(Objects.requireNonNull(activity))) {
                val customDialog = CustomDialog((activity)!!)
                customDialog.show()
                customDialog.CF_setBackKeyMode(CustomDialog.BackMode.CANCELED)
                customDialog.CF_setTextContent(resources.getString(R.string.dlg_accessible_move_iuii31m00))
                customDialog.CF_setDoubleButtonText(resources.getString(R.string.btn_cancel), resources.getString(R.string.btn_ok))
                customDialog.setOnDismissListener { dialog ->
                    if (!(dialog as CustomDialog).CF_getCanceled()) {
                        startBankListActivity()
                    } else {
                        Handler().postDelayed(object : Runnable {
                            override fun run() {
                                textBankName?.sendAccessibilityEvent(AccessibilityEvent.TYPE_VIEW_FOCUSED)
                            }
                        }, 500)
                    }
                }
            } else {
                startBankListActivity()
            }
        }

        // -- 계좌번호 (단일)
        edt_BankAccount?.filters = CommonFunction.CF_getInputLengthFilter(maxlengthBankAccount)
        edt_BankAccount?.hint = resources.getString(R.string.hint_bank_account)
        edt_BankAccount?.inputType = InputType.TYPE_CLASS_NUMBER
        edt_BankAccount?.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                if (edt_BankAccount?.text?.length == maxlengthBankAccount) {
                    CommonFunction.CF_closeVirtualKeyboard(activity, edt_BankAccount!!.windowToken)
                }
            }

            override fun afterTextChanged(s: Editable) {}
        })

        // -- 계좌번호 (우체국 리스트)
        spn_BankAccount?.onItemSelectedListener = object : OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {}
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        // -- 예금주
        val tmp_linBankOwner: LinearLayout = view!!.findViewById(R.id.labelTextName)
        val tmp_labelName: TextView = tmp_linBankOwner.findViewById(R.id.label)
        tmp_labelName.text = resources.getString(R.string.label_bank_owner)
        textBankOwner = tmp_linBankOwner.findViewById(R.id.text)

        // -- 추천국
        textRecommDepart = tmp_linRecommendDepartment?.findViewById(R.id.text)
        textRecommDepart?.isClickable = true
        textRecommDepart?.hint = resources.getString(R.string.hint_recomm_depart_select)
        textRecommDepart?.contentDescription = "선택창, " + resources.getString(R.string.hint_recomm_depart_select) + ", 선택하시려면 두번 누르세요."
        textRecommDepart?.tag = ""
        textRecommDepart?.setBackgroundResource(R.drawable.btn_bank_name_selector)
        textRecommDepart?.setOnClickListener { // -- Accessible On 상태 : 팝업 안내 후 이동
            if (CommonFunction.CF_checkAccessibilityTurnOn(Objects.requireNonNull(activity))) {
                val customDialog = CustomDialog((activity)!!)
                customDialog.show()
                customDialog.CF_setBackKeyMode(CustomDialog.BackMode.CANCELED)
                customDialog.CF_setTextContent(resources.getString(R.string.dlg_accessible_move_iuii33m00))
                customDialog.CF_setDoubleButtonText(resources.getString(R.string.btn_cancel), resources.getString(R.string.btn_ok))
                customDialog.setOnDismissListener { dialog ->
                    if (!(dialog as CustomDialog).CF_getCanceled()) {
                        startIUII33M00(0)
                    } else {
                        Handler().postDelayed(object : Runnable {
                            override fun run() {
                                textRecommDepart?.sendAccessibilityEvent(AccessibilityEvent.TYPE_VIEW_FOCUSED)
                            }
                        }, 500)
                    }
                }
            } else {
                startIUII33M00(0)
            }
        }

        // -- 추천인
        textRecommPerson = tmp_linRecommendPerson?.findViewById(R.id.text)
        textRecommPerson?.isClickable = true
        textRecommPerson?.hint = resources.getString(R.string.hint_recommand_person_name)
        textRecommPerson?.contentDescription = "선택창, " + resources.getString(R.string.hint_recommand_person_name) + ", 선택하시려면 두번 누르세요."
        textRecommPerson?.tag = ""
        textRecommPerson?.setBackgroundResource(R.drawable.btn_bank_name_selector)
        textRecommPerson?.setOnClickListener { // -- Accessible On 상태 : 팝업 안내 후 이동
            if (CommonFunction.CF_checkAccessibilityTurnOn(Objects.requireNonNull(activity))) {
                val customDialog = CustomDialog((activity)!!)
                customDialog.show()
                customDialog.CF_setBackKeyMode(CustomDialog.BackMode.CANCELED)
                customDialog.CF_setTextContent(resources.getString(R.string.dlg_accessible_move_iuii33m01))
                customDialog.CF_setDoubleButtonText(resources.getString(R.string.btn_cancel), resources.getString(R.string.btn_ok))
                customDialog.setOnDismissListener { dialog ->
                    if (!(dialog as CustomDialog).CF_getCanceled()) {
                        startIUII33M00(1)
                    } else {
                        Handler().postDelayed(object : Runnable {
                            override fun run() {
                                textRecommPerson?.sendAccessibilityEvent(AccessibilityEvent.TYPE_VIEW_FOCUSED)
                            }
                        }, 500)
                    }
                }
            } else {
                startIUII33M00(1)
            }
        }

        // -- 다음 버튼
        btnNext = view?.findViewById(R.id.btnFill)
        btnNext?.text = resources.getString(R.string.btn_next_2)
        btnNext?.setOnClickListener {
            btnNext?.isEnabled = false

            // --<> 입력값 검사
            if (checkUserInput()) {
                // -- (HTTP) 계좌정보 확인 요청
                httpReq_checkBankAcc()
            } else {
                btnNext?.isEnabled = true
            }
        }
    }

    override fun onSaveInstanceState(p_bundle: Bundle) {
        super.onSaveInstanceState(p_bundle)
        LogPrinter.CF_debug("!-----------------------------------------------------------")
        LogPrinter.CF_debug("!-- IUII10M07_F.onSaveInstanceState()")
        LogPrinter.CF_debug("!-----------------------------------------------------------")

        saveData()
        p_bundle.putParcelable("data", data)
    }

    /**
     * 데이터 복구
     * 데이터 복구는 Fragment Data만 사용해야함 (Activity Data 사용불가)
     */
    private fun restoreData() {
        LogPrinter.CF_debug("!-----------------------------------------------------------")
        LogPrinter.CF_debug("!-- IUII10M07_F.restoreData()")
        LogPrinter.CF_debug("!-----------------------------------------------------------")

        // -- 예금주 & 은행코드 & 은행명
        textBankOwner!!.text = data!!.bankOwner
        textBankName!!.text  = data!!.bankName
        textBankName!!.tag   = data!!.bankCode
        if (TextUtils.isEmpty(textBankName!!.text.toString())) {
            textBankName!!.contentDescription = "선택창, " + resources.getString(R.string.hint_bank_company) + ", 선택하시려면 두번 누르세요."
        } else {
            textBankName!!.contentDescription = "선택창, " + textBankName!!.text + "선택됨, 선택하시려면 두번 누르세요."
        }

        // -- 계좌종류
        setAccountVisibility(data!!.accountVisibilityDivision)

        // -- 타행계좌(EditText)
        edt_BankAccount!!.setText(data!!.bankAccount)

        // -- 우체국계좌(span)
        val tmp_selectedPostAcno: String = data!!.bankPostAccount
        val tmp_postAccArray: Array<String>? = data!!.bankPostAccountArray
        try {
            val adapter: ArrayAdapter<String> = ArrayAdapter(mActivity!!.applicationContext, R.layout.layout_label_spinner_list, tmp_postAccArray!!)
            spn_BankAccount!!.adapter = adapter
        } catch (e: NullPointerException) {
            e.message
        } catch (e: Exception) {
            e.message
        }
        if (!("" == tmp_selectedPostAcno)) {
            if (tmp_postAccArray != null) {
                for (i in tmp_postAccArray.indices) {
                    if ((tmp_selectedPostAcno == tmp_postAccArray.get(i))) {
                        LogPrinter.CF_debug("!---- 계좌번호 일치(계좌번호) :" + tmp_postAccArray.get(i) + "/ index:" + i)
                        spn_BankAccount!!.setSelection(i)
                    }
                }
            }
        }

        // -- 추천국 & 추천인
        textRecommDepart!!.text = data!!.recommDepartName
        textRecommPerson!!.text = data!!.recommPersonName
        textRecommDepart!!.tag = data!!.recommDepartCode
        textRecommPerson!!.tag = data!!.recommPersonCode
        data!!.logPrint()
    }

    /**
     * 데이터 저장
     */
    private fun saveData() {
        LogPrinter.CF_debug("!-----------------------------------------------------------")
        LogPrinter.CF_debug("!-- IUII10M07_F.saveData()")
        LogPrinter.CF_debug("!-----------------------------------------------------------")

        // -- 금융기관, 계좌번호, 예금주
        data!!.bankCode = textBankName!!.tag as String?
        data!!.bankName = textBankName!!.text.toString().trim()
        data!!.bankOwner = textBankOwner!!.text.toString().trim()
        data!!.accountVisibilityDivision = accountVisibilityDivision
        data!!.bankAccount = edt_BankAccount!!.text.toString().trim()
        if (spn_BankAccount!!.selectedItem != null) {
            data!!.bankPostAccount = spn_BankAccount!!.selectedItem.toString().trim()
        }

        // -- 추천국 & 추천인
        data!!.recommDepartName = textRecommDepart!!.text.toString().trim()
        data!!.recommPersonName = textRecommPerson!!.text.toString().trim()
        data!!.recommDepartCode = textRecommDepart!!.tag.toString().trim()
        data!!.recommPersonCode = textRecommPerson!!.tag.toString().trim()
        data!!.logPrint()
    }

    /**
     * 사용자 입력 값 검사 함수<br></br>
     * 계좌정보확인 전 호출되는 함수<br></br>
     * @return      boolean
     */
    private fun checkUserInput(): Boolean {
        var flagOk = true
        val flagOnAccibility: Boolean = CommonFunction.CF_checkAccessibilityTurnOn(Objects.requireNonNull(activity))

        // --<1> (은행명 없음)
        if (textBankName!!.text.toString().trim().isEmpty()) {
            if (flagOnAccibility) {
                showCustomDlgAndAccessibilityFocus(resources.getString(R.string.dlg_no_select_bank), textBankName)
            } else {
                CommonFunction.CF_showCustomAlertDilaog(activity, resources.getString(R.string.dlg_no_select_bank), resources.getString(R.string.btn_ok))
            }
            flagOk = false
        }
        else if (edt_BankAccount!!.text.toString().isEmpty()) {
            // --<2> (우체국 계좌)
            if (("list" == accountVisibilityDivision)) {
                val tmpAcc: String = spn_BankAccount!!.selectedItem.toString()
                if ("" == tmpAcc) {
                    flagOk = false
                }
            } else {
                if (flagOnAccibility) {
                    val tmp_edtAccount: EditText = view?.findViewById<View>(R.id.relKeyInput)!!.findViewById(R.id.editText)
                    showCustomDlgAndAccessibilityFocus(resources.getString(R.string.dlg_no_input_bank_account), tmp_edtAccount)
                } else {
                    CommonFunction.CF_showCustomAlertDilaog(activity, resources.getString(R.string.dlg_no_input_bank_account), resources.getString(R.string.btn_ok))
                }
                flagOk = false
            }
        }
        else if (textBankOwner!!.text.toString().trim().isEmpty()) {
            showCustomDlgAndAccessibilityFocus(resources.getString(R.string.dlg_no_input_bank_owner), textBankOwner)
            if ("" != data!!.bankOwner) {
                textBankOwner!!.text = data!!.bankOwner
            }
        }
        return flagOk
    }

    /**
     * 계좌번호 입력칸 변경
     * @param reqVisibility "one":단일계좌, "list":셀렉트
     */
    private fun setAccountVisibility(reqVisibility: String) {
        when (reqVisibility) {
            "one" -> {
                accountVisibilityDivision = "one"
                lin_BankAccount!!.visibility = View.VISIBLE
                lin_BankAccountList!!.visibility = View.GONE
            }
            "list" -> {
                accountVisibilityDivision = "list"
                lin_BankAccount!!.visibility = View.GONE
                lin_BankAccountList!!.visibility = View.VISIBLE
            }
            else -> {
                accountVisibilityDivision = "one"
                LogPrinter.CF_debug("!---- 구분값 에러(단일계좌기본세팅) / 구분값 : $accountVisibilityDivision")
                lin_BankAccount!!.visibility = View.VISIBLE
                lin_BankAccountList!!.visibility = View.GONE
            }
        }
    }

    /**
     * 다이얼로그 팝업 후 접근성 포커스 이동
     * @param p_message     String
     * @param p_view        View
     */
    private fun showCustomDlgAndAccessibilityFocus(p_message: String, p_view: View?) {
        // -----------------------------------------------------------------------------------------
        //  StepIndicator hide 상태에서 AccessibilityEvent.TYPE_VIEW_FOCUSED 가 EditText 대상으로
        //  제대로 동작하지 않음.
        // -----------------------------------------------------------------------------------------
        mActivity!!.CF_setVisibleStepIndicator(true)

        val customDialog = CustomDialog((Objects.requireNonNull(activity))!!)
        customDialog.show()
        customDialog.CF_setBackKeyMode(CustomDialog.BackMode.NOTUSE)
        customDialog.CF_setTextContent(p_message)
        customDialog.CF_setSingleButtonText(getResources().getString(R.string.btn_ok))
        customDialog.setOnDismissListener {
            Handler().postDelayed(object : Runnable {
                override fun run() {
                    p_view!!.sendAccessibilityEvent(AccessibilityEvent.TYPE_VIEW_FOCUSED)
                }
            }, 500)
        }
    }

    // #############################################################################################
    //  HTTP 호출
    // #############################################################################################
    override fun handleMessage(p_message: Message) {
        LogPrinter.CF_debug("!-----------------------------------------------------------")
        LogPrinter.CF_debug("!-- IUII10M07_F.handleMessage()")
        LogPrinter.CF_debug("!-----------------------------------------------------------")

        if (mActivity != null && !mActivity!!.isDestroyed) {
            when (p_message.what) {
                HANDLERJOB_CHECK -> {
                    if (btnNext != null) {
                        btnNext!!.isEnabled = true
                    }
                    try {
                        // -- 결과값 저장
                        httpRes_checkBankAcc(JSONObject(p_message.obj as String?))
                    } catch (e: JSONException) {
                        LogPrinter.CF_line()
                        LogPrinter.CF_debug(getResources().getString(R.string.log_json_exception))
                    }
                }
                HANDLERJOB_ERROR_CHECK -> {
                    if (btnNext != null) {
                        btnNext!!.isEnabled = true
                    }
                    mActivity!!.CF_dismissProgressDialog()
                    CommonFunction.CF_showCustomAlertDilaog(activity, p_message.obj as String?, getResources().getString(R.string.btn_ok))
                }
                else -> {
                }
            }
        }
    }

    /**
     * 계좌정보 확인 요청
     */
    private fun httpReq_checkBankAcc() {
        LogPrinter.CF_debug("!-----------------------------------------------------------")
        LogPrinter.CF_debug("!-- IUII10M07_F.httpReq_checkBankAcc()")
        LogPrinter.CF_debug("!-----------------------------------------------------------")

        // 현재 포커싱 중인 VIew의 키패드 닫기
        if (Objects.requireNonNull(activity)!!.hasWindowFocus()) {
            if (activity!!.window.currentFocus != null) {
                CommonFunction.CF_closeVirtualKeyboard(activity, activity!!.window.currentFocus!!.windowToken)
            }
        }
        mActivity!!.CF_showProgressDialog()
        val builder: Uri.Builder = Uri.Builder()
        builder.appendQueryParameter("mode", "check")
        builder.appendQueryParameter("bankCode", textBankName!!.tag as String?)

        // -- 단일계좌필드가 활성화 되있을경우 edt필드에서, 리스트일때는 spn필드에서 값 추출
        authBankAccount = if (("one" == accountVisibilityDivision)) {
                            edt_BankAccount!!.text.toString().trim()
                        } else {
                            spn_BankAccount!!.selectedItem.toString().trim() }
        builder.appendQueryParameter("account", authBankAccount)
        builder.appendQueryParameter("owner", textBankOwner!!.text.toString().trim())

        LogPrinter.CF_debug("!---- account  : $authBankAccount")
        LogPrinter.CF_debug("!---- bankCode : " + textBankName!!.tag)
        LogPrinter.CF_debug("!---- owner    : " + textBankOwner!!.text.toString().trim())

        HttpConnections.sendPostData(
                EnvConfig.host_url + EnvConfig.URL_BANK_INFO_REQ,
                builder.build().encodedQuery,
                handler,
                HANDLERJOB_CHECK,
                HANDLERJOB_ERROR_CHECK)
    }

    /**
     * 계좌정보확인 요청 결과 처리 함수
     * @param p_jsonObject  JSONObject
     */
    @Throws(JSONException::class)
    private fun httpRes_checkBankAcc(p_jsonObject: JSONObject) {
        LogPrinter.CF_debug("!-----------------------------------------------------------")
        LogPrinter.CF_debug("!-- IUII10M07_F.httpRes_checkBankAcc() --계좌정보확인 요청 결과 처리 함수")
        LogPrinter.CF_debug("!-----------------------------------------------------------")

        mActivity!!.CF_dismissProgressDialog()
        val jsonKey_errorCode = "errCode"
        val tmp_errorCode: String

        // --<1> (전문수신 성공)
        if (p_jsonObject.has(jsonKey_errorCode)) {
            LogPrinter.CF_debug("!--<1> 전문수신 성공")
            tmp_errorCode = p_jsonObject.getString(jsonKey_errorCode)
            when {
                tmp_errorCode == "ERRIUII30M00001" -> {
                    LogPrinter.CF_debug("!--<2> (ERRIUII30M00001)전문 오류")
                    CommonFunction.CF_showCustomAlertDilaog(activity, getResources().getString(R.string.dlg_error_erriuii30m00001), resources.getString(R.string.btn_ok))
                }
                tmp_errorCode == "ERRIUII30M00002" -> {
                    LogPrinter.CF_debug("!--<2> (ERRIUII30M00002)예금주 다름")
                    CommonFunction.CF_showCustomAlertDilaog(activity, getResources().getString(R.string.dlg_error_erriuii30m00002), resources.getString(R.string.btn_ok))
                }
                tmp_errorCode == "ERRIUII30M00003" -> {
                    LogPrinter.CF_debug("!--<2> (ERRIUII30M00003)상대은행 처리 지연(전문 에러 EEA683)")
                    CommonFunction.CF_showCustomAlertDilaog(activity, getResources().getString(R.string.dlg_error_erriuii30m00003), resources.getString(R.string.btn_ok))
                }
                TextUtils.isEmpty(tmp_errorCode) -> {
                    LogPrinter.CF_debug("!--<2> Data수신 성공 (에러코드 empty)")

                    // -- ActivityData Set
                    aData = mActivity!!.CF_getData()
                    aData!!.CF_setS_fnis_nm(textBankName!!.text.toString().trim())
                    aData!!.CF_setS_fnis_code(textBankName!!.tag as String?)
                    aData!!.CF_setS_acno(authBankAccount)
                    aData!!.CF_setDecode_s_acno(authBankAccount)
                    aData!!.CF_setS_dpow_nm(textBankOwner!!.text.toString().trim())
                    aData!!.CF_setS_rcmd_brn_code(textRecommDepart!!.tag as String?)
                    aData!!.CF_setS_rcmd_clmm_no(textRecommPerson!!.tag as String?)
                    aData!!.logPrint()

                    // -- 계좌인증 완료 Dialog 팝업 후 -> 본인인증(IUPC80M00) 호출
                    val customDialog = CustomDialog((Objects.requireNonNull(activity))!!)
                    customDialog.show()
                    customDialog.CF_setTextContent(resources.getString(R.string.dlg_ok_bankcode))
                    customDialog.CF_setSingleButtonText(resources.getString(R.string.btn_ok))
                    customDialog.setOnDismissListener(object : DialogInterface.OnDismissListener {
                        override fun onDismiss(dialog: DialogInterface) {
                            when {
                                mActivity!!.CF_getAuthDvsn() == AuthDvsn.MOBILE -> { // 휴대폰 전자서명
                                    startIUPC90M00_P()
                                }
                                mActivity!!.CF_getAuthDvsn() == AuthDvsn.KAKAOPAY -> { // 카카오페이 전자서명
                                    startKakaopayAuth()
                                }
                                mActivity!!.CF_getAuthDvsn() == AuthDvsn.PINGER -> { // 간편인증(지문)
                                    startFidoAuth(Fido2Constant.AUTH_TECH_FINGER)
                                }
                                mActivity!!.CF_getAuthDvsn() == AuthDvsn.PIN -> { // 간편인증(핀)
                                    startFidoAuth(Fido2Constant.AUTH_TECH_PIN)
                                }
                                mActivity!!.CF_getAuthDvsn() == AuthDvsn.PATTERN -> { // 간편인증(패턴)
                                    startFidoAuth(Fido2Constant.AUTH_TECH_PATTERN)
                                }
                                mActivity!!.CF_getAuthDvsn() == AuthDvsn.FINCERT -> { // 금융인증서
                                    startIUPC10M00()
                                }
                                mActivity!!.CF_getAuthDvsn() == AuthDvsn.PASS -> { // PASS인증서
                                    startIUPC20M00()
                                }
                                else -> {  // -- (그외) 공동인증 전자서명
                                    startIUPC80M00()
                                }
                            }
                        }
                    })
                }
            }
        } else {
            LogPrinter.CF_debug("!--<1> 전문수신 실패")
            CommonFunction.CF_showCustomAlertDilaog(activity, getResources().getString(R.string.dlg_error_server_1), getResources().getString(R.string.btn_ok))
        }
    }
    // #############################################################################################
    //  Activity 호출
    // #############################################################################################
    /**
     * 금융기관 선택 Activity 호출 함수
     */
    private fun startBankListActivity() {
        LogPrinter.CF_debug("!-----------------------------------------------------------")
        LogPrinter.CF_debug("!-- IUII10M07_F.startBankListActivity()")
        LogPrinter.CF_debug("!-----------------------------------------------------------")

        val intent = Intent(activity, IUII31M00::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
        startActivityForResult(intent, EnvConfig.REQUESTCODE_ACTIVITY_CHOICE_BANK)
    }

    /**
     * 추천국/추천인 선택 Activity 호출 함수
     * @since 2019-05-29 최초 등록
     * @param fragmentDvsn  int     0:추천국화면 1:추천인화면
     */
    private fun startIUII33M00(fragmentDvsn: Int) {
        LogPrinter.CF_debug("!-----------------------------------------------------------")
        LogPrinter.CF_debug("!-- IUII10M07_F.startIUII33M00()")
        LogPrinter.CF_debug("!-----------------------------------------------------------")

        val tmp_departCode: String = textRecommDepart!!.tag as String // 추천국 코드

        // --<1> (추천인 선택시) : 추천국 유무에 따른 분기처리
        if (fragmentDvsn == 1) {
            // --<2> (추천국이 없을때) : 다이얼로그 && 추천국 선택화면으로 이동
            if ("" == tmp_departCode) {
                val customDialog = CustomDialog((Objects.requireNonNull(getActivity()))!!)
                customDialog.show()
                customDialog.CF_setTextContent(getResources().getString(R.string.dlg_ok_recomm))
                customDialog.CF_setSingleButtonText(getResources().getString(R.string.btn_ok))
                customDialog.setOnDismissListener { startIUII33M00EXE(0) }
            } else {
                startIUII33M00EXE(fragmentDvsn)
            }
        } else {
            startIUII33M00EXE(fragmentDvsn)
        }
    }

    /**
     * 공동인증서 전자서명 Activity 호출 함수
     */
    private fun startIUPC80M00() {
        LogPrinter.CF_debug("!-----------------------------------------------------------")
        LogPrinter.CF_debug("!-- IUII10M07_F.startIUPC80M00() --공동인증서 전자서명 요청")
        LogPrinter.CF_debug("!-----------------------------------------------------------")

        val intent = Intent(activity, IUPC80M00::class.java)
        intent.putExtra(EnvConstant.KEY_INTENT_AUTH_CSNO, CustomSQLiteFunction.getLastLoginCsno(mActivity))

        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
        startActivityForResult(intent, EnvConfig.REQUESTCODE_ACTIVITY_IUPC80M00)
    }

    /**
     * 금융인증서 전자서명 Activity 호출 함수
     */
    private fun startIUPC10M00() {
        LogPrinter.CF_debug("!-----------------------------------------------------------")
        LogPrinter.CF_debug("!-- IUII10M07_F.startIUPC10M00() --금융인증서 전자서명 요청")
        LogPrinter.CF_debug("!-----------------------------------------------------------")

        val intent = Intent(activity, IUPC10M00::class.java)
        intent.putExtra(EnvConstant.KEY_INTENT_AUTH_MODE, EnvConfig.AuthMode.SIGN_APP)

        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
        startActivityForResult(intent, EnvConfig.REQUESTCODE_ACTIVITY_IUPC10M00)
    }
    
    /**
     * PASS인증서 전자서명 Activity 호출 함수
     */
    private fun startIUPC20M00() {
        LogPrinter.CF_debug("!-----------------------------------------------------------")
        LogPrinter.CF_debug("!-- IUII10M07_F.startIUPC20M00() --PASS인증서 전자서명 요청")
        LogPrinter.CF_debug("!-----------------------------------------------------------")

        val intent = Intent(activity, IUPC20M00::class.java)
        intent.putExtra(EnvConstant.KEY_INTENT_AUTH_MODE, EnvConfig.AuthMode.SIGN_APP)

        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
        startActivityForResult(intent, EnvConfig.REQUESTCODE_ACTIVITY_IUPC20M00)
    }

    /**
     * 휴대폰인증 전자서명 Activity 호출 함수
     */
    private fun startIUPC90M00_P() {
        LogPrinter.CF_debug("!-----------------------------------------------------------")
        LogPrinter.CF_debug("!-- IUII10M07_F.startIUPC90M00_P() --휴대폰인증 전자서명 요청")
        LogPrinter.CF_debug("!-----------------------------------------------------------")

        val intent = Intent(activity, IUPC90M00_P::class.java)
        intent.putExtra(EnvConstant.KEY_INTENT_AUTH_NAME, textBankOwner!!.text.toString().trim()) // 휴대폰인증시 이름을 계좌 예금주로 세팅
        intent.putExtra(EnvConstant.KEY_INTENT_AUTH_MODE, EnvConfig.AuthMode.SIGN_APP)

        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
        startActivityForResult(intent, EnvConfig.REQUESTCODE_ACTIVITY_IUPC90M00)
    }

    /**
     * 간편인증(핀,지문,패턴) 전자서명 Activity 호출 함수
     * @param authTechCode  String
     */
    private fun startFidoAuth(authTechCode: String) {
        LogPrinter.CF_debug("!-----------------------------------------------------------")
        LogPrinter.CF_debug("!-- IUII10M07_F.startFidoAuth() --간편인증 전자서명 요청")
        LogPrinter.CF_debug("!-----------------------------------------------------------")

        val intent = Intent(activity, IUFC00M00::class.java)
        intent.putExtra(EnvConstant.KEY_INTENT_AUTH_MODE, EnvConfig.AuthMode.SIGN_APP)
        intent.putExtra(EnvConstant.KEY_INTENT_AUTH_TECH_CODE, authTechCode)

        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
        startActivityForResult(intent, EnvConfig.REQUESTCODE_ACTIVITY_IUFC00M00)
    }

    /**
     * 카카오페이인증 전자서명 Activity 호출 함수
     */
    private fun startKakaopayAuth() {
        LogPrinter.CF_debug("!-----------------------------------------------------------")
        LogPrinter.CF_debug("!-- IUII10M07_F.startKakaopayAuth() --카카오인증 전자서명 요청")
        LogPrinter.CF_debug("!-----------------------------------------------------------")

        val intent = Intent(activity, IUPC95M20::class.java)
        intent.putExtra(EnvConstant.KEY_INTENT_AUTH_MODE, EnvConfig.AuthMode.SIGN_APP)
        intent.putExtra(EnvConstant.KEY_INTENT_AUTH_CSNO, CustomSQLiteFunction.getLastLoginCsno(mActivity))
        intent.putExtra(EnvConstant.KEY_INTENT_AUTH_NAME, CustomSQLiteFunction.getLastLoginName(mActivity))

        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
        startActivityForResult(intent, EnvConfig.REQUESTCODE_ACTIVITY_IUCOK0M00)
    }

    /**
     * 추천국/추천인 ACTIVITY 실행
     * @param fragmentDvsn  int
     */
    private fun startIUII33M00EXE(fragmentDvsn: Int) {
        LogPrinter.CF_debug("!-----------------------------------------------------------")
        LogPrinter.CF_debug("!-- IUII10M07_F.startIUII33M00EXE(int fragmentDvsn)")
        LogPrinter.CF_debug("!-----------------------------------------------------------")
        LogPrinter.CF_debug("!---- 선택된 추천국 코드 : " + textRecommDepart!!.tag.toString())
        LogPrinter.CF_debug("!---- 선택된 추천인 코드 : " + textRecommPerson!!.tag.toString())

        val intent = Intent(activity, IUII33M00_P::class.java)
        intent.putExtra("fragmentDvsn"  , fragmentDvsn) // 추천국(0), 추천인(1)
        intent.putExtra("departCode"    , textRecommDepart!!.tag.toString())
        intent.putExtra("personRemnNo"  , textRecommPerson!!.tag.toString())
        intent.putExtra("departName"    , textRecommDepart!!.text.toString())
        intent.putExtra("personName"    , textRecommPerson!!.text.toString())
        intent.putExtra("srchDepartName", srchDepartName)
        intent.putExtra("srchPersonName", srchPersonName)

        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
        startActivityForResult(intent, EnvConfig.REQUESTCODE_ACTIVITY_IUII33M00)
    }
}