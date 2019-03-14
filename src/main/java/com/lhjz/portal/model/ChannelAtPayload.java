package com.lhjz.portal.model;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ChannelAtPayload {

	private String from;

	private String to;

	private Cmd cmd;

	private Long id;
	
	private Long ccid;

	private Long cid;

	private Long version;

	private String content;

	private String username;
	
	private String cname;

	public static enum Cmd {
		C, U, RC, RU;
	}
}
