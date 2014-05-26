package com.fuwu.mobileim.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * @作者 马龙
<<<<<<< HEAD
 * @时间 2014-5-16 下午2:00:41
=======
 * @时间 2014-5-26 上午9:57:21
>>>>>>> c39595f8fb5b0bca0474307094b7b9a7f5b5aedc
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

	public static String getChatTime(String date) {
		String result = "";
		try {
			SimpleDateFormat format = new SimpleDateFormat("yy-MM-dd HH:mm");
			SimpleDateFormat sdf = new SimpleDateFormat("dd");
			Date today = new Date(System.currentTimeMillis());
			Date sendDay = format.parse(date);
			int temp = Integer.parseInt(sdf.format(today))
					- Integer.parseInt(sdf.format(sendDay));

			if (temp > 7) {
				result = getTime(sendDay.getTime());
			} else if (temp > 1) {
				result = getWeekOfDate(sendDay.getTime()) + " "
						+ getHourAndMin(sendDay.getTime());
			} else if (temp == 1) {
				result = "昨天" + " " + getHourAndMin(sendDay.getTime());
			} else {
				result = getTodayTime(sendDay.getTime()) + " "
						+ getHourAndMin(sendDay.getTime());
			}

		} catch (ParseException e) {
		}
		return result;
	}

	public static boolean isFiveMin(String date) {
		try {
			SimpleDateFormat format = new SimpleDateFormat("yy-MM-dd HH:mm");
			Date sendDay = format.parse(date);
			long time = sendDay.getTime();
			if (System.currentTimeMillis() - time >= 300000) {
				return true;
			}
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return false;
	}
}
