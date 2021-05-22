package com.lhjz.portal.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.lhjz.portal.pojo.Enum.Key;
import com.lhjz.portal.pojo.Enum.Module;
import com.lhjz.portal.pojo.Enum.Page;
import com.lhjz.portal.pojo.Enum.Status;

public class EnumUtil {

	private static final Logger logger = LoggerFactory
			.getLogger(EnumUtil.class);

	private EnumUtil() {
	}

	public static Status status(String status) {

		for (Status sts : Status.values()) {
			if (sts.name().equalsIgnoreCase(status)) {
				logger.info("Mapped status: {} <-> {}", status, sts.name());
				return sts;
			}
		}

		logger.info("Unmapped status: {}. Return: {}", status,
				Status.Unknow.name());

		return Status.Unknow;
	}

	public static Key key(String key) {

		for (Key k : Key.values()) {
			if (k.name().equalsIgnoreCase(key)) {
				logger.info("Mapped key: {} <-> {}", key, k.name());
				return k;
			}
		}

		logger.info("Unmapped key: {}. Return: {}", key, Key.Unknow.name());

		return Key.Unknow;
	}

	public static Page page(String page) {

		for (Page p : Page.values()) {
			if (p.name().equalsIgnoreCase(page)) {
				logger.info("Mapped page: {} <-> {}", page, p.name());
				return p;
			}
		}

		logger.info("Unmapped page: {}. Return: {}", page, Page.Unknow.name());

		return Page.Unknow;
	}

	public static Module module(String module) {

		for (Module m : Module.values()) {
			if (m.name().equalsIgnoreCase(module)) {
				logger.info("Mapped module: {} <-> {}", module, m.name());
				return m;
			}
		}

		logger.info("Unmapped module: {}. Return: {}", module,
				Module.Unknow.name());

		return Module.Unknow;
	}

}
