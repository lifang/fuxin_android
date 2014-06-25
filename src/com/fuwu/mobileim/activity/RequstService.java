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
import com.fuwu.mobileim.pojo.MessagePojo;
import com.fuwu.mobileim.pojo.ShortContactPojo;
import com.fuwu.mobileim.pojo.TalkPojo;
import com.fuwu.mobileim.util.DBManager;
import com.fuwu.mobileim.util.FuXunTools;
import com.fuwu.mobileim.util.HttpUtil;
import com.fuwu.mobileim.util.ImageUtil;
import com.fuwu.mobileim.util.TimeUtil;
import com.fuwu.mobileim.util.Urlinterface;
import com.google.protobuf.InvalidProtocolBufferException;

public class RequstService extends Service {

	private int time = 25;
	private SharedPreferences sp;
	private IBinder binder = new RequstService.RequstBinder();
	private ScheduledExecutorService scheduledThreadPool = Executors
			.newScheduledThreadPool(2);
	ExecutorService fixedThreadPool = Executors.newFixedThreadPool(2);
	private DBManager db;
	private Context context;
	private int user_id;
	private String token;

	public IBinder onBind(Intent intent) {
		return binder;
	}

	public void onCreate() {
		super.onCreate();
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		context = this;
		// 防止intent为null时异常
		if (intent != null) {
			if (!scheduledThreadPool.isShutdown()) {
				sp = getSharedPreferences(Urlinterface.SHARED,
						Context.MODE_PRIVATE);
				user_id = sp.getInt("user_id", 0);
				token = sp.getString("Token", "");
				db = new DBManager(this);
				scheduledThreadPool.scheduleAtFixedRate(new RequstThread(), 0,
						time, TimeUnit.SECONDS);
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
			Log.i("FuWu", imgUrl);
			Bitmap bitmap = ImageUtil.getBitmapFromUrl(imgUrl, 10 * 1000);
			String fileName = System.currentTimeMillis() + "";
			if (bitmap != null && bitmap.getWidth() > 0
					&& bitmap.getHeight() > 0) {
				ImageUtil.saveBitmap(fileName, "JPG", bitmap);
				MessagePojo mp = new MessagePojo(user_id, contact_id, time,
						fileName + ".jpg", 0, 2);
				Log.i("FuWu", "imgMP:" + mp.toString());
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
				if (!FuXunTools.isApplicationBroughtToBackground(context)) {
					MessageRequest.Builder builder = MessageRequest
							.newBuilder();
					Log.i("Ax", "timeStamp:" + getTimeStamp());
					builder.setUserId(user_id);
					builder.setToken(token);
					builder.setTimeStamp(getTimeStamp());
					Log.i("FuWu", "user_id:" + user_id + "--token:" + token);
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
								if (TimeUtil.isFiveMin(
										db.getLastTime(user_id, contact_id),
										m.getSendTime())) {
									time = m.getSendTime();
								}
								if (m.getContentType() == ContentType.Text) {
									mp = new MessagePojo(user_id, contact_id,
											time, m.getContent(), 0, 1);
									Log.i("FuWu", "serviceMP:" + mp.toString());
									list.add(mp);
								} else {
									fixedThreadPool
											.execute(new DownloadImageThread(
													user_id, contact_id, time,
													m.getContent(), token));
								}
								// 对话列表
								if (j == 0) {
									String str = m.getContent();
									if (m.getContentType() == ContentType.Image) {
										str = "[图片]";
									}
									ShortContactPojo cp = db.queryContact(
											user_id, contact_id);
									String name = cp.getName();
									if (cp.getCustomName() != null
											&& !cp.getCustomName().equals("")) {
										name = cp.getCustomName();
									}
									TalkPojo tp = new TalkPojo(user_id,
											contact_id, name,
											cp.getUserface_url(), str,
											m.getSendTime(), mesCount);
									db.addTalk(tp);
									db.updateContactlastContactTime(user_id,
											contact_id,
											TimeUtil.getCurrentTime());
								}
							}
							db.addMessageList(list);
						}
						Intent intnet = new Intent(
								"com.comdosoft.fuxun.REQUEST_ACTION");
						sendBroadcast(intnet);
					}
				}
			} catch (InvalidProtocolBufferException e) {
				Log.i("FuWu", "RequstServiceError:" + e.toString());
				if (scheduledThreadPool.isShutdown()) {
					scheduledThreadPool.scheduleAtFixedRate(new RequstThread(),
							0, time, TimeUnit.SECONDS);
				}
			} finally {
				if (scheduledThreadPool.isShutdown()) {
					scheduledThreadPool.scheduleAtFixedRate(new RequstThread(),
							0, time, TimeUnit.SECONDS);
				}
			}
		}
	}
}