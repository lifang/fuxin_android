package com.fuwu.mobileim.activity;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.RelativeLayout.LayoutParams;

import com.fuwu.mobileim.R;
import com.fuwu.mobileim.pojo.ProfilePojo;
import com.fuwu.mobileim.util.FuXunTools;
import com.fuwu.mobileim.util.FxApplication;
import com.fuwu.mobileim.view.CircularImage;

public class MyInformationActivity extends Activity {
	private ImageButton my_info_back;// 返回按钮
	private ImageButton my_info_confirm;// 保存按钮
	private FxApplication fxApplication;
	private ProfilePojo profilePojo;
	private CircularImage myinfo_userface;
	private TextView myinfo_nickname, myinfo_certification, myinfo_mobile,
			myinfo_email, myinfo_birthday, myinfo_sex;
	private Handler handler = new Handler() {
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case 0:
				// prodialog.dismiss();

				break;

			case 7:
				// Toast.makeText(getApplicationContext(),
				// ExerciseBookParams.INTERNET, Toast.LENGTH_SHORT).show();
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
	 * 获得相关组件
	 * 
	 * 
	 */
	private void init() {
		myinfo_userface = (CircularImage) findViewById(R.id.myinfo_userface);
		myinfo_nickname = (TextView) findViewById(R.id.myinfo_nickname);
		myinfo_certification = (TextView) findViewById(R.id.myinfo_certification);
		myinfo_mobile = (TextView) findViewById(R.id.myinfo_mobile);
		myinfo_email = (TextView) findViewById(R.id.myinfo_email);
		myinfo_birthday = (TextView) findViewById(R.id.myinfo_birthday);
		myinfo_sex = (TextView) findViewById(R.id.myinfo_sex);

		// 设置头像
		String face_str = profilePojo.getTileUrl();
		if (face_str.length() > 4) {
			FuXunTools.setBackground(face_str, myinfo_userface);
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
		int sex = profilePojo.getGender();
		if (sex == 1) {// 男
			myinfo_sex.setText("男");
		} else if (sex == 0) {// 女
			myinfo_sex.setText("女");
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
			Toast.makeText(getApplicationContext(), "提交修改", Toast.LENGTH_LONG)
					.show();
		}
	};

}
