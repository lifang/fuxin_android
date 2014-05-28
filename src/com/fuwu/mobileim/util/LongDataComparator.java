package com.fuwu.mobileim.util;

import java.util.Comparator;

import com.fuwu.mobileim.pojo.ContactPojo;

/**
 * @作者 丁作强
 * @时间 2014-5-24 下午1:18:47
 */
public class LongDataComparator implements Comparator<ContactPojo> {

	public int compare(ContactPojo o1, ContactPojo o2) {
		
			return (TimeUtil.getLongTime(o2.getLastContactTime())+"").compareTo((TimeUtil.getLongTime(o1.getLastContactTime())+""));
	}

}
