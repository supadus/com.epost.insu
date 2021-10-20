package com.epost.insu.fragment

import android.annotation.SuppressLint
import android.content.Context
import android.content.DialogInterface
import android.net.Uri
import android.os.Bundle
import android.os.Message
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.*
import android.widget.TextView.OnEditorActionListener
import com.epost.insu.EnvConfig
import com.epost.insu.R
import com.epost.insu.activity.Activity_Default
import com.epost.insu.activity.IUII33M00_P
import com.epost.insu.adapter.RecommandDepartmentListAdapter
import com.epost.insu.common.CommonFunction
import com.epost.insu.common.LogPrinter
import com.epost.insu.data.Data_IUII33M00
import com.epost.insu.dialog.CustomDialog
import com.epost.insu.event.OnListItemClickedEventListener
import com.epost.insu.network.HttpConnections
import com.epost.insu.network.WeakReferenceHandler
import com.epost.insu.network.WeakReferenceHandler.ObjectHandlerMessage
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.util.*

/**
 * 보험금청구 > 보험금청구 > 추천국선택 팝업
 * @since     : project 30:1.2.8
 * @version   : 1.0
 * @author    : NJM
 * @see IUII33M00_P     추천국/추천인 Acitivity
 * <pre>
 * - 추천국 선택 화면
</pre> */
class IUII33M00_F constructor() : Fragment_Default(), ObjectHandlerMessage {
    private var mActivity: IUII33M00_P? = null
    private val subUrl: String = "/MY/IUMYS0M04.do" // 추천국 목록 조회
    private val HANDLERJOB_GET_LIST: Int = 0 // 목록 요청 성공 코드
    private val HANDLERJOB_ERROR_GET_LIST: Int = 1 // 목록 요청 에러 코드
    private var adapter: RecommandDepartmentListAdapter? = null // 추천국 목록 Adapter
    private var data: Data_IUII33M00? = null // 추천국 목록 데이터
    private var handler: WeakReferenceHandler? = null
    private var listener: OnListItemClickedEventListener? = null // 리스트 아이템 클릭 리스너


    //TODO NJM 탭뷰 구현 or 처리필요
    //private CustomTabView tabView;                    // 상단 추천국/추천인 구분 TabView
    /* 추천국 */
    private var edtSerachName: EditText? = null // 추천국
    private val maxLengthDepartName: Int = 40 // 추천국 명 max length
    private var listView: ListView? = null // 추천인 목록
    private var fl_ListConNodata: FrameLayout? = null // 리스트 : 노데이터
    private var fl_ListCon: FrameLayout? = null // 리스트
    var tmp_linSearchNameBtn: ImageButton? = null // 조회버튼 : 이벤트 할당용 선언


    override fun onAttach(context: Context) {
        super.onAttach(context)
        LogPrinter.CF_debug("!----------------------------------------------------------")
        LogPrinter.CF_debug("!-- IUII33M00_F.onAttach()")
        LogPrinter.CF_debug("!----------------------------------------------------------")
        mActivity = getActivity() as IUII33M00_P?
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        LogPrinter.CF_debug("!----------------------------------------------------------")
        LogPrinter.CF_debug("!-- IUII33M00_F.onCreate()")
        LogPrinter.CF_debug("!----------------------------------------------------------")
        data = Data_IUII33M00()
        handler = WeakReferenceHandler(this)
        adapter = RecommandDepartmentListAdapter((getActivity())!!, R.layout.list_recomm_depart)
        adapter!!.CE_setOnListItemClickedEventListener(object : OnListItemClickedEventListener {
            override fun onClick(p_index: Int) {
                if (listener != null) {
                    listener!!.onClick(p_index)
                }
            }
        })
        handler = WeakReferenceHandler(this)
    }

    @SuppressLint("InflateParams")
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        LogPrinter.CF_debug("!----------------------------------------------------------")
        LogPrinter.CF_debug("!-- IUII33M00_F.onCreateView()")
        LogPrinter.CF_debug("!----------------------------------------------------------")
        return inflater.inflate(R.layout.iuii33m00_f, null)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        LogPrinter.CF_debug("!----------------------------------------------------------")
        LogPrinter.CF_debug("!-- IUII33M00_F.onActivityCreated()")
        LogPrinter.CF_debug("!----------------------------------------------------------")
        setBundleData(savedInstanceState)
        setUIControl() // UI 생성 및 세팅
    }

    /**
     * Bundle 데이터 세팅 함수
     * @param p_bundle  Bundle
     */
    private fun setBundleData(p_bundle: Bundle?) {
        if (p_bundle != null) {
            if (p_bundle.containsKey("data")) {
                data = p_bundle.getParcelable("data")
            }
        }
    }

    /**
     * UI 생성 및 세팅 함수
     */
    fun setUIControl() {
        LogPrinter.CF_debug("!----------------------------------------------------------")
        LogPrinter.CF_debug("!-- IUII33M00_F.setUIControl()")
        LogPrinter.CF_debug("!----------------------------------------------------------")
        val tmp_linSearchName: LinearLayout = getView()!!.findViewById(R.id.labelEditSearchName) // 추천국조회 폼
        val tmp_labelName: TextView = tmp_linSearchName?.findViewById(R.id.label) // 라벨 세팅
        tmp_linSearchNameBtn = getView()!!.findViewById(R.id.labelEditSearchNameBtn)

        // 라벨 LayoutParam 설정
        val tmp_lpLabel: LinearLayout.LayoutParams = tmp_labelName.getLayoutParams() as LinearLayout.LayoutParams
        tmp_lpLabel.width = CommonFunction.CF_convertDipToPixel(getActivity()!!.getApplicationContext(), 75f)
        tmp_labelName.setLayoutParams(tmp_lpLabel)
        ////tmp_labelCode.setLayoutParams(tmp_lpLabel);
        tmp_labelName.setPadding(
                CommonFunction.CF_convertDipToPixel(
                        getActivity()!!.getApplicationContext(), 10f),
                tmp_labelName.getPaddingTop(),
                tmp_labelName.getPaddingRight(),
                tmp_labelName.getPaddingBottom()
        ) // 라벨 Pading 설정

        ////tmp_labelCode.setPadding(CommonFunction.CF_convertDipToPixel(getActivity().getApplicationContext(),10), tmp_labelCode.getPaddingTop(), tmp_labelCode.getPaddingRight(), tmp_labelCode.getPaddingBottom());
        tmp_labelName.setText(getResources().getString(R.string.label_recomm_depart_name)) // 라벨 Text 설정
        ////tmp_labelCode.setText(getResources().getString(R.string.label_disease_code_n));

        // -- 추천국 입력 EditText 설정
        edtSerachName = tmp_linSearchName.findViewById(R.id.edit)
        edtSerachName?.setFilters(CommonFunction.CF_getInputLengthFilter(maxLengthDepartName))
        edtSerachName?.setHint(R.string.hint_recomm_depart_input)
        edtSerachName?.setText(mActivity!!.CF_getSrchDepartName())
        edtSerachName?.requestFocus()
        edtSerachName?.setImeOptions(EditorInfo.IME_ACTION_SEARCH)
        edtSerachName?.setOnEditorActionListener(object : OnEditorActionListener {
            override fun onEditorAction(v: TextView, actionId: Int, event: KeyEvent?): Boolean {

                // 소프트 조회버튼(검색) 클릭 이벤트
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    tmp_linSearchNameBtn?.performClick()
                }
                return false
            }
        })

        // -- 조회 버튼
        tmp_linSearchNameBtn?.setOnClickListener(object : View.OnClickListener {
            override fun onClick(v: View) {
                LogPrinter.CF_debug("조회버튼 클릭 : " + edtSerachName?.getText().toString().trim({ it <= ' ' }))
                LogPrinter.CF_debug("###글자수 : " + edtSerachName?.getText().toString().trim({ it <= ' ' }).length)

                // 공백 체크
                if (("" == edtSerachName?.getText().toString().trim({ it <= ' ' }))) {
                    mActivity!!.showCustomDialog(getResources().getString(R.string.dig_no_select_recomm_depart_name), edtSerachName!!)

                    // "우체", "우체국" 키워드 검색 금지
                } else if (("우체" == edtSerachName?.getText().toString().trim({ it <= ' ' })) || ("우체국" == edtSerachName?.getText().toString().trim({ it <= ' ' }))) {
                    mActivity!!.showCustomDialog(getResources().getString(R.string.dig_wrong_select_recomm_depart_name1), edtSerachName!!)

                    // 두글자 이상 검색
                } else if (edtSerachName?.getText().toString().trim({ it <= ' ' }).length <= 1) {
                    mActivity!!.showCustomDialog(getResources().getString(R.string.dig_wrong_select_recomm_depart_name2), edtSerachName!!)

                    // 정상
                } else {
                    requestList()
                }
            }
        })

        // 추천국 / 추천인 선택 TabView
//        tabView = (CustomTabView)findViewById(R.id.customTabView);
//        tabView.CF_setTabText(getResources().getString(R.string.btn_recomm_depart), getResources().getString(R.string.btn_recomm_person));
//        tabView.CF_setTabBackground(R.drawable.btn_navy_left_selector, R.drawable.btn_navy_right_selector);
//        tabView.CE_setOnSelectedChangeEventListener(new OnSelectedChangeEventListener() {
//            @Override
//            public void onSelected(int p_index) {
//                //setAdapterData();
//            }
//        });

        // -- 리스트뷰
        fl_ListCon = getView()!!.findViewById(R.id.fl_ListCon) // 리스트뷰 Con
        fl_ListConNodata = getView()!!.findViewById(R.id.fl_ListConNodata) // 노데이터 리스브뷰 Con

        // 초기에 NoData 뷰
        //fl_ListCon.setVisibility(View.GONE);
        //fl_ListConNodata.setVisibility(View.GONE);

        // 추천국/추천인 출력 ListView
        listView = getView()!!.findViewById(R.id.listView)
        listView?.setAdapter(adapter)
    }

    override fun onResume() {
        super.onResume()
        LogPrinter.CF_debug("!----------------------------------------------------------")
        LogPrinter.CF_debug("!-- IUII33M00_F.onResume()")
        LogPrinter.CF_debug("!----------------------------------------------------------")

        // 조회값 유무에 따른 분기
        if (("" == edtSerachName!!.getText().toString().trim({ it <= ' ' }))) {
            LogPrinter.CF_debug("!-- -1- edtSerachName 조회값 없음 : 빈페이지 출력")

            // 조회값이 있을 경우 : http 요청
        } else {
            LogPrinter.CF_debug("!-- -1- edtSerachName 조회값 있음 :  " + edtSerachName!!.getText().toString().trim({ it <= ' ' }))
            requestList()
        }

//        else if(data.CF_getDataCount() > 0 && adapter.getCount() == 0){
//            updateList();
//        }

//        if(dataDepartment.CF_getDataCount()==0 && dataPerson.CF_getDataCount() == 0){
//            // 데이터 요청
//            requestList();
//        }
//        else if(adapter.getCount() == 0){
//            setadapterata();
//        }
    }

    //TODO NJM onSaveInstanceState 작업필요
    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        LogPrinter.CF_debug("!----------------------------------------------------------")
        LogPrinter.CF_debug("!-- IUII33M00_F.onSaveInstanceState()")
        LogPrinter.CF_debug("!----------------------------------------------------------")
        outState.putParcelable("dataDepartment", data) // 추천국 목록 데이터
        //outState.putInt("tabIndex", tabView.CF_getCurrentIndex());      // 탭뷰 인덱스
    }

    /**
     * Adapter 데이터 세팅 함수<br></br>
     */
    private fun updateList() {
        adapter!!.CF_setData(data!!.CF_getDepartmentCodeList(), data!!.CF_getDepartmentNameList(), data!!.CF_getDepartmentAddrList())
    }

    /**
     * 아이템 클릭 이벤트 리스너 세팅 함수
     * @param p_listener OnListItemClickedEventListener
     */
    fun CE_setOnListItemClickedEventListener(p_listener: OnListItemClickedEventListener?) {
        listener = p_listener
    }

    /**
     * 해당 index 위치의 청구접수 ID 반환
     * @param p_index int
     * @return String
     */
    fun CF_getReqName(p_index: Int): String {
        return adapter!!.CF_getName(p_index)
    }

    /**
     * 해당 index 위치의 청구접수 ID 반환
     * @param p_index int
     * @return String
     */
    fun CF_getReqCode(p_index: Int): String {
        return adapter!!.CF_getCode(p_index)
    }

    fun CF_getSrchDepartName(): String {
        return edtSerachName!!.getText().toString().trim({ it <= ' ' })
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
     * 추천국 목록 조회
     */
    private fun requestList() {
        LogPrinter.CF_debug("!----------------------------------------------------------")
        LogPrinter.CF_debug("!-- IUII33M00_F.requestList()")
        LogPrinter.CF_debug("!----------------------------------------------------------")
        (getActivity() as Activity_Default?)!!.CF_showProgressDialog()

        // -- 리스트뷰 초기 숨김
        fl_ListCon!!.setVisibility(View.GONE)
        fl_ListConNodata!!.setVisibility(View.GONE)
        val searchName: String = edtSerachName!!.getText().toString().trim({ it <= ' ' })
        LogPrinter.CF_debug("!---- searchName:" + searchName)
        val tmp_builder: Uri.Builder = Uri.Builder()
        tmp_builder.appendQueryParameter("callDvsn", "1")
        tmp_builder.appendQueryParameter("postOfficeName", searchName)
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
        LogPrinter.CF_debug("!-- IUII33M00_F.setResultOfList()")
        LogPrinter.CF_debug("!----------------------------------------------------------")
        val jsonKey_errorCode: String = "errCode"
        val jsonKey_data: String = "data"
        val jsonKey_arrDepartment: String = "arr_department" // 추천국 목록

        // --<1>
        if (p_jsonObject.has(jsonKey_errorCode)) {
            val tmp_errorCode: String = p_jsonObject.getString(jsonKey_errorCode) // 에러코드 필드 초기화
            if ((tmp_errorCode == "ERRIUII33M00001")) {
                LogPrinter.CF_debug("!--<2> result 에러 : ERRIUII33M00001 쿼리오류시")
                showDlgOfHttpError(getResources().getString(R.string.dlg_error_recomm_depart_list), true)
                fl_ListConNodata!!.setVisibility(View.VISIBLE)
            } else if ((tmp_errorCode == "ERRIUII33M00002")) {
                LogPrinter.CF_debug("!--<2> result 에러 : ERRIUII33M00002 소속국이 없는 경우")
                fl_ListConNodata!!.setVisibility(View.VISIBLE)
            } else if (p_jsonObject.has(jsonKey_data)) {
                val tmp_jsonData: JSONObject = p_jsonObject.getJSONObject(jsonKey_data)
                if (tmp_jsonData.has(jsonKey_arrDepartment)) {
                    // 리스트뷰 표기 : 리스트 표기
                    fl_ListCon!!.setVisibility(View.VISIBLE)

                    // 데이터 세팅
                    val tmp_jsonArrDepartment: JSONArray = tmp_jsonData.getJSONArray(jsonKey_arrDepartment)
                    data!!.CF_setData(tmp_jsonArrDepartment) // 데이터 클래스 세팅
                    updateList() // 목록갱신
                } else {
                    showDlgOfHttpError(getResources().getString(R.string.dlg_error_server_2), true)
                }
            } else {
                showDlgOfHttpError(getResources().getString(R.string.dlg_error_server_2), true)
            }
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
        LogPrinter.CF_debug("!----------------------------------------------------------")
        LogPrinter.CF_debug("!-- IUII33M00_F.showDlgOfHttpError()")
        LogPrinter.CF_debug("!----------------------------------------------------------")

        // 프로그레스 다이얼로그 dismiss
        //CF_dismissProgressDialog();
        val tmp_dlg: CustomDialog = CustomDialog((getActivity())!!)
        tmp_dlg.show()
        tmp_dlg.CF_setBackKeyMode(CustomDialog.BackMode.NOTUSE)
        tmp_dlg.CF_setTextContent(p_message)
        tmp_dlg.CF_setSingleButtonText(getResources().getString(R.string.btn_ok))
        tmp_dlg.setOnDismissListener(object : DialogInterface.OnDismissListener {
            override fun onDismiss(dialog: DialogInterface) {
//                if(p_flagFinish) {
//                   // finish();
//                }
            }
        })
    }
}