package com.lhjz.portal.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ChineseUtil {

	private static final Pattern PATTERN = Pattern.compile("\\s*|\t*|\r*|\n*");

	private static boolean isChinese(char c) {
		Character.UnicodeBlock ub = Character.UnicodeBlock.of(c);
		if (ub == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS
				|| ub == Character.UnicodeBlock.CJK_COMPATIBILITY_IDEOGRAPHS
				|| ub == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS_EXTENSION_A
				|| ub == Character.UnicodeBlock.GENERAL_PUNCTUATION
				|| ub == Character.UnicodeBlock.CJK_SYMBOLS_AND_PUNCTUATION
				|| ub == Character.UnicodeBlock.HALFWIDTH_AND_FULLWIDTH_FORMS) {
			return true;
		}
		return false;
	}

	public static boolean isMessyCode(String strName) {
		Matcher m = PATTERN.matcher(strName);
		String after = m.replaceAll("");
		String temp = after.replaceAll("\\p{P}", "");
		char[] ch = temp.trim().toCharArray();
		float chLength = 0;
		float count = 0;
		for (int i = 0; i < ch.length; i++) {
			char c = ch[i];
			if (!Character.isLetterOrDigit(c)) {
				if (!isChinese(c)) {
					count = count + 1;
				}
				chLength++;
			}
		}
		float result = count / chLength;
		if (result > 0.4) {
			return true;
		} else {
			return false;
		}
	}

	public static boolean isMessyCode2(String strName) {

		try {
			Matcher m = PATTERN.matcher(strName);
			String after = m.replaceAll("");
			String temp = after.replaceAll("\\p{P}", "");
			char[] ch = temp.trim().toCharArray();

			int length = (ch != null) ? ch.length : 0;
			for (int i = 0; i < length; i++) {
				char c = ch[i];
				if (!Character.isLetterOrDigit(c)) {
					String str = "" + ch[i];
					if (!str.matches("[\u4e00-\u9fa5]+")) {
						return true;
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			log.error(e.getMessage(), e);
		}

		return false;
	}

	public static boolean isMessyCode3(String str) {

		for (int i = 0; i < str.length(); i++) {
			char c = str.charAt(i);
			// 当从Unicode编码向某个字符集转换时，如果在该字符集中没有对应的编码，则得到0x3f（即问号字符?）
			//从其他字符集向Unicode编码转换时，如果这个二进制数在该字符集中没有标识任何的字符，则得到的结果是0xfffd
			//System.out.println("--- " + (int) c);
			if ((int) c == 0xfffd) {
				// 存在乱码
				//System.out.println("存在乱码 " + (int) c);
				return true;
			}
		}
		return false;
	}

	public static String toChinese(String msg) {
		String tempMsg = msg;
		if (isMessyCode(tempMsg)) {
			try {
				return new String(tempMsg.getBytes("ISO8859-1"), "UTF-8");
			} catch (Exception e) {
				log.error(e.getMessage(), e);
			}
		}
		return tempMsg;
	}
}