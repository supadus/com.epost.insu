package com.epost.insu.fido;

import android.os.Bundle;

/**
 * Fido2 콜백 class
 * @since     : project 48:1.4.5
 * @version   : 1.0
 * @author    : NJM
 * <pre>
 * ======================================================================
 * 1.4.5    NJM_20201229    최초 등록
 * 1.5.2    NJM_20210322    [FIDO인증 로직 변경] onFailure 파라미터 Bundle -> String
 * =======================================================================
 * copyright : (주)프로니스정보기술 | http://www.phronis.co.kr
 * </pre>
 */
public interface Fido2Callback {
    void onReceiveMessage(String code, Bundle bundle);
    void onFailure(String code, String msg);
}
