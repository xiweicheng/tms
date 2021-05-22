/**
 * 版权所有 (TMS)
 */
package com.lhjz.portal.exception;

/**
 * @author xiweicheng
 * @date 2021/5/22 1:39 下午
 */
public class BizException extends RuntimeException {

    public BizException(String message) {
        super(message);
    }

    public BizException(String message, Throwable cause) {
        super(message, cause);
    }

    public BizException(Throwable cause) {
        super(cause);
    }

    public BizException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

    public BizException() {
    }
}
