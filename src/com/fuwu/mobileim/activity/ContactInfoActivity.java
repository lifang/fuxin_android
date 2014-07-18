package com.fuwu.mobileim.activity;

import java.io.File;
import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Typeface;
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
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.fuwu.mobileim.R;
import com.fuwu.mobileim.model.Models.BlockContactRequest;
import com.fuwu.mobileim.model.Models.BlockContactResponse;
import com.fuwu.mobileim.model.Models.ChangeContactDetailRequest;
import com.fuwu.mobileim.model.Models.ChangeContactDetailResponse;
import com.fuwu.mobileim.model.Models.Contact;
import com.fuwu.mobileim.model.Models.ContactDetailRequest;
import com.fuwu.mobileim.model.Models.ContactDetailResponse;
import com.fuwu.mobileim.pojo.ContactPojo;
import com.fuwu.mobileim.util.DBManager;
import com.fuwu.mobileim.util.FuXunTools;
import com.fuwu.mobileim.util.FxApplication;
import com.fuwu.mobileim.util.HttpUtil;
import com.fuwu.mobileim.util.Urlinterface;
import com.fuwu.mobileim.view.SlipButton.OnChangedListener;

/**
 * @作者 马龙
 * @时间 创建时间：2014-6-16 下午5:30:51
 */
public class ContactInfoActivity extends Activity implements OnClickListener,
		OnTouchListener, OnChangedListener {
	private TextView name, lisence, sign, fuzhi, sex, location;
	private ImageView img;
	private ContactPojo cp = null;
	private int user_id;
	private int contact_id;
	private RelativeLayout personal_info_relativeLayout5;
	private String token;
	private ProgressDialog pd;
	private DBManager db;
	private boolean shielding;
	private FxApplication fxApplication;
	private PopupWindow menuWindow;
	private boolean isPlusShow = false;
	private ImageView mOther; // 右上方按钮
	private float height = 0;
	private Handler handler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);

			switch (msg.what) {
			case 1:
				pd.dismiss();
				updateData();
				break;
			case 2:
				pd.dismiss();
				Toast.makeText(getApplicationContext(), "获取详细信息失败!", 0).show();
				break;
			case 3:
				pd.dismiss();
				InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
				boolean isOpen = imm.isActive();
				if (isOpen) {
					imm.hideSoftInputFromWindow(ContactInfoActivity.this
							.getCurrentFocus().getWindowToken(),
							InputMethodManager.HIDE_NOT_ALWAYS);
				}
				findViewById(R.id.personal_info_customName).setFocusable(true);
				findViewById(R.id.personal_info_customName)
						.setFocusableInTouchMode(true);
				findViewById(R.id.personal_info_customName).requestFocus();
				// info_ok.setClickable(false);
				// info_ok.setTextColor(getResources().getColor(
				// R.color.system_textColor2));
				// String str = rem.getText().toString();
				// db.updateContactRem(user_id, cp.getContactId(), str);
				Toast.makeText(getApplicationContext(), "修改备注成功!", 0).show();
				break;
			case 4:
				pd.dismiss();
				Toast.makeText(getApplicationContext(), "修改备注失败!", 0).show();
				break;
			case 6:
				Toast.makeText(getApplicationContext(), "请求失败",
						Toast.LENGTH_SHORT).show();
				break;
			case 7:
				Toast.makeText(getApplicationContext(), R.string.no_internet,
						Toast.LENGTH_SHORT).show();
				break;
			case 9:
				pd.dismiss();
				db = new DBManager(ContactInfoActivity.this);
				if (!db.isOpen()) {
					db = new DBManager(ContactInfoActivity.this);
				}
				if (shielding) {
					Toast.makeText(getApplicationContext(), "屏蔽成功",
							Toast.LENGTH_SHORT).show();
					cp.setIsBlocked(1);
					db.modifyContactBlock(1, user_id, contact_id);
				} else {
					cp.setIsBlocked(0);
					Toast.makeText(getApplicationContext(), "取消屏蔽成功",
							Toast.LENGTH_SHORT).show();
					db.modifyContactBlock(0, user_id, contact_id);
				}
				break;
			case 10:
				pd.dismiss();
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
				pd.dismiss();
				new Handler().postDelayed(new Runnable() {
					public void run() {
						Intent intent = new Intent(ContactInfoActivity.this,
								LoginActivity.class);
						startActivity(intent);
						clearActivity();
					}
				}, 3500);
				Toast.makeText(getApplicationContext(), "您的账号已在其他手机登陆",
						Toast.LENGTH_LONG).show();
				break;
			}
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.personal_info);
		ViewGroup vg = (ViewGroup) findViewById(R.id.personal_info_main);
//		FuXunTools.changeFonts(vg, ContactInfoActivity.this);
		db = new DBManager(this);
		fxApplication = (FxApplication) getApplication();
		fxApplication.getActivityList().add(this);
		DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
		height = displayMetrics.heightPixels;
		InitView();

		SharedPreferences sp = getSharedPreferences(Urlinterface.SHARED,
				Context.MODE_PRIVATE);
		user_id = sp.getInt("user_id", 1);
		contact_id = sp.getInt("contact_id", 1);
		token = sp.getString("Token", "token");

		if (FuXunTools.isConnect(ContactInfoActivity.this)) {
			pd = new ProgressDialog(this);
			pd.setMessage("正在加载详细信息...");
			pd.setCanceledOnTouchOutside(false);
			pd.show();

			new GetContactDetail().start();
		} else {
			handler.sendEmptyMessage(7);
		}

	}

	public void InitView() {
		findViewById(R.id.info_sendBtn).setOnClickListener(this);
		findViewById(R.id.contact_info_back).setOnClickListener(this);
		findViewById(R.id.contact_info_back).setOnTouchListener(this);
		findViewById(R.id.chat_other).setOnTouchListener(this);
		name = (TextView) findViewById(R.id.info_name);
		mOther = (ImageView) findViewById(R.id.chat_other);
		mOther.setOnTouchListener(this);
		mOther.setOnClickListener(this);
		lisence = (TextView) findViewById(R.id.info_lisence);// 认证行业
		sign = (TextView) findViewById(R.id.info_sign);// 个人简介
   
   
   
		fuzhi = (TextView) findViewById(R.id.info_fuzhi); // 福指
		img = (ImageView) findViewById(R.id.info_img); // 头像
		sex = (TextView) findViewById(R.id.info_sex); // 性别
		location = (TextView) findViewById(R.id.info_location); // 地址

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
		// 认证行业
		lisence.setText("" + cp.getLisence());
		// 个人简介
		sign.setText("" + cp.getIndividualResume());
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
		
		if (cp.getIsProvider()==1) {
			findViewById(R.id.personal_info_fz).setVisibility(View.VISIBLE);
			findViewById(R.id.personal_info_gerenjianjie).setVisibility(View.VISIBLE);
			findViewById(R.id.personal_info_hangye).setVisibility(View.VISIBLE);
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
						if (customName != null && customName.length() > 0) {
							sortKey = FuXunTools.findSortKey(customName);
						} else {
							sortKey = FuXunTools.findSortKey(name);
						}
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
//						contact.getLicensesList()
						cp = new ContactPojo(contactId, sortKey, name,
								customName, userface_url, sex, source,
								lastContactTime, isBlocked, isProvider,
								lisence, individualResume, fuzhi, location);
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

	class UpdateContactRem extends Thread {
		public void run() {
			try {
				ChangeContactDetailRequest.Builder builder = ChangeContactDetailRequest
						.newBuilder();
				builder.setUserId(user_id);
				builder.setToken(token);
				Contact.Builder cb = Contact.newBuilder();
				cb.setContactId(cp.getContactId());
				String str = "";
				cb.setCustomName(str);
				builder.setContact(cb);
				ChangeContactDetailRequest response = builder.build();
				byte[] by = HttpUtil.sendHttps(response.toByteArray(),
						Urlinterface.ContactDetail, "PUT");
				if (by != null && by.length > 0) {
					ChangeContactDetailResponse res = ChangeContactDetailResponse
							.parseFrom(by);
					if (res.getIsSucceed()) {
						handler.sendEmptyMessage(3);
					} else {
						int ErrorCode = res.getErrorCode().getNumber();
						if (ErrorCode == 2001) {
							handler.sendEmptyMessage(12);
						} else {
							handler.sendEmptyMessage(4);
						}
					}
				} else {
					handler.sendEmptyMessage(4);
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
			intent = new Intent();
			intent.putExtra("customName", cp.getCustomName());
			intent.setClass(ContactInfoActivity.this, OpenInputMethod.class);
			startActivityForResult(intent, 0);
			break;
		case R.id.personal_info_other_shielding: // 屏蔽此人
			if (FuXunTools.isConnect(ContactInfoActivity.this)) {
				pd = new ProgressDialog(this);
				pd.setMessage("正在屏蔽联系人...");
				pd.setCanceledOnTouchOutside(false);
				pd.show();
				shielding = true;
				Thread thread = new Thread(new BlockContact());
				thread.start();
			} else {
				handler.sendEmptyMessage(11);
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

	@Override
	public void onChanged(boolean checkState, View v) {
		switch (v.getId()) {
		case R.id.personal_info_shielding:
			if (checkState) {

				if (FuXunTools.isConnect(ContactInfoActivity.this)) {
					pd = new ProgressDialog(this);
					pd.setMessage("正在屏蔽联系人...");
					pd.setCanceledOnTouchOutside(false);
					pd.show();
					shielding = true;
					Thread thread = new Thread(new BlockContact());
					thread.start();
				} else {
					handler.sendEmptyMessage(11);
				}

			} else {

				if (FuXunTools.isConnect(ContactInfoActivity.this)) {
					pd = new ProgressDialog(this);
					pd.setMessage("取消屏蔽...");
					pd.setCanceledOnTouchOutside(false);
					pd.show();
					shielding = false;
					Thread thread = new Thread(new BlockContact());
					thread.start();
				} else {
					handler.sendEmptyMessage(11);
				}
			}
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

	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			// spf.edit().putString("Token", "null").commit();
			Dialog dialog = new AlertDialog.Builder(ContactInfoActivity.this)
					.setTitle("提示")
					.setMessage("您确认要退出应用么?")
					.setPositiveButton("确认",
							new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog,
										int which) {

									clearActivity();
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
}
