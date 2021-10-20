package com.epost.insu.dialog;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import androidx.annotation.NonNull;
import android.text.Spannable;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.accessibility.AccessibilityEvent;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.epost.insu.R;
import com.epost.insu.common.CommonFunction;

/**
 * @copyright : (주)프로니스정보기술 | http://www.phronis.co.kr
 *
 * @project   : 모바일슈랑스 구축
 * @pakage   : com.epost.insu.common
 * @fileName  : CustomDialog.java
 *
 * @Title     : 메시지 팝업 다이얼로그
 * @author    : 이수행
 * @created   : 2017-08-02
 * @version   : 1.0
 *
 * @note      : 싱글버튼, 투버튼 다이얼로그<br/>
 *               {@link BackMode} 속성 값 변경으로 Back키 제어가 가능하다.<br/>
 *               투버튼 사용시 좌측버튼 클릭시 다이얼로그 cancel()<br/>
 *
 *               <prev>
 *                   CustomDialog tmp_dlg = new CustomDialog(IUCOA0M00.this);
 *                   tmp_dlg.show();
 *                   tmp_dlg.CF_setTextContent("테스트 팝업");
 *                   tmp_dlg.CF_setBackKeyMode(CustomDialog.BackMode.NOTUSE);   // default CANCELED
 *                   //tmp_dlg.CF_setDoubleButtonText("취소","확인");
 *                   tmp_dlg.CF_setSingleButtonText("확인");
 *                   tmp_dlg.setCanceledOnTouchOutside(true);                   // default false
 *                   tmp_dlg.setOnDismissListener(new DialogInterface.OnDismissListener() {
 *                      @Override
 *                      public void onDismiss(DialogInterface dialog) {
 *                          if(((CustomDialog)dialog).CF_getCanceled()==false){
 *                              Toast.makeText(IUCOA0M00.this,"dismiss", Toast.LENGTH_SHORT);
 *                          }
 *                      }
 *                   });
 *               </prev>
 * ======================================================================
 * 수정 내역
 * NO       날짜          작업자       내용
 * 01       2017-08-02    이수행     : 최초 등록
 * 02       2017-08-08    이수행     : 타이틀/닫기 버튼 추가
 * 03       2017-11-14    이수행     : 타이틀/닫기 버튼 제거
 * 04       2020-02-17    노지민     : Triple버튼 Dialog 추가
 * =======================================================================
 */
public class CustomDialog extends Dialog{

    public enum BackMode{
        NOTUSE,
        CANCELED
    }

    private String strMessage = "";
    private Button btnLeft, btnRight, btnBottom;
    private TextView textContents;
    private View viewLineV;
    private boolean flagIsCanceled = false;
    private int flagBtnIndex;
    private BackMode backMode = BackMode.CANCELED;

    public CustomDialog(@NonNull Context context) {
        super(context);
    }

    public CustomDialog(@NonNull Context context, String p_message){
        super(context);
        strMessage = p_message;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        this.setInit();
        this.setUIControl();
    }


    @Override
    protected void onStop(){
        super.onStop();

        clearMemory();
    }


    @Override
    public void show() {



        super.show();

        // 웹접근성 && textContents
        if(CommonFunction.CF_checkAccessibilityTurnOn(getContext()) && textContents != null){
            textContents.announceForAccessibility("알림");
        }
    }


    @Override
    public void onBackPressed(){
        if(this.backMode == BackMode.CANCELED){
            super.onBackPressed();
        }
    }


    @Override
    public void cancel(){
        super.cancel();
        flagIsCanceled = true;
    }


    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);

        // -----------------------------------------------------------------------------------------
        //  접근성 On : 다이얼로그 알림 문자열 Auto 포커싱
        // -----------------------------------------------------------------------------------------
        /*
        if(CommonFunction.CF_checkAccessibilityTurnOn(getContext())) {
            if (hasFocus) {

                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        textContents.setFocusableInTouchMode(true);
                        textContents.requestFocus();
                        textContents.setFocusableInTouchMode(false);
                    }
                }, 500);
            }
        }
        */
    }


    @Override
    public boolean dispatchPopulateAccessibilityEvent(@NonNull AccessibilityEvent event) {
        // -----------------------------------------------------------------------------------------
        //  다이얼로그 TYPE_WINDOW_STATE_CHANGED Talkback 안내 막기
        // -----------------------------------------------------------------------------------------
        //return super.dispatchPopulateAccessibilityEvent(event);
        return true;
    }


    /**
     * 초기 세팅
     */
    private void setInit(){
        WindowManager.LayoutParams windowManagerLayoutParams = new WindowManager.LayoutParams();
        windowManagerLayoutParams.flags = WindowManager.LayoutParams.FLAG_DIM_BEHIND;
        windowManagerLayoutParams.dimAmount = 0.8f;
        getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        getWindow().setAttributes(windowManagerLayoutParams);
        setCanceledOnTouchOutside(false);

        setContentView(R.layout.dialog_custom);
    }


    /**
     * 가로 사이즈 재조정
     */
    private void resizeWidth(){

        LinearLayout tmp_rootView = findViewById(R.id.rootView);
        ViewGroup.LayoutParams tmp_lp = tmp_rootView.getLayoutParams();

        if(CommonFunction.CF_isTablet(getContext())){
            tmp_lp.width = (int)(getContext().getResources().getDisplayMetrics().widthPixels * 0.6);
        }
        else{
            tmp_lp.width = (int)(getContext().getResources().getDisplayMetrics().widthPixels * 0.8);
        }

        tmp_rootView.setLayoutParams(tmp_lp);
    }


    /**
     * UI 생성 및 세팅 함수
     */
    private void setUIControl(){

        resizeWidth();

        btnLeft         = findViewById(R.id.btnLeft);
        btnRight        = findViewById(R.id.btnRight);
        btnBottom       = findViewById(R.id.btnBottom);
        textContents    = findViewById(R.id.textContent);
        viewLineV       = findViewById(R.id.viewLine);
        flagBtnIndex    = 0;

        this.viewLineV.setVisibility(View.GONE);
        this.btnLeft.setVisibility(View.GONE);
        this.btnRight.setVisibility(View.GONE);
        this.btnBottom.setVisibility(View.GONE);

        CF_setTextContent(this.strMessage);
    }


    /**
     * 다이얼로그 cancel 정보 반환
     * @return if true, Dialog is canceled
     */
    public boolean CF_getCanceled() {
        return this.flagIsCanceled;
    }

    /**
     * 다이얼로그 버튼 인덱스 정보 반환
     * @return if true, Dialog is canceled
     */
    public int CF_getBtnIndex() {
        return this.flagBtnIndex;
    }

    /**
     * 다이얼로그 메시지 세팅 함수
     * @param p_content         String
     */
    public void CF_setTextContent(String p_content){
        textContents.setText(p_content);
    }


    /**
     * 다이얼로그 메시지 세팅 함수
     * @param p_spannable       Spannable
     */
    public void CF_setTextContent(Spannable p_spannable){
        textContents.setText(p_spannable);
    }

    /**
     * 원버튼 사용 함수<br/>
     * @param p_strButtonText   String
     */
    public void CF_setSingleButtonText(String p_strButtonText){
        this.btnRight.setVisibility(View.VISIBLE);

        this.btnRight.setText(p_strButtonText);
        //this.btnRight.setBackgroundResource(R.drawable.btn_red_selector);

        this.btnRight.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                CustomDialog.this.dismiss();
            }
        });
    }


    /**
     * 투버튼 사용 함수<br/>
     * @param p_strButton_left      String  좌특버튼 (cancel)
     * @param p_strButton_right     String  우측버튼 (dismiss)
     */
    public void CF_setDoubleButtonText(String p_strButton_left, String p_strButton_right) {
        this.viewLineV.setVisibility(View.VISIBLE);
        this.btnLeft.setVisibility(View.VISIBLE);
        this.btnRight.setVisibility(View.VISIBLE);

        // -- Left 버튼
        this.btnLeft.setText(p_strButton_left);
        //this.btnLeft.setBackgroundResource(R.drawable.btn_grey_light_selector);
        this.btnLeft.setOnClickListener(new View.OnClickListener(){
            public void onClick(View p_view) {
                CustomDialog.this.btnRight.setClickable(false);
                CustomDialog.this.cancel();
            }});

        // -- Right 버튼
        this.btnRight.setText(p_strButton_right);
       // this.btnRight.setBackgroundResource(R.drawable.btn_red_selector);
        this.btnRight.setOnClickListener(new View.OnClickListener(){
            public void onClick(View p_view) {
                CustomDialog.this.btnLeft.setClickable(false);
                CustomDialog.this.dismiss();
            }});
    }


    /**
     * 쓰리버튼 사용 함수<br/>
     * @param p_strButton_left      String      좌측버튼명
     * @param p_strButton_right     String      우측버튼명
     * @param p_strButton_bottom    String      하단버튼명 (cancel)
     */
    public void CF_setTripleButtonText(String p_strButton_left, String p_strButton_right, String p_strButton_bottom) {
        this.viewLineV.setVisibility(View.VISIBLE);
        this.btnLeft.setVisibility(View.VISIBLE);
        this.btnRight.setVisibility(View.VISIBLE);
        this.btnBottom.setVisibility(View.VISIBLE);

        // -- Left 버튼
        this.btnLeft.setText(p_strButton_left);
      //  this.btnLeft.setBackgroundResource(R.drawable.btn_red_selector);
        this.btnLeft.setTextColor(Color.parseColor("#FFFFFF"));
        this.btnLeft.setOnClickListener(new View.OnClickListener(){
            public void onClick(View p_view) {
                CustomDialog.this.btnRight.setClickable(false);
                CustomDialog.this.btnBottom.setClickable(false);
                flagBtnIndex = 1;
                CustomDialog.this.dismiss();
            }});

        // -- Right 버튼
        this.btnRight.setText(p_strButton_right);
      //  this.btnRight.setBackgroundResource(R.drawable.btn_red_selector);
        this.btnRight.setOnClickListener(new View.OnClickListener(){
            public void onClick(View p_view) {
                CustomDialog.this.btnLeft.setClickable(false);
                CustomDialog.this.btnBottom.setClickable(false);
                flagBtnIndex = 2;
                CustomDialog.this.dismiss();
            }});

        // -- Bottom 버튼
        this.btnBottom.setText(p_strButton_bottom);
      //  this.btnBottom.setBackgroundResource(R.drawable.btn_grey_light_selector);
        this.btnBottom.setOnClickListener(new View.OnClickListener(){
            public void onClick(View p_view) {
                CustomDialog.this.btnLeft.setClickable(false);
                CustomDialog.this.btnRight.setClickable(false);
                CustomDialog.this.cancel();
            }});
    }


    /**
     * 모든 View null 처리
     */
    private void clearMemory() {
        this.strMessage = null;
        this.btnLeft = null;
        this.btnRight = null;
        this.btnBottom = null;
        this.textContents = null;
    }


    /**
     * Back키 클릭 모드 값 세팅 함수
     * @param p_backMode    BackMode
     */
    public void CF_setBackKeyMode(BackMode p_backMode)
    {
        this.backMode = p_backMode;
    }
}
