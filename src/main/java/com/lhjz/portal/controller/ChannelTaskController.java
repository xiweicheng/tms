/**
 * 版权所有 (TMS)
 */
package com.lhjz.portal.controller;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.lhjz.portal.base.BaseController;
import com.lhjz.portal.component.MailSender;
import com.lhjz.portal.entity.Channel;
import com.lhjz.portal.entity.ChatChannel;
import com.lhjz.portal.model.RespBody;
import com.lhjz.portal.repository.ChannelRepository;
import com.lhjz.portal.repository.ChatChannelRepository;
import com.lhjz.portal.repository.UserRepository;
import com.lhjz.portal.util.AuthUtil;

/**
 * 
 * @author xi
 * 
 * @date 2015年3月28日 下午1:19:05
 * 
 */
@RestController
@RequestMapping("admin/channel/task")
public class ChannelTaskController extends BaseController {

	static Logger logger = LoggerFactory.getLogger(ChannelTaskController.class);

	@Autowired
	ChannelRepository channelRepository;

	@Autowired
	UserRepository userRepository;

	@Autowired
	MailSender mailSender;

	@Autowired
	ChatChannelRepository chatChannelRepository;

	@GetMapping("listBy")
	public RespBody listBy(@PageableDefault(sort = { "id" }, direction = Direction.DESC) Pageable pageable,
			@RequestParam("label") String label, @RequestParam("cid") Long cid) {
		Channel channel = channelRepository.findOne(cid);

		if (!AuthUtil.hasChannelAuth(channel)) {
			return RespBody.failed("权限不足！");
		}

		List<ChatChannel> chatChannels = chatChannelRepository.queryByChannelAndLabel(cid, label, pageable.getOffset(),
				pageable.getPageSize());
		long count = chatChannelRepository.countByChannelAndLabel(cid, label);

		Page<ChatChannel> page = new PageImpl<>(chatChannels, pageable, count);

		page.forEach(cc -> {
			cc.setChannel(null);
		});

		return RespBody.succeed(page);
	}
}
