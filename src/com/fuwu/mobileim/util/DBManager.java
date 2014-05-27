package com.fuwu.mobileim.util;

import java.util.ArrayList;
import java.util.List;
import com.fuwu.mobileim.pojo.MessagePojo;
import com.fuwu.mobileim.pojo.TalkPojo;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

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
					"INSERT INTO message VALUES(null,?,?,?,?,?)",
					new Object[] { mp.getUserId(), mp.getContent(),
							mp.getSendTime(), mp.getMsgType(), mp.getIsComMeg() });
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
						"INSERT INTO message VALUES(null,?,?,?,?,?)",
						new Object[] { mp.getUserId(), mp.getContent(),
								mp.getSendTime(), mp.getMsgType(),
								mp.getIsComMeg() });
			}
			db.setTransactionSuccessful();
		} finally {
			db.endTransaction();
		}
	}

	public void addTalk(TalkPojo tp) {
		db.beginTransaction();
		try {
			db.execSQL(
					"INSERT INTO talk VALUES(null,?,?,?,?,?,?)",
					new Object[] { tp.getContact_id(), tp.getNick_name(),
							tp.getHead_pic(), tp.getContent(), tp.getTime(),
							tp.getMes_count() });
			db.setTransactionSuccessful();
		} finally {
			db.endTransaction();
		}
	}

	public List<MessagePojo> queryMessageList(int user_id) {
		ArrayList<MessagePojo> mpList = new ArrayList<MessagePojo>();
		Cursor c = queryMessageCursor(user_id);
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

	public String getLastTime(int user_id) {
		Cursor c = queryMessageLastTimeCursor(user_id);
		if (c.moveToLast()) {
			return c.getString(c.getColumnIndex("time"));
		}
		return null;
	}

	public Cursor queryMessageCursor(int user_id) {
		Cursor c = db.rawQuery("SELECT * FROM message where user_id = ?",
				new String[] { user_id + "" });
		return c;
	}

	public Cursor queryMessageLastTimeCursor(int user_id) {
		Cursor c = db.rawQuery(
				"SELECT * FROM message where user_id = ? and time != ?",
				new String[] { user_id + "", "" });
		return c;
	}

	public void closeDB() {
		db.close();
	}

	public boolean isOpen() {
		return db.isOpen();
	}
}