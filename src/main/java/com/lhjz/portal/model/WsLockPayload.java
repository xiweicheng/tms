package com.lhjz.portal.model;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class WsLockPayload {

	private Cmd cmd;

	private String locker;
	
	private String name;

	private Long blogId;

	private String sessionId;

	public enum Cmd {
		LOCK, UNLOCK;
	}
}
