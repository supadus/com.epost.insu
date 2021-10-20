package com.epost.insu.data;

import android.os.Parcel;
import android.os.Parcelable;

public class Data_IUII90M03_F extends Data_IUII10M03_F {
/*

    private String s_rctr_csno;         // 접수자고객번호
    private String s_rctr_relt_dvsn;    // 접수자의 계약관계 (계약자/수익자) (30:계약자, 31:주피보험자, 32:종피보험자1, 41:만기(생존)수익자, 42:입원(장해)수익자, 43:사망수익자)
    private String s_rctr_type;         // 부모유형 (접수자의 자녀와의 관계-1:부,2:모)

    private String s_rel_another;       // 접수자 외의 계약관계 (계약자/수익자)
    private String s_rel_type;          // 부모유형 (접수자 외 부모의 자녀와의 관계-1:부,2:모)

    private String s_rel_agree_type;    // 친권자동의유형 (1:공동친권, 2:사망, 3:이혼, 4:기타)

    private String s_acdp_csno;         // 사고자(피보험자) 고객번호
    private String s_acdp_nm;           // 피보험자명
    private String s_acdp_rrno1;        // 피보험자 실명번호 암호화
    private String s_acdp_rrno2_enc;    // 피보험자 실명번호 암호화
 */

    // 자녀보험금청구
    private Integer relationType;       // 청구자 계약관계 (0:계약자, 1:입원(장해)수익자)
    private Integer parentType;         // 자녀와의관계 (0:부, 1:모)
    private String beneficiaryName;     // 수익자명

    private Integer agreeType;          // 공동친권여부 (0:공동, 1:단독)
    private Integer agreeTypeReason;    // 단독친권사유 (0:사망, 1:이혼, 2:기타)

    private String anotherParentName;   // 청구자 외의 부모의 이름
    //private Integer anotherParentType;   // 청구자 외의 부모유형 (0:부, 1:모)

    private String childName;           // 자녀이름
    private String childRrno1;          // 자녀주민번호1
    private String childRrno2_enc;          // 자녀주민번호1

    // 사고자 고객번호
    // 청구자 고객번호


    /**
     * 생성자
     */
    public Data_IUII90M03_F(){
        setInit();
    }
    private Data_IUII90M03_F(Parcel p_parcel){
        setInit();
        readFromParcel(p_parcel);
    }

    /**
     * 초기 세팅 함수
     */
    public void setInit(){
        super.setInit();

        relationType = -1;
        parentType = -1;
        beneficiaryName = "";
        agreeType = -1;
        agreeTypeReason = -1;
        anotherParentName = "";
        //anotherParentType = -1;
        childName = "";
        childRrno1 = "";
        childRrno2_enc = "";
    }

    public void CF_clear(){
        setInit();
    }

    public Integer CF_getRelationType() {
        return relationType;
    }
    public void CF_setRelationType(Integer relationType) {
        this.relationType = relationType;
    }

    public Integer CF_getParentType() {
        return parentType;
    }
    public void CF_setParentType(Integer parentType) {
        this.parentType = parentType;
    }

    public String CF_getBeneficiaryName(){
        return beneficiaryName;
    }
    public void CF_setBeneficiaryName(String beneficiaryName){ this.beneficiaryName = beneficiaryName; }

    public Integer CF_getAgreeType() {
        return agreeType;
    }
    public void CF_setAgreeType(Integer agreeType) {
        this.agreeType = agreeType;
    }

    public Integer CF_getAgreeTypeReason() {
        return agreeTypeReason;
    }
    public void CF_setAgreeTypeReason(Integer agreeTypeReason) { this.agreeTypeReason = agreeTypeReason; }

    public String CF_getAnotherParentName() {
        return anotherParentName;
    }
    public void CF_setAnotherParentName(String anotherParentName) { this.anotherParentName = anotherParentName; }

//    public Integer CF_getAnotherParentType() { return anotherParentType; }
//    public void CF_setAnotherParentType(Integer anotherParentType) { this.anotherParentType = anotherParentType; }

    public String CF_getChildName() {
        return childName;
    }
    public void CF_setChildName(String childName) {
        this.childName = childName;
    }

    public String CF_getChildRrno1() {
        return childRrno1;
    }
    public void CF_setChildRrno1(String childRrno1) {
        this.childRrno1 = childRrno1;
    }

    public String CF_getChildRrno2_enc() {
        return childRrno2_enc;
    }
    public void CF_setChildRrno2_enc(String childRrno2_enc) { this.childRrno2_enc = childRrno2_enc; }

    /**
     * Parcel Write
     */
    public void writeToParcel(Parcel p_parcel, int p_flags) {
        super.writeToParcel(p_parcel, p_flags);

        p_parcel.writeInt(relationType);
        p_parcel.writeInt(parentType);
        p_parcel.writeString(beneficiaryName);
        p_parcel.writeInt(agreeType);
        p_parcel.writeInt(agreeTypeReason);
        p_parcel.writeString(anotherParentName);
        //p_parcel.writeInt(anotherParentType);
        p_parcel.writeString(childName);
        p_parcel.writeString(childRrno1);
        p_parcel.writeString(childRrno2_enc);
    }

    /**
     * Parcel Read
     * @param p_parcel
     */
    public void readFromParcel(Parcel p_parcel)
    {
        super.readFromParcel(p_parcel);

        relationType = p_parcel.readInt();
        parentType = p_parcel.readInt();
        beneficiaryName = p_parcel.readString();
        agreeType = p_parcel.readInt();
        agreeTypeReason = p_parcel.readInt();
        anotherParentName = p_parcel.readString();
        //anotherParentType = p_parcel.readInt();
        childName = p_parcel.readString();
        childRrno1 = p_parcel.readString();
        childRrno2_enc = p_parcel.readString();
    }

    /**
     * Parcel Creator
     */
    public static Parcelable.Creator<Data_IUII90M03_F> CREATOR = new Parcelable.Creator<Data_IUII90M03_F>() {

        public Data_IUII90M03_F createFromParcel(Parcel source) {
            return new Data_IUII90M03_F(source);
        }

        public Data_IUII90M03_F[] newArray(int size) {
            return new Data_IUII90M03_F[size];
        }
    };
}
