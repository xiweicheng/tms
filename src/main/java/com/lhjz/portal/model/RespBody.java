package com.lhjz.portal.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.beanutils.BeanMap;

public class RespBody implements Serializable {

	/** serialVersionUID long */
	private static final long serialVersionUID = -1965817463440121331L;
	private boolean success;
	private Object data = "";
	private List<Object> msgs = new ArrayList<>();

	public RespBody addMsg(Object msg) {
		this.msgs.add(msg);
		return this;
	}

	public RespBody data(Object data) {
		this.data = data;
		return this;
	}

	public RespBody status(boolean success) {
		this.success = success;
		return this;
	}

	public Map<?, ?> asMap() {
		return new BeanMap(this);
	}

	public RespBody toggle() {
		this.success = !success;
		return this;
	}

	public static RespBody succeed(Object data) {
		return new RespBody(true, data);
	}

	public static RespBody succeed() {
		return new RespBody(true);
	}

	public static RespBody failed(Object data) {
		return new RespBody(false, data);
	}

	public static RespBody failed() {
		return new RespBody(false);
	}

	public RespBody(boolean success) {
		this.success = success;
	}

	public RespBody(boolean success, Object data) {
		this.success = success;
		this.data = data;
	}

	public boolean isSuccess() {
		return success;
	}

	public void setSuccess(boolean success) {
		this.success = success;
	}

	public Object getData() {
		return data;
	}

	public void setData(Object data) {
		this.data = data;
	}

	public List<Object> getMsgs() {
		return msgs;
	}

	public void setMsgs(List<Object> msgs) {
		this.msgs = msgs;
	}

}
