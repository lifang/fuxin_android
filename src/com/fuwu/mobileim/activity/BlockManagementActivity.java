package com.fuwu.mobileim.activity;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
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
import com.fuwu.mobileim.pojo.ContactPojo;
import com.fuwu.mobileim.util.FxApplication;

public class BlockManagementActivity extends Activity {
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
				// prodialog.dismiss();

				break;

			case 7:
				// Toast.makeText(getApplicationContext(),
				// ExerciseBookParams.INTERNET, Toast.LENGTH_SHORT).show();
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
			if (fxApplication.getContactsList().get(i).isBlocked()) {
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
			// CircularImage head = (CircularImage)
			// layout.findViewById(R.id.block_face);
			TextView name = (TextView) layout.findViewById(R.id.block_name);
			name.setText(contact.getName());
			Button restore = (Button) layout.findViewById(R.id.block_restore);
			restore.setOnClickListener(new View.OnClickListener() {
				public void onClick(View v) {
					final Handler mHandler = new Handler() {
						public void handleMessage(android.os.Message msg) {
							switch (msg.what) {
							case 0:
								final String json8 = (String) msg.obj;
								if (json8.length() != 0) {
									JSONObject array;
									try {
										array = new JSONObject(json8);//
										String status = array
												.getString("status");
										String notice = array
												.getString("notice");
										if ("success".equals(status)) {

											list.remove(arg0);
											// 重新适配数据
										}
										Toast.makeText(getApplicationContext(),
												notice, Toast.LENGTH_SHORT)
												.show();
									} catch (JSONException e) {
										e.printStackTrace();
									}
								}
								break;
							default:
								break;
							}
						}
					};
					final Thread thread = new Thread() {
						public void run() {
							try {
								// Map<String, String> map = new HashMap<String,
								// String>();
								// map.put("reply_micropost_id",
								// child_Micropost.getId());
								// String child_delete_json = ExerciseBookTool
								// .doPost(Urlinterface.DELETE_REPLY_POSTS,
								// map);
								// Message msg = new Message();// 创建Message 对象
								// msg.what = 0;
								// msg.obj = child_delete_json;
								// mHandler.sendMessage(msg);
							} catch (Exception e) {
								handler.sendEmptyMessage(7);
							}
						}
					};

					// if (ExerciseBookTool.isConnect(HomepageAllActivity.this))
					// {
					// thread.start();
					// } else {
					// handler.sendEmptyMessage(7);
					// }

					Toast.makeText(getApplicationContext(), "恢复",
							Toast.LENGTH_SHORT).show();

				}
			});

			layout.setOnClickListener(new View.OnClickListener() {

				@Override
				public void onClick(View v) {
					Intent intent = new Intent(BlockManagementActivity.this,
							BlockManagementDisplayActivity.class);
					intent.putExtra("contactId", contact.getContactId());
					startActivity(intent);
				}
			});
			return layout;
		}
	}

}
