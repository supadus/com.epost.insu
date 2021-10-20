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
import androidx.viewpager.widget.ViewPager.OnPageChangeListener
import com.epost.insu.*
import com.epost.insu.EnvConfig.AuthDvsn
import com.epost.insu.activity.BC.IUBC10M00_P
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
import java.util.*

/**
 * 인증 > 공동인증서 로그인
 * @since     :
 * @version   : 1.4
 * @author    : LSH
 * @see
 * <pre>
 *  - 공동인증서를 통해 로그인한다.
 *  - 공동인증 성공시 DB에 을 저장한다. (이름, 고객번호, 로그인타입)
 *  - 공동인증 성공시 SharedPreferences에 로그인 정보를 저장한다. (로그아웃시 삭제함)
 *  - Fragment 01. 공동인증서 선택(Fragment_Certificate)
 *  02. 공동인증서 암호 입력(Fragment_KeyGuard)
 * ======================================================================
 * 0.0.0    LSH_20170915    최초 등록
 * 0.0.0    LKM_20180814    스마트보험금청구 구현 추가
 * 0.0.0    NJM_20191122    공동인증 후 알림팝업 안뜨는 문제 수정
 * 0.0.0    NJM_20190220    폐기된 인증서 알림메시지 표기(임시)
 * 1.5.2    NJM_20210322    [subUrl 공통파일로 변경]
 * 1.5.4    NJM_20210429    [카카오페이앱 인증 S320] 인증정보 저장방식 일부 변경
 * 1.5.6    NJM_20210527    [인증오류코드 수정] ERRIUC0 -> ERRIUCO
 * 1.5.6    NJM_20210601    [push업데이트 삭제] 모슈DB push id 업데이트 호출 삭제(유라클 사용함)
 * 1.5.8    NJM_20210630    [로그인처리 공통화] CF_setLogin() 공통 로그인 처리 함수
 * =======================================================================
 * copyright : (주)프로니스정보기술 | http://www.phronis.co.kr
 * </pre>
 */
class IUCOC0M00 : Activity_Auth(), ObjectHandlerMessage {
    private val HANDLERJOB_CERT: Int = 0
    private val HANDLERJOB_ERROR_CERT: Int = 1

    private var fragmentCert: Fragment_Certificate? = null // 인증서 목록 Fragment
    private var fragmentKeyGuard: Fragment_KeyGuard? = null // 인증서 패스워드 입력 Fragment

    private var pager: CustomViewPager? = null

    private var selecteCertificateIndex: Int = 0

    private var sign: ByteArray? = null // 인증서 서명 값
    private var vvid: String? = null // 인증서 검증 값
    private var isSmartReqPay: Boolean = false // 스마트보험금청구 여부

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        LogPrinter.CF_debug("!----------------------------------------------------------")
        LogPrinter.CF_debug("!-- IUCOC0M00.onCreate()")
        LogPrinter.CF_debug("!----------------------------------------------------------")

        setIntentData()
        setFragments(savedInstanceState)
        val adapter = CustomPagerAdapter(supportFragmentManager, arrayOf(fragmentCert, fragmentKeyGuard))
        pager!!.adapter = adapter
    }

    override fun onActivityResult(p_requestCode: Int, p_resultCode: Int, p_data: Intent?) {
        super.onActivityResult(p_requestCode, p_resultCode, p_data)
        LogPrinter.CF_debug("!----------------------------------------------------------")
        LogPrinter.CF_debug("!-- IUCOC0M00.onActivityResult()")
        LogPrinter.CF_debug("!----------------------------------------------------------")

        if (p_requestCode == EnvConfig.REQUESTCODE_ACTIVITY_IUCOC2M00) {
            if (p_resultCode == RESULT_OK) {
                setResult(RESULT_OK)
            }
            finish()
        }
    }

    override fun onSaveInstanceState(p_bundle: Bundle) {
        super.onSaveInstanceState(p_bundle)
        LogPrinter.CF_debug("!----------------------------------------------------------")
        LogPrinter.CF_debug("!-- IUCOC0M00.onSaveInstanceState()")
        LogPrinter.CF_debug("!----------------------------------------------------------")

        p_bundle.putInt("selecteCertificateIndex", selecteCertificateIndex)
        p_bundle.putInt("currentPageIndex", pager!!.currentItem)
        supportFragmentManager.putFragment(p_bundle, Fragment_Certificate::class.java.name, (fragmentCert)!!)
        supportFragmentManager.putFragment(p_bundle, Fragment_KeyGuard::class.java.name, (fragmentKeyGuard)!!)
    }

    override fun onRestoreInstanceState(p_bundle: Bundle) {
        super.onRestoreInstanceState(p_bundle)
        LogPrinter.CF_debug("!----------------------------------------------------------")
        LogPrinter.CF_debug("!-- IUCOC0M00.onRestoreInstanceState()")
        LogPrinter.CF_debug("!----------------------------------------------------------")

        if (p_bundle.containsKey("selecteCertificateIndex")) {
            selecteCertificateIndex = p_bundle.getInt("selecteCertificateIndex")
        }
        if (p_bundle.containsKey("currentPageIndex")) {
            val curPageIdx: Int = p_bundle.getInt("currentPageIndex")
            showPage(curPageIdx)
        }
    }

    override fun setInit() {
        LogPrinter.CF_debug("!----------------------------------------------------------")
        LogPrinter.CF_debug("!-- IUCOC0M00.setInit()")
        LogPrinter.CF_debug("!----------------------------------------------------------")

        setContentView(R.layout.iucoc0m00)
        isSmartReqPay = false
        selecteCertificateIndex = -1
        handler = WeakReferenceHandler(this)
    }

    /**
     * Intent 데이터 세팅 함수
     */
    private fun setIntentData() {
        LogPrinter.CF_debug("!----------------------------------------------------------")
        LogPrinter.CF_debug("!-- IUCOC0M00.setIntentData()")
        LogPrinter.CF_debug("!----------------------------------------------------------")

        if (intent != null) {
            if (intent.hasExtra("isSmartReqPay")) {
                isSmartReqPay = intent.extras!!.getBoolean("isSmartReqPay")
            }
        }
    }

    override fun setUIControl() {
        LogPrinter.CF_debug("!----------------------------------------------------------")
        LogPrinter.CF_debug("!-- IUCOC0M00.setUIControl()")
        LogPrinter.CF_debug("!----------------------------------------------------------")

        // 타이틀바 세팅
        setTitleBarUI()

        // ViewPager 세팅
        pager = findViewById(R.id.activity_login_cert_viewPager)
        pager!!.CF_setPagingEnabled(false)
        pager!!.addOnPageChangeListener(object : OnPageChangeListener {
            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {}
            override fun onPageSelected(position: Int) {
                var label: String? = resources.getString(R.string.label_iucoc0m00_1)
                if (position == 1) {
                    label = resources.getString(R.string.label_iucoc0m00_2)
                }
                this@IUCOC0M00.title = label
            }

            override fun onPageScrollStateChanged(state: Int) {}
        })
    }

    override fun onBackPressed() {
        // -----------------------------------------------------------------------------------------
        //  현재 페이지가 패스워드 입력 페이지인 경우 인증서 선택 화면으로 이동<br/>
        //  현재 페이지가 인증서 선택 화면인 경우 Activity 종료
        // -----------------------------------------------------------------------------------------
        if (pager!!.currentItem > 0) {
            if (fragmentKeyGuard != null && fragmentKeyGuard!!.isAdded) {
                fragmentKeyGuard!!.cancel(Intent())
            }
            selecteCertificateIndex = -1
            showPage(0)
        } else {
            super.onBackPressed()
        }
    }

    /**
     * Fragment 세팅
     * @param p_bundle  Bundle
     */
    private fun setFragments(p_bundle: Bundle?) {
        LogPrinter.CF_debug("!----------------------------------------------------------")
        LogPrinter.CF_debug("!-- IUCOC0M00.setFragments()")
        LogPrinter.CF_debug("!----------------------------------------------------------")

        if (p_bundle != null) {
            fragmentCert = supportFragmentManager.getFragment(p_bundle, Fragment_Certificate::class.java.name) as Fragment_Certificate?
            fragmentKeyGuard = supportFragmentManager.getFragment(p_bundle, Fragment_KeyGuard::class.java.name) as Fragment_KeyGuard?
        } else {
            fragmentCert = Fragment.instantiate(this, Fragment_Certificate::class.java.name) as Fragment_Certificate?
            fragmentKeyGuard = Fragment.instantiate(this, Fragment_KeyGuard::class.java.name) as Fragment_KeyGuard?
        }

        // 공동인증서 목록 리스너 설정
        fragmentCert!!.CE_setOnFragmentCertificateEventListener(object : OnFragmentCertificateEventListener {
            override fun onSigned(p_sign: ByteArray, p_vid: String) {
                vvid = p_vid
                sign = ByteArray(p_sign.size)
                System.arraycopy(p_sign, 0, sign, 0, p_sign.size)
                httpReq_Cert()
            }

            override fun onPasswordError() {
                // ---------------------------------------------------------------------------------
                //  공동인증서 패스워드 오류 : 다이얼로그 팝업 후 보안 키패드 띄움.
                // ---------------------------------------------------------------------------------
                val customDialog = CustomDialog(this@IUCOC0M00)
                customDialog.show()
                customDialog.CF_setBackKeyMode(CustomDialog.BackMode.NOTUSE)
                customDialog.CF_setTextContent(resources.getString(R.string.dlg_mismatch_pw))
                customDialog.CF_setSingleButtonText(resources.getString(R.string.btn_ok))
                customDialog.setOnDismissListener {
                    fragmentKeyGuard!!.CF_showTransKeyPad()
                    if (CommonFunction.CF_checkAccessibilityTurnOn(this@IUCOC0M00)) {
                        fragmentKeyGuard!!.CF_setAccessibleFocusInputBox()
                    }
                }
            }

            override fun onSelectedEvent(p_index: Int, p_flagIsExpire: Boolean) {
                if (p_flagIsExpire) {
                    CommonFunction.CF_showCustomAlertDilaog(this@IUCOC0M00, resources.getString(R.string.dlg_expire_certificate), resources.getString(R.string.btn_ok))
                } else {
                    selecteCertificateIndex = p_index
                    showPage(1)
                    showKeyPad()
                }
            }

            override fun onGetList(p_count: Int) {
                if (p_count == 0) {
                    val customDialog = CustomDialog(this@IUCOC0M00)
                    customDialog.show()
                    customDialog.CF_setTextContent(resources.getString(R.string.dlg_no_certificate))
                    customDialog.CF_setSingleButtonText(resources.getString(R.string.btn_ok))
                    customDialog.setOnDismissListener { finish() }
                }
            }
        })

        // 공동인증서 세부화면 리스너 설정
        fragmentKeyGuard!!.CE_setOnFragmentKeyGuardEventListener(object : OnFragmentKeyGuardEventListener {
            override fun onCancel() {
                pager!!.currentItem = 0
            }

            override fun onDone(p_plainText: String) {
                if (fragmentCert != null && fragmentCert!!.isAdded) {
                    fragmentCert!!.CF_requestSign(selecteCertificateIndex, p_plainText)
                }
            }
        })
    }

    /**
     * 타이틀바 레이아웃 세팅
     */
    private fun setTitleBarUI() {
        LogPrinter.CF_debug("!----------------------------------------------------------")
        LogPrinter.CF_debug("!-- IUCOC0M00.setTitleBarUI()")
        LogPrinter.CF_debug("!----------------------------------------------------------")

        // 타이틀 세팅
        val txtTitle: TextView = findViewById(R.id.title_bar_textTitle)
        txtTitle.text = resources.getString(R.string.title_login_certificate)

        // left 버튼 세팅
        val btnLeft: ImageButton = findViewById(R.id.title_bar_imgBtnLeft)
        btnLeft.visibility = View.VISIBLE
        btnLeft.setOnClickListener {
            if (pager!!.currentItem > 0) {
                if (fragmentKeyGuard != null && fragmentKeyGuard!!.isAdded) {
                    fragmentKeyGuard!!.cancel(Intent())
                }
                selecteCertificateIndex = -1
                showPage(0)
            } else {
                finish()
            }
        }
    }

    /**
     * 해당 page show 함수
     * @param p_index   int
     */
    private fun showPage(p_index: Int) {
        pager!!.currentItem = p_index
    }

    /**
     * 보안 키패드 Fragment의 키패드 show 함수
     */
    private fun showKeyPad() {
        if (fragmentKeyGuard != null && fragmentKeyGuard!!.isAdded) {
            if (!fragmentKeyGuard!!.CF_getFlagIsShwonKeypad()) {
                fragmentKeyGuard!!.CF_showTransKeyPad()
            }
        }
    }

    /**
     * 스마트보험 서비스 이용신청 Activity 호출 함수
     */
    private fun startSmartServiceAgreeActivity() {
        LogPrinter.CF_debug("!----------------------------------------------------------")
        LogPrinter.CF_debug("!-- IUCOC0M00.startSmartServiceAgreeActivity()")
        LogPrinter.CF_debug("!----------------------------------------------------------")

        val customDialog = CustomDialog(this)
        customDialog.show()
        customDialog.CF_setTextContent(resources.getString(R.string.dlg_no_agree_smart_service))
        customDialog.CF_setDoubleButtonText(resources.getString(R.string.btn_no), resources.getString(R.string.btn_yes))
        customDialog.setOnDismissListener { dialog: DialogInterface ->
            if (!(dialog as CustomDialog).CF_getCanceled()) {
                val intent = Intent(this@IUCOC0M00, IUCOC2M00::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
                intent.putExtra("sign", sign)
                intent.putExtra("vvid", vvid)
                startActivityForResult(intent, EnvConfig.REQUESTCODE_ACTIVITY_IUCOC2M00)
            } else {
                finish()
            }
        }
    }

    // #############################################################################################
    //  Http 관련
    // #############################################################################################
    override fun handleMessage(p_message: Message) {
        LogPrinter.CF_debug("!----------------------------------------------------------")
        LogPrinter.CF_debug("!-- IUCOC0M00.handleMessage()")
        LogPrinter.CF_debug("!----------------------------------------------------------")

        if (!isDestroyed) {
            when (p_message.what) {
                HANDLERJOB_CERT -> try {
                    httpRes_Cert(JSONObject(p_message.obj as String?))
                } catch (e: JSONException) {
                    LogPrinter.CF_line()
                    LogPrinter.CF_debug(resources.getString(R.string.log_json_exception))
                }
                HANDLERJOB_ERROR_CERT -> {
                    CF_dismissProgressDialog()
                    CommonFunction.CF_showCustomAlertDilaog(this, p_message.obj as String?, resources.getString(R.string.btn_ok))
                }
                else -> {
                }
            }
        }
    }

    /**
     * 공동인증 유효성 체크 및 공동인증 고객체크 요청
     */
    private fun httpReq_Cert() {
        LogPrinter.CF_debug("!----------------------------------------------------------")
        LogPrinter.CF_debug("!-- IUCOC0M00.requestCert()")
        LogPrinter.CF_debug("!----------------------------------------------------------")

        pager!!.currentItem = 0

        // 프로그레스 다이얼로그 Show
        CF_showProgressDialog()
        val builder: Uri.Builder = Uri.Builder()
        try {
            builder.appendQueryParameter("signedData", String(Base64Util.encode(sign)))
        } catch (e: IOException) {
            LogPrinter.CF_line()
            LogPrinter.CF_debug(resources.getString(R.string.log_fail_encoding_base64))
        }
        builder.appendQueryParameter("rv", vvid)
        HttpConnections.sendPostData(
            EnvConfig.host_url + EnvConfig.URL_CERT_LOGIN_APP_REQ,
            builder.build().encodedQuery,
            handler,
            HANDLERJOB_CERT,
            HANDLERJOB_ERROR_CERT)
    }

    /**
     * 공동인증 유효성체크 & 공동인증 고객체크 요청 결과 처리 함수
     * @param p_jsonObject  JSONObject
     */
    @Throws(JSONException::class)
    private fun httpRes_Cert(p_jsonObject: JSONObject) {
        LogPrinter.CF_debug("!----------------------------------------------------------")
        LogPrinter.CF_debug("!-- IUCOC0M00.setResultOfCert()")
        LogPrinter.CF_debug("!----------------------------------------------------------")

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

        // --<1> (성공)
        if (p_jsonObject.has(jsonKey_errorCode)) {
            tmp_errorCode = p_jsonObject.getString(jsonKey_errorCode)

            // --<2> (에러)
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
                var errMsg: String? = "[" + tmp_errorCode + "] " + resources.getString(R.string.dlg_error_erriucoc1m00001)
                if (("ERRIUCOC1M00091" == tmp_errorCode)) {
                    errMsg = resources.getString(R.string.dlg_error_cert_91)
                }
                CommonFunction.CF_showCustomAlertDilaog(this, errMsg, resources.getString(R.string.btn_ok))
            } else if (p_jsonObject.has(jsonKey_data)) {
                val tmp_jsonData: JSONObject = p_jsonObject.getJSONObject(jsonKey_data)
                val tmp_flagCust: Boolean = tmp_jsonData.getBoolean(jsonKey_flagCust) // 모바일슈랑스 고객 DB에 공동인증 정보 저장 유무
                val tmp_flagCert: Boolean = tmp_jsonData.getBoolean(jsonKey_flagCert) // 공동인증발급기관에서의 전자서명값 체크 여부(if false : 유효하지 않은 공동인증서)

                // --<3> (실패) 등록되지 않는 인증서
                if (!tmp_flagCust) {
                    CF_dismissProgressDialog()
                    startSmartServiceAgreeActivity()
                }
                else if (!tmp_flagCert) {
                    CF_dismissProgressDialog()
                    CommonFunction.CF_showCustomAlertDilaog(this, resources.getString(R.string.dlg_error_erriucoc1m00002), resources.getString(R.string.btn_ok))
                }
                // --<3> (최종완료)
                else {
                    val tmpCsno: String = tmp_jsonData.getString(jsonKey_csno) // 고객 csno
                    val tmpName: String = tmp_jsonData.getString(jsonKey_name) // 고객명

                    // -----------------------------------------------------------------------------
                    // -- 최종 로그인 처리
                    // -----------------------------------------------------------------------------
                    CF_setLogin(true,  AuthDvsn.CERT, tmpCsno, tmpName, p_jsonObject.optString(jsonKey_tempKey) )

                    // TODO 스마트청구 처리 방안 결정
                    // 스마트보험금청구 요청처리
                    if (isSmartReqPay) {
                        runSmartReqPay()
                    }
                    setResult(RESULT_OK)
                    finish()
                }
            } else {
                CF_dismissProgressDialog()
                CommonFunction.CF_showCustomAlertDilaog(this, resources.getString(R.string.dlg_error_server_2), resources.getString(R.string.btn_ok))
            }
        } else {
            CF_dismissProgressDialog()
            CommonFunction.CF_showCustomAlertDilaog(this, resources.getString(R.string.dlg_error_server_1), resources.getString(R.string.btn_ok))
        }
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
        setResult(RESULT_OK)
        finish()
    }
}