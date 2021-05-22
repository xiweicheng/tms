/**
 * 版权所有 (TMS)
 */
package com.lhjz.portal.service;

import java.util.List;

import javax.servlet.http.HttpServletRequest;

import com.lhjz.portal.exception.BizException;
import org.springframework.web.multipart.MultipartFile;

import com.lhjz.portal.entity.File;
import com.lhjz.portal.model.WordInfo;

/**
 * @author xi
 *
 */
public interface FileService {

	File uploadImg(HttpServletRequest request, MultipartFile file) throws BizException;
	
	boolean removeImg(String src) throws BizException;
	
	File uploadFile(HttpServletRequest request, MultipartFile file) throws BizException;
	
	boolean removeFile(String src) throws BizException;
	
	List<File> listImg() throws BizException;
	
	WordInfo word2html(HttpServletRequest request, MultipartFile file) throws BizException;

	boolean removeFileByAtId(String atId);
}
