package com.lhjz.portal.repository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.annotations.Test;

import com.lhjz.portal.Application;

@SpringBootTest(classes = Application.class)
public class ChatAtRepositoryTest extends AbstractTestNGSpringContextTests {

	@Autowired
	ChatAtRepository chatAtRepository;

	@Autowired
	UserRepository userRepository;

	@Test
	public void markAllAsReaded() {
		int cnt = chatAtRepository
				.markChatAllAsReaded(userRepository.findOne("test"));

		System.out.println(cnt);
	}
}
