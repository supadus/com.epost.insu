package com.epost.insu.fragment

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.database.sqlite.SQLiteDatabase
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import com.epost.insu.R
import com.epost.insu.common.CustomSQLiteHelper
import com.epost.insu.data.Data_IUII90M00

/**
 * @copyright : 우정사업정보센터
 *
 * @project   : 모바일슈랑스 구축
 * @pakage    : com.epost.insu.fragment
 * @fileName  : IUII90M08_F.java
 *
 * @Title     : 보험금청구 > 자녀청구 > 8단계. 접수신청정보확인 (화면 ID : iuii90m08_f)
 * @author    : 양지훈
 * @created   : 2018-11-09
 * @version   : 1.0
 *
 * @note      :
 * ======================================================================
 * 수정 내역
 * NO       날짜          작업자       내용
 * 01       2018-11-09    양지훈     : 최초 등록
 * 02       2019-08-02    노지민     : 8단계 삭제로 인한 사용안함
 * =======================================================================
 */
class IUII90M08_F constructor() : IUII90M00_FD() {
    private var data: Data_IUII90M00? = null
    private var linContents: LinearLayout? = null

    // 접수신청정보 Text
    private var textReqName: TextView? = null
    private var textReqCategory: TextView? = null
    private var textReqReason: TextView? = null
    private var textReqType: TextView? = null // 청구자,청구유형,발생원인,청구사유
    private var textAcdpName: TextView? = null
    private var textBeneficiaryName: TextView? = null // 피보험자(자녀), 수익자
    private var textDiseaseName: TextView? = null
    private var textAccidentDate: TextView? = null
    private var textAccidentPlace: TextView? = null
    private var textAccidentReason: TextView? = null // 진단명,사고일시,사고장소,사고경위

    private var textCarInsure: TextView? = null
    private var textCarInsureYN: TextView? = null
    private var textIndustryInsure: TextView? = null
    private var textCallPolice: TextView? = null
    private var textEtc: TextView? = null // 자동차보험여부,자동차보험,산업재해,경찰서신고

    private var textOtherInsure: TextView? = null
    private var textOtherInsureYN: TextView? = null
    private var textSilsonYN: TextView? = null
    private var textSilsonInsure: TextView? = null // 타사가입내용,가입여부,실손가입여부
    private var textBankInfo: TextView? = null // 계좌정보

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 초기 세팅
        setInit()
    }

    @SuppressLint("InflateParams")
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.iuii90m08_f, null)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        // UI 생성 및 세팅
        setUIControl()

        // 데이터 복구
        restoreData(savedInstanceState)
    }

    override fun onResume() {
        super.onResume()
    }

    override fun onStop() {
        super.onStop()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putBoolean("flagVisibleContents", linContents!!.getVisibility() == View.VISIBLE)
        outState.putString("textReqName", textReqName!!.getText().toString())
        outState.putString("textAcdpName", textAcdpName!!.getText().toString())
        outState.putString("textBeneficiaryName", textBeneficiaryName!!.getText().toString())
        outState.putString("textReqCategory", textReqCategory!!.getText().toString())
        outState.putString("textReqReason", textReqReason!!.getText().toString())
        outState.putString("textReqType", textReqType!!.getText().toString())
        outState.putString("textBankInfo", textBankInfo!!.getText().toString())
        outState.putString("textDiseaseName", textDiseaseName!!.getText().toString())
        outState.putString("textAccidentDate", textAccidentDate!!.getText().toString())
        outState.putString("textAccidentPlace", textAccidentPlace!!.getText().toString())
        outState.putString("textAccidentReason", textAccidentReason!!.getText().toString())
        outState.putString("textCarInsureYN", textCarInsureYN!!.getText().toString())
        outState.putString("textCarInsure", textCarInsure!!.getText().toString())
        outState.putString("textIndustryInsure", textIndustryInsure!!.getText().toString())
        outState.putString("textCallPolice", textCallPolice!!.getText().toString())
        outState.putString("textEtc", textEtc!!.getText().toString())
        outState.putString("textOtherInsure", textOtherInsure!!.getText().toString())
        outState.putString("textOtherInsureYN", textOtherInsureYN!!.getText().toString())
        outState.putString("textSilsonYN", textSilsonYN!!.getText().toString())
        outState.putString("textSilsonInsure", textSilsonInsure!!.getText().toString())
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
    }

    override fun onDestroy() {
        super.onDestroy()
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
    }

    /**
     * 초기 세팅 함수
     */
    private fun setInit() {
        data = mActivity!!.CF_getData()
    }

    /**
     * UI 생성 및 세팅 함수
     */
    private fun setUIControl() {
        linContents = getView()!!.findViewById<View>(R.id.lin_Contents) as LinearLayout?
        setUIRequestInfo() // 접수신청정보 UI 세팅
        btnNext = getView()!!.findViewById<View>(R.id.btnFill) as Button
        btnNext?.setText(getResources().getString(R.string.btn_next_2))
        btnNext?.setOnClickListener(object : View.OnClickListener {
            override fun onClick(v: View) {
                btnNext?.setEnabled(false)
                mActivity?.CF_showNextPage()
            }
        })
    }

    /**
     * 접수신청정보 UI 세팅 함수
     */
    private fun setUIRequestInfo() {
        val tmp_linReqName: LinearLayout = getView()!!.findViewById<View>(R.id.label_TextReqName) as LinearLayout
        val tmp_linAcdpName: LinearLayout = getView()!!.findViewById<View>(R.id.label_TextAcdpName) as LinearLayout
        val tmp_linBeneficiaryName: LinearLayout = getView()!!.findViewById<View>(R.id.label_TextBeneficiaryName) as LinearLayout
        val tmp_linReqCategory: LinearLayout = getView()!!.findViewById<View>(R.id.label_TextReqCategory) as LinearLayout
        val tmp_linReqReason: LinearLayout = getView()!!.findViewById<View>(R.id.label_TextReqReason) as LinearLayout
        val tmp_linReqType: LinearLayout = getView()!!.findViewById<View>(R.id.label_TextReqType) as LinearLayout
        val tmp_linBankInfo: LinearLayout = getView()!!.findViewById<View>(R.id.label_TextBankInfo) as LinearLayout
        val tmp_linDiseaseName: LinearLayout = getView()!!.findViewById<View>(R.id.label_TextDiseaseName) as LinearLayout
        val tmp_linAccidentDate: LinearLayout = getView()!!.findViewById<View>(R.id.label_TextAccidentDate) as LinearLayout
        val tmp_linAccidentPlace: LinearLayout = getView()!!.findViewById<View>(R.id.label_TextAccidentPlace) as LinearLayout
        val tmp_linAccidentReason: LinearLayout = getView()!!.findViewById<View>(R.id.label_TextAccidentReason) as LinearLayout
        val tmp_linCarInsureYN: LinearLayout = getView()!!.findViewById<View>(R.id.label_TextCarInsureYN) as LinearLayout
        val tmp_linCarInsure: LinearLayout = getView()!!.findViewById<View>(R.id.label_TextCarInsure) as LinearLayout
        val tmp_linIndustryInsure: LinearLayout = getView()!!.findViewById<View>(R.id.label_TextIndustryInsure) as LinearLayout
        val tmp_linCallPolice: LinearLayout = getView()!!.findViewById<View>(R.id.label_TextCallPolice) as LinearLayout
        val tmp_linEtc: LinearLayout = getView()!!.findViewById<View>(R.id.label_TextEtc) as LinearLayout
        val tmp_linOtherInsureYN: LinearLayout = getView()!!.findViewById<View>(R.id.label_TextOtherInsureYN) as LinearLayout
        val tmp_linOtherInsure: LinearLayout = getView()!!.findViewById<View>(R.id.label_TextOtherInsure) as LinearLayout
        val tmp_linSilsonYN: LinearLayout = getView()!!.findViewById<View>(R.id.label_TextSilsonYN) as LinearLayout
        val tmp_linSilsonInsure: LinearLayout = getView()!!.findViewById<View>(R.id.label_TextSilsonInsure) as LinearLayout
        val tmp_labelReqName: TextView = tmp_linReqName.findViewById<View>(R.id.label) as TextView
        val tmp_labelAcdpName: TextView = tmp_linAcdpName.findViewById<View>(R.id.label) as TextView
        val tmp_labelBeneficiaryName: TextView = tmp_linBeneficiaryName.findViewById<View>(R.id.label) as TextView
        val tmp_labelReqCategory: TextView = tmp_linReqCategory.findViewById<View>(R.id.label) as TextView
        val tmp_labelReqReason: TextView = tmp_linReqReason.findViewById<View>(R.id.label) as TextView
        val tmp_labelReqType: TextView = tmp_linReqType.findViewById<View>(R.id.label) as TextView
        val tmp_labelReqBank: TextView = tmp_linBankInfo.findViewById<View>(R.id.label) as TextView
        val tmp_labelDiseaseName: TextView = tmp_linDiseaseName.findViewById<View>(R.id.label) as TextView
        val tmp_labelAccidentDate: TextView = tmp_linAccidentDate.findViewById<View>(R.id.label) as TextView
        val tmp_labelAccidentPlace: TextView = tmp_linAccidentPlace.findViewById<View>(R.id.label) as TextView
        val tmp_labelAccidentReason: TextView = tmp_linAccidentReason.findViewById<View>(R.id.label) as TextView
        val tmp_labelCarInsureYN: TextView = tmp_linCarInsureYN.findViewById<View>(R.id.label) as TextView
        val tmp_labelCarInsure: TextView = tmp_linCarInsure.findViewById<View>(R.id.label) as TextView
        val tmp_labelIndustryInsure: TextView = tmp_linIndustryInsure.findViewById<View>(R.id.label) as TextView
        val tmp_labelCallPolice: TextView = tmp_linCallPolice.findViewById<View>(R.id.label) as TextView
        val tmp_labelEtc: TextView = tmp_linEtc.findViewById<View>(R.id.label) as TextView
        val tmp_labelOtherInsureYN: TextView = tmp_linOtherInsureYN.findViewById<View>(R.id.label) as TextView
        val tmp_labelOtherInsure: TextView = tmp_linOtherInsure.findViewById<View>(R.id.label) as TextView
        val tmp_labelSilsonYN: TextView = tmp_linSilsonYN.findViewById<View>(R.id.label) as TextView
        val tmp_labelSilsonInsure: TextView = tmp_linSilsonInsure.findViewById<View>(R.id.label) as TextView
        tmp_labelReqName.setText(getResources().getString(R.string.label_request_name))
        tmp_labelAcdpName.setText(getResources().getString(R.string.label_acdp_name))
        tmp_labelBeneficiaryName.setText(getResources().getString(R.string.label_beneficiary_name))
        tmp_labelReqCategory.setText(getResources().getString(R.string.label_req_category))
        tmp_labelReqReason.setText(getResources().getString(R.string.label_req_reason))
        tmp_labelReqType.setText(getResources().getString(R.string.label_req_type))
        tmp_labelReqBank.setText(getResources().getString(R.string.label_req_bank_account))
        tmp_labelDiseaseName.setText(getResources().getString(R.string.label_disease_name))
        tmp_labelAccidentDate.setText(getResources().getString(R.string.label_accident_date))
        tmp_labelAccidentPlace.setText(getResources().getString(R.string.label_accident_place))
        tmp_labelAccidentReason.setText(getResources().getString(R.string.label_accident_note))
        tmp_labelCarInsureYN.setText(getResources().getString(R.string.label_insure_car))
        tmp_labelCarInsure.setText(getResources().getString(R.string.label_insure_name))
        tmp_labelIndustryInsure.setText(getResources().getString(R.string.label_industry_insure))
        tmp_labelCallPolice.setText(getResources().getString(R.string.label_call_police))
        tmp_labelEtc.setText(getResources().getString(R.string.label_other))
        tmp_labelOtherInsureYN.setText(getResources().getString(R.string.label_flag_other_insure_yn))
        tmp_labelOtherInsure.setText(getResources().getString(R.string.label_insure_name))
        tmp_labelSilsonYN.setText(getResources().getString(R.string.label_flag_real_insure))
        tmp_labelSilsonInsure.setText(getResources().getString(R.string.label_insure_name))
        textReqName = tmp_linReqName.findViewById<View>(R.id.text) as TextView?
        textAcdpName = tmp_linAcdpName.findViewById<View>(R.id.text) as TextView?
        textBeneficiaryName = tmp_linBeneficiaryName.findViewById<View>(R.id.text) as TextView?
        textReqCategory = tmp_linReqCategory.findViewById<View>(R.id.text) as TextView?
        textReqReason = tmp_linReqReason.findViewById<View>(R.id.text) as TextView?
        textReqType = tmp_linReqType.findViewById<View>(R.id.text) as TextView?
        textBankInfo = tmp_linBankInfo.findViewById<View>(R.id.text) as TextView?
        textDiseaseName = tmp_linDiseaseName.findViewById<View>(R.id.text) as TextView?
        textAccidentDate = tmp_linAccidentDate.findViewById<View>(R.id.text) as TextView?
        textAccidentPlace = tmp_linAccidentPlace.findViewById<View>(R.id.text) as TextView?
        textAccidentReason = tmp_linAccidentReason.findViewById<View>(R.id.text) as TextView?
        textCarInsureYN = tmp_linCarInsureYN.findViewById<View>(R.id.text) as TextView?
        textCarInsure = tmp_linCarInsure.findViewById<View>(R.id.text) as TextView?
        textIndustryInsure = tmp_linIndustryInsure.findViewById<View>(R.id.text) as TextView?
        textCallPolice = tmp_linCallPolice.findViewById<View>(R.id.text) as TextView?
        textEtc = tmp_linEtc.findViewById<View>(R.id.text) as TextView?
        textOtherInsureYN = tmp_linOtherInsureYN.findViewById<View>(R.id.text) as TextView?
        textOtherInsure = tmp_linOtherInsure.findViewById<View>(R.id.text) as TextView?
        textSilsonYN = tmp_linSilsonYN.findViewById<View>(R.id.text) as TextView?
        textSilsonInsure = tmp_linSilsonInsure.findViewById<View>(R.id.text) as TextView?
    }

    /**
     * onSave 에서 저장한 데이터 복구
     * @param p_bundle
     */
    private fun restoreData(p_bundle: Bundle?) {
        if (p_bundle != null) {
            if (p_bundle.containsKey("flagVisibleContents")) {
                val tmp_flagVisibleContents: Boolean = p_bundle.getBoolean("flagVisibleContents")
                if (tmp_flagVisibleContents) {
                    linContents!!.setVisibility(View.VISIBLE)
                } else {
                    linContents!!.setVisibility(View.GONE)
                }
            }
            if (p_bundle.containsKey("textReqName")) {
                textReqName!!.setText(p_bundle.getString("textReqName"))
            }
            if (p_bundle.containsKey("textAcdpName")) {
                textAcdpName!!.setText(p_bundle.getString("textAcdpName"))
            }
            if (p_bundle.containsKey("textBeneficiaryName")) {
                textBeneficiaryName!!.setText(p_bundle.getString("textBeneficiaryName"))
            }
            if (p_bundle.containsKey("textReqCategory")) {
                textReqCategory!!.setText(p_bundle.getString("textReqCategory"))
            }
            if (p_bundle.containsKey("textReqReason")) {
                textReqReason!!.setText(p_bundle.getString("textReqReason"))
            }
            if (p_bundle.containsKey("textReqType")) {
                textReqType!!.setText(p_bundle.getString("textReqType"))
            }
            if (p_bundle.containsKey("textBankInfo")) {
                textBankInfo!!.setText(p_bundle.getString("textBankInfo"))
            }
            if (p_bundle.containsKey("textDiseaseName")) {
                textDiseaseName!!.setText(p_bundle.getString("textDiseaseName"))
            }
            if (p_bundle.containsKey("textAccidentDate")) {
                textAccidentDate!!.setText(p_bundle.getString("textAccidentDate"))
            }
            if (p_bundle.containsKey("textAccidentPlace")) {
                textAccidentPlace!!.setText(p_bundle.getString("textAccidentPlace"))
            }
            if (p_bundle.containsKey("textAccidentReason")) {
                textAccidentReason!!.setText(p_bundle.getString("textAccidentReason"))
            }
            if (p_bundle.containsKey("textCarInsureYN")) {
                textCarInsureYN!!.setText(p_bundle.getString("textCarInsureYN"))
            }
            if (p_bundle.containsKey("textCarInsure")) {
                textCarInsure!!.setText(p_bundle.getString("textCarInsure"))
            }
            if (p_bundle.containsKey("textIndustryInsure")) {
                textIndustryInsure!!.setText(p_bundle.getString("textIndustryInsure"))
            }
            if (p_bundle.containsKey("textCallPolice")) {
                textCallPolice!!.setText(p_bundle.getString("textCallPolice"))
            }
            if (p_bundle.containsKey("textEtc")) {
                textEtc!!.setText(p_bundle.getString("textEtc"))
            }
            if (p_bundle.containsKey("textOtherInsure")) {
                textOtherInsure!!.setText(p_bundle.getString("textOtherInsure"))
            }
            if (p_bundle.containsKey("textOtherInsureYN")) {
                textOtherInsureYN!!.setText(p_bundle.getString("textOtherInsureYN"))
            }
            if (p_bundle.containsKey("textSilsonYN")) {
                textSilsonYN!!.setText(p_bundle.getString("textSilsonYN"))
            }
            if (p_bundle.containsKey("textSilsonInsure")) {
                textSilsonInsure!!.setText(p_bundle.getString("textSilsonInsure"))
            }
        }
    }

    /**
     * 보험금청구 상세내역 데이터 세팅 함수
     */
    fun CF_setData() {
        textReqName!!.setText(user_name_fromSqlite) // 청구자
        textAcdpName!!.setText(data!!.CF_getS_acdp_nm()) // 피보험자(자녀)
        textBeneficiaryName!!.setText(data!!.CF_getS_bnfc_nm()) // 수익자
        textReqCategory!!.setText(data!!.CF_getStr_s_insu_requ_type_code()) // 청구유형
        textReqReason!!.setText(data!!.CF_getStr_s_requ_gent_caus_code()) // 발생원인
        textReqType!!.setText(data!!.CF_getStr_s_insu_requ_resn_code()) // 청구사유

        // 진단명,사고일시,사고장소,사고경위
        textDiseaseName!!.setText(data!!.CF_getS_dign_nm())
        textAccidentDate!!.setText(convertDateTime(data!!.CF_getS_acdt_date() + data!!.CF_getS_acdt_time()))
        textAccidentPlace!!.setText(data!!.CF_getS_acdt_pace())
        textAccidentReason!!.setText(data!!.CF_getS_acdt_cntt())

        // 사고처리여부,자동차보험,산업재해,경찰서신고, 기타
        textCarInsureYN!!.setText(data!!.CF_getS_car_insu_yn())
        textCarInsure!!.setText(data!!.CF_getS_insu_comp_nm())
        textIndustryInsure!!.setText(data!!.CF_getS_inds_dstr_insu_yn())
        textCallPolice!!.setText(data!!.CF_getS_polc_decl_yn())
        textEtc!!.setText(data!!.CF_getS_etc_desc())

        // 타사가입내용,가입여부,실손가입여부
        textOtherInsureYN!!.setText(data!!.CF_getS_other_insu_comp_entr_yn())
        textOtherInsure!!.setText(data!!.CF_getS_other_insu_comp_nm())
        textSilsonYN!!.setText(data!!.CF_getS_rllo_entr_yn())
        textSilsonInsure!!.setText(data!!.CF_getS_rllo_insu_comp_nm())
        textBankInfo!!.setText("은행명 : " + data!!.CF_getS_fnis_nm() + "\r\n" + "계좌번호 : " + data!!.CF_getDecode_s_acno() + "\r\n예금주 : " + data!!.CF_getS_dpow_nm()) // 계좌정보
    }

    /**
     * 데이터 클리어
     */
    fun CF_clearData() {
        textReqName!!.setText("")
        textAcdpName!!.setText("")
        textBeneficiaryName!!.setText("")
        textReqCategory!!.setText("")
        textReqReason!!.setText("")
        textReqType!!.setText("")
        textBankInfo!!.setText("")
        textDiseaseName!!.setText("")
        textAccidentDate!!.setText("")
        textAccidentPlace!!.setText("")
        textAccidentReason!!.setText("")
        textCarInsureYN!!.setText("")
        textCarInsure!!.setText("")
        textIndustryInsure!!.setText("")
        textCallPolice!!.setText("")
        textEtc!!.setText("")
        textOtherInsure!!.setText("")
        textOtherInsureYN!!.setText("")
        textSilsonYN!!.setText("")
        textSilsonInsure!!.setText("")
    }

    /**
     * YYYYMMDDHHMMSS 형식을 YYYY.MM.DD HH:MM:SS로 변환
     * @param p_strDateTime
     * @return
     */
    private fun convertDateTime(p_strDateTime: String): String {
        var tmp_convertedData: String = p_strDateTime
        if (p_strDateTime.length == 14) {
            tmp_convertedData = (p_strDateTime.substring(0, 4) + "." + p_strDateTime.substring(4, 6) + "." + p_strDateTime.substring(6, 8) + " "
                    + p_strDateTime.substring(8, 10) + ":" + p_strDateTime.substring(10, 12) + ":" + p_strDateTime.substring(12, 14))
        }
        return tmp_convertedData
    }
    // #############################################################################################
    //  sqlite
    // #############################################################################################
    /**
     * 단말기 DB에 저장되어 있는 사용자 이름 반환<br></br>
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
}