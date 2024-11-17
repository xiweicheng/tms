/**
 * 版权所有 (TMS)
 */
package com.lhjz.portal.repository;

import com.lhjz.portal.entity.Channel;
import com.lhjz.portal.entity.ChatChannel;
import com.lhjz.portal.entity.ChatPin;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

/**
 * 
 * @author xi
 * 
 * @date 2015年3月28日 下午2:09:06
 * 
 */
public interface ChatPinRepository extends JpaRepository<ChatPin, Long> {

	ChatPin findOneByChannelAndChatChannel(Channel channel, ChatChannel chatChannel);

	List<ChatPin> findByChannel(Channel channel);
	
	Page<ChatPin> findByChannel(Channel channel, Pageable pageable);

	@Query("SELECT cp FROM ChatPin cp " +
			"LEFT JOIN cp.chatChannel cc " +  // 使用实体属性而非列名
			"WHERE cp.channel = :channel AND " +
			"LOWER(cc.content) LIKE LOWER(CONCAT('%', :content, '%'))")
	Page<ChatPin> queryChatPins(@Param("channel") Channel channel, @Param("content") String content, Pageable pageable);

	@Query(value = "SELECT chat_pin.id, chat_pin.chat_channel FROM chat_pin WHERE channel = ?1 and status = 'New';", nativeQuery = true)
	List<Object> listByChannel(Long channelId);

}
