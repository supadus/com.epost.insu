package com.epost.insu.common;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;

import com.epost.insu.EnvConfig;
import com.epost.insu.R;
import com.epost.insu.activity.Activity_WebView;
import com.epost.insu.activity.IUCOA0M00;
import com.epost.insu.dialog.CustomDialog;

import java.net.URLEncoder;

/**
 * <br/> copyright : (주)프로니스정보기술 | http://www.phronis.co.kr
 * <br/> project   : 모바일슈랑스 구축
 * <br/> Title     : 웹페이지 처리 관련 Helper
 *
 * <pre>
 * ======================================================================
 * 수정 내역
 * 0.0.0    NJM_20191216    최초 등록
 * 1.6.2    NJM_20210802    [2021년 대우 취약점] 2차본 : 부적절한 예외 처리
 * =======================================================================
 * </pre>
 *
 * @version   : 1.0
 * @author    : 노지민
 */
public final class WebBrowserHelper {

    /**
     * 웹뷰 호출
     * @param p_context         Context
     * @param webViewDvsn       int         레이아웃유형(0:전체화면, 1:상단바)
     * @param allowNewWindow    boolean     새창(차일드뷰) 허용(false:불가, true:허용)
     * @param url               String      이동할 웹 url
     * @param viewTitle         String      웹뷰 타이틀
     */
    public static void startWebViewActivity( Context p_context, int webViewDvsn, boolean allowNewWindow, String  url, String viewTitle) {
        LogPrinter.CF_debug("!----------------------------------------------------------");
        LogPrinter.CF_debug("!-- WebBrowserHelper.startWebViewActivity()");
        LogPrinter.CF_debug("!----------------------------------------------------------");
        LogPrinter.CF_debug("!---- p_context::::::"+ p_context);
        LogPrinter.CF_debug("!---- webViewDvsn::::"+ webViewDvsn);
        LogPrinter.CF_debug("!---- allowNewWindow:"+ allowNewWindow);
        LogPrinter.CF_debug("!---- url::::::::::::"+ url);
        LogPrinter.CF_debug("!---- viewTitle::::::"+ viewTitle);

        // -- hostUrl 체크
        url = hostUrlCheck(url);

        // -- startActivity

        if(p_context instanceof IUCOA0M00){
            ((IUCOA0M00) p_context).getMWebview().loadUrl(url);



        }
        else {
            Intent intent = new Intent(p_context, Activity_WebView.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.putExtra("webViewDvsn", webViewDvsn);
            intent.putExtra("allowNewWindow", allowNewWindow);
            intent.putExtra("url", url);
            intent.putExtra("viewTitle", viewTitle);
            p_context.startActivity(intent);
        }
    }

    /**
     * 일반 브라우저 호출
     * @param p_context     Context
     * @param url           String
     */
    public static void callWebBrowser(Context p_context, String url){
        LogPrinter.CF_debug("!----------------------------------------------------------");
        LogPrinter.CF_debug("!-- WebBrowserHelper.callWebBrowser()");
        LogPrinter.CF_debug("!----------------------------------------------------------");

        //startWebViewActivity(p_context,0,true,url,"");

        url = hostUrlCheck(url);            // hostUrl 체크
        try {
            LogPrinter.CF_debug("!---- 브라우저 호출 URL(uri.parse):::::"+ Uri.parse(url));
            LogPrinter.CF_debug("!---- 브라우저 호출 URL(URLEncoder)::::"+ URLEncoder.encode(url,"utf-8"));
        } catch (NullPointerException e) {
            LogPrinter.CF_debug("!---- 브라우저 호출 URL(URLEncoder) (NPE)");
            e.getMessage();
        } catch (Exception e){
            LogPrinter.CF_debug("!---- 브라우저 호출 URL(URLEncoder) 인코딩 에러!!");
            e.getMessage();
        }

        boolean bNavigated = false;

        for(int i=0; i < EnvConfig.webBrowserPackages.length; i++){
            final String packageName = EnvConfig.webBrowserPackages[i];

            PackageInfo packInfo = null;
            try{
                packInfo = p_context.getPackageManager().getPackageInfo(packageName, PackageManager.GET_META_DATA);
            } catch(PackageManager.NameNotFoundException e){
                LogPrinter.CF_line();
                LogPrinter.CF_debug("NameNotFoundException", e.toString());
            }

            PackageInfo pi = packInfo;
            if(pi != null){ //!pi.equals(null)){
                try{
                    // 다음 - daumapps://web?url=[웹 URL]&loginDaumId=[Daum ID]
                    // 요청한 주소로 이동
                    if("com.nhn.android.search".equals(packageName)){
                        Intent tmpIntent = new Intent(Intent.ACTION_VIEW);
                        tmpIntent.addCategory(Intent.CATEGORY_DEFAULT);
                        tmpIntent.addCategory(Intent.CATEGORY_BROWSABLE);
                        String encUrl = URLEncoder.encode(url,"utf-8");
                        tmpIntent.setData(Uri.parse("naversearchapp://inappbrowser?url="+encUrl+"&target=replace&version=6"));
                        p_context.startActivity(tmpIntent);
                        bNavigated = true;
                    } else if("net.daum.android.daum".equals(packageName)){
                        Intent tmpIntent = new Intent(Intent.ACTION_VIEW);
                        tmpIntent.addCategory(Intent.CATEGORY_DEFAULT);
                        tmpIntent.addCategory(Intent.CATEGORY_BROWSABLE);
                        String encUrl = URLEncoder.encode(url,"utf-8");
                        tmpIntent.setData(Uri.parse("daumapps://web?url="+encUrl+"&loginDaumId"));
                        p_context.startActivity(tmpIntent);
                        bNavigated = true;
                    } else if("com.sec.android.app.sbrowser".equals(packageName) || "com.samsung.android.sm".equals(packageName)){
                        Intent tmpIntent = new Intent(Intent.ACTION_VIEW);
                        tmpIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        //tmpIntent.addCategory(Intent.CATEGORY_DEFAULT);
                        //tmpIntent.addCategory(Intent.CATEGORY_BROWSABLE);
                        tmpIntent.setPackage(packageName);
                        tmpIntent.setData(Uri.parse(url));
                        p_context.startActivity(tmpIntent);
                        bNavigated = true;
                    } else{
                        Intent tmpIntent = p_context.getPackageManager().getLaunchIntentForPackage(packageName);
                        tmpIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        tmpIntent.setPackage(packageName);
                        tmpIntent.setData(Uri.parse(url));
                        p_context.startActivity(tmpIntent);
                        bNavigated = true;
                    }
                } catch (NullPointerException e) {
                    e.getMessage();
                } catch (Exception e){
                    e.getMessage();
                }
            }
            if(bNavigated) break;
        }

        // -- 브라우저가 없을 경우
        if(!bNavigated){
            try{
                // 요청한 주소로 이동
                Intent tmpIntent = new Intent(Intent.ACTION_VIEW);
                tmpIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                tmpIntent.setData(Uri.parse(url));
                p_context.startActivity(tmpIntent);
            } catch (NullPointerException e) {
                e.getMessage();
            } catch (Exception e){
                e.getMessage();

                CustomDialog tmp_dlg = new CustomDialog(p_context);
                tmp_dlg.show();
                tmp_dlg.CF_setBackKeyMode(CustomDialog.BackMode.NOTUSE);
                tmp_dlg.CF_setTextContent("브라우저 설정이 되어있지 않습니다.\n우체국보험 서비스 이용을 위해서는 브라우저가 필요합니다.");
                tmp_dlg.CF_setSingleButtonText(p_context.getResources().getString(R.string.btn_ok));
                tmp_dlg.setOnDismissListener(new DialogInterface.OnDismissListener() {
                    @Override
                    public void onDismiss(DialogInterface dialog) {
                        //CF_showTransKeyPad();
                        //if(CommonFunction.CF_checkAccessibilityTurnOn(getActivity())){
                        //   CF_setAccessibleFocusInputBox();
                        //}
                    }
                });
                LogPrinter.CF_line();
                LogPrinter.CF_debug(p_context.getResources().getString(R.string.log_not_found_activity_browser));
            }
        }


    }


    /**
     * host url 유무 체크 후 url 세팅 리턴
     * @param url   String
     * @return      String
     */
    private static String hostUrlCheck(String url) {
        // host URL이 없으면
        if(!url.startsWith("https:") && !url.startsWith("http")) {
            url = EnvConfig.host_url + url;
        }
        return url;
    }


    /**
     * 크롬브라우저를 기본으로 URL호출
     * 크롬브라우저가 없으면 마켓으로 이동
     * @param url
     */
    /*
    public void callChrome(String url, String pkgName){

        final String packageName = pkgName;

        PackageInfo pi = checkInstalledEpostPackage(packageName);

        if(pi == null){
            // 마켓이동
            CustomDialog tmp_dlg = new CustomDialog(this);
            tmp_dlg.show();
            tmp_dlg.CF_setBackKeyMode(CustomDialog.BackMode.NOTUSE);
            tmp_dlg.CF_setTextContent(getResources().getString(R.string.dlg_need_chrome_app));
            tmp_dlg.CF_setDoubleButtonText(getResources().getString(R.string.btn_no), getResources().getString(R.string.btn_yes));
            tmp_dlg.setOnDismissListener(new DialogInterface.OnDismissListener() {
                @Override
                public void onDismiss(DialogInterface dialog) {
                    if(((CustomDialog)dialog).CF_getCanceled()==false){

                        // -----------------------------------------------------------------------------
                        //  App 업데이트 마켓 이동
                        // -----------------------------------------------------------------------------
                        //Intent tmp_intent = new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id="+getPackageName()));
                        //startActivity(tmp_intent);
                        IUCOA0M00.gotoMarket(IUCOA0M00.this, packageName);
                    }
                }
            });
        } else if(!pi.equals(null)){
            try{
                // 요청한 주소로 이동
                Intent i = getPackageManager().getLaunchIntentForPackage(packageName);
                i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                i.setPackage(packageName);
                i.setData(Uri.parse(url));
                startActivity(i);
            } catch (Exception e){
                e.printStackTrace();

                CustomDialog tmp_dlg = new CustomDialog(this);
                tmp_dlg.show();
                tmp_dlg.CF_setBackKeyMode(CustomDialog.BackMode.NOTUSE);
                tmp_dlg.CF_setTextContent(getResources().getString(R.string.dlg_unuse_chrome_app));
                tmp_dlg.CF_setSingleButtonText(getResources().getString(R.string.btn_ok));
                tmp_dlg.setOnDismissListener(new DialogInterface.OnDismissListener() {
                    @Override
                    public void onDismiss(DialogInterface dialog) {
                        //CF_showTransKeyPad();

                        //if(CommonFunction.CF_checkAccessibilityTurnOn(getActivity())){
                         //   CF_setAccessibleFocusInputBox();
                        //}
                    }
                });

            }
        }
    }
    */

    /**
     * 패키지의정보를 조회한다.
     * 설치된 패키지가 없으면 널을 리턴한다.
     * @param packageName 패키지명
     * @return packInfo
     */
/*
    protected PackageInfo checkInstalledEpostPackage(Context context, String packageName){
        PackageInfo packInfo = null;
        try{
            packInfo = context.getPackageManager().getPackageInfo(packageName, PackageManager.GET_META_DATA);
        } catch(PackageManager.NameNotFoundException e){
            LogPrinter.CF_line();
            LogPrinter.CF_debug("NameNotFoundException", e.toString());
        }
        return packInfo;
    }
*/

    /**
     * 마켓으로 이동
     * @param ctxt 컨텍스트
     * @param appName 어플리케이션명
     */
/*
    public static void gotoMarket(Context ctxt, String appName){
        Uri marketUri = Uri.parse("market://details?id=" + appName);
        Intent marketIntent = new Intent(Intent.ACTION_VIEW).setData(marketUri);
        ctxt.startActivity(marketIntent);
    }
*/

}
