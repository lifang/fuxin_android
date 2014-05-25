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
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.TextView;
import android.widget.Toast;

import com.comdo.fuxun.R;
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
		
		LinearLayout a_layout = (LinearLayout) findViewById(R.id.setting_userface0);
		LayoutParams param = (LayoutParams) a_layout.getLayoutParams();
		param.leftMargin = 60;
		param.height = 150;
		param.width = 150;
//		setting_userface = (CircularImage) findViewById(R.id.setting_userface);
//		setting_userface.
		listview=(ListView) findViewById(R.id.setting_listview);
		listview.setDivider(null);
		listview.setAdapter(adapter);
		listview.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {

				jumpToActivity(position);

			}
		});
	}
	/**
	 * 跳转到功能页面
	 * 
	 */
	private void jumpToActivity(int num) {
		switch (num) {
		case 0://新版本检测
//			
			break;
		case 1://清除全部聊天记录

			break;
		case 2://消息推送
			Toast.makeText(getApplicationContext(), "消息推送" ,
					Toast.LENGTH_LONG).show();
//			Intent intent = new Intent (SettingsActivity.this,SettingsActivity.class);			
//			startActivity(intent);
			break;
		case 3://修改密码
			Toast.makeText(getApplicationContext(), "修改密码" ,
					Toast.LENGTH_LONG).show();
//			Intent intent = new Intent (SettingsActivity.this,SettingsActivity.class);			
//			startActivity(intent);
			break;
		case 4://屏蔽管理
//			Toast.makeText(getApplicationContext(), "屏蔽管理" ,
//					Toast.LENGTH_LONG).show();
			Intent intent = new Intent (SettingsActivity.this,BlockManagementActivity.class);			
			startActivity(intent);
			break;
		case 5://系统公告管理
			Toast.makeText(getApplicationContext(), "系统公告管理" ,
					Toast.LENGTH_LONG).show();
//			Intent intent = new Intent (SettingsActivity.this,SettingsActivity.class);			
//			startActivity(intent);
			break;
		case 6://退出登录
//			
			break;
		default:
			break;
		}
	}
	
	
	public class SettingBottomAdapter extends BaseAdapter {

		private int[] icon = new int[] { R.drawable.setting_image1,
				R.drawable.setting_image2, R.drawable.setting_image3,
				R.drawable.setting_image4, R.drawable.setting_image5, R.drawable.setting_image6, R.drawable.setting_image7 }; // icon 集合
		private String[] titleArr = new String[] { "新版本检测", "清除全部聊天记录", "消息推送", "修改密码",
				"屏蔽管理", "系统公告管理", "退出登录"  }; //

		@Override
		public int getCount() {
			// TODO Auto-generated method stub
			return titleArr.length;
		}

		@Override
		public Object getItem(int position) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public long getItemId(int position) {
			// TODO Auto-generated method stub
			return 0;
		}
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			RelativeLayout layout = null;
			if (convertView == null) {
				layout = (RelativeLayout)  LayoutInflater.from(SettingsActivity.this).inflate(
						R.layout.setting_adapter_item, null);
			} else {
				layout = (RelativeLayout) convertView;
			}
			ImageView im = (ImageView) layout.findViewById(R.id.setting_adapter_item_iv);
			TextView titleStr = (TextView) layout.findViewById(R.id.titleStr);
			Resources resources = getResources();
			im.setImageResource(icon[position]);
			titleStr.setText(titleArr[position]);
			RelativeLayout re = (RelativeLayout) layout.findViewById(R.id.notice_sign);
			TextView te = (TextView) layout.findViewById(R.id.notice_number);
			
			if (position==5) {
//				如果有通知，则显示通知数目
				if (true) {
					te.setText("4");
				}else {
					re.setVisibility(View.GONE);
				}
			}else { //  当前postion 不为5时，隐藏黑色线条和圆形红色块
				View view  = (View)layout.findViewById(R.id.item_thicklines);
				view.setVisibility(View.GONE);
				re.setVisibility(View.GONE);
			}
			return layout;
		}
	}

	
}
