/**
 * 版权所有 (TMS)
 */
package com.lhjz.portal.controller;

import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.web.SortDefault;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.lhjz.portal.base.BaseController;
import com.lhjz.portal.component.MailSender;
import com.lhjz.portal.entity.Blog;
import com.lhjz.portal.entity.Comment;
import com.lhjz.portal.entity.Space;
import com.lhjz.portal.model.BlogInfo;
import com.lhjz.portal.model.RespBody;
import com.lhjz.portal.pojo.Enum.Status;
import com.lhjz.portal.repository.BlogRepository;
import com.lhjz.portal.repository.CommentRepository;
import com.lhjz.portal.repository.LogRepository;
import com.lhjz.portal.repository.UserRepository;
import com.lhjz.portal.util.StringUtil;

/**
 * 
 * @author xi
 * 
 * @date 2015年3月28日 下午1:19:05
 * 
 */
@RestController
@RequestMapping("free/home")
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

		blogs.forEach(b -> {
			b.setBlogAuthorities(null);
			Space space = b.getSpace();
			if (space != null) {
				Space spaceN = new Space();
				spaceN.setId(space.getId());
				spaceN.setName(space.getName());
				b.setSpace(spaceN);
			}

			b.getTags().forEach(tag -> {
				tag.setCreator(null);
				tag.setUpdater(null);
			});
		});

		return RespBody.succeed(blogs);
	}

	@GetMapping("blog/list")
	public RespBody listBlog(@PageableDefault(sort = { "id" }, direction = Direction.DESC) Pageable pageable) {

		Page<Blog> blogs = blogRepository.findByStatusNotAndOpenedTrue(Status.Deleted, pageable);

		blogs.forEach(b -> {
			b.setBlogAuthorities(null);
			b.setContent(null);
		});

		return RespBody.succeed(blogs);
	}

	@GetMapping("blog/{id}")
	public RespBody getBlog(@PathVariable("id") Long id) {

		Blog blog = blogRepository.findOne(id);

		if (blog == null || Status.Deleted.equals(blog.getStatus()) || !Boolean.TRUE.equals(blog.getOpened())) {
			return RespBody.failed("博文不存在或者权限不足!");
		}

		Blog pre = blogRepository.findTopByStatusNotAndOpenedTrueAndIdLessThanOrderByIdDesc(Status.Deleted, id);
		Blog next = blogRepository.findTopByStatusNotAndOpenedTrueAndIdGreaterThanOrderByIdAsc(Status.Deleted, id);

		// 博文阅读次数+1
		Long readCnt = blog.getReadCnt();
		readCnt = readCnt == null ? 1L : (readCnt + 1);
		
		blogRepository.updateReadCnt(readCnt, id);

		blog.setReadCnt(readCnt);
		
		blog.setBlogAuthorities(null);
		
		return RespBody.succeed(new BlogInfo(blog, pre, next));
	}

	@GetMapping("blog/search")
	public RespBody searchBlog(@RequestParam("search") String search,
			@RequestParam(value = "ellipsis", defaultValue = "60") Integer ellipsis,
			@SortDefault(value = "id", direction = Direction.DESC) Sort sort) {

		if (StringUtil.isEmpty(search)) {
			return RespBody.failed("检索条件不能为空!");
		}

		List<Blog> blogs = blogRepository
				.findByStatusNotAndTitleContainingAndOpenedTrueOrStatusNotAndContentContainingAndOpenedTrue(
						Status.Deleted, search, Status.Deleted, search, sort)
				.stream().peek(b -> {
					b.setContent(StringUtil.limitLength(b.getContent(), ellipsis));
					b.setBlogAuthorities(null);
				}).collect(Collectors.toList());

		return RespBody.succeed(blogs);
	}

	@GetMapping("blog/{id}/comments")
	public RespBody listBlogComments(@PathVariable("id") Long id,
			@PageableDefault(sort = { "id" }, direction = Direction.ASC) Pageable pageable) {

		Blog blog = blogRepository.findOne(id);

		if (Status.Deleted.equals(blog.getStatus()) || !Boolean.TRUE.equals(blog.getOpened())) {
			return RespBody.failed("博文不存在或者权限不足!");
		}

		Page<Comment> page = commentRepository.findByTargetIdAndStatusNot(String.valueOf(id), Status.Deleted, pageable);

		return RespBody.succeed(page);

	}
}
