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
 * 인증 > 공동인증서 전자서명(APP 요청)
 * @since     :
 * @version   : 1.3
 * @author    : LSH
 * <pre>
 * 공동인증서를 통한 본인인증 처리한다(본거래 전 1회성처리)
 * Fragment 01. 공동인증서 선택 [Fragment_Certificate]
 * 02. 공동인증서 암호 입력 ([Fragment_KeyGuard])
 * ======================================================================
 * LSH_20170922    최초 등록
 * NJM_20190220    폐기된 인증서 알림메시지 표기(임시)
 * 1.5.2    NJM_20210322    [subUrl 공통파일로 변경]
 * 1.5.4    NJM_20210506    [IUFC34M00 임시고객번호 삭제]
 * =======================================================================
 * copyright : (주)프로니스정보기술 | http://www.phronis.co.kr
</pre> *
 */
class IUPC80M00 : Activity_Default(), ObjectHandlerMessage {
    private val HANDLERJOB_CERT: Int = 0
    private val HANDLERJOB_ERROR_CERT: Int = 1

    private var fragmentCert: Fragment_Certificate? = null // 공동인증서 목록 Fragment
    private var fragmentKeyGuard: Fragment_KeyGuard? = null // 인증서 패스워드입력 Fragment

    private var adapter: CustomPagerAdapter? = null
    private lateinit var pager: CustomViewPager
    private var selectedCertIndex: Int = 0
    private var plainText: String? = null

    private var sign: ByteArray? = null // 인증서 서명 값
    private var vvid: String? = null // 인증서 검증 값

    private var mAuthCsno: String? = null // 인증요청자 csno

    override fun onActivityResult(p_requestCode: Int, p_resultCode: Int, p_data: Intent?) {
        super.onActivityResult(p_requestCode, p_resultCode, p_data)
        LogPrinter.CF_debug("!-----------------------------------------------------------")
        LogPrinter.CF_debug("!-- IUPC80M00.onActivityResult() --")
        LogPrinter.CF_debug("!-----------------------------------------------------------")
        if (p_requestCode == EnvConfig.REQUESTCODE_ACTIVITY_IUCOC2M00 && p_resultCode == RESULT_OK) {
            finish()
        }
    }

    override fun onSaveInstanceState(p_bundle: Bundle) {
        super.onSaveInstanceState(p_bundle)
        p_bundle.putInt("selecteCertificateIndex", selectedCertIndex)
        p_bundle.putInt("currentPageIndex", pager!!.getCurrentItem())
        getSupportFragmentManager().putFragment(p_bundle, Fragment_Certificate::class.java.getName(), (fragmentCert)!!)
        getSupportFragmentManager().putFragment(p_bundle, Fragment_KeyGuard::class.java.getName(), (fragmentKeyGuard)!!)
    }

    override fun onRestoreInstanceState(p_bundle: Bundle) {
        super.onRestoreInstanceState(p_bundle)
        if (p_bundle.containsKey("selecteCertificateIndex")) {
            selectedCertIndex = p_bundle.getInt("selecteCertificateIndex")
        }
        if (p_bundle.containsKey("currentPageIndex")) {
            val tmp_currentPageIndex: Int = p_bundle.getInt("currentPageIndex")
            showPage(tmp_currentPageIndex)
        }
    }

    override fun onBackPressed() {
        if (pager!!.getCurrentItem() > 0) {
            if (fragmentKeyGuard != null && fragmentKeyGuard!!.isAdded()) {
                fragmentKeyGuard!!.cancel(Intent())
            }
            selectedCertIndex = -1
            showPage(0)
        } else {
            super.onBackPressed()
        }
    }

    override fun setInit() {
        setContentView(R.layout.iucoc0m00)
        handler = WeakReferenceHandler(this)
        selectedCertIndex = -1
        if (getIntent() != null) {
            if (getIntent().hasExtra(EnvConstant.KEY_INTENT_AUTH_CSNO)) { // 고객번호
                mAuthCsno = getIntent().getExtras()!!.getString(EnvConstant.KEY_INTENT_AUTH_CSNO)
            }
        }

        // -- (고객번호 없음) 비정상 요청 종료처리
        if (mAuthCsno == null) {
            setResult(RESULT_CANCELED)
            finish()
        }
    }

    override fun setUIControl() {
        // 타이틀바 세팅
        setTitleBarUI()

        // ViewPager 세팅
        pager = findViewById(R.id.activity_login_cert_viewPager)
        pager.setAdapter(adapter)
        pager.CF_setPagingEnabled(false)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setFragments(savedInstanceState)
        adapter = CustomPagerAdapter(getSupportFragmentManager(), arrayOf<Fragment?>(fragmentCert, fragmentKeyGuard))
        pager!!.setAdapter(adapter)
    }

    /**
     * Fragment 세팅
     * @param p_bundle  Bundle
     */
    private fun setFragments(p_bundle: Bundle?) {
        if (p_bundle != null) {
            fragmentCert = getSupportFragmentManager().getFragment(p_bundle, Fragment_Certificate::class.java.getName()) as Fragment_Certificate?
            fragmentKeyGuard = getSupportFragmentManager().getFragment(p_bundle, Fragment_KeyGuard::class.java.getName()) as Fragment_KeyGuard?
        } else {
            // 로그인한 사용자 이름 세팅
            val tmp_bundle: Bundle = Bundle()
            //tmp_bundle.putString("name", getUser_name_fromSQlite());
            fragmentCert = Fragment.instantiate(this, Fragment_Certificate::class.java.getName(), tmp_bundle) as Fragment_Certificate?
            fragmentKeyGuard = Fragment.instantiate(this, Fragment_KeyGuard::class.java.getName()) as Fragment_KeyGuard?
        }
        fragmentCert!!.CE_setOnFragmentCertificateEventListener(object : OnFragmentCertificateEventListener {
            override fun onSigned(p_sign: ByteArray, p_vid: String) {
                vvid = p_vid
                sign = ByteArray(p_sign.size)
                //                for(int i = 0 ; i < p_sign.length; i++){
//                    sign[i] = p_sign[i];
//                }
                System.arraycopy(p_sign, 0, sign, 0, p_sign.size)
                httpReq_cert()
            }

            override fun onPasswordError() {
                CommonFunction.CF_showCustomAlertDilaog(this@IUPC80M00, getResources().getString(R.string.dlg_mismatch_pw), getResources().getString(R.string.btn_ok))
            }

            override fun onSelectedEvent(p_index: Int, p_flagIsExpire: Boolean) {
                if (p_flagIsExpire) {
                    CommonFunction.CF_showCustomAlertDilaog(this@IUPC80M00, getResources().getString(R.string.dlg_expire_certificate), getResources().getString(R.string.btn_ok))
                } else {
                    selectedCertIndex = p_index
                    showPage(1)
                    showKeyPad()
                }
            }

            override fun onGetList(p_count: Int) {
                if (p_count == 0) {
                    val tmp_dlg: CustomDialog = CustomDialog(this@IUPC80M00)
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
        fragmentKeyGuard!!.CE_setOnFragmentKeyGuardEventListener(object : OnFragmentKeyGuardEventListener {
            override fun onCancel() {
                pager!!.setCurrentItem(0)
            }

            override fun onDone(p_plainText: String) {
                if (fragmentCert != null && fragmentCert!!.isAdded()) {
                    fragmentCert!!.CF_requestSign(selectedCertIndex, p_plainText)
                    plainText = p_plainText
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
                    selectedCertIndex = -1
                    showPage(0)
                } else {
                    finish()
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

    // #############################################################################################
    //  Http 관련
    // #############################################################################################
    override fun handleMessage(p_message: Message) {
        if (!isDestroyed()) {
            CF_dismissProgressDialog()
            when (p_message.what) {
                HANDLERJOB_CERT -> try {
                    httpRes_cert(JSONObject(p_message.obj as String?))
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
     * 공동인증 유효성 체크 및 고객체크 요청
     */
    private fun httpReq_cert() {
        pager!!.setCurrentItem(0)

        // 프로그레스 다이얼로그 Show
        CF_showProgressDialog()
        val tmp_builder: Uri.Builder = Uri.Builder()
        try {
            tmp_builder.appendQueryParameter("signedData", String(Base64Util.encode(sign)))
            tmp_builder.appendQueryParameter("rv", vvid)
            tmp_builder.appendQueryParameter("csno", mAuthCsno)
            HttpConnections.sendPostData(
                    EnvConfig.host_url + EnvConfig.URL_CERT_SIGN_APP_REQ,
                    tmp_builder.build().getEncodedQuery(),
                    handler,
                    HANDLERJOB_CERT,
                    HANDLERJOB_ERROR_CERT)
        } catch (e: IOException) {
            LogPrinter.CF_line()
            LogPrinter.CF_debug(getResources().getString(R.string.log_fail_encoding_base64))
        }
    }

    /**
     * 공동인증 유효성체크 & 고객체크 요청 결과 처리 함수
     * @param p_jsonObject  JSONObject
     */
    @Throws(JSONException::class)
    private fun httpRes_cert(p_jsonObject: JSONObject) {
        val jsonKey_errorCode: String = "errCode"
        val jsonKey_flagOk: String = "flagOk"
        val tmp_errorCode: String
        if (p_jsonObject.has(jsonKey_errorCode)) {
            tmp_errorCode = p_jsonObject.getString(jsonKey_errorCode)
            if (!TextUtils.isEmpty(tmp_errorCode)) {
                // ---------------------------------------------------------------------------------
                // ERRIUPC81M0001 : 요청데이터 누락
                // ERRIUPC81M0002 : 인증서 파일 처리 오류
                // ERRIUPC81M0003 : 인증시스템 장애
                // ERRIUPC81M0004 : 인증서 검증 모듈 오류
                // ERRIUPC81M0005 : 인증서DN추출 실패(미사용)
                // ERRIUPC81M0006 : 청약고객여부 조회 실패(미사용)
                // ERRIUPC81M0007 : 개인식별번호 조회 실패(미사용)
                // ERRIUPC81M0008 : 인증서 검증 실패(미사용)
                // ERRIUPC81M0091 : 폐기된 인증서 {ER91}
                // ---------------------------------------------------------------------------------
                var errMsg: String? = "[" + tmp_errorCode + "] " + getResources().getString(R.string.dlg_error_erriupc81m00001)
                if (("ERRIUPC81M0091" == tmp_errorCode)) {
                    errMsg = getResources().getString(R.string.dlg_error_cert_91)
                }
                CommonFunction.CF_showCustomAlertDilaog(this, errMsg, getResources().getString(R.string.btn_ok))
            } else if (p_jsonObject.has(jsonKey_flagOk)) {
                val tmp_flagOk: Boolean = p_jsonObject.getBoolean(jsonKey_flagOk)
                if (tmp_flagOk) {
                    val tmp_dlg: CustomDialog = CustomDialog(this)
                    tmp_dlg.show()
                    tmp_dlg.CF_setBackKeyMode(CustomDialog.BackMode.NOTUSE)
                    tmp_dlg.CF_setTextContent(getResources().getString(R.string.dlg_success_auth_cert))
                    tmp_dlg.CF_setSingleButtonText(getResources().getString(R.string.btn_ok))
                    tmp_dlg.setOnDismissListener(object : DialogInterface.OnDismissListener {
                        override fun onDismiss(dialog: DialogInterface) {
                            val data: Intent = Intent()
                            data.putExtra("certificateIndex", selectedCertIndex)
                            data.putExtra("plainText", plainText)
                            setResult(RESULT_OK, data)
                            finish()
                        }
                    })
                } else {
                    CommonFunction.CF_showCustomAlertDilaog(this, getResources().getString(R.string.dlg_error_erriupc81m00001), getResources().getString(R.string.btn_ok))
                }
            } else {
                CommonFunction.CF_showCustomAlertDilaog(this, getResources().getString(R.string.dlg_error_server_2), getResources().getString(R.string.btn_ok))
            }
        } else {
            CommonFunction.CF_showCustomAlertDilaog(this, getResources().getString(R.string.dlg_error_server_1), getResources().getString(R.string.btn_ok))
        }
    }
}