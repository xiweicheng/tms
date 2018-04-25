/**
 * 版权所有 (TMS)
 */
package com.lhjz.portal.controller;

import java.util.stream.Stream;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.lhjz.portal.base.BaseController;
import com.lhjz.portal.component.MailSender;
import com.lhjz.portal.entity.Channel;
import com.lhjz.portal.entity.ChannelGroup;
import com.lhjz.portal.entity.ChatChannel;
import com.lhjz.portal.entity.security.User;
import com.lhjz.portal.model.RespBody;
import com.lhjz.portal.pojo.Enum.Status;
import com.lhjz.portal.repository.ChannelGroupRepository;
import com.lhjz.portal.repository.ChannelRepository;
import com.lhjz.portal.repository.ChatChannelRepository;
import com.lhjz.portal.repository.UserRepository;
import com.lhjz.portal.util.StringUtil;
import com.lhjz.portal.util.WebUtil;

import lombok.extern.slf4j.Slf4j;

/**
 * 
 * @author xi
 * 
 * @date 2015年3月28日 下午1:19:05
 * 
 */
@Slf4j
@Controller
@RequestMapping("admin/channel/group")
public class ChannelGroupController extends BaseController {

	@Autowired
	ChannelRepository channelRepository;

	@Autowired
	ChannelGroupRepository channelGroupRepository;

	@Autowired
	UserRepository userRepository;

	@Autowired
	ChatChannelRepository chatChannelRepository;

	@Autowired
	MailSender mailSender;

	@RequestMapping(value = "create", method = RequestMethod.POST)
	@ResponseBody
	public RespBody create(@RequestParam("channelId") Long channelId, @RequestParam("name") String name,
			@RequestParam(value = "title", required = false) String title) {

		if (StringUtil.isEmpty(name)) {
			return RespBody.failed("标识不能为空!");
		}

		Channel channel = channelRepository.findOne(channelId);

		if (!isSuperOrCreator(channel.getCreator())) {
			return RespBody.failed("权限不足!");
		}

		ChannelGroup channelGroup = ChannelGroup.builder().name(name).title(title).channel(channel).build();

		ChannelGroup channelGroup2 = channelGroupRepository.saveAndFlush(channelGroup);

		return RespBody.succeed(channelGroup2);
	}

	@RequestMapping(value = "get", method = RequestMethod.GET)
	@ResponseBody
	public RespBody get(@RequestParam("id") Long id) {

		ChannelGroup channelGroup = channelGroupRepository.findOne(id);

		if (!isSuperOrCreator(channelGroup.getCreator()) && !isChannelMember(channelGroup.getChannel())) {
			return RespBody.failed("权限不足!");
		}

		return RespBody.succeed(channelGroup);
	}

	@RequestMapping(value = "update", method = RequestMethod.POST)
	@ResponseBody
	public RespBody update(@RequestParam("id") Long id, @RequestParam(value = "title", required = false) String title,
			@RequestParam(value = "name", required = false) String name) {

		ChannelGroup channelGroup = channelGroupRepository.findOne(id);

		if (!isSuperOrCreator(channelGroup.getCreator())) {
			return RespBody.failed("权限不足!");
		}

		boolean updated = false;
		if (StringUtil.isNotEmpty(name)) {
			channelGroup.setName(name);
			updated = true;
		}

		if (title != null) {
			channelGroup.setTitle(title);
			updated = true;
		}

		if (updated) {
			RespBody.succeed(channelGroupRepository.saveAndFlush(channelGroup));
		}

		return RespBody.succeed(channelGroup);
	}

	@RequestMapping(value = "delete", method = RequestMethod.POST)
	@ResponseBody
	public RespBody delete(@RequestParam("id") Long id) {

		ChannelGroup channelGroup = channelGroupRepository.findOne(id);

		if (!isSuperOrCreator(channelGroup.getCreator().getUsername())) {
			return RespBody.failed("您没有权限删除该频道组!");
		}

		channelGroup.setStatus(Status.Deleted);

		channelGroupRepository.saveAndFlush(channelGroup);

		return RespBody.succeed(id);
	}

	@RequestMapping(value = "member/add", method = RequestMethod.POST)
	@ResponseBody
	public RespBody addMember(@RequestParam("id") Long id, @RequestParam("members") String members) {

		ChannelGroup channelGroup = channelGroupRepository.findOne(id);

		if (!isSuperOrCreator(channelGroup.getCreator().getUsername())) {
			return RespBody.failed("您没有权限添加成员到该频道组!");
		}

		String[] ms = members.split(",");

		Stream.of(ms).forEach((m) -> {
			User user = userRepository.findOne(m);
			if (user != null && !channelGroup.getMembers().contains(user)) {
				user.getJoinChannelGroups().add(channelGroup);
				userRepository.saveAndFlush(user);
				channelGroup.getMembers().add(user);

				// 用户频道消息提醒
				ChatChannel chatChannel = new ChatChannel();
				chatChannel.setChannel(channelGroup.getChannel());
				chatChannel.setContent("## ~频道消息播报~\n> {~" + user.getUsername() + "} 被**添加到**该频道下的【"
						+ channelGroup.getName() + "】频道组!");

				chatChannelRepository.saveAndFlush(chatChannel);
			}
		});

		return RespBody.succeed(channelGroup);
	}

	@RequestMapping(value = "member/remove", method = RequestMethod.POST)
	@ResponseBody
	public RespBody removeMember(@RequestParam("id") Long id, @RequestParam("members") String members) {

		ChannelGroup channelGroup = channelGroupRepository.findOne(id);

		if (!isSuperOrCreator(channelGroup.getCreator().getUsername())) {
			return RespBody.failed("您没有权限从该频道组移除成员!");
		}

		String[] ms = members.split(",");

		Stream.of(ms).forEach((m) -> {
			User user = userRepository.findOne(m);
			if (user != null) {
				user.getJoinChannelGroups().remove(channelGroup);
				userRepository.saveAndFlush(user);
				channelGroup.getMembers().remove(user);

				// 用户频道消息提醒
				ChatChannel chatChannel = new ChatChannel();
				chatChannel.setChannel(channelGroup.getChannel());
				chatChannel.setContent("## ~频道消息播报~\n> {~" + user.getUsername() + "} 被**移除出**该频道下的【"
						+ channelGroup.getName() + "】频道组!");

				chatChannelRepository.saveAndFlush(chatChannel);
			}
		});

		return RespBody.succeed(channelGroup);
	}

	@RequestMapping(value = "join", method = RequestMethod.POST)
	@ResponseBody
	public RespBody join(@RequestParam("id") Long id) {

		ChannelGroup channelGroup = channelGroupRepository.findOne(id);

		final User loginUser = getLoginUser();
		if (!channelGroup.getMembers().contains(loginUser) && isChannelMember(channelGroup.getChannel())) {
			loginUser.getJoinChannelGroups().add(channelGroup);
			userRepository.saveAndFlush(loginUser);
			channelGroup.getMembers().add(loginUser);

			// 用户频道消息提醒
			ChatChannel chatChannel = new ChatChannel();
			chatChannel.setChannel(channelGroup.getChannel());
			chatChannel.setContent("## ~频道消息播报~\n> {~" + loginUser.getUsername() + "} **加入**该频道下的【"
					+ channelGroup.getName() + "】频道组!");

			chatChannelRepository.saveAndFlush(chatChannel);
		}

		return RespBody.succeed(channelGroup);
	}

	@RequestMapping(value = "leave", method = RequestMethod.POST)
	@ResponseBody
	public RespBody leave(@RequestParam("id") Long id) {

		ChannelGroup channelGroup = channelGroupRepository.findOne(id);

		final User loginUser = getLoginUser();

		if (channelGroup.getMembers().contains(loginUser)) {
			loginUser.getJoinChannelGroups().add(channelGroup);
			userRepository.saveAndFlush(loginUser);
			channelGroup.getMembers().remove(loginUser);

			// 用户频道消息提醒
			ChatChannel chatChannel = new ChatChannel();
			chatChannel.setChannel(channelGroup.getChannel());
			chatChannel.setContent("## ~频道消息播报~\n> {~" + loginUser.getUsername() + "} **离开**该频道下的【"
					+ channelGroup.getName() + "】频道组!");

			chatChannelRepository.saveAndFlush(chatChannel);
		}

		return RespBody.succeed(channelGroup);
	}

	private boolean isChannelMember(Channel channel) {

		if (channel == null) {
			return false;
		}

		return channel.getMembers().stream().anyMatch(m -> m.equals(new User(WebUtil.getUsername())));
	}

}
