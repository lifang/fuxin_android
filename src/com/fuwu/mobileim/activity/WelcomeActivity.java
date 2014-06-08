package com.fuwu.mobileim.activity;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;

import com.fuwu.mobileim.R;
import com.fuwu.mobileim.util.Urlinterface;

public class WelcomeActivity extends Activity {

	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.welcome);
		final Intent intent = new Intent(WelcomeActivity.this,
				LoginActivity.class);
		final SharedPreferences spf = getSharedPreferences(Urlinterface.SHARED,
				0);
		if (spf.getBoolean("welcome", true)) {
			new Handler().postDelayed(new Runnable() {
				public void run() {
					spf.edit().putBoolean("welcome", false).commit();
					startActivity(intent);
					WelcomeActivity.this.finish();
				}
			}, 3000);
		} else {
			startActivity(intent);
			WelcomeActivity.this.finish();
		}
	}
}
