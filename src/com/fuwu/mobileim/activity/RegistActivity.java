package com.fuwu.mobileim.activity;

import java.util.Timer;
import java.util.TimerTask;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
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
import android.widget.Toast;

import com.fuwu.mobileim.R;
import com.fuwu.mobileim.model.Models.RegisterRequest;
import com.fuwu.mobileim.model.Models.RegisterResponse;
import com.fuwu.mobileim.model.Models.ValidateCodeRequest;
import com.fuwu.mobileim.model.Models.ValidateCodeRequest.ValidateType;
import com.fuwu.mobileim.model.Models.ValidateCodeResponse;
import com.fuwu.mobileim.util.FuXunTools;
import com.fuwu.mobileim.util.FxApplication;
import com.fuwu.mobileim.util.HttpUtil;
import com.fuwu.mobileim.util.Urlinterface;
import com.google.protobuf.InvalidProtocolBufferException;

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
	private CheckBox agreement;
	private Button over;
	public Intent intent = new Intent();
	public RelativeLayout validate_time;
	private Timer timer;
	public int time = 180;
	private boolean validate_boolean = false;
	private String error_code;
	private FxApplication fx;
	@SuppressLint("HandlerLeak")
	private Handler handler = new Handler() {
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			switch (msg.what) {
			case 0:
				Toast.makeText(RegistActivity.this, "短信已发出请注意查看",
						Toast.LENGTH_SHORT).show();
				break;
			case 1:
				intent.setClass(RegistActivity.this, FragmengtActivity.class);
				startActivity(intent);
				RegistActivity.this.finish();
				break;
			case 2:
				if (!error_code.equals("")) {
					String errorString = fx.error_map.get(error_code);
					if (errorString == null) {
						Toast.makeText(RegistActivity.this, "注册失败",
								Toast.LENGTH_SHORT).show();
					} else {
						Toast.makeText(RegistActivity.this, errorString,
								Toast.LENGTH_SHORT).show();
					}
				}
				break;
			case 3:
				Toast.makeText(RegistActivity.this, "短信发送失败,请重试",
						Toast.LENGTH_SHORT).show();
				break;
			case 4:
				Toast.makeText(RegistActivity.this, "请求超时", Toast.LENGTH_SHORT)
						.show();
				break;
			}
		}
	};

	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.regist);
		fx = (FxApplication) getApplication();
		initialize();
	}

	// 初始化
	public void initialize() {
		timer = new Timer();
		findViewById(R.id.exit).setOnClickListener(this);
		name_text = (EditText) findViewById(R.id.name);
		pwd_text = (EditText) findViewById(R.id.pwd);
		pwds_text = (EditText) findViewById(R.id.pwds);
		phone_text = (EditText) findViewById(R.id.phone);
		yz_text = (EditText) findViewById(R.id.yz);
		name_text.setOnFocusChangeListener(this);
		pwd_text.setOnFocusChangeListener(this);
		pwds_text.setOnFocusChangeListener(this);

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
		if (yz_text.getText().toString().equals("")) {
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

	class Regist_Post implements Runnable {
		public void run() {
			try {
				RegisterRequest.Builder builder = RegisterRequest.newBuilder();
				builder.setMobilePhoneNumber(phone_text.getText().toString());
//				builder.setName(name_text.getText().toString());
				builder.setPassword(pwd_text.getText().toString());
				builder.setPasswordConfirm(pwds_text.getText().toString());
				builder.setValidateCode(yz_text.getText().toString());
				RegisterRequest request = builder.build();
				RegisterResponse response = RegisterResponse.parseFrom(HttpUtil
						.sendHttps(request.toByteArray(), Urlinterface.REGIST,
								"POST"));
				Log.i("Max", phone_text.getText().toString() + "/"
						+ name_text.getText().toString() + "/"
						+ pwd_text.getText().toString() + "/"
						+ yz_text.getText().toString());
				if (response.getIsSucceed()) {
					handler.sendEmptyMessage(1);
				} else {
					error_code = response.getErrorCode().toString();
					handler.sendEmptyMessage(2);
				}
			} catch (Exception e) {
				handler.sendEmptyMessage(4);
				Log.i("error", e.toString());
			}
		}
	}

	// 短信验证
	class ValidateCode_Post implements Runnable {
		public void run() {
			try {
				Log.i("Max", "--" + phone_text.getText().toString());
				ValidateCodeRequest.Builder builder = ValidateCodeRequest
						.newBuilder();
				builder.setPhoneNumber(phone_text.getText().toString());
				builder.setType(ValidateType.Register);
				ValidateCodeRequest request = builder.build();
				ValidateCodeResponse response = ValidateCodeResponse
						.parseFrom(HttpUtil.sendHttps(request.toByteArray(),
								Urlinterface.ValidateCode, "POST"));
				Log.i("Max",
						response.getIsSucceed() + "??"
								+ response.getErrorCode());
				if (response.getIsSucceed()) {
					validate_boolean = false;
					if (time != 180) {
						time = 180;
					} else {
						timer.schedule(timerTask, 1000, 1000);
					}
					handler.sendEmptyMessage(0);
				} else {
					validate_boolean = true;
					handler.sendEmptyMessage(3);
				}
			} catch (Exception e) {
				handler.sendEmptyMessage(4);
				Log.i("error", e.toString());
			}
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
			new Thread(new Regist_Post()).start();
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
				timer.cancel();
				view.setBackgroundColor(getResources().getColor(R.color.white));
			}
			regist_btnOver();
			break;
		case R.id.yz_send:
			if (phone_btn) {
				Toast.makeText(RegistActivity.this, "请先填写手机号码",
						Toast.LENGTH_SHORT).show();
			} else {
				if (validate_boolean) {
					new Thread(new ValidateCode_Post()).start();
				} else {
					Toast.makeText(RegistActivity.this, "请等180秒后再次发送验证码",
							Toast.LENGTH_SHORT).show();
				}
			}
			break;
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

	// EditText失去焦点时的处理
	public void onFocusChange(View arg0, boolean arg1) {
		if (!arg1) {
			switch (arg0.getId()) {
			case R.id.name:
				String name = name_text.getText().toString();
				if (name.equals("") || name.length() > 30) {
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
