package com.lhjz.portal.util;

import com.lhjz.portal.exception.BizException;

import java.security.MessageDigest;

/**
 * 字符串加密
 *
 * @author xiweicheng
 * @version 1.0
 * @creation 2014年3月20日 上午11:24:21
 * @modification 2014年3月20日 上午11:24:21
 * @company Canzs
 */
public class EncoderUtil {

    private static final char[] HEX_DIGITS = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd',
            'e', 'f'};

    private EncoderUtil() {
    }

    /**
     * encode string
     *
     * @param algorithm
     * @param str
     * @return
     * @author xiweicheng
     * @creation 2014年3月20日 上午11:24:10
     * @modification 2014年3月20日 上午11:24:10
     */
    public static String encode(String algorithm, String str) {
        if (str == null) {
            return null;
        }
        try {
            MessageDigest messageDigest = MessageDigest.getInstance(algorithm);
            messageDigest.update(str.getBytes());
            return getFormattedText(messageDigest.digest());
        } catch (Exception e) {
            throw new BizException(e);
        }

    }

    /**
     * encode By SHA1
     *
     * @param str
     * @return
     * @author xiweicheng
     * @creation 2014年3月20日 上午11:23:13
     * @modification 2014年3月20日 上午11:23:13
     */
    public static String encodeBySHA1(String str) {
        return encode("SHA1", str);
    }

    /**
     * encode By MD5
     *
     * @param str
     * @return
     * @author xiweicheng
     * @creation 2014年3月20日 上午11:23:28
     * @modification 2014年3月20日 上午11:23:28
     */
    public static String encodeByMD5(String str) {
        return encode("MD5", str);
    }

    /**
     * 把密文转换成十六进制的字符串形式
     *
     * @param bytes
     * @return
     * @author xiweicheng
     * @creation 2014年3月20日 上午11:23:45
     * @modification 2014年3月20日 上午11:23:45
     */
    private static String getFormattedText(byte[] bytes) {
        int len = bytes.length;
        StringBuilder buf = new StringBuilder(len * 2);
        // 把密文转换成十六进制的字符串形式
        for (int j = 0; j < len; j++) {
            buf.append(HEX_DIGITS[(bytes[j] >> 4) & 0x0f]);
            buf.append(HEX_DIGITS[bytes[j] & 0x0f]);
        }
        return buf.toString();
    }

}