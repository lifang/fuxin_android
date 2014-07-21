package com.fuwu.mobileim.pojo;

import java.util.List;

import com.fuwu.mobileim.model.Models.License;


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
	private int isBlocked; //是否屏蔽   1 --屏蔽    0--没屏蔽
	private int isProvider; //是不是 福师   1 --是    0--不是
	
    private String lisence; // 认证
    private String individualResume ; //  个人简介
    private String fuzhi;
    private String location;
    private List<License> licenses;
	public ContactPojo() {
	}
	
	
	public ContactPojo(int contactId, String sortKey, String name,
			String customName, String userface_url, int sex, int source,
			String lastContactTime, int isBlocked, int isProvider,
			String lisence, String individualResume) {
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
		this.isProvider = isProvider;
		this.lisence = lisence;
		this.individualResume = individualResume;
	}
	

	public ContactPojo(int contactId, String sortKey, String name,
			String customName, String userface_url, int sex, int source,
			String lastContactTime, int isBlocked, int isProvider,
			String lisence, String individualResume, String fuzhi,
			String location) {
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
		this.isProvider = isProvider;
		this.lisence = lisence;
		this.individualResume = individualResume;
		this.fuzhi = fuzhi;
		this.location = location;
	}


	public ContactPojo(int contactId, String sortKey, String name,
			String customName, String userface_url, int sex, int source,
			String lastContactTime, int isBlocked, int isProvider,
			String lisence, String individualResume, String fuzhi,
			String location, List<License> licenses) {
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
		this.isProvider = isProvider;
		this.lisence = lisence;
		this.individualResume = individualResume;
		this.fuzhi = fuzhi;
		this.location = location;
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


	public String getFuzhi() {
		return fuzhi;
	}

	public void setFuzhi(String fuzhi) {
		this.fuzhi = fuzhi;
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
	public void setCustomName(String customName) {
		this.customName = customName;
	}
	public String getUserface_url() {
		return userface_url;
	}
	public void setUserface_url(String userface_url) {
		this.userface_url = userface_url;
	}
	public int getSex() {
		return sex;
	}
	public void setSex(int sex) {
		this.sex = sex;
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
	public int getIsBlocked() {
		return isBlocked;
	}
	public void setIsBlocked(int isBlocked) {
		this.isBlocked = isBlocked;
	}
	public int getIsProvider() {
		return isProvider;
	}
	public void setIsProvider(int isProvider) {
		this.isProvider = isProvider;
	}
	public String getLisence() {
		return lisence;
	}
	public void setLisence(String lisence) {
		this.lisence = lisence;
	}
	public String getIndividualResume() {
		return individualResume;
	}
	public void setIndividualResume(String individualResume) {
		this.individualResume = individualResume;
	}

	@Override
	public String toString() {
		return "ContactPojo [contactId=" + contactId + ", sortKey=" + sortKey
				+ ", name=" + name + ", customName=" + customName
				+ ", userface_url=" + userface_url + ", sex=" + sex
				+ ", source=" + source + ", lastContactTime=" + lastContactTime
				+ ", isBlocked=" + isBlocked + ", isProvider=" + isProvider
				+ ", lisence=" + lisence + ", individualResume="
				+ individualResume + ", fuzhi=" + fuzhi + "]";
	}
	
}
