package com.epost.insu.fragment

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.os.Message
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import com.epost.insu.R
import com.epost.insu.activity.auth.IUPC90M00_P
import com.epost.insu.common.CommonFunction
import com.epost.insu.network.WeakReferenceHandler
import com.epost.insu.network.WeakReferenceHandler.ObjectHandlerMessage
import java.util.*

/**
 * @copyright : (주)프로니스정보기술 | http://www.phronis.co.kr
 *
 * @project   : 모바일슈랑스 구축
 * @pakage    : com.epost.insu.activity
 * @fileName  : IUPC71M00_F.java
 *
 * @Title     : 공통 > 휴대폰 본인인증 >> 02. 휴대폰 인증번호 확인 (화면 ID : iupc91m00_f)
 * @author    : 이수행
 * @created   : 2017-09-05
 * @version   : 1.0
 *
 * @note      : 휴대폰 본인인증 인증번호 입력 화면
 * ======================================================================
 * 수정 내역
 * NO       날짜          작업자       내용
 * 01       2017-09-05    이수행     : 최초 등록
 * =======================================================================
 */
class IUPC91M00_F : Fragment_Default(), ObjectHandlerMessage {
    private val maxLengthOfSmsCode: Int = 6
    private val limitTimeMilli: Long = 180000
    private val HANDLERJOB_UPDATE_TIME: Int = 0
    private var flagTimeOver: Boolean = false
    private var startTimeMilli: Long = 0
    private var textTitmer: TextView? = null
    private var edtCode: EditText? = null
    private var mActivity: IUPC90M00_P? = null
    private var timer: Timer? = null
    private var handler: WeakReferenceHandler? = null

    override fun handleMessage(p_message: Message) {
        when (p_message.what) {
            HANDLERJOB_UPDATE_TIME -> if (textTitmer != null) {
                textTitmer!!.setText(timeRemaining)
            }
            else -> {
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setInit()
    }

    /**
     * 초기 세팅 함수
     */
    private fun setInit() {
        startTimeMilli = System.currentTimeMillis()
        flagTimeOver = false
        handler = WeakReferenceHandler(this)
    }

    @SuppressLint("InflateParams")
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.iupc91m00_f, null)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mActivity = getActivity() as IUPC90M00_P?
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        setUIControl()
        if (savedInstanceState != null) {
            flagTimeOver = savedInstanceState.getBoolean("flagTimeOver")
            startTimeMilli = savedInstanceState.getLong("startTimeMilli")
            val tmp_flagRunningTimer: Boolean = savedInstanceState.getBoolean("flagRunningTimer")
            if (tmp_flagRunningTimer && flagTimeOver == false) {
                startTimer(startTimeMilli)
            }
        }
    }

    /**
     * UI 생성 및 세팅 함수
     */
    private fun setUIControl() {
        edtCode = getView()!!.findViewById(R.id.edtitext)
        edtCode?.setFilters(CommonFunction.CF_getInputLengthFilter(maxLengthOfSmsCode))
        edtCode?.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable) {
                // 키패드 hide
                if (s.length == maxLengthOfSmsCode) {
                    CommonFunction.CF_closeVirtualKeyboard(getActivity(), edtCode!!.getWindowToken())
                }
            }
        })
        textTitmer = getView()!!.findViewById(R.id.textView)
        val tmp_btnRetry: Button = getView()!!.findViewById(R.id.btnRetry)
        tmp_btnRetry.setOnClickListener(object : View.OnClickListener {
            override fun onClick(v: View) {
                CF_stopTimer()
                mActivity!!.httpReq_mobile_auth1()
            }
        })
        val tmp_btnOk: Button = getView()!!.findViewById(R.id.btnOk)
        tmp_btnOk.setOnClickListener(object : View.OnClickListener {
            override fun onClick(v: View) {
                if (flagTimeOver) {
                    // -- 제한시간 초과
                    showCustomDialog(getResources().getString(R.string.dlg_timeover_sms), tmp_btnOk)
                } else {
                    if (edtCode?.getText().toString().trim({ it <= ' ' }).length > 0) {
                        mActivity!!.httpReq_mobileAuth2(edtCode?.getText().toString().trim({ it <= ' ' }))
                    } else {
                        showCustomDialog(getResources().getString(R.string.dlg_no_sms), edtCode!!)
                    }
                }
            }
        })
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putBoolean("flagTimeOver", flagTimeOver)
        outState.putLong("startTimeMilli", startTimeMilli)
        outState.putBoolean("flagRunningTimer", timer != null)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        if (timer != null) {
            timer!!.cancel()
            timer = null
        }
    }

    /**
     * 남은 시간 반환 함수
     * @return      String
     */
    private val timeRemaining: String
        get() {
            val tmp_strTime: String
            val tmp_currentTimeMilli: Long = System.currentTimeMillis()
            val tmp_limitTimeMilli: Long = limitTimeMilli - (tmp_currentTimeMilli - startTimeMilli)
            if (tmp_limitTimeMilli <= 0) {
                flagTimeOver = true
                if (timer != null) {
                    timer!!.cancel()
                    timer = null
                }
                tmp_strTime = "00:00"
            } else {
                val tmp_minute: Int = ((tmp_limitTimeMilli / (1000 * 60)) % 60).toInt()
                val tmp_second: Int = (tmp_limitTimeMilli / 1000).toInt() % 60
                tmp_strTime = String.format(Locale.getDefault(), "%02d:%02d", tmp_minute, tmp_second)
            }
            return tmp_strTime
        }

    /**
     * 타이머 시작 함수
     * @param p_startTime   Long
     */
    private fun startTimer(p_startTime: Long) {
        flagTimeOver = false
        startTimeMilli = p_startTime
        timer = Timer()
        timer!!.schedule(object : TimerTask() {
            override fun run() {
                if (timer != null) {
                    handler!!.sendEmptyMessage(HANDLERJOB_UPDATE_TIME)
                } else {
                    cancel()
                }
            }
        }, 0L, 1000L)
        textTitmer!!.setVisibility(View.VISIBLE)
    }

    /**
     * 타이머 시작 함수
     */
    fun CF_startTimer() {
        startTimer(System.currentTimeMillis())
    }

    /**
     * 타이머 정지 함수
     */
    fun CF_stopTimer() {
        if (timer != null) {
            timer!!.cancel()
            timer = null
        }
        textTitmer!!.setVisibility(View.INVISIBLE)
    }

    /**
     * 인증번호 입력 EditText 반환
     * @return      EditText
     */
    fun CF_getInputBox(): EditText? {
        return edtCode
    }
}