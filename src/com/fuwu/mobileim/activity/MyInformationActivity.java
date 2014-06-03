package com.fuwu.mobileim.activity;

import android.app.Activity;
import android.app.ProgressDialog;
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
import com.fuwu.mobileim.model.Models.Profile;
import com.fuwu.mobileim.pojo.ProfilePojo;
import com.fuwu.mobileim.util.FxApplication;
import com.fuwu.mobileim.util.HttpUtil;
import com.fuwu.mobileim.util.Urlinterface;
import com.fuwu.mobileim.view.CircularImage;

public class MyInformationActivity extends Activity {
	private ProgressDialog prodialog;
	private ImageButton my_info_back;// 返回按钮
	private ImageButton my_info_confirm;// 保存按钮
	private FxApplication fxApplication;
	private ProfilePojo profilePojo;
	private CircularImage myinfo_userface;
	private EditText myinfo_nickname;
	private TextView  myinfo_certification, myinfo_mobile,
			myinfo_email, myinfo_birthday, myinfo_sex;
	private Handler handler = new Handler() {
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case 0:
				 prodialog.dismiss();
				 Toast.makeText(getApplicationContext(),
							 "修改成功", Toast.LENGTH_SHORT).show();
				break;
			case 1:
				 prodialog.dismiss();
				 Toast.makeText(getApplicationContext(),
							 "修改失败", Toast.LENGTH_SHORT).show();
				break;
			case 6:
				prodialog.dismiss();
				Toast.makeText(getApplicationContext(), "请求失败", Toast.LENGTH_SHORT)
						.show();
				break;

			case 7:
				prodialog.dismiss();
				Toast.makeText(getApplicationContext(), "网络错误", Toast.LENGTH_SHORT)
				.show();
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

		profilePojo = fxApplication.getProfilePojo();
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

//		// 设置头像
//		String face_str = profilePojo.getTileUrl();
//		if (face_str.length() > 4) {
//			FuXunTools.setBackground(face_str, myinfo_userface);
//		} else {
//			myinfo_userface.setImageResource(R.drawable.moren);
//		}
//		// 设置昵称
//		myinfo_nickname.setText(profilePojo.getNickName()+"111");
//
//		// 设置认证行业
//		String str1 = profilePojo.getLisence();
//		myinfo_certification.setText(str1);
//
//		// 手机
//		String str3 = profilePojo.getMobile();
//		myinfo_mobile.setText(str3);
//		// 邮箱
//		String str2 = profilePojo.getEmail();
//		myinfo_email.setText(str2);
//		// 生日
//		myinfo_birthday.setText(profilePojo.getBirthday());
//		// 设置性别
//		int sex = profilePojo.getGender();
//		if (sex == 1) {// 男
//			myinfo_sex.setText("男");
//		} else if (sex == 2) {// 女
//			myinfo_sex.setText("女");
//		}else{
//		myinfo_sex.setText("保密");
//	}

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
				
				Profile.Builder pb = Profile.newBuilder();
				pb.setUserId(profilePojo.getUserId());
				pb.setName(profilePojo.getName());
				pb.setNickName(nickname_str);
				pb.setGender(profilePojo.getGender());
				pb.setMobilePhoneNum(profilePojo.getMobile());
				pb.setEmail(profilePojo.getEmail());
				pb.setBirthday(profilePojo.getBirthday());
				pb.setTileUrl(profilePojo.getTileUrl());
				pb.setIsProvider(profilePojo.getIsProvider());
				pb.setLisence(profilePojo.getLisence());
				pb.setPublishClassType(profilePojo.getPublishClassType());
				Log.i("linshi", "-----------------");

				ChangeProfileRequest.Builder builder = ChangeProfileRequest.newBuilder();
				builder.setUserId(1);
				builder.setToken("MockToken");
				builder.setProfile(pb);
				ChangeProfileRequest response = builder.build();

				byte[] by = HttpUtil.sendHttps(response.toByteArray(),
						Urlinterface.ChangeProfile, "PUT");
				if (by!= null  && by.length> 0) {

					ChangeProfileResponse res = ChangeProfileResponse.parseFrom(by);
					if (res.getIsSucceed()) {
						handler.sendEmptyMessage(0);
					} else {
						handler.sendEmptyMessage(1);
					}
				}else {
					handler.sendEmptyMessage(6);
				}
				// 
			} catch (Exception e) {
				handler.sendEmptyMessage(7);
			}
		}
	}

}
