package com.lhjz.portal.repository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.annotations.Test;

import com.lhjz.portal.Application;

@SpringBootTest(classes = Application.class)
public class ChatRepositoryTest extends AbstractTestNGSpringContextTests {

	@Autowired
	ChatRepository chatRepository;

	@Test
	public void countAllNew() {
		System.out.println(chatRepository.count());
	}

	@Test
	public void queryMaxAndMinId() {
		Object[] mm = (Object[]) chatRepository.queryMaxAndMinId();
		System.out.println(mm[0]);
		System.out.println(mm[1]);
	}
}