package com.fuwu.mobileim.activity;

import java.util.HashMap;
import java.util.Map;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.Toast;

import com.fuwu.mobileim.R;
import com.fuwu.mobileim.model.Models.AuthenticationRequest;
import com.fuwu.mobileim.model.Models.AuthenticationResponse;
import com.fuwu.mobileim.util.FxApplication;
import com.fuwu.mobileim.util.HttpUtil;
import com.fuwu.mobileim.util.Urlinterface;
import com.google.protobuf.InvalidProtocolBufferException;

/**
 * 作者: 张秀楠 时间：2014-5-23 下午4:34:03
 */
public class LoginActivity extends Activity implements OnClickListener,
		Urlinterface {
	public EditText user_text;
	public EditText pwd_text;
	private String user;
	private String pwd;
	private FxApplication fx;
	private ProgressDialog prodialog;
	private String error_code;
	@SuppressLint("HandlerLeak")
	private Handler handler = new Handler() {
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			Intent intent = new Intent();
			prodialog.dismiss();
			switch (msg.what) {
			case 0:
				intent.setClass(LoginActivity.this, FragmengtActivity.class);
				startActivity(intent);
				LoginActivity.this.finish();
				break;
			case 1:
				Map<String, String> map = getErrorMap();
				if (!error_code.equals("")) {
					Toast.makeText(LoginActivity.this, map.get(error_code),
							Toast.LENGTH_SHORT).show();
				}
				break;
			}
		}
	};

	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.login);
		fx = (FxApplication) getApplication();
		findViewById(R.id.regist).setOnClickListener(this);
		findViewById(R.id.forgetpwd).setOnClickListener(this);
		findViewById(R.id.login_btn).setOnClickListener(this);
		initialize();// 初始化
	}

	// 初始化
	public void initialize() {
		user_text = (EditText) findViewById(R.id.user);
		pwd_text = (EditText) findViewById(R.id.pwd);
		user_text.setText("15862373890");
		pwd_text.setText("111111");
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
			intent.setClass(LoginActivity.this, ResetPasswordActicity.class);
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
				prodialog = new ProgressDialog(LoginActivity.this);
				prodialog.setMessage("努力登陆中..");
				prodialog.setCanceledOnTouchOutside(false);
				prodialog.show();
				new Thread(new Login_Post()).start();
			}
			break;
		}
	}

	// 短信验证
	class Login_Post implements Runnable {
		public void run() {
			try {
				AuthenticationRequest.Builder builder = AuthenticationRequest
						.newBuilder();
				builder.setUserName(user);
				builder.setPassword(pwd);
				AuthenticationRequest request = builder.build();
				AuthenticationResponse response = AuthenticationResponse
						.parseFrom(HttpUtil.sendHttps(request.toByteArray(),
								Urlinterface.LOGIN, "POST"));
				if (response.getIsSucceed()) {
					fx.setUser_id(response.getUserId());
					fx.setToken(response.getToken());
					handler.sendEmptyMessage(0);

				} else {
					Log.i("Max", "errorCode:" + response.getErrorCode());
					error_code = response.getErrorCode().toString();
					handler.sendEmptyMessage(1);
				}
			} catch (InvalidProtocolBufferException e) {
				e.printStackTrace();
			}
		}
	}

	public Map<String, String> getErrorMap() {
		Map<String, String> map = new HashMap<String, String>();
		map.put("InvalidUserName", "用户名不存在");
		map.put("InvalidPassword", "密码不正确");
		map.put("InvalidPasswordExceedCount", "密码错误次数过多");
		map.put("NotActivate", "服务器无响应");
		return map;
	}
}
