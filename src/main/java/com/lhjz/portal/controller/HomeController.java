/**
 * 版权所有 (TMS)
 */
package com.lhjz.portal.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.lhjz.portal.base.BaseController;
import com.lhjz.portal.component.MailSender;
import com.lhjz.portal.entity.Blog;
import com.lhjz.portal.model.RespBody;
import com.lhjz.portal.pojo.Enum.Status;
import com.lhjz.portal.repository.BlogRepository;
import com.lhjz.portal.repository.CommentRepository;
import com.lhjz.portal.repository.LogRepository;
import com.lhjz.portal.repository.UserRepository;

/**
 * 
 * @author xi
 * 
 * @date 2015年3月28日 下午1:19:05
 * 
 */
@RestController
@RequestMapping("home")
public class HomeController extends BaseController {

	static Logger logger = LoggerFactory.getLogger(HomeController.class);

	@Autowired
	BlogRepository blogRepository;

	@Autowired
	UserRepository userRepository;

	@Autowired
	CommentRepository commentRepository;

	@Autowired
	LogRepository logRepository;

	@Autowired
	MailSender mailSender;

	@GetMapping("blogs")
	public RespBody blogs(@PageableDefault(sort = { "id" }, direction = Direction.DESC) Pageable pageable) {

		Page<Blog> blogs = blogRepository.findByStatusNotAndOpenedTrue(Status.Deleted, pageable);

		blogs.forEach(b -> b.setBlogAuthorities(null));

		return RespBody.succeed(blogs);
	}

}
