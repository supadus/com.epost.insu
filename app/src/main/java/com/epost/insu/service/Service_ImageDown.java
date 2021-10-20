package com.epost.insu.service;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import androidx.annotation.Nullable;

import com.epost.insu.event.OnDownloadProgressEventListener;

import java.io.File;

/**
 * @copyright : (주)프로니스정보기술 | http://www.phronis.co.kr
 *
 * @project   : 모바일슈랑스 구축
 * @pakage   : com.epost.insu.service
 * @fileName  : Service_ImageDown.java
 *
 * @Title     : 배너 이미지 파일 다운로드 서비스
 * @author    : 이수행
 * @created   : 2017-09-01
 * @version   : 1.0
 *
 * @note      : <u>배너 이미지 파일 다운로드 서비스</u><br/>
 *               서비스에서 파일을 다운로드하고 그 결과를 callBack으로 전달한다.
 * ======================================================================
 * 수정 내역
 * NO      날짜          작업자       내용
 * 01      2017-09-01    이수행       최초 등록
 * =======================================================================
 */
public class Service_ImageDown extends Service{

    private final IBinder binder = new LocalBinder();       // 바인더
    private ICallback callback;                              // callback

    private ImageDownloadTask downloadTask;                 // 이미지 다운로드 Task



    /**
     * LocalBinder<br/>
     * {@link #getService()} 함수를 이용해 현재 실행중인 Service를 받을 수 있다.
     */
    public class LocalBinder extends Binder {
        public Service_ImageDown getService(){
            return Service_ImageDown.this;
        }
    }

    /**
     * callback 인터페이스
     */
    public interface ICallback{
        void onDownLoadStart(String p_path, String p_savePath);     // 다운로드 시작
        void onProgress(String p_path, int p_progress);             // 다운로딩 퍼센트
        void onDownLoadComplete(String p_path, String p_savePath);  // 다운로드 완료
        void onCancel(String p_path);                               // 다운로드 취소
        void onError(String p_path);                                // 다운로드 에러
    }

    @Override
    public void onCreate() {
        super.onCreate();

        this.downloadTask = ImageDownloadTask.getInstance();
        this.downloadTask.CF_onProgressEventListener(new OnDownloadProgressEventListener() {
            @Override
            public void onProgress(String p_path, int p_progress) {
                if(callback != null){
                    callback.onProgress(p_path, p_progress);
                }
            }

            @Override
            public void onCancel(String p_path) {

                if(callback != null){
                    callback.onCancel(p_path);
                }

                if(getDownloadingImageCount() <=0){
                    stopSelf();
                }
            }

            @Override
            public void onDownloadComplete(String p_path, String p_savePath) {

                if(callback != null){
                    callback.onDownLoadComplete(p_path, p_savePath);
                }

                if(getDownloadingImageCount() <=0){
                    stopSelf();
                }
            }

            @Override
            public void onPrepare(String p_path, String p_savePath) {
                if(callback != null){
                    callback.onDownLoadStart(p_path, p_savePath);
                }
            }

            @Override
            public void onError(String p_path) {

                if(callback != null){
                    callback.onError(p_path);
                }

                if(getDownloadingImageCount() <=0){
                    stopSelf();
                }
            }
        });
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    @Override
    public int onStartCommand(Intent p_intent, int p_flag, int p_startId){
        super.onStartCommand(p_intent, p_flag, p_startId);

        return START_STICKY;
    }

    /**
     * 다운로드중 또는 대기중인 이미지 수 반환
     * @return
     */
    private int getDownloadingImageCount(){
        if(downloadTask != null){
            return  downloadTask.CF_getDownLoadingCount();
        }

        return  0;
    }

    public ICallback CF_getCallBack(){
        return this.callback;
    }

    /**
     * 콜백 인터페이스 등록
     * @param p_callBack
     */
    public void CF_registerCallBack(ICallback p_callBack){
        this.callback = p_callBack;
    }

    /**
     * 이미지 파일 다운로드
     * @param p_file           파일 저장 경로
     * @param p_filePath      다운로드 이미지 url
     */
    public void downLoad(File p_file, String p_filePath){
        downLoad(p_file,p_filePath,false);
    }

    /**
     * 이미지 파일 다운로드
     * @param p_file
     * @param p_filePath
     * @param p_flagExcutor
     */
    private void downLoad(File p_file, String p_filePath, boolean p_flagExcutor){
        this.downloadTask.CF_download(p_file, p_filePath, p_flagExcutor);
    }

    /**
     * 다운로드 상태 확인 함수
     * @return if true, 이미지 파일 다운 중
     */
    public boolean CF_flagIsDownLoading(){
        return getDownloadingImageCount() > 0;
    }
}
