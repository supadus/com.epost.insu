package com.epost.insu.fragment

import android.annotation.SuppressLint
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Message
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ListView
import com.epost.insu.EnvConfig
import com.epost.insu.EnvConfig.AuthDvsn
import com.epost.insu.R
import com.epost.insu.SharedPreferencesFunc
import com.epost.insu.activity.Activity_Default
import com.epost.insu.activity.IUII50M00_P
import com.epost.insu.adapter.ReqBenefitListAdapter
import com.epost.insu.common.CommonFunction
import com.epost.insu.common.LogPrinter
import com.epost.insu.data.Data_IUII50M00_F_P
import com.epost.insu.dialog.CustomDialog
import com.epost.insu.event.OnListItemClickedEventListener
import com.epost.insu.network.HttpConnections
import com.epost.insu.network.WeakReferenceHandler
import com.epost.insu.network.WeakReferenceHandler.ObjectHandlerMessage
import org.json.JSONException
import org.json.JSONObject
import java.util.*

/**
 * 보험금청구 > 보험금청구 > 보험금청구조회
 * @since     :
 * @version   : 1.3
 * @author    : LSH
 * <pre>
 * ======================================================================
 * LSH_20171116    최초 등록
 * NJM_20200122    공통 인증유형/청구유형 추가에 따른 로직수정
 * NJM_20200304    보험금청구조회에서 이미지 조회 및 접수취소 기능 추가
 * 1.5.2    NJM_20210322    [subUrl 공통파일로 변경]
 * =======================================================================
 * copyright : (주)프로니스정보기술 | http://www.phronis.co.kr
</pre> *
 */
class IUII50M00_F constructor() : Fragment_Default(), ObjectHandlerMessage {
    private val HANDLERJOB_LIST: Int = 0
    private val HANDLERJOB_ERROR_LIST: Int = 1

    private var listener: OnListItemClickedEventListener? = null // 리스트 아이템 클릭 리스너

    private var linList: LinearLayout? = null
    private var linEmpty: LinearLayout? = null
    private var data: Data_IUII50M00_F_P? = null
    private var adapter: ReqBenefitListAdapter? = null
    private var handler: WeakReferenceHandler? = null
    private var flagReqData: Boolean = false
    private var mActivity: IUII50M00_P? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mActivity = getActivity() as IUII50M00_P?
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setInit() // 초기 세팅
    }

    @SuppressLint("InflateParams")
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.iuii50m00_f, null)
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
        setBundleData(savedInstanceState)
        setUIControl() // UI 생성 및 세팅
    }

    // onStart()
    override fun onResume() {
        super.onResume()
        if (!flagReqData) {
            httpReq_claimList()
        } else if (data!!.CF_getDataCount() > 0 && adapter!!.getCount() == 0) {
            updateList()
        }
    }

    // onPause()
    override fun onStop() {
        super.onStop()
    }

    // onDestroyView()
    override fun onDestroy() {
        super.onDestroy()
    }

    // onDetach()
    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putBoolean("flagReqData", flagReqData)
        outState.putParcelable("data", data)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
    }
    // #############################################################################################
    //
    // #############################################################################################
    /**
     * 초기 세팅 함수
     */
    private fun setInit() {
        flagReqData = false
        data = Data_IUII50M00_F_P()
        adapter = ReqBenefitListAdapter((getActivity())!!, R.layout.list_req_benefit)
        adapter!!.CE_setOnListItemClickedEventListener(object : OnListItemClickedEventListener {
            override fun onClick(p_index: Int) {
                if (listener != null) {
                    listener!!.onClick(p_index)
                }
            }
        })
        handler = WeakReferenceHandler(this)
    }

    /**
     * UI 생성 및 세팅 함수
     */
    private fun setUIControl() {
        linList = getView()?.findViewById(R.id.linResult)
        linEmpty = getView()!!.findViewById(R.id.linEmpty)
        val tmp_listView: ListView = getView()!!.findViewById(R.id.listView)
        tmp_listView.setAdapter(adapter)
        val tmp_btnRetry: Button = getView()!!.findViewById(R.id.btnFill)
        tmp_btnRetry.setText(getResources().getString(R.string.btn_retry_search))
        tmp_btnRetry.setOnClickListener(object : View.OnClickListener {
            override fun onClick(v: View) {
                httpReq_claimList() // 보험금청구 조회 목록 요청
            }
        })
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
            if (p_bundle.containsKey("flagReqData")) {
                flagReqData = p_bundle.getBoolean("flagReqData")
            }
        }
    }

    /**
     * 지급청구 목록 갱신
     */
    private fun updateList() {
        adapter!!.CF_setData(data!!.CF_getDataList())
        if (adapter!!.getCount() > 0) {
            linList!!.setVisibility(View.VISIBLE)
            linEmpty!!.setVisibility(View.GONE)
        } else {
            linList!!.setVisibility(View.GONE)
            linEmpty!!.setVisibility(View.VISIBLE)
        }
    }

    /**
     * 아이템 클릭 이벤트 리스너 세팅 함수
     * @param p_listener    OnListItemClickedEventListener
     */
    fun CE_setOnListItemClickedEventListener(p_listener: OnListItemClickedEventListener?) {
        listener = p_listener
    }

    /**
     * Activity 종료 다이얼로그 show 함수
     * @param p_message String
     */
    private fun showDlgOfFinish(p_message: String) {
        val tmp_dlg: CustomDialog = CustomDialog((Objects.requireNonNull(getActivity()))!!)
        tmp_dlg.show()
        tmp_dlg.CF_setTextContent(p_message)
        tmp_dlg.CF_setSingleButtonText(getResources().getString(R.string.btn_ok))
        tmp_dlg.setOnDismissListener(object : DialogInterface.OnDismissListener {
            override fun onDismiss(dialog: DialogInterface) {
                getActivity()!!.finish()
            }
        })
    }

    /**
     * 해당 index 위치의 청구접수 ID 반환
     * @param p_index   int
     * @return          String
     */
    fun CF_getReqId(p_index: Int): String {
        return adapter!!.CF_getReqId(p_index)
    }

    /**
     * 구비서류보완 완료 업데이트
     * @param p_reqId   String
     */
    fun CF_updateAddDocComplete(p_reqId: String?) {
        data!!.CF_updateFlagNeedMod(p_reqId)
        adapter!!.CF_updateFlagAddDoc((p_reqId)!!)
        adapter!!.notifyDataSetChanged()
    }

    // #############################################################################################
    //  Http 관련
    // #############################################################################################
    override fun handleMessage(p_message: Message) {
        if (getActivity() != null && !getActivity()!!.isDestroyed()) {
            (getActivity() as Activity_Default?)!!.CF_dismissProgressDialog()
            when (p_message.what) {
                HANDLERJOB_LIST -> try {
                    httpRes_claimList(JSONObject(p_message.obj as String?))
                } catch (e: JSONException) {
                    LogPrinter.CF_line()
                    LogPrinter.CF_debug(getResources().getString(R.string.log_json_exception))
                }
                HANDLERJOB_ERROR_LIST -> showDlgOfFinish(p_message.obj as String)
                else -> {
                }
            }
        }
    }

    /**
     * (HTTP요청) 보험금청구접수 목록 요청 함수
     */
    fun httpReq_claimList() {
        LogPrinter.CF_debug("!----------------------------------------------------------")
        LogPrinter.CF_debug("!-- IUII50M00_F.requestList() -- HTTP 지급청구 목록요청")
        LogPrinter.CF_debug("!----------------------------------------------------------")
        flagReqData = true
        (Objects.requireNonNull(getActivity()) as Activity_Default).CF_showProgressDialog()
        linList!!.setVisibility(View.GONE)
        linEmpty!!.setVisibility(View.GONE)
        val reqUrl: String
        val tmp_builder: Uri.Builder = Uri.Builder()
        tmp_builder.appendQueryParameter("s_acdp_csno", mActivity!!.CF_getCsno())

        // -- (휴대폰인증)
        if (mActivity!!.CF_getAuthDvsn() == AuthDvsn.MOBILE) {
            LogPrinter.CF_debug("!--<> 휴대폰인증 청구내역조회:::: " + mActivity!!.CF_getCsno())
            reqUrl = EnvConfig.host_url + EnvConfig.URL_CLAIM_MOBILE_LIST_INQ
        } else {
            LogPrinter.CF_debug("!--<> 기타인증(휴대폰 외) 청구내역조회:::: " + mActivity!!.CF_getCsno())
            tmp_builder.appendQueryParameter("tempKey", SharedPreferencesFunc.getWebTempKey(getActivity()))
            reqUrl = EnvConfig.host_url + EnvConfig.URL_CLAIM_CERT_LIST_INQ
        }
        HttpConnections.sendPostData(
                reqUrl,
                tmp_builder.build().getEncodedQuery(),
                handler,
                HANDLERJOB_LIST,
                HANDLERJOB_ERROR_LIST)
    }

    /**
     * (HTTP결과) 보험금청구접수 목록 요청 결과 처리 함수
     * @param p_jsonObject  JSONObject
     */
    @Throws(JSONException::class)
    private fun httpRes_claimList(p_jsonObject: JSONObject) {
        LogPrinter.CF_debug("!----------------------------------------------------------")
        LogPrinter.CF_debug("!-- IUII50M00_F.setResultOfList()")
        LogPrinter.CF_debug("!----------------------------------------------------------")
        val jsonKey_errorCode: String = "errCode"
        val jsonKey_data: String = "data"
        val tmp_errorCode: String
        if (p_jsonObject.has(jsonKey_errorCode)) {
            tmp_errorCode = p_jsonObject.getString(jsonKey_errorCode)
            if ((tmp_errorCode == "ERRIUII50M00001")) {        // 입력값 오류
                showDlgOfFinish(getResources().getString(R.string.dlg_error_erriuii50M00001))
            } else if ((tmp_errorCode == "ERRIUII50M00002")) {       // 전문 오류
                showDlgOfFinish(getResources().getString(R.string.dlg_error_erriuii50M00002))
            } else if (p_jsonObject.has(jsonKey_data)) {
                data!!.CF_setData(p_jsonObject.getJSONArray(jsonKey_data))

                // -- 목록 갱신
                updateList()
            } else {
                CommonFunction.CF_showCustomAlertDilaog(getActivity(), getResources().getString(R.string.dlg_error_server_2), getResources().getString(R.string.btn_ok))
            }
        } else {
            CommonFunction.CF_showCustomAlertDilaog(getActivity(), getResources().getString(R.string.dlg_error_server_1), getResources().getString(R.string.btn_ok))
        }
    }
}