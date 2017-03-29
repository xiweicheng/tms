/**
 * 版权所有 (TMS)
 */
package com.lhjz.portal.repository;

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
	
	List<Blog> findByStatusNot(Status status, Sort sort);

	Page<Blog> findByTitleContainingOrContentContaining(String searchT, String searchC, Pageable pageable);
	
	List<Blog> findByStatusNotAndTitleContainingOrStatusNotAndContentContaining(Status status, String searchT, Status status2, String searchC, Sort sort);

	Page<Blog> findByStatusNotAndCreatorOrStatusNotAndPrivatedFalse(Status status, User creator, Status status2,
			Pageable pageable);

	Page<Blog> findByTitleContainingOrContentContainingAndStatusNotAndCreatorOrStatusNotAndPrivatedFalse(String searchT,
			String searchC, Status status, User creator, Status status2, Pageable pageable);

	@Query(value = "SELECT * FROM `blog` WHERE blog.`status` <> 'Deleted' AND (privated = 0 OR creator = :username) AND (title LIKE :search OR content LIKE :search) ORDER BY id DESC LIMIT :start,:size", nativeQuery = true)
	List<Blog> search(@Param("username") String username, @Param("search") String search, @Param("start") Integer start,
			@Param("size") Integer size);

	@Query(value = "SELECT COUNT(*) FROM `blog` WHERE blog.`status` <> 'Deleted' AND (privated = 0 OR creator = :username) AND (title LIKE :search OR content LIKE :search)", nativeQuery = true)
	long countSearch(@Param("username") String username, @Param("search") String search);
	
	@Query(value = "SELECT COUNT(*) as cnt FROM blog WHERE `status` <> 'Deleted'", nativeQuery = true)
	long countBlogs();

	@Transactional
	@Modifying
	@Query("update Blog b set b.readCnt = ?1 where b.id = ?2")
	int updateReadCnt(Long readCnt, Long id);
}
