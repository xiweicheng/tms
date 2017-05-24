package com.lhjz.portal.component.core.model;

import com.lhjz.portal.model.MailAddr;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MailItem {

	String subject;

	String html;

	MailAddr[] toAddr;

}
