package org.geniusSociety.codelooms.common.constant;

/**
 * 错误信息
 *
 * @author Cealus
 * @date 2025/7/2
 */
public enum ErrorCode {


    FAIL(10001, "操作失败"),

    ARGUMENTS_LOST_ERROR(10002, "缺少必要参数"),
    ARGUMENT_WRONG(10003, "请求参数不合法"),
    ARGUMENTS_NOT_ALLOWED(10004, "不支持该参数"),
    FILE_TYPE_FORBIDDEN(10005, "不支持的文件类型"),
    FILE_CONTENT_FORBIDDEN(10006, "不支持的文件内容"),
    NAME_ILLEGAL_ERROR(10035, "不能包含特殊字符"),
    DB_CONSTRAINS_ERROR(10055, "插入或更新数据时违反完整性约束"),

    REQUEST_UNAUTHORIZED(10401, "请求需要进行用户认证"),
    REQUEST_VERIFICATION_FAIL(10402, "请求用户验证失败"),

    REQUEST_FORBIDDEN(10403, "服务器拒绝请求"),

    REQUEST_NOT_FOUND(10404, "请求的资源不存在"),

    REQUEST_NOT_ALLOWED(10405, "不支持的请求方式"),
    REQUEST_TOO_LARGE(10413, "请求提交的实体数据大小超过了服务器的接收限制"),
    REQUEST_UNSUPPORTED_TYPE(10415, "请求中提交的实体并不是服务器中所支持的格式"),

    OPERATION_TIME_OUT(10015, "当前操作超时"),


    ERROR_INTERNAL(10500, "服务器遇到了一个未曾预料的状况，导致它无法完成对请求的处理"),
    SYSTEM_BUSY_ERROR(10502, "系统繁忙，请稍后再试"),
    ERROR_SERVICE_UNAVAILABLE(10501, "由于临时的服务器维护或者过载，服务器当前无法处理请求"),
    ERROR_GATEWAY_TIMEOUT(10504, "系统在请求一个远程服务没有收到回应"),
    ERROR_VERSION_NOT_SUPPORTED(10505, "服务器不支持或者拒绝支持请求使用的版本");

    private final Integer code;

    private final String desc;

    ErrorCode(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public Integer getCode() {
        return code;
    }

    public String getDesc() {
        return desc;
    }
}
