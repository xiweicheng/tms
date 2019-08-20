/**
 * 版权所有 (TMS)
 */
package com.lhjz.portal.model;

import lombok.Builder;
import lombok.Data;

/**
 * 
 * @author xi
 * 
 * @date 2016年5月20日 下午8:38:54
 * 
 */
@Data
@Builder
public class SysConfigInfo {

	String fileViewUrl;
	
	Boolean userRegister;

}
