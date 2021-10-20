package com.epost.insu.activity.auth

import android.content.Intent
import android.view.View
import android.widget.ImageButton
import android.widget.TextView
import com.epost.insu.R
import com.epost.insu.activity.Activity_Default
import com.epost.insu.common.UiUtil

/**
 * 인증센터 > 간편인증서비스안내
 * @since     : project 48:1.4.5
 * @version   : 1.1
 * @author    : NJM
 * <pre>
 * ======================================================================
 * 1.0  NJM_20201229    최초 등록
 * =======================================================================
 * copyright : (주)프로니스정보기술 | http://www.phronis.co.kr
</pre> *
 */
class IUFC31M00 : Activity_Default() {

    override fun setInit() {
        setContentView(R.layout.iufc31m00)
    }

    override fun setUIControl() {

        // 타이틀바 세팅
        setTitleBarUI()

/*
        final LinearLayout tmp_linBtnBio = (LinearLayout)findViewById(R.id.linBioAuth);
        tmp_linBtnBio.setOnClickListener(new View.OnClickListener() {
            @Override
            void onClick(View v) {

                // 바이오 연계앱 설치 확인
                if(FingerprintCommon.CF_checkLinkAppInstalled(IUFC31M00.this)){
                    showCustomDialog(getResources().getString(R.string.dlg_prev_down_bio_app), tmp_linBtnBio);
                }
                else{
                    if(CommonFunction.CF_checkAccessibilityTurnOn(IUFC31M00.this)) {
                        // -------------------------------------------------------------------------
                        //  접근성을 위한 플레이스토어 마켓이동 다이얼로그 안내
                        // -------------------------------------------------------------------------
                        showCustomDialog(getResources().getString(R.string.dlg_accessible_move_market)
                                , tmp_linBtnBio
                                , new DialogInterface.OnDismissListener() {
                            @Override
                            void onDismiss(DialogInterface dialog) {
                                if(((CustomDialog)dialog).CF_getCanceled() == false) {
                                    FingerprintCommon.CF_showLinkAppMarketPage(IUFC31M00.this);
                                }else{
                                    clearAllFocus();
                                    tmp_linBtnBio.requestFocus();
                                }
                                tmp_linBtnBio.setFocusableInTouchMode(false);
                            }
                        });
                    }else{
                        FingerprintCommon.CF_showLinkAppMarketPage(IUFC31M00.this);
                    }
                }
            }
        });
*/

//        // -- 지문인증등록 버튼
//        Button tmp_btnReg = findViewById(R.id.btnFill);
//        tmp_btnReg.setText(getResources().getString(R.string.btn_reg_finger));
//        tmp_btnReg.setOnClickListener(new View.OnClickListener() {
//            @Override
//            void onClick(View v) {
//                //startIUPC32M00();
//            }
//        });
    }

    /**
     * 타이틀바 UI 세팅 함수
     */
    private fun setTitleBarUI() {
        // 타이틀 세팅
        val tmp_title: TextView = findViewById(R.id.title_bar_textTitle)
        tmp_title.setText(getResources().getString(R.string.title_noti_bio))

        val title_main: TextView = findViewById(R.id.title_main)
        UiUtil.setTitleColor(title_main,getString(R.string.label_iufc30m00_title1),4,8)



        // left 버튼 세팅
        val tmp_btnLeft: ImageButton = findViewById(R.id.title_bar_imgBtnLeft)
        tmp_btnLeft.setVisibility(View.VISIBLE)
        tmp_btnLeft.setOnClickListener(object : View.OnClickListener {
            override fun onClick(v: View) {
                finish()
            }
        })
    }
}