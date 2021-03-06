package com.fuwu.mobileim.activity;

import java.io.File;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.BitmapFactory;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewTreeObserver;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.view.WindowManager;
import android.widget.ImageView;

import com.fuwu.mobileim.R;
import com.fuwu.mobileim.util.ImageUtil;
import com.fuwu.mobileim.util.Urlinterface;
import com.fuwu.mobileim.view.DragImageView;

/**
 * @作者 丁作强
 * @时间 2014-6-26 下午3:23:18
 */
public class ComtactZoomImageActivity extends Activity implements
		OnTouchListener {
	private ImageView mBack;
	private int window_width, window_height;// 控件宽度
	private int state_height;// 状态栏的高度
	private DragImageView dragImageView;// 自定义控件
	private ViewTreeObserver viewTreeObserver;

	@SuppressWarnings("deprecation")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.contact_info_zoom_image);
		mBack = (ImageView) findViewById(R.id.chat_back);
		mBack.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				finish();
			}
		});
		mBack.setOnTouchListener(this);
		String path = getIntent().getStringExtra("image_path");
		WindowManager manager = getWindowManager();
		window_width = manager.getDefaultDisplay().getWidth();
		window_height = manager.getDefaultDisplay().getHeight();
		dragImageView = (DragImageView) findViewById(R.id.chat_zoom_image);
		SharedPreferences sp = getSharedPreferences(Urlinterface.SHARED,
				Context.MODE_PRIVATE);
		int contact_id = sp.getInt("contact_id", 1);
		if (contact_id == 0) {
			dragImageView.setImageResource(R.drawable.system_user_face);
		} else {

			File f = new File(path);
			if (f.exists()) {
				// ImageCacheUtil.IMAGE_CACHE.get(path, dragImageView);
				dragImageView.setImageBitmap(ImageUtil.getLoacalBitmap(path));
			} else {
				dragImageView.setImageResource(R.drawable.moren);
			}
		}
		// 设置图片
		dragImageView.setmActivity(this);// 注入Activity.
		/** 测量状态栏高度 **/
		viewTreeObserver = dragImageView.getViewTreeObserver();
		viewTreeObserver
				.addOnGlobalLayoutListener(new OnGlobalLayoutListener() {
					@Override
					public void onGlobalLayout() {
						if (state_height == 0) {
							// 获取状况栏高度
							Rect frame = new Rect();
							getWindow().getDecorView()
									.getWindowVisibleDisplayFrame(frame);
							state_height = frame.top;
							dragImageView.setScreen_H(window_height
									- state_height);
							dragImageView.setScreen_W(window_width);
						}
					}
				});
	}

	@Override
	public boolean onTouch(View v, MotionEvent event) {
		if (v.getId() == R.id.chat_back) {
			if (event.getAction() == MotionEvent.ACTION_DOWN) {
				findViewById(R.id.chat_back).getBackground().setAlpha(70);
			} else if (event.getAction() == MotionEvent.ACTION_UP) {
				findViewById(R.id.chat_back).getBackground().setAlpha(255);
			}
		}
		return false;
	}
}
