package com.epost.insu

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import android.view.View
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.epost.insu.activity.*
import com.epost.insu.activity.BC.IUBC01M00
import com.epost.insu.activity.BC.IUBC10M00_P
import com.epost.insu.activity.auth.*
import com.epost.insu.common.CommonFunction
import com.epost.insu.common.CustomSQLiteFunction
import com.epost.insu.common.LogPrinter
import com.epost.insu.dialog.CustomDialog

class   IntentManager{



    companion object {
        //외부에서 값 변경 Activity_WebStart.kt
        var isWebViewCall = false
        var menuNo = ""
        var menuLink = ""

        fun loginCompleteToMain(activity: Activity){
            LogPrinter.CF_debug("!----------------------------------------------------------")
            LogPrinter.CF_debug("!-- loginCompleteToMain --")
            LogPrinter.CF_debug("!----------------------------------------------------------")


            var intent = Intent(activity, IUCOA0M00::class.java)
            intent.putExtra("menuLink",menuLink)
            intent.putExtra("menuNo",menuNo)
            intent.putExtra(EnvConstant.KEY_INTENT_LAUNCH_MODE,"webLogin")
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            activity.startActivity(intent)

        }


        public fun moveTaskToBack(activity: Activity_Default,boolean: Boolean){
            //웹뷰에서 실행시킨것이아니라면 백그라운드로이동
            if(!isWebViewCall){
                activity.moveTaskToBack(boolean)
            }
        }
        /**
         * 인증센터 Activity 호출 함수
         */
        public fun startIUPC30M00(activity: Activity) {
            val intent = Intent(activity, IUFC30M00::class.java)
            activity.startActivity(intent)
        }


        /**
         * 로그인목록 Activity 호출 함수<br></br>
         */
        public fun startIUCOB0M00_Login(activity: Activity,requestCode:Int) {
            val intent = Intent(activity, IUCOB0M00::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
            activity.startActivityForResult(intent, requestCode)
        }

        /**
         * 공동인증 로그인 Activity 호출(웹 요청)
         * @param p_key String
         */
        public fun startIUCOC0M00_Web(activity: Activity,p_key: String) {
            val intent = Intent(activity, IUCOC0M00_Web::class.java)
            intent.putExtra("key", p_key)
            activity.startActivity(intent)
        }

        /**
         * 공동인증 전자서명 Activity 호출(웹 요청)
         * @param p_key String
         * @param p_name String
         * @param p_rnno_enc String
         */
        public fun startIUPC80M10_WebActivity(activity: Activity,p_key: String, p_name: String, p_rnno_enc: String) {
            val intent = Intent(activity, IUPC80M10_Web::class.java)
            intent.putExtra("key", p_key)
            intent.putExtra("name", p_name)
            intent.putExtra("rnno_enc", p_rnno_enc)
            activity.startActivity(intent)
        }

        /**
         * 간편인증(핀/지문/패턴) 로그인/전자서명 Activity 호출(웹 요청)
         * @param authTechCode String
         */
        public fun startIUFC00M00_Auth(activity: Activity,authTechCode: String, authMode: EnvConfig.AuthMode, p_key: String) {
            LogPrinter.CF_debug("!----------------------------------------------------------")
            LogPrinter.CF_debug("!-- IUCOA0M00.startIUFC00M00_Auth() --간편인증 Activity 호출")
            LogPrinter.CF_debug("!----------------------------------------------------------")

            if (CustomSQLiteFunction.hasUserCsno(activity)) {
                val intent = Intent(activity, IUFC00M00::class.java)
                intent.putExtra(EnvConstant.KEY_INTENT_AUTH_MODE, authMode)
                intent.putExtra(EnvConstant.KEY_INTENT_AUTH_TECH_CODE, authTechCode)
                intent.putExtra("key", p_key)

                intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
                activity.startActivityForResult(intent, EnvConfig.REQUESTCODE_ACTIVITY_IUFC00M00)
            }
            else {
                CommonFunction.CF_showCustomAlertDilaog(activity, activity.resources.getString(R.string.dlg_empty_csno), activity.resources.getString(R.string.btn_ok))
            }
        }

        /**
         * 설정 Activity 호출 함수
         */
        public fun startIUCOG0M00_Activity(activity: Activity) {
            val intent = Intent(activity, IUCOG0M00::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
            activity.startActivity(intent)
        }

        /**
         * 퍼미션 권한 변경을 위한 설정 Activity 이동 함수
         */
        public fun startAppSettingActivity(activity: Activity) {
            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
            val uri = Uri.fromParts("package", activity.packageName, null)
            intent.data = uri
            activity.startActivity(intent)
        }

        /**
         * 보험금지급청구 첫화면 Activity 호출 함수
         */
        public fun startIUII01M00_Activity(activity: Activity) {
            val intent = Intent(activity, IUII01M00::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
            activity.startActivity(intent)
        }

        /**
         * 보험금청구접수신청 Activity 호출 함수(웹요청)
         * Web에서 보험금청구 요청시에 호출된다.
         */
        public fun startIUII10M00_P_Activity(activity: Activity) {
            val intent = Intent(activity, IUII10M00_P::class.java)
            activity.startActivity(intent)
        }

        /**
         * 자녀 보험금청구 Activity 호출(웹요청)
         */
        public fun startIUII90M00_P_Activity(activity: Activity) {
            val intent = Intent(activity, IUII90M00_P::class.java)
            activity.startActivity(intent)
        }

        /**
         * 보험금 청구내역 조회 Activity 호출 함수
         */
        public fun startIUII50M00_P(activity: Activity) {
            // --<> (로그인)
            if (SharedPreferencesFunc.getFlagLogin(activity)) {
                val intent = Intent(activity, IUII50M00_P::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                activity.startActivity(intent)
            }
            // --<> (비로그인)
            else {
                startIUII01M00_Activity(activity) // 청구 첫화면
            }
        }

        /**
         * 서비스 이용약관 Activity 호출 함수
         */
        public fun startIUCOG0M01(activity: Activity) {
            val intent = Intent(activity, IUCOG0M01::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
            activity.startActivity(intent)
        }



//    /**
//     * 간편인증관리 버튼 click 함수
//     * @param p_view View
//     */
//    fun onClickManage_S_Auth(p_view: View?) {
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//            val intent = Intent(activity, IUFC30M00::class.java)
//            intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
//            activity.startActivity(intent)
//        } else {
//            showCustomDialog(resources.getString(R.string.label_no_simple_login_device), p_view!!)
//        }
//    }

        /**
         * 스마트보험금청구접수신청 Activity 호출 함수
         */
        public fun startIUBC10M00_Activity(activity: Activity,hospital_code: String) {
            // --<2> (청구가능) 청구불가시 메시지 팝업
            if (EnvConfig.isPayEnableHour(activity)) {
                if (SharedPreferencesFunc.getFlagLogin(activity)) {
                    val intent = Intent(activity, IUBC10M00_P::class.java)
                    activity.startActivity(intent)
                } else {
                    val customDialog = CustomDialog(activity)
                    customDialog.show()
                    customDialog.CF_setBackKeyMode(CustomDialog.BackMode.NOTUSE)
                    customDialog.CF_setTextContent(activity.resources.getString(R.string.dlg_need_login_for_req_pay))
                    customDialog.CF_setSingleButtonText(activity.resources.getString(R.string.btn_ok))
                    customDialog.setOnDismissListener {
                        val intent = Intent(activity, IUBC01M00::class.java)
                        SharedPreferencesFunc.setSmartReqHospitalCode(activity, hospital_code)
                        intent.putExtra("isSmartReqPay", true)
                        activity.startActivity(intent)
                    }
                }
            }
        }




        //외부 앱 호출


        // #############################################################################################
        //  XML에 정의된 onClick function
        // #############################################################################################
        /**
         * 우체국 스마트뱅킹 App 호출
         * @param p_view View
         */
        fun onClickLaunchSmartBankApp(activity: Activity_Default) {
            // --<1> 우체국스마트뱅킹 App
            if (checkSmartBankAppInstalled(activity)) {
                // --<2>
                if (CommonFunction.CF_checkAccessibilityTurnOn(activity)) {
                    activity.showCustomDialog(activity.resources.getString(R.string.dlg_accessible_move_smartbank_app)
                    ) { dialog ->
                        if (!(dialog as CustomDialog).CF_getCanceled()) {
                            val intent = activity.packageManager.getLaunchIntentForPackage(EnvConfig.epostBankAppPackage)
                            intent?.let { activity.startActivity(it) }
                        } else {

                        }

                    }
                } else {
                    val intent = activity.packageManager.getLaunchIntentForPackage(EnvConfig.epostBankAppPackage)
                    intent?.let { activity.startActivity(it) }
                }
            } else {
                // --<2> 접근성 On/Off 에 따른 메시지 분기 처리
                val message: String = if (CommonFunction.CF_checkAccessibilityTurnOn(activity)) {
                    activity.resources.getString(R.string.dlg_accessible_move_smartbank_market)
                } else {
                    activity.resources.getString(R.string.dlg_down_bank_app)
                }
                activity.showCustomDialog(message) { dialog ->
                    if (!(dialog as CustomDialog).CF_getCanceled()) {
                        val intentUpdate = Intent(Intent.ACTION_VIEW)
                        intentUpdate.data = Uri.parse("market://details?id=" + EnvConfig.epostBankAppPackage)
                        activity.startActivity(intentUpdate)
                    } else if (CommonFunction.CF_checkAccessibilityTurnOn(activity)) {

                    }
                }
            }
        }

        /**
         * 스마트뱅킹 앱 설치 확인
         * @return tmp_flagInstalled
         */
        private fun checkSmartBankAppInstalled(activity: Activity): Boolean {
            var flagInstalled = false
            val packageInfos = activity.packageManager.getInstalledPackages(0)
            for (packageInfo in packageInfos) {
                if (packageInfo.packageName == EnvConfig.epostBankAppPackage) {
                    flagInstalled = true
                    break
                }
            }
            return flagInstalled
        }

        /**
         * 포스트페이 App 호출
         * @param p_view View
         */
        fun onClickLaunchPostPayApp(activity: Activity_Default) {
            var isInstalled = false
            val packageInfos = activity.packageManager.getInstalledPackages(0)
            for (packageInfo in packageInfos) {
                if (packageInfo.packageName == EnvConfig.postPayAppPackage) {
                    isInstalled = true
                    break
                }
            }

            // --<1> (앱 설치됨)
            if (isInstalled) {
                // --<2> (접근성ON)
                if (CommonFunction.CF_checkAccessibilityTurnOn(activity)) {
                    activity.showCustomDialog(activity.resources.getString(R.string.dlg_accessible_move_postpay_app)
                    ) { dialog ->
                        if (!(dialog as CustomDialog).CF_getCanceled()) {
                            val intent = activity.packageManager.getLaunchIntentForPackage(EnvConfig.postPayAppPackage)
                            intent?.let { activity.startActivity(it) }
                        } else {

                        }

                    }
                } else {
                    val intent = activity.packageManager.getLaunchIntentForPackage(EnvConfig.postPayAppPackage)
                    intent?.let { activity.startActivity(it) }
                }
            } else {
                // --<2> (접근성ON)
                val message: String = if (CommonFunction.CF_checkAccessibilityTurnOn(activity)) {
                    activity.resources.getString(R.string.dlg_accessible_move_postpay_market)
                } else {
                    activity.resources.getString(R.string.dlg_down_postpay_app)
                }
                activity.showCustomDialog(message) { dialog ->
                    if (!(dialog as CustomDialog).CF_getCanceled()) {
                        val intentUpdate = Intent(Intent.ACTION_VIEW)
                        intentUpdate.data = Uri.parse("market://details?id=" + EnvConfig.postPayAppPackage)
                        activity.startActivity(intentUpdate)
                    } else if (CommonFunction.CF_checkAccessibilityTurnOn(activity)) {

                    }
                }
            }
        }


    }







}