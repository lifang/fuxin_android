﻿package com.fuwu.mobileim.activity;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.ContactsContract.Profile;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.mobstat.StatService;
import com.fuwu.mobileim.R;
import com.fuwu.mobileim.model.Models.ClientInfo;
import com.fuwu.mobileim.model.Models.ClientInfoRequest;
import com.fuwu.mobileim.model.Models.ClientInfoResponse;
import com.fuwu.mobileim.model.Models.ProfileRequest;
import com.fuwu.mobileim.model.Models.ProfileResponse;
import com.fuwu.mobileim.model.Models.UnAuthenticationRequest;
import com.fuwu.mobileim.model.Models.UnAuthenticationResponse;
import com.fuwu.mobileim.model.Models.ClientInfo.OSType;
import com.fuwu.mobileim.pojo.ProfilePojo;
import com.fuwu.mobileim.util.DBManager;
import com.fuwu.mobileim.util.FuXunTools;
import com.fuwu.mobileim.util.FxApplication;
import com.fuwu.mobileim.util.HttpUtil;
import com.fuwu.mobileim.util.ImageCacheUtil;
import com.fuwu.mobileim.util.Urlinterface;
import com.fuwu.mobileim.view.CircularImage;
import com.google.protobuf.InvalidProtocolBufferException;

/**
 * @作者 丁作强
 * @时间 2014-6-6 下午4:48:35
 */
public class SettingsActivity extends Fragment implements Urlinterface {
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
	/* 更新进度条 */
	private ProgressBar mProgress;
	private Dialog DownloadDialog;
	private boolean cancelUpdate = false;
	/* 下载保存路径 */
	private String SavePath;
	/* 记录进度条数量 */
	private int progress;
	private String fileurl = "";
	private DBManager db;
	int dataNumber = 0; // 0 数据没加载完，1 数据加载完
	private ProgressDialog prodialog;
	private Handler handler = new Handler() {
		/*
		 * (non-Javadoc)
		 * 
		 * @see android.os.Handler#handleMessage(android.os.Message)
		 */
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case 0:
				// fxApplication.setProfilePojo(profilePojo);
				putProfile(profilePojo);
//				setData();
				break;
			case 5:
				prodialog.dismiss();
				Intent intent = new Intent();
				preferences.edit().putString("pwd", "").commit();
				preferences.edit().putString("clientid", "").commit();
				intent.setClass(getActivity(), LoginActivity.class);
				startActivity(intent);
				clearActivity();
				fxApplication.initData();
				break;
			case 6:
				Toast.makeText(getActivity(), "请求失败", Toast.LENGTH_SHORT)
						.show();
				break;
			case 7:
				Toast.makeText(getActivity(),  R.string.no_internet, Toast.LENGTH_SHORT)
						.show();
				break;
			case 8:

				Builder builder = new Builder(getActivity());
				builder.setTitle("提示");
				builder.setMessage("检测到新版本,您需要更新吗？");
				builder.setPositiveButton("确定",
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog,
									int which) {
								showDownloadDialog_table();
							}
						});
				builder.setNegativeButton("下次再说",
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog,
									int which) {
							}
						}).show();
				break;
			case 9:
				// 设置进度条位置
				mProgress.setProgress(progress);
				break;
			case 10:
				// 安装文件
				installApk();
			case 11:
				Toast.makeText(getActivity(), "当前已是最新版本", Toast.LENGTH_SHORT)
						.show();
				break;
			}
		}
	};
	SharedPreferences preferences;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		rootView = inflater.inflate(R.layout.settings, container, false);
		fxApplication = (FxApplication) getActivity().getApplication();
		db = new DBManager(getActivity());
		adapter = new SettingBottomAdapter();
		preferences = getActivity().getSharedPreferences(Urlinterface.SHARED,
				Context.MODE_PRIVATE);
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
		if (FuXunTools.isConnect(getActivity())) {
			Thread thread = new Thread(new getProfile());
			thread.start();
		} else {
			Toast.makeText(getActivity(), R.string.no_internet,
					Toast.LENGTH_SHORT).show();
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
				int user_id = preferences.getInt("user_id", -1);
				String Token = preferences.getString("Token", "");
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
						profilePojo = new ProfilePojo(userId, name, nickName,
								gender, tileUrl, isProvider, lisence, mobile,
								email, birthday, isAuthentication, fuzhi);
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
				Log.i("Max", e.toString());
				handler.sendEmptyMessage(7);
			}
		}
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
		// LayoutParams param = (LayoutParams) a_layout.getLayoutParams();
		// param.leftMargin = 40;
		// param.topMargin = 50;
		// RelativeLayout setting_relativeLayout1 = (RelativeLayout) rootView
		// .findViewById(R.id.setting_relativeLayout1);
		// LayoutParams param2 = (LayoutParams) setting_relativeLayout1
		// .getLayoutParams();
		// param2.leftMargin = 30;
		// param2.topMargin = 38;
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
		Log.i("Ax", "profilePojo.getTileUrl()" + profilePojo.getTileUrl());
		if (face_str != null && face_str.length() > 4) {
				FuXunTools.set_bk(profilePojo.getUserId(), face_str,
						setting_userface);
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
		Boolean str1 = profilePojo.getIsAuthentication();
		if (str1) {
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
			intent.putExtra("dataNumber", dataNumber);
			intent.setClass(getActivity(), MyInformationActivity.class);
			startActivityForResult(intent, 0);
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
			if (FuXunTools.isConnect(getActivity())) {
			new VersionChecking().start();
			} else {
				Toast.makeText(getActivity(), R.string.no_internet,
						Toast.LENGTH_SHORT).show();
			}
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
		// case 5:// 系统公告管理
		// // intent.setClass(getActivity(), SystemPushActivity.class);
		// // startActivity(intent);
		// Toast.makeText(getActivity().getApplication(), "该功能暂不实现",
		// Toast.LENGTH_LONG).show();
		// break;
		case 5:// 退出登录
			prodialog = new ProgressDialog(getActivity());
			prodialog.setMessage("努力退出中..");
			prodialog.setCanceledOnTouchOutside(false);
			prodialog.show();
			new Thread(new UnAuthentication()).start();
			break;
		default:
			break;
		}
	}

	public class SettingBottomAdapter extends BaseAdapter {

		private int[] icon = new int[] { R.drawable.setting_image1,
				R.drawable.setting_image2, R.drawable.setting_image3,
				R.drawable.setting_image4, R.drawable.setting_image5,
				R.drawable.setting_image7 }; // icon
												// 集合
		private String[] titleArr = new String[] { "新版本检测", "清除全部聊天记录", "消息推送",
				"修改密码", "屏蔽管理", "退出登录" }; //

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
		public View getView(int position, View view, ViewGroup parent) {
			RelativeLayout layout = null;
			// if (convertView == null) {
			// layout = (RelativeLayout) LayoutInflater.from(getActivity())
			// .inflate(R.layout.setting_adapter_item, null);
			// } else {
			// layout = (RelativeLayout) convertView;
			// }

			ViewHolder viewHolder = null;
			if (view == null) {
				viewHolder = new ViewHolder();
				view = LayoutInflater.from(getActivity()).inflate(
						R.layout.setting_adapter_item, null);
				viewHolder.titleStr = (TextView) view
						.findViewById(R.id.titleStr);
				viewHolder.te = (TextView) view
						.findViewById(R.id.notice_number);
				viewHolder.re = (RelativeLayout) view
						.findViewById(R.id.notice_sign);
				viewHolder.im = (ImageView) view
						.findViewById(R.id.setting_adapter_item_iv);
				viewHolder.view = (View) view
						.findViewById(R.id.item_thicklines);

				view.setTag(viewHolder);
			} else {
				viewHolder = (ViewHolder) view.getTag();
			}
			viewHolder.im.setImageResource(icon[position]);
			viewHolder.titleStr.setText(titleArr[position]);

			if (position == 4) {
				// 如果有通知，则显示通知数目
				if (true) {
					// te.setText("3");
					viewHolder.re.setVisibility(View.GONE);
				} else {
					viewHolder.re.setVisibility(View.GONE);
				}
			} else { // 当前postion 不为5时，隐藏黑色线条和圆形红色块
				viewHolder.view.setVisibility(View.GONE);
				viewHolder.re.setVisibility(View.GONE);
			}
			return view;
		}

		final class ViewHolder {
			TextView titleStr; // 功能标题
			TextView te; // 公告数量
			RelativeLayout re; //
			ImageView im; // 功能图标
			View view; // 底部黑色线
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
		Dialog dialog = new AlertDialog.Builder(getActivity()).setTitle("提示")
				.setMessage("您确认要删除全部聊天记录么?")
				.setPositiveButton("确认", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						// Toast.makeText(getActivity().getApplication(),
						// "清除全部聊天记录", Toast.LENGTH_LONG).show();
						int user_id = preferences.getInt("user_id", -1);
						db.delMessage(user_id);
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
	 */

	class VersionChecking extends Thread {
		public void run() {
			try {
				// TelephonyManager tm = (TelephonyManager) getActivity()
				// .getSystemService(Context.TELEPHONY_SERVICE);
				String release = android.os.Build.VERSION.RELEASE; // android系统版本号

				ClientInfo.Builder pb = ClientInfo.newBuilder();
				// pb.setDeviceId(tm.getDeviceId());
				String deviceId = preferences.getString("clientid", "");
				int user_id = preferences.getInt("user_id", -1);
				String Token = preferences.getString("Token", "");
				pb.setDeviceId(deviceId);
				pb.setOSVersion(release);
				pb.setUserId(user_id);
				pb.setChannel(0);
				pb.setClientVersion(Urlinterface.current_version + "");
				pb.setIsPushEnable(true);
				Log.i("linshi", "-----------------");
				ClientInfoRequest.Builder builder = ClientInfoRequest
						.newBuilder();
				builder.setUserId(user_id);
				builder.setToken(Token);
				builder.setClientInfo(pb);
				ClientInfoRequest response = builder.build();

				byte[] by = HttpUtil.sendHttps(response.toByteArray(),
						Urlinterface.Client, "PUT");
				if (by != null && by.length > 0) {

					ClientInfoResponse res = ClientInfoResponse.parseFrom(by);
					if (res.getIsSucceed()) {
						if (res.getHasNewVersion()) {
							// 新版本提示
							fileurl = res.getClientUrl();
							handler.sendEmptyMessage(8);
						} else {
							handler.sendEmptyMessage(11);
						}

					} else {
						handler.sendEmptyMessage(6);
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

	public void showDownloadDialog_table() {
		// 构造软件下载对话框
		AlertDialog.Builder builder = new Builder(getActivity());
		builder.setTitle("正在更新");
		// 给下载对话框增加进度条
		final LayoutInflater inflater = LayoutInflater.from(getActivity());
		View v = inflater.inflate(R.layout.softupdate_progress, null);
		mProgress = (ProgressBar) v.findViewById(R.id.update_progress);
		builder.setView(v);
		// 取消更新
		builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
				// 设置取消状态
				cancelUpdate = true;
			}
		});
		DownloadDialog = builder.create();
		DownloadDialog.setCanceledOnTouchOutside(false);
		DownloadDialog.show();
		// 现在文件
		downloadApk_table();
	}

	/**
	 * 下载文件线程
	 * 
	 * @author coolszy
	 * @date 2012-4-26
	 * @blog http://blog.92coding.com
	 */
	public class downloadApkThread_table extends Thread {
		@Override
		public void run() {
			try {
				// 判断SD卡是否存在，并且是否具有读写权限
				if (Environment.getExternalStorageState().equals(
						Environment.MEDIA_MOUNTED)) {
					// 获得存储卡的路径
					String sdpath = Environment.getExternalStorageDirectory()
							+ "/";
					Log.i("suanfa", sdpath);
					SavePath = sdpath + "download";
					URL url = new URL(fileurl);
					// 创建连接
					HttpURLConnection conn = (HttpURLConnection) url
							.openConnection();
					conn.connect();
					// 获取文件大小
					int length = conn.getContentLength();
					// 创建输入流
					InputStream is = conn.getInputStream();

					File file = new File(SavePath);
					// 判断文件目录是否存在
					if (!file.exists()) {
						file.mkdir();
					}
					File apkFile = new File(SavePath, filename);
					FileOutputStream fos = new FileOutputStream(apkFile);
					int count = 0;
					// 缓存
					byte buf[] = new byte[1024];
					// 写入到文件中
					do {
						int numread = is.read(buf);
						count += numread;
						// 计算进度条位置
						progress = (int) (((float) count / length) * 100);
						// 更新进度
						handler.sendEmptyMessage(9);
						if (numread <= 0) {
							// 下载完成
							handler.sendEmptyMessage(10);
							break;
						}
						// 写入文件
						fos.write(buf, 0, numread);
					} while (!cancelUpdate);// 点击取消就停止下载.
					fos.close();
					is.close();
				}
			} catch (MalformedURLException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
			// 取消下载对话框显示
			DownloadDialog.dismiss();
		}
	};

	// 退出登陆
	class UnAuthentication implements Runnable {
		public void run() {
			try {
				// UnAuthenticationRequest.Builder builder =
				// UnAuthenticationRequest
				// .newBuilder();
				// builder.setUserId(preferences.getInt("user_id", 0));
				// builder.setToken(preferences.getString("Token", ""));
				// UnAuthenticationRequest request = builder.build();
				// byte[] by = HttpUtil.sendHttps(request.toByteArray(),
				// Urlinterface.LOGIN, "POST");
				// if (by != null && by.length > 0) {
				// UnAuthenticationResponse response = UnAuthenticationResponse
				// .parseFrom(by);
				// Log.i("Max", response.getIsSucceed() + "");
				// handler.sendEmptyMessage(5);
				// }
				ClientInfo.Builder cinfo = ClientInfo.newBuilder();
				cinfo.setDeviceId(preferences.getString("clientid", ""));
				cinfo.setOsType(OSType.Android);
				cinfo.setOSVersion(android.os.Build.VERSION.RELEASE);
				cinfo.setUserId(preferences.getInt("user_id", 0));
				cinfo.setChannel(10000);
				cinfo.setClientVersion(Urlinterface.current_version + "");
				cinfo.setIsPushEnable(false);
				ClientInfoRequest.Builder builder = ClientInfoRequest
						.newBuilder();
				builder.setUserId(preferences.getInt("user_id", 0));
				builder.setToken(preferences.getString("Token", ""));
				builder.setClientInfo(cinfo);
				ClientInfoRequest request = builder.build();
				byte[] by = HttpUtil.sendHttps(request.toByteArray(),
						Urlinterface.Client, "PUT");
				if (by != null && by.length > 0) {
					ClientInfoResponse response = ClientInfoResponse
							.parseFrom(by);
					Log.i("MyReceiver", response.getIsSucceed() + "/"
							+ response.getErrorCode());
					handler.sendEmptyMessage(5);
				}
			} catch (InvalidProtocolBufferException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * 下载apk文件
	 */
	public void downloadApk_table() {
		// 启动新线程下载软件
		new downloadApkThread_table().start();
	}

	/**
	 * 安装APK文件
	 */
	private void installApk() {
		File apkfile = new File(SavePath, filename);
		if (!apkfile.exists()) {
			return;
		}
		// 通过Intent安装APK文件
		Intent i = new Intent(Intent.ACTION_VIEW);
		i.setDataAndType(Uri.parse("file://" + apkfile.toString()),
				"application/vnd.android.package-archive");
		startActivity(i);
	}

	public void onPause() {
		super.onPause();

		/**
		 * 页面结束（每个Activity中都需要添加，如果有继承的父Activity中已经添加了该调用，那么子Activity中务必不能添加）
		 * 不能与StatService.onPageStart一级onPageEnd函数交叉使用
		 */
		StatService.onPause(this);
	}

	/**
	 * 获得本地存储的 个人信息
	 */
	private ProfilePojo getProfilePojo() {

		SharedPreferences preferences = getActivity().getSharedPreferences(
				Urlinterface.SHARED, Context.MODE_PRIVATE);

		int profile_userid = preferences.getInt("profile_userid", -1);
		String name = preferences.getString("profile_name", "");// 名称
		String nickName = preferences.getString("profile_nickName", "");// 昵称
		int gender = preferences.getInt("profile_gender", -1);// 性别
		String tileUrl = preferences.getString("profile_tileUrl", "");// 头像
		Boolean isProvider = preferences
				.getBoolean("profile_isProvider", false);//
		String lisence = preferences.getString("profile_lisence", "");// 行业认证
		String mobile = preferences.getString("profile_mobile", "");// 手机号码
		String email = preferences.getString("profile_email", "");// 邮箱
		String birthday = preferences.getString("profile_birthday", "");// 生日
		Boolean isAuthentication = preferences.getBoolean(
				"profile_isAuthentication", false);//
		String fuzhi = preferences.getString("profile_fuZhi", "");// 生日
		profilePojo = new ProfilePojo(profile_userid, name, nickName, gender,
				tileUrl, isProvider, lisence, mobile, email, birthday,
				isAuthentication, fuzhi);
		return profilePojo;
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
		editor.putBoolean("profile_isAuthentication", pro.getIsAuthentication());
		editor.putString("profile_fuZhi", pro.getFuZhi());
		editor.commit();
		dataNumber = 1;

	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {

		switch (resultCode) {
		case -11:
			profilePojo = getProfilePojo();
			handler.sendEmptyMessage(0);
			break;
		default:
			break;

		}
		super.onActivityResult(requestCode, resultCode, data);

	}
}
