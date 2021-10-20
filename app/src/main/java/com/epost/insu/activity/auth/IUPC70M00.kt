package com.epost.insu.activity.auth

import android.content.DialogInterface
import android.net.Uri
import android.os.Bundle
import android.os.Message
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import com.epost.insu.EnvConfig
import com.epost.insu.R
import com.epost.insu.activity.Activity_Default
import com.epost.insu.common.CommonFunction
import com.epost.insu.common.CustomSQLiteFunction
import com.epost.insu.common.LogPrinter
import com.epost.insu.dialog.CustomDialog
import com.epost.insu.network.HttpConnections
import com.epost.insu.network.WeakReferenceHandler
import com.epost.insu.network.WeakReferenceHandler.ObjectHandlerMessage
import org.json.JSONException
import org.json.JSONObject
import java.util.*

/**
 * 추가인증(SMS/ARS)
 * @since     :
 * @version   : 1.1
 * @author    : NJM
 * <pre>
 * ======================================================================
 * NJM_20201229    최초 등록
 * 1.5.2    NJM_20210322    [subUrl 공통파일로 변경]
 * =======================================================================
 * copyright : (주)프로니스정보기술 | http://www.phronis.co.kr
</pre> *
 */
class IUPC70M00() : Activity_Default(), ObjectHandlerMessage {
    private val HANDLERJOB_UPDATE_TIME = 0
    private val HANDLERJOB_SMS_REQ = 1 // SMS 인증
    private val HANDLERJOB_ERROR_SMS_REQ = 2
    private val HANDLERJOB_SMS_CNFM = 3
    private val HANDLERJOB_ERROR_SMS_CNFM = 4
    private val HANDLERJOB_ARS_REQ = 5 // ARS 인증
    private val HANDLERJOB_ERROR_ARS_REQ = 6
    private val HANDLERJOB_ARS_CNFM = 7
    private val HANDLERJOB_ERROR_ARS_CNFM = 8

    private var reqDvsn: String? = null // 인증요청구분( "sms", "ars")
    private var sTchAuthMgntNo: String? = null // SMS 요청관리번호
    private var svcCd: String? = null // ARS 2채널구분코드		1Byte (1:ARS, 2:App)
    private var tradeReq: String? = null // ARS 전문번호
    private var workCd: String? = null // ARS 업무구분코드
    private var trandWorkCd: String? = null // ARS 거래종류
    private var sArsAuthRqutNo: String? = null // ARS 전화번호

    private val maxLengthOfSmsCode = 6
    private lateinit var edtCode: EditText
    private var notiText: TextView? = null

    // 타이머
    private val limitTimeMilli: Long = 180000
    private var startTimeMilli: Long = 0
    private var flagTimeOver = false
    private var textTitmer: TextView? = null
    private var timer: Timer? = null

    override fun handleMessage(p_message: Message) {
        LogPrinter.CF_debug("!-----------------------------------------------------------")
        LogPrinter.CF_debug("!-- IUPC70M00.handleMessage()")
        LogPrinter.CF_debug("!-----------------------------------------------------------")
        if (!isDestroyed) {
            // 프로그레스 다이얼로그 dismiss
            CF_dismissProgressDialog()
            when (p_message.what) {
                HANDLERJOB_UPDATE_TIME -> if (textTitmer != null) {
                    textTitmer!!.text = timeRemaining
                }
                HANDLERJOB_SMS_REQ -> try {
                    httpRes_smsReq1(JSONObject(p_message.obj as String))
                } catch (e: JSONException) {
                    LogPrinter.CF_line()
                    LogPrinter.CF_debug(resources.getString(R.string.log_json_exception))
                }
                HANDLERJOB_ERROR_SMS_REQ -> CommonFunction.CF_showCustomAlertDilaog(this, p_message.obj as String, resources.getString(R.string.btn_ok))
                HANDLERJOB_SMS_CNFM -> try {
                    httpRes_smsCnfm2(JSONObject(p_message.obj as String))
                } catch (e: JSONException) {
                    LogPrinter.CF_line()
                    LogPrinter.CF_debug(resources.getString(R.string.log_json_exception))
                }
                HANDLERJOB_ERROR_SMS_CNFM -> CommonFunction.CF_showCustomAlertDilaog(this, p_message.obj as String, resources.getString(R.string.btn_ok))
                HANDLERJOB_ARS_REQ -> try {
                    httpRes_arsReq1(JSONObject(p_message.obj as String))
                } catch (e: JSONException) {
                    LogPrinter.CF_line()
                    LogPrinter.CF_debug(resources.getString(R.string.log_json_exception))
                }
                HANDLERJOB_ERROR_ARS_REQ -> CommonFunction.CF_showCustomAlertDilaog(this, p_message.obj as String, resources.getString(R.string.btn_ok))
                HANDLERJOB_ARS_CNFM -> try {
                    httpRes_arsCnfm2(JSONObject(p_message.obj as String))
                } catch (e: JSONException) {
                    LogPrinter.CF_line()
                    LogPrinter.CF_debug(resources.getString(R.string.log_json_exception))
                }
                HANDLERJOB_ERROR_ARS_CNFM -> CommonFunction.CF_showCustomAlertDilaog(this, p_message.obj as String, resources.getString(R.string.btn_ok))
                else -> {
                }
            }
        }
    }

    override fun setInit() {
        LogPrinter.CF_debug("!-----------------------------------------------------------")
        LogPrinter.CF_debug("!-- IUPC70M00.setInit()")
        LogPrinter.CF_debug("!-----------------------------------------------------------")
        setContentView(R.layout.iupc70m00)
        handler = WeakReferenceHandler(this)
        startTimeMilli = System.currentTimeMillis()
        flagTimeOver = false
    }

    override fun setUIControl() {
        // 타이틀바 세팅
        setTitleBarUI()
        notiText = findViewById(R.id.notiText)
        edtCode = findViewById(R.id.edtitext)
        edtCode.setFilters(CommonFunction.CF_getInputLengthFilter(maxLengthOfSmsCode))
        edtCode.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable) {

                // 키패드 hide
                if (s.length == maxLengthOfSmsCode) {
                    CommonFunction.CF_closeVirtualKeyboard(applicationContext, edtCode.getWindowToken())
                }
            }
        })
        textTitmer = findViewById(R.id.textView)

        // -- 휴대폰 재인증 버튼
        val tmp_btnRetry = findViewById<Button>(R.id.btnRetry)
        tmp_btnRetry.setOnClickListener(View.OnClickListener {
            CF_stopTimer()
            if (("sms" == reqDvsn)) {
                httpReq_smsReq1()
            } else if (("ars" == reqDvsn)) {
                httpReq_arsReq1()
            } else {
                // TODO 에러
                LogPrinter.CF_debug("!-- 선택값 에러")
            }
        })

        // -- 확인 버튼
        val tmp_btnOk = findViewById<Button>(R.id.btnOk)
        tmp_btnOk.setOnClickListener(object : View.OnClickListener {
            override fun onClick(v: View) {
                if (flagTimeOver) {
                    // 제한시간 초과
                    showCustomDialog(resources.getString(R.string.dlg_timeover_sms), tmp_btnOk)
                } else {
                    if (edtCode.getText().toString().trim { it <= ' ' }.length > 0) {
                        // -- 인증번호 확인 요청
                        if (("sms" == reqDvsn)) {
                            httpReq_smsCnfm2()
                        } else if (("ars" == reqDvsn)) {
                            httpReq_arsCnfm2()
                        } else {
                            // TODO 에러
                            LogPrinter.CF_debug("!-- 선택값 에러")
                        }
                    } else {
                        showCustomDialog(resources.getString(R.string.dlg_no_sms), edtCode)
                    }
                }
            }
        })
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        LogPrinter.CF_debug("!-----------------------------------------------------------")
        LogPrinter.CF_debug("!-- IUPC70M00.onCreate()")
        LogPrinter.CF_debug("!-----------------------------------------------------------")

        // restore 데이터
        if (savedInstanceState != null) {
            flagTimeOver = savedInstanceState.getBoolean("flagTimeOver")
            startTimeMilli = savedInstanceState.getLong("startTimeMilli")
            val tmp_flagRunningTimer = savedInstanceState.getBoolean("flagRunningTimer")
            if (tmp_flagRunningTimer && !flagTimeOver) {
                startTimer(startTimeMilli)
            }
        }

        // intent Data
        if (intent != null) {
            if (intent.hasExtra("reqDvsn")) {
                reqDvsn = intent.getSerializableExtra("reqDvsn") as String

                // -- 인증 요청 시작
                if (("sms" == reqDvsn)) {
                    notiText!!.text = resources.getString(R.string.guide_input_sms_noti)
                    httpReq_smsReq1()
                } else if (("ars" == reqDvsn)) {
                    notiText!!.text = resources.getString(R.string.guide_input_ars_noti)
                    httpReq_arsReq1()
                } else {
                    // TODO 에러
                    LogPrinter.CF_debug("!-- 선택값 에러")
                }
            }
        } else {
            reqDvsn = ""
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        LogPrinter.CF_debug("!-----------------------------------------------------------")
        LogPrinter.CF_debug("!-- IUPC70M00.onDestroy()")
        LogPrinter.CF_debug("!-----------------------------------------------------------")
        if (timer != null) {
            timer!!.cancel()
            timer = null
        }
        if (handler != null) {
            handler!!.removeCallbacksAndMessages(null)
            handler = null
        }
    }

    override fun onBackPressed() {
        LogPrinter.CF_debug("!-----------------------------------------------------------")
        LogPrinter.CF_debug("!-- IUPC70M00.onBackPressed()")
        LogPrinter.CF_debug("!-----------------------------------------------------------")
        backPressedEvent(null)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putBoolean("flagTimeOver", flagTimeOver)
        outState.putLong("startTimeMilli", startTimeMilli)
        outState.putBoolean("flagRunningTimer", timer != null)
    }

    /**
     * 타이틀바 레이아웃 세팅
     */
    private fun setTitleBarUI() {
        LogPrinter.CF_debug("!-----------------------------------------------------------")
        LogPrinter.CF_debug("!-- IUPC70M00.setTitleBarUI()")
        LogPrinter.CF_debug("!-----------------------------------------------------------")

        // 타이틀 세팅
        val tmp_title = findViewById<TextView>(R.id.title_bar_textTitle)
        tmp_title.text = resources.getString(R.string.title_mobile_auth)

        // left 버튼 세팅
        val tmp_btnLeft = findViewById<ImageButton>(R.id.title_bar_imgBtnLeft)
        tmp_btnLeft.visibility = View.VISIBLE
        tmp_btnLeft.setOnClickListener(object : View.OnClickListener {
            override fun onClick(v: View) {
                backPressedEvent(tmp_btnLeft)
            }
        })
    }
    // #############################################################################################
    //  HTTP 관련 함수
    // #############################################################################################
    /**
     * SMS인증 선거래 요청 함수
     */
    private fun httpReq_smsReq1() {
        LogPrinter.CF_debug("!-----------------------------------------------------------")
        LogPrinter.CF_debug("!-- IUPC70M00.httpReq_smsReq1()")
        LogPrinter.CF_debug("!-----------------------------------------------------------")

        // 프로그레스 다이얼로그 show
        CF_showProgressDialog()

        // TODO 로그인안하면 csno 없음
        val tmp_builder = Uri.Builder()
        tmp_builder.appendQueryParameter("csno", CustomSQLiteFunction.getLastLoginCsno(this)) // 고객번호
        HttpConnections.sendPostData(
                EnvConfig.host_url + EnvConfig.URL_SMS_REQ,
                tmp_builder.build().encodedQuery,
                handler,
                HANDLERJOB_SMS_REQ,
                HANDLERJOB_ERROR_SMS_REQ)
    }

    /**
     * SMS인증 선거래 결과 처리 함수
     * @param p_jsonObject  JSONObject
     */
    @Throws(JSONException::class)
    private fun httpRes_smsReq1(p_jsonObject: JSONObject) {
        LogPrinter.CF_debug("!-----------------------------------------------------------")
        LogPrinter.CF_debug("!-- IUPC70M00.httpRes_smsReq1()")
        LogPrinter.CF_debug("!-----------------------------------------------------------")
        LogPrinter.CF_debug("!---- p_jsonObject : $p_jsonObject")

        /*
            요청성공 (p_jsonObject)
            {"reqCd":"00","sTchAuthMgntNo":"071024DEF5716","reqMsg":"요청 성공"}
         */
        val jsonKey_reqCd = "reqCd" // 결과코드
        val jsonKey_reqMsg = "reqMsg" // 결과 메시지
        val jsonKey_sTchAuthMgntNo = "sTchAuthMgntNo"
        val tmp_reqCd: String
        var tmp_reqMsg: String? = ""

        // --<1> (결과코드 유) : 통신 성공
        if (p_jsonObject.has(jsonKey_reqCd)) {
            tmp_reqCd = p_jsonObject.getString(jsonKey_reqCd)

            // --<2> (최종 성공)
            if (("00" == tmp_reqCd)) {
                sTchAuthMgntNo = p_jsonObject.getString(jsonKey_sTchAuthMgntNo)
                CF_startTimer()
                CF_getInputBox()?.let { showCustomDialog(resources.getString(R.string.dlg_send_sms_code), it) }
            } else {
                if (p_jsonObject.has(jsonKey_reqMsg)) {
                    tmp_reqMsg = p_jsonObject.getString(tmp_reqMsg)
                } else {
                    tmp_reqMsg = resources.getString(R.string.dlg_error_server_2)
                }
            }
        } else {
            tmp_reqMsg = resources.getString(R.string.dlg_error_server_1)
        }

        // -- 에러가 있는 경우 에러 메시지 팝업
        if (!TextUtils.isEmpty(tmp_reqMsg)) {
            CommonFunction.CF_showCustomAlertDilaog(this, tmp_reqMsg, resources.getString(R.string.btn_ok))
        }
    }

    /**
     * SMS인증 본거래 요청 함수
     */
    private fun httpReq_smsCnfm2() {
        LogPrinter.CF_debug("!-----------------------------------------------------------")
        LogPrinter.CF_debug("!-- IUPC70M00.httpReq_smsCnfm2()")
        LogPrinter.CF_debug("!-----------------------------------------------------------")

        // 프로그레스 다이얼로그 show
        CF_showProgressDialog()
        val tmp_builder = Uri.Builder()
        tmp_builder.appendQueryParameter("sTchAuthRqutNo", edtCode!!.text.toString().trim { it <= ' ' }) // 사용자입력 인증번호(6자리)
        tmp_builder.appendQueryParameter("sTchAuthMgntNo", sTchAuthMgntNo) // SMS요청 관리번호
        HttpConnections.sendPostData(
                EnvConfig.host_url + EnvConfig.URL_SMS_CNFM,
                tmp_builder.build().encodedQuery,
                handler,
                HANDLERJOB_SMS_CNFM,
                HANDLERJOB_ERROR_SMS_CNFM)
    }

    /**
     * SMS인증 본거래 결과 처리 함수
     * @param p_jsonObject  JSONObject
     */
    @Throws(JSONException::class)
    private fun httpRes_smsCnfm2(p_jsonObject: JSONObject) {
        LogPrinter.CF_debug("!-----------------------------------------------------------")
        LogPrinter.CF_debug("!-- IUPC70M00.httpRes_smsCnfm2()")
        LogPrinter.CF_debug("!-----------------------------------------------------------")
        LogPrinter.CF_debug("!---- p_jsonObject : $p_jsonObject")
        /*
            에러(p_jsonObject)
                {"reqCd":"E001","reqMsg":"요청데이터가 누락되었습니다."}
         */
        val jsonKey_reqCd = "reqCd" // 결과코드
        val jsonKey_reqMsg = "reqMsg" // 결과 메시지
        val tmp_reqCd: String
        var tmp_reqMsg: String? = ""

        // --<1> (결과코드 유) : 통신 성공
        if (p_jsonObject.has(jsonKey_reqCd)) {
            tmp_reqCd = p_jsonObject.getString(jsonKey_reqCd)

            // --<2> (최종 성공)
            if (("00" == tmp_reqCd)) {
                CF_stopTimer()
                val tmp_dlg = CustomDialog(this)
                tmp_dlg.show()
                tmp_dlg.CF_setBackKeyMode(CustomDialog.BackMode.NOTUSE)
                tmp_dlg.CF_setTextContent(resources.getString(R.string.dlg_auth_sms))
                tmp_dlg.CF_setSingleButtonText(resources.getString(R.string.btn_ok))
                tmp_dlg.setOnDismissListener(object : DialogInterface.OnDismissListener {
                    override fun onDismiss(dialog: DialogInterface) {
                        setResult(RESULT_OK)
                        finish()
                    }
                })
            } else {
                if (p_jsonObject.has(jsonKey_reqMsg)) {
                    tmp_reqMsg = p_jsonObject.getString(jsonKey_reqMsg)
                } else {
                    tmp_reqMsg = resources.getString(R.string.dlg_error_server_2)
                }
            }
        } else {
            tmp_reqMsg = resources.getString(R.string.dlg_error_server_1)
        }

        // -- 에러가 있는 경우 에러 메시지 팝업
        if (!TextUtils.isEmpty(tmp_reqMsg)) {
            CommonFunction.CF_showCustomAlertDilaog(this, tmp_reqMsg, resources.getString(R.string.btn_ok))
        }
    }

    /**
     * ARS인증 선거래 HTTP 요청
     */
    private fun httpReq_arsReq1() {
        LogPrinter.CF_debug("!-----------------------------------------------------------")
        LogPrinter.CF_debug("!-- IUPC70M00.httpReq_arsReq1() --ARS인증 선거래 HTTP 요청")
        LogPrinter.CF_debug("!-----------------------------------------------------------")

        // 프로그레스 다이얼로그 show
        CF_showProgressDialog()
        val tmp_builder = Uri.Builder()
        tmp_builder.appendQueryParameter("csno", CustomSQLiteFunction.getLastLoginCsno(this)) // 고객번호
        HttpConnections.sendPostData(
                EnvConfig.host_url + EnvConfig.URL_ARS_REQ,
                tmp_builder.build().encodedQuery,
                handler,
                HANDLERJOB_ARS_REQ,
                HANDLERJOB_ERROR_ARS_REQ)
    }

    /**
     * ARS인증 선거래 결과 처리 함수
     * @param p_jsonObject  JSONObject
     */
    @Throws(JSONException::class)
    private fun httpRes_arsReq1(p_jsonObject: JSONObject) {
        LogPrinter.CF_debug("!-----------------------------------------------------------")
        LogPrinter.CF_debug("!-- IUPC70M00.httpRes_arsReq1() --ARS인증 선거래 HTTP 응답")
        LogPrinter.CF_debug("!-----------------------------------------------------------")
        LogPrinter.CF_debug("!---- p_jsonObject : $p_jsonObject")
        /*
            reqCd		    //응답코드
            reqMsg		    //응답메시지
            authNum		    //고객이 입력한 인증번호
            svcCd		    //2채널구분코드		1Byte (1:ARS)
            tradeReq	    //전문번호			17Byte(생성:StringUtil.makeTradeReq())
            workCd		    //업무구분코드		1Byte (1:이체, 2:인증서, 3:사기방지, 4:예금, 5:보험, 6:카드, 9:공통)
            trandWorkCd	    //거래종류	미입력시 59(보험공통)
            sArsAuthRqutNo	//전화번호
         */
        val jsonKey_reqCd = "reqCd" // 결과코드
        val jsonKey_reqMsg = "reqMsg" // 결과 메시지
        val jsonKey_svcCd = "svcCd" // ARS 2채널구분코드 1Byte (1:ARS, 2:App)
        val jsonKey_tradeReq = "tradeReq" // ARS 전문번호
        val jsonKey_workCd = "workCd" // ARS 업무구분코드
        val jsonKey_trandWorkCd = "trandWorkCd" // ARS 거래종류
        val jsonKey_sArsAuthRqutNo = "sArsAuthRqutNo" // ARS 전화번호
        val jsonKey_authNum = "authNum" // ARS 인증번호
        val tmp_reqCd: String
        var tmp_reqMsg: String? = ""

        // --<1> (결과코드 유) : 통신 성공
        if (p_jsonObject.has(jsonKey_reqCd)) {
            tmp_reqCd = p_jsonObject.getString(jsonKey_reqCd)

            // --<2> (최종 성공)
            if (("00" == tmp_reqCd)) {
                CF_startTimer()
                svcCd = p_jsonObject.getString(jsonKey_svcCd) // ARS 2채널구분코드		1Byte (1:ARS, 2:App)
                tradeReq = p_jsonObject.getString(jsonKey_tradeReq) // ARS 전문번호
                workCd = p_jsonObject.getString(jsonKey_workCd) // ARS 업무구분코드
                trandWorkCd = p_jsonObject.getString(jsonKey_trandWorkCd) // ARS 거래종류
                sArsAuthRqutNo = p_jsonObject.getString(jsonKey_sArsAuthRqutNo) // ARS 전화번호

                // 화면에 표시
                edtCode!!.setText(p_jsonObject.getString(jsonKey_authNum))
            } else {
                if (p_jsonObject.has(jsonKey_reqMsg)) {
                    tmp_reqMsg = p_jsonObject.getString(tmp_reqMsg)
                } else {
                    tmp_reqMsg = resources.getString(R.string.dlg_error_server_2)
                }
            }
        } else {
            tmp_reqMsg = resources.getString(R.string.dlg_error_server_1)
        }

        // -- 에러가 있는 경우 에러 메시지 팝업
        if (!TextUtils.isEmpty(tmp_reqMsg)) {
            CommonFunction.CF_showCustomAlertDilaog(this, tmp_reqMsg, resources.getString(R.string.btn_ok))
        }
    }

    /**
     * ARS인증 본거래 요청 함수
     */
    private fun httpReq_arsCnfm2() {
        LogPrinter.CF_debug("!-----------------------------------------------------------")
        LogPrinter.CF_debug("!-- IUPC70M00.httpReq_arsCnfm2()")
        LogPrinter.CF_debug("!-----------------------------------------------------------")

        // 프로그레스 다이얼로그 show
        CF_showProgressDialog()

        /*
            csno
            svcCd		//2채널구분코드		1Byte (1:ARS, 2:App)
            tradeReq	//전문번호
            workCd		//업무구분코드
            trandWorkCd	//거래종류
            sArsAuthRqutNo	//전화번호
         */
        val tmp_builder = Uri.Builder()
        tmp_builder.appendQueryParameter("csno", CustomSQLiteFunction.getLastLoginCsno(this)) // 고객번호
        tmp_builder.appendQueryParameter("svcCd", svcCd)
        tmp_builder.appendQueryParameter("tradeReq", tradeReq)
        tmp_builder.appendQueryParameter("workCd", workCd)
        tmp_builder.appendQueryParameter("trandWorkCd", trandWorkCd)
        tmp_builder.appendQueryParameter("sArsAuthRqutNo", sArsAuthRqutNo)
        HttpConnections.sendPostData(
                EnvConfig.host_url + EnvConfig.URL_ARS_CNFM,
                tmp_builder.build().encodedQuery,
                handler,
                HANDLERJOB_ARS_CNFM,
                HANDLERJOB_ERROR_ARS_CNFM)
    }

    /**
     * ARS인증 본거래 결과 처리 함수
     * @param p_jsonObject  JSONObject
     */
    @Throws(JSONException::class)
    private fun httpRes_arsCnfm2(p_jsonObject: JSONObject) {
        LogPrinter.CF_debug("!-----------------------------------------------------------")
        LogPrinter.CF_debug("!-- IUPC70M00.httpRes_arsCnfm2()")
        LogPrinter.CF_debug("!-----------------------------------------------------------")
        LogPrinter.CF_debug("!---- p_jsonObject : $p_jsonObject")

        /*
            "00" : 성공
            "E001" : 필수값 누락

            case "02" : reqMsg = "전문 오류가 발생하였습니다."; break;
            case "07" : reqMsg = "ARS 네트워크에 문제가 발생하였습니다."; break;
            case "08" : reqMsg = "내부에 오류가 발생하였습니다."; break;
            case "09" : reqMsg = "DB 연결오류가 발생하였습니다."; break;
            case "99" : reqMsg = "알수없는 오류가 발생하였습니다."; break;

            case "01" : reqMsg = "입력된 인증번호가 일치하지 않습니다."; break;
            case "03" : reqMsg = "인증번호가 입력되지 않았습니다."; break;
            case "04" : reqMsg = "ARS통화 연결이 실패하였습니다."; break;
            case "05" : reqMsg = "거래시간이 경과되었습니다."; break;
            case "06" : reqMsg = "입력값이 제대로 입력되지 않았습니다."; break;
            case "10" : reqMsg = "APP 다운로드 이력이 존재하지 않습니다."; break;
            case "11" : reqMsg = "같은 거래번호로 3회이상 인증요청이 시도되었습니다."; break;
         */
        val jsonKey_reqCd = "reqCd" // 결과코드
        val jsonKey_reqMsg = "reqMsg" // 결과 메시지
        val tmp_reqCd: String
        var tmp_reqMsg: String? = ""

        // --<1> (결과코드 유) : 통신 성공
        if (p_jsonObject.has(jsonKey_reqCd)) {
            tmp_reqCd = p_jsonObject.getString(jsonKey_reqCd)

            // --<2> (최종 성공)
            if (("00" == tmp_reqCd)) {
                CF_stopTimer()
                val tmp_dlg = CustomDialog(this)
                tmp_dlg.show()
                tmp_dlg.CF_setBackKeyMode(CustomDialog.BackMode.NOTUSE)
                tmp_dlg.CF_setTextContent(resources.getString(R.string.dlg_auth_sms))
                tmp_dlg.CF_setSingleButtonText(resources.getString(R.string.btn_ok))
                tmp_dlg.setOnDismissListener(object : DialogInterface.OnDismissListener {
                    override fun onDismiss(dialog: DialogInterface) {
                        setResult(RESULT_OK)
                        finish()
                    }
                })
            } else {
                if (p_jsonObject.has(jsonKey_reqMsg)) {
                    tmp_reqMsg = p_jsonObject.getString(tmp_reqMsg)
                } else {
                    tmp_reqMsg = resources.getString(R.string.dlg_error_server_2)
                }
            }
        } else {
            tmp_reqMsg = resources.getString(R.string.dlg_error_server_1)
        }

        // -- 에러가 있는 경우 에러 메시지 팝업
        if (!TextUtils.isEmpty(tmp_reqMsg)) {
            CommonFunction.CF_showCustomAlertDilaog(this, tmp_reqMsg, resources.getString(R.string.btn_ok))
        }
    }

    /**
     * 남은 시간 반환 함수
     * @return  String
     */
    private val timeRemaining: String
        private get() {
            val tmp_strTime: String
            val tmp_currentTimeMilli = System.currentTimeMillis()
            val tmp_limitTimeMilli = limitTimeMilli - (tmp_currentTimeMilli - startTimeMilli)
            if (tmp_limitTimeMilli <= 0) {
                flagTimeOver = true
                if (timer != null) {
                    timer!!.cancel()
                    timer = null
                }
                tmp_strTime = "00:00"
            } else {
                val tmp_minute = ((tmp_limitTimeMilli / (1000 * 60)) % 60).toInt()
                val tmp_second = (tmp_limitTimeMilli / 1000).toInt() % 60
                tmp_strTime = String.format(Locale.getDefault(), "%02d:%02d", tmp_minute, tmp_second)
            }
            return tmp_strTime
        }

    /**
     * 타이머 시작 함수
     * @param p_startTime   Long
     */
    private fun startTimer(p_startTime: Long) {
        flagTimeOver = false
        startTimeMilli = p_startTime
        timer = Timer()
        timer!!.schedule(object : TimerTask() {
            override fun run() {
                if (timer != null) {
                    handler?.sendEmptyMessage(HANDLERJOB_UPDATE_TIME)
                } else {
                    cancel()
                }
            }
        }, 0L, 1000L)
        textTitmer!!.visibility = View.VISIBLE
    }

    /**
     * 타이머 시작 함수
     */
    fun CF_startTimer() {
        startTimer(System.currentTimeMillis())
    }

    /**
     * 타이머 정지 함수
     */
    fun CF_stopTimer() {
        if (timer != null) {
            timer!!.cancel()
            timer = null
        }
        textTitmer!!.visibility = View.INVISIBLE
    }

    /**
     * 인증번호 입력 EditText 반환
     * @return  EditText
     */
    fun CF_getInputBox(): EditText? {
        return edtCode
    }

    // -- 백버튼 (타이틀바 포함)
    private fun backPressedEvent(focusView: View?) {
        if (focusView == null) {
            val tmp_dlg = CustomDialog(this)
            tmp_dlg.show()
            tmp_dlg.CF_setTextContent(resources.getString(R.string.dlg_cancel_mobile_auth))
            tmp_dlg.CF_setDoubleButtonText(resources.getString(R.string.btn_no), resources.getString(R.string.btn_yes))
            tmp_dlg.setOnDismissListener(object : DialogInterface.OnDismissListener {
                override fun onDismiss(dialog: DialogInterface) {
                    if (!(dialog as CustomDialog).CF_getCanceled()) {
                        CF_stopTimer()
                        finish()
                    }
                }
            })
        } else {
            showCustomDialog(resources.getString(R.string.dlg_cancel_mobile_auth),
                    resources.getString(R.string.btn_no),
                    resources.getString(R.string.btn_yes),
                    focusView,
                    object : DialogInterface.OnDismissListener {
                        override fun onDismiss(dialog: DialogInterface) {
                            if (!(dialog as CustomDialog).CF_getCanceled()) {
                                CF_stopTimer()
                                finish()
                            } else if (CommonFunction.CF_checkAccessibilityTurnOn(applicationContext)) {
                                clearAllFocus()
                                focusView.requestFocus()
                            }
                            focusView.isFocusableInTouchMode = false
                        }
                    })
        }
    }
}