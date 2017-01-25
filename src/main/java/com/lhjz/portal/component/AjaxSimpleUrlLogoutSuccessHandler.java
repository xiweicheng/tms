package com.lhjz.portal.component;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.SimpleUrlLogoutSuccessHandler;

import com.lhjz.portal.model.RespBody;
import com.lhjz.portal.util.JsonUtil;

public class AjaxSimpleUrlLogoutSuccessHandler extends SimpleUrlLogoutSuccessHandler {
	
	private String targetUrl;

	public AjaxSimpleUrlLogoutSuccessHandler(String targetUrl) {
		super();
		this.targetUrl = targetUrl;
	}

	@Override
	public void onLogoutSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication)
			throws IOException, ServletException {
		if (Arrays.asList("XMLHttpRequest|fetch".split("\\|")).contains(request.getHeader("X-Requested-With"))) {

			response.setContentType("application/json");
			try (PrintWriter writer = response.getWriter();) {
				writer.print(JsonUtil.toJson(RespBody.succeed()));
				writer.flush();
			}

		} else {
			setDefaultTargetUrl(targetUrl);
			super.onLogoutSuccess(request, response, authentication);
		}
	}

}
