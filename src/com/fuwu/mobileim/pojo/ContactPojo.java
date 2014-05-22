package com.fuwu.mobileim.pojo;

/**
 * @作者 马龙
 * @时间 创建时间：2014-5-14 下午2:17:56
 */
public class ContactPojo {

	private String name;
	private String date;
	private String lastMes;
	private String img;
	private int type;

	public ContactPojo() {
	}

	public ContactPojo(String name, String date, String lastMes, String img,
			int type) {
		super();
		this.name = name;
		this.date = date;
		this.lastMes = lastMes;
		this.img = img;
		this.type = type;
	}

	public String getImg() {
		return img;
	}

	public void setImg(String img) {
		this.img = img;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDate() {
		return date;
	}

	public void setDate(String date) {
		this.date = date;
	}

	public String getLastMes() {
		return lastMes;
	}

	public void setLastMes(String lastMes) {
		this.lastMes = lastMes;
	}

	public int getOrder() {
		return type;
	}

	public void setOrder(int type) {
		this.type = type;
	}
}
