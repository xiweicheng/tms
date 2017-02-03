package com.lhjz.portal.component;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.activation.DataHandler;
import javax.activation.FileDataSource;
import javax.mail.Address;
import javax.mail.BodyPart;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.mail.internet.MimeUtility;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.lhjz.portal.model.MailAuthenticator;
import com.lhjz.portal.model.MailInfo;
import com.lhjz.portal.util.StringUtil;

/**
 * 邮件发送
 * 
 * @creation 2014年3月21日 上午11:27:03
 * @modification 2014年3月21日 上午11:27:03
 * @author xiweicheng
 * @version 1.0
 * 
 */
@Component("myMailSender")
public class MailSender {

	static Logger logger = Logger.getLogger(MailSender.class);

	@Value("${lhjz.mail.to.addresses}")
	private String toAddrArr;
	@Value("${spring.mail.host}")
	private String serverHost;
	@Value("${spring.mail.port}")
	private String serverPort;
	@Value("${spring.mail.username}")
	private String username;
	@Value("${spring.mail.password}")
	private String password;
	@Value("${spring.mail.username}")
	private String fromAddr;

	/**
	 * 发送简单文本邮件
	 * 
	 * @param subject
	 * @param formatText
	 *            eg: xx{?1}yyy{?2}zzz
	 * @param vals
	 *            replace values
	 * @return
	 */
	public boolean sendText(String subject, String formatText, Object... vals) {

		MailInfo mailInfo = new MailInfo();
		mailInfo.setMailServerHost(serverHost);
		mailInfo.setMailServerPort(serverPort);
		mailInfo.setValidate(true);
		mailInfo.setUserName(username);
		mailInfo.setPassword(password);
		mailInfo.setFromAddress(fromAddr);
		mailInfo.setToAddresses(StringUtil.split(toAddrArr, ","));
		mailInfo.setSubject(subject);
		mailInfo.setContent(StringUtil.replace(formatText, vals));

		return send(mailInfo);
	}

	/**
	 * 发送简单文本邮件
	 * 
	 * @param subject
	 * @param formatText
	 *            eg: xx{?1}yyy{?2}zzz
	 * @param vals
	 *            replace values
	 * @return
	 */
	public boolean sendTextTo(String[] toAddr, String subject,
			String formatText, Object... vals) {

		MailInfo mailInfo = new MailInfo();
		mailInfo.setMailServerHost(serverHost);
		mailInfo.setMailServerPort(serverPort);
		mailInfo.setValidate(true);
		mailInfo.setUserName(username);
		mailInfo.setPassword(password);
		mailInfo.setFromAddress(fromAddr);
		mailInfo.setToAddresses(toAddr);
		mailInfo.setSubject(subject);
		mailInfo.setContent(StringUtil.replace(formatText, vals));

		return send(mailInfo);
	}

	/**
	 * 发送html邮件
	 * 
	 * @param subject
	 * @param content
	 * @return
	 */
	public boolean sendHtml(String subject, String content) {

		MailInfo mailInfo = new MailInfo();
		mailInfo.setMailServerHost(serverHost);
		mailInfo.setMailServerPort(serverPort);
		mailInfo.setValidate(true);
		mailInfo.setUserName(username);
		mailInfo.setPassword(password);
		mailInfo.setFromAddress(fromAddr);
		mailInfo.setToAddresses(StringUtil.split(toAddrArr, ","));
		mailInfo.setSubject(subject);
		mailInfo.setContent(content);
		mailInfo.setHtml(true);

		return send(mailInfo);
	}

	/**
	 * 发送html邮件
	 * 
	 * @param subject
	 * @param content
	 * @return
	 */
	public boolean sendHtmlTo(String[] toAddr, String subject, String content) {

		MailInfo mailInfo = new MailInfo();
		mailInfo.setMailServerHost(serverHost);
		mailInfo.setMailServerPort(serverPort);
		mailInfo.setValidate(true);
		mailInfo.setUserName(username);
		mailInfo.setPassword(password);
		mailInfo.setFromAddress(fromAddr);
		mailInfo.setToAddresses(toAddr);
		mailInfo.setSubject(subject);
		mailInfo.setContent(content);
		mailInfo.setHtml(true);

		return send(mailInfo);
	}

	/**
	 * 发送邮件
	 * 
	 * @param mailInfo
	 * @return
	 */
	public boolean send(MailInfo mailInfo) {

		try {

			MailAuthenticator authenticator = null;

			if (mailInfo.isValidate()) {
				authenticator = new MailAuthenticator(mailInfo.getUserName(),
						mailInfo.getPassword());
			}

			Message message = new MimeMessage(Session.getInstance(
					mailInfo.getProperties(), authenticator));
			message.setFrom(new InternetAddress(mailInfo.getFromAddress()));

			if (mailInfo.getToAddresses() == null
					|| mailInfo.getToAddresses().length == 0) {
				logger.error("邮件发送目标地址不存在！");
				return false;
			}

			List<Address> toAddresses = new ArrayList<Address>();
			for (String toAddr : mailInfo.getToAddresses()) {
				toAddresses.add(new InternetAddress(toAddr));
			}

			message.addRecipients(Message.RecipientType.TO,
					toAddresses.toArray(new Address[] {}));
			try {
				message.setSubject(MimeUtility.encodeText(
						mailInfo.getSubject(), "UTF-8", "B"));
			} catch (UnsupportedEncodingException e1) {
				e1.printStackTrace();
				logger.error("邮件附件设置主题，Error:" + e1.getMessage(), e1);
			}
			message.setSentDate(new Date());

			Multipart multipart = new MimeMultipart();

			BodyPart contentBodyPart = new MimeBodyPart();

			if (mailInfo.isHtml()) {
				contentBodyPart.setContent(mailInfo.getContent(),
						"text/html; charset=utf-8");
			} else {
				contentBodyPart.setContent(mailInfo.getContent(),
						"text/plain; charset=utf-8");
			}

			multipart.addBodyPart(contentBodyPart);

			if (mailInfo.hasAttachFiles()) {

				for (String filePath : mailInfo.getAttachFileNames()) {

					BodyPart fileBodyPart = new MimeBodyPart(); // 第二个BodyPart

					// 必须存在的文档，否则throw异常。
					FileDataSource fileDataSource = new FileDataSource(filePath);
					// 字符流形式装入文件
					fileBodyPart
							.setDataHandler(new DataHandler(fileDataSource));

					try {
						// 设置文件名，可以不是原来的文件名。
						fileBodyPart.setFileName(MimeUtility.encodeText(
								fileDataSource.getName(), "UTF-8", "B"));
					} catch (UnsupportedEncodingException e) {
						e.printStackTrace();
						logger.error("邮件附件设置失败，Error:" + e.getMessage(), e);
					}

					multipart.addBodyPart(fileBodyPart);
				}

			}

			message.setContent(multipart);

			Transport.send(message);

			return true;

		} catch (MessagingException ex) {
			ex.printStackTrace();
			logger.error("邮件发送失败，Error:" + ex.getMessage(), ex);
		}

		return false;
	}
}
