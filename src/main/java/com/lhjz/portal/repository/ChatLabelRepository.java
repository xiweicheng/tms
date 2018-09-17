/**
 * 版权所有 (TMS)
 */
package com.lhjz.portal.repository;

import java.util.List;

import javax.transaction.Transactional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import com.lhjz.portal.entity.ChatChannel;
import com.lhjz.portal.entity.ChatDirect;
import com.lhjz.portal.entity.ChatLabel;
import com.lhjz.portal.entity.security.User;
import com.lhjz.portal.pojo.Enum.Status;

/**
 * 
 * @author xi
 * 
 * @date 2015年3月28日 下午2:09:06
 * 
 */
public interface ChatLabelRepository extends JpaRepository<ChatLabel, Long> {

	ChatLabel findOneByNameAndChatChannelAndStatusNot(String name, ChatChannel chatChannel, Status status);

	ChatLabel findOneByNameAndChatDirectAndStatusNot(String name, ChatDirect chatDirect, Status status);

	List<ChatLabel> findByChatChannel(ChatChannel chatChannel);

	List<ChatLabel> findByChatDirect(ChatDirect chatDirect);

	@Query(value = "SELECT DISTINCT name FROM chat_label WHERE type = 'Tag' AND status <> 'Deleted' AND creator = ?1 ORDER BY name", nativeQuery = true)
	List<String> queryTagsByUser(String username);
	
	@Transactional
	@Modifying
	@Query("update ChatLabel cl set cl.status = 'Deleted' where cl.creator = ?1 and cl.name = ?2 AND cl.status = 'New'")
	int markChatLabelAsDeleted(User user, String name);

}
