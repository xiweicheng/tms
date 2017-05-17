package com.lhjz.portal.component.core.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MailItem {

	String subject;

	String html;

	String[] toAddr;

}
