package com.epost.insu.common;


import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.util.Calendar;
import java.util.Locale;

/**
 * @copyright : (주)프로니스정보기술 | http://www.phronis.co.kr
 *
 * @project   : 모바일슈랑스 구축
 * @pakage   : com.epost.insu.common
 * @fileName  : FormatUtils.java
 *
 * @Title     : 공통 > Format
 * @author    : 이수행
 * @created   : 2017-06-22
 * @version   : 1.0
 *
 * @note      : <u>공통으로 사용하는 Format관련 클래스</u><br/>
 * ======================================================================
 * 수정 내역
 * NO      날짜          작업자       내용
 * 01      2017-12-19    노지민     : 최초 등록
 * =======================================================================
 */
public final class FormatUtils {

    /**
     * YYYYMMDD 날짜 형식을 YYYY.MM.DD 로 변환한다.
     * @param p_strDate
     * @return
     */
    public static String CF_convertDate(String p_strDate){

        String tmp_convertedData = p_strDate;

        if(p_strDate.length() == 8){
            tmp_convertedData = p_strDate.substring(0,4)+"."+p_strDate.substring(4,6)+"."+p_strDate.substring(6,8);
        }
        return tmp_convertedData;
    }

    /**
     * 오늘 날짜를 YYYY.MM.dd 포멧으로 반환한다.
     *
     * @return String
     */
    public static String CF_getTodayStr() {
        Calendar tmp_calendar = Calendar.getInstance();
        return "" + tmp_calendar.get(Calendar.YEAR) + "." + String.format(Locale.getDefault(), "%02d", tmp_calendar.get(Calendar.MONTH) + 1) + "." + String.format(Locale.getDefault(), "%02d", tmp_calendar.get(Calendar.DAY_OF_MONTH));
    }


    /**
     * UTF-8 -> EUC-KR 문자셋 변환시 오류검출 <br />
     * - CP949로 문자셋을 변환 후 비교한다. (euc-kr로 변환시 정상변환됨)  <br />
     * @return  String      정상 = "" / 오류 = [변환문자열]
     */
    public static String CF_checkUtf8toEucKr(String utfStr) {
        LogPrinter.CF_debug("!----------------------------------------------------------");
        LogPrinter.CF_debug("!-- FormatUtils.CF_checkUtf8toEucKr() --EUC-KR 문자셋 변환시 오류검출");
        LogPrinter.CF_debug("!----------------------------------------------------------");
        LogPrinter.CF_debug("!---- utfStr:::::" + utfStr + "[length: " + utfStr.length() + "]");

        byte[] eucKrBuffer = utfStr.getBytes(Charset.forName("euc-kr"));
        String cp949Str = "";
        try {
            cp949Str = new String(eucKrBuffer, "cp949");
            LogPrinter.CF_debug("!---- cp949St::::" + cp949Str + "[length: " + cp949Str.length() + "]");

        } catch (UnsupportedEncodingException e) {
            e.getMessage();
        }

        if (utfStr.equals(cp949Str)) {
            return "";
        } else {
            return cp949Str;
        }
    }
}
