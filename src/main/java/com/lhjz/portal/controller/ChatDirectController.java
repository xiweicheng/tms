/**
 * 版权所有 (TMS)
 */
package com.lhjz.portal.controller;

import java.util.Date;
import java.util.List;

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
import com.lhjz.portal.constant.SysConstant;
import com.lhjz.portal.entity.ChatDirect;
import com.lhjz.portal.entity.security.User;
import com.lhjz.portal.model.RespBody;
import com.lhjz.portal.repository.ChatAtRepository;
import com.lhjz.portal.repository.ChatDirectRepository;
import com.lhjz.portal.repository.ChatStowRepository;
import com.lhjz.portal.repository.GroupMemberRepository;
import com.lhjz.portal.repository.GroupRepository;
import com.lhjz.portal.repository.LogRepository;
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
@RequestMapping("admin/chat/direct")
public class ChatDirectController extends BaseController {

	static Logger logger = LoggerFactory.getLogger(ChatDirectController.class);

	@Autowired
	ChatDirectRepository chatDirectRepository;

	@Autowired
	UserRepository userRepository;

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
	MailSender2 mailSender;

	@RequestMapping(value = "create", method = RequestMethod.POST)
	@ResponseBody
	public RespBody create(@RequestParam("baseUrl") String baseUrl,
			@RequestParam("path") String path,
			@RequestParam("chatTo") String chatTo,
			@RequestParam("content") String content,
			@RequestParam("contentHtml") final String contentHtml) {

		if (StringUtil.isEmpty(content)) {
			return RespBody.failed("提交内容不能为空!");
		}

		final User chatToUser = userRepository.findOne(chatTo);

		if (chatToUser == null) {
			return RespBody.failed("聊天对象不存在!");
		}

		ChatDirect chatDirect = new ChatDirect();
		chatDirect.setChatTo(chatToUser);
		chatDirect.setContent(content);

		ChatDirect chatDirect2 = chatDirectRepository.saveAndFlush(chatDirect);

		final User loginUser = getLoginUser();
		final String href = baseUrl + path + "#/chat/@"
				+ loginUser.getUsername() + "?id="
				+ chatDirect2.getId();

		ThreadUtil.exec(() -> {

			try {
				Thread.sleep(3000);
				mailSender.sendHtml(
						String.format("TMS-私聊@消息_%s",
								DateUtil.format(new Date(), DateUtil.FORMAT7)),
						TemplateUtil.process("templates/mail/mail-dynamic",
								MapUtil.objArr2Map("user", loginUser, "date",
										new Date(), "href", href, "title",
										"发给你的私聊消息", "content", contentHtml)),
						chatToUser.getMails());
				logger.info("私聊邮件发送成功！");
			} catch (Exception e) {
				e.printStackTrace();
				logger.error("私聊邮件发送失败！");
			}

		});

		return RespBody.succeed();
	}

	@RequestMapping(value = "update", method = RequestMethod.POST)
	@ResponseBody
	public RespBody update(@RequestParam("baseUrl") String baseUrl,
			@RequestParam("id") Long id, @RequestParam("path") String path,
			@RequestParam("content") String content,
			@RequestParam(value = "diff", required = false) String diff,
			@RequestParam(value = "contentHtml", required = false) String contentHtml,
			@RequestParam(value = "contentHtmlOld", required = false) String contentHtmlOld) {

		if (StringUtil.isEmpty(content)) {
			return RespBody.failed("更新内容不能为空!");
		}

		final ChatDirect chatDirect = chatDirectRepository.findOne(id);
		
		if (chatDirect == null) {
			return RespBody.failed("更新内容不存在!");
		}
		
		final User loginUser = getLoginUser();

		if (!isSuperOrCreator(chatDirect.getCreator().getUsername())) {
			return RespBody.failed("您没有权限编辑该消息内容!");
		}

		if (chatDirect.getContent().equals(content)) {
			return RespBody.failed("更新内容没有任何变更!");
		}

		chatDirect.setContent(content);

		chatDirectRepository.saveAndFlush(chatDirect);

		final String href = baseUrl + path + "#/chat/@"
				+ loginUser.getUsername() + "?id="
				+ chatDirect.getId();
		final String html;
		if(StringUtil.isNotEmpty(diff)) {
			html = "<h3>内容(Markdown)变更对比:</h3><b>原文链接:</b> <a href=\"" + href + "\">" + href + "</a><hr/>" + diff;
		} else {
			html = "<h3>编辑后内容:</h3>" + contentHtml + "<hr/><h3>编辑前内容:</h3>" + contentHtmlOld;
		}

		ThreadUtil.exec(() -> {

			try {
				Thread.sleep(3000);
				mailSender.sendHtml(
						String.format("TMS-私聊@消息更新_%s",
								DateUtil.format(new Date(), DateUtil.FORMAT7)),
						TemplateUtil.process("templates/mail/mail-dynamic",
								MapUtil.objArr2Map("user", loginUser, "date",
										new Date(), "href", href, "title",
										"发给你的私聊消息更新", "content", html)),
						chatDirect.getChatTo().getMails());
				logger.info("私聊消息更新邮件发送成功！");
			} catch (Exception e) {
				e.printStackTrace();
				logger.error("私聊消息更新邮件发送失败！");
			}

		});

		return RespBody.succeed();
	}

	@RequestMapping(value = "delete", method = RequestMethod.POST)
	@ResponseBody
	public RespBody delete(@RequestParam("id") Long id) {

		final ChatDirect chatDirect = chatDirectRepository.findOne(id);

		if (chatDirect == null) {
			return RespBody.failed("删除内容不存在!");
		}

		boolean isSuper = WebUtil.getUserAuthorities()
				.contains(SysConstant.ROLE_SUPER);
		boolean isCreator = chatDirect.getCreator().getUsername()
				.equals(WebUtil.getUsername());

		if (!isSuper && !isCreator) {
			return RespBody.failed("您没有权限删除该消息内容!");
		}

		chatDirectRepository.delete(chatDirect);

		return RespBody.succeed();
	}
	
	@RequestMapping(value = "get", method = RequestMethod.GET)
	@ResponseBody
	public RespBody get(@RequestParam("id") Long id) {
		
		ChatDirect chatDirect = chatDirectRepository.findOne(id);
		
		return RespBody.succeed(chatDirect);
	}

	@RequestMapping(value = "list", method = RequestMethod.GET)
	@ResponseBody
	public RespBody list(@RequestParam(value = "id", required = false) Long id,
			@PageableDefault(sort = {
					"createDate" }, direction = Direction.DESC) Pageable pageable,
			@RequestParam("chatTo") String chatTo) {

		User chatToUser = userRepository.findOne(chatTo);

		if (chatToUser == null) {
			return RespBody.failed("聊天对象不存在!");
		}

		long start = 0;
		int limit = pageable.getPageSize();

		User loginUser = getLoginUser();

		if (StringUtil.isNotEmpty(id)) {
			long cntGtId = chatDirectRepository.countGtId(loginUser, chatToUser,
					id);
			int size = limit;
			long page = cntGtId / size;
			if (cntGtId % size == 0) {
				page--;
			}

			pageable = new PageRequest(page > -1 ? (int) page : 0, size,
					Direction.DESC, "createDate");
			start = pageable.getOffset();
		}

		long total = chatDirectRepository.countChatDirect(loginUser,
				chatToUser);

		List<ChatDirect> chats = chatDirectRepository.queryChatDirect(loginUser,
				chatToUser, start, limit);

		Page<ChatDirect> page = new PageImpl<ChatDirect>(chats, pageable,
				total);

		return RespBody.succeed(page);
	}

	@RequestMapping(value = "latest", method = RequestMethod.GET)
	@ResponseBody
	public RespBody latest(@RequestParam("id") Long id,
			@RequestParam("chatTo") String chatTo) {

		User chatToUser = userRepository.findOne(chatTo);

		if (chatToUser == null) {
			return RespBody.failed("聊天对象不存在!");
		}

		List<ChatDirect> chats = chatDirectRepository
				.queryChatDirectAndIdGreaterThan(getLoginUser(), chatToUser,
						id);

		return RespBody.succeed(chats);
	}

	@RequestMapping(value = "more", method = RequestMethod.GET)
	@ResponseBody
	public RespBody more(@RequestParam("start") Long start,
			@RequestParam("last") Boolean last,
			@RequestParam("size") Integer size,
			@RequestParam("chatTo") String chatTo) {

		User chatToUser = userRepository.findOne(chatTo);

		if (chatToUser == null) {
			return RespBody.failed("聊天对象不存在!");
		}

		long count = 0;
		List<ChatDirect> chats = null;
		if (last) {
			count = chatDirectRepository.countAllOld(getLoginUser(), chatToUser,
					start);
			chats = chatDirectRepository.queryMoreOld(getLoginUser(),
					chatToUser, start, size);
		} else {
			count = chatDirectRepository.countAllNew(getLoginUser(), chatToUser,
					start);
			chats = chatDirectRepository.queryMoreNew(getLoginUser(),
					chatToUser, start, size);
		}

		return RespBody.succeed(chats).addMsg(count);
	}

	@RequestMapping(value = "search", method = RequestMethod.GET)
	@ResponseBody
	public RespBody search(@RequestParam("search") String search,
			@PageableDefault(sort = {
					"createDate" }, direction = Direction.DESC) Pageable pageable) {

		if (StringUtil.isEmpty(search)) {
			return RespBody.failed("检索条件不能为空!");
		}

		User loginUser = getLoginUser();
		String _search = "%" + search + "%";
		List<ChatDirect> chats = chatDirectRepository.queryAboutMe(loginUser,
				_search, pageable.getOffset(), pageable.getPageSize());
		long cnt = chatDirectRepository.countAboutMe(loginUser, _search);

		Page<ChatDirect> page = new PageImpl<>(chats, pageable, cnt);

		return RespBody.succeed(page);
	}

}
