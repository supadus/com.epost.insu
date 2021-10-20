package com.epost.insu.fragment

import android.content.DialogInterface
import android.view.View
import androidx.fragment.app.Fragment
import com.epost.insu.R
import com.epost.insu.common.CommonFunction
import com.epost.insu.dialog.CustomDialog

/**
 * @copyright : (주)프로니스정보기술 | http://www.phronis.co.kr
 *
 * @project   : 모바일슈랑스 구축
 * @pakage   : com.epost.insu.fragment
 * @fileName  : Fragment_Default.java
 *
 * @Title     : App 내에서 사용하는 Fragment 상위 클래스
 * @author    : 이수행
 * @created   : 2017-11-17
 * @version   : 1.0
 *
 * @note      : <u>App 내에서 사용하는 Fragment 상위 클래스</u><br></br>
 * ======================================================================
 * 수정 내역
 * NO      날짜          작업자       내용
 * 01      2017-11-17    이수행       최초 등록
 * =======================================================================
 */
open class Fragment_Default : Fragment() {
    /**
     * Activity안의 Focus clear
     */
    fun clearAllFocus() {
        if (activity != null) {
            if (activity!!.window.currentFocus != null) {
                activity!!.window.currentFocus!!.clearFocus()
            }
            if (activity!!.window.decorView != null) {
                activity!!.window.decorView.clearFocus()
            }
        }
    }

    /**
     * 커스텀 다이얼로그 show 함수<br></br>
     * 접근성 포커스 이동을 위해 param View의 focusableInTouchMode 값 검사 후<br></br>
     * param View의 focusableInTouchMode true 세팅 ==> Dialog dismiss 시점에 false 세팅
     * @param p_message     String, 메시지
     * @param p_focusView   View, 접근성 포커스 이동 View
     */
    fun showCustomDialog(p_message: String?, p_focusView: View) {
        val tmp_flagIsFocusTouchEnable = p_focusView.isFocusableInTouchMode
        if (tmp_flagIsFocusTouchEnable == false && CommonFunction.CF_checkAccessibilityTurnOn(activity)) {
            p_focusView.isFocusableInTouchMode = true
        }
        val tmp_dlg = CustomDialog(activity!!)
        tmp_dlg.show()
        tmp_dlg.CF_setTextContent(p_message)
        tmp_dlg.CF_setBackKeyMode(CustomDialog.BackMode.CANCELED)
        tmp_dlg.CF_setSingleButtonText(resources.getString(R.string.btn_ok))

        //if(CommonFunction.CF_checkAccessibilityTurnOn(getActivity())){
        p_focusView.isFocusableInTouchMode = true
        tmp_dlg.setOnDismissListener {
            clearAllFocus()
            p_focusView.requestFocus()
            p_focusView.isFocusableInTouchMode = tmp_flagIsFocusTouchEnable
        }
        //}
    }

    /**
     * 커스텀 다이얼로그 show 함수
     * 접근성 포커스 이동을 위해<br></br>
     * param View의 focusableInTouchMode true 세팅 ==> Dialog dismiss 시점에 false 세팅 필요.<br></br>
     * [android.widget.EditText] 처럼 focusableInTouchMode가 true인 경우에는 Dialog dismiss 시점에 flase세팅 불필요.
     * @param p_message     String, 메시지
     * @param p_focusView   View, 접근성 포커스 이동 View
     */
    fun showCustomDialog(p_message: String?, p_focusView: View, p_listener: DialogInterface.OnDismissListener?) {
        showCustomDialog(p_message, resources.getString(R.string.btn_cancel), resources.getString(R.string.btn_ok), p_focusView, p_listener)
    }

    /**
     * 커스텀 다이얼로그 show 함수
     * 접근성 포커스 이동을 위해<br></br>
     * param View의 focusableInTouchMode true 세팅 ==> Dialog dismiss 시점에 false 세팅 필요.<br></br>
     * [android.widget.EditText] 처럼 focusableInTouchMode가 true인 경우에는 Dialog dismiss 시점에 flase세팅 불필요.
     * @param p_message     String
     * @param p_btnLeft     String
     * @param p_btnRight    String
     * @param p_focusView   View
     * @param p_listener    DialogInterface.OnDismissListener
     */
    fun showCustomDialog(p_message: String?, p_btnLeft: String?, p_btnRight: String?, p_focusView: View, p_listener: DialogInterface.OnDismissListener?) {
        val tmp_flagIsFocusTouchEnable = p_focusView.isFocusableInTouchMode
        if (tmp_flagIsFocusTouchEnable == false && CommonFunction.CF_checkAccessibilityTurnOn(activity)) {
            p_focusView.isFocusableInTouchMode = true
        }
        val tmp_dlg = CustomDialog(activity!!)
        tmp_dlg.show()
        tmp_dlg.CF_setTextContent(p_message)
        tmp_dlg.CF_setBackKeyMode(CustomDialog.BackMode.CANCELED)
        tmp_dlg.CF_setDoubleButtonText(p_btnLeft, p_btnRight)
        tmp_dlg.setOnDismissListener(p_listener)
    }
}