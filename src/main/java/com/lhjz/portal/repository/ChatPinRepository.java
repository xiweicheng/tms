/**
 * 版权所有 (TMS)
 */
package com.lhjz.portal.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.lhjz.portal.entity.Channel;
import com.lhjz.portal.entity.ChatChannel;
import com.lhjz.portal.entity.ChatPin;

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
	
}
