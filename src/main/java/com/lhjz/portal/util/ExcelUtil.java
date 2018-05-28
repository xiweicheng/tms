package com.lhjz.portal.util;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
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
@Slf4j
public class ExcelUtil {

	public static List<List<List<String>>> read(String path) {

		if (path.toLowerCase().endsWith(".xls")) {
			return readXls(path);
		} else {
			return readXlsx(path);
		}
	}

	private static int maxCols(HSSFSheet sheet) {
		int maxCol = 0;
		for (int j = 0; j < sheet.getLastRowNum() + 1; j++) {
			HSSFRow row = sheet.getRow(j);
			if (row != null) {
				int num = row.getLastCellNum();
				maxCol = num > maxCol ? num : maxCol;
			}
		}
		return maxCol;
	}

	private static int maxCols(XSSFSheet sheet) {
		int maxCol = 0;
		for (int j = 0; j < sheet.getLastRowNum() + 1; j++) {
			XSSFRow row = sheet.getRow(j);
			if (row != null) {
				int num = row.getLastCellNum();
				maxCol = num > maxCol ? num : maxCol;
			}
		}
		return maxCol;
	}

	public static List<List<List<String>>> readXls(String filePath) {
		List<List<List<String>>> tables = new ArrayList<>();
		try (HSSFWorkbook workbook = new HSSFWorkbook(new FileInputStream(new File(filePath)))) {
			HSSFSheet sheet = null;
			for (int i = 0; i < workbook.getNumberOfSheets(); i++) {// 获取每个Sheet表
				sheet = workbook.getSheetAt(i);
				List<List<String>> table = new ArrayList<>();
				for (int j = 0; j < sheet.getLastRowNum() + 1; j++) {// getLastRowNum，获取最后一行的行标
					HSSFRow row = sheet.getRow(j);
					if (row != null) {
						List<String> tr = new ArrayList<>();
						for (int k = 0; k < maxCols(sheet); k++) {// getLastCellNum，是获取最后一个不为空的列是第几个
							if (row.getCell(k) != null) { // getCell 获取单元格数据
								row.getCell(k).setCellType(CellType.STRING);
								tr.add(row.getCell(k).getStringCellValue());
							} else {
								tr.add("");
							}
						}
						table.add(tr);
					}
				}
				tables.add(table);
			}
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}

		return tables;
	}

	public static List<List<List<String>>> readXlsx(String filePath) {

		List<List<List<String>>> tables = new ArrayList<>();
		try (XSSFWorkbook workbook = new XSSFWorkbook(new FileInputStream(new File(filePath)))) {
			XSSFSheet sheet = null;
			for (int i = 0; i < workbook.getNumberOfSheets(); i++) {// 获取每个Sheet表
				sheet = workbook.getSheetAt(i);
				List<List<String>> table = new ArrayList<>();
				for (int j = 0; j < sheet.getLastRowNum() + 1; j++) {// getLastRowNum，获取最后一行的行标
					XSSFRow row = sheet.getRow(j);
					if (row != null) {
						List<String> tr = new ArrayList<>();
						for (int k = 0; k < maxCols(sheet); k++) {// getLastCellNum，是获取最后一个不为空的列是第几个
							if (row.getCell(k) != null) { // getCell 获取单元格数据
								row.getCell(k).setCellType(CellType.STRING);
								tr.add(row.getCell(k).getStringCellValue());
							} else {
								tr.add("");
							}
						}
						table.add(tr);
					}
				}
				tables.add(table);
			}
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}

		return tables;
	}

}
