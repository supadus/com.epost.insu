package com.epost.insu.push.notification;

import android.app.KeyguardManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.widget.Toast;

import androidx.core.app.NotificationCompat;

import com.epost.insu.R;
import com.epost.insu.common.LogPrinter;
import com.epost.insu.activity.push.PushMessageActivity;

import org.json.JSONObject;

import m.client.push.library.PushManager;
import m.client.push.library.common.PushConstants;

/**
 * 푸쉬  알림 관리
 * @since     :
 * @version   : 1.1
 * @author    : NJM
 * <pre>
 * ======================================================================
 * 1.5.5    NJM_20210518    [PUSH 타이틀 설정]
 * 1.6.2    NJM_20210802    [2021년 대우 취약점] 2차본 : 부적절한 예외 처리
 * =======================================================================
 * copyright : (주)프로니스정보기술 | http://www.phronis.co.kr
 * </pre>
 */
public class PushNotificationManager {
	// 마지막 메세지 체크를 위한 변수
	private static String lastestSeqNo = "";

	/**
	 * createNotification
	 * 푸시 타입에 따른 노티피케이션 생성
	 * @param context	context
	 * @param intent	Intent
	 * @param pushType	String
	 * UPNS = {"HEADER":{"ACTION":"SENDMSG"},"BODY":{"MESSAGE":"dfsfdsfsdaf","EXT":"","SEQNO":"607","PSID":"3d5c7522a0a893957d230d70d196e8dcbf3785e9","APPID":"kr.co.morpheus.mobile1","CUID":"12345","MESSAGEID":"20150212143841e6fcce8440e502b45a223a54f0","PUBLIC":"N","SENDERCODE":"admin"}}
	 * GCM = {"aps":{"badge":"1","sound":"alert.aif","alert":"fdsafasfa"},"mps":{"appid":"com.uracle.push.test","messageuniquekey":"20141204112801b6f1fd7840868afc82e51fd6a5","seqno":"387","sender":"admin",,"ext":"http://211.241.199.139:8080/msp-admin/totalInfo\\1203143401_I.html"}}
	 * http://docs.morpheus.co.kr/client/push/gcm.html#service 참고.
	 */
	public static void createNotification(final Context context, final Intent intent, final String pushType) {
		try {
            // --<> (UPNS) 타입에 따라 프로세스 진행
			if (pushType.equals(PushConstants.STR_UPNS_PUSH_TYPE)) {
				createUpnsNotification(context, intent);
			}
            // --<> (GCM)
			else {
				createGcmNotification(context, intent);
			}
		} catch (NullPointerException e) {
			e.getMessage();
		} catch (Exception e) {
			e.getMessage();
		}
	}
	
	/**
	 * isRestrictedScreen
	 * 화면 온/오프 상태 체크 
	 * @param context   Context
	 * @return          static boolean
	 */
	public static boolean isRestrictedScreen(final Context context) {
		KeyguardManager km = (KeyguardManager) context.getSystemService(Context.KEYGUARD_SERVICE);
		return km.inKeyguardRestrictedInputMode(); 
	}
	
	/**
	 * showToast
	 * 토스트 발생 - 각 용도에 맞게 커스터마이징
	 * @param context	context
	 * @param intent	Intent
	 * @param pushType	String
	 * @throws Exception e
	 */
	public static void showToast(final Context context, final Intent intent, String pushType) throws Exception {
		String jsonData = intent.getExtras().getString(PushConstants.KEY_JSON);
		//String psid = intent.getExtras().getString(PushConstants.KEY_PSID);
		JSONObject jsonMsg = new JSONObject(jsonData);
		
		String extData = ""; // ext 데이터
		String title = ""; // 메세지 타이틀
		String seqno = ""; // 메세지 시퀀스
		if (PushConstants.STR_UPNS_PUSH_TYPE.equals(pushType)) { // UPNS
			if (jsonMsg.has(PushConstants.KEY_EXT)) { // 존재하지 않을 수 있으므로
				extData = jsonMsg.getString(PushConstants.KEY_EXT);
		    }
			// 필수 항목
			title = jsonMsg.getString(PushConstants.KEY_MESSAGE);
		}
		else { //GCM
			JSONObject aps = jsonMsg.getJSONObject(PushConstants.KEY_APS);
			JSONObject mps = jsonMsg.getJSONObject(PushConstants.KEY_MPS);
			if (aps.has(PushConstants.KEY_GCM_ALERT)) {
				title = aps.getString(PushConstants.KEY_GCM_ALERT);
		    }
		    if (mps.has(PushConstants.KEY_EXT)) {
		    	extData = mps.getString(PushConstants.KEY_EXT);
		    }
		    if (mps.has(PushConstants.KEY_GCM_SEQNO)) {
		    	seqno = mps.getString(PushConstants.KEY_GCM_SEQNO);
		    	// 중복 메세지에 대한 체크
		    	if (lastestSeqNo.equals(seqno)) {
		    		return;
		    	}
		    	lastestSeqNo = seqno;
		    }
		}
		
		Toast.makeText(context, title + ": " + extData,Toast.LENGTH_SHORT).show();
	}
	
	/**
	 * UPNS 노티피케이션 생성
	 * @param context	Context
	 * @param intent	Intent
	 * @throws Exception e
	 */
	public static void createUpnsNotification(final Context context, final Intent intent) throws Exception {
		LogPrinter.CF_debug("!----------------------------------------------------------");
		LogPrinter.CF_debug("!-- PushNotificationManager.createUpnsNotification()");
		LogPrinter.CF_debug("!----------------------------------------------------------");
        String jsonData = intent.getExtras().getString(PushConstants.KEY_JSON);
        String encryptData = intent.getExtras().getString(PushConstants.KEY_MESSAGE_ENCRYPT);
//        if (jsonData != null) {
//        	jsonData = jsonData.replaceAll("https", "http");
//        	jsonData = jsonData.replaceAll("\\\\", "/");
//        	jsonData = jsonData.replaceAll("//", "/");
//        }
        
        try {
			LogPrinter.CF_debug("!-- [PushNotificationManager] createUpnsNotification: " + jsonData);
	        JSONObject jsonMsg = new JSONObject(jsonData);
	        String psid = intent.getExtras().getString(PushConstants.KEY_PSID);
	        //PushWakeLock.acquireCpuWakeLock(context);

			// -- 푸쉬 알림 실행
	        UpnsNotifyHelper.showNotification(context, jsonMsg, psid, encryptData);
	        //PushWakeLock.releaseCpuLock();
		} catch (NullPointerException e) {
			e.getMessage();
		} catch(Exception e) {
			e.getMessage();
        }	
	}
	
	/**
	 * GCM 노티피케이션 생성
	 * @param context	Context
	 * @param intent	Intent
	 * @throws Exception e
	 */
	public static void createGcmNotification(final Context context, final Intent intent) throws Exception {
		LogPrinter.CF_debug("!----------------------------------------------------------");
		LogPrinter.CF_debug("!-- PushNotificationManager.createGcmNotification()");
		LogPrinter.CF_debug("!----------------------------------------------------------");

		String jsonData = intent.getExtras().getString(PushConstants.KEY_JSON);
		String psid = intent.getExtras().getString(PushConstants.KEY_PSID);
		JSONObject rootJsonObj = new JSONObject(jsonData);
	    JSONObject apsJsonObj = rootJsonObj.getJSONObject(PushConstants.KEY_APS);
	    JSONObject mpsJsonObj = rootJsonObj.getJSONObject(PushConstants.KEY_MPS);
	
	    String alertMessage = apsJsonObj.getString(PushConstants.KEY_GCM_ALERT);
	    String seqNo = mpsJsonObj.getString(PushConstants.KEY_GCM_SEQNO);
	    if (lastestSeqNo.equals(seqNo)) {
    		return;
    	}
    	lastestSeqNo = seqNo;
	    
	    String extUrl = "";
	    if (mpsJsonObj.has(PushConstants.KEY_EXT)) {
	        extUrl = mpsJsonObj.getString(PushConstants.KEY_EXT);
	    }
	
	    int icon = R.mipmap.ic_launcher;
	    //long when = System.currentTimeMillis();
	
	    String title = context.getString(R.string.app_name);
	    Intent notificationIntent = new Intent(context, PushMessageActivity.class);
	    notificationIntent.putExtra(PushConstants.KEY_JSON, rootJsonObj.toString());
	    notificationIntent.putExtra(PushConstants.KEY_PSID, psid);
	    notificationIntent.putExtra(PushConstants.KEY_TITLE, alertMessage);
	    notificationIntent.putExtra(PushConstants.KEY_EXT, extUrl);
	    notificationIntent.putExtra(PushConstants.KEY_PUSHTYPE, "GCM");
	    notificationIntent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY); 
		PendingIntent pendingIntent = PendingIntent.getActivity(context, Integer.parseInt(seqNo), notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);
	    
	    NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
	    Uri soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
		NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context.getApplicationContext())
			    .setAutoCancel(true)
				.setContentIntent(pendingIntent)
		        .setSmallIcon(R.drawable.ic_stat_name)
		        .setContentTitle(title)
		        .setTicker(title)
		        .setContentText(alertMessage)
				.setStyle(new NotificationCompat.BigTextStyle().bigText(alertMessage))
		        .setSound(soundUri);
		
		//mBuilder.setDefaults(Notification.DEFAULT_VIBRATE);
		notificationManager.notify("gcm", Integer.parseInt(seqNo), mBuilder.build());	
		
		PushManager.getInstance().pushMessageReceiveConfirm(context, rootJsonObj.toString());
	}
	
	/*@Deprecated
	public static void showPopupDialog(final Context context, final Intent intent, String pushType) throws Exception {
		String jsonData = intent.getExtras().getString("message");
		String psid = intent.getExtras().getString("psid");
		JSONObject jsonMsg = new JSONObject(jsonData);
		
		String extData = "";
		if ("UPNS".equals(pushType)) {
			if (jsonMsg.has("EXT")) {
				extData = jsonMsg.getString("EXT");
		    }
		}
		else {
			JSONObject mps = jsonMsg.getJSONObject("mps");
		    if (mps.has("ext")) {
		    	extData = mps.getString("ext");
		    }
		}
		
		Intent popupIntent = new Intent(); 
		popupIntent.setClass(context, ShowPushPopup.class);
		popupIntent.setAction(ShowPushPopup.class.getName());
		popupIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		popupIntent.setAction(ShowPushPopup.class.getName());
		popupIntent.putExtra("TITLE", "push Notification");
		popupIntent.putExtra("MESSAGE", "message");
		popupIntent.putExtra("JSON", jsonData);
		popupIntent.putExtra("PS_ID", psid);
		popupIntent.putExtra("EXT", extData);
		popupIntent.putExtra("PUSH_TYPE", pushType);
	    context.startActivity(popupIntent);
		
	}*/
	
	/*@Deprecated
	public static void showToast(final Context context, final String message) throws Exception {
		Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
	}*/
	
	/*@Deprecated
	public static AlertDialog createAlertDialog(final Context context, final String title, final String message, final Handler handler) {
		return new AlertDialog.Builder(context)
        .setIcon(android.R.drawable.ic_dialog_alert)
        .setTitle(title)
        .setMessage(message)
        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
	        @Override
	        public void onClick(DialogInterface dialog, int which) {
	        	if (handler != null) {
					Message msg = handler.obtainMessage(Dialog.BUTTON_POSITIVE, 0, 0, 0);
					handler.sendMessage(msg);
					
					dialog.cancel();
					dialog.dismiss();
				}   
	        }
	    })
	    .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
	        @Override
	        public void onClick(DialogInterface dialog, int which) {
	        	if (handler != null) {
					Message msg = handler.obtainMessage(Dialog.BUTTON_NEGATIVE, 0, 0, 0);
					handler.sendMessage(msg);
					
					dialog.dismiss();
				}    
	        }
	    })
	    .create();
	}
	
	@Deprecated
	public static AlertDialog createConfirmDialog(final Context context, final String title, final String message, final Handler handler) {
		return new AlertDialog.Builder(context)
        .setIcon(android.R.drawable.ic_dialog_alert)
        .setTitle(title)
        .setMessage(message)
        .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
	        @Override
	        public void onClick(DialogInterface dialog, int which) {
	        	if (handler != null) {
					Message msg = handler.obtainMessage(Dialog.BUTTON_POSITIVE, 0, 0, 0);
					handler.sendMessage(msg);
					
					dialog.cancel();
					dialog.dismiss();
				}   
	        }
	    })
	    .create();
	}*/
}
