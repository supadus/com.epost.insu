package com.epost.insu.fido;

/**
 * FIDO 사용에 필요한 상수 class
 * @since     : project 48:1.4.5
 * @version   : 1.1
 * @author    : NJM
 * <pre>
 * ======================================================================
 * 1.4.5    NJM_20201229    최초 등록
 * 1.5.2    NJM_20210322    [FIDO인증 로직 변경]
 * =======================================================================
 * copyright : (주)프로니스정보기술 | http://www.phronis.co.kr
 * </pre>
 */
public class Fido2Constant {
    // =============================================================================================
    //  Constant
    // =============================================================================================

    // ---------------------------------------------------------------------------------------------
    // -- Key 모음
    public static final String KEY_CODE             = "DATA_KEY_CODE";                              // 코드 키
//    public static final String KEY_ERROR_CODE = "DATA_KEY_ERROR_CODE";                            // 에러코드 키
//    public static final String KEY_DEPATURE_PKG_ID = "DATA_KEY_DEPARTURE_PKG_ID";                 // 시작 pkg id  키
    public static final String KEY_VERSION          = "DATA_KEY_VERSION";                           // 바이오인증앱 버전 키
    public static final String KEY_NIDCT            = "DATA_KEY_NIDCT";                             // 비식별번호 토큰 키
    public static final String KEY_NIDCT_CHALLENGE  = "DATA_KEY_NIDCT_CHALLENGE";                   // 호환토큰 Challenge 키(인증장치가 지문인 경우 필수, 비식별 호환토큰을 생성하기 위한 일회용 난수 값)
    public static final String KEY_AUTH_TECH_CODE   ="DATA_KEY_AUTH_TECH_CODE";                     // 인증기술코드 키
    public static final String KEY_FIDO             = "DATA_KEY_FIDO";                              // FIDO인증요청메시지 키(인증장치가 지문인 경우 필수, 공동FIDO인증정보생성 (200201) 통보 전문의 26번째 항목 값을 설정한다)
    public static final String KEY_PUB_KEY          = "DATA_KEY_PUBKEY";                            // 공개키 키
    public static final String KEY_PRV_KEY          = "DATA_KEY_PRIVKEY";                           // 개인키 키
    public static final String KEY_ENC_NIDCN = "DATA_KEY_ENC_NIDCN";                                // 비식별호환번호(암호화) 키
//    public static final String KEY_COMPATIBLE = "DATA_KEY_COMPATIBLE";                            // 호환가능 여부 키
//    public static final String KEY_NIDCN_SAVE_RESULT = "DATA_KEY_SAVE_NIDCN_RESULT";              // 비식별호환번호 저장 결과 키
    public static final String KEY_TLS_CERT         = "DATA_KEY_TLS_CERT";                          // TLS Certificate 키(인증장치가 지문인 경우 필수, 참가기관 앱과 서버 간 TLS(Transport Layer Security) 통신 시 사용하는 서버 인증서의 값이다. TLS용 서버 인증서를 사용하지 않는 경우 “MI”를 설정한다.)
    public static final String KEY_SITE_CODE        = "DATA_KEY_SITE_CODE";                         // 기관코드 키
    public static final String KEY_SVC_CODE         = "DATA_KEY_SVC_CODE";                          // 서비스 코드 키
    public static final String KEY_NEED_KEY         = "DATA_KEY_NEED_KEY";                          // 축약서명여부 키 (Y: 있음, N: 없음)
    public static final String KEY_DEVICE_ID        = "DATA_KEY_DEVICE_ID";                         // 디바이스 아이디 키
    public static final String KEY_AUTH_TECH_LIST   = "DATA_KEY_AUTH_TECH_LIST";                    // 인증기술 목록 키
    //    public static final String KEY_DEREG_RESULT = "DATA_KEY_DEREG_RESULT";                    // 해지결과 키
    public static final String KEY_AAID             = "DATA_KEY_AAID";                              // AAID 키
    public static final String  KEY_TRID           = "DATA_KEY_TRID";                               // 파이도용 추적번호(인증장치가 PIN/패턴인 경우 필수, 공동FIDO 2.0에서 사용하는 거래 추적번호로 참가기관과 서버 간 등록(300101), 등록결과(300102), 인증(300201), 인증결과(300202), 해지(300401), 해지결과(300402) 전문에서 동일한 값을 사용한다.)


    // ---------------------------------------------------------------------------------------------
    //  -- 코드 모음
    public static final String FIDO_CODE_SUCCESS        = "0000";                                   // 성공 코드(FIDO API)
    public static final String FIDO_CODE_ERROR          = "1000";                                   // 에러 코드(FIDO API)

    public static final String CALLBACK_FAIL            = "9000";                                   // 실패 코드(콜백시 사용)
    public static final String KEY_CALLBACK_MSG         = "FAIL_MSG";                               // 실패 MSG KEY(콜백시 사용)

    public static final int FIDO_CODE_CHECK_DEVICE_IN   = 401;                                      // 디바이스 정보 요청 코드
    public static final int FIDO_CODE_CHECH_DEVICE_OUT  = 402;                                      // 디바이스 정보 요청 결과 코드
    public static final int FIDO_CODE_REG_1_IN          = 2103;                                     // (등록 1단계) 등록 요청 코드
    public static final int FIDO_CODE_REG_1_OUT         = 2104;                                     // (등록 1단계)등록 요청 결과 코드

    public static final int FIDO_CODE_AUTH_IN           = 2115;                                     // 인증 요청 코드
    public static final int FIDO_CODE_AUTH_OUT          = 2116;                                     // 인증 요청 결과 코드

    public static final int FIDO_CODE_CANCEL_IN         = 2121;                                     // 해지 요청 코드
    public static final int FIDO_CODE_CANCEL_OUT        = 2122;                                     // 해지 요청 결과 코드

    // ---------------------------------------------------------------------------------------------
    //  -- 에러 키 모음
//    public static final String ERROR_FIDO_NO_REG_FINGER = "-1206";                          // 단말기에 등록되어 있는 지문이나, 바이오인증앱 미등록 지문으로 인증 시도 시 발생 (바이오인증 테스트앱에서 발생)
//    public static final String ERROR_FIDO_NOT_ENROLLED_FINGER = "-1260";                   // 지문 재등록 필요(운영용 바이오 인증앱 지문 등록 후 단말기 지문 변경(추가) 시 발생)
//    public static final String ERROR_FIDO_USER_CANCELED = "-1203";                          // 사용자가 FIDO 인증을 취소함
//    public static final String ERROR_FIDO_UNKNOWN = "-1290";                                 // 정의되지 않은 FIDO 에러(사용자가 FIDO 인증 취소 시 발생할 수 있음)
//    public static final String ERROR_FIDO_NOT_VALID_RESPONSE = "-1270";                    // 정상적이지 않은 FIDO 응답메시지
//    public static final String ERROR_FIDO_UNSUPPORTED_VERSION = "-1204";                   // 지원하지 않는 FIDO 버전

    // ---------------------------------------------------------------------------------------------
    // -- 고정 Value 모음
    public static final String OS_TYPE              = "1";      // OS Type (1, 안드로이드, 2,iOS, 9,기타)
    public static final String AUTH_TECH_FINGER     = "100";    // 지문인증 기술코드
    public static final String AUTH_TECH_PIN = "116";    // 핀인증 기술코드
    public static final String AUTH_TECH_PATTERN    = "121";    // 패턴인증 기술코드
//    public static final String AUTH_TECH_EYE        = "104";    // 홍채인증 기술코드(미사용)

    public static final String TLS_CERTIFICATE = "MI";     // 우정사업부는 해당 기능을 사용하지 않음(MI 고정)
    public static final String SITE_CODE       = "00071";  // 우체국 기관코드(고정)
    public static final String SVC_CODE        = "005";    // 서비스 코드(고정) : 모바일슈랑스인앱 ex)스뱅:002
//    public static final String RGTN_CODE       = "02";     // 등록방식 코드(01 : SMS, 02 : 인증서, 03 : OTP)
//    public static final String SGNT_TYPE       = "0";      // 축약서명타입(0:일반, 1:축약/자동 ::: 0 고정)
//    public static final String AUTH_TYPE_DVSN  = "S";      // 인증타입 구분(S: 자사인증 ::: 고정)
//    public static final String TX_TYPE_DVSN    = "0";      // 거래타입 구분(0:인증, 1:서명-내용포함, 2:서명-내용미포함 ::: 0 고정)

    // ---------------------------------------------------------------------------------------------


    /**
     * 에러메시지 호출
     * @param resCode   String
     * @return          String
     */
    public static String getErrMsgForCode(String resCode) {
        String msg = "오류가 발생하였습니다. \n잠시 후에 다시 시도해주세요. [" + resCode + "]";

        switch (resCode) {
            case "-1101": // 정의되지 않은 코드
            case "-2002": // 네트워크 오류
            case "-2001": // 데이터 오류
            case "-1102": // 잘못된 FIDO 메시지
            case "-1103": // 잘못된 채널 바인딩
            case "-1104": // 잘못된 참가기관 코드
            case "-1105": // 잘못된 서비스 코드
            case "-1106": // 잘못된 호환토큰 Challenge
            case "-1107": // 잘못된 AAID
            case "-1108": // 잘못된 TLS Certificate
            case "-1109": // 잘못된 비식별호환번호|AAID
            case "-1110": // 잘못된 비식별호환번호(암호화)
            case "-1111": // 호한가능 여부가 'Y' 또는 'N' 이 아님
            case "-1112": // 잘못된 호환패키지아이디
            case "-1113": // 잘못된 인증기술코드
            case "-1114": // 잘못된 축약서명 키쌍 필요여부
            case "-1207": // 신뢰하지 않는 Facet ID
            case "-1250": // 잘못된 FIDO 메세지
            case "-1252": // AAID 추출 실패
            case "-1270": // 정상적이지 않은 FIDO 응답메시지 (안드로이드 전용)
            case "-1301": // 키쌍 생성 실패
            case "-1403": // DB 저장 실패
            case "-1601": // 보관기관 앱에서 인증 취소
            case "-1602": // 보관기관 앱에서 인증 응답메시지 생성 실패
            case "-1603": // 허용되지 않은 보관기관 앱
            case "-1604": // 보관기관 앱에서 잘못된 FIDO 메시지 전송
            case "-1605": // 호환인증 성공여부가 "Success" "Fail" "Canceled" 가 아님
            case "-2000": // 데이터 파싱 오류
            case "-2003": // FIDO 서버 오류
            case "-2004": // WrapKey를 찾을 수 없는 상태(안드로이드)
            case "-2005": // FIDO 서버 내부 오류
            case "-2007": // 토큰 오류
            case "-2008": // 인증서 오류
            case "-2202": // DB오류
            case "-2205": // 필수 파라미터 오류
                msg = "오류가 발생하였습니다.\n잠시 후 다시 거래하세요.";
                break;

            case "-1203": // 사용자가 FIDO 인증(지문)을 취소 또는 OS 지문 lock
            case "-1290": // 정의되지 않은 FIDO 에러
                msg = "거래가 취소되었습니다.";
                break;

            case "-1204": // 지원하지 않는 FIDO 버전
                msg = "바이오인증 공동앱을 업데이트 해주세요.";
                break;
            case "-1205": // 인증장치를 찾을 수 없음
                msg = "인증정보가 일치 하지 않습니다. \n단말기 변경 또는 앱 재설치시 간편인증 재등록 후 거래하여 주세요.";
                break;
            case "-1260": // 지문 미등록 및 지문 등록 정보 변경
                msg = "지문이 등록되어 있지 않습니다.\n지문등록 후 거래하여 주세요.\n\n인증센터 > 간편인증관리";
                break;
            case "-1302": // 비식별호환토큰 생성 실패
                msg = "등록된 바이오정보와 일치하지 않습니다.\n재등록 후 거래하여 주세요.\n\n인증센터 > 간편인증관리";
                break;
            case "-1401": // DB 삭제 실패
                msg = "바이오 인증 해지에 실패하였습니다.\n고객센터(1588-1900)에 문의하여 주세요.";
                break;
            case "-1402": // DB 읽기 실패
                msg = "오류가 발생하였습니다.\n간편인증 재등록이 필요합니다.";
                break;
            case "-1501": // 루팅된 단말 (안드로이드 전용)
                msg = "단말 상태(루팅 등)를 확인해주세요.";
                break;

            case "-1700": // 업데이트 대상
            case "-1701": // 버전 정보 오류(미사용 버전입)
                msg = "바이오인증을 지원하지 않는 버전입니다.\n앱을 업데이트 해주세요.";
                break;

            case "-2006": // 지원하지 않는 단말
                msg = "바이오인증을 지원하는 단말이 아닙니다.";
                break;
            case "-2009": // 인증 실패 및 오류 횟수 초과 (횟수초과는 서버에러 EEF174 참조)
                msg = "등록된 인증정보와 일치하지 않습니다.\n확인 후 다시 거래하세요.";
                break;
            case "-2010": // 생체정보 미등록
                msg = "등록 후 다시 거래하여 주세요.";
                break;
            case "-2020": // 단말 암호 끄기 상태, Face ID 미등록 상태 등
                msg = "단말 암호 끄기 상태; FaceId 미등록 상태";
                break;
            case "-2021": // Face ID 재설정과 같은 인증정보 변경
                msg = "FaceId 재설정으로 인증정보 변경 되었습니다.\n등록 후 다시 거래하여 주세요.";
                break;
            case "-2022": // Face ID 입력 횟수 초과
                msg = "FaceId 입력 횟수를 초과하였습니다.\n잠시 후 다시 거래하세요.";
                break;
            case "-2023": // 초기화 설정 오류
                msg = "초기화 설정 오류입니다.\n앱을 재실행해 주세요.";
                break;
            case "-2024": // 단말 암호 비활성화 상태
                msg = "단말 암호 비활성화 상태입니다.";
                break;
            case "-2201": // 서비스 개시 이전
                msg = "서비스 개시 이전입니다.\n잠시 후 다시 거래하세요.";
                break;
            case "-2203": // 처리지연
                msg = "처리가 지연되고 있습니다.\n잠시 후 다시 거래하세요.";
                break;
            case "-2204": // 앱 무결성 오류
                msg = "무결성 오류입니다.\n앱을 다시 설치하시기 바랍니다.";
                break;
            case "-2206": // 생체정보 삭제로 인한 등록 필요
                msg = "바이오인증 정보가 삭제되었습니다.\n재등록 후 거래하세요.\n\n인증센터 > 간편인증 관리";
                break;
            case "-2207": // 인증 시간 초과
                msg = "인증시간이 초과되었습니다.n잠시 후 다시 거래하세요.";
                break;
            case "-2208": // 라이센스 에러
                msg = "라이센스 오류입니다.\n앱을 다시 설치하시기 바랍니다.";
                break;
            case "-2999":
                msg = "기타오류";
                break;

            // 자체 오류 코드 지정
            case "-5000":    // FIDO생성 실패 // 데이터 오류
                msg = "간편인증모듈 생성에 실패하였습니다.\n앱을 종료 후 다시 실행해주시기 바랍니다.";
                break;
        }
        return msg;
    }
}
