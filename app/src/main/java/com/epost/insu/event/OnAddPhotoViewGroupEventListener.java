package com.epost.insu.event;

import android.view.View;

import com.epost.insu.control.AddPhotoViewGroup;

/**
 * @copyright : (주)프로니스정보기술 | http://www.phronis.co.kr
 *
 * @project   : 모바일슈랑스 구축
 * @pakage   : com.epost.insu.event
 * @fileName  : OnAddPhotoViewGroupEventListener.java
 *
 * @Title     : {@link AddPhotoViewGroup} 애서 사용하는 Event Listener
 * @author    : 이수행
 * @created   : 2017-08-14
 * @version   : 1.0
 *
 * @note      : {@link AddPhotoViewGroup} 에서 사용하는 Event Listener<br/>
 *               {@link #onReqAddPhoto(AddPhotoViewGroup)} 사진 추가 버튼 클릭 이벤트<br/>
 *               {@link #onReqTakePicture(AddPhotoViewGroup)} 촬영 버튼 클릭 이벤트<br/>
 * ======================================================================
 * 수정 내역
 * NO      날짜          작업자       내용
 * 01      2017-08-14    이수행       최초 등록
 * =======================================================================
 */
public interface OnAddPhotoViewGroupEventListener {
    void onReqAddPhoto(AddPhotoViewGroup p_view);
    void onReqTakePicture(AddPhotoViewGroup p_view);
    void onDeletedPhoto(AddPhotoViewGroup p_view);
    void onDeletCanceled(AddPhotoViewGroup p_view, View p_viewDel);
}
