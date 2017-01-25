package com.lhjz.portal.component;

import java.io.IOException;
import java.util.Arrays;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.LoginUrlAuthenticationEntryPoint;

//@Component
public class AjaxAwareLoginUrlAuthenticationEntryPoint extends LoginUrlAuthenticationEntryPoint {

	public AjaxAwareLoginUrlAuthenticationEntryPoint(String loginFormUrl) {
		super(loginFormUrl);
	}

	public void commence(final HttpServletRequest request, final HttpServletResponse response,
			final AuthenticationException authException) throws IOException, ServletException {
		if (Arrays.asList("XMLHttpRequest|fetch".split("\\|")).contains(request.getHeader("X-Requested-With"))) {
			// 对于ajax请求不重定向 而是返回错误代码
			response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Access Denied");
		} else {
			super.commence(request, response, authException);
		}
	}

}
