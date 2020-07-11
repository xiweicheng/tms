/**
 * 版权所有 (TMS)
 */
package com.lhjz.portal.service.impl;

import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.lhjz.portal.entity.Blog;
import com.lhjz.portal.repository.BlogRepository;
import com.lhjz.portal.repository.UserRepository;
import com.lhjz.portal.service.BlogLockService;

import lombok.extern.slf4j.Slf4j;

/**
 * @author xi
 *
 */
@Slf4j
@Service
//@Transactional
public class BlogLockServiceImpl implements BlogLockService {

	@Autowired
	UserRepository userRepository;

	@Autowired
	BlogRepository blogRepository;

	@Override
	public Boolean lockBy(String username, Long blogId) {

		Blog blog = blogRepository.findOne(blogId);

		if (blog == null) {
			return false;
		}

		if (blog.getLocker() != null) {
			if (!blog.getLocker().getUsername().equals(username)) { // 已经被别人编辑中
				// TODO 考虑如果编辑占用锁时间过长，强制抢占过来
				return false;
			} else { // 本身就是自己在编辑中，更新锁定时间
				blog.setLockDate(new Date());
			}
		} else {
			blog.setLocker(userRepository.findOne(username));
			blog.setLockDate(new Date());
		}

		blogRepository.updateLock(blog.getLocker(), blog.getLockDate(), blogId);

		return true;
	}

	@Override
	public Boolean unlockBy(String username, Long blogId) {

		log.info("unlockBy: {} {}", username, blogId);

		Blog blog = blogRepository.findOne(blogId);

		if (blog == null) {
			return false;
		}

		if (blog.getLocker() == null) {
			return true;
		}

		if (blog.getLocker().getUsername().equals(username)) {
			blog.setLocker(null);
			blog.setLockDate(null);

			blogRepository.updateLock(blog.getLocker(), blog.getLockDate(), blogId);

			return true;
		} else {
			return false;
		}
	}

	@Override
	public Boolean isLock(Long blogId) {

		Blog blog = blogRepository.findOne(blogId);

		return blog != null && blog.getLocker() != null;
	}

	@Override
	public Boolean isLockBy(String username, Long blogId) {

		Blog blog = blogRepository.findOne(blogId);

		return blog != null && blog.getLocker() != null && blog.getLocker().getUsername().equals(username);
	}

	@Override
	public Boolean lockForce(String username, Long blogId) {
		Blog blog = blogRepository.findOne(blogId);

		if (blog == null) {
			return false;
		}

		blog.setLocker(userRepository.findOne(username));
		blog.setLockDate(new Date());

		blogRepository.updateLock(blog.getLocker(), blog.getLockDate(), blogId);

		return true;
	}

	@Override
	public Boolean unlockForce(Long blogId) {
		Blog blog = blogRepository.findOne(blogId);

		if (blog == null) {
			return false;
		}

		if (blog.getLocker() == null) {
			return true;
		}

		blog.setLocker(null);
		blog.setLockDate(null);

		blogRepository.updateLock(blog.getLocker(), blog.getLockDate(), blogId);

		return true;
	}

}
