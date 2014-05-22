package com.comdosoft.fuxun.util;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/** 
* @作者 马龙 
* @时间 2014-5-16 下午2:00:41 
*/ 
public class TimeUtil {
	static String[] weekDays = { "星期日", "星期一", "星期二", "星期三", "星期四", "星期五",
			"星期六" };

	public static String getTime(long time) {
		SimpleDateFormat format = new SimpleDateFormat("yy-MM-dd HH:mm");
		return format.format(new Date(time));
	}

	public static String getHourAndMin(long time) {
		SimpleDateFormat format = new SimpleDateFormat("HH:mm");
		return format.format(new Date(time));
	}

	public static String getTodayTime(long time) {
		SimpleDateFormat sdf = new SimpleDateFormat("HH");
		Date date = new Date(time);
		int hour = Integer.parseInt(sdf.format(date));
		if (hour >= 18) {
			return "晚上";
		} else if (hour >= 12) {
			return "下午";
		} else if (hour >= 6) {
			return "上午";
		} else {
			return "凌晨";
		}
	}

	public static String getWeekOfDate(long time) {
		Date dt = new Date(time);
		Calendar cal = Calendar.getInstance();
		cal.setTime(dt);

		int w = cal.get(Calendar.DAY_OF_WEEK) - 1;
		if (w < 0)
			w = 0;

		return weekDays[w];
	}

	public static String getChatTime(long timesamp) {
		String result = "";
		SimpleDateFormat sdf = new SimpleDateFormat("dd");
		Date today = new Date(System.currentTimeMillis());
		Date otherDay = new Date(timesamp);
		int temp = Integer.parseInt(sdf.format(today))
				- Integer.parseInt(sdf.format(otherDay));

		if (temp > 7) {
			result = getTime(timesamp);
		} else if (temp > 1) {
			result = getWeekOfDate(timesamp) + " " + getHourAndMin(timesamp);
		} else if (temp == 1) {
			result = "昨天" + " " + getHourAndMin(timesamp);
		} else {
			result = getTodayTime(timesamp) + " " + getHourAndMin(timesamp);
		}
		return result;
	}
}
