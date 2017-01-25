/**
 * 版权所有 (TMS)
 */
package com.lhjz.portal.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.lhjz.portal.entity.security.Group;
import com.lhjz.portal.entity.security.GroupMember;

/**
 * 
 * @author xi
 * 
 * @date 2015年3月28日 下午2:09:06
 * 
 */
public interface GroupMemberRepository
		extends JpaRepository<GroupMember, Long> {

	List<GroupMember> findByGroup(Group group);

	List<GroupMember> findByUsername(String username);

	List<GroupMember> findByGroupAndUsername(Group group, String username);

}
