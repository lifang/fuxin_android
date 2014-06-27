package com.fuwu.mobileim.util;

import java.util.Comparator;

import com.fuwu.mobileim.pojo.ShortContactPojo;

/**
 * @作者 丁作强
 * @时间 2014-5-24 下午1:18:47
 */
public class SubscribeTimeLongDataComparator implements Comparator<ShortContactPojo> {

	public int compare(ShortContactPojo o1, ShortContactPojo o2) {
		
			return (TimeUtil.getLongTime(o2.getSubscribeTime())+"").compareTo((TimeUtil.getLongTime(o1.getSubscribeTime())+""));
	}

}
