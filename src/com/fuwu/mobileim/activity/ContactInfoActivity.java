package com.fuwu.mobileim.activity;

import java.io.File;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.View.OnTouchListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
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
import com.fuwu.mobileim.util.HttpUtil;
import com.fuwu.mobileim.util.ImageCacheUtil;
import com.fuwu.mobileim.util.Urlinterface;
import com.fuwu.mobileim.view.SlipButton;
import com.fuwu.mobileim.view.SlipButton.OnChangedListener;

/**
 * @作者 马龙
 * @时间 创建时间：2014-6-16 下午5:30:51
 */
public class ContactInfoActivity extends Activity implements OnClickListener,
		OnTouchListener, OnChangedListener {
	private SlipButton personal_info_shielding;
	private TextView name;
	private EditText rem;
	private TextView lisence;
	private TextView sign;
	private TextView fuzhi;
	private ImageView img;
	private ImageView sexView;
	private ImageView img_gou;
	private ImageView img_yue;
	private ContactPojo cp;
	private Button info_ok;
	private int user_id;
	private int contact_id;
	private RelativeLayout personal_info_relativeLayout5;
	private String token;
	private ProgressDialog pd;
	private DBManager db;
	private boolean shielding;
	private Handler handler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			pd.dismiss();
			switch (msg.what) {
			case 1:
				updateData();
				break;
			case 2:
				Toast.makeText(getApplicationContext(), "获取详细信息失败!", 0).show();
				break;
			case 3:

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
				info_ok.setClickable(false);
				info_ok.setTextColor(getResources().getColor(
						R.color.system_textColor2));
				String str = rem.getText().toString();
				db.updateContactRem(user_id, cp.getContactId(), str);
				Toast.makeText(getApplicationContext(), "修改备注成功!", 0).show();
				break;
			case 4:
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
			case 8:
				Toast.makeText(getApplicationContext(), "网络异常", 0).show();
				break;
			case 9:
				
				db = new DBManager(ContactInfoActivity.this);
				if (!db.isOpen()) {
					db = new DBManager(ContactInfoActivity.this);
				}
				if (shielding) {
					Toast.makeText(getApplicationContext(), "屏蔽成功",
							Toast.LENGTH_SHORT).show();
					db.modifyContactBlock(1, user_id, contact_id);
				} else {
					Toast.makeText(getApplicationContext(), "取消屏蔽成功",
							Toast.LENGTH_SHORT).show();
					db.modifyContactBlock(0, user_id, contact_id);
				}
				break;
			case 10:
				if (shielding) {
					Toast.makeText(getApplicationContext(), "屏蔽失败",
							Toast.LENGTH_SHORT).show();
				} else {
					Toast.makeText(getApplicationContext(), "取消屏蔽失败",
							Toast.LENGTH_SHORT).show();
				}
				personal_info_shielding.setCheck(!shielding);
				break;
			case 11:
				personal_info_shielding.setCheck(!shielding);
				Toast.makeText(getApplicationContext(), R.string.no_internet,
						Toast.LENGTH_SHORT).show();
				break;
			}
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.personal_info);
		db = new DBManager(this);
		findViewById(R.id.info_sendBtn).setOnClickListener(this);
		findViewById(R.id.contact_info_back).setOnClickListener(this);
		findViewById(R.id.contact_info_back).setOnTouchListener(this);
		findViewById(R.id.info_ok).setOnClickListener(this);
		// findViewById(R.id.info_rem).setOnClickListener(this);
		personal_info_shielding = (SlipButton) findViewById(R.id.personal_info_shielding);

		personal_info_shielding.setOnChangedListener(this);
		name = (TextView) findViewById(R.id.info_name);
		rem = (EditText) findViewById(R.id.info_rem);

		lisence = (TextView) findViewById(R.id.info_lisence);
		sign = (TextView) findViewById(R.id.info_sign);
		fuzhi = (TextView) findViewById(R.id.info_fuzhi);
		img_gou = (ImageView) findViewById(R.id.info_gouIcon);
		img_yue = (ImageView) findViewById(R.id.info_yueIcon);
		img = (ImageView) findViewById(R.id.info_img); // 头像
		img.setOnClickListener(listener);
		sexView = (ImageView) findViewById(R.id.info_sex);// 性别
		info_ok = (Button) findViewById(R.id.info_ok);// 保存
		info_ok.setClickable(false);
		personal_info_relativeLayout5 = (RelativeLayout) findViewById(R.id.personal_info_relativeLayout5);
		rem.setOnFocusChangeListener(new OnFocusChangeListener() {

			@SuppressLint("ResourceAsColor")
			@Override
			public void onFocusChange(View v, boolean hasFocus) {
				// TODO Auto-generated method stub
				info_ok.setClickable(true);
				info_ok.setTextColor(getResources().getColor(R.color.red_block));
			}
		});
		SharedPreferences sp = getSharedPreferences(Urlinterface.SHARED,
				Context.MODE_PRIVATE);
		user_id = sp.getInt("user_id", 1);
		contact_id = sp.getInt("contact_id", 1);
		token = sp.getString("Token", "token");

		pd = new ProgressDialog(this);
		pd.setMessage("正在加载详细信息...");
		pd.setCanceledOnTouchOutside(false);
		pd.show();

		new GetContactDetail().start();
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
		// ImageCacheUtil.IMAGE_CACHE.get(Urlinterface.head_pic + contact_id,
		// img);
		int isblock = cp.getIsBlocked();
		if (isblock == 1) {
			personal_info_shielding.setCheck(true);
		}
		String face_str = cp.getUserface_url();
		if (face_str != null && face_str.length() > 4) {
			File f = new File(Urlinterface.head_pic, contact_id + "");
			if (f.exists()) {
				img.setTag(Urlinterface.head_pic + contact_id);
				ImageCacheUtil.IMAGE_CACHE.get(Urlinterface.head_pic
						+ contact_id, img);
				if (!ImageCacheUtil.IMAGE_CACHE.get(
						Urlinterface.head_pic + contact_id, img)) {
					img.setImageDrawable(null);
				}
			} else {
				FuXunTools.set_bk(contact_id, face_str, img);
			}

		} else {
			img.setImageResource(R.drawable.moren);
		}
		String str = FuXunTools.toNumber(cp.getSource());
	
		if (FuXunTools.isExist(str, 2, 3)) {
			img_gou.setVisibility(View.VISIBLE);
			if (FuXunTools.isExist(str, 0, 1)) {
				img_yue.setVisibility(View.VISIBLE);
			} else {
				img_yue.setVisibility(View.GONE);
			}
		} else {
			if (FuXunTools.isExist(str, 0, 1)) {
				img_gou.setImageResource(R.drawable.yue);
				img_gou.setVisibility(View.VISIBLE);
			} else {
				img_gou.setVisibility(View.GONE);
			}
			img_yue.setVisibility(View.GONE);
		}
		if (cp.getIsProvider() == 0) {
			personal_info_relativeLayout5.setVisibility(View.GONE);
		}
		int sex = cp.getSex();
		if (sex == 0) {// 男
			sexView.setImageResource(R.drawable.nan);
		} else if (sex == 1) {// 女
			sexView.setImageResource(R.drawable.nv);
		} else {
			sexView.setVisibility(View.GONE);
		}
		name.setText("" + cp.getName());
		rem.setText("" + cp.getCustomName());
		lisence.setText("" + cp.getLisence());
		sign.setText("" + cp.getIndividualResume());
		fuzhi.setText("" + cp.getFuzhi());
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
						cp = new ContactPojo(contactId, sortKey, name,
								customName, userface_url, sex, source,
								lastContactTime, isBlocked, isProvider,
								lisence, individualResume);
						cp.setFuzhi(fuzhi);
						handler.sendEmptyMessage(1);
						Log.i("FuWu", "contact:" + cp.toString());
					} else {
						handler.sendEmptyMessage(2);
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
				String str = rem.getText().toString();
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
						handler.sendEmptyMessage(4);
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
		switch (v.getId()) {
		case R.id.info_sendBtn:
			this.finish();
			Intent intent = new Intent();
			intent.setClass(ContactInfoActivity.this, ChatActivity.class);
			startActivity(intent);
			break;
		case R.id.contact_info_back:
			this.finish();
			break;
		case R.id.info_rem:
			rem.setFocusable(true);
			break;
		case R.id.info_ok:
			String str = rem.getText().toString();
			if (str != null && !str.equals("")) {
				if (FuXunTools.isConnect(ContactInfoActivity.this)) {
					pd = new ProgressDialog(ContactInfoActivity.this);
					pd.setMessage("正在发送请求...");
					pd.setCanceledOnTouchOutside(false);
					pd.show();
					new UpdateContactRem().start();
				} else {
					handler.sendEmptyMessage(7);
				}
			}
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
						handler.sendEmptyMessage(10);
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
			case R.id.contact_info_back:
				Log.i("linshi", "onTouchonTouchonTouchonTouch--my_info_back");
				findViewById(R.id.contact_info_back).getBackground().setAlpha(
						70);
				break;
			}
		} else if (event.getAction() == MotionEvent.ACTION_UP) {
			switch (v.getId()) {
			case R.id.contact_info_back:
				findViewById(R.id.contact_info_back).getBackground().setAlpha(
						255);
				break;
			}
		}
		return false;
	}

}
