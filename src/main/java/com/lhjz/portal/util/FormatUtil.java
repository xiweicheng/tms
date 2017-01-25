package com.lhjz.portal.util;

import java.io.StringWriter;
import java.util.Map;

import freemarker.template.Configuration;
import freemarker.template.Template;

public final class FormatUtil {

	private static final String EMPTY_STRING = "";

	public static String format(String tpl, Map<String, Object> dataModelMap) {

		try {
			Configuration configuration = new Configuration(Configuration.VERSION_2_3_22);
			configuration.setClassicCompatible(true);
			configuration.setClassForTemplateLoading(FormatUtil.class, EMPTY_STRING);
			configuration.setDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
			configuration.setNumberFormat("");
			configuration.setDefaultEncoding("utf-8");
			Template template = Template.getPlainTextTemplate("", tpl, configuration);

			StringWriter sw = new StringWriter();

			template.process(dataModelMap, sw);

			return sw.toString();

		} catch (Exception ex) {
			ex.printStackTrace();
		}

		return EMPTY_STRING;

	}
}
