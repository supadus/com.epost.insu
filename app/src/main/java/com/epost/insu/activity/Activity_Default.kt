package com.epost.insu.activity

import android.content.BroadcastReceiver
import android.content.DialogInterface
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Color
import android.net.ConnectivityManager
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import com.TouchEn.mVaccine.b2b2c.receiver.ScanReceiver
import com.TouchEn.mVaccine.b2b2c.util.CommonUtil
import com.epost.insu.CustomApplication
import com.epost.insu.EnvConfig
import com.epost.insu.EnvConstant
import com.epost.insu.R
import com.epost.insu.common.CommonFunction
import com.epost.insu.common.ImageProgressDialog
import com.epost.insu.common.LogPrinter
import com.epost.insu.dialog.CustomDialog
import com.epost.insu.network.WeakReferenceHandler

/**
 * Default Activity
 * @since     :
 * @version   : 1.3
 * @author    : NJM
 * <pre>
 * 솔루션에서 제공하는 Activity를 제외하고 모든 Activity는 [Activity_Default]를 상속받는다.
 * [.onCreate]에서 초기화, UI 세팅을 위한 abstract 함수 제공
 * [.setInit], [.setUIControl]
 *
 * 접근성 처리를 위한 다이얼로그 show 함수 제공
 * [.showCustomDialog], [.showCustomDialog]
 * [.showCustomDialog]
 *
 * Touch mVaccine 리시버 등록
 * 리시버를 [.onStart] 시점에 등록하고 [.onStop] 시점에 해지함.
 * ======================================================================
 * 0.0.0    LSM_20170808    최초 등록
 * 0.0.0    NJM_20190207    앱 초기 실행시 네트워크 상태 체크 추가
 * 0.0.0    NJM_20191121    라온시큐어 엠백신 모듈 안드로이드Q대응 업데이트
 * 0.0.0    NJM_20200225    onStart에서 앱 루팅검사 추가
 * 1.5.2    NJM_20210322    [FIDO인증 로직 변경] CF_finishCancelMsg() 추가
 * 1.6.3    NJM_20210826    [캡쳐방지 예외] 캡쳐방지 예외추가 및 로그표기일때만 예외처리
 * =======================================================================
 * copyright : (주)프로니스정보기술 | http://www.phronis.co.kr
</pre> *
 */
abstract class Activity_Default : AppCompatActivity() {
    val Tag = this.javaClass.simpleName
    @JvmField
    protected var handler: WeakReferenceHandler? = null // 핸들러
    private var progressDialog: ImageProgressDialog? = null // 이미지 프로그레스 다이얼로그


    private var receiver: BroadcastReceiver? = null // mVaccine 리시버

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        LogPrinter.CF_debug("Activity Class Name :"+localClassName)
        LogPrinter.CF_debug("!----------------------------------------------------------")
        LogPrinter.CF_debug("!-- Activity_Default.onCreate()")
        LogPrinter.CF_debug("!----------------------------------------------------------")
        if(Build.VERSION.SDK_INT>=23) {
            getWindow().setStatusBarColor(Color.WHITE);
        }
        setInit()
        setUIControl()
        // 캡쳐 방지 적용 (DRM적용: addFlags / 미적용: clearFlags)
        if (EnvConfig.mFlagShowLog) {
            window.clearFlags(WindowManager.LayoutParams.FLAG_SECURE)
        } else {
            window.addFlags(WindowManager.LayoutParams.FLAG_SECURE)
        }
    }



    //    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    override fun onStart() {
        super.onStart()
        LogPrinter.CF_debug("!----------------------------------------------------------")
        LogPrinter.CF_debug("!-- Activity_Default.onStart()")
        LogPrinter.CF_debug("!----------------------------------------------------------")

        // -- foregroundCount가 1이면 백그라운드 -> 포그라운드
        foregroundCount += 1


        // -- 루팅검사
        LogPrinter.CF_debug("!---- foregroundCount : $foregroundCount")
        if (foregroundCount == 1) {
            checkRooting()
        }
        if (CustomApplication.CF_getKillApp() == CustomApplication.KillAppMode.noKill) {
            setMVaccineReceiver()
        } else {
            var tmp_flagFinish = true



            // -------------------------------------------------------------------------------------
            //  메인 화면인 경우 AppKill flag value  초기화
            // -------------------------------------------------------------------------------------
            if (Tag == IUCOA0M00::class.java.simpleName) {
                if (CustomApplication.CF_getKillApp() == CustomApplication.KillAppMode.goMain) tmp_flagFinish = false
                CustomApplication.CF_setKillApp(CustomApplication.KillAppMode.noKill)
            }
            if (tmp_flagFinish) finish()
        }
    }
    override fun onPostResume() {
        super.onPostResume()
        // -----------------------------------------------------------------------------------------
        //  Activity 실행 시 포커싱 최상단 이동
        // -----------------------------------------------------------------------------------------
        if (CommonFunction.CF_checkAccessibilityTurnOn(applicationContext)) {
            window.decorView.isFocusableInTouchMode = true
            window.decorView.requestFocus()
            window.decorView.isFocusableInTouchMode = false
        }
    }

    override fun onStop() {
        super.onStop()

        // -- bacnkground 진입
        foregroundCount -= 1
        if (receiver != null) {
            unregisterReceiver(receiver)
            receiver = null
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        LogPrinter.CF_debug("!----------------------------------------------------------")
        LogPrinter.CF_debug("!-- Activity_Default.onDestroy()")
        LogPrinter.CF_debug("!----------------------------------------------------------")

        // -----------------------------------------------------------------------------------------
        //  Handler 메시지 및 Callback 삭제
        // -----------------------------------------------------------------------------------------
        if (handler != null) {
            LogPrinter.CF_debug("!---- handler 삭제")
            handler!!.removeCallbacksAndMessages(null)
            handler = null
        }

        // -----------------------------------------------------------------------------------------
        //  ProgressDialog dismiss 및 null
        // -----------------------------------------------------------------------------------------
        if (progressDialog != null) {
            if (progressDialog!!.isShowing) progressDialog!!.dismiss()
            progressDialog = null
        }
    }
    // #############################################################################################
    //  abstract 함수
    // #############################################################################################
    /**
     * 초기 세팅 함수
     */
    protected abstract fun setInit()

    /**
     * UI 생성 및 세팅 함수
     */
    protected abstract fun setUIControl()
    // #############################################################################################
    //  솔루션 함수
    // #############################################################################################
    /**
     * 루팅 검사 함수<br></br>
     */
    private fun checkRooting() {
        LogPrinter.CF_debug("!----------------------------------------------------------")
        LogPrinter.CF_debug("!-- Activity_Default.checkRooting()")
        LogPrinter.CF_debug("!----------------------------------------------------------")

        when (CommonUtil.checkRooting(this, true)) {
            "1" ->                 //Toast.makeText(this, "루팅 단말 입니다.", Toast.LENGTH_SHORT).show();
                showCustomDialogAndAppFinish("루팅 단말 입니다. 보안문제로 인하여 앱을 종료합니다")
            "2", "3" ->                 //Toast.makeText(this, "루팅 관련 앱 활동이 탐지되었습니다.", Toast.LENGTH_SHORT).show();
                showCustomDialogAndAppFinish("루팅 관련 앱 활동이 탐지되었습니다. 보안문제로 인하여 앱을 종료합니다.")
            "4" ->                 // Toast.makeText(this, tmp_arrMessage[0]+"루팅 우회 앱이 설치되어 있습니다.", Toast.LENGTH_SHORT).show();
                showCustomDialogAndAppFinish("루팅 우회 앱이 설치되어 있습니다. 보안문제로 인하여 앱을 종료합니다")
            "6" ->                 // Toast.makeText(this, "무결성 검증에 실패 하였습니다.", Toast.LENGTH_SHORT).show();
                showCustomDialogAndAppFinish("무결성 검증에 실패 하였습니다.")
        }
    }

    /**
     * mVaccine 실시간 감시 리시버 등록
     */
    private fun setMVaccineReceiver() {
        val filter = IntentFilter()
        //tmp_filter.addAction(EnvConfig.mVaccineReceiverAction);
        filter.addAction("android.intent.action.PACKAGE_ADDED")
        filter.addAction("android.intent.action.PACKAGE_INSTALL")
        filter.addAction("android.intent.action.PACKAGE_CHANGED")
        filter.addAction("android.intent.action.PACKAGE_REPLACED")
        filter.addDataScheme("package")
        receiver = ScanReceiver()
        registerReceiver(receiver, filter)
    }
    // #############################################################################################
    //  Dialog(다이얼로그) 함수
    // #############################################################################################
    /**
     * 이미지 프로그레스 다이얼로그 show
     */
    fun CF_showProgressDialog() {
        if (!isDestroyed) {
            if (progressDialog == null) {
                progressDialog = ImageProgressDialog(this)
            }
            if (!progressDialog!!.isShowing) progressDialog!!.show()
        }
    }

    /**
     * 이미지 프로그레스 다이얼로그 dismiss
     */
    fun CF_dismissProgressDialog() {
        if (progressDialog != null && progressDialog!!.isShowing) {
            progressDialog!!.dismiss()
        }
    }

    /**
     * 커스텀 다이얼로그 기본 알럿창 show
     * @since 2019-05-29 초기생성
     * @param p_message String
     */
    fun showCustomDialogAlert(p_message: String?) {
        val customDialog = CustomDialog(this)
        customDialog.show()
        customDialog.CF_setTextContent(p_message)
        customDialog.CF_setSingleButtonText(resources.getString(R.string.btn_ok))
        customDialog.CF_setBackKeyMode(CustomDialog.BackMode.NOTUSE)
        customDialog.setOnDismissListener { }
    }

    /**
     * 커스텀 다이얼로그 show & Activity 종료
     * @since 2019-02-07 초기생성
     * @param p_message String
     */
    fun showCustomDialog(p_message: String?) {
        val customDialog = CustomDialog(this)
        customDialog.show()
        customDialog.CF_setTextContent(p_message)
        customDialog.CF_setSingleButtonText(resources.getString(R.string.btn_ok))
        customDialog.CF_setBackKeyMode(CustomDialog.BackMode.NOTUSE)
        customDialog.setOnDismissListener { finish() }
    }

    /**
     * 2020-03-10 커스텀 다이얼로그 show 함수, activity 결과값 추가
     * @param p_message String
     * @param p_result  int
     */
    fun showCustomDialog(p_message: String?, p_result: Int) {
        val customDialog = CustomDialog(this)
        customDialog.show()
        customDialog.CF_setTextContent(p_message)
        customDialog.CF_setSingleButtonText(resources.getString(R.string.btn_ok))
        customDialog.CF_setBackKeyMode(CustomDialog.BackMode.NOTUSE)
        customDialog.setOnDismissListener {
            setResult(p_result)
            finish()
        }
    }

    /**
     * 커스텀 다이얼로그 show & App 종료
     * @param p_message String
     */
    fun showCustomDialogAndAppFinish(p_message: String?) {
        if (!isFinishing) {
            val customDialog = CustomDialog(this)
            customDialog.show()
            customDialog.CF_setTextContent(p_message)
            customDialog.CF_setSingleButtonText(resources.getString(R.string.btn_ok))
            customDialog.CF_setBackKeyMode(CustomDialog.BackMode.NOTUSE)
            customDialog.setOnDismissListener { finishAffinity() }
        }
    }

    /**
     * 커스텀 다이얼로그 show 함수<br></br>
     * 확인 버튼 구성
     * @param p_message     String
     * @param p_listener    DialogInterface.OnDismissListener
     */
    fun showCustomDialog(p_message: String?, p_listener: DialogInterface.OnDismissListener?) {
        val customDialog = CustomDialog(this)
        customDialog.show()
        customDialog.CF_setTextContent(p_message)
        customDialog.CF_setBackKeyMode(CustomDialog.BackMode.CANCELED)
        customDialog.CF_setSingleButtonText(resources.getString(R.string.btn_ok))
        customDialog.setOnDismissListener(p_listener)
    }

    /**
     * 커스텀 다이얼로그 show 함수<br></br>
     * @param p_message     String
     * @param p_focusView   View
     */
    fun showCustomDialog(p_message: String?, p_focusView: View) {
        val flagIsFocusTouchEnable = p_focusView.isFocusableInTouchMode
        if (!flagIsFocusTouchEnable && CommonFunction.CF_checkAccessibilityTurnOn(this)) {
            p_focusView.isFocusableInTouchMode = true
        }
        val customDialog = CustomDialog(this)
        customDialog.show()
        customDialog.CF_setTextContent(p_message)
        customDialog.CF_setBackKeyMode(CustomDialog.BackMode.CANCELED)
        customDialog.CF_setSingleButtonText(resources.getString(R.string.btn_ok))

        // -----------------------------------------------------------------------------------------
        //  접근성 ON : 다이얼로그 종료후 포커스 이동
        //  AccessibilityEvent.TYPE_VIEW_FOCUSED 적용시 포커싱 변경이 안되는 경우가 많음.
        //  해당 view focusalbeInTouchMode true 세팅 후 requestFocus()를 이용해 포커싱.
        // -----------------------------------------------------------------------------------------
        if (CommonFunction.CF_checkAccessibilityTurnOn(this)) {
            p_focusView.isFocusableInTouchMode = true
            customDialog.setOnDismissListener {
                clearAllFocus()
                p_focusView.requestFocus()
                p_focusView.isFocusableInTouchMode = flagIsFocusTouchEnable
            }
        }
    }

    /**
     * 커스텀 다이얼로그 show 함수<br></br>
     * 취소 / 확인 : 버튼 구성
     * @param p_message     String
     * @param p_focusView   View
     */
    protected fun showCustomDialog(p_message: String?, p_focusView: View, p_listener: DialogInterface.OnDismissListener?) {
        showCustomDialog(p_message, resources.getString(R.string.btn_cancel), resources.getString(R.string.btn_ok), p_focusView, p_listener)
    }

    /**
     * 커스텀 다이얼로그 show 함수<br></br>
     * 함수 내부적으로 param View의 focusableIntouchMode true 세팅 한다.<br></br>
     * [android.content.DialogInterface.OnDismissListener] param 전달 시 param View의 focusableInTouchMode값 복원이 필요하다.
     * @param p_message     dialog text
     * @param p_btnLeft     left button text
     * @param p_btnRight    right button text
     * @param p_focusView   View
     * @param p_listener    OnDismissListener
     */
    fun showCustomDialog(p_message: String?, p_btnLeft: String?, p_btnRight: String?, p_focusView: View?, p_listener: DialogInterface.OnDismissListener?) {
        if (CommonFunction.CF_checkAccessibilityTurnOn(this)) {
            p_focusView?.isFocusableInTouchMode = true
        }
        val customDialog = CustomDialog(this)
        customDialog.show()
        customDialog.CF_setTextContent(p_message)
        customDialog.CF_setBackKeyMode(CustomDialog.BackMode.CANCELED)
        customDialog.CF_setDoubleButtonText(p_btnLeft, p_btnRight)
        customDialog.setOnDismissListener(p_listener)
    }
    // #############################################################################################
    //  기타 함수
    // #############################################################################################
    /**
     * Activity안의 Focus clear
     */
    protected fun clearAllFocus() {
        if (currentFocus != null) {
            currentFocus!!.clearFocus()
        }
        if (window.decorView != null) {
            window.decorView.clearFocus()
        }
    }

// TODO : 네트워크 체크 로직 deprecated 되어 재작업 필요 (현재 사용하는곳 없음)
//    /**
//     * @since 2019-02-07 네트워크 체크 함수 초기 생성
//     * @return connectNetwork boolean : 정상(true)/에러(false)
//     */
//    protected fun checkNetwork(): Boolean {
//        var connectNetwork = true
//
////        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
////            ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
////            // --<> (인터넷 연결됨)
////            if(cm.isDefaultNetworkActive()) {
////                LogPrinter.CF_debug("----인터넷 연결 됨(ConnectivityManager)");
////            }
////            // --<> (인터넷 연결 안됨)
////            else {
////                LogPrinter.CF_debug("----인터넷 연결 안됨(ConnectivityManager)");
////                connectNetwork = false;
////            }
////        }
////        else {
//        val connectivityManager = getSystemService(CONNECTIVITY_SERVICE) as ConnectivityManager
//        val networkInfo = connectivityManager.activeNetworkInfo
//        if (networkInfo != null && networkInfo.isConnected) {
//            if (networkInfo.type == ConnectivityManager.TYPE_WIFI) {
//                // TODO NJM WIFI 처리 (필요시)
//                //Toast.makeText(this, "WIFI 접속중 입니다..", Toast.LENGTH_LONG).show();
//            } else if (networkInfo.type == ConnectivityManager.TYPE_MOBILE) {
//                // TODO NJM 모바일데이터 처리 (필요시)
//                //Toast.makeText(this, "모바일데이터 접속중 입니다.", Toast.LENGTH_LONG).show();
//            }
//        } else {
//            //Toast.makeText(this, "네트워크 상태 에러 입니다..", Toast.LENGTH_LONG).show();
//            connectNetwork = false
//        }
//        //      }
//        if (!connectNetwork) {
//            showCustomDialogAndAppFinish("네트워크 오류입니다. 네트워크 상태를 확인해주십시오. 앱을 종료합니다.")
//        }
//        return connectNetwork
//    }

    protected fun CF_finishCancelMsg(msg: String?) {
        LogPrinter.CF_debug("!----------------------------------------------------------")
        LogPrinter.CF_debug("!-- Activity_Default.CF_finishCancelMsg() --에러 콜백 세팅")
        LogPrinter.CF_debug("!----------------------------------------------------------")
        val rtnIntent = Intent()
        rtnIntent.putExtra(EnvConstant.KEY_INTENT_RTN_MSG, msg)
        setResult(RESULT_CANCELED, rtnIntent)
        finish()
    }

    companion object {
        // -- TODO : NJM foreground / background 체크용 추후 다른방식으로 변경하시길
        var foregroundCount = 0
    }
}