/**
 * 版权所有 (TMS)
 */
package com.lhjz.portal.controller;

import com.jayway.jsonpath.JsonPath;
import com.lhjz.portal.base.BaseController;
import com.lhjz.portal.component.MailSender;
import com.lhjz.portal.constant.SysConstant;
import com.lhjz.portal.entity.Channel;
import com.lhjz.portal.entity.ChatChannel;
import com.lhjz.portal.entity.security.User;
import com.lhjz.portal.model.Mail;
import com.lhjz.portal.model.RespBody;
import com.lhjz.portal.pojo.Enum.JenkinsStatus;
import com.lhjz.portal.repository.ChannelRepository;
import com.lhjz.portal.service.ChatChannelService;
import com.lhjz.portal.util.DateUtil;
import com.lhjz.portal.util.MapUtil;
import com.lhjz.portal.util.StringUtil;
import com.lhjz.portal.util.TemplateUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.Date;

/**
 * @author xi
 * @date 2015年3月28日 下午1:19:05
 *
 * ApiController类是一个控制器，负责处理API请求，继承自BaseController
 * 提供了频道消息发送功能，包括Jenkins消息和第三方应用消息
 */
@Controller
@RequestMapping("api/")
public class ApiController extends BaseController {

    // 使用Logger记录日志
    static Logger logger = LoggerFactory.getLogger(ApiController.class);

    // 自动注入频道仓库，用于数据库操作
    @Autowired
    ChannelRepository channelRepository;

    // 自动注入聊天频道服务，处理频道相关业务逻辑
    @Autowired
    ChatChannelService chatChannelService;

    // 自动注入邮件发送器，用于发送邮件通知
    @Autowired
    MailSender mailSender;

    // 从配置文件中获取基础URL
    @Value("${tms.base.url}")
    private String baseUrl;

    /**
     * 处理Jenkins消息发送请求
     * @param channel 频道名称
     * @param mail 是否发送邮件通知
     * @param raw 是否显示原始消息内容
     * @param debug 是否开启调试模式
     * @param web Web服务URL
     * @param reqBody 请求体内容
     * @return 返回响应结果
     */
    @PostMapping("channel/jenkins/send")
    @ResponseBody
    public RespBody sendChannelJenkinsMsg(@RequestParam("channel") String channel,
                                          @RequestParam(value = "mail", required = false, defaultValue = "false") Boolean mail,
                                          @RequestParam(value = "raw", required = false, defaultValue = "false") Boolean raw,
                                          @RequestParam(value = "debug", required = false, defaultValue = "false") Boolean debug,
                                          @RequestParam(value = "web", required = false) String web, @RequestBody String reqBody) {

        // 如果是原始模式或调试模式，记录请求体信息
        if (raw || debug) {
            logger.info("sendChannelJenkinsMsg: {}", reqBody);
        }

        // 根据频道名称查找频道
        Channel channel2 = channelRepository.findOneByName(channel);
        if (channel2 == null) {
            return RespBody.failed("发送消息目的频道不存在!");
        }

        // 从请求体中解析Jenkins消息的各项信息
        String name = JsonPath.read(reqBody, "$.name");
        String status = JsonPath.read(reqBody, "$.build.status");
        String phase = JsonPath.read(reqBody, "$.build.phase");
        long timestamp = JsonPath.read(reqBody, "$.build.timestamp");
        String fullUrl;
        try {
            fullUrl = JsonPath.read(reqBody, "$.build.full_url");
        } catch (Exception e) {
            fullUrl = JsonPath.read(reqBody, "$.build.url");
        }

        // 构建消息内容
        StringBuilder sb = new StringBuilder();
        sb.append("## Jenkins任务发版状态报告").append(SysConstant.NEW_LINE);
        sb.append("> **任务名称:** ").append(name).append(SysConstant.NEW_LINE);
        sb.append("> **任务URL:** ").append(fullUrl).append(SysConstant.NEW_LINE);
        sb.append("> **任务阶段:** ").append(phase).append(SysConstant.NEW_LINE);
        sb.append("> **任务时间:** ").append(DateUtil.format(new Date(timestamp), DateUtil.FORMAT1))
                .append(SysConstant.NEW_LINE);
        // 根据任务状态选择对应的图标
        JenkinsStatus sts = JenkinsStatus.valueOf(status);
        String icon = sts.equals(JenkinsStatus.SUCCESS) ? "<i class=\"large green check circle icon\"></i>"
                : "<i class=\"large red remove circle icon\"></i>";
        sb.append("> **任务状态:** ").append(icon).append(sts).append(SysConstant.NEW_LINE);

        // 如果提供了Web服务URL，添加到消息中
        if (StringUtil.isNotEmpty(web)) {
            sb.append("> ").append(SysConstant.NEW_LINE);
            sb.append(StringUtil.replace("> [点击此访问web服务]({?1})", web)).append(SysConstant.NEW_LINE);
        }

        // 如果是原始模式，添加完整消息内容
        if (Boolean.TRUE.equals(raw)) {
            sb.append(SysConstant.NEW_LINE);
            sb.append("---").append(SysConstant.NEW_LINE);
            sb.append("> **完整内容:** ").append(SysConstant.NEW_LINE);
            sb.append("```").append(SysConstant.NEW_LINE);
            sb.append(reqBody);
            sb.append("```").append(SysConstant.NEW_LINE);
        }

        // 创建并保存聊天频道消息
        ChatChannel chatChannel = new ChatChannel();
        chatChannel.setChannel(channel2);
        chatChannel.setContent(sb.toString());

        ChatChannel chatChannel2 = chatChannelService.save(chatChannel);

        // 创建邮件实例
        final Mail mail2 = Mail.instance();

        // 如果需要发送邮件，添加频道成员
        if (Boolean.TRUE.equals(mail)) {
            channel2.getMembers().forEach(mail2::addUsers);
        }

        // 添加频道订阅者和当前登录用户
        mail2.addUsers(channel2.getSubscriber(), getLoginUser());

        // 如果邮件不为空，发送邮件通知
        if (!mail2.isEmpty()) {

            final User loginUser = getLoginUser();
            final String href = baseUrl + "/page/index.html#/chat/" + channel + "?id=" + chatChannel2.getId();

            final String html = StringUtil.md2Html(sb.toString(), true, true);

            try {
                mailSender.sendHtmlByQueue(
                        String.format("TMS-Jenkins发版报告沟通频道@消息_%s", DateUtil.format(new Date(), DateUtil.FORMAT7)),
                        TemplateUtil.process("templates/mail/mail-dynamic",
                                MapUtil.objArr2Map("user", loginUser, "date", new Date(), "href", href, "title",
                                        "Jenkins发版报告频道消息有@到你", "content", html)),
                        getLoginUserName(loginUser), mail2.get());
            } catch (Exception e) {
                e.printStackTrace();
            }

        }

        return RespBody.succeed();
    }

    @PostMapping("channel/send")
    @ResponseBody
    public RespBody sendChannelMsg(@RequestParam("channel") String channel,
                                   @RequestParam(value = "mail", required = false, defaultValue = "false") Boolean mail,
                                   @RequestParam(value = "web", required = false) String web, @RequestBody String reqBody) {

        Channel channel2 = channelRepository.findOneByName(channel);
        if (channel2 == null) {
            return RespBody.failed("发送消息目的频道不存在!");
        }

        StringBuilder sb = new StringBuilder();
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

        ChatChannel chatChannel2 = chatChannelService.save(chatChannel);

        final Mail mail2 = Mail.instance();

        if (Boolean.TRUE.equals(mail)) {
            channel2.getMembers().forEach(mail2::addUsers);
        }

        mail2.addUsers(channel2.getSubscriber(), getLoginUser());

        if (!mail2.isEmpty()) {

            final User loginUser = getLoginUser();
            final String href = baseUrl + "/page/index.html#/chat/" + channel + "?id=" + chatChannel2.getId();

            final String html = StringUtil.md2Html(sb.toString(), true, true);

            try {
                mailSender.sendHtmlByQueue(
                        String.format("TMS-来自第三方应用推送的@消息_%s", DateUtil.format(new Date(), DateUtil.FORMAT7)),
                        TemplateUtil.process("templates/mail/mail-dynamic",
                                MapUtil.objArr2Map("user", loginUser, "date", new Date(), "href", href, "title",
                                        "来自第三方应用推送的消息有@到你", "content", html)),
                        getLoginUserName(loginUser), mail2.get());
            } catch (Exception e) {
                e.printStackTrace();
            }

        }

        return RespBody.succeed();
    }

}
