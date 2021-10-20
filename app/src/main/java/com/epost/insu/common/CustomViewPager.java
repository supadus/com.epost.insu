package com.epost.insu.common;

import android.content.Context;
import androidx.viewpager.widget.ViewPager;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;

import com.epost.insu.event.OnTapEventListener;

/**
 * @copyright : (주)프로니스정보기술 | http://www.phronis.co.kr
 *
 * @project   : 모바일슈랑스 구축
 * @pakage   : com.epost.insu.common
 * @fileName  : CustomViewPager.java
 *
 * @Title     : Custom ViewPager
 * @author    : 이수행
 * @created   : 2017-10-26
 * @version   : 1.0
 *
 * @note      : <u>Custom ViewPager</u><br/>
 *               {@link #CF_setPagingEnabled(boolean)} 페이징 기능 disable 가능
 *               {@link #CF_setScrollCompulsion(boolean)} 스크롤 disable 가능
 *               {@link #CE_setOnTapEventListener(OnTapEventListener)} Tap 제스쳐 수신 가능
 * ======================================================================
 * 수정 내역
 * NO      날짜          작업자       내용
 * 01      2017-10-26    이수행       최초 등록
 * =======================================================================
 */
public class CustomViewPager extends ViewPager{

    private boolean flag_isPagingEnabled;       // flag 페이징 기능
    private boolean flag_isScrollCompulsion;    // flag Scroll  이벤트 강제 막기

    private GestureDetector gestureTap;             // 제스쳐 (탭)
    private OnTapEventListener listener;            // 탭 이벤트 리스너

    public CustomViewPager(Context context){
        super(context);
        this.flag_isPagingEnabled = true;
        this.flag_isScrollCompulsion = false;
    }

    public CustomViewPager(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.flag_isPagingEnabled = true;
        this.flag_isScrollCompulsion = false;
    }


    @Override
    public boolean onTouchEvent(MotionEvent event) {

        if(gestureTap != null){
            gestureTap.onTouchEvent(event);
        }

        if (this.flag_isPagingEnabled) {
            return super.onTouchEvent(event);
        }

        return false;
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent event) {
        if (this.flag_isPagingEnabled) {
            return super.onInterceptTouchEvent(event);
        }

        return false;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        int mode = MeasureSpec.getMode(heightMeasureSpec);
        if (mode == MeasureSpec.UNSPECIFIED || mode == MeasureSpec.AT_MOST) {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
            int height = 0;
            for (int i = 0; i < getChildCount(); i++) {
                View child = getChildAt(i);
                child.measure(widthMeasureSpec, MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED));
                int h = child.getMeasuredHeight();
                if (h > height) height = h;
            }
            heightMeasureSpec = MeasureSpec.makeMeasureSpec(height, MeasureSpec.EXACTLY);
        }
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    /**
     * ViewPager 페이징 가능 여부 세팅<br/>
     * default true 페이징 가능, false 세팅 시 페이징이 안된다.
     *
     * @param p_flagIsPagingEnable  boolean
     */
    public void CF_setPagingEnabled(boolean p_flagIsPagingEnable) {
        this.flag_isPagingEnabled = p_flagIsPagingEnable;
    }


    /**
     * 스크롤 강제 flag 값 세팅 함수<br/>
     * true 세팅 시 {@link #canScroll(View, boolean, int, int, int)}가 false를 반환한다.
     * @param p_flagScrollCompulsion    boolean
     */
    public void CF_setScrollCompulsion(boolean p_flagScrollCompulsion){
        this.flag_isScrollCompulsion = p_flagScrollCompulsion;
    }


    @Override
    protected boolean canScroll(View v, boolean checkV, int dx, int x, int y) {

        if(this.flag_isScrollCompulsion)
            return false;

        return super.canScroll(v, checkV, dx, x, y);
    }


    /**
     * Tap 이벤트 리스너 세팅 함수
     * @param p_listener    OnTapEventListener
     */
    public void CE_setOnTapEventListener(OnTapEventListener p_listener){
        gestureTap = new GestureDetector(getContext(), new TapGesture(){});
        listener = p_listener;
    }


    /**
     * @copyright : (주)프로니스정보기술 | http://www.phronis.co.kr
     *
     * @project   : 모바일슈랑스 구축
     * @pakage   : com.epost.insu.common
     * @fileName  : CustomViewPager.java
     *
     * @Title     : 탭 제스쳐 디텍터
     * @author    : 이수행
     * @created   : 2017-11-21
     * @version   : 1.0
     *
     * @note      : <u>탭 제스쳐 디텍터</u><br/>
     * ======================================================================
     * 수정 내역
     * NO      날짜          작업자       내용
     * 01      2017-11-21    이수행       최초 등록
     * =======================================================================
     */
    private class TapGesture extends GestureDetector.SimpleOnGestureListener{

        /**
         * 싱글탭
         * @param   e   MotionEvent
         * @return      boolean
         */
        @Override
        public boolean onSingleTapConfirmed(MotionEvent e) {

            if(listener != null){
                listener.onTap(getCurrentItem());
            }
            return true;
        }

        /**
         * 더블탭
         * @param e     MotionEvent
         * @return      boolean
         */
        @Override
        public boolean onDoubleTap(MotionEvent e) {
            if(listener != null){
                listener.onTap(getCurrentItem());
            }
            return true;
        }
    }
}
