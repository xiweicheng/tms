/**
 * NumberUtil.java
 */
package com.lhjz.portal.util;

import lombok.extern.slf4j.Slf4j;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Map;
import java.util.Random;

/**
 * 数字处理工具类.
 *
 * @author xiweicheng
 * @version 1.0
 * @creation 2013-10-11 上午11:51:59
 * @modification 2013-10-11 上午11:51:59
 * @company Canzs
 */
@Slf4j
public final class NumberUtil {

    /**
     * EMPTY [String]
     */
    public static final String EMPTY = "";

    public static final Random random = new Random();

    private NumberUtil() {
        super();
    }

    /**
     * 去除小数点部位.
     *
     * @param value
     * @return
     */
    public static String formatDouble(double value) {
        NumberFormat format = new DecimalFormat("0");
        return format.format(value);
    }

    /**
     * 保留两位小数.
     *
     * @param value
     * @return
     */
    public static String format2Money(double value) {
        NumberFormat format2 = new DecimalFormat("0.00");
        return format2.format(value);
    }

    /**
     * 转换为Integer.
     *
     * @param value
     * @return
     */
    public static Integer toInteger(Object value) {

        try {
            if (value == null || EMPTY.equals(value)) {
                return null;
            }
            return Integer.valueOf(value.toString());
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return null;
        }

    }

    /**
     * 转换为Double.
     *
     * @param value
     * @return
     */
    public static Double toDouble(Object value) {

        try {
            if (value == null || EMPTY.equals(value)) {
                return null;
            }
            return Double.valueOf(value.toString());
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return null;
        }

    }

    /**
     * 转换为Long.
     *
     * @param value
     * @return
     */
    public static Long toLong(Object value) {

        try {
            if (value == null || EMPTY.equals(value)) {
                return null;
            }
            return Long.valueOf(value.toString());
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return null;
        }

    }

    /**
     * 判断 val1 > val2
     *
     * @param val1
     * @param val2
     * @return
     * @author xiweicheng
     * @creation 2013年12月31日 下午3:44:21
     * @modification 2013年12月31日 下午3:44:21
     */
    public static boolean gt(Object val1, Double val2) {

        Double v1 = toDouble(val1);

        if (v1 == null) {
            throw new IllegalArgumentException("Argument[val1]can not be null.");
        }

        if (val2 == null) {
            throw new IllegalArgumentException("Argument[val2]can not be null.");
        }

        return v1 > val2;
    }

    /**
     * 判断 val1 == val2
     *
     * @param val1
     * @param val2
     * @return
     * @author xiweicheng
     * @creation 2013年12月31日 下午3:44:21
     * @modification 2013年12月31日 下午3:44:21
     */
    public static boolean eq(Object val1, Double val2) {

        Double v1 = toDouble(val1);

        if (v1 == null) {
            throw new IllegalArgumentException("Argument[val1]can not be null.");
        }

        if (val2 == null) {
            throw new IllegalArgumentException("Argument[val2]can not be null.");
        }

        return v1.equals(val2);
    }

    /**
     * 判断 val1 != val2
     *
     * @param val1
     * @param val2
     * @return
     * @author xiweicheng
     * @creation 2013年12月31日 下午3:44:21
     * @modification 2013年12月31日 下午3:44:21
     */
    public static boolean ne(Object val1, Double val2) {

        return !eq(val1, val2);
    }

    /**
     * 判断 val1 == val2
     *
     * @param val1
     * @param val2
     * @return
     * @author xiweicheng
     * @creation 2013年12月31日 下午3:44:21
     * @modification 2013年12月31日 下午3:44:21
     */
    public static boolean lt(Object val1, Double val2) {

        return !gt(val1, val2);
    }

    public static Integer getInteger(Map<String, Object> map, String key) {

        return toInteger(map.get(key));
    }

    public static Double getDouble(Map<String, Object> map, String key) {

        return toDouble(map.get(key));
    }

    public static Long getLong(Map<String, Object> map, String key) {

        return toLong(map.get(key));
    }

    public static Short getShort(Map<String, Object> map, String key) {
        return toShort(map.get(key));
    }

    /**
     * 转换为Short.
     *
     * @param value
     * @return
     */
    public static Short toShort(Object value) {

        try {
            if (value == null || EMPTY.equals(value)) {
                return null;
            }
            return Short.valueOf(value.toString());
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return null;
        }

    }

    public static String format(Object object) {

        if (object instanceof Double) {
            return formatDouble((Double) object);
        } else {
            Double val = toDouble(object);

            if (val != null) {
                return formatDouble(val);
            }
        }

        return EMPTY;
    }

    /**
     * 判断是否为数字类型或者数字字符串.
     *
     * @param val
     * @return
     * @author xiweicheng
     * @creation 2013年11月15日 下午2:47:36
     * @modification 2013年11月15日 下午2:47:36
     */
    public static boolean isNumber(Object val) {

        if (val instanceof Number) {
            return true;
        }

        if (toDouble(val) != null) {
            return true;
        }

        return false;
    }

    /**
     * 产生指定长度的随机数.
     *
     * @param length
     * @return
     * @author xiweicheng
     * @creation 2014年3月28日 上午11:05:33
     * @modification 2014年3月28日 上午11:05:33
     */
    public static String random(int length) {

        StringBuilder sBuffer = new StringBuilder();

        for (int i = 0; i < length; i++) {
            sBuffer.append(random.nextInt(10));
        }

        return sBuffer.toString();
    }
}
