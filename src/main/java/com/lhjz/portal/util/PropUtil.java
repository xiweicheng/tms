package com.lhjz.portal.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.util.Properties;

import lombok.extern.slf4j.Slf4j;

/**
 * classpath:writable.properties配置读写操作.
 * 
 * @creation 2014年3月26日 下午12:11:10
 * @modification 2014年3月26日 下午12:11:10
 * @company Canzs
 * @author xiweicheng
 * @version 1.0
 * 
 */
@Slf4j
public final class PropUtil {

	public static Properties properties;

	static {
		properties = new Properties();
		InputStream inputStream = null;

		try {
			inputStream = PropUtil.class.getClassLoader().getResourceAsStream("writable.properties");
			properties.load(inputStream);
		} catch (IOException e) {
			log.error(e.getMessage(), e);
		} finally {
			if (inputStream != null) {
				try {
					inputStream.close();
				} catch (IOException e) {
					log.error(e.getMessage(), e);
				}
			}
		}
	}

	/**
	 * 获取运行时可以修改的配置属性值.
	 * 
	 * @author xiweicheng
	 * @creation 2014年3月26日 下午5:30:11
	 * @modification 2014年3月26日 下午5:30:11
	 * @param key
	 * @param defaultVal
	 * @return
	 */
	public static String getValueByRealtime(String key, String defaultVal) {

		Properties properties = new Properties();
		InputStream inputStream = null;

		try {
			inputStream = PropUtil.class.getClassLoader().getResourceAsStream("writable.properties");
			properties.load(inputStream);

			return properties.getProperty(key, defaultVal);
		} catch (IOException e) {
			log.error(e.getMessage(), e);
		} finally {
			if (inputStream != null) {
				try {
					inputStream.close();
				} catch (IOException e) {
					log.error(e.getMessage(), e);
				}
			}
		}
		return null;
	}

	/**
	 * 获取运行时可以修改的配置属性值.
	 * 
	 * @author xiweicheng
	 * @creation 2014年3月26日 下午5:30:58
	 * @modification 2014年3月26日 下午5:30:58
	 * @param key
	 * @return
	 */
	public static String getValueByRealtime(String key) {

		return getValueByRealtime(key, null);
	}

	/**
	 * 获取配置属性值.
	 * 
	 * @author xiweicheng
	 * @creation 2014年3月26日 上午11:26:51
	 * @modification 2014年3月26日 上午11:26:51
	 * @param key
	 * @param defaultVal
	 * @return
	 */
	public static String getValue(String key, String defaultVal) {
		if (properties.containsKey(key)) {
			return properties.getProperty(key);
		} else {
			return defaultVal;
		}
	}

	/**
	 * 获取配置属性值.
	 * 
	 * @author xiweicheng
	 * @creation 2014年3月26日 上午11:28:15
	 * @modification 2014年3月26日 上午11:28:15
	 * @param key
	 * @return
	 */
	public static String getValue(String key) {
		return getValue(key, null);
	}

	/**
	 * 保存配置键值对
	 * 
	 * @author xiweicheng
	 * @creation 2014年3月26日 下午12:05:53
	 * @modification 2014年3月26日 下午12:05:53
	 * @param key
	 * @param val
	 * @return
	 */
	public static boolean save(String key, String val) {

		log.debug("保存配置键值对");

		properties.setProperty(key, val);

		try (FileOutputStream fos = new FileOutputStream(
				new File(PropUtil.class.getResource("/writable.properties").toURI()))) {
			properties.store(fos, "系统可读写配置");
			return true;
		} catch (URISyntaxException | IOException e) {
			log.error(e.getMessage(), e);
		}

		return false;
	}

}
