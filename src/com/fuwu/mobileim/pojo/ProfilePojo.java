package com.fuwu.mobileim.pojo;

import java.util.List;

import com.fuwu.mobileim.model.Models.License;

public class ProfilePojo {

	private int userId;// 用户id
	private String name;// 名称
	private String nickName;// 昵称
	private int gender;// 性别
	private String tileUrl;// 头像
	private boolean isProvider;// 福师
	private String lisence;// 行业认证（纯汉字）
	private String mobile;// 手机号码
	private String email;// 邮箱
	private String birthday;// 生日
	private boolean isAuthentication;// 实名认证
	private String fuZhi;// 福值
	private String location;//  //所在地
	private String description;// 福师简介
//	private String lisences;// 行业认证（非纯汉字，小图标）
	private List<License> licenses;
	public ProfilePojo() {
	}

	public ProfilePojo(int userId, String name, String nickName, int gender,
			String tileUrl, boolean isProvider, String lisence, String mobile,
			String email, String birthday, boolean isAuthentication , String fuZhi) {
		super();
		this.userId = userId;
		this.name = name;
		this.nickName = nickName;
		this.gender = gender;
		this.tileUrl = tileUrl;
		this.isProvider = isProvider;
		this.lisence = lisence;
		this.mobile = mobile;
		this.email = email;
		this.birthday = birthday;
		this.isAuthentication = isAuthentication;
		this.fuZhi = fuZhi;
	}

	
	public ProfilePojo(int userId, String name, String nickName, int gender,
			String tileUrl, boolean isProvider, String lisence, String mobile,
			String email, String birthday, boolean isAuthentication,
			String fuZhi,  String location, String description) {
		super();
		this.userId = userId;
		this.name = name;
		this.nickName = nickName;
		this.gender = gender;
		this.tileUrl = tileUrl;
		this.isProvider = isProvider;
		this.lisence = lisence;
		this.mobile = mobile;
		this.email = email;
		this.birthday = birthday;
		this.isAuthentication = isAuthentication;
		this.fuZhi = fuZhi;
		this.location = location;
		this.description = description;
	}


	public ProfilePojo(int userId, String name, String nickName, int gender,
			String tileUrl, boolean isProvider, String lisence, String mobile,
			String email, String birthday, boolean isAuthentication,
			String fuZhi, String location, String description,
			List<License> licenses) {
		super();
		this.userId = userId;
		this.name = name;
		this.nickName = nickName;
		this.gender = gender;
		this.tileUrl = tileUrl;
		this.isProvider = isProvider;
		this.lisence = lisence;
		this.mobile = mobile;
		this.email = email;
		this.birthday = birthday;
		this.isAuthentication = isAuthentication;
		this.fuZhi = fuZhi;
		this.location = location;
		this.description = description;
		this.licenses = licenses;
	}

	public List<License> getLicenses() {
		return licenses;
	}

	public void setLicenses(List<License> licenses) {
		this.licenses = licenses;
	}

	public String getLocation() {
		return location;
	}

	public void setLocation(String location) {
		this.location = location;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getFuZhi() {
		return fuZhi;
	}

	public void setFuZhi(String fuZhi) {
		this.fuZhi = fuZhi;
	}

	public int getUserId() {
		return userId;
	}

	public void setUserId(int userId) {
		this.userId = userId;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getNickName() {
		return nickName;
	}

	public void setNickName(String nickName) {
		this.nickName = nickName;
	}

	public int getGender() {
		return gender;
	}

	public void setGender(int gender) {
		this.gender = gender;
	}

	public String getTileUrl() {
		return tileUrl;
	}

	public void setTileUrl(String tileUrl) {
		this.tileUrl = tileUrl;
	}

	public boolean getIsProvider() {
		return isProvider;
	}

	public void setIsProvider(boolean isProvider) {
		this.isProvider = isProvider;
	}

	public String getLisence() {
		return lisence;
	}

	public void setLisence(String lisence) {
		this.lisence = lisence;
	}

	public String getMobile() {
		return mobile;
	}

	public void setMobile(String mobile) {
		this.mobile = mobile;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getBirthday() {
		return birthday;
	}

	public void setBirthday(String birthday) {
		this.birthday = birthday;
	}

	public boolean getIsAuthentication() {
		return isAuthentication;
	}

	public void setIsAuthentication(boolean isAuthentication) {
		this.isAuthentication = isAuthentication;
	}



	

	
	
}
