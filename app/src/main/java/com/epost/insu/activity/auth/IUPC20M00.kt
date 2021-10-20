package com.epost.insu.activity.auth

import android.content.Intent
import android.os.Bundle
import com.epost.insu.EnvConfig
import com.epost.insu.EnvConstant
import com.epost.insu.SharedPreferencesFunc
import com.epost.insu.activity.Activity_WebView
import com.epost.insu.common.CustomSQLiteFunction
import com.epost.insu.common.LogPrinter

/**
 * 인증 > PASS인증서 로그인/전자서명(화면없음)
 * @since     : 1.6.1
 * @version   : 1.0
 * @author    : NJM
 * <pre>
 * ======================================================================
 * 1.6.1    NJM_20210722    [PASS인증서 도입]
 * =======================================================================
 * copyright : (주)프로니스정보기술 | http://www.phronis.co.kr
 * </pre>
 */
class IUPC20M00 : Activity_Auth() {
    private var authMode: EnvConfig.AuthMode? = null // 요청모드("LOGIN_APP", "SIGN_APP",..)

    override fun onBackPressed() {
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        LogPrinter.CF_debug("!----------------------------------------------------------")
        LogPrinter.CF_debug("!-- IUPC20M00.onActivityResult()")
        LogPrinter.CF_debug("!----------------------------------------------------------")
        LogPrinter.CF_debug("!---- requestCode : [$requestCode] / resultCode : [$resultCode]")

        if(requestCode == EnvConfig.REQUESTCODE_ACTIVITY_WEBVIEW) {
            // (인증완료)
            if(resultCode == RESULT_OK) {
                // -- 로그인 처리
                if(EnvConfig.AuthMode.isLogin(authMode)) {
                    CF_setLogin(true, EnvConfig.AuthDvsn.PASS, data?.getStringExtra("csno").toString(), data?.getStringExtra("name").toString(), data?.getStringExtra("tempKey"))
                }
                setResult(RESULT_OK)
                finish()
            }
            else {
                setResult(RESULT_CANCELED)
                finish()
            }
        }
    }

    override fun setInit() {
        LogPrinter.CF_debug("!----------------------------------------------------------")
        LogPrinter.CF_debug("!-- IUPC20M000.setInit() --PASS인증서")
        LogPrinter.CF_debug("!----------------------------------------------------------")

        if (intent.hasExtra(EnvConstant.KEY_INTENT_AUTH_MODE)) {
            authMode = intent.getSerializableExtra(EnvConstant.KEY_INTENT_AUTH_MODE) as EnvConfig.AuthMode?
        }

        LogPrinter.CF_debug("!---- authMode : " + authMode.toString())
    }

    override fun setUIControl() {
        // -- 타이틀바 세팅
        setTitleBarUI()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        startWebview()
    }

    /**
     * 타이틀바 레이아웃 세팅
     */
    private fun setTitleBarUI() {
    }

    override fun onPause() {
        super.onPause()
        LogPrinter.CF_debug("!-----------------------------------------------------------")
        LogPrinter.CF_debug("!-- IUPC20M000.onPause() --PASS인증서")
        LogPrinter.CF_debug("!-----------------------------------------------------------")
        overridePendingTransition(0, 0) // end anition 해지
    }

    override fun onResume() {
        super.onResume()
        LogPrinter.CF_debug("!-----------------------------------------------------------")
        LogPrinter.CF_debug("!-- IUPC20M000.onResume() --PASS인증서")
        LogPrinter.CF_debug("!-----------------------------------------------------------")
    }

    /**
     * PASS인증서 웹뷰 호출
     */
    private fun startWebview() {
        LogPrinter.CF_debug("!-----------------------------------------------------------")
        LogPrinter.CF_debug("!-- IUPC20M000.startWebview() --PASS인증서")
        LogPrinter.CF_debug("!-----------------------------------------------------------")

        val signType = if (EnvConfig.AuthMode.isLogin(authMode)) "01" else "99"

        var csno = ""
        if(SharedPreferencesFunc.getFlagRegAuthDvsn(applicationContext, EnvConfig.AuthDvsn.PASS)) {
            csno = CustomSQLiteFunction.getLastLoginCsno(applicationContext)
        }
        LogPrinter.CF_debug("!---- csno : $csno")
        val url = EnvConfig.host_url + EnvConfig.URL_PASS_AUTH + "?appType=A&signType=$signType&csno=$csno"
        val title = if ("01" == signType) "로그인" else "본인확인"

        LogPrinter.CF_debug("!---- title : $title")
        LogPrinter.CF_debug("!---- csno  : $csno")
        LogPrinter.CF_debug("!---- url   : $url")

        // -- startActivity
        val intent = Intent(this, Activity_WebView::class.java)
        intent.putExtra("webViewDvsn", 0)
        intent.putExtra("allowNewWindow", true)
        intent.putExtra("url", url)
        intent.putExtra("viewTitle", title)
        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
        startActivityForResult(intent, EnvConfig.REQUESTCODE_ACTIVITY_WEBVIEW)
    }
}