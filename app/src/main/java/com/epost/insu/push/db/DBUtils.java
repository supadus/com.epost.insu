//package com.epost.insu.push.db;
//
//
//import android.content.Context;
//
//import java.util.Calendar;
//
//
//public class DBUtils {
//
//
//	private static DbOpenHelper mDbOpenHelper;
//	/**
//	 * 데이타베이스를 체크해서 존재하지 않으면 OBJECT를 생성한다.
//	 * CBS 데이타베이스 관련처리는 공통으로 이용한다.
//	 * @param context Context
//	 * @return 데이타OBJECT를 반환한다  DbOpenHelper
//	 */
//	public static DbOpenHelper getDbOpenHelper(Context context) {
//		if (mDbOpenHelper == null) {
//			mDbOpenHelper = new DbOpenHelper(context);
//			mDbOpenHelper.open();
//		}
//		return mDbOpenHelper;
//	}
//
//	/**
//	 * 시분초를 HH, MM, SS 문자열 배열로 반환한다.
//	 * @return
//	 */
//	public static String[] getStrArrNowTime() {
//		Calendar oCalendar = Calendar.getInstance();
//
//		int iHour = oCalendar.get(Calendar.HOUR_OF_DAY);
//		String hour = (iHour < 10) ? "0" + iHour : "" + iHour;
//
//		int iMinute = oCalendar.get(Calendar.MINUTE);
//		String minute = (iMinute < 10) ? "0" + iMinute : "" + iMinute;
//
//		int iSecond = oCalendar.get(Calendar.SECOND);
//		String second = (iSecond < 10) ? "0" + iSecond : "" + iSecond;
//
//		String[] strNowTime = { hour, minute, second };
//		return strNowTime;
//	}
//
//	/**
//	 * 현재 날짜를 YYYYMMDD로 반환한다.
//	 * @return
//	 */
//	public static String getStrNowDate() {
//		Calendar oCalendar = Calendar.getInstance();
//
//		String year = "" + oCalendar.get(Calendar.YEAR);
//		String month = "";
//		String date = "";
//
//		int m = oCalendar.get(Calendar.MONTH) + 1;
//		int d = oCalendar.get(Calendar.DAY_OF_MONTH);
//
//		month = (m < 10) ? "0" + m : "" + m;
//		date = (d < 10) ? "0" + d : "" + d;
//		String[] time = getStrArrNowTime();
//		return year +"년"+ month +"월"+ date+"일"+time[0]+"시"+time[1]+"분"+time[2]+"초";
//	}
//
//
//}
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
