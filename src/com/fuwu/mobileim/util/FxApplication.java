package com.fuwu.mobileim.util;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Application;

import com.fuwu.mobileim.R;
import com.fuwu.mobileim.pojo.ProfilePojo;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.display.RoundedBitmapDisplayer;

/**
 * @作者 马龙
 * @时间 创建时间：2014-5-19 下午12:15:25
 */
public class FxApplication extends Application {
	public static final int NUM_PAGE = 2;// 总共有多少页
	public static int NUM = 20;// 每页20个表情,还有最后一个删除button
	private Map<String, Integer> mFaceMap = new LinkedHashMap<String, Integer>();
	private static FxApplication mApplication;
	@SuppressLint("UseSparseArrays")
	private int user_id;
	private String token;
	private ProfilePojo profilePojo = new ProfilePojo();
	public DisplayImageOptions options;
	public Map<String, String> error_map;
	public Map<String, String> ValidateCode;
	private List<Activity> activityList = new LinkedList<Activity>();
	private int width = 0; // 屏幕宽度
	private int height = 0; // 屏幕宽度
	private boolean user_exit = false;
	public synchronized static FxApplication getInstance() {
		return mApplication;
	}

	public void initData() {
		user_exit = false;
		profilePojo = new ProfilePojo();
	}

	public FxApplication() {
	}

	public void onCreate() {

		super.onCreate();
		error_map = new HashMap<String, String>();
		error_map.put("InvalidUserName", "用户名不存在");
		error_map.put("InvalidPassword", "密码不正确");
		error_map.put("InvalidPasswordExceedCount", "密码错误次数过多");
		error_map.put("NotActivate", "服务器无响应");
		error_map.put("InvalidValidateCode", "验证码错误");
		error_map.put("InvalidDatabase", "连接数据库失败");
		error_map.put("ExistingUserYes", "昵称已被注册");
		error_map.put("InvalidMatchPassword", "两次密码不一致");
		error_map.put("InvalidPasswordConfirm", "两次密码不一致");
		error_map.put("InvalidPhoneNumber", "手机号码错误");
		error_map.put("InvalidOriginalPassword", "原密码错误");

		ValidateCode = new HashMap<String, String>();
		ValidateCode.put("BadRequest", "序列化参数出错");
		ValidateCode.put("InvalidPhoneNumber", "手机号码有误");
		ValidateCode.put("InvalidType", "发送类型有误");
		ValidateCode.put("ExistingUserYes", "用户已存在");
		ValidateCode.put("ExistingUserNo", "用户不存在");
		ValidateCode.put("LockTime", "限定的时间内不能重复发送");
		ValidateCode.put("SendError", "短信服务出错，发送失败");

		options = new DisplayImageOptions.Builder()
				.showImageOnLoading(R.drawable.moren)
				.showImageForEmptyUri(R.drawable.moren)
				.showImageOnFail(R.drawable.test).cacheInMemory(true)
				.cacheOnDisk(false).considerExifParams(true)
				.displayer(new RoundedBitmapDisplayer(20)).build();
		mApplication = this;
		initFaceMap();
	}

	public boolean getUser_exit() {
		return user_exit;
	}

	public void setUser_exit(boolean user_exit) {
		this.user_exit = user_exit;
	}

	public int getWidth() {
		return width;
	}

	public void setWidth(int width) {
		this.width = width;
	}

	public int getHeight() {
		return height;
	}

	public void setHeight(int height) {
		this.height = height;
	}

	public List<Activity> getActivityList() {
		return activityList;
	}

	public void setActivityList() {
		this.activityList = new LinkedList<Activity>();
	}

	public Map<String, Integer> getFaceMap() {
		if (!mFaceMap.isEmpty())
			return mFaceMap;
		return null;
	}

	private void initFaceMap() {
		// TODO Auto-generated method stub

		mFaceMap.put("[#微笑]", R.drawable.bq1);
		mFaceMap.put("[#羞涩]", R.drawable.bq2);
		mFaceMap.put("[#吐舌]", R.drawable.bq3);
		mFaceMap.put("[#偷笑]", R.drawable.bq4);
		mFaceMap.put("[#大笑]", R.drawable.bq5);
		mFaceMap.put("[#飞吻]", R.drawable.bq6);
		mFaceMap.put("[#安慰]", R.drawable.bq7);
		mFaceMap.put("[#给力]", R.drawable.bq8);
		mFaceMap.put("[#哦耶]", R.drawable.bq9);
		mFaceMap.put("[#怒赞]", R.drawable.bq10);
		mFaceMap.put("[#狂爱]", R.drawable.bq11);
		mFaceMap.put("[#得意]", R.drawable.bq12);
		mFaceMap.put("[#查找]", R.drawable.bq13);
		mFaceMap.put("[#大吼]", R.drawable.bq14);
		mFaceMap.put("[#计算]", R.drawable.bq15);
		mFaceMap.put("[#鬼脸]", R.drawable.bq16);
		mFaceMap.put("[#招手]", R.drawable.bq17);
		mFaceMap.put("[#口水]", R.drawable.bq18);
		mFaceMap.put("[#美梦]", R.drawable.bq19);
		mFaceMap.put("[#鼻血]", R.drawable.bq20);
		mFaceMap.put("[#不解]", R.drawable.bq21);
		mFaceMap.put("[#疑惑]", R.drawable.bq22);
		mFaceMap.put("[#等待]", R.drawable.bq23);
		mFaceMap.put("[#撇嘴]", R.drawable.bq24);
		mFaceMap.put("[#困扰]", R.drawable.bq25);
		mFaceMap.put("[#无奈]", R.drawable.bq26);
		mFaceMap.put("[#无语]", R.drawable.bq27);
		mFaceMap.put("[#愧疚]", R.drawable.bq28);
		mFaceMap.put("[#流汗]", R.drawable.bq29);
		mFaceMap.put("[#困倦]", R.drawable.bq30);
		mFaceMap.put("[#大哭]", R.drawable.bq31);
		mFaceMap.put("[#含泪]", R.drawable.bq32);
		mFaceMap.put("[#抱歉]", R.drawable.bq33);
		mFaceMap.put("[#憔悴]", R.drawable.bq34);
		mFaceMap.put("[#惊讶]", R.drawable.bq35);
		mFaceMap.put("[#生气]", R.drawable.bq36);
		mFaceMap.put("[#恭喜]", R.drawable.bq37);
		mFaceMap.put("[#好的]", R.drawable.bq38);
		mFaceMap.put("[#鼓掌]", R.drawable.bq39);
		mFaceMap.put("[#握手]", R.drawable.bq40);
	}

	public int getUser_id() {
		return user_id;
	}

	public void setUser_id(int user_id) {
		this.user_id = user_id;
	}

	public String getToken() {
		return token;
	}

	public void setToken(String token) {
		this.token = token;
	}

	public ProfilePojo getProfilePojo() {
		return profilePojo;
	}

	public void setProfilePojo(ProfilePojo profilePojo) {
		this.profilePojo = profilePojo;
	}

}
