package com.fuwu.mobileim.util;

public interface Urlinterface {
	static final String tag = "FuXun";
	static final String SHARED = "FuXun";
	static final double current_version = 0.0;// 应用版本号
	static final String IP = "https://118.242.18.189";

	// 注册
	static final String REGIST = IP + "/api/Register";
	// 短信验证
	static final String ValidateCode = IP + "/api/ValidateCode";
	// 登陆
	static final String LOGIN = IP + "/api/Authentication";
	// 修改密码
	static final String PASSWORD = IP + "/api/Password";
}
