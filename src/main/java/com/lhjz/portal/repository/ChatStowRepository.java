/**
 * 版权所有 (TMS)
 */
package com.lhjz.portal.repository;

import com.lhjz.portal.entity.Chat;
import com.lhjz.portal.entity.ChatChannel;
import com.lhjz.portal.entity.ChatDirect;
import com.lhjz.portal.entity.ChatReply;
import com.lhjz.portal.entity.ChatStow;
import com.lhjz.portal.entity.security.User;
import com.lhjz.portal.pojo.Enum.Status;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import javax.transaction.Transactional;
import java.util.List;

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

	Page<ChatStow> findByChatIsNullAndStowUserAndStatus(User user, Status status, Pageable pageable);

	@Query("SELECT cs FROM ChatStow cs " +
			"LEFT JOIN cs.chatChannel cc " +  // 使用实体属性而非列名
			"LEFT JOIN cs.chatDirect cd " +   // 使用实体属性而非列名
			"LEFT JOIN cs.chatReply cr " +     // 使用实体属性而非列名
			"WHERE cs.chat IS NULL AND cs.stowUser = :user AND cs.status = :status AND " +
			"(LOWER(cc.content) LIKE LOWER(CONCAT('%', :content, '%')) OR " +
			"LOWER(cd.content) LIKE LOWER(CONCAT('%', :content, '%')) OR " +
			"LOWER(cr.content) LIKE LOWER(CONCAT('%', :content, '%')))")
	Page<ChatStow> queryChatStows(@Param("user") User user, @Param("status") Status status, @Param("content") String content, Pageable pageable);

	List<ChatStow> findByChat(Chat chat);
	
	List<ChatStow> findByChatChannel(ChatChannel chatChannel);

	List<ChatStow> findByChatDirect(ChatDirect chatDirect);

	ChatStow findOneByChatAndStowUser(Chat chat, User stowUser);
	
	ChatStow findOneByChatChannelAndStowUser(ChatChannel chatChannel, User stowUser);

	ChatStow findOneByChatDirectAndStowUser(ChatDirect chatDirect, User stowUser);

	ChatStow findOneByChatChannelAndChatReplyAndStowUser(ChatChannel chatChannel, ChatReply chatReply, User stowUser);
	
	Long deleteByChatChannel(ChatChannel chatChannel);
	
	@Transactional
	@Modifying
	@Query(value = "DELETE FROM chat_stow WHERE EXISTS(SELECT id FROM chat_channel b WHERE chat_stow.chat_channel_id = b.id AND b.channel = ?1);", nativeQuery = true)
	void deleteByChannel(Long channelId);

	@Query(value = "SELECT chat_stow.id, chat_stow.chat_channel_id FROM chat_stow WHERE chat_channel_id is not null and stow_user = ?1 and status = 'New';", nativeQuery = true)
	List<Object> listChatChannels(String username);

	@Query(value = "SELECT chat_stow.id, chat_stow.chat_direct_id FROM chat_stow WHERE chat_direct_id is not null and stow_user = ?1 and status = 'New';", nativeQuery = true)
	List<Object> listChatDirects(String username);

}