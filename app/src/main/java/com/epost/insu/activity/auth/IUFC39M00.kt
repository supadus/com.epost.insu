package com.epost.insu.activity.auth

import android.view.View
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import com.epost.insu.EnvConstant
import com.epost.insu.R
import com.epost.insu.activity.Activity_Default
import java.util.*

/**
 * 인증센터 > 간편인증 등록/변경 or 간편인증 조회/해지 > 완료화면
 * @since     : project 48:1.4.5
 * @version   : 1.1
 * @author    : NJM
 * @see
 * <pre>
 * ======================================================================
 * 1.4.5    NJM_20201229    최초 등록
 * 1.5.9    NJM_20210701    [PIN등록 오류 수정] 최초설치시 PIN등록시 csno 누락 오류 수정
 * 1.6.2    NJM_20210729    [간편인증 플래그 반영] 간편인증 로그인시 flag값 참조 변경 (서버저장 -> 단말저장)
 * =======================================================================
 * copyright : (주)프로니스정보기술 | http://www.phronis.co.kr
 * </pre>
 */
class IUFC39M00 : Activity_Default() {
    private var authTechCode: String? = null
    private var authResultDvsn: Int = 0

    override fun setInit() {
        setContentView(R.layout.iufc39m00)

        // 인증기술코드
        if (intent.hasExtra(EnvConstant.KEY_INTENT_AUTH_TECH_CODE)) {
            authTechCode = intent.getSerializableExtra(EnvConstant.KEY_INTENT_AUTH_TECH_CODE) as String?
        }

        // 요청구분 (0:등록/변경, 1:해지)
        if (intent.hasExtra("authResultDvsn")) {
            authResultDvsn = intent.getSerializableExtra("authResultDvsn") as Int
        }
    }

    override fun setUIControl() {

        // 타이틀바 레이아웃 세팅
        setTitleBarUI()
        val calendar: Calendar = Calendar.getInstance()
        val year: Int = calendar.get(Calendar.YEAR)
        val month: Int = calendar.get(Calendar.MONTH) + 1
        val dayOfMonth: Int = calendar.get(Calendar.DAY_OF_MONTH)
        val textDate: TextView = findViewById(R.id.textRegDate)

        // 등록/변경
        var preMsg = "("
        if (authResultDvsn == 0) {
            preMsg = "(등록일 : "
        } else if (authResultDvsn == 1) {
            preMsg = "(해지일 : "
        }
        textDate.text = preMsg + year + "." + String.format(Locale.getDefault(), "%02d", month) + "." + String.format(Locale.getDefault(), "%02d", dayOfMonth) + ")"
        val btnOk: Button = findViewById(R.id.btnFill)
        btnOk.text = resources.getString(R.string.btn_ok)
        btnOk.setOnClickListener {
            setResult(RESULT_OK)
            finish()
        }
    }

    /**
     * 타이틀바 레이아웃 세팅
     */
    private fun setTitleBarUI() {
        var tmpTitle: String? = ""
        var tmpMsg: String? = ""

        // 등록/변경
        if (authResultDvsn == 0) {
            tmpTitle = getResources().getString(R.string.title_complete_reg_bio)
            tmpMsg = getResources().getString(R.string.guide_reg_ok_bio)
        } else if (authResultDvsn == 1) {
            tmpTitle = getResources().getString(R.string.title_complete_del_bio)
            tmpMsg = getResources().getString(R.string.guide_del_ok_bio)
        }

        // 타이틀 세팅
        val txtTitle: TextView = findViewById(R.id.title_bar_textTitle)
        txtTitle.text = tmpTitle

        // 텍스트 세팅
        val txtMsg: TextView = findViewById(R.id.resultMsg)
        txtMsg.text = tmpMsg

        // left 버튼 세팅
        val btnLeft: ImageButton = findViewById(R.id.title_bar_imgBtnLeft)
        btnLeft.visibility = View.VISIBLE
        btnLeft.setOnClickListener { finish() }
    }
}