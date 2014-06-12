package com.fuwu.mobileim.activity;

import android.app.Activity;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;

import com.baidu.mobstat.StatService;
import com.fuwu.mobileim.R;
import com.fuwu.mobileim.util.Urlinterface;
import com.fuwu.mobileim.view.SlipButton;
import com.fuwu.mobileim.view.SlipButton.OnChangedListener;

public class PushSettingActivity extends Activity implements OnClickListener,
		OnChangedListener {

	private SlipButton pushsetting_sound;
	private SlipButton pushsetting_music;
	private SlipButton pushsetting_shake;
	public SharedPreferences sf;

	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.pushsetting);

		initialize();
	}

	public void initialize() {
		sf = getSharedPreferences(Urlinterface.SHARED, 0);
		findViewById(R.id.exit).setOnClickListener(this);
		pushsetting_sound = (SlipButton) findViewById(R.id.pushsetting_sound);
		pushsetting_music = (SlipButton) findViewById(R.id.pushsetting_music);
		pushsetting_shake = (SlipButton) findViewById(R.id.pushsetting_shake);
		pushsetting_sound.setCheck(sf.getBoolean("pushsetting_sound", false));
		pushsetting_music.setCheck(sf.getBoolean("pushsetting_music", false));
		pushsetting_shake.setCheck(sf.getBoolean("pushsetting_shake", false));
		pushsetting_sound.setOnChangedListener(this);
		pushsetting_music.setOnChangedListener(this);
		pushsetting_shake.setOnChangedListener(this);
	}

	public void onClick(View v) {
		PushSettingActivity.this.finish();
	}

	public void onChanged(boolean checkState, View v) {
		String key = "";
		boolean values = false;
		switch (v.getId()) {
		case R.id.pushsetting_music:
			key = "pushsetting_music";
			if (checkState) {
				values = true;
			} else {
				values = false;
			}
			break;
		case R.id.pushsetting_shake:
			key = "pushsetting_shake";
			if (checkState) {
				values = true;
			} else {
				values = false;
			}
			break;
		case R.id.pushsetting_sound:
			key = "pushsetting_sound";
			if (checkState) {
				values = true;
			} else {
				values = false;
			}
			break;
		}

		Editor editor = sf.edit();
		editor.putBoolean(key, values);
		editor.commit();
	}

	public void onResume() {
		super.onResume();

		/**
		 * 页面起始（每个Activity中都需要添加，如果有继承的父Activity中已经添加了该调用，那么子Activity中务必不能添加）
		 * 不能与StatService.onPageStart一级onPageEnd函数交叉使用
		 */
		StatService.onResume(this);
	}

	public void onPause() {
		super.onPause();

		/**
		 * 页面结束（每个Activity中都需要添加，如果有继承的父Activity中已经添加了该调用，那么子Activity中务必不能添加）
		 * 不能与StatService.onPageStart一级onPageEnd函数交叉使用
		 */
		StatService.onPause(this);
	}
}