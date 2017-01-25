package com.lhjz.portal.component;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.AuditorAware;
import org.springframework.stereotype.Component;

import com.lhjz.portal.entity.security.User;
import com.lhjz.portal.repository.UserRepository;
import com.lhjz.portal.util.WebUtil;

@Component
public class SpringSecurityAuditorAware implements AuditorAware<User> {

	@Autowired
	UserRepository userRepository;

	@Override
	public User getCurrentAuditor() {

		return userRepository.findOne(WebUtil.getUsername());

	}

}
