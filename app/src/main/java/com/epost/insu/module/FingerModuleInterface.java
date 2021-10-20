package com.epost.insu.module;

import androidx.core.os.CancellationSignal;

/**
 *
 */
interface FingerModuleInterface {

    boolean CF_hasSensor();

    boolean CF_hasFingerprint();

    void CF_auth(CancellationSignal p_CancellationSignal, AuthEventListener p_listener);
}
