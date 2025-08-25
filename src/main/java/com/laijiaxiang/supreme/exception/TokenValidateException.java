package com.laijiaxiang.supreme.exception;

/**
 * token校验异常
 */
public class TokenValidateException extends RuntimeException {

    public TokenValidateException() {
    }

    public TokenValidateException(String message) {
        super(message);
    }

    public TokenValidateException(String message, Throwable cause) {
        super(message, cause);
    }

    public TokenValidateException(Throwable cause) {
        super(cause);
    }

    public TokenValidateException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

}
