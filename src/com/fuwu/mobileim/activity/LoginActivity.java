package com.fuwu.mobileim.activity;

import java.io.File;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.baidu.mobstat.StatService;
import com.fuwu.mobileim.R;
import com.fuwu.mobileim.model.Models.AuthenticationRequest;
import com.fuwu.mobileim.model.Models.AuthenticationResponse;
import com.fuwu.mobileim.util.FuXunTools;
import com.fuwu.mobileim.util.FxApplication;
import com.fuwu.mobileim.util.HttpUtil;
import com.fuwu.mobileim.util.KeyboardLayout;
import com.fuwu.mobileim.util.KeyboardLayout.onKybdsChangeListener;
import com.fuwu.mobileim.util.Urlinterface;
import com.fuwu.mobileim.view.CircularImage;

/**
 * 作者: 张秀楠 时间：2014-5-23 下午4:34:03
 */
public class LoginActivity extends Activity implements OnClickListener,
		OnFocusChangeListener, Urlinterface {
	public EditText user_text;
	public EditText pwd_text;
	private String user;
	private String pwd;
	private FxApplication fx;
	private ProgressDialog prodialog;
	private String error_code;
	private SharedPreferences spf;
	private LinearLayout layout;
	private KeyboardLayout keyboardLayout1;
	@SuppressLint("HandlerLeak")
	private Handler handler = new Handler() {
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			Intent intent = new Intent();

			switch (msg.what) {
			case 0:
				prodialog.dismiss();
				intent.setClass(LoginActivity.this, FragmengtActivity.class);
				startActivity(intent);
				LoginActivity.this.finish();
				break;
			case 1:
				prodialog.dismiss();
				if (!error_code.equals("")) {
					String errorString = fx.error_map.get(error_code);
					if (errorString == null) {
						Toast.makeText(LoginActivity.this, "登录失败",
								Toast.LENGTH_SHORT).show();
					} else {
						Toast.makeText(LoginActivity.this, errorString,
								Toast.LENGTH_SHORT).show();
					}
				}
				break;
			case 2:
				prodialog.dismiss();
				Toast.makeText(LoginActivity.this, "网络连接异常", Toast.LENGTH_SHORT)
						.show();
				break;
			case 3:
				int item = 0;
				if (fx.getHeight() <= 1920 && fx.getHeight() >= 1750) {
					item = -400;
				} else if (fx.getHeight() < 1280 && fx.getHeight() >= 1170) {
					item = -290;
				} else if (fx.getHeight() == 1280) {
					item = -200;
				} else {
					switch (fx.getHeight()) {
					case 854:
						item = -250;
						break;
					case 800:
						item = -290;
						break;
					case 960:
						item = -290;
						break;
					default:
						item = -250;
						break;
					}
				}
				KeyboardLayout.LayoutParams params = new KeyboardLayout.LayoutParams(
						KeyboardLayout.LayoutParams.WRAP_CONTENT,
						KeyboardLayout.LayoutParams.WRAP_CONTENT);
				params.setMargins(0, item, 0, 0);
				layout.setLayoutParams(params);
				break;
			case 4:
				pwd_text.clearFocus();
				KeyboardLayout.LayoutParams params2 = new KeyboardLayout.LayoutParams(
						KeyboardLayout.LayoutParams.WRAP_CONTENT,
						KeyboardLayout.LayoutParams.WRAP_CONTENT);
				params2.setMargins(0, 0, 0, 0);
				layout.setLayoutParams(params2);
				break;
			}
		}
	};

	@SuppressWarnings("deprecation")
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.login);
		fx = (FxApplication) getApplication();
		spf = getSharedPreferences(Urlinterface.SHARED, 0);
		findViewById(R.id.regist).setOnClickListener(this);
		findViewById(R.id.forgetpwd).setOnClickListener(this);
		findViewById(R.id.login_btn).setOnClickListener(this);
		Display display = getWindowManager().getDefaultDisplay();
		int width = display.getWidth();
		int height = display.getHeight();
		Log.i("linshi", "getHeight().getWidth():" + height + "x" + width);
		// Toast.makeText(this, width + "/" + height,
		// Toast.LENGTH_SHORT).show();
		fx.setWidth(width);
		fx.setHeight(height);
		initialize();// 初始化
		// 百度统计
		StatService.setOn(this, StatService.EXCEPTION_LOG);
		int id = spf.getInt("user_id", 0);
		String tken = spf.getString("Token", "null");
		if (id != 0 || !tken.equals("null")) {
			Intent intent = new Intent();
			intent.setClass(LoginActivity.this, FragmengtActivity.class);
			startActivity(intent);
			LoginActivity.this.finish();
		}
	}

	// 初始化
	public void initialize() {
		user_text = (EditText) findViewById(R.id.user);
		pwd_text = (EditText) findViewById(R.id.pwd);
		layout = (LinearLayout) findViewById(R.id.layout);
		keyboardLayout1 = (KeyboardLayout) findViewById(R.id.keyboardLayout1);
		keyboardLayout1.setOnkbdStateListener(new onKybdsChangeListener() {
			public void onKeyBoardStateChange(int state) {
				switch (state) {
				case KeyboardLayout.KEYBOARD_STATE_HIDE:
					handler.sendEmptyMessage(4);
					break;
				case KeyboardLayout.KEYBOARD_STATE_SHOW:
					break;
				}
			}
		});
		pwd_text.setOnFocusChangeListener(this);
		user_text.setOnFocusChangeListener(this);
		CircularImage head = (CircularImage) findViewById(R.id.head);
		int uid = spf.getInt("user_id", 0);
		if (uid != 0) {
			File file = new File(Urlinterface.head_pic, uid + "");
			if (file.exists()) {
				@SuppressWarnings("deprecation")
				Drawable dra = new BitmapDrawable(
						BitmapFactory.decodeFile(Urlinterface.head_pic + uid));
				head.setImageDrawable(dra);
			}
		}
		String user = spf.getString("user", "null");
		// user_text.setText("MockUserName");
		// user_text.setText("15862373890");
		// user_text.setText("18913536561");
		// user_text.setText("15862373890");
		// user_text.setText("18711111120");
		// pwd_text.setText("111111");
		if (!user.equals("null")) {
			user_text.setText(user);
		}
		String pwd_str = spf.getString("pwd", "");
		if (!pwd_str.equals("")) {
			pwd_text.setText(pwd_str);
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
					prodialog.setMessage("努力登录中..");
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
						spf.edit().putString("Token", response.getToken())
								.commit();
						spf.edit().putString("pwd", pwd).commit();
						handler.sendEmptyMessage(0);
					} else {
						Log.i("Max", "errorCode:" + response.getErrorCode());
						error_code = response.getErrorCode().toString();
						handler.sendEmptyMessage(1);
					}
				} else {
					Toast.makeText(LoginActivity.this, "登录失败",
							Toast.LENGTH_SHORT).show();
				}
			} catch (Exception e) {
				handler.sendEmptyMessage(2);
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

	public void onFocusChange(View arg0, boolean arg1) {
		switch (arg0.getId()) {
		case R.id.user:
			if (arg1) {
				handler.sendEmptyMessage(3);
			}
			break;

		case R.id.pwd:
			if (arg1) {
				handler.sendEmptyMessage(3);
			}
			break;
		}

	}
}
