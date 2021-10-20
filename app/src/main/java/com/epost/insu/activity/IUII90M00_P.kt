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
import com.epost.insu.adapter.ReqBenefitChildPageAdapter
import com.epost.insu.common.*
import com.epost.insu.control.StepIndicator
import com.epost.insu.data.Data_IUII90M00
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
 * 보험금청구 > 자녀보험금청구
 * @since     :
 * @version   : 1.8
 * @author    : YJH
 * @see
 * <pre>
 * 보험금청구접수신청 각 화면(Step 1 ~ Step 8)을 관리하는 Activity.
 * ======================================================================
 * 0.0.0    YJH_20181109    최초 등록
 * 0.0.0    YJH_20190924    자녀보험금청구 8단계 삭제
 * 0.0.0    NJM_20191002    (자녀)지급청구 마지막 단계 하얀화면 문제 해결 (3단계에서 고객휴대폰 번호 소실되는 문제 수정)
 * 0.0.0    NJM_20191007    최근 청구 계좌 & 우체국 계좌 조회 후 화면에 초기세팅
 * 0.0.0    NJM_20200122    공통 인증유형/청구유형 추가에 따른 로직수정
 * 0.0.0    NJM_20200123    자녀보험금청구에 휴대폰인증 추가
 * 0.0.0    NJM_20201028    [모바일 사진촬영 패키지 도입]
 * 0.0.0    NJM_20210108    카카오페이인증 추가
 * 1.5.2    NJM_20210318    [액티비티 재실행시 사진첨부오류(~준비되지 않은) 수정] psMobileModule 호출 위치 변경
 * 1.5.2    NJM_20210322    [subUrl 공통파일로 변경]
 * 1.5.3    NJM_20210422    [자녀청구 예금주소실 수정]
 * 1.5.4    NJM_20210504    [간편인증 전자서명 추가] 간편인증 전자서명 로직 추가
 * 1.6.2    NJM_20210802    [2021년 대우 취약점] 2차본 : private 배열에 public 데이터 할당
 * 1.6.2    NJM_20210802    [2021년 대우 취약점] 2차본 : 부적절한 예외 처리
 * =======================================================================
 * copyright : (주)프로니스정보기술 | http://www.phronis.co.kr
 * </pre>
 */
class IUII90M00_P : Activity_Default(), ObjectHandlerMessage {
    private val HANDLERJOB_SUBMIT = 0
    private val HANDLERJOB_ERROR_SUBMIT = 1

    // 페이지 배열 Index (배열값 -1)
    private val agreePageIndex = 2 - 1 // 2단계. 개인정보처리동의 페이지
    private val userInfoPageIndex = 3 - 1 // 3단계. 개인정보 입력 페이지
    private val submitPayPageIndex = 8 - 1 // 8단계. 보험청구신청 결과 페이지

    private var indicator: StepIndicator? = null // 단계 표기 컨트롤
    private lateinit var pager: CustomViewPager // ViewPager
    private lateinit var btnBack: ImageButton  // '뒤로' 버튼

    private var textBtnCancel: TextView? = null // 취소 버튼(TextView)

    private var arrFragment: ArrayList<IUII90M00_FD?>? = null
    private var data: Data_IUII90M00? = null

    private var flagReqSuccess = false // 보험금청구 성공 유무

    private var mAuthDvsn: AuthDvsn? = null // 청구구분(SharedPreferences)

    // 최근청구계좌 관련
    private var sLastFnisCode = "" // 최근청구계좌 은행코드, 은행명, 계좌번호, 예금주
    private var sLastFnisNm = ""
    private var sLastAcno = ""
    private var sLastDpowNm = ""
    private var accList = arrayOfNulls<String>(0) // 고객 우체국 계좌 리스트

    //
    //    @Override
    //    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    //        super.onActivityResult(requestCode, resultCode, data);
    //        LogPrinter.CF_debug("!-----------------------------------------------------------");
    //        LogPrinter.CF_debug("!-- IUII90M00_P.onActivityResult()");
    //        LogPrinter.CF_debug("!-----------------------------------------------------------");
    //
    //        if(resultCode == RESULT_OK) {
    //            switch (requestCode) {
    //                // -----------------------------------------------------------------------------------------
    //                // -- 카카오인증 성공
    //                // -----------------------------------------------------------------------------------------
    //                case EnvConfig.REQUESTCODE_ACTIVITY_IUCOK0M00 :
    //                    LogPrinter.CF_debug("!--## 카카오인증 성공");
    //                    CF_showNextPage();
    //                    break;
    //
    //                // -----------------------------------------------------------------------------------------
    //                // -- 기타
    //                // -----------------------------------------------------------------------------------------
    //                default:
    //                    LogPrinter.CF_debug("!---- p_requestCode값 없음 :" + requestCode);
    //            }
    //        } else {
    //            LogPrinter.CF_debug("!---- p_resultCode실패 :" + requestCode);
    //        }
    //    }
    override fun setInit() {
        LogPrinter.CF_debug("!-----------------------------------------------------------")
        LogPrinter.CF_debug("!-- IUII90M00_P.setInit()")
        LogPrinter.CF_debug("!-----------------------------------------------------------")

        setContentView(R.layout.iuii90m00_p)
        handler = WeakReferenceHandler(this)
        data = Data_IUII90M00()
        flagReqSuccess = false
        mAuthDvsn = SharedPreferencesFunc.getLoginAuthDvsn(applicationContext) // 로그인 인증유형
        data!!.CF_setS_rctr_csno(SharedPreferencesFunc.getLoginCsno(applicationContext)) // 로그인 고객번호 (청구자)

        LogPrinter.CF_debug("!---- mAuthDvsn(인증유형):::::::::::::$mAuthDvsn")
        LogPrinter.CF_debug("!---- s_rctr_csno(청구자 고객번호)::::" + data!!.CF_getS_rctr_csno())
    }

    override fun setUIControl() {
        LogPrinter.CF_debug("!-----------------------------------------------------------")
        LogPrinter.CF_debug("!-- IUII90M00_P.setUIControl()")
        LogPrinter.CF_debug("!-----------------------------------------------------------")

        // 타이틀바 세팅
        setTitleBarUI()
        pager = findViewById(R.id.activity_req_viewPager)
        pager.CF_setPagingEnabled(false)
        pager.setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_NO)
        pager.addOnPageChangeListener(object : OnPageChangeListener {
            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {}
            override fun onPageSelected(position: Int) {
                // 페이지의 다음 버튼 활성화
                arrFragment!![position]!!.CF_setBtnNextEnabled()
                arrFragment!![position]!!.CF_scrollTop()

                // -- 접근성 대체 Text 변경 : 청구 완료 화면에서는 닫기, 나머지 화면에서는 이전 화면 이동
                if (CommonFunction.CF_checkAccessibilityTurnOn(this@IUII90M00_P)) {
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
        LogPrinter.CF_debug("!-----------------------------------------------------------")
        LogPrinter.CF_debug("!-- IUII90M00_P.handleMessage()")
        LogPrinter.CF_debug("!-----------------------------------------------------------")

        // -- Fragment 생성및 복구
        setFragments(savedInstanceState)

        // -- pager 셋팅
        val adapter = ReqBenefitChildPageAdapter(supportFragmentManager, arrFragment)
        pager!!.adapter = adapter
        setUIStateOfPage(0)
    }

    override fun onResume() {
        super.onResume()
        LogPrinter.CF_debug("!-----------------------------------------------------------")
        LogPrinter.CF_debug("!-- IUII90M00_P.onResume()")
        LogPrinter.CF_debug("!-----------------------------------------------------------")
    }

    override fun onSaveInstanceState(p_bundle: Bundle) {
        super.onSaveInstanceState(p_bundle)
        LogPrinter.CF_debug("!-----------------------------------------------------------")
        LogPrinter.CF_debug("!-- IUII90M00_P.onSaveInstanceState()")
        LogPrinter.CF_debug("!-----------------------------------------------------------")

        // -- Fragment 번들 저장
        for (i in arrFragment!!.indices) {
            val tmp_fragment: Fragment? = arrFragment!![i]
            if (arrFragment!![i] != null && tmp_fragment!!.isAdded) {
                supportFragmentManager.putFragment(p_bundle, arrFragment!![i]!!.javaClass.name, tmp_fragment)
            }
        }
        p_bundle.putInt("currentFragmentIndex", pager!!.currentItem)
        p_bundle.putParcelable("data", data)
        p_bundle.putBoolean("flagShowCancleBtn", textBtnCancel!!.visibility == View.VISIBLE)
        p_bundle.putBoolean("flagReqSuccess", flagReqSuccess)
    }

    override fun onRestoreInstanceState(p_bundle: Bundle) {
        super.onRestoreInstanceState(p_bundle)
        LogPrinter.CF_debug("!-----------------------------------------------------------")
        LogPrinter.CF_debug("!-- IUII90M00_P.onRestoreInstanceState()")
        LogPrinter.CF_debug("!-----------------------------------------------------------")
        if (p_bundle.containsKey("currentFragmentIndex")) {
            val tmp_currentFragmentIndex = p_bundle.getInt("currentFragmentIndex")
            setUIStateOfPage(tmp_currentFragmentIndex)
        }
        if (p_bundle.containsKey("data")) {
            data = p_bundle.getParcelable("data")
        }
        if (p_bundle.containsKey("flagShowCancleBtn")) {
            val tmp_flagVisible = p_bundle.getBoolean("flagShowCancleBtn")
            if (tmp_flagVisible) {
                textBtnCancel!!.visibility = View.VISIBLE
            } else {
                textBtnCancel!!.visibility = View.GONE
            }
        }

        // 지급청구 요청 flag
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
        LogPrinter.CF_debug("!-----------------------------------------------------------")
        LogPrinter.CF_debug("!-- IUII90M00_P.onRequestPermissionsResult()")
        LogPrinter.CF_debug("!-----------------------------------------------------------")

        // --<1> IUII90M06_F 에서 요청한 저장소 사용권한
        if (requestCode == IUII90M06_F.REQUEST_STORAGE_PERMISSION) {
            if (grantResults.size > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                val tmp_fragment = arrFragment!![5] as IUII90M06_F?
                if (tmp_fragment != null && tmp_fragment.isAdded) {
                    tmp_fragment.CF_startGalleryActivity()
                }
            }
        } else if (requestCode == IUII90M06_F.REQUEST_CAMERA_PERMISSION) {
            if (grantResults.size > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                val tmp_fragment = arrFragment!![5] as IUII90M06_F?
                if (tmp_fragment != null && tmp_fragment.isAdded) {
                    tmp_fragment.CF_startCameraApp()
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        LogPrinter.CF_debug("!-----------------------------------------------------------")
        LogPrinter.CF_debug("!-- IUII90M00_P.onNewIntent()")
        LogPrinter.CF_debug("!-----------------------------------------------------------")
        val tmp_dlg = CustomDialog(this)
        tmp_dlg.show()
        tmp_dlg.CF_setBackKeyMode(CustomDialog.BackMode.CANCELED)
        tmp_dlg.CF_setTextContent(resources.getString(R.string.dlg_already_req_pay))
        tmp_dlg.CF_setDoubleButtonText(resources.getString(R.string.btn_cancel), resources.getString(R.string.btn_ok))
        tmp_dlg.setOnDismissListener { dialog ->
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
        LogPrinter.CF_debug("!-----------------------------------------------------------")
        LogPrinter.CF_debug("!-- IUII90M00_P.onBackPressed()")
        LogPrinter.CF_debug("!-----------------------------------------------------------")
        val tmp_curPageIndex = pager!!.currentItem

        // --<2> STEP 1단계 or (8단계 이면서 오류 화면 상태)인 경우
        if (tmp_curPageIndex == 0 || tmp_curPageIndex == submitPayPageIndex && !flagReqSuccess) {
            showDlgOfCancel()
        } else if (tmp_curPageIndex == userInfoPageIndex) {
            val fragmentUserInfo = arrFragment!![userInfoPageIndex] as IUII90M03_F?
            if (fragmentUserInfo!!.CF_isShownTransKeyPad()) {
                fragmentUserInfo.CF_closeTransKeyPad()
            }
        } else if (tmp_curPageIndex < submitPayPageIndex) {
            CF_showPrevPage()
        } else {
            // -- 서류첨부 솔루션 헬퍼 : 임시파일(이미지) 삭제
            val psMobileModule = PSMobileModule(applicationContext)
            psMobileModule.clearFiles()
            finish()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        LogPrinter.CF_debug("!-----------------------------------------------------------")
        LogPrinter.CF_debug("!-- IUII90M00_P.onDestroy()")
        LogPrinter.CF_debug("!-----------------------------------------------------------")
    }
    // #############################################################################################
    //  private 함수
    // #############################################################################################
    /**
     * 타이틀바 레이아웃 세팅
     */
    private fun setTitleBarUI() {
        LogPrinter.CF_debug("!-----------------------------------------------------------")
        LogPrinter.CF_debug("!-- IUII90M00_P.setTitleBarUI()")
        LogPrinter.CF_debug("!-----------------------------------------------------------")

        // 타이틀 세팅
        val tmp_title = findViewById<TextView>(R.id.title_bar_textTitle)
        tmp_title.text = resources.getString(R.string.title_request_child)

        // left 버튼 세팅
        btnBack = findViewById(R.id.title_bar_imgBtnLeft)
        btnBack.setVisibility(View.VISIBLE)
        btnBack.setOnClickListener(View.OnClickListener {
            val tmp_curPageIndex = pager!!.currentItem
            if (tmp_curPageIndex == 0 || tmp_curPageIndex == submitPayPageIndex && !flagReqSuccess) {
                showDlgOfCancel()
            } else if (tmp_curPageIndex < submitPayPageIndex) {
                CF_showPrevPage()
            } else {
                // -- 서류첨부 솔루션 헬퍼 : 임시파일(이미지) 삭제
                val psMobileModule = PSMobileModule(applicationContext)
                psMobileModule.clearFiles()
                finish()
            }
        })

        // -- Right 버튼 세팅
        val tmp_lp = RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.MATCH_PARENT)
        tmp_lp.addRule(RelativeLayout.ALIGN_PARENT_RIGHT)
        textBtnCancel = TextView(this)
        textBtnCancel!!.layoutParams = tmp_lp
        textBtnCancel!!.text = resources.getString(R.string.btn_cancel)
        textBtnCancel!!.contentDescription = resources.getString(R.string.btn_cancel) + " 버튼"
        textBtnCancel!!.setPadding(CommonFunction.CF_convertDipToPixel(applicationContext, 15f), 0, CommonFunction.CF_convertDipToPixel(applicationContext, 15f), 0)
        textBtnCancel!!.setTextColor(Color.WHITE)
        textBtnCancel!!.setTextSize(TypedValue.COMPLEX_UNIT_PX, resources.getDimensionPixelSize(R.dimen.button_text_size).toFloat())
        textBtnCancel!!.isClickable = true
        textBtnCancel!!.gravity = Gravity.CENTER_VERTICAL
        textBtnCancel!!.setBackgroundResource(R.drawable.img_button_bg_selector)
        textBtnCancel!!.setOnClickListener { showDlgOfCancel() }
        val tmp_titleLayout = findViewById<RelativeLayout>(R.id.title_bar_root)
        tmp_titleLayout.addView(textBtnCancel)
    }

    /**
     * Fragment 데이터 세팅<br></br>
     * Bundle 데이터가 있을 경우 Bundle 데이터로 세팅한다.<br></br>
     * @param p_bundle  Bundle
     */
    private fun setFragments(p_bundle: Bundle?) {
        LogPrinter.CF_debug("!-----------------------------------------------------------")
        LogPrinter.CF_debug("!-- IUII90M00_P.setFragments()")
        LogPrinter.CF_debug("!-----------------------------------------------------------")
        arrFragment = ArrayList()
        var tmp_fragment_1: IUII90M01_F? = null
        var tmp_fragment_2: IUII90M02_F? = null
        var tmp_fragment_3: IUII90M03_F? = null
        var tmp_fragment_4: IUII90M04_F? = null
        var tmp_fragment_5: IUII90M05_F? = null
        var tmp_fragment_6: IUII90M06_F? = null
        var tmp_fragment_7: IUII90M07_F? = null
        var tmp_fragment_9: IUII90M09_F? = null
        if (p_bundle != null) {
            tmp_fragment_1 = supportFragmentManager.getFragment(p_bundle, IUII90M01_F::class.java.name) as IUII90M01_F? // 1단계. 접수 시 확인사항
            tmp_fragment_2 = supportFragmentManager.getFragment(p_bundle, IUII90M02_F::class.java.name) as IUII90M02_F? // 2단계. 개인정보처리동의
            tmp_fragment_3 = supportFragmentManager.getFragment(p_bundle, IUII90M03_F::class.java.name) as IUII90M03_F? // 3단계. 보험금청구서 작성(개인정보)
            tmp_fragment_4 = supportFragmentManager.getFragment(p_bundle, IUII90M04_F::class.java.name) as IUII90M04_F? // 4단계. 보험금청구서 작성(청구내용1)
            tmp_fragment_5 = supportFragmentManager.getFragment(p_bundle, IUII90M05_F::class.java.name) as IUII90M05_F? // 5단계. 보험금청구서 작성(청구내용2)
            tmp_fragment_6 = supportFragmentManager.getFragment(p_bundle, IUII90M06_F::class.java.name) as IUII90M06_F? // 6단계. 구비서류첨부
            tmp_fragment_7 = supportFragmentManager.getFragment(p_bundle, IUII90M07_F::class.java.name) as IUII90M07_F? // 7단계. 계좌정보확인 및 추천국/추천인 선택 > 본인인증
            tmp_fragment_9 = supportFragmentManager.getFragment(p_bundle, IUII90M09_F::class.java.name) as IUII90M09_F? // 9단계. 청구완료
        }
        if (tmp_fragment_1 == null) {
            tmp_fragment_1 = Fragment.instantiate(this, IUII90M01_F::class.java.name) as IUII90M01_F
        }
        if (tmp_fragment_2 == null) {
            tmp_fragment_2 = Fragment.instantiate(this, IUII90M02_F::class.java.name) as IUII90M02_F
        }
        if (tmp_fragment_3 == null) {
            tmp_fragment_3 = Fragment.instantiate(this, IUII90M03_F::class.java.name) as IUII90M03_F
        }
        if (tmp_fragment_4 == null) {
            tmp_fragment_4 = Fragment.instantiate(this, IUII90M04_F::class.java.name) as IUII90M04_F
        }
        if (tmp_fragment_5 == null) {
            tmp_fragment_5 = Fragment.instantiate(this, IUII90M05_F::class.java.name) as IUII90M05_F
        }
        if (tmp_fragment_6 == null) {
            tmp_fragment_6 = Fragment.instantiate(this, IUII90M06_F::class.java.name) as IUII90M06_F
        }
        if (tmp_fragment_7 == null) {
            tmp_fragment_7 = Fragment.instantiate(this, IUII90M07_F::class.java.name) as IUII90M07_F
        }
        if (tmp_fragment_9 == null) {
            tmp_fragment_9 = Fragment.instantiate(this, IUII90M09_F::class.java.name) as IUII90M09_F
        }
        arrFragment!!.add(tmp_fragment_1)
        arrFragment!!.add(tmp_fragment_2)
        arrFragment!!.add(tmp_fragment_3)
        arrFragment!!.add(tmp_fragment_4)
        arrFragment!!.add(tmp_fragment_5)
        arrFragment!!.add(tmp_fragment_6)
        arrFragment!!.add(tmp_fragment_7)
        arrFragment!!.add(tmp_fragment_9)
    }

    /**
     * 현재 페이지에 맞는 UI 세팅 함수
     */
    private fun setUIStateOfPage(p_pageIndex: Int) {
        LogPrinter.CF_debug("!-----------------------------------------------------------")
        LogPrinter.CF_debug("!-- IUII90M00_P.setUIStateOfPage()")
        LogPrinter.CF_debug("!-----------------------------------------------------------")
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
     * @param p_index       int
     */
    private fun showPage(p_index: Int) {
        LogPrinter.CF_debug("!-----------------------------------------------------------")
        LogPrinter.CF_debug("!-- IUII90M00_P.showPage()")
        LogPrinter.CF_debug("!-----------------------------------------------------------")

        // StepIndicator 보이기
        CF_setVisibleStepIndicator(true)
        val tmp_fragment_2 = arrFragment!![agreePageIndex] as IUII90M02_F?

        // --<1> (2단계. 개인정보처리동의 페이지)
        if (p_index == agreePageIndex) {
            tmp_fragment_2!!.CF_setWebViewAccessbileImportant(true)

//        } else if(p_index == confirmReqInfoPageIndex) {
//            IUII90M08_F tmp_fragment_8 = (IUII90M08_F)arrFragment.get(confirmReqInfoPageIndex);
//            tmp_fragment_8.CF_setData();
        } else {
            if (tmp_fragment_2 != null && tmp_fragment_2.isAdded) {
                tmp_fragment_2.CF_setWebViewAccessbileImportant(false)
            }
            // --<2> (3단계 개인정보입력페이지) 지급청구 요청
            if (p_index == userInfoPageIndex) {
                val tmp_fragment = arrFragment!![p_index] as IUII90M03_F?
                tmp_fragment!!.httpReq_userInfo()
            } else if (p_index == submitPayPageIndex && !flagReqSuccess) {
                CF_requestSubmitPay()
            }
        }

        // 페이지 UI 세팅
        setUIStateOfPage(p_index)
        if (currentFocus != null) {
            CommonFunction.CF_closeVirtualKeyboard(this, currentFocus!!.windowToken)
        }
    }

    /**
     * 취소 버튼 show / hide 세팅
     */
    private fun setVisibilityOfCancelBtn() {
        LogPrinter.CF_debug("!-----------------------------------------------------------")
        LogPrinter.CF_debug("!-- IUII90M00_P.setVisibilityOfCancelBtn()")
        LogPrinter.CF_debug("!-----------------------------------------------------------")
        val tmp_curPageIndex = pager!!.currentItem
        if (tmp_curPageIndex == 0 || tmp_curPageIndex == submitPayPageIndex) {
            textBtnCancel!!.visibility = View.GONE
        } else {
            textBtnCancel!!.visibility = View.VISIBLE
        }
    }

    /**
     * 뒤로 가기 버튼의 이미지 세팅
     */
    private fun setImageResourOfBackBtn() {
        LogPrinter.CF_debug("!-----------------------------------------------------------")
        LogPrinter.CF_debug("!-- IUII90M00_P.setImageResourOfBackBtn()")
        LogPrinter.CF_debug("!-----------------------------------------------------------")
        if (pager!!.currentItem == submitPayPageIndex) {
            btnBack!!.setImageResource(R.drawable.ic_close_3)
        } else {
            btnBack!!.setImageResource(R.drawable.ic_prev)
        }
    }
    // #############################################################################################
    //  public 함수
    // #############################################################################################
    /**
     * 접근성 포커싱을 위해 단계 지시자에 포커스 옮기기
     */
    fun CF_requestFocusIndicator() {
        LogPrinter.CF_debug("!-----------------------------------------------------------")
        LogPrinter.CF_debug("!-- IUII90M00_P.CF_requestFocusIndicator()")
        LogPrinter.CF_debug("!-----------------------------------------------------------")
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
     * @param p_flagShow    boolean
     */
    fun CF_setVisibleStepIndicator(p_flagShow: Boolean) {
        LogPrinter.CF_debug("!-----------------------------------------------------------")
        LogPrinter.CF_debug("!-- IUII90M00_P.CF_setVisibleStepIndicator()")
        LogPrinter.CF_debug("!-----------------------------------------------------------")
        val tmp_appBar = findViewById<AppBarLayout>(R.id.appBar)
        tmp_appBar.setExpanded(p_flagShow, false)
    }
    // #############################################################################################
    //  Getter & Setter
    // #############################################################################################
    /**
     * 데이터 반환
     * @return  Data_IUII90M00
     */
    fun CF_getData(): Data_IUII90M00? {
        return data
    }

    /**
     * 청구구분
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
    // #############################################################################################
    //  Dialog 함수
    // #############################################################################################
    /**
     * 보험금청구신청 취소 다이얼로그 show 함수
     */
    private fun showDlgOfCancel() {
        LogPrinter.CF_debug("!-----------------------------------------------------------")
        LogPrinter.CF_debug("!-- IUII90M00_P.showDlgOfCancel()")
        LogPrinter.CF_debug("!-----------------------------------------------------------")
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
     * @param p_strError        String
     */
    fun CF_OnError(p_strError: String?) {
        LogPrinter.CF_debug("!-----------------------------------------------------------")
        LogPrinter.CF_debug("!-- IUII90M00_P.CF_OnError()")
        LogPrinter.CF_debug("!-----------------------------------------------------------")
        val tmp_dlg = CustomDialog(this)
        tmp_dlg.show()
        tmp_dlg.CF_setTextContent(p_strError)
        tmp_dlg.CF_setSingleButtonText(resources.getString(R.string.btn_ok))
        tmp_dlg.CF_setBackKeyMode(CustomDialog.BackMode.NOTUSE)
        tmp_dlg.setOnDismissListener { // -- 서류첨부 솔루션 헬퍼 : 임시파일(이미지) 삭제
            val psMobileModule = PSMobileModule(applicationContext)
            psMobileModule.clearFiles()
            finish()
        }
    }

    // #############################################################################################
    //  Http 관련
    // #############################################################################################
    override fun handleMessage(p_message: Message) {
        LogPrinter.CF_debug("!-----------------------------------------------------------")
        LogPrinter.CF_debug("!-- IUII90M00_P.handleMessage()")
        LogPrinter.CF_debug("!-----------------------------------------------------------")
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
                    val tmp_fragment = arrFragment!![submitPayPageIndex] as IUII90M09_F?
                    if (tmp_fragment != null && tmp_fragment.isAdded) {
                        tmp_fragment.CF_showVisibliteyPage(1)
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
        LogPrinter.CF_debug("!-----------------------------------------------------------")
        LogPrinter.CF_debug("!-- IUII90M00_P.CF_requestSubmitPay()")
        LogPrinter.CF_debug("!-----------------------------------------------------------")
        CF_showProgressDialog()
        val tmp_hashMap = data!!.CF_getDataMap()
        tmp_hashMap["s_pay_clam_nm"] = CustomSQLiteFunction.getLastLoginName(applicationContext) // 고객 이름
        tmp_hashMap["tempKey"] = SharedPreferencesFunc.getWebTempKey(applicationContext)
        LogPrinter.CF_debug("!-- 최종 청구 전송용 DATA")
        data!!.logPrint()
        HttpConnections.sendMultiData(
                EnvConfig.host_url + EnvConfig.URL_CLAIM_CHILD_REQ,
                data!!.CF_getUploadFileKeyList(),
                data!!.CF_getUploadFileList(),
                tmp_hashMap,
                HANDLERJOB_SUBMIT,
                HANDLERJOB_ERROR_SUBMIT,
                handler
        )
    }

    /**
     * 보험금 청구 접수 요청 결과 처리 함수
     * @param p_jsonObject  JSONObject
     * 2019-10-02     예외처리 로직 변경
     */
    private fun setResultOfSubmitPay(p_jsonObject: JSONObject) {
        LogPrinter.CF_debug("!-----------------------------------------------------------")
        LogPrinter.CF_debug("!-- IUII90M00_P.setResultOfSubmitPay()")
        LogPrinter.CF_debug("!-----------------------------------------------------------")
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
            if (p_jsonObject.has(jsonKey_errorCode)) {
                LogPrinter.CF_debug("!-- --<1> 에러코드키 있음 : 전문수신 성공")
                tmp_errorCode = p_jsonObject.getString(jsonKey_errorCode)
                when (tmp_errorCode) {
                    "" -> {
                        LogPrinter.CF_debug("!--<2> 에러코드 정상 : 청구 성공 수신")
                        if (p_jsonObject.has(jsonKey_data)) {
                            LogPrinter.CF_debug("!--<3> 데이터키 수신 성공")
                            val tmp_jsonData = p_jsonObject.getJSONObject(jsonKey_data)

                            // --<> (최종 성공)
                            if (tmp_jsonData.has(jsonKey_s_requ_recp_id) && "" != tmp_jsonData.getString(jsonKey_s_requ_recp_id)) {
                                LogPrinter.CF_debug("!--<4> 데이터 내용 파싱 : 접수번호 수신 성공")
                                s_requ_recp_id = tmp_jsonData.getString(jsonKey_s_requ_recp_id)
                                s_recp_cent_nm = tmp_jsonData.getString(jsonKey_s_recp_cent_nm)
                                s_recp_cent_tlno = tmp_jsonData.getString(jsonKey_s_recp_cent_tlno)
                                s_recp_cent_chps_nm = tmp_jsonData.getString(jsonKey_s_recp_cent_chps_nm)
                                s_requ_proc_schd_date = tmp_jsonData.getString(jsonKey_s_requ_proc_schd_date)
                                flagReqSuccess = true
                                CF_requestFocusIndicator()
                            } else {
                                LogPrinter.CF_debug("!--<4> 데이터 내용 파싱 : 접수번호 수신 실패")
                                CommonFunction.CF_showCustomAlertDilaog(this, resources.getString(R.string.dlg_error_server_2), resources.getString(R.string.btn_ok))
                            }
                        } else {
                            LogPrinter.CF_debug("!--<3> 데이터키 수신 실패")
                            CommonFunction.CF_showCustomAlertDilaog(this, resources.getString(R.string.dlg_error_server_2), resources.getString(R.string.btn_ok))
                        }
                    }
                    "ERRIUII40M00001" -> {
                        LogPrinter.CF_debug("!--<2> 에러코드 : 청구실패(ERRIUII40M00001)")
                        CommonFunction.CF_showCustomAlertDilaog(this, resources.getString(R.string.dlg_error_erriuii40m00001), resources.getString(R.string.btn_ok))
                    }
                    "ERRIUII40M00002" -> {
                        LogPrinter.CF_debug("!--<2> 에러코드 : 청구실패(ERRIUII40M00002)")
                        CommonFunction.CF_showCustomAlertDilaog(this, resources.getString(R.string.dlg_error_erriuii40m00002), resources.getString(R.string.btn_ok))
                    }
                    else -> {
                        LogPrinter.CF_debug("!--<2> 에러코드 : 청구실패(기타)")
                        CommonFunction.CF_showCustomAlertDilaog(this, resources.getString(R.string.dlg_error_server_2), resources.getString(R.string.btn_ok))
                    }
                }
            } else {
                LogPrinter.CF_debug("!--<1> 에러코드키 없음 : 수신실패")
                CommonFunction.CF_showCustomAlertDilaog(this, resources.getString(R.string.dlg_error_server_1), resources.getString(R.string.btn_ok))
            }
        } catch (e: NullPointerException) {
            LogPrinter.CF_debug("!-- 전문파싱 중에 에러 발생(NPE) :" + e.message)
        } catch (e: Exception) {
            LogPrinter.CF_debug("!-- 전문파싱 중에 에러 발생 :" + e.message)
            CommonFunction.CF_showCustomAlertDilaog(this, resources.getString(R.string.dlg_error_server_1), resources.getString(R.string.btn_ok))
        }

        // -----------------------------------------------------------------------------------------
        // -- 최종 결과 처리
        // -----------------------------------------------------------------------------------------
        // -- 서류첨부 솔루션 헬퍼 : 임시파일(이미지) 삭제
        val psMobileModule = PSMobileModule(applicationContext)
        psMobileModule.clearFiles()

        //  지급청구 성공 시(결과 화면), 실패 시(오류 화면) show
        val tmp_fragment = arrFragment!![submitPayPageIndex] as IUII90M09_F?
        if (tmp_fragment != null && tmp_fragment.isAdded) {
            // --<> (청구 성공)
            if (flagReqSuccess) {
                tmp_fragment.CF_setResultOk(s_requ_recp_id, s_recp_cent_nm, s_recp_cent_tlno, s_recp_cent_chps_nm, s_requ_proc_schd_date)
                tmp_fragment.CF_showVisibliteyPage(0)
            } else {
                tmp_fragment.CF_showVisibliteyPage(1)
            }
        }
    }
}