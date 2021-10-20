package com.epost.insu.activity

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.viewpager.widget.ViewPager.OnPageChangeListener
import com.epost.insu.*
import com.epost.insu.adapter.CustomPagerAdapter
import com.epost.insu.common.*
import com.epost.insu.fragment.IUII33M00_F
import com.epost.insu.fragment.IUII33M01_F
import java.util.*

/**
 * 보험금청구 > 7. 계좌정보확인 및 추천국/추천 선택 > 추천국/추천인선택 팝업
 * @since     : project 30:1.2.8
 * @version   : 1.0
 * @author    : NJM
 * @see IUII33M00_F     추천국 선택 Fragment
 *
 * @see IUII33M01_F     추천인 선택 Fragment
 * <pre>
 * - 추천국/추천인 선택 화면
</pre> */
class IUII33M00_P : Activity_Default() {
    private lateinit var pager: CustomViewPager

    private var fragmentDepart: IUII33M00_F? = null // 추천국 Fragment
    private var fragmentPerson: IUII33M01_F? = null // 추천인 Fragment

    private var fragmentDvsn= 0 // 프로그먼트번호 (추천국:0, 추천인:1)
    private lateinit var btnBack: ImageButton  // '뒤로' 버튼

    //private TextView textBtnCancel;               // 취소 버튼(TextView)
    // 선택된 추천국/추천인
    private var departCode: String? = ""
    private var departName: String? = ""
    private var personRemnNo: String? = ""
    private var personName: String? = ""
    private var srchDepartName: String? = "" // 추천국 이름 검색어
    private var srchPersonName: String? = "" // 추천국 이름 검색어
    override fun onSaveInstanceState(p_bundle: Bundle) {
        super.onSaveInstanceState(p_bundle)
        LogPrinter.CF_debug("!----------------------------------------------------------")
        LogPrinter.CF_debug("!-- IUII33M00_P.onSaveInstanceState()")
        LogPrinter.CF_debug("!----------------------------------------------------------")
        p_bundle.putInt("currentPageIndex", pager!!.currentItem)
        if (fragmentDepart != null && fragmentDepart!!.isAdded) {
            supportFragmentManager.putFragment(p_bundle, IUII33M00_F::class.java.name, fragmentDepart!!)
        }
        if (fragmentPerson != null && fragmentPerson!!.isAdded) {
            supportFragmentManager.putFragment(p_bundle, IUII33M01_F::class.java.name, fragmentPerson!!)
        }
    }

    override fun onRestoreInstanceState(p_bundle: Bundle) {
        super.onRestoreInstanceState(p_bundle)
        LogPrinter.CF_debug("!----------------------------------------------------------")
        LogPrinter.CF_debug("!-- IUII33M00_P.onRestoreInstanceState()")
        LogPrinter.CF_debug("!----------------------------------------------------------")
        if (p_bundle.containsKey("currentPageIndex")) {
            pager!!.currentItem = p_bundle.getInt("currentPageIndex")
        }
    }

    /**
     * abstract에 의해서 onCreate()보다 먼저 실행됨
     */
    override fun setInit() {
        LogPrinter.CF_debug("!----------------------------------------------------------")
        LogPrinter.CF_debug("!-- IUII33M00_P.setInit()")
        LogPrinter.CF_debug("!----------------------------------------------------------")
        setContentView(R.layout.iuii33m00)
    }

    override fun setUIControl() {
        LogPrinter.CF_debug("!----------------------------------------------------------")
        LogPrinter.CF_debug("!-- IUII33M00_P.setUIControl()")
        LogPrinter.CF_debug("!----------------------------------------------------------")

        // 타이틀바 UI
        setTitleBarUI()
        pager = findViewById(R.id.activity_search_req_benefit_viewPager)
        pager.CF_setScrollCompulsion(false) // 스크롤 막기 (무슨기능인지..)
        pager.CF_setPagingEnabled(false) // 페이징 슬라이드 가능
        pager.setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_NO)
        pager.addOnPageChangeListener(object : OnPageChangeListener {
            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
                // 스크롤 막음
            }

            override fun onPageSelected(position: Int) {
                // -- 접근성 대체 Text 변경 : 청구 완료 화면에서는 닫기, 나머지 화면에서는 이전 화면 이동
                if (CommonFunction.CF_checkAccessibilityTurnOn(this@IUII33M00_P)) {
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
        LogPrinter.CF_debug("!-- IUII33M00_P.onCreate()")
        LogPrinter.CF_debug("!----------------------------------------------------------")
        setIntentData() // Intent 데이터 세팅 함수
        setFragments(savedInstanceState) // Fragment 세팅
        val adapter = CustomPagerAdapter(this.supportFragmentManager, arrayOf<Fragment?>(fragmentDepart, fragmentPerson))
        pager!!.adapter = adapter
        pager!!.currentItem = fragmentDvsn // Fragment 번호
    }

    override fun onBackPressed() {
        LogPrinter.CF_debug("!----------------------------------------------------------")
        LogPrinter.CF_debug("!-- IUII33M00_P.onBackPressed()")
        LogPrinter.CF_debug("!----------------------------------------------------------")

        // -- 현재 상세조회 화면인 경우 목록 화면으로 이동 시킨다.
        if (pager!!.currentItem == 1) {
            pager!!.currentItem = 0
        } else {
            super.onBackPressed()
        }
    }

    /**
     * Intent 데이터 세팅 함수
     */
    private fun setIntentData() {
        LogPrinter.CF_debug("!----------------------------------------------------------")
        LogPrinter.CF_debug("!-- IUII33M00_P.setIntentData()")
        LogPrinter.CF_debug("!----------------------------------------------------------")
        if (intent != null) {
            if (intent.hasExtra("fragmentDvsn")) {
                fragmentDvsn = intent.getIntExtra("fragmentDvsn", 0)
            }
            if (intent.hasExtra("departCode")) {
                departCode = Objects.requireNonNull(intent.extras)?.getString("departCode")
            }
            if (intent.hasExtra("departName")) {
                departName = Objects.requireNonNull(intent.extras)?.getString("departName")
                LogPrinter.CF_debug("!---- setIntentData(departName):::::$departName")
            }
            if (intent.hasExtra("personRemnNo")) {
                personRemnNo = Objects.requireNonNull(intent.extras)?.getString("personRemnNo")
            }
            if (intent.hasExtra("personName")) {
                personName = Objects.requireNonNull(intent.extras)?.getString("personName")
            }
            // 검색어
            if (intent.hasExtra("srchDepartName")) {
                srchDepartName = Objects.requireNonNull(intent.extras)?.getString("srchDepartName")
                LogPrinter.CF_debug("!---- setIntentData(srchDepartName)::::$srchDepartName")
            }
            if (intent.hasExtra("srchPersonName")) {
                srchPersonName = Objects.requireNonNull(intent.extras)?.getString("srchPersonName")
                LogPrinter.CF_debug("!---- setIntentData(srchPersonName)::::$srchPersonName")
            }
        }
    }

    /**
     * Fragment 세팅
     */
    private fun setFragments(p_bundle: Bundle?) {
        LogPrinter.CF_debug("!----------------------------------------------------------")
        LogPrinter.CF_debug("!-- IUII33M00_P.setFragments()")
        LogPrinter.CF_debug("!----------------------------------------------------------")
        if (p_bundle != null) {
            fragmentDepart = supportFragmentManager.getFragment(p_bundle, IUII33M00_F::class.java.name) as IUII33M00_F?
            fragmentPerson = supportFragmentManager.getFragment(p_bundle, IUII33M01_F::class.java.name) as IUII33M01_F?
        } else {
            fragmentDepart = Fragment.instantiate(this, IUII33M00_F::class.java.name) as IUII33M00_F
            fragmentPerson = Fragment.instantiate(this, IUII33M01_F::class.java.name) as IUII33M01_F
        }

        // -- 추천국 아이템 선택 이벤트
        fragmentDepart!!.CE_setOnListItemClickedEventListener { p_index -> // 추천국 코드&네임 저장
            departCode = fragmentDepart!!.CF_getReqCode(p_index)
            departName = fragmentDepart!!.CF_getReqName(p_index)
            srchDepartName = fragmentDepart!!.CF_getSrchDepartName()

            // -- 추천인 프래그먼트 호출
            if (fragmentPerson != null && fragmentPerson!!.isAdded) {
                // 기존 추천인 변수 초기화
                personName = ""
                personRemnNo = ""
                fragmentPerson!!.requestList(true)
            }
            pager!!.currentItem = 1
        }

        // -- 추천인 아이템 선택 이벤트
        fragmentPerson!!.CE_setOnListItemClickedEventListener { p_index -> // 추천인 번호&이름 저장
            personRemnNo = fragmentPerson!!.CF_getReqRemnNo(p_index)
            personName = fragmentPerson!!.CF_getName(p_index)

            // 리턴 후 종료
            val intent = Intent()
            intent.putExtra("departCode", departCode)
            intent.putExtra("departName", departName)
            intent.putExtra("personRemnNo", personRemnNo)
            intent.putExtra("personName", personName)
            intent.putExtra("srchDepartName", srchDepartName)
            intent.putExtra("srchPersonName", srchPersonName)
            LogPrinter.CF_debug("## 추천국/추천인 조회 성공 ")
            setResult(RESULT_OK, intent)
            finish()
        }
    }

    /**
     * 타이틀바 레이아웃 세팅
     */
    private fun setTitleBarUI() {
        LogPrinter.CF_debug("!----------------------------------------------------------")
        LogPrinter.CF_debug("!-- IUII33M00_P.setTitleBarUI()")
        LogPrinter.CF_debug("!----------------------------------------------------------")

        // -- 타이틀 세팅
        val tmp_title = findViewById<TextView>(R.id.title_bar_textTitle)
        tmp_title.text = resources.getString(R.string.title_choice_recomm)

        // -- left 버튼 세팅
        btnBack = findViewById(R.id.title_bar_imgBtnLeft)
        btnBack.setVisibility(View.VISIBLE)
        btnBack.setOnClickListener(View.OnClickListener { // -1- 현재 추천인선택 화면인 경우 추천국 선택 화면으로 이동 시킨다.
            if (pager!!.currentItem == 1) {
                pager!!.currentItem = 0
            } else {
                finish()
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
     * 선택된 추천국 이름 반환
     * @return departName String
     */
    fun CF_getDepartName(): String? {
        return departName
    }

    /**
     * 선택된 추천국 코드 반환
     * @return departCode String
     */
    fun CF_getDepartCode(): String? {
        return departCode
    }

    /**
     * 선택된 추천인 번호 반환
     * @return personRemnNo String
     */
    fun CF_getPersonRemnNo(): String? {
        return personRemnNo
    }

    /**
     * 선택된 추천인 번호 반환
     * @return personName String
     */
    fun CF_getPersonName(): String? {
        return personName
    }

    /**
     * 추천국 검색어 반환
     * @return personRemnNo String
     */
    fun CF_getSrchDepartName(): String? {
        return srchDepartName
    }

    /**
     * 추천인 검색어 반환
     * @return personRemnNo String
     */
    fun CF_getSrchPersonName(): String? {
        return srchPersonName
    }
}