/**
 * Cealus Li 2025/7/13
 * Copyright
 */
package org.geniusSociety.codelooms.domain.bo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

/**
 * 项目信息
 *
 * @author Cealus Li
 * @date 2025/7/13
 */
@Getter
@Setter
@Schema(description = "项目信息")
public class ItemUpdateBO extends ItemBO {

    @Schema(title = "项目ID", example = "10000")
    private Long id;
}
