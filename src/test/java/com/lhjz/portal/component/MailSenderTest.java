package com.lhjz.portal.component;

import java.util.Date;

import javax.mail.MessagingException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.mail.MailSender;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.annotations.Test;

import com.lhjz.portal.Application;

@SpringApplicationConfiguration(classes = Application.class)
public class MailSenderTest extends AbstractTestNGSpringContextTests {

	@Autowired
	MailSender mailSender;

	@Test
	public void sendHtml() throws MessagingException {

		SimpleMailMessage message = new SimpleMailMessage();
		message.setFrom("lihengjizhu@163.com");
		message.setTo("xiwc87@yeah.net");
		message.setSubject("Mail Sender测试 " + new Date().getTime());
		message.setText("Mail Sender Test.... 邮件正文内容...");
		message.setCc("xiweicheng@yeah.net");
		message.setBcc("xiweicheng@yeah.net");

		mailSender.send(message);

	}

}
