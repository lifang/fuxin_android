package com.fuwu.mobileim.pojo;

/**
 * @作者 马龙
 * @时间 创建时间：2014-5-14 下午2:17:56
 */
public class ContactPojo {

	private int contactId;  // id
	private String sortKey;  // 显示数据拼音的首字母
	private String name; // 昵称
	private String customName;  // 备注
	private String userface_url;  // 头像路径
	private int source;  //  是否交易 是否订阅
	private String lastContactTime; //最近联系时间
	

	public ContactPojo() {
	}


	public ContactPojo(int contacId, String sortKey, String name,
			String custom_name, String userface_url, int source,
			String lastContactTime) {
		super();
		this.contactId = contacId;
		this.sortKey = sortKey;
		this.name = name;
		this.customName = custom_name;
		this.userface_url = userface_url;
		this.source = source;
		this.lastContactTime = lastContactTime;
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
