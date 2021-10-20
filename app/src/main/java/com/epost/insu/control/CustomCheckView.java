package com.epost.insu.control;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.content.Context;
import android.content.res.TypedArray;
import androidx.annotation.AttrRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import android.graphics.Color;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.epost.insu.R;
import com.epost.insu.common.CommonFunction;
import com.epost.insu.event.OnChangedCheckedStateEventListener;

/**
 * @copyright : (주)프로니스정보기술 | http://www.phronis.co.kr
 *
 * @project   : 모바일슈랑스 구축
 * @pakage    : com.epost.insu.control
 * @fileName  : CustomCheckView.java
 *
 * @Title     : 체크박스View
 * @author    : 이수행
 * @created   : 2017-08-08
 * @version   : 1.0
 *
 * @note      : 커스텀 CheckBox<br/>
 *              <prev>
 *                  CustomCheckView checkView = new CustomCheckView(Context);<br/>
 *                  checkView.CF_setFlagPlayAnim(boolean);      // Animation 사용유무<br/>
 *                  checkView.CF_setCheck(boolean);     // 체크 상태 변경<br/>
 *                  <br/>
 *                  {@link #CF_isChecked()}     // 현재 체크 상태 반환
 *              </prev>
 * ======================================================================
 * 수정 내역
 * NO      날짜          작업자       내용
 * 01      2017-08-08    이수행       최초 등록
 * =======================================================================
 */
@SuppressWarnings("FieldCanBeLocal")
public class CustomCheckView extends FrameLayout{

    private final long durationMain = 120;              // 메인 Animation duration
    private final long durationSub = 60;                // 서브 Animation duration
    private final float scaleSize = 1.2f;               // Animation Scale 사이즈


    private OnChangedCheckedStateEventListener listener;

    private View viewOff, viewOn;
    private ImageView imgCheck;

    private boolean flagIsCheck;                        // 선택상태 flag 값
    private boolean flagPlayAnim;                       // Animation Play flag 값

    private String descOnState = "";                    // check 상태의 contentsDescription
    private String descOffState = "";                   // uncheck 상태의 contentsDescription

    /**
     * 생성자
     * @param context
     */
    public CustomCheckView(@NonNull Context context) {
        super(context);

        setInit();
        setUIControl();
    }
    public CustomCheckView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);

        setInit();
        setAttribute(attrs);
        setUIControl();
    }
    public CustomCheckView(@NonNull Context context, @Nullable AttributeSet attrs, @AttrRes int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        setInit();
        setAttribute(attrs);
        setUIControl();
    }


    /**
     * 초기 세팅 함수
     */
    private void setInit(){
        flagIsCheck = false;
        flagPlayAnim = true;
        descOnState = getResources().getString(R.string.desc_uncheck);
        descOffState = getResources().getString(R.string.desc_check);
    }


    /**
     * Attr 세팅 함수
     * @param p_attr
     */
    private void setAttribute(AttributeSet p_attr){
        TypedArray tmp_typedArray = this.getContext().obtainStyledAttributes(p_attr, R.styleable.CustomCheckView);

        flagIsCheck = tmp_typedArray.getBoolean(R.styleable.CustomCheckView_flagChecked, false);
        flagPlayAnim = tmp_typedArray.getBoolean(R.styleable.CustomCheckView_flagUseAnim, true);

        tmp_typedArray.recycle();
    }

    /**
     * UI 생성 및 세팅 함수
     */
    private void setUIControl(){

        FrameLayout.LayoutParams tmp_lp = new FrameLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
        tmp_lp.setMargins(CommonFunction.CF_convertDipToPixel(getContext(),10),CommonFunction.CF_convertDipToPixel(getContext(),10),CommonFunction.CF_convertDipToPixel(getContext(),10),CommonFunction.CF_convertDipToPixel(getContext(),10));
        viewOff = new View(getContext());
        viewOff.setLayoutParams(tmp_lp);
        viewOff.setBackgroundColor(Color.TRANSPARENT);


        viewOff.setBackgroundResource(R.drawable.oval_check_none);

        viewOn = new View(getContext());
        viewOn.setLayoutParams(tmp_lp);
        viewOn.setBackgroundColor(Color.TRANSPARENT);
        viewOn.setBackgroundResource(R.drawable.oval_check);


        imgCheck = new ImageView(getContext());
        imgCheck.setLayoutParams(tmp_lp);
        imgCheck.setAdjustViewBounds(true);

        imgCheck.setScaleType(ImageView.ScaleType.FIT_CENTER);
        imgCheck.setImageResource(R.drawable.ic_check_g);
        imgCheck.setPadding(CommonFunction.CF_convertDipToPixel(getContext(), 4),CommonFunction.CF_convertDipToPixel(getContext(), 4),CommonFunction.CF_convertDipToPixel(getContext(), 4),CommonFunction.CF_convertDipToPixel(getContext(), 4));

        this.addView(viewOff);
        this.addView(viewOn);
        this.addView(imgCheck);


        if(flagIsCheck){
            playCheckAnim(0);
            setContentDescription(descOnState);
        }else{
            playUnCheckAnim(0);
            setContentDescription(descOffState);
        }

        this.setClickable(true);
        this.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {

                if(flagIsCheck){
                    setOff(flagPlayAnim,true,true);
                }else{
                    setOn(flagPlayAnim,true,true);
                }
            }
        });
    }

    /**
     * OFF 상태 세팅 함수
     * @param p_flagShowAnim
     */
    private void setOff(boolean p_flagShowAnim, boolean p_flagCallEvent, boolean p_flagAnnounceMent){
        flagIsCheck = false;

        if(flagPlayAnim || p_flagShowAnim){

            viewOn.clearAnimation();

            PropertyValuesHolder tmp_scale_x = PropertyValuesHolder.ofFloat(SCALE_X,scaleSize);
            PropertyValuesHolder tmp_scale_y = PropertyValuesHolder.ofFloat(SCALE_Y,scaleSize);

            ObjectAnimator tmp_animScale = ObjectAnimator.ofPropertyValuesHolder(viewOn, tmp_scale_x, tmp_scale_y);
            tmp_animScale.setDuration(durationSub);
            tmp_animScale.addListener(new Animator.AnimatorListener() {
                @Override
                public void onAnimationStart(Animator animation) {
                }

                @Override
                public void onAnimationEnd(Animator animation) {
                    playUnCheckAnim(durationMain);
                }

                @Override
                public void onAnimationCancel(Animator animation) {
                }

                @Override
                public void onAnimationRepeat(Animator animation) {
                }
            });
            tmp_animScale.start();
        }
        else{
            viewOn.setVisibility(View.INVISIBLE);
        }

        if(listener != null && p_flagCallEvent){
            listener.onCheck(false);
        }

        setContentDescription(descOffState);
        if(p_flagAnnounceMent) {
            announceForAccessibility(getResources().getString(R.string.announce_check_off));
        }
    }


    /**
     * On 상태 세팅 함수
     * @param p_flagShowAnim
     */
    private void setOn(boolean p_flagShowAnim,boolean p_flagCallEvent, boolean p_flagAnnounceMent){

        flagIsCheck = true;

        if(flagPlayAnim || p_flagShowAnim){
            viewOn.clearAnimation();
            playCheckAnim(durationMain);
        }
        else{
            viewOn.setVisibility(View.VISIBLE);
        }

        if(listener != null && p_flagCallEvent){
            listener.onCheck(true);
        }

        setContentDescription(descOnState);
        if(p_flagAnnounceMent) {
            announceForAccessibility(getResources().getString(R.string.announce_check_on));
        }
    }

    /**
     * uncheck 상태 animation play
     * @param p_duration
     */
    private void playUnCheckAnim(long p_duration){

        imgCheck.setImageResource(R.drawable.ic_check_g);
        PropertyValuesHolder tmp_scale_x = PropertyValuesHolder.ofFloat(SCALE_X,0.0f);
        PropertyValuesHolder tmp_scale_y = PropertyValuesHolder.ofFloat(SCALE_Y,0.0f);
        ObjectAnimator tmp_animScaleOrig = ObjectAnimator.ofPropertyValuesHolder(viewOn, tmp_scale_x, tmp_scale_y);
        tmp_animScaleOrig.setDuration(p_duration);
        tmp_animScaleOrig.start();
    }

    /**
     * check 상태 animation play
     * @param p_duration
     */
    private void playCheckAnim(long p_duration){

        imgCheck.setImageResource(R.drawable.ic_check);


        PropertyValuesHolder tmp_scale_x = PropertyValuesHolder.ofFloat(SCALE_X, 1.0f);
        PropertyValuesHolder tmp_scale_y = PropertyValuesHolder.ofFloat(SCALE_Y, 1.0f);

        ObjectAnimator tmp_animScale = ObjectAnimator.ofPropertyValuesHolder(viewOn, tmp_scale_x, tmp_scale_y);
        tmp_animScale.setDuration(p_duration);
        tmp_animScale.start();
    }


    /**
     * Off 상태 백그라운드 세팅
     * @param p_resId
     */
    public void CF_setBgOff(int p_resId){
        viewOff.setBackgroundResource(p_resId);
    }

    /**
     * On 상태 백그라운드 세팅
     * @param p_resId
     */
    public void CF_setBgOn(int p_resId){
        viewOn.setBackgroundResource(p_resId);
    }

    /**
     * 상태값 변경 시 Animation 사용 유무
     * @param p_flagPlayAnim
     */
    @SuppressWarnings("unused")
    public void CF_setFlagPlayAnim(boolean p_flagPlayAnim){
        flagPlayAnim = p_flagPlayAnim;
    }

    public void CF_setCheck(boolean p_flagCheck, boolean p_flagShowAnim){
        CF_setCheck(p_flagCheck,p_flagShowAnim,true);
    }

    /**
     * On/Off 상태에 따른 대체 텍스트 세팅 함수
     * @param p_onState
     * @param p_offState
     */
    public void CF_setContentsDesc(String p_onState, String p_offState){
        descOnState = p_onState;
        descOffState = p_offState;

        if(flagIsCheck){
            setContentDescription(descOnState);
        }else{
            setContentDescription(descOffState);
        }
    }

    /**
     * 체크 상태 세팅 함수
     * @param p_flagCheck
     * @param p_flagShowAnim 1회성 Animation Show
     */
    public void CF_setCheck(boolean p_flagCheck, boolean p_flagShowAnim, boolean p_flagCallEvent){

        if(p_flagCheck){
            setOn(p_flagShowAnim,p_flagCallEvent,false);
        }else{
            setOff(p_flagShowAnim,p_flagCallEvent,false);
        }
    }

    /**
     * 현재 체크 상태 반환 함수
     * @return if true, 현재 체크 상태
     */
    public boolean CF_isChecked(){
        return flagIsCheck;
    }

    /**
     * 이벤트 리스너 세팅 함수
     * @param p_listener
     */
    public void CE_setOnChangedCheckedStateEventListener(OnChangedCheckedStateEventListener p_listener){
        listener = p_listener;
    }
}
