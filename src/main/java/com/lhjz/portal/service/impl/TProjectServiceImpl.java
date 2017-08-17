/**
 * 版权所有 (TMS)
 */
package com.lhjz.portal.service.impl;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.lhjz.portal.entity.TProject;
import com.lhjz.portal.repository.TProjectRepository;
import com.lhjz.portal.service.TProjectService;

/**
 * @author xi
 *
 */
@Service
public class TProjectServiceImpl implements TProjectService {

	@Autowired
	TProjectRepository projectRepository;

	@Transactional
	@Override
	public Long getTaskIncId(Long pid) {
		
		TProject project = projectRepository.findOne(pid);
		Long tid = project.getTaskIncId();
		
		if(tid == null) {
			tid = 1L;
		} else {
			tid = tid + 1;
		}
		
		project.setTaskIncId(tid);
		projectRepository.saveAndFlush(project);
		
		return tid;
	}

}
