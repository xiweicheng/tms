package com.lhjz.portal.component;

import java.io.IOException;
import java.util.Date;

import javax.mail.MessagingException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;

import com.lhjz.portal.Application;
import com.lhjz.portal.util.MapUtil;
import com.lhjz.portal.util.TemplateUtil;

@SpringApplicationConfiguration(classes = Application.class)
public class MailSender2Test extends AbstractTestNGSpringContextTests {

	@Autowired
	MailSender2 mailSender;

	// @Test
	public void sendHtml() throws MessagingException {

		mailSender.sendHtml("标题" + new Date().getTime(),
				"<html><head><meta charset='utf-8' /></head><body><h1>邮件标题</h1><p>邮件内容...</p></body></html>",
				"xiwc87@yeah.net", "xiweicheng@yeah.net");

	}

	// @Test
	public void sendHtmlWithAttachment() throws MessagingException, IOException {

		String html = TemplateUtil.process("templates/test",
				MapUtil.objArr2Map("title", "标题", "header", "消息标题", "message", "消息..."));

		mailSender.sendHtmlWithAttachment("标题" + new Date().getTime(), html,
				new String[] { "D:\\xiwc-desktop\\Dropzone-configration.pdf",
						"D:\\xiwc-desktop\\CentOS+nginx+jdk+tomcat.txt", "D:\\xiwc-desktop\\xiwc.jpg" },
				"xiwc87@yeah.net", "xiweicheng@yeah.net");
	}

	// @Test
	public void sendText() {
		mailSender.sendText("标题" + new Date().getTime(), "内容...", "xiwc87@yeah.net", "xiweicheng@yeah.net");
	}
}
