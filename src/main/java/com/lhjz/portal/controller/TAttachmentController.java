/**
 * 版权所有 (TMS)
 */
package com.lhjz.portal.controller;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import com.lhjz.portal.base.BaseController;
import com.lhjz.portal.component.MailSender;
import com.lhjz.portal.entity.TAttachment;
import com.lhjz.portal.entity.TProject;
import com.lhjz.portal.entity.Task;
import com.lhjz.portal.model.RespBody;
import com.lhjz.portal.pojo.Enum.Action;
import com.lhjz.portal.pojo.Enum.FileType;
import com.lhjz.portal.pojo.Enum.Status;
import com.lhjz.portal.pojo.Enum.TAttachmentType;
import com.lhjz.portal.pojo.Enum.Target;
import com.lhjz.portal.repository.LogRepository;
import com.lhjz.portal.repository.TAttachmentRepository;
import com.lhjz.portal.repository.UserRepository;
import com.lhjz.portal.service.FileService;
import com.lhjz.portal.util.AuthUtil;
import com.lhjz.portal.util.StringUtil;

/**
 * 
 * @author xi
 * 
 * @date 2015年3月28日 下午1:19:05
 * 
 */
@Controller
@RequestMapping("admin/task/attachment")
public class TAttachmentController extends BaseController {

	static Logger logger = LoggerFactory.getLogger(TAttachmentController.class);

	@Autowired
	UserRepository userRepository;

	@Autowired
	LogRepository logRepository;

	@Autowired
	TAttachmentRepository attachmentRepository;

	@Autowired
	MailSender mailSender;

	@Autowired
	FileService fileService;

	@RequestMapping(value = "create", method = RequestMethod.POST)
	@ResponseBody
	public RespBody create(@RequestParam("fid") Long fid, @RequestParam("href") String href,
			@RequestParam(value = "type", defaultValue = "File") String type,
			@RequestParam(value = "name", required = false) String name,
			@RequestParam(value = "description", required = false) String description) {

		TAttachment attachment = new TAttachment();

		attachment.setFileId(fid);
		attachment.setHref(href);
		attachment.setType(TAttachmentType.valueOf(type));
		attachment.setName(name);
		attachment.setDescription(description);

		TAttachment attachment2 = attachmentRepository.saveAndFlush(attachment);

		return RespBody.succeed(attachment2);
	}

	@RequestMapping(value = "delete", method = RequestMethod.POST)
	@ResponseBody
	public RespBody delete(@RequestParam("id") Long id) {

		TAttachment attachment = attachmentRepository.findOne(id);

		Task task = attachment.getTask();
		if (task != null) {
			TProject project = task.getProject();
			if (!AuthUtil.isTProjectLeader(project) && !isSuperOrCreator(attachment.getCreator())) {
				return RespBody.failed("权限不足!");
			}
		}

		attachment.setStatus(Status.Deleted);

		attachmentRepository.saveAndFlush(attachment);

		log(Action.Delete, Target.TAttachment, id, attachment.getId());

		return RespBody.succeed(id);
	}

	@RequestMapping(value = "upload", method = RequestMethod.POST)
	@ResponseBody
	public RespBody upload(HttpServletRequest request, @RequestParam(value = "toType", required = false) String toType,
			@RequestParam(value = "toId", required = false) String toId, @RequestParam("file") MultipartFile[] files) {

		try {
			List<com.lhjz.portal.entity.File> uploads = fileService.upload(request, toType, toId, files);
			List<TAttachment> attachments = new ArrayList<>();
			uploads.forEach(u -> {
				attachments.add(save(u));
			});

			return RespBody.succeed(attachments);
		} catch (Exception e) {
			e.printStackTrace();
			return RespBody.failed(e.getMessage());
		}

	}

	private TAttachment save(com.lhjz.portal.entity.File file) {

		TAttachment attachment = new TAttachment();

		String href = null;
		if (file.getType().equals(FileType.Image)) {
			href = StringUtil.replace("/{?1}{?2}", file.getPath(), file.getUuidName());
		} else {
			href = StringUtil.replace("/{?1}{?2}", "admin/file/download/", file.getId());
		}
		attachment.setName(file.getName());
		attachment.setFileId(file.getId());
		attachment.setHref(href);
		attachment.setType(file.getType().equals(FileType.Image) ? TAttachmentType.Image : TAttachmentType.File);
		attachment.setDescription(file.getName());

		return attachmentRepository.saveAndFlush(attachment);
	}

	@RequestMapping(value = "base64", method = RequestMethod.POST)
	@ResponseBody
	public RespBody base64(HttpServletRequest request, @RequestParam(value = "toType", required = false) String toType,
			@RequestParam(value = "toId", required = false) String toId, @RequestParam("dataURL") String dataURL,
			@RequestParam("type") String type) {

		try {
			com.lhjz.portal.entity.File u = fileService.base64(request, toType, toId, dataURL, type);

			return RespBody.succeed(save(u));
		} catch (Exception e) {
			e.printStackTrace();
			logger.error(e.getMessage(), e);
			return RespBody.failed(e.getMessage());
		}

	}

}
