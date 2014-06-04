package com.fuwu.mobileim.activity;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
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
import com.fuwu.mobileim.pojo.ContactPojo;
import com.fuwu.mobileim.util.FuXunTools;
import com.fuwu.mobileim.util.FxApplication;
import com.fuwu.mobileim.util.HttpUtil;
import com.fuwu.mobileim.util.Urlinterface;
import com.fuwu.mobileim.view.CircularImage;

public class BlockManagementActivity extends Activity {
	private ProgressDialog prodialog;
	private int index = -1;
	private ListView mListView;
	private myListViewAdapter clvAdapter;
	private ImageButton block_management_back;// 返回按钮
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
				list.remove(index);
				for (int i = 0; i < fxApplication.getContactsList().size(); i++) {
					if (fxApplication.getContactsList().get(i).getContactId() == list
							.get(index).getContactId()) {
						fxApplication.getContactsList().get(i)
								.setIsBlocked(0);
					}
					break;
				}
				/*
				 * 
				 * 更新到数据库！！！！！！
				 */
				clvAdapter.notifyDataSetChanged();
				break;
			case 1:
				 prodialog.dismiss();
				Toast.makeText(getApplicationContext(), "恢复失败",
						Toast.LENGTH_SHORT).show();
				break;
			case 2:
				list.remove(index);
				for (int i = 0; i < fxApplication.getContactsList().size(); i++) {
					if (fxApplication.getContactsList().get(i).getContactId() == list
							.get(index).getContactId()) {
						fxApplication.getContactsList().get(i)
								.setIsBlocked(0);
					}
					break;
				}
				/*
				 * 
				 * 更新到数据库！！！！！！
				 */
				clvAdapter.notifyDataSetChanged();
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
	private FxApplication fxApplication;

	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.block_management);
		fxApplication = (FxApplication) getApplication();
		// 获得被屏蔽的联系人
		for (int i = 0; i < fxApplication.getContactsList().size(); i++) {
			if (fxApplication.getContactsList().get(i).getIsBlocked()==1) {
				list.add(fxApplication.getContactsList().get(i));
			}
		}
		block_management_back = (ImageButton) findViewById(R.id.block_management_back);
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
				builder.setUserId(fxApplication.getUser_id());
				builder.setToken(fxApplication.getToken());
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
			final ContactPojo contact = list.get(arg0);
			RelativeLayout layout = null;
			if (arg1 == null) {
				layout = (RelativeLayout) LayoutInflater.from(
						BlockManagementActivity.this).inflate(
						R.layout.block_management_adapter_item, null);
			} else {
				layout = (RelativeLayout) arg1;
			}
			 CircularImage head = (CircularImage)
			 layout.findViewById(R.id.block_face);
			// 设置头像
				String face_str = contact.getUserface_url();
				if (face_str.length() > 4) {
					File f = new File(Urlinterface.head_pic, "bbb");
					if (f.exists()) {
						Log.i("linshi------------", "加载本地图片");
						Drawable dra = new BitmapDrawable(
								BitmapFactory.decodeFile(Urlinterface.head_pic + "bbb"));
						head.setImageDrawable(dra);
					} else {
						FuXunTools.set_bk(face_str, head);
					}
				}
			TextView name = (TextView) layout.findViewById(R.id.block_name);
			name.setText(contact.getName());
			Button restore = (Button) layout.findViewById(R.id.block_restore);
			restore.setOnClickListener(new View.OnClickListener() {
				public void onClick(View v) {
					index = arg0;
					// if (ExerciseBookTool.isConnect(HomepageAllActivity.this))
					// {
					prodialog = new ProgressDialog(BlockManagementActivity.this);
					prodialog.setMessage("正在恢复...");
					prodialog.setCanceledOnTouchOutside(false);
					prodialog.show();
					Thread thread = new Thread(new BlockContact());
					thread.start();
					// } else {
					// handler.sendEmptyMessage(7);
					// }

					// Toast.makeText(getApplicationContext(), "恢复",
					// Toast.LENGTH_SHORT).show();

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

}
