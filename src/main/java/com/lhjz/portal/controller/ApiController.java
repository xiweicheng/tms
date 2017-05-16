/**
 * 版权所有 (TMS)
 */
package com.lhjz.portal.controller;

import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.jayway.jsonpath.JsonPath;
import com.lhjz.portal.base.BaseController;
import com.lhjz.portal.component.MailSender2;
import com.lhjz.portal.constant.SysConstant;
import com.lhjz.portal.entity.Channel;
import com.lhjz.portal.entity.ChatChannel;
import com.lhjz.portal.entity.security.User;
import com.lhjz.portal.model.Mail;
import com.lhjz.portal.model.RespBody;
import com.lhjz.portal.repository.ChannelRepository;
import com.lhjz.portal.repository.ChatChannelRepository;
import com.lhjz.portal.repository.UserRepository;
import com.lhjz.portal.util.DateUtil;
import com.lhjz.portal.util.MapUtil;
import com.lhjz.portal.util.StringUtil;
import com.lhjz.portal.util.TemplateUtil;
import com.lhjz.portal.util.ThreadUtil;

/**
 * 
 * @author xi
 * 
 * @date 2015年3月28日 下午1:19:05
 * 
 */
@Controller
@RequestMapping("api/")
public class ApiController extends BaseController {

	static Logger logger = LoggerFactory.getLogger(ApiController.class);

	@Autowired
	ChannelRepository channelRepository;

	@Autowired
	ChatChannelRepository chatChannelRepository;

	@Autowired
	UserRepository userRepository;

	@Autowired
	MailSender2 mailSender;

	@Value("${tms.base.url}")
	private String baseUrl;

	@RequestMapping(value = "channel/jenkins/send", method = RequestMethod.POST)
	@ResponseBody
	public RespBody sendChannelJenkinsMsg(@RequestParam("channel") String channel,
			@RequestParam(value = "mail", required = false, defaultValue = "false") Boolean mail,
			@RequestParam(value = "raw", required = false, defaultValue = "false") Boolean raw,
			@RequestParam(value = "web", required = false) String web,
			@RequestBody String reqBody) {

		Channel channel2 = channelRepository.findOneByName(channel);
		if (channel2 == null) {
			return RespBody.failed("发送消息目的频道不存在!");
		}

		String name = JsonPath.read(reqBody, "$.name");
		String status = JsonPath.read(reqBody, "$.build.status");
		String phase = JsonPath.read(reqBody, "$.build.phase");
		String fullUrl = JsonPath.read(reqBody, "$.build.full_url");

		StringBuffer sb = new StringBuffer();
		sb.append("## Jenkins任务发版状态报告").append(SysConstant.NEW_LINE);
		sb.append("> **任务名称:** ").append(name).append(SysConstant.NEW_LINE);
		sb.append("> **任务URL:** ").append(fullUrl).append(SysConstant.NEW_LINE);
		sb.append("> **任务阶段:** ").append(phase).append(SysConstant.NEW_LINE);
		sb.append("> **任务状态:** ").append(status).append(SysConstant.NEW_LINE);

		if (StringUtil.isNotEmpty(web)) {
			sb.append("> ").append(SysConstant.NEW_LINE);
			sb.append(StringUtil.replace("> [点击此访问web服务]({?1})", web)).append(SysConstant.NEW_LINE);
		}

		if (raw) {
			sb.append(SysConstant.NEW_LINE);
			sb.append("---").append(SysConstant.NEW_LINE);
			sb.append("> **完整内容:** ").append(SysConstant.NEW_LINE);
			sb.append("```").append(SysConstant.NEW_LINE);
			sb.append(reqBody);
			sb.append("```").append(SysConstant.NEW_LINE);
		}

		ChatChannel chatChannel = new ChatChannel();
		chatChannel.setChannel(channel2);
		chatChannel.setContent(sb.toString());

		ChatChannel chatChannel2 = chatChannelRepository.saveAndFlush(chatChannel);

		final Mail mail2 = Mail.instance();

		if (mail) {
			channel2.getMembers().forEach(item -> mail2.addUsers(item));
		}

		mail2.addUsers(channel2.getSubscriber());

		if (!mail2.isEmpty()) {

			final User loginUser = getLoginUser();
			final String href = baseUrl + "/page/index.html#/chat/" + channel + "?id=" + chatChannel2.getId();

			final String html = StringUtil.md2Html(sb.toString());

			ThreadUtil.exec(() -> {

				try {
					Thread.sleep(3000);
					mailSender.sendHtml(
							String.format("TMS-Jenkins发版报告沟通频道@消息_%s", DateUtil.format(new Date(), DateUtil.FORMAT7)),
							TemplateUtil.process("templates/mail/mail-dynamic", MapUtil.objArr2Map("user", loginUser,
									"date", new Date(), "href", href, "title", "Jenkins发版报告频道消息有@到你", "content", html)),
							mail2.get());
					logger.info("沟通频道Jenins发版消息邮件发送成功！");
				} catch (Exception e) {
					e.printStackTrace();
					logger.error("沟通频道Jenins发版消息邮件发送失败！");
				}

			});
		}

		return RespBody.succeed();
	}
	
	@RequestMapping(value = "channel/send", method = RequestMethod.POST)
	@ResponseBody
	public RespBody sendChannelMsg(@RequestParam("channel") String channel,
			@RequestParam(value = "mail", required = false, defaultValue = "false") Boolean mail,
			@RequestParam(value = "web", required = false) String web,
			@RequestBody String reqBody) {

		Channel channel2 = channelRepository.findOneByName(channel);
		if (channel2 == null) {
			return RespBody.failed("发送消息目的频道不存在!");
		}

		StringBuffer sb = new StringBuffer();
		sb.append("## 来自第三方应用推送的消息").append(SysConstant.NEW_LINE);
		sb.append("> **消息内容:** ").append(SysConstant.NEW_LINE);
		sb.append("```").append(SysConstant.NEW_LINE);
		sb.append(reqBody);
		sb.append("```").append(SysConstant.NEW_LINE);

		if (StringUtil.isNotEmpty(web)) {
			sb.append("> ").append(SysConstant.NEW_LINE);
			sb.append(StringUtil.replace("> [点击此访问web服务]({?1})", web)).append(SysConstant.NEW_LINE);
		}

		ChatChannel chatChannel = new ChatChannel();
		chatChannel.setChannel(channel2);
		chatChannel.setContent(sb.toString());

		ChatChannel chatChannel2 = chatChannelRepository.saveAndFlush(chatChannel);

		final Mail mail2 = Mail.instance();

		if (mail) {
			channel2.getMembers().forEach(item -> mail2.addUsers(item));
		}

		mail2.addUsers(channel2.getSubscriber());

		if (!mail2.isEmpty()) {

			final User loginUser = getLoginUser();
			final String href = baseUrl + "/page/index.html#/chat/" + channel + "?id=" + chatChannel2.getId();

			final String html = StringUtil.md2Html(sb.toString());

			ThreadUtil.exec(() -> {

				try {
					Thread.sleep(3000);
					mailSender.sendHtml(
							String.format("TMS-来自第三方应用推送的@消息_%s", DateUtil.format(new Date(), DateUtil.FORMAT7)),
							TemplateUtil.process("templates/mail/mail-dynamic", MapUtil.objArr2Map("user", loginUser,
									"date", new Date(), "href", href, "title", "来自第三方应用推送的消息有@到你", "content", html)),
							mail2.get());
					logger.info("沟通频道来自第三方应用推送的消息邮件发送成功！");
				} catch (Exception e) {
					e.printStackTrace();
					logger.error("沟通频道来自第三方应用推送的消息邮件发送失败！");
				}

			});
		}

		return RespBody.succeed();
	}

}
