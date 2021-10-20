package com.epost.insu.fragment

import android.content.Context
import android.widget.Button
import androidx.core.widget.NestedScrollView
import com.epost.insu.activity.BC.IUBC10M00_P

/**
 * @copyright : 우정사업정보센터
 *
 * @project   : 모바일슈랑스 구축
 * @pakage   : com.epost.insu.fragment
 * @fileName  : IUBC10M00_FD.java
 *
 * @Title     : 보험금지급청구 관련 (IUBC10M00_F ~~ IUBC13M00_F) 상위 Fragment
 * @author    : 이경민
 * @created   : 2018-06-09
 * @version   : 1.0
 *
 * @note      : <u>보험금지급청구 관련 (IUBC10M00_F ~~ IUBC13M00_F) 상위 Fragment</u><br></br>
 * ======================================================================
 * 수정 내역
 * NO      날짜          작업자       내용
 * 01      2018-06-09    이경민       최초 등록
 * =======================================================================
 */
open class IUBC10M00_FD : Fragment_Default() {
    var mActivity // 보험금지급청구 Activity
            : IUBC10M00_P? = null
    var btnNext // 다음 버튼
            : Button? = null
    var scrollView // Fragment ScrollView
            : NestedScrollView? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mActivity = activity as IUBC10M00_P?
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