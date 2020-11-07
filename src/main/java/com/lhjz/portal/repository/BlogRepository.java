/**
 * 版权所有 (TMS)
 */
package com.lhjz.portal.repository;

import java.util.Date;
import java.util.List;

import javax.transaction.Transactional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.lhjz.portal.entity.Blog;
import com.lhjz.portal.entity.Dir;
import com.lhjz.portal.entity.Space;
import com.lhjz.portal.entity.security.User;
import com.lhjz.portal.pojo.Enum.Status;

/**
 * 
 * @author xi
 * 
 * @date 2015年3月28日 下午2:09:06
 * 
 */
public interface BlogRepository extends JpaRepository<Blog, Long> {

	Page<Blog> findByStatusNot(Status status, Pageable pageable);

	List<Blog> findByStatusNotAndTags_nameIn(Status status, List<String> tags, Sort sort);

	List<Blog> findByStatusNot(Status status, Sort sort);

	Page<Blog> findByTitleContainingIgnoreCaseOrContentContainingIgnoreCase(String searchT, String searchC,
			Pageable pageable);

	List<Blog> findByStatusNotAndTitleContainingIgnoreCaseOrStatusNotAndContentContainingIgnoreCase(Status status,
			String searchT, Status status2, String searchC, Sort sort);

	List<Blog> findByCreatorAndStatusNotAndTitleContainingIgnoreCaseOrCreatorAndStatusNotAndContentContainingIgnoreCase(
			User creator, Status status, String searchT, User creator2, Status status2, String searchC, Sort sort);

	List<Blog> findByCreatorAndStatusNot(User creator, Status status, Sort sort);

	List<Blog> findByStatusNotAndTitleContainingIgnoreCaseAndOpenedTrueOrStatusNotAndContentContainingIgnoreCaseAndOpenedTrue(
			Status status, String searchT, Status status2, String searchC, Sort sort);

	List<Blog> findByStatusNotAndSpaceAndTitleContainingIgnoreCaseAndOpenedTrueOrStatusNotAndContentContainingIgnoreCaseAndOpenedTrue(
			Status status, Space space, String searchT, Status status2, String searchC, Sort sort);

	Page<Blog> findByStatusNotAndTitleContainingIgnoreCaseAndOpenedTrueOrStatusNotAndContentContainingIgnoreCaseAndOpenedTrue(
			Status status, String searchT, Status status2, String searchC, Pageable pageable);

	Page<Blog> findByStatusNotAndSpaceAndTitleContainingIgnoreCaseAndOpenedTrueOrStatusNotAndSpaceAndContentContainingIgnoreCaseAndOpenedTrue(
			Status status, Space space, String searchT, Status status2, Space space2, String searchC,
			Pageable pageable);

	Page<Blog> findByStatusNotAndTitleContainingIgnoreCaseAndOpenedTrue(Status status, String search,
			Pageable pageable);

	Page<Blog> findByStatusNotAndSpaceAndTitleContainingIgnoreCaseAndOpenedTrue(Status status, Space space,
			String search, Pageable pageable);

	Page<Blog> findByStatusNotAndContentContainingIgnoreCaseAndOpenedTrue(Status status, String search,
			Pageable pageable);

	Page<Blog> findByStatusNotAndSpaceAndContentContainingIgnoreCaseAndOpenedTrue(Status status, Space space,
			String search, Pageable pageable);

	Page<Blog> findByStatusNotAndCreatorOrStatusNotAndPrivatedFalse(Status status, User creator, Status status2,
			Pageable pageable);

	Page<Blog> findByTitleContainingIgnoreCaseOrContentContainingIgnoreCaseAndStatusNotAndCreatorOrStatusNotAndPrivatedFalse(
			String searchT, String searchC, Status status, User creator, Status status2, Pageable pageable);

	@Query(value = "SELECT * FROM blog WHERE blog.status <> 'Deleted' AND (privated = 0 OR creator = :username) AND (upper(title) LIKE upper(:search) OR upper(content) LIKE upper(:search)) ORDER BY id DESC LIMIT :size OFFSET :start", nativeQuery = true)
	List<Blog> search(@Param("username") String username, @Param("search") String search, @Param("start") Integer start,
			@Param("size") Integer size);

	@Query(value = "SELECT * FROM blog WHERE status <> 'Deledted' AND tpl = 2 OR (tpl = 1 AND creator = :username) ORDER BY create_date DESC", nativeQuery = true)
	List<Blog> queryTpl(@Param("username") String username);

	@Query(value = "SELECT COUNT(*) FROM blog WHERE blog.status <> 'Deleted' AND (privated = 0 OR creator = :username) AND (upper(title) LIKE upper(:search) OR upper(content) LIKE upper(:search))", nativeQuery = true)
	long countSearch(@Param("username") String username, @Param("search") String search);

	@Query(value = "SELECT COUNT(*) as cnt FROM blog WHERE status <> 'Deleted'", nativeQuery = true)
	long countBlogs();

	@Transactional
	@Modifying
	@Query("update Blog b set b.readCnt = ?1 where b.id = ?2")
	int updateReadCnt(Long readCnt, Long id);

	@Transactional
	@Modifying
	@Query("update Blog b set b.privated = ?1, b.opened = ?2 where b.id = ?3")
	int updatePrivatedAndOpened(Boolean privated, Boolean opened, Long id);

	@Transactional
	@Modifying
	@Query("update Blog b set b.space = ?1, b.dir = ?2 where b.id = ?3")
	int updateSpaceAndDir(Space space, Dir dir, Long id);

	@Transactional
	@Modifying
	@Query("update Blog b set b.privated = ?1 where b.id = ?2")
	int updatePrivated(Boolean privated, Long id);

	@Transactional
	@Modifying
	@Query("update Blog b set b.opened= ?1 where b.id = ?2")
	int updateOpened(Boolean opened, Long id);

	@Transactional
	@Modifying
	@Query("update Blog b set b.tplHotCnt = ?1 where b.id = ?2")
	int updateTplHotCnt(Long tplHotCnt, Long id);

	@Transactional
	@Modifying
	@Query("update Blog b set b.shareId = ?1 where b.id = ?2")
	int updateShareId(String shareId, Long id);

	@Transactional
	@Modifying
	@Query("update Blog b set b.tpl = ?1, b.tplDesc = ?2 where b.id = ?3")
	int updateTpl(Integer tpl, String desc, Long id);

	@Transactional
	@Modifying
	@Query("update Blog b set b.voteZan = ?1, b.voteZanCnt = ?2 where b.id = ?3")
	int updateVoteZan(String voteZan, Integer voteZanCnt, Long id);

	@Transactional
	@Modifying
	@Query("update Blog b set b.locker = ?1, b.lockDate = ?2 where b.id = ?3")
	int updateLock(User locker, Date lockDate, Long id);

	Page<Blog> findByStatusNotAndOpenedTrue(Status status, Pageable pageable);

	Page<Blog> findByStatusNotAndOpenedTrueAndSpace(Status status, Space space, Pageable pageable);

	Blog findTopByStatusNotAndOpenedTrueAndIdLessThanOrderByIdDesc(Status status, Long id);

	Blog findTopByStatusNotAndOpenedTrueAndSpaceAndIdLessThanOrderByIdDesc(Status status, Space space, Long id);

	Blog findTopByStatusNotAndOpenedTrueAndIdGreaterThanOrderByIdAsc(Status status, Long id);

	Blog findTopByStatusNotAndOpenedTrueAndSpaceAndIdGreaterThanOrderByIdAsc(Status status, Space space, Long id);

	Blog findTopByStatusNotAndShareId(Status status, String shareId);
}
