package com.epost.insu.push.db;

import android.provider.BaseColumns;

// DataBase Table
public final class DataBases {
	
	public static final class CreateDB implements BaseColumns {
		
		public static final String PUSHMSG_TABLENAME = "push_msg";
		public static final String COL_MSGKEY = "msgkey";
		public static final String COL_MSGDATA = "msgdata";
		public static final String COL_EXTDATA = "extdata";
		public static final String COL_ENCRYPTDATA = "encryptdata";
		public static final String COL_DATE = "date";
				
		public static final String PUSHMSG_CREATE = 
				"CREATE TABLE "+PUSHMSG_TABLENAME+"(" 
						+ _ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " 	
						+  COL_MSGKEY + " TEXT UNIQUE NOT NULL , "
						+  COL_MSGDATA + " TEXT NOT NULL , "
						+  COL_EXTDATA + " TEXT NOT NULL , "
						+  COL_ENCRYPTDATA + " TEXT , "
						+  COL_DATE + ");";
	}
}
