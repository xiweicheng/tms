package com.lhjz.portal.repository;

import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@DataJpaTest
@AutoConfigureTestDatabase(replace = Replace.NONE)
public class ChatStowRepositoryTest {

	@Autowired
	ChatStowRepository chatStowRepository;

	@Test
	public void test() {
		List<Object> listChatChannels = chatStowRepository.listChatChannels("super");
		System.out.println(listChatChannels);
	}

}
