package com.lhjz.portal.model;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class BlogPayload {

	private Cmd cmd;

	private Long id;

	private Long cid;
	
	private Long version;
	
	// BlogNews id
	private Long nid;
	
	private String username;
	
	private String title;
	
	private String content;

	public static enum Cmd {
		R, C, U, OU, D, At, Open, CC, CU, CD, F, FCC, FCU, CAt;
	}
}
