package com.laijiaxiang.supreme.model.response;

import com.alibaba.fastjson2.JSON;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.laijiaxiang.supreme.exception.ExceptionCode;
import lombok.Getter;
import lombok.Setter;
import org.springframework.util.StringUtils;

/**
 * 定义返回结构
 *
 * @param <T>
 */
@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SupremeResponse<T> {

    public static final String SUCCESS_CODE = ExceptionCode.SUCCESS.getCode();
    public static final String FAIL_CODE = ExceptionCode.SYSTEM_BUSY.getCode();

    public static final String DEF_ERROR_MESSAGE = ExceptionCode.SYSTEM_BUSY.getMsg();

    private String code;

    private T data;

    private String msg = "ok";

    private long timestamp = System.currentTimeMillis();

    private SupremeResponse() {
        this.code = SUCCESS_CODE;
    }

    private SupremeResponse(T data) {
        this.code = SUCCESS_CODE;
        this.data = data;
    }

    public SupremeResponse(String code, String msg) {
        this.code = code;
        this.msg = msg;
    }

    public SupremeResponse(String code, T data, String msg) {
        this.code = code;
        this.data = data;
        this.msg = msg;
    }

    public static <T> SupremeResponse<T> success() {
        return new SupremeResponse<>();
    }

    public static <T> SupremeResponse<T> success(T data) {
        return new SupremeResponse<>(data);
    }

    public static <T> SupremeResponse<T> fail() {
        return new SupremeResponse<>(FAIL_CODE, DEF_ERROR_MESSAGE);
    }

    public static <T> SupremeResponse<T> fail(String msg) {
        return new SupremeResponse<>(FAIL_CODE, msg);
    }

    public static <T> SupremeResponse<T> fail(String code, String msg) {
        return new SupremeResponse<>(code, StringUtils.hasText(msg) ? msg : DEF_ERROR_MESSAGE);
    }

    public static <T> SupremeResponse<T> fail(ExceptionCode exceptionCode) {
        return new SupremeResponse<>(exceptionCode.getCode(), exceptionCode.getMsg());
    }

    public static <T> SupremeResponse<T> fail(ExceptionCode exceptionCode, String msg) {
        return new SupremeResponse<>(exceptionCode.getCode(), msg);
    }


    /**
     * 逻辑处理是否成功
     *
     * @return 是否成功
     */
    public boolean getIsSuccess() {
        return this.code == SUCCESS_CODE;
    }

    @Override
    public String toString() {
        return JSON.toJSONString(this);
    }
}
