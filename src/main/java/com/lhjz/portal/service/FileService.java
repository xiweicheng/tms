/**
 * 版权所有 (TMS)
 */
package com.lhjz.portal.service;

import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.springframework.web.multipart.MultipartFile;

/**
 * @author xi
 *
 */
public interface FileService {

	List<com.lhjz.portal.entity.File> upload(HttpServletRequest request, String toType, String toId,
			MultipartFile[] files) throws Exception;

	com.lhjz.portal.entity.File base64(HttpServletRequest request, String toType, String toId, String dataURL,
			String type) throws Exception;
}
