package com.fuwu.mobileim.activity;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.text.Editable;
import android.text.Html;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextWatcher;
import android.text.method.LinkMovementMethod;
import android.text.style.ImageSpan;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.ProgressBar;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.mobstat.StatService;
import com.fuwu.mobileim.R;
import com.fuwu.mobileim.adapter.FaceAdapter;
import com.fuwu.mobileim.adapter.FacePageAdapter;
import com.fuwu.mobileim.model.Models.Message.ContentType;
import com.fuwu.mobileim.model.Models.Message.ImageType;
import com.fuwu.mobileim.model.Models.MessageConfirmedRequest;
import com.fuwu.mobileim.model.Models.MessageConfirmedResponse;
import com.fuwu.mobileim.model.Models.SendMessageRequest;
import com.fuwu.mobileim.model.Models.SendMessageResponse;
import com.fuwu.mobileim.pojo.MessagePojo;
import com.fuwu.mobileim.pojo.ShortContactPojo;
import com.fuwu.mobileim.pojo.TalkPojo;
import com.fuwu.mobileim.util.ContactCache;
import com.fuwu.mobileim.util.DBManager;
import com.fuwu.mobileim.util.FuXunTools;
import com.fuwu.mobileim.util.FxApplication;
import com.fuwu.mobileim.util.HttpUtil;
import com.fuwu.mobileim.util.ImageCacheUtil;
import com.fuwu.mobileim.util.ImageUtil;
import com.fuwu.mobileim.util.TimeUtil;
import com.fuwu.mobileim.util.Urlinterface;
import com.fuwu.mobileim.view.CirclePageIndicator;
import com.fuwu.mobileim.view.XListView;
import com.fuwu.mobileim.view.XListView.IXListViewListener;
import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;


@SuppressLint("NewApi")
public class LookAllMessageActivity extends Activity implements OnClickListener,
		OnTouchListener,  IXListViewListener {
	private ShortContactPojo cp;
	private int user_id = 1;
	private int contact_id = 1;
	private String token;
	private XListView mListView;
	private List<MessagePojo> list;
	private MessageListViewAdapter mMessageAdapter;
	private DBManager db;
	private SharedPreferences sp;
	private ImageView mBack;
	private TextView mName;
	private Handler handler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			switch (msg.what) {
			case 0:
				Toast.makeText(getApplicationContext(), "请求失败!", 0).show();
			case 6:
				mMessageAdapter.notifyDataSetChanged();
				break;
			case 7:
				// mMessageAdapter.updMessage(mp);
				mListView.setSelection(list.size() - 1);
				break;
			case 8:
				Toast.makeText(getApplicationContext(), "网络异常", 0).show();
				break;
			case 13:
				new Handler().postDelayed(new Runnable() {
					public void run() {
						Intent intent = new Intent(LookAllMessageActivity.this,
								LoginActivity.class);
						startActivity(intent);
						clearActivity();
					}
				}, 3500);
				FuXunTools.initdate(sp, fxApplication);
				Toast.makeText(getApplicationContext(), "您的账号已在其他手机登陆",
						Toast.LENGTH_LONG).show();
				break;
			case 14: // 加载更多消息
//				loadMoreMessageData();
				mMessageAdapter.notifyDataSetChanged();
				mListView.stopRefresh();
				break;
			case 15:
				mMessageAdapter.notifyDataSetChanged();
				mListView.setSelection(list.size() - 1);
				break;
			}
		}
	};
	private FxApplication fxApplication;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.lookallmessage);
		fxApplication = (FxApplication) getApplication();
		fxApplication.getActivityList().add(this);
		initData();
		initView();
	}

	public void initData() {
		db = new DBManager(this);

		sp = getSharedPreferences(Urlinterface.SHARED, Context.MODE_PRIVATE);
		user_id = sp.getInt("user_id", 1);
		contact_id = sp.getInt("contact_id", 1);
		token = sp.getString("Token", "token");
		if (contact_id == 0) {
			cp = new ShortContactPojo(0, "", "系统消息", "系统消息", "", 2, 0, "", 0,
					"", "");
		} else {
			cp = db.queryContact(user_id, contact_id);
		}

		updateMessageData();
	}

	// 获取最近的 15条消息
	public void updateMessageData() {

		db.clearTalkMesCount(user_id, contact_id);
		list = db.queryMessageList(user_id, contact_id,
				0, 100);
	}

	
	public void initView() {
		mListView = (XListView) findViewById(R.id.chat_listView);
		mBack = (ImageView) findViewById(R.id.chat_back);
		mListView.setXListViewListener(this);
		mBack.setOnClickListener(this);
		mBack.setOnTouchListener(this);
		mMessageAdapter = new MessageListViewAdapter(getResources(), this);
		mListView.setAdapter(mMessageAdapter);
		mListView.setOnTouchListener(this);
		mListView.setSelection(list.size() - 1);
		mName = (TextView) findViewById(R.id.chat_name);
		mName.setText("所有消息");
	}

	


	public String getContactName() {
		if (cp.getCustomName() != null && cp.getCustomName().length()!=0) {
			return cp.getCustomName();
		}
		return cp.getName();
	}




	// 防止乱pageview乱滚动
	private OnTouchListener forbidenScroll() {
		return new OnTouchListener() {
			public boolean onTouch(View v, MotionEvent event) {
				if (event.getAction() == MotionEvent.ACTION_MOVE) {
					return true;
				}
				return false;
			}
		};
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {

		case R.id.chat_back:
			ContactCache.flag = false;
			this.finish();
			break;
		}
	}

	@Override
	public boolean onTouch(View v, MotionEvent event) {
		if (event.getAction() == MotionEvent.ACTION_DOWN) {
			switch (v.getId()) {
			case R.id.chat_back:
				findViewById(R.id.chat_back).getBackground().setAlpha(70);
				break;
			}
		} else if (event.getAction() == MotionEvent.ACTION_UP) {
			switch (v.getId()) {
			case R.id.chat_back:
				findViewById(R.id.chat_back).getBackground().setAlpha(255);
				break;
			}
		}
		return false;
	}



	@Override
	protected void onStart() {
		super.onStart();
		sp = getSharedPreferences(Urlinterface.SHARED, Context.MODE_PRIVATE);
		user_id = sp.getInt("user_id", 1);
		contact_id = sp.getInt("contact_id", 1);
		token = sp.getString("Token", "token");
	}

	@Override
	protected void onResume() {
		super.onResume();
		if (!db.isOpen()) {
			db = new DBManager(this);
		}
		StatService.onResume(this);
	}

	@Override
	protected void onPause() {
		super.onPause();
		StatService.onPause(this);
	}


	@Override
	protected void onDestroy() {
		super.onDestroy();
		if (db != null) {
			db.closeDB();
		}
		mMessageAdapter.closeDB();
	}

	@Override
	public void onRefresh() {

		handler.sendEmptyMessage(14);
	}


	// 关闭界面
	public void clearActivity() {
		List<Activity> activityList = fxApplication.getActivityList();
		for (int i = 0; i < activityList.size(); i++) {
			activityList.get(i).finish();
		}
		fxApplication.setActivityList();
	}

	public class MessageListViewAdapter extends BaseAdapter {
		public String time_sign; // 时间标志
		public String time_sign2; // 时间标志
		public String customName;
		public final Pattern EMOTION_URL = Pattern.compile("\\[(\\S+?)\\]");
		private Context mContext;
		private Resources res;
		private LayoutInflater mInflater;
		private DBManager db;
		private File f;
		private ProgressDialog pd;
		public List timearr = new ArrayList<String>();

		public MessageListViewAdapter(Resources res, Context mContext) {
			super();
			this.mContext = mContext;
			this.res = res;
			this.mInflater = LayoutInflater.from(mContext);
			db = new DBManager(mContext);
			pd = new ProgressDialog(mContext);
			pd.setMessage("正在加载详细信息...");
			if (list.size() != 0) {
				time_sign2 = list.get(0).getSendTime();
				timearr.add(list.get(0).getSendTime());
			}
			for (int i = 1; i < list.size(); i++) {

				if (TimeUtil.isFiveMin(time_sign2, list.get(i).getSendTime())) {
					timearr.add(list.get(i).getSendTime());
					time_sign2 = list.get(i).getSendTime();
				} else {
					timearr.add("");
				}

			}
		}

		@Override
		public void notifyDataSetChanged() {
			// TODO Auto-generated method stub
			timearr = new ArrayList<String>();
			if (list.size() != 0) {
				time_sign2 = list.get(0).getSendTime();
				timearr.add(list.get(0).getSendTime());
			}
			for (int i = 1; i < list.size(); i++) {

				if (TimeUtil.isFiveMin(time_sign2, list.get(i).getSendTime())) {
					timearr.add(list.get(i).getSendTime());
					time_sign2 = list.get(i).getSendTime();
				} else {
					timearr.add("");
				}

			}

			super.notifyDataSetChanged();
		}

		public void updMessage(MessagePojo cp) {
			list.add(cp);
			notifyDataSetChanged();
		}

		public void closeDB() {
			if (db != null) {
				db.closeDB();
			}
		}

		@Override
		public int getCount() {
			return list.size();
		}

		@Override
		public Object getItem(int arg0) {
			return null;
		}

		@Override
		public long getItemId(int arg0) {
			return 0;
		}

		@Override
		public View getView(final int position, View convertView,
				ViewGroup parent) {
			final MessagePojo mp = list.get(position);
			if (position == 0) {
				time_sign = "";
			}

			ViewHolder holder = null;
			if (convertView == null
					|| convertView.getTag(R.drawable.ic_launcher + position) == null) {
				holder = new ViewHolder();
				if (mp.getIsComMeg() == 0) { // 别人发的消息
					convertView = mInflater.inflate(R.layout.chat_item_left,
							null);
				} else {// 自己发的消息
					convertView = mInflater.inflate(R.layout.chat_item_right,
							null);
					holder.load = (ProgressBar) convertView
							.findViewById(R.id.loadingcircle);
					holder.sendFail = (ImageView) convertView
							.findViewById(R.id.sendfail);
				}
				holder.time = (TextView) convertView
						.findViewById(R.id.chat_datetime);
				holder.mes = (TextView) convertView
						.findViewById(R.id.chat_textView2);
				holder.img = (ImageView) convertView
						.findViewById(R.id.chat_icon);
				holder.sendImg = (ImageView) convertView
						.findViewById(R.id.chat_img);
				convertView.setTag(R.drawable.ic_launcher + position);
			} else {
				holder = (ViewHolder) convertView.getTag(R.drawable.ic_launcher
						+ position);
			}

			holder.time.setText(TimeUtil.getTalkTime(mp.getSendTime()));
			if (timearr.get(position).equals("")) {
				holder.time.setVisibility(View.GONE);
			} else {
				holder.time.setVisibility(View.VISIBLE);
			}
			if (mp.getIsComMeg() == 0) { // 别人发送的消息
				if (mp.getContactId() == 0) {
					holder.img.setImageResource(R.drawable.system_user_face);
				} else {
					f = new File(Urlinterface.head_pic, cp.getContactId() + "");
					if (f.exists()) {
						holder.img.setTag(Urlinterface.head_pic
								+ cp.getContactId());
						if (!ImageCacheUtil.IMAGE_CACHE.get(
								Urlinterface.head_pic + cp.getContactId(),
								holder.img)) {
							holder.img.setImageDrawable(null);
						}
					} else {
						holder.img.setImageResource(R.drawable.moren);
					}
				}
				// ImageCacheUtil.IMAGE_CACHE.get(
				// Urlinterface.head_pic + cp.getContactId(), holder.img);
				holder.img.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						Intent i = new Intent();
						i.setClass(mContext, ContactInfoActivity.class);
						mContext.startActivity(i);
					}
				});
			} else { // 自己
				if (mp.getStatus() == 0) {
					holder.load.setVisibility(View.GONE);
				} else if (mp.getStatus() == 2) {
					holder.load.setVisibility(View.GONE);
					holder.sendFail.setVisibility(View.VISIBLE);
					holder.sendFail.setOnClickListener(new OnClickListener() {
						@Override
						public void onClick(View v) {
//							sendAgain_postion = position;
//							sendAgain_method(sendAgain_postion);
						}
					});
				}

				f = new File(Urlinterface.head_pic, user_id + "");
				if (f.exists()) {
					holder.img.setTag(Urlinterface.head_pic + user_id);
					if (!ImageCacheUtil.IMAGE_CACHE.get(Urlinterface.head_pic
							+ user_id, holder.img)) {
						holder.img.setImageDrawable(null);
					}
				} else {
					holder.img.setImageResource(R.drawable.moren);
				}
				// ImageCacheUtil.IMAGE_CACHE.get(Urlinterface.head_pic +
				// user_id,
				// holder.img);
			}
			if (mp.getMsgType() == 1) {
				holder.mes.setText(convertNormalStringToSpannableString(mp
						.getContent()));
			} else if (mp.getMsgType() == 2) {
				holder.sendImg.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						Intent intent = new Intent();
						intent.setClass(mContext, ZoomImageActivity.class);
						intent.putExtra("image_path",
								Urlinterface.SDCARD + mp.getContent());
						mContext.startActivity(intent);
					}
				});
				holder.mes.setVisibility(View.GONE);
				holder.sendImg.setVisibility(View.VISIBLE);
				holder.sendImg.setImageBitmap(ImageUtil.createImageThumbnail(
						Urlinterface.SDCARD + mp.getContent(), 720));
			} else if (mp.getMsgType() == 3) {
				String str = mp.getContent();
				if (str.indexOf("\"www") != -1) {
					str = str.replaceAll("\"www", "\"http://www");
				}
				holder.mes.setText(Html.fromHtml(str));
				holder.mes.setMovementMethod(LinkMovementMethod.getInstance());
			}
			return convertView;
		}

		public class ViewHolder {
			public TextView time;
			public TextView mes;
			public TextView order;
			public ImageView img;
			public ImageView sendImg;
			public ProgressBar load;
			public ImageView sendFail;
		}

		private CharSequence convertNormalStringToSpannableString(String message) {
			String hackTxt;
			if (message.startsWith("[") && message.endsWith("]")) {
				hackTxt = message + " ";
			} else {
				hackTxt = message;
			}
			SpannableString value = SpannableString.valueOf(hackTxt);

			Matcher localMatcher = EMOTION_URL.matcher(value);
			while (localMatcher.find()) {
				String str2 = localMatcher.group(0);
				int k = localMatcher.start();
				int m = localMatcher.end();
				if (m - k < 8) {
					if (FxApplication.getInstance().getFaceMap()
							.containsKey(str2)) {
						int face = FxApplication.getInstance().getFaceMap()
								.get(str2);
						Bitmap bitmap = BitmapFactory.decodeResource(res, face);
						if (bitmap != null) {
							ImageSpan localImageSpan = new ImageSpan(mContext,
									bitmap, ImageSpan.ALIGN_BASELINE);
							value.setSpan(localImageSpan, k, m,
									Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
						}
					}
				}
			}
			return value;
		}

	}

	

	public String getTimeStamp() {
		String time = sp.getString("sendTime", "");
		if (time != null && !time.equals("")) {
			return time;
		}
		return "";
	}
}
