package com.epost.insu.dialog;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.StyleRes;

import com.epost.insu.R;
import com.epost.insu.common.CommonFunction;
import com.epost.insu.common.WebBrowserHelper;

/**
 * 공지사항 팝업
 * @since     : project 50:1.5.0
 * @version   : 1.0
 * @author    : NJM
 * <pre>
 * ======================================================================
 * 1.0  NJM_20210201    최초 등록
 * =======================================================================
 * copyright : (주)프로니스정보기술 | http://www.phronis.co.kr
 * </pre>
 */
public class CustomPopup extends Dialog{
    /**
     * 생성자
     * @param context
     */
    public CustomPopup(@NonNull Context context) {
        super(context);

        setInit();
        setUIControl();
    }
    public CustomPopup(@NonNull Context context, @StyleRes int themeResId) {
        super(context, themeResId);

        setInit();
        setUIControl();
    }

    @Override
    public void onBackPressed() {
        //super.onBackPressed();
        // nothing
    }

    /**
     * 초기 세팅 함수
     */
    private void setInit(){
        WindowManager.LayoutParams windowManagerLayoutParams = new WindowManager.LayoutParams();
        windowManagerLayoutParams.flags = WindowManager.LayoutParams.FLAG_DIM_BEHIND;
        windowManagerLayoutParams.dimAmount = 0.8f;
        getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        getWindow().setAttributes(windowManagerLayoutParams);

        setCanceledOnTouchOutside(false);


        setContentView(R.layout.custom_popup);
    }

    /**
     * UI 생성 및 세팅 함수
     */
    private void setUIControl(){
        // 가로 사이즈 재조정
        resizeWidth();

        Button tmp_btnClose = findViewById(R.id.btnFill);
        tmp_btnClose.setText(getContext().getResources().getString(R.string.btn_ok));
        tmp_btnClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });

        //WebBrowserHelper.startWebViewActivity();
    }

    /**
     * 가로 사이즈 재조정
     */
    private void resizeWidth(){
        LinearLayout tmp_rootView = findViewById(R.id.rootView);
        ViewGroup.LayoutParams tmp_lp = tmp_rootView.getLayoutParams();

        if(CommonFunction.CF_isTablet(getContext())){
            tmp_lp.width = (int)(getContext().getResources().getDisplayMetrics().widthPixels * 0.8);
        }
        else{
            tmp_lp.width = (int)(getContext().getResources().getDisplayMetrics().widthPixels * 0.9);
        }
    }
}
