package com.epost.insu.activity

import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.viewpager.widget.ViewPager.OnPageChangeListener
import com.epost.insu.*
import com.epost.insu.EnvConfig.AuthDvsn
import com.epost.insu.adapter.CustomPagerAdapter
import com.epost.insu.common.*
import com.epost.insu.dialog.CustomDialog
import com.epost.insu.fragment.IUII50M00_F
import com.epost.insu.fragment.IUII51M00_F

/**
 * @copyright : (주)프로니스정보기술 | http://www.phronis.co.kr
 *
 * @project   : 모바일슈랑스 구축
 * @pakage    : com.epost.insu.activity
 * @fileName  : IUII50M00_P.java
 *
 * @Title     : 보험금청구 > 보험금청구조회 ( 화면 ID : IUII50M00, IUII51M00) - #25
 * @author    : 이수행
 * @created   : 2017-10-19
 * @version   : 1.0
 *
 * @note      : IUII50M00 보험금청구 조회, IUII51M00 보험금청구 조회 상세 Fragment로 구성
 *
 * @see IUII50M00_F
 *
 * @see IUII51M00_F
 * ======================================================================
 * 수정 내역
 * NO       날짜          작업자       내용
 * 01       2017-10-19    이수행     : 최초 등록
 * 02       2020-01-22    노지민     : 공통 인증유형/청구유형 추가에 따른 로직수정
 * 03       2020-03-04    노지민     : 보험금청구조회에서 이미지 조회 및 접수취소 기능 추가
 * 04       2020-10-28    노지민     : [모바일 사진촬영 패키지 도입]
 * =======================================================================
 */
class IUII50M00_P : Activity_Default() {
    // #############################################################################################
    // Getter & Setter
    // #############################################################################################
    var fragmentList: IUII50M00_F? = null // 보험금청구조회 목록 Fragment
    private var fragmentDetail: IUII51M00_F? = null // 보험금청구조회 상세 Fragment

    private lateinit var pager: CustomViewPager
    private lateinit var btnBack: ImageButton // '뒤로' 버튼

    //private TextView textBtnCancel;                       // 취소 버튼(TextView)
    private var mAuthDvsn: AuthDvsn? = null // 로그인 인증유형 (Preferences)
    private var mCsno: String? = null // 로그인 고객번호 (Preferences)

    override fun onSaveInstanceState(p_bundle: Bundle) {
        super.onSaveInstanceState(p_bundle)
        LogPrinter.CF_debug("!----------------------------------------------------------")
        LogPrinter.CF_debug("!-- IUII50M00_P.onSaveInstanceState()")
        LogPrinter.CF_debug("!----------------------------------------------------------")
        p_bundle.putInt("currentPageIndex", pager!!.currentItem)
        if (fragmentList != null && fragmentList!!.isAdded) {
            supportFragmentManager.putFragment(p_bundle, IUII50M00_F::class.java.name, fragmentList!!)
        }
        if (fragmentDetail != null && fragmentDetail!!.isAdded) {
            supportFragmentManager.putFragment(p_bundle, IUII51M00_F::class.java.name, fragmentDetail!!)
        }
    }

    override fun onRestoreInstanceState(p_bundle: Bundle) {
        super.onRestoreInstanceState(p_bundle)
        if (p_bundle.containsKey("currentPageIndex")) {
            pager!!.currentItem = p_bundle.getInt("currentPageIndex")
        }
    }

    override fun setInit() {
        setContentView(R.layout.iuii50m00)

        // -- 초기화
        mAuthDvsn = SharedPreferencesFunc.getLoginAuthDvsn(applicationContext)
        mCsno = SharedPreferencesFunc.getLoginCsno(applicationContext)
    }

    override fun setUIControl() {

        // -- 타이틀바 UI
        setTitleBarUI()
        pager = findViewById(R.id.activity_search_req_benefit_viewPager)
        pager.CF_setPagingEnabled(false)
        pager.setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_NO)
        pager.addOnPageChangeListener(object : OnPageChangeListener {
            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {}
            override fun onPageSelected(position: Int) {
                // 페이지의 다음 버튼 활성화
                //arrFragment.get(position).CF_setBtnNextEnabled();
                //arrFragment.get(position).CF_scrollTop();

                // ---------------------------------------------------------------------------------
                //  접근성 대체 Text 변경 : 청구 완료 화면에서는 닫기, 나머지 화면에서는 이전 화면 이동
                // ---------------------------------------------------------------------------------
                if (CommonFunction.CF_checkAccessibilityTurnOn(this@IUII50M00_P)) {

                    //CF_requestFocusIndicator();
                    if (position < 1) {
                        btnBack!!.contentDescription = resources.getString(R.string.desc_close)
                    } else {
                        btnBack!!.contentDescription = resources.getString(R.string.desc_back)
                    }
                }
            }

            override fun onPageScrollStateChanged(state: Int) {}
        })
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        LogPrinter.CF_debug("!----------------------------------------------------------")
        LogPrinter.CF_debug("!-- IUII50M00_P.onCreate()")
        LogPrinter.CF_debug("!----------------------------------------------------------")

        // Fragment 세팅
        setFragments(savedInstanceState)
        val adapter = CustomPagerAdapter(this.supportFragmentManager, arrayOf<Fragment?>(fragmentList, fragmentDetail))
        pager!!.adapter = adapter
    }

    override fun onResume() {
        super.onResume()
        LogPrinter.CF_debug("!----------------------------------------------------------")
        LogPrinter.CF_debug("!-- IUII50M00_P.onResume()")
        LogPrinter.CF_debug("!----------------------------------------------------------")
        LogPrinter.CF_debug("!---- 인증유형(로그인구분) ::::$mAuthDvsn")
        LogPrinter.CF_debug("!---- 고객CSNO ::::::::::::::::$mCsno")
    }

    override fun onBackPressed() {
        if (pager!!.currentItem == 1) {
            // -------------------------------------------------------------
            //  현재 상세조회 화면인 경우 목록 화면으로 이동 시킨다.
            // -------------------------------------------------------------
            pager!!.currentItem = 0
            if (fragmentDetail != null && fragmentDetail!!.isAdded) {
                fragmentDetail!!.CF_clearData()
                fragmentDetail!!.CF_scrollTop()
            }
        } else {
            super.onBackPressed()
        }
    }

    /**
     * Fragment 세팅
     */
    private fun setFragments(p_bundle: Bundle?) {
        if (p_bundle != null) {
            fragmentList = supportFragmentManager.getFragment(p_bundle, IUII50M00_F::class.java.name) as IUII50M00_F?
            fragmentDetail = supportFragmentManager.getFragment(p_bundle, IUII51M00_F::class.java.name) as IUII51M00_F?
        } else {
            fragmentList = Fragment.instantiate(this, IUII50M00_F::class.java.name) as IUII50M00_F
            fragmentDetail = Fragment.instantiate(this, IUII51M00_F::class.java.name) as IUII51M00_F
        }

        // -- 보험금청구조회 목록 (상세조회) 이벤트
        fragmentList!!.CE_setOnListItemClickedEventListener { p_index ->
            if (fragmentDetail != null && fragmentDetail!!.isAdded) {
                fragmentDetail!!.CF_requestDetail(fragmentList!!.CF_getReqId(p_index))
            }
            pager!!.currentItem = 1
        }
    }

    /**
     * 타이틀바 레이아웃 세팅
     */
    private fun setTitleBarUI() {
        // -- 타이틀 세팅
        val tmp_title = findViewById<TextView>(R.id.title_bar_textTitle)
        tmp_title.text = resources.getString(R.string.title_search_req_benefit)

        // -- left 버튼 세팅
        btnBack = findViewById(R.id.title_bar_imgBtnLeft)
        btnBack.setVisibility(View.VISIBLE)
        btnBack.setOnClickListener(View.OnClickListener {
            // -- (현재 상세조회화면)
            if (pager!!.currentItem == 1) {
                // -------------------------------------------------------------
                //  현재 상세조회화면인 경우 목록 화면으로 이동 시킨다.
                // -------------------------------------------------------------
                pager!!.currentItem = 0
                if (fragmentDetail != null && fragmentDetail!!.isAdded) {
                    fragmentDetail!!.CF_clearData()
                    fragmentDetail!!.CF_scrollTop()
                }
            } else {
                // -- (휴대폰인증일 경우)
                if (mAuthDvsn == AuthDvsn.MOBILE) {
                    showDlgOfCancel()
                } else {
                    finish()
                }
            }
        })
    }

    /**
     * ViewPager current item 세팅
     * @param p_pageIndex   int
     */
    fun CF_setCurrentPage(p_pageIndex: Int) {
        pager!!.currentItem = p_pageIndex
    }

    /**
     * 목록 Fragment의 구비서류 보완 여부 값 업데이트 : false 세팅
     * @param p_reqId   String
     */
    fun CF_updateFlagNeedModValue(p_reqId: String?) {
        if (fragmentList != null && fragmentList!!.isAdded) {
            fragmentList!!.CF_updateAddDocComplete(p_reqId)
        }
    }

    /**
     * 청구구분 조회
     * @return  EnvConfig.AuthDvsn
     */
    fun CF_getAuthDvsn(): AuthDvsn? {
        return mAuthDvsn
    }

    /**
     * 고객번호 조회
     * @return  String
     */
    fun CF_getCsno(): String? {
        return mCsno
    }
    // #############################################################################################
    //  Dialog
    // #############################################################################################
    //    /**
    //     * 뒤로 가기 버튼의 이미지 세팅
    //     */
    //    private void setImageResourOfBackBtn(){
    //
    //        if(bSelfReqInquery){
    //
    //            if(pager.getCurrentItem() == 0){
    //                btnBack.setImageResource(R.drawable.ic_close_3);
    //            }else{
    //                btnBack.setImageResource(R.drawable.ic_prev);
    //            }
    //        }
    //    }
    /**
     * 보험금청구신청 취소 다이얼로그 show 함수
     */
    private fun showDlgOfCancel() {
        showCustomDialog(resources.getString(R.string.dlg_cancel_req_inquery),
                resources.getString(R.string.btn_no),
                resources.getString(R.string.btn_yes),
                btnBack!!
        ) { dialog ->
            if ((dialog as CustomDialog).CF_getCanceled() == false) {
                finish()
            } else if (CommonFunction.CF_checkAccessibilityTurnOn(applicationContext)) {
                clearAllFocus()
                btnBack!!.requestFocus()
            }
            btnBack!!.isFocusableInTouchMode = false
        }
    }
}