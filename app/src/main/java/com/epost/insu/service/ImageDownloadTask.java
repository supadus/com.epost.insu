package com.epost.insu.service;

import android.os.AsyncTask;
import android.text.TextUtils;

import com.epost.insu.common.LogPrinter;
import com.epost.insu.event.OnDownloadProgressEventListener;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.util.ArrayList;

/**
 * @copyright : (주)프로니스정보기술 | http://www.phronis.co.kr
 *
 * @project   : 모바일슈랑스 구축
 * @pakage   : com.epost.insu.service
 * @fileName  : ImageDownloadTask.java
 *
 * @Title     : 사진 다운로드 Task
 * @author    : 이수행
 * @created   : 2017-09-01
 * @version   : 1.0
 *
 * @note      : <u>사진 다운로드 Task</u><br/>
 *               AsyncTask 를 이용해 백그라운드에서 사진을 다운로드한다.<br/>
 *               {@link android.app.DownloadManager} 미사용
 * ======================================================================
 * 수정 내역
 * NO      날짜          작업자       내용
 * 01      2017-09-01    이수행       최초 등록
 * =======================================================================
 */
public class ImageDownloadTask {

    private static ImageDownloadTask task;
    private OnDownloadProgressEventListener listener;

    private ArrayList<Integer> arrDownLoadPercent;
    private ArrayList<String> arrDownLoadPath;
    private ArrayList<DownLoadTask> arrTask;

    /**
     * ImageDownloadTask 객체 반환
     * @return
     */
    public static ImageDownloadTask getInstance(){

        if(task == null){
            synchronized (ImageDownloadTask.class){
                if(task == null){
                    task = new ImageDownloadTask();
                }
            }
        }

        return task;
    }

    /**
     * 생성자
     */
    private ImageDownloadTask(){

        arrDownLoadPercent = new ArrayList<>();
        arrDownLoadPath = new ArrayList<>();
        arrTask = new ArrayList<>();
    }

    /**
     * 이미지 파일 다운로드
     * @param p_fileSave
     * @param p_filePath
     */
    public void CF_download(File p_fileSave, String p_filePath){
        CF_download(p_fileSave,p_filePath,false);
    }


    /**
     * 이미지 파일 다운로드
     * @param p_fileSave    저장할 파일 경로
     * @param p_filePath    다운로드 받을 이미지 url
     * @param p_flagExcutor if true, AsyncTask Excutor 실행
     */
    public void CF_download(File p_fileSave, String p_filePath, boolean p_flagExcutor){

        if(!TextUtils.isEmpty(p_filePath)){
            if(p_flagExcutor){
                new DownLoadTask(p_fileSave, p_filePath).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            }else {
                new DownLoadTask(p_fileSave, p_filePath).execute();
            }
        }

    }


    /**
     * 다운로드 Task
     */
    private class DownLoadTask extends AsyncTask<Void, Long, Boolean> {

        private File fileSave;              // 저장할 파일 경로
        private String filePath;            // 다운로드 받을 이미지 파일 url
        private long prevProgress=-1;      // 프로그레스바 값

        /**
         * 생성자
         * @param p_file
         * @param p_filePath
         */
        public DownLoadTask(File p_file, String p_filePath){
            fileSave = p_file;
            filePath = p_filePath;
        }

        /**
         * 실행 이전
         */
        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            arrDownLoadPath.add(filePath);
            arrDownLoadPercent.add(0);
            arrTask.add(this);

            if(listener != null){
                listener.onPrepare(filePath, fileSave.getAbsolutePath());
            }
        }

        @Override
        protected void onPostExecute(Boolean p_flagComplete) {

            int tmp_index = arrDownLoadPath.indexOf(filePath);

            if(tmp_index >=0){
                arrDownLoadPath.remove(tmp_index);
                arrDownLoadPercent.remove(tmp_index);
            }

            arrTask.remove(this);

            if(listener != null){
                if(p_flagComplete){
                    listener.onDownloadComplete(filePath, fileSave.getAbsolutePath());
                }else{
                    listener.onError(filePath);
                }
            }
        }

        @Override
        protected void onCancelled(Boolean aBoolean) {
            super.onCancelled(aBoolean);

            int tmp_index = arrDownLoadPath.indexOf(filePath);

            if(tmp_index >=0){
                arrDownLoadPath.remove(tmp_index);
                arrDownLoadPercent.remove(tmp_index);
            }

            arrTask.remove(this);

            // 파일 삭제
            if(fileSave != null && fileSave.exists()){
                fileSave.delete();
            }

            if(listener != null){
                listener.onCancel(filePath);
            }
        }

        @Override
        protected Boolean doInBackground(Void... params) {

            boolean tmp_flagCompleted = false;

            if(fileSave != null){

                FileOutputStream tmp_fileOutputStream = null;
                InputStream tmp_inputStream = null;


                    byte[] buffer = new byte[1024];
                    int len1 = 0;
                    long total = 0;


                try {
                    URL u = new URL(filePath);

                    HttpURLConnection c = (HttpURLConnection) u.openConnection();

                    if(fileSave.exists()){
                        fileSave.delete();
                    }

                    c.setRequestMethod("POST");
                    c.setDoOutput(true);
                    c.connect();

                    long lengthOfFile = c.getContentLength();

                    tmp_fileOutputStream = new FileOutputStream(fileSave);
                    tmp_inputStream = c.getInputStream();


                    while ((len1 = tmp_inputStream.read(buffer)) > 0) {

                        if(this.isCancelled()){

                            if(fileSave.exists()){
                                fileSave.delete();
                            }
                            return null;
                        }

                        total += len1;
                        tmp_fileOutputStream.write(buffer, 0, len1);
                        long progress = (long) Math.ceil(((total*100)/lengthOfFile));

                        if(progress != prevProgress){
                            prevProgress = progress;
                            publishProgress(progress,total,lengthOfFile);
                        }
                    }

                    if(fileSave.length() >= lengthOfFile) {
                        tmp_flagCompleted = true;
                    }
                } catch (MalformedURLException e) {
                    LogPrinter.CF_line();
                    LogPrinter.CF_debug("이미지 다운로드 실패 => "+filePath);
                } catch (FileNotFoundException e) {
                    LogPrinter.CF_line();
                    LogPrinter.CF_debug("이미지 다운로드 실패 => "+filePath);
                } catch (ProtocolException e) {
                    LogPrinter.CF_line();
                    LogPrinter.CF_debug("이미지 다운로드 실패 => "+filePath);
                } catch (IOException e) {
                    LogPrinter.CF_line();
                    LogPrinter.CF_debug("이미지 다운로드 실패 => "+filePath);
                } finally {

                    // output stream close
                    if(tmp_fileOutputStream != null){
                        try {
                            tmp_fileOutputStream.close();
                        } catch (IOException e) {
                            LogPrinter.CF_line();
                            LogPrinter.CF_debug("Stream Close 실패");
                        }
                    }

                    // input stream close
                    if(tmp_inputStream != null){
                        try {
                            tmp_inputStream.close();
                        } catch (IOException e) {
                            LogPrinter.CF_line();
                            LogPrinter.CF_debug("Stream Close 실패");
                        }
                    }
                }
            }

            return tmp_flagCompleted;
        }

        @Override
        protected void onProgressUpdate(Long... progress) {

            long tmp_progress = progress[0];
            if(tmp_progress >= 0 || tmp_progress <= 100){

                int tmp_index = arrDownLoadPath.indexOf(filePath);

                if(tmp_index >= 0){
                    arrDownLoadPercent.set(tmp_index, (int)tmp_progress);
                }

                if(listener != null){
                    listener.onProgress(filePath, (int)tmp_progress);
                }
            }
        }
    }

    public int CF_getDownLoadingCount(){
        return this.arrDownLoadPath.size();
    }

    public void CF_onProgressEventListener(OnDownloadProgressEventListener p_listener){
        this.listener = p_listener;
    }
}
