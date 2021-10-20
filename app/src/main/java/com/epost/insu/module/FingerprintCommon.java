package com.epost.insu.module;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.net.Uri;

import java.util.List;

/**
 *
 */
public class FingerprintCommon {

    public static final String LNK_PACKAGE_NAME = "org.kftc.fido.lnk.lnk_app";

    /**
     * 금융결제원 FIDO LINK APP 설치여부 확인
     * @param context
     * @return
     */
    public static boolean CF_checkLinkAppInstalled(Context context)  {

        boolean tmp_flagInstalled = false;

        List<PackageInfo> packageInfos = context.getPackageManager().getInstalledPackages(0);
        for(PackageInfo packageInfo : packageInfos) {

            if(packageInfo.packageName.equals(LNK_PACKAGE_NAME)) {
                tmp_flagInstalled = true;
                break;
            }
        }

        return tmp_flagInstalled;
    }
    /**
     * 금융결제원 FIDO LINK APP 다운로드 마켓 이동
     * @param p_context
     */
    public static  void CF_showLinkAppMarketPage(Context p_context){
        Intent tmp_intentUpdate = new Intent(Intent.ACTION_VIEW);
        tmp_intentUpdate.setData(Uri.parse("market://details?id="+LNK_PACKAGE_NAME));
        p_context.startActivity(tmp_intentUpdate);
    }
}
