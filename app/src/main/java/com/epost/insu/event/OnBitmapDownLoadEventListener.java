package com.epost.insu.event;

import android.graphics.Bitmap;

/**
 * @copyright : (주)프로니스정보기술 | http://www.phronis.co.kr
 *
 * @project   : 모바일슈랑스 구축
 * @pakage   : com.epost.insu.event
 * @fileName  : OnBitmapDownLoadEventListener.java
 *
 * @Title     : Bitmap 다운로드 이벤트 리스너
 * @author    : 이수행
 * @created   : 2017-11-21
 * @version   : 1.0
 *
 * @note      : <u>Bitmap 다운로드 이벤트 리스너</u><br/>
 * ======================================================================
 * 수정 내역
 * NO      날짜          작업자       내용
 * 01      2017-11-21    이수행       최초 등록
 * =======================================================================
 */
public interface OnBitmapDownLoadEventListener {
    void onDownLoad(String p_path, Bitmap p_bitmap);

    @SuppressWarnings("unused")
    void onError(String p_path);
}
