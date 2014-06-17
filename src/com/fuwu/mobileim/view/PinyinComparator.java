package com.fuwu.mobileim.view;

import java.util.Comparator;

import com.fuwu.mobileim.pojo.ShortContactPojo;

/**
 * @作者 丁作强
 * @时间 2014-5-24 下午1:18:47
 */
public class PinyinComparator implements Comparator<ShortContactPojo> {

	public int compare(ShortContactPojo o1, ShortContactPojo o2) {
		if (o1.getSortKey().equals("@") || o2.getSortKey().equals("#")) {
			return -1;
		} else if (o1.getSortKey().equals("#") || o2.getSortKey().equals("@")) {
			return 1;
		} else {
			return o1.getSortKey().compareTo(o2.getSortKey());
		}
	}

}
