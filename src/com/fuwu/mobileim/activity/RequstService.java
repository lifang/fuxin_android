package com.fuwu.mobileim.activity;

import java.util.ArrayList;
import java.util.List;
import android.app.Service;
import android.content.Intent;
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
import com.fuwu.mobileim.util.HttpUtil;
import com.google.protobuf.InvalidProtocolBufferException;

public class RequstService extends Service {

	private static final String TAG = "Ax";
	private IBinder binder = new RequstService.RequstBinder();
	private DBManager db;

	@Override
	public IBinder onBind(Intent intent) {
		return binder;
	}

	@Override
	public void onCreate() {
		Log.i(TAG, "onCreate");
		super.onCreate();
	}

	@SuppressWarnings("deprecation")
	@Override
	public void onStart(Intent intent, int startId) {
		Log.i(TAG, "onStart");
		super.onStart(intent, startId);
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		Log.i(TAG, "onStartCommand");
		// 防止intent为null时异常
		if (intent != null) {
			db = new DBManager(this);
			new RequstThread().start();
		}
		return START_STICKY;
	}

	@Override
	public void onDestroy() {
		Log.i(TAG, "onDestroy");
		super.onDestroy();
	}

	// 定义内容类继承Binder
	public class RequstBinder extends Binder {
		// 返回本地服务
		RequstService getService() {
			return RequstService.this;
		}
	}

	class RequstThread extends Thread {
		@Override
		public void run() {
			super.run();
			try {
				while (true) {
					MessageRequest.Builder builder = MessageRequest
							.newBuilder();
					builder.setUserId(1);
					builder.setToken("MockToken");
					MessageRequest response = builder.build();
					String https = "https://118.242.18.189/api/Message";
					MessageResponse mr = MessageResponse.parseFrom(HttpUtil
							.sendHttps(response.toByteArray(), https, "POST"));
					int contactCount = mr.getMessageListsCount();
					Log.i("Ax", "messageCount:" + contactCount);
					for (int i = 0; i < contactCount; i++) {
						MessageList mes = mr.getMessageLists(i);
						int mesCount = mes.getMessagesCount();
						List<MessagePojo> list = new ArrayList<MessagePojo>();
						for (int j = 0; j < mesCount; j++) {
							Message m = mes.getMessages(j);
							MessagePojo mp = new MessagePojo(i, j,
									m.getSendTime(), m.getContent(), 0, 1);
							// Log.i("Ax", "Message:" + mp.toString());
							list.add(mp);
							if (j == mesCount - 1) {
								TalkPojo tp = new TalkPojo(i, j, "", "",
										m.getContent(), m.getSendTime(),
										mesCount);
								db.addTalk(tp);
							}
						}
						db.addMessageList(list);
					}
					Intent intnet = new Intent(
							"com.comdosoft.fuxun.REQUEST_ACTION");
					sendBroadcast(intnet);
					Thread.sleep(60 * 1000);
				}
			} catch (InterruptedException e) {
				e.printStackTrace();
			} catch (InvalidProtocolBufferException e) {
				e.printStackTrace();
			}
		}
	}
}