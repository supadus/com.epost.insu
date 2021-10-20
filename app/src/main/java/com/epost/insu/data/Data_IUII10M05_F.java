package com.epost.insu.data;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;

/**
 * @copyright : (주)프로니스정보기술 | http://www.phronis.co.kr
 *
 * @project   : 모바일슈랑스 구축
 * @pakage    : com.epost.insu.data
 * @fileName  : Data_IUII10M05_F.java
 *
 * @Title     : 보험금청구 > 본인청구 > 5단계. 보험청구서작성(청구내용2) (화면 ID : iuii10m05_f) - 상태값 데이터 클래스
 * @author    : 이수행
 * @created   : 2017-09-06
 * @version   : 1.0
 *
 * @note      :
 * ======================================================================
 * 수정 내역
 * NO      날짜          작업자       내용
 * 01      2017-09-06    이수행       최초 등록
 * =======================================================================
 */
public class Data_IUII10M05_F implements Parcelable {

    private Integer checkStateCar;                          // 자동차보험 체크 상태
    private Integer checkStateIndustry;                    // 산업재해보험 체크 상태
    private Integer checkStatePolice;                      // 경찰서신고 체크 상태
    private Integer checkStateJoinOther;                   // 타보험사 가입여부 체크 상태
    private Integer checkStateJoinReal;                    // 실손보험 가입여부 체크 상태
    private String strCarInsure;                            // 자동차보험 보험사명
    private String note;                                     // 기타
    private String strOtherInsure;                         // 타보험사 보험사명
    private String strRealInsure;                          // 실손보험 보험사명
    private ArrayList<String> arrOtherInsureMore;         // 타보험 추가 보험사명
    private ArrayList<String> arrRealInsureMore;          // 실손보험 추가 보험사명


    /**
     * 생성자
     */
    public Data_IUII10M05_F(){
        setInit();
    }
    private Data_IUII10M05_F(Parcel p_parcel){
        setInit();
        readFromParcel(p_parcel);
    }

    /**
     * 초기 세팅 함수
     */
    public void setInit(){

        checkStateCar = 0;
        checkStateIndustry = 0;
        checkStatePolice = 0;
        checkStateJoinOther = 0;
        checkStateJoinReal = 0;
        strCarInsure = "";
        note = "";
        strOtherInsure = "";
        strRealInsure = "";
        arrOtherInsureMore = new ArrayList<>();
        arrRealInsureMore = new ArrayList<>();
    }

    /**
     * clear 함수
     */
    public void CF_clear(){
        checkStateCar = 0;
        checkStateIndustry = 0;
        checkStatePolice = 0;
        checkStateJoinOther = 0;
        checkStateJoinReal = 0;
        strCarInsure = "";
        note = "";
        strOtherInsure = "";
        strRealInsure = "";
        arrOtherInsureMore.clear();
        arrRealInsureMore.clear();
    }

    public Integer CF_getCheckStateCar() {
        return checkStateCar;
    }

    public void CF_setCheckStateCar(Integer checkStateCar) {
        this.checkStateCar = checkStateCar;
    }

    public Integer CF_getCheckStateIndustry() {
        return checkStateIndustry;
    }

    public void CF_setCheckStateIndustry(Integer checkStateIndustry) {
        this.checkStateIndustry = checkStateIndustry;
    }

    public Integer CF_getCheckStatePolice() {
        return checkStatePolice;
    }

    public void CF_setCheckStatePolice(Integer checkStatePolice) {
        this.checkStatePolice = checkStatePolice;
    }

    public Integer CF_getCheckStateJoinOther() {
        return checkStateJoinOther;
    }

    public void CF_setCheckStateJoinOther(Integer checkStateJoinOther) {
        this.checkStateJoinOther = checkStateJoinOther;
    }

    public Integer CF_getCheckStateJoinReal() {
        return checkStateJoinReal;
    }

    public void CF_setCheckStateJoinReal(Integer checkStateJoinReal) {
        this.checkStateJoinReal = checkStateJoinReal;
    }

    public String CF_getStrCarInsure() {
        return strCarInsure;
    }

    public void CF_setStrCarInsure(String strCarInsure) {
        this.strCarInsure = strCarInsure;
    }

    public String CF_getNote() {
        return note;
    }

    public void CF_setNote(String note) {
        this.note = note;
    }

    public String CF_getStrOtherInsure() {
        return strOtherInsure;
    }

    public void CF_setStrOtherInsure(String strOtherInsure) {
        this.strOtherInsure = strOtherInsure;
    }

    public String CF_getStrRealInsure() {
        return strRealInsure;
    }

    public void CF_setStrRealInsure(String strRealInsure) {
        this.strRealInsure = strRealInsure;
    }

    public ArrayList<String> CF_getArrOtherInsureMore() {
        return arrOtherInsureMore;
    }

    public void CF_setArrOtherInsureMore(ArrayList<String> arrOtherInsureMore) {
        this.arrOtherInsureMore = arrOtherInsureMore;
    }

    public ArrayList<String> CF_getArrRealInsureMore() {
        return arrRealInsureMore;
    }

    public void CF_setArrRealInsureMore(ArrayList<String> arrRealInsureMore) {
        this.arrRealInsureMore = arrRealInsureMore;
    }

    public int describeContents() {
        return 0;
    }


    /**
     * Parcel Write
     */
    public void writeToParcel(Parcel p_parcel, int p_flags) {

        p_parcel.writeInt(checkStateCar);
        p_parcel.writeInt(checkStateIndustry);
        p_parcel.writeInt(checkStatePolice);
        p_parcel.writeInt(checkStateJoinOther);
        p_parcel.writeInt(checkStateJoinReal);
        p_parcel.writeString(strCarInsure);
        p_parcel.writeString(note);
        p_parcel.writeString(strOtherInsure);
        p_parcel.writeString(strRealInsure);
        p_parcel.writeStringList(arrOtherInsureMore);
        p_parcel.writeStringList(arrRealInsureMore);

    }

    /**
     * Parcel Read
     * @param p_parcel
     */
    public void readFromParcel(Parcel p_parcel)
    {
        checkStateCar = p_parcel.readInt();
        checkStateIndustry = p_parcel.readInt();
        checkStatePolice = p_parcel.readInt();
        checkStateJoinOther = p_parcel.readInt();
        checkStateJoinReal = p_parcel.readInt();
        strCarInsure = p_parcel.readString();
        note = p_parcel.readString();
        strOtherInsure = p_parcel.readString();
        strRealInsure = p_parcel.readString();
        p_parcel.readStringList(arrOtherInsureMore);
        p_parcel.readStringList(arrRealInsureMore);
    }


    /**
     * Parcel Creator
     */
    public static Parcelable.Creator<Data_IUII10M05_F> CREATOR = new Parcelable.Creator<Data_IUII10M05_F>() {

        public Data_IUII10M05_F createFromParcel(Parcel source) {
            return new Data_IUII10M05_F(source);
        }

        public Data_IUII10M05_F[] newArray(int size) {
            return new Data_IUII10M05_F[size];
        }
    };
}
