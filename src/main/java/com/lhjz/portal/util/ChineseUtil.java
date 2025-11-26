package com.lhjz.portal.util;

import lombok.extern.slf4j.Slf4j;

import java.nio.charset.StandardCharsets;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 中文处理工具类，用于检测和处理字符串中的中文及乱码问题
 */
@Slf4j
public class ChineseUtil {

    // 用于匹配空白字符的正则表达式模式
    private static final Pattern PATTERN = Pattern.compile("\\s*|\t*|\r*|\n*");

    /**
     * 私有构造方法，防止实例化
     */
    private ChineseUtil() {
    }

    /**
     * 判断字符是否为中文
     * @param c 要检查的字符
     * @return 如果是中文字符返回true，否则返回false
     */
    private static boolean isChinese(char c) {
        Character.UnicodeBlock ub = Character.UnicodeBlock.of(c);
        return (ub == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS
                || ub == Character.UnicodeBlock.CJK_COMPATIBILITY_IDEOGRAPHS
                || ub == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS_EXTENSION_A
                || ub == Character.UnicodeBlock.GENERAL_PUNCTUATION
                || ub == Character.UnicodeBlock.CJK_SYMBOLS_AND_PUNCTUATION
                || ub == Character.UnicodeBlock.HALFWIDTH_AND_FULLWIDTH_FORMS);
    }

    /**
     * 检查字符串是否包含乱码（基于统计方法）
     * @param strName 要检查的字符串
     * @return 如果乱码比例超过40%返回true，否则返回false
     */
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
        if (chLength == 0) {
            return false;
        }

        float result = count / chLength;

        return result > 0.4;
    }

    /**
     * 检查字符串是否包含乱码（基于Unicode范围匹配）
     * @param strName 要检查的字符串
     * @return 如果包含非中文字符返回true，否则返回false
     */
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

    /**
     * 检查字符串是否包含乱码（基于特殊字符检测）
     * @param str 要检查的字符串
     * @return 如果存在问号(?)或�等特殊字符返回true，否则返回false
     */
    public static boolean isMessyCode3(String str) {

        for (int i = 0; i < str.length(); i++) {
            char c = str.charAt(i);
            // 当从Unicode编码向某个字符集转换时，如果在该字符集中没有对应的编码，则得到0x3f（即问号字符?）
            //从其他字符集向Unicode编码转换时，如果这个二进制数在该字符集中没有标识任何的字符，则得到的结果是0xfffd
            if ((int) c == 0xfffd) {
                // 存在乱码
                return true;
            }
        }
        return false;
    }

    /**
     * 将字符串转换为中文编码
     * @param msg 要转换的字符串
     * @return 转换后的字符串，如果转换失败则返回原字符串
     */
    public static String toChinese(String msg) {
        String tempMsg = msg;
        if (isMessyCode(tempMsg)) {
            try {
                return new String(tempMsg.getBytes(StandardCharsets.ISO_8859_1), StandardCharsets.UTF_8);
            } catch (Exception e) {
                log.error(e.getMessage(), e);
            }
        }
        return tempMsg;
    }
}