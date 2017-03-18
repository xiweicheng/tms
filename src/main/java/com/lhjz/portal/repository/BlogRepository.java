/**
 * 版权所有 (TMS)
 */
package com.lhjz.portal.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.lhjz.portal.entity.Blog;
import com.lhjz.portal.entity.security.User;
import com.lhjz.portal.pojo.Enum.Status;

/**
 * 
 * @author xi
 * 
 * @date 2015年3月28日 下午2:09:06
 * 
 */
public interface BlogRepository extends JpaRepository<Blog, Long> {

	Page<Blog> findByStatusNot(Status status, Pageable pageable);

	Page<Blog> findByTitleContainingOrContentContaining(String searchT, String searchC, Pageable pageable);
	
	Page<Blog> findByStatusNotAndCreatorOrStatusNotAndPrivatedFalse(Status status, User creator, Status status2, Pageable pageable);
}
