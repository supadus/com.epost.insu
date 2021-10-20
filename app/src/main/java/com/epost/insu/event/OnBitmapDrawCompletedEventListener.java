package com.epost.insu.event;

import android.graphics.Bitmap;

/**
 * @copyright : (주)프로니스정보기술 | http://www.phronis.co.kr
 *
 * @project   : 모바일슈랑스 구축
 * @pakage   : com.epost.insu.event
 * @fileName  : OnBitmapDrawCompletedEventListener.java
 *
 * @Title     : Bitmap Draw 완료 이벤트 리스너
 * @author    : 이수행
 * @created   : 2017-11-21
 * @version   : 1.0
 *
 * @note      : <u>Bitmap Draw 완료 이벤트 리스너</u><br/>
 * ======================================================================
 * 수정 내역
 * NO      날짜          작업자       내용
 * 01      2017-11-21    이수행       최초 등록
 * =======================================================================
 */
public interface OnBitmapDrawCompletedEventListener {
    void onDraw(String p_thumbId, Bitmap p_bitmap);
}
