package com.epost.insu.data;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * @copyright : (주)프로니스정보기술 | http://www.phronis.co.kr
 *
 * @project   : 모바일슈랑스 구축
 * @pakage   : com.epost.insu.data
 * @fileName  : Data_IUPC40M00.java
 *
 * @Title     : 인증 > 지문인증 > 지문인증해지 (화면 ID : IUPC40M00) - #40 지문인증해지 데이터
 * @author    : 이수행
 * @created   : 2017-12-07
 * @version   : 1.0
 *
 * @note      : <u>인증 > 지문인증 > 지문인증해지 (화면 ID : IUPC40M00) - #40 지문인증해지 데이터</u><br/>
 * ======================================================================
 * 수정 내역
 * NO      날짜          작업자       내용
 * 01      2017-12-07    이수행       최초 등록
 * =======================================================================
 */
public class Data_IUPC40M00 implements Parcelable {

    private String s_proc_dvsn;                 // 처리 구분
    private String s_entr_csno;                 // 가입고객번호(csNo 암호화 값)
    private String s_rgtn_yn_inqy_key;          // 등록여부조회키
    private String s_trmn_id_enc;               // 단말아이디 암호화값
    private String s_serv_code;                 // 서비스 코드
    private String s_tmnt_rqut_msg_len;         // FIDO 해지요청 메시지 길이
    private String s_tmnt_rqut_msg;             // FIDO 해지요청 메시지


    /**
     * 생성자
     */
    public Data_IUPC40M00(){
        setInit();
    }
    private Data_IUPC40M00(Parcel p_parcel){
        setInit();
        readFromParcel(p_parcel);
    }

    private void setInit(){
        s_proc_dvsn = "";
        s_entr_csno = "";
        s_rgtn_yn_inqy_key = "";
        s_trmn_id_enc = "";
        s_serv_code = "";
        s_tmnt_rqut_msg_len = "";
        s_tmnt_rqut_msg = "";
    }


    public String CF_getS_proc_dvsn() {
        return s_proc_dvsn;
    }
    public void CF_setS_proc_dvsn(String s_proc_dvsn) {
        this.s_proc_dvsn = s_proc_dvsn;
    }

    public String CF_getS_entr_csno() {
        return s_entr_csno;
    }
    public void CF_setS_entr_csno(String s_entr_csno) {
        this.s_entr_csno = s_entr_csno;
    }

    public String CF_getS_rgtn_yn_inqy_key() {
        return s_rgtn_yn_inqy_key;
    }
    public void CF_setS_rgtn_yn_inqy_key(String s_rgtn_yn_inqy_key) { this.s_rgtn_yn_inqy_key = s_rgtn_yn_inqy_key;}

    public String CF_getS_trmn_id_enc() {
        return s_trmn_id_enc;
    }
    public void CF_setS_trmn_id_enc(String s_trmn_id_enc) {
        this.s_trmn_id_enc = s_trmn_id_enc;
    }

    public String CF_getS_serv_code() {
        return s_serv_code;
    }
    public void CF_setS_serv_code(String s_serv_code) {
        this.s_serv_code = s_serv_code;
    }

    public String CF_getS_tmnt_rqut_msg_len() {
        return s_tmnt_rqut_msg_len;
    }
    public void CF_setS_tmnt_rqut_msg_len(String s_tmnt_rqut_msg_len) { this.s_tmnt_rqut_msg_len = s_tmnt_rqut_msg_len; }

    public String CF_getS_tmnt_rqut_msg() {
        return s_tmnt_rqut_msg;
    }
    public void CF_setS_tmnt_rqut_msg(String s_tmnt_rqut_msg) { this.s_tmnt_rqut_msg = s_tmnt_rqut_msg; }

    @Override
    public int describeContents() {
        return 0;
    }


    /**
     * Parcel Write
     */
    public void writeToParcel(Parcel p_parcel, int p_flags) {

        p_parcel.writeString(s_proc_dvsn);
        p_parcel.writeString(s_entr_csno);
        p_parcel.writeString(s_rgtn_yn_inqy_key);
        p_parcel.writeString(s_trmn_id_enc);
        p_parcel.writeString(s_serv_code);
        p_parcel.writeString(s_tmnt_rqut_msg_len);
        p_parcel.writeString(s_tmnt_rqut_msg);
    }

    /**
     * Parcel Read
     * @param p_parcel
     */
    private void readFromParcel(Parcel p_parcel)
    {
        s_proc_dvsn = p_parcel.readString();
        s_entr_csno = p_parcel.readString();
        s_rgtn_yn_inqy_key = p_parcel.readString();
        s_trmn_id_enc = p_parcel.readString();
        s_serv_code = p_parcel.readString();
        s_tmnt_rqut_msg_len = p_parcel.readString();
        s_tmnt_rqut_msg = p_parcel.readString();
    }


    /**
     * Parcel Creator
     */
    public static Parcelable.Creator<Data_IUPC40M00> CREATOR = new Parcelable.Creator<Data_IUPC40M00>() {

        public Data_IUPC40M00 createFromParcel(Parcel source) {
            return new Data_IUPC40M00(source);
        }

        public Data_IUPC40M00[] newArray(int size) {
            return new Data_IUPC40M00[size];
        }
    };
}
