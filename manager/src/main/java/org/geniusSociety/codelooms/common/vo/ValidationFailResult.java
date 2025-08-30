/**
 * Cealus Li 2025/7/7
 * Copyright
 */
package org.geniusSociety.codelooms.common.vo;

import lombok.Getter;
import lombok.Setter;
import org.geniusSociety.codelooms.common.dto.BaseResult;

/**
 * 验证返回
 *
 * @author Cealus Li
 * @date 2025/7/7
 */
@Getter
@Setter
public class ValidationFailResult extends BaseResult {

    private String field;

    public ValidationFailResult(Integer code, Integer subCode) {
        this.code = code;
        this.subCode = subCode;
    }
}
