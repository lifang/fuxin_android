package com.fuwu.mobileim.util;

import java.util.ArrayList;
import java.util.List;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;
import com.fuwu.mobileim.pojo.ContactPojo;
import com.fuwu.mobileim.pojo.MessagePojo;
import com.fuwu.mobileim.pojo.TalkPojo;

public class DBManager {
	private DBHelper helper;
	private SQLiteDatabase db;

	public DBManager(Context context) {
		helper = new DBHelper(context);
		db = helper.getWritableDatabase();
	}

	public void addMessage(MessagePojo mp) {
		db.beginTransaction();
		try {
			db.execSQL(
					"INSERT INTO message VALUES(null,?,?,?,?,?,?)",
					new Object[] { mp.getUserId(), mp.getContactId(),
							mp.getContent(), mp.getSendTime(), mp.getMsgType(),
							mp.getIsComMeg() });
			db.setTransactionSuccessful();
		} finally {
			db.endTransaction();
		}
	}

	public void addMessageList(List<MessagePojo> mps) {
		db.beginTransaction();
		try {
			for (int i = 0; i < mps.size(); i++) {
				MessagePojo mp = mps.get(i);
				db.execSQL(
						"INSERT INTO message VALUES(null,?,?,?,?,?,?)",
						new Object[] { mp.getUserId(), mp.getContactId(),
								mp.getContent(), mp.getSendTime(),
								mp.getMsgType(), mp.getIsComMeg() });
			}
			db.setTransactionSuccessful();
		} finally {
			db.endTransaction();
		}
	}

	public void addTalk(TalkPojo tp) {
		db.beginTransaction();
		try {
			Cursor c = queryTalkCursor(tp.getUser_id(), tp.getContact_id());
			if (c.getCount() > 0) {
				db.execSQL(
						"DELETE from talk where user_id = ? and contact_id = ?",
						new Object[] { tp.getUser_id(), tp.getContact_id(), });
			}
			db.execSQL(
					"INSERT INTO talk VALUES(null,?,?,?,?,?,?,?)",
					new Object[] { tp.getUser_id(), tp.getContact_id(),
							tp.getNick_name(), tp.getHead_pic(),
							tp.getContent(), tp.getTime(), tp.getMes_count() });
			db.setTransactionSuccessful();
		} finally {
			db.endTransaction();
		}
	}

	public void delMessage(int user_id, int contact_id) {
		db.beginTransaction();
		try {
			db.execSQL(
					"Delete from message where user_id = ? and contact_id = ? ",
					new Object[] { user_id + "", contact_id + "" });
		} finally {
			db.endTransaction();
		}
	}

	// 联系人id，首字母,昵称，备注,头像,性别,交易订阅,最近联系时间,是否屏蔽，是不是 福师，认证，个人简介
	public void addContact(int userId, ContactPojo cp) {
		db.beginTransaction();
		try {
			db.execSQL(
					"INSERT INTO contact VALUES(null,?,?,?,?,?,?,?,?,?,?,?,?,?)",
					new Object[] { cp.getContactId(), cp.getSortKey(),
							cp.getName(), cp.getCustomName(),
							cp.getUserface_url(), cp.getSex(), cp.getSource(),
							cp.getLastContactTime(), cp.getIsBlocked(),
							cp.getIsProvider(), cp.getLisence(),
							cp.getIndividualResume(), userId });
			db.setTransactionSuccessful();
		} finally {
			db.endTransaction();
		}
	}

	public boolean modifyContact(int userId, ContactPojo mp) {
		boolean flag = true;
		db = helper.getWritableDatabase();
		try {
			db.execSQL("DELETE FROM contact WHERE contactId = "
					+ mp.getContactId() + " and userId = " + userId);
			addContact(userId, mp);
		} catch (SQLException e) {
			Log.i("Max", "异常:" + e.toString());
			flag = false;
		}
		db.close();
		return flag;
	}

	/**
	 * 获得联系人列表
	 * 
	 * */
	public List<ContactPojo> queryContactList(int user_id) {
		ArrayList<ContactPojo> cpList = new ArrayList<ContactPojo>();
		Cursor c = queryContactCursor(user_id);
		while (c.moveToNext()) {
			ContactPojo mp = new ContactPojo();
			mp.setContactId(c.getInt(c.getColumnIndex("contactId")));
			mp.setCustomName(c.getString(c.getColumnIndex("customName")));
			mp.setIndividualResume(c.getString(c
					.getColumnIndex("individualResume")));
			mp.setIsBlocked(c.getInt(c.getColumnIndex("isBlocked")));
			mp.setIsProvider(c.getInt(c.getColumnIndex("isProvider")));
			mp.setLastContactTime(c.getString(c
					.getColumnIndex("lastContactTime")));
			mp.setLisence(c.getString(c.getColumnIndex("lisence")));
			mp.setName(c.getString(c.getColumnIndex("name")));
			mp.setSex(c.getInt(c.getColumnIndex("sex")));
			mp.setSortKey(c.getString(c.getColumnIndex("sortKey")));
			mp.setSource(c.getInt(c.getColumnIndex("source")));
			mp.setUserface_url(c.getString(c.getColumnIndex("userface_url")));
			cpList.add(mp);

		}
		c.close();
		return cpList;
	}

	public List<MessagePojo> queryMessageList(int user_id, int contact_id,
			int num, int max) {
		ArrayList<MessagePojo> mpList = new ArrayList<MessagePojo>();
		Cursor c = queryMessageCursor(user_id, contact_id, num, max);
		while (c.moveToNext()) {
			MessagePojo mp = new MessagePojo();
			mp.setUserId(user_id);
			mp.setContent(c.getString(c.getColumnIndex("content")));
			mp.setIsComMeg(c.getInt(c.getColumnIndex("is_com")));
			mp.setMsgType(c.getInt(c.getColumnIndex("type")));
			mp.setSendTime(c.getString(c.getColumnIndex("time")));
			mpList.add(mp);
		}
		c.close();
		return mpList;
	}

	public String getLastTime(int user_id, int contact_id) {
		Cursor c = queryMessageLastTimeCursor(user_id, contact_id);
		if (c.moveToLast()) {
			return c.getString(c.getColumnIndex("time"));
		}
		return null;
	}

	public int getMesCount(int user_id, int contact_id) {
		Cursor c = queryMessageCountCursor(user_id, contact_id);
		return c.getCount();
	}

	public List<TalkPojo> queryTalkList(int user_id) {
		Log.i("Max", user_id + "");
		ArrayList<TalkPojo> talkList = new ArrayList<TalkPojo>();
		Cursor c = queryTalkCursor(user_id);
		while (c.moveToNext()) {
			TalkPojo talk = new TalkPojo();
			talk.setUser_id(c.getInt(c.getColumnIndex("user_id")));
			talk.setContact_id(c.getInt(c.getColumnIndex("contact_id")));
			talk.setNick_name(c.getString(c.getColumnIndex("nick_name")));
			talk.setHead_pic(c.getString(c.getColumnIndex("head_pic")));
			talk.setContent(c.getString(c.getColumnIndex("content")));
			talk.setTime(c.getString(c.getColumnIndex("time")));
			talk.setMes_count(c.getInt(c.getColumnIndex("mes_count")));
			talkList.add(talk);
		}
		c.close();
		return talkList;
	}

	public boolean delTalk(int user_id, int contact_id) {
		boolean flag = true;
		db = helper.getWritableDatabase();
		try {
			db.execSQL("DELETE FROM talk WHERE user_id = " + user_id
					+ " and contact_id = " + contact_id);
		} catch (SQLException e) {
			Log.i("Max", "删除异常:" + e.toString());
			flag = false;
		}
		db.close();
		return flag;
	}

	public Cursor queryMessageCountCursor(int user_id, int contact_id) {
		Cursor c = db.rawQuery(
				"SELECT * FROM message where user_id = ? and contact_id = ?",
				new String[] { user_id + "", contact_id + "" });
		return c;
	}

	public Cursor queryContactCursor(int userid) {
		Cursor c = db.rawQuery("SELECT * FROM contact where userId = ?",
				new String[] { userid + "" });
		return c;
	}

	public Cursor queryMessageCursor(int user_id, int contact_id, int num,
			int max) {
		Cursor c = db
				.rawQuery(
						"SELECT * FROM message where user_id = ? and contact_id = ? limit ?,?",
						new String[] { user_id + "", contact_id + "", num + "",
								max + "" });
		return c;
	}

	public Cursor queryMessageLastTimeCursor(int user_id, int contact_id) {
		Cursor c = db
				.rawQuery(
						"SELECT * FROM message where user_id = ? and contact_id = ? and time != ?",
						new String[] { user_id + "", contact_id + "", "" });
		return c;
	}

	public Cursor queryTalkCursor(int user_id) {
		Cursor c = db.rawQuery(
				"SELECT * FROM talk where user_id = ? order by time desc",
				new String[] { user_id + "" });
		return c;
	}

	public Cursor queryTalkCursor(int user_id, int contact_id) {
		Cursor c = db.rawQuery(
				"SELECT * FROM talk where user_id = ? and contact_id = ?",
				new String[] { user_id + "", contact_id + "" });
		return c;
	}

	public void closeDB() {
		db.close();
	}

	public boolean isOpen() {
		return db.isOpen();
	}
}