package com.epost.insu.activity.auth

import android.view.View
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import com.epost.insu.EnvConfig.AuthDvsn
import com.epost.insu.IntentManager
import com.epost.insu.R
import com.epost.insu.activity.Activity_Default

/**
 * 인증 > 간편인증(핀/지문/패턴) > 인증완료
 * @since     : project 48:1.4.5
 * @version   : 1.1
 * @author    : NJM
 * <pre>
 * ======================================================================
 * 1.4.5    NJM_20201229    최초 등록
 * =======================================================================
 * copyright : (주)프로니스정보기술 | http://www.phronis.co.kr
</pre> *
 */
class IUFC00M09 : Activity_Default() {
    private var authDvsn: AuthDvsn? = null

    override fun setInit() {
        setContentView(R.layout.iufc00m01)
        if (getIntent().hasExtra("authDvsn")) {
            authDvsn = getIntent().getSerializableExtra("authDvsn") as AuthDvsn?
        }
    }

    override fun setUIControl() {
        // 타이틀바 세팅
        setTitleBarUI()
        val tmp_img: ImageView = findViewById(R.id.imageView)
        val tmp_Text: TextView = findViewById(R.id.textView)
        when (authDvsn) {
            AuthDvsn.PIN -> {
                tmp_img.setImageResource(R.drawable.ic_complete_pin)
                tmp_img.setContentDescription(getResources().getString(R.string.desc_img_auth_pin_c))
                tmp_Text.setText(getResources().getString(R.string.guide_auth_pin))
            }
            AuthDvsn.PINGER -> {
                tmp_img.setImageResource(R.drawable.ic_complete_finger)
                tmp_img.setContentDescription(getResources().getString(R.string.desc_img_auth_finger_c))
                tmp_Text.setText(getResources().getString(R.string.guide_auth_finger))
            }
            AuthDvsn.PATTERN -> {
                tmp_img.setImageResource(R.drawable.ic_pattern)
                tmp_img.setContentDescription(getResources().getString(R.string.desc_img_auth_pattern_c))
                tmp_Text.setText(getResources().getString(R.string.guide_auth_pattern))
            }
            AuthDvsn.KAKAOPAY -> {
                tmp_img.setImageResource(R.drawable.ic_kakaopay_auth)
                tmp_img.setContentDescription(getResources().getString(R.string.desc_img_auth_kakao_c))
                tmp_Text.setText(getResources().getString(R.string.guide_auth_kakao))
            }
            else -> {
                tmp_img.setImageResource(R.drawable.ic_complete_cert)
                tmp_img.setContentDescription(getResources().getString(R.string.desc_img_auth_common_c))
                tmp_Text.setText(getResources().getString(R.string.guide_auth_common))
            }
        }
        val tmp_btn: Button = findViewById(R.id.btnFill)
        tmp_btn.setText(getResources().getString(R.string.btn_ok))
        tmp_btn.setOnClickListener(object : View.OnClickListener {
            override fun onClick(v: View) {
                //finish()
                IntentManager.moveTaskToBack(this@IUFC00M09,true)
            }
        })
    }

    override fun onStop() {
        super.onStop()
        finish()
    }

    /**
     * 타이틀바 레이아웃 세팅
     */
    private fun setTitleBarUI() {
        // 타이틀 세팅
        val tmp_title: TextView = findViewById(R.id.title_bar_textTitle)
        tmp_title.setText(getResources().getString(R.string.title_complete_pin))

        // left 버튼 세팅
        val tmp_btnLeft: ImageButton = findViewById(R.id.title_bar_imgBtnLeft)
        tmp_btnLeft.setVisibility(View.VISIBLE)
        tmp_btnLeft.setOnClickListener(object : View.OnClickListener {
            override fun onClick(v: View) {
                finish()
            }
        })
    }
}