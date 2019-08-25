package com.lhjz.portal.model;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class BlogCommentPayload {

	private Cmd cmd;

	private Long id;

	private String bid;

	private Long version;

	private String username;

	public static enum Cmd {
		C, D;
	}
}
