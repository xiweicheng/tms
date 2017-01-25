/**
 * 版权所有 (TMS)
 */
package com.lhjz.portal.controller;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.lhjz.portal.base.BaseController;
import com.lhjz.portal.component.MailSender2;
import com.lhjz.portal.entity.Channel;
import com.lhjz.portal.entity.ChatAt;
import com.lhjz.portal.entity.ChatChannel;
import com.lhjz.portal.entity.ChatStow;
import com.lhjz.portal.entity.security.User;
import com.lhjz.portal.model.Mail;
import com.lhjz.portal.model.Poll;
import com.lhjz.portal.model.RespBody;
import com.lhjz.portal.pojo.Enum.Action;
import com.lhjz.portal.pojo.Enum.Status;
import com.lhjz.portal.pojo.Enum.Target;
import com.lhjz.portal.pojo.Enum.VoteType;
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
@RequestMapping("admin/chat/channel")
public class ChatChannelController extends BaseController {

	static Logger logger = LoggerFactory.getLogger(ChatChannelController.class);

	@Autowired
	ChannelRepository channelRepository;
	
	@Autowired
	ChatAtRepository chatAtRepository;
	
	@Autowired
	ChatStowRepository chatStowRepository;

	@Autowired
	ChatChannelRepository chatChannelRepository;

	@Autowired
	UserRepository userRepository;

	@Autowired
	MailSender2 mailSender;

	@RequestMapping(value = "create", method = RequestMethod.POST)
	@ResponseBody
	public RespBody create(@RequestParam("url") String url,
			@RequestParam(value = "usernames", required = false) String usernames,
			@RequestParam("channelId") Long channelId, @RequestParam("content") String content,
			@RequestParam("contentHtml") String contentHtml) {
		
		if (StringUtil.isEmpty(content)) {
			return RespBody.failed("提交内容不能为空!");
		}

		Channel channel = channelRepository.findOne(channelId);

		ChatChannel chatChannel = new ChatChannel();
		chatChannel.setChannel(channel);
		chatChannel.setContent(content);

		ChatChannel chatChannel2 = chatChannelRepository.saveAndFlush(chatChannel);
		
		final String href = url + "?id=" + chatChannel2.getId();
		final String html = contentHtml;
		final User loginUser = getLoginUser();

		final Mail mail = Mail.instance();
		if (StringUtil.isNotEmpty(usernames)) {

			Map<String, User> atUserMap = new HashMap<String, User>();

			if (StringUtil.isNotEmpty(usernames)) {
				String[] usernameArr = usernames.split(",");
				Arrays.asList(usernameArr).stream().forEach((username) -> {
					User user = getUser(username);
					if (user != null) {
						mail.addUsers(user);
						atUserMap.put(user.getUsername(), user);
					}
				});
			}

			List<ChatAt> chatAtList = new ArrayList<ChatAt>();
			// 保存chatAt关系
			atUserMap.values().forEach((user) -> {
				ChatAt chatAt = new ChatAt();
				chatAt.setChatChannel(chatChannel2);
				chatAt.setAtUser(user);

				chatAtList.add(chatAt);
			});

			chatAtRepository.save(chatAtList);
			chatAtRepository.flush();

			ThreadUtil.exec(() -> {

				try {
					Thread.sleep(3000);
					mailSender.sendHtml(String.format("TMS-沟通频道@消息_%s",
							DateUtil.format(new Date(), DateUtil.FORMAT7)),
							TemplateUtil.process("templates/mail/mail-dynamic",
									MapUtil.objArr2Map("user", loginUser,
											"date", new Date(), "href", href,
											"title", "下面的沟通频道消息中有@到你", "content",
											html)), mail.get());
					logger.info("沟通频道邮件发送成功！");
				} catch (Exception e) {
					e.printStackTrace();
					logger.error("沟通频道邮件发送失败！");
				}

			});
		}

		return RespBody.succeed(chatChannel2);
	}

	@RequestMapping(value = "listBy", method = RequestMethod.GET)
	@ResponseBody
	public RespBody listMy(@RequestParam(value = "id", required = false) Long id,
			@RequestParam("channelId") Long channelId,
			@PageableDefault(sort = { "id" }, direction = Direction.DESC) Pageable pageable) {

		Channel channel = channelRepository.findOne(channelId);
		int limit = pageable.getPageSize();

		if (StringUtil.isNotEmpty(id)) {
			long cntGtId = chatChannelRepository.countGtId(channel, id);
			int size = limit;
			long page = cntGtId / size;
			if (cntGtId % size == 0) {
				page--;
			}

			pageable = new PageRequest(page > -1 ? (int) page : 0, size, Direction.DESC, "id");
		}

		Page<ChatChannel> page = chatChannelRepository.findByChannel(channel, pageable);

		return RespBody.succeed(page);
	}

	@RequestMapping(value = "update", method = RequestMethod.POST)
	@ResponseBody
	public RespBody update(@RequestParam("url") String url,
			@RequestParam(value = "usernames", required = false) String usernames, @RequestParam("id") Long id,
			@RequestParam("version") Long version,
			@RequestParam("content") String content, @RequestParam(value = "diff", required = false) String diff,
			@RequestParam(value = "contentHtml", required = false) String contentHtml,
			@RequestParam(value = "contentHtmlOld", required = false) String contentHtmlOld) {

		if (StringUtil.isEmpty(content)) {
			return RespBody.failed("更新内容不能为空!");
		}

		ChatChannel chatChannel = chatChannelRepository.findOne(id);
		
		Boolean isOpenEdit = chatChannel.getOpenEdit() == null ? false : chatChannel
				.getOpenEdit();

		if (!isSuperOrCreator(chatChannel.getCreator().getUsername()) && !isOpenEdit) {
			return RespBody.failed("您没有权限编辑该消息内容!");
		}
		
		if (chatChannel.getVersion() != version.longValue()) {
			return RespBody.failed("该频道消息已经被其他人更新,请刷新消息重新编辑提交!");
		}

		if (content.equals(chatChannel.getContent())) {
			return RespBody.failed("更新内容没有任何变更的内容!");
		}
		
		String contentOld = chatChannel.getContent();

		chatChannel.setContent(content);

		ChatChannel chatChannel2 = chatChannelRepository.saveAndFlush(chatChannel);
		
		logWithProperties(Action.Update, Target.ChatChannel, chatChannel2.getId(), "content", contentOld);

		final User loginUser = getLoginUser();
		final String href = url + "?id=" + chatChannel2.getId();
		final String html;
		if(StringUtil.isNotEmpty(diff)) {
			html = "<h3>内容(Markdown)变更对比:</h3><b>原文链接:</b> <a href=\"" + href + "\">" + href + "</a><hr/>" + diff;
		} else {
			html = "<h3>编辑后内容:</h3>" + contentHtml + "<hr/><h3>编辑前内容:</h3>" + contentHtmlOld;
		}

		final Mail mail = Mail.instance();

		if (StringUtil.isNotEmpty(usernames)) {

			Map<String, User> atUserMap = new HashMap<String, User>();

			if (StringUtil.isNotEmpty(usernames)) {
				String[] usernameArr = usernames.split(",");
				Arrays.asList(usernameArr).stream().forEach((username) -> {
					User user = getUser(username);
					if (user != null) {
						mail.addUsers(user);
						atUserMap.put(user.getUsername(), user);
					}
				});
			}

			List<ChatAt> chatAtList = new ArrayList<ChatAt>();
			// 保存chatAt关系
			atUserMap.values().forEach((user) -> {

				ChatAt chatAt2 = chatAtRepository.findOneByChatChannelAndAtUser(chatChannel2, user);
				if (chatAt2 == null) {
					ChatAt chatAt = new ChatAt();
					chatAt.setChatChannel(chatChannel2);
					chatAt.setAtUser(user);

					chatAtList.add(chatAt);
				} else {
					chatAt2.setStatus(Status.New);

					chatAtList.add(chatAt2);
				}
			});
			chatAtRepository.save(chatAtList);
			chatAtRepository.flush();

			ThreadUtil.exec(() -> {

				try {
					Thread.sleep(3000);
					mailSender.sendHtml(
							String.format("TMS-沟通频道编辑@消息_%s", DateUtil.format(new Date(), DateUtil.FORMAT7)),
							TemplateUtil.process("templates/mail/mail-dynamic", MapUtil.objArr2Map("user", loginUser,
									"date", new Date(), "href", href, "title", "下面编辑的沟通频道消息中有@到你", "content", html)),
							mail.get());
					logger.info("沟通频道编辑邮件发送成功！");
				} catch (Exception e) {
					e.printStackTrace();
					logger.error("沟通频道编辑邮件发送失败！");
				}

			});
		}

		return RespBody.succeed(chatChannel2);
	}

	@RequestMapping(value = "delete", method = RequestMethod.POST)
	@ResponseBody
	public RespBody delete(@RequestParam("id") Long id) {
		
		ChatChannel chatChannel = chatChannelRepository.findOne(id);

		if (!isSuperOrCreator(chatChannel.getCreator().getUsername())) {
			return RespBody.failed("您没有权限删除该消息内容!");
		}
		
		List<ChatAt> chatAts = chatAtRepository.findByChatChannel(chatChannel);
		chatAtRepository.delete(chatAts);
		chatAtRepository.flush();
		
		List<ChatStow> chatStows = chatStowRepository.findByChatChannel(chatChannel);
		chatStowRepository.delete(chatStows);
		chatStowRepository.flush();

		chatChannelRepository.delete(id);
		
		logWithProperties(Action.Delete, Target.ChatChannel, id, "content", chatChannel.getContent());

		return RespBody.succeed(id);
	}
		
	@RequestMapping(value = "get", method = RequestMethod.GET)
	@ResponseBody
	public RespBody get(@RequestParam("id") Long id) {
		
		ChatChannel chatChannel = chatChannelRepository.findOne(id);
	
		return RespBody.succeed(chatChannel);
	}

	@RequestMapping(value = "latest", method = RequestMethod.GET)
	@ResponseBody
	public RespBody latest(@RequestParam("id") Long id,
			@RequestParam("channelId") Long channelId) {
		
		List<ChatChannel> chats = chatChannelRepository.latest(channelRepository.findOne(channelId), id);
	
		return RespBody.succeed(chats);
	}
	
	@RequestMapping(value = "more", method = RequestMethod.GET)
	@ResponseBody
	public RespBody more(@RequestParam("start") Long start, @RequestParam("last") Boolean last,
			@RequestParam("size") Integer size, @RequestParam("channelId") Long channelId) {

		long count = 0;
		List<ChatChannel> chats = null;
		
		Channel channel = channelRepository.findOne(channelId);
		
		if (last) {
			count = chatChannelRepository.countAllOld(channel, start);
			chats = chatChannelRepository.queryMoreOld(channel, start, size);
		} else {
			count = chatChannelRepository.countAllNew(channel, start);
			chats = chatChannelRepository.queryMoreNew(channel, start, size);
		}

		return RespBody.succeed(chats).addMsg(count);
	}
	
	@RequestMapping(value = "search", method = RequestMethod.GET)
	@ResponseBody
	public RespBody search(@RequestParam("search") String search, @RequestParam("channelId") Long channelId,
			@PageableDefault(sort = { "id" }, direction = Direction.DESC) Pageable pageable) {

		if (StringUtil.isEmpty(search)) {
			return RespBody.failed("检索条件不能为空!");
		}
		
		Channel channel = channelRepository.findOne(channelId);

		String _search = "%" + search + "%";
		List<ChatChannel> chats = chatChannelRepository.queryAboutMe(channel, _search, pageable.getOffset(),
				pageable.getPageSize());
		long cnt = chatChannelRepository.countAboutMe(channel, _search);

		Page<ChatChannel> page = new PageImpl<>(chats, pageable, cnt);

		return RespBody.succeed(page);
	}
	

	@RequestMapping(value = "stow", method = RequestMethod.POST)
	@ResponseBody
	public RespBody stow(@RequestParam("id") Long id) {

		ChatChannel chatChannel = chatChannelRepository.findOne(id);

		if (chatChannel == null) {
			return RespBody.failed("收藏频道消息不存在,可能已经被删除!");
		}

		User loginUser = getLoginUser();
		ChatStow chatStow = chatStowRepository.findOneByChatChannelAndStowUser(chatChannel,
				loginUser);

		if (chatStow != null) {
			return RespBody.failed("收藏频道消息重复!");
		}

		ChatStow chatStow2 = new ChatStow();
		chatStow2.setChatChannel(chatChannel);
		chatStow2.setStowUser(loginUser);

		ChatStow chatStow3 = chatStowRepository.saveAndFlush(chatStow2);

		return RespBody.succeed(chatStow3);
	}
	
	@RequestMapping(value = "removeStow", method = RequestMethod.POST)
	@ResponseBody
	public RespBody removeStow(@RequestParam("id") Long id) {

		chatStowRepository.delete(id);

		return RespBody.succeed(id);
	}

	@RequestMapping(value = "getStows", method = RequestMethod.GET)
	@ResponseBody
	public RespBody getStows() {

		List<ChatStow> chatStows = chatStowRepository.findByChatChannelNotNullAndStowUserAndStatus(
				getLoginUser(), Status.New);
		
//		chatStows = chatStows.stream().filter((cs) -> {
//			return !cs.getChatChannel().getChannel().getStatus().equals(Status.Deleted);
//		}).collect(Collectors.toList());

		return RespBody.succeed(chatStows);
	}
	
	@RequestMapping(value = "getAts", method = RequestMethod.GET)
	@ResponseBody
	public RespBody getAts(
			@PageableDefault(sort = { "id" }, direction = Direction.DESC) Pageable pageable) {

		Page<ChatAt> chatAts = chatAtRepository.findByChatChannelNotNullAndAtUserAndStatus(
				getLoginUser(), Status.New, pageable);

		return RespBody.succeed(chatAts);
	}

	@RequestMapping(value = "markAsReaded", method = RequestMethod.POST)
	@ResponseBody
	public RespBody markAsReaded(@RequestParam("chatAtId") Long chatAtId) {

		ChatAt chatAt = chatAtRepository.findOne(chatAtId);
		if (chatAt == null) {
			return RespBody.failed("@消息不存在,可能已经被删除!");
		}
		chatAt.setStatus(Status.Readed);
		chatAtRepository.saveAndFlush(chatAt);

		return RespBody.succeed(chatAt);
	}
	
	@RequestMapping(value = "markAsReadedByChat", method = RequestMethod.POST)
	@ResponseBody
	public RespBody markAsReadedByChat(@RequestParam("chatId") Long chatId) {
		
		ChatChannel chatChannel = chatChannelRepository.findOne(chatId);
		if (chatChannel == null) {
			return RespBody.failed("@頻道消息不存在,可能已经被删除!");
		}
		
		int cnt = chatAtRepository.markChatChannelAsReaded(chatChannel, getLoginUser());
		
		return RespBody.succeed(cnt);
	}

	@RequestMapping(value = "markAllAsReaded", method = RequestMethod.POST)
	@ResponseBody
	public RespBody markAllAsReaded() {

		int cnt = chatAtRepository.markChatChannelAllAsReaded(getLoginUser());

		return RespBody.succeed(cnt);
	}
	
	@RequestMapping(value = "openEdit", method = RequestMethod.POST)
	@ResponseBody
	public RespBody openEdit(@RequestParam("id") Long id,
			@RequestParam("open") Boolean open) {

		ChatChannel chatChannel = chatChannelRepository.findOne(id);

		if (chatChannel == null) {
			return RespBody.failed("操作频道消息不存在,可能已经被删除!");
		}

		chatChannel.setOpenEdit(open);
		chatChannelRepository.saveAndFlush(chatChannel);

		return RespBody.succeed();
	}
	
	private boolean isVoterExists(String voters) {
		boolean isExits = false;
		if (voters != null) {
			String loginUsername = WebUtil.getUsername();
			String[] voterArr = voters.split(",");

			for (String voter : voterArr) {
				if (voter.equals(loginUsername)) {
					isExits = true;
					break;
				}
			}
		}

		return isExits;
	}
	
	@RequestMapping(value = "vote", method = RequestMethod.POST)
	@ResponseBody
	public RespBody vote(@RequestParam("id") Long id,
			@RequestParam("url") String url,
			@RequestParam("contentHtml") String contentHtml,
			@RequestParam(value = "type", required = false) String type) {

		ChatChannel chatChannel = chatChannelRepository.findOne(id);
		if (chatChannel == null) {
			return RespBody.failed("投票频道消息不存在!");
		}
		String loginUsername = WebUtil.getUsername();

		ChatChannel chatChannel2 = null;

		String title = "";
		final User loginUser = getLoginUser();

		if (VoteType.Zan.name().equalsIgnoreCase(type)) {
			String voteZan = chatChannel.getVoteZan();
			if (isVoterExists(voteZan)) {
				return RespBody.failed("您已经投票[赞]过！");
			} else {
				chatChannel.setVoteZan(voteZan == null ? loginUsername : voteZan + ','
						+ loginUsername);
				
				Integer voteZanCnt = chatChannel.getVoteZanCnt();
				if (voteZanCnt == null) {
					voteZanCnt = 0;
				}
				chatChannel.setVoteZanCnt(++voteZanCnt);

				chatChannel2 = chatChannelRepository.saveAndFlush(chatChannel);
				title = loginUser.getName() + "[" + loginUsername
						+ "]赞了你的频道消息!";
			}

		} else {
			String voteCai = chatChannel.getVoteCai();
			if (isVoterExists(voteCai)) {
				return RespBody.failed("您已经投票[踩]过！");
			} else {
				chatChannel.setVoteCai(voteCai == null ? loginUsername : voteCai + ','
						+ loginUsername);
				
				Integer voteCaiCnt = chatChannel.getVoteCaiCnt();
				if (voteCaiCnt == null) {
					voteCaiCnt = 0;
				}
				chatChannel.setVoteCaiCnt(++voteCaiCnt);
				
				chatChannel2 = chatChannelRepository.saveAndFlush(chatChannel);
				title = loginUser.getName() + "[" + loginUsername
						+ "]踩了你的频道消息!";
			}
		}

		final String href = url + "?id=" + id;
		final String titleHtml = title;
		final Mail mail = Mail.instance().addUsers(chatChannel.getCreator());
		final String html = "<h3>投票频道消息内容:</h3><hr/>" + contentHtml;

		ThreadUtil.exec(() -> {

			try {
				Thread.sleep(3000);
				mailSender.sendHtml(String.format("TMS-沟通频道消息投票@消息_%s",
						DateUtil.format(new Date(), DateUtil.FORMAT7)),
						TemplateUtil.process("templates/mail/mail-dynamic",
								MapUtil.objArr2Map("user", loginUser, "date",
										new Date(), "href", href, "title",
										titleHtml, "content", html)), mail
								.get());
				logger.info("沟通频道消息投票邮件发送成功！");
			} catch (Exception e) {
				e.printStackTrace();
				logger.error("沟通频道消息投票邮件发送失败！");
			}

		});

		log(Action.Vote, Target.ChatChannel, chatChannel.getId(), chatChannel2);

		return RespBody.succeed(chatChannel2);
	}
	
	@RequestMapping(value = "poll", method = RequestMethod.GET)
	@ResponseBody
	public RespBody poll(
			@RequestParam("channelId") Long channelId,
			@RequestParam("lastChatChannelId") Long lastChatChannelId,
			@RequestParam(value = "isAt", required = false, defaultValue = "false") Boolean isAt) {

		long cnt = isAt ? chatAtRepository.countChatChannelRecentAt(
				WebUtil.getUsername(), lastChatChannelId) : chatChannelRepository
				.countQueryRecent(channelId, lastChatChannelId);
				
		long cntAtUserNew = chatAtRepository.countChatChannelAtUserNew(WebUtil
				.getUsername());

		return RespBody.succeed(new Poll(channelId, lastChatChannelId, isAt, cnt, cntAtUserNew));
	}
}
