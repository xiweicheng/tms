/**
 * 版权所有 (TMS)
 */
package com.lhjz.portal.controller;

import java.util.List;
import java.util.stream.Collectors;

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
import com.lhjz.portal.util.WebUtil;

/**
 * 
 * @author xi
 * 
 * @date 2015年3月28日 下午1:19:05
 * 
 */
@Controller
@RequestMapping("admin/task")
public class TaskController extends BaseController {

	static Logger logger = LoggerFactory.getLogger(TaskController.class);

	// @Value("${tms.blog.upload.path}")
	// private String uploadPath;
	//
	// @Value("${tms.blog.md2pdf.path}")
	// private String md2pdfPath;

	@Autowired
	UserRepository userRepository;

	@Autowired
	LogRepository logRepository;

	@Autowired
	TProjectRepository projectRepository;

	@Autowired
	MailSender mailSender;

	@RequestMapping(value = "project/create", method = RequestMethod.POST)
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

	private boolean hasAuth(TProject project) {
		if (project == null) {
			return false;
		}

		if (isSuper()) { // 超级用户
			return true;
		}

		if (project.getStatus().equals(Status.Deleted)) { // 过滤掉删除的
			return false;
		}

		User loginUser = new User(WebUtil.getUsername());

		// 过滤掉没有权限的
		if (project.getCreator().equals(loginUser)) { // creator
			return true;
		}
		// 过滤掉没有权限的
		if (project.getLeader().equals(loginUser)) { // leader
			return true;
		}

		return project.getMembers().contains(loginUser);
	}

	@RequestMapping(value = "listMy", method = RequestMethod.GET)
	@ResponseBody
	public RespBody listMy() {

		List<TProject> projects = projectRepository.findByStatusNot(Status.Deleted).stream().filter(p -> hasAuth(p))
				.collect(Collectors.toList());

		return RespBody.succeed(projects);
	}

	private boolean isLeader(TProject project) {
		return new User(WebUtil.getUsername()).equals(project.getLeader());
	}

	@RequestMapping(value = "update", method = RequestMethod.POST)
	@ResponseBody
	public RespBody update(@RequestParam("id") Long id, @RequestParam("name") String name,
			@RequestParam("leader") String leader, @RequestParam("operator") String operator,
			@RequestParam(value = "website", required = false) String website,
			@RequestParam(value = "description", required = false) String description) {

		TProject project = projectRepository.findOne(id);

		if (!isLeader(project) && !isSuperOrCreator(WebUtil.getUsername())) {
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

		if (!isLeader(project) && !isSuperOrCreator(WebUtil.getUsername())) {
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

		if (!hasAuth(project)) {
			return RespBody.failed("权限不足!");
		}

		return RespBody.succeed(project);
	}
	
	@PostMapping("project/member/add")
	@ResponseBody
	public RespBody addProjectMember(@RequestParam("id") Long id) {
		
		
		return RespBody.succeed();
	}

}
