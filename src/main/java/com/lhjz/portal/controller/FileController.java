/**
 * 版权所有 (TMS)
 */
package com.lhjz.portal.controller;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.ui.ModelMap;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import com.google.common.collect.Lists;
import com.lhjz.portal.base.BaseController;
import com.lhjz.portal.constant.SysConstant;
import com.lhjz.portal.model.RespBody;
import com.lhjz.portal.model.UploadResult;
import com.lhjz.portal.pojo.Enum.Action;
import com.lhjz.portal.pojo.Enum.FileType;
import com.lhjz.portal.pojo.Enum.Status;
import com.lhjz.portal.pojo.Enum.Target;
import com.lhjz.portal.pojo.Enum.ToType;
import com.lhjz.portal.pojo.FileForm;
import com.lhjz.portal.repository.FileRepository;
import com.lhjz.portal.service.FileService;
import com.lhjz.portal.util.ChineseUtil;
import com.lhjz.portal.util.ExcelUtil;
import com.lhjz.portal.util.FileUtil;
import com.lhjz.portal.util.ImageUtil;
import com.lhjz.portal.util.StringUtil;
import com.lhjz.portal.util.WebUtil;
import com.opencsv.CSVReader;

/**
 * 
 * @author xi
 * 
 * @date 2015年3月28日 下午1:19:05
 * 
 */
@Controller
@RequestMapping("admin/file")
public class FileController extends BaseController {

	static Logger logger = LoggerFactory.getLogger(FileController.class);

	@Autowired
	FileRepository fileRepository;

	@Autowired
	FileService fileService;

	@RequestMapping(value = "list", method = RequestMethod.POST)
	@ResponseBody
	public RespBody list(HttpServletRequest request, HttpServletResponse response, ModelMap model, Locale locale) {

		String storePath = env.getProperty("lhjz.upload.img.store.path");
		int sizeLarge = env.getProperty("lhjz.upload.img.scale.size.large", Integer.class);
		int sizeHuge = env.getProperty("lhjz.upload.img.scale.size.huge", Integer.class);
		int sizeOriginal = env.getProperty("lhjz.upload.img.scale.size.original", Integer.class);

		// img relative path (eg:'upload/img/' & 640 & '/' )
		model.addAttribute("path", storePath + sizeOriginal + "/");
		model.addAttribute("pathLarge", storePath + sizeLarge + "/");
		model.addAttribute("pathHuge", storePath + sizeHuge + "/");
		// list all files
		model.addAttribute("imgs", fileRepository.findAll());

		return RespBody.succeed(model);
	}

	@RequestMapping(value = "update", method = RequestMethod.POST)
	@ResponseBody
	public RespBody update(HttpServletRequest request, HttpServletResponse response, Model model, Locale locale,
			@Valid FileForm fileForm, BindingResult bindingResult) {

		if (bindingResult.hasErrors()) {
			return RespBody.failed(bindingResult.getAllErrors().stream().map(err -> err.getDefaultMessage())
					.collect(Collectors.joining("<br/>")));
		}

		com.lhjz.portal.entity.File file = fileRepository.findOne(fileForm.getId());
		if (file.getStatus() == Status.Bultin) {
			return RespBody.failed("内置文件，不能修改！");
		}
		if (!hasAuth(file)) {
			return RespBody.failed("没有该文件的编辑权限！");
		}

		String oldName = file.getName();

		file.setName(fileForm.getName() + FileUtil.getType(file.getName()));

		logWithProperties(Action.Update, Target.File, "name", file.getName(), oldName);

		return RespBody.succeed(fileRepository.save(file));
	}

	private boolean hasAuth(com.lhjz.portal.entity.File file) {
		return isSuperOrCreator(file.getUsername());
	}

	@RequestMapping(value = "delete", method = RequestMethod.POST)
	@ResponseBody
	public RespBody delete(HttpServletRequest request, HttpServletResponse response, Model model, Locale locale,
			@RequestParam(value = "id", required = true) Long id) {

		com.lhjz.portal.entity.File file = fileRepository.findOne(id);
		if (file.getStatus() == Status.Bultin) {
			return RespBody.failed("内置文件，不能删除！");
		}
		if (!hasAuth(file)) {
			return RespBody.failed("没有该文件的删除权限！");
		}

		fileRepository.delete(id);

		log(Action.Delete, Target.File, id);

		return RespBody.succeed();
	}

	@RequestMapping(value = "upload", method = RequestMethod.POST)
	@ResponseBody
	public RespBody upload(HttpServletRequest request, @RequestParam(value = "toType", required = false) String toType, // Channel | User
			@RequestParam(value = "toId", required = false) String toId, // channelId | username
			@RequestParam("file") MultipartFile[] files) {

		logger.debug("upload file start...");

		String realPath = WebUtil.getRealPath(request);

		List<com.lhjz.portal.entity.File> saveFiles = new ArrayList<com.lhjz.portal.entity.File>();

		for (MultipartFile file : files) {

			String originalFileName = file.getOriginalFilename().replaceAll("\\[|\\]|\\{|\\}|\\(|\\)", "\\$");
			int lIndex = originalFileName.lastIndexOf(".");
			String type = lIndex == -1 ? SysConstant.EMPTY : originalFileName.substring(lIndex);

			String uuid = UUID.randomUUID().toString();

			String uuidName = StringUtil.replace("{?1}{?2}", uuid, type);

			try {
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
				file2.setUuid(UUID.randomUUID().toString());

				if (StringUtil.isNotEmpty(toType)) {
					file2.setToType(ToType.valueOf(toType));
					file2.setToId(toId);
				}

				saveFiles.add(fileRepository.save(file2));

				log(Action.Upload, Target.File, file2.getId());

			} catch (Exception e) {
				logger.error(e.getMessage(), e);
				return RespBody.failed(e.getMessage());
			}
		}

		// back relative file path
		return RespBody.succeed(saveFiles);
	}

	@RequestMapping(value = "base64", method = RequestMethod.POST)
	@ResponseBody
	public RespBody base64(HttpServletRequest request, @RequestParam(value = "toType", required = false) String toType, // Channel | User
			@RequestParam(value = "toId", required = false) String toId, // channelId | username
			@RequestParam("dataURL") String dataURL, @RequestParam("type") String type) {

		logger.debug("upload base64 start...");

		try {

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
			file2.setUuid(UUID.randomUUID().toString());

			if (StringUtil.isNotEmpty(toType)) {
				file2.setToType(ToType.valueOf(toType));
				file2.setToId(toId);
			}

			com.lhjz.portal.entity.File file = fileRepository.save(file2);

			log(Action.Upload, Target.File, file2.getId());

			return RespBody.succeed(file);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			return RespBody.failed(e.getMessage());
		}

	}

	private static String encodingFileName(String fileName) {
		String returnFileName = "";
		try {
			returnFileName = URLEncoder.encode(fileName, "UTF-8");
			returnFileName = StringUtils.replace(returnFileName, "+", "%20");
			if (returnFileName.length() > 100) {
				returnFileName = new String(fileName.getBytes("GBK"), "ISO8859-1");
				returnFileName = StringUtils.replace(returnFileName, " ", "%20");
			}
		} catch (UnsupportedEncodingException e) {
			logger.error(e.getMessage(), e);
		}
		return returnFileName;
	}
	
	@PostMapping("copyId2uuid")
	@ResponseBody
	public RespBody copyId2uuid() {

		List<com.lhjz.portal.entity.File> files = fileRepository.findAll();

		files.forEach(f -> {
			if (StringUtil.isEmpty(f.getUuid())) {
				f.setUuid(String.valueOf(f.getId()));
				fileRepository.saveAndFlush(f);
			}
		});

		return RespBody.succeed();
	}

	@RequestMapping(value = "download/{uuid}", method = RequestMethod.GET)
	public void download(HttpServletRequest request, HttpServletResponse response, @PathVariable String uuid)
			throws Exception {

		logger.debug("download file start...");

		com.lhjz.portal.entity.File file2 = fileRepository.findTopByUuidAndStatusNot(uuid, Status.Deleted);
		if (file2 == null) {
			try {
				response.sendError(404, "下载文件不存在!");
				return;
			} catch (IOException e) {
				logger.error(e.getMessage(), e);
			}
		}

		// 获取网站部署路径(通过ServletContext对象)，用于确定下载文件位置，从而实现下载
		String path = WebUtil.getRealPath(request);

		String filePath = path + file2.getPath() + file2.getUuidName();
		File file = new File(filePath);
		long fileLength = file.length();

		if (!file.exists()) {
			try {
				response.sendError(404, "下载文件不存在!");
				return;
			} catch (IOException e) {
				logger.error(e.getMessage(), e);
			}
		}

		// 1.设置文件ContentType类型，这样设置，会自动判断下载文件类型
		// response.setContentType("multipart/form-data");
		response.setContentType("application/x-msdownload;");
		response.addHeader("Content-Type", "text/html; charset=utf-8");
		// 2.设置文件头：最后一个参数是设置下载文件名
		response.setHeader("Content-Disposition", "attachment; fileName=" + encodingFileName(file2.getName().trim()));
		response.setHeader("Content-Length", String.valueOf(fileLength));

		java.io.BufferedInputStream bis = null;
		java.io.BufferedOutputStream bos = null;

		try {
			bis = new BufferedInputStream(new FileInputStream(file));
			bos = new BufferedOutputStream(response.getOutputStream());
			byte[] buff = new byte[2048];
			int bytesRead;
			while (-1 != (bytesRead = bis.read(buff, 0, buff.length))) {
				bos.write(buff, 0, bytesRead);
			}
		} catch (IOException e) {
			logger.error(e.getMessage(), e);
		} finally {
			if (bis != null) {
				bis.close();
			}
			if (bos != null) {
				bos.close();
			}
		}
	}

	@RequestMapping(value = "listByChannel", method = RequestMethod.GET)
	@ResponseBody
	public RespBody listByChannel(@RequestParam("name") String name, @RequestParam("type") String type,
			@RequestParam(value = "search", defaultValue = "") String search,
			@PageableDefault(sort = { "id" }, direction = Direction.DESC) Pageable pageable) {

		Page<com.lhjz.portal.entity.File> files = fileRepository.findByToTypeAndToIdAndTypeAndNameContainingIgnoreCase(
				ToType.Channel, name, FileType.valueOf(type), search, pageable);

		return RespBody.succeed(files);
	}

	@RequestMapping(value = "listByUser", method = RequestMethod.GET)
	@ResponseBody
	public RespBody listByUser(@RequestParam("name") String name, @RequestParam("type") String type,
			@RequestParam(value = "search", defaultValue = "") String search,
			@PageableDefault(sort = { "id" }, direction = Direction.DESC) Pageable pageable) {

		Page<com.lhjz.portal.entity.File> files = fileRepository.findByToTypeAndUsernameAndToIdAndTypeAndNameContainingIgnoreCase(
				ToType.User, WebUtil.getUsername(), name, FileType.valueOf(type), search, pageable);

		return RespBody.succeed(files);
	}

	@RequestMapping(value = "csv2md", method = RequestMethod.POST)
	@ResponseBody
	public RespBody csv2md(HttpServletRequest request, @RequestParam("file") MultipartFile[] files) {

		logger.info("csv2md start...");

		String realPath = WebUtil.getRealPath(request);

		List<String> list = Lists.newArrayList();

		for (MultipartFile file : files) {

			String originalFileName = file.getOriginalFilename().replaceAll("\\[|\\]|\\{|\\}|\\(|\\)", "\\$");
			int lIndex = originalFileName.lastIndexOf(".");
			String type = lIndex == -1 ? SysConstant.EMPTY : originalFileName.substring(lIndex);

			if (StringUtils.equalsIgnoreCase(".csv", type) || StringUtils.equalsIgnoreCase(".xls", type)
					|| StringUtils.equalsIgnoreCase(".xlsx", type)) { // check is csv or excel file

				String uuid = UUID.randomUUID().toString();

				String uuidName = StringUtil.replace("{?1}{?2}", uuid, type);

				try {
					String storeAttachmentPath = env.getProperty("lhjz.upload.attachment.store.path");

					FileUtils.forceMkdir(new File(realPath + storeAttachmentPath));
					String filePath = realPath + storeAttachmentPath + uuidName;
					// store into webapp dir
					file.transferTo(new File(filePath));

					// 保存记录到数据库
					com.lhjz.portal.entity.File file2 = new com.lhjz.portal.entity.File();
					file2.setCreateDate(new Date());
					file2.setName(originalFileName);
					file2.setUsername(WebUtil.getUsername());
					file2.setUuidName(uuidName);
					file2.setPath(storeAttachmentPath);
					file2.setType(FileType.Attachment);
					file2.setUuid(UUID.randomUUID().toString());

					fileRepository.save(file2);

					log(Action.Upload, Target.File, file2.getId());

					if (StringUtils.equalsIgnoreCase(".csv", type)) {
						list.add(csv2md2(filePath));
					} else {
						List<List<List<String>>> tables = ExcelUtil.read(filePath);
						for (List<List<String>> table : tables) {
							List<String[]> rows = table.stream().map(item -> item.toArray(new String[0]))
									.collect(Collectors.toList());
							list.add(toMdTable(rows));
						}
					}

				} catch (Exception e) {
					logger.error(e.getMessage(), e);
					return RespBody.failed(e.getMessage());
				}
			}
		}

		return RespBody.succeed(list);
	}

	private int maxColWitdh(List<String[]> rows, int colIndex) {
		logger.debug(String.valueOf(colIndex));
		return rows.stream().mapToInt(row -> row[colIndex].length()).max().getAsInt();
	}

	private boolean isMessyCode(List<String[]> rows) {
		return rows.stream()
				.anyMatch(row -> Arrays.asList(row).stream().anyMatch(cell -> ChineseUtil.isMessyCode3(cell)));
	}

	private String csv2md2(String csvPath) {

		try (CSVReader csvReader = new CSVReader(
				new InputStreamReader(new FileInputStream(csvPath), Charset.forName("UTF-8")))) {

			List<String[]> rowAll = csvReader.readAll();

			csvReader.close();

			if (isMessyCode(rowAll)) {
				try (CSVReader csvReader2 = new CSVReader(
						new InputStreamReader(new FileInputStream(csvPath), Charset.forName("GBK")))) {
					rowAll = csvReader2.readAll();
				}
			}

			return toMdTable(rowAll);

		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}

		return "";

	}

	private String toMdTable(List<String[]> rowAll) {

		if (rowAll == null || rowAll.isEmpty()) {
			return "";
		}

		List<String[]> rows = rowAll;

		List<String> row = Arrays.asList(rows.get(0));
		List<Integer> colWidths = row.stream().map(cell -> maxColWitdh(rows, row.indexOf(cell)))
				.collect(Collectors.toList());

		List<String> rows2 = rows.stream().map(item -> {
			List<String> cells = Arrays.asList(item);
			return "| " + cells.stream().map(cell -> {
				return StringUtils.rightPad(cell, colWidths.get(cells.indexOf(cell)) - cell.length() + 1);
			}).collect(Collectors.joining(" | ")) + " |";
		}).collect(Collectors.toList());

		String headerSplit = "|" + colWidths.stream().map(item -> StringUtils.rightPad("", item + 3, "-"))
				.collect(Collectors.joining("|")) + "|";

		rows2.add(1, headerSplit);

		return StringUtils.join(rows2, "\n") + "\n";
	}

	@ResponseBody
	@PostMapping("upload/img")
	public UploadResult uploadImg(HttpServletRequest request, @RequestParam("file") MultipartFile file,
			@RequestParam(value = "baseUrl", defaultValue = "") String baseUrl) {

		logger.debug("upload img start...");

		try {
			com.lhjz.portal.entity.File uploadImg = fileService.uploadImg(request, file);
			return UploadResult.builder().link(baseUrl + "/" + uploadImg.getPath() + uploadImg.getUuidName()).build();

		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			return UploadResult.builder().error(e.getMessage()).build();
		}

	}

	@ResponseBody
	@PostMapping("upload/file")
	public UploadResult uploadFile(HttpServletRequest request, @RequestParam("file") MultipartFile file,
			@RequestParam(value = "baseUrl", defaultValue = "") String baseUrl) {

		logger.debug("upload file start...");

		try {
			com.lhjz.portal.entity.File uploadFile = fileService.uploadFile(request, file);
			return UploadResult.builder().link(baseUrl + "/admin/file/download/" + uploadFile.getUuid()).build();

		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			return UploadResult.builder().error(e.getMessage()).build();
		}

	}

	@ResponseBody
	@PostMapping("upload/img/remove")
	public String uploadImgRemove(@RequestParam("src") String src) {

		logger.debug("upload img remove start...");

		try {
			fileService.removeImg(src);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}

		return "Success";

	}

	@ResponseBody
	@PostMapping("upload/file/remove")
	public String uploadFileRemove(@RequestParam("src") String src) {

		logger.debug("upload file remove start...");

		try {
			fileService.removeFile(src);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}

		return "Success";

	}

	@ResponseBody
	@GetMapping("upload/img/list")
	public Object uploadImgList() {

		logger.debug("upload img list start...");

		try {
			return fileService.listImg().stream().map(img -> {
				Map<Object, Object> imgs = new HashMap<Object, Object>();
				imgs.put("url", "/" + img.getPath() + img.getUuidName());

				String storePath = env.getProperty("lhjz.upload.img.store.path");
				int thumb = env.getProperty("lhjz.upload.img.scale.size.large", Integer.class);
				//				int thumb = env.getProperty("lhjz.upload.img.scale.size.huge", Integer.class);

				imgs.put("thumb", "/" + storePath + thumb + "/" + img.getUuidName());
				imgs.put("name", img.getName());

				return imgs;
			}).collect(Collectors.toList());
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}

		return new ArrayList<>();

	}

}
