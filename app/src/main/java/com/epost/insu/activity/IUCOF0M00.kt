package com.epost.insu.activity

import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.*
import android.text.TextUtils
import android.util.TypedValue
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.*
import android.widget.AdapterView.OnItemClickListener
import android.widget.TextView.OnEditorActionListener
import com.epost.insu.*
import com.epost.insu.adapter.AddressAdapter
import com.epost.insu.common.*
import com.epost.insu.data.Data_IUCOF0M00
import com.epost.insu.network.HttpConnections
import com.epost.insu.network.WeakReferenceHandler
import com.epost.insu.network.WeakReferenceHandler.ObjectHandlerMessage
import org.json.JSONException
import org.json.JSONObject

/**
 * @copyright : (주)프로니스정보기술 | http://www.phronis.co.kr
 *
 * @project   : 모바일슈랑스 구축
 * @pakage    : com.epost.insu.activity
 * @fileName  : IUCOD2M00.java
 *
 * @Title     : 공통 > 보험금청구 > 주소검색 & 주소검색 팝업_상세정보입력 (화면 ID : IUCOD2M00 , IUCOF0M01, IUCOF0M02) - #12, #13, #14
 * @author    : 이수행
 * @created   : 2017-07-25
 * @version   : 1.0
 *
 * @note      : - 도로명주소 통합검색 화면<br></br>
 * - 사용자가 입력한 검색어로 주소 검색을 하고 상세주소는 입력 받는다.<br></br>
 * - 도로명,건물명,지번으로 통합검색이 가능하다.(검색어 최소 2글자 이상)<br></br>
 * - 사용자가 선택한 기본주소와 입력한 상세주소를 Intent로 반환한다.<br></br>
 * ======================================================================
 * 수정 내역
 * NO      날짜          작업자       내용
 * 01      2017-08-11    이수행       최초 등록
 * =======================================================================
 */
class IUCOF0M00 : Activity_Default(), ObjectHandlerMessage {
    private val minLengthSearch = 2 // 검색어 min length
    private val maxLengthSearch = 40
    private val maxLengthAddrDetail = 40

    //private final String subUrl_search = "/CO/IUCOD2M00.do";          // 전문 사용 (반환값 중 s_seri_key 타입 String)
    private val subUrl_search_2 = "/CO/IUCOF0M01.do" // WAS DB 사용 (반환값 중 s_seri_key 타입 int)
    private val HANDLERJOB_LIST = 0 // GET 목록 최초
    private val HANDLERJOB_ERROR_LIST = 1 // GET 목록 에러
    private var textZipNo: TextView? = null
    private var textAddr: TextView? = null
    private var edtSearch: EditText? = null
    private var edtAddrDetail: EditText? = null
    private var linGuide: LinearLayout? = null
    private var linEmpty: LinearLayout? = null
    private var linList: LinearLayout? = null
    private var scrResult: ScrollView? = null
    private var listViewAddr: ListView? = null
    private var viewMore: FrameLayout? = null
    private var data: Data_IUCOF0M00? = null
    private var adapter: AddressAdapter? = null
    private var currentPageIndex = 0
    private var s_seri_key = 0

    //private String s_seri_key;              // 주소검색 page Index
    private var flagAddedFooter // 주소목록 더가져오기 버튼 추가 여부
            = false

    override fun handleMessage(p_message: Message) {
        if (!isDestroyed) {
            // 프로그레스 다이얼로그 dismiss
            CF_dismissProgressDialog()
            when (p_message.what) {
                HANDLERJOB_LIST -> try {
                    setResultOfSearchAddress(JSONObject(p_message.obj as String))
                } catch (e: JSONException) {
                    LogPrinter.CF_line()
                    LogPrinter.CF_debug(resources.getString(R.string.log_json_exception))
                }
                HANDLERJOB_ERROR_LIST -> CommonFunction.CF_showCustomAlertDilaog(this, p_message.obj as String, resources.getString(R.string.btn_ok))
                else -> {
                }
            }
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putInt("curPageIndex", currentPageIndex)
        outState.putParcelable("data", data)
        outState.putBoolean("flagAddedFooter", flagAddedFooter)
        outState.putInt("s_seri_key", s_seri_key)

        // -----------------------------------------------------------------------------------------
        //  현재 페이지가 상세주소 입력 페이지
        // -----------------------------------------------------------------------------------------
        if (currentPageIndex == 3) {
            outState.putString("textZipNo", textZipNo!!.text.toString())
            outState.putString("textAddr", textAddr!!.text.toString())
            outState.putString("edtAddrDetail", edtAddrDetail!!.text.toString().trim { it <= ' ' })
            val tmp_addr = textAddr!!.getTag(R.string.tag_addr_road) as String
            val tmp_townName = textAddr!!.getTag(R.string.tag_town_name) as String
            if (TextUtils.isEmpty(tmp_addr) == false) {
                outState.putString("tagAddr", tmp_addr)
            }
            if (TextUtils.isEmpty(tmp_townName) == false) {
                outState.putString("tagTownName", tmp_townName)
            }
        }
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        if (savedInstanceState.containsKey("curPageIndex")) {
            currentPageIndex = savedInstanceState.getInt("curPageIndex")
            showPage(currentPageIndex)

            // -----------------------------------------------------------------------------------------
            //  현재 페이지가 상세주소 입력 페이지
            // -----------------------------------------------------------------------------------------
            if (currentPageIndex == 3) {
                textZipNo!!.text = savedInstanceState.getString("textZipNo")
                textAddr!!.text = savedInstanceState.getString("textAddr")
                if (savedInstanceState.containsKey("tagAddr")) {
                    textAddr!!.setTag(R.string.tag_addr_road, savedInstanceState.getString("tagAddr"))
                }
                if (savedInstanceState.containsKey("tagTownName")) {
                    textAddr!!.setTag(R.string.tag_town_name, savedInstanceState.getString("tagTownName"))
                }
                edtAddrDetail!!.setText(savedInstanceState.getString("edtAddrDetail"))
            }
        }
        if (savedInstanceState.containsKey("data")) {
            data = savedInstanceState.getParcelable("data")
            adapter!!.CF_setData(data!!.CF_getZipNo(), data!!.CF_getTownName(), data!!.CF_getAddrRoad(), data!!.CF_getAddrBunji())
        }
        if (savedInstanceState.containsKey("flagAddedFooter")) {
            flagAddedFooter = savedInstanceState.getBoolean("flagAddedFooter")
            listViewAddr!!.addFooterView(viewMore)
        }
        if (savedInstanceState.containsKey("s_seri_key")) {
            s_seri_key = savedInstanceState.getInt("s_seri_key")
        }
    }

    override fun onBackPressed() {
        if (currentPageIndex == 2) {
            showPage(0)
        } else {
            super.onBackPressed()
        }
    }

    override fun setInit() {
        setContentView(R.layout.iucof0m00)
        currentPageIndex = 0
        //s_seri_key = "";
        s_seri_key = 0
        flagAddedFooter = false
        data = Data_IUCOF0M00()
        adapter = AddressAdapter(this, R.layout.list_address)
        handler = WeakReferenceHandler(this)
    }

    override fun setUIControl() {

        // 타이틀바
        setTitleBarUI()

        // 상세보기 페이지
        setDetailUI()
        linGuide = findViewById<View>(R.id.linGuide) as LinearLayout
        linEmpty = findViewById<View>(R.id.linEmpty) as LinearLayout
        linList = findViewById<View>(R.id.linList) as LinearLayout
        edtSearch = findViewById<View>(R.id.edtSearch) as EditText
        edtSearch!!.filters = CommonFunction.CF_getInputLengthFilter(maxLengthSearch)
        edtSearch!!.setOnEditorActionListener(OnEditorActionListener { v, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                requestSearchAddress(true)
                if (edtSearch!!.text.toString().trim { it <= ' ' }.length >= minLengthSearch) {
                    return@OnEditorActionListener true
                }
            }
            false
        })


        // 주소검색 이미지 버튼
        val tmp_btnSearch = findViewById<View>(R.id.activity_search_addr_imgSearch) as ImageButton
        tmp_btnSearch.setOnClickListener { requestSearchAddress(true) }

        // 더 가져오기 FooterView 생성
        createFooterView()

        // 주소목록 ListView
        listViewAddr = findViewById<View>(R.id.activity_search_Addr_listVIew) as ListView
        listViewAddr!!.adapter = adapter
        listViewAddr!!.onItemClickListener = OnItemClickListener { parent, view, position, id ->
            setSelectedAddrInfo(adapter!!.CF_getZipCode(position), adapter!!.CF_getAddrRoad(position), adapter!!.CF_getTownName(position))
            showPage(3)
            if (CommonFunction.CF_checkAccessibilityTurnOn(this@IUCOF0M00)) {
                Handler().postDelayed({
                    clearAllFocus()
                    edtAddrDetail!!.requestFocus()
                }, 500)
            }
        }

        // 확인 버튼
        val tmp_btnOk = findViewById<View>(R.id.btnFill) as Button
        tmp_btnOk.text = resources.getString(R.string.btn_ok)
        tmp_btnOk.setOnClickListener { finishSearchAddress() }
        showPage(currentPageIndex)
    }

    /**
     * 타이틀바 레이아웃 세팅
     */
    private fun setTitleBarUI() {

        // 타이틀 세팅
        val tmp_title = findViewById<View>(R.id.title_bar_textTitle) as TextView
        tmp_title.text = resources.getString(R.string.title_search_addr)

        // left 버튼 세팅
        val tmp_btnLeft = findViewById<View>(R.id.title_bar_imgBtnLeft) as ImageButton
        tmp_btnLeft.visibility = View.VISIBLE
        tmp_btnLeft.setOnClickListener { finish() }
    }




    /**
     * 주소검색 상세보기 UI 세팅 함수
     */
    private fun setDetailUI() {
        scrResult = findViewById<View>(R.id.scrResult) as ScrollView
        val tmp_linZipNo = findViewById<View>(R.id.labelTextZipNo) as LinearLayout
        val tmp_linAddr = findViewById<View>(R.id.labelTextAddr) as LinearLayout
        val tmp_linAddrDetail = findViewById<View>(R.id.labelEditAddrDetail) as LinearLayout

        // 라벨 세팅
        val tmp_labelZipNo = tmp_linZipNo.findViewById<View>(R.id.label) as TextView
        val tmp_labelAddr = tmp_linAddr.findViewById<View>(R.id.label) as TextView
        val tmp_labelAddrDetail = tmp_linAddrDetail.findViewById<View>(R.id.label) as TextView
        tmp_labelZipNo.text = resources.getString(R.string.label_zipcode)
        tmp_labelAddr.text = resources.getString(R.string.label_addr_default)
        tmp_labelAddrDetail.text = resources.getString(R.string.label_addr_detail)
        textZipNo = tmp_linZipNo.findViewById<View>(R.id.text) as TextView
        textAddr = tmp_linAddr.findViewById<View>(R.id.text) as TextView
        edtAddrDetail = tmp_linAddrDetail.findViewById<View>(R.id.edit) as EditText
        edtAddrDetail!!.filters = CommonFunction.CF_getInputLengthFilter(maxLengthAddrDetail)
        edtAddrDetail!!.setHint(R.string.hint_addr_detail)
    }

    /**
     * 주소검색 결과 리스트의 FooterView 반환
     * @return
     */
    private fun createFooterView(): View {
        viewMore = FrameLayout(this)
        viewMore!!.layoutParams = AbsListView.LayoutParams(AbsListView.LayoutParams.MATCH_PARENT, AbsListView.LayoutParams.WRAP_CONTENT)
        val tmp_lpBtn = FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.WRAP_CONTENT)
        tmp_lpBtn.topMargin = CommonFunction.CF_convertDipToPixel(applicationContext, 10f)
        val tmp_btnMore = Button(this)
        tmp_btnMore.layoutParams = tmp_lpBtn
        tmp_btnMore.setBackgroundResource(R.drawable.btn_navy_selector)
        tmp_btnMore.setTextSize(TypedValue.COMPLEX_UNIT_PX, resources.getDimensionPixelSize(R.dimen.button_text_size).toFloat())
        tmp_btnMore.setTextColor(Color.WHITE)
        tmp_btnMore.text = resources.getString(R.string.btn_more_address)
        tmp_btnMore.setPadding(0, CommonFunction.CF_convertDipToPixel(applicationContext, 10f), 0, CommonFunction.CF_convertDipToPixel(applicationContext, 10f))
        tmp_btnMore.setOnClickListener { requestSearchAddress(false) }
        viewMore!!.addView(tmp_btnMore)
        return viewMore as FrameLayout
    }

    /**
     * 해당 페이지 Show 함수
     * @param p_pageIndex
     */
    private fun showPage(p_pageIndex: Int) {
        currentPageIndex = p_pageIndex
        if (p_pageIndex == 0) {                             // 초기 상태
            linGuide!!.visibility = View.VISIBLE
            linEmpty!!.visibility = View.GONE
            linList!!.visibility = View.GONE
            scrResult!!.visibility = View.GONE
        } else if (p_pageIndex == 1) {                        // 검색 데이터 Empty
            linGuide!!.visibility = View.VISIBLE
            linEmpty!!.visibility = View.VISIBLE
            linList!!.visibility = View.GONE
            scrResult!!.visibility = View.GONE
        } else if (p_pageIndex == 2) {                        // 검색 데이터 존재
            linGuide!!.visibility = View.VISIBLE
            linEmpty!!.visibility = View.GONE
            linList!!.visibility = View.VISIBLE
            scrResult!!.visibility = View.GONE
        } else if (p_pageIndex == 3) {                        // 상세 주소 입력
            linGuide!!.visibility = View.GONE
            linEmpty!!.visibility = View.GONE
            linList!!.visibility = View.GONE
            scrResult!!.visibility = View.VISIBLE
        }
    }

    /**
     * 사용자가 선택한 주소값을 결과 LayoutOut에 출력한다.
     * @param p_zipCode
     * @param p_address
     */
    private fun setSelectedAddrInfo(p_zipCode: String, p_address: String, p_townName: String) {
        textZipNo!!.text = p_zipCode
        textAddr!!.text = "$p_address ($p_townName)"
        textAddr!!.setTag(R.string.tag_addr_road, p_address)
        textAddr!!.setTag(R.string.tag_town_name, p_townName)
    }

    /**
     * 종료 함수
     */
    private fun finishSearchAddress() {
        if (edtAddrDetail!!.windowToken != null) {
            CommonFunction.CF_closeVirtualKeyboard(this, edtAddrDetail!!.windowToken)
        }
        val tmp_intent = Intent()
        tmp_intent.putExtra("zipNo", textZipNo!!.text.toString().trim { it <= ' ' })
        tmp_intent.putExtra("addrRoad", textAddr!!.getTag(R.string.tag_addr_road) as String)
        //tmp_intent.putExtra("townName", (String)textAddr.getTag(R.string.tag_town_name));
        tmp_intent.putExtra("addrDetail", edtAddrDetail!!.text.toString().trim { it <= ' ' })
        setResult(RESULT_OK, tmp_intent)
        finish()
    }

    /**
     * 사용자 입력값 검사 함수
     * @return
     */
    private fun checkUserInput(): Boolean {
        var tmp_flagOk = true

        // 검색어 입력값 검사 함수
        if (edtSearch!!.text.toString().trim { it <= ' ' }.length < minLengthSearch) {
            tmp_flagOk = false
            showCustomDialog(resources.getString(R.string.dlg_input_search_addr), edtSearch!!)
        }
        return tmp_flagOk
    }
    // #############################################################################################
    //  Http 관련
    // #############################################################################################
    /**
     * 주소 검색 요청
     */
    private fun requestSearchAddress(p_flagInitSearch: Boolean) {
        if (checkUserInput()) {
            // 프로그레스 다이얼로그 show
            CF_showProgressDialog()

            // -------------------------------------------------------------------------------------
            //  해당 주소 첫 조회인 경우(더 가져오기 X) : 데이터 초기화 및 첫 페이지 이동
            // -------------------------------------------------------------------------------------
            if (p_flagInitSearch) {
                //s_seri_key = "";
                s_seri_key = 0
                data!!.CF_clear()
                adapter!!.CF_clear()
                showPage(0)
            }
            val tmp_builder = Uri.Builder()
            tmp_builder.appendQueryParameter("getListAddress", edtSearch!!.text.toString().trim { it <= ' ' })
            tmp_builder.appendQueryParameter("s_seri_key", "" + s_seri_key)
            HttpConnections.sendPostData(
                    EnvConfig.host_url + subUrl_search_2,
                    tmp_builder.build().encodedQuery,
                    handler,
                    HANDLERJOB_LIST,
                    HANDLERJOB_ERROR_LIST)
        }
    }

    /**
     * 주소 검색 요청결과 처리 함수
     */
    @Throws(JSONException::class)
    private fun setResultOfSearchAddress(p_jsonObject: JSONObject) {

        // 프로그레스 다이얼로그 dismiss
        CF_dismissProgressDialog()
        val jsonKey_errorCode = "errCode"
        val jsonKey_data = "data"
        val jsonKey_flagIsOver = "flagIsOver"
        val jsonKey_list = "list"
        val jsonKey_s_seri_key = "s_seri_key"
        var tmp_errorCode = ""
        if (p_jsonObject.has(jsonKey_errorCode)) {
            tmp_errorCode = p_jsonObject.getString(jsonKey_errorCode)
            if (tmp_errorCode == "ERRIUCOF0M00001") {
                showPage(1)
                if (CommonFunction.CF_checkAccessibilityTurnOn(this)) {
                    showCustomDialog("""
    ${resources.getString(R.string.label_empty_addr)}
    
    ${resources.getString(R.string.guide_search_addr_3)}
    """.trimIndent(), edtSearch!!)
                }
            } else if (tmp_errorCode == "ERRIUCOF0M00002") {
                showPage(0)
                if (CommonFunction.CF_checkAccessibilityTurnOn(this)) {
                    showCustomDialog(resources.getString(R.string.dlg_error_erriucof0m00002), edtSearch!!)
                }
            } else if (p_jsonObject.has(jsonKey_data)) {
                val tmp_jsonData = p_jsonObject.getJSONObject(jsonKey_data)
                val tmp_flagIsOver = tmp_jsonData.getBoolean(jsonKey_flagIsOver)
                //s_seri_key = tmp_jsonData.getString(jsonKey_s_seri_key);
                s_seri_key = tmp_jsonData.getInt(jsonKey_s_seri_key)
                val tmp_jsonArrList = tmp_jsonData.getJSONArray(jsonKey_list)
                if (tmp_flagIsOver) {
                    if (flagAddedFooter) {
                        flagAddedFooter = false
                        listViewAddr!!.removeFooterView(viewMore)
                    }
                } else {
                    if (flagAddedFooter == false) {
                        flagAddedFooter = true
                        listViewAddr!!.addFooterView(viewMore)
                    }
                }

                // 데이터 갱신 전에 Scroll 포지션을 Top 위치로 변경해야하는지 확인한다.
                // 최초 목록 요칭 && 주소 데이터가 있을 경우 Top 위치로 이동
                var tmp_flagScrollTop = false
                if (data!!.CF_getDataCount() == 0 && tmp_jsonArrList.length() > 0) {
                    tmp_flagScrollTop = true
                }
                data!!.CF_addData(tmp_jsonArrList)
                adapter!!.CF_setData(data!!.CF_getZipNo(), data!!.CF_getTownName(), data!!.CF_getAddrRoad(), data!!.CF_getAddrBunji())

                // ListView 포지션 이동
                if (tmp_flagScrollTop) {
                    listViewAddr!!.setSelection(0)
                }

                // 해당 페이지 이동
                if (adapter!!.count > 0) {
                    showPage(2)
                } else {
                    showPage(1)
                }

                // ---------------------------------------------------------------------------------
                //  접근성을 위한 팝업 안내
                // ---------------------------------------------------------------------------------
                if (adapter!!.count == 0) {
                    // -----------------------------------------------------------------------------
                    //  진입 X : 서버에서 데이터가 없는 경우 에러코드 ERRIUCOF0M00001 반환으로 변경됨.
                    //  서버가 언제 변경될 지 알 수 없어 소스 남김.
                    // -----------------------------------------------------------------------------
                    showCustomDialog("""
    ${resources.getString(R.string.label_empty_addr)}
    
    ${resources.getString(R.string.guide_search_addr_3)}
    """.trimIndent(), edtSearch!!)
                } else if (tmp_flagScrollTop) {
                    Handler().postDelayed({
                        listViewAddr!!.isFocusableInTouchMode = true
                        listViewAddr!!.requestFocus()
                        listViewAddr!!.isFocusableInTouchMode = false
                    }, 500)
                }
            } else {
                showPage(0)
                CommonFunction.CF_showCustomAlertDilaog(this, resources.getString(R.string.dlg_error_server_2), resources.getString(R.string.btn_ok))
            }
        } else {
            showPage(0)
            CommonFunction.CF_showCustomAlertDilaog(this, resources.getString(R.string.dlg_error_server_1), resources.getString(R.string.btn_ok))
        }
    }
}