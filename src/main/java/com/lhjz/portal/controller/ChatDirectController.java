/**
 * 版权所有 (TMS)
 */
package com.lhjz.portal.controller;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Date;
import java.util.List;
import java.util.stream.Stream;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.lhjz.portal.base.BaseController;
import com.lhjz.portal.component.MailSender2;
import com.lhjz.portal.constant.SysConstant;
import com.lhjz.portal.entity.Channel;
import com.lhjz.portal.entity.ChatChannel;
import com.lhjz.portal.entity.ChatDirect;
import com.lhjz.portal.entity.security.User;
import com.lhjz.portal.model.Mail;
import com.lhjz.portal.model.RespBody;
import com.lhjz.portal.repository.ChannelRepository;
import com.lhjz.portal.repository.ChatAtRepository;
import com.lhjz.portal.repository.ChatChannelRepository;
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
import com.lhjz.portal.util.ValidateUtil;
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
	
	@Value("${tms.chat.direct.upload.path}")
	private String uploadPath;
	
	@Value("${tms.blog.md2pdf.path}")
	private String md2pdfPath;

	@Autowired
	ChannelRepository channelRepository;
	
	@Autowired
	ChatDirectRepository chatDirectRepository;
	
	@Autowired
	ChatChannelRepository chatChannelRepository;

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
	
	boolean isCreatorOrChatter(ChatDirect chatDirect) {
		User creator = chatDirect.getCreator();
		User chatTo = chatDirect.getChatTo();
		User loginUser = getLoginUser();

		return (loginUser.equals(creator) || loginUser.equals(chatTo));
	}
	
	@RequestMapping(value = "get", method = RequestMethod.GET)
	@ResponseBody
	public RespBody get(@RequestParam("id") Long id) {
		
		ChatDirect chatDirect = chatDirectRepository.findOne(id);
		
		if(!isCreatorOrChatter(chatDirect)) {
			return RespBody.failed("没有权限查看该私聊消息!");
		}
		
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
	

	@RequestMapping(value = "download/{id}", method = RequestMethod.GET)
	public void download(HttpServletRequest request,
			HttpServletResponse response, @PathVariable Long id, @RequestParam(value = "type", defaultValue = "pdf") String type)
			throws Exception {

		logger.debug("download direct chat start...");
		
		ChatDirect chatDirect = chatDirectRepository.findOne(id);

		if (chatDirect == null) {
			try {
				response.sendError(404, "下载私聊消息不存在!");
				return;
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		if(!isCreatorOrChatter(chatDirect)) {
			try {
				response.sendError(401, "没有权限下载该私聊消息!");
				return;
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		// 获取网站部署路径(通过ServletContext对象)，用于确定下载文件位置，从而实现下载
		String path = WebUtil.getRealPath(request);
		
		String blogUpdateDate = DateUtil.format(chatDirect.getUpdateDate(), DateUtil.FORMAT9);
		
		String mdFileName = chatDirect.getId() + "_" + blogUpdateDate + ".md";
		String pdfFileName = chatDirect.getId() + "_" + blogUpdateDate + ".pdf";
		
		String mdFilePath = path + uploadPath + mdFileName;
		String pdfFilePath = path + uploadPath + pdfFileName;
		
		File fileMd = new File(mdFilePath);

		if (!fileMd.exists()) {
			try {
				FileUtils.writeStringToFile(fileMd, chatDirect.getContent(), "UTF-8");
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		File filePdf = new File(pdfFilePath);
		
		if (!filePdf.exists()) {
			try {
				String pathNode = StringUtil.isNotEmpty(md2pdfPath) ? md2pdfPath : new File(Class.class.getClass().getResource("/md2pdf").getPath()).getAbsolutePath();
				
				String nodeCmd = StringUtil.replace("node {?1} {?2} {?3}", pathNode, mdFilePath, pdfFilePath);
				logger.debug("Node CMD: " + nodeCmd);
				Process process = Runtime.getRuntime().exec(nodeCmd);
				BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(process.getInputStream()));
				String s = null;
				while ((s = bufferedReader.readLine()) != null) {
					logger.debug(s);
				}
				process.waitFor();
				logger.debug("Md2pdf done!");
			} catch (IOException | InterruptedException e) {
				e.printStackTrace();
			}
		}
		
		// 1.设置文件ContentType类型，这样设置，会自动判断下载文件类型
		// response.setContentType("multipart/form-data");
		response.setContentType("application/x-msdownload;");
		response.addHeader("Content-Type", "text/html; charset=utf-8");
		String dnFileName = null;
		String dnFileLength = null;
		File dnFile = null;
		String name = chatDirect.getChatTo().getName();
		if (StringUtil.isEmpty(name)) {
			name = chatDirect.getChatTo().getUsername();
		}
		if ("md".equalsIgnoreCase(type)) {
			dnFileName = StringUtil.replace("{?1}_{?2}", name, mdFileName);
			dnFileLength = String.valueOf(fileMd.length());
			dnFile = fileMd;
		} else {
			dnFileName = StringUtil.replace("{?1}_{?2}", name, pdfFileName);
			dnFileLength = String.valueOf(filePdf.length());
			dnFile = filePdf;
		}
		// 2.设置文件头：最后一个参数是设置下载文件名
		response.setHeader("Content-Disposition", "attachment; fileName="
				+ StringUtil.encodingFileName(dnFileName));
		response.setHeader("Content-Length", dnFileLength);

		java.io.BufferedInputStream bis = null;
		java.io.BufferedOutputStream bos = null;

		try {
			bis = new BufferedInputStream(new FileInputStream(dnFile));
			bos = new BufferedOutputStream(response.getOutputStream());
			byte[] buff = new byte[2048];
			int bytesRead;
			while (-1 != (bytesRead = bis.read(buff, 0, buff.length))) {
				bos.write(buff, 0, bytesRead);
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (bis != null) {
				bis.close();
			}
			if (bos != null) {
				bos.close();
			}
		}
	}
	
	private boolean hasAuth(ChatDirect cd) {

		if (isSuperOrCreator(cd.getCreator().getUsername())) {
			return true;
		}

		User loginUser = new User(WebUtil.getUsername());

		return cd.getChatTo().equals(loginUser);
	}

	@RequestMapping(value = "share", method = RequestMethod.POST)
	@ResponseBody
	public RespBody share(@RequestParam("basePath") String basePath, @RequestParam("id") Long id,
			@RequestParam("href") final String href, @RequestParam("html") String html,
			@RequestParam(value = "desc", required = false) String desc,
			@RequestParam(value = "users", required = false) String users,
			@RequestParam(value = "channels", required = false) String channels,
			@RequestParam(value = "mails", required = false) String mails) {

		ChatDirect chatDirect2 = chatDirectRepository.findOne(id);

		if (!hasAuth(chatDirect2)) {
			return RespBody.failed("您没有权限分享该沟通消息!");
		}

		final User loginUser = getLoginUser();

		final String html2 = StringUtil.replace(
				"<h1 style=\"color: blue;\">分享沟通消息: <a target=\"_blank\" href=\"{?1}\">{?2}</a></h1><hr/>{?3}", href,
				"沟通消息链接", html);

		final String title = StringUtil.isNotEmpty(desc) ? desc : "下面的沟通消息有分享到你";

		Mail mail = Mail.instance();
		if (StringUtil.isNotEmpty(users)) {
			Stream.of(users.split(",")).forEach(username -> {
				User user = getUser(username);
				if (user != null) {
					mail.addUsers(user);

					ChatDirect chatDirect = new ChatDirect();
					chatDirect.setChatTo(user);
					chatDirect.setContent(
							StringUtil.replace("## ~私聊消息播报~\n> 来自 {~{?1}} 的沟通消息分享:  [{?2}]({?3})\n\n---\n\n{?4}",
									loginUser.getUsername(), "沟通消息链接", href, chatDirect2.getContent()));

					chatDirectRepository.saveAndFlush(chatDirect);
				}
			});
		}
		if (StringUtil.isNotEmpty(channels)) {
			Stream.of(channels.split(",")).forEach(name -> {
				Channel channel = channelRepository.findOneByName(name);
				if (channel != null) {
					channel.getMembers().forEach(user -> {
						mail.addUsers(user);
					});

					ChatChannel chatChannel = new ChatChannel();
					chatChannel.setChannel(channel);
					chatChannel.setContent(
							StringUtil.replace("## ~频道消息播报~\n> 来自 {~{?1}} 的沟通消息分享:  [{?2}]({?3})\n\n---\n\n{?4}",
									loginUser.getUsername(), "沟通消息链接", href, chatDirect2.getContent()));

					chatChannelRepository.saveAndFlush(chatChannel);
				}
			});
		}

		if (StringUtil.isNotEmpty(mails)) {
			Stream.of(mails.split(",")).forEach(m -> {
				if (ValidateUtil.isEmail(m)) {
					mail.add(m);
				}
			});
		}

		ThreadUtil.exec(() -> {

			try {
				Thread.sleep(3000);
				mailSender
						.sendHtml(String.format("TMS-沟通消息分享_%s", DateUtil.format(new Date(), DateUtil.FORMAT7)),
								TemplateUtil.process("templates/mail/mail-dynamic", MapUtil.objArr2Map("user",
										loginUser, "date", new Date(), "href", href, "title", title, "content", html2)),
								mail.get());
				logger.info("沟通消息分享邮件发送成功！");
			} catch (Exception e) {
				e.printStackTrace();
				logger.error("沟通消息分享邮件发送失败！");
			}

		});
		return RespBody.succeed();
	}

}
