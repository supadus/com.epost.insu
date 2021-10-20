package com.epost.insu.common;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @copyright : (주)프로니스정보기술 | http://www.phronis.co.kr
 *
 * @project   : 모바일슈랑스 구축
 * @pakage   : com.epost.insu
 * @fileName  : Regex.java
 *
 * @Title     : 정규식 클래스
 * @author    : 이수행
 * @created   : 2017-09-11
 * @version   : 1.0
 *
 * @note      : 입력값 정규식 검사에 사용하는 class
 * ======================================================================
 * 수정 내역
 * NO       날짜            작업자      내용
 * 01       2017-09-11      이수행     최초 등록
 * 02       2019-05-08      노지민     생년월일 정규식 추가
 * =======================================================================
 */
public class Regex {

    private static final String regex_eng_number        = "^[a-zA-Z0-9]*$";
    private static final String regex_mobile            = "^01[016789]-[1-9]{1}[0-9]{2,3}-[0-9]{4}$";
    private static final String regex_mobile_no_hypen   = "^01[016789][1-9]{1}[0-9]{2,3}[0-9]{4}$";
    private static final String regex_phoneMobile       = "^0[0-9]{1,2}-[0-9]{3,4}-[0-9]{4}$";
    private static final String regex_consonantKorean   = "^[ㄱ-ㅎㅏ-ㅣ]*$";
    private static final String regex_8birth            = "^(19|20)[0-9]{2}(0[1-9]|1[012])(0[1-9]|[12][0-9]|3[0-1])$";

    /**
     * 영문자숫자 조합 정규식 체크 함수
     * @param p_str String
     * @return boolean
     */
    public static boolean CF_MacherEngNumber(String p_str){
        Pattern tmp_pattern = Pattern.compile(regex_eng_number);
        Matcher tmp_matcher = tmp_pattern.matcher(p_str);
        return tmp_matcher.matches();
    }

    /**
     * 휴대폰번호 정규식 체크 함수
     * @param p_mobile String
     * @return boolean
     */
    public static boolean CF_MacherMobile(String p_mobile){
        Pattern tmp_pattern = Pattern.compile(regex_mobile);
        Matcher tmp_matcher = tmp_pattern.matcher(p_mobile);
        return tmp_matcher.matches();
    }

    /**
     * 휴대폰번호 정규식 체크 함수
     * @param p_mobile String
     * @return boolean
     */
    public static boolean CF_MacherMobileNoHypen(String p_mobile){
        Pattern tmp_pattern = Pattern.compile(regex_mobile_no_hypen);
        Matcher tmp_matcher = tmp_pattern.matcher(p_mobile);
        return tmp_matcher.matches();
    }

    /**
     * 집전화 휴대폰번호 정규식 체크 함수
     * @param p_phoneOrMobile String
     * @return boolean
     */
    public static boolean CF_MacherPhoneMobile(String p_phoneOrMobile){
        Pattern tmp_pattern = Pattern.compile(regex_phoneMobile);
        Matcher tmp_matcher = tmp_pattern.matcher(p_phoneOrMobile);
        return tmp_matcher.matches();
    }


    public static boolean CF_MacherConsonant(String p_str){
        Pattern tmp_pattern = Pattern.compile(regex_consonantKorean);
        Matcher tmp_matcher = tmp_pattern.matcher(p_str);
        return tmp_matcher.matches();
    }

    /**
     * 생년월일 정규식 체크 함수
     * @param p_mobile String
     * @since 2019-05-08 노지민
     * @return boolean
     */
    public static boolean CF_Macher8Birth(String p_mobile){
        Pattern tmp_pattern = Pattern.compile(regex_8birth);
        Matcher tmp_matcher = tmp_pattern.matcher(p_mobile);
        return tmp_matcher.matches();
    }
}
