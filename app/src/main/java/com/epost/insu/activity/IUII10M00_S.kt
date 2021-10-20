package com.epost.insu.activity

import android.os.Bundle
import android.os.Message
import android.view.View
import android.widget.ImageButton
import android.widget.TextView
import com.epost.insu.EnvConfig
import com.epost.insu.R
import com.epost.insu.SharedPreferencesFunc
import com.epost.insu.common.CommonFunction
import com.epost.insu.common.CustomSQLiteFunction
import com.epost.insu.common.LogPrinter
import com.epost.insu.dialog.CustomDialog
import com.epost.insu.module.PSMobileModule
import com.epost.insu.network.HttpConnections
import com.epost.insu.network.WeakReferenceHandler
import com.epost.insu.network.WeakReferenceHandler.ObjectHandlerMessage
import org.json.JSONException
import org.json.JSONObject
import java.io.File
import java.util.*

/**
 * 보험금청구 > 지급진행조회 및 서류보완 > 구비서류첨부 보완
 * @since     :
 * @version   : 1.3
 * @author    : LSH
 * <pre>
 * 보험금지급청구 후 구비서류 보완 요청시 사용
 * ======================================================================
 * LSH_20171117    최초 등록
 * NJM_20201028    [모바일 사진촬영 패키지 도입]
 * 1.5.2    NJM_20210318    [액티비티 재실행시 사진첨부오류(~준비되지 않은) 수정] psMobileModule 호출 위치 변경
 * 1.5.2    NJM_20210322    [subUrl 공통파일로 변경]
 * =======================================================================
 * copyright : (주)프로니스정보기술 | http://www.phronis.co.kr
</pre> *
 */
class IUII10M00_S : Activity_Default(), ObjectHandlerMessage {
    private val HANDLERJOB_SUBMIT = 0
    private val HANDLERJOB_ERROR_SUBMIT = 1

    private var psMobileModule: PSMobileModule? = null // 서류첨부 솔루션 제어
    private var s_requ_recp_id: String? = null // 청구접수번호
    private var s_csno: String? = null // 고객번호 (휴대폰본인인증)
    private var bSelfReqInquery: Boolean? = null // 휴대폰인증청구내역조회

    override fun setInit() {
        setContentView(R.layout.iuii10m00_s)
        s_requ_recp_id = ""
        s_csno = ""
        bSelfReqInquery = false
        handler = WeakReferenceHandler(this)
    }

    override fun setUIControl() {

        // 타이틀바 세팅
        setTitleBarUI()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // -- Intent 데이터 세팅 함수
        setIntentData()

        // -- 서류첨부 솔루션
        psMobileModule = PSMobileModule(applicationContext)
    }

    override fun onBackPressed() {
        showDlgOfCancel()
    }

    override fun onDestroy() {
        super.onDestroy()
        LogPrinter.CF_debug("!----------------------------------------------------------")
        LogPrinter.CF_debug("!-- IUII10M00_S.onDestroy()")
        LogPrinter.CF_debug("!----------------------------------------------------------")
        psMobileModule!!.clearFiles() // 임시파일(이미지) 삭제
    }

    /**
     * Intent 데이터 세팅 함수
     */
    private fun setIntentData() {
        if (intent != null) {
            if (intent.hasExtra("s_requ_recp_id")) {
                s_requ_recp_id = intent.extras!!.getString("s_requ_recp_id")
            }
            if (intent.hasExtra("s_csno")) {
                s_csno = intent.extras!!.getString("s_csno")
            }
            if (intent.hasExtra("bSelfReqInquery")) {
                bSelfReqInquery = intent.extras!!.getBoolean("bSelfReqInquery")
            }
        }
    }

    /**
     * 타이틀바 레이아웃 세팅
     */
    private fun setTitleBarUI() {
        // 타이틀 세팅
        val tmp_title = findViewById<TextView>(R.id.title_bar_textTitle)
        tmp_title.text = resources.getString(R.string.title_add_docs)

        // left 버튼 세팅
        val tmp_btn = findViewById<ImageButton>(R.id.title_bar_imgBtnLeft)
        tmp_btn.visibility = View.VISIBLE
        tmp_btn.setOnClickListener { showDlgOfCancel() }
    }
    // #############################################################################################
    //  구비서류 이미지 파일 처리
    // #############################################################################################
    /**
     * 파일 업로드를 위한 파일 목록 반환
     * @param p_arrFIlePath ArrayList<String>
     * @return              ArrayList<File>
    </File></String> */
    private fun getFIleList(p_arrFIlePath: ArrayList<String>): ArrayList<File> {
        val tmp_arrFile = ArrayList<File>()
        for (p_path in p_arrFIlePath) {
            val tmp_file = File(p_path)
            if (tmp_file.exists()) {
                tmp_arrFile.add(tmp_file)
            }
        }
        return tmp_arrFile
    }

    /**
     * 파일 업르드를 위한 파일 키 리스트 반환
     * @param p_arrFile ArrayList<File>
     * @return          ArrayList<String>
    </String></File> */
    private fun getFileKeyList(p_arrFile: ArrayList<File>): ArrayList<String> {
        val tmp_arrKey = ArrayList<String>()
        for (i in p_arrFile.indices) {
            tmp_arrKey.add("img" + (i + 1))
        }
        return tmp_arrKey
    }
    // #############################################################################################
    //  다이얼로그 관련
    // #############################################################################################
    /**
     * 보험금청구신청 취소 다이얼로그 show 함수
     */
    private fun showDlgOfCancel() {
        val tmp_dlg = CustomDialog(this)
        tmp_dlg.show()
        tmp_dlg.CF_setBackKeyMode(CustomDialog.BackMode.CANCELED)
        tmp_dlg.CF_setTextContent(resources.getString(R.string.dlg_cancel_add_doc))
        tmp_dlg.CF_setDoubleButtonText(resources.getString(R.string.btn_no), resources.getString(R.string.btn_yes))
        tmp_dlg.setOnDismissListener { dialog ->
            if (!(dialog as CustomDialog).CF_getCanceled()) {
                // -- 서류첨부 솔루션 헬퍼 : 임시파일(이미지) 삭제
                val psMobileModule = PSMobileModule(applicationContext)
                psMobileModule.clearFiles()
                setResult(RESULT_CANCELED)
                finish()
            }
        }
    }

    // #############################################################################################
    //  Http 관련
    // #############################################################################################
    override fun handleMessage(p_message: Message) {
        if (!isDestroyed) {
            CF_dismissProgressDialog()
            when (p_message.what) {
                HANDLERJOB_SUBMIT -> try {
                    setResultOfSubmit(JSONObject(p_message.obj as String))
                } catch (e: JSONException) {
                    CommonFunction.CF_showCustomAlertDilaog(this, resources.getString(R.string.dlg_error_erriuii40m01001), resources.getString(R.string.btn_ok))
                    LogPrinter.CF_debug(resources.getString(R.string.log_json_exception) + " : " + p_message.obj)
                }
                HANDLERJOB_ERROR_SUBMIT -> CommonFunction.CF_showCustomAlertDilaog(this, p_message.obj as String, resources.getString(R.string.btn_ok))
                else -> {
                }
            }
        }
    }

    /**
     * 보험금청구접수 구비서류 보완 요청
     */
    fun CF_requestSubmit(p_arrFilePath: ArrayList<String>) {
        CF_showProgressDialog()
        val tmp_arrFile = getFIleList(p_arrFilePath) // 업로드 파일
        val tmp_arrKey = getFileKeyList(tmp_arrFile) // 업로드 파일키
        val tmp_map = HashMap<String, String?>()
        tmp_map["s_requ_recp_id"] = s_requ_recp_id
        tmp_map["imgCnt"] = "" + tmp_arrFile.size
        if (bSelfReqInquery!!) {
            tmp_map["s_acdp_csno"] = s_csno // 휴대폰인증청구
        } else {
            tmp_map["s_acdp_csno"] = CustomSQLiteFunction.getLastLoginCsno(applicationContext) // 공동인증로그인
        }
        tmp_map["tempKey"] = SharedPreferencesFunc.getWebTempKey(applicationContext)
        HttpConnections.sendMultiData(EnvConfig.host_url + EnvConfig.URL_CLAIM_DOC_REQ,
                tmp_arrKey,
                tmp_arrFile,
                tmp_map,
                HANDLERJOB_SUBMIT,
                HANDLERJOB_ERROR_SUBMIT,
                handler
        )
    }

    /**
     * 보험금청구 접수 구비서류 보완 처리 결과
     * @param p_jsonObject  JSONObject
     */
    @Throws(JSONException::class)
    private fun setResultOfSubmit(p_jsonObject: JSONObject) {
        val jsonKey_errorCode = "errCode"
        val jsonKey_data = "data"
        val tmp_errorCode: String
        if (p_jsonObject.has(jsonKey_errorCode)) {
            tmp_errorCode = p_jsonObject.getString(jsonKey_errorCode)
            if (tmp_errorCode == "ERRIUII40M01001") {
                CommonFunction.CF_showCustomAlertDilaog(this, resources.getString(R.string.dlg_error_erriuii40m01001), resources.getString(R.string.btn_ok))
            } else if (tmp_errorCode == "ERRIUII40M01002") {
                CommonFunction.CF_showCustomAlertDilaog(this, resources.getString(R.string.dlg_error_erriuii40m01002), resources.getString(R.string.btn_ok))
            } else if (tmp_errorCode == "ERRIUII40M01003") {
                CommonFunction.CF_showCustomAlertDilaog(this, resources.getString(R.string.dlg_error_erriuii40m01002), resources.getString(R.string.btn_ok))
            } else if (p_jsonObject.has(jsonKey_data)) {
                val tmp_dlg = CustomDialog(this)
                tmp_dlg.show()
                tmp_dlg.CF_setBackKeyMode(CustomDialog.BackMode.CANCELED)
                tmp_dlg.CF_setTextContent(resources.getString(R.string.dlg_complete_add_photo))
                tmp_dlg.CF_setSingleButtonText(resources.getString(R.string.btn_ok))
                tmp_dlg.setOnDismissListener { dialog ->
                    if (!(dialog as CustomDialog).CF_getCanceled()) {
                        // -- 서류첨부 솔루션 헬퍼 : 임시파일(이미지) 삭제
                        val psMobileModule = PSMobileModule(applicationContext)
                        psMobileModule.clearFiles()
                        setResult(RESULT_OK)
                        finish()
                    }
                }
            } else {
                CommonFunction.CF_showCustomAlertDilaog(this, resources.getString(R.string.dlg_error_server_2), resources.getString(R.string.btn_ok))
            }
        } else {
            CommonFunction.CF_showCustomAlertDilaog(this, resources.getString(R.string.dlg_error_server_1), resources.getString(R.string.btn_ok))
        }
    }
}