package com.epost.insu.activity

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.view.View
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.ImageButton
import android.widget.ProgressBar
import android.widget.TextView
import com.epost.insu.EnvConfig
import com.epost.insu.R
import com.epost.insu.common.DeprecatedFunc

/**
 * 공통 > 설정 > 개인정보취급방침
 * @since     :
 * @version   : 1.1
 * @author    : LSH
 * <pre>
 * ======================================================================
 * LSH_20170912    최초 등록
 * 1.5.2    NJM_20210322    [subUrl 공통파일로 변경]
 * =======================================================================
 * copyright : (주)프로니스정보기술 | http://www.phronis.co.kr
</pre> *
 */
class IUCOG0M02 : Activity_Default() {
    override fun setInit() {
        setContentView(R.layout.iucog0m02)
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

        tmp_webView.setWebChromeClient(object : WebChromeClient() {
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
        })


        tmp_webView.settings.javaScriptEnabled = true
        tmp_webView.setBackgroundColor(DeprecatedFunc.CF_getColor(applicationContext, R.color.colorBackground, null))
        tmp_webView.loadUrl(EnvConfig.host_url + EnvConfig.URL_WEBVIEW_PERSONAL)
    }

    /**
     * 타이틀바 레이아웃 세팅
     */
    private fun setTitleBarUI() {
        // 타이틀 세팅
        val tmp_title = findViewById<TextView>(R.id.title_bar_textTitle)
        tmp_title.text = resources.getString(R.string.title_privacy_guide)

        // left 버튼 세팅
        val tmp_btnLeft = findViewById<ImageButton>(R.id.title_bar_imgBtnLeft)
        tmp_btnLeft.visibility = View.VISIBLE
        tmp_btnLeft.setOnClickListener { finish() }
    }
}