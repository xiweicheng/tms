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
import com.lhjz.portal.entity.ChatChannel;
import com.lhjz.portal.entity.ChatReply;
import com.lhjz.portal.entity.ChatStow;
import com.lhjz.portal.entity.security.User;
import com.lhjz.portal.pojo.Enum.Status;

/**
 * 
 * @author xi
 * 
 * @date 2015年3月28日 下午2:09:06
 * 
 */
public interface ChatStowRepository extends JpaRepository<ChatStow, Long> {

	List<ChatStow> findByChatNotNullAndStowUserAndStatus(User user, Status status);
	
	List<ChatStow> findByChatChannelNotNullAndStowUserAndStatus(User user, Status status);
	
	Page<ChatStow> findByChatChannelNotNullAndStowUserAndStatus(User user, Status status, Pageable pageable);

	List<ChatStow> findByChat(Chat chat);
	
	List<ChatStow> findByChatChannel(ChatChannel chatChannel);

	ChatStow findOneByChatAndStowUser(Chat chat, User stowUser);
	
	ChatStow findOneByChatChannelAndStowUser(ChatChannel chatChannel, User stowUser);
	
	ChatStow findOneByChatChannelAndChatReplyAndStowUser(ChatChannel chatChannel, ChatReply chatReply, User stowUser);
	
	Long deleteByChatChannel(ChatChannel chatChannel);
	
	@Transactional
	@Modifying
	@Query(value = "DELETE FROM chat_stow WHERE EXISTS(SELECT id FROM chat_channel b WHERE chat_stow.chat_channel_id = b.id AND b.channel = ?1);", nativeQuery = true)
	void deleteByChannel(Long channelId);

	@Query(value = "SELECT chat_stow.id, chat_stow.chat_channel_id FROM chat_stow WHERE stow_user = ?1 and `status` = 'New';", nativeQuery = true)
	List<Object> listChatChannels(String username);

}