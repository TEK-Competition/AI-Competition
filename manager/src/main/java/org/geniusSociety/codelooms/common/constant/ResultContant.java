package org.geniusSociety.codelooms.common.constant;


import org.geniusSociety.codelooms.common.dto.BaseResult;

/**
 * 返回值常量
 *
 * @author Cealus
 * @date 2025/7/2
 */
public final class ResultContant {

    /**
     * 成功
     */
    public static final BaseResult SUCCESS = new BaseResult(Code.SUCCESS.code, Code.SUCCESS.desc);

    /**
     * 返回码
     */
    public enum Code {
        /**
         * 成功
         */
        SUCCESS(0, BaseConstant.SUCCESS),
        /**
         * 失败
         */
        FAIL(1, "fail"),
        /**
         * 错误
         */
        ERROR(2, "error");

        private final Integer code;

        private final String desc;

        Code(Integer code, String desc) {
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
}
