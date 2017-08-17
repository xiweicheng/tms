/**
 * 版权所有 (TMS)
 */
package com.lhjz.portal.controller;

import java.util.List;
import java.util.Optional;

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

	private Long getMaxOrder(List<TStatus> states) {
		if (states == null) {
			return 0L;
		}

		if (states.size() == 0) {
			return 0L;
		}

		Optional<TStatus> max = states.stream().max((s1, s2) -> (int) (s1.getOrder() - s2.getOrder()));
		return max.get().getOrder();
	}

	@RequestMapping(value = "create", method = RequestMethod.POST)
	@ResponseBody
	public RespBody create(@RequestParam("name") String name,
			@RequestParam(value = "order", required = false) Long order, @RequestParam("pid") Long pid,
			@RequestParam(value = "description", required = false) String description) {

		if (!isAdmin()) {
			return RespBody.failed("权限不足!");
		}

		TProject project = projectRepository.findOne(pid);

		TStatus status = statusRepository.findOneByStatusNotAndProjectAndName(Status.Deleted, project, name);
		List<TStatus> states = statusRepository.findByStatusNotAndProject(Status.Deleted, project);

		if (status != null) {
			return RespBody.failed("该项目下同名状态已经存在!");
		}
		status = new TStatus();
		status.setName(name);
		status.setDescription(description);

		if (order == null) {
			order = (long) (getMaxOrder(states) + 1);
		}
		status.setOrder(order);
		status.setProject(project);

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
	public RespBody update(@RequestParam("id") Long id, @RequestParam(value = "name", required = false) String name,
			@RequestParam(value = "order", required = false) Long order,
			@RequestParam(value = "description", required = false) String description) {

		TStatus status = statusRepository.findOne(id);

		if (!AuthUtil.isTProjectLeader(status.getProject()) && !isSuperOrCreator(status.getCreator())) {
			return RespBody.failed("权限不足!");
		}

		if (name != null) {
			status.setName(name);
		}
		if (description != null) {
			status.setDescription(description);
		}
		if (order != null) {
			status.setOrder(order);
		}

		TStatus status2 = statusRepository.saveAndFlush(status);

		return RespBody.succeed(status2);

	}

	@RequestMapping(value = "delete", method = RequestMethod.POST)
	@ResponseBody
	public RespBody delete(@RequestParam("id") Long id) {

		TStatus status = statusRepository.findOne(id);

		if (!AuthUtil.isTProjectLeader(status.getProject()) && !isSuperOrCreator(status.getCreator())) {
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
