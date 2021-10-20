package com.epost.insu.push.notification;

import android.os.Handler;
import android.os.Message;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;

/**
 * HttpGetStringThread
 * @since     :
 * @version   : 1.1
 * @author    : NJM
 * <pre>
 * 	메세지json 데이터 내의 EXT가 존재할 경우,
 * 	EXT url로부터 상세 정보를 수신하기 위한 통신 쓰레드
 * ======================================================================
 * 1.6.2    NJM_20210802    [2021년 대우 취약점] 2차본 : 경쟁조건: 검사시점과 사용시점 (TOCTOU)
 * 1.6.2    NJM_20210802    [2021년 대우 취약점] 2차본 : 부적절한 자원 해제
 * 1.6.3    NJM_20211006    [2021년 대우 취약점] 3차본 : 오류메시지를 통한 정보노출
 * =======================================================================
 * copyright : (주)프로니스정보기술 | http://www.phronis.co.kr
 * </pre>
 */
public class HttpGetStringThread extends Thread {
	
	private Handler mHandler;
	private String mHttpUrl;

	public HttpGetStringThread(Handler handler, String url) {
		mHandler = handler;
		mHttpUrl = url;
	}
	
	@Override
	public void run() {
		synchronized (this) {
			StringBuffer sb = new StringBuffer();
			HttpURLConnection urlConn = null;
			BufferedReader br = null;

			try {
				if (mHttpUrl.contains("https")) {
					mHttpUrl = mHttpUrl.replaceAll("https", "http");
					mHttpUrl = mHttpUrl.replaceAll("\\\\", "/");
				} else {
					//mHttpUrl = mHttpUrl.replaceAll("//", "/");
					Log.d("HttpGetStringThread", "HttpGetStringThread mHttpUrl: " + mHttpUrl);
				}

				//mHttpUrl = "http://211.241.199.158:8180/msg/totalInfo/0218115649_msp.html";
				URL url = new URL(mHttpUrl);
				urlConn = (HttpURLConnection) url.openConnection();

				if (urlConn != null) {
					Log.e("HttpGetStringThread", "HttpGetStringThread HttpURLConnection is not null");
					urlConn.setDoOutput(true);
					urlConn.setConnectTimeout(10000);
					//urlConn.setChunkedStreamingMode(0);
					//urlConn.setRequestMethod("GET");
					//urlConn.connect();

					int retCode = urlConn.getResponseCode();
					if (retCode == HttpURLConnection.HTTP_OK) {
						br = new BufferedReader(new InputStreamReader(urlConn.getInputStream(), "UTF-8"));

						String line = "";
						while ((line = br.readLine()) != null) {
							//line = new String(line.getBytes("ISO-8859-1"), "UTF-8"); // 한글???
							line = URLDecoder.decode(line, "UTF-8");
							//System.out.println(line);
							sb.append(line);
						}

					} else {
						Log.e("HttpGetStringThread", "HttpGetStringThread Response Code = " + retCode);
					}

					Message msg = mHandler.obtainMessage(0, sb.toString());
					mHandler.sendMessage(msg);
				} else {
					Log.e("HttpGetStringThread", "HttpGetStringThread HttpURLConnection is null");
				}

			} catch (MalformedURLException e) {
				Log.d("HttpGetStringThread", "HttpGetStringThread INVALID URL:: " + e.getLocalizedMessage());
			} catch (IOException e) {
				Log.d("HttpGetStringThread", "HttpGetStringThread NETWORK ERROR:: " + e.getLocalizedMessage());
			} finally {
				if(br != null) {
					try {
						br.close();
					} catch (IOException e) {
						e.getMessage();
					}
				}
				if(urlConn != null) {
					urlConn.disconnect();
				}
			}
		}
	}
}