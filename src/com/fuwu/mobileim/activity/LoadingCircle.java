package com.fuwu.mobileim.activity;

import android.app.Activity;
import android.os.Bundle;
import android.widget.ProgressBar;

import com.fuwu.mobileim.R;

/**
 * @作者 马龙
 * @时间 创建时间：2014-7-1 上午10:13:30
 */
public class LoadingCircle extends Activity {

	ProgressBar load;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.loading);
		load = (ProgressBar) findViewById(R.id.loadingcircle);
	}
}
