package com.fuwu.mobileim.activity;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.fuwu.mobileim.R;
import com.fuwu.mobileim.model.Models.ChangeProfileRequest;
import com.fuwu.mobileim.model.Models.ChangeProfileResponse;
import com.fuwu.mobileim.model.Models.ProfileRequest;
import com.fuwu.mobileim.model.Models.ProfileResponse;
import com.fuwu.mobileim.pojo.ProfilePojo;
import com.fuwu.mobileim.util.FuXunTools;
import com.fuwu.mobileim.util.FxApplication;
import com.fuwu.mobileim.util.HttpUtil;
import com.fuwu.mobileim.util.ImageCacheUtil;
import com.fuwu.mobileim.util.Urlinterface;
import com.fuwu.mobileim.view.CircularImage;
import com.fuwu.mobileim.view.MyDialog;
import com.google.protobuf.ByteString;

public class MyInformationActivity extends Activity implements OnTouchListener {
	byte[] buf = null;
	private ProgressDialog prodialog;
	private ImageButton my_info_back;// 返回按钮
	private ProfilePojo profilePojo;
	private ImageView myinfo_userface, myinfo_modifynickname;
	private TextView myinfo_certification, myinfo_mobile, myinfo_email,
			myinfo_birthday, myinfo_fuzhi, myinfo_location, myinfo_sign,
			myinfo_nickname, myinfo_name;
	String uri;
	Bitmap bm = null;
	private Handler handler = new Handler() {
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case 0:
				prodialog.dismiss();
				ImageCacheUtil.IMAGE_CACHE.clear();
				Toast.makeText(getApplicationContext(), "修改成功",
						Toast.LENGTH_SHORT).show();
				if (buf != null) {
					File file = new File(Urlinterface.head_pic,
							profilePojo.getUserId() + "");
					if (file.exists()) {
						file.delete();
					}
					if (!file.getParentFile().exists()) {
						file.getParentFile().mkdirs();
					}
					try {
						file.createNewFile();
						FileOutputStream stream = new FileOutputStream(file);
						ByteArrayOutputStream stream1 = new ByteArrayOutputStream();
						Bitmap b = BitmapFactory.decodeByteArray(buf, 0,
								buf.length);
						myinfo_userface.setImageDrawable(new BitmapDrawable(
								FuXunTools.createRoundConerImage(b)));
						b.compress(Bitmap.CompressFormat.PNG, 90, stream);
						byte[] buf2 = stream1.toByteArray(); // 将图片流以字符串形式存储下来
						stream.write(buf2);
						stream.close();
					} catch (IOException e) {
					}
				}
				buf = null;
				break;
			case 1:
				prodialog.dismiss();
				Toast.makeText(getApplicationContext(), "修改失败",
						Toast.LENGTH_SHORT).show();
				break;
			case 2:
				prodialog.dismiss();
				fxApplication.setUser_exit(true);
				putProfile(profilePojo);
				init();
				break;
			case 6:
				prodialog.dismiss();
				Toast.makeText(getApplicationContext(), "请求失败",
						Toast.LENGTH_SHORT).show();
				break;

			case 7:
				prodialog.dismiss();
				Toast.makeText(getApplicationContext(), "网络错误",
						Toast.LENGTH_SHORT).show();
				break;
			case 9:
				prodialog.dismiss();
				new Handler().postDelayed(new Runnable() {
					public void run() {
						Intent intent = new Intent(MyInformationActivity.this,
								LoginActivity.class);
						startActivity(intent);
						clearActivity();
					}
				}, 3500);
				Toast.makeText(getApplicationContext(), "您的账号已在其他手机登陆",
						Toast.LENGTH_LONG).show();
				break;
			}
		}
	};
	SharedPreferences preferences;
	private FxApplication fxApplication;

	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.my_information);
		findViewById(R.id.my_info_back).setOnTouchListener(this);
		findViewById(R.id.my_info_back).setOnClickListener(listener1);// 给返回按钮设置监听
		preferences = getSharedPreferences(Urlinterface.SHARED,
				Context.MODE_PRIVATE);
		fxApplication = (FxApplication) getApplication();
		fxApplication.getActivityList().add(this);
		if (fxApplication.getUser_exit()) {
			profilePojo = getProfilePojo();// 获得本地中的个人信息
			init();
		} else {
			if (FuXunTools.isConnect(this)) {
				prodialog = new ProgressDialog(MyInformationActivity.this);
				prodialog.setMessage("正在加载数据，请稍后...");
				prodialog.setCanceledOnTouchOutside(false);
				prodialog.show();
				Thread thread = new Thread(new getProfile());
				thread.start();
			} else {
				Toast.makeText(MyInformationActivity.this,
						R.string.no_internet, Toast.LENGTH_SHORT).show();
			}
		}

	}

	/**
	 * 获得相关组件 并设置数据
	 */
	private void init() {
		myinfo_modifynickname = (ImageView) findViewById(R.id.myinfo_modifynickname);// 画笔
		myinfo_userface = (ImageView) findViewById(R.id.myinfo_userface);// 头像
		myinfo_name = (TextView) findViewById(R.id.myinfo_name);// 名称
		myinfo_nickname = (TextView) findViewById(R.id.myinfo_nickname);// 昵称
		myinfo_certification = (TextView) findViewById(R.id.myinfo_certification); // 行业认证
		myinfo_mobile = (TextView) findViewById(R.id.myinfo_mobile); // 手机
		myinfo_email = (TextView) findViewById(R.id.myinfo_email); // 邮箱
		myinfo_birthday = (TextView) findViewById(R.id.myinfo_birthday); // 生日
		myinfo_fuzhi = (TextView) findViewById(R.id.myinfo_fuzhi); // 福值
		myinfo_sign = (TextView) findViewById(R.id.myinfo_sign); // 个人简介
		myinfo_location = (TextView) findViewById(R.id.myinfo_location); // 所在地
		myinfo_userface.setOnClickListener(listener);
		myinfo_modifynickname.setOnClickListener(listener3);
		// 设置头像
		String face_str = profilePojo.getTileUrl();
		Log.i("linshi1", "修改前----" + face_str);
		if (face_str != null && face_str.length() > 4) {
			File f = new File(Urlinterface.head_pic, profilePojo.getUserId()
					+ "");
			if (f.exists()) {
				Log.i("linshi------------", "加载本地图片");
				myinfo_userface.setImageDrawable(new BitmapDrawable(FuXunTools
						.createRoundConerImage(BitmapFactory
								.decodeFile(Urlinterface.head_pic
										+ profilePojo.getUserId()))));
			} else {
				FuXunTools.set_bk_createRoundConerImage(
						profilePojo.getUserId(), face_str, myinfo_userface);
			}
		} else {
			Drawable drawable = getResources().getDrawable(R.drawable.moren);
			BitmapDrawable bitmapDrawable = (BitmapDrawable) drawable;
			Bitmap bitmap = bitmapDrawable.getBitmap();
			myinfo_userface.setImageDrawable(new BitmapDrawable(FuXunTools
					.createRoundConerImage(bitmap)));
		}
		// 设置名称
		String nameStr = profilePojo.getName();
		if (nameStr != null && nameStr.length() > 0 && !nameStr.equals("null")) {
			myinfo_name.setText(nameStr);
		} else {
			myinfo_name.setText(profilePojo.getNickName());
		}

		// 昵称
		myinfo_nickname.setText(profilePojo.getNickName());
		// 认证

		// 设置实名认证
		Boolean str1_ = profilePojo.getIsAuthentication();
		// 设置邮箱认证
		String str2_ = profilePojo.getEmail();
		// 设置手机验证
		String str3_ = profilePojo.getMobile();
		if (str1_) {// 设置实名认证
			findViewById(R.id.info_certification_name).setBackgroundResource(
					R.drawable.certification11);
		} else {
			findViewById(R.id.info_certification_name).setBackgroundResource(
					R.drawable.certification1);
		}
		if (str2_ != null && !("").equals(str2_)) {// 设置邮箱认证
			findViewById(R.id.info_certification_email).setBackgroundResource(
					R.drawable.certification21);
		} else {
			findViewById(R.id.info_certification_email).setBackgroundResource(
					R.drawable.certification2);
		}
		if (str3_ != null && !("").equals(str3_)) {// 设置手机验证
			findViewById(R.id.info_certification_mobile).setBackgroundResource(
					R.drawable.certification31);
		} else {
			findViewById(R.id.info_certification_mobile).setBackgroundResource(
					R.drawable.certification3);
		}
		// 设置福值
		String fuzhiStr = profilePojo.getFuZhi();
		if ("".equals(fuzhiStr)) {
			fuzhiStr = "0";
		}
		fuzhiStr = fuzhiStr + "/5.0";
		int index = fuzhiStr.indexOf("/");
		SpannableStringBuilder mSpannableStringBuilder = new SpannableStringBuilder(
				fuzhiStr);
		mSpannableStringBuilder.setSpan(new ForegroundColorSpan(Color.RED), 0,
				index, Spannable.SPAN_EXCLUSIVE_INCLUSIVE);
		ForegroundColorSpan span_1 = new ForegroundColorSpan(Color.argb(255,
				153, 153, 153));
		mSpannableStringBuilder.setSpan(span_1, index, fuzhiStr.length(),
				Spannable.SPAN_EXCLUSIVE_INCLUSIVE);
		myinfo_fuzhi.setText(mSpannableStringBuilder);
		// 设置认证行业
		String str1 = profilePojo.getLisence();
		myinfo_certification.setText(str1);

		// 手机
		String str3 = profilePojo.getMobile();
		myinfo_mobile.setText(str3);
		// 邮箱
		String str2 = profilePojo.getEmail();
		myinfo_email.setText(str2);
		// 生日
		myinfo_birthday.setText(profilePojo.getBirthday());
		//
		// 设置个人简介
		myinfo_sign.setText(profilePojo.getDescription());
		// 设置所在地
		myinfo_location.setText(profilePojo.getLocation());
		if (profilePojo.getIsProvider()) {
			findViewById(R.id.myinfo_hangye_layout).setVisibility(View.VISIBLE);
			findViewById(R.id.myinfo_fuzhi_layout).setVisibility(View.VISIBLE);
			findViewById(R.id.myinfo_gerenjianjie_layout).setVisibility(View.VISIBLE);
		}
	}

	private View.OnClickListener listener1 = new View.OnClickListener() {
		@Override
		public void onClick(View v) {
			Intent intent2 = new Intent();
			MyInformationActivity.this.setResult(-11, intent2);
			MyInformationActivity.this.finish();
		}
	};

	/**
	 * 
	 * 修改个人详细信息
	 */

	class modifyProfile implements Runnable {
		public void run() {
			try {

				int user_id = preferences.getInt("user_id", -1);
				String Token = preferences.getString("Token", "");

				ChangeProfileRequest.Builder builder = ChangeProfileRequest
						.newBuilder();
				builder.setUserId(user_id);
				builder.setToken(Token);
				if (buf != null) {
					builder.setContentType("jpg");
					builder.setTiles(ByteString.copyFrom(buf));

				}
				ChangeProfileRequest response = builder.build();

				byte[] by = HttpUtil.sendHttps(response.toByteArray(),
						Urlinterface.ChangeProfile, "PUT");
				if (by != null && by.length > 0) {

					ChangeProfileResponse res = ChangeProfileResponse
							.parseFrom(by);
					if (res.getIsSucceed()) {
						Log.i("linshi1", "修改后---"
								+ res.getProfile().getTileUrl());
						profilePojo.setTileUrl(res.getProfile().getTileUrl());
						putProfile(profilePojo);
						handler.sendEmptyMessage(0);
					} else {
						int ErrorCode = res.getErrorCode().getNumber();
						if (ErrorCode == 2001) {
							handler.sendEmptyMessage(9);
						} else {
							handler.sendEmptyMessage(1);
						}
					}
				} else {
					handler.sendEmptyMessage(6);
				}
			} catch (Exception e) {
				handler.sendEmptyMessage(7);
			}
		}
	}

	private View.OnClickListener listener = new View.OnClickListener() {

		@Override
		public void onClick(View v) {

			Intent intentp = new Intent();
			intentp.setClass(MyInformationActivity.this, SettingPhoto.class);//
			startActivityForResult(intentp, 0);
		}
	};

	private View.OnClickListener listener3 = new View.OnClickListener() {
		// 修改昵称
		@Override
		public void onClick(View v) {
			Intent intent = new Intent();
			intent.putExtra("nickName", profilePojo.getNickName());
			intent.setClass(MyInformationActivity.this,
					ModifyNickNameActivity.class);
			startActivityForResult(intent, 0);
		}
	};

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		Bundle bundle;
		switch (resultCode) {
		case -11:

			bundle = data.getExtras();
			uri = bundle.getString("uri");
			buf = bundle.getByteArray("buf");
			Log.i("linshi", buf.length + "--size");
			// Bitmap b = BitmapFactory.decodeByteArray(buf, 0, buf.length);
			// myinfo_userface.setImageDrawable(new BitmapDrawable(FuXunTools
			// .createRoundConerImage(b)));
			if (FuXunTools.isConnect(MyInformationActivity.this)) {
				prodialog = new ProgressDialog(MyInformationActivity.this);
				prodialog.setMessage("正在修改...");
				prodialog.setCanceledOnTouchOutside(false);
				prodialog.show();
				Thread thread = new Thread(new modifyProfile());
				thread.start();
			} else {
				Toast.makeText(MyInformationActivity.this,
						R.string.no_internet, Toast.LENGTH_SHORT).show();
			}

			break;
		case -12:
			bundle = data.getExtras();
			String nickName = bundle.getString("nickName");
			if (nickName != null && !nickName.equals(profilePojo.getNickName())) {
				profilePojo.setNickName(nickName);
				putProfile(profilePojo);
				init();
			}
			break;
		default:
			break;

		}
		super.onActivityResult(requestCode, resultCode, data);

	}

	/**
	 * 获得本地存储的 个人信息
	 */
	private ProfilePojo getProfilePojo() {

		SharedPreferences preferences = getSharedPreferences(
				Urlinterface.SHARED, Context.MODE_PRIVATE);

		int profile_userid = preferences.getInt("profile_userid", -1);
		String name = preferences.getString("profile_name", "");// 名称
		String nickName = preferences.getString("profile_nickName", "");// 昵称
		int gender = preferences.getInt("profile_gender", -1);// 性别
		String tileUrl = preferences.getString("profile_tileUrl", "");// 头像
		Boolean isProvider = preferences
				.getBoolean("profile_isProvider", false);//
		String lisence = preferences.getString("profile_lisence", "");// 行业认证
		String mobile = preferences.getString("profile_mobile", "");// 手机号码
		String email = preferences.getString("profile_email", "");// 邮箱
		String birthday = preferences.getString("profile_birthday", "");// 生日
		Boolean isAuthentication = preferences.getBoolean(
				"profile_isAuthentication", false);// 实名认证
		String fuzhi = preferences.getString("profile_fuZhi", "");// 福指
		String location = preferences.getString("profile_location", "");// 所在地
		String description = preferences.getString("profile_description", "");// 福师简介
		profilePojo = new ProfilePojo(profile_userid, name, nickName, gender,
				tileUrl, isProvider, lisence, mobile, email, birthday,
				isAuthentication, fuzhi, location, description);
		return profilePojo;
	}

	private void putProfile(ProfilePojo pro) {
		SharedPreferences preferences = getSharedPreferences(
				Urlinterface.SHARED, Context.MODE_PRIVATE);
		Editor editor = preferences.edit();
		editor.putInt("profile_userid", pro.getUserId());
		editor.putString("profile_name", pro.getName());
		editor.putString("profile_nickName", pro.getNickName());
		editor.putInt("profile_gender", pro.getGender());
		editor.putString("profile_tileUrl", pro.getTileUrl());
		editor.putBoolean("profile_isProvider", pro.getIsProvider());
		editor.putString("profile_lisence", pro.getLisence());
		editor.putString("profile_mobile", pro.getMobile());
		editor.putString("profile_email", pro.getEmail());
		editor.putString("profile_birthday", pro.getBirthday());
		editor.putBoolean("profile_isAuthentication", pro.getIsAuthentication());
		editor.putString("profile_fuZhi", pro.getFuZhi());
		editor.putString("profile_location", pro.getLocation());
		editor.putString("profile_description", pro.getDescription());
		editor.commit();

	}

	/**
	 * 
	 * 获得个人详细信息
	 * 
	 * 
	 */

	class getProfile implements Runnable {
		public void run() {
			try {
				int user_id = preferences.getInt("user_id", -1);
				String Token = preferences.getString("Token", "");
				ProfileRequest.Builder builder = ProfileRequest.newBuilder();
				builder.setUserId(user_id);
				builder.setToken(Token);
				ProfileRequest response = builder.build();

				byte[] by = HttpUtil.sendHttps(response.toByteArray(),
						Urlinterface.PROFILE, "POST");
				if (by != null && by.length > 0) {

					ProfileResponse res = ProfileResponse.parseFrom(by);
					if (res.getIsSucceed()) {
						int userId = res.getProfile().getUserId();// 用户id
						String name = res.getProfile().getName();// 名称
						String nickName = res.getProfile().getNickName();// 昵称
						int gender = res.getProfile().getGender().getNumber();// 性别
						String tileUrl = res.getProfile().getTileUrl();// 头像
						Boolean isProvider = res.getProfile().getIsProvider();//
						String lisence = res.getProfile().getLisence();// 行业认证
						String mobile = res.getProfile().getMobilePhoneNum();// 手机号码
						String email = res.getProfile().getEmail();// 邮箱
						String birthday = res.getProfile().getBirthday();// 生日
						Boolean isAuthentication = res.getProfile()
								.getIsAuthentication();// 实名认证
						String fuzhi = res.getProfile().getFuzhi();// 福值
						String location = res.getProfile().getLocation();// 所在地
						String description = res.getProfile().getDescription();// 福师简介

						profilePojo = new ProfilePojo(userId, name, nickName,
								gender, tileUrl, isProvider, lisence, mobile,
								email, birthday, isAuthentication, fuzhi,
								location, description);
						Log.i("linshi", "  --nickName" + nickName
								+ "  --gender" + gender + "  --tileUrl"
								+ tileUrl + "  --lisence" + lisence
								+ "  --mobile" + mobile + "  --email" + email
								+ "  birthday--" + birthday);
						Log.i("linshi------------",
								"profileprofileprofileprofile网络shuju");
						Message msg = new Message();// 创建Message 对象
						msg.what = 2;
						handler.sendMessage(msg);
					} else {
						handler.sendEmptyMessage(6);
					}
				} else {
					handler.sendEmptyMessage(6);
				}

			} catch (Exception e) {
				// prodialog.dismiss();
				Log.i("error", e.toString());
				handler.sendEmptyMessage(7);
			}
		}
	}

	@Override
	public boolean onTouch(View v, MotionEvent event) {

		if (event.getAction() == MotionEvent.ACTION_DOWN) {
			switch (v.getId()) {
			case R.id.my_info_back:
				Log.i("linshi", "onTouchonTouchonTouchonTouch--my_info_back");
				findViewById(R.id.my_info_back).getBackground().setAlpha(70);
				break;
			}
		} else if (event.getAction() == MotionEvent.ACTION_UP) {
			switch (v.getId()) {
			case R.id.my_info_back:
				findViewById(R.id.my_info_back).getBackground().setAlpha(255);
				break;
			}
		}

		return false;
	}

	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			// spf.edit().putString("Token", "null").commit();
			Dialog dialog = new AlertDialog.Builder(MyInformationActivity.this)
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

	// 关闭界面
	public void clearActivity() {
		List<Activity> activityList = fxApplication.getActivityList();
		for (int i = 0; i < activityList.size(); i++) {
			activityList.get(i).finish();
		}
		fxApplication.setActivityList();
	}

}
