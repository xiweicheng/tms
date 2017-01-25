package com.lhjz.portal.util;

import org.apache.commons.io.FileUtils;
import org.testng.Assert;
import org.testng.annotations.Test;

public class FileUtilTest {

	@Test
	public void test() {
		System.out.println(FileUtil.getName("C:\\ddfd\\dfdfdff\\etetr.txt"));

		System.out.println(FileUtils.getFile("C:\\ddfd\\dfdfdff\\etetr.txt")
				.getName());
	}

	@Test
	public void joinPathsTest() {
		System.out.println(FileUtil.joinPaths("aa", "bb/", "cc"));
		Assert.assertFalse(FileUtil.joinPaths("aa", "bb/", "cc").endsWith("/"));
	}

	@Test
	public void joinPathsTest2() {
		System.out.println(FileUtil.joinPaths("aa", "bb/", "cc/"));
		Assert.assertTrue(FileUtil.joinPaths("aa", "bb/", "cc/").endsWith("/"));
	}

	@Test
	public void joinPathsTest3() {
		System.out.println(FileUtil.joinPaths("aa\\", "ee\\bb\\", "ff/cc/"));
		Assert.assertTrue(FileUtil.joinPaths("aa\\", "ee\\bb\\", "ff/cc/")
				.equals("aa/ee/bb/ff/cc/"));
	}

	@Test
	public void joinPathsTest4() {
		Assert.assertTrue(FileUtil
				.joinPaths("aa\\", "ee\\bb\\", "ff/cc/aa.txt").equals(
						"aa/ee/bb/ff/cc/aa.txt"));
	}
}
