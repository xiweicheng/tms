/**
 * 版权所有 (TMS)
 */
package com.lhjz.portal.controller;

import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.lhjz.portal.base.BaseController;
import com.lhjz.portal.component.MailSender;
import com.lhjz.portal.constant.SysConstant;
import com.lhjz.portal.entity.TAttachment;
import com.lhjz.portal.entity.TLabel;
import com.lhjz.portal.entity.TLink;
import com.lhjz.portal.entity.TModule;
import com.lhjz.portal.entity.TProject;
import com.lhjz.portal.entity.TVersion;
import com.lhjz.portal.entity.Task;
import com.lhjz.portal.entity.security.User;
import com.lhjz.portal.model.RespBody;
import com.lhjz.portal.pojo.Enum.Status;
import com.lhjz.portal.pojo.Enum.TLinkType;
import com.lhjz.portal.pojo.Enum.TaskPriority;
import com.lhjz.portal.repository.LogRepository;
import com.lhjz.portal.repository.TAttachmentRepository;
import com.lhjz.portal.repository.TCommentRepository;
import com.lhjz.portal.repository.TEpicRepository;
import com.lhjz.portal.repository.TLabelRepository;
import com.lhjz.portal.repository.TLinkRepository;
import com.lhjz.portal.repository.TModuleRepository;
import com.lhjz.portal.repository.TProjectRepository;
import com.lhjz.portal.repository.TStatusRepository;
import com.lhjz.portal.repository.TVersionRepository;
import com.lhjz.portal.repository.TaskRepository;
import com.lhjz.portal.repository.UserRepository;
import com.lhjz.portal.service.TProjectService;
import com.lhjz.portal.util.StringUtil;

/**
 * 
 * @author xi
 * 
 * @date 2015年3月28日 下午1:19:05
 * 
 */
@RestController
@RequestMapping("admin/task")
public class TaskController extends BaseController {

	static Logger logger = LoggerFactory.getLogger(TaskController.class);

	@Autowired
	UserRepository userRepository;

	@Autowired
	LogRepository logRepository;

	@Autowired
	TaskRepository taskRepository;

	@Autowired
	TModuleRepository moduleRepository;

	@Autowired
	TVersionRepository versionRepository;

	@Autowired
	TStatusRepository statusRepository;

	@Autowired
	TLabelRepository labelRepository;

	@Autowired
	TAttachmentRepository attachmentRepository;

	@Autowired
	TCommentRepository commentRepository;

	@Autowired
	TEpicRepository epicRepository;

	@Autowired
	TLinkRepository linkRepository;

	@Autowired
	TProjectRepository projectRepository;

	@Autowired
	MailSender mailSender;

	@Autowired
	TProjectService projectService;

	@PostMapping("create")
	public RespBody create(@RequestParam("pid") Long pid, @RequestParam("title") String title,
			@RequestParam(value = "description", required = false) String description,
			@RequestParam(value = "modules", required = false) String modules,
			@RequestParam(value = "labels", required = false) String labels,
			@RequestParam(value = "effectVersions", required = false) String effectVersions,
			@RequestParam(value = "resolvedVersions", required = false) String resolvedVersions,
			@RequestParam(value = "epic", required = false) Long epic,
			@RequestParam(value = "reporter", required = false) String reporter,
			@RequestParam(value = "operator", required = false) String operator,
			@RequestParam(value = "priority", required = false) String priority,
			@RequestParam(value = "links", required = false) String links,
			@RequestParam(value = "attachments", required = false) String attachments,
			@RequestParam(value = "parentTask", required = false) Long parentTask) {

		TProject project = projectRepository.findOne(pid);

		if (project == null) {
			return RespBody.failed("对应项目不存在!");
		}

		Task task = new Task();
		task.setProject(project);
		task.setTitle(title);
		task.setDescription(description);
		task.setProjectTaskId(projectService.getTaskIncId(pid));

		if (StringUtil.isNotEmpty(epic)) {
			task.setEpic(epicRepository.findOne(epic));
		}

		User r = StringUtil.isNotEmpty(reporter) ? getUser(reporter) : null;
		task.setReporter(r != null ? r : getLoginUser());

		User o = getUser(operator);
		User po = project.getOperator();
		task.setOperator(o != null ? o : (po != null ? po : project.getLeader()));

		TaskPriority tp = TaskPriority.Medium;
		if (StringUtil.isNotEmpty(priority)) {
			tp = TaskPriority.valueOf(priority);
		}
		task.setPriority(tp);

		if (StringUtil.isNotEmpty(parentTask)) {
			task.setParentTask(taskRepository.findOne(parentTask));
		}

		Task task2 = taskRepository.saveAndFlush(task);

		// modules
		Stream.of(StringUtil.split2(modules, SysConstant.COMMA)).forEach(m -> {
			TModule module = moduleRepository.findOne(Long.valueOf(m));
			module.getTasks().add(task2);

			task2.getModules().add(moduleRepository.saveAndFlush(module));
		});

		// labels
		Stream.of(StringUtil.split2(labels, SysConstant.COMMA)).forEach(l -> {
			TLabel label = new TLabel();

			label.setName(l);
			label.setTask(task2);

			TLabel label2 = labelRepository.saveAndFlush(label);
			task2.getLabels().add(label2);
		});

		// effectVersions
		Stream.of(StringUtil.split2(effectVersions, SysConstant.COMMA)).forEach(v -> {
			TVersion version = versionRepository.findOne(Long.valueOf(v));
			version.getEffectTasks().add(task2);

			task2.getEffectVersions().add(versionRepository.saveAndFlush(version));
		});

		// resolvedVersions
		Stream.of(StringUtil.split2(resolvedVersions, SysConstant.COMMA)).forEach(v -> {
			TVersion version = versionRepository.findOne(Long.valueOf(v));
			version.getResolvedTasks().add(task2);

			task2.getResolvedVersions().add(versionRepository.saveAndFlush(version));
		});

		// links
		Stream.of(StringUtil.split2(links, SysConstant.COMMA)).forEach(l -> {
			TLink link = new TLink();

			link.setLink(l);
			link.setType(l.startsWith("#") ? TLinkType.Task : TLinkType.External);
			link.setTask(task2);

			TLink link2 = linkRepository.saveAndFlush(link);
			task2.getLinks().add(link2);
		});

		// attachments
		Stream.of(StringUtil.split2(attachments, SysConstant.COMMA)).forEach(a -> {
			TAttachment attachment = attachmentRepository.findOne(Long.valueOf(a));

			attachment.setTask(task2);

			TAttachment attachment2 = attachmentRepository.saveAndFlush(attachment);
			task2.getAttachments().add(attachment2);

		});

		return RespBody.succeed(task2);
	}

	@GetMapping("listMy")
	public RespBody listMy(@PageableDefault(sort = { "id" }, direction = Direction.DESC) Pageable pageable) {

		Page<Task> taskPage = taskRepository.findByStatusNot(Status.Deleted, pageable);

		return RespBody.succeed(taskPage);
	}
}
