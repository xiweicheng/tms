/**
 * 版权所有 (TMS)
 */
package com.lhjz.portal.repository;

import java.util.Collection;
import java.util.Date;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.lhjz.portal.entity.Log;
import com.lhjz.portal.pojo.Enum.Target;

/**
 * 
 * @author xi
 * 
 * @date 2015年3月28日 下午2:09:06
 * 
 */
public interface LogRepository extends JpaRepository<Log, Long> {

	Page<Log> findByTarget(Target target, Pageable pageable);

	@Query(value = "SELECT * FROM log WHERE target = 'Translate' AND id > ?1 ORDER BY id DESC", nativeQuery = true)
	List<Log> queryRecent(Long lastEvtId);

	@Query(value = "SELECT COUNT(*) FROM log WHERE target = 'Translate' AND id > ?1", nativeQuery = true)
	long countQueryRecent(Long lastEvtId);
	
	List<Log> findByTargetInAndCreateDateAfter(Collection<Target> targets, Date dateAfter);
}
