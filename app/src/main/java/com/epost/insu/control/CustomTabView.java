package com.epost.insu.control;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Build;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;

import com.epost.insu.R;
import com.epost.insu.common.CommonFunction;
import com.epost.insu.event.OnSelectedChangeEventListener;


/**
 * @copyright : (주)프로니스정보기술 | http://www.phronis.co.kr
 *
 * @project   : 모바일슈랑스 구축
 * @pakage   : com.epost.insu.control
 * @fileName  : CustomTabView.java
 *
 * @Title     : 2개의 버튼으로 구성된 탭바
 * @author    : 이수행
 * @created   : 2017-08-28
 * @version   : 1.0
 *
 * @note      : 2개의 버튼으로 구성된 탭바<br/>
 *               attr은 지원하지 않는다.
 *               <prev>
 *                   CustomTabView tmp_tabView = new CustomTabView(Context);
 *                   tmp_tabView.{@link #CF_setTabText(String, String)};
 *                   tmp_tabView.{@link #CF_setTabTextSize(int)};
 *                   tmp_tabView.{@link #CF_setTabBackground(int, int)};
 *                   tmp_tabView.{@link #CE_setOnSelectedChangeEventListener(OnSelectedChangeEventListener)};
 *               </prev>
 * ======================================================================
 * 수정 내역
 * NO      날짜          작업자       내용
 * 01      2017-08-28    이수행       최초 등록
 * =======================================================================
 */
public class CustomTabView extends LinearLayout{


    private OnSelectedChangeEventListener listener;

    private Button btnLeft, btnRight;

    private int currentSelectedIndex;

    /**
     * 생성자
     * @param context
     */
    public CustomTabView(Context context) {
        super(context);

        setInit();
        setUIControl();
    }
    public CustomTabView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);

        setInit();
        setUIControl();
    }
    public CustomTabView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        setInit();
        setUIControl();
    }


    /**
     * 초기 세팅 함수
     */
    private void setInit(){
        this.setOrientation(LinearLayout.HORIZONTAL);
        this.setBackgroundResource(R.drawable.tab_nav_backround);

        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT,LayoutParams.WRAP_CONTENT);


        this.setLayoutParams(layoutParams);


        this.setPadding(0,0,0,0);
        currentSelectedIndex = 0;
    }


    /**
     * UI 생성 및 세팅 함수
     */
    @SuppressLint("NewApi")
    private void setUIControl(){
        LinearLayout.LayoutParams tmp_lp = new LinearLayout.LayoutParams( LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
       // tmp_lp.weight = 1.0f;
        btnLeft = new Button(getContext(),null,R.attr.borderlessButtonStyle);
        btnLeft.setLayoutParams(tmp_lp);
        btnLeft.setPadding(0, CommonFunction.CF_convertDipToPixel(getContext(),10),0, CommonFunction.CF_convertDipToPixel(getContext(),10));
        btnLeft.setTextColor(ContextCompat.getColorStateList(getContext(), R.color.text_color_btn_tab));
        btnLeft.setTextSize(TypedValue.COMPLEX_UNIT_PX,getResources().getDimensionPixelSize(R.dimen.button_text_size));
        btnLeft.setSelected(true);

        btnLeft.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                CF_setSelectState(0);
            }
        });



        btnRight = new Button(getContext(),null,R.attr.borderlessButtonStyle);
        btnRight.setLayoutParams(tmp_lp);
        btnRight.setPadding(0, CommonFunction.CF_convertDipToPixel(getContext(),10), 0, CommonFunction.CF_convertDipToPixel(getContext(),10));
        btnRight.setTextColor(ContextCompat.getColorStateList(getContext(), R.color.text_color_btn_tab));
        btnRight.setTextSize(TypedValue.COMPLEX_UNIT_PX,getResources().getDimensionPixelSize(R.dimen.button_text_size));
        btnRight.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                CF_setSelectState(1);
            }
        });

        int sdk = android.os.Build.VERSION.SDK_INT;
        if(sdk > Build.VERSION_CODES.LOLLIPOP) {
            btnLeft.setStateListAnimator(null);
            btnRight.setStateListAnimator(null);
        }

        this.addView(btnLeft);
        this.addView(btnRight);
    }

    /**
     * Tab 텍스트 세팅 함수
     * @param p_strLeft
     * @param p_strRight
     */
    public void CF_setTabText(String p_strLeft, String p_strRight){
        btnLeft.setText(p_strLeft);
        btnRight.setText(p_strRight);


        if(currentSelectedIndex == 0){
            this.setContentDescription(p_strLeft+"/"+p_strRight+" 중 "+p_strLeft+" 선택됨");
        }
        else if(currentSelectedIndex ==1){
            this.setContentDescription(p_strLeft+"/"+p_strRight+" 중 "+p_strRight+" 선택됨");
        }

    }

    /**
     * Tab 텍스트 크기 세팅 함수
     * @param p_pxSize
     */
    public void CF_setTabTextSize(int p_pxSize){
        btnLeft.setTextSize(TypedValue.COMPLEX_UNIT_PX, p_pxSize);
        btnRight.setTextSize(TypedValue.COMPLEX_UNIT_PX, p_pxSize);
    }

    /**
     * Tab 백그라운드 세팅 함수
     * @param p_resLeft
     * @param p_resRight
     */
    public void CF_setTabBackground(int p_resLeft, int p_resRight){
        btnLeft.setBackgroundResource(p_resLeft);
        btnRight.setBackgroundResource(p_resRight);
    }

    /**
     * 선택 상태 세팅 함수<br/>
     * @param p_index if 0 Left, 1 Right
     */
    public void CF_setSelectState(int p_index){
        currentSelectedIndex = p_index;
        if(p_index == 0){
            btnLeft.setSelected(true);
            btnRight.setSelected(false);
            btnRight.setContentDescription(btnRight.getText().toString());
            if(CommonFunction.CF_checkAccessibilityTurnOn(getContext())) {
                btnLeft.announceForAccessibility(btnLeft.getText().toString()+" 선택");
            }
            this.setContentDescription(btnLeft.getText().toString()+"/"+btnRight.getText().toString()+" 중 "+btnLeft.getText().toString()+" 선택됨");
        }
        else if(p_index == 1){

            btnLeft.setSelected(false);
            btnRight.setSelected(true);
            btnLeft.setContentDescription(btnLeft.getText().toString());
            if(CommonFunction.CF_checkAccessibilityTurnOn(getContext())) {
                btnRight.announceForAccessibility(btnRight.getText().toString()+" 선택");
            }
            this.setContentDescription(btnLeft.getText().toString()+"/"+btnRight.getText().toString()+" 중 "+btnRight.getText().toString()+" 선택됨");

        }

        if(listener != null){
            listener.onSelected(currentSelectedIndex);
        }
    }

    /**
     * 현재 선택된 view index 값 반환 함수
     * @return
     */
    public int CF_getCurrentIndex(){
        return currentSelectedIndex;
    }

    /**
     * 선택 상태 변경 이벤트 리스너 세팅 함수
     * @param p_listener
     */
    public void CE_setOnSelectedChangeEventListener(OnSelectedChangeEventListener p_listener){
        listener = p_listener;
    }
}
