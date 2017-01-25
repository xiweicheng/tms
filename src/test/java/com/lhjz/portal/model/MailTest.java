package com.lhjz.portal.model;

import org.testng.annotations.Test;

public class MailTest {

	@Test
	public void get() {
		String[] strings = Mail.instance().get();
		System.out.println(strings);
	}
}
