package com.epost.insu.activity

import android.Manifest
import android.annotation.SuppressLint
import android.app.NotificationManager
import android.content.ComponentName
import android.content.Intent
import android.content.IntentFilter
import android.content.ServiceConnection
import android.graphics.Color
import android.graphics.drawable.AnimationDrawable
import android.net.Uri
import android.os.*
import android.provider.Settings
import android.text.Spannable
import android.text.SpannableString
import android.text.TextUtils
import android.text.style.ForegroundColorSpan
import android.view.View
import android.view.accessibility.AccessibilityEvent
import android.widget.*
import androidx.core.app.ActivityCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.viewpager.widget.ViewPager.OnPageChangeListener
import com.TouchEn.mVaccine.b2b2c.activity.BackgroundScanActivity
import com.epost.insu.*
import com.epost.insu.EnvConfig.AuthMode
import com.epost.insu.activity.BC.IUBC01M00
import com.epost.insu.activity.BC.IUBC10M00_P
import com.epost.insu.activity.auth.*
import com.epost.insu.activity.push.PushMessageActivity
import com.epost.insu.adapter.BannerAdapter
import com.epost.insu.common.*
import com.epost.insu.control.CustomIndicator
import com.epost.insu.dialog.BottomSheetDialog
import com.epost.insu.dialog.CustomDialog
import com.epost.insu.dialog.IUCOA2M00
import com.epost.insu.e2e.AppDefenceManager
import com.epost.insu.event.OnTapEventListener
import com.epost.insu.fido.Fido2Callback
import com.epost.insu.fido.Fido2Constant
import com.epost.insu.fido.Fido2RegistableAuthTech
import com.epost.insu.network.HttpConnections
import com.epost.insu.network.WeakReferenceHandler
import com.epost.insu.network.WeakReferenceHandler.ObjectHandlerMessage
import com.epost.insu.psmobile.CommonUtils
import com.epost.insu.service.Service_ImageDown
import com.epost.insu.service.Service_ImageDown.ICallback
import com.epost.insu.service.Service_ImageDown.LocalBinder
import com.google.firebase.FirebaseApp
import com.secureland.smartmedic.SmartMedic
import com.secureland.smartmedic.core.Constants
import m.client.push.library.common.PushConstants
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import kotlin.system.exitProcess
import android.os.Bundle
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DecodeFormat


/**
 * 인트로/메인
 * @since     :
 * @version   : 3.1
 * @author    : NJM
 * @see
 * <pre>
 *  - 인트로는 App 최초 실행 한번만 보이며 인트로 단계 처리 과정은 다음과 같다.
 *  : 백신실행 - 앱 위변조 확인 - 배너 파일 확인 및 다운로드
 *  - App 버전 확인 ==> 앱 위변조 이용(DB관리 or 파싱 or FirebaseRemoteConfig 정보 사용 안함)
 *  [showNeedAppUpdateDlg] 함수 내 주석 참조
 * ======================================================================
 * 0.0.0    LSH_20170908    최초 등록
 * 0.0.0    LSH_20170908    배너정보 요청 시 최근 공지 내용 추가 반환
 * 0.0.0    YJH_20180203    웹브라우저 호출 방식 변경 (기본5개브라우저 호출후 기본 설정 브라우저 호출)
 * 0.0.0    LKM_20180814    스마트보험금청구 구현 추가
 * 0.0.0    NJM_20190207    앱 초기 실행시 네트워크 상태 체크 추가
 * 0.0.0    NJM_20190218    개발/운영 구분 추가(초기화 화면에 표기)
 * 0.0.0    NJM_20190827    메인화면 아이콘 개수 4 -> 5개
 * 0.0.0    NJM_20191121    라온시큐어 엠백신 모듈 안드로이드Q대응 업데이트
 * 0.0.0    NJM_20191202    메인링크 DB조회값으로 변경 및 예상보험금 아이콘으로 교체
 * 0.0.0    NJM_20191210    앱접근성 - 롤링배너 중지처리
 * 0.0.0    NJM_20191215    arrLoginMenuPage에 연금예상액조회 페이지번호 추가(21)
 * 0.0.0    NJM_20191216    웹뷰로 띄울 링크 추가(FAQ, 등)
 *                          : 앱접근성 - 롤링배너 중지처리 (시각접근성일때만 중지처리)
 *                          : 공통소스 리팩토링
 * 0.0.0    NJM_20190117    제제로고 삭제 및 타이틀 로고 사이즈 업
 * 0.0.0    NJM_20200205    뒤로가기로 종료시 에러 발생 해결 : 배너 timer종료되지 않아 발생함(timer.cancel() 추가)
 * 0.0.0    NJM_20200212    배너 Timer 일부 로직 변경 및 액티비티 종료시 timer null 조건 추가
 * 0.0.0    NJM_20200213    배너다운로드 완료 후 배너 롤링 스레드 시작하도록 변경
 * 0.0.0    NJM_20200217    Triple버튼 Dialog 추가 -- 보험료납입, 대출/상환
 * 0.0.0    NJM_20200225    checkRooting() 함수 Activity_Default.class 로 이동
 * 0.0.0    NJM_20200323    웹뷰전환, sql조회 공통 class로 전환
 * 0.0.0    NJM_20200330    (메인화면-대출) 다이얼로그 삭제하고 대출/상환 둘다 안내페이지로 이동
 * 0.0.0    YJH_20201210    고객 부담보내역 조회 추가
 * 0.0.0    YJH_20201210    카카오페이인증 추가
 * 0.0.0    NJM_20210209    [하단팝업 공지] 하단 팝업공지 호출 추가
 * 0.0.0    NJM_20200216    arrLoginMenuPage 리스트 EnvConfig로 이동
 * 1.5.2    NJM_20210322    [FIDO인증 로직 변경] Fido2RegistableAuthTech() 메인엑티비티에서 호출
 * 1.5.2    NJM_20210322    [subUrl 공통파일로 변경]
 * 1.5.3    NJM_20210330    [FIDO호출 로직 변경] 에러발생으로인한 변경
 * 1.5.4    NJM_20210504    [간편인증 전자서명 추가] 웹요청 간편인증 추가, 청구시 간편인증 전자서명 추가
 * 1.5.4    NJM_20210507    [보험앱 메뉴명 정리]
 * 1.5.5    NJM_20210513    [내소식 추가]
 * 1.5.6    NJM_20210531    [메인공지URL 수정] 공지사항 url 오류 수정
 * 1.5.7    NJM_20210604    [배너오토플레이시간 변경] 3초 -> 10초
 * 1.6.1    NJM_20210705    [배너 높이문제 수정] 폴드폰에서 화면사이즈 변경시 배너 사이즈가 이전사이즈로 고정되는 문제 수정
 * 1.6.1    NJM_20210708    [청구가능시간 변경] 4~5시 청구 불가 처리
 * 1.6.1    NJM_20210722    인증센터 호출 로직 수정 (간편인증/공동인증 별도 호출 통합)
 * 1.6.1    NJM_20210726    [web요청스킴 추가] 자녀보험금청구, 지급내역조회 스킴 추가
 * 1.6.2    NJM_20210729    [간편인증 플래그 반영] 간편인증 로그인시 flag값 참조 변경 (서버저장 -> 단말저장)
 * 1.6.2    NJM_20210729    [자동로그인 신규] 1차본 (기능 반영만)
 * 1.6.2    NJM_20210802    [2021년 대우 취약점] 2차본 : 부적절한 예외 처리
 * 1.6.3    NJM_20210604    [배너 시간 변경] 첫페이지 딜레이시간 0.5 -> 10초 (메인 배너 사용안함)
 * 1.6.3    NJM_20211007    [API30 대응] targetApi 29 -> 30 변경에 따른 코틀린 오류 수정
 * 1.6.3    NJM_20211008    [API30 대응] 솔루션 업데이트 반영 (인증서 공용 -> 내부 복사 로직 추가)
 * =======================================================================
 * copyright : (주)프로니스정보기술 | http://www.phronis.co.kr
 * </pre>
 */
class IUCOA0M00 : Activity_WebView(), ObjectHandlerMessage {

    private lateinit var img_intro:ImageView
    private var subUrl_bbs = "/CC/IUCC00M01.do?bbsId=BBSMSTR_000000000001" // bbs 페이지 요청 url
    private var load_ani:AnimationDrawable? =null
    private var img_load:ImageView? =null //인트로 로딩아이콘
    private var isAccessibilityTurnOn = false // 시각접근성(스크린리더 On유무)
    private var tmp_popupCnt = 0 // 팝업 갯수
    private var rollingHandler: Handler? = null
    private var Update: Runnable? = null
    private var timer: Timer? = null
    private var mIntroLayout: RelativeLayout? = null // 인트로 레이아웃

    private var checkState = 0 // 0:최초 상태(접근권한 미확인)
    private var flagShownDlgPermission = false // 권한동의 안내 팝업 Shown 유무
    private var flagStartedVaccine = false // 백신 실행 유무
    private var flagStartedAppDefence = false // 앱 위변조 실행 유무
    private var flagRequestOnePassInit = false // OnePass 초기 정보 요청 유무
    private var flagRunOkGetBanner = false // 배너 정보 요청 결과 수신 유무
    private var flagFinishedDownloadBanner = false // 배너 파일 다운로드 종료 유무
    private var flagRunOkVaccine = false // 백신 실행 완료 유무
    private var flagRunOkAppDefence = false // 앱 위변조 검사 완료 유무
    private var flagShownContents = false // 컨텐츠 show 상태 값
    private var flagBindServiceDownload = false // 배너 이미지 다운로드 서비스 바인드 연결 상태 값
    // private var fraNoticeLayout: FrameLayout? = null // 공지사항 레이아웃

    private var appDefenceManager: AppDefenceManager? = null // 앱위변조 솔루션 매니져
    private var serviceDownload: Service_ImageDown? = null // 이미지 다운로드 서비스
    private var dlgGuidePermission: IUCOA2M00? =
        null // App 권한 안내 다이얼로그 (권한 안내가 필요한 경우에 한하여 App 실행시 한번만 보인다.)
    private var bottomSheetDialog: BottomSheetDialog? = null // 하단팝업공지
    private var dlgRetryPermission: CustomDialog? = null // 권한 재요청 다이얼로그
    private var dlgSettingPermission: CustomDialog? = null // [설정]-[권한] 이동 안내 다이얼로그
    private var backHandler: BackDoubleClickCloseHandler? = null
    private var jsonStartedWeb: JSONObject? = null // Web 요청 jsonObject
    private val isWebViewCall = false
    private var arrBannerFile: ArrayList<File>? = null // 배너 파일
    private var arrbannerPath: ArrayList<String>? = null // 배너 이미지 경로

    // -- push
    private var pushTitle = ""
    private var pushMessage = ""
    private var mPageNum = "" // 로그인 후 이동할 웹페이지
    private val mPageTitle = "" // 로그인 후 이동할 웹페이지 제목


    companion object {
        const val HANDLERJOB_APP_DEFENCE_SUCCESS = 0 // 앱 위변조 확인 성공 what
        const val HANDLERJOB_APP_DEFENCE_FAIL = 1 // 앱 위변조 확인 실패 what
        const val HANDLERJOB_APP_DEFENCE_ERROR = 2 // 앱 위변조 확인 에러 what
        private const val HANDLERJOB_INIT = 3 // 배너 파일 및 초기 값 요청 성공 what
        private const val HANDLERJOB_ERROR_INIT = 4 // 배너 파일 및 초기 값 요청 실패 what
        private const val HANDLERJOB_SERVER_ON_CHECK_OK = 8 // WAS서버on 체크 성공
        private const val HANDLERJOB_SERVER_ON_CHECK_FAIL = 9 // WAS서버on 체크 실패
        private const val REQUEST_DEFAULT_PERMISSIONS = 96 // 필수 퍼미션 확인 REQUEST CODE
        private const val mIntroDuration = 500L // 인트로 화면 애니메이션 시간
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        LogPrinter.CF_debug("!----------------------------------------------------------")
        LogPrinter.CF_debug("!-- IUCOA0M00.onActivityResult()")
        LogPrinter.CF_debug("!----------------------------------------------------------")
        LogPrinter.CF_debug("!---- requestCode : [$requestCode] / resultCode : [$resultCode]")

        if (requestCode == EnvConfig.REQUESTCODE_VACCINE) {
            // -------------------------------------------------------------------------------------
            //  (Touch mVaccine) 루팅일때 : 종료
            // -------------------------------------------------------------------------------------
            if (resultCode == Constants.ROOTING_EXIT_APP || resultCode == Constants.ROOTING_YES_OR_NO) {
                finish()
            } else if (resultCode == Constants.EXIST_VIRUS_CASE1 || resultCode == Constants.EXIST_VIRUS_CASE2) {
                Handler().postDelayed({ // 인트로화면 animation을 위해 delay 적용
                    flagRunOkVaccine = true
                    updateInitProcessStateText()
                }, 300)
            } else if (resultCode == Constants.EMPTY_VIRUS) {
                Handler().postDelayed({ // 인트로화면 animation을 위해 delay 적용
                    flagRunOkVaccine = true
                    updateInitProcessStateText()
                }, 300)
            }
        } else if (requestCode == EnvConfig.REQUESTCODE_ACTIVITY_IUFC00M00 && resultCode == RESULT_OK) {
            // --<> (웹브라우저 호출) 앱 백그라운드 처리
            if (!isWebViewCall) {
                IntentManager.moveTaskToBack(this@IUCOA0M00, true)
            }
            finish()
        } else if (requestCode == EnvConfig.REQUESTCODE_ACTIVITY_IUCOB0M00 && resultCode == RESULT_OK) {
            // 로그인 처리 후 웹뷰 호출
            if ("" != mPageNum) {
                showWebPage(
                    true,
                    mPageNum,
                    if ("" != mPageTitle) mPageTitle else resources.getString(R.string.app_name)
                )
            }
        }
    }


    override fun onBackPressed() {


        LogPrinter.CF_debug("onBackPressed $showAllMenu")


        if(showAllMenu){
            mWebview?.loadUrl("javascript:hideMenu()")
            showAllMenu = false
        }
        else {

            val url = mWebview?.url ?: ""



            var canback = mWebview?.canGoBack() ?: false

            //웹뷰 팝업 백키 입력시 팝업창 종료..
            if(childWebView!=null)
            {
                canback = true
            }
            LogPrinter.CF_debug("onBackPressed $url $canback ${!url.contains("#")}")




            if (canback && !url.endsWith("/") ) {
                super.onBackPressed()
            } else {

                if (flagShownContents) {
                    // -- 접근성 ON : 종료 다이얼로그 팝업
                    if (CommonFunction.CF_checkAccessibilityTurnOn(applicationContext)) {


                        val customDialog = CustomDialog(this)
                        customDialog.show()
                        customDialog.CF_setBackKeyMode(CustomDialog.BackMode.CANCELED)
                        customDialog.CF_setTextContent(resources.getString(R.string.dlg_accessible_finish_app))
                        customDialog.CF_setDoubleButtonText(
                            resources.getString(R.string.btn_no),
                            resources.getString(R.string.btn_yes)
                        )
                        customDialog.setOnDismissListener { dialog ->
                            if (!(dialog as CustomDialog).CF_getCanceled()) {
                                CustomApplication.CF_logOut(applicationContext)
                                finish()
                            }
                        }

                    } else {
                        backHandler!!.CF_onBackPressed(this@IUCOA0M00,true)
                    }
                }
            }

        }

    }



    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        LogPrinter.CF_debug("!----------------------------------------------------------")
        LogPrinter.CF_debug("!-- IUCOA0M00.onNewIntent()")
        LogPrinter.CF_debug("!----------------------------------------------------------")

        // -- Intent 데이터 세팅
        setIntentData(intent)





        // 푸시팝업
        showPushPopup()
    }

    /**
     * Intent 데이터 세팅 함수<br></br>
     * Web 요청 정보가 jsonObject 문자열로 전달되며, intro 화면 hide 애니메이션 종료 후 전달받은 JSON 문자열을 풀어 실행시킬 작업을 판단한다.
     */
    private fun setIntentData(p_intent: Intent?) {
        LogPrinter.CF_debug("!-----------------------------------------------------------")
        LogPrinter.CF_debug("!-- IUCOA0M00.setIntentData()")
        LogPrinter.CF_debug("!-----------------------------------------------------------")
        if (p_intent != null) {
            if (p_intent.hasExtra("startJson")) {
                try {
                    jsonStartedWeb = JSONObject(p_intent.extras!!.getString("startJson")!!)
                } catch (e: JSONException) {
                    LogPrinter.CF_line()
                    LogPrinter.CF_debug(resources.getString(R.string.log_json_exception))
                }
            }

            // -- 알림창에서 푸시메시지 클릭시
            if (p_intent.hasExtra(PushConstants.KEY_MESSAGE)) {
                //String jsonMessage = p_intent.getStringExtra(PushConstants.KEY_JSON);
                pushTitle = p_intent.getStringExtra(PushConstants.KEY_TITLE)!!
                pushMessage = p_intent.getStringExtra(PushConstants.KEY_MESSAGE)!!
                pushMessage = pushMessage.replace("/n", "<br />")
                //		String ext         = p_intent.getStringExtra(PushConstants.KEY_EXT);
                //		String psid        = p_intent.getStringExtra(PushConstants.KEY_PSID);
                //		String pushType    = p_intent.getStringExtra(PushConstants.KEY_PUSHTYPE);
                LogPrinter.CF_debug("!---- pushTitle : $pushTitle / pushMessage :$pushMessage")
            }
        }
    }

    @SuppressLint("NewApi")
    override fun onCreate(savedInstanceState: Bundle?) {
        LogPrinter.CF_debug("!----------------------------------------------------------")
        LogPrinter.CF_debug("!-- IUCOA0M00.onCreate()")
        LogPrinter.CF_debug("!----------------------------------------------------------")
        //메인일경우 iucoa0m00.xml 아닐경우 c_webview`.xml을 사용
        setContentView(R.layout.iucoa0m00)

         img_intro = findViewById<ImageView>(R.id.img_intro)
        Glide.with(this@IUCOA0M00).load(R.drawable.img_intro).format(DecodeFormat.PREFER_RGB_565).into(img_intro)

        super.onCreate(savedInstanceState)




        // -- Firebase SDK 초기화
        FirebaseApp.initializeApp(applicationContext)

        // -- 금융결제원 공통FIDO(생체)인증 시 사용하는 키(싸인후 출력)
        /*
        PackageInfo packInfo = null;
        try{
            packInfo = this.getPackageManager().getPackageInfo("com.epost.insu", PackageManager.GET_SIGNATURES);
        } catch(PackageManager.NameNotFoundException e){
            LogPrinter.CF_line();
            LogPrinter.CF_debug("NameNotFoundException", e.toString());
        }
        PackageInfo pi = packInfo;

        //String facetid = getFacetID(this.getApplicationContext().getPackageManager().getPackageInfo(this.getApplicationContext().getPackageName(), this.getApplicationContext().getPackageManager().GET_SIGNATURES));

        if(pi == null ){

            LogPrinter.CF_info("에라났다");
        } else {
            LogPrinter.CF_line();
            LogPrinter.CF_info(getFacetID(pi));
            LogPrinter.CF_line();
        }
        */

        // -- Intent 데이터 세팅
        setIntentData(intent)
        // -----------------------------------------------------------------------------------------
        //  Activity 중복 실행 검사 : Play Store 에서 실행 후 App 목록에서 런쳐 아이콘 클릭 시 메인 Activity 중복 실행 현상 있음.
        //      => Main Activity 종료 시 프로세스 강제 종료 또는 finishAffinity() 사용 등의 방법으로도 처리가 가능하나
        //      => Main Activity onCreate 시점에 isTaskRoot 확인 후 finish 시키는 방법으로 처리함.
        //
        //  1. jsonStartedWeb 값을 확인 해 Activity_WebStart 에서 launch 한 것인지 검사
        //      => Activity_WebStart에서 실행된 경우 백신 초기 세팅
        //  2. 1번 외 방법으로 launch 된 경우 현재 Activity가 task root Activity인지 검사
        //      => if task root : 백신 초기 세팅
        //      => if not task root : Activity 종료
        // -----------------------------------------------------------------------------------------
        LogPrinter.CF_debug("!---- jsonStartedWeb : $jsonStartedWeb")
        LogPrinter.CF_debug("!---- isTaskRoot     : $isTaskRoot")
        LogPrinter.CF_debug("!-----------------------------------------------------------")
        LogPrinter.CF_debug("!-- IUCOA0M00.setInit()")
        LogPrinter.CF_debug("!-----------------------------------------------------------")


        flagShownDlgPermission = false
        flagStartedVaccine = false
        flagStartedAppDefence = false
        flagRequestOnePassInit = false
        flagRunOkGetBanner = false
        flagFinishedDownloadBanner = false
        flagRunOkVaccine = false
        flagRunOkAppDefence = false
        flagShownContents = false
        flagBindServiceDownload = false
        arrBannerFile = ArrayList()
        arrbannerPath = ArrayList()
        handler = WeakReferenceHandler(this)
        backHandler = BackDoubleClickCloseHandler(this)

        // -- FIDO 정보조회 : TODO : Application에서 조회하는데 없을 경우 추가 조회... 확인 필요
        val bioInfo = (applicationContext as CustomApplication).bundleBioInfo
        if (bioInfo == null) {
            LogPrinter.CF_debug("!---- CustomApplication.getBundleBioInfo() == null")
            Fido2RegistableAuthTech(applicationContext, object : Fido2Callback {
                override fun onReceiveMessage(code: String, bundle: Bundle) {
                    LogPrinter.CF_debug("!----[getPhoneInfo] [성공] (code) : $code / (bundle) : $bundle")
                    (applicationContext as CustomApplication).bundleBioInfo = bundle
                }
                override fun onFailure(code: String, msg: String) {
                    LogPrinter.CF_debug("!----[getPhoneInfo] [에러] (code) : $code / (msg) : $msg")
                }
            }).process()
        }






        setIntroLayout()
        if (jsonStartedWeb != null || isTaskRoot || "" != pushMessage) {
            setInitVaccine() // mVaccine 초기 세팅
        }
        /*
        else {
            LogPrinter.CF_debug("!-- finish")
            finish()
        }*/



    }

    override fun onResume() {
        super.onResume()
        LogPrinter.CF_debug("!----------------------------------------------------------")
        LogPrinter.CF_debug("!-- IUCOA0M00.onResume()")
        LogPrinter.CF_debug("!----------------------------------------------------------")
        isAccessibilityTurnOn = CommonFunction.CF_checkAccessibilityTurnOn(this) // 접근성 On 검사

        // -----------------------------------------------------------------------------------------
        // -- 네트워크 상태 체크
        // -----------------------------------------------------------------------------------------
        //TODO : 네트워크 연결 테스트 필(메소드 변경됨)
        // -- 프로세스 실행전에 네트워크상태 체크       // 2019-02-07 추가
        //if(super.checkNetwork() ) {
        // -- WAS서버 연결 정상 유무 체크
        val builder = Uri.Builder()
        HttpConnections.sendPostDataCheck(
            EnvConfig.host_url,
            builder.build().encodedQuery,
            handler,
            HANDLERJOB_SERVER_ON_CHECK_OK,
            HANDLERJOB_SERVER_ON_CHECK_FAIL
        )

        // -----------------------------------------------------------------------------------------
        // -- 초기 프로세스 실행
        // -----------------------------------------------------------------------------------------
        initProcess()
        //}

    }

    override fun onPause() {
        super.onPause()
        LogPrinter.CF_debug("!----------------------------------------------------------")
        LogPrinter.CF_debug("!-- IUCOA0M00.onPause()")
        LogPrinter.CF_debug("!----------------------------------------------------------")
    }


    override fun onStop() {
        super.onStop()
        LogPrinter.CF_debug("!----------------------------------------------------------")
        LogPrinter.CF_debug("!-- IUCOA0M00.onStop()")
        LogPrinter.CF_debug("!----------------------------------------------------------")
        if (timer != null) {
            timer!!.cancel()
            timer = null
        }
    }

    override fun onTrimMemory(level: Int) {

        if (level == TRIM_MEMORY_RUNNING_CRITICAL || level == TRIM_MEMORY_RUNNING_LOW
            || level == TRIM_MEMORY_RUNNING_MODERATE || level == TRIM_MEMORY_COMPLETE
        ) {
            finishAffinity()
            exitProcess(0)
        } else {
            super.onTrimMemory(level)
        }
    }


    override fun onDestroy() {
        LogPrinter.CF_debug("!-----------------------------------------------------------")
        LogPrinter.CF_debug("!-- IUCOA0M00.onDestroy()")
        LogPrinter.CF_debug("!-----------------------------------------------------------")
        // -- 배너 스크롤링 중지
        if (timer != null) {
            timer!!.cancel()
            timer = null
        }
        // 서비스 바인드 해지
        if (flagBindServiceDownload) {
            flagBindServiceDownload = false
            unbindService(connection)
            serviceDownload = null
        }
        // mVaccine notification 삭제 :: not use
        if (flagStartedVaccine) {
            val tmp_noti = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            tmp_noti.cancel(EnvConfig.mVaccineMsgId_1)
            tmp_noti.cancel(EnvConfig.mVaccineMsgId_2)
        }
        backHandler = null
        super.onDestroy()
    }



    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putBoolean("flagShownDlgPermission", flagShownDlgPermission)
        outState.putBoolean("flagShownContents", flagShownContents)
        outState.putBoolean("flagStartedVaccine", flagStartedVaccine)
        outState.putBoolean("flagRequestOnePassInit", flagRequestOnePassInit)
        outState.putBoolean("flagStartedAppDefence", flagStartedAppDefence)
        outState.putBoolean("flagFinishedDownloadBanner", flagFinishedDownloadBanner)
        outState.putBoolean("flagRunOkVaccine", flagRunOkVaccine)
        outState.putBoolean("flagRunOkAppDefence", flagRunOkAppDefence)

        outState.putBoolean("flagRunOkGetBanner", flagRunOkGetBanner)
        outState.putBoolean("flagBindServiceDownload", flagBindServiceDownload)

        outState.putInt("checkState", checkState)

    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        flagShownDlgPermission = savedInstanceState.getBoolean("flagShownDlgPermission") ?: false
        flagShownContents = savedInstanceState.getBoolean("flagShownContents")  ?: false
        flagStartedVaccine = savedInstanceState.getBoolean("flagStartedVaccine")  ?: false
        flagRequestOnePassInit = savedInstanceState.getBoolean("flagRequestOnePassInit")  ?: false
        flagStartedAppDefence = savedInstanceState.getBoolean("flagStartedAppDefence")  ?: false
        flagRunOkGetBanner = savedInstanceState.getBoolean("flagRunOkGetBanner")  ?: false
        flagFinishedDownloadBanner = savedInstanceState.getBoolean("flagFinishedDownloadBanner")  ?: false
        flagRunOkVaccine = savedInstanceState.getBoolean("flagRunOkVaccine")  ?: false
        flagRunOkAppDefence = savedInstanceState.getBoolean("flagRunOkAppDefence")  ?: false
        flagBindServiceDownload = savedInstanceState.getBoolean("flagBindServiceDownload")  ?: false
        checkState = savedInstanceState.getInt("checkState") ?: 0

        // 컨텐츠 레이아웃 show
        if (flagShownContents) {
            mIntroLayout!!.visibility = View.GONE
        }


    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    /**
     * 퍼미션 재요청 다이얼로그 팝업 함수
     */
    private fun showDlgOfRetryPermissions() {
        // -----------------------------------------------------------------------------------------
        // 모든 [필수]권한에 대해 접근 동의를 하여야 이용이 가능하다는 안내 문구를 띄우고,
        // 사용자가 재요청 버튼을 클릭하면 접근동의 재요청을 한다.
        // 사용자가 종료 버튼을 클릭하면 App을 종료한다.
        // -----------------------------------------------------------------------------------------
        if (dlgRetryPermission == null) {
            dlgRetryPermission = CustomDialog(this)
        }
        if (!dlgRetryPermission!!.isShowing) {
            dlgRetryPermission!!.show()
            dlgRetryPermission!!.CF_setBackKeyMode(CustomDialog.BackMode.CANCELED)
            dlgRetryPermission!!.CF_setTextContent(resources.getString(R.string.dlg_p_denied))
            dlgRetryPermission!!.CF_setDoubleButtonText(
                resources.getString(R.string.btn_finish),
                resources.getString(R.string.btn_retry)
            )
            dlgRetryPermission!!.setOnDismissListener { dialog ->
                if (!(dialog as CustomDialog).CF_getCanceled()) {
                    requestDefaultPermissions()
                } else {
                    finish()
                }
                dlgRetryPermission = null
            }
        }
    }

    /**
     * 필수 퍼미션 동의유무 체크 함수<br></br>
     * 필수 퍼미션 중 단 하나라도 동의하지 않았으면 false를 반환환다.<br></br>
     * @return tmp_flagAllOk
     */
    private fun checkDefaultPermissions(): Boolean {
        var flagAllOk = true
        for (i in EnvConfig.getDefaultPermission().indices) {
            if (!CommonFunction.CF_checkAgreePermission(
                    this,
                    EnvConfig.getDefaultPermission()[i]
                )
            ) {
                flagAllOk = false
                break
            }
        }
        return flagAllOk
    }

    /**
     * 필수 퍼미션 동의 요청유무 검사함수<br></br>
     * 필수 퍼미션 중 단 하나라도 요청한 적이 있으면 true를 반환한다.<br></br>
     * @return tmp_flagRational
     */
    private fun checkDefaultPermissionRational(): Boolean {
        var flagRational = false
        for (i in EnvConfig.getDefaultPermission().indices) {

            if (ActivityCompat.shouldShowRequestPermissionRationale(
                    this,
                    EnvConfig.getDefaultPermission()[i]
                )
            ) {


                flagRational = true

                break
            }


        }

        return flagRational
    }

    /**
     * 초기 프로세스 실행<br></br>
     * 1. 접근권한 확인
     * 2. 백신 가동
     * 3. 앱 위변조 확인
     * 4. 배너 파일 다운로드
     */
    private fun initProcess() {
        LogPrinter.CF_debug("!----------------------------------------------------------")
        LogPrinter.CF_debug("!-- IUCOA0M00.initProcess()")
        LogPrinter.CF_debug("!----------------------------------------------------------")

        // -----------------------------------------------------------------------------------------
        //  checkState 0 : 초기 상태
        //  checkState 1 : initProcess 구동 중(배너 요청, 백신, 앱 위변조, FIDO 초기 요청)
        //  checkState 2 : InitProcess 구동 완료
        // -----------------------------------------------------------------------------------------
        if (checkDefaultPermissions()) {
            // -------------------------------------------------------------------------------------
            //  사용자가 필수 권한 모두 동의한 경우
            // -------------------------------------------------------------------------------------
            if (checkState == 0) {            // 최초 상태
                // 배너정보 요청,백신,위변조,FIDO 초기 실행
                checkState = 1
                runInitProcess()
            } else if (checkState == 1) {       // initProcess 구동 요청 상태
                // initProcess 구동 상태 TEXT 세팅
                updateInitProcessStateText()
            }
        } else if (!flagShownDlgPermission) {       // 권한 사용 안내 다이얼로그 Shown Flag
            // -------------------------------------------------------------------------------------
            // 모든 필수권한에 대해 접근 동의를 받지 못한 경우 그리고 권한 사용 안내가 필요한 경우
            // 권한 사용 안내 다이얼로그를 보인다.
            // -------------------------------------------------------------------------------------
            if (dlgGuidePermission == null) {
                dlgGuidePermission = IUCOA2M00(this@IUCOA0M00)
            }
            if (!dlgGuidePermission!!.isShowing) {
                dlgGuidePermission!!.show()
                dlgGuidePermission!!.setOnDismissListener {
                    flagShownDlgPermission = true
                    requestDefaultPermissions()
                    dlgGuidePermission = null
                }
            }
        } else {
            // -------------------------------------------------------------------------------------
            // 모든 필수 권한에 대해 접근 동의를 받지 못한 경우, 그리고 권한 사용 안내가 불필요한 경우
            // 권한 허용 재요청 다이얼로그를 보인다.
            // -------------------------------------------------------------------------------------
            if (dlgSettingPermission == null || !dlgSettingPermission!!.isShowing) {
                showDlgOfRetryPermissions()
            }
        }
    }

    /**
     * Intro 레이아웃 기본 세팅
     * @since 2019-02-18  tmp_textVersion.setText에 EnvConfig.app_dvsn 추가
     */
    @SuppressLint("SetTextI18n")
    private fun setIntroLayout() {
        LogPrinter.CF_debug("!----------------------------------------------------------")
        LogPrinter.CF_debug("!-- IUCOA0M00.setIntroLayout()")
        LogPrinter.CF_debug("!----------------------------------------------------------")
        mIntroLayout = findViewById(R.id.relIntro)

        // 현재 App 버전 세팅
        val txtVersion = findViewById<TextView>(R.id.textVersion)
        txtVersion.text = "ver " + CommonFunction.CF_getVersionName(this) + EnvConfig.app_dvsn



        val loadingImage = findViewById<ImageView>(R.id.img_load).apply {
            setBackgroundResource(R.drawable.img_progress)
            load_ani = background as AnimationDrawable
            load_ani?.isOneShot =false
            load_ani?.start()
        }

        // TODO : 아이콘 스크롤시 사용 (NJM_20210330 주석처리)
        // -- 화면 사이즈에 따른 아이콘 배치 // 2019-08-27 추가
//        LogPrinter.CF_debug("!---- setIntroLayout() 화면 사이즈에 따른 아이콘 배치")
//
//        // 아이콘 리소스 리스트
//        val lin_iconList = arrayOf(
//                R.id.linkIcon01,
//                R.id.linkIcon02,
//                R.id.linkIcon03,
//                R.id.linkIcon04)

//        DisplayMetrics dm = getApplicationContext().getResources().getDisplayMetrics();
//        float x = dm.widthPixels;
//        LogPrinter.CF_debug("!---- 화면 가로 사이즈:::: [" + x + "]");
//        // 화면사이즈가 작을때(노트9 1080기준) : 아이콘 4개 반 표기 (이상일 때는 모두 표기(기본값))
//        if(x <= 1200) {
//            LinearLayout lin_linkIconBase = findViewById(lin_iconList[0]);
//            float iconWidth = (float) (x / 4.5);
//
//            LinearLayout.LayoutParams iconLp;
//            iconLp = (LinearLayout.LayoutParams) lin_linkIconBase.getLayoutParams();
//
//            iconLp.width = (int) iconWidth;
//            iconLp.weight = 0;
//
////            for (int i = 0; i < lin_iconList.length; i++) {
////                //lin_iconList[i]
////                LinearLayout lin_linkIcon = findViewById(lin_iconList[i]);
////                lin_linkIcon.setLayoutParams(iconLp);
//
//            for (Integer icon : lin_iconList) {
//                //lin_iconList[i]
//                LinearLayout lin_linkIcon = findViewById(icon);
//                lin_linkIcon.setLayoutParams(iconLp);
//            }
//        }
    }


    /**
     * Intro 레이아웃 숨김
     */
    private fun hideIntroLayout() {
        mIntroLayout!!.animate().duration = mIntroDuration
        mIntroLayout!!.animate().alpha(0.3f)
        mIntroLayout!!.animate().x(-mIntroLayout!!.width.toFloat()).y(0f)
        Handler().postDelayed({
            mIntroLayout!!.visibility = View.GONE

            Glide.with(this@IUCOA0M00).clear(img_intro)

            try {
                startActivityFromWeb()

                // -- 푸시팝업
                showPushPopup()
            } catch (e: JSONException) {
                LogPrinter.CF_line()
                LogPrinter.CF_debug(resources.getString(R.string.log_json_exception))
            }
        }, mIntroDuration)

        load_ani?.stop()

        findViewById<ImageView>(R.id.img_load).background=null

    }

    /**
     * Intro 화면 Progress Text 세팅 함수
     * @param p_strText String
     */
    private fun setIntroProgressText(p_strText: String) {
        val txtProgress = findViewById<TextView>(R.id.textProgress)
        txtProgress.text = p_strText
    }

    /**
     * 인트로화면 프로그레스 레이아웃 Show Hide
     * @param p_flagShow boolean
     */
    private fun showHideIntroProgress(p_flagShow: Boolean) {
        val linProgress = findViewById<LinearLayout>(R.id.linProgress)
        if (p_flagShow) {
            linProgress.visibility = View.VISIBLE
        } else {
            linProgress.visibility = View.INVISIBLE
            setIntroProgressText("")
        }
    }

    /**
     * 메인화면 show 함수<br></br>
     * intro 레이아웃 hide
     */
    private fun showContentLayout() {
        LogPrinter.CF_debug("!----------------------------------------------------------")
        LogPrinter.CF_debug("!-- IUCOA0M00.showContentLayout()")
        LogPrinter.CF_debug("!----------------------------------------------------------")
        if (!flagShownContents) {
            flagShownContents = true
        //    showHideIntroProgress(false)

            // 팝업갯수 체크
            val customSQLiteHelper = CustomSQLiteHelper(applicationContext)
            val sqLiteDatabase = customSQLiteHelper.readableDatabase
            tmp_popupCnt = customSQLiteHelper.CF_popupCnt(sqLiteDatabase)
            Handler().postDelayed({
                showHideIntroProgress(false)
                hideIntroLayout()

                // -----------------------------------------------------------------------------------------
                // -- 로그인 정보 조회
                // -----------------------------------------------------------------------------------------
                val lastAuthDvsnNum =
                    CustomSQLiteFunction.getLastLoginAuthDvsnNum(applicationContext)
                val lastAuthDvsn = EnvConfig.AuthDvsn.getAuthDvsn(lastAuthDvsnNum)
                val lastAuthCsno = CustomSQLiteFunction.getLastLoginCsno(applicationContext)
                val lastAuthName = CustomSQLiteFunction.getLastLoginName(applicationContext)
                LogPrinter.CF_warnning("!---- lastAuthDvsn : $lastAuthDvsn [$lastAuthDvsnNum]")
                LogPrinter.CF_warnning("!---- lastAuthCsno : $lastAuthCsno")
                LogPrinter.CF_warnning("!---- lastAuthName : $lastAuthName")

                // 자동로그인 설정시 로그인페이지로 바로 이동
                if ("" != lastAuthCsno && !SharedPreferencesFunc.getFlagLogin(applicationContext) && false) { // TODO 자동로그인 오픈시 false 삭제
                    IntentManager.startIUCOB0M00_Login(
                        this@IUCOA0M00,
                        EnvConfig.REQUESTCODE_ACTIVITY_IUCOB0M00
                    )
                }


                if(jsonStartedWeb==null) {

                    Handler().postDelayed(Runnable {
                        IntentManager.startIUCOB0M00_Login(
                            this@IUCOA0M00,
                            EnvConfig.REQUESTCODE_ACTIVITY_IUCOB0M00
                        )

                    },500)

                }


                // -- 오늘하루 보지않기 여부 확인
                val date = Date()
                val sdf = SimpleDateFormat("yyyyMMdd", Locale.KOREA)
                val toDay = sdf.format(date).toInt()
                val keepDay = SharedPreferencesFunc.getDt_notiOneDayClose(applicationContext)

                // -- 누른날짜가 지났으면(공백이거나)
                if ("" == keepDay || toDay > keepDay.toInt()) {
                    if (tmp_popupCnt > 0 && bottomSheetDialog == null) {
                        bottomSheetDialog = BottomSheetDialog(applicationContext)
                        bottomSheetDialog!!.show(supportFragmentManager, "bottomSheet")
                    }
                }
            }, 500)
        } else {
            mIntroLayout!!.visibility = View.GONE
        }
    }

    /**
     * 초기 프로세스 구동 결과 체크 함수<br></br>
     */
    private fun updateInitProcessStateText() {

        if (flagRunOkGetBanner && flagFinishedDownloadBanner && flagRunOkVaccine && flagRunOkAppDefence) {
            checkState = 2
            showContentLayout()

            // -----------------------------------------------------------------------------------------
            // -- TODO : 인증서 복사(타겟API30 미만 or 단말OS11 미만일때 인증서 복사 시행됨 (SD -> DB) 추후 삭제 필요
            // -----------------------------------------------------------------------------------------
            try {
                val xSignHelper = XSignHelper(this@IUCOA0M00, EnvConfig.xSignDebugLevel)
                if (xSignHelper.CF_getDbCertCnt() == 0) {
                    xSignHelper.CF_moveCert();
                }
            } catch (e: Exception) {
                e.message
            }
        } else if (!flagRunOkGetBanner) {
            setIntroProgressText(resources.getString(R.string.progress_check_banner))
        } else if (!flagFinishedDownloadBanner) {
            setIntroProgressText(resources.getString(R.string.progress_download_banner))
        } else if (!flagRunOkAppDefence) {
            setIntroProgressText(resources.getString(R.string.progress_check_app_defence))
        } else if (false == false) {
            setIntroProgressText(resources.getString(R.string.progress_loading_vaccine))
        }

    }
    // #############################################################################################
    //  App 구동시 init
    // #############################################################################################
    /**
     * 초기 프로세스 실행<br></br>
     * 순차적 실행에서 일괄 실행으로 변경함<br></br>
     * -- 백신 구동 5초, 배너 이미지 다운로드 3G 환경 2초 등 시간이 오래걸림.
     * -- 현재(2019.01.11) 수정 안함(위변조 체크 안함) >> 삼성KNOX(보안폴더)에 설치 될경우 실행이 안되기 때문에.
     */
    private fun runInitProcess() {
        showHideIntroProgress(true)
        runVaccine_mini()

        // TODO : 위변조 개발 필요
//        if("product" == EnvConfig.operation){
//        //if(true){
//            runAppDepence();                      // 운영
//        } else{
//            flagRunOkAppDefence = true;           // 개발
//        }
        flagRunOkAppDefence = true // 현재 AppDepence 사용중지
        httpReq_bannerInfo()
    }

    /**
     * mVaccine 초기세팅
     */
    private fun setInitVaccine() {
        // mVaccine 초기 설정 값
        Constants.site_id = EnvConfig.mVaccineId
        Constants.license_key = EnvConfig.mVaccineLicenseKey

        // mVaccine 초기화
        try {
            SmartMedic.init(this)
        } catch (e: NullPointerException) {
            e.message
        } catch (e: Exception) {
            LogPrinter.CF_debug(resources.getString(R.string.log_fail_init_mvaccine))
            e.message
        }
    }

    /**
     * 앱위변조 검사(AppDefence) 초기 세팅
     */
    private fun runAppDepence() {
        if (!flagStartedAppDefence) {
            flagStartedAppDefence = true
            setIntroProgressText(resources.getString(R.string.progress_check_app_defence))
            appDefenceManager = AppDefenceManager(this, handler)
            appDefenceManager!!.initAppDefennce()

            // 앱위변조 검사 실행
            AppDefenceTask().execute()
        }
    }

    /**
     * 필수권한 요청
     */
    private fun requestDefaultPermissions() {
        // 1. 퍼미션 요청 이력 검사
        // 요청이력이 없는 경우 && 다시묻지않기 체크 후 거부한 경우 false 반환
        // 사용자가 거부하여 재 요청시 true 반환
        if (checkDefaultPermissionRational()) {
            ActivityCompat.requestPermissions(
                this,
                EnvConfig.getDefaultPermission(),
                REQUEST_DEFAULT_PERMISSIONS
            )
        } else {
            if (SharedPreferencesFunc.getReqPermissionDefault(applicationContext)) {
                // 사용자가 다시 묻지 않기를 선택하고 거절한 경우로 팝업을 통해 해당 권한이 왜 필요한지 안내하고
                // App 설정 페이지로 이동할 수 있도록 한다.
                if (dlgSettingPermission == null) {
                    dlgSettingPermission = CustomDialog(this)
                }
                if (!dlgSettingPermission!!.isShowing) {
                    dlgSettingPermission!!.show()
                    dlgSettingPermission!!.CF_setBackKeyMode(CustomDialog.BackMode.CANCELED)
                    dlgSettingPermission!!.CF_setTextContent(
                        """
                        ${resources.getString(R.string.dlg_p_denied)}
                        ${resources.getString(R.string.dlg_p_denied_setting)}
                        """.trimIndent()
                    )
                    dlgSettingPermission!!.CF_setDoubleButtonText(
                        resources.getString(R.string.btn_finish),
                        resources.getString(R.string.btn_setting)
                    )
                    dlgSettingPermission!!.setOnDismissListener { dialog ->
                        if (!(dialog as CustomDialog).CF_getCanceled()) {
                            checkState = 0
                            IntentManager.startAppSettingActivity(this@IUCOA0M00)
                            flagShownDlgPermission = false
                        } else {
                            finish()
                        }
                        dlgSettingPermission = null
                    }
                }
            } else {
                // 최초 권한 요청
                SharedPreferencesFunc.setReqPermissionDefault(applicationContext, true)
                ActivityCompat.requestPermissions(
                    this,
                    EnvConfig.getDefaultPermission(),
                    REQUEST_DEFAULT_PERMISSIONS
                )
            }
        }
    }
    // #############################################################################################
    //  Activity 호출 함수 모음
    // #############################################################################################
    /**
     * mVaccine mini 모드 실행
     */
    private fun runVaccine_mini() {
        if (!flagStartedVaccine) {
            flagStartedVaccine = true
            setIntroProgressText(resources.getString(R.string.progress_loading_vaccine))
            val intent = Intent(this, BackgroundScanActivity::class.java)
            intent.putExtra("useBlackAppCheck", true)
            intent.putExtra("scan_rooting", true)
            intent.putExtra("scan_package", true)
            //intent.putExtra("scan_heuristic"          , true)        // 휴레스틱 검사 default false
            //intent.putExtra("useDualEngine"           , true)        // BitDefender 엔진 검사 default false
            intent.putExtra("show_license", false) // 라이선스 유효성 토스트 보임 default true
            intent.putExtra("backgroundScan", true)
            intent.putExtra(
                "rootingexitapp",
                true
            ) // App 바로 종료되는 옵션 아니며, OnActivityResult 반환 값이 달라짐
            intent.putExtra("show_update", true) // App 업데이트시 토스트 show 유무 default true
            intent.putExtra("show_notify", true)
            intent.putExtra("notifyAutoClear", true) // (mini 전용) 검사 후 노티피케이션 자동 종료 default true;
            intent.putExtra("show_toast", false)
            intent.putExtra("show_warning", false)
            intent.putExtra("show_scan_ui", false)
            this.startActivityForResult(intent, EnvConfig.REQUESTCODE_VACCINE)
        }
    }


    /**
     * Web 실행 요청 Activity 호출 함수
     */
    @Throws(JSONException::class)
    private fun startActivityFromWeb() {
        LogPrinter.CF_debug("!----------------------------------------------------------")
        LogPrinter.CF_debug("!-- IUCOA0M00.startActivityFromWeb()")
        LogPrinter.CF_debug("!----------------------------------------------------------")
        if (jsonStartedWeb != null) {
            val jsonKey_host = "host"
            val jsonKey_key = "key"
            val jsonKey_name = "name"
            val jsonKey_rnno_enc = "rnno_enc"
            val jsonKey_is_webview_call = "is_webview_call"
            val jsonKey_hospital_Code = "hospital_code"
            val tmp_host = jsonStartedWeb!!.getString(jsonKey_host)
            val tmp_key = jsonStartedWeb!!.getString(jsonKey_key)
            val tmp_name = jsonStartedWeb!!.getString(jsonKey_name)
            val tmp_rnno_enc = jsonStartedWeb!!.getString(jsonKey_rnno_enc)
            val tmp_hospital_code = jsonStartedWeb!!.getString(jsonKey_hospital_Code)
            when (tmp_host) {
                EnvConfig.webHost_reqCert -> IntentManager.startIUCOC0M00_Web(
                    this@IUCOA0M00,
                    tmp_key
                )
                EnvConfig.webHost_reqPin ->
                    if (CustomSQLiteFunction.hasUserCsno(applicationContext)) {
                        IntentManager.startIUFC00M00_Auth(
                            this@IUCOA0M00,
                            Fido2Constant.AUTH_TECH_PIN,
                            AuthMode.LOGIN_WEB,
                            tmp_key
                        )
                    } else {
                        CommonFunction.CF_showCustomAlertDilaog(
                            this@IUCOA0M00,
                            resources.getString(R.string.dlg_empty_csno),
                            resources.getString(R.string.btn_ok)
                        )
                    }
                EnvConfig.webHost_reqFido ->
                    if (CustomSQLiteFunction.hasUserCsno(applicationContext)) {
                        IntentManager.startIUFC00M00_Auth(
                            this@IUCOA0M00,
                            Fido2Constant.AUTH_TECH_FINGER,
                            AuthMode.LOGIN_WEB,
                            tmp_key
                        )
                    } else {
                        CommonFunction.CF_showCustomAlertDilaog(
                            this@IUCOA0M00,
                            resources.getString(R.string.dlg_empty_csno),
                            resources.getString(R.string.btn_ok)
                        )
                    }
                EnvConfig.webHost_reqPattern ->
                    if (CustomSQLiteFunction.hasUserCsno(applicationContext)) {
                        IntentManager.startIUFC00M00_Auth(
                            this@IUCOA0M00,
                            Fido2Constant.AUTH_TECH_PATTERN,
                            AuthMode.LOGIN_WEB,
                            tmp_key
                        )
                    } else {
                        CommonFunction.CF_showCustomAlertDilaog(
                            this@IUCOA0M00,
                            resources.getString(R.string.dlg_empty_csno),
                            resources.getString(R.string.btn_ok)
                        )
                    }
                EnvConfig.webHost_checkCert -> IntentManager.startIUPC80M10_WebActivity(
                    this@IUCOA0M00,
                    tmp_key,
                    tmp_name,
                    tmp_rnno_enc
                )
                EnvConfig.webHost_singPin -> IntentManager.startIUFC00M00_Auth(
                    this@IUCOA0M00,
                    Fido2Constant.AUTH_TECH_PIN,
                    AuthMode.SIGN_WEB,
                    tmp_key
                )
                EnvConfig.webHost_singBio -> IntentManager.startIUFC00M00_Auth(
                    this@IUCOA0M00,
                    Fido2Constant.AUTH_TECH_FINGER,
                    AuthMode.SIGN_WEB,
                    tmp_key
                )
                EnvConfig.webHost_singPattern -> IntentManager.startIUFC00M00_Auth(
                    this@IUCOA0M00,
                    Fido2Constant.AUTH_TECH_PATTERN,
                    AuthMode.SIGN_WEB,
                    tmp_key
                )
                EnvConfig.webHost_singPattern -> IntentManager.startIUFC00M00_Auth(
                    this@IUCOA0M00,
                    Fido2Constant.AUTH_TECH_PATTERN,
                    AuthMode.SIGN_WEB,
                    tmp_key
                )

                EnvConfig.webHost_pay, EnvConfig.webHost_child ->
                    // --<2> (로그인) App 로그인과 Web 로그인은 별개로 운영되며 App 로그인이 되어 있는 경우에만 보험금청구가 가능함.
                    if (SharedPreferencesFunc.getFlagLogin(applicationContext)) {
                        // -------------------------------------------------------------------------------------
                        //  미사용 구간 : App 종료 시 logout 기능 추가하면서 메인Activity 최초 실행 시에는 로그아웃 상태임
                        //  Web에서 전달하는 고객 name 정보를 사용하지 않고, App 로그인 고객 name 안내 후 진행
                        // -------------------------------------------------------------------------------------
                        val customDialog1 = CustomDialog(this)
                        customDialog1.show()
                        customDialog1.CF_setTextContent(
                            "" + CustomSQLiteFunction.getLastLoginName(
                                applicationContext
                            ) + " 고객으로 보험금청구를 진행하시겠습니까?"
                        )
                        customDialog1.CF_setDoubleButtonText(
                            resources.getString(R.string.btn_no),
                            resources.getString(R.string.btn_yes)
                        )
                        customDialog1.setOnDismissListener { dialog ->
                            // --<1> (확인버튼)
                            if (!(dialog as CustomDialog).CF_getCanceled()) {
                                // --<2> (청구가능) 청구불가시 메시지 팝업
                                if (EnvConfig.isPayEnableHour(this)) {
                                    // -----------------------------------------------------------------
                                    //  보험금 청구금액 확인 : 100만원 이하 => 청구 불가 안내
                                    // -----------------------------------------------------------------
                                    val spannable1: Spannable =
                                        SpannableString(resources.getString(R.string.dlg_money_over_30))
                                    spannable1.setSpan(
                                        ForegroundColorSpan(Color.parseColor("#ff7c29c7")),
                                        10,
                                        18,
                                        Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                                    )
                                    val customDialog2 = CustomDialog(this@IUCOA0M00)
                                    customDialog2.show()
                                    customDialog2.CF_setTextContent(spannable1)
                                    customDialog2.CF_setDoubleButtonText(
                                        resources.getString(R.string.btn_no),
                                        resources.getString(R.string.btn_yes)
                                    )
                                    customDialog2.setOnDismissListener { dialog ->
                                        if (!(dialog as CustomDialog).CF_getCanceled()) {
                                            // -----------------------------------------------------------------
                                            //  보험금청구 불가 안내
                                            // -----------------------------------------------------------------
                                            val customDialog3 = CustomDialog(this@IUCOA0M00)
                                            customDialog3.show()
                                            customDialog3.CF_setBackKeyMode(CustomDialog.BackMode.NOTUSE)
                                            customDialog3.CF_setTextContent(resources.getString(R.string.dlg_is_over_30))
                                            customDialog3.CF_setSingleButtonText(
                                                resources.getString(
                                                    R.string.btn_ok
                                                )
                                            )
                                            customDialog3.setOnDismissListener {
                                                IntentManager.moveTaskToBack(this@IUCOA0M00, true)
                                                finish()
                                            }
                                        }
                                        // -- (메시지1 통과)
                                        else {
                                            // (자녀청구 가능)
                                            if (tmp_host == EnvConfig.webHost_child) {
                                                IntentManager.startIUII90M00_P_Activity(this@IUCOA0M00)
                                                finish()
                                            }
                                            // (본인청구 메시지2)
                                            else {
                                                // -- 계약자, 수익자 피보험자 안내
                                                val customDialog3 = CustomDialog(this@IUCOA0M00)
                                                customDialog3.show()
                                                val tmp_spannable: Spannable =
                                                    SpannableString(resources.getString(R.string.dlg_same_person))
                                                tmp_spannable.setSpan(
                                                    ForegroundColorSpan(
                                                        Color.parseColor(
                                                            "#ff7c29c7"
                                                        )
                                                    ), 4, 18, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                                                )
                                                customDialog3.CF_setTextContent(tmp_spannable)
                                                customDialog3.CF_setDoubleButtonText(
                                                    resources.getString(
                                                        R.string.btn_no
                                                    ), resources.getString(R.string.btn_yes)
                                                )
                                                customDialog3.setOnDismissListener { dialog ->
                                                    // --<> (청구가능)
                                                    if (!(dialog as CustomDialog).CF_getCanceled()) {
                                                        IntentManager.startIUII10M00_P_Activity(this@IUCOA0M00)
                                                        finish()
                                                    }
                                                    // --<> (청구불가2)
                                                    else {
                                                        val customDialog4 =
                                                            CustomDialog(this@IUCOA0M00)
                                                        customDialog4.show()
                                                        customDialog4.CF_setBackKeyMode(CustomDialog.BackMode.NOTUSE)
                                                        customDialog4.CF_setTextContent(
                                                            resources.getString(
                                                                R.string.dlg_save_person_2
                                                            )
                                                        )
                                                        customDialog4.CF_setSingleButtonText(
                                                            resources.getString(R.string.btn_ok)
                                                        )
                                                        customDialog4.setOnDismissListener {

                                                            IntentManager.moveTaskToBack(
                                                                this@IUCOA0M00,
                                                                true
                                                            )
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                    // --<2> (비로그인)
                    else {
                        IntentManager.startIUII01M00_Activity(this@IUCOA0M00)
                    }
                EnvConfig.webHost_search -> IntentManager.startIUII50M00_P(this@IUCOA0M00) // 청구내역 조회
                EnvConfig.webHost_smartPay -> IntentManager.startIUBC10M00_Activity(
                    this@IUCOA0M00,
                    tmp_hospital_code
                )
                EnvConfig.webHost_manageCert -> IntentManager.startIUPC30M00(this@IUCOA0M00)
                EnvConfig.webHost_manageFido -> IntentManager.startIUPC30M00(this@IUCOA0M00)
                //기타 호스트일경우
                else -> {

                }
            }
            jsonStartedWeb = null
        } else {


        }


        var mainUrl = EnvConfig.host_url

        LogPrinter.CF_debug("!----------------------------------------------------------")
        LogPrinter.CF_debug("!-- IUCOA0M00.KEY_INTENT_LAUNCH_MODE()")
        LogPrinter.CF_debug("!----------------------------------------------------------")



        //앱에 로그인된 상태라면 웹뷰에 로그인을 연동
        if (SharedPreferencesFunc.getFlagLogin(applicationContext)) {


                menuNo = intent.getStringExtra("menuNo") ?: ""

                menuLink = intent.getStringExtra("menuLink") ?: ""

                webLoginSession()


        }
        else{
            //mWebview?.loadUrl("file:///android_asset/test.html")
            mWebview?.loadUrl(mainUrl)




        }


    }


    //    /**
    //     * 전화연결 버튼 click 함수
    //     * @param p_view View
    //     */
    //    public void onClickCallCenter(final View p_view){
    //        if(CommonFunction.CF_checkAccessibilityTurnOn(this)){
    //            showCustomDialog(getResources().getString(R.string.dlg_accessible_move_call),
    //                    p_view,
    //                    new DialogInterface.OnDismissListener() {
    //                        @Override
    //                        public void onDismiss(DialogInterface dialog) {
    //                            if(!((CustomDialog) dialog).CF_getCanceled()){
    //                                Intent tmp_intent = new Intent(Intent.ACTION_DIAL);
    //                                tmp_intent.setData(Uri.parse("tel:" + getResources().getString(R.string.label_customer_center_phone)));
    //                                startActivity(tmp_intent);
    //                            }else{
    //                                clearAllFocus();
    //                                p_view.requestFocus();
    //                            }
    //                            p_view.setFocusableInTouchMode(false);
    //                        }
    //                    });
    //        } else {
    //            Intent tmp_intent = new Intent(Intent.ACTION_DIAL);
    //            tmp_intent.setData(Uri.parse("tel:" + getResources().getString(R.string.label_customer_center_phone)));
    //
    //            startActivity(tmp_intent);
    //        }
    //    }

//    /**
//     * 공동인증관리 버튼 click 함수
//     * @param p_view View
//     */
//    fun onClickManage_C_Auth(p_view: View?) {
//        startIUCOG0M01()
//    }

    /**
     * 하단공지 하루보지않기 버튼 클릭 함수
     * @param p_view View
     */
    fun onClickOneDayClose(p_view: View?) {
        val toDay = Date()
        val sdf = SimpleDateFormat("yyyyMMdd", Locale.KOREA)
        SharedPreferencesFunc.setDt_notiOneDayClose(applicationContext, sdf.format(toDay))
        bottomSheetDialog!!.dismiss()
    }

    /**
     * 하단공지 닫기 버튼 클릭 함수
     * @param p_view View
     */
    fun onClickNotiClose(p_view: View?) {
        bottomSheetDialog!!.dismiss()
    }

    //    /**
    //     * 보험 ARS안내 버튼 클릭 함수<br/>
    //     * 보험 ARS는 모바일슈랑스 페이지 이동으로 {@link #onClickReqWeb(View)} 함수 사용을 해야하나<br/>
    //     * 서버와 iOS 수정이 불가능한 관계로 공통 url이 아닌 해당 url 다이렉트로 호출한다.
    //     * @param p_view View
    //     */
    //    public void onClickARS(final View p_view){
    //
    //        final String tmp_url = EnvConfig.host_url+"/CC/IUCC75M00.do";
    //        if(CommonFunction.CF_checkAccessibilityTurnOn(this)){
    //
    //            showCustomDialog(getResources().getString(R.string.dlg_accessible_move_ars),
    //                    p_view,
    //                    new DialogInterface.OnDismissListener() {
    //                        @Override
    //                        public void onDismiss(DialogInterface dialog) {
    //                            if(((CustomDialog)dialog).CF_getCanceled()==false){
    //                                //Intent tmp_intent = new Intent(Intent.ACTION_VIEW);
    //                                //tmp_intent.setData(Uri.parse(EnvConfig.host_url+"/CC/IUCC75M00.do"));
    //                                //startActivity(tmp_intent);
    //                                callWebBrowser(tmp_url);
    //                            }else{
    //                                clearAllFocus();
    //                                p_view.requestFocus();
    //                            }
    //                            p_view.setFocusableInTouchMode(false);
    //                        }
    //                    });
    //        }
    //        else {
    //            //Intent tmp_intent = new Intent(Intent.ACTION_VIEW);
    //            //tmp_intent.setData(Uri.parse(EnvConfig.host_url+"/CC/IUCC75M00.do"));
    //            //startActivity(tmp_intent);
    //            callWebBrowser(tmp_url);
    //        }
    //    }

    private fun showWebPage(isWebView: Boolean, pageNum: String, title: String) {
        // csno , loginType 값으로 로그인 상태를 확인한다.
        //------------------------------------------------------------------------------------------
        //  Web페이지에서 로그인필수가 아닌 메뉴도 로그인시키는 오류가 있기에, App로그인 상태가 아니면
        //  csno를 empty로 변경함.
        //------------------------------------------------------------------------------------------
        var tmp_csNo = ""
        var tmp_loginType = ""
        var tmp_tempKey = ""
        if (SharedPreferencesFunc.getFlagLogin(applicationContext)) {
            tmp_csNo = CustomSQLiteFunction.getLastLoginCsno(applicationContext)
            tmp_loginType = CustomSQLiteFunction.getLastLoginAuthDvsnNum(applicationContext)
            tmp_tempKey = SharedPreferencesFunc.getWebTempKey(applicationContext)
        }
        val moveUrl = (EnvConfig.host_url + EnvConfig.URL_WEB_LINK
                + "?csno=" + tmp_csNo
                + "&loginType=" + tmp_loginType
                + "&tempKey=" + tmp_tempKey
                + "&deviceType=A"
                + "&version=" + EnvConfig.appVersion
                + "&page=" + pageNum
                + "&menuNo="
                + "&link="


                )
        if (isWebView) {
            WebBrowserHelper.startWebViewActivity(applicationContext, 0, false, moveUrl, title)
        } else {
            WebBrowserHelper.callWebBrowser(applicationContext, moveUrl)
        }
    }

    /**
     * 간편인증관리 버튼 click 함수
     * @param p_view View
     */
    fun onClickManage_S_Auth(p_view: View?) {
        IntentManager.startIUPC30M00(this@IUCOA0M00)
    }

    /**
     * 웹페이지 메뉴 버튼 클릭
     * @param p_view View
     */
    fun onClickReqWeb() {
        LogPrinter.CF_debug("!-----------------------------------------------------------")
        LogPrinter.CF_debug("!-- IUCOA0M00.onClickReqWeb()")
        LogPrinter.CF_debug("!-----------------------------------------------------------")


        //내소식
        val pageNum = "27"
        var flagLaunchWeb = true // 웹호출실행 여부

        // -----------------------------------------------------------------------------------------
        //  로그인 필수 메뉴 요청 : App 로그인 상태 검사 ==> 로그인 상태가 아닌경우 Dialog 팝업
        // -----------------------------------------------------------------------------------------
        if (EnvConfig.arrLoginMenuPage.contains(pageNum)) {
            if (!SharedPreferencesFunc.getFlagLogin(applicationContext)) {
                flagLaunchWeb = false
                showCustomDialog(resources.getString(R.string.dlg_need_login_menu)) { dialog ->
                    if (!(dialog as CustomDialog).CF_getCanceled()) {
                        // -- 로그인 진행
                        mPageNum = pageNum
                        IntentManager.startIUCOB0M00_Login(
                            this@IUCOA0M00,
                            EnvConfig.REQUESTCODE_ACTIVITY_IUCOB0M00
                        )
                    } else if (CommonFunction.CF_checkAccessibilityTurnOn(this@IUCOA0M00.applicationContext)) {
                        clearAllFocus()
                        //  p_view.requestFocus()
                    }
                    //p_view.isFocusableInTouchMode = false
                }
            }
        }

        // -----------------------------------------------------------------------------------------
        //  Web 브라우져 오픈 : 현재 로그인 필수 메뉴가 아니여도 Web에서 로그인되는 문제가 있음.
        //  :: App에서 csnNo  empty 전달로 로그인처리 되는 것 막음.=> Web에서 정상적으로 로그인 체크를
        //  하면 csNo empty 변환 없이 전송해도 됨.
        // -----------------------------------------------------------------------------------------
        if (flagLaunchWeb) {
            when (pageNum) {
                "1", "11" -> showWebPage(
                    true,
                    pageNum,
                    resources.getString(R.string.btn_insure_goods)
                )
                "2" -> showWebPage(true, pageNum, resources.getString(R.string.btn_my_page2))
                "6", "8", "10", "21", "27" -> showWebPage(
                    true,
                    pageNum,
                    resources.getString(R.string.btn_my_page)
                )
                "9" -> {
                    val customDialog = CustomDialog(this)
                    customDialog.show()
                    customDialog.CF_setBackKeyMode(CustomDialog.BackMode.NOTUSE)
                    customDialog.CF_setTextContent("진행하실 업무를 선택하세요.")
                    customDialog.CF_setTripleButtonText(
                        "보험료납입",
                        "실시간보험료납부",
                        this.resources.getString(R.string.btn_cancel)
                    )
                    customDialog.setOnDismissListener { dialog ->
                        // left버튼 (보험료납입)
                        if ((dialog as CustomDialog).CF_getBtnIndex() == 1) {
                            //WebBrowserHelper.callWebBrowser(getApplicationContext(), tmp_url);
                            showWebPage(true, pageNum, resources.getString(R.string.btn_my_page))
                        } else if (dialog.CF_getBtnIndex() == 2) {
                            // -- 포스트페이문제로 웹브라우저 연결
                            //WebBrowserHelper.callWebBrowser(getApplicationContext(), tmp_url_22);
                            showWebPage(true, "22", resources.getString(R.string.btn_my_page))
                        }
                    }
                }

                // case "20":  // 미디어뉴스 --v_1.4.5 메뉴삭제
                "3", "14", "15", "18" ->
                    showWebPage(true, pageNum, resources.getString(R.string.btn_customer_center))

                "5" -> {
                    val tmp_bbs_url = EnvConfig.host_url + subUrl_bbs
                    WebBrowserHelper.startWebViewActivity(
                        applicationContext,
                        0,
                        false,
                        tmp_bbs_url,
                        resources.getString(R.string.btn_customer_center)
                    )
                }

                "4" -> showWebPage(true, pageNum, resources.getString(R.string.btn_more_service))
                else ->
                    // --<> (접근성ON일때)
                    if (CommonFunction.CF_checkAccessibilityTurnOn(this)) {
                        showCustomDialog(
                            resources.getString(R.string.dlg_accessible_move_smart_web)

                        ) { dialog ->
                            if (!(dialog as CustomDialog).CF_getCanceled()) {
                                showWebPage(false, pageNum, "")
                            } else {
                                clearAllFocus()

                            }

                        }
                    } else {
                        showWebPage(false, pageNum, "")
                    }
            }
        }
    }

    /**
     * 보험금청구 버튼 click 함수 (xml에서 요청함)
     * @param p_view View
     */
    fun onClickReqBenefit(p_view: View?) {
        IntentManager.startIUII01M00_Activity(this@IUCOA0M00)
    }


    // ############################################################################################
    //  Http 통신 관련
    // ############################################################################################
    override fun handleMessage(p_message: Message) {
        LogPrinter.CF_debug("!----------------------------------------------------------")
        LogPrinter.CF_debug("!-- IUCOA0M00.handleMessage()")
        LogPrinter.CF_debug("!----------------------------------------------------------")

        if (!isDestroyed) {
            when (p_message.what) {
                HANDLERJOB_APP_DEFENCE_SUCCESS -> {
                    flagRunOkAppDefence = true
                    updateInitProcessStateText()
                }
                HANDLERJOB_APP_DEFENCE_ERROR -> {
                    showHideIntroProgress(false)
                    showErrorFinishDlg(resources.getString(R.string.dlg_error_appdefence))
                }
                HANDLERJOB_APP_DEFENCE_FAIL -> {
                    showHideIntroProgress(false)
                    showNeedAppUpdateDlg()
                }
                HANDLERJOB_INIT -> {
                    flagRunOkGetBanner = true
                    try {

                        var str = p_message.obj as String
                        var json = JSONObject(str)



                        if (EnvConfig.uiTest) {
                            var data = json.getJSONObject("data")
                            data.put("arr_popup", data.getJSONArray("arr_banner"))
                        }
                        httpRes_bannerInfo(json)
                    } catch (e: JSONException) {
                        LogPrinter.CF_line()
                        LogPrinter.CF_debug(resources.getString(R.string.log_json_exception))
                    }
                }
                HANDLERJOB_ERROR_INIT -> {
                    flagRunOkGetBanner = true
                    flagFinishedDownloadBanner = true

                    //  -- 초기 정보 요청은 실패 : 배너 정보 없이 컨텐츠 화면이 보이도록 한다.
                    updateInitProcessStateText()
                }
                HANDLERJOB_SERVER_ON_CHECK_OK -> LogPrinter.CF_debug(p_message.obj.toString())
                HANDLERJOB_SERVER_ON_CHECK_FAIL -> {
                    LogPrinter.CF_debug(p_message.obj.toString())
                    showCustomDialogAndAppFinish(p_message.obj.toString())
                }
                else -> {
                }
            }
        }
    }

    /**
     * 초기 세팅 정보 요청 함수
     */
    private fun httpReq_bannerInfo() {
        updateInitProcessStateText()
        //setIntroProgressText(getResources().getString(R.string.progress_check_banner));
        val builder = Uri.Builder()
        builder.appendQueryParameter(
            "csno",
            CustomSQLiteFunction.getLastLoginCsno(applicationContext)
        )
        builder.appendQueryParameter(
            "tempKey",
            SharedPreferencesFunc.getWebTempKey(applicationContext)
        )
        HttpConnections.sendPostData(
            EnvConfig.host_url + EnvConfig.URL_APP_INIT,
            builder.build().encodedQuery,
            handler,
            HANDLERJOB_INIT,
            HANDLERJOB_ERROR_INIT
        )

    }


    /**
     * 에러종료 다이얼로그 Show 함수
     * @param p_message String
     */
    private fun showErrorFinishDlg(p_message: String) {
        val customDialog = CustomDialog(this)
        customDialog.show()
        customDialog.CF_setBackKeyMode(CustomDialog.BackMode.NOTUSE)
        customDialog.CF_setTextContent(p_message)
        customDialog.CF_setSingleButtonText(resources.getString(R.string.btn_ok))
        customDialog.setOnDismissListener { finish() }
    }

    /**
     * App 업데이트 안내 다이얼로그 show 함수<br></br>
     * 앱 위변조 검사 실패 시에만 호출되어야 한다.<br></br>
     */
    private fun showNeedAppUpdateDlg() {
        // -----------------------------------------------------------------------------------------
        //  앱 위변조 검사에는 App 버전, Hash Key 값이 사용된다.
        //  신규 앱 업데이트 시 기존 앱도 사용이 가능하도록 하려면 Appdefence  관리자 사이트에서 기존 정보는 살려두고 신규 앱 정보만 등록<br/>
        //  신규 앱 업데이트 시 기존 앱 사용 불가능하도록 하려면 Appdenfece 관리자 사이트에서 기존 정보 삭제 후 신규 앱 정보만 등록이 필요하다.
        //  ( 단말기마다 Play Store에 업데이트 뜨는 시기가 다르기 때문에 신규 앱 업데이트 후 일주일 정도 뒤에 기존 앱 정보 삭제를 추천한다.)
        //  ( A 단말기는 Play Store 에 App 업데이트가 뜨지만 B 단말기는 안뜨는 경우를 말하며 ==> 일반적으로 Play Store 캐시 삭제하면 업데이트 뜸)
        //  ( 이는 단말기마다 Play Store 통신 주기가 달라 발생하는 문제로 정확한 업데이트 시간은 알 수 없다.==> 구글답변)
        //  ( 단계적 배포가 아닌 전체 배포를 하여도 위 증상은 동일하다.)
        // -----------------------------------------------------------------------------------------
        val customDialog = CustomDialog(this)
        customDialog.show()
        customDialog.CF_setBackKeyMode(CustomDialog.BackMode.NOTUSE)
        customDialog.CF_setTextContent(resources.getString(R.string.dlg_need_update_app))
        customDialog.CF_setDoubleButtonText(
            resources.getString(R.string.btn_finish),
            resources.getString(R.string.btn_update)
        )
        customDialog.setOnDismissListener { dialog ->
            if (!(dialog as CustomDialog).CF_getCanceled()) {
                //  -- App 업데이트 마켓 이동
                val intent =
                    Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=$packageName"))
                startActivity(intent)
            }
            finish()
        }
    }

    /**
     * AppDefence 앱위변조 검사 실행 Task<br></br>
     */
    private inner class AppDefenceTask : AsyncTask<Void?, Void?, Void?>() {
        override fun doInBackground(vararg params: Void?): Void? {
            appDefenceManager!!.appDefenceCallApi()
            return null
        }
    }


    /**
     * 푸쉬 메시지가 있을경우 다이얼로그 팝업
     */
    private fun showPushPopup() {
        LogPrinter.CF_debug("!----------------------------------------------------------")
        LogPrinter.CF_debug("!-- IUCOA0M00.showPushPopup() --푸쉬내용이 있을 경우 팝업")
        LogPrinter.CF_debug("!----------------------------------------------------------")
        LogPrinter.CF_debug("!---- pushMessage : $pushMessage")


        if (EnvConfig.uiTest) {
            pushTitle = "제목"
            pushMessage = "테스트입니다."

        }

        // push팝업
        if ("" != pushMessage) {
            showCustomDialog(
                """
                $pushTitle
                $pushMessage""".trimIndent(),
                resources.getString(R.string.btn_cancel),
                resources.getString(R.string.btn_ok),
                null
            ) { dialog -> // --<> (취소)
                if ((dialog as CustomDialog).CF_getCanceled()) {
                } else {
                    // 내소식 웹뷰 실행
                    //startLoginActivity();
                    onClickReqWeb()
                }

                // 팝업 후 내용 삭제
                pushTitle = ""
                pushMessage = ""
            }

            //            showCustomDialog(pushTitle + '\n' + pushMessage, new DialogInterface.OnDismissListener() {
            //                @Override
            //                public void onDismiss(DialogInterface dialog) {
            //                    // 팝업 후 내용 삭제
            //                    pushTitle   = "";
            //                    pushMessage = "";
            //                }
            //            });
        }
    }

    // 금융결제원 공통FIDO(생체)인증 시 사용하는 키(싸인후 출력)
    //    private String getFacetID(PackageInfo paramPackageInfo) {
    //        try {
    //            ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(paramPackageInfo.signatures[0].toByteArray());
    //            Certificate certificate = CertificateFactory.getInstance("X509").generateCertificate(byteArrayInputStream);
    //            MessageDigest messageDigest = MessageDigest.getInstance("SHA1");
    //            String facetID = "" + Base64.encodeToString(((MessageDigest) messageDigest).digest(certificate.getEncoded()), 3);
    //            return facetID;
    //        } catch (Exception e) {
    //            e.printStackTrace();
    //        }
    //        return null;
    //    }


    /**
     * 배너파일 저장 경로 업데이트 함수<br></br>
     * @param p_path String
     * @param p_savedPath String
     */
    @Synchronized
    private fun updateBannerSavedPath(p_path: String, p_savedPath: String) {
        val customSQLiteHelper = CustomSQLiteHelper(applicationContext)
        val sqLiteDatabase = customSQLiteHelper.writableDatabase
        customSQLiteHelper.CF_UpdateBannerSavedPath(sqLiteDatabase, p_path, p_savedPath) // 배너
        customSQLiteHelper.CF_UpdatePopupSavedPath(sqLiteDatabase, p_path, p_savedPath) // 공지
        sqLiteDatabase.close()
        customSQLiteHelper.close()
    }


    /**
     * 배너 이미지 다운로드 서비스 콜백
     */
    private val callBackDownload: ICallback = object : ICallback {
        override fun onDownLoadStart(p_path: String, p_savePath: String) {}
        override fun onProgress(p_path: String, p_progress: Int) {}
        override fun onDownLoadComplete(p_path: String, p_savePath: String) {
            // -- 배너 경로 업데이트
            updateBannerSavedPath(p_path, p_savePath)
            if (serviceDownload != null && !serviceDownload!!.CF_flagIsDownLoading()) {
                // -- 컨텐츠 화면 이미지 세팅


                if (flagBindServiceDownload) {
                    flagBindServiceDownload = false
                    unbindService(connection)
                    serviceDownload = null
                }
                flagFinishedDownloadBanner = true
                updateInitProcessStateText()
            }
        }

        override fun onCancel(p_path: String) {
            if (serviceDownload != null && !serviceDownload!!.CF_flagIsDownLoading()) {
                // -- 컨텐츠 화면 이미지 세팅

                if (flagBindServiceDownload) {
                    flagBindServiceDownload = false
                    unbindService(connection)
                    serviceDownload = null
                }
                flagFinishedDownloadBanner = true
                updateInitProcessStateText()
            }
        }

        override fun onError(p_path: String) {
            if (serviceDownload != null && !serviceDownload!!.CF_flagIsDownLoading()) {
                // -- 컨텐츠 화면 이미지 세팅

                if (flagBindServiceDownload) {
                    flagBindServiceDownload = false
                    unbindService(connection)
                    serviceDownload = null
                }
                flagFinishedDownloadBanner = true
                updateInitProcessStateText()
            }
        }
    }

    /**
     * 배너 이미지 다운로드 서비스 커넥션
     */
    private val connection: ServiceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName, p_binder: IBinder) {
            serviceDownload = (p_binder as LocalBinder).service
            serviceDownload?.CF_registerCallBack(callBackDownload)
            flagBindServiceDownload = true
            for (i in arrbannerPath!!.indices) {
                serviceDownload?.downLoad(arrBannerFile!![i], arrbannerPath!![i])
            }
        }

        override fun onServiceDisconnected(name: ComponentName) {}
    }

    override fun dispatchPopulateAccessibilityEvent(event: AccessibilityEvent): Boolean {
        return super.dispatchPopulateAccessibilityEvent(event)
    }


    /**
     * 초기 세팅 정보 요청 결과 처리 함수
     * @param p_jsonObject JSONObject
     */
    @Throws(JSONException::class)
    private fun httpRes_bannerInfo(p_jsonObject: JSONObject) {
        val tmp_flagRnCheckInitProcess: Boolean
        val jsonKey_errorCode = "errCode"
        val jsonKey_data = "data"
        val jsonKey_banner = "arr_banner"
        val jsonKey_popup = "arr_popup"
        //val jsonKey_flagRegCert = "flagRegCert" // 공동인증 등록여부
        val jsonKey_noticeTitle = "noticeTitle" // 최근공지 1줄 타이틀
        val jsonKey_noticeNo = "noticeNo" // 최근공지 1줄 게시번호

        // --<1>
        if (p_jsonObject.has(jsonKey_errorCode)) {            // errorCode는 필수이나 모두 empty로 반환함.
            // --<2>
            if (p_jsonObject.has(jsonKey_data)) {
                val tmp_jsonData = p_jsonObject.getJSONObject(jsonKey_data)
                val tmp_jsonBanner = tmp_jsonData.getJSONArray(jsonKey_banner)
                val tmp_jsonPopup = tmp_jsonData.getJSONArray(jsonKey_popup)


                if (tmp_jsonData.has(jsonKey_noticeTitle)) {
                    subUrl_bbs += "&nttId=" + tmp_jsonData.getString(jsonKey_noticeNo)
                }

                // -- 배너 정보 업데이트 및 다운로드(정보만 가져옴)
                updateBannerInfos(tmp_jsonBanner)
                // -- 공지 정보 업데이트 및 다운로드(정보만 가져옴)
                updatePopupInfos(tmp_jsonPopup)

                // -- 이미지 다운로드
                if (!downloadImage()) {
                    flagFinishedDownloadBanner = true

                    // 컨텐츠 화면 이미지 세팅
                    //adapter.CF_refreshBannerInfo()
                    //indicator.CF_drawDots(adapter.CF_getRealCount())
                    //viewPagerBanner.currentItem = adapter.CF_GetInitPosition()
                    tmp_flagRnCheckInitProcess = true
                } else {
                    tmp_flagRnCheckInitProcess = false
                }
            } else {
                tmp_flagRnCheckInitProcess = true
            }
        } else {
            tmp_flagRnCheckInitProcess = true
        }
        if (tmp_flagRnCheckInitProcess) {
            flagFinishedDownloadBanner = true
            updateInitProcessStateText()
        }
    }

// #############################################################################################
    //  이미지(배너,공지) 처리 관련
    // #############################################################################################
    /**
     * 이미지 path를 받아 이미지 파일명을 추출한다(배너,공지).<br></br>
     * 파일명은 이미지 path에서 =을 구분하여 만든다.<br></br>
     * @param p_imgUrl  String
     * @return          String
     */
    private fun getImageFileName(p_imgUrl: String): String {
        var tmp_fileName = p_imgUrl
        val tmp_fileNames = p_imgUrl.split("=").toTypedArray()
        if (tmp_fileNames.size > 1) {
            tmp_fileName = tmp_fileNames[1]
        }
        return tmp_fileName
    }


    /**
     * 이미지 다운로드(배너,공지)
     */
    private fun downloadImage(): Boolean {
        var tmp_flagDownloading = false
        arrbannerPath!!.clear()
        arrBannerFile!!.clear()

        // 1. DB에서 이미지(배너,공지) 정보를 가져온다.
        val tmp_helper = CustomSQLiteHelper(applicationContext)
        val tmp_sqlite = tmp_helper.readableDatabase

        // 배너
        val tmp_arrPath = tmp_helper.CF_SelectBannerPaths(tmp_sqlite)
        val tmp_arrSavedPath = tmp_helper.CF_SelectBannerSavedPaths(tmp_sqlite)
        // 공지
        tmp_arrPath.addAll(tmp_helper.CF_SelectPopupPaths(tmp_sqlite))
        tmp_arrSavedPath.addAll(tmp_helper.CF_SelectPopupSavedPaths(tmp_sqlite))
        tmp_sqlite.close()
        tmp_helper.close()

        // 2. 다운로드 받아야 할 파일을 구분한다.
        for (i in tmp_arrPath.indices) {
            val tmp_fileName = getImageFileName(tmp_arrPath[i])
            val tmp_file = File(filesDir, "$tmp_fileName.png")
            if (!tmp_file.exists()) {
                arrbannerPath!!.add(tmp_arrPath[i])
                arrBannerFile!!.add(tmp_file)
            } else {
                // 파일은 존재하나 savedPath 정보가 없는 경우 => 파일 다운로드 완료가 안된 경우
                // : 기존 파일을 삭제하고 다운로드 목록 추가
                if (TextUtils.isEmpty(tmp_arrSavedPath[i])) {
                    tmp_file.delete()
                    arrbannerPath!!.add(tmp_arrPath[i])
                    arrBannerFile!!.add(tmp_file)
                }
            }
        }

        // 3. 파일을 다운로드한다.
        if (arrbannerPath!!.size > 0) {
            tmp_flagDownloading = true
            //setIntroProgressText(getResources().getString(R.string.progress_download_banner));
            updateInitProcessStateText()
            bindService(Intent(this, Service_ImageDown::class.java), connection, BIND_AUTO_CREATE)
        }
        return tmp_flagDownloading
    }


// #############################################################################################
    //  배너처리 관련
    // #############################################################################################
    /**
     * 배너 정보 갱신<br></br>
     * 기존 배너 정보 중 사용하지 않은 정보는 삭제(파일 포함)<br></br>
     * 신규 배너 정보는 추가 한다.
     * @param p_jsonArray       JSONArray
     * @throws JSONException    json
     */
    @Throws(JSONException::class)
    private fun updateBannerInfos(p_jsonArray: JSONArray) {
        val jsonKey_path = "path" // 배너 사진 다운로드 경로
        val jsonKey_link = "link" // 배너 사진 click 링크 연결 경로
        val jsonKey_width = "width" // 배너 사진 width
        val jsonKey_height = "height" // 배너 사진 height
        val jsonKey_desc = "desc" // 배너 설명

        // 1. DB에 저장되어 있는 모든 배너 다운로드 경로 SELECT
        val tmp_helper = CustomSQLiteHelper(applicationContext)
        val tmp_sqliteDatabase = tmp_helper.writableDatabase
        val tmp_arrBannerPath = tmp_helper.CF_SelectBannerPaths(tmp_sqliteDatabase) // 다운로드 경로
        val tmp_arrSavedPath = tmp_helper.CF_SelectBannerSavedPaths(tmp_sqliteDatabase) // 저장 경로

        // 2. PATH 비교를 하여 사용하지 않은 배너 사진 파일 삭제
        for (i in tmp_arrBannerPath.indices) {
            val tmp_bannerPath = tmp_arrBannerPath[i]
            var tmp_flagNeedRemove = true
            for (j in 0 until p_jsonArray.length()) {
                val tmp_jsonBanner = p_jsonArray.getJSONObject(j)
                val tmp_path = tmp_jsonBanner.getString(jsonKey_path)
                if (tmp_bannerPath == tmp_path) {
                    tmp_flagNeedRemove = false
                    break
                }
            }

            // -- (종료된이미지) 이미지 파일 삭제
            if (tmp_flagNeedRemove) {
                val tmp_fileName = getImageFileName(tmp_bannerPath)
                val tmp_file = File(filesDir, "$tmp_fileName.png")
                if (tmp_file.exists()) {
                    tmp_file.delete()
                }
            }
        }

        // 3. DB에 저장되어 있는 배너 정보 모두 삭제
        tmp_helper.CF_DelBannerInfo(tmp_sqliteDatabase)

        // 4. DB에 신규 배너 정보 모두 추가
        // 서버 반환 정보에 배너 order by 정보가 없음.
        // 배너 데이터 순으로 보이기 위해 모든 데이터 삭제 후 다시 추가 했으며 기존에 저장된 savedPath 정보는 업데이트 함.
        if (p_jsonArray.length() > 0) {
            var tmp_width = 0
            var tmp_height = 0
            for (i in 0 until p_jsonArray.length()) {
                val tmp_jsonBanner = p_jsonArray.getJSONObject(i)
                val tmp_path = tmp_jsonBanner.getString(jsonKey_path) // 배너 이미지 다운로드 링크
                val tmp_link = tmp_jsonBanner.getString(jsonKey_link) // 배너 링크 url
                val tmp_desc = tmp_jsonBanner.getString(jsonKey_desc) // 배너 설명
                tmp_width = tmp_jsonBanner.getInt(jsonKey_width)
                tmp_height = tmp_jsonBanner.getInt(jsonKey_height)
                tmp_helper.CF_InsertBannerInfo(
                    tmp_sqliteDatabase,
                    tmp_path,
                    tmp_link,
                    tmp_desc,
                    tmp_width,
                    tmp_height
                )
                if (tmp_arrBannerPath.contains(tmp_path)) {
                    val tmp_index = tmp_arrBannerPath.indexOf(tmp_path)
                    tmp_helper.CF_UpdateBannerSavedPath(
                        tmp_sqliteDatabase,
                        tmp_path,
                        tmp_arrSavedPath[tmp_index]
                    )
                }
            }

            // 배너 레이아웃 갱신
            // 마지막 이미지의 가로 세로 길이를 기준으로 배너 레이아웃의 사이즈를 결정한다.
            /*
            if (tmp_width > 0) {
                val tmp_lp = fraBannerLayout!!.layoutParams as LinearLayout.LayoutParams
                //tmp_lp.height = resources.displayMetrics.widthPixels * tmp_height / tmp_width
                tmp_lp.height = resources.displayMetrics.widthPixels * 280 / 640
                fraBannerLayout!!.layoutParams = tmp_lp
                fraBannerLayout!!.visibility = View.VISIBLE
            }*/
        } else {
            //fraBannerLayout!!.visibility = View.GONE
        }
        tmp_helper.close()
        tmp_sqliteDatabase.close()
    }

    /**
     * 하단 공지 정보 갱신<br></br>
     * 기존 배너 정보 중 사용하지 않은 정보는 삭제(파일 포함)<br></br>
     * 신규 배너 정보는 추가 한다.
     * @param p_jsonArray       JSONArray
     * @throws JSONException    json
     */
    @Throws(JSONException::class)
    private fun updatePopupInfos(p_jsonArray: JSONArray) {
        val jsonKey_path = "path" // 공지 사진 다운로드 경로
        val jsonKey_link = "link" // 공지 사진 click 링크 연결 경로
        val jsonKey_width = "width" // 공지 사진 width
        val jsonKey_height = "height" // 공지 사진 height
        val jsonKey_desc = "desc" // 공지 설명

        // 1. DB에 저장되어 있는 모든 공지 다운로드 경로 SELECT
        val tmp_helper = CustomSQLiteHelper(applicationContext)
        val tmp_sqliteDatabase = tmp_helper.writableDatabase
        val tmp_arrPopupPath = tmp_helper.CF_SelectPopupPaths(tmp_sqliteDatabase) // 다운로드 경로
        val tmp_arrSavedPath = tmp_helper.CF_SelectPopupSavedPaths(tmp_sqliteDatabase) // 저장 경로

        // 2. PATH 비교를 하여 사용하지 않은 공지 사진 파일 삭제
        for (i in tmp_arrPopupPath.indices) {
            val tmp_popupPath = tmp_arrPopupPath[i]
            var tmp_flagNeedRemove = true
            for (j in 0 until p_jsonArray.length()) {
                val tmp_jsonPopup = p_jsonArray.getJSONObject(j)
                val tmp_path = tmp_jsonPopup.getString(jsonKey_path)
                if (tmp_popupPath == tmp_path) {
                    tmp_flagNeedRemove = false
                    break
                }
            }

            // -- (종료된이미지) 이미지 파일 삭제
            if (tmp_flagNeedRemove) {
                val tmp_fileName = getImageFileName(tmp_popupPath)
                val tmp_file = File(filesDir, "$tmp_fileName.png")
                if (tmp_file.exists()) {
                    tmp_file.delete()
                }
            }
        }

        // 3. DB에 저장되어 있는 공지 정보 모두 삭제
        tmp_helper.CF_DelPopupInfo(tmp_sqliteDatabase)

        // 4. DB에 신규 공지 정보 모두 추가
        // 서버 반환 정보에 공지 order by 정보가 없음.
        // 공지 데이터 순으로 보이기 위해 모든 데이터 삭제 후 다시 추가 했으며 기존에 저장된 savedPath 정보는 업데이트 함.
        if (p_jsonArray.length() > 0) {
            var tmp_width = 0
            var tmp_height = 0
            for (i in 0 until p_jsonArray.length()) {
                val tmp_jsonPopup = p_jsonArray.getJSONObject(i)
                val tmp_path = tmp_jsonPopup.getString(jsonKey_path) // 배너 이미지 다운로드 링크
                val tmp_link = tmp_jsonPopup.getString(jsonKey_link) // 배너 링크 url
                val tmp_desc = tmp_jsonPopup.getString(jsonKey_desc) // 배너 설명
                tmp_width = tmp_jsonPopup.getInt(jsonKey_width)
                tmp_height = tmp_jsonPopup.getInt(jsonKey_height)
                tmp_helper.CF_InsertPopupInfo(
                    tmp_sqliteDatabase,
                    tmp_path,
                    tmp_link,
                    tmp_desc,
                    tmp_width,
                    tmp_height
                )
                if (tmp_arrPopupPath.contains(tmp_path)) {
                    val tmp_index = tmp_arrPopupPath.indexOf(tmp_path)
                    tmp_helper.CF_UpdatePopupSavedPath(
                        tmp_sqliteDatabase,
                        tmp_path,
                        tmp_arrSavedPath[tmp_index]
                    )
                }
            }
        }
        tmp_helper.close()
        tmp_sqliteDatabase.close()
    }

}