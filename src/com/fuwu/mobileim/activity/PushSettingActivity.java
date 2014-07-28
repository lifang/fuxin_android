package com.fuwu.mobileim.activity;

import android.app.Activity;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.Toast;

import com.baidu.mobstat.StatService;
import com.fuwu.mobileim.R;
import com.fuwu.mobileim.util.Urlinterface;
import com.fuwu.mobileim.view.SlipButton;
import com.fuwu.mobileim.view.SlipButton.OnChangedListener;

public class PushSettingActivity extends Activity implements OnClickListener,
		OnChangedListener,OnTouchListener {

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
		findViewById(R.id.exit).setOnTouchListener(this);
		pushsetting_sound = (SlipButton) findViewById(R.id.pushsetting_sound);
		pushsetting_music = (SlipButton) findViewById(R.id.pushsetting_music);
		pushsetting_shake = (SlipButton) findViewById(R.id.pushsetting_shake);
		pushsetting_sound.setCheck(sf.getBoolean("pushsetting_sound", true));
		pushsetting_music.setCheck(sf.getBoolean("pushsetting_music", true));
		pushsetting_shake.setCheck(sf.getBoolean("pushsetting_shake", true));
		pushsetting_sound.setOnChangedListener(this);
		pushsetting_music.setOnChangedListener(this);
		pushsetting_shake.setOnChangedListener(this);
	}

	public void onClick(View v) {
		PushSettingActivity.this.finish();
	}

	public void onChanged(boolean checkState, View v) {
		String key = "";
		String content = "";
		switch (v.getId()) {
		case R.id.pushsetting_music:
			key = "pushsetting_music";
			if (checkState) {
				content = "您打开了声音开关";
			} else {
				content = "您关闭了声音开关";
			}
			break;
		case R.id.pushsetting_shake:
			key = "pushsetting_shake";
			if (checkState) {
				content = "您打开了震动开关";
			} else {
				content = "您关闭了震动开关";
			}
			break;
		case R.id.pushsetting_sound:
			key = "pushsetting_sound";
			if (checkState) {
				content = "您打开了接受推送开关";
			} else {
				content = "您关闭了接受推送开关";
			}
			break;
		}
		Toast.makeText(this, content, Toast.LENGTH_SHORT).show();
		Editor editor = sf.edit();
		editor.putBoolean(key, checkState);
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

	@Override
	public boolean onTouch(View v, MotionEvent event) {
		if (event.getAction() == MotionEvent.ACTION_DOWN) {
			switch (v.getId()) {
			case R.id.exit:
				findViewById(R.id.exit).getBackground().setAlpha(70);
				break;
			}
		} else if (event.getAction() == MotionEvent.ACTION_UP) {
			switch (v.getId()) {
			case R.id.exit:
				findViewById(R.id.exit).getBackground().setAlpha(255);
				break;
			}
		}
		return false;
	}
}