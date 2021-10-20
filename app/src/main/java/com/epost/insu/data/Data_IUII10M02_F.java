package com.epost.insu.data;

import android.os.Parcel;
import android.os.Parcelable;


/**
 * @copyright : (주)프로니스정보기술 | http://www.phronis.co.kr
 *
 * @project   : 모바일슈랑스 구축
 * @pakage    : com.epost.insu.data
 * @fileName  : Data_IUII10M02_F.java
 *
 * @Title     : 보험금청구 > 본인청구 > 2단계. 개인정보처리동의 (화면 ID : iuii10m02_f) - 상태값 데이터 클래스
 * @author    : 이수행
 * @created   : 2017-12-07
 * @version   : 1.0
 *
 * @note      :
 * ======================================================================
 * 수정 내역
 * NO      날짜          작업자       내용
 * 01      2017-12-07    이수행       최초 등록
 * 02      2018-11-13    양지훈       자녀보험금관련 파라미터 추가 (IUII90M02_F 사용)
 * =======================================================================
 */
public class Data_IUII10M02_F implements Parcelable {

    private Boolean flagCheckAll;                   // 약관 전체동의 체크 상태
    private Boolean flagCheck_1;                    // 개인(신용)정보의 수집이용에 관한 동의 체크 상태
    private Boolean flagCheck_2;                    // 개인(신용)정보의 조회에 관한 사항 체크 상태
    private Boolean flagCheck_3;                    // 개인(신용)정보의 제공에 관한 사항 체크 상태
    private Boolean flagCheck_4;                    // 민감정보 및 고유식별정보의 처리에 관한 사항 체크 상태

    /**
     * 생성자
     */
    public Data_IUII10M02_F(){
        setInit();
    }
    private Data_IUII10M02_F(Parcel p_parcel){
        setInit();
        readFromParcel(p_parcel);
    }

    /**
     * 초기 세팅 함수
     */
    public void setInit(){
        flagCheckAll = false;
        flagCheck_1 = false;
        flagCheck_2 = false;
        flagCheck_3 = false;
        flagCheck_4 = false;
    }

    /**
     * clear 함수
     */
    public void CF_clear(){
        setInit();
    }

    public Boolean CF_getFlagCheckAll() {
        return flagCheckAll;
    }

    public void CF_setFlagCheckAll(Boolean flagCheckAll) {
        this.flagCheckAll = flagCheckAll;
    }

    public Boolean CF_getFlagCheck_1() {
        return flagCheck_1;
    }

    public void CF_setFlagCheck_1(Boolean flagCheck_1) {
        this.flagCheck_1 = flagCheck_1;
    }

    public Boolean CF_getFlagCheck_2() {
        return flagCheck_2;
    }

    public void CF_setFlagCheck_2(Boolean flagCheck_2) {
        this.flagCheck_2 = flagCheck_2;
    }

    public Boolean CF_getFlagCheck_3() {
        return flagCheck_3;
    }

    public void CF_setFlagCheck_3(Boolean flagCheck_3) {
        this.flagCheck_3 = flagCheck_3;
    }

    public Boolean CF_getFlagCheck_4() {
        return flagCheck_4;
    }

    public void CF_setFlagCheck_4(Boolean flagCheck_4) {
        this.flagCheck_4 = flagCheck_4;
    }

    public int describeContents() {
        return 0;
    }


    /**
     * Parcel Write
     */
    public void writeToParcel(Parcel p_parcel, int p_flags) {
        p_parcel.writeInt(flagCheckAll ? 1: 0);
        p_parcel.writeInt(flagCheck_1 ? 1: 0);
        p_parcel.writeInt(flagCheck_2 ? 1: 0);
        p_parcel.writeInt(flagCheck_3 ? 1: 0);
        p_parcel.writeInt(flagCheck_4 ? 1: 0);
    }


    /**
     * Parcel Read
     * @param p_parcel
     */
    public void readFromParcel(Parcel p_parcel)
    {
        flagCheckAll = p_parcel.readInt() == 1;
        flagCheck_1 = p_parcel.readInt() == 1;
        flagCheck_2 = p_parcel.readInt() == 1;
        flagCheck_3 = p_parcel.readInt() == 1;
        flagCheck_4 = p_parcel.readInt() == 1;
    }


    /**
     * Parcel Creator
     */
    public static Parcelable.Creator<Data_IUII10M02_F> CREATOR = new Parcelable.Creator<Data_IUII10M02_F>() {

        public Data_IUII10M02_F createFromParcel(Parcel source) {
            return new Data_IUII10M02_F(source);
        }

        public Data_IUII10M02_F[] newArray(int size) {
            return new Data_IUII10M02_F[size];
        }
    };
}
