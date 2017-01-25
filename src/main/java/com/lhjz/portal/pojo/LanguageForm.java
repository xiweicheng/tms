package com.lhjz.portal.pojo;

import javax.validation.constraints.Pattern;

import org.hibernate.validator.constraints.Length;
import org.hibernate.validator.constraints.NotBlank;

public class LanguageForm {

	@NotBlank(message = "语言名称不能为空！")
	@Length(min = 2, max = 10, message = "语言名称长度必须介于2到10(包含边界)！")
	@Pattern(regexp = "^[A-Za-z][A-Za-z_]*[A-Za-z]$", message = "语言名称必须是字母数字下划线组合,而且需要以[A-Za-z]开头和结尾!")
	private String name;

	@NotBlank(message = "语言描述不能为空！")
	@Length(max = 100, message = "语言描述过长！")
	private String desc;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDesc() {
		return desc;
	}

	public void setDesc(String desc) {
		this.desc = desc;
	}

	@Override
	public String toString() {
		return "LanguageForm [name=" + name + ", desc=" + desc + "]";
	}

}
