package com.fuwu.mobileim.activity;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.EditText;
import android.widget.ImageButton;
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
import com.google.protobuf.ByteString;

public class MyInformationActivity extends Activity implements OnTouchListener {
	byte[] buf = null;
	private ProgressDialog prodialog;
	private ImageButton my_info_back;// 返回按钮
	private ImageButton my_info_confirm;// 保存按钮
	private ProfilePojo profilePojo;
	private CircularImage myinfo_userface;
	private EditText myinfo_nickname;
	private RelativeLayout fuzhi_layout;
	private RelativeLayout myinfo_userface_layout;
	private TextView myinfo_certification, myinfo_mobile, myinfo_email,
			myinfo_birthday, myinfo_sex, myinfo_fuzhi;
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
						b.compress(Bitmap.CompressFormat.PNG, 90, stream);
						byte[] buf2 = stream1.toByteArray(); // 将图片流以字符串形式存储下来
						stream.write(buf2);
						stream.close();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
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
				Toast.makeText(getApplicationContext(), "修改失败",
						Toast.LENGTH_SHORT).show();
				break;

			case 7:
				prodialog.dismiss();
				Toast.makeText(getApplicationContext(), "网络错误",
						Toast.LENGTH_SHORT).show();
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
		my_info_confirm = (ImageButton) findViewById(R.id.my_info_confirm);
		my_info_confirm.setOnTouchListener(this);
		my_info_confirm.setOnClickListener(listener2);// 给保存按钮设置监听
//		Intent intent = getIntent();
//		int dataNumber = intent.getIntExtra("dataNumber", -1);
//		if (dataNumber == 1) {
//			profilePojo = getProfilePojo();// 获得全局变量中的个人信息
//			init();
//		}
		if (fxApplication.getUser_exit()) {
			profilePojo = getProfilePojo();// 获得全局变量中的个人信息
			init();
		}else {
			if (FuXunTools.isConnect(this)) {
				prodialog =new ProgressDialog(MyInformationActivity.this);
				prodialog.setMessage("正在加载数据，请稍后...");
				prodialog.setCanceledOnTouchOutside(false);
				prodialog.show();
				Thread thread = new Thread(new getProfile());
				thread.start();
			} else {
				Toast.makeText(MyInformationActivity.this, R.string.no_internet,
						Toast.LENGTH_SHORT).show();
			}
		}


	}

	/**
	 * 获得相关组件 并设置数据
	 */
	private void init() {
		myinfo_userface_layout = (RelativeLayout) findViewById(R.id.myinfo_userface_layout);
		fuzhi_layout = (RelativeLayout) findViewById(R.id.fuzhi_layout);
		myinfo_userface = (CircularImage) findViewById(R.id.myinfo_userface);// 头像
		myinfo_nickname = (EditText) findViewById(R.id.myinfo_nickname);// 昵称
		myinfo_certification = (TextView) findViewById(R.id.myinfo_certification); // 行业认证
		myinfo_mobile = (TextView) findViewById(R.id.myinfo_mobile); // 手机
		myinfo_email = (TextView) findViewById(R.id.myinfo_email); // 邮箱
		myinfo_birthday = (TextView) findViewById(R.id.myinfo_birthday); // 生日
		myinfo_sex = (TextView) findViewById(R.id.myinfo_sex); // 性别
		myinfo_fuzhi = (TextView) findViewById(R.id.myinfo_fuzhi); // 福值
		myinfo_userface.setOnClickListener(listener3);
		myinfo_userface_layout.setOnClickListener(listener);
		// 设置头像
		String face_str = profilePojo.getTileUrl();
		Log.i("linshi1", "修改前----" + face_str);
		if (face_str != null && face_str.length() > 4) {
			File f = new File(Urlinterface.head_pic, profilePojo.getUserId()
					+ "");
			if (f.exists()) {
				Log.i("linshi------------", "加载本地图片");
				myinfo_userface.setImageDrawable(new BitmapDrawable(
						BitmapFactory.decodeFile(Urlinterface.head_pic
								+ profilePojo.getUserId())));
			} else {
				FuXunTools.set_bk(profilePojo.getUserId(), face_str,
						myinfo_userface);
			}
		} else {
			myinfo_userface.setImageResource(R.drawable.moren);
		}
		// 设置昵称
		myinfo_nickname.setText(profilePojo.getNickName());

		// 设置福值
		if (profilePojo.getIsProvider()) {
			myinfo_fuzhi.setText(profilePojo.getFuZhi());
		} else {
			fuzhi_layout.setVisibility(View.GONE);
		}

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
		// 设置性别
		myinfo_sex.setText("");
		int sex = profilePojo.getGender();
		if (sex == 0) {// 男
			myinfo_sex.setText("男");
		} else if (sex == 1) {// 女
			myinfo_sex.setText("女");
		} else if (sex == 2) {
			myinfo_sex.setText("保密");
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

	private View.OnClickListener listener2 = new View.OnClickListener() {
		@Override
		public void onClick(View v) {

			String nickname_str = myinfo_nickname.getText().toString();

			String kongge = nickname_str.replaceAll(" ", "");
			if (nickname_str.length() == 0 || kongge.equals("")) {
				Toast.makeText(getApplicationContext(), R.string.edit_null,
						Toast.LENGTH_SHORT).show();
			} else if (profilePojo.getNickName().equals(nickname_str)
					&& buf == null) {
				Toast.makeText(getApplicationContext(), R.string.no_change,
						Toast.LENGTH_SHORT).show();

			} else {

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

			}
		}
	};

	/**
	 * 
	 * 修改个人详细信息
	 */

	class modifyProfile implements Runnable {
		public void run() {
			try {

				String nickname_str = myinfo_nickname.getText().toString();
				int user_id = preferences.getInt("user_id", -1);
				String Token = preferences.getString("Token", "");

				ChangeProfileRequest.Builder builder = ChangeProfileRequest
						.newBuilder();
				builder.setUserId(user_id);
				builder.setToken(Token);
				if (!profilePojo.getNickName().equals(nickname_str)) {
					builder.setNickName(nickname_str);
				}
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
						profilePojo.setNickName(nickname_str);
						putProfile(profilePojo);
						handler.sendEmptyMessage(0);
					} else {
						handler.sendEmptyMessage(1);
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

		@Override
		public void onClick(View v) {
			Intent intent = new Intent();
			intent.putExtra("image_path",
					Urlinterface.head_pic + preferences.getInt("user_id", -1));
			intent.setClass(MyInformationActivity.this,
					ComtactZoomImageActivity.class);
			startActivity(intent);
		}
	};

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {

		switch (resultCode) {
		case -11:

			Bundle bundle = data.getExtras();
			uri = bundle.getString("uri");
			buf = bundle.getByteArray("buf");
			Log.i("linshi", buf.length + "--size");
			Bitmap b = BitmapFactory.decodeByteArray(buf, 0, buf.length);
			myinfo_userface.setImageDrawable(new BitmapDrawable(b));

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
				"profile_isAuthentication", false);//
		String fuzhi = preferences.getString("profile_fuZhi", "");// 生日
		profilePojo = new ProfilePojo(profile_userid, name, nickName, gender,
				tileUrl, isProvider, lisence, mobile, email, birthday,
				isAuthentication, fuzhi);
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
		editor.putString("profile_user", pro.getUserId()+"");//  用于判断本地是否有当前用户的信息
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
						profilePojo = new ProfilePojo(userId, name, nickName,
								gender, tileUrl, isProvider, lisence, mobile,
								email, birthday, isAuthentication, fuzhi);
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
				}else {
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

}
