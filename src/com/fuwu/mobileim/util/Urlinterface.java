package com.fuwu.mobileim.util;

import android.os.Environment;

public interface Urlinterface {
	static final String head_pic = Environment.getExternalStorageDirectory()
			.getPath() + "/fuXun/head_pic/";
	static final String WebViewUrl = "https://118.242.18.189/resource/static/public/doc/app%20agreement.html";
	static final String tag = "FuXun";
	static final String SHARED = "FuXun";
	static final double current_version = 0.0;// 应用版本号
	static final String IP = "https://118.242.18.189/IMApi/";
	
	static String fileurl = IP + "fuxin.apk";
	static String filename = "fuxin.apk";
	// static final String IP = "https://118.242.18.189/IMApiMock/";
	// 获得联系人
	static final String getContacts = IP + "api/Contact";
	// 注册
	static final String REGIST = IP + "api/Register";
	// 短信验证
	static final String ValidateCode = IP + "api/ValidateCode";
	// 登陆
	static final String LOGIN = IP + "api/Authentication";
	// 修改密码
	static final String PASSWORD = IP + "api/ChangePassword";
	// 找回密码
	static final String RESETPASSWORD = IP + "api/ResetPassword";
	// 获得 个人详细信息
	String PROFILE = IP + "api/Profile";
	// 修改个人详细信息
	static final String ChangeProfile = IP + "api/Profile";
	// 获取/发送 消息
	static final String Message = IP + "api/Message";
	// 获得个人详细信息
	static final String getProfile = IP + "api/Profile";
	// 是否屏蔽 联系人 put
	static final String BlockContact = IP + "api/Contact";
	// 
	static final String Client = IP + "api/Client";

}
