package com.lhjz.portal.model;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class OnlinePayload {

	private Cmd cmd;

	private String username;

	public static enum Cmd {
		ON, B, L, OFF; // online,busy,left,offline
	}
}
