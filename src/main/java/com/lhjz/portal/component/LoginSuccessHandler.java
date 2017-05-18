package com.lhjz.portal.component;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.Date;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.SavedRequestAwareAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import com.lhjz.portal.entity.security.User;
import com.lhjz.portal.model.RespBody;
import com.lhjz.portal.repository.UserRepository;
import com.lhjz.portal.util.JsonUtil;
import com.lhjz.portal.util.WebUtil;

@Component("loginSuccessHandler")
public class LoginSuccessHandler extends SavedRequestAwareAuthenticationSuccessHandler {

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