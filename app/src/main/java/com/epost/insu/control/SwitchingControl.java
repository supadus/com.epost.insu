package com.epost.insu.control;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import androidx.annotation.Nullable;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;

import com.epost.insu.R;
import com.epost.insu.common.CommonFunction;
import com.epost.insu.common.DeprecatedFunc;
import com.epost.insu.event.OnChangedCheckedStateEventListener;

/**
 * @copyright : (주)프로니스정보기술 | http://www.phronis.co.kr
 *
 * @project   : 모바일슈랑스 구축
 * @pakage   : com.epost.insu.control
 * @fileName  : SwitchingControl.java
 *
 * @Title     : SwitchingControl
 * @author    : 이수행
 * @created   : 2017-11-10
 * @version   : 1.0
 *
 * @note      : <u> 2가지 항목 중 단일 선택 컨트롤</u> </u><br/>
 * ======================================================================
 * 수정 내역
 * NO       날짜          작업자       내용
 * 01       2017-11-10    이수행     : 최초 등록
 * 02       2019-12-10    노지민     : 앱접근성 - 버튼 description 설정
 * =======================================================================
 */
public class SwitchingControl extends LinearLayout{

    private OnChangedCheckedStateEventListener listener;

    private Button btnLeft, btnRight;

    private int checkedState;

    /**
     * 생성자
     * @param context
     */
    public SwitchingControl(Context context){
        super(context);

        setInit();
        setUIControl();
    }

    public SwitchingControl(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);

        setInit();
        setUIControl();
    }

    /**
     * 초기 세팅 함수
     */
    private void setInit(){
        checkedState = -1;
    }

    /**
     * UI 생성 및 세팅 함수
     */
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void setUIControl(){

        setOrientation(LinearLayout.HORIZONTAL);
        LinearLayout.LayoutParams tmp_lpBtn = new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.MATCH_PARENT);
        tmp_lpBtn.setMargins(CommonFunction.CF_convertDipToPixel(getContext(), 5),
                0,
                CommonFunction.CF_convertDipToPixel(getContext(), 5),
                0);

        btnLeft = new Button(getContext());
        btnLeft.setText(getResources().getString(R.string.btn_yes));
        btnLeft.setLayoutParams(tmp_lpBtn);
        btnLeft.setMinWidth(0);
        btnLeft.setMinimumWidth(0);
        btnLeft.setMinHeight(0);
        btnLeft.setMinimumHeight(0);

        btnLeft.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimensionPixelSize(R.dimen.button_text_small_size));
        btnLeft.setTextColor(DeprecatedFunc.CF_getColorStateList(getContext(),R.color.text_color_btn_red,null));
        btnLeft.setBackgroundResource(R.drawable.btn_r_selector);
        btnLeft.setPadding(CommonFunction.CF_convertDipToPixel(getContext(),20),
                CommonFunction.CF_convertDipToPixel(getContext(),12),
                CommonFunction.CF_convertDipToPixel(getContext(),20),
                CommonFunction.CF_convertDipToPixel(getContext(),12));
        btnLeft.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {

                if(checkedState != 1) {
                    checkedState = 1;
                    btnLeft.setSelected(true);
                    btnRight.setSelected(false);

                    if (listener != null) {
                        listener.onCheck(true);
                    }

                    // -----------------------------------------------------------------------------
                    //  접근성 announce 출력
                    // -----------------------------------------------------------------------------
                    if(CommonFunction.CF_checkAccessibilityTurnOn(getContext())){
                        btnLeft.announceForAccessibility(btnLeft.getText()+" 선택");
                    }
                }
            }
        });

        btnRight = new Button(getContext());
        btnRight.setText(getResources().getString(R.string.btn_no));
        btnRight.setLayoutParams(tmp_lpBtn);

        btnRight.setMinWidth(0);
        btnRight.setMinimumWidth(0);
        btnRight.setMinHeight(0);
        btnRight.setMinimumHeight(0);
        btnRight.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimensionPixelSize(R.dimen.button_text_small_size));
        btnRight.setTextColor(DeprecatedFunc.CF_getColorStateList(getContext(), R.color.text_color_btn_red, null));
        btnRight.setBackgroundResource(R.drawable.btn_r_selector);
        btnRight.setPadding(CommonFunction.CF_convertDipToPixel(getContext(),20),
                CommonFunction.CF_convertDipToPixel(getContext(),12),
                CommonFunction.CF_convertDipToPixel(getContext(),20),
                CommonFunction.CF_convertDipToPixel(getContext(),12));
        btnRight.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if(checkedState != 0) {
                    checkedState = 0;
                    btnLeft.setSelected(false);
                    btnRight.setSelected(true);

                    if (listener != null) {
                        listener.onCheck(false);
                    }

                    // -----------------------------------------------------------------------------
                    //  접근성 announce 출력
                    // -----------------------------------------------------------------------------
                    if(CommonFunction.CF_checkAccessibilityTurnOn(getContext())){
                        btnRight.announceForAccessibility(btnRight.getText()+" 선택");
                    }
                }
            }
        });

        int sdk = android.os.Build.VERSION.SDK_INT;
        if(sdk > Build.VERSION_CODES.LOLLIPOP) {
            btnLeft.setStateListAnimator(null);
            btnRight.setStateListAnimator(null);
        }

        addView(btnLeft);
        addView(btnRight);
    }

    /**
     * 체크 상태 변경 이벤트 리스너 세팅 함수
     * @param p_listener
     */
    public void CE_setOnChangedCheckedStateEventListener(OnChangedCheckedStateEventListener p_listener){
        listener = p_listener;
    }

    /**
     * check 상태 세팅 함수<br/>
     * @param p_checkState if 0보다 작은 경우 체크 없음, if 0 아니오 Select, if 0보다 큰 경우 예 선택
     */
    public void CF_setCheck(int p_checkState){

        if(p_checkState <0){
            checkedState = -1;
            btnLeft.setSelected(false);
            btnRight.setSelected(false);
        }
        else if(p_checkState == 0){
            checkedState = 0;
            btnLeft.setSelected(false);
            btnRight.setSelected(true);
        }
        else if(p_checkState > 0){
            checkedState = 1;
            btnLeft.setSelected(true);
            btnRight.setSelected(false);
        }
    }

    /**
     * 버튼 Text 세팅 함수
     * @param p_left
     * @param p_rigth
     */
    public void CF_setText(String p_left , String p_rigth){
        btnLeft.setText(p_left);
        btnRight.setText(p_rigth);
    }

    /**
     * weight를 이용하여 두 개의 버튼의 가로 길이를 동일하게 설정
     */
    public void CF_setUniformWidth(){

        LinearLayout.LayoutParams tmp_lp = (LinearLayout.LayoutParams) btnLeft.getLayoutParams();
        tmp_lp.weight = 1.0f;

        btnLeft.setLayoutParams(tmp_lp);
        btnRight.setLayoutParams(tmp_lp);
    }

    /**
     * 체크상태 반환 함수
     * @return
     */
    public int CF_getCheckState(){
        return checkedState;
    }

    /**
     * 체크상태 반환 함수
     * @return
     */
    public Button CF_getBtnLeft(){
        return btnLeft;
    }

    /**
     * 체크상태 반환 함수
     * @return
     */
    public Button CF_getBtnRight(){
        return btnRight;
    }
}
