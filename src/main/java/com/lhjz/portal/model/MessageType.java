/**
 * 版权所有 (TMS)
 */
package com.lhjz.portal.model;

/**
 * 
 * @author xi
 * 
 * @date 2015年5月23日 下午6:57:39
 * 
 */
public enum MessageType {

	Error("error"), Warn("warn"), Info("info");

	private String value;

	private MessageType(String value) {
		this.value = value;
	}

	public String getValue() {
		return value;
	}

}
