package com.epost.insu.module;

/**
 *s
 */
public interface AuthEventListener {
    void onSuccess();
    void onFail(String p_errorMsg);
    void onNeedRetry();
}
