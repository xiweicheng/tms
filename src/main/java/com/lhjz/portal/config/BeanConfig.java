package com.lhjz.portal.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cache.CacheManager;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import com.lhjz.portal.constant.SysConstant;
import com.lhjz.portal.entity.security.User;
import com.lhjz.portal.model.SysConf;

@Configuration
public class BeanConfig {

	@Bean
	BCryptPasswordEncoder bCryptPasswordEncoderBean() {
		return new BCryptPasswordEncoder();
	}

	@Bean
	@ConfigurationProperties(prefix = "tms.super")
	User superUser() {
		return new User();
	}
	
	@Bean
	@ConfigurationProperties(prefix = "tms.sys.conf")
	SysConf sysConfBean() {
		return new SysConf();
	}

	@Bean
	public CacheManager cacheManager() {
		return new ConcurrentMapCacheManager(SysConstant.ONLINE_USERS, SysConstant.LOCK_BLOGS);
	}

}