package com.epost.insu.adapter

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter
import com.epost.insu.fragment.IUBC10M00_FD
import java.util.*

/**
 * @copyright : 우정사업정보센터
 *
 * @project   : 모바일슈랑스 구축
 * @pakage   : com.epost.insu.adapter
 * @fileName  : SmartReqBenefitPageAdapter.java
 *
 * @Title     : 보험금지급청구 페이지 Adapter
 * @author    : 이경민
 * @created   : 2018-06-09
 * @version   : 1.0
 *
 * @note      : <u>IUBC10M00(보험금청구 step 1), IUBC11M00(step 2), IUBC12M00(step 3), IUBC13M00(step 4)</u><br></br>
 * 보험금지급청구 에서 사용하는 FragmentPagerAdapter<br></br>
 * ======================================================================
 * 수정 내역
 * NO      날짜          작업자       내용
 * 01      2018-06-09    이경민       최초 등록
 * =======================================================================
 */
class SmartReqBenefitPageAdapter(p_fm: FragmentManager?, private val arrFragment: ArrayList<IUBC10M00_FD?>?) : FragmentStatePagerAdapter(p_fm!!) {
    override fun getItem(position: Int): Fragment {
        return arrFragment?.get(position)!!
    }

    override fun getCount(): Int {
        return arrFragment?.size!!
    }
}