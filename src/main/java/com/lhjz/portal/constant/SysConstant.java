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

	public static final String NUMBER_MINUS_1 = "-1";
	public static final String NUMBER_0 = "0";
	public static final String NUMBER_1 = "1";
	public static final String NUMBER_2 = "2";
	public static final String NUMBER_3 = "3";
	public static final String NUMBER_4 = "4";
	public static final String NUMBER_5 = "5";
	public static final String NUMBER_63 = "63";

	public static final String EMPTY = "";

	public static final String FALSE = "false";
	public static final String TRUE = "true";

	public static final String DEFAULT_IP = "0.0.0.0";
	// public static final String DEFAULT_PORT = "8050";
	public static final String DEFAULT_DNS = "0.0.0.0";

	public static final int NUM_MINUS_1 = -1;
	public static final int NUM_0 = 0;
	public static final int NUM_1 = 1;
	public static final int NUM_2 = 2;
	public static final int NUM_3 = 3;
	public static final int NUM_4 = 4;
	public static final int NUM_5 = 5;
	public static final int NUM_120 = 120;

	/** UNDERLINE [String] 下划线 字符串 */
	public static final String UNDERLINE = "_";
	/** COMMA [String] 逗号 字符串 */
	public static final String COMMA = ",";
	/** COLON [String] 冒号 字符串 */
	public static final String COLON = ":";
	/** NEW_LINE [String] */
	public static final String NEW_LINE = System.getProperty("line.separator");

	/** CALLBACK [String] 跨域访问的回调函数名 对应的参数Key值 */
	public static final String CALLBACK = "callback";
	/** MODEL_NAME_AJAX_JSON [Object] MODEL_NAME_AJAX_JSON */
	public static final String MODEL_NAME_AJAX_JSON = "ajax_json";

	/** ID_USRGRPIDS [String] */
	public static final String ID_USRGRPIDS = "usrgrpids";
	/** ID_USERIDS [String] */
	public static final String ID_USERIDS = "userids";
	/** ID_GROUPIDS [String] */
	public static final String ID_GROUPIDS = "groupids";
	/** ID_HOSTIDS [String] */
	public static final String ID_HOSTIDS = "hostids";
	/** ID_ACTIONIDS [String] */
	public static final String ID_ACTIONIDS = "actionids";

	/** SESSION_USER [String] */
	public static final String SESSION_USER = "sessionUser";
	/** MEDIA_PERIOD [String] */
	public static final String MEDIA_PERIOD = "1-7,00:00-24:00";
	/** ACTION_NAME_WEBSITE [String] */
	public static final String ACTION_NAME_WEBSITE = "trigger action[website]";
	/** ACTION_DEFSHORTDATA_WEBSITE [String] */
	public static final String ACTION_DEFSHORTDATA_WEBSITE = "{TRIGGER.STATUS}: {TRIGGER.NAME}";
	/** ACTION_DEFLONGDATA_WEBSITE [String] */
	public static final String ACTION_DEFLONGDATA_WEBSITE = "告警等级：{TRIGGER.NSEVERITY}\r\n监控URL：{HOST.NAME}\r\n告警时间：{EVENT.DATE} {EVENT.TIME}\r\n告警描述：{TRIGGER.NAME}";
	/** QR_LIMIT_SCENE [String] 永久二维码 */
	public static final String QR_LIMIT_SCENE = "QR_LIMIT_SCENE";
	/** QR_SCENE [String] 临时二维码 */
	public static final String QR_SCENE = "QR_SCENE";

	/** QR_TYPE_TZ [String] 二维码类型 台桌 */
	public static final String QR_TYPE_TZ = "TZ";
	public static final String QR_TYPE_JS = "JS";

	/** CONSUME_STATUS_STOP [Short] 消费结束 */
	public static final Short CONSUME_STATUS_STOP = 0;
	/** CONSUME_STATUS_ONGOING [Short] 消费进行中 */
	public static final Short CONSUME_STATUS_ONGOING = 1;
	/** CONSUME_STATUS_DISABLE [Short] 消费禁止 */
	public static final Short CONSUME_STATUS_DISABLE = 2;
	/** CONSUME_STATUS_OWN [Short] 个人结账请求中 */
	public static final Short CONSUME_STATUS_OWN = 3;
	/** CONSUME_STATUS_GROUP [Short] 集体结账请求中 */
	public static final Short CONSUME_STATUS_GROUP = 4;
	/** CONSUME_STATUS_REQ [Short] 进入请求中 */
	public static final Short CONSUME_STATUS_REQ = 5;
	public static final String RESOURCES_IMAGE = "image";
	public static final String SESSION_OBJECT = "session_object";
	public static final Short SHORT_FALSE = 0;
	public static final Short SHORT_TRUE = 1;
	public static final String CHECK_ON = "on";
	public static final String CHECK_OFF = "off";

	public static final String CHANGE_TO = " => ";

	/** 店铺 - 菜单 收藏 赞不赞 **/
	public static final Short FAVORITES_TYPE_BUSINESS = 0;
	public static final Short FAVORITES_TYPE_BUSINESS_NOT = 6;
	public static final Short FAVORITES_TYPE_MENU = 1;
	public static final Short FAVORITES_TYPE_MENU_NOT = 7;
	public static final Short FAVORITES_TYPE_BUSINESS_LIKE = 2;
	public static final Short FAVORITES_TYPE_BUSINESS_DISLIKE = 3;
	public static final Short FAVORITES_TYPE_MENU_LIKE = 4;
	public static final Short FAVORITES_TYPE_MENU_DISLIKE = 5;

	/** 菜单状态 **/
	public static final Short MENU_BILL_STATUS_CONFIRM = 1;
	public static final Short MENU_BILL_STATUS_DEBOOK = 2;
	public static final Short MENU_BILL_STATUS_STOW = 0;
	public static final Short MENU_BILL_STATUS_ACCEPT = 3;

	public static final Short REQUEST_STATUS_ONGOING = 1;
	public static final Short REQUEST_STATUS_STOP = 0;

	public static final Short REQUEST_TYPE_ENTER = 0;
	public static final Short REQUEST_TYPE_BILL_OWN = 1;
	public static final Short REQUEST_TYPE_BILL_GROUP = 2;
	public static final Short REQUEST_TYPE_QRCODE_USE_LIMIT = 3;
	public static final Short REQUEST_TYPE_SERVICE_CALL = 4;
	public static final Short REQUEST_TYPE_SERVICE_CALL_HANDLE = 5;

	public static final Long CATEGORY_ID_TZ = 1L;
	public static final Long CATEGORY_ID_JS = 2L;

	public static final Short ROLE_TYPE_UNDETERMINED = 0;
	public static final Short ROLE_TYPE_BOSS = 1;
	public static final Short ROLE_TYPE_COOK = 2;
	public static final Short ROLE_TYPE_WAITER = 3;
	public static final Short ROLE_TYPE_PROSCENIUM = 4;

	public static final String ORDER_PRICE_ASC = "1";
	public static final String ORDER_PRICE_DESC = "2";
	public static final String ORDER_LIKE = "3";

	public static final Short SERVICE_STATUS_ONGOING = 1;
	public static final Short SERVICE_STATUS_ACCEPT = 0;

	public static final Short SERVICE_TYPE_CALL = 0;

	/** MESSAGE_STATUS_NEW [Short] */
	public static final Short MESSAGE_STATUS_NEW = 0;
	public static final Short MESSAGE_STATUS_UNDERSTANDING = 1;
	public static final Short MESSAGE_STATUS_STOW = 2;

	public static final Short BILL_STATUS_NEW = 0;
	public static final Short BILL_STATUS_SETTLEMENT = 1;

	public static final Short BILL_TYPE_OWN = 0;
	public static final Short BILL_TYPE_GROUP = 1;

	public static final Short BUSINESS_CONSUMER_RECORD_NEW = 0;

	public static final Short BUSINESS_STATUS_NEW = 0;
	public static final Short BUSINESS_STATUS_UNDERSTANDING = 1;

	public static final Short LOG_STATUS_SUCCEED = 1;
	public static final Short LOG_STATUS_FAIL = 0;

	public static final Short LOG_TYPE_UPDATE_BUSINESS = 0;

	public static final Short QRCODE_STATUS_NEW = 0;

	public static final Short COMMENT_STATUS_NEW = 0;

	public static final Short COMMENT_TYPE_PLATFORM = 0;

	public static final String FILTER_PRE = "is:";

	public static final String ROLE_SUPER = "ROLE_SUPER";
	public static final String ROLE_ADMIN = "ROLE_ADMIN";
	public static final String ROLE_USER = "ROLE_USER";
	
	public static final String USER_VISITOR = "visitor";
	public static final String USER_NAME_VISITOR = "游客";
	
	public static final String ONLINE_USERS = "online_users";

}
