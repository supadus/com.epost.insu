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
 * @fileName  : Data_IUII50M00_F_P.java
 *
 * @Title     : 보험금청구 > 보험금청구 > 보험청구서작성(개인정보) (화면 ID : IUII12M00_F3) 데이터
 * @author    : 이수행
 * @created   : 2017-12-06
 * @version   : 1.0
 *
 * @note      : <u>보험금청구 > 보험금청구 > 보험청구서작성(개인정보) (화면 ID : IUII12M00_F3) 데이터</u><br/>
 * ======================================================================
 * 수정 내역
 * NO      날짜          작업자       내용
 * 01      2017-12-06    이수행       최초 등록
 * =======================================================================
 */
public class Data_IUII50M00_F_P implements Parcelable {

    private ArrayList<Data_IUII50M00_F> arrData;

    /**
     * 생성자
     */
    public Data_IUII50M00_F_P(){
        setInit();
    }
    private Data_IUII50M00_F_P(Parcel p_parcel){
        setInit();
        readFromParcel(p_parcel);
    }

    /**
     * 초기 세팅
     */
    private void setInit(){
        arrData = new ArrayList<>();
    }

    /**
     * clear 함수
     */
    private void clear(){
        arrData.clear();
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
    public void addData(JSONObject p_jsonObject) throws JSONException {
        Data_IUII50M00_F tmp_data = new Data_IUII50M00_F();
        tmp_data.CF_setData(p_jsonObject);
        arrData.add(tmp_data);
    }

    /**
     * 구비서류보완유무 값 false 업데이트
     * @param p_reqId
     */
    public void CF_updateFlagNeedMod(String p_reqId){
        for(Data_IUII50M00_F tmp_data : arrData){
            if(tmp_data.CF_getReqId().equals(p_reqId)){
                tmp_data.CF_setFlagNeedMod(false);
                break;
            }
        }
    }

    /**
     * 데이터 수 반환
     * @return
     */
    public int CF_getDataCount(){
        return this.arrData.size();
    }

    /**
     * 데이터 반환
     * @return
     */
    public ArrayList<Data_IUII50M00_F> CF_getDataList(){
        return arrData;
    }

    public int describeContents() {
        return 0;
    }

    /**
     * Parcel Write
     */
    public void writeToParcel(Parcel p_parcel, int p_flags) {

        p_parcel.writeTypedList(arrData);
    }

    /**
     * Parcel Read
     * @param p_parcel
     */
    private void readFromParcel(Parcel p_parcel)
    {
        p_parcel.readTypedList(arrData, Data_IUII50M00_F.CREATOR);
    }


    /**
     * Parcel Creator
     */
    public static Parcelable.Creator<Data_IUII50M00_F_P> CREATOR = new Parcelable.Creator<Data_IUII50M00_F_P>() {

        public Data_IUII50M00_F_P createFromParcel(Parcel source) {
            return new Data_IUII50M00_F_P(source);
        }

        public Data_IUII50M00_F_P[] newArray(int size) {
            return new Data_IUII50M00_F_P[size];
        }
    };
}
