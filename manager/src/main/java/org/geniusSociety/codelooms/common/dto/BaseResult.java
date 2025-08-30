package org.geniusSociety.codelooms.common.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import org.geniusSociety.codelooms.common.constant.ResultContant;

import java.util.Objects;

/**
 * 返回结果内容
 *
 * @author Cealus
 * @date 2025/7/2
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "响应返回数据对象")
public class BaseResult {

    @Schema(name = "状态码", example = "0")
    protected Integer code;

    @Schema(name = "错误码", example = "500")
    protected Integer subCode;

    @Schema(name = "返回信息", example = "success")
    protected String msg;

    public BaseResult(Integer code, String msg) {
        this.code = code;
        this.msg = msg;
    }

    /**
     * 结果判断
     *
     * @return
     */
    public boolean isSuccess() {
        return Objects.equals(ResultContant.Code.SUCCESS.getCode(), this.code);
    }

    /**
     * 成功
     *
     * @return
     */
    public static BaseResult success() {
        return ResultContant.SUCCESS;
    }

    public static BaseResult fail() {
        return new BaseResult(ResultContant.Code.FAIL.getCode(), ResultContant.Code.FAIL.getDesc());
    }

}
