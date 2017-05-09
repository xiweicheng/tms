/**
 * 版权所有 (TMS)
 */
package com.lhjz.portal.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.lhjz.portal.entity.Tag;
import com.lhjz.portal.entity.security.User;

/**
 * 
 * @author xi
 * 
 * @date 2015年3月28日 下午2:09:06
 * 
 */
public interface TagRepository extends JpaRepository<Tag, Long> {

	Tag findOneByNameAndCreator(String name, User creator);
	
	List<Tag> findByCreator(User creator);

}
