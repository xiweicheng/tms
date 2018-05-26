package com.lhjz.portal.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.junit.Test;

import com.opencsv.CSVReader;

public class Csv2mdTest {

	public static void main(String[] args) throws IOException {

		String path = new File(Class.class.getClass().getResource("/csv2md").getPath()).getAbsolutePath();
		System.out.println(path);

		String csv = "/Users/xiweicheng/temp/test.csv";

		String nodeCmd = StringUtil.replace("node {?1} {?2}", path, csv);

		System.out.println(nodeCmd);

		try {
			Process process = Runtime.getRuntime().exec(nodeCmd);

			String out = IOUtils.toString(process.getInputStream(), "UTF-8");

			System.out.println(out);

			process.waitFor();

			System.out.println("csv2md done!");

		} catch (IOException | InterruptedException e) {
			e.printStackTrace();
		}
	}

	@Test
	public void test() throws IOException {
		File file = new File("/Users/xiweicheng/temp/test.csv");
		//		CSVReader csvReader = new CSVReader(new InputStreamReader(new FileInputStream(file), "UTF-8"));
		CSVReader csvReader = new CSVReader(new InputStreamReader(new FileInputStream(file), Charset.forName("GBK")));
		//		String[] strs = csvReader.readNext();
		//		if (strs != null && strs.length > 0) {
		//			for (String str : strs)
		//				if (null != str && !str.equals(""))
		//					System.out.print(str + " , ");
		//			System.out.println("\n---------------");
		//		}
		List<String[]> list = csvReader.readAll();
		for (String[] ss : list) {
			for (String s : ss)
				if (null != s && !s.equals(""))
					System.out.print(s + " , ");
			System.out.println();
		}
		csvReader.close();
	}

}
