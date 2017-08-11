/**
 * 版权所有 (TMS)
 */
package com.lhjz.portal.controller;

import java.util.Date;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.lhjz.portal.base.BaseController;
import com.lhjz.portal.component.MailSender;
import com.lhjz.portal.entity.TProject;
import com.lhjz.portal.entity.TEpic;
import com.lhjz.portal.model.RespBody;
import com.lhjz.portal.pojo.Enum.Action;
import com.lhjz.portal.pojo.Enum.Status;
import com.lhjz.portal.pojo.Enum.Target;
import com.lhjz.portal.repository.LogRepository;
import com.lhjz.portal.repository.TProjectRepository;
import com.lhjz.portal.repository.TEpicRepository;
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
@RequestMapping("admin/task/epic")
public class TEpicController extends BaseController {

	static Logger logger = LoggerFactory.getLogger(TEpicController.class);

	@Autowired
	UserRepository userRepository;

	@Autowired
	LogRepository logRepository;

	@Autowired
	TProjectRepository projectRepository;

	@Autowired
	TEpicRepository epicRepository;

	@Autowired
	MailSender mailSender;

	@RequestMapping(value = "create", method = RequestMethod.POST)
	@ResponseBody
	public RespBody create(@RequestParam("name") String name, @RequestParam("pid") Long pid,
			@RequestParam(value = "description", required = false) String description,
			@RequestParam(value = "start", required = false) Date start,
			@RequestParam(value = "end", required = false) Date end) {

		if (!isAdmin()) {
			return RespBody.failed("权限不足!");
		}

		TProject project = projectRepository.findOne(pid);

		TEpic epic = epicRepository.findOneByStatusNotAndProjectAndName(Status.Deleted, project, name);

		if (epic != null) {
			return RespBody.failed("该项目下同名史诗已经存在!");
		}
		epic = new TEpic();
		epic.setName(name);
		epic.setDescription(description);
		epic.setStartDate(start);
		epic.setEndDate(end);
		epic.setProject(project);

		TEpic epic2 = epicRepository.saveAndFlush(epic);

		return RespBody.succeed(epic2);
	}

	@RequestMapping(value = "listByProject", method = RequestMethod.GET)
	@ResponseBody
	public RespBody listByProject(@RequestParam("pid") Long pid) {

		TProject project = projectRepository.findOne(pid);

		if (!AuthUtil.hasTProjectAuth(project)) {
			return RespBody.failed("权限不足!");
		}

		List<TEpic> epics = epicRepository.findByStatusNotAndProject(Status.Deleted, project);

		return RespBody.succeed(epics);
	}

	@RequestMapping(value = "update", method = RequestMethod.POST)
	@ResponseBody
	public RespBody update(@RequestParam("id") Long id, @RequestParam(value = "name", required = false) String name,
			@RequestParam(value = "description", required = false) String description,
			@RequestParam(value = "start", required = false) Date start,
			@RequestParam(value = "end", required = false) Date end) {

		TEpic epic = epicRepository.findOne(id);

		if (!AuthUtil.isTProjectLeader(epic.getProject()) && !isSuperOrCreator(WebUtil.getUsername())) {
			return RespBody.failed("权限不足!");
		}

		if (name != null) {
			epic.setName(name);
		}
		if (description != null) {
			epic.setDescription(description);
		}
		if (start != null) {
			epic.setStartDate(start);
		}
		if (end != null) {
			epic.setEndDate(end);
		}

		TEpic epic2 = epicRepository.saveAndFlush(epic);

		return RespBody.succeed(epic2);

	}

	@RequestMapping(value = "delete", method = RequestMethod.POST)
	@ResponseBody
	public RespBody delete(@RequestParam("id") Long id) {

		TEpic epic = epicRepository.findOne(id);

		if (!AuthUtil.isTProjectLeader(epic.getProject()) && !isSuperOrCreator(WebUtil.getUsername())) {
			return RespBody.failed("权限不足!");
		}

		epic.setStatus(Status.Deleted);

		epicRepository.saveAndFlush(epic);

		log(Action.Delete, Target.TEpic, id, epic.getName());

		return RespBody.succeed(id);
	}

	@RequestMapping(value = "get", method = RequestMethod.GET)
	@ResponseBody
	public RespBody get(@RequestParam("id") Long id) {

		TEpic epic = epicRepository.findOne(id);

		if (!AuthUtil.hasTProjectAuth(epic.getProject())) {
			return RespBody.failed("权限不足!");
		}

		return RespBody.succeed(epic);
	}

}
