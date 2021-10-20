package com.epost.insu.activity

import android.view.View
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import com.epost.insu.R
import com.epost.insu.common.UiUtil

/**
 * @copyright : (주)프로니스정보기술 | http://www.phronis.co.kr
 *
 * @project   : 모바일슈랑스 구축
 * @pakage    : com.epost.insu.activity
 * @fileName  : IUII21S01.java
 *
 * @Title     : 보험금청구 > (구비서류첨부) > 사진촬영 시 주의사항 (화면 ID : iuii21s01)
 * @author    : 이수행
 * @created   : 2017-08-22
 * @version   : 1.0
 *
 * @note      : <u>보험금청구 > 사진촬영 시 주의사항 (화면 ID : IUII21S01) - #17</u><br></br>
 * 지급청구구비서류 첨부 촬영 버튼 클릭시 최초 1회 보이는 Activity(매 청구시 최초 1회만 보임)<br></br>
 * 사진촬영하기 버튼 클릭시 RESULT_OK를 설정하고 Activity를 종료한다.<br></br>
 * ======================================================================
 * 수정 내역
 * NO      날짜          작업자       내용
 * 01      2017-08-22    이수행       최초 등록
 * =======================================================================
 */
class IUII21S01 : Activity_Default() {
    override fun setInit() {
        setContentView(R.layout.iuii21s01)
    }

    override fun setUIControl() {
        // -- 타이틀바 세팅
        setTitleBarUI()
        val tmp_btnCamera = findViewById<Button>(R.id.btnFill)
        tmp_btnCamera.text = resources.getString(R.string.btn_take_picture_2)
        tmp_btnCamera.setOnClickListener {
            setResult(RESULT_OK)
            finish()
        }
    }

    /**
     * 타이틀바 레이아웃 세팅
     */
    private fun setTitleBarUI() {

        // 타이틀 세팅
        val tmp_title = findViewById<TextView>(R.id.title_bar_textTitle)
        tmp_title.text = resources.getString(R.string.title_guide_camera)
        val title_main = findViewById<TextView>(R.id.title_main)
        val str = getString(R.string.label_iuii21s01_title)
        UiUtil.setTitleColor(title_main,str,8,15)

        // left 버튼 세팅
        val tmp_btnLeft = findViewById<ImageButton>(R.id.title_bar_imgBtnLeft)
        tmp_btnLeft.visibility = View.VISIBLE
        tmp_btnLeft.setOnClickListener { finish() }
    }
}