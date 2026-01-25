/**
 * 版权所有 (TMS)
 */
package com.lhjz.portal.service.impl;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.lhjz.portal.entity.ChatChannel;
import com.lhjz.portal.model.ChannelPayload;
import com.lhjz.portal.model.ChannelPayload.Cmd;
import com.lhjz.portal.repository.ChatChannelRepository;
import com.lhjz.portal.service.ChatChannelService;
import com.lhjz.portal.util.WebUtil;

import lombok.extern.slf4j.Slf4j;

/**
 * @author xi
 * 聊天频道服务实现类，提供聊天频道的保存和WebSocket消息发送功能
 */
@Service
@Transactional
@Slf4j
public class ChatChannelServiceImpl implements ChatChannelService {

    // 自动注入ChatChannelRepository，用于数据库操作
	@Autowired
	ChatChannelRepository chatChannelRepository;

    // 懒加载注入SimpMessagingTemplate，用于WebSocket消息发送
	@Lazy
	@Autowired
	SimpMessagingTemplate messagingTemplate;

    /**
     * 保存聊天频道并发送WebSocket更新消息
     * @param chatChannel 要保存的聊天频道对象
     * @return 保存后的聊天频道对象
     */
	@Override
	public ChatChannel save(ChatChannel chatChannel) {
		// 保存聊天频道到数据库并立即刷新
		ChatChannel chatChannel2 = chatChannelRepository.saveAndFlush(chatChannel);
		// 发送WebSocket更新消息
		wsSend(chatChannel2);
		return chatChannel2;
	}

    /**
     * 发送WebSocket更新消息
     * @param chatChannel 包含更新信息的聊天频道对象
     */
	private void wsSend(ChatChannel chatChannel) {
		try {
            // 构建并发送消息到指定的WebSocket频道
			messagingTemplate.convertAndSend("/channel/update",
					ChannelPayload.builder().uuid(UUID.randomUUID().toString()).username(WebUtil.getUsername())
							.cmd(Cmd.R).id(chatChannel.getChannel().getId()).cid(chatChannel.getId()).build());
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
	}

}
