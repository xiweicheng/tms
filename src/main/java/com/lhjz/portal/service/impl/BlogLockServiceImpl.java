/**
 * 版权所有 (TMS)
 */
package com.lhjz.portal.service.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Service;

import com.lhjz.portal.constant.SysConstant;
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

	@Autowired
	CacheManager cacheManager;

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

	@Override
	public Boolean isRealLock(Long blogId) {

		Blog blog = blogRepository.findOne(blogId);

		if (blog == null) {
			return false;
		}

		if (blog.getLocker() == null) {
			return false;
		}

		// 记录的编辑锁定者没有真的实时在线，自动释放锁
		if (!wsLockOnline(blog.getLocker().getUsername(), String.valueOf(blogId))) {

			blogRepository.updateLock(null, null, blogId);

			return false;
		} else {
			return true;
		}

	}

	private boolean wsLockOnline(String username, String blogId) {

		final List<Object> res = new ArrayList<>();

		try {
			@SuppressWarnings("unchecked")
			ConcurrentHashMap<Object, Object> cache = (ConcurrentHashMap<Object, Object>) cacheManager
					.getCache(SysConstant.LOCK_BLOGS).getNativeCache();

			cache.forEachKey(1, key -> {
				String un = String.valueOf(key).split("@")[0];
				String bid = String.valueOf(key).split("@")[1];
				if (un.equals(username) && bid.equals(blogId)) {
					res.add(cache.get(key));
				}
			});
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}

		return res.size() > 0;
	}

}
