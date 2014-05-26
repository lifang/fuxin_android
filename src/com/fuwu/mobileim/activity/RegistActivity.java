package com.fuwu.mobileim.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.fuwu.mobileim.R;
import com.fuwu.mobileim.util.FuXunTools;

/**
 * 作者: 张秀楠 时间：2014-5-24 下午3:21:40
 */
public class RegistActivity extends Activity implements OnClickListener,
		OnFocusChangeListener, OnCheckedChangeListener {
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
	public Button phone_ok;
	public Button yz_send;
	public boolean phone_btn = true;
	private RelativeLayout view;
	private String yznumber = "123456";
	private CheckBox agreement;
	private Button over;
	private boolean yz_boolean = false;

	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.regist);
		initialize();
	}

	// 初始化
	public void initialize() {
		findViewById(R.id.exit).setOnClickListener(this);
		name_text = (EditText) findViewById(R.id.name);
		pwd_text = (EditText) findViewById(R.id.pwd);
		pwds_text = (EditText) findViewById(R.id.pwds);
		phone_text = (EditText) findViewById(R.id.phone);
		yz_text = (EditText) findViewById(R.id.yz);
		name_text.setOnFocusChangeListener(this);
		pwd_text.setOnFocusChangeListener(this);
		pwds_text.setOnFocusChangeListener(this);
		yz_text.addTextChangedListener(new TextWatcher() {
			public void onTextChanged(CharSequence arg0, int arg1, int arg2,
					int arg3) {
			}

			public void beforeTextChanged(CharSequence arg0, int arg1,
					int arg2, int arg3) {
			}

			public void afterTextChanged(Editable arg0) {
				if (arg0.toString().equals(yznumber)) {
					yz_tag.setVisibility(View.GONE);
					yz_boolean = true;
					regist_btnOver();
				} else {
					yz_tag.setVisibility(View.VISIBLE);
					yz_boolean = false;
					regist_btnOver();
				}
			}
		});

		name_tag = (TextView) findViewById(R.id.name_tag);
		pwd_tag = (TextView) findViewById(R.id.pwd_tag);
		pwds_tag = (TextView) findViewById(R.id.pwds_tag);
		phone_tag = (TextView) findViewById(R.id.phone_tag);
		yz_tag = (TextView) findViewById(R.id.yz_tag);
		view = (RelativeLayout) findViewById(R.id.view);
		phone_ok = (Button) findViewById(R.id.phone_ok);
		phone_ok.setOnClickListener(this);
		yz_send = (Button) findViewById(R.id.yz_send);
		yz_send.setOnClickListener(this);
		agreement = (CheckBox) findViewById(R.id.agreement);
		agreement.setOnCheckedChangeListener(this);
		over = (Button) findViewById(R.id.regist_over);
		over.setOnClickListener(this);
		over.setClickable(false);
	}

	public boolean judge() {// 判断注册是否完成
		if (name_text.getText().toString().equals("")) {
			return false;
		}
		if (pwd_text.getText().toString().equals("")) {
			return false;
		}
		if (!pwd_text.getText().toString()
				.equals(pwds_text.getText().toString())) {
			return false;
		}
		if (phone_btn) {
			return false;
		}
		if (!yz_boolean) {
			return false;
		}
		if (!agreement.isChecked()) {
			return false;
		}
		return true;
	}

	public void regist_btnOver() {
		if (judge()) {
			over.setClickable(true);
			over.setBackgroundResource(R.drawable.login_btn);
		} else {
			over.setClickable(false);
			over.setBackgroundResource(R.drawable.regist_btn);
		}
	}

	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.exit:
			Intent intent = new Intent(RegistActivity.this, LoginActivity.class);
			startActivity(intent);
			this.finish();
			break;
		case R.id.regist_over:
			Log.i("Max", judge() + "");
			break;
		case R.id.phone_ok:
			if (phone_btn) {
				String phone = phone_text.getText().toString();
				if (phone.equals("")) {
					phone_tag.setVisibility(View.VISIBLE);
				} else {
					if (FuXunTools.isMobileNO(phone)) {
						phone_tag.setVisibility(View.GONE);
						phone_ok.setText("重填");
						phone_btn = false;
						phone_text.setEnabled(false);
						view.setBackgroundColor(getResources().getColor(
								R.color.regist_bg));
					} else {
						phone_tag.setVisibility(View.VISIBLE);
					}
				}
			} else {
				phone_btn = true;
				phone_text.setEnabled(true);
				phone_text.setText("");
				phone_ok.setText("确定");
				phone_text.requestFocus();// 获取焦点
				view.setBackgroundColor(getResources().getColor(R.color.white));
			}
			regist_btnOver();
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
				Log.i("Max", pwd_text.getText().toString() + "---"
						+ pwds_text.getText().toString());
				if (pwd_text.getText().toString()
						.equals(pwds_text.getText().toString())) {
					pwds_tag.setVisibility(View.GONE);
				} else {
					pwds_tag.setVisibility(View.VISIBLE);
				}
				break;
			}
			regist_btnOver();
		}
	}

	public void onCheckedChanged(CompoundButton arg0, boolean arg1) {
		regist_btnOver();
	}
}
