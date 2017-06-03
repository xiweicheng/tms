/**
 * 版权所有 (TMS)
 */
package com.lhjz.portal.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.lhjz.portal.entity.ChatChannel;
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
	
	List<ChatLabel> findByChatChannel(ChatChannel chatChannel);
}
