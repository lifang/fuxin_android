package com.fuwu.mobileim.util;

import java.util.Comparator;

import com.fuwu.mobileim.pojo.MessagePojo;

/**
 * @作者 丁作强
 * @时间 2014-5-24 下午1:18:47
 */
public class SendDataComparator implements Comparator<MessagePojo> {

	public int compare(MessagePojo o1, MessagePojo o2) {
		
			return (TimeUtil.getLongTime(o1.getSendTime())+"").compareTo((TimeUtil.getLongTime(o2.getSendTime())+""));
	}

}
