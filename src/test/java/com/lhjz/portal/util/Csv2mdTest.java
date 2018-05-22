package com.lhjz.portal.util;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.IOUtils;

public class Csv2mdTest {

	public static void main(String[] args) throws IOException {

		String path = new File(Class.class.getClass().getResource("/csv2md").getPath()).getAbsolutePath();
		System.out.println(path);

		String csv = "/Users/xiweicheng/temp/test.csv";

		String nodeCmd = StringUtil.replace("node {?1} {?2}", path, csv);

		System.out.println(nodeCmd);

		try {
			Process process = Runtime.getRuntime().exec(nodeCmd);

			String out = IOUtils.toString(process.getInputStream());

			System.out.println(out);

			process.waitFor();

			System.out.println("csv2md done!");

		} catch (IOException | InterruptedException e) {
			e.printStackTrace();
		}
	}

}
