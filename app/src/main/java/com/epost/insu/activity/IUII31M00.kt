package com.epost.insu.activity

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Message
import android.view.View
import android.widget.GridView
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import com.epost.insu.EnvConfig
import com.epost.insu.R
import com.epost.insu.adapter.BankListAdapter
import com.epost.insu.common.LogPrinter
import com.epost.insu.control.CustomTabView
import com.epost.insu.data.Data_IUII31M00
import com.epost.insu.dialog.CustomDialog
import com.epost.insu.network.HttpConnections
import com.epost.insu.network.WeakReferenceHandler
import com.epost.insu.network.WeakReferenceHandler.ObjectHandlerMessage
import org.json.JSONException
import org.json.JSONObject

/**
 * @copyright : (주)프로니스정보기술 | http://www.phronis.co.kr
 *
 * @project   : 모바일슈랑스 구축
 * @pakage   : com.epost.insu.activity
 * @fileName  : IUII31M00.java
 *
 * @Title     : 보험금청구 > 금융기관선택 팝업 (화면 ID : IUII31M00) - #23
 * @author    : 이수행
 * @created   : 2017-08-31
 * @version   : 1.0
 *
 * @note      : - 금융기관 선택 화면(금융기관 정보는 서버 요청으로 Get)<br></br>
 * - 사용자는 1개의 금융기관을 선택하면 선택한 금융기관의 code , name을 Intent로 반환하고 Activity 종료
 * ======================================================================
 * 수정 내역
 * NO      날짜          작업자       내용
 * 01      2017-08-31    이수행       최초 등록
 * =======================================================================
 */
class IUII31M00 : Activity_Default(), ObjectHandlerMessage {
    private val subUrl_list = "/II/IUII31M00.do" // 금융기관코드 목록 조회

    private val HANDLERJOB_GET_LIST = 0 // 금융기관 목록 요청 성공 코드
    private val HANDLERJOB_ERROR_GET_LIST = 1 // 금융기관 목록 요청 에러 코드

    private var dataBanks: Data_IUII31M00? = null // 은행사 목록 데이터
    private var dataStocks: Data_IUII31M00? = null // 증권사 목록 데이터
    private var adapter: BankListAdapter? = null // 목록 Adapter
    private var tabView: CustomTabView? = null // 상단 은행/증권사 구분 TabView

    override fun handleMessage(p_message: Message) {
        if (!isDestroyed) {
            when (p_message.what) {
                HANDLERJOB_GET_LIST ->                     // 금융기관 목록 요청 성공
                    try {
                        setResultOfReqList(JSONObject(p_message.obj as String))
                    } catch (e: JSONException) {
                        LogPrinter.CF_line()
                        LogPrinter.CF_debug(resources.getString(R.string.log_json_exception))
                    }
                HANDLERJOB_ERROR_GET_LIST ->                     // 금융기관 목록 요청 실패
                    showDlgOfHttpError(p_message.obj as String, true)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        if (dataBanks!!.CF_getDataCount() == 0 && dataStocks!!.CF_getDataCount() == 0) {
            // 데이터 요청
            requestBankList()
        } else if (adapter!!.count == 0) {
            setAdapterData()
        }
    }

    override fun onSaveInstanceState(p_bundle: Bundle) {
        super.onSaveInstanceState(p_bundle)
        p_bundle.putParcelable("dataBanks", dataBanks) // 은행 목록 데이터
        p_bundle.putParcelable("dataStocks", dataStocks) // 증권 목록 데이터
        p_bundle.putInt("tabIndex", tabView!!.CF_getCurrentIndex())
    }

    override fun onRestoreInstanceState(p_bundle: Bundle) {
        super.onRestoreInstanceState(p_bundle)

        // 은행 목록 데이터
        if (p_bundle.containsKey("dataBanks")) {
            dataBanks = p_bundle.getParcelable("dataBanks")
        }

        // 증권 목록 데이터
        if (p_bundle.containsKey("dataStocks")) {
            dataStocks = p_bundle.getParcelable("dataStocks")
        }
        if (p_bundle.containsKey("tabIndex")) {
            tabView!!.CF_setSelectState(p_bundle.getInt("tabIndex"))
        }
    }

    override fun setInit() {
        setContentView(R.layout.iuii31m00)
        dataBanks = Data_IUII31M00()
        dataStocks = Data_IUII31M00()
        handler = WeakReferenceHandler(this)
        adapter = BankListAdapter(this, R.layout.list_bank_name)
        adapter!!.CE_setOnListItemClickedEventListener { p_index -> // 금융기관 선택시
            // 선택한 금융기관 정보(code , name)을 Intent에 담아 전달한다.
            val tmp_intent = Intent()
            tmp_intent.putExtra("code", adapter!!.CF_getCode(p_index))
            tmp_intent.putExtra("name", adapter!!.CF_getName(p_index))
            setResult(RESULT_OK, tmp_intent)
            finish()
        }
    }

    override fun setUIControl() {

        // 타이틀바 레이아웃 세팅
        setTitleBarUI()

        // 은행사 / 증권사 선택 TabView
        tabView = findViewById<View>(R.id.customTabView) as CustomTabView
        tabView!!.CF_setTabText(resources.getString(R.string.btn_bank), resources.getString(R.string.btn_stock))
        tabView!!.CF_setTabBackground(R.drawable.tab_button_selector, R.drawable.tab_button_selector)
        tabView!!.CE_setOnSelectedChangeEventListener { setAdapterData() }

        // 은행사 / 증권사 출력 GridView
        val tmp_grid = findViewById<View>(R.id.gridView) as GridView
        tmp_grid.adapter = adapter
    }

    /**
     * 타이틀바 레이아웃 세팅
     */
    private fun setTitleBarUI() {

        // 타이틀 세팅
        val tmp_title = findViewById<View>(R.id.title_bar_textTitle) as TextView
        tmp_title.text = resources.getString(R.string.title_choice_bank)

        // left 버튼 세팅
        val tmp_btnLeft = findViewById<View>(R.id.title_bar_imgBtnLeft) as ImageButton
        tmp_btnLeft.visibility = View.VISIBLE
        tmp_btnLeft.setOnClickListener { finish() }
    }

    /**
     * Adapter 데이터 세팅 함수<br></br>
     * 은행/증권사 구분하여 데이터를 세팅 한다.<br></br>
     * 사용자가 은행 Tab에 있는 경우 [.dataBanks] 이용, 증권사 Tab에 있는 경우 [.dataStocks] 이용
     */
    private fun setAdapterData() {
        if (tabView!!.CF_getCurrentIndex() == 0) {
            adapter!!.CF_setData(dataBanks!!.CF_getBankCodeList(), dataBanks!!.CF_getBankNameList())
        } else if (tabView!!.CF_getCurrentIndex() == 1) {
            adapter!!.CF_setData(dataStocks!!.CF_getBankCodeList(), dataStocks!!.CF_getBankNameList())
        }
        val tmp_linContent = findViewById<View>(R.id.linContent) as LinearLayout
        tmp_linContent.visibility = View.VISIBLE
    }
    // #############################################################################################
    //  Http 관련
    // #############################################################################################
    /**
     * 금융기관 목록 조회
     */
    private fun requestBankList() {
        //CF_showProgressDialog()
        val tmp_builder = Uri.Builder()
        HttpConnections.sendPostData(
                EnvConfig.host_url + subUrl_list,
                tmp_builder.build().encodedQuery,
                handler,
                HANDLERJOB_GET_LIST,
                HANDLERJOB_ERROR_GET_LIST)
    }

    /**
     * 금융사 목록 요청 결과 처리 함수
     * @param p_jsonObject JSONObject
     */
    @Throws(JSONException::class)
    private fun setResultOfReqList(p_jsonObject: JSONObject) {

        // 프로그레스 다이얼로그 dismiss
       // CF_dismissProgressDialog()
        val jsonKey_errorCode = "errCode"
        val jsonKey_data = "data"
        val jsonKey_arrBank = "arr_bank" // 은행권 목록
        val jsonKey_arrOther = "arr_other" // 증권사 목록

        //String tmp_errorCode = "";
        if (p_jsonObject.has(jsonKey_errorCode)) {
            val tmp_errorCode = p_jsonObject.getString(jsonKey_errorCode)
            if (tmp_errorCode == "ERRIUII31M00001") {        // 쿼리 오류
                showDlgOfHttpError(resources.getString(R.string.dlg_error_bank_list), true)
            } else if (p_jsonObject.has(jsonKey_data)) {
                val tmp_jsonData = p_jsonObject.getJSONObject(jsonKey_data)
                if (tmp_jsonData.has(jsonKey_arrBank) && tmp_jsonData.has(jsonKey_arrOther)) {
                    val tmp_jsonArrBank = tmp_jsonData.getJSONArray(jsonKey_arrBank)
                    val tmp_jsonArrStocks = tmp_jsonData.getJSONArray(jsonKey_arrOther)

                    // 데이터 클래스 세팅
                    dataBanks!!.CF_setData(tmp_jsonArrBank)
                    dataStocks!!.CF_setData(tmp_jsonArrStocks)

                    // Adapter 세팅
                    setAdapterData()
                }
            } else {
                showDlgOfHttpError(resources.getString(R.string.dlg_error_server_2), true)
            }
        } else {
            showDlgOfHttpError(resources.getString(R.string.dlg_error_server_1), true)
        }
    }

    /**
     * 네트워크 오류 발생시 사용하는 다이얼로그 팝업
     * @param p_message String
     * @param p_flagFinish  if true, 다이얼로그 종료 시 Activity 종료
     */
    private fun showDlgOfHttpError(p_message: String, p_flagFinish: Boolean) {

        // 프로그레스 다이얼로그 dismiss
        CF_dismissProgressDialog()
        val tmp_dlg = CustomDialog(this)
        tmp_dlg.show()
        tmp_dlg.CF_setBackKeyMode(CustomDialog.BackMode.NOTUSE)
        tmp_dlg.CF_setTextContent(p_message)
        tmp_dlg.CF_setSingleButtonText(resources.getString(R.string.btn_ok))
        tmp_dlg.setOnDismissListener {
            if (p_flagFinish) {
                finish()
            }
        }
    }
}