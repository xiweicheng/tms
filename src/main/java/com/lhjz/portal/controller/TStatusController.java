/**
 * 版权所有 (TMS)
 */
package com.lhjz.portal.controller;

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
import com.lhjz.portal.entity.TStatus;
import com.lhjz.portal.model.RespBody;
import com.lhjz.portal.pojo.Enum.Action;
import com.lhjz.portal.pojo.Enum.Status;
import com.lhjz.portal.pojo.Enum.Target;
import com.lhjz.portal.repository.LogRepository;
import com.lhjz.portal.repository.TProjectRepository;
import com.lhjz.portal.repository.TStatusRepository;
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
@RequestMapping("admin/task/status")
public class TStatusController extends BaseController {

	static Logger logger = LoggerFactory.getLogger(TStatusController.class);

	@Autowired
	UserRepository userRepository;

	@Autowired
	LogRepository logRepository;

	@Autowired
	TProjectRepository projectRepository;

	@Autowired
	TStatusRepository statusRepository;

	@Autowired
	MailSender mailSender;

	@RequestMapping(value = "create", method = RequestMethod.POST)
	@ResponseBody
	public RespBody create(@RequestParam("name") String name, @RequestParam("pid") Long pid,
			@RequestParam(value = "description", required = false) String description) {

		if (!isAdmin()) {
			return RespBody.failed("权限不足!");
		}

		TStatus status = new TStatus();
		status.setName(name);
		status.setDescription(description);
		status.setProject(projectRepository.findOne(pid));

		TStatus status2 = statusRepository.saveAndFlush(status);

		return RespBody.succeed(status2);
	}

	@RequestMapping(value = "listByProject", method = RequestMethod.GET)
	@ResponseBody
	public RespBody listByProject(@RequestParam("pid") Long pid) {

		TProject project = projectRepository.findOne(pid);

		if (!AuthUtil.hasTProjectAuth(project)) {
			return RespBody.failed("权限不足!");
		}

		List<TStatus> states = statusRepository.findByStatusNotAndProject(Status.Deleted, project);

		return RespBody.succeed(states);
	}

	@RequestMapping(value = "update", method = RequestMethod.POST)
	@ResponseBody
	public RespBody update(@RequestParam("id") Long id, @RequestParam("name") String name,
			@RequestParam(value = "description", required = false) String description) {

		TStatus status = statusRepository.findOne(id);

		if (!AuthUtil.isTProjectLeader(status.getProject()) && !isSuperOrCreator(WebUtil.getUsername())) {
			return RespBody.failed("权限不足!");
		}

		status.setName(name);
		status.setDescription(description);

		TStatus status2 = statusRepository.saveAndFlush(status);

		return RespBody.succeed(status2);

	}

	@RequestMapping(value = "delete", method = RequestMethod.POST)
	@ResponseBody
	public RespBody delete(@RequestParam("id") Long id) {

		TStatus status = statusRepository.findOne(id);

		if (!AuthUtil.isTProjectLeader(status.getProject()) && !isSuperOrCreator(WebUtil.getUsername())) {
			return RespBody.failed("权限不足!");
		}

		status.setStatus(Status.Deleted);

		statusRepository.saveAndFlush(status);

		log(Action.Delete, Target.TStatus, id, status.getName());

		return RespBody.succeed(id);
	}

	@RequestMapping(value = "get", method = RequestMethod.GET)
	@ResponseBody
	public RespBody get(@RequestParam("id") Long id) {

		TStatus status = statusRepository.findOne(id);

		if (!AuthUtil.hasTProjectAuth(status.getProject())) {
			return RespBody.failed("权限不足!");
		}

		return RespBody.succeed(status);
	}

}
