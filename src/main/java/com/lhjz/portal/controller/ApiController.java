/**
 * 版权所有 (TMS)
 */
package com.lhjz.portal.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.jayway.jsonpath.JsonPath;
import com.lhjz.portal.base.BaseController;
import com.lhjz.portal.component.MailSender2;
import com.lhjz.portal.constant.SysConstant;
import com.lhjz.portal.entity.Channel;
import com.lhjz.portal.entity.ChatChannel;
import com.lhjz.portal.model.RespBody;
import com.lhjz.portal.repository.ChannelRepository;
import com.lhjz.portal.repository.ChatChannelRepository;
import com.lhjz.portal.repository.UserRepository;

/**
 * 
 * @author xi
 * 
 * @date 2015年3月28日 下午1:19:05
 * 
 */
@Controller
@RequestMapping("api/")
public class ApiController extends BaseController {

	static Logger logger = LoggerFactory.getLogger(ApiController.class);

	@Autowired
	ChannelRepository channelRepository;
	
	@Autowired
	ChatChannelRepository chatChannelRepository;

	@Autowired
	UserRepository userRepository;
	
	@Autowired
	MailSender2 mailSender;

	@RequestMapping(value = "channel/jenkins/send", method = RequestMethod.POST)
	@ResponseBody
	public RespBody create(@RequestParam("channel") String channel, @RequestBody String reqBody) {
		
		Channel channel2 = channelRepository.findOneByName(channel);
		if(channel2 == null) {
			return RespBody.failed("发送消息目的频道不存在!");
		}
		
		String name = JsonPath.read(reqBody, "$.name");
		String status = JsonPath.read(reqBody, "$.build.status");
		String phase = JsonPath.read(reqBody, "$.build.phase");
		String fullUrl = JsonPath.read(reqBody, "$.build.full_url");
		
		StringBuffer sb = new StringBuffer();
		sb.append("## Jenkins任务发版状态报告").append(SysConstant.NEW_LINE);
		sb.append("> **任务名称:** ").append(name).append(SysConstant.NEW_LINE);
		sb.append("> **任务URL:** ").append(fullUrl).append(SysConstant.NEW_LINE);
		sb.append("> **任务阶段:** ").append(phase).append(SysConstant.NEW_LINE);
		sb.append("> **任务状态:** ").append(status).append(SysConstant.NEW_LINE);
		
		ChatChannel chatChannel = new ChatChannel();
		chatChannel.setChannel(channel2);
		chatChannel.setContent(sb.toString());
		
		chatChannelRepository.saveAndFlush(chatChannel);

		return RespBody.succeed();
	}
	
}
