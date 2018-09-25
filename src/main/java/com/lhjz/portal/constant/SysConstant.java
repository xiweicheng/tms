/**
 * SysConstant.java
 */
package com.lhjz.portal.constant;

/**
 * 
 * 
 * @creation 2013-9-18 上午11:06:26
 * @modification 2013-9-18 上午11:06:26
 * @company Canzs
 * @author xiweicheng
 * @version 1.0
 * 
 */
/**
 * 
 * 
 * @creation 2014年1月3日 上午11:07:58
 * @modification 2014年1月3日 上午11:07:58
 * @company Canzs
 * @author xiweicheng
 * @version 1.0
 * 
 */
public final class SysConstant {

	private SysConstant() {
	}

	public static final String EMPTY = "";

	/** UNDERLINE [String] 下划线 字符串 */
	public static final String UNDERLINE = "_";
	/** COMMA [String] 逗号 字符串 */
	public static final String COMMA = ",";
	/** COLON [String] 冒号 字符串 */
	public static final String COLON = ":";
	/** NEW_LINE [String] */
	public static final String NEW_LINE = System.getProperty("line.separator");

	public static final String CHANGE_TO = " => ";

	public static final String FILTER_PRE = "is:";

	public static final String ROLE_SUPER = "ROLE_SUPER";
	public static final String ROLE_ADMIN = "ROLE_ADMIN";
	public static final String ROLE_USER = "ROLE_USER";

	public static final String USER_VISITOR = "visitor";
	public static final String USER_NAME_VISITOR = "游客";

	public static final String ONLINE_USERS = "online_users";

}
