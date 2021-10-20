package com.epost.insu.common;

import android.content.Context;
import android.os.Environment;

import com.dreamsecurity.dstoolkit.exception.DSToolkitException;
import com.dreamsecurity.dstoolkit.pkcs.Pkcs5;
import com.dreamsecurity.magicxsign.MagicXSign;
import com.dreamsecurity.magicxsign.MagicXSign_Exception;
import com.dreamsecurity.magicxsign.MagicXSign_Type;
import com.epost.insu.R;
import com.epost.insu.service.AES256Util;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

/**
 * 공동인증서 키 획득 헬퍼 클래스
 * @since     :
 * @version   :
 * @author    : LKM
 * <pre>
 *  공동인증서 키 획득 헬퍼 클래스
 *  1. 전자서명을 위한 공동인증
 *  2. 공동인증서 키 획득
 *  3. 공동인증서 개인키, 공개키 전송
 * ======================================================================
 * 0.0.0    LKM_20190330    최초 등록
 * 1.6.2    NJM_20210802    [2021년 대우 취약점] 2차본 : 부적절한 예외 처리
 * 1.6.3    NJM_20210831    [2021년 모의해킹취약점 2차] 부적절한 예외 처리
 * =======================================================================
 * copyright : (주)프로니스정보기술 | http://www.phronis.co.kr
 * </pre>
 */
public class CertKeyHelper {
    private final String mRootPath = Environment.getExternalStorageDirectory().getPath();
    private MagicXSign mMagicXSign;
    private Context mContext;
    private PrivateKey privateKey;
    private PublicKey publicKey;
    private KeyPair pair;
    private AES256Util aes256Util;
    private final String symmetricKey = "1533kwhnhf332efdk3jgdwoekfddso39";

    /**
     * 생성자
     *
     * @param p_context    Context
     * @param p_debugLevel 0 : 아무것도 안남김 , 100 모두 , 200 Warning 이상, 300 Error 이상
     */
    public CertKeyHelper(Context p_context, int p_debugLevel) {

        mContext = p_context;
        mMagicXSign = new MagicXSign();
        try {
            mMagicXSign.Init(mContext, p_debugLevel);
        } catch (MagicXSign_Exception e) {
            LogPrinter.CF_debug(mContext.getResources().getString(R.string.log_fail_init_xsign));
        }
        try {
            aes256Util = new AES256Util(symmetricKey);
        } catch (UnsupportedEncodingException e) {
            e.getMessage();
        }
    }

    /**
     * Generate public/private keys from certificate
     *
     * @param p_index      int
     * @param p_passPhrase byte[]
     * @throws MagicXSign_Exception, NoSuchAlgorithmException, InvalidKeySpecException, NoSuchProviderException, NoSuchPaddingException, InvalidAlgorithmParameterException, InvalidKeyException, BadPaddingException, IllegalBlockSizeException, DSToolkitException, IOException, CertificateException
     */
    public void generateKeyPair(int p_index, byte[] p_passPhrase) throws MagicXSign_Exception, NoSuchAlgorithmException, InvalidKeySpecException, NoSuchProviderException, NoSuchPaddingException, InvalidAlgorithmParameterException, InvalidKeyException, BadPaddingException, IllegalBlockSizeException, DSToolkitException, IOException, CertificateException {
        privateKey = getPrivateKey(p_index, p_passPhrase);
        publicKey = getPublicKey(p_index);
        pair = new KeyPair(publicKey, privateKey);
        return;
    }

    /**
     * Get public/private keys from certificate
     *
     * @return KeyPair
     */
    public KeyPair getKeyPair() {
        return pair;
    }

    /**
     * index에 따른 서명용 인증서 반환 함수<br/>
     * @param p_index int    인증서(Base64 encoding)
     * @return
     */
    public String getCertificate(int p_index) throws MagicXSign_Exception {
        byte[] tmp_binCert = mMagicXSign.MEDIA_ReadCert(p_index, MagicXSign_Type.XSIGN_PKI_CERT_SIGN, null);
        return base64EncodeToString(tmp_binCert);
    }

    /**
     * index에 따른 서명용 인증서 반환 함수<br/>
     * @param p_index int    인증서 만료일
     * @return
     */
    public String getExpireDate(int p_index) throws MagicXSign_Exception {
        byte[] tmp_binCert = mMagicXSign.MEDIA_ReadCert(p_index, MagicXSign_Type.XSIGN_PKI_CERT_SIGN, null);
        return mMagicXSign.CERT_GetAttribute(tmp_binCert, MagicXSign_Type.XSIGN_CERT_ATTR_EXPIRATION_TO,true);
    }

    /**
     * Get private key from certificate
     *
     * @param p_index      int
     * @param p_passPhrase byte[]
     * @return privateKey PrivateKey
     * @throws NoSuchAlgorithmException, InvalidKeySpecException
     */
    private PrivateKey getPrivateKey(int p_index, byte[] p_passPhrase) throws NoSuchAlgorithmException, InvalidKeySpecException {
        byte[] privateKeyBytes = null;
        Pkcs5 p = new Pkcs5();
        try {
            mMagicXSign.MEDIA_Load(MagicXSign_Type.XSIGN_PKI_TYPE_NPKI, MagicXSign_Type.XSIGN_PKI_CERT_TYPE_USER, MagicXSign_Type.XSIGN_PKI_CERT_SIGN, MagicXSign_Type.XSIGN_PKI_MEDIA_TYPE_REMOVABLE, mRootPath);
            byte[] tmp_binKey = mMagicXSign.MEDIA_ReadPriKey(p_index, MagicXSign_Type.XSIGN_PKI_CERT_SIGN);
            com.dreamsecurity.dstoolkit.crypto.PrivateKey tmp_key = p.decrypt(tmp_binKey, p_passPhrase);
            privateKeyBytes = tmp_key.getKey();
        } catch (NullPointerException e) {
            LogPrinter.CF_debug("!---- (NPE)");
            e.getMessage();
            return null;
        } catch (Exception e) {
            LogPrinter.CF_debug(e.getMessage());
            return null;
        }

        KeyFactory fact = KeyFactory.getInstance("RSA");
        return fact.generatePrivate(new PKCS8EncodedKeySpec(privateKeyBytes));
    }

    /**
     * Get public key from certificate
     *
     * @param p_index int
     * @return publicKey PublicKey
     * @throws IOException, CertificateException, MagicXSign_Exception
     */
    private PublicKey getPublicKey(int p_index) throws IOException, CertificateException, MagicXSign_Exception {
        X509Certificate cert = null;
        FileInputStream fis = null;
        String path = getCertPath(p_index);

        try {
            fis = new FileInputStream(new File(path));
            CertificateFactory certificateFactory = CertificateFactory.getInstance("X509");
            cert = (X509Certificate) certificateFactory.generateCertificate(fis);
        } catch (NullPointerException e) {
            e.getMessage();
        } catch (Exception e) {
            e.getMessage();
        } finally {
            if (fis != null) try {
                fis.close();
            } catch (IOException ie) {
                ie.getMessage();
            }
        }
        return cert.getPublicKey();
    }

    /**
     * Get public key path from certificate
     *
     * @param p_index int
     * @return String
     * @throws MagicXSign_Exception
     */
    private String getCertPath(int p_index) throws MagicXSign_Exception {
        String[] paths = mMagicXSign.MEDIA_ReadCertFilePath(p_index);
        for (int i = 0; i < paths.length; i++) {
            if (paths[i] != null && paths[i].contains("signCert.der")) {
                return paths[i];
            }
        }
        return "";
    }

    /**
     * Encrypt plaintext
     *
     * @param msg        String
     * @param privateKey PrivateKey
     * @return cipherText byte[]
     * @throws InvalidKeyException, IllegalBlockSizeException, BadPaddingException
     */
    public byte[] encrypt(String msg, PrivateKey privateKey) throws InvalidKeyException, IllegalBlockSizeException, BadPaddingException, NoSuchAlgorithmException, NoSuchProviderException, NoSuchPaddingException {
        Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding", "BC");
        cipher.init(Cipher.ENCRYPT_MODE, privateKey);
        byte[] tempCipherText = cipher.doFinal(msg.getBytes());
        byte[] cipherText = new byte[tempCipherText.length];
        System.arraycopy(tempCipherText, 0, cipherText, 0, tempCipherText.length);
        return cipherText;
    }

    /**
     * Decrypt ciphertext
     *
     * @param cipherText byte[]
     * @param publicKey  PublicKey
     * @return plainText String
     * @throws InvalidKeyException, IllegalBlockSizeException, BadPaddingException
     */
    private String decrypt(byte[] cipherText, PublicKey publicKey) throws InvalidKeyException, IllegalBlockSizeException, BadPaddingException, NoSuchAlgorithmException, NoSuchProviderException, NoSuchPaddingException {
        Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding", "BC");
        cipher.init(Cipher.DECRYPT_MODE, publicKey);
        byte[] plainText = cipher.doFinal(cipherText);
        return new String(plainText);
    }

    /**
     * Cryptography test with key pair
     *
     * @param msg  String
     * @param pair KeyPair
     */
    private void cryptographyTest(String msg, KeyPair pair) throws BadPaddingException, NoSuchPaddingException, NoSuchAlgorithmException, IllegalBlockSizeException, NoSuchProviderException, InvalidKeyException {
        byte[] cipherText = encrypt(msg, pair.getPrivate());
        String plainText = decrypt(cipherText, pair.getPublic());
        return;
    }

    /**
     * Convert encoded byte key to public key
     *
     * @param encodedKey byte[]
     * @return PublicKey
     * @throws NoSuchAlgorithmException, InvalidKeySpecException
     */
    private PublicKey convertBytesToPublicKey(byte[] encodedKey) throws NoSuchAlgorithmException, InvalidKeySpecException {
        // encodedKey : publicKey.getEncoded();
        KeyFactory fact = KeyFactory.getInstance("RSA");
        return fact.generatePublic(new X509EncodedKeySpec(encodedKey));
    }

    /**
     * Convert encoded byte key to private key
     *
     * @param encodedKey byte[]
     * @return PrivateKey
     * @throws NoSuchAlgorithmException, InvalidKeySpecException
     */
    public PrivateKey convertBytesToPrivateKey(byte[] encodedKey) throws NoSuchAlgorithmException, InvalidKeySpecException {
        // encodedKey : publicKey.getEncoded();
        KeyFactory fact = KeyFactory.getInstance("RSA");
        return fact.generatePrivate(new PKCS8EncodedKeySpec(encodedKey));
    }

    /**
     * Encode byte array using base64
     *
     * @param bytes byte[]
     * @return String
     * @throws MagicXSign_Exception
     */
    public String base64EncodeToString(byte[] bytes) throws MagicXSign_Exception {
        return mMagicXSign.BASE64_Encode(bytes);
    }

    /**
     * Decode string using base64
     *
     * @param str String
     * @return byte[]
     * @throws MagicXSign_Exception
     */
    public byte[] base64Decode(String str) throws MagicXSign_Exception {
        return mMagicXSign.BASE64_Decode(str);
    }

    /**
     * Encode string using aes
     *
     * @param str String
     * @return aesEncodedString String
     */
    public String aesEncode(String str) {
        String aesEncodedString = "";
        try {
            aesEncodedString = aes256Util.aesEncode(str);
        } catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException | InvalidAlgorithmParameterException | IllegalBlockSizeException | BadPaddingException | UnsupportedEncodingException e) {
            e.getMessage();
        }
        return aesEncodedString;
    }

}