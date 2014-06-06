package com.fuwu.mobileim.activity;

import java.io.File;
import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.res.Resources;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.ContactsContract.Profile;
import android.support.v4.app.Fragment;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.TextView;
import android.widget.Toast;

import com.fuwu.mobileim.R;
import com.fuwu.mobileim.model.Models.ClientInfo;
import com.fuwu.mobileim.model.Models.ClientInfoRequest;
import com.fuwu.mobileim.model.Models.ClientInfoResponse;
import com.fuwu.mobileim.model.Models.OSType;
import com.fuwu.mobileim.model.Models.ProfileRequest;
import com.fuwu.mobileim.model.Models.ProfileResponse;
import com.fuwu.mobileim.pojo.ProfilePojo;
import com.fuwu.mobileim.util.FuXunTools;
import com.fuwu.mobileim.util.FxApplication;
import com.fuwu.mobileim.util.HttpUtil;
import com.fuwu.mobileim.util.Urlinterface;
import com.fuwu.mobileim.view.CircularImage;

/**
 * @作者 丁作强
 * @时间 2014-6-6 下午4:48:35
 */
public class SettingsActivity extends Fragment {
	private FxApplication fxApplication;
	private ListView listview;
	SettingBottomAdapter adapter;
	private CircularImage setting_userface;
	private View rootView;
	private Profile profile;
	private ProfilePojo profilePojo = new ProfilePojo();
	private TextView nickName;
	private ImageView setting_sex_item, certification_one, certification_two,
			certification_three;
	private Handler handler = new Handler() {
		/*
		 * (non-Javadoc)
		 * 
		 * @see android.os.Handler#handleMessage(android.os.Message)
		 */
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case 0:
				fxApplication.setProfilePojo(profilePojo);
				setData();
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
		rootView = inflater.inflate(R.layout.settings, container, false);
		fxApplication = (FxApplication) getActivity().getApplication();
		adapter = new SettingBottomAdapter();

		listview = (ListView) rootView.findViewById(R.id.setting_listview);
		listview.setDivider(null);
		listview.setAdapter(adapter);
		listview.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {

				jumpToActivity(position);

			}
		});
		init();

		SharedPreferences preferences = getActivity().getSharedPreferences(
				Urlinterface.SHARED, Context.MODE_PRIVATE);

		int profile_userid = preferences.getInt("profile_userid", -1);
		if (profile_userid != -1) {
			Log.i("linshi------------", "profileprofileprofileprofile本地shuju");
			String name = preferences.getString("profile_name", "");// 名称
			String nickName = preferences.getString("profile_nickName", "");// 昵称
			int gender = preferences.getInt("profile_gender", -1);
			;// 性别
			String tileUrl = preferences.getString("profile_tileUrl", "");// 头像
			Boolean isProvider = preferences.getBoolean("profile_isProvider",
					false);//
			String lisence = preferences.getString("profile_lisence", "");// 行业认证
			String mobile = preferences.getString("profile_mobile", "");// 手机号码
			String email = preferences.getString("profile_email", "");// 邮箱
			String birthday = preferences.getString("profile_birthday", "");// 生日

			profilePojo = new ProfilePojo(profile_userid, name, nickName,
					gender, tileUrl, isProvider, lisence, mobile, email,
					birthday);
			handler.sendEmptyMessage(0);

		} else {
			Thread thread = new Thread(new getProfile());
			thread.start();
		}

		return rootView;
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
				ProfileRequest.Builder builder = ProfileRequest.newBuilder();
				builder.setUserId(fxApplication.getUser_id());
				builder.setToken(fxApplication.getToken());
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

						profilePojo = new ProfilePojo(userId, name, nickName,
								gender, tileUrl, isProvider, lisence, mobile,
								email, birthday);
						putProfile(profilePojo);
						Log.i("linshi", "  --nickName" + nickName
								+ "  --gender" + gender + "  --tileUrl"
								+ tileUrl + "  --lisence" + lisence
								+ "  --mobile" + mobile + "  --email" + email
								+ "  birthday--" + birthday);
						Log.i("linshi------------",
								"profileprofileprofileprofile网络shuju");
						Message msg = new Message();// 创建Message 对象
						msg.what = 0;
						handler.sendMessage(msg);
					} else {

					}
				}

				// handler.sendEmptyMessage(0);
			} catch (Exception e) {
				// prodialog.dismiss();
				handler.sendEmptyMessage(7);
			}
		}
	}

	private void putProfile(ProfilePojo pro) {
		SharedPreferences preferences = getActivity().getSharedPreferences(
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
		editor.commit();

	}

	/**
	 * 
	 * 获得相关组件
	 * 
	 * 
	 */
	private void init() {
		RelativeLayout setting_top = (RelativeLayout) rootView
				.findViewById(R.id.setting_top);// 用户个人信息部分
		RelativeLayout a_layout = (RelativeLayout) rootView
				.findViewById(R.id.setting_userface0);
		setting_userface = (CircularImage) rootView
				.findViewById(R.id.setting_userface);// 头像
		nickName = (TextView) rootView.findViewById(R.id.setting_teachername);// 昵称

		setting_sex_item = (ImageView) rootView
				.findViewById(R.id.setting_sex_item);// 性别
		certification_one = (ImageView) rootView
				.findViewById(R.id.certification_one);// 验证1
		certification_two = (ImageView) rootView
				.findViewById(R.id.certification_two);// 验证2
		certification_three = (ImageView) rootView
				.findViewById(R.id.certification_three);// 验证3
		LayoutParams param = (LayoutParams) a_layout.getLayoutParams();
		param.leftMargin = 40;
		param.topMargin = 50;
		RelativeLayout setting_relativeLayout1 = (RelativeLayout) rootView
				.findViewById(R.id.setting_relativeLayout1);
		LayoutParams param2 = (LayoutParams) setting_relativeLayout1
				.getLayoutParams();
		param2.leftMargin = 30;
		param2.topMargin = 38;
		setting_top.setOnClickListener(listener1);// 给个人信息部分设置监听
	}

	/**
	 * 
	 * 设置对应的数据信息
	 * 
	 * 
	 */
	private void setData() {

		// 设置头像
		String face_str = profilePojo.getTileUrl();
		if (face_str.length() > 4) {
			FuXunTools.setBackground(face_str, setting_userface);
			File f = new File(Urlinterface.head_pic, profilePojo.getUserId()
					+ "");
			if (f.exists()) {
				Log.i("linshi------------", "加载本地图片");
				Drawable dra = new BitmapDrawable(
						BitmapFactory.decodeFile(Urlinterface.head_pic
								+ profilePojo.getUserId()));
				setting_userface.setImageDrawable(dra);
			} else {
				FuXunTools.set_bk(profilePojo.getUserId(), face_str,
						setting_userface);
			}
		} else {
			setting_userface.setImageResource(R.drawable.moren);
		}
		// 设置昵称
		nickName.setText(profilePojo.getNickName());
		// 设置性别
		int sex = profilePojo.getGender();
		if (sex == 0) {// 男
			setting_sex_item.setImageResource(R.drawable.nan);
		} else if (sex == 1) {// 女
			setting_sex_item.setImageResource(R.drawable.nv);
		} else {
			setting_sex_item.setVisibility(View.GONE);
		}
		// 设置行业认证
		String str1 = profilePojo.getLisence();
		if (str1 != null && !("").equals(str1)) {
			certification_one.setVisibility(View.VISIBLE);
		} else {
			certification_one.setVisibility(View.GONE);
		}

		// 设置邮箱认证
		String str2 = profilePojo.getEmail();
		if (str2 != null && !("").equals(str2)) {
			certification_two.setVisibility(View.VISIBLE);
		} else {
			certification_two.setVisibility(View.GONE);
		}
		// 设置手机验证
		String str3 = profilePojo.getMobile();
		if (str2 != null && !("").equals(str3)) {
			certification_three.setVisibility(View.VISIBLE);
		} else {
			certification_three.setVisibility(View.GONE);
		}

	}

	private View.OnClickListener listener1 = new View.OnClickListener() {
		@Override
		public void onClick(View v) {
			// Toast.makeText(getActivity().getApplication(), "跳到个人信息页面",
			// Toast.LENGTH_LONG).show();
			Intent intent = new Intent();
			intent.setClass(getActivity(), MyInformationActivity.class);
			startActivity(intent);
		}
	};

	/**
	 * 跳转到功能页面
	 * 
	 */
	private void jumpToActivity(int num) {
		Intent intent = new Intent();
		switch (num) {
		case 0:// 新版本检测
			TelephonyManager tm = (TelephonyManager) getActivity()
					.getSystemService(Context.TELEPHONY_SERVICE);
			StringBuilder sb = new StringBuilder();
			sb.append("\nDeviceId(IMEI) = " + tm.getDeviceId());
			String release = android.os.Build.VERSION.RELEASE; // android系统版本号
			sb.append("\nAndroid： = " + release);
			Log.e("info", sb.toString());
			Toast.makeText(getActivity().getApplication(), "新版本检测" + sb,
					Toast.LENGTH_LONG).show();
			break;
		case 1:// 清除全部聊天记录
				// Toast.makeText(getActivity().getApplication(), "清除全部聊天记录",
				// Toast.LENGTH_LONG).show();
			deleteAllChatRecords();
			break;
		case 2:// 消息推送
			intent.setClass(getActivity(), PushSettingActivity.class);
			startActivity(intent);
			break;
		case 3:// 修改密码
			intent.setClass(getActivity(), UpdatePwdActivity.class);
			startActivity(intent);
			break;
		case 4:// 屏蔽管理
				// Toast.makeText(getActivity().getApplication(), "屏蔽管理" ,
				// Toast.LENGTH_LONG).show();
			intent.setClass(getActivity(), BlockManagementActivity.class);
			startActivity(intent);
			break;
		case 5:// 系统公告管理
				// intent.setClass(getActivity(), SystemPushActivity.class);
				// startActivity(intent);
			Toast.makeText(getActivity().getApplication(), "该功能暂不实现",
					Toast.LENGTH_LONG).show();
			break;
		case 6:// 退出登录
			intent.setClass(getActivity(), LoginActivity.class);
			startActivity(intent);
			clearActivity();
			fxApplication.initData();
			SharedPreferences preferences = getActivity().getSharedPreferences(
					Urlinterface.SHARED, Context.MODE_PRIVATE);
			Editor editor = preferences.edit();
			editor.putInt("profile_userid", -1);
			editor.commit();
			break;
		default:
			break;
		}
	}

	public class SettingBottomAdapter extends BaseAdapter {

		private int[] icon = new int[] { R.drawable.setting_image1,
				R.drawable.setting_image2, R.drawable.setting_image3,
				R.drawable.setting_image4, R.drawable.setting_image5,
				R.drawable.setting_image6, R.drawable.setting_image7 }; // icon
																		// 集合
		private String[] titleArr = new String[] { "新版本检测", "清除全部聊天记录", "消息推送",
				"修改密码", "屏蔽管理", "系统公告管理", "退出登录" }; //

		public int getCount() {
			return titleArr.length;
		}

		public Object getItem(int position) {
			return null;
		}

		public long getItemId(int position) {
			return 0;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			RelativeLayout layout = null;
			if (convertView == null) {
				layout = (RelativeLayout) LayoutInflater.from(getActivity())
						.inflate(R.layout.setting_adapter_item, null);
			} else {
				layout = (RelativeLayout) convertView;
			}
			ImageView im = (ImageView) layout
					.findViewById(R.id.setting_adapter_item_iv);
			TextView titleStr = (TextView) layout.findViewById(R.id.titleStr);
			Resources resources = getResources();
			im.setImageResource(icon[position]);
			titleStr.setText(titleArr[position]);
			RelativeLayout re = (RelativeLayout) layout
					.findViewById(R.id.notice_sign);
			TextView te = (TextView) layout.findViewById(R.id.notice_number);

			if (position == 5) {
				// 如果有通知，则显示通知数目
				if (true) {
					te.setText("3");
				} else {
					re.setVisibility(View.GONE);
				}
			} else { // 当前postion 不为5时，隐藏黑色线条和圆形红色块
				View view = (View) layout.findViewById(R.id.item_thicklines);
				view.setVisibility(View.GONE);
				re.setVisibility(View.GONE);
			}
			return layout;
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

	/*
	 * 删除所有记录
	 */
	public void deleteAllChatRecords() {
		Dialog dialog = new AlertDialog.Builder(getActivity())
				.setTitle("提示")
				.setMessage("您确认要删除全部聊天记录么?")
				.setPositiveButton("确认", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						Toast.makeText(getActivity().getApplication(),
								"清除全部聊天记录", Toast.LENGTH_LONG).show();
					}
				})
				.setNegativeButton("取消", new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
					}
				}).create();
		dialog.show();

	}

	/**
	 * 
	 * 版本检测
	 * 
	 * 
	 */

	class VersionChecking implements Runnable {
		public void run() {
			try {
				TelephonyManager tm = (TelephonyManager) getActivity()
						.getSystemService(Context.TELEPHONY_SERVICE);
				StringBuilder sb = new StringBuilder();
				sb.append("\nDeviceId(IMEI) = " + tm.getDeviceId());
				String release = android.os.Build.VERSION.RELEASE; // android系统版本号

				ClientInfo.Builder pb = ClientInfo.newBuilder();
				pb.setDeviceId(tm.getDeviceId());
				pb.setOsType(OSType.Android);
				pb.setOSVersion(release);
				pb.setUserId(profilePojo.getUserId());
				pb.setChannel(0);
				pb.setClientVersion(Urlinterface.current_version + "");
				pb.setIsPushEnable(true);
				Log.i("linshi", "-----------------");
				ClientInfoRequest.Builder builder = ClientInfoRequest
						.newBuilder();
				builder.setUserId(fxApplication.getUser_id());
				builder.setToken(fxApplication.getToken());
				builder.setClientInfo(pb);
				ClientInfoRequest response = builder.build();

				byte[] by = HttpUtil.sendHttps(response.toByteArray(),
						Urlinterface.ChangeProfile, "PUT");
				if (by != null && by.length > 0) {

					ClientInfoResponse res = ClientInfoResponse.parseFrom(by);
					if (res.getIsSucceed()) {
						handler.sendEmptyMessage(0);
					} else {
						handler.sendEmptyMessage(1);
					}
				} else {
					handler.sendEmptyMessage(6);
				}
				//
			} catch (Exception e) {
				handler.sendEmptyMessage(7);
			}
		}
	}
}
