package com.lhjz.portal.repository;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.annotations.Test;

import com.lhjz.portal.Application;
import com.lhjz.portal.entity.ChatDirect;

@SpringApplicationConfiguration(classes = Application.class)
public class ChatDirectRepositoryTest extends AbstractTestNGSpringContextTests {

	@Autowired
	ChatDirectRepository chatDirectRepository;

	@Autowired
	UserRepository userRepository;

	@Test
	public void countAllNew() {
		System.out.println(chatDirectRepository.count());
	}

	@Test
	public void save() {
		ChatDirect cd = new ChatDirect();
		cd.setChatTo(userRepository.findOne("admin"));
		cd.setContent("test..........");

		chatDirectRepository.saveAndFlush(cd);

	}

	@Test
	public void queryChatDirect() {
		List<ChatDirect> chats = chatDirectRepository.queryChatDirect(
				userRepository.findOne("admin"), userRepository.findOne("test"),
				0, 10);
		System.out.println(chats.size());
	}

	@Test
	public void queryAboutMe() {

		List<ChatDirect> list = chatDirectRepository.queryAboutMe(
				userRepository.findOne("test"), "%df%",
				0, 5);

		System.out.println(list.size());

		long cnt = chatDirectRepository
				.countAboutMe(userRepository.findOne("test"), "%df%");

		System.out.println(cnt);
	}
}