package com.fuwu.mobileim.activity;

import java.util.ArrayList;
import java.util.List;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.fuwu.mobileim.R;
import com.fuwu.mobileim.activity.ChatActivity.chatMessage;
import com.fuwu.mobileim.pojo.TalkPojo;
import com.fuwu.mobileim.util.DBManager;
import com.fuwu.mobileim.util.TimeUtil;
import com.fuwu.mobileim.view.CircularImage;
import com.fuwu.mobileim.view.MyDialog;

/**
 * 作者: 张秀楠 时间：2014-5-23 下午4:34:44
 */
public class TalkActivity extends Activity {
	private ListView mListView;
	private myListViewAdapter clvAdapter;
	private List<TalkPojo> list = new ArrayList<TalkPojo>();
	public Intent intent = new Intent();
	private DBManager db;
	private int uid = 1;
	@SuppressLint("HandlerLeak")
	private Handler handler = new Handler() {
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			switch (msg.what) {
			case 1:
				break;
			case 2:
				updateMessageData();
				Log.i("Max", list.size() + "-");
				clvAdapter = new myListViewAdapter(TalkActivity.this);
				mListView.setAdapter(clvAdapter);
				break;
			case 3:
				Toast.makeText(getApplicationContext(), "删除失败",
						Toast.LENGTH_SHORT).show();
				break;
			}
		}
	};

	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.talk);
		initData();
		mListView = (ListView) findViewById(R.id.talk_listview);
		for (int i = 0; i < 20; i++) {
//			list.add(new ContactPojo("联系人" + i, "5月7日", "最近的一条对话文本记录", "你好啊", 0));
		}
		clvAdapter = new myListViewAdapter(this);
		mListView.setAdapter(clvAdapter);
		mListView.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3) {
				intent.setClass(TalkActivity.this, ChatActivity.class);
				startActivity(intent);
			}
		});

		mListView.setOnItemLongClickListener(new OnItemLongClickListener() {
			public boolean onItemLongClick(AdapterView<?> arg0, View arg1,
					int arg2, long arg3) {
				showLoginDialog(arg2);
				return false;
			}
		});
		Log.i("aa", "onCreate");
		handler.sendEmptyMessage(2);
	}

	private void showLoginDialog(int item) {
		View view = getLayoutInflater().inflate(R.layout.talk_builder, null);
		final TextView btnYes = (TextView) view.findViewById(R.id.name);
		btnYes.setText(list.get(item).getNick_name());
		final TextView del = (TextView) view.findViewById(R.id.del_talk);
		// 设置对话框显示的View
		// 点击确定是的监听
		final MyDialog builder = new MyDialog(TalkActivity.this, 0, 0, view,
				R.style.mydialog);
		del.setOnClickListener(new View.OnClickListener() {
			public void onClick(View arg0) {
				new Thread(new DeleteDB()).start();
				builder.dismiss();
			}
		});
		builder.show();
	}

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

		public View getView(int arg0, View arg1, ViewGroup arg2) {
			if (arg1 == null) {
				arg1 = mInflater.inflate(R.layout.talk_adpter, null);
			}
			CircularImage head = (CircularImage) arg1.findViewById(R.id.head);
			TextView name = (TextView) arg1.findViewById(R.id.name);
			String names = list.get(arg0).getNick_name();
			if (names.equals("")) {
				name.setText("暂未设置昵称");
			} else {
				name.setText(list.get(arg0).getNick_name());
			}
			TextView content = (TextView) arg1.findViewById(R.id.content);
			content.setText(list.get(arg0).getContent());
			TextView dath = (TextView) arg1.findViewById(R.id.dath);
			dath.setText(TimeUtil.getChatTime(list.get(arg0).getTime()));
			return arg1;

		}
	}

	// 获取对话列表
	class DeleteDB implements Runnable {
		public void run() {
			if (delTalkData()) {
				handler.sendEmptyMessage(2);
			} else {
				Log.i("Max", "删除失败");
			}
		}
	}

	public void updateMessageData() {
		if (!db.isOpen()) {
			db = new DBManager(this);
		}
		list = db.queryTalkList(uid);
	}

	public boolean delTalkData() {
		if (!db.isOpen()) {
			db = new DBManager(this);
		}
		return db.delTalk(uid);
	}

	public void initData() {
		db = new DBManager(this);
		updateMessageData();
	}

	protected void onRestart() {
		Log.i("aa", "11111");
		super.onRestart();
	}

	protected void onResume() {
		Log.i("aa", "22222");
		super.onResume();
	}

}
