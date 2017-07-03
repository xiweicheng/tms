/**
 * 版权所有 (TMS)
 */
package com.lhjz.portal.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.lhjz.portal.entity.TProject;
import com.lhjz.portal.entity.TStatus;
import com.lhjz.portal.pojo.Enum.Status;

/**
 * 
 * @author xi
 * 
 * @date 2015年3月28日 下午2:09:06
 * 
 */
public interface TStatusRepository extends JpaRepository<TStatus, Long> {
	
	List<TStatus> findByStatusNot(Status status);
	List<TStatus> findByStatusNotAndProject(Status status, TProject project);
}
