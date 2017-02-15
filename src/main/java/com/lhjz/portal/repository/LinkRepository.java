/**
 * 版权所有 (TMS)
 */
package com.lhjz.portal.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.lhjz.portal.entity.Link;
import com.lhjz.portal.pojo.Enum.LinkType;
import com.lhjz.portal.pojo.Enum.Status;

/**
 * 
 * @author xi
 * 
 * @date 2015年3月28日 下午2:09:06
 * 
 */
public interface LinkRepository extends JpaRepository<Link, Long> {
	
	List<Link> findByChannelIdAndStatus(Long channelId, Status status);
	
	List<Link> findByTypeAndStatus(LinkType type, Status status);
	
}