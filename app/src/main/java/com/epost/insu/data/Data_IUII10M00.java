package com.epost.insu.data;

import android.os.Parcel;
import android.os.Parcelable;

import com.epost.insu.common.LogPrinter;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * 보험금청구 > 본인청구 > 보험금청구접수신청
 * @since     :
 * @version   : 1.3
 * @author    : LSH
 * <pre>
 *      보험금청구시 서버에 저장하여야할 데이터 클래스
 * ======================================================================
 *          YJH_            최초 등록
 *          LSH_20170908    최초 등록
 *          NJM_20190529    추천국/추천인 선택 추가
 *          NJM_20200331    p_parcel.writeString(s_mobl_no_1); 빠진거 추가
 * 1.5.3    NJM_20210422    [자녀청구 예금주소실 수정]
 * =======================================================================
 * copyright : (주)프로니스정보기술 | http://www.phronis.co.kr
 * </pre>
 */
public class Data_IUII10M00 implements Parcelable {

    private String loginUserName;               // 로그인 사용자 고객명 (청구자)

    private String s_acdp_csno;                 // 사고자(피보험자) 고객번호

    private String s_mobl_no_1;                 // 휴대폰번호 1
    private String s_mobl_no_2;                 // 휴대폰번호 2
    private String s_mobl_no_3;                 // 휴대폰번호 3
    private String s_infm_serv_rqst_ym;         // SMS알림서비스 신청여부
    private String s_ofce_nm;                   // 직장명
    private String s_ocpn_nm;                   // 직업명
    private String s_psno;                      // 우편번호

    private boolean b_smbr_psbl = false;        // 부담보내역 존재 여부

    private String s_bass_addr_nm;              // 기본주소명
    private String s_dtls_addr_nm;              // 상세주소명
    private String s_insu_requ_type_code;       // 청구유형 코드
    private String s_requ_gent_caus_code;       // 발생원인 코드
    private String s_insu_requ_resn_code;       // 청구사유 코드
    private String str_s_insu_requ_type_code;   // 청구유형 텍스트
    private String str_s_requ_gent_caus_code;   // 발생원인 텍스트
    private String str_s_insu_requ_resn_code;   // 청구사유 텍스트
    private String s_acdt_date;                 // 사고일자
    private String s_acdt_time;                 // 사고시간
    private String s_acdt_pace;                 // 사고장소
    private String s_acdt_cntt;                 // 사고내용
    private String s_dign_nm;                   // 진단명
    private String s_sick_code_no;              // 진단코드
    private String s_car_insu_yn;               // 자동차보험여부
    private String s_insu_comp_nm;              // (자동차보험)보험사명
    private String s_inds_dstr_insu_yn;         // 산업재해보험여부
    private String s_polc_decl_yn;              // 경찰신고여부
    private String s_etc_desc;                  // 기타설명
    private String s_othr_insu_comp_entr_yn;    // 타보험사 가입여부
    private String s_othr_insu_comp_nm;         // (타보험사)보험사명
    private String s_rllo_entr_yn;              // 실손가입여부
    private String s_rllo_insu_comp_nm;         // (실손)보험사명

    private String s_fnis_nm;                   // 금융기관명
    private String s_fnis_code;                 // 금융기관 코드
    private String s_acno;                      // 계좌번호 암호화 값
    private String decode_s_acno;               // 계좌번호 디코딩 값
    private String s_dpow_nm;                   // 예금주명

    private String imgCnt;                      // 이미지 개수
    private ArrayList<String> arrImgPath;       // 업로드 이미지 경로
    private String s_rcmd_clmm_no;              // 추천모집자번호
    private String s_rcmd_brn_code;             // 추천국코드


    /**
     * 생성자
     */
    public Data_IUII10M00(){
        setInit();
    }
    private Data_IUII10M00(Parcel p_parcel){
        setInit();
        readFromParcel(p_parcel);
    }

    /**
     * 초기 세팅 함수
     */
    public void setInit(){
        s_acdp_csno                 = "";
        loginUserName               = "";

        s_mobl_no_1                 = "";
        s_mobl_no_2                 = "";
        s_mobl_no_3                 = "";
        s_infm_serv_rqst_ym         = "";
        s_ofce_nm                   = "";
        s_ocpn_nm                   = "";
        s_psno                      = "";
        b_smbr_psbl                 = false;
        s_bass_addr_nm              = "";
        s_dtls_addr_nm              = "";
        s_insu_requ_type_code       = "";
        s_requ_gent_caus_code       = "";
        s_insu_requ_resn_code       = "";
        str_s_insu_requ_resn_code   = "";
        str_s_insu_requ_type_code   = "";
        str_s_requ_gent_caus_code   = "";
        s_acdt_date                 = "";
        s_acdt_time                 = "";
        s_acdt_pace                 = "";
        s_acdt_cntt                 = "";
        s_dign_nm                   = "";
        s_sick_code_no              = "";
        s_car_insu_yn               = "";
        s_insu_comp_nm              = "";
        s_inds_dstr_insu_yn         = "";
        s_polc_decl_yn              = "";
        s_etc_desc                  = "";
        s_othr_insu_comp_entr_yn    = "";
        s_othr_insu_comp_nm         = "";
        s_rllo_entr_yn              = "";
        s_rllo_insu_comp_nm         = "";
        s_fnis_nm                   = "";
        s_fnis_code                 = "";
        s_acno                      = "";
        decode_s_acno               = "";
        s_dpow_nm                   = "";
        imgCnt                      = "";
        arrImgPath                  = new ArrayList<>();
        s_rcmd_clmm_no              = "";
        s_rcmd_brn_code             = "";
    }

    //----------------------------------------------------------------------------------------------
    // Getter & Setter
    //----------------------------------------------------------------------------------------------
    public String CF_getLoginUserName() {
        return loginUserName;
    }
    public void CF_setLoginUserName(String loginUserName) {
        this.loginUserName = loginUserName;
    }

    public String CF_getS_acdp_csno() {
        return s_acdp_csno;
    }
    public void CF_setS_acdp_csno(String s_acdp_csno) {
        this.s_acdp_csno = s_acdp_csno;
    }

    public String CF_getS_mobl_no_1() {
        return s_mobl_no_1;
    }
    public void CF_setS_mobl_no_1(String s_mobl_no_1) {
        this.s_mobl_no_1 = s_mobl_no_1;
    }

    public String CF_getS_mobl_no_2() {
        return s_mobl_no_2;
    }
    public void CF_setS_mobl_no_2(String s_mobl_no_2) {
        this.s_mobl_no_2 = s_mobl_no_2;
    }

    public String CF_getS_mobl_no_3() {
        return s_mobl_no_3;
    }
    public void CF_setS_mobl_no_3(String s_mobl_no_3) {
        this.s_mobl_no_3 = s_mobl_no_3;
    }

    public String CF_getS_infm_serv_rqst_ym() {
        return s_infm_serv_rqst_ym;
    }
    public void CF_setS_infm_serv_rqst_ym(String s_infm_serv_rqst_ym) { this.s_infm_serv_rqst_ym = s_infm_serv_rqst_ym; }

    public String CF_getS_cfce_nm() {
        return s_ofce_nm;
    }
    public void CF_setS_cfce_nm(String s_cfce_nm) {
        this.s_ofce_nm = s_cfce_nm;
    }

    public String CF_getS_ocpn_nm() {
        return s_ocpn_nm;
    }
    public void CF_setS_ocpn_nm(String s_ocpn_nm) {
        this.s_ocpn_nm = s_ocpn_nm;
    }

    public String CF_getS_psno() {
        return s_psno;
    }
    public void CF_setS_psno(String s_psno) {
        this.s_psno = s_psno;
    }

    public boolean CF_getB_smbr_psbl() { return b_smbr_psbl; }
    public void CF_setB_smbr_psbl(boolean b_smbr_psbl){ this.b_smbr_psbl = b_smbr_psbl; }

    public String CF_getS_bass_addr_nm() {
        return s_bass_addr_nm;
    }
    public void CF_setS_bass_addr_nm(String s_bass_addr_nm) { this.s_bass_addr_nm = s_bass_addr_nm; }

    public String CF_getS_dtls_addr_nm() {
        return s_dtls_addr_nm;
    }
    public void CF_setS_dtls_addr_nm(String s_dtls_addr_nm) { this.s_dtls_addr_nm = s_dtls_addr_nm; }

    public String CF_getS_insu_requ_type_code() {
        return s_insu_requ_type_code;
    }
    public void CF_setS_insu_requ_type_code(String s_insu_requ_type_code) { this.s_insu_requ_type_code = s_insu_requ_type_code; }

    public String CF_getS_requ_gent_caus_code() {
        return s_requ_gent_caus_code;
    }
    public void CF_setS_requ_gent_caus_code(String s_requ_gent_caus_code) { this.s_requ_gent_caus_code = s_requ_gent_caus_code; }

    public String CF_getS_insu_requ_resn_code() {
        return s_insu_requ_resn_code;
    }
    public void CF_setS_insu_requ_resn_code(String s_insu_requ_resn_code) { this.s_insu_requ_resn_code = s_insu_requ_resn_code; }

    public String CF_getStr_s_insu_requ_type_code() {
        return str_s_insu_requ_type_code;
    }
    public void CF_setStr_s_insu_requ_type_code(String str_s_insu_requ_type_code) { this.str_s_insu_requ_type_code = str_s_insu_requ_type_code; }

    public String CF_getStr_s_requ_gent_caus_code() {
        return str_s_requ_gent_caus_code;
    }
    public void CF_setStr_s_requ_gent_caus_code(String str_s_requ_gent_caus_code) { this.str_s_requ_gent_caus_code = str_s_requ_gent_caus_code; }

    public String CF_getStr_s_insu_requ_resn_code() {
        return str_s_insu_requ_resn_code;
    }
    public void CF_setStr_s_insu_requ_resn_code(String str_s_insu_requ_resn_code) { this.str_s_insu_requ_resn_code = str_s_insu_requ_resn_code; }

    public String CF_getS_acdt_date() {
        return s_acdt_date;
    }
    public void CF_setS_acdt_date(String s_acdt_date) {
        this.s_acdt_date = s_acdt_date;
    }

    public String CF_getS_acdt_time() {
        return s_acdt_time;
    }
    public void CF_setS_acdt_time(String s_acdt_time) {
        this.s_acdt_time = s_acdt_time;
    }

    public String CF_getS_acdt_pace() {
        return s_acdt_pace;
    }
    public void CF_setS_acdt_pace(String s_acdt_pace) {
        this.s_acdt_pace = s_acdt_pace;
    }

    public String CF_getS_acdt_cntt() {
        return s_acdt_cntt;
    }
    public void CF_setS_acdt_cntt(String s_acdt_cntt) {
        this.s_acdt_cntt = s_acdt_cntt;
    }

    public String CF_getS_dign_nm() {
        return s_dign_nm;
    }
    public void CF_setS_dign_nm(String s_dign_nm) {
        this.s_dign_nm = s_dign_nm;
    }

    public String CF_getS_sick_code_no() {
        return s_sick_code_no;
    }
    public void CF_setS_sick_code_no(String s_sick_code_no) { this.s_sick_code_no = s_sick_code_no; }

    public String CF_getS_car_insu_yn() {
        return s_car_insu_yn;
    }
    public void CF_setS_car_insu_yn(String s_car_insu_yn) {
        this.s_car_insu_yn = s_car_insu_yn;
    }

    public String CF_getS_insu_comp_nm() {
        return s_insu_comp_nm;
    }
    public void CF_setS_insu_comp_nm(String s_insu_comp_nm) { this.s_insu_comp_nm = s_insu_comp_nm; }

    public String CF_getS_inds_dstr_insu_yn() {
        return s_inds_dstr_insu_yn;
    }
    public void CF_setS_inds_dstr_insu_yn(String s_inds_dstr_insu_yn) { this.s_inds_dstr_insu_yn = s_inds_dstr_insu_yn; }

    public String CF_getS_polc_decl_yn() {
        return s_polc_decl_yn;
    }
    public void CF_setS_polc_decl_yn(String s_polc_decl_yn) { this.s_polc_decl_yn = s_polc_decl_yn; }

    public String CF_getS_etc_desc() {
        return s_etc_desc;
    }
    public void CF_setS_etc_desc(String s_etc_desc) {
        this.s_etc_desc = s_etc_desc;
    }

    public String CF_getS_other_insu_comp_entr_yn() {
        return s_othr_insu_comp_entr_yn;
    }
    public void CF_setS_other_insu_comp_entr_yn(String s_othr_insu_comp_entr_yn) { this.s_othr_insu_comp_entr_yn = s_othr_insu_comp_entr_yn; }

    public String CF_getS_other_insu_comp_nm() {
        return s_othr_insu_comp_nm;
    }
    public void CF_setS_other_insu_comp_nm(String s_othr_insu_comp_nm) { this.s_othr_insu_comp_nm = s_othr_insu_comp_nm; }

    public String CF_getS_rllo_entr_yn() {
        return s_rllo_entr_yn;
    }
    public void CF_setS_rllo_entr_yn(String s_rllo_entr_yn) { this.s_rllo_entr_yn = s_rllo_entr_yn; }

    public String CF_getS_rllo_insu_comp_nm() {
        return s_rllo_insu_comp_nm;
    }
    public void CF_setS_rllo_insu_comp_nm(String s_rllo_insu_comp_nm) { this.s_rllo_insu_comp_nm = s_rllo_insu_comp_nm; }

    public String CF_getS_fnis_nm() {
        return s_fnis_nm;
    }
    public void CF_setS_fnis_nm(String s_fnis_nm) {
        this.s_fnis_nm = s_fnis_nm;
    }

    public String CF_getS_fnis_code() {
        return s_fnis_code;
    }
    public void CF_setS_fnis_code(String s_fnis_code) {
        this.s_fnis_code = s_fnis_code;
    }

    public String CF_getS_acno() {
        return s_acno;
    }
    public void CF_setS_acno(String s_acno) {
        this.s_acno = s_acno;
    }

    public String CF_getDecode_s_acno() {
        return decode_s_acno;
    }
    public void CF_setDecode_s_acno(String decode_s_acno) {
        this.decode_s_acno = decode_s_acno;
    }

    public String CF_getS_dpow_nm() {
        return s_dpow_nm;
    }
    public void CF_setS_dpow_nm(String s_dpow_nm) {
        this.s_dpow_nm = s_dpow_nm;
    }

    public String CF_getImgCnt() {
        return imgCnt;
    }
    public void CF_setImgCnt(String imgCnt) {
        this.imgCnt = imgCnt;
    }

    public ArrayList<String> CF_getArrImgPath() {
        return arrImgPath;
    }
    public void CF_setArrImgPath(ArrayList<String> arrImgPath) {
        this.arrImgPath.clear();
        this.arrImgPath.addAll(arrImgPath);
    }

    // 추천인모집자코드
    public String CF_getS_rcmd_clmm_no() {
        return s_rcmd_clmm_no;
    }
    public void CF_setS_rcmd_clmm_no(String s_rcmd_clmm_no) {
        this.s_rcmd_clmm_no = s_rcmd_clmm_no;
    }

    // 추천국코드
    public String CF_getS_rcmd_brn_code() {
        return s_rcmd_brn_code;
    }
    public void CF_setS_rcmd_brn_code(String s_rcmd_brn_code) {
        this.s_rcmd_brn_code = s_rcmd_brn_code;
    }

    public int describeContents() {
        return 0;
    }

    /**
     * 이미지 파일 리스트 반환
     * @return  ArrayList<File>
     */
    public ArrayList<File> CF_getUploadFileList(){
        ArrayList<File> tmp_arrFile = new ArrayList<>();

        for(int i = 0 ; i < arrImgPath.size(); i++){
            File tmp_file = new File(arrImgPath.get(i));

            if(tmp_file.exists()){
                tmp_arrFile.add(tmp_file);
            }
        }

        return tmp_arrFile;
    }

    /**
     * 이미지 파일 업로드 키 리스트 반환
     * @return  ArrayList<String>
     */
    public ArrayList<String> CF_getUploadFileKeyList(){

        ArrayList<File> tmp_arrFile = CF_getUploadFileList();
        ArrayList<String> tmp_arrKey = new ArrayList<>();

        for(int i = 0 ; i < tmp_arrFile.size(); i++){
            tmp_arrKey.add("img"+(i+1));
        }

        return tmp_arrKey;
    }

    /**
     * 데이터를 HshMap 에 담아 반환
     * @return  HashMap<String,String>
     */
    public HashMap<String,String> CF_getDataMap(){
        HashMap<String,String> tmp_hashMap = new HashMap<>();
        tmp_hashMap.put("s_acdp_csno", s_acdp_csno);

        tmp_hashMap.put("s_mobl_no_1", s_mobl_no_1);
        tmp_hashMap.put("s_mobl_no_2", s_mobl_no_2);
        tmp_hashMap.put("s_mobl_no_3", s_mobl_no_3);

        tmp_hashMap.put("s_infm_serv_rqst_ym",s_infm_serv_rqst_ym);     // 삭제 필요 :: s_infm_serv_rqst_yn으로 키 변경
        tmp_hashMap.put("s_infm_serv_rqst_yn",s_infm_serv_rqst_ym);
        tmp_hashMap.put("s_ofce_nm", s_ofce_nm);
        tmp_hashMap.put("s_ocpn_nm",s_ocpn_nm);
        tmp_hashMap.put("s_psno",s_psno);
        tmp_hashMap.put("s_bass_addr_nm",s_bass_addr_nm);
        tmp_hashMap.put("s_dtls_addr_nm",s_dtls_addr_nm);
        tmp_hashMap.put("s_insu_requ_type_code",s_insu_requ_type_code);
        tmp_hashMap.put("s_requ_gent_caus_code", s_requ_gent_caus_code);
        tmp_hashMap.put("s_insu_requ_resn_code",s_insu_requ_resn_code);
        tmp_hashMap.put("s_acdt_date",s_acdt_date);
        tmp_hashMap.put("s_acdt_time",s_acdt_time);
        tmp_hashMap.put("s_acdt_pace",s_acdt_pace);
        tmp_hashMap.put("s_acdt_cntt",s_acdt_cntt);
        tmp_hashMap.put("s_dign_nm",s_dign_nm);
        /*
        try {
            tmp_hashMap.put("s_dign_nm", URLEncoder.encode(s_dign_nm,"utf-8"));
        } catch (UnsupportedEncodingException e) {
            LogPrinter.CF_line();
            LogPrinter.CF_debug("URLEncoder endoing 실패 : "+s_dign_nm);
        }
        */
        tmp_hashMap.put("s_sick_code_no",s_sick_code_no);
        tmp_hashMap.put("s_car_insu_yn",s_car_insu_yn);
        if(s_car_insu_yn.equals("Y")) {
            tmp_hashMap.put("s_insu_comp_nm",s_insu_comp_nm);
        }

        tmp_hashMap.put("s_inds_dstr_insu_yn",s_inds_dstr_insu_yn);
        tmp_hashMap.put("s_polc_decl_yn",s_polc_decl_yn);
        tmp_hashMap.put("s_etc_desc",s_etc_desc);
        tmp_hashMap.put("s_othr_insu_comp_entr_yn", s_othr_insu_comp_entr_yn);
        if(s_othr_insu_comp_entr_yn.equals("Y")) {
            tmp_hashMap.put("s_othr_insu_comp_nm", s_othr_insu_comp_nm);
        }
        tmp_hashMap.put("s_rllo_entr_yn",s_rllo_entr_yn);
        if(s_rllo_entr_yn.equals("Y")) {
            tmp_hashMap.put("s_rllo_insu_comp_nm",s_rllo_insu_comp_nm);
        }
        tmp_hashMap.put("s_fnis_nm",s_fnis_nm);
        tmp_hashMap.put("s_fnis_code",s_fnis_code);
        tmp_hashMap.put("s_acno",s_acno);
        tmp_hashMap.put("s_dpow_nm",s_dpow_nm);
        tmp_hashMap.put("imgCnt",imgCnt);

        tmp_hashMap.put("s_rcmd_clmm_no",s_rcmd_clmm_no);                           // 추천모집자코드
        tmp_hashMap.put("s_rcmd_brn_code",s_rcmd_brn_code);                         // 추천국코드

        return  tmp_hashMap;
    }


    @Override
    public String toString() {
        return "Data_IUII10M00{" +
                "s_acdp_csno='" + s_acdp_csno + '\'' +
                ", loginUserName='" + loginUserName + '\'' +
                ", s_mobl_no_1='" + s_mobl_no_1 + '\'' +
                ", s_mobl_no_2='" + s_mobl_no_2 + '\'' +
                ", s_mobl_no_3='" + s_mobl_no_3 + '\'' +
                ", s_infm_serv_rqst_ym='" + s_infm_serv_rqst_ym + '\'' +
                ", s_ofce_nm='" + s_ofce_nm + '\'' +
                ", s_ocpn_nm='" + s_ocpn_nm + '\'' +
                ", s_psno='" + s_psno + '\'' +
                ", s_bass_addr_nm='" + s_bass_addr_nm + '\'' +
                ", s_dtls_addr_nm='" + s_dtls_addr_nm + '\'' +
                ", s_insu_requ_type_code='" + s_insu_requ_type_code + '\'' +
                ", s_requ_gent_caus_code='" + s_requ_gent_caus_code + '\'' +
                ", s_insu_requ_resn_code='" + s_insu_requ_resn_code + '\'' +
                ", s_acdt_date='" + s_acdt_date + '\'' +
                ", s_acdt_time='" + s_acdt_time + '\'' +
                ", s_acdt_pace='" + s_acdt_pace + '\'' +
                ", s_acdt_cntt='" + s_acdt_cntt + '\'' +
                ", s_dign_nm='" + s_dign_nm + '\'' +
                ", s_sick_code_no='" + s_sick_code_no + '\'' +
                ", s_car_insu_yn='" + s_car_insu_yn + '\'' +
                ", s_insu_comp_nm='" + s_insu_comp_nm + '\'' +
                ", s_inds_dstr_insu_yn='" + s_inds_dstr_insu_yn + '\'' +
                ", s_polc_decl_yn='" + s_polc_decl_yn + '\'' +
                ", s_etc_desc='" + s_etc_desc + '\'' +
                ", s_othr_insu_comp_entr_yn='" + s_othr_insu_comp_entr_yn + '\'' +
                ", s_othr_insu_comp_nm='" + s_othr_insu_comp_nm + '\'' +
                ", s_rllo_entr_yn='" + s_rllo_entr_yn + '\'' +
                ", s_rllo_insu_comp_nm='" + s_rllo_insu_comp_nm + '\'' +
                ", s_fnis_nm='" + s_fnis_nm + '\'' +
                ", s_fnis_code='" + s_fnis_code + '\'' +
                ", s_acno='" + s_acno + '\'' +
                ", s_dpow_nm='" + s_dpow_nm + '\'' +
                ", imgCnt='" + imgCnt + '\'' +
                ", arrImgPath=" + arrImgPath +
                ", s_rcmd_clmm_no=" + s_rcmd_clmm_no +
                ", s_rcmd_brn_code=" + s_rcmd_brn_code +
                '}';
    }

    /**
     * Parcel Write
     */
    public void writeToParcel(Parcel p_parcel, int p_flags) {
        p_parcel.writeString(s_acdp_csno);
        p_parcel.writeString(loginUserName);
        p_parcel.writeString(s_mobl_no_1);
        p_parcel.writeString(s_mobl_no_2);
        p_parcel.writeString(s_mobl_no_3);
        p_parcel.writeString(s_infm_serv_rqst_ym);
        p_parcel.writeString(s_ofce_nm);
        p_parcel.writeString(s_ocpn_nm);
        p_parcel.writeString(s_psno);
        p_parcel.writeString(s_bass_addr_nm);
        p_parcel.writeString(s_dtls_addr_nm);
        p_parcel.writeString(s_insu_requ_type_code);
        p_parcel.writeString(s_requ_gent_caus_code);
        p_parcel.writeString(s_insu_requ_resn_code);
        p_parcel.writeString(str_s_insu_requ_type_code);
        p_parcel.writeString(str_s_requ_gent_caus_code);
        p_parcel.writeString(str_s_insu_requ_resn_code);
        p_parcel.writeString(s_acdt_date);
        p_parcel.writeString(s_acdt_time);
        p_parcel.writeString(s_acdt_pace);
        p_parcel.writeString(s_acdt_cntt);
        p_parcel.writeString(s_dign_nm);
        p_parcel.writeString(s_sick_code_no);
        p_parcel.writeString(s_car_insu_yn);
        p_parcel.writeString(s_insu_comp_nm);
        p_parcel.writeString(s_inds_dstr_insu_yn);
        p_parcel.writeString(s_polc_decl_yn);
        p_parcel.writeString(s_etc_desc);
        p_parcel.writeString(s_othr_insu_comp_entr_yn);
        p_parcel.writeString(s_othr_insu_comp_nm);
        p_parcel.writeString(s_rllo_entr_yn);
        p_parcel.writeString(s_rllo_insu_comp_nm);
        p_parcel.writeString(s_fnis_nm);
        p_parcel.writeString(s_fnis_code);
        p_parcel.writeString(s_acno);
        p_parcel.writeString(decode_s_acno);
        p_parcel.writeString(s_dpow_nm);
        p_parcel.writeString(imgCnt);
        p_parcel.writeStringList(arrImgPath);
        p_parcel.writeString(s_rcmd_clmm_no);
        p_parcel.writeString(s_rcmd_brn_code);
    }

    /**
     * Parcel Read
     * @param p_parcel  Parcel
     */
    public void readFromParcel(Parcel p_parcel) {
        s_acdp_csno                 = p_parcel.readString();
        loginUserName               = p_parcel.readString();

        s_mobl_no_1                 = p_parcel.readString();
        s_mobl_no_2                 = p_parcel.readString();
        s_mobl_no_3                 = p_parcel.readString();
        s_infm_serv_rqst_ym         = p_parcel.readString();
        s_ofce_nm                   = p_parcel.readString();
        s_ocpn_nm                   = p_parcel.readString();
        s_psno                      = p_parcel.readString();
        s_bass_addr_nm              = p_parcel.readString();
        s_dtls_addr_nm              = p_parcel.readString();
        s_insu_requ_type_code       = p_parcel.readString();
        s_requ_gent_caus_code       = p_parcel.readString();
        s_insu_requ_resn_code       = p_parcel.readString();
        str_s_insu_requ_type_code   = p_parcel.readString();
        str_s_requ_gent_caus_code   = p_parcel.readString();
        str_s_insu_requ_resn_code   = p_parcel.readString();
        s_acdt_date                 = p_parcel.readString();
        s_acdt_time                 = p_parcel.readString();
        s_acdt_pace                 = p_parcel.readString();
        s_acdt_cntt                 = p_parcel.readString();
        s_dign_nm                   = p_parcel.readString();
        s_sick_code_no              = p_parcel.readString();
        s_car_insu_yn               = p_parcel.readString();
        s_insu_comp_nm              = p_parcel.readString();
        s_inds_dstr_insu_yn         = p_parcel.readString();
        s_polc_decl_yn              = p_parcel.readString();
        s_etc_desc                  = p_parcel.readString();
        s_othr_insu_comp_entr_yn    = p_parcel.readString();
        s_othr_insu_comp_nm         = p_parcel.readString();
        s_rllo_entr_yn              = p_parcel.readString();
        s_rllo_insu_comp_nm         = p_parcel.readString();
        s_fnis_nm                   = p_parcel.readString();
        s_fnis_code                 = p_parcel.readString();
        s_acno                      = p_parcel.readString();
        decode_s_acno               = p_parcel.readString();
        s_dpow_nm                   = p_parcel.readString();
        imgCnt                      = p_parcel.readString();
        p_parcel.readStringList(arrImgPath);
        s_rcmd_clmm_no              = p_parcel.readString();
        s_rcmd_brn_code             = p_parcel.readString();
    }

    /**
     * Parcel Creator
     */
    public static Parcelable.Creator<Data_IUII10M00> CREATOR = new Parcelable.Creator<Data_IUII10M00>() {

        public Data_IUII10M00 createFromParcel(Parcel source) {
            return new Data_IUII10M00(source);
        }

        public Data_IUII10M00[] newArray(int size) {
            return new Data_IUII10M00[size];
        }
    };


    public void logPrint() {
        LogPrinter.CF_debug("!-----------------------------------------------------------");
        LogPrinter.CF_debug("!-- Data_IUII10M00.logPrint() -- 보험청구 DATA");
        LogPrinter.CF_debug("!-----------------------------------------------------------");
        LogPrinter.CF_debug("!---- s_acdp_csno(사고자(피보험자) 고객번호)              : " + s_acdp_csno);
        LogPrinter.CF_debug("!---- loginUserName(로그인 사용자 고객명 (청구자))        : " + loginUserName);
        LogPrinter.CF_debug("!---- s_mobl_no_1(휴대폰번호 1)                          : " + s_mobl_no_1);
        LogPrinter.CF_debug("!---- s_mobl_no_2(휴대폰번호 2)                          : " + s_mobl_no_2);
        LogPrinter.CF_debug("!---- s_mobl_no_3(휴대폰번호 3)                          : " + s_mobl_no_3);
        LogPrinter.CF_debug("!---- s_infm_serv_rqst_ym(SMS알림서비스 신청여부)         : " + s_infm_serv_rqst_ym);
        LogPrinter.CF_debug("!---- s_ofce_nm(직장명)                                  : " + s_ofce_nm);
        LogPrinter.CF_debug("!---- s_ocpn_nm(직업명)                                  : " + s_ocpn_nm);
        LogPrinter.CF_debug("!---- s_psno(우편번호)                                   : " + s_psno);
        LogPrinter.CF_debug("!---- s_bass_addr_nm(기본주소명)                         : " + s_bass_addr_nm);
        LogPrinter.CF_debug("!---- s_dtls_addr_nm(상세주소명)                         : " + s_dtls_addr_nm);
        LogPrinter.CF_debug("!---- s_insu_requ_type_code(청구유형 코드)               : " + s_insu_requ_type_code);
        LogPrinter.CF_debug("!---- s_requ_gent_caus_code(발생원인 코드)               : " + s_requ_gent_caus_code);
        LogPrinter.CF_debug("!---- s_insu_requ_resn_code(청구사유 코드)               : " + s_insu_requ_resn_code);
        LogPrinter.CF_debug("!---- str_s_insu_requ_type_code(청구유형 텍스트)         : " + str_s_insu_requ_type_code  );
        LogPrinter.CF_debug("!---- str_s_requ_gent_caus_code(발생원인 텍스트)         : " + str_s_requ_gent_caus_code  );
        LogPrinter.CF_debug("!---- str_s_insu_requ_resn_code(청구사유 텍스트)         : " + str_s_insu_requ_resn_code);
        LogPrinter.CF_debug("!---- s_acdt_date(사고일자)                              : " + s_acdt_date);
        LogPrinter.CF_debug("!---- s_acdt_time(사고시간)                              : " + s_acdt_time);
        LogPrinter.CF_debug("!---- s_acdt_pace(사고장소)                              : " + s_acdt_pace);
        LogPrinter.CF_debug("!---- s_acdt_cntt(사고내용)                              : " + s_acdt_cntt);
        LogPrinter.CF_debug("!---- s_dign_nm(진단명)                                  : " + s_dign_nm);
        LogPrinter.CF_debug("!---- s_sick_code_no(진단코드)                           : " + s_sick_code_no);
        LogPrinter.CF_debug("!---- s_car_insu_yn(자동차보험여부)                      : " + s_car_insu_yn);
        LogPrinter.CF_debug("!---- s_insu_comp_nm(자동차보험 보험사명)                 : " + s_insu_comp_nm);
        LogPrinter.CF_debug("!---- s_inds_dstr_insu_yn(산업재해보험여부)               : " + s_inds_dstr_insu_yn);
        LogPrinter.CF_debug("!---- s_polc_decl_yn(경찰신고여부)                        : " + s_polc_decl_yn);
        LogPrinter.CF_debug("!---- s_etc_desc(기타설명)                                : " + s_etc_desc);
        LogPrinter.CF_debug("!---- s_othr_insu_comp_entr_yn(타보험사 가입여부)         : " + s_othr_insu_comp_entr_yn   );
        LogPrinter.CF_debug("!---- s_othr_insu_comp_nm((타보험사)보험사명)             : " + s_othr_insu_comp_nm        );
        LogPrinter.CF_debug("!---- s_rllo_entr_yn(실손가입여부)                        : " + s_rllo_entr_yn             );
        LogPrinter.CF_debug("!---- s_rllo_insu_comp_nm((실손)보험사명)                 : " + s_rllo_insu_comp_nm        );
        LogPrinter.CF_debug("!---- s_fnis_nm(금융기관명)                               : " + s_fnis_nm                  );
        LogPrinter.CF_debug("!---- s_fnis_code(금융기관 코드)                          : " + s_fnis_code                );
        LogPrinter.CF_debug("!---- s_acno(계좌번호 암호화 값)                          : " + s_acno                     );
        LogPrinter.CF_debug("!---- decode_s_acno(계좌번호 디코딩 값):                  : " + decode_s_acno              );
        LogPrinter.CF_debug("!---- s_dpow_nm(예금주명)                                : " + s_dpow_nm                  );
        LogPrinter.CF_debug("!---- imgCnt(이미지 개수)                                : " + imgCnt                     );
        LogPrinter.CF_debug("!---- arrImgPath(업로드 이미지 경로)                     : " + arrImgPath                 );
        LogPrinter.CF_debug("!---- s_rcmd_clmm_no(추천모집자번호)                     : " + s_rcmd_clmm_no             );
        LogPrinter.CF_debug("!---- s_rcmd_brn_code(추천국코드)                        : " + s_rcmd_brn_code            );
    }
}
