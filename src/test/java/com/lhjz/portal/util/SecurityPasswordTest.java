package com.lhjz.portal.util;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.annotations.Test;

import com.lhjz.portal.Application;

@SpringApplicationConfiguration(classes = Application.class)
public class SecurityPasswordTest extends AbstractTestNGSpringContextTests {

	@Autowired
	BCryptPasswordEncoder bCryptPasswordEncoder;

	@Test
	public void generatePwd() {
		System.out.println(bCryptPasswordEncoder.encode("xiwc"));
		System.out.println(bCryptPasswordEncoder.encode("admin"));
		System.out.println(bCryptPasswordEncoder.encode("lhjz"));
	}

}
