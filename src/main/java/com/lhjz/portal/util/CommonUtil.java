package com.lhjz.portal.util;

import java.util.HashSet;
import java.util.Set;

public class CommonUtil {

	// private static final Logger logger = Logger.getLogger(CommonUtil.class);

	/**
	 * 转换win换行符成html<br/>
	 * 
	 * @param winString
	 * @return
	 */
	public static String replaceLinebreak(String winString) {
		if (winString != null) {
			return winString.replaceAll("\n", "<br/>");
		}
		return StringUtil.EMPTY;
	}

	public static Set<String> arr2Set(String... arr) {
		Set<String> set = new HashSet<String>();
		if (arr != null) {
			for (String string : arr) {
				set.add(string);
			}
		}
		return set;
	}
}
