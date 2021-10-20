package com.epost.insu.data;

import android.os.Parcel;
import android.os.Parcelable;


/**
 * @copyright : (주)프로니스정보기술 | http://www.phronis.co.kr
 *
 * @project   : 모바일슈랑스 구축
 * @pakage   : com.epost.insu.data
 * @fileName  : Data_IUPC32M00.java
 *
 * @Title     : IUFC32M00(지문인증 등록)에서 사용하는 데이터 클래스
 * @author    : 이수행
 * @created   : 2017-10-16
 * @version   : 1.0
 *
 * @note      : <u>IUFC32M00(지문인증 등록)에서 사용하는 데이터 클래스</u><br/>
 *               공동 FIDO 등록에 필요한 데이터만 갖고 있다.
 * ======================================================================
 * 수정 내역
 * NO      날짜          작업자       내용
 * 01      2017-10-16    이수행       최초 등록
 * =======================================================================
 */
public class Data_IUPC32M00 implements Parcelable {

    private String s_entr_csno;             // '공동 FIDO등록 선거래' 거래 결과 중 가입고객번호
    private String tlgr_chas_no;            // '공동 FIDO등록 선거래' 거래 결과 중 전문추적번호
    private String not_rcgn_cpat_no_len;   // 비식별호환번호길이
    private String not_rcgn_cpat_no;        // 비식별호환번호

    private String s_serv_code;             // 서비스 코드
    private String s_rgtn_frml_code;       // 등록방식 코드
    private String s_auth_tech_code;       // 등록방식 코드

    /**
     * 생성자
     */
    public Data_IUPC32M00(){
        setInit();
    }
    private Data_IUPC32M00(Parcel p_parcel){
        setInit();
        readFromParcel(p_parcel);
    }

    private void setInit(){
        s_entr_csno = "";
        tlgr_chas_no = "";
        not_rcgn_cpat_no_len = "";
        not_rcgn_cpat_no = "";

        s_serv_code = "";
        s_rgtn_frml_code = "";
        s_auth_tech_code = "";
    }

    public String CF_getS_entr_csno() {
        return s_entr_csno;
    }
    public void CF_setS_entr_csno(String s_entr_csno) {
        this.s_entr_csno = s_entr_csno;
    }

    public String CF_getTlgr_chas_no() {
        return tlgr_chas_no;
    }
    public void CF_setTlgr_chas_no(String tlgr_chas_no) {
        this.tlgr_chas_no = tlgr_chas_no;
    }

    public String CF_getNot_rcgn_cpat_no_len() {
        return not_rcgn_cpat_no_len;
    }
    public void CF_setNot_rcgn_cpat_no_len(String not_rcgn_cpat_no_len) { this.not_rcgn_cpat_no_len = not_rcgn_cpat_no_len;}

    public String CF_getNot_rcgn_cpat_no() {
        return not_rcgn_cpat_no;
    }
    public void CF_setNot_rcgn_cpat_no(String not_rcgn_cpat_no) { this.not_rcgn_cpat_no = not_rcgn_cpat_no; }

    public String CF_getS_serv_code() {
        return s_serv_code;
    }
    public void CF_setS_serv_code(String s_serv_code) {
        this.s_serv_code = s_serv_code;
    }

    public String CF_getS_rgtn_frml_code() {
        return s_rgtn_frml_code;
    }
    public void CF_setS_rgtn_frml_code(String s_rgtn_frml_code) { this.s_rgtn_frml_code = s_rgtn_frml_code; }

    public String CF_getS_auth_tech_code() {
        return s_auth_tech_code;
    }
    public void CF_setS_auth_tech_code(String s_auth_tech_code) { this.s_auth_tech_code = s_auth_tech_code; }

    @Override
    public int describeContents() {
        return 0;
    }


    /**
     * Parcel Write
     */
    public void writeToParcel(Parcel p_parcel, int p_flags) {

        p_parcel.writeString(s_entr_csno);
        p_parcel.writeString(tlgr_chas_no);
        p_parcel.writeString(not_rcgn_cpat_no_len);
        p_parcel.writeString(not_rcgn_cpat_no);

        p_parcel.writeString(s_serv_code);
        p_parcel.writeString(s_rgtn_frml_code);
        p_parcel.writeString(s_auth_tech_code);
    }

    /**
     * Parcel Read
     * @param p_parcel
     */
    private void readFromParcel(Parcel p_parcel)
    {
        s_entr_csno = p_parcel.readString();
        tlgr_chas_no = p_parcel.readString();
        not_rcgn_cpat_no_len = p_parcel.readString();
        not_rcgn_cpat_no = p_parcel.readString();

        s_serv_code = p_parcel.readString();
        s_rgtn_frml_code = p_parcel.readString();
        s_auth_tech_code = p_parcel.readString();
    }


    /**
     * Parcel Creator
     */
    public static Parcelable.Creator<Data_IUPC32M00> CREATOR = new Parcelable.Creator<Data_IUPC32M00>() {

        public Data_IUPC32M00 createFromParcel(Parcel source) {
            return new Data_IUPC32M00(source);
        }

        public Data_IUPC32M00[] newArray(int size) {
            return new Data_IUPC32M00[size];
        }
    };
}
