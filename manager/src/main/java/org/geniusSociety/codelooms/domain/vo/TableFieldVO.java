/**
 * Cealus Li 2025/7/9
 * Copyright
 */
package org.geniusSociety.codelooms.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 表字段
 *
 * @author Cealus Li
 * @date 2025/7/9
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TableFieldVO {

    @Schema(name = "ID")
    private Long id;
    @Schema(name = "字段名")
    private String name;
    @Schema(name = "数据类型")
    private String dataType;
    @Schema(name = "字段说明")
    private String description;

}

