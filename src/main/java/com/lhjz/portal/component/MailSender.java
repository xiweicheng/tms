/**
 * 版权所有 (TMS)
 */
package com.lhjz.portal.component;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeUtility;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.lhjz.portal.component.core.MailQueue;
import com.lhjz.portal.component.core.model.MailItem;
import com.lhjz.portal.util.StringUtil;

import lombok.extern.log4j.Log4j;

/**
 * 
 * @author xi
 * 
 * @date 2015年6月14日 上午10:31:32
 * 
 */
@Component("myMailSender")
@Log4j
public class MailSender {

	@Autowired
	JavaMailSender mailSender;
	
	@Autowired
	MailQueue mailQueue;
	
	static ExecutorService pool = Executors.newSingleThreadExecutor();

	public JavaMailSenderImpl getMailSender() {
		return (JavaMailSenderImpl) mailSender;
	}

	public boolean sendText(String subject, String text, String... toAddr) {

		SimpleMailMessage message = new SimpleMailMessage();
		message.setFrom(((JavaMailSenderImpl) mailSender).getUsername());
		message.setTo(toAddr);
		message.setSubject(subject);
		message.setText(text);

		mailSender.send(message);

		return true;
	}

	public boolean sendHtml(String subject, String html, String... toAddr)
			throws MessagingException {

		if (toAddr == null || toAddr.length == 0) {
			return false;
		}

		MimeMessage mimeMessage = mailSender.createMimeMessage();
		MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, false,
				"UTF-8");

		helper.setFrom(((JavaMailSenderImpl) mailSender).getUsername());
		helper.setTo(toAddr);
		helper.setSubject(subject);
		helper.setText(html, true);

		mailSender.send(mimeMessage);

		return true;
	}

	public boolean sendHtmlWithAttachment(String subject, String html,
			String[] attachmentPaths, String... toAddr)
			throws MessagingException, IOException {

		MimeMessage mimeMessage = mailSender.createMimeMessage();
		MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true,
				"UTF-8");

		helper.setFrom(((JavaMailSenderImpl) mailSender).getUsername());
		helper.setTo(toAddr);
		helper.setSubject(subject);
		helper.setText(html, true);

		for (String path : attachmentPaths) {
			helper.addAttachment(MimeUtility.encodeText(
					new File(path).getName(), "UTF-8", "B"),
					new FileSystemResource(path));
		}

		mailSender.send(mimeMessage);

		return true;
	}

	public void sendHtmlByQueue(MailItem mailItem) {

		this.sendHtmlByQueue(mailItem.getSubject(), mailItem.getHtml(), mailItem.getToAddr());
	}
	
	public void sendHtmlByQueue(String subject, String html, String... toAddr) {

		if (StringUtil.isEmpty(subject) || StringUtil.isEmpty(html)) {
			return;
		}

		if (toAddr == null || toAddr.length == 0) {
			return;
		}

		pool.execute(() -> {
			try {
				this.sendHtml(subject, html, toAddr);
				log.info("邮件发送成功!");
			} catch (MessagingException e) {
				log.info("邮件发送失败,放入邮件定时计划任务队列中!");
				if (!mailQueue.offer(new MailItem(subject, html, toAddr))) {
					log.error("邮件队列已满!");
				}
				e.printStackTrace();
			}
		});
	}
	
	@Scheduled(fixedRate = 60000)
	public void mailQueueScheduledTask() {
		log.info("邮件定时计划任务执行!");
		MailItem mailItem = mailQueue.poll();
		if (mailItem != null) {
			this.sendHtmlByQueue(mailItem);
		}
	}
}
