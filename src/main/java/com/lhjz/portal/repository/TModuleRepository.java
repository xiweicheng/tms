/**
 * 版权所有 (TMS)
 */
package com.lhjz.portal.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.lhjz.portal.entity.TModule;
import com.lhjz.portal.entity.TProject;
import com.lhjz.portal.pojo.Enum.Status;

/**
 * 
 * @author xi
 * 
 * @date 2015年3月28日 下午2:09:06
 * 
 */
public interface TModuleRepository extends JpaRepository<TModule, Long> {
	
	List<TModule> findByStatusNot(Status status);
	
	TModule findOneByStatusNotAndProjectAndName(Status status, TProject project, String name);
	
	List<TModule> findByStatusNotAndProject(Status status, TProject project);
}
