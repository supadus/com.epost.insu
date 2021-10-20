package com.epost.insu.common;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.ColorDrawable;
import androidx.annotation.NonNull;
import androidx.annotation.StyleRes;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;

import com.epost.insu.R;

/**
 * @copyright : (주)프로니스정보기술 | http://www.phronis.co.kr
 *
 * @project   : 모바일슈랑스 구축
 * @pakage   : com.epost.insu.common
 * @fileName  : ImageProgressDialog.java
 *
 * @Title     : 이미지 프로그레스 다이얼로그
 * @author    : 이수행
 * @created   : 2017-08-08
 * @version   : 1.0
 *
 * @note      : 이미지 프로그레스 다이얼로그<br/>
 *               {@link #onAttachedToWindow()} 시점에 Animation Start<br/>
 *               {@link #onDetachedFromWindow()} 시점에 Animation Stop<br/><br/>
 *
 *               <prev>
*                   private ImageProgressDialog progressDialog;         // 이미지 프로그레스 다이얼로그
 *
 *                  // 이미지 프로그레스 다이얼로그 show
 *                  public void CF_showProgressDialog(){
 *                      if(!isDestroyed()) {
 *                          if (progressDialog == null) {
 *                              progressDialog = new ImageProgressDialog(this);
 *                          }

 *                      if (progressDialog.isShowing() == false)
 *                          progressDialog.show();
 *                      }
 *                  }
 *
 *                  // 이미지 프로그레스 다이얼로그 dismiss
 *                  public void CF_dismissProgressDialog(){
 *                      if (progressDialog != null && progressDialog.isShowing()) {
 *                          progressDialog.dismiss();
 *                      }
 *                  }
 *               </prev>
 *
 * ======================================================================
 * 수정 내역
 * NO      날짜          작업자       내용
 * 01      2017-08-08    이수행       최초 등록
 * =======================================================================
 */
public class ImageProgressDialog extends Dialog{

    /** 이미지 Animation Drawable */
    private AnimationDrawable animDrawable;

    /**
     * 생성자
     * @param context       Context
     */
    public ImageProgressDialog(@NonNull Context context) {
        super(context);

        setInit();
        setUIControl();
    }

    /**
     * 생성자
     * @param context       Context
     * @param themeResId    int
     */
    public ImageProgressDialog(@NonNull Context context, @StyleRes int themeResId) {
        super(context, themeResId);

        setInit();
        setUIControl();
    }

    @Override
    public void onAttachedToWindow() {
        super.onAttachedToWindow();

        if(animDrawable.isRunning() == false){
            animDrawable.start();
        }
    }

    @Override
    public void onDetachedFromWindow() {
        super.onDetachedFromWindow();

        if(animDrawable.isRunning()){
            animDrawable.stop();
        }
    }

    @Override
    public void onBackPressed() {
        //super.onBackPressed();
    }

    /**
     * 초기 세팅 함수
     */
    private void setInit(){
        WindowManager.LayoutParams windowManagerLayoutParams = new WindowManager.LayoutParams();
        windowManagerLayoutParams.flags = WindowManager.LayoutParams.FLAG_DIM_BEHIND;
        windowManagerLayoutParams.dimAmount = 0.8f;
        getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        getWindow().setAttributes(windowManagerLayoutParams);

        setCanceledOnTouchOutside(false);
        setContentView(R.layout.dialog_image);
        //setCancelable(false);

        animDrawable = (AnimationDrawable)DeprecatedFunc.CF_getDrawable(getContext(), R.drawable.img_progress);
        animDrawable.setOneShot(false);

    }

    /**
     * UI 생성 및 세팅 함수
     */
    private void setUIControl(){

        ImageView tmp_img = findViewById(R.id.img);
        tmp_img.setImageDrawable(animDrawable);
    }
}
