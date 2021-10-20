package com.epost.insu.event;

/**
 * @copyright : (주)프로니스정보기술 | http://www.phronis.co.kr
 *
 * @project   : 모바일슈랑스 구축
 * @pakage   : com.epost.insu.event
 * @fileName  : OnDownloadProgressEventListener.java
 *
 * @Title     : 프로그레스 이벤트 리스너
 * @author    : 이수행
 * @created   : 2017-11-17
 * @version   : 1.0
 *
 * @note      : <u>프로그레스 이벤트 리스너</u><br/>
 * ======================================================================
 * 수정 내역
 * NO      날짜          작업자       내용
 * 01      2017-11-17    이수행       최초 등록
 * =======================================================================
 */
public interface OnDownloadProgressEventListener {

    /**
     * 프로그레스 진행
     * @param p_path
     * @param p_progress
     */
    void onProgress(String p_path, int p_progress);

    /**
     * 프로그레스 취소
     * @param p_path
     */
    void onCancel(String p_path);

    /**
     * 다운로드 완료
     * @param p_path
     * @param p_savePath
     */
    void onDownloadComplete(String p_path, String p_savePath);

    /**
     * 다운로드 시작 준비
     * @param p_path
     * @param p_savePath
     */
    void onPrepare(String p_path, String p_savePath);

    /**
     * 에러
     * @param p_path
     */
    void onError(String p_path);
}
