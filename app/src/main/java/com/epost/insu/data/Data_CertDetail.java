package com.epost.insu.data;


import com.epost.insu.common.LogPrinter;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/**
 * @copyright : (주)프로니스정보기술 | http://www.phronis.co.kr
 *
 * @project   : 모바일슈랑스 구축
 * @pakage   : com.epost.insu.data
 * @fileName  : Data_CertDetail.java
 *
 * @Title     : 공동인증서 상세정보 데이터 클래스
 * @author    : 이수행
 * @created   : 2017-06-30
 * @version   : 1.0
 *
 * @note      : <u>공동인증서 상세정보 데이터 클래스</u><br/>
 * ======================================================================
 * 수정 내역
 * NO      날짜          작업자       내용
 * 01      2017-06-30    이수행       최초 등록
 * =======================================================================
 */
public class Data_CertDetail {

    private String SubjectDN = null;            // 주체자
    private String CA = null;                    // 발급자(ex CA131000002)
    private String OID = null;                   // 용도(ex 1 2 410 200004 2 202 7)
    private String KeyUsage = null;             // 목적(ex 서명용 암호용)
    private String ExpirationTo = null;        // 유효기간 To
    private String ExpirationFrom = null;      // 유효기간 From

    private String CA_Readable = null;         // 발급자(ex 행정안전부)
    private String USER = null;                 // 사용자(ex 홍길동()34322342...)
    private String USER_Name = null;           // 사용자 이름(ex 홍길동)
    private String OID_Readable = null;        // 용도(ex 은행개인)
    private Boolean isExpire = null;            // 유효기관 초과
    private Integer index = null;               // 인증서 인덱스




    public String getSubjectDN() {
        return SubjectDN;
    }

    public String getCA() {
        return CA;
    }

    public String getOID() {
        return OID;
    }

    public String getKeyUsage() {
        return KeyUsage;
    }

    public String getExpirationTo() {
        return ExpirationTo;
    }

    public String getExpirationFrom() {
        return ExpirationFrom;
    }

    public void setSubjectDN(String subjectDN) {
        SubjectDN = subjectDN;
    }

    public void setCA(String CA) {
        this.CA = CA;
    }

    public void setOID(String OID) {
        this.OID = OID;
    }

    public void setKeyUsage(String keyIsage) {
        KeyUsage = keyIsage;
    }

    public void setExpirationTo(String expirationTo) {
        ExpirationTo = expirationTo;
        setExpire(checkExpireDate());
    }

    public void setExpirationFrom(String expirationFrom) {
        ExpirationFrom = expirationFrom;
    }

    public String getCA_Readable() {
        return CA_Readable;
    }

    public void setCA_Readable(String CA_Readable) {
        this.CA_Readable = CA_Readable;
    }

    public String getUSER() {
        return USER;
    }

    public void setUSER(String USER) {
        this.USER = USER;
    }

    public String getOID_Readable() {
        return OID_Readable;
    }

    public void setOID_Readable(String OID_Readable) {
        this.OID_Readable = OID_Readable;
    }

    public String getUSER_Name() {
        return USER_Name;
    }

    public void setUSER_Name(String USER_Name) {
        this.USER_Name = USER_Name;
    }

    public Boolean getExpire() {
        return isExpire;
    }

    private void setExpire(Boolean expire) {
        isExpire = expire;
    }

    public void setIndex(int p_index){
        index = p_index;
    }

    public Integer getIndex(){
        return  index;
    }

    /**
     * 인증서 유효기간을 확인한다.
     * @return
     */
    private boolean checkExpireDate(){

        SimpleDateFormat tmp_simpleFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());

        try {
            Calendar tmp_calendar = Calendar.getInstance();
            tmp_calendar.set(Calendar.HOUR_OF_DAY,0);
            tmp_calendar.set(Calendar.MINUTE,0);
            tmp_calendar.set(Calendar.SECOND,0);
            tmp_calendar.set(Calendar.MILLISECOND,0);
            Date tmp_now = tmp_calendar.getTime();
            Date tmp_expireDate = tmp_simpleFormat.parse(ExpirationTo);

            long tmp_duration = tmp_expireDate.getTime() - tmp_now.getTime();

            if(tmp_duration >=0){
                return false;
            }


        } catch (ParseException e) {
            LogPrinter.CF_line();
            LogPrinter.CF_debug("날짜 형식 변환 에러 : 입력값 : "+ExpirationTo);
            LogPrinter.CF_line();
        }

        return true;
    }
}
