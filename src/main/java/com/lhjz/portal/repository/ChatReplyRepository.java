/**
 * 版权所有 (TMS)
 */
package com.lhjz.portal.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.lhjz.portal.entity.ChatChannel;
import com.lhjz.portal.entity.ChatReply;
import com.lhjz.portal.pojo.Enum.ChatReplyType;
import com.lhjz.portal.pojo.Enum.Status;

/**
 * 
 * @author xi
 * 
 * @date 2015年3月28日 下午2:09:06
 * 
 */
public interface ChatReplyRepository extends JpaRepository<ChatReply, Long> {

	List<ChatReply> findByChatChannelAndTypeAndStatusNot(ChatChannel chatChannel, ChatReplyType type, Status status);

	List<ChatReply> findByChatChannelAndTypeAndStatusNotAndIdGreaterThan(ChatChannel chatChannel, ChatReplyType type,
			Status status, Long id);

	Page<ChatReply> findByChatChannelAndTypeAndStatusNot(ChatChannel chatChannel, ChatReplyType type, Status status,
			Pageable pageable);
	
	ChatReply findTopByUuid(String uuid);
}
