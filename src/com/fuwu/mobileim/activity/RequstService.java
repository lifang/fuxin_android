package com.fuwu.mobileim.activity;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;
import com.fuwu.mobileim.model.Models.Message;
import com.fuwu.mobileim.model.Models.MessageList;
import com.fuwu.mobileim.model.Models.MessageRequest;
import com.fuwu.mobileim.model.Models.MessageResponse;
import com.fuwu.mobileim.pojo.MessagePojo;
import com.fuwu.mobileim.pojo.TalkPojo;
import com.fuwu.mobileim.util.DBManager;
import com.fuwu.mobileim.util.FxApplication;
import com.fuwu.mobileim.util.HttpUtil;
import com.fuwu.mobileim.util.TimeUtil;
import com.fuwu.mobileim.util.Urlinterface;
import com.google.protobuf.InvalidProtocolBufferException;

public class RequstService extends Service {

	// private static final String TAG = "Ax";
	private SharedPreferences sp;
	private IBinder binder = new RequstService.RequstBinder();
	private ScheduledExecutorService scheduledThreadPool = Executors
			.newScheduledThreadPool(2);
	private DBManager db;
	private FxApplication fx;

	@Override
	public IBinder onBind(Intent intent) {
		return binder;
	}

	@Override
	public void onCreate() {
		super.onCreate();
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		// 防止intent为null时异常
		fx = (FxApplication) getApplication();
		if (intent != null) {
			sp = getSharedPreferences("FuXin", Context.MODE_PRIVATE);
			db = new DBManager(this);
			scheduledThreadPool.scheduleAtFixedRate(new RequstThread(), 0, 10,
					TimeUnit.SECONDS);
		}
		return START_STICKY;
	}

	// 定义内容类继承Binder
	public class RequstBinder extends Binder {
		// 返回本地服务
		RequstService getService() {
			return RequstService.this;
		}
	}

	public void setTimeStamp(String time) {
		if (time != null && !time.equals("")) {
			Editor editor = sp.edit();
			editor.putString("sendTime", time);
			editor.commit();
		}
	}

	public String getTimeStamp() {
		String time = sp.getString("sendTime", "");
		if (time != null && !time.equals("")) {
			return time;
		}
		return "";
	}

	class RequstThread extends Thread {
		@Override
		public void run() {
			super.run();
			try {
				MessageRequest.Builder builder = MessageRequest.newBuilder();
				// builder.setUserId(1);
				// builder.setToken("MockToken");
				Log.i("Ax", "timeStamp:" + getTimeStamp());
				builder.setUserId(fx.getUser_id());
				builder.setToken(fx.getToken());
				builder.setTimeStamp(getTimeStamp());
				Log.i("Ax",
						"user_id:" + fx.getUser_id() + "--token:"
								+ fx.getToken());
				MessageRequest response = builder.build();
				byte[] b = HttpUtil.sendHttps(response.toByteArray(),
						Urlinterface.Message, "POST");
				if (b != null && b.length > 0) {
					MessageResponse mr = MessageResponse.parseFrom(b);
					setTimeStamp(mr.getTimeStamp());
					int contactCount = mr.getMessageListsCount();
					for (int i = 0; i < contactCount; i++) {
						MessageList mes = mr.getMessageLists(i);
						int mesCount = mes.getMessagesCount();
						List<MessagePojo> list = new ArrayList<MessagePojo>();
						Log.i("Ax", "messageCount:" + mesCount);
						for (int j = 0; j < mesCount; j++) {
							Message m = mes.getMessages(j);
							MessagePojo mp;
							int user_id = m.getUserId();
							int contact_id = m.getContactId();
							String time = "";
							Log.i("FuWu", "time:" + m.getSendTime());
							if (TimeUtil.isFiveMin(
									db.getLastTime(user_id, contact_id),
									m.getSendTime())) {
								time = m.getSendTime();
							}
							mp = new MessagePojo(user_id, contact_id, time,
									m.getContent(), 0, 1);
							list.add(mp);
							if (j == mesCount - 1) {
								TalkPojo tp = new TalkPojo(user_id, contact_id,
										"", "", m.getContent(),
										m.getSendTime(), mesCount);
								db.addTalk(tp);
							}
						}
						db.addMessageList(list);
					}
					Intent intnet = new Intent(
							"com.comdosoft.fuxun.REQUEST_ACTION");
					sendBroadcast(intnet);
				}
			} catch (InvalidProtocolBufferException e) {
				Log.i("Ax", e.toString());
			}
		}
	}
}