package com.lhjz.portal;

import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.support.SpringBootServletInitializer;

public class ServletInitializer extends SpringBootServletInitializer {

	@Override
	protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
		return application.sources(Application.class);
	}

//	@Override
//	public void onStartup(ServletContext servletContext)
//			throws ServletException {
//		super.onStartup(servletContext);
//		DelegatingFilterProxy filter = new DelegatingFilterProxy(
//				"springSecurityFilterChain");
//		filter.setContextAttribute("org.springframework.web.servlet.FrameworkServlet.CONTEXT.dispatcher");
//		servletContext.addFilter("springSecurityFilterChain", filter)
//				.addMappingForUrlPatterns(null, false, "/*");
//	}

}
