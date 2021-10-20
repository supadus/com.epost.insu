package com.epost.insu.control;

import android.content.Context;
import android.content.res.TypedArray;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatImageView;
import android.util.AttributeSet;

import com.epost.insu.R;


/**
 * @copyright : (주)프로니스정보기술 | http://www.phronis.co.kr
 *
 * @project   : 모바일슈랑스 구축
 * @pakage   : com.epost.insu.control
 * @fileName  : CustomRatioImageView.java
 *
 * @Title     : 가로세로 비율 조정이 가능한 ImageView
 * @author    : 이수행
 * @created   : 2017-11-21
 * @version   : 1.0
 *
 * @note      : <u>가로세로 비율 조정이 가능한 ImageView</u><br/>
 * ======================================================================
 * 수정 내역
 * NO      날짜          작업자       내용
 * 01      2017-11-21    이수행       최초 등록
 * =======================================================================
 */
public class CustomRatioImageView extends AppCompatImageView {

    private int ratioWidth;
    private int ratioHeight;
    private boolean flagStandWidth=true;

    /**
     * 생성자
     * @param context
     * @param p_ratioWidth
     * @param p_ratioHeight
     */
    public CustomRatioImageView(Context context, int p_ratioWidth, int p_ratioHeight) {
        super(context);

        this.ratioWidth = p_ratioWidth;
        this.ratioHeight = p_ratioHeight;

    }
    public CustomRatioImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.setInit(context, attrs);
    }
    public CustomRatioImageView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        this.setInit(context, attrs);
    }


    /**
     * 초기 세팅 함수
     *
     * @param context
     * @param attrs
     */
    private void setInit(Context context, AttributeSet attrs) {
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.RatioView);

        this.ratioWidth = a.getInt(R.styleable.RatioView_ratioWidth, 4);
        this.ratioHeight = a.getInt(R.styleable.RatioView_ratioHeight, 3);
        this.flagStandWidth = a.getBoolean(R.styleable.RatioView_flagStandWidth, true);

        a.recycle();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {

        if(flagStandWidth) {
            int originalWidth = MeasureSpec.getSize(widthMeasureSpec);
            int calculatedHeight = originalWidth * this.ratioHeight / this.ratioWidth;

            int finalWidth, finalHeight;

            finalWidth = originalWidth;
            finalHeight = calculatedHeight;

            super.onMeasure(
                    MeasureSpec.makeMeasureSpec(finalWidth, MeasureSpec.EXACTLY),
                    MeasureSpec.makeMeasureSpec(finalHeight, MeasureSpec.EXACTLY));
        }
        else{
            int originalHeight = MeasureSpec.getSize(heightMeasureSpec);
            int calculatedWidth = originalHeight * this.ratioWidth / this.ratioHeight;

            int finalWidth, finalHeight;

            finalWidth = calculatedWidth;
            finalHeight = originalHeight;

            super.onMeasure(
                    MeasureSpec.makeMeasureSpec(finalWidth, MeasureSpec.EXACTLY),
                    MeasureSpec.makeMeasureSpec(finalHeight, MeasureSpec.EXACTLY));
        }
    }

    /**
     * 가로세로 비율 세팅 함수
     * @param p_widthRatio
     * @param p_heightRatio
     */
    public void CF_setRatioWidthHeight(int p_widthRatio, int p_heightRatio){
        ratioWidth = p_widthRatio;
        ratioHeight = p_heightRatio;
    }
}

