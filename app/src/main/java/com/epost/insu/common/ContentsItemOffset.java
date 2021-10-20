package com.epost.insu.common;

import android.graphics.Rect;
import androidx.recyclerview.widget.RecyclerView;
import android.view.View;

/**
 * @copyright : (주)프로니스정보기술 | http://www.phronis.co.kr
 *
 * @project   : 모바일슈랑스 구축
 * @pakage   : com.epost.insu.common
 * @fileName  : ContentsItemOffset.java
 *
 * @Title     : RecyclerView 아이템 데코레이션
 * @author    : 이수행
 * @created   : 2017-10-10
 * @version   : 1.0
 *
 * @note      : <u>RecyclerView 아이템 데코레이션</u><br/>
 * ======================================================================
 * 수정 내역
 * NO      날짜          작업자       내용
 * 01      2017-10-10    이수행       최초 등록
 * =======================================================================
 */
public class ContentsItemOffset extends RecyclerView.ItemDecoration {

    private int spacinngLeft;
    private int spacingTop;
    private int spacingRight;
    private int spacingBottom;


    @SuppressWarnings("unused")
    public ContentsItemOffset(int p_spacing)
    {
        this.spacinngLeft = p_spacing;
        this.spacingTop = p_spacing;
        this.spacingRight = p_spacing;
        this.spacingBottom = p_spacing;
    }

    public ContentsItemOffset(int p_left, int p_top, int p_right, int p_bottom){
        this.spacinngLeft = p_left;
        this.spacingTop = p_top;
        this.spacingRight = p_right;
        this.spacingBottom = p_bottom;
    }

    @Override
    public void getItemOffsets(Rect outRect, View view, RecyclerView parent,
                               RecyclerView.State state) {
        outRect.set(spacinngLeft,spacingTop,spacingRight,spacingBottom);
    }
}