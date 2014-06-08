package com.fuwu.mobileim.activity;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.fuwu.mobileim.R;
import com.fuwu.mobileim.model.Models.BlockContactRequest;
import com.fuwu.mobileim.model.Models.BlockContactResponse;
import com.fuwu.mobileim.pojo.ContactPojo;
import com.fuwu.mobileim.util.DBManager;
import com.fuwu.mobileim.util.FuXunTools;
import com.fuwu.mobileim.util.FxApplication;
import com.fuwu.mobileim.util.HttpUtil;
import com.fuwu.mobileim.util.Urlinterface;
import com.fuwu.mobileim.view.CircularImage;

public class BlockManagementDisplayActivity extends Activity {
	private ProgressDialog prodialog;
	private ListView mListView;
	private DBManager db;
	private List<ContactPojo> list = new ArrayList<ContactPojo>();

	private Handler handler = new Handler() {
		/*
		 * (non-Javadoc)
		 * 
		 * @see android.os.Handler#handleMessage(android.os.Message)
		 */
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case 0:
				prodialog.dismiss();
				Toast.makeText(getApplicationContext(), "恢复成功",
						Toast.LENGTH_SHORT).show();
			
				Intent intent2 = new Intent();

				BlockManagementDisplayActivity.this.setResult(-11, intent2);
				// 关闭当前activity
				BlockManagementDisplayActivity.this.finish();
				
				break;
			case 1:
				 prodialog.dismiss();
				Toast.makeText(getApplicationContext(), "恢复失败",
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
	private int contactId = -1;
	private ContactPojo contact;
	private FxApplication fxApplication;

	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);
		setContentView(R.layout.block_management_display);
		fxApplication = (FxApplication) getApplication();
		Setwindow(0.19f);// 设置窗口化
		db = new DBManager(this);
		Intent intent = getIntent();//
		contactId = intent.getIntExtra("contactId", -1);
		for (int i = 0; i < db.queryContactList(fxApplication.getUser_id()).size(); i++) {
			if (db.queryContactList(fxApplication.getUser_id()).get(i).getContactId() == contactId) {
				contact = db.queryContactList(fxApplication.getUser_id()).get(i);
				break;
			}
		}
		setRelatedData();

	}

	public void Setwindow(float s) {
		WindowManager m = getWindowManager();
		Display d = m.getDefaultDisplay(); // 为获取屏幕宽、高

		android.view.WindowManager.LayoutParams p = getWindow().getAttributes(); // 获取对话框当前的参数值
		p.height = (int) (d.getHeight() * s); // 高度设置
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
		// 性别
		ImageView block_display_sex_item = (ImageView) findViewById(R.id.block_display_sex_item);
		// 交易
		ImageView block_display_gou = (ImageView) findViewById(R.id.block_display_gou);
		// 订阅
		ImageView block_display_yue = (ImageView) findViewById(R.id.block_display_yue);
		// 备注
		TextView block_display_notename = (TextView) findViewById(R.id.block_display_notename);
		// 恢复接收此人消息
		TextView block_display_restore = (TextView) findViewById(R.id.block_display_restore);

		// 设置头像
		String face_str = contact.getUserface_url();
		if (face_str.length() > 4) {
			face_str=Urlinterface.IP+face_str;
			File f = new File(Urlinterface.head_pic, contact.getContactId()+"");
			if (f.exists()) {
				Log.i("linshi------------", "加载本地图片");
				Drawable dra = new BitmapDrawable(
						BitmapFactory.decodeFile(Urlinterface.head_pic + contact.getContactId()));
				block_display_userface.setImageDrawable(dra);
			} else {
				FuXunTools.set_bk(contact.getContactId(),face_str, block_display_userface);
			}
		}

		// 设置昵称
		block_display_name.setText(contact.getName());

		// 设置性别
		int sex = contact.getSex();
		if (sex == 0) { // 男
			block_display_sex_item.setBackgroundResource(R.drawable.nan);
		} else if (sex == 1) {
			block_display_sex_item.setBackgroundResource(R.drawable.nv);
		} else {
			block_display_sex_item.setVisibility(View.GONE);
		}

		// 设置交易订阅
		String str = FuXunTools.toNumber(contact.getSource());
		if (FuXunTools.isExist(str, 0, 1)) {
			block_display_gou.setVisibility(View.VISIBLE);
		} else {
			block_display_gou.setVisibility(View.GONE);
		}
		if (FuXunTools.isExist(str, 2, 3)) {
			block_display_yue.setVisibility(View.VISIBLE);
		} else {
			block_display_yue.setVisibility(View.GONE);
		}
		// 设置备注
		block_display_notename.setText(contact.getName());
		// 接收此人消息
		block_display_restore.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				prodialog = new ProgressDialog(BlockManagementDisplayActivity.this);
				prodialog.setMessage("正在恢复...");
				prodialog.setCanceledOnTouchOutside(false);
				prodialog.show();
				Thread thread = new Thread(new BlockContact());
				thread.start();

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
				builder.setUserId(fxApplication.getUser_id());
				builder.setToken(fxApplication.getToken());
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
}
