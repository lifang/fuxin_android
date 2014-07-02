package com.fuwu.mobileim.activity;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
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
import android.text.TextWatcher;
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
import android.widget.PopupWindow;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;
import com.baidu.mobstat.StatService;
import com.fuwu.mobileim.R;
import com.fuwu.mobileim.adapter.FaceAdapter;
import com.fuwu.mobileim.adapter.FacePageAdapter;
import com.fuwu.mobileim.adapter.MessageListViewAdapter;
import com.fuwu.mobileim.model.Models.Message.ContentType;
import com.fuwu.mobileim.model.Models.Message.ImageType;
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
import com.fuwu.mobileim.util.ImageUtil;
import com.fuwu.mobileim.util.TimeUtil;
import com.fuwu.mobileim.util.Urlinterface;
import com.fuwu.mobileim.view.CirclePageIndicator;
import com.fuwu.mobileim.view.XListView;
import com.fuwu.mobileim.view.XListView.IXListViewListener;
import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;

/**
 * @作者 马龙
 * @时间 创建时间：2014-5-15 下午4:41:29
 */
@SuppressLint("NewApi")
public class ChatActivity extends Activity implements OnClickListener,
		OnTouchListener, TextWatcher, IXListViewListener {

	private String[] itemName = new String[] { "图片", "拍摄" };
	private int[] itemImg = new int[] { R.drawable.pic, R.drawable.camera };
	private int mMesPageNum = 1;
	private int mMesCount = 0;
	private int user_id = 1;
	private int contact_id = 1;
	private float height = 0;
	private int currentPage = 0;
	private boolean isFaceShow = false;
	private boolean isPlusShow = false;
	private String customName;
	private String token;
	private String sendTime;
	private List<String> keys;
	private List<MessagePojo> list;
	private XListView mListView;
	private ViewPager faceViewPager;
	private LinearLayout faceLinearLayout;
	private GridView mPlusGridView;
	private Button mSendBtn;
	private TextView mName;
	private ImageView mBack;
	private ImageView mOther;
	private ImageView mFaceBtn;
	private ImageView mPlusBtn;
	private EditText msgEt;
	private MessagePojo mp;
	private ProgressDialog pd;
	private PopupWindow menuWindow;
	private MessageListViewAdapter mMessageAdapter;
	private DBManager db;
	private ShortContactPojo cp;
	private TalkPojo tp;
	private SharedPreferences sp;
	private RequstReceiver mReuRequstReceiver;
	private ExecutorService sendMessageExecutor = Executors
			.newFixedThreadPool(5);
	private ExecutorService loadImageExecutor = Executors
			.newSingleThreadExecutor();
	private Handler handler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			if (msg.what == 5 || msg.what == 6 || msg.what == 7) {
				progressDialogDismiss();
			}
			switch (msg.what) {
			case 0:
				Toast.makeText(getApplicationContext(), "请求失败!", 0).show();
				break;
			case 1:
				msgEt.setText("");
				break;
			case 2:
				updateMessageData();
				mMessageAdapter.updMessageList(list);
				mListView.stopRefresh();
				break;
			case 3:
				Toast.makeText(getApplicationContext(), "屏蔽联系人成功!", 0).show();
				break;
			case 4:
				Toast.makeText(getApplicationContext(), "屏蔽联系人失败!", 0).show();
				break;
			case 5:
				mMessageAdapter.notifyDataSetChanged();
				Toast.makeText(getApplicationContext(), "消息发送失败!", 0).show();
				break;
			case 6:
				mMessageAdapter.notifyDataSetChanged();
				break;
			case 7:
				mMessageAdapter.updMessage(mp);
				mListView.setSelection(list.size() - 1);
				break;
			case 8:
				Toast.makeText(getApplicationContext(), "网络异常", 0).show();
				break;
			case 9:
				pd.dismiss();
				Toast.makeText(getApplicationContext(), "获取联系人失败", 0).show();
				break;
			case 10:
				db.updateContactRem(user_id, cp.getContactId(), customName);
				mName.setText(customName);
				ContactCache.cp.setCustomName(customName);
				Toast.makeText(getApplicationContext(), "修改备注成功!", 0).show();
				break;
			case 11:
				Toast.makeText(getApplicationContext(), "修改备注失败!", 0).show();
				break;
			case 12:
				ContactCache.flag = true;
				pd.dismiss();
				break;
			}
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
	}

	public void initData() {
		db = new DBManager(this);
		mReuRequstReceiver = new RequstReceiver();
		DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
		height = displayMetrics.heightPixels;
		Set<String> keySet = FxApplication.getInstance().getFaceMap().keySet();
		keys = new ArrayList<String>();
		keys.addAll(keySet);

		sp = getSharedPreferences(Urlinterface.SHARED, Context.MODE_PRIVATE);
		user_id = sp.getInt("user_id", 1);
		contact_id = sp.getInt("contact_id", 1);
		token = sp.getString("Token", "token");

		cp = db.queryContact(user_id, contact_id);
		updateMessageData();
	}

	public void updateMessageData() {
		mMesCount = db.getMesCount(user_id, contact_id);
		db.clearTalkMesCount(user_id, contact_id);
		list = db.queryMessageList(user_id, contact_id,
				(mMesCount - mMesPageNum * 15), mMesCount);
		mMesPageNum++;
	}

	public void initView() {
		mListView = (XListView) findViewById(R.id.chat_listView);
		faceLinearLayout = (LinearLayout) findViewById(R.id.face_ll);
		mPlusGridView = (GridView) findViewById(R.id.chat_plus_panel);
		mName = (TextView) findViewById(R.id.chat_name);
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
		mBack.setOnClickListener(this);
		mBack.setOnTouchListener(this);
		mOther.setOnTouchListener(this);
		mOther.setOnClickListener(this);
		mListView.setXListViewListener(this);
		// mBack.setImageBitmap(ImageUtil.getBitmapScale(height,
		// R.drawable.back,
		// getResources()));
		// mOther.setImageBitmap(ImageUtil.getBitmapScale(height,
		// R.drawable.other, getResources()));
		mFaceBtn.setImageBitmap(ImageUtil.getBitmapScale(height,
				R.drawable.face, getResources()));
		mFaceBtn.setOnTouchListener(new OnTouchListener() {
			// 点击变色
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				if (event.getAction() == MotionEvent.ACTION_DOWN) {
					mFaceBtn.setImageBitmap(ImageUtil.getBitmapScale(height,
							R.drawable.face_hover, getResources()));
				} else if (event.getAction() == MotionEvent.ACTION_UP) {
					mFaceBtn.setImageBitmap(ImageUtil.getBitmapScale(height,
							R.drawable.face, getResources()));
				}
				return false;
			}
		});
		mPlusBtn.setImageBitmap(ImageUtil.getBitmapScale(height,
				R.drawable.plus, getResources()));
		mPlusBtn.setOnTouchListener(new OnTouchListener() {
			// 点击变色
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				if (event.getAction() == MotionEvent.ACTION_DOWN) {
					mPlusBtn.setImageBitmap(ImageUtil.getBitmapScale(height,
							R.drawable.plus_hover, getResources()));
				} else if (event.getAction() == MotionEvent.ACTION_UP) {
					mPlusBtn.setImageBitmap(ImageUtil.getBitmapScale(height,
							R.drawable.plus, getResources()));
				}
				return false;
			}
		});
		mName.setText(getContactName());
		pd = new ProgressDialog(this);
		pd.setCanceledOnTouchOutside(false);
		pd.setMessage("正在发送图片...");
		mMessageAdapter = new MessageListViewAdapter(getResources(), this,
				list, cp, user_id, token);
		mListView.setAdapter(mMessageAdapter);
		mListView.setOnTouchListener(this);
		mListView.setSelection(list.size() - 1);
	}

	private void initFacePage() {
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
					String ori = msgEt.getText().toString();
					int index = msgEt.getSelectionStart();
					StringBuilder stringBuilder = new StringBuilder(ori);
					stringBuilder.insert(index, keys.get(count));
					msgEt.setText(stringBuilder.toString());
					msgEt.setSelection(index + keys.get(count).length());
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
				if (FuXunTools.isConnect(ChatActivity.this)) {
					if (position == 0) {
						Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
						intent.setType("image/*");
						startActivityForResult(intent, 1);
					} else {
						Intent takephoto = new Intent(
								MediaStore.ACTION_IMAGE_CAPTURE);
						takephoto.putExtra(
								MediaStore.EXTRA_OUTPUT,
								Uri.fromFile(new File(Urlinterface.SDCARD
										+ "camera.jpg")));
						startActivityForResult(takephoto, 2);
					}
				} else {
					handler.sendEmptyMessage(8);
				}
			}
		});
	}

	public void progressDialogDismiss() {
		if (pd.isShowing()) {
			pd.dismiss();
		}
	}

	public String getContactName() {
		if (cp.getCustomName() != null && !cp.getCustomName().isEmpty()) {
			return cp.getCustomName();
		}
		return cp.getName();
	}

	public void HideKeyboard() {
		InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
		imm.hideSoftInputFromWindow(msgEt.getWindowToken(), 0); // 强制隐藏键盘
	}

	private byte[] Bitmap2Bytes(Bitmap bm) {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		bm.compress(Bitmap.CompressFormat.PNG, 100, baos);
		return baos.toByteArray();
	}

	@SuppressWarnings("deprecation")
	public void menu_press() {
		View view = getLayoutInflater().inflate(R.layout.chat_other, null);
		view.findViewById(R.id.chatset_clear).setOnClickListener(this);
		// view.findViewById(R.id.chatset_block).setOnClickListener(this);
		// view.findViewById(R.id.chatset_beizhu).setOnClickListener(this);
		menuWindow = new PopupWindow(view, LayoutParams.WRAP_CONTENT,
				LayoutParams.WRAP_CONTENT);
		menuWindow.setFocusable(true);
		menuWindow.setOutsideTouchable(true);
		menuWindow.update();
		menuWindow.setBackgroundDrawable(new BitmapDrawable());
		// 设置layout在PopupWindow中显示的位置
		int h = (int) height * 145 / 1280;
		menuWindow.showAtLocation(this.findViewById(R.id.chat_main),
				Gravity.TOP | Gravity.RIGHT, 0, h);
	}

	class SendMessageThread extends Thread {
		private String message;
		private int type;
		private byte[] bArr;
		private MessagePojo messagePojo;

		public SendMessageThread(byte[] bArr, String message, int type,
				MessagePojo messagePojo) {
			super();
			this.message = message;
			this.type = type;
			this.bArr = bArr;
			this.messagePojo = messagePojo;
		}

		@Override
		public void run() {
			super.run();
			try {
				handler.sendEmptyMessage(1);
				SendMessageRequest.Builder builder = SendMessageRequest
						.newBuilder();
				builder.setToken(token);
				builder.setUserId(user_id);
				com.fuwu.mobileim.model.Models.Message.Builder mes = com.fuwu.mobileim.model.Models.Message
						.newBuilder();
				mes.setSendTime("");
				mes.setContactId(contact_id);
				mes.setUserId(user_id);
				if (type == 1) {
					mes.setContent(message);
					mes.setContentType(ContentType.Text);
				} else {
					mes.setBinaryContent(ByteString.copyFrom(bArr));
					mes.setContentType(ContentType.Image);
					mes.setImageType(ImageType.JPG);
				}
				builder.setMessage(mes);
				Log.i("FuWu", user_id + "--id--" + contact_id);
				SendMessageRequest smr = builder.build();
				byte[] b = HttpUtil.sendHttps(smr.toByteArray(),
						Urlinterface.Message, "PUT");
				if (b != null && b.length > 0) {
					SendMessageResponse response = SendMessageResponse
							.parseFrom(b);
					if (response.getIsSucceed()) {
						sendTime = response.getSendTime();
						String time = "";
						if (TimeUtil.isFiveMin(
								db.getLastTime(user_id, contact_id), sendTime)) {
							time = sendTime;
						}
						messagePojo.setSendTime(time);
						messagePojo.setStatus(0);
						db.addMessage(messagePojo);
						db.updateContactlastContactTime(user_id, contact_id,
								TimeUtil.getCurrentTime());
						if (type == 1) {
							tp = new TalkPojo(user_id, contact_id,
									getContactName(), cp.getUserface_url(),
									message, sendTime, 0);
						} else {
							tp = new TalkPojo(user_id, contact_id,
									getContactName(), cp.getUserface_url(),
									"[图片]", sendTime, 0);
							mp = messagePojo;
							handler.sendEmptyMessage(7);
						}
						db.addTalk(tp);
						handler.sendEmptyMessage(6);
					} else {
						messagePojo.setStatus(2);
						handler.sendEmptyMessage(5);
					}
				} else {
					handler.sendEmptyMessage(5);
				}
			} catch (InvalidProtocolBufferException e) {
				e.printStackTrace();
			}
		}
	}

	class LoadImage extends Thread {
		private String path;

		public LoadImage(String path) {
			super();
			this.path = path;
		}

		@Override
		public void run() {
			super.run();
			Bitmap photo = ImageUtil.compressImage(path);
			String fileName = System.currentTimeMillis() + "";
			ImageUtil.saveBitmap(fileName, "JPG", photo);
			String time = "";
			String sendTime = TimeUtil.getCurrentTime();
			if (TimeUtil.isFiveMin(db.getLastTime(user_id, contact_id),
					sendTime)) {
				time = sendTime;
			}
			mp = new MessagePojo(user_id, contact_id, time, fileName + ".jpg",
					1, 2);
			sendMessageExecutor.execute(new SendMessageThread(
					Bitmap2Bytes(photo), null, 2, mp));
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
				if (str.length() > 300) {
					Toast.makeText(getApplicationContext(),
							"信息内容限制300字,请分段输入.", 0).show();
				} else {
					if (FuXunTools.isConnect(this)) {
						handler.sendEmptyMessage(1);
						String time = "";
						String sendTime = TimeUtil.getCurrentTime();
						if (TimeUtil.isFiveMin(
								db.getLastTime(user_id, contact_id), sendTime)) {
							time = sendTime;
						}
						mp = new MessagePojo(user_id, contact_id, time, str, 1,
								1);
						mp.setStatus(1);
						mMessageAdapter.updMessage(mp);
						mListView.setSelection(list.size() - 1);
						sendMessageExecutor.execute(new SendMessageThread(null,
								str, 1, mp));
					} else {
						handler.sendEmptyMessage(8);
					}
				}
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
		case R.id.chatset_clear:
			db.delMessage(user_id, contact_id);
			handler.sendEmptyMessage(2);
			if (menuWindow.isShowing()) {
				menuWindow.dismiss();
			}
			break;
		case R.id.chat_back:
			ContactCache.flag = false;
			this.finish();
			break;
		case R.id.chat_other:
			menu_press();
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
			case R.id.chat_other:
				findViewById(R.id.chat_other).getBackground().setAlpha(70);
				break;
			}
		} else if (event.getAction() == MotionEvent.ACTION_UP) {
			switch (v.getId()) {
			case R.id.chat_back:
				findViewById(R.id.chat_back).getBackground().setAlpha(255);
				break;
			case R.id.chat_other:
				findViewById(R.id.chat_other).getBackground().setAlpha(255);
				break;
			}
		}
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
		registerReceiver(mReuRequstReceiver, new IntentFilter(
				"com.comdosoft.fuxun.REQUEST_ACTION"));
		StatService.onResume(this);
	}

	@Override
	protected void onPause() {
		super.onPause();
		unregisterReceiver(mReuRequstReceiver);
		StatService.onPause(this);
	}

	@SuppressWarnings("deprecation")
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (FuXunTools.isConnect(this)) {
			if (resultCode == RESULT_OK) {
				pd.setMessage("正在发送图片...");
				switch (requestCode) {
				case 1:
					pd.show();
					Uri imgUri = data.getData();
					String[] proj = { MediaStore.Images.Media.DATA };
					Cursor cursor = managedQuery(imgUri, proj, null, null, null);
					int column_index = cursor
							.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
					cursor.moveToFirst();
					String path = cursor.getString(column_index);
					loadImageExecutor.execute(new LoadImage(path));
					break;
				case 2:
					pd.show();
					loadImageExecutor.execute(new LoadImage(Urlinterface.SDCARD
							+ "camera.jpg"));
					break;
				}
			}
		} else {
			handler.sendEmptyMessage(8);
		}
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
		handler.sendEmptyMessage(2);
	}

	class RequstReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			handler.sendEmptyMessage(2);
		}
	}

}
