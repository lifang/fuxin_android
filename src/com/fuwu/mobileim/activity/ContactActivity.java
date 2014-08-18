package com.fuwu.mobileim.activity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.TextView;
import android.widget.Toast;

import com.fuwu.mobileim.R;
import com.fuwu.mobileim.adapter.ContactAdapter;
import com.fuwu.mobileim.model.Models.ContactRequest;
import com.fuwu.mobileim.model.Models.ContactResponse;
import com.fuwu.mobileim.pojo.ShortContactPojo;
import com.fuwu.mobileim.util.DBManager;
import com.fuwu.mobileim.util.FuXunTools;
import com.fuwu.mobileim.util.FxApplication;
import com.fuwu.mobileim.util.HttpUtil;
import com.fuwu.mobileim.util.ImageCacheUtil;
import com.fuwu.mobileim.util.LongDataComparator;
import com.fuwu.mobileim.util.OrderTimeLongDataComparator;
import com.fuwu.mobileim.util.SubscribeTimeLongDataComparator;
import com.fuwu.mobileim.util.Urlinterface;
import com.fuwu.mobileim.view.XListView;
import com.fuwu.mobileim.view.XListView.IXListViewListener;

/**
 * @作者 丁作强
 * @时间 2014-6-13 上午11:31:19
 */
public class ContactActivity extends Fragment implements IXListViewListener {
	private ExecutorService singleThreadExecutor = Executors
			.newSingleThreadExecutor();
	private DBManager db;
	private FxApplication fxApplication;
	private XListView xListView;// 可上拉刷新 的 listview ，
	private boolean onRefresh_number = true;
	private TextView dialog;
	private ContactAdapter adapter2;
	private View rootView;
	private int user_number1 = 0;
	List<ShortContactPojo> contactsList1 = new ArrayList<ShortContactPojo>();

	private List<ShortContactPojo> contactsList = new ArrayList<ShortContactPojo>();; // 联系人arraylist数组

	/**
	 * 根据最近联系时间来排列ListView里面的数据类
	 */
	private LongDataComparator longDataComparator;
	/**
	 * 根据最后产生订单时间来排列ListView里面的数据类
	 */
	private OrderTimeLongDataComparator orderTimeLongDataComparator;
	/**
	 * 根据最后订阅时间来排列ListView里面的数据类
	 */
	private SubscribeTimeLongDataComparator subscribeTimeLongDataComparator;
	private Button button_recently, button_trading, button_subscription;
	private Button view2, view3;
	private int width;
	private int buttonNumber = 0;
	private int index = 1; // 记录 数组下标
	private List<Button> btnList = new ArrayList<Button>();
	private int user_id = 0;
	private int version = 0;
	private Handler handler = new Handler() {
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case 0:
				contactsList = new ArrayList<ShortContactPojo>();
				contactsList1 = db.queryContactList(user_id);
				Collections.sort(contactsList1, longDataComparator);
				if (contactsList1.size() > 20) { // 20个以上进行排序
					for (int i = 0; i < 20; i++) {
						contactsList.add(contactsList1.get(i));
					}
				} else {
					for (int i = 0; i < contactsList1.size(); i++) {
						contactsList.add(contactsList1.get(i));
					}
				}

				xListView.setAdapter(adapter2);
				// adapter2.updateListView(contactsList);
				onLoad();
				break;
			case 1:
				onLoad();
				if (contactsList1.size() == 0) {
					Toast.makeText(getActivity(), "没有数据更新", Toast.LENGTH_SHORT)
							.show();
				} else {
					ImageCacheUtil.IMAGE_CACHE.clear();
					for (int i = 0; i < contactsList1.size(); i++) {
						db.modifyContact(user_id, contactsList1.get(i));
						String customName = contactsList1.get(i).getCustomName();
						if (customName.length() > 0) {
							db.updateTalkRem(user_id, contactsList1.get(i)
									.getContactId(), customName);
						} else {
							String customName2 = contactsList1.get(i).getName();
							db.updateTalkRem(user_id, contactsList1.get(i)
									.getContactId(), customName2);
						}
					}
					FuXunTools.getBitmap(contactsList1);
				}
				switchButton(buttonNumber);
				break;
			case 2:
				onRefresh_number = true;
				onLoad();
				break;
			case 6:
				Toast.makeText(getActivity(), "请求失败", Toast.LENGTH_SHORT)
						.show();
				break;
			case 7:
				Toast.makeText(getActivity(), R.string.no_internet,
						Toast.LENGTH_SHORT).show();
				break;
			case 9:
				onLoad();
				new Handler().postDelayed(new Runnable() {
					public void run() {
						Intent intent = new Intent(getActivity(),
								LoginActivity.class);
						startActivity(intent);
						getActivity().finish();
					}
				}, 3500);
				FuXunTools.initdate(preferences, fxApplication);
				Toast.makeText(getActivity(), "您的账号已在其他手机登陆", Toast.LENGTH_LONG)
						.show();
				break;
			case 10:
				onRefresh_number = true;
				break;
			}
		}
	};
	private SharedPreferences preferences;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		rootView = inflater
				.inflate(R.layout.contact_activity, container, false);
		fxApplication = (FxApplication) getActivity().getApplication();
		adapter2 = new ContactAdapter(getActivity(), contactsList);
		db = new DBManager(getActivity());
		String release = android.os.Build.VERSION.RELEASE; // android系统版本号
		version = Integer.parseInt(release.substring(0, 1));
		preferences = getActivity().getSharedPreferences(Urlinterface.SHARED,
				Context.MODE_PRIVATE);
		user_id = preferences.getInt("user_id", -1);
		longDataComparator = new LongDataComparator();
		orderTimeLongDataComparator = new OrderTimeLongDataComparator();
		subscribeTimeLongDataComparator = new SubscribeTimeLongDataComparator();
		initViews();
		setButton();
		return rootView;
	}

	@Override
	public void onStart() {
		super.onStart();
		Log.i("Test", "onStart");
		// if (buttonNumber == 0) {
		// handler.sendEmptyMessage(0);
		// }
		switchButton(buttonNumber);
	}

	/**
	 * 
	 * 第二次 获得所有联系人
	 * 
	 * 
	 */

	private void getContacts2() {

		singleThreadExecutor.execute(new Runnable() {

			@Override
			public void run() {
				try {

					String timeStamp = preferences.getString(
							"contactTimeStamp", "");
					ContactRequest.Builder builder = ContactRequest
							.newBuilder();
					builder.setUserId(user_id);
					builder.setToken(preferences.getString("Token", ""));
					if (timeStamp.equals("")) {

					} else {
						builder.setTimeStamp(timeStamp);
					}
					Log.i("1", "User_id:" + fxApplication.getUser_id()
							+ "--Token" + fxApplication.getToken()
							+ "--timeStamp:" + timeStamp);

					ContactRequest response = builder.build();

					byte[] by = HttpUtil.sendHttps(response.toByteArray(),
							Urlinterface.getContacts, "POST");
					if (by != null && by.length > 0) {

						ContactResponse res = ContactResponse.parseFrom(by);
						contactsList1.clear();
						if (res.getIsSucceed()) {
							for (int i = 0; i < res.getContactsCount(); i++) {
								int contactId = res.getContacts(i)
										.getContactId();
								String name = res.getContacts(i).getName();
								String customName = res.getContacts(i)
										.getCustomName();
								String sortKey = "";
								String userface_url = res.getContacts(i)
										.getTileUrl();
								int sex = res.getContacts(i).getGender()
										.getNumber();
								int source = res.getContacts(i).getSource();
								String lastContactTime = res.getContacts(i)
										.getLastContactTime();// 2014-05-27
																// 11:42:18
								boolean isblocked = res.getContacts(i)
										.getIsBlocked();
								boolean isprovider = res.getContacts(i)
										.getIsProvider();
								int isBlocked = -1, isProvider = -1;
								if (isblocked == true) {
									isBlocked = 1;
								} else if (isblocked == false) {
									isBlocked = 0;
								}
								if (isprovider == true) {
									isProvider = 1;
								} else if (isprovider == false) {
									isProvider = 0;
								}
								String lisence = res.getContacts(i)
										.getLisence();
								String individualResume = res.getContacts(i)
										.getIndividualResume();
								String orderTime = res.getContacts(i)
										.getOrderTime();
								String subscribeTime = res.getContacts(i)
										.getSubscribeTime();
								ShortContactPojo coPojo = new ShortContactPojo(
										contactId, sortKey, name, customName,
										userface_url, sex, source,
										lastContactTime, isBlocked, orderTime,
										subscribeTime);
								contactsList1.add(coPojo);

							}

							SharedPreferences preferences2 = getActivity()
									.getSharedPreferences(Urlinterface.SHARED,
											Context.MODE_PRIVATE);
							Editor editor = preferences2.edit();
							editor.putString("contactTimeStamp",
									res.getTimeStamp());

							editor.commit();
							Message msg = new Message();// 创建Message 对象
							msg.what = 1;
							handler.sendMessage(msg);
						} else {
							int ErrorCode = res.getErrorCode().getNumber();
							if (ErrorCode == 2001) {
								handler.sendEmptyMessage(9);
							} else {
								handler.sendEmptyMessage(2);
							}
						}
					}else {
						handler.sendEmptyMessage(2);
					}

				} catch (Exception e) {
					handler.sendEmptyMessage(2);
				}

			}
		});
	}

	private void initViews() {

		xListView = (XListView) rootView
				.findViewById(R.id.contacts_list_view_refresh);
		xListView.setDivider(null);
		xListView.setXListViewListener(ContactActivity.this);
		xListView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {

				Intent intent = new Intent();
				SharedPreferences preferences = getActivity()
						.getSharedPreferences(Urlinterface.SHARED,
								Context.MODE_PRIVATE);
				Editor editor = preferences.edit();
				editor.putInt("contact_id", contactsList.get(position - 1)
						.getContactId());
				editor.commit();
				intent.putExtra("contact_id", contactsList.get(position - 1)
						.getContactId());
				intent.setClass(getActivity(), ContactInfoActivity.class);
				startActivity(intent);
				index = position - 1;
			}
		});
		xListView.setAdapter(adapter2);

	}

	/**
	 * 设置button的 宽度 以及监听
	 * 
	 * 
	 */
	private void setButton() {
		Display display = getActivity().getWindowManager().getDefaultDisplay();
		int width = display.getWidth();
		int height = display.getHeight();
		int width0 = 4; // 边框宽度
		int width1 = 20; // 外部边框距左右边界距离
		int hight0 = 70; // 外部边框高度
		LinearLayout a_layout = (LinearLayout) rootView
				.findViewById(R.id.a_layout);
		LayoutParams param = (LayoutParams) a_layout.getLayoutParams();
		param.topMargin = 10;
		param.bottomMargin = 10;
		param.leftMargin = width1;
		param.rightMargin = width1;
		if (height == 1280 && width == 720) {
			hight0 = 70;
			param.leftMargin = width1;
			param.rightMargin = width1 + 1;
		} else if (height == 854 && width == 480) {
			hight0 = 45;
			width1 = 18;
			width0 = 2;
			param.leftMargin = width1;
			param.rightMargin = width1 - 1;
		}else if (height == 800 && width == 480) {
			hight0 = 45;
			width1 = 18;
			width0 = 2;
			param.leftMargin = width1;
			param.rightMargin = width1 - 1;
		}  else if (height >= 1750 && height <= 1920 && width == 1080) {
			width1 = 40; // 外部边框距左右边界距离
			hight0 = 100;
			param.leftMargin = width1;
			param.rightMargin = width1;
		}
		int hight1 = hight0 - width0 * 2; // button高度

		param.height = hight0;

		view2 = (Button) rootView.findViewById(R.id.view_2);
		view3 = (Button) rootView.findViewById(R.id.view_3);
		view2.setWidth(width0);
		view3.setWidth(width0);
		view2.setHeight(hight1);
		view3.setHeight(hight1);

		int button_width = (width - width1 * 2 - 4 * width0) / 3;
		button_recently = (Button) rootView.findViewById(R.id.button_recently);
		button_trading = (Button) rootView.findViewById(R.id.button_trading);
		button_subscription = (Button) rootView
				.findViewById(R.id.button_subscription);
		btnList.add(button_recently);
		btnList.add(button_trading);
		btnList.add(button_subscription);
		for (int i = 0; i < btnList.size(); i++) {
			btnList.get(i).setWidth(button_width);
			btnList.get(i).setHeight(hight1);
		}
		button_recently.setOnClickListener(listener_1);
		button_trading.setOnClickListener(listener_2);
		button_subscription.setOnClickListener(listener_3);

		if (version < 4) {
			button_recently.setBackgroundResource(R.drawable.left_shape_red2);
			button_subscription
					.setBackgroundResource(R.drawable.right_shape_white2);
		}

	}

	// 最近
	private View.OnClickListener listener_1 = new View.OnClickListener() {
		@Override
		public void onClick(View v) {
			buttonNumber = 0;
			index = 0;
			switchButton(buttonNumber);
		}
	};
	// 交易
	private View.OnClickListener listener_2 = new View.OnClickListener() {
		@Override
		public void onClick(View v) {
			buttonNumber = 1;
			index = 0;
			switchButton(buttonNumber);
		}
	};
	// 订阅
	private View.OnClickListener listener_3 = new View.OnClickListener() {
		@Override
		public void onClick(View v) {
			buttonNumber = 2;
			index = 0;
			switchButton(buttonNumber);
		}
	};

	private void switchButton(int buttonNumber) {
		onLoad();
		switch (buttonNumber) {
		case 0:
			setButtonColor(buttonNumber);

			
			contactsList1 = db.queryContactList(user_id);
			Collections.sort(contactsList1, longDataComparator);
			contactsList.clear();
			if (contactsList1.size() > 20) { // 20个以上进行排序
				for (int i = 0; i < 20; i++) {
					contactsList.add(contactsList1.get(i));
				}
			} else {
				for (int i = 0; i < contactsList1.size(); i++) {
					contactsList.add(contactsList1.get(i));
				}
			}
//			ShortContactPojo coPojo = new ShortContactPojo(0, "", "系统消息",
//					"系统消息", "", 2, 0, "", 0, "", "");
//			contactsList.add(0, coPojo);
			adapter2.updateListView(contactsList);
			xListView.setSelection(index);
			onRefresh_number = true;
			break;
		case 1:
			setButtonColor(buttonNumber);
			contactsList1 = db.queryContactList(user_id);
			contactsList.clear();
			if (contactsList1.size() != 0) {
				for (int i = 0; i < contactsList1.size(); i++) {
					String str = FuXunTools.toNumber(contactsList1.get(i)
							.getSource());
					if (FuXunTools.isExist(str, 2, 3)) {
						contactsList.add(contactsList1.get(i));
					}
				}
				Collections.sort(contactsList, orderTimeLongDataComparator);
			}
			adapter2.updateListView(contactsList);
			xListView.setSelection(index);
			onRefresh_number = true;
			break;
		case 2:
			setButtonColor(buttonNumber);
			int a = contactsList.size();
			Log.i("linshi1", "----1-----2-----3-------contactsList.size()---"
					+ a);
			contactsList1 = db.queryContactList(user_id);
			contactsList.clear();
			if (contactsList1.size() != 0) {
				for (int i = 0; i < contactsList1.size(); i++) {
					Log.i("linshi1",
							"----1-----2-----3-------contactsList1.get(i).getSubscribeTime().---"
									+ contactsList1.get(0).getSubscribeTime());
					String str = FuXunTools.toNumber(contactsList1.get(i)
							.getSource());
					if (FuXunTools.isExist(str, 0, 1)) {
						contactsList.add(contactsList1.get(i));
					}
				}
				Log.i("linshi",
						"----1-----2-----3---------------------------------");
				Collections.sort(contactsList, subscribeTimeLongDataComparator);
			}
			adapter2.updateListView(contactsList);
			xListView.setSelection(index);
			onRefresh_number = true;
			break;

		default:
			break;
		}

	}

	private void setButtonColor(int buttonNumber) {
		for (int i = 0; i < btnList.size(); i++) {
			if (buttonNumber == i) {
				btnList.get(i).setTextColor(
						this.getResources().getColor(R.color.white));
			} else {
				btnList.get(i).setTextColor(
						this.getResources().getColor(R.color.red_block));
			}
		}
		btnList.get(0).setBackgroundResource(R.drawable.left_shape_white);

		btnList.get(1).setBackgroundResource(R.drawable.middle_shape_white);
		btnList.get(2).setBackgroundResource(R.drawable.right_shape_white);
		if (version < 4) {
			btnList.get(0).setBackgroundResource(R.drawable.left_shape_white2);
			btnList.get(2).setBackgroundResource(R.drawable.right_shape_white2);
		}
		switch (buttonNumber) {
		case 0:
			btnList.get(buttonNumber).setBackgroundResource(
					R.drawable.left_shape_red);
			if (version < 4) {
				button_recently
						.setBackgroundResource(R.drawable.left_shape_red2);
			}
			break;
		case 1:
			btnList.get(buttonNumber).setBackgroundResource(
					R.drawable.middle_shape_red);
			break;
		case 2:
			btnList.get(buttonNumber).setBackgroundResource(
					R.drawable.right_shape_red);
			if (version < 4) {
				btnList.get(buttonNumber).setBackgroundResource(
						R.drawable.right_shape_red2);
			}
			break;
		default:
			break;
		}
	}

	private void onLoad() {
		xListView.stopRefresh();
	}

	public void onRefresh() {
		index = 0;
		if (onRefresh_number) {

			if (FuXunTools.isConnect(getActivity())) {
				onRefresh_number = false;
				getContacts2();
			} else {
				onRefresh_number = true;
				Toast.makeText(getActivity(), R.string.no_internet,
						Toast.LENGTH_SHORT).show();
			}
		}
		else {
			Toast.makeText(getActivity(), "刷新频繁，请稍后再试",
					Toast.LENGTH_SHORT).show();
		}
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		if (db != null) {
			db.closeDB();
		}
	}

}
