/**
 * 版权所有 (TMS)
 */
package com.lhjz.portal.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
 * 频道服务实现类，实现了ChannelService接口
 * 提供频道创建、加入等功能
 */
@Service
@Transactional
public class ChannelServiceImpl implements ChannelService {

    // 从配置文件中获取超级管理员用户名
	@Value("${tms.super.username}")
	String superUsername;

    // 从配置文件中获取全员频道名称
	@Value("${tms.channel.name.all}")
	String channelNameAll;

    // 注入用户数据访问层
	@Autowired
	UserRepository userRepository;

    // 注入频道数据访问层
	@Autowired
	ChannelRepository channelRepository;

    // 注入聊天频道数据访问层
	@Autowired
	ChatChannelRepository chatChannelRepository;

    /**
     * 用户加入全员频道
     * @param username 用户名
     */
	@Override
	public void joinAll(String username) {
		this.joinAll(userRepository.findOne(username));
	}

    /**
     * 创建超级管理员频道
     * @param name 频道名称
     * @param title 频道标题
     * @return 创建的频道对象
     */
	@Override
	public Channel createAsSuper(String name, String title) {
		// 获取超级管理员用户
		User user = userRepository.findOne(superUsername);
		if (user == null) {
			return null;
		}
		// 设置当前操作审计员为超级管理员
		ThreadUtil.setCurrentAuditor(superUsername);

		// 创建新频道
		Channel channel = new Channel();
		channel.setName(name);
		channel.setTitle(title);

		// 设置频道所有者
		channel.setOwner(user);

		// 保存频道并刷新
		Channel channel2 = channelRepository.saveAndFlush(channel);

		// 清除当前操作审计员
		ThreadUtil.clearCurrentAuditor();

		// 将用户加入频道
		user.getJoinChannels().add(channel2);
		User user2 = userRepository.saveAndFlush(user);

		// 将用户添加到频道成员列表
		channel2.getMembers().add(user2);

		return channel2;
	}

    /**
     * 用户加入全员频道
     * @param user 用户对象
     */
	@Override
	public void joinAll(User user) {
		// 参数校验
		if (user == null) {
			return;
		}

		// 查询全员频道
		Channel channel = channelRepository.findOneByName(channelNameAll);

		// 如果频道不存在，则创建全员频道
		if (channel == null) {
			channel = createAsSuper(channelNameAll, "全员频道");
		}

		// 如果创建频道失败，则返回
		if (channel == null) {
			return;
		}

		// 如果用户尚未加入该频道
		if (!user.getJoinChannels().contains(channel)) {
			// 将用户加入频道
			user.getJoinChannels().add(channel);
			userRepository.saveAndFlush(user);
			
			// 设置当前操作审计员为当前用户
			ThreadUtil.setCurrentAuditor(user.getUsername());
			
			// 用户频道消息提醒
			ChatChannel chatChannel = new ChatChannel();
			chatChannel.setChannel(channel);
			chatChannel.setContent("## ~频道消息播报~\n> {~" + user.getUsername() + "} **加入**该频道!");

			chatChannelRepository.saveAndFlush(chatChannel);
			
			ThreadUtil.clearCurrentAuditor();
		}

	}

}
