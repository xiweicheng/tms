package com.lhjz.portal.pojo;

import javax.validation.constraints.Pattern;

import org.apache.commons.lang.StringUtils;
import org.hibernate.validator.constraints.Email;
import org.hibernate.validator.constraints.NotBlank;

public class UserForm {

	@NotBlank(message = "用户名不能为空!")
	@Pattern(regexp = "^[a-z][a-z0-9]{2,49}$", message = "用户名必须是3到50位小写字母和数字组合,并且以字母开头!")
	private String username;

	private String password;

	@Email(message = "邮件格式不正确!")
	private String mail;

	private String name;

	private Boolean enabled;

	public String getUsername() {
		return StringUtils.trim(username);
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getPassword() {
		return StringUtils.trim(password);
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public Boolean getEnabled() {
		return enabled;
	}

	public void setEnabled(Boolean enabled) {
		this.enabled = enabled;
	}

	public String getMail() {
		return StringUtils.trim(mail);
	}

	public void setMail(String mail) {
		this.mail = mail;
	}

	public String getName() {
		return StringUtils.trim(name);
	}

	public void setName(String name) {
		this.name = name;
	}

	@Override
	public String toString() {
		return "UserForm [username=" + username + ", password=" + password
				+ ", mail=" + mail + ", name=" + name + ", enabled=" + enabled
				+ "]";
	}

}
