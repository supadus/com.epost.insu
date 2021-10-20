package com.epost.insu.module;

import android.annotation.TargetApi;
import android.content.Context;
import android.hardware.fingerprint.FingerprintManager;
import android.os.Build;
import androidx.annotation.RequiresApi;
import androidx.core.os.CancellationSignal;

import com.epost.insu.R;
import com.epost.insu.common.LogPrinter;

/**
 *
 */
@TargetApi(Build.VERSION_CODES.M)
@RequiresApi(Build.VERSION_CODES.M)
public class GoogleModule implements FingerModuleInterface {

    private Context context;
    private FingerprintManager fingerprintManager;

    /**
     * 생성자
     * @param p_context
     */
    public GoogleModule(Context p_context) {
        context = p_context;

        setInit();
    }

    /**
     * 초기 세팅 함수
     */
    private void setInit(){
        fingerprintManager = context.getSystemService(FingerprintManager.class);
    }


    @Override
    public boolean CF_hasSensor() {
        try {
            return fingerprintManager != null && fingerprintManager.isHardwareDetected();
        }catch (SecurityException e){
            return false;
        }
    }

    @Override
    public boolean CF_hasFingerprint() throws SecurityException{
        return fingerprintManager != null && fingerprintManager.hasEnrolledFingerprints();
    }

    @Override
    public void CF_auth(CancellationSignal p_CancellationSignal, AuthEventListener p_listener) {

        if(fingerprintManager == null){
            p_listener.onFail(context.getResources().getString(R.string.dlg_fingerpinrt_unknown));
        }

        FingerprintManager.AuthenticationCallback callback = new AuthCallback(p_CancellationSignal, p_listener);

        android.os.CancellationSignal tmp_cancellationSignale = p_CancellationSignal == null ? null : (android.os.CancellationSignal)p_CancellationSignal.getCancellationSignalObject();

        try {
            fingerprintManager.authenticate(null, tmp_cancellationSignale, 0, callback, null);
        }catch (SecurityException e){
            LogPrinter.CF_line();
            LogPrinter.CF_debug(context.getResources().getString(R.string.log_fail_auth_g_module));
        }
    }

    class AuthCallback extends FingerprintManager.AuthenticationCallback{

        private CancellationSignal cancellationSignal;
        private AuthEventListener listener;

        public AuthCallback(CancellationSignal p_cancellationSignal, AuthEventListener p_listener){
            cancellationSignal = p_cancellationSignal;
            listener = p_listener;
        }

        @Override
        public void onAuthenticationError(int errorCode, CharSequence errString) {
            super.onAuthenticationError(errorCode, errString);

            if(errorCode != FingerprintManager.FINGERPRINT_ERROR_CANCELED) {
                if (listener != null) {
                    listener.onFail(errString.toString());
                }
            }
        }

        @Override
        public void onAuthenticationHelp(int helpCode, CharSequence helpString) {
            super.onAuthenticationHelp(helpCode, helpString);

            if(listener != null) {
                listener.onFail(helpString.toString());
            }
        }

        @Override
        public void onAuthenticationSucceeded(FingerprintManager.AuthenticationResult result) {
            super.onAuthenticationSucceeded(result);

            if(listener != null) {
                listener.onSuccess();
            }
        }

        @Override
        public void onAuthenticationFailed() {
            super.onAuthenticationFailed();

            if(listener != null) {
                listener.onFail(context.getResources().getString(R.string.dlg_fingerprint_no_recognize));
            }
        }
    }
}
