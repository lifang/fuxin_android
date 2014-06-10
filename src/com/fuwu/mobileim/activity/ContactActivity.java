package com.fuwu.mobileim.activity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.TextView;
import android.widget.Toast;

import com.fuwu.mobileim.R;
import com.fuwu.mobileim.adapter.ContactAdapter;
import com.fuwu.mobileim.model.Models.ContactRequest;
import com.fuwu.mobileim.model.Models.ContactResponse;
import com.fuwu.mobileim.pojo.ContactPojo;
import com.fuwu.mobileim.util.DBManager;
import com.fuwu.mobileim.util.FuXunTools;
import com.fuwu.mobileim.util.FxApplication;
import com.fuwu.mobileim.util.HttpUtil;
import com.fuwu.mobileim.util.LongDataComparator;
import com.fuwu.mobileim.util.Urlinterface;
import com.fuwu.mobileim.view.CharacterParser;
import com.fuwu.mobileim.view.PinyinComparator;
import com.fuwu.mobileim.view.SideBar;
import com.fuwu.mobileim.view.SideBar.OnTouchingLetterChangedListener;
import com.fuwu.mobileim.view.XListView;
import com.fuwu.mobileim.view.XListView.IXListViewListener;

public class ContactActivity extends Fragment implements IXListViewListener {

	private DBManager db;
	private FxApplication fxApplication;
	private ListView sortListView;// 普通的 listview，最近，订阅，交易 这三个部分使用
	private XListView xListView;// 可上拉刷新 的 listview ，全部 部分使用
	private SideBar sideBar;
	private TextView dialog;
	private ContactAdapter adapter;
	private View rootView;
	List<ContactPojo> contactsList1 = new ArrayList<ContactPojo>();
	/**
	 * 弹出式分组的布局
	 */
	private RelativeLayout sectionToastLayout;
	private TextView sectionToastText;
	/**
	 * 汉字转换成拼音的类
	 */
	private CharacterParser characterParser;
	private List<ContactPojo> contactsList; // 联系人arraylist数组

	/**
	 * 根据拼音来排列ListView里面的数据类
	 */
	private PinyinComparator pinyinComparator;
	private LongDataComparator longDataComparator;
	/**
	 * 定义字母表的排序规则
	 */
	private Button button_all, button_recently, button_trading,
			button_subscription;
	private Button view1, view2, view3;
	int width;
	private int buttonNumber = -1;
	private List<Button> btnList = new ArrayList<Button>();
	private String alphabet = "ABCDEFGHIJKLMNOPQRSTUVWXYZ#";
	SideBar b;
	private Handler handler = new Handler() {
		/*
		 * (non-Javadoc)
		 * 
		 * @see android.os.Handler#handleMessage(android.os.Message)
		 */
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case 0:
				contactsList = db.queryContactList(fxApplication.getUser_id());
				//
				// 根据a-z进行排序源数据
				if (contactsList.size() > 1) { // 2个以上进行排序
					Collections.sort(contactsList, pinyinComparator);
				}
				adapter = new ContactAdapter(getActivity(), contactsList, 1);
				xListView.setAdapter(adapter);
				onLoad();
				break;
			case 1:
				for (int i = 0; i < contactsList.size(); i++) {
					db.modifyContact(fxApplication.getUser_id(),
							contactsList.get(i));
					String url = Urlinterface.IP
							+ contactsList.get(i).getUserface_url();
					FuXunTools.getBitmap(contactsList.get(i).getContactId(),
							url);
				}
				contactsList = db.queryContactList(fxApplication.getUser_id());

				// 根据a-z进行排序源数据
				if (contactsList.size() > 1) { // 2个以上进行排序
					Collections.sort(contactsList, pinyinComparator);
				}

				// fxApplication.setContactsList(contactsList);
				adapter = new ContactAdapter(getActivity(), contactsList, 1);
				xListView.setAdapter(adapter);
				onLoad();
				break;
			case 6:
				Toast.makeText(getActivity(), "请求失败", Toast.LENGTH_SHORT)
						.show();
				break;
			case 7:
				Toast.makeText(getActivity(), "网络错误", Toast.LENGTH_SHORT)
						.show();
				break;
			}
		}
	};

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		rootView = inflater
				.inflate(R.layout.contact_activity, container, false);
		fxApplication = (FxApplication) getActivity().getApplication();
		db = new DBManager(getActivity());
		initViews();
		Display display = getActivity().getWindowManager().getDefaultDisplay();
		width = display.getWidth();
		setButton();
		return rootView;
	}

	@Override
	public void onStart() {
		// TODO Auto-generated method stub
		super.onStart();
		Log.i("11", "-----------");
		handler.sendEmptyMessage(0);
	}

	/**
	 * 
	 * 第二次 获得所有联系人
	 * 
	 * 
	 */

	class getContacts2 implements Runnable {
		public void run() {
			try {
				SharedPreferences preferences = getActivity()
						.getSharedPreferences(Urlinterface.SHARED,
								Context.MODE_PRIVATE);

				String timeStamp = preferences
						.getString("contactTimeStamp", "");
				ContactRequest.Builder builder = ContactRequest.newBuilder();
				builder.setUserId(fxApplication.getUser_id());
				builder.setToken(fxApplication.getToken());
				builder.setTimeStamp(timeStamp);
				Log.i("1", "User_id:" + fxApplication.getUser_id() + "--Token"
						+ fxApplication.getToken());

				ContactRequest response = builder.build();

				byte[] by = HttpUtil.sendHttps(response.toByteArray(),
						Urlinterface.getContacts, "POST");
				if (by != null && by.length > 0) {
					contactsList = new ArrayList<ContactPojo>();
					ContactResponse res = ContactResponse.parseFrom(by);
					for (int i = 0; i < res.getContactsCount(); i++) {
						int contactId = res.getContacts(i).getContactId();
						String name = res.getContacts(i).getName();
						String customName = res.getContacts(i).getCustomName();
						String sortKey = null;
						if (customName != null && customName.length() > 0) {
							sortKey = findSortKey(customName);
						} else {
							sortKey = findSortKey(name);
						}
						String userface_url = res.getContacts(i).getTileUrl();
						int sex = res.getContacts(i).getGender().getNumber();
						int source = res.getContacts(i).getSource();
						String lastContactTime = res.getContacts(i)
								.getLastContactTime();// 2014-05-27 11:42:18
						Boolean isblocked = res.getContacts(i).getIsBlocked();
						Boolean isprovider = res.getContacts(i).getIsProvider();
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
						String lisence = res.getContacts(i).getLisence();
						String individualResume = res.getContacts(i)
								.getIndividualResume();
						ContactPojo coPojo = new ContactPojo(contactId,
								sortKey, name, customName, userface_url, sex,
								source, lastContactTime, isBlocked, isProvider,
								lisence, individualResume);
						contactsList.add(coPojo);

					}

					SharedPreferences preferences2 = getActivity()
							.getSharedPreferences(Urlinterface.SHARED,
									Context.MODE_PRIVATE);
					Editor editor = preferences2.edit();
					editor.putString("contactTimeStamp", res.getTimeStamp());

					editor.commit();
				}
				Message msg = new Message();// 创建Message 对象
				msg.what = 1;
				handler.sendMessage(msg);

				// handler.sendEmptyMessage(0);
			} catch (Exception e) {
				// prodialog.dismiss();
				// handler.sendEmptyMessage(7);
				onLoad();
			}
		}
	}

	private void initViews() {
		contactsList = new ArrayList<ContactPojo>();

		// 实例化汉字转拼音类
		characterParser = CharacterParser.getInstance();

		pinyinComparator = new PinyinComparator();
		// Thread thread = new Thread(new getContacts());
		// thread.start();

		sectionToastLayout = (RelativeLayout) rootView
				.findViewById(R.id.section_toast_layout);
		sectionToastText = (TextView) rootView
				.findViewById(R.id.section_toast_text);
		sideBar = (SideBar) rootView.findViewById(R.id.sidrbar);
		// 设置右侧触摸监听
		sideBar.setOnTouchingLetterChangedListener(new OnTouchingLetterChangedListener() {

			@Override
			public void onTouchingLetterChanged(String s) {

				sideBar.setTextView(sectionToastText);
				sideBar.setRelativeLayout(sectionToastLayout);
				float alphabetHeight = sideBar.getHeight();
				int pos = getPositionForAlphabet(s);
				float y = (pos * 100 / 27f) * alphabetHeight / 100;
				LayoutParams param = (LayoutParams) sectionToastLayout
						.getLayoutParams();
				param.rightMargin = 60;
				param.topMargin = (int) y;
				// sectionToastText.setText(s);
				// 该字母首次出现的位置
				int position = adapter.getPositionForSection(s.charAt(0));
				if (position != -1) {
					sortListView.setSelection(position);
					xListView.setSelection(position);

					// TextView tv= (TextView)
					// sortListView.getChildAt(position).findViewById(R.id.sort_key);
					// String tv_str = (String) tv.getText().toString();
					// SpannableStringBuilder style2 = new
					// SpannableStringBuilder(tv_str);
					// style2.setSpan(new ForegroundColorSpan(Color.RED), 0,
					// 1, Spannable.SPAN_EXCLUSIVE_INCLUSIVE);
					// tv.setText(style2);
				}

			}
		});

		sortListView = (ListView) rootView
				.findViewById(R.id.contacts_list_view);
		sortListView.setDivider(null);
		sortListView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {

				Intent intent = new Intent();
				intent.putExtra("contact_id", contactsList.get(position)
						.getContactId());
				intent.setClass(getActivity(), ChatActivity.class);
				startActivity(intent);
			}
		});
		xListView = (XListView) rootView
				.findViewById(R.id.contacts_list_view_refresh);
		xListView.setDivider(null);
		xListView.setXListViewListener(ContactActivity.this);
		xListView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {

				Intent intent = new Intent();
				intent.putExtra("contact_id", contactsList.get(position - 1)
						.getContactId());
				intent.setClass(getActivity(), ChatActivity.class);
				startActivity(intent);
			}
		});
		// handler.sendEmptyMessage(0);

	}

	/**
	 * 获得首字母
	 */
	private String findSortKey(String str) {
		if (str.length() > 0) {

			String pinyin = characterParser.getSelling(str);
			String sortString = pinyin.substring(0, 1).toUpperCase();
			// 正则表达式，判断首字母是否是英文字母
			if (sortString.matches("[A-Z]")) {
				return sortString.toUpperCase();
			} else {
				return "#";
			}
		} else {
			return "#";
		}
	}

	/**
	 * 设置button的 宽度 以及监听
	 * 
	 * 
	 */
	private void setButton() {
		int width0 = 4; // 边框宽度
		int width1 = 20; // 外部边框距左右边界距离
		int hight0 = 70; // 外部边框高度
		int hight1 = hight0 - width0 * 2; // button高度
		LinearLayout a_layout = (LinearLayout) rootView
				.findViewById(R.id.a_layout);
		LayoutParams param = (LayoutParams) a_layout.getLayoutParams();
		param.leftMargin = 20;
		param.rightMargin = 20;
		param.topMargin = 10;
		param.bottomMargin = 10;
		param.height = hight0;

		view1 = (Button) rootView.findViewById(R.id.view_1);
		view2 = (Button) rootView.findViewById(R.id.view_2);
		view3 = (Button) rootView.findViewById(R.id.view_3);
		view1.setWidth(width0);
		view2.setWidth(width0);
		view3.setWidth(width0);
		view1.setHeight(hight1);
		view2.setHeight(hight1);
		view3.setHeight(hight1);

		int button_width = (width - width1 * 2 - 5 * width0) / 4;
		button_all = (Button) rootView.findViewById(R.id.button_all);
		button_recently = (Button) rootView.findViewById(R.id.button_recently);
		button_trading = (Button) rootView.findViewById(R.id.button_trading);
		button_subscription = (Button) rootView
				.findViewById(R.id.button_subscription);
		btnList.add(button_all);
		btnList.add(button_recently);
		btnList.add(button_trading);
		btnList.add(button_subscription);
		for (int i = 0; i < btnList.size(); i++) {
			btnList.get(i).setWidth(button_width);
			btnList.get(i).setHeight(hight1);
		}
		button_all.setOnClickListener(listener_0);
		button_recently.setOnClickListener(listener_1);
		button_trading.setOnClickListener(listener_2);
		button_subscription.setOnClickListener(listener_3);
	}

	// 全部
	private View.OnClickListener listener_0 = new View.OnClickListener() {
		@Override
		public void onClick(View v) {
			buttonNumber = 0;
			setButtonColor(buttonNumber);
			xListView.setVisibility(View.VISIBLE);
			contactsList = db.queryContactList(fxApplication.getUser_id());
			Collections.sort(contactsList, pinyinComparator);
			adapter = new ContactAdapter(getActivity(), contactsList, 1);
			xListView.setAdapter(adapter);
			sortListView.setVisibility(View.GONE);
		}
	};
	// 最近
	private View.OnClickListener listener_1 = new View.OnClickListener() {
		@Override
		public void onClick(View v) {
			buttonNumber = 1;
			setButtonColor(buttonNumber);
			longDataComparator = new LongDataComparator();
			contactsList = new ArrayList<ContactPojo>();
			contactsList1 = db.queryContactList(fxApplication.getUser_id());
			if (contactsList1.size() > 20) { // 20个以上进行排序
				Collections.sort(contactsList1, longDataComparator);
				for (int i = 0; i < 20; i++) {
					contactsList.add(contactsList1.get(i));
				}

			} else {
				for (int i = 0; i < contactsList1.size(); i++) {
					contactsList.add(contactsList1.get(i));
				}
			}
			Collections.sort(contactsList, pinyinComparator);
			adapter = new ContactAdapter(getActivity(), contactsList, 0);
			sortListView.setVisibility(View.VISIBLE);
			xListView.setVisibility(View.GONE);
			sortListView.setAdapter(adapter);
		}
	};
	// 交易
	private View.OnClickListener listener_2 = new View.OnClickListener() {
		@Override
		public void onClick(View v) {
			buttonNumber = 2;
			setButtonColor(buttonNumber);
			contactsList = new ArrayList<ContactPojo>();
			contactsList1 = db.queryContactList(fxApplication.getUser_id());

			for (int i = 0; i < contactsList1.size(); i++) {
				String str = FuXunTools.toNumber(contactsList1.get(i)
						.getSource());
				if (FuXunTools.isExist(str, 0, 1)) {
					contactsList.add(contactsList1.get(i));
				}
			}
			Collections.sort(contactsList, pinyinComparator);
			adapter = new ContactAdapter(getActivity(), contactsList, 0);
			sortListView.setVisibility(View.VISIBLE);
			xListView.setVisibility(View.GONE);
			sortListView.setAdapter(adapter);
		}
	};
	// 订阅
	private View.OnClickListener listener_3 = new View.OnClickListener() {
		@Override
		public void onClick(View v) {
			buttonNumber = 3;
			setButtonColor(buttonNumber);
			contactsList = new ArrayList<ContactPojo>();
			contactsList1 = db.queryContactList(fxApplication.getUser_id());

			for (int i = 0; i < contactsList1.size(); i++) {
				String str = FuXunTools.toNumber(contactsList1.get(i)
						.getSource());
				if (FuXunTools.isExist(str, 2, 3)) {
					contactsList.add(contactsList1.get(i));
				}
			}
			Collections.sort(contactsList, pinyinComparator);
			adapter = new ContactAdapter(getActivity(), contactsList, 0);
			sortListView.setVisibility(View.VISIBLE);
			xListView.setVisibility(View.GONE);
			sortListView.setAdapter(adapter);
		}
	};

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
		btnList.get(2).setBackgroundResource(R.drawable.middle_shape_white);
		btnList.get(3).setBackgroundResource(R.drawable.right_shape_white);
		switch (buttonNumber) {
		case 0:
			btnList.get(buttonNumber).setBackgroundResource(
					R.drawable.left_shape_red);
			break;
		case 1:
		case 2:
			btnList.get(buttonNumber).setBackgroundResource(
					R.drawable.middle_shape_red);
			break;
		case 3:
			btnList.get(buttonNumber).setBackgroundResource(
					R.drawable.right_shape_red);
			break;
		default:
			break;
		}
	}

	/**
	 * 根据分类的首字母获取 字母表中的位置
	 */
	public int getPositionForAlphabet(String s) {
		for (int i = 0; i < alphabet.length(); i++) {
			String sortStr = alphabet.charAt(i) + "";
			if (s.equals(sortStr)) {
				return i + 1;
			}
		}
		return -1;
	}

	private void onLoad() {
		xListView.stopRefresh();
	}

	public void onRefresh() {
		// TODO Auto-generated method stub
		contactsList = new ArrayList<ContactPojo>();
		Thread thread = new Thread(new getContacts2());
		thread.start();
		Log.i("linshi", "1111111111111111111111111111");
	}

	@Override
	public void onResume() {
		super.onResume();
		if (!db.isOpen()) {
			db = new DBManager(getActivity());
		}
	}

}
