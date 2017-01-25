/**
 * 版权所有 (TMS)
 */
package com.lhjz.portal.repository;

import java.util.List;

import javax.transaction.Transactional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import com.lhjz.portal.entity.Chat;
import com.lhjz.portal.entity.ChatAt;
import com.lhjz.portal.entity.ChatChannel;
import com.lhjz.portal.entity.security.User;
import com.lhjz.portal.pojo.Enum.Status;

/**
 * 
 * @author xi
 * 
 * @date 2015年3月28日 下午2:09:06
 * 
 */
public interface ChatAtRepository extends JpaRepository<ChatAt, Long> {

	Page<ChatAt> findByChatNotNullAndAtUserAndStatus(User user, Status status,
			Pageable pageable);
	
	Page<ChatAt> findByChatChannelNotNullAndAtUserAndStatus(User user, Status status,
			Pageable pageable);

	List<ChatAt> findByChat(Chat chat);
	
	List<ChatAt> findByChatChannel(ChatChannel chatChannel);

	ChatAt findOneByChatAndAtUser(Chat chat, User atUser);
	
	ChatAt findOneByChatChannelAndAtUser(ChatChannel chatChannel, User atUser);

	@Query(value = "SELECT COUNT(*) FROM `chat_at` WHERE `chat_id` IS NOT NULL AND at_user = ?1 AND `status` = 'New';", nativeQuery = true)
	long countChatAtUserNew(String atUser);
	
	@Query(value = "SELECT COUNT(*) FROM `chat_at` WHERE `chat_channel_id` IS NOT NULL AND at_user = ?1 AND `status` = 'New';", nativeQuery = true)
	long countChatChannelAtUserNew(String atUser);

	@Transactional
	@Modifying
	@Query("update ChatAt ca set ca.status = 'Readed' where ca.chat is not null and ca.atUser = ?1 and ca.status = 'New'")
	int markChatAllAsReaded(User atUser);
	
	@Transactional
	@Modifying
	@Query("update ChatAt ca set ca.status = 'Readed' where ca.chatChannel is not null and ca.atUser = ?1 and ca.status = 'New'")
	int markChatChannelAllAsReaded(User atUser);
	
	@Transactional
	@Modifying
	@Query("update ChatAt ca set ca.status = 'Readed' where ca.chat = ?1 and ca.atUser = ?2 and ca.status = 'New'")
	int markChatAsReaded(Chat chat, User atUser);
	
	@Transactional
	@Modifying
	@Query("update ChatAt ca set ca.status = 'Readed' where ca.chatChannel = ?1 and ca.atUser = ?2 and ca.status = 'New'")
	int markChatChannelAsReaded(ChatChannel chatChannel, User atUser);
	
	Long deleteByChatChannel(ChatChannel chatChannel);
	
	@Transactional
	@Modifying
	@Query(value = "DELETE FROM chat_at WHERE EXISTS(SELECT id FROM chat_channel b WHERE chat_at.chat_channel_id = b.id AND b.channel = ?1);", nativeQuery = true)
	void deleteByChannel(Long channelId);
	
	@Query(value = "SELECT COUNT(*) as cnt FROM `chat_at` WHERE at_user = ?1 AND chat_channel_id > ?2", nativeQuery = true)
	long countChatChannelRecentAt(String atUsername, Long lastChatChannelId);
}