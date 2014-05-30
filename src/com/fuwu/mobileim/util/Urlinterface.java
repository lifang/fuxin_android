package com.fuwu.mobileim.util;

import android.os.Environment;

public interface Urlinterface {
	static final String head_pic = Environment.getExternalStorageDirectory()
			.getPath() + "/fuXun/head_pic/";
	static final String tag = "FuXun";
	static final String SHARED = "FuXun";
	static final double current_version = 0.0;// 应用版本号
	static final String IP = "https://118.242.18.189/IMApiMock/";
	// static final String IP = "https://118.242.18.189/IMApiMock";

	String getContacts = IP + "api/Contact";// 获得联系人
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

	// 获得个人详细信息
	String getProfile = IP + "api/Profile";
}
