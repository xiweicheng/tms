package com.lhjz.portal.util;

import java.util.regex.Pattern;

public class ValidateUtil {

    private ValidateUtil() {
    }

    static Pattern p = Pattern.compile(
            "^[a-z0-9!#$%&'*+/=?^_`{|}~-]+(?:\\.[a-z0-9!#$%&'*+/=?^_`{|}~-]+)*@(?:[a-z0-9](?:[a-z0-9-]*[a-z0-9])?\\.)+[a-z0-9](?:[a-z0-9-]*[a-z0-9])?$",
            Pattern.CASE_INSENSITIVE);

    public static boolean isEmail(String email) {

        return p.matcher(email).matches();
    }

}
