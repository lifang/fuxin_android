package com.fuwu.mobileim.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.telephony.TelephonyManager;
import android.util.Log;

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
		// 创建电话管理
		TelephonyManager tm = (TelephonyManager) this
				.getSystemService(Context.TELEPHONY_SERVICE);
		try {
			String phoneId = tm.getLine1Number();
			if (!phoneId.equals("")) {
				String str = phoneId.substring(3, phoneId.length());
				spf.edit().putString("phone", str).commit();
			}
		} catch (Exception e) {
		}

		// if (spf.getBoolean("welcome", true)) {
		new Handler().postDelayed(new Runnable() {
			public void run() {
				spf.edit().putBoolean("welcome", false).commit();
				startActivity(intent);
				WelcomeActivity.this.finish();
			}
		}, 3000);
		// } else {
		// startActivity(intent);
		// WelcomeActivity.this.finish();
		// }
	}
}
