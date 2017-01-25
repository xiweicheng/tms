/**
 * 版权所有 (TMS)
 */
package com.lhjz.portal.model;

import java.io.Serializable;

/**
 * 
 * @author xi
 * 
 * @date 2015年5月23日 下午6:56:12
 * 
 */
public class Message implements Serializable {

	/** serialVersionUID (long) */
	private static final long serialVersionUID = 8604987580302483818L;
	private Long id;
	private String title;
	private String message;
	private String detail;

	private MessageType type;

	public Message(MessageType type) {
		super();
		this.type = type;
	}

	public static Message error(String message) {
		return new Message(MessageType.Error).messsage(message);
	}

	public static Message warn(String message) {
		return new Message(MessageType.Warn).messsage(message);
	}

	public static Message info(String message) {
		return new Message(MessageType.Info).messsage(message);
	}

	public Message messsage(String message) {
		setMessage(message);
		return this;
	}

	public Message detail(String detail) {
		setDetail(detail);
		return this;
	}

	public Message title(String title) {
		setTitle(title);
		return this;
	}

	public Message id(Long id) {
		setId(id);
		return this;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public String getDetail() {
		return detail;
	}

	public void setDetail(String detail) {
		this.detail = detail;
	}

	public MessageType getType() {
		return type;
	}

	public void setType(MessageType type) {
		this.type = type;
	}

}
