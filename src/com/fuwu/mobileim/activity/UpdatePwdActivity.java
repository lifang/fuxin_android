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
import com.fuwu.mobileim.model.Models.ChangePasswordRequest;
import com.fuwu.mobileim.model.Models.ChangePasswordResponse;
import com.fuwu.mobileim.model.Models.ValidateCodeRequest;
import com.fuwu.mobileim.model.Models.ValidateCodeResponse;
import com.fuwu.mobileim.util.FuXunTools;
import com.fuwu.mobileim.util.HttpUtil;
import com.fuwu.mobileim.util.Urlinterface;
import com.google.protobuf.InvalidProtocolBufferException;

/**
 * 作者: 张秀楠 时间：2014-5-27 下午3:23:31
 */
public class UpdatePwdActivity extends Activity implements OnClickListener,
		OnFocusChangeListener {

	private EditText old_pwd;
	private EditText new_pwd;
	private EditText new_pwds;
	private EditText yzm;
	private EditText yz_text;
	private EditText phone_text;
	public TextView phone_tag;
	public TextView yz_tag;
	public Button phone_ok;
	public Button yz_send;
	public boolean phone_btn = true;
	private RelativeLayout view;
	private Button over;
	public Intent intent = new Intent();
	public RelativeLayout validate_time;
	private Timer timer;
	public int time = 180;
	private boolean validate_boolean = false;
	private TextView old_pwd_tag;
	private TextView new_pwd_tag;
	private TextView new_pwds_tag;
	@SuppressLint("HandlerLeak")
	private Handler handler = new Handler() {
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			switch (msg.what) {
			case 0:
				// yz_text.setText(yznumber);
				break;
			case 1:
				Toast.makeText(UpdatePwdActivity.this, "修改密码成功",
						Toast.LENGTH_SHORT).show();
				UpdatePwdActivity.this.finish();
				break;
			case 2:
				Toast.makeText(UpdatePwdActivity.this, "获取短信验证码失败!",
						Toast.LENGTH_SHORT).show();
				break;
			case 3:
				Toast.makeText(UpdatePwdActivity.this, "修改密码失败!",
						Toast.LENGTH_SHORT).show();
				break;
			}
		}
	};

	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.update_pwd);
		initialize();
	}

	// 初始化
	public void initialize() {
		timer = new Timer();
		findViewById(R.id.exit).setOnClickListener(this);
		old_pwd = (EditText) findViewById(R.id.old_pwd);
		new_pwd = (EditText) findViewById(R.id.new_pwd);
		new_pwds = (EditText) findViewById(R.id.new_pwds);
		phone_text = (EditText) findViewById(R.id.phone);
		yz_text = (EditText) findViewById(R.id.yz);
		old_pwd.setOnFocusChangeListener(this);
		new_pwd.setOnFocusChangeListener(this);
		new_pwds.setOnFocusChangeListener(this);

		validate_time = (RelativeLayout) findViewById(R.id.validate_time);
		old_pwd_tag = (TextView) findViewById(R.id.old_pwd_tag);
		new_pwd_tag = (TextView) findViewById(R.id.new_pwd_tag);
		new_pwds_tag = (TextView) findViewById(R.id.new_pwds_tag);
		phone_tag = (TextView) findViewById(R.id.phone_tag);
		yz_tag = (TextView) findViewById(R.id.yz_tag);
		view = (RelativeLayout) findViewById(R.id.view);
		phone_ok = (Button) findViewById(R.id.phone_ok);
		phone_ok.setOnClickListener(this);
		yz_send = (Button) findViewById(R.id.yz_send);
		yz_send.setOnClickListener(this);
		over = (Button) findViewById(R.id.update_over);
		over.setOnClickListener(this);
		over.setClickable(false);
	}

	// 短信验证
	class ValidateCode_Post implements Runnable {
		public void run() {
			try {
				Log.i("Max", "--" + phone_text.getText().toString());
				ValidateCodeRequest.Builder builder = ValidateCodeRequest
						.newBuilder();
				builder.setPhoneNumber(phone_text.getText().toString());
				ValidateCodeRequest request = builder.build();
				byte[] httpReturn = HttpUtil.sendHttps(request.toByteArray(),
						Urlinterface.ValidateCode, "POST");

				ValidateCodeResponse response = ValidateCodeResponse
						.parseFrom(httpReturn);
				if (response.getIsSucceed()) {
					validate_boolean = false;

					if (time != 180) {
						time = 180;
					} else {
						timer.schedule(timerTask, 1000, 1000);
					}
					handler.sendEmptyMessage(0);
				} else {
					handler.sendEmptyMessage(2);
				}

			} catch (InvalidProtocolBufferException e) {
				e.printStackTrace();
			}
		}
	}

	// 修改密码
	class UpdatePwd_Post implements Runnable {
		public void run() {
			try {
				ChangePasswordRequest.Builder builder = ChangePasswordRequest
						.newBuilder();
				builder.setToken("MockToken");
				builder.setUserId(1);
				builder.setOriginalPassword(old_pwd.getText().toString());
				builder.setPassword(new_pwd.getText().toString());
				builder.setPasswordConfirm(new_pwds.getText().toString());

				ChangePasswordRequest request = builder.build();

				ChangePasswordResponse response = ChangePasswordResponse
						.parseFrom(HttpUtil.sendHttps(request.toByteArray(),
								Urlinterface.PASSWORD, "PUT"));
				if (response.getIsSucceed()) {
					handler.sendEmptyMessage(1);
				} else {
					handler.sendEmptyMessage(3);
				}
			} catch (InvalidProtocolBufferException e) {
				e.printStackTrace();
			}
		}
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

	public boolean judge() {// 判断注册是否完成
		if (old_pwd.getText().toString().equals("")) {
			return false;
		}
		if (new_pwd.getText().toString().equals("")) {
			return false;
		}
		if (!new_pwd.getText().toString().equals(new_pwds.getText().toString())) {
			return false;
		}
		if (phone_btn) {
			return false;
		}
		if (yz_text.getText().toString().equals("")) {
			return false;
		}
		return true;
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

	public void onClick(View arg0) {
		switch (arg0.getId()) {
		case R.id.exit:
			this.finish();
			break;
		case R.id.update_over:
			Log.i("Max", judge() + "");
			new Thread(new UpdatePwd_Post()).start();
			break;
		case R.id.phone_ok:
			Log.i("aa", "111");
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
				Toast.makeText(UpdatePwdActivity.this, "111",
						Toast.LENGTH_SHORT).show();
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
				Toast.makeText(UpdatePwdActivity.this, "请先填写手机号码",
						Toast.LENGTH_SHORT).show();
			} else {
				if (validate_boolean) {
					new Thread(new ValidateCode_Post()).start();
				} else {
					Toast.makeText(UpdatePwdActivity.this, "请等180秒后再次发送验证码",
							Toast.LENGTH_SHORT).show();
				}
			}
			break;
		}
	}

	public void onFocusChange(View arg0, boolean arg1) {
		if (!arg1) {
			switch (arg0.getId()) {
			case R.id.old_pwd:
				String name = old_pwd.getText().toString();
				if (name.equals("") || name.length() > 8) {
					old_pwd_tag.setVisibility(View.VISIBLE);
				} else {
					old_pwd_tag.setVisibility(View.GONE);
				}
				break;
			case R.id.new_pwd:
				String pwd = new_pwd.getText().toString();
				if (pwd.equals("")) {
					new_pwd_tag.setVisibility(View.VISIBLE);
				} else {
					new_pwd_tag.setVisibility(View.GONE);
				}
				break;
			case R.id.new_pwds:
				Log.i("Max", new_pwds.getText().toString() + "---"
						+ new_pwds.getText().toString());
				if (new_pwd.getText().toString()
						.equals(new_pwds.getText().toString())) {
					new_pwds_tag.setVisibility(View.GONE);
				} else {
					new_pwds_tag.setVisibility(View.VISIBLE);
				}
				break;
			}
			regist_btnOver();
		}
	}
}
