package com.epost.insu.activity.BC

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import com.epost.insu.EnvConfig
import com.epost.insu.EnvConstant
import com.epost.insu.R
import com.epost.insu.activity.Activity_Default
import com.epost.insu.activity.auth.IUCOC0M00
import com.epost.insu.activity.auth.IUFC00M00
import com.epost.insu.common.CustomSQLiteHelper
import com.epost.insu.fido.Fido2Constant
import com.epost.insu.module.FingerModule

/**
 * @copyright : 우정사업정보센터
 *
 * @project   : 블록체인 간편인증 개발
 * @package   : com.epost.insu.activity
 * @fileName  : IUBC01M00.java
 *
 * @Title     : 스마트보험금청구 신청 > 로그인방법선택 (화면 ID : IUBC01M00)
 * @author    : 이경민
 * @created   : 2020-03-02
 * @version   : 1.0
 *
 * @note      : <u>스마트보험금청구 신청 > 로그인방법선택 (화면 ID : IUBC01M00)</u><br></br>
 * 로그인 방법 선택 화면(공동인증, 지문인증, PIN인증, 블록체인 간편인증 중 택 1)<br></br>
 * ======================================================================
 * 수정 내역
 * NO      날짜          작업자       내용
 * 01      2018-08-04    이경민       최초 등록
 * 02      2020-03-02    이경민       블록체인 간편인증 추가
 * 03      2020-04-28    이경민       액티비티 결과값 처리 코드 추가
 * =======================================================================
 */
class IUBC01M00 : Activity_Default(), View.OnClickListener {
    private val key_smartReqPay = "isSmartReqPay"
    private var fingerModule // 지문인식 관련 모듈
            : FingerModule? = null

    override fun onClick(p_view: View) {
        if (p_view.id == R.id.btnSmartReqLoginCertificate) {   // 공동인증 로그인
            startIUCOC0M00()
        } else if (p_view.id == R.id.btnSmartReqLoginFinger) {   // 지문인증 로그인
            checkFinger(p_view)
            startIUFC00M00_fido(Fido2Constant.AUTH_TECH_FINGER, p_view)
        } else if (p_view.id == R.id.btnSmartReqLoginPin) {      // PIN번호 로그인
            startIUFC00M00_fido(Fido2Constant.AUTH_TECH_PIN, p_view)
        }
        //        else if (p_view.getId() == R.id.btnSmartReqLoginBlockchain) { // 블록체인 로그인
//            startIUBC02M00(p_view);
//        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun setInit() {
        setContentView(R.layout.iubc01m00)
    }

    override fun setUIControl() {
        // 타이틀바 세팅
        setTitleBarUI()

        // 버튼 세팅
        setBtnUI()
    }

    override fun onSaveInstanceState(p_bundle: Bundle) {
        super.onSaveInstanceState(p_bundle)
    }

    override fun onRestoreInstanceState(p_bundle: Bundle) {
        super.onRestoreInstanceState(p_bundle)
    }

    override fun onActivityResult(p_requestCode: Int, p_resultCode: Int, p_data: Intent?) {
        super.onActivityResult(p_requestCode, p_resultCode, p_data)
        if (p_requestCode == EnvConfig.REQUESTCODE_ACTIVITY_IUBC02M00 && p_resultCode == RESULT_OK) {
            setResult(RESULT_OK)
            finish()
        }
        if (p_requestCode == EnvConfig.REQUESTCODE_ACTIVITY_IUCOC0M00 && p_resultCode == RESULT_OK) {
            setResult(RESULT_OK)
            finish()
        }
        if (p_requestCode == EnvConfig.REQUESTCODE_ACTIVITY_IUFC00M00 && p_resultCode == RESULT_OK) {
            setResult(RESULT_OK)
            finish()
        }
    }

    /**
     * 타이틀바 UI 세팅 함수
     */
    private fun setTitleBarUI() {
        // 타이틀 세팅
        val tmp_title = findViewById<TextView>(R.id.title_bar_textTitle)
        tmp_title.text = resources.getString(R.string.title_login)

        // left 버튼 세팅
        val tmp_btnLeft = findViewById<ImageButton>(R.id.title_bar_imgBtnLeft)
        tmp_btnLeft.visibility = View.VISIBLE
        tmp_btnLeft.setOnClickListener { finish() }
    }

    /**
     * 로그인 버튼 UI 세팅 함수
     */
    private fun setBtnUI() {
        val tmp_btnLogin_c: View
        val tmp_btnLogin_f: View
        val tmp_btnLogin_p: View
        val tmp_btnLogin_b: View

        // -- 공동인증 로그인 버튼 VIew
        tmp_btnLogin_c = findViewById(R.id.btnSmartReqLoginCertificate)
        (tmp_btnLogin_c.findViewById<View>(R.id.imageView) as ImageView).setImageResource(R.drawable.ic_certificate_for_login)
        (tmp_btnLogin_c.findViewById<View>(R.id.textView) as TextView).text = resources.getString(R.string.btn_login_certificate)
        tmp_btnLogin_c.findViewById<View>(R.id.textView).contentDescription = resources.getString(R.string.btn_login_certificate) + " 버튼"

        // -- 지문인증 로그인 버튼 View
        tmp_btnLogin_f = findViewById(R.id.btnSmartReqLoginFinger)
        (tmp_btnLogin_f.findViewById<View>(R.id.imageView) as ImageView).setImageResource(R.drawable.ic_finger)
        (tmp_btnLogin_f.findViewById<View>(R.id.textView) as TextView).text = resources.getString(R.string.btn_login_finger)
        tmp_btnLogin_f.findViewById<View>(R.id.textView).contentDescription = resources.getString(R.string.btn_login_finger) + " 버튼"

        // -- 해당 단말기의 지문인증 하드웨어 지원 여부 확인
        fingerModule = FingerModule(this)
        if (!fingerModule!!.CF_hasFingerSensor()) {
            tmp_btnLogin_f.isEnabled = false
            val tmp_textSub = tmp_btnLogin_f.findViewById<TextView>(R.id.textViewSub)
            tmp_textSub.text = resources.getString(R.string.label_no_sensor_finger)
            tmp_textSub.visibility = View.VISIBLE
            tmp_textSub.isSelected = true
        }

        // -- 핀 번호 로그인 버튼 View
        tmp_btnLogin_p = findViewById(R.id.btnSmartReqLoginPin)
        (tmp_btnLogin_p.findViewById<View>(R.id.imageView) as ImageView).setImageResource(R.drawable.ic_pin)
        (tmp_btnLogin_p.findViewById<View>(R.id.textView) as TextView).text = resources.getString(R.string.btn_login_pin)
        tmp_btnLogin_p.findViewById<View>(R.id.textView).contentDescription = resources.getString(R.string.btn_login_pin) + " 버튼"
        val sdk = Build.VERSION.SDK_INT
        if (sdk < Build.VERSION_CODES.M) {
            tmp_btnLogin_p.isEnabled = false
            val tmp_textSub = tmp_btnLogin_p.findViewById<TextView>(R.id.textViewSub)
            tmp_textSub.text = resources.getString(R.string.label_no_sensor_pin)
            tmp_textSub.visibility = View.VISIBLE
            tmp_textSub.isSelected = true
        }

        // -- 블록체인 로그인 버튼 View
        tmp_btnLogin_b = findViewById(R.id.btnSmartReqLoginBlockchain)
        (tmp_btnLogin_b.findViewById<View>(R.id.imageView) as ImageView).setImageResource(R.drawable.ic_blockchain)
        (tmp_btnLogin_b.findViewById<View>(R.id.textView) as TextView).text = resources.getString(R.string.btn_login_blockchain)
        tmp_btnLogin_b.findViewById<View>(R.id.textView).contentDescription = resources.getString(R.string.btn_login_blockchain) + " 버튼"

        // -- 리스너
        tmp_btnLogin_c.setOnClickListener(this)
        tmp_btnLogin_f.setOnClickListener(this)
        tmp_btnLogin_p.setOnClickListener(this)
        tmp_btnLogin_b.setOnClickListener(this)
    }

    /**
     * 단말기 DB에 저장되어 있는 csno 정보를 가져와
     * 고객 식별번호(csno) 보유 여부 검사
     * @return
     */
    private fun hasUsercsno(): Boolean {
        var tmp_flagHas = false
        var tmp_csno = ""
        val tmp_helper = CustomSQLiteHelper(applicationContext)
        val tmp_sqlite = tmp_helper.readableDatabase
        tmp_csno = tmp_helper.CF_Selectcsno(tmp_sqlite)
        tmp_helper.close()
        tmp_sqlite.close()
        if (!TextUtils.isEmpty(tmp_csno)) {
            tmp_flagHas = true
        }
        return tmp_flagHas
    }
    // #############################################################################################
    //  Activity 호출
    // #############################################################################################
    /**
     * 공동인증 로그인 Activity 호출 함수
     */
    private fun startIUCOC0M00() {
        val tmp_intent = Intent(this, IUCOC0M00::class.java)
        tmp_intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
        tmp_intent.putExtra(key_smartReqPay, true)
        startActivityForResult(tmp_intent, EnvConfig.REQUESTCODE_ACTIVITY_IUCOC0M00)
    }

    /**
     * 지문 등록 유무 체크 호출 함수<br></br>
     */
    private fun checkFinger(p_view: View) {
        if (fingerModule!!.CF_hasFIngerprint()) {      // 단말기에 등록된 지문이 있는지 검사
            startIUFC00M00_fido(Fido2Constant.AUTH_TECH_FINGER, p_view)
        } else {
            showCustomDialog(resources.getString(R.string.dlg_not_reg_finger), p_view)
        }
    }




    /**
     * FIDO 로그인 Activity 호출 함수<br></br>
     */
    private fun startIUFC00M00_fido(auhTechCode: String, p_view: View) {
        if (hasUsercsno()) {
            val tmp_intent = Intent(this, IUFC00M00::class.java)
            tmp_intent.putExtra(EnvConstant.KEY_INTENT_AUTH_MODE, EnvConfig.AuthMode.LOGIN_APP)
            tmp_intent.putExtra(EnvConstant.KEY_INTENT_AUTH_TECH_CODE, auhTechCode)
            tmp_intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
            startActivityForResult(tmp_intent, EnvConfig.REQUESTCODE_ACTIVITY_IUFC00M00)
        } else {
            showCustomDialog(resources.getString(R.string.dlg_empty_csno), p_view)
        }
    } //    /**
    //     * 블록체인 로그인 Activity 호출 함수<br/>
    //     */
    //    private void startIUBC02M00(View p_view) {
    //        Intent tmp_intent = new Intent(this, IUBC02M00.class);
    //        tmp_intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
    //        startActivityForResult(tmp_intent, EnvConfig.REQUESTCODE_ACTIVITY_IUBC02M00);
    //    }
}