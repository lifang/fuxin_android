package com.fuwu.mobileim.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * @作者 马龙
 * @时间 2014-5-26 上午9:57:21
 */
public class TimeUtil {
	static String[] weekDays = { "星期日", "星期一", "星期二", "星期三", "星期四", "星期五",
			"星期六" };

	public static String getHourAndMin(long time) {
		SimpleDateFormat format = new SimpleDateFormat("HH:mm");
		return format.format(new Date(time));
	}
	public static String getYear(long time) {
		SimpleDateFormat format = new SimpleDateFormat("yyyy年");
		String md = format.format(new Date(time));
		return md;
	}
	public static String getMonth(long time) {
		SimpleDateFormat format = new SimpleDateFormat("MM月");
		String md = format.format(new Date(time));
		if ((md.charAt(0)+"").endsWith("0")) {
			md = md.substring(1, md.length());
		}
		return md;
	}
	public static String getDay(long time) {
		SimpleDateFormat format = new SimpleDateFormat("dd日");
		String md = format.format(new Date(time));
		if ((md.charAt(0)+"").endsWith("0")) {
			md = md.substring(1, md.length());
		}
		return md;
	}
	public static String getYearMonthDay(long time) {//yyyy年MM月dd日
		return getYear(time)+getMonth(time)+getDay(time);
	}
	public static String getMonthDay(long time) {//MM月dd日
		return getMonth(time)+getDay(time);
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

	public static String getWeekOfDate(long time, long todaytime) {
		Date dt = new Date(time);
		Calendar cal = Calendar.getInstance();
		cal.setTime(dt);

		int w = cal.get(Calendar.DAY_OF_WEEK) - 1;
		if (w < 0)
			w = 0;
		Date dt2 = new Date(todaytime);
		cal.setTime(dt2);

		int w2 = cal.get(Calendar.DAY_OF_WEEK) - 1;
		if (w2 < 0)
			w2 = 0;

		if (w >= w2) {
			return getMonthDay(time);
		} else {
			return weekDays[w];
		}

	}

	public static String getChatTime(String date) {
		String result = "";
		try {
			SimpleDateFormat format = new SimpleDateFormat(
					"yyyy-MM-dd HH:mm:ss");
			Date today = new Date(System.currentTimeMillis());
			Date sendDay = format.parse(date);
			int temp = (int) ((today.getTime() - sendDay.getTime()) / 86400000);

			if (sendDay.getTime() <= 0) {
				return "未知";
			}
			if (temp <= 1) {
				SimpleDateFormat sdf = new SimpleDateFormat("dd");
				int day = Integer.parseInt(sdf.format(today))
						- Integer.parseInt(sdf.format(sendDay));
				if (day == 1) {
					return "昨天" + " " + getHourAndMin(sendDay.getTime());
				} else if (day == 2) {
					return getWeekOfDate(sendDay.getTime()) + " "
							+ getHourAndMin(sendDay.getTime());
				}
			}
			if (temp > 7) {
				result = date;
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

	public static String getTalkTime(String date) {
		String result = "";
		try {
			SimpleDateFormat format = new SimpleDateFormat(
					"yyyy-MM-dd HH:mm:ss");
			Date today = new Date(System.currentTimeMillis());
			Date sendDay = format.parse(date);
			int temp = (int) ((today.getTime() - sendDay.getTime()) / 86400000);

			if (sendDay.getTime() <= 0) {
				return "未知";
			}
			SimpleDateFormat sdf_year = new SimpleDateFormat("yyyy");
			int year = Integer.parseInt(sdf_year.format(today))
					- Integer.parseInt(sdf_year.format(sendDay));

			if (year == 0) {

				if (temp <= 1) {
					SimpleDateFormat sdf = new SimpleDateFormat("dd");
					int day = Integer.parseInt(sdf.format(today))
							- Integer.parseInt(sdf.format(sendDay));
					if (day == 1) {
						return "昨天 " + getHourAndMin(sendDay.getTime());
					} else if (day == 2) {
						return getWeekOfDate(sendDay.getTime())+ " "
								+ getHourAndMin(sendDay.getTime());
					} else if (day == 0) {
						return getTodayTime(sendDay.getTime()) + " "
								+ getHourAndMin(sendDay.getTime());
					}
				}
				if (temp > 7) {
					result = getMonthDay(sendDay.getTime())+ " " + getHourAndMin(sendDay.getTime());
				} else if (temp > 1) {
					result = getWeekOfDate(sendDay.getTime(), today.getTime())
							+ " " + getHourAndMin(sendDay.getTime());
				}

			}else {
				result = getYearMonthDay(sendDay.getTime());	
			}

		} catch (ParseException e) {
		}
		return result;
	}

	public static String getCurrentTime() {
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		return format.format(new Date(System.currentTimeMillis()));
	}

	public static long getLongTime(String time) {
		if (time == null || time.equals("") || time.equals("null")) {
			return 0;
		}
		try {
			SimpleDateFormat format = new SimpleDateFormat(
					"yyyy-MM-dd HH:mm:ss");
			Date sendDay = format.parse(time);
			return sendDay.getTime();
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return 0;
	}

	public static boolean isFiveMin(String date, String sendTime) {
		try {
			if (date == null || date.equals("")) {
				return true;
			}
			SimpleDateFormat format = new SimpleDateFormat(
					"yyyy-MM-dd HH:mm:ss");
			Date sendDay = format.parse(date);
			Date sd = format.parse(sendTime);
			long time = sendDay.getTime();
			if (sd.getTime() - time >= 300000) {
				return true;
			}
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return false;
	}
}
