package com.epost.insu.data;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * @copyright : (주)프로니스정보기술 | http://www.phronis.co.kr
 *
 * @project   : 모바일슈랑스 구축
 * @pakage    : com.epost.insu.data
 * @fileName  : Data_IUII10M09_F.java
 *
 * @Title     : 보험금청구 > 본인청구 > 9단계. 보험금청구신청완료 (화면 ID : iuii10m09_f) - 상태값 데이터 클래스
 *              보험금청구 > 자녀청구 > 9단계. 보험금청구신청완료 (화면 ID : iuii10m09_f) - 상태값 데이터 클래스
 * @author    : 이수행
 * @created   : 2017-09-07
 * @version   : 1.0
 *
 * @note      :
 * ======================================================================
 * 수정 내역
 * NO      날짜          작업자       내용
 * 01      2017-09-07    이수행     : 최초 등록
 * 02      2019-12-24    노지민     : 사고일자, 진단명 추가
 *                                  : 자녀청구 완료 Data(Data_IUII90M09_F) 삭제 후 Data 공유
 * =======================================================================
 */
public class Data_IUII10M09_F implements Parcelable {

    private String number;                 // 접수번호
    private String date;                   // 접수일자
    private String center;                 // 접수센터
    private String personInCharge;         // 담당자
    private String centerPhone;            // 접수센터 전화번호
    private String reqName;                // 청구자
    private String reqCategory;            // 청구유형
    private String reqReason;              // 사고발생원인
    private String reqType;                // 청구사유
    private String dueDate;                // 보험처리예정일
    private String bankInfo;               // 송금요청계좌정보
    private String reqReasonDate;          // 사고일자             // 2019-12-24 추가
    private String reqPlace;               // 사고장소             // 2019-12-24 추가
    private String reqCntt;                // 사고내용             // 2019-12-24 추가
    private String reqDignNm;              // 진단명               // 2019-12-24 추가

    /**
     * 생성자
     */
    public Data_IUII10M09_F(){
        setInit();
    }
    private Data_IUII10M09_F(Parcel p_parcel){
        setInit();
        readFromParcel(p_parcel);
    }

    /**
     * 초기 세팅 함수
     */
    public void setInit(){
        number             = "";
        date               = "";
        center             = "";
        personInCharge     = "";
        centerPhone        = "";
        reqName            = "";
        reqCategory        = "";
        reqReason          = "";
        reqType            = "";
        dueDate            = "";
        bankInfo           = "";
        reqReasonDate      = "";
        reqPlace           = "";
        reqCntt            = "";
        reqDignNm          = "";
    }

    public String CF_getNumber() {
        return number;
    }
    public void CF_setNumber(String number) {
        this.number = number;
    }

    public String CF_getDate() {
        return date;
    }
    public void CF_setDate(String date) {
        this.date = date;
    }

    public String CF_getCenter() {
        return center;
    }
    public void CF_setCenter(String center) {
        this.center = center;
    }

    public String CF_getPersonInCharge() {
        return personInCharge;
    }
    public void CF_setPersonInCharge(String personInCharge) {
        this.personInCharge = personInCharge;
    }

    public String CF_getCenterPhone() {
        return centerPhone;
    }
    public void CF_setCenterPhone(String centerPhone) {
        this.centerPhone = centerPhone;
    }

    public String CF_getReqName() {
        return reqName;
    }
    public void CF_setReqName(String reqName) {
        this.reqName = reqName;
    }

    public String CF_getReqCategory() {
        return reqCategory;
    }
    public void CF_setReqCategory(String reqCategory) {
        this.reqCategory = reqCategory;
    }

    public String CF_getReqReason() {
        return reqReason;
    }
    public void CF_setReqReason(String reqReason) {
        this.reqReason = reqReason;
    }

    public String CF_getReqType() {
        return reqType;
    }
    public void CF_setReqType(String reqType) {
        this.reqType = reqType;
    }

    public String CF_getDueDate() {
        return dueDate;
    }
    public void CF_setDueDate(String dueDate) {
        this.dueDate = dueDate;
    }

    public String CF_getBankInfo() {
        return bankInfo;
    }
    public void CF_setBankInfo(String bankInfo) {
        this.bankInfo = bankInfo;
    }

    public String CF_getReqReasonDate() {
        return reqReasonDate;
    }
    public void CF_setReqReasonDate(String reqReasonDate) {
        this.reqReasonDate = reqReasonDate;
    }

    public String CF_getReqPlace() {
        return reqPlace;
    }
    public void CF_setReqPlace(String reqPlace) {
        this.reqPlace = reqPlace;
    }

    public String CF_getReqCntt() {
        return reqCntt;
    }
    public void CF_setReqCntt(String reqCntt) {
        this.reqCntt = reqCntt;
    }

    public String CF_getReqDignNm() {
        return reqDignNm;
    }
    public void CF_setReqDignNm(String reqDignNm) {
        this.reqDignNm = reqDignNm;
    }

    /**
     * clear 함수
     */
    public void CF_clear(){
        setInit();
    }


    public int describeContents() {
        return 0;
    }


    /**
     * Parcel Write
     */
    public void writeToParcel(Parcel p_parcel, int p_flags) {
        p_parcel.writeString(number);
        p_parcel.writeString(date);
        p_parcel.writeString(center);
        p_parcel.writeString(centerPhone);
        p_parcel.writeString(personInCharge);
        p_parcel.writeString(reqName);
        p_parcel.writeString(reqCategory);
        p_parcel.writeString(reqReason);
        p_parcel.writeString(reqType);
        p_parcel.writeString(dueDate);
        p_parcel.writeString(bankInfo);
        p_parcel.writeString(reqReasonDate);
        p_parcel.writeString(reqPlace);
        p_parcel.writeString(reqCntt);
        p_parcel.writeString(reqDignNm);
    }

    /**
     * Parcel Read
     * @param p_parcel
     */
    public void readFromParcel(Parcel p_parcel) {
        number          = p_parcel.readString();
        date            = p_parcel.readString();
        center          = p_parcel.readString();
        centerPhone     = p_parcel.readString();
        personInCharge  = p_parcel.readString();
        reqName         = p_parcel.readString();
        reqCategory     = p_parcel.readString();
        reqReason       = p_parcel.readString();
        reqType         = p_parcel.readString();
        dueDate         = p_parcel.readString();
        bankInfo        = p_parcel.readString();
        reqReasonDate   = p_parcel.readString();
        reqPlace        = p_parcel.readString();
        reqCntt         = p_parcel.readString();
        reqDignNm       = p_parcel.readString();
    }


    /**
     * Parcel Creator
     */
    public static Parcelable.Creator<Data_IUII10M09_F> CREATOR = new Parcelable.Creator<Data_IUII10M09_F>() {

        public Data_IUII10M09_F createFromParcel(Parcel source) {
            return new Data_IUII10M09_F(source);
        }

        public Data_IUII10M09_F[] newArray(int size) {
            return new Data_IUII10M09_F[size];
        }
    };
}
