package com.fuwu.mobileim.activity;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.text.Editable;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextWatcher;
import android.text.style.ImageSpan;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup.LayoutParams;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import com.fuwu.mobileim.R;
import com.fuwu.mobileim.adapter.FaceAdapter;
import com.fuwu.mobileim.adapter.FacePageAdapter;
import com.fuwu.mobileim.adapter.MessageListViewAdapter;
import com.fuwu.mobileim.model.Models.SendMessageRequest;
import com.fuwu.mobileim.model.Models.SendMessageResponse;
import com.fuwu.mobileim.pojo.MessagePojo;
import com.fuwu.mobileim.util.DBManager;
import com.fuwu.mobileim.util.FxApplication;
import com.fuwu.mobileim.util.HttpUtil;
import com.fuwu.mobileim.util.TimeUtil;
import com.fuwu.mobileim.view.CirclePageIndicator;
import com.google.protobuf.InvalidProtocolBufferException;

/**
 * @作者 马龙
 * @时间 创建时间：2014-5-15 下午4:41:29
 */
@SuppressLint("NewApi")
public class ChatActivity extends Activity implements OnClickListener,
		OnTouchListener, TextWatcher {

	private String[] itemName = new String[] { "图片", "拍摄" };
	private int[] itemImg = new int[] { R.drawable.pic, R.drawable.camera };
	private float height = 0;
	private int currentPage = 0;
	private boolean isFaceShow = false;;
	private boolean isPlusShow = false;;
	private ListView mListView;
	private ViewPager faceViewPager;
	private LinearLayout faceLinearLayout;
	private GridView mPlusGridView;
	private Button mSendBtn;
	private ImageView mBack;
	private ImageView mOther;
	private ImageView mFaceBtn;
	private ImageView mPlusBtn;
	private EditText msgEt;
	private List<String> keys;
	private List<MessagePojo> list;
	private MessageListViewAdapter mMessageAdapter;
	private ProgressDialog pd;
	private DBManager db;
	private RequstReceiver mReuRequstReceiver;
	private Handler handler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			switch (msg.what) {
			case 1:
				break;
			case 2:
				Toast.makeText(getApplicationContext(), "更新记录", 0).show();
				updateMessageData();
				mMessageAdapter.updMessageList(list);
				break;
			}
			// pd.dismiss();
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.chat);
		initData();
		initView();
		initFacePage();
		initPlusGridView();
		// pd = new ProgressDialog(this);
		// pd.setMessage("正在加载");
		// pd.show();
		mReuRequstReceiver = new RequstReceiver();

	}

	class chatMessage extends Thread {
		public void run() {
			super.run();
			handler.sendEmptyMessage(1);
		}
	}

	public void initData() {
		db = new DBManager(this);
		DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
		height = displayMetrics.heightPixels;
		Set<String> keySet = FxApplication.getInstance().getFaceMap().keySet();
		keys = new ArrayList<String>();
		keys.addAll(keySet);
		updateMessageData();
	}

	public void updateMessageData() {
		if (!db.isOpen()) {
			db = new DBManager(this);
		}
		list = db.queryMessageList(1, 2);
	}

	public void initView() {
		mListView = (ListView) findViewById(R.id.chat_listView);
		faceLinearLayout = (LinearLayout) findViewById(R.id.face_ll);
		mPlusGridView = (GridView) findViewById(R.id.chat_plus_panel);
		mFaceBtn = (ImageView) findViewById(R.id.face_btn);
		mSendBtn = (Button) findViewById(R.id.send_btn);
		mBack = (ImageView) findViewById(R.id.chat_back);
		mOther = (ImageView) findViewById(R.id.chat_other);
		msgEt = (EditText) findViewById(R.id.msg_et);
		faceViewPager = (ViewPager) findViewById(R.id.face_pager);
		mPlusBtn = (ImageView) findViewById(R.id.plus_btn);
		mFaceBtn.setOnClickListener(this);
		mPlusBtn.setOnClickListener(this);
		mSendBtn.setOnClickListener(this);
		msgEt.setOnClickListener(this);
		msgEt.addTextChangedListener(this);
		mBack.setImageBitmap(getBitmapScale(BitmapFactory.decodeResource(
				getResources(), R.drawable.back)));
		mOther.setImageBitmap(getBitmapScale(BitmapFactory.decodeResource(
				getResources(), R.drawable.other)));
		mFaceBtn.setImageBitmap(getBitmapScale(BitmapFactory.decodeResource(
				getResources(), R.drawable.face)));
		mFaceBtn.setOnTouchListener(new OnTouchListener() {
			// 点击变色
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				if (event.getAction() == MotionEvent.ACTION_DOWN) {
					mFaceBtn.setImageBitmap(getBitmapScale(BitmapFactory
							.decodeResource(getResources(),
									R.drawable.face_hover)));
				} else if (event.getAction() == MotionEvent.ACTION_UP) {
					mFaceBtn.setImageBitmap(getBitmapScale(BitmapFactory
							.decodeResource(getResources(), R.drawable.face)));
				}
				return false;
			}
		});
		mPlusBtn.setImageBitmap(getBitmapScale(BitmapFactory.decodeResource(
				getResources(), R.drawable.plus)));
		mPlusBtn.setOnTouchListener(new OnTouchListener() {
			// 点击变色
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				if (event.getAction() == MotionEvent.ACTION_DOWN) {
					mPlusBtn.setImageBitmap(getBitmapScale(BitmapFactory
							.decodeResource(getResources(),
									R.drawable.plus_hover)));
				} else if (event.getAction() == MotionEvent.ACTION_UP) {
					mPlusBtn.setImageBitmap(getBitmapScale(BitmapFactory
							.decodeResource(getResources(), R.drawable.plus)));
				}
				return false;
			}
		});

		mMessageAdapter = new MessageListViewAdapter(getResources(), this, list);
		mListView.setAdapter(mMessageAdapter);
		mListView.setOnTouchListener(this);
		mListView.setSelection(list.size() - 1);
	}

	private void initFacePage() {
		// TODO Auto-generated method stub
		List<View> lv = new ArrayList<View>();
		for (int i = 0; i < FxApplication.NUM_PAGE; ++i)
			lv.add(getGridView(i));
		FacePageAdapter adapter = new FacePageAdapter(lv, faceViewPager);
		faceViewPager.setAdapter(adapter);
		faceViewPager.setCurrentItem(currentPage);
		CirclePageIndicator indicator = (CirclePageIndicator) findViewById(R.id.indicator);
		indicator.setViewPager(faceViewPager);
		adapter.notifyDataSetChanged();
		faceLinearLayout.setVisibility(View.GONE);
		indicator.setOnPageChangeListener(new OnPageChangeListener() {
			@Override
			public void onPageSelected(int arg0) {
				currentPage = arg0;
			}

			@Override
			public void onPageScrolled(int arg0, float arg1, int arg2) {
			}

			@Override
			public void onPageScrollStateChanged(int arg0) {
			}
		});
	}

	private GridView getGridView(int i) {
		GridView gv = new GridView(this);
		gv.setNumColumns(7);
		gv.setSelector(new ColorDrawable(Color.TRANSPARENT));// 屏蔽GridView默认点击效果
		gv.setBackgroundColor(Color.rgb(250, 250, 250));
		gv.setCacheColorHint(Color.rgb(250, 250, 250));
		gv.setHorizontalSpacing(1);
		gv.setVerticalSpacing(1);
		gv.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT,
				LayoutParams.MATCH_PARENT));
		gv.setGravity(Gravity.CENTER);
		gv.setAdapter(new FaceAdapter(this, i));
		gv.setOnTouchListener(forbidenScroll());
		gv.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3) {
				// TODO Auto-generated method stub
				if (arg2 == 20) {// 删除键的位置
					int selection = msgEt.getSelectionStart();
					String text = msgEt.getText().toString();
					if (selection > 0) {
						String text2 = text.substring(selection - 1);
						if ("]".equals(text2)) {
							int start = text.lastIndexOf("[");
							int end = selection;
							msgEt.getText().delete(start, end);
							return;
						}
						msgEt.getText().delete(selection - 1, selection);
					}
				} else {
					int count = currentPage * FxApplication.NUM + arg2;
					// 下面这部分，在EditText中显示表情
					Bitmap bitmap = BitmapFactory.decodeResource(
							getResources(), (Integer) FxApplication
									.getInstance().getFaceMap().values()
									.toArray()[count]);
					if (bitmap != null) {
						int rawHeigh = bitmap.getHeight();
						int rawWidth = bitmap.getHeight();
						int newHeight = 40;
						int newWidth = 40;
						// 计算缩放因子
						float heightScale = ((float) newHeight) / rawHeigh;
						float widthScale = ((float) newWidth) / rawWidth;
						// 新建立矩阵
						Matrix matrix = new Matrix();
						matrix.postScale(heightScale, widthScale);
						// 设置图片的旋转角度
						// matrix.postRotate(-30);
						// 设置图片的倾斜
						// matrix.postSkew(0.1f, 0.1f);
						// 将图片大小压缩
						// 压缩后图片的宽和高以及kB大小均会变化
						Bitmap newBitmap = Bitmap.createBitmap(bitmap, 0, 0,
								rawWidth, rawHeigh, matrix, true);
						ImageSpan imageSpan = new ImageSpan(ChatActivity.this,
								newBitmap);
						String emojiStr = keys.get(count);
						SpannableString spannableString = new SpannableString(
								emojiStr);
						spannableString.setSpan(imageSpan,
								emojiStr.indexOf('['),
								emojiStr.indexOf(']') + 1,
								Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
						msgEt.append(spannableString);
					} else {
						String ori = msgEt.getText().toString();
						int index = msgEt.getSelectionStart();
						StringBuilder stringBuilder = new StringBuilder(ori);
						stringBuilder.insert(index, keys.get(count));
						msgEt.setText(stringBuilder.toString());
						msgEt.setSelection(index + keys.get(count).length());
					}
				}
			}
		});
		return gv;
	}

	private void initPlusGridView() {
		mPlusGridView.setNumColumns(4);
		// mPlusGridView.setSelector(new ColorDrawable(Color.TRANSPARENT));//
		// 屏蔽GridView默认点击效果
		// mPlusGridView.setBackgroundColor(Color.TRANSPARENT);
		mPlusGridView.setCacheColorHint(Color.TRANSPARENT);
		mPlusGridView.setHorizontalSpacing(1);
		mPlusGridView.setVerticalSpacing(1);
		mPlusGridView.setGravity(Gravity.CENTER);
		ArrayList<HashMap<String, Object>> item = new ArrayList<HashMap<String, Object>>();
		for (int i = 0; i < itemImg.length; i++) {
			HashMap<String, Object> map = new HashMap<String, Object>();
			map.put("itemImage", itemImg[i]);
			map.put("itemName", itemName[i]);
			item.add(map);
		}
		SimpleAdapter simpleAdapter = new SimpleAdapter(this, item,
				R.layout.chat_plus_item,
				new String[] { "itemImage", "itemName" }, new int[] {
						R.id.plus_img, R.id.plus_tv }) {
		};
		mPlusGridView.setAdapter(simpleAdapter);
		mPlusGridView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
			}
		});
	}

	public void HideKeyboard() {
		InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
		imm.hideSoftInputFromWindow(msgEt.getWindowToken(), 0); // 强制隐藏键盘
	}

	public Bitmap getBitmapScale(Bitmap bmp) {
		int bmpWidth = bmp.getWidth();

		int bmpHeight = bmp.getHeight();

		// 缩放图片的尺寸

		float scale = (height / 1920f) * 100;

		float scaleWidth = (float) scale / bmpWidth; // 按固定大小缩放 sWidth 写多大就多大

		float scaleHeight = (float) scale / bmpHeight;

		Matrix matrix = new Matrix();

		matrix.postScale(scaleWidth, scaleHeight);// 产生缩放后的Bitmap对象

		Bitmap resizeBitmap = Bitmap.createBitmap(bmp, 0, 0, bmpWidth,
				bmpHeight, matrix, false);

		bmp.recycle();

		return resizeBitmap;
	}

	class SendMessageThread extends Thread {
		@Override
		public void run() {
			super.run();
			try {
				SendMessageRequest.Builder builder = SendMessageRequest
						.newBuilder();
				builder.setToken("MockToken");
				builder.setUserId(1);
				com.fuwu.mobileim.model.Models.Message.Builder mes = com.fuwu.mobileim.model.Models.Message
						.newBuilder();
				mes.setContactId(2);
				mes.setUserId(1);
				// mes.setSendTime("2014-05-27 14:07:30");
				mes.setContent("你好");
				builder.setMessage(mes);
				SendMessageRequest smr = builder.build();
				SendMessageResponse response = SendMessageResponse
						.parseFrom(HttpUtil.sendHttps(smr.toByteArray(),
								"https://118.242.18.189/api/Message", "PUT"));
				Log.i("Ax",
						response.getIsSucceed() + "--"
								+ response.getErrorCode());
			} catch (InvalidProtocolBufferException e) {
				e.printStackTrace();
			}
		}
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
		case R.id.face_btn:
			if (!isFaceShow) {
				if (isPlusShow) {
					isPlusShow = false;
					mPlusGridView.setVisibility(View.GONE);
				}
				HideKeyboard();
				isFaceShow = true;
				faceLinearLayout.setVisibility(View.VISIBLE);
			} else {
				isFaceShow = false;
				faceLinearLayout.setVisibility(View.GONE);
			}
			break;
		case R.id.plus_btn:
			if (!isPlusShow) {
				if (isFaceShow) {
					isFaceShow = false;
					faceLinearLayout.setVisibility(View.GONE);
				}
				HideKeyboard();
				isPlusShow = true;
				mPlusGridView.setVisibility(View.VISIBLE);
			} else {
				isPlusShow = false;
				mPlusGridView.setVisibility(View.GONE);
			}
			break;
		case R.id.send_btn:
			String str = msgEt.getText().toString();
			if (str != null && !str.isEmpty()) {
				if (!db.isOpen()) {
					db = new DBManager(this);
				}
				MessagePojo mp;
				if (TimeUtil.isFiveMin(db.getLastTime(1, 2))) {
					SimpleDateFormat format = new SimpleDateFormat(
							"yy-MM-dd HH:mm");
					Date today = new Date(System.currentTimeMillis());
					mp = new MessagePojo(1, 2, format.format(today), str, 1, 1);
				} else {
					mp = new MessagePojo(1, 2, "", str, 1, 1);
				}
				new SendMessageThread().start();
				mMessageAdapter.updMessage(mp);
				mListView.setSelection(list.size() - 1);
				msgEt.setText("");
				db.addMessage(mp);
			}
			break;
		case R.id.msg_et:
			if (isFaceShow) {
				isFaceShow = false;
				faceLinearLayout.setVisibility(View.GONE);
			}
			if (isPlusShow) {
				isPlusShow = false;
				mPlusGridView.setVisibility(View.GONE);
			}
			break;
		}
	}

	@Override
	public boolean onTouch(View v, MotionEvent event) {
		if (isFaceShow) {
			faceLinearLayout.setVisibility(View.GONE);
		}
		if (isPlusShow) {
			mPlusGridView.setVisibility(View.GONE);
		}
		HideKeyboard();
		return false;
	}

	@Override
	public void beforeTextChanged(CharSequence s, int start, int count,
			int after) {
	}

	@Override
	public void onTextChanged(CharSequence s, int start, int before, int count) {
	}

	@Override
	public void afterTextChanged(Editable s) {
		int count = msgEt.getText().toString().length();
		if (count > 0) {
			mPlusBtn.setVisibility(View.GONE);
			mSendBtn.setVisibility(View.VISIBLE);
		} else {
			mPlusBtn.setVisibility(View.VISIBLE);
			mSendBtn.setVisibility(View.GONE);
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
		registerReceiver(mReuRequstReceiver, new IntentFilter(
				"com.comdosoft.fuxun.REQUEST_ACTION"));
	}

	@Override
	protected void onPause() {
		super.onPause();
		unregisterReceiver(mReuRequstReceiver);

	}

	class RequstReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			handler.sendEmptyMessage(2);
		}
	}
}
