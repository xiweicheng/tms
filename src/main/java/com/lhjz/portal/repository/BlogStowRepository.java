/**
 * 版权所有 (TMS)
 */
package com.lhjz.portal.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.lhjz.portal.entity.Blog;
import com.lhjz.portal.entity.BlogStow;
import com.lhjz.portal.entity.security.User;
import com.lhjz.portal.pojo.Enum.Status;

/**
 * 
 * @author xi
 * 
 * @date 2015年3月28日 下午2:09:06
 * 
 */
public interface BlogStowRepository extends JpaRepository<BlogStow, Long> {

	BlogStow findOneByBlogAndCreator(Blog blog, User user);
	
	BlogStow findOneByBlogAndCreatorAndStatusNot(Blog blog, User user, Status stats);

	List<BlogStow> findByCreatorAndStatusNot(User user, Status status);
	
	List<BlogStow> findByBlogAndStatusNot(Blog blog, Status status);

}
