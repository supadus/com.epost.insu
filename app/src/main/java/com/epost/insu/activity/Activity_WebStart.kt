package com.epost.insu.activity

import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableString
import android.text.TextUtils
import android.text.style.ForegroundColorSpan
import com.epost.insu.*
import com.epost.insu.EnvConfig.AuthMode
import com.epost.insu.activity.BC.IUBC01M00
import com.epost.insu.activity.BC.IUBC10M00_P
import com.epost.insu.activity.auth.*
import com.epost.insu.common.*
import com.epost.insu.dialog.CustomDialog
import com.epost.insu.fido.Fido2Constant
import com.epost.insu.module.FingerModule
import com.google.firebase.FirebaseApp
import org.json.JSONException
import org.json.JSONObject
import kotlin.system.exitProcess

/**
 * Web에서 App 호출 시 사용되는 Activity
 * @since     :
 * @version   : 1.3
 * @author    : LSH
 * @see
 * <pre>
 *  - App 실행중 유무를 확인하여 상황에 맞게 Activity를 호출한다.
 *  - App가 실행중이였으면 Web에서 요청한 내용에 맞는 Activity 호출
 *  - App가 실행중이 아니면 메인 Activity를 호출하여 백신 앱위변조등의 내용 구동 후 Web에서 요청한 내용에 맞는 Activity를 보인다.
 * ======================================================================
 * 0.0.0    LSM_20171010    최초 등록
 * 0.0.0    LSM_20171204    Web => App 로그아웃 요청
 * 1.3.6    NJM_20200212    웹뷰에서 공동인증 호출시 구분값 추가
 * 1.5.4    NJM_20210504    [간편인증 전자서명 추가] 웹요청 로직 추가
 * 1.6.1    NJM_20210708    [청구가능시간 변경] 4~5시 청구 불가 처리
 * 1.6.1    NJM_20210722    인증센터 호출 로직 수정 (간편인증/공동인증 별도 호출 통합)
 * 1.6.1    NJM_20210726    [web요청스킴 추가] 자녀보험금청구, 지급내역조회 스킴 추가
 * 1.6.3    NJM_20211007    스마트청구 청구가능시간 오류 수정
 * =======================================================================
 * copyright : (주)프로니스정보기술 | http://www.phronis.co.kr
 * </pre>
 */
class Activity_WebStart : Activity_Default() {
    private var flagRun = false
    private var isWebViewCall = false
    private var dialog: CustomDialog? = null
    private val hospitalCodeKey = "hospitalCode"

    private var menuNo =""
    private var menuLink = ""

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        LogPrinter.CF_debug("!-----------------------------------------------------------")
        LogPrinter.CF_debug("!-- Activity_WebStart.onActivityResult()")
        LogPrinter.CF_debug("!-----------------------------------------------------------")
        LogPrinter.CF_debug("!---- requestCode : [$requestCode] / resultCode : [$resultCode]")
        LogPrinter.CF_debug("!---- isWebViewCall : $isWebViewCall")

        // -----------------------------------------------------------------------------------------
        // --<> 간편인증(핀/지문/패턴) 로그인/전자서명 완료
        // -----------------------------------------------------------------------------------------
        if (requestCode == EnvConfig.REQUESTCODE_ACTIVITY_IUFC00M00 && resultCode == RESULT_OK) {
            // --<> (웹브라우저 호출) 앱 백그라운드 처리
            if (!isWebViewCall) {
                IntentManager.moveTaskToBack(this@Activity_WebStart,true)
            }
            finish()
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        LogPrinter.CF_debug("!-----------------------------------------------------------")
        LogPrinter.CF_debug("!-- Activity_WebStart.onNewIntent()")
        LogPrinter.CF_debug("!-----------------------------------------------------------")
        setIntent(intent)
        flagRun = false

        // 웹 요청 Activity 실행 함수
        startReqWebActivity()
    }

    override fun setInit() {}
    override fun setUIControl() {}
    override fun onCreate(savedInstanceState: Bundle?) {
        FirebaseApp.initializeApp(applicationContext) // AppSolid 난독화 Firebase 충돌로 추가함.
        super.onCreate(savedInstanceState)
        LogPrinter.CF_debug("!-----------------------------------------------------------")
        LogPrinter.CF_debug("!-- Activity_WebStart.onCreate()")
        LogPrinter.CF_debug("!-----------------------------------------------------------")

        // 웹 요청 Activity 실행 함수
        startReqWebActivity()
    }

    override fun onPause() {
        super.onPause()
        overridePendingTransition(0, 0) // end Animation 제거
    }

    /**
     * Web 요청 Activity 호출 함수
     * 최초실행일 경우 메인엑티비티 활성화 후 메인엑티비티에서 처리(초기화 처리 필요하여 경유함)
     * 실행중일 경우 바로 해당 엑티비티 호출
     */
    private fun startReqWebActivity() {

        LogPrinter.CF_debug("!-----------------------------------------------------------")
        LogPrinter.CF_debug("!-- Activity_WebStart.startReqWebActivity()")
        LogPrinter.CF_debug("!-----------------------------------------------------------")
        // -- 웹뷰 호출 구분(Intent)
        if (intent.hasExtra("isWebViewCall")) {
            isWebViewCall = intent.extras!!.getBoolean("isWebViewCall")
            IntentManager.isWebViewCall = isWebViewCall
        }

        if (intent.hasExtra("menuNo")) {
            menuNo = intent.extras!!.getString("menuNo") ?: ""
            IntentManager.menuNo = menuNo
        }
        if (intent.hasExtra("menuLink")) {
            menuLink = intent.extras!!.getString("menuLink") ?: ""
            IntentManager.menuLink = menuLink
        }

       

        val hostStr = host // 요청 hostUrl
        LogPrinter.CF_debug("!---- isWebViewCall : $isWebViewCall")
        LogPrinter.CF_debug("!---- hostStr       : $hostStr")

        // -----------------------------------------------------------------------------------------
        //  현재 로그인 상태인 경우 : App 백그라운드 진입 시간 초기화
        //    ==> App로그인 유지 시간이 초과되었어도 Web 요청이 들어오면 유지시간 연장시키기 위함.
        //    ==> CustomApplication onActivityStarted() 함수보다 선행되어야 한다.
        // -----------------------------------------------------------------------------------------
        if (SharedPreferencesFunc.getFlagLogin(applicationContext)) {
            SharedPreferencesFunc.setAppInBackgroundTime(applicationContext, 0L)
        }

        // -- (최초실행) 중복실행방지
        if (!flagRun) {
            flagRun = true
            var tmp_flagFinish = true
            val sdk = Build.VERSION.SDK_INT // 현 SDK Version

            // -- Web 요청 시간 저장
            SharedPreferencesFunc.setReqLoginTime(applicationContext, System.currentTimeMillis())

            // -------------------------------------------------------------------------------------
            // --<1> App 최초실행 : Activity_WebStart가 task root Activity
            // -------------------------------------------------------------------------------------
            if (isTaskRoot) {
                var tmp_flagLaunch = true
                when (hostStr) {
                    EnvConfig.webHost_reqFido -> {
                        val tmp_fingerModule = FingerModule(this)
                        if (!tmp_fingerModule.CF_hasFingerSensor() || sdk < Build.VERSION_CODES.M) {
                            tmp_flagLaunch = false
                            tmp_flagFinish = false
                            showCustomDialog()
                            dialog!!.CF_setTextContent(resources.getString(R.string.label_no_sensor_finger))
                            dialog!!.CF_setSingleButtonText(resources.getString(R.string.btn_ok))
                            dialog!!.setOnDismissListener {
                                IntentManager.moveTaskToBack(this@Activity_WebStart,true)
                                finish()
                            }
                        }
                    }
                    EnvConfig.webHost_reqPin -> if (sdk < Build.VERSION_CODES.M) {
                        tmp_flagLaunch = false
                        tmp_flagFinish = false
                        showCustomDialog()
                        dialog!!.CF_setTextContent(resources.getString(R.string.label_no_sensor_pin))
                        dialog!!.CF_setSingleButtonText(resources.getString(R.string.btn_ok))
                        dialog!!.setOnDismissListener {
                            IntentManager.moveTaskToBack(this@Activity_WebStart,true)
                            finish()
                        }
                    }
                    EnvConfig.webHost_reqPattern -> if (sdk < Build.VERSION_CODES.M) {
                        tmp_flagLaunch = false
                        tmp_flagFinish = false
                        showCustomDialog()
                        dialog!!.CF_setTextContent(resources.getString(R.string.label_no_sensor_pattern))
                        dialog!!.CF_setSingleButtonText(resources.getString(R.string.btn_ok))
                        dialog!!.setOnDismissListener {
                            IntentManager.moveTaskToBack(this@Activity_WebStart,true)
                            finish()
                        }
                    }
                    EnvConfig.webHost_manageFido -> if (sdk < Build.VERSION_CODES.M) {
                        tmp_flagLaunch = false
                        tmp_flagFinish = false
                        showCustomDialog()
                        dialog!!.CF_setTextContent(resources.getString(R.string.label_no_simple_login_device))
                        dialog!!.CF_setSingleButtonText(resources.getString(R.string.btn_ok))
                        dialog!!.setOnDismissListener {
                            IntentManager.moveTaskToBack(this@Activity_WebStart,true)
                            finish()
                        }
                    }
                }

                // ---------------------------------------------------------------------------------
                //  --> 메인 Activity 실행
                // ---------------------------------------------------------------------------------
                if (tmp_flagLaunch) {
                    val tmp_intent = packageManager.getLaunchIntentForPackage(packageName)

                    // -- 최근 앱에서 생성 검사 : 최근 앱 목록에서 생성이 아닌 경우에만 데이터 전달
                    if (intent != null) {
                        val tmp_flags = intent.flags
                        if (tmp_flags and Intent.FLAG_ACTIVITY_LAUNCHED_FROM_HISTORY == 0) {
                            try {
                                tmp_intent!!.putExtra("startJson", createLaunchJson().toString())
                            } catch (e: JSONException) {
                                LogPrinter.CF_line()
                                LogPrinter.CF_debug(resources.getString(R.string.log_json_exception))
                            }
                        }
                    }
                    startActivity(tmp_intent)
                }
            } else {
                when (hostStr) {

                    EnvConfig.webHost_logOut -> runReqLogOut()
                    EnvConfig.webHost_reqCert -> startLoginCertActivity()
                    EnvConfig.webHost_reqPin -> tmp_flagFinish = runReqPin(AuthMode.LOGIN_WEB)
                    EnvConfig.webHost_reqFido -> tmp_flagFinish = runReqFinger(AuthMode.LOGIN_WEB)
                    EnvConfig.webHost_reqPattern -> tmp_flagFinish = runReqPattern(AuthMode.LOGIN_WEB)
                    EnvConfig.webHost_checkCert -> startIUPC80M10_WebActivity()
                    EnvConfig.webHost_singPin -> {
                        tmp_flagFinish = false
                        runReqPin(AuthMode.SIGN_WEB)
                    }
                    EnvConfig.webHost_singBio -> {
                        tmp_flagFinish = false
                        runReqFinger(AuthMode.SIGN_WEB)
                    }
                    EnvConfig.webHost_singPattern -> {
                        tmp_flagFinish = false
                        runReqPattern(AuthMode.SIGN_WEB)
                    }
                    EnvConfig.webHost_pay      -> tmp_flagFinish = runReqPay(EnvConfig.webHost_pay) // 본인보험금 청구
                    EnvConfig.webHost_child -> tmp_flagFinish = runReqPay(EnvConfig.webHost_child) // 자녀보험금 청구
                    EnvConfig.webHost_search  -> tmp_flagFinish = runReqPaySearch() // 청구내역 조회
                    EnvConfig.webHost_smartPay -> tmp_flagFinish = runSmartReqPay()
                    EnvConfig.webHost_smartPayProc -> tmp_flagFinish = runSmartReqPayProc()
                    EnvConfig.webHost_manageCert -> tmp_flagFinish = runManageFido()
                    EnvConfig.webHost_manageFido -> tmp_flagFinish = runManageFido()
                }
            }

            // -------------------------------------------------------------------------------------
            // -- (정상완료) 정상 처리시 종료
            // -------------------------------------------------------------------------------------
            if (tmp_flagFinish) {
                finish()
            }
        }
    }

    /**
     * 로그아웃 요청 처리<br></br>
     */
    private fun runReqLogOut() {
        if (SharedPreferencesFunc.getFlagLogin(applicationContext)) {
            // -- 현재 App 로그인 상태인 경우 : 로그아웃 및 메인화면으로 이동

            // 데이터 로그아웃 처리
            CustomApplication.CF_logOut(this.applicationContext)


            // Back Stack 이동
            IntentManager.moveTaskToBack(this@Activity_WebStart,true)

            // App 리스타트
            CustomApplication.CF_setKillApp(CustomApplication.KillAppMode.goMain)
        } else {

            IntentManager.moveTaskToBack(this@Activity_WebStart,true)

        }
    }

    /**
     * 본인/자녀 보험금청구 요청 처리
     * @param reqDvsn   [EnvConfig.webHost_pay], [EnvConfig.webHost_payChild]
     * @return boolean, flagFinishActivity
     */
    private fun runReqPay(reqDvsn:String): Boolean {
        var tmp_flagFinishActivity = true

        // --<> (로그인)
        if (SharedPreferencesFunc.getFlagLogin(applicationContext)) {
            tmp_flagFinishActivity = false
            // -------------------------------------------------------------------------
            //  현재 미사용 구간 : App 종료 시 logout 하기 때문
            // -------------------------------------------------------------------------
            //  서버에서 전달되는 고객 name 정보를 사용하지 않고,
            //  App 로그인 고객 name 안내 후 진행
            // -------------------------------------------------------------------------
            showCustomDialog()
            dialog!!.CF_setTextContent("$user_name_fromSqlite 고객으로 보험금청구를 진행하시겠습니까?")
            dialog!!.CF_setDoubleButtonText(resources.getString(R.string.btn_no), resources.getString(R.string.btn_yes))
            dialog!!.setOnDismissListener { p_dialog ->
                if (!(p_dialog as CustomDialog).CF_getCanceled()) {
                    // --<2> (청구가능) 청구불가시 메시지 팝업
                    if (EnvConfig.isPayEnableHour(this)) {
                        val spannable1: Spannable = SpannableString(resources.getString(R.string.dlg_money_over_30))
                        spannable1.setSpan(ForegroundColorSpan(Color.parseColor("#ff7c29c7")), 10, 18, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                        val dialog1 = CustomDialog(this@Activity_WebStart)
                        dialog1.show()
                        dialog1.CF_setTextContent(spannable1)
                        dialog1.CF_setDoubleButtonText(resources.getString(R.string.btn_no), resources.getString(R.string.btn_yes))
                        dialog1.setOnDismissListener { dialog ->
                            // --<> (청구불가1)
                            if ((dialog as CustomDialog).CF_getCanceled()) {
                                val dialog2 = CustomDialog(this@Activity_WebStart)
                                dialog2.show()
                                dialog2.CF_setBackKeyMode(CustomDialog.BackMode.NOTUSE)
                                dialog2.CF_setTextContent(resources.getString(R.string.dlg_is_over_30))
                                dialog2.CF_setSingleButtonText(resources.getString(R.string.btn_ok))
                                dialog2.setOnDismissListener {
                                    IntentManager.moveTaskToBack(this@Activity_WebStart,true)
                                    finish()
                                }
                            }
                            else {
                                // --<> (자녀청구 가능)
                                if(reqDvsn == EnvConfig.webHost_child) {
                                    startIUII90M00_P_Activity()
                                    finish()
                                }
                                else {
                                    val dialog2 = CustomDialog(this@Activity_WebStart)
                                    dialog2.show()
                                    val spannable2: Spannable = SpannableString(resources.getString(R.string.dlg_same_person))
                                    spannable2.setSpan(ForegroundColorSpan(Color.parseColor("#ff7c29c7")), 4, 18, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                                    dialog2.CF_setTextContent(spannable2)
                                    dialog2.CF_setDoubleButtonText(resources.getString(R.string.btn_no), resources.getString(R.string.btn_yes))
                                    dialog2.setOnDismissListener { dialog ->
                                        // --<> (본인청구가능)
                                        if (!(dialog as CustomDialog).CF_getCanceled()) {
                                            startIUII10M00_P_Activity()
                                            finish()
                                        }
                                        // --<> (본인청구불가2)
                                        else {
                                            val dialog3 = CustomDialog(this@Activity_WebStart)
                                            dialog3.show()
                                            dialog3.CF_setBackKeyMode(CustomDialog.BackMode.NOTUSE)
                                            dialog3.CF_setTextContent(resources.getString(R.string.dlg_save_person_2))
                                            dialog3.CF_setSingleButtonText(resources.getString(R.string.btn_ok))
                                            dialog3.setOnDismissListener {
                                                IntentManager.moveTaskToBack(this@Activity_WebStart,true)
                                                finish()
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                } else {
                    IntentManager.moveTaskToBack(this@Activity_WebStart,true)
                    finish()
                }
            }
        }
        // --<> (비로그인)
        else {
            startIUII01M00_Activity()
        }
        return tmp_flagFinishActivity
    }

    /**
     * 보험금청구 내역조회 요청 처리
     * @param reqDvsn   [EnvConfig.webHost_pay], [EnvConfig.webHost_child]
     * @return boolean, flagFinishActivity
     */
    private fun runReqPaySearch(): Boolean {
        var tmp_flagFinishActivity = true

        // --<> (로그인)
        if (SharedPreferencesFunc.getFlagLogin(applicationContext)) {
            tmp_flagFinishActivity = false
            startIUII50M00_P()
        }
        // --<> (비로그인)
        else {
            startIUII01M00_Activity()
        }
        return tmp_flagFinishActivity
    }

       /**
     * 스마트보험금청구 요청 처리
     * @return boolean, flagFinishActivity
     */
    private fun runSmartReqPay(): Boolean {
        val tmp_flagFinishActivity: Boolean

        // -----------------------------------------------------------------
        // 청구가능시간 체크
        // -----------------------------------------------------------------
        if (EnvConfig.isPayEnableHour(this)) {
            if (SharedPreferencesFunc.getFlagLogin(applicationContext)) {
                tmp_flagFinishActivity = false
                SmartReqBenefit_Activity()
                finish()
            } else {
                tmp_flagFinishActivity = false
                showCustomDialog()
                dialog!!.CF_setBackKeyMode(CustomDialog.BackMode.CANCELED)
                dialog!!.CF_setTextContent(resources.getString(R.string.dlg_need_login_for_req_pay))
                dialog!!.CF_setSingleButtonText(resources.getString(R.string.btn_ok))
                dialog!!.setOnDismissListener {
                    val intent = Intent(applicationContext, IUBC01M00::class.java)
                    LogPrinter.CF_debug("[앱호출(비로그인)] 병의원 코드 : " + getParameter(hospitalCodeKey))
                    SharedPreferencesFunc.setSmartReqHospitalCode(applicationContext, getParameter(hospitalCodeKey))
                    intent.putExtra("isSmartReqPay", true)
                    startActivity(intent)
                    finish()
                }
            }
        }
        else {
            tmp_flagFinishActivity = false
        }
        return tmp_flagFinishActivity
    }


    /**
     * 스마트보험금청구 청구절차 페이지 호출<br></br>
     * @return boolean, true
     */
    private fun runSmartReqPayProc(): Boolean {
        val intent = Intent(this, IUII60M00::class.java)
        startActivity(intent)
        return true
    }

    /**
     * 간편인증관리 요청 처리<br></br>
     * @return boolean, flagFinishActivity
     */
    private fun runManageFido(): Boolean {
        startIUPC30M00_Activity()
        return true
    }

    /**
     * PIN 인증 요청(로그인/전자서명) 처리
     * @return boolean, flagFinishActivity
     */
    private fun runReqPin(authMode: AuthMode): Boolean {
        var tmp_flagFinishActivity = true
        val sdk = Build.VERSION.SDK_INT
        // --<1> (지원 단말기)
        if (sdk >= Build.VERSION_CODES.M) {
            // --<> 단말기 DB에 고객식별번호가 있는 경우
            if (CustomSQLiteFunction.hasUserCsno(applicationContext)) {
                startIUFC00M00_Auth(Fido2Constant.AUTH_TECH_PIN, authMode)
            } else {
                // -------------------------------------------------------
                // 고객식별번호가 없는 경우 Dialog를 통해 최초 1회 공동인증
                // 로그인이 필요하다는 안내 Dialog 팝업
                // -------------------------------------------------------
                tmp_flagFinishActivity = false
                showCustomDialog()
                dialog!!.CF_setTextContent(resources.getString(R.string.dlg_empty_csno))
                dialog!!.CF_setSingleButtonText(resources.getString(R.string.btn_ok))
                dialog!!.setOnDismissListener { finish() }
            }
        } else {
            tmp_flagFinishActivity = false
            showCustomDialog()
            dialog!!.CF_setTextContent(resources.getString(R.string.label_no_sensor_pin))
            dialog!!.CF_setSingleButtonText(resources.getString(R.string.btn_ok))
            dialog!!.setOnDismissListener { finish() }
        }
        return tmp_flagFinishActivity
    }

    /**
     * 지문인증 요청(로그인/전자서명) 처리<br></br>
     * @return  boolean, flagFinishActivity
     */
    private fun runReqFinger(authMode: AuthMode): Boolean {
        var tmp_flagFinishActivity = true
        val tmp_fingerModule = FingerModule(this)
        if (tmp_fingerModule.CF_hasFingerSensor() && Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

            // --<2> (고객번호 유)
            if (CustomSQLiteFunction.hasUserCsno(applicationContext)) {
                startIUFC00M00_Auth(Fido2Constant.AUTH_TECH_FINGER, authMode)
            } else {
                tmp_flagFinishActivity = false
                showCustomDialog()
                dialog!!.CF_setTextContent(resources.getString(R.string.dlg_empty_csno))
                dialog!!.CF_setSingleButtonText(resources.getString(R.string.btn_ok))
                dialog!!.setOnDismissListener { finish() }
            }
        } else {
            tmp_flagFinishActivity = false
            showCustomDialog()
            dialog!!.CF_setTextContent(resources.getString(R.string.label_no_sensor_finger))
            dialog!!.CF_setSingleButtonText(resources.getString(R.string.btn_ok))
            dialog!!.setOnDismissListener { finish() }
        }
        return tmp_flagFinishActivity
    }

    /**
     * 패턴인증 요청(로그인/전자서명) 처리
     * @return boolean  flagFinishActivity
     */
    private fun runReqPattern(authMode: AuthMode): Boolean {
        var tmp_flagFinishActivity = true
        val sdk = Build.VERSION.SDK_INT
        if (sdk >= Build.VERSION_CODES.M) {
            if (CustomSQLiteFunction.hasUserCsno(applicationContext)) {
                // 단말기 DB에 고객식별번호가 있는 경우
                startIUFC00M00_Auth(Fido2Constant.AUTH_TECH_PATTERN, authMode)
            } else {
                // -------------------------------------------------------
                // 고객식별번호가 없는 경우 Dialog를 통해 최초 1회 공동인증
                // 로그인이 필요하다는 안내 Dialog 팝업
                // -------------------------------------------------------
                tmp_flagFinishActivity = false
                showCustomDialog()
                dialog!!.CF_setTextContent(resources.getString(R.string.dlg_empty_csno))
                dialog!!.CF_setSingleButtonText(resources.getString(R.string.btn_ok))
                dialog!!.setOnDismissListener { finish() }
            }
        } else {
            tmp_flagFinishActivity = false
            showCustomDialog()
            dialog!!.CF_setTextContent(resources.getString(R.string.label_no_sensor_pattern))
            dialog!!.CF_setSingleButtonText(resources.getString(R.string.btn_ok))
            dialog!!.setOnDismissListener { finish() }
        }
        return tmp_flagFinishActivity
    }

    /**
     * 커스텀 다이얼로그 show 함수
     */
    private fun showCustomDialog() {
        if (dialog == null) {
            dialog = CustomDialog(this)
        }
        if (!dialog!!.isShowing) {
            dialog!!.show()
        }
    }

    /**
     * 커스텀 다이얼로그 dismiss
     */
    private fun dismissCustomDialog() {
        if (dialog != null && dialog!!.isShowing) {
            dialog!!.dismiss()
        }
    }

    /**
     * 단말기에 저장된 사용자 이름 반환
     * @return      String
     */
    private val user_name_fromSqlite: String
        get() {
            val tmp_name: String
            val tmp_helper = CustomSQLiteHelper(applicationContext)
            val tmp_sqlite = tmp_helper.readableDatabase
            tmp_name = tmp_helper.CF_SelectUserName(tmp_sqlite)
            tmp_sqlite.close()
            tmp_helper.close()
            return tmp_name
        }

    /**
     * 호스트명 반환
     * Main Activity는 Manifest에 정의된 호스트명에 따라 실행시킬 Activity를 판단한다.
     * @return      String
     */
    private val host: String
        get() {
            var tmp_host: String? = ""
            if (intent != null) {
                if (intent.data != null) {
                    tmp_host = intent.data!!.host
                }
            }
            return tmp_host ?: ""
        }

    /**
     * Web에서 전달한 tempKey 반환
     * @return      String
     */
    private val tempKey: String
        get() {
            var tmp_key: String? = ""
            if (intent != null) {
                if (intent.data != null) {
                    tmp_key = intent.data!!.getQueryParameter("tempKey")
                }
            }
            return tmp_key ?: ""
        }// ---------------------------------------------------------------------------------
    //  getQueryParameter() 함수 => '+'를 ' ' 공백으로 치환해서 반환함.
    // ---------------------------------------------------------------------------------
    /**
     * Web에서 전달한 rnno 반환
     * @return      String
     */
    private val rNNO_ENC: String
        get() {
            var tmp_rnno: String? = ""
            if (intent != null) {
                if (intent.data != null) {
                    tmp_rnno = intent.data!!.getQueryParameter("rnno_enc")

                    // ---------------------------------------------------------------------------------
                    //  getQueryParameter() 함수 => '+'를 ' ' 공백으로 치환해서 반환함.
                    // ---------------------------------------------------------------------------------
                    if (!TextUtils.isEmpty(tmp_rnno)) {
                        tmp_rnno = tmp_rnno!!.replace(" ", "+")
                    }
                }
            }
            return tmp_rnno ?: ""
        }

    /**
     * Web에서 전달한 name 반환
     * @return      String
     */
    private val name: String
        get() {
            var tmp_name: String? = ""
            if (intent.data != null) {
                tmp_name = intent.data!!.getQueryParameter("name")
            }
            return tmp_name ?: ""
        }

    /**
     * Web에서 전달한 hospitalCode 반환
     * @return      String
     */
    private val hospitalCode: String
        get() {
            var tmp_hospital_code: String? = ""
            if (intent.data != null) {
                tmp_hospital_code = intent.data!!.getQueryParameter("hospitalCode")
            }
            return tmp_hospital_code ?: ""
        }

    /**
     * Web에서 전달한 parameter 반환
     * @return      String
     */
    private fun getParameter(key: String): String {
        var tmp_name: String? = ""
        if (intent.data != null) {
            tmp_name = intent.data!!.getQueryParameter(key)
        }
        return tmp_name ?: ""
    }

    /**
     * App 실행 JSONObject 반환 함수<br></br>
     * Main Activity 에 JSONObject를 전달해 INTRO 후 실행시킬 Activity를 판단한다.
     * @return      JSONObject
     * @throws      JSONException jsonException
     */
    @Throws(JSONException::class)
    private fun createLaunchJson(): JSONObject {
        val tmp_jsonObject = JSONObject()
        tmp_jsonObject.put("host", host)
        tmp_jsonObject.put("key", tempKey)
        tmp_jsonObject.put("name", name)
        tmp_jsonObject.put("rnno_enc", rNNO_ENC)
        tmp_jsonObject.put("isWebViewCall", isWebViewCall)
        tmp_jsonObject.put("hospital_code", hospitalCode)
        return tmp_jsonObject
    }
    //    /**
    //     * Root Activity 검사<br/>
    //     * Root Activity라면 현재 App가 실행중이 아니였음을 의미한다.
    //     * @return
    //     */
    //    @SuppressWarnings("deprecation")
    //    @SuppressLint("NewApi")
    //    private boolean isRootActivity(){
    //
    //        boolean tmp_flagIsRoot = false;
    //
    //        ActivityManager tmp_activityManager = (ActivityManager)getSystemService(ACTIVITY_SERVICE);
    //
    //        int tmp_sdk = Build.VERSION.SDK_INT;
    //        if(tmp_sdk >= Build.VERSION_CODES.M){
    //            List<ActivityManager.AppTask> tmp_procInfos = tmp_activityManager.getAppTasks();
    //
    //            for(int i = 0; i < tmp_procInfos.size(); i++){
    //                if(tmp_procInfos.get(i).getTaskInfo().baseActivity.getClassName().equals(getClass().getName())){
    //                    tmp_flagIsRoot = true;
    //
    //                    break;
    //                }
    //            }
    //        }
    //        else{
    //            List<ActivityManager.RunningTaskInfo> tmp_procInfos = tmp_activityManager.getRunningTasks(1);
    //
    //            for(int i = 0 ; i < tmp_procInfos.size(); i++){
    //
    //                if(tmp_procInfos.get(i).baseActivity.getClassName().equals(getClass().getName())){
    //                    tmp_flagIsRoot = true;
    //                    break;
    //                }
    //            }
    //        }
    //
    //        return tmp_flagIsRoot;
    //    }
    /**
     * 공동인증 로그인 Activity 호출 함수
     */
    private fun startLoginCertActivity() {
        val intent = Intent(this, IUCOC0M00_Web::class.java)
        intent.putExtra("key", tempKey)
        startActivity(intent)
    }


    /**
     * 공동인증서 전자서명 Activity 호출(웹요청)
     */
    private fun startIUPC80M10_WebActivity() {
        val intent = Intent(this, IUPC80M10_Web::class.java)
        intent.putExtra("key", tempKey)
        intent.putExtra("name", name)
        intent.putExtra("rnno_enc", rNNO_ENC)
        intent.putExtra("isWebViewCall", isWebViewCall) // 웹뷰에서 호출했는지 구분 true:웹뷰호출
        intent.putExtra("hospital_code", hospitalCode)
        startActivity(intent)
    }



    /**
     * 간편인증(지문,핀,패턴) 로그인/전자서명 Activity 호출(웹요청)
     * @param authTechCode String
     */
    private fun startIUFC00M00_Auth(authTechCode: String, authMode: AuthMode) {
        LogPrinter.CF_debug("!-----------------------------------------------------------")
        LogPrinter.CF_debug("!-- Activity_WebStart.startIUFC00M00_Auth() --간편인증 Activity 호출")
        LogPrinter.CF_debug("!-----------------------------------------------------------")
        LogPrinter.CF_debug("!-- authMode     : $authMode")
        LogPrinter.CF_debug("!-- authTechCode : $authTechCode")

        val intent = Intent(this, IUFC00M00::class.java)
        intent.putExtra(EnvConstant.KEY_INTENT_AUTH_MODE, authMode) // 요청모드(LOGIN_APP,SIGN_APP..)
        intent.putExtra(EnvConstant.KEY_INTENT_AUTH_TECH_CODE, authTechCode) // FIDO기술코드
        intent.putExtra("key", tempKey)
        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
        startActivityForResult(intent, EnvConfig.REQUESTCODE_ACTIVITY_IUFC00M00)
    }

    /**
     * 인증센터 Activity 호출
     */
    private fun startIUPC30M00_Activity() {
        val intent = Intent(this, IUFC30M00::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        startActivity(intent)
    }

    /**
     * 보험금청구 첫화면 Activity 호출(웹요청)
     */
    private fun startIUII01M00_Activity() {
        val intent = Intent(this, IUII01M00::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
        startActivity(intent)
    }

    /**
     * 본인 보험금청구 Activity 호출(웹요청)
     */
    private fun startIUII10M00_P_Activity() {
        val intent = Intent(this, IUII10M00_P::class.java)
        startActivity(intent)
    }

    /**
     * 자녀 보험금청구 Activity 호출(웹요청)
     */
    private fun startIUII90M00_P_Activity() {
        val intent = Intent(this, IUII90M00_P::class.java)
        startActivity(intent)
    }

    /**
     * 보험금 청구내역 조회 Activity 호출 함수
     */
    private fun startIUII50M00_P() {
        val intent = Intent(this, IUII50M00_P::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        startActivity(intent)
    }

    /**
     * 스마트보험금청구접수신청 Activity 호출(웹요청)
     */
    private fun SmartReqBenefit_Activity() {
        LogPrinter.CF_debug("[앱호출(로그인)] 병의원 코드 : " + getParameter(hospitalCodeKey))
        SharedPreferencesFunc.setSmartReqHospitalCode(applicationContext, getParameter(hospitalCodeKey))
        val intent = Intent(this, IUBC10M00_P::class.java)
        startActivity(intent)
    }
}