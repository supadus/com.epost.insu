package com.epost.insu.data;

import android.os.Parcel;
import android.os.Parcelable;

public class Data_IUII90M02_F extends Data_IUII10M02_F {

    // 자녀보험금청구
    private Boolean flagCheck_1_1;                    // 개인(신용)정보의 수집이용에 관한 동의 체크 상태
    private Boolean flagCheck_1_2;                    // 개인(신용)정보의 수집이용에 관한 동의 체크 상태
    private Boolean flagCheck_2_1;                    // 개인(신용)정보의 조회에 관한 사항 체크 상태
    private Boolean flagCheck_2_2;                    // 개인(신용)정보의 조회에 관한 사항 체크 상태
    private Boolean flagCheck_3_1;                    // 개인(신용)정보의 제공에 관한 사항 체크 상태
    private Boolean flagCheck_3_2;                    // 개인(신용)정보의 제공에 관한 사항 체크 상태
    private Boolean flagCheck_4_1;                    // 민감정보 및 고유식별정보의 처리에 관한 사항 체크 상태
    private Boolean flagCheck_4_2;                    // 민감정보 및 고유식별정보의 처리에 관한 사항 체크 상태


    /**
     * 생성자
     */
    public Data_IUII90M02_F(){
        setInit();
    }
    private Data_IUII90M02_F(Parcel p_parcel){
        setInit();
        readFromParcel(p_parcel);
    }

    /**
     * 초기 세팅 함수
     */
    public void setInit(){
        super.setInit();

        // 자녀보험금청구
        flagCheck_1_1 = false;
        flagCheck_1_2 = false;
        flagCheck_2_1 = false;
        flagCheck_2_2 = false;
        flagCheck_3_1 = false;
        flagCheck_3_2 = false;
        flagCheck_4_1 = false;
        flagCheck_4_2 = false;
    }

    /**
     * clear 함수
     */
    public void CF_clear(){
        setInit();
    }

    public Boolean CF_getFlagCheck_1_1() {
        return flagCheck_1_1;
    }
    public void CF_setFlagCheck_1_1(Boolean flagCheck_1_1) {
        this.flagCheck_1_1 = flagCheck_1_1;
    }

    public Boolean CF_getFlagCheck_1_2() {
        return flagCheck_1_2;
    }
    public void CF_setFlagCheck_1_2(Boolean flagCheck_1_2) {
        this.flagCheck_1_2 = flagCheck_1_2;
    }

    public Boolean CF_getFlagCheck_2_1() { return flagCheck_2_1; }
    public void CF_setFlagCheck_2_1(Boolean flagCheck_2_1) {
        this.flagCheck_2_1 = flagCheck_2_1;
    }

    public Boolean CF_getFlagCheck_2_2() { return flagCheck_2_2; }
    public void CF_setFlagCheck_2_2(Boolean flagCheck_2_2) {
        this.flagCheck_2_2 = flagCheck_2_2;
    }

    public Boolean CF_getFlagCheck_3_1() { return flagCheck_3_1; }
    public void CF_setFlagCheck_3_1(Boolean flagCheck_3_1) {
        this.flagCheck_3_1 = flagCheck_3_1;
    }

    public Boolean CF_getFlagCheck_3_2() { return flagCheck_3_2; }
    public void CF_setFlagCheck_3_2(Boolean flagCheck_3_2) {
        this.flagCheck_3_2 = flagCheck_3_2;
    }

    public Boolean CF_getFlagCheck_4_1() { return flagCheck_4_1; }
    public void CF_setFlagCheck_4_1(Boolean flagCheck_4_1) {
        this.flagCheck_4_1 = flagCheck_4_1;
    }

    public Boolean CF_getFlagCheck_4_2() { return flagCheck_4_2; }
    public void CF_setFlagCheck_4_2(Boolean flagCheck_4_2) {
        this.flagCheck_4_2 = flagCheck_4_2;
    }


    /**
     * Parcel Write
     */
    public void writeToParcel(Parcel p_parcel, int p_flags) {

        super.writeToParcel(p_parcel, p_flags);

        // 자녀보험금청구
        p_parcel.writeInt(flagCheck_1_1 ? 1: 0);
        p_parcel.writeInt(flagCheck_1_2 ? 1: 0);
        p_parcel.writeInt(flagCheck_2_1 ? 1: 0);
        p_parcel.writeInt(flagCheck_2_2 ? 1: 0);
        p_parcel.writeInt(flagCheck_3_1 ? 1: 0);
        p_parcel.writeInt(flagCheck_3_2 ? 1: 0);
        p_parcel.writeInt(flagCheck_4_1 ? 1: 0);
        p_parcel.writeInt(flagCheck_4_2 ? 1: 0);

    }

    /**
     * Parcel Read
     * @param p_parcel
     */
    public void readFromParcel(Parcel p_parcel)
    {
        super.readFromParcel(p_parcel);

        // 자녀보험금청구
        flagCheck_1_1 = p_parcel.readInt() == 1;
        flagCheck_1_2 = p_parcel.readInt() == 1;
        flagCheck_2_1 = p_parcel.readInt() == 1;
        flagCheck_2_2 = p_parcel.readInt() == 1;
        flagCheck_3_1 = p_parcel.readInt() == 1;
        flagCheck_3_2 = p_parcel.readInt() == 1;
        flagCheck_4_1 = p_parcel.readInt() == 1;
        flagCheck_4_2 = p_parcel.readInt() == 1;
    }


    /**
     * Parcel Creator
     */
    public static Parcelable.Creator<Data_IUII90M02_F> CREATOR = new Parcelable.Creator<Data_IUII90M02_F>() {

        public Data_IUII90M02_F createFromParcel(Parcel source) {
            return new Data_IUII90M02_F(source);
        }

        public Data_IUII90M02_F[] newArray(int size) {
            return new Data_IUII90M02_F[size];
        }
    };
}
