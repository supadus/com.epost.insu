package com.epost.insu.activity.BC

import android.content.Intent
import android.os.Bundle
import android.util.Log
import com.epost.insu.EnvConfig
import com.epost.insu.R
import com.epost.insu.SharedPreferencesFunc
import com.epost.insu.activity.Activity_Default
import com.epost.insu.common.WebBrowserHelper
import com.epost.insu.service.AES256Util
import java.io.UnsupportedEncodingException
import java.net.URLEncoder
import java.security.InvalidAlgorithmParameterException
import java.security.InvalidKeyException
import java.security.NoSuchAlgorithmException
import java.util.*
import javax.crypto.BadPaddingException
import javax.crypto.IllegalBlockSizeException
import javax.crypto.NoSuchPaddingException

/**
 * @copyright : 우정사업정보센터
 *
 * @project   : 모바일슈랑스 구축
 * @pakage   : com.epost.insu.activity
 * @fileName  : IUBC11M00.java
 *
 * @Title     : 보험금청구 > 보험금청구 (Class name : IUBC11M00)
 * @author    : 이경민
 * @created   : 2018-06-16
 * @version   : 1.0
 *
 * @note      : <u>보험금청구 > 보험금청구 (Class name : IUBC11M00) </u><br></br>
 * 고객ID 전송, 병원통합시스템 웹페이지 송/수신 관리 클래스
 * ======================================================================
 * 수정 내역
 * NO      날짜          작업자       내용
 * 01      2018-06-16    이경민       최초 등록
 * 02      2020-04-28    이경민       웹뷰 적용, 액티비티 결과값 처리 코드 추가
 * =======================================================================
 */
class IUBC11M00 : Activity_Default() {
    private var csno: String? = ""
    private val SYMMETRIC_KEY_SIZE = 32
    private val symmetricKey = "422d3951ef007cc32e2f714ccb6ff8dc"
    private val medCerti_url = "https://www.medcerti.com" // test url - 메티서티 웹사이트
    private val medCerti_subUrl = "/SMARTCONTRACT" // test url - 메디서티 url
    private val companycd = "002"
    private var urlParameterList: ArrayList<String>? = null
    private var urlValueList: ArrayList<String>? = null
    private var aes256Util: AES256Util? = null
    override fun onBackPressed() {
        super.onBackPressed()
    }

    override fun onDestroy() {
        super.onDestroy()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setIntentData()
        setUrlParameter()
        WebBrowserHelper.startWebViewActivity(applicationContext, 0, true, getUrl(urlParameterList, urlValueList), resources.getString(R.string.btn_smartclaim))
        setResult(RESULT_OK)
        finish()
    }

    override fun onResume() {
        super.onResume()
    }

    override fun onSaveInstanceState(p_bundle: Bundle) {
        super.onSaveInstanceState(p_bundle)
    }

    override fun onRestoreInstanceState(p_bundle: Bundle) {
        super.onRestoreInstanceState(p_bundle)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
    }

    override fun setInit() {
        try {
            if (symmetricKey.length != SYMMETRIC_KEY_SIZE) {
                Log.e("Key length error", "Length of key must be 32 bytes")
                return
            }
            aes256Util = AES256Util(symmetricKey)
        } catch (e: UnsupportedEncodingException) {
            e.message
        }
        urlParameterList = ArrayList()
        urlValueList = ArrayList()
    }

    override fun setUIControl() {}

    /**
     * Intent 데이터 세팅 함수
     */
    private fun setIntentData() {
        if (intent != null) {
            if (intent.hasExtra("csno")) {
                csno = intent.extras!!.getString("csno")
            }
        }
    }

    /**
     * 고객번호 암호화 값 url 파라미터 설정
     */
    private fun getUrlValueUserID(csno: String?): String {
        var encodedCsno = ""
        var encodedParameter = ""
        try {
            encodedCsno = aes256Util!!.aesEncode(csno)
            encodedParameter = URLEncoder.encode("{^userid^:^" + encodedCsno + "^,^companycd^:^" + companycd + "^,^sn^:^" + SharedPreferencesFunc.getSmartReqAuthTime(applicationContext) + "^}", "UTF-8")
        } catch (e: NoSuchAlgorithmException) {
            e.message
        } catch (e: NoSuchPaddingException) {
            e.message
        } catch (e: InvalidKeyException) {
            e.message
        } catch (e: InvalidAlgorithmParameterException) {
            e.message
        } catch (e: IllegalBlockSizeException) {
            e.message
        } catch (e: BadPaddingException) {
            e.message
        } catch (e: UnsupportedEncodingException) {
            e.message
        }
        return encodedParameter
    }

    /**
     * 병원코드 url 파라미터 설정
     */
    private fun getHospitalCodeParameter(hospitalCode: String): String {
        var encodedParameter = ""
        encodedParameter = URLEncoder.encode("{^giwanno^:^$hospitalCode^}")
        return encodedParameter
    }

    /**
     * MedCerti parameter 설정
     */
    private fun setUrlParameter() {
        val hospitalCode = SharedPreferencesFunc.getSmartReqHospitalCode(applicationContext)
        if (hospitalCode == "H20024") {
            urlParameterList!!.add("COMMAND")
            urlValueList!!.add("GIWANLIST")
            urlParameterList!!.add("GIWANINFO")
            urlValueList!!.add(getHospitalCodeParameter(hospitalCode))
        } else {
            urlParameterList!!.add("COMMAND")
            urlValueList!!.add("INFO")
        }
        urlParameterList!!.add("USERID")
        urlValueList!!.add(getUrlValueUserID(csno))
        urlParameterList!!.add("SERVICE")
        if (EnvConfig.isMedCertiOperationServer) {
            urlValueList!!.add("Y")
        } else {
            urlValueList!!.add("N")
        }
    }

    /**
     * MedCerti url + parameter 반환
     */
    private fun getUrl(urlParameterList: ArrayList<String>?, urlValueList: ArrayList<String>?): String {
        val url: String
        var queryParameter = "?"
        for (i in urlParameterList!!.indices) {
            queryParameter += urlParameterList[i] + "=" + urlValueList!![i]
            if (i != urlParameterList.size - 1) {
                queryParameter += "&"
            }
        }
        url = medCerti_url + medCerti_subUrl + queryParameter
        return url
    }
}