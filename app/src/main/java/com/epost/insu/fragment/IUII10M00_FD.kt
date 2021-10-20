package com.epost.insu.fragment

import android.content.Context
import android.widget.Button
import androidx.core.widget.NestedScrollView
import com.epost.insu.activity.IUII10M00_P
import com.epost.insu.common.LogPrinter

/**
 * @copyright : (주)프로니스정보기술 | http://www.phronis.co.kr
 *
 * @project   : 모바일슈랑스 구축
 * @pakage   : com.epost.insu.fragment
 * @fileName  : IUII10M00_FD.java
 *
 * @Title     : 보험금지급청구 관련 (IUII10M01_F ~ IUII10M09_F) 상위 Fragment
 * @author    : 이수행
 * @created   : 2017-11-17
 * @version   : 1.0
 *
 * @note      : <u>보험금지급청구 관련 (IUII10M01_F ~ IUII10M09_F) 상위 Fragment</u><br></br>
 * ======================================================================
 * 수정 내역
 * NO      날짜          작업자       내용
 * 01      2017-11-17    이수행       최초 등록
 * =======================================================================
 */
open class IUII10M00_FD constructor() : Fragment_Default() {
    var mActivity // 보험금지급청구 Activity
            : IUII10M00_P? = null
    var scrollView // Fragment ScrollView
            : NestedScrollView? = null
    var btnNext // 다음 버튼
            : Button? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)
        LogPrinter.CF_debug("!----------------------------------------------------------")
        LogPrinter.CF_debug("!-- IUII10M00_FD.onAttach()")
        LogPrinter.CF_debug("!----------------------------------------------------------")
        mActivity = getActivity() as IUII10M00_P?
        if (mActivity == null) {
            LogPrinter.CF_debug("!---- mActivity null")
        } else {
            LogPrinter.CF_debug("!---- mActivity null 아님")
        }
    }

    fun CF_setBtnNextEnabled() {
        if (btnNext != null) {
            btnNext!!.setEnabled(true)
        }
    }

    /**
     * ScrollView Top 포지션 이동
     */
    fun CF_scrollTop() {
        if (scrollView != null) {
            scrollView!!.scrollTo(0, 0)
        }
    }
}