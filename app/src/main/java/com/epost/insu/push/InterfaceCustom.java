package com.epost.insu.push;

import android.util.Log;

public class InterfaceCustom {
    private String TAG = "SONG";
    public static final int SUCCESS_RESULT_CODE = 0;
    private static InterfaceCustom interfaceCustom;
    private PushReceiverCBListner listener;
    public InterfaceCustom(){
        Log.d(TAG ,"InterfaceCustom()");
    }
    public static InterfaceCustom getInstance(){
        if(interfaceCustom == null){
            interfaceCustom = new InterfaceCustom();
        }
        return interfaceCustom;
    }
    public interface PushReceiverCBListner{
        void onPushReceiverState(int resultCd, String resultMsg);
    }
    public void setPushReceiverCBListner(PushReceiverCBListner listener){
        this.listener = listener;
    }
    public PushReceiverCBListner getPushReceiverCBListner(){
        return this.listener;
    }
}
