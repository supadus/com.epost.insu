package com.epost.insu.common;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;

import static com.epost.insu.EnvConfig.CREDENTIAL_FILEPATH;
import static com.epost.insu.EnvConfig.CREDENTIAL_FOLDERPATH;
import static com.epost.insu.EnvConfig.CREDENTIAL_KEY_FILEPATH;


/**
 * CredentialUtils
 * @since     :
 * @version   : 1.1
 * @author    : LKM
 * <pre>
 *  공통으로 사용하는 Credential 관련 기능
 * ======================================================================
 * 0.0.0    LKM_20200423    최초 등록
 * 0.0.0    LKM_20200427    Exception throw 추가
 * 1.6.2    NJM_20210802    [2021년 대우 취약점] 2차본 : 부적절한 자원 해제
 * 1.6.3    NJM_20211006    [2021년 대우 취약점] 3차본 : 부적절한 예외 처리
 * 1.6.3    NJM_20211006    [2021년 대우 취약점] 3차본 : 부적절한 자원 해제
 * =======================================================================
 * copyright : (주)프로니스정보기술 | http://www.phronis.co.kr
 * </pre>
 */
public class CredentialUtils {
	private final String AUTHTYPE_PASSWORD = "password";
	private final String AUTHTYPE_FINGERPRINT = "fingerPrint";
	private final String AUTHTYPE_PATTERN = "pattern";

    public class IssueCredentialParam {
        public String did = "";
        public String csno = "";
        public String uuid = "";
        public String name = "";
        public String birthDate = "";
        public String phoneNo = "";
        public String sex = "";
        public String password = "";
        public String fingerPrint = "";
        public String pattern = "";
    }

    public class VerifyCredentialParam {
        public String did = "";
        public String uuid = "";
        public String csno = "";
        public String password = "";
        public String fingerPrint = "";
        public String pattern = "";
        public String name = "";
        public String phoneNo = "";
        public String birthDate = "";
        public String sex = "";
    }

	/**
     * 신원정보 파일 저장 함수
     * @param p_credential String
     * @throws IOException
     */
    public void saveCredential(String p_credential) throws IOException {
        File dir = new File(CREDENTIAL_FOLDERPATH);
        if (dir.exists() == false) {
            dir.mkdir();
        }

        BufferedWriter writer = null;
        try (FileOutputStream fos = new FileOutputStream(CREDENTIAL_FILEPATH)) {
            writer = new BufferedWriter(new OutputStreamWriter(fos));
        } catch (NullPointerException e) {
            e.getMessage();
        } catch (Exception e) {
            e.getMessage();
        } finally {
            if (writer != null) {
                writer.write(p_credential);
                writer.flush();
                writer.close();
            }
        }
    }

    /**
     * 신원정보 키파일 저장 함수
     * @param p_authKey String
     * @throws IOException
     */
    public void saveAuthKey(String p_authKey) throws IOException {
        File dir = new File(CREDENTIAL_FOLDERPATH);
        if(dir.exists() == false) {
            dir.mkdir();
        }

        BufferedWriter writer = null;
        try (FileOutputStream fos = new FileOutputStream(CREDENTIAL_KEY_FILEPATH)) {
            writer = new BufferedWriter(new OutputStreamWriter(fos));
        } catch (NullPointerException e) {
            e.getMessage();
        } catch (Exception e) {
            e.getMessage();
        } finally {
            if (writer != null) {
                writer.write(p_authKey);
                writer.flush();
                writer.close();
            }
        }
    }

    /**
     * 신원인증정보 파일 읽기<br/>
     * @return credential String
     * @throws IOException
     */
    public String readCredential() throws IOException {
        String credential = "";
        File f = new File(CREDENTIAL_FILEPATH);
        if(f.exists() == false) {
            return credential;
        }
        String line = null;
        BufferedReader buf = null;
        try {
            buf = new BufferedReader(new FileReader(CREDENTIAL_FILEPATH));
            while ((line = buf.readLine()) != null) {
                credential += line;
                credential += "\n";
            }
        } catch (NullPointerException e) {
            e.getMessage();
        } catch (Exception e) {
            e.getMessage();
        } finally {
            if(buf != null) {
                buf.close();
            }
        }

        return credential;
    }

    /**
     * 신원인증정보 키파일 읽기<br/>
     * @return credential String
     * @throws IOException
     */
    public String readAuthKey() throws IOException {
        String key = "";

        BufferedReader buf = null;
        try {
            File f = new File(CREDENTIAL_KEY_FILEPATH);
            if (!f.exists()) {
                return key;
            }
            String line = null;
            buf = new BufferedReader(new FileReader(CREDENTIAL_KEY_FILEPATH));
            while ((line = buf.readLine()) != null) {
                key += line;
            }
        } catch (NullPointerException e) {
            e.getMessage();
        } finally {
            if(buf != null)  buf.close();
        }
        return key;
    }

    /**
     * 신원정보, 키파일 삭제
     */
    public void removeCredentialAndKey() {
    	File f = new File(CREDENTIAL_FILEPATH);
        File f2 = new File(CREDENTIAL_KEY_FILEPATH);
        File dir = new File(CREDENTIAL_FOLDERPATH);
        f.delete();
        f2.delete();
        dir.delete();
    }

    /**
     * 신원정보 추출
     * @param credential String
     * @return IssueCredentialParam
     * @throws Exception
     */
    public IssueCredentialParam getUserInfoFromCredential(String credential) throws Exception {

        final String jsonKey_credentialSubject = "credentialSubject";
        final String jsonKey_id = "id";
        final String jsonKey_uuid = "uuid";
        final String jsonKey_name = "name";
        final String jsonKey_birthDate = "birthDate";
        final String jsonKey_phoneNo = "phoneNo";
        final String jsonKey_sex = "sex";

        final String jsonKey_blockchainclaim = "blockchainclaim";
        final String jsonKey_csno = "csno";

        IssueCredentialParam p_param = new IssueCredentialParam();  // 20200408_YJH_추가
        JSONObject obj = new JSONObject(credential);
        JSONObject credentialSubjectObject = obj.getJSONObject(jsonKey_credentialSubject);
        if(credentialSubjectObject.has(jsonKey_id)) {
            p_param.did = credentialSubjectObject.getString(jsonKey_id);
        }
        if(credentialSubjectObject.has(jsonKey_uuid)) {
            p_param.uuid = credentialSubjectObject.getString(jsonKey_uuid);
        }
        if(credentialSubjectObject.has(jsonKey_name)) {
            p_param.name = credentialSubjectObject.getString(jsonKey_name);
        }
        if(credentialSubjectObject.has(jsonKey_birthDate)) {
            p_param.birthDate = credentialSubjectObject.getString(jsonKey_birthDate);
        }
        if(credentialSubjectObject.has(jsonKey_phoneNo)) {
            p_param.phoneNo = credentialSubjectObject.getString(jsonKey_phoneNo);
        }
        if(credentialSubjectObject.has(jsonKey_sex)) {
            p_param.sex = credentialSubjectObject.getString(jsonKey_sex);
        }
        if(credentialSubjectObject.has(jsonKey_blockchainclaim)) {
            JSONObject blockchainclaimObject = credentialSubjectObject.getJSONObject(jsonKey_blockchainclaim);
            if(blockchainclaimObject.has(jsonKey_csno)) {
                p_param.csno = blockchainclaimObject.getString(jsonKey_csno);
            }
        }
        return p_param;
    }

    /**
     * 신원정보 추출
     * @param credential String
     * @param authType String
     * @return VerifyCredentialParam
     * @throws Exception
     */
    public VerifyCredentialParam getUserInfoFromCredential(String credential, String authType) throws Exception {
    	if(authType.equals(AUTHTYPE_PASSWORD)) {
    		final String jsonKey_credentialSubject = "credentialSubject";
	        final String jsonKey_id = "id";
	        final String jsonKey_uuid = "uuid";

	        final String jsonKey_blockchainclaim = "blockchainclaim";
	        final String jsonKey_csno = "csno";
	        final String jsonKey_password = "password";

	        VerifyCredentialParam p_param = new VerifyCredentialParam();    // 20200325_YJH_추가
            JSONObject obj = new JSONObject(credential);
            JSONObject credentialSubjectObject = obj.getJSONObject(jsonKey_credentialSubject);
            if(credentialSubjectObject.has(jsonKey_id)) {
                p_param.did = credentialSubjectObject.getString(jsonKey_id);
            }
            if(credentialSubjectObject.has(jsonKey_uuid)) {
                p_param.uuid = credentialSubjectObject.getString(jsonKey_uuid);
            }
            if(credentialSubjectObject.has(jsonKey_blockchainclaim)) {
                JSONObject blockchainclaimObject = credentialSubjectObject.getJSONObject(jsonKey_blockchainclaim);
                if(blockchainclaimObject.has(jsonKey_csno)) {
                    p_param.csno = blockchainclaimObject.getString(jsonKey_csno);
                }
                if(blockchainclaimObject.has(jsonKey_password)) {
                    p_param.password = blockchainclaimObject.getString(jsonKey_password);
                }
            }
	        return p_param;
    	} else if(authType.equals(AUTHTYPE_FINGERPRINT)) {
    		final String jsonKey_credentialSubject = "credentialSubject";
	        final String jsonKey_id = "id";
	        final String jsonKey_uuid = "uuid";

	        final String jsonKey_blockchainclaim = "blockchainclaim";
	        final String jsonKey_csno = "csno";
	        final String jsonKey_name = "name";
	        final String jsonKey_phoneNo = "phoneNo";
	        final String jsonKey_birthDate = "birthDate";
	        final String jsonKey_sex = "sex";

	        VerifyCredentialParam p_param = new VerifyCredentialParam();    // 20200325_YJH_추가
            JSONObject obj = new JSONObject(credential);
            JSONObject credentialSubjectObject = obj.getJSONObject(jsonKey_credentialSubject);
            if(credentialSubjectObject.has(jsonKey_id)) {
                p_param.did = credentialSubjectObject.getString(jsonKey_id);
            }
            if(credentialSubjectObject.has(jsonKey_uuid)) {
                p_param.uuid = credentialSubjectObject.getString(jsonKey_uuid);
            }
            if(credentialSubjectObject.has(jsonKey_name)) {
                p_param.name = credentialSubjectObject.getString(jsonKey_name);
            }
            if(credentialSubjectObject.has(jsonKey_phoneNo)) {
                p_param.phoneNo = credentialSubjectObject.getString(jsonKey_phoneNo);
            }
            if(credentialSubjectObject.has(jsonKey_birthDate)) {
                p_param.birthDate = credentialSubjectObject.getString(jsonKey_birthDate);
            }
            if(credentialSubjectObject.has(jsonKey_sex)) {
                p_param.sex = credentialSubjectObject.getString(jsonKey_sex);
            }
            if(credentialSubjectObject.has(jsonKey_blockchainclaim)) {
                JSONObject blockchainclaimObject = credentialSubjectObject.getJSONObject(jsonKey_blockchainclaim);
                if(blockchainclaimObject.has(jsonKey_csno)) {
                    p_param.csno = blockchainclaimObject.getString(jsonKey_csno);
                }
            }
	        return p_param;
    	} else if(authType.equals(AUTHTYPE_PATTERN)) {
    		final String jsonKey_credentialSubject = "credentialSubject";
	        final String jsonKey_id = "id";
	        final String jsonKey_uuid = "uuid";

	        final String jsonKey_blockchainclaim = "blockchainclaim";
	        final String jsonKey_csno = "csno";
	        final String jsonKey_name = "name";
	        final String jsonKey_phoneNo = "phoneNo";
	        final String jsonKey_birthDate = "birthDate";
	        final String jsonKey_sex = "sex";
	        final String jsonKey_pattern = "pattern";

	        VerifyCredentialParam p_param = new VerifyCredentialParam();    // 20200325_YJH_추가
            JSONObject obj = new JSONObject(credential);
            JSONObject credentialSubjectObject = obj.getJSONObject(jsonKey_credentialSubject);
            if(credentialSubjectObject.has(jsonKey_id)) {
                p_param.did = credentialSubjectObject.getString(jsonKey_id);
            }
            if(credentialSubjectObject.has(jsonKey_uuid)) {
                p_param.uuid = credentialSubjectObject.getString(jsonKey_uuid);
            }
            if(credentialSubjectObject.has(jsonKey_name)) {
                p_param.name = credentialSubjectObject.getString(jsonKey_name);
            }
            if(credentialSubjectObject.has(jsonKey_phoneNo)) {
                p_param.phoneNo = credentialSubjectObject.getString(jsonKey_phoneNo);
            }
            if(credentialSubjectObject.has(jsonKey_birthDate)) {
                p_param.birthDate = credentialSubjectObject.getString(jsonKey_birthDate);
            }
            if(credentialSubjectObject.has(jsonKey_sex)) {
                p_param.sex = credentialSubjectObject.getString(jsonKey_sex);
            }
            if(credentialSubjectObject.has(jsonKey_blockchainclaim)) {
                JSONObject blockchainclaimObject = credentialSubjectObject.getJSONObject(jsonKey_blockchainclaim);
                if(blockchainclaimObject.has(jsonKey_csno)) {
                    p_param.csno = blockchainclaimObject.getString(jsonKey_csno);
                }
                if(blockchainclaimObject.has(jsonKey_pattern)) {
                    p_param.pattern = blockchainclaimObject.getString(jsonKey_pattern);
                }
            }
	        return p_param;
    	}
    	return new VerifyCredentialParam();
    }

    /**
     * DID 정보 추출
     * @param credential String
     * @return did String
     * @throws JSONException
     */
    public String getDIDFromCredential(String credential) throws JSONException {
        String did = "";
        JSONObject obj = new JSONObject(credential);
        final String jsonKey_credentialSubject = "credentialSubject";
        JSONObject credentialSubjectObject = obj.getJSONObject(jsonKey_credentialSubject);
        did = credentialSubjectObject.getString("id");
        return did;
    }

    /**
     * CSNO 정보 추출
     * @param credential String
     * @return csno String
     * @throws JSONException
     */
    public String getCsnoFromCredential(String credential) throws JSONException {
        JSONObject obj = new JSONObject(credential);
        final String jsonKey_credentialSubject = "credentialSubject";
        final String jsonKey_blockchainclaim = "blockchainclaim";
        JSONObject credentialSubjectObject = obj.getJSONObject(jsonKey_credentialSubject);
        JSONObject blockchainclaimObject = credentialSubjectObject.getJSONObject(jsonKey_blockchainclaim);
        return blockchainclaimObject.getString("csno");
    }

    /**
     * 인증유형에 따른 신원정보 유무 확인
     * @param p_authType String
     * @return isPresent boolean
     * @throws JSONException, IOException
     */
    public boolean existsAuthTypeFromCredential(String p_authType) throws JSONException, IOException {
        String credential = readCredential();
        JSONObject obj = new JSONObject(credential);
        final String jsonKey_credentialSubject = "credentialSubject";
        final String jsonKey_blockchainclaim = "blockchainclaim";
        JSONObject credentialSubjectObject = obj.getJSONObject(jsonKey_credentialSubject);
        JSONObject blockchainclaimObject = credentialSubjectObject.getJSONObject(jsonKey_blockchainclaim);
        if(blockchainclaimObject.has(p_authType) == true) {
            return true;
        } else {
            return false;
        }
    }
}