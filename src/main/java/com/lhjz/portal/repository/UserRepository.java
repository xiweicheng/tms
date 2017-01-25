/**
 * 版权所有 (TMS)
 */
package com.lhjz.portal.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.lhjz.portal.entity.security.User;

/**
 * 
 * @author xi
 * 
 * @date 2015年3月28日 下午2:09:06
 * 
 */
public interface UserRepository extends JpaRepository<User, String> {

	List<User> findByEnabled(boolean enabled);

	List<User> findByEnabledTrue();

	List<User> findByEnabledFalse();

	List<User> findByMails(String mail);

	User findOneByResetPwdToken(String token);

}
