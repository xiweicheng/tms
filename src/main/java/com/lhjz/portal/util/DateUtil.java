/**
 * WebUtil.java
 */
package com.lhjz.portal.util;

import org.joda.time.DateTime;
import org.joda.time.DateTimeFieldType;

import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Date;

/**
 * @author XiWeiCheng
 */
public final class DateUtil {

    /**
     * EMPTY [String]
     */
    public static final String EMPTY = "";
    public static final String FORMAT1 = "yyyy-MM-dd HH:mm:ss";
    public static final String FORMAT2 = "yyyy/MM/dd HH:mm:ss";
    public static final String FORMAT7 = "yyyy/MM/dd HH:mm:ss S";
    public static final String FORMAT8 = "yyMMddHHmmssS";
    public static final String FORMAT9 = "yyyyMMddHHmmssS";
    public static final String FORMAT3 = "yyyy-MM-dd";
    public static final String FORMAT4 = "yyyy/MM/dd";
    public static final String FORMAT5 = "yyyyMMddHHmmss";
    public static final String FORMAT6 = "yyyyMMdd";

    private DateUtil() {
        super();
    }

    /**
     * 解析日期字符串.
     *
     * @param date
     * @param format
     * @return
     */
    public static Date parse(String date, String... formats) {

        if (date == null || date.length() == 0) {
            return null;
        }

        for (String format : formats) {

            try {
                return new SimpleDateFormat(format).parse(date);
            } catch (Exception e) {
                // dothing
            }
        }

        return null;
    }

    /**
     * 返回当前日期.
     *
     * @return
     */
    public static Date now() {
        return new Date();
    }

    /**
     * 格式化日期.
     *
     * @param date
     * @param format
     * @return
     */
    public static String format(Date date, String format) {

        if (date == null) {
            return DateUtil.EMPTY;
        }

        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(FORMAT2);


        simpleDateFormat.applyPattern(format);

        return simpleDateFormat.format(date);
    }

    /**
     * 获取日期时间秒值.
     *
     * @param dateTime
     * @return
     * @author xiweicheng
     * @creation 2014年2月28日 下午1:31:34
     * @modification 2014年2月28日 下午1:31:34
     */
    public static Long getTime(String dateTime) {

        if (StringUtil.isNotEmpty(dateTime)) {
            Date date = parse(dateTime, FORMAT1, FORMAT2, FORMAT3, FORMAT4);

            if (date != null) {
                return date.getTime() / 1000;
            }
        }

        return null;
    }

    /**
     * 获取当天限制码.
     *
     * @return
     * @author xiweicheng
     * @creation 2014年4月9日 上午11:46:44
     * @modification 2014年4月9日 上午11:46:44
     */
    public static String getTodayLimitCode() {
        return format(now(), FORMAT6);
    }

    /**
     * 将距今的秒数转为为 秒 分 时 天 月
     *
     * @param sec
     * @return
     * @author xiweicheng
     * @creation 2014年4月15日 上午10:31:39
     * @modification 2014年4月15日 上午10:31:39
     */
    public static String convert(Long sec) {

        if (sec == null) {
            return EMPTY;
        }

        if (sec < 60) {
            return sec + "秒";
        } else if (sec / 60 < 60) {
            return (sec / 60) + "分" + convert(sec % 60);
        } else if (sec / 3600 < 60) {
            return (sec / 3600) + "时" + convert(sec % 3600);
        } else if (sec / (3600 * 24) < 30) {
            return sec / (3600 * 24) + "天" + convert(sec % (3600 * 24));
        } else {
            return ">" + sec / (3600 * 24) + "天";
        }
    }

    /**
     * 将日期转换为 今天 昨天 ...
     *
     * @param times
     * @return
     * @author xiweicheng
     * @creation 2014年5月7日 下午12:06:42
     * @modification 2014年5月7日 下午12:06:42
     */
    public static String toNiceTime(long times) {

        DateTime dateTime = new DateTime(times);
        DateTime now = DateTime.now();

        if (dateTime.get(DateTimeFieldType.year()) == now.get(DateTimeFieldType.year())) {

            if (dateTime.get(DateTimeFieldType.monthOfYear()) == now.get(DateTimeFieldType.monthOfYear())) {

                if (dateTime.get(DateTimeFieldType.dayOfMonth()) == now.getDayOfMonth()) {
                    return dateTime.toString("H:m");
                } else if (dateTime.plusDays(1).get(DateTimeFieldType.dayOfMonth()) == now.getDayOfMonth()) {
                    return "昨天";
                } else if (dateTime.plusDays(2).get(DateTimeFieldType.dayOfMonth()) == now.getDayOfMonth()) {
                    return "前天";
                } else {
                    return dateTime.toString("M-d");
                }
            } else if (dateTime.plusMonths(1).get(DateTimeFieldType.monthOfYear()) == now.get(DateTimeFieldType
                    .monthOfYear())) {
                return "上个月";
            } else if (dateTime.plusMonths(2).get(DateTimeFieldType.monthOfYear()) == now.get(DateTimeFieldType
                    .monthOfYear())) {
                return "上上个月";
            } else {
                return dateTime.toString("yy-M");
            }
        } else if (dateTime.plusYears(1).get(DateTimeFieldType.year()) == now.get(DateTimeFieldType.year())) {
            return "去年";
        } else if (dateTime.plusYears(2).get(DateTimeFieldType.year()) == now.get(DateTimeFieldType.year())) {
            return "前年";
        } else {
            return dateTime.toString("yyyy");
        }
    }

    /**
     * 将日期转换为 今天 昨天 ...
     *
     * @param date
     * @return
     * @author xiweicheng
     * @creation 2014年5月7日 下午12:48:51
     * @modification 2014年5月7日 下午12:48:51
     */
    public static String toNiceTime(Date date) {
        if (date == null) {
            return StringUtil.EMPTY;
        }
        return toNiceTime(date.getTime());
    }

    /**
     * 将日期转换为 今天 昨天 ...
     *
     * @param date
     * @return
     * @author xiweicheng
     * @creation 2014年5月7日 下午12:48:51
     * @modification 2014年5月7日 下午12:48:51
     */
    public static String toNiceTime(String date) {
        return toNiceTime(parse(date, FORMAT1, FORMAT2));
    }

    /**
     * localDateTime转Date
     *
     * @param localDateTime
     */
    public static Date localDateTime2Date(LocalDateTime localDateTime) {
        ZoneId zoneId = ZoneId.systemDefault();
        ZonedDateTime zdt = localDateTime.atZone(zoneId);

        return Date.from(zdt.toInstant());
    }

}
