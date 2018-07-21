/**
 * 版权所有 (TMS)
 */
package com.lhjz.portal.component;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import com.lhjz.portal.component.core.IChatMsg;
import com.lhjz.portal.entity.ChatChannel;
import com.lhjz.portal.entity.ChatReply;
import com.lhjz.portal.model.ChannelPayload;
import com.lhjz.portal.model.ChannelPayload.Cmd;
import com.lhjz.portal.pojo.Enum.Action;
import com.lhjz.portal.pojo.Enum.ChatMsgType;
import com.lhjz.portal.repository.ChatChannelRepository;
import com.lhjz.portal.repository.ChatReplyRepository;
import com.lhjz.portal.util.HtmlUtil;

import lombok.extern.log4j.Log4j;

/**
 * 
 * @author xi
 * 
 * @date 2015年6月14日 上午10:31:32
 * 
 */
@Component
@Log4j
public class AsyncTask {

	@Autowired
	ChatChannelRepository chatChannelRepository;

	@Autowired
	ChatReplyRepository chatReplyRepository;

	@Autowired
	IChatMsg chatMsg;

	@Async
	public void updateChatChannel(String content, Long id, SimpMessagingTemplate messagingTemplate, String username) {
		String[] lines = content.trim().split("\n");
		String lastLine = lines[lines.length - 1];
		if (HtmlUtil.isUrl(lastLine)) {
			String summary = HtmlUtil.summary(lastLine);
			if (StringUtils.isNotEmpty(summary)) {
				content = content + "\n" + summary;

				ChatChannel chatChannel = chatChannelRepository.findOne(id);

				if (chatChannel != null) {
					chatChannel.setContent(content);
					ChatChannel chatChannel2 = chatChannelRepository.saveAndFlush(chatChannel);

					chatMsg.put(chatChannel2, Action.Update, ChatMsgType.Content, username);
					wsSend(chatChannel2, messagingTemplate, username);
				}
			}
		}
	}

	@Async
	public void updateChatReply(String content, Long id, SimpMessagingTemplate messagingTemplate, String username) {
		String[] lines = content.trim().split("\n");
		String lastLine = lines[lines.length - 1];
		if (HtmlUtil.isUrl(lastLine)) {
			String summary = HtmlUtil.summary(lastLine);
			if (StringUtils.isNotEmpty(summary)) {
				content = content + "\n" + summary;

				ChatReply chatReply = chatReplyRepository.findOne(id);

				if (chatReply != null) {
					chatReply.setContent(content);
					ChatReply chatReply2 = chatReplyRepository.saveAndFlush(chatReply);

					chatMsg.put(chatReply2.getChatChannel(), Action.Update, ChatMsgType.Reply, username);
					wsSend(chatReply2.getChatChannel(), messagingTemplate, username);
				}
			}
		}
	}

	private void wsSend(ChatChannel chatChannel, SimpMessagingTemplate messagingTemplate, String username) {
		try {
			messagingTemplate.convertAndSend("/channel/update", ChannelPayload.builder().username(username).cmd(Cmd.R)
					.id(chatChannel.getChannel().getId()).cid(chatChannel.getId()).build());
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
	}
}
