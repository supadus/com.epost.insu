package com.epost.insu.control;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.LinearInterpolator;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.epost.insu.R;
import com.epost.insu.common.CommonFunction;
import com.epost.insu.event.OnChangedCheckedStateEventListener;

/**
 * 아이폰 스타일의 On / Off 버튼
 * ####
 *  레이아웃에 추가 시 사이즈는 width, height 모두 WRAP_CONTENT 로 배치한다.
 *  컨트롤의 width, height는 아래 샘플 참고한다.
 * ###
 *
 * <prev>
 *     CustomOnOff tmp_onOff = new CustomOnOff(getContext());
 *     tmp_onOff.CF_setSize(100,30);
 *     //tmp_onOff.CF_setOnOff(true);
 *     tmp_onOff.CF_setOnOffNoAnim(true);
 *     tmp_onOff.CF_setText("On","Off");
 *     tmp_onOff.CF_setTextSize(14,14);
 *     tmp_onOff.CF_setTextColor(Color.rgb(33,33,33),Color.rgb(33,33,33));
 * </prev>
 *
 * <prev>
 *     xml
 *     <com.example.administrator.myapplication.control.CustomOnOff
 *           android:id="@+id/custom_onoff_2"
 *           android:layout_width="wrap_content"
 *           android:layout_height="wrap_content"
 *           android:layout_marginTop="20dp"
 *           app:onText="켜기"
 *           app:offText="끄기"
 *           app:width="72dp"
 *           app:height="30dp"
 *           app:flagSelect="true"
 *           app:animDuration="120"
 *           app:onTextSize = "14"
 *           app:offTextSize = "14"
 *           app:onTextColor = "#FFFFFF"
 *           app:offTextColor="#F12121"
 *       ></com.example.administrator.myapplication.control.CustomOnOff>
 *
 * </prev>
 * Created by 이수행 on 2017-06-16.
 */

public class OnOffControl extends FrameLayout {

    private final int mDefaultWidth = CommonFunction.CF_convertDipToPixel(getContext(),68);    // default width
    private final int mDefaultHeight = CommonFunction.CF_convertDipToPixel(getContext(),30);   // default height : 변경시 drawable 배경의 radius 값 변경 필요
    private final int mDefaultTextSize = 14;                                                   // default Text Size
    private final String mDefaultTextOn = getResources().getString(R.string.label_on);         // default On Text
    private final String mDefaultTextOff = getResources().getString(R.string.label_off);      // default Off Text
    private final int mDefaultTextColor = Color.parseColor("#333333");                                        // default Text Color
    private final int mDefaultAnimDuration = 120;
    private final int mDefaultSwitchViewMargin = CommonFunction.CF_convertDipToPixel(getContext(), 5);

    private OnChangedCheckedStateEventListener listener;

    private View mViewAnim;         // Off 변경 시 Animation View
    private View mViewSwitch;       // Switch View
    private TextView textViewOn;    // On TextView
    private TextView textViewOff;   // Off TextView

    private boolean mFlagIsOn=false;    // 현재 상태값 true : On, flase : Off

    private int mWidth = mDefaultWidth;                   // control width
    private int mHeight = mDefaultHeight;                 // control height
    private int mSwitchViewMargin = mDefaultSwitchViewMargin; // switch view 마진
    private int mOnTextSize = mDefaultTextSize;          // on 상태 Text Size int 값 :  textSize 세팅은 TYPEDVALUE.COMPLEX_UNIT_DIP, mOnTextSize 로 세팅
    private int mOffTextSize = mDefaultTextSize;         // off 상태 Text Size int 값
    private String mOnText = mDefaultTextOn;              // on 상태 Text
    private String mOffText = mDefaultTextOff;            // off 상태 Text
    private int mOnTextColor = mDefaultTextColor;       // on 상태 Text Color
    private int mOffTextColor = mDefaultTextColor;      // off 상태 Text Color
    private int mDuration = mDefaultAnimDuration;       // animation 시간

    /**
     * 생성자
     * @param context
     */
    public OnOffControl(@NonNull Context context){
        super(context);

        setInit();
        setUIControl();
    }

    /**
     * 생성자
     * @param context
     * @param attrs
     */
    public OnOffControl(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);

        setInit();
        setAttribute(attrs);
        setUIControl();
    }

    /**
     * 초기 세팅 함수
     */
    private void setInit(){
        this.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {

                LinearInterpolator tmp_linInterpolatore = new LinearInterpolator();

                mFlagIsOn = !mFlagIsOn;
                if(mFlagIsOn) {
                    setOn(mDuration);
                }
                else{
                    setOff(mDuration);
                }
            }
        });
    }

    /**
     * 속성값 세팅함수
     * @param p_attr
     */
    private void setAttribute(AttributeSet p_attr){
        TypedArray tmp_typedArray = this.getContext().obtainStyledAttributes(p_attr, R.styleable.OnOffControl);

        // control width height
        mWidth = tmp_typedArray.getDimensionPixelSize(R.styleable.OnOffControl_width, mDefaultWidth);
        mHeight = tmp_typedArray.getDimensionPixelSize(R.styleable.OnOffControl_height, mDefaultHeight);

        // on off Text 관련
        mOnText = tmp_typedArray.getString(R.styleable.OnOffControl_onText);
        mOffText = tmp_typedArray.getString(R.styleable.OnOffControl_offText);
        mOnTextSize = tmp_typedArray.getInt(R.styleable.OnOffControl_onTextSize, mDefaultTextSize);
        mOffTextSize = tmp_typedArray.getInt(R.styleable.OnOffControl_offTextSize, mDefaultTextSize);
        mOnTextColor = tmp_typedArray.getColor(R.styleable.OnOffControl_onTextColor, mDefaultTextColor);
        mOffTextColor = tmp_typedArray.getColor(R.styleable.OnOffControl_offTextColor, mDefaultTextColor);

        // Animation duration
        mDuration = tmp_typedArray.getInt(R.styleable.OnOffControl_animDuration,mDefaultAnimDuration);

        // On Off 상태 값
        mFlagIsOn = tmp_typedArray.getBoolean(R.styleable.OnOffControl_flagOn, false);


        tmp_typedArray.recycle();
    }



    /**
     * UI 생성 및 세팅 함수
     */
    private void setUIControl(){
        this.setBackgroundResource(R.drawable.bg_custom_off_parent);

        View mViewBg = new View(getContext());
        mViewBg.setLayoutParams(new FrameLayout.LayoutParams(mWidth, mHeight));
        mViewBg.setBackgroundResource(R.drawable.bg_custom_on_parent);

        this.mViewAnim = new View(getContext());
        this.mViewAnim.setLayoutParams(new FrameLayout.LayoutParams(mWidth,mHeight));
        this.mViewAnim.setBackgroundResource(R.drawable.bg_custom_off);

        FrameLayout.LayoutParams tmp_lpSwitchView = new FrameLayout.LayoutParams(mHeight-mSwitchViewMargin,mHeight-mSwitchViewMargin);
        tmp_lpSwitchView.gravity = Gravity.CENTER_VERTICAL;

        this.mViewSwitch = new View(getContext());
        this.mViewSwitch.setBackgroundResource(R.drawable.circle_white);
        this.mViewSwitch.setLayoutParams(tmp_lpSwitchView);

        FrameLayout.LayoutParams tmp_lpTextLayout = new FrameLayout.LayoutParams(mWidth,mHeight);
        tmp_lpTextLayout.gravity = Gravity.CENTER;

        LinearLayout tmp_linText = new LinearLayout(getContext());
        tmp_linText.setLayoutParams(tmp_lpTextLayout);
        tmp_linText.setWeightSum(1.0f);
        tmp_linText.setOrientation(LinearLayout.HORIZONTAL);

        LinearLayout.LayoutParams tmp_lpText = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT);
        tmp_lpText.weight = 0.5f;
        tmp_lpText.gravity = Gravity.CENTER_VERTICAL;

        this.textViewOn = new TextView(getContext());
        this.textViewOn.setLayoutParams(tmp_lpText);
        this.textViewOn.setTextSize(TypedValue.COMPLEX_UNIT_DIP,mOnTextSize);
        this.textViewOn.setGravity(Gravity.CENTER);
        this.textViewOn.setText(mOnText);
        this.textViewOn.setTextColor(mOnTextColor);

        this.textViewOff = new TextView(getContext());
        this.textViewOff.setLayoutParams(tmp_lpText);
        this.textViewOff.setTextSize(TypedValue.COMPLEX_UNIT_DIP,mOffTextSize);
        this.textViewOff.setTextColor(Color.BLACK);
        this.textViewOff.setGravity(Gravity.CENTER);
        this.textViewOff.setText(mOffText);
        this.textViewOff.setTextColor(mOffTextColor);

        tmp_linText.addView(this.textViewOn);
        tmp_linText.addView(this.textViewOff);

        this.addView(mViewBg);
        this.addView(mViewAnim);
        this.addView(tmp_linText);
        this.addView(mViewSwitch);

        CF_setOnOffNoAnim(mFlagIsOn);
    }



    /**
     * ON 상태 세팅
     * UI, On/Off 상태 값 변경
     * @param p_duration Animation 지속 시간
     */
    private void setOn(long p_duration){

        mFlagIsOn = true;
        LinearInterpolator tmp_linInterpolatore = new LinearInterpolator();

        // animation clear
        mViewAnim.clearAnimation();
        mViewSwitch.clearAnimation();
        textViewOn.clearAnimation();
        textViewOff.clearAnimation();

        mViewAnim.animate().scaleX(0.0f).scaleY(0.0f).setDuration(p_duration/2).setInterpolator(tmp_linInterpolatore);
        mViewSwitch.animate().setDuration(p_duration).setInterpolator(tmp_linInterpolatore).translationX(mWidth - mHeight);
        textViewOn.animate().setDuration(p_duration).setInterpolator(tmp_linInterpolatore).translationX(0).alpha(1.0f);
        textViewOff.animate().setDuration(p_duration).setInterpolator(tmp_linInterpolatore).translationX(-textViewOff.getWidth()).alpha(0.0f);

        this.setContentDescription(getResources().getString(R.string.desc_push_off));

        if(CommonFunction.CF_checkAccessibilityTurnOn(getContext())) {
            this.announceForAccessibility(getResources().getString(R.string.announce_push_on));
        }

        if(listener != null){
            listener.onCheck(true);
        }


    }

    /**
     * Off 세팅
     */
    /**
     * OFF 상태 세팅
     * UI, On/Off 상태 값 변경
     * @param p_duration Animation 지속 시간
     */
    private void setOff(long p_duration){

        mFlagIsOn = false;
        LinearInterpolator tmp_linInterpolatore = new LinearInterpolator();

        // animation clear
        mViewAnim.clearAnimation();
        mViewSwitch.clearAnimation();
        textViewOn.clearAnimation();
        textViewOff.clearAnimation();

        mViewAnim.animate().setDuration(p_duration).setStartDelay(p_duration/3).scaleX(1.0f).scaleY(1.0f).setInterpolator(tmp_linInterpolatore);
        mViewSwitch.animate().setDuration(p_duration).setInterpolator(tmp_linInterpolatore).translationX(0+mSwitchViewMargin);
        textViewOn.animate().setDuration(p_duration).setInterpolator(tmp_linInterpolatore).translationX(textViewOn.getWidth()).alpha(0.0f);
        textViewOff.animate().setDuration(p_duration).setInterpolator(tmp_linInterpolatore).translationX(0).alpha(1.0f);

        this.setContentDescription(getResources().getString(R.string.desc_push_on));
        if(CommonFunction.CF_checkAccessibilityTurnOn(getContext())) {
            this.announceForAccessibility(getResources().getString(R.string.announce_push_off));
        }

        if(listener != null){
            listener.onCheck(false);
        }


    }

    /**
     * On/Off 상태 세팅 함수(No Animation)
     * 애니메이션 동작 {@link #CF_setOnOff(boolean)}}
     * @param p_flagSetOn true=on, false=off
     */
    public void CF_setOnOffNoAnim(boolean p_flagSetOn){
        if(p_flagSetOn){
            setOn(0l);
        }else{
            setOff(0l);
        }
    }

    /**
     * On/Off 상태 세팅 함수로 애니메이션 동작
     * @see #CF_setOnOffNoAnim(boolean)
     * @param p_flagSetOn true=on, false=off
     */
    public void CF_setOnOff(boolean p_flagSetOn){
        if(p_flagSetOn){
            setOn(mDuration);
        }else{
            setOff(mDuration);
        }
    }

    /**
     * On / Off Text 세팅
     * @param p_textOn  On 텍스트
     * @param p_textOff Off 텍스트
     */
    public void CF_setText(String p_textOn, String p_textOff){
        textViewOn.setText(p_textOn);
        textViewOff.setText(p_textOff);
    }

    /**
     * On / Off Text Color 세팅
     * @param p_textOnColor On 텍스트 색상
     * @param p_textOffColor Off 텍스트 색상
     */
    public void CF_setTextColor(int p_textOnColor, int p_textOffColor){
        textViewOn.setTextColor(p_textOnColor);
        textViewOff.setTextColor(p_textOffColor);
    }

    /**
     * On / Off Text Size 세팅
     * @param p_unit    unit
     * @param p_sizeOn  on size
     * @param p_sizeOff off size
     */
    public void CF_setTextSize(int p_unit, int p_sizeOn, int p_sizeOff){
        textViewOn.setTextSize(p_unit, p_sizeOn);
        textViewOff.setTextSize(p_unit, p_sizeOff);
    }

    /**
     * On / Off Text Size 세팅
     * @param p_unit unit
     * @param p_size text Size
     */
    public void CF_setTextSize(int p_unit, int p_size){
        textViewOn.setTextSize(p_unit,p_size);
        textViewOff.setTextSize(p_unit,p_size);
    }

    /**
     * Switch View 마진 값 세팅
     * @param p_marginPx
     */
    public void CF_setSwitchViewMargin(int p_marginPx){
        mSwitchViewMargin = p_marginPx;
    }

    /**
     * 애니메이션 지속 시간 세팅
     * @param p_duration 애니메이션 지속 시간
     */
    public void CF_setAnimDuration(int p_duration){
        mDuration = p_duration;
    }

    /**
     * UI 사이즈 세팅
     * @param p_width 컨트롤 Width Dimention Pixel 값
     * @param p_height 컨트롤 Height Dimention Pixel 값
     */
    public void CF_setSize(int p_width, int p_height){
        mWidth = p_width;
        mHeight = p_height;

        this.removeAllViews();
        this.setUIControl();
    }

    /**
     * 현재 On / Off 상태값 반환 함수
     * @return
     */
    public boolean CF_getFlagIsOn(){
        return mFlagIsOn;
    }

    /**
     * 체크 상태 변경 이벤트 리스너 세팅 함수
     * @param p_listener
     */
    public void CE_setONChangedCheckedStateEventListener(OnChangedCheckedStateEventListener p_listener){
        listener = p_listener;
    }
}
