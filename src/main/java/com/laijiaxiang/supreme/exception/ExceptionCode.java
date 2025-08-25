package com.laijiaxiang.supreme.exception;


import lombok.Getter;

/**
 * 自定义错误码
 */
@Getter
public enum ExceptionCode {

    //系统相关
    SUCCESS("SUPREME_SUCCESS", "成功"),
    SYSTEM_BUSY("SUPREME_SYSTEM_BUSY", "系统繁忙~请稍后再试~"),
    BAD_REQUEST("SUPREME_BAD_REQUEST", "请求方法或者请求参数有误"),
    UNAUTHORIZED("SUPREME_UNAUTHORIZED", "未认证或认证凭据无效"),
    FORBIDDEN("SUPREME_FORBIDDEN", "没有权限访问该资源"),
    SERVER_NOT_FOUND("SUPREME_SERVER_NOT_FOUND", "找不到服务或者资源");

    private final String code;
    private final String msg;

    ExceptionCode(String code, String msg) {
        this.code = code;
        this.msg = msg;
    }

}
