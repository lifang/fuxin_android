package com.fuwu.mobileim.activity;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
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
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.view.View;

import com.fuwu.mobileim.R;
import com.fuwu.mobileim.model.Models.Message;
import com.fuwu.mobileim.model.Models.Message.ContentType;
import com.fuwu.mobileim.model.Models.Contact;
import com.fuwu.mobileim.model.Models.ContactDetailRequest;
import com.fuwu.mobileim.model.Models.ContactDetailResponse;
import com.fuwu.mobileim.model.Models.License;
import com.fuwu.mobileim.model.Models.MessageList;
import com.fuwu.mobileim.model.Models.MessageRequest;
import com.fuwu.mobileim.model.Models.MessageResponse;
import com.fuwu.mobileim.pojo.ContactPojo;
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
	private static Thread GetContactDetail;
	/**
	 * 根据消息时间来排序
	 */
	private SendDataComparator sendDataComparator = new SendDataComparator();
	private boolean flag = false;
	private int time = 25;
	private static SharedPreferences sp;
	private IBinder binder = new RequstService.RequstBinder();
	private ScheduledExecutorService scheduledThreadPool = Executors
			.newScheduledThreadPool(2);
	private ExecutorService fixedThreadPool = Executors.newFixedThreadPool(2);
	private ExecutorService pushThreadPool = Executors.newFixedThreadPool(2);
	private static DBManager db;
	private Context context;
	private int user_id;
	private int contact_id;
	private String token;
	private static ShortContactPojo cp = null;
	private int isComMeg = 0;// 0接收/1发送 消息
	private Handler handler = new Handler() {
		public void handleMessage(android.os.Message msg) {
			switch (msg.what) {
			case 0:
				break;
			case 1:
				break;

			}
		}
	};

	public IBinder onBind(Intent intent) {
		// 一个客户端通过bindService()绑定到这个service  
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
		// service 正在启动，在调用startService()期间被调用  
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
				Intent intnet = new Intent("com.comdosoft.fuxun.REQUEST_ACTION2");
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
					Log.i("RequstThread", "timeStamp:" + getTimeStamp());
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
											Log.i("FuWu1", m.getContent()
													+ "--" + m.getSendTime());
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
		// Log.i("FuWu", "sendTime-3:" + time);
		if (m.getContentType() == ContentType.Text) {
			mp = new MessagePojo(userid, contactid, time, m.getContent(),
					isComMeg, 1);
			// Log.i("FuWu", "serviceMP-:" + mp.toString());
		} else if (m.getContentType() == ContentType.Image) {
			String fileName = System.currentTimeMillis() + "";
			fixedThreadPool.execute(new DownloadImageThread(userid, contactid,
					time, m.getContent(), token, fileName));

			mp = new MessagePojo(user_id, contactid, time, fileName + ".jpg",
					isComMeg, 2);
		} else if (m.getContentType() == ContentType.Notice) {

			mp = new MessagePojo(user_id, contactid, time, m
					.getContent(), isComMeg, 3);
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
		if (contactid2 == 0) { // 系统消息
			tp = new TalkPojo(user_id, contactid2, "系统消息", "", FuXunTools.del_tag(str),
					msp.getSendTime(), len - num);
			db.addTalk(tp);
			db.updateContactlastContactTime(user_id, contactid2,
					msp.getSendTime());
			db.addMessageList(list);
		} else {
			cp = db.queryContact(user_id, contactid2);
			if (cp.getContactId() == 0) { // 本地没有该用户
				db.addMessageList(list);
				get_new_contact(contactid2, msp.getSendTime(), str, len - num,
						list);
			} else {
				name = cp.getName();
				if (cp.getCustomName() != null
						&& !cp.getCustomName().equals("")) {
					name = cp.getCustomName();
				}
				tp = new TalkPojo(user_id, contactid2, name,
						cp.getUserface_url(), str, msp.getSendTime(), len - num);
				db.addTalk(tp);
				db.updateContactlastContactTime(user_id, contactid2,
						msp.getSendTime());
				db.addMessageList(list);
			}

		}

	}

	public static void get_new_contact(final int contact_id,
			final String SendTime, final String content, final int len,
			final List<MessagePojo> list) {

		final Handler mHandler = new Handler() {
			public void handleMessage(android.os.Message msg) {
				TalkPojo tp = null;
				switch (msg.what) {
				case 0:
					ShortContactPojo cp = (ShortContactPojo) msg.obj;
					FuXunTools.getBitmap_url(cp.getUserface_url(), contact_id);
					String name = cp.getName();
					if (cp.getCustomName() != null
							&& !cp.getCustomName().equals("")) {
						name = cp.getCustomName();
					}
					tp = new TalkPojo(sp.getInt("user_id", 0), contact_id,
							name, cp.getUserface_url(), content, SendTime, len);
					db.addTalk(tp);
					db.addContact(sp.getInt("user_id", 0), cp);
					
					break;
				case 1:

					break;
				case 2:
					tp = new TalkPojo(sp.getInt("user_id", 0), contact_id,
							"暂未设置昵称", "", content, SendTime, len);
					db.addTalk(tp);
					break;
				default:
					break;
				}
			}
		};

		Thread GetContactDetail = new Thread() {
			public void run() {
				try {
					ContactDetailRequest.Builder builder = ContactDetailRequest
							.newBuilder();
					builder.setUserId(sp.getInt("user_id", 0));
					builder.setContactId(contact_id);
					builder.setToken(sp.getString("Token", ""));
					ContactDetailRequest response = builder.build();
					byte[] by = HttpUtil.sendHttps(response.toByteArray(),
							Urlinterface.ContactDetail, "POST");
					if (by != null && by.length > 0) {
						ContactDetailResponse res = ContactDetailResponse
								.parseFrom(by);
						if (res.getIsSucceed()) {
							Contact contact = res.getContact();
							int contactId = contact.getContactId();
							String name = contact.getName();
							String customName = contact.getCustomName();
							String sortKey = "";
							String userface_url = contact.getTileUrl();
							int sex = contact.getGender().getNumber();
							int source = contact.getSource();
							String lastContactTime = contact
									.getLastContactTime();
							boolean isblocked = contact.getIsBlocked();
							boolean isprovider = contact.getIsProvider();
							int isBlocked = -1, isProvider = -1;
							if (isblocked == true) {
								isBlocked = 1;
							} else if (isblocked == false) {
								isBlocked = 0;
							}
							if (isprovider == true) {
								isProvider = 1;
							} else if (isprovider == false) {
								isProvider = 0;
							}
							String orderTime = contact.getOrderTime();
							String subscribeTime = contact.getSubscribeTime();
							ShortContactPojo cp = new ShortContactPojo(
									contactId, sortKey, name, customName,
									userface_url, sex, source, lastContactTime,
									isBlocked, orderTime, subscribeTime);
//							android.os.Message me = new android.os.Message();
//							me.what = 0;
//							me.obj = cp;
//							mHandler.sendMessage(me);
							Log.i("Ax", "contact:" + cp.toString());
							
							String name2 = cp.getName();
							if (cp.getCustomName() != null
									&& !cp.getCustomName().equals("")) {
								name2 = cp.getCustomName();
							}
							TalkPojo	tp = new TalkPojo(sp.getInt("user_id", 0), contact_id,
									name2, cp.getUserface_url(), content, SendTime, len);
							db.addTalk(tp);
							db.addContact(sp.getInt("user_id", 0), cp);
							FuXunTools.getBitmap_url(cp.getUserface_url(), contact_id);
							Log.i("Ax", "contact2:" + cp.toString());
						} else {
							mHandler.sendEmptyMessage(2);
						}
					} else {
						mHandler.sendEmptyMessage(2);
					}
				} catch (Exception e) {
					mHandler.sendEmptyMessage(2);
				}
			}
		};
		GetContactDetail.start();

	}

}