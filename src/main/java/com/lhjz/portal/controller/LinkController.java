/**
 * 版权所有 (TMS)
 */
package com.lhjz.portal.controller;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.lhjz.portal.base.BaseController;
import com.lhjz.portal.entity.Link;
import com.lhjz.portal.model.RespBody;
import com.lhjz.portal.pojo.Enum.LinkType;
import com.lhjz.portal.pojo.Enum.Status;
import com.lhjz.portal.repository.LinkRepository;
import com.lhjz.portal.util.StringUtil;

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

		return RespBody.succeed(link2);
	}

	@RequestMapping(value = "listBy", method = RequestMethod.GET)
	@ResponseBody
	public RespBody listBy(@RequestParam("channelId") Long channelId) {

		List<Link> links = linkRepository.findByChannelIdAndStatus(channelId, Status.New);

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

		link.setHref(href);
		link.setTitle(title);

		Link link2 = linkRepository.saveAndFlush(link);

		return RespBody.succeed(link2);
	}

	@RequestMapping(value = "delete", method = RequestMethod.POST)
	@ResponseBody
	public RespBody delete(@RequestParam("id") Long id) {

		linkRepository.delete(id);

		return RespBody.succeed(id);
	}

}
