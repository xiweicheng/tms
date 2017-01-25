/**
 * 版权所有 (TMS)
 */
package com.lhjz.portal.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.boot.test.TestRestTemplate;
import org.springframework.boot.test.WebIntegrationTest;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.context.WebApplicationContext;

import com.lhjz.portal.Application;

/**
 * 
 * @author xi
 * 
 * @date 2015年3月28日 下午6:58:17
 * 
 */
@SpringApplicationConfiguration(classes = Application.class)
@WebIntegrationTest({ "server.port=0", "management.port=0" })
public abstract class BaseControllerTest extends
		AbstractTestNGSpringContextTests {

	@Value("${local.server.port}")
	protected int port;

	@Autowired
	protected WebApplicationContext context;

	protected MockMvc mvc;

	protected RestTemplate template = new TestRestTemplate();

	protected String url(String path) {
		return String.format("%s://%s:%s/%s", "http", "localhost", port, path);
	}
}
