//package com.epost.insu.push.db;
//
//import android.content.ContentValues;
//import android.content.Context;
//import android.database.Cursor;
//import android.database.SQLException;
//import android.database.sqlite.SQLiteDatabase;
//import android.database.sqlite.SQLiteDatabase.CursorFactory;
//import android.database.sqlite.SQLiteOpenHelper;
//import android.os.Environment;
//
//import java.io.File;
//
//public class DbOpenHelper {
//
//	private static final String DATABASE_NAME = "cbsapp1.db";
//	//public static final String DATABASE_PATH = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "cbs" + File.separator + DATABASE_NAME;
//	public static String DATABASE_PATH = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + DATABASE_NAME;
//	private static final int DATABASE_VERSION = 1;
//	public static SQLiteDatabase mDB;
//	private DatabaseHelper mDBHelper;
//	private Context mContext;
//	protected String mDBPath;
//
//	private class DatabaseHelper extends SQLiteOpenHelper{
//
//		public DatabaseHelper(Context context, String name, CursorFactory factory, int version) {
//			super(context, name, factory, version);
//			try {
//				DATABASE_PATH  = context.getDatabasePath(DATABASE_NAME).getPath(); //.getFilesDir().getCanonicalPath() + File.separator + DATABASE_NAME;
//			}
//			catch(Exception e) {
//				e.printStackTrace();
//			}
//		}
//
//		@Override
//		public void onCreate(SQLiteDatabase db) {
//			db.execSQL(com.push.democlient.db.DataBases.CreateDB.PUSHMSG_CREATE);
//		}
//
//		@Override
//		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
//			db.execSQL("DROP TABLE IF EXISTS "+ com.push.democlient.db.DataBases.CreateDB.PUSHMSG_TABLENAME);
//			onCreate(db);
//		}
//	}
//
//	public DbOpenHelper(Context context){
//		this.mContext = context;
//		try {
//			DATABASE_PATH  = context.getDatabasePath(DATABASE_NAME).getPath();
//		}
//		catch(Exception e) {
//			e.printStackTrace();
//		}
//	}
//
//	public com.push.democlient.db.DbOpenHelper open() {
//		try {
//			mDBHelper = new DatabaseHelper(mContext, DATABASE_PATH /*DATABASE_NAME*/, null, DATABASE_VERSION);
//			mDB = mDBHelper.getWritableDatabase();
//		}
//		catch(Exception e) {
//			e.printStackTrace();
//		}
//		return this;
//	}
//
//	public void close(){
//		mDB.close();
//	}
//
//	public boolean databaseDrop() {
//		return this.mContext.deleteDatabase(DATABASE_PATH);
//	}
//
//	public Cursor getPushAppMassageALL() {
//		Cursor c = mDB.query(com.push.democlient.db.DataBases.CreateDB.PUSHMSG_TABLENAME, null, null, null, null, null, null);
//		if (c != null && c.getCount() != 0) {
//			c.moveToFirst();
//		}
//
//		return c;
//	}
//
//	public Cursor getPushAppMassage(String messageKey) {
//		Cursor c = mDB.query(com.push.democlient.db.DataBases.CreateDB.PUSHMSG_TABLENAME, null,
//				com.push.democlient.db.DataBases.CreateDB.COL_MSGKEY + " = '"+ messageKey +"'", null, null, null, null);
//		if (c != null && c.getCount() != 0) {
//			c.moveToFirst();
//		}
//
//		return c;
//	}
//
//	public long insertPushMSG(String msgkey, String msg, String ext, String encryptData) {
//		long returnVal = -1;
//		String key = Long.toString(System.currentTimeMillis());
//		try {
//			ContentValues values = new ContentValues();
//			values.put(com.push.democlient.db.DataBases.CreateDB.COL_MSGKEY, key);
//			values.put(com.push.democlient.db.DataBases.CreateDB.COL_MSGDATA, msg);
//			values.put(com.push.democlient.db.DataBases.CreateDB.COL_EXTDATA, ext);
//			values.put(com.push.democlient.db.DataBases.CreateDB.COL_ENCRYPTDATA, encryptData);
//			values.put(com.push.democlient.db.DataBases.CreateDB.COL_DATE, com.push.democlient.db.DBUtils.getStrNowDate());
//			returnVal = -1;
//
//			returnVal = mDB.insert(com.push.democlient.db.DataBases.CreateDB.PUSHMSG_TABLENAME, null, values);
//		}
//		catch(Exception e) {
//			returnVal = - 99;
//		}
//		return returnVal;
//	}
//
//	public int deletePushMSG(String msgkey){
//		int returnVal = -1;
//		try {
//			returnVal = mDB.delete(com.push.democlient.db.DataBases.CreateDB.PUSHMSG_TABLENAME,
//								   	com.push.democlient.db.DataBases.CreateDB.COL_MSGKEY + " = '"+ msgkey +"'", null);
//		} catch(SQLException e) {
//		}
//		return returnVal;
//	}
//
//}
//
//
//
//
//
//
