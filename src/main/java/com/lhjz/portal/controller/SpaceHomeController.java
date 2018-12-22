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
import org.springframework.data.domain.PageImpl;
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

import com.google.common.collect.Lists;
import com.lhjz.portal.base.BaseController;
import com.lhjz.portal.component.MailSender;
import com.lhjz.portal.entity.Blog;
import com.lhjz.portal.entity.Space;
import com.lhjz.portal.model.BlogInfo;
import com.lhjz.portal.model.RespBody;
import com.lhjz.portal.pojo.Enum.Status;
import com.lhjz.portal.repository.BlogRepository;
import com.lhjz.portal.repository.CommentRepository;
import com.lhjz.portal.repository.LogRepository;
import com.lhjz.portal.repository.SpaceRepository;
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
@RequestMapping("free/space/home")
public class SpaceHomeController extends BaseController {

	static Logger logger = LoggerFactory.getLogger(SpaceHomeController.class);

	@Autowired
	BlogRepository blogRepository;

	@Autowired
	SpaceRepository spaceRepository;

	@Autowired
	UserRepository userRepository;

	@Autowired
	CommentRepository commentRepository;

	@Autowired
	LogRepository logRepository;

	@Autowired
	MailSender mailSender;

	@GetMapping("{sid}/blogs")
	public RespBody blogsBySpace(@PathVariable(value = "sid") String sid,
			@PageableDefault(sort = { "id" }, direction = Direction.DESC) Pageable pageable) {

		Space space = spaceRepository.findOneByNameAndStatusNot(sid, Status.Deleted);

		if (space == null) {
			return RespBody.failed("空间不存或者权限不足！");
		}

		Page<Blog> blogs = blogRepository.findByStatusNotAndOpenedTrueAndSpace(Status.Deleted, space, pageable);

		blogs.forEach(b -> {
			b.setBlogAuthorities(null);
			Space space2 = b.getSpace();
			if (space2 != null) {
				Space spaceN = new Space();
				spaceN.setId(space2.getId());
				spaceN.setName(space2.getName());
				b.setSpace(spaceN);
			}

			b.getTags().forEach(tag -> {
				tag.setCreator(null);
				tag.setUpdater(null);
			});
		});

		return RespBody.succeed(blogs);
	}

	@GetMapping("{sid}/blog/list")
	public RespBody listBlog(@PathVariable(value = "sid") String sid,
			@PageableDefault(sort = { "id" }, direction = Direction.DESC) Pageable pageable) {

		Space space = spaceRepository.findOneByNameAndStatusNot(sid, Status.Deleted);

		if (space == null) {
			return RespBody.failed("空间不存或者权限不足！");
		}

		Page<Blog> blogs = blogRepository.findByStatusNotAndOpenedTrueAndSpace(Status.Deleted, space, pageable);

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

		Blog pre = blogRepository.findTopByStatusNotAndOpenedTrueAndSpacAndIdLessThanOrderByIdDesc(Status.Deleted,
				blog.getSpace(), id);
		Blog next = blogRepository.findTopByStatusNotAndOpenedTrueAndSpacAndIdGreaterThanOrderByIdAsc(Status.Deleted,
				blog.getSpace(), id);

		// 博文阅读次数+1
		Long readCnt = blog.getReadCnt();
		readCnt = readCnt == null ? 1L : (readCnt + 1);

		blogRepository.updateReadCnt(readCnt, id);

		blog.setReadCnt(readCnt);

		blog.setBlogAuthorities(null);

		return RespBody.succeed(new BlogInfo(blog, pre, next));
	}

	@GetMapping("{sid}/blog/search")
	public RespBody searchBlog(@PathVariable(value = "sid") String sid, @RequestParam("search") String search,
			@RequestParam(value = "ellipsis", defaultValue = "60") Integer ellipsis,
			@SortDefault(value = "id", direction = Direction.DESC) Sort sort) {

		if (StringUtil.isEmpty(search)) {
			return RespBody.failed("检索条件不能为空!");
		}

		Space space = spaceRepository.findOneByNameAndStatusNot(sid, Status.Deleted);

		if (space == null) {
			return RespBody.failed("空间不存或者权限不足！");
		}

		List<Blog> blogs = blogRepository
				.findByStatusNotAndSpaceAndTitleContainingAndOpenedTrueOrStatusNotAndContentContainingAndOpenedTrue(
						Status.Deleted, space, search, Status.Deleted, search, sort)
				.stream().peek(b -> {
					b.setContent(StringUtil.limitLength(b.getContent(), ellipsis));
					b.setBlogAuthorities(null);
				}).collect(Collectors.toList());

		return RespBody.succeed(blogs);
	}

	@GetMapping("{sid}/blog/page/search")
	public RespBody searchBlogByPage(@PathVariable(value = "sid") String sid, @RequestParam("search") String search,
			@PageableDefault(sort = { "id" }, direction = Direction.DESC) Pageable pageable) {

		if (StringUtil.isEmpty(search.trim())) {
			return listBlog(sid, pageable);
		}

		Space space = spaceRepository.findOneByNameAndStatusNot(sid, Status.Deleted);

		if (space == null) {
			return RespBody.failed("空间不存或者权限不足！");
		}

		Page<Blog> blogs = new PageImpl<>(Lists.newArrayList());

		// 按标签检索
		if (search.toLowerCase().startsWith("title:")) {
			String[] arr = search.split(":", 2);
			if (StringUtil.isNotEmpty(arr[1].trim())) {
				blogs = blogRepository.findByStatusNotAndSpaceAndTitleContainingAndOpenedTrue(Status.Deleted, space,
						arr[1], pageable);
			}
		} else if (search.toLowerCase().startsWith("content:")) {
			String[] arr = search.split(":", 2);
			if (StringUtil.isNotEmpty(arr[1].trim())) {
				blogs = blogRepository.findByStatusNotAndSpaceAndContentContainingAndOpenedTrue(Status.Deleted, space,
						arr[1], pageable);
			}
		} else {
			blogs = blogRepository
					.findByStatusNotAndSpaceAndTitleContainingAndOpenedTrueOrStatusNotAndContentContainingAndOpenedTrue(
							Status.Deleted, space, search, Status.Deleted, search, pageable);
		}

		blogs.forEach(b -> {
			b.setBlogAuthorities(null);
			b.setContent(null);
		});

		return RespBody.succeed(blogs);

	}

}
