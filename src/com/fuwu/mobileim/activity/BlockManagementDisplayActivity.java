package com.fuwu.mobileim.activity;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.BitmapFactory;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.ViewTreeObserver.OnPreDrawListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.fuwu.mobileim.R;
import com.fuwu.mobileim.model.Models.BlockContactRequest;
import com.fuwu.mobileim.model.Models.BlockContactResponse;
import com.fuwu.mobileim.pojo.ContactPojo;
import com.fuwu.mobileim.pojo.ShortContactPojo;
import com.fuwu.mobileim.util.DBManager;
import com.fuwu.mobileim.util.FuXunTools;
import com.fuwu.mobileim.util.FxApplication;
import com.fuwu.mobileim.util.HttpUtil;
import com.fuwu.mobileim.util.Urlinterface;
import com.fuwu.mobileim.view.CircularImage;
import com.fuwu.mobileim.view.MyDialog;

public class BlockManagementDisplayActivity extends Activity {
	private DBManager db;

	private Handler handler = new Handler() {
		/*
		 * (non-Javadoc)
		 * 
		 * @see android.os.Handler#handleMessage(android.os.Message)
		 */
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case 0:
				builder.dismiss();
				Toast.makeText(getApplicationContext(), "恢复成功",
						Toast.LENGTH_SHORT).show();

				Intent intent2 = new Intent();

				BlockManagementDisplayActivity.this.setResult(-11, intent2);
				// 关闭当前activity
				BlockManagementDisplayActivity.this.finish();

				break;
			case 6:
				builder.dismiss();
				Toast.makeText(getApplicationContext(), "恢复失败",
						Toast.LENGTH_SHORT).show();
				break;

			case 7:
				Toast.makeText(getApplicationContext(), R.string.no_internet,
						Toast.LENGTH_SHORT).show();
				break;
			case 9:
				builder.dismiss();
				new Handler().postDelayed(new Runnable() {
					public void run() {
						Intent intent = new Intent(
								BlockManagementDisplayActivity.this,
								LoginActivity.class);
						startActivity(intent);
						clearActivity();
					}
				}, 3500);
				FuXunTools.initdate(preferences, fxApplication);
				Toast.makeText(getApplicationContext(), "您的账号已在其他手机登陆",
						Toast.LENGTH_LONG).show();
				break;
			}
		}
	};
	private int contactId = -1;
	private ShortContactPojo contact;
	SharedPreferences preferences;
	int user_id = -1;
	String Token = "";
	private FxApplication fxApplication;
	private MyDialog builder;
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);
		setContentView(R.layout.block_management_display);
		Setwindow(0.19f);// 设置窗口化
		db = new DBManager(this);
		fxApplication = (FxApplication) getApplication();
		fxApplication.getActivityList().add(this);
		preferences = getSharedPreferences(Urlinterface.SHARED,
				Context.MODE_PRIVATE);
		user_id = preferences.getInt("user_id", -1);
		Token = preferences.getString("Token", "");
		Intent intent = getIntent();//
		contactId = intent.getIntExtra("contactId", -1);
		contact = db.queryContact(user_id, contactId);
		setRelatedData();

	}

	public void Setwindow(float s) {
		WindowManager m = getWindowManager();
		Display d = m.getDefaultDisplay(); // 为获取屏幕宽、高

		android.view.WindowManager.LayoutParams p = getWindow().getAttributes(); // 获取对话框当前的参数值
		// p.height = (int) (d.getHeight() * s); // 高度设置
		p.width = (int) (d.getWidth() * 0.9); // 宽度设置
		p.alpha = 1.0f; // 设置本 身透明度
		p.dimAmount = 0.8f; // 设置黑暗度
		p.y = -150; // 设置位置
		getWindow().setAttributes(p); // 设置生效

	}

	public void setRelatedData() {
		// 头像
		CircularImage block_display_userface = (CircularImage) findViewById(R.id.block_display_userface);
		// 昵称
		TextView block_display_name = (TextView) findViewById(R.id.block_display_name);
		// 恢复接收此人消息
		Button block_display_restore = (Button) findViewById(R.id.block_display_restore);

		// 设置头像
		String face_str = contact.getUserface_url();
		if (face_str != null && face_str.length() > 4) {
			File f = new File(Urlinterface.head_pic, contact.getContactId()
					+ "");
			if (f.exists()) {
				Log.i("linshi------------", "加载本地图片");
				block_display_userface.setImageDrawable(new BitmapDrawable(
						BitmapFactory.decodeFile(Urlinterface.head_pic
								+ contact.getContactId())));
			} else {
				FuXunTools.set_bk(contact.getContactId(), face_str,
						block_display_userface);
			}
		}

		// 显示级别为：备注名>真实姓名>昵称

		String customname = contact.getCustomName();
		if (customname != null && customname.length() > 0) {
			block_display_name.setText(customname);
		} else {
			block_display_name.setText(contact.getName());
		}
		// // 设置名称
		// block_display_name.setText(contact.getName());
		// // 设置备注
		// block_display_notename.setText(contact.getCustomName());
		// // 设置性别
		// int sex = contact.getSex();
		// if (sex == 0) { // 男
		// block_display_sex_item.setBackgroundResource(R.drawable.nan);
		// } else if (sex == 1) {
		// block_display_sex_item.setBackgroundResource(R.drawable.nv);
		// } else {
		// block_display_sex_item.setVisibility(View.GONE);
		// }
		//
		// // 设置交易订阅
		// String str = FuXunTools.toNumber(contact.getSource());
		// if (FuXunTools.isExist(str, 0, 1)) {
		// block_display_yue.setVisibility(View.VISIBLE);
		// } else {
		// block_display_yue.setVisibility(View.GONE);
		// }
		// if (FuXunTools.isExist(str, 2, 3)) {
		// block_display_gou.setVisibility(View.VISIBLE);
		// } else {
		// block_display_gou.setVisibility(View.GONE);
		// }
		// 接收此人消息
		block_display_restore.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				if (FuXunTools.isConnect(BlockManagementDisplayActivity.this)) {
					builder= FuXunTools.showLoading(getLayoutInflater(),BlockManagementDisplayActivity.this,"正在恢复..");
					Thread thread = new Thread(new BlockContact());
					thread.start();
				} else {
					handler.sendEmptyMessage(7);
				}

			}
		});

	}

	/**
	 * 
	 * 恢复联系人到通讯录
	 * 
	 * 
	 */

	class BlockContact implements Runnable {
		public void run() {
			try {
				Log.i("linshi", "-----------------");

				BlockContactRequest.Builder builder = BlockContactRequest
						.newBuilder();
				builder.setUserId(user_id);
				builder.setToken(Token);
				builder.setContactId(contactId);
				builder.setIsBlocked(false);

				BlockContactRequest response = builder.build();

				byte[] by = HttpUtil.sendHttps(response.toByteArray(),
						Urlinterface.BlockContact, "PUT");
				if (by != null && by.length > 0) {

					BlockContactResponse res = BlockContactResponse
							.parseFrom(by);
					if (res.getIsSucceed()) {
						handler.sendEmptyMessage(0);
					} else {
						int ErrorCode = res.getErrorCode().getNumber();
						if (ErrorCode == 2001) {
							handler.sendEmptyMessage(9);
						} else {
							handler.sendEmptyMessage(6);
						}
					}
				} else {
					handler.sendEmptyMessage(6);
				}

				//
			} catch (Exception e) {
				handler.sendEmptyMessage(6);
			}
		}
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
