package com.epost.insu.activity

import android.annotation.SuppressLint
import android.annotation.TargetApi
import android.app.Activity
import android.content.*
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.*
import android.view.View
import android.webkit.*
import android.webkit.WebView.WebViewTransport
import android.widget.*
import com.epost.insu.*
import com.epost.insu.activity.auth.IUCOB0M00
import com.epost.insu.common.*
import com.epost.insu.dialog.CustomDialog
import android.content.Intent
import android.view.MotionEvent
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import kotlin.system.exitProcess


/**
 * WebView 실행 Activity
 * @since     :
 * @version   : 2.1
 * @author    : NJM
 * <pre>
 *  App 사용하는 url, 상수 등의 값 정의 클래스
 * ======================================================================
 * 0.0.0    NJM_20190206    최초 등록
 * 0.0.0    NJM_20191216    웹뷰 기능 보강
 * 0.0.0    NJM_20190117    제제로고 삭제
 * 0.0.0    NJM_20200212    웹뷰에서 공동인증 호출시 구분값 추가
 * 0.0.0    NJM_20200214    프로그레스바 변경, 웹뷰 하드웨어 가속
 * 0.0.0    NJM_20200319    프로그레스바 앱버전으로 변경(ajax만 web사용)
 * 0.0.0    NJM_20200408    pdf 다운로드 리시버 개선 - 종료시 리시버 해제 fileDownloadUnRegisterReceiver()
 * 0.0.0    LKM_20200428    웹뷰 새창 생성기능 추가
 * 0.0.0    NJM_20200601    웹뷰에서 포스트페이 납입 지원
 * 0.0.0    NJM_20200904    구비서류 안내영상 버튼 추가 - https에서 http 열기(롤리팝 이상부터)
 * 0.0.0    NJM_20200216    arrLoginMenuPage 리스트 EnvConfig로 이동
 * 1.5.4    NJM_20210426    [유저에이전트 변경] v_137 삭제 -> 현재 버전 표기 ex)_v1.5.4
 * 1.5.4    NJM_20210504    [간편인증 전자서명 추가] 웹요청 간편인증 추가, 청구시 간편인증 전자서명 추가
 * 1.5.6    NJM_20210531    [파일다운로드 개선] 정상:doc, pdf, ppt, xls, xlsx (비정상:docx,hwp,pptx) -- 비정상 추가 수정 필요함
 * 1.5.8    NJM_20210630    [금융인증서 도입] 자바스크립트 브릿지 추가
 * 1.6.1    NJM_20210722    [PASS인증서 도입]
 * 1.6.2    NJM_20210802    [2021년 대우 취약점] 2차본 : 부적절한 예외 처리
 * 1.6.3    NJM_20211007    [API30 대응] targetApi 29 -> 30 변경에 따른 코틀린 오류 수정
 * =======================================================================
 * copyright : (주)프로니스정보기술 | http://www.phronis.co.kr
 * </pre>
 */
public open class Activity_WebView : Activity_Default() {
    private var mContext: Context? = null

    // Intent 변수
    private var webViewDvsn = 0 // 레이아웃유형( 0:전체화면, 1:상단바)
    private var allowNewWindow = true // 새창(차일드뷰) 허용( false:불가, true:허용)
    private var url = "" // 웹뷰url 주소
    private var viewTitle = "" // 화면 서브 타이틀
    private var progressBar: ProgressBar? = null // 프로그레스

    // 웹뷰변수 선언
    public var mWebview: WebView? = null // 웹뷰
    private var mWebSettings: WebSettings? = null // 웹뷰셋팅
    private var btnLeft: ImageButton? = null  // 타이틀바 좌측버튼(뒤로가기)


     var showAllMenu = false


    //private long  pressedTime  = 0  // 키입력시간 (뒤로가기용)
    private val retURL = "" // 리턴 URL
    public var menuNo = ""
    public var menuLink = ""
    private var isBrowserCall = false


    /**
     * URL 로딩방식 설정
     * @param url   String
     * @return      boolean (true :다른 앱에서 처리, false : 웹뷰에서 처리)
     */
    private fun checkUrl(url: String): Boolean {
        LogPrinter.CF_debug("!----------------------------------------------------------")
        LogPrinter.CF_debug("!-- (mWebview)WebViewClient.checkUrl()")
        LogPrinter.CF_debug("!----------------------------------------------------------")
        LogPrinter.CF_debug("!---- 웹뷰 URI(uri.getPath()) :$url")
        // --<1> 전화 url : 전화 앱 호출
        if (url.startsWith("tel:")) {
            val dial = Intent(Intent.ACTION_DIAL, Uri.parse(url))
            startActivity(dial)
            return true
        }
        //로그인페이지
        /*
        else if (url.contains("CO/IUCO05M00.do") ){
            mWebview?.loadUrl("javascript:loginAppPage()")
            return true
        }
         */
        else if (url.startsWith("https:") || url.startsWith("http:")) {
            // --<2> 특정 url 웹브라우저실행
            return if (url.contains("bot.epostbank")
                || url.contains("botdv.epostbank") // 챗봇 : 마이크 권한 문제로 임시 필터링
                || url.contains("vod1.postfc.kr")
            ) {      // 동영상서버
                // -- 브라우저실행
                mWebview?.loadUrl(url)
                //WebBrowserHelper.callWebBrowser(applicationContext, url)

                true
            } else {
                false
            }
        } else if (url.startsWith("intent:")) {
            try {
                val intent = Intent.parseUri(url, Intent.URI_INTENT_SCHEME)

                // ---- 웹뷰호출시에는 로그인 완료후 앱을 닫지 않는다. (isWebViewCall:true)
                intent.putExtra("isWebViewCall", true)

                // --<2> 공동인증서, 간편인증(핀/지문/패턴) 전자서명 호출
                return if (url.contains(EnvConfig.webHost_checkCert) || url.contains(EnvConfig.webHost_singPin)
                    || url.contains(EnvConfig.webHost_singBio) || url.contains(EnvConfig.webHost_singPattern)
                ) {
                    startActivity(intent)
                    true
                } else if (url.contains(EnvConfig.webHost_reqCert)) {
                    startActivity(intent)
                    true
                } else if (isExistInfo(intent, mContext) || isExistPackage(intent, mContext)) {
                    startActivity(intent)
                    true
                } else {
                    startActivity(
                        Intent(
                            Intent.ACTION_VIEW,
                            Uri.parse("market://details?id=" + intent.getPackage())
                        )
                    )
                    true
                }
            } catch (e: NullPointerException) {
                e.message
            } catch (e: Exception) {
                e.message
            }
        }

        // ---- 해당되지 않는 URL은 false 처리
        return false
    }

    private fun isExistInfo(intent: Intent?, context: Context?): Boolean {
        return try {
            intent != null && context!!.packageManager.getPackageInfo(
                intent.getPackage()!!,
                PackageManager.GET_ACTIVITIES
            ) != null
        } catch (e: PackageManager.NameNotFoundException) {
            e.message
            false
        }
    }

    private fun isExistPackage(intent: Intent?, context: Context?): Boolean {
        return intent != null && context!!.packageManager.getLaunchIntentForPackage(intent.getPackage()!!) != null
    }



    /**
     * 백버튼 터치시 웹뷰페이지 뒤로 가기.
     */
    override fun onBackPressed() {
        LogPrinter.CF_debug("WebView onBackPressed")

        if(childWebView!=null){
            LogPrinter.CF_debug("!----------------------------------------------------------")
            LogPrinter.CF_debug("!-- (childWebView)onBackPressed")
            LogPrinter.CF_debug("!----------------------------------------------------------")
            childWebView?.destroy()
            mWebview?.removeView(childWebView)
            childWebView = null
        }
        else{
            goBack()
        }



    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        LogPrinter.CF_debug("!----------------------------------------------------------")
        LogPrinter.CF_debug("!-- Activity_WebView.onActivityResult()")
        LogPrinter.CF_debug("!----------------------------------------------------------")
        LogPrinter.CF_debug("!-- requestCode : [$requestCode] / resultCode : [$resultCode]")

        // -- (로그인 완료) startIUCOB0M00() 콜백
        if (requestCode == EnvConfig.REQUESTCODE_ACTIVITY_IUCOB0M00 && resultCode == RESULT_OK) {

            if (SharedPreferencesFunc.getFlagLogin(applicationContext)) {
                webLoginSession()
            }

        }



    }


     fun webLoginSession(){

        var csno = ""
        if (SharedPreferencesFunc.getFlagLogin(applicationContext)) {
            csno = CustomSQLiteFunction.getLastLoginCsno(applicationContext)
        }
        val loginType = CustomSQLiteFunction.getLastLoginAuthDvsnNum(applicationContext)
        val tempKey = SharedPreferencesFunc.getWebTempKey(applicationContext)
        var url =  EnvConfig.host_url + EnvConfig.URL_WEB_LINK + "?page=-1" + "&csno=" + csno + "&loginType=" + loginType + "&tempKey=" + tempKey + "&deviceType=A" + "&version=" + EnvConfig.appVersion
        url += "&menuNo=$menuNo&link=$menuLink"
        mWebview?.loadUrl(url)
        LogPrinter.CF_debug("!----------------------------------------------------------")
        LogPrinter.CF_debug("!-- AndroidBridge  return url $url")


            menuNo = ""
            menuLink = ""

    }


    val broadcastReceiver= object:BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {

            var action = intent?.action ?: ""
            when(action){
                EnvConfig.BROADCAST_APPLOGINSUCCESS ->{
                    if (SharedPreferencesFunc.getFlagLogin(this@Activity_WebView)) {
                        webLoginSession()
                    }
                }
            }

        }

    }



    override fun setInit() {
        LogPrinter.CF_debug("!----------------------------------------------------------")
        LogPrinter.CF_debug("!-- Activity_WebView.setInit()")
        LogPrinter.CF_debug("!----------------------------------------------------------")
        mContext = this

        // -- Intent Data Get!!
        val intent = Intent(this.intent)
        url = intent.getStringExtra("url") ?: ""



        webViewDvsn = intent.getIntExtra("webViewDvsn", 0)
        allowNewWindow = intent.getBooleanExtra("allowNewWindow", true)
        viewTitle = intent.getStringExtra("viewTitle") ?: ""
        // -- 타이틀 세팅
        if ("" == viewTitle) {
            viewTitle = "우체국보험"
        }


        var intentFilter = IntentFilter()
        intentFilter.addAction(EnvConfig.BROADCAST_APPLOGINSUCCESS)
        //메인 화면일경우 브로드캐스트 등록
        if(this is IUCOA0M00){
            intentFilter.addAction(EnvConfig.BROADCAST_MAIN)
        }
        //메인화면이아니면
        else{
            setContentView(R.layout.c_webview)
            findViewById<RelativeLayout>(R.id.title_bar_root).visibility = View.VISIBLE
            var text= findViewById<TextView>(R.id.title_bar_textTitle)
            text.setText(viewTitle)
            var button= findViewById<ImageButton>(R.id.title_bar_imgBtnLeft)
            button.setOnClickListener {
                finish()
            }

        }
        LocalBroadcastManager.getInstance(this).registerReceiver(broadcastReceiver,intentFilter)
        LogPrinter.CF_debug("!---- webViewDvsn   : $webViewDvsn")
        LogPrinter.CF_debug("!---- allowNewWindow: $allowNewWindow")
        LogPrinter.CF_debug("!---- url           : $url")
        LogPrinter.CF_debug("!---- viewTitle     : $viewTitle")
        // --<> (전체화면 레이아웃)






    }


    var rel_progress:RelativeLayout?=null
    var childWebView:WebView?=null
    @SuppressLint("SetJavaScriptEnabled", "ClickableViewAccessibility")
    override fun setUIControl() {
        LogPrinter.CF_debug("!----------------------------------------------------------")
        LogPrinter.CF_debug("!-- Activity_WebView.setUIControl()")
        LogPrinter.CF_debug("!----------------------------------------------------------")


        // -- 웹뷰
        rel_progress = findViewById(R.id.rel_progress)
        rel_progress?.setOnTouchListener(object:View.OnTouchListener {
            override fun onTouch(v: View?, event: MotionEvent?): Boolean {
                return true
            }

        })
        progressBar = findViewById(R.id.progress_page)
        mWebview = findViewById(R.id.webView)
        mWebview?.webChromeClient = object : WebChromeClient() {
            override fun onProgressChanged(view: WebView, newProgress: Int) {
                super.onProgressChanged(view, newProgress)
                LogPrinter.CF_debug("!----------------------------------------------------------")
                LogPrinter.CF_debug("!-- (mWebview)WebChromeClient.onProgressChanged() --로딩율 : $newProgress")
                LogPrinter.CF_debug("!----------------------------------------------------------")


                // Return the app name after finish loading
                if(newProgress!=100){
                    rel_progress?.visibility =View.VISIBLE
                    mWebview?.isEnabled =false
                }
                else{
                    rel_progress?.visibility =View.GONE
                    mWebview?.isEnabled =true
                }

                progressBar?.progress = newProgress



            }

            override fun onCreateWindow(
                view: WebView,
                dialog: Boolean,
                userGesture: Boolean,
                resultMsg: Message
            ): Boolean {

                LogPrinter.CF_debug("!----------------------------------------------------------")
                LogPrinter.CF_debug("!-- (mWebview)WebChromeClient.onCreateWindow() --차일드뷰 생성 ")
                LogPrinter.CF_debug("!----------------------------------------------------------")
                val c = mWebview?.context
                c?.let { Wcontext ->

                    childWebView = WebView(Wcontext)
                    childWebView?.settings?.javaScriptEnabled = true
                    childWebView?.settings?.allowFileAccess =true
                    childWebView?.setDownloadListener(MyDownloadListener(childWebView)) // 파일다운로드 리스너 (파일 다운로드시 호출됨)
                    childWebView?.webViewClient = PopupWebViewClient() // 웹뷰클라이언트 설정
                    childWebView?.layoutParams = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.MATCH_PARENT
                    )
                    childWebView?.webChromeClient = object : WebChromeClient() {
                        override fun onProgressChanged(view: WebView, newProgress: Int) {
                            super.onProgressChanged(view, newProgress)
                            LogPrinter.CF_debug("!----------------------------------------------------------")
                            LogPrinter.CF_debug("!-- (childWebView)WebChromeClient.onProgressChanged() -- 로딩율 : $newProgress")
                            LogPrinter.CF_debug("!----------------------------------------------------------")
                            if(newProgress!=100){
                                rel_progress?.visibility =View.VISIBLE
                            }
                            else{
                                rel_progress?.visibility =View.GONE
                            }

                            progressBar?.progress = newProgress
                        }

                        override fun onCloseWindow(childWebViewClose: WebView) {
                            LogPrinter.CF_debug("!----------------------------------------------------------")
                            LogPrinter.CF_debug("!-- (childWebView)WebChromeClient.onCloseWindow()")
                            LogPrinter.CF_debug("!----------------------------------------------------------")
                            childWebViewClose.destroy()
                            mWebview?.removeView(childWebView)
                            childWebView = null
                        }
                    }
                    mWebview?.addView(childWebView)
                    val transport = resultMsg.obj as WebViewTransport
                    transport.webView = childWebView
                    resultMsg.sendToTarget()
                }
                return true
            }
        }

        // -----------------------------------------------------------------------------------------
        // 웹뷰 옵션 설정
        // -----------------------------------------------------------------------------------------
        mWebview?.setLayerType(View.LAYER_TYPE_HARDWARE, null) // -- 하드웨어 가속 (성능향상?)
        mWebview?.setDownloadListener(MyDownloadListener()) // -- 파일다운로드 리스너 (파일 다운로드시 호출됨)
        val bridge = AndroidBridge(this, mWebview)
        mWebview?.addJavascriptInterface(bridge, "appCall") // -- 안드로이드<->자바스크립트 통신 브릿지 설정
        mWebview?.webViewClient = MyWebViewClient() // -- 웹뷰클라이언트 설정
        mWebview?.setNetworkAvailable(true) // 웹뷰 네트워크 사용설정
        // -----------------------------------------------------------------------------------------
        // 웹뷰세팅 옵션 설정
        // -----------------------------------------------------------------------------------------
        mWebSettings = mWebview?.settings // 세부 세팅 등록
        mWebSettings?.useWideViewPort = true
        mWebSettings?.javaScriptEnabled = true // -- 자바스크립트 사용가능 설정
        mWebSettings?.javaScriptCanOpenWindowsAutomatically = true
        //      mWebSettings.setCacheMode(WebSettings.LOAD_CACHE_ELSE_NETWORK) // -- 캐시설정 (뒤로가기시 cache_miss 발생으로 인하여 추가함)
        mWebSettings?.cacheMode = WebSettings.LOAD_NO_CACHE // -- 캐시설정 (납입 후에 기존 납입월이 캐시되어 변경)
        mWebSettings?.domStorageEnabled = true
        mWebSettings?.loadWithOverviewMode = true

        // https에서 http 열기(롤리팝 이상부터)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            mWebSettings?.mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
        }


        if (allowNewWindow) {
            mWebview?.settings?.setSupportMultipleWindows(true) // -- 다중 윈도우 창 생성 허용
        }

        val userAgent = mWebview?.settings?.userAgentString // -- userAgent 설정
        var isMain = this is IUCOA0M00
        if(isMain){
            mWebview?.settings?.userAgentString = userAgent + EnvConfig.userAgentAdd
        }
        else{
            mWebview?.settings?.userAgentString = userAgent + EnvConfig.userAgentAdd  + "_NOHEADER"
        }
        // -- 웹뷰 디버깅 설정 (개발에서만)
        WebView.setWebContentsDebuggingEnabled(EnvConfig.mFlagShowLog)
        try {
            val cookieManager = CookieManager.getInstance()
            cookieManager.removeSessionCookies { LogPrinter.CF_debug("!---- 쿠키(removeSessionCookies)삭제") }
            cookieManager.removeAllCookies { LogPrinter.CF_debug("!---- 쿠키(removeAllCookies)삭제") }
        } catch (e: NullPointerException) {
            e.message
        } catch (e: Exception) {
            e.message
        }

         mWebview?.clearCache(true)
         mWebview?.clearHistory()





        if (!url.isEmpty()) {

            //앱에 로그인된 상태라면 웹뷰에 로그인을 연동
            if (SharedPreferencesFunc.getFlagLogin(applicationContext)) {
                var tempUrl= url.replace(EnvConfig.host_url,"")
                menuNo = intent.getStringExtra("menuNo") ?: ""
                menuLink = intent.getStringExtra("menuLink") ?: tempUrl
                webLoginSession()
            }
            else {
                mWebview?.loadUrl(url)
            }

        }






    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        LogPrinter.CF_debug("!----------------------------------------------------------")
        LogPrinter.CF_debug("!-- Activity_WebView.onCreate()")
        LogPrinter.CF_debug("!----------------------------------------------------------")
    }

    override fun onStart() {
        super.onStart()
        LogPrinter.CF_debug("!----------------------------------------------------------")
        LogPrinter.CF_debug("!-- Activity_WebView.onStart()")
        LogPrinter.CF_debug("!----------------------------------------------------------")
    }

    override fun onResume() {
        super.onResume()
        LogPrinter.CF_debug("!----------------------------------------------------------")
        LogPrinter.CF_debug("!-- Activity_WebView.onResume()")
        LogPrinter.CF_debug("!----------------------------------------------------------")
    }

    override fun onPause() {
        super.onPause()
        LogPrinter.CF_debug("!----------------------------------------------------------")
        LogPrinter.CF_debug("!-- Activity_WebView.onPause()")
        LogPrinter.CF_debug("!----------------------------------------------------------")
    }

    override fun onDestroy() {
        super.onDestroy()
        LogPrinter.CF_debug("!----------------------------------------------------------")
        LogPrinter.CF_debug("!-- Activity_WebView.onDestroy()")
        LogPrinter.CF_debug("!----------------------------------------------------------")

        // -- 다운로드 리시버 해제
        WebFileDownloadHelper.fileDownloadUnRegisterReceiver(mContext)

        LocalBroadcastManager.getInstance(this).unregisterReceiver(broadcastReceiver)

    }


    private fun goLogin(v: View) {

        // --<1> (로그아웃 상태)
        if (!SharedPreferencesFunc.getFlagLogin(applicationContext)) {

            startLoginActivity()

        } else {
            showCustomDialog(
                resources.getString(R.string.dlg_logout),
                v
            ) { dialog -> // --<2> (접근성OFF)
                if (!(dialog as CustomDialog).CF_getCanceled()) {
                    // 데이터 로그아웃 처리
                    CustomApplication.CF_logOut(applicationContext)
                    //CommonFunction.CF_showCustomAlertDilaog(IUCOA0M00.this, getResources().getString(R.string.dlg_complete_logout), getResources().getString(R.string.btn_ok))
                } else if (CommonFunction.CF_checkAccessibilityTurnOn(applicationContext)) {
                    clearAllFocus()
                    v.requestFocus()
                }
                v.isFocusableInTouchMode = false
            }
        }
    }

    private fun goBack() {

            if (mWebview!!.canGoBack()) {
                mWebview!!.goBack()
            } else {
                finish()
            }

    }

//    fun goForward() {
//        if (mWebview!!.canGoForward()) {
//            mWebview!!.goForward()
//        }
//    }

    /**
     * 로그인 목록 Activity 호출 함수
     */
    private fun startLoginActivity() {
        LogPrinter.CF_debug("!----------------------------------------------------------")
        LogPrinter.CF_debug("!-- Activity_WebView.startLoginActivity()")
        LogPrinter.CF_debug("!----------------------------------------------------------")

        val intent = Intent(this, IUCOB0M00::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
        startActivityForResult(intent, EnvConfig.REQUESTCODE_ACTIVITY_IUCOB0M00)
    }


    /**
     * (메뉴바형)웹페이지 메뉴 버튼 클릭
     * @param p_view View
     */
    private fun onClickReqWeb(p_view: View) {
        LogPrinter.CF_debug("!----------------------------------------------------------")
        LogPrinter.CF_debug("!-- Activity_WebView.onClickReqWeb()")
        LogPrinter.CF_debug("!----------------------------------------------------------")
        val pageNumber = p_view.tag as String

        // csno , loginType 값으로 로그인 상태를 확인한다.
        //------------------------------------------------------------------------------------------
        //  Web페이지에서 로그인필수가 아닌 메뉴도 로그인시키는 오류가 있기에, App로그인 상태가 아니면
        //  csno를 empty로 변경함.
        //------------------------------------------------------------------------------------------
        var csno = ""
        if (SharedPreferencesFunc.getFlagLogin(applicationContext)) {
            csno = CustomSQLiteFunction.getLastLoginCsno(applicationContext)
        }
        val loginType = CustomSQLiteFunction.getLastLoginAuthDvsnNum(applicationContext)
        val tempKey = SharedPreferencesFunc.getWebTempKey(applicationContext)
        var flagLaunchWeb = true

        // -----------------------------------------------------------------------------------------
        //  로그인 필수 메뉴 요청 : App 로그인 상태 검사 ==> 로그인 상태가 아닌경우 Dialog 팝업
        // -----------------------------------------------------------------------------------------
        if (EnvConfig.arrLoginMenuPage.contains(pageNumber)) {
            if (!SharedPreferencesFunc.getFlagLogin(applicationContext)) {
                flagLaunchWeb = false
                showCustomDialog(
                    resources.getString(R.string.dlg_need_login_menu),
                    p_view
                ) { dialog ->
                    if (!(dialog as CustomDialog).CF_getCanceled()) {
                        startLoginActivity()
                    } else if (CommonFunction.CF_checkAccessibilityTurnOn(this@Activity_WebView.applicationContext)) {
                        clearAllFocus()
                        p_view.requestFocus()
                    }
                    p_view.isFocusableInTouchMode = false
                }
            }
        }

        // -----------------------------------------------------------------------------------------
        //  Web 브라우져 오픈 : 현재 로그인 필수 메뉴가 아니여도 Web에서 로그인되는 문제가 있음.
        //  :: App에서 csnNo  empty 전달로 로그인처리 되는 것 막음.=> Web에서 정상적으로 로그인 체크를
        //  하면 csNo empty 변환 없이 전송해도 됨.
        // -----------------------------------------------------------------------------------------
        if (flagLaunchWeb) {
            var url =
                EnvConfig.host_url + EnvConfig.URL_WEB_LINK + "?page=" + pageNumber + "&csno=" + csno + "&loginType=" + loginType + "&tempKey=" + tempKey + "&deviceType=A" + "&version=" + EnvConfig.appVersion

            url += "&menuNo=$menuNo&link="

            if (CommonFunction.CF_checkAccessibilityTurnOn(this)) {
                showCustomDialog(
                    resources.getString(R.string.dlg_accessible_move_smart_web),
                    p_view
                ) { dialog ->
                    if (!(dialog as CustomDialog).CF_getCanceled()) {
                        mWebview?.loadUrl(url)
                        //WebBrowserHelper.callWebBrowser(applicationContext, url)
                    } else {
                        clearAllFocus()
                        p_view.requestFocus()
                    }
                    p_view.isFocusableInTouchMode = false
                }
            } else {
                mWebview?.loadUrl(url)
                //WebBrowserHelper.callWebBrowser(applicationContext, url)
            }
        }
    }

    /**
     * 웹뷰 인터페이스
     */
    private inner class AndroidBridge(var mContext: Context, var mWebView2: WebView?) {
        var mHandler: Handler = Handler()





        //setAllMenu
        @JavascriptInterface
        fun setAllMenu(bool: Boolean) {
            LogPrinter.CF_debug("!----------------------------------------------------------")
           // LogPrinter.CF_debug("!-- AndroidBridge.setAllMenu($bool)")
            LogPrinter.CF_debug("!----------------------------------------------------------")
            showAllMenu =  bool
        }



        //menuNo셋팅
        @JavascriptInterface
        fun setMenuNo(menuNoScript: String) {
            LogPrinter.CF_debug("!----------------------------------------------------------")
            LogPrinter.CF_debug("!-- AndroidBridge.setMenuNo($menuNo)")
            LogPrinter.CF_debug("!----------------------------------------------------------")
            menuNo =  menuNoScript


        }


        //네이티브 로그인 페이지 호출
        @JavascriptInterface
        fun webToAppLogin(tempKey: String?) {
            LogPrinter.CF_debug("!----------------------------------------------------------")
            LogPrinter.CF_debug("!-- AndroidBridge.webToAppLogin()")
            LogPrinter.CF_debug("!----------------------------------------------------------")
            runOnUiThread {
                if(tempKey!=null && tempKey.isNotEmpty()){
                    SharedPreferencesFunc.setWebTempKey(this@Activity_WebView,tempKey)
                }
                IntentManager.startIUCOB0M00_Login(
                    this@Activity_WebView,
                    EnvConfig.REQUESTCODE_ACTIVITY_WEBVIEW
                )
            }
        }

        //앱로그아웃
        @JavascriptInterface
        fun webToAppLogout(){
            LogPrinter.CF_debug("!----------------------------------------------------------")
            LogPrinter.CF_debug("!-- AndroidBridge.webToAppLogout()")
            LogPrinter.CF_debug("!----------------------------------------------------------")

            runOnUiThread {
                if (SharedPreferencesFunc.getFlagLogin(applicationContext)) {

                    // -- 현재 App 로그인 상태인 경우 : 로그아웃 및 메인화면으로 이동
                    // 데이터 로그아웃 처리
                    CustomApplication.CF_logOut(applicationContext)
                    //앱 재시작
                    /*
                    val packageManager = packageManager
                    val intent = packageManager.getLaunchIntentForPackage(packageName)
                    val componentName = intent!!.component
                    val mainIntent = Intent.makeRestartActivityTask(componentName)
                    startActivity(mainIntent)
                    exitProcess(0)
                    */

                }

            }

        }
        @JavascriptInterface
        fun setLinkUrl(url: String) {
            LogPrinter.CF_debug("!----------------------------------------------------------")
            LogPrinter.CF_debug("!-- AndroidBridge.setLinkUrl($url)")
            LogPrinter.CF_debug("!----------------------------------------------------------")
            menuLink = url
        }

        @JavascriptInterface
        fun webViewClose(msg: String?) {
            LogPrinter.CF_debug("!----------------------------------------------------------")
            LogPrinter.CF_debug("!-- AndroidBridge.webViewClose()")
            LogPrinter.CF_debug("!----------------------------------------------------------")
            finish()

        }

        @JavascriptInterface
        fun reqUniqVal() {
            LogPrinter.CF_debug("!----------------------------------------------------------")
            LogPrinter.CF_debug("!-- AndroidBridge.reqUniqVal()")
            LogPrinter.CF_debug("!----------------------------------------------------------")

            mHandler.post {
                try {
                    val uniqVal = CommonFunction.CF_getUUID(applicationContext)
                    mWebView2?.loadUrl("javascript:rtnUniqval('$uniqVal');")
                } catch (e: NullPointerException) {
                    e.message
                } catch (e: java.lang.Exception) {
                    e.message
                }
            }
        }

        // 인증 종료 (금융인증서,PASS인증서)
        @JavascriptInterface
        fun finCertFinish(
            errCode: String,
            errMsg: String,
            csno: String,
            name: String,
            tempKey: String
        ) {
            LogPrinter.CF_debug("!----------------------------------------------------------")
            LogPrinter.CF_debug("!-- AndroidBridge.finCertFinish()")
            LogPrinter.CF_debug("!----------------------------------------------------------")

            LogPrinter.CF_debug("!---- errCode : $errCode")
            LogPrinter.CF_debug("!---- errMsg  : $errMsg")
            LogPrinter.CF_debug("!---- csno    : $csno")
            LogPrinter.CF_debug("!---- name    : $name")
            LogPrinter.CF_debug("!---- tempKey : $tempKey")

            intent.putExtra("errCode", errCode)
            intent.putExtra("errMsg", errMsg)
            intent.putExtra("csno", csno)
            intent.putExtra("name", name)
            intent.putExtra("tempKey", tempKey)
            if ("E200" == errCode) {
                setResult(RESULT_OK, intent)
                finish()
            } else {
                CommonFunction.CF_showCustomAlertDilaog(mContext, errMsg) {
                    setResult(RESULT_CANCELED, intent)
                    finish()
                }
            }
        }
    }
    private inner class PopupWebViewClient : WebViewClient() {


        override fun shouldOverrideUrlLoading(view: WebView, url: String): Boolean {
            LogPrinter.CF_debug("!----------------------------------------------------------")
            LogPrinter.CF_debug("!-- (mWebview)WebViewClient.shouldOverrideUrlLoading(view, url)")
            LogPrinter.CF_debug("!----------------------------------------------------------")
            LogPrinter.CF_debug("!---- checkUrl() : $url")
            // rtnVal:true 이면 다른 엑티비티로 이동하기 때문에 프로그레스바 종료
            val rtnVal =checkUrl(url)
            LogPrinter.CF_debug("!---- checkUrl() : $rtnVal")
            if (rtnVal) {
                rel_progress?.visibility =View.GONE
            }
            return rtnVal
        }
        @TargetApi(Build.VERSION_CODES.N)
        override fun shouldOverrideUrlLoading(view: WebView, request: WebResourceRequest): Boolean {
            LogPrinter.CF_debug("!----------------------------------------------------------")
            LogPrinter.CF_debug("!-- (mWebview)WebViewClient.shouldOverrideUrlLoading(view, request) --VERSION_CODES.N")
            LogPrinter.CF_debug("!----------------------------------------------------------")

            // -- rtnVal:true 이면 다른 엑티비티로 이동하기 때문에 프로그레스바 종료
            val rtnVal = checkUrl(request.url.toString())
            LogPrinter.CF_debug("!---- checkUrl() : ${request.url.toString()}")
            LogPrinter.CF_debug("!---- checkUrl() : $rtnVal")
            if (rtnVal) {
                rel_progress?.visibility =View.GONE
            }
            return rtnVal
        }
    }
    /**
     * MyWebViewClient
     */
    private inner class MyWebViewClient : WebViewClient() {
        override fun shouldOverrideUrlLoading(view: WebView, url: String): Boolean {
            LogPrinter.CF_debug("!----------------------------------------------------------")
            LogPrinter.CF_debug("!-- (mWebview)WebViewClient.shouldOverrideUrlLoading(view, url)")
            LogPrinter.CF_debug("!----------------------------------------------------------")
            LogPrinter.CF_debug("!---- checkUrl() : $url")
            // rtnVal:true 이면 다른 엑티비티로 이동하기 때문에 프로그레스바 종료
            val rtnVal = checkUrl(url)
            LogPrinter.CF_debug("!---- checkUrl() : $rtnVal")
            if (rtnVal) {
                rel_progress?.visibility =View.GONE
            }
            return rtnVal
        }

        @TargetApi(Build.VERSION_CODES.N)
        override fun shouldOverrideUrlLoading(view: WebView, request: WebResourceRequest): Boolean {
            LogPrinter.CF_debug("!----------------------------------------------------------")
            LogPrinter.CF_debug("!-- (mWebview)WebViewClient.shouldOverrideUrlLoading(view, request) --VERSION_CODES.N")
            LogPrinter.CF_debug("!----------------------------------------------------------")

            // -- rtnVal:true 이면 다른 엑티비티로 이동하기 때문에 프로그레스바 종료
            val rtnVal = checkUrl(request.url.toString())
            LogPrinter.CF_debug("!---- checkUrl() : ${request.url.toString()}")
            LogPrinter.CF_debug("!---- checkUrl() : $rtnVal")
            if (rtnVal) {
                rel_progress?.visibility =View.GONE
            }
            return rtnVal
        }

        override fun onReceivedLoginRequest(
            view: WebView,
            realm: String,
            account: String?,
            args: String
        ) {
            super.onReceivedLoginRequest(view, realm, account, args)
        }


        override fun onPageStarted(view: WebView, url: String, favicon: Bitmap?) {
            super.onPageStarted(view, url, favicon)
            LogPrinter.CF_debug("!----------------------------------------------------------")
            LogPrinter.CF_debug("!-- (mWebview)WebViewClient.onPageStarted()")
            LogPrinter.CF_debug("!----------------------------------------------------------")
            // -- 타이틀바 좌측버튼 세팅(뒤로가기)
            if (btnLeft != null) {
                if (mWebview!!.canGoBack()) {
                    btnLeft!!.visibility = View.VISIBLE
                } else {
                    btnLeft!!.visibility = View.GONE
                }
            }

        }

        override fun onLoadResource(view: WebView, url: String) {
            super.onLoadResource(view, url)
            LogPrinter.CF_debug("!----------------------------------------------------------")
            LogPrinter.CF_debug("!-- (mWebview)WebViewClient.onLoadResource()")
            LogPrinter.CF_debug("!----------------------------------------------------------")
            LogPrinter.CF_debug("!---- url::::$url")
        }

        override fun onPageCommitVisible(view: WebView, url: String) {
            super.onPageCommitVisible(view, url)
            LogPrinter.CF_debug("!----------------------------------------------------------")
            LogPrinter.CF_debug("!-- (mWebview)WebViewClient.onPageCommitVisible()")
            LogPrinter.CF_debug("!----------------------------------------------------------")
        }

        override fun onReceivedError(
            view: WebView,
            request: WebResourceRequest,
            error: WebResourceError
        ) {
            super.onReceivedError(view, request, error)
            LogPrinter.CF_debug("!----------------------------------------------------------")
            LogPrinter.CF_debug("!-- WebViewClient.onReceivedError()")
            LogPrinter.CF_debug("!----------------------------------------------------------")

            // -- 앱 프로그레스바 종료
            rel_progress?.visibility =View.GONE
            view.clearCache(true)
        }

        override fun onPageFinished(view: WebView, url: String) {
            super.onPageFinished(view, url)
            LogPrinter.CF_debug("!----------------------------------------------------------")
            LogPrinter.CF_debug("!-- WebViewClient.onPageFinished()")
            LogPrinter.CF_debug("!----------------------------------------------------------")
            LogPrinter.CF_debug("!---- 하드웨어가속 사용 여부::::" + view.isHardwareAccelerated)


            //로그인 url일경우 back
             if (url.contains("CO/IUCO05M00.do") ) {
                 mWebview?.goBack()
             }


        }


    }

    /**
     * DownloadListener
     */
    private inner class MyDownloadListener : DownloadListener {
        private var mChildWebView: WebView?

        constructor(childWebView: WebView?) {
            mChildWebView = childWebView
        }

        constructor() {
            mChildWebView = null
        }

        override fun onDownloadStart(
            url: String,
            userAgent: String,
            contentDisposition: String,
            pMimeType: String,
            contentLength: Long
        ) {
            LogPrinter.CF_debug("!----------------------------------------------------------")
            LogPrinter.CF_debug("!-- (childWebView)DownloadListener.onDownloadStart()")
            LogPrinter.CF_debug("!----------------------------------------------------------")

            var mimeType: String = pMimeType
            // ------------------------------------
            // -- mimeType 지정(bin으로 받아지는 문제 해결)
            // ------------------------------------
            // -- attachment; filename="" 일때 사용
            if (contentDisposition.endsWith(".pdf\"")) {
                mimeType = "application/pdf"
            } else if (contentDisposition.endsWith(".xls\"") || contentDisposition.endsWith(".xlsx\"")) {
                mimeType = "application/vnd.ms-excel"
            } else if (contentDisposition.endsWith(".ppt\"") || contentDisposition.endsWith(".pptx\"")) {
                mimeType = "application/vnd.ms-powerpoint"
            } else if (contentDisposition.endsWith(".doc\"") || contentDisposition.endsWith(".docx\"")) {
                mimeType = "application/msword"
            }
            val fileName = URLUtil.guessFileName(url, contentDisposition, mimeType)

            // -- inline; filename=""  일때 사용
            // String fileName = contentDisposition.replace("inline; filename=", "")
            // fileName = fileName.replaceAll("\"", "")
            // request.setTitle(fileName)
            if (mChildWebView != null) {
                // -- TODO: 다운로드시 차일드(새창)으로 띄워져 화면이 잠시 깜빡거림. 추후 변경방법이 있으면 변경 필요함
                mWebview!!.removeView(mChildWebView)
                childWebView = null
            }
            WebFileDownloadHelper.webFileDownloadManager(
                mContext,
                url,
                userAgent,
                fileName,
                mimeType,
                false,
                contentLength
            )
        }
    }
}