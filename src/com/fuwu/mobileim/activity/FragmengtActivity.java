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

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
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
import android.view.Display;
import android.view.KeyEvent;
import android.view.MotionEvent;
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
import com.fuwu.mobileim.adapter.FragmentViewPagerAdapter;
import com.fuwu.mobileim.adapter.SearchContactAdapter;
import com.fuwu.mobileim.model.Models.ContactRequest;
import com.fuwu.mobileim.model.Models.ContactResponse;
import com.fuwu.mobileim.model.Models.ProfileRequest;
import com.fuwu.mobileim.model.Models.ProfileResponse;
import com.fuwu.mobileim.pojo.ProfilePojo;
import com.fuwu.mobileim.pojo.ShortContactPojo;
import com.fuwu.mobileim.util.DBManager;
import com.fuwu.mobileim.util.FuXunTools;
import com.fuwu.mobileim.util.FxApplication;
import com.fuwu.mobileim.util.HttpUtil;
import com.fuwu.mobileim.util.ImageCacheUtil;
import com.fuwu.mobileim.util.Urlinterface;
import com.fuwu.mobileim.view.CharacterParser;
import com.igexin.sdk.PushManager;

/**
 * @作者 马龙
 * @时间 创建时间：2014-5-27 下午6:36:44
 */
public class FragmengtActivity extends FragmentActivity {
	private ProfilePojo profilePojo = new ProfilePojo();
	private int user_id;
	private String token;
	private ViewPager vp;
	private List<Fragment> list = new ArrayList<Fragment>();
	private LinearLayout countLinear;
	private TextView countText;
	private ImageView contact_search; // 搜索功能 图标
	private RelativeLayout main_search;// 搜索框全部
	private TextView contact_search_edittext;// 搜索框输入框
	private ImageView contact_search_empty;// 搜索框 清空图标
	private Button contact_search_cancel;// 搜索功能 取消按钮
	private ListView contacts_search_listview;// 搜索到的内容 listview
	private LinearLayout contacts_search_linearLayout;// 搜索 内容显示部分
	private FxApplication fxApplication;
	private List<ShortContactPojo> SourceDateList;
	private SearchContactAdapter adapter;
	private ImageView cursor;
	private RequstReceiver mReuRequstReceiver;
	private int offset = 0;
	private int currIndex = 0;
	private int cursorW = 0;
	private List<TextView> btnList = new ArrayList<TextView>();
	private TextView menu_talk, menu_address_book, menu_settings;
	private CharacterParser characterParser;
	private DBManager db;
	private List<ShortContactPojo> contactsLists = new ArrayList<ShortContactPojo>(); // 联系人arraylist数组
	private ProgressDialog prodialog;
	private static Bitmap bm = null;
	private int user_number1 = 0;
	private int user_number2 = 0;
	private SharedPreferences spf;
	int dataNumber = 0; // 0 数据没加载完，1 数据加载完
	int version;
	private Handler handler = new Handler() {
		/*
		 * (non-Javadoc)
		 * 
		 * @see android.os.Handler#handleMessage(android.os.Message)
		 */
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case 0://
					// prodialog.dismiss();

				for (int i = 0; i < contactsLists.size(); i++) {
					String face_str = contactsLists.get(i).getUserface_url();
					db.addContact(spf.getInt("user_id", 0),
							contactsLists.get(i));
					if (face_str.length() > 4) {
						user_number2 = user_number2 + 1;
					}
				}

				if (user_number2 > 0) {
					getUserBitmap();
				} else {
					getProfile();
				}

				break;
			case 1:
				user_number1 = user_number1 + 1;
				if (user_number1 == contactsLists.size()) {
					getProfile();
				}
				break;
			case 2:
				fxApplication.setUser_exit(true);
				putProfile(profilePojo);
				getBitmap_url(profilePojo.getTileUrl(), profilePojo.getUserId());// 加载个人头像

				break;
			case 3:
				prodialog.dismiss();
				Intent i = new Intent();
				i.setClass(FragmengtActivity.this, RequstService.class);
				startService(i);
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
				Toast.makeText(getApplicationContext(), R.string.no_internet,
						Toast.LENGTH_SHORT).show();
				break;
			case 8:
				int count = db.queryMessageCount(user_id);
				if (count > 0) {
					count = count > 99 ? 99 : count;
					countLinear.setVisibility(View.VISIBLE);
					countText.setText(count + "");
				} else {
					countLinear.setVisibility(View.GONE);
				}
				break;
			case 9:
				prodialog.dismiss();
				new Handler().postDelayed(new Runnable() {
					public void run() {
						Intent intent = new Intent(FragmengtActivity.this,
								LoginActivity.class);
						startActivity(intent);
						FragmengtActivity.this.finish();
					}
				}, 3500);
				spf.edit().putInt("exit_user_id", spf.getInt("user_id", 0))
						.commit();
				spf.edit()
						.putString("exit_Token", spf.getString("Token", "null"))
						.commit();
				spf.edit()
						.putString("exit_clientid",
								spf.getString("clientid", "")).commit();
				spf.edit().putInt("user_id", 0).commit();
				spf.edit().putString("Token", "null").commit();
				spf.edit().putString("pwd", "").commit();
				spf.edit().putString("clientid", "").commit();
				spf.edit().putString("profile_user", "").commit();
				fxApplication.initData();
				Toast.makeText(getApplicationContext(), "您的账号已在其他手机登陆",
						Toast.LENGTH_LONG).show();
				break;
			}
		}
	};

	@Override
	protected void onCreate(Bundle arg0) {
		super.onCreate(arg0);
		setContentView(R.layout.main);
		getButton();
		countLinear = (LinearLayout) findViewById(R.id.main_countLinear);
		countText = (TextView) findViewById(R.id.main_count);
		spf = getSharedPreferences(Urlinterface.SHARED, 0);
		user_id = spf.getInt("user_id", 0);
		token = spf.getString("Token", "");
		vp = (ViewPager) findViewById(R.id.main_viewPager);
		ImageCacheUtil.IMAGE_CACHE.clear();
		list.add(new TalkActivity());
		list.add(new ContactActivity());
		list.add(new SettingsActivity());
		db = new DBManager(this);
		String release = android.os.Build.VERSION.RELEASE; // android系统版本号
		version = Integer.parseInt(release.substring(0, 1));
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

		contact_search = (ImageView) findViewById(R.id.contact_search);
		contact_search.setOnTouchListener(new View.OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				if (event.getAction() == MotionEvent.ACTION_DOWN) {
					contact_search.getBackground().setAlpha(70);// 设置图片透明度0~255，0完全透明，255不透明
																// imgButton.invalidate();
				} else if (event.getAction() == MotionEvent.ACTION_UP) {
					contact_search.getBackground().setAlpha(255);// 还原图片
				}
				return false;
			}
		});
		fxApplication = (FxApplication) getApplication();
		mReuRequstReceiver = new RequstReceiver();
		fxApplication.getActivityList().add(this);
		searchMethod();
		Display display = getWindowManager().getDefaultDisplay();
		int width = display.getWidth();
		int a = display.getHeight();
		Log.i("linshi", "display.getHeight()xdisplay.getWidth():" + a + "x"
				+ width);
		fxApplication.setWidth(width);
		fxApplication.setHeight(a);
		changeTitleStyle();
		setEdittextListening();
		InitImageView();

		contactInformation();

		// 个推SDK初始化
		PushManager.getInstance().initialize(this.getApplicationContext());
		handler.sendEmptyMessage(8);
	}

	/**
	 * 查询本地联系人信息，没有的话，则请求服务器并保存到本地
	 * 
	 * 
	 */
	private void contactInformation() {
		// 实例化汉字转拼音类
		characterParser = CharacterParser.getInstance();
		contactsLists = db.queryContactList(spf.getInt("user_id", 0));
		Log.i("11", contactsLists.size() + "-----------1");
		if (contactsLists.size() == 0) {
			if (FuXunTools.isConnect(this)) {
				prodialog = new ProgressDialog(FragmengtActivity.this);
				prodialog.setMessage("正在加载数据，请稍后...");
				prodialog.setCanceledOnTouchOutside(false);
				prodialog.show();
				Thread thread = new Thread(new getContacts());
				thread.start();
			} else {
				Toast.makeText(FragmengtActivity.this, R.string.no_internet,
						Toast.LENGTH_SHORT).show();
			}
		} else {
			Log.i("Ax", "加载本地联系人");
			if (spf.getString("profile_user", "").equals(user_id + "")) {
				Intent i = new Intent();
				i.setClass(this, RequstService.class);
				startService(i);
			} else {
				if (FuXunTools.isConnect(this)) {
					prodialog = new ProgressDialog(FragmengtActivity.this);
					prodialog.setMessage("正在加载数据，请稍后...");
					prodialog.setCanceledOnTouchOutside(false);
					prodialog.show();
					getProfile();
				} else {
					Toast.makeText(FragmengtActivity.this,
							R.string.no_internet, Toast.LENGTH_SHORT).show();
					Intent i = new Intent();
					i.setClass(this, RequstService.class);
					startService(i);
				}
			}

		}

	}

	private void getProfile() {

		Thread thread = new Thread(new getProfile());
		thread.start();

	}

	/**
	 * 加载联系人头像，并保存到本地
	 * 
	 * 
	 */
	private void getUserBitmap() {
		ExecutorService singleThreadExecutor = Executors
				.newSingleThreadExecutor();
		for (int i = 0; i < contactsLists.size(); i++) {
			final int index = i;
			final int contactId = contactsLists.get(i).getContactId();
			final String url = contactsLists.get(i).getUserface_url();
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
						options.inSampleSize = 2;
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
							bm.compress(Bitmap.CompressFormat.PNG, 90, out);
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
				Log.i("Max",
						spf.getInt("user_id", 0) + "/"
								+ spf.getString("Token", "null"));
				ContactRequest.Builder builder = ContactRequest.newBuilder();
				builder.setUserId(spf.getInt("user_id", 0));
				builder.setToken(spf.getString("Token", "null"));
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
							String orderTime = res.getContacts(i)
									.getOrderTime();
							String subscribeTime = res.getContacts(i)
									.getSubscribeTime();
							ShortContactPojo coPojo = new ShortContactPojo(
									contactId, sortKey, name, customName,
									userface_url, sex, source, lastContactTime,
									isBlocked, orderTime, subscribeTime);
							contactsLists.add(coPojo);

						}

						SharedPreferences preferences = getSharedPreferences(
								Urlinterface.SHARED, Context.MODE_PRIVATE);
						Editor editor = preferences.edit();
						editor.putString("contactTimeStamp", res.getTimeStamp());
						editor.commit();
						Log.i("Max", contactsLists.size() + "");
						Message msg = new Message();// 创建Message 对象
						msg.what = 0;
						handler.sendMessage(msg);
					} else {
						int ErrorCode = res.getErrorCode().getNumber();
						if (ErrorCode == 2001) {
							handler.sendEmptyMessage(9);
						} else {
							handler.sendEmptyMessage(5);
						}

					}

				} else {
					handler.sendEmptyMessage(6);
				}

			} catch (Exception e) {
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
		Display display = getWindowManager().getDefaultDisplay();
		int width = display.getWidth();
		int height = display.getHeight();

		TextView tv = (TextView) findViewById(R.id.contact_title);
		String tv_str = (String) tv.getText().toString();
		SpannableStringBuilder style2 = new SpannableStringBuilder(tv_str);

		 if (height == 854 && width == 480) {
			style2.setSpan(new AbsoluteSizeSpan(27), 0, 3,
					Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
			tv.setText(style2);
		} else if (height >= 1750 && height <= 1920 && width == 1080) {
			style2.setSpan(new AbsoluteSizeSpan(60), 0, 3,
					Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
			tv.setText(style2);
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
					public void onItemClick(AdapterView<?> parent, View view,
							int position, long id) {
						SharedPreferences preferences = getSharedPreferences(
								Urlinterface.SHARED, Context.MODE_PRIVATE);
						Editor editor = preferences.edit();
						editor.putInt("contact_id", contactsLists.get(position)
								.getContactId());
						editor.commit();
						Intent intent = new Intent();
						intent.putExtra("contact_id",
								contactsLists.get(position).getContactId());
						intent.setClass(FragmengtActivity.this,
								ContactInfoActivity.class);
						startActivity(intent);
						contact_search_edittext.setText("");
					}
				});

	}

	/*
	 * 搜索按妞
	 */
	private View.OnClickListener listener1 = new View.OnClickListener() {
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
			SourceDateList = db.queryContactList(spf.getInt("user_id", 0));
		}
	};
	/*
	 * 清空搜索框
	 */
	private View.OnClickListener listener2 = new View.OnClickListener() {
		public void onClick(View v) {
			contact_search_edittext.setText("");
		}
	};
	/*
	 * 取消
	 */
	private View.OnClickListener listener3 = new View.OnClickListener() {
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
				adapter = new SearchContactAdapter(FragmengtActivity.this,
						findSimilarContacts(content));
				contacts_search_listview.setAdapter(adapter);
			}

			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
			}

			public void afterTextChanged(Editable s) {
			}
		});
	}

	/**
	 * 
	 * 获得个人详细信息
	 * 
	 * 
	 */

	class getProfile implements Runnable {
		public void run() {
			try {
				int user_id = spf.getInt("user_id", -1);
				String Token = spf.getString("Token", "");
				ProfileRequest.Builder builder = ProfileRequest.newBuilder();
				builder.setUserId(user_id);
				builder.setToken(Token);
				ProfileRequest response = builder.build();

				byte[] by = HttpUtil.sendHttps(response.toByteArray(),
						Urlinterface.PROFILE, "POST");
				if (by != null && by.length > 0) {

					ProfileResponse res = ProfileResponse.parseFrom(by);
					if (res.getIsSucceed()) {
						int userId = res.getProfile().getUserId();// 用户id
						String name = res.getProfile().getName();// 名称
						String nickName = res.getProfile().getNickName();// 昵称
						int gender = res.getProfile().getGender().getNumber();// 性别
						String tileUrl = res.getProfile().getTileUrl();// 头像
						Boolean isProvider = res.getProfile().getIsProvider();//
						String lisence = res.getProfile().getLisence();// 行业认证
						String mobile = res.getProfile().getMobilePhoneNum();// 手机号码
						String email = res.getProfile().getEmail();// 邮箱
						String birthday = res.getProfile().getBirthday();// 生日
						Boolean isAuthentication = res.getProfile()
								.getIsAuthentication();// 实名认证
						String fuzhi = res.getProfile().getFuzhi();// 福值
						String location = res.getProfile().getLocation();// 所在地
						String description = res.getProfile().getDescription();// 福师简介

						profilePojo = new ProfilePojo(userId, name, nickName,
								gender, tileUrl, isProvider, lisence, mobile,
								email, birthday, isAuthentication, fuzhi,
								location, description);
						Log.i("linshi", "  --nickName" + nickName
								+ "  --gender" + gender + "  --tileUrl"
								+ tileUrl + "  --lisence" + lisence
								+ "  --mobile" + mobile + "  --email" + email
								+ "  birthday--" + birthday);
						Log.i("linshi------------",
								"profileprofileprofileprofile网络shuju");
						Message msg = new Message();// 创建Message 对象
						msg.what = 2;
						handler.sendMessage(msg);
					} else {

						int ErrorCode = res.getErrorCode().getNumber();
						if (ErrorCode == 2001) {
							handler.sendEmptyMessage(9);
						} else {
							handler.sendEmptyMessage(3);
						}

					}
				} else {
					handler.sendEmptyMessage(3);
				}

				// handler.sendEmptyMessage(0);
			} catch (Exception e) {
				// prodialog.dismiss();
				Log.i("error", e.toString());
				handler.sendEmptyMessage(7);
			}
		}
	}

	private void putProfile(ProfilePojo pro) {
		SharedPreferences preferences = getSharedPreferences(
				Urlinterface.SHARED, Context.MODE_PRIVATE);
		Editor editor = preferences.edit();
		editor.putInt("profile_userid", pro.getUserId());
		editor.putString("profile_name", pro.getName());
		editor.putString("profile_nickName", pro.getNickName());
		editor.putInt("profile_gender", pro.getGender());
		editor.putString("profile_tileUrl", pro.getTileUrl());
		editor.putBoolean("profile_isProvider", pro.getIsProvider());
		editor.putString("profile_lisence", pro.getLisence());
		editor.putString("profile_mobile", pro.getMobile());
		editor.putString("profile_email", pro.getEmail());
		editor.putString("profile_birthday", pro.getBirthday());
		editor.putBoolean("profile_isAuthentication", pro.getIsAuthentication());
		editor.putString("profile_fuZhi", pro.getFuZhi());
		editor.putString("profile_location", pro.getLocation());
		editor.putString("profile_description", pro.getDescription());
		editor.putString("profile_user", pro.getUserId() + "");// 用于判断本地是否有当前用户的信息
		editor.commit();
		dataNumber = 1;
	}

	public void getBitmap_url(final String url, final int id) {

		Thread thread = new Thread() {
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
					// bm =decodeSampledBitmapFromStream(is,150,150);

					BitmapFactory.Options options = new BitmapFactory.Options();
					options.inJustDecodeBounds = false;
					options.inSampleSize = 1;
					bm = BitmapFactory.decodeStream(is, null, options);
					Log.i("linshi", bm.getWidth() + "---" + bm.getHeight());
					is.close();
					if (bm != null) {
						Log.i("linshi",
								bm.getWidth() + "---2---" + bm.getHeight());
						File f = new File(Urlinterface.head_pic, id + "");

						if (f.exists()) {
							f.delete();
						}
						if (!f.getParentFile().exists()) {
							f.getParentFile().mkdirs();
						}
						Log.i("linshi", "----1");
						FileOutputStream out = new FileOutputStream(f);
						Log.i("linshi", "----6");
						bm.compress(Bitmap.CompressFormat.PNG, 90, out);
						out.flush();
						out.close();

						Log.i("linshi", "已经保存");
					}
					handler.sendEmptyMessage(3);
				} catch (Exception e) {
					Log.i("linshi", "发生异常");
					handler.sendEmptyMessage(3);
				}
			}
		};
		thread.start();
	}

	public List<ShortContactPojo> findSimilarContacts(String et) {
		contactsLists = new ArrayList<ShortContactPojo>();
		if (et.length() > 0) {
			for (int i = 0; i < SourceDateList.size(); i++) {
				String str = SourceDateList.get(i).getCustomName();
				String str2 = SourceDateList.get(i).getName();
				if (str.indexOf(et) != -1 || str2.indexOf(et) != -1) {
					contactsLists.add(SourceDateList.get(i));
				}
			}
		}
		return contactsLists;
	}

	class menuOnclick implements OnClickListener {
		private int index = 0;

		public menuOnclick(int index) {
			super();
			this.index = index;
		}

		public void onClick(View v) {
			if (index != currIndex) {
				changeLocation(index);
			}
		}
	}

	protected void onResume() {
		super.onResume();
		registerReceiver(mReuRequstReceiver, new IntentFilter(
				"com.comdosoft.fuxun.REQUEST_ACTION"));
		handler.sendEmptyMessage(8);
		StatService.onResume(this);
	}

	protected void onPause() {
		super.onPause();
		unregisterReceiver(mReuRequstReceiver);
		StatService.onPause(this);
	}

	class RequstReceiver extends BroadcastReceiver {
		public void onReceive(Context context, Intent intent) {
			handler.sendEmptyMessage(8);
			list.get(0).onStart();
		}
	}

	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			// spf.edit().putString("Token", "null").commit();
			Dialog dialog = new AlertDialog.Builder(FragmengtActivity.this)
					.setTitle("提示")
					.setMessage("您确认要退出应用么?")
					.setPositiveButton("确认",
							new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog,
										int which) {

									System.exit(0);
								}
							})
					.setNegativeButton("取消",
							new DialogInterface.OnClickListener() {

								@Override
								public void onClick(DialogInterface dialog,
										int which) {
									dialog.dismiss();
								}
							}).create();
			dialog.show();

			return true;
		}
		return super.onKeyDown(keyCode, event);
	}
}
