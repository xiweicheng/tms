/**
 * 版权所有 (TMS)
 */
package com.lhjz.portal.service.impl;

import java.io.File;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.lhjz.portal.constant.SysConstant;
import com.lhjz.portal.pojo.Enum.FileType;
import com.lhjz.portal.pojo.Enum.Status;
import com.lhjz.portal.pojo.Enum.ToType;
import com.lhjz.portal.repository.FileRepository;
import com.lhjz.portal.service.FileService;
import com.lhjz.portal.util.ImageUtil;
import com.lhjz.portal.util.StringUtil;
import com.lhjz.portal.util.WebUtil;

/**
 * @author xi
 *
 */
@Service
@Transactional
public class FileServiceImpl implements FileService {

	@Autowired
	Environment env;

	@Autowired
	FileRepository fileRepository;

	@Override
	public com.lhjz.portal.entity.File uploadImg(HttpServletRequest request, MultipartFile file) throws Exception {

		String realPath = WebUtil.getRealPath(request);

		String originalFileName = file.getOriginalFilename().replaceAll("\\[|\\]|\\{|\\}|\\(|\\)", "\\$");
		int lIndex = originalFileName.lastIndexOf(".");
		String type = lIndex == -1 ? SysConstant.EMPTY : originalFileName.substring(lIndex);

		String uuid = UUID.randomUUID().toString();

		String uuidName = StringUtil.replace("{?1}{?2}", uuid, type);

		String path2 = null;
		FileType fileType = null;

		String storePath = env.getProperty("lhjz.upload.img.store.path");
		int sizeOriginal = env.getProperty("lhjz.upload.img.scale.size.original", Integer.class);
		int sizeLarge = env.getProperty("lhjz.upload.img.scale.size.large", Integer.class);
		int sizeHuge = env.getProperty("lhjz.upload.img.scale.size.huge", Integer.class);

		// make upload dir if not exists
		FileUtils.forceMkdir(new File(realPath + storePath + sizeOriginal));
		FileUtils.forceMkdir(new File(realPath + storePath + sizeLarge));
		FileUtils.forceMkdir(new File(realPath + storePath + sizeHuge));

		// relative file path
		String path = storePath + sizeOriginal + "/" + uuidName;// 原始图片存放
		String pathLarge = storePath + sizeLarge + "/" + uuidName;// 缩放图片存放
		String pathHuge = storePath + sizeHuge + "/" + uuidName;// 缩放图片存放

		// absolute file path
		String filePath = realPath + path;
		// store into webapp dir
		file.transferTo(new File(filePath));

		// scale image size as thumbnail
		// 图片缩放处理.120*120
		ImageUtil.scale2(filePath, realPath + pathLarge, sizeLarge, sizeLarge, true);
		// 图片缩放处理.640*640
		ImageUtil.scale2(filePath, realPath + pathHuge, sizeHuge, sizeHuge, true);

		path2 = storePath + sizeOriginal + "/";
		fileType = FileType.Image;

		// 保存记录到数据库
		com.lhjz.portal.entity.File file2 = new com.lhjz.portal.entity.File();
		file2.setCreateDate(new Date());
		file2.setName(originalFileName);
		file2.setUsername(WebUtil.getUsername());
		file2.setUuidName(uuidName);
		file2.setPath(path2);
		file2.setType(fileType);

		file2.setToType(ToType.Blog);

		return fileRepository.save(file2);

	}

	@Override
	public boolean removeImg(String src) {

		// src /upload/img/120/54a68013-9ea5-4722-b90b-c0c17260f600.png
		String[] split = StringUtils.split(src, "/");
		if (split != null && split.length > 0) {
			com.lhjz.portal.entity.File img = fileRepository.findTopByUuidName(split[split.length - 1]);
			if (img != null) {
				img.setStatus(Status.Deleted);
				fileRepository.saveAndFlush(img);
			}
		}

		return true;
	}

	@Override
	public com.lhjz.portal.entity.File uploadFile(HttpServletRequest request, MultipartFile file) throws Exception {

		String realPath = WebUtil.getRealPath(request);

		String originalFileName = file.getOriginalFilename().replaceAll("\\[|\\]|\\{|\\}|\\(|\\)", "\\$");
		int lIndex = originalFileName.lastIndexOf(".");
		String type = lIndex == -1 ? SysConstant.EMPTY : originalFileName.substring(lIndex);

		String uuid = UUID.randomUUID().toString();

		String uuidName = StringUtil.replace("{?1}{?2}", uuid, type);

		String storeAttachmentPath = env.getProperty("lhjz.upload.attachment.store.path");
		String path2 = null;
		FileType fileType = null;
		FileUtils.forceMkdir(new File(realPath + storeAttachmentPath));
		String filePath = realPath + storeAttachmentPath + uuidName;
		// store into webapp dir
		file.transferTo(new File(filePath));

		path2 = storeAttachmentPath;
		fileType = FileType.Attachment;

		// 保存记录到数据库
		com.lhjz.portal.entity.File file2 = new com.lhjz.portal.entity.File();
		file2.setCreateDate(new Date());
		file2.setName(originalFileName);
		file2.setUsername(WebUtil.getUsername());
		file2.setUuidName(uuidName);
		file2.setPath(path2);
		file2.setType(fileType);

		file2.setToType(ToType.Blog);

		return fileRepository.save(file2);

	}

	@Override
	public boolean removeFile(String src) {

		// src /admin/file/download/10
		String[] split = StringUtils.split(src, "/");
		if (split != null && split.length > 0) {
			com.lhjz.portal.entity.File img = fileRepository.findOne(Long.valueOf(split[split.length - 1]));
			if (img != null) {
				img.setStatus(Status.Deleted);
				fileRepository.saveAndFlush(img);
			}
		}

		return true;
	}

	@Override
	public List<com.lhjz.portal.entity.File> listImg() throws Exception {

		return fileRepository.findTop40ByTypeAndStatusNot(FileType.Image, Status.Deleted,
				new Sort(Direction.DESC, "id"));
	}

}
