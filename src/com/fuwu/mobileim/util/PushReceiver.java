package com.fuwu.mobileim.util;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.fuwu.mobileim.R;
import com.fuwu.mobileim.activity.FragmengtActivity;
import com.fuwu.mobileim.activity.LoginActivity;
import com.fuwu.mobileim.activity.SystemPushActivity;
import com.igexin.sdk.PushConsts;

public class PushReceiver extends BroadcastReceiver {
	public FxApplication fx;
	public Intent intent = new Intent();

	public void onReceive(Context context, Intent intent) {
		fx = (FxApplication) context.getApplicationContext();
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
				if (FuXunTools.isApplicationBroughtToBackground(context)) {
					Log.i("MyReceiver", "后台运行");
					if (fx.getToken().equals("NULL")) {
						intent.setClass(context, LoginActivity.class); // 点击该通知后要跳转的Activity
					} else {
						intent.setClass(context, FragmengtActivity.class); // 点击该通知后要跳转的Activity
					}
					MyNotification("福务网", data, context, intent);
				} else {
					Log.i("MyReceiver", "前台运行");

				}
			}
			break;
		case PushConsts.GET_CLIENTID:
			// 获取ClientID(CID)
			String cid = bundle.getString("clientid");
			if (!cid.equals("")) {
				Log.i("MyReceiver", "-----" + cid);
			}
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
			Intent startIntent) {
		// 1.得到NotificationManager
		NotificationManager nm = (NotificationManager) context
				.getSystemService(android.content.Context.NOTIFICATION_SERVICE);
		// 2.实例化一个通知，指定图标、概要、时间
		Notification notification = new Notification(R.drawable.icon, "福务网",
				System.currentTimeMillis());

		// notification.defaults = Notification.DEFAULT_LIGHTS;
		notification.defaults |= Notification.DEFAULT_SOUND;// 声音
		notification.defaults |= Notification.DEFAULT_VIBRATE;
		notification.defaults |= Notification.DEFAULT_LIGHTS;
		notification.defaults |= Notification.DEFAULT_VIBRATE;// 震动
		notification.flags |= Notification.FLAG_AUTO_CANCEL;// 点击后删除通知
		// CharSequence contentTitle = title; // 通知栏标题
		// CharSequence contentText = content; // 通知栏内容
		PendingIntent contentItent = PendingIntent.getActivity(context, 0,
				startIntent, 0);
		notification.setLatestEventInfo(context, title, content, contentItent);
		nm.notify(0, notification);
	}
}