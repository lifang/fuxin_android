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
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.fuwu.mobileim.R;
import com.fuwu.mobileim.model.Models.BlockContactRequest;
import com.fuwu.mobileim.model.Models.BlockContactResponse;
import com.fuwu.mobileim.pojo.ShortContactPojo;
import com.fuwu.mobileim.util.DBManager;
import com.fuwu.mobileim.util.FuXunTools;
import com.fuwu.mobileim.util.FxApplication;
import com.fuwu.mobileim.util.HttpUtil;
import com.fuwu.mobileim.util.ImageCacheUtil;
import com.fuwu.mobileim.util.Urlinterface;
import com.fuwu.mobileim.view.CircularImage;

public class BlockManagementActivity extends Activity  {
	private DBManager db;
	private ProgressDialog prodialog;
	private int index = -1;
	private ListView mListView;
	private myListViewAdapter clvAdapter;
	private ImageButton block_management_back;// 返回按钮
	private List<ShortContactPojo> list = new ArrayList<ShortContactPojo>();
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
				db = new DBManager(BlockManagementActivity.this);
				if (!db.isOpen()) {
					db = new DBManager(BlockManagementActivity.this);
				}
				Toast.makeText(getApplicationContext(), "恢复成功",
						Toast.LENGTH_SHORT).show();

				db.modifyContactBlock(0, user_id, list.get(index)
						.getContactId());
				list.remove(index);
				clvAdapter.notifyDataSetChanged();
				break;
			case 1:
				prodialog.dismiss();
				Toast.makeText(getApplicationContext(), "恢复失败",
						Toast.LENGTH_SHORT).show();
				break;
			case 2:
				db = new DBManager(BlockManagementActivity.this);
				if (!db.isOpen()) {
					db = new DBManager(BlockManagementActivity.this);
				}

				db.modifyContactBlock(0, user_id, list.get(index)
						.getContactId());
				list.remove(index);
				clvAdapter.notifyDataSetChanged();
				break;
			case 6:
				prodialog.dismiss();
				Toast.makeText(getApplicationContext(), "恢复失败",
						Toast.LENGTH_SHORT).show();
				break;

			case 7:
				Toast.makeText(getApplicationContext(), R.string.no_internet,
						Toast.LENGTH_SHORT).show();
				break;
			case 9:
				prodialog.dismiss();
				new Handler().postDelayed(new Runnable() {
					public void run() {
						Intent intent = new Intent(BlockManagementActivity.this,
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
	int user_id = -1;
	String Token = "";
	private FxApplication fxApplication;
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.block_management);
		db = new DBManager(this);
		fxApplication = (FxApplication) getApplication();
		fxApplication.getActivityList().add(this);
		preferences = getSharedPreferences(Urlinterface.SHARED,
				Context.MODE_PRIVATE);
		user_id = preferences.getInt("user_id", -1);
		Token = preferences.getString("Token", "");
		// 获得被屏蔽的联系人
		List<ShortContactPojo> contactsList = db.queryContactList(user_id);
		for (int i = 0; i < contactsList.size(); i++) {
			if (contactsList.get(i).getIsBlocked() == 1) {
				list.add(contactsList.get(i));
			}
		}

		block_management_back = (ImageButton) findViewById(R.id.block_management_back);
		block_management_back.setOnTouchListener(new View.OnTouchListener()
		{
		    @Override             
		    public boolean onTouch(View v, MotionEvent event)
		    {              
		        if(event.getAction()==MotionEvent.ACTION_DOWN)
		        {                
		        	block_management_back.getBackground().setAlpha(70);//设置图片透明度0~255，0完全透明，255不透明                    imgButton.invalidate();             
		        }              
		        else if (event.getAction() == MotionEvent.ACTION_UP) 
		        {                  
		        	block_management_back.getBackground().setAlpha(255);//还原图片 
		        }               
		        return false;         
		    }     
		});
		mListView = (ListView) findViewById(R.id.block_management_listView);
		mListView.setDivider(null);
		clvAdapter = new myListViewAdapter(this);
		mListView.setAdapter(clvAdapter);
		block_management_back.setOnClickListener(listener1);// 给返回按钮设置监听
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

				// optional string token = 1;
				// optional int32 userId = 2;
				// optional int32 contactId = 3;
				// optional bool isBlocked = 4;
				Log.i("linshi", "-----------------");

				BlockContactRequest.Builder builder = BlockContactRequest
						.newBuilder();
				builder.setUserId(user_id);
				builder.setToken(Token);
				builder.setContactId(list.get(index).getContactId());
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
						if (ErrorCode==2001) {
							handler.sendEmptyMessage(9);
						}else {
							handler.sendEmptyMessage(1);	
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

	private View.OnClickListener listener1 = new View.OnClickListener() {
		@Override
		public void onClick(View v) {
			BlockManagementActivity.this.finish();
		}
	};

	public class myListViewAdapter extends BaseAdapter {
		private LayoutInflater mInflater;
		private Context mcontext = null;

		public myListViewAdapter(Context context) {
			this.mcontext = context;
			this.mInflater = LayoutInflater.from(mcontext);
		}

		public int getCount() {
			return list.size();
		}

		public Object getItem(int arg0) {
			return list.get(arg0);
		}

		public long getItemId(int arg0) {
			return arg0;
		}

		public View getView(final int arg0, View arg1, ViewGroup arg2) {
			final ShortContactPojo contact = list.get(arg0);
			RelativeLayout layout = null;
			if (arg1 == null) {
				layout = (RelativeLayout) LayoutInflater.from(
						BlockManagementActivity.this).inflate(
						R.layout.block_management_adapter_item, null);
			} else {
				layout = (RelativeLayout) arg1;
			}
			CircularImage head = (CircularImage) layout
					.findViewById(R.id.block_face);
			// 设置头像
			String face_str = contact.getUserface_url();
			if (face_str != null && face_str.length() > 4) {
				File f = new File(Urlinterface.head_pic, contact.getContactId()
						+ "");
				
				if (f.exists()) {
					Log.i("linshi------------", "加载本地图片");
					head.setImageDrawable(new BitmapDrawable(
							BitmapFactory.decodeFile(Urlinterface.head_pic
									+ contact.getContactId())));
				} else {
					FuXunTools.set_bk(contact.getContactId(), face_str,
							head);
				}
			}
			TextView name = (TextView) layout.findViewById(R.id.block_name);

			String customname = contact.getCustomName();
			if (customname != null && customname.length() > 0) {
				name.setText(customname);
			} else {
				name.setText(contact.getName());
			}
			final Button restore = (Button) layout.findViewById(R.id.block_restore);
			restore.setOnClickListener(new View.OnClickListener() {
				public void onClick(View v) {
					index = arg0;
					if (FuXunTools.isConnect(BlockManagementActivity.this)) {
						prodialog = new ProgressDialog(
								BlockManagementActivity.this);
						prodialog.setMessage("正在恢复...");
						prodialog.setCanceledOnTouchOutside(false);
						prodialog.show();
						Thread thread = new Thread(new BlockContact());
						thread.start();
					} else {
						handler.sendEmptyMessage(7);
					}
				}
			});
			restore.setOnTouchListener(new View.OnTouchListener()
			{
			    @Override             
			    public boolean onTouch(View v, MotionEvent event)
			    {              
			        if(event.getAction()==MotionEvent.ACTION_DOWN)
			        {                
			        	restore.getBackground().setAlpha(70);//设置图片透明度0~255，0完全透明，255不透明                    imgButton.invalidate();             
			        	restore.setTextColor(Color.GRAY);
			        }              
			        else if (event.getAction() == MotionEvent.ACTION_UP) 
			        {                  
			        	restore.getBackground().setAlpha(255);//还原图片 
			        	restore.setTextColor(Color.BLACK);
			        }               
			        return false;         
			    }     
			});
			layout.setOnClickListener(new View.OnClickListener() {

				@Override
				public void onClick(View v) {
					index = arg0;
					Intent intent = new Intent(BlockManagementActivity.this,
							BlockManagementDisplayActivity.class);
					intent.putExtra("contactId", contact.getContactId());
					startActivityForResult(intent, 0);
				}
			});
			return layout;
		}
	}

	
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {

		switch (resultCode) {
		case -11:
			handler.sendEmptyMessage(2);

			break;
		default:
			break;

		}
		super.onActivityResult(requestCode, resultCode, data);

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
