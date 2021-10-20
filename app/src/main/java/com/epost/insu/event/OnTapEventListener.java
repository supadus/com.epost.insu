package com.epost.insu.event;

import com.epost.insu.common.CustomViewPager;

/**
 * @copyright : (주)프로니스정보기술 | http://www.phronis.co.kr
 *
 * @project   : 모바일슈랑스 구축
 * @pakage   : com.epost.insu.event
 * @fileName  : OnTapEventListener.java
 *
 * @Title     : Tap 이벤트 리스너
 * @author    : 이수행
 * @created   : 2017-08-07
 * @version   : 1.0
 *
 * @note      : {@link CustomViewPager} 에서 사용
 * ======================================================================
 * 수정 내역
 * NO      날짜          작업자       내용
 * 01      2017-08-07    이수행       최초 등록
 * =======================================================================
 */
public interface OnTapEventListener {
    void onTap(int p_index);
}
