package com.epost.insu.e2e;

import android.content.Context;
import android.os.Message;
import android.os.StrictMode;

import com.epost.insu.EnvConfig;
import com.epost.insu.activity.IUCOA0M00;
import com.epost.insu.network.WeakReferenceHandler;
import com.extrus.exafe.appdefence.DefenceApiImpl;
import com.extrus.exafe.common.CommonAPIManager;
import com.extrus.exafe.common.exception.ExafeException;
import com.extrus.exafe.common.log.LogManager;

/**
 * @copyright : (주)프로니스정보기술 | http://www.phronis.co.kr
 *
 * @project   : 모바일슈랑스 구축
 * @pakage   : com.epost.insu.e2e
 * @fileName  : AppDefenceManager.java
 *
 * @Title     : 앱위변조 검사 솔루션 AppDefence
 * @author    : 이수행
 * @created   : 2017-11-17
 * @version   : 1.0
 *
 * @note      : <u>앱위변조 검사 솔루션 AppDefence </u><br/>
 *               앱위변조 검사 솔루션 초기 설정 및 실행
 * ======================================================================
 * 수정 내역
 * NO      날짜          작업자       내용
 * 01      2017-11-17    이수행       최초 등록
 * =======================================================================
 */
public class AppDefenceManager {

    private WeakReferenceHandler mHandler = null;

    static {
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
    }

    public AppDefenceManager(Context context, WeakReferenceHandler handler) {
        // Context 설정
        CommonAPIManager.setActivityContext(context);

        // Handler 설정
        mHandler = handler;
    }

    public void initAppDefennce() {
        LogManager.debug("initAppDefennce Start!!!");

        // 라이선스 세팅
        CommonAPIManager.setLicenseCode(EnvConfig.appDefenceLicenseCode);

        // 디버그 모드 적용
        CommonAPIManager.setDebuggingMode(false);

        // Server 세팅
        CommonAPIManager.setAppDefenceIP(exafeConfig.SERVICE_URL);

        // Version 세팅
        CommonAPIManager.setAppDefenceVersion(exafeConfig.VERSION_CODE);

        // 앱위변조 로그인 타입 세팅 함수
        CommonAPIManager.setEventType(exafeConfig.EVENT_TYPE);

        // 앱위변조 검증 타입 세팅 함수
        CommonAPIManager.setDefenceFileType(exafeConfig.DEFENCE_FILE_TYPE);
    }

    public void appDefenceCallApi() {
        if (mHandler != null) {
            DefenceApiImpl DefenceApi = new DefenceApiImpl();
            try {
                String result = "";
                // 앱위변조 검증
                // exafeConfig.EVENT_TYPE : 1:`기기고유정보 해시, 2:ID/PWD
                // exafeConfig.EVENT_TYPE 1인 경우 mLogin, mPassword값을 null로 처리해 주시면됩니다.
                if (DefenceApi != null && DefenceApi.verify(null, null) == true) {
                    // 검증 성공 후 고객사 실행 API를 넣어주시면 됩니다.
                    mHandler.sendEmptyMessage(IUCOA0M00.HANDLERJOB_APP_DEFENCE_SUCCESS);
                } else {
                    result = CommonAPIManager.getAppDefenceResult();
                    Message msg = Message.obtain();
                    msg.what = IUCOA0M00.HANDLERJOB_APP_DEFENCE_FAIL;
                    msg.obj = result;
                    mHandler.sendMessage(msg);
                }
            } catch (ExafeException e) {
                mHandler.sendEmptyMessage(IUCOA0M00.HANDLERJOB_APP_DEFENCE_ERROR);
                LogManager.printStackTrace(e);
            }
        }
    }
}
