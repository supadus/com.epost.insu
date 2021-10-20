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
 * @Title     : 추천국 정보 데이터 클래스
 * @author    : 노지민
 * @created   : 2019-05-XX
 * @version   : 1.0
 *
 * @note      : <u>추천국 정보 데이터 클래스</u><br/>
 *               Parcelable 구현으로 기관명/기관코드 관리
 * ======================================================================
 * 수정 내역
 * NO      날짜          작업자       내용
 * 01      2019-05-XX    노지민       최초 등록
 * =======================================================================
 */
public class Data_IUII33M00 implements Parcelable {

    private ArrayList<String> arrDepartmentCode;      // 추천국 코드
    private ArrayList<String> arrDepartmentName;      // 추천국명
    private ArrayList<String> arrDepartmentAddr;      // 주소

    /**
     * 생성자
     */
    public Data_IUII33M00(){
        setInit();
    }

    private Data_IUII33M00(Parcel p_parcel){
        setInit();
        readFromParcel(p_parcel);
    }

    /**
     * 초기 세팅 함수
     */
    private void setInit(){
        arrDepartmentCode = new ArrayList<>();
        arrDepartmentName = new ArrayList<>();
        arrDepartmentAddr = new ArrayList<>();
    }

    /**
     * clear 함수
     */
    private void clear(){
        arrDepartmentCode.clear();
        arrDepartmentName.clear();
        arrDepartmentAddr.clear();
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
        final String jsonKey_addr = "addr";

        arrDepartmentCode.add(p_jsonObject.getString(jsonKey_code));
        arrDepartmentName.add(p_jsonObject.getString(jsonKey_name));
        arrDepartmentAddr.add(p_jsonObject.getString(jsonKey_addr));
    }

    /**
     * 데이터 수 반환
     * @return
     */
    public int CF_getDataCount(){
        return this.arrDepartmentCode.size();
    }

    /**
     * 추천국 코드 리스트 반환
     * @return
     */
    public ArrayList<String> CF_getDepartmentCodeList(){
        return arrDepartmentCode;
    }

    /**
     * 추천국 이름 리스트 반환
     * @return
     */
    public ArrayList<String> CF_getDepartmentNameList(){
        return arrDepartmentName;
    }

    /**
     * 추천국 주소 리스트 반환
     * @return
     */
    public ArrayList<String> CF_getDepartmentAddrList(){
        return arrDepartmentAddr;
    }

    public int describeContents() {
        return 0;
    }


    /**
     * Parcel Write
     */
    public void writeToParcel(Parcel p_parcel, int p_flags) {
        p_parcel.writeStringList(arrDepartmentCode);
        p_parcel.writeStringList(arrDepartmentName);
        p_parcel.writeStringList(arrDepartmentAddr);
    }

    /**
     * Parcel Read
     * @param p_parcel
     */
    private void readFromParcel(Parcel p_parcel) {
        p_parcel.readStringList(arrDepartmentCode);
        p_parcel.readStringList(arrDepartmentName);
        p_parcel.readStringList(arrDepartmentAddr);
    }


    /**
     * Parcel Creator
     */
    public static Creator<Data_IUII33M00> CREATOR = new Creator<Data_IUII33M00>() {

        public Data_IUII33M00 createFromParcel(Parcel source) {
            return new Data_IUII33M00(source);
        }

        public Data_IUII33M00[] newArray(int size) {
            return new Data_IUII33M00[size];
        }
    };
}
