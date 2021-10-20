package com.epost.insu.data;

import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;

import com.epost.insu.EnvConfig;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * @copyright : (주)프로니스정보기술 | http://www.phronis.co.kr
 *
 * @project   : 모바일슈랑스 구축
 * @pakage   : com.epost.insu.data
 * @fileName  : Data_IUII50M00_F.java
 *
 * @Title     : 보험금청구 > 보험금청구 > 보험금청구조회 (화면 ID : IUII50M00) -  #25 목록 데이터
 * @author    : 이수행
 * @created   : 2017-12-06
 * @version   : 1.0
 *
 * @note      : <u>보험금청구 > 보험금청구 > 보험금청구조회 (화면 ID : IUII50M00) -  #25 목록 데이터</u><br/>
 * ======================================================================
 * 수정 내역
 * NO      날짜          작업자       내용
 * 01      2017-12-06    이수행       최초 등록
 * =======================================================================
 */
public class Data_IUII50M00_F implements Parcelable{

    private String reqId;                           // 접수 아이디
    private String statusName_1;                   // 접수 상태 값 1
    private String statusName_2;                   // 접수 상태 값 2
    private String statusCode;                     // 접수 상태 코드
    private String reqDate;                         // 접수신청일
    private String typeCode;                        // 청구사유 코드
    private String typeName;                        // 청구사유 명
    private String checkPerson;                     // 접수자
    private String payDate;                         // 처리일자
    private boolean flagNeedMod;                   // 서류보완 필요 여부 flag

    public boolean CF_isFlagNeedMod() {
        return flagNeedMod;
    }

    public void CF_setFlagNeedMod(boolean flagNeedMod) {
        this.flagNeedMod = flagNeedMod;
    }

    public String CF_getStatusCode() {
        return statusCode;
    }

    public String CF_getReqId() {
        return reqId;
    }

    public String CF_getStatusName_1() {
        return statusName_1;
    }

    public String CF_getStatusName_2() {
        return statusName_2;
    }

    public String CF_getReqDate() {
        return reqDate;
    }

    public String CF_getCheckPerson() {
        return checkPerson;
    }

    public String CF_getPayDate() {
        return payDate;
    }

    public String CF_getTypeCode() {
        return typeCode;
    }

    public String CF_getTypeName() {
        return typeName;
    }

    public Data_IUII50M00_F(){
        setInit();
    }

    private Data_IUII50M00_F(Parcel p_parcel){
        setInit();
        readFromParcel(p_parcel);
    }

    private void setInit(){

        reqId = "";
        statusName_1 = "";
        statusName_2 = "";
        statusCode = "";
        reqDate = "";
        typeCode = "";
        typeName = "";
        checkPerson = "";
        payDate = "";
        flagNeedMod = false;
    }

    /**
     * 데이터 세팅 함수
     * @param p_jsonObject
     * @throws JSONException
     */
    public void CF_setData(JSONObject p_jsonObject) throws JSONException {

        final String jsonKey_s_requ_recp_id = "s_requ_recp_id";
        final String jsonKey_s_acdt_insp_proc_stat_nm = "s_acdt_insp_proc_stat_nm";
        final String s_acdt_insp_proc_stat = "s_acdt_insp_proc_stat";
        final String jsonKey_s_recp_date = "s_recp_date";
        final String jsonKey_s_insu_requ_resn_code = "s_insu_requ_resn_code";
        final String jsonKey_s_rctr_nm = "s_rctr_nm";
        final String jsonKey_s_proc_date = "s_proc_date";
        final String jsonKey_s_requ_doc_yn = "s_requ_doc_yn";

        reqId = p_jsonObject.getString(jsonKey_s_requ_recp_id);             // 접수 아이디
        statusCode = p_jsonObject.getString(s_acdt_insp_proc_stat);        // 상태 코드

        setStatusName(statusCode);                                         // 상태명

        reqDate = p_jsonObject.getString(jsonKey_s_recp_date);              // 접수신청일
        if(reqDate.length() == 8){
            reqDate = reqDate.substring(0,4)+"."+reqDate.substring(4,6)+"."+reqDate.substring(6,8);
        }

        typeCode = p_jsonObject.getString(jsonKey_s_insu_requ_resn_code);   // 청구사유 코드
        checkPerson = p_jsonObject.getString(jsonKey_s_rctr_nm);            // 접수자
        payDate = p_jsonObject.getString(jsonKey_s_proc_date);              // 처리일자
        if(payDate.length() == 8){
            payDate = payDate.substring(0,4)+"."+payDate.substring(4,6)+"."+payDate.substring(6,8);
        }

        String[] tmp_arrTypeCode = typeCode.split(",");
        ArrayList<String> tmp_arrTypeName = new ArrayList<>();
        for(int i = 0 ; i < tmp_arrTypeCode.length; i++){
            tmp_arrTypeName.add(getConvertCodeName(tmp_arrTypeCode[i]));
        }
        typeName = TextUtils.join("/",tmp_arrTypeName);                     // 청구사유 텍스트

        String tmp_s_requ_doc_yn = p_jsonObject.getString(jsonKey_s_requ_doc_yn);
        if(tmp_s_requ_doc_yn.toLowerCase().equals("y")){
            flagNeedMod = true;                                            // 서류보완 필요 여부
        }
    }

    /**
     * 접수상태 값 세팅 함수
     * @param p_statusCode
     */
    private void setStatusName(String p_statusCode){

        String[] tmp_code = EnvConfig.reqStateCode;
        int tmp_index = -1;
        for(int i = 0 ; i < tmp_code.length; i++){
            if(tmp_code[i].equals(p_statusCode)){
                tmp_index = i;
                break;
            }
        }

        if(tmp_index >= 0 && tmp_index < EnvConfig.reqStateName_1.length && tmp_index < EnvConfig.reqStateName_2.length){
            statusName_1 = EnvConfig.reqStateName_1[tmp_index];
            statusName_2 = EnvConfig.reqStateName_2[tmp_index];
        }
        else{
            statusName_1 = "";
            statusName_2 = "";
        }
    }

    /**
     * 청구사유 코드 값을 문자로 반환한다.
     * @param p_typeCode
     * @return
     */
    private String getConvertCodeName(String p_typeCode){

        int tmp_index = Arrays.asList(EnvConfig.reqTypeCode).indexOf(p_typeCode);

        if(tmp_index >= 0 && tmp_index < EnvConfig.reqTypeName.length){
            return EnvConfig.reqTypeName[tmp_index];
        }

        return  "";
    }

    @Override
    public int describeContents() {
        return 0;
    }

    /**
     * Parcel Write
     */
    public void writeToParcel(Parcel p_parcel, int p_flags) {

        p_parcel.writeString(reqId);
        p_parcel.writeString(statusName_1);
        p_parcel.writeString(statusName_2);
        p_parcel.writeString(statusCode);
        p_parcel.writeString(reqDate);
        p_parcel.writeString(typeCode);
        p_parcel.writeString(typeName);
        p_parcel.writeString(checkPerson);
        p_parcel.writeString(payDate);
        p_parcel.writeInt(flagNeedMod?1:0);
    }

    /**
     * Parcel Read
     * @param p_parcel
     */
    private void readFromParcel(Parcel p_parcel)
    {
        reqId = p_parcel.readString();
        statusName_1 = p_parcel.readString();
        statusName_2 = p_parcel.readString();
        statusCode = p_parcel.readString();
        reqDate = p_parcel.readString();
        typeCode = p_parcel.readString();
        typeName = p_parcel.readString();
        checkPerson = p_parcel.readString();
        payDate = p_parcel.readString();
        flagNeedMod = p_parcel.readInt() > 0;
    }

    /**
     * Parcel Creator
     */
    public static Parcelable.Creator<Data_IUII50M00_F> CREATOR = new Parcelable.Creator<Data_IUII50M00_F>() {

        public Data_IUII50M00_F createFromParcel(Parcel source) {
            return new Data_IUII50M00_F(source);
        }

        public Data_IUII50M00_F[] newArray(int size) {
            return new Data_IUII50M00_F[size];
        }
    };
}
