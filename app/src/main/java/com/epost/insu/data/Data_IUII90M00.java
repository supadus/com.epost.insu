package com.epost.insu.data;

import android.os.Parcel;
import android.os.Parcelable;

import com.epost.insu.common.LogPrinter;

import java.util.HashMap;

/**
 * 보험금청구 > 자녀청구 > 보험금청구접수신청
 * @since     :
 * @version   : 1.3
 * @author    : LSH
 * <pre>
 *     보험금청구시 서버에 저장하여야할 데이터 클래스
 * ======================================================================
 *          YJH_            최초 등록
 * 1.5.3    NJM_20210422    [자녀청구 예금주소실 수정]
 * =======================================================================
 * copyright : (주)프로니스정보기술 | http://www.phronis.co.kr
 * </pre>
 */
public class Data_IUII90M00 extends Data_IUII10M00 {

    private String s_rctr_csno;                 // 청구자 고객번호
    private String s_rctr_relt_dvsn;            // 청구자의 계약관계 (계약자/수익자) (30:계약자, 42:입원(장해)수익자)
    private String s_rctr_type;                 // 부모유형 (접수자의 자녀와의 관계-1:부,2:모)

    private String s_bnfc_nm;                   // 수익자명 (입원장해수익자)
    private String s_bnfc_csno;                 // 수익자고객번호

    //private String s_rel_another;             // 접수자 외의 계약관계 (계약자/수익자)
    //private String s_rel_type;                // 부모유형 (접수자 외 부모의 자녀와의 관계-1:부,2:모)
    private String s_rel_agree_type;            // 친권자동의유형 (1:공동친권, 2:사망, 3:이혼, 4:기타)
    private String s_pipa_nm_1;                 // 친권자명1
    private String s_pipa_nm_2;                 // 친권자명2

    private String s_acdp_nm;                   // 피보험자명
    private String s_acdp_rrno1;                // 피보험자 실명번호
    private String s_acdp_rrno2_enc;            // 피보험자 실명번호 암호화

    /**
     * 생성자
     */
    public Data_IUII90M00(){
        setInit();
    }
    private Data_IUII90M00(Parcel p_parcel){
        setInit();
        readFromParcel(p_parcel);
    }

    /**
     * 초기 세팅 함수
     */
    public void setInit(){
        super.setInit();

        s_rctr_csno         = "";
        s_rctr_relt_dvsn    = "";
        s_rctr_type         = "";
        s_bnfc_nm           = "";
        s_bnfc_csno         = "";
        //s_rel_another       = "";
        //s_rel_type          = "";
        s_rel_agree_type    = "";
        s_pipa_nm_1         = "";
        s_pipa_nm_2         = "";
        s_acdp_nm           = "";
        s_acdp_rrno1        = "";
        s_acdp_rrno2_enc    = "";
    }

    //----------------------------------------------------------------------------------------------
    // Getter & Setter
    //----------------------------------------------------------------------------------------------
    public String CF_getS_rctr_csno() {
        return s_rctr_csno;
    }
    public void CF_setS_rctr_csno(String s_rctr_csno) {
        this.s_rctr_csno = s_rctr_csno;
    }

    public String CF_getS_rctr_relt_dvsn() {
        return s_rctr_relt_dvsn;
    }
    public void CF_setS_rctr_relt_dvsn(String s_rctr_relt_dvsn) {
        this.s_rctr_relt_dvsn = s_rctr_relt_dvsn;
    }

    public String CF_getS_rctr_type() {
        return s_rctr_type;
    }
    public void CF_setS_rctr_type(String s_rctr_type) {
        this.s_rctr_type = s_rctr_type;
    }

    public String CF_getS_bnfc_nm() { return s_bnfc_nm; }
    public void CF_setS_bnfc_nm(String s_bnfc_nm){
        this.s_bnfc_nm = s_bnfc_nm;
    }

    public String CF_getS_bnfc_csno() {
        return s_bnfc_csno;
    }
    public void CF_setS_bnfc_csno(String s_bnfc_csno) {
        this.s_bnfc_csno = s_bnfc_csno;
    }

//    public String CF_getS_rel_another() { return s_rel_another; }
//    public void CF_setS_rel_another(String s_rel_another) { this.s_rel_another = s_rel_another; }

//    public String CF_getS_rel_type() { return s_rel_type; }
//    public void CF_setS_rel_type(String s_rel_type) { this.s_rel_type = s_rel_type; }

    public String CF_getS_rel_agree_type() {
        return s_rel_agree_type;
    }
    public void CF_setS_rel_agree_type(String s_rel_agree_type) { this.s_rel_agree_type = s_rel_agree_type; }

    public String CF_getS_pipa_nm_1(){ return s_pipa_nm_1; }
    public void CF_setS_pipa_nm_1(String s_pipa_nm_1){
        this.s_pipa_nm_1 = s_pipa_nm_1;
    }

    public String CF_getS_pipa_nm_2(){ return s_pipa_nm_2; }
    public void CF_setS_pipa_nm_2(String s_pipa_nm_2){
        this.s_pipa_nm_2 = s_pipa_nm_2;
    }

    public String CF_getS_acdp_nm() {
        return s_acdp_nm;
    }
    public void CF_setS_acdp_nm(String s_acdp_nm) {
        this.s_acdp_nm = s_acdp_nm;
    }

    public String CF_getS_acdp_rrno1() {
        return s_acdp_rrno1;
    }
    public void CF_setS_acdp_rrno1(String s_acdp_rrno1) {
        this.s_acdp_rrno1 = s_acdp_rrno1;
    }

    public String CF_getS_acdp_rrno2_enc() {
        return s_acdp_rrno2_enc;
    }
    public void CF_setS_acdp_rrno2_enc(String s_acdp_rrno2_enc) { this.s_acdp_rrno2_enc = s_acdp_rrno2_enc; }


    /**
     * 데이터를 HshMap 에 담아 반환
     * @return HashMap<String,String>
     */
    public HashMap<String,String> CF_getDataMap(){
        HashMap<String,String> tmp_hashMap = super.CF_getDataMap();

        tmp_hashMap.put("s_rctr_csno", s_rctr_csno);
        tmp_hashMap.put("s_rctr_relt_dvsn", s_rctr_relt_dvsn);
        tmp_hashMap.put("s_rctr_type", s_rctr_type);
        tmp_hashMap.put("s_bnfc_nm", s_bnfc_nm);
        tmp_hashMap.put("s_bnfc_csno", s_bnfc_csno);
        //tmp_hashMap.put("s_rel_another", s_rel_another);
        //tmp_hashMap.put("s_rel_type", s_rel_type);
        tmp_hashMap.put("s_rel_agree_type", s_rel_agree_type);
        tmp_hashMap.put("s_pipa_nm_1", s_pipa_nm_1);
        tmp_hashMap.put("s_pipa_nm_2", s_pipa_nm_2);
        tmp_hashMap.put("s_acdp_nm", s_acdp_nm);
        tmp_hashMap.put("s_acdp_rrno1", s_acdp_rrno1);
        tmp_hashMap.put("s_acdp_rrno2_enc", s_acdp_rrno2_enc);

        return tmp_hashMap;
    }

    /**
     * Parcel Write
     */
    @Override
    public void writeToParcel(Parcel p_parcel, int p_flags) {
        super.writeToParcel(p_parcel, p_flags);

        p_parcel.writeString(s_rctr_csno);
        p_parcel.writeString(s_rctr_relt_dvsn);
        p_parcel.writeString(s_rctr_type);
        p_parcel.writeString(s_bnfc_nm);
        p_parcel.writeString(s_bnfc_csno);
        //p_parcel.writeString(s_rel_another);
        //p_parcel.writeString(s_rel_type);
        p_parcel.writeString(s_rel_agree_type);
        p_parcel.writeString(s_pipa_nm_1);
        p_parcel.writeString(s_pipa_nm_2);
        p_parcel.writeString(s_acdp_nm);
        p_parcel.writeString(s_acdp_rrno1);
        p_parcel.writeString(s_acdp_rrno2_enc);
    }

    /**
     * Parcel Read
     * @param p_parcel  Parcel
     */
    @Override
    public void readFromParcel(Parcel p_parcel){
        super.readFromParcel(p_parcel);

        s_rctr_csno = p_parcel.readString();
        s_rctr_relt_dvsn = p_parcel.readString();
        s_rctr_type = p_parcel.readString();
        s_bnfc_nm = p_parcel.readString();
        s_bnfc_csno = p_parcel.readString();
        //s_rel_another = p_parcel.readString();
        //s_rel_type = p_parcel.readString();
        s_rel_agree_type = p_parcel.readString();
        s_pipa_nm_1 = p_parcel.readString();
        s_pipa_nm_2 = p_parcel.readString();
        s_acdp_nm = p_parcel.readString();
        s_acdp_rrno1 = p_parcel.readString();
        s_acdp_rrno2_enc = p_parcel.readString();
    }

    @Override
    public String toString() {
        return super.toString() +
                ", " +
                "Data_IUII90M00{" +
                "s_rctr_csno='" + s_rctr_csno + '\'' +
                ", s_rctr_relt_dvsn='" + s_rctr_relt_dvsn + '\'' +
                ", s_rctr_type='" + s_rctr_type + '\'' +
                ", s_bnfc_nm='" + s_bnfc_nm + '\'' +
                ", s_bnfc_csno='" + s_bnfc_csno + '\'' +
                //", s_rel_another='" + s_rel_another + '\'' +
                //", s_rel_type='" + s_rel_type + '\'' +
                ", s_rel_agree_type='" + s_rel_agree_type + '\'' +
                ", s_pipa_nm_1='" + s_pipa_nm_1 + '\'' +
                ", s_pipa_nm_2='" + s_pipa_nm_2 + '\'' +
                ", s_acdp_nm='" + s_acdp_nm + '\'' +
                ", s_acdp_rrno1='" + s_acdp_rrno1 + '\'' +
                ", s_acdp_rrno2_enc='" + s_acdp_rrno2_enc + '\'' +
                '}' ;
    }

    /**
     * Parcel Creator
     */
    public static Parcelable.Creator<Data_IUII90M00> CREATOR = new Parcelable.Creator<Data_IUII90M00>() {
        public Data_IUII90M00 createFromParcel(Parcel source) {
            return new Data_IUII90M00(source);
        }

        public Data_IUII90M00[] newArray(int size) {
            return new Data_IUII90M00[size];
        }
    };

    @Override
    public void logPrint() {
        super.logPrint();
        LogPrinter.CF_debug("!-----------------------------------------------------------");
        LogPrinter.CF_debug("!-- Data_IUII90M00.logPrint() -- 자녀보험청구 추가 DATA");
        LogPrinter.CF_debug("!-----------------------------------------------------------");
        LogPrinter.CF_debug("!---- s_rctr_csno(청구자 고객번호)                                      : " + s_rctr_csno);
        LogPrinter.CF_debug("!---- s_rctr_relt_dvsn(청구자의 계약관계(30:계약자,42:입원(장해)수익자)) : " + s_rctr_relt_dvsn);
        LogPrinter.CF_debug("!---- s_rctr_type(부모유형(접수자의 자녀와의 관계-1:부,2:모))            : " + s_rctr_type);
        LogPrinter.CF_debug("!---- s_bnfc_nm(수익자명(입원장해수익자)))                               : " + s_bnfc_nm);
        LogPrinter.CF_debug("!---- s_bnfc_csno(수익자고객번호)                                       : " + s_bnfc_csno);
        LogPrinter.CF_debug("!---- s_rel_agree_type(친권자동의유형(1:공동친권,2:사망,3:이혼,4:기타))  : " + s_rel_agree_type);
        LogPrinter.CF_debug("!---- s_pipa_nm_1(친권자명1)                                           : " + s_pipa_nm_1);
        LogPrinter.CF_debug("!---- s_pipa_nm_2(친권자명2)                                           : " + s_pipa_nm_2);
        LogPrinter.CF_debug("!---- s_acdp_nm(피보험자명)                                            : " + s_acdp_nm);
        LogPrinter.CF_debug("!---- s_acdp_rrno1(피보험자 실명번호)                                  : " + s_acdp_rrno1);
        LogPrinter.CF_debug("!---- s_acdp_rrno2_enc(피보험자 실명번호 암호화)                       :" + s_acdp_rrno2_enc);
    }
}
