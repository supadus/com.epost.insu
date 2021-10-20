package com.epost.insu.event;

/**
 * @copyright : (주)프로니스정보기술 | http://www.phronis.co.kr
 *
 * @project   : 모바일슈랑스 구축
 * @pakage   : com.epost.insu.event
 * @fileName  : OnFragmentKeyGuardEventListener.java
 *
 * @Title     : 보안키패드 입력 Fragment 이벤트 인터페이스
 * @author    : 이수행
 * @created   : 2017-11-24
 * @version   : 1.0
 *
 * @note      : <u>보안키패드 입력 Fragment 이벤트 인터페이스</u><br/>
 * ======================================================================
 * 수정 내역
 * NO      날짜          작업자       내용
 * 01      2017-11-24    이수행       최초 등록
 * =======================================================================
 */
public interface OnFragmentKeyGuardEventListener {
    void onCancel();
    void onDone(String p_plainText);
}
