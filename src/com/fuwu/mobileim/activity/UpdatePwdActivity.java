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
import com.fuwu.mobileim.model.Models.ChangePasswordRequest;
import com.fuwu.mobileim.model.Models.ChangePasswordResponse;
import com.fuwu.mobileim.model.Models.ValidateCodeRequest;
import com.fuwu.mobileim.model.Models.ValidateCodeRequest.ValidateType;
import com.fuwu.mobileim.model.Models.ValidateCodeResponse;
import com.fuwu.mobileim.util.FuXunTools;
import com.fuwu.mobileim.util.FxApplication;
import com.fuwu.mobileim.util.HttpUtil;
import com.fuwu.mobileim.util.Urlinterface;
import com.fuwu.mobileim.view.MyDialog;

/**
 * 作者: 张秀楠 时间：2014-5-27 下午3:23:31
 */
public class UpdatePwdActivity extends Activity implements OnClickListener,
		OnFocusChangeListener, OnTouchListener {

	private EditText old_pwd;
	private EditText new_pwd;
	private EditText new_pwds;
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
	private TextView old_pwd_tag;
	private TextView new_pwd_tag;
	private TextView new_pwds_tag;
	private FxApplication fx;
	private String error_code;
	private ProgressDialog prodialog;
	private ScrollView scrol;
	private SharedPreferences spf;
	private IntentFilter filter = null;
	public static final String ACTION = "android.provider.Telephony.SMS_RECEIVED";
	@SuppressLint("HandlerLeak")
	private Handler handler = new Handler() {
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			switch (msg.what) {
			case 0:
				Toast.makeText(UpdatePwdActivity.this, "短信已发出请注意查看",
						Toast.LENGTH_SHORT).show();
				break;
			case 1:
				prodialog.dismiss();
				showLoginDialog();
				break;
			case 2:
				if (!error_code.equals("")) {
					String errorString = fx.ValidateCode.get(error_code);
					if (errorString == null) {
						Toast.makeText(UpdatePwdActivity.this, "短信发送失败,请重试",
								Toast.LENGTH_SHORT).show();
					} else if (error_code.equals("ExistingUserYes")) {
						Toast.makeText(UpdatePwdActivity.this, "手机已被注册",
								Toast.LENGTH_SHORT).show();
					} else {
						Toast.makeText(UpdatePwdActivity.this, errorString,
								Toast.LENGTH_SHORT).show();
					}
				}
				break;
			case 3:
				prodialog.dismiss();
				if (!error_code.equals("")) {
					String errorString = fx.error_map.get(error_code);
					if (errorString == null) {
						Toast.makeText(UpdatePwdActivity.this, "找回失败",
								Toast.LENGTH_SHORT).show();
					}
					if (error_code.equals("InvalidPassword")) {
						Toast.makeText(UpdatePwdActivity.this, "新密码不可少于6位",
								Toast.LENGTH_SHORT).show();
					} else {
						Toast.makeText(UpdatePwdActivity.this, errorString,
								Toast.LENGTH_SHORT).show();
					}
				}
				break;
			case 4:
				prodialog.dismiss();
				Toast.makeText(UpdatePwdActivity.this, "请求超时",
						Toast.LENGTH_SHORT).show();
				break;
			case 5:
				scrol.scrollTo(0, 500);
				break;
			case 9:
				prodialog.dismiss();
				new Handler().postDelayed(new Runnable() {
					public void run() {
						Intent intent = new Intent(UpdatePwdActivity.this,
								LoginActivity.class);
						startActivity(intent);
						clearActivity();
					}
				}, 3500);
				FuXunTools.initdate(spf, fx);
				Toast.makeText(getApplicationContext(), "您的账号已在其他手机登陆",
						Toast.LENGTH_LONG).show();
				break;
			}
		}
	};

	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.update_pwd);
		fx = (FxApplication) getApplication();
		fx.getActivityList().add(this);
		spf = getSharedPreferences(Urlinterface.SHARED, 0);
		initialize();
	}

	// 初始化
	public void initialize() {
		findViewById(R.id.update_pwd_exit).setOnClickListener(this);
		findViewById(R.id.update_pwd_exit).setOnTouchListener(this);
		old_pwd = (EditText) findViewById(R.id.old_pwd);
		new_pwd = (EditText) findViewById(R.id.new_pwd);
		new_pwds = (EditText) findViewById(R.id.new_pwds);
		phone_text = (EditText) findViewById(R.id.phone);
		phone_text.setText(spf.getString("phone", ""));
		yz_text = (EditText) findViewById(R.id.yz);
		old_pwd.setOnFocusChangeListener(this);
		new_pwd.setOnFocusChangeListener(this);
		new_pwds.setOnFocusChangeListener(this);
		yz_text.setOnFocusChangeListener(this);
		scrol = (ScrollView) findViewById(R.id.scrol);
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
				handler.sendEmptyMessage(0);
				ValidateCodeRequest.Builder builder = ValidateCodeRequest
						.newBuilder();
				builder.setPhoneNumber(phone_text.getText().toString());
				builder.setType(ValidateType.ChangePassword);
				ValidateCodeRequest request = builder.build();
				byte[] httpReturn = HttpUtil.sendHttps(request.toByteArray(),
						Urlinterface.ValidateCode, "POST");

				ValidateCodeResponse response = ValidateCodeResponse
						.parseFrom(httpReturn);
				if (!response.getIsSucceed()) {
					int ErrorCode = response.getErrorCode().getNumber();
					if (ErrorCode == 2001) {
						handler.sendEmptyMessage(9);
					} else {
						error_code = response.getErrorCode().toString();
						handler.sendEmptyMessage(2);
					}
				}

			} catch (Exception e) {
				handler.sendEmptyMessage(4);
				Log.i("error", e.toString());
			}
		}
	}

	// 修改密码
	class UpdatePwd_Post implements Runnable {
		public void run() {
			try {
				ChangePasswordRequest.Builder builder = ChangePasswordRequest
						.newBuilder();
				builder.setToken(spf.getString("Token", ""));
				builder.setUserId(spf.getInt("user_id", 0));
				builder.setOriginalPassword(old_pwd.getText().toString());
				builder.setPassword(new_pwd.getText().toString());
				builder.setPasswordConfirm(new_pwds.getText().toString());
				builder.setValidateCode(yz_text.getText().toString());
				ChangePasswordRequest request = builder.build();

				ChangePasswordResponse response = ChangePasswordResponse
						.parseFrom(HttpUtil.sendHttps(request.toByteArray(),
								Urlinterface.PASSWORD, "PUT"));
				if (response.getIsSucceed()) {
					handler.sendEmptyMessage(1);
				} else {
					int ErrorCode = response.getErrorCode().getNumber();
					if (ErrorCode == 2001) {
						handler.sendEmptyMessage(9);
					} else {
						error_code = response.getErrorCode().toString();
						handler.sendEmptyMessage(1);
					}
				}
			} catch (Exception e) {
				handler.sendEmptyMessage(4);
				Log.i("error", e.toString());
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
		return true;
	}

	public void onClick(View arg0) {
		switch (arg0.getId()) {
		case R.id.update_pwd_exit:
			this.finish();
			break;
		case R.id.update_over:
			if (FuXunTools.isConnect(this)) {
				prodialog = new ProgressDialog(UpdatePwdActivity.this);
				prodialog.setMessage("努力连接中..");
				prodialog.setCanceledOnTouchOutside(false);
				prodialog.show();
				new Thread(new UpdatePwd_Post()).start();
			} else {
				Toast.makeText(UpdatePwdActivity.this, R.string.no_internet,
						Toast.LENGTH_SHORT).show();
			}
			break;
		case R.id.phone_ok:
			Log.i("aa", "111");
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
				Toast.makeText(UpdatePwdActivity.this, R.string.no_internet,
						Toast.LENGTH_SHORT).show();
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
		} else {
			if (arg0.getId() == R.id.yz) {
				handler.sendEmptyMessage(5);
			}
		}
	}

	private void showLoginDialog() {
		View view = getLayoutInflater().inflate(R.layout.talk_builder, null);
		final TextView btnYes = (TextView) view.findViewById(R.id.name);
		btnYes.setText("修改密码成功");
		final TextView del = (TextView) view.findViewById(R.id.del_talk);
		del.setText("确定");
		RelativeLayout layout = (RelativeLayout) view
				.findViewById(R.id.nick_layout);
		layout.setVisibility(View.GONE);
		// 设置对话框显示的View
		// 点击确定是的监听
		final MyDialog builder = new MyDialog(UpdatePwdActivity.this, 0, view,
				R.style.mydialog);
		del.setOnClickListener(new View.OnClickListener() {
			public void onClick(View arg0) {
				builder.dismiss();
				UpdatePwdActivity.this.finish();
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
						String code = content.substring(19, 25);
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
			case R.id.update_pwd_exit:
				Log.i("linshi",
						"onTouchonTouchonTouchonTouch--modify_nickname_back");
				findViewById(R.id.update_pwd_exit).getBackground().setAlpha(70);
				break;
			}
		} else if (event.getAction() == MotionEvent.ACTION_UP) {
			switch (v.getId()) {
			case R.id.update_pwd_exit:
				findViewById(R.id.update_pwd_exit).getBackground()
						.setAlpha(255);
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
