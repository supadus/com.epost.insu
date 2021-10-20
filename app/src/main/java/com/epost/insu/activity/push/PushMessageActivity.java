package com.epost.insu.activity.push;

import android.os.Bundle;

import com.epost.insu.EnvConfig;
import com.epost.insu.R;
import com.epost.insu.SharedPreferencesFunc;
import com.epost.insu.activity.Activity_Default;
import com.epost.insu.common.CustomSQLiteFunction;
import com.epost.insu.common.LogPrinter;
import com.epost.insu.common.WebBrowserHelper;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;

import m.client.push.library.PushManager;
import m.client.push.library.common.PushConstants;

public class PushMessageActivity extends Activity_Default {
	private final String subUrl_web     = "/CO/IUCOA1M00.do";                               // 웹 페이지 요청 url

	@Override
	protected void setInit() {
	}

	@Override
	protected void setUIControl() {
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		LogPrinter.CF_debug("!----------------------------------------------------------");
		LogPrinter.CF_debug("!-- PushMessageActivity.onCreate()");
		LogPrinter.CF_debug("!----------------------------------------------------------");

		String jsonMessage = this.getIntent().getStringExtra(PushConstants.KEY_JSON);
		String title       = this.getIntent().getStringExtra(PushConstants.KEY_TITLE);
		String message     = this.getIntent().getStringExtra(PushConstants.KEY_MESSAGE);
//		String ext         = this.getIntent().getStringExtra(PushConstants.KEY_EXT);
//		String psid        = this.getIntent().getStringExtra(PushConstants.KEY_PSID);
//		String pushType    = this.getIntent().getStringExtra(PushConstants.KEY_PUSHTYPE);

		try {
			JSONObject jsonMsg = new JSONObject(jsonMessage);
	    	PushManager.getInstance().pushMessageReadConfirm(this, jsonMessage);
			
			ArrayList<String> params = null;
			if (message != null) {
				String[] paramArray = message.split("\\|");
				params = new ArrayList<String>(Arrays.asList(paramArray));
			}

			// --<> (로그인시) 웹뷰 호출
			if(SharedPreferencesFunc.getFlagLogin(getApplicationContext())) {
				//EnvConfig.AuthDvsn tmp_loginType = SharedPreferencesFunc.getLoginAuthDvsn(getApplicationContext());
				//String tmp_tempKey = SharedPreferencesFunc.getWebTempKey(getApplicationContext());
				//final String tmp_url = EnvConfig.host_url + subUrl_web + "?page=" + "27" + "&csno=" + tmp_csNo + "&loginType=" + tmp_loginType + "&tempKey=" + tmp_tempKey;

				String tmp_csNo = CustomSQLiteFunction.getLastLoginCsno(getApplicationContext());
				final String tmp_url = EnvConfig.host_url + "/CO/IUCOP0M10.do" + "?csno=" + tmp_csNo;
				WebBrowserHelper.startWebViewActivity(getApplicationContext(), 0, false, tmp_url, getResources().getString(R.string.btn_my_notice));

			}
			// --<> (비로그인시) 팝업 호출
			else {
				//CommonFunction.CF_showCustomAlertDilaogFinishingActivity(getApplicationContext(),title + '\n' + message, "확인");
				showCustomDialog(title + '\n' + message, RESULT_OK);
			}

			//TODO 추후 분리 적용
			//String msg = title + "\n" + params.get(0);
//			if (params != null && params.size() > 0) {
//				if (params.get(0).equals("0")) {
//					Intent intent = new Intent(this, PushDetailActivity.class);
//					intent.putExtra(PushConstants.KEY_JSON, jsonMsg.toString());
//					intent.putExtra(Define.KEY_TYPE, params.get(0));
//					intent.putExtra(Define.KEY_TITLE, title);
//			    	intent.putExtra(Define.KEY_MESSAGE, params.get(1));
//			    	intent.putExtra(Define.KEY_BODY, jsonMsg.optString(Define.KEY_BODY));
//			    	startActivity(intent);
//				}
//				else if (params.get(0).equals(Define.TYPE_WEB) || params.get(0).equals(Define.TYPE_IMAGE) || params.get(0).equals(Define.TYPE_VIDEO)) {
//					String contentUrl = "";
//					if(params.size() > 3){
//						contentUrl = params.get(3);
//					}
//					// 단순 이미지만 존재하는 메세지
//					if (TextUtils.isEmpty(contentUrl)) {
//						Intent intent = new Intent(this, PushDetailActivity.class);
//						intent.putExtra(PushConstants.KEY_JSON, jsonMsg.toString());
//						intent.putExtra(Define.KEY_TYPE, params.get(0));
//						intent.putExtra(Define.KEY_TITLE, title);
//				    	intent.putExtra(Define.KEY_MESSAGE, params.get(1));
//				    	intent.putExtra("img", params.get(2));
//						intent.putExtra(Define.KEY_BODY, jsonMsg.optString(Define.KEY_BODY));
//				    	startActivity(intent);
//					}
//					// 컨텐츠가 있는 메세지 - 동영상/웹페이지
//					else {
//						Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(contentUrl));
//						startActivity(intent);
//					}
//				}
//				// 타입이 없는 경우 사용자 정의
//				else {
//					Intent intent = new Intent(this, PushDetailActivity.class);
//					intent.putExtra(PushConstants.KEY_JSON, jsonMsg.toString());
//					intent.putExtra(Define.KEY_TYPE, params.get(0));
//					intent.putExtra(Define.KEY_TITLE, title);
//			    	intent.putExtra(Define.KEY_MESSAGE, params.get(0));
//					intent.putExtra(Define.KEY_BODY, jsonMsg.optString(Define.KEY_BODY));
//			    	startActivity(intent);
//				}
//			}
		} 
		catch (JSONException e) {
			e.getMessage();
		}
		
		//finish();
	}

}
