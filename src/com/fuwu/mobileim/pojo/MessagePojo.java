package com.fuwu.mobileim.pojo;

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

	private int userId;
	private String sendTime;// 消息日期
	private String content;// 消息内容
	private int isComMeg = 0;// 0接收/1发送 消息
	private int msgType;

	public MessagePojo() {
	}

	public MessagePojo(int userId, String sendTime, String content, int isComMeg,
			int msgType) {
		super();
		this.userId = userId;
		this.sendTime = sendTime;
		this.content = content;
		this.isComMeg = isComMeg;
		this.msgType = msgType;
	}

	public int getUserId() {
		return userId;
	}

	public void setUserId(int userId) {
		this.userId = userId;
	}

	public int getMsgType() {
		return msgType;
	}

	public void setMsgType(int msgType) {
		this.msgType = msgType;
	}

	public String getSendTime() {
		return sendTime;
	}

	public void setSendTime(String sendTime) {
		this.sendTime = sendTime;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public int getIsComMeg() {
		return isComMeg;
	}

	public void setIsComMeg(int isComMeg) {
		this.isComMeg = isComMeg;
	}

}
