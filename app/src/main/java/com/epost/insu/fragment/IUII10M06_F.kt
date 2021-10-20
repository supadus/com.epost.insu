package com.epost.insu.fragment

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
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
import android.widget.FrameLayout
import android.widget.TextView
import androidx.core.app.ActivityCompat
import com.epost.insu.EnvConfig
import com.epost.insu.R
import com.epost.insu.SharedPreferencesFunc
import com.epost.insu.activity.IUII21S01
import com.epost.insu.common.CommonFunction
import com.epost.insu.common.LogPrinter
import com.epost.insu.control.AddPhotoViewGroup
import com.epost.insu.data.Data_IUII10M00
import com.epost.insu.data.Data_IUII10M06_F
import com.epost.insu.dialog.CustomDialog
import com.epost.insu.event.OnAddPhotoViewGroupEventListener
import com.epost.insu.psmobile.PSMobileActivity
import java.io.File
import java.util.*

/**
 * 보험금청구 > 본인청구 > 6단계. 구비서류 첨부
 * @since     :
 * @version   : 1.2
 * @author    : LSH
 * @see
 * <pre>
 * Android 6.0(API 23) 동적권한 적용
 *  - App 초기에 WRITE_EXTERNAL_STORAGE 권한을 요청해 저장소 권한을 획득하기에 READ_EXTERNAL_STORAGE 권한 요청이 필요 없음
 *  - 하지만 READ_EXTERNAL_STORAGE 역시 권한 확인 후 처리하도록 구현함
 * ======================================================================
 * 0.0.0    LSH_20170822    최초 등록
 * 0.0.0    NJM_20200218    신분증 첨부하기 삭제 -> gone처리
 * 0.0.0    NJM_20201028    촬영솔루션 교체 [모바일 사진촬영 패키지 도입]
 * 1.6.1    NJM_20210722    [2021년 대우 취약점] 1차본
 * 1.6.3    NJM_20211007    [API30 대응] targetApi 29 -> 30 변경에 따른 코틀린 오류 수정
 * =======================================================================
 * copyright : (주)프로니스정보기술 | http://www.phronis.co.kr
 * </pre>
 */
class IUII10M06_F : IUII10M00_FD(), OnAddPhotoViewGroupEventListener {
    private var reqPhotoGroupIdx: Int = 0
    private var photoGroupView: Array<AddPhotoViewGroup?>? = null
    private var flagShownCameraGuide: Boolean = false
    private var data: Data_IUII10M06_F? = null
    private var lastFocuesPhotoViewGroup: AddPhotoViewGroup? = null // 접근성 포커싱 용도의 마지막 포커스 View

    companion object {
        val REQUEST_STORAGE_PERMISSION: Int = 1
        val REQUEST_CAMERA_PERMISSION: Int = 2
    }

    //----------------------------------------------------------------------------------------------
    // -- onReqAddPhoto in interface OnAddPhotoViewGroupEventListener
    //----------------------------------------------------------------------------------------------
    override fun onReqAddPhoto(p_view: AddPhotoViewGroup) {
        lastFocuesPhotoViewGroup = p_view
        if (addedPhotoCnt < EnvConfig.MAX_PHOTO_COUNT) {
            reqPhotoGroupIdx = p_view.tag as Int
            CF_startGalleryActivity()
        } else {
            CommonFunction.CF_showCustomAlertDilaog(activity, getResources().getString(R.string.dlg_max_doc_for_req), getResources().getString(R.string.btn_ok))
        }
    }

    override fun onReqTakePicture(p_view: AddPhotoViewGroup) {
        // --<1> 촬영 버튼 클릭 시 카메라가 있는 단말일 경우
        if (Objects.requireNonNull(activity)!!.packageManager.hasSystemFeature(PackageManager.FEATURE_CAMERA_ANY)) {
            lastFocuesPhotoViewGroup = p_view
            if (addedPhotoCnt < EnvConfig.MAX_PHOTO_COUNT) {
                reqPhotoGroupIdx = p_view.tag as Int

                // --<> 사진 촬영 시 주의사항 안내를 하지 않은 경우
                if (!flagShownCameraGuide) {
                    // 사진 촬영 시 주의사항 안내 Activity 호출
                    flagShownCameraGuide = true
                    startCameraGuideActivity()
                } else {
                    CF_startCameraApp() // 카메라 App 실행
                }
            } else {
                showCustomDialog(resources.getString(R.string.dlg_max_doc_for_req), p_view.findViewById(R.id.btnCamera))
            }
        } else {
            showCustomDialog(resources.getString(R.string.dlg_no_camera_feature), p_view.findViewById(R.id.btnCamera))
        }
    }

    override fun onDeletedPhoto(p_view: AddPhotoViewGroup) {
        // -- 접근성 포커스 이동
        if (CommonFunction.CF_checkAccessibilityTurnOn(Objects.requireNonNull(activity)!!.applicationContext)) {
            val txtLabel: TextView? = p_view.findViewById(R.id.textLabel)
            if (txtLabel != null) {
                clearAllFocus()
                txtLabel.isFocusableInTouchMode = true
                txtLabel.requestFocus()
                txtLabel.isFocusableInTouchMode = false
            }
        }
    }

    override fun onDeletCanceled(p_view: AddPhotoViewGroup, p_viewDel: View) {
        // -- 접근성 포커스 이동
        if (CommonFunction.CF_checkAccessibilityTurnOn(Objects.requireNonNull(activity)!!.applicationContext)) {
            clearAllFocus()
            p_viewDel.isFocusableInTouchMode = true
            p_viewDel.requestFocus()
            p_viewDel.isFocusableInTouchMode = false
        }
    }
    //----------------------------------------------------------------------------------------------
    // -- LifeCycle
    //----------------------------------------------------------------------------------------------
    /**
     * 초기 세팅 함수
     */
    private fun setInit() {
        LogPrinter.CF_debug("!----------------------------------------------------------")
        LogPrinter.CF_debug("!-- IUII10M06_F.setInit()")
        LogPrinter.CF_debug("!----------------------------------------------------------")

        reqPhotoGroupIdx = -1
        flagShownCameraGuide = false
        photoGroupView = arrayOfNulls(2)
        data = Data_IUII10M06_F()
        LogPrinter.CF_debug("!---- (6단계) 인증구분 : " + mActivity!!.CF_getAuthDvsn())
    }

    /**
     * UI 생성 및 세팅 함수
     */
    private fun setUIControl() {
        // 구비서류(사진) 첨부 View Control 세팅
        setAddPhotoViewGroupUI()
        scrollView = view?.findViewById(R.id.scrollView)
        btnNext = view!!.findViewById(R.id.btnFill)
        btnNext!!.text = resources.getString(R.string.btn_next_2)
        btnNext!!.setOnClickListener {
            if (checkUserInput()) {
                val iuiI10M00: Data_IUII10M00? = mActivity!!.CF_getData()
                val arrImgPath: ArrayList<String> = ArrayList()
                arrImgPath.addAll(photoGroupView?.get(0)!!.CF_getImgPathList())
                arrImgPath.addAll(photoGroupView?.get(1)!!.CF_getImgPathList())
                iuiI10M00!!.CF_setImgCnt("" + arrImgPath.size)
                iuiI10M00.CF_setArrImgPath(arrImgPath)
                btnNext!!.isEnabled = false
                mActivity!!.CF_showNextPage()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // -- 초기화
        setInit()
        if (savedInstanceState != null) {
            if (savedInstanceState.containsKey("flagShownCameraGuide")) {
                flagShownCameraGuide = savedInstanceState.getBoolean("flagShownCameraGuide")
            }
            if (savedInstanceState.containsKey("reqPhotoGroupIndex")) {
                reqPhotoGroupIdx = savedInstanceState.getInt("reqPhotoGroupIndex")
            }
        }
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
        saveData()
        p_bundle.putParcelable("data", data)
        p_bundle.putBoolean("flagShownCameraGuide", flagShownCameraGuide)
        p_bundle.putInt("reqPhotoGroupIndex", reqPhotoGroupIdx)
    }

    override fun onActivityResult(p_requestCode: Int, p_resultCode: Int, p_data: Intent?) {
        super.onActivityResult(p_requestCode, p_resultCode, p_data)
        LogPrinter.CF_debug("!-----------------------------------------------------------")
        LogPrinter.CF_debug("!-- IUII10M06_F.onActivityResult()")
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
                val imgPath: String? = uri.path

                // 포토그룹에 임시 경로 추가
                photoGroupView?.get(reqPhotoGroupIdx)!!.CF_addPhotoView(imgPath)

                // -- 접근성 포커스 이동
                if (CommonFunction.CF_checkAccessibilityTurnOn(Objects.requireNonNull(activity))) {
                    Handler().postDelayed({
                        val addedPhotoView: FrameLayout? = photoGroupView?.get(reqPhotoGroupIdx)!!.CF_getLastAddedPhotoView()
                        if (addedPhotoView != null) {
                            clearAllFocus()
                            addedPhotoView.isFocusableInTouchMode = true
                            addedPhotoView.requestFocus()
                            addedPhotoView.isFocusableInTouchMode = false
                        }
                    }, 500)
                }
            }
        } else if (p_requestCode == EnvConfig.REQUESTCODE_ACTIVITY_IUII21S01 && p_resultCode == Activity.RESULT_OK) {
            // --<> (접근성ON)
            if (CommonFunction.CF_checkAccessibilityTurnOn(Objects.requireNonNull(activity)) && lastFocuesPhotoViewGroup != null) {
                showCustomDialog(resources.getString(R.string.dlg_accessible_open_camera_app), lastFocuesPhotoViewGroup!!.findViewById(R.id.btnCamera)) { dialog ->
                    if (!(dialog as CustomDialog).CF_getCanceled()) {
                        CF_startCameraApp()
                    } else {
                        clearAllFocus()
                        lastFocuesPhotoViewGroup!!.findViewById<View>(R.id.btnCamera).requestFocus()
                    }
                    lastFocuesPhotoViewGroup!!.isFocusableInTouchMode = false
                    lastFocuesPhotoViewGroup = null
                }
            } else {
                CF_startCameraApp()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        for (addPhotoViewGroup: AddPhotoViewGroup? in photoGroupView!!) {
            addPhotoViewGroup!!.CF_recycle()
        }
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
        val arrPath1: ArrayList<String> = data!!.CF_getPhotoPath_1()
        for (i in arrPath1.indices) {
            photoGroupView?.get(0)!!.CF_addPhotoView(arrPath1[i])
        }
        val arrPath2: ArrayList<String> = data!!.CF_getPhotoPath_2()
        for (i in arrPath2.indices) {
            photoGroupView?.get(1)!!.CF_addPhotoView(arrPath2[i])
        }
    }

    /**
     * 구비서류(사진) 첨부 View Control 세팅 함수
     */
    private fun setAddPhotoViewGroupUI() {
        val addPhoto1: AddPhotoViewGroup = view?.findViewById(R.id.addPhotoView_1)!!
        val addPhoto2: AddPhotoViewGroup = view?.findViewById(R.id.addPhotoView_2)!!
        addPhoto1.tag = 0
        addPhoto2.tag = 1
        addPhoto1.CE_setOnAddPhotoViewGroupEventListener(this)
        addPhoto2.CE_setOnAddPhotoViewGroupEventListener(this)
        photoGroupView!![0] = addPhoto1
        photoGroupView!![1] = addPhoto2
    }

    /**
     * 저장소 읽기 권한 요청<br></br>
     * 첫 요청인 경우 저장소 권한동의 Dialog를 바로 띄우고<br></br>
     * 거절한 적이 있는 경우 Snackbar를 통해 해당 권한이 왜 필요한지 안내한다.<br></br>
     */
    private fun requestREAD_EXTERNAL_STORAGE_Permission() {
        if (checkPermissionRational(Manifest.permission.READ_EXTERNAL_STORAGE)) {
            ActivityCompat.requestPermissions((Objects.requireNonNull(activity))!!, arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), REQUEST_STORAGE_PERMISSION)
        }
        else {
            if (SharedPreferencesFunc.getReqPermissionCamera(Objects.requireNonNull(activity)!!.applicationContext)) {
                // 사용자가 다시 묻지 않기를 선택하고 거절한 경우로 팝업을 통해 해당 권한이 왜 필요한지 안내하고
                // App 설정 페이지로 이동할 수 있도록 한다.
                val customDialog = CustomDialog((activity)!!)
                customDialog.show()
                customDialog.CF_setBackKeyMode(CustomDialog.BackMode.CANCELED)
                customDialog.CF_setTextContent(resources.getString(R.string.dlg_permission_storage_read) + "\r\n\r\n" + resources.getString(R.string.dlg_permission_storage_read_setting))
                customDialog.CF_setDoubleButtonText(resources.getString(R.string.btn_close), resources.getString(R.string.btn_setting))
                customDialog.setOnDismissListener { dialog ->
                    if (!(dialog as CustomDialog).CF_getCanceled()) {
                        startOSSettingActivity()
                    }
                }
            } else {
                // -- 최초 권한 요청
                SharedPreferencesFunc.setReqPermissionReadStorage(activity!!.applicationContext, true)
                ActivityCompat.requestPermissions((activity)!!, arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), REQUEST_STORAGE_PERMISSION)
            }
        }
    }

    /**
     * 카메라 퍼미션 동적 요청
     */
    private fun requestCameraPermission() {
        if (checkPermissionRational(Manifest.permission.CAMERA)) {
            ActivityCompat.requestPermissions((Objects.requireNonNull(activity))!!, arrayOf(Manifest.permission.CAMERA), REQUEST_CAMERA_PERMISSION)
        }
        else {
            if (SharedPreferencesFunc.getReqPermissionCamera(Objects.requireNonNull(activity)!!.applicationContext)) {
                // 사용자가 다시 묻지 않기를 선택하고 거절한 경우로 팝업을 통해 해당 권한이 왜 필요한지 안내하고
                // App 설정 페이지로 이동할 수 있도록 한다.
                val customDialog = CustomDialog((activity)!!)
                customDialog.show()
                customDialog.CF_setBackKeyMode(CustomDialog.BackMode.CANCELED)
                customDialog.CF_setTextContent(resources.getString(R.string.dlg_p_camera_denied) + "\r\n\r\n" + resources.getString(R.string.dlg_p_camera_denied_setting))
                customDialog.CF_setDoubleButtonText(resources.getString(R.string.btn_close), resources.getString(R.string.btn_setting))
                customDialog.setOnDismissListener { dialog ->
                    if (!(dialog as CustomDialog).CF_getCanceled()) {
                        startOSSettingActivity()
                    }
                }
            } else {
                SharedPreferencesFunc.setReqPermissionCamera(activity!!.applicationContext, true)
                ActivityCompat.requestPermissions((activity)!!, arrayOf(Manifest.permission.CAMERA), REQUEST_CAMERA_PERMISSION)
            }
        }
    }

    /**
     * 퍼미션 요청 유무 확인 함수<br></br>
     * 퍼미션 요청한적이 한번이라도 있으면 true를 반환한다.<br></br>
     * @return  boolean
     */
    private fun checkPermissionRational(p_permission: String): Boolean {
        return ActivityCompat.shouldShowRequestPermissionRationale((Objects.requireNonNull(activity))!!, p_permission)
    }

    /**
     * 퍼미션 권한 변경을 위한 설정 Activity 이동 함수
     */
    private fun startOSSettingActivity() {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
        val uri: Uri = Uri.fromParts("package", Objects.requireNonNull(activity)!!.packageName, null)
        intent.data = uri
        startActivity(intent)
    }

    /**
     * 사진첩 Activity 호출 함수(일루텍 솔루션)
     */
    fun CF_startGalleryActivity() {
        if (CommonFunction.CF_checkAgreePermission(activity, Manifest.permission.READ_EXTERNAL_STORAGE)) {
            val intent = Intent(activity, PSMobileActivity::class.java)
            intent.putExtra(PSMobileActivity.MODE      , PSMobileActivity.MODE_GALLERY)
            intent.putExtra(PSMobileActivity.MAX_WIDTH , EnvConfig.upload_photo_max_size)
            intent.putExtra(PSMobileActivity.MAX_HEIGHT, EnvConfig.upload_photo_max_size)
            intent.putExtra(PSMobileActivity.MAX_COUNT , EnvConfig.MAX_PHOTO_COUNT - addedPhotoCnt) // 최대첨부가능사진수 - 첨부된사진수

            startActivityForResult(intent, EnvConfig.REQUESTCODE_MODULE_PSMobileActivity)

            // TODO : 안드로이드시스템갤러리 호출 테스트중 CF_startGalleryActivity
//            Intent intent = new Intent();
//            intent.setDataAndType(android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*");
//            intent.setAction(Intent.ACTION_GET_CONTENT);
//            startActivityForResult(intent, EnvConfig.REQUESTCODE_ACTIVITY_IUII20M00);
        }
        else {
            requestREAD_EXTERNAL_STORAGE_Permission()
        }
    }

    /**
     * IUII21S01 (사진 촬영 시 주의사항) Activity 호출 함수
     */
    private fun startCameraGuideActivity() {
        val intent = Intent(activity, IUII21S01::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
        startActivityForResult(intent, EnvConfig.REQUESTCODE_ACTIVITY_IUII21S01)
    }

    /**
     * IUII21M00 카메라 촬영 App 호출 함수(일루텍 솔루션)<br></br>
     * 단말기에 Camera 기능을 사용할 수 없을 수도 있기에 Intent.resolveActivity()검사후 실행한다<br></br>
     * 사용할 수 없는 경우에는 알림창을 띄운다<br></br>
     */
    fun CF_startCameraApp() {
        if (CommonFunction.CF_checkAgreePermission(Objects.requireNonNull(activity)!!.applicationContext, Manifest.permission.CAMERA)) {
            val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            if (intent.resolveActivity(activity!!.packageManager) != null) {
                val i = Intent(activity, PSMobileActivity::class.java)
                i.putExtra(PSMobileActivity.MODE, PSMobileActivity.MODE_CAMERA)
                i.putExtra(PSMobileActivity.MAX_WIDTH, EnvConfig.upload_photo_max_size)
                i.putExtra(PSMobileActivity.MAX_HEIGHT, EnvConfig.upload_photo_max_size)
                i.putExtra(PSMobileActivity.MAX_COUNT, EnvConfig.MAX_PHOTO_COUNT - addedPhotoCnt) // 최대첨부가능사진수 - 첨부된사진수
                startActivityForResult(i, EnvConfig.REQUESTCODE_MODULE_PSMobileActivity)
            } else {
                CommonFunction.CF_showCustomAlertDilaog(activity, resources.getString(R.string.dlg_no_camera), resources.getString(R.string.btn_ok))
            }
        } else {
            // -- 카메라 퍼미션 권한 요청
            requestCameraPermission()
        }
    }

    /**
     * 현재 추가되어 있는 사진 수 반환 함수
     * @return int  rtnCount    포토그룹 전체에 추가된 사진 수
     */
    private val addedPhotoCnt: Int
        get() {
            var rtnCount = 0
            for (p: AddPhotoViewGroup? in photoGroupView!!) {

                val cnt = p!!.CF_getPhotoCount()

                // 취약점) 정수 연산 전에 안전한 범위 내의 값인지 검사한다.
                require(!(cnt < 0 || cnt > Int.MAX_VALUE - rtnCount)) { "out of bound" }
                rtnCount += cnt
            }
            return rtnCount
        }

    /**
     * 첨부한 서류 파일 존재 여부 확인
     * @return  boolean     tmp_flagAllExists
     */
    private fun checkAddedFileExists(): Boolean {
        var flagAllExists = true
        val arrImgPath: ArrayList<String> = ArrayList()
        arrImgPath.addAll(photoGroupView?.get(0)!!.CF_getImgPathList())
        arrImgPath.addAll(photoGroupView?.get(1)!!.CF_getImgPathList())
        for (i in arrImgPath.indices) {
            val file = File(arrImgPath[i])
            if (!file.exists()) {
                flagAllExists = false
                break
            }
        }
        return flagAllExists
    }

    /**
     * 사용자 입력값 검사 함수
     * @return  boolean
     */
    private fun checkUserInput(): Boolean {
        var flagOk = true
        val arrImgPath: ArrayList<String> = ArrayList()
        arrImgPath.addAll(photoGroupView?.get(0)!!.CF_getImgPathList())
        arrImgPath.addAll(photoGroupView?.get(1)!!.CF_getImgPathList())
        if (arrImgPath.size == 0) {
            flagOk = false
            showCustomDialog(resources.getString(R.string.dlg_add_doc_for_req), view!!.findViewById(R.id.textLabelAddPhoto))
        } else if (arrImgPath.size > EnvConfig.MAX_PHOTO_COUNT) {
            flagOk = false
            showCustomDialog(resources.getString(R.string.dlg_max_doc_for_req), view!!.findViewById(R.id.textLabelAddPhoto))
        } else if (!checkAddedFileExists()) {
            flagOk = false
            showCustomDialog(resources.getString(R.string.dlg_not_exists_doc), view!!.findViewById(R.id.textLabelAddPhoto))
        }
        return flagOk
    }
}