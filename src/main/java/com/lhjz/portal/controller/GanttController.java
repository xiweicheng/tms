/**
 * 版权所有 (TMS)
 */
package com.lhjz.portal.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.lhjz.portal.base.BaseController;
import com.lhjz.portal.component.MailSender;
import com.lhjz.portal.entity.Channel;
import com.lhjz.portal.entity.Gantt;
import com.lhjz.portal.model.RespBody;
import com.lhjz.portal.pojo.Enum.Status;
import com.lhjz.portal.repository.ChannelRepository;
import com.lhjz.portal.repository.GanttRepository;
import com.lhjz.portal.repository.LogRepository;
import com.lhjz.portal.repository.UserRepository;
import com.lhjz.portal.util.StringUtil;

import lombok.extern.slf4j.Slf4j;

/**
 * 
 * @author xi
 * 
 * @date 2015年3月28日 下午1:19:05
 * 
 */
@Controller
@RequestMapping("admin/gantt")
@Slf4j
public class GanttController extends BaseController {

	@Autowired
	GanttRepository ganttRepository;

	@Autowired
	ChannelRepository channelRepository;

	@Autowired
	UserRepository userRepository;

	@Autowired
	LogRepository logRepository;

	@Autowired
	MailSender mailSender;

	@RequestMapping(value = "create", method = RequestMethod.POST)
	@ResponseBody
	public RespBody create(@RequestParam("title") String title, @RequestParam("cid") Long cid,
			@RequestParam(value = "desc", required = false) String desc, @RequestParam("content") String content) {

		if (StringUtil.isEmpty(title)) {
			return RespBody.failed("标题不能为空!");
		}

		if (StringUtil.isEmpty(content)) {
			return RespBody.failed("内容不能为空!");
		}

		Channel channel = channelRepository.findOne(cid);
		if (channel == null) {
			return RespBody.failed("频道不存在!");
		}

		Gantt gantt = Gantt.builder().title(title).content(content).description(desc).channel(channel).build();
		log.debug("创建Gantt: {}", gantt);

		Gantt gantt2 = ganttRepository.saveAndFlush(gantt);

		return RespBody.succeed(gantt2);
	}

	@RequestMapping(value = "search", method = RequestMethod.GET)
	@ResponseBody
	public RespBody search(@PageableDefault(sort = { "id" }, direction = Direction.DESC) Pageable pageable,
			@RequestParam("cid") Long cid, String search) {

		Channel channel = channelRepository.findOne(cid);
		if (channel == null) {
			return RespBody.failed("频道不存在!");
		}

		Page<Gantt> pageGantt = ganttRepository.findByStatusNotAndChannelAndTitleContainingIgnoreCase(Status.Deleted,
				channel, search, pageable);

		return RespBody.succeed(pageGantt);
	}

	@RequestMapping(value = "update", method = RequestMethod.POST)
	@ResponseBody
	public RespBody update(@RequestParam("id") Long id, @RequestParam(value = "title", required = false) String title,
			@RequestParam(value = "desc", required = false) String desc,
			@RequestParam(value = "content", required = false) String content) {

		Gantt gantt = ganttRepository.findOne(id);

		if (gantt == null) {
			return RespBody.failed("甘特图不存在!");
		}

		if (!isSuperOrCreator(gantt.getCreator()) && !gantt.getOpenEdit()) {
			return RespBody.failed("权限不足！");
		}

		boolean needUpdate = false;

		if (StringUtil.isNotEmpty(title)) {
			gantt.setTitle(title);
			needUpdate = true;
		}

		if (StringUtil.isNotEmpty(content)) {
			gantt.setContent(content);
			needUpdate = true;
		}

		if (StringUtil.isNotEmpty(desc)) {
			gantt.setDescription(desc);
			needUpdate = true;
		}

		if (!needUpdate) {
			return RespBody.failed("没有更新内容！");
		}

		Gantt gantt2 = ganttRepository.saveAndFlush(gantt);

		return RespBody.succeed(gantt2);

	}

	@RequestMapping(value = "delete/{id}", method = RequestMethod.POST)
	@ResponseBody
	public RespBody delete(@PathVariable("id") Long id) {

		Gantt gantt = ganttRepository.findOne(id);

		if (!isSuperOrCreator(gantt.getCreator())) {
			return RespBody.failed("权限不足！");
		}

		gantt.setStatus(Status.Deleted);

		ganttRepository.saveAndFlush(gantt);

		return RespBody.succeed(id);
	}

	@RequestMapping(value = "get/{id}", method = RequestMethod.GET)
	@ResponseBody
	public RespBody get(@PathVariable("id") Long id) {

		Gantt gantt = ganttRepository.findOne(id);

		return RespBody.succeed(gantt);
	}

}
