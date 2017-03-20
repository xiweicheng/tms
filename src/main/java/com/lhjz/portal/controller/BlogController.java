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
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
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
import com.lhjz.portal.entity.Blog;
import com.lhjz.portal.entity.BlogHistory;
import com.lhjz.portal.entity.Channel;
import com.lhjz.portal.entity.Comment;
import com.lhjz.portal.entity.Space;
import com.lhjz.portal.entity.security.User;
import com.lhjz.portal.model.Mail;
import com.lhjz.portal.model.RespBody;
import com.lhjz.portal.pojo.Enum.Action;
import com.lhjz.portal.pojo.Enum.CommentType;
import com.lhjz.portal.pojo.Enum.Status;
import com.lhjz.portal.pojo.Enum.Target;
import com.lhjz.portal.pojo.Enum.VoteType;
import com.lhjz.portal.repository.BlogHistoryRepository;
import com.lhjz.portal.repository.BlogRepository;
import com.lhjz.portal.repository.ChannelRepository;
import com.lhjz.portal.repository.CommentRepository;
import com.lhjz.portal.repository.SpaceRepository;
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
@RequestMapping("admin/blog")
public class BlogController extends BaseController {

	static Logger logger = LoggerFactory.getLogger(BlogController.class);
	
	@Value("${tms.blog.upload.path}")
	private String uploadPath;
	
	@Value("${tms.blog.md2pdf.path}")
	private String md2pdfPath;

	@Autowired
	BlogRepository blogRepository;

	@Autowired
	BlogHistoryRepository blogHistoryRepository;

	@Autowired
	SpaceRepository spaceRepository;

	@Autowired
	ChannelRepository channelRepository;

	@Autowired
	UserRepository userRepository;

	@Autowired
	CommentRepository commentRepository;

	@Autowired
	MailSender2 mailSender;

	@RequestMapping(value = "create", method = RequestMethod.POST)
	@ResponseBody
	public RespBody create(@RequestParam("url") String url,
			@RequestParam(value = "spaceId", required = false) Long spaceId,
			@RequestParam(value = "privated", required = false) Boolean privated,
			@RequestParam(value = "usernames", required = false) String usernames, @RequestParam("title") String title,
			@RequestParam("content") String content, @RequestParam("contentHtml") String contentHtml) {

		if (StringUtil.isEmpty(title)) {
			return RespBody.failed("标题不能为空!");
		}

		if (StringUtil.isEmpty(content)) {
			return RespBody.failed("内容不能为空!");
		}

		Blog blog = new Blog();
		blog.setTitle(title);
		blog.setContent(content);

		if (spaceId != null) {
			Space space = spaceRepository.findOne(spaceId);
			if (space == null) {
				return RespBody.failed("指定空间不存在!");
			}
			blog.setSpace(space);
		}

		if (privated != null) {
			blog.setPrivated(privated);
		}

		Blog blog2 = blogRepository.saveAndFlush(blog);

		final String href = url + "/#/blog/" + blog2.getId();
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

			ThreadUtil.exec(() -> {

				try {
					Thread.sleep(3000);
					mailSender.sendHtml(String.format("TMS-博文频道@消息_%s", DateUtil.format(new Date(), DateUtil.FORMAT7)),
							TemplateUtil.process("templates/mail/mail-dynamic", MapUtil.objArr2Map("user", loginUser,
									"date", new Date(), "href", href, "title", "下面的博文消息中有@到你", "content", html)),
							mail.get());
					logger.info("博文邮件发送成功！");
				} catch (Exception e) {
					e.printStackTrace();
					logger.error("博文邮件发送失败！");
				}

			});
		}

		return RespBody.succeed(blog2);
	}

	@RequestMapping(value = "list", method = RequestMethod.GET)
	@ResponseBody
	public RespBody list(@PageableDefault(sort = { "id" }, direction = Direction.DESC) Pageable pageable) {

		// TODO 过滤不可见博文
		// Page<Blog> page = blogRepository.findByStatusNot(Status.Deleted,
		// pageable);
		Page<Blog> page = blogRepository.findByStatusNotAndCreatorOrStatusNotAndPrivatedFalse(Status.Deleted,
				getLoginUser(), Status.Deleted, pageable);
		page.forEach(b -> b.setContent(null));

		return RespBody.succeed(page);
	}

	@RequestMapping(value = "update", method = RequestMethod.POST)
	@ResponseBody
	public RespBody update(@RequestParam("url") String url,
			@RequestParam(value = "usernames", required = false) String usernames, @RequestParam("id") Long id,
			@RequestParam("version") Long version, @RequestParam("title") String title,
			@RequestParam("content") String content, @RequestParam(value = "diff", required = false) String diff,
			@RequestParam(value = "contentHtml", required = false) String contentHtml,
			@RequestParam(value = "contentHtmlOld", required = false) String contentHtmlOld) {

		if (StringUtil.isEmpty(title)) {
			return RespBody.failed("更新标题不能为空!");
		}

		if (StringUtil.isEmpty(content)) {
			return RespBody.failed("更新内容不能为空!");
		}

		Blog blog = blogRepository.findOne(id);

		Boolean isOpenEdit = blog.getOpenEdit() == null ? false : blog.getOpenEdit();

		if (!isSuperOrCreator(blog.getCreator().getUsername()) && !isOpenEdit) {
			return RespBody.failed("您没有权限编辑该博文!");
		}

		if (blog.getVersion() != version.longValue()) {
			return RespBody.failed("该博文已经被其他人更新,请刷新博文重新编辑提交!");
		}

		boolean isUpdated = false;

		if (!content.equals(blog.getContent())) {
			logWithProperties(Action.Update, Target.ChatChannel, blog.getId(), "title", blog.getTitle());
			isUpdated = true;
		}

		if (!title.equals(blog.getTitle())) {
			logWithProperties(Action.Update, Target.ChatChannel, blog.getId(), "content", blog.getContent());
			isUpdated = true;
		}

		if (isUpdated) {

			BlogHistory blogHistory = new BlogHistory();
			blogHistory.setBlog(blog);
			blogHistory.setTitle(blog.getTitle());
			blogHistory.setContent(blog.getContent());
			blogHistory.setBlogUpdater(blog.getUpdater());
			blogHistory.setBlogUpdateDate(blog.getUpdateDate());

			blogHistoryRepository.saveAndFlush(blogHistory);

			blog.setTitle(title);
			blog.setContent(content);

			Blog blog2 = blogRepository.saveAndFlush(blog);

			final User loginUser = getLoginUser();
			final String href = url + "/#/blog/" + blog2.getId();
			final String html;
			if (StringUtil.isNotEmpty(diff)) {
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

				ThreadUtil.exec(() -> {

					try {
						Thread.sleep(3000);
						mailSender.sendHtml(
								String.format("TMS-b博文编辑@消息_%s", DateUtil.format(new Date(), DateUtil.FORMAT7)),
								TemplateUtil.process("templates/mail/mail-dynamic",
										MapUtil.objArr2Map("user", loginUser, "date", new Date(), "href", href, "title",
												"下面编辑的博文消息中有@到你", "content", html)),
								mail.get());
						logger.info("博文编辑邮件发送成功！");
					} catch (Exception e) {
						e.printStackTrace();
						logger.error("博文编辑邮件发送失败！");
					}

				});
			}

			return RespBody.succeed(blog2);
		} else {
			return RespBody.failed("修改博文无变更!");
		}

	}

	@RequestMapping(value = "delete", method = RequestMethod.POST)
	@ResponseBody
	public RespBody delete(@RequestParam("id") Long id) {

		Blog blog = blogRepository.findOne(id);

		if (!isSuperOrCreator(blog.getCreator().getUsername())) {
			return RespBody.failed("您没有权限删除该博文!");
		}

		blog.setStatus(Status.Deleted);

		blogRepository.saveAndFlush(blog);

		log(Action.Delete, Target.Blog, id);

		return RespBody.succeed(id);
	}

	@RequestMapping(value = "get", method = RequestMethod.GET)
	@ResponseBody
	public RespBody get(@RequestParam("id") Long id) {

		Blog blog = blogRepository.findOne(id);

		if (blog == null || Status.Deleted.equals(blog.getStatus())) {
			return RespBody.failed("博文消息不存在或者已经被删除!");
		}

		if (!isSuperOrCreator(blog.getCreator().getUsername())) {
			// TODO 权限可见用户判断
			if (blog.getPrivated()) {
				return RespBody.failed("您没有权限查看该博文!");
			}
		}

		return RespBody.succeed(blog);
	}

	@RequestMapping(value = "search", method = RequestMethod.GET)
	@ResponseBody
	public RespBody search(@RequestParam("search") String search,
			@PageableDefault(sort = { "id" }, direction = Direction.DESC) Pageable pageable) {

		if (StringUtil.isEmpty(search)) {
			return RespBody.failed("检索条件不能为空!");
		}

//		Page<Blog> page = blogRepository.findByTitleContainingOrContentContaining(search, search, pageable);
		Page<Blog> page = blogRepository.findByTitleContainingOrContentContainingAndStatusNotAndCreatorOrStatusNotAndPrivatedFalse(search, search, Status.Deleted, getLoginUser(), Status.Deleted, pageable);

		return RespBody.succeed(page);
	}

	@RequestMapping(value = "openEdit", method = RequestMethod.POST)
	@ResponseBody
	public RespBody openEdit(@RequestParam("id") Long id, @RequestParam("open") Boolean open) {

		Blog blog = blogRepository.findOne(id);

		if (blog == null) {
			return RespBody.failed("博文消息不存在,可能已经被删除!");
		}

		blog.setOpenEdit(open);
		blogRepository.saveAndFlush(blog);

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

	private String calcVoters(String voters) {

		List<String> list = Stream.of(voters.split(",")).filter(v -> !v.equals(WebUtil.getUsername()))
				.collect(Collectors.toList());

		return StringUtil.join(",", list);
	}

	@RequestMapping(value = "vote", method = RequestMethod.POST)
	@ResponseBody
	public RespBody vote(@RequestParam("id") Long id, @RequestParam("url") String url,
			@RequestParam("contentHtml") String contentHtml,
			@RequestParam(value = "type", required = false) String type) {

		Blog blog = blogRepository.findOne(id);
		if (blog == null) {
			return RespBody.failed("投票博文消息不存在!");
		}
		String loginUsername = WebUtil.getUsername();

		Blog blog2 = null;

		String title = "";
		final User loginUser = getLoginUser();

		if (VoteType.Zan.name().equalsIgnoreCase(type)) {
			String voteZan = blog.getVoteZan();
			if (isVoterExists(voteZan)) {
				return RespBody.failed("您已经投票[赞]过！");
			} else {
				blog.setVoteZan(voteZan == null ? loginUsername : voteZan + ',' + loginUsername);

				Integer voteZanCnt = blog.getVoteZanCnt();
				if (voteZanCnt == null) {
					voteZanCnt = 0;
				}
				blog.setVoteZanCnt(++voteZanCnt);

				blog2 = blogRepository.saveAndFlush(blog);
				title = loginUser.getName() + "[" + loginUsername + "]赞了你的博文消息!";
			}

		} else {
			String voteZan = blog.getVoteZan();
			if (isVoterExists(voteZan)) {
				blog.setVoteZan(this.calcVoters(voteZan));
				Integer voteZanCnt = blog.getVoteZanCnt();
				blog.setVoteZanCnt(voteZanCnt == null ? 0 : voteZanCnt - 1);

				blog2 = blogRepository.saveAndFlush(blog);
				return RespBody.succeed(blog2);
			}
		}

		final String href = url + "/#/blog/" + id;
		final String titleHtml = title;
		final Mail mail = Mail.instance().addUsers(blog.getCreator());
		final String html = "<h3>投票博文消息内容:</h3><hr/>" + contentHtml;

		ThreadUtil.exec(() -> {

			try {
				Thread.sleep(3000);
				mailSender.sendHtml(String.format("TMS-博文消息投票@消息_%s", DateUtil.format(new Date(), DateUtil.FORMAT7)),
						TemplateUtil.process("templates/mail/mail-dynamic", MapUtil.objArr2Map("user", loginUser,
								"date", new Date(), "href", href, "title", titleHtml, "content", html)),
						mail.get());
				logger.info("博文消息投票邮件发送成功！");
			} catch (Exception e) {
				e.printStackTrace();
				logger.error("博文消息投票邮件发送失败！");
			}

		});

		log(Action.Vote, Target.Blog, blog.getId(), blog2);

		return RespBody.succeed(blog2);
	}

	@RequestMapping(value = "share/to/search", method = RequestMethod.GET)
	@ResponseBody
	public RespBody searchShareTo(@RequestParam("search") String search) {

		if (StringUtil.isEmpty(search)) {
			return RespBody.failed("检索条件不能为空!");
		}

		Map<String, Object> map = new HashMap<>();

		List<User> users = userRepository.findTop6ByUsernameContainingAndEnabledTrue(search);
		List<Channel> channels = channelRepository.findTop6ByNameContainingAndStatusNot(search, Status.Deleted);

		map.put("users", users);
		map.put("channels", channels);

		return RespBody.succeed(map);
	}

	@RequestMapping(value = "share", method = RequestMethod.POST)
	@ResponseBody
	public RespBody share(@RequestParam("basePath") String basePath, @RequestParam("id") Long id,
			@RequestParam("html") String html, @RequestParam(value = "desc", required = false) String desc,
			@RequestParam(value = "users", required = false) String users,
			@RequestParam(value = "channels", required = false) String channels) {

		final User loginUser = getLoginUser();

		final String href = basePath + "#/blog/" + id;
		final String html2 = html;
		final String title = StringUtil.isNotEmpty(desc) ? desc : "下面的博文有分享到你";

		Mail mail = Mail.instance();
		if (StringUtil.isNotEmpty(users)) {
			Stream.of(users.split(",")).forEach(username -> {
				User user = getUser(username);
				mail.addUsers(user);
			});
		}
		if (StringUtil.isNotEmpty(channels)) {
			Stream.of(channels.split(",")).forEach(name -> {
				Channel channel = channelRepository.findOneByName(name);
				if (channel != null) {
					channel.getMembers().forEach(user -> {
						mail.addUsers(user);
					});
				}
			});
		}

		ThreadUtil.exec(() -> {

			try {
				Thread.sleep(3000);
				mailSender
						.sendHtml(String.format("TMS-博文分享_%s", DateUtil.format(new Date(), DateUtil.FORMAT7)),
								TemplateUtil.process("templates/mail/mail-dynamic", MapUtil.objArr2Map("user",
										loginUser, "date", new Date(), "href", href, "title", title, "content", html2)),
								mail.get());
				logger.info("博文分享邮件发送成功！");
			} catch (Exception e) {
				e.printStackTrace();
				logger.error("博文分享邮件发送失败！");
			}

		});
		return RespBody.succeed();
	}

	@RequestMapping(value = "comment/create", method = RequestMethod.POST)
	@ResponseBody
	public RespBody createComment(@RequestParam("basePath") String basePath, @RequestParam("id") Long id,
			@RequestParam("content") String content, @RequestParam("contentHtml") final String contentHtml,
			@RequestParam(value = "users", required = false) String users) {

		// TODO 博文权限判断

		Comment comment = new Comment();
		comment.setContent(content);
		comment.setTargetId(String.valueOf(id));
		comment.setType(CommentType.Blog);

		Comment comment2 = commentRepository.saveAndFlush(comment);

		final User loginUser = getLoginUser();

		final String href = basePath + "#/blog/" + id + "?cid=" + comment2.getId();

		Blog blog = blogRepository.findOne(id);

		Mail mail = Mail.instance().addUsers(blog.getCreator());
		if (StringUtil.isNotEmpty(users)) {
			Stream.of(users.split(",")).forEach(username -> {
				User user = getUser(username);
				mail.addUsers(user);
			});
		}
		
		final String html = StringUtil.replace("<h1 style=\"color: blue;\">评论博文: <a target=\"_blank\" href=\"{?1}\">{?2}</a></h1><hr/>{?3}", href, blog.getTitle(), contentHtml);

		ThreadUtil.exec(() -> {

			try {
				Thread.sleep(3000);
				mailSender.sendHtml(String.format("TMS-博文评论_%s", DateUtil.format(new Date(), DateUtil.FORMAT7)),
						TemplateUtil.process("templates/mail/mail-dynamic", MapUtil.objArr2Map("user", loginUser,
								"date", new Date(), "href", href, "title", "下面博文评论涉及到你", "content", html)),
						mail.get());
				logger.info("博文评论邮件发送成功！");
			} catch (Exception e) {
				e.printStackTrace();
				logger.error("博文评论邮件发送失败！");
			}

		});

		return RespBody.succeed(comment2);
	}

	@RequestMapping(value = "comment/update", method = RequestMethod.POST)
	@ResponseBody
	public RespBody updateComment(@RequestParam("basePath") String basePath, @RequestParam("id") Long id,
			@RequestParam("cid") Long cid, @RequestParam("version") Long version,
			@RequestParam("content") String content, @RequestParam("contentHtml") final String contentHtml,
			@RequestParam("diff") final String diff, @RequestParam(value = "users", required = false) String users) {

		// TODO 博文权限判断

		Comment comment = commentRepository.findOne(cid);

		if (comment == null) {
			return RespBody.failed("修改博文评论不存在,可能已经被删除!");
		}

		if (!isSuperOrCreator(comment.getCreator().getUsername())) {
			return RespBody.failed("您没有权限编辑该博文评论!");
		}

		if (comment.getVersion() != version.longValue()) {
			return RespBody.failed("该博文评论已经被其他人更新,请刷新页面重新编辑提交!");
		}

		comment.setContent(content);

		Comment comment2 = commentRepository.saveAndFlush(comment);

		final User loginUser = getLoginUser();

		final String href = basePath + "#/blog/" + id + "?cid=" + comment2.getId();

		Blog blog = blogRepository.findOne(id);

		Mail mail = Mail.instance().addUsers(blog.getCreator());
		if (StringUtil.isNotEmpty(users)) {
			Stream.of(users.split(",")).forEach(username -> {
				User user = getUser(username);
				mail.addUsers(user);
			});
		}
		
		final String html = StringUtil.replace("<h1 style=\"color: blue;\">评论博文: <a target=\"_blank\" href=\"{?1}\">{?2}</a></h1><hr/>{?3}", href, blog.getTitle(), contentHtml);

		ThreadUtil.exec(() -> {

			try {
				Thread.sleep(3000);
				mailSender.sendHtml(String.format("TMS-博文评论更新_%s", DateUtil.format(new Date(), DateUtil.FORMAT7)),
						TemplateUtil.process("templates/mail/mail-dynamic", MapUtil.objArr2Map("user", loginUser,
								"date", new Date(), "href", href, "title", "下面更新博文评论涉及到你", "content", html)),
						mail.get());
				logger.info("博文评论更新邮件发送成功！");
			} catch (Exception e) {
				e.printStackTrace();
				logger.error("博文评论更新邮件发送失败！");
			}

		});

		return RespBody.succeed(comment2);
	}

	@RequestMapping(value = "comment/query", method = RequestMethod.GET)
	@ResponseBody
	public RespBody queryComment(@RequestParam("id") Long id,
			@PageableDefault(sort = { "id" }, direction = Direction.ASC) Pageable pageable) {

		// TODO 博文权限判断

		Page<Comment> page = commentRepository.findByTargetIdAndStatusNot(String.valueOf(id), Status.Deleted, pageable);

		return RespBody.succeed(page);
	}

	@RequestMapping(value = "comment/remove", method = RequestMethod.POST)
	@ResponseBody
	public RespBody removeComment(@RequestParam("cid") Long cid) {

		Comment comment = commentRepository.findOne(cid);
		if (comment != null) {

			if (!isSuperOrCreator(comment.getCreator().getUsername())) {
				return RespBody.failed("您没有权限删除该博文评论!");
			}

			comment.setStatus(Status.Deleted);
			commentRepository.saveAndFlush(comment);
		}

		return RespBody.succeed(cid);
	}

	@RequestMapping(value = "comment/get", method = RequestMethod.GET)
	@ResponseBody
	public RespBody getComment(@RequestParam("cid") Long cid) {

		Comment comment = commentRepository.findOne(cid);

		// TODO 博文权限判断

		return RespBody.succeed(comment);
	}

	@RequestMapping(value = "space/update", method = RequestMethod.POST)
	@ResponseBody
	public RespBody updateSpace(@RequestParam("id") Long id, @RequestParam(value = "sid", required = false) Long sid) {

		Blog blog = blogRepository.findOne(id);

		if (!isSuperOrCreator(blog.getCreator().getUsername())) {
			return RespBody.failed("您没有权限修改该博文从属空间!");
		}

		Space space = sid != null ? spaceRepository.findOne(sid) : null;

		blog.setSpace(space);

		Blog blog2 = blogRepository.saveAndFlush(blog);

		return RespBody.succeed(blog2);
	}

	@RequestMapping(value = "privated/update", method = RequestMethod.POST)
	@ResponseBody
	public RespBody updatePrivated(@RequestParam("id") Long id, @RequestParam("privated") Boolean privated) {

		Blog blog = blogRepository.findOne(id);

		if (!isSuperOrCreator(blog.getCreator().getUsername())) {
			return RespBody.failed("您没有权限修改该博文可见性!");
		}

		blog.setPrivated(privated);
		if (privated) {
			blog.setOpenEdit(false);
		}

		Blog blog2 = blogRepository.saveAndFlush(blog);

		return RespBody.succeed(blog2);
	}

	@RequestMapping(value = "history/list", method = RequestMethod.GET)
	@ResponseBody
	public RespBody listHistory(@RequestParam("id") Long id) {

		Blog blog = blogRepository.findOne(id);

		if (!isSuperOrCreator(blog.getCreator().getUsername()) && blog.getPrivated()) {
			return RespBody.failed("您没有权限查看该博文可历史!");
		}

		List<BlogHistory> blogHistories = blogHistoryRepository.findByBlogAndStatusNot(blog, Status.Deleted);

		return RespBody.succeed(blogHistories);
	}

	@RequestMapping(value = "history/get", method = RequestMethod.GET)
	@ResponseBody
	public RespBody getHistory(@RequestParam("hid") Long hid) {

		BlogHistory blogHistory = blogHistoryRepository.findOne(hid);
		Blog blog = blogHistory.getBlog();

		if (!isSuperOrCreator(blog.getCreator().getUsername()) && blog.getPrivated()) {
			return RespBody.failed("您没有权限查看该博文可历史!");
		}

		return RespBody.succeed(blogHistory);
	}

	@RequestMapping(value = "history/remove", method = RequestMethod.POST)
	@ResponseBody
	public RespBody removeHistory(@RequestParam("hid") Long hid) {

		BlogHistory blogHistory = blogHistoryRepository.findOne(hid);
		Blog blog = blogHistory.getBlog();

		if (!isSuperOrCreator(blog.getCreator().getUsername())) {
			return RespBody.failed("您没有权限删除该博文历史!");
		}

		blogHistory.setStatus(Status.Deleted);

		blogHistoryRepository.saveAndFlush(blogHistory);

		return RespBody.succeed(hid);
	}

	@RequestMapping(value = "history/restore", method = RequestMethod.POST)
	@ResponseBody
	public RespBody restoreHistory(@RequestParam("hid") Long hid) {

		BlogHistory blogHistory = blogHistoryRepository.findOne(hid);
		Blog blog = blogHistory.getBlog();

		Boolean isOpenEdit = blog.getOpenEdit() == null ? false : blog.getOpenEdit();
		if (!isSuperOrCreator(blog.getCreator().getUsername()) && !isOpenEdit) {
			return RespBody.failed("您没有权限还原该博文历史!");
		}

		BlogHistory blogHistory2 = new BlogHistory();
		blogHistory2.setBlog(blog);
		blogHistory2.setTitle(blog.getTitle());
		blogHistory2.setContent(blog.getContent());
		blogHistory2.setBlogUpdater(blog.getUpdater());
		blogHistory2.setBlogUpdateDate(blog.getUpdateDate());

		blogHistoryRepository.saveAndFlush(blogHistory2);

		blog.setTitle(blogHistory.getTitle());
		blog.setContent(blogHistory.getContent());

		Blog blog2 = blogRepository.saveAndFlush(blog);

		return RespBody.succeed(blog2);
	}
	

	@RequestMapping(value = "download/{id}", method = RequestMethod.GET)
	public void download(HttpServletRequest request,
			HttpServletResponse response, @PathVariable Long id, @RequestParam(value = "type", defaultValue = "pdf") String type)
			throws Exception {

		logger.debug("download blog start...");
		
		Blog blog = blogRepository.findOne(id);

		if (blog == null) {
			try {
				response.sendError(404, "下载博文不存在!");
				return;
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		// 获取网站部署路径(通过ServletContext对象)，用于确定下载文件位置，从而实现下载
		String path = WebUtil.getRealPath(request);
		
		String blogUpdateDate = DateUtil.format(blog.getUpdateDate(), DateUtil.FORMAT9);
		
		String mdFileName = blog.getId() + "_" + blogUpdateDate + ".md";
		String pdfFileName = blog.getId() + "_" + blogUpdateDate + ".pdf";
		
		String mdFilePath = path + uploadPath + mdFileName;
		String pdfFilePath = path + uploadPath + pdfFileName;
		
		File fileMd = new File(mdFilePath);

		if (!fileMd.exists()) {
			try {
				FileUtils.writeStringToFile(fileMd, blog.getContent(), "UTF-8");
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
		if("md".equalsIgnoreCase(type)) {
			dnFileName = blog.getTitle().trim() + ".md";
			dnFileLength = String.valueOf(fileMd.length());
			dnFile = fileMd;
		} else {
			dnFileName = blog.getTitle().trim() + ".pdf";
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

}
