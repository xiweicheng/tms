package com.lhjz.portal.model;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SchedulePayload {

	private Cmd cmd;

	private Long id;
	
	private String title;

	private String creator;

	public static enum Cmd {
		R, C, U, D;
	}
}
