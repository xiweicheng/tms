package com.lhjz.portal.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.beanutils.BeanMap;

import lombok.Data;

@Data
public class RespBody implements Serializable {

	private static final long serialVersionUID = -1965817463440121331L;
	private boolean success;
	private Object data = "";
	private List<Object> msgs = new ArrayList<>();
	private Object code = 0;

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

	public RespBody code(Object code) {
		this.code = code;
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

}
