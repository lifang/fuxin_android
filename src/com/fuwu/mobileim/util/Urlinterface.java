package com.fuwu.mobileim.util;

import android.os.Environment;

public interface Urlinterface {
	static final String head_pic = Environment.getExternalStorageDirectory().getPath()+"/fuXun/head_pic/";
	static final String tag = "FuXun";
	static final String SHARED = "FuXun";
	static final double current_version = 0.0;// 应用版本号
	static final String IP = "https://118.242.18.189";

	String getContacts = IP + "/api/Contact";// 获得联系人

}
