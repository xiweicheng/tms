/**
 * 版权所有 (TMS)
 */
package com.lhjz.portal.controller;

import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.lhjz.portal.base.BaseController;
import com.lhjz.portal.entity.Channel;
import com.lhjz.portal.entity.ChatChannel;
import com.lhjz.portal.entity.Link;
import com.lhjz.portal.model.RespBody;
import com.lhjz.portal.pojo.Enum.LinkType;
import com.lhjz.portal.pojo.Enum.Status;
import com.lhjz.portal.repository.ChannelRepository;
import com.lhjz.portal.repository.ChatChannelRepository;
import com.lhjz.portal.repository.LinkRepository;
import com.lhjz.portal.util.AuthUtil;
import com.lhjz.portal.util.StringUtil;
import com.lhjz.portal.util.WebUtil;

/**
 * 
 * @author xi
 * 
 * @date 2015年3月28日 下午1:19:05
 * 
 */
@Controller
@RequestMapping("admin/link")
public class LinkController extends BaseController {

	static Logger logger = LoggerFactory.getLogger(LinkController.class);

	@Autowired
	LinkRepository linkRepository;

	@Autowired
	ChatChannelRepository chatChannelRepository;

	@Autowired
	ChannelRepository channelRepository;

	@RequestMapping(value = "create", method = RequestMethod.POST)
	@ResponseBody
	public RespBody create(@RequestParam("href") String href, @RequestParam("title") String title,
			@RequestParam(value = "channelId", required = false) Long channelId,
			@RequestParam(value = "type", defaultValue = "Channel") String type) {

		if (StringUtil.isEmpty(href)) {
			return RespBody.failed("链接地址不能为空!");
		}

		if (StringUtil.isEmpty(title)) {
			return RespBody.failed("标题名称不能为空!");
		}

		Link link = new Link();
		link.setHref(href);
		link.setTitle(title);
		link.setChannelId(channelId);
		link.setType(LinkType.valueOf(type));

		Link link2 = linkRepository.saveAndFlush(link);

		if (channelId != null) {
			Channel channel = channelRepository.findOne(channelId);
			if (channel != null) {
				ChatChannel chatChannel = new ChatChannel();
				chatChannel.setChannel(channel);
				chatChannel.setContent(StringUtil.replace("## ~频道消息播报~\n\n> {~{?1}} 添加了频道外链: [**{?2}**]({?3})\n\n",
						WebUtil.getUsername(), title, href));

				chatChannelRepository.saveAndFlush(chatChannel);
			}
		}

		return RespBody.succeed(link2);
	}

	@RequestMapping(value = "listBy", method = RequestMethod.GET)
	@ResponseBody
	public RespBody listBy(@RequestParam("channelId") Long channelId) {

		List<Link> links = linkRepository.findByChannelIdAndStatus(channelId, Status.New);

		return RespBody.succeed(links);
	}

	@GetMapping("listByType")
	@ResponseBody
	public RespBody listByType(@RequestParam("type") String type) {

		List<Link> links = linkRepository.findByTypeAndStatus(LinkType.valueOf(type), Status.New);

		links = links.stream().filter(l -> AuthUtil.isChannelMember(channelRepository.findOne(l.getChannelId())))
				.collect(Collectors.toList());

		return RespBody.succeed(links);
	}

	@RequestMapping(value = "listByApp", method = RequestMethod.GET)
	@ResponseBody
	public RespBody listByApp() {

		List<Link> links = linkRepository.findByTypeAndStatus(LinkType.App, Status.New);

		return RespBody.succeed(links);
	}

	@RequestMapping(value = "get", method = RequestMethod.GET)
	@ResponseBody
	public RespBody get(@RequestParam("id") Long id) {

		return RespBody.succeed(linkRepository.findOne(id));
	}

	@RequestMapping(value = "update", method = RequestMethod.POST)
	@ResponseBody
	public RespBody update(@RequestParam("href") String href, @RequestParam("title") String title,
			@RequestParam("id") Long id) {

		if (StringUtil.isEmpty(href)) {
			return RespBody.failed("链接地址不能为空!");
		}

		if (StringUtil.isEmpty(title)) {
			return RespBody.failed("标题名称不能为空!");
		}

		Link link = linkRepository.findOne(id);

		String hrefOld = link.getHref();
		String titleOld = link.getTitle();

		link.setHref(href);
		link.setTitle(title);

		Link link2 = linkRepository.saveAndFlush(link);

		Long channelId = link2.getChannelId();

		if (channelId != null) {
			Channel channel = channelRepository.findOne(channelId);
			if (channel != null) {
				ChatChannel chatChannel = new ChatChannel();
				chatChannel.setChannel(channel);
				chatChannel.setContent(
						StringUtil.replace("## ~频道消息播报~\n\n> {~{?1}} 将频道外链 [**{?2}**]({?3}) 更新为 [**{?4}**]({?5})\n\n",
								WebUtil.getUsername(), titleOld, hrefOld, title, href));

				chatChannelRepository.saveAndFlush(chatChannel);
			}
		}

		return RespBody.succeed(link2);
	}

	@RequestMapping(value = "delete", method = RequestMethod.POST)
	@ResponseBody
	public RespBody delete(@RequestParam("id") Long id) {

		Link link = linkRepository.findOne(id);

		if (link == null) {
			return RespBody.failed("删除频道外链不存在!");
		}

		linkRepository.delete(id);

		Long channelId = link.getChannelId();
		if (channelId != null) {

			Channel channel = channelRepository.findOne(channelId);
			if (channel != null) {
				ChatChannel chatChannel = new ChatChannel();
				chatChannel.setChannel(channel);
				chatChannel.setContent(StringUtil.replace("## ~频道消息播报~\n\n> {~{?1}} 删除了频道外链: [**{?2}**]({?3})\n\n",
						WebUtil.getUsername(), link.getTitle(), link.getHref()));

				chatChannelRepository.saveAndFlush(chatChannel);
			}
		}

		return RespBody.succeed(id);
	}

	@PostMapping("count/inc")
	@ResponseBody
	public RespBody incCount(@RequestParam("id") Long id) {

		linkRepository.incCount(id);

		return RespBody.succeed(id);
	}

}
