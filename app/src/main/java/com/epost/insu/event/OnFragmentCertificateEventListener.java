package com.epost.insu.event;

/**
 * @copyright : (주)프로니스정보기술 | http://www.phronis.co.kr
 *
 * @project   : 모바일슈랑스 구축
 * @pakage    : com.epost.insu.event
 * @fileName  : OnFragmentCertificateEventListener.java
 *
 * @Title     : 공동인증서 목록 관련 이벤트
 * @author    : 이수행
 * @created   : 2017-11-16
 * @version   : 1.0
 *
 * @note      : <u>공동인증서 목록 관련 이벤트</u><br/>
 * ======================================================================
 * 수정 내역
 * NO      날짜          작업자       내용
 * 01      2017-11-16    이수행       최초 등록
 * =======================================================================
 */
public interface OnFragmentCertificateEventListener {

    /**
     * 인증서 선택 이벤트
     * @param p_indexk          int
     * @param p_flagIsExpire    boolean
     */
    void onSelectedEvent(int p_indexk, boolean p_flagIsExpire);

    /**
     * 인증서 목록 로딩 완료 이벤트
     * @param p_count       int
     */
    void onGetList(int p_count);

    /**
     * 인증서 서명 이벤트
     * @param p_sign        byte[]
     * @param p_vid         String
     */
    void onSigned(byte[] p_sign, String p_vid);

    /**
     * 인증서 패스워드 오류 이벤트
     */
    void onPasswordError();
}
