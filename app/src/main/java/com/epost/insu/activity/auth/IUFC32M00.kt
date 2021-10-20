package com.epost.insu.activity.auth

import android.content.DialogInterface
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.view.View
import android.widget.*
import com.epost.insu.*
import com.epost.insu.activity.Activity_Default
import com.epost.insu.common.CommonFunction
import com.epost.insu.common.LogPrinter
import com.epost.insu.control.CustomCheckView
import com.epost.insu.dialog.CustomDialog
import com.epost.insu.event.OnChangedCheckedStateEventListener
import com.epost.insu.fido.Fido2Constant
import com.epost.insu.module.FingerModule

/**
 * 인증센터 > 간편인증 등록/변경
 * @since     : project 48:1.4.5
 * @version   : 1.1
 * @author    : NJM
 * @see
 * <pre>
 * ======================================================================
 * 1.4.5    NJM_20201229    최초 등록
 * 1.5.2    NJM_20210317    [간편인증 등록화면에서 단말에 지문 없을 경우 메시지 팝업]
 * 1.5.2    NJM_20210317    onActivityResult에서 실패 메시지 팝업 처리
 * 1.5.9    NJM_20210701    [PIN등록 오류 수정] 최초설치시 PIN등록시 csno 누락 오류 수정
 * 1.6.2    NJM_20210729    [간편인증 플래그 반영] 간편인증 로그인시 flag값 참조 변경 (서버저장 -> 단말저장)
 * =======================================================================
 * copyright : (주)프로니스정보기술 | http://www.phronis.co.kr
 * </pre>
 */
class IUFC32M00 : Activity_Default() {
    private var selectedAuthTechCode: String = "" // 선택된 인증기술코드
    private lateinit var btnFill: Button
    private lateinit var btnSms: Button
    private lateinit var btnArs: Button
    private lateinit var arrCheckView: Array<CustomCheckView?> // 동의 체크View Array

    // 추가인증
    private lateinit var linSmArs: LinearLayout  // 추가인증 버튼
    private lateinit var linSmArsComplete: LinearLayout  // 추가인증 완료 텍스트
    private var isAuthComplete: Boolean = false // 추가인증 완료 여부

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        LogPrinter.CF_debug("!----------------------------------------------------------")
        LogPrinter.CF_debug("!-- IUFC32M00.onActivityResult() --")
        LogPrinter.CF_debug("!----------------------------------------------------------")
        LogPrinter.CF_debug("!-- requestCode : $requestCode")
        LogPrinter.CF_debug("!-- resultCode  : $resultCode")
        LogPrinter.CF_debug("!-- data        : " + CommonFunction.CF_intentToString(data))

        if (resultCode == RESULT_OK) {
            when (requestCode) {
                EnvConfig.REQUESTCODE_ACTIVITY_IUPC70M00 -> {
                    isAuthComplete = true
                    linSmArs.visibility = View.GONE
                    linSmArsComplete.visibility = View.VISIBLE
                }
                EnvConfig.REQUESTCODE_ACTIVITY_IUFC10M00 -> {
                    SharedPreferencesFunc.setFlagRegAuthDvsn(applicationContext, true, EnvConfig.AuthDvsn.getAuthDvsnByFido(selectedAuthTechCode)) // -- 인증 등록 여부
                    startIUPC39M00()
                }
                EnvConfig.REQUESTCODE_ACTIVITY_IUPC39M00, EnvConfig.REQUESTCODE_ACTIVITY_IUPC34M00 -> {
                    setResult(RESULT_OK)
                    finish()
                }
            }
        }
        else {
            var rtnMsg: String? = ""
            if (data != null && data.hasExtra(EnvConstant.KEY_INTENT_RTN_MSG)) {
                rtnMsg = data.extras!!.getString(EnvConstant.KEY_INTENT_RTN_MSG)
            }
            setResult(RESULT_CANCELED)
            if ("" != rtnMsg) {
                CommonFunction.CF_showCustomAlertDilaog(this@IUFC32M00, rtnMsg)
            } else {
                finish()
            }
        }
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
    }

    override fun onBackPressed() {
        val customDialog = CustomDialog(this@IUFC32M00)
        customDialog.show()
        customDialog.CF_setBackKeyMode(CustomDialog.BackMode.CANCELED)
        customDialog.CF_setTextContent(getResources().getString(R.string.dlg_cancel_reg_bio))
        customDialog.CF_setDoubleButtonText(getResources().getString(R.string.btn_no), getResources().getString(R.string.btn_yes))
        customDialog.setOnDismissListener(object : DialogInterface.OnDismissListener {
            override fun onDismiss(dialog: DialogInterface) {
                if (!(dialog as CustomDialog).CF_getCanceled()) {
                    setResult(RESULT_CANCELED)
                    finish()
                }
            }
        })
    }

    override fun setInit() {
        LogPrinter.CF_debug("!----------------------------------------------------------")
        LogPrinter.CF_debug("!-- IUFC32M00.setInit() --")
        LogPrinter.CF_debug("!----------------------------------------------------------")

        setContentView(R.layout.iufc32m00)
        arrCheckView = arrayOfNulls(3)
    }

    override fun setUIControl() {
        LogPrinter.CF_debug("!----------------------------------------------------------")
        LogPrinter.CF_debug("!-- IUFC32M00.setUIControl() --")
        LogPrinter.CF_debug("!----------------------------------------------------------")

        // -- 타이틀 세팅 함수
        setTitleBarUI()
        val linBioReg1: LinearLayout = findViewById(R.id.linBioReg_1)
        val linBioReg2: LinearLayout = findViewById(R.id.linBioReg_2)
        val linBioReg3: LinearLayout = findViewById(R.id.linBioReg_3)

        // Image 세팅
        (linBioReg1.findViewById<View>(R.id.imageView) as ImageView).setImageResource(R.drawable.ic_pin)
        (linBioReg2.findViewById<View>(R.id.imageView) as ImageView).setImageResource(R.drawable.ic_finger)
        (linBioReg3.findViewById<View>(R.id.imageView) as ImageView).setImageResource(R.drawable.ic_pattern)

        // Text 세팅
        val txtBioReg1: TextView = linBioReg1.findViewById(R.id.text)
        val txtBioReg2: TextView = linBioReg2.findViewById(R.id.text)
        val txtBioReg3: TextView = linBioReg3.findViewById(R.id.text)

        // (개인인증번호)
        var bioBtnFinStr: String = resources.getString(R.string.btn_bio_login_fin)
        if (SharedPreferencesFunc.getFlagRegAuthDvsn(applicationContext, EnvConfig.AuthDvsn.getAuthDvsnByFido(Fido2Constant.AUTH_TECH_PIN))) {
            bioBtnFinStr += "(등록)"
            val spannable1: Spannable = SpannableString(bioBtnFinStr)
            spannable1.setSpan(ForegroundColorSpan(Color.rgb(0, 0, 154)), 0, spannable1.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
            txtBioReg1.text = spannable1
        } else {
            txtBioReg1.text = bioBtnFinStr
        }

        // (지문)
        var bioBtnFingerStr: String? = getResources().getString(R.string.btn_bio_login_finger)
        val fingerModule = FingerModule(this) // 지문인식 관련 모듈
        // (지문가능) : 해당 단말기의 지문인증 하드웨어 지원 여부 확인
        if (fingerModule.CF_hasFingerSensor()) {
            if (SharedPreferencesFunc.getFlagRegAuthDvsn(applicationContext, EnvConfig.AuthDvsn.getAuthDvsnByFido(Fido2Constant.AUTH_TECH_FINGER))) {
                bioBtnFingerStr += "(등록)"
                val spannable2: Spannable = SpannableString(bioBtnFingerStr)
                spannable2.setSpan(ForegroundColorSpan(Color.rgb(0, 0, 154)), 0, spannable2.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                txtBioReg2.text = spannable2
            } else {
                txtBioReg2.text = bioBtnFingerStr
            }
        } else {
            bioBtnFingerStr += "(지원불가)"
            val spannable2: Spannable = SpannableString(bioBtnFingerStr)
            spannable2.setSpan(ForegroundColorSpan(Color.rgb(154, 0, 0)), 0, spannable2.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
            txtBioReg2.text = spannable2
        }

        // (패턴)
        var bioBtnPatternStr: String? = getResources().getString(R.string.btn_bio_login_pattern)
        if (SharedPreferencesFunc.getFlagRegAuthDvsn(applicationContext, EnvConfig.AuthDvsn.getAuthDvsnByFido(Fido2Constant.AUTH_TECH_PATTERN))) {
            bioBtnPatternStr += "(등록)"
            val spannable3: Spannable = SpannableString(bioBtnPatternStr)
            spannable3.setSpan(ForegroundColorSpan(Color.rgb(0, 0, 154)), 0, spannable3.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
            txtBioReg3.text = spannable3
        } else {
            txtBioReg3.text = bioBtnPatternStr
        }

        // 체크박스 세팅
        arrCheckView[0] = linBioReg1.findViewById(R.id.checkBio)
        arrCheckView[1] = linBioReg2.findViewById(R.id.checkBio)
        arrCheckView[2] = linBioReg3.findViewById(R.id.checkBio)

        arrCheckView[0]?.CF_setContentsDesc(getResources().getString(R.string.btn_bio_login_fin) + "체크해제 버튼", getResources().getString(R.string.btn_bio_login_fin) + "체크 버튼")
        arrCheckView[1]?.CF_setContentsDesc(getResources().getString(R.string.btn_bio_login_finger) + "체크해제 버튼", getResources().getString(R.string.btn_bio_login_finger) + "체크 버튼")
        arrCheckView[2]?.CF_setContentsDesc(getResources().getString(R.string.btn_bio_login_pattern) + "체크해제 버튼", getResources().getString(R.string.btn_bio_login_pattern) + "체크 버튼")

        arrCheckView[0]?.tag = Fido2Constant.AUTH_TECH_PIN
        arrCheckView[1]?.tag = Fido2Constant.AUTH_TECH_FINGER
        arrCheckView[2]?.tag = Fido2Constant.AUTH_TECH_PATTERN

        if (!fingerModule.CF_hasFingerSensor()) {
            arrCheckView[1]?.visibility = View.GONE
        }

        // 체크된 값 제외하고 OFF
        for (i in arrCheckView.indices) {
            val index: Int = i
            arrCheckView[i]?.CE_setOnChangedCheckedStateEventListener(object : OnChangedCheckedStateEventListener {
                override fun onCheck(p_flagIsCheck: Boolean) {
                    for (j in arrCheckView.indices) {
                        if (index != j) {
                            if (arrCheckView[j]?.CF_isChecked() == true) {
                                arrCheckView[j]?.CF_setCheck(false, false, false)
                            }
                        }
                    }
                }
            })
        }

        // -- 추가인증 버튼 Visible 세팅
        linSmArsComplete = findViewById(R.id.linSmArsComplete)
        linSmArsComplete.visibility = View.GONE
        linSmArs = findViewById(R.id.linSmArs)
        linSmArs.visibility = View.VISIBLE

        // -- SMS 인증 버튼
        btnSms = findViewById(R.id.btnSmsAuth)
        btnSms.setOnClickListener { startIUPC70M00("sms") }

        // -- ARS 인증 버튼
        btnArs = findViewById(R.id.btnArsAuth)
        btnArs.setOnClickListener { startIUPC70M00("ars") }

        // -- 등록 버튼
        btnFill = findViewById(R.id.btnFill)
        btnFill.text = resources.getString(R.string.btn_bio_reg_reg)
        btnFill.setOnClickListener {
            var flagOk = false

            // 인증방법 체크값
            for (customCheckView: CustomCheckView? in arrCheckView) {
                if (customCheckView!!.CF_isChecked()) {
                    selectedAuthTechCode = customCheckView.tag as String
                }
            }

            // 인증방법 선택여부, 추가인증 여부 확인
            if (("" == selectedAuthTechCode)) { // 인증방법 선택
                arrCheckView.get(0)?.let { showCustomDialog(resources.getString(R.string.dlg_agree_choice_auth), it) }
            } else if (!isAuthComplete) { // 추가인증완료 여부
                showCustomDialog("추가인증을 진행해주십시오.", btnSms)
            } else { // OK
                flagOk = true
            }

            // --<> 유효성 검사 통과
            if (flagOk) {
                // --<> (지문인데, 단말기에 지문이 없을 경우)
                val fingerModule1 = FingerModule(applicationContext)
                if ((selectedAuthTechCode == Fido2Constant.AUTH_TECH_FINGER) && !fingerModule1.CF_hasFIngerprint()) {
                    CommonFunction.CF_showCustomAlertDilaog(this@IUFC32M00, resources.getString(R.string.dlg_not_reg_finger))
                } else {
                    startIUFC10M00_fidoReg()
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        LogPrinter.CF_debug("!----------------------------------------------------------")
        LogPrinter.CF_debug("!-- IUFC32M00.onCreate() --")
        LogPrinter.CF_debug("!----------------------------------------------------------")

        // (핀 미등록) : 핀등록부터 하라우
        if (!SharedPreferencesFunc.getFlagRegAuthDvsn(applicationContext, EnvConfig.AuthDvsn.getAuthDvsnByFido(Fido2Constant.AUTH_TECH_PIN))) {
            startIUPC34M00()
        }
    }

    override fun onStart() {
        super.onStart()
        LogPrinter.CF_debug("!----------------------------------------------------------")
        LogPrinter.CF_debug("!-- IUFC32M00.onStart() --")
        LogPrinter.CF_debug("!----------------------------------------------------------")
    }

    /**
     * 타이틀바 레이아웃 세팅
     */
    private fun setTitleBarUI() {
        LogPrinter.CF_debug("!----------------------------------------------------------")
        LogPrinter.CF_debug("!-- IUFC32M00.setTitleBarUI() --")
        LogPrinter.CF_debug("!----------------------------------------------------------")

        // -- 타이틀 세팅
        val txtTitle: TextView = findViewById(R.id.title_bar_textTitle)
        txtTitle.text = getResources().getString(R.string.btn_bio_reg)

        // -- left 버튼 세팅
        val btnLeft: ImageButton = findViewById(R.id.title_bar_imgBtnLeft)
        btnLeft.visibility = View.VISIBLE
        btnLeft.contentDescription = resources.getString(R.string.desc_cancel_reg_bio)
        btnLeft.setOnClickListener {
            val customDialog = CustomDialog(this@IUFC32M00)
            customDialog.show()
            customDialog.CF_setBackKeyMode(CustomDialog.BackMode.CANCELED)
            customDialog.CF_setTextContent(resources.getString(R.string.dlg_cancel_reg_bio))
            customDialog.CF_setDoubleButtonText(resources.getString(R.string.btn_no), resources.getString(R.string.btn_yes))
            customDialog.setOnDismissListener(object : DialogInterface.OnDismissListener {
                override fun onDismiss(dialog: DialogInterface) {
                    if (!(dialog as CustomDialog).CF_getCanceled()) {
                        setResult(RESULT_CANCELED)
                        finish()
                    }
                }
            })
        }
    }
    //    /**
    //     * 지문인식 안내 접근성 메시지 출력<br/>
    //     * 금융결제원 바이오인증앱은 접근성 지원하지 않음<br/>
    //     * 지문인증 요청 시점에 강제로 메시지 출력함
    //     */
    //    private void sendAnnounceMsgForAuthFinger() {
    //        final AccessibilityManager tmp_acceessibilityManager = (AccessibilityManager) getSystemService(ACCESSIBILITY_SERVICE);
    //        if (tmp_acceessibilityManager.isEnabled() && tmp_acceessibilityManager.isTouchExplorationEnabled()) {
    //            new Handler().postDelayed(new Runnable() {
    //                @Override
    //                public void run() {
    //                    AccessibilityEvent tmp_event = AccessibilityEvent.obtain();
    //                    tmp_event.setEventType(AccessibilityEvent.TYPE_ANNOUNCEMENT);
    //                    tmp_event.getText().add(getResources().getString(R.string.announce_auth_finger));
    //                    tmp_acceessibilityManager.sendAccessibilityEvent(tmp_event);
    //
    //                }
    //            }, 500);
    //        }
    //    }

    // #############################################################################################
    //  Activity 호출
    // #############################################################################################
    /**
     * 추가인증(SMS/ARS) Activity 호출 함수
     */
    private fun startIUPC70M00(reqDvsn: String) {
        LogPrinter.CF_debug("!----------------------------------------------------------")
        LogPrinter.CF_debug("!-- IUFC32M00.startIUPC70M00() --추가인증(SMS/ARS) Activity 호출")
        LogPrinter.CF_debug("!----------------------------------------------------------")

        val intent = Intent(this, IUPC70M00::class.java)
        intent.putExtra("reqDvsn", reqDvsn) // 추가인증 구분("sms", "ars")

        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
        startActivityForResult(intent, EnvConfig.REQUESTCODE_ACTIVITY_IUPC70M00)
    }

    /**
     * 개인인증번호 등록화면 Activity 호출
     */
    private fun startIUPC34M00() {
        LogPrinter.CF_debug("!----------------------------------------------------------")
        LogPrinter.CF_debug("!-- IUFC32M00.startIUPC34M00() --개인인증번호 등록화면 Activity 호출")
        LogPrinter.CF_debug("!----------------------------------------------------------")

        val customDialog = CustomDialog(this@IUFC32M00)
        customDialog.show()
        customDialog.CF_setBackKeyMode(CustomDialog.BackMode.CANCELED)
        customDialog.CF_setTextContent(getResources().getString(R.string.dlg_choice_reg_bio))
        customDialog.CF_setDoubleButtonText(getResources().getString(R.string.btn_no), getResources().getString(R.string.btn_yes))
        customDialog.setOnDismissListener(object : DialogInterface.OnDismissListener {
            override fun onDismiss(dialog: DialogInterface) {
                if (!(dialog as CustomDialog).CF_getCanceled()) {
                    val intent = Intent(applicationContext, IUFC34M00::class.java)
                    intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
                    startActivityForResult(intent, EnvConfig.REQUESTCODE_ACTIVITY_IUPC34M00)
                } else {
                    setResult(RESULT_CANCELED)
                    finish()
                }
            }
        })
    }

    /**
     * 간편인증 등록요청 Activity 호출 함수
     */
    private fun startIUFC10M00_fidoReg() {
        LogPrinter.CF_debug("!----------------------------------------------------------")
        LogPrinter.CF_debug("!-- IUFC32M00.startIUFC10M00_fidoReg() --간편인증 등록요청 Activity 호출")
        LogPrinter.CF_debug("!----------------------------------------------------------")

        val intent = Intent(this, IUFC10M00::class.java)
        intent.putExtra(EnvConstant.KEY_INTENT_AUTH_TECH_CODE, selectedAuthTechCode) // 인증기술코드
        intent.putExtra(EnvConstant.KEY_INTENT_AUTH_CSNO     , SharedPreferencesFunc.getLoginCsno(applicationContext)) // 고객번호

        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
        startActivityForResult(intent, EnvConfig.REQUESTCODE_ACTIVITY_IUFC10M00)
    }

    /**
     * 간편인증 등록완료 Activity 호출 함수
     */
    private fun startIUPC39M00() {
        LogPrinter.CF_debug("!----------------------------------------------------------")
        LogPrinter.CF_debug("!-- IUFC32M00.startIUPC39M00() --간편인증 등록완료 Activity 호출")
        LogPrinter.CF_debug("!----------------------------------------------------------")

        val intent = Intent(this, IUFC39M00::class.java)
        intent.putExtra(EnvConstant.KEY_INTENT_AUTH_TECH_CODE, selectedAuthTechCode) // 인증기술코드
        intent.putExtra("authResultDvsn", 0) // 요청구분(0:등록/변경, 1:해지)

        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
        startActivityForResult(intent, EnvConfig.REQUESTCODE_ACTIVITY_IUPC39M00)
    }
}