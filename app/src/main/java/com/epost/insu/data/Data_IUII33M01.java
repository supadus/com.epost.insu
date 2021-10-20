package com.epost.insu.data;

import android.os.Parcel;
import android.os.Parcelable;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;


/**
 * @copyright : (주)프로니스정보기술 | http://www.phronis.co.kr
 *
 * @project   : 모바일슈랑스 구축
 * @pakage   : com.epost.insu.data
 * @fileName  : Data_IUII31M00.java
 *
 * @Title     : 금융기관 정보 데이터 클래스
 * @author    : 이수행
 * @created   : 2017-08-31
 * @version   : 1.0
 *
 * @note      : <u>금융기관 정보 데이터 클래스</u><br/>
 *               Parcelable 구현으로 기관명/기관코드 관리
 * ======================================================================
 * 수정 내역
 * NO      날짜          작업자       내용
 * 01      2017-08-31    이수행       최초 등록
 * =======================================================================
 */
public class Data_IUII33M01 implements Parcelable {

    private ArrayList<String> arrRemnNo;    // 모집자 번호
    private ArrayList<String> arrName;      // 모집자 명

    /**
     * 생성자
     */
    public Data_IUII33M01(){
        setInit();
    }
    private Data_IUII33M01(Parcel p_parcel){
        setInit();
        readFromParcel(p_parcel);
    }

    /**
     * 초기 세팅 함수
     */
    private void setInit(){
        arrRemnNo = new ArrayList<>();
        arrName   = new ArrayList<>();
    }

    /**
     * clear 함수
     */
    private void clear(){
        arrRemnNo.clear();
        arrName.clear();
    }

    /**
     * 데이터 세팅 함수
     * @param p_jsonArray
     * @throws JSONException
     */
    public void CF_setData(JSONArray p_jsonArray) throws JSONException {
        clear();
        CF_addData(p_jsonArray);
    }

    /**
     * 데이터 추가 함수 (더보기 : 리스트 하단 데이터 추가)
     * @param p_jsonArray
     * @throws JSONException
     */
    public void CF_addData(JSONArray p_jsonArray) throws JSONException {
        for(int i = 0 ; i < p_jsonArray.length(); i++){
            addData(p_jsonArray.getJSONObject(i));
        }
    }

    /**
     * 데이터 추가 함수
     * @param p_jsonObject
     * @throws JSONException
     */
    private void addData(JSONObject p_jsonObject) throws JSONException {
        final String jsonKey_remnNo = "remnNo";
        final String jsonKey_name   = "name";

        arrRemnNo.add(p_jsonObject.getString(jsonKey_remnNo));
        arrName.add(p_jsonObject.getString(jsonKey_name));
    }

    /**
     * 데이터 수 반환
     * @return
     */
    public int CF_getDataCount(){
        return this.arrRemnNo.size();
    }

    /**
     * 추천국 코드 리스트 반환
     * @return
     */
    public ArrayList<String> CF_getDepartmentRemnNoList(){
        return arrRemnNo;
    }

    /**
     * 추천국 이름 리스트 반환
     * @return
     */
    public ArrayList<String> CF_getDepartmentNameList(){
        return arrName;
    }


    public int describeContents() {
        return 0;
    }


    /**
     * Parcel Write
     */
    public void writeToParcel(Parcel p_parcel, int p_flags) {
        p_parcel.writeStringList(arrRemnNo);
        p_parcel.writeStringList(arrName);
    }

    /**
     * Parcel Read
     * @param p_parcel
     */
    private void readFromParcel(Parcel p_parcel) {
        p_parcel.readStringList(arrRemnNo);
        p_parcel.readStringList(arrName);
    }


    /**
     * Parcel Creator
     */
    public static Creator<Data_IUII33M01> CREATOR = new Creator<Data_IUII33M01>() {

        public Data_IUII33M01 createFromParcel(Parcel source) {
            return new Data_IUII33M01(source);
        }

        public Data_IUII33M01[] newArray(int size) {
            return new Data_IUII33M01[size];
        }
    };
}
