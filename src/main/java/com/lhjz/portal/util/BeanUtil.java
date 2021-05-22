package com.lhjz.portal.util;

import java.util.Map;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.log4j.Logger;

public class BeanUtil {

    private static final Logger logger = Logger.getLogger(BeanUtil.class);

    private BeanUtil() {
    }

    public static void copyNotEmptyFields(Object src, Object dest) {

        try {
            Map<String, String> describe = BeanUtils.describe(src);

            describe.entrySet().forEach(es -> {
                try {
                    if (es.getValue() != null) {
                        BeanUtils.setProperty(dest, es.getKey(), es.getValue());
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    logger.error(e.getMessage(), e);
                }
            });

        } catch (Exception e) {
            e.printStackTrace();
            logger.error(e.getMessage(), e);
        }
    }

}
