package com.fuwu.mobileim.util;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.fuwu.mobileim.pojo.VersionPojo;

public class DBHelper extends SQLiteOpenHelper {

	private static final String DATABASE_NAME = "fuxun.db";
	private static final int DATABASE_VERSION = 2;
	private VersionPojo vPojo;
	private List<VersionPojo> list_version;

	public DBHelper(Context context) {
		// CursorFactory设置为null,使用默认值
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}

	// 数据库第一次被创建时onCreate会被调用

	@Override
	public void onCreate(SQLiteDatabase db) {
		// 聊天记录表: 联系人id,发送时间,内容,消息类型(文本/图片),消息来源(接受/发送);
		db.execSQL("CREATE TABLE IF NOT EXISTS message"
				+ "(id INTEGER PRIMARY KEY AUTOINCREMENT, user_id INTEGER,contact_id INTEGER,content VARCHAR, time VARCHAR, type INTEGER,is_com INTEGER)");
		// 对面列表表: 联系人id,昵称,头像,最后一条消息内容,发送时间,消息数量,时间标记
		db.execSQL("CREATE TABLE IF NOT EXISTS talk"
				+ "(id INTEGER PRIMARY KEY AUTOINCREMENT, user_id INTEGER,contact_id INTEGER,nick_name VARCHAR, head_pic VARCHAR, content VARCHAR,time VARCHAR,mes_count INTEGER)");

		// 联系人表: 联系人id，首字母,昵称，备注,头像,性别,交易订阅,最近联系时间,是否屏蔽
		db.execSQL("CREATE TABLE IF NOT EXISTS contact"
				+ "(id INTEGER PRIMARY KEY AUTOINCREMENT,contactId INTEGER,sortKey VARCHAR, name VARCHAR, customName VARCHAR,userface_url VARCHAR,sex INTEGER,source INTEGER,lastContactTime VARCHAR,isBlocked INTEGER,userId INTEGER,orderTime VARCHAR,subscribeTime VARCHAR)");
		//
		db.execSQL("CREATE TABLE IF NOT EXISTS push"
				+ "(id INTEGER PRIMARY KEY AUTOINCREMENT,content VARCHAR,url VARCHAR,time VARCHAR,status INTEGER,userId INTEGER)");

		Log.i("xinye", "#############数据库创建了##############:" + DATABASE_VERSION);

	}

	// 1 2 3 2.1 2.2 3.1 3.2 3.3
	// 如果DATABASE_VERSION值被改为2,系统发现现有数据库版本不同,即会调用onUpgrade
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		update_db();
		for (int j = 0; j < list_version.size(); j++) {
			VersionPojo pojo = list_version.get(j);
			if (pojo.getVersion() > oldVersion) {
				db.execSQL(pojo.getSql_str());
			}
		}
		Log.i("xinye", "#############数据库升级了##############:" + DATABASE_VERSION);
	}

	public void update_db() {
		list_version = new ArrayList<VersionPojo>();
		list_version.add(new VersionPojo(2,
				"ALTER TABLE contact ADD orderTime VARCHAR"));
		list_version.add(new VersionPojo(2,
				"ALTER TABLE contact ADD subscribeTime VARCHAR"));
	}
}