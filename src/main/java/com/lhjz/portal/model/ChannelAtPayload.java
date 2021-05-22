package com.lhjz.portal.model;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ChannelAtPayload {
	
	private String uuid;

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
	
	private String ctitle;

	public enum Cmd {
		C, U, RC, RU;
	}
}
