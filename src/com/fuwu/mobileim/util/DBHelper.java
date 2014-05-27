package com.fuwu.mobileim.util;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DBHelper extends SQLiteOpenHelper {

	private static final String DATABASE_NAME = "fuxun.db";
	private static final int DATABASE_VERSION = 1;

	public DBHelper(Context context) {
		// CursorFactory设置为null,使用默认值
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}

	// 数据库第一次被创建时onCreate会被调用

	@Override
	public void onCreate(SQLiteDatabase db) {
		// 聊天记录表: 联系人id,发送时间,内容,消息类型(文本/图片),消息来源(接受/发送);
		db.execSQL("CREATE TABLE IF NOT EXISTS message"
				+ "(id INTEGER PRIMARY KEY AUTOINCREMENT, user_id INTEGER,content VARCHAR, time VARCHAR, type INTEGER,is_com INTEGER)");
		// 对面列表表: 联系人id,昵称,头像,最后一条消息内容,发送时间,消息数量,时间标记
		db.execSQL("CREATE TABLE IF NOT EXISTS talk"
				+ "(id INTEGER PRIMARY KEY AUTOINCREMENT, contact_id INTEGER,nick_name VARCHAR, head_pic VARCHAR, content VARCHAR,time VARCHAR,mes_count INTEGER)");
	}

	// 如果DATABASE_VERSION值被改为2,系统发现现有数据库版本不同,即会调用onUpgrade
	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

	}
}