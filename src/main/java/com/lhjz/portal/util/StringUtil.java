package com.lhjz.portal.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 字符串常用工具类.
 * 
 * @creation 2013-10-8 下午12:57:30
 * @modification 2013-10-8 下午12:57:30
 * @company Canzs
 * @author xiweicheng
 * @version 1.0
 * 
 */
public final class StringUtil {

	/**
	 * 判断是不是数字字符串.
	 * 
	 * @param str
	 * @return
	 */
	public static boolean isNumber(String str) {

		if (isEmpty(str)) {
			return false;
		} else {
			try {
				Double.parseDouble(str);
				return true;
			} catch (Exception e) {
				return false;
			}
		}
	}

	/**
	 * 判断是否为正确的邮件格式
	 * 
	 * @param str
	 * @return boolean
	 */
	public static boolean isEmail(String str) {
		if (isEmpty(str))
			return false;
		return str.matches("^[\\w-]+(\\.[\\w-]+)*@[\\w-]+(\\.[\\w-]+)+$");
	}

	/**
	 * 判断字符串是否为合法手机号 11位 13 14 15 18开头
	 * 
	 * @param str
	 * @return boolean
	 */
	public static boolean isMobile(String str) {
		if (isEmpty(str))
			return false;
		return str.matches("^(13|14|15|18)\\d{9}$");
	}

	/**
	 * 裁剪字符串到指定长度.
	 * 
	 * @param string
	 * @param i
	 */
	public static String limitLength(String str, int i) {
		if (isEmpty(str)) {
			return "";
		} else {
			if (str.length() <= i) {
				return str;
			} else {
				return str.substring(0, i - 3) + "...";
			}
		}
	}

	/** EMPTY [String] */
	public static final String EMPTY = "";

	/** EMPTY [String] */
	public static final String SEARCH_SEPARATOR = "@&~";

	private StringUtil() {
		super();
	}

	/**
	 * 字符串占位符替换.
	 * 
	 * @param tpl
	 *            like xx{?1}yyy{?2}zzz
	 * @param vals
	 *            替换占位符的值
	 * @return 替换后的结果
	 */
	public static String replace(String tpl, Object... vals) {

		if (tpl != null) {

			for (int i = 1; i <= vals.length; i++) {
				tpl = tpl.replace("{?" + i + "}", String.valueOf(vals[i - 1]));
			}
		}

		return tpl;
	}

	/**
	 * replace like xxxx{key}yyy{key}zzzz
	 * 
	 * @author xiweicheng
	 * @creation 2013年11月28日 下午1:25:16
	 * @modification 2013年11月28日 下午1:25:16
	 * @param tpl
	 *            string like xxxx{key}yyy{key}zzzz
	 * @param map
	 * @return
	 */
	public static String replaceByMap(String tpl, Map<String, Object> map) {

		if (tpl != null && map != null && map.size() > 0) {

			for (String key : map.keySet()) {
				tpl = tpl
						.replace("{" + key + "}", String.valueOf(map.get(key)));
			}
		}

		return tpl;
	}

	/**
	 * replace like xxxx{key}yyy{key}zzzz
	 * 
	 * @author xiweicheng
	 * @creation 2013年11月28日 下午1:58:16
	 * @modification 2013年11月28日 下午1:58:16
	 * @param tpl
	 *            string like xxxx{key}yyy{key}zzzz
	 * @param vals
	 *            key:value pair like k1, v1, k2, v2 ...
	 * @return
	 */
	public static String replaceByKV(String tpl, Object... vals) {

		Map<String, Object> map = new HashMap<String, Object>();

		for (int i = 0; i < vals.length; i += 2) {
			if (i + 1 < vals.length) {
				map.put(String.valueOf(vals[i]), vals[i + 1]);
			}
		}

		return replaceByMap(tpl, map);
	}

	/**
	 * 连接字符串.
	 * 
	 * @param connector
	 * @param objects
	 * @return
	 */
	public static String join(String connector, Object... objects) {

		StringBuffer sBuffer = new StringBuffer();

		for (Object object : objects) {
			sBuffer.append(object).append(connector);
		}

		if (sBuffer.length() > 0) {
			sBuffer.delete(sBuffer.length() - connector.length(),
					sBuffer.length());
		}

		return sBuffer.toString();
	}

	/**
	 * 连接字符串.
	 * 
	 * @param connector
	 * @param arr
	 * @return
	 */
	public static String join(String connector, String[] arr) {

		StringBuffer sBuffer = new StringBuffer();

		for (Object object : arr) {
			sBuffer.append(object).append(connector);
		}

		if (sBuffer.length() > 0) {
			sBuffer.delete(sBuffer.length() - connector.length(),
					sBuffer.length());
		}

		return sBuffer.toString();
	}

	/**
	 * 连接字符串.
	 * 
	 * @param connector
	 * @param strs
	 * @return
	 */
	public static String join2(String connector, String... strs) {

		StringBuffer sBuffer = new StringBuffer();

		for (Object object : strs) {
			sBuffer.append(object).append(connector);
		}

		if (sBuffer.length() > 0) {
			sBuffer.delete(sBuffer.length() - connector.length(),
					sBuffer.length());
		}

		return sBuffer.toString();
	}

	/**
	 * 连接字符串.
	 * 
	 * @param connector
	 * @param objects
	 * @return
	 */
	public static String join(String connector, List<String> objects) {

		StringBuffer sBuffer = new StringBuffer();

		for (String object : objects) {
			sBuffer.append(object).append(connector);
		}

		if (sBuffer.length() > 0) {
			sBuffer.delete(sBuffer.length() - connector.length(),
					sBuffer.length());
		}

		return sBuffer.toString();
	}
	
	/**
	 * 连接字符串.
	 * 
	 * @param connector
	 * @param objects
	 * @return
	 */
	public static String join2(String connector, List<Object> objects) {
 
		StringBuffer sBuffer = new StringBuffer();

		for (Object object : objects) {
			sBuffer.append(object).append(connector);
		}

		if (sBuffer.length() > 0) {
			sBuffer.delete(sBuffer.length() - connector.length(),
					sBuffer.length());
		}

		return sBuffer.toString();
	}

	/**
	 * 连接字符串.
	 * 
	 * @param connector
	 * @param objects
	 * @return
	 */
	public static String join(String connector, Set<String> objects) {

		StringBuffer sBuffer = new StringBuffer();

		for (String object : objects) {
			sBuffer.append(object).append(connector);
		}

		if (sBuffer.length() > 0) {
			sBuffer.delete(sBuffer.length() - connector.length(),
					sBuffer.length());
		}

		return sBuffer.toString();
	}

	/**
	 * 对象转字符串.
	 * 
	 * @param object
	 * @return
	 */
	public static String toString(Object object) {

		return object == null ? EMPTY : object.toString();
	}

	/**
	 * 从map中获取key对应的字符串值.
	 * 
	 * @param map
	 * @param key
	 * @return
	 */
	public static String getString(Map<String, Object> map, String key) {

		if (map == null || !map.containsKey(key)) {
			return null;
		}

		return toString(map.get(key));
	}

	/**
	 * 从map中获取key对应的字符串值.
	 * 
	 * @param map
	 * @param key
	 * @return
	 */
	public static String getNotNullString(Map<String, Object> map, String key) {

		if (map == null || !map.containsKey(key)) {
			return EMPTY;
		}

		return toString(map.get(key));
	}

	/**
	 * 字符串判空.
	 * 
	 * @param str
	 * @return
	 */
	public static boolean isEmpty(Object str) {

		return str == null || str.equals(EMPTY);
	}

	/**
	 * 字符串判非空.
	 * 
	 * @param str
	 * @return
	 */
	public static boolean isNotEmpty(Object str) {

		return !isEmpty(str);
	}

	/**
	 * 字符串判非空.
	 * 
	 * @param str
	 * @return
	 */
	public static boolean isValid(String str) {

		return !isEmpty(str);
	}

	/**
	 * 字符串数组转化为List<String>.
	 * 
	 * @param values
	 * @return
	 */
	public static List<String> array2List(String... values) {

		List<String> list = new ArrayList<String>();

		for (String value : values) {
			list.add(value);
		}

		return list;
	}

	/**
	 * 字符串数组转化为Map<String, Object>
	 * 
	 * @author xiweicheng
	 * @creation 2013年11月30日 下午6:22:26
	 * @modification 2013年11月30日 下午6:22:26
	 * @param values
	 * @return
	 */
	public static Map<String, Object> array2Map(Object... values) {

		Map<String, Object> map = new HashMap<>();

		if (values.length > 0) {

			for (int i = 0; i < values.length; i += 2) {

				if (i + 1 < values.length) {
					map.put(String.valueOf(values[i]), values[i + 1]);
				}
			}
		}

		return map;
	}

	/**
	 * 字符串数组转化为Map<String, Object>
	 * 
	 * @author xiweicheng
	 * @creation 2013年11月30日 下午6:22:26
	 * @modification 2013年11月30日 下午6:22:26
	 * @param values
	 * @return
	 */
	public static Map<String, String> stringArr2Map(String... values) {

		Map<String, String> map = new HashMap<>();

		if (values.length > 0) {

			for (int i = 0; i < values.length; i += 2) {

				if (i + 1 < values.length) {
					map.put(values[i], values[i + 1]);
				}
			}
		}

		return map;
	}

	/**
	 * 字符串分割.
	 * 
	 * @author xiweicheng
	 * @creation 2013年11月30日 下午6:33:03
	 * @modification 2013年11月30日 下午6:33:03
	 * @param val
	 * @param decollator
	 * @return
	 */
	public static String[] split(String val, String decollator) {

		if (!isEmpty(val)) {
			return val.split(decollator);
		}

		return null;
	}

	/**
	 * 获取一组值.
	 * 
	 * @author xiweicheng
	 * @creation 2013年11月30日 下午5:58:37
	 * @modification 2013年11月30日 下午5:58:37
	 * @param map
	 * @param keyArr
	 * @return
	 */
	public static String[] getValues(Map<String, String> map, String... keyArr) {

		String[] objArr = new String[keyArr.length];

		if (keyArr.length > 0 && map != null && map.size() > 0) {
			int i = 0;

			for (String key : keyArr) {
				objArr[i++] = map.get(key);
			}
		}

		return objArr;
	}

	/**
	 * HTML标签转义方法 —— java代码库
	 * 
	 * @param content
	 * @return
	 */
	public static String html(String content) {

		if (content == null) {
			return "";
		}

		String html = content;
		html = html.replace("'", "&apos;");
		html = html.replace("\"", "&quot;");
		html = html.replace("\t", "&nbsp;&nbsp;");// 替换跳格
		html = html.replace("<", "&lt;");
		html = html.replace(">", "&gt;");

		return html;
	}

	/**
	 * 使用textarea包裹返回到前端的json body
	 * 
	 * @param jsonBody
	 * @return
	 */
	public static String wrapByTextarea(String jsonBody) {
		return String.format(
				"<textarea data-type='application/json'>%s</textarea>",
				jsonBody);
	}

}
