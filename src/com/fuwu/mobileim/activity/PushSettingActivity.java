package com.fuwu.mobileim.activity;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;

import com.fuwu.mobileim.R;

public class PushSettingActivity extends Activity implements OnClickListener {

	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.pushsetting);
		findViewById(R.id.exit).setOnClickListener(this);
	}

	public void onClick(View v) {
		PushSettingActivity.this.finish();
	}
}