package com.epost.insu.control;

import android.content.Context;
import android.content.DialogInterface;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.provider.MediaStore;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.GridLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.epost.insu.R;
import com.epost.insu.common.CommonFunction;
import com.epost.insu.dialog.CustomDialog;
import com.epost.insu.event.OnAddPhotoViewGroupEventListener;

import java.io.File;
import java.util.ArrayList;

/**
 * @copyright : (주)프로니스정보기술 | http://www.phronis.co.kr
 *
 * @project   : 모바일슈랑스 구축
 * @pakage   : com.epost.insu.control
 * @fileName  : AddPhotoViewGroup.java
 *
 * @Title     : 사진첨부 ViewGroup
 * @author    : 이수행
 * @created   : 2017-08-14
 * @version   : 1.0
 *
 * @note      : <u>IUII20M00(구비서류첨부)</u><br/>
 *               구비서류 첨부에서 사용하는 사진추가 View<br/>
 *
 *               촬영 - OS 카메라 App를 이용하여 사진 촬영<br/>
 *               선택 또는 촬영된 사진의 썸네일을 화면에 그린다.<br/>
 * ======================================================================
 * 수정 내역
 * NO      날짜          작업자       내용
 * 01      2017-08-14    이수행       최초 등록
 * =======================================================================
 */
public class AddPhotoViewGroup extends LinearLayout{

    private OnAddPhotoViewGroupEventListener listener;              // 이벤트 리스너

    private GridLayout gridPhotoGroup;                             // 사진 썸네일 GridView
    private ArrayList<String> arrImgPath;                           // 이미지 경로

    /**
     * 생성자
     * @param context   Context
     */
    public AddPhotoViewGroup(Context context) {
        super(context);

        setInit();
        setUIControl();
    }
    public AddPhotoViewGroup(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);

        setInit();
        setAttributes(attrs);
        setUIControl();
    }
    public AddPhotoViewGroup(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        setInit();
        setAttributes(attrs);
        setUIControl();
    }

    /**
     * 초기 세팅 함수
     */
    private void setInit(){

        arrImgPath = new ArrayList<>();

        // xml 레이아웃 inflate
        LayoutInflater.from(getContext()).inflate(R.layout.c_add_photo_viewgroup,this,true);

        setOrientation(LinearLayout.VERTICAL);
    }

    /**
     * Attribute 세팅 함수<br/>
     * 라벨 & Empty Text
     * @param p_attr    AttributeSet
     */
    private void setAttributes(AttributeSet p_attr){
        TypedArray tmp_typedArray = this.getContext().obtainStyledAttributes(p_attr, R.styleable.AddPhotoViewGroup);

        String tmp_label  = tmp_typedArray.getString(R.styleable.AddPhotoViewGroup_label);
        String tmp_empty = tmp_typedArray.getString(R.styleable.AddPhotoViewGroup_hint);
        String tmp_descBtn_1 = tmp_typedArray.getString(R.styleable.AddPhotoViewGroup_desc_btn_1);
        String tmp_descBtn_2 = tmp_typedArray.getString(R.styleable.AddPhotoViewGroup_desc_btn_2);

        TextView tmp_textLabel = findViewById(R.id.textLabel);
        tmp_textLabel.setText(tmp_label);

        TextView tmp_textEmpty = findViewById(R.id.textEmpty);
        tmp_textEmpty.setText(tmp_empty);

        LinearLayout tmp_btnAddPhoto = findViewById(R.id.linBtnAddPhoto);     // 사진첨부 버튼
        LinearLayout tmp_btnCamera = findViewById(R.id.linBtnCamera);         // 촬영 버튼

        tmp_btnAddPhoto.setContentDescription(tmp_descBtn_1+" 버튼");
        tmp_btnCamera.setContentDescription(tmp_descBtn_2+" 버튼");

        tmp_typedArray.recycle();
    }

    /**
     * UI 생성 및 세팅 함수
     */
    private void setUIControl(){

        // 사진 View group
        gridPhotoGroup = findViewById(R.id.gridPhotoGroup);

        // 버튼 세팅
        LinearLayout tmp_btnAddPhoto = findViewById(R.id.linBtnAddPhoto);     // 사진첨부 버튼
        tmp_btnAddPhoto.setId(R.id.btnGallery);
        LinearLayout tmp_btnCamera = findViewById(R.id.linBtnCamera);         // 촬영 버튼
        tmp_btnCamera.setId(R.id.btnCamera);

        tmp_btnAddPhoto.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {

                if(listener != null){
                    listener.onReqAddPhoto(AddPhotoViewGroup.this);
                }
            }
        });

        tmp_btnCamera.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {

                if(listener != null){
                    listener.onReqTakePicture(AddPhotoViewGroup.this);
                }

            }
        });
    }

    /**
     * 사진 추가 함수
     * @param p_imgPath 이미지 파일 경로
     */
    public void CF_addPhotoView(String p_imgPath){

        final int tmp_itemMargin = CommonFunction.CF_convertDipToPixel(getContext().getApplicationContext(), 6.5f);

        float tmp_gridWidth = getResources().getDisplayMetrics().widthPixels;
        tmp_gridWidth = tmp_gridWidth - CommonFunction.CF_convertDipToPixel(getContext().getApplicationContext(),20)*2;   // AddPhotoViewGroup의 패딩 값
        tmp_gridWidth = tmp_gridWidth -CommonFunction.CF_convertDipToPixel(getContext().getApplicationContext(),15)*2;    // GridView의 left,right 마진 값
        tmp_gridWidth = tmp_gridWidth - tmp_itemMargin*gridPhotoGroup.getColumnCount()*2;                               // Grid item 마진 값

        // 썸네일 photoView 세팅
        GridLayout.LayoutParams tmp_lpRootItem = new GridLayout.LayoutParams();
        tmp_lpRootItem.setMargins(tmp_itemMargin,tmp_itemMargin,tmp_itemMargin,tmp_itemMargin);
        tmp_lpRootItem.width = (int)tmp_gridWidth/gridPhotoGroup.getColumnCount();
        tmp_lpRootItem.height = GridLayout.LayoutParams.WRAP_CONTENT;
        FrameLayout tmp_item = (FrameLayout)View.inflate(getContext(), R.layout.c_photo_view, null);
        tmp_item.setLayoutParams(tmp_lpRootItem);

        // 썸네일 photoView contentDescription 세팅
        TextView tmp_textLabel = findViewById(R.id.textLabel);
        tmp_item.setContentDescription(tmp_textLabel.getText()+" 사진");

        // imageView
        final CustomRatioImageView tmp_imgView = tmp_item.findViewById(R.id.imgView);
        tmp_imgView.setImageBitmap(getPhotoThumb(p_imgPath));

        // 삭제 버튼
        final ImageButton tmp_btnRemove = tmp_item.findViewById(R.id.btnRemove);
        tmp_btnRemove.setContentDescription(getResources().getString(R.string.desc_img_remove));
        tmp_btnRemove.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(final View v) {

                // ---------------------------------------------------------------------------------
                //  삭제 확인 다이얼로그 팝업
                // ---------------------------------------------------------------------------------
                CustomDialog tmp_dlg = new CustomDialog(getContext());
                tmp_dlg.show();
                tmp_dlg.CF_setBackKeyMode(CustomDialog.BackMode.CANCELED);
                tmp_dlg.CF_setTextContent(getResources().getString(R.string.dlg_ask_remove));
                tmp_dlg.CF_setDoubleButtonText(getResources().getString(R.string.btn_cancel), getResources().getString(R.string.btn_del));
                tmp_dlg.setOnDismissListener(new DialogInterface.OnDismissListener() {
                    @Override
                    public void onDismiss(DialogInterface dialog) {
                        if(!((CustomDialog) dialog).CF_getCanceled()){

                            View tmp_parentView = (View)v.getParent();
                            int tmp_index = gridPhotoGroup.indexOfChild(tmp_parentView);


                            // ---------------------------------------------------------------------------------
                            // click한 삭제 버튼의 getParent()를 이용해 해당 썸네일을 삭제하고, 이미지 경로 또한 삭제한다.
                            // ---------------------------------------------------------------------------------
                            if(tmp_index >= 0 && tmp_index < gridPhotoGroup.getChildCount() && tmp_index < arrImgPath.size()) {
                                CommonFunction.CF_recycleBitmap((ImageView)gridPhotoGroup.getChildAt(tmp_index).findViewById(R.id.imgView));
                                gridPhotoGroup.removeViewAt(tmp_index);

                                removeFile(arrImgPath.get(tmp_index));
                                arrImgPath.remove(tmp_index);
                            }

                            // ---------------------------------------------------------------------------------
                            // 첨부사진이 없는 경우 Empty Text를 보인다.
                            // ---------------------------------------------------------------------------------
                            if(gridPhotoGroup.getChildCount() == 0){
                                TextView tmp_textEmpty = findViewById(R.id.textEmpty);
                                tmp_textEmpty.setVisibility(View.VISIBLE);
                                tmp_textEmpty.setImportantForAccessibility(IMPORTANT_FOR_ACCESSIBILITY_YES);
                            }

                            // ---------------------------------------------------------------------------------
                            //  썸네일 삭제 이벤트 호출
                            // ---------------------------------------------------------------------------------
                            if(listener != null){
                                listener.onDeletedPhoto(AddPhotoViewGroup.this);
                            }
                        }
                        else{
                            // ---------------------------------------------------------------------------------
                            //  썸네일 삭제 취소 이벤트 호출
                            // ---------------------------------------------------------------------------------
                            if(listener != null){
                                listener.onDeletCanceled(AddPhotoViewGroup.this, tmp_btnRemove);
                            }
                        }
                    }
                });
            }
        });

        gridPhotoGroup.addView(tmp_item);
        arrImgPath.add(p_imgPath);

        // ---------------------------------------------------------------------------------
        //  empty 문자열 hide
        // ---------------------------------------------------------------------------------
        TextView tmp_textEmpty = findViewById(R.id.textEmpty);
        tmp_textEmpty.setVisibility(View.INVISIBLE);
        tmp_textEmpty.setImportantForAccessibility(IMPORTANT_FOR_ACCESSIBILITY_NO);
    }

    /**
     * 마지막 사진 아이템 반환
     * @return  FrameLayout
     */
    public FrameLayout CF_getLastAddedPhotoView(){
        if(gridPhotoGroup.getChildCount() > 0){
            return (FrameLayout)gridPhotoGroup.getChildAt(gridPhotoGroup.getChildCount()-1);
        }
        return  null;
    }


    /**
     * 사진 썸네일 반환 함수
     * @param p_imgPath 이미지 파일 경로
     * @return  Bitmap
     */
    private Bitmap getPhotoThumb(String p_imgPath){

        BitmapFactory.Options tmp_option = new BitmapFactory.Options();
        tmp_option.inSampleSize = 4;

        // 이미지 썸네일 생성
        Bitmap tmp_bitmap = BitmapFactory.decodeFile(p_imgPath,tmp_option);

        if(tmp_bitmap != null){
            // 이미지 회전
            int tmp_orientation = CommonFunction.CF_getImageOrientation(p_imgPath);
            tmp_bitmap = CommonFunction.CF_rotateBitmap(tmp_bitmap, tmp_orientation);
        }

        return  tmp_bitmap;
    }

    /**
     * 썸네일 Bitmap 생성및 반환 함수<br/>
     * 썸네일 Bitmap은 MediaStore.Images.Thumbnails.getThumbnail 함수 이용<br/>
     * @param p_imgPath String
     * @param p_imgId   String
     * @return          Bitmap
     */
    private Bitmap getPhotoThumb(String p_imgPath, String p_imgId){

        // 이미지 썸네일 다운
        Bitmap tmp_bitmap = null;
        tmp_bitmap = MediaStore.Images.Thumbnails.getThumbnail(getContext().getContentResolver(), Integer.parseInt(p_imgId), MediaStore.Images.Thumbnails.MINI_KIND, null);

        if(tmp_bitmap != null){
            // 이미지 회전
            int tmp_orientation = CommonFunction.CF_getImageOrientation(p_imgPath);
            tmp_bitmap = CommonFunction.CF_rotateBitmap(tmp_bitmap, tmp_orientation);
        }

        return tmp_bitmap;
    }

    /**
     * 파일 삭제 함수
     * @param p_filePath    String
     */
    private void removeFile(String p_filePath){

        File tmp_file = new File(p_filePath);
        if(tmp_file.exists()){
            tmp_file.delete();
        }
    }

    /**
     * 이벤트 리스너 세팅 함수
     * @param p_listener    OnAddPhotoViewGroupEventListener
     */
    public void CE_setOnAddPhotoViewGroupEventListener(OnAddPhotoViewGroupEventListener p_listener){
        listener = p_listener;
    }

    /**
     * 이미지 recycle
     */
    public void CF_recycle(){

        for(int i = 0 ; i< gridPhotoGroup.getChildCount(); i++){

            CommonFunction.CF_recycleBitmap((ImageView)gridPhotoGroup.getChildAt(i).findViewById(R.id.imgView));
        }
    }

    /**
     * 추가한 이미지 경로 리스트 반환 함수
     * @return  ArrayList<String>
     */
    public ArrayList<String> CF_getImgPathList(){
        return arrImgPath;
    }

    /**
     * 추가되어 있는 사진 수 반환 함수
     * @return  int
     */
    public int CF_getPhotoCount(){
        return gridPhotoGroup.getChildCount();
    }
}
