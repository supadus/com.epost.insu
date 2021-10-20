package com.epost.insu.control;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import androidx.annotation.Nullable;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.epost.insu.R;
import com.epost.insu.common.CommonFunction;
import com.epost.insu.common.DeprecatedFunc;

import java.util.ArrayList;

/**
 * 단계 지시자
 * @since     :
 * @version   : 1.1
 * @author    : LSH
 * <pre>
 *      총 단계를 보이고 현재 단계를 선택상태로 표기한다.
 * ======================================================================
 * 1.6.1    LSH_20171026    [PASS인증서 도입]
 * =======================================================================
 * copyright : (주)프로니스정보기술 | http://www.phronis.co.kr
 * </pre>
 */
@SuppressWarnings("FieldCanBeLocal")
public class StepIndicator extends FrameLayout {

    private final int defaultStep = 5;
    private final int defaultSelectedIndex = 0;
    private final int defaultLineSpaceWidth = CommonFunction.CF_convertDipToPixel(getContext(),10);
    private final int defaultLineColor = Color.TRANSPARENT;
    private final int defaultLineThick = CommonFunction.CF_convertDipToPixel(getContext(),1);
    private final int defaultDotSize = CommonFunction.CF_convertDipToPixel(getContext(), 29);
    private final int defaultSelectedColor = Color.YELLOW;
    private final int defaultNormalColor = Color.WHITE;
    private final int defaultNumberColor = Color.BLACK;
    private final int defaultNonSelectedNumberColor = Color.parseColor("#767676");

    private int stepCount;      // 단계 수
    private int selectedIndex;  // 선택된 단계 index
    private int lineThickness;    // 단계 이음라인 두께
    private int stepSize;       // 단계 view 가로 세로 크기
    private int numberColor;    // 선택상태의 단계 TEXT Color
    private int numberColorNonSelected; // 선택안한 단계 TEXT Color

    private int lineColor;
    private Drawable selectedDrawable, normalDrawable;

    private LinearLayout linDots;

    private ArrayList<View> arrDotView;


    /**
     * 생성자
     * @param context Context
     */
    public StepIndicator(Context context){
        super(context);
        setInit();
        setUIControl();
    }


    /**
     * 생성자
     * @param context   Context
     * @param attrs     AttributeSet
     */
    public StepIndicator(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);

        setInit();
        setAttribute(attrs);
        setUIControl();
    }


    /**
     * 초기 세팅 함수
     */
    private void setInit(){
        stepCount = defaultStep;
        selectedIndex = defaultSelectedIndex;
        lineColor = defaultLineColor;
        lineThickness = defaultLineThick;
        numberColor = defaultNumberColor;
        numberColorNonSelected = defaultNonSelectedNumberColor;
        arrDotView = new ArrayList<>();
    }


    /**
     * Attr 세팅 함수
     * @param p_attr
     */
    private void setAttribute(AttributeSet p_attr){
        TypedArray tmp_typedArray = this.getContext().obtainStyledAttributes(p_attr, R.styleable.StepIndicator);

        stepCount = tmp_typedArray.getInt(R.styleable.StepIndicator_stepCount, defaultStep);
        selectedIndex = tmp_typedArray.getInt(R.styleable.StepIndicator_selectedIndex, defaultSelectedIndex);
        lineThickness = tmp_typedArray.getDimensionPixelSize(R.styleable.StepIndicator_lineThick, defaultLineThick);
        numberColor = tmp_typedArray.getColor(R.styleable.StepIndicator_numberColor, defaultNumberColor);
        numberColorNonSelected = tmp_typedArray.getColor(R.styleable.StepIndicator_numberColorNonSelected, defaultNonSelectedNumberColor);
        lineColor = tmp_typedArray.getColor(R.styleable.StepIndicator_lineColor, Color.TRANSPARENT);
        stepSize = tmp_typedArray.getDimensionPixelSize(R.styleable.StepIndicator_stepSize, defaultDotSize);

        if(tmp_typedArray.hasValue(R.styleable.StepIndicator_bgSelected)) {
            selectedDrawable = tmp_typedArray.getDrawable(R.styleable.StepIndicator_bgSelected);
        }
        if(tmp_typedArray.hasValue(R.styleable.StepIndicator_bgNormal)) {
            normalDrawable = tmp_typedArray.getDrawable(R.styleable.StepIndicator_bgNormal);
        }


        tmp_typedArray.recycle();
    }

    /**
     * UI 생성 및 세팅 함수
     */
    private void setUIControl(){

        // 라인
        this.addView(getLineView());

        // indicator 추가
        linDots = new LinearLayout(getContext());
        this.addView(linDots);

        if(selectedDrawable != null && normalDrawable != null ){
            drawIndicator();
        }
    }

    public void CF_setStepCount(int p_stepCount){
        stepCount = p_stepCount;
    }

    public void CF_setStepSize(int p_stepSize){
        stepSize = p_stepSize;
    }

    /**
     * 숫자 color 값 세팅
     * @param p_color
     */
    public void CF_setNumberColor(int p_color){
        this.numberColor = p_color;
    }
    public void CF_setNumberColorNonSelected(int p_color){
        this.numberColorNonSelected = p_color;
    }
    /**
     * 라인 두께 세팅 함수
     * @param p_lineThick
     */
    public void CF_setLineThick(int p_lineThick){
        lineThickness = p_lineThick;
    }


    /**
     * 라인 색상 세팅 함수
     * @param p_color
     */
    public void CF_setLineColor(int p_color){
        this.lineColor = p_color;
    }

    /**
     * 현재 선택된 index 반환
     * @return
     */
    public int CF_getSelectedIndex(){
        return selectedIndex;
    }

    /**
     * Dot 배경 세팅 함수
     * @param p_selectedBg
     * @param p_normalBg
     */
    public void CF_setStepBackground(Drawable p_selectedBg, Drawable p_normalBg){
        this.selectedDrawable = p_selectedBg;
        this.normalDrawable = p_normalBg;
    }

    /**
     * 선택된 항목 인덱스 세팅 함수<br/>
     * 선택한 항목의 state를 선택 상태로 변경 한다.<br/>
     * @param p_index
     */
    public void CF_setSelectedIndex(int p_index){

        if(selectedIndex != p_index ){

            // 백그라운드 변경
            if(selectedDrawable != null && normalDrawable != null) {
                DeprecatedFunc.CF_setBackgroundDrawable(arrDotView.get(p_index), selectedDrawable);
                DeprecatedFunc.CF_setBackgroundDrawable(arrDotView.get(selectedIndex), normalDrawable);
            }
            else{
                arrDotView.get(p_index).setBackgroundColor(defaultSelectedColor);
                arrDotView.get(selectedIndex).setBackgroundColor(defaultNormalColor);
            }

            // TextView Text style 변경
            TextView tmp_textSelected = (TextView)arrDotView.get(p_index).findViewById(R.id.text);
            TextView tmp_textNoSelected = (TextView)arrDotView.get(selectedIndex).findViewById(R.id.text);

            tmp_textSelected.setTextColor(numberColor);
            tmp_textNoSelected.setTextColor(numberColorNonSelected);

            tmp_textSelected.setTypeface(tmp_textSelected.getTypeface(), Typeface.BOLD);
            tmp_textNoSelected.setTypeface(tmp_textNoSelected.getTypeface(), Typeface.NORMAL);

            selectedIndex = p_index;
        }

        setContentDescription(""+stepCount+"단계 중 "+(selectedIndex+1)+"단계");
    }


    /**
     * UI 생성 및 세팅 함수
     */
    private void drawIndicator(){

        arrDotView.clear();
        linDots.removeAllViews();

        for(int i = 0 ; i < stepCount; i++){

            View tmp_dotView = getDotView(i);

            linDots.addView(tmp_dotView);
            arrDotView.add(tmp_dotView);

            if(i < (stepCount-1)) {
                this.linDots.addView(getEmptyView());
            }
        }

        // 취약점) 정수 연산 전에 안전한 범위 내의 값인지 검사한다.
        if (selectedIndex < 0 || selectedIndex > 100 ) {
            throw new IllegalArgumentException("out of bound");
        }

        setContentDescription(""+stepCount+"단계 중 "+(selectedIndex+1)+"단계");
    }


    /**
     * dot View 생성하여 반환
     * @param p_index
     * @return
     */
    @SuppressLint("SetTextI18n")
    private View getDotView(int p_index){

        LinearLayout.LayoutParams tmp_lp = new LinearLayout.LayoutParams(stepSize, stepSize);
        tmp_lp.gravity = Gravity.CENTER_VERTICAL;
        //tmp_lp.weight = 1.0f;

        TextView tmp_textView = new TextView(getContext());
        tmp_textView.setId(R.id.text);
        tmp_textView.setLayoutParams(tmp_lp);
        tmp_textView.setGravity(Gravity.CENTER);
        if(p_index == selectedIndex){
            tmp_textView.setTextColor(numberColor);
        }else{
            tmp_textView.setTextColor(numberColorNonSelected);
        }
        tmp_textView.setText(""+(p_index+1));
        tmp_textView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 14);
        if(normalDrawable != null && selectedDrawable != null) {

            if(p_index == selectedIndex){
                DeprecatedFunc.CF_setBackgroundDrawable(tmp_textView, selectedDrawable);
                //tmp_textView.setBackgroundDrawable(selectedDrawable);
            }else {
                DeprecatedFunc.CF_setBackgroundDrawable(tmp_textView, normalDrawable);
                //tmp_textView.setBackgroundDrawable(normalDrawable);
            }
        }else {
            if(p_index == selectedIndex) {
                tmp_textView.setBackgroundColor(defaultSelectedColor);
            }else{
                tmp_textView.setBackgroundColor(defaultNormalColor);
            }
        }

        return tmp_textView;
    }

    /**
     * empty view 반환
     * @return
     */
    private View getEmptyView(){
        LinearLayout.LayoutParams tmp_lp = new LinearLayout.LayoutParams(defaultLineSpaceWidth, 1);
        //tmp_lp.weight = 1.0f;

        View tmp_view = new View(getContext());
        tmp_view.setLayoutParams(tmp_lp);
        tmp_view.setBackgroundColor(Color.TRANSPARENT);

        return tmp_view;
    }


    /**
     * 라인 View 반환
     * @return
     */
    private View getLineView(){

        FrameLayout.LayoutParams tmp_lp = new FrameLayout.LayoutParams(stepSize*stepCount+defaultLineSpaceWidth*(stepCount-1)-CommonFunction.CF_convertDipToPixel(getContext(),5), lineThickness);
        tmp_lp.gravity = Gravity.CENTER;

        View tmp_view = new View(getContext());
        tmp_view.setLayoutParams(tmp_lp);
        tmp_view.setBackgroundColor(lineColor);

        return tmp_view;
    }
}
