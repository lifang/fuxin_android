package com.fuwu.mobileim.activity;

import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.View.OnTouchListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.fuwu.mobileim.R;
import com.fuwu.mobileim.activity.MyInformationActivity.modifyProfile;
import com.fuwu.mobileim.model.Models.ChangeContactDetailRequest;
import com.fuwu.mobileim.model.Models.ChangeContactDetailResponse;
import com.fuwu.mobileim.model.Models.Contact;
import com.fuwu.mobileim.util.DBManager;
import com.fuwu.mobileim.util.FuXunTools;
import com.fuwu.mobileim.util.FxApplication;
import com.fuwu.mobileim.util.HttpUtil;
import com.fuwu.mobileim.util.Urlinterface;

public class OpenInputMethod extends Activity implements OnTouchListener {
	// private MyDialog dialog;
	private EditText edittext;
	private ImageButton my_info_back;// 返回按钮
	private ImageButton my_info_confirm;// 保存按钮
	SharedPreferences preferences;
	private FxApplication fxApplication;
	private ProgressDialog pd;
	private DBManager db;
	private int user_id;
	private int contact_id;
	private String token;
	private String customName;
	private ImageView input_empty;// 输入框 清空图标
	private Handler handler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);

			switch (msg.what) {

			case 3:
				pd.dismiss();
				InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
				boolean isOpen = imm.isActive();
				if (isOpen) {
					imm.hideSoftInputFromWindow(OpenInputMethod.this
							.getCurrentFocus().getWindowToken(),
							InputMethodManager.HIDE_NOT_ALWAYS);
				}
				String str = edittext.getText().toString();
				customName = str;
				db.updateContactRem(user_id, contact_id, str);
				Toast.makeText(getApplicationContext(), "修改备注成功!", 0).show();
				break;
			case 4:
				pd.dismiss();
				Toast.makeText(getApplicationContext(), "修改备注失败!", 0).show();
				break;
			case 7:
				Toast.makeText(getApplicationContext(), R.string.no_internet,
						Toast.LENGTH_SHORT).show();
				break;
			case 12:
				pd.dismiss();
				new Handler().postDelayed(new Runnable() {
					public void run() {
						Intent intent = new Intent(OpenInputMethod.this,
								LoginActivity.class);
						startActivity(intent);
						clearActivity();
					}
				}, 3500);
				preferences
						.edit()
						.putInt("exit_user_id",
								preferences.getInt("user_id", 0)).commit();
				preferences
						.edit()
						.putString("exit_Token",
								preferences.getString("Token", "null"))
						.commit();
				preferences
						.edit()
						.putString("exit_clientid",
								preferences.getString("clientid", "")).commit();
				preferences.edit().putInt("user_id", 0).commit();
				preferences.edit().putString("Token", "null").commit();
				preferences.edit().putString("pwd", "").commit();
				preferences.edit().putString("clientid", "").commit();
				preferences.edit().putString("profile_user", "").commit();
				fxApplication.initData();
				Toast.makeText(getApplicationContext(), "您的账号已在其他手机登陆",
						Toast.LENGTH_LONG).show();
				break;
			}
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);// 去掉标题栏
		setContentView(R.layout.inputmethod);
		db = new DBManager(this);
		preferences = getSharedPreferences(Urlinterface.SHARED,
				Context.MODE_PRIVATE);
		user_id = preferences.getInt("user_id", 1);
		contact_id = preferences.getInt("contact_id", 1);
		token = preferences.getString("Token", "token");
		customName = getIntent().getStringExtra("customName");
		fxApplication = (FxApplication) getApplication();
		fxApplication.getActivityList().add(this);
		edittext = (EditText) findViewById(R.id.edittext);
		edittext.setText(customName);
		findViewById(R.id.my_info_back).setOnTouchListener(this);
		findViewById(R.id.my_info_back).setOnClickListener(listener1);// 给返回按钮设置监听
		my_info_confirm = (ImageButton) findViewById(R.id.my_info_confirm);
		my_info_confirm.setOnTouchListener(this);
		my_info_confirm.setOnClickListener(listener);// 给保存按钮设置监听
		input_empty = (ImageView) findViewById(R.id.input_empty);
		input_empty.setOnClickListener(listener2);// 给清空按钮设置监听
	}

	/*
	 * 清空搜索框
	 */
	private View.OnClickListener listener2 = new View.OnClickListener() {
		public void onClick(View v) {
			edittext.setText("");
		}
	};
	private View.OnClickListener listener = new View.OnClickListener() {

		@Override
		public void onClick(View v) {
			String reply_edit = edittext.getText().toString();

			String kongge = reply_edit.replaceAll(" ", "");
			if (reply_edit.length() == 0 || kongge.equals("")) {
				Toast.makeText(getApplicationContext(), "内容不能为空",
						Toast.LENGTH_SHORT).show();
			} else if (reply_edit.length() > 32) {
				Toast.makeText(getApplicationContext(), "备注过长，请重新输入",
						Toast.LENGTH_SHORT).show();
			} else if (reply_edit.equals(customName)) {
				Toast.makeText(getApplicationContext(), "备注没有变化",
						Toast.LENGTH_SHORT).show();
			} else {
				// Intent intent2 = new Intent();
				// intent2.putExtra("content", reply_edit);
				// // 通过调用setResult方法返回结果给前一个activity。
				// OpenInputMethod.this.setResult(5, intent2);
				// // 关闭当前activity
				// OpenInputMethod.this.finish();
				if (FuXunTools.isConnect(OpenInputMethod.this)) {
					pd = new ProgressDialog(OpenInputMethod.this);
					pd.setMessage("正在发送请求...");
					pd.setCanceledOnTouchOutside(false);
					pd.show();
					new UpdateContactRem().start();
				} else {
					handler.sendEmptyMessage(7);
				}
			}
		}
	};

	class UpdateContactRem extends Thread {
		public void run() {
			try {
				ChangeContactDetailRequest.Builder builder = ChangeContactDetailRequest
						.newBuilder();
				builder.setUserId(user_id);
				builder.setToken(token);
				Contact.Builder cb = Contact.newBuilder();
				cb.setContactId(contact_id);
				String str = edittext.getText().toString();
				cb.setCustomName(str);
				builder.setContact(cb);
				ChangeContactDetailRequest response = builder.build();
				byte[] by = HttpUtil.sendHttps(response.toByteArray(),
						Urlinterface.ContactDetail, "PUT");
				if (by != null && by.length > 0) {
					ChangeContactDetailResponse res = ChangeContactDetailResponse
							.parseFrom(by);
					if (res.getIsSucceed()) {
						handler.sendEmptyMessage(3);
					} else {
						int ErrorCode = res.getErrorCode().getNumber();
						if (ErrorCode == 2001) {
							handler.sendEmptyMessage(12);
						} else {
							handler.sendEmptyMessage(4);
						}
					}
				} else {
					handler.sendEmptyMessage(4);
				}
			} catch (Exception e) {
			}
		}
	}

	@Override
	public boolean onTouch(View v, MotionEvent event) {
		// TODO Auto-generated method stub
		if (event.getAction() == MotionEvent.ACTION_DOWN) {
			switch (v.getId()) {
			case R.id.my_info_back:
				Log.i("linshi", "onTouchonTouchonTouchonTouch--my_info_back");
				findViewById(R.id.my_info_back).getBackground().setAlpha(70);
				break;
			case R.id.my_info_confirm:
				findViewById(R.id.my_info_confirm).getBackground().setAlpha(70);
				break;
			}
		} else if (event.getAction() == MotionEvent.ACTION_UP) {
			switch (v.getId()) {
			case R.id.my_info_back:
				findViewById(R.id.my_info_back).getBackground().setAlpha(255);
				break;
			case R.id.my_info_confirm:
				findViewById(R.id.my_info_confirm).getBackground()
						.setAlpha(255);
				break;
			}
		}

		return false;
	}

	private View.OnClickListener listener1 = new View.OnClickListener() {
		@Override
		public void onClick(View v) {
			Intent intent2 = new Intent();
			intent2.putExtra("customName", customName);
			OpenInputMethod.this.setResult(-11, intent2);
			OpenInputMethod.this.finish();
		}
	};

	// @Override
	// public boolean onTouchEvent(MotionEvent event) {
	// finish();
	// return true;
	// }
	// 关闭界面
	public void clearActivity() {
		List<Activity> activityList = fxApplication.getActivityList();
		for (int i = 0; i < activityList.size(); i++) {
			activityList.get(i).finish();
		}
		fxApplication.setActivityList();
	}

	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			// spf.edit().putString("Token", "null").commit();
			Dialog dialog = new AlertDialog.Builder(OpenInputMethod.this)
					.setTitle("提示")
					.setMessage("您确认要退出应用么?")
					.setPositiveButton("确认",
							new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog,
										int which) {
									clearActivity();
								}
							})
					.setNegativeButton("取消",
							new DialogInterface.OnClickListener() {

								@Override
								public void onClick(DialogInterface dialog,
										int which) {
									dialog.dismiss();
								}
							}).create();
			dialog.show();

			return true;
		}
		return super.onKeyDown(keyCode, event);
	}
}
