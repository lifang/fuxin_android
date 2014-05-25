package com.fuwu.mobileim.activity;

import com.fuwu.mobileim.R;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;

public class BackPwdActicity extends Activity implements OnClickListener {

	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.backpwd);
		findViewById(R.id.exit).setOnClickListener(this);
	}

	public void onClick(View v) {
		Intent intent = new Intent(BackPwdActicity.this, LoginActivity.class);
		startActivity(intent);
		this.finish();
	}
}
