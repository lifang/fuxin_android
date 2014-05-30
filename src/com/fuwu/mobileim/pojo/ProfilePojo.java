package com.fuwu.mobileim.pojo;

public class ProfilePojo {

	
    private int userId;//  用户id
    private String name;//  名称
    private String nickName;//  昵称
    private int gender ;//  性别
    private String tileUrl;//  头像
    private Boolean isProvider;//  福师
    private String lisence;//  行业认证
    private String mobile;//  手机号码
    private String email;//  邮箱
    private String birthday;//  生日
	
    
    public ProfilePojo() {
		super();
	}


	public ProfilePojo(int userId, String name, String nickName, int gender,
			String tileUrl, Boolean isProvider, String lisence,
			 String mobile,
			String email, String birthday) {
		super();
		this.userId = userId;
		this.name = name;
		this.nickName = nickName;
		this.gender = gender;
		this.tileUrl = tileUrl;
		this.isProvider = isProvider;// 福师
		this.lisence = lisence;
		this.mobile = mobile;
		this.email = email;
		this.birthday = birthday;
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


	public Boolean getIsProvider() {
		return isProvider;
	}


	public void setIsProvider(Boolean isProvider) {
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
    
    
    
    
}
