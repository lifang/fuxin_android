package com.fuwu.mobileim.activity;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.TextView;
import android.widget.Toast;

import com.fuwu.mobileim.R;
import com.fuwu.mobileim.view.CircularImage;

/**
 * @作者 马龙
 * @时间 创建时间：2014-5-14 下午12:06:40
 */
public class SettingsActivity extends Activity {

	private ListView listview;
	SettingBottomAdapter adapter;
	private CircularImage setting_userface;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.settings);
		adapter = new SettingBottomAdapter();
		init();

	}

	private void init() {
		RelativeLayout setting_top = (RelativeLayout) findViewById(R.id.setting_top);// 用户个人信息部分
		RelativeLayout a_layout = (RelativeLayout) findViewById(R.id.setting_userface0);
		CircularImage userface = (CircularImage) findViewById(R.id.setting_userface);// 头像
		ImageView setting_sex_item = (ImageView) findViewById(R.id.setting_sex_item);// 性别
		ImageView certification_one = (ImageView) findViewById(R.id.certification_one);// 验证1
		ImageView certification_two = (ImageView) findViewById(R.id.certification_two);// 验证2
		ImageView certification_three = (ImageView) findViewById(R.id.certification_three);// 验证3
		LayoutParams param = (LayoutParams) a_layout.getLayoutParams();
		param.leftMargin = 40;
		param.topMargin = 20;
		RelativeLayout setting_relativeLayout1 = (RelativeLayout) findViewById(R.id.setting_relativeLayout1);
		LayoutParams param2 = (LayoutParams) setting_relativeLayout1
				.getLayoutParams();
		param2.leftMargin = 30;
		param2.topMargin = 20;
		// setting_userface = (CircularImage)
		// findViewById(R.id.setting_userface);
		// setting_userface.
		listview = (ListView) findViewById(R.id.setting_listview);
		listview.setDivider(null);
		listview.setAdapter(adapter);
		listview.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {

				jumpToActivity(position);

			}
		});
		setting_top.setOnClickListener(listener1);// 给个人信息部分设置监听
	}

	private View.OnClickListener listener1 = new View.OnClickListener() {
		@Override
		public void onClick(View v) {
			Toast.makeText(getApplicationContext(), "跳到个人信息页面",
					Toast.LENGTH_LONG).show();
		}
	};

	/**
	 * 跳转到功能页面
	 * 
	 */
	private void jumpToActivity(int num) {
		Intent intent = new Intent();
		switch (num) {
		case 0:// 新版本检测
			Toast.makeText(getApplicationContext(), "新版本检测", Toast.LENGTH_LONG)
					.show();
			break;
		case 1:// 清除全部聊天记录
			Toast.makeText(getApplicationContext(), "清除全部聊天记录",
					Toast.LENGTH_LONG).show();
			break;
		case 2:// 消息推送
			Toast.makeText(getApplicationContext(), "消息推送", Toast.LENGTH_LONG)
					.show();
			// Intent intent = new Intent
			// (SettingsActivity.this,SettingsActivity.class);
			// startActivity(intent);
			break;
		case 3:// 修改密码
			Toast.makeText(getApplicationContext(), "修改密码", Toast.LENGTH_LONG)
					.show();
			intent.setClass(SettingsActivity.this, UpdatePwdActivity.class);
			startActivity(intent);
			break;
		case 4:// 屏蔽管理
				// Toast.makeText(getApplicationContext(), "屏蔽管理" ,
				// Toast.LENGTH_LONG).show();
			intent.setClass(SettingsActivity.this,
					BlockManagementActivity.class);
			startActivity(intent);
			break;
		case 5:// 系统公告管理
			Toast.makeText(getApplicationContext(), "系统公告管理", Toast.LENGTH_LONG)
					.show();
			// Intent intent = new Intent
			// (SettingsActivity.this,SettingsActivity.class);
			// startActivity(intent);
			intent.setClass(SettingsActivity.this, SystemPushActivity.class);
			startActivity(intent);
			break;
		case 6:// 退出登录
			intent.setClass(SettingsActivity.this, LoginActivity.class);
			startActivity(intent);
			break;
		default:
			break;
		}
	}

	public class SettingBottomAdapter extends BaseAdapter {

		private int[] icon = new int[] { R.drawable.setting_image1,
				R.drawable.setting_image2, R.drawable.setting_image3,
				R.drawable.setting_image4, R.drawable.setting_image5,
				R.drawable.setting_image6, R.drawable.setting_image7 }; // icon
																		// 集合
		private String[] titleArr = new String[] { "新版本检测", "清除全部聊天记录", "消息推送",
				"修改密码", "屏蔽管理", "系统公告管理", "退出登录" }; //

		public int getCount() {
			return titleArr.length;
		}

		public Object getItem(int position) {
			return null;
		}

		public long getItemId(int position) {
			return 0;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			RelativeLayout layout = null;
			if (convertView == null) {
				layout = (RelativeLayout) LayoutInflater.from(
						SettingsActivity.this).inflate(
						R.layout.setting_adapter_item, null);
			} else {
				layout = (RelativeLayout) convertView;
			}
			ImageView im = (ImageView) layout
					.findViewById(R.id.setting_adapter_item_iv);
			TextView titleStr = (TextView) layout.findViewById(R.id.titleStr);
			Resources resources = getResources();
			im.setImageResource(icon[position]);
			titleStr.setText(titleArr[position]);
			RelativeLayout re = (RelativeLayout) layout
					.findViewById(R.id.notice_sign);
			TextView te = (TextView) layout.findViewById(R.id.notice_number);

			if (position == 5) {
				// 如果有通知，则显示通知数目
				if (true) {
					te.setText("4");
				} else {
					re.setVisibility(View.GONE);
				}
			} else { // 当前postion 不为5时，隐藏黑色线条和圆形红色块
				View view = (View) layout.findViewById(R.id.item_thicklines);
				view.setVisibility(View.GONE);
				re.setVisibility(View.GONE);
			}
			return layout;
		}
	}

}
