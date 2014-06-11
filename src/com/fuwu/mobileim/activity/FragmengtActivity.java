package com.fuwu.mobileim.activity;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;
import android.text.Editable;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.TextWatcher;
import android.text.style.AbsoluteSizeSpan;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.mobstat.StatService;
import com.fuwu.mobileim.R;
import com.fuwu.mobileim.adapter.ContactAdapter;
import com.fuwu.mobileim.adapter.FragmentViewPagerAdapter;
import com.fuwu.mobileim.model.Models.ContactRequest;
import com.fuwu.mobileim.model.Models.ContactResponse;
import com.fuwu.mobileim.pojo.ContactPojo;
import com.fuwu.mobileim.util.DBManager;
import com.fuwu.mobileim.util.FxApplication;
import com.fuwu.mobileim.util.HttpUtil;
import com.fuwu.mobileim.util.Urlinterface;
import com.fuwu.mobileim.view.CharacterParser;
import com.igexin.sdk.PushManager;

/**
 * @作者 马龙
 * @时间 创建时间：2014-5-27 下午6:36:44
 */
public class FragmengtActivity extends FragmentActivity {
	private ViewPager vp;
	private List<Fragment> list = new ArrayList<Fragment>();
	private ImageView contact_search; // 搜索功能 图标
	private RelativeLayout main_search;// 搜索框全部
	private TextView contact_search_edittext;// 搜索框输入框
	private ImageView contact_search_empty;// 搜索框 清空图标
	private Button contact_search_cancel;// 搜索功能 取消按钮
	private ListView contacts_search_listview;// 搜索到的内容 listview
	private LinearLayout contacts_search_linearLayout;// 搜索 内容显示部分
	private FxApplication fxApplication;
	private List<ContactPojo> SourceDateList;
	private ContactAdapter adapter;
	private ImageView cursor;
	private RequstReceiver mReuRequstReceiver;
	private int offset = 0;
	private int currIndex = 0;
	private int cursorW = 0;
	private List<TextView> btnList = new ArrayList<TextView>();
	private TextView menu_talk, menu_address_book, menu_settings;
	private CharacterParser characterParser;
	private DBManager db;
	private List<ContactPojo> contactsList = new ArrayList<ContactPojo>();; // 联系人arraylist数组
	private ProgressDialog prodialog;
	private static Bitmap bm = null;
	private int user_number1 = 0;
	private int user_number2 = 0;
	private SharedPreferences spf;
	private Handler handler = new Handler() {
		/*
		 * (non-Javadoc)
		 * 
		 * @see android.os.Handler#handleMessage(android.os.Message)
		 */
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case 0:// 调用加载头像的方法
				prodialog.dismiss();

				for (int i = 0; i < contactsList.size(); i++) {
					String face_str = contactsList.get(i).getUserface_url();
					db.addContact(fxApplication.getUser_id(),
							contactsList.get(i));
					if (face_str.length() > 4) {
						user_number2 = user_number2 + 1;
					}
				}
				if (user_number2 > 0) {
					getUserBitmap();
				} else {
					prodialog.dismiss();
				}
				list.get(1).onStart();

				break;
			case 1:
				user_number1 = user_number1 + 1;
				if (user_number1 == contactsList.size()) {
					prodialog.dismiss();
				}
				list.get(1).onStart();
				break;
			case 5:
				prodialog.dismiss();
				Toast.makeText(getApplicationContext(), "当前用户没有联系人",
						Toast.LENGTH_SHORT).show();
				break;
			case 6:
				prodialog.dismiss();
				Toast.makeText(getApplicationContext(), "请求失败",
						Toast.LENGTH_SHORT).show();
				break;
			case 7:
				prodialog.dismiss();
				Toast.makeText(getApplicationContext(), "网络错误",
						Toast.LENGTH_SHORT).show();
				break;
			}
		}
	};

	@Override
	protected void onCreate(Bundle arg0) {
		super.onCreate(arg0);
		setContentView(R.layout.main);
		getButton();
		spf = getSharedPreferences(Urlinterface.SHARED, 0);
		vp = (ViewPager) findViewById(R.id.main_viewPager);
		list.add(new TalkActivity());
		list.add(new ContactActivity());
		list.add(new SettingsActivity());

		FragmentViewPagerAdapter adapter = new FragmentViewPagerAdapter(list,
				vp, this.getSupportFragmentManager());
		adapter.setOnExtraPageChangeListener(new FragmentViewPagerAdapter.OnExtraPageChangeListener() {
			@Override
			public void onExtraPageSelected(int i) {
				super.onExtraPageSelected(i);
				if (i == 0) {
					list.get(0).onStart();
				}
				changeLocation(i);
				changeColor(i);
				if (i == 1) {
					contact_search.setVisibility(View.VISIBLE);
				} else {
					contact_search.setVisibility(View.GONE);
				}
			}
		});
		Intent i = new Intent();
		i.setClass(this, RequstService.class);
		startService(i);

		contact_search = (ImageView) findViewById(R.id.contact_search);
		fxApplication = (FxApplication) getApplication();
		mReuRequstReceiver = new RequstReceiver();
		fxApplication.getActivityList().add(this);
		searchMethod();

		changeTitleStyle();
		setEdittextListening();
		InitImageView();

		Log.i("Max",
				fxApplication.getToken() + "/" + fxApplication.getUser_id());
		// contactInformation();
		contactInformation();

		// 个推SDK初始化
		PushManager.getInstance().initialize(this.getApplicationContext());
	}

	/**
	 * 查询本地联系人信息，没有的话，则请求服务器并保存到本地
	 * 
	 * 
	 */
	private void contactInformation() {
		// 实例化汉字转拼音类
		characterParser = CharacterParser.getInstance();
		db = new DBManager(this);
		contactsList = db.queryContactList(fxApplication.getUser_id());
		Log.i("11", contactsList.size() + "-----------1");
		if (contactsList.size() == 0) {
			prodialog = new ProgressDialog(FragmengtActivity.this);
			prodialog.setMessage("正在加载数据，请稍后...");
			prodialog.setCanceledOnTouchOutside(false);
			prodialog.show();
			Thread thread = new Thread(new getContacts());
			thread.start();
		} else {
			Log.i("Ax", "加载本地联系人");
		}
	}

	/**
	 * 加载联系人头像，并保存到本地
	 * 
	 * 
	 */
	private void getUserBitmap() {
		ExecutorService singleThreadExecutor = Executors
				.newSingleThreadExecutor();
		for (int i = 0; i < contactsList.size(); i++) {
			final int index = i;
			final int contactId = contactsList.get(i).getContactId();
			final String url = contactsList.get(i).getUserface_url();
			singleThreadExecutor.execute(new Runnable() {

				@Override
				public void run() {
					try {
						URL myurl = new URL(url);
						// 获得连接
						HttpURLConnection conn = (HttpURLConnection) myurl
								.openConnection();
						conn.setConnectTimeout(6000);// 设置超时
						conn.setDoInput(true);
						conn.setUseCaches(false);// 不缓存
						conn.connect();
						InputStream is = conn.getInputStream();// 获得图片的数据流

						BitmapFactory.Options options = new BitmapFactory.Options();
						options.inJustDecodeBounds = false;
						options.inSampleSize = 1;
						bm = BitmapFactory.decodeStream(is, null, options);
						Log.i("linshi", bm.getWidth() + "---" + bm.getHeight());
						is.close();
						if (bm != null) {
							Log.i("linshi",
									bm.getWidth() + "---2---" + bm.getHeight());
							File f = new File(Urlinterface.head_pic, contactId
									+ "");

							if (f.exists()) {
								f.delete();
							}
							if (!f.getParentFile().exists()) {
								f.getParentFile().mkdirs();
							}
							Log.i("linshi", "----1");
							FileOutputStream out = new FileOutputStream(f);
							Log.i("linshi", "----6");
							bm.compress(Bitmap.CompressFormat.PNG, 60, out);
							out.flush();
							out.close();
							Log.i("linshi", "已经保存");

						}
						handler.sendEmptyMessage(1);
					} catch (Exception e) {
						// TODO Auto-generated catch block
						handler.sendEmptyMessage(1);
					}
				}
			});
		}
	}

	/**
	 * 
	 * 第一次 获得所有联系人
	 * 
	 * 
	 */

	class getContacts implements Runnable {
		public void run() {
			try {
				ContactRequest.Builder builder = ContactRequest.newBuilder();
				builder.setUserId(fxApplication.getUser_id());
				builder.setToken(fxApplication.getToken());
				Log.i("Ax", "User_id:" + fxApplication.getUser_id() + "--Token"
						+ fxApplication.getToken());
				Log.i("Ax", "加载网络联系人---");
				ContactRequest response = builder.build();

				byte[] by = HttpUtil.sendHttps(response.toByteArray(),
						Urlinterface.getContacts, "POST");
				if (by != null && by.length > 0) {

					ContactResponse res = ContactResponse.parseFrom(by);
					if (res.getIsSucceed()) {

						for (int i = 0; i < res.getContactsCount(); i++) {
							Log.i("linshi", res.getContactsCount() + "---" + i
									+ "---" + res.getContacts(i).getName());
							int contactId = res.getContacts(i).getContactId();
							String name = res.getContacts(i).getName();
							String customName = res.getContacts(i)
									.getCustomName();
							String sortKey = null;
							if (customName != null && customName.length() > 0) {
								sortKey = findSortKey(customName);
							} else {
								sortKey = findSortKey(name);
							}
							String userface_url = res.getContacts(i)
									.getTileUrl();
							int sex = res.getContacts(i).getGender()
									.getNumber();
							int source = res.getContacts(i).getSource();
							String lastContactTime = res.getContacts(i)
									.getLastContactTime();// 2014-05-27 11:42:18
							Boolean isblocked = res.getContacts(i)
									.getIsBlocked();
							Boolean isprovider = res.getContacts(i)
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

							String lisence = res.getContacts(i).getLisence();
							String individualResume = res.getContacts(i)
									.getIndividualResume();
							ContactPojo coPojo = new ContactPojo(contactId,
									sortKey, name, customName, userface_url,
									sex, source, lastContactTime, isBlocked,
									isProvider, lisence, individualResume);
							contactsList.add(coPojo);

						}

						SharedPreferences preferences = getSharedPreferences(
								Urlinterface.SHARED, Context.MODE_PRIVATE);
						Editor editor = preferences.edit();
						editor.putString("contactTimeStamp", res.getTimeStamp());

						editor.commit();

						Message msg = new Message();// 创建Message 对象
						msg.what = 0;
						handler.sendMessage(msg);
					} else {
						handler.sendEmptyMessage(5);
					}

				} else {
					handler.sendEmptyMessage(6);
				}

			} catch (Exception e) {
				// prodialog.dismiss();
				handler.sendEmptyMessage(7);
			}
		}
	}

	/**
	 * 获得首字母
	 */
	public String findSortKey(String str) {
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
	 * 获得button 以及设置监听
	 * 
	 * 
	 */
	private void getButton() {
		menu_talk = (TextView) findViewById(R.id.menu_talk);
		menu_address_book = (TextView) findViewById(R.id.menu_address_book);
		menu_settings = (TextView) findViewById(R.id.menu_settings);
		btnList.add(menu_talk);
		btnList.add(menu_address_book);
		btnList.add(menu_settings);
		menu_talk.setOnClickListener(new menuOnclick(0));
		menu_address_book.setOnClickListener(new menuOnclick(1));
		menu_settings.setOnClickListener(new menuOnclick(2));
	}

	/**
	 * 改变文本 的颜色
	 * 
	 * 
	 */
	private void changeColor(int buttonNumber) {

		for (int i = 0; i < btnList.size(); i++) {
			if (buttonNumber == i) {
				btnList.get(i).setTextColor(
						this.getResources().getColor(R.color.red_block));
			} else {
				btnList.get(i).setTextColor(
						this.getResources().getColor(R.color.text_color));

			}
		}
	}

	/**
	 * 初始化动画
	 */
	public void InitImageView() {
		cursor = (ImageView) findViewById(R.id.main_cursor);
		DisplayMetrics dm = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(dm);
		cursorW = cursor.getWidth();
		int screenW = dm.widthPixels;// 获取分辨率宽度
		offset = (screenW / 3 - cursorW) / 2;// 计算偏移量
		Matrix matrix = new Matrix();
		matrix.postTranslate(offset, 0);
		cursor.setImageMatrix(matrix);// 设置动画初始位置
	}

	// 自定义更改图片位置
	public void changeLocation(int index) {
		int one = offset * 2 + cursorW;// 页卡1 -> 页卡2 偏移量
		Animation animation = new TranslateAnimation(one * currIndex, one
				* index, 0, 0);// 显然这个比较简洁，只有一行代码。
		animation.setFillAfter(true);// True:图片停在动画结束位置
		currIndex = index;
		animation.setDuration(300);
		vp.setCurrentItem(index);
		cursor.startAnimation(animation);
	}

	/**
	 * 改变“褔务网v1.0” 的 样式
	 */
	public void changeTitleStyle() {
		int width = fxApplication.getWidth();
		TextView tv = (TextView) findViewById(R.id.contact_title);
		String tv_str = (String) tv.getText().toString();
		SpannableStringBuilder style2 = new SpannableStringBuilder(tv_str);

		switch (width) {
		case 480:
			style2.setSpan(new AbsoluteSizeSpan(30), 0, 3,
					Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
			style2.setSpan(new AbsoluteSizeSpan(20), 3, tv_str.length(),
					Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
			tv.setText(style2);
			break;

		default:
			style2.setSpan(new AbsoluteSizeSpan(40), 0, 3,
					Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
			style2.setSpan(new AbsoluteSizeSpan(25), 3, tv_str.length(),
					Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
			tv.setText(style2);
			break;
		}

	}

	/**
	 * 搜索相关设置
	 */
	public void searchMethod() {
		contacts_search_linearLayout = (LinearLayout) findViewById(R.id.contacts_search_linearLayout); //
		contacts_search_listview = (ListView) findViewById(R.id.contacts_search_list_view); // 搜索到的内容
																							// listview
		contact_search = (ImageView) findViewById(R.id.contact_search); // 搜索功能图标
		main_search = (RelativeLayout) findViewById(R.id.main_search);// 搜索框全部
		contact_search_edittext = (TextView) findViewById(R.id.contact_search_edittext);// 搜索框输入框
		contact_search_empty = (ImageView) findViewById(R.id.contact_search_empty);// 搜索框清空图标
		contact_search_cancel = (Button) findViewById(R.id.contact_search_cancel);// 搜索功能
																					// 取消按钮
		contact_search.setOnClickListener(listener1);
		contact_search_empty.setOnClickListener(listener2);
		contact_search_cancel.setOnClickListener(listener3);
		contacts_search_listview.setDivider(null);
		contacts_search_listview
				.setOnItemClickListener(new OnItemClickListener() {

					@Override
					public void onItemClick(AdapterView<?> parent, View view,
							int position, long id) {
						Intent intent = new Intent();
						intent.putExtra("contact_id", contactsList
								.get(position).getContactId());
						intent.setClass(FragmengtActivity.this,
								ChatActivity.class);
						startActivity(intent);
						contact_search_edittext.setText("");
					}
				});

	}

	/*
	 * 搜索按妞
	 */
	private View.OnClickListener listener1 = new View.OnClickListener() {
		@Override
		public void onClick(View v) {
			vp.setVisibility(View.GONE);
			main_search.setVisibility(View.VISIBLE);
			contacts_search_linearLayout.setVisibility(View.VISIBLE);

			final Animation translateAnimation = new TranslateAnimation(720, 0,
					0, 0); // 移动动画效果

			translateAnimation.setDuration(100); // 设置动画持续时间
			main_search.setAnimation(translateAnimation); // 设置动画效果
			translateAnimation.startNow(); // 启动动画
			// 模拟
			SourceDateList = db.queryContactList(fxApplication.getUser_id());
		}
	};
	/*
	 * 清空搜索框
	 */
	private View.OnClickListener listener2 = new View.OnClickListener() {
		@Override
		public void onClick(View v) {
			contact_search_edittext.setText("");
		}
	};
	/*
	 * 取消
	 */
	private View.OnClickListener listener3 = new View.OnClickListener() {
		@Override
		public void onClick(View v) {
			vp.setVisibility(View.VISIBLE);
			main_search.setVisibility(View.GONE);
			contacts_search_linearLayout.setVisibility(View.GONE);
			contact_search_edittext.setText("");
		}
	};

	/**
	 * 搜索输入框文本监听
	 */
	public void setEdittextListening() {
		contact_search_edittext.addTextChangedListener(new TextWatcher() {
			public void onTextChanged(CharSequence s, int start, int before,
					int count) {
				String content = contact_search_edittext.getText().toString();
				adapter = new ContactAdapter(FragmengtActivity.this,
						findSimilarContacts(content), -1);
				contacts_search_listview.setAdapter(adapter);
			}

			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
			}

			public void afterTextChanged(Editable s) {
			}
		});
	}

	public List<ContactPojo> findSimilarContacts(String et) {
		contactsList = new ArrayList<ContactPojo>();
		if (et.length() > 0) {
			for (int i = 0; i < SourceDateList.size(); i++) {
				if (SourceDateList.get(i).getName().indexOf(et) != -1) {
					contactsList.add(SourceDateList.get(i));
				}
			}
		}

		return contactsList;
	}

	class menuOnclick implements OnClickListener {
		private int index = 0;

		public menuOnclick(int index) {
			super();
			this.index = index;
		}

		@Override
		public void onClick(View v) {
			if (index != currIndex) {
				changeLocation(index);
			}
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
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

	class RequstReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			list.get(0).onStart();
		}
	}

	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			spf.edit().putString("Token", "null").commit();
			System.exit(0);

			return true;
		}
		return super.onKeyDown(keyCode, event);
	}
}
