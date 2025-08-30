/**
 * Cealus Li 2025/7/6
 * Copyright
 */
package org.geniusSociety.codelooms.domain.bo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.validator.constraints.Range;

/**
 * 项目信息
 *
 * @author Cealus Li
 * @date 2025/7/6
 */
@Getter
@Setter
public class ItemBO {

    @NotNull(message = "运行方式不能为空")
    @Range(min = 1, max = 2, message = "运行方式类型不合法")
    @Schema(title = "运行方式", example = "1:自动；2:手动")
    private Integer mode;

    @Schema(title = "项目说明")
    @Size(max = 1000, message = "项目说明不能超过100个字符")
    private String remark;
}
