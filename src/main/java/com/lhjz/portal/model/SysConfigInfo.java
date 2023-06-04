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

	// 文件预览服务地址
	String fileViewUrl;

	// 预览文件的访问地址是否使用base64编码预处理
	Boolean fileViewEncodePreviewUrl;
	
	// 是否允许用户注册
	Boolean userRegister;
	
	// 上传支持的最大文件大小限制
	Integer uploadMaxFileSize;

}
