/**
 * Cealus Li 2025/8/18
 * Copyright
 */
package org.geniusSociety.codelooms.domain.vo;


import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 关系
 *
 * @author Cealus Li 2025/8/18
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RelationVO {

    @Schema(name = "来源")
    private String from;
    @Schema(name = "目标")
    private String to;
}
