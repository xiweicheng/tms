/**
 * 版权所有 (TMS)
 */
package com.lhjz.portal.repository;

import java.util.Collection;
import java.util.Date;
import java.util.List;

import javax.transaction.Transactional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import com.lhjz.portal.entity.Channel;
import com.lhjz.portal.entity.ChatChannel;
import com.lhjz.portal.entity.security.User;
import com.lhjz.portal.pojo.Enum.Status;

/**
 * 
 * @author xi
 * 
 * @date 2015年3月28日 下午2:09:06
 * 
 */
public interface ChatChannelRepository extends JpaRepository<ChatChannel, Long> {

	Page<ChatChannel> findByChannel(Channel channel, Pageable pageable);

	Page<ChatChannel> findByChannelAndCreatorInAndStatusNot(Channel channel, Collection<User> creators, Status status,
			Pageable pageable);

	@Query(value = "SELECT * FROM chat_channel WHERE channel = ?1 AND id > ?2", nativeQuery = true)
	List<ChatChannel> latest(Channel channel, Long id);
	
	@Query(value = "SELECT COUNT(*) as cnt FROM chat_channel WHERE channel = ?1 AND id < ?2", nativeQuery = true)
	long countAllOld(Channel channel, Long startId);

	@Query(value = "SELECT COUNT(*) as cnt FROM chat_channel WHERE channel = ?1 AND id > ?2", nativeQuery = true)
	long countAllNew(Channel channel, Long startId);
	
	@Query(value = "SELECT * FROM chat_channel WHERE channel = ?1 AND id < ?2 ORDER BY id DESC LIMIT ?3", nativeQuery = true)
	List<ChatChannel> queryMoreOld(Channel channel, Long startId,
			int limit);

	@Query(value = "SELECT * FROM chat_channel WHERE channel = ?1 AND id > ?2 ORDER BY id ASC LIMIT ?3", nativeQuery = true)
	List<ChatChannel> queryMoreNew(Channel channel, Long startId,
			int limit);
	
	@Query(value = "SELECT * FROM `chat_channel` WHERE channel = ?1 AND content LIKE ?2 ORDER BY id DESC LIMIT ?3,?4", nativeQuery = true)
	List<ChatChannel> queryAboutMe(Channel channel, String search, int startId,
			int limit);

	@Query(value = "SELECT COUNT(*) FROM `chat_channel` WHERE channel = ?1 AND content LIKE ?2", nativeQuery = true)
	long countAboutMe(Channel channel, String search);
	
	@Query(value = "SELECT distinct cc.* FROM chat_channel cc, chat_label cl where cl.chat_channel = cc.id and cc.channel = ?1 and cl.`status` <> 'Deleted' and cl.name in (?2) ORDER BY id DESC LIMIT ?3,?4", nativeQuery = true)
	List<ChatChannel> queryAboutMeByTags(Channel channel, List<String> tags, int startId,
			int limit);
	
	@Query(value = "SELECT COUNT(DISTINCT cc.id) FROM chat_channel cc, chat_label cl where cl.chat_channel = cc.id and cc.channel = ?1 and cl.`status` <> 'Deleted' and cl.name in (?2)", nativeQuery = true)
	long countAboutMeByTags(Channel channel, List<String> tags);

	@Query(value = "SELECT COUNT(*) FROM chat_channel WHERE channel = ?1 AND id >= ?2", nativeQuery = true)
	long countGtId(Channel channe, long id);
	
	@Query(value = "SELECT COUNT(*) FROM chat_channel WHERE channel = ?1 AND id > ?2", nativeQuery = true)
	long countQueryRecent(Long channelId, Long lastId);
	
	@Transactional
	@Modifying
	@Query("update ChatChannel cc set cc.creator = ?1, cc.updater = ?2, cc.createDate = ?3, cc.updateDate = ?4 where cc.id = ?5")
	int updateAuditing(User creator, User updater, Date createDate, Date updateDate, Long id);
}
