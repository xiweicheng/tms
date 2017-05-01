/**
 * 版权所有 (TMS)
 */
package com.lhjz.portal.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;

import com.lhjz.portal.entity.Comment;
import com.lhjz.portal.pojo.Enum.CommentType;
import com.lhjz.portal.pojo.Enum.Status;

/**
 * 
 * @author xi
 * 
 * @date 2015年3月28日 下午2:09:06
 * 
 */
public interface CommentRepository extends JpaRepository<Comment, Long> {
	
	Page<Comment> findByTargetIdAndStatusNot(String targetId, Status status, Pageable pageable);
	
	List<Comment> findByTypeAndStatusNotAndContentContaining(CommentType type, Status status, String search, Sort sort);

}