/**
 * 版权所有 (TMS)
 */
package com.lhjz.portal.controller;

import java.io.UnsupportedEncodingException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.mail.MessagingException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.lhjz.portal.base.BaseController;
import com.lhjz.portal.component.MailSender;
import com.lhjz.portal.entity.Setting;
import com.lhjz.portal.model.Mail;
import com.lhjz.portal.model.RespBody;
import com.lhjz.portal.pojo.Enum.SettingType;
import com.lhjz.portal.pojo.Enum.Status;
import com.lhjz.portal.repository.LogRepository;
import com.lhjz.portal.repository.SettingRepository;
import com.lhjz.portal.util.JsonUtil;
import com.lhjz.portal.util.StringUtil;

/**
 * 
 * @author xi
 * 
 * @date 2015年3月28日 下午1:19:05
 * 
 */
@Controller
@RequestMapping("admin/setting")
public class SettingController extends BaseController {

	static Logger logger = LoggerFactory.getLogger(SettingController.class);

	@Autowired
	LogRepository logRepository;

	@Autowired
	SettingRepository settingRepository;

	@Autowired
	MailSender mailSender;

	@Value("${lhjz.mail.to.addresses}")
	private String toAddrArr;

	@RequestMapping(value = "mail/test", method = RequestMethod.POST)
	@ResponseBody
	@Secured({ "ROLE_SUPER", "ROLE_ADMIN" })
	public RespBody testMail(@RequestParam("host") String host,
			@RequestParam("port") int port,
			@RequestParam("username") String username,
			@RequestParam("password") String password,
			@RequestParam(value = "addr", required = true) String addr) {

		if (StringUtil.isEmpty(host)) {
			return RespBody.failed("主机不能为空");
		}

		if (StringUtil.isEmpty(username)) {
			return RespBody.failed("用户名不能为空");
		}

		if (StringUtil.isEmpty(password)) {
			return RespBody.failed("密码不能为空");
		}

		if (StringUtil.isEmpty(addr)) {
			return RespBody.failed("测试邮箱不能为空");
		}

		JavaMailSenderImpl sender = mailSender.getMailSender();

		sender.setHost(host);
		sender.setPort(port);
		sender.setUsername(username);
		sender.setPassword(password);
		sender.setDefaultEncoding("UTF-8");

		final Mail mail = Mail.instance().add(StringUtil.split(addr, ","));

		try {
			boolean sendSts = mailSender.sendHtml(
					"邮箱服务配置测试邮件-" + System.currentTimeMillis(),
					"恭喜您,邮箱服务配置成功!", null, mail.get());
			logger.info("邮箱服务配置测试邮件发送成功！");

			if (sendSts) {
				return RespBody.succeed();
			} else {
				return RespBody.failed();
			}
		} catch (MessagingException | UnsupportedEncodingException e) {
			e.printStackTrace();
			logger.error("邮箱服务配置测试邮件发送失败！");
			return RespBody.failed(e.getMessage());
		}
	}

	@RequestMapping(value = "mail/createOrUpdate", method = RequestMethod.POST)
	@ResponseBody
	@Secured({ "ROLE_SUPER", "ROLE_ADMIN" })
	public RespBody createOrUpdateMail(@RequestParam("host") String host,
			@RequestParam("port") int port,
			@RequestParam("username") String username,
			@RequestParam("password") String password,
			@RequestParam(value = "addr", required = false) String addr) {

		if (StringUtil.isEmpty(host)) {
			return RespBody.failed("主机不能为空");
		}

		if (StringUtil.isEmpty(username)) {
			return RespBody.failed("用户名不能为空");
		}

		if (StringUtil.isEmpty(password)) {
			return RespBody.failed("密码不能为空");
		}

		JavaMailSenderImpl sender = mailSender.getMailSender();

		sender.setHost(host);
		sender.setPort(port);
		sender.setUsername(username);
		sender.setPassword(password);
		sender.setDefaultEncoding("UTF-8");

		Map<String, Object> mailSettings = new HashMap<String, Object>();
		mailSettings.put("host", host);
		mailSettings.put("port", String.valueOf(port));
		mailSettings.put("username", username);
		mailSettings.put("password", password);

		Setting setting = settingRepository
				.findOneBySettingType(SettingType.Mail);

		if (setting == null) {
			setting = new Setting();
			setting.setContent(JsonUtil.toJson(mailSettings));
			setting.setCreateDate(new Date());
			setting.setCreator(getLoginUser());
			setting.setSettingType(SettingType.Mail);
			setting.setStatus(Status.New);
		} else {
			setting.setContent(JsonUtil.toJson(mailSettings));
			setting.setUpdateDate(new Date());
			setting.setUpdater(getLoginUser());
			setting.setStatus(Status.Updated);
		}

		settingRepository.saveAndFlush(setting);

		final Mail mail = Mail.instance().addUsers(getLoginUser())
				.add(StringUtil.split(toAddrArr, ","))
				.add(StringUtil.split(addr, ","));

		try {
			mailSender.sendHtmlByQueue("邮箱服务配置测试邮件-" + System.currentTimeMillis(), "恭喜您,邮箱服务配置成功!", null, mail.get());
		} catch (Exception e) {
			e.printStackTrace();
		}

		return RespBody.succeed();
	}

	@SuppressWarnings("unchecked")
	@RequestMapping(value = "mail/getOpts", method = RequestMethod.GET)
	@ResponseBody
	@Secured({ "ROLE_SUPER", "ROLE_ADMIN" })
	public RespBody getMailOpts() {

		Setting setting = settingRepository
				.findOneBySettingType(SettingType.Mail);

		if (setting == null) {

			JavaMailSenderImpl sender = mailSender.getMailSender();

			Map<String, Object> mailSettings = new HashMap<String, Object>();
			mailSettings.put("host", sender.getHost());
			mailSettings.put("port", sender.getPort());
			mailSettings.put("username", sender.getUsername());
			mailSettings.put("password", "");

			return RespBody.succeed(mailSettings);
		} else {
			Map<String, Object> mailSettings = JsonUtil.json2Object(
					setting.getContent(),
					Map.class);

			mailSettings.put("password", "");

			return RespBody.succeed(mailSettings);
		}

	}
}
