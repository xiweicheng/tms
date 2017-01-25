package com.lhjz.portal.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

import org.apache.commons.io.FileUtils;

/**
 * ZIP文件压缩和解压(要使用apache ant.jar以处理中文乱码)
 * 
 * @creation 2014年4月12日 下午7:15:00
 * @modification 2014年4月12日 下午7:15:00
 * @company
 * @author xiweicheng
 * @version 1.0
 * 
 */
public class ZipUtil {

	/**
	 * 压缩文件file成zip文件zipFile
	 * 
	 * @param file
	 *            要压缩的文件
	 * @param zipFile
	 *            压缩文件存放地方
	 * @throws Exception
	 */
	public static void zip(File file, File zipFile) throws Exception {
		ZipOutputStream output = null;
		try {
			output = new ZipOutputStream(new FileOutputStream(zipFile));
			// 顶层目录开始
			zipFile(output, file, "");
		} catch (Exception ex) {
			ex.printStackTrace();
		} finally {
			// 关闭流
			if (output != null) {
				output.flush();
				output.close();
			}
		}
	}

	/**
	 * 压缩文件为zip格式
	 * 
	 * @param output
	 *            ZipOutputStream对象
	 * @param file
	 *            要压缩的文件或文件夹
	 * @param basePath
	 *            条目根目录
	 * @throws IOException
	 */
	private static void zipFile(ZipOutputStream output, File file, String basePath) throws IOException {
		FileInputStream input = null;
		try {
			// 文件为目录
			if (file.isDirectory()) {
				// 得到当前目录里面的文件列表
				File list[] = file.listFiles();
				basePath = basePath + (basePath.length() == 0 ? "" : "/") + file.getName();
				// 循环递归压缩每个文件
				for (File f : list) {
					zipFile(output, f, basePath);
				}
			} else {
				// 压缩文件
				basePath = (basePath.length() == 0 ? "" : basePath + "/") + file.getName();
				// System.out.println(basePath);
				output.putNextEntry(new ZipEntry(basePath));
				input = new FileInputStream(file);
				int readLen = 0;
				byte[] buffer = new byte[1024 * 8];
				while ((readLen = input.read(buffer, 0, 1024 * 8)) != -1) {
					output.write(buffer, 0, readLen);
				}
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		} finally {
			// 关闭流
			if (input != null) {
				input.close();
			}
		}
	}

	/**
	 * 解压zip文件
	 * 
	 * @param zipFilePath
	 *            zip文件绝对路径
	 * @param unzipDirectory
	 *            解压到的目录
	 * @throws Exception
	 */
	public static void unzip(String zipFilePath, String unzipDirectory) throws Exception {
		// 定义输入输出流对象
		InputStream input = null;
		OutputStream output = null;
		ZipFile zipFile = null;

		try {
			// 创建文件对象
			File file = new File(zipFilePath);
			// 创建zip文件对象
			zipFile = new ZipFile(file);
			// 创建本zip文件解压目录
			String name = file.getName().substring(0, file.getName().lastIndexOf("."));
			File unzipFile = new File(unzipDirectory + "/" + name);
			if (unzipFile.exists()) {
				unzipFile.delete();
			}
			unzipFile.mkdir();
			// 得到zip文件条目枚举对象
			Enumeration<?> zipEnum = zipFile.entries();
			// 定义对象
			ZipEntry entry = null;
			String entryName = null, path = null;
			String names[] = null;
			int length;
			// 循环读取条目
			while (zipEnum.hasMoreElements()) {
				// 得到当前条目
				entry = (ZipEntry) zipEnum.nextElement();
				entryName = new String(entry.getName());
				// 用/分隔条目名称
				names = entryName.split("\\/");
				length = names.length;
				path = unzipFile.getAbsolutePath();
				for (int v = 0; v < length; v++) {
					if (v < length - 1) { // 最后一个目录之前的目录
						FileUtils.forceMkdir(new File(path += "/" + names[v] + "/"));
					} else { // 最后一个
						if (entryName.endsWith("/")) { // 为目录,则创建文件夹
							FileUtils.forceMkdir(new File(unzipFile.getAbsolutePath() + "/" + entryName));
						} else { // 为文件,则输出到文件
							input = zipFile.getInputStream(entry);
							output = new FileOutputStream(new File(unzipFile.getAbsolutePath() + "/" + entryName));
							byte[] buffer = new byte[1024 * 8];
							int readLen = 0;
							while ((readLen = input.read(buffer, 0, 1024 * 8)) != -1) {
								output.write(buffer, 0, readLen);
							}
						}
					}
				}
			}

		} catch (Exception ex) {
			ex.printStackTrace();
		} finally {
			// 关闭流
			if (input != null) {
				input.close();
			}
			if (output != null) {
				output.flush();
				output.close();
			}
			if (zipFile != null) {
				zipFile.close();
			}
		}
	}

	/**
	 * 压缩多个文件.
	 * 
	 * @author xiweicheng
	 * @creation 2014年4月12日 下午7:43:12
	 * @modification 2014年4月12日 下午7:43:12
	 * @param file
	 * @param filePaths
	 */
	public static void zip(File zipFile, String[] filePaths) {

		ZipOutputStream output = null;

		try {
			output = new ZipOutputStream(new FileOutputStream(zipFile));

			FileInputStream input = null;

			try {
				// 文件为目录

				for (String filePath : filePaths) {
					output.putNextEntry(new ZipEntry(new File(filePath).getName()));
					input = new FileInputStream(filePath);
					int readLen = 0;
					byte[] buffer = new byte[1024 * 8];
					while ((readLen = input.read(buffer, 0, 1024 * 8)) != -1) {
						output.write(buffer, 0, readLen);
					}
				}

			} catch (Exception ex) {
				ex.printStackTrace();
			} finally {
				// 关闭流
				if (input != null) {
					input.close();
				}
			}

		} catch (Exception ex) {
			ex.printStackTrace();
		} finally {
			// 关闭流
			if (output != null) {
				try {
					output.flush();
					output.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

}