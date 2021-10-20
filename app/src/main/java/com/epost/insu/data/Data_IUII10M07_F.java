package com.epost.insu.data;

import android.os.Parcel;
import android.os.Parcelable;

import com.epost.insu.common.LogPrinter;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * 보험금청구 > 본인청구 > 7단계. 계좌정보확인 및 추천국/추천인 선택 (화면 ID : iuii10m07_f) - 상태값 데이터 클래스
 * @since     :
 * @version   : 1.3
 * @author    : LSH
 * <pre>
 * ======================================================================
 *          LSH_20170907    최초 등록
 *          NJM_20200402    bankPostAccountList 변경 (Array -> list) : readFromParcel에서 에러발생함
 * 1.5.3    NJM_20210422    [자녀청구 예금주소실 수정] 주석수정
 * =======================================================================
 * copyright : (주)프로니스정보기술 | http://www.phronis.co.kr
 * </pre>
 */
public class Data_IUII10M07_F implements Parcelable {

    private String bankCode;                    // 금융기관 코드
    private String bankName;                    // 금융기관 이름
    private String bankOwner;                   // 예금주

    private String accountVisibilityDivision;   // 계좌필드 종류지정   ("one":단일, "list":스피너)
    private String bankAccount;                 // 입력된 계좌번호(타행계좌)  (단일필드)
    private String bankPostAccount;             // 선택된 계좌번호(우체국계좌)(리스트필드)
    private ArrayList<String> bankPostAccountList;      // 보유중인 우체국 계좌리스트

    private String recommDepartName;            // 추천국 이름
    private String recommDepartCode;            // 추천국 코드
    private String recommPersonName;            // 추천인 이름
    private String recommPersonCode;            // 추천인 코드

    /**
     * 생성자
     */
    public Data_IUII10M07_F(){
        setInit();
    }
    private Data_IUII10M07_F(Parcel p_parcel){
        setInit();
        readFromParcel(p_parcel);
    }

    /**
     * 초기 세팅 함수
     */
    public void setInit(){
        bankCode                    = "";
        bankName                    = "";
        bankOwner                   = "";
        accountVisibilityDivision   = "";
        bankAccount                 = "";
        bankPostAccount             = "";
        bankPostAccountList        = new ArrayList<>();
        recommDepartName            = "";
        recommPersonName            = "";
        recommDepartCode            = "";
        recommPersonCode            = "";
    }

    /**
     * clear 함수
     */
    public void CF_clear(){
        setInit();
    }

    public String getBankCode() {
        return bankCode;
    }
    public void setBankCode(String bankCode) {
        this.bankCode = bankCode;
    }


    public String getBankName() {
        return bankName;
    }
    public void setBankName(String bankName) {
        this.bankName = bankName;
    }


    public String getBankOwner() {
        return bankOwner;
    }
    public void setBankOwner(String bankOwner) {
        this.bankOwner = bankOwner;
    }


    public String getAccountVisibilityDivision() {
        return accountVisibilityDivision;
    }
    public void setAccountVisibilityDivision(String accountVisibilityDivision) {
        this.accountVisibilityDivision = accountVisibilityDivision;
    }

    public String getBankAccount() {
        return bankAccount;
    }
    public void setBankAccount(String bankAccount) {
        this.bankAccount = bankAccount;
    }


    public String getBankPostAccount() {
        return bankPostAccount;
    }
    public void setBankPostAccount(String bankPostAccount) {
        this.bankPostAccount = bankPostAccount;
    }

    public String[] getBankPostAccountArray() {
        String[] rtnArray = new String[bankPostAccountList.size()];
        int size=0;
        for(String item : bankPostAccountList) {
            rtnArray[size++] = item;
        }
        return rtnArray;
    }
    public void setBankPostAccountArray(String[] bankPostAccountArray) {
        ArrayList<String> arrayList = new ArrayList<>();
        for(String item : bankPostAccountArray) {
            arrayList.add(item);
        }
        this.bankPostAccountList = arrayList;
    }

    public String getRecommDepartName() {
        return recommDepartName;
    }

    public void setRecommDepartName(String recommDepartName) {
        this.recommDepartName = recommDepartName;
    }

    public String getRecommPersonName() {
        return recommPersonName;
    }

    public void setRecommPersonName(String recommPersonName) {
        this.recommPersonName = recommPersonName;
    }


    public String getRecommDepartCode() {
        return recommDepartCode;
    }
    public void setRecommDepartCode(String recommDepartCode) {
        this.recommDepartCode = recommDepartCode;
    }


    public String getRecommPersonCode() {
        return recommPersonCode;
    }
    public void setRecommPersonCode(String recommPersonCode) {
        this.recommPersonCode = recommPersonCode;
    }


    public int describeContents() {
        return 0;
    }
    /**
     * Parcel Write
     */
    public void writeToParcel(Parcel p_parcel, int p_flags) {
        p_parcel.writeString(bankCode);
        p_parcel.writeString(bankName);
        p_parcel.writeString(bankOwner);

        p_parcel.writeString(accountVisibilityDivision);
        p_parcel.writeString(bankAccount);
        p_parcel.writeString(bankPostAccount);
        p_parcel.writeStringList(bankPostAccountList);

        p_parcel.writeString(recommDepartName);
        p_parcel.writeString(recommDepartCode);
        p_parcel.writeString(recommPersonName);
        p_parcel.writeString(recommPersonCode);
    }

    /**
     * Parcel Read
     * @param p_parcel  Parcel
     */
    public void readFromParcel(Parcel p_parcel) {
        bankCode                    = p_parcel.readString();
        bankName                    = p_parcel.readString();
        bankOwner                   = p_parcel.readString();

        accountVisibilityDivision   = p_parcel.readString();
        bankAccount                 = p_parcel.readString();
        bankPostAccount             = p_parcel.readString();
        p_parcel.readStringList(bankPostAccountList);

        recommDepartName            = p_parcel.readString();
        recommDepartCode            = p_parcel.readString();
        recommPersonName            = p_parcel.readString();
        recommPersonCode            = p_parcel.readString();
    }

    /**
     * Parcel Creator
     */
    public static Parcelable.Creator<Data_IUII10M07_F> CREATOR = new Parcelable.Creator<Data_IUII10M07_F>() {

        public Data_IUII10M07_F createFromParcel(Parcel source) {
            return new Data_IUII10M07_F(source);
        }

        public Data_IUII10M07_F[] newArray(int size) {
            return new Data_IUII10M07_F[size];
        }
    };

    public void logPrint() {
        LogPrinter.CF_debug("!-----------------------------------------------------------");
        LogPrinter.CF_debug("!-- Data_IUII10M07_F.logPrint()");
        LogPrinter.CF_debug("!-----------------------------------------------------------");
        LogPrinter.CF_debug("!---- bankCode                  : " + bankCode);
        LogPrinter.CF_debug("!---- bankName                  : " + bankName);
        LogPrinter.CF_debug("!---- bankOwne                  : " + bankOwner);

        LogPrinter.CF_debug("!---- accountVisibilityDivision : " + accountVisibilityDivision);
        LogPrinter.CF_debug("!---- bankAccount               : " + bankAccount);
        LogPrinter.CF_debug("!---- bankPostAccount           : " + bankPostAccount);
        LogPrinter.CF_debug("!---- bankPostAccountArray      : " + bankPostAccountList);

        LogPrinter.CF_debug("!---- recommDepartName          : " + recommDepartName);
        LogPrinter.CF_debug("!---- recommPersonName          : " + recommPersonName);
        LogPrinter.CF_debug("!---- recommDepartCode          : " + recommDepartCode);
        LogPrinter.CF_debug("!---- recommPersonCode          : " + recommPersonCode);
    }
}
