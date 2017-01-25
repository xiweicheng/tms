/**
 * 版权所有 (TMS)
 */
package com.lhjz.portal.controller;

import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.lhjz.portal.base.BaseController;
import com.lhjz.portal.model.RespBody;

/**
 * 
 * @author xi
 * 
 * @date 2015年3月28日 下午1:19:05
 * 
 */
@Controller
@RequestMapping("oauth2/resource")
public class Oauth2ResourceController extends BaseController {

	static final Logger logger = LoggerFactory.getLogger(Oauth2ResourceController.class);

	@RequestMapping("loginUser")
	@ResponseBody
	public RespBody loginUser() {
		
		return RespBody.succeed(getLoginUser());
	}
	
	@RequestMapping("now")
	@ResponseBody
	public RespBody now() {
		
		return RespBody.succeed(new Date());
	}

}
