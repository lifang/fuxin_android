package com.fuwu.mobileim.activity;

import android.app.Activity;
import android.os.Bundle;
import android.os.Environment;
import android.util.DisplayMetrics;
import android.util.Log;
import android.widget.ImageView;
import com.fuwu.mobileim.R;
import com.fuwu.mobileim.util.ImageUtil;

/**
 * @作者 马龙
 * @时间 创建时间：2014-6-5 下午4:03:40
 */
public class TestActivity extends Activity {

	private ImageView img;
	private int width;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.chat_item_right);
		img = (ImageView) findViewById(R.id.right_img);
		DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
		width = displayMetrics.widthPixels;
		long t = System.currentTimeMillis();
		img.setImageBitmap(ImageUtil.createImageThumbnail(
				Environment.getExternalStorageDirectory() + "/fuxin/3.jpg",
				width));
		Log.i("FuWu", "runTime:" + (System.currentTimeMillis() - t));
	}

}
