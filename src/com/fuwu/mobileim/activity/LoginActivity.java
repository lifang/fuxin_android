package com.fuwu.mobileim.activity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.Toast;

import com.baidu.mobstat.StatService;
import com.fuwu.mobileim.R;
import com.fuwu.mobileim.model.Models.AuthenticationRequest;
import com.fuwu.mobileim.model.Models.AuthenticationResponse;
import com.fuwu.mobileim.util.FuXunTools;
import com.fuwu.mobileim.util.FxApplication;
import com.fuwu.mobileim.util.HttpUtil;
import com.fuwu.mobileim.util.Urlinterface;
import com.fuwu.mobileim.view.CircularImage;

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
	private SharedPreferences spf;
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
				if (!error_code.equals("")) {
					String errorString = fx.error_map.get(error_code);
					if (errorString == null) {
						Toast.makeText(LoginActivity.this, "登陆失败",
								Toast.LENGTH_SHORT).show();
					} else {
						Toast.makeText(LoginActivity.this, errorString,
								Toast.LENGTH_SHORT).show();
					}
				}
				break;
			case 2:
				Toast.makeText(LoginActivity.this, "网络连接异常", Toast.LENGTH_SHORT)
						.show();
				break;
			}
		}
	};

	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.login);
		fx = (FxApplication) getApplication();
		spf = getPreferences(0);
		findViewById(R.id.regist).setOnClickListener(this);
		findViewById(R.id.forgetpwd).setOnClickListener(this);
		findViewById(R.id.login_btn).setOnClickListener(this);
		initialize();// 初始化
		// 百度统计
		StatService.setOn(this, StatService.EXCEPTION_LOG);
	}

	// 初始化
	public void initialize() {
		user_text = (EditText) findViewById(R.id.user);
		pwd_text = (EditText) findViewById(R.id.pwd);
		CircularImage head = (CircularImage) findViewById(R.id.head);
		int uid = spf.getInt("user_id", 0);
		if (uid != 0) {
			FuXunTools.set_img(uid, head);
		}
		String user = spf.getString("user", "null");
		// user_text.setText("MockUserName");
		// user_text.setText("15862373890");
		// user_text.setText("18913536561");
		// user_text.setText("15862373890");
		user_text.setText("18711111120");
		pwd_text.setText("111111");
		if (!user.equals("null")) {
			user_text.setText(user);
		}
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
			if (FuXunTools.isConnect(this)) {
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
			} else {
				Toast.makeText(LoginActivity.this, R.string.no_internet,
						Toast.LENGTH_SHORT).show();
			}
		}
	}

	// 登陆
	class Login_Post implements Runnable {
		public void run() {
			try {
				AuthenticationRequest.Builder builder = AuthenticationRequest
						.newBuilder();
				builder.setUserName(user);
				builder.setPassword(pwd);
				AuthenticationRequest request = builder.build();
				byte[] by = HttpUtil.sendHttps(request.toByteArray(),
						Urlinterface.LOGIN, "POST");
				if (by != null && by.length > 0) {
					AuthenticationResponse response = AuthenticationResponse
							.parseFrom(by);
					if (response.getIsSucceed()) {
						fx.setUser_id(response.getUserId());
						fx.setToken(response.getToken());
						spf.edit().putInt("user_id", response.getUserId())
								.commit();
						spf.edit()
								.putString("user",
										user_text.getText().toString())
								.commit();
						handler.sendEmptyMessage(0);
					} else {
						Log.i("Max", "errorCode:" + response.getErrorCode());
						error_code = response.getErrorCode().toString();
						handler.sendEmptyMessage(1);
					}
				} else {
					Toast.makeText(LoginActivity.this, "登陆失败",
							Toast.LENGTH_SHORT).show();
				}
			} catch (Exception e) {
				handler.sendEmptyMessage(2);
				Log.i("error", e.toString());
			}
		}
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
