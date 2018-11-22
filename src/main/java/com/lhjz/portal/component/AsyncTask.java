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
import com.lhjz.portal.entity.ChatDirect;
import com.lhjz.portal.entity.ChatReply;
import com.lhjz.portal.model.ChannelPayload;
import com.lhjz.portal.model.ChannelPayload.Cmd;
import com.lhjz.portal.model.DirectPayload;
import com.lhjz.portal.pojo.Enum.Action;
import com.lhjz.portal.pojo.Enum.ChatMsgType;
import com.lhjz.portal.repository.ChatChannelRepository;
import com.lhjz.portal.repository.ChatDirectRepository;
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
	ChatDirectRepository chatDirectRepository;

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

					chatMsg.put(chatChannel2, Action.Update, ChatMsgType.Content, username, null);
					wsSendChannel(chatChannel2, messagingTemplate, username);
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

					chatMsg.put(chatReply2.getChatChannel(), Action.Update, ChatMsgType.Reply, username, chatReply2);
					wsSendChannel(chatReply2.getChatChannel(), messagingTemplate, username);
				}
			}
		}
	}

	private void wsSendChannel(ChatChannel chatChannel, SimpMessagingTemplate messagingTemplate, String username) {
		try {
			messagingTemplate.convertAndSend("/channel/update", ChannelPayload.builder().username(username).cmd(Cmd.R)
					.id(chatChannel.getChannel().getId()).cid(chatChannel.getId()).build());
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
	}

	@Async
	public void updateChatDirect(String content, Long id, SimpMessagingTemplate messagingTemplate, String username) {
		String[] lines = content.trim().split("\n");
		String lastLine = lines[lines.length - 1];
		if (HtmlUtil.isUrl(lastLine)) {
			String summary = HtmlUtil.summary(lastLine);
			if (StringUtils.isNotEmpty(summary)) {
				content = content + "\n" + summary;

				ChatDirect chatDirect = chatDirectRepository.findOne(id);

				if (chatDirect != null) {
					chatDirect.setContent(content);
					ChatDirect chatDirect2 = chatDirectRepository.saveAndFlush(chatDirect);

					wsSendDirect(chatDirect2, com.lhjz.portal.model.DirectPayload.Cmd.U, messagingTemplate, username);
				}
			}
		}
	}

	private void wsSendDirect(ChatDirect chatDirect, com.lhjz.portal.model.DirectPayload.Cmd cmd,
			SimpMessagingTemplate messagingTemplate, String username) {
		try {
			messagingTemplate.convertAndSendToUser(chatDirect.getChatTo().getUsername(), "/direct/update",
					DirectPayload.builder().cmd(cmd).username(username).id(chatDirect.getId()).build());
			if (!StringUtils.equals(chatDirect.getChatTo().getUsername(), username)) {
				messagingTemplate.convertAndSendToUser(username, "/direct/update",
						DirectPayload.builder().cmd(cmd).username(username).id(chatDirect.getId()).build());
			}
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
	}
}
