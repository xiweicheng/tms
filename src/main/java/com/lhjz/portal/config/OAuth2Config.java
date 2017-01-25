package com.lhjz.portal.config;

import javax.sql.DataSource;

import org.springframework.context.annotation.Bean;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.oauth2.provider.ClientDetailsService;
import org.springframework.security.oauth2.provider.OAuth2RequestFactory;
import org.springframework.security.oauth2.provider.approval.ApprovalStore;
import org.springframework.security.oauth2.provider.approval.ApprovalStoreUserApprovalHandler;
import org.springframework.security.oauth2.provider.approval.JdbcApprovalStore;
import org.springframework.security.oauth2.provider.approval.UserApprovalHandler;
import org.springframework.security.oauth2.provider.client.JdbcClientDetailsService;
import org.springframework.security.oauth2.provider.code.AuthorizationCodeServices;
import org.springframework.security.oauth2.provider.code.JdbcAuthorizationCodeServices;
import org.springframework.security.oauth2.provider.expression.OAuth2WebSecurityExpressionHandler;
import org.springframework.security.oauth2.provider.request.DefaultOAuth2RequestFactory;
import org.springframework.security.oauth2.provider.token.TokenStore;
import org.springframework.security.oauth2.provider.token.store.JdbcTokenStore;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

//@Configuration
public class OAuth2Config extends WebSecurityConfigurerAdapter {

	@Override
	protected void configure(HttpSecurity http) throws Exception {

		// @formatter:off
		http.antMatcher("/oauth/**")
			.authorizeRequests()
			.anyRequest()
			.permitAll()
//        	.hasAnyRole("ROLE_USER", "ROLE_ADMIN", "ROLE_SUPER")
//        	.antMatchers("/**").anonymous()
	        .and()
	        .exceptionHandling().accessDeniedPage("/admin/login?error")
	        .and()
	        .csrf()
	        .requireCsrfProtectionMatcher(new AntPathRequestMatcher("/oauth/authorize"))
	        .disable();
		// @formatter:on
	}

	@Override
	public void configure(WebSecurity web) throws Exception {
		web.expressionHandler(new OAuth2WebSecurityExpressionHandler());
	}

	@Override
	@Bean(name = "authenticationManager")
	public AuthenticationManager authenticationManagerBean() throws Exception {
		return super.authenticationManagerBean();
	}

	@Bean(name = "clientDetailsService")
	public ClientDetailsService clientDetailsService(DataSource dataSource) {
		return new JdbcClientDetailsService(dataSource);
	}

	@Bean(name = "tokenStore")
	public TokenStore tokenStore(DataSource dataSource) {
		return new JdbcTokenStore(dataSource);
	}

	@Bean(name = "approvalStore")
	public ApprovalStore approvalStore(DataSource dataSource) {
		return new JdbcApprovalStore(dataSource);
	}

	@Bean(name = "oAuth2RequestFactory")
	public OAuth2RequestFactory oAuth2RequestFactory(ClientDetailsService clientDetailsService) {
		return new DefaultOAuth2RequestFactory(clientDetailsService);
	}

	@Bean(name = "oauthUserApprovalHandler")
	public UserApprovalHandler oauthUserApprovalHandler(ApprovalStore approvalStore,
			ClientDetailsService clientDetailsService, OAuth2RequestFactory oAuth2RequestFactory) {
		ApprovalStoreUserApprovalHandler userApprovalHandler = new ApprovalStoreUserApprovalHandler();
		userApprovalHandler.setApprovalStore(approvalStore);
		userApprovalHandler.setClientDetailsService(clientDetailsService);
		userApprovalHandler.setRequestFactory(oAuth2RequestFactory);
		return userApprovalHandler;
	}

	@Bean(name = "jdbcAuthorizationCodeServices")
	public AuthorizationCodeServices jdbcAuthorizationCodeServices(DataSource dataSource) {
		return new JdbcAuthorizationCodeServices(dataSource);
	}

	// @Bean(name = "oauth2AuthenticationEntryPoint")
	// public OAuth2AuthenticationEntryPoint oauth2AuthenticationEntryPoint() {
	// return new OAuth2AuthenticationEntryPoint();
	// }

	// @Bean(name = "oauth2ClientDetailsUserService")
	// public ClientDetailsUserDetailsService
	// oauth2ClientDetailsUserService(ClientDetailsService clientDetailsService)
	// {
	// return new ClientDetailsUserDetailsService(clientDetailsService);
	// }
	//
	// @Bean(name = "oauth2AccessDecisionManager")
	// public UnanimousBased oauth2AccessDecisionManager() {
	// return new UnanimousBased(Arrays.asList(new ScopeVoter(), new
	// RoleVoter(), new AuthenticatedVoter()));
	// }
	//
	// @Bean(name = "oauth2AccessDeniedHandler")
	// public OAuth2AccessDeniedHandler oauth2AccessDeniedHandler() {
	// return new OAuth2AccessDeniedHandler();
	// }

}
