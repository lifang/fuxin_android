package com.fuwu.mobileim.activity;

import java.util.ArrayList;
import java.util.List;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
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
import com.fuwu.mobileim.util.FuXunTools;
import com.fuwu.mobileim.util.FxApplication;
import com.fuwu.mobileim.util.HttpUtil;
import com.fuwu.mobileim.util.PushReceiver;
import com.fuwu.mobileim.util.Urlinterface;
import com.fuwu.mobileim.view.MyDialog;
import com.igexin.sdk.PushConsts;

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
	private int uid;
	private String token;
	private String CustomName;
	private MyDialog builder;
	private ProgressDialog prodialog;
	private FxApplication fx;
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
				prodialog.dismiss();
				Toast.makeText(getActivity(), "修改备注成功", Toast.LENGTH_SHORT)
						.show();
				db.updateContactRem(uid, contact_id, CustomName);
				builder.dismiss();
				handler.sendEmptyMessage(2);
				break;
			case 6:
				Toast.makeText(getActivity(), "修改备注失败", Toast.LENGTH_SHORT)
						.show();
				break;
			case 9:
				prodialog.dismiss();
				new Handler().postDelayed(new Runnable() {
					public void run() {
						Intent intent = new Intent(
								getActivity(),
								LoginActivity.class);
						startActivity(intent);
						clearActivity();
					}
				}, 3500);
				sp
						.edit()
						.putInt("exit_user_id",
								sp.getInt("user_id", 0)).commit();
				sp
						.edit()
						.putString("exit_Token",
								sp.getString("Token", "null"))
						.commit();
				sp
						.edit()
						.putString("exit_clientid",
								sp.getString("clientid", "")).commit();
				sp.edit().putInt("user_id", 0).commit();
				sp.edit().putString("Token", "null").commit();
				sp.edit().putString("pwd", "").commit();
				sp.edit().putString("clientid", "").commit();
				sp.edit().putString("profile_user", "").commit();
				fx.initData();
				Toast.makeText(getActivity(), "您的账号已在其他手机登陆",
						Toast.LENGTH_LONG).show();
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
		uid = sp.getInt("user_id", 0);
		token = sp.getString("Token", "");
		db = new DBManager(getActivity());
		mListView = (ListView) rootView.findViewById(R.id.talk_listview);
		mListView.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3) {
				Log.i("Max", "uid->" + uid + "    contact_id->"
						+ list.get(arg2).getContact_id());
				sp.edit().putInt("contact_id", list.get(arg2).getContact_id())
				.commit();
				Intent intent = new Intent(getActivity(),PushReceiver.class);
				intent.putExtra(PushConsts.CMD_ACTION, Urlinterface.Receiver_code);
				getActivity().sendBroadcast(intent); 
				
				db.clearTalkMesCount(uid, list.get(arg2).getContact_id());
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
				prodialog = new ProgressDialog(getActivity());
				prodialog.setMessage("努力修改中..");
				prodialog.setCanceledOnTouchOutside(false);
				prodialog.show();
				CustomName = nickname.getText().toString();
				new UpdateContactRem().start();
			}
		});
		// 设置对话框显示的View
		// 点击确定是的监听
		builder = new MyDialog(getActivity(), 0, view, R.style.mydialog);
		del.setOnClickListener(new View.OnClickListener() {
			public void onClick(View arg0) {
				new Thread(new DeleteDB()).start();
				builder.dismiss();
			}
		});
		builder.setCanceledOnTouchOutside(true);// 点击其他区域不消失
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
						int ErrorCode = res.getErrorCode().getNumber();
						if (ErrorCode == 2001) {
							handler.sendEmptyMessage(9);
						} else {
							handler.sendEmptyMessage(6);}
						
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
		return db.delTalk(uid, contact_id);
	}

	public void onStart() {
		handler.sendEmptyMessage(2);
		super.onStart();
	}

	public void onDestroy() {
		super.onDestroy();
		if (db != null) {
			db.closeDB();
		}
	}
	// 关闭界面
		public void clearActivity() {
			List<Activity> activityList = fx.getActivityList();
			for (int i = 0; i < activityList.size(); i++) {
				activityList.get(i).finish();
			}
			fx.setActivityList();
		}
}
