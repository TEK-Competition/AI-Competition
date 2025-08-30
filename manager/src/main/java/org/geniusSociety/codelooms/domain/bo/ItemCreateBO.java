/**
 * Cealus Li 2025/7/6
 * Copyright
 */
package org.geniusSociety.codelooms.domain.bo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * 项目信息
 *
 * @author Cealus Li
 * @date 2025/7/6
 */
@Getter
@Setter
@Schema(description = "项目信息")
public class ItemCreateBO extends ItemBO {
    @NotBlank(message = "项目名称不能为空")
    @Size(min = 4, max = 32, message = "名称长度为4到32个字符")
    @Schema(title = "项目名称", example = "10000")
    private String name;
    @Valid
    @NotEmpty(message = "缺失必要文件")
    @Schema(title = "存储过程文件")
    private List<Long> spFiles;
    @Schema(title = "知识文件文件")
    private List<Long> knowledgeFiles;
}
