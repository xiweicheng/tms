/**
 * 版权所有 (TMS)
 */
package com.lhjz.portal.controller;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.web.PageableDefault;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.lhjz.portal.base.BaseController;
import com.lhjz.portal.component.MailSender2;
import com.lhjz.portal.entity.Chat;
import com.lhjz.portal.entity.Label;
import com.lhjz.portal.entity.Language;
import com.lhjz.portal.entity.Log;
import com.lhjz.portal.entity.Project;
import com.lhjz.portal.entity.Setting;
import com.lhjz.portal.entity.Translate;
import com.lhjz.portal.entity.security.Authority;
import com.lhjz.portal.entity.security.Group;
import com.lhjz.portal.entity.security.User;
import com.lhjz.portal.model.RespBody;
import com.lhjz.portal.model.UserInfo;
import com.lhjz.portal.pojo.Enum.SettingType;
import com.lhjz.portal.pojo.Enum.Status;
import com.lhjz.portal.pojo.Enum.Target;
import com.lhjz.portal.repository.BlogRepository;
import com.lhjz.portal.repository.ChannelRepository;
import com.lhjz.portal.repository.ChatRepository;
import com.lhjz.portal.repository.FileRepository;
import com.lhjz.portal.repository.GroupRepository;
import com.lhjz.portal.repository.LabelRepository;
import com.lhjz.portal.repository.LanguageRepository;
import com.lhjz.portal.repository.LogRepository;
import com.lhjz.portal.repository.ProjectRepository;
import com.lhjz.portal.repository.SettingRepository;
import com.lhjz.portal.repository.TranslateRepository;
import com.lhjz.portal.repository.UserRepository;
import com.lhjz.portal.util.CollectionUtil;
import com.lhjz.portal.util.JsonUtil;
import com.lhjz.portal.util.StringUtil;
import com.lhjz.portal.util.WebUtil;

/**
 * 
 * @author xi
 * 
 * @date 2015年3月28日 下午1:19:05
 * 
 */
@Controller
@RequestMapping("admin")
public class AdminController extends BaseController {

	static final Logger logger = LoggerFactory.getLogger(AdminController.class);

	@Autowired
	FileRepository fileRepository;

	@Autowired
	ProjectRepository projectRepository;

	@Autowired
	LanguageRepository languageRepository;

	@Autowired
	TranslateRepository translateRepository;

	@Autowired
	UserRepository userRepository;

	@Autowired
	LabelRepository labelRepository;
	
	@Autowired
	ChatRepository chatRepository;

	@Autowired
	LogRepository logRepository;

	@Autowired
	GroupRepository groupRepository;

	@Autowired
	SettingRepository settingRepository;
	
	@Autowired
	ChannelRepository channelRepository;
	
	@Autowired
	BlogRepository blogRepository;

	@Autowired
	MailSender2 mailSender;

	@RequestMapping("login")
	public String login(Model model) {

		logger.debug("Enter method: {}", "login");

		return "admin/login";
	}
	
	@RequestMapping("health")
	@ResponseBody
	public RespBody health() {
		return RespBody.succeed();
	}

	@RequestMapping()
	public String home(Model model) {

		model.addAttribute("cntProject", projectRepository.count());
		model.addAttribute("cntUser", userRepository.count());
		model.addAttribute("cntLanguage", languageRepository.count());
		model.addAttribute("cntTranslate", translateRepository.count());
		model.addAttribute("cntChat", chatRepository.count());
		model.addAttribute("cntChannel", channelRepository.countChannels());
		model.addAttribute("cntBlog", blogRepository.countBlogs());

		return "admin/index";
	}

	@RequestMapping("user")
	public String user(Model model) {

		logger.debug("Enter method...");

		List<User> users = userRepository.findAll();
		List<UserInfo> userInfos = new ArrayList<UserInfo>();
		for (User user : users) {
			UserInfo userInfo = new UserInfo();
			userInfo.setCreateDate(user.getCreateDate());
			userInfo.setEnabled(user.isEnabled());
			userInfo.setStatus(user.getStatus());
			userInfo.setUsername(user.getUsername());
			userInfo.setMails(user.getMails());
			userInfo.setName(user.getName());
			userInfo.setLastLoginDate(user.getLastLoginDate());
			userInfo.setLoginCount(user.getLoginCount());
			userInfo.setLoginRemoteAddress(user.getLoginRemoteAddress());

			Set<String> authorities = new HashSet<String>();
			for (Authority authority : user.getAuthorities()) {
				authorities.add(authority.getId().getAuthority());
			}

			userInfo.setAuthorities(authorities);

			userInfos.add(userInfo);
		}

		List<Group> groups = groupRepository.findAll();

		model.addAttribute("users", userInfos);
		model.addAttribute("groups", groups);
		model.addAttribute("loginUser", getLoginUser());

		return "admin/user";
	}

	@RequestMapping("project")
	public String project(Model model) {

		logger.debug("Enter method...");

		List<Project> projects = projectRepository.findAll();

		List<Language> languages = languageRepository.findAll();

		List<User> users = userRepository.findAll();

		model.addAttribute("projects", projects);
		model.addAttribute("languages", languages);
		model.addAttribute("users", users);
		model.addAttribute("user", getLoginUser());

		return "admin/project";
	}

	@RequestMapping("language")
	public String language(Model model) {

		logger.debug("Enter method...");

		List<Language> languages = languageRepository.findAll();

		// List<User> users = userRepository.findAll();

		model.addAttribute("languages", languages);
		// model.addAttribute("users", users);
		model.addAttribute("user", getLoginUser());

		return "admin/language";
	}

	@RequestMapping("feedback")
	public String feedback(Model model) {
		return "admin/feedback";
	}
	
	@RequestMapping("dynamic")
	public String dynamic(
			Model model,
			@RequestParam(value = "id", required = false) Long id,
			@RequestParam(value = "search", required = false) String search,
			@PageableDefault(sort = { "createDate" }, direction = Direction.DESC) Pageable pageable) {

		if (StringUtil.isNotEmpty(id)) {
			long cntGtId = chatRepository.countGtId(id);
			int size = pageable.getPageSize();
			long page = cntGtId / size;
			if (cntGtId % size == 0) {
				page--;
			}

			pageable = new PageRequest(page > -1 ? (int) page : 0, size,
					Direction.DESC,
					"createDate");
		}

		Page<Chat> chats = chatRepository.findAll(pageable);
		chats = new PageImpl<Chat>(CollectionUtil.reverseList(chats
				.getContent()), pageable, chats.getTotalElements());

		Page<Log> logs = logRepository.findByTarget(Target.Translate,
				new PageRequest(0, 15, Direction.DESC, "id"));

		List<User> users = userRepository.findAll();
		Collections.sort(users);

		List<Group> groups = groupRepository.findAll();
		Collections.sort(groups);
		
		// login user labels
		List<Label> labels = labelRepository.findByCreatorGroupByName(WebUtil
				.getUsername());
		Set<String> lbls = null;
		if (labels != null) {
			lbls = labels.stream().map((label) -> {
				return label.getName();
			}).collect(Collectors.toSet());
		} else {
			lbls = new HashSet<String>();
		}

		model.addAttribute("chats", chats);
		model.addAttribute("logs", logs);
		model.addAttribute("users", users);
		model.addAttribute("groups", groups);
		model.addAttribute("labels", new TreeSet<>(lbls));
		model.addAttribute("user", getLoginUser());
		
		return "admin/dynamic";
	}

	@RequestMapping("translate")
	public String translate(Model model,
			@PageableDefault(sort = {
					"createDate" }, direction = Direction.DESC) Pageable pageable,
			@RequestParam(value = "projectId", required = false) Long projectId,
			@RequestParam(value = "creator", required = false) String creator,
			@RequestParam(value = "status", required = false) String status,
			@RequestParam(value = "languageId", required = false) Long languageId,
			@RequestParam(value = "id", required = false) Long id,
			@RequestParam(value = "search", required = false) String search) {

		List<Project> projects = projectRepository.findAll();

		if (projects.size() == 0) {
			throw new RuntimeException("系统中不存在项目,请先创建项目后再尝试访问本页面!");
		}

		Set<Language> languages = null;
		Project project = null;
		org.springframework.data.domain.Page<Translate> page = null;
		if (projectId != null) {
			if (projectId != -1) {
				project = projectRepository.findOne(projectId);
			}
		}

		if (project != null) { // 存在检索项目

			languages = project.getLanguages();
			projectId = project.getId();

			if (StringUtil.isNotEmpty(id)) {
				page = translateRepository.findById(id, pageable);
			} else if (StringUtil.isNotEmpty(creator)) {
				page = translateRepository.findByProjectAndCreator(project,
						creator, pageable);
			} else if (StringUtil.isNotEmpty(status)) {
				if (Status.valueOf(status).equals(Status.Updated)) {
					pageable = new PageRequest(pageable.getPageNumber(),
							pageable.getPageSize(), Direction.DESC,
							"updateDate");
				}
				page = translateRepository.findByProjectAndStatus(project,
						Status.valueOf(status), pageable);
			} else if (StringUtil.isNotEmpty(languageId)) {
				long total = translateRepository
						.countUnTranslatedByProject(languageId, projectId);
				List<Translate> unTranslates = translateRepository
						.queryUnTranslatedByProject(languageId, projectId,
								pageable.getOffset(), pageable.getPageSize());
				page = new PageImpl<Translate>(unTranslates, pageable, total);
			} else if (StringUtil.isNotEmpty(search)) {
				String like = "%" + search + "%";
				page = translateRepository.findByProjectAndSearchLike(project,
						like, pageable);
			} else {
				page = translateRepository.findByProject(project, pageable);
			}
		} else { // 不存在指定检索项目,检索全部项目
			projectId = Long.valueOf(-1);
			if (projects.size() > 0) { // 如果不存在项目,也不需要检索翻译,因为翻译是关联到项目的
				languages = projects.get(0).getLanguages();

				if (StringUtil.isNotEmpty(id)) {
					page = translateRepository.findById(id, pageable);
				} else if (StringUtil.isNotEmpty(creator)) {
					page = translateRepository.findByCreator(creator, pageable);
				} else if (StringUtil.isNotEmpty(status)) {
					if (Status.valueOf(status).equals(Status.Updated)) {
						pageable = new PageRequest(pageable.getPageNumber(),
								pageable.getPageSize(), Direction.DESC,
								"updateDate");
					}
					page = translateRepository
							.findByStatus(Status.valueOf(status), pageable);
				} else if (StringUtil.isNotEmpty(languageId)) {
					long total = translateRepository
							.countUnTranslated(languageId);
					List<Translate> unTranslates = translateRepository
							.queryUnTranslated(languageId, pageable.getOffset(),
									pageable.getPageSize());
					page = new PageImpl<Translate>(unTranslates, pageable,
							total);
				} else if (StringUtil.isNotEmpty(search)) {
					String like = "%" + search + "%";
					page = translateRepository.findBySearchLike(like, pageable);
				} else {
					page = translateRepository.findAll(pageable);
				}
			}
		}

		if (page == null) {
			page = new PageImpl<Translate>(new ArrayList<Translate>(), pageable,
					0);
		}

		page.getContent().forEach((t) -> {
			t.getTranslateItems().forEach((ti) -> {
				ti.setTranslateItemHistories(
						new TreeSet<>(ti.getTranslateItemHistories()));
			});
		});

		List<Language> languages2 = new ArrayList<Language>();

		if (languages != null && project != null
				&& project.getLanguage() != null) {
			for (Language language : languages) {
				if (language.getId().equals(project.getLanguage().getId())) { // 主语言放在第一个
					languages2.add(0, language);
				} else {
					languages2.add(language);
				}
			}
		} else {
			languages2.addAll(languages);
		}

		// login user labels
		List<Label> labels = labelRepository
				.findByCreatorGroupByName(WebUtil.getUsername());
		Set<String> lbls = null;
		if (labels != null) {
			lbls = labels.stream().map((label) -> {
				return label.getName();
			}).collect(Collectors.toSet());
		} else {
			lbls = new HashSet<String>();
		}

		model.addAttribute("projects", new TreeSet<>(projects));
		model.addAttribute("project", project);
		model.addAttribute("labels", new TreeSet<>(lbls));
		model.addAttribute("page", page);
		model.addAttribute("languages", languages2);
		model.addAttribute("projectId", projectId);
		model.addAttribute("user", getLoginUser());
		model.addAttribute("users", userRepository.findAll());

		return "admin/translate";
	}

	@RequestMapping("import")
	public String _import(Model model,
			@RequestParam(value = "projectId", required = false) Long projectId) {

		List<Project> projects = projectRepository.findAll();
		Set<Language> languages = null;
		Project project = null;
		if (projectId != null) {
			project = projectRepository.findOne(projectId);
			if (project != null) {
				languages = project.getLanguages();
			} else {
				if (projects.size() > 0) {
					project = projects.get(0);
					projectId = projects.get(0).getId();
					languages = projects.get(0).getLanguages();
				}
			}
		} else {
			if (projects.size() > 0) {
				project = projects.get(0);
				projectId = projects.get(0).getId();
				languages = projects.get(0).getLanguages();
			}
		}

		List<Language> languages2 = new ArrayList<Language>();

		if (languages != null && project != null
				&& project.getLanguage() != null) {
			for (Language language : languages) {
				if (language.getId().equals(project.getLanguage().getId())) {
					languages2.add(0, language);
				} else {
					languages2.add(language);
				}
			}
		} else {
			languages2.addAll(languages);
		}

		// login user labels
		List<Label> labels = labelRepository
				.findByCreatorGroupByName(WebUtil.getUsername());
		Set<String> lbls = null;
		if (labels != null) {
			lbls = labels.stream().map((label) -> {
				return label.getName();
			}).collect(Collectors.toSet());
		} else {
			lbls = new HashSet<String>();
		}

		Set<String> notifiers = new HashSet<>();
		if (project != null) {
			project.getWatchers().forEach((u) -> {
				notifiers.add(u.getUsername());
			});
		}
		notifiers.add(getLoginUser().getUsername());

		model.addAttribute("projects", new TreeSet<>(projects));
		model.addAttribute("languages", languages2);
		model.addAttribute("projectId", projectId);
		model.addAttribute("labels", new TreeSet<>(lbls));
		model.addAttribute("users", userRepository.findAll());
		model.addAttribute("notifiers", StringUtil.join(",", notifiers));

		return "admin/import";
	}

	@SuppressWarnings("unchecked")
	@RequestMapping("setting")
	@Secured({ "ROLE_SUPER", "ROLE_ADMIN" })
	public String setting(Model model) {

		Setting setting = settingRepository
				.findOneBySettingType(SettingType.Mail);

		if (setting == null) {
			JavaMailSenderImpl sender = mailSender.getMailSender();

			Map<String, Object> mailSettings = new HashMap<String, Object>();
			mailSettings.put("host", sender.getHost());
			mailSettings.put("port", sender.getPort());
			mailSettings.put("username", sender.getUsername());
			mailSettings.put("password", "");

			model.addAttribute("mail", mailSettings);
		} else {
			Map<String, Object> mailSettings = JsonUtil.json2Object(
					setting.getContent(),
					Map.class);

			mailSettings.put("password", "");

			model.addAttribute("mail", mailSettings);
		}

		return "admin/setting";
	}

}
