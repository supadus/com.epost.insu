package com.epost.insu.control;

import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.content.Context;
import androidx.annotation.Nullable;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.view.animation.LinearInterpolator;
import android.widget.LinearLayout;

import com.epost.insu.R;
import com.epost.insu.common.CommonFunction;

import java.util.ArrayList;

/**
 * @copyright : (주)프로니스정보기술 | http://www.phronis.co.kr
 *
 * @project   : 모바일슈랑스 구축
 * @pakage   : com.epost.insu.control
 * @fileName  : CustomIndicator.java
 *
 * @Title     : ViewPager 지시자(indicator)
 * @author    : 이수행
 * @created   : 2017-07-24
 * @version   : 1.0
 *
 * @note      : <u>ViewPager 지시자(indicator)</u><br/>
 *              {@link #CF_drawDots(int)} 지시 점 그리기<br/>
 *              {@link #CF_setCurrentIndex(int)} 선택상태 index 세팅<br/>
 *              {@link #CF_getCurrentIndex()} 선택상태 index 반환<br/>
 * ======================================================================
 * 수정 내역
 * NO      날짜          작업자       내용
 * 01      2017-07-24    이수행       최초 등록
 * =======================================================================
 */
@SuppressWarnings("FieldCanBeLocal")
public class CustomIndicator extends LinearLayout{

    private final int defaultDotSize = 7;                       // default indicator 포인터 크기(pixel)
    private final long defaultAnimDuration = 200L;             // default Animation duration

    private ArrayList<View> arrDot;                                // 포인터 View 리스트

    private int dotSize;                                          // 포인터 크기(pixel)
    private int currentIndex;                                    // 현재 선택 index
    private long animDuration;                                   // Animatino duration

    /**
     * 생성자
     * @param context
     */
    public CustomIndicator(Context context) {
        super(context);

        setInit();
        setUIControl();
    }
    public CustomIndicator(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);

        setInit();
        setUIControl();
    }
    public CustomIndicator(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        setInit();
        setUIControl();
    }

    /**
     * 초기 세팅 함수
     */
    private void setInit(){

        currentIndex = 0;
        dotSize = defaultDotSize;
        animDuration = defaultAnimDuration;
        arrDot = new ArrayList<>();
    }

    /**
     * UI 생성 및 세팅 함수
     */
    private void setUIControl(){

        setPadding(CommonFunction.CF_convertDipToPixel(getContext(),10)
                , CommonFunction.CF_convertDipToPixel(getContext(),10)
                , CommonFunction.CF_convertDipToPixel(getContext(),10)
                , CommonFunction.CF_convertDipToPixel(getContext(),10));
    }

    /**
     * 포인터 그리기 함수
     * @param p_dotCount
     */
    public void CF_drawDots(int p_dotCount){
        LinearLayout.LayoutParams tmp_lp = new LinearLayout.LayoutParams(CommonFunction.CF_convertDipToPixel(getContext(),dotSize), CommonFunction.CF_convertDipToPixel(getContext(),dotSize));
        tmp_lp.gravity = Gravity.CENTER_VERTICAL;
        tmp_lp.setMargins(CommonFunction.CF_convertDipToPixel(getContext(),5),CommonFunction.CF_convertDipToPixel(getContext(),5),CommonFunction.CF_convertDipToPixel(getContext(),5),CommonFunction.CF_convertDipToPixel(getContext(),5));

        for(int i = 0 ; i < p_dotCount; i++){
            View tmp_view = new View(getContext());
            tmp_view.setLayoutParams(tmp_lp);

            this.addView(tmp_view);
            arrDot.add(tmp_view);

            if(i == currentIndex){
                playAnimOn(tmp_view,0L);
            }else {
                playAnimOff(tmp_view,0L);
            }
        }
    }

    /**
     * 선택 상태의 포인터 Animation
     * @param p_view
     * @param p_duration
     */
    private void playAnimOn(View p_view, long p_duration){

        p_view.setBackgroundResource(R.drawable.oval_red_t);

        PropertyValuesHolder tmp_scale_x = PropertyValuesHolder.ofFloat(SCALE_X, 1.4f);
        PropertyValuesHolder tmp_scale_y = PropertyValuesHolder.ofFloat(SCALE_Y, 1.4f);

        ObjectAnimator tmp_animScale = ObjectAnimator.ofPropertyValuesHolder(p_view, tmp_scale_x, tmp_scale_y);
        tmp_animScale.setDuration(defaultAnimDuration);
        tmp_animScale.setInterpolator(new LinearInterpolator());
        tmp_animScale.start();
    }

    /**
     * 선택해제 상태의 포인터 Animation
     * @param p_view
     * @param p_duration
     */
    private void playAnimOff(View p_view, long p_duration){

        p_view.setBackgroundResource(R.drawable.oval_grey);

        PropertyValuesHolder tmp_scale_x = PropertyValuesHolder.ofFloat(SCALE_X, 1.0f);
        PropertyValuesHolder tmp_scale_y = PropertyValuesHolder.ofFloat(SCALE_Y, 1.0f);

        ObjectAnimator tmp_animScale = ObjectAnimator.ofPropertyValuesHolder(p_view, tmp_scale_x, tmp_scale_y);
        tmp_animScale.setDuration(defaultAnimDuration);
        tmp_animScale.setInterpolator(new LinearInterpolator());
        tmp_animScale.start();
    }

    /**
     * 현재 선택된 index
     * @param p_index
     */
    public void CF_setCurrentIndex(int p_index){

        if(currentIndex != p_index){
            playAnimOff(arrDot.get(currentIndex),animDuration);
            playAnimOn(arrDot.get(p_index),animDuration);

            currentIndex = p_index;
        }
    }

    @SuppressWarnings("unused")
    public int CF_getCurrentIndex(){
        return currentIndex;
    }
}
