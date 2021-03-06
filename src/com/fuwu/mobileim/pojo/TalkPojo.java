package com.fuwu.mobileim.pojo;

/**
 * @作者 马龙
 * @时间 创建时间：2014-5-26 下午3:02:43
 */
public class TalkPojo {

	private int user_id;
	private int contact_id;
	private String nick_name;
	private String head_pic;
	private String content;
	private String time;
	private int mes_count;

	public TalkPojo() {
	}

	public TalkPojo(int user_id, int contact_id, String nick_name,
			String head_pic, String content, String time, int mes_count) {
		super();
		this.user_id = user_id;
		this.contact_id = contact_id;
		this.nick_name = nick_name;
		this.head_pic = head_pic;
		this.content = content;
		this.time = time;
		this.mes_count = mes_count;
	}

	public int getUser_id() {
		return user_id;
	}

	public void setUser_id(int user_id) {
		this.user_id = user_id;
	}

	public int getContact_id() {
		return contact_id;
	}

	public void setContact_id(int contact_id) {
		this.contact_id = contact_id;
	}

	public String getNick_name() {
		return nick_name;
	}

	public void setNick_name(String nick_name) {
		this.nick_name = nick_name;
	}

	public String getHead_pic() {
		return head_pic;
	}

	public void setHead_pic(String head_pic) {
		this.head_pic = head_pic;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public String getTime() {
		return time;
	}

	public void setTime(String time) {
		this.time = time;
	}

	public int getMes_count() {
		return mes_count;
	}

	public void setMes_count(int mes_count) {
		this.mes_count = mes_count;
	}

	@Override
	public String toString() {
		return "TalkPojo [user_id=" + user_id + ", contact_id=" + contact_id
				+ ", nick_name=" + nick_name + ", head_pic=" + head_pic
				+ ", content=" + content + ", time=" + time + ", mes_count="
				+ mes_count + "]";
	}

}
