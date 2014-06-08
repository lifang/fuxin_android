package com.fuwu.mobileim.activity;

import android.app.Activity;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Toast;

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
		pushsetting_sound.setOnChangedListener(this);
		pushsetting_music.setOnChangedListener(this);
		pushsetting_shake.setOnChangedListener(this);
	}

	public void onClick(View v) {
		PushSettingActivity.this.finish();
	}

	public void onChanged(boolean checkState, View v) {
		String key = "";
		boolean values = true;
		switch (v.getId()) {
		case R.id.pushsetting_music:
			key = "pushsetting_music";
			if (checkState) {
				Toast.makeText(this, "pushsetting_music关闭了", Toast.LENGTH_SHORT)
						.show();
				values = false;
			} else {
				Toast.makeText(this, "pushsetting_music打开了", Toast.LENGTH_SHORT)
						.show();
				values = true;
			}
			break;
		case R.id.pushsetting_shake:
			key = "pushsetting_shake";
			if (checkState) {
				Toast.makeText(this, "pushsetting_shake关闭了", Toast.LENGTH_SHORT)
						.show();
				values = false;
			} else {
				Toast.makeText(this, "pushsetting_shake打开了", Toast.LENGTH_SHORT)
						.show();
				values = true;
			}
			break;
		case R.id.pushsetting_sound:
			key = "pushsetting_sound";
			if (checkState) {
				Toast.makeText(this, "pushsetting_sound关闭了", Toast.LENGTH_SHORT)
						.show();
				values = false;
			} else {
				Toast.makeText(this, "pushsetting_sound打开了", Toast.LENGTH_SHORT)
						.show();
				values = true;
			}
			break;
		}

		Editor editor = sf.edit();
		editor.putBoolean(key, values);
		editor.commit();
	}
}