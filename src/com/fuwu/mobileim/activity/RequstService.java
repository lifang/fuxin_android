package com.fuwu.mobileim.activity;

import java.util.ArrayList;
import java.util.Collections;
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
import com.fuwu.mobileim.model.Models.MessageConfirmedRequest;
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
import com.fuwu.mobileim.util.SendDataComparator;
import com.fuwu.mobileim.util.Urlinterface;
import com.google.protobuf.InvalidProtocolBufferException;

public class RequstService extends Service {
	/**
	 * 根据消息时间来排序
	 */
	private SendDataComparator sendDataComparator = new SendDataComparator();
	private boolean flag = false;
	private int time = 25;
	private SharedPreferences sp;
	private IBinder binder = new RequstService.RequstBinder();
	private ScheduledExecutorService scheduledThreadPool = Executors
			.newScheduledThreadPool(2);
	private ExecutorService fixedThreadPool = Executors.newFixedThreadPool(2);
	private ExecutorService pushThreadPool = Executors.newFixedThreadPool(2);
	private DBManager db;
	private Context context;
	private int user_id;
	private int contact_id;
	private String token;
	private int isComMeg = 0;// 0接收/1发送 消息

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
			sp = getSharedPreferences(Urlinterface.SHARED, Context.MODE_PRIVATE);
			user_id = sp.getInt("user_id", 0);
			token = sp.getString("Token", "");
			contact_id = sp.getInt("contact_id", 1);
			db = new DBManager(this);
			Log.i("FuWu", "type---" + intent.getIntExtra("type", 0));
			if (intent.getIntExtra("type", 0) == 1) {
				flag = true;
				pushThreadPool.execute(new RequstThread());
			} else {
				if (!scheduledThreadPool.isShutdown()) {
					scheduledThreadPool.scheduleAtFixedRate(new RequstThread(),
							0, time, TimeUnit.SECONDS);
				}
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
		private String fileName;

		public DownloadImageThread() {
		}

		public DownloadImageThread(int user_id, int contact_id, String time,
				String url, String token, String fileName) {
			super();
			this.user_id = user_id;
			this.contact_id = contact_id;
			this.time = time;
			this.url = url;
			this.token = token;
			this.fileName = fileName;
		}

		@Override
		public void run() {
			super.run();
			String imgUrl = url + "&userId=" + user_id + "&token=" + token;
			Log.i("FuWu", imgUrl);
			Bitmap bitmap = ImageUtil.getBitmapFromUrl(imgUrl, 10 * 1000);
			if (bitmap != null && bitmap.getWidth() > 0
					&& bitmap.getHeight() > 0) {
				ImageUtil.saveBitmap(fileName, "JPG", bitmap);
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
				Log.i("FuWu", "startServer--------");
				if (flag
						|| !FuXunTools
								.isApplicationBroughtToBackground(context)) {
					Log.i("FuWu", "RunServer--------" + flag);
					flag = false;
					MessageRequest.Builder builder = MessageRequest
							.newBuilder();
					Log.i("Ax", "timeStamp:" + getTimeStamp());
					builder.setUserId(user_id);
					builder.setToken(token);
					builder.setTimeStamp(getTimeStamp());
					Log.i("Ax", "user_id:" + user_id + "--token:" + token);
					MessageRequest response = builder.build();
					byte[] b = HttpUtil.sendHttps(response.toByteArray(),
							Urlinterface.Message, "POST");
					if (b != null && b.length > 0) {
						MessageResponse mr = MessageResponse.parseFrom(b);
						if (mr.getIsSucceed()) {
							setTimeStamp(mr.getTimeStamp());
							int contactCount = mr.getMessageListsCount();
							if (contactCount != 0) {

								// 别人发送+自自发
								Log.i("FuWu",
										" mr.getMessageListsCount()--------"
												+ contactCount);
								for (int i = 0; i < contactCount; i++) {
									MessageList mes = mr.getMessageLists(i);
									if (mes.getContactId() != user_id) {
										int mesCount = mes.getMessagesCount();
										List<MessagePojo> list = new ArrayList<MessagePojo>();
										for (int j = mesCount - 1; j >= 0; j--) {
											Message m = mes.getMessages(j);
											addMessagePojo(list, m);
											Log.i("FuWu1",m.getContent()+"--"+m.getSendTime());
										}
										// 排序
										Collections.sort(list,
												sendDataComparator);
										// 更新本地数据库
										updataDb(list);

									}
								}

								Intent intnet = new Intent(
										"com.comdosoft.fuxun.REQUEST_ACTION");
								sendBroadcast(intnet);
							}
						}
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

	/**
	 * 
	 * 将数据进行处理，生成 MessagePojo对象，插入数组 isComMeg 0 别人发的消息（包含自己给对方发的），1 纯自己发的消息
	 * */
	void addMessagePojo(List<MessagePojo> list, Message m) {
		int isComMeg;
		MessagePojo mp = null;
		int userid = m.getUserId();
		int contactid = m.getContactId();
		if (m.getContactId() == user_id) {
			userid = m.getContactId();
			contactid = m.getUserId();
			isComMeg = 1;
		} else {
			isComMeg = 0;
		}
		String time = "";
		Log.i("Ax", "sendTime:" + m.getSendTime());
		time = m.getSendTime();
//		Log.i("FuWu", "sendTime-3:" + time);
		if (m.getContentType() == ContentType.Text) {
			mp = new MessagePojo(userid, contactid, time, m.getContent(),
					isComMeg, 1);
//			Log.i("FuWu", "serviceMP-:" + mp.toString());
		} else if (m.getContentType() == ContentType.Image) {
			String fileName = System.currentTimeMillis() + "";
			fixedThreadPool.execute(new DownloadImageThread(userid, contactid,
					time, m.getContent(), token, fileName));

			mp = new MessagePojo(user_id, contactid, time, fileName + ".jpg",
					isComMeg, 2);
		}else if (m.getContentType() == ContentType.Notice) {

			mp = new MessagePojo(user_id, contactid, time,  FuXunTools.del_tag(m.getContent()),
					isComMeg, 3);
		}
		list.add(mp);

	}

	/**
	 * 
	 * 更新本地数据库
	 * 
	 * */
	void updataDb(List<MessagePojo> list) {
		int len = list.size();
		TalkPojo tp = null;
		MessagePojo msp;
		int contactid2;
		String str;
		ShortContactPojo cp = null;
		String name = null;

		msp = list.get(len - 1);
		contactid2 = msp.getContactId();
		str = list.get(len - 1).getContent();
		if (msp.getMsgType() == 2) {
			str = "[图片]";
		}
		int num = 0;
		for (int i = 0; i < list.size(); i++) {
			if (list.get(i).getIsComMeg() == 1) {
				num = num + 1;
			}
		}
		if (contactid2==0) { // 系统消息
			tp = new TalkPojo(user_id, contactid2, "系统消息", "", str,
					msp.getSendTime(), len - num);
		}else {
			cp = db.queryContact(user_id, contactid2);
			name = cp.getName();
			if (cp.getCustomName() != null && !cp.getCustomName().equals("")) {
				name = cp.getCustomName();
			}
			tp = new TalkPojo(user_id, contactid2, name, cp.getUserface_url(), str,
					msp.getSendTime(), len - num);
		}
		

		db.addTalk(tp);
		db.updateContactlastContactTime(user_id, contactid2, msp.getSendTime());
		db.addMessageList(list);

	}

}