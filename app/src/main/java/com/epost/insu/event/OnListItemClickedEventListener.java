package com.epost.insu.event;

/**
 * @copyright : (주)프로니스정보기술 | http://www.phronis.co.kr
 *
 * @project   : 모바일슈랑스 구축
 * @pakage   : com.epost.insu.event
 * @fileName  : OnListItemClickedEventListener.java
 *
 * @Title     : 아이템클릭 이벤트 리스너 for Adapter
 * @author    : 이수행
 * @created   : 2017-11-17
 * @version   : 1.0
 *
 * @note      : <u>아이템클릭 이벤트 리스너 for Adapter</u><br/>
 * ======================================================================
 * 수정 내역
 * NO      날짜          작업자       내용
 * 01      2017-11-17    이수행       최초 등록
 * =======================================================================
 */
public interface OnListItemClickedEventListener {
    void onClick(int p_index);
}
