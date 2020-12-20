/**
 * 版权所有 (TMS)
 */
package com.lhjz.portal.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.lhjz.portal.entity.Channel;
import com.lhjz.portal.pojo.Enum.Status;

/**
 * 
 * @author xi
 * 
 * @date 2015年3月28日 下午2:09:06
 * 
 */
public interface ChannelRepository extends JpaRepository<Channel, Long> {
	
	@Query(value = "SELECT channel.* FROM channel INNER JOIN chat_channel ON channel.id = chat_channel.channel WHERE (upper(content) LIKE upper(?1))", nativeQuery = true)
	List<Channel> queryChannelByContentLike(String conentLike);
	
	Channel findOneByName(String name);
	
	@Query(value = "SELECT COUNT(*) as cnt FROM channel WHERE status <> 'Deleted'", nativeQuery = true)
	long countChannels();
	
	List<Channel> findTop6ByNameContainingIgnoreCaseAndStatusNot(String name, Status status);
	
}
