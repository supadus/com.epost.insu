package com.epost.insu.activity.auth

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Message
import android.text.TextUtils
import com.epost.insu.*
import com.epost.insu.EnvConfig.AuthDvsn
import com.epost.insu.EnvConfig.AuthMode
import com.epost.insu.common.CommonFunction
import com.epost.insu.common.CustomSQLiteFunction
import com.epost.insu.common.LogPrinter
import com.epost.insu.fido.Fido2Constant
import com.epost.insu.network.HttpConnections
import com.epost.insu.network.WeakReferenceHandler
import com.epost.insu.network.WeakReferenceHandler.ObjectHandlerMessage
import kr.or.kftc.fido.api.KFTCBioFidoManager
import kr.or.kftc.fido.api.OnCompleteListener
import org.json.JSONException
import org.json.JSONObject

/**
 * 인증 > 간편인증(핀/지문/패턴) > 로그인/전자서명
 * @since     : project 48:1.4.5
 * @version   : 1.5
 * @author    : NJM
 * @see
 * <pre>
 * ======================================================================
 * 1.4.5    NJM_20201229    최초 등록
 * 1.5.2    NJM_20210322    [subUrl 공통파일로 변경]
 * 1.5.2    NJM_20210322    [FIDO인증 로직 변경]
 * 1.5.3    NJM_20210330    [FIDO호출 로직 변경] 에러발생으로인한 변경
 * 1.5.4    NJM_20210429    [카카오페이앱 인증 S320] 앱to앱 인증 추가, push업데이트 삭제
 * 1.5.4    NJM_20210430    [FIDO 오류 개선] phoneInfoBundle NullPointerException 처리
 * 1.5.4    NJM_20210504    [간편인증 전자서명 추가] 웹요청 간편인증 추가, 청구시 간편인증 전자서명 추가
 * 1.5.8    NJM_20210624    [인증센터 로그인 개선] 소스정렬
 * 1.5.8    NJM_20210630    [로그인처리 공통화] CF_setLogin() 공통 로그인 처리 함수
 * 1.5.9    NJM_20210701    [PIN등록 오류 수정] 최초설치시 PIN등록시 csno 누락 오류 수정
 * 1.6.2    NJM_20210729    [간편인증 플래그 반영] 간편인증 로그인시 flag값 참조 변경 (서버저장 -> 단말저장)
 * 1.6.2    NJM_20210802    [2021년 대우 취약점] 2차본 : 부적절한 예외 처리
 * =======================================================================
 * copyright : (주)프로니스정보기술 | http://www.phronis.co.kr
 * </pre>
 */
class IUFC00M00 : Activity_Auth(), OnCompleteListener, ObjectHandlerMessage {
    private val HANDLERJOB_AUTH_1: Int = 1 // 간편인증 선거래
    private val HANDLERJOB_ERROR_AUTH_1: Int = 2
    private val HANDLERJOB_AUTH_2: Int = 3 // 간편인증 본거래
    private val HANDLERJOB_ERROR_AUTH_2: Int = 4

    private var phoneInfoBundle: Bundle? = null // FIDO디바이스 정보
    private var authTechCode: String? = null // 간편인증코드(116:PIN,100:지문,..)
    private var authMode: AuthMode? = null // 요청모드("LOGIN_APP", "SIGN_APP",..)
    private var tmp_tlgr_chas_no: String? = null // 전문추적번호 결과값

    override fun onComplete(resCode: String, resData: Bundle) {
        LogPrinter.CF_debug("!----------------------------------------------------------")
        LogPrinter.CF_debug("!-- IUFC00M00.onComplete() --FIDO 콜백")
        LogPrinter.CF_debug("!----------------------------------------------------------")
        LogPrinter.CF_debug("!---- resCode : $resCode / resData : $resData")

        // -- (API 성공)
        if ((Fido2Constant.FIDO_CODE_SUCCESS == resCode)) {
            LogPrinter.CF_debug("!----------------------------------------------------------")
            LogPrinter.CF_debug("!-- IUFC00M00.onReceiveMessage() --FIDO API 인증 요청 성공")
            LogPrinter.CF_debug("!----------------------------------------------------------")
            /*
                Bundle[{DATA_KEY_PUBKEY=null,
                        DATA_KEY_AAID=0012#0020,
                        DATA_KEY_CODE=116,
                        DATA_KEY_FIDO={"uafResponse":"[{\"assertions\":[{\"assertion\":\"Aj7RAAQ-ggALLgkAMDAxMiMwMDIwDi4FAAEAAQYADy4UAHQJFa2qCBcdnsrbJUFtU9L5P3jmCi4gAFYqVuOatl4rRER3p-vsbammVfv730qpz-xJGWfk-GM9EC4AAAkuIAAZHwTm3wrxbl2HnNs1-6Fx1ZHlT_YlZRyfeVzZb7PuBA0uBAAAAAAABi5HADBFAiAPsSffHwf2wneL_hsmwJZ3N9M3qw_Ukc7IOBTOoS186QIhAPuqtGbZ5BFPWofGx6XrI5jIYHFHBg7TT94C1_n5LFvj\",\"assertionScheme\":\"UAFV1TLV\"}],\"fcParams\":\"eyJhcHBJRCI6ImFuZHJvaWQ6YXBrLWtleS1oYXNoOkNBMEV5eHZ5QzZUKzdGWll2VVUxamxCRmExVSIsImNoYWxsZW5nZSI6IjF1dVhUSW1LSGlEeUF4S00waktURDBmT2FnaUVSam5DbzVEckswZzVJUFEiLCJjaGFubmVsQmluZGluZyI6eyJzZXJ2ZXJFbmRQb2ludCI6IlgtenJadl9JYnpqWlVuaHNiV2xzZWNMYndqbmRUcEcwWnluWE9pZjdWLWsiLCJ0bHNTZXJ2ZXJDZXJ0aWZpY2F0ZSI6Ik1BIn0sImZhY2V0SUQiOiJhbmRyb2lkOmFway1rZXktaGFzaDpDQTBFeXh2eUM2VCs3RlpZdlVVMWpsQkZhMVUifQ\",\"header\":{\"appID\":\"android:apk-key-hash:CA0EyxvyC6T+7FZYvUU1jlBFa1U\",\"op\":\"Auth\",\"serverData\":\"Pw5mjw-svPFkV842kcV8peBUcjjyPS7F3STcuai5xps\",\"upv\":{\"major\":1,\"minor\":0}}}]"}
                        , DATA_KEY_NIDCT_CHALLENGE=5850459E, DATA_KEY_TLS_CERT=MI, DATA_KEY_NEED_KEY=N, DATA_KEY_AUTH_TECH_CODE=100, DATA_KEY_PRIVKEY=null, DATA_KEY_NIDCT=22948556, DATA_KEY_SITE_CODE=00071, DATA_KEY_SVC_CODE=005}]
             */
            /*
                Bundle[{DATA_KEY_PUBKEY=30820122300D06092A864886F70D01010105000382010F003082010A0282010100CAB61186C6D0CFC4C7EBE6415FD8F50F10BF2C8C8D5531C5E9F431EF9FD1A91C0D63389B4EF0DE3187EB89B0E7F4060A2657848940E58E4DE00DBA11F845C4F7592F6F3419719D85E0C42CB2B36EBD25813218B2466061758E51139C7F01F553C6F97F652CF208BF543AAD431413E78B796E053C1E11B37F5E43077F353DE16008AC7FFF6965810441BCF012BF20782DE2605678E00D9C8D7ED67A8660B10312744549A57A47A2C9873DAE55492752DDC0AE39D313484B2C21A521D6EF3DEE4EEAA1A1C8B67EA39FC35A8EE92D2DFA3098857F5C084B189CF09D4A825B887F3819DF189B632BDBC032DB6E718E8D6B398C3A90AC072A2EEF29CEE089BE68DAA10203010001
                , DATA_KEY_CODE=2116
                , DATA_KEY_TRID=16207121622688874633
                , DATA_KEY_NEED_KEY=Y
                , DATA_KEY_AUTH_TECH_CODE=116
                , DATA_KEY_PRIVKEY=308204BD020100300D06092A864886F70D0101010500048204A7308204A30201000282010100CAB61186C6D0CFC4C7EBE6415FD8F50F10BF2C8C8D5531C5E9F431EF9FD1A91C0D63389B4EF0DE3187EB89B0E7F4060A2657848940E58E4DE00DBA11F845C4F7592F6F3419719D85E0C42CB2B36EBD25813218B2466061758E51139C7F01F553C6F97F652CF208BF543AAD431413E78B796E053C1E11B37F5E43077F353DE16008AC7FFF6965810441BCF012BF20782DE2605678E00D9C8D7ED67A8660B10312744549A57A47A2C9873DAE55492752DDC0AE39D313484B2C21A521D6EF3DEE4EEAA1A1C8B67EA39FC35A8EE92D2DFA3098857F5C084B189CF09D4A825B887F3819DF189B632BDBC032DB6E718E8D6B398C3A90AC072A2EEF29CEE089BE68DAA1020301000102820100114987A31BCF70F892281F01D23FB95E688A352D635C319B74413C04F87733D9CD96799C5D16138CDBCF8D7585C27CC55ED1B5DED2A70104C927C28787C9C36B68B2AFE41C8B60E8AAEB6FF8696EF7387C951E200DFA68355346BCCA6AD66AEA931C71EDD9F0662892F15217575DFF3018D1F6265356251DA69746CB6569FA8F43F3F76ED20C1D3AA8AF490B79272ECB93558BF178B8F5685874892C4592787D5ADEAAC50BC09D7B475E849BD554FD9A1A0D8836B84125307DDEA728A02955542FAA20732A0E119C619B42ED0025CCCE22CC89C4581BE40C6F4FCB74388F16745AF153A2774706171ACDBBE2E31E3BA24A280DB55044301AC17C5CFF27FDF3D502818100FF00DA745910FCCC11C3D8E0BE4FC1E2F6EC92F2FF237DF0B445D87E7C5D055C12D9584EB750740C4BCEE5387C67EED435A18BDC74A6A1BFBA4246A1B5186B06A892445C960DAF737635712EAB68DC036B1FAA081989F67A368AABDFC3E3D58465BD1A3D3B659173C997A65F33635844B58AD7FA29747B9E167A1D60B9EF1F3702818100CB80E4C34A17A1450FF558D808D8C6A7E57DF724BE3893896C1F3D16493F25B5209B8CC4128B072B4E7F2383965718F5B9C9618127F57698EC41F7AF76CB12A033D8AB5E2DD9E81B1D6333CB504F7EA4A72912294B67DA8757A0FD73004AB3E56210ED912F727F557C3C3135B7800F5C174CED893D2493591070A291D171D0E7028180034BB087166B98C8CE65F894970112E16B0D8DACA44D82202D4445D304AA731A4888149317DD6B52895C482C1DB77E27B1F5BF87FEDDF47334E60C61A4A4872593E2280E064671010E030CCE6C9AB3553BFA31C95F0320735DE716E792718C5DE4AEB7FB189C6C6FD1A82B1749E3BBCD4F8756D93DD5EA8F157D4889EA7D43AB0281807950110F49EFE437635802EA65FBB4110A8198FED256AFBCD0339F1AD5F74AE73AD4FF8932F4A6056EF9E395E1DD7207AA051E96FBD71C27F54F2DF3EF81074D711ACCAD8BEFC6A9C34F0BE53BAEE7F407A2382D5DC23F474739B55D04E383724E8F80CF77D56D8AEC5085955C1A7F0D8AE65503E6EB12DB992D4BFC593ADC2902818100E9252E49562DE98D708706550F1FCFBFA15314EB2F7E02174F86999824457D641C6505F8A8FCE967AD10C9436771D20CC4987EBB8FFCC5B366244C949AED87631FC422E0172FAB628018D76ED6FE794B4A435201015AE50747901BD576EE5EC060362FE8E23E4C3ECAFE7FFAFDED1DFF488C0465D73236184A6D637BEA6DF84A
                , DATA_KEY_VERSION=0107}]
             */
            var tmp_aaid: String? = ""
            var tmp_nidct: String? = ""
            var tmp_message: String? = ""
//            var tmp_prvKey: String? = resData.getString((Fido2Constant.KEY_PRV_KEY)) // 개인키
//            var tmp_pubKey: String? = resData.getString((Fido2Constant.KEY_PUB_KEY)) // 공개키

            // -- (지문) : 지문일때만 FIDO값 있음
            if (resData.getString(Fido2Constant.KEY_FIDO) != null) {
                tmp_aaid = resData.getString(Fido2Constant.KEY_AAID) // AAID
                tmp_nidct = resData.getString(Fido2Constant.KEY_NIDCT) // 비식별번호토큰
                tmp_message = resData.getString(Fido2Constant.KEY_FIDO) // FIDO 응답 메시지
            }
            assert(tmp_message != null)
            httpReq_authFido2(tmp_aaid, tmp_nidct, tmp_message /*, tmp_prvKey, tmp_pubKey*/)
        } else {
            showCustomDialog(Fido2Constant.getErrMsgForCode(resCode))
        }
    }

    override fun setInit() {
        LogPrinter.CF_debug("!-----------------------------------------------------------")
        LogPrinter.CF_debug("!-- IUFC00M00.setInit() --간편인증")
        LogPrinter.CF_debug("!-----------------------------------------------------------")

        handler = WeakReferenceHandler(this)
        if (intent.hasExtra(EnvConstant.KEY_INTENT_AUTH_MODE)) {
            authMode = intent.getSerializableExtra(EnvConstant.KEY_INTENT_AUTH_MODE) as AuthMode?
        }
        if (intent.hasExtra(EnvConstant.KEY_INTENT_AUTH_TECH_CODE)) {
            authTechCode = intent.extras?.getString(EnvConstant.KEY_INTENT_AUTH_TECH_CODE)
        }
        if (intent.hasExtra("key")) {
            SharedPreferencesFunc.setWebTempKey(applicationContext, intent.extras?.getString("key"))
        }
        LogPrinter.CF_debug("!---- authTechCode : $authTechCode")
        LogPrinter.CF_debug("!---- authMode     : $authMode")
        setContentView(R.layout.iufc00m00)
    }

    override fun setUIControl() {
        LogPrinter.CF_debug("!-----------------------------------------------------------")
        LogPrinter.CF_debug("!-- IUFC00M00.setUIControl() --간편인증")
        LogPrinter.CF_debug("!-----------------------------------------------------------")
        CF_showProgressDialog()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        LogPrinter.CF_debug("!-----------------------------------------------------------")
        LogPrinter.CF_debug("!-- IUFC00M00.onCreate() --간편인증")
        LogPrinter.CF_debug("!-----------------------------------------------------------")
        LogPrinter.CF_debug("!-- authTechCode : $authTechCode")
        LogPrinter.CF_debug("!-- authMode     : $authMode")

        try {
            phoneInfoBundle = (applicationContext as CustomApplication).bundleBioInfo
            // -- 선거래 요청
            httpReq_authFido1()
        } catch (e: NullPointerException) {
            e.message
            CommonFunction.CF_showCustomDialogFinishActivity(this, resources.getString(R.string.dlg_error_param_1))
        } catch (e : Exception) {
            e.message
            CommonFunction.CF_showCustomDialogFinishActivity(this, resources.getString(R.string.dlg_error_param_1))
        }
    }

    override fun onPause() {
        super.onPause()
        LogPrinter.CF_debug("!-----------------------------------------------------------")
        LogPrinter.CF_debug("!-- IUFC00M00.onPause() --간편인증")
        LogPrinter.CF_debug("!-----------------------------------------------------------")
    }

    override fun onResume() {
        super.onResume()
        LogPrinter.CF_debug("!----------------------------------------------------------")
        LogPrinter.CF_debug("!-- IUFC00M00.onResume()")
        LogPrinter.CF_debug("!----------------------------------------------------------")
        LogPrinter.CF_debug("!-- authTechCode    : $authTechCode")
        LogPrinter.CF_debug("!-- authMode        : " + (if (authMode != null) authMode.toString() else "NULL"))

        if (phoneInfoBundle == null) {
            phoneInfoBundle = (applicationContext as CustomApplication).bundleBioInfo
        }
        LogPrinter.CF_debug("!-- phoneInfoBundle : " + (if (phoneInfoBundle != null) phoneInfoBundle.toString() else "NULL"))
    }

    override fun onStop() {
        super.onStop()
        LogPrinter.CF_debug("!----------------------------------------------------------")
        LogPrinter.CF_debug("!-- IUFC00M00.onStop()")
        LogPrinter.CF_debug("!----------------------------------------------------------")
    }

    override fun onDestroy() {
        super.onDestroy()
        LogPrinter.CF_debug("!----------------------------------------------------------")
        LogPrinter.CF_debug("!-- IUFC00M00.onDestroy()")
        LogPrinter.CF_debug("!----------------------------------------------------------")
    }

    // -- FIDO 인앱 API 호출
    private fun reqBioAuth(pBundle: Bundle) {
        LogPrinter.CF_debug("!----------------------------------------------------------")
        LogPrinter.CF_debug("!-- IUFC00M00.reqBioAuth() --FIDO API 호출")
        LogPrinter.CF_debug("!----------------------------------------------------------")
        LogPrinter.CF_debug("!--pBundle2 : $pBundle")

        try {
            val kftcBioFidoManager = KFTCBioFidoManager(applicationContext)
            kftcBioFidoManager.reqBioAuth(pBundle, this)
        } catch (e: NullPointerException) {
            e.message
            showCustomDialog(resources.getString(R.string.dlg_error_server_5))
        } catch (e: Exception) {
            e.message
            showCustomDialog(resources.getString(R.string.dlg_error_server_5))
        }
    }

    // #############################################################################################
    //  Http 관련
    // #############################################################################################
    override fun handleMessage(p_message: Message) {
        LogPrinter.CF_debug("!----------------------------------------------------------")
        LogPrinter.CF_debug("!-- IUFC00M00.handleMessage()")
        LogPrinter.CF_debug("!----------------------------------------------------------")

        when (p_message.what) {
            HANDLERJOB_AUTH_1 -> try {
                httpRes_authFido1(JSONObject(p_message.obj as String?))
            } catch (e: JSONException) {
                LogPrinter.CF_debug(p_message.obj.toString())
                showCustomDialog(getResources().getString(R.string.dlg_error_server_5))
            }
            HANDLERJOB_AUTH_2 -> try {
                httpRes_authFido2(JSONObject(p_message.obj as String?))
            } catch (e: JSONException) {
                LogPrinter.CF_debug(p_message.obj.toString())
                showCustomDialog(getResources().getString(R.string.dlg_error_server_5))
            }
            HANDLERJOB_ERROR_AUTH_1, HANDLERJOB_ERROR_AUTH_2 -> {
                LogPrinter.CF_debug(p_message.obj.toString())
                showCustomDialog(getResources().getString(R.string.dlg_error_server_5))
            }
            else -> {
            }
        }
    }

    /**
     * FIDO 인증 1단계 HTTP 요청
     * @since 1.0
     */
    @Throws(NullPointerException::class)
    private fun httpReq_authFido1() {
        LogPrinter.CF_debug("!----------------------------------------------------------")
        LogPrinter.CF_debug("!-- IUFC00M00.httpReq_authFido1() --FIDO 인증 1단계 HTTP 요청")
        LogPrinter.CF_debug("!----------------------------------------------------------")
        LogPrinter.CF_debug("!---- authTechCode : $authTechCode")

        val builder: Uri.Builder = Uri.Builder()
        if (phoneInfoBundle != null) {
            builder.appendQueryParameter("csno"         , CustomSQLiteFunction.getLastLoginCsno(applicationContext))
            builder.appendQueryParameter("auth_tech"    , authTechCode) // 100:지문, 116:핀, 121:패턴
            builder.appendQueryParameter("device_id"    , phoneInfoBundle!!.getString(Fido2Constant.KEY_DEVICE_ID))
            builder.appendQueryParameter("appVersion"   , phoneInfoBundle!!.getString(Fido2Constant.KEY_VERSION))
            builder.appendQueryParameter("osType"       , Fido2Constant.OS_TYPE) // 고정(1:안드로이드)
            builder.appendQueryParameter("serviceCode"  , Fido2Constant.SVC_CODE) // 서비스코드 005 고정
            HttpConnections.sendPostData(
                EnvConfig.host_url + EnvConfig.URL_FIDO_AUTH1,
                builder.build().encodedQuery,
                handler,
                HANDLERJOB_AUTH_1,
                HANDLERJOB_ERROR_AUTH_1)
        } else {
            throw NullPointerException("ERR : phoneInfoBundle 초기화 오류 ")
        }
    }

    /**
     * FIDO 인증 1단계 HTTP 응답
     * @since 1.0
     * @param p_jsonObject      JSONObject
     */
    @Throws(JSONException::class)
    private fun httpRes_authFido1(p_jsonObject: JSONObject) {
        LogPrinter.CF_debug("!----------------------------------------------------------")
        LogPrinter.CF_debug("!-- IUFC00M00.httpRes_authFido1() --FIDO 인증 1단계 HTTP 응답")
        LogPrinter.CF_debug("!----------------------------------------------------------")
        LogPrinter.CF_debug("!---- p_jsonObject : $p_jsonObject")

        /*
            핀인증 성공
            p_jsonObject : {"data":{"s_tx_type":"0","s_tx_ctnt":"","s_tx_ctnt_len":"00000","s_enc_dvsn":"","s_fido_tlgr_no":"16091397906179360957","errCode":"000000","s_trmn_id_enc":"232EC3578C7A6D0FCAAC1A709FD3E3DEFF83C4734E4E3A11BE5F59E79F525B5D","s_tx_ctnt_hash":"","s_autn_tech_code":"116","s_tlgr_chas_no":"000717083585520201228","s_psnid_enc":"","errMsg":"Success.","errDetail":"","e_msg_no":"071024DFFCF16","s_serv_code":"005","s_filler":"","tranId":"691330_UEE691330UI0","s_rgtn_yn_inqy_key":"70299B6A92431F05760ECE2F35614494474968AC62E5EC3ECE50439B5F3DAE77","next_gb":"0"},"errCode":""}
         */
        val jsonKey_errorCode        = "errCode"
        val jsonKey_data             = "data"
        val jsonKey_debugMsg         = "debugMsg"
        val jsonKey_s_fido_tlgr_no   = "s_fido_tlgr_no" // FIDO 추적번호
        val jsonKey_s_tlgr_chas_no   = "s_tlgr_chas_no" // 전문추적번호
        val jsonKey_s_cpat_tokn_chng = "s_cpat_tokn_chng" // 호환토큰 Challenge
        val jsonKey_s_autn_rqut_msg  = "s_autn_rqut_msg" // 인증요청 메시지
        val tmp_errorCode: String
        var tmp_errorMsg: String?    = ""

        // --<> (통신 성공)
        if (p_jsonObject.has(jsonKey_errorCode)) {
            tmp_errorCode = p_jsonObject.getString(jsonKey_errorCode)

            // -- (전문 에러)
            if ("" != tmp_errorCode) {
                tmp_errorMsg = p_jsonObject.getString(jsonKey_debugMsg)
            } else {
                if (p_jsonObject.has(jsonKey_data)) {
                    val tmp_jsonData: JSONObject = p_jsonObject.getJSONObject(jsonKey_data)
                    tmp_tlgr_chas_no = tmp_jsonData.getString(jsonKey_s_tlgr_chas_no)
                    try {
                        val tmp_bundleApi = Bundle()
                        tmp_bundleApi.putString(Fido2Constant.KEY_AUTH_TECH_CODE, authTechCode)
                        tmp_bundleApi.putString(Fido2Constant.KEY_NEED_KEY, "N") // 축약서명 키쌍 필요여부 "Y", "N" ::: 축약서명타입 0(일반)만 사용하기에 N 값 넣음.

                        // --<> (지문)
                        if ((authTechCode == Fido2Constant.AUTH_TECH_FINGER)) {
                            tmp_bundleApi.putString(Fido2Constant.KEY_NIDCT_CHALLENGE, tmp_jsonData.getString(jsonKey_s_cpat_tokn_chng))
                            tmp_bundleApi.putString(Fido2Constant.KEY_FIDO, tmp_jsonData.getString(jsonKey_s_autn_rqut_msg))
                            tmp_bundleApi.putInt(Fido2Constant.KEY_CODE, Fido2Constant.FIDO_CODE_AUTH_IN)
                            tmp_bundleApi.putString(Fido2Constant.KEY_SITE_CODE, Fido2Constant.SITE_CODE)
                            tmp_bundleApi.putString(Fido2Constant.KEY_SVC_CODE, Fido2Constant.SVC_CODE)
                            tmp_bundleApi.putString(Fido2Constant.KEY_TLS_CERT, Fido2Constant.TLS_CERTIFICATE)
                        } else {
                            tmp_bundleApi.putString(Fido2Constant.KEY_TRID, tmp_jsonData.getString(jsonKey_s_fido_tlgr_no)) // FIDO 추적번호
                        }

                        // -- FIDO API 실행
                        reqBioAuth(tmp_bundleApi)

                        // --접근성을 위한 Announcement 출력
                        //sendAnnounceMsgForAuthFinger();

                    } catch (e: NullPointerException) {
                        e.message
                        showCustomDialog(resources.getString(R.string.dlg_error_server_5))
                    } catch (e: Exception) {
                        e.message
                        showCustomDialog(resources.getString(R.string.dlg_error_server_5))
                    }
                } else {
                    tmp_errorMsg = getResources().getString(R.string.dlg_error_server_2)
                }
            }
        } else {
            tmp_errorMsg = getResources().getString(R.string.dlg_error_server_1)
        }

        // -- 에러가 있는 경우 에러 메시지 팝업
        if (!TextUtils.isEmpty(tmp_errorMsg)) {
            showCustomDialog(tmp_errorMsg)
        }
    }

    /**
     * FIDO 인증 처리
     * @since 1.0
     */
    private fun httpReq_authFido2(p_aaid: String?, p_nidct: String?, p_message: String? /*, String prvKey, String pubKey*/) {
        LogPrinter.CF_debug("!----------------------------------------------------------")
        LogPrinter.CF_debug("!-- IUFC00M00.httpReq_authFido2() --FIDO 인증 2단계 HTTP 요청")
        LogPrinter.CF_debug("!----------------------------------------------------------")

        // 공통
        val builder: Uri.Builder = Uri.Builder()
        builder.appendQueryParameter("auth_tech"     , authTechCode) // 100:지문, 116:핀, 121:패턴
        builder.appendQueryParameter("tlgr_chas_no"  , tmp_tlgr_chas_no) // 전문추적번호(공동 FIDO인증 선거래 거래 결과)
        builder.appendQueryParameter("autn_rslt_code", "0000") // 인증결과코드(0000:고정) but 금융결제원 바이오인증앱에서 -1203, -1290 에러를 받은 경우 5121
        builder.appendQueryParameter("isSign"        , if(AuthMode.isLogin(authMode)) "N" else "Y") // 전자서명 여부(N:로그인, Y:전자서명)

//        tmp_builder.appendQueryParameter("signData", prvKey); // 개인키
//        tmp_builder.appendQueryParameter("signData2", pubKey); // 공개키

        // 기타(추가처리용)
        builder.appendQueryParameter("serviceCode", Fido2Constant.SVC_CODE) // (고정값) 서비스코드
        builder.appendQueryParameter("device_id"  , phoneInfoBundle!!.getString(Fido2Constant.KEY_DEVICE_ID)) // 로그기록위해 추가
        builder.appendQueryParameter("csno"       , CustomSQLiteFunction.getLastLoginCsno(applicationContext)) // 고객번호
        builder.appendQueryParameter("tempKey"    , SharedPreferencesFunc.getWebTempKey(applicationContext))

        // (지문) : 지문일경우만 공백이 아님
        builder.appendQueryParameter("aaid"                  , p_aaid)
        builder.appendQueryParameter("non_rcgn_tokn"         , p_nidct) // 비식별호환토큰
        builder.appendQueryParameter("fido_autn_resp_msg_len", "" + p_message!!.length) // FIDO 인증응답메시지 length
        builder.appendQueryParameter("fido_autn_resp_msg"    , p_message) // FIDO 인증응답메시지
        LogPrinter.CF_debug("!---- tmp_builder : $builder")

        HttpConnections.sendPostData(
            EnvConfig.host_url + EnvConfig.URL_FIDO_AUTH2,
            builder.build().encodedQuery,
            handler,
            HANDLERJOB_AUTH_2,
            HANDLERJOB_ERROR_AUTH_2)
    }

    /**
     * FIDO 인증 결과 처리
     * @since 1.0
     * @param p_jsonObject  JSONObject
     */
    @Throws(JSONException::class)
    private fun httpRes_authFido2(p_jsonObject: JSONObject) {
        LogPrinter.CF_debug("!----------------------------------------------------------")
        LogPrinter.CF_debug("!-- IUFC00M00.httpRes_authFido2() --FIDO 인증 2단계 HTTP 응답")
        LogPrinter.CF_debug("!----------------------------------------------------------")
        LogPrinter.CF_debug("!---- p_jsonObject : $p_jsonObject")

        val jsonKey_errorCode = "errCode"
        val jsonKey_debugMsg = "debugMsg"
        val jsonKey_data = "data"
        val jsonKey_tempKey = "tempKey"
        val jsonKey_name = "name"
        val tmp_errorCode: String
        var tmp_errorMsg: String? = ""
        if (p_jsonObject.has(jsonKey_errorCode)) {
            tmp_errorCode = p_jsonObject.getString(jsonKey_errorCode)

            // --<> (최종 에러)
            if (!("" == tmp_errorCode)) {
                tmp_errorMsg = p_jsonObject.getString(jsonKey_debugMsg)
            }
            else {
                if (p_jsonObject.has(jsonKey_data)) {
                    val tmp_csno: String = CustomSQLiteFunction.getLastLoginCsno(applicationContext) // 최초 공동인증시 저장된 DB에서 가져오기
                    var tmp_name = ""

                    // -- 서버에서 name, tempKey, flagInsuCust 정보를 data 키가 아닌 최상위에 넣어서 보냄.
                    if (p_jsonObject.has(jsonKey_name)) {
                        tmp_name = p_jsonObject.getString(jsonKey_name)
                    }

                    // --<> (로그인 요청시)
                    if(AuthMode.isLogin(authMode)) {
                        val fidoLoginType: AuthDvsn = AuthDvsn.getAuthDvsnByFido(authTechCode)

                        // -----------------------------------------------------------------------------
                        // -- 최종 로그인 처리
                        // -----------------------------------------------------------------------------
                        CF_setLogin(true, fidoLoginType, tmp_csno, tmp_name, p_jsonObject.optString(jsonKey_tempKey) )

                        // --<> (앱요청)
                        if (authMode == AuthMode.LOGIN_APP) {
                            setResult(RESULT_OK)
                        }
                        else { // --<> (웹요청)
                            startActivityComplete()
                        }
                    } else {
                        setResult(RESULT_OK)
                    }
                    finish()
                } else {
                    tmp_errorMsg = resources.getString(R.string.dlg_error_server_2)
                }
            }
        } else {
            tmp_errorMsg = resources.getString(R.string.dlg_error_server_1)
        }

        // -- 에러가 있는 경우 에러 메시지 팝업
        if (!TextUtils.isEmpty(tmp_errorMsg)) {
            showCustomDialog(tmp_errorMsg)
        }
    }

    /**
     * 인증 완료 Activity 호출 함수
     */
    private fun startActivityComplete() {
        val intent = Intent(this, IUFC00M09::class.java)
        intent.putExtra("authDvsn", AuthDvsn.getAuthDvsnByFido(authTechCode))
        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
        startActivity(intent)
    }
}