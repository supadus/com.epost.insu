package com.epost.insu.activity.auth

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import com.epost.insu.EnvConfig
import com.epost.insu.EnvConfig.AuthDvsn.getAuthDvsn
import com.epost.insu.IntentManager
import com.epost.insu.LoginManger.CF_setLogin
import com.epost.insu.R
import com.epost.insu.SharedPreferencesFunc
import com.epost.insu.activity.Activity_Default
import com.epost.insu.common.CustomSQLiteFunction
import com.epost.insu.common.LogPrinter

/**
 * 인증 > 공동인증서(로그인/전자서명 > 인증 완료 (웹요청)
 * @since     :
 * @version   : 1.2
 * @author    : LSH
 * <pre>
 * ======================================================================
 * LSH_20170905    최초 등록
 * NJM_20200212    웹뷰에서 공동인증 호출시 구분값 추가  TODO : 웹뷰에서 (납입등 후에)공동인증 호출시 로직 수정필요
 * =======================================================================
 * copyright : (주)프로니스정보기술 | http://www.phronis.co.kr
</pre> *
 */
class IUCOC3M00 : Activity_Auth() {
    var isWebViewCall: Boolean = false

    override fun setInit() {
        setContentView(R.layout.iucoc3m00)
    }

    override fun setUIControl() {
        // 타이틀바 세팅
        setTitleBarUI()
        val tmp_img: ImageView = findViewById(R.id.imageView)
        val tmp_Text: TextView = findViewById(R.id.textView)
        tmp_img.setImageResource(R.drawable.ic_complete_cert)
        tmp_img.setContentDescription(getResources().getString(R.string.desc_img_auth_certificate_c))
        tmp_Text.setText(getResources().getString(R.string.guide_auth_cert))
        val tmp_btn: Button = findViewById(R.id.btnFill)
        tmp_btn.setText(getResources().getString(R.string.btn_ok))
        tmp_btn.setOnClickListener(object : View.OnClickListener {
            override fun onClick(v: View) {
                if (!isWebViewCall) {
                    IntentManager.moveTaskToBack(this@IUCOC3M00,true)
                }

                var tmp_csNo = ""
                var tmp_loginType = ""
                var tmp_tempKey = ""
                var tmp_name = ""
                if (SharedPreferencesFunc.getFlagLogin(applicationContext)) {
                    tmp_csNo = CustomSQLiteFunction.getLastLoginCsno(applicationContext)
                    tmp_loginType = CustomSQLiteFunction.getLastLoginAuthDvsnNum(applicationContext)
                    tmp_tempKey = SharedPreferencesFunc.getWebTempKey(applicationContext)
                    tmp_name = CustomSQLiteFunction.getLastLoginName(applicationContext)


                    val auth = EnvConfig.AuthDvsn.getAuthDvsn(tmp_loginType)
                    CF_setLogin(true, auth, tmp_csNo, tmp_name,tmp_tempKey)
                }


                //  finish()
            }
        })
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        LogPrinter.CF_debug("!----------------------------------------------------------")
        LogPrinter.CF_debug("!-- IUCOC3M00.onCreate()")
        LogPrinter.CF_debug("!----------------------------------------------------------")
        isWebViewCall = false
        if (getIntent() != null) {
            if (getIntent().hasExtra("isWebViewCall")) {
                isWebViewCall = getIntent().getExtras()!!.getBoolean("isWebViewCall")
            }
        }
        LogPrinter.CF_debug("!---- isWebViewCall : " + isWebViewCall)
    }

    override fun onStop() {
        super.onStop()
        finish()
    }

    /**
     * 타이틀바 레이아웃 세팅
     */
    private fun setTitleBarUI() {
        // 타이틀 세팅
        val tmp_title: TextView = findViewById(R.id.title_bar_textTitle)
        tmp_title.setText(getResources().getString(R.string.title_complete_cert))

        // left 버튼 세팅
        val tmp_btnLeft: ImageButton = findViewById(R.id.title_bar_imgBtnLeft)
        tmp_btnLeft.setVisibility(View.VISIBLE)
        tmp_btnLeft.setOnClickListener(object : View.OnClickListener {
            override fun onClick(v: View) {
                finish()
            }
        })
    }
}