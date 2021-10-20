package com.epost.insu.data;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;

/**
 * @copyright : (주)프로니스정보기술 | http://www.phronis.co.kr
 *
 * @project   : 모바일슈랑스 구축
 * @pakage    : com.epost.insu.data
 * @fileName  : Data_IUII10M04_F.java
 *
 * @Title     : 보험금청구 > 본인청구 > 4단계. 보험청구서작성(청구내용1) (화면 ID : iuii10m04_f) - 상태값 데이터 클래스
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
public class Data_IUII10M04_F implements Parcelable {

    private Integer category;
    private Integer reason;
    private ArrayList<Boolean> arrType_1, arrType_2, arrType_3;
    private String diseaseName, diseaseCode;
    private String accidentYear, accidentMonth, accidentDayOfMonth, accidentHour, accidentMinute, accidentPlace, accidentNote;

    public Data_IUII10M04_F(){
        setInit();
    }

    private Data_IUII10M04_F(Parcel p_parcel){
        setInit();
        readFromParcel(p_parcel);
    }

    /**
     * 초기 세팅 함수
     */
    public void setInit(){
        category = -1;
        reason = -1;
        arrType_1 = new ArrayList<>();
        arrType_2 = new ArrayList<>();
        arrType_3 = new ArrayList<>();
        diseaseName = "";
        diseaseCode = "";
        accidentYear = "";
        accidentMonth = "";
        accidentDayOfMonth = "";
        accidentHour = "";
        accidentMinute = "";
        accidentPlace = "";
        accidentNote = "";

    }

    /**
     * clear 함수
     */
    public void CF_clear(){
        category = -1;
        reason = -1;
        arrType_1.clear();
        arrType_2.clear();
        arrType_3.clear();
        diseaseName = "";
        diseaseCode = "";
        accidentYear = "";
        accidentMonth = "";
        accidentDayOfMonth = "";
        accidentHour = "";
        accidentMinute = "";
        accidentPlace = "";
        accidentNote = "";
    }




    public int describeContents() {
        return 0;
    }


    public Integer CF_getCategory() {
        return category;
    }

    public void CF_setCategory(Integer category) {
        this.category = category;
    }

    public Integer CF_getReason() {
        return reason;
    }

    public void CF_setReason(Integer reason) {
        this.reason = reason;
    }

    public ArrayList<Boolean> CF_getArrType_1() {
        return arrType_1;
    }

    public void CF_setArrType_1(ArrayList<Boolean> arrType_1) {
        this.arrType_1 = arrType_1;
    }

    public ArrayList<Boolean> CF_getArrType_2() {
        return arrType_2;
    }

    public void CF_setArrType_2(ArrayList<Boolean> arrType_2) {
        this.arrType_2 = arrType_2;
    }

    public ArrayList<Boolean> CF_getArrType_3() {
        return arrType_3;
    }

    public void CF_setArrType_3(ArrayList<Boolean> arrType_3) {
        this.arrType_3 = arrType_3;
    }

    public String CF_getDiseaseName() {
        return diseaseName;
    }

    public void CF_setDiseaseName(String diseaseName) {
        this.diseaseName = diseaseName;
    }

    public String CF_getDiseaseCode() {
        return diseaseCode;
    }

    public void CF_setDiseaseCode(String diseaseCode) {
        this.diseaseCode = diseaseCode;
    }

    public String CF_getAccidentYear() {
        return accidentYear;
    }

    public void CF_setAccidentYear(String accidentYear) {
        this.accidentYear = accidentYear;
    }

    public String CF_getAccidentMonth() {
        return accidentMonth;
    }

    public void CF_setAccidentMonth(String accidentMonth) {
        this.accidentMonth = accidentMonth;
    }

    public String CF_getAccidentDayOfMonth() {
        return accidentDayOfMonth;
    }

    public void CF_setAccidentDayOfMonth(String accidentDayOfMonth) {
        this.accidentDayOfMonth = accidentDayOfMonth;
    }

    public String CF_getAccidentHour() {
        return accidentHour;
    }

    public void CF_setAccidentHour(String accidentHour) {
        this.accidentHour = accidentHour;
    }

    public String CF_getAccidentMinute() {
        return accidentMinute;
    }

    public void CF_setAccidentMinute(String accidentMinute) {
        this.accidentMinute = accidentMinute;
    }

    public String CF_getAccidentPlace() {
        return accidentPlace;
    }

    public void CF_setAccidentPlace(String accidentPlace) {
        this.accidentPlace = accidentPlace;
    }

    public String CF_getAccidentNote() {
        return accidentNote;
    }

    public void CF_setAccidentNote(String accidentNote) {
        this.accidentNote = accidentNote;
    }

    /**
     * Parcel Write
     */
    public void writeToParcel(Parcel p_parcel, int p_flags) {

        p_parcel.writeInt(category);
        p_parcel.writeInt(reason);
        p_parcel.writeString(diseaseName);
        p_parcel.writeString(diseaseCode);
        p_parcel.writeString(accidentYear);
        p_parcel.writeString(accidentMonth);
        p_parcel.writeString(accidentDayOfMonth);
        p_parcel.writeString(accidentHour);
        p_parcel.writeString(accidentMinute);
        p_parcel.writeString(accidentPlace);
        p_parcel.writeString(accidentNote);
        p_parcel.writeList(arrType_1);
        p_parcel.writeList(arrType_2);
        p_parcel.writeList(arrType_3);
    }

    /**
     * Parcel Read
     * @param p_parcel
     */
    public void readFromParcel(Parcel p_parcel)
    {
        category = p_parcel.readInt();
        reason = p_parcel.readInt();
        diseaseName = p_parcel.readString();
        diseaseCode = p_parcel.readString();
        accidentYear = p_parcel.readString();
        accidentMonth = p_parcel.readString();
        accidentDayOfMonth = p_parcel.readString();
        accidentHour = p_parcel.readString();
        accidentMinute = p_parcel.readString();
        accidentPlace = p_parcel.readString();
        accidentNote = p_parcel.readString();
        p_parcel.readList(arrType_1, Boolean.class.getClassLoader());
        p_parcel.readList(arrType_2, Boolean.class.getClassLoader());
        p_parcel.readList(arrType_3, Boolean.class.getClassLoader());
    }


    /**
     * Parcel Creator
     */
    public static Parcelable.Creator<Data_IUII10M04_F> CREATOR = new Parcelable.Creator<Data_IUII10M04_F>() {

        public Data_IUII10M04_F createFromParcel(Parcel source) {
            return new Data_IUII10M04_F(source);
        }

        public Data_IUII10M04_F[] newArray(int size) {
            return new Data_IUII10M04_F[size];
        }
    };
}
