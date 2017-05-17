package com.lhjz.portal.repository;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;

import com.lhjz.portal.Application;
import com.lhjz.portal.entity.Channel;
import com.lhjz.portal.entity.ChatDirect;
import com.lhjz.portal.entity.File;
import com.lhjz.portal.pojo.Enum.FileType;

@SpringBootTest(classes = Application.class)
public class FileRepositoryTest extends AbstractTestNGSpringContextTests {

	@Autowired
	FileRepository fileRepository;

	@Autowired
	ChannelRepository channelRepository;

	@Autowired
	ChatDirectRepository chatDirectRepository;

	// @Test
	public void test() {
		List<File> files = fileRepository.queryByToTypeIsNull();

		files.stream().forEach((file) -> {
			String like = file.getType().equals(FileType.Image) ? "%" + file.getUuidName() + "%"
					: "%/admin/file/download/" + file.getId() + "%";
			List<Channel> channel = channelRepository.queryChannelByContentLike(like);
			if (channel.size() == 1) {
				System.out.println(channel.get(0).getName());
				int cnt = fileRepository.updateFileToChannel(channel.get(0).getName(), file.getId());
				System.out.println(cnt);
			} else {
				System.err.println("channel size: " + channel.size());
			}
		});

		System.out.println(files.size());
	}

	// @Test
	public void test2() {
		List<File> files = fileRepository.queryByToTypeIsNull();

		files.stream().forEach((file) -> {
			String like = file.getType().equals(FileType.Image) ? "%" + file.getUuidName() + "%"
					: "%/admin/file/download/" + file.getId() + "%";
			List<ChatDirect> chatDirects = chatDirectRepository.queryByCreatorAndContentLike(file.getUsername(), like);
			if (chatDirects.size() == 1) {
				System.out.println(chatDirects.get(0).getChatTo().getUsername());
				int cnt = fileRepository.updateFileToUser(chatDirects.get(0).getChatTo().getUsername(), file.getId());
				System.out.println(cnt);
			} else {
				System.err.println("channel size: " + chatDirects.size());
			}
		});

		System.out.println(files.size());
	}

}