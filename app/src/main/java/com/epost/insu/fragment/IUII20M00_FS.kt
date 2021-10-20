package com.epost.insu.fragment

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.provider.MediaStore
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.FrameLayout
import android.widget.TextView
import androidx.core.app.ActivityCompat
import com.epost.insu.EnvConfig
import com.epost.insu.R
import com.epost.insu.SharedPreferencesFunc
import com.epost.insu.activity.IUII10M00_S
import com.epost.insu.activity.IUII21S01
import com.epost.insu.common.CommonFunction
import com.epost.insu.common.LogPrinter
import com.epost.insu.control.AddPhotoViewGroup
import com.epost.insu.data.Data_IUII10M06_F
import com.epost.insu.dialog.CustomDialog
import com.epost.insu.event.OnAddPhotoViewGroupEventListener
import com.epost.insu.psmobile.PSMobileActivity
import java.io.File
import java.util.*

/**
 * @copyright : (주)프로니스정보기술 | http://www.phronis.co.kr
 *
 * @project   : 모바일슈랑스 구축
 * @pakage    : com.epost.insu.fragment
 * @fileName  : IUII10M00_F6.java
 *
 * @Title     : IUII20M00 (보험금청구접수신청 - 구비서류 첨부)
 * @author    : 이수행
 * @created   : 2017-08-22
 * @version   : 1.0
 *
 * @note      : <u>IUII20M00(보험금청구접수신청 - 구비서류 첨부)</u><br></br>
 * Android 6.0(API 23) 동적권한 적용<br></br>
 * App 초기에 WRITE_EXTERNAL_STORAGE 권한을 요청해 저장소 권한을 획득하기에 READ_EXTERNAL_STORAGE 권한 요청이 필요 없음<br></br>
 * 하지만 READ_EXTERNAL_STORAGE 역시 권한 확인 후 처리하도록 구현함<br></br>
 * ======================================================================
 * 수정 내역
 * NO       날짜          작업자        내용
 * 01       2017-08-22    이수행     : 최초 등록
 * 02       2019-03-04    노지민     : 지급진행조회에서 보완청구시 사진 첨부갯수 10 -> 15로 변경
 * 03       2020-10-28    노지민     : [모바일 사진촬영 패키지 도입]
 * 1.6.3    NJM_20211007    [API30 대응] targetApi 29 -> 30 변경에 따른 코틀린 오류 수정
 * =======================================================================
 */
class IUII20M00_FS constructor() : Fragment_Default(), OnAddPhotoViewGroupEventListener {
    private var reqPhotoGroupIndex: Int = 0
    private var photoGroupView: Array<AddPhotoViewGroup?>? = null
    private var flagShownCameraGuide: Boolean = false
    private var data: Data_IUII10M06_F? = null
    private var mActivity: IUII10M00_S? = null

    override fun onReqAddPhoto(p_view: AddPhotoViewGroup) {
        if (addedPhotoCount < EnvConfig.MAX_PHOTO_COUNT) {
            reqPhotoGroupIndex = p_view.getTag() as Int
            startGalleryActivity()
        } else {
            CommonFunction.CF_showCustomAlertDilaog(getActivity(), getResources().getString(R.string.dlg_max_doc_for_req), getResources().getString(R.string.btn_ok))
        }
    }

    override fun onReqTakePicture(p_view: AddPhotoViewGroup) {
        // --<1> (카메라가능 단말기)
        if (getActivity()!!.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_ANY)) {
            if (addedPhotoCount < EnvConfig.MAX_PHOTO_COUNT) {
                reqPhotoGroupIndex = p_view.getTag() as Int
                if (flagShownCameraGuide == false) {
                    flagShownCameraGuide = true

                    // 사진 촬영 시 주의사항 안내 Activity 호출
                    startCameraGuideActivity()
                } else {
                    // 카메라 App 실행
                    startCameraApp()
                }
            } else {
                showCustomDialog(getResources().getString(R.string.dlg_max_doc_for_req), p_view.findViewById(R.id.btnCamera))
            }
        } else {
            showCustomDialog(getResources().getString(R.string.dlg_no_camera_feature), p_view.findViewById(R.id.btnCamera))
        }
    }

    override fun onDeletedPhoto(p_view: AddPhotoViewGroup) {
        // -----------------------------------------------------------------------------------------
        //  접근성 포커스 이동
        // -----------------------------------------------------------------------------------------
        if (CommonFunction.CF_checkAccessibilityTurnOn(getActivity()!!.getApplicationContext())) {
            val tmp_textLabel: TextView? = p_view.findViewById(R.id.textLabel)
            if (tmp_textLabel != null) {
                clearAllFocus()
                tmp_textLabel.setFocusableInTouchMode(true)
                tmp_textLabel.requestFocus()
                tmp_textLabel.setFocusableInTouchMode(false)
            }
        }
    }

    override fun onDeletCanceled(p_view: AddPhotoViewGroup, p_viewDel: View) {
        // -----------------------------------------------------------------------------------------
        //  접근성 포커스 이동
        // -----------------------------------------------------------------------------------------
        if (CommonFunction.CF_checkAccessibilityTurnOn(Objects.requireNonNull(getActivity())!!.getApplicationContext())) {
            if (p_viewDel != null) {
                clearAllFocus()
                p_viewDel.setFocusableInTouchMode(true)
                p_viewDel.requestFocus()
                p_viewDel.setFocusableInTouchMode(false)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setInit()
        if (savedInstanceState != null) {
            if (savedInstanceState.containsKey("flagShownCameraGuide")) {
                flagShownCameraGuide = savedInstanceState.getBoolean("flagShownCameraGuide")
            }
            if (savedInstanceState.containsKey("reqPhotoGroupIndex")) {
                reqPhotoGroupIndex = savedInstanceState.getInt("reqPhotoGroupIndex")
            }
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mActivity = context as IUII10M00_S?
    }

    @SuppressLint("InflateParams")
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.iuii10m06_f, null)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        if (savedInstanceState != null) {
            if (savedInstanceState.containsKey("data")) {
                data = savedInstanceState.getParcelable("data")
            }
        }

        // -- UI 생성 및 세팅
        setUIControl()

        // -- 데이터 복구
        restoreData()
    }

    override fun onSaveInstanceState(p_bundle: Bundle) {
        super.onSaveInstanceState(p_bundle)

        // -- 데이터 저장
        saveData()
        p_bundle.putParcelable("data", data)
        p_bundle.putBoolean("flagShownCameraGuide", flagShownCameraGuide)
        p_bundle.putInt("reqPhotoGroupIndex", reqPhotoGroupIndex)
    }

    override fun onActivityResult(p_requestCode: Int, p_resultCode: Int, p_data: Intent?) {
        super.onActivityResult(p_requestCode, p_resultCode, p_data)
        LogPrinter.CF_debug("!-----------------------------------------------------------")
        LogPrinter.CF_debug("!-- IUII20M00_FS.onActivityResult()")
        LogPrinter.CF_debug("!-----------------------------------------------------------")

        // -----------------------------------------------------------------------------------------
        // -- 갤러리/카메라 이미지 보정 후 리턴(일루텍 솔루션)
        // -----------------------------------------------------------------------------------------
        if (p_requestCode == EnvConfig.REQUESTCODE_MODULE_PSMobileActivity && p_resultCode == Activity.RESULT_OK) {
            assert(p_data != null)
            val results: ArrayList<Uri> = p_data!!.getParcelableArrayListExtra(PSMobileActivity.RESULT_URI_ARRAYLIST)!!
            LogPrinter.CF_debug("!---- Result 이미지 임시파일 경로 :" + Objects.requireNonNull(results).toString())

            // -- 임시파일 이미지 경로 처리
            for (uri: Uri in results) {
                val tmp_imgPath: String? = uri.getPath()

                // 포토그룹에 임시 경로 추가
                photoGroupView?.get(reqPhotoGroupIndex)!!.CF_addPhotoView(tmp_imgPath)

                // -- 접근성 포커스 이동
                if (CommonFunction.CF_checkAccessibilityTurnOn(Objects.requireNonNull(getActivity()))) {
                    Handler().postDelayed(object : Runnable {
                        override fun run() {
                            val tmp_addedPhotoView: FrameLayout? = photoGroupView?.get(reqPhotoGroupIndex)!!.CF_getLastAddedPhotoView()
                            if (tmp_addedPhotoView != null) {
                                clearAllFocus()
                                tmp_addedPhotoView.setFocusableInTouchMode(true)
                                tmp_addedPhotoView.requestFocus()
                                tmp_addedPhotoView.setFocusableInTouchMode(false)
                            }
                        }
                    }, 500)
                }
            }
        } else if (p_requestCode == EnvConfig.REQUESTCODE_ACTIVITY_IUII21S01 && p_resultCode == Activity.RESULT_OK) {
            startCameraApp()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        for (i in photoGroupView!!.indices) {
            photoGroupView?.get(i)!!.CF_recycle()
        }
    }

    /**
     * 초기 세팅 함수
     */
    private fun setInit() {
        reqPhotoGroupIndex = -1
        flagShownCameraGuide = false
        photoGroupView = arrayOfNulls(2)
        data = Data_IUII10M06_F()
    }

    /**
     * UI 생성 및 세팅 함수
     */
    private fun setUIControl() {
        // -- 구비서류(사진) 첨부 View Control 세팅
        setAddPhotoViewGroupUI()
        val tmp_textSubTitle: TextView = getView()!!.findViewById(R.id.textSubTitle)
        tmp_textSubTitle.setText(getResources().getString(R.string.label_req_step_sub))
        tmp_textSubTitle.setVisibility(View.GONE)
        val tmp_btnNext: Button = getView()!!.findViewById(R.id.btnFill)
        tmp_btnNext.setText(getResources().getString(R.string.btn_ok))
        tmp_btnNext.setOnClickListener(object : View.OnClickListener {
            override fun onClick(v: View) {
                if (checkUserInput()) {
                    val tmp_arrImgPath: ArrayList<String> = ArrayList()
                    tmp_arrImgPath.addAll(photoGroupView?.get(0)!!.CF_getImgPathList())
                    tmp_arrImgPath.addAll(photoGroupView?.get(1)!!.CF_getImgPathList())
                    mActivity!!.CF_requestSubmit(tmp_arrImgPath)
                }
            }
        })
    }

    /**
     * 데이터 백업
     */
    private fun saveData() {
        if (photoGroupView?.get(0) != null && photoGroupView?.get(1) != null) {
            data!!.CF_setPhotoPath(photoGroupView?.get(0)!!.CF_getImgPathList(), photoGroupView?.get(1)!!.CF_getImgPathList())
        }
    }

    /**
     * 데이터 복구
     */
    private fun restoreData() {
        // 사용자가 추가한 사진 정보 복구
        val tmp_arrPath_1: ArrayList<String> = data!!.CF_getPhotoPath_1()
        for (i in tmp_arrPath_1.indices) {
            photoGroupView?.get(0)!!.CF_addPhotoView(tmp_arrPath_1.get(i))
        }
        val tmp_arrPath_2: ArrayList<String> = data!!.CF_getPhotoPath_2()
        for (i in tmp_arrPath_2.indices) {
            photoGroupView?.get(1)!!.CF_addPhotoView(tmp_arrPath_2.get(i))
        }
    }

    /**
     * 구비서류(사진) 첨부 View Control 세팅 함수
     */
    private fun setAddPhotoViewGroupUI() {
        val tmp_addPhoto_1: AddPhotoViewGroup = getView()!!.findViewById(R.id.addPhotoView_1)
        val tmp_addPhoto_2: AddPhotoViewGroup = getView()!!.findViewById(R.id.addPhotoView_2)
        tmp_addPhoto_1.setTag(0)
        tmp_addPhoto_2.setTag(1)
        tmp_addPhoto_1.CE_setOnAddPhotoViewGroupEventListener(this)
        tmp_addPhoto_2.CE_setOnAddPhotoViewGroupEventListener(this)
        photoGroupView!![0] = tmp_addPhoto_1
        photoGroupView!![1] = tmp_addPhoto_2
    }

    /**
     * 저장소 읽기 권한 요청<br></br>
     * 첫 요청인 경우 저장소 권한동의 Dialog를 바로 띄우고<br></br>
     * 거절한 적이 있는 경우 Snackbar를 통해 해당 권한이 왜 필요한지 안내한다.<br></br>
     */
    private fun requestREAD_EXTERNAL_STORAGE_Permission() {
        if (checkPermissionRational(Manifest.permission.READ_EXTERNAL_STORAGE)) {
            ActivityCompat.requestPermissions((getActivity())!!, arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), REQUEST_STORAGE_PERMISSION)
        } else {
            if (SharedPreferencesFunc.getReqPermissionCamera(getActivity()!!.getApplicationContext())) {

                // 사용자가 다시 묻지 않기를 선택하고 거절한 경우로 팝업을 통해 해당 권한이 왜 필요한지 안내하고
                // App 설정 페이지로 이동할 수 있도록 한다.
                val tmp_dlg: CustomDialog = CustomDialog((getActivity())!!)
                tmp_dlg.show()
                tmp_dlg.CF_setBackKeyMode(CustomDialog.BackMode.CANCELED)
                tmp_dlg.CF_setTextContent(getResources().getString(R.string.dlg_permission_storage_read) + "\r\n\r\n" + getResources().getString(R.string.dlg_permission_storage_read_setting))
                tmp_dlg.CF_setDoubleButtonText(getResources().getString(R.string.btn_close), getResources().getString(R.string.btn_setting))
                tmp_dlg.setOnDismissListener(object : DialogInterface.OnDismissListener {
                    override fun onDismiss(dialog: DialogInterface) {
                        if ((dialog as CustomDialog).CF_getCanceled() == false) {
                            startOSSettingActivity()
                        }
                    }
                })
            } else {
                // 최초 권한 요청
                SharedPreferencesFunc.setReqPermissionReadStorage(getActivity()!!.getApplicationContext(), true)
                ActivityCompat.requestPermissions((getActivity())!!, arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), REQUEST_STORAGE_PERMISSION)
            }
        }
    }

    /**
     * 카메라 퍼미션 동적 요청
     */
    private fun requestCameraPermission() {
        if (checkPermissionRational(Manifest.permission.CAMERA)) {
            ActivityCompat.requestPermissions((getActivity())!!, arrayOf(Manifest.permission.CAMERA), REQUEST_CAMERA_PERMISSION)
        } else {
            if (SharedPreferencesFunc.getReqPermissionCamera(getActivity()!!.getApplicationContext())) {

                // 사용자가 다시 묻지 않기를 선택하고 거절한 경우로 팝업을 통해 해당 권한이 왜 필요한지 안내하고
                // App 설정 페이지로 이동할 수 있도록 한다.
                val tmp_dlg: CustomDialog = CustomDialog((getActivity())!!)
                tmp_dlg.show()
                tmp_dlg.CF_setBackKeyMode(CustomDialog.BackMode.CANCELED)
                tmp_dlg.CF_setTextContent(getResources().getString(R.string.dlg_p_camera_denied) + "\r\n\r\n" + getResources().getString(R.string.dlg_p_camera_denied_setting))
                tmp_dlg.CF_setDoubleButtonText(getResources().getString(R.string.btn_close), getResources().getString(R.string.btn_setting))
                tmp_dlg.setOnDismissListener(object : DialogInterface.OnDismissListener {
                    override fun onDismiss(dialog: DialogInterface) {
                        if ((dialog as CustomDialog).CF_getCanceled() == false) {
                            startOSSettingActivity()
                        }
                    }
                })
            } else {
                // 최초 권한 요청
                SharedPreferencesFunc.setReqPermissionCamera(getActivity()!!.getApplicationContext(), true)
                ActivityCompat.requestPermissions((getActivity())!!, arrayOf(Manifest.permission.CAMERA), REQUEST_CAMERA_PERMISSION)
            }
        }
    }

    /**
     * 퍼미션 요청 유무 확인 함수<br></br>
     * 퍼미션 요청한적이 한번이라도 있으면 true를 반환한다.<br></br>
     * @return boolean
     */
    private fun checkPermissionRational(p_permission: String): Boolean {
        return ActivityCompat.shouldShowRequestPermissionRationale((getActivity())!!, p_permission)
    }

    /**
     * 현재 추가되어 있는 사진 수 반환 함수
     * @return int
     */
    private val addedPhotoCount: Int
        private get() {
            return photoGroupView?.get(0)!!.CF_getPhotoCount() + photoGroupView?.get(1)!!.CF_getPhotoCount()
        }

    /**
     * 첨부한 서류 파일 존재 여부 확인
     * @return tmp_flagAllExists : boolean
     */
    private fun checkAddedFileExists(): Boolean {
        var tmp_flagAllExists: Boolean = true
        val tmp_arrImgPath: ArrayList<String> = ArrayList()
        tmp_arrImgPath.addAll(photoGroupView?.get(0)!!.CF_getImgPathList())
        tmp_arrImgPath.addAll(photoGroupView?.get(1)!!.CF_getImgPathList())
        for (i in tmp_arrImgPath.indices) {
            val tmp_file: File = File(tmp_arrImgPath.get(i))
            if (tmp_file.exists() == false) {
                tmp_flagAllExists = false
                break
            }
        }
        return tmp_flagAllExists
    }

    /**
     * 사용자 입력값 검사 함수
     * @return tmp_flagOk : boolean
     */
    private fun checkUserInput(): Boolean {
        var tmp_flagOk: Boolean = true
        val tmp_arrImgPath: ArrayList<String> = ArrayList()
        tmp_arrImgPath.addAll(photoGroupView?.get(0)!!.CF_getImgPathList())
        tmp_arrImgPath.addAll(photoGroupView?.get(1)!!.CF_getImgPathList())
        if (tmp_arrImgPath.size == 0) {
            tmp_flagOk = false
            CommonFunction.CF_showCustomAlertDilaog(getActivity(), getResources().getString(R.string.dlg_add_doc_for_req), getResources().getString(R.string.btn_ok))
        } else if (tmp_arrImgPath.size > EnvConfig.MAX_PHOTO_COUNT) {
            tmp_flagOk = false
            CommonFunction.CF_showCustomAlertDilaog(getActivity(), getResources().getString(R.string.dlg_max_doc_for_req), getResources().getString(R.string.btn_ok))
        } else if (checkAddedFileExists() == false) {
            tmp_flagOk = false
            CommonFunction.CF_showCustomAlertDilaog(getActivity(), getResources().getString(R.string.dlg_not_exists_doc), getResources().getString(R.string.btn_ok))
        }
        return tmp_flagOk
    }
    // #############################################################################################
    //  Activity 호출
    // #############################################################################################
    /**
     * 퍼미션 권한 변경을 위한 설정 Activity 이동 함수
     */
    private fun startOSSettingActivity() {
        val tmp_intent: Intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
        val tmp_uri: Uri = Uri.fromParts("package", getActivity()!!.getPackageName(), null)
        tmp_intent.setData(tmp_uri)
        startActivity(tmp_intent)
    }

    /**
     * 사진첩 Activity 호출 함수(일루텍 솔루션)
     */
    private fun startGalleryActivity() {
        if (CommonFunction.CF_checkAgreePermission(getActivity(), Manifest.permission.READ_EXTERNAL_STORAGE)) {
            val i: Intent = Intent(getActivity(), PSMobileActivity::class.java)
            i.putExtra(PSMobileActivity.MODE, PSMobileActivity.MODE_GALLERY)
            i.putExtra(PSMobileActivity.MAX_WIDTH, EnvConfig.upload_photo_max_size)
            i.putExtra(PSMobileActivity.MAX_HEIGHT, EnvConfig.upload_photo_max_size)
            i.putExtra(PSMobileActivity.MAX_COUNT, EnvConfig.MAX_PHOTO_COUNT - addedPhotoCount) // 최대첨부가능사진수 - 첨부된사진수
            startActivityForResult(i, EnvConfig.REQUESTCODE_MODULE_PSMobileActivity)
        } else {
            requestREAD_EXTERNAL_STORAGE_Permission()
        }
    }

    /**
     * IUII21S01 (사진 촬영 시 주의사항) Activity 호출 함수
     */
    private fun startCameraGuideActivity() {
        val tmp_intent: Intent = Intent(getActivity(), IUII21S01::class.java)
        tmp_intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
        startActivityForResult(tmp_intent, EnvConfig.REQUESTCODE_ACTIVITY_IUII21S01)
    }

    /**
     * IUII21M00 카메라 촬영 App 호출 함수(일루텍 솔루션)<br></br>
     * 단말기에 Camera 기능을 사용할 수 없을 수도 있기에 Intent.resolveActivity()검사후 실행한다<br></br>
     * 사용할 수 없는 경우에는 알림창을 띄운다<br></br>
     */
    private fun startCameraApp() {
        if (CommonFunction.CF_checkAgreePermission(getActivity()!!.getApplicationContext(), Manifest.permission.CAMERA)) {
            val tmp_intent: Intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            if (tmp_intent.resolveActivity(getActivity()!!.getPackageManager()) != null) {
                val i: Intent = Intent(getActivity(), PSMobileActivity::class.java)
                i.putExtra(PSMobileActivity.MODE, PSMobileActivity.MODE_CAMERA)
                i.putExtra(PSMobileActivity.MAX_WIDTH, EnvConfig.upload_photo_max_size)
                i.putExtra(PSMobileActivity.MAX_HEIGHT, EnvConfig.upload_photo_max_size)
                i.putExtra(PSMobileActivity.MAX_COUNT, EnvConfig.MAX_PHOTO_COUNT - addedPhotoCount) // 최대첨부가능사진수 - 첨부된사진수
                startActivityForResult(i, EnvConfig.REQUESTCODE_MODULE_PSMobileActivity)
            } else {
                CommonFunction.CF_showCustomAlertDilaog(getActivity(), getResources().getString(R.string.dlg_no_camera), getResources().getString(R.string.btn_ok))
            }
        } else {
            // 카메라 퍼미션 권한 요청
            requestCameraPermission()
        }
    }

    companion object {
        private val REQUEST_STORAGE_PERMISSION: Int = 1
        private val REQUEST_CAMERA_PERMISSION: Int = 2
    }
}