package com.fuwu.mobileim.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.Toast;

import com.fuwu.mobileim.R;
import com.fuwu.mobileim.util.HttpUtil;
import com.fuwu.mobileim.util.Urlinterface;

/**
 * 作者: 张秀楠 时间：2014-5-23 下午4:34:03
 */
public class LoginActivity extends Activity implements OnClickListener,
		Urlinterface {
	public EditText user_text;
	public EditText pwd_text;
	private String user;
	private String pwd;

	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.login);
		findViewById(R.id.regist).setOnClickListener(this);
		findViewById(R.id.forgetpwd).setOnClickListener(this);
		findViewById(R.id.login_btn).setOnClickListener(this);
		initialize();// 初始化
	}

	// 初始化
	public void initialize() {
		user_text = (EditText) findViewById(R.id.user);
		pwd_text = (EditText) findViewById(R.id.pwd);
	}

	public void onClick(View v) {
		Intent intent = new Intent();
		switch (v.getId()) {
		case R.id.regist:
			intent.setClass(LoginActivity.this, RegistActivity.class);
			startActivity(intent);
			this.finish();
			break;
		case R.id.forgetpwd:
			intent.setClass(LoginActivity.this, BackPwdActicity.class);
			startActivity(intent);
			this.finish();
			break;
		case R.id.login_btn:
			user = user_text.getText().toString();
			pwd = pwd_text.getText().toString();
			if (user.equals("") && pwd.equals("")) {
				Toast.makeText(LoginActivity.this, "用户名或密码不可为空",
						Toast.LENGTH_SHORT).show();
			} else {
				intent.setClass(LoginActivity.this, MainActivity.class);
				startActivity(intent);
				this.finish();
			}
			break;
		}
	}

	class test implements Runnable {
		public void run() {
			new HttpUtil().doPost2("https://118.242.18.189/api/Message",
					"\"CglNb2NrVG9rZW4QAQ==\"");
		}
	}
}
