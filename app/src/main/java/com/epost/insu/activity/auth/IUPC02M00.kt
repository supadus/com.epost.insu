package com.epost.insu.activity.auth

import android.content.DialogInterface
import android.os.Bundle
import android.os.Message
import android.text.TextUtils
import android.view.View
import android.widget.*
import com.dreamsecurity.magicxsign.MagicXSign_Type
import com.epost.insu.*
import com.epost.insu.activity.Activity_Default
import com.epost.insu.common.CommonFunction
import com.epost.insu.common.XSignHelper
import com.epost.insu.dialog.CustomDialog
import com.epost.insu.network.WeakReferenceHandler
import com.epost.insu.network.WeakReferenceHandler.ObjectHandlerMessage
import com.raonsecure.ksw.RSKSWCertManager
import com.raonsecure.ksw.RSKSWICRProtocol
import java.io.IOException
import java.util.*

/**
 * 인증센터 > 공동인증 > 공동인증서 가져오기
 * @since     :
 * @version   : 1.1
 * @author    : NJM
 * <pre>
 *      1. 인증번호 생성 요청
 *      2. 인증서 가져오기 버튼 클릭시 인증서 저장
 *      (단, 사용자가 PC에 접속해 인증서 선택 및 인증번호 입력을 하였을 경우만)
 * ======================================================================
 *          NJM_20170807    최초 등록
 * 1.6.1    NJM_20210722    인증번호 생성 오류 발생시 예외처리 추가
 * 1.6.3    NJM_20211007    [API30 대응] targetApi 29 -> 30 변경에 따른 코틀린 오류 수정
 * 1.6.3    NJM_20211008    [API30 대응] 솔루션 업데이트 반영 (인증서 공용 -> 내부 복사 로직 추가)
 * =======================================================================
 * copyright : (주)프로니스정보기술 | http://www.phronis.co.kr
 * </pre>
 */
class IUPC02M00 : Activity_Default(), ObjectHandlerMessage {
    private val handlerJob_generated_number: Int          = 1 // 인증번호 12자리 생성
    private val handlerjob_get_certificate: Int           = 2 // 인증서 가져오기
    private val handlerjob_error_network_number: Int      = 3 // 인증번호 생성 에러(네트워크)
    private val handlerjob_error_cretae_number: Int       = 4 // 인증번호 생성 에러
    private val handlerjob_error_network_certificate: Int = 5 // 인증서 가져오기 에러(네트워크)
    private val handlerjob_error_save: Int                = 6 // 인증서 저장 에러
    private val handlerjob_error_save_2: Int              = 7 // 인증서 저장 에러

    private var icrp: RSKSWICRProtocol? = null // 인증서 관리 솔루션
    private var manager: RSKSWCertManager? = null // 인증서 관리 솔루션

    private var btnGet: Button? = null

    override fun handleMessage(p_message: Message) {
        if (!isDestroyed()) {
            CF_dismissProgressDialog()
            when (p_message.what) {
                handlerJob_generated_number -> setNumber(p_message.obj as String)
                handlerjob_get_certificate -> showAlertDlg(getResources().getString(R.string.dlg_get_certificate), true)
                handlerjob_error_cretae_number -> showAlertDlg(getResources().getString(R.string.dlg_error_create_number), true)
                handlerjob_error_network_number -> showAlertDlg(getResources().getString(R.string.dlg_error_network_create_number), true)
                handlerjob_error_network_certificate -> showDlgOfGetCertificateError(getResources().getString(R.string.dlg_error_get_certificate))
                handlerjob_error_save -> showDlgOfGetCertificateError(getResources().getString(R.string.dlg_error_get_certificate))
                handlerjob_error_save_2 -> btnGet?.let { showCustomDialog(getResources().getString(R.string.dlg_error_no_number), it) }
                else -> {
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 인증번호 생성 요청
        requestCreateNumber()
    }

    override fun onBackPressed() {
        val customDialog = CustomDialog(this@IUPC02M00)
        customDialog.show()
        customDialog.CF_setTextContent(getResources().getString(R.string.dlg_cancel_get_certificate))
        customDialog.CF_setDoubleButtonText(getResources().getString(R.string.btn_no), getResources().getString(R.string.btn_yes))
        customDialog.CF_setBackKeyMode(CustomDialog.BackMode.CANCELED)
        customDialog.setOnDismissListener(object : DialogInterface.OnDismissListener {
            override fun onDismiss(dialog: DialogInterface) {
                if (!(dialog as CustomDialog).CF_getCanceled()) {
                    finish()
                }
            }
        })
    }

    override fun setInit() {
        setContentView(R.layout.iupc02m00)
        handler = WeakReferenceHandler(this)
        manager = RSKSWCertManager.getInstance(this)

        // 보안 레벨 설정 : SHA1
        RSKSWICRProtocol.securityLevel = RSKSWICRProtocol.RSKSWConstCRSecLevel_SHA1
        icrp = RSKSWICRProtocol(EnvConfig.keywireless_ip, EnvConfig.keywireless_port)
    }

    override fun setUIControl() {
        setTitleBarUI()
        btnGet = findViewById<View>(R.id.btnFill) as Button?
        btnGet!!.setText(getResources().getString(R.string.btn_get_cert))
        btnGet!!.setOnClickListener(object : View.OnClickListener {
            override fun onClick(v: View) {
                requestCertificate()
            }
        })
    }

    override fun onSaveInstanceState(p_bundle: Bundle) {
        super.onSaveInstanceState(p_bundle)
        val tmp_textNumber_1: TextView = findViewById<View>(R.id.activity_getCert_textNumber_1) as TextView
        val tmp_textNumber_2: TextView = findViewById<View>(R.id.activity_getCert_textNumber_2) as TextView
        val tmp_textNumber_3: TextView = findViewById<View>(R.id.activity_getCert_textNumber_3) as TextView
        p_bundle.putString("number_1", tmp_textNumber_1.getText().toString())
        p_bundle.putString("number_2", tmp_textNumber_2.getText().toString())
        p_bundle.putString("number_3", tmp_textNumber_3.getText().toString())
    }

    override fun onRestoreInstanceState(p_bundle: Bundle) {
        super.onRestoreInstanceState(p_bundle)
        if (p_bundle.containsKey("number_1")) {
            val tmp_textNumber_1: TextView = findViewById<View>(R.id.activity_getCert_textNumber_1) as TextView
            tmp_textNumber_1.setText(p_bundle.getString("number_1"))
            tmp_textNumber_1.setContentDescription("인증번호 앞 네 자리 " + tmp_textNumber_1.getText() + " 입력")
        }
        if (p_bundle.containsKey("number_2")) {
            val tmp_textNumber_2: TextView = findViewById<View>(R.id.activity_getCert_textNumber_2) as TextView
            tmp_textNumber_2.setText(p_bundle.getString("number_2"))
            tmp_textNumber_2.setContentDescription("인증번호 중간 네 자리 " + tmp_textNumber_2.getText() + " 입력")
        }
        if (p_bundle.containsKey("number_3")) {
            val tmp_textNumber_3: TextView = findViewById<View>(R.id.activity_getCert_textNumber_3) as TextView
            tmp_textNumber_3.setText(p_bundle.getString("number_3"))
            tmp_textNumber_3.setContentDescription("인증번호 마지막 네 자리 " + tmp_textNumber_3.getText() + " 입력")
        }
    }

    /**
     * 타이틀바 레이아웃 세팅
     */
    private fun setTitleBarUI() {
        // 타이틀 세팅
        val tmp_title: TextView = findViewById<View>(R.id.title_bar_textTitle) as TextView
        tmp_title.setText(getResources().getString(R.string.title_get_certification))

        // left 버튼 세팅
        val tmp_btnLeft: ImageButton = findViewById<View>(R.id.title_bar_imgBtnLeft) as ImageButton
        tmp_btnLeft.setVisibility(View.VISIBLE)
        tmp_btnLeft.setOnClickListener(object : View.OnClickListener {
            override fun onClick(v: View) {
                showCustomDialog(getResources().getString(R.string.dlg_cancel_get_certificate),
                    getResources().getString(R.string.btn_no),
                    getResources().getString(R.string.btn_yes),
                    tmp_btnLeft,
                    object : DialogInterface.OnDismissListener {
                        override fun onDismiss(dialog: DialogInterface) {
                            if ((dialog as CustomDialog).CF_getCanceled() == false) {
                                finish()
                            } else if (CommonFunction.CF_checkAccessibilityTurnOn(this@IUPC02M00)) {
                                clearAllFocus()
                                tmp_btnLeft.requestFocus()
                            }
                            tmp_btnLeft.setFocusableInTouchMode(false)
                        }
                    })
            }
        })
    }

    /**
     * 생성된 인증번호 세팅함수
     * @param p_generatedNumber String
     */
    private fun setNumber(p_generatedNumber: String) {
        if (!TextUtils.isEmpty(p_generatedNumber) && p_generatedNumber.length == 12) {
            val tmp_textNumber_1: TextView = findViewById<View>(R.id.activity_getCert_textNumber_1) as TextView
            val tmp_textNumber_2: TextView = findViewById<View>(R.id.activity_getCert_textNumber_2) as TextView
            val tmp_textNumber_3: TextView = findViewById<View>(R.id.activity_getCert_textNumber_3) as TextView
            val tmp_number_1: String = p_generatedNumber.substring(0, 4)
            val tmp_number_2: String = p_generatedNumber.substring(4, 8)
            val tmp_number_3: String = p_generatedNumber.substring(8, 12)
            tmp_textNumber_1.setText(tmp_number_1)
            tmp_textNumber_2.setText(tmp_number_2)
            tmp_textNumber_3.setText(tmp_number_3)
            tmp_textNumber_1.setContentDescription("인증번호 앞 네 자리 " + tmp_number_1 + " 입력")
            tmp_textNumber_2.setContentDescription("인증번호 중간 네 자리 " + tmp_number_2 + " 입력")
            tmp_textNumber_3.setContentDescription("인증번호 마지막 네 자리 " + tmp_number_3 + " 입력")
        }
    }

    // #############################################################################################
    //  Dialog
    // #############################################################################################
    /**
     * 메시지를 팝업으로 보이고 확인시 Activity를 종료한다.
     * @param p_message String
     */
    private fun showAlertDlg(p_message: String, p_flagActivity: Boolean) {
        val tmp_dlg = CustomDialog(this)
        tmp_dlg.show()
        tmp_dlg.CF_setTextContent(p_message)
        tmp_dlg.CF_setBackKeyMode(CustomDialog.BackMode.NOTUSE)
        tmp_dlg.CF_setSingleButtonText(getResources().getString(R.string.btn_ok))
        if (p_flagActivity) {
            tmp_dlg.setOnDismissListener(object : DialogInterface.OnDismissListener {
                override fun onDismiss(dialog: DialogInterface) {
                    finish()
                }
            })
        }
    }

    /**
     * 공동인증서 가져오기 error 다이얼로그
     * @param p_message
     */
    private fun showDlgOfGetCertificateError(p_message: String) {
        val tmp_dlg = CustomDialog(this)
        tmp_dlg.show()
        tmp_dlg.CF_setBackKeyMode(CustomDialog.BackMode.NOTUSE)
        tmp_dlg.CF_setTextContent(p_message)
        tmp_dlg.CF_setSingleButtonText(getResources().getString(R.string.btn_ok))
        tmp_dlg.setOnDismissListener(object : DialogInterface.OnDismissListener {
            override fun onDismiss(dialog: DialogInterface) {
                requestCreateNumber()
            }
        })
    }

    // #############################################################################################
    //  Http 관련
    // #############################################################################################
    /**
     * 인증번호 생성 요청 함수
     */
    private fun requestCreateNumber() {
        CF_showProgressDialog()

        Thread(object : Runnable {
            override fun run() {
                try {
                    var messageHashtable: Hashtable<*, *> = Hashtable<String, Any>()

                    // 인증 번호 생성 요청
                    messageHashtable = icrp!!.import1()
                    val tmp_codeR1: String? = messageHashtable.get("CODE") as String?
                    val tmp_msgR1: String? = messageHashtable.get("MESSAGE") as String?

                    if ((tmp_codeR1 == "SC200")) {
                        // 인증번호 생성 성공
                        val tmp_number: String = icrp!!.generatedNumber()
                        handler?.sendMessage(handler!!.obtainMessage(handlerJob_generated_number, tmp_number))
                    } else {
                        if ((tmp_codeR1 == "PT118")) {
                            handler?.sendMessage(handler?.obtainMessage(handlerjob_error_cretae_number)!!)
                        }
                        else if ((tmp_codeR1 == "NT110")) { // tmp_msgR1 : "서버 연결에 실패했습니다."
                            handler?.sendMessage(handler?.obtainMessage(handlerjob_error_cretae_number)!!)
                        }
                        else {
                            handler?.sendMessage(handler?.obtainMessage(handlerjob_error_network_number)!!)
                        }
                    }
                } catch (e: Exception) {
                    handler?.sendMessage(handler?.obtainMessage(handlerjob_error_cretae_number)!!)
                }
            }
        }).start()
    }

    /**
     * 인증서 저장 요청
     */
    private fun requestCertificate() {
        CF_showProgressDialog()
        Thread(object : Runnable {
            override fun run() {
                var messageHashtable: Hashtable<*, *> = Hashtable<String, Any>()
                // 인증서 가져오기 실제 API
                messageHashtable = icrp!!.import2()
                val tmp_codeR2: String? = messageHashtable.get("CODE") as String?
                val tmp_messageR2: String? = messageHashtable.get("MESSAGE") as String?
                val userCertByte: ByteArray
                val userKeyByte: ByteArray
                val userKmCertByte: ByteArray
                val userKmKeyByte: ByteArray
                var tmp_flagisSave = false

                manager!!.certSavingMode = RSKSWCertManager.CERT_IN_SDCARD
                if ((tmp_codeR2 == "SC201")) {
                    userCertByte = icrp!!.cert
                    userKeyByte = icrp!!.key
                    try {
                        // 모바일 기기에 인증서 저장
                        tmp_flagisSave = manager!!.saveCert(userCertByte, userKeyByte)

                        // ----NEW
                        val xSignHelper = XSignHelper(this@IUPC02M00, EnvConfig.xSignDebugLevel)
                        xSignHelper.CF_insert(MagicXSign_Type.XSIGN_PKI_CERT_SIGN, userCertByte, userKeyByte)
                        // ----//NEW

                        if (tmp_flagisSave) {
                            handler?.sendMessage(handler?.obtainMessage(handlerjob_get_certificate)!!)
                        } else {
                            handler?.sendMessage(handler?.obtainMessage(handlerjob_error_save)!!)
                        }
                    } catch (e: IOException) {
                        handler?.sendMessage(handler?.obtainMessage(handlerjob_error_save)!!)
                    }
                }
                else if ((tmp_codeR2 == "SC203")) {
                    userCertByte = icrp!!.cert
                    userKeyByte = icrp!!.key
                    userKmCertByte = icrp!!.kmCert
                    userKmKeyByte = icrp!!.kmKey

                    try {
                        // 모바일 기기에 인증서 저장
                        tmp_flagisSave = manager!!.saveCert(userCertByte, userKeyByte, userKmCertByte, userKmKeyByte)

                        // ----NEW
                        val xSignHelper = XSignHelper(this@IUPC02M00, EnvConfig.xSignDebugLevel)
                        xSignHelper.CF_insert(MagicXSign_Type.XSIGN_PKI_CERT_SIGN, userCertByte, userKeyByte)
                        // ----//NEW

                        if (tmp_flagisSave) {
                            handler?.sendMessage(handler?.obtainMessage(handlerjob_get_certificate)!!)
                        } else {
                            handler?.sendMessage(handler?.obtainMessage(handlerjob_error_save)!!)
                        }
                    } catch (e: IOException) {
                        handler?.sendMessage(handler?.obtainMessage(handlerjob_error_save)!!)
                    }
                }
                else if ((tmp_codeR2 == "PT115")) {
                    handler?.sendMessage(handler?.obtainMessage(handlerjob_error_save_2)!!)
                } /*
                else if (tmp_codeR2.equals("NT300")) {
                    handler.sendMessage(handler.obtainMessage(handlerjob_error_network));
                }
                */ else {
                    handler?.sendMessage(handler?.obtainMessage(handlerjob_error_network_certificate)!!)
                }
            }
        }).start()
    }
}