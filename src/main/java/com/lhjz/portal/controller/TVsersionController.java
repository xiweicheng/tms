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
import com.lhjz.portal.entity.TVersion;
import com.lhjz.portal.model.RespBody;
import com.lhjz.portal.pojo.Enum.Action;
import com.lhjz.portal.pojo.Enum.Status;
import com.lhjz.portal.pojo.Enum.Target;
import com.lhjz.portal.repository.LogRepository;
import com.lhjz.portal.repository.TProjectRepository;
import com.lhjz.portal.repository.TVersionRepository;
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
@RequestMapping("admin/task/version")
public class TVsersionController extends BaseController {

	static Logger logger = LoggerFactory.getLogger(TVsersionController.class);

	@Autowired
	UserRepository userRepository;

	@Autowired
	LogRepository logRepository;

	@Autowired
	TProjectRepository projectRepository;

	@Autowired
	TVersionRepository versionRepository;

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

		TVersion version = versionRepository.findOneByStatusNotAndProjectAndName(Status.Deleted, project, name);

		if (version != null) {
			return RespBody.failed("该项目下同名版本已经存在!");
		}
		version = new TVersion();
		version.setName(name);
		version.setDescription(description);
		version.setStartDate(start);
		version.setEndDate(end);
		version.setProject(project);

		TVersion version2 = versionRepository.saveAndFlush(version);

		return RespBody.succeed(version2);
	}

	@RequestMapping(value = "listByProject", method = RequestMethod.GET)
	@ResponseBody
	public RespBody listByProject(@RequestParam("pid") Long pid) {

		TProject project = projectRepository.findOne(pid);

		if (!AuthUtil.hasTProjectAuth(project)) {
			return RespBody.failed("权限不足!");
		}

		List<TVersion> versions = versionRepository.findByStatusNotAndProject(Status.Deleted, project);

		return RespBody.succeed(versions);
	}

	@RequestMapping(value = "update", method = RequestMethod.POST)
	@ResponseBody
	public RespBody update(@RequestParam("id") Long id, @RequestParam(value = "name", required = false) String name,
			@RequestParam(value = "description", required = false) String description,
			@RequestParam(value = "start", required = false) Date start,
			@RequestParam(value = "end", required = false) Date end) {

		TVersion version = versionRepository.findOne(id);

		if (!AuthUtil.isTProjectLeader(version.getProject()) && !isSuperOrCreator(WebUtil.getUsername())) {
			return RespBody.failed("权限不足!");
		}

		if (name != null) {
			version.setName(name);
		}
		if (description != null) {
			version.setDescription(description);
		}
		if (start != null) {
			version.setStartDate(start);
		}
		if (end != null) {
			version.setEndDate(end);
		}

		TVersion version2 = versionRepository.saveAndFlush(version);

		return RespBody.succeed(version2);

	}

	@RequestMapping(value = "delete", method = RequestMethod.POST)
	@ResponseBody
	public RespBody delete(@RequestParam("id") Long id) {

		TVersion version = versionRepository.findOne(id);

		if (!AuthUtil.isTProjectLeader(version.getProject()) && !isSuperOrCreator(WebUtil.getUsername())) {
			return RespBody.failed("权限不足!");
		}

		version.setStatus(Status.Deleted);

		versionRepository.saveAndFlush(version);

		log(Action.Delete, Target.TVersion, id, version.getName());

		return RespBody.succeed(id);
	}

	@RequestMapping(value = "get", method = RequestMethod.GET)
	@ResponseBody
	public RespBody get(@RequestParam("id") Long id) {

		TVersion version = versionRepository.findOne(id);

		if (!AuthUtil.hasTProjectAuth(version.getProject())) {
			return RespBody.failed("权限不足!");
		}

		return RespBody.succeed(version);
	}

}
