package com.epost.insu.activity.auth

import android.content.DialogInterface
import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.viewpager.widget.ViewPager.OnPageChangeListener
import com.epost.insu.*
import com.epost.insu.activity.Activity_Default
import com.epost.insu.adapter.CustomPagerAdapter
import com.epost.insu.common.CommonFunction
import com.epost.insu.common.CustomViewPager
import com.epost.insu.common.LogPrinter
import com.epost.insu.dialog.CustomDialog
import com.epost.insu.event.OnFragmentCertificateEventListener
import com.epost.insu.fragment.Fragment_Certificate
import com.epost.insu.fragment.Fragment_CertificateDetail
import com.epost.insu.fragment.Fragment_KeyGuard

/**
 * 인증 > 공동인증서 관리 > 공동인증서 삭제
 * @since     : project 44:1.4.1
 * @version   : 1.1
 * @author    : NJM
 * <pre>
 * ======================================================================
 * 1.0  NJM_20200714    최초 등록
 * 1.1  NJM_20210218    공동인증서 삭제일경우만 만료인증서 표기 [만료인증서 숨김처리]
 * =======================================================================
 * copyright : (주)프로니스정보기술 | http://www.phronis.co.kr
</pre> *
 */
class IUPC03M00 : Activity_Default() {
    private var fragmentCert: Fragment_Certificate? = null // 인증서 목록 Fragment
    private var fragmentCertificateDetail: Fragment_CertificateDetail? = null // 인증서 상세 Fragment

    private lateinit var pager: CustomViewPager
    private var adapter: CustomPagerAdapter? = null
    private var selecteCertificateIndex: Int = 0 // 인증서목록에서 선택한 증서

    override fun onBackPressed() {
        // --<> (상세화면) 목록화면으로 이동
        if (pager!!.getCurrentItem() > 0) {
            selecteCertificateIndex = -1
            showPage(0)
        } else {
            super.onBackPressed()
        }
    }

    override fun onSaveInstanceState(p_bundle: Bundle) {
        super.onSaveInstanceState(p_bundle)
        LogPrinter.CF_debug("!----------------------------------------------------------")
        LogPrinter.CF_debug("!-- IUPC03M00.onSaveInstanceState()")
        LogPrinter.CF_debug("!----------------------------------------------------------")
        p_bundle.putInt("selecteCertificateIndex", selecteCertificateIndex)
        p_bundle.putInt("currentPageIndex", pager!!.getCurrentItem())
        p_bundle.putString("name", "ALL")
        getSupportFragmentManager().putFragment(p_bundle, Fragment_Certificate::class.java.getName(), (fragmentCert)!!)
        getSupportFragmentManager().putFragment(p_bundle, Fragment_KeyGuard::class.java.getName(), (fragmentCertificateDetail)!!)
    }

    override fun onRestoreInstanceState(p_bundle: Bundle) {
        super.onRestoreInstanceState(p_bundle)
        LogPrinter.CF_debug("!----------------------------------------------------------")
        LogPrinter.CF_debug("!-- IUPC03M00.onRestoreInstanceState()")
        LogPrinter.CF_debug("!----------------------------------------------------------")
        if (p_bundle.containsKey("selecteCertificateIndex")) {
            selecteCertificateIndex = p_bundle.getInt("selecteCertificateIndex")
        }
        if (p_bundle.containsKey("currentPageIndex")) {
            val tmp_currentPageIndex: Int = p_bundle.getInt("currentPageIndex")
            showPage(tmp_currentPageIndex)
        }
    }

    override fun setInit() {
        LogPrinter.CF_debug("!----------------------------------------------------------")
        LogPrinter.CF_debug("!-- IUPC03M00.setInit()")
        LogPrinter.CF_debug("!----------------------------------------------------------")
        setContentView(R.layout.iucoc0m00)
        selecteCertificateIndex = -1
    }

    override fun setUIControl() {
        LogPrinter.CF_debug("!----------------------------------------------------------")
        LogPrinter.CF_debug("!-- IUPC03M00.setUIControl()")
        LogPrinter.CF_debug("!----------------------------------------------------------")

        // 타이틀바 세팅
        setTitleBarUI()

        // ViewPager 세팅
        pager = findViewById(R.id.activity_login_cert_viewPager)
        pager.CF_setPagingEnabled(false)
        pager.addOnPageChangeListener(object : OnPageChangeListener {
            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {}
            override fun onPageSelected(position: Int) {
                // -- 타이틀 설정
                var tmp_label: String? = getResources().getString(R.string.label_iucoc0m00_3)
                if (position == 1) {
                    tmp_label = getResources().getString(R.string.label_iucoc0m00_4)
                }
                this@IUPC03M00.setTitle(tmp_label)
            }

            override fun onPageScrollStateChanged(state: Int) {}
        })
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        LogPrinter.CF_debug("!----------------------------------------------------------")
        LogPrinter.CF_debug("!-- IUPC03M00.onCreate()")
        LogPrinter.CF_debug("!----------------------------------------------------------")

        // -- 프래그먼트 세팅
        setFragments(savedInstanceState)
        adapter = CustomPagerAdapter(getSupportFragmentManager(), arrayOf<Fragment?>(fragmentCert, fragmentCertificateDetail))
        pager!!.setAdapter(adapter)
    }

    /**
     * 타이틀바 레이아웃 세팅
     */
    private fun setTitleBarUI() {
        LogPrinter.CF_debug("!----------------------------------------------------------")
        LogPrinter.CF_debug("!-- IUPC03M00.setTitleBarUI()")
        LogPrinter.CF_debug("!----------------------------------------------------------")

        // 타이틀 세팅
        val tmp_title: TextView = findViewById(R.id.title_bar_textTitle)
        tmp_title.setText(getResources().getString(R.string.title_del_certificate))

        // left 버튼 세팅
        val tmp_btnLeft: ImageButton = findViewById(R.id.title_bar_imgBtnLeft)
        tmp_btnLeft.setVisibility(View.VISIBLE)
        tmp_btnLeft.setOnClickListener(object : View.OnClickListener {
            override fun onClick(v: View) {
                if (pager!!.getCurrentItem() > 0) {
                    selecteCertificateIndex = -1
                    showPage(0)
                } else {
                    finish()
                }
            }
        })
    }

    /**
     * Fragment 세팅
     * Caller   onCreate()
     * @param p_bundle  Bundle
     */
    private fun setFragments(p_bundle: Bundle?) {
        var p_bundle: Bundle? = p_bundle
        LogPrinter.CF_debug("!----------------------------------------------------------")
        LogPrinter.CF_debug("!-- IUPC03M00.setFragments()")
        LogPrinter.CF_debug("!----------------------------------------------------------")
        if (p_bundle != null) {
            p_bundle.putString("name", "ALL")
            fragmentCert = getSupportFragmentManager().getFragment(p_bundle, Fragment_Certificate::class.java.getName()) as Fragment_Certificate?
            fragmentCertificateDetail = getSupportFragmentManager().getFragment(p_bundle, Fragment_CertificateDetail::class.java.getName()) as Fragment_CertificateDetail?
        } else {
            p_bundle = Bundle()
            p_bundle.putString("name", "ALL")
            fragmentCert = Fragment.instantiate(this, Fragment_Certificate::class.java.getName(), p_bundle) as Fragment_Certificate?
            fragmentCertificateDetail = Fragment.instantiate(this, Fragment_CertificateDetail::class.java.getName()) as Fragment_CertificateDetail?
        }

        // -----------------------------------------------------------------------------------------
        // 공동인증서 목록 리스너 설정
        // -----------------------------------------------------------------------------------------
        fragmentCert!!.CE_setOnFragmentCertificateEventListener(object : OnFragmentCertificateEventListener {
            override fun onSigned(p_sign: ByteArray, p_vid: String) {}
            override fun onPasswordError() {}
            override fun onSelectedEvent(p_index: Int, p_flagIsExpire: Boolean) {
                LogPrinter.CF_debug("!----------------------------------------------------------")
                LogPrinter.CF_debug("!-- IUPC03M00.OnFragmentCertificateEventListener.onSelectedEvent()")
                LogPrinter.CF_debug("!----------------------------------------------------------")
                LogPrinter.CF_debug("!-- p_index/pflagisExpire: " + p_index + " / " + p_flagIsExpire)
                selecteCertificateIndex = p_index
                fragmentCertificateDetail!!.CF_requestData(p_index)
                showPage(1)
            }

            override fun onGetList(p_count: Int) {
                if (p_count == 0) {
                    val tmp_dlg: CustomDialog = CustomDialog(this@IUPC03M00)
                    tmp_dlg.show()
                    tmp_dlg.CF_setTextContent(getResources().getString(R.string.dlg_no_certificate))
                    tmp_dlg.CF_setSingleButtonText(getResources().getString(R.string.btn_ok))
                    tmp_dlg.setOnDismissListener(object : DialogInterface.OnDismissListener {
                        override fun onDismiss(dialog: DialogInterface) {
                            finish()
                        }
                    })
                }
            }
        })
    }

    /**
     * 해당 page show 함수
     * @param p_index   int
     */
    private fun showPage(p_index: Int) {
        pager!!.setCurrentItem(p_index)
    }

    /**
     * 인증서 목록 갱신
     */
    fun deleteComplete() {
        fragmentCert!!.setInit()
        showPage(0)
        CommonFunction.CF_showCustomAlertDilaog(this, getResources().getString(R.string.dlg_cert_delete_complete), getResources().getString(R.string.btn_ok))
    }
}