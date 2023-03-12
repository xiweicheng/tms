package com.lhjz.portal.util;

import com.google.common.collect.Sets;

import java.util.HashSet;
import java.util.Set;

public class CommonUtil {

	private CommonUtil() {
	}

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

		if (arr != null) {
			return Sets.newHashSet(arr);
		}
		
		return new HashSet<String>();
	}
}
