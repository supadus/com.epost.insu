package com.epost.insu.fido;

import android.content.Context;
import android.os.Bundle;

import com.epost.insu.common.LogPrinter;

import kr.or.kftc.fido.api.KFTCBioFidoManager;
import kr.or.kftc.fido.api.OnCompleteListener;

/**
 * FIDO 조회 class
 * @since     : project 48:1.4.5
 * @version   : 1.1
 * @author    : NJM
 * <pre>
 * ======================================================================
 * 1.4.5    NJM_20201229    최초 등록
 * 1.5.3    NJM_20210330    kftcBioFidoManager 호출시 try catch 추가
 * 1.5.3    NJM_20210330    [FIDO호출 로직 변경] 에러발생으로인한 변경
 * 1.6.2    NJM_20210802    [2021년 대우 취약점] 2차본 : 부적절한 예외 처리
 * =======================================================================
 * copyright : (주)프로니스정보기술 | http://www.phronis.co.kr
 * </pre>
 */
public class Fido2RegistableAuthTech implements OnCompleteListener {
    private final Fido2Callback callback;                   // 화면
    private final Context context;

    public Fido2RegistableAuthTech(Context context, Fido2Callback pCallback) {
        this.callback = pCallback;
        this.context  = context;
    }

    /**
     * 디바이스 조회
     */
    public void process() {
        try {
            KFTCBioFidoManager kftcBioFidoManager = new KFTCBioFidoManager(context);
            kftcBioFidoManager.getPhoneInfo(this);
        } catch (NullPointerException e) {
            e.getMessage();
        } catch (Exception e) {
            e.getMessage();
        }
    }

    @Override
    public void onComplete(String resCode, Bundle resData) {
        LogPrinter.CF_debug("!----------------------------------------------------------");
        LogPrinter.CF_debug("!-- Fido2RegistableAuthTech.onComplete() --폰정보조회 완료");
        LogPrinter.CF_debug("!----------------------------------------------------------");
        LogPrinter.CF_debug("!---- resCode : " + resCode + " / resData : " + resData.toString() );
        /* 정상 리턴 값
            resCode : 0000
            resData : Bundle[{  DATA_KEY_AUTH_TECH_LIST=[100, 116, 121],
                                DATA_KEY_VERSION=0107,
                                DATA_KEY_DEVICE_ID=B9934A4D1767A39694581952BBB9ACCA15AEECE87F7BDF9CDF286A008F225348}]
        */

        if(Fido2Constant.FIDO_CODE_SUCCESS.equals(resCode)) {
            callback.onReceiveMessage( resCode, resData );
        }
        else {
            callback.onFailure( resCode, Fido2Constant.getErrMsgForCode(resCode) );
        }
    }
}
