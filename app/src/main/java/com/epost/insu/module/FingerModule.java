package com.epost.insu.module;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import androidx.core.os.CancellationSignal;

import com.epost.insu.common.LogPrinter;

/**
 * 지문인식 모듈 체크
 */
public class FingerModule {

    public interface RestartPredicate{
        boolean invoke(int restartCount);
    }

    private Context context;
    private FingerModuleInterface module;

    @TargetApi(Build.VERSION_CODES.M)
    public  FingerModule(Context p_context){
        context = p_context;

        setModule();
    }

    /**
     * 사용할 지문인식 모듈 세팅
     */
    private void setModule(){
        // --<> 마시멜로(23) 이상일 경우
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            module = new GoogleModule(context);
        }
        else {
            LogPrinter.CF_debug("지문인식 지원하지 않는 OS버전!!");
        }
    }

    /**
     * 지문인식 센서 보유여부
     * @return if true, 지문인식 하드웨어 지원.
     */
    public boolean CF_hasFingerSensor(){
        return  module != null && module.CF_hasSensor();
    }

    /**
     * 단말기에 지문 등록 여부
     * @return if true, 단말기에 등록된 지문이 있음.
     */
    public boolean CF_hasFIngerprint(){
        return module != null && module.CF_hasFingerprint();
    }

    public void CF_Auth(AuthEventListener p_listener, CancellationSignal p_cancelSignal){

        module.CF_auth(p_cancelSignal, p_listener);
    }
}
