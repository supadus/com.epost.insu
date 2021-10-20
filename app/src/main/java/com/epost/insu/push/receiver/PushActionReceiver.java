package com.epost.insu.push.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

import com.epost.insu.common.LogPrinter;
import com.epost.insu.push.InterfaceCustom;

import org.json.JSONException;
import org.json.JSONObject;

import m.client.push.library.PushManager;
import m.client.push.library.common.Logger;
import m.client.push.library.common.PushConstants;
import m.client.push.library.common.PushConstantsEx;
import m.client.push.library.utils.PushUtils;

public class PushActionReceiver extends BroadcastReceiver {
	@Override   
	public void onReceive(Context context, Intent intent) {
        LogPrinter.CF_debug("!----------------------------------------------------------");
        LogPrinter.CF_debug("!-- PushActionReceiver.onReceive()");
        LogPrinter.CF_debug("!----------------------------------------------------------");

        //intent 정보가 정상적인지 판단
        String result = intent.getExtras().getString(PushConstants.KEY_RESULT);
		String bundle = intent.getStringExtra(PushConstantsEx.KEY_BUNDLE);
		if(bundle == null){
			Logger.i("Empty Bundle !!" );
		}

		if(!intent.getAction().contains(context.getPackageName())){
			Logger.i("Not Support Action");
			return ;
		}

        // 액션 유효성 체크
        if (!PushUtils.checkValidationOfCompleted(intent, context)) {
            return;
        }

        JSONObject result_obj = null; // 결과 오브젝트
        String resultCode = ""; // 결과 코드
        String resultMsg = ""; // 결과 메세지
        try {
            result_obj = new JSONObject(result);
            resultCode = result_obj.getString(PushConstants.KEY_RESULT_CODE);
            resultMsg = result_obj.getString(PushConstants.KEY_RESULT_MSG);
        }
        catch (JSONException e) {
            e.getMessage();
        }

        // -----------------------------------------------------------------------------------------
        // 액션에 따라 분기 처리
        // -----------------------------------------------------------------------------------------
        // --<> 사용자 등록 결과 처리
        if (bundle.equals(PushConstantsEx.COMPLETE_BUNDLE.REG_USER)) {
            if (resultCode.equals(PushConstants.SUCCESS_RESULT_CODE)) {
                LogPrinter.CF_debug("!-- 사용자 등록 성공");
                // 등록 성공시 다음 스템 진행 (예 : 다음으로 진행해야 할 페이지로 이동 등등)
                InterfaceCustom.getInstance().getPushReceiverCBListner().onPushReceiverState(InterfaceCustom.SUCCESS_RESULT_CODE,"사용자 등록 성공");
            }
            else {
                LogPrinter.CF_debug("!-- 사용자 등록 실패 : " + resultCode + " msg: " + resultMsg);
                //Toast.makeText(context, "[LoginActivity] error code: " + resultCode + " msg: " + resultMsg, Toast.LENGTH_SHORT).show();
            }
        }
        // --<> 서비스 및 사용자 등록 API를 사용했을 경우
        else if(bundle.equals(PushConstantsEx.COMPLETE_BUNDLE.REG_SERVICE_AND_USER)) {
            if (resultCode.equals(PushConstants.SUCCESS_RESULT_CODE)) {
                LogPrinter.CF_debug("!-- 서비스 등록 및 사용자 등록 성공");
                InterfaceCustom.getInstance().getPushReceiverCBListner().onPushReceiverState(InterfaceCustom.SUCCESS_RESULT_CODE,"서비스 등록 및 사용자 등록 성공");
            }
            else {
                LogPrinter.CF_debug("!-- 서비스 등록 및 사용자 등록 실패 : " + resultCode + " msg: " + resultMsg);
                //Toast.makeText(context, "[LoginActivity] error code: " + resultCode + " msg: " + resultMsg, Toast.LENGTH_SHORT).show();
            }
        }
        // --<> 푸시 서비스 해제
        else if (bundle.equals(PushConstantsEx.COMPLETE_BUNDLE.UNREG_PUSHSERVICE)) {
            if (resultCode.equals(PushConstants.SUCCESS_RESULT_CODE)) {
                LogPrinter.CF_debug("!-- 서비스 해제 성공");
            }
            else {
                LogPrinter.CF_debug("!-- 서비스 해제 실패 : " + resultCode + " msg: " + resultMsg);
                //Toast.makeText(context, "[LoginActivity] error code: " + resultCode + " msg: " + resultMsg, Toast.LENGTH_SHORT).show();
            }
        }
        // --<> 그룹 등록
        else if (bundle.equals(PushConstantsEx.COMPLETE_BUNDLE.REG_GROUP)) {
            if (resultCode.equals(PushConstants.SUCCESS_RESULT_CODE)) {
                LogPrinter.CF_debug("!-- 그룹 등록 성공");
                //Toast.makeText(context, "그룹 등록 성공!", Toast.LENGTH_SHORT).show();
            }
            else {
                LogPrinter.CF_debug("!-- 그룹 등록 실패 : " + resultCode + " msg: " + resultMsg);
                //Toast.makeText(context, "[LoginActivity] error code: " + resultCode + " msg: " + resultMsg, Toast.LENGTH_SHORT).show();
            }
        }
        // --<> 그룹 해제
        else if (bundle.equals(PushConstantsEx.COMPLETE_BUNDLE.UNREG_GROUP)) {
            if (resultCode.equals(PushConstants.SUCCESS_RESULT_CODE)) {
                LogPrinter.CF_debug("!-- 그룹 해제 성공");
                //Toast.makeText(context, "그룹 해제 성공!", Toast.LENGTH_SHORT).show();
            }
            else {
                LogPrinter.CF_debug("!-- 그룹 해제 실패 : " + resultCode + " msg: " + resultMsg);
                //Toast.makeText(context, "[LoginActivity] error code: " + resultCode + " msg: " + resultMsg, Toast.LENGTH_SHORT).show();
            }
        }
        // --<> (뱃지 초기화) 안드로이드 기본 기능으로 제공되는 것이 아니므로 뱃지 카운트를 서버를 통해 초기화 및 업데이트를 진행하고 이에 대해 디바이스에 적용해준다.
        else if (bundle.equals(PushConstantsEx.COMPLETE_BUNDLE.INITBADGENO)) {
            if (resultCode.equals(PushConstants.SUCCESS_RESULT_CODE)) {
                LogPrinter.CF_debug("!-- Badge Number 초기화 성공");
                //Toast.makeText(context, "Badge Number 초기화 성공 !", Toast.LENGTH_SHORT).show();
                // 초기화 성공 시 실제 디바이스의 뱃지 값을 변경하여 준다.
                PushManager.getInstance().setDeviceBadgeCount(context.getApplicationContext(), "0");
            }
            else {
                LogPrinter.CF_debug("!-- Badge Number 초기화 실패 : " + resultCode + " msg: " + resultMsg);
                //Toast.makeText(context, "[LoginActivity] error code: " + resultCode + " msg: " + resultMsg, Toast.LENGTH_SHORT).show();
            }
        }
        // --<> 이미 등록되어 있다면
        else if (bundle.equals(PushConstantsEx.COMPLETE_BUNDLE.IS_REGISTERED_SERVICE)) {
            String isRegister = "";
            try {
                isRegister = result_obj.getString(PushConstants.KEY_ISREGISTER);
            } catch (JSONException e) {
                e.getMessage();
            }

            if (isRegister.equals("C")) {
                LogPrinter.CF_debug("!-- CHECK ON [ 사용자 재등록 필요 !! ]");
                //Toast.makeText(context, "CHECK ON [ 사용자 재등록 필요 !! ]", Toast.LENGTH_LONG).show();
            }
            else if(isRegister.equals("N")) {
                LogPrinter.CF_debug("!-- CHECK ON [ 서비스 재등록 필요 !! ]");
                //Toast.makeText(context, "CHECK ON [ 서비스 재등록 필요 !! ]", Toast.LENGTH_LONG).show();
            }
            else {
                LogPrinter.CF_debug("!-- 서비스 정상 등록 상태");
            }
        }
	}
}
