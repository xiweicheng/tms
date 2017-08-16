package com.lhjz.portal.repository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.annotations.Test;

import com.lhjz.portal.Application;
import com.lhjz.portal.entity.Task;
import com.lhjz.portal.specs.TaskSpecs;

@SpringBootTest(classes = Application.class)
public class TaskRepositoryTest extends AbstractTestNGSpringContextTests {

	@Autowired
	TaskRepository taskRepository;

	@Test
	public void search() {

		Page<Task> page = taskRepository.findAll(TaskSpecs.search(), new PageRequest(0, 10));

		page.forEach(t -> {
			System.out.println(t);
		});
	}
}
