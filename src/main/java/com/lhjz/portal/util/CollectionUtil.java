/**
 * CollectionUtil.java
 */
package com.lhjz.portal.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * CollectionUtil处理工具类.
 * 
 * @creation 2013-10-10 下午3:05:53
 * @modification 2013-10-10 下午3:05:53
 * @company Canzs
 * @author xiweicheng
 * @version 1.0
 * 
 */
public final class CollectionUtil {

	// private static Logger logger = Logger.getLogger(MapUtil.class);

	private CollectionUtil() {
		super();
	}

	/**
	 * 判断Collection是否为空.
	 * 
	 * @author xiweicheng
	 * @creation 2014年1月6日 上午11:24:46
	 * @modification 2014年1月6日 上午11:24:46
	 * @param collection
	 * @return
	 */
	public static boolean isEmpty(Collection<?> collection) {
		return collection == null || collection.isEmpty();
	}

	/**
	 * 判断Collection是否不为空.
	 * 
	 * @author xiweicheng
	 * @creation 2014年1月6日 上午11:24:46
	 * @modification 2014年1月6日 上午11:24:46
	 * @param collection
	 * @return
	 */
	public static boolean isNotEmpty(Collection<?> collection) {
		return !isEmpty(collection);
	}
	
	/**
	 * 反向排序List
	 * @param list
	 * @return
	 */
	public static <T> List<T> reverseList(List<T> list) {
		List<T> newList = new ArrayList<T>();
		list.stream().forEach((l) -> {
			newList.add(0, l);
		});
		
		return newList;
	}

}
