/**
 * 版权所有 (TMS)
 */
package com.lhjz.portal.service.impl;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.lhjz.portal.constant.SysConstant;
import com.lhjz.portal.pojo.Enum.FileType;
import com.lhjz.portal.pojo.Enum.ToType;
import com.lhjz.portal.repository.FileRepository;
import com.lhjz.portal.service.FileService;
import com.lhjz.portal.util.ImageUtil;
import com.lhjz.portal.util.StringUtil;
import com.lhjz.portal.util.WebUtil;

import lombok.extern.log4j.Log4j;

/**
 * @author xi
 *
 */
@Log4j
@Service
public class FileServiceImpl implements FileService {

	@Autowired
	protected Environment env;

	@Autowired
	FileRepository fileRepository;

	@Override
	public List<com.lhjz.portal.entity.File> upload(HttpServletRequest request, String toType, String toId,
			MultipartFile[] files) throws Exception {

		log.debug("upload file start...");

		String realPath = WebUtil.getRealPath(request);

		List<com.lhjz.portal.entity.File> saveFiles = new ArrayList<com.lhjz.portal.entity.File>();

		for (MultipartFile file : files) {

			String originalFileName = file.getOriginalFilename().replaceAll("\\[|\\]|\\{|\\}|\\(|\\)", "\\$");
			int lIndex = originalFileName.lastIndexOf(".");
			String type = lIndex == -1 ? SysConstant.EMPTY : originalFileName.substring(lIndex);

			String uuid = UUID.randomUUID().toString();

			String uuidName = StringUtil.replace("{?1}{?2}", uuid, type);

			String storeAttachmentPath = env.getProperty("lhjz.upload.attachment.store.path");
			String path2 = null;
			FileType fileType = null;
			if (!ImageUtil.isImage(originalFileName)) { // 不是图片按附件上传处理
				FileUtils.forceMkdir(new File(realPath + storeAttachmentPath));
				String filePath = realPath + storeAttachmentPath + uuidName;
				// store into webapp dir
				file.transferTo(new File(filePath));

				path2 = storeAttachmentPath;
				fileType = FileType.Attachment;
			} else {
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
			}

			// 保存记录到数据库
			com.lhjz.portal.entity.File file2 = new com.lhjz.portal.entity.File();
			file2.setCreateDate(new Date());
			file2.setName(originalFileName);
			file2.setUsername(WebUtil.getUsername());
			file2.setUuidName(uuidName);
			file2.setPath(path2);
			file2.setType(fileType);

			if (StringUtil.isNotEmpty(toType)) {
				file2.setToType(ToType.valueOf(toType));
				file2.setToId(toId);
			}

			saveFiles.add(fileRepository.save(file2));

			// log(Action.Upload, Target.File, file2.getId());

		}

		return saveFiles;
	}

	@Override
	public com.lhjz.portal.entity.File base64(HttpServletRequest request, String toType, String toId, String dataURL,
			String type) throws Exception {

		log.debug("upload base64 start...");

		String realPath = WebUtil.getRealPath(request);

		String storePath = env.getProperty("lhjz.upload.img.store.path");
		int sizeOriginal = env.getProperty("lhjz.upload.img.scale.size.original", Integer.class);
		int sizeLarge = env.getProperty("lhjz.upload.img.scale.size.large", Integer.class);
		int sizeHuge = env.getProperty("lhjz.upload.img.scale.size.huge", Integer.class);

		// make upload dir if not exists
		FileUtils.forceMkdir(new File(realPath + storePath + sizeOriginal));
		FileUtils.forceMkdir(new File(realPath + storePath + sizeLarge));
		FileUtils.forceMkdir(new File(realPath + storePath + sizeHuge));

		String uuid = UUID.randomUUID().toString();

		// data:image/gif;base64,base64编码的gif图片数据
		// data:image/png;base64,base64编码的png图片数据
		// data:image/jpeg;base64,base64编码的jpeg图片数据
		// data:image/x-icon;base64,base64编码的icon图片数据

		String suffix = type.contains("png") ? ".png" : ".jpg";

		String uuidName = StringUtil.replace("{?1}{?2}", uuid, suffix);

		// relative file path
		String path = storePath + sizeOriginal + "/" + uuidName;// 原始图片存放
		String pathLarge = storePath + sizeLarge + "/" + uuidName;// 缩放图片存放
		String pathHuge = storePath + sizeHuge + "/" + uuidName;// 缩放图片存放

		// absolute file path
		String filePath = realPath + path;

		int index = dataURL.indexOf(",");

		// 原始图保存
		ImageUtil.decodeBase64ToImage(dataURL.substring(index + 1), filePath);
		// 缩放图
		// scale image size as thumbnail
		// 图片缩放处理.120*120
		ImageUtil.scale2(filePath, realPath + pathLarge, sizeLarge, sizeLarge, true);
		// 图片缩放处理.640*640
		ImageUtil.scale2(filePath, realPath + pathHuge, sizeHuge, sizeHuge, true);

		// 保存记录到数据库
		com.lhjz.portal.entity.File file2 = new com.lhjz.portal.entity.File();
		file2.setCreateDate(new Date());
		file2.setName(uuidName);
		file2.setUsername(WebUtil.getUsername());
		file2.setUuidName(uuidName);
		file2.setPath(storePath + sizeOriginal + "/");
		file2.setType(FileType.Image);

		if (StringUtil.isNotEmpty(toType)) {
			file2.setToType(ToType.valueOf(toType));
			file2.setToId(toId);
		}

		com.lhjz.portal.entity.File file = fileRepository.save(file2);

		// log(Action.Upload, Target.File, file2.getId());

		return file;
	}

}
