package com.epost.insu.common;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * DownloadThread
 * @since     :
 * @version   : 1.1
 * @author    : NJM
 * <pre>
 * ======================================================================
 * 0.0.0    NJM_20191216    최초 등록
 * 1.6.2    NJM_20210802    [2021년 대우 취약점] 2차본 : 경쟁조건: 검사시점과 사용시점 (TOCTOU)
 * 1.6.2    NJM_20210802    [2021년 대우 취약점] 2차본 : 부적절한 자원 해제
 * 1.6.3    NJM_20210831    [2021년 모의해킹취약점 2차] 부적절한 예외 처리
 * 1.6.3    NJM_20211006    [2021년 대우 취약점] 3차본 : Null Pointer 역참조
 * =======================================================================
 * copyright : (주)프로니스정보기술 | http://www.phronis.co.kr
 * </pre>
 */
public class DownloadThread extends Thread{
    private Context mContext;
    private Activity mActivity;
    private String mServerUrl;
    private String mLocalPath;
    private String mFileName;
    private String mFileTitle;

    private ImageProgressDialog progressDialog;      // 이미지 프로그레스 다이얼로그


    DownloadThread(Context context, String serverPath, String localPath, String fileName, String pFileTitle) {
        mContext    = context;
        mActivity   = (Activity) context;
        mServerUrl  = serverPath;
        mLocalPath  = localPath;
        mFileName   = fileName;
        mFileTitle  = pFileTitle;

        CF_showProgressDialog();
    }

    @Override
    public void run() {
        synchronized (this) {
            URL imgurl;
            int Read;
            int len = 0;
            InputStream is = null;
            FileOutputStream fos = null;
            HttpURLConnection conn = null;

            try {
                imgurl = new URL(mServerUrl);
                try {
                    conn = (HttpURLConnection) imgurl.openConnection();
                } catch (IOException e) {
                    e.getMessage();
                }

                len = conn.getContentLength();

                // len == -1 : 명확하지 않을경우(파일이 없을경우 등)
                if (len != -1) {
                    byte[] tmpByte = new byte[len];
                    is = conn.getInputStream();
                    File file = new File(mLocalPath);
                    fos = new FileOutputStream(file);
                    for (; ; ) {
                        Read = is.read(tmpByte);
                        if (Read <= 0) {
                            break;
                        }
                        fos.write(tmpByte, 0, Read);
                    }
                }
            } catch (MalformedURLException e) {
                LogPrinter.CF_debug("[ERROR1]" + e.getMessage() );
            } catch (IOException e) {
                LogPrinter.CF_debug("[ERROR2]" + e.getMessage());
            } catch (Exception e) {
                LogPrinter.CF_debug("[ERROR3]" + e.getMessage());
            } finally {
                if(is != null) {
                    try {
                        is.close();
                    } catch (IOException e) {
                        e.getMessage();
                    }
                }
                if(fos != null) {
                    try {
                        fos.close();
                    } catch (IOException e) {
                        e.getMessage();
                    }
                }
               if(conn != null) conn.disconnect();
            }

            Activity a = (Activity) mContext;

            if(len != -1) {
                a.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(mContext, "다운로드(Download)/우체국보험에 파일을 다운받았습니다." , Toast.LENGTH_LONG).show();
                    }
                });
                mAfterDown.sendEmptyMessage(0);
            } else {
                a.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        CF_dismissProgressDialog();
                        Toast.makeText(mContext, "파일 다운로드를 실패하였습니다.", Toast.LENGTH_LONG).show();
                    }
                });
            }
        }
    }

    @SuppressLint("HandlerLeak")
    private Handler mAfterDown = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            CF_dismissProgressDialog();
            // -- 파일 다운로드 종료 후 다운받은 파일을 실행시킨다.
            File tempFile = new File(mLocalPath);
            WebFileDownloadHelper.showDownloadFile(mContext, Uri.fromFile(tempFile), mFileName, mFileTitle);
        }
    };


    // #############################################################################################
    //  Dialog(다이얼로그) 함수
    // #############################################################################################
    /**
     * 이미지 프로그레스 다이얼로그 show
     */
    public void CF_showProgressDialog(){

        if(!mActivity.isDestroyed()) {
            if (progressDialog == null) {
                progressDialog = new ImageProgressDialog(mContext);
            }

            if (progressDialog.isShowing() == false)
                progressDialog.show();
        }
    }

    /**
     * 이미지 프로그레스 다이얼로그 dismiss
     */
    public void CF_dismissProgressDialog(){
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
    }
}