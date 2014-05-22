package com.fuwu.mobileim.util;

import android.annotation.SuppressLint;
import java.util.HashMap;

/**
 * @作者 马龙
 * @时间 创建时间：2014-5-21 下午3:06:54
 */
@SuppressLint("UseSparseArrays")
public class LastTimeMap {
	private static HashMap<Integer, String> mTimeMap = new HashMap<Integer, String>();

	public static void addLastTime(int id, long time) {
		mTimeMap.put(id, String.valueOf(time));
	}

	public static long getLastTime(int id) {
		return Long.parseLong(mTimeMap.get(id));
	}

	public static boolean isFiveMin(int id) {
		long time = LastTimeMap.getLastTime(id);
		if (System.currentTimeMillis() - time >= 300000) {
			return true;
		}
		return false;
	}

}
