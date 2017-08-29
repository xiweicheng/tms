/**
 * 版权所有 (TMS)
 */
package com.lhjz.portal.model;

import java.util.ArrayList;
import java.util.List;

import com.lhjz.portal.entity.TProject;
import com.lhjz.portal.entity.TStatus;
import com.lhjz.portal.entity.security.User;
import com.lhjz.portal.pojo.Enum.TaskType;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 
 * @author xi
 * 
 * @date 2016年5月20日 下午8:38:54
 * 
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class TaskSearchParams {

	List<TProject> projects = new ArrayList<>();
	
	List<TaskType> taskTypes = new ArrayList<>();
	
	List<TStatus> states = new ArrayList<>();
	
	List<User> operator = new ArrayList<>();

}
