package com.fuwu.mobileim.activity;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.fuwu.mobileim.R;
import com.fuwu.mobileim.adapter.TalkListViewAdapter;
import com.fuwu.mobileim.pojo.TalkPojo;
import com.fuwu.mobileim.util.DBManager;
import com.fuwu.mobileim.util.FxApplication;
import com.fuwu.mobileim.view.MyDialog;

/**
 * 作者: 张秀楠 时间：2014-5-23 下午4:34:44
 */
public class TalkActivity extends Fragment {
	private ListView mListView;
	private TalkListViewAdapter clvAdapter;
	private List<TalkPojo> list = new ArrayList<TalkPojo>();
	public Intent intent = new Intent();
	private DBManager db;
	private View rootView;
	private int contact_id;
	private FxApplication fx;
	@SuppressLint("HandlerLeak")
	private Handler handler = new Handler() {
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			switch (msg.what) {
			case 1:
				break;
			case 2:
				updateMessageData();
				clvAdapter = new TalkListViewAdapter(getActivity(), list,
						fx.options);
				Log.i("Max", list.size() + "-");
				mListView.setAdapter(clvAdapter);
//				Log.i("FuWu", list.get(0).toString());
				break;
			case 3:
				Toast.makeText(getActivity(), "删除失败", Toast.LENGTH_SHORT)
						.show();
				break;
			case 4:
				clvAdapter.notifyDataSetChanged();
				break;
			}
		}
	};

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		rootView = inflater.inflate(R.layout.talk, container, false);
		fx = (FxApplication) getActivity().getApplication();
		initData();

		mListView = (ListView) rootView.findViewById(R.id.talk_listview);
		mListView.setAdapter(clvAdapter);
		mListView.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3) {
				intent.putExtra("contact_id", list.get(arg2).getContact_id());
				intent.setClass(getActivity(), ChatActivity.class);
				startActivity(intent);
			}
		});

		mListView.setOnItemLongClickListener(new OnItemLongClickListener() {
			public boolean onItemLongClick(AdapterView<?> arg0, View arg1,
					int arg2, long arg3) {
				contact_id = list.get(arg2).getContact_id();
				showLoginDialog(arg2);
				return false;
			}
		});
		Log.i("aa", "onCreate");
		File file = new File(Environment.getExternalStorageDirectory()
				+ "/test.jpg");
		if (file.exists()) {
			Log.i("Max", "存在");
		}
		// handler.sendEmptyMessage(2);
		return rootView;
	}

	private void showLoginDialog(int item) {
		View view = getActivity().getLayoutInflater().inflate(
				R.layout.talk_builder, null);
		final TextView btnYes = (TextView) view.findViewById(R.id.name);
		btnYes.setText(list.get(item).getNick_name());
		final TextView del = (TextView) view.findViewById(R.id.del_talk);
		// 设置对话框显示的View
		// 点击确定是的监听
		final MyDialog builder = new MyDialog(getActivity(), 0, view,
				R.style.mydialog);
		del.setOnClickListener(new View.OnClickListener() {
			public void onClick(View arg0) {
				new Thread(new DeleteDB()).start();
				builder.dismiss();
			}
		});
		builder.show();
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
			db = new DBManager(getActivity());
		}
		list = db.queryTalkList(fx.getUser_id());
	}

	public boolean delTalkData() {
		if (!db.isOpen()) {
			db = new DBManager(getActivity());
		}
		return db.delTalk(fx.getUser_id(), contact_id);
	}

	public void initData() {
		db = new DBManager(getActivity());
		updateMessageData();
	}

	public void onResume() {
		super.onResume();
	}

	public void onPause() {
		super.onPause();
	}

	public void onStart() {
		Log.i("Max", "onStart");
		Log.i("Max", "刷新");
		handler.sendEmptyMessage(2);
		super.onStart();
	}

}
