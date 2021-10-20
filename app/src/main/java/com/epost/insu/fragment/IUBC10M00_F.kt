package com.epost.insu.fragment

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import com.epost.insu.R
import com.epost.insu.SharedPreferencesFunc

/**
 * @copyright : 우정사업정보센터
 *
 * @project   : 모바일슈랑스 구축
 * @pakage   : com.epost.insu.fragment
 * @fileName  : IUBC10M00_F.java
 *
 * @Title     : 스마트보험금청구 > 보험금청구접수신청 (화면 ID : IUBC10M00)
 * @author    : 이경민
 * @created   : 2018-06-09
 * @version   : 1.0
 *
 * @note      : <u>스마트보험금청구 > 보험금청구접수신청 (화면 ID : IUBC10M00)</u><br></br>
 * 보험금지급청구 1단계 :: 접수 시 확인 사항 안내
 * ======================================================================
 * 수정 내역
 * NO      날짜          작업자       내용
 * 01      2018-06-09    이경민       최초 등록
 * =======================================================================
 */
class IUBC10M00_F : IUBC10M00_FD() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setInit()
    }

    @SuppressLint("InflateParams")
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.iubc10m00_f, null)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        setUIControl()
    }

    /**
     * 초기 세팅 함수
     */
    private fun setInit() {}

    /**
     * UI 생성 및 세팅 함수
     */
    private fun setUIControl() {
        scrollView = view!!.findViewById(R.id.scrollView)
        val hospitalCode = SharedPreferencesFunc.getSmartReqHospitalCode(activity!!.applicationContext)
        if (hospitalCode == "H20024") {
            val layout_step1_7 = view!!.findViewById<LinearLayout>(R.id.guide_req_step_1_7)
            val layout_step1_8 = view!!.findViewById<LinearLayout>(R.id.guide_req_step_1_8)
            layout_step1_7.visibility = View.INVISIBLE
            layout_step1_8.visibility = View.INVISIBLE
        }
        btnNext = view!!.findViewById(R.id.btnFill)
        btnNext?.text = resources.getString(R.string.btn_next_2)
        btnNext?.setOnClickListener {
            btnNext?.isEnabled = false
            mActivity!!.CF_showNextPage()
        }
    }
}