package com.epost.insu.common;

import android.app.Activity;
import android.content.ContentUris;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.text.InputFilter;
import android.util.DisplayMetrics;
import android.view.accessibility.AccessibilityManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.epost.insu.R;
import com.epost.insu.SharedPreferencesFunc;
import com.epost.insu.activity.IUCOA0M00;
import com.epost.insu.dialog.CustomDialog;

import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.UUID;

/**
 * 공통으로 사용하는 클래스
 * @since     :
 * @version   : 1.5
 * @author    : LSH
 * <pre>
 * ======================================================================
 *          LSH_20170622    최초 등록
 *          LKM_20180814    스마트보험금청구 구현 추가
 *          NJM_20191101    리사이클러뷰 오류로 사진첨부시 앱이 죽는 현상 수정
 *          NJM_20191224    사진첩 첨부시 png 파일 ExifInterface() 제외처리하고, jpeg 썸네일 직접생성으로 변경(getThumbNail)
 * 1.5.2    NJM_20210322    Dilaog 함수 추가
 * 1.5.9    NJM_20210702    [금융인증서 uuid 변경] UUID 생성 로직 변경
 * 1.6.2    NJM_20210802    [2021년 대우 취약점] 2차본 : 부적절한 예외 처리
 * =======================================================================
 * copyright : (주)프로니스정보기술 | http://www.phronis.co.kr
 * </pre>
 */
public final class CommonFunction {

    /**
     * px to dp
     * @param px
     * @param context
     * @return
     */
    public static int convertPixelsToDp(float px, Context context){
        return (int) (px / ((float) context.getResources().getDisplayMetrics().densityDpi / DisplayMetrics.DENSITY_DEFAULT));
    }
    /**
     * 시각 접근성 On / OFF 상태 값 반환
     * @param p_context Context
     * @return          boolean
     */
    public static boolean CF_checkAccessibilityTurnOn(Context p_context){
        AccessibilityManager tmp_accessibleManage = (AccessibilityManager)p_context.getSystemService(Context.ACCESSIBILITY_SERVICE);
        if(tmp_accessibleManage != null && tmp_accessibleManage.isEnabled()) {
            return tmp_accessibleManage.isTouchExplorationEnabled();
        }
        return false;
    }

    /**
     * 커스텀 Alert다이얼로그 호출 함수
     * @param p_context Context
     * @param p_message String
     */
    public static void CF_showCustomAlertDilaog(Context p_context, String p_message){
        CF_showCustomAlertDilaog(p_context, p_message, p_context.getResources().getString(R.string.btn_ok));
    }

    /**
     * 커스텀 Alert다이얼로그 호출 함수
     * @param p_context Context
     * @param p_message String
     * @param p_btnText String
     */
    public static void CF_showCustomAlertDilaog(Context p_context, String p_message, String p_btnText){
        if(p_context != null) {
            CustomDialog tmp_dlg = new CustomDialog(p_context);
            tmp_dlg.show();
            tmp_dlg.CF_setTextContent(p_message);
            tmp_dlg.CF_setSingleButtonText(p_btnText);
        }
    }

    /**
     * 커스텀 Alert다이얼로그 호출 함수
     * @since 1.5.2
     * @param p_context  Context
     * @param p_message  String
     * @param p_listener DialogInterface.OnDismissListener
     */
    public static void CF_showCustomAlertDilaog(Context p_context, String p_message, DialogInterface.OnDismissListener p_listener){
        CustomDialog tmp_dlg = new CustomDialog(p_context);
        tmp_dlg.show();
        tmp_dlg.CF_setTextContent(p_message);
        tmp_dlg.CF_setBackKeyMode(CustomDialog.BackMode.CANCELED);
        tmp_dlg.CF_setSingleButtonText(p_context.getResources().getString(R.string.btn_ok));
        tmp_dlg.setOnDismissListener(p_listener);
    }

    /**
     * 커스텀 다이얼로그 show & Activity 종료
     * @since 2019-02-07 초기생성
     * @param p_message String
     */
    public static void CF_showCustomDialogFinishActivity(final Context p_context, final String p_message){
        CF_showCustomDialogFinishActivity(p_context, p_message,  p_context.getResources().getString(R.string.btn_ok) );
    }

    /**
     * 커스텀 다이얼로그 show & Activity 종료
     * @param p_context Context
     * @param p_message String
     * @param p_btnText String
     */
    public static void CF_showCustomDialogFinishActivity(final Context p_context, final String p_message, final String p_btnText){
        if(p_context != null) {
            Handler mHandler = new Handler(Looper.getMainLooper());
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    CustomDialog tmp_dlg = new CustomDialog(p_context);
                    tmp_dlg.show();
                    tmp_dlg.CF_setTextContent(p_message);
                    tmp_dlg.CF_setSingleButtonText(p_btnText);
                    tmp_dlg.CF_setBackKeyMode(CustomDialog.BackMode.NOTUSE);
                    tmp_dlg.setOnDismissListener(new DialogInterface.OnDismissListener() {
                        @Override
                        public void onDismiss(DialogInterface dialog) {
                            ((Activity) p_context).finish();
                        }
                    });
                }
            }, 0);
        }
    }

    /**
     * 커스텀 다이얼로그 show & Activity 종료 : 기본오류안내 메시지 + [요청메시지]
     * @since 1.5.2 초기생성
     * @param p_message String
     */
    public static void CF_showCustomDialogException(final Context p_context, final String p_message){
        Handler mHandler = new Handler(Looper.getMainLooper());
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                String msg = p_context.getResources().getString(R.string.dlg_error_server_6) + p_message;
                CustomDialog tmp_dlg = new CustomDialog(p_context);
                tmp_dlg.show();
                tmp_dlg.CF_setTextContent(msg);
                tmp_dlg.CF_setSingleButtonText(p_context.getResources().getString(R.string.btn_ok));
                tmp_dlg.CF_setBackKeyMode(CustomDialog.BackMode.NOTUSE);
                tmp_dlg.setOnDismissListener(new DialogInterface.OnDismissListener() {
                    @Override
                    public void onDismiss(DialogInterface dialog) {
                        ((Activity)p_context).finish();
                    }
                });
            }
        }, 0);
    }
    /**
     * 단말기가 태블릿인지 검사하는 함수
     * @param p_context Context
     * @return  if true, 태블릿
     */
    public static boolean CF_isTablet(Context p_context){
        boolean tmp_flagIsXLarge = ((p_context.getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK) == Configuration.SCREENLAYOUT_SIZE_XLARGE);
        boolean tmp_flagIsLarge = ((p_context.getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK) == Configuration.SCREENLAYOUT_SIZE_LARGE);
        return tmp_flagIsXLarge || tmp_flagIsLarge;
    }

    /**
     * Length InputFilter 반환 함수
     * @param p_length  int
     * @return          InputFilter[]
     */
    public static InputFilter[] CF_getInputLengthFilter(int p_length){
        InputFilter[] tmp_filterArray = new InputFilter[2];
        tmp_filterArray[0] = new InputFilter.LengthFilter(p_length);
        tmp_filterArray[1] = new EmojiFilter();

        return tmp_filterArray;
    }

    /**
     * Hex 문자열을 byte[]로 반환
     * @param p_hexStr  String
     * @return          byte[]
     */
    @SuppressWarnings("unused")
    public static byte[] CF_toByteArray(String p_hexStr) {
        int tmp_len = p_hexStr.length();
        byte[] tmp_bytes = new byte[tmp_len/2];

        for(int i = 0 ; i < tmp_len; i+=2){
            tmp_bytes[i/2] = (byte)((Character.digit(p_hexStr.charAt(i),16) <<4) +Character.digit(p_hexStr.charAt(i+1),16));
        }
        return tmp_bytes;
    }

    /**
     * 문자열에서 UTF-8 BOM 문자열을 제거하여 반환 한다.
     * @param p_string BOM 문자열을 제거할 문자열
     * @return BOM 문자열이 제거된 문자열
     */
    public static String CF_removeBOM(String p_string) {
        // --------------------------------------------------------------------------
        // BOM 은 가장 앞에 붙기 때문에 LastIndex 를 검사하여 문자열을 조정하는 방법을 사용함.
        // BOM UTF-8 코드 int 65279
        // --------------------------------------------------------------------------
        int lastIndexBOM = p_string.lastIndexOf(65279);

        if(lastIndexBOM ==4) {
            return p_string.substring(lastIndexBOM + 1);
        }

        return  p_string;
    }


    // #############################################################################################
    //  키패드 관련
    // #############################################################################################
    /**
     * 가상 키패드 Close 함수
     * @param p_context Context
     * @param p_iBinder IBinder
     */
    public static void CF_closeVirtualKeyboard(Context p_context, IBinder p_iBinder) {
        if(p_context != null && p_iBinder != null){
            InputMethodManager im = (InputMethodManager) p_context.getSystemService(Context.INPUT_METHOD_SERVICE);
            im.hideSoftInputFromWindow(p_iBinder, 0);
        }
    }

    /**
     * 가상 키패드 Show 함수
     * @param p_context     Context
     * @param p_editText    EditText
     */
    public static void CF_showVirtualKeyboard(Context p_context, EditText p_editText) {
        if(p_context != null && p_editText != null ){
            p_editText.setFocusable(true);
            p_editText.setFocusableInTouchMode(true);
            p_editText.requestFocus();
            InputMethodManager im = (InputMethodManager) p_context.getSystemService(Context.INPUT_METHOD_SERVICE);
            im.showSoftInput(p_editText, InputMethodManager.SHOW_IMPLICIT);
        }
    }

    // #############################################################################################
    //  이미지 처리 관련
    // #############################################################################################
    /**
     * Uri 의 파일 절대 경로 반환 함수
     * @param p_context Context
     * @param p_uri     Uri
     * @return          String
     */
    @SuppressWarnings("unused")
    public static String CF_getPathOfUri(Context p_context, Uri p_uri){
        String tmp_path;
        String[] tmp_projection = {MediaStore.Images.Media.DATA};

        Cursor tmp_cursor = p_context.getContentResolver().query(p_uri, tmp_projection, null, null, null);
        tmp_cursor.moveToFirst();

        int tmp_colIndex = tmp_cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATA);

        if(tmp_colIndex == -1){
            tmp_path = p_uri.getPath();
        }else{
            tmp_path = tmp_cursor.getString(tmp_colIndex);
        }

        tmp_cursor.close();

        return tmp_path;
    }

    /**
     * Uri 의 파일 절대 경로 반환 함수
     * @param context   Context
     * @param uri       Uri
     * @return          String
     */
    //TODO : 안드로이드시스템갤러리 호출 테스트중 getRealPathFromURI
    public static String getRealPathFromURI(final Context context, final Uri uri) {

        // DocumentProvider
        if (DocumentsContract.isDocumentUri(context, uri)) {

            // ExternalStorageProvider
            if (isExternalStorageDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];
                if ("primary".equalsIgnoreCase(type)) {
                    return Environment.getExternalStorageDirectory() + "/" + split[1];
                } else {
                    String SDcardpath = getRemovableSDCardPath(context).split("/Android")[0];
                    return SDcardpath + "/" + split[1];
                }
            }

            // DownloadsProvider
            else if (isDownloadsDocument(uri)) {
                final String id = DocumentsContract.getDocumentId(uri);
                final Uri contentUri = ContentUris.withAppendedId(Uri.parse("content://downloads/public_downloads"), Long.valueOf(id));

                return getDataColumn(context, contentUri, null, null);
            }

            // MediaProvider
            else if (isMediaDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                Uri contentUri = null;
                if ("image".equals(type)) {
                    contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                } else if ("video".equals(type)) {
                    contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                } else if ("audio".equals(type)) {
                    contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                }

                final String selection = "_id=?";
                final String[] selectionArgs = new String[] { split[1] };

                return getDataColumn(context, contentUri, selection, selectionArgs);
            }
        } else if ("content".equalsIgnoreCase(uri.getScheme())) {
            // Return the remote address
            if (isGooglePhotosUri(uri))
                return uri.getLastPathSegment();


            return getDataColumn(context, uri, null, null);

        } else if ("file".equalsIgnoreCase(uri.getScheme())) {
            return uri.getPath();
        }
        return null;
    }

    public static String getRemovableSDCardPath(Context context) {
        File[] storages = ContextCompat.getExternalFilesDirs(context, null);
        if (storages.length > 1 && storages[0] != null && storages[1] != null)
            return storages[1].toString();
        else
            return "";
    }

    public static String getDataColumn(Context context, Uri uri, String selection, String[] selectionArgs) {
        Cursor cursor = null;
        final String column = "_data";
        final String[] projection = { column };

        try {
            cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs, null);
            if (cursor != null && cursor.moveToFirst()) {
                final int index = cursor.getColumnIndexOrThrow(column);
                return cursor.getString(index);
            }
        } catch (NullPointerException e) {
            e.getMessage();
        } catch (Exception e) {
            e.getMessage();
        } finally {
            if (cursor != null)
                cursor.close();
        }
        return null;
    }

    public static boolean isExternalStorageDocument(Uri uri) {
        return "com.android.externalstorage.documents".equals(uri.getAuthority());
    }

    public static boolean isDownloadsDocument(Uri uri) {
        return "com.android.providers.downloads.documents".equals(uri.getAuthority());
    }

    public static boolean isMediaDocument(Uri uri) {
        return "com.android.providers.media.documents".equals(uri.getAuthority());
    }

    public static boolean isGooglePhotosUri(Uri uri) {
        return "com.google.android.apps.photos.content".equals(uri.getAuthority());
    }

    /**
     * 파일 사이즈를 읽기 쉬운 문자열로 변환<br/>
     * @param size  long
     * @return      String
     */
    @SuppressWarnings("unused")
    public static String CF_readableFileSize(long size) {
        if(size <= 0) return "0";
        final String[] units = new String[] { "B", "kB", "MB", "GB", "TB" };
        int digitGroups = (int) (Math.log10(size)/Math.log10(1024));
        return new DecimalFormat("#,##0.#").format(size/Math.pow(1024, digitGroups)) + " " + units[digitGroups];
    }

    /**
     * 비트맵 회전 시키는 함수
     * 		: 비트맵을 생성하고 하는 작업은 메모리를 많이 차지 하므로 synchronized 함수를 통해 한번에 하나의 작업만 처리 하도록 한다.
     * @param   p_bitmap    Bitmap
     * @param   p_degree    p_degree
     * @return  Bitmap
     */
    public synchronized static Bitmap CF_rotateBitmap(Bitmap p_bitmap, int p_degree) {
        if(p_degree != 0 && p_bitmap != null)
        {
            Matrix tmp_matrix = new Matrix();
            tmp_matrix.setRotate(p_degree,(float)p_bitmap.getWidth()/2,(float)p_bitmap.getHeight()/2);

            try
            {
                Bitmap tmp_bitmapRotated = Bitmap.createBitmap(p_bitmap,0,0,p_bitmap.getWidth(),p_bitmap.getHeight(),tmp_matrix,true);

                if(tmp_bitmapRotated != p_bitmap)
                {
                    p_bitmap.recycle();
                    p_bitmap = tmp_bitmapRotated;
                }
            } catch(OutOfMemoryError e) {
                LogPrinter.CF_line();
                LogPrinter.CF_debug("[me01] Bitmap 생성 실패 : 메모리 부족");
            }
        }

        return p_bitmap;
    }

    /**
     * 저장된 이미지의 회전 방향을 반환한다.
     * jpeg/jpg일때만 exifInterFace 사용가능함
     * <pre>
     * 2019-12-24    노지민     : ExifInterface는 png/gif는 안되어 jpg만 가능하도록 수정
     * </pre>
     *
     * @param   p_filePath    String
     * @return  tmp_degree    int
     */
    public static int CF_getImageOrientation(String p_filePath) {
        LogPrinter.CF_debug("!----------------------------------------------------------");
        LogPrinter.CF_debug("!-- CommonFunction.CF_getImageOrientation()");
        LogPrinter.CF_debug("!----------------------------------------------------------");
        LogPrinter.CF_debug("!---- p_filePath :" + p_filePath );

        int tmp_degree = 0;
        // ExifInterface는 png/gif는 안되어 jpg만 가능하도록 수정
        if ( p_filePath.endsWith("jpg") || p_filePath.endsWith("jpeg") ) {
            LogPrinter.CF_debug("!---- 이미지 회전방향 확인");
            ExifInterface exifInterFace = null;
            try {
                exifInterFace = new ExifInterface(p_filePath);
            } catch (IOException e) {
                LogPrinter.CF_line();
                LogPrinter.CF_debug("!---- 이미지 정보 획득 실패 : " + p_filePath);
            }

            if (exifInterFace != null) {
                int tmp_orientation = exifInterFace.getAttributeInt(ExifInterface.TAG_ORIENTATION, -1);

                if (tmp_orientation != -1) {
                    switch (tmp_orientation) {
                        case ExifInterface.ORIENTATION_ROTATE_90:
                            tmp_degree = 90;
                            break;
                        case ExifInterface.ORIENTATION_ROTATE_180:
                            tmp_degree = 180;
                            break;
                        case ExifInterface.ORIENTATION_ROTATE_270:
                            tmp_degree = 270;
                            break;
                        default:
                            break;
                    }
                }
            }
        }
        return tmp_degree;
    }

    /**
     * ImageView의 Bitmap recycle 처리 함수
     * @param iv        ImageView
     * <pre>
     * 2019-11-01    노지민     : 리사이클러뷰 오류로 사진첨부시 앱이 죽는 현상 수정
     * </pre>
     */
    public static void CF_recycleBitmap(ImageView iv) {
        try {
           // Drawable tmp_drawable = iv.getDrawable();

            if(iv.getDrawable() instanceof BitmapDrawable) {
              //  Bitmap tmp_bitmap = ((BitmapDrawable)tmp_drawable).getBitmap();

                if(((BitmapDrawable)iv.getDrawable()).getBitmap() != null) {
                    ((BitmapDrawable)iv.getDrawable()).getBitmap().recycle();
                }
            }
            iv.getDrawable().setCallback(null);

        } catch (NullPointerException e) {
            LogPrinter.CF_line();
            LogPrinter.CF_debug("Bitmap 자원 해제 실패1 : is Null");
        }
    }

    // #############################################################################################
    // -- App Info
    // #############################################################################################
    /**
     * App Version Name 반환
     * @param p_context Context
     * @return          String
     */
    public static String CF_getVersionName(Context p_context){
        String tmp_versionName = "";

        try {
            tmp_versionName = p_context.getPackageManager().getPackageInfo(p_context.getPackageName(),0).versionName;
        } catch (PackageManager.NameNotFoundException e) {
            LogPrinter.CF_debug("[PS01]패키지명 조회 실패 : CommonFunction");
        }

        return tmp_versionName;
    }

    /**
     * dp 값을 pixel 값으로 반환 하는 함수<br/>
     * @param p_context Context
     * @param p_dp      float
     * @return          int
     */
    public static int CF_convertDipToPixel(Context p_context, float p_dp){
        return (int)(p_dp *(p_context.getResources().getDisplayMetrics().density));
    }

    /**
     * 해당 퍼미션에 대해 사용자 접근 동의 상태를 검사한다.
     * @param p_context     Context
     * @param p_permission  String
     * @return  if true, 접근 동의함.
     */
    public static boolean CF_checkAgreePermission(Context p_context, String p_permission){
        boolean tmp_flagHas = false;
        if(ContextCompat.checkSelfPermission(p_context, p_permission) == PackageManager.PERMISSION_GRANTED){
            tmp_flagHas = true;
        }
        return tmp_flagHas;
    }

    /**
     * UUID 생성해 반환하는 함수<br/>
     * 기본적으로 ANDROID_ID 값을 사용하며, TelephonyManger를 이용해 생성 한다.
     * @param p_context     Context
     * @return              String
     */
    public static String CF_getUUID(Context p_context) {
        LogPrinter.CF_debug("!-----------------------------------------------------------");
        LogPrinter.CF_debug("!-- CommonFunction.CF_getUUID()");
        LogPrinter.CF_debug("!-----------------------------------------------------------");

        String uuidStr = SharedPreferencesFunc.getUuid(p_context);
        if("".equals(uuidStr)) {
            LogPrinter.CF_debug("!---- 저장된 SharedPreferencesFunc UUID : " + uuidStr);
            uuidStr = UUID.randomUUID().toString();
            SharedPreferencesFunc.setUuid(p_context, uuidStr);
        }
        return uuidStr;
    }
//
//
//    /**
//     * UUID 생성해 반환하는 함수<br/>
//     * 기본적으로 ANDROID_ID 값을 사용하며, TelephonyManger를 이용해 생성 한다.
//     * @param p_context     Context
//     * @return              String
//     */
//    public static String CF_getUUID(Context p_context) {
//        LogPrinter.CF_debug("!-----------------------------------------------------------");
//        LogPrinter.CF_debug("!-- CommonFunction.CF_getUUID()");
//        LogPrinter.CF_debug("!-----------------------------------------------------------");
//        UUID deviceUuid;
//        String rtnDeviceIdStr = null;
//
//        // --<1> Q(29)이상일 때 UUID가져오는 방식
//        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
//            deviceUuid = new UUID(-0x121074568629b532L, -0x5c37d8232ae2de13L);
//            try {
//                // DRM - WIDEVINE_UUID(앱 삭제 or 데이터 삭제해도 변경x)
//                MediaDrm mediaDrm = new MediaDrm(deviceUuid);
//                rtnDeviceIdStr = android.util.Base64.encodeToString(mediaDrm.getPropertyByteArray(MediaDrm.PROPERTY_DEVICE_UNIQUE_ID), 0).trim();
//
//            } catch (UnsupportedSchemeException e) {
//                e.printStackTrace();
//            }
//        }
//        // --<1> 기존 UUID 조합해서 사용하는 방식
//        else {
//            int permissionCheck = ContextCompat.checkSelfPermission(p_context, Manifest.permission.READ_PHONE_STATE);
//            if(permissionCheck== PackageManager.PERMISSION_DENIED){
//                // 권한 없음
//                return "";
//            }else{
//                // 권한 있음
//                TelephonyManager tm = (TelephonyManager) p_context.getSystemService(Context.TELEPHONY_SERVICE);
//
//                String tmDevice, tmSerial, androidId;
//
//                //TODO : uuid 가져오는부분 에러 임시 공백처리
//                tmDevice  = "" + tm.getDeviceId();
//                tmSerial  = "" + tm.getSimSerialNumber();
//
////                tmDevice  = "";
////                tmSerial  = "";
//
//                androidId = "" + android.provider.Settings.Secure.getString(p_context.getContentResolver(), android.provider.Settings.Secure.ANDROID_ID);
//
//                deviceUuid  = new UUID(androidId.hashCode(), ((long)tmDevice.hashCode() << 32) | tmSerial.hashCode());
//                rtnDeviceIdStr = deviceUuid.toString();
//            }
//        }
//
//        return rtnDeviceIdStr;
//    }

    /**
     * 20181120 형태의 날짜 형식이 정확한지 검사
     * @param date  String
     * @return      boolean
     */
    public static boolean CF_isValidDate(String date){
        final String DATE_FORMAT = "yyyyMMdd";

        try{
            DateFormat df = new SimpleDateFormat(DATE_FORMAT);
            df.setLenient(false);
            df.parse(date);

            return true;
        } catch (ParseException e){

            return false;
        }
    }

    /**
     * App 재시작
     * @param p_context Context
     */
    public static void CF_restartApp(Context p_context){
        Intent tmp_intent = new Intent(p_context, IUCOA0M00.class);
        tmp_intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        tmp_intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        p_context.startActivity(tmp_intent);
        if(p_context instanceof Activity){
            ActivityCompat.finishAffinity((Activity) p_context);
        }
        Runtime.getRuntime().exit(0);
    }

    public static String CF_intentToString(Intent intent) {
        String rtnStr = "";
        if(intent != null) {
            Bundle bundle = intent.getExtras();

            for(String _key : bundle.keySet()) {
                rtnStr += _key + " : " + bundle.get(_key) + " / ";
            }
        }

        return rtnStr;
    }
}