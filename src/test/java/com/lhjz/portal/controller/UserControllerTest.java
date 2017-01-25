package com.lhjz.portal.controller;

import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;

import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.test.context.support.WithSecurityContextTestExecutionListener;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

@ActiveProfiles(profiles = "test")
// @TestExecutionListeners(listeners = { ServletTestExecutionListener.class,
// DependencyInjectionTestExecutionListener.class,
// DirtiesContextTestExecutionListener.class,
// TransactionalTestExecutionListener.class,
// WithSecurityContextTestExecutionListener.class })
@TestExecutionListeners(listeners = { WithSecurityContextTestExecutionListener.class })
public class UserControllerTest extends BaseControllerTest {

	@BeforeClass
	public void setup() throws Exception {
		mvc = MockMvcBuilders.webAppContextSetup(context)
				.apply(springSecurity()).build();
	}

	@Test
	@WithMockUser(roles = "ADMIN")
	public void save() throws Exception {

		mvc.perform(
				MockMvcRequestBuilders.post("/admin/user/save")
						.contentType(MediaType.APPLICATION_FORM_URLENCODED)
						.param("username", "test")
						.param("password", "test123456")
						.accept(MediaType.APPLICATION_JSON))
				.andExpect(MockMvcResultMatchers.status().isOk())
				.andExpect(
						MockMvcResultMatchers.jsonPath("$.success").value(true));

	}

	@Test
	@WithMockUser
	public void save2() throws Exception {

		mvc.perform(
				MockMvcRequestBuilders.post("/admin/user/save")
						.contentType(MediaType.APPLICATION_FORM_URLENCODED)
						.param("username", "test")
						.param("password", "test123456")
						.accept(MediaType.APPLICATION_JSON))
				.andExpect(MockMvcResultMatchers.status().isOk())
				.andExpect(MockMvcResultMatchers.view().name("admin/error"));

	}

	@Test
	@WithMockUser(username = "test")
	public void update2() throws Exception {

		mvc.perform(
				MockMvcRequestBuilders.post("/admin/user/update2")
						.contentType(MediaType.APPLICATION_FORM_URLENCODED)
						.param("username", "test1")
						.param("password", "test123456")
						.accept(MediaType.APPLICATION_JSON))
				.andExpect(MockMvcResultMatchers.status().isOk())
				.andExpect(
						MockMvcResultMatchers.jsonPath("$.success")
								.value(false));

	}
}
