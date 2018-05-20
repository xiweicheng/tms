/**
 * 版权所有 (TMS)
 */
package com.lhjz.portal.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.lhjz.portal.entity.ChatChannel;
import com.lhjz.portal.entity.ChatDirect;
import com.lhjz.portal.entity.ChatLabel;

/**
 * 
 * @author xi
 * 
 * @date 2015年3月28日 下午2:09:06
 * 
 */
public interface ChatLabelRepository extends JpaRepository<ChatLabel, Long> {

	ChatLabel findOneByNameAndChatChannel(String name, ChatChannel chatChannel);

	ChatLabel findOneByNameAndChatDirect(String name, ChatDirect chatDirect);

	List<ChatLabel> findByChatChannel(ChatChannel chatChannel);

	List<ChatLabel> findByChatDirect(ChatDirect chatDirect);

	@Query(value = "SELECT DISTINCT name FROM chat_label WHERE type = 'Tag' AND creator = ?1 ORDER BY name", nativeQuery = true)
	List<String> queryTagsByUser(String username);

}
