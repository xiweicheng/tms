package com.lhjz.portal.pojo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserExtraForm {

	private String username;

	// 座机号
	private String phone;

	// 手机号
	private String mobile;

	// 地理位置
	private String place;

	// 职级
	private String level;

	// 爱好
	private String hobby;
	
	// 个人介绍
	private String introduce;

}
