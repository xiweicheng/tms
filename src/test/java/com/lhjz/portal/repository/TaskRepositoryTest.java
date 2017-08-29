package com.lhjz.portal.repository;

import java.util.stream.Stream;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.annotations.Test;

import com.lhjz.portal.Application;
import com.lhjz.portal.entity.TProject;
import com.lhjz.portal.entity.TStatus;
import com.lhjz.portal.entity.Task;
import com.lhjz.portal.entity.security.User;
import com.lhjz.portal.model.TaskSearchParams;
import com.lhjz.portal.pojo.Enum.TaskType;
import com.lhjz.portal.specs.TaskSpecs;

@SpringBootTest(classes = Application.class)
public class TaskRepositoryTest extends AbstractTestNGSpringContextTests {

	@Autowired
	TaskRepository taskRepository;

	@Test
	public void search() {
		
		TaskSearchParams params = new TaskSearchParams();
		
		TProject project = new TProject();
		project.setId(1L);
		params.getProjects().add(project);
		
//		project = new TProject();
//		project.setId(2L);
//		params.getProjects().add(project);
		
		Stream.of(TaskType.values()).forEach(item -> {
			params.getTaskTypes().add(item);
		});
		
//		TStatus status = new TStatus();
//		status.setId(1L);
//		params.getStates().add(status);
//		
//		status = new TStatus();
//		status.setId(2L);
//		params.getStates().add(status);
		
		User operator = new User("admin");
		params.getOperator().add(operator);

		Page<Task> page = taskRepository.findAll(TaskSpecs.search(params), new PageRequest(0, 10));

		page.forEach(t -> {
			System.out.println(t.getTitle());
		});
	}
}
