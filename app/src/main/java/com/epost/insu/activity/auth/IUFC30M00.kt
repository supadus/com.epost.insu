package com.epost.insu.activity.auth

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.*
import com.epost.insu.*
import com.epost.insu.EnvConfig.AuthMode
import com.epost.insu.activity.Activity_Default
import com.epost.insu.common.CustomSQLiteFunction
import com.epost.insu.common.LogPrinter
import com.epost.insu.common.UiUtil
import com.epost.insu.control.CustomTabView
import com.epost.insu.dialog.CustomDialog
import com.epost.insu.fido.Fido2Constant

/**
 * 인증센터 > 간편인증/공동인증 서비스 선택 화면
 * @since     : project 48:1.4.5
 * @version   : 1.1
 * @author    : NJM
 * @see
 * <pre>
 * ======================================================================
 * 1.4.5    NJM_20201229    최초 등록
 * 1.5.8    NJM_20210624    [인증센터 로그인 개선] 핀번호 로그인사용자만 등록/해지 메뉴 바로 접근 가능
 * 1.6.2    NJM_20210729    [간편인증 플래그 반영] 간편인증 로그인시 flag값 참조 변경 (서버저장 -> 단말저장)
 * =======================================================================
 * copyright : (주)프로니스정보기술 | http://www.phronis.co.kr
 * </pre>
 */
class IUFC30M00 : Activity_Default(), View.OnClickListener {
    private lateinit var tabView: CustomTabView  // 간편인증 | 공동인증 선택 Tab
    private var viewFocus: View? = null // onPostResume 에서 포커싱할 view

    private var selectedPageNo: Int = -1 // onActivityResult 후 이동할 pageNo

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        LogPrinter.CF_debug("!----------------------------------------------------------")
        LogPrinter.CF_debug("!-- IUFC30M00.onActivityResult()")
        LogPrinter.CF_debug("!----------------------------------------------------------")
        LogPrinter.CF_debug("!---- requestCode : [$requestCode] / resultCode : [$resultCode]")

        if (resultCode == RESULT_OK) {
            when (requestCode) {
                EnvConfig.REQUESTCODE_ACTIVITY_IUFC00M00 -> {
                    movePageExe()
                }
                else -> LogPrinter.CF_debug("!---- p_requestCode값 없음 : $requestCode")
            }
        } else {
            LogPrinter.CF_debug("!---- p_resultCode실패 : $requestCode")
        }
    }

    override fun onClick(p_view: View) {
        LogPrinter.CF_debug("!----------------------------------------------------------")
        LogPrinter.CF_debug("!-- IUFC30M00.onClick() --")
        LogPrinter.CF_debug("!----------------------------------------------------------")

        // -- 간편인증
        when (p_view.id) {
            R.id.btnBioNoti -> { // 간편인증서비스안내
                startIUPC31M00()
            }
            R.id.btnBioReg -> { // 간편인증 등록/변경
                selectedPageNo = 0
                movePageExe()
            }
            R.id.btnBioReReg -> { // 개인인증번호 재등록
                startIUFC34M00()
            }
            R.id.btnBioDel -> { // 간편인증 조회/해지
                selectedPageNo = 1
                movePageExe()
            }
            R.id.btnReg -> { // 인증서 등록
                startIUCOC0M00()
            }
            R.id.btnGet -> { // 인증서 가져오기
                startGetCertificateActivity()
            }
            R.id.btnDel -> { // 인증서 삭제
                startDelCertificateActivity()
            }
        }
    }

    override fun setInit() {
        setContentView(R.layout.iufc30m00)
    }

    override fun setUIControl() {
        // 타이틀바 UI
        setTitleBarUI()
        setManageBioBtn()
        setManageCertiBtn()

        tabView = findViewById(R.id.activity_manage_s_tab)
        tabView.CF_setTabText(resources.getString(R.string.btn_login_Bio), resources.getString(R.string.btn_login_certificate))
        tabView.CF_setTabBackground(R.drawable.tab_button_selector, R.drawable.tab_button_selector)
        tabView.CE_setOnSelectedChangeEventListener { p_index -> showPage(p_index) }
    }

    override fun onSaveInstanceState(p_bundle: Bundle) {
        super.onSaveInstanceState(p_bundle)
        p_bundle.putInt("currentPageIndex", tabView.CF_getCurrentIndex())
    }

    override fun onRestoreInstanceState(p_bundle: Bundle) {
        super.onRestoreInstanceState(p_bundle)
        if (p_bundle.containsKey("currentPageIndex")) {
            val currentPageIdx: Int = p_bundle.getInt("currentPageIndex")
            tabView.CF_setSelectState(currentPageIdx)
            showPage(currentPageIdx)
        }
    }

    override fun onPostResume() {
        super.onPostResume()
        if (viewFocus != null) {
            clearAllFocus()
            viewFocus!!.isFocusableInTouchMode = true
            viewFocus!!.requestFocus()
            viewFocus!!.isFocusableInTouchMode = false
            viewFocus = null
        }
    }

    /**
     * 타이틀바 레이아웃 세팅
     */
    private fun setTitleBarUI() {
        // 타이틀 세팅
        val txtTitle: TextView = findViewById(R.id.title_bar_textTitle)
        txtTitle.text = resources.getString(R.string.title_manage_auth)



        val title_main: TextView = findViewById(R.id.title_main)
        UiUtil.setTitleColor(title_main,getString(R.string.label_iufc30m00_title1),4,8)



        val title_main2: TextView = findViewById(R.id.title_main2)
        UiUtil.setTitleColor(title_main2,getString(R.string.label_iufc30m00_title2),4,8)




        // left 버튼 세팅
        val btnLeft: ImageButton = findViewById(R.id.title_bar_imgBtnLeft)
        btnLeft.visibility = View.VISIBLE
        btnLeft.setOnClickListener { finish() }
    }

    /**
     * 간편인증 관련 버튼 세팅
     */
    private fun setManageBioBtn() {
        // 간편인증서비스 안내
        val btnBioNoti: View = findViewById(R.id.btnBioNoti)
        val txtBioNoti: TextView = btnBioNoti.findViewById(R.id.textView)
        (btnBioNoti.findViewById<View>(R.id.imageView) as ImageView).setImageResource(R.drawable.ic_bio_noti)
        txtBioNoti.text = resources.getString(R.string.btn_bio_noti)
        txtBioNoti.contentDescription = resources.getString(R.string.btn_bio_noti) + resources.getString(R.string.label_btn)

        // 간편인증 등록/변경
        val btnBioReg: View = findViewById(R.id.btnBioReg)
        (btnBioReg.findViewById<View>(R.id.imageView) as ImageView).setImageResource(R.drawable.ic_bio_reg)
        (btnBioReg.findViewById<View>(R.id.textView) as TextView).text = resources.getString(R.string.btn_bio_reg)
        btnBioReg.findViewById<View>(R.id.textView).contentDescription = resources.getString(R.string.btn_bio_reg) + resources.getString(R.string.label_btn)

        // 개인인증번호 재등록
        val btnBioReReg: View = findViewById(R.id.btnBioReReg)
        (btnBioReReg.findViewById<View>(R.id.imageView) as ImageView).setImageResource(R.drawable.ic_bio_re_reg)
        (btnBioReReg.findViewById<View>(R.id.textView) as TextView).text = resources.getString(R.string.btn_bio_re_reg)
        btnBioReReg.findViewById<View>(R.id.textView).contentDescription = resources.getString(R.string.btn_bio_re_reg) + resources.getString(R.string.label_btn)

        // 간편인증 조회/해지
        val btnBioDel: View = findViewById(R.id.btnBioDel)
        (btnBioDel.findViewById<View>(R.id.imageView) as ImageView).setImageResource(R.drawable.ic_bio_del)
        (btnBioDel.findViewById<View>(R.id.textView) as TextView).text = resources.getString(R.string.btn_bio_del)
        btnBioDel.findViewById<View>(R.id.textView).contentDescription = resources.getString(R.string.btn_bio_del) + resources.getString(R.string.label_btn)

        btnBioNoti.setOnClickListener(this)
        btnBioReg.setOnClickListener(this)
        btnBioReReg.setOnClickListener(this)
        btnBioDel.setOnClickListener(this)
    }

    /**
     * 공동인증 관련 버튼 세팅
     */
    private fun setManageCertiBtn() {
        // -- 공동인증서 등록
        val btnReg: View = findViewById(R.id.btnReg)
        val txtReg: TextView = btnReg.findViewById(R.id.textView)
        (btnReg.findViewById<View>(R.id.imageView) as ImageView).setImageResource(R.drawable.ic_certificate_reg)
        txtReg.text = resources.getString(R.string.btn_reg_certificate)
        txtReg.contentDescription = resources.getString(R.string.btn_reg_certificate) + resources.getString(R.string.label_btn)

        // -- 공동인증서 가져오기
        val btnGet: View = findViewById(R.id.btnGet)
        (btnGet.findViewById<View>(R.id.imageView) as ImageView).setImageResource(R.drawable.ic_certificate_get)
        (btnGet.findViewById<View>(R.id.textView) as TextView).text = resources.getString(R.string.btn_get_certificate)
        btnGet.findViewById<View>(R.id.textView).contentDescription = resources.getString(R.string.btn_get_certificate) + resources.getString(R.string.label_btn)

        // -- 공동인증서 삭제
        val btnDel: View = findViewById(R.id.btnDel)
        (btnDel.findViewById<View>(R.id.imageView) as ImageView).setImageResource(R.drawable.ic_certificate_del)
        (btnDel.findViewById<View>(R.id.textView) as TextView).text = resources.getString(R.string.btn_del_certificate)
        btnDel.findViewById<View>(R.id.textView).contentDescription = resources.getString(R.string.btn_del_certificate) + resources.getString(R.string.label_btn)

        btnGet.setOnClickListener(this)
        btnReg.setOnClickListener(this)
        btnDel.setOnClickListener(this)
    }

    /**
     * 해당 페이지 show 함수<br></br>
     * @param p_index if 0  간편인증, if 1 공동인증
     */
    private fun showPage(p_index: Int) {
        val linFinger: LinearLayout = findViewById(R.id.activity_manage_simple_linBio)
        val linPin   : LinearLayout = findViewById(R.id.activity_manage_simple_linCerti)
        if (p_index == 0) {
            linFinger.visibility = View.VISIBLE
            linPin.visibility = View.GONE
        } else if (p_index == 1) {
            linFinger.visibility = View.GONE
            linPin.visibility = View.VISIBLE
        }
    }
    // #############################################################################################
    //  Activity 호출
    // #############################################################################################
    /**
     * 간편인증서비스안내 Activity 호출 함수
     */
    private fun startIUPC31M00() {
        LogPrinter.CF_debug("!----------------------------------------------------------")
        LogPrinter.CF_debug("!-- IUFC30M00.startIUPC31M00() --간편인증서비스안내 Activity 호출")
        LogPrinter.CF_debug("!----------------------------------------------------------")

        val intent = Intent(this, IUFC31M00::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
        startActivity(intent)
    }

//    /**
//     * 간편인증 등록/변경 Activity 호출 함수
//     * tmp_movePageNo (0:등록/변경 , 1:조회/해지)
//     */
//    private fun movePageCheck() {
//        LogPrinter.CF_debug("!----------------------------------------------------------")
//        LogPrinter.CF_debug("!-- IUFC30M00.startIUPC32M00() --간편인증 등록/변경 Activity 호출")
//        LogPrinter.CF_debug("!----------------------------------------------------------")
//
//        movePageExe(movePageNo)
//
//        // FIDO 등록 정보 DB 조회 후 실행
//        Fido2RegistableServer(applicationContext, this, object : Fido2Callback {
//            override fun onReceiveMessage(code: String, bundle: Bundle) {
//                CF_dismissProgressDialog()
//                LogPrinter.CF_debug("!----[최종완료] (code) : $code / (bundle) : $bundle")
//
//                // 결과에 따른 페이지 분기
//                movePageExe(tmp_movePageNo)
//            }
//
//            override fun onFailure(code: String, msg: String) {
//                LogPrinter.CF_debug("!----[최종에러] (code) : $code / (msg) : $msg")
//                CF_dismissProgressDialog()
//                CommonFunction.CF_showCustomAlertDilaog(getApplicationContext(), msg, getResources().getString(R.string.btn_ok))
//            }
//        }).process()
//    }

    /**
     * 로그인, 핀등록여부 등에 따라 진입 페이지 분기
     * pageNo int  호출페이지번호 (0:등록페이지, 1:해지페이지)
     */
    private fun movePageExe() {
        var pageNo: Int = selectedPageNo

        val flagHasCsno: Boolean = CustomSQLiteFunction.hasUserCsno(applicationContext)
        val flagLogin: Boolean   = SharedPreferencesFunc.getFlagLogin(applicationContext)
        val flagRegFin: Boolean  = SharedPreferencesFunc.getFlagRegAuthDvsn(applicationContext, EnvConfig.AuthDvsn.getAuthDvsnByFido(Fido2Constant.AUTH_TECH_PIN))
        val authDvsn: EnvConfig.AuthDvsn = SharedPreferencesFunc.getLoginAuthDvsn(applicationContext)

        // --<> (비로그인) 이동 페이지 변경
        if (!flagLogin) {
            pageNo = if (flagHasCsno && flagRegFin) 2 else 3 // --<> (2:PIN 등록자, 3:PIN 미등록자)
        }
        else { // --<> (로그인)
            if(authDvsn != EnvConfig.AuthDvsn.PIN) {  // -- (다른인증으로 로그인)
                pageNo = 2
            }
        }

        val tmpDlg = CustomDialog(this@IUFC30M00)
        when (pageNo) {
            // (PIN 로그인 정상 -> 등록페이지)
            0 -> {
                val tmpIntent0 = Intent(applicationContext, IUFC32M00::class.java)
                tmpIntent0.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
                startActivity(tmpIntent0)
            }
            // (PIN 로그인 정상 -> 해지페이지)
            1 -> {
                val tmpIntent1 = Intent(applicationContext, IUFC33M00::class.java)
                tmpIntent1.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
                startActivity(tmpIntent1)
            }
            // (PIN 등록자 -> PIN 로그인 요청)
            2 -> {
                tmpDlg.show()
                tmpDlg.CF_setTextContent(resources.getString(R.string.dlg_need_login_for_bio))
                tmpDlg.CF_setDoubleButtonText(resources.getString(R.string.btn_no), resources.getString(R.string.btn_yes))
                tmpDlg.setOnDismissListener { dialog ->
                    if (!(dialog as CustomDialog).CF_getCanceled()) {
                        // --간편인증 로그인(핀)
                        startIUFC00M00_fido()
                    }
                }
            }
            // (PIN 미등록자 -> PIN 등록 요청)
            3 -> {
                tmpDlg.show()
                tmpDlg.CF_setBackKeyMode(CustomDialog.BackMode.CANCELED)
                tmpDlg.CF_setTextContent(resources.getString(R.string.dlg_choice_reg_bio))
                tmpDlg.CF_setDoubleButtonText(resources.getString(R.string.btn_no), resources.getString(R.string.btn_yes))
                tmpDlg.setOnDismissListener { dialog ->
                    if (!(dialog as CustomDialog).CF_getCanceled()) {
                        startIUFC34M00()
                    }
                }
            }
        }
    }

    /**
     * 간편인증 Activity 호출 함수<br></br>
     */
    private fun startIUFC00M00_fido() {
        val intent = Intent(this, IUFC00M00::class.java)
        intent.putExtra(EnvConstant.KEY_INTENT_AUTH_MODE, AuthMode.LOGIN_APP)
        intent.putExtra(EnvConstant.KEY_INTENT_AUTH_TECH_CODE, Fido2Constant.AUTH_TECH_PIN)
        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
        startActivityForResult(intent, EnvConfig.REQUESTCODE_ACTIVITY_IUFC00M00)
    }

    /**
     * 개인인증번호 재등록 Activity 호출 함수
     */
    private fun startIUFC34M00() {
        val intent = Intent(this, IUFC34M00::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
        startActivity(intent)
    }

    /**
     * 공동인증서 등록 Activity 호출 함수
     */
    private fun startIUCOC0M00() {
        val intent = Intent(this, IUCOC0M00::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
        startActivity(intent)
    }

    /**
     * 공동인증서 가져오기 Activity 호출 함수
     */
    private fun startGetCertificateActivity() {
        val intent = Intent(this, IUPC02M00::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
        startActivity(intent)
    }

    /**
     * 공동인증서 삭제 Activity 호출 함수
     */
    private fun startDelCertificateActivity() {
        val intent = Intent(this, IUPC03M00::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
        startActivity(intent)
    }
}