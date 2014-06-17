package com.fuwu.mobileim.activity;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.fuwu.mobileim.R;
import com.fuwu.mobileim.model.Models.Contact;
import com.fuwu.mobileim.model.Models.ContactDetailRequest;
import com.fuwu.mobileim.model.Models.ContactDetailResponse;
import com.fuwu.mobileim.pojo.ContactPojo;
import com.fuwu.mobileim.util.FuXunTools;
import com.fuwu.mobileim.util.HttpUtil;
import com.fuwu.mobileim.util.ImageCacheUtil;
import com.fuwu.mobileim.util.Urlinterface;

/**
 * @作者 马龙
 * @时间 创建时间：2014-6-16 下午5:30:51
 */
public class ContactInfoActivity extends Activity implements OnClickListener {

	ContactPojo cp;
	int user_id;
	int contact_id;
	String token;
	ProgressDialog pd;
	Handler handler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			pd.dismiss();
			switch (msg.what) {
			case 1:
				initView();
				break;
			case 2:
				Toast.makeText(getApplicationContext(), "获取详细信息失败!", 0).show();
				break;
			}
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.contact_info);
		findViewById(R.id.info_sendBtn).setOnClickListener(this);
		findViewById(R.id.contact_info_back).setOnClickListener(this);
		SharedPreferences sp = getSharedPreferences(Urlinterface.SHARED,
				Context.MODE_PRIVATE);
		user_id = sp.getInt("user_id", 1);
		contact_id = sp.getInt("contact_id", 1);
		token = sp.getString("Token", "token");

		pd = new ProgressDialog(this);
		pd.setMessage("正在加载详细信息...");
		pd.show();

		new GetContactDetail().start();
	}

	public void initView() {
		TextView name = (TextView) findViewById(R.id.info_name);
		View fzLine = findViewById(R.id.info_fzLine);
		View rzLine = findViewById(R.id.info_rzLine);
		LinearLayout fz = (LinearLayout) findViewById(R.id.info_fz);
		LinearLayout rz = (LinearLayout) findViewById(R.id.info_rz);
		LinearLayout jj = (LinearLayout) findViewById(R.id.info_jj);
		TextView lisence = (TextView) findViewById(R.id.info_lisence);
		TextView sign = (TextView) findViewById(R.id.info_sign);
		TextView fuzhi = (TextView) findViewById(R.id.info_fuzhi);
		ImageView img = (ImageView) findViewById(R.id.info_img);
		ImageView img_gou = (ImageView) findViewById(R.id.info_gouIcon);
		ImageView img_yue = (ImageView) findViewById(R.id.info_yueIcon);
		ImageCacheUtil.IMAGE_CACHE.get(Urlinterface.head_pic + contact_id, img);

		String str = FuXunTools.toNumber(cp.getSource());
		if (FuXunTools.isExist(str, 0, 1)) {
			img_yue.setVisibility(View.VISIBLE);
		} else {
			img_yue.setVisibility(View.GONE);
		}
		if (FuXunTools.isExist(str, 2, 3)) {
			img_gou.setVisibility(View.VISIBLE);
		} else {
			img_gou.setVisibility(View.GONE);
		}
		if (cp.getIsProvider() == 0) {
			fz.setVisibility(View.GONE);
			rz.setVisibility(View.GONE);
			jj.setVisibility(View.GONE);
			fzLine.setVisibility(View.GONE);
			rzLine.setVisibility(View.GONE);
		}
		name.setText("" + cp.getName());
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

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.info_sendBtn:
			Intent intent = new Intent();
			intent.setClass(ContactInfoActivity.this, ChatActivity.class);
			startActivity(intent);
			break;
		case R.id.contact_info_back:
			this.finish();
			break;
		}
	}

}