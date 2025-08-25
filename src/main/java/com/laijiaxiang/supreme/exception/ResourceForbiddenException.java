package com.laijiaxiang.supreme.exception;

/**
 * 资源权限相关异常
 */
public class ResourceForbiddenException extends RuntimeException {

    public ResourceForbiddenException() {
    }

    public ResourceForbiddenException(String message) {
        super(message);
    }

    public ResourceForbiddenException(String message, Throwable cause) {
        super(message, cause);
    }

    public ResourceForbiddenException(Throwable cause) {
        super(cause);
    }

    public ResourceForbiddenException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

}
