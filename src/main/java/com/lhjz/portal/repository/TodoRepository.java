/**
 * 版权所有 (TMS)
 */
package com.lhjz.portal.repository;

import java.util.Collection;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;

import com.lhjz.portal.entity.Todo;
import com.lhjz.portal.entity.security.User;
import com.lhjz.portal.pojo.Enum.Status;

/**
 * @author xi
 * @date 2015年3月28日 下午2:09:06
 */
public interface TodoRepository extends JpaRepository<Todo, Long> {

    List<Todo> findByStatusNotAndCreator(Status status, User creator, Sort sort);

    List<Todo> findByStatusInAndCreator(Collection<Status> status, User creator, Sort sort);

    Page<Todo> findByStatusAndCreator(Status status, User creator, Pageable pageable);

    Page<Todo> findByStatusAndCreatorAndTitleContainingIgnoreCaseOrStatusAndCreatorAndContentContainingIgnoreCase(
            Status status, User creator, String searchT, Status status2, User creator2, String searchC, Pageable pageable);

}
