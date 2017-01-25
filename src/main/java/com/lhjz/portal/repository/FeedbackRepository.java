/**
 * 版权所有 (TMS)
 */
package com.lhjz.portal.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.lhjz.portal.entity.Feedback;

/**
 * 
 * @author xi
 * 
 * @date 2015年3月28日 下午2:09:06
 * 
 */
public interface FeedbackRepository extends JpaRepository<Feedback, Long> {

	List<Feedback> findByContentAndUsername(String content, String username);

	List<Feedback> findByContent(String content);

}
