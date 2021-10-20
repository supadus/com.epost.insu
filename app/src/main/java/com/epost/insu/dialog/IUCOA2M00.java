package com.epost.insu.dialog;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import androidx.annotation.NonNull;
import androidx.annotation.StyleRes;
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

import com.epost.insu.R;
import com.epost.insu.common.CommonFunction;

/**
 * @copyright : (주)프로니스정보기술 | http://www.phronis.co.kr
 *
 * @project   : 모바일슈랑스 구축
 * @pakage   : com.epost.insu.dialog
 * @fileName  : IUCOA2M00.java
 *
 * @Title     : 공통 > 앱권한안내 > 앱권한안내 (화면 ID : IUCOA2M00) - #50
 * @author    : 이수행
 * @created   : 2017-08-22
 * @version   : 1.0
 *
 * @note      : <u>공통 > 앱권한안내 > 앱권한안내 (화면 ID : IUCOA2M00) - #50</u><br/>
 *               App 접근권한 안내 다이얼로그<br/>
 *               필수 접근 권한 , 선택 접근 권한 구분하여 안내한다.
 * ======================================================================
 * 수정 내역
 * NO      날짜          작업자       내용
 * 01      2017-09-06    이수행       최초 등록
 * =======================================================================
 */
public class IUCOA2M00 extends Dialog{

    /**
     * 생성자
     * @param context
     */
    public IUCOA2M00(@NonNull Context context) {
        super(context);

        setInit();
        setUIControl();
    }
    public IUCOA2M00(@NonNull Context context, @StyleRes int themeResId) {
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


        setContentView(R.layout.iucoa2m00);
    }

    /**
     * UI 생성 및 세팅 함수
     */
    private void setUIControl(){

        // 가로 사이즈 재조정
        resizeWidth();

        TextView tmp_textStorageLabel = (TextView)findViewById(R.id.textStorageLabel);
        TextView tmp_textPhoneLabel = (TextView)findViewById(R.id.textPhoneLabel);

        Spannable tmp_spannableStorage = new SpannableString(getContext().getResources().getString(R.string.label_permission_storage));
        tmp_spannableStorage.setSpan(new ForegroundColorSpan(Color.rgb(254,0,0)), 0,4, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        tmp_textStorageLabel.setText(getContext().getResources().getString(R.string.label_permission_storage));

        Spannable tmp_spannablePhone = new SpannableString(getContext().getResources().getString(R.string.label_permission_phone));
        tmp_spannablePhone.setSpan(new ForegroundColorSpan(Color.rgb(254,0,0)), 0,4, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        tmp_textPhoneLabel.setText(getContext().getResources().getString(R.string.label_permission_phone));

        Button tmp_btnClose = (Button)findViewById(R.id.btnFill);
        tmp_btnClose.setText(getContext().getResources().getString(R.string.btn_ok));
        tmp_btnClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });
    }

    /**
     * 가로 사이즈 재조정
     */
    private void resizeWidth(){

        LinearLayout tmp_rootView = (LinearLayout)findViewById(R.id.rootView);
        ViewGroup.LayoutParams tmp_lp = tmp_rootView.getLayoutParams();

        if(CommonFunction.CF_isTablet(getContext())){
            tmp_lp.width = (int)(getContext().getResources().getDisplayMetrics().widthPixels * 0.6);
        }
        else{
            tmp_lp.width = (int)(getContext().getResources().getDisplayMetrics().widthPixels * 0.9);
        }
    }
}
