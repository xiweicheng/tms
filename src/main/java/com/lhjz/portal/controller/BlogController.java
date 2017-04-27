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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.FileUtils;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.web.SortDefault;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.lhjz.portal.base.BaseController;
import com.lhjz.portal.component.MailSender2;
import com.lhjz.portal.entity.Blog;
import com.lhjz.portal.entity.BlogAuthority;
import com.lhjz.portal.entity.BlogFollower;
import com.lhjz.portal.entity.BlogHistory;
import com.lhjz.portal.entity.BlogStow;
import com.lhjz.portal.entity.Channel;
import com.lhjz.portal.entity.ChatChannel;
import com.lhjz.portal.entity.ChatDirect;
import com.lhjz.portal.entity.Comment;
import com.lhjz.portal.entity.Log;
import com.lhjz.portal.entity.Space;
import com.lhjz.portal.entity.SpaceAuthority;
import com.lhjz.portal.entity.security.User;
import com.lhjz.portal.model.Mail;
import com.lhjz.portal.model.PollBlog;
import com.lhjz.portal.model.RespBody;
import com.lhjz.portal.pojo.Enum.Action;
import com.lhjz.portal.pojo.Enum.CommentType;
import com.lhjz.portal.pojo.Enum.Status;
import com.lhjz.portal.pojo.Enum.Target;
import com.lhjz.portal.pojo.Enum.VoteType;
import com.lhjz.portal.repository.BlogAuthorityRepository;
import com.lhjz.portal.repository.BlogFollowerRepository;
import com.lhjz.portal.repository.BlogHistoryRepository;
import com.lhjz.portal.repository.BlogRepository;
import com.lhjz.portal.repository.BlogStowRepository;
import com.lhjz.portal.repository.ChannelRepository;
import com.lhjz.portal.repository.ChatChannelRepository;
import com.lhjz.portal.repository.ChatDirectRepository;
import com.lhjz.portal.repository.CommentRepository;
import com.lhjz.portal.repository.LogRepository;
import com.lhjz.portal.repository.SpaceRepository;
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
	BlogAuthorityRepository blogAuthorityRepository;

	@Autowired
	SpaceRepository spaceRepository;

	@Autowired
	ChannelRepository channelRepository;
	
	@Autowired
	ChatChannelRepository chatChannelRepository;
	
	@Autowired
	ChatDirectRepository chatDirectRepository;

	@Autowired
	UserRepository userRepository;

	@Autowired
	CommentRepository commentRepository;
	
	@Autowired
	BlogStowRepository blogStowRepository;
	
	@Autowired
	BlogFollowerRepository blogFollowerRepository;
	
	@Autowired
	LogRepository logRepository;

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
		
		log(Action.Create, Target.Blog, blog2.getId(), blog2.getTitle());

		final String href = url + "#/blog/" + blog2.getId();
		final String html = contentHtml;
		final User loginUser = getLoginUser();

		final Mail mail = Mail.instance();
		if (StringUtil.isNotEmpty(usernames)) {

			if (StringUtil.isNotEmpty(usernames)) {
				String[] usernameArr = usernames.split(",");
				Arrays.asList(usernameArr).stream().forEach((username) -> {
					mail.addUsers(getUser(username));
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
	public RespBody list(@SortDefault(value = "id", direction = Direction.DESC) Sort sort) {

		if (!isSuper()) {
			return RespBody.failed("没有权限查看全部博文列表!");
		}

		List<Blog> blogs = blogRepository.findByStatusNot(Status.Deleted, sort);
		blogs.forEach(b -> b.setContent(null));

		return RespBody.succeed(blogs);
	}
	
	@RequestMapping(value = "listMy", method = RequestMethod.GET)
	@ResponseBody
	public RespBody listMy(@SortDefault(value = "id", direction = Direction.DESC) Sort sort) {

		List<Blog> blogs = blogRepository.findByStatusNot(Status.Deleted, sort).stream().filter(b -> hasAuth(b))
				.peek(b -> b.setContent(null)).collect(Collectors.toList());

		return RespBody.succeed(blogs);
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
		
		if(isOpenEdit && !hasAuth(blog)) {
			return RespBody.failed("您没有权限编辑该博文!");
		}

		if (blog.getVersion() != version.longValue()) {
			return RespBody.failed("该博文已经被其他人更新,请刷新博文重新编辑提交!");
		}

		boolean isUpdated = false;

		if (!content.equals(blog.getContent())) {
			logWithProperties(Action.Update, Target.Blog, blog.getId(), "content", diff, blog.getTitle());
			isUpdated = true;
		}

		if (!title.equals(blog.getTitle())) {
			logWithProperties(Action.Update, Target.Blog, blog.getId(), "title", title, blog.getTitle());
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
			final String href = url + "#/blog/" + blog2.getId();
			final String html;
			if (StringUtil.isNotEmpty(diff)) {
				html = "<h3>内容(Markdown)变更对比:</h3><b>原文链接:</b> <a href=\"" + href + "\">" + href + "</a><hr/>" + diff;
			} else {
				html = "<h3>编辑后内容:</h3>" + contentHtml + "<hr/><h3>编辑前内容:</h3>" + contentHtmlOld;
			}

			final Mail mail = Mail.instance();
			
			boolean isCreator = blog2.getCreator().equals(loginUser);
			
			List<BlogFollower> followers = blogFollowerRepository.findByBlogAndStatusNot(blog2, Status.Deleted);

			if (!isCreator || StringUtil.isNotEmpty(usernames) || followers.size() > 0) {
				
				if (!isCreator) {
					mail.addUsers(blog2.getCreator());
				}
				
				if (StringUtil.isNotEmpty(usernames)) {
					String[] usernameArr = usernames.split(",");
					Arrays.asList(usernameArr).stream().forEach((username) -> {
						mail.addUsers(getUser(username));
					});
				}

				followers.forEach(bf -> mail.addUsers(bf.getCreator()));

				ThreadUtil.exec(() -> {

					try {
						Thread.sleep(3000);
						mailSender.sendHtml(
								String.format("TMS-博文编辑@消息_%s", DateUtil.format(new Date(), DateUtil.FORMAT7)),
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

		log(Action.Delete, Target.Blog, id, blog.getTitle());

		return RespBody.succeed(id);
	}

	@RequestMapping(value = "get", method = RequestMethod.GET)
	@ResponseBody
	public RespBody get(@RequestParam("id") Long id) {

		Blog blog = blogRepository.findOne(id);

		if (blog == null || Status.Deleted.equals(blog.getStatus())) {
			return RespBody.failed("博文消息不存在或者已经被删除!");
		}

		if (!hasAuth(blog)) {
			return RespBody.failed("您没有权限查看该博文!");
		}

		Long readCnt = blog.getReadCnt();
		if (readCnt == null) {
			readCnt = 1L;
		} else {
			readCnt = readCnt + 1;
		}
		
		blogRepository.updateReadCnt(readCnt, id);

		blog.setReadCnt(readCnt);

		return RespBody.succeed(blog);
	}

	@RequestMapping(value = "search", method = RequestMethod.GET)
	@ResponseBody
	public RespBody search(@RequestParam("search") String search,
			@SortDefault(value = "id", direction = Direction.DESC) Sort sort) {

		if (StringUtil.isEmpty(search)) {
			return RespBody.failed("检索条件不能为空!");
		}

		List<Blog> blogs = blogRepository
				.findByStatusNotAndTitleContainingOrStatusNotAndContentContaining(Status.Deleted, search,
						Status.Deleted, search, sort)
				.stream().filter(b -> hasAuth(b)).peek(b -> {
					b.setContent(null);
					b.setBlogAuthorities(null);
				}).collect(Collectors.toList());

		return RespBody.succeed(blogs);
	}

	@RequestMapping(value = "openEdit", method = RequestMethod.POST)
	@ResponseBody
	public RespBody openEdit(@RequestParam("id") Long id, @RequestParam("open") Boolean open) {

		Blog blog = blogRepository.findOne(id);

		if (blog == null) {
			return RespBody.failed("博文消息不存在,可能已经被删除!");
		}

		if (!isSuperOrCreator(blog.getCreator().getUsername())) {
			return RespBody.failed("没有权限修改博文开放编辑全选!");
		}

		blog.setOpenEdit(open);
		blogRepository.saveAndFlush(blog);
		
		logWithProperties(Action.Update, Target.Blog, id, "openEdit", open, blog.getTitle());

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

		String title = "";
		final User loginUser = getLoginUser();

		if (VoteType.Zan.name().equalsIgnoreCase(type)) {
			String voteZan = blog.getVoteZan();
			if (isVoterExists(voteZan)) {
				return RespBody.failed("您已经投票[赞]过！");
			} else {
				
				String vz = voteZan == null ? loginUsername : voteZan + ',' + loginUsername;

				Integer voteZanCnt = blog.getVoteZanCnt();
				Integer vzc = voteZanCnt == null ? 1 : voteZanCnt + 1;

				blogRepository.updateVoteZan(vz, vzc, id);
				
				logWithProperties(Action.Update, Target.Blog, id, "voteZan", blog.getTitle());
				
				blog.setVoteZan(vz);
				blog.setVoteZanCnt(vzc);
				title = loginUser.getName() + "[" + loginUsername + "]赞了你的博文消息!";
			}

		} else {
			String voteZan = blog.getVoteZan();
			if (isVoterExists(voteZan)) {
				String vz = this.calcVoters(voteZan);
				Integer voteZanCnt = blog.getVoteZanCnt();
				Integer vzc = voteZanCnt == null ? 0 : voteZanCnt - 1;

				blogRepository.updateVoteZan(vz, vzc, id);
				
				blog.setVoteZan(vz);
				blog.setVoteZanCnt(vzc);
				return RespBody.succeed(blog);
			}
		}

		final String href = url + "#/blog/" + id;
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

		return RespBody.succeed(blog);
	}
	
	@RequestMapping(value = "comment/vote", method = RequestMethod.POST)
	@ResponseBody
	public RespBody voteComment(@RequestParam("cid") Long cid, @RequestParam("url") String url,
			@RequestParam("contentHtml") String contentHtml,
			@RequestParam(value = "type", required = false) String type) {

		Comment comment = commentRepository.findOne(cid);
		if (comment == null) {
			return RespBody.failed("投票博文评论不存在!");
		}
		String loginUsername = WebUtil.getUsername();

		Comment comment2 = null;

		String title = "";
		final User loginUser = getLoginUser();

		if (VoteType.Zan.name().equalsIgnoreCase(type)) {
			String voteZan = comment.getVoteZan();
			if (isVoterExists(voteZan)) {
				return RespBody.failed("您已经投票[赞]过！");
			} else {
				comment.setVoteZan(voteZan == null ? loginUsername : voteZan + ',' + loginUsername);

				Integer voteZanCnt = comment.getVoteZanCnt();
				comment.setVoteZanCnt(voteZanCnt == null ? 1 : voteZanCnt + 1);

				comment2 = commentRepository.saveAndFlush(comment);
				title = loginUser.getName() + "[" + loginUsername + "]赞了你的博文评论!";
				
				logWithProperties(Action.Update, Target.Comment, cid, "voteZan", comment2.getTargetId(), comment2.getContent());
			}

		} else {
			String voteZan = comment.getVoteZan();
			if (isVoterExists(voteZan)) {
				comment.setVoteZan(this.calcVoters(voteZan));
				Integer voteZanCnt = comment.getVoteZanCnt();
				comment.setVoteZanCnt(voteZanCnt == null ? 0 : voteZanCnt - 1);

				comment2 = commentRepository.saveAndFlush(comment);
				return RespBody.succeed(comment2);
			}
		}

		final String href = url + "#/blog/" + comment.getTargetId() + "?cid=" + cid;
		final String titleHtml = title;
		final Mail mail = Mail.instance().addUsers(comment.getCreator());
		final String html = "<h3>投票博文评论内容:</h3><hr/>" + contentHtml;

		ThreadUtil.exec(() -> {

			try {
				Thread.sleep(3000);
				mailSender.sendHtml(String.format("TMS-博文评论投票@消息_%s", DateUtil.format(new Date(), DateUtil.FORMAT7)),
						TemplateUtil.process("templates/mail/mail-dynamic", MapUtil.objArr2Map("user", loginUser,
								"date", new Date(), "href", href, "title", titleHtml, "content", html)),
						mail.get());
				logger.info("博文评论投票邮件发送成功！");
			} catch (Exception e) {
				e.printStackTrace();
				logger.error("博文评论投票邮件发送失败！");
			}

		});

		return RespBody.succeed(comment2);
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
		channels.forEach(c -> c.setMembers(null));
		
		map.put("users", users);
		map.put("channels", channels);

		return RespBody.succeed(map);
	}

	@RequestMapping(value = "share", method = RequestMethod.POST)
	@ResponseBody
	public RespBody share(@RequestParam("basePath") String basePath, @RequestParam("id") Long id,
			@RequestParam("html") String html, @RequestParam(value = "desc", required = false) String desc,
			@RequestParam(value = "users", required = false) String users,
			@RequestParam(value = "channels", required = false) String channels,
			@RequestParam(value = "mails", required = false) String mails) {

		Blog blog = blogRepository.findOne(id);

		if (!hasAuth(blog)) {
			return RespBody.failed("您没有权限分享该博文!");
		}
		
		final User loginUser = getLoginUser();

		final String href = basePath + "#/blog/" + id;
		final String html2 = StringUtil.replace(
				"<h1 style=\"color: blue;\">分享博文: <a target=\"_blank\" href=\"{?1}\">{?2}</a></h1><hr/>{?3}", href,
				blog.getTitle(), html);

		final String title = StringUtil.isNotEmpty(desc) ? desc : "下面的博文有分享到你";

		Mail mail = Mail.instance();
		if (StringUtil.isNotEmpty(users)) {
			Stream.of(users.split(",")).forEach(username -> {
				User user = getUser(username);
				if (user != null) {
					mail.addUsers(user);

					ChatDirect chatDirect = new ChatDirect();
					chatDirect.setChatTo(user);
					chatDirect.setContent(
							StringUtil.replace("## ~私聊消息播报~\n> 来自 {~{?1}} 的博文分享:  [{?2}]({?3})\n\n---\n\n{?4}",
									loginUser.getUsername(), blog.getTitle(), href, blog.getContent()));

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
							StringUtil.replace("## ~频道消息播报~\n> 来自 {~{?1}} 的博文分享:  [{?2}]({?3})\n\n---\n\n{?4}",
									loginUser.getUsername(), blog.getTitle(), href, blog.getContent()));

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
	
	@RequestMapping(value = "comment/share", method = RequestMethod.POST)
	@ResponseBody
	public RespBody shareComment(@RequestParam("basePath") String basePath, @RequestParam("id") Long id,
			@RequestParam("href") final String href, @RequestParam("html") String html,
			@RequestParam(value = "desc", required = false) String desc,
			@RequestParam(value = "users", required = false) String users,
			@RequestParam(value = "channels", required = false) String channels,
			@RequestParam(value = "mails", required = false) String mails) {

		Comment comment = commentRepository.findOne(id);

		final User loginUser = getLoginUser();

		final String html2 = StringUtil.replace(
				"<h1 style=\"color: blue;\">分享博文评论: <a target=\"_blank\" href=\"{?1}\">{?2}</a></h1><hr/>{?3}", href,
				"博文评论链接", html);

		final String title = StringUtil.isNotEmpty(desc) ? desc : "下面的博文评论有分享到你";

		Mail mail = Mail.instance();
		if (StringUtil.isNotEmpty(users)) {
			Stream.of(users.split(",")).forEach(username -> {
				User user = getUser(username);
				if (user != null) {
					mail.addUsers(user);

					ChatDirect chatDirect = new ChatDirect();
					chatDirect.setChatTo(user);
					chatDirect.setContent(
							StringUtil.replace("## ~私聊消息播报~\n> 来自 {~{?1}} 的博文评论分享:  [{?2}]({?3})\n\n---\n\n{?4}",
									loginUser.getUsername(), "博文评论链接", href, comment.getContent()));

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
							StringUtil.replace("## ~频道消息播报~\n> 来自 {~{?1}} 的博文评论分享:  [{?2}]({?3})\n\n---\n\n{?4}",
									loginUser.getUsername(), "博文评论链接", href, comment.getContent()));

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
						.sendHtml(String.format("TMS-博文评论分享_%s", DateUtil.format(new Date(), DateUtil.FORMAT7)),
								TemplateUtil.process("templates/mail/mail-dynamic", MapUtil.objArr2Map("user",
										loginUser, "date", new Date(), "href", href, "title", title, "content", html2)),
								mail.get());
				logger.info("博文评论分享邮件发送成功！");
			} catch (Exception e) {
				e.printStackTrace();
				logger.error("博文评论分享邮件发送失败！");
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
		
		log(Action.Create, Target.Comment, comment2.getId(), content, id);

		final User loginUser = getLoginUser();

		final String href = basePath + "#/blog/" + id + "?cid=" + comment2.getId();

		Blog blog = blogRepository.findOne(id);

		Mail mail = Mail.instance();
		if (!blog.getCreator().equals(loginUser)) {
			mail.addUsers(blog.getCreator());
		}
		if (StringUtil.isNotEmpty(users)) {
			Stream.of(users.split(",")).forEach(username -> {
				User user = getUser(username);
				mail.addUsers(user);
			});
		}

		List<BlogFollower> followers = blogFollowerRepository.findByBlogAndStatusNot(blog, Status.Deleted);
		followers.forEach(bf -> mail.addUsers(bf.getCreator()));
		
		// auto follow blog
		boolean isFollower = followers.stream().anyMatch(f -> f.getCreator().equals(loginUser));
		if (!isFollower && !blog.getCreator().equals(loginUser)) {
			BlogFollower blogFollower = blogFollowerRepository.findOneByBlogAndCreator(blog, getLoginUser());

			if (blogFollower != null) {
				if (blogFollower.getStatus().equals(Status.Deleted)) {
					blogFollower.setStatus(Status.New);

					blogFollowerRepository.saveAndFlush(blogFollower);
				}
			} else {
				blogFollower = new BlogFollower();
				blogFollower.setBlog(blog);

				blogFollowerRepository.saveAndFlush(blogFollower);
			}
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
		
		logWithProperties(Action.Update, Target.Comment, cid, "content", diff, id);

		Comment comment2 = commentRepository.saveAndFlush(comment);

		final User loginUser = getLoginUser();

		final String href = basePath + "#/blog/" + id + "?cid=" + comment2.getId();

		Blog blog = blogRepository.findOne(id);

		Mail mail = Mail.instance();
		if (!blog.getCreator().equals(loginUser)) {
			mail.addUsers(blog.getCreator());
		}
		if (StringUtil.isNotEmpty(users)) {
			Stream.of(users.split(",")).forEach(username -> {
				User user = getUser(username);
				mail.addUsers(user);
			});
		}
		
		List<BlogFollower> followers = blogFollowerRepository.findByBlogAndStatusNot(blog, Status.Deleted);
		followers.forEach(bf -> mail.addUsers(bf.getCreator()));
		
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
			
			log(Action.Delete, Target.Comment, cid, comment.getContent());
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

		logWithProperties(Action.Update, Target.Blog, id, "space", space != null ? space.getName() : "",
				blog.getTitle());

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
		
		Blog blog2 = blogRepository.saveAndFlush(blog);

		logWithProperties(Action.Update, Target.Blog, id, "privated", privated, blog.getTitle());

		return RespBody.succeed(blog2);
	}

	@RequestMapping(value = "history/list", method = RequestMethod.GET)
	@ResponseBody
	public RespBody listHistory(@RequestParam("id") Long id) {

		Blog blog = blogRepository.findOne(id);

		if (!hasAuth(blog)) {
			return RespBody.failed("您没有权限查看该博文历史列表!");
		}

		List<BlogHistory> blogHistories = blogHistoryRepository.findByBlogAndStatusNot(blog, Status.Deleted);

		return RespBody.succeed(blogHistories);
	}

	@RequestMapping(value = "history/get", method = RequestMethod.GET)
	@ResponseBody
	public RespBody getHistory(@RequestParam("hid") Long hid) {

		BlogHistory blogHistory = blogHistoryRepository.findOne(hid);
		Blog blog = blogHistory.getBlog();
		
		if (!hasAuth(blog)) {
			return RespBody.failed("您没有权限查看该博文历史!");
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

		if (!hasAuth(blog)) {
			return RespBody.failed("您没有权限查看该博文历史!");
		}

		Boolean isOpenEdit = blog.getOpenEdit() == null ? false : blog.getOpenEdit();
		if (!isSuperOrCreator(blog.getCreator().getUsername()) && !isOpenEdit) {
			return RespBody.failed("您没有权限还原该博文历史!");
		}

		if (isOpenEdit && !hasAuth(blog)) {
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
		
		if (!hasAuth(blog)) {
			try {
				response.sendError(404, "您没有权限下载该博文!");
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
	
	private boolean hasAuth(Blog b) {
		
		if (b == null) {
			return false;
		}

		if (b.getStatus().equals(Status.Deleted)) { // 过滤掉删除的
			return false;
		}

		return hasAuthWithDeleted(b);
	}

	private boolean hasSpaceAuth(Space s) {

		if (s == null) {
			return false;
		}

		if (isSuper()) { // 超级用户
			return true;
		}

		if (s.getStatus().equals(Status.Deleted)) { // 过滤掉删除的
			return false;
		}

		User loginUser = new User(WebUtil.getUsername());

		// 过滤掉没有权限的
		if (s.getCreator().equals(loginUser)) { // 我创建的
			return true;
		}

		if (!s.getPrivated()) { // 非私有的
			return true;
		}

		boolean exists = false;
		for (SpaceAuthority sa : s.getSpaceAuthorities()) {
			if (loginUser.equals(sa.getUser())) {
				exists = true;
				break;
			} else {
				Channel channel = sa.getChannel();
				if (channel != null) {
					Set<User> members = channel.getMembers();
					if (members.contains(loginUser)) {
						exists = true;
						break;
					}
				}
			}
		}

		return exists;
	}
	
	private boolean hasAuthWithDeleted(Blog b) {

		if (b == null) {
			return false;
		}

		if (isSuper()) { // 超级用户
			return true;
		}

		User loginUser = new User(WebUtil.getUsername());

		// 过滤掉没有权限的
		if (b.getCreator().equals(loginUser)) { // 我创建的
			return true;
		}

		if (!b.getPrivated()) { // 非私有的
			if (b.getSpace() == null) {
				return true;
			} else {
				return hasSpaceAuth(b.getSpace());
			}
		}

		boolean exists = false;
		for (BlogAuthority ba : b.getBlogAuthorities()) {
			if (loginUser.equals(ba.getUser())) {
				exists = true;
				break;
			} else {
				Channel channel = ba.getChannel();
				if (channel != null) {
					Set<User> members = channel.getMembers();
					if (members.contains(loginUser)) {
						exists = true;
						break;
					}
				}
			}
		}

		return exists;
	}
	
	@RequestMapping(value = "auth/get", method = RequestMethod.GET)
	@ResponseBody
	public RespBody getAuth(@RequestParam("id") Long id) {
		Blog blog = blogRepository.findOne(id);
		if(!hasAuth(blog)) {
			return RespBody.failed("您没有权限查看该博文权限!");
		}
		return RespBody.succeed(blog.getBlogAuthorities());
	}
	
	@RequestMapping(value = "auth/add", method = RequestMethod.POST)
	@ResponseBody
	public RespBody addAuth(@RequestParam("id") Long id,
			@RequestParam(value = "channels", required = false) String channels,
			@RequestParam(value = "users", required = false) String users) {
		Blog blog = blogRepository.findOne(id);
		if (!isSuperOrCreator(blog.getCreator().getUsername())) {
			return RespBody.failed("您没有权限为该博文添加权限!");
		}

		List<BlogAuthority> blogAuthorities = new ArrayList<>();

		if (StringUtil.isNotEmpty(channels)) {
			Stream.of(channels.split(",")).forEach(c -> {
				Channel channel = channelRepository.findOne(Long.valueOf(c));

				if (channel != null) {
					BlogAuthority blogAuthority = new BlogAuthority();
					blogAuthority.setBlog(blog);
					blogAuthority.setChannel(channel);
					blogAuthorities.add(blogAuthority);
				}

			});
		}

		if (StringUtil.isNotEmpty(users)) {
			Stream.of(users.split(",")).forEach(u -> {
				User user = userRepository.findOne(u);
				if (user != null) {
					BlogAuthority blogAuthority = new BlogAuthority();
					blogAuthority.setBlog(blog);
					blogAuthority.setUser(user);
					blogAuthorities.add(blogAuthority);
				}

			});
		}

		List<BlogAuthority> list = blogAuthorityRepository.save(blogAuthorities);
		blogAuthorityRepository.flush();
		
		blog.getBlogAuthorities().addAll(list);

		return RespBody.succeed(blog);
	}
	
	@RequestMapping(value = "auth/remove", method = RequestMethod.POST)
	@ResponseBody
	public RespBody removeAuth(@RequestParam("id") Long id,
			@RequestParam(value = "channels", required = false) String channels,
			@RequestParam(value = "users", required = false) String users) {
		Blog blog = blogRepository.findOne(id);
		if (!isSuperOrCreator(blog.getCreator().getUsername())) {
			return RespBody.failed("您没有权限为该博文移除权限!");
		}
		
		List<BlogAuthority> list = new ArrayList<>();
		Collection<Channel> channelC = new ArrayList<>();
		if (StringUtil.isNotEmpty(channels)) {
			Stream.of(channels.split(",")).forEach(c -> {
				Channel ch = new Channel();
				ch.setId(Long.valueOf(c));
				channelC.add(ch);
				
				BlogAuthority ba = new BlogAuthority();
				ba.setBlog(blog);
				ba.setChannel(ch);
				list.add(ba);
			});
		}
		Collection<User> userC = new ArrayList<>();
		if (StringUtil.isNotEmpty(users)) {
			Stream.of(users.split(",")).forEach(u -> {
				User user = new User();
				user.setUsername(u);
				userC.add(user);
				
				BlogAuthority ba = new BlogAuthority();
				ba.setBlog(blog);
				ba.setUser(user);
				list.add(ba);
			});
		}
		
		if (channelC.size() > 0 && userC.size() > 0) {
			blogAuthorityRepository.removeAuths(blog, channelC, userC);
		} else {
			if (channelC.size() > 0) {
				blogAuthorityRepository.removeChannelAuths(blog, channelC);
			} else if (userC.size() > 0) {
				blogAuthorityRepository.removeUserAuths(blog, userC);
			}
		}

		blogAuthorityRepository.flush();
		
		blog.getBlogAuthorities().removeAll(list);

		return RespBody.succeed(blog);
	}

	@RequestMapping(value = "stow/add", method = RequestMethod.POST)
	@ResponseBody
	public RespBody addStow(@RequestParam("id") Long id) {
		Blog blog = blogRepository.findOne(id);
		if (!hasAuth(blog)) {
			return RespBody.failed("您没有权限收藏该博文!");
		}

		User loginUser = getLoginUser();
		
		BlogStow blogStow3 = blogStowRepository.findOneByBlogAndCreator(blog, loginUser);

		if (blogStow3 != null) {
			if (!blogStow3.getStatus().equals(Status.Deleted)) {
				return RespBody.failed("您已经收藏过该博文!");
			} else {
				blogStow3.setStatus(Status.New);
				
				BlogStow blogStow = blogStowRepository.saveAndFlush(blogStow3);
				
				logWithProperties(Action.Update, Target.Blog, id, "stow", blog.getTitle());

				return RespBody.succeed(blogStow);
			}
		} else {
			BlogStow blogStow = new BlogStow();
			blogStow.setBlog(blog);
			
			BlogStow blogStow2 = blogStowRepository.saveAndFlush(blogStow);
			
			logWithProperties(Action.Update, Target.Blog, id, "stow", blog.getTitle());

			return RespBody.succeed(blogStow2);
		}

	}

	@RequestMapping(value = "stow/remove", method = RequestMethod.POST)
	@ResponseBody
	public RespBody removeStow(@RequestParam("sid") Long sid) {

		BlogStow blogStow = blogStowRepository.findOne(sid);

		if (blogStow == null) {
			return RespBody.failed("该博文收藏记录不存在!");
		}

		if (!hasAuth(blogStow.getBlog())) {
			return RespBody.failed("您没有权限操作该博文!");
		}

		blogStow.setStatus(Status.Deleted);

		blogStowRepository.saveAndFlush(blogStow);

		return RespBody.succeed(sid);
	}

	@RequestMapping(value = "stow/listMy", method = RequestMethod.GET)
	@ResponseBody
	public RespBody listMyStow() {

		List<BlogStow> blogStows = blogStowRepository.findByCreatorAndStatusNot(getLoginUser(), Status.Deleted);
		blogStows = blogStows.stream().filter(bs -> !bs.getBlog().getStatus().equals(Status.Deleted))
				.collect(Collectors.toList());
		blogStows.forEach(bs -> {
			bs.getBlog().setContent(null);
			bs.getBlog().setBlogAuthorities(null);
		});

		return RespBody.succeed(blogStows);
	}
	
	@RequestMapping(value = "stow/get", method = RequestMethod.GET)
	@ResponseBody
	public RespBody getStow(@RequestParam("id") Long id) {

		Blog blog = blogRepository.findOne(id);
		if (!hasAuth(blog)) {
			return RespBody.failed("您没有权限操作该博文!");
		}

		BlogStow blogStow = blogStowRepository.findOneByBlogAndCreatorAndStatusNot(blog, getLoginUser(), Status.Deleted);
		
		return RespBody.succeed(blogStow);
	}
	
	@RequestMapping(value = "stow/list", method = RequestMethod.GET)
	@ResponseBody
	public RespBody listStow(@RequestParam("id") Long id) {

		Blog blog = blogRepository.findOne(id);
		if (!hasAuth(blog)) {
			return RespBody.failed("您没有权限操作该博文!");
		}

		List<BlogStow> stows = blogStowRepository.findByBlogAndStatusNot(blog, Status.Deleted);
		stows.forEach(bs -> bs.setBlog(null));

		return RespBody.succeed(stows);
	}
	
	@RequestMapping(value = "follower/add", method = RequestMethod.POST)
	@ResponseBody
	public RespBody addFollower(@RequestParam("id") Long id) {
		Blog blog = blogRepository.findOne(id);
		if (!hasAuth(blog)) {
			return RespBody.failed("您没有权限关注该博文!");
		}
		
		User loginUser = getLoginUser();
		
		BlogFollower blogFollower = blogFollowerRepository.findOneByBlogAndCreator(blog, loginUser);

		if (blogFollower != null) {
			if (!blogFollower.getStatus().equals(Status.Deleted)) {
				return RespBody.failed("您已经关注过该博文!");
			} else {
				blogFollower.setStatus(Status.New);
				
				BlogFollower blogFollower2 = blogFollowerRepository.saveAndFlush(blogFollower);
				
				logWithProperties(Action.Update, Target.Blog, id, "follower", blog.getTitle());

				return RespBody.succeed(blogFollower2);
			}
		} else {
			BlogFollower blogFollower2 = new BlogFollower();
			blogFollower2.setBlog(blog);
			
			BlogFollower blogFollower3 = blogFollowerRepository.saveAndFlush(blogFollower2);
			
			logWithProperties(Action.Update, Target.Blog, id, "follower", blog.getTitle());

			return RespBody.succeed(blogFollower3);
		}

	}

	@RequestMapping(value = "follower/remove", method = RequestMethod.POST)
	@ResponseBody
	public RespBody removeFollower(@RequestParam("fid") Long fid) {

		BlogFollower blogFollower = blogFollowerRepository.findOne(fid);

		if (blogFollower == null) {
			return RespBody.failed("该博文关注记录不存在!");
		}

		if (!hasAuth(blogFollower.getBlog())) {
			return RespBody.failed("您没有权限操作该博文!");
		}

		blogFollower.setStatus(Status.Deleted);

		blogFollowerRepository.saveAndFlush(blogFollower);

		return RespBody.succeed(fid);
	}

	@RequestMapping(value = "follower/listMy", method = RequestMethod.GET)
	@ResponseBody
	public RespBody listMyFollower() {

		List<BlogFollower> blogFollowers = blogFollowerRepository.findByCreatorAndStatusNot(getLoginUser(),
				Status.Deleted);
		blogFollowers.forEach(bf -> {
			bf.getBlog().setContent(null);
			bf.getBlog().setBlogAuthorities(null);
		});

		return RespBody.succeed(blogFollowers);
	}

	@RequestMapping(value = "follower/get", method = RequestMethod.GET)
	@ResponseBody
	public RespBody getFollower(@RequestParam("id") Long id) {

		Blog blog = blogRepository.findOne(id);
		if (!hasAuth(blog)) {
			return RespBody.failed("您没有权限操作该博文!");
		}

		BlogFollower blogFollower = blogFollowerRepository.findOneByBlogAndCreatorAndStatusNot(blog, getLoginUser(), Status.Deleted);

		return RespBody.succeed(blogFollower);
	}
	
	@RequestMapping(value = "follower/list", method = RequestMethod.GET)
	@ResponseBody
	public RespBody listFollower(@RequestParam("id") Long id) {

		Blog blog = blogRepository.findOne(id);
		if (!hasAuth(blog)) {
			return RespBody.failed("您没有权限操作该博文!");
		}

		List<BlogFollower> followers = blogFollowerRepository.findByBlogAndStatusNot(blog, Status.Deleted);
		followers.forEach(bf -> bf.setBlog(null));

		return RespBody.succeed(followers);
	}
	
	@RequestMapping(value = "poll", method = RequestMethod.GET)
	@ResponseBody
	public RespBody poll(@RequestParam("id") Long id) {
		
		Blog blog = blogRepository.findOne(id);
		if (!hasAuth(blog)) {
			return RespBody.failed("您没有权限操作该博文!");
		}
		
		return RespBody.succeed(PollBlog.builder().version(blog.getVersion()).build());
	}
	
	@RequestMapping(value = "log/my", method = RequestMethod.GET)
	@ResponseBody
	public RespBody myLog() {

		List<Log> logs = logRepository.findByTargetInAndCreateDateAfter(Arrays.asList(Target.Blog, Target.Comment),
				new DateTime().minusDays(7).toDate());

		logs = logs.stream().filter(lg -> {

			String targetId = lg.getTargetId();
			if (Target.Blog.equals(lg.getTarget())) {
				Blog blog = blogRepository.findOne(Long.valueOf(targetId));
				return hasAuthWithDeleted(blog);
			} else if (Target.Comment.equals(lg.getTarget())) {
				Comment comment = commentRepository.findOne(Long.valueOf(targetId));
				Blog blog = blogRepository.findOne(Long.valueOf(comment.getTargetId()));
				return hasAuthWithDeleted(blog);
			}

			return false;
		}).limit(100).collect(Collectors.toList());

		return RespBody.succeed(logs);
	}

}
