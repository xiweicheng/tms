/**
 * 版权所有 (TMS)
 */
package com.lhjz.portal.exception;

/**
 * 自定义业务异常类，继承自RuntimeException
 * 用于在业务逻辑处理过程中抛出特定异常
 *
 * @author xiweicheng
 * @date 2021/5/22 1:39 下午
 */
public class BizException extends RuntimeException {

    /**
     * 带消息参数的构造方法
     * @param message 异常信息
     */
    public BizException(String message) {
        super(message);
    }

    /**
     * 带消息和异常原因的构造方法
     * @param message 异常信息
     * @param cause 异常原因
     */
    public BizException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * 带异常原因的构造方法
     * @param cause 异常原因
     */
    public BizException(Throwable cause) {
        super(cause);
    }

    /**
     * 完整参数的构造方法
     * @param message 异常信息
     * @param cause 异常原因
     * @param enableSuppression 是否启用异常抑制
     * @param writableStackTrace 是否可写堆栈跟踪
     */
    public BizException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

    /**
     * 无参构造方法
     */
    public BizException() {
    }
}
