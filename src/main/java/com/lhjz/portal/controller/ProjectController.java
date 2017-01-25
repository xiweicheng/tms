/**
 * 版权所有 (TMS)
 */
package com.lhjz.portal.controller;

import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import javax.validation.Valid;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.lhjz.portal.base.BaseController;
import com.lhjz.portal.entity.Label;
import com.lhjz.portal.entity.Language;
import com.lhjz.portal.entity.Project;
import com.lhjz.portal.entity.Translate;
import com.lhjz.portal.entity.TranslateItem;
import com.lhjz.portal.entity.TranslateItemHistory;
import com.lhjz.portal.entity.security.User;
import com.lhjz.portal.model.RespBody;
import com.lhjz.portal.pojo.Enum.Action;
import com.lhjz.portal.pojo.Enum.Status;
import com.lhjz.portal.pojo.Enum.Target;
import com.lhjz.portal.pojo.ProjectForm;
import com.lhjz.portal.repository.AuthorityRepository;
import com.lhjz.portal.repository.LabelRepository;
import com.lhjz.portal.repository.LanguageRepository;
import com.lhjz.portal.repository.ProjectRepository;
import com.lhjz.portal.repository.TranslateItemHistoryRepository;
import com.lhjz.portal.repository.TranslateItemRepository;
import com.lhjz.portal.repository.TranslateRepository;
import com.lhjz.portal.repository.UserRepository;
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
@RequestMapping("admin/project")
public class ProjectController extends BaseController {

	static Logger logger = LoggerFactory.getLogger(ProjectController.class);

	@Autowired
	TranslateRepository translateRepository;

	@Autowired
	TranslateItemRepository translateItemRepository;
	
	@Autowired
	TranslateItemHistoryRepository translateItemHistoryRepository;

	@Autowired
	ProjectRepository projectRepository;

	@Autowired
	LanguageRepository languageRepository;

	@Autowired
	LabelRepository labelRepository;

	@Autowired
	UserRepository userRepository;

	@Autowired
	AuthorityRepository authorityRepository;

	private boolean isContainsMainLanguage(ProjectForm projectForm) {

		String[] lngs = projectForm.getLanguages().split(",");
		for (String lng : lngs) {
			if (lng.equals(String.valueOf(projectForm.getLanguage()))) {
				return true;
			}
		}
		return false;
	}

	@RequestMapping(value = "create", method = RequestMethod.POST)
	@ResponseBody
	@Secured({ "ROLE_SUPER", "ROLE_ADMIN" })
	public RespBody create(@Valid ProjectForm projectForm,
			BindingResult bindingResult) {

		if (bindingResult.hasErrors()) {
			return RespBody.failed(bindingResult.getAllErrors().stream()
					.map(err -> err.getDefaultMessage())
					.collect(Collectors.joining("<br/>")));
		}

		Project project3 = projectRepository.findOneByName(projectForm
				.getName());
		if (project3 != null) {
			return RespBody.failed("同名项目已经存在!");
		}

		if (!isContainsMainLanguage(projectForm)) {
			return RespBody.failed("设置语言必须包含主语言!");
		}

		Project project = new Project();
		project.setCreateDate(new Date());
		project.setCreator(WebUtil.getUsername());
		project.setDescription(projectForm.getDesc());
		project.setName(projectForm.getName());
		project.setStatus(Status.New);
		project.setLanguage(languageRepository.findOne(projectForm
				.getLanguage()));

		// 项目语言保存
		String[] lngArr = projectForm.getLanguages().split(",");
		List<Long> collect = Arrays.asList(lngArr).stream().map((lng) -> {
			return Long.valueOf(lng);
		}).collect(Collectors.toList());

		List<Language> languages = languageRepository.findAll(collect);

		project.getLanguages().addAll(languages);

		// 项目关注者保存
		List<User> watchers = null;
		if (StringUtil.isNotEmpty(projectForm.getWatchers())) {

			watchers = userRepository.findAll(Arrays.asList(projectForm
					.getWatchers().split(",")));
			project.getWatchers().addAll(watchers);

		}

		Project project2 = projectRepository.saveAndFlush(project);

		// 保存项目语言关系
		for (Language language : languages) {
			language.getProjects().add(project2);
		}

		languageRepository.save(languages);
		languageRepository.flush();

		// 保存项目关注者关系
		if (watchers != null) {
			for (User user : watchers) {
				user.getWatcherProjects().add(project2);
			}

			userRepository.save(watchers);
			userRepository.flush();
		}

		log(Action.Create, Target.Project, project2.getId());

		return RespBody.succeed(project2);
	}

	private boolean isExistLanguage(Set<Language> lngs, Language lng) {

		for (Language language : lngs) {
			if (language.getId().equals(lng.getId())) {
				return true;
			}
		}
		return false;
	}

	private boolean isExistWatcher(Set<User> watchers, User user) {

		for (User watcher : watchers) {
			if (watcher.getUsername().equals(user.getUsername())) {
				return true;
			}
		}
		return false;
	}

	@RequestMapping(value = "update", method = RequestMethod.POST)
	@ResponseBody
	@Secured({ "ROLE_SUPER", "ROLE_ADMIN" })
	public RespBody update(@RequestParam("id") Long id,
			@Valid ProjectForm projectForm, BindingResult bindingResult) {

		if (bindingResult.hasErrors()) {
			return RespBody.failed(bindingResult.getAllErrors().stream()
					.map(err -> err.getDefaultMessage())
					.collect(Collectors.joining("<br/>")));
		}

		if (!isContainsMainLanguage(projectForm)) {
			return RespBody.failed("设置语言必须包含主语言!");
		}

		Project project = projectRepository.findOne(id);

		project.setDescription(projectForm.getDesc());

		Project project2 = projectRepository.findOneByName(projectForm
				.getName());
		if (project2 == null) {
			project.setName(projectForm.getName());
		}

		project.setStatus(Status.Updated);
		project.setUpdateDate(new Date());
		project.setUpdater(WebUtil.getUsername());

		// 主语言如果变化,则保存
		if (!project.getLanguage().getId().equals(projectForm.getLanguage())) {
			project.setLanguage(languageRepository.findOne(projectForm
					.getLanguage()));
		}

		// 项目语言保存
		String[] lngArr = projectForm.getLanguages().split(",");
		List<Long> collect = Arrays.asList(lngArr).stream().map((lng) -> {
			return Long.valueOf(lng);
		}).collect(Collectors.toList());

		Set<Language> languages = project.getLanguages();
		for (Language language : languages) {
			// 语言不存在,被删除了
			if (!collect.contains(language.getId())) {
				language.getProjects().remove(project);
			}
		}

		HashSet<Language> languages2 = new HashSet<Language>(
				languageRepository.findAll(collect));

		for (Language language : languages2) {
			// 新添加的语言
			if (!isExistLanguage(languages, language)) {
				language.getProjects().add(project);
			}
		}

		languageRepository.save(languages);
		languageRepository.save(languages2);
		languageRepository.flush();

		if (StringUtil.isNotEmpty(projectForm.getWatchers())) {
			List<String> watchers = Arrays.asList(projectForm.getWatchers()
					.split(","));

			Set<User> watchers2 = project.getWatchers();
			for (User user : watchers2) {
				if (!watchers.contains(user.getUsername())) {
					user.getWatcherProjects().remove(project);
				}
			}

			List<User> watcher3 = userRepository.findAll(watchers);
			for (User user : watcher3) {
				if (!isExistWatcher(watchers2, user)) {
					user.getWatcherProjects().add(project);
				}
			}

			userRepository.save(watchers2);
			userRepository.save(watcher3);
			languageRepository.flush();

			project.setWatchers(new HashSet<User>(watcher3));

		} else { // 删除全部关注者
			project.getWatchers().stream().forEach((w) -> {
				w.getWatcherProjects().remove(project);
			});
			userRepository.save(project.getWatchers());
			userRepository.flush();

			project.getWatchers().clear();
		}

		project.setLanguages(languages2);

		projectRepository.saveAndFlush(project);

		log(Action.Update, Target.Project, project.getId());

		return RespBody.succeed(project);
	}

	@RequestMapping(value = "delete", method = RequestMethod.POST)
	@ResponseBody
	@Secured({ "ROLE_SUPER", "ROLE_ADMIN" })
	public RespBody delete(@RequestParam("id") Long id) {

		if (WebUtil.isRememberMeAuthenticated()) {
			return RespBody.failed("因为当前是通过[记住我]登录,为了安全需要,请退出重新登录再尝试删除项目!");
		}

		Project project = projectRepository.findOne(id);

		if (project == null) {
			return RespBody.failed("删除项目不存在！");
		}

		// 解除项目&语言关系
		Set<Language> languages = project.getLanguages();
		for (Language language : languages) {
			language.getProjects().remove(project);
		}

		languageRepository.save(languages);
		languageRepository.flush();

		// 删除项目下全部翻译
		Set<Translate> translates = project.getTranslates();
		Set<TranslateItem> translateItems = new HashSet<TranslateItem>();
		Set<TranslateItemHistory> translateItemHistories = new HashSet<TranslateItemHistory>();
		Set<Label> labels = new HashSet<Label>();
		translates.stream().forEach((t) -> {
			Set<TranslateItem> translateItems2 = t.getTranslateItems();
			translateItems2.stream().forEach((ti) -> {
				ti.setTranslate(null);
				
				Set<TranslateItemHistory> translateItemHistories2 = ti.getTranslateItemHistories();
				translateItemHistories2.stream().forEach((h -> {
					h.setTranslateItem(null);
				}));
				translateItemHistories.addAll(translateItemHistories2);
			});
			translateItems.addAll(translateItems2);

			Set<Label> labels2 = t.getLabels();
			labels2.stream().forEach((l) -> {
				l.setTranslate(null);
			});
			labels.addAll(labels2);

			t.setProject(null);
		});

		translateItemHistoryRepository.deleteInBatch(translateItemHistories);
		translateItemHistoryRepository.flush();
		
		translateItemRepository.deleteInBatch(translateItems);
		translateItemRepository.flush();

		labelRepository.deleteInBatch(labels);
		labelRepository.flush();

		// 解除项目关注者和用户关系
		Set<User> watchers = project.getWatchers();
		for (User user : watchers) {
			user.getWatcherProjects().remove(project);
		}
		userRepository.save(watchers);
		userRepository.flush();

		// 解除项目用户&项目关系
		Set<User> users = project.getUsers();
		users.stream().forEach((u) -> {
			u.getProjects().remove(project);
		});
		userRepository.save(users);
		userRepository.flush();

		translateRepository.deleteInBatch(translates);
		translateRepository.flush();

		// 待关系都解除后,删除项目
		projectRepository.delete(project);
		projectRepository.flush();

		log(Action.Delete, Target.Project, id);

		return RespBody.succeed(id);
	}

	@RequestMapping(value = "deleteWatcher", method = RequestMethod.POST)
	@ResponseBody
	@Secured({ "ROLE_SUPER", "ROLE_ADMIN", "ROLE_USER" })
	public RespBody deleteWatcher(@RequestParam("id") Long id,
			@RequestParam("username") String username) {

		Project project = projectRepository.findOne(id);
		if (project == null) {
			return RespBody.failed("项目不存在！");
		}

		User watcher = userRepository.findOne(username);
		if (watcher == null) {
			return RespBody.failed("关注者用户不存在！");
		}

		watcher.getWatcherProjects().remove(project);
		project.getWatchers().remove(watcher);

		userRepository.saveAndFlush(watcher);

		projectRepository.saveAndFlush(project);

		log(Action.Update, Target.Project, project.getId());

		return RespBody.succeed(id);
	}

	@RequestMapping(value = "get", method = RequestMethod.GET)
	@ResponseBody
	@Secured({ "ROLE_SUPER", "ROLE_ADMIN" })
	public RespBody get(@RequestParam("id") Long id) {

		Project project = projectRepository.findOne(id);

		if (project == null) {
			return RespBody.failed("获取项目不存在！");
		}

		log(Action.Read, Target.Project, id);

		return RespBody.succeed(project);
	}

}
