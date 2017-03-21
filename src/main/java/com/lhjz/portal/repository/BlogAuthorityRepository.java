/**
 * 版权所有 (TMS)
 */
package com.lhjz.portal.repository;

import java.util.Collection;

import javax.transaction.Transactional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import com.lhjz.portal.entity.Blog;
import com.lhjz.portal.entity.BlogAuthority;
import com.lhjz.portal.entity.Channel;
import com.lhjz.portal.entity.security.User;

/**
 * 
 * @author xi
 * 
 * @date 2015年3月28日 下午2:09:06
 * 
 */
public interface BlogAuthorityRepository extends JpaRepository<BlogAuthority, Long> {

	@Modifying
	@Transactional
	@Query("delete from BlogAuthority ba where ba.blog=?1 and (ba.channel in ?2 or ba.user in ?3)")
	void removeAuths(Blog blog, Collection<Channel> channels, Collection<User> users);
	
	@Modifying
	@Transactional
	@Query("delete from BlogAuthority ba where ba.blog=?1 and (ba.channel in ?2)")
	void removeChannelAuths(Blog blog, Collection<Channel> channels);
	
	@Modifying
	@Transactional
	@Query("delete from BlogAuthority ba where ba.blog=?1 and (ba.user in ?2)")
	void removeUserAuths(Blog blog, Collection<User> users);
}
