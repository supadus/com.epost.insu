package com.epost.insu.common;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * SQLite 헬퍼 클래스
 * @since     :
 * @version   : 1.1
 * @author    : LSH
 * <pre>
 *      SQLite 테이블 생성, 조회, 쿼리 실행
 * ======================================================================
 * 1.0  LSH_20171026    최초 등록
 * 1.1  NJM_20210209    versionCode Update(1->2), TableName_Banner 컬럼명 변경(no->seq, desc->s_desc), Popup테이블추가 [하단팝업공지]
 * =======================================================================
 * copyright : (주)프로니스정보기술 | http://www.phronis.co.kr
 * </pre>
 */
@SuppressWarnings("FieldCanBeLocal")
public class CustomSQLiteHelper extends SQLiteOpenHelper{
    private static final String db_name   = "com.epost.insu.pay";
    private static final int versionCode  = 2;

    private final String TableName_User   = "User";
    private final String TableName_Banner = "Banner";
    private final String TableName_Popup  = "Popup";

    // 테이블 생성 쿼리문
    private final String QUERYSTRING_CREATE_USER   = "CREATE TABLE IF NOT EXISTS "+TableName_User  +" (csno varchar(9), name varchar(10), loginType varchar(1))";
    private final String QUERYSTRING_CREATE_BANNER = "CREATE TABLE IF NOT EXISTS "+TableName_Banner+" (seq integer primary key autoincrement, path varchar(100), link varchar(100), s_desc varchar(100), width integer, height integer, savedPath varchar(100))";
    private final String QUERYSTRING_CREATE_POPUP  = "CREATE TABLE IF NOT EXISTS "+TableName_Popup +" (seq integer primary key autoincrement, path varchar(100), link varchar(100), s_desc varchar(100), width integer, height integer, savedPath varchar(100))";

    // 테이블 생성 유무 검사 쿼리문
    private final String QUERYSTRING_CHECK_USER   = "SELECT name FROM sqlite_master WHERE type='table' AND name='"+TableName_User+"';";
    private final String QUERYSTRING_CHECK_BANNER = "SELECT name FROM sqlite_master WHERE type='table' AND name='"+TableName_Banner+"';";
    private final String QUERYSTRING_CHECK_POPUP  = "SELECT name FROM sqlite_master WHERE type='table' AND name='"+TableName_Popup+"';";

    /**
     * 생성자
     * @param p_context     Context
     */
    public CustomSQLiteHelper(Context p_context){
        this(p_context, db_name, null, versionCode);
    }
    /**
     * 생성자
     * @param p_context         Context
     * @param p_dbName          String
     * @param p_cursorFactory   SQLiteDatabase.CursorFactory
     * @param p_dbVersion       int
     */
    public CustomSQLiteHelper(Context p_context, String p_dbName, SQLiteDatabase.CursorFactory p_cursorFactory, int p_dbVersion){
        super(p_context, p_dbName, p_cursorFactory, p_dbVersion);
    }

    @Override
    public void onCreate(SQLiteDatabase p_sqlite) {
        LogPrinter.CF_debug("!-----------------------------------------------------------");
        LogPrinter.CF_debug("!-- CustomSQLiteHelper.onCreate()");
        LogPrinter.CF_debug("!-----------------------------------------------------------");

        checkCreatedDBTable(p_sqlite);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        LogPrinter.CF_debug("!-----------------------------------------------------------");
        LogPrinter.CF_debug("!-- CustomSQLiteHelper.onUpgrade()");
        LogPrinter.CF_debug("!-----------------------------------------------------------");
        LogPrinter.CF_debug("!-- oldVersion :" + oldVersion);
        LogPrinter.CF_debug("!-- newVersion :" + newVersion);

        // 2버전 수정사항 : TableName_Banner 컬럼명 변경, no -> seq, desc -> s_desc, Popup테이블추가(자동적용)
        if (newVersion > 1) {
            db.execSQL("DROP TABLE IF EXISTS " + TableName_Banner);
            LogPrinter.CF_debug("!-- TableName_Banner :" + "table delete");
        }

        checkCreatedDBTable(db);
    }

    /**
     * DB 테이블 검사 및 생성 함수<br/>
     * DB 테이블 검사하여 테이블이 존재하지 않는 경우 생성하고 초기 데이터가 필요한 경우 INSERT 한다.
     * @param p_sqlite      SQLiteDatabase
     */
    private void checkCreatedDBTable(SQLiteDatabase p_sqlite){
        createUserTable(p_sqlite);
        createBannerTable(p_sqlite);
        createPopupTable(p_sqlite);
    }

    /**
     * USER 정보 테이블 생성 함수<br/>
     * insert default value
     * @param p_sqlite      SQLiteDatabase
     */
    private void createUserTable(SQLiteDatabase p_sqlite){
        Cursor tmp_cursor = p_sqlite.rawQuery(QUERYSTRING_CHECK_USER,null);
        if(!tmp_cursor.moveToFirst()){
            p_sqlite.execSQL(QUERYSTRING_CREATE_USER);

            ContentValues tmp_value = new ContentValues(1);
            tmp_value.put("csno"     , "");
            tmp_value.put("name"     , "");
            tmp_value.put("loginType", "");
            p_sqlite.insert(TableName_User,null, tmp_value);

        }
        tmp_cursor.close();
    }

    /**
     * 배너 테이블 생성 함수<br/>
     * @param p_sqlite      SQLiteDatabase
     */
    private void createBannerTable(SQLiteDatabase p_sqlite){
        Cursor tmp_cursor = p_sqlite.rawQuery(QUERYSTRING_CHECK_BANNER,null);
        if(!tmp_cursor.moveToFirst()){
            p_sqlite.execSQL(QUERYSTRING_CREATE_BANNER);
        }
        tmp_cursor.close();
    }

    /**
     * 공지 테이블 생성 함수<br/>
     * @param p_sqlite      SQLiteDatabase
     */
    private void createPopupTable(SQLiteDatabase p_sqlite){
        Cursor tmp_cursor = p_sqlite.rawQuery(QUERYSTRING_CHECK_POPUP,null);
        if(!tmp_cursor.moveToFirst()){
            p_sqlite.execSQL(QUERYSTRING_CREATE_POPUP);
        }
        tmp_cursor.close();
    }

    // #############################################################################################
    //  USER 테이블 관련 처리 함수
    // #############################################################################################
    /**
     * 로그인 고객 정보 업데이트<br/>
     * @param p_sqlite      SQLiteDatabase
     * @param p_name        String
     * @param p_csno        String
     * @param p_loginType   String ("1":공동인증, "2":지문인증, "3":PIN인증, "4":패턴인증, "10":카카오인증)
     */
    public void CF_UpdateUserInfo(SQLiteDatabase p_sqlite, String p_name, String p_csno, String p_loginType){
        ContentValues tmp_value = new ContentValues(3);
        tmp_value.put("name"     , p_name);
        tmp_value.put("csno"     , p_csno);
        tmp_value.put("loginType", p_loginType);

        p_sqlite.update(TableName_User, tmp_value, null, null);
    }

    /**
     * 로그인 고객정보 업데이트<br/>
     * @param p_sqlite      SQLiteDatabase
     * @param p_name        String
     * @param p_loginType   String ("1":공동인증, "2":지문인증, "3":PIN인증, "4":패턴인증, "10":카카오인증)
     */
    public void CF_UpdateUserInfo(SQLiteDatabase p_sqlite, String p_name, String p_loginType){
        ContentValues tmp_value = new ContentValues(2);
        tmp_value.put("name"     , p_name);
        tmp_value.put("loginType", p_loginType);

        p_sqlite.update(TableName_User, tmp_value, null, null);
    }

    /**
     * csno 반환<br/>
     * @param p_sqlite  SQLiteDatabase
     * @return          String
     */
    public String CF_Selectcsno(SQLiteDatabase p_sqlite){
        String tmp_csNo = "";
        String[] tmp_columns = new String[]{"csno"};

        Cursor tmp_cursor = p_sqlite.query(TableName_User,tmp_columns,null,null,null,null,null);

        if(tmp_cursor.moveToFirst()){
            tmp_csNo = tmp_cursor.getString(0);
        }
        tmp_cursor.close();

        return tmp_csNo;
    }

    /**
     * 로그인 타입 반환함수<br/>
     * @param p_sqlite  SQLiteDatabase
     * @return          String (empty:로그인안함, 1:공동인증, 2:Fido , 3:PIN)
     */
    public String CF_SelectLoginType(SQLiteDatabase p_sqlite){
        String tmp_loginType = "";
        String[] tmp_columns = new String[]{"loginType"};

        Cursor tmp_cursor = p_sqlite.query(TableName_User, tmp_columns, null, null, null, null, null);

        if(tmp_cursor.moveToFirst()){
            tmp_loginType = tmp_cursor.getString(0);
        }
        tmp_cursor.close();

        return tmp_loginType;
    }

    /**
     * 고객 이름 반환 함수
     * @param p_sqlite  SQLiteDatabase
     * @return          String
     */
    public String CF_SelectUserName(SQLiteDatabase p_sqlite){
        String tmp_name = "";
        String[] tmp_columns = new String[]{"name"};

        Cursor tmp_cursor = p_sqlite.query(TableName_User,tmp_columns,null,null,null,null,null);

        if(tmp_cursor.moveToFirst()){
            tmp_name = tmp_cursor.getString(0);
        }
        tmp_cursor.close();

        return tmp_name;
    }

    // #############################################################################################
    //  Banner 테이블 관련 처리 함수
    // #############################################################################################
    /**
     * 배너 이미지 정보 삭제(전체)
     * @param p_sqlite      SQLiteDatabase
     */
    public void CF_DelBannerInfo(SQLiteDatabase p_sqlite){
        p_sqlite.delete(TableName_Banner,null,null);
    }

//    /**
//     * 배너 이미지 정보 삭제(1개씩)
//     * @param p_sqlite      SQLiteDatabase
//     * @param p_path        String
//     */
//    public void CF_DelBannerInfo(SQLiteDatabase p_sqlite, String p_path){
//        p_sqlite.delete(TableName_Banner, "path='"+p_path+"'", null);
//    }

    /**
     * 배너 이미지 정보 추가
     * @param p_sqlite      SQLiteDatabase
     * @param p_path        String
     * @param p_link        String
     * @param p_width       int
     * @param p_height      int
     */
    public void CF_InsertBannerInfo(SQLiteDatabase p_sqlite, String p_path, String p_link , String p_desc,  int p_width, int p_height){
        ContentValues tmp_values = new ContentValues();
        tmp_values.put("path"     , p_path);
        tmp_values.put("link"     , p_link);
        tmp_values.put("s_desc"   , p_desc);
        tmp_values.put("width"    , p_width);
        tmp_values.put("height"   , p_height);
        tmp_values.put("savedPath", "");

        p_sqlite.insert(TableName_Banner, null, tmp_values );
    }

    /**
     * 배너 파일 저장 경로 업데이트<br/>
     * @param p_sqlite      SQLiteDatabase
     * @param p_path        String
     * @param p_savedPath   String
     */
    public void CF_UpdateBannerSavedPath(SQLiteDatabase p_sqlite, String p_path, String p_savedPath){
        ContentValues tmp_values = new ContentValues();
        tmp_values.put("savedPath", p_savedPath);

        p_sqlite.update(TableName_Banner, tmp_values, "path='"+p_path+"'",null);
    }

    /**
     * 배너 이미지 경로를 반환한다.
     * @param p_sqlite      SQLiteDatabase
     * @return              ArrayList<String>
     */
    public ArrayList<String> CF_SelectBannerPaths(SQLiteDatabase p_sqlite){
        ArrayList<String> tmp_arrPath = new ArrayList<>();

        String[] tmp_columns = new String[]{"path"};
        Cursor tmp_cursor = p_sqlite.query(TableName_Banner,tmp_columns,null,null,null,null,"seq ASC");

        if(tmp_cursor.moveToFirst()){
            boolean tmp_flagHasNext = true;

            while(tmp_flagHasNext){
                tmp_arrPath.add(tmp_cursor.getString(0));

                tmp_flagHasNext = tmp_cursor.moveToNext();
            }
        }

        tmp_cursor.close();

        return  tmp_arrPath;
    }

    /**
     * 배너 이미지 저장 경로를 반환한다.
     * @param p_sqlite      SQLiteDatabase
     * @return              ArrayList<String>
     */
    public ArrayList<String> CF_SelectBannerSavedPaths(SQLiteDatabase p_sqlite){
        ArrayList<String> tmp_arrPath = new ArrayList<>();

        String[] tmp_columns = new String[]{"savedPath"};
        Cursor tmp_cursor = p_sqlite.query(TableName_Banner,tmp_columns,null,null,null,null,"seq ASC");

        if(tmp_cursor.moveToFirst()){
            boolean tmp_flagHasNext = true;

            while(tmp_flagHasNext){
                tmp_arrPath.add(tmp_cursor.getString(0));

                tmp_flagHasNext = tmp_cursor.moveToNext();
            }
        }

        tmp_cursor.close();

        return  tmp_arrPath;
    }

    /**
     * 모든 배너이미지 정보를 JSONObject형으로 JSONArray에 담아 반환
     * @param p_sqlite      SQLiteDatabase
     * @return              JSONArray
     */
    public JSONArray CF_SelectBannerInfo(SQLiteDatabase p_sqlite) throws JSONException {
        String[] tmp_columns = new String[]{"path,link,s_desc,width,height,savedPath"};

        Cursor tmp_cursor = p_sqlite.query(TableName_Banner,tmp_columns,null,null,null,null,"seq ASC");

        ArrayList<String> tmp_arrIntTypeColumnName = new ArrayList<>();
        tmp_arrIntTypeColumnName.add("width");
        tmp_arrIntTypeColumnName.add("height");

        JSONArray tmp_jsonArray = createJSONArrayFromCursor(tmp_cursor, tmp_arrIntTypeColumnName, new ArrayList<String>());

        tmp_cursor.close();

        return tmp_jsonArray;
    }


    // #############################################################################################
    //  Popup 테이블 관련 처리 함수
    // #############################################################################################
    /**
     * 팝업 이미지 정보 삭제(전체)
     * @param p_sqlite      SQLiteDatabase
     */
    public void CF_DelPopupInfo(SQLiteDatabase p_sqlite){
        p_sqlite.delete(TableName_Popup,null,null);
    }

//    /**
//     * 팝업 이미지 정보 삭제(1개씩)
//     * @param p_sqlite      SQLiteDatabase
//     * @param p_path        String
//     */
//    public void CF_DelPopupInfo(SQLiteDatabase p_sqlite, String p_path){
//        p_sqlite.delete(TableName_Popup, "path='"+p_path+"'", null);
//    }

    /**
     * 팝업 이미지 정보 추가
     * @param p_sqlite      SQLiteDatabase
     * @param p_path        String
     * @param p_link        String
     * @param p_width       int
     * @param p_height      int
     */
    public void CF_InsertPopupInfo(SQLiteDatabase p_sqlite, String p_path, String p_link , String p_desc,  int p_width, int p_height){
        ContentValues tmp_values = new ContentValues();
        tmp_values.put("path"     , p_path);
        tmp_values.put("link"     , p_link);
        tmp_values.put("s_desc"   , p_desc);
        tmp_values.put("width"    , p_width);
        tmp_values.put("height"   , p_height);
        tmp_values.put("savedPath", "");

        p_sqlite.insert(TableName_Popup, null, tmp_values );
    }

    /**
     * 팝업 파일 저장 경로 업데이트<br/>
     * @param p_sqlite      SQLiteDatabase
     * @param p_path        String
     * @param p_savedPath   String
     */
    public void CF_UpdatePopupSavedPath(SQLiteDatabase p_sqlite, String p_path, String p_savedPath){
        ContentValues tmp_values = new ContentValues();
        tmp_values.put("savedPath", p_savedPath);

        p_sqlite.update(TableName_Popup, tmp_values, "path='"+p_path+"'",null);
    }

    /**
     * 팝업 정보 테이블 생성 함수<br/>
     * insert default value
     * @param p_sqlite      SQLiteDatabase
     */
    public int CF_popupCnt(SQLiteDatabase p_sqlite){
        int cnt = (int) DatabaseUtils.queryNumEntries(p_sqlite, TableName_Popup);
        p_sqlite.close();
        return cnt;
    }

    /**
     * 팝업 이미지 경로를 반환한다.
     * @param p_sqlite      SQLiteDatabase
     * @return              ArrayList<String>
     */
    public ArrayList<String> CF_SelectPopupPaths(SQLiteDatabase p_sqlite){
        ArrayList<String> tmp_arrPath = new ArrayList<>();

        String[] tmp_columns = new String[]{"path"};
        Cursor tmp_cursor = p_sqlite.query(TableName_Popup,tmp_columns,null,null,null,null,"seq ASC");

        if(tmp_cursor.moveToFirst()){
            boolean tmp_flagHasNext = true;

            while(tmp_flagHasNext){
                tmp_arrPath.add(tmp_cursor.getString(0));

                tmp_flagHasNext = tmp_cursor.moveToNext();
            }
        }

        tmp_cursor.close();
        return  tmp_arrPath;
    }

    /**
     * 팝업 이미지 저장 경로를 반환한다.
     * @param p_sqlite      SQLiteDatabase
     * @return              ArrayList<String>
     */
    public ArrayList<String> CF_SelectPopupSavedPaths(SQLiteDatabase p_sqlite){
        ArrayList<String> tmp_arrPath = new ArrayList<>();

        String[] tmp_columns = new String[]{"savedPath"};
        Cursor tmp_cursor = p_sqlite.query(TableName_Popup,tmp_columns,null,null,null,null,"seq ASC");

        if(tmp_cursor.moveToFirst()){
            boolean tmp_flagHasNext = true;

            while(tmp_flagHasNext){
                tmp_arrPath.add(tmp_cursor.getString(0));
                tmp_flagHasNext = tmp_cursor.moveToNext();
            }
        }

        tmp_cursor.close();
        return  tmp_arrPath;
    }

    /**
     * 모든 팝업이미지 정보를 JSONObject형으로 JSONArray에 담아 반환
     * @param p_sqlite      SQLiteDatabase
     * @return              JSONArray
     */
    public JSONArray CF_SelectPopupInfo(SQLiteDatabase p_sqlite) throws JSONException {
        String[] tmp_columns = new String[]{"path,link,s_desc,width,height,savedPath"};

        Cursor tmp_cursor = p_sqlite.query(TableName_Popup,tmp_columns,null,null,null,null,"seq ASC");

        ArrayList<String> tmp_arrIntTypeColumnName = new ArrayList<>();
        tmp_arrIntTypeColumnName.add("width");
        tmp_arrIntTypeColumnName.add("height");

        JSONArray tmp_jsonArray = createJSONArrayFromCursor(tmp_cursor, tmp_arrIntTypeColumnName, new ArrayList<String>());
        tmp_cursor.close();

        return tmp_jsonArray;
    }

    // #############################################################################################
    //  ETC
    // #############################################################################################
    /**
     * SELECT 쿼리 실행 결과인 Cursor를 읽어 JSONArray 타입으로 반환한다.
     * @param p_cursor                  Cursor
     * @param p_arrIntColumnName        ArrayList<String>
     * @param p_arrBooleanColumnName    ArrayList<String>
     * @return                          JSONArray
     */
    private JSONArray createJSONArrayFromCursor(Cursor p_cursor, ArrayList<String> p_arrIntColumnName, ArrayList<String> p_arrBooleanColumnName) throws JSONException {
        JSONArray tmp_jsonArray = new JSONArray();

        if(p_cursor.moveToFirst()){
            boolean tmp_flagHasNext = true;

            while(tmp_flagHasNext){
                JSONObject tmp_jsonObject = new JSONObject();

                for(int i = 0 ; i < p_cursor.getColumnCount(); i++){
                    String tmp_columnName = p_cursor.getColumnName(i);

                    if(p_arrIntColumnName.contains(tmp_columnName)){
                        tmp_jsonObject.put(tmp_columnName, p_cursor.getInt(i));
                    }
                    else if(p_arrBooleanColumnName.contains(tmp_columnName)){
                        tmp_jsonObject.put(tmp_columnName, p_cursor.getInt(i) == 1);
                    }
                    else{
                        tmp_jsonObject.put(tmp_columnName, p_cursor.getString(i));
                    }
                }
                tmp_jsonArray.put(tmp_jsonObject);
                tmp_flagHasNext = p_cursor.moveToNext();
            }
        }

        return tmp_jsonArray;
    }
}
