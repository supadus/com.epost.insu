package com.epost.insu.control;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;

import com.epost.insu.R;

/**
 * @copyright : (주)프로니스정보기술 | http://www.phronis.co.kr
 *
 * @project   : 모바일슈랑스 구축
 * @pakage   : com.epost.insu.control
 * @fileName  : CustomRatioPhotoView.java
 *
 * @Title     : 비율 설정이 가능한 {@link CustomPhotoView}
 * @author    : 이수행
 * @created   : 2017-10-26
 * @version   : 1.0
 *
 * @note      : <u>비율 설정이 가능한 {@link CustomPhotoView}</u><br/>
 * ======================================================================
 * 수정 내역
 * NO      날짜          작업자       내용
 * 01      2017-10-26    이수행       최초 등록
 * =======================================================================
 */
public class CustomRatioPhotoView extends CustomPhotoView{

    private int ratioWidth;
    private int ratioHeight;
    private boolean flagStandWidth=true;

    /**
     * 생성자
     * @param context
     * @param p_previewResId
     * @param p_ratioWidth
     * @param p_ratioHeight
     */
    public CustomRatioPhotoView(Context context, int p_previewResId, int p_ratioWidth, int p_ratioHeight) {
        super(context,p_previewResId);

        this.ratioWidth = p_ratioWidth;
        this.ratioHeight = p_ratioHeight;
    }
    public CustomRatioPhotoView(Context context, Drawable p_preViewDrawable, int p_ratioWidth, int p_ratioHeight) {
        super(context,p_preViewDrawable);

        this.ratioWidth = p_ratioWidth;
        this.ratioHeight = p_ratioHeight;
    }
    public CustomRatioPhotoView(Context context, AttributeSet attrs) {
        super(context, attrs);

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
}
