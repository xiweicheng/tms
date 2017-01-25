/**
 * 版权所有 (TMS)
 */
package com.lhjz.portal.controller;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.mail.MessagingException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.lhjz.portal.base.BaseController;
import com.lhjz.portal.component.MailSender2;
import com.lhjz.portal.constant.SysConstant;
import com.lhjz.portal.entity.security.Authority;
import com.lhjz.portal.entity.security.AuthorityId;
import com.lhjz.portal.entity.security.User;
import com.lhjz.portal.model.RespBody;
import com.lhjz.portal.pojo.Enum.Role;
import com.lhjz.portal.pojo.Enum.Status;
import com.lhjz.portal.repository.AuthorityRepository;
import com.lhjz.portal.repository.UserRepository;
import com.lhjz.portal.util.DateUtil;
import com.lhjz.portal.util.StringUtil;

/**
 * 
 * @author xi
 * 
 * @date 2015年3月28日 下午1:19:05
 * 
 */
@RestController
@RequestMapping("free")
public class FreeController extends BaseController {

	static final Logger logger = LoggerFactory.getLogger(FreeController.class);

	@Autowired
	MailSender2 mailSender;

	@Value("${lhjz.mail.to.addresses}")
	private String toAddrArr;

	@Autowired
	UserRepository userRepository;

	@Autowired
	AuthorityRepository authorityRepository;

	@Autowired
	PasswordEncoder passwordEncoder;

	@Autowired
	Environment env;

	@RequestMapping(value = "user/pwd/reset", method = { RequestMethod.POST })
	public RespBody resetUserPwd(@RequestBody Map<String, Object> params) {

		logger.debug(params.toString());

		List<User> users = userRepository.findByMails(params.get("mail")
				.toString());

		if (users.size() == 0) {
			return RespBody.failed("用户不存在!");
		} else if (users.size() > 1) {
			return RespBody.failed("邮箱使用于多个账号,不能通过邮箱重置密码!");
		}

		final User user = users.get(0);

		user.setResetPwdDate(new Date());
		user.setResetPwdToken(UUID.randomUUID().toString());

		userRepository.saveAndFlush(user);

		final String content = StringUtil
				.replaceByKV(
						"<a target='_blank' href='{baseUrl}{path}#/pwd-reset?id={id}'>点击该链接重置密码</a>",
						"baseUrl", params.get("baseUrl"), "path",
						params.get("path"), "id", user.getResetPwdToken());
		boolean sts = false;

		try {
			sts = mailSender.sendHtml(
					String.format("TMS-密码重置_%s",
							DateUtil.format(new Date(), DateUtil.FORMAT7)),
					content, user.getMails());

			logger.info("重置密码邮件发送状态: " + sts);
			return sts ? RespBody.succeed() : RespBody.failed("重置邮件发送失败!");
		} catch (MessagingException e) {
			e.printStackTrace();
			return RespBody.failed("重置邮件发送失败!");
		}

	}

	@RequestMapping(value = "user/pwd/new", method = { RequestMethod.POST })
	public RespBody newUserPwd(@RequestBody Map<String, Object> params) {

		logger.debug(params.toString());

		String pwd = params.get("pwd").toString().trim();

		if (pwd.length() < 8) {
			return RespBody.failed("密码长度需要不少于8位!");
		}

		final User user = userRepository.findOneByResetPwdToken(params.get(
				"token").toString());

		if (user == null) {
			return RespBody.failed("密码重置Token失效!");
		}

		user.setResetPwdToken(SysConstant.EMPTY);
		user.setResetPwdDate(new Date());

		user.setPassword(passwordEncoder.encode(pwd));

		userRepository.saveAndFlush(user);

		return RespBody.succeed();

	}

	@RequestMapping(value = "user/register", method = { RequestMethod.POST })
	public RespBody registerUser(@RequestBody Map<String, Object> params) {

		logger.debug(params.toString());

		// username(唯一行校验 & !all), mail(激活用户), name(可选,没有,设置为username),
		// pwd(长度>=8)

		String username = params.get("username").toString().trim();
		String mail = params.get("mail").toString().trim();
		String name = null;
		String pwd = params.get("pwd").toString().trim();

		User user = userRepository.findOne(username);
		if (user != null || "all".equalsIgnoreCase(username)) {
			return RespBody.failed("用户名已经存在!");
		}
		
		List<User> users = userRepository.findByMails(mail);
		if (users.size() > 0) {
			return RespBody.failed("注册邮箱已经存在!");
		}

		if (StringUtil.isEmpty(params.get("name"))) {
			name = username;
		} else {
			name = params.get("name").toString().trim();
		}

		if (pwd.length() < 8) {
			return RespBody.failed("密码长度需要不少于8位!");
		}

		User newUser = new User();
		newUser.setUsername(username);
		newUser.setMails(mail);
		newUser.setName(name);
		newUser.setPassword(passwordEncoder.encode(pwd));
		newUser.setCreateDate(new Date());
		newUser.setCreator(username);
		newUser.setEnabled(false);
		newUser.setResetPwdDate(new Date());
		newUser.setResetPwdToken(UUID.randomUUID().toString());
		newUser.setStatus(Status.New);

		userRepository.saveAndFlush(newUser);

		Authority authority = new Authority();
		authority.setId(new AuthorityId(username, Role.ROLE_USER.name()));

		authorityRepository.saveAndFlush(authority);

		final String content = StringUtil.replaceByKV(
				"<a target='_blank' href='{baseUrl}{path}#/register?id={id}'>点击该链接激活账户</a>",
				"baseUrl", params.get("baseUrl"), "path", params.get("path"),
				"id", newUser.getResetPwdToken());
		boolean sts = false;

		try {
			sts = mailSender
					.sendHtml(
							String.format("TMS-账户激活_%s",
									DateUtil.format(new Date(),
											DateUtil.FORMAT7)),
							content, newUser.getMails());

			logger.info("激活账户邮件发送状态: " + sts);
			return sts ? RespBody.succeed() : RespBody.failed("激活账户邮件发送失败!");
		} catch (MessagingException e) {
			e.printStackTrace();
			return RespBody.failed("激活账户邮件发送失败!");
		}

	}

	@RequestMapping(value = "user/register/activate", method = {
			RequestMethod.POST })
	public RespBody activateUserRegister(
			@RequestBody Map<String, Object> params) {

		logger.debug(params.toString());

		final User user = userRepository
				.findOneByResetPwdToken(params.get("token").toString());

		if (user == null) {
			return RespBody.failed("账户激活Token失效!");
		}

		user.setResetPwdToken(SysConstant.EMPTY);
		user.setResetPwdDate(new Date());
		user.setEnabled(true);

		userRepository.saveAndFlush(user);

		logger.info("新用户注册成功!");

		if (StringUtil.isNotEmpty(toAddrArr)) {
			try {
				mailSender
						.sendHtml(
								String.format("TMS-新注册用户通知_%s",
										DateUtil.format(new Date(),
												DateUtil.FORMAT7)),
								"注册用户: " + user.getUsername() + " - "
										+ user.getMails(),
								toAddrArr.split(","));

			} catch (MessagingException e) {
				e.printStackTrace();
				logger.warn("新注册用户通知邮件发送失败!");
			}
		}

		return RespBody.succeed();

	}
}
