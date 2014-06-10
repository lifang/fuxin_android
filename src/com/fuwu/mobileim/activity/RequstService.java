package com.fuwu.mobileim.activity;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Bitmap;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;
import com.fuwu.mobileim.model.Models.Message;
import com.fuwu.mobileim.model.Models.Message.ContentType;
import com.fuwu.mobileim.model.Models.MessageList;
import com.fuwu.mobileim.model.Models.MessageRequest;
import com.fuwu.mobileim.model.Models.MessageResponse;
import com.fuwu.mobileim.pojo.ContactPojo;
import com.fuwu.mobileim.pojo.MessagePojo;
import com.fuwu.mobileim.pojo.TalkPojo;
import com.fuwu.mobileim.util.DBManager;
import com.fuwu.mobileim.util.FxApplication;
import com.fuwu.mobileim.util.HttpUtil;
import com.fuwu.mobileim.util.ImageUtil;
import com.fuwu.mobileim.util.TimeUtil;
import com.fuwu.mobileim.util.Urlinterface;
import com.google.protobuf.InvalidProtocolBufferException;

public class RequstService extends Service {

	// private static final String TAG = "Ax";
	private SharedPreferences sp;
	private IBinder binder = new RequstService.RequstBinder();
	private ScheduledExecutorService scheduledThreadPool = Executors
			.newScheduledThreadPool(2);
	private ExecutorService singleThreadExecutor = Executors
			.newSingleThreadExecutor();
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
		if (intent != null) {
			Log.i("FuWu", "scheduledThreadPool.isShutdown:"
					+ scheduledThreadPool.isShutdown());
			if (!scheduledThreadPool.isShutdown()) {
				fx = (FxApplication) getApplication();
				sp = getSharedPreferences("FuXin", Context.MODE_PRIVATE);
				db = new DBManager(this);
				scheduledThreadPool.scheduleAtFixedRate(new RequstThread(), 0,
						10, TimeUnit.SECONDS);
			}
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

	class DownloadImageThread extends Thread {
		private int user_id;
		private int contact_id;
		private String time;
		private String url;
		private String token;

		public DownloadImageThread() {
		}

		public DownloadImageThread(int user_id, int contact_id, String time,
				String url, String token) {
			super();
			this.user_id = user_id;
			this.contact_id = contact_id;
			this.time = time;
			this.url = url;
			this.token = token;
		}

		@Override
		public void run() {
			super.run();
			String imgUrl = url + "&userId=" + user_id + "&token=" + token;
			Log.i("FuWu", "url" + imgUrl);
			Bitmap bitmap = ImageUtil.getBitmapFromUrl(imgUrl, 10 * 1000);
			String fileName = System.currentTimeMillis() + "";
			if (bitmap != null && bitmap.getWidth() > 0
					&& bitmap.getHeight() > 0) {
				ImageUtil.saveBitmap(fileName, "JPG", bitmap);
				MessagePojo mp = new MessagePojo(user_id, contact_id, time,
						fileName + ".jpg", 0, 2);
				db.addMessage(mp);
				Intent intnet = new Intent("com.comdosoft.fuxun.REQUEST_ACTION");
				sendBroadcast(intnet);
			}
		}
	}

	class RequstThread extends Thread {
		@Override
		public void run() {
			super.run();
			try {
				Log.i("FuWu", "starService------");
				MessageRequest.Builder builder = MessageRequest.newBuilder();
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
						for (int j = mesCount - 1; j >= 0; j--) {
							Message m = mes.getMessages(j);
							MessagePojo mp = null;
							int user_id = m.getUserId();
							int contact_id = m.getContactId();
							String time = "";
							Log.i("Ax", "sendTime:" + m.getSendTime());
							Log.i("FuWu", "content:" + m.getContent());
							if (TimeUtil.isFiveMin(
									db.getLastTime(user_id, contact_id),
									m.getSendTime())) {
								time = m.getSendTime();
							}
							if (m.getContentType() == ContentType.Text) {
								mp = new MessagePojo(user_id, contact_id, time,
										m.getContent(), 0, 1);
							} else {
								Log.i("FuWu", "imageURL:" + m.getContent());
								singleThreadExecutor
										.execute(new DownloadImageThread(
												user_id, contact_id, time, m
														.getContent(), fx
														.getToken()));
							}
							list.add(mp);
							if (j == 0) {
								String str = m.getContent();
								if (m.getContentType() == ContentType.Image) {
									str = "[图片]";
								}
								ContactPojo cp = db.queryContact(user_id,
										contact_id);
								String name = cp.getName();
								if (cp.getCustomName() != null
										&& !cp.getCustomName().equals("")) {
									name = cp.getCustomName();
								}
								TalkPojo tp = new TalkPojo(user_id, contact_id,
										name, cp.getUserface_url(), str,
										m.getSendTime(), mesCount);
								db.addTalk(tp);
							}
						}
						db.addMessageList(list);
					}
					Log.i("FuWu", "sendBroadcast!!!");
					Intent intnet = new Intent(
							"com.comdosoft.fuxun.REQUEST_ACTION");
					sendBroadcast(intnet);
				}
			} catch (InvalidProtocolBufferException e) {
				Log.i("FuWu", e.toString());
			}
		}
	}
}