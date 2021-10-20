package com.epost.insu.data;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;

/**
 * @copyright : (주)프로니스정보기술 | http://www.phronis.co.kr
 *
 * @project   : 모바일슈랑스 구축
 * @pakage   : com.epost.insu.data
 * @fileName  : Data_IUII10M06_F.java
 *
 * @Title     : 보험금청구 > 본인청구 > 6단계. 구비서류 첨부 (화면 ID : iuii10m06_f) - 상태값 데이터 클래스
 * @author    : 이수행
 * @created   : 2017-09-07
 * @version   : 1.0
 *
 * @note      :
 * ======================================================================
 * 수정 내역
 * NO      날짜          작업자       내용
 * 01      2017-09-07    이수행       최초 등록
 * =======================================================================
 */
public class Data_IUII10M06_F implements Parcelable {

    private ArrayList<String> arrPhotoPath_1;      // 신분증 사진 경로
    private ArrayList<String> arrPhotoPath_2;      // 구비서류 사진 경로

    /**
     * 생성자
     */
    public Data_IUII10M06_F(){
        setInit();
    }
    private Data_IUII10M06_F(Parcel p_parcel){
        setInit();
        readFromParcel(p_parcel);
    }

    /**
     * 초기 세팅 함수
     */
    public void setInit(){
        arrPhotoPath_1 = new ArrayList<>();
        arrPhotoPath_2 = new ArrayList<>();
    }

    /**
     * clear 함수
     */
    public void CF_clear(){
        arrPhotoPath_1.clear();
        arrPhotoPath_2.clear();
    }

    /**
     * 포토 Path 세팅
     * @param p_arrPath_1
     * @param p_arrPath_2
     */
    public void CF_setPhotoPath(ArrayList<String> p_arrPath_1, ArrayList<String> p_arrPath_2){

        arrPhotoPath_1.clear();
        arrPhotoPath_2.clear();
        arrPhotoPath_1.addAll(p_arrPath_1);
        arrPhotoPath_2.addAll(p_arrPath_2);
    }

    /**
     * 신분증 사진경로 반환 함수
     * @return
     */
    public ArrayList<String> CF_getPhotoPath_1(){
        return arrPhotoPath_1;
    }

    /**
     * 구비서류 사진경로 반환 함수
     * @return
     */
    public ArrayList<String> CF_getPhotoPath_2(){
        return arrPhotoPath_2;
    }

    public int describeContents() {
        return 0;
    }


    /**
     * Parcel Write
     */
    public void writeToParcel(Parcel p_parcel, int p_flags) {

        p_parcel.writeStringList(arrPhotoPath_1);
        p_parcel.writeStringList(arrPhotoPath_2);
    }

    /**
     * Parcel Read
     * @param p_parcel
     */
    public void readFromParcel(Parcel p_parcel)
    {
        p_parcel.readStringList(arrPhotoPath_1);
        p_parcel.readStringList(arrPhotoPath_2);
    }


    /**
     * Parcel Creator
     */
    public static Parcelable.Creator<Data_IUII10M06_F> CREATOR = new Parcelable.Creator<Data_IUII10M06_F>() {

        public Data_IUII10M06_F createFromParcel(Parcel source) {
            return new Data_IUII10M06_F(source);
        }

        public Data_IUII10M06_F[] newArray(int size) {
            return new Data_IUII10M06_F[size];
        }
    };
}
