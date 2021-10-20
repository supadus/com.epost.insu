package com.epost.insu.data;

import android.os.Build;
import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.RequiresApi;

/**
 * @copyright : (주)프로니스정보기술 | http://www.phronis.co.kr
 *
 * @project   : 모바일슈랑스 구축
 * @pakage    : com.epost.insu.data
 * @fileName  : Data_IUII10M03_F.java
 *
 * @Title     : 보험금청구 > 본인청구 > 3단계. 보험청구서작성(개인정보) (화면 ID : iuii10m03_f) -  상태값 데이터 클래스
 * @author    : 이수행
 * @created   : 2017-09-06
 * @version   : 1.0
 *
 * @note      :
 * ======================================================================
 * 수정 내역
 * NO      날짜          작업자       내용
 * 01      2017-09-06   이수행       최초 등록
 * 02      2020-12-10   양지훈       부담보내역 여부 추가
 * =======================================================================
 */
public class Data_IUII10M03_F implements Parcelable {

    private String name;                // 사용자 이름
    private String birth;               // 사용자 주민번호
    private String mobile;              // 사용자 휴대폰 번호
    private String zipNo;               // 우편번호
    private String address;             // 주소
    private String addressDetail;       // 주소 상세
    private Integer agreeAlarm;         // SMS알림서비스 동의 여부 상태값( -1 선택안함, 0 아니오 1 예)
    private String company;             // 직장명
    private String job;                 // 하는일가
    private String smbrPsblYn;          // 부담보내역 여부

    /**
     * 생성자
     */
    public Data_IUII10M03_F(){
        setInit();
    }
    private Data_IUII10M03_F(Parcel p_parcel){
        setInit();
        readFromParcel(p_parcel);
    }

    /**
     * 초기 세팅 함수
     */
    public void setInit(){
        name = "";
        birth = "";
        mobile = "";
        zipNo = "";
        address = "";
        addressDetail = "";
        agreeAlarm = 1;
        company = "";
        job = "";
        smbrPsblYn = "";
    }

    /**
     * clear 함수
     */
    public void CF_clear(){
        setInit();
    }

    public String CF_getName() {
        return name;
    }

    public void CF_setName(String name) {
        this.name = name;
    }

    public String CF_getBirth() { return birth; }

    public void CF_setBirth(String birth) {
        this.birth = birth;
    }

    public String CF_getMobile() {
        return mobile;
    }

    public void CF_setMobile(String mobile) {
        this.mobile = mobile;
    }

    public String CF_getAddress() {
        return address;
    }

    public void CF_setAddress(String address) {
        this.address = address;
    }

    public Integer CF_getAgreeAlarm() {
        return agreeAlarm;
    }

    public void CF_setAgreeAlarm(Integer agreeAlarm) {
        this.agreeAlarm = agreeAlarm;
    }

    public String CF_getCompany() {
        return company;
    }

    public void CF_setCompany(String company) {
        this.company = company;
    }

    public String CF_getJob() {
        return job;
    }

    public void CF_setJob(String job) {
        this.job = job;
    }

    public String CF_getZipNo() {
        return zipNo;
    }

    public void CF_setZipNo(String zipNo) {
        this.zipNo = zipNo;
    }

    public String CF_getAddressDetail() {
        return addressDetail;
    }

    public void CF_setAddressDetail(String addressDetail) {
        this.addressDetail = addressDetail;
    }

    public String CF_getSmbrPsblYn(){ return smbrPsblYn; }
    public void CF_setSmbrPsblYn(String smbrPsblYn){ this.smbrPsblYn = smbrPsblYn; }

    public int describeContents() {
        return 0;
    }


    /**
     * Parcel Write
     */
    public void writeToParcel(Parcel p_parcel, int p_flags)
    {
        p_parcel.writeString(name);
        p_parcel.writeString(birth);
        p_parcel.writeString(mobile);
        p_parcel.writeString(zipNo);
        p_parcel.writeString(address);
        p_parcel.writeString(addressDetail);
        p_parcel.writeString(company);
        p_parcel.writeString(job);
        p_parcel.writeInt(agreeAlarm);
        p_parcel.writeString(smbrPsblYn);
    }

    /**
     * Parcel Read
     * @param p_parcel
     */
    public void readFromParcel(Parcel p_parcel)
    {
        name = p_parcel.readString();
        birth = p_parcel.readString();
        mobile = p_parcel.readString();
        zipNo = p_parcel.readString();
        address = p_parcel.readString();
        addressDetail = p_parcel.readString();
        company = p_parcel.readString();
        job = p_parcel.readString();
        agreeAlarm = p_parcel.readInt();
        smbrPsblYn = p_parcel.readString();
    }


    /**
     * Parcel Creator
     */
    public static Parcelable.Creator<Data_IUII10M03_F> CREATOR = new Parcelable.Creator<Data_IUII10M03_F>() {

        public Data_IUII10M03_F createFromParcel(Parcel source) {
            return new Data_IUII10M03_F(source);
        }

        public Data_IUII10M03_F[] newArray(int size) {
            return new Data_IUII10M03_F[size];
        }
    };
}
