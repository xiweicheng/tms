/**
 * 版权所有 (TMS)
 */
package com.lhjz.portal.repository;

import java.util.Collection;

import javax.transaction.Transactional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import com.lhjz.portal.entity.Channel;
import com.lhjz.portal.entity.Space;
import com.lhjz.portal.entity.SpaceAuthority;
import com.lhjz.portal.entity.security.User;

/**
 * 
 * @author xi
 * 
 * @date 2015年3月28日 下午2:09:06
 * 
 */
public interface SpaceAuthorityRepository extends JpaRepository<SpaceAuthority, Long> {
	
	@Modifying
	@Transactional
	@Query("delete from SpaceAuthority sa where sa.space=?1 and (sa.channel in ?2 or sa.user in ?3)")
	void removeAuths(Space space, Collection<Channel> channels, Collection<User> users);
}
