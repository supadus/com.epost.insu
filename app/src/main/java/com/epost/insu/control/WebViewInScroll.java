package com.epost.insu.control;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.webkit.WebView;

/**
 * @copyright : (주)프로니스정보기술 | http://www.phronis.co.kr
 *
 * @project   : 모바일슈랑스 구축
 * @pakage   : com.epost.insu.control
 * @fileName  : WebViewInScroll.java
 *
 * @Title     : ScrollView 자식으로 포함되어도 스크롤이 가능한 webView
 * @author    : 이수행
 * @created   : 2017-11-13
 * @version   : 1.0
 *
 * @note      : <u>ScrollView 자식으로 포함되어도 스크롤이 가능한 webView</u><br/>
 * ======================================================================
 * 수정 내역
 * NO      날짜          작업자       내용
 * 01      2017-11-13    이수행       최초 등록
 * =======================================================================
 */
@SuppressWarnings("FieldCanBeLocal")
public class WebViewInScroll extends WebView{

    private boolean flagScrllIsBottom = false;


    public WebViewInScroll(Context context) {
        super(context);
    }

    public WebViewInScroll(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public WebViewInScroll(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {

        requestDisallowInterceptTouchEvent(true);
        //requestDisallowInterceptTouchEvent(!flagScrllIsBottom);


        return super.onTouchEvent(event);
    }

    @Override
    protected void onOverScrolled(int scrollX, int scrollY, boolean clampedX, boolean clampedY) {
        super.onOverScrolled(scrollX, scrollY, clampedX, clampedY);

        flagScrllIsBottom = scrollY > 0 && clampedY;
    }

    @Override
    protected void onScrollChanged(int scrollX, int scrollY, int oldScrollX, int oldScrollY) {
        super.onScrollChanged(scrollX, scrollY, oldScrollX, oldScrollY);

    }
}
