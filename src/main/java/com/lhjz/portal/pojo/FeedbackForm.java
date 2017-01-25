package com.lhjz.portal.pojo;

import org.hibernate.validator.constraints.NotBlank;

public class FeedbackForm {

	@NotBlank(message = "反馈标题不能为空！")
	private String name;
	private String mail;
	private String phone;
	private String url;

	@NotBlank(message = "反馈内容不能为空！")
	private String content;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getMail() {
		return mail;
	}

	public void setMail(String mail) {
		this.mail = mail;
	}

	public String getPhone() {
		return phone;
	}

	public void setPhone(String phone) {
		this.phone = phone;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	@Override
	public String toString() {
		return "FeedbackForm [name=" + name + ", mail=" + mail + ", phone="
				+ phone + ", url=" + url + ", content=" + content + "]";
	}

}
