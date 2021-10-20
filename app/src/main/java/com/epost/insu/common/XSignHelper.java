package com.epost.insu.common;

import android.content.Context;
import android.os.Environment;

import com.dreamsecurity.magicxsign.MagicXSign;
import com.dreamsecurity.magicxsign.MagicXSign_Err;
import com.dreamsecurity.magicxsign.MagicXSign_Exception;
import com.dreamsecurity.magicxsign.MagicXSign_Type;
import com.epost.insu.R;
import com.epost.insu.data.Data_CertDetail;

import java.util.ArrayList;

/**
 * 공동인증서(MagicXSign) 헬퍼 클래스
 * @since     :
 * @version   : 1.1
 * @author    : LSH
 * <pre>
 * 공동인증 로그인 관련 모바일단 흐름
 *  1. 전자서명데이터 생성 {@link #CF_certSign(int, String, byte[])}
 *  2. VID 랜덤값 추출 {@link #CF_getVIDRandom(int, byte[])}
 *  3. VID 랜덤값을 BASE64 인코딩 {@link #CF_encodeBase64(byte[])}
 *  4. 전자서명데이터와 VID 랜덤값 WAS 서버 전송
 * ======================================================================
 *          LSH_20171026    최초 등록
 * 1.6.3    NJM_20211008    [API30 대응] 솔루션 업데이트 반영 (인증서 공용 -> 내부 복사 로직 추가)
 * =======================================================================
 * copyright : (주)프로니스정보기술 | http://www.phronis.co.kr
 * </pre>
 */
public class XSignHelper {

    private final String mRootPath = Environment.getExternalStorageDirectory().getPath();
    private MagicXSign mMagicXSign;
    private Context mContext;

    /**
     * 생성자
     * @param p_context Context
     * @param p_debugLevel 0 : 아무것도 안남김 , 100 모두 , 200 Warning 이상, 300 Error 이상
     */
    public XSignHelper(Context p_context, int p_debugLevel){
        mContext = p_context;
        mMagicXSign = new MagicXSign();

        try {
            mMagicXSign.Init(mContext,p_debugLevel);
        } catch (MagicXSign_Exception e) {
            LogPrinter.CF_debug(mContext.getResources().getString(R.string.log_fail_init_xsign) + " : " + e.getMessage());
        }
    }

    /**
     * MagicXSign 종료 함수</br>
     * MagicXSign 자원 해제 수행
     */
    public void CF_Finish(){
        if(mMagicXSign != null){

            try {
                mMagicXSign.Finish();
                mMagicXSign = null;
            } catch (MagicXSign_Exception e) {
                LogPrinter.CF_debug(mContext.getResources().getString(R.string.log_fail_finish_xsign) + " : " + e.getMessage());
            }
        }
    }

    /**
     * 서명용 인증서 목록 반환 함수
     * @see #getCertList(int)
     * @return  ArrayList<Data_CertDetail>
     */
    public ArrayList<Data_CertDetail> getCertList() throws MagicXSign_Exception {
        return getCertList(MagicXSign_Type.XSIGN_PKI_CERT_SIGN);
    }

    /**
     * p_certType에 따른 인증서 목록 반환 함수<br/>
     * p_certType 정보는 아래 참조<br/>
     * MagicXSign_Type.XSIGN_PKI_CERT_SIGN : 서명용 인증서<br/>
     * MagicXSign_Type.XSIGN_PKI_CERT_KM : 암호용 인증서
     * @param p_certType    인증서 타입
     * @return  ArrayList<Data_CertDetail>
     */
    private ArrayList<Data_CertDetail> getCertList(int p_certType) throws MagicXSign_Exception {
        LogPrinter.CF_debug("!----------------------------------------------------------");
        LogPrinter.CF_debug("!-- XSignHelper.getCertList()");
        LogPrinter.CF_debug("!----------------------------------------------------------");

        ArrayList<Data_CertDetail> tmp_arrCert = new ArrayList<>();

        Data_CertDetail tmp_certData;
        byte[] tmp_binCert;
        int tmp_count = 0;

        try {
            mMagicXSign.MEDIA_Load(MagicXSign_Type.XSIGN_PKI_TYPE_NPKI, MagicXSign_Type.XSIGN_PKI_CERT_TYPE_USER, p_certType, MagicXSign_Type.XSIGN_PKI_MEDIA_TYPE_DISK, mRootPath);
            tmp_count = mMagicXSign.MEDIA_GetCertCount();
            LogPrinter.CF_debug("!---- 인증서 목록 갯수(DB) : " + tmp_count);
        } catch (MagicXSign_Exception e) {
            throw  e;
        }

        for( int i = 0 ; i < tmp_count; i++){
            try {
                tmp_binCert = mMagicXSign.MEDIA_ReadCert(i, p_certType, null);
                tmp_certData = new Data_CertDetail();

                // 인증서 사용용도 설정
                tmp_certData.setOID(mMagicXSign.CERT_GetAttribute(tmp_binCert, MagicXSign_Type.XSIGN_CERT_ATTR_POLICY_ID,true));
                tmp_certData.setOID_Readable(XSignCertPolicy.parseOID(tmp_certData.getOID()));

                // 인증서 사용목적
                tmp_certData.setKeyUsage(mMagicXSign.CERT_GetAttribute(tmp_binCert, MagicXSign_Type.XSIGN_CERT_ATTR_KEY_USAGE,true));

                // 인증서 사용자 이름 설정
                tmp_certData.setSubjectDN(mMagicXSign.CERT_GetAttribute(tmp_binCert, MagicXSign_Type.XSIGN_CERT_ATTR_SUBJECT_DN,true));
                tmp_certData.setUSER(XSignCertPolicy.parserUserName(tmp_certData.getSubjectDN()));
                tmp_certData.setUSER_Name(mMagicXSign.CERT_GetAttribute(tmp_binCert, MagicXSign_Type.XSIGN_CERT_ATTR_REAL_NAME,true));

                // 인증서 유효기간 설정
                tmp_certData.setExpirationFrom(mMagicXSign.CERT_GetAttribute(tmp_binCert, MagicXSign_Type.XSIGN_CERT_ATTR_EXPIRATION_FROM,true));
                tmp_certData.setExpirationTo(mMagicXSign.CERT_GetAttribute(tmp_binCert, MagicXSign_Type.XSIGN_CERT_ATTR_EXPIRATION_TO,true));

                // 인증서 발급기관 설정
                tmp_certData.setCA(mMagicXSign.CERT_GetAttribute(tmp_binCert, MagicXSign_Type.XSIGN_CERT_ATTR_ISSUER_DN, true));
                tmp_certData.setCA_Readable(XSignCertPolicy.parseISSUER(tmp_certData.getCA()));
                tmp_certData.setIndex(i);

                tmp_arrCert.add(tmp_certData);
            } catch (MagicXSign_Exception e) {
                throw e;
            }
        }

        mMagicXSign.MEDIA_UnLoad();

        return tmp_arrCert;
    }

    /**
     * 인증서 저장
     * <pre>
     * - API30 이슈로 인하여 외장메모리 저장기능은 사용하지 않고, 내장메모리(DB)에만 저장한다.
     * - 실제 인증서 가져오기는 라온솔루션을 이용하기에 외장메모리 저장이 필요 없음
     * </pre>
     * @param p_certType    인증서 타입 서명용 or 암호용
     * @param p_binCert     인증서
     * @param p_binPrikey   개인키
     * @return tmp_flagInsert   인증서 저장성공 여부 (true:성공)
     */
    public boolean CF_insert(int p_certType, byte[] p_binCert, byte[] p_binPrikey){
        boolean tmp_flagInsert = true;

        try {
            // 1. 인증서 리스트 구성
            mMagicXSign.MEDIA_Load(MagicXSign_Type.XSIGN_PKI_TYPE_NPKI, MagicXSign_Type.XSIGN_PKI_CERT_TYPE_USER, p_certType, MagicXSign_Type.XSIGN_PKI_MEDIA_TYPE_ALL, mRootPath);

            LogPrinter.CF_debug("!---- 인증서 갯수 : " + mMagicXSign.MEDIA_GetCertCount());

            // 2. 내장 디스크에 저장하도록 한다.
            // 인증서를 넣은 후에는 Xsign.MEDIA_ReLoad()를 하거나, Xsign.MEDIA_UnLoad() -> Xsign.MEDIA_Load() 를 통해 인증서 목록을 재구성한다.
            mMagicXSign.MEDIA_WriteCertAndPriKey(p_binCert, p_binPrikey, MagicXSign_Type.XSIGN_PKI_MEDIA_TYPE_DISK);

        } catch (MagicXSign_Exception e) {
            LogPrinter.CF_debug(mContext.getResources().getString(R.string.log_fail_write_xsign)+ " : " + e.getMessage());
            tmp_flagInsert = false;
        }
        catch (Exception e){
            tmp_flagInsert = false;
            LogPrinter.CF_debug(mContext.getResources().getString(R.string.log_fail_write_xsign) + " : " + e.getMessage());
        }

        try {
            // 3. 인증서 리스트 해제
            mMagicXSign.MEDIA_UnLoad();
        } catch (MagicXSign_Exception e) {
            LogPrinter.CF_line();
            LogPrinter.CF_debug(mContext.getResources().getString(R.string.log_fail_unload_xsign));
        }

        return tmp_flagInsert;
    }

    /**
     * 인증서를 이용해 서명데이터 생성하여 반환<br/>
     * subjectDN 값을 이용해 인증서의 index를 찾아 처리한다.
     * @param p_certType    인증서 타입 (서명용 암호용)
     * @param p_subjectDn   주체자
     * @param p_plainText   평문
     * @param p_password    인증서 암호
     * @return   byte[]
     * @throws MagicXSign_Exception 인증서 에러
     */
    @SuppressWarnings("unused")
    public byte[] CF_certSign(int p_certType, String p_subjectDn, String p_plainText, String p_password) throws MagicXSign_Exception {
        byte[] tmp_binSignData;
        int tmp_count = 0;
        int tmp_index = -1;

        try {
            // 1. 인증서 목록 구성
            mMagicXSign.MEDIA_Load(MagicXSign_Type.XSIGN_PKI_TYPE_NPKI, MagicXSign_Type.XSIGN_PKI_CERT_TYPE_USER, MagicXSign_Type.XSIGN_PKI_CERT_SIGN, MagicXSign_Type.XSIGN_PKI_MEDIA_TYPE_ALL, mRootPath);

            // 2. 인증서 갯수 확인
            tmp_count = mMagicXSign.MEDIA_GetCertCount();

            // 3. 인증서 index 구함
            for( int i = 0 ; i < tmp_count; i++) {
                byte[] tmp_binCert = mMagicXSign.MEDIA_ReadCert(i, p_certType, null);
                String tmp_dn = mMagicXSign.CERT_GetAttribute(tmp_binCert, MagicXSign_Type.XSIGN_CERT_ATTR_SUBJECT_DN,true);

                if(tmp_dn.equals(p_subjectDn)){
                    tmp_index = i;
                    break;
                }
            }

            if (tmp_index <= 0) {
                throw new MagicXSign_Exception(mContext.getResources().getString(R.string.exception_not_exist), MagicXSign_Err.ERR_READ_CERT);
            }

            // 4. 인증서를 이용해 서명
            tmp_binSignData = mMagicXSign.CMS_SignData(MagicXSign_Type.XSIGN_PKI_OPT_NONE, tmp_index, p_password.getBytes(), p_plainText.getBytes());

            // 5. 서명된 데이터 검증
            byte[] tmp_binVerifyData = mMagicXSign.CMS_VerifyData(MagicXSign_Type.XSIGN_PKI_OPT_NONE, tmp_binSignData);

            // 6. 원본 메시지와 비교
            if (p_plainText.compareTo(new String(tmp_binVerifyData)) != 0) {
                throw new MagicXSign_Exception(mContext.getResources().getString(R.string.exception_failed_verify), MagicXSign_Err.ERR_VERIFY_SIGNATURE);
            }
        }catch (MagicXSign_Exception e){
            throw e;
        }finally {

            // 7. 인증서 리스트 해제제
            try {
                mMagicXSign.MEDIA_UnLoad();
            }catch (MagicXSign_Exception e){
                throw e;
            }
        }

        return tmp_binSignData;
    }

    /**
     * 인증서를 이용해 서명 데이터를 생성하여 반환
     * @param p_certIndex   선택한 인증서의 index
     * @param p_plainText   평문
     * @param p_password    인증서 비밀번호
     * @return   byte[]
     */
    public byte[] CF_certSign(int p_certIndex, String p_plainText, byte[] p_password) throws MagicXSign_Exception {
        byte[] tmp_binSignData;
        int tmp_count = 0;

        try {
            // 1. 인증서 리스트 구성
            mMagicXSign.MEDIA_Load(MagicXSign_Type.XSIGN_PKI_TYPE_NPKI, MagicXSign_Type.XSIGN_PKI_CERT_TYPE_USER, MagicXSign_Type.XSIGN_PKI_CERT_SIGN, MagicXSign_Type.XSIGN_PKI_MEDIA_TYPE_ALL, mRootPath);

            // 2. 인증서 갯수 확인
            tmp_count = mMagicXSign.MEDIA_GetCertCount();

            if (tmp_count <= 0) {
                throw new MagicXSign_Exception(mContext.getResources().getString(R.string.exception_not_exist), MagicXSign_Err.ERR_READ_CERT);
            }

            // 3. 인증서를 이용해 서명
            tmp_binSignData = mMagicXSign.CMS_SignData(MagicXSign_Type.XSIGN_PKI_OPT_NONE, p_certIndex, p_password, p_plainText.getBytes());

            // 4. 서명된 데이터 검증
            byte[] tmp_binVerifyData = mMagicXSign.CMS_VerifyData(MagicXSign_Type.XSIGN_PKI_OPT_NONE, tmp_binSignData);

            // 5. 원본 메시지와 비교
            if (p_plainText.compareTo(new String(tmp_binVerifyData)) != 0) {
                throw new MagicXSign_Exception(mContext.getResources().getString(R.string.exception_failed_verify), MagicXSign_Err.ERR_VERIFY_SIGNATURE);
            }
        }catch (MagicXSign_Exception e){
            throw e;
        }finally {

            // 6. 인증서 리스트 해제
            try {
                mMagicXSign.MEDIA_UnLoad();
            } catch (MagicXSign_Exception e) {
                LogPrinter.CF_line();
                LogPrinter.CF_debug(mContext.getResources().getString(R.string.log_fail_unload_xsign));
            }
        }

        return tmp_binSignData;
    }

    /**
     * 인증서 비밀번호 변경<br/>
     * @param p_certIndex   인증서 인덱스
     * @param p_oldPassword 구 암호
     * @param p_newPassword 신 암호
     * @return  boolean 암호 변경 여부, 암호 변경 시 true 반환
     * @throws MagicXSign_Exception 인증서 에러
     */
    @SuppressWarnings("unused")
    public boolean CF_changePassword(int p_certIndex, String p_oldPassword, String p_newPassword) throws MagicXSign_Exception {
        int tmp_count = 0;
        boolean tmp_flagChange = false;
        int tmp_mediaType[] = new int[1];

        try{
            // 1. 인증서 목록 구성
            mMagicXSign.MEDIA_Load(MagicXSign_Type.XSIGN_PKI_TYPE_NPKI, MagicXSign_Type.XSIGN_PKI_CERT_TYPE_USER, MagicXSign_Type.XSIGN_PKI_CERT_SIGN, MagicXSign_Type.XSIGN_PKI_MEDIA_TYPE_ALL, mRootPath);

            //2. 인증서 갯수를 구한다.
            tmp_count = mMagicXSign.MEDIA_GetCertCount();
            if( tmp_count <= 0 )
                throw new MagicXSign_Exception(mContext.getResources().getString(R.string.exception_not_exist), MagicXSign_Err.ERR_READ_CERT);

            //3. 해당 인덱스의 인증서 개인키 비밀번호를 변경한다.
            tmp_flagChange = mMagicXSign.MEDIA_ChangePassword( p_certIndex, p_oldPassword.getBytes(), p_newPassword.getBytes());
        }
        catch( MagicXSign_Exception e ) {
            throw e;
        } finally {
            //4. 인증서 리스트를 해제한다.
            mMagicXSign.MEDIA_UnLoad();

        }

    	return tmp_flagChange;
    }


    /**
     * 인증서 삭제
     * @param p_certIndex 인증서의 인덱스
     * @return  boolean
     * @throws  MagicXSign_Exception 인증서 에러
     */
    public boolean CF_delete(int p_certIndex) throws MagicXSign_Exception {
        int tmp_count = 0;
        boolean tmp_flagDelete = false;

        try {
            mMagicXSign.MEDIA_Load(MagicXSign_Type.XSIGN_PKI_TYPE_NPKI, MagicXSign_Type.XSIGN_PKI_CERT_TYPE_USER, MagicXSign_Type.XSIGN_PKI_CERT_SIGN, MagicXSign_Type.XSIGN_PKI_MEDIA_TYPE_DISK, mRootPath);
            //2. 인증서 갯수를 구한다.
            tmp_count = mMagicXSign.MEDIA_GetCertCount();
            LogPrinter.CF_debug("!---- 인증서 목록 갯수(ALL) : " + tmp_count);
            if (tmp_count <= 0)
                throw new MagicXSign_Exception(mContext.getResources().getString(R.string.exception_not_exist), MagicXSign_Err.ERR_READ_CERT);

            //3. 해당 인덱스의 인증서를 삭제한다.
            tmp_flagDelete = mMagicXSign.MEDIA_DeleteCertificate(p_certIndex);
            mMagicXSign.MEDIA_ReLoad();
        }catch(MagicXSign_Exception e){
            throw e;
        }
        finally {
            mMagicXSign.MEDIA_UnLoad();
        }

        return tmp_flagDelete;
    }

    /**
     * SEED 알고리즘을 이용한 암호화
     * @param p_plainData   암호화할 평문
     * @return  암호화된 StringBuffer
     * @throws MagicXSign_Exception
     */
    @SuppressWarnings("unused")
    public StringBuffer CF_encrypt(String p_plainData) throws MagicXSign_Exception {
        byte[] tmp_binKey = null;
        byte[] tmp_binIV = null;
        byte[] tmp_binEncryptData = null;
        StringBuffer tmp_encryptBuffer = new StringBuffer();

        try{
            //1. SEED 알고리즘의 Key, IV 값을 생성한다.
            mMagicXSign.CRYPTO_GenKeyAndIV("SEED");

            //2. 해당 Key값과 IV 값을 가져온다.
            tmp_binKey = mMagicXSign.CRYPTO_GetKey();
            tmp_binIV = mMagicXSign.CRYPTO_GetIV();

            //3. 설정된 Key, IV 값을 이용하여 대칭키 암호화를 한다.
            tmp_binEncryptData = mMagicXSign.CRYPTO_Encrypt(p_plainData.getBytes());

            tmp_encryptBuffer.append(XSignCertPolicy.ByteToHex(tmp_binEncryptData));

        } catch( MagicXSign_Exception e ) {
            throw e;
        }

        return tmp_encryptBuffer;
    }

    /**
     * SEED 알고리즘을 이용한 복호화
     * @param p_key     복호화에 사용할 key, 암호화에 사용한 값과 동일해야 한다.
     * @param p_IV      복호화에 사용할 IV, 암호화에 사용한 값과 동일해야 한다.
     * @return  암호화된 StringBuffer
     * @throws MagicXSign_Exception
     */
    @SuppressWarnings("unused")
    public StringBuffer CF_decrypt(byte[] p_key, byte[] p_IV) throws MagicXSign_Exception {
        byte[] tmp_binDecryptData = null;
        StringBuffer tmp_decryptBuffer = new StringBuffer();

        try {
            mMagicXSign.CRYPTO_SetKeyAndIV("SEED", p_key, p_IV);

            tmp_binDecryptData = mMagicXSign.CRYPTO_Decrypt(tmp_binDecryptData);

            if(tmp_binDecryptData != null){
                tmp_decryptBuffer.append(new String(tmp_decryptBuffer));
            }

        }catch (MagicXSign_Exception e){
            throw e;
        }

        return  tmp_decryptBuffer;
    }

    /**
     * HASH 생성
     * @param p_plainData 평문
     * @return 생성한 Hash
     * @throws MagicXSign_Exception
     */
    @SuppressWarnings("unused")
    public byte[] CF_createHash(String p_plainData) throws MagicXSign_Exception {
        byte[] tmp_binHashData = null;

        try {
            tmp_binHashData = mMagicXSign.CRYPTO_Hash("SHA1", p_plainData.getBytes());
        }catch (MagicXSign_Exception e){
            throw e;
        }
        return tmp_binHashData;
    }

    /**
     * BASE 64 Encode
     * @param p_binData 데이터
     * @return BASE 64 인코딩된 문자열
     * @throws MagicXSign_Exception
     */
    public String CF_encodeBase64(byte[] p_binData) throws MagicXSign_Exception {
        String tmp_encodeData = "";

        try{
            tmp_encodeData = mMagicXSign.BASE64_Encode(p_binData);
        }catch (MagicXSign_Exception e){
            throw e;
        }

        return tmp_encodeData;
    }

    /**
     * BASE 64 Decode
     * @param p_data
     * @return BASE_64 디코딩된 byte[]
     * @throws MagicXSign_Exception
     */
    @SuppressWarnings("unused")
    public byte[] CF_decodeBase64(String p_data) throws MagicXSign_Exception {
        byte[] tmp_binDecodeData = null;

        try {
            tmp_binDecodeData = mMagicXSign.BASE64_Decode(p_data);
        }catch (MagicXSign_Exception e){
            throw e;
        }

        return tmp_binDecodeData;
    }

    /**
     * VID 랜덤값 추출 함수
     * @param p_certIndex   인증서 인덱스
     * @param p_password 인증서 암호
     * @return VID 랜덤값 byte[]
     * @throws MagicXSign_Exception 인증서 에러
     */
    public byte[] CF_getVIDRandom(int p_certIndex,  byte[] p_password) throws MagicXSign_Exception {

        byte[] tmp_binKey = null;
        byte[] tmp_vidRandom = null;
        int tmp_count = 0;

        try{

            //1. 인증서 리스트를 구성한다.
            mMagicXSign.MEDIA_Load(MagicXSign_Type.XSIGN_PKI_TYPE_NPKI, MagicXSign_Type.XSIGN_PKI_CERT_TYPE_USER, MagicXSign_Type.XSIGN_PKI_CERT_SIGN, MagicXSign_Type.XSIGN_PKI_MEDIA_TYPE_ALL, mRootPath );

            //2. 인증서 갯수를 구한다.
            tmp_count = mMagicXSign.MEDIA_GetCertCount();
            if( tmp_count <= 0 )
                throw new MagicXSign_Exception("No Certificate List Item");

            //3. 해당 인덱스의 개인키 binary를 구한다.
            tmp_binKey = mMagicXSign.MEDIA_ReadPriKey(p_certIndex, MagicXSign_Type.XSIGN_PKI_CERT_SIGN);

            //4. 해당 인덱스 인증서 VID 랜덤값 추출
            tmp_vidRandom = mMagicXSign.VID_GetRandom(tmp_binKey, p_password);


        } catch( MagicXSign_Exception e ) {
            throw e;
        } finally {
            //5. 인증서 리스트를 해제한다.
            mMagicXSign.MEDIA_UnLoad();
        }
        return tmp_vidRandom;
    }

    /**
     * 인증서에서 추출한 VID(주민번호) 검증 수행
     * @param p_certIndex   인증서 index
     * @param p_IDN IDN
     * @param p_passWord 패스워드
     * @return  boolean
     * @throws MagicXSign_Exception
     */
    @SuppressWarnings("unused")
    public boolean CF_checkVID(int p_certIndex, String p_IDN, String p_passWord) throws MagicXSign_Exception {

        byte[] tmp_binCert = null;
        byte[] tmp_binKey = null;
        int tmp_count = 0;
        int tmp_mediaType[] = new int[1];
        boolean tmp_flagVerity = false;

        try {
            //1. 인증서 리스트를 구성한다.
            mMagicXSign.MEDIA_Load( MagicXSign_Type.XSIGN_PKI_TYPE_NPKI, MagicXSign_Type.XSIGN_PKI_CERT_TYPE_USER, MagicXSign_Type.XSIGN_PKI_CERT_SIGN, MagicXSign_Type.XSIGN_PKI_MEDIA_TYPE_ALL, mRootPath );

            //2. 인증서의 갯수를 가져온다.
            tmp_count = mMagicXSign.MEDIA_GetCertCount();
            if( tmp_count <= 0 )
                throw new MagicXSign_Exception("No Certificate List Item");

            //3. 해당 인덱스의 인증서 binary를 가져온다.
            tmp_binCert = mMagicXSign.MEDIA_ReadCert( p_certIndex, MagicXSign_Type.XSIGN_PKI_CERT_SIGN, tmp_mediaType );

            //4. 해당 인덱스의 인증서 개인키 binary를 가져온다.
            tmp_binKey = mMagicXSign.MEDIA_ReadPriKey( p_certIndex, MagicXSign_Type.XSIGN_PKI_CERT_SIGN );

            //5. VID 검증을 한다.
            // String 형의 경우 메모리에 남기 때문에 BYTE[] 형을 사용해야 한다
            tmp_flagVerity = mMagicXSign.VID_Verify(tmp_binCert, tmp_binKey, p_passWord.getBytes(), p_IDN.getBytes());
        }catch (MagicXSign_Exception e){
            throw e;
        }finally {
            mMagicXSign.MEDIA_UnLoad();
        }
        return tmp_flagVerity;
    }

    /**
     * 인증서를 이용하여 서명 데이터를 생성하고, 생성한 서명 데이터 검증
     * @param p_certIndex   int
     * @param p_plainData   String
     * @param p_password    String
     * @return  byte[]
     * @throws MagicXSign_Exception
     */
    @SuppressWarnings("unused")
    public byte[] CF_cryptoSignVerify(int p_certIndex, String p_plainData, String p_password) throws MagicXSign_Exception {
        byte[] tmp_binCert      = null;
        byte[] tmp_binKey       = null;
        byte[] tmp_binSignature = null;
        boolean tmp_flagVerity        = false;
        int tmp_count      = 0;

        try{

            //1. 인증서 리스트를 구성한다.
            mMagicXSign.MEDIA_Load(MagicXSign_Type.XSIGN_PKI_TYPE_NPKI, MagicXSign_Type.XSIGN_PKI_CERT_TYPE_USER, MagicXSign_Type.XSIGN_PKI_CERT_SIGN, MagicXSign_Type.XSIGN_PKI_MEDIA_TYPE_REMOVABLE, mRootPath );

            //2. 인증서 갯수를 구한다.
            tmp_count = mMagicXSign.MEDIA_GetCertCount();
            if( tmp_count <= 0 )
                throw new MagicXSign_Exception("No Certificate List Item");

            //3. 해당 인덱스의 인증서 binary를 구한다.
            tmp_binCert = mMagicXSign.MEDIA_ReadCert(p_certIndex, MagicXSign_Type.XSIGN_PKI_CERT_SIGN, null);

            //4. 해당 인덱스의 개인키 binary를 구한다.
            tmp_binKey = mMagicXSign.MEDIA_ReadPriKey(p_certIndex, MagicXSign_Type.XSIGN_PKI_CERT_SIGN);

            //5. CRYPTO 서명을 한다.
            tmp_binSignature = mMagicXSign.CRYPTO_SignData(tmp_binCert, tmp_binKey, p_password, p_plainData.getBytes());

            //6. CRYPTO 서명 검증을 한다.
            tmp_flagVerity = mMagicXSign.CRYPTO_VerifyData(tmp_binCert, tmp_binKey, p_password, p_plainData.getBytes(), tmp_binSignature);
            if(!tmp_flagVerity)
                throw new MagicXSign_Exception(mContext.getResources().getString(R.string.exception_failed_verify), MagicXSign_Err.ERR_VERIFY_SIGNATURE);

        } catch( MagicXSign_Exception e ) {
            throw e;
        } finally {
            //7. 인증서 리스트를 해제한다.
            mMagicXSign.MEDIA_UnLoad();
        }
        return tmp_binSignature;
    }

    /**
     * 내장메모리 인증서 갯수 조회
     * @return int  rtnCnt 내장메모리 인증서 갯수
     * @throws MagicXSign_Exception 공동인증서 예외
     */
    public int CF_getDbCertCnt() throws MagicXSign_Exception {
        LogPrinter.CF_debug("!----------------------------------------------------------");
        LogPrinter.CF_debug("!-- XSignHelper.CF_getCnt()");
        LogPrinter.CF_debug("!----------------------------------------------------------");
        mMagicXSign.MEDIA_Load(MagicXSign_Type.XSIGN_PKI_TYPE_NPKI, MagicXSign_Type.XSIGN_PKI_CERT_TYPE_USER, MagicXSign_Type.XSIGN_PKI_CERT_SIGN, MagicXSign_Type.XSIGN_PKI_MEDIA_TYPE_DISK, mRootPath );
        int rtnCnt = mMagicXSign.MEDIA_GetCertCount();
        LogPrinter.CF_debug("!---- 인증서 목록 갯수(DB) : " + rtnCnt );
        mMagicXSign.MEDIA_UnLoad();

        return rtnCnt;
    }

    /**
     * API 30 이슈로 인한 임시로 외장메모리 -> 내장메모리로 복사
     * @throws MagicXSign_Exception 공동인증서 예외
     */
    public void CF_moveCert() throws MagicXSign_Exception {
        LogPrinter.CF_debug("!----------------------------------------------------------");
        LogPrinter.CF_debug("!-- XSignHelper.CF_moveCert()");
        LogPrinter.CF_debug("!----------------------------------------------------------");

        byte[] tmp_binCert      = null;
        byte[] tmp_binKey       = null;

        // 외장메모리 인증서 갯수 조회
        mMagicXSign.MEDIA_Load(MagicXSign_Type.XSIGN_PKI_TYPE_NPKI, MagicXSign_Type.XSIGN_PKI_CERT_TYPE_USER, MagicXSign_Type.XSIGN_PKI_CERT_SIGN, MagicXSign_Type.XSIGN_PKI_MEDIA_TYPE_REMOVABLE, mRootPath );
        int tmp_count = mMagicXSign.MEDIA_GetCertCount();
        LogPrinter.CF_debug("!---- 인증서 목록 갯수(SD) : " + tmp_count);
        mMagicXSign.MEDIA_UnLoad();

        if( tmp_count <= 0 )
            throw new MagicXSign_Exception("No Certificate List Item");

        for(int idx=0; idx < tmp_count; idx++) {

            //1. 인증서 리스트를 구성한다.
            mMagicXSign.MEDIA_Load(MagicXSign_Type.XSIGN_PKI_TYPE_NPKI, MagicXSign_Type.XSIGN_PKI_CERT_TYPE_USER, MagicXSign_Type.XSIGN_PKI_CERT_SIGN, MagicXSign_Type.XSIGN_PKI_MEDIA_TYPE_REMOVABLE, mRootPath );

            //2. 해당 인덱스의 인증서 binary를 구한다.
            tmp_binCert = mMagicXSign.MEDIA_ReadCert(idx, MagicXSign_Type.XSIGN_PKI_CERT_SIGN, null);

            //3. 해당 인덱스의 개인키 binary를 구한다.
            tmp_binKey = mMagicXSign.MEDIA_ReadPriKey(idx, MagicXSign_Type.XSIGN_PKI_CERT_SIGN);

            //4. 인증서리스트 해제
            mMagicXSign.MEDIA_UnLoad();

            //5. 인증서 리스트를 구성한다.
            mMagicXSign.MEDIA_Load(MagicXSign_Type.XSIGN_PKI_TYPE_NPKI, MagicXSign_Type.XSIGN_PKI_CERT_TYPE_USER, MagicXSign_Type.XSIGN_PKI_CERT_SIGN, MagicXSign_Type.XSIGN_PKI_MEDIA_TYPE_DISK, mRootPath );

            //6. DB insert
            LogPrinter.CF_debug("!---- 인증서 내장메모리 이동(" + (idx+1) + "/" + tmp_count + ") : " +  mMagicXSign.MEDIA_WriteCertAndPriKey(tmp_binCert, tmp_binKey, MagicXSign_Type.XSIGN_PKI_MEDIA_TYPE_DISK));

            //7. 인증서리스트 해제
            mMagicXSign.MEDIA_UnLoad();
        }
    }
}