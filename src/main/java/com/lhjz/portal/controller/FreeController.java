/**
 * 版权所有 (TMS)
 */
package com.lhjz.portal.controller;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.mail.MessagingException;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.io.FileUtils;
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
import com.lhjz.portal.component.MailSender;
import com.lhjz.portal.constant.SysConstant;
import com.lhjz.portal.entity.Channel;
import com.lhjz.portal.entity.ChatChannel;
import com.lhjz.portal.entity.security.Authority;
import com.lhjz.portal.entity.security.AuthorityId;
import com.lhjz.portal.entity.security.User;
import com.lhjz.portal.model.Mail;
import com.lhjz.portal.model.MailAddr;
import com.lhjz.portal.model.RespBody;
import com.lhjz.portal.model.RunAsAuth;
import com.lhjz.portal.pojo.Enum.Action;
import com.lhjz.portal.pojo.Enum.FileType;
import com.lhjz.portal.pojo.Enum.GitAction;
import com.lhjz.portal.pojo.Enum.Role;
import com.lhjz.portal.pojo.Enum.Status;
import com.lhjz.portal.pojo.Enum.Target;
import com.lhjz.portal.pojo.Enum.ToType;
import com.lhjz.portal.repository.AuthorityRepository;
import com.lhjz.portal.repository.ChannelRepository;
import com.lhjz.portal.repository.FileRepository;
import com.lhjz.portal.repository.UserRepository;
import com.lhjz.portal.service.ChannelService;
import com.lhjz.portal.service.ChatChannelService;
import com.lhjz.portal.util.DateUtil;
import com.lhjz.portal.util.ImageUtil;
import com.lhjz.portal.util.JsonUtil;
import com.lhjz.portal.util.MapUtil;
import com.lhjz.portal.util.StringUtil;
import com.lhjz.portal.util.TemplateUtil;
import com.lhjz.portal.util.WebUtil;

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
	MailSender mailSender;

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
	FileRepository fileRepository;
	
	@Autowired
	ChannelService channelService;
	
	@Autowired
	ChatChannelService chatChannelService;
	
	@Autowired
	Environment env;
	
	@Value("${tms.base.url}")
	private String baseUrl;
	
	@Value("${tms.token.jira}")
	private String tokenJira;
	
	@Value("${tms.token.feedback}")
	private String tokenFeedback;
	
	@Value("${tms.git.token}")
	private String gitToken;
	
	@Value("${tms.git.username}")
	private String gitUsername;

	@Value("${tms.alm.token}")
	private String almToken;

	@Value("${tms.alm.username}")
	private String almUsername;

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
					content, null, Mail.instance().addUsers(user).get());

			logger.info("重置密码邮件发送状态: " + sts);
			return sts ? RespBody.succeed() : RespBody.failed("重置邮件发送失败!");
		} catch (MessagingException | UnsupportedEncodingException e) {
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
		
		if (StringUtil.isEmpty(params.get("username"))) {
			return RespBody.failed("注册用户名不能为空!");
		}
		if (StringUtil.isEmpty(params.get("mail"))) {
			return RespBody.failed("注册邮箱不能为空!");
		}
		if (StringUtil.isEmpty(params.get("pwd"))) {
			return RespBody.failed("注册登录密码不能为空!");
		}
		String username = params.get("username").toString().trim();
		String mail = params.get("mail").toString().trim();
		String name = null;
		String pwd = params.get("pwd").toString().trim();

		if (username != null && !username.matches("^[a-z][a-z0-9]{2,49}$")) {
			return RespBody.failed("用户名必须是3到50位小写字母和数字组合,并且以字母开头!");
		}

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

		User user2 = userRepository.saveAndFlush(newUser);

		Authority authority = new Authority();
		authority.setId(new AuthorityId(username, Role.ROLE_USER.name()));

		authorityRepository.saveAndFlush(authority);
		
		channelService.joinAll(user2);

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
							content, new MailAddr(mail, name));

			logger.info("激活账户邮件发送状态: " + sts);
			return sts ? RespBody.succeed() : RespBody.failed("激活账户邮件发送失败!");
		} catch (MessagingException | UnsupportedEncodingException e) {
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
				mailSender.sendHtml(String.format("TMS-新注册用户通知_%s", DateUtil.format(new Date(), DateUtil.FORMAT7)),
						"注册用户: " + user.getUsername() + " - " + user.getMails(),
						null, Mail.instance().add(toAddrArr.split(",")).get());

			} catch (MessagingException | UnsupportedEncodingException e) {
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
			String assigneeSelf = JsonPath.read(reqBody, "$.issue.fields.assignee.self");
			String assigneeAvatarUrls = JsonPath.read(reqBody, "$.issue.fields.assignee.avatarUrls.16x16");

			sb.append("**分配给:** " + StringUtil.replace("![]({?1}) [{?2}]({?3})", assigneeAvatarUrls, assigneeName, assigneeSelf))
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

		RunAsAuth runAsAuth = RunAsAuth.instance().runAs(user);

		ChatChannel chatChannel = new ChatChannel();
		chatChannel.setChannel(channel2);
		chatChannel.setContent(sb.toString());

		ChatChannel chatChannel2 = chatChannelService.save(chatChannel);
		

		final Mail mail2 = Mail.instance();
		
		if (mail) {
			channel2.getMembers().forEach(item -> mail2.addUsers(item));
		}
		
		mail2.addUsers(channel2.getSubscriber(), getLoginUser());
		
		if (!mail2.isEmpty()) {
			final User loginUser = getLoginUser();
			final String href = baseUrl + "/page/index.html#/chat/" + channel + "?id=" + chatChannel2.getId();
			
			final String html = StringUtil.md2Html(sb.toString(), true, true);

			try {
				mailSender.sendHtmlByQueue(
						String.format("TMS-来自第三方应用推送的@消息_%s", DateUtil.format(new Date(), DateUtil.FORMAT7)),
						TemplateUtil.process("templates/mail/mail-dynamic", MapUtil.objArr2Map("user", loginUser,
								"date", new Date(), "href", href, "title", "来自第三方应用推送的消息有@到你", "content", html)),
						getLoginUserName(loginUser), mail2.get());
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
		runAsAuth.rest();

		return RespBody.succeed();
	}
	
	@RequestMapping(value = "{token}/feedback", method = RequestMethod.POST)
	@ResponseBody
	public RespBody feedback(@RequestParam("channel") String channel, @RequestParam("user") String user,
			@PathVariable("token") String token,
			@RequestParam(value = "mail", required = false, defaultValue = "false") Boolean mail,
			@RequestParam(value = "raw", required = false, defaultValue = "false") Boolean raw,
			@RequestParam(value = "format", required = false, defaultValue = "false") Boolean format,
			@RequestBody String reqBody) {

		if (!tokenFeedback.equals(token)) {
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

		String category = JsonPath.read(reqBody, "$.category");
		String content = JsonPath.read(reqBody, "$.content");
		String url = JsonPath.read(reqBody, "$.url");
		
		StringBuffer sb = new StringBuffer();
		sb.append("## 用户工单反馈").append(SysConstant.NEW_LINE);
		
		try {
			String loginName = JsonPath.read(reqBody, "$.user.loginName");
			String realName = JsonPath.read(reqBody, "$.user.realName");
			String email = JsonPath.read(reqBody, "$.user.email");
			String mobile = JsonPath.read(reqBody, "$.user.mobile");

			sb.append(StringUtil.replace("> **反馈用户: ** `{?1}` `{?2}` `{?3}` [`{?4}`](mailto:{?4})", realName, loginName,
					mobile, email)).append(SysConstant.NEW_LINE);

		} catch (Exception e1) {
		}
		
		sb.append("> **环境地址: **" + url).append(SysConstant.NEW_LINE);
		sb.append("> **工单分类: **" + category).append(SysConstant.NEW_LINE);
		sb.append("> **工单内容: **").append(SysConstant.NEW_LINE).append(SysConstant.NEW_LINE);
		sb.append(content).append(SysConstant.NEW_LINE);

		if (raw) {
			sb.append(SysConstant.NEW_LINE);
			sb.append("---").append(SysConstant.NEW_LINE);
			sb.append("> **完整内容:** ").append(SysConstant.NEW_LINE);
			sb.append("```").append(SysConstant.NEW_LINE);
			sb.append(format ? JsonUtil.toPrettyJson(reqBody) : reqBody);
			sb.append("```").append(SysConstant.NEW_LINE);
		}

		RunAsAuth runAsAuth = RunAsAuth.instance().runAs(user);

		ChatChannel chatChannel = new ChatChannel();
		chatChannel.setChannel(channel2);
		chatChannel.setContent(sb.toString());

		ChatChannel chatChannel2 = chatChannelService.save(chatChannel);
		
		final Mail mail2 = Mail.instance();
		
		if (mail) {
			channel2.getMembers().forEach(item -> mail2.addUsers(item));
		}
		
		mail2.addUsers(channel2.getSubscriber(), getLoginUser());
		
		if (!mail2.isEmpty()) {

			final User loginUser = getLoginUser();
			final String href = baseUrl + "/page/index.html#/chat/" + channel + "?id=" + chatChannel2.getId();

			final String html = StringUtil.md2Html(sb.toString(), true, true);

			try {
				mailSender.sendHtmlByQueue(
						String.format("TMS-来自第三方应用推送的@消息_%s", DateUtil.format(new Date(), DateUtil.FORMAT7)),
						TemplateUtil.process("templates/mail/mail-dynamic", MapUtil.objArr2Map("user", loginUser,
								"date", new Date(), "href", href, "title", "来自第三方应用推送的消息有@到你", "content", html)),
						getLoginUserName(loginUser), mail2.get());
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		runAsAuth.rest();

		return RespBody.succeed();
	}

	@RequestMapping(value = "{token}/base64", method = RequestMethod.POST)
	@ResponseBody
	public RespBody base64(HttpServletRequest request, @RequestParam(value = "toType", required = false) String toType, // Feedback
			@PathVariable("token") String token,
			@RequestParam(value = "toId", required = false) String toId, @RequestParam("dataURL") String dataURL,
			@RequestParam("type") String type) {
		
		logger.debug("upload base64 start...");
		
		if (!tokenFeedback.equals(token)) {
			return RespBody.failed("Token认证失败!");
		}

		try {

			String realPath = WebUtil.getRealPath(request);

			String storePath = env.getProperty("lhjz.upload.img.store.path");
			int sizeOriginal = env.getProperty("lhjz.upload.img.scale.size.original", Integer.class);
			int sizeLarge = env.getProperty("lhjz.upload.img.scale.size.large", Integer.class);
			int sizeHuge = env.getProperty("lhjz.upload.img.scale.size.huge", Integer.class);

			// make upload dir if not exists
			FileUtils.forceMkdir(new File(realPath + storePath + sizeOriginal));
			FileUtils.forceMkdir(new File(realPath + storePath + sizeLarge));
			FileUtils.forceMkdir(new File(realPath + storePath + sizeHuge));

			String uuid = UUID.randomUUID().toString();

			String suffix = type.contains("png") ? ".png" : ".jpg";

			String uuidName = StringUtil.replace("{?1}{?2}", uuid, suffix);

			// relative file path
			String path = storePath + sizeOriginal + "/" + uuidName;// 原始图片存放
			String pathLarge = storePath + sizeLarge + "/" + uuidName;// 缩放图片存放
			String pathHuge = storePath + sizeHuge + "/" + uuidName;// 缩放图片存放

			// absolute file path
			String filePath = realPath + path;

			int index = dataURL.indexOf(",");

			// 原始图保存
			ImageUtil.decodeBase64ToImage(dataURL.substring(index + 1), filePath);
			// 缩放图
			// scale image size as thumbnail
			// 图片缩放处理.120*120
			ImageUtil.scale2(filePath, realPath + pathLarge, sizeLarge, sizeLarge, true);
			// 图片缩放处理.640*640
			ImageUtil.scale2(filePath, realPath + pathHuge, sizeHuge, sizeHuge, true);

			// 保存记录到数据库
			com.lhjz.portal.entity.File file2 = new com.lhjz.portal.entity.File();
			file2.setCreateDate(new Date());
			file2.setName(uuidName);
			file2.setUsername(WebUtil.getUsername());
			file2.setUuidName(uuidName);
			file2.setPath(storePath + sizeOriginal + "/");
			file2.setType(FileType.Image);

			if (StringUtil.isNotEmpty(toType)) {
				file2.setToType(ToType.valueOf(toType));
				file2.setToId(toId);
			}

			com.lhjz.portal.entity.File file = fileRepository.save(file2);

			log(Action.Upload, Target.File, file2.getId());

			return RespBody.succeed(file);
		} catch (Exception e) {
			e.printStackTrace();
			logger.error(e.getMessage(), e);
			return RespBody.failed(e.getMessage());
		}

	}
	
	@RequestMapping(value = "channel/git/send", method = RequestMethod.POST)
	@ResponseBody
	public RespBody sendChannelGitMsg(@RequestParam("channel") String channel, @RequestParam("token") String token,
			@RequestParam(value = "mail", required = false, defaultValue = "false") Boolean mail,
			@RequestParam(value = "raw", required = false, defaultValue = "false") Boolean raw,
			@RequestParam(value = "web", required = false) String web, @RequestBody String reqBody) {

		if (!gitToken.equals(token)) {
			return RespBody.failed("参数安全校验token不合法!");
		}

		Channel channel2 = channelRepository.findOneByName(channel);
		if (channel2 == null) {
			return RespBody.failed("发送消息目的频道不存在!");
		}

		String fullName = JsonPath.read(reqBody, "$.header.createdBy.fullName");
		// open：新建合并请求  close：关闭合并请求  accept：接受合并请求
		String action = JsonPath.read(reqBody, "$.header.action");
		String actionName = GitAction.valueOf(action.toUpperCase()).getName();

		Integer id = JsonPath.read(reqBody, "$.merge_request.id");
		String name = JsonPath.read(reqBody, "$.merge_request.name");
		String message = JsonPath.read(reqBody, "$.merge_request.message");
		String sourceBranch = JsonPath.read(reqBody, "$.merge_request.source_branch");
		String targetBranch = JsonPath.read(reqBody, "$.merge_request.target_branch");

		String httpUrl = JsonPath.read(reqBody, "$.repository.httpUrl");

		String repoAddr = StringUtil.EMPTY;

		try {
			URL url = new URL(httpUrl);
			String path = url.getPath(); // /hys_git_test4/439141543.git
			repoAddr = path != null ? path.replace(".git", "") : StringUtil.EMPTY;
		} catch (MalformedURLException e1) {
			logger.error(e1.getMessage(), e1);
		}

		StringBuffer sb = new StringBuffer();
		sb.append("## 代码合并通知").append(SysConstant.NEW_LINE);
		sb.append(StringUtil.replace(
				"> **{?1}** {?2}`{?3}`到`{?4}`的合并请求：http://code.paic.com.cn/#/repo{?5}/merge/{?6}/detail  ", fullName,
				actionName, sourceBranch, targetBranch, repoAddr, id)).append(SysConstant.NEW_LINE);

		if (StringUtil.isNotEmpty(web)) {
			sb.append("> ").append(SysConstant.NEW_LINE);
			sb.append(StringUtil.replace("> [点击此访问web服务]({?1})", web)).append(SysConstant.NEW_LINE);
		}

		if (StringUtil.isNotEmpty(name)) {
			sb.append("> ").append(SysConstant.NEW_LINE);
			sb.append(StringUtil.replace("> **标题：** {?1}  ", name)).append(SysConstant.NEW_LINE);
		}

		if (StringUtil.isNotEmpty(message)) {
			sb.append(StringUtil.replace("> **描述：** {?1}  ", message)).append(SysConstant.NEW_LINE);
		}

		if (raw) {
			sb.append(SysConstant.NEW_LINE);
			sb.append("---").append(SysConstant.NEW_LINE);
			sb.append("> **完整内容:** ").append(SysConstant.NEW_LINE);
			sb.append("```").append(SysConstant.NEW_LINE);
			sb.append(reqBody);
			sb.append("```").append(SysConstant.NEW_LINE);
		}

		RunAsAuth runAsAuth = RunAsAuth.instance().runAs(gitUsername);

		ChatChannel chatChannel = new ChatChannel();
		chatChannel.setChannel(channel2);
		chatChannel.setContent(sb.toString());

		ChatChannel chatChannel2 = chatChannelService.save(chatChannel);

		final Mail mail2 = Mail.instance();

		if (mail) {
			channel2.getMembers().forEach(item -> mail2.addUsers(item));
		}

		mail2.addUsers(channel2.getSubscriber(), getLoginUser());

		if (!mail2.isEmpty()) {

			final User loginUser = getLoginUser();
			final String href = baseUrl + "/page/index.html#/chat/" + channel + "?id=" + chatChannel2.getId();

			final String html = StringUtil.md2Html(sb.toString(), true, true);

			try {
				mailSender
						.sendHtmlByQueue(String.format("TMS-代码合并通知_%s", DateUtil.format(new Date(), DateUtil.FORMAT7)),
								TemplateUtil.process("templates/mail/mail-dynamic",
										MapUtil.objArr2Map("user", loginUser, "date", new Date(), "href", href, "title",
												"代码合并通知消息有@到你", "content", html)),
								getLoginUserName(loginUser), mail2.get());
			} catch (Exception e) {
				e.printStackTrace();
			}

		}

		runAsAuth.rest();

		return RespBody.succeed();
	}

	@RequestMapping(value = "channel/alm/send", method = RequestMethod.POST)
	@ResponseBody
	public RespBody sendChanneAlmMsg(@RequestParam("channel") String channel, @RequestParam("token") String token,
			@RequestParam(value = "mail", required = false, defaultValue = "false") Boolean mail,
			@RequestParam(value = "raw", required = false, defaultValue = "false") Boolean raw,
			@RequestParam(value = "web", required = false) String web, @RequestBody String reqBody) {

		if (!almToken.equals(token)) {
			return RespBody.failed("参数安全校验token不合法!");
		}

		Channel channel2 = channelRepository.findOneByName(channel);
		if (channel2 == null) {
			return RespBody.failed("发送消息目的频道不存在!");
		}
		
		/**
		{
			"path": "http://code.paic.com.cn/sqms/sqms_alm.git?branch=master",
			"deployUsername": "李洋",
			"envType": "STG",
			"envName": "sqms_alm_stg",
			"planName": "SQMS-ALM2.67.0",
			"header": {
				"createdBy": {
					"avatarUrl": "xxx",
					"fullName": "xxx",
					"username": "xx"
				},
				"createdTime": 1539570237360
			},
			"planId": 49094,
			"revision": "f0217835617b88b23f65f2b6e106ddafb74828df",
			"isSuccess": true,
			"commitIds": "xx"
		}
		 **/

		String fullName = JsonPath.read(reqBody, "$.header.createdBy.fullName");
		String planName = JsonPath.read(reqBody, "$.planName");
		String envName = JsonPath.read(reqBody, "$.envName");
		String envType = JsonPath.read(reqBody, "$.envType");
		String deployUsername = JsonPath.read(reqBody, "$.deployUsername");
		
		Integer planId = JsonPath.read(reqBody, "$.planId");
		String revision = JsonPath.read(reqBody, "$.revision");
		Boolean isSuccess = JsonPath.read(reqBody, "$.isSuccess");

		String pathUrl = JsonPath.read(reqBody, "$.path");

		String repoAddr = StringUtil.EMPTY;
		String branch = StringUtil.EMPTY;

		try {
			URL url = new URL(pathUrl);
			String path = url.getPath(); // /hys_git_test4/439141543.git
			repoAddr = path != null ? path.replace(".git", "") : StringUtil.EMPTY;

			branch = url.getQuery().split("=")[1]; // branch=develop

		} catch (MalformedURLException e1) {
			logger.error(e1.getMessage(), e1);
		}

		String icon = isSuccess ? "<i class=\"large green check circle icon\"></i>"
				: "<i class=\"large red remove circle icon\"></i>";

		StringBuffer sb = new StringBuffer();
		sb.append("## 神兵事件通知").append(SysConstant.NEW_LINE);
		sb.append(StringUtil.replace(
				"> {?6}【{?3}】**`{?1}`** [`{?2}`] 部署**{?4}** http://vt.paic.com.cn/pipeline/index.jsp?project_id={?5}  ",
				planName, envName, envType, (isSuccess ? "成功" : "失败"), planId, icon)).append(SysConstant.NEW_LINE);
		sb.append(StringUtil.replace("> ")).append(SysConstant.NEW_LINE);
		sb.append(StringUtil.replace("> **创建用户**：`{?1}`  ", fullName)).append(SysConstant.NEW_LINE);
		sb.append(StringUtil.replace("> **部署用户**：`{?1}`  ", deployUsername)).append(SysConstant.NEW_LINE);
		sb.append(StringUtil.replace(
				"> **部署代码**：http://code.paic.com.cn/#/repo{?1}/{?2}/commit  `{?3}`", repoAddr, branch, revision))
				.append(SysConstant.NEW_LINE);
		sb.append(StringUtil.replace("> **版本标识**：http://code.paic.com.cn/#/repo{?1}/commit/{?2}  ", repoAddr, revision))
				.append(SysConstant.NEW_LINE);

		if (StringUtil.isNotEmpty(web)) {
			sb.append("> ").append(SysConstant.NEW_LINE);
			sb.append(StringUtil.replace("> [点击此访问web服务]({?1})", web)).append(SysConstant.NEW_LINE);
		}

		if (raw) {
			sb.append(SysConstant.NEW_LINE);
			sb.append("---").append(SysConstant.NEW_LINE);
			sb.append("> **完整内容:** ").append(SysConstant.NEW_LINE);
			sb.append("```").append(SysConstant.NEW_LINE);
			sb.append(reqBody);
			sb.append("```").append(SysConstant.NEW_LINE);
		}

		RunAsAuth runAsAuth = RunAsAuth.instance().runAs(almUsername);

		ChatChannel chatChannel = new ChatChannel();
		chatChannel.setChannel(channel2);
		chatChannel.setContent(sb.toString());

		ChatChannel chatChannel2 = chatChannelService.save(chatChannel);

		final Mail mail2 = Mail.instance();

		if (mail) {
			channel2.getMembers().forEach(item -> mail2.addUsers(item));
		}

		mail2.addUsers(channel2.getSubscriber(), getLoginUser());

		if (!mail2.isEmpty()) {

			final User loginUser = getLoginUser();
			final String href = baseUrl + "/page/index.html#/chat/" + channel + "?id=" + chatChannel2.getId();

			final String html = StringUtil.md2Html(sb.toString(), true, true);

			try {
				mailSender
						.sendHtmlByQueue(String.format("TMS-神兵事件通知_%s", DateUtil.format(new Date(), DateUtil.FORMAT7)),
								TemplateUtil.process("templates/mail/mail-dynamic",
										MapUtil.objArr2Map("user", loginUser, "date", new Date(), "href", href, "title",
												"神兵事件通知消息有@到你", "content", html)),
								getLoginUserName(loginUser), mail2.get());
			} catch (Exception e) {
				e.printStackTrace();
			}

		}

		runAsAuth.rest();

		return RespBody.succeed();
	}
}
