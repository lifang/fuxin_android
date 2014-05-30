package com.fuwu.mobileim.activity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
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
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.fuwu.mobileim.R;
import com.fuwu.mobileim.pojo.TalkPojo;
import com.fuwu.mobileim.util.DBManager;
import com.fuwu.mobileim.util.TimeUtil;
import com.fuwu.mobileim.view.CircularImage;
import com.fuwu.mobileim.view.MyDialog;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;
import com.nostra13.universalimageloader.core.display.RoundedBitmapDisplayer;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;

/**
 * 作者: 张秀楠 时间：2014-5-23 下午4:34:44
 */
public class TalkActivity extends Fragment {
	private ListView mListView;
	private myListViewAdapter clvAdapter;
	private List<TalkPojo> list = new ArrayList<TalkPojo>();
	public Intent intent = new Intent();
	private DBManager db;
	private View rootView;
	private RequstReceiver mReuRequstReceiver;
	private int uid = 1;
	private int contact_id;
	DisplayImageOptions options;
	protected ImageLoader imageLoader = ImageLoader.getInstance();
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
				clvAdapter = new myListViewAdapter(getActivity());
				mListView.setAdapter(clvAdapter);
				break;
			case 3:
				Toast.makeText(getActivity(), "删除失败", Toast.LENGTH_SHORT)
						.show();
				break;
			}
		}
	};

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		rootView = inflater.inflate(R.layout.talk, container, false);
		initData();
		options = new DisplayImageOptions.Builder()
				.showImageOnLoading(R.drawable.test)
				.showImageForEmptyUri(R.drawable.test)
				.showImageOnFail(R.drawable.test).cacheInMemory(true)
				.cacheOnDisk(true).considerExifParams(true)
				.displayer(new RoundedBitmapDisplayer(20)).build();
		mReuRequstReceiver = new RequstReceiver();
		mListView = (ListView) rootView.findViewById(R.id.talk_listview);
		clvAdapter = new myListViewAdapter(getActivity());
		mListView.setAdapter(clvAdapter);
		mListView.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3) {
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
		final MyDialog builder = new MyDialog(getActivity(), 0, 0, view,
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
		private ImageLoadingListener animateFirstListener = new AnimateFirstDisplayListener();

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
			ViewHolder holder;
			if (arg1 == null) {
				arg1 = mInflater.inflate(R.layout.talk_adpter, null);
				holder = new ViewHolder();
				holder.head = (CircularImage) arg1.findViewById(R.id.head);
				holder.statics = (LinearLayout) arg1.findViewById(R.id.statics);
				holder.size = (TextView) arg1.findViewById(R.id.size);
				holder.name = (TextView) arg1.findViewById(R.id.name);
				holder.content = (TextView) arg1.findViewById(R.id.content);
				holder.dath = (TextView) arg1.findViewById(R.id.dath);
				arg1.setTag(holder);
			} else {
				holder = (ViewHolder) arg1.getTag();
			}

			if (list.get(arg0).getMes_count() == 0) {
				holder.statics.setVisibility(View.GONE);
			} else {
				holder.size.setText(list.get(arg0).getMes_count() + "");
				holder.statics.setVisibility(View.VISIBLE);
			}
			String names = list.get(arg0).getNick_name();
			if (names.equals("")) {
				holder.name.setText("暂未设置昵称");
			} else {
				holder.name.setText(names);
			}
			holder.content.setText("嗨你好,嗨再见");
			holder.dath.setText(TimeUtil.getChatTime(list.get(arg0).getTime()));
			imageLoader
					.displayImage(
							"http://www.sinaimg.cn/dy/slidenews/9_img/2012_28/32172_1081661_673195.jpg",
							holder.head, options, animateFirstListener);
			return arg1;

		}
	}

	static final class ViewHolder {
		CircularImage head;
		LinearLayout statics;
		TextView size;
		TextView name;
		TextView content;
		TextView dath;
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
		list = db.queryTalkList(uid);
	}

	public boolean delTalkData() {
		if (!db.isOpen()) {
			db = new DBManager(getActivity());
		}
		return db.delTalk(uid, contact_id);
	}

	public void initData() {
		db = new DBManager(getActivity());
		updateMessageData();
	}

	public void onResume() {
		handler.sendEmptyMessage(2);
//		getActivity().registerReceiver(mReuRequstReceiver,
//				new IntentFilter("com.comdosoft.fuxun.REQUEST_ACTION"));
		super.onResume();
	}

	public void onPause() {
		super.onPause();
//		getActivity().unregisterReceiver(mReuRequstReceiver);
	}

	class RequstReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			handler.sendEmptyMessage(2);
		}
	}

	private static class AnimateFirstDisplayListener extends
			SimpleImageLoadingListener {

		static final List<String> displayedImages = Collections
				.synchronizedList(new LinkedList<String>());

		public void onLoadingComplete(String imageUri, View view,
				Bitmap loadedImage) {
			if (loadedImage != null) {
				ImageView imageView = (ImageView) view;
				boolean firstDisplay = !displayedImages.contains(imageUri);
				if (firstDisplay) {
					FadeInBitmapDisplayer.animate(imageView, 500);
					displayedImages.add(imageUri);
				}
			}
		}
	}
}
