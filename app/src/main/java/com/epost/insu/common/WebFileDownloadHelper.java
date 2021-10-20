package com.epost.insu.common;

import android.app.Activity;
import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import android.widget.Toast;

import com.epost.insu.R;

import java.io.File;
import java.util.Objects;


/**
 * <br/> copyright : (주)프로니스정보기술 | http://www.phronis.co.kr
 * <br/> project   : 모바일슈랑스 구축
 * <br/> Title     : 웹에서 파일다운로드 처리 관련 Helper
 *
 * <pre>
 * ======================================================================
 *          NJM_20200207    최초 등록
 *          NJM_20200408    pdf 다운로드 리시버 개선
 * 1.5.6    NJM_20210531    [파일다운로드개선] 정상:doc, pdf, ppt, xls, xlsx (비정상:docx,hwp,pptx) -- 비정상 추가 수정 필요함
 * 1.6.3    NJM_20211006    [2021년 대우 취약점] 3차본 : 부적절한 예외 처리
 * =======================================================================
 * </pre>
 *
 * @version   : 1.0
 * @author    : 노지민
 */
public class WebFileDownloadHelper {

    // -- 웹파일다운로드용 변수
    //private static String saveFolder = Environment.DIRECTORY_DOWNLOADS + "/우체국보험";         // 모바일 저장경로
    private static String saveFolder = Environment.DIRECTORY_DOWNLOADS;         // 모바일 저장경로

    private static BroadcastReceiver fileDownloadCompleteReceiver;                              // 파일다운로드 리시버


    /**
     * 안드로이드 다운로드매니저를 이용한 파일 다운로드 (+ Notification)
     * <pre>
     * * Notification에 파일 다운로드 알림 표기함
     * * 파일다운로드시 BroadcastReceiver를 등록한다.
     * -- BroadcastReceiver를 통하여 파일 다운로드 status를 체크할 수 있다.
     * -- 호출하는 activity에서 activity 종료시 fileDownloadUnRegisterReceiver()를 호출해야한다.
     * </pre>
     *
     * @param pContext          Context
     * @param url               String
     * @param userAgent         String
     * @param pFileName         String
     * @param mimeType          String
     * @param canDuplicateFile  boolean     기존 다운로드 파일 있을 경우 처리(true:새로받음, false:기존실행)
     * @param contentLength     long
     */
    public static void webFileDownloadManager(Context pContext, String url, String userAgent, String pFileName, String mimeType, boolean canDuplicateFile, long contentLength) {
        LogPrinter.CF_debug("!----------------------------------------------------------");
        LogPrinter.CF_debug("!-- WebFileDownloadHelper.webFileDownloadManager()");
        LogPrinter.CF_debug("!----------------------------------------------------------");

        Activity mActivity = (Activity) pContext;

        // -- 다운로드 폴더에 동일한 파일명이 존재하는지 확인
        // TODO : 저장소 절대값 주소 해결 필요
        boolean hasFile = false;
        String temp_AbsoluteFilePath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/" + saveFolder + "/";

        // --<0> (기존파일 유무체크)
        if (new File(temp_AbsoluteFilePath + pFileName).exists()) {
            LogPrinter.CF_debug("!--<> 기존파일 있음::::" + saveFolder + "/" + pFileName);
            hasFile = true;
        }
        else {
            LogPrinter.CF_debug("!--<> 기존파일 없음::::" + saveFolder + "/" + pFileName);
        }

        // --<1> (중복불가 && 기존파일 있음) 중복불가이면서 이미 다운 받을 파일이 있을 경우에는 다시 다운로드 받지말고 기존파일 실행
        if(!canDuplicateFile && hasFile) {
            LogPrinter.CF_debug("!--<1> 파일 바로 실행");
            File tempFile = new File(temp_AbsoluteFilePath + "/" + pFileName);
            showDownloadFile(pContext, Uri.fromFile(tempFile), pFileName, pFileName);
        }
        // --<1> () 그 외 다운로드 시작
        else {
            LogPrinter.CF_debug("!--<1> 파일 다운로드");
            try {
                DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url));
                request.setDescription(pContext.getResources().getString(R.string.hint_download_start_noti));

                if(!"".equals(mimeType)) {
                    request.setMimeType(mimeType);
                }

                if(!"".equals(userAgent)) {
                    request.addRequestHeader("User-Agent", userAgent);
                }

                //request.allowScanningByMediaScanner();
                request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);

                // -- 저장폴더 지정
                request.setDestinationInExternalPublicDir(saveFolder, pFileName);

                // -- 다운로드 매니저
                DownloadManager dm = (DownloadManager) pContext.getSystemService(Context.DOWNLOAD_SERVICE);

                // -- 다운로드 리시버 등록
                fileDownloadRegisterReceiver(pContext, dm.enqueue(request));
                Toast.makeText(pContext, pContext.getResources().getString(R.string.dlg_download_start), Toast.LENGTH_LONG).show();
            } catch (NullPointerException e) {
                e.getMessage();
            } catch (Exception e) {
                // -- 권한 체크
                if (ContextCompat.checkSelfPermission(pContext, android.Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                    // Should we show an explanation?
                    if (ActivityCompat.shouldShowRequestPermissionRationale(mActivity, android.Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                        Toast.makeText(pContext, "첨부파일 다운로드를 위해\n동의가 필요합니다.", Toast.LENGTH_LONG).show();
                        ActivityCompat.requestPermissions(mActivity, new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE},
                                110);
                    } else {
                        Toast.makeText(pContext, "첨부파일 다운로드를 위해\n동의가 필요합니다.", Toast.LENGTH_LONG).show();
                        ActivityCompat.requestPermissions(mActivity, new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE},
                                110);
                    }
                }
            }
        }
    }

    /**
     * 쓰레드를 이용한 웹서버에 있는 파일 다운로드
     * - 파일 중복 다운로드 불가함 (기존파일있으면 그대로 사용함)
     *
     * @param pContext     Context
     * @param pUrl         String  파일확장자까지 포함한 full 경로
     * @param pFileName    String  파일명(확장자포함)
     * @param pFileTitle   String  뷰 타이틀에 표기할 파일 타이틀
     */
    public static void webFileDownloadThread(Context pContext, String pUrl, String pFileName, String pFileTitle) {
        LogPrinter.CF_debug("!----------------------------------------------------------");
        LogPrinter.CF_debug("!-- WebFileDownloadHelper.webFileDownloadThread()");
        LogPrinter.CF_debug("!----------------------------------------------------------");

        String mSaveDir = "";                   // (로컬) 파일 저장 경로
        DownloadThread dThread;

        // -- 다운로드 경로를 외장메모리 사용자 지정 폴더로 함.
        String ext = Environment.getExternalStorageState();
        if (ext.equals(Environment.MEDIA_MOUNTED)) {
            mSaveDir = Environment.getExternalStorageDirectory().getAbsolutePath() + "/" + saveFolder;
        }

        // -- 저장경로 체크
        File dir = new File(mSaveDir);
        // ---- 폴더가 존재하지 않을 경우 폴더를 만듬
        if (!dir.exists()) {
            dir.mkdir();
        }

        // -- 다운로드 폴더에 동일한 파일명이 존재하는지 확인해서
        // 없으면 다운받고 있으면 해당 파일 실행시킴.
        if (!new File(mSaveDir + "/" + pFileName).exists()) {
            //       loadingBar.setVisibility(View.VISIBLE);
            dThread = new DownloadThread(pContext, pUrl ,mSaveDir + "/" + pFileName, pFileName, pFileTitle);
            dThread.start();

        } else {
            File tempFile = new File(mSaveDir + "/" + pFileName);
            LogPrinter.CF_debug("!---- Uri.fromFile(tempFile).toString()::::" + Uri.fromFile(tempFile).toString());

            showDownloadFile(pContext, Uri.fromFile(tempFile), pFileName, pFileTitle);
        }
    }


    /**
     * 파일다운로드 리시버 등록
     * <pre>
     *  - 다운로드 완료 후 리시버 등록 해제한다.
     * </pre>
     * @param pContext      final Context
     * @param pDownloadId   final long      파일다운로드ID
     */
    private static void fileDownloadRegisterReceiver(final Context pContext, final long pDownloadId) {
        // -- 파일다운로드 리시버
        if(fileDownloadCompleteReceiver == null) {
            fileDownloadCompleteReceiver = new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    LogPrinter.CF_debug("!----------------------------------------------------------");
                    LogPrinter.CF_debug("!-- WebFileDownloadHelper.onReceive() --파일다운로드");
                    LogPrinter.CF_debug("!----------------------------------------------------------");
                    LogPrinter.CF_debug("!---- intent.getAction()::::" + intent.getAction());

                    long id = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, 0L);
                    LogPrinter.CF_debug("!---- 파일다운로드 id / onReceive id::::" + id + "/" + pDownloadId);

                    // --<1> (다운로드 종료시) 브로드캐스트
                    if(DownloadManager.ACTION_DOWNLOAD_COMPLETE.equals(intent.getAction())) {
                        // -- 해당 다운로드가 맞는지 확인
//                        if(id != pDownloadId) {
//                            return ;
//                        }

                        DownloadManager dm = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
                        DownloadManager.Query query = new DownloadManager.Query();
                        query.setFilterById(id);

                        // --<2> (다운로드 완료시)
                        Cursor cursor = dm.query(query);
                        if (cursor.moveToFirst()) {
                            int statusIndex = cursor.getColumnIndex(DownloadManager.COLUMN_STATUS);     // 진행상태
                            int titleIndex  = cursor.getColumnIndex(DownloadManager.COLUMN_TITLE);      // 파일명
                            int uriIndex    = cursor.getColumnIndex(DownloadManager.COLUMN_LOCAL_URI);  // URI

                            LogPrinter.CF_debug("!---- statusIndex::::" + cursor.getInt(statusIndex));
                            // -- 다운로드 실패시
                            if(DownloadManager.STATUS_SUCCESSFUL != cursor.getInt(statusIndex)) {
                                LogPrinter.CF_debug("!---- 다운로드 실패::::");
                                return;
                            }

                            String fileName = cursor.getString(titleIndex);
                            String uri      = cursor.getString(uriIndex);
                            LogPrinter.CF_debug("!---- 다운로드 완료(uri):::::::::" + uri);
                            LogPrinter.CF_debug("!---- 다운로드 완료(fileName)::::" + fileName);

                            // -- pdf뷰어 호출
                            showDownloadFile(pContext, Uri.parse(uri), fileName, fileName);
                        }

                        // --<2> (다운로드 취소시) 다운로드 목록에 없음
                        else {
                            LogPrinter.CF_debug("!--<> 다운로드 취소됨");
                        }
                    }
                }
            };

            // -- 파일다운로드 리시버 등록
            IntentFilter fileDownloadCompleteFilter = new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE);
            pContext.registerReceiver(fileDownloadCompleteReceiver, fileDownloadCompleteFilter);
        }
     }

    public static void fileDownloadUnRegisterReceiver(Context pContext) {
        // -- 파일다운로드 리시버 해제
        if(fileDownloadCompleteReceiver != null) {
            LogPrinter.CF_debug("!---- 파일다운로드 리시버 해제 시작");
            pContext.unregisterReceiver(fileDownloadCompleteReceiver);
            fileDownloadCompleteReceiver = null;
            LogPrinter.CF_debug("!---- 파일다운로드 리시버 해제 완료");
        }
    }

    /**
     * 파일 다운로드 완료 후 처리
     * @param pContext      Context
     * @param pUri          Uri
     * @param pFileName     String
     * @param pFileTitle    String
     */
    public static void showDownloadFile(Context pContext, Uri pUri, String pFileName, String pFileTitle) {
        LogPrinter.CF_debug("!----------------------------------------------------------");
        LogPrinter.CF_debug("!-- WebFileDownloadHelper.showDownloadFile()");
        LogPrinter.CF_debug("!----------------------------------------------------------");

        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_VIEW);

        // -- FileProvider : 권한 문제로 사용
        File file = new File(Objects.requireNonNull(pUri.getPath()));
        LogPrinter.CF_debug("!---- Uri.fromFile(file).toString() :" + Uri.fromFile(file).toString());
        pUri = FileProvider.getUriForFile(pContext, pContext.getPackageName()+".capture", file);

        // -----------------------------------------------------------------------------------------
        // 파일 확장자 별로 mime type 지정 및 Action 분기
        // -----------------------------------------------------------------------------------------
        if (pFileName.endsWith("pdf")) {
            intent.setDataAndType(pUri, "application/pdf");
        }
        else if (pFileName.endsWith("xls") || pFileName.endsWith("xlsx")) {
            intent.setDataAndType(pUri, "application/vnd.ms-excel");
        }
        else if (pFileName.endsWith("ppt") || pFileName.endsWith("pptx")) {
            intent.setDataAndType(pUri, "application/vnd.ms-powerpoint");
        }
        else if (pFileName.endsWith("doc") || pFileName.endsWith("docx")) {
            intent.setDataAndType(pUri, "application/msword");
        }
        else if (pFileName.endsWith("txt")) {
            intent.setDataAndType(pUri, "text/*");
        }

        else if (pFileName.endsWith("mp3")) {
            intent.setDataAndType(pUri, "audio/*");
        }
        else if (pFileName.endsWith("mp4")) {
            intent.setDataAndType(pUri, "vidio/*");
        }
        else if (  pFileName.endsWith("jpg") || pFileName.endsWith("jpeg")
                || pFileName.endsWith("JPG") || pFileName.endsWith("gif")
                || pFileName.endsWith("png") || pFileName.endsWith("bmp")) {
            intent.setDataAndType(pUri, "image/*");
        }

        else {
            intent.setDataAndType(pUri, "application/*");
        }
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        pContext.startActivity(intent);
    }
}
