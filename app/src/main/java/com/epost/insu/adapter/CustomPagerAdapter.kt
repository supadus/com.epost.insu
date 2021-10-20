package com.epost.insu.adapter

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter

/**
 * @copyright : (주)프로니스정보기술 | http://www.phronis.co.kr
 *
 * @project   : 모바일슈랑스 구축
 * @pakage   : com.epost.insu.adapter
 * @fileName  : CustomPagerAdapter.java
 *
 * @Title     : 커스텀 Pager Adapter
 * @author    : 이수행
 * @created   : 2017-08-02
 * @version   : 1.0
 *
 * @note      : ViewPager에서 사용하는 FragmentPagerAdapter<br></br>
 * ======================================================================
 * 수정 내역
 * NO      날짜          작업자       내용
 * 01      2017-08-02    이수행       최초 등록
 * =======================================================================
 */
class CustomPagerAdapter(p_fm: FragmentManager?, private val fragments: Array<Fragment?>) : FragmentPagerAdapter(p_fm!!) {
    override fun getItem(position: Int): Fragment {
        return fragments!![position]!!
    }

    override fun getCount(): Int {
        return fragments?.size ?: 0
    }
}