package com.epost.insu.push.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.epost.insu.common.LogPrinter;
import com.epost.insu.push.notification.PushNotificationManager;

import m.client.push.library.common.Logger;
import m.client.push.library.common.PushConstants;
import m.client.push.library.common.PushLog;

/**
 * 메세지 수신 처리를 위해 등록된 리시버
 * @since     :
 * @version   : 1.1
 * @author    : NJM
 * <pre>
 * ======================================================================
 * 1.5.5    NJM_20210518    [푸쉬타이틀 설정] 소스정렬
 * 1.6.2    NJM_20210802    [2021년 대우 취약점] 2차본 : 부적절한 예외 처리
 * =======================================================================
 * copyright : (주)프로니스정보기술 | http://www.phronis.co.kr
 * </pre>
 */
public class MessageArrivedReceiver extends BroadcastReceiver {
	private Context mContext;
	@Override
	public void onReceive(Context context, Intent intent) {
		LogPrinter.CF_debug("!----------------------------------------------------------");
		LogPrinter.CF_debug("!-- MessageArrivedReceiver.onReceive()");
		LogPrinter.CF_debug("!----------------------------------------------------------");
		PushLog.d("onReceive", "[UPNSCallback] receive action");

		// UPNS 타입의 경우
		if (intent.getAction().equals(context.getPackageName() + PushConstants.ACTION_UPNS_MESSAGE_ARRIVED)) {
			try {
				/*String jsonData = intent.getExtras().getString(PushConstants.KEY_JSON);
				JSONObject jsonMsg = new JSONObject(jsonData);
				String msgID = intent.getExtras().getString(PushConstants.KEY_UPNSMESSAGEID);*/

				String rawData = intent.getExtras().getString(PushConstants.KEY_ORIGINAL_PAYLOAD_STRING);
				byte[] rawDataBytes = intent.getExtras().getByteArray(PushConstants.KEY_ORIGINAL_PAYLOAD_BYTES);

				Logger.i("received raw data : " + rawData);
				Logger.i("received bytes data : " + new String(rawDataBytes, "utf-8"));
				//PushLog.d("onReceive", "## received message: " + jsonData); // 수신 메세지 페이로드
				//PushLog.d("onReceive", "1. receive confirm msgIDForAck: " + msgID); // 수신 ACK를 위한 메세지 아이디
				//PushLog.d("onReceive", "2. received message SEQNO: " + jsonMsg.getString("SEQNO")); // 메세지 시퀀스 넘버
				//PushLog.d("onReceive", "3. received message MESSAGEID: " + jsonMsg.getString("MESSAGEID")); // UPNS 메세지 아이디
				//PushManager.getInstance().upnsMessageReceiveConfirm(context, msgID);
				//PushManager.getInstance().upnsSubscribe(context);

				// 노티피케이션 생성
				PushNotificationManager.createNotification(context, intent, PushConstants.STR_UPNS_PUSH_TYPE);

				// 토스트 발생
				//PushNotificationManager.showToast(context, intent, PushConstants.STR_UPNS_PUSH_TYPE);
			} catch (NullPointerException e) {
				e.getMessage();
			} catch (Exception e) {
				e.getMessage();
			}
		}
		// GCM 의 경우
		else if (intent.getAction().equals(context.getPackageName() + PushConstants.ACTION_GCM_MESSAGE_ARRIVED)) {
			try {
				// 노티피케이션 생성
				PushNotificationManager.createNotification(context, intent, PushConstants.STR_GCM_PUSH_TYPE);

				// 팝업 다이얼로그 예제
//				PushNotificationManager.showNotificationPopupDialog(context, intent, PushConstants.STR_GCM_PUSH_TYPE);
			} catch (NullPointerException e) {
				e.getMessage();
			} catch (Exception e) {
				e.getMessage();
			}
		}
	}
}
