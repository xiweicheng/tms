package com.lhjz.portal.pojo;

import javax.validation.constraints.Pattern;

import org.hibernate.validator.constraints.NotBlank;

public class GroupForm {

	@NotBlank(message = "用户组名不能为空!")
	@Pattern(regexp = "^[a-z][a-z0-9_\\-]{2,49}$", message = "用户组名必须是3到50位小写字母数字_-组合,并且以字母开头!")
	private String groupName;

	public String getGroupName() {
		return groupName;
	}

	public void setGroupName(String groupName) {
		this.groupName = groupName;
	}

	@Override
	public String toString() {
		return "GroupForm [groupName=" + groupName + "]";
	}

}
