package com.shawn.dubbo.utils;

import org.springframework.util.StringUtils;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * 
 * @描述：时间辅助类
 * @日期：2015年2月6日 下午6:33:21
 * @开发人员： MR.X
 */
public final class DateUtils {
	private static final String FORMAT = "yyyy-MM-dd HH:mm:ss";
	private static final String FORMAT_DATEONLYNOSP = "yyyyMMdd"; // 年/月/日
	private static final String DATE_FORMAT_DATEONLY = "yyyy-MM-dd";
	public static final long DAY_MILLI = 24 * 60 * 60 * 1000; // 一天的MilliSecond

	public static String DateToStr(Date date) {
		if (date == null) {
			return null;
		}
		return DateToStr(date, FORMAT);
	}

	/**
	 * 日期转指定格式字符串
	 * 
	 * @param date
	 * @param format
	 * @return
	 */
	public static String DateToStr(Date date, String format) {
		if (date == null) {
			return null;
		}
		if (StringUtils.isEmpty(format)) {
			return null;
		}
		String sRet = null;
		try {
			SimpleDateFormat formatter = new SimpleDateFormat(format);
			sRet = formatter.format(date).toString();
		} catch (Exception ex) {
			sRet = null;
		}
		return sRet;
	}

	/**
	 * 接收形如2000-02-01 01:02:03的时间两个参数转化为long型值后进行比较
	 * 
	 * @param smallTamp
	 * @param bigTamp
	 * @return
	 */
	public static long compareTimestamp(String smallTamp, String bigTamp) {
		if (!StringUtils.isEmpty(smallTamp) && !StringUtils.isEmpty(bigTamp)) {
			try {
				Timestamp t = parseTimestamp(smallTamp, true);
				Timestamp t2 = parseTimestamp(bigTamp, true);
				if (t != null && t2 != null) {
					return t2.getTime() - t.getTime();
				}
			} catch (Exception ex) {
			}
		} else {
			return 0;
		}
		return 0;
	}

	/**
	 * 获取Tiestamp<br/>
	 * 输入：2002-02-02 或者 2002-02-02 00:00:00
	 * 
	 * @param timestamp
	 *            时间格式或者日期格式
	 */
	public static Timestamp parseTimestamp(String timestamp, boolean bStart) {
		if (bStart) {
			return parseTimestamp(timestamp);
		}
		try {
			return Timestamp.valueOf(timestamp);
		} catch (Exception e) {
			try {
				return Timestamp.valueOf(timestamp.trim() + " 23:59:59");
			} catch (Exception ee) {
			}
		}

		return null;
	}

	/**
	 * 根据时间戳字符串获取Tiestamp对象<br/>
	 * 输入：2002-02-02 或者 2002-02-02 00:00:00
	 * 
	 * @param timestamp
	 *            时间格式或者日期格式
	 * @return Timestamp对象
	 */
	public static Timestamp parseTimestamp(String timestamp) {
		try {
			return Timestamp.valueOf(timestamp);
		} catch (Exception e) {
			try {
				return Timestamp.valueOf(timestamp.trim() + " 00:00:00");
			} catch (Exception ee) {
				try {
					return Timestamp.valueOf(timestamp.trim() + ":00");
				} catch (Exception eee) {
					try {
						return Timestamp.valueOf(timestamp.trim() + ":00:00");
					} catch (Exception eeee) {
					}
				}
			}
		}
		return null;
	}

	public static long daysBetween(Timestamp t1, Timestamp t2) {
		return (t2.getTime() - t1.getTime()) / DAY_MILLI;
	}

	public static String getNoSpSysDateString() {
		return DateToStr(new Date(System.currentTimeMillis()), FORMAT_DATEONLYNOSP);
	}

	public static String getSysDateString() {
		return DateToStr(new Date(System.currentTimeMillis()), DATE_FORMAT_DATEONLY);
	}

	public static Timestamp toSqlTimestamp(String sDate) {
		if (sDate == null) {
			return null;
		}
		if (sDate.length() != DateUtils.DATE_FORMAT_DATEONLY.length()) {
			return null;
		}
		return toSqlTimestamp(sDate, DateUtils.DATE_FORMAT_DATEONLY);
	}

	public static Timestamp toSqlTimestamp(String sDate, String sFmt) {
		String temp = null;
		if (sDate == null || sFmt == null) {
			return null;
		}
		if (sDate.length() != sFmt.length()) {
			return null;
		}
		if (sFmt.equals(DateUtils.FORMAT)) {
			temp = sDate.replace('/', '-');
			temp = temp + ".000000000";
		} else if (sFmt.equals(DateUtils.DATE_FORMAT_DATEONLY)) {
			temp = sDate.replace('/', '-');
			temp = temp + " 00:00:00.000000000";
		} else {
			return null;
		}
		// java.sql.Timestamp.value() 要求的格式必须为yyyy-mm-dd hh:mm:ss.fffffffff
		return Timestamp.valueOf(temp);
	}

	public static Timestamp getSysDateTimestamp() {
		return new Timestamp(System.currentTimeMillis());
	}
}
