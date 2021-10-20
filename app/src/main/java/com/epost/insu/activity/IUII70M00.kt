package com.epost.insu.activity

import android.content.Context
import android.graphics.Color
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.text.style.UnderlineSpan
import android.view.View
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import com.epost.insu.EnvConfig
import com.epost.insu.R
import com.epost.insu.common.CommonFunction
import com.epost.insu.common.UiUtil
import com.epost.insu.common.WebFileDownloadHelper
import java.util.*

/**
 * @copyright : (주)프로니스정보기술 | http://www.phronis.co.kr
 *
 * @project   : 모바일슈랑스 구축
 * @pakage   : com.epost.insu.activity
 * @fileName  : IUII70M00.java
 *
 * @Title     : 보험금청구 > 보험금청구선택 > 보험금청구시 구비서류 (화면 ID : IUII70M00) - #28
 * @author    : 이수행
 * @created   : 2017-09-20
 * @version   : 1.0
 *
 * @note      :  안내항목<br></br>
 * 공통서류/재해사고 증명서류/사망보험금 청구서류/장해급부금 청구서류/진단급부금 청구서류 / 수술급부금 청구서류<br></br>
 * 통원급부금 청구서류/납입면제 안내/실손의료비보험 청구서류 안내/단체보장보험 청구서류/구비서류 유의사항<br></br>
 * ======================================================================
 * 수정 내역
 * NO       날짜          작업자       내용
 * 01       2017-09-20    이수행     : 최초 등록
 * 02       2017-11-27    이수행     : 공통서류 제거
 * 03       2019-09-11    노지민     : 입원금부금 추가 (index 12)
 * 04       2019-12-04    노지민     : 구비서류 텍스트 음영 틀어진부분 수정
 * 05       2019-12-23    노지민     : 치아보험 구비서류 추가
 * 05       2020-01-06    노지민     : 치아보험 구비서류 양식 다운로드 후 pdf 뷰어로 출력
 * =======================================================================
 */
class IUII70M00 : Activity_Default(), View.OnClickListener {
    private var arrGroupView: ArrayList<View>? = null
    private var arrContentsView: ArrayList<View>? = null
    private var arrIcons: ArrayList<ImageView>? = null
    private var arrExpandDescString: ArrayList<String>? = null
    private var arrCollpseDescString: ArrayList<String>? = null
    private var arrSubTitle: ArrayList<String>? = null
    private val mContext: Context? = null

    override fun onClick(p_view: View) {
        val tmp_index = arrGroupView!!.indexOf(p_view)
        if (tmp_index >= 0 && tmp_index < arrGroupView!!.size) {
            showHideSubContents(tmp_index)
        }
        if (tmp_index == arrGroupView!!.size - 1) {
            if (arrContentsView!![tmp_index].visibility == View.VISIBLE) {
                arrGroupView!![tmp_index].setBackgroundResource(R.drawable.btn_w_g_0_selector)
            } else {
                arrGroupView!![tmp_index].setBackgroundResource(R.drawable.btn_w_g_bottom_selector)
            }
            arrGroupView!![tmp_index].setPadding(CommonFunction.CF_convertDipToPixel(applicationContext, 10f),
                    CommonFunction.CF_convertDipToPixel(applicationContext, 10f),
                    CommonFunction.CF_convertDipToPixel(applicationContext, 10f),
                    CommonFunction.CF_convertDipToPixel(applicationContext, 10f))
        }
    }

    /**
     * 2019-09-11   입원급부금 추가
     */
    override fun setInit() {
        setContentView(R.layout.iuii70m00)
        arrGroupView = ArrayList()
        arrContentsView = ArrayList()
        arrIcons = ArrayList()
        arrExpandDescString = ArrayList()
        arrExpandDescString!!.add(resources.getString(R.string.desc_expand_doc_2))
        arrExpandDescString!!.add(resources.getString(R.string.desc_expand_doc_3))
        arrExpandDescString!!.add(resources.getString(R.string.desc_expand_doc_4))
        arrExpandDescString!!.add(resources.getString(R.string.desc_expand_doc_5))
        arrExpandDescString!!.add(resources.getString(R.string.desc_expand_doc_6))
        arrExpandDescString!!.add(resources.getString(R.string.desc_expand_doc_7))
        arrExpandDescString!!.add(resources.getString(R.string.desc_expand_doc_8))
        arrExpandDescString!!.add(resources.getString(R.string.desc_expand_doc_9))
        arrExpandDescString!!.add(resources.getString(R.string.desc_expand_doc_10))
        arrExpandDescString!!.add(resources.getString(R.string.desc_expand_doc_11))
        arrExpandDescString!!.add(resources.getString(R.string.desc_expand_doc_12))
        arrExpandDescString!!.add(resources.getString(R.string.desc_expand_doc_13))
        arrCollpseDescString = ArrayList()
        arrCollpseDescString!!.add(resources.getString(R.string.desc_collpse_doc_2))
        arrCollpseDescString!!.add(resources.getString(R.string.desc_collpse_doc_3))
        arrCollpseDescString!!.add(resources.getString(R.string.desc_collpse_doc_4))
        arrCollpseDescString!!.add(resources.getString(R.string.desc_collpse_doc_5))
        arrCollpseDescString!!.add(resources.getString(R.string.desc_collpse_doc_6))
        arrCollpseDescString!!.add(resources.getString(R.string.desc_collpse_doc_7))
        arrCollpseDescString!!.add(resources.getString(R.string.desc_collpse_doc_8))
        arrCollpseDescString!!.add(resources.getString(R.string.desc_collpse_doc_9))
        arrCollpseDescString!!.add(resources.getString(R.string.desc_collpse_doc_10))
        arrCollpseDescString!!.add(resources.getString(R.string.desc_collpse_doc_11))
        arrCollpseDescString!!.add(resources.getString(R.string.desc_collpse_doc_12))
        arrCollpseDescString!!.add(resources.getString(R.string.desc_collpse_doc_13))
        arrSubTitle = ArrayList()
        arrSubTitle!!.add(resources.getString(R.string.btn_doc_list_2))
        arrSubTitle!!.add(resources.getString(R.string.btn_doc_list_3))
        arrSubTitle!!.add(resources.getString(R.string.btn_doc_list_4))
        arrSubTitle!!.add(resources.getString(R.string.btn_doc_list_5))
        arrSubTitle!!.add(resources.getString(R.string.btn_doc_list_6))
        arrSubTitle!!.add(resources.getString(R.string.btn_doc_list_7))
        arrSubTitle!!.add(resources.getString(R.string.btn_doc_list_8))
        arrSubTitle!!.add(resources.getString(R.string.btn_doc_list_9))
        arrSubTitle!!.add(resources.getString(R.string.btn_doc_list_10))
        arrSubTitle!!.add(resources.getString(R.string.btn_doc_list_12))
        arrSubTitle!!.add(resources.getString(R.string.btn_doc_list_13))
    }

    /**
     * 2019-09-11   입원급부금 추가
     */
    override fun setUIControl() {

        // 타이틀바 세팅
        setTitleBarUI()
        val tmp_relDisasterDoc = findViewById<RelativeLayout>(R.id.relDisasterDoc) // 02. 재해사고 증명서류(우선순위 참조)
        val tmp_relDeathDoc = findViewById<RelativeLayout>(R.id.relDeathDoc) // 03. 사망보험금
        val tmp_relObstacleDoc = findViewById<RelativeLayout>(R.id.relObstacleDoc) // 04. 장해급부금
        val tmp_relDiagnosisDoc = findViewById<RelativeLayout>(R.id.relDiagnosisDoc) // 05. 진단급부금
        val tmp_relSurgeryDoc = findViewById<RelativeLayout>(R.id.relSurgeryDoc) // 06. 수술급부금
        val tmp_relGoingHospitalDoc = findViewById<RelativeLayout>(R.id.relGoingHospitalDoc) // 07. 통원급부금
        val tmp_relPayDoc = findViewById<RelativeLayout>(R.id.relPayDoc) // 08. 납입면제
        val tmp_relRealAccidentDoc = findViewById<RelativeLayout>(R.id.relRealAccidentDoc) // 09. 실손의료비보험
        val tmp_relGroupDoc = findViewById<RelativeLayout>(R.id.relGroupDoc) // 10. 단체보장보험
        val tmp_relMatterDoc = findViewById<RelativeLayout>(R.id.relMatterDoc) // 11. 구비서류 유의사항
        val tmp_relEnterDoc = findViewById<RelativeLayout>(R.id.relEnterDoc) // 12. 입원급부금
        val tmp_relToothInsulDoc = findViewById<RelativeLayout>(R.id.relToothInsulDoc) // 13. 치아보험
        tmp_relDisasterDoc.setOnClickListener(this)
        tmp_relDeathDoc.setOnClickListener(this)
        tmp_relObstacleDoc.setOnClickListener(this)
        tmp_relDiagnosisDoc.setOnClickListener(this)
        tmp_relSurgeryDoc.setOnClickListener(this)
        tmp_relGoingHospitalDoc.setOnClickListener(this)
        tmp_relPayDoc.setOnClickListener(this)
        tmp_relRealAccidentDoc.setOnClickListener(this)
        tmp_relGroupDoc.setOnClickListener(this)
        tmp_relMatterDoc.setOnClickListener(this)
        tmp_relEnterDoc.setOnClickListener(this)
        tmp_relToothInsulDoc.setOnClickListener(this)
        arrGroupView!!.add(tmp_relDisasterDoc)
        arrGroupView!!.add(tmp_relDeathDoc)
        arrGroupView!!.add(tmp_relObstacleDoc)
        arrGroupView!!.add(tmp_relDiagnosisDoc)
        arrGroupView!!.add(tmp_relSurgeryDoc)
        arrGroupView!!.add(tmp_relGoingHospitalDoc)
        arrGroupView!!.add(tmp_relPayDoc)
        arrGroupView!!.add(tmp_relRealAccidentDoc)
        arrGroupView!!.add(tmp_relGroupDoc)
        arrGroupView!!.add(tmp_relMatterDoc)
        arrGroupView!!.add(tmp_relEnterDoc)
        arrGroupView!!.add(tmp_relToothInsulDoc)
        arrContentsView!!.add(findViewById(R.id.linDoc_2))
        arrContentsView!!.add(findViewById(R.id.linDoc_3))
        arrContentsView!!.add(findViewById(R.id.linDoc_4))
        arrContentsView!!.add(findViewById(R.id.linDoc_5))
        arrContentsView!!.add(findViewById(R.id.linDoc_6))
        arrContentsView!!.add(findViewById(R.id.linDoc_7))
        arrContentsView!!.add(findViewById(R.id.linDoc_8))
        arrContentsView!!.add(findViewById(R.id.linDoc_9))
        arrContentsView!!.add(findViewById(R.id.linDoc_10))
        arrContentsView!!.add(findViewById(R.id.linDoc_11))
        arrContentsView!!.add(findViewById(R.id.linDoc_12))
        arrContentsView!!.add(findViewById(R.id.linDoc_13)) // 우체국치아보험
        arrIcons!!.add(findViewById<View>(R.id.img_2) as ImageView)
        arrIcons!!.add(findViewById<View>(R.id.img_3) as ImageView)
        arrIcons!!.add(findViewById<View>(R.id.img_4) as ImageView)
        arrIcons!!.add(findViewById<View>(R.id.img_5) as ImageView)
        arrIcons!!.add(findViewById<View>(R.id.img_6) as ImageView)
        arrIcons!!.add(findViewById<View>(R.id.img_7) as ImageView)
        arrIcons!!.add(findViewById<View>(R.id.img_8) as ImageView)
        arrIcons!!.add(findViewById<View>(R.id.img_9) as ImageView)
        arrIcons!!.add(findViewById<View>(R.id.img_10) as ImageView)
        arrIcons!!.add(findViewById<View>(R.id.img_11) as ImageView)
        arrIcons!!.add(findViewById<View>(R.id.img_12) as ImageView)
        arrIcons!!.add(findViewById<View>(R.id.img_13) as ImageView)

        // Text Spannable 세팅
        setSpannableText()

        // 치아보험 양식(pdf) 다운로드
        val mContext: Context = this
        val tmp_textDoc13_1 = findViewById<TextView>(R.id.textDoc13_1) // 치아보험 양식
        tmp_textDoc13_1.setOnClickListener {
            // -- 파일 다운로드 URL
            val downUrl = EnvConfig.host_url + "/pdfs/claim/insu_claim_10.pdf"

            // TODO : 파일다운로드 선택
            // 파일스레드를 통한 다운로드 : 리시버 없이 다운로드 후 파일뷰 실행 가능
            WebFileDownloadHelper.webFileDownloadThread(mContext, downUrl, "insu_claim_10.pdf", "insu_claim_10.pdf")

            // 웹으로 연결 : 브라우저를 통한 다운로드
            // WebBrowserHelper.callWebBrowser(getApplicationContext(), downUrl);

            // 파일매니저를 통한 다운로드 : 리시버 없으면 다운로드 후 실행 불가
            //WebFileDownloadHelper.webFileDownloadManager(mContext, downUrl, "", "insu_claim_10.pdf", "", false, 0 );
        }
    }

    /**
     * 타이틀바 레이아웃 세팅
     */
    private fun setTitleBarUI() {

        // 타이틀 세팅
        val tmp_title = findViewById<TextView>(R.id.title_bar_textTitle)
        tmp_title.text = resources.getString(R.string.title_guide_doc)


        //타이틀 텍스트 수정

        val title_main = findViewById<View>(R.id.title_main) as TextView

        UiUtil.setTitleColor(title_main,resources.getString(R.string.label_iuii70m00_title),7,11)

        // left 버튼 세팅
        val tmp_btnLeft = findViewById<ImageButton>(R.id.title_bar_imgBtnLeft)
        tmp_btnLeft.visibility = View.VISIBLE
        tmp_btnLeft.setOnClickListener { finish() }
    }

    /**
     * Spannable Text 세팅 함수
     */
    private fun setSpannableText() {

        // 진단급부금 청구서류 Spannable
        val tmp_text_5_2 = findViewById<TextView>(R.id.textDoc_5_2)
        val tmp_spannable_5_2: Spannable = SpannableString(resources.getString(R.string.guide_add_doc_5_2))
        tmp_spannable_5_2.setSpan(ForegroundColorSpan(Color.parseColor("#ff7c29c7")), 14, 33, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        tmp_spannable_5_2.setSpan(ForegroundColorSpan(Color.parseColor("#ff7c29c7")), 62, 72, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        tmp_spannable_5_2.setSpan(ForegroundColorSpan(Color.parseColor("#ff7c29c7")), 118, 125, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        tmp_spannable_5_2.setSpan(ForegroundColorSpan(Color.parseColor("#ff7c29c7")), 144, 170, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        tmp_text_5_2.text = tmp_spannable_5_2

        // 납입면제 안내 청구서류 Spannable
        val tmp_text_8_1 = findViewById<TextView>(R.id.textDoc8_1)
        val tmp_spannable_8_1: Spannable = SpannableString(resources.getString(R.string.guide_add_doc_8_1))
        tmp_spannable_8_1.setSpan(ForegroundColorSpan(Color.parseColor("#ff7c29c7")), 0, 4, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        tmp_text_8_1.text = tmp_spannable_8_1

        // 실손의료비보험 청구서류 Spannable
        val tmp_text_9_2 = findViewById<TextView>(R.id.textDoc9_2)
        val tmp_spannable_9_2: Spannable = SpannableString(resources.getString(R.string.guide_add_doc_9_2))
        tmp_spannable_9_2.setSpan(ForegroundColorSpan(Color.parseColor("#ff7c29c7")), 57, 94, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        tmp_text_9_2.text = tmp_spannable_9_2

        // 치아보험 청구서류 Spannable
        val tmp_text_13_1 = findViewById<TextView>(R.id.textDoc13_1)
        val tmp_spannable_13_1: Spannable = SpannableString(resources.getString(R.string.guide_add_doc_13_1))
        tmp_spannable_13_1.setSpan(ForegroundColorSpan(Color.parseColor("#ff7c29c7")), 0, 8, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        tmp_spannable_13_1.setSpan(ForegroundColorSpan(Color.rgb(0, 0, 254)), 9, 14, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        tmp_spannable_13_1.setSpan(UnderlineSpan(), 9, 14, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        tmp_text_13_1.text = tmp_spannable_13_1
    }

    /**
     * 컨텐츠 Show / Hide 변경 함수<br></br>
     * 현재 Visible 상태이면 Gone 처리, 현재 Gone 상태이면 Visible 처리
     * @param p_index   int
     */
    fun showHideSubContents(p_index: Int) {
        if (arrContentsView!![p_index].visibility == View.VISIBLE) {
            arrContentsView!![p_index].visibility = View.GONE
            arrGroupView!![p_index].contentDescription = arrExpandDescString!![p_index]
            arrIcons!![p_index].setImageResource(R.drawable.accordian)
            if (CommonFunction.CF_checkAccessibilityTurnOn(this)) {
                arrGroupView!![p_index].announceForAccessibility(arrSubTitle!![p_index] + " 내용 접힘")
            }
        } else {
            arrContentsView!![p_index].visibility = View.VISIBLE
            arrGroupView!![p_index].contentDescription = arrCollpseDescString!![p_index]
            arrIcons!![p_index].setImageResource(R.drawable.accordian_on)
            if (CommonFunction.CF_checkAccessibilityTurnOn(this)) {
                arrGroupView!![p_index].announceForAccessibility(arrSubTitle!![p_index] + " 내용 펼쳐짐")
            }
        }
    }
}