package com.fuwu.mobileim.util;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Application;
import android.content.Context;

import com.fuwu.mobileim.R;
import com.fuwu.mobileim.pojo.ProfilePojo;
import com.nostra13.universalimageloader.cache.disc.naming.Md5FileNameGenerator;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.QueueProcessingType;
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
	private int hight = 0; // 屏幕宽度

	public synchronized static FxApplication getInstance() {
		return mApplication;
	}

	public void initData() {
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
				.showImageOnLoading(R.drawable.test)
				.showImageForEmptyUri(R.drawable.test)
				.showImageOnFail(R.drawable.test).cacheInMemory(true)
				.cacheOnDisk(false).considerExifParams(true)
				.displayer(new RoundedBitmapDisplayer(20)).build();
		initImageLoader(getApplicationContext());
		mApplication = this;
		initFaceMap();
	}

	public int getWidth() {
		return width;
	}

	public void setWidth(int width) {
		this.width = width;
	}

	public int getHight() {
		return hight;
	}

	public void setHight(int hight) {
		this.hight = hight;
	}

	public List<Activity> getActivityList() {
		return activityList;
	}

	public void setActivityList() {
		this.activityList = new LinkedList<Activity>();
	}

	public static void initImageLoader(Context context) {
		// This configuration tuning is custom. You can tune every option, you
		// may tune some of them,
		// or you can create default configuration by
		// ImageLoaderConfiguration.createDefault(this);
		// method.
		ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(
				context).threadPriority(Thread.NORM_PRIORITY - 2)
				.denyCacheImageMultipleSizesInMemory()
				.diskCacheFileNameGenerator(new Md5FileNameGenerator())
				.tasksProcessingOrder(QueueProcessingType.LIFO)
				.writeDebugLogs() // Remove for release app
				.build();
		// Initialize ImageLoader with configuration.
		ImageLoader.getInstance().init(config);
	}

	public Map<String, Integer> getFaceMap() {
		if (!mFaceMap.isEmpty())
			return mFaceMap;
		return null;
	}

	private void initFaceMap() {
		// TODO Auto-generated method stub

		mFaceMap.put("[#1]", R.drawable.bq1);
		mFaceMap.put("[#2]", R.drawable.bq2);
		mFaceMap.put("[#3]", R.drawable.bq3);
		mFaceMap.put("[#4]", R.drawable.bq4);
		mFaceMap.put("[#5]", R.drawable.bq5);
		mFaceMap.put("[#6]", R.drawable.bq6);
		mFaceMap.put("[#7]", R.drawable.bq7);
		mFaceMap.put("[#8]", R.drawable.bq8);
		mFaceMap.put("[#9]", R.drawable.bq9);
		mFaceMap.put("[#10]", R.drawable.bq10);
		mFaceMap.put("[#11]", R.drawable.bq11);
		mFaceMap.put("[#12]", R.drawable.bq12);
		mFaceMap.put("[#13]", R.drawable.bq13);
		mFaceMap.put("[#14]", R.drawable.bq14);
		mFaceMap.put("[#15]", R.drawable.bq15);
		mFaceMap.put("[#16]", R.drawable.bq16);
		mFaceMap.put("[#17]", R.drawable.bq17);
		mFaceMap.put("[#18]", R.drawable.bq18);
		mFaceMap.put("[#19]", R.drawable.bq19);
		mFaceMap.put("[#20]", R.drawable.bq20);
		mFaceMap.put("[#21]", R.drawable.bq21);
		mFaceMap.put("[#22]", R.drawable.bq22);
		mFaceMap.put("[#23]", R.drawable.bq23);
		mFaceMap.put("[#24]", R.drawable.bq24);
		mFaceMap.put("[#25]", R.drawable.bq25);
		mFaceMap.put("[#26]", R.drawable.bq26);
		mFaceMap.put("[#27]", R.drawable.bq27);
		mFaceMap.put("[#28]", R.drawable.bq28);
		mFaceMap.put("[#29]", R.drawable.bq29);
		mFaceMap.put("[#30]", R.drawable.bq30);
		mFaceMap.put("[#31]", R.drawable.bq31);
		mFaceMap.put("[#32]", R.drawable.bq32);
		mFaceMap.put("[#33]", R.drawable.bq33);
		mFaceMap.put("[#34]", R.drawable.bq34);
		mFaceMap.put("[#35]", R.drawable.bq35);
		mFaceMap.put("[#36]", R.drawable.bq36);
		mFaceMap.put("[#37]", R.drawable.bq37);
		mFaceMap.put("[#38]", R.drawable.bq38);
		mFaceMap.put("[#39]", R.drawable.bq39);
		mFaceMap.put("[#40]", R.drawable.bq40);
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
