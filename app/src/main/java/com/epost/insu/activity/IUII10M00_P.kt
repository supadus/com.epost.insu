package com.epost.insu.activity

import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.*
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.widget.ImageButton
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.viewpager.widget.ViewPager.OnPageChangeListener
import com.epost.insu.*
import com.epost.insu.EnvConfig.AuthDvsn
import com.epost.insu.adapter.ReqBenefitPageAdapter
import com.epost.insu.common.*
import com.epost.insu.control.StepIndicator
import com.epost.insu.data.Data_IUII10M00
import com.epost.insu.dialog.CustomDialog
import com.epost.insu.fragment.*
import com.epost.insu.module.PSMobileModule
import com.epost.insu.network.HttpConnections
import com.epost.insu.network.WeakReferenceHandler
import com.epost.insu.network.WeakReferenceHandler.ObjectHandlerMessage
import com.google.android.material.appbar.AppBarLayout
import org.json.JSONException
import org.json.JSONObject
import java.util.*

/**
 * 보험금청구 > 본인보험금 신청
 * @since     :
 * @version   : 1.7
 * @author    : LSH
 * @see
 * <pre>
 * 보험금청구접수신청 각 화면(Step 1 ~ Step 8)을 관리하는 Activity.
 * ======================================================================
 * 0.0.0    LSH_20170919    최초 등록
 * 0.0.0    NJM_20190802    8단계 삭제
 * 0.0.0    NJM_20190919    최근 청구 계좌 & 우체국 계좌 조회 후 화면에 초기세팅
 * 0.0.0    NJM_20191002    (자녀)지급청구 마지막 단계 하얀화면 문제 해결 (3단계에서 고객휴대폰 번호 소실되는 문제 수정)
 * 0.0.0    NJM_20200122    공통 인증유형/청구유형 추가에 따른 로직수정
 * 0.0.0    NJM_20201028    [모바일 사진촬영 패키지 도입]
 * 0.0.0    YJH_20201210    고객 부담보내역 조회 추가
 * 0.0.0    YJH_20201210    카카오페이인증 추가
 * 1.5.2    NJM_20210318    [액티비티 재실행시 사진첨부오류(~준비되지 않은) 수정] psMobileModule 호출 위치 변경
 * 1.5.2    NJM_20210322    [subUrl 공통파일로 변경]
 * 1.5.4    NJM_20210504    [간편인증 전자서명 추가] 간편인증 전자서명 로직 추가
 * 1.6.2    NJM_20210802    [2021년 대우 취약점] 2차본 : private 배열에 public 데이터 할당
 * 1.6.2    NJM_20210802    [2021년 대우 취약점] 2차본 : 부적절한 예외 처리
 * =======================================================================
 * copyright : (주)프로니스정보기술 | http://www.phronis.co.kr
 * </pre>
 */
class IUII10M00_P : Activity_Default(), ObjectHandlerMessage {
    private val HANDLERJOB_SUBMIT       = 4 // 8단계 요청 성공
    private val HANDLERJOB_ERROR_SUBMIT = 5 // 8단계 요청 실패

    // 페이지 배열 Index (배열값 -1)
    private val agreePageIndex     = 2 - 1 // 2단계. 사용자동의 페이지 (개인정보처리동의)
    private val userInfoPageIndex  = 3 - 1 // 3단계. 사용자정보 입력 페이지 (개인정보 확인 및 입력)
    private val submitPayPageIndex = 8 - 1 // 8단계. 보험청구신청 결과 페이지

    private var indicator: StepIndicator? = null // 단계 표기 컨트롤
    private var pager: CustomViewPager? = null // ViewPager
    private var btnBack: ImageButton? = null // '뒤로' 버튼
    private var textBtnCancel: TextView? = null // 취소 버튼(TextView)
    private var arrFragment: ArrayList<IUII10M00_FD?>? = null
    private var aData: Data_IUII10M00? = null
    private var flagReqSuccess= false // 보험금청구 성공 유무
    private var mAuthDvsn: AuthDvsn? = null // 청구구분(SharedPreferences)

    // 최근청구계좌 관련
    private var sLastFnisCode = "" // 최근청구계좌 은행코드, 은행명, 계좌번호, 예금주
    private var sLastFnisNm = ""
    private var sLastAcno = ""
    private var sLastDpowNm = ""
    private var accList = arrayOfNulls<String>(0) // 고객 우체국 계좌 리스트

    override fun setInit() {
        LogPrinter.CF_debug("!----------------------------------------------------------")
        LogPrinter.CF_debug("!-- IUII10M00_P.setInit()")
        LogPrinter.CF_debug("!----------------------------------------------------------")

        setContentView(R.layout.iuii10m00_p)

        handler = WeakReferenceHandler(this)
        aData = Data_IUII10M00()
        flagReqSuccess = false
        mAuthDvsn = SharedPreferencesFunc.getLoginAuthDvsn(applicationContext) // 로그인 인증유형
        aData!!.CF_setS_acdp_csno(SharedPreferencesFunc.getLoginCsno(applicationContext)) // 로그인 고객번호 (로그인==피보험자)
        aData!!.CF_setLoginUserName(SharedPreferencesFunc.getLoginName(applicationContext)) // 로그인 사용자명 (로그인==피보험자)
        LogPrinter.CF_debug("!---- mAuthDvsn     - 로그인유형               : $mAuthDvsn")
        LogPrinter.CF_debug("!---- acdp_csno     - 고객번호(로그인/피보험자) : " + aData!!.CF_getS_acdp_csno())
        LogPrinter.CF_debug("!---- loginUserName - 고객명(로그인/피보험자)   : " + aData!!.CF_getLoginUserName())
    }

    override fun setUIControl() {
        LogPrinter.CF_debug("!----------------------------------------------------------")
        LogPrinter.CF_debug("!-- IUII10M00_P.setUIControl()")
        LogPrinter.CF_debug("!----------------------------------------------------------")

        // -- 타이틀바 세팅
        setTitleBarUI()
        pager = findViewById(R.id.activity_req_viewPager)
        pager?.CF_setPagingEnabled(false)
        pager?.importantForAccessibility = View.IMPORTANT_FOR_ACCESSIBILITY_NO
        pager?.addOnPageChangeListener(object : OnPageChangeListener {
            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {}
            override fun onPageSelected(position: Int) {
                // -- 페이지의 다음 버튼 활성화
                arrFragment!![position]!!.CF_setBtnNextEnabled()
                arrFragment!![position]!!.CF_scrollTop()

                // -- 접근성 대체 Text 변경 : 청구 완료 화면에서는 닫기, 나머지 화면에서는 이전 화면 이동
                if (CommonFunction.CF_checkAccessibilityTurnOn(this@IUII10M00_P)) {
                    CF_requestFocusIndicator()

                    // --<2> 1~8단계 화면 좌측버튼 Text설정 (이전화면)
                    if (position < submitPayPageIndex) {
                        btnBack!!.contentDescription = resources.getString(R.string.desc_back)
                    } else {
                        btnBack!!.contentDescription = resources.getString(R.string.desc_close)
                    }
                }
            }

            override fun onPageScrollStateChanged(state: Int) {}
        })
        indicator = findViewById(R.id.activity_req_indicator)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        LogPrinter.CF_debug("!----------------------------------------------------------")
        LogPrinter.CF_debug("!-- IUII10M00_P.onCreate()")
        LogPrinter.CF_debug("!----------------------------------------------------------")

        // -- Fragment 생성 및 복구
        setFragments(savedInstanceState)

        // -- pager 셋팅
        val adapter = ReqBenefitPageAdapter(supportFragmentManager, arrFragment)
        pager?.adapter = adapter
        setUIStateOfPage(0)
    }

    override fun onResume() {
        super.onResume()
        LogPrinter.CF_debug("!----------------------------------------------------------")
        LogPrinter.CF_debug("!-- IUII10M00_P.onResume()")
        LogPrinter.CF_debug("!----------------------------------------------------------")
    }

    override fun onSaveInstanceState(p_bundle: Bundle) {
        super.onSaveInstanceState(p_bundle)
        LogPrinter.CF_debug("!----------------------------------------------------------")
        LogPrinter.CF_debug("!-- IUII10M00_P.onSaveInstanceState()")
        LogPrinter.CF_debug("!----------------------------------------------------------")

        // -- Fragment 번들 저장
        for (i in arrFragment!!.indices) {
            val fragment: Fragment? = arrFragment!![i]
            if (arrFragment!![i] != null && fragment!!.isAdded) {
                supportFragmentManager.putFragment(p_bundle, arrFragment!![i]!!.javaClass.name, fragment)
            }
        }
        p_bundle.putInt("currentFragmentIndex", pager!!.currentItem)
        p_bundle.putParcelable("data", aData)
        p_bundle.putBoolean("flagShowCancleBtn", textBtnCancel!!.visibility == View.VISIBLE)
        p_bundle.putBoolean("flagReqSuccess", flagReqSuccess)
    }

    override fun onRestoreInstanceState(p_bundle: Bundle) {
        super.onRestoreInstanceState(p_bundle)
        LogPrinter.CF_debug("!----------------------------------------------------------")
        LogPrinter.CF_debug("!-- IUII10M00_P.onRestoreInstanceState()")
        LogPrinter.CF_debug("!----------------------------------------------------------")

        if (p_bundle.containsKey("currentFragmentIndex")) {
            val currentFragmentIndex = p_bundle.getInt("currentFragmentIndex")
            setUIStateOfPage(currentFragmentIndex)
        }
        if (p_bundle.containsKey("data")) {
            aData = p_bundle.getParcelable("data")
        }
        if (p_bundle.containsKey("flagShowCancleBtn")) {
            val flagVisible = p_bundle.getBoolean("flagShowCancleBtn")
            if (flagVisible) {
                textBtnCancel!!.visibility = View.VISIBLE
            } else {
                textBtnCancel!!.visibility = View.GONE
            }
        }

        // -- 지급청구 요청 flag
        if (p_bundle.containsKey("flagReqSuccess")) {
            flagReqSuccess = p_bundle.getBoolean("flagReqSuccess")
        }
    }

    /**
     * 권한요청
     * @param requestCode   int
     * @param permissions   String[]
     * @param grantResults  int[]
     */
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        LogPrinter.CF_debug("!----------------------------------------------------------")
        LogPrinter.CF_debug("!-- IUII10M00_P.onRequestPermissionsResult()")
        LogPrinter.CF_debug("!----------------------------------------------------------")

        // --<1> IUII10M06_F 에서 요청한 저장소 사용권한
        if (requestCode == IUII10M06_F.REQUEST_STORAGE_PERMISSION) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                val fragment = arrFragment!![5] as IUII10M06_F?
                if (fragment != null && fragment.isAdded) {
                    fragment.CF_startGalleryActivity()
                }
            }
        } else if (requestCode == IUII10M06_F.REQUEST_CAMERA_PERMISSION) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                val fragment = arrFragment!![5] as IUII10M06_F?
                if (fragment != null && fragment.isAdded) {
                    fragment.CF_startCameraApp()
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        LogPrinter.CF_debug("!----------------------------------------------------------")
        LogPrinter.CF_debug("!-- IUII10M00_P.onNewIntent()")
        LogPrinter.CF_debug("!----------------------------------------------------------")

        val customDialog = CustomDialog(this)
        customDialog.show()
        customDialog.CF_setBackKeyMode(CustomDialog.BackMode.CANCELED)
        customDialog.CF_setTextContent(resources.getString(R.string.dlg_already_req_pay))
        customDialog.CF_setDoubleButtonText(resources.getString(R.string.btn_cancel), resources.getString(R.string.btn_ok))
        customDialog.setOnDismissListener { dialog ->
            if (!(dialog as CustomDialog).CF_getCanceled()) {
                // -- 서류첨부 솔루션 헬퍼 : 임시파일(이미지) 삭제
                val psMobileModule = PSMobileModule(applicationContext)
                psMobileModule.clearFiles()
                finish()
                startActivity(getIntent())
            }
        }
    }

    override fun onBackPressed() {
        LogPrinter.CF_debug("!----------------------------------------------------------")
        LogPrinter.CF_debug("!-- IUII10M00_P.onBackPressed()")
        LogPrinter.CF_debug("!----------------------------------------------------------")

        val curPageIdx = pager!!.currentItem
        // --<> 1단계 or (8단계 이면서 오류 화면 상태)인 경우
        if (curPageIdx == 0 || curPageIdx == submitPayPageIndex && !flagReqSuccess) {
            showDlgOfCancel()
        }
        else if (curPageIdx < submitPayPageIndex) {
            CF_showPrevPage()
        }
        else {
            // -- 서류첨부 솔루션 헬퍼 : 임시파일(이미지) 삭제
            val psMobileModule = PSMobileModule(applicationContext)
            psMobileModule.clearFiles()
            finish()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        LogPrinter.CF_debug("!----------------------------------------------------------")
        LogPrinter.CF_debug("!-- IUII10M00_P.onDestroy()")
        LogPrinter.CF_debug("!----------------------------------------------------------")
    }

    // #############################################################################################
    //  private 함수
    // #############################################################################################
    /**
     * 타이틀바 레이아웃 세팅
     */
    private fun setTitleBarUI() {
        LogPrinter.CF_debug("!----------------------------------------------------------")
        LogPrinter.CF_debug("!-- IUII10M00_P.setTitleBarUI()")
        LogPrinter.CF_debug("!----------------------------------------------------------")

        // -- 타이틀 세팅
        val txtTitle = findViewById<TextView>(R.id.title_bar_textTitle)
        txtTitle.text = resources.getString(R.string.title_request)

        // -- Left 버튼 세팅
        btnBack = findViewById(R.id.title_bar_imgBtnLeft)
        btnBack?.visibility = View.VISIBLE
        btnBack?.setOnClickListener(View.OnClickListener {
            val curPageIdx = pager!!.currentItem
            if (curPageIdx == 0 || curPageIdx == submitPayPageIndex && !flagReqSuccess) {
                showDlgOfCancel()
            } else if (curPageIdx < submitPayPageIndex) {
                CF_showPrevPage()
            } else {
                // -- 서류첨부 솔루션 헬퍼 : 임시파일(이미지) 삭제
                val psMobileModule = PSMobileModule(applicationContext)
                psMobileModule.clearFiles()
                finish()
            }
        })

        // -- Right 버튼 세팅
        val layoutParams = RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.MATCH_PARENT)
        layoutParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT)
        textBtnCancel = TextView(this)
        textBtnCancel!!.layoutParams = layoutParams
        textBtnCancel!!.text = resources.getString(R.string.btn_cancel)
        textBtnCancel!!.contentDescription = resources.getString(R.string.btn_cancel) + resources.getString(R.string.label_btn)
        textBtnCancel!!.setPadding(CommonFunction.CF_convertDipToPixel(applicationContext, 15f), 0, CommonFunction.CF_convertDipToPixel(applicationContext, 15f), 0)
        textBtnCancel!!.setTextColor(Color.WHITE)
        textBtnCancel!!.setTextSize(TypedValue.COMPLEX_UNIT_PX, resources.getDimensionPixelSize(R.dimen.button_text_size).toFloat())
        textBtnCancel!!.isClickable = true
        textBtnCancel!!.gravity = Gravity.CENTER_VERTICAL
        textBtnCancel!!.setBackgroundResource(R.drawable.img_button_bg_selector)
        textBtnCancel!!.setOnClickListener { showDlgOfCancel() }
        val titleLayout = findViewById<RelativeLayout>(R.id.title_bar_root)
        titleLayout.addView(textBtnCancel)
    }

    /**
     * Fragment 데이터 세팅<br></br>
     * Bundle 데이터가 있을 경우 Bundle 데이터로 세팅한다.<br></br>
     * @param p_bundle Bundle
     */
    private fun setFragments(p_bundle: Bundle?) {
        LogPrinter.CF_debug("!----------------------------------------------------------")
        LogPrinter.CF_debug("!-- IUII10M00_P.setFragments()")
        LogPrinter.CF_debug("!----------------------------------------------------------")
        arrFragment = ArrayList()
        var fragment1: IUII10M01_F? = null
        var fragment2: IUII10M02_F? = null
        var fragment3: IUII10M03_F? = null
        var fragment4: IUII10M04_F? = null
        var fragment5: IUII10M05_F? = null
        var fragment6: IUII10M06_F? = null
        var fragment7: IUII10M07_F? = null
        //var fragment8: IUII10M08_F?  = null
        var fragment9: IUII10M09_F? = null

        if (p_bundle != null) {
            fragment1 = supportFragmentManager.getFragment(p_bundle, IUII10M01_F::class.java.name) as IUII10M01_F? // 1단계. 접수 시 확인사항
            fragment2 = supportFragmentManager.getFragment(p_bundle, IUII10M02_F::class.java.name) as IUII10M02_F? // 2단계. 개인정보처리동의
            fragment3 = supportFragmentManager.getFragment(p_bundle, IUII10M03_F::class.java.name) as IUII10M03_F? // 3단계. 보험금청구서 작성(개인정보)
            fragment4 = supportFragmentManager.getFragment(p_bundle, IUII10M04_F::class.java.name) as IUII10M04_F? // 4단계. 보험금청구서 작성(청구내용1)
            fragment5 = supportFragmentManager.getFragment(p_bundle, IUII10M05_F::class.java.name) as IUII10M05_F? // 5단계. 보험금청구서 작성(청구내용2)
            fragment6 = supportFragmentManager.getFragment(p_bundle, IUII10M06_F::class.java.name) as IUII10M06_F? // 6단계. 구비서류첨부
            fragment7 = supportFragmentManager.getFragment(p_bundle, IUII10M07_F::class.java.name) as IUII10M07_F? // 7단계. 계좌정보확인 및 추천국/추천인 선택 > 전자서명
            //fragment8 = supportFragmentManager.getFragment(p_bundle, IUII10M08_F::class.java.name) as IUII10M08_F?  // 8단계. 접수신청정보확인
            fragment9 = supportFragmentManager.getFragment(p_bundle, IUII10M09_F::class.java.name) as IUII10M09_F? // 9단계. 청구완료
        }
        if (fragment1 == null) {
            fragment1 = Fragment.instantiate(this, IUII10M01_F::class.java.name) as IUII10M01_F
        }
        if (fragment2 == null) {
            fragment2 = Fragment.instantiate(this, IUII10M02_F::class.java.name) as IUII10M02_F
        }
        if (fragment3 == null) {
            fragment3 = Fragment.instantiate(this, IUII10M03_F::class.java.name) as IUII10M03_F
        }
        if (fragment4 == null) {
            fragment4 = Fragment.instantiate(this, IUII10M04_F::class.java.name) as IUII10M04_F
        }
        if (fragment5 == null) {
            fragment5 = Fragment.instantiate(this, IUII10M05_F::class.java.name) as IUII10M05_F
        }
        if (fragment6 == null) {
            fragment6 = Fragment.instantiate(this, IUII10M06_F::class.java.name) as IUII10M06_F
        }
        if (fragment7 == null) {
            fragment7 = Fragment.instantiate(this, IUII10M07_F::class.java.name) as IUII10M07_F
        }
        if (fragment9 == null) {
            fragment9 = Fragment.instantiate(this, IUII10M09_F::class.java.name) as IUII10M09_F
        }
        arrFragment!!.add(fragment1)
        arrFragment!!.add(fragment2)
        arrFragment!!.add(fragment3)
        arrFragment!!.add(fragment4)
        arrFragment!!.add(fragment5)
        arrFragment!!.add(fragment6)
        arrFragment!!.add(fragment7)
        //       arrFragment.add(tmp_fragment_8);
        arrFragment!!.add(fragment9)
    }

    /**
     * 현재 페이지에 맞는 UI 세팅 함수
     */
    private fun setUIStateOfPage(p_pageIndex: Int) {
        LogPrinter.CF_debug("!----------------------------------------------------------")
        LogPrinter.CF_debug("!-- IUII10M00_P.setUIStateOfPage()")
        LogPrinter.CF_debug("!----------------------------------------------------------")
        indicator!!.CF_setSelectedIndex(p_pageIndex)
        pager!!.currentItem = p_pageIndex

        // -- 취소버튼 상태 세팅
        setVisibilityOfCancelBtn()

        // -- 뒤로버튼 이미지 세팅
        setImageResourOfBackBtn()
    }

    /**
     * 해당 page show 함수<br></br>
     * 해당 page를 보이고 indicator 값을 변경한다.<br></br>
     * @param p_index int
     * 2019-08-02 8단계 삭제
     */
    private fun showPage(p_index: Int) {
        LogPrinter.CF_debug("!----------------------------------------------------------")
        LogPrinter.CF_debug("!-- IUII10M00_P.showPage()")
        LogPrinter.CF_debug("!----------------------------------------------------------")

        // StepIndicator 보이기
        CF_setVisibleStepIndicator(true)
        val fragment2 = arrFragment!![agreePageIndex] as IUII10M02_F?

        // --<1> (2단계화면) 개인정보처리동의 페이지 일때 : 접근성 활성화
        if (p_index == agreePageIndex) {
            fragment2!!.CF_setWebViewAccessbileImportant(true)
        }
        else {
            // --<2> 2단계 개인정보 동의 처리(페이지가 없을경우 아니오 처리 후 페이지 삽입)
            if (fragment2 != null && fragment2.isAdded) {
                fragment2.CF_setWebViewAccessbileImportant(false)
            }

            // --<2> 3단계. 지급청구 요청(개인정보 확인 및 입력)
            if (p_index == userInfoPageIndex) {
                val fragment = arrFragment!![p_index] as IUII10M03_F?
                fragment!!.httpReq_serInfo()
            } else if (p_index == submitPayPageIndex && !flagReqSuccess) {
                CF_requestSubmitPay()
            }
        }

        // -- 페이지 UI 세팅
        setUIStateOfPage(p_index)
        if (currentFocus != null) {
            CommonFunction.CF_closeVirtualKeyboard(this, currentFocus!!.windowToken)
        }
    }

    /**
     * 취소 버튼 show / hide 세팅
     */
    private fun setVisibilityOfCancelBtn() {
        LogPrinter.CF_debug("!----------------------------------------------------------")
        LogPrinter.CF_debug("!-- IUII10M00_P.setVisibilityOfCancelBtn()")
        LogPrinter.CF_debug("!----------------------------------------------------------")

        val curPageIndex = pager!!.currentItem
        if (curPageIndex == 0 || curPageIndex == submitPayPageIndex) {
            textBtnCancel!!.visibility = View.GONE
        } else {
            textBtnCancel!!.visibility = View.VISIBLE
        }
    }

    /**
     * 뒤로 가기 버튼의 이미지 세팅
     */
    private fun setImageResourOfBackBtn() {
        LogPrinter.CF_debug("!----------------------------------------------------------")
        LogPrinter.CF_debug("!-- IUII10M00_P.setImageResourOfBackBtn()")
        LogPrinter.CF_debug("!----------------------------------------------------------")

        if (pager?.currentItem == submitPayPageIndex) {
            btnBack?.setImageResource(R.drawable.ic_close_3)
        } else {
            btnBack?.setImageResource(R.drawable.ic_prev)
        }
    }

    // #############################################################################################
    //  public 함수
    // #############################################################################################
    /**
     * 접근성 포커싱을 위해 단계 지시자에 포커스 옮기기
     */
    fun CF_requestFocusIndicator() {
        LogPrinter.CF_debug("!----------------------------------------------------------")
        LogPrinter.CF_debug("!-- IUII10M00_P.CF_requestFocusIndicator()")
        LogPrinter.CF_debug("!----------------------------------------------------------")
        clearAllFocus()
        Handler().postDelayed({
            indicator!!.isFocusableInTouchMode = true
            indicator!!.requestFocus()
            indicator!!.isFocusableInTouchMode = false
        }, 500)
    }

    /**
     * 이전페이지 이동
     */
    fun CF_showPrevPage() {
        showPage(pager!!.currentItem - 1)
    }

    /**
     * 다음페이지 이동
     */
    fun CF_showNextPage() {
        showPage(pager!!.currentItem + 1)
    }

    /**
     * Step Indicator Show/Hide
     * @param p_flagShow boolean
     */
    fun CF_setVisibleStepIndicator(p_flagShow: Boolean) {
        LogPrinter.CF_debug("!----------------------------------------------------------")
        LogPrinter.CF_debug("!-- IUII10M00_P.CF_setVisibleStepIndicator()")
        LogPrinter.CF_debug("!----------------------------------------------------------")

        val tmp_appBar = findViewById<AppBarLayout>(R.id.appBar)
        tmp_appBar.setExpanded(p_flagShow, false)
    }

    // #############################################################################################
    //  Getter & Setter
    // #############################################################################################
    /**
     * 데이터 반환
     * @return data
     */
    fun CF_getData(): Data_IUII10M00? {
        return aData
    }

    /**
     * 청구구분 (SharedPreferences) Get
     * @return mAuthDvsn
     */
    fun CF_getAuthDvsn(): AuthDvsn? {
        return mAuthDvsn
    }

    /**
     * 최근 청구계좌 은행코드 Get
     * @return sFnisCode    String
     */
    fun CF_getsLastFnisCode(): String {
        return sLastFnisCode
    }

    /**
     * 최근 청구계좌 은행코드 Set
     */
    fun CF_setsLastFnisCode(sLastFnisCode: String) {
        this.sLastFnisCode = sLastFnisCode
    }

    /**
     * 최근 청구계좌 은행명 Get
     * @return sFnisNm  String
     */
    fun CF_getsLastFnisNm(): String {
        return sLastFnisNm
    }

    /**
     * 최근 청구계좌 은행명 Set
     */
    fun CF_setsLastFnisNm(sLastFnisNm: String) {
        this.sLastFnisNm = sLastFnisNm
    }

    /**
     * 최근 청구계좌 계좌번호 Get
     * @return sAcno    String
     */
    fun CF_getsLastAcno(): String {
        return sLastAcno
    }

    /**
     * 최근 청구계좌 계좌번호 Set
     */
    fun CF_setsLastAcno(sLastAcno: String) {
        this.sLastAcno = sLastAcno
    }

    /**
     * 최근 청구계좌 예금주 Get
     * @return sDpowNm  String
     */
    fun CF_getsLastDpowNm(): String {
        return sLastDpowNm
    }

    /**
     * 최근 청구계좌 예금주 Set
     */
    fun CF_setsLastDpowNm(sDpowNm: String) {
        sLastDpowNm = sDpowNm
    }

    /**
     * 고객 보유 우체국 계좌 리스트 Get
     */
    fun CF_getAccList(): Array<String?> {
        return accList
    }

    /**
     * 고객 보유 우체국 계좌 리스트 Set
     */
    fun CF_setAccList(accList: Array<String?>) {
        this.accList = accList.copyOf(accList.size)
    }

    //    /**
    //     * 고객 부담보내역 존재 여부 get
    //     */
    //    public boolean CF_getSmbrYn() { return bSmbrYn; }
    //    /**
    //     * 고객 부담보내역 존재 여부 set
    //     */
    //    public void CF_setSmbrYn(boolean bSmbrYn){ this.bSmbrYn = bSmbrYn; }
    // #############################################################################################
    //  Dialog 함수
    // #############################################################################################
    /**
     * 보험금청구신청 취소 다이얼로그 show 함수
     */
    private fun showDlgOfCancel() {
        LogPrinter.CF_debug("!----------------------------------------------------------")
        LogPrinter.CF_debug("!-- IUII10M00_P.showDlgOfCancel() -- 닫기버튼 클릭됨")
        LogPrinter.CF_debug("!----------------------------------------------------------")
        showCustomDialog(resources.getString(R.string.dlg_cancel_reqbenefit),
            resources.getString(R.string.btn_no),
            resources.getString(R.string.btn_yes),
            textBtnCancel!!
        ) { dialog ->
            if (!(dialog as CustomDialog).CF_getCanceled()) {
                // -- 서류첨부 솔루션 헬퍼 : 임시파일(이미지) 삭제
                val psMobileModule = PSMobileModule(applicationContext)
                psMobileModule.clearFiles()
                finish()
            } else if (CommonFunction.CF_checkAccessibilityTurnOn(applicationContext)) {
                clearAllFocus()
                textBtnCancel!!.requestFocus()
            }
            textBtnCancel!!.isFocusableInTouchMode = false
        }
    }

    /**
     * 에러 처리 함수<br></br>
     * 에러 메시지를 다이얼로그로 띄우고 Activity 종료
     * @param p_strError String
     */
    fun CF_OnError(p_strError: String?) {
        LogPrinter.CF_debug("!----------------------------------------------------------")
        LogPrinter.CF_debug("!-- IUII10M00_P.CF_OnError()")
        LogPrinter.CF_debug("!----------------------------------------------------------")
        val customDialog = CustomDialog(this)
        customDialog.show()
        customDialog.CF_setTextContent(p_strError)
        customDialog.CF_setSingleButtonText(resources.getString(R.string.btn_ok))
        customDialog.CF_setBackKeyMode(CustomDialog.BackMode.NOTUSE)
        customDialog.setOnDismissListener { // -- 서류첨부 솔루션 헬퍼 : 임시파일(이미지) 삭제
            val psMobileModule = PSMobileModule(applicationContext)
            psMobileModule.clearFiles()
            finish()
        }
    }

    // #############################################################################################
    //  Http 관련
    // #############################################################################################
    override fun handleMessage(p_message: Message) {
        LogPrinter.CF_debug("!----------------------------------------------------------")
        LogPrinter.CF_debug("!-- IUII10M00_P.handleMessage()")
        LogPrinter.CF_debug("!----------------------------------------------------------")
        if (!isDestroyed) {
            CF_dismissProgressDialog()
            when (p_message.what) {
                HANDLERJOB_SUBMIT -> try {
                    setResultOfSubmitPay(JSONObject(p_message.obj as String))
                } catch (e: JSONException) {
                    LogPrinter.CF_debug(resources.getString(R.string.log_json_exception))
                }
                HANDLERJOB_ERROR_SUBMIT -> {
                    CommonFunction.CF_showCustomAlertDilaog(this, p_message.obj as String, resources.getString(R.string.btn_ok))
                    val fragment = arrFragment!![submitPayPageIndex] as IUII10M09_F?
                    if (fragment != null && fragment.isAdded) {
                        fragment.CF_showVisibliteyPage(1)
                    }
                }
                else -> {
                }
            }
        }
    }

    /**
     * 보험금청구 접수 요청
     */
    fun CF_requestSubmitPay() {
        LogPrinter.CF_debug("!----------------------------------------------------------")
        LogPrinter.CF_debug("!-- IUII10M00_P.CF_requestSubmitPay()")
        LogPrinter.CF_debug("!----------------------------------------------------------")
        CF_showProgressDialog()
        val sUrl: String
        val hashMap = aData!!.CF_getDataMap()
        hashMap["s_pay_clam_nm"] = aData!!.CF_getLoginUserName() // 고객 이름

        // --<> (휴대폰인증)
        if (mAuthDvsn == AuthDvsn.MOBILE) {
            sUrl = EnvConfig.host_url + EnvConfig.URL_CLAIM_MOBILE_REQ
        } else {
            sUrl = EnvConfig.host_url + EnvConfig.URL_CLAIM_REQ
            hashMap["tempKey"] = SharedPreferencesFunc.getWebTempKey(applicationContext)
        }
        LogPrinter.CF_debug("!-- 최종 청구 전송용 DATA")
        aData!!.logPrint()
        HttpConnections.sendMultiData(
            sUrl,
            aData!!.CF_getUploadFileKeyList(),
            aData!!.CF_getUploadFileList(),
            hashMap,
            HANDLERJOB_SUBMIT,
            HANDLERJOB_ERROR_SUBMIT,
            handler
        )
    }

    /**
     * 보험금 청구 접수 요청 결과 처리 함수
     * @param p_jsonObject JSONObject
     */
    private fun setResultOfSubmitPay(p_jsonObject: JSONObject) {
        LogPrinter.CF_debug("!----------------------------------------------------------")
        LogPrinter.CF_debug("!-- IUII10M00_P.setResultOfSubmitPay()")
        LogPrinter.CF_debug("!----------------------------------------------------------")
        val jsonKey_errorCode = "errCode"
        val jsonKey_data = "data"
        val jsonKey_s_requ_recp_id = "s_requ_recp_id" // 청구접수번호
        val jsonKey_s_recp_cent_nm = "s_recp_cent_nm" // 전수센터명
        val jsonKey_s_recp_cent_tlno = "s_recp_cent_tlno" // 접수센터 전화번호
        val jsonKey_s_recp_cent_chps_nm = "s_recp_cent_chps_nm" // 전수센터담당자명
        val jsonKey_s_requ_proc_schd_date = "s_requ_proc_schd_date" // 청구처리예정일자
        val tmp_errorCode: String
        var s_requ_recp_id = ""
        var s_recp_cent_nm = ""
        var s_recp_cent_tlno = ""
        var s_recp_cent_chps_nm = ""
        var s_requ_proc_schd_date = ""
        try {
            // --<1>
            if (p_jsonObject.has(jsonKey_errorCode)) {
                LogPrinter.CF_debug("!--<1> (에러코드키 있음) : 전문수신 성공")
                tmp_errorCode = p_jsonObject.getString(jsonKey_errorCode)
                when (tmp_errorCode) {
                    "" -> {
                        LogPrinter.CF_debug("!--<2> (청구 성공 수신)")
                        if (p_jsonObject.has(jsonKey_data)) {
                            LogPrinter.CF_debug("!--<3> (데이터키 수신 성공)")
                            val tmp_jsonData = p_jsonObject.getJSONObject(jsonKey_data)

                            // --<4> 데이터 내용 파싱
                            if (tmp_jsonData.has(jsonKey_s_requ_recp_id)) {
                                s_requ_recp_id = tmp_jsonData.getString(jsonKey_s_requ_recp_id)
                            }
                            if (tmp_jsonData.has(jsonKey_s_recp_cent_nm)) {
                                s_recp_cent_nm = tmp_jsonData.getString(jsonKey_s_recp_cent_nm)
                            }
                            if (tmp_jsonData.has(jsonKey_s_recp_cent_tlno)) {
                                s_recp_cent_tlno = tmp_jsonData.getString(jsonKey_s_recp_cent_tlno)
                            }
                            if (tmp_jsonData.has(jsonKey_s_recp_cent_chps_nm)) {
                                s_recp_cent_chps_nm = tmp_jsonData.getString(jsonKey_s_recp_cent_chps_nm)
                            }
                            if (tmp_jsonData.has(jsonKey_s_requ_proc_schd_date)) {
                                s_requ_proc_schd_date = tmp_jsonData.getString(jsonKey_s_requ_proc_schd_date)
                            }
                            flagReqSuccess = true
                            CF_requestFocusIndicator()
                        } else {
                            LogPrinter.CF_debug("!--<3> 데이터키 수신 실패")
                            CommonFunction.CF_showCustomAlertDilaog(this, resources.getString(R.string.dlg_error_server_2), resources.getString(R.string.btn_ok))
                        }
                    }
                    "ERRIUII40M00001" -> {
                        LogPrinter.CF_debug("!--<2> 에러 : 청구실패(ERRIUII40M00001)")
                        CommonFunction.CF_showCustomAlertDilaog(this, resources.getString(R.string.dlg_error_erriuii40m00001), resources.getString(R.string.btn_ok))
                    }
                    "ERRIUII40M00002" -> {
                        LogPrinter.CF_debug("!--<2> 에러 : 청구실패(ERRIUII40M00002)")
                        CommonFunction.CF_showCustomAlertDilaog(this, resources.getString(R.string.dlg_error_erriuii40m00002), resources.getString(R.string.btn_ok))
                    }
                    else -> {
                        LogPrinter.CF_debug("!--<2> 에러 : 청구실패(기타)")
                        CommonFunction.CF_showCustomAlertDilaog(this, resources.getString(R.string.dlg_error_server_2), resources.getString(R.string.btn_ok))
                    }
                }
            } else {
                LogPrinter.CF_debug("!--<1> 에러코드 없음: 전문수신 실패")
                CommonFunction.CF_showCustomAlertDilaog(this, resources.getString(R.string.dlg_error_server_1), resources.getString(R.string.btn_ok))
            }
        } catch (e: NullPointerException) {
            e.message
        } catch (e: Exception) {
            LogPrinter.CF_debug("!--## 전문파싱 중에 에러 발생::::" + e.message)
            CommonFunction.CF_showCustomAlertDilaog(this, resources.getString(R.string.dlg_error_server_1), resources.getString(R.string.btn_ok))
        }

        // -----------------------------------------------------------------------------------------
        // --최종 결과 처리
        // -----------------------------------------------------------------------------------------
        // -- 서류첨부 솔루션 헬퍼 : 최종단계에서 임시파일 초기화
        val psMobileModule = PSMobileModule(applicationContext)
        psMobileModule.clearFiles() // 임시파일(이미지) 삭제

        //  지급청구 성공 시(결과 화면), 실패 시(오류 화면) show
        val fragment = arrFragment!![submitPayPageIndex] as IUII10M09_F?
        if (fragment != null && fragment.isAdded) {
            // --<> (청구 성공)
            if (flagReqSuccess) {
                fragment.CF_setResultOk(s_requ_recp_id, s_recp_cent_nm, s_recp_cent_tlno, s_recp_cent_chps_nm, s_requ_proc_schd_date)
                fragment.CF_showVisibliteyPage(0)
            } else {
                fragment.CF_showVisibliteyPage(1)
            }
        }
    }
}