/**
 * 版权所有 (TMS)
 */
package com.lhjz.portal.repository;

import java.util.Collection;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.lhjz.portal.entity.Chat;
import com.lhjz.portal.entity.security.User;
import com.lhjz.portal.pojo.Enum.ChatType;

/**
 * 
 * @author xi
 * 
 * @date 2015年3月28日 下午2:09:06
 * 
 */
public interface ChatRepository extends JpaRepository<Chat, Long> {

	@Query(value = "SELECT COUNT(*) FROM chat WHERE id >= ?1", nativeQuery = true)
	long countGtId(Long id);
	
	@Query(value = "SELECT COUNT(*) FROM chat WHERE privated <> 1 AND type = 'Wiki' AND id >= ?1", nativeQuery = true)
	long countPublicWikiGtId(Long id);
	
	@Query(value = "SELECT COUNT(*) FROM chat WHERE type = 'Wiki' AND id >= ?1", nativeQuery = true)
	long countAllWikiGtId(Long id);

	Page<Chat> findByTypeAndPrivated(ChatType type, Boolean privated,
			Pageable pageable);
	
	Page<Chat> findByType(ChatType type, Pageable pageable);
	
	Page<Chat> findByTypeAndPrivatedAndContentLike(ChatType type,
			Boolean privated, String search, Pageable pageable);
	
	Page<Chat> findByTypeAndContentLike(ChatType type, String search, Pageable pageable);

	@Query(value = "SELECT * FROM chat WHERE id > ?1 ORDER BY id ASC", nativeQuery = true)
	List<Chat> queryRecent(Long lastId);

	@Query(value = "SELECT * FROM chat WHERE id < ?1 ORDER BY create_date DESC LIMIT ?2", nativeQuery = true)
	List<Chat> queryMoreOld(Long startId, int limit);

	@Query(value = "SELECT * FROM chat WHERE id > ?1 ORDER BY create_date ASC LIMIT ?2", nativeQuery = true)
	List<Chat> queryMoreNew(Long startId, int limit);

	@Query(value = "SELECT * FROM chat WHERE id > ?1 AND (upper(content) LIKE upper(?2)) ORDER BY create_date ASC", nativeQuery = true)
	List<Chat> queryReplies(Long id, String like);

	@Query(value = "SELECT MAX(id) as max_id, MIN(id) as min_id FROM chat", nativeQuery = true)
	Object queryMaxAndMinId();

	@Query(value = "SELECT COUNT(*) as cnt FROM chat WHERE id < ?1", nativeQuery = true)
	long countAllOld(Long startId);

	@Query(value = "SELECT COUNT(*) as cnt FROM chat WHERE id > ?1", nativeQuery = true)
	long countAllNew(Long startId);

	@Query(value = "SELECT COUNT(*) FROM chat WHERE id > ?1", nativeQuery = true)
	long countQueryRecent(Long lastId);

	@Query(value = "SELECT COUNT(*) FROM chat_at WHERE at_user = ?1 AND chat_id > ?2", nativeQuery = true)
	long countQueryRecentAt(String atUsername, Long lastId);

	Page<Chat> findByContentLike(String search, Pageable pageable);

	Page<Chat> findByCreator(User creator, Pageable pageable);

	Page<Chat> findByCreatorIn(Collection<User> creators, Pageable pageable);

	Page<Chat> findByCreatorInAndContentContainingIgnoreCase(Collection<User> creators,
			String search, Pageable pageable);
}
