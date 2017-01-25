package com.lhjz.portal.config;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.context.annotation.Bean;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

//@Configuration
@Order(Ordered.HIGHEST_PRECEDENCE)
public class WebConfig {

	@Bean
	WebMvcConfigurerAdapter webMvcConfigurerAdapterBean() {
		return new WebMvcConfigurerAdapter() {

			@Override
			public void addInterceptors(InterceptorRegistry registry) {
				// super.addInterceptors(registry);
				System.out.println("add LoginInterceptor...");
				registry.addInterceptor(new LoginInterceptor())
						.addPathPatterns("/**");
			}

		};
	}

	private static class LoginInterceptor extends HandlerInterceptorAdapter {

		@Override
		public boolean preHandle(HttpServletRequest request,
				HttpServletResponse response, Object handler) throws Exception {
			// String username = request.getParameter("username");
			System.out.println(request.getRequestURI());
			return super.preHandle(request, response, handler);
		}

		@Override
		public void postHandle(HttpServletRequest request,
				HttpServletResponse response, Object handler,
				ModelAndView modelAndView) throws Exception {
			// String username = request.getParameter("username");
			// System.out.println(username);
		}

		@Override
		public void afterCompletion(HttpServletRequest request,
				HttpServletResponse response, Object handler, Exception ex)
				throws Exception {

			// String username = request.getParameter("username");
			// System.out.println(username);

		}

	}
}
