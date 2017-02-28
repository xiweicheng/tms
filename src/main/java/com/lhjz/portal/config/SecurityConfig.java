/**
 * 版权所有 (TMS)
 */
package com.lhjz.portal.config;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.Date;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
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
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.authentication.SavedRequestAwareAuthenticationSuccessHandler;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;
import org.springframework.security.web.authentication.rememberme.JdbcTokenRepositoryImpl;
import org.springframework.security.web.authentication.rememberme.PersistentTokenRepository;
import org.springframework.stereotype.Component;

import com.lhjz.portal.component.AjaxAwareLoginUrlAuthenticationEntryPoint;
import com.lhjz.portal.component.AjaxSimpleUrlAuthenticationFailureHandler;
import com.lhjz.portal.component.AjaxSimpleUrlLogoutSuccessHandler;
import com.lhjz.portal.entity.security.User;
import com.lhjz.portal.model.RespBody;
import com.lhjz.portal.repository.UserRepository;
import com.lhjz.portal.util.JsonUtil;
import com.lhjz.portal.util.WebUtil;

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

	@Bean
	BCryptPasswordEncoder bCryptPasswordEncoderBean() {
		return new BCryptPasswordEncoder();
	}

	@Autowired
	public void configureGlobal(AuthenticationManagerBuilder auth) throws Exception {

		auth.jdbcAuthentication().dataSource(dataSource).passwordEncoder(bCryptPasswordEncoderBean());
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
			web.ignoring().antMatchers("/admin/file/download/**", 
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
					"/favicon.ico");
			// @formatter:on
		}

		@Override
		protected void configure(HttpSecurity http) throws Exception {

			// @formatter:off
			http
				.authorizeRequests()
				.antMatchers("/admin/**", "/api/**")
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

			http.antMatcher("/").authorizeRequests().anyRequest().permitAll().and().csrf().disable();

		}

	}

	@Configuration
	@Order(order + 2)
	@Profile({ "dev", "prod" })
	public static class SecurityConfiguration3 extends WebSecurityConfigurerAdapter {

		@Override
		protected void configure(HttpSecurity http) throws Exception {

			http.antMatcher("/free/**").authorizeRequests().anyRequest().permitAll().and().csrf().disable();

		}

	}

	@Configuration
	@Order(order)
	@Profile("test")
	public static class SecurityConfigurationTest extends WebSecurityConfigurerAdapter {

		@Autowired
		LoginSuccessHandler loginSuccessHandler;

		@Override
		protected void configure(HttpSecurity http) throws Exception {

			// @formatter:off
			http
				.antMatcher("/admin/**")
					.authorizeRequests()
					.antMatchers("/admin/file/download/**")
					.permitAll()
				.antMatchers("/admin/css/**", "/admin/img/**", "/admin/js/**")
					.permitAll()
				.anyRequest()
					.authenticated()
				.and()
					.formLogin()
					.loginPage("/admin/login")
					.permitAll()
					.loginProcessingUrl("/admin/signin")
					.successHandler(loginSuccessHandler)
				.and()
					.logout()
					.logoutUrl("/admin/logout")
					.logoutSuccessUrl("/admin/login")
				.and()
					.csrf()
					.disable();
			// @formatter:on

		}

	}

	@Component("loginSuccessHandler")
	public static class LoginSuccessHandler extends SavedRequestAwareAuthenticationSuccessHandler {

		@Autowired
		UserRepository userRepository;

		@Override
		public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
				Authentication authentication) throws IOException, ServletException {

			UserDetails uds = (UserDetails) authentication.getPrincipal();

			User loginUser = userRepository.findOne(uds.getUsername());
			if (loginUser != null) {
				loginUser.setLastLoginDate(new Date());
				loginUser.setLoginRemoteAddress(WebUtil.getIpAddr(request));

				long loginCount = loginUser.getLoginCount();
				loginUser.setLoginCount(++loginCount);

				userRepository.saveAndFlush(loginUser);
			}
			
			if (Arrays.asList("XMLHttpRequest|fetch".split("\\|")).contains(request.getHeader("X-Requested-With"))) {
				this.clearAuthenticationAttributes(request);
				// 对于ajax请求不重定向
				response.setContentType("application/json");
				try (PrintWriter writer = response.getWriter();) {
					writer.print(JsonUtil.toJson(RespBody.succeed()));
					writer.flush();
				}
			} else {
				this.setDefaultTargetUrl("/admin");
				this.setAlwaysUseDefaultTargetUrl(false);

				super.onAuthenticationSuccess(request, response, authentication);
			}

		}

	}

}
