/**
 * 版权所有 (TMS)
 */
package com.lhjz.portal.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.lhjz.portal.base.BaseController;
import com.lhjz.portal.entity.security.User;
import com.lhjz.portal.model.RespBody;
import com.lhjz.portal.repository.ChatLabelRepository;
import com.lhjz.portal.util.WebUtil;

/**
 * 
 * @author xi
 * 
 * @date 2015年3月28日 下午1:19:05
 * 
 */
@Controller
@RequestMapping("admin/chat/label")
public class ChatLabelController extends BaseController {

	static Logger logger = LoggerFactory.getLogger(ChatLabelController.class);

	@Autowired
	ChatLabelRepository chatLabelRepository;

	@PostMapping("delete")
	@ResponseBody
	public RespBody delete(@RequestParam("name") String name) {

		int val = chatLabelRepository.markChatLabelAsDeleted(new User(WebUtil.getUsername()), name);

		return RespBody.succeed(val);

	}

}
