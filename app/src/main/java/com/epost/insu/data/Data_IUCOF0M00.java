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
 * @fileName  : Data_IUII10M04_F.java
 *
 * @Title     : 주소 목록 데이터 클래스
 * @author    : 이수행
 * @created   : 2017-08-11
 * @version   : 1.0
 *
 * @note      : <u>IUCOF0M01(주소검색) 화면에서 사용</u><br/>
 * ======================================================================
 * 수정 내역
 * NO      날짜          작업자       내용
 * 01      2017-08-11    이수행       최초 등록
 * =======================================================================
 */
public class Data_IUCOF0M00 implements Parcelable {

    private ArrayList<String> arrZipNo;                     // 우편번호
    private ArrayList<String> arrTownName;                  // 동
    private ArrayList<String> arrAddrRoad;                  // 도로명 주소
    private ArrayList<String> arrAddrBunji;                 // 동 주소

    public Data_IUCOF0M00(){
        setInit();
    }

    private Data_IUCOF0M00(Parcel p_parcel){
        setInit();
        readFromParcel(p_parcel);
    }

    /**
     * 초기 세팅 함수
     */
    private void setInit(){
        arrZipNo = new ArrayList<>();
        arrTownName = new ArrayList<>();
        arrAddrRoad = new ArrayList<>();
        arrAddrBunji = new ArrayList<>();
    }

    /**
     * clear 함수
     */
    public void CF_clear(){
        arrZipNo.clear();
        arrTownName.clear();
        arrAddrRoad.clear();
        arrAddrBunji.clear();
    }

    /**
     * 데이터 세팅 함수
     * @param p_jsonArray
     * @throws JSONException
     */
    public void CF_setData(JSONArray p_jsonArray) throws JSONException {
        CF_clear();
        CF_addData(p_jsonArray);
    }

    /**
     * 데이터 추가 함수
     * @param p_jsonArray
     * @throws JSONException
     */
    public void CF_addData(JSONArray p_jsonArray) throws JSONException {
        for(int i = 0 ; i < p_jsonArray.length(); i++){
            CF_addData(p_jsonArray.getJSONObject(i));
        }
    }

    /**
     * 데이터 추가 함수
     * @param p_jsonObject
     * @throws JSONException
     */
    private void CF_addData(JSONObject p_jsonObject) throws JSONException {
        final String jsonKey_zipNo = "zipNo";
        final String jsonKey_townName = "townName";
        final String jsonKey_addrRoad = "addrRoad";
        final String jsonKey_addrBunji = "addrBunji";

        arrZipNo.add(p_jsonObject.getString(jsonKey_zipNo));
        arrTownName.add(p_jsonObject.getString(jsonKey_townName));
        arrAddrRoad.add(p_jsonObject.getString(jsonKey_addrRoad));
        arrAddrBunji.add(p_jsonObject.getString(jsonKey_addrBunji));
    }

    /**
     * 데이터 수 반환
     * @return
     */
    public int CF_getDataCount(){
        return this.arrZipNo.size();
    }

    public ArrayList<String> CF_getZipNo(){
        return arrZipNo;
    }

    public ArrayList<String> CF_getAddrBunji(){
        return arrAddrBunji;
    }

    public ArrayList<String> CF_getAddrRoad(){
        return arrAddrRoad;
    }

    public ArrayList<String> CF_getTownName(){
        return arrTownName;
    }

    public int describeContents() {
        return 0;
    }


    /**
     * Parcel Write
     */
    public void writeToParcel(Parcel p_parcel, int p_flags) {

        p_parcel.writeStringList(arrZipNo);
        p_parcel.writeStringList(arrTownName);
        p_parcel.writeStringList(arrAddrRoad);
        p_parcel.writeStringList(arrAddrBunji);
    }

    /**
     * Parcel Read
     * @param p_parcel
     */
    private void readFromParcel(Parcel p_parcel)
    {
        p_parcel.readStringList(arrZipNo);
        p_parcel.readStringList(arrTownName);
        p_parcel.readStringList(arrAddrRoad);
        p_parcel.readStringList(arrAddrBunji);
    }


    /**
     * Parcel Creator
     */
    public static Parcelable.Creator<Data_IUCOF0M00> CREATOR = new Parcelable.Creator<Data_IUCOF0M00>() {

        public Data_IUCOF0M00 createFromParcel(Parcel source) {
            return new Data_IUCOF0M00(source);
        }

        public Data_IUCOF0M00[] newArray(int size) {
            return new Data_IUCOF0M00[size];
        }
    };
}
