/**
 * 版权所有 (TMS)
 */
package com.lhjz.portal.controller;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.Cache.ValueWrapper;
import org.springframework.cache.CacheManager;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.google.common.collect.Lists;
import com.lhjz.portal.base.BaseController;
import com.lhjz.portal.component.MailSender;
import com.lhjz.portal.constant.SysConstant;
import com.lhjz.portal.entity.security.Authority;
import com.lhjz.portal.entity.security.AuthorityId;
import com.lhjz.portal.entity.security.Group;
import com.lhjz.portal.entity.security.GroupMember;
import com.lhjz.portal.entity.security.User;
import com.lhjz.portal.model.Mail;
import com.lhjz.portal.model.MailAddr;
import com.lhjz.portal.model.OnlineUser;
import com.lhjz.portal.model.RespBody;
import com.lhjz.portal.pojo.Enum.Action;
import com.lhjz.portal.pojo.Enum.OnlineStatus;
import com.lhjz.portal.pojo.Enum.Role;
import com.lhjz.portal.pojo.Enum.Status;
import com.lhjz.portal.pojo.Enum.Target;
import com.lhjz.portal.pojo.GroupForm;
import com.lhjz.portal.pojo.UserForm;
import com.lhjz.portal.repository.AuthorityRepository;
import com.lhjz.portal.repository.GroupMemberRepository;
import com.lhjz.portal.repository.GroupRepository;
import com.lhjz.portal.repository.UserRepository;
import com.lhjz.portal.service.ChannelService;
import com.lhjz.portal.util.DateUtil;
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
@Controller
@RequestMapping("admin/user")
public class UserController extends BaseController {

	static Logger logger = LoggerFactory.getLogger(UserController.class);

	@Autowired
	UserRepository userRepository;

	@Autowired
	AuthorityRepository authorityRepository;

	@Autowired
	PasswordEncoder passwordEncoder;

	@Autowired
	MailSender mailSender;

	@Autowired
	GroupRepository groupRepository;

	@Autowired
	GroupMemberRepository groupMemberRepository;

	@Autowired
	ChannelService channelService;

	@Autowired
	CacheManager cacheManager;

	@Value("${tms.base.url}")
	private String baseUrl;

	String loginAction = "admin/login";

	private RespBody createUser(String role, String baseURL, UserForm userForm) {

		if (userRepository.exists(StringUtils.trim(userForm.getUsername()))) {
			logger.error("添加用户已经存在, ID: {}", StringUtils.trim(userForm.getUsername()));
			return RespBody.failed("添加用户已经存在!");
		}

		// @all 使用
		if ("all".equalsIgnoreCase(userForm.getUsername())) {
			return RespBody.failed("该用户名不能使用,请更换其它用户名!");
		}

		// save username and password
		final User user = new User();
		user.setUsername(StringUtils.trim(userForm.getUsername()));
		user.setPassword(passwordEncoder.encode(StringUtils.trim(userForm.getPassword())));
		user.setEnabled(userForm.getEnabled());
		user.setCreateDate(new Date());
		user.setName(StringUtils.trim(userForm.getName()));
		user.setMails(StringUtils.trim(userForm.getMail()));
		user.setCreator(WebUtil.getUsername());

		User user2 = userRepository.saveAndFlush(user);

		log(Action.Create, Target.User, user.getUsername());

		// save default authority `ROLE_USER`
		Authority authority = new Authority();
		authority.setId(new AuthorityId(StringUtils.trim(userForm.getUsername()), Role.ROLE_USER.name()));

		authorityRepository.saveAndFlush(authority);

		log(Action.Create, Target.Authority, authority.getId().toString());

		if (role.equalsIgnoreCase("admin")) {
			Authority authority2 = new Authority();
			authority2.setId(new AuthorityId(StringUtils.trim(userForm.getUsername()), Role.ROLE_ADMIN.name()));

			authorityRepository.saveAndFlush(authority2);

			log(Action.Create, Target.Authority, authority2.getId().toString());
		}

		channelService.joinAll(user2);

		final String userRole = role;

		final String href = baseURL;

		final UserForm userForm2 = userForm;

		final String loginUrl = baseURL + loginAction + "?username=" + userForm.getUsername() + "&password="
				+ userForm.getPassword();

		try {
			mailSender
					.sendHtmlByQueue(String.format("TMS-用户创建_%s", DateUtil.format(new Date(), DateUtil.FORMAT7)),
							TemplateUtil.process("templates/mail/user-create",
									MapUtil.objArr2Map("user", userForm2, "userRole", userRole, "href", href,
											"loginUrl", loginUrl, "baseUrl", baseUrl)),
							new MailAddr(user.getMails(), user.getName()));
		} catch (Exception e) {
			e.printStackTrace();
		}

		return RespBody.succeed(user.getUsername());
	}

	@RequestMapping(value = "batchCreate", method = RequestMethod.POST)
	@ResponseBody
	@Secured({ "ROLE_ADMIN" })
	public RespBody batchCreate(@RequestParam("baseURL") String baseURL, @RequestParam("data") String data) {

		// test,88888,user,测试用户,test@test.com,true
		String[] lines = data.split("\n");
		int cnt = 0;
		for (String line : lines) {
			String[] infos = line.trim().split(",");
			if (infos.length >= 5) {
				UserForm userForm = new UserForm();
				userForm.setEnabled(true);
				userForm.setMail(StringUtils.trim(infos[4]));
				userForm.setName(StringUtils.trim(infos[3]));
				userForm.setPassword(StringUtils.trim(infos[1]));
				userForm.setUsername(StringUtils.trim(infos[0]));
				userForm.setEnabled("true".equalsIgnoreCase(StringUtils.trim(infos[5])) ? true : false);

				RespBody respBody = createUser(StringUtils.trim(infos[2]), baseURL, userForm);
				if (respBody.isSuccess()) {
					cnt = cnt + 1;
				}
			}
		}

		if (cnt == 0) {
			return RespBody.failed();
		}

		return RespBody.succeed();
	}

	@RequestMapping(value = "batchMail", method = RequestMethod.POST)
	@ResponseBody
	@Secured({ "ROLE_SUPER", "ROLE_ADMIN", "ROLE_USER" })
	public RespBody batchMail(@RequestParam("baseURL") String baseURL, @RequestParam("users") String users,
			@RequestParam("title") String title, @RequestParam("content") String content) {

		if (StringUtil.isEmpty(users)) {
			return RespBody.failed("发送用户不能为空!");
		}

		if (StringUtil.isEmpty(title)) {
			return RespBody.failed("发送标题不能为空!");
		}

		if (StringUtil.isEmpty(content)) {
			return RespBody.failed("发送内容不能为空!");
		}

		String[] usernames = users.split(",");
		List<User> users2 = userRepository.findAll();
		final Mail mail = Mail.instance();
		for (String username : usernames) {
			for (User user : users2) {
				if (user.getUsername().equals(username)) {
					mail.addUsers(user);
					break;
				}
			}
		}

		final User loginUser = getLoginUser();
		final String href = baseURL;
		final String title1 = title;
		final String content1 = content;

		try {
			mailSender.sendHtmlByQueue(String.format("TMS-系统消息_%s", DateUtil.format(new Date(), DateUtil.FORMAT7)),
					TemplateUtil.process("templates/mail/mail-msg", MapUtil.objArr2Map("user", loginUser, "date",
							new Date(), "href", href, "title", title1, "content", content1)),
					getLoginUserName(loginUser), mail.get());
		} catch (Exception e) {
			e.printStackTrace();
		}

		return RespBody.succeed();
	}

	@RequestMapping(value = "save", method = RequestMethod.POST)
	@ResponseBody
	@Secured({ "ROLE_ADMIN" })
	public RespBody save(@RequestParam("role") String role, @RequestParam("baseURL") String baseURL,
			@Valid UserForm userForm, BindingResult bindingResult) {

		if (bindingResult.hasErrors()) {
			return RespBody.failed(bindingResult.getAllErrors().stream().map(err -> err.getDefaultMessage())
					.collect(Collectors.joining("<br/>")));
		}

		return createUser(role, baseURL, userForm);
	}

	@RequestMapping(value = "update", method = RequestMethod.POST)
	@ResponseBody
	@Secured({ "ROLE_ADMIN" })
	public RespBody update(@RequestParam(value = "role", required = false) String role, @Valid UserForm userForm,
			BindingResult bindingResult) {

		if (WebUtil.isRememberMeAuthenticated()) {
			return RespBody.failed("因为当前是通过[记住我]登录,为了安全需要,请退出重新登录再尝试修改用户信息!");
		}

		if (bindingResult.hasErrors()) {
			return RespBody.failed(bindingResult.getAllErrors().stream().map(err -> err.getDefaultMessage())
					.collect(Collectors.joining("<br/>")));
		}

		User user = userRepository.findOne(userForm.getUsername());

		if (user == null) {
			logger.error("更新用户不存在! ID: {}", userForm.getUsername());
			return RespBody.failed("更新用户不存在!");
		}

		if (Boolean.TRUE.equals(user.getLocked()) && !isSuper()) {
			return RespBody.failed("用户信息被锁定,不能修改!");
		}

		if (StringUtil.isNotEmpty(userForm.getPassword())) {

			if (userForm.getPassword().length() < 6) {
				logger.error("修改密码长度小于六位, ID: {}", userForm.getUsername());
				return RespBody.failed("修改密码长度不能小于六位!");
			}

			user.setPassword(passwordEncoder.encode(userForm.getPassword()));
		}

		if (userForm.getEnabled() != null && user.getStatus() != Status.Bultin) {
			user.setEnabled(userForm.getEnabled());
		}

		if (userForm.getMail() != null) {
			user.setMails(userForm.getMail());
		}

		if (userForm.getName() != null) {
			user.setName(userForm.getName());
		}

		if (StringUtil.isNotEmpty(role)) {

			// 删除当前的权限
			Set<Authority> authorities = user.getAuthorities();
			authorities.stream().forEach((auth) -> {
				auth.setUser(null);
			});
			authorityRepository.delete(authorities);
			authorityRepository.flush();

			user.getAuthorities().clear();

			// 附加新的权限
			// add role_user
			Authority authority = new Authority();
			authority.setId(new AuthorityId(StringUtils.trim(userForm.getUsername()), Role.ROLE_USER.name()));
			authority.setUser(user);

			Authority saveAndFlush = authorityRepository.saveAndFlush(authority);

			user.getAuthorities().add(saveAndFlush);

			// add role_user
			if ("admin".equalsIgnoreCase(role)) {
				Authority authority2 = new Authority();
				authority2.setId(new AuthorityId(StringUtils.trim(userForm.getUsername()), Role.ROLE_ADMIN.name()));
				authority2.setUser(user);

				Authority saveAndFlush2 = authorityRepository.saveAndFlush(authority2);

				user.getAuthorities().add(saveAndFlush2);
			}

		}

		userRepository.saveAndFlush(user);

		log(Action.Update, Target.User, user.getUsername());

		return RespBody.succeed(user.getUsername());
	}

	@RequestMapping(value = "update2", method = RequestMethod.POST)
	@ResponseBody
	@Secured({ "ROLE_USER" })
	public RespBody update2(HttpServletRequest request, @Valid UserForm userForm, BindingResult bindingResult) {

		if (WebUtil.isRememberMeAuthenticated()) {
			return RespBody.failed("因为当前是通过[记住我]登录,为了安全需要,请退出重新登录再尝试修改用户信息!");
		}

		if (bindingResult.hasErrors()) {
			return RespBody.failed(bindingResult.getAllErrors().stream().map(err -> err.getDefaultMessage())
					.collect(Collectors.joining("<br/>")));
		}

		if (!WebUtil.getUsername().equals(userForm.getUsername())) {
			logger.error("普通用户无权限修改其他用户信息!");
			return RespBody.failed("普通用户无权限修改其他用户信息!");
		}

		User user = userRepository.findOne(userForm.getUsername());

		if (user == null) {
			logger.error("更新用户不存在! ID: {}", userForm.getUsername());
			return RespBody.failed("更新用户不存在!");
		}

		if (Boolean.TRUE.equals(user.getLocked()) && !isSuper()) {
			return RespBody.failed("用户信息被锁定,不能修改!");
		}

		if (StringUtil.isNotEmpty(userForm.getPassword())) {

			if (userForm.getPassword().length() < 6) {
				logger.error("修改密码长度小于六位, ID: {}", userForm.getUsername());
				return RespBody.failed("修改密码长度不能小于六位!");
			}

			user.setPassword(passwordEncoder.encode(userForm.getPassword()));
		}

		if (userForm.getEnabled() != null && user.getStatus() != Status.Bultin) {
			user.setEnabled(userForm.getEnabled());
		}

		if (userForm.getMail() != null) {
			user.setMails(userForm.getMail());
		}

		if (userForm.getName() != null) {
			user.setName(userForm.getName());
		}

		userRepository.saveAndFlush(user);

		log(Action.Update, Target.User, user.getUsername());

		return RespBody.succeed(user.getUsername());
	}

	@RequestMapping(value = "delete", method = RequestMethod.POST)
	@ResponseBody
	@Secured({ "ROLE_ADMIN" })
	public RespBody delete(@RequestParam("username") String username) {

		if (WebUtil.isRememberMeAuthenticated()) {
			return RespBody.failed("因为当前是通过[记住我]登录,为了安全需要,请退出重新登录再尝试删除用户信息!");
		}

		User user = userRepository.findOne(username);

		if (user == null) {
			logger.error("删除用户不存在! ID: {}", username);
			return RespBody.failed("删除用户不存在!");
		}

		if (user.getStatus() == Status.Bultin) {
			logger.error("内置用户,不能删除! ID: {}", username);
			return RespBody.failed("内置用户,不能删除!");
		}

		user.setEnabled(false);
		user.setStatus(Status.Deleted);

		userRepository.saveAndFlush(user);

		log(Action.Delete, Target.User, user.getUsername(), user);

		return RespBody.succeed(username);
	}

	@RequestMapping(value = "get", method = RequestMethod.GET)
	@ResponseBody
	@Secured({ "ROLE_ADMIN", "ROLE_USER" })
	public RespBody get(@RequestParam("username") String username) {

		User user = userRepository.findOne(username);

		if (user == null) {
			logger.error("查询用户不存在! ID: {}", username);
			return RespBody.failed("查询用户不存在!");
		}

		List<GroupMember> gms = groupMemberRepository.findByUsername(username);
		List<String> gns = gms.stream().map((gm) -> {
			return gm.getGroup().getGroupName();
		}).collect(Collectors.toList());

		setOnlineStatus(user);

		return RespBody.succeed(user).addMsg(gns);
	}

	@RequestMapping(value = "loginUser", method = RequestMethod.GET)
	@ResponseBody
	@Secured({ "ROLE_ADMIN", "ROLE_USER" })
	public RespBody loginUser() {

		return RespBody.succeed(getLoginUser());
	}

	@RequestMapping(value = "all", method = RequestMethod.GET)
	@ResponseBody
	@Secured({ "ROLE_ADMIN", "ROLE_USER" })
	public RespBody getAllUsers(@RequestParam(value = "enabled", required = false) Boolean enabled) {

		List<User> users = null;

		if (enabled != null) {
			users = userRepository.findByEnabled(enabled);
		} else {
			users = userRepository.findAll();
		}

		users.forEach(this::setOnlineStatus);

		return RespBody.succeed(users);
	}

	@RequestMapping(value = "online", method = RequestMethod.GET)
	@ResponseBody
	@Secured({ "ROLE_ADMIN", "ROLE_USER" })
	public RespBody getOnlineUsers() {

		List<OnlineUser> users = Lists.newArrayList();
		try {
			@SuppressWarnings("unchecked")
			ConcurrentHashMap<Object, Object> cache = (ConcurrentHashMap<Object, Object>) cacheManager
					.getCache(SysConstant.ONLINE_USERS).getNativeCache();

			cache.forEachKey(1, key -> {
				users.add(new OnlineUser(String.valueOf(key),
						(Date) cacheManager.getCache(SysConstant.ONLINE_USERS).get(key).get()));
			});
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}

		return RespBody.succeed(users);
	}

	private void setOnlineStatus(User user) {
		ValueWrapper valueWrapper = cacheManager.getCache(SysConstant.ONLINE_USERS).get(user.getUsername());
		if (valueWrapper != null && valueWrapper.get() != null) {
			user.setOnlineStatus(OnlineStatus.Online);
			user.setOnlineDate((Date) valueWrapper.get());
		} else {
			if (user.getUsername().equals(WebUtil.getUsername())) {
				user.setOnlineStatus(OnlineStatus.Online);
				user.setOnlineDate(new Date());
			}
		}
	}

	@RequestMapping(value = "getGroup", method = RequestMethod.GET)
	@ResponseBody
	@Secured({ "ROLE_ADMIN", "ROLE_USER" })
	public RespBody getGroup(@RequestParam("groupName") String groupName) {

		List<Group> groups = groupRepository.findByGroupName(groupName);

		if (groups.size() == 0) {
			logger.error("查询用户组不存在! ID: {}", groupName);
			return RespBody.failed("查询用户组不存在!");
		}

		return RespBody.succeed(groups.get(0));
	}

	@RequestMapping(value = "groups", method = RequestMethod.GET)
	@ResponseBody
	@Secured({ "ROLE_ADMIN", "ROLE_USER" })
	public RespBody getGroups() {

		List<Group> groups = groupRepository.findAll();

		return RespBody.succeed(groups);
	}

	@RequestMapping(value = "groupMemembers", method = RequestMethod.GET)
	@ResponseBody
	@Secured({ "ROLE_ADMIN", "ROLE_USER" })
	public RespBody groupMemembers(@RequestParam("groupId") Long groupId) {

		Group group = groupRepository.findOne(groupId);

		if (group == null) {
			logger.error("用户组不存在! ID: {}", groupId);
			return RespBody.failed("用户组不存在!");
		}

		List<GroupMember> groupMembers = groupMemberRepository.findByGroup(group);

		return RespBody.succeed(groupMembers);
	}

	@RequestMapping(value = "createGroup", method = RequestMethod.POST)
	@ResponseBody
	@Secured({ "ROLE_ADMIN", "ROLE_USER" })
	public RespBody createGroup(@Valid GroupForm groupForm, BindingResult bindingResult) {

		if (bindingResult.hasErrors()) {
			return RespBody.failed(bindingResult.getAllErrors().stream().map(err -> err.getDefaultMessage())
					.collect(Collectors.joining("<br/>")));
		}

		List<Group> groups = groupRepository.findByGroupName(groupForm.getGroupName());
		if (groups.size() > 0) {
			return RespBody.failed("该用户组已经存在!");
		}

		Group group = new Group(groupForm.getGroupName());
		group.setCreateDate(new Date());
		group.setCreator(WebUtil.getUsername());
		group.setStatus(Status.New);

		Group group2 = groupRepository.saveAndFlush(group);

		return RespBody.succeed(group2);
	}

	@RequestMapping(value = "updateGroup", method = RequestMethod.POST)
	@ResponseBody
	@Secured({ "ROLE_ADMIN", "ROLE_USER" })
	public RespBody updateGroup(@RequestParam("groupId") Long groupId, @RequestParam("groupName") String groupName) {

		Group group = groupRepository.findOne(groupId);

		if (group == null) {
			logger.error("用户组不存在! ID: {}", groupId);
			return RespBody.failed("用户组不存在!");
		}

		group.setUpdateDate(new Date());
		group.setUpdater(WebUtil.getUsername());
		group.setStatus(Status.Updated);

		group.setGroupName(groupName);

		Group group2 = groupRepository.saveAndFlush(group);

		return RespBody.succeed(group2);
	}

	@RequestMapping(value = "deleteGroup", method = RequestMethod.POST)
	@ResponseBody
	@Secured({ "ROLE_ADMIN", "ROLE_USER" })
	public RespBody deleteGroup(@RequestParam("groupId") Long groupId) {

		Group group = groupRepository.findOne(groupId);

		if (group == null) {
			logger.error("用户组不存在! ID: {}", groupId);
			return RespBody.failed("用户组不存在!");
		}

		List<GroupMember> groupMembers = groupMemberRepository.findByGroup(group);

		groupMemberRepository.deleteInBatch(groupMembers);
		groupMemberRepository.flush();

		groupRepository.delete(group);
		groupRepository.flush();

		return RespBody.succeed(groupId);
	}

	@RequestMapping(value = "addGroupMembers", method = RequestMethod.POST)
	@ResponseBody
	@Secured({ "ROLE_ADMIN", "ROLE_USER" })
	public RespBody addGroupMembers(@RequestParam("groupId") Long groupId,
			@RequestParam("usernames") String usernames) {

		Group group = groupRepository.findOne(groupId);

		if (group == null) {
			logger.error("用户组不存在! ID: {}", groupId);
			return RespBody.failed("用户组不存在!");
		}

		String[] usernameArr = usernames.split(",");

		List<GroupMember> groupMembers = new ArrayList<>();
		Stream.of(usernameArr).forEach((un) -> {

			List<GroupMember> gms = groupMemberRepository.findByGroupAndUsername(group, un);
			if (gms.size() == 0) {
				GroupMember gm = new GroupMember(group, un);
				gm.setCreateDate(new Date());
				gm.setCreator(WebUtil.getUsername());
				gm.setStatus(Status.New);

				groupMembers.add(gm);
			}
		});

		List<GroupMember> groupMembers2 = groupMemberRepository.save(groupMembers);
		groupMemberRepository.flush();

		return RespBody.succeed(groupMembers2);
	}

	@RequestMapping(value = "deleteGroupMembers", method = RequestMethod.POST)
	@ResponseBody
	@Secured({ "ROLE_ADMIN", "ROLE_USER" })
	public RespBody deleteGroupMembers(@RequestParam("groupId") Long groupId,
			@RequestParam("usernames") String usernames) {

		Group group = groupRepository.findOne(groupId);

		if (group == null) {
			logger.error("用户组不存在! ID: {}", groupId);
			return RespBody.failed("用户组不存在!");
		}

		String[] usernameArr = usernames.split(",");

		List<GroupMember> groupMembers = new ArrayList<>();
		Stream.of(usernameArr).forEach((un) -> {
			List<GroupMember> gms = groupMemberRepository.findByGroupAndUsername(group, un);
			groupMembers.addAll(gms);
		});

		groupMemberRepository.deleteInBatch(groupMembers);
		groupMemberRepository.flush();

		return RespBody.succeed();
	}

	@RequestMapping(value = "updateGroupMembers", method = RequestMethod.POST)
	@ResponseBody
	@Secured({ "ROLE_ADMIN", "ROLE_USER" })
	public RespBody updateGroupMembers(@RequestParam("groupId") Long groupId,
			@RequestParam("usernames") String usernames) {

		Group group = groupRepository.findOne(groupId);

		if (group == null) {
			logger.error("用户组不存在! ID: {}", groupId);
			return RespBody.failed("用户组不存在!");
		}

		List<GroupMember> groupMembers2 = groupMemberRepository.findByGroup(group);

		groupMemberRepository.deleteInBatch(groupMembers2);
		groupMemberRepository.flush();

		String[] usernameArr = usernames.split(",");

		List<GroupMember> groupMembers = new ArrayList<>();
		Stream.of(usernameArr).forEach((un) -> {

			List<GroupMember> gms = groupMemberRepository.findByGroupAndUsername(group, un);
			if (gms.size() == 0) {
				GroupMember gm = new GroupMember(group, un);
				gm.setCreateDate(new Date());
				gm.setCreator(WebUtil.getUsername());
				gm.setStatus(Status.New);

				groupMembers.add(gm);
			}
		});

		List<GroupMember> groupMembers3 = groupMemberRepository.save(groupMembers);
		groupMemberRepository.flush();

		return RespBody.succeed(groupMembers3);
	}
}
