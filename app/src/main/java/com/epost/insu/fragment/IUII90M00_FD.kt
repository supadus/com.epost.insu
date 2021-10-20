package com.epost.insu.fragment

import android.content.Context
import android.widget.Button
import androidx.core.widget.NestedScrollView
import com.epost.insu.activity.IUII90M00_P

/**
 * @copyright : (주)프로니스정보기술 | http://www.phronis.co.kr
 *
 * @project   : 모바일슈랑스 구축
 * @pakage   : com.epost.insu.fragment
 * @fileName  : IUII90M00_FD.java
 *
 * @Title     : 보험금지급청구 관련 (IUII90M01_F ~~ IUII90M09_F) 상위 Fragment
 * @author    : 양지훈
 * @created   : 2018-11-09
 * @version   : 1.0
 *
 * @note      : <u>보험금지급청구 관련 (IUII90M01_F ~~ IUII90M09_F) 상위 Fragment</u><br></br>
 * ======================================================================
 * 수정 내역
 * NO      날짜          작업자       내용
 * 01      2018-11-09    양지훈       최초 등록
 * =======================================================================
 */
open class IUII90M00_FD : Fragment_Default() {
    var mActivity: IUII90M00_P? = null // 보험금지급청구 Activity
    var btnNext: Button? = null // 다음 버튼
    var scrollView: NestedScrollView? = null // Fragment ScrollView

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mActivity = activity as IUII90M00_P?
    }

    fun CF_setBtnNextEnabled() {
        if (btnNext != null) {
            btnNext!!.isEnabled = true
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