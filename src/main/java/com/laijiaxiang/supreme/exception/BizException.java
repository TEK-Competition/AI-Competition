package com.laijiaxiang.supreme.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

/**
 * 自定义异常
 */
@Getter
public class BizException extends RuntimeException {

    private HttpStatus httpStatus;

    public BizException() {
    }

    public BizException(HttpStatus httpStatus, String message) {
        super(message);
        this.httpStatus = httpStatus;
    }

}
