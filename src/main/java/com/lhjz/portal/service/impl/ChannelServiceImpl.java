/**
 * 版权所有 (TMS)
 */
package com.lhjz.portal.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.lhjz.portal.entity.Channel;
import com.lhjz.portal.entity.ChatChannel;
import com.lhjz.portal.entity.security.User;
import com.lhjz.portal.repository.ChannelRepository;
import com.lhjz.portal.repository.ChatChannelRepository;
import com.lhjz.portal.repository.UserRepository;
import com.lhjz.portal.service.ChannelService;
import com.lhjz.portal.util.ThreadUtil;

/**
 * @author xi
 *
 */
@Service
public class ChannelServiceImpl implements ChannelService {

	@Value("${tms.super.username}")
	String superUsername;

	@Value("${tms.channel.name.all}")
	String channelNameAll;

	@Autowired
	UserRepository userRepository;

	@Autowired
	ChannelRepository channelRepository;

	@Autowired
	ChatChannelRepository chatChannelRepository;

	@Override
	public void joinAll(String username) {
		this.joinAll(userRepository.findOne(username));
	}

	@Override
	public Channel createAsSuper(String name, String title) {
		User user = userRepository.findOne(superUsername);
		if (user == null) {
			return null;
		}
		ThreadUtil.setCurrentAuditor(superUsername);

		Channel channel = new Channel();
		channel.setName(name);
		channel.setTitle(title);

		channel.setOwner(user);

		Channel channel2 = channelRepository.saveAndFlush(channel);

		ThreadUtil.clearCurrentAuditor();

		user.getJoinChannels().add(channel2);
		User user2 = userRepository.saveAndFlush(user);

		channel2.getMembers().add(user2);

		return channel2;
	}

	@Override
	public void joinAll(User user) {
		if (user == null) {
			return;
		}

		Channel channel = channelRepository.findOneByName(channelNameAll);

		if (channel == null) {
			channel = createAsSuper(channelNameAll, "全员频道");
		}

		if (channel == null) {
			return;
		}
		user.getJoinChannels().add(channel);
		userRepository.saveAndFlush(user);

		ThreadUtil.setCurrentAuditor(superUsername);

		// 用户频道消息提醒
		ChatChannel chatChannel = new ChatChannel();
		chatChannel.setChannel(channel);
		chatChannel.setContent("## ~频道消息播报~\n> {~" + user.getUsername() + "} 被**添加到**该频道!");

		chatChannelRepository.saveAndFlush(chatChannel);

		ThreadUtil.clearCurrentAuditor();

	}

}
