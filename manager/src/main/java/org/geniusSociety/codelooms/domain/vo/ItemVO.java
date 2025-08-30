/**
 * Cealus Li 2025/7/6
 * Copyright
 */
package org.geniusSociety.codelooms.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 项目信息
 *
 * @author Cealus Li
 * @date 2025/7/6
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "项目信息")
public class ItemVO {

    @Schema(name = "ID")
    private Long id;

    @Schema(name = "项目名")
    private String name;

    @Schema(name = "执行模式")
    private Integer type;

    @Schema(name = "执行状态")
    private Integer status;
    @Schema(name = "表关系")
    private List<RelationVO> tableGraph;
    @Schema(name = "文件关系")
    private List<RelationVO> spGraph;

    @Schema(name = "说明")
    private String remark;

}
