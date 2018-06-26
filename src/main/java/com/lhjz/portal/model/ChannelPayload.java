package com.lhjz.portal.model;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ChannelPayload {

	private Cmd cmd;

	private Long id;

	private Long cid;
	
	private String username;

	public static enum Cmd {
		R, C, U, D;
	}
}
