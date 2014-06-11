package com.fuwu.mobileim.activity;

import android.app.Activity;
import android.graphics.Rect;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewTreeObserver;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.view.WindowManager;
import android.widget.ImageView;

import com.fuwu.mobileim.R;
import com.fuwu.mobileim.util.ImageCacheUtil;
import com.fuwu.mobileim.util.ImageUtil;
import com.fuwu.mobileim.view.DragImageView;

/**
 * @作者 马龙
 * @时间 创建时间：2014-6-11 下午2:36:58
 */
public class ZoomImageActivity extends Activity {
	private ImageView mBack;
	private int window_width, window_height;// 控件宽度
	private int state_height;// 状态栏的高度
	private DragImageView dragImageView;// 自定义控件
	private ViewTreeObserver viewTreeObserver;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.chat_zoom_image);
		mBack = (ImageView) findViewById(R.id.chat_back);
		mBack.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				finish();
			}
		});

		String path = getIntent().getStringExtra("image_path");
		WindowManager manager = getWindowManager();
		window_width = manager.getDefaultDisplay().getWidth();
		window_height = manager.getDefaultDisplay().getHeight();
		mBack.setImageBitmap(ImageUtil.getBitmapScale(window_height,
				R.drawable.back, getResources()));
		dragImageView = (DragImageView) findViewById(R.id.chat_zoom_image);
		ImageCacheUtil.IMAGE_CACHE.get(path, dragImageView);
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
}
