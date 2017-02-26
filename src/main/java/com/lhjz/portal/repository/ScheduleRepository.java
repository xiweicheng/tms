/**
 * 版权所有 (TMS)
 */
package com.lhjz.portal.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.lhjz.portal.entity.Schedule;

/**
 * 
 * @author xi
 * 
 * @date 2015年3月28日 下午2:09:06
 * 
 */
public interface ScheduleRepository extends JpaRepository<Schedule, Long> {

	@Query(value = "SELECT COUNT(*) AS cnt FROM actor_schedule INNER JOIN `schedule` ON actor_schedule.schedule_id = `schedule`.id AND actor_schedule.user_id = ?1 AND `schedule`.start_date >= NOW() AND `schedule`.`status` <> 'Deleted'", nativeQuery = true)
	long countRecentScheduleByUser(String username);
}
