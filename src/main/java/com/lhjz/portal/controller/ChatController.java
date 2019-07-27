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
import java.util.stream.Stream;

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
import com.lhjz.portal.component.MailSender;
import com.lhjz.portal.constant.SysConstant;
import com.lhjz.portal.entity.Chat;
import com.lhjz.portal.entity.ChatAt;
import com.lhjz.portal.entity.ChatStow;
import com.lhjz.portal.entity.Label;
import com.lhjz.portal.entity.Log;
import com.lhjz.portal.entity.security.Group;
import com.lhjz.portal.entity.security.GroupMember;
import com.lhjz.portal.entity.security.User;
import com.lhjz.portal.model.Mail;
import com.lhjz.portal.model.RespBody;
import com.lhjz.portal.pojo.Enum.Action;
import com.lhjz.portal.pojo.Enum.ChatType;
import com.lhjz.portal.pojo.Enum.Prop;
import com.lhjz.portal.pojo.Enum.Status;
import com.lhjz.portal.pojo.Enum.Target;
import com.lhjz.portal.pojo.Enum.VoteType;
import com.lhjz.portal.repository.ChatAtRepository;
import com.lhjz.portal.repository.ChatRepository;
import com.lhjz.portal.repository.ChatStowRepository;
import com.lhjz.portal.repository.GroupMemberRepository;
import com.lhjz.portal.repository.GroupRepository;
import com.lhjz.portal.repository.LabelRepository;
import com.lhjz.portal.repository.LogRepository;
import com.lhjz.portal.util.CollectionUtil;
import com.lhjz.portal.util.DateUtil;
import com.lhjz.portal.util.MapUtil;
import com.lhjz.portal.util.StringUtil;
import com.lhjz.portal.util.TemplateUtil;
import com.lhjz.portal.util.WebUtil;

/**
 * 
 * @author xi
 * 
 * @date 2015年3月28日 下午1:19:05
 * 
 */
@Controller
@RequestMapping("admin/chat")
public class ChatController extends BaseController {

	static Logger logger = LoggerFactory.getLogger(ChatController.class);

	@Autowired
	ChatRepository chatRepository;

	@Autowired
	GroupRepository groupRepository;

	@Autowired
	GroupMemberRepository groupMemberRepository;

	@Autowired
	LogRepository logRepository;

	@Autowired
	ChatAtRepository chatAtRepository;

	@Autowired
	ChatStowRepository chatStowRepository;
	
	@Autowired
	LabelRepository labelRepository;

	@Autowired
	MailSender mailSender;

	String dynamicAction = "admin/dynamic";

	@RequestMapping(value = "create", method = RequestMethod.POST)
	@ResponseBody
	public RespBody create(
			@RequestParam("baseURL") String baseURL,
			@RequestParam(value = "usernames", required = false) String usernames,
			@RequestParam(value = "groups", required = false) String groups,
			@RequestParam("content") String content,
			@RequestParam(value = "preMore", defaultValue = "true") Boolean preMore,
			@RequestParam("lastId") Long lastId,
			@RequestParam("contentHtml") String contentHtml) {

		if (StringUtil.isEmpty(content)) {
			return RespBody.failed("提交内容不能为空!");
		}

		final User loginUser = getLoginUser();

		Chat chat = new Chat();
		chat.setContent(content);
		chat.setCreateDate(new Date());
		chat.setCreator(loginUser);
		chat.setStatus(Status.New);
		chat.setType(ChatType.Msg);

		Chat chat2 = chatRepository.saveAndFlush(chat);

		log(Action.Create, Target.Chat, chat2.getId());

		final String href = baseURL + dynamicAction + "?id=" + chat2.getId();
		final String html = contentHtml;

		final Mail mail = Mail.instance();
		if (StringUtil.isNotEmpty(usernames) || StringUtil.isNotEmpty(groups)) {

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
			if (StringUtil.isNotEmpty(groups)) {
				String[] groupArr = groups.split(",");
				Arrays.asList(groupArr)
						.stream()
						.forEach(
								(group) -> {
									List<Group> groupList = groupRepository
											.findByGroupName(group);
									if (groupList.size() > 0) {
										List<GroupMember> groupMembers = groupMemberRepository
												.findByGroup(groupList.get(0));
										groupMembers.stream().forEach(
												gm -> {
													User user = getUser(gm
															.getUsername());
													if (user != null) {
														mail.addUsers(user);
														atUserMap.put(user
																.getUsername(),
																user);
													}
												});
									}
								});
			}

			List<ChatAt> chatAtList = new ArrayList<ChatAt>();
			// 保存chatAt关系
			atUserMap.values().forEach((user) -> {
				ChatAt chatAt = new ChatAt();
				chatAt.setChat(chat2);
				chatAt.setAtUser(user);
				chatAt.setCreateDate(new Date());
				chatAt.setCreator(loginUser);

				chatAtList.add(chatAt);
			});

			chatAtRepository.save(chatAtList);
			chatAtRepository.flush();

			try {
				mailSender.sendHtmlByQueue(
						String.format("TMS-沟通动态@消息_%s", DateUtil.format(new Date(), DateUtil.FORMAT7)),
						TemplateUtil.process("templates/mail/mail-dynamic", MapUtil.objArr2Map("user", loginUser,
								"date", new Date(), "href", href, "title", "下面的沟通消息中有@到你", "content", html)),
						getLoginUserName(loginUser), mail.get());
			} catch (Exception e) {
				e.printStackTrace();
			}

		}

		if (!preMore && chatRepository.countQueryRecent(lastId) <= 50) {
			List<Chat> chats = chatRepository.queryRecent(lastId);
			return RespBody.succeed(chats);
		}

		return RespBody.succeed(chat2);
	}

	@RequestMapping(value = "update", method = RequestMethod.POST)
	@ResponseBody
	public RespBody update(
			@RequestParam("id") Long id,
			@RequestParam("version") Long version,
			@RequestParam("content") String content,
			@RequestParam("baseURL") String baseURL,
			@RequestParam(value = "usernames", required = false) String usernames,
			@RequestParam(value = "groups", required = false) String groups,
			@RequestParam(value = "diff", required = false) String diff,
			@RequestParam(value = "contentHtmlOld", required = false) String contentHtmlOld,
			@RequestParam(value = "contentHtml", required = false) String contentHtml) {

		if (StringUtil.isEmpty(content)) {
			return RespBody.failed("更新内容不能为空!");
		}

		final User loginUser = getLoginUser();
		Chat chat = chatRepository.findOne(id);

		Boolean isOpenEdit = chat.getOpenEdit() == null ? false : chat
				.getOpenEdit();

		if (!isSuperOrCreator(chat.getCreator().getUsername()) && !isOpenEdit) {
			return RespBody.failed("您没有权限编辑该消息内容!");
		}

		if (chat.getVersion() != version.longValue()) {
			return RespBody.failed("该消息已经被其他人更新,请刷新消息重新编辑提交!");
		}

		if (content.equals(chat.getContent())) {
			return RespBody.failed("修改内容没有任何变更的内容!");
		}

		chat.setContent(content);
		chat.setUpdateDate(new Date());
		chat.setUpdater(loginUser);
		chat.setStatus(Status.Updated);

		Chat chat2 = chatRepository.saveAndFlush(chat);

		log(Action.Update, Target.Chat, chat2.getId());

		final String href = baseURL + dynamicAction + "?id=" + chat2.getId();
		final String html;
		if(StringUtil.isNotEmpty(diff)) {
			html = "<h3>内容(Markdown)变更对比:</h3><b>原文链接:</b> <a href=\"" + href + "\">" + href + "</a><hr/>" + diff;
		} else {
			html = "<h3>编辑后内容:</h3>" + contentHtml + "<hr/><h3>编辑前内容:</h3>" + contentHtmlOld;
		}

		final Mail mail = Mail.instance();

		if (StringUtil.isNotEmpty(usernames) || StringUtil.isNotEmpty(groups)) {

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
			if (StringUtil.isNotEmpty(groups)) {
				String[] groupArr = groups.split(",");
				Arrays.asList(groupArr)
						.stream()
						.forEach(
								(group) -> {
									List<Group> groupList = groupRepository
											.findByGroupName(group);
									if (groupList.size() > 0) {
										List<GroupMember> groupMembers = groupMemberRepository
												.findByGroup(groupList.get(0));
										groupMembers.stream().forEach(
												gm -> {
													User user = getUser(gm
															.getUsername());
													if (user != null) {
														mail.addUsers(user);
														atUserMap.put(user
																.getUsername(),
																user);
													}
												});
									}
								});
			}

			List<ChatAt> chatAtList = new ArrayList<ChatAt>();
			// 保存chatAt关系
			atUserMap.values().forEach(
					(user) -> {

						ChatAt chatAt2 = chatAtRepository
								.findOneByChatAndAtUser(chat2, user);
						if (chatAt2 == null) {
							ChatAt chatAt = new ChatAt();
							chatAt.setChat(chat2);
							chatAt.setAtUser(user);

							chatAtList.add(chatAt);
						} else {
							chatAt2.setStatus(Status.New);

							chatAtList.add(chatAt2);
						}
					});
			chatAtRepository.save(chatAtList);
			chatAtRepository.flush();

			try {
				mailSender.sendHtmlByQueue(
						String.format("TMS-沟通动态编辑@消息_%s", DateUtil.format(new Date(), DateUtil.FORMAT7)),
						TemplateUtil.process("templates/mail/mail-dynamic", MapUtil.objArr2Map("user", loginUser,
								"date", new Date(), "href", href, "title", "下面编辑的沟通消息中有@到你", "content", html)),
						getLoginUserName(loginUser), mail.get());
			} catch (Exception e) {
				e.printStackTrace();
			}

		}

		return RespBody.succeed(chat2);
	}

	@RequestMapping(value = "delete", method = RequestMethod.POST)
	@ResponseBody
	public RespBody delete(@RequestParam("id") Long id) {

		Chat chat = chatRepository.findOne(id);
		if (chat == null) {
			return RespBody.failed("删除聊天内容不存在!");
		}

		if (!isSuperOrCreator(chat.getCreator().getUsername())) {
			return RespBody.failed("您没有权限删除该消息内容!");
		}

		List<ChatAt> chatAts = chatAtRepository.findByChat(chat);
		chatAtRepository.delete(chatAts);
		chatAtRepository.flush();

		List<ChatStow> chatStows = chatStowRepository.findByChat(chat);
		chatStowRepository.delete(chatStows);
		chatStowRepository.flush();

		chatRepository.delete(chat);
		chatRepository.flush();

		log(Action.Delete, Target.Chat, chat.getId(), chat);

		return RespBody.succeed(id);
	}

	@RequestMapping(value = { "get", "get/unmask" }, method = RequestMethod.GET)
	@ResponseBody
	public RespBody get(@RequestParam("id") Long id) {

		Chat chat = chatRepository.findOne(id);
		if (chat == null) {
			return RespBody.failed("获取聊天内容不存在!");
		}

		return RespBody.succeed(chat);
	}

	@RequestMapping(value = { "poll", "poll/unmask" }, method = RequestMethod.GET)
	@ResponseBody
	public RespBody poll(
			@RequestParam("lastId") Long lastId,
			@RequestParam("lastEvtId") Long lastEvtId,
			@RequestParam(value = "isAt", required = false, defaultValue = "false") Boolean isAt) {

		long cnt = isAt ? chatRepository.countQueryRecentAt(
				WebUtil.getUsername(), lastId) : chatRepository
				.countQueryRecent(lastId);
		long cntLogs = logRepository.countQueryRecent(lastEvtId);
		long cntAtUserNew = chatAtRepository.countChatAtUserNew(WebUtil
				.getUsername());

		Long[] data = new Long[] { lastId, cnt, lastEvtId, cntLogs,
				cntAtUserNew };

		List<Log> logs = logRepository.queryRecent(lastEvtId);

		return RespBody.succeed(data).addMsg(logs);
	}

	@RequestMapping(value = { "getNews", "getNews/unmask" }, method = RequestMethod.GET)
	@ResponseBody
	public RespBody getNews(@RequestParam("lastId") Long lastId) {

		List<Chat> chats = chatRepository.queryRecent(lastId);

		return RespBody.succeed(chats);
	}

	@RequestMapping(value = "more", method = RequestMethod.GET)
	@ResponseBody
	public RespBody more(
			@PageableDefault(sort = { "createDate" }, direction = Direction.DESC) Pageable pageable) {

		Page<Chat> chats = chatRepository.findAll(pageable);
		chats = new PageImpl<Chat>(CollectionUtil.reverseList(chats
				.getContent()), pageable, chats.getTotalElements());

		return RespBody.succeed(chats);
	}

	@RequestMapping(value = "more/old", method = RequestMethod.GET)
	@ResponseBody
	public RespBody moreOld(
			@RequestParam("startId") Long startId,
			@RequestParam(value = "size", required = false, defaultValue = "10") int size) {

		List<Chat> chats = chatRepository.queryMoreOld(startId, size);
		long countAllOld = chatRepository.countAllOld(startId);

		Map<String, Object> map = new HashMap<>();
		map.put("content", CollectionUtil.reverseList(chats));
		map.put("hasMore", countAllOld > chats.size());
		map.put("countMore", countAllOld - chats.size());
		map.put("numberOfElements", chats.size());
		map.put("size", size);

		return RespBody.succeed(map);
	}

	@RequestMapping(value = "more/new", method = RequestMethod.GET)
	@ResponseBody
	public RespBody moreNew(
			@RequestParam("startId") Long startId,
			@RequestParam(value = "size", required = false, defaultValue = "10") int size) {

		List<Chat> chats = chatRepository.queryMoreNew(startId, size);
		long countAllNew = chatRepository.countAllNew(startId);

		Map<String, Object> map = new HashMap<>();
		map.put("content", chats);
		map.put("hasMore", countAllNew > chats.size());
		map.put("countMore", countAllNew - chats.size());
		map.put("numberOfElements", chats.size());
		map.put("size", size);

		return RespBody.succeed(map);
	}

	@RequestMapping(value = "moreLogs", method = RequestMethod.GET)
	@ResponseBody
	public RespBody moreLogs(
			@PageableDefault(sort = { "createDate" }, direction = Direction.DESC) Pageable pageable) {

		Page<Log> logs = logRepository.findByTarget(Target.Translate, pageable);

		return RespBody.succeed(logs);
	}

	@RequestMapping(value = { "search", "search/unmask" }, method = RequestMethod.GET)
	@ResponseBody
	public RespBody search(
			@PageableDefault(sort = { "createDate" }, direction = Direction.DESC) Pageable pageable,
			@RequestParam(value = "search", required = true) String search) {

		Page<Chat> chats = null;

		if (search.startsWith(SysConstant.FILTER_PRE)) {
			String query = search.substring(SysConstant.FILTER_PRE.length());
			String[] querys = query.split(":");

			final List<User> users = new ArrayList<>();
			String searchContent = null;

			if (querys.length > 0) {
				Stream.of(querys[0].split("&")).filter((name) -> {
					return StringUtil.isNotEmpty(name.trim());
				}).forEach((name) -> {
					User user = getUser(name);
					if (user != null) {
						users.add(user);
					}
				});
			}

			if (querys.length > 1) {
				searchContent = querys[1].trim();
			}

			if (CollectionUtil.isNotEmpty(users)) {
				if (StringUtil.isNotEmpty(searchContent)) {
					chats = chatRepository.findByCreatorInAndContentContainingIgnoreCase(
							users, searchContent, pageable);
				} else {
					chats = chatRepository.findByCreatorIn(users, pageable);
				}
			} else {
				return RespBody.failed("查询指定创建者不存在!");
			}

		} else {
			chats = chatRepository.findByContentLike("%" + search + "%",
					pageable);
		}

		return RespBody.succeed(chats);
	}

	@RequestMapping(value = { "searchBy", "searchBy/unmask" }, method = RequestMethod.GET)
	@ResponseBody
	public RespBody searchBy(
			@RequestParam(value = "id") Long id,
			@PageableDefault(sort = { "createDate" }, direction = Direction.DESC) Pageable pageable) {

		long cntGtId = chatRepository.countGtId(id);
		int size = pageable.getPageSize();
		long page = cntGtId / size;
		if (cntGtId % size == 0) {
			page--;
		}

		pageable = new PageRequest(page > -1 ? (int) page : 0, size,
				Direction.DESC, "createDate");

		Page<Chat> chats = chatRepository.findAll(pageable);
		chats = new PageImpl<Chat>(CollectionUtil.reverseList(chats
				.getContent()), pageable, chats.getTotalElements());

		return RespBody.succeed(chats);
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

	@RequestMapping(value = { "vote", "vote/unmask" }, method = RequestMethod.POST)
	@ResponseBody
	public RespBody vote(@RequestParam("id") Long id,
			@RequestParam("baseURL") String baseURL,
			@RequestParam("contentHtml") String contentHtml,
			@RequestParam(value = "type", required = false) String type) {

		Chat chat = chatRepository.findOne(id);
		if (chat == null) {
			return RespBody.failed("投票聊天内容不存在!");
		}
		String loginUsername = WebUtil.getUsername();

		Chat chat2 = null;

		String title = "";
		final User loginUser = getLoginUser();

		if (VoteType.Zan.name().equalsIgnoreCase(type)) {
			String voteZan = chat.getVoteZan();
			if (isVoterExists(voteZan)) {
				return RespBody.failed("您已经投票[赞]过！");
			} else {
				chat.setVoteZan(voteZan == null ? loginUsername : voteZan + ','
						+ loginUsername);
				
				Integer voteZanCnt = chat.getVoteZanCnt();
				if (voteZanCnt == null) {
					voteZanCnt = 0;
				}
				chat.setVoteZanCnt(++voteZanCnt);

				chat2 = chatRepository.saveAndFlush(chat);
				title = loginUser.getName() + "[" + loginUsername
						+ "]赞了你的沟通消息!";
			}

		} else {
			String voteCai = chat.getVoteCai();
			if (isVoterExists(voteCai)) {
				return RespBody.failed("您已经投票[踩]过！");
			} else {
				chat.setVoteCai(voteCai == null ? loginUsername : voteCai + ','
						+ loginUsername);
				
				Integer voteCaiCnt = chat.getVoteCaiCnt();
				if (voteCaiCnt == null) {
					voteCaiCnt = 0;
				}
				chat.setVoteCaiCnt(++voteCaiCnt);
				
				chat2 = chatRepository.saveAndFlush(chat);
				title = loginUser.getName() + "[" + loginUsername
						+ "]踩了你的沟通消息!";
			}
		}

		final String href = baseURL + dynamicAction + "?id=" + id;
		final String titleHtml = title;
		final Mail mail = Mail.instance().addUsers(chat.getCreator());
		final String html = "<h3>投票沟通消息内容:</h3><hr/>" + contentHtml;

		try {
			mailSender
					.sendHtmlByQueue(String.format("TMS-沟通动态投票@消息_%s", DateUtil.format(new Date(), DateUtil.FORMAT7)),
							TemplateUtil.process("templates/mail/mail-dynamic", MapUtil.objArr2Map("user", loginUser,
									"date", new Date(), "href", href, "title", titleHtml, "content", html)),
							getLoginUserName(loginUser), mail.get());
		} catch (Exception e) {
			e.printStackTrace();
		}

		log(Action.Vote, Target.Chat, chat.getId(), chat2);

		return RespBody.succeed(chat2);
	}

	@RequestMapping(value = { "getAtChats", "getAtChats/unmask" }, method = RequestMethod.GET)
	@ResponseBody
	public RespBody getAtChats(
			@PageableDefault(sort = { "createDate" }, direction = Direction.DESC) Pageable pageable) {

		Page<ChatAt> chatAts = chatAtRepository.findByChatNotNullAndAtUserAndStatus(
				getLoginUser(), Status.New, pageable);

		return RespBody.succeed(chatAts);
	}

	@RequestMapping(value = { "markAsReaded", "markAsReaded/unmask" }, method = RequestMethod.POST)
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
	
	@RequestMapping(value = { "markAsReadedByChat", "markAsReadedByChat/unmask" }, method = RequestMethod.POST)
	@ResponseBody
	public RespBody markAsReadedByChat(@RequestParam("chatId") Long chatId) {
		
		Chat chat = chatRepository.findOne(chatId);
		if (chat == null) {
			return RespBody.failed("@消息不存在,可能已经被删除!");
		}
		
		int cnt = chatAtRepository.markChatAsReaded(chat, getLoginUser());
		
		return RespBody.succeed(cnt);
	}

	@RequestMapping(value = { "markAllAsReaded",
			"markAllAsReaded/unmask" }, method = RequestMethod.POST)
	@ResponseBody
	public RespBody markAllAsReaded() {

		int cnt = chatAtRepository.markChatAllAsReaded(getLoginUser());

		return RespBody.succeed(cnt);
	}

	@RequestMapping(value = { "stow", "stow/unmask" }, method = RequestMethod.POST)
	@ResponseBody
	public RespBody stow(@RequestParam("id") Long id) {

		Chat chat = chatRepository.findOne(id);

		if (chat == null) {
			return RespBody.failed("收藏消息不存在,可能已经被删除!");
		}

		User loginUser = getLoginUser();
		ChatStow chatStow = chatStowRepository.findOneByChatAndStowUser(chat,
				loginUser);

		if (chatStow != null) {
			return RespBody.failed("收藏消息重复!");
		}

		ChatStow chatStow2 = new ChatStow();
		chatStow2.setChat(chat);
		chatStow2.setStowUser(loginUser);

		ChatStow chatStow3 = chatStowRepository.saveAndFlush(chatStow2);

		return RespBody.succeed(chatStow3);
	}

	@RequestMapping(value = { "removeStow", "removeStow/unmask" }, method = RequestMethod.POST)
	@ResponseBody
	public RespBody removeStow(@RequestParam("id") Long id) {

		chatStowRepository.delete(id);

		return RespBody.succeed(id);
	}

	@RequestMapping(value = { "getStows", "getStows/unmask" }, method = RequestMethod.GET)
	@ResponseBody
	public RespBody getStows() {

		List<ChatStow> chatStows = chatStowRepository.findByChatNotNullAndStowUserAndStatus(
				getLoginUser(), Status.New);

		return RespBody.succeed(chatStows);
	}

	@RequestMapping(value = "getReplies", method = RequestMethod.GET)
	@ResponseBody
	public RespBody getReplies(@RequestParam("id") Long id) {

		// [回复#871](http://translation.sh1.newtouch.com/admin/dynamic?id=871)

		String query = StringUtil.replaceByKV(
				"%[回复#{id}](%/admin/dynamic?id={id})%", "id", id);

		List<Chat> chats = chatRepository.queryReplies(id, query);

		return RespBody.succeed(chats);
	}

	@RequestMapping(value = "openEdit", method = RequestMethod.POST)
	@ResponseBody
	public RespBody openEdit(@RequestParam("id") Long id,
			@RequestParam("open") Boolean open) {

		Chat chat = chatRepository.findOne(id);

		if (chat == null) {
			return RespBody.failed("操作消息不存在,可能已经被删除!");
		}

		chat.setOpenEdit(open);
		chatRepository.saveAndFlush(chat);

		return RespBody.succeed();
	}
	
	@RequestMapping(value = { "asWiki", "asWiki/unmask" }, method = RequestMethod.POST)
	@ResponseBody
	public RespBody asWiki(@RequestParam("id") Long id,
			@RequestParam("baseURL") String baseURL,
			@RequestParam("title") String title,
			@RequestParam(value = "privated", required = false) Boolean privated,
			@RequestParam(value = "labels", required = false) String labels) {

		if (StringUtil.isEmpty(title)) {
			return RespBody.failed("标题不能为空!");
		}

		Chat chat = chatRepository.findOne(id);

		if (chat == null) {
			return RespBody.failed("聊天内容不存在!");
		}

		chat.setTitle(title);
		chat.setType(ChatType.Wiki);
		chat.setPrivated(privated == null ? false : privated);

		Chat chat2 = chatRepository.saveAndFlush(chat);

		if (StringUtil.isNotEmpty(labels)) {

			List<Label> labelList = new ArrayList<Label>();

			String[] labelArr = labels.split(SysConstant.COMMA);
			Stream.of(labelArr).forEach((lbl) -> {
				Label label = new Label();
				label.setCreateDate(new Date());
				label.setCreator(WebUtil.getUsername());
				label.setName(lbl);
				label.setStatus(Status.New);
				label.setChat(chat2);

				labelList.add(label);
			});

			labelRepository.save(labelList);
			labelRepository.flush();
		}

		logWithProperties(Action.Update, Target.Chat, chat2.getId(),
				Prop.Title.name(), title);

		return RespBody.succeed(id);
	}

	@RequestMapping(value = { "updateWiki", "updateWiki/unmask" }, method = RequestMethod.POST)
	@ResponseBody
	public RespBody updateWiki(@RequestParam("id") Long id,
			@RequestParam("baseURL") String baseURL,
			@RequestParam("title") String title,
			@RequestParam(value = "privated", required = false) Boolean privated,
			@RequestParam(value = "labels", required = false) String labels) {

		if (StringUtil.isEmpty(title)) {
			return RespBody.failed("标题不能为空!");
		}

		Chat chat = chatRepository.findOne(id);

		if (chat == null) {
			return RespBody.failed("聊天内容不存在!");
		}
		
		if (!isSuperOrCreator(chat.getCreator().getUsername())) {
			return RespBody.failed("您没有权限编辑该消息内容!");
		}

		String oldTitle = chat.getTitle();
		chat.setTitle(title);
		
		if (privated != null) {
			chat.setPrivated(privated);
		}

		Chat chat2 = chatRepository.saveAndFlush(chat);

		if (StringUtil.isNotEmpty(labels)) {

			List<Label> labelList2 = labelRepository.findByChat(chat2);
			labelRepository.delete(labelList2);
			labelRepository.flush();

			List<Label> labelList = new ArrayList<Label>();

			String[] labelArr = labels.split(SysConstant.COMMA);
			Stream.of(labelArr).forEach((lbl) -> {
				Label label = new Label();
				label.setCreateDate(new Date());
				label.setCreator(WebUtil.getUsername());
				label.setName(lbl);
				label.setStatus(Status.New);
				label.setChat(chat2);

				labelList.add(label);
			});

			labelRepository.save(labelList);
			labelRepository.flush();
		}

		logWithProperties(Action.Update, Target.Chat, chat2.getId(),
				Prop.Title.name(), title, oldTitle);

		return RespBody.succeed(id);
	}

	@RequestMapping(value = "deleteWiki", method = RequestMethod.POST)
	@ResponseBody
	public RespBody deleteWiki(@RequestParam("id") Long id) {

		Chat chat = chatRepository.findOne(id);

		if (chat == null) {
			return RespBody.failed("博文不存在!");
		}

		if (!isSuperOrCreator(chat.getCreator().getUsername())) {
			return RespBody.failed("您没有权限编辑该消息内容!");
		}

		chat.setType(ChatType.Msg);
		
		chatRepository.saveAndFlush(chat);

		return RespBody.succeed(id);
	}
	
	@RequestMapping(value = { "addLabel", "addLabel/unmask" }, method = RequestMethod.POST)
	@ResponseBody
	public RespBody addLabel(@RequestParam("id") Long id,
			@RequestParam("baseURL") String baseURL,
			@RequestParam("label") String label) {

		if (StringUtil.isEmpty(label)) {
			return RespBody.failed("标签不能为空!");
		}

		Chat chat = chatRepository.findOne(id);

		if (chat == null) {
			return RespBody.failed("聊天内容不存在!");
		}

		Label label2 = new Label();
		label2.setCreateDate(new Date());
		label2.setCreator(WebUtil.getUsername());
		label2.setName(label);
		label2.setStatus(Status.New);
		label2.setChat(chat);

		labelRepository.save(label2);
		labelRepository.flush();

		logWithProperties(Action.Update, Target.Chat, chat.getId(),
				Prop.Labels.name(), label);

		return RespBody.succeed(id);
	}

	@RequestMapping(value = { "deleteLabel", "deleteLabel/unmask" }, method = RequestMethod.POST)
	@ResponseBody
	public RespBody deleteLabel(@RequestParam("baseURL") String baseURL,
			@RequestParam("labelId") Long labelId) {
		
		Label label = labelRepository.findOne(labelId);
		
		if (!isSuperOrCreator(label.getCreator())) {
			return RespBody.failed("您没有权限删除该标签!");
		}

		labelRepository.delete(labelId);

		log(Action.Delete, Target.Label, labelId);

		return RespBody.succeed(labelId);
	}
}
