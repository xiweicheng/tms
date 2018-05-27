package com.lhjz.portal.util;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import org.apache.poi.EncryptedDocumentException;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.poi.xssf.extractor.XSSFExcelExtractor;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import lombok.extern.slf4j.Slf4j;

/**
 * Excel工具类.
 * 
 * @creation 2014年3月25日 上午9:55:48
 * @modification 2014年3月25日 上午9:55:48
 * @company Canzs
 * @author xiweicheng
 * @version 1.0
 * 
 */
public class ExcelUtil {

	private static XSSFExcelExtractor extractor;

	public static void main_(String[] args) {
		read("");
	}

	public static List<?> read(String path) {

		// Use a file
		try {
			try (InputStream inp = new FileInputStream("/Users/xiweicheng/temp/test.xlsx")) {
				XSSFWorkbook wb = (XSSFWorkbook) WorkbookFactory.create(inp);
				extractor = new XSSFExcelExtractor(wb);

				extractor.setFormulasNotResults(true);
				extractor.setIncludeSheetNames(false);
				String text = extractor.getText();

				System.out.println(text);
				wb.close();
			}
		} catch (EncryptedDocumentException | InvalidFormatException | IOException e) {
			e.printStackTrace();
		}

		return null;
	}

}
