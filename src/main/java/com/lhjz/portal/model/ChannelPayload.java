package com.lhjz.portal.model;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ChannelPayload {
	
	private String uuid;

	private Cmd cmd;

	private Long id;

	private Long cid;
	
	private String username;
	
	private String atUsernames;

	public static enum Cmd {
		R, C, U, D;
	}
}
