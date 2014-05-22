package com.zhishi.fuxun.pojo;

/**
 * 联系人实体类
 * @作者 丁作强
 * @时间 2014-5-22 下午4:57:16
 */
public class Contact {
//	联系人表: 联系人ID ,昵称 ,头像 ,性别 ,身份, 联系人对用户的关系(订购者/聊天者/关注者) 
	/**
	 * 联系人姓名
	 */
	private String name;

	/**
	 * 排序字母
	 */
	private String sortKey;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getSortKey() {
		return sortKey;
	}

	public void setSortKey(String sortKey) {
		this.sortKey = sortKey;
	}

}
