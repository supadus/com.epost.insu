package com.epost.insu.service;

/**
 * @copyright : 우정사업정보센터
 *
 * @project   : 모바일슈랑스 구축
 * @pakage   : com.epost.insu.service
 * @fileName  : AES256Util.java
 *
 * @Title     : AES256 암복호화 처리
 * @author    : 이경민
 * @created   : 2018-07-23
 * @version   : 1.0
 *
 * @note      : <u>AES256 암복호화 처리</u><br/>
 *               키생성, 암복호화 기능 제공</br>
 *
 * ======================================================================
 * 수정 내역
 * NO      날짜          작업자       내용
 * 01      2018-07-23    이경민       최초 등록
 * =======================================================================
 */

import java.io.UnsupportedEncodingException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import com.sun.org.apache.xml.internal.security.exceptions.Base64DecodingException;
import com.sun.org.apache.xml.internal.security.utils.Base64;



public class AES256Util {

    private String iv;
    private Key keySpec;

    public AES256Util(String key) throws UnsupportedEncodingException{


        this.iv = key.substring(0, 16);

        byte[] keyBytes = new byte[16];
        byte[] b =key.getBytes("UTF-8");
        int len = b.length;
        if (len > keyBytes.length)
            len = keyBytes.length;
        System.arraycopy(b, 0, keyBytes, 0, len);
        SecretKeySpec keySpec = new SecretKeySpec(keyBytes, "AES");

        this.keySpec = keySpec;
    }

    public String aesEncode(String str) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, InvalidAlgorithmParameterException, IllegalBlockSizeException, BadPaddingException, UnsupportedEncodingException {
        Cipher c = Cipher.getInstance("AES/CBC/PKCS5Padding");
        c.init(Cipher.ENCRYPT_MODE, keySpec, new IvParameterSpec(iv.getBytes()));

        byte[] encrypted = c.doFinal(str.getBytes("UTF-8"));
        String enStr = new String(Base64.encode(encrypted));

        return enStr;
    }

    public String aesDecode(String str) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, InvalidAlgorithmParameterException, Base64DecodingException, UnsupportedEncodingException, IllegalBlockSizeException, BadPaddingException {
        Cipher c = Cipher.getInstance("AES/CBC/PKCS5Padding");
        c.init(Cipher.DECRYPT_MODE, keySpec, new IvParameterSpec(iv.getBytes()));

        byte[] byteStr = Base64.decode(str.getBytes());

        return new String(c.doFinal(byteStr), "UTF-8");

    }

    public static byte[] generationAES_Key() throws NoSuchAlgorithmException{
        KeyGenerator kgen = KeyGenerator.getInstance("AES");
        kgen.init(128);
        SecretKey key = kgen.generateKey();
        return key.getEncoded();

    }

}
