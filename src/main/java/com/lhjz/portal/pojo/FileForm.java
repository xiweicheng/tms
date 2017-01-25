package com.lhjz.portal.pojo;

import javax.validation.constraints.NotNull;

import org.hibernate.validator.constraints.Length;
import org.hibernate.validator.constraints.NotBlank;

public class FileForm {

	@NotNull(message = "文件ID不能为空！")
	private Long id;
	@NotBlank(message = "文件名不能为空！")
	@Length(max = 255, message = "文件名长度不能超过225！")
	private String name;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@Override
	public String toString() {
		return "FileForm [id=" + id + ", name=" + name + "]";
	}

}
