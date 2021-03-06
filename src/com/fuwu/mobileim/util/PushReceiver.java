package com.fuwu.mobileim.util;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;

import com.fuwu.mobileim.R;
import com.fuwu.mobileim.activity.FragmengtActivity;
import com.fuwu.mobileim.activity.LoginActivity;
import com.fuwu.mobileim.activity.RequstService;
import com.fuwu.mobileim.model.Models.ClientInfo;
import com.fuwu.mobileim.model.Models.ClientInfo.OSType;
import com.fuwu.mobileim.model.Models.ClientInfoRequest;
import com.fuwu.mobileim.model.Models.ClientInfoResponse;
import com.fuwu.mobileim.model.Models.MessagePush;
import com.fuwu.mobileim.model.Models.PushRequest;
import com.igexin.sdk.PushConsts;

public class PushReceiver extends BroadcastReceiver {
	public FxApplication fx;
	public Intent intent = new Intent();
	public SharedPreferences sf;
	public String clientid;
	public Context mContext;
	private NotificationManager nm;
	private int TID = 8888;

	public void onReceive(Context context, Intent intent) {
		mContext = context;
		fx = (FxApplication) context.getApplicationContext();
		sf = context.getSharedPreferences(Urlinterface.SHARED, 0);
		nm = (NotificationManager) context
				.getSystemService(android.content.Context.NOTIFICATION_SERVICE);
		Bundle bundle = intent.getExtras();
		Log.d("GetuiSdkDemo", "onReceive() action=" + bundle.getInt("action"));
		switch (bundle.getInt(PushConsts.CMD_ACTION)) {
		case PushConsts.GET_MSG_DATA:
			// 获取透传（payload）数据
			byte[] payload = bundle.getByteArray("payload");

			if (payload != null) {
				String data = new String(payload);
				Log.i("MyReceiver", data);
				// true表示后台运行 false表示前台
				if (sf.getBoolean("pushsetting_sound", true)) {
					if (FuXunTools.isApplicationBroughtToBackground(context)) {
						Intent i = new Intent();
						i.putExtra("type", 1);
						i.setClass(context, RequstService.class);
						context.startService(i);
						if (sf.getString("Token", "null").equals("null")) {
							intent.setClass(context, LoginActivity.class); // 点击该通知后要跳转的Activity
						} else {
							intent.setClass(context, FragmengtActivity.class); // 点击该通知后要跳转的Activity
						}
						byte[] byteArray = Base64.decode(data, Base64.DEFAULT);
						String con = new String(byteArray);
						try {
//							PushRequest pr = PushRequest.parseFrom(byteArray);
							Log.i("MyReceiver", "收到推送"+con);
//							MessagePush mp = pr.getMessagePush();
							// MyNotification("福务网",
							// mp.getSenderName() + ":" + mp.getContent(),
							// context, intent);
							int n = getMessageNumber()+Integer.parseInt(con.substring(3, 4));
							MyNotification("手机福务网", "您收到" + n + "条消息", context,
									intent);
							setMessageNumber(n);
							// MyNotification("福务网", con, context, intent);

						} catch (Exception e) {
							e.printStackTrace();
						}
					} else {
						Log.i("MyReceiver", "前台运行");
					}
				}
			}
			break;
		case PushConsts.GET_CLIENTID:
			// 获取ClientID(CID)
			clientid = bundle.getString("clientid");
			Log.i("MyReceiver", "clientid=>" + clientid);
			if (sf.getString("clientid", "").equals("")) {
				sf.edit().putString("clientid", clientid).commit();
				new Thread(new ClientID_Post()).start();
			}
			/*
			 * 第三方应用需要将ClientID上传到第三方服务器，并且将当前用户帐号和ClientID进行关联，
			 * 以便以后通过用户帐号查找ClientID进行消息推送
			 * 有些情况下ClientID可能会发生变化，为保证获取最新的ClientID，请应用程序在每次获取ClientID广播后
			 * ，都能进行一次关联绑定
			 */
			break;
		case Urlinterface.Receiver_code:
			Log.i("Max", "删除通知uid:" + sf.getInt("contact_id", 0));
			clearNotification();
			break;
		default:
			break;
		}
	}

	// 自定义通知
	public void MyNotification(String title, String content, Context context,
			Intent startIntent) {
		// 1.得到NotificationManager
		// 2.实例化一个通知，指定图标、概要、时间
		Notification notification = new Notification(R.drawable.moren, "手机福务网",
				System.currentTimeMillis());
		// notification.defaults = Notification.DEFAULT_LIGHTS;
		if (sf.getBoolean("pushsetting_music", true)) {
			notification.defaults |= Notification.DEFAULT_SOUND;// 声音

		}
		if (sf.getBoolean("pushsetting_shake", true)) {
			notification.defaults |= Notification.DEFAULT_VIBRATE;// 震动
		}
		// notification.number += 1;
		// notification.defaults |= Notification.DEFAULT_LIGHTS;
		notification.flags |= Notification.FLAG_AUTO_CANCEL;// 点击后删除通知
		// CharSequence contentTitle = title; // 通知栏标题
		// CharSequence contentText = content; // 通知栏内容
		PendingIntent contentItent = PendingIntent.getActivity(context, 0,
				startIntent, 0);
		notification.setLatestEventInfo(context, title, content, contentItent);// 功能：
																				// 显示在拉伸状态栏中的Notification属性，点击后将发送PendingIntent对象
		nm.notify(TID, notification);// 执行一个notification的消息；
	}

	// 删除通知取
	private void clearNotification() {
		// 启动后删除之前我们定义的通知
		nm.cancel(TID);// 消一个notificatioin的消息；
	}

	// 发送ClientID
	class ClientID_Post implements Runnable {
		public void run() {
			try {
				Log.i("error", "1");
				ClientInfo.Builder cinfo = ClientInfo.newBuilder();
				cinfo.setDeviceId(clientid);
				cinfo.setOsType(OSType.Android);
				Log.i("error", "2");
				cinfo.setOSVersion(android.os.Build.VERSION.RELEASE);
				cinfo.setUserId(fx.getUser_id());
				cinfo.setChannel(Urlinterface.current_channel);
				Log.i("error", "3");
				cinfo.setClientVersion(Urlinterface.current_version + "");
				cinfo.setIsPushEnable(true);
				ClientInfoRequest.Builder builder = ClientInfoRequest
						.newBuilder();
				Log.i("error", "4");
				builder.setUserId(fx.getUser_id());
				builder.setToken(fx.getToken());
				builder.setClientInfo(cinfo);
				Log.i("error", "5");
				ClientInfoRequest request = builder.build();

				byte[] by = HttpUtil.sendHttps(request.toByteArray(),
						Urlinterface.Client, "PUT");
				Log.i("error", "6");
				if (by != null && by.length > 0) {
					ClientInfoResponse response = ClientInfoResponse
							.parseFrom(by);
					if (response.getIsSucceed()) {
						sf.edit().putString("clientid", clientid).commit();
						Log.i("MyReceiver",
								response.getIsSucceed() + "/"
										+ response.getErrorCode());
						Log.i("MyReceiver",
								 "response.getHasNewVersion() +"
										+ response.getHasNewVersion() );
						if (response.getHasNewVersion()) {
							sf.edit().putString("newClientVersion", response.getNewClientVersion()).commit();
							sf.edit().putString("NewVersionUrl", response.getClientUrl()).commit();
						}
					}
				}
			} catch (Exception e) {
				Log.i("error", e.toString());
				e.printStackTrace();
			}
		}
	}

	public void setMessageNumber(int num) {
		Editor editor = sf.edit();
		editor.putInt("MessageNumber", num);
		editor.commit();
	}

	public int getMessageNumber() {
		int time = sf.getInt("MessageNumber", 0);
		return time;
	}
}