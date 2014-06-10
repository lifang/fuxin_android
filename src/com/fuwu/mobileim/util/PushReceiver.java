package com.fuwu.mobileim.util;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;

import com.fuwu.mobileim.R;
import com.fuwu.mobileim.activity.FragmengtActivity;
import com.fuwu.mobileim.activity.LoginActivity;
import com.fuwu.mobileim.model.Models.ClientInfo;
import com.fuwu.mobileim.model.Models.ClientInfo.OSType;
import com.fuwu.mobileim.model.Models.ClientInfoRequest;
import com.fuwu.mobileim.model.Models.ClientInfoResponse;
import com.fuwu.mobileim.model.Models.MessagePush;
import com.google.protobuf.InvalidProtocolBufferException;
import com.igexin.sdk.PushConsts;

public class PushReceiver extends BroadcastReceiver {
	public FxApplication fx;
	public Intent intent = new Intent();
	public SharedPreferences sf;
	public String clientid;

	public void onReceive(Context context, Intent intent) {
		fx = (FxApplication) context.getApplicationContext();
		sf = context.getSharedPreferences(Urlinterface.SHARED, 0);
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
					Log.i("MyReceiver", "clientid=>1");
					if (FuXunTools.isApplicationBroughtToBackground(context)) {

						if (fx.getToken().equals("NULL")) {
							intent.setClass(context, LoginActivity.class); // 点击该通知后要跳转的Activity
						} else {
							intent.setClass(context, FragmengtActivity.class); // 点击该通知后要跳转的Activity
						}
						Log.i("MyReceiver", "clientid=>2");
						byte[] byteArray = Base64.decode(data, Base64.DEFAULT);
						try {
							MessagePush mp = MessagePush.parseFrom(byteArray);
							mp.getSendTime();
							Log.i("MyReceiver", new String(byteArray));
							Log.i("MyReceiver", "clientid=>3");
							MyNotification("福务网",
									mp.getSenderName() + ":" + mp.getContent(),
									context, intent, mp.getSendTime());
							Log.i("MyReceiver", "clientid=>4");
						} catch (InvalidProtocolBufferException e) {
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
			// if (sf.getBoolean("welcome", true)) {
			new Thread(new ClientID_Post()).start();
			// }
			/*
			 * 第三方应用需要将ClientID上传到第三方服务器，并且将当前用户帐号和ClientID进行关联，
			 * 以便以后通过用户帐号查找ClientID进行消息推送
			 * 有些情况下ClientID可能会发生变化，为保证获取最新的ClientID，请应用程序在每次获取ClientID广播后
			 * ，都能进行一次关联绑定
			 */
			break;
		default:
			break;
		}
	}

	// 自定义通知
	@SuppressWarnings("deprecation")
	public void MyNotification(String title, String content, Context context,
			Intent startIntent, String time) {
		// 1.得到NotificationManager
		NotificationManager nm = (NotificationManager) context
				.getSystemService(android.content.Context.NOTIFICATION_SERVICE);
		// 2.实例化一个通知，指定图标、概要、时间
		Notification notification = new Notification(R.drawable.moren, "福务网",
				TimeUtil.getLongTime(time));

		// notification.defaults = Notification.DEFAULT_LIGHTS;
		if (sf.getBoolean("pushsetting_music", true)) {
			notification.defaults |= Notification.DEFAULT_SOUND;// 声音

		}
		if (sf.getBoolean("pushsetting_shake", true)) {
			notification.defaults |= Notification.DEFAULT_VIBRATE;// 震动
		}
		// notification.defaults |= Notification.DEFAULT_LIGHTS;
		notification.flags |= Notification.FLAG_AUTO_CANCEL;// 点击后删除通知
		// CharSequence contentTitle = title; // 通知栏标题
		// CharSequence contentText = content; // 通知栏内容
		PendingIntent contentItent = PendingIntent.getActivity(context, 0,
				startIntent, 0);
		notification.setLatestEventInfo(context, title, content, contentItent);
		nm.notify(0, notification);
	}

	// 发送ClientID
	class ClientID_Post implements Runnable {
		public void run() {
			try {
				ClientInfo.Builder cinfo = ClientInfo.newBuilder();
				cinfo.setDeviceId(clientid);
				cinfo.setOsType(OSType.Android);
				cinfo.setOSVersion(android.os.Build.VERSION.RELEASE);
				cinfo.setUserId(fx.getUser_id());
				cinfo.setChannel(10000);
				cinfo.setClientVersion(Urlinterface.current_version + "");
				cinfo.setIsPushEnable(true);
				ClientInfoRequest.Builder builder = ClientInfoRequest
						.newBuilder();
				builder.setUserId(fx.getUser_id());
				builder.setToken(fx.getToken());
				builder.setClientInfo(cinfo);
				ClientInfoRequest request = builder.build();
				byte[] by = HttpUtil.sendHttps(request.toByteArray(),
						Urlinterface.Client, "PUT");
				if (by != null && by.length > 0) {
					ClientInfoResponse response = ClientInfoResponse
							.parseFrom(by);
					sf.edit().putString("clientid", clientid).commit();
					Log.i("MyReceiver", response.getIsSucceed() + "/"
							+ response.getErrorCode());
				}
			} catch (Exception e) {
				Log.i("error", e.toString());
			}
		}
	}
}