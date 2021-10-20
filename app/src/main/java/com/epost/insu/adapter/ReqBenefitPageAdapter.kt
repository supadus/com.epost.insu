package com.epost.insu.adapter

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter
import com.epost.insu.fragment.IUII10M00_FD
import java.util.*

/**
 * @copyright : (주)프로니스정보기술 | http://www.phronis.co.kr
 *
 * @project   : 모바일슈랑스 구축
 * @pakage   : com.epost.insu.adapter
 * @fileName  : ReqBenefitPageAdapter.java
 *
 * @Title     : 보험금지급청구 페이지 Adapter
 * @author    : 이수행
 * @created   : 2017-07-07
 * @version   : 1.0
 *
 * @note      : <u>IUII10M00(보험금청구 step 1), IUII11M00(step 2), IUII12M00(step 3), IUII15M00(step 4), IUII20M00(step 5), IUII30M00(step 6) IUII40M00(step 7)</u><br></br>
 * 보험금지급청구 에서 사용하는 FragmentPagerAdapter<br></br>
 * ======================================================================
 * 수정 내역
 * NO      날짜          작업자       내용
 * 01      2017-07-07    이수행       최초 등록
 * =======================================================================
 */
class ReqBenefitPageAdapter(p_fm: FragmentManager?, private val arrFragment: ArrayList<IUII10M00_FD?>?) : FragmentStatePagerAdapter(p_fm!!) {
    override fun getItem(position: Int): Fragment {
        return arrFragment?.get(position)!!
    }

    override fun getCount(): Int {
        return arrFragment?.size!!
    }
}