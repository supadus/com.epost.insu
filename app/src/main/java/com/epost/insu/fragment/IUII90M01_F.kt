package com.epost.insu.fragment

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.epost.insu.R
import com.epost.insu.common.LogPrinter

/**
 * 보험금청구 > 자녀청구 > 1단계. 접수 시 확인 사항
 * @since     :
 * @version   : 1.0
 * @author    : YJH
 * @see
 * <pre>
 * ======================================================================
 * 0.0.0    YJH_20181109    최초 등록
 * =======================================================================
 * copyright : (주)프로니스정보기술 | http://www.phronis.co.kr
 * </pre>
 */
class IUII90M01_F : IUII90M00_FD() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setInit()
    }

    @SuppressLint("InflateParams")
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.iuii10m01_f, null)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        setUIControl()
    }

    /**
     * 초기 세팅 함수
     */
    private fun setInit() {
        LogPrinter.CF_debug("!----------------------------------------------------------")
        LogPrinter.CF_debug("!-- IUII90M01_F.setInit()")
        LogPrinter.CF_debug("!----------------------------------------------------------")
        LogPrinter.CF_debug("!---- (1단계) 인증구분 : " + mActivity?.CF_getAuthDvsn())
    }

    /**
     * UI 생성 및 세팅 함수
     */
    private fun setUIControl() {
        scrollView = view!!.findViewById(R.id.scrollView)
        btnNext = view!!.findViewById(R.id.btnFill)
        btnNext?.text = resources.getString(R.string.btn_next_2)
        btnNext?.setOnClickListener {
            btnNext?.isEnabled = false
            mActivity?.CF_showNextPage()
        }
    }
}