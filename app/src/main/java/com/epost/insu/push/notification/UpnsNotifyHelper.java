package com.epost.insu.push.notification;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;

import androidx.core.app.NotificationCompat;

import com.epost.insu.R;
import com.epost.insu.activity.IUCOA0M00;
import com.epost.insu.activity.push.PushMessageActivity;
import com.epost.insu.common.LogPrinter;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;

import m.client.push.library.common.PushConstants;

/**
 * 푸쉬 알림 SHOW
 * @since     :
 * @version   : 1.2
 * @author    : NJM
 * <pre>
 * ======================================================================
 * 1.5.5    NJM_20210518    [PUSH 타이틀 설정]
 * 1.5.5    NJM_20210518    [PUSH 개행 설정]
 * 1.6.2    NJM_20210802    [2021년 대우 취약점] 2차본 : Null Pointer 역참조
 * 1.6.2    NJM_20210802    [2021년 대우 취약점] 2차본 : 부적절한 예외 처리
 * =======================================================================
 * copyright : (주)프로니스정보기술 | http://www.phronis.co.kr
 * </pre>
 */
public class UpnsNotifyHelper {
	
	/**
	 * showNotification
	 * 메세지를 분석하여 타입 별 노티피케이션 생성
	 * @param context		Context
	 * @param jsonMsg		JSONObject
	 * @param psid			String
	 * @param encryptData	String
	 * @throws Exception    e
	 */
	public static void showNotification(final Context context, final JSONObject jsonMsg, final String psid, final String encryptData) throws Exception {
		LogPrinter.CF_debug("!----------------------------------------------------------");
		LogPrinter.CF_debug("!-- UpnsNotifyHelper.showNotification()");
		LogPrinter.CF_debug("!----------------------------------------------------------");

	    String extData = "";
	    final String SEQNO = jsonMsg.getString("SEQNO");
	    if (jsonMsg.has(PushConstants.KEY_EXT)) {
	        extData = jsonMsg.getString(PushConstants.KEY_EXT);
	    }
	 
	    // EXT가 존재할 경우 다음과 같은 형태로 전달됨
	    // extData = "http:/211.241.199.158:8180/msg/totalInfo/0218115649_msp.html";
	    if (extData.startsWith("http")) { // HTTP 로 시작하는 url 형태일 경우 - 메세지 구분을 위한 스트링 형태로 서버로부터 내려받는다.
	    	// HttpGetStringThread로부터 응답값을 받은 후 처리 로직
			Handler handler = new Handler() {
				@Override
				public void handleMessage(Message msg) {
	        		if(msg.what == 0) {
	        			String message = (String) msg.obj;
	        			if (message != null) {
	        				//message = message.replaceAll("https", "http");
	        				message = message.replaceAll("\\\\", "/");

	        				try { 
	        					// 다음과 같은 형태로 응답 값이 구성되었을 경우 치환을 위한 로직 수행
	        					// "TYPE":"R","VAR":"hoseok|2015-06-31|2015-07-30|104320|11"
	        					if (jsonMsg.has("TYPE") && jsonMsg.getString("TYPE").equals("R")) {
	        						String var = jsonMsg.getString("VAR");
	        						HashMap<String, String> varMap = new HashMap<String, String>();
	        						if (var != null) {
	        							String[] varArray = var.split("\\|");
	        							for (int i=0; i<varArray.length; i++) {
	        								int idx = i+1;
	        								varMap.put("%VAR" + idx + "%", varArray[i]);
	        								//Log.d("test", "%VAR" + idx + "%" + " " +  varArray[i]);
	        							}
	        							
	        							Iterator<?> keys = varMap.keySet().iterator();
	        							while (keys.hasNext()) {
	        								String key = (String) keys.next();
	        								message = message.replaceAll(key, varMap.get(key));
	        								//Log.d("test", "message: " + message);
	        							}
	        						}
	        					}
	        				} catch (JSONException e) {
								e.getMessage();
	        				}
	        			}
	        			
	        			//DBUtils.getDbOpenHelper(context).insertPushMSG(SEQNO, jsonMsg.toString(), message, encryptData);
	        			createNotification(context, jsonMsg, psid, encryptData, message);
	        		}
				}
			};
			// EXT url 로부터 메세지 정보를 수신하기 위한 쓰레드
			new HttpGetStringThread(handler, extData).start();
		}
		else { // 일반 스크링의 경우
			//DBUtils.getDbOpenHelper(context).insertPushMSG(SEQNO, jsonMsg.toString(), extData, encryptData);
			createNotification(context, jsonMsg, psid, encryptData, extData);
		}
	}	
	
	/**
	 * createNotification
	 * 노티피케이션 생성
	 * @param context		Context
	 * @param jsonMsg		JSONObject
	 * @param psid			String
	 * @param encryptData	String
	 * @param message		String
	 */
	private static void createNotification(final Context context, final JSONObject jsonMsg, final String psid, final String encryptData, String message) {
		LogPrinter.CF_debug("!----------------------------------------------------------");
		LogPrinter.CF_debug("!-- UpnsNotifyHelper.createNotification()");
		LogPrinter.CF_debug("!----------------------------------------------------------");
		/*
			예시) jsonMsg
			{"MESSAGE":"파라미터처리용 첫번째 테스트 내용입니다.\/n노지민 고객님 청구금액 1,000,000 입니다.",
			 "EXT":"","SEQNO":"1573543","PSID":"22defc8e3172ffc82cd1bdfe496adb5288cf0d1a","APPID":"com.epost.insu","CUID":"147647863",
			 "PUBLIC":"N","SENDERCODE":"A01", "SENDDATE":"2021021910","DB_IN":"Y","BADGENO":"40","SUB_TITLE":"테스트제목"}
		 */
		ArrayList<String> params = null;
		if (message != null) {
			String[] paramArray = message.split("\\|");
			params = new ArrayList<String>(Arrays.asList(paramArray));
		}

		Log.d("NotificationManager", "[NotificationManager] params size:: " + (params == null ? "null" : params.size()));
		// 메세지 타입에 따라 노티피케이션 생성 - 용도에 따라 커스터마이징하여 사용이 가능 (타입 번호/파싱 룰 등등..)
		if (params != null && params.size() > 0) {
			try {
				// URL을 실제 상세 메세지 데이터로 치환
				jsonMsg.remove(PushConstants.KEY_EXT);
				jsonMsg.put(PushConstants.KEY_EXT, message);
				
				// --<> 일반 메세지의 경우
				if (params.get(0).equals("0")) {
					LogPrinter.CF_debug("!---- defaultNotification: " + message);
					defaultNotification(context, jsonMsg, params.get(1), psid, message, encryptData);
				}
				// --<> 이미지 메세지의 경우
				else if (params.size() > 2) {
					LogPrinter.CF_debug("!---- showImageNotification: " + message);
					showImageNotification(context, jsonMsg, params.get(1), params.get(2), psid, message);
				}
				// --<> 기타의 경우 - 일반메세지로 처리했으나 용도에 맞게 변경
				else {
					LogPrinter.CF_debug("!---- UNKNOUWN TYPE: " + params.get(0));
					defaultNotification(context, jsonMsg, message, psid, message, encryptData);
				}
			} catch (NullPointerException e) {
				e.getMessage();
			} catch (Exception e) {
				e.getMessage();
			}
		}
	}
		
	private static void defaultNotification(Context context, JSONObject jsonMsg, String strMessage, String psid, String ext, String encryptData) throws Exception {
		LogPrinter.CF_debug("!----------------------------------------------------------");
		LogPrinter.CF_debug("!-- UpnsNotifyHelper.defaultNotification()");
		LogPrinter.CF_debug("!----------------------------------------------------------");
		/*
			예시) jsonMsg
			{"MESSAGE":"파라미터처리용 첫번째 테스트 내용입니다.\/n노지민 고객님 청구금액 1,000,000 입니다.",
			 "SEQNO":"1573543","PSID":"22defc8e3172ffc82cd1bdfe496adb5288cf0d1a", "APPID":"com.epost.insu","CUID":"147647863",
			 "PUBLIC":"N","SENDERCODE":"A01","SENDDATE":"2021021910","DB_IN":"Y","BADGENO":"40","SUB_TITLE":"테스트제목","EXT":""}
		 */

		// -- 타이틀 설정 : 타이틀 없을 경우 앱이름 표기
		String subTitle = jsonMsg.getString("SUB_TITLE");
		if(TextUtils.isEmpty(subTitle)) {
			subTitle = context.getString(R.string.app_name);
		}

		// -- 내용 설정
		String message  = jsonMsg.getString(PushConstants.KEY_MESSAGE);
		message = message.replaceAll("/n", "\n");	// 개행
	    int icon = R.mipmap.ic_launcher;
	    int seqno = Integer.parseInt(jsonMsg.getString(PushConstants.KEY_SEQNO));

	    // -- intent(메인화면) 전달 파라미터 세팅
		Intent intent = new Intent(context, IUCOA0M00.class);
		intent.putExtra(PushConstants.KEY_JSON    , jsonMsg.toString());
		intent.putExtra(PushConstants.KEY_PSID    , psid);
		intent.putExtra(PushConstants.KEY_TITLE	  , subTitle);
		intent.putExtra(PushConstants.KEY_MESSAGE , message);
		intent.putExtra(PushConstants.KEY_EXT     , ext);
		intent.putExtra("ENCRYPT"		  , encryptData);
		intent.putExtra(PushConstants.KEY_PUSHTYPE, "UPNS");
		intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
		PendingIntent pIntent = PendingIntent.getActivity(context, seqno, intent, PendingIntent.FLAG_UPDATE_CURRENT);

		//
		NotificationManager mManager = (NotificationManager)context.getSystemService(Context.NOTIFICATION_SERVICE);
		String CHANNEL_ID = "my_channel_01";

		if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
			CharSequence name = context.getPackageName();
			String Description = context.getPackageName();
			int importance = NotificationManager.IMPORTANCE_HIGH;
			NotificationChannel mChannel = new NotificationChannel(CHANNEL_ID, name, importance);
			mChannel.setDescription(Description);
			mChannel.enableLights(true);
			mChannel.setLightColor(Color.RED);
			mChannel.enableVibration(true);
			mChannel.setVibrationPattern(new long[]{100, 200, 300, 400, 500, 400, 300, 200, 400});
			mManager.createNotificationChannel(mChannel);
		}

		// -- noti show
		Uri soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
		NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context.getApplicationContext(), CHANNEL_ID)
			    .setAutoCancel(true)
				.setContentIntent(pIntent)
		        .setSmallIcon(icon)
		       // .setLargeIcon(BitmapFactory.decodeResource(context.getResources(), icon))
		        .setContentTitle(subTitle)
		        .setTicker(subTitle)
		        .setContentText(message)
				.setStyle(new NotificationCompat.BigTextStyle().bigText(message))
		        .setSound(soundUri);
		
		//mBuilder.setDefaults(Notification.DEFAULT_VIBRATE);
		mManager.notify("upns", seqno, mBuilder.build());
	}
	
	private static void showImageNotification(Context context, final JSONObject jsonMsg, final String strMessage, String img, final String psid, final String ext) {
		LogPrinter.CF_debug("!----------------------------------------------------------");
		LogPrinter.CF_debug("!-- UpnsNotifyHelper.showImageNotification()");
		LogPrinter.CF_debug("!----------------------------------------------------------");

		final int icon = R.mipmap.ic_launcher;
		final String title = context.getString(R.string.app_name);
		final Context ctx = context;
		
		if (img.contains("https")) {
			img = img.replaceAll("https", "http");
			img = img.replaceAll("\\\\", "/");
		}
		
		ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(context).build();
		ImageLoader.getInstance().init(config);
		ImageLoader.getInstance().loadImage(img, new SimpleImageLoadingListener() {
			
		    @Override
		    public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
		    	notificationWithBigPicture(ctx, jsonMsg, title, strMessage, icon, loadedImage, psid, ext);
		    }
		});		
	}
	
	private static void notificationWithBigPicture(Context context, JSONObject jsonMsg, String title, String message, int icon, Bitmap banner, String psid, String ext) {
		LogPrinter.CF_debug("!----------------------------------------------------------");
		LogPrinter.CF_debug("!-- UpnsNotifyHelper.notificationWithBigPicture()");
		LogPrinter.CF_debug("!----------------------------------------------------------");

		Intent intent = new Intent(context, PushMessageActivity.class);
		intent.putExtra(PushConstants.KEY_JSON, jsonMsg.toString());
		intent.putExtra(PushConstants.KEY_PSID, psid);
		intent.putExtra(PushConstants.KEY_TITLE, message);
		intent.putExtra(PushConstants.KEY_EXT, ext);
		intent.putExtra(PushConstants.KEY_PUSHTYPE, "UPNS");
		intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY); 
    	int seqno = 0;
		try {
			seqno = Integer.parseInt(jsonMsg.getString("SEQNO"));
		} catch (NumberFormatException | JSONException e) {
			e.getMessage();
		}
		PendingIntent pendingIntent = PendingIntent.getActivity(context, seqno, intent, PendingIntent.FLAG_UPDATE_CURRENT);

		NotificationManager mManager = (NotificationManager)context.getSystemService(Context.NOTIFICATION_SERVICE);
		String CHANNEL_ID = "my_channel_01";

		if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
			CharSequence name = context.getPackageName();
			String Description = context.getPackageName();
			int importance = NotificationManager.IMPORTANCE_HIGH;
			NotificationChannel mChannel = new NotificationChannel(CHANNEL_ID, name, importance);
			mChannel.setDescription(Description);
			mChannel.enableLights(true);
			mChannel.setLightColor(Color.RED);
			mChannel.enableVibration(true);
			mChannel.setVibrationPattern(new long[]{100, 200, 300, 400, 500, 400, 300, 200, 400});
			mManager.createNotificationChannel(mChannel);
		}

		NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
			.setSmallIcon(icon)
			.setTicker(title)
			.setContentTitle(title)
			.setContentText(message)
			.setAutoCancel(true);
		
		NotificationCompat.BigPictureStyle style = new NotificationCompat.BigPictureStyle();
		style.setBigContentTitle(title);
		style.setSummaryText(message);
		style.bigPicture(banner);
		
		builder.setStyle(style);
		builder.setContentIntent(pendingIntent);
		
		builder.setDefaults(Notification.DEFAULT_VIBRATE);
		builder.setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION));
		mManager.notify("upns", seqno/*seqno*/, builder.build());
	}
}