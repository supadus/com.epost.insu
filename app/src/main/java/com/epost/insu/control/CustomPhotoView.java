package com.epost.insu.control;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.animation.Animation;
import android.widget.ImageView;
import android.widget.ViewSwitcher;

import com.epost.insu.R;
import com.epost.insu.common.CommonFunction;
import com.epost.insu.common.DeprecatedFunc;
import com.epost.insu.common.LogPrinter;
import com.epost.insu.event.OnBitmapDownLoadEventListener;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

/**
 * 커스텀 Image View
 * @since     :
 * @version   : 1.1
 * @author    : NJM
 * <pre>
 *  커스텀 Image View</u><br/>
 *  이미지 url을 전달받아 다운로드 후 ImageView에 그린다.<br/>
 * ======================================================================
 * 0.0.0    LSH_20171026    최초 등록
 * 0.0.0    NJM_20191101    리사이클러뷰 오류로 사진첨부시 앱이 죽는 현상 수정
 * 1.6.2    NJM_20210802    [2021년 대우 취약점] 2차본 : 부적절한 자원 해제
 * =======================================================================
 * copyright : (주)프로니스정보기술 | http://www.phronis.co.kr
 * </pre>
 */
public class CustomPhotoView extends ViewSwitcher {


    private OnBitmapDownLoadEventListener listener;

    private ImageView preView;

    private ImageView photoView;

    private String path;
    private Drawable previewDrawable;

    private ImageDownLoadTask imageDownLoadTask;


    private int background = Color.rgb(238, 238, 238);

    /**
     * 생성자
     * @param context
     * @param p_preViewResId
     */
    public CustomPhotoView(Context context, int p_preViewResId){
        super(context);

        previewDrawable = DeprecatedFunc.CF_getDrawable(getContext(), p_preViewResId);

        this.setInit();
        this.setUIControl();
    }
    public CustomPhotoView(Context context, Drawable p_preViewDrawable){
        super(context);

        this.previewDrawable = p_preViewDrawable;

        this.setInit();
        this.setUIControl();
    }
    public CustomPhotoView(Context context, AttributeSet attrs) {
        super(context, attrs);

        this.setInit();
        this.setAttribute(attrs);
        this.setUIControl();
    }

    @Override
    public void setDisplayedChild(int whichChild) {

        if(whichChild == 0) {
            Animation tmp_animIn = this.getInAnimation();
            Animation tmp_animOut = this.getOutAnimation();
            if (tmp_animIn != null || tmp_animOut != null) {
                this.setInAnimation(null);
                this.setOutAnimation(null);
                super.setDisplayedChild(whichChild);
                this.setInAnimation(tmp_animIn);
                this.setOutAnimation(tmp_animOut);
            } else {
                super.setDisplayedChild(whichChild);
            }
        }
        else {
            super.setDisplayedChild(whichChild);
        }
    }

    /**
     * 초기 세팅 함수
     */
    private void setInit(){
        this.path = "";
    }


    /**
     * 속성값 세팅함수
     * @param p_attr
     */
    private void setAttribute(AttributeSet p_attr){
        TypedArray tmp_typedArray = this.getContext().obtainStyledAttributes(p_attr, R.styleable.CustomPhotoView);

        Drawable tmp_drawable = tmp_typedArray.getDrawable(R.styleable.CustomPhotoView_preView);

        if(tmp_drawable != null){
            this.previewDrawable = tmp_drawable;
        }

        tmp_typedArray.recycle();
    }


    /**
     * UI 생성 및 세팅 함수
     */
    private void setUIControl(){

        ViewSwitcher.LayoutParams lp_preView = new ViewSwitcher.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);

        this.preView = new ImageView(this.getContext());
        this.preView.setAdjustViewBounds(true);
        this.preView.setBackgroundColor(background);
        this.preView.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
        this.preView.setPadding(CommonFunction.CF_convertDipToPixel(getContext(), 5),
                CommonFunction.CF_convertDipToPixel(getContext(), 5),
                CommonFunction.CF_convertDipToPixel(getContext(), 5),
                CommonFunction.CF_convertDipToPixel(getContext(), 5));
        this.preView.setLayoutParams(lp_preView);
        this.preView.setImageDrawable(this.previewDrawable);

        // imageView 세팅
        ViewSwitcher.LayoutParams tmp_lpImageView = new ViewSwitcher.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
        tmp_lpImageView.gravity = Gravity.CENTER;

        this.photoView = new ImageView(this.getContext());
        this.photoView.setScaleType(ImageView.ScaleType.CENTER_CROP);
        this.photoView.setLayoutParams(tmp_lpImageView);

        this.addView(this.preView);
        this.addView(this.photoView);
    }


    /**
     * 로딩이미지 백그라운드 색상 세팅
     * @param p_color
     */
    public void CF_setPreViewBackground(int p_color){
        background = p_color;
        this.preView.setBackgroundColor(background);
    }

    /**
     * 로딩이미지 세팅
     * @param p_drawable
     */
    public void CF_setPreViewImage(Drawable p_drawable){
        this.preView.setImageDrawable(p_drawable);
    }

    /**
     * 로딩이미지 scaleType 세팅
     * @param p_scaleType
     */
    @SuppressWarnings("unused")
    public void CF_setPhotoViewScaleType(ImageView.ScaleType p_scaleType){
        this.photoView.setScaleType(p_scaleType);
    }

    /**
     * 이미지 adjustViewBounds 세팅
     * @param p_adjustViewBounds
     */
    @SuppressWarnings("unused")
    public void CF_setPhotoViewAdjust(boolean p_adjustViewBounds){
        this.photoView.setAdjustViewBounds(p_adjustViewBounds);
    }

    /**
     * 로딩 이미지 Show
     * @param p_flagRecycle if true, 세팅된 이미지 recycle
     */
    @SuppressWarnings("unused")
    public void CF_showPrevView(boolean p_flagRecycle){
        if(p_flagRecycle){
            CF_showPrevView();
        }else{
            this.path = "";
            this.CF_setPreViewBackground(background);
            this.setDisplayedChild(0);
        }
    }

    /**
     * 로딩 이미지 Show<br/>
     * 기존에 세팅된 이미지는 recycle 처리
     */
    public void CF_showPrevView(){

        CF_recycle();

        this.path = "";
        this.CF_setPreViewBackground(background);
        this.setDisplayedChild(0);
    }


    /**
     * 이미지 세팅
     * @param p_path
     * @param p_bitmap
     */
    @SuppressWarnings("unused")
    public void CF_drawPhoto(String p_path, Bitmap p_bitmap){
        this.path = p_path;
        this.photoView.setImageBitmap(p_bitmap);
        this.setDisplayedChild(1);
    }

    /**
     * 이미지 사진 recycle
     */
    public void CF_recycle(){
        // 2019-11-01    노지민     : 리사이클러뷰 오류로 사진첨부시 앱이 죽는 현상 수정
        // CommonFunction.CF_recycleBitmap(this.photoView);


        try {
            //Drawable tmp_drawable = iv.getDrawable();

            if(this.photoView.getDrawable() instanceof BitmapDrawable) {
                //  Bitmap tmp_bitmap = ((BitmapDrawable)tmp_drawable).getBitmap();

                if(((BitmapDrawable) this.photoView.getDrawable()).getBitmap() != null) {
                    ((BitmapDrawable) this.photoView.getDrawable()).getBitmap().recycle();
                }
            }
            ((BitmapDrawable) this.photoView.getDrawable()).setCallback(null);

        } catch (NullPointerException e) {
            LogPrinter.CF_line();
            LogPrinter.CF_debug("Bitmap 자원 해제 실패2 : is Null");
        }


        this.photoView.setImageBitmap(null);
    }


    /**
     * 이미지 경로 반환
     * @return
     */
    @SuppressWarnings("unused")
    public String CF_getPath(){
        return this.path;
    }

    /**
     * 해당 경로의 이미지를 다운로드하여 세팅<br/>
     * @param p_path
     */
    @SuppressWarnings("unused")
    public void CF_drawPhoto(String p_path){

        if(!this.path.equals(p_path) && imageDownLoadTask != null){
            imageDownLoadTask.cancel(true);
            imageDownLoadTask = null;
        }

        if(!this.path.equals(p_path)){
            this.path = p_path;

            this.imageDownLoadTask = new ImageDownLoadTask(this.path);
            this.imageDownLoadTask.execute();
            //this.imageDownLoadTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, "");
        }
    }

    /**
     * 사진 다운로드 완료 이벤트 리스너 세팅 함수
     * @param p_listener
     */
    @SuppressWarnings("unused")
    public void CE_setOnDownLoadCompletedEventListener(OnBitmapDownLoadEventListener p_listener){
        this.listener = p_listener;
    }




    /**
     * @copyright : (주)프로니스정보기술 | http://www.phronis.co.kr
     *
     * @project   : 모바일슈랑스 구축
     * @pakage   : com.epost.insu.control
     * @fileName  : CustomPhotoView.java
     *
     * @Title     : 이미지 다운로드 Task
     * @author    : 이수행
     * @created   : 2017-11-21
     * @version   : 1.0
     *
     * @note      : <u>이미지 다운로드 Task</u><br/>
     *               {@link URLConnection}을 이용해 이미지 다운로드<br/>
     * ======================================================================
     * 수정 내역
     * NO      날짜          작업자       내용
     * 01      2017-11-21    이수행       최초 등록
     * =======================================================================
     */
    private class ImageDownLoadTask extends AsyncTask<String, Void, Bitmap> {
        private String url;

        public ImageDownLoadTask(String p_path) {
            this.url = p_path;
        }

        @Override
        protected void onPreExecute(){
            super.onPreExecute();

            setBackgroundColor(background);
            CF_setPreViewImage(previewDrawable);
            setDisplayedChild(0);
        }

        @Override
        // Actual download method, run in the task thread
        protected Bitmap doInBackground(String... params) {
            // params comes from the execute() call: params[0] is the url.
            return  getImageBitmapFromURL(url);
        }

        @Override
        protected void onCancelled(Bitmap p_bitmap){
            super.onCancelled(p_bitmap);
        }

        @Override
        // Once the image is downloaded, associates it to the imageView
        protected void onPostExecute(Bitmap p_bitmap) {


            if(p_bitmap != null){

                if(url.equals(path)){
                    photoView.setImageBitmap(p_bitmap);
                    setDisplayedChild(1);

                    if(listener != null){
                        listener.onDownLoad(url, p_bitmap);
                    }
                }
            }
            else{
                CF_setPreViewBackground(Color.rgb(0, 0, 0));
                CF_setPreViewImage(previewDrawable);
                setDisplayedChild(0);
            }
        }

        /**
         * URL로 부터 이미지 Bitmap 다운로드 함수
         * @param p_urlPath
         * @return
         * @throws IOException
         */
        private Bitmap getImageBitmapFromURL(String p_urlPath) {
            Bitmap tmp_bitmap = null;

            // ----------------------------------------------------------------------------------------------------
            //	URL 이미지를 다운로드 하여 Bitmap 으로 세팅
            // ----------------------------------------------------------------------------------------------------
            BufferedInputStream tmp_bufferedInputStream = null;
            try {
                URL url = new URL(p_urlPath);

                URLConnection urlConnection = url.openConnection();
                urlConnection.setConnectTimeout(10000);
                urlConnection.setReadTimeout(30000);
                urlConnection.connect();

                tmp_bufferedInputStream = new BufferedInputStream(urlConnection.getInputStream());
                tmp_bitmap = BitmapFactory.decodeStream(tmp_bufferedInputStream, null, null);
            } catch (MalformedURLException e) {
                LogPrinter.CF_line();
                LogPrinter.CF_debug(getContext().getResources().getString(R.string.log_fail_no_exists_photo));
            } catch (IOException e) {
                LogPrinter.CF_line();
                LogPrinter.CF_debug(getContext().getResources().getString(R.string.log_fail_download_photo));
            } finally {
                if(tmp_bufferedInputStream != null)
                    try {
                        tmp_bufferedInputStream.close();
                    } catch (IOException e) {
                        LogPrinter.CF_line();
                        LogPrinter.CF_debug(getResources().getString(R.string.log_fail_close_stream));
                    }
            }


            return tmp_bitmap;
        }
    }

}
