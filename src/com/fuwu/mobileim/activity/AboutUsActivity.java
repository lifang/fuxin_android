package com.fuwu.mobileim.activity;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver.OnPreDrawListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.fuwu.mobileim.R;
import com.fuwu.mobileim.model.Models.BlockContactRequest;
import com.fuwu.mobileim.model.Models.BlockContactResponse;
import com.fuwu.mobileim.pojo.ShortContactPojo;
import com.fuwu.mobileim.util.DBManager;
import com.fuwu.mobileim.util.FuXunTools;
import com.fuwu.mobileim.util.FxApplication;
import com.fuwu.mobileim.util.HttpUtil;
import com.fuwu.mobileim.util.Urlinterface;
import com.fuwu.mobileim.view.CircularImage;
import com.fuwu.mobileim.view.MyDialog;

public class AboutUsActivity extends Activity {
	private ImageButton about_us_back;// 返回按钮

	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.about_us);

		about_us_back = (ImageButton) findViewById(R.id.about_us_back);
		about_us_back.setOnTouchListener(new View.OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				if (event.getAction() == MotionEvent.ACTION_DOWN) {
					about_us_back.getBackground().setAlpha(70);// 设置图片透明度0~255，0完全透明，255不透明
																		// imgButton.invalidate();
				} else if (event.getAction() == MotionEvent.ACTION_UP) {
					about_us_back.getBackground().setAlpha(255);// 还原图片
				}
				return false;
			}
		});
		about_us_back.setOnClickListener(listener1);// 给返回按钮设置监听
	}


	private View.OnClickListener listener1 = new View.OnClickListener() {
		@Override
		public void onClick(View v) {
			AboutUsActivity.this.finish();
		}
	};

	
}
