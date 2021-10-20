package com.epost.insu.activity

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.view.View
import android.widget.ImageButton
import android.widget.TextView
import androidx.core.app.NotificationManagerCompat
import com.epost.insu.*
import com.epost.insu.activity.IUCOG0M00
import com.epost.insu.common.CommonFunction
import com.epost.insu.control.OnOffControl
import com.epost.insu.dialog.CustomDialog
import com.epost.insu.event.OnChangedCheckedStateEventListener

/**
 * 공통 > 설정
 * @since     :
 * @version   : 1.1
 * @author    : LSH
 * <pre>
 * App 환경 설정 화면
 * ======================================================================
 * LSH_20170711    최초 등록
 * 1.5.6    NJM_20210531    [설정알림숨김] 설정화면에서 알림 버튼 숨김 처리 (본부요청)
 * =======================================================================
 * copyright : (주)프로니스정보기술 | http://www.phronis.co.kr
</pre> *
 */
class IUCOG0M00 : Activity_Default(), View.OnClickListener {
    private var onOffControl: OnOffControl? = null // 푸시알림 수신 여부 On/Off 컨트롤

    override fun onClick(p_view: View) {
        if (p_view.id == R.id.relBtnGuideService) {          // 서비스이용약관
            startIUCOG0M01()
        } else if (p_view.id == R.id.relBtnGuidePrivacy) {    // 개인정보처리방침
            startIUCOG0M02()
        }
    }

    override fun setInit() {
        setContentView(R.layout.iucog0m00)
    }

    @SuppressLint("SetTextI18n")
    override fun setUIControl() {
        // 타이틀바 레이아웃
        setTitleBarUI()
        // 클릭가능한 UI 세팅
        setClickableUI()
        val tmp_textVersion = findViewById<TextView>(R.id.activity_setting_textVersion)
        tmp_textVersion.text = "ver " + CommonFunction.CF_getVersionName(applicationContext) + EnvConfig.app_dvsn
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }
    override fun onResume() {
        super.onResume()
        if (onOffControl!!.CF_getFlagIsOn() != flagShowNotiValue) {
            onOffControl!!.CF_setOnOffNoAnim(flagShowNotiValue)
        }
    }

    /**
     * 타이틀바 레이아웃 세팅
     */
    private fun setTitleBarUI() {
        // 타이틀 세팅
        val tmp_title = findViewById<TextView>(R.id.title_bar_textTitle)
        tmp_title.text = resources.getString(R.string.title_settings)

        // left 버튼 세팅
        val tmp_btnLeft = findViewById<ImageButton>(R.id.title_bar_imgBtnLeft)
        tmp_btnLeft.visibility = View.VISIBLE
        tmp_btnLeft.setOnClickListener { finish() }
    }

    /**
     * 클릭 가능한 UI 세팅 함수
     */
    private fun setClickableUI() {
        onOffControl = findViewById(R.id.onOffControl)
        onOffControl?.CE_setONChangedCheckedStateEventListener(OnChangedCheckedStateEventListener { p_flagCheck -> // App 내에서 관리하는 값 설정
            SharedPreferencesFunc.setFlagShowNotification(applicationContext, p_flagCheck)

            // 단말기에서 관리하는 값 설정 : 푸시알림 수신 요청 상태에서만 확인한다.
            if (p_flagCheck) {
                if (!NotificationManagerCompat.from(this@IUCOG0M00).areNotificationsEnabled()) {
                    val tmp_dlg = CustomDialog(this@IUCOG0M00)
                    tmp_dlg.show()
                    tmp_dlg.CF_setTextContent(resources.getString(R.string.dlg_agree_notification))
                    tmp_dlg.CF_setDoubleButtonText(resources.getString(R.string.btn_cancel), resources.getString(R.string.btn_setting))
                    tmp_dlg.setOnDismissListener { dialog ->
                        if (!(dialog as CustomDialog).CF_getCanceled()) {
                            startOSSettingActivity()
                        } else {
                            onOffControl?.CF_setOnOff(false)
                        }
                    }
                }
            }
        })

        // 서비스 약관, 개인정보 이용약관
        val tmp_btnService = findViewById<View>(R.id.relBtnGuideService)
        val tmp_btnPrivacy = findViewById<View>(R.id.relBtnGuidePrivacy)
        tmp_btnService.setOnClickListener(this)
        tmp_btnPrivacy.setOnClickListener(this)
    }

    /**
     * 푸시 수신 알림 유무 값 반환<br></br>
     * App 내에서 체크하는 값과, 단말기 설정에서 체크하는 값 모두 비교
     * @return  boolean
     */
    private val flagShowNotiValue: Boolean
        private get() {
            var tmp_flagShowNoti = SharedPreferencesFunc.getFlagShowNotification(applicationContext)
            if (!NotificationManagerCompat.from(this).areNotificationsEnabled()) {
                tmp_flagShowNoti = false
            }
            return tmp_flagShowNoti
        }
    // #############################################################################################
    //  Activity 호출 함수
    // #############################################################################################
    /**
     * 알림 수신 여부 변경을 위한 설정 Activity 이동 함수
     */
    private fun startOSSettingActivity() {
        val tmp_intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
        val tmp_uri = Uri.fromParts("package", packageName, null)
        tmp_intent.data = tmp_uri
        startActivity(tmp_intent)
    }

    /**
     * (IUCOG0M01) 서비스 이용약관 Activity 호출 함수
     */
    private fun startIUCOG0M01() {
        val tmp_intent = Intent(this, IUCOG0M01::class.java)
        tmp_intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
        startActivity(tmp_intent)
    }

    /**
     * (IUCOG0M02) 개인정보처리방침 Activity 호출 함수
     */
    private fun startIUCOG0M02() {
        val tmp_intent = Intent(this, IUCOG0M02::class.java)
        tmp_intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
        startActivity(tmp_intent)
    }
}