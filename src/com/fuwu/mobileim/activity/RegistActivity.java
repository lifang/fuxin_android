package com.fuwu.mobileim.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.widget.EditText;
import android.widget.TextView;

import com.fuwu.mobileim.R;

/**
 * 作者: 张秀楠 时间：2014-5-24 下午3:21:40
 */
public class RegistActivity extends Activity implements OnClickListener,
		OnFocusChangeListener {
	public EditText name_text;
	public EditText pwd_text;
	public EditText pwds_text;
	public EditText phone_text;
	public EditText yz_text;
	public TextView name_tag;
	public TextView pwd_tag;
	public TextView pwds_tag;
	public TextView phone_tag;
	public TextView yz_tag;

	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.regist);
		initialize();
	}

	// 初始化
	public void initialize() {
		findViewById(R.id.exit).setOnClickListener(this);
		findViewById(R.id.regist_over).setOnClickListener(this);

		name_text = (EditText) findViewById(R.id.name);
		pwd_text = (EditText) findViewById(R.id.pwd);
		pwds_text = (EditText) findViewById(R.id.pwds);
		phone_text = (EditText) findViewById(R.id.phone);
		yz_text = (EditText) findViewById(R.id.yz);
		name_text.setOnFocusChangeListener(this);
		pwd_text.setOnFocusChangeListener(this);
		pwds_text.setOnFocusChangeListener(this);
		phone_text.setOnFocusChangeListener(this);
		yz_text.setOnFocusChangeListener(this);

		name_tag = (TextView) findViewById(R.id.name_tag);
		pwd_tag = (TextView) findViewById(R.id.pwd_tag);
		pwds_tag = (TextView) findViewById(R.id.pwds_tag);
		phone_tag = (TextView) findViewById(R.id.phone_tag);
		yz_tag = (TextView) findViewById(R.id.yz_tag);
	}

	public boolean judge() {// 判断注册是否完成
		if (name_text.getText().toString().equals("")) {
		}
		return true;
	}

	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.exit:
			Intent intent = new Intent(RegistActivity.this, LoginActivity.class);
			startActivity(intent);
			this.finish();
			break;
		case R.id.regist_over:
			if (name_text.getText().toString().equals("")) {

			}
			break;
		}
	}

	// EditText失去焦点时的处理
	public void onFocusChange(View arg0, boolean arg1) {
		if (!arg1) {
			switch (arg0.getId()) {
			case R.id.name:
				String name = name_text.getText().toString();
				if (name.equals("") || name.length() > 8) {
					name_tag.setVisibility(View.VISIBLE);
				} else {
					name_tag.setVisibility(View.GONE);
				}
				break;
			case R.id.pwd:
				String pwd = pwd_text.getText().toString();
				if (pwd.equals("")) {
					pwd_tag.setVisibility(View.VISIBLE);
				} else {
					pwd_tag.setVisibility(View.GONE);
				}
				break;
			case R.id.pwds:
				if (pwd_text.getText().toString()
						.equals(pwds_text.getText().toString())) {
					pwd_tag.setVisibility(View.VISIBLE);
				} else {
					pwd_tag.setVisibility(View.GONE);
				}
				break;
			case R.id.phone:
				String phone = phone_text.getText().toString();
				if (phone.equals("")) {
					pwd_tag.setVisibility(View.VISIBLE);
				} else {
					pwd_tag.setVisibility(View.GONE);
				}
				break;
			case R.id.yz:

				break;
			}
		}
	}
}
