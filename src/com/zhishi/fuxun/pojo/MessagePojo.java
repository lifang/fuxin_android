package com.zhishi.fuxun.pojo;

/**
 * @作者 马龙
 * @时间 创建时间：2014-5-16 上午10:49:45
 */
public class MessagePojo {
	// Text
	public static final int MESSAGE_TYPE_TEXT = 1;
	// image
	public static final int MESSAGE_TYPE_IMG = 2;
	// file
	public static final int MESSAGE_TYPE_FILE = 3;

	private int msgType;
	private long time;// 消息日期
	private String message;// 消息内容
	private String img;
	private int isComMeg = 0;// 0接收/1发送 消息
	private int isTimeShow = 0; // 0显示/1不显示 时间

	public MessagePojo(int msgType, long time, String message, String img,
			int isComMeg, int isTimeShow) {
		super();
		this.msgType = msgType;
		this.time = time;
		this.message = message;
		this.img = img;
		this.isComMeg = isComMeg;
		this.isTimeShow = isTimeShow;
	}

	public int getIsTimeShow() {
		return isTimeShow;
	}

	public void setIsTimeShow(int isTimeShow) {
		this.isTimeShow = isTimeShow;
	}

	public int getMsgType() {
		return msgType;
	}

	public void setMsgType(int msgType) {
		this.msgType = msgType;
	}

	public long getTime() {
		return time;
	}

	public void setTime(long time) {
		this.time = time;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public String getImg() {
		return img;
	}

	public void setImg(String headImg) {
		this.img = headImg;
	}

	public int isComMeg() {
		return isComMeg;
	}

	public void setComMeg(int isComMeg) {
		this.isComMeg = isComMeg;
	}

}
