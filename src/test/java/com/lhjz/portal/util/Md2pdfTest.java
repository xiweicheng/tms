package com.lhjz.portal.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;

import org.apache.commons.io.FileUtils;

public class Md2pdfTest {

	public static void main(String[] args) throws IOException {

		String path = new File(Class.class.getClass().getResource("/md2pdf").getPath()).getAbsolutePath();
		String pathMd = path + "/test.md";
		String pathPdf = path + "/test.pdf";
		System.out.println(path);
		System.out.println("Start create md(" + pathMd + ")...");
		FileUtils.writeStringToFile(new File(pathMd), "## Markdown title\n> test...", "UTF-8");
		System.out.println("End create md(" + pathMd + ")...");
		// System.out.println(path);
		// System.out.println(pathMd);
		// System.out.println(pathPdf);
		String nodeCmd = StringUtil.replace("node {?1} {?2} {?3}", path, new File(pathMd).getAbsolutePath(),
				new File(pathPdf).getAbsolutePath());
		System.out.println(nodeCmd);
		try {
			Process process = Runtime.getRuntime().exec(nodeCmd);
			BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(process.getInputStream()));
			String s = null;
			while ((s = bufferedReader.readLine()) != null) {
				System.out.println(s);
			}
			process.waitFor();
			System.out.println("Md2pdf done!");
		} catch (IOException | InterruptedException e) {
			e.printStackTrace();
		}
	}

}
