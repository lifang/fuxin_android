package com.fuwu.mobileim.activity;

import java.util.ArrayList;
import java.util.List;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.fuwu.mobileim.R;
import com.fuwu.mobileim.adapter.TalkListViewAdapter;
import com.fuwu.mobileim.model.Models.ChangeContactDetailRequest;
import com.fuwu.mobileim.model.Models.ChangeContactDetailResponse;
import com.fuwu.mobileim.model.Models.Contact;
import com.fuwu.mobileim.pojo.TalkPojo;
import com.fuwu.mobileim.util.DBManager;
import com.fuwu.mobileim.util.FxApplication;
import com.fuwu.mobileim.util.HttpUtil;
import com.fuwu.mobileim.util.Urlinterface;
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
	private int uid;
	private String token;
	private String CustomName;
	@SuppressLint("HandlerLeak")
	private SharedPreferences sp;
	private Handler handler = new Handler() {
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			switch (msg.what) {
			case 1:
				break;
			case 2:
				updateTalkData();
				clvAdapter.updateList(list);
				break;
			case 3:
				Toast.makeText(getActivity(), "删除失败", Toast.LENGTH_SHORT)
						.show();
				break;
			case 4:
				clvAdapter.notifyDataSetChanged();
				break;
			case 5:
				Toast.makeText(getActivity(), "修改备注成功", Toast.LENGTH_SHORT)
						.show();
				db.updateContactRem(uid, contact_id, CustomName);
				handler.sendEmptyMessage(2);
				break;
			case 6:
				Toast.makeText(getActivity(), "修改备注失败", Toast.LENGTH_SHORT)
						.show();
				break;
			}
		}
	};

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		rootView = inflater.inflate(R.layout.talk, container, false);
		fx = (FxApplication) getActivity().getApplication();
		sp = getActivity().getSharedPreferences(Urlinterface.SHARED, 0);
		mListView = (ListView) rootView.findViewById(R.id.talk_listview);
		mListView.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3) {
				db.clearTalkMesCount(fx.getUser_id(), fx.getUser_id());
				sp.edit()
						.putString("contact_id",
								list.get(arg2).getContact_id() + "").commit();
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
		clvAdapter = new TalkListViewAdapter(getActivity(), list, fx.options);
		mListView.setAdapter(clvAdapter);
		return rootView;
	}

	private void showLoginDialog(int item) {
		View view = getActivity().getLayoutInflater().inflate(
				R.layout.talk_builder, null);
		final TextView btnYes = (TextView) view.findViewById(R.id.name);
		btnYes.setText(list.get(item).getNick_name());
		final TextView del = (TextView) view.findViewById(R.id.del_talk);
		final EditText nickname = (EditText) view
				.findViewById(R.id.info_nickname);
		nickname.setText(list.get(item).getNick_name());
		Button ok = (Button) view.findViewById(R.id.info_nickname_ok);
		ok.setOnClickListener(new View.OnClickListener() {
			public void onClick(View arg0) {
				CustomName = nickname.getText().toString();
				new UpdateContactRem().start();
			}
		});
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

	class UpdateContactRem extends Thread {
		public void run() {
			try {
				ChangeContactDetailRequest.Builder builder = ChangeContactDetailRequest
						.newBuilder();
				builder.setUserId(uid);
				builder.setToken(token);
				Contact.Builder cb = Contact.newBuilder();
				cb.setContactId(contact_id);
				cb.setCustomName(CustomName);
				builder.setContact(cb);
				ChangeContactDetailRequest response = builder.build();
				byte[] by = HttpUtil.sendHttps(response.toByteArray(),
						Urlinterface.ContactDetail, "PUT");
				if (by != null && by.length > 0) {
					ChangeContactDetailResponse res = ChangeContactDetailResponse
							.parseFrom(by);
					if (res.getIsSucceed()) {
						handler.sendEmptyMessage(5);
					} else {
						handler.sendEmptyMessage(6);
					}
				} else {
					handler.sendEmptyMessage(2);
				}
			} catch (Exception e) {
			}
		}
	}

	// 获取对话列表
	class DeleteDB implements Runnable {
		public void run() {
			if (delTalkData()) {
				handler.sendEmptyMessage(2);
			} else {
				handler.sendEmptyMessage(3);
			}
		}
	}

	public void updateTalkData() {
		if (!db.isOpen()) {
			db = new DBManager(getActivity());
		}
		list = db.queryTalkList(uid);
	}

	public boolean delTalkData() {
		if (!db.isOpen()) {
			db = new DBManager(getActivity());
		}
		return db.delTalk(fx.getUser_id(), contact_id);
	}

	public void onResume() {
		super.onResume();
	}

	public void onPause() {
		super.onPause();
	}

	public void onStart() {
		uid = sp.getInt("user_id", 0);
		token = sp.getString("Token", "");
		handler.sendEmptyMessage(2);
		super.onStart();
	}

}
