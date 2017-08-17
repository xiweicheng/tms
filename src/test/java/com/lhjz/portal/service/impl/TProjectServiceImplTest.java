package com.lhjz.portal.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.annotations.Test;

import com.lhjz.portal.Application;
import com.lhjz.portal.service.TProjectService;

@SpringBootTest(classes = Application.class)
public class TProjectServiceImplTest extends AbstractTestNGSpringContextTests {

	@Autowired
	TProjectService projectService;

	@Test
	public void getTaskIncId() {
		projectService.getTaskIncId(1L);
	}
}
