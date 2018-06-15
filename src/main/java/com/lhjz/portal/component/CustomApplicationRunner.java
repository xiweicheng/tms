package com.lhjz.portal.component;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import com.lhjz.portal.entity.security.Authority;
import com.lhjz.portal.entity.security.AuthorityId;
import com.lhjz.portal.entity.security.User;
import com.lhjz.portal.pojo.Enum.Role;
import com.lhjz.portal.pojo.Enum.Status;
import com.lhjz.portal.repository.AuthorityRepository;
import com.lhjz.portal.repository.UserRepository;

@Component
@Order(1)
public class CustomApplicationRunner implements ApplicationRunner {

	@Autowired
	UserRepository userRepository;

	@Autowired
	PasswordEncoder passwordEncoder;

	@Autowired
	AuthorityRepository authorityRepository;

	@Autowired
	User superUser;

	@Override
	public void run(ApplicationArguments arg0) throws Exception {

		// check super exists or not
		if (userRepository.findOne(superUser.getUsername()) != null) {
			return;
		}

		// save super user
		User user = new User();
		user.setUsername(superUser.getUsername());
		user.setPassword(passwordEncoder.encode(superUser.getPassword()));
		user.setEnabled(true);
		user.setCreateDate(new Date());
		user.setName("系统管理员");
		user.setMails(superUser.getMails());
		user.setStatus(Status.Bultin);

		userRepository.saveAndFlush(user);

		List<Authority> authorities = Arrays.asList(Role.ROLE_SUPER, Role.ROLE_ADMIN, Role.ROLE_USER).stream()
				.map(r -> {
					Authority authority = new Authority();
					authority.setId(new AuthorityId(user.getUsername(), r.name()));
					return authority;
				}).collect(Collectors.toList());

		authorityRepository.save(authorities);
		authorityRepository.flush();
	}

}
