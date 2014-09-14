package com.fuwu.mobileim.activity;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import com.fuwu.mobileim.R;
import com.fuwu.mobileim.model.Models.BlockContactRequest;
import com.fuwu.mobileim.model.Models.BlockContactResponse;
import com.fuwu.mobileim.model.Models.Contact;
import com.fuwu.mobileim.model.Models.ContactDetailRequest;
import com.fuwu.mobileim.model.Models.ContactDetailResponse;
import com.fuwu.mobileim.model.Models.License;
import com.fuwu.mobileim.pojo.ContactPojo;
import com.fuwu.mobileim.util.DBManager;
import com.fuwu.mobileim.util.FuXunTools;
import com.fuwu.mobileim.util.FxApplication;
import com.fuwu.mobileim.util.HttpUtil;
import com.fuwu.mobileim.util.Urlinterface;
import com.fuwu.mobileim.view.MyDialog;

/**
 * @作者 马龙
 * @时间 创建时间：2014-6-16 下午5:30:51
 */
public class ContactInfoActivity extends Activity implements OnClickListener,
		OnTouchListener {
	private TextView name, sign, fuzhi, sex, location;
	private ImageView img;
	private View personal_info_relativeLayout2; // 背景
	private ContactPojo cp = null;
	private int user_id;
	private int contact_id;
	private String token;
	private DBManager db;
	private boolean shielding;
	private FxApplication fxApplication;
	private PopupWindow menuWindow;
	private ImageView mOther; // 右上方按钮
	private Button info_sendBtn;
	private float height = 0;
	private ImageView pingbi;
	private Handler handler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);

			switch (msg.what) {
			case 1:
				builder.dismiss();
				updateData();
				break;
			case 2:
				builder.dismiss();
				Toast.makeText(getApplicationContext(), "获取详细信息失败!", 0).show();
				break;
			case 7:
				Toast.makeText(getApplicationContext(), R.string.no_internet,
						Toast.LENGTH_SHORT).show();
				break;
			case 9:
				builder.dismiss();
				db = new DBManager(ContactInfoActivity.this);
				if (!db.isOpen()) {
					db = new DBManager(ContactInfoActivity.this);
				}
				if (shielding) {
					Toast.makeText(getApplicationContext(), "屏蔽成功",
							Toast.LENGTH_SHORT).show();
					cp.setIsBlocked(1);
					db.modifyContactBlock(1, user_id, contact_id);
					handler.sendEmptyMessage(13);
				} else {
					cp.setIsBlocked(0);
					Toast.makeText(getApplicationContext(), "取消屏蔽成功",
							Toast.LENGTH_SHORT).show();
					db.modifyContactBlock(0, user_id, contact_id);
				}
				break;
			case 10:
				builder.dismiss();
				if (shielding) {
					Toast.makeText(getApplicationContext(), "屏蔽失败",
							Toast.LENGTH_SHORT).show();
				} else {
					Toast.makeText(getApplicationContext(), "取消屏蔽失败",
							Toast.LENGTH_SHORT).show();
				}
				break;
			case 11:
				break;
			case 12:
				builder.dismiss();
				new Handler().postDelayed(new Runnable() {
					public void run() {
						Intent intent = new Intent(ContactInfoActivity.this,
								LoginActivity.class);
						startActivity(intent);
						clearActivity();
					}
				}, 3500);
				FuXunTools.initdate(preferences, fxApplication);
				Toast.makeText(getApplicationContext(), "您的账号已在其他手机登陆",
						Toast.LENGTH_LONG).show();
				break;
			case 13:
				updateData();
				break;
			case 14:
				Toast.makeText(getApplicationContext(), "此联系人不可以修改备注。", 0)
						.show();
				break;
			case 15:
				Toast.makeText(getApplicationContext(), "此联系人不可以屏蔽。", 0).show();
				break;
			}
		}
	};
	private SharedPreferences preferences;
	private MyDialog builder;
	// 行业认证图标
	private ArrayList<ImageView> imageviewList;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.personal_info);
		db = new DBManager(this);
		fxApplication = (FxApplication) getApplication();
		fxApplication.getActivityList().add(this);
		DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
		height = displayMetrics.heightPixels;
		InitView();

		preferences = getSharedPreferences(Urlinterface.SHARED,
				Context.MODE_PRIVATE);
		user_id = preferences.getInt("user_id", 1);
		contact_id = preferences.getInt("contact_id", 1);
		token = preferences.getString("Token", "token");
		if (contact_id != 0) {
			if (FuXunTools.isConnect(ContactInfoActivity.this)) {
				builder= FuXunTools.showLoading(getLayoutInflater(),ContactInfoActivity.this,"正在加载详细信息..");
				new GetContactDetail().start();
			} else {
				handler.sendEmptyMessage(7);
			}
		} else {
			cp = new ContactPojo(0, "", "系统消息", "系统消息", "", 2, 0, "", 0, 0, "",
					"随时、随地、随需", "", "上海", null, "",false);
			handler.sendEmptyMessage(13);
		}

	}

	public void InitView() {
		info_sendBtn = (Button) findViewById(R.id.info_sendBtn);
		findViewById(R.id.info_sendBtn).setOnClickListener(this);
		findViewById(R.id.contact_info_back).setOnClickListener(this);
		findViewById(R.id.contact_info_back).setOnTouchListener(this);
		findViewById(R.id.chat_other).setOnTouchListener(this);
		personal_info_relativeLayout2 = findViewById(R.id.personal_info_relativeLayout2);
		name = (TextView) findViewById(R.id.info_name);
		mOther = (ImageView) findViewById(R.id.chat_other);
		mOther.setOnTouchListener(this);
		mOther.setOnClickListener(this);
		sign = (TextView) findViewById(R.id.info_sign);// 个人简介

		fuzhi = (TextView) findViewById(R.id.info_fuzhi); // 福指
		img = (ImageView) findViewById(R.id.info_img); // 头像
		sex = (TextView) findViewById(R.id.info_sex); // 性别
		location = (TextView) findViewById(R.id.info_location); // 地址
		Drawable drawable = getResources().getDrawable(R.drawable.moren);
		BitmapDrawable bitmapDrawable = (BitmapDrawable) drawable;
		Bitmap bitmap = bitmapDrawable.getBitmap();
		img.setImageDrawable(new BitmapDrawable(FuXunTools
				.createRoundConerImage(bitmap)));
		 pingbi = (ImageView) findViewById(R.id.pingbi); // 屏蔽

	}

	private View.OnClickListener listener = new View.OnClickListener() {

		@Override
		public void onClick(View v) {
			Intent intent = new Intent();
			intent.putExtra("image_path", Urlinterface.head_pic + contact_id);
			intent.setClass(ContactInfoActivity.this,
					ComtactZoomImageActivity.class);
			startActivity(intent);
		}
	};

	public void updateData() {
		img.setOnClickListener(listener); // 头像设置监听
		String face_str = cp.getUserface_url();
		if (face_str != null && face_str.length() > 4) {
			File f = new File(Urlinterface.head_pic, contact_id + "");
			if (f.exists()) {
				img.setImageDrawable(new BitmapDrawable(
						FuXunTools.createRoundConerImage(BitmapFactory
								.decodeFile(Urlinterface.head_pic + contact_id))));

			} else {
				FuXunTools.set_bk_createRoundConerImage(contact_id, face_str,
						img);
			}

		} else {
			Drawable drawable = getResources().getDrawable(R.drawable.moren);
			if (cp.getContactId() == 0) {
				drawable = getResources().getDrawable(
						R.drawable.system_user_face);
			}
			BitmapDrawable bitmapDrawable = (BitmapDrawable) drawable;
			Bitmap bitmap = bitmapDrawable.getBitmap();
			img.setImageDrawable(new BitmapDrawable(FuXunTools
					.createRoundConerImage(bitmap)));
		}
		// 性别
		int sexNum = cp.getSex();
		if (sexNum == 0) {// 男
			sex.setText("男");
		} else if (sexNum == 1) {// 女
			sex.setText("女");
		} else if (sexNum == 2) {
			sex.setText("保密");
		}
		// 地址
		location.setText("" + cp.getLocation());
		// 备注 名称 昵称
		String customname = cp.getCustomName();
		if (customname != null && customname.length() > 0
				&& !customname.equals("null")) {
			name.setText(customname);
		} else {
			name.setText(cp.getName());
		}
		// 个人简介
		String IndividualResume = cp.getIndividualResume();
		if (IndividualResume != null && IndividualResume.length() > 0
				&& !IndividualResume.equals("null")) {
			sign.setText("" + cp.getIndividualResume());
		} else {
			sign.setText("此福师尚未编写信息");
			// findViewById(R.id.info_sign_null).setVisibility(View.VISIBLE);
		}

		// 福指
		String fuzhiStr = cp.getFuzhi();
		if ("".equals(fuzhiStr)) {
			fuzhiStr = "0";
		}
		fuzhiStr = fuzhiStr + "/5.0";
		int index = fuzhiStr.indexOf("/");
		SpannableStringBuilder mSpannableStringBuilder = new SpannableStringBuilder(
				fuzhiStr);
		mSpannableStringBuilder.setSpan(new ForegroundColorSpan(Color.RED), 0,
				index, Spannable.SPAN_EXCLUSIVE_INCLUSIVE);
		ForegroundColorSpan span_1 = new ForegroundColorSpan(Color.argb(255,
				153, 153, 153));
		mSpannableStringBuilder.setSpan(span_1, index, fuzhiStr.length(),
				Spannable.SPAN_EXCLUSIVE_INCLUSIVE);
		fuzhi.setText(mSpannableStringBuilder);
		// 行业认证图标
		imageviewList = new ArrayList<ImageView>();
		for (int i = 0; i < FuXunTools.image_id.length; i++) {
			imageviewList.add((ImageView) findViewById(FuXunTools.image_id[i]));
		}
		if (cp.getIsProvider() == 1) { // 福师
			findViewById(R.id.personal_info_fz).setVisibility(View.VISIBLE);
			findViewById(R.id.personal_info_gerenjianjie).setVisibility(
					View.VISIBLE);
			findViewById(R.id.personal_info_hangye).setVisibility(View.VISIBLE);

			if (cp.getLicenses().size() != 0) { // 福师认证了行业

				FuXunTools.setItem_bg(imageviewList, cp.getLicenses());
				for (int i = 0; i < cp.getLicenses().size(); i++) {
					final int num = i;
					imageviewList.get(i).setOnClickListener(
							new OnClickListener() {

								@Override
								public void onClick(View v) {
									// TODO Auto-generated method stub
									int[] location = new int[2];
									v.getLocationOnScreen(location);
									int x = location[0]-2*v.getWidth();
									int y = (int) (location[1]-v.getWidth()*0.5);
									authentication_dis(x, y, cp.getLicenses()
											.get(num).getName());
//									Toast.makeText(getApplicationContext(), ""+v.getWidth(), 0).show();
								}
							});
				}

			}
		}
		if (cp.getContactId() != 0) {
		int size = cp.getLicenses().size();
		if (size == 6) {
			imageviewList.add((ImageView) findViewById(R.id.info_face6));
			size = size + 1;
		}
		// 实名认证
		
		if (cp.isAuthentication()) {
			findViewById(R.id.personal_info_hangye).setVisibility(View.VISIBLE);
			imageviewList.get(size).setBackgroundResource(R.drawable.real_name);
			imageviewList.get(size).setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub
					int[] location = new int[2];
					v.getLocationOnScreen(location);
					int x = location[0]-2*v.getWidth();
					int y = (int) (location[1]-v.getWidth()*0.5);
					authentication_dis(x, y, "实名认证");
				}
			});
		}
		}
		// 设置背景
		String backgroundUrl = cp.getBackgroundUrl();

		if (backgroundUrl != null && backgroundUrl.length() > 4) {
			String backgroundUrl_filename = backgroundUrl.substring(
					backgroundUrl.lastIndexOf("/") + 1, backgroundUrl.length());
			backgroundUrl_filename = backgroundUrl_filename.substring(0,
					backgroundUrl_filename.indexOf(".")) + ".jpg";
			File f = new File(Urlinterface.head_pic, backgroundUrl_filename
					+ "");
			if (f.exists()) {
				personal_info_relativeLayout2
						.setBackgroundDrawable(new BitmapDrawable(BitmapFactory
								.decodeFile(Urlinterface.head_pic
										+ backgroundUrl_filename)));

			} else {
				FuXunTools.set_view_bk(cp.getIsProvider(),
						backgroundUrl_filename, backgroundUrl,
						personal_info_relativeLayout2);
			}
		} else {
			if (cp.getContactId() != 0) {

				if (cp.getIsProvider() == 1) { // 福师
					// 福师未认证行业
					personal_info_relativeLayout2
							.setBackgroundResource(R.drawable.unauthorized_bg);
				} else { // 设置福客 背景
					findViewById(R.id.personal_info_relativeLayout2)
							.setBackgroundResource(R.drawable.fuke_bg);
				}
			} else { // 设置系统消息 个人简介、 背景
				findViewById(R.id.personal_info_gerenjianjie).setVisibility(
						View.VISIBLE);
				findViewById(R.id.personal_info_relativeLayout2)
						.setBackgroundResource(R.drawable.system_user_bg);
			}
		}
		if (cp.getContactId() == 0) {
			mOther.setVisibility(View.GONE);
			info_sendBtn.setText("查看消息");
			
		} 
		if (cp.getIsBlocked()==1) {
			pingbi.setVisibility(View.VISIBLE);
		}else {
			pingbi.setVisibility(View.GONE);
		}
	}

	class GetContactDetail extends Thread {
		public void run() {
			try {
				ContactDetailRequest.Builder builder = ContactDetailRequest
						.newBuilder();
				builder.setUserId(user_id);
				builder.setContactId(contact_id);
				builder.setToken(token);
				ContactDetailRequest response = builder.build();
				byte[] by = HttpUtil.sendHttps(response.toByteArray(),
						Urlinterface.ContactDetail, "POST");
				if (by != null && by.length > 0) {
					ContactDetailResponse res = ContactDetailResponse
							.parseFrom(by);
					if (res.getIsSucceed()) {
						Contact contact = res.getContact();
						int contactId = contact.getContactId();
						String name = contact.getName();
						String customName = contact.getCustomName();
						String sortKey = "";
						// if (customName != null && customName.length() > 0) {
						// sortKey = FuXunTools.findSortKey(customName);
						// } else {
						// sortKey = FuXunTools.findSortKey(name);
						// }
						String userface_url = contact.getTileUrl();
						int sex = contact.getGender().getNumber();
						int source = contact.getSource();
						String lastContactTime = contact.getLastContactTime();
						boolean isblocked = contact.getIsBlocked();
						boolean isprovider = contact.getIsProvider();
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
						String lisence = contact.getLisence();
						String individualResume = contact.getIndividualResume();
						String fuzhi = contact.getFuzhi();
						String location = contact.getLocation();
						List<License> licenses = contact.getLicensesList();
						String backgroundUrl = contact.getBackgroundUrl();
						boolean  isAuthentication = contact.getIsAuthentication();
						Log.i("FuWu", "isAuthentication:" + isAuthentication);
						cp = new ContactPojo(contactId, sortKey, name,
								customName, userface_url, sex, source,
								lastContactTime, isBlocked, isProvider,
								lisence, individualResume, fuzhi, location,
								licenses, backgroundUrl,isAuthentication);
						handler.sendEmptyMessage(1);
						Log.i("FuWu", "contact:" + cp.toString());
					} else {
						int ErrorCode = res.getErrorCode().getNumber();
						if (ErrorCode == 2001) {
							handler.sendEmptyMessage(12);
						} else {
							handler.sendEmptyMessage(2);
						}
					}
				} else {
					handler.sendEmptyMessage(2);
				}
			} catch (Exception e) {

			}
		}
	}

	@Override
	public void onClick(View v) {
		Intent intent = new Intent();
		switch (v.getId()) {
		case R.id.info_sendBtn:
			this.finish();
			db.clearTalkMesCount(user_id, contact_id);
			intent = new Intent();
			intent.setClass(ContactInfoActivity.this, ChatActivity.class);
			startActivity(intent);
			break;
		case R.id.contact_info_back:
			this.finish();
			break;
		case R.id.personal_info_other_moifyCustomname: // 修改备注
			if (menuWindow.isShowing()) {
				menuWindow.dismiss();
			}
			if (cp.getContactId() != 0) {
				intent = new Intent();
				intent.putExtra("customName", cp.getCustomName());
				intent.setClass(ContactInfoActivity.this, OpenInputMethod.class);
				startActivityForResult(intent, 0);
			} else {
				handler.sendEmptyMessage(14);
			}
			break;
		case R.id.personal_info_other_shielding: // 屏蔽此人
			if (cp.getContactId() != 0) {
				if (FuXunTools.isConnect(ContactInfoActivity.this)) {
					builder= FuXunTools.showLoading(getLayoutInflater(),ContactInfoActivity.this,"正在屏蔽联系人..");
					shielding = true;
					Thread thread = new Thread(new BlockContact());
					thread.start();
				} else {
					handler.sendEmptyMessage(11);
				}
			} else {
				handler.sendEmptyMessage(15);
			}
			if (menuWindow.isShowing()) {
				menuWindow.dismiss();
			}
			break;

		case R.id.chat_other:
			menu_press();
			break;
		}
	}

	class BlockContact implements Runnable {
		public void run() {
			try {

				BlockContactRequest.Builder builder = BlockContactRequest
						.newBuilder();
				builder.setUserId(user_id);
				builder.setToken(token);
				builder.setContactId(contact_id);
				builder.setIsBlocked(shielding);

				BlockContactRequest response = builder.build();

				byte[] by = HttpUtil.sendHttps(response.toByteArray(),
						Urlinterface.BlockContact, "PUT");
				if (by != null && by.length > 0) {

					BlockContactResponse res = BlockContactResponse
							.parseFrom(by);
					if (res.getIsSucceed()) {
						handler.sendEmptyMessage(9);
					} else {
						int ErrorCode = res.getErrorCode().getNumber();
						if (ErrorCode == 2001) {
							handler.sendEmptyMessage(12);
						} else {
							handler.sendEmptyMessage(10);
						}
					}
				} else {
					handler.sendEmptyMessage(10);
				}
				//
			} catch (Exception e) {
				handler.sendEmptyMessage(10);
			}
		}
	}

	@Override
	public boolean onTouch(View v, MotionEvent event) {
		if (event.getAction() == MotionEvent.ACTION_DOWN) {
			switch (v.getId()) {
			case R.id.chat_other:
				findViewById(R.id.chat_other).getBackground().setAlpha(70);
				break;
			case R.id.contact_info_back:
				Log.i("linshi", "onTouchonTouchonTouchonTouch--my_info_back");
				findViewById(R.id.contact_info_back).getBackground().setAlpha(
						70);
				break;
			}
		} else if (event.getAction() == MotionEvent.ACTION_UP) {
			switch (v.getId()) {
			case R.id.chat_other:
				findViewById(R.id.chat_other).getBackground().setAlpha(255);
				break;
			case R.id.contact_info_back:
				findViewById(R.id.contact_info_back).getBackground().setAlpha(
						255);
				break;
			}
		}
		return false;
	}

	@SuppressWarnings("deprecation")
	public void menu_press() {
		View view = getLayoutInflater().inflate(R.layout.personal_info_other,
				null);
		if (cp != null) {

			view.findViewById(R.id.personal_info_other_moifyCustomname)
					.setOnClickListener(this);
			int isblock = cp.getIsBlocked();
			if (isblock == 0) {
				view.findViewById(R.id.personal_info_other_shielding)
						.setOnClickListener(this);

			} else {
				TextView tv = (TextView) view
						.findViewById(R.id.personal_info_other_shielding);

				tv.setText("此人已屏蔽");
			}
			
		}
		menuWindow = new PopupWindow(view, LayoutParams.WRAP_CONTENT,
				LayoutParams.WRAP_CONTENT);
		menuWindow.setFocusable(true);
		menuWindow.setOutsideTouchable(true);
		menuWindow.update();
		menuWindow.setBackgroundDrawable(new BitmapDrawable());
		// 设置layout在PopupWindow中显示的位置
		int h = (int) height * 150 / 1280;
		menuWindow.showAtLocation(this.findViewById(R.id.personal_info_main),
				Gravity.TOP | Gravity.RIGHT, 0, h);
	}

	// 关闭界面
	public void clearActivity() {
		List<Activity> activityList = fxApplication.getActivityList();
		for (int i = 0; i < activityList.size(); i++) {
			activityList.get(i).finish();
		}
		fxApplication.setActivityList();
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {

		switch (resultCode) {
		case -11:
			Bundle bundle = data.getExtras();
			String customName = bundle.getString("customName");
			if (customName != null && !customName.equals(cp.getCustomName())) {
				cp.setCustomName(customName);
				handler.sendEmptyMessage(1);
			}
			// handler.sendEmptyMessage(0);
			break;
		default:
			break;

		}
		super.onActivityResult(requestCode, resultCode, data);

	}



	@SuppressWarnings("deprecation")
	public void authentication_dis(int x, int y, String content) {
		View view = getLayoutInflater().inflate(
				R.layout.authentication_display, null);
		TextView tv = (TextView) view.findViewById(R.id.authentication_text);
		tv.setText(content);
		menuWindow = new PopupWindow(view, LayoutParams.WRAP_CONTENT,
				LayoutParams.WRAP_CONTENT);
		menuWindow.setFocusable(true);
		menuWindow.setOutsideTouchable(true);
		menuWindow.update();
		menuWindow.setBackgroundDrawable(new BitmapDrawable());
		// 设置layout在PopupWindow中显示的位置
		menuWindow.showAtLocation(this.findViewById(R.id.personal_info_main),
				Gravity.TOP | Gravity.LEFT, x, y);
	}

}
