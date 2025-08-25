package com.laijiaxiang.supreme.exception;

import lombok.Getter;

/**
 * 自定义异常
 */
@Getter
public class ParameterValidateException extends RuntimeException {

    public ParameterValidateException() {
    }

    public ParameterValidateException(String message) {
        super(message);
    }

    public ParameterValidateException(String message, Throwable cause) {
        super(message, cause);
    }
    public ParameterValidateException(ExceptionCode exceptionCode, String message) {
        super(exceptionCode.getMsg() + message);
    }

    public ParameterValidateException(Throwable cause) {
        super(cause);
    }

    public ParameterValidateException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

}
