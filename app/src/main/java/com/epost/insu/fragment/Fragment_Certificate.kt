package com.epost.insu.fragment

import android.annotation.SuppressLint
import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.dreamsecurity.magicxsign.MagicXSign_Exception
import com.epost.insu.EnvConfig
import com.epost.insu.R
import com.epost.insu.activity.auth.IUPC80M00
import com.epost.insu.adapter.CertificateAdapter
import com.epost.insu.common.*
import com.epost.insu.data.Data_CertDetail
import com.epost.insu.event.OnFragmentCertificateEventListener
import java.util.*

/**
 * 공동인증서 선택 Fragment
 * @since     : project 44:1.4.1
 * @version   : 1.2
 * @author    : LSM
 * <pre>
 * 공통 사용
 * - [IUCOC0M00]     공동인증 로그인
 * - [IUCOC0M00_Web] 공동인증 로그인(웹 요청)
 * - [IUPC80M00]     본인인증
 * - [IUPC80M10_Web] 본인인증(웹 요청)
 * ======================================================================
 * 0.0.0    LSH_20171116    최초 등록
 * 0.0.0    NJM_20200714    공동인증서 삭제 추가 - setInit() 함수 변경
 * 0.0.0    NJM_20210218    공동인증서 삭제일경우만 만료인증서 표기 [만료인증서 숨김처리]
 * 1.6.3    NJM_20211007    [API30 대응] targetApi 29 -> 30 변경에 따른 코틀린 오류 수정
 * =======================================================================
 * copyright : (주)프로니스정보기술 | http://www.phronis.co.kr
</pre> *
 */
class Fragment_Certificate : Fragment_Default() {
    private var listener: OnFragmentCertificateEventListener? = null
    private var adapter: CertificateAdapter? = null
    private var xSignHelper: XSignHelper? = null
    private var userName: String? = null // 인증서 조회 이름(empty가 아닌 경우 해당 이름의 인증서만 노출)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (arguments != null) {
            if (arguments!!.containsKey("name")) {
                userName = arguments!!.getString("name")
            }
        }

        // 공동인증관리 헬퍼 생성
        xSignHelper = XSignHelper(this.activity, EnvConfig.xSignDebugLevel)

        // 공동인증서 목록 Adapter 생성
        adapter = CertificateAdapter(activity!!)

        // 초기 세팅
        setInit()
    }

    @SuppressLint("InflateParams")
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_certificate, null)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        // UI 생성 및 세팅
        setUIControl()
    }

    override fun onDestroy() {
        super.onDestroy()
        if (xSignHelper != null) {
            xSignHelper!!.CF_Finish()
        }
    }

    /**
     * 초기 세팅 함수
     */
    fun setInit() {
        try {
            if (TextUtils.isEmpty(userName)) {
                // ---------------------------------------------------------------------------------
                //  만료 제외 인증서만 apdater에 세팅
                // ---------------------------------------------------------------------------------
                val arrData = xSignHelper!!.certList
                val arrSearchData = ArrayList<Data_CertDetail>()
                for (i in arrData.indices) {
                    if (!arrData[i].expire) {
                        arrSearchData.add(arrData[i])
                    }
                }
                adapter!!.CF_setData(arrSearchData)
            } else if ("ALL" == userName) {
                // ---------------------------------------------------------------------------------
                //  전체 인증서 adapter에 세팅
                // ---------------------------------------------------------------------------------
                adapter!!.CF_setData(xSignHelper!!.certList)
            } else {
                // ---------------------------------------------------------------------------------
                //  해당 이름정보의 인증서만 apdater에 세팅
                // ---------------------------------------------------------------------------------
                val arrData = xSignHelper!!.certList
                val arrSearchData = ArrayList<Data_CertDetail>()
                for (i in arrData.indices) {
                    if (arrData[i].user.contains(userName!!)) {
                        arrSearchData.add(arrData[i])
                    }
                }
                adapter!!.CF_setData(arrSearchData)
            }
        } catch (e: MagicXSign_Exception) {
            LogPrinter.CF_debug(resources.getString(R.string.log_fail_get_xsign))
        }

        // 인증서 클릭 이벤트 리스너
        adapter!!.CE_setOnItemClickEventListener { p_index ->
            if (listener != null) {
                listener!!.onSelectedEvent(adapter!!.CF_getCertificateIndex(p_index), adapter!!.CF_isExpire(p_index))
            }
        }
        if (listener != null) {
            // -------------------------------------------------------------------------------------
            //  인증서 로드 완료 이벤트 발생
            // -------------------------------------------------------------------------------------
            listener!!.onGetList(adapter!!.itemCount)
        }
    }

    /**
     * UI 생성 및 세팅 함수
     */
    private fun setUIControl() {
        val linLM = LinearLayoutManager(this.activity)
        linLM.orientation = LinearLayoutManager.VERTICAL

        // 공동인증서 목록 RecyclerView 세팅
        val recycleView: RecyclerView? = view?.findViewById(R.id.fragment_certificate_recyclerView)
        recycleView?.layoutManager = linLM
        recycleView?.adapter = adapter
        recycleView?.addItemDecoration(ContentsItemOffset(0, CommonFunction.CF_convertDipToPixel(Objects.requireNonNull(this.activity)!!.applicationContext, 5f), 0, CommonFunction.CF_convertDipToPixel(this.activity!!.applicationContext, 5f)))
    }

    /**
     * 라벨 문구 세팅
     * @param p_str String
     */
    fun CF_setLabel(p_str: String?) {
        val tmp_textLabel = Objects.requireNonNull(view)?.findViewById<View>(R.id.fragment_certificate_labelText) as TextView
        tmp_textLabel.text = p_str
    }

    /**
     * 공동인증서 목록 이벤트 세팅
     * @param p_listener    OnFragmentCertificateEventListener
     */
    fun CE_setOnFragmentCertificateEventListener(p_listener: OnFragmentCertificateEventListener?) {
        listener = p_listener
    }

    /**
     * 공동인증서 서명 요청 함수
     * @param p_index      int 인증서 index
     * @param p_password   String 비밀번호
     */
    fun CF_requestSign(p_index: Int, p_password: String) {
        var binSignData: ByteArray? = null
        val strVID: String
        try {
            binSignData = xSignHelper!!.CF_certSign(p_index, "" + System.currentTimeMillis(), p_password.toByteArray())
            if (binSignData != null) {
                strVID = xSignHelper!!.CF_encodeBase64(xSignHelper!!.CF_getVIDRandom(p_index, p_password.toByteArray()))
                if (listener != null) {
                    listener!!.onSigned(binSignData, strVID)
                }
            }
        } catch (e: MagicXSign_Exception) {
            LogPrinter.CF_debug(resources.getString(R.string.log_fail_sign_xsign) + e.message)
        }
        if (binSignData == null && listener != null) {
            listener!!.onPasswordError()
        }
    }
}