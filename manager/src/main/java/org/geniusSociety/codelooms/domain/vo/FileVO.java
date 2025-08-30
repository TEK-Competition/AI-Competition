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
 * 项目文件
 *
 * @author Cealus Li
 * @date 2025/7/9
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "项目文件")
public class FileVO {
    @Schema(name = "ID")
    private Long id;

    @Schema(name = "文件名")
    private String name;

    @Schema(name = "存储过程")
    private String sp;

    @Schema(name = "转换后SQL")
    private String sql;
}

