package com.lhjz.portal.util;

import org.testng.Assert;
import org.testng.annotations.Test;

public class TemplateUtilTest {

	@Test
	public void process() {

		String result = TemplateUtil.process("templates/test",
				MapUtil.objArr2Map("title", "标题", "header", "消息标题", "message",
						"消息..."));

		Assert.assertTrue(result.contains("消息..."));
	}
}
