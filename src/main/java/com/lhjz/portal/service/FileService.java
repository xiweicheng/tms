/**
 * 版权所有 (TMS)
 */
package com.lhjz.portal.service;

import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.springframework.web.multipart.MultipartFile;

import com.lhjz.portal.entity.File;
import com.lhjz.portal.model.WordInfo;

/**
 * @author xi
 *
 */
public interface FileService {

	File uploadImg(HttpServletRequest request, MultipartFile file) throws Exception;
	
	boolean removeImg(String src) throws Exception;
	
	File uploadFile(HttpServletRequest request, MultipartFile file) throws Exception;
	
	boolean removeFile(String src) throws Exception;
	
	List<File> listImg() throws Exception;
	
	WordInfo word2html(HttpServletRequest request, MultipartFile file) throws Exception;
}
