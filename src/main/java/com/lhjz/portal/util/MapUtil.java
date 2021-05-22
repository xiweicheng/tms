/**
 * MapUtil.java
 */
package com.lhjz.portal.util;

import java.util.HashMap;
import java.util.Map;

/**
 * Map处理工具类.
 * 
 * @creation 2013-10-10 下午3:05:53
 * @modification 2013-10-10 下午3:05:53
 * @company Canzs
 * @author xiweicheng
 * @version 1.0
 * 
 */
public final class MapUtil {

	private MapUtil() {
		super();
	}

	public static Map<String, Object> convert2Object(Map<String, String> map) {

		Map<String, Object> newMap = new HashMap<>();
		newMap.putAll(map);

		return newMap;
	}

	public static Map<String, String> convert2String(Map<String, Object> map) {

		Map<String, String> newMap = new HashMap<>();

		for (String key : map.keySet()) {
			newMap.put(key, String.valueOf(map.get(key)));
		}

		return newMap;
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
	public static Map<String, Object> objArr2Map(Object... values) {

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
	public static Map<String, String> strArr2Map(String... values) {

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

}
