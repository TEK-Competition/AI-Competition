package org.geniusSociety.codelooms.common.exception;

import org.geniusSociety.codelooms.common.constant.ErrorCode;

/**
 * 验证异常返回
 *
 * @author Cealus Li
 * @date 2025/7/12
 */
public class AssertException extends RuntimeException {

    private final int code;

    public AssertException() {
        super();
        this.code = ErrorCode.ERROR_INTERNAL.getCode();
    }

    public AssertException(int code, String message) {
        super(message);
        this.code = code;
    }

    public AssertException(int code, String message, Throwable cause) {
        super(message, cause);
        this.code = code;
    }

    public int getCode() {
        return code;
    }
}
