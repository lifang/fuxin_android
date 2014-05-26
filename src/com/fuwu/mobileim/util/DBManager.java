package com.fuwu.mobileim.util;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.fuwu.mobileim.pojo.MessagePojo;

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