package com.lhjz.portal.util;

import java.util.Map;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.log4j.Logger;

public class BeanUtil {

	private static final Logger logger = Logger.getLogger(BeanUtil.class);

	public static void copyNotEmptyFields(Object src, Object dest) {

		try {
			Map<String, String> describe = BeanUtils.describe(src);

			for (String name : describe.keySet()) {
				try {
					if (describe.get(name) != null) {
						BeanUtils.setProperty(dest, name, describe.get(name));
					}
				} catch (Exception e) {
					e.printStackTrace();
					logger.error(e.getMessage(), e);
					continue;
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
			logger.error(e.getMessage(), e);
		}
	}

}
