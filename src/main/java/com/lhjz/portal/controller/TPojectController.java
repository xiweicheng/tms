/**
 * 版权所有 (TMS)
 */
package com.lhjz.portal.controller;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.lhjz.portal.base.BaseController;
import com.lhjz.portal.component.MailSender;
import com.lhjz.portal.entity.TProject;
import com.lhjz.portal.entity.security.User;
import com.lhjz.portal.model.RespBody;
import com.lhjz.portal.pojo.Enum.Action;
import com.lhjz.portal.pojo.Enum.Status;
import com.lhjz.portal.pojo.Enum.Target;
import com.lhjz.portal.repository.LogRepository;
import com.lhjz.portal.repository.TProjectRepository;
import com.lhjz.portal.repository.UserRepository;
import com.lhjz.portal.util.AuthUtil;
import com.lhjz.portal.util.WebUtil;

/**
 * 
 * @author xi
 * 
 * @date 2015年3月28日 下午1:19:05
 * 
 */
@Controller
@RequestMapping("admin/task/project")
public class TPojectController extends BaseController {

	static Logger logger = LoggerFactory.getLogger(TPojectController.class);

	@Autowired
	UserRepository userRepository;

	@Autowired
	LogRepository logRepository;

	@Autowired
	TProjectRepository projectRepository;

	@Autowired
	MailSender mailSender;

	@RequestMapping(value = "create", method = RequestMethod.POST)
	@ResponseBody
	public RespBody create(@RequestParam("name") String name, @RequestParam("key") String key,
			@RequestParam("leader") String leader, @RequestParam("operator") String operator,
			@RequestParam(value = "website", required = false) String website,
			@RequestParam(value = "description", required = false) String description) {

		if (!isAdmin()) {
			return RespBody.failed("非管理员权限不能创建项目!");
		}

		if (!key.matches("^[A-Z]{2,10}$")) {
			return RespBody.failed("项目标识必须是2到10位A-Z字母组合!");
		}

		TProject project = new TProject();
		project.setName(name);
		project.setKey(key);
		project.setLeader(getUser(leader));
		project.setOperator(getUser(operator));
		project.setWebsite(website);
		project.setDescription(description);

		TProject project2 = projectRepository.saveAndFlush(project);

		return RespBody.succeed(project2);
	}

	@RequestMapping(value = "list", method = RequestMethod.GET)
	@ResponseBody
	public RespBody list() {

		if (!isSuper()) {
			return RespBody.failed("权限不足!");
		}

		List<TProject> projects = projectRepository.findByStatusNot(Status.Deleted);

		return RespBody.succeed(projects);
	}

	@RequestMapping(value = "listMy", method = RequestMethod.GET)
	@ResponseBody
	public RespBody listMy() {

		List<TProject> projects = projectRepository.findByStatusNot(Status.Deleted).stream()
				.filter(p -> AuthUtil.hasTProjectAuth(p)).collect(Collectors.toList());

		return RespBody.succeed(projects);
	}

	@RequestMapping(value = "update", method = RequestMethod.POST)
	@ResponseBody
	public RespBody update(@RequestParam("id") Long id, @RequestParam("name") String name,
			@RequestParam("leader") String leader, @RequestParam("operator") String operator,
			@RequestParam(value = "website", required = false) String website,
			@RequestParam(value = "description", required = false) String description) {

		TProject project = projectRepository.findOne(id);

		if (!AuthUtil.isTProjectLeader(project) && !isSuperOrCreator(project.getCreator())) {
			return RespBody.failed("权限不足!");
		}

		project.setName(name);
		project.setLeader(getUser(leader));
		project.setOperator(getUser(operator));
		project.setWebsite(website);
		project.setDescription(description);

		TProject project2 = projectRepository.saveAndFlush(project);

		return RespBody.succeed(project2);

	}

	@RequestMapping(value = "delete", method = RequestMethod.POST)
	@ResponseBody
	public RespBody delete(@RequestParam("id") Long id) {

		TProject project = projectRepository.findOne(id);

		if (!AuthUtil.isTProjectLeader(project) && !isSuperOrCreator(project.getCreator())) {
			return RespBody.failed("权限不足!");
		}

		project.setStatus(Status.Deleted);

		projectRepository.saveAndFlush(project);

		log(Action.Delete, Target.TPoject, id, project.getName());

		return RespBody.succeed(id);
	}

	@RequestMapping(value = "get", method = RequestMethod.GET)
	@ResponseBody
	public RespBody get(@RequestParam("id") Long id) {

		TProject project = projectRepository.findOne(id);

		if (!AuthUtil.hasTProjectAuth(project)) {
			return RespBody.failed("权限不足!");
		}

		return RespBody.succeed(project);
	}

	@PostMapping("member/add")
	@ResponseBody
	public RespBody addProjectMember(@RequestParam("id") Long id, @RequestParam("users") String users) {

		TProject project = projectRepository.findOne(id);

		Stream.of(users.split(",")).forEach(u -> {
			User user = userRepository.findOne(u);
			Set<TProject> projects = user.getJoinTProjects();
			if (!projects.contains(project)) {
				projects.add(project);

				userRepository.saveAndFlush(user);
				project.getMembers().add(user);
			}
		});

		return RespBody.succeed(project);
	}

	@PostMapping("member/remove")
	@ResponseBody
	public RespBody removeProjectMember(@RequestParam("id") Long id, @RequestParam("users") String users) {

		TProject project = projectRepository.findOne(id);

		Stream.of(users.split(",")).forEach(u -> {
			User user = userRepository.findOne(u);
			Set<TProject> projects = user.getJoinTProjects();
			if (projects.contains(project)) {
				projects.remove(project);

				userRepository.saveAndFlush(user);
				project.getMembers().remove(user);
			}
		});

		return RespBody.succeed(project);
	}

}
