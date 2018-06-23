package com.lhjz.portal.model;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ChannelPayload {

	private Cmd cmd;

	private Long id;

	private Long cid;

	public static enum Cmd {
		C, U, D;
	}
}
