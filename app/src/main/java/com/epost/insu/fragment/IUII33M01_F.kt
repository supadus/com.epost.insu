package com.epost.insu.fragment

import android.annotation.SuppressLint
import android.content.Context
import android.content.DialogInterface
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.os.Message
import android.util.TypedValue
import android.view.*
import android.view.inputmethod.EditorInfo
import android.widget.*
import android.widget.TextView.OnEditorActionListener
import androidx.core.content.ContextCompat
import com.epost.insu.EnvConfig
import com.epost.insu.R
import com.epost.insu.activity.Activity_Default
import com.epost.insu.activity.IUII33M00_P
import com.epost.insu.adapter.RecommandPersonListAdapter
import com.epost.insu.common.*
import com.epost.insu.data.Data_IUII33M01
import com.epost.insu.dialog.CustomDialog
import com.epost.insu.event.OnListItemClickedEventListener
import com.epost.insu.network.HttpConnections
import com.epost.insu.network.WeakReferenceHandler
import com.epost.insu.network.WeakReferenceHandler.ObjectHandlerMessage
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.util.*

//import com.epost.insu.control.CustomTabView;
/**
 * 보험금청구 > 보험금청구 > 추천인선택 팝업
 * @since     : project 30:1.2.8
 * @version   : 1.0
 * @author    : NJM
 * @see IUII33M00_P     추천국/추천인 Acitivity
 * <pre>
 * - 추천인 선택 화면
</pre> */
class IUII33M01_F constructor() : Fragment_Default(), ObjectHandlerMessage {
    private var mActivity: IUII33M00_P? = null
    private val subUrl: String = "/MY/IUMYS0M05.do" // 추천인 목록 조회
    private val HANDLERJOB_GET_LIST: Int = 0 // 목록 요청 성공 코드
    private val HANDLERJOB_ERROR_GET_LIST: Int = 1 // 목록 요청 에러 코드
    private var data: Data_IUII33M01? = null // 추천인 목록 데이터

    private var adapter: RecommandPersonListAdapter? = null // 추천인 목록 Adapter

    private var handler: WeakReferenceHandler? = null
    private var listener: OnListItemClickedEventListener? = null // 리스트 아이템 클릭 리스너


    //TODO NJM 탭뷰 구현 or 처리필요
    //private CustomTabView tabView;                              // 상단 추천국/추천인 구분 TabView
    /* 추천인 */
    private var tv_departName // 추천국 명 (선택된)
            : TextView? = null
    private var edtSerachName // 추천인 명
            : EditText? = null
    private val maxLengthPersonName: Int = 40 // 추천인 명 max length
    private var listView // 추천인 목록
            : ListView? = null
    private var viewMore // 푸터(더보기버튼)
            : FrameLayout? = null
    private var flag_firstSearch: Boolean = true // 첫조회 여부
    private var flag_addedFooter // 더보기버튼 유무
            : Boolean = false
    private var fl_ListConNodata // 리스트 : 노데이터
            : FrameLayout? = null
    private var fl_ListCon // 리스트
            : FrameLayout? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)
        LogPrinter.CF_debug("!----------------------------------------------------------")
        LogPrinter.CF_debug("!-- IUII33M01_F.onAttach()")
        LogPrinter.CF_debug("!----------------------------------------------------------")
        mActivity = getActivity() as IUII33M00_P?
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        LogPrinter.CF_debug("!----------------------------------------------------------")
        LogPrinter.CF_debug("!-- IUII33M01_F.onCreate()")
        LogPrinter.CF_debug("!----------------------------------------------------------")
        data = Data_IUII33M01()
        handler = WeakReferenceHandler(this)
        flag_addedFooter = false
        adapter = RecommandPersonListAdapter((getActivity())!!, R.layout.list_recomm_person)
        adapter!!.CE_setOnListItemClickedEventListener(object : OnListItemClickedEventListener {
            override fun onClick(p_index: Int) {
                if (listener != null) {
                    listener!!.onClick(p_index)
                }
            }
        })
    }

    @SuppressLint("InflateParams")
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        LogPrinter.CF_debug("!----------------------------------------------------------")
        LogPrinter.CF_debug("!-- IUII33M01_F.onCreateView()")
        LogPrinter.CF_debug("!----------------------------------------------------------")
        return inflater.inflate(R.layout.iuii33m01_f, null)
    }

    /**
     * OnCreateView > (Activity)onCreate() > onActivityCreated()
     * Activity와 Fragment의 뷰가 모두 생성된 상태
     * View를 변경하는 작업이 가능한 단계
     * onDestroyView() 에서 돌아오는 단계
     * @param savedInstanceState Bundle
     */
    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        LogPrinter.CF_debug("!----------------------------------------------------------")
        LogPrinter.CF_debug("!-- IUII33M01_F.onActivityCreated()")
        LogPrinter.CF_debug("!----------------------------------------------------------")
        setBundleData(savedInstanceState)
        setUIControl() // UI 생성 및 세팅
    }

    /**
     * Bundle 데이터 세팅 함수
     * @param p_bundle Bundle
     */
    private fun setBundleData(p_bundle: Bundle?) {
        if (p_bundle != null) {
            if (p_bundle.containsKey("data")) {
                data = p_bundle.getParcelable("data")
            }
            //            if(p_bundle.containsKey("flagReqData")){
//                flagReqData = p_bundle.getBoolean("flagReqData");
//            }
        }
    }

    /**
     * UI 생성 및 세팅 함수
     */
    fun setUIControl() {
        LogPrinter.CF_debug("!----------------------------------------------------------")
        LogPrinter.CF_debug("!-- IUII33M01_F.setUIControl()")
        LogPrinter.CF_debug("!----------------------------------------------------------")

        // -- 추천국 라벨(선택된)
        val tmp_linDepartLabel: LinearLayout = getView()!!.findViewById(R.id.labelTxt_recommDepartName)
        val tmp_labelDepartName: TextView = tmp_linDepartLabel.findViewById(R.id.label)
        val tmp_lpLabel1: LinearLayout.LayoutParams = tmp_labelDepartName.getLayoutParams() as LinearLayout.LayoutParams
        tmp_lpLabel1.width = CommonFunction.CF_convertDipToPixel(getActivity()!!.getApplicationContext(), 75f)
        tmp_labelDepartName.setLayoutParams(tmp_lpLabel1) // 라벨 LayoutParam 설정
        tmp_labelDepartName.setPadding(
                CommonFunction.CF_convertDipToPixel(getActivity()!!.getApplicationContext(), 10f),
                tmp_labelDepartName.getPaddingTop(),
                tmp_labelDepartName.getPaddingRight(),
                tmp_labelDepartName.getPaddingBottom()
        ) // 라벨 Pading 설정
        tmp_labelDepartName.setText(getResources().getString(R.string.label_recomm_depart_name)) // 라벨 Text 설정
        tv_departName = tmp_linDepartLabel.findViewById(R.id.text) // 추천인 입력 TextView 설정
        tv_departName?.setTextColor(ContextCompat.getColor((getContext())!!, R.color.colorAccent))
        tv_departName?.setText(mActivity!!.CF_getDepartName()) // 선택된 추천국명

        // -- 추천인 라벨
        val tmp_linPersonLabel: LinearLayout = getView()!!.findViewById(R.id.labelEdit_recommPersonSearch) // 추천인 조회 라인
        val tmp_labelPensonName: TextView = tmp_linPersonLabel.findViewById(R.id.label) // 라벨 세팅
        val tmp_lpLabel2: LinearLayout.LayoutParams = tmp_labelDepartName.getLayoutParams() as LinearLayout.LayoutParams
        tmp_lpLabel2.width = CommonFunction.CF_convertDipToPixel(getActivity()!!.getApplicationContext(), 75f)
        tmp_labelPensonName.setLayoutParams(tmp_lpLabel2) // 라벨 LayoutParam 설정
        tmp_labelPensonName.setPadding(
                CommonFunction.CF_convertDipToPixel(getActivity()!!.getApplicationContext(), 10f),
                tmp_labelPensonName.getPaddingTop(),
                tmp_labelPensonName.getPaddingRight(),
                tmp_labelPensonName.getPaddingBottom()
        ) // 라벨 Pading 설정
        tmp_labelPensonName.setText(getResources().getString(R.string.label_recomm_person_name)) // 라벨 Text 설정

        // -- 조회버튼
        val tmp_linSearchNameBtn: ImageButton = getView()!!.findViewById(R.id.linBtn_recommPersonSearch)
        tmp_linSearchNameBtn.setOnClickListener(object : View.OnClickListener {
            override fun onClick(v: View) {
                // --<1> input값 체크 : 공백
                if (("" == edtSerachName!!.getText().toString().trim({ it <= ' ' }))) {
                    // --<2> 추천국코드 체크 : 공백이 아닐때
                    if (!("" == mActivity!!.CF_getDepartCode())) {
                        requestList(true)
                    } else {
                        Toast.makeText(getActivity(), "추천국을 선택해주세요", Toast.LENGTH_LONG).show()
                    }
                } else {
                    requestList(true)
                }
            }
        })

        // -- 추천인 에디터텍스트 뷰
        edtSerachName = tmp_linPersonLabel.findViewById(R.id.edit) // 추천인 입력 EditText 설정
        edtSerachName?.setFilters(CommonFunction.CF_getInputLengthFilter(maxLengthPersonName))
        edtSerachName?.setImeOptions(EditorInfo.IME_ACTION_SEARCH)
        edtSerachName?.setHint(R.string.hint_recommand_person_name)
        //edtSerachName.setText(mActivity.CF_getPersonName());
        edtSerachName?.setText("")
        edtSerachName?.setOnEditorActionListener(object : OnEditorActionListener {
            override fun onEditorAction(v: TextView, actionId: Int, event: KeyEvent): Boolean {

                // 소프트버튼(검색) 클릭 이벤트
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    tmp_linSearchNameBtn.performClick()
                }
                return false
            }
        })

        // //////////////////////////////////////////////////////////////////////////////////////////////////////NJM 추천국 라벨 추가
//        // 추천국 / 추천인 선택 TabView
//        tabView = (CustomTabView)findViewById(R.id.customTabView);
//        tabView.CF_setTabText(getResources().getString(R.string.btn_recomm_depart), getResources().getString(R.string.btn_recomm_person));
//        tabView.CF_setTabBackground(R.drawable.btn_navy_left_selector, R.drawable.btn_navy_right_selector);
//        tabView.CE_setOnSelectedChangeEventListener(new OnSelectedChangeEventListener() {
//            @Override
//            public void onSelected(int p_index) {
//                setadapterata();
//            }
//        });

        // -- 더 가져오기 FooterView 생성
        createFooterView()

        // -- 리스트뷰
        fl_ListCon = getView()!!.findViewById(R.id.fl_ListCon) // 리스트뷰 Con
        fl_ListConNodata = getView()!!.findViewById(R.id.fl_ListConNodata) // 노데이터 리스브뷰 Con

        // 리스트뷰 세팅
        // 초기에 Nodata 뷰
        fl_ListCon?.setVisibility(View.GONE)
        fl_ListConNodata?.setVisibility(View.VISIBLE)

        // 추천국/추천인 출력 ListView
        listView = getView()!!.findViewById(R.id.listView)
        listView?.setAdapter(adapter)
    }

    override fun onResume() {
        super.onResume()
        LogPrinter.CF_debug("!----------------------------------------------------------")
        LogPrinter.CF_debug("!-- IUII33M01_F.onResume()")
        LogPrinter.CF_debug("!----------------------------------------------------------")
        LogPrinter.CF_debug("!-- data.CF_getDataCount():" + data!!.CF_getDataCount())

        // -- 추천국코드 유무에 따른 분기
        // 추천국코드가 없을 경우 : 빈페이지 출력
        if (("" == mActivity!!.CF_getDepartCode())) {
            LogPrinter.CF_debug("!-- edtSerachName:" + edtSerachName!!.getText().toString().trim({ it <= ' ' }))

            // 추천국코드가 있을 경우 : http 요청
        } else {
            requestList(true)
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        LogPrinter.CF_debug("!----------------------------------------------------------")
        LogPrinter.CF_debug("!-- IUII33M01_F.onSaveInstanceState()")
        LogPrinter.CF_debug("!----------------------------------------------------------")
        outState.putParcelable("dataPerson", data) // 추천인 목록 데이터
        //outState.putInt("tabIndex", tabView.CF_getCurrentIndex());      // 탭뷰 인덱스
    }
    //    @Override
    //    protected void onRestoreInstanceState(Bundle p_bundle) {
    //        super.onRestoreInstanceState(p_bundle);
    //
    //        // 추천국 목록 데이터
    //        if(p_bundle.containsKey("dataDepartment")){
    //            dataDepartment = p_bundle.getParcelable("dataDepartment");
    //        }
    //
    //        // 추천인 목록 데이터
    //        if(p_bundle.containsKey("dataPerson")){
    //            dataPerson = p_bundle.getParcelable("dataPerson");
    //        }
    //
    //        // 탭인덱스
    //        if(p_bundle.containsKey("tabIndex")){
    //            tabView.CF_setSelectState(p_bundle.getInt("tabIndex"));
    //        }
    //    }
    /**
     * 리스트의 FooterView 반환
     * @return  View
     */
    private fun createFooterView(): View {
        LogPrinter.CF_debug("!----------------------------------------------------------")
        LogPrinter.CF_debug("!-- IUII33M01_F.createFooterView()")
        LogPrinter.CF_debug("!----------------------------------------------------------")
        viewMore = FrameLayout((getContext())!!)
        viewMore!!.layoutParams = AbsListView.LayoutParams(AbsListView.LayoutParams.MATCH_PARENT, AbsListView.LayoutParams.WRAP_CONTENT)
        val tmp_lpBtn: FrameLayout.LayoutParams = FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.WRAP_CONTENT)
        tmp_lpBtn.topMargin = CommonFunction.CF_convertDipToPixel(getContext()!!.getApplicationContext(), 10f)
        val tmp_btnMore: Button = Button(getContext())
        tmp_btnMore.setLayoutParams(tmp_lpBtn)
        tmp_btnMore.setBackgroundResource(R.drawable.btn_navy_selector)
        tmp_btnMore.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimensionPixelSize(R.dimen.button_text_size).toFloat())
        tmp_btnMore.setTextColor(Color.WHITE)
        tmp_btnMore.setText(getResources().getString(R.string.btn_more))
        tmp_btnMore.setPadding(0, CommonFunction.CF_convertDipToPixel(getContext()!!.getApplicationContext(), 10f), 0, CommonFunction.CF_convertDipToPixel(getContext()!!.getApplicationContext(), 10f))
        tmp_btnMore.setOnClickListener(object : View.OnClickListener {
            override fun onClick(v: View) {
                requestList(false)
            }
        })
        viewMore!!.addView(tmp_btnMore)
        return viewMore!!
    }

    /**
     * Adapter 데이터 세팅 함수<br></br>
     */
    private fun updateList() {
        adapter!!.CF_setData(data!!.CF_getDepartmentRemnNoList(), data!!.CF_getDepartmentNameList())
        listView!!.setVisibility(View.VISIBLE)
    }

    /**
     * 아이템 클릭 이벤트 리스너 세팅 함수
     * @param p_listener OnListItemClickedEventListener
     */
    fun CE_setOnListItemClickedEventListener(p_listener: OnListItemClickedEventListener?) {
        listener = p_listener
    }

    /**
     * 해당 index 위치의 추천인 번호 반환
     * @param   p_index int
     * @return  String
     */
    fun CF_getReqRemnNo(p_index: Int): String {
        return adapter!!.CF_getRemnNo(p_index)
    }

    /**
     * 해당 index 위치의 추천인 번호 반환
     * @param   p_index int
     * @return  String
     */
    fun CF_getName(p_index: Int): String {
        return adapter!!.CF_getName(p_index)
    }

    // #############################################################################################
    //  Http 관련
    // #############################################################################################
    override fun handleMessage(p_message: Message) {
        if (getActivity() != null && !getActivity()!!.isDestroyed()) {
            (getActivity() as Activity_Default?)!!.CF_dismissProgressDialog()
            when (p_message.what) {
                HANDLERJOB_GET_LIST -> try {
                    setResultOfList(JSONObject(p_message.obj as String?))
                } catch (e: JSONException) {
                    LogPrinter.CF_line()
                    LogPrinter.CF_debug(getResources().getString(R.string.log_json_exception))
                }
                HANDLERJOB_ERROR_GET_LIST -> showDlgOfHttpError(p_message.obj as String, true)
            }
        }
    }

    /**
     * 추천인 목록 조회
     */
    fun requestList(flagFirstSearch: Boolean) {
        LogPrinter.CF_debug("!----------------------------------------------------------")
        LogPrinter.CF_debug("!-- IUII33M01_F.requestList() --추천인 목록조회")
        LogPrinter.CF_debug("!----------------------------------------------------------")
        (getActivity() as Activity_Default?)!!.CF_showProgressDialog()
        val tmp_srchDepartCode: String? = mActivity!!.CF_getDepartCode() // 추천국 코드
        val tmp_srchDepartName: String? = mActivity!!.CF_getDepartName() // 추천국 이름
        //String tmp_srchPersonName   = mActivity.CF_getPersonName();                 // 추천인 이름 (삭제예정)
        val tmp_srchPersonName: String = edtSerachName!!.getText().toString().trim({ it <= ' ' }) // 추천인 이름
        val tmp_srchIndex: Int = adapter!!.getCount()
        var tmp_srchKey: String = ""

        // --<1> (첫 조회)
        if (flagFirstSearch) {
            // -- 리스트뷰 보이기 전에 모두 숨김
            fl_ListCon!!.setVisibility(View.GONE)
            fl_ListConNodata!!.setVisibility(View.GONE)
            flag_firstSearch = true
        } else {
            tmp_srchKey = adapter!!.CF_getRemnNo(tmp_srchIndex - 1) // 리스트 마지막 고객번호 가져오기(전문 키로 사용함)
            flag_firstSearch = false
        }
        LogPrinter.CF_debug("###requestList >>>>>>tmp_srchKey:" + tmp_srchKey + "/adapter.getCount():" + adapter!!.getCount())

        // 필드 세팅
        tv_departName!!.setText(tmp_srchDepartName)
        LogPrinter.CF_debug("###requestList >>>>>>tmp_srchDepartCode:" + tmp_srchDepartCode + "/tmp_srchPersonName:" + tmp_srchPersonName + "/tmp_srchDepartName" + tmp_srchDepartName)
        val tmp_builder: Uri.Builder = Uri.Builder()
        tmp_builder.appendQueryParameter("s_brn_code", tmp_srchDepartCode) // 소속국 코드
        tmp_builder.appendQueryParameter("postOfficeAdmName", tmp_srchPersonName) // 모집자 이름
        tmp_builder.appendQueryParameter("s_search_key", tmp_srchKey) // 더보기 시작키(마지막 모집자번호)
        tmp_builder.appendQueryParameter("callDvsn", "1") // 요청구분(앱:1/웹:0)
        HttpConnections.sendPostData(
                EnvConfig.host_url + subUrl,
                tmp_builder.build().getEncodedQuery(),
                handler,
                HANDLERJOB_GET_LIST,
                HANDLERJOB_ERROR_GET_LIST)
    }

    /**
     * 추천국 목록 요청 결과 처리 함수
     * @param p_jsonObject  JSONObject
     */
    @Throws(JSONException::class)
    private fun setResultOfList(p_jsonObject: JSONObject) {
        LogPrinter.CF_debug("!----------------------------------------------------------")
        LogPrinter.CF_debug("!-- IUII33M01_F.setResultOfList() --추천국 목록 요청 결과 처리 함수")
        LogPrinter.CF_debug("!----------------------------------------------------------")
        val jsonKey_errorCode: String = "errCode"
        val jsonKey_data: String = "data"
        val jsonKey_arrPerson: String = "arr_person" // 추천국 목록
        val jsonKey_arrNextGb: String = "next_gb" // 다음페이지 존재 여부

        // --<1> result
        if (p_jsonObject.has(jsonKey_errorCode)) {
            val tmp_errorCode: String = p_jsonObject.getString(jsonKey_errorCode) // 에러코드 필드 초기화
            // --<2> result 에러 : ERRIUII33M01001 쿼리 오류
            if ((tmp_errorCode == "ERRIUII33M01001")) {
                showDlgOfHttpError(getResources().getString(R.string.dlg_error_recomm_person_list), true)
                mActivity!!.CF_setCurrentPage(0) // 추천국페이지로 이동
            } else if ((tmp_errorCode == "ERRIUII33M01002")) {
                //showDlgOfHttpError(getResources().getString(R.string.dlg_error_recomm_person_list), true); // 추천국페이지로 이동 (로직변경으로 주석)
                //mActivity.CF_setCurrentPage(0);                           // 추천국페이지로 이동 (로직변경으로 주석)
                fl_ListConNodata!!.setVisibility(View.VISIBLE) // 리스트뷰 표기 : nodata 뷰 표기
            } else if (p_jsonObject.has(jsonKey_data)) {
                val tmp_jsonData: JSONObject = p_jsonObject.getJSONObject(jsonKey_data)
                // --<3> 리스트 있음 : arrPerson 배열이 있을 경우
                if (tmp_jsonData.has(jsonKey_arrPerson)) {
                    fl_ListCon!!.setVisibility(View.VISIBLE) // 리스트뷰 표기 : 리스트 표기
                    // --<4> (더보기) 플래그 설정
                    if (tmp_jsonData.has(jsonKey_arrNextGb)) {
                        val tmp_hasNext: String = tmp_jsonData.getString("next_gb")
                        // --<5> (더보기 표기) 추가 리스트가 있을 경우
                        if (("1" == tmp_hasNext)) {
                            // --<6> 더보기 버튼이 없으면
                            if (!flag_addedFooter) {
                                listView!!.addFooterView(viewMore) // 더보기 버튼 추가
                                flag_addedFooter = true
                            }
                            // --<5> (더보기 미표기)  추가 리스트가 없을 경우
                        } else {
                            flag_addedFooter = false
                            listView!!.removeFooterView(viewMore)
                        }
                    }
                    // --<4> 추천인 데이터 세팅
                    val tmp_jsonArrPerson: JSONArray = tmp_jsonData.getJSONArray(jsonKey_arrPerson)
                    if (flag_firstSearch /*첫조회*/) {
                        data!!.CF_setData(tmp_jsonArrPerson) // 데이터 클래스 세팅
                    } else {
                        data!!.CF_addData(tmp_jsonArrPerson) // 데이터 클래스 추가
                    }
                    // -- 목록갱신
                    updateList()
                }
                // --<2> (에러)
            } else {
                showDlgOfHttpError(getResources().getString(R.string.dlg_error_server_2), true)
            }
            // --<1> (에러)
        } else {
            showDlgOfHttpError(getResources().getString(R.string.dlg_error_server_1), true)
        }
    }

    /**
     * 네트워크 오류 발생시 사용하는 다이얼로그 팝업
     * @param p_message     String
     * @param p_flagFinish  final boolean   if true, 다이얼로그 종료 시 Activity 종료
     */
    private fun showDlgOfHttpError(p_message: String, p_flagFinish: Boolean) {
        // 프로그레스 다이얼로그 dismiss
        (getActivity() as Activity_Default?)!!.CF_dismissProgressDialog()
        val tmp_dlg: CustomDialog = CustomDialog((getActivity())!!)
        tmp_dlg.show()
        tmp_dlg.CF_setBackKeyMode(CustomDialog.BackMode.NOTUSE)
        tmp_dlg.CF_setTextContent(p_message)
        tmp_dlg.CF_setSingleButtonText(getResources().getString(R.string.btn_ok))
        tmp_dlg.setOnDismissListener(object : DialogInterface.OnDismissListener {
            override fun onDismiss(dialog: DialogInterface) {
//                if(p_flagFinish) {
//                    // finish();
//                }
            }
        })
    }
}