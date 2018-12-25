/**
 * 版权所有 (TMS)
 */
package com.lhjz.portal.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.lhjz.portal.entity.Channel;
import com.lhjz.portal.entity.Gantt;
import com.lhjz.portal.entity.security.User;
import com.lhjz.portal.pojo.Enum.Status;

/**
 * 
 * @author xi
 * 
 * @date 2015年3月28日 下午2:09:06
 * 
 */
public interface GanttRepository extends JpaRepository<Gantt, Long> {

	@Query(value = "SELECT * FROM `gantt` WHERE `status` <> 'Deleted' channel = ?1 AND title LIKE ?2 AND (creator = ?3 OR privated = 0) ORDER BY id DESC LIMIT ?4,?5", nativeQuery = true)
	List<Gantt> search(Channel channel, String search, User user, int startId, int limit);

	@Query(value = "SELECT * FROM `gantt` WHERE `status` <> 'Deleted' channel = ?1 AND title LIKE ?2 AND (creator = ?3 OR privated = 0)", nativeQuery = true)
	long count(Channel channel, String search, User user);

	Page<Gantt> findByStatusNotAndChannelAndTitleContainingIgnoreCase(Status status, Channel channel, String search,
			Pageable pageable);

}
