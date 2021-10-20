package com.epost.insu.e2e;

import com.epost.insu.EnvConfig;
import com.extrus.exafe.config.exafeKeysecConfig;

/**
 * @copyright : (주)프로니스정보기술 | http://www.phronis.co.kr
 *
 * @project   : 모바일슈랑스 구축
 * @pakage   : com.epost.insu.e2e
 * @fileName  : exafeConfig.java
 *
 * @Title     :
 * @author    : 이수행
 * @created   : 2017-11-17
 * @version   : 1.0
 *
 * @note      : <u></u><br/>
 * ======================================================================
 * 수정 내역
 * NO      날짜          작업자       내용
 * 01      2017-11-17    이수행       최초 등록
 * =======================================================================
 */
class exafeConfig {
    /*########################################################################################################*/
    //# 라이센스 설정
    //# (주의 사항)
    //# 고객사에 배포되는 라이선스로 반듯이 설정을 해주셔야 기능 동작이 정상적으로 됩니다.
    /*########################################################################################################*/
    /*########################################################################################################*/
    //# 보안키패드 설정
    //# (주의 사항)
    //# 해당 설정은 보안키패드의 종류를 세팅해 주는 부분으로 고객사에서 구매 시 협의된 타입으로 설정 해 주셔야 합니다.
    //#  KEYSEC_ENTERPRISE_MODE : encryption된 암호 결과 값
    //#   KEYSEC_STANDARD_MODE, KEYSEC_LITE_MODE 타입으로도 설정이 가능합니다.
    //#  KEYSEC_STANDARD_MODE : Hash 결과 값
    //#   KEYSEC_LITE_MODE 타입으로도 설정이 가능합니다.
    //#  KEYSEC_LITE_MODE : 평문 결과 값
    /*########################################################################################################*/
    // 모드 설정
    public static int mKeysecMode = exafeKeysecConfig.KEYSEC_ENTERPRISE_MODE;

    /*########################################################################################################*/
    //# 앱위변조 설정
    /*########################################################################################################*/
    public static final String SERVICE_URL = EnvConfig.host_url+"/e2e/e2e";
    public static final String EVENT_TYPE = "1"; // Type : 1:`기기고유정보 해시, 2:ID/PWD
    
    // 다음 사항에 대해서는 고객사에서 따로 설정을 하지 않으셔도 됩니다.
    public static final int VERSION_CODE = 2; // 1: 검증 xml 변경 전 // 2: 검증 xml 변경 후
    public static final String DEFENCE_FILE_TYPE = "APK"; // 검증 모듈 타입
}
