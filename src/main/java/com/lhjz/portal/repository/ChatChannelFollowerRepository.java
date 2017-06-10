/**
 * 版权所有 (TMS)
 */
package com.lhjz.portal.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.lhjz.portal.entity.ChatChannel;
import com.lhjz.portal.entity.ChatChannelFollower;
import com.lhjz.portal.entity.security.User;

/**
 * 
 * @author xi
 * 
 * @date 2015年3月28日 下午2:09:06
 * 
 */
public interface ChatChannelFollowerRepository extends JpaRepository<ChatChannelFollower, Long> {

	ChatChannelFollower findOneByChatChannelAndCreator(ChatChannel chatChannel, User user);

	List<ChatChannelFollower> findByCreator(User user);

	List<ChatChannelFollower> findByChatChannel(ChatChannel chatChannel);
}
