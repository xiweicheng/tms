package com.lhjz.portal.pojo;

import javax.validation.constraints.Pattern;
import javax.validation.constraints.Pattern.Flag;

import org.hibernate.validator.constraints.Length;
import org.hibernate.validator.constraints.NotBlank;

public class TranslateForm {

	@NotBlank(message = "翻译名称不能为空！")
	@Length(max = 255, message = "翻译名称长度不能超过255！")
	@Pattern(regexp = "^[a-zA-Z_][a-zA-Z0-9\\-_\\.]+[a-zA-Z0-9_]$", message = "翻译名称必须是[a-zA-Z-_.]组合,而且需要以[a-zA-Z_]开头和以[a-zA-Z0-9_]结尾!", flags = {
			Flag.CASE_INSENSITIVE })
	private String key;

	@Length(max = 2000, message = "描述过长！")
	private String desc;

	private String content;

	private String tags;

	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}

	public String getDesc() {
		return desc;
	}

	public void setDesc(String desc) {
		this.desc = desc;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public String getTags() {
		return tags;
	}

	public void setTags(String tags) {
		this.tags = tags;
	}

	@Override
	public String toString() {
		return "TranslateForm [key=" + key + ", desc=" + desc + ", content="
				+ content + ", tags=" + tags + "]";
	}

}
