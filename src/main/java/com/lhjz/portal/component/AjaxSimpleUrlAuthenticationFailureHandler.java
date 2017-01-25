package com.lhjz.portal.component;

import java.io.IOException;
import java.util.Arrays;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;
import org.springframework.stereotype.Component;

@Component
public class AjaxSimpleUrlAuthenticationFailureHandler extends SimpleUrlAuthenticationFailureHandler {

	@Override
	public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response,
			AuthenticationException exception) throws IOException, ServletException {
		if (Arrays.asList("XMLHttpRequest|fetch".split("\\|")).contains(request.getHeader("X-Requested-With"))) {

			response.setContentType("application/json");
			response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Access Denied!");
		} else {
			super.onAuthenticationFailure(request, response, exception);
		}
	}
}
