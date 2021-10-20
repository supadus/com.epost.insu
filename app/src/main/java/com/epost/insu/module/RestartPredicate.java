package com.epost.insu.module;

/**
 *
 */
class RestartPredicate {

    public static FingerModule.RestartPredicate restartTimeouts(final int timeoutRestartCount){
        return new FingerModule.RestartPredicate(){

            private int timeoutRestarts = 0;

            @Override
            public boolean invoke(int restartCount) {
                return timeoutRestarts++ < timeoutRestartCount;
            }
        };
    }
}
