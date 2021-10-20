package com.epost.insu.network;

import android.os.Handler;
import android.util.Log;

import com.epost.insu.EnvConfig;
import com.epost.insu.common.CommonFunction;
import com.epost.insu.common.LogPrinter;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Http 통신 구현
 * @since     :
 * @version   : 1.1
 * @author    : LSH
 * <pre>
 *     {@link #sendPostData(String, String, Handler, int, int)}  : common 통신 함수
 * ======================================================================
 * 0.0.0    LSH_20170808    최초 등록
 * 1.6.2    NJM_20210802    [2021년 대우 취약점] 2차본 : 경쟁조건: 검사시점과 사용시점 (TOCTOU)
 * =======================================================================
 * copyright : (주)프로니스정보기술 | http://www.phronis.co.kr
 * </pre>
 */
public class HttpConnections {

    /**
     * Http 통신 post data 전송, 멀티파일 업로드
     * @param p_url             String, url
     * @param p_arrFileKey      ArrayList<String>, 업로드 파일키
     * @param p_arrFile         ArrayList<File>, 업로드 파일
     * @param p_strParams       HashMap<String,String>, param
     * @param p_successMsgId    int, 성공 message.what
     * @param p_failMsgId       int, 실패, message.what
     * @param p_handler         Handler, 결과수신 handler
     */
    public static void sendMultiData(final String                   p_url,
                                     final ArrayList<String>        p_arrFileKey,
                                     final ArrayList<File>          p_arrFile,
                                     final HashMap<String,String>   p_strParams,
                                     final int                      p_successMsgId,
                                     final int                      p_failMsgId,
                                     final Handler                  p_handler ){

        new Thread(new Runnable() {
            public void run() {
                synchronized (this) {
                    try {
                        URL url = new URL(p_url);

                        // ---------------------------------------------------
                        //	ResopnseCode 체크 변수 세팅
                        // ---------------------------------------------------
                        boolean flag_getResponseCode = false;
                        int tmp_responseCode = -1;

                        String lineEnd = "\r\n";
                        String twoHyphens = "--";
                        String boundary = "*****adva2vDFfdab2";

                        HttpURLConnection http = (HttpURLConnection) url.openConnection();

                        if (http != null) {
                            http.setConnectTimeout(EnvConfig.HTTP_CONNECT_TIMEOUT);
                            http.setReadTimeout(EnvConfig.HTTP_MILTIPART_TIMEOUT);
                            http.setUseCaches(false);
                            http.setDefaultUseCaches(false);
                            http.setDoInput(true);
                            http.setDoOutput(true);
                            http.setRequestMethod("POST");
                            //http.setRequestProperty("Transfer-Encoding","chunked");
                            http.setChunkedStreamingMode(EnvConfig.HTTP_CHUNKED_SIZE);
                            http.setRequestProperty("Connection", "Keep-Alive");
                            http.setRequestProperty("ENCTYPE", "multipart/form-data");
                            http.setRequestProperty("Content-Type", "multipart/form-data;boundary=" + boundary);

                            DataOutputStream tmp_dataOS = null;
                            InputStream tmp_inputStream = null;
                            InputStreamReader tmp_inputStreamReader = null;
                            BufferedReader tmp_reader = null;
                            FileInputStream tmp_fileIS = null;

                            try {
                                tmp_dataOS = new DataOutputStream(http.getOutputStream());

                                for (Map.Entry<String, String> entry : p_strParams.entrySet()) {
                                    tmp_dataOS.writeBytes(lineEnd + twoHyphens + boundary + lineEnd); //필드 구분자 시작
                                    tmp_dataOS.writeBytes("Content-Disposition: form-data; name=\"" + entry.getKey() + "\"" + lineEnd);
                                    tmp_dataOS.writeBytes(lineEnd);
                                    tmp_dataOS.write(entry.getValue().getBytes("UTF-8"));
                                    tmp_dataOS.writeBytes(lineEnd);
                                }

                                // ------------------------------------
                                //	이미지 전달
                                // ------------------------------------
                                int maxBufferSize = EnvConfig.HTTP_MAX_BUFFER_SIZE;
                                for (int k = 0; k < p_arrFileKey.size(); k++) {
                                    if (p_arrFile.get(k).exists()) {
                                        int bytesRead, bytesAvailable, bufferSize;
                                        byte[] buffer;

                                        tmp_fileIS = new FileInputStream(p_arrFile.get(k).getPath());

                                        Log.d("FILE" + k, "전송할파일");
                                        Log.d("FILE" + k, p_arrFile.get(k).getPath());

                                        tmp_dataOS.writeBytes(lineEnd + twoHyphens + boundary + lineEnd);
                                        tmp_dataOS.writeBytes("Content-Disposition: form-data; name=\"" + p_arrFileKey.get(k) + "\";filename=\"" + p_arrFile.get(k).getName() + "\"" + lineEnd);
                                        tmp_dataOS.writeBytes(lineEnd);

                                        // 버퍼 세팅
                                        bytesAvailable = tmp_fileIS.available();

                                        bufferSize = Math.min(bytesAvailable, maxBufferSize);
                                        buffer = new byte[bufferSize];

                                        // 파일을 읽고 쓰기
                                        bytesRead = tmp_fileIS.read(buffer, 0, bufferSize);
                                        while (bytesRead > 0) {
                                            tmp_dataOS.write(buffer, 0, bufferSize);
                                            bytesAvailable = tmp_fileIS.available();
                                            bufferSize = Math.min(bytesAvailable, maxBufferSize);
                                            bytesRead = tmp_fileIS.read(buffer, 0, bufferSize);
                                        }

                                        tmp_dataOS.writeBytes(lineEnd);

                                        tmp_fileIS.close();
                                        tmp_fileIS = null;
                                    }
                                }

                                tmp_dataOS.writeBytes(lineEnd + twoHyphens + boundary + twoHyphens + lineEnd);

                                tmp_dataOS.flush();
                                tmp_dataOS.close();
                                tmp_dataOS = null;

                                //http.connect();

                                tmp_responseCode = http.getResponseCode();

                                if (tmp_responseCode == HttpURLConnection.HTTP_OK) {
                                    flag_getResponseCode = true;

                                    // --------------------------------------------------
                                    // Stream 읽어 Data 세팅
                                    // --------------------------------------------------
                                    tmp_inputStream = http.getInputStream();
                                    tmp_inputStreamReader = new InputStreamReader(tmp_inputStream, "utf-8");

                                    tmp_reader = new BufferedReader(tmp_inputStreamReader);
                                    StringBuilder builder = new StringBuilder();
                                    String str = "";

                                    while ((str = tmp_reader.readLine()) != null) {
                                        builder.append(str);
                                    }


                                    // ---------------------------------------------------------------
                                    //  BOM (byte order mark) 제거
                                    // ---------------------------------------------------------------
                                    String strHttpData = CommonFunction.CF_removeBOM(builder.toString());

                                    tmp_reader.close();
                                    tmp_reader = null;
                                    tmp_inputStreamReader.close();
                                    tmp_inputStreamReader = null;
                                    tmp_inputStream.close();
                                    tmp_inputStream = null;

                                    if (p_handler != null) {
                                        p_handler.sendMessage(p_handler.obtainMessage(p_successMsgId, strHttpData));
                                    }
                                } else {
                                    flag_getResponseCode = true;

                                    if (p_handler != null) {
                                        p_handler.sendMessage(p_handler.obtainMessage(p_failMsgId, "서버에 연결할 수 없습니다.( code: " + http.getResponseCode() + ")"));
                                    }
                                }
                            } catch (IOException e) {
                                http.disconnect();
                            } finally {
                                if (tmp_dataOS != null)                  // Http OutputStream 해제
                                    tmp_dataOS.close();

                                if (tmp_inputStream != null)             // Http InputStream 해제
                                    tmp_inputStream.close();

                                if (tmp_inputStreamReader != null)       // Http InputStreamReader 해제
                                    tmp_inputStreamReader.close();

                                if (tmp_reader != null)                  // BufferReader 해제
                                    tmp_reader.close();

                                if (tmp_fileIS != null) {                 // FileInputStream 해제
                                    tmp_fileIS.close();
                                }

                                http.disconnect();
                            }
                        }

                        if (!flag_getResponseCode && tmp_responseCode == -1) {
                            LogPrinter.CF_debug("HTTP 소켓 응답 에러");
                            throw new IOException();
                        }

                    } catch (MalformedURLException e1) {
                        LogPrinter.CF_line();
                        LogPrinter.CF_debug("sendMultiData HTTP 서버 연결에 실패(1) : " + p_url);
                        p_handler.sendMessage(p_handler.obtainMessage(p_failMsgId, "서버 연결에 실패 하였습니다.\r\n관리자에게 문의 하세요.."));
                    } catch (IOException e) {
                        LogPrinter.CF_line();
                        LogPrinter.CF_debug("sendMultiData HTTP 통신 에러(2) : " + p_url);
                        e.getMessage();
                        p_handler.sendMessage(p_handler.obtainMessage(p_failMsgId, "통신 상태가 좋지 않습니다.\r\n확인 후 다시 시도해주세요."));
                    }
                }
            }
        }).start();
    }

    /**
     * Http 통신 post data 전송, 응답처리 없음.
     * @param p_url            String, url
     * @param p_strParam       String, param
     */
    public static void sendPostDataNoResponse(final String p_url, final String p_strParam){
        new Thread(new Runnable() {
            public void run() {
                synchronized (this) {
                    try {
                        URL url = new URL(p_url);

                        // ---------------------------------------------------
                        //	ResopnseCode 체크 변수 세팅
                        // ---------------------------------------------------
                        boolean flag_getResponseCode = false;
                        int tmp_responseCode = -1;

                        HttpURLConnection http = (HttpURLConnection) url.openConnection();

                        if (http != null) {

                            http.setConnectTimeout(EnvConfig.HTTP_CONNECT_TIMEOUT);
                            http.setReadTimeout(EnvConfig.HTTP_SEND_TIMEOUT);
                            http.setUseCaches(false);
                            http.setDefaultUseCaches(false);
                            http.setDoInput(true);
                            http.setDoOutput(true);
                            http.setRequestMethod("POST");
                            if (p_strParam != null) {
                                http.setFixedLengthStreamingMode(p_strParam.getBytes("utf-8").length);
                            }
                            http.setRequestProperty("content-type", "application/x-www-form-urlencoded");

                            // -------------------------------------
                            //	Param 전달
                            // -------------------------------------
                            OutputStream outStream = null;                      // http output stream
                            InputStream tmp_inputStream = null;                 // http input stream
                            BufferedReader tmp_reader = null;                   // 버퍼리더
                            InputStreamReader tmp_inputStreamReader = null;     // http input stream 리더
                            try {
                                if (p_strParam != null) {
                                    outStream = http.getOutputStream();
                                    outStream.write(p_strParam.getBytes("utf-8"));
                                    outStream.flush();
                                    outStream.close();
                                    outStream = null;
                                }

                                tmp_responseCode = http.getResponseCode();

                                if (tmp_responseCode == HttpURLConnection.HTTP_OK) {
                                    flag_getResponseCode = true;

                                    // --------------------------------------------------
                                    // Stream 읽어 Data 세팅
                                    // --------------------------------------------------
                                    tmp_inputStream = http.getInputStream();
                                    tmp_inputStreamReader = new InputStreamReader(tmp_inputStream, "utf-8");

                                    tmp_reader = new BufferedReader(tmp_inputStreamReader);
                                    StringBuilder builder = new StringBuilder();
                                    String str = "";

                                    while ((str = tmp_reader.readLine()) != null) {
                                        builder.append(str);
                                    }


                                    // ---------------------------------------------------------------
                                    //  BOM (byte of mark) 제거
                                    // ---------------------------------------------------------------
                                    String strHttpData = CommonFunction.CF_removeBOM(builder.toString());

                                    tmp_reader.close();
                                    tmp_reader = null;
                                    tmp_inputStreamReader.close();
                                    tmp_inputStreamReader = null;
                                    tmp_inputStream.close();
                                    tmp_inputStream = null;
                                } else {
                                    flag_getResponseCode = true;
                                }
                            } catch (IOException e) {
                                http.disconnect();
                            } finally {
                                if (outStream != null)                  // Outputstream 해제
                                    outStream.close();

                                if (tmp_inputStream != null)            // InputStream 해제
                                    tmp_inputStream.close();

                                if (tmp_reader != null)                 // 버퍼리더 해제
                                    tmp_reader.close();

                                if (tmp_inputStreamReader != null)      // Inputstream리더 해제
                                    tmp_inputStreamReader.close();


                                http.disconnect();
                            }
                        }

                        if (!flag_getResponseCode && tmp_responseCode == -1) {
                            throw new IOException();
                        }

                    } catch (MalformedURLException e1) {
                        LogPrinter.CF_line();
                        LogPrinter.CF_debug("sendPostDataNoResponse : HTTP 서버 연결에 실패(1) : " + p_url);
                    } catch (IOException e) {
                        LogPrinter.CF_line();
                        LogPrinter.CF_debug("sendPostDataNoResponse : HTTP 통신 에러(2) : " + p_url);
                    }
                }
            }
        }).start();
    }

    /**
     * Http 통신 post data 전송
     * @param p_url             String,     url
     * @param p_strParam        String,     param
     * @param p_handler         Handler,    결과수신 handler
     * @param p_successMSGId    int,        성공 message.what
     * @param p_failMSGId       int,        실패 message.what
     */
    public static void sendPostDataCheck(final String p_url , final String p_strParam, final Handler p_handler , final int p_successMSGId , final int p_failMSGId ){
        new Thread(new Runnable() {
            public void run() {
                synchronized (this) {
                    try {
                        URL url = new URL(p_url);

                        // ---------------------------------------------------
                        //	ResopnseCode 체크 변수 세팅
                        // ---------------------------------------------------
                        boolean flag_getResponseCode = false;
                        int tmp_responseCode = -1;

                        HttpURLConnection http = (HttpURLConnection) url.openConnection();

                        if (http != null) {
                            http.setConnectTimeout(EnvConfig.HTTP_CONNECT_TIMEOUT);
                            http.setReadTimeout(EnvConfig.HTTP_POST_TIMEOUT);
                            http.setUseCaches(false);
                            http.setDefaultUseCaches(false);
                            http.setDoInput(true);
                            http.setDoOutput(true);
                            http.setRequestMethod("POST");

                            if (p_strParam != null) {
                                http.setFixedLengthStreamingMode(p_strParam.getBytes("utf-8").length);
                            }
                            http.setRequestProperty("content-type", "application/x-www-form-urlencoded");

                            // -------------------------------------
                            //	Param 전달
                            // -------------------------------------
                            OutputStream outStream = null;                 // http output stream
                            InputStream tmp_inputStream = null;                 // http input stream
                            BufferedReader tmp_reader = null;                 // 버퍼리더
                            InputStreamReader tmp_inputStreamReader = null;     // http input stream 리더
                            if (p_strParam != null) {
                                outStream = http.getOutputStream();
                                outStream.write(p_strParam.getBytes("utf-8"));
                                outStream.flush();
                                outStream.close();
                                outStream = null;
                            }

                            try {
                                tmp_responseCode = http.getResponseCode();

                                if (tmp_responseCode == HttpURLConnection.HTTP_OK) {
                                    flag_getResponseCode = true;

                                    // --------------------------------------------------
                                    // stream 읽어 Data 세팅
                                    // --------------------------------------------------
                                    tmp_inputStream = http.getInputStream();
                                    tmp_inputStreamReader = new InputStreamReader(tmp_inputStream, "utf-8");

                                    tmp_reader = new BufferedReader(tmp_inputStreamReader);
                                    StringBuilder builder = new StringBuilder();
                                    String str = "";

                                    while ((str = tmp_reader.readLine()) != null) {
                                        builder.append(str);
                                    }

                                    // ---------------------------------------------------------------
                                    //  BOM (byte of mark) 제거
                                    // ---------------------------------------------------------------
                                    String strHttpData = CommonFunction.CF_removeBOM(builder.toString());

                                    tmp_reader.close();
                                    tmp_reader = null;
                                    tmp_inputStreamReader.close();
                                    tmp_inputStreamReader = null;
                                    tmp_inputStream.close();
                                    tmp_inputStream = null;

                                    if (p_handler != null) {
                                        p_handler.sendMessage(p_handler.obtainMessage(p_successMSGId, strHttpData));
                                    }

                                }
                                // --<> (연결실패)
                                else {
                                    flag_getResponseCode = true;

                                    if (p_handler != null) {
                                        LogPrinter.CF_debug("--- 서버연결 실패 CODE : " + http.getResponseCode());
                                        p_handler.sendMessage(p_handler.obtainMessage(p_failMSGId, "고객님 죄송합니다. \r\n 서비스는 현재 임시점검중입니다. \r\n 빠른 시간안에 조치하도록 하겠습니다."));
                                    }
                                }

                            } catch (IOException e) {
                                http.disconnect();

                            } finally {
                                if (outStream != null)                  // Outputstream 해제
                                    outStream.close();

                                if (tmp_inputStream != null)            // InputStream 해제
                                    tmp_inputStream.close();

                                if (tmp_reader != null)                 // 버퍼리더 해제
                                    tmp_reader.close();

                                if (tmp_inputStreamReader != null)      // Inputstream리더 해제
                                    tmp_inputStreamReader.close();

                                http.disconnect();
                            }
                        }

                        if (!flag_getResponseCode && tmp_responseCode == -1) {
                            throw new IOException();
                        }

                    } catch (MalformedURLException e1) {
                        LogPrinter.CF_line();
                        LogPrinter.CF_debug("sendPostData : HTTP 서버 연결에 실패(1) : " + p_url);
                        p_handler.sendMessage(p_handler.obtainMessage(p_failMSGId, "서버 연결에 실패 하였습니다.\r\n관리자에게 문의 하세요.."));

                    } catch (IOException e) {
                        LogPrinter.CF_line();
                        LogPrinter.CF_debug("sendPostData : HTTP 통신 에러(2) : " + p_url);
                        p_handler.sendMessage(p_handler.obtainMessage(p_failMSGId, "통신 상태가 좋지 않습니다.\r\n확인 후 다시 시도해주세요."));
                    }
                }
            }
        }).start();
    }

    /**
     * Http 통신 post data 전송
     * @param p_url             String,     url
     * @param p_strParam        String,     param
     * @param p_handler         Handler,    결과수신 handler
     * @param p_successMSGId    int,        성공 message.what
     * @param p_failMSGId       int,        실패 message.what
     */
    public static void sendPostData(final String p_url , final String p_strParam, final Handler p_handler , final int p_successMSGId , final int p_failMSGId ){
        new Thread(new Runnable() {
            public void run() {
                synchronized (this) {
                    try {
                        URL url = new URL(p_url);

                        // ---------------------------------------------------
                        //	ResopnseCode 체크 변수 세팅
                        // ---------------------------------------------------
                        boolean flag_getResponseCode = false;
                        int tmp_responseCode = -1;

                        HttpURLConnection http = (HttpURLConnection) url.openConnection();

                        if (http != null) {
                            http.setConnectTimeout(EnvConfig.HTTP_CONNECT_TIMEOUT);
                            http.setReadTimeout(EnvConfig.HTTP_POST_TIMEOUT);
                            http.setUseCaches(false);
                            http.setDefaultUseCaches(false);
                            http.setDoInput(true);
                            http.setDoOutput(true);
                            http.setRequestMethod("POST");

                            if (p_strParam != null) {
                                http.setFixedLengthStreamingMode(p_strParam.getBytes("utf-8").length);
                            }
                            http.setRequestProperty("content-type", "application/x-www-form-urlencoded");

                            // -------------------------------------
                            //	Param 전달
                            // -------------------------------------
                            OutputStream outStream = null;                 // http output stream
                            InputStream tmp_inputStream = null;                 // http input stream
                            BufferedReader tmp_reader = null;                 // 버퍼리더
                            InputStreamReader tmp_inputStreamReader = null;     // http input stream 리더
                            if (p_strParam != null) {
                                outStream = http.getOutputStream();
                                outStream.write(p_strParam.getBytes("utf-8"));
                                outStream.flush();
                                outStream.close();
                                outStream = null;
                            }

                            try {
                                tmp_responseCode = http.getResponseCode();

                                if (tmp_responseCode == HttpURLConnection.HTTP_OK) {
                                    flag_getResponseCode = true;

                                    // --------------------------------------------------
                                    // stream 읽어 Data 세팅
                                    // --------------------------------------------------
                                    tmp_inputStream = http.getInputStream();
                                    tmp_inputStreamReader = new InputStreamReader(tmp_inputStream, "utf-8");

                                    tmp_reader = new BufferedReader(tmp_inputStreamReader);
                                    StringBuilder builder = new StringBuilder();
                                    String str = "";

                                    while ((str = tmp_reader.readLine()) != null) {
                                        builder.append(str);
                                    }

                                    // ---------------------------------------------------------------
                                    //  BOM (byte of mark) 제거
                                    // ---------------------------------------------------------------
                                    String strHttpData = CommonFunction.CF_removeBOM(builder.toString());

                                    tmp_reader.close();
                                    tmp_reader = null;
                                    tmp_inputStreamReader.close();
                                    tmp_inputStreamReader = null;
                                    tmp_inputStream.close();
                                    tmp_inputStream = null;

                                    LogPrinter.CF_debug("!---- http handler : " + p_handler.toString());
                                    if (p_handler != null) {
                                        p_handler.sendMessage(p_handler.obtainMessage(p_successMSGId, strHttpData));
                                    }

                                } else {
                                    flag_getResponseCode = true;

                                    if (p_handler != null) {
                                        p_handler.sendMessage(p_handler.obtainMessage(p_failMSGId, "서버에 연결할 수 없습니다.( code: " + http.getResponseCode() + ")"));
                                    }
                                }

                            } catch (IOException e) {
                                http.disconnect();

                            } finally {
                                if (outStream != null)                  // Outputstream 해제
                                    outStream.close();

                                if (tmp_inputStream != null)            // InputStream 해제
                                    tmp_inputStream.close();

                                if (tmp_reader != null)                 // 버퍼리더 해제
                                    tmp_reader.close();

                                if (tmp_inputStreamReader != null)      // Inputstream리더 해제
                                    tmp_inputStreamReader.close();

                                http.disconnect();
                            }
                        }

                        if (!flag_getResponseCode && tmp_responseCode == -1) {
                            throw new IOException();
                        }

                    } catch (MalformedURLException e1) {
                        LogPrinter.CF_line();
                        LogPrinter.CF_debug("sendPostData : HTTP 서버 연결에 실패(1) : " + p_url);
                        p_handler.sendMessage(p_handler.obtainMessage(p_failMSGId, "서버 연결에 실패 하였습니다.\r\n관리자에게 문의 하세요.."));

                    } catch (IOException e) {
                        LogPrinter.CF_line();
                        LogPrinter.CF_debug("sendPostData : HTTP 통신 에러(2) : " + p_url);
                        p_handler.sendMessage(p_handler.obtainMessage(p_failMSGId, "통신 상태가 좋지 않습니다.\r\n확인 후 다시 시도해주세요."));
                    }
                }
            }
        }).start();
    }
}

