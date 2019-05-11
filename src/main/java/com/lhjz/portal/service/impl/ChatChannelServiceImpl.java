/**
 * 版权所有 (TMS)
 */
package com.lhjz.portal.service.impl;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
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
 *
 */
@Service
@Transactional
@Slf4j
public class ChatChannelServiceImpl implements ChatChannelService {

	@Autowired
	ChatChannelRepository chatChannelRepository;

	@Autowired
	SimpMessagingTemplate messagingTemplate;

	@Override
	public ChatChannel save(ChatChannel chatChannel) {
		ChatChannel chatChannel2 = chatChannelRepository.saveAndFlush(chatChannel);
		wsSend(chatChannel2);
		return chatChannel2;
	}

	private void wsSend(ChatChannel chatChannel) {
		try {
			messagingTemplate.convertAndSend("/channel/update",
					ChannelPayload.builder().uuid(UUID.randomUUID().toString()).username(WebUtil.getUsername())
							.cmd(Cmd.R).id(chatChannel.getChannel().getId()).cid(chatChannel.getId()).build());
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
	}

}
