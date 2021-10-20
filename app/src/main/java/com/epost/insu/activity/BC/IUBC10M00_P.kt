package com.epost.insu.activity.BC

import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.*
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.viewpager.widget.ViewPager.OnPageChangeListener
import com.dreamsecurity.magicxsign.MagicXSign_Exception
import com.epost.insu.EnvConfig
import com.epost.insu.R
import com.epost.insu.SharedPreferencesFunc
import com.epost.insu.activity.Activity_Default
import com.epost.insu.adapter.SmartReqBenefitPageAdapter
import com.epost.insu.common.*
import com.epost.insu.control.StepIndicator
import com.epost.insu.data.Data_IUII10M00
import com.epost.insu.dialog.CustomDialog
import com.epost.insu.fragment.*
import com.epost.insu.network.HttpConnections
import com.epost.insu.network.WeakReferenceHandler
import com.epost.insu.network.WeakReferenceHandler.ObjectHandlerMessage
import com.epost.insu.service.AES256Util
import com.google.android.material.appbar.AppBarLayout
import org.json.JSONException
import org.json.JSONObject
import java.io.File
import java.io.UnsupportedEncodingException
import java.security.InvalidAlgorithmParameterException
import java.security.InvalidKeyException
import java.security.KeyPair
import java.security.NoSuchAlgorithmException
import java.util.*
import javax.crypto.BadPaddingException
import javax.crypto.IllegalBlockSizeException
import javax.crypto.NoSuchPaddingException

/**
 * @copyright : 우정사업정보센터
 *
 * @project   : 모바일슈랑스 구축
 * @pakage   : com.epost.insu.activity
 * @fileName  : IUBC10M00_P.java
 *
 * @Title     : 보험금청구 > 보험금청구 > 스마트보험금청구접수신청 (화면 ID : IUBC10M00_P)
 * @author    : 이경민
 * @created   : 2018-06-08
 * @version   : 1.0
 *
 * @note      : <u>보험금청구 > 보험금청구 > 스마트보험금청구접수신청 (화면 ID : IUBC10M00_P)</u><br></br>
 * 보험금청구접수신청 각 화면(Step 1 ~ Step 4)을 관리하는 Activity.
 * ======================================================================
 *          2018-06-08    이경민       최초 등록
 *          2019-02-04    이경민       페이지별 청구제한시간 적용, 에러처리 강화
 *          2019-09-23    이경민       서울의료원 추가
 *          2019-11-11    이경민       서울의료원 추가수정
 *          2019-11-19    이경민       임시청구접수데이터에 병원코드(hospitalCode) 추가
 *          2020-04-28    이경민       액티비티 결과값 처리 코드 추가
 * 1.6.1    NJM_20210708    [청구가능시간 변경] 4~5시 청구 불가 처리
 * =======================================================================
 */
class IUBC10M00_P : Activity_Default(), ObjectHandlerMessage {
    private val subUrl_submit = "/II/IUII80M00.do" // 스마트보험금청구 접수
    private val subUrl_writeAuthTime = "/CO/IUCOC3M00.do"
    private val HANDLERJOB_SUBMIT = 0
    private val HANDLERJOB_ERROR_SUBMIT = 1
    private val HANDLERJOB_WRITE_AUTHTIME = 2
    private val HANDLERJOB_ERROR_WRITE_AUTHTIME = 3
    private val agreePageIndex = 0 // 사용자동의 페이지
    private val userInfoPageIndex = 1 // 개인정보처리동의 + SMS알림서비스동의
    private val reqContentsPageIndex1 = 2 // 보험금청구내용 페이지(개인정보입력)
    private val reqContentsPageIndex2 = 3 // 보험금청구내용 페이지(청구유형,발생원인,청구사유,진단명,실손가입여부,SMS알림서비스신청)
    private val submitPayPageIndex = 4 // 보험청구신청 결과 페이지
    private var indicator: StepIndicator? = null // 단계 표기 컨트롤
    private var pager: CustomViewPager? = null // ViewPager
    private var btnBack: ImageButton? = null // '뒤로' 버튼
    private var textBtnCancel: TextView? = null // 취소 버튼(TextView)
    private var arrFragment: ArrayList<IUBC10M00_FD?>? = null
    private var data: Data_IUII10M00? = null
    private var flagReqSuccess= false // 보험금청구 성공 유무
    private val SYMMETRIC_KEY_SIZE = 32
    private val symmetricKey = "422d3951ef007cc32e2f714ccb6ff8dc"
    private var aes256Util: AES256Util? = null
    private var plainText: String? = null
    private var certificateIndex = 0
    private var pair: KeyPair? = null
    private var helper: CertKeyHelper? = null

    // ---------------------------------------------------------------------------------------------
    //  내부직원 패스워드 입력 미사용시 flagUseLoginPw = false 세팅
    // ---------------------------------------------------------------------------------------------
    private val flagUseLoginPw = true // 내부 직원용 패스워드 입력 사용여부
    private val flagAuthPw = false // 내부 직원용 패스워드 입력 통과여부
    override fun handleMessage(p_message: Message) {
        if (!isDestroyed) {
            CF_dismissProgressDialog()
            when (p_message.what) {
                HANDLERJOB_SUBMIT -> try {
                    setResultOfSubmitPay(JSONObject(p_message.obj as String))
                } catch (e: JSONException) {
                    LogPrinter.CF_debug(resources.getString(R.string.log_json_exception))
                }
                HANDLERJOB_ERROR_SUBMIT -> CF_OnError(p_message.obj as String)
                HANDLERJOB_WRITE_AUTHTIME -> try {
                    setResultOfWriteSmartReqAuthTime(JSONObject(p_message.obj as String))
                } catch (e: JSONException) {
                    LogPrinter.CF_debug(resources.getString(R.string.log_json_exception))
                }
                HANDLERJOB_ERROR_WRITE_AUTHTIME -> CF_OnError(p_message.obj as String)
                else -> {
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Fragment 생성및 복구
        setFragments(savedInstanceState)
        val adapter = SmartReqBenefitPageAdapter(supportFragmentManager, arrFragment)
        pager!!.adapter = adapter
        setUIStateOfPage(0)

        // 청구가능시간 체크, 스마트보험금청구 인증시간 기록요청
        if (EnvConfig.isPayEnableHour(this)) requestWriteSmartReqAuthTime()
    }

    override fun onResume() {
        super.onResume()
        // -----------------------------------------------------------------------------------------
        //  일반 고객 로그인을 막기 위한 패스워드 입력 레이아웃 show
        //  : 일반 고객 대상 배포시 관련 파트 전체 삭제 필요.
        // -----------------------------------------------------------------------------------------
//        if(flagUseLoginPw && flagAuthPw == false){
//
//            showTempPwView();
//        }
    }

    /**
     * 내부직원 패스워드 입력 임시 레이아웃 show
     */
    private fun showTempPwView() {
        val tmp_relPw = findViewById<View>(R.id.relTempPw) as RelativeLayout
        tmp_relPw.visibility = View.VISIBLE
        val tmp_edit = findViewById<View>(R.id.edtPw) as EditText
        tmp_edit.setText("")
    }

    /**
     * 내부고객용 패스워드 입력 레이아웃 세팅
     */
    private fun setTempPwLayoutUI() {
        val tmp_textGuide = findViewById<View>(R.id.textTempPwGuide) as TextView
        val tmp_spannable: Spannable = SpannableString("본 시스템은 내부직원 대상으로 시범운영 중에 있습니다.\n내부직원 인증번호 입력 후 서비스 이용이 가능합니다.\n\n인증번호 입력")
        tmp_spannable.setSpan(ForegroundColorSpan(Color.parseColor("#ff7c29c7")), 17, 21, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        tmp_spannable.setSpan(ForegroundColorSpan(Color.parseColor("#ff7c29c7")), 35, 40, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        tmp_textGuide.text = tmp_spannable
        val tmp_btnCancel = findViewById<View>(R.id.btnCancelPw) as Button
        val tmp_btnOk = findViewById<View>(R.id.btnOkPw) as Button


        // 취소 버튼 이벤트 연결
        tmp_btnCancel.setOnClickListener {
            val tmp_edit = findViewById<View>(R.id.edtPw) as EditText

            // 키패드 내리기
            CommonFunction.CF_closeVirtualKeyboard(this@IUBC10M00_P, tmp_edit.windowToken)

            // 패스워드 입력 View HIDE
            val tmp_rel = findViewById<View>(R.id.relTempPw) as RelativeLayout
            tmp_rel.visibility = View.GONE
            setResult(RESULT_CANCELED)
            finish()
        }

        // 확인 버튼 이벤트 연결 : 패스워드 일치시 로그인 화면 이동
        tmp_btnOk.setOnClickListener {
            val tmp_edit = findViewById<View>(R.id.edtPw) as EditText
            val tmp_inputPw = tmp_edit.text.toString().trim { it <= ' ' }
            if (tmp_inputPw == "smartclaim2018*") {

                ////flagAuthPw = true;

                // 키패드 내리기
                CommonFunction.CF_closeVirtualKeyboard(this@IUBC10M00_P, tmp_edit.windowToken)

                // 패스워드 입력 View HIDE
                val tmp_rel = findViewById<View>(R.id.relTempPw) as RelativeLayout
                tmp_rel.visibility = View.GONE
                /*
                    // 스마트보험금청구 인증시간 기록요청
                    requestWriteSmartReqAuthTime(); */
            } else {
                val tmp_dlg = CustomDialog(this@IUBC10M00_P)
                tmp_dlg.show()
                tmp_dlg.CF_setTextContent("인증번호가 틀렸습니다.\n다시 입력해 주세요.")
                tmp_dlg.CF_setSingleButtonText(resources.getString(R.string.btn_ok))
                tmp_dlg.setOnDismissListener { tmp_edit.setText("") }
            }
        }
    }

    override fun onSaveInstanceState(p_bundle: Bundle) {
        super.onSaveInstanceState(p_bundle)
        // -----------------------------------------------------------------------------------------
        //  Fragment 번들 저장
        // -----------------------------------------------------------------------------------------
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

        ////p_bundle.putBoolean("flagAuthPw",flagAuthPw);
    }

    override fun onRestoreInstanceState(p_bundle: Bundle) {
        super.onRestoreInstanceState(p_bundle)
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

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        val tmp_dlg = CustomDialog(this)
        tmp_dlg.show()
        tmp_dlg.CF_setBackKeyMode(CustomDialog.BackMode.CANCELED)
        tmp_dlg.CF_setTextContent(resources.getString(R.string.dlg_already_req_pay))
        tmp_dlg.CF_setDoubleButtonText(resources.getString(R.string.btn_cancel), resources.getString(R.string.btn_ok))
        tmp_dlg.setOnDismissListener { dialog ->
            if ((dialog as CustomDialog).CF_getCanceled() == false) {
                deleteImageFiles()
                finish()
                startActivity(getIntent())
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == EnvConfig.REQUESTCODE_ACTIVITY_IUBC11M00 && resultCode == RESULT_OK) {
            setResult(RESULT_OK)
            finish()
        }
    }

    override fun setInit() {
        setContentView(R.layout.iubc10m00_p)
        data = Data_IUII10M00()
        flagReqSuccess = false
        handler = WeakReferenceHandler(this)
        helper = CertKeyHelper(this, EnvConfig.xSignDebugLevel)
        try {
            if (symmetricKey.length != SYMMETRIC_KEY_SIZE) {
                return
            }
            aes256Util = AES256Util(symmetricKey)
        } catch (e: UnsupportedEncodingException) {
            e.message
        }
    }

    /**
     * Fragment 데이터 세팅<br></br>
     * Bundle 데이터가 있을 경우 Bundle 데이터로 세팅한다.<br></br>
     * @param p_bundle
     */
    private fun setFragments(p_bundle: Bundle?) {
        arrFragment = ArrayList()
        var tmp_fragment_1: IUBC10M00_F? = null // 1. 보험금신청 안내
        var tmp_fragment_2: IUBC11M00_F? = null // 2. 개인정보 수집이용동의 + SMS알림서비스추가
        var tmp_fragment_3: IUBC12M00_F? = null // 3. 보험금 청구서작성(개인정보)
        var tmp_fragment_4: IUBC13M00_F? = null // 4. 보험금 청구서작성(추가정보1)
        if (p_bundle != null) {
            tmp_fragment_1 = supportFragmentManager.getFragment(p_bundle, IUBC10M00_F::class.java.name) as IUBC10M00_F?
            tmp_fragment_2 = supportFragmentManager.getFragment(p_bundle, IUBC11M00_F::class.java.name) as IUBC11M00_F?
            tmp_fragment_3 = supportFragmentManager.getFragment(p_bundle, IUBC12M00_F::class.java.name) as IUBC12M00_F?
            tmp_fragment_4 = supportFragmentManager.getFragment(p_bundle, IUBC13M00_F::class.java.name) as IUBC13M00_F?
        }
        if (tmp_fragment_1 == null) {
            tmp_fragment_1 = Fragment.instantiate(this, IUBC10M00_F::class.java.name) as IUBC10M00_F
        }
        if (tmp_fragment_2 == null) {
            tmp_fragment_2 = Fragment.instantiate(this, IUBC11M00_F::class.java.name) as IUBC11M00_F
        }
        if (tmp_fragment_3 == null) {
            tmp_fragment_3 = Fragment.instantiate(this, IUBC12M00_F::class.java.name) as IUBC12M00_F
        }
        if (tmp_fragment_4 == null) {
            tmp_fragment_4 = Fragment.instantiate(this, IUBC13M00_F::class.java.name) as IUBC13M00_F
        }
        arrFragment!!.add(tmp_fragment_1) // 보험금신청안내 페이지(page 0)
        arrFragment!!.add(tmp_fragment_2) // 개인정보처리동의 페이지(page 1)
        arrFragment!!.add(tmp_fragment_3) // 보험금 청구서작성(개인정보)
        arrFragment!!.add(tmp_fragment_4) // 보험금 청구서작성 페이지(page 2)
    }

    override fun setUIControl() {
        // 타이틀바 세팅
        setTitleBarUI()

        // 내부직원 패스워드 입력 임시 레이아웃 세팅
        setTempPwLayoutUI()
        pager = findViewById<View>(R.id.activity_req_viewPager) as CustomViewPager
        pager!!.CF_setPagingEnabled(false)
        pager!!.importantForAccessibility = View.IMPORTANT_FOR_ACCESSIBILITY_NO
        pager!!.addOnPageChangeListener(object : OnPageChangeListener {
            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {}
            override fun onPageSelected(position: Int) {
                // 페이지의 다음 버튼 활성화
                arrFragment!![position]!!.CF_setBtnNextEnabled()
                arrFragment!![position]!!.CF_scrollTop()
            }

            override fun onPageScrollStateChanged(state: Int) {}
        })
        indicator = findViewById<View>(R.id.activity_req_indicator) as StepIndicator
    }

    override fun onBackPressed() {
        // -----------------------------------------------------------------------------------------
        //  내부직원용 PW 입력 레이아웃 Visible 상태 GONE 상태에서만 처리
        // -----------------------------------------------------------------------------------------
        val tmp_relPw = findViewById<View>(R.id.relTempPw) as RelativeLayout
        if (tmp_relPw.visibility == View.GONE) {
            doBackPressed()
        }
    }

    fun doBackPressed() {
        val tmp_curPageIndex = pager!!.currentItem
        if (tmp_curPageIndex == 0 || tmp_curPageIndex == submitPayPageIndex && flagReqSuccess == false) {
            // -------------------------------------------------------------------------------------
            //  Step 1단계 또는 Step 8단계 이며 오류 화면 상태인 경우
            // -------------------------------------------------------------------------------------
            showDlgOfCancel()
        } else if (tmp_curPageIndex < submitPayPageIndex) {
            showPage(tmp_curPageIndex - 1)
        } else {
            finish()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (flagReqSuccess) {
            deleteImageFiles()
        }
    }

    /**
     * 타이틀바 레이아웃 세팅
     */
    private fun setTitleBarUI() {
        // 타이틀 세팅
        val tmp_title = findViewById<View>(R.id.title_bar_textTitle) as TextView
        tmp_title.text = resources.getString(R.string.title_smart_request)

        // Back 버튼 세팅
        btnBack = findViewById<View>(R.id.title_bar_imgBtnLeft) as ImageButton
        btnBack!!.visibility = View.VISIBLE
        btnBack!!.setOnClickListener { doBackPressed() }

        // right 버튼 세팅
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
        val tmp_titleLayout = findViewById<View>(R.id.title_bar_root) as RelativeLayout
        tmp_titleLayout.addView(textBtnCancel)
    }

    /**
     * 현재 페이지에 맞는 UI 세팅 함수
     */
    private fun setUIStateOfPage(p_pageIndex: Int) {
        indicator!!.CF_setSelectedIndex(p_pageIndex)
        pager!!.currentItem = p_pageIndex
        setVisibilityOfCancelBtn() // 취소버튼 상태 세팅
        setImageResourOfBackBtn() // 뒤로버튼 이미지 세팅
    }

    /**
     * 해당 page show 함수<br></br>
     * 해당 page를 보이고 indicator 값을 변경한다.<br></br>
     * @param p_index
     */
    private fun showPage(p_index: Int) {

        // 청구가능시간 체크
        if (!EnvConfig.isPayEnableHour(this)) return

        // StepIndicator 보이기
        CF_setVisibleStepIndicator(true)
        val tmp_fragment = arrFragment!![userInfoPageIndex] as IUBC11M00_F?
        when (p_index) {
            agreePageIndex -> {
                tmp_fragment!!.CF_setWebViewAccessbileImportant(false)
                setUIStateOfPage(p_index)
            }
            userInfoPageIndex -> {
                tmp_fragment!!.CF_setWebViewAccessbileImportant(true)
                setUIStateOfPage(p_index)
            }
            reqContentsPageIndex1 -> {
                tmp_fragment!!.CF_setWebViewAccessbileImportant(false)
                setUIStateOfPage(p_index)
                val tmp_fragment2 = arrFragment!![reqContentsPageIndex1] as IUBC12M00_F?
                tmp_fragment2!!.CF_requestUserInfo()
            }
            reqContentsPageIndex2 -> {
                tmp_fragment!!.CF_setWebViewAccessbileImportant(false)
                setUIStateOfPage(p_index)
            }
            submitPayPageIndex -> {
                CF_requestSubmitPay()
                tmp_fragment!!.CF_setWebViewAccessbileImportant(false)
            }
        }
        if (currentFocus != null) {
            CommonFunction.CF_closeVirtualKeyboard(this, currentFocus!!.windowToken)
        }
    }
    // #############################################################################################
    //  Activity 호출
    // #############################################################################################
    /**
     * (IUBC11M00) MedCerti 웹페이지 접근 Activity 호출 함수
     */
    private fun startIUBC11M00(csno: String) {
        val tmp_intent = Intent(this, IUBC11M00::class.java)
        tmp_intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
        tmp_intent.putExtra("csno", csno)
        startActivityForResult(tmp_intent, EnvConfig.REQUESTCODE_ACTIVITY_IUBC11M00)
    }

    /**
     * 취소 버튼 show / hide 세팅
     */
    private fun setVisibilityOfCancelBtn() {
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
        if (pager!!.currentItem == submitPayPageIndex) {
            btnBack!!.setImageResource(R.drawable.ic_close_3)
        } else {
            btnBack!!.setImageResource(R.drawable.ic_prev)
        }
    }

    /**
     * 구비서류 첨부 이미지 삭제
     */
    private fun deleteImageFiles() {
        val tmp_fileDir = File(Environment.getExternalStorageDirectory(), EnvConfig.IMAGE_TEMP_FOLER)
        if (tmp_fileDir.exists() && tmp_fileDir.isDirectory) {
            val tmp_files = tmp_fileDir.listFiles()
            if (tmp_files != null) {
                for (file in tmp_files) {
                    if (file != null && file.exists()) {
                        file.delete()
                    }
                }
            }
            tmp_fileDir.delete()
        }
    }
    // ---------------------------------------------------------------------------------------------
    //  public 함수
    // ---------------------------------------------------------------------------------------------
    /**
     * 접근성 포커싱을 위해 단계 지시자에 포커스 옮기기
     */
    fun CF_requestFocusIndicator() {
        clearAllFocus()
        Handler().postDelayed({
            indicator!!.isFocusableInTouchMode = true
            indicator!!.requestFocus()
            indicator!!.isFocusableInTouchMode = false
        }, 500)
    }

    /**
     * 데이터 반환
     * @return
     */
    fun CF_getData(): Data_IUII10M00? {
        return data
    }

    /**
     * 다음페이지 이동
     */
    fun CF_showNextPage() {
        showPage(pager!!.currentItem + 1)
    }

    /**
     * Step Indicator Show/Hide
     * @param p_flagShow
     */
    fun CF_setVisibleStepIndicator(p_flagShow: Boolean) {
        val tmp_appBar = findViewById<View>(R.id.appBar) as AppBarLayout
        tmp_appBar.setExpanded(p_flagShow, false)
    }
    // #############################################################################################
    //  Dialog
    // #############################################################################################
    /**
     * 보험금청구신청 취소 다이얼로그 show 함수
     */
    private fun showDlgOfCancel() {
        showCustomDialog(resources.getString(R.string.dlg_cancel_reqbenefit),
                resources.getString(R.string.btn_no),
                resources.getString(R.string.btn_yes),
                textBtnCancel!!
        ) { dialog ->
            if ((dialog as CustomDialog).CF_getCanceled() == false) {
                deleteImageFiles()
                setResult(RESULT_CANCELED)
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
     * @param p_strError
     */
    fun CF_OnError(p_strError: String?) {
        val tmp_dlg = CustomDialog(this)
        tmp_dlg.show()
        tmp_dlg.CF_setTextContent(p_strError)
        tmp_dlg.CF_setSingleButtonText(resources.getString(R.string.btn_ok))
        tmp_dlg.CF_setBackKeyMode(CustomDialog.BackMode.NOTUSE)
        tmp_dlg.setOnDismissListener { finish() }
    }

    /**
     * 인증서 관련정보 설정
     */
    fun CF_setCertificateData(p_plainText: String?, p_certificateIndex: Int) {
        plainText = p_plainText
        certificateIndex = p_certificateIndex
        try {
            helper!!.generateKeyPair(certificateIndex, plainText!!.toByteArray())
            pair = helper!!.keyPair
        } catch (e: NullPointerException) {
            e.message
        } catch (e: Exception) {
            e.message
        }
        return
    }
    // #############################################################################################
    //  Sqlite
    // #############################################################################################
    /**
     * 단말기 DB에 저장되어 있는 이름 반환
     * @return
     */
    private val user_name_fromSqlite: String
        get() {
            var tmp_name = ""
            val tmp_helper = CustomSQLiteHelper(applicationContext)
            val tmp_sqlite = tmp_helper.readableDatabase
            tmp_name = tmp_helper.CF_SelectUserName(tmp_sqlite)
            tmp_sqlite.close()
            tmp_helper.close()
            return tmp_name
        }

    /**
     * 단말기 DB에 저장된 로그인한 사용자 csNo 반환 함수
     * @return
     */
    private val user_csno_fromSqlite: String
        get() {
            var tmp_csNo = ""
            val tmp_helper = CustomSQLiteHelper(applicationContext)
            val tmp_sqlite = tmp_helper.readableDatabase
            tmp_csNo = tmp_helper.CF_Selectcsno(tmp_sqlite)
            tmp_helper.close()
            tmp_sqlite.close()
            return tmp_csNo
        }
    // #############################################################################################
    //  Http 관련
    // #############################################################################################
    /**
     * 보험금청구 접수 요청
     */
    fun CF_requestSubmitPay() {

        // 프로그레스 다이얼로그 show
        CF_showProgressDialog()
        var privateKey = ""
        var expireDate = ""
        try {
            privateKey = helper!!.aesEncode(helper!!.base64EncodeToString(pair!!.private.encoded))
            expireDate = helper!!.getExpireDate(certificateIndex)

            // Test Data
        } catch (e: MagicXSign_Exception) {
            e.message
        }
        val tmp_hashMap = data!!.CF_getDataMap()
        tmp_hashMap["s_pay_clam_nm"] = user_name_fromSqlite // 고객 이름
        tmp_hashMap["s_acdp_csno"] = user_csno_fromSqlite // 고객 식별번호
        tmp_hashMap["tempKey"] = SharedPreferencesFunc.getWebTempKey(applicationContext)
        tmp_hashMap["sn"] = SharedPreferencesFunc.getSmartReqAuthTime(applicationContext)
        tmp_hashMap["privateKey"] = privateKey
        tmp_hashMap["expireDate"] = expireDate
        tmp_hashMap["hospitalCode"] = SharedPreferencesFunc.getSmartReqHospitalCode(applicationContext)
        if ("devel" == EnvConfig.operation) {
            // 개발 Test Data
            var publicKey = ""
            var cert = ""
            try {
                publicKey = helper!!.base64EncodeToString(pair!!.public.encoded)
                cert = helper!!.getCertificate(certificateIndex)
            } catch (e: MagicXSign_Exception) {
                e.message
            }
            tmp_hashMap["publicKey"] = publicKey
            tmp_hashMap["cert"] = cert
        }
        HttpConnections.sendMultiData(EnvConfig.host_url + subUrl_submit,
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
     * @param p_jsonObject
     */
    @Throws(JSONException::class)
    private fun setResultOfSubmitPay(p_jsonObject: JSONObject) {
        val jsonKey_errorCode = "errCode"
        val jsonKey_result = "result"
        var tmp_errorCode = ""
        if (p_jsonObject.has(jsonKey_result)) {
            flagReqSuccess = p_jsonObject.getBoolean(jsonKey_result)
            if (flagReqSuccess) {
                startIUBC11M00(user_csno_fromSqlite)
                setResult(RESULT_OK)
                finish()
            } else {
                if (p_jsonObject.has(jsonKey_errorCode)) {
                    tmp_errorCode = p_jsonObject.getString(jsonKey_errorCode)
                    if (tmp_errorCode == "ERRIUII40M00001") {
                        showCustomDialog(resources.getString(R.string.dlg_error_erriuii40m00001), RESULT_CANCELED)
                    } else if (tmp_errorCode == "ERRIUII40M00002") {
                        showCustomDialog(resources.getString(R.string.dlg_error_erriuii40m00002), RESULT_CANCELED)
                    } else {
                        showCustomDialog(resources.getString(R.string.dlg_error_server_3), RESULT_CANCELED)
                    }
                } else {
                    showCustomDialog(resources.getString(R.string.dlg_error_parameter_error), RESULT_CANCELED)
                }
            }
        } else {
            showCustomDialog(resources.getString(R.string.dlg_error_parameter_error), RESULT_CANCELED)
        }
    }

    /**
     * 고객인증시간 기록(로그인 된 경우)
     */
    private fun requestWriteSmartReqAuthTime() {

        // 프로그레스 다이얼로그 show
        CF_showProgressDialog()
        val csno = user_csno_fromSqlite
        var encodedCsno: String? = ""
        try {
            encodedCsno = aes256Util!!.aesEncode(csno)
        } catch (e: NoSuchAlgorithmException) {
            e.message
        } catch (e: NoSuchPaddingException) {
            e.message
        } catch (e: InvalidKeyException) {
            e.message
        } catch (e: InvalidAlgorithmParameterException) {
            e.message
        } catch (e: IllegalBlockSizeException) {
            e.message
        } catch (e: BadPaddingException) {
            e.message
        } catch (e: UnsupportedEncodingException) {
            e.message
        }
        val tmp_builder = Uri.Builder()
        tmp_builder.appendQueryParameter("csno", encodedCsno)
        tmp_builder.appendQueryParameter("tempKey", SharedPreferencesFunc.getWebTempKey(applicationContext))
        HttpConnections.sendPostData(
                EnvConfig.host_url + subUrl_writeAuthTime,
                tmp_builder.build().encodedQuery,
                handler,
                HANDLERJOB_WRITE_AUTHTIME,
                HANDLERJOB_ERROR_WRITE_AUTHTIME)
    }

    /**
     * 고객인증시간 기록(로그인 된 경우) 결과 처리 함수
     * @param p_jsonObject
     */
    @Throws(JSONException::class)
    private fun setResultOfWriteSmartReqAuthTime(p_jsonObject: JSONObject) {
        val jsonKey_errorCode = "errCode"
        val jsonKey_result = "result"
        val jsonKey_sn = "sn"
        var tmp_errorCode = ""
        if (p_jsonObject.has(jsonKey_result)) {
            val flagReqSuccess = p_jsonObject.getBoolean(jsonKey_result)
            if (flagReqSuccess) {
                if (p_jsonObject.has(jsonKey_sn)) {
                    SharedPreferencesFunc.setSmartReqAuthTime(applicationContext, p_jsonObject.getString(jsonKey_sn))
                } else {
                    showCustomDialog(resources.getString(R.string.dlg_error_parameter_error), RESULT_CANCELED)
                }
            } else {
                if (p_jsonObject.has(jsonKey_errorCode)) {
                    tmp_errorCode = p_jsonObject.getString(jsonKey_errorCode)
                    if (tmp_errorCode == "ERRIUII40M00001") {
                        showCustomDialog(resources.getString(R.string.dlg_error_erriuii40m00001), RESULT_CANCELED)
                    } else if (tmp_errorCode == "ERRIUII40M00002") {
                        showCustomDialog(resources.getString(R.string.dlg_error_erriuii40m00002), RESULT_CANCELED)
                    } else {
                        showCustomDialog(resources.getString(R.string.dlg_error_server_3), RESULT_CANCELED)
                    }
                } else {
                    showCustomDialog(resources.getString(R.string.dlg_error_parameter_error), RESULT_CANCELED)
                }
            }
        } else {
            showCustomDialog(resources.getString(R.string.dlg_error_parameter_error), RESULT_CANCELED)
        }
    }
}