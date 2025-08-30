package org.geniusSociety.codelooms.common.vo;


import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.geniusSociety.codelooms.common.constant.ErrorCode;
import org.geniusSociety.codelooms.common.constant.ResultContant;
import org.geniusSociety.codelooms.common.dto.BaseResult;

/**
 * 通用数据返回结果VO
 *
 * @author Cealus
 * @date 2025/7/2
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public final class CommonResultVO<T> extends BaseResult {

    @Schema(name = "返回值")
    private T data;


    public CommonResultVO(Integer code, String msg, T data) {
        this.code = code;
        this.msg = msg;
        this.data = data;
    }

    public CommonResultVO(Integer code, String msg) {
        this.code = code;
        this.msg = msg;
    }

    public CommonResultVO(Integer code, Integer subCode, String msg) {
        this.code = code;
        this.msg = msg;
        this.subCode = subCode;
    }

    public static <T> CommonResultVO<T> success(T data) {
        return new CommonResultVO(ResultContant.Code.SUCCESS.getCode(), ResultContant.Code.SUCCESS.getDesc(), data);
    }

    public CommonResultVO<T> setData(T data) {
        this.data = data;
        return this;
    }

    public static CommonResultVO success() {
        return new CommonResultVO(ResultContant.Code.SUCCESS.getCode(), ResultContant.Code.SUCCESS.getDesc());
    }

    public static CommonResultVO fail(ErrorCode code) {
        return new CommonResultVO(ResultContant.Code.FAIL.getCode(), code.getCode(), code.getDesc());
    }

    public static CommonResultVO fail(Integer subCode, String msg) {
        return new CommonResultVO(ResultContant.Code.FAIL.getCode(), subCode, msg);
    }

}
