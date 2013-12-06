package org.mrchen.commlib.helper;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

/**
 * @author Copyright (c) 2006 by BEA Systems.All Rights Reserved
 * 
 */
public final class DateHelper {

	public static final Date getDate(int year, int month, int day, int hour, int minute, int second) {
		Calendar cal = new GregorianCalendar(year, intToCalendarMonth(month), day, hour, minute, second);
		return cal.getTime();
	}

	public static final Date getDate(int year, int month, int day, int hour, int minute) {
		Calendar cal = new GregorianCalendar(year, intToCalendarMonth(month), day, hour, minute);
		return cal.getTime();
	}

	public static final Date getDate(int year, int month, int day) {
		Calendar cal = new GregorianCalendar(year, intToCalendarMonth(month), day);
		return cal.getTime();
	}

	/**
	 * 按周的方式获取日期
	 * 
	 * @param year
	 *            年份
	 * @param week
	 *            周
	 * @param dayOfWeek
	 *            一周内的第几天（周日为第一天）
	 * @return
	 */
	public static final Date getDateByWeek(int year, int week, int dayOfWeek) {
		return getDateByWeek(year, week, dayOfWeek, 0, 0, 0);
	}

	/**
	 * 按周的方式获取日期及时间
	 * 
	 * @param year
	 *            年份
	 * @param week
	 *            周
	 * @param dayOfWeek
	 *            一周内的第几天（周日为第一天,取值=1）
	 * @param hour
	 *            时
	 * @param minute
	 *            分
	 * @param second
	 *            秒
	 * @return
	 */
	public static final Date getDateByWeek(int year, int week, int dayOfWeek, int hour, int minute, int second) {
		Calendar cal = new GregorianCalendar();
		cal.setFirstDayOfWeek(Calendar.SUNDAY);

		cal.set(Calendar.YEAR, year);
		cal.set(Calendar.WEEK_OF_YEAR, week);
		cal.set(Calendar.DAY_OF_WEEK, dayOfWeek);
		cal.set(Calendar.HOUR_OF_DAY, hour);
		cal.set(Calendar.MINUTE, minute);
		cal.set(Calendar.SECOND, second);
		cal.set(Calendar.MILLISECOND, 0);

		return cal.getTime();
	}

	public static final Date getDate(String text) throws Exception {
		Date date = null;
		if (text != null && text.trim().length() > 0) {
			text = text.trim();
			text = text.replaceAll("\\.", "");
			text = text.replaceAll("/", "");
			text = text.replaceAll("-", "");
			text = text.replaceAll(",", "");
			text = text.replaceAll(" ", "");
			try {
				Integer.parseInt(text);
				if (text.length() == 8) {
					int day = Integer.parseInt(text.substring(0, 2));
					int month = Integer.parseInt(text.substring(2, 4));
					int year = Integer.parseInt(text.substring(4));
					date = getDate(year, month, day);
				} else {
					throw new NumberFormatException("Please enter a valid date.");
				}
			} catch (NumberFormatException nfe) {
				throw new NumberFormatException("Please enter a valid date.");
			}
		}
		return date;
	}

	public static Date getDate(String text, String format) throws ParseException {
		SimpleDateFormat sdf = new SimpleDateFormat(format);
		Date result = sdf.parse(text);

		return result;
	}

	public static String getDateFormatString(Date date, String format) {
		SimpleDateFormat formatter = new SimpleDateFormat(format);
		String result = formatter.format(date);

		return result;
	}

	/**
	 * 获取指定月份的最大天数
	 * 
	 * @param year
	 *            年份
	 * @param month
	 *            月份（1-12）
	 * @return
	 */
	public static int getDaysOfMonth(int year, int month) {
		Calendar cal = new GregorianCalendar();
		cal.set(year, intToCalendarMonth(month), 1);

		return cal.getActualMaximum(Calendar.DAY_OF_MONTH);
	}

	/**
	 * 获取指定年份最大的周数
	 * 
	 * @param year
	 * @return
	 */
	public static int getWeeksOfYear(int year) {
		Calendar cal = new GregorianCalendar();
		cal.set(year, 1, 1);

		return cal.getActualMaximum(Calendar.WEEK_OF_YEAR);
	}

	/**
	 * 取得当前日期所在周的第一天
	 * 
	 * @param date
	 * @return
	 */
	public static Date getDateOfWeekFirstDay(Date date) {
		Calendar cal = new GregorianCalendar();
		cal.setFirstDayOfWeek(Calendar.SUNDAY);
		cal.setTime(date);
		cal.set(Calendar.DAY_OF_WEEK, cal.getFirstDayOfWeek()); // Sunday
		return cal.getTime();
	}

	/**
	 * 取得当前日期所在周的最后一天
	 * 
	 * @param date
	 * @return
	 */
	public static Date getDateOfWeekLastDay(Date date) {
		Calendar cal = new GregorianCalendar();
		cal.setFirstDayOfWeek(Calendar.SUNDAY);
		cal.setTime(date);
		cal.set(Calendar.DAY_OF_WEEK, cal.getFirstDayOfWeek() + 6); // Saturday

		return cal.getTime();
	}

	public static String toString(Date date) {
		if (date == null) {
			return "";
		}
		return getDayAsString(date) + "." + getMonthNumberAsString(date) + "." + getYear(date);
	}

	/**
	 * @since 2006-9-5
	 * 
	 * @return 返回null或当前日期,格式为年-月-日。
	 */
	public static String toDateString() {
		Date date = getCurrent();
		if (date == null) {
			return "";
		}
		return getYear(date) + "-" + getMonthNumberAsString(date) + "-" + getDayAsString(date);
		// return
		// getDayAsString(date)+"."+getMonthNumberAsString(date)+"."+getYear(date);
	}

	/**
	 * @since 2006-9-5 15:29:30
	 * 
	 * @return 返回null或当前日期和确切时间,格式为年-月-日 时:分:秒。
	 */
	public static String toStringDetailed() {
		Date date = getCurrent();
		if (date == null) {
			return "";
		}
		return getYear(date) + "-" + getMonthNumberAsString(date) + "-" + getDayAsString(date) + " "
				+ getHourAsString(date) + ":" + getMinuteAsString(date) + ":" + getSecondAsString(date);
	}

	/**
	 * @since 2006-9-5 15:29:30
	 * 
	 * @return 返回null或当前日期和确切时间,格式为年-月-日 时:分:秒。
	 */
	public static String toStringNoFormat() {
		Date date = getCurrent();
		if (date == null) {
			return "";
		}
		return getYear(date) + getMonthNumberAsString(date) + getDayAsString(date) + "_" + getHourAsString(date)
				+ getMinuteAsString(date) + getSecondAsString(date);
	}

	public static String toStringNoFormat(Date date) {
		if (date == null) {
			return "";
		}
		return getYear(date) + getMonthNumberAsString(date) + getDayAsString(date) + "_" + getHourAsString(date)
				+ getMinuteAsString(date) + getSecondAsString(date);
	}

	public static String toStringSlashed() {
		Date date = getCurrent();
		if (date == null) {
			return "";
		}
		return getDayAsString(date) + "/" + getMonthNumberAsString(date) + "/" + getYear(date) + " " + getHour(date)
				+ ":" + getMinute(date) + ":" + getSecond(date);
	}

	public static String toStringNoFormatForCurrentDate() {
		Date date = getCurrent();
		return getYear(date) + getMonthNumberAsString(date) + getDayAsString(date) + getHourAsString(date)
				+ getMinuteAsString(date) + getSecondAsString(date);
	}

	public static String toStringDetailed(Date date) {
		if (date == null) {
			return "";
		}
		return getDayAsString(date) + "." + getMonthNumberAsString(date) + "." + getYear(date) + " "
				+ getHourAsString(date) + ":" + getMinuteAsString(date) + ":" + getSecondAsString(date);
	}

	public static String toStringSlashed(Date date) {
		if (date == null) {
			return "";
		}
		return getYear(date) + "-" + getMonthNumberAsString(date) + "-" + getDayAsString(date) + " "
				+ getHourAsString(date) + ":" + getMinuteAsString(date) + ":" + getSecondAsString(date);
	}

	public static final Date addSubstractDays(Date target, int days, boolean isAdd) {
		long msPerDay = 1000 * 60 * 60 * 24;
		long msTarget = target.getTime();
		long msSum = 0;
		if (isAdd) {
			msSum = msTarget + (msPerDay * days);
		} else {
			msSum = msTarget - (msPerDay * days);
		}
		Date result = new Date(msSum);
		return result;
	}

	public static final Date addDays(Date target, int days) {
		return addSubstractDays(target, days, true);
	}

	public static final Date substractDays(Date target, int days) {
		return addSubstractDays(target, days, false);
	}

	public static Date addSeconds(Date dt, long seconds) {
		return new Date(dt.getTime() + seconds * 1000);
	}

	public static int dayDiff(Date first, Date second) {
		long msPerDay = 1000 * 60 * 60 * 24;
		long diff = (first.getTime() / msPerDay) - (second.getTime() / msPerDay);
		Long convertLong = new Long(diff);
		return convertLong.intValue();
	}

	public static double hourDiff(Date first, Date second) {
		double msPerHour = 1000 * 60 * 60;
		double diff = ((double) first.getTime() / msPerHour) - ((double) second.getTime() / msPerHour);
		return diff;
	}

	public static String hourDiffAsString(Date first, Date second) {
		double diff = hourDiff(first, second);
		int hour = (int) diff;
		int minute = (int) ((diff - (double) hour) * 60);
		return hour + " saat " + minute + " dakika";
	}

	/**
	 * 获取两个时间中最大值
	 * 
	 * @param dt1
	 *            时间1
	 * @param dt2
	 *            时间2
	 * @return 最大值
	 */
	public static Date getMax(Date dt1, Date dt2) {
		if (dt1 == null || dt2 == null)
			return null;

		if (dt1.compareTo(dt2) >= 0)
			return dt1;
		else
			return dt2;
	}

	/**
	 * 获取两个时间中最小值
	 * 
	 * @param dt1
	 *            时间1
	 * @param dt2
	 *            时间2
	 * @return 最小值
	 */
	public static Date getMin(Date dt1, Date dt2) {
		if (dt1 == null || dt2 == null)
			return null;

		if (dt1.compareTo(dt2) <= 0)
			return dt1;
		else
			return dt2;
	}

	public static int getYear(Date date) {
		Calendar cal = new GregorianCalendar();
		cal.setTime(date);
		return cal.get(Calendar.YEAR);
	}

	public static int getMonth(Date date) {
		Calendar cal = new GregorianCalendar();
		cal.setTime(date);
		return calendarMonthToInt(cal.get(Calendar.MONTH));
	}

	public static int getWeek(Date date) {
		Calendar cal = new GregorianCalendar();
		cal.setTime(date);
		return cal.get(Calendar.WEEK_OF_YEAR);
	}

	/**
	 * 获取指定日期属于一周内的哪一天
	 * 
	 * @param date
	 *            日期
	 * @return 周内的第几天（周日为第一天，取值=1）
	 */
	public static int getDayOfWeek(Date date) {
		Calendar cal = new GregorianCalendar();
		cal.setFirstDayOfWeek(Calendar.SUNDAY);
		cal.setTime(date);
		return cal.get(Calendar.DAY_OF_WEEK);
	}

	public static String getMonthNumberAsString(Date date) {
		Calendar cal = new GregorianCalendar();
		cal.setTime(date);
		int month = calendarMonthToInt(cal.get(Calendar.MONTH));
		if (month < 10)
			return "0" + month;
		return "" + month;
	}

	public static String getMonthName(Date date) {
		String monthName = null;
		Calendar cal = new GregorianCalendar();
		cal.setTime(date);
		int month = calendarMonthToInt(cal.get(Calendar.MONTH));
		if (month == 1)
			monthName = "January";
		else if (month == 2)
			monthName = "February";
		else if (month == 3)
			monthName = "March";
		else if (month == 4)
			monthName = "April";
		else if (month == 5)
			monthName = "May";
		else if (month == 6)
			monthName = "June";
		else if (month == 7)
			monthName = "July";
		else if (month == 8)
			monthName = "August";
		else if (month == 9)
			monthName = "September";
		else if (month == 10)
			monthName = "October";
		else if (month == 11)
			monthName = "November";
		else if (month == 12)
			monthName = "December";
		return monthName;
	}

	public static int getDay(Date date) {
		Calendar cal = new GregorianCalendar();
		cal.setTime(date);
		return cal.get(Calendar.DAY_OF_MONTH);
	}

	static public String getDayAsString(Date date) {
		Calendar cal = new GregorianCalendar();
		cal.setTime(date);
		int day = cal.get(Calendar.DAY_OF_MONTH);
		if (day < 10)
			return "0" + day;
		return "" + day;
	}

	public static int getHour(Date date) {
		Calendar cal = new GregorianCalendar();
		cal.setTime(date);
		return cal.get(Calendar.HOUR_OF_DAY);
	}

	public static String getHourAsString(Date date) {
		Calendar cal = new GregorianCalendar();
		cal.setTime(date);
		int hour = cal.get(Calendar.HOUR_OF_DAY);
		if (hour < 10) {
			return "0" + hour;
		}
		return "" + hour;
	}

	public static int getMinute(Date date) {
		Calendar cal = new GregorianCalendar();
		cal.setTime(date);
		return cal.get(Calendar.MINUTE);
	}

	public static String getMinuteAsString(Date date) {
		Calendar cal = new GregorianCalendar();
		cal.setTime(date);
		int minute = cal.get(Calendar.MINUTE);
		if (minute < 10) {
			return "0" + minute;
		}
		return "" + minute;
	}

	public static int getSecond(Date date) {
		Calendar cal = new GregorianCalendar();
		cal.setTime(date);
		return cal.get(Calendar.SECOND);
	}

	public static String getSecondAsString(Date date) {
		Calendar cal = new GregorianCalendar();
		cal.setTime(date);
		int second = cal.get(Calendar.SECOND);
		if (second < 10) {
			return "0" + second;
		}
		return "" + second;
	}

	public static int getMillisecond(Date date) {
		Calendar cal = new GregorianCalendar();
		cal.setTime(date);
		return cal.get(Calendar.MILLISECOND);
	}

	private static int calendarMonthToInt(int calendarMonth) {
		if (calendarMonth == Calendar.JANUARY)
			return 1;
		else if (calendarMonth == Calendar.FEBRUARY)
			return 2;
		else if (calendarMonth == Calendar.MARCH)
			return 3;
		else if (calendarMonth == Calendar.APRIL)
			return 4;
		else if (calendarMonth == Calendar.MAY)
			return 5;
		else if (calendarMonth == Calendar.JUNE)
			return 6;
		else if (calendarMonth == Calendar.JULY)
			return 7;
		else if (calendarMonth == Calendar.AUGUST)
			return 8;
		else if (calendarMonth == Calendar.SEPTEMBER)
			return 9;
		else if (calendarMonth == Calendar.OCTOBER)
			return 10;
		else if (calendarMonth == Calendar.NOVEMBER)
			return 11;
		else if (calendarMonth == Calendar.DECEMBER)
			return 12;
		else
			return 1;
	}

	public static String format(Date date, String pattern) {
		SimpleDateFormat formatter = new SimpleDateFormat(pattern);
		return formatter.format(date);
	}

	private static int intToCalendarMonth(int month) {
		if (month == 1)
			return Calendar.JANUARY;
		else if (month == 2)
			return Calendar.FEBRUARY;
		else if (month == 3)
			return Calendar.MARCH;
		else if (month == 4)
			return Calendar.APRIL;
		else if (month == 5)
			return Calendar.MAY;
		else if (month == 6)
			return Calendar.JUNE;
		else if (month == 7)
			return Calendar.JULY;
		else if (month == 8)
			return Calendar.AUGUST;
		else if (month == 9)
			return Calendar.SEPTEMBER;
		else if (month == 10)
			return Calendar.OCTOBER;
		else if (month == 11)
			return Calendar.NOVEMBER;
		else if (month == 12)
			return Calendar.DECEMBER;
		else
			return Calendar.JANUARY;
	}

	public static java.sql.Date getSqlDate(Date utilDate) {
		if (utilDate == null) {
			return null;
		}
		return new java.sql.Date(utilDate.getTime());
	}

	public static Date getDate(java.sql.Date sqlDate) {
		if (sqlDate == null) {
			return null;
		}
		return new java.util.Date(sqlDate.getTime());
	}

	/**
	 * 获取当前时间的方法
	 * 
	 * @return
	 */
	public static Date getCurrent() {
		return getCurrent(true);
	}

	/**
	 * 获取当前时间的方法
	 * 
	 * @param useMilliseconds
	 *            是否有毫秒值
	 * @return 当前时间
	 */
	public static Date getCurrent(boolean useMilliseconds) {
		Calendar cal = new GregorianCalendar();
		cal.setTime(new Date(System.currentTimeMillis()));

		if (!useMilliseconds) // 忽略毫秒
			cal.set(Calendar.MILLISECOND, 0);

		return cal.getTime();
	}

	public static boolean isInSameDay(Date first, Date second) {
		boolean flag = false;
		Calendar firstCal = new GregorianCalendar();
		Calendar secondCal = new GregorianCalendar();
		firstCal.setTime(first);
		secondCal.setTime(second);

		if (firstCal.get(Calendar.YEAR) == secondCal.get(Calendar.YEAR)
				&& firstCal.get(Calendar.MONTH) == secondCal.get(Calendar.MONTH)
				&& firstCal.get(Calendar.DAY_OF_MONTH) == secondCal.get(Calendar.DAY_OF_MONTH)) {
			flag = true;
		}
		return flag;
	}

	public static boolean isInSameDayOrAfter(Date first, Date second) {
		boolean flag = false;
		Calendar firstCal = new GregorianCalendar();
		Calendar secondCal = new GregorianCalendar();
		firstCal.setTime(first);
		secondCal.setTime(second);

		if (first.after(second)) {
			flag = true;
		} else if (firstCal.get(Calendar.YEAR) == secondCal.get(Calendar.YEAR)
				&& firstCal.get(Calendar.MONTH) == secondCal.get(Calendar.MONTH)
				&& firstCal.get(Calendar.DAY_OF_MONTH) == secondCal.get(Calendar.DAY_OF_MONTH)) {
			flag = true;
		}
		return flag;
	}

	public static boolean isMonday(Date date) {
		Calendar cal = new GregorianCalendar();
		cal.setTime(date);
		return cal.get(Calendar.DAY_OF_WEEK) == Calendar.MONDAY;
	}

	public static boolean isTuesday(Date date) {
		Calendar cal = new GregorianCalendar();
		cal.setTime(date);
		return cal.get(Calendar.DAY_OF_WEEK) == Calendar.TUESDAY;
	}

	public static boolean isWednesday(Date date) {
		Calendar cal = new GregorianCalendar();
		cal.setTime(date);
		return cal.get(Calendar.DAY_OF_WEEK) == Calendar.WEDNESDAY;
	}

	public static boolean isThursday(Date date) {
		Calendar cal = new GregorianCalendar();
		cal.setTime(date);
		return cal.get(Calendar.DAY_OF_WEEK) == Calendar.THURSDAY;
	}

	public static boolean isFriday(Date date) {
		Calendar cal = new GregorianCalendar();
		cal.setTime(date);
		return cal.get(Calendar.DAY_OF_WEEK) == Calendar.FRIDAY;
	}

	public static boolean isSaturday(Date date) {
		Calendar cal = new GregorianCalendar();
		cal.setTime(date);
		return cal.get(Calendar.DAY_OF_WEEK) == Calendar.SATURDAY;
	}

	public static boolean isSunday(Date date) {
		Calendar cal = new GregorianCalendar();
		cal.setTime(date);
		return cal.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY;
	}

	public static int getCurrentYear() {
		return getYear(getCurrent());
	}

	public static int getCurrentMonth() {
		return getMonth(getCurrent());
	}

	public static String getCurrentMonthNumberAsString() {
		int month = getCurrentMonth();
		if (month < 10) {
			return "0" + month;
		}
		return "" + month;
	}

	public static String getCurrentMonthName() {
		return getMonthName(getCurrent());
	}

	public static int getCurrentDay() {
		return getDay(getCurrent());
	}

	public static String getCurrentDayNumberAsString() {
		int day = getCurrentDay();
		if (day < 10) {
			return "0" + day;
		}
		return "" + day;
	}

	/**
	 * 计算两个时间的间隔天数
	 * 
	 * @param startday
	 * @param endday
	 * @return
	 */
	public static int getIntervalDays(Date startday, Date endday) {
		if (startday.after(endday)) {
			Date cal = startday;
			startday = endday;
			endday = cal;
		}
		long sl = startday.getTime();
		long el = endday.getTime();
		long ei = el - sl;
		return (int) (ei / (1000 * 60 * 60 * 24));
	}

	/**
	 * 得到你想要当前日期的格式
	 */
	public static String getCurrentDateFormat(String dateFormat) {
		Date date = new Date();
		SimpleDateFormat formatter = new SimpleDateFormat(dateFormat);
		return formatter.format(date);
	}
}
