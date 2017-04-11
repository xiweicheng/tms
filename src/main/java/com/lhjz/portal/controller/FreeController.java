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
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.jayway.jsonpath.JsonPath;
import com.lhjz.portal.base.BaseController;
import com.lhjz.portal.component.MailSender2;
import com.lhjz.portal.constant.SysConstant;
import com.lhjz.portal.entity.Channel;
import com.lhjz.portal.entity.ChatChannel;
import com.lhjz.portal.entity.security.Authority;
import com.lhjz.portal.entity.security.AuthorityId;
import com.lhjz.portal.entity.security.User;
import com.lhjz.portal.model.Mail;
import com.lhjz.portal.model.RespBody;
import com.lhjz.portal.pojo.Enum.Role;
import com.lhjz.portal.pojo.Enum.Status;
import com.lhjz.portal.repository.AuthorityRepository;
import com.lhjz.portal.repository.ChannelRepository;
import com.lhjz.portal.repository.ChatChannelRepository;
import com.lhjz.portal.repository.UserRepository;
import com.lhjz.portal.util.DateUtil;
import com.lhjz.portal.util.MapUtil;
import com.lhjz.portal.util.StringUtil;
import com.lhjz.portal.util.TemplateUtil;
import com.lhjz.portal.util.ThreadUtil;

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
	ChannelRepository channelRepository;
	
	@Autowired
	ChatChannelRepository chatChannelRepository;
	
	@Autowired
	Environment env;
	
	@Value("${tms.base.url}")
	private String baseUrl;
	
	@Value("${tms.token.jira}")
	private String tokenJira;

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
	
	@RequestMapping(value = "channel/send/{token}", method = RequestMethod.POST)
	@ResponseBody
	public RespBody sendChannelMsg(@RequestParam("channel") String channel, @RequestParam("user") String user,
			@PathVariable("token") String token,
			@RequestParam(value = "mail", required = false, defaultValue = "false") Boolean mail,
			@RequestBody String reqBody) {

		if (!tokenJira.equals(token)) {
			return RespBody.failed("Token认证失败!");
		}

		Channel channel2 = channelRepository.findOneByName(channel);
		if (channel2 == null) {
			return RespBody.failed("发送消息目的频道不存在!");
		}

		User user2 = userRepository.findOne(user);
		if (user2 == null) {
			return RespBody.failed("用户不存在不存在!");
		}

		String webhookEvent = JsonPath.read(reqBody, "$.webhookEvent");

		if (!"jira:issue_created".equals(webhookEvent)) {
			return RespBody.failed("不支持处理类型!");
		}

		String issueSelf = JsonPath.read(reqBody, "$.issue.self");
		String issueKey = JsonPath.read(reqBody, "$.issue.key");
		String description = JsonPath.read(reqBody, "$.issue.fields.description");
		String summary = JsonPath.read(reqBody, "$.issue.fields.summary");
		String creatorSelf = JsonPath.read(reqBody, "$.issue.fields.creator.self");
		String creatorName = JsonPath.read(reqBody, "$.issue.fields.creator.displayName");
		String avatarUrls = JsonPath.read(reqBody, "$.issue.fields.creator.avatarUrls.16x16");
		
		String issuetype = JsonPath.read(reqBody, "$.issue.fields.issuetype.name");
		String issuetypeIconUrl = JsonPath.read(reqBody, "$.issue.fields.issuetype.iconUrl");

		String issueUrl = StringUtil.parseUrl(issueSelf) + "/browse/" + issueKey;

		StringBuffer sb = new StringBuffer();
		sb.append("## JIRA状态报告").append(SysConstant.NEW_LINE);
		sb.append(StringUtil.replace("> ![{?1}]({?5}) [{?1}]({?2})  创建了 ![]({?6}) {?7}: [{?3}]({?4})", creatorName,
				creatorSelf, issueKey, issueUrl, avatarUrls, issuetypeIconUrl, issuetype)).append(SysConstant.NEW_LINE);
		sb.append("**内容:** " + summary).append(SysConstant.NEW_LINE);
		sb.append("**描述:** " + (description != null ? description : "")).append(SysConstant.NEW_LINE);
		
		try {
			String assigneeName = JsonPath.read(reqBody, "$.issue.fields.assignee.displayName");
			String assigneeAvatarUrls = JsonPath.read(reqBody, "$.issue.fields.assignee.avatarUrls.16x16");

			sb.append("**分配给:** " + StringUtil.replace("![]({?1}) {?2}", assigneeAvatarUrls, assigneeName))
					.append(SysConstant.NEW_LINE);
		} catch (Exception e1) {
		}
		
		try {
			String priority = JsonPath.read(reqBody, "$.issue.fields.priority.name");
			String priorityIconUrl = JsonPath.read(reqBody, "$.issue.fields.priority.iconUrl");
			sb.append("**优先级:** " + StringUtil.replace("![]({?1}) {?2}", priorityIconUrl, priority))
					.append(SysConstant.NEW_LINE);
		} catch (Exception e1) {
		}
		
		sb.append("> ").append(SysConstant.NEW_LINE);
		sb.append(StringUtil.replace("[{?1}]({?2})", "点击查看更多该票相关信息", issueSelf)).append(SysConstant.NEW_LINE);

		ChatChannel chatChannel = new ChatChannel();
		chatChannel.setChannel(channel2);
		chatChannel.setContent(sb.toString());

		ChatChannel chatChannel2 = chatChannelRepository.saveAndFlush(chatChannel);
		chatChannelRepository.updateAuditing(user2, user2, new Date(), new Date(), chatChannel2.getId());

		if (mail) {
			final Mail mail2 = Mail.instance();
			final User loginUser = getLoginUser();
			final String href = baseUrl + "/page/index.html#/chat/" + channel + "?id=" + chatChannel2.getId();
			channel2.getMembers().forEach(item -> mail2.addUsers(item));

			final String html = StringUtil.md2Html(sb.toString());

			ThreadUtil.exec(() -> {

				try {
					Thread.sleep(3000);
					mailSender.sendHtml(
							String.format("TMS-来自第三方应用推送的@消息_%s", DateUtil.format(new Date(), DateUtil.FORMAT7)),
							TemplateUtil.process("templates/mail/mail-dynamic", MapUtil.objArr2Map("user", loginUser,
									"date", new Date(), "href", href, "title", "来自第三方应用推送的消息有@到你", "content", html)),
							mail2.get());
					logger.info("沟通频道来自第三方应用推送的消息邮件发送成功！");
				} catch (Exception e) {
					e.printStackTrace();
					logger.error("沟通频道来自第三方应用推送的消息邮件发送失败！");
				}

			});
		}

		return RespBody.succeed();
	}
}
