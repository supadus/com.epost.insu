package com.epost.insu.activity

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.view.View
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.ImageButton
import android.widget.ProgressBar
import android.widget.TextView
import com.epost.insu.EnvConfig
import com.epost.insu.R
import com.epost.insu.common.DeprecatedFunc
import android.webkit.WebChromeClient




/**
 * @copyright : (주)프로니스정보기술 | http://www.phronis.co.kr
 *
 * @project   : 모바일슈랑스 구축
 * @pakage    : com.epost.insu.activity
 * @fileName  : IUCOG0M01.java
 *
 * @Title     : 공통 > 설정 > 이용약관 (화면 ID : IUCOG0M01) - #48
 * @author    : 이수행
 * @created   : 2017-08-28
 * @version   : 1.0
 *
 * @note      : - 전자금융거래 기본약관 / 전자금융 서비스 이용약관을 WebView로 보인다.
 * ======================================================================
 * 수정 내역
 * NO      날짜          작업자       내용
 * 01      2017-08-28    이수행       최초 등록
 * =======================================================================
 */
class IUCOG0M01 : Activity_Default() {
    private val subUrl_web = "/CO/IUCOG0M01.do"

    override fun setInit() {
        setContentView(R.layout.iucog0m01)
    }
    var progressBar:ProgressBar?=null
    @SuppressLint("SetJavaScriptEnabled")
    override fun setUIControl() {

        // 타이틀바 세팅
        setTitleBarUI()
        val tmp_webView = findViewById<WebView>(R.id.webView)
        var progressBar  = findViewById<ProgressBar>(R.id.progressBar)
        tmp_webView.webViewClient = object : WebViewClient() {
            override fun onPageFinished(view: WebView, url: String) {
                super.onPageFinished(view, url)
                view.clearCache(true)

            }

            override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                super.onPageStarted(view, url, favicon)

            }
        }


        tmp_webView.webChromeClient = object : WebChromeClient() {
            override fun onProgressChanged(view: WebView, progress: Int) {


                // Return the app name after finish loading
                if(progress!=100){
                    progressBar?.visibility =View.VISIBLE
                }
                else{
                    progressBar?.visibility =View.GONE
                }

                progressBar?.progress = progress

            }
        }


        tmp_webView.settings.javaScriptEnabled = true
        tmp_webView.setBackgroundColor(DeprecatedFunc.CF_getColor(applicationContext, R.color.colorBackground, null))
        tmp_webView.loadUrl(EnvConfig.host_url + subUrl_web)
    }

    /**
     * 타이틀바 레이아웃 세팅
     */
    private fun setTitleBarUI() {

        // 타이틀 세팅
        val tmp_title = findViewById<TextView>(R.id.title_bar_textTitle)
        tmp_title.text = resources.getString(R.string.title_guide_service)

        // left 버튼 세팅
        val tmp_btnLeft = findViewById<ImageButton>(R.id.title_bar_imgBtnLeft)
        tmp_btnLeft.visibility = View.VISIBLE
        tmp_btnLeft.setOnClickListener { finish() }
    }
}