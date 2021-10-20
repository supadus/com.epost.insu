package com.epost.insu;

import android.Manifest;
import android.content.Context;
import android.os.Build;
import android.os.Environment;

import com.epost.insu.common.CommonFunction;
import com.epost.insu.common.LogPrinter;
import com.epost.insu.fido.Fido2Constant;

import java.util.Arrays;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Locale;
/**
 * 설정 값 클래스
 * @since     :
 * @version   : 1.7
 * @author    : NJM
 * <pre>
 *      App 사용하는 url, 상수 등의 값 정의 클래스
 * ======================================================================
 * 0.0.0    LSH_20170628    최초 등록
 * 0.0.0    LKM_20180814    스마트보험금청구 구현 추가
 * 0.0.0    NJM_20190218    개발/운영 구분 추가(초기화 화면에 표기) : app_dvsn
 * 0.0.0    NJM_20191001    로그표시 여부 추가
 * 0.0.0    NJM_20191208    정액+실손 추가
 * 0.0.0    NJM_20200122    인증유형/청구유형 추가
 * 0.0.0    NJM_20200216    arrLoginMenuPage 리스트 추가
 * 1.5.2    NJM_20210322    [subUrl 공통파일로 변경]
 * 1.5.4    NJM_20210504    [간편인증 전자서명 추가] 전자서명 web요청Key 추가
 * 1.5.5    NJM_20210504    [구비서류 사이즈 변경] 2000 -> 1600
 * 1.5.6    NJM_20210528    [청구서서식변경] 청구사유변경(깁스삭제,기타추가)
 * 1.6.1    NJM_20210708    [청구가능시간 변경] 4~5시 청구 불가 처리
 * 1.6.1    NJM_20210726    [web요청스킴 추가] 자녀보험금청구, 지급내역조회 스킴 추가
 * 1.6.2    NJM_20210708    [청구가능시간 변경] 4~5시 청구 불가 처리 오류 수정
 * =======================================================================
 * copyright : (주)프로니스정보기술 | http://www.phronis.co.kr
 * </pre>
 */
public class EnvConfig {

    // [필수설정]
    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // TODO!!!!!!!!!!운영/개발 확인 필!!!!!!!!!
    // build.gradle app   > FIDO 모듈
    // Manifest.xml       > push 모듈
    // proguard-debug.pro > 난독화
    // TODO!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!

    /**
     * true
     *  - 로그표기
     *  - DRM 미적용(캡쳐가능)
     *  - 고객번호로 로그인 폼 표기
     */
    public static final boolean uiTest = false; //팝업 테스트 TODO : 배포시 -> false 필!
    public static final boolean mFlagShowLog = true;  // 로그유무(true경우 임시로그인 가능함) TODO : 배포시 -> false 필!
    public static final String appVersion    = "164"; // 서버에서 앱 구분값 TODO 버전업시 확인 필!
    public static final String userAgentAdd  = "/MISAPP_ANDROID_v" + appVersion; // 서버에서 Agent 구분값

// [운영]
///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////



    /*
    public static final String operation             = "product";                                        // product:운영
    public static final String app_dvsn              = "";                                               // 로딩화면에 운영/개발 구분
    public static final String host_url              = "https://m.postinsu.kr:20433";                    // host url
    public static final String keywireless_ip        = "125.60.4.34";                                    // 인증서 가져오기 IP
    public static final String appDefenceLicenseCode = "N2JiOTc2NjVlODIyNDMzMDMzNTU3NWFhN2FlNzAzMjFlMmVjNTgzY0soMCkrRSgxKStEKDEpdW5saW1pdGVk";
    public static final String mVaccineId            = "koreapost_mobile";
    public static final String mVaccineLicenseKey    = "b809665b2d37ae87447d78fb791f512bdaca5649";
    public static final Integer keywireless_port     = 20500;                                            // 인증서 가져오기 POST
    public static final String pushServerUrl         = "https://ipostbank.kr";                           // PUSH 서버 URL
    */


//    // -- 스마트보험금청구 medCerti url
//    public static final boolean isMedCertiOperationServer = true;                                           // 운영 서버인 경우 true
///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
// [개발]
///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    public static final String operation             = "devel";                                          // devel:개발
    public static final String app_dvsn              = "(개발)";                                         // 로딩화면에 운영/개발 구분
    public static final String host_url              = "https://mdev2.postinsu.kr:20433";                 // host url
    public static final String keywireless_ip        = "125.60.4.154";                                   // 인증서 가져오기 IP
    public static final String appDefenceLicenseCode = "YmUyZjUzMDYyYTU3Y2I4ZGE5MzM1YzMzNTczMjdjMzEzOTg1ZjM0NUsoMCkrRSgxKStEKDEpMjAxNy0wOC0xNH4yMDE4LTAyLTE0";
    public static final String mVaccineId            = "";
    public static final String mVaccineLicenseKey    = "";
    public static final Integer keywireless_port     = 20500;                                            // 인증서 가져오기 POST
    public static final String pushServerUrl         = "http://125.60.4.147";                            // PUSH 서버 URL

    // -- 스마트보험금청구 medCerti url
    public static final boolean isMedCertiOperationServer = false;                                          // 운영 서버인 경우 true
///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * 로그인이 필요한 웹페이지 메뉴 번호 리스트
     * <pre>
     *  NJM_20191215    21(연금예상액조회) 추가
     *  NJM_20210513    27(내소식) 추가
     * </pre>
     */
    public static final List<String> arrLoginMenuPage = Arrays.asList("2","6","8","9","10","21","27");
    public static final long logoutTime               = 20*60*1000L; // 자동 로그아웃 타임


    public static final long webToAppReqLoginTime     = 3*60*1000; // Web 요청 인증 제한 시간(3분)
    public static final String SHAREDPREFERENCES_NAME = "com.epost.insu";
    public static final String SHAREDPREFERENCES_NAME_SETTING = "com.epost.insu.setting";
    public static final String epostBankAppPackage    = "com.epost.psf.sdsi"; // 우체국 스마트뱅킹 패키지명
    public static final String postPayAppPackage      = "com.epost.psf.ss"; // 포스트페이 패키지명

    // 브라우저별 패키지명
    public static final String webBrowserPackages[] = {
            "com.android.chrome"            // 크롬    O
            ,"com.sec.android.app.sbrowser"  // 삼성브라우저
            ,"com.samsung.android.sm"        // 삼성
            ,"org.mozilla.firefox"           // 파이어폭스 O
            ,"com.opera.browser"             // 오페라  O
            ,"com.nhn.android.search"        // 네이버  O
            ,"net.daum.android.daum"         // 다음    O
            ,"com.nate.android.portalmini"   // 네이트  O
    };

    // ---------------------------------------------------------------------------------------------
    // HTTP 타임아웃
    // ---------------------------------------------------------------------------------------------
    public static final int HTTP_CONNECT_TIMEOUT    = 60 * 1000;
    public static final int HTTP_SEND_TIMEOUT       = 60 * 1000;
    public static final int HTTP_POST_TIMEOUT       = 60 * 1000;
    public static final int HTTP_MILTIPART_TIMEOUT  = 100 * 1000;
    // HTTP Chunked Size
    public static final int HTTP_CHUNKED_SIZE    = 1024; // 최초 : 1024
    public static final int HTTP_MAX_BUFFER_SIZE = 1024*1024; // 최초 : 1024*1024 = 1M
    // ---------------------------------------------------------------------------------------------
    // 청구가능시간 (05시~ 23시가능) -> 24시간가능 -> 4:00~4:59불가
    // ---------------------------------------------------------------------------------------------
    public static final int PAY_MIN_HOUR = 4;
    public static final int PAY_MAX_HOUR = 4;
    /**
     * 청구가능시간 조회
     * @return enableHour(true:가능, false:불가)
     */
    public static boolean isPayEnableHour(Context context) {
        LogPrinter.CF_debug("!----------------------------------------------------------");
        LogPrinter.CF_debug("!-- EnvConfig.isPayEnableHour()");
        LogPrinter.CF_debug("!----------------------------------------------------------");
        boolean enableHour = true;
        GregorianCalendar now = new GregorianCalendar(Locale.KOREA);
        int nowHour = now.get(Calendar.HOUR_OF_DAY);
        if (nowHour >= PAY_MIN_HOUR && nowHour <= PAY_MAX_HOUR) {
            CommonFunction.CF_showCustomAlertDilaog(context, context.getResources().getString(R.string.dlg_pay_enable_time), context.getResources().getString(R.string.btn_ok));
            enableHour = false;
        }
        LogPrinter.CF_debug("!---- 현재시간 : " + nowHour + " / 청구가능여부 : " + enableHour);
        return enableHour;
    }

    // ---------------------------------------------------------------------------------------------
    //  이미지 캐쉬
    // ---------------------------------------------------------------------------------------------
    public static final String IMAGE_TEMP_FOLER   = "/libscan/result/"; // 일루텍 임시이미지 경로
    public static final int MAX_PHOTO_COUNT       = 15; // 구비서류첨부파일 최대 갯수
    public static final int upload_photo_max_size = 1600; // 구비서류 이미지 가로 세로 최대 길이

    // ---------------------------------------------------------------------------------------------
    //  블록체인 인증
    // ---------------------------------------------------------------------------------------------
    public static final String CREDENTIAL_FOLDERPATH    = Environment.getExternalStorageDirectory().getPath() + "/.postinsu";
    public static final String CREDENTIAL_FILEPATH      = Environment.getExternalStorageDirectory().getPath() + "/.postinsu/.user_credential";
    public static final String CREDENTIAL_KEY_FILEPATH  = Environment.getExternalStorageDirectory().getPath() + "/.postinsu/.authkey";
    public static final String ORGANIZATION_DID         = "did:krpost:549dba3c25eadd3645870b6930da2d5e";
    public static final String CLAIM_CHANNEL_NAME       = "blockchainclaimchannel";

    // ---------------------------------------------------------------------------------------------
    //  mVaccine
    // ---------------------------------------------------------------------------------------------
    public static final int mVaccineMsgId_1 = 12345; // mVaccine에서 사용하는 msg notification id
    public static final int mVaccineMsgId_2 = 123456; // mVaccine에서 사용하는 msg notification id
    public static final String mVaccineReceiverAction = "com.TouchEn.mVaccine.b2b2c.FIRE";

    // ---------------------------------------------------------------------------------------------
    //  App 실행에 필요한 필수 퍼미션 모음
    // ---------------------------------------------------------------------------------------------
    public static final  String[] defaultPermissions = new String[]{ Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_PHONE_STATE};

    public static String[] getDefaultPermission(){

            return defaultPermissions;


    }

    public static final String BROADCAST_MAIN            = "broadcastMain";



    public static final String BROADCAST_APPLOGINSUCCESS             = "appLoginSuccess";

    /**
     * 공동인증 솔루션 Debug Level<br/>
     * 0    : 아무것도 남기지 않는다.<br/>
     * 100  : 모든 Debug 로그를 남긴다.<br/>
     * 200  : Warning 이상의 Debug Log를 남긴다.<br/>
     * 300  : Error 이상의 Debug Log를 남긴다.<br/>
     */
    public static final int xSignDebugLevel = 0;

    public static final int REQUESTCODE_VACCINE              = 0;
    public static final int REQUESTCODE_ACTIVITY_CHOICE_BANK = 1;      // 금융기관 선택
    public static final int REQUESTCODE_ACTIVITY_IUII33M00   = 2;      // 추천국/추천인 선택
    public static final int REQUESTCODE_ACTIVITY_IUCOC2M00   = 3;      // 스마트보험 서비스 신청
    public static final int REQUESTCODE_ACTIVITY_IUII10M00_S = 4;      // 보험금청구접수신청 구비서류 보완

    // 인증
    public static final int REQUESTCODE_ACTIVITY_IUCOB0M00   = 10;      // 인증방법선택
    public static final int REQUESTCODE_ACTIVITY_IUCOC0M00   = 11;      // 공동인증(로그인)
    public static final int REQUESTCODE_ACTIVITY_IUPC80M00   = 12;      // 공동인증(전자서명)
    public static final int REQUESTCODE_ACTIVITY_IUPC95M00   = 13;      // 카카오페이인증(최초)
    public static final int REQUESTCODE_ACTIVITY_IUCOK0M00   = 14;      // 카카오페이인증(for csno)
    public static final int REQUESTCODE_ACTIVITY_IUFC00M00   = 15;      // FIDO(간편인증) 인증
    public static final int REQUESTCODE_ACTIVITY_IUFC10M00   = 16;      // FIDO(간편인증) 등록
    public static final int REQUESTCODE_ACTIVITY_IUPC39M00   = 17;      // FIDO(간편인증) 등록 완료화면
    public static final int REQUESTCODE_ACTIVITY_IUPC34M00   = 18;      // FIDO(개인인증번호) 재등록
    public static final int REQUESTCODE_ACTIVITY_IUPC70M00   = 19;      // 추가인증(SMS/ARS) 완료
    public static final int REQUESTCODE_ACTIVITY_IUPC90M00   = 20;      // 휴대폰인증(1회로그인/전자서명)
    public static final int REQUESTCODE_ACTIVITY_IUPC95M90   = 22;      // 카카오페이인증
    public static final int REQUESTCODE_ACTIVITY_IUPC10M00   = 23;      // 금융인증(로그인/전자서명)
    public static final int REQUESTCODE_ACTIVITY_IUPC20M00   = 24;      // PASS인증(로그인/전자서명)

    // 기타
    public static final int REQUESTCODE_ACTIVITY_WEBVIEW     = 50;      // 웹뷰
    // 블록체인
    public static final int REQUESTCODE_ACTIVITY_IUBC02M00   = 121;      // 블록체인 간편인증
//    public static final int REQUESTCODE_ACTIVITY_IUBC02M01   = 122;      // 블록체인 간편인증 등록 완료
//    public static final int REQUESTCODE_ACTIVITY_IUBC03M00   = 123;      // 블록체인 간편인증 약관 및 개인정보 이용동의
//    public static final int REQUESTCODE_ACTIVITY_IUBC04M01   = 124;      // 블록체인 비밀번호 인증
//    public static final int REQUESTCODE_ACTIVITY_IUBC05M01   = 125;      // 블록체인 지문 인증
//    public static final int REQUESTCODE_ACTIVITY_IUBC06M01   = 126;      // 블록체인 패턴 인증
//    public static final int REQUESTCODE_ACTIVITY_IUBC04M00   = 127;      // 블록체인 비밀번호 등록
//    public static final int REQUESTCODE_ACTIVITY_IUBC05M00   = 128;      // 블록체인 지문 등록
//    public static final int REQUESTCODE_ACTIVITY_IUBC06M00   = 129;      // 블록체인 패턴 등록
//    public static final int REQUESTCODE_ACTIVITY_IUBC10M00   = 130;      // 스마트보험금청구
    public static final int REQUESTCODE_ACTIVITY_IUBC11M00   = 131;      // 스마트보험금청구 병원통합서버 연계

    // 모듈호출
    public static final int REQUESTCODE_MODULE_PSMobileActivity = 100;   // 사진촬영 모듈
    public static final int REQUESTCODE_ACTIVITY_IUII21S01      = 101;   // 사진촬영 시 주의사항

    // ---------------------------------------------------------------------------------------------
    //  URL
    // ---------------------------------------------------------------------------------------------
    public static final String URL_APP_INIT = "/CO/IUCOA0M00.do";  // 배너 파일 및 초기 값 요청
    public static final String URL_WEB_LINK = "/CO/IUCOA1M00.do";  // 웹 페이지 요청 url
    public static final String URL_WEBVIEW_PERSONAL = "/CO/IUCOG0M02.do"; // 개인정보 이용안내 url

    // -- 인증(로그인)
    public static final String URL_FIDO_AUTH1   = "/CO/IUCOD0M50.do"; // FIDO 인증 선거래
    public static final String URL_FIDO_AUTH2   = "/CO/IUCOD0M51.do"; // FIDO 인증 본거래
    public static final String URL_FIDO_REG1    = "/PC/IUPC32M50.do"; // FIDO 등록 선거래
    public static final String URL_FIDO_REG2    = "/PC/IUPC32M51.do"; // FIDO 등록 본거래
    public static final String URL_FIDO_CANCEL1 = "/PC/IUPC40M50.do"; // FIDO 해지 선거래
    public static final String URL_FIDO_CANCEL2 = "/PC/IUPC40M51.do"; // FIDO 해지 본거래(PIN/패턴)


    public static final String URL_CERT_REG_APP_REQ   = "/CO/IUCOC2M00.do"; // 스마트보험서비스 신청 및 로그인 요청(APP)
    public static final String URL_CERT_REG_WEB_REQ   = "/CO/IUCOC2M10.do"; // 스마트보험서비스 신청 및 로그인 요청(WEB)
    public static final String URL_CERT_LOGIN_APP_REQ = "/CO/IUCOC1M00.do"; // 공동인증서 로그인(APP)
    public static final String URL_CERT_LOGIN_WEB_REQ = "/CO/IUCOC1M10.do"; // 공동인증서 로그인(WEB)
    public static final String URL_CERT_SIGN_APP_REQ  = "/PC/IUPC81M00.do"; // 공동인증서 전자서명(APP)
    public static final String URL_CERT_SIGN_WEB_REQ  = "/PC/IUPC81M10.do"; // 공동인증서 전자서명(WEB)

    public static final String URL_CUSTOMER_INFO = "/CO/IUCOD2M50.do";   // 고객정보조회
    public static final String URL_PUSH_UPDATE = "/CO/IUCOXXM00.do";   // 푸쉬 정보 업데이트

    public static final String URL_KAKAO_REG_AUTH1 = "/PC/IUPC95M00.do"; // 카카오페이 인증 요청(최초등록 로그인)(APP)
    public static final String URL_KAKAO_REG_AUTH2 = "/PC/IUPC95M01.do"; // 카카오페이 인증 확인(로그인)(APP)
    public static final String URL_KAKAO_SIGN_AUTH1 = "/PC/IUPC95M20.do"; // 카카오페이 인증 요청(등록이후 로그인/전자서명)(APP)
    public static final String URL_KAKAO_SIGN_AUTH2 = "/PC/IUPC95M21.do"; // 카카오페이 인증 확인(전자서명)(APP)

    public static final String URL_SMS_REQ  = "/CO/IUCO05M04_REQU.do"; // SMS 인증번호 요청
    public static final String URL_SMS_CNFM = "/CO/IUCO05M04_CNFM.do"; // SMS 인증번호 확인
    public static final String URL_ARS_REQ  = "/CO/IUCO05M03_REQU.do"; // ARS 인증번호 요청
    public static final String URL_ARS_CNFM = "/CO/IUCO05M03_CNFM.do"; // ARS 인증번호 확인

    public static final String URL_MOBILE_AUTH1 = "/PC/IUPC92M01.do"; // 휴대폰인증(SMS) 요청 ([AES256]휴대폰번호,생년월일,성별)
    public static final String URL_MOBILE_AUTH2 = "/PC/IUPC91M00.do"; // 휴대폰인증(SMS) 확인

    public static final String URL_FINCERT_AUTH = "/PC/IUPC10M00.do"; // 금융인증서 인증(웹뷰)
    public static final String URL_PASS_AUTH    = "/PC/IUPC20M00.do"; // PASS인증서 인증(웹뷰)

    public static final String URL_TEMP_AUTH = "/CO/IUCOC4M00.do"; // 개발 임시 고객전환 인증

    // -- 청구
    public static final String URL_CLAIM_REQ = "/II/IUII40M00.do"; // 본인 보험금청구 접수 요청
    public static final String URL_CLAIM_MOBILE_REQ = "/II/IUII40M02.do"; // 본인 보험금청구(휴대폰인증) 접수 요청
    public static final String URL_CLAIM_CHILD_REQ = "/II/IUII41M00.do"; // 자녀 보험금청구 접수 요청
    public static final String URL_CLAIM_DOC_REQ = "/II/IUII40M01.do"; // 구비서류 보완 요청

    public static final String URL_EVENT_INQ = "/AS/IUAS21M00.do"; // 보험금청구 이벤트 유무 조회


    public static final String URL_CLAIM_CERT_LIST_INQ      = "/II/IUII50M00.do"; // 보험금청구 목록조회(공동인증 등)
    public static final String URL_CLAIM_MOBILE_LIST_INQ    = "/II/IUII50M01.do"; // 보험금청구 목록조회(휴대폰인증)
    public static final String URL_CLAIM_CERT_DETAIL_INQ    = "/II/IUII51M00.do"; // 보험금청구 상세조회(공동인증 등)
    public static final String URL_CLAIM_MOBILE_DETAIL_INQ  = "/II/IUII51M01.do"; // 보험금청구 상세조회(휴대폰인증)
    public static final String URL_CLAIM_IMAGE_INQ          = "/II/IUII60M00.do"; // 보험금청구 서류이미지 조회
    public static final String URL_CLAIM_CANCLE_CERT_REQ    = "/II/IUII52M00.do"; // 보험금청구취소 요청(공동인증 등)
    public static final String URL_CLAIM_CANCLE_MOBILE_REQ  = "/II/IUII52M01.do"; // 보험금청구취소 요청(휴대폰인증)
    public static final String URL_CLAIM_SELF_CERT_USER_INFO_INQ    = "/II/IUII12M00.do"; // 본인청구 청구자 고객조회(공동인증)
    public static final String URL_CLAIM_SELF_MOBILE_USER_INFO_INQ  = "/II/IUII12M01.do"; // 본인청구 청구자 고객조회(휴대폰인증)
    public static final String URL_CLAIM_CHILD_CERT_USER_INFO_INQ   = "/II/IUII12M10.do"; // 자녀청구 청구자 고객조회
    public static final String URL_CLAIM_CHILD_MOBILE_USER_INFO_INQ = "/II/IUII12M11.do"; // 자녀청구 자녀 고객조회
    public static final String URL_BANK_INFO_REQ = "/II/IUII30M00.do"; // 고객이 입력한계좌 정합성 체크 url

    public static final String URL_FILE_AGREE1 = "file:///android_asset/req_agree_html/agree_1.html"; // 이용약관
    public static final String URL_FILE_AGREE2 = "file:///android_asset/req_agree_html/agree_2.html"; // 이용약관
    public static final String URL_FILE_AGREE3 = "file:///android_asset/req_agree_html/agree_3.html"; // 이용약관
    // ---------------------------------------------------------------------------------------------
    //  ENUM
    // ---------------------------------------------------------------------------------------------
    /**
     * 로그인 유형
     * <pre>
     *  UNCERTI   00:미인증
     *  CERTI     01:공동인증
     *  PINGER    02:지문인증
     *  FACE      03:얼굴인증
     *  PIN       04:핀(개인인증번호)인증
     *  PATTERN   05:패턴인증
     *  KAKAOPAY  06:카카오페이인증
     *  MOBILE    07:휴대폰인증
     *  FINCERT   08:금융인증서
     *  SMARTBANK 51:스마트뱅킹연계
     * </pre>
     */
    public enum AuthDvsn {
        UNCERTI, CERT, PINGER, PIN, FACE, PATTERN, KAKAOPAY, MOBILE, FINCERT, PASS, SMARTBANK;

        /**
         * 인증유형 조회(by FIDO기술코드)
         * @param fidoCode  String
         * @return          AuthDvsn
         */
        public static AuthDvsn getAuthDvsnByFido(String fidoCode) {
            AuthDvsn rtnCode = null;
            switch (fidoCode) {
                case "100": rtnCode = AuthDvsn.PINGER; break;
                case "116": rtnCode = AuthDvsn.PIN; break;
                case "121": rtnCode = AuthDvsn.PATTERN; break;
            }
            return rtnCode;
        }

        /**
         * FIDO기술코드 조회(by 인증유형)
         * @param dvsn  AuthDvsn
         * @return      String
         */
        public static String getFidoByAuthDvsn(AuthDvsn dvsn) {
            String rtnCode = "";
            switch (dvsn) {
                case PINGER : rtnCode = Fido2Constant.AUTH_TECH_FINGER; break;
                case PIN    : rtnCode = Fido2Constant.AUTH_TECH_PIN; break;
                case PATTERN: rtnCode = Fido2Constant.AUTH_TECH_PATTERN; break;
            }
            return rtnCode;
        }

        /**
         * 로그인 유형 번호 조회
         * @param   dvsn    AuthDvsn
         * @return          String
         */
        public static String getAuthDvsnNum(AuthDvsn dvsn) {
            String rtnNum = "00";
            switch (dvsn) {
                case CERT     : rtnNum = "01"; break;
                case PINGER   : rtnNum = "02"; break;
                case FACE     : rtnNum = "03"; break;
                case PIN      : rtnNum = "04"; break;
                case PATTERN  : rtnNum = "05"; break;
                case KAKAOPAY : rtnNum = "06"; break;
                case MOBILE   : rtnNum = "07"; break;
                case FINCERT  : rtnNum = "08"; break;
                case PASS     : rtnNum = "09"; break;
                case SMARTBANK: rtnNum = "51"; break;
            }
            return rtnNum;
        }

        /**
         * 로그인 유형 조회
         * @param   dvsnNum String
         * @return          AuthDvsn
         */
        public static AuthDvsn getAuthDvsn(String dvsnNum) {
            AuthDvsn rtnNum = AuthDvsn.UNCERTI;
            switch (dvsnNum) {
                case "01": rtnNum = CERT     ; break;
                case "02": rtnNum = PINGER   ; break;
                case "03": rtnNum = FACE     ; break;
                case "04": rtnNum = PIN      ; break;
                case "05": rtnNum = PATTERN  ; break;
                case "06": rtnNum = KAKAOPAY ; break;
                case "07": rtnNum = MOBILE   ; break;
                case "08": rtnNum = FINCERT  ; break;
                case "09": rtnNum = PASS     ; break;
                case "51": rtnNum = SMARTBANK; break;
            }
            return rtnNum;
        }
    }

    /**
     * 인증요청 유형
     *  LOGIN_APP   앱 로그인 요청
     *  LOGIN_WEB   웹 로그인 요청
     *  SIGN_APP    앱 전자서명 요청
     *  SIGN_WEB    웹 전자서명 요청
     */
    public enum AuthMode{
        LOGIN_APP, LOGIN_WEB, SIGN_APP, SIGN_WEB;

        /**
         * 인증 유형이 로그인인지 조회
         * @param   authMode    AuthMode
         * @return              boolean  (true:로그인)
         */
        public static boolean isLogin(AuthMode authMode) {
            boolean rtnBool = false;
            if(authMode == EnvConfig.AuthMode.LOGIN_APP || authMode == EnvConfig.AuthMode.LOGIN_WEB) {
                rtnBool = true;
            }
            return rtnBool;
        }
    }

    /**
     * 청구유형
     * <pre>
     *  SELF      본인청구
     *  CHILDE    자녀청구
     *  INQUERY   지급조회
     * </pre>
     */
    public enum ReqDvsn {
        SELF, CHILDE, INQUERY
    }

    // ---------------------------------------------------------------------------------------------
    // 암복호화
    // ---------------------------------------------------------------------------------------------
    //  Transkey (보안 키패드)
    public static final byte[] mTransServerKey = { 'M', 'o', 'b', 'i', 'l', 'e', 'S', 'u', 'r', 'a', 'n', 's', '2' ,'0','1','7'};

    public static final int SYMMETRIC_KEY_SIZE = 32;
    public static final String SYMMETRIC_KEY = "422d3951ef007cc32e2f714ccb6ff8dc";

    // ---------------------------------------------------------------------------------------------
    // 배열정보
    // ---------------------------------------------------------------------------------------------
    // 일반청구
    public static final String[] reqCategoryCode = {"1","2","3"}; // 보험금청구 청구유형
    public static final String[] reqCategoryName = {"정액", "실손", "정액+실손"};
    public static final String[] reqReasonCode = {"1","2","3"}; // 보험금청구 발생원인
    public static final String[] reqReasonName = {"상해(재해)","질병","기타"};
    //public static final String[] reqTypeCode = {"48","49","46","45","50","47","53","52","51"}; // 보험금청구 청구사유 코드(44:사망코드 제외)
    //public static final String[] reqTypeName = {"입원","수술","진단","장해","통원","골절","요양","치아","깁스"}; // 보험금청구 청구사유명(44:사망코드 제외)
    public static final String[] reqTypeCode = {"45","46","49","48","50","53","52","47",/*"51",*/"68"}; // 보험금청구 청구사유 코드(44:사망코드 제외) NJM_20210601수정
    public static final String[] reqTypeName = {"장해","진단","수술","입원","통원","요양","치아","골절",/*"깁스",*/"기타"}; // 보험금청구 청구사유명(44:사망코드 제외) NJM_20210601수정

    public static final String[] reqStateCode = {"00","01","02","03","04","05","06","07","08","09","10","11","12"};

    // 지급진행조회 상세화면 (심사진행결과-상태에 표기)
    public static final String[] reqStateName = {"접수","청구등록","심사접수완료","조사의뢰","조사접수완료","조사완료","심사완료(지급)","심사완료(지급거절)","심사완료(서류반송)","심사완료(해지)","심사완료(암부담보 지급거절)","심사완료(자동심사반송)","심사완료(보장개시일이전거절)"};
    // 목록화면(아이콘 하위에 표기)
    public static final String[] reqStateName_1 = {"접수","청구등록","심사접수\n완료","조사의뢰","조사접수\n완료","조사완료","심사완료","심사완료","심사완료","심사완료","심사완료","심사완료","심사완료"};
    public static final String[] reqStateName_2 = {"","","","","","","(지급)","(지급거절)","(서류반송)","(해지)","(지급거절)","(자동심사반송)","(지급거절)"};

    // 스마트청구
    public static final String[] smartReqRealCategoryCode = {"1","2","3"}; // 청구유형
    public static final String[] smartReqRealCategoryName = {"정액","실손","정액+실손"}; // 청구유형
    public static final String[] smartReqReasonCode = {"2"}; // 보험금청구 발생원인(질병)
    public static final String[] smartReqReasonName = {"질병"}; // 보험금청구 발생원인(질병)
    public static final String[] smartReqTypeCode = {"50"}; // 보험금청구 청구사유 코드(50:통원)
    public static final String[] smartReqTypeName = {"통원"}; // 보험금청구 청구사유명(통원)

    // 자녀보험금청구
    public static final String[] reqChildRelationName = {"계약자", "입원(장해)수익자"}; // 계약관계유형
    public static final String[] reqChildRelationCode = {"30", "42"}; // 계약관계유형 (30:계약자, 42:입원(장해)수익자)
    public static final String[] reqChildParentTypeName = {"부", "모"}; // 자녀와의 관계
    public static final String[] reqChildParentTypeCode = {"1", "2"}; // 자녀와의 관계
    public static final String[] reqChildParentAgreeName = {"예","아니오"}; // 친권행사 동의 여부
    public static final String[] reqChildParentAgreeCode = {"1","2"}; // 친권행사 동의 여부
    public static final String[] reqChildParentAloneReasonName = {"이혼","사별","기타"}; // 단독친권행사 사유
    public static final String[] reqChildParentAloneReasonCode = {"2","3","4"}; // 단독친권행사 사유

    // ---------------------------------------------------------------------------------------------
    // WEB to APP요청 scheme
    // ---------------------------------------------------------------------------------------------
    // 로그인
    public static final String webHost_logOut     = "logOut"; // 로그아웃
    public static final String webHost_reqCert    = "reqCertLogin"; // 공동인증
    public static final String webHost_reqFido    = "reqFidoLogin"; // 지문인증
    public static final String webHost_reqPin     = "reqPinLogin"; // 핀인증
    public static final String webHost_reqPattern = "reqPatternLogin"; // 패턴인증



    // 전자서명(본인확인)
    public static final String webHost_checkCert   = "checkCert"; // 공동인증서
    public static final String webHost_singPin     = "reqPinSign"; // 핀인증
    public static final String webHost_singBio     = "reqBioSign"; // 지문인증
    public static final String webHost_singPattern = "reqPatternSign"; // 패턴인증

    // 청구
    public static final String webHost_pay      = "reqPay"; // 본인보험금청구
    public static final String webHost_child    = "reqChild"; // 자녀보험금청구
    public static final String webHost_search   = "reqSearch"; // 청구내역조회
    public static final String webHost_smartPay = "smartReqPay"; // 스마트보험금청구
    public static final String webHost_smartPayProc = "smartReqPayProc"; // 스마트보험금청구절차

    public static final String webHost_manageCert = "mngCert"; // 공동인증서 관리
    public static final String webHost_manageFido = "mngFido"; // 간편인증 관리
}
