package com.epost.insu.module;

import android.content.Context;

import com.epost.insu.common.LogPrinter;
import com.epost.insu.psmobile.PSMobileActivity;

/**
 * 사진촬영모듈 제어 class
 * @since     : 1.4.2
 * @version   : 1.0
 * @author    : NJM
 * <pre>
 * ======================================================================
 * 1.4.2    NJM_20201028    [모바일 사진촬영 패키지 도입] 최초등록
 * =======================================================================
 * copyright : (주)프로니스정보기술 | http://www.phronis.co.kr
 * </pre>
 */
public class PSMobileModule {
    private final Context context;

    /**
     * 생성자
     * @param p_context Context
     */
    public PSMobileModule(Context p_context) {
        context = p_context;
    }
    /**
     * 구비서류 첨부 이미지 삭제
     */
    public void clearFiles(){
        LogPrinter.CF_debug("!----------------------------------------------------------");
        LogPrinter.CF_debug("!-- PSMobileModule.clearFiles()");
        LogPrinter.CF_debug("!----------------------------------------------------------");

        PSMobileActivity.clearFiles(context);

        // TODO : 임시파일 수동삭제
        /*
        File tmp_fileDir = new File(getApplicationContext().getFilesDir(),EnvConfig.IMAGE_TEMP_FOLER);
       // File tmp_fileDir = new File(Environment.getExternalStorageDirectory(),EnvConfig.IMAGE_TEMP_FOLER);

        if(tmp_fileDir.exists() && tmp_fileDir.isDirectory()){
            File[] tmp_files = tmp_fileDir.listFiles();

            if(tmp_files != null){
                for(File file : tmp_files){
                    if(file != null && file.exists()){
                        file.delete();
                    }
                }
            }
            tmp_fileDir.delete();
        }
        */
    }
}
