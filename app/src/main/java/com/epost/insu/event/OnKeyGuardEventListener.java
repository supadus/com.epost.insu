package com.epost.insu.event;

/**
 * @copyright : 우정사업정보센터
 *
 * @project   : 블록체인 간편인증 개발
 * @pakage   : com.epost.insu.event
 * @fileName  : OnKeyGuardEventListener.java
 *
 * @Title     : 보안키패드 이벤트 인터페이스
 * @author    : 이경민
 * @created   : 2020-03-04
 * @version   : 1.0
 *
 * @note      : <u>보안키패드 이벤트 인터페이스</u><br/>
 * ======================================================================
 * 수정 내역
 * NO      날짜          작업자       내용
 * 01      2020-03-04    이경민       최초 등록
 * =======================================================================
 */
public interface OnKeyGuardEventListener {
    public void onCancel();
    public void onDone(String p_plainText);
    public void onInput(int i);
    public void onMinTextSizeCallback();
    public void onMaxTextSizeCallback();
}
