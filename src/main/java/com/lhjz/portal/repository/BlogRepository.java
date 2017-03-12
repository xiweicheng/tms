/**
 * 版权所有 (TMS)
 */
package com.lhjz.portal.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.lhjz.portal.entity.Blog;
import com.lhjz.portal.entity.ChatChannel;
import com.lhjz.portal.pojo.Enum.Status;

/**
 * 
 * @author xi
 * 
 * @date 2015年3月28日 下午2:09:06
 * 
 */
public interface BlogRepository extends JpaRepository<Blog, Long> {

	Page<ChatChannel> findByStatusNot(Status status, Pageable pageable);

	Page<ChatChannel> findByTitleContainingOrContentContaining(String searchT, String searchC, Pageable pageable);
}
