/**
 * 版权所有 (TMS)
 */
package com.lhjz.portal.controller;

import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.web.PageableDefault;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.lhjz.portal.base.BaseController;
import com.lhjz.portal.component.MailSender;
import com.lhjz.portal.component.core.IChatMsg;
import com.lhjz.portal.entity.Channel;
import com.lhjz.portal.entity.ChatChannel;
import com.lhjz.portal.entity.ChatLabel;
import com.lhjz.portal.entity.security.User;
import com.lhjz.portal.model.ChannelPayload;
import com.lhjz.portal.model.ChannelPayload.Cmd;
import com.lhjz.portal.model.RespBody;
import com.lhjz.portal.pojo.Enum.Action;
import com.lhjz.portal.pojo.Enum.ChatLabelType;
import com.lhjz.portal.pojo.Enum.ChatMsgType;
import com.lhjz.portal.pojo.Enum.Status;
import com.lhjz.portal.pojo.Enum.Target;
import com.lhjz.portal.repository.ChannelRepository;
import com.lhjz.portal.repository.ChatChannelRepository;
import com.lhjz.portal.repository.ChatLabelRepository;
import com.lhjz.portal.repository.UserRepository;
import com.lhjz.portal.util.AuthUtil;
import com.lhjz.portal.util.WebUtil;

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
	ChatLabelRepository chatLabelRepository;

	@Autowired
	UserRepository userRepository;

	@Autowired
	MailSender mailSender;

	@Autowired
	ChatChannelRepository chatChannelRepository;

	@Autowired
	IChatMsg chatMsg;

	@Autowired
	SimpMessagingTemplate messagingTemplate;

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
			Channel channel2 = new Channel();
			channel2.setId(channel.getId());
			channel2.setName(channel.getName());
			cc.setChannel(channel2);
		});

		return RespBody.succeed(page);
	}

	@PostMapping("status/update")
	public RespBody listBy(@RequestParam("from") String from, @RequestParam("to") String to,
			@RequestParam("id") Long id, @RequestParam(value = "all", defaultValue = "false") Boolean all) {

		ChatChannel chatChannel = chatChannelRepository.findOne(id);

		if (!AuthUtil.hasChannelAuth(chatChannel)) {
			return RespBody.failed("权限不足！");
		}

		User loginUser = getLoginUser();

		ChatLabel chatLabelFrom = chatLabelRepository.findOneByNameAndChatChannelAndStatusNot(from, chatChannel,
				Status.Deleted);

		if (chatLabelFrom != null) {
			Set<User> voters = chatLabelFrom.getVoters();

			boolean updated = false;

			if (all) {
				for (User user : voters) {
					user.getVoterChatLabels().remove(chatLabelFrom);
				}
				userRepository.save(voters);
				userRepository.flush();

				voters.clear();

				updated = true;
			} else {
				if (voters.contains(loginUser)) {
					loginUser.getVoterChatLabels().remove(chatLabelFrom);
					voters.remove(loginUser);

					userRepository.saveAndFlush(loginUser);

					updated = true;
				}
			}

			if (updated) {
				
				if (voters.size() == 0) {
					chatLabelFrom.setStatus(Status.Deleted);
					chatLabelFrom = chatLabelRepository.saveAndFlush(chatLabelFrom);
				}

				logWithProperties(Action.Vote, Target.ChatLabel, chatLabelFrom.getId(), "name", from);

				chatChannel.setUpdateDate(new Date());
				chatChannelRepository.saveAndFlush(chatChannel);

				chatMsg.put(chatChannel, Action.Delete, ChatMsgType.Label, null, null, null);
				wsSend(chatChannel);
			}

		}

		ChatLabel chatLabelTo = chatLabelRepository.findOneByNameAndChatChannelAndStatusNot(to, chatChannel,
				Status.Deleted);

		if (chatLabelTo == null) {
			chatLabelTo = new ChatLabel();
			chatLabelTo.setName(to);
			chatLabelTo.setDescription(to);
			chatLabelTo.setChatChannel(chatChannel);
			chatLabelTo.setType(ChatLabelType.Tag);

			ChatLabel chatLabel2 = chatLabelRepository.saveAndFlush(chatLabelTo);

			chatLabel2.getVoters().add(loginUser);

			loginUser.getVoterChatLabels().add(chatLabel2);

			userRepository.saveAndFlush(loginUser);

			logWithProperties(Action.Create, Target.ChatLabel, chatLabel2.getId(), "name", to);

			chatChannel.setUpdateDate(new Date());
			chatChannelRepository.saveAndFlush(chatChannel);

			chatMsg.put(chatChannel, Action.Create, ChatMsgType.Label, null, null, null);
			wsSend(chatChannel);

			userRepository.saveAndFlush(loginUser);

		} else {

			Set<User> voters = chatLabelTo.getVoters();
			if (!voters.contains(loginUser)) {
				loginUser.getVoterChatLabels().add(chatLabelTo);
				voters.add(loginUser);

				logWithProperties(Action.UnVote, Target.ChatLabel, chatLabelTo.getId(), "name", to);

				chatChannel.setUpdateDate(new Date());
				chatChannelRepository.saveAndFlush(chatChannel);

				chatMsg.put(chatChannel, Action.Update, ChatMsgType.Label, null, null, null);
				wsSend(chatChannel);

				userRepository.saveAndFlush(loginUser);
			}
		}

		return RespBody.succeed(id);
	}

	private void wsSend(ChatChannel chatChannel) {
		try {
			messagingTemplate.convertAndSend("/channel/update",
					ChannelPayload.builder().uuid(UUID.randomUUID().toString()).username(WebUtil.getUsername())
							.cmd(Cmd.R).id(chatChannel.getChannel().getId()).cid(chatChannel.getId()).build());
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
	}

	@PostMapping("label/remove")
	public RespBody removeLabel(@RequestParam("id") Long id) {

		ChatLabel chatLabel = chatLabelRepository.findOne(id);

		User loginUser = getLoginUser();

		if (chatLabel != null) {
			Set<User> voters = chatLabel.getVoters();
			if (voters.contains(loginUser)) {
				loginUser.getVoterChatLabels().remove(chatLabel);
				voters.remove(loginUser);

				if (voters.size() == 0) {
					chatLabel.setStatus(Status.Deleted);
					chatLabel = chatLabelRepository.saveAndFlush(chatLabel);
				}

				logWithProperties(Action.Vote, Target.ChatLabel, chatLabel.getId(), "name", chatLabel.getName());

				chatLabel.getChatChannel().setUpdateDate(new Date());
				chatChannelRepository.saveAndFlush(chatLabel.getChatChannel());

				chatMsg.put(chatLabel.getChatChannel(), Action.Delete, ChatMsgType.Label, null, null, null);
				wsSend(chatLabel.getChatChannel());

				userRepository.saveAndFlush(loginUser);
			}
		}

		return RespBody.succeed(id);
	}

	@PostMapping("remove")
	public RespBody remove(@RequestParam("id") Long id, @RequestParam("label") String label) {

		ChatChannel chatChannel = chatChannelRepository.findOne(id);

		if (!isSuperOrCreator(chatChannel.getChannel().getCreator())) {
			return RespBody.failed("权限不足！");
		}

		final ChatLabel chatLabel = chatLabelRepository.findOneByNameAndChatChannelAndStatusNot(label, chatChannel,
				Status.Deleted);

		if (chatLabel != null) {

			Set<User> voters = chatLabel.getVoters();

			voters.forEach(voter -> {
				voter.getVoterChatLabels().remove(chatLabel);
			});

			logWithProperties(Action.Vote, Target.ChatLabel, chatLabel.getId(), "name", chatLabel.getName());

			userRepository.save(voters);
			userRepository.flush();

			chatLabel.setStatus(Status.Deleted);
			chatLabelRepository.saveAndFlush(chatLabel);

			chatChannel.setUpdateDate(new Date());
			chatChannelRepository.saveAndFlush(chatChannel);

			chatMsg.put(chatChannel, Action.Delete, ChatMsgType.Label, null, null, null);
			wsSend(chatChannel);
		}

		return RespBody.succeed(id);
	}

}
