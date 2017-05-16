/**
 * 版权所有 (TMS)
 */
package com.lhjz.portal.controller;

import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.lhjz.portal.base.BaseController;
import com.lhjz.portal.component.MailSender2;
import com.lhjz.portal.entity.Channel;
import com.lhjz.portal.entity.ChatChannel;
import com.lhjz.portal.entity.security.User;
import com.lhjz.portal.model.Mail;
import com.lhjz.portal.model.RespBody;
import com.lhjz.portal.pojo.Enum.Status;
import com.lhjz.portal.repository.ChannelRepository;
import com.lhjz.portal.repository.ChatAtRepository;
import com.lhjz.portal.repository.ChatChannelRepository;
import com.lhjz.portal.repository.ChatStowRepository;
import com.lhjz.portal.repository.UserRepository;
import com.lhjz.portal.util.DateUtil;
import com.lhjz.portal.util.MapUtil;
import com.lhjz.portal.util.StringUtil;
import com.lhjz.portal.util.TemplateUtil;
import com.lhjz.portal.util.ThreadUtil;
import com.lhjz.portal.util.WebUtil;

/**
 * 
 * @author xi
 * 
 * @date 2015年3月28日 下午1:19:05
 * 
 */
@Controller
@RequestMapping("admin/channel")
public class ChannelController extends BaseController {

	static Logger logger = LoggerFactory.getLogger(ChannelController.class);

	@Autowired
	ChannelRepository channelRepository;
	
	@Autowired
	ChatChannelRepository chatChannelRepository;

	@Autowired
	UserRepository userRepository;
	
	@Autowired
	ChatAtRepository chatAtRepository;
	
	@Autowired
	ChatStowRepository chatStowRepository;

	@Autowired
	MailSender2 mailSender;

	@RequestMapping(value = "create", method = RequestMethod.POST)
	@ResponseBody
	public RespBody create(@RequestParam("name") String name, @RequestParam("title") String title,
			@RequestParam(value = "desc", required = false) String desc,
			@RequestParam(value = "privated", required = false) Boolean privated) {
		
		if(StringUtil.isEmpty(name)) {
			return RespBody.failed("标识不能为空!");
		}
		
		if(StringUtil.isEmpty(title)) {
			return RespBody.failed("名称不能为空!");
		}
		
		Channel channel = new Channel();
		channel.setName(name);
		channel.setTitle(title);
		if (desc != null) {
			channel.setDescription(desc);
		}
		if (privated != null) {
			channel.setPrivated(privated);
		}
		User loginUser = getLoginUser();
		channel.setOwner(loginUser);

		Channel channel2 = channelRepository.saveAndFlush(channel);

		loginUser.getJoinChannels().add(channel2);
		User user = userRepository.saveAndFlush(loginUser);

		channel2.getMembers().add(user);

		return RespBody.succeed(channel2);
	}

	@RequestMapping(value = "listMy", method = RequestMethod.GET)
	@ResponseBody
	public RespBody listMy() {

		User loginUser = getLoginUser();
		Set<Channel> joinChannels = loginUser.getJoinChannels();
		
		Set<Channel> channels = joinChannels.stream().filter((channel) -> {
			return !channel.getStatus().equals(Status.Deleted);
		}).collect(Collectors.toSet());

		return RespBody.succeed(channels);
	}
	
	@RequestMapping(value = "get", method = RequestMethod.GET)
	@ResponseBody
	public RespBody get(@RequestParam("id") Long id) {
		
		Channel channel = channelRepository.findOne(id);
		
		return RespBody.succeed(channel);
	}

	@RequestMapping(value = "update", method = RequestMethod.POST)
	@ResponseBody
	public RespBody update(@RequestParam("id") Long id, @RequestParam(value = "title", required = false) String title,
			@RequestParam(value = "desc", required = false) String desc,
			@RequestParam(value = "privated", required = false) Boolean privated) {

		Channel channel = channelRepository.findOne(id);
		
		if (!isSuperOrCreator(channel.getCreator().getUsername())) {
			return RespBody.failed("您没有权限编辑该频道!");
		}
		
		if (StringUtil.isNotEmpty(title)) {
			channel.setTitle(title);
		}
		if (desc != null) {
			channel.setDescription(desc);
		}
		if (privated != null) {
			channel.setPrivated(privated);
		}

		Channel channel2 = channelRepository.saveAndFlush(channel);

		return RespBody.succeed(channel2);
	}

	@RequestMapping(value = "delete", method = RequestMethod.POST)
	@ResponseBody
	public RespBody delete(@RequestParam("id") Long id) {

		Channel channel = channelRepository.findOne(id);
		
		if (!isSuperOrCreator(channel.getCreator().getUsername())) {
			return RespBody.failed("您没有权限删除该频道!");
		}
		
		// 删除收藏|@
		chatStowRepository.deleteByChannel(id);
		chatAtRepository.deleteByChannel(id);
		
		channel.setStatus(Status.Deleted);
		
		channelRepository.saveAndFlush(channel);

		return RespBody.succeed(id);
	}

	@RequestMapping(value = "addMember", method = RequestMethod.POST)
	@ResponseBody
	public RespBody addMember(@RequestParam("id") Long id, @RequestParam("members") String members,
			@RequestParam("baseUrl") String baseUrl,
			@RequestParam("path") String path) {

		Channel channel = channelRepository.findOne(id);
		
		if (channel.getPrivated() && !isSuperOrCreator(channel.getCreator().getUsername())) {
			return RespBody.failed("您没有权限添加成员到该频道!");
		}

		String[] ms = members.split(",");
		final Mail mail = Mail.instance();

		Stream.of(ms).forEach((m) -> {
			User user = userRepository.findOne(m);
			if (user != null && !channel.getMembers().contains(user)) {
				user.getJoinChannels().add(channel);
				userRepository.saveAndFlush(user);
				channel.getMembers().add(user);

				mail.addUsers(user);
				
				// 用户频道消息提醒
				ChatChannel chatChannel = new ChatChannel();
				chatChannel.setChannel(channel);
				chatChannel.setContent("## ~频道消息播报~\n> {~" + user.getUsername() + "} 被**添加到**该频道!");
				
				chatChannelRepository.saveAndFlush(chatChannel);
			}
		});
		
		final User loginUser = getLoginUser();
		
		final String href = baseUrl + path + "#/chat/" + channel.getName();
		final String html = loginUser.getName() + " 将你加入沟通频道 [" + channel.getTitle() + "], 点击上面链接进入!";
		
		ThreadUtil.exec(() -> {

			try {
				Thread.sleep(3000);
				mailSender.sendHtml(String.format("TMS-频道加入通知_%s",
						DateUtil.format(new Date(), DateUtil.FORMAT7)),
						TemplateUtil.process("templates/mail/mail-dynamic",
								MapUtil.objArr2Map("user", loginUser,
										"date", new Date(), "href", href,
										"title", "下面的沟通消息中有@到你", "content",
										html)), mail.get());
				logger.info("频道加入参与者邮件发送成功！");
			} catch (Exception e) {
				e.printStackTrace();
				logger.error("频道加入参与者邮件发送失败！");
			}

		});

		return RespBody.succeed(channel);
	}
	
	@RequestMapping(value = "removeMember", method = RequestMethod.POST)
	@ResponseBody
	public RespBody removeMember(@RequestParam("id") Long id, @RequestParam("members") String members) {

		Channel channel = channelRepository.findOne(id);
		
		if (!isSuperOrCreator(channel.getCreator().getUsername())) {
			return RespBody.failed("您没有权限从该频道移除成员!");
		}

		String[] ms = members.split(",");

		Stream.of(ms).forEach((m) -> {
			User user = userRepository.findOne(m);
			if (user != null && !channel.getOwner().getUsername().equals(user.getUsername())) {
				user.getJoinChannels().remove(channel);
				userRepository.saveAndFlush(user);
				channel.getMembers().remove(user);
				
				// 用户频道消息提醒
				ChatChannel chatChannel = new ChatChannel();
				chatChannel.setChannel(channel);
				chatChannel.setContent("## ~频道消息播报~\n> {~" + user.getUsername() + "} 被**移除出**该频道!");
				
				chatChannelRepository.saveAndFlush(chatChannel);
			}
		});

		return RespBody.succeed(channel);
	}

	@RequestMapping(value = "list", method = RequestMethod.GET)
	@ResponseBody
	public RespBody list() {

		List<Channel> channels = channelRepository.findAll().stream().filter((c) -> {
			return !c.getStatus().equals(Status.Deleted);
		}).collect(Collectors.toList());

		return RespBody.succeed(channels);
	}
	
	@RequestMapping(value = "listPublic", method = RequestMethod.GET)
	@ResponseBody
	public RespBody listPublic() {
		
		List<Channel> channels = channelRepository.findAll().stream().filter((c) -> {
			return !c.getStatus().equals(Status.Deleted) && !c.getPrivated();
		}).collect(Collectors.toList());
		
		return RespBody.succeed(channels);
	}

	@RequestMapping(value = "join", method = RequestMethod.POST)
	@ResponseBody
	public RespBody join(@RequestParam("id") Long id) {

		Channel channel = channelRepository.findOne(id);

		if (channel.getPrivated()) {
			return RespBody.failed("非公开频道,没有加入权限!");
		}

		final User loginUser = getLoginUser();
		if (!channel.getMembers().contains(loginUser)) {
			loginUser.getJoinChannels().add(channel);
			userRepository.saveAndFlush(loginUser);
			channel.getMembers().add(loginUser);
			
			// 用户频道消息提醒
			ChatChannel chatChannel = new ChatChannel();
			chatChannel.setChannel(channel);
			chatChannel.setContent("## ~频道消息播报~\n> {~" + loginUser.getUsername() + "} **加入**该频道!");
			
			chatChannelRepository.saveAndFlush(chatChannel);
		}
		
		return RespBody.succeed(channel);
	}
	
	@RequestMapping(value = "leave", method = RequestMethod.POST)
	@ResponseBody
	public RespBody leave(@RequestParam("id") Long id) {

		Channel channel = channelRepository.findOne(id);

		final User loginUser = getLoginUser();

		if (channel.getOwner().equals(loginUser)) {
			return RespBody.failed("频道拥有者不能离开所在频道!");
		}

		if (channel.getMembers().contains(loginUser)) {
			loginUser.getJoinChannels().remove(channel);
			userRepository.saveAndFlush(loginUser);
			channel.getMembers().remove(loginUser);

			// 用户频道消息提醒
			ChatChannel chatChannel = new ChatChannel();
			chatChannel.setChannel(channel);
			chatChannel.setContent("## ~频道消息播报~\n> {~" + loginUser.getUsername() + "} **离开**该频道!");
			
			chatChannelRepository.saveAndFlush(chatChannel);
		}
		
		return RespBody.succeed(channel);
	}
	
	private boolean isMember(Channel channel) {
		if (channel == null) {
			return false;
		}
		return channel.getMembers().stream().anyMatch(m -> m.equals(new User(WebUtil.getUsername())));
	}
	
	@SuppressWarnings("unused")
	private boolean hasAuth(Channel channel) {
		if (channel == null) {
			return false;
		}

		if (!channel.getPrivated()) {
			return true;
		}

		return isMember(channel);

	}
	
	@RequestMapping(value = "subscribe", method = RequestMethod.POST)
	@ResponseBody
	public RespBody subscribe(@RequestParam("id") Long id) {

		Channel channel = channelRepository.findOne(id);

		if (!isMember(channel)) {
			return RespBody.failed("非频道成员,无订阅权限!");
		}

		final User loginUser = getLoginUser();

		if (loginUser.getSubscribeChannels().contains(channel)) {
			return RespBody.failed("重复订阅该频道!");
		}

		loginUser.getSubscribeChannels().add(channel);
		userRepository.saveAndFlush(loginUser);

		channel.getSubscriber().add(loginUser);

		return RespBody.succeed(channel);
	}
	
	@RequestMapping(value = "unsubscribe", method = RequestMethod.POST)
	@ResponseBody
	public RespBody unsubscribe(@RequestParam("id") Long id) {

		Channel channel = channelRepository.findOne(id);

		final User loginUser = getLoginUser();

		if (loginUser.getSubscribeChannels().contains(channel)) {
			loginUser.getSubscribeChannels().remove(channel);
			userRepository.saveAndFlush(loginUser);

			channel.getSubscriber().remove(loginUser);
		}

		return RespBody.succeed(channel);
	}
}
