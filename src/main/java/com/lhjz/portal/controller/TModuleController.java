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
import com.lhjz.portal.entity.TModule;
import com.lhjz.portal.entity.TProject;
import com.lhjz.portal.model.RespBody;
import com.lhjz.portal.pojo.Enum.Action;
import com.lhjz.portal.pojo.Enum.Status;
import com.lhjz.portal.pojo.Enum.Target;
import com.lhjz.portal.repository.LogRepository;
import com.lhjz.portal.repository.TModuleRepository;
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
@RequestMapping("admin/task/module")
public class TModuleController extends BaseController {

	static Logger logger = LoggerFactory.getLogger(TModuleController.class);

	@Autowired
	UserRepository userRepository;

	@Autowired
	LogRepository logRepository;

	@Autowired
	TProjectRepository projectRepository;

	@Autowired
	TModuleRepository moduleRepository;

	@Autowired
	MailSender mailSender;

	@RequestMapping(value = "create", method = RequestMethod.POST)
	@ResponseBody
	public RespBody create(@RequestParam("name") String name, @RequestParam("pid") Long pid,
			@RequestParam(value = "description", required = false) String description,
			@RequestParam(value = "leader", required = false) String leader,
			@RequestParam(value = "operator", required = false) String operator) {

		if (!isAdmin()) {
			return RespBody.failed("权限不足!");
		}

		TProject project = projectRepository.findOne(pid);

		TModule module = moduleRepository.findOneByStatusNotAndProjectAndName(Status.Deleted, project, name);

		if (module != null) {
			return RespBody.failed("该项目下同名模块已经存在!");
		}
		module = new TModule();
		module.setName(name);
		module.setDescription(description);
		module.setLeader(getUser(leader));
		module.setOperator(getUser(operator));
		module.setProject(project);

		TModule module2 = moduleRepository.saveAndFlush(module);

		return RespBody.succeed(module2);
	}

	@RequestMapping(value = "listByProject", method = RequestMethod.GET)
	@ResponseBody
	public RespBody listByProject(@RequestParam("pid") Long pid) {

		TProject project = projectRepository.findOne(pid);

		if (!AuthUtil.hasTProjectAuth(project)) {
			return RespBody.failed("权限不足!");
		}

		List<TModule> modules = moduleRepository.findByStatusNotAndProject(Status.Deleted, project);

		return RespBody.succeed(modules);
	}

	@RequestMapping(value = "update", method = RequestMethod.POST)
	@ResponseBody
	public RespBody update(@RequestParam("id") Long id, @RequestParam(value = "name", required = false) String name,
			@RequestParam(value = "description", required = false) String description,
			@RequestParam(value = "leader", required = false) String leader,
			@RequestParam(value = "operator", required = false) String operator) {

		TModule module = moduleRepository.findOne(id);

		if (!AuthUtil.isTProjectLeader(module.getProject()) && !isSuperOrCreator(module.getCreator())) {
			return RespBody.failed("权限不足!");
		}

		if (name != null) {
			module.setName(name);
		}
		if (description != null) {
			module.setDescription(description);
		}
		if (leader != null) {
			module.setLeader(getUser(leader));
		}
		if (operator != null) {
			module.setOperator(getUser(operator));
		}

		TModule module2 = moduleRepository.saveAndFlush(module);

		return RespBody.succeed(module2);

	}

	@RequestMapping(value = "delete", method = RequestMethod.POST)
	@ResponseBody
	public RespBody delete(@RequestParam("id") Long id) {

		TModule module = moduleRepository.findOne(id);

		if (!AuthUtil.isTProjectLeader(module.getProject()) && !isSuperOrCreator(module.getCreator())) {
			return RespBody.failed("权限不足!");
		}

		module.setStatus(Status.Deleted);

		moduleRepository.saveAndFlush(module);

		log(Action.Delete, Target.TModule, id, module.getName());

		return RespBody.succeed(id);
	}

	@RequestMapping(value = "get", method = RequestMethod.GET)
	@ResponseBody
	public RespBody get(@RequestParam("id") Long id) {

		TModule module = moduleRepository.findOne(id);

		if (!AuthUtil.hasTProjectAuth(module.getProject())) {
			return RespBody.failed("权限不足!");
		}

		return RespBody.succeed(module);
	}

}
