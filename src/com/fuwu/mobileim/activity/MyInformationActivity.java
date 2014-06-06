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
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.fuwu.mobileim.R;
import com.fuwu.mobileim.model.Models.ChangeProfileRequest;
import com.fuwu.mobileim.model.Models.ChangeProfileResponse;
import com.fuwu.mobileim.pojo.ProfilePojo;
import com.fuwu.mobileim.util.FuXunTools;
import com.fuwu.mobileim.util.FxApplication;
import com.fuwu.mobileim.util.HttpUtil;
import com.fuwu.mobileim.util.Urlinterface;
import com.fuwu.mobileim.view.CircularImage;
import com.google.protobuf.ByteString;

public class MyInformationActivity extends Activity {
	byte[] buf = null;
	private ProgressDialog prodialog;
	private ImageButton my_info_back;// 返回按钮
	private ImageButton my_info_confirm;// 保存按钮
	private FxApplication fxApplication;
	private ProfilePojo profilePojo;
	private CircularImage myinfo_userface;
	private EditText myinfo_nickname;
	private TextView myinfo_certification, myinfo_mobile, myinfo_email,
			myinfo_birthday, myinfo_sex;
	Bitmap bm=null;
	private Handler handler = new Handler() {
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case 0:
				prodialog.dismiss();
				Toast.makeText(getApplicationContext(), "修改成功",
						Toast.LENGTH_SHORT).show();
				if (buf != null) {
					File file = new File(Urlinterface.head_pic,
							profilePojo.getUserId() + "");
						if (file.exists()) {
							file.delete();
						}
						try {
							file.createNewFile();
							FileOutputStream stream = new FileOutputStream(file);
							ByteArrayOutputStream stream1 = new ByteArrayOutputStream();
							bm.compress(Bitmap.CompressFormat.JPEG, 100, stream);
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
			}
		}
	};

	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.my_information);
		fxApplication = (FxApplication) getApplication();
		my_info_back = (ImageButton) findViewById(R.id.my_info_back);
		my_info_back.setOnClickListener(listener1);// 给返回按钮设置监听
		my_info_confirm = (ImageButton) findViewById(R.id.my_info_confirm);
		my_info_confirm.setOnClickListener(listener2);// 给保存按钮设置监听

		// profilePojo = fxApplication.getProfilePojo();

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

		profilePojo = new ProfilePojo(profile_userid, name, nickName, gender,
				tileUrl, isProvider, lisence, mobile, email, birthday);

		init();

	}

	/**
	 * 
	 * 获得相关组件 并设置数据
	 * 
	 * 
	 */
	private void init() {
		myinfo_userface = (CircularImage) findViewById(R.id.myinfo_userface);
		myinfo_nickname = (EditText) findViewById(R.id.myinfo_nickname);
		myinfo_certification = (TextView) findViewById(R.id.myinfo_certification);
		myinfo_mobile = (TextView) findViewById(R.id.myinfo_mobile);
		myinfo_email = (TextView) findViewById(R.id.myinfo_email);
		myinfo_birthday = (TextView) findViewById(R.id.myinfo_birthday);
		myinfo_sex = (TextView) findViewById(R.id.myinfo_sex);
		myinfo_userface.setOnClickListener(listener);
		// 设置头像
		String face_str = profilePojo.getTileUrl();
		if (face_str != null) {

			if (face_str.length() > 4) {
				File f = new File(Urlinterface.head_pic,
						profilePojo.getUserId() + "");
				if (f.exists()) {
					Log.i("linshi------------", "加载本地图片");
					Drawable dra = new BitmapDrawable(
							BitmapFactory.decodeFile(Urlinterface.head_pic
									+ profilePojo.getUserId()));
					myinfo_userface.setImageDrawable(dra);
				} else {
					FuXunTools.set_bk(profilePojo.getUserId(), face_str,
							myinfo_userface);
				}
			} else {
				myinfo_userface.setImageResource(R.drawable.moren);
			}
			// 设置昵称
			myinfo_nickname.setText(profilePojo.getNickName());

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

	}

	private View.OnClickListener listener1 = new View.OnClickListener() {
		@Override
		public void onClick(View v) {
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
			} else {
				prodialog = new ProgressDialog(MyInformationActivity.this);
				prodialog.setMessage("正在修改...");
				prodialog.setCanceledOnTouchOutside(false);
				prodialog.show();
				Thread thread = new Thread(new modifyProfile());
				thread.start();

			}
		}
	};

	/**
	 * 
	 * 修改个人详细信息
	 * 
	 * 
	 */

	class modifyProfile implements Runnable {
		public void run() {
			try {

				String nickname_str = myinfo_nickname.getText().toString();

				ChangeProfileRequest.Builder builder = ChangeProfileRequest
						.newBuilder();
				builder.setUserId(fxApplication.getUser_id());
				builder.setToken(fxApplication.getToken());
				if (!profilePojo.getNickName().equals(nickname_str)) {

				}
				if (buf != null) {
					builder.setTiles(ByteString.copyFrom(buf));
				}
				ChangeProfileRequest response = builder.build();

				byte[] by = HttpUtil.sendHttps(response.toByteArray(),
						Urlinterface.ChangeProfile, "PUT");
				if (by != null && by.length > 0) {

					ChangeProfileResponse res = ChangeProfileResponse
							.parseFrom(by);
					if (res.getIsSucceed()) {
						handler.sendEmptyMessage(0);
					} else {
						handler.sendEmptyMessage(1);
					}
				} else {
					handler.sendEmptyMessage(6);
				}
				//
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

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {

		switch (resultCode) {
		case -11:

			Bundle bundle = data.getExtras();
			String uri = bundle.getString("uri");
			buf = bundle.getByteArray("buf");

			BitmapFactory.Options options = new BitmapFactory.Options();
			options.inSampleSize = 1;// 7就代表容量变为以前容量的1/7
			 bm = BitmapFactory.decodeFile(uri, options);
			myinfo_userface.setImageDrawable(new BitmapDrawable(bm));
			break;
		default:
			break;

		}
		super.onActivityResult(requestCode, resultCode, data);

	}

}
