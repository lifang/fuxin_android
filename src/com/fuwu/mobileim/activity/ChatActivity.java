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

/**
 * @作者 马龙
 * @时间 创建时间：2014-5-15 下午4:41:29
 */
@SuppressLint("NewApi")
public class ChatActivity extends Activity implements OnClickListener,
		OnTouchListener, TextWatcher, IXListViewListener {

	private String[] itemName = new String[] { " ", " " };
	private int[] itemImg = new int[] { R.drawable.pic, R.drawable.camera };
	private int mMesPageNum = 1;
	private int mMesCount = 0;
	private int newMessage = 0; // 用于统计历史数据数量
	private List<MessagePojo> newMessageList; // 新消息集合
	private int sendAgain_postion = 0;
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
	private RequstReceiver2 mReuRequstReceiver2;
	private ExecutorService sendMessageExecutor = Executors
			.newFixedThreadPool(1);
	private ExecutorService loadImageExecutor = Executors
			.newSingleThreadExecutor();
	private Handler handler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			// if (msg.what == 5 || msg.what == 6 || msg.what == 7) {
			progressDialogDismiss();
			// }
			switch (msg.what) {
			case 0:
				Toast.makeText(getApplicationContext(), "请求失败!", 0).show();
				break;
			case 1:
				msgEt.setText("");
				break;
			case 2:
				updateMessageData();
				mMessageAdapter.notifyDataSetChanged();
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
				Log.i("FuWu", "发送消息后newMessage---" + newMessage);
				break;
			case 7:
				// mMessageAdapter.updMessage(mp);
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
			case 13:
				new Handler().postDelayed(new Runnable() {
					public void run() {
						Intent intent = new Intent(ChatActivity.this,
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
				loadMoreMessageData();
				mMessageAdapter.notifyDataSetChanged();
				mListView.stopRefresh();
				break;
			case 15:
				mMessageAdapter.notifyDataSetChanged();
				mListView.setSelection(list.size() - 1);
				break;
			case 16:
				Toast.makeText(getApplicationContext(),
						"此联系人尚未开通接收消息功能，如需帮助请拨打福务热线：400-000-5555。",
						Toast.LENGTH_LONG).show();
				break;
			}
		}
	};
	private FxApplication fxApplication;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.chat);
		fxApplication = (FxApplication) getApplication();
		fxApplication.getActivityList().add(this);
		initData();
		initView();
		initFacePage();
		initPlusGridView();
		if (FuXunTools.isConnect(ChatActivity.this)) {
			new MessageConfirmed().start();
		}
	}

	public void initData() {
		db = new DBManager(this);
		mReuRequstReceiver = new RequstReceiver();
		mReuRequstReceiver2 = new RequstReceiver2();
		DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
		height = displayMetrics.heightPixels;
		Set<String> keySet = FxApplication.getInstance().getFaceMap().keySet();
		keys = new ArrayList<String>();
		keys.addAll(keySet);

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

		mMesCount = db.getMesCount(user_id, contact_id);
		updateMessageData();
		newMessageList = new ArrayList<MessagePojo>();
	}

	// 获取最近的 15条消息
	public void updateMessageData() {

		db.clearTalkMesCount(user_id, contact_id);
		list = db.queryMessageList(user_id, contact_id,
				(mMesCount - mMesPageNum * 100), 100);
		newMessage = mMesCount;
		Log.i("FuWu", "newMessage---" + newMessage);
		mMesPageNum = 2;
//		Toast.makeText(getApplicationContext(), list.get(0).getContent(),
//				Toast.LENGTH_LONG).show();
	}

	// 获取老消息
	public void loadMoreMessageData() {
		db.clearTalkMesCount(user_id, contact_id);
		Log.i("FuWu", "mMesCount---" + mMesCount);
		Log.i("FuWu", "(mMesCount - mMesPageNum * 100)---"
				+ (mMesCount - mMesPageNum * 100));
		Log.i("FuWu", "(mMesCount - (mMesPageNum - 1) * 100)---"
				+ (mMesCount - (mMesPageNum - 1) * 100));
		Log.i("FuWu", "mMesPageNum---" + mMesPageNum);
		if ((mMesCount - (mMesPageNum - 1) * 100) > 0) { // 为负数时会获取全部消息

			List<MessagePojo> loadMoreList = db.queryMessageList(user_id,
					contact_id, (mMesCount - mMesPageNum * 100), 100);
			for (int i = loadMoreList.size() - 1; i >= 0; i--) {
				list.add(0, loadMoreList.get(i));
			}
			Log.i("FuWu", "loadMoreList.size()---" + loadMoreList.size());
			mMesPageNum++;
		}
	}

	// 获取新消息
	public void loadNewMessageData() {

		int allMesCount = db.getMesCount(user_id, contact_id);
		db.clearTalkMesCount(user_id, contact_id);
		newMessageList = db.queryMessageList(user_id, contact_id, newMessage,
				allMesCount - newMessage);

		for (int i = 0; i < newMessageList.size(); i++) {
			if (newMessageList.get(i).getIsComMeg() == 0) {
				list.add(newMessageList.get(i));
			}
		}
		if (newMessageList.size()!=0) {
			if (FuXunTools.isConnect(ChatActivity.this)) {
				new MessageConfirmed().start();
			}
		}

		newMessage = allMesCount;
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
		mMessageAdapter = new MessageListViewAdapter(getResources(), this);
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
		if (cp.getCustomName() != null && cp.getCustomName().length()!=0) {
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
		view.findViewById(R.id.lookall_mes).setOnClickListener(this);

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
				mes.setSendTime(messagePojo.getSendTime());
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
						Log.i("FuWu", mp.getContent()+"--sendTime-2:" + sendTime);
						// messagePojo.setSendTime(sendTime);
						messagePojo.setStatus(0);
						db.updateContactlastContactTime(user_id, contact_id,
								TimeUtil.getCurrentTime());
						handler.sendEmptyMessage(6);

					} else {

						int ErrorCode = response.getErrorCode().getNumber();
						if (ErrorCode == 2001) {
							handler.sendEmptyMessage(13);
						} else {
							messagePojo.setStatus(2);
							handler.sendEmptyMessage(5);
						}

					}
				} else {
					messagePojo.setStatus(2);
					handler.sendEmptyMessage(5);
				}
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
			if (str != null && str.length()!=0) {
				if (str.length() > 300) {
					Toast.makeText(getApplicationContext(),
							"信息内容限制300字,请分段输入.", 0).show();
				} else {
					if (contact_id == 0) {
						handler.sendEmptyMessage(16);
					} else {

						if (FuXunTools.isConnect(this)) {
							handler.sendEmptyMessage(1);
							String time = "";
							String sendTime = TimeUtil.getCurrentTime();
							time = sendTime;
							mp = new MessagePojo(user_id, contact_id, time,
									str, 1, 1);
							mp.setStatus(1);
							list.add(mp);
							mMessageAdapter.notifyDataSetChanged();
							// mMessageAdapter.updMessage(mp);
							mListView.setSelection(list.size() - 1);
							sendMessageExecutor.execute(new SendMessageThread(
									null, str, 1, mp));
						} else {
							handler.sendEmptyMessage(8);
						}

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
		case R.id.lookall_mes:
			Intent intent = new Intent();
			intent.setClass(ChatActivity.this,
					LookAllMessageActivity.class);
			startActivity(intent);
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
		registerReceiver(mReuRequstReceiver2, new IntentFilter(
				"com.comdosoft.fuxun.REQUEST_ACTION2"));
		StatService.onResume(this);
	}

	@Override
	protected void onPause() {
		super.onPause();
		unregisterReceiver(mReuRequstReceiver);
		unregisterReceiver(mReuRequstReceiver2);
		StatService.onPause(this);
	}

	@SuppressWarnings("deprecation")
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (contact_id == 0) {
			handler.sendEmptyMessage(16);
		} else {

			if (FuXunTools.isConnect(this)) {
				if (resultCode == RESULT_OK) {
					Bitmap photo = null;
					String fileName = System.currentTimeMillis() + "";
					String sendTime = TimeUtil.getCurrentTime();
					Log.i("FuWu", "sendTime-1:" + sendTime);
					mp = new MessagePojo(user_id, contact_id, sendTime,
							fileName + ".jpg", 1, 2);
					mp.setStatus(1);
					// mMessageAdapter.updMessage(mp);
					list.add(mp);
					mMessageAdapter.notifyDataSetChanged();
					switch (requestCode) {
					case 1:
						// pd.show();
						Uri imgUri = data.getData();
						String[] proj = { MediaStore.Images.Media.DATA };
						Cursor cursor = managedQuery(imgUri, proj, null, null,
								null);
						int column_index = cursor
								.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
						cursor.moveToFirst();
						String path = cursor.getString(column_index);
						photo = ImageUtil.compressImage(path);
						ImageUtil.saveBitmap(fileName, "JPG", photo);
						sendMessageExecutor.execute(new SendMessageThread(
								Bitmap2Bytes(photo), null, 2, mp));
						break;
					case 2:
						// pd.show();
						photo = ImageUtil.compressImage(Urlinterface.SDCARD
								+ "camera.jpg");
						ImageUtil.saveBitmap(fileName, "JPG", photo);
						sendMessageExecutor.execute(new SendMessageThread(
								Bitmap2Bytes(photo), null, 2, mp));
						break;
					}
				}
			} else {
				handler.sendEmptyMessage(8);
			}
		}
	}

	// 重新发送
	public void sendAgain_method(int postion) {
		if (FuXunTools.isConnect(this)) {
			Bitmap photo = null;
			list.get(postion).setStatus(1);
			mMessageAdapter.notifyDataSetChanged();
			MessagePojo mp = list.get(postion);
			switch (mp.getMsgType()) {
			case 1: // 文本
				sendMessageExecutor.execute(new SendMessageThread(null, mp
						.getContent(), 1, mp));
				break;
			case 2: // 图片
				photo = ImageUtil.compressImage(Urlinterface.SDCARD
						+ mp.getContent());
				sendMessageExecutor.execute(new SendMessageThread(
						Bitmap2Bytes(photo), null, 2, mp));
				break;

			default:
				break;
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

		handler.sendEmptyMessage(14);
	}

	class RequstReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			loadNewMessageData();
			handler.sendEmptyMessage(15);

		}
	}
	class RequstReceiver2 extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			handler.sendEmptyMessage(15);
		}
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
							sendAgain_postion = position;
							sendAgain_method(sendAgain_postion);
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

	class MessageConfirmed extends Thread {
		@Override
		public void run() {
			super.run();
			try {
				MessageConfirmedRequest.Builder builder = MessageConfirmedRequest
						.newBuilder();
				Log.i("Ax", "timeStamp:" + getTimeStamp());
				builder.setUserId(user_id);
				builder.setToken(token);
				builder.setTimeStamp(getTimeStamp());
				builder.setContactId(contact_id);
				MessageConfirmedRequest request = builder.build();
				Log.i("FuWu1", "-----------");
				Log.i("FuWu1", Urlinterface.MessageConfirmed + "--");
				byte[] b = HttpUtil.sendHttps(request.toByteArray(),
						Urlinterface.MessageConfirmed, "POST");
				Log.i("FuWu1", b.toString() + "--");
				if (b != null && b.length > 0) {
					MessageConfirmedResponse response = MessageConfirmedResponse
							.parseFrom(b);
					if (response.getIsSucceed()) {
						Log.i("FuWu1", response.getIsSucceed() + "--");
					} else {
						int ErrorCode = response.getErrorCode().getNumber();
						if (ErrorCode == 2001) {
							handler.sendEmptyMessage(13);
						}
					}
				}
			} catch (Exception e) {
			}
		}
	}

	public String getTimeStamp() {
		String time = sp.getString("sendTime"+user_id, "");
		if (time != null && !time.equals("")) {
			return time;
		}
		return "";
	}
}
