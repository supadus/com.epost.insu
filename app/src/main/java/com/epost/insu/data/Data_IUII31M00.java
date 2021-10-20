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
public class Data_IUII31M00 implements Parcelable {

    private ArrayList<String> arrBankCode;      // 금융기관 코드
    private ArrayList<String> arrBankName;      // 금융기관명

    /**
     * 생성자
     */
    public Data_IUII31M00(){
        setInit();
    }
    private Data_IUII31M00(Parcel p_parcel){
        setInit();
        readFromParcel(p_parcel);
    }

    /**
     * 초기 세팅 함수
     */
    private void setInit(){
        arrBankCode = new ArrayList<>();
        arrBankName = new ArrayList<>();
    }

    /**
     * clear 함수
     */
    private void clear(){
        arrBankCode.clear();
        arrBankName.clear();
    }

    /**
     * 데이터 세팅 함수
     * @param p_jsonArray
     * @throws JSONException
     */
    public void CF_setData(JSONArray p_jsonArray) throws JSONException {
        clear();
        addData(p_jsonArray);
    }

    /**
     * 데이터 추가 함수
     * @param p_jsonArray
     * @throws JSONException
     */
    private void addData(JSONArray p_jsonArray) throws JSONException {
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
        final String jsonKey_code = "code";
        final String jsonKey_name = "name";

        arrBankCode.add(p_jsonObject.getString(jsonKey_code));
        arrBankName.add(p_jsonObject.getString(jsonKey_name));
    }

    /**
     * 데이터 수 반환
     * @return
     */
    public int CF_getDataCount(){
        return this.arrBankCode.size();
    }

    /**
     * 금융기관 코드 리스트 반환
     * @return
     */
    public ArrayList<String> CF_getBankCodeList(){
        return arrBankCode;
    }

    /**
     * 금융기관 이름 리스트 반환
     * @return
     */
    public ArrayList<String> CF_getBankNameList(){
        return arrBankName;
    }

    public int describeContents() {
        return 0;
    }


    /**
     * Parcel Write
     */
    public void writeToParcel(Parcel p_parcel, int p_flags) {

        p_parcel.writeStringList(arrBankCode);
        p_parcel.writeStringList(arrBankName);
    }

    /**
     * Parcel Read
     * @param p_parcel
     */
    private void readFromParcel(Parcel p_parcel)
    {
        p_parcel.readStringList(arrBankCode);
        p_parcel.readStringList(arrBankName);
    }


    /**
     * Parcel Creator
     */
    public static Parcelable.Creator<Data_IUII31M00> CREATOR = new Parcelable.Creator<Data_IUII31M00>() {

        public Data_IUII31M00 createFromParcel(Parcel source) {
            return new Data_IUII31M00(source);
        }

        public Data_IUII31M00[] newArray(int size) {
            return new Data_IUII31M00[size];
        }
    };
}
