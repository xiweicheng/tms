package com.lhjz.portal.util;

public class FileUtil {

	// private static final Logger logger = Logger.getLogger(FileUtil.class);
	private static final String EMPTY = "";

	/**
	 * 获取文件名.
	 * 
	 * @author xiweicheng
	 * @creation 2014年4月26日 下午6:43:41
	 * @modification 2014年4月26日 下午6:43:41
	 * @param filePath
	 * @return
	 */
	public static String getName(String filePath) {
		if (filePath == null || filePath.length() == 0) {
			return EMPTY;
		} else {
			filePath = filePath.replaceAll("\\\\", "/");
		}

		int i = filePath.lastIndexOf("/");
		int j = filePath.lastIndexOf(".");

		if (i != -1) {
			if (j > i) {
				return filePath.substring(i + 1, j);
			} else if (i != filePath.length() - 1) {
				return filePath.substring(i + 1);
			}
		} else {
			if (j > 0) {
				return filePath.substring(0, j);
			} else if (j == 0) {
				return EMPTY;
			}
		}

		return filePath;
	}

	/**
	 * get file type, eg: .jpg
	 * 
	 * @param name
	 * @return
	 */
	public static String getType(String name) {

		if (StringUtil.isEmpty(name)) {
			return StringUtil.EMPTY;
		}

		int i = name.lastIndexOf(".");

		return (i != -1) ? name.substring(i) : StringUtil.EMPTY;
	}

	public static String joinPaths(String... paths) {

		StringBuilder sb = new StringBuilder();

		int len = paths.length;
		int cnt = 0;

		for (String path : paths) {

			path = path.replaceAll("\\\\", "/");

			if ((++cnt) < len) {
				if (path.endsWith("/")) {
					sb.append(path);
				} else {
					sb.append(path).append("/");
				}
			} else {
				sb.append(path);
			}
		}

		return sb.toString();
	}

}
