package com.epost.insu.common;

import android.text.InputFilter;
import android.text.Spanned;

/**
 * @copyright : (주)프로니스정보기술 | http://www.phronis.co.kr
 *
 * @project   : 모바일슈랑스 구축
 * @pakage   : com.epost.insu.common
 * @fileName  : EmojiFilter.java
 *
 * @Title     : 이모티콘 제외 InputFilter
 * @author    : 이수행
 * @created   : 2017-10-26
 * @version   : 1.0
 *
 * @note      : <u>이모티콘 제외 InputFilter</u><br/>
 *               키패드로 입력 가능한 이모티콘 입력 방지 필터
 * ======================================================================
 * 수정 내역
 * NO      날짜          작업자       내용
 * 01      2017-10-26    이수행       최초 등록
 * =======================================================================
 */
class EmojiFilter implements InputFilter {

    public EmojiFilter(){

    }

    @Override
    public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
        for (int index = start; index < end; index++) {

            int type = Character.getType(source.charAt(index));

            if (type == Character.SURROGATE) {
                return "";
            }
        }
        return null;
    }
}