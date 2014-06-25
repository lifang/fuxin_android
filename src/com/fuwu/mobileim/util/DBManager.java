package com.fuwu.mobileim.util;

import java.util.ArrayList;
import java.util.List;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;
import com.fuwu.mobileim.pojo.MessagePojo;
import com.fuwu.mobileim.pojo.PushPojo;
import com.fuwu.mobileim.pojo.TalkPojo;
import com.fuwu.mobileim.pojo.ShortContactPojo;

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
				Log.i("FuWu", "mp:" + mp.toString());
				if (mp != null && mp.getUserId() != 0) {
					db.execSQL(
							"INSERT INTO message VALUES(null,?,?,?,?,?,?)",
							new Object[] { mp.getUserId(), mp.getContactId(),
									mp.getContent(), mp.getSendTime(),
									mp.getMsgType(), mp.getIsComMeg() });
				}
			}
			db.setTransactionSuccessful();
		} catch (Exception e) {
			Log.i("FuWu", e.getMessage());
		} finally {
			db.endTransaction();
		}
	}

	public void addTalk(TalkPojo tp) {
		db.beginTransaction();
		Cursor c = null;
		try {
			c = queryTalkCursor(tp.getUser_id(), tp.getContact_id());
			if (c.getCount() > 0) {
				c.moveToFirst();
				int count = c.getInt(c.getColumnIndex("mes_count"));
				db.execSQL(
						"update talk set nick_name = ? , content = ? , time = ? , mes_count = ? where user_id = ? and contact_id = ?",
						new Object[] { tp.getNick_name(), tp.getContent(),
								tp.getTime(), tp.getMes_count() + count,
								tp.getUser_id(), tp.getContact_id(), });
			} else {
				db.execSQL(
						"INSERT INTO talk VALUES(null,?,?,?,?,?,?,?)",
						new Object[] { tp.getUser_id(), tp.getContact_id(),
								tp.getNick_name(), tp.getHead_pic(),
								tp.getContent(), tp.getTime(),
								tp.getMes_count() });
			}
			db.setTransactionSuccessful();
		} catch (Exception e) {
			Log.i("FuWu", e.getMessage());
		} finally {
			c.close();
			db.endTransaction();
		}
	}

	public void updateContactRem(int user_id, int contact_id, String rem) {
		db.beginTransaction();
		try {
			db.execSQL(
					"update contact set customName = ? where userId = ? and contactId = ?",
					new Object[] { rem, user_id + "", contact_id + "" });
			db.execSQL(
					"update talk set nick_name = ? where user_id = ? and contact_id = ?",
					new Object[] { rem, user_id + "", contact_id + "" });

			db.setTransactionSuccessful();
		} finally {
			db.endTransaction();
		}
	}

	public void updateContactlastContactTime(int user_id, int contact_id,
			String time) {
		db.beginTransaction();
		try {
			db.execSQL(
					"update contact set lastContactTime = ? where userId = ? and contactId = ?",
					new Object[] { time, user_id + "", contact_id + "" });

			db.setTransactionSuccessful();
		} finally {
			db.endTransaction();
		}
	}

	public void updateTalkRem(int user_id, int contact_id, String rem) {
		db.beginTransaction();
		try {
			db.execSQL(
					"update talk set nick_name = ? where user_id = ? and contact_id = ?",
					new Object[] { rem, user_id + "", contact_id + "" });

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
			db.execSQL(
					"Delete from talk where user_id = ? and contact_id = ? ",
					new Object[] { user_id + "", contact_id + "" });
			db.setTransactionSuccessful();
		} finally {
			db.endTransaction();
		}
	}

	public void delMessage(int user_id) {
		db.beginTransaction();
		try {
			db.execSQL("Delete from message where user_id = ?",
					new Object[] { user_id + "" });
			db.execSQL("Delete from talk where user_id = ?",
					new Object[] { user_id + "" });
			db.setTransactionSuccessful();
		} finally {
			db.endTransaction();
		}
	}

	// 联系人id，首字母,昵称，备注,头像,性别,交易订阅,最近联系时间,是否屏蔽，
	public void addContact(int userId, ShortContactPojo cp) {
		db.beginTransaction();
		try {
			db.execSQL(
					"INSERT INTO contact VALUES(null,?,?,?,?,?,?,?,?,?,?)",
					new Object[] { cp.getContactId(), cp.getSortKey(),
							cp.getName(), cp.getCustomName(),
							cp.getUserface_url(), cp.getSex(), cp.getSource(),
							cp.getLastContactTime(), cp.getIsBlocked(), userId });
			db.setTransactionSuccessful();
		} finally {
			db.endTransaction();
		}
	}

	public boolean modifyContact(int userId, ShortContactPojo mp) {
		boolean flag = true;
		db.beginTransaction();
		try {
			db.execSQL("DELETE FROM contact WHERE contactId = "
					+ mp.getContactId() + " and userId = " + userId);
			addContact(userId, mp);
			db.setTransactionSuccessful();
		} finally {
			db.endTransaction();
		}
		return flag;
	}

	public boolean modifyContactBlock(int isblocked, int userId, int contactId) {
		boolean flag = true;
		db.beginTransaction();
		try {
			db.execSQL(
					"update  contact set  isBlocked = ?  WHERE userId = ? and contactId = ?",
					new Object[] { isblocked, userId, contactId });
			db.setTransactionSuccessful();
		} finally {
			db.endTransaction();
		}
		return flag;
	}

	public List<ShortContactPojo> queryContactList(int user_id) {
		ArrayList<ShortContactPojo> cpList = new ArrayList<ShortContactPojo>();
		Cursor c = null;
		try {
			c = queryContactCursor(user_id);
			while (c.moveToNext()) {

				ShortContactPojo mp = new ShortContactPojo();
				mp.setContactId(c.getInt(c.getColumnIndex("contactId")));
				mp.setCustomName(c.getString(c.getColumnIndex("customName")));
				mp.setIsBlocked(c.getInt(c.getColumnIndex("isBlocked")));
				mp.setLastContactTime(c.getString(c
						.getColumnIndex("lastContactTime")));
				mp.setName(c.getString(c.getColumnIndex("name")));
				mp.setSex(c.getInt(c.getColumnIndex("sex")));
				String sortKey = FuXunTools.getSortKey(
						c.getString(c.getColumnIndex("customName")),
						c.getString(c.getColumnIndex("name")));
				mp.setSortKey(sortKey);
				// mp.setSortKey(c.getString(c.getColumnIndex("sortKey")));
				mp.setSource(c.getInt(c.getColumnIndex("source")));
				mp.setUserface_url(c.getString(c.getColumnIndex("userface_url")));
				cpList.add(mp);

			}
		} catch (Exception e) {
		} finally {
			c.close();
		}
		return cpList;
	}

	public ShortContactPojo queryContact(int user_id, int contact_id) {
		Cursor c = null;
		ShortContactPojo mp = new ShortContactPojo();
		try {
			c = queryContactCursor(user_id, contact_id);
			if (c.moveToNext()) {

				mp.setContactId(c.getInt(c.getColumnIndex("contactId")));
				mp.setCustomName(c.getString(c.getColumnIndex("customName")));
				mp.setIsBlocked(c.getInt(c.getColumnIndex("isBlocked")));
				mp.setLastContactTime(c.getString(c
						.getColumnIndex("lastContactTime")));
				mp.setName(c.getString(c.getColumnIndex("name")));
				mp.setSex(c.getInt(c.getColumnIndex("sex")));
				String sortKey = FuXunTools.getSortKey(
						c.getString(c.getColumnIndex("customName")),
						c.getString(c.getColumnIndex("name")));
				mp.setSortKey(sortKey);
				// mp.setSortKey(c.getString(c.getColumnIndex("sortKey")));
				mp.setSource(c.getInt(c.getColumnIndex("source")));
				mp.setUserface_url(c.getString(c.getColumnIndex("userface_url")));
			}
		} catch (Exception e) {
		} finally {
			c.close();
		}
		return mp;
	}

	public List<MessagePojo> queryMessageList(int user_id, int contact_id,
			int num, int max) {
		ArrayList<MessagePojo> mpList = new ArrayList<MessagePojo>();
		Cursor c = null;
		try {
			clearTalkMesCount(user_id, contact_id);
			c = queryMessageCursor(user_id, contact_id, num, max);
			while (c.moveToNext()) {
				MessagePojo mp = new MessagePojo();
				mp.setUserId(user_id);
				mp.setContactId(contact_id);
				mp.setContent(c.getString(c.getColumnIndex("content")));
				mp.setIsComMeg(c.getInt(c.getColumnIndex("is_com")));
				mp.setMsgType(c.getInt(c.getColumnIndex("type")));
				mp.setSendTime(c.getString(c.getColumnIndex("time")));
				mpList.add(mp);
			}
		} catch (Exception e) {
		} finally {
			c.close();
		}
		return mpList;
	}

	public String getLastTime(int user_id, int contact_id) {
		Cursor c = null;
		String time = "";
		try {
			c = queryMessageLastTimeCursor(user_id, contact_id);
			if (c.moveToLast()) {
				time = c.getString(c.getColumnIndex("time"));
			}
		} catch (Exception e) {
		} finally {
			c.close();
		}
		return time;
	}

	public int getMesCount(int user_id, int contact_id) {
		Cursor c = null;
		int count = 0;
		try {
			c = queryMessageCountCursor(user_id, contact_id);
			count = c.getCount();
		} catch (Exception e) {
		} finally {
			c.close();
		}
		return count;
	}

	public int queryMessageCount(int user_id) {
		int count = 0;
		Cursor c = null;
		try {
			c = queryTalkCursor(user_id);
			while (c.moveToNext()) {
				count = count + c.getInt(c.getColumnIndex("mes_count"));
			}
		} catch (Exception e) {
		} finally {
			c.close();
		}
		return count;
	}

	public List<TalkPojo> queryTalkList(int user_id) {
		ArrayList<TalkPojo> talkList = new ArrayList<TalkPojo>();
		Cursor c = null;
		try {
			c = queryTalkCursor(user_id);
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
		} catch (Exception e) {
		} finally {
			c.close();
		}
		return talkList;
	}

	public List<PushPojo> queryPushList(int user_id) {
		Log.i("Max", user_id + "");
		ArrayList<PushPojo> pushList = new ArrayList<PushPojo>();
		Cursor c = null;
		try {
			c = querySystemPush(user_id);
			while (c.moveToNext()) {
				PushPojo push = new PushPojo();
				push.setId(c.getInt(c.getColumnIndex("id")));
				push.setContent(c.getString(c.getColumnIndex("content")));
				push.setUrl(c.getString(c.getColumnIndex("url")));
				push.setTime(c.getString(c.getColumnIndex("time")));
				push.setStatus(c.getInt(c.getColumnIndex("status")));
				push.setUserId(c.getInt(c.getColumnIndex("userId")));
				pushList.add(push);
			}
		} catch (Exception e) {
		} finally {
			c.close();
		}
		return pushList;
	}

	public boolean delTalk(int user_id, int contact_id) {
		boolean flag = true;
		db.beginTransaction();
		try {
			db.execSQL("Delete FROM talk where user_id = ? and contact_id = ?",
					new String[] { user_id + "", contact_id + "" });
			db.setTransactionSuccessful();
		} catch (SQLException e) {
			Log.i("Max", "删除异常:" + e.toString());
			flag = false;
		} finally {
			db.endTransaction();
		}
		Log.i("Max", flag + "");
		return flag;
	}

	public void clearTalkMesCount(int user_id, int contact_id) {
		db.beginTransaction();
		try {
			db.execSQL(
					"UPDATE talk set mes_count = 0 where user_id = ? and contact_id = ? ",
					new Object[] { user_id + "", contact_id + "" });
			db.setTransactionSuccessful();
		} finally {
			db.endTransaction();
		}
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

	public Cursor queryContactCursor(int userid, int contact_id) {
		Cursor c = db.rawQuery(
				"SELECT * FROM contact where userId = ? and contactId = ?",
				new String[] { userid + "", contact_id + "" });
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

	public Cursor querySystemPush(int user_id) {
		Cursor c = db.rawQuery("SELECT * FROM push where user_id = ?",
				new String[] { user_id + "" });
		return c;
	}

	public void closeDB() {
		db.close();
	}

	public boolean isOpen() {
		return db.isOpen();
	}

}