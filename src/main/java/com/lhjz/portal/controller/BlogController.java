/**
 * 版权所有 (TMS)
 */
package com.lhjz.portal.controller;

import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
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
import com.lhjz.portal.entity.Blog;
import com.lhjz.portal.entity.ChatChannel;
import com.lhjz.portal.entity.security.User;
import com.lhjz.portal.model.Mail;
import com.lhjz.portal.model.RespBody;
import com.lhjz.portal.pojo.Enum.Action;
import com.lhjz.portal.pojo.Enum.Status;
import com.lhjz.portal.pojo.Enum.Target;
import com.lhjz.portal.pojo.Enum.VoteType;
import com.lhjz.portal.repository.BlogRepository;
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

	@Autowired
	BlogRepository blogRepository;

	@Autowired
	UserRepository userRepository;

	@Autowired
	MailSender2 mailSender;

	@RequestMapping(value = "create", method = RequestMethod.POST)
	@ResponseBody
	public RespBody create(@RequestParam("url") String url,
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

		Page<ChatChannel> page = blogRepository.findByStatusNot(Status.Deleted, pageable);

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
			blog.setTitle(title);
			blog.setContent(content);

			Blog blog2 = blogRepository.saveAndFlush(blog);

			// TODO version control

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

		return RespBody.succeed(blog);
	}

	@RequestMapping(value = "search", method = RequestMethod.GET)
	@ResponseBody
	public RespBody search(@RequestParam("search") String search,
			@PageableDefault(sort = { "id" }, direction = Direction.DESC) Pageable pageable) {

		if (StringUtil.isEmpty(search)) {
			return RespBody.failed("检索条件不能为空!");
		}

		Page<ChatChannel> page = blogRepository.findByTitleContainingOrContentContaining(search, search, pageable);

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
			String voteCai = blog.getVoteCai();
			if (isVoterExists(voteCai)) {
				return RespBody.failed("您已经投票[踩]过！");
			} else {
				blog.setVoteCai(voteCai == null ? loginUsername : voteCai + ',' + loginUsername);

				Integer voteCaiCnt = blog.getVoteCaiCnt();
				if (voteCaiCnt == null) {
					voteCaiCnt = 0;
				}
				blog.setVoteCaiCnt(++voteCaiCnt);

				blog2 = blogRepository.saveAndFlush(blog);
				title = loginUser.getName() + "[" + loginUsername + "]踩了你的博文消息!";
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

		log(Action.Vote, Target.ChatChannel, blog.getId(), blog2);

		return RespBody.succeed(blog2);
	}

}
