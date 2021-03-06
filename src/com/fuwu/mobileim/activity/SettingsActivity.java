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
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.app.NotificationManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.fuwu.mobileim.R;
import com.fuwu.mobileim.model.Models.ClientInfo;
import com.fuwu.mobileim.model.Models.ClientInfoRequest;
import com.fuwu.mobileim.model.Models.ClientInfoResponse;
import com.fuwu.mobileim.pojo.ProfilePojo;
import com.fuwu.mobileim.util.DBManager;
import com.fuwu.mobileim.util.ExitService;
import com.fuwu.mobileim.util.FuXunTools;
import com.fuwu.mobileim.util.FxApplication;
import com.fuwu.mobileim.util.HttpUtil;
import com.fuwu.mobileim.util.Urlinterface;
import com.fuwu.mobileim.view.CircularImage;
import com.fuwu.mobileim.view.MyDialog;

/**
 * @作者 丁作强
 * @时间 2014-6-6 下午4:48:35
 */
public class SettingsActivity extends Fragment implements Urlinterface,
		OnTouchListener {
	private FxApplication fxApplication;
	private ListView listview;
	SettingBottomAdapter adapter;
	private CircularImage setting_userface;
	private View rootView;
	private ProfilePojo profilePojo = new ProfilePojo();
	private TextView name;
	private Button setting_exitBtn;
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
	int version = 0;
	private TextView quit_cancel;
	private TextView quit_ok;
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
				// putProfile(profilePojo);
				// setData();
				break;
			case 6:
				builder.dismiss();
				Toast.makeText(getActivity(), "请求失败", Toast.LENGTH_SHORT)
						.show();
				break;
			case 8:
				builder.dismiss();
				showVersionDialog();
				break;
			case 9:
				// 设置进度条位置
				mProgress.setProgress(progress);
				break;
			case 10:
				// 安装文件
				installApk();
			case 11:
				builder.dismiss();
				Toast.makeText(getActivity(), "当前已是最新版本", Toast.LENGTH_SHORT)
						.show();
				break;
			case 12:
				builder.dismiss();
				new Handler().postDelayed(new Runnable() {
					public void run() {
						Intent intent = new Intent(getActivity(),
								LoginActivity.class);
						startActivity(intent);
						getActivity().finish();
					}
				}, 3500);
				Toast.makeText(getActivity(), "您的账号已在其他手机登陆", Toast.LENGTH_LONG)
						.show();
				break;
			}
		}
	};
	SharedPreferences preferences;
	private MyDialog builder;

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
		int user_id = preferences.getInt("user_id", -1);
		setting_exitBtn = (Button) rootView.findViewById(R.id.setting_exitBtn);// 退出账户
		setting_exitBtn.setOnClickListener(listener0);// 给退出账户 设置监听

		if (preferences.getString("profile_user", "").equals(user_id + "")) {
			init();
			profilePojo = FuXunTools.getProfilePojo(preferences, fxApplication);
			setData();
		}
		String release = android.os.Build.VERSION.RELEASE; // android系统版本号
		version = Integer.parseInt(release.substring(0, 1));
		return rootView;
	}

	/**
	 * 新版本提示
	 * 
	 * */
	private void showVersionDialog() {
		View view = getActivity().getLayoutInflater().inflate(
				R.layout.quit_builder, null);
		TextView tv = (TextView) view.findViewById(R.id.quit_message);
		tv.setText("检测到新版本,您需要更新吗？");
		quit_cancel = (TextView) view.findViewById(R.id.quit_cancel);
		quit_ok = (TextView) view.findViewById(R.id.quit_ok);
		quit_cancel.setText("下次再说");
		quit_ok.setText("确认升级");
		quit_ok.setOnTouchListener(this);
		quit_cancel.setOnTouchListener(this);
		if (version < 4) {
			quit_cancel
					.setBackgroundResource(R.drawable.quit_button_cancel_shape2);
			quit_ok.setBackgroundResource(R.drawable.quit_button_ok_shape2);
		}
		final MyDialog builder = new MyDialog(getActivity(), 0, view,
				R.style.mydialog);
		quit_cancel.setOnClickListener(new View.OnClickListener() {
			public void onClick(View arg0) {
				builder.dismiss();
			}
		});
		quit_ok.setOnClickListener(new View.OnClickListener() {
			public void onClick(View arg0) {
				builder.dismiss();
				cancelUpdate = false;
				showDownloadDialog_table();
			}
		});
		builder.show();
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
		// setting_userface.setOnClickListener(listener2);
		name = (TextView) rootView.findViewById(R.id.setting_name);// 显示级别为：备注名>真实姓名>昵称
		setting_top.setOnClickListener(listener1);// 给个人信息部分设置监听
	}

	/**
	 * 
	 * 设置对应的数据信息
	 */
	private void setData() {

		// 设置头像
		String face_str = profilePojo.getTileUrl();
		Log.i("Ax", "profilePojo.getTileUrl()" + profilePojo.getTileUrl());
		if (face_str != null && face_str.length() > 4) {
			File f = new File(Urlinterface.head_pic, profilePojo.getUserId()
					+ "");
			if (f.exists()) {
				setting_userface.setImageDrawable(new BitmapDrawable(
						BitmapFactory.decodeFile(Urlinterface.head_pic
								+ profilePojo.getUserId())));
			} else {
				FuXunTools.set_bk(profilePojo.getUserId(), face_str,
						setting_userface);
			}

		} else {
			setting_userface.setImageResource(R.drawable.moren);
		}
		// 设置名称 昵称 显示级别为：备注名>真实姓名>昵称

		String namestr = profilePojo.getName();
		if (namestr != null && namestr.length() > 0) {
			name.setText(namestr);
		} else {
			name.setText(profilePojo.getNickName());
		}

	}

	private View.OnClickListener listener0 = new View.OnClickListener() {
		@Override
		public void onClick(View v) {

			FuXunTools.initdate(preferences, fxApplication);
			Intent intent = new Intent();
			intent.setClass(getActivity(), ExitService.class);
			getActivity().startService(intent);

			intent.setClass(getActivity(), LoginActivity.class);
			startActivity(intent);
			clearActivity();
		}
	};
	private View.OnClickListener listener1 = new View.OnClickListener() {
		@Override
		public void onClick(View v) {
			Intent intent = new Intent();
			intent.setClass(getActivity(), MyInformationActivity.class);
			startActivityForResult(intent, 0);
		}
	};

	private View.OnClickListener listener2 = new View.OnClickListener() {

		@Override
		public void onClick(View v) {
			Intent intent = new Intent();
			intent.putExtra("image_path",
					Urlinterface.head_pic + preferences.getInt("user_id", -1));
			intent.setClass(getActivity(), ComtactZoomImageActivity.class);
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
			if (FuXunTools.isConnect(getActivity())) {
//				showLoading("正在检测新版本，请稍后..");
				builder= FuXunTools.showLoading(getActivity().getLayoutInflater(),getActivity(),"正在检测新版本，请稍后..");
				new VersionChecking().start();
			} else {
				Toast.makeText(getActivity(), R.string.no_internet,
						Toast.LENGTH_SHORT).show();
			}
			break;
		case 1:// 清除全部聊天记录
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
			intent.setClass(getActivity(), BlockManagementActivity.class);
			startActivity(intent);
			break;
		 case 5:// 关于我们
		  intent.setClass(getActivity(), AboutUsActivity.class);
		  startActivity(intent);
		 break;

		}
	}

	public class SettingBottomAdapter extends BaseAdapter {

		private int[] icon = new int[] { R.drawable.setting_image1,
				R.drawable.setting_image2, R.drawable.setting_image3,
				R.drawable.setting_image4, R.drawable.setting_image5,
				R.drawable.setting_image6 }; // icon
		// 集合
		private String[] titleArr = new String[] { "新版本检测", "清除全部聊天记录", "消息推送",
				"修改密码", "屏蔽管理", "关于我们" }; //

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

			viewHolder.re.setVisibility(View.GONE);
			viewHolder.view.setVisibility(View.GONE);
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
			View view = getActivity().getLayoutInflater().inflate(
					R.layout.quit_builder, null);
			TextView tv = (TextView) view.findViewById(R.id.quit_message);
			tv.setText("您确认要删除全部聊天记录吗？");
			quit_cancel = (TextView) view.findViewById(R.id.quit_cancel);
			quit_ok = (TextView) view.findViewById(R.id.quit_ok);
			quit_cancel.setText("取消");
			quit_ok.setText("确认");
			quit_ok.setOnTouchListener(this);
			quit_cancel.setOnTouchListener(this);
			if (version < 4) {
				quit_cancel
						.setBackgroundResource(R.drawable.quit_button_cancel_shape2);
				quit_ok.setBackgroundResource(R.drawable.quit_button_ok_shape2);
			}
			final MyDialog builder = new MyDialog(getActivity(), 0, view,
					R.style.mydialog);
			quit_cancel.setOnClickListener(new View.OnClickListener() {
				public void onClick(View arg0) {
					builder.dismiss();
				}
			});
			quit_ok.setOnClickListener(new View.OnClickListener() {
				public void onClick(View arg0) {
					builder.dismiss();
					int user_id = preferences.getInt("user_id", -1);
					db.delMessage(user_id);
					NotificationManager nm = (NotificationManager) getActivity().getSystemService(android.content.Context.NOTIFICATION_SERVICE);
					nm.cancel(Urlinterface.Receiver_code);
					Intent intnet = new Intent(
							"com.comdosoft.fuxun.REQUEST_ACTION");
					getActivity().sendBroadcast(intnet);
				}
			});
			builder.show();

	}

	/**
	 * 版本检测
	 */
	class VersionChecking extends Thread {
		public void run() {
			try {
				String release = android.os.Build.VERSION.RELEASE; // android系统版本号
				ClientInfo.Builder pb = ClientInfo.newBuilder();
				String deviceId = preferences.getString("clientid", "");
				int user_id = preferences.getInt("user_id", -1);
				String Token = preferences.getString("Token", "");
				pb.setDeviceId(deviceId);
				pb.setOSVersion(release);
				pb.setUserId(user_id);
				pb.setChannel(Urlinterface.current_channel);
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
				handler.sendEmptyMessage(6);
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

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {

		switch (resultCode) {
		case -11:
			profilePojo = FuXunTools.getProfilePojo(preferences, fxApplication);
			;
			// handler.sendEmptyMessage(0);
			setData();
			break;
		default:
			break;

		}
		super.onActivityResult(requestCode, resultCode, data);

	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		if (db != null) {
			db.closeDB();
		}
	}

	@Override
	public boolean onTouch(View v, MotionEvent event) {
		if (event.getAction() == MotionEvent.ACTION_DOWN) {
			switch (v.getId()) {
			case R.id.quit_cancel:
				quit_cancel.setTextColor(this.getResources().getColor(
						R.color.system_textColor2));
				break;
			case R.id.quit_ok:
				quit_ok.setTextColor(this.getResources().getColor(
						R.color.system_textColor2));
				break;
			}
		} else if (event.getAction() == MotionEvent.ACTION_UP) {
			switch (v.getId()) {
			case R.id.quit_cancel:
				quit_cancel.setTextColor(this.getResources().getColor(
						R.color.system_textColor));
				break;
			case R.id.quit_ok:
				quit_ok.setTextColor(this.getResources().getColor(
						R.color.system_textColor));
				break;
			}
		}
		return false;
	}

	
}
