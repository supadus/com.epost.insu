package com.epost.insu.adapter

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter
import com.epost.insu.fragment.IUII90M00_FD
import java.util.*

/**
 * @copyright : (주)프로니스정보기술 | http://www.phronis.co.kr
 *
 * @project   : 모바일슈랑스 구축
 * @pakage   : com.epost.insu.adapter
 * @fileName  : ReqBenefitChildPageAdapter.java
 *
 * @Title     : 자녀보험금지급청구 페이지 Adapter
 * @author    : 양지훈
 * @created   : 2018-11-09
 * @version   : 1.0
 *
 * @note      : <u>IUII90M00(자녀보험금청구 step 1), IUII90M02(step 2), IUII90M03(step 3), IUII90M04(step 4), IUII90M05(step 5), IUII90M06(step 6) IUII90M07(step 7)</u><br></br>
 * 보험금지급청구 에서 사용하는 FragmentPagerAdapter<br></br>
 * ======================================================================
 * 수정 내역
 * NO      날짜          작업자       내용
 * 01      2018-11-09    양지훈       최초 등록
 * =======================================================================
 */
class ReqBenefitChildPageAdapter(p_fm: FragmentManager?, private val arrFragment: ArrayList<IUII90M00_FD?>?) : FragmentStatePagerAdapter(p_fm!!) {
    override fun getItem(position: Int): Fragment {
        return arrFragment?.get(position)!!
    }

    override fun getCount(): Int {
        return arrFragment?.size!!
    }
}