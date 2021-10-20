package com.epost.insu.activity.push;

import android.app.Activity;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.epost.insu.R;
import com.epost.insu.push.Define;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;

public class PushDetailActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_detail);
		
		String title = this.getIntent().getStringExtra(Define.KEY_TITLE);
		String message = this.getIntent().getStringExtra(Define.KEY_MESSAGE);
		if(TextUtils.isEmpty(message)){
			message = this.getIntent().getStringExtra(Define.KEY_BODY);
		}
		
		TextView titleTextView = (TextView)this.findViewById(R.id.text_detail_title);
		titleTextView.setText(title);
		
		TextView messageTextView = (TextView)this.findViewById(R.id.text_detail_message);
		messageTextView.setText(message);
			
		String imgUrl = this.getIntent().getStringExtra("img");
		if (!TextUtils.isEmpty(imgUrl)) {
			final ImageView image = (ImageView) this.findViewById(R.id.image_detail_img);
			ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(this).build();
	        ImageLoader.getInstance().init(config);
	        ImageLoader.getInstance().loadImage(imgUrl, new SimpleImageLoadingListener() {
	            @Override
	            public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
	            	image.setImageBitmap(loadedImage);
	            	image.setVisibility(View.VISIBLE);
	            }
	        });
		}
	}
}
