package com.lhjz.portal.repository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;

import com.lhjz.portal.Application;

@SpringBootTest(classes = Application.class)
public class LinkRepositoryTest extends AbstractTestNGSpringContextTests {
	
	@Autowired
	LinkRepository linkRepository;

//	@Test
	public void incCount() {
		linkRepository.incCount(7L);
	}
}
