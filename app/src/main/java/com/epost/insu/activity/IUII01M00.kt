package com.epost.insu.activity

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableString
import android.text.format.Time
import android.text.style.ForegroundColorSpan
import android.view.View
import android.widget.*
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.epost.insu.*
import com.epost.insu.EnvConfig.ReqDvsn
import com.epost.insu.activity.IUII01M00
import com.epost.insu.activity.auth.IUCOB0M00
import com.epost.insu.activity.push.PushMessageActivity
import com.epost.insu.common.*
import com.epost.insu.dialog.CustomDialog

/**
 * Web에서 App 호출 시 사용되는 Activity
 * @since     :
 * @version   : 1.3
 * @author    : LSH
 * @see
 * <pre>
 *  - 보험금청구 제한 사항 안내 및 보험금청구 관련 메뉴 진입 화면
 *  - (보험금신청/지급진행조회/보험금청구절차안내/보험금청구시구비서류)
 * ======================================================================
 *          LSH_20170802    최초 등록
 *          NJM_20200122    공통 인증유형/청구유형 추가에 따른 로직수정
 *          NJM_20200130    지급청구 다이얼로그 로직 변경
 *          NJM_20200904    (상단)구비서류 안내영상 버튼 추가 (기존 아이콘 삭제)
 *          NJM_20201028    [모바일 사진촬영 패키지 도입]
 * 1.6.1    NJM_20210708    [청구가능시간 변경] 4~5시 청구 불가 처리
 * =======================================================================
 * copyright : (주)프로니스정보기술 | http://www.phronis.co.kr
 * </pre>
 */
class IUII01M00 : Activity_Default(), View.OnClickListener {
    private var mReqDvsn: ReqDvsn? = null // 청구구분

    override fun onClick(p_view: View) {
        LogPrinter.CF_debug("!----------------------------------------------------------")
        LogPrinter.CF_debug("!-- IUII01M00.onClick()")
        LogPrinter.CF_debug("!----------------------------------------------------------")
        when (p_view.id) {
            // -------------------------------------------------------------------------------------
            // -- 본인보험금청구 버튼
            // -------------------------------------------------------------------------------------
            R.id.relReqBenefit ->
                // --<1> (청구가능) 청구불가시 메시지 팝업
                if (EnvConfig.isPayEnableHour(this)) {
                    // 합산금액 체크(100만원)
                    val tmp_spannable: Spannable = SpannableString(resources.getString(R.string.dlg_money_over_30))
                    tmp_spannable.setSpan(ForegroundColorSpan(Color.parseColor("#ff7c29c7")), 10, 18, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)

                    // --<2> 접근성 On 인 경우 새창 알림 필요 : 감리대응
                    if (CommonFunction.CF_checkAccessibilityTurnOn(this)) {
                        val tmp_dlg = CustomDialog(this@IUII01M00)
                        tmp_dlg.show()
                        tmp_dlg.CF_setTextContent(tmp_spannable)
                        tmp_dlg.CF_setDoubleButtonText(resources.getString(R.string.btn_no), resources.getString(R.string.btn_yes))
                        tmp_dlg.setOnDismissListener { dialog ->
                            if ((dialog as CustomDialog).CF_getCanceled() == true) {
                                showCustomDialog(resources.getString(R.string.dlg_is_over_30), p_view)
                            } else {
                                showCustomDialog(resources.getString(R.string.dlg_same_person),
                                        p_view
                                ) { dialog ->
                                    if ((dialog as CustomDialog).CF_getCanceled() == false) {
                                        showCustomDialog(resources.getString(R.string.dlg_accessible_move_iuii10m00_p), p_view) { dialog ->
                                            if ((dialog as CustomDialog).CF_getCanceled() == false) {
                                                // (로그인상태) 지급청구로 진행
                                                if (SharedPreferencesFunc.getFlagLogin(applicationContext)) {
                                                    startIUII10M00_P()
                                                } else {
                                                    startLogin(p_view, ReqDvsn.SELF)
                                                }
                                            } else {
                                                clearAllFocus()
                                                p_view.requestFocus()
                                            }
                                            p_view.isFocusableInTouchMode = false
                                        }
                                    } else {
                                        showCustomDialog(resources.getString(R.string.dlg_save_person_2), p_view)
                                    }
                                }
                            }
                        }
                    } else {
                        val tmp_dlg = CustomDialog(this@IUII01M00)
                        tmp_dlg.show()
                        tmp_dlg.CF_setTextContent(tmp_spannable)
                        tmp_dlg.CF_setDoubleButtonText(resources.getString(R.string.btn_no), resources.getString(R.string.btn_yes))
                        tmp_dlg.setOnDismissListener { dialog ->
                            if ((dialog as CustomDialog).CF_getCanceled() == true) {
                                showCustomDialog(resources.getString(R.string.dlg_is_over_30), p_view)
                            } else {
                                val tmp_dlg_2 = CustomDialog(this@IUII01M00)
                                tmp_dlg_2.show()
                                //tmp_dlg_2.CF_setTextContent(getResources().getString(R.string.dlg_same_person));
                                val tmp_spannable: Spannable = SpannableString(resources.getString(R.string.dlg_same_person))
                                tmp_spannable.setSpan(ForegroundColorSpan(Color.parseColor("#ff7c29c7")), 4, 18, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                                tmp_dlg_2.CF_setTextContent(tmp_spannable)
                                tmp_dlg_2.CF_setDoubleButtonText(resources.getString(R.string.btn_no), resources.getString(R.string.btn_yes))
                                tmp_dlg_2.setOnDismissListener { dialog ->
                                    if ((dialog as CustomDialog).CF_getCanceled() == false) {
                                        // ---------------------------------------------
                                        // -- 보험금청구 Activity 호출 함수
                                        // ---------------------------------------------
                                        // (로그인상태) 지급청구로 진행
                                        if (SharedPreferencesFunc.getFlagLogin(applicationContext)) {
                                            startIUII10M00_P()
                                        } else {
                                            startLogin(p_view, ReqDvsn.SELF)
                                        }
                                    } else {
                                        CommonFunction.CF_showCustomAlertDilaog(this@IUII01M00, resources.getString(R.string.dlg_save_person_2), resources.getString(R.string.btn_ok))
                                    }
                                }
                            }
                        }
                    }
                }
            // -------------------------------------------------------------------------------------
            // -- 자녀보험금청구 버튼
            // -------------------------------------------------------------------------------------
            R.id.relReqChild ->
                // --<1> (청구가능) 청구불가시 메시지 팝업
                if (EnvConfig.isPayEnableHour(this)) {
                    // -- 합산금액 체크(100만원)
                    val tmp_spannable: Spannable = SpannableString(resources.getString(R.string.dlg_money_over_30))
                    tmp_spannable.setSpan(ForegroundColorSpan(Color.parseColor("#ff7c29c7")), 10, 18, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)

                    // --<2> (접근성On 상태) 인 경우 새창 알림 필요 : 감리대응
                    if (CommonFunction.CF_checkAccessibilityTurnOn(this)) {
                        val tmp_dlg = CustomDialog(this@IUII01M00)
                        tmp_dlg.show()
                        tmp_dlg.CF_setTextContent(tmp_spannable)
                        tmp_dlg.CF_setDoubleButtonText(resources.getString(R.string.btn_no), resources.getString(R.string.btn_yes))
                        tmp_dlg.setOnDismissListener { dialog ->
                            if ((dialog as CustomDialog).CF_getCanceled() == true) {
                                showCustomDialog(resources.getString(R.string.dlg_is_over_30), p_view)
                            } else {
                                showCustomDialog(resources.getString(R.string.dlg_is_available_req_child),
                                        p_view
                                ) { dialog ->
                                    if ((dialog as CustomDialog).CF_getCanceled() == false) {
                                        showCustomDialog(resources.getString(R.string.dlg_accessible_move_iuii10m00_p),
                                                p_view
                                        ) { dialog ->
                                            if ((dialog as CustomDialog).CF_getCanceled() == false) {
                                                // ---------------------------------------------
                                                // -- 자녀보험금 청구 Activity 호출 함수
                                                // ---------------------------------------------
                                                // (로그인상태) 지급청구로 진행
                                                if (SharedPreferencesFunc.getFlagLogin(applicationContext)) {
                                                    startIUII90M00_P()
                                                } else {
                                                    startLogin(p_view, ReqDvsn.CHILDE)
                                                }
                                            } else {
                                                clearAllFocus()
                                                p_view.requestFocus()
                                            }
                                            p_view.isFocusableInTouchMode = false
                                        }
                                    } else {
                                        //showCustomDialog(getResources().getString(R.string.dlg_save_person_2), p_view);
                                        showCustomDialog(resources.getString(R.string.dlg_error_req_child), p_view)
                                    }
                                }
                            }
                        }
                    } else {
                        val tmp_dlg = CustomDialog(this@IUII01M00)
                        tmp_dlg.show()
                        tmp_dlg.CF_setTextContent(tmp_spannable)
                        tmp_dlg.CF_setDoubleButtonText(resources.getString(R.string.btn_no), resources.getString(R.string.btn_yes))
                        tmp_dlg.setOnDismissListener { dialog ->
                            if ((dialog as CustomDialog).CF_getCanceled() == true) {
                                showCustomDialog(resources.getString(R.string.dlg_error_req_child), p_view)
                            } else {
                                val tmp_dlg_2 = CustomDialog(this@IUII01M00)
                                tmp_dlg_2.show()
                                //tmp_dlg_2.CF_setTextContent(getResources().getString(R.string.dlg_same_person));
                                val tmp_spannable: Spannable = SpannableString(resources.getString(R.string.dlg_is_available_req_child))
                                tmp_spannable.setSpan(ForegroundColorSpan(Color.parseColor("#ff7c29c7")), 6, 20, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                                tmp_dlg_2.CF_setTextContent(tmp_spannable)
                                tmp_dlg_2.CF_setDoubleButtonText(resources.getString(R.string.btn_no), resources.getString(R.string.btn_yes))
                                tmp_dlg_2.setOnDismissListener { dialog ->
                                    if ((dialog as CustomDialog).CF_getCanceled() == false) {
                                        // ---------------------------------------------
                                        // -- 자녀보험금 청구 Activity 호출 함수
                                        // ---------------------------------------------
                                        // (로그인상태) 지급청구로 진행
                                        if (SharedPreferencesFunc.getFlagLogin(applicationContext)) {
                                            startIUII90M00_P()
                                        } else {
                                            startLogin(p_view, ReqDvsn.CHILDE)
                                        }
                                    } else {
                                        CommonFunction.CF_showCustomAlertDilaog(this@IUII01M00, resources.getString(R.string.dlg_error_req_child), resources.getString(R.string.btn_ok))
                                    }
                                }
                            }
                        }
                    }
                }
            // -------------------------------------------------------------------------------------
            // -- 청구내역조회 버튼
            // -------------------------------------------------------------------------------------
            R.id.relSearch ->
                // -- (로그인 상태)
                if (SharedPreferencesFunc.getFlagLogin(applicationContext)) {
                    startIUII50M00_P()
                } else {
                    startLogin(p_view, ReqDvsn.INQUERY)
                }
            R.id.btnReqCourse -> startIUII60M00()
            R.id.btnReqDoc -> startIUII70M00()
            R.id.btnReqVideo -> WebBrowserHelper.startWebViewActivity(applicationContext, 0, false, EnvConfig.host_url + "/AS/IUAS70M00.do", resources.getString(R.string.btn_guide_video_title))
            else -> {
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)




    }

    override fun setInit() {
        setContentView(R.layout.iuii01m00)

        if(EnvConfig.uiTest){
            val btn_test = findViewById<Button>(R.id.btn_test) // 보험금청구 절차안내
            btn_test.setOnClickListener {


                //EnvConfig.AuthDvsn tmp_loginType = SharedPreferencesFunc.getLoginAuthDvsn(getApplicationContext());
                //String tmp_tempKey = SharedPreferencesFunc.getWebTempKey(getApplicationContext());
                //final String tmp_url = EnvConfig.host_url + subUrl_web + "?page=" + "27" + "&csno=" + tmp_csNo + "&loginType=" + tmp_loginType + "&tempKey=" + tmp_tempKey;
                val tmp_csNo = CustomSQLiteFunction.getLastLoginCsno(applicationContext)
                val tmp_url = EnvConfig.host_url + "/CO/IUCOP0M10.do" + "?csno=" + tmp_csNo
                WebBrowserHelper.startWebViewActivity(
                    applicationContext,
                    0,
                    false,
                    tmp_url,
                    resources.getString(R.string.btn_my_notice)
                )


            }
        }
    }

    override fun onResume() {
        super.onResume()
    }

    override fun onActivityResult(p_requestCode: Int, p_resultCode: Int, p_data: Intent?) {
        super.onActivityResult(p_requestCode, p_resultCode, p_data)
        LogPrinter.CF_debug("!----------------------------------------------------------")
        LogPrinter.CF_debug("!-- IUII01M00.onActivityResult()")
        LogPrinter.CF_debug("!----------------------------------------------------------")

        // --<> (인증완료) startIUCOB0M00() 콜백
        if (p_requestCode == EnvConfig.REQUESTCODE_ACTIVITY_IUCOB0M00 && p_resultCode == RESULT_OK) {
            // --<1> (본인청구)
            if (mReqDvsn == ReqDvsn.SELF) {
                startIUII10M00_P()
            } else if (mReqDvsn == ReqDvsn.CHILDE) {
                startIUII90M00_P()
            } else if (mReqDvsn == ReqDvsn.INQUERY) {
                startIUII50M00_P()
            }
        }
    }

    override fun setUIControl() {
        LogPrinter.CF_debug("!----------------------------------------------------------")
        LogPrinter.CF_debug("!-- IUII01M00.setUIControl()")
        LogPrinter.CF_debug("!----------------------------------------------------------")
        // 타이틀바 UI
        setTitleBarUI()
        // 첫번째 안내 문구 Text 세팅
        val tmp_textGuide_1 = findViewById<TextView>(R.id.textGuide_1)
        val tmp_spannable: Spannable = SpannableString(resources.getString(R.string.guide_req_benefit_1))
        tmp_spannable.setSpan(ForegroundColorSpan(Color.parseColor("#ff7c29c7")), 10, 19, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        tmp_textGuide_1.text = tmp_spannable
        val tmp_textGuide_2 = findViewById<TextView>(R.id.textGuide_2)
        val tmp_spannable_2: Spannable = SpannableString(resources.getString(R.string.guide_req_benefit_2))
        tmp_spannable_2.setSpan(ForegroundColorSpan(Color.parseColor("#ff7c29c7")), 16, 20, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        tmp_spannable_2.setSpan(ForegroundColorSpan(Color.parseColor("#ff7c29c7")), 24, 27, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        //tmp_spannable_2.setSpan(new ForegroundColorSpan(Color.rgb(254,0,0)), 54,68, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        tmp_textGuide_2.text = tmp_spannable_2
        val tmp_textGuide_3 = findViewById<TextView>(R.id.textGuide_3)
        val tmp_spannable_3: Spannable = SpannableString(resources.getString(R.string.guide_req_benefit_3))
        tmp_spannable_3.setSpan(ForegroundColorSpan(Color.parseColor("#ff7c29c7")), 15, 28, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        tmp_spannable_3.setSpan(ForegroundColorSpan(Color.parseColor("#ff7c29c7")), 40, 57, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        tmp_textGuide_3.text = tmp_spannable_3
        val tmp_textGuide_4 = findViewById<TextView>(R.id.textGuide_4)
        val tmp_spannable_4: Spannable = SpannableString(resources.getString(R.string.guide_req_benefit_4))
        //tmp_spannable_4.setSpan(new ForegroundColorSpan(Color.rgb(254,0,0)), 0,8, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        tmp_spannable_4.setSpan(ForegroundColorSpan(Color.parseColor("#ff7c29c7")), 18, 20, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        tmp_textGuide_4.text = tmp_spannable_4

        // 버튼 클릭 이벤트 세팅
        setBtnClickEvent()
    }

    /**
     * 버튼 UI 세팅 함수
     */
    private fun setBtnClickEvent() {
        val tmp_btnReqCourse = findViewById<Button>(R.id.btnReqCourse) // 보험금청구 절차안내 버튼
        val tmp_btnReqDoc = findViewById<Button>(R.id.btnReqDoc) // 보험금청구시 구비서류 버튼
        val tmp_btnReqVideo = findViewById<Button>(R.id.btnReqVideo) // 구비서류 안내영상 버튼
        tmp_btnReqCourse.setOnClickListener(this)
        tmp_btnReqDoc.setOnClickListener(this)
        tmp_btnReqVideo.setOnClickListener(this)
        val tmp_btnReq = findViewById<RelativeLayout>(R.id.relReqBenefit)
        val tmp_btnReqChild = findViewById<RelativeLayout>(R.id.relReqChild)
        val tmp_btnSearch = findViewById<RelativeLayout>(R.id.relSearch)
        tmp_btnReq.setOnClickListener(this)
        tmp_btnReqChild.setOnClickListener(this)
        tmp_btnSearch.setOnClickListener(this)
    }

    /**
     * 타이틀바 레이아웃 세팅
     */
    private fun setTitleBarUI() {
        // -- 타이틀 세팅
        val tmp_title = findViewById<TextView>(R.id.title_bar_textTitle)
        tmp_title.text = resources.getString(R.string.title_req_benefit)
        // --left 버튼 세팅
        val tmp_btnLeft = findViewById<ImageButton>(R.id.title_bar_imgBtnLeft)
        tmp_btnLeft.visibility = View.VISIBLE
        tmp_btnLeft.setOnClickListener { finish() }
    }

    /**
     * 비로그인시 로그인 Activity 실행
     * @param p_view        View
     * @param reqDvsn       EnvConfig.ReqDvsn
     */
    private fun startLogin(p_view: View, reqDvsn: ReqDvsn) {
        showCustomDialog(resources.getString(R.string.dlg_need_login_menu), p_view) { dialog ->
            if ((dialog as CustomDialog).CF_getCanceled() == false) {
                // ---------------------------------------------
                // -- 로그인 Activity 호출 함수
                // ---------------------------------------------
                startIUCOB0M00(reqDvsn)
            } else if (CommonFunction.CF_checkAccessibilityTurnOn(applicationContext)) {
                clearAllFocus()
                p_view.requestFocus()
            }
            p_view.isFocusableInTouchMode = false
        }
    }

    // #############################################################################################
    //  Activity 호출 함수
    // #############################################################################################
    /**
     * 보험금청구 Activity 호출 함수
     */
    private fun startIUII10M00_P() {
        val tmp_intent = Intent(this, IUII10M00_P::class.java)
        tmp_intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
        tmp_intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        startActivity(tmp_intent)
    }

    /**
     * 자녀보험금청구 Activity 호출 함수
     */
    private fun startIUII90M00_P() {
        val tmp_intent = Intent(this, IUII90M00_P::class.java)
        tmp_intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
        tmp_intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        startActivity(tmp_intent)
    }

    /**
     * 보험금 청구내역 조회 Activity 호출 함수
     */
    private fun startIUII50M00_P() {
        val tmp_intent = Intent(this, IUII50M00_P::class.java)
        tmp_intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
        tmp_intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        startActivity(tmp_intent)
    }

    /**
     * 로그인 방법선택 Activity 호출 함수
     */
    private fun startIUCOB0M00(reqDvsn: ReqDvsn) {
        mReqDvsn = reqDvsn
        val tmp_intent = Intent(this, IUCOB0M00::class.java)
        tmp_intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
        tmp_intent.putExtra("reqDvsn", reqDvsn) // 요청구분
        startActivityForResult(tmp_intent, EnvConfig.REQUESTCODE_ACTIVITY_IUCOB0M00)
    }

    /**
     * 보험금청구 절차안내 Activity 호출 함수
     */
    private fun startIUII60M00() {
        val tmp_intent = Intent(this, IUII60M00::class.java)
        tmp_intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
        startActivity(tmp_intent)
    }

    /**
     * 보험금청구 구비서류안내 Activity 호출 함수
     */
    private fun startIUII70M00() {
        val tmp_intent = Intent(this, IUII70M00::class.java)
        tmp_intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
        startActivity(tmp_intent)
    }
}