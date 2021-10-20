package com.epost.insu.activity.auth

import android.app.Activity
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
import com.epost.insu.fido.Fido2Callback
import com.epost.insu.fido.Fido2Cancelation
import com.epost.insu.fido.Fido2Constant
import com.epost.insu.module.FingerModule

/**
 * 인증센터 > 간편인증 조회/해지
 * @since     : project 48:1.4.5
 * @version   : 1.1
 * @author    : NJM
 * @see
 * <pre>
 * ======================================================================
 * 1.4.5    NJM_20201229    최초 등록
 * 1.5.2    NJM_20210322    [FIDO인증 로직 변경]
 * 1.6.2    NJM_20210729    [간편인증 플래그 반영] 간편인증 로그인시 flag값 참조 변경 (서버저장 -> 단말저장)
 * =======================================================================
 * copyright : (주)프로니스정보기술 | http://www.phronis.co.kr
 * </pre>
 */
class IUFC33M00 : Activity_Default() {
    private val activity: Activity = this
    private var selectedAuthTechCode: String = "" // 선택된 인증기술코드
    private lateinit var btnFill: Button
    private lateinit var arrCheckView: Array<CustomCheckView?> // 동의 체크View Array

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        // -- 간편인증 등록/해지 완료 Activity 종료
        if (requestCode == EnvConfig.REQUESTCODE_ACTIVITY_IUPC39M00) {
            if (resultCode == RESULT_OK) {
                setResult(RESULT_OK)
                finish()
            } else {
                setResult(RESULT_CANCELED)
                finish()
            }
        }
        // -- 개인인증번호 재등록 Activity 종료
        if (requestCode == EnvConfig.REQUESTCODE_ACTIVITY_IUPC34M00) {
            if (resultCode == RESULT_OK) {
                setResult(RESULT_OK)
                finish()
            } else {
                setResult(RESULT_CANCELED)
                finish()
            }
        }
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
    }

    override fun onBackPressed() {
        val tmp_dlg = CustomDialog(this@IUFC33M00)
        tmp_dlg.show()
        tmp_dlg.CF_setBackKeyMode(CustomDialog.BackMode.CANCELED)
        tmp_dlg.CF_setTextContent(getResources().getString(R.string.dlg_cancel_del_bio))
        tmp_dlg.CF_setDoubleButtonText(getResources().getString(R.string.btn_no), getResources().getString(R.string.btn_yes))
        tmp_dlg.setOnDismissListener { dialog ->
            if (!(dialog as CustomDialog).CF_getCanceled()) {
                setResult(RESULT_CANCELED)
                finish()
            }
        }
    }

    override fun setInit() {
        setContentView(R.layout.iufc33m00)
        arrCheckView = arrayOfNulls(3)
    }

    override fun setUIControl() {
        // -- 타이틀 세팅 함수
        setTitleBarUI()
        val linbioReg1: LinearLayout = findViewById(R.id.linBioReg_1)
        val linbioReg2: LinearLayout = findViewById(R.id.linBioReg_2)
        val linbioReg3: LinearLayout = findViewById(R.id.linBioReg_3)

        // Image 세팅
        (linbioReg1.findViewById<View>(R.id.imageView) as ImageView).setImageResource(R.drawable.ic_pin)
        (linbioReg2.findViewById<View>(R.id.imageView) as ImageView).setImageResource(R.drawable.ic_finger)
        (linbioReg3.findViewById<View>(R.id.imageView) as ImageView).setImageResource(R.drawable.ic_pattern)

        // Text 세팅
        val textLinBioReg1: TextView = linbioReg1.findViewById(R.id.text)
        val textLinBioReg2: TextView = linbioReg2.findViewById(R.id.text)
        val textLinBioReg3: TextView = linbioReg3.findViewById(R.id.text)

        // (PIN)
        var btnBioPinLogin: String? = getResources().getString(R.string.btn_bio_login_fin)
        //if (SharedPreferencesFunc.getFlag_RegFido(applicationContext, Fido2Constant.AUTH_TECH_PIN)) {
        if (SharedPreferencesFunc.getFlagRegAuthDvsn(applicationContext, EnvConfig.AuthDvsn.getAuthDvsnByFido(Fido2Constant.AUTH_TECH_PIN))) {
            btnBioPinLogin += "(해지불가)"
            val tmp_spannable_1: Spannable = SpannableString(btnBioPinLogin)
            tmp_spannable_1.setSpan(ForegroundColorSpan(Color.rgb(0, 0, 154)), 0, tmp_spannable_1.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
            textLinBioReg1.text = tmp_spannable_1
        } else {
            textLinBioReg1.text = btnBioPinLogin
        }

        // (지문)
        var btnBioFingerLogin: String? = getResources().getString(R.string.btn_bio_login_finger)
        val fingerModule = FingerModule(this) // 지문인식 관련 모듈
        // (지문가능) : 해당 단말기의 지문인증 하드웨어 지원 여부 확인
        if (fingerModule.CF_hasFingerSensor()) {
            //if (SharedPreferencesFunc.getFlag_RegFido(applicationContext, Fido2Constant.AUTH_TECH_FINGER)) {
            if (SharedPreferencesFunc.getFlagRegAuthDvsn(applicationContext, EnvConfig.AuthDvsn.getAuthDvsnByFido(Fido2Constant.AUTH_TECH_FINGER))) {
                btnBioFingerLogin += "(등록)"
                val tmp_spannable_2: Spannable = SpannableString(btnBioFingerLogin)
                tmp_spannable_2.setSpan(ForegroundColorSpan(Color.rgb(0, 0, 154)), 0, tmp_spannable_2.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                textLinBioReg2.text = tmp_spannable_2
            } else {
                textLinBioReg2.text = btnBioFingerLogin
            }
        } else {
            btnBioFingerLogin += "(지원불가)"
            val tmp_spannable_2: Spannable = SpannableString(btnBioFingerLogin)
            tmp_spannable_2.setSpan(ForegroundColorSpan(Color.rgb(154, 0, 0)), 0, tmp_spannable_2.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
            textLinBioReg2.text = tmp_spannable_2
        }

        // (패턴)
        var btnBioPatternLogin: String? = getResources().getString(R.string.btn_bio_login_pattern)
        //if (SharedPreferencesFunc.getFlag_RegFido(applicationContext, Fido2Constant.AUTH_TECH_PATTERN)) {
        if (SharedPreferencesFunc.getFlagRegAuthDvsn(applicationContext, EnvConfig.AuthDvsn.getAuthDvsnByFido(Fido2Constant.AUTH_TECH_PATTERN))) {
            btnBioPatternLogin += "(등록)"
            val tmp_spannable_3: Spannable = SpannableString(btnBioPatternLogin)
            tmp_spannable_3.setSpan(ForegroundColorSpan(Color.rgb(0, 0, 154)), 0, tmp_spannable_3.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
            textLinBioReg3.text = tmp_spannable_3
        } else {
            textLinBioReg3.text = btnBioPatternLogin
        }
        //
//        Spannable tmp_spannable_1 = new SpannableString(getResources().getString(R.string.btn_bio_login_fin));
//        //tmp_spannable_1.setSpan(new ForegroundColorSpan(Color.rgb(254,0,0)), tmp_spannable_1.length()- 4,tmp_spannable_1.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
//        tmp_text_1.setText(tmp_spannable_1);
//
//        Spannable tmp_spannable_2 = new SpannableString(getResources().getString(R.string.btn_bio_login_finger));
//        //tmp_spannable_2.setSpan(new ForegroundColorSpan(Color.rgb(254,0,0)), tmp_spannable_2.length()- 4,tmp_spannable_2.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
//        tmp_text_2.setText(tmp_spannable_2);
//
//        Spannable tmp_spannable_3 = new SpannableString(getResources().getString(R.string.btn_bio_login_pattern));
//        //tmp_spannable_3.setSpan(new ForegroundColorSpan(Color.rgb(254,0,0)), tmp_spannable_3.length()- 4,tmp_spannable_3.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
//        tmp_text_3.setText(tmp_spannable_3);

        // 체크박스 세팅
        arrCheckView[0] = linbioReg1.findViewById(R.id.checkBio)
        arrCheckView[1] = linbioReg2.findViewById(R.id.checkBio)
        arrCheckView[2] = linbioReg3.findViewById(R.id.checkBio)

        arrCheckView[0]?.CF_setContentsDesc(getResources().getString(R.string.btn_bio_login_fin) + "체크해제 버튼", getResources().getString(R.string.btn_bio_login_fin) + "체크 버튼")
        arrCheckView[1]?.CF_setContentsDesc(getResources().getString(R.string.btn_bio_login_finger) + "체크해제 버튼", getResources().getString(R.string.btn_bio_login_finger) + "체크 버튼")
        arrCheckView[2]?.CF_setContentsDesc(getResources().getString(R.string.btn_bio_login_pattern) + "체크해제 버튼", getResources().getString(R.string.btn_bio_login_pattern) + "체크 버튼")

        arrCheckView[0]?.tag = Fido2Constant.AUTH_TECH_PIN
        arrCheckView[1]?.tag = Fido2Constant.AUTH_TECH_FINGER
        arrCheckView[2]?.tag = Fido2Constant.AUTH_TECH_PATTERN

        // -- 수정 불가처리
        arrCheckView[0]?.visibility = View.GONE
        // (지문모듈 없을때)
        if (!fingerModule.CF_hasFingerSensor()) {
            arrCheckView[1]?.visibility = View.GONE
        }

        // 체크된 값 제외하고 OFF
        for (i in arrCheckView.indices) {
            val index: Int = i
            arrCheckView.get(i)?.CE_setOnChangedCheckedStateEventListener {
                for (j in arrCheckView.indices) {
                    if (index != j) {
                        if (arrCheckView[j]?.CF_isChecked() == true) {
                            arrCheckView[j]?.CF_setCheck(false, false, false)
                        }
                    }
                }
            }
        }

        // -- [해지] 버튼
        btnFill = findViewById(R.id.btnFill)
        btnFill.text = resources.getString(R.string.btn_bio_reg_del)
        btnFill.setOnClickListener {
            // 인증방법 체크값
            for (customCheckView: CustomCheckView? in arrCheckView) {
                if (customCheckView!!.CF_isChecked()) {
                    selectedAuthTechCode = customCheckView.tag as String
                }
            }

            // 인증방법, 추가인증 여부 확인
            if (("" == selectedAuthTechCode)) {   // 인증방법 선택
                arrCheckView.get(0)?.let { showCustomDialog(getResources().getString(R.string.dlg_agree_choice_auth), it) }
            } else {
                CF_showProgressDialog()
                Fido2Cancelation(applicationContext, activity, selectedAuthTechCode, object : Fido2Callback {
                    override fun onReceiveMessage(code: String, bundle: Bundle) {
                        CF_dismissProgressDialog()
                        LogPrinter.CF_debug("!----[최종완료] (code) : $code / (bundle) : $bundle")

                        SharedPreferencesFunc.setFlagRegAuthDvsn(applicationContext, false, EnvConfig.AuthDvsn.getAuthDvsnByFido(selectedAuthTechCode)) // -- 인증 등록 여부
                        startIUPC39M00() // -- 완료화면
                    }

                    override fun onFailure(code: String, msg: String) {
                        LogPrinter.CF_debug("!----[최종에러] (code) : $code / (msg) : $msg")
                        CF_dismissProgressDialog()
                        CommonFunction.CF_showCustomAlertDilaog(activity, msg, getResources().getString(R.string.btn_ok))
                    }
                }).process()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        LogPrinter.CF_debug("!----------------------------------------------------------")
        LogPrinter.CF_debug("!-- IUFC34M00.onCreate() --")
        LogPrinter.CF_debug("!----------------------------------------------------------")

        // (PIN 미등록) : PIN등록부터
        //if (!SharedPreferencesFunc.getFlag_RegFido(applicationContext, Fido2Constant.AUTH_TECH_PIN)) {
        if (!SharedPreferencesFunc.getFlagRegAuthDvsn(applicationContext, EnvConfig.AuthDvsn.getAuthDvsnByFido(Fido2Constant.AUTH_TECH_PIN))) {
            startIUPC34M00()
        }
    }

    /**
     * 타이틀바 레이아웃 세팅
     */
    private fun setTitleBarUI() {
        // -- 타이틀 세팅
        val txvTitle: TextView = findViewById(R.id.title_bar_textTitle)
        txvTitle.text = resources.getString(R.string.title_del_bio)

        // -- left 버튼 세팅
        val btnLeft: ImageButton = findViewById(R.id.title_bar_imgBtnLeft)
        btnLeft.visibility = View.VISIBLE
        btnLeft.contentDescription = resources.getString(R.string.desc_cancel_del_bio)
        btnLeft.setOnClickListener {
            val customDialog = CustomDialog(this@IUFC33M00)
            customDialog.show()
            customDialog.CF_setBackKeyMode(CustomDialog.BackMode.CANCELED)
            customDialog.CF_setTextContent(resources.getString(R.string.dlg_cancel_del_bio))
            customDialog.CF_setDoubleButtonText(resources.getString(R.string.btn_no), resources.getString(R.string.btn_yes))
            customDialog.setOnDismissListener { dialog ->
                if (!(dialog as CustomDialog).CF_getCanceled()) {
                    setResult(RESULT_CANCELED)
                    finish()
                }
            }
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
    /**
     * 간편인증 등록/해지 완료 Activity 호출 함수
     */
    private fun startIUPC39M00() {
        val intent = Intent(this, IUFC39M00::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
        intent.putExtra(EnvConstant.KEY_INTENT_AUTH_TECH_CODE, selectedAuthTechCode) // 인증기술코드
        intent.putExtra("authResultDvsn", 1) // 요청구분(0:등록/변경, 1:해지)
        startActivityForResult(intent, EnvConfig.REQUESTCODE_ACTIVITY_IUPC39M00)
    }

    /**
     * 핀(개인인증번호) 등록 호출
     */
    private fun startIUPC34M00() {
        val customDialog = CustomDialog(this@IUFC33M00)
        customDialog.show()
        customDialog.CF_setBackKeyMode(CustomDialog.BackMode.CANCELED)
        customDialog.CF_setTextContent(resources.getString(R.string.dlg_choice_reg_bio))
        customDialog.CF_setDoubleButtonText(resources.getString(R.string.btn_no), resources.getString(R.string.btn_yes))
        customDialog.setOnDismissListener { dialog ->
            if (!(dialog as CustomDialog).CF_getCanceled()) {
                val intent = Intent(applicationContext, IUFC34M00::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
                startActivityForResult(intent, EnvConfig.REQUESTCODE_ACTIVITY_IUPC34M00)
            } else {
                setResult(RESULT_CANCELED)
                finish()
            }
        }
    }
}