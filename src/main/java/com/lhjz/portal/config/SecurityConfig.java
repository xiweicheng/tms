/**
 * 版权所有 (TMS)
 */
package com.lhjz.portal.config;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;
import org.springframework.security.web.authentication.rememberme.JdbcTokenRepositoryImpl;
import org.springframework.security.web.authentication.rememberme.PersistentTokenRepository;

import com.lhjz.portal.component.AjaxAwareLoginUrlAuthenticationEntryPoint;
import com.lhjz.portal.component.AjaxSimpleUrlAuthenticationFailureHandler;
import com.lhjz.portal.component.AjaxSimpleUrlLogoutSuccessHandler;
import com.lhjz.portal.component.LoginSuccessHandler;

/**
 * 
 * @author xi
 * 
 * @date 2015年3月30日 下午9:42:00
 * 
 */
@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(securedEnabled = true)
public class SecurityConfig {

	static final int order = -10;

	@Autowired
	DataSource dataSource;

	@Autowired
	PasswordEncoder passwordEncoder;

	@Autowired
	public void configureGlobal(AuthenticationManagerBuilder auth) throws Exception {

		auth.jdbcAuthentication().dataSource(dataSource).passwordEncoder(passwordEncoder);
	}

	@Configuration
	@Order(order)
	@Profile({ "dev", "prod" })
	public static class SecurityConfiguration extends WebSecurityConfigurerAdapter {

		@Autowired
		DataSource dataSource;

		@Autowired
		LoginSuccessHandler loginSuccessHandler;

		@Autowired
		AjaxSimpleUrlAuthenticationFailureHandler ajaxSimpleUrlAuthenticationFailureHandler;

		@Bean
		public PersistentTokenRepository persistentTokenRepository() {
			JdbcTokenRepositoryImpl db = new JdbcTokenRepositoryImpl();
			db.setDataSource(dataSource);
			return db;
		}

		@Bean
		public AuthenticationEntryPoint authenticationEntryPoint() {
			return new AjaxAwareLoginUrlAuthenticationEntryPoint("/admin/login");
		}

		@Bean
		public LogoutSuccessHandler logoutSuccessHandler() {
			return new AjaxSimpleUrlLogoutSuccessHandler("/admin/login?logout");
		}

		@Override
		public void configure(WebSecurity web) throws Exception {
			// @formatter:off
			web.ignoring().antMatchers(
					"/admin/file/download/**", 
					"/admin/css/**", 
					"/admin/img/**", 
					"/admin/js/**",
					"/admin/login", 
					"/admin/page/**",
					"/admin/vuejs/**",
					"/img/**",
					"/landing/**",
					"/lib/**",
					"/page/**",
					"/index.html",
					"/favicon.ico");
			// @formatter:on
		}

		@Override
		protected void configure(HttpSecurity http) throws Exception {

			// @formatter:off
			http
				.authorizeRequests()
				.antMatchers("/admin/**", "/api/**", "/ws/**")
					.authenticated()
				.and()
					.exceptionHandling()
					.authenticationEntryPoint(authenticationEntryPoint())
				.and().httpBasic()
				.and()
					.formLogin()
					.loginPage("/admin/login")
					.permitAll()
					.loginProcessingUrl("/admin/signin")
					.successHandler(loginSuccessHandler)
					.failureHandler(ajaxSimpleUrlAuthenticationFailureHandler)
				.and()
					.logout()
					.logoutUrl("/admin/logout")
					.permitAll()
					.logoutSuccessHandler(logoutSuccessHandler())
				.and()
					.rememberMe()
					.tokenRepository(persistentTokenRepository())
					.tokenValiditySeconds(1209600)
				.and()
					.csrf()
					.disable();
			// @formatter:on
		}

	}

	@Configuration
	@Order(order + 1)
	@Profile({ "dev", "prod" })
	public static class SecurityConfiguration2 extends WebSecurityConfigurerAdapter {

		@Override
		protected void configure(HttpSecurity http) throws Exception {

			http.authorizeRequests().antMatchers("/", "/free/**").permitAll().and().csrf().disable();

		}

	}

}
