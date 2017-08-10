/**
 * 版权所有 (TMS)
 */
package com.lhjz.portal.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.lhjz.portal.entity.TProject;
import com.lhjz.portal.entity.TVersion;
import com.lhjz.portal.pojo.Enum.Status;

/**
 * 
 * @author xi
 * 
 * @date 2015年3月28日 下午2:09:06
 * 
 */
public interface TVersionRepository extends JpaRepository<TVersion, Long> {
	
	List<TVersion> findByStatusNot(Status status);
	
	List<TVersion> findByStatusNotAndProject(Status status, TProject project);
	
	TVersion findOneByStatusNotAndProjectAndName(Status status, TProject project, String name);
}
