package com.fuwu.mobileim.pojo;


/**
 * @作者 丁作强
 * @时间 2014-5-28 上午10:07:55
 */
public class ContactPojo {

	private int contactId;  // id
	private String sortKey;  // 显示数据拼音的首字母
	private String name; // 昵称
	private String customName;  // 备注
	private String userface_url;  // 头像路径
	private int sex;  //  性别
	private int source;  //  是否交易 是否订阅
	private String lastContactTime; //最近联系时间
	private Boolean isBlocked; //是否屏蔽
	

	public ContactPojo() {
	}



	public ContactPojo(int contactId, String sortKey, String name,
			String customName, String userface_url, int sex, int source,
			String lastContactTime, Boolean isBlocked) {
		super();
		this.contactId = contactId;
		this.sortKey = sortKey;
		this.name = name;
		this.customName = customName;
		this.userface_url = userface_url;
		this.sex = sex;
		this.source = source;
		this.lastContactTime = lastContactTime;
		this.isBlocked = isBlocked;
	}





	public int getSex() {
		return sex;
	}


	public void setSex(int sex) {
		this.sex = sex;
	}


	public Boolean getIsBlocked() {
		return isBlocked;
	}


	public void setIsBlocked(Boolean isBlocked) {
		this.isBlocked = isBlocked;
	}


	public int getContactId() {
		return contactId;
	}


	public void setContactId(int contactId) {
		this.contactId = contactId;
	}


	public String getSortKey() {
		return sortKey;
	}


	public void setSortKey(String sortKey) {
		this.sortKey = sortKey;
	}


	public String getName() {
		return name;
	}


	public void setName(String name) {
		this.name = name;
	}


	public String getCustomName() {
		return customName;
	}


	public void setCustomName(String custom_name) {
		this.customName = custom_name;
	}


	public String getUserface_url() {
		return userface_url;
	}


	public void setUserface_url(String userface_url) {
		this.userface_url = userface_url;
	}


	public int getSource() {
		return source;
	}


	public void setSource(int source) {
		this.source = source;
	}


	public String getLastContactTime() {
		return lastContactTime;
	}


	public void setLastContactTime(String lastContactTime) {
		this.lastContactTime = lastContactTime;
	}



}
