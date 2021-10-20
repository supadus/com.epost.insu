package com.epost.insu.activity.auth

import android.content.DialogInterface
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Message
import android.text.TextUtils
import android.view.View
import android.widget.ImageButton
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.epost.insu.*
import com.epost.insu.activity.Activity_Default
import com.epost.insu.activity.BC.IUBC10M00_P
import com.epost.insu.activity.IUCOA0M00
import com.epost.insu.adapter.CustomPagerAdapter
import com.epost.insu.common.CommonFunction
import com.epost.insu.common.CustomViewPager
import com.epost.insu.common.LogPrinter
import com.epost.insu.dialog.CustomDialog
import com.epost.insu.event.OnFragmentCertificateEventListener
import com.epost.insu.event.OnFragmentKeyGuardEventListener
import com.epost.insu.fragment.Fragment_Certificate
import com.epost.insu.fragment.Fragment_KeyGuard
import com.epost.insu.network.HttpConnections
import com.epost.insu.network.WeakReferenceHandler
import com.epost.insu.network.WeakReferenceHandler.ObjectHandlerMessage
import com.initech.cryptox.util.Base64Util
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException

/**
 * 인증 > 공동인증서 로그인(WEB)
 * @since     :
 * @version   : 1.2
 * @author    : LSH
 * <pre>
 * - 공동인증서를 통해 로그인한다.
 * - 공동인증 성공시 DB에 을 저장한다. (이름, 고객번호, 로그인타입)
 * - 공동인증 성공시 SharedPreferences에 로그인 정보를 저장한다. (로그아웃시 삭제함)
 * - Fragment 01. 공동인증서 선택(Fragment_Certificate)
 * 02. 공동인증서 암호 입력(Fragment_KeyGuard)
 * ======================================================================
 * LSH_20170919    최초 등록
 * NJM_20190220    폐기된 인증서 알림메시지 표기(임시)
 * 1.5.2    NJM_20210322    [subUrl 공통파일로 변경]
 * 1.5.6    NJM_20210527    [인증오류코드 수정] ERRIUC0 -> ERRIUCO
 * =======================================================================
 * copyright : (주)프로니스정보기술 | http://www.phronis.co.kr
</pre> *
 */
class IUCOC0M00_Web : Activity_Auth(),  ObjectHandlerMessage {
    private val HANDLERJOB_CERT: Int = 0
    private val HANDLERJOB_ERROR_CERT: Int = 1
    private var isSmartReqPay: Boolean = false // 스마트보험금청구 여부

    private var fragmentCert : Fragment_Certificate? = null // 인증서 목록 Fragment
    private var fragmentKeyGuard : Fragment_KeyGuard? = null // 인증서 패스워드 입력 Fragment

    private var pager: CustomViewPager? = null
    private var selecteCertificateIndex: Int = 0

    private lateinit var sign: ByteArray // 인증서 서명 값
    private var vvid: String? = null // 인증서 검증 값

    private var tempKey : String? = null // Web 로그인 처리를 위한 값(Web에서 전달 받음)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        LogPrinter.CF_debug("!----------------------------------------------------------")
        LogPrinter.CF_debug("!-- IUCOC0M00_Web.onCreate()")
        LogPrinter.CF_debug("!----------------------------------------------------------")
        setIntentData()
        setFragments(savedInstanceState)
        val adapter = CustomPagerAdapter(getSupportFragmentManager(), arrayOf<Fragment?>(fragmentCert, fragmentKeyGuard))
        pager!!.setAdapter(adapter)
    }

    override fun onActivityResult(p_requestCode: Int, p_resultCode: Int, p_data: Intent?) {
        super.onActivityResult(p_requestCode, p_resultCode, p_data)
        if (p_requestCode == EnvConfig.REQUESTCODE_ACTIVITY_IUCOC2M00) {
            finish()
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        LogPrinter.CF_debug("!----------------------------------------------------------")
        LogPrinter.CF_debug("!-- onNewIntent.onCreate()")
        LogPrinter.CF_debug("!----------------------------------------------------------")
        if (intent != null) {
            setIntent(intent)
            setIntentData()
        }
    }

    override fun onSaveInstanceState(p_bundle: Bundle) {
        super.onSaveInstanceState(p_bundle)
        p_bundle.putInt("selecteCertificateIndex", selecteCertificateIndex)
        p_bundle.putInt("currentPageIndex", pager!!.getCurrentItem())
        getSupportFragmentManager().putFragment(p_bundle, Fragment_Certificate::class.java.getName(), (fragmentCert)!!)
        getSupportFragmentManager().putFragment(p_bundle, Fragment_KeyGuard::class.java.getName(), (fragmentKeyGuard)!!)
    }

    override fun onRestoreInstanceState(p_bundle: Bundle) {
        super.onRestoreInstanceState(p_bundle)
        if (p_bundle.containsKey("selecteCertificateIndex")) {
            selecteCertificateIndex = p_bundle.getInt("selecteCertificateIndex")
        }
        if (p_bundle.containsKey("currentPageIndex")) {
            val tmp_currentPageIndex: Int = p_bundle.getInt("currentPageIndex")
            showPage(tmp_currentPageIndex)
        }
    }

    override fun setInit() {
        setContentView(R.layout.iucoc0m00)
        tempKey = ""
        selecteCertificateIndex = -1
        handler = WeakReferenceHandler(this)
    }

    override fun setUIControl() {

        // 타이틀바 세팅
        setTitleBarUI()

        // ViewPager 세팅
        pager = findViewById(R.id.activity_login_cert_viewPager)
        pager?.CF_setPagingEnabled(false)
    }

    override fun onBackPressed() {
        // -----------------------------------------------------------------------------------------
        //  현재 페이지가 패스워드 입력 페이지인 경우 인증서 선택 화면으로 이동<br/>
        //  현재 페이지가 인증서 선택 화면인 경우 Activity 종료(Web 로그인 요청 취소 안내 후 동의 시 종료)
        // -----------------------------------------------------------------------------------------
        if (pager!!.getCurrentItem() > 0) {
            if (fragmentKeyGuard != null && fragmentKeyGuard!!.isAdded()) {
                fragmentKeyGuard!!.cancel(Intent())
            }
            selecteCertificateIndex = -1
            showPage(0)
        } else {

            // -------------------------------------------------------------------------------------
            //  웹 로그인 요청 취소 알림.
            // -------------------------------------------------------------------------------------
            val tmp_dlg = CustomDialog(this@IUCOC0M00_Web)
            tmp_dlg.show()
            tmp_dlg.CF_setTextContent(getResources().getString(R.string.dlg_cancel_iucoc0m00_web))
            tmp_dlg.CF_setDoubleButtonText(getResources().getString(R.string.btn_cancel), getResources().getString(R.string.btn_ok))
            tmp_dlg.setOnDismissListener(object : DialogInterface.OnDismissListener {
                override fun onDismiss(dialog: DialogInterface) {
                    if (!(dialog as CustomDialog).CF_getCanceled()) {
                        IntentManager.moveTaskToBack(this@IUCOC0M00_Web,true)
                        setResult(RESULT_CANCELED)
                        finish()
                    }
                }
            })
        }
    }

    /**
     * Intent 데이터 세팅 함수
     */
    private fun setIntentData() {
        LogPrinter.CF_debug("!----------------------------------------------------------")
        LogPrinter.CF_debug("!-- onNewIntent.setIntentData()")
        LogPrinter.CF_debug("!----------------------------------------------------------")
        if (getIntent() != null) {
            if (getIntent().hasExtra("key")) {
                tempKey = getIntent().getExtras()!!.getString("key")
                LogPrinter.CF_debug("!---- getIntent(tempKey)::::" + tempKey)
            }
        }
    }

    /**
     * Fragment 세팅
     * @param p_bundle  Bundle
     */
    private fun setFragments(p_bundle: Bundle?) {
        // Fragment 생성
        if (p_bundle != null) {
            fragmentCert = getSupportFragmentManager().getFragment(p_bundle, Fragment_Certificate::class.java.getName()) as Fragment_Certificate?
            fragmentKeyGuard = getSupportFragmentManager().getFragment(p_bundle, Fragment_KeyGuard::class.java.getName()) as Fragment_KeyGuard?
        } else {
            fragmentCert = Fragment.instantiate(this, Fragment_Certificate::class.java.getName()) as Fragment_Certificate?
            fragmentKeyGuard = Fragment.instantiate(this, Fragment_KeyGuard::class.java.getName()) as Fragment_KeyGuard?
        }

        // Fragment_Certificate(공동인증서 목록 Fragment) 이벤트 연결
        fragmentCert!!.CE_setOnFragmentCertificateEventListener(object : OnFragmentCertificateEventListener {
            override fun onSigned(p_sign: ByteArray, p_vid: String) {
                // ---------------------------------------------------------------------------------
                //  인증요청 대기시간 검사
                // ---------------------------------------------------------------------------------
                if ((System.currentTimeMillis() - SharedPreferencesFunc.getReqLoginTime(getApplicationContext())) <= EnvConfig.webToAppReqLoginTime) {
                    vvid = p_vid
                    sign = ByteArray(p_sign.size)
                    for (i in p_sign.indices) {
                        sign[i] = p_sign.get(i)
                    }
                    httpReq_Cert()
                } else {
                    CommonFunction.CF_showCustomAlertDilaog(this@IUCOC0M00_Web, getResources().getString(R.string.dlg_timeover_auth), getResources().getString(R.string.btn_ok))
                }
            }

            override fun onPasswordError() {
                CommonFunction.CF_showCustomAlertDilaog(this@IUCOC0M00_Web, getResources().getString(R.string.dlg_mismatch_pw), getResources().getString(R.string.btn_ok))
            }

            override fun onSelectedEvent(p_index: Int, p_flagIsExpire: Boolean) {
                if (p_flagIsExpire) {
                    CommonFunction.CF_showCustomAlertDilaog(this@IUCOC0M00_Web, getResources().getString(R.string.dlg_expire_certificate), getResources().getString(R.string.btn_ok))
                } else {
                    selecteCertificateIndex = p_index
                    showPage(1)
                    showKeyPad()
                }
            }

            override fun onGetList(p_count: Int) {
                if (p_count == 0) {
                    val tmp_dlg = CustomDialog(this@IUCOC0M00_Web)
                    tmp_dlg.show()
                    tmp_dlg.CF_setTextContent(getResources().getString(R.string.dlg_no_certificate))
                    tmp_dlg.CF_setSingleButtonText(getResources().getString(R.string.btn_ok))
                    tmp_dlg.setOnDismissListener(object : DialogInterface.OnDismissListener {
                        override fun onDismiss(dialog: DialogInterface) {
                            finish()
                        }
                    })
                }
            }
        })

        // Fragment_Keyguard(보안키패드 입력 Fragment)이벤트 연결
        fragmentKeyGuard!!.CE_setOnFragmentKeyGuardEventListener(object : OnFragmentKeyGuardEventListener {
            override fun onCancel() {
                pager!!.setCurrentItem(0)
            }

            override fun onDone(p_plainText: String) {
                if (fragmentCert != null && fragmentCert!!.isAdded()) {
                    fragmentCert!!.CF_requestSign(selecteCertificateIndex, p_plainText)
                }
            }
        })
    }

    /**
     * 타이틀바 레이아웃 세팅
     */
    private fun setTitleBarUI() {

        // 타이틀 세팅
        val tmp_title: TextView = findViewById(R.id.title_bar_textTitle)
        tmp_title.setText(getResources().getString(R.string.title_login_certificate))

        // left 버튼 세팅
        val tmp_btnLeft: ImageButton = findViewById(R.id.title_bar_imgBtnLeft)
        tmp_btnLeft.setVisibility(View.VISIBLE)
        tmp_btnLeft.setOnClickListener(object : View.OnClickListener {
            override fun onClick(v: View) {
                if (pager!!.getCurrentItem() > 0) {
                    if (fragmentKeyGuard != null && fragmentKeyGuard!!.isAdded()) {
                        fragmentKeyGuard!!.cancel(Intent())
                    }
                    selecteCertificateIndex = -1
                    showPage(0)
                } else {
                    // -------------------------------------------------------------------------------------
                    //  웹 로그인 요청 취소 알림.
                    // -------------------------------------------------------------------------------------
                    val tmp_dlg = CustomDialog(this@IUCOC0M00_Web)
                    tmp_dlg.show()
                    tmp_dlg.CF_setTextContent(getResources().getString(R.string.dlg_cancel_iucoc0m00_web))
                    tmp_dlg.CF_setDoubleButtonText(getResources().getString(R.string.btn_cancel), getResources().getString(R.string.btn_ok))
                    tmp_dlg.setOnDismissListener(object : DialogInterface.OnDismissListener {
                        override fun onDismiss(dialog: DialogInterface) {
                            if (!(dialog as CustomDialog).CF_getCanceled()) {
                                IntentManager.moveTaskToBack(this@IUCOC0M00_Web,true)
                                setResult(RESULT_CANCELED)
                                finish()
                            }
                        }
                    })
                }
            }
        })
    }

    /**
     * 해당 page show 함수
     * @param p_index   int
     */
    private fun showPage(p_index: Int) {
        pager!!.setCurrentItem(p_index)
    }

    /**
     * 보안 키패드 Fragment의 키패드 show 함수
     */
    private fun showKeyPad() {
        if (fragmentKeyGuard != null && fragmentKeyGuard!!.isAdded()) {
            if (!fragmentKeyGuard!!.CF_getFlagIsShwonKeypad()) {
                fragmentKeyGuard!!.CF_showTransKeyPad()
            }
        }
    }

    /**
     * 스마트보험 서비스 이용신청 Activity 호출 함수
     */
    private fun startSmartServiceAgreeActivity() {
        val tmp_dlg = CustomDialog(this)
        tmp_dlg.show()
        tmp_dlg.CF_setTextContent(getResources().getString(R.string.dlg_no_agree_smart_service))
        tmp_dlg.CF_setDoubleButtonText(getResources().getString(R.string.btn_no), getResources().getString(R.string.btn_yes))
        tmp_dlg.setOnDismissListener(object : DialogInterface.OnDismissListener {
            override fun onDismiss(dialog: DialogInterface) {
                if (!(dialog as CustomDialog).CF_getCanceled()) {
                    val tmp_intent = Intent(this@IUCOC0M00_Web, IUCOC2M00_Web::class.java)
                    tmp_intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
                    tmp_intent.putExtra("sign", sign)
                    tmp_intent.putExtra("vvid", vvid)
                    tmp_intent.putExtra("tempKey", tempKey)
                    startActivityForResult(tmp_intent, EnvConfig.REQUESTCODE_ACTIVITY_IUCOC2M00)
                } else {
                    finish()
                }
            }
        })
    }

    // #############################################################################################
    //  Http 관련
    // #############################################################################################
    override fun handleMessage(p_message: Message) {
        if (!isDestroyed()) {
            CF_dismissProgressDialog()
            when (p_message.what) {
                HANDLERJOB_CERT -> try {
                    httpResCert(JSONObject(p_message.obj as String?))
                } catch (e: JSONException) {
                    LogPrinter.CF_line()
                    LogPrinter.CF_debug(getResources().getString(R.string.log_json_exception))
                }
                HANDLERJOB_ERROR_CERT -> CommonFunction.CF_showCustomAlertDilaog(this, p_message.obj as String?, getResources().getString(R.string.btn_ok))
                else -> {
                }
            }
        }
    }

    /**
     * 공동인증 유효성 체크 및 공동인증 고겍체크 요청
     */
    private fun httpReq_Cert() {
        // 프로그레스 다이얼로그 Show
        CF_showProgressDialog()
        val tmp_builder: Uri.Builder = Uri.Builder()
        try {
            tmp_builder.appendQueryParameter("signedData", String(Base64Util.encode(sign)))
        } catch (e: IOException) {
            LogPrinter.CF_line()
            LogPrinter.CF_debug(getResources().getString(R.string.log_fail_encoding_base64))
        }
        tmp_builder.appendQueryParameter("rv", vvid)
        tmp_builder.appendQueryParameter("tempKey", tempKey)
        HttpConnections.sendPostData(
                EnvConfig.host_url + EnvConfig.URL_CERT_LOGIN_WEB_REQ,
                tmp_builder.build().getEncodedQuery(),
                handler,
                HANDLERJOB_CERT,
                HANDLERJOB_ERROR_CERT)
    }

    /**
     * 공동인증 유효성체크 & 공동인증 고객체크 요청 결과 처리 함수
     * @param p_jsonObject  JSONObject
     */
    @Throws(JSONException::class)
    private fun httpResCert(p_jsonObject: JSONObject) {
        CF_dismissProgressDialog()
        val jsonKey_errorCode = "errCode"
        val jsonKey_data = "data"
        val jsonKey_flagCert = "flagCert" // 공동인증유효여부
        val jsonKey_flagCust = "flagCust" // 고객여부(true csno있는상태)


        val jsonKey_csno = "csno" // 고객번호
        val jsonKey_name = "name" // 고객명
        //val jsonKey_flagInsuCust = "flagInsuCust"; // 보험청약고객여부
        //val jsonKey_flagRegCert = "flagRegCert" // 공동인증등록여부
        val jsonKey_tempKey = "tempKey"



        val tmp_errorCode: String

        if (p_jsonObject.has(jsonKey_errorCode)) {
            tmp_errorCode = p_jsonObject.getString(jsonKey_errorCode)
            if (!TextUtils.isEmpty(tmp_errorCode)) {
                // ---------------------------------------------------------------------------------
                // ERRIUCOC1M00001 : 요청데이터 누락
                // ERRIUCOC1M00002 : 인증서 파일 처리 오류
                // ERRIUCOC1M00003 : 인증서 시스템 장애
                // ERRIUCOC1M00004 : 인증서 검증 모듈 오류
                // ERRIUCOC1M00005 : 인증서DN추출 실패(미사용)
                // ERRIUCOC1M00006 : 스마트보험 이용 고객 여부 조회 실패(미사용)
                // ERRIUCOC1M00007 : 전자금융고객여부 조회 실패(미사용)
                // ERRIUCOC1M00008 : 청약고객여부 조회 실패(미사용)
                // ERRIUCOC1M00009 : 개인식별번호 조회 실패(미사용)
                // ERRIUCOC1M00010 : 인증서 검증 실패(미사용)
                // ERRIUCOC1M00091 : 폐기된 인증서 {ER91}
                // ---------------------------------------------------------------------------------
                CF_dismissProgressDialog()
                var errMsg: String? = "[" + tmp_errorCode + "] " + getResources().getString(R.string.dlg_error_erriucoc1m00001)
                if (("ERRIUCOC1M00091" == tmp_errorCode)) {
                    errMsg = getResources().getString(R.string.dlg_error_cert_91)
                }
                CommonFunction.CF_showCustomAlertDilaog(this, errMsg, getResources().getString(R.string.btn_ok))
            } else if (p_jsonObject.has(jsonKey_data)) {
                val tmp_jsonData: JSONObject = p_jsonObject.getJSONObject(jsonKey_data)
                val tmp_flagCert: Boolean = tmp_jsonData.getBoolean(jsonKey_flagCert) // 공동인증발급기관에서의 전자서명값 체크 여부(if false : 유효하지 않은 공동인증서)
                val tmp_flagCust: Boolean = tmp_jsonData.getBoolean(jsonKey_flagCust) // 모바일슈랑스 고객 DB에 공동인증 정보 저장 유무
                if (!tmp_flagCust) {
                    startSmartServiceAgreeActivity()
                } else if (!tmp_flagCert) {
                    CommonFunction.CF_showCustomAlertDilaog(this, getResources().getString(R.string.dlg_error_erriucoc1m00002), getResources().getString(R.string.btn_ok))
                } else {

                    val tmpCsno: String = tmp_jsonData.getString(jsonKey_csno) // 고객 csno
                    val tmpName: String = tmp_jsonData.getString(jsonKey_name) // 고객명
                    // -----------------------------------------------------------------------------
                    // -- 최종 로그인 처리
                    // -----------------------------------------------------------------------------
                    CF_setLogin(true,  EnvConfig.AuthDvsn.CERT, tmpCsno, tmpName, p_jsonObject.optString(jsonKey_tempKey) )
                    startIUCOC3M00()
                    finish()
                }
            } else {
                CommonFunction.CF_showCustomAlertDilaog(this, getResources().getString(R.string.dlg_error_server_2), getResources().getString(R.string.btn_ok))
            }
        } else {
            CommonFunction.CF_showCustomAlertDilaog(this, getResources().getString(R.string.dlg_error_server_1), getResources().getString(R.string.btn_ok))
        }
    }

    /**
     * 공동인증 로그인 완료 Activity 호출 함수
     */
    private fun startIUCOC3M00() {
        IntentManager.loginCompleteToMain(this@IUCOC0M00_Web)
    }


    /**
     * 스마트보험금청구 요청 처리<br></br>
     */
    private fun runSmartReqPay() {
        LogPrinter.CF_debug("!----------------------------------------------------------")
        LogPrinter.CF_debug("!-- IUCOC0M00.runSmartReqPay()")
        LogPrinter.CF_debug("!----------------------------------------------------------")

        val intent = Intent(this, IUBC10M00_P::class.java)
        startActivity(intent)

    }

}