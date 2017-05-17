/**
 * 版权所有 (TMS)
 */
package com.lhjz.portal.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.client.TestRestTemplate;
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
@SpringBootTest(classes = Application.class, webEnvironment = WebEnvironment.DEFINED_PORT)
public abstract class BaseControllerTest extends AbstractTestNGSpringContextTests {

	@Value("${local.server.port}")
	protected int port;

	@Autowired
	protected WebApplicationContext context;

	protected MockMvc mvc;

	protected RestTemplate template = new TestRestTemplate().getRestTemplate();

	protected String url(String path) {
		return String.format("%s://%s:%s/%s", "http", "localhost", port, path);
	}
}
