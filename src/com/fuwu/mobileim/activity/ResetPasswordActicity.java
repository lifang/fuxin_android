package com.fuwu.mobileim.activity;

import java.util.Timer;
import java.util.TimerTask;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.fuwu.mobileim.R;
import com.fuwu.mobileim.model.Models.ValidateCodeRequest;
import com.fuwu.mobileim.model.Models.ValidateCodeResponse;
import com.fuwu.mobileim.util.FuXunTools;
import com.fuwu.mobileim.util.FxApplication;
import com.fuwu.mobileim.util.HttpUtil;
import com.fuwu.mobileim.util.Urlinterface;
import com.google.protobuf.InvalidProtocolBufferException;

public class ResetPasswordActicity extends Activity implements OnClickListener,
		OnFocusChangeListener {
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
	private Button over;
	private boolean yz_boolean = false;
	public Intent intent = new Intent();
	private Timer timer;
	public int time = 180;
	private RelativeLayout validate_time;
	private boolean validate_boolean = false;
	private FxApplication fx;
	@SuppressLint("HandlerLeak")
	private Handler handler = new Handler() {
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			switch (msg.what) {
			case 0:
				yz_text.setText(yznumber);
				break;
			case 1:
				break;
			}
		}
	};

	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.resetpassword);
		fx = (FxApplication) getApplication();
		initialize();
	}

	// 初始化
	public void initialize() {
		timer = new Timer();
		findViewById(R.id.exit).setOnClickListener(this);
		pwd_text = (EditText) findViewById(R.id.pwd);
		pwds_text = (EditText) findViewById(R.id.pwds);
		phone_text = (EditText) findViewById(R.id.phone);
		yz_text = (EditText) findViewById(R.id.yz);
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
		validate_time = (RelativeLayout) findViewById(R.id.validate_time);
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
		over = (Button) findViewById(R.id.backpwd_over);
		over.setOnClickListener(this);
		over.setClickable(false);
	}

	public boolean judge() {// 判断注册是否完成
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
		return true;
	}

	// 短信验证
	class ValidateCode_Post implements Runnable {
		public void run() {
			try {
				ValidateCodeRequest.Builder builder = ValidateCodeRequest
						.newBuilder();
				builder.setPhoneNumber(phone_text.getText().toString());
				builder.setType("");
				ValidateCodeRequest request = builder.build();
				ValidateCodeResponse response = ValidateCodeResponse
						.parseFrom(HttpUtil.sendHttps(request.toByteArray(),
								Urlinterface.ValidateCode, "POST"));
				if (response.getIsSucceed()) {
					validate_boolean = false;

					if (time != 180) {
						time = 180;
					} else {
						timer.schedule(timerTask, 1000, 1000);
					}
					handler.sendEmptyMessage(0);
				} else {
					// Toast.makeText(RegistActivity.this,
					// "errorCode:" + response.getErrorCode(),
					// Toast.LENGTH_SHORT).show();
				}
			} catch (InvalidProtocolBufferException e) {
				e.printStackTrace();
			}
		}
	}

	// 找回密码
	class Backpwd_Post implements Runnable {
		public void run() {
			// try {
			// ResetPasswordRequest.Builder builder = ResetPasswordRequest
			// .newBuilder();
			// builder.setToken(fx.getToken());
			// ValidateCodeRequest request = builder.build();
			// ValidateCodeResponse response = ValidateCodeResponse
			// .parseFrom(HttpUtil.sendHttps(request.toByteArray(),
			// Urlinterface.RESETPASSWORD, "PUT"));
			// if (response.getIsSucceed()) {
			// yznumber = response.getValidateCode();
			// validate_boolean = false;
			//
			// if (time != 180) {
			// time = 180;
			// } else {
			// timer.schedule(timerTask, 1000, 1000);
			// }
			// Log.i("Max", "短信验证码:" + response.getValidateCode());
			// handler.sendEmptyMessage(0);
			// } else {
			// // Toast.makeText(RegistActivity.this,
			// // "errorCode:" + response.getErrorCode(),
			// // Toast.LENGTH_SHORT).show();
			// }
			// } catch (InvalidProtocolBufferException e) {
			// e.printStackTrace();
			// }
		}
	}

	// TimerTask是个抽象类,实现了Runnable接口，所以TimerTask就是一个子线程
	TimerTask timerTask = new TimerTask() {
		// 倒数10秒
		public void run() {
			// 定义一个消息传过去
			time--;
			if (time < 0) {
				validate_boolean = true;
				timer.cancel();
			}
		}
	};

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
			Intent intent = new Intent(ResetPasswordActicity.this,
					LoginActivity.class);
			startActivity(intent);
			this.finish();
			break;
		case R.id.backpwd_over:
			Log.i("Max", judge() + "");
			new Thread(new Backpwd_Post()).start();
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
						new Thread(new ValidateCode_Post()).start();
						validate_time.setVisibility(View.VISIBLE);
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
		case R.id.yz_send:
			if (phone_btn) {
				Toast.makeText(ResetPasswordActicity.this, "请先填写手机号码",
						Toast.LENGTH_SHORT).show();
			} else {
				if (validate_boolean) {
					new Thread(new ValidateCode_Post()).start();
				} else {
					Toast.makeText(ResetPasswordActicity.this,
							"请等180秒后再次发送验证码", Toast.LENGTH_SHORT).show();
				}
			}
			break;
		}
	}

	public void onFocusChange(View v, boolean hasFocus) {
		if (!hasFocus) {
			switch (v.getId()) {
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
}
