package com.fuwu.mobileim.activity;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.NotificationManager;
import android.app.ProgressDialog;
import android.app.AlertDialog.Builder;
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
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
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
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.View.OnTouchListener;
import android.view.ViewTreeObserver.OnPreDrawListener;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;

import com.baidu.mobstat.StatService;
import com.fuwu.mobileim.R;
import com.fuwu.mobileim.activity.FragmengtActivity.RequstReceiver;
import com.fuwu.mobileim.activity.FragmengtActivity.downloadApkThread_table;
import com.fuwu.mobileim.activity.FragmengtActivity.getContacts;
import com.fuwu.mobileim.activity.FragmengtActivity.getProfile;
import com.fuwu.mobileim.activity.FragmengtActivity.menuOnclick;
import com.fuwu.mobileim.adapter.FragmentViewPagerAdapter;
import com.fuwu.mobileim.adapter.SearchContactAdapter;
import com.fuwu.mobileim.model.Models.AuthenticationRequest;
import com.fuwu.mobileim.model.Models.AuthenticationResponse;
import com.fuwu.mobileim.model.Models.ContactRequest;
import com.fuwu.mobileim.model.Models.ContactResponse;
import com.fuwu.mobileim.model.Models.License;
import com.fuwu.mobileim.model.Models.ProfileRequest;
import com.fuwu.mobileim.model.Models.ProfileResponse;
import com.fuwu.mobileim.pojo.ProfilePojo;
import com.fuwu.mobileim.pojo.ShortContactPojo;
import com.fuwu.mobileim.util.DBManager;
import com.fuwu.mobileim.util.FuXunTools;
import com.fuwu.mobileim.util.FxApplication;
import com.fuwu.mobileim.util.HttpUtil;
import com.fuwu.mobileim.util.ImageCacheUtil;
import com.fuwu.mobileim.util.KeyboardLayout;
import com.fuwu.mobileim.util.KeyboardLayout.onKybdsChangeListener;
import com.fuwu.mobileim.util.Urlinterface;
import com.fuwu.mobileim.view.CircularImage;
import com.fuwu.mobileim.view.MyDialog;
import com.igexin.sdk.PushManager;

/**
 * 作者: 张秀楠 时间：2014-5-23 下午4:34:03
 */
public class SearchContactActivity extends Activity implements Urlinterface {
	private int user_id;
	private RelativeLayout main_search;// 搜索框全部
	private TextView contact_search_edittext;// 搜索框输入框
	private ImageView contact_search_empty;// 搜索框 清空图标
	private Button contact_search_cancel;// 搜索功能 取消按钮
	private ListView contacts_search_listview;// 搜索到的内容 listview
	private FxApplication fxApplication;
	private List<ShortContactPojo> SourceDateList;
	private SearchContactAdapter adapter;
	private DBManager db;
	private List<ShortContactPojo> contactsLists = new ArrayList<ShortContactPojo>(); // 联系人arraylist数组
	private SharedPreferences spf;
	int version = 0;

	@Override
	protected void onCreate(Bundle arg0) {
		super.onCreate(arg0);
		setContentView(R.layout.search_contact);
		
		spf = getSharedPreferences(Urlinterface.SHARED, 0);
		user_id = spf.getInt("user_id", 0);
		db = new DBManager(this);
		String release = android.os.Build.VERSION.RELEASE; // android系统版本号
		version = Integer.parseInt(release.substring(0, 1));
		fxApplication = (FxApplication) getApplication();
		fxApplication.getActivityList().add(this);
		searchMethod();
		Display display = getWindowManager().getDefaultDisplay();
		int width = display.getWidth();
		int a = display.getHeight();
		fxApplication.setWidth(width);
		fxApplication.setHeight(a);
		setEdittextListening();
		SourceDateList = db.queryContactList(spf.getInt("user_id", 0));
	}

	/**
	 * 搜索相关设置
	 */
	public void searchMethod() {
		contacts_search_listview = (ListView) findViewById(R.id.contacts_search_list_view); // 搜索到的内容
		main_search = (RelativeLayout) findViewById(R.id.main_search);// 搜索框全部
		contact_search_edittext = (TextView) findViewById(R.id.contact_search_edittext);// 搜索框输入框
		contact_search_empty = (ImageView) findViewById(R.id.contact_search_empty);// 搜索框清空图标
		contact_search_cancel = (Button) findViewById(R.id.contact_search_cancel);// 搜索功能
																					// 取消按钮
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
						intent.setClass(SearchContactActivity.this,
								ContactInfoActivity.class);
						startActivity(intent);
//						contact_search_edittext.setText("");
					}
				});
	}

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
			finish();
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
				adapter = new SearchContactAdapter(SearchContactActivity.this,
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

	protected void onResume() {
		super.onResume();
		StatService.onResume(this);
		Editor editor = spf.edit();
		editor.putInt("MessageNumber", 0);
		editor.commit();
		NotificationManager nm = (NotificationManager) getSystemService(android.content.Context.NOTIFICATION_SERVICE);
		nm.cancel(Urlinterface.Receiver_code);
	}

	protected void onPause() {
		super.onPause();
		StatService.onPause(this);
	}

	@Override
	protected void onStart() {
		// TODO Auto-generated method stub
		super.onStart();
		final Animation translateAnimation = new TranslateAnimation(720, 0, 0,
				0); // 移动动画效果
		translateAnimation.setDuration(100); // 设置动画持续时间
		main_search.setAnimation(translateAnimation); // 设置动画效果
		translateAnimation.startNow(); // 启动动画
	}

}
