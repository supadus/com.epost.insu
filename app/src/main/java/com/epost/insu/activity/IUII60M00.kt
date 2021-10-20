package com.epost.insu.activity

import android.graphics.Color
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.view.View
import android.widget.ImageButton
import android.widget.TextView
import com.epost.insu.R
import com.epost.insu.common.UiUtil

/**
 * @copyright : (주)프로니스정보기술 | http://www.phronis.co.kr
 *
 * @project   : 모바일슈랑스 구축
 * @pakage   : com.epost.insu.activity
 * @fileName  : IUII60M00.java
 *
 * @Title     : 보험금청구 > 보험금청구선택 > 보험금청구절차 (화면 ID : IUII60M00) - #27
 * @author    : 이수행
 * @created   : 2017-08-16
 * @version   : 1.0
 *
 * @note      : <u>보험금청구 > 보험금청구 > 보험금청구절차 (화면 ID : IUII60M00) - #27</u><br></br>
 * 보험금청구절차 안내 화면<br></br>
 * ======================================================================
 * 수정 내역
 * NO      날짜          작업자       내용
 * 01      2017-08-16    이수행       최초 등록
 * =======================================================================
 */
class IUII60M00 : Activity_Default() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setIntentData()
    }

    override fun setInit() {
        setContentView(R.layout.iuii60m00)
    }

    override fun setUIControl() {

        // 타이틀바 세팅
        setTitleBarUI()
    }

    /**
     * 타이틀바 레이아웃 세팅
     */
    private fun setTitleBarUI() {

        // 타이틀 세팅
        val tmp_title = findViewById<View>(R.id.title_bar_textTitle) as TextView
        tmp_title.text = resources.getString(R.string.title_guide_req)


        val title_main = findViewById<View>(R.id.title_main) as TextView

        UiUtil.setTitleColor(title_main,resources.getString(R.string.label_iuii60m00_title),6,11)


        // left 버튼 세팅
        val tmp_btnLeft = findViewById<View>(R.id.title_bar_imgBtnLeft) as ImageButton
        tmp_btnLeft.visibility = View.VISIBLE
        tmp_btnLeft.setOnClickListener { finish() }
    }

    /**
     * Intent 데이터 세팅 함수
     */
    private fun setIntentData() {
        if (intent != null) {
            if (intent.hasExtra("flagUseCloseBtn")) {
                val tmp_flagUseCloseBtn = intent.extras!!.getBoolean("flagUseCloseBtn")
                if (tmp_flagUseCloseBtn) {
                    val tmp_btnLeft = findViewById<View>(R.id.title_bar_imgBtnLeft) as ImageButton
                    tmp_btnLeft.contentDescription = resources.getString(R.string.desc_close)
                    tmp_btnLeft.setImageResource(R.drawable.ic_close_3)
                }
            }
        }
    }
}