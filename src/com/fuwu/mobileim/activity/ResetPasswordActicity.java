package com.fuwu.mobileim.activity;

import java.util.List;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.telephony.SmsMessage;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.View.OnTouchListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.mobstat.StatService;
import com.fuwu.mobileim.R;
import com.fuwu.mobileim.model.Models.ResetPasswordRequest;
import com.fuwu.mobileim.model.Models.ResetPasswordResponse;
import com.fuwu.mobileim.model.Models.ValidateCodeRequest;
import com.fuwu.mobileim.model.Models.ValidateCodeRequest.ValidateType;
import com.fuwu.mobileim.model.Models.ValidateCodeResponse;
import com.fuwu.mobileim.util.FuXunTools;
import com.fuwu.mobileim.util.FxApplication;
import com.fuwu.mobileim.util.HttpUtil;
import com.fuwu.mobileim.util.Urlinterface;
import com.fuwu.mobileim.view.MyDialog;
import com.google.protobuf.InvalidProtocolBufferException;

public class ResetPasswordActicity extends Activity implements OnClickListener,
		OnFocusChangeListener, OnTouchListener {
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
	private Button over;
	public Intent intent = new Intent();
	private RelativeLayout validate_time;
	private FxApplication fx;
	@SuppressLint("HandlerLeak")
	private String error_code;
	private ScrollView scrol;
	private ProgressDialog prodialog;
	private SharedPreferences spf;
	private IntentFilter filter = null;
	public static final String ACTION = "android.provider.Telephony.SMS_RECEIVED";
	private Handler handler = new Handler() {
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			switch (msg.what) {
			case 0:
				Toast.makeText(ResetPasswordActicity.this, "短信已发出请注意查看",
						Toast.LENGTH_SHORT).show();
				break;
			case 1:
				prodialog.dismiss();
				showLoginDialog();
				break;
			case 3:
				if (!error_code.equals("")) {
					String errorString = fx.ValidateCode.get(error_code);
					if (errorString == null) {
						Toast.makeText(ResetPasswordActicity.this,
								"短信发送失败,请重试", Toast.LENGTH_SHORT).show();
					} else if (error_code.equals("ExistingUserYes")) {
						Toast.makeText(ResetPasswordActicity.this, "手机已被注册",
								Toast.LENGTH_SHORT).show();
					} else {
						Toast.makeText(ResetPasswordActicity.this, errorString,
								Toast.LENGTH_SHORT).show();
					}
				}
				break;
			case 4:
				prodialog.dismiss();
				if (!error_code.equals("")) {
					String errorString = fx.error_map.get(error_code);
					if (errorString == null) {
						Toast.makeText(ResetPasswordActicity.this, "找回失败",
								Toast.LENGTH_SHORT).show();
					}
					if (error_code.equals("InvalidPassword")) {
						Toast.makeText(ResetPasswordActicity.this, "密码不可少于6位",
								Toast.LENGTH_SHORT).show();
					} else {
						Toast.makeText(ResetPasswordActicity.this, errorString,
								Toast.LENGTH_SHORT).show();
					}
				}
				break;
			case 5:
				scrol.scrollTo(0, 500);
				break;
			case 9:
				prodialog.dismiss();
				new Handler().postDelayed(new Runnable() {
					public void run() {
						Intent intent = new Intent(ResetPasswordActicity.this,
								LoginActivity.class);
						startActivity(intent);
						clearActivity();
					}
				}, 3500);
				spf.edit().putInt("exit_user_id", spf.getInt("user_id", 0))
						.commit();
				spf.edit()
						.putString("exit_Token", spf.getString("Token", "null"))
						.commit();
				spf.edit()
						.putString("exit_clientid",
								spf.getString("clientid", "")).commit();
				spf.edit().putInt("user_id", 0).commit();
				spf.edit().putString("Token", "null").commit();
				spf.edit().putString("pwd", "").commit();
				spf.edit().putString("clientid", "").commit();
				spf.edit().putString("profile_user", "").commit();
				fx.initData();
				Toast.makeText(getApplicationContext(), "您的账号已在其他手机登陆",
						Toast.LENGTH_LONG).show();
				break;
			}
		}
	};

	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.resetpassword);
		fx = (FxApplication) getApplication();
		fx.getActivityList().add(this);
		spf = getSharedPreferences(Urlinterface.SHARED, 0);
		initialize();
	}

	// 初始化
	public void initialize() {
		findViewById(R.id.exit).setOnClickListener(this);
		findViewById(R.id.exit).setOnTouchListener(this);
		pwd_text = (EditText) findViewById(R.id.pwd);
		pwds_text = (EditText) findViewById(R.id.pwds);
		phone_text = (EditText) findViewById(R.id.phone);
		phone_text.setText(spf.getString("phone", ""));
		yz_text = (EditText) findViewById(R.id.yz);
		pwd_text.setOnFocusChangeListener(this);
		pwds_text.setOnFocusChangeListener(this);
		yz_text.setOnFocusChangeListener(this);
		scrol = (ScrollView) findViewById(R.id.scrol);
		validate_time = (RelativeLayout) findViewById(R.id.validate_time);
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
		// if (yz_text.getText().toString().equals("")) {
		// return false;
		// }
		return true;
	}

	// 短信验证
	class ValidateCode_Post implements Runnable {
		public void run() {
			try {
				handler.sendEmptyMessage(0);
				ValidateCodeRequest.Builder builder = ValidateCodeRequest
						.newBuilder();
				builder.setPhoneNumber(phone_text.getText().toString());
				builder.setType(ValidateType.ResetPassword);
				ValidateCodeRequest request = builder.build();
				ValidateCodeResponse response = ValidateCodeResponse
						.parseFrom(HttpUtil.sendHttps(request.toByteArray(),
								Urlinterface.ValidateCode, "POST"));
				if (!response.getIsSucceed()) {
					int ErrorCode = response.getErrorCode().getNumber();
					if (ErrorCode == 2001) {
						handler.sendEmptyMessage(9);
					} else {
						error_code = response.getErrorCode().toString();
						handler.sendEmptyMessage(3);
					}
				}
			} catch (InvalidProtocolBufferException e) {
				e.printStackTrace();
			}
		}
	}

	// 找回密码
	class Backpwd_Post implements Runnable {
		public void run() {
			try {
				ResetPasswordRequest.Builder builder = ResetPasswordRequest
						.newBuilder();
				builder.setPhoneNumber(phone_text.getText().toString());
				builder.setValidateCode(yz_text.getText().toString());
				builder.setPassword(pwd_text.getText().toString());
				builder.setPasswordConfirm(pwds_text.getText().toString());
				ResetPasswordRequest request = builder.build();
				ResetPasswordResponse response = ResetPasswordResponse
						.parseFrom(HttpUtil.sendHttps(request.toByteArray(),
								Urlinterface.RESETPASSWORD, "PUT"));
				if (response.getIsSucceed()) {
					handler.sendEmptyMessage(1);
				} else {
					error_code = response.getErrorCode().toString();
					handler.sendEmptyMessage(4);
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

	public void onClick(View v) {
		Intent intent = new Intent();
		switch (v.getId()) {
		case R.id.exit:
			intent.setClass(ResetPasswordActicity.this, LoginActivity.class);
			startActivity(intent);
			this.finish();
			break;
		case R.id.backpwd_over:
			Log.i("Max", judge() + "");
			if (FuXunTools.isConnect(this)) {
				prodialog = new ProgressDialog(ResetPasswordActicity.this);
				prodialog.setMessage("努力连接中..");
				prodialog.setCanceledOnTouchOutside(false);
				prodialog.show();
				new Thread(new Backpwd_Post()).start();
			} else {
				Toast.makeText(ResetPasswordActicity.this,
						R.string.no_internet, Toast.LENGTH_SHORT).show();
			}
			break;
		case R.id.phone_ok:
			if (phone_btn) {
				String phone = phone_text.getText().toString();
				if (phone.equals("") || !FuXunTools.isMobileNO(phone)) {
					phone_tag.setVisibility(View.VISIBLE);
				} else {
					phone_tag.setVisibility(View.GONE);
					phone_ok.setText("重填");
					phone_btn = false;
					phone_text.setEnabled(false);
					view.setBackgroundColor(getResources().getColor(
							R.color.regist_bg));
					yz_send.setBackgroundResource(R.drawable.touming_btn);
					yz_send.setTextColor(getResources().getColor(
							R.color.system_textColor));
				}
			} else {
				phone_btn = true;
				phone_text.setEnabled(true);
				phone_text.setText("");
				phone_ok.setText("确定");
				phone_text.requestFocus();// 获取焦点
				view.setBackgroundColor(getResources().getColor(R.color.white));
				yz_send.setBackgroundResource(R.drawable.touming_btn2);
				yz_send.setTextColor(getResources().getColor(R.color.qianhui));
			}
			regist_btnOver();
			break;
		case R.id.yz_send:
			if (FuXunTools.isConnect(this)) {
				if (!phone_btn) {
					if (filter == null) {
						filter = new IntentFilter();
					}
					filter.addAction(ACTION);
					filter.setPriority(Integer.MAX_VALUE);
					registerReceiver(myReceiver, filter);
					new Thread(new ValidateCode_Post()).start();
					validate_time.setVisibility(View.VISIBLE);
				}
			} else {
				Toast.makeText(ResetPasswordActicity.this,
						R.string.no_internet, Toast.LENGTH_SHORT).show();
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
		} else {
			if (v.getId() == R.id.yz) {
				handler.sendEmptyMessage(5);
			}
		}
	}

	private void showLoginDialog() {
		View view = getLayoutInflater().inflate(R.layout.talk_builder, null);
		final TextView btnYes = (TextView) view.findViewById(R.id.name);
		btnYes.setText("找回密码成功");
		final TextView del = (TextView) view.findViewById(R.id.del_talk);
		del.setText("确定");
		RelativeLayout layout = (RelativeLayout) view
				.findViewById(R.id.nick_layout);
		layout.setVisibility(View.GONE);
		// 设置对话框显示的View
		// 点击确定是的监听
		final MyDialog builder = new MyDialog(ResetPasswordActicity.this, 0,
				view, R.style.mydialog);
		del.setOnClickListener(new View.OnClickListener() {
			public void onClick(View arg0) {
				builder.dismiss();
				intent.setClass(ResetPasswordActicity.this, LoginActivity.class);
				startActivity(intent);
				ResetPasswordActicity.this.finish();
			}
		});
		builder.show();
	}

	// 短信拦截
	private BroadcastReceiver myReceiver = new BroadcastReceiver() {
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			Log.i("Max", "2222");
			if (action.equals("android.provider.Telephony.SMS_RECEIVED")) {
				Bundle bundle = intent.getExtras();
				if (bundle != null) {
					Object[] object = (Object[]) bundle.get("pdus");
					SmsMessage[] messages = new SmsMessage[object.length];
					for (int i = 0; i < object.length; i++) {
						messages[i] = SmsMessage
								.createFromPdu((byte[]) object[i]);
					}
					SmsMessage message = messages[0];
					String phone = message.getDisplayOriginatingAddress();
					if (phone.equals("1069022905555")) {
						String content = message.getMessageBody();
						String code = content.substring(17, 23);
						Log.i("Max", code);
						if (!code.equals("")) {
							yz_text.setText(code);
						}
						unregisterReceiver(myReceiver);
					}
				}
			}
		}
	};

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

	@Override
	public boolean onTouch(View v, MotionEvent event) {
		if (event.getAction() == MotionEvent.ACTION_DOWN) {
			switch (v.getId()) {
			case R.id.exit:
				findViewById(R.id.exit).getBackground().setAlpha(70);
				break;
			}
		} else if (event.getAction() == MotionEvent.ACTION_UP) {
			switch (v.getId()) {
			case R.id.exit:
				findViewById(R.id.exit).getBackground().setAlpha(255);
				break;
			}
		}
		return false;
	}

	// 关闭界面
	public void clearActivity() {
		List<Activity> activityList = fx.getActivityList();
		for (int i = 0; i < activityList.size(); i++) {
			activityList.get(i).finish();
		}
		fx.setActivityList();
	}
}
