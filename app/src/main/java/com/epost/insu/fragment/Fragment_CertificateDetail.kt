package com.epost.insu.fragment

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import com.dreamsecurity.magicxsign.MagicXSign_Exception
import com.epost.insu.EnvConfig
import com.epost.insu.R
import com.epost.insu.activity.auth.IUPC03M00
import com.epost.insu.common.LogPrinter
import com.epost.insu.common.XSignHelper
import com.epost.insu.dialog.CustomDialog

/**
 * @copyright : (주)프로니스정보기술 | http://www.phronis.co.kr
 *
 * @project   : 모바일슈랑스 구축
 * @pakage    : com.epost.insu.fragment
 * @fileName  : Fragment_CertificateDetail.java
 *
 * @Title     : 공동인증서 상세(삭제) Fragment
 * @author    : 노지민
 * @created   : 2020-07-14
 * @version   : 1.0
 *
 * @note      : 공동인증서 삭제 상세화면
 * ======================================================================
 * 수정 내역
 * NO       날짜          작업자       내용
 * 01       2020-07-14    노지민     : 최초 등록
 * =======================================================================
 */
class Fragment_CertificateDetail : Fragment_Default() {
    private var xSignHelper: XSignHelper? = null
    private var selectedItemIndex = 0
    private var tmp_text01: TextView? = null
    private var tmp_text02: TextView? = null
    private var tmp_text03: TextView? = null
    private var tmp_text04: TextView? = null
    private var tmp_text05: TextView? = null
    override fun onAttach(context: Context) {
        super.onAttach(context)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setInit()
    }

    @SuppressLint("InflateParams")
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_certificate_detail, null)
    }

    override fun onInflate(context: Context, attrs: AttributeSet, savedInstanceState: Bundle?) {
        super.onInflate(context, attrs, savedInstanceState)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        setUIControl()
    }

    override fun onDestroy() {
        super.onDestroy()
    }

    /**
     * 초기 세팅
     */
    private fun setInit() {
        // 공동인증관리 헬퍼 생성
        xSignHelper = XSignHelper(this.activity, EnvConfig.xSignDebugLevel)
    }

    /**
     * UI 생성 및 세팅 함수
     */
    private fun setUIControl() {
        LogPrinter.CF_debug("!----------------------------------------------------------")
        LogPrinter.CF_debug("!-- Fragment_CertificateDetail.setUIControl()")
        LogPrinter.CF_debug("!----------------------------------------------------------")

        // 상단 텍스트
        val tmp_textLabel = view!!.findViewById<TextView>(R.id.fragment_textLabel)
        tmp_textLabel.text = resources.getString(R.string.guide_confirm_certificate)
        val tmp_lin01 = view!!.findViewById<LinearLayout>(R.id.label_TextReqType01)
        val tmp_lin02 = view!!.findViewById<LinearLayout>(R.id.label_TextReqType02)
        val tmp_lin03 = view!!.findViewById<LinearLayout>(R.id.label_TextReqType03)
        val tmp_lin04 = view!!.findViewById<LinearLayout>(R.id.label_TextReqType04)
        val tmp_lin05 = view!!.findViewById<LinearLayout>(R.id.label_TextReqType05)

        // -- 라벨 세팅
        val tmp_label01 = tmp_lin01.findViewById<TextView>(R.id.label)
        val tmp_label02 = tmp_lin02.findViewById<TextView>(R.id.label)
        val tmp_label03 = tmp_lin03.findViewById<TextView>(R.id.label)
        val tmp_label04 = tmp_lin04.findViewById<TextView>(R.id.label)
        val tmp_label05 = tmp_lin05.findViewById<TextView>(R.id.label)
        tmp_label01.text = resources.getString(R.string.label_old_readable)
        tmp_label02.text = resources.getString(R.string.label_user_name)
        tmp_label03.text = resources.getString(R.string.label_ca_readable)
        tmp_label04.text = resources.getString(R.string.label_expiration_from)
        tmp_label05.text = resources.getString(R.string.label_expiration_to)

        // -- 텍스트 세팅
        tmp_text01 = tmp_lin01.findViewById(R.id.text)
        tmp_text02 = tmp_lin02.findViewById(R.id.text)
        tmp_text03 = tmp_lin03.findViewById(R.id.text)
        tmp_text04 = tmp_lin04.findViewById(R.id.text)
        tmp_text05 = tmp_lin05.findViewById(R.id.text)

        // -- 다음버튼
        val tmp_btnOk = view!!.findViewById<Button>(R.id.btnFill)
        tmp_btnOk.text = resources.getString(R.string.btn_ok)
        tmp_btnOk.setOnClickListener {
            // ---------------------------------------------------------------------------------
            //  삭제 확인 다이얼로그 팝업
            // ---------------------------------------------------------------------------------
            val tmp_dlg = CustomDialog(context!!)
            tmp_dlg.show()
            tmp_dlg.CF_setBackKeyMode(CustomDialog.BackMode.CANCELED)
            tmp_dlg.CF_setTextContent(resources.getString(R.string.dlg_ask_remove))
            tmp_dlg.CF_setDoubleButtonText(resources.getString(R.string.btn_cancel), resources.getString(R.string.btn_del))
            tmp_dlg.setOnDismissListener { dialog ->
                // -- (삭제선택)
                if ((dialog as CustomDialog).CF_getCanceled() == false) {
                    try {
                        // -- (삭제성공)
                        if (xSignHelper!!.CF_delete(selectedItemIndex)) {
                            (activity as IUPC03M00?)!!.deleteComplete()
                        }
                    } catch (e: MagicXSign_Exception) {
                        e.message
                    }
                } else {
                }
            }
        }
    }

    /**
     * Activity 호출
     * 선택된 인증서 인덱스로 데이터 조회
     */
    fun CF_requestData(index: Int) {
        try {
            val selectedItem = xSignHelper!!.certList[index]
            tmp_text01!!.text = selectedItem.oiD_Readable
            tmp_text02!!.text = selectedItem.useR_Name
            tmp_text03!!.text = selectedItem.cA_Readable
            tmp_text04!!.text = convertDate(selectedItem.expirationFrom)
            tmp_text05!!.text = convertDate(selectedItem.expirationTo)
            selectedItemIndex = selectedItem.index
            LogPrinter.CF_debug("!-- 선택증서$selectedItem")
        } catch (e: MagicXSign_Exception) {
            e.message
        }
    }

    /**
     * 만료일 포멧 변경<br></br>
     * @param p_strDate
     * @return
     */
    private fun convertDate(p_strDate: String): String {
        var tmp_convert = p_strDate
        val tmp_arrDate = p_strDate.split(" ").toTypedArray()
        if (tmp_arrDate.size > 0) {
            tmp_convert = tmp_arrDate[0].replace("-", ".")
        }
        return tmp_convert
    }
}